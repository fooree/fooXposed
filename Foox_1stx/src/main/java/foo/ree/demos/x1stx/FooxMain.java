package foo.ree.demos.x1stx;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.app.Application;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Toast shows the activity wheXposedn onCreate method is invoked.
 * <p>android.app.Activity#onCreate(android.os.Bundle)
 * Created by fooree on 2018/1/7.
 */

public class FooxMain implements IXposedHookLoadPackage {

    private static final String TAG = FooxMain.class.getSimpleName();
    private final XSharedPreferences shares = new XSharedPreferences(Constants.THIS_MODULE, Constants.HOOK_CONFIG);

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        shares.reload();
        String pkg = shares.getString(Constants.SELECT_APPLICATION, null);

        if (lpparam.packageName.equals(pkg)) {
            Log.i(TAG, "inject into process: " + Process.myPid() + ", package: " + lpparam.packageName);
            findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
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
}
