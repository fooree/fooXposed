package foo.ree.demos.easyxp;

import android.app.Activity;
import android.os.Bundle;

import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * 演示{@link EasyHook}的用法。
 * 相对于来说更佳简单：https://github.com/fooree/fooXposed/blob/master/Foox_05th/src/main/java/foo/ree/demos/x05th/FooxMain.java}
 *
 * Created by fooree on 2018/4/6.
 */

public class EasyUseDemo extends EasyHook {
    @Override
    public String packageName() {
        return "foo.ree.demos.x4th02";
    }

    @Override
    public void onLoadPackage(XC_LoadPackage.LoadPackageParam lpp) {
        hook("foo.ree.demos.x4th01.Base64Util", new OnLoad() {
            @Override
            public void executeHook(Class<?> cls) {
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
        });
        findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log(param.method + " this: " + param.thisObject);
            }
        });
    }
}
