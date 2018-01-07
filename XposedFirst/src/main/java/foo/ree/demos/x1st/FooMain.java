package foo.ree.demos.x1st;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.app.Application;
import android.os.Bundle;
import android.widget.Toast;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Toast shows the activity when onCreate method is invoked.
 * <p>
 * Created by fooree on 2018/1/7.
 */

public class FooMain implements IXposedHookLoadPackage {

    public static final String TAG = FooMain.class.getSimpleName();

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (!"com.netease.newsreader.activity".equals(lpparam.packageName)) {
            return;
        }

        XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Application context = AndroidAppHelper.currentApplication();
                String classname = param.thisObject.getClass().getName();
                Toast.makeText(context, classname, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
