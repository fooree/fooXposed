package foo.ree.demos.x4th01;

import android.text.TextUtils;
import android.util.Base64;

import java.nio.charset.Charset;

/**
 * Created by fooree on 2018/2/18.
 */
public class Base64Util {
    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static String encrypt(String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        } else {
            return new String(Base64.encode(text.getBytes(UTF8), Base64.NO_WRAP), UTF8);
        }
    }

    public static String decrypt(String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        } else {
            return new String(Base64.decode(text.getBytes(UTF8), Base64.DEFAULT),UTF8);
        }
    }
}
