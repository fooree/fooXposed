package foo.ree.demos.dumpdex;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        displaySelectedApplication();
    }

    public void onClick(View view) {
        startActivityForResult(new Intent(this, ApplicationListActivity.class), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        displaySelectedApplication();
    }

    private void displaySelectedApplication() {
        String packageName = shares().getString(Constants.SELECT_APPLICATION, null);

        if (TextUtils.isEmpty(packageName)) return;

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.app);
        PackageManager pm = getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            ((ImageView) layout.findViewById(R.id.app_icon)).setImageDrawable(info.applicationInfo.loadIcon(pm));
            ((TextView) layout.findViewById(R.id.app_name)).setText(info.applicationInfo.loadLabel(pm));
            ((TextView) layout.findViewById(R.id.package_name)).setText(info.packageName);
            ((TextView) layout.findViewById(R.id.version_name)).setText(info.versionName);
            ((TextView) layout.findViewById(R.id.version_code)).setText(String.valueOf(info.versionCode));
            layout.setVisibility(View.VISIBLE);
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "请选择应用", Toast.LENGTH_SHORT).show();
            editor().remove(Constants.SELECT_APPLICATION).apply();
        }
    }

    private SharedPreferences.Editor editor() {
        return shares().edit();
    }

    private SharedPreferences shares() {
        return getSharedPreferences(Constants.HOOK_CONFIG, MODE_PRIVATE);
    }

    @SuppressLint("SetWorldReadable")
    @Override
    protected void onPause() {
        super.onPause();
        File dir = new File(getFilesDir().getParentFile(), "shared_prefs");
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
        if (files == null|| files.length == 0) return;
        for (File file : files) {
            //noinspection ResultOfMethodCallIgnored
            file.setReadable(true, false);
        }
    }
}
