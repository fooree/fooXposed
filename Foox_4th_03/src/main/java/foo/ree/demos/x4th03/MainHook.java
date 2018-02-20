package foo.ree.demos.x4th03;

import android.util.Log;

import java.io.File;

import dalvik.system.BaseDexClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;

/**
 * Created by fooree on 2018/2/20.
 */
public class MainHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (!"foo.ree.demos.x4th02".equals(lpparam.packageName)) {
            return;
        }

        findAndHookConstructor(BaseDexClassLoader.class,
                String.class, File.class, String.class, ClassLoader.class,// 构造器参数列表
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String dexPath = (String) param.args[0];
                        Log.i(MainHook.class.getSimpleName(), "dexPath = " + dexPath);
                        // 此处已经获得dex文件路径，可以通过IO操作，将文件保存到理想的位置
                    }
                });
    }

}
