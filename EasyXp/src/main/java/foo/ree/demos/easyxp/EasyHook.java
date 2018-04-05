package foo.ree.demos.easyxp;

import android.os.Process;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * 继承{@link EasyHook}开发Xposed插件，可以使您不再担心ClassLoader的问题。
 *
 * Created by fooree on 2018/4/5.
 */

public abstract class EasyHook implements IXposedHookLoadPackage {

    protected String TAG = EasyHook.class.getSimpleName();

    private final Map<String, OnLoad> classes = new HashMap<>();

    /**
     * 应用包名
     */
    public abstract String packageName();

    /**
     * 代码都在这个方法中实现。
     * 用来替换{@link XC_LoadPackage.LoadPackageParam#handleLoadPackage(XC_LoadPackage.LoadPackageParam)}
     * 一般在该方法中实现的代码如下所示：
     * <pre>
     * <code>
     * void onLoadPackage() {
     *      ......
     *      hook("className", new OnLoad() {
     *          public void executeHook(Class<?> clazz) {
     *              Log.i(TAG, "Hooking " + clazz.getName());
     *              findAndHookMethod(clazz, "methodNameA", int.class, new XC_MethodHook());
     *              findAndHookMethod(clazz, "methodNameB", int.class, int.class, new XC_MethodHook());
     *          }
     *      });
     *      findAndHookMethod("className", lpp.classLoader, "methodNameA", int.class, new XC_MethodHook());
     *      ......
     * }
     *
     * </code>
     * </pre>
     */
    public abstract void onLoadPackage(XC_LoadPackage.LoadPackageParam lpp);

    /**
     * 注册hook代码。
     * 同一个类不能重复注册。
     * 在{@link #onLoadPackage(XC_LoadPackage.LoadPackageParam)}中调用该方法。
     *
     * @param className - 类名
     * @param onLoad    - 当类加载时需要对类执行的操作
     */
    public final void hook(String className, OnLoad onLoad) {
        if (className == null || className.isEmpty() || onLoad == null) {
            return;
        }
        if (classes.containsKey(className)) {
            Log.e(TAG, "Multiple registration class: " + className);
        } else {
            classes.put(className, onLoad);
        }
    }

    @Override
    public final void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpp) throws Throwable {
        if (lpp.packageName.equals(packageName())) {
            TAG = getClass().getSimpleName();
            Log.i(TAG, "Inject code into application " + lpp.packageName);
            try {
                onLoadPackage(lpp);
            } catch (Throwable e) {
                Log.i(TAG, e.getLocalizedMessage(), e);
            }

            hookLoadClass();
        }
    }

    private void hookLoadClass() {
        findAndHookMethod(ClassLoader.class, "loadClass", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.hasThrowable()) {
                    return;
                }
                Class<?> clazz = (Class<?>) param.getResult();
                if (clazz.isInterface() || clazz.isAnnotation() || clazz.isEnum() || classes.isEmpty()) {
                    return;
                }
                try {
                    String name = (String) param.args[0];
                    if (classes.containsKey(name)) {
                        Log.i(TAG, "Process: " + Process.myPid() + ", loadClass:" + name);
                        OnLoad onLoad = classes.get(name);
                        if (onLoad == null) {
                            return;
                        }
                        onLoad.executeHook(clazz);
                    }
                } catch (Throwable e) {
                    Log.i(TAG, e.getLocalizedMessage(), e);
                }
            }
        });
    }
}
