package foo.ree.demos.dumpdex;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import dalvik.system.DexClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by fooree on 2018/1/19.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@SuppressLint({"ObsoleteSdkInt", "SetWorldReadable", "SetWorldWritable"})
public class DexHook implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    private static final String TAG = DexHook.class.getSimpleName();
        private static final File DUMP_PATH = new File("/data/local/tmp/dumpdex/");
    private final XSharedPreferences shares = new XSharedPreferences(Constants.THIS_MODULE, Constants.HOOK_CONFIG);

    @Override
    public void handleLoadPackage(final LoadPackageParam lp) throws Throwable {
        if (Build.VERSION.SDK_INT < 19 || Build.VERSION.SDK_INT > 25) {
            return;
        }

        shares.reload();
        String pkg = shares.getString(Constants.SELECT_APPLICATION, null);
        if (lp.packageName.equals(pkg)) {
            Log.i(TAG, "Loading " + lp.packageName);

            final ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
            final ClassLoader bootLoader = DexClassLoader.class.getClassLoader();

            findAndHookMethod(ClassLoader.class, "loadClass", String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ClassLoader loader = (ClassLoader) param.thisObject;
                    if (param.hasThrowable()
                            || loader == bootLoader
                            || loader == systemLoader
                            || loader.getParent() == systemLoader) {
                        return;
                    }

                    Class<?> clazz = (Class<?>) param.getResult();
                    Object dex = XposedHelpers.callMethod(clazz, "getDex");
                    byte[] data = (byte[]) XposedHelpers.callMethod(dex, "getBytes");

                    File file = new File(DUMP_PATH, data.length + ".dex");
                    if (!file.exists() && file.createNewFile()) {
                        file.setReadable(true, false);
                        IO.write(data, file);
                    }
                }
            });
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        if (DUMP_PATH.exists()) return;
        DUMP_PATH.mkdirs();
        DUMP_PATH.setReadable(true, false);
        DUMP_PATH.setWritable(true, false);
        DUMP_PATH.setExecutable(true, false);
    }

    static class IO implements Runnable {

        private final byte[] data;
        private final File file;

        private IO(byte[] data, File file) {
            this.data = data;
            this.file = file;
        }

        @Override
        public void run() {
            Log.i(TAG, "dump dex => " + file);
            try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
                output.write(data);
            } catch (Exception e) {
                Log.e(TAG, "Dump Dex Exception", e);
            }
        }

        static void write(final byte[] data, final File file) {
            new Thread(new IO(data, file)).start();
        }
    }
}
