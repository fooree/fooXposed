package foo.ree.demos.x4th02;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.hello_dex);
    }

    public void decryptText(View view) {
        String name = "Foox_4th_01.apk";
        File file = new File(getFilesDir(), name);
        if (copyAssetFile(name, file)) {
            String dexPath = file.getPath();
            String optimizedDirectory = file.getParent();
            ClassLoader parent = getClass().getClassLoader();
            DexClassLoader classLoader = new DexClassLoader(dexPath, optimizedDirectory, null, parent);
            try {
                Class<?> clazz = classLoader.loadClass("foo.ree.demos.x4th01.Base64Util");
                Method method = clazz.getDeclaredMethod("decrypt", String.class);
                String text = (String) method.invoke(clazz, textView.getText().toString());

                if (!TextUtils.isEmpty(text)) {
                    textView.setText(text);
                    textView.refreshDrawableState();
                    view.setClickable(false);
                    Toast.makeText(this, "解密成功", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(this, "解密失败", Toast.LENGTH_SHORT).show();
    }

    private boolean copyAssetFile(String name, File file) {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = getAssets().open(name);
            output = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buf = new byte[4096];
            while (true) {
                int len = input.read(buf);
                if (len == -1) {
                    break;
                }
                output.write(buf, 0, len);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(output);
            close(input);
        }
        return false;
    }

    private void close(Closeable closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
