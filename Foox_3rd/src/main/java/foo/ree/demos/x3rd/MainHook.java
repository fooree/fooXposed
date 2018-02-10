package foo.ree.demos.x3rd;

import android.util.Log;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Hook method {@link java.lang.Runtime}#doLoad(String, ClassLoader)
 * Created by fooree on 2018/2/6.
 */
public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = MainHook.class.getSimpleName();

    @Override
    public void handleLoadPackage(LoadPackageParam lp) throws Throwable {
        if (!"com.leo.myworldstr".equals(lp.packageName)) return;

        findAndHookMethod(Runtime.class, "doLoad", String.class, ClassLoader.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String filename = (String) param.args[0];
                if (new File(filename).exists()) {
                    Log.i(TAG, "Loading so file " + filename);
                } else {
                    Log.i(TAG, "Loading so file " + filename + ", but the file does not exist");
                }

//                      param.args[0] = "/sdcard/my.so";  // 通过修改第0个参数，可以替换so文件
            }
        });
    }
}
