package foo.ree.demos.x2nd;

import android.util.Log;

import java.net.URL;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MainHook implements IXposedHookLoadPackage {
    private static final String TAG = MainHook.class.getSimpleName();

    @Override
    public void handleLoadPackage(final LoadPackageParam lp) throws Throwable {
        final String appPkgName = lp.packageName;
        if (!"目标应用包名".equals(appPkgName)) return;   // "目标应用包名"需要替换为实际的包名

        /**
         * {@link java.net.URL#openConnection()}方法没有参数，所以没有列出参数类型
         */
        XposedHelpers.findAndHookMethod(URL.class, "openConnection", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                URL url = (URL) param.thisObject;
                Log.i(TAG, param.method + " in Application(" + appPkgName + ") request: " + url.toString());
            }
        });

        /**
         * (1)高版本Android系统默认不包含HttpClient，所以需要try...catch，避免后续代码不能执行
         * (2)参数类型列表为：HttpHost.class, HttpRequest.class, HttpContext.class
         * (3)调用{@link XposedHelpers#findClass(String, ClassLoader)}从类加载器中查找类
         * (4)工具方法，反射调用：{@link XposedHelpers#callMethod(java.lang.Object, java.lang.String, java.lang.Object...)}
         */
        try {
            ClassLoader classLoader = lp.classLoader;   // 当前进程的Java类加载器

            // 类加载器，期望从中查找到类AbstractHttpClient和HttpUriRequest
            // 如果找不到，HOOK失败
            // 如果找得到，HOOK成功
            final Class<?> httpClient = XposedHelpers.findClass(
                    "org.apache.http.impl.client.AbstractHttpClient", classLoader);
            final Class<?> httpUriRequest = XposedHelpers.findClass(
                    "org.apache.http.client.methods.HttpUriRequest", classLoader);

            XposedHelpers.findAndHookMethod(httpClient,
                    "execute",                              // 目标方法，AbstractHttpClient类中的方法
                    "org.apache.http.HttpHost",             // execute方法的第0个参数类型
                    "org.apache.http.HttpRequest",          // execute方法的第1个参数类型
                    "org.apache.http.protocol.HttpContext", // execute方法的第2个参数类型
                    new XC_MethodHook() {                   // 方法回调
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Object request = param.args[1];
                            if (request == null) return;

                            if (!httpUriRequest.isAssignableFrom(request.getClass())) {
                                return;
                            }
                            // 参考：org.apache.http.client.methods.HttpUriRequest
                            Object uri = XposedHelpers.callMethod(request, "getURI");
                            Log.i(TAG, param.method + " in Application(" + appPkgName + ") request: " + uri.toString());
                        }
                    });
        } catch (XposedHelpers.ClassNotFoundError e) {
            Log.e(TAG, "There is NO HttpClient", e);
        }
    }
}
