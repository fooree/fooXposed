package foo.ree.demos.x1st;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.app.Application;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
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

public class FooxMain implements IXposedHookLoadPackage {

    public static final String TAG = FooxMain.class.getSimpleName();

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        Log.i(TAG, "inject into process: " + Process.myPid() + ", package: " + lpparam.packageName);

        XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Application context = AndroidAppHelper.currentApplication();
                String classname = param.thisObject.getClass().getName();
                String text = lpparam.packageName + "\n" + classname;
                Log.i(TAG, text);
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
