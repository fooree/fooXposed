package foo.ree.demos.x05th;

import java.util.Arrays;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by fooree on 2018/3/4.
 */

public class FooxMain implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(LoadPackageParam lpp) throws Throwable {
        if (!"foo.ree.demos.x4th02".equals(lpp.packageName)) return;

        // 第一步：Hook方法ClassLoader#loadClass(String)
        findAndHookMethod(ClassLoader.class, "loadClass", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.hasThrowable()) return;
                Class<?> cls = (Class<?>) param.getResult();
                String name = cls.getName();
                if ("foo.ree.demos.x4th01.Base64Util".equals(name)) {
                    // 所有的类都是通过loadClass方法加载的
                    // 所以这里通过判断全限定类名，查找到目标类
                    // 第二步：Hook目标方法
                    findAndHookMethod(cls, "decrypt", String.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(param.method + " params: " + Arrays.toString(param.args));
                        }
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(param.method + " return: " + param.getResult());
                        }
                    });
                }
            }
        });
    }
}
