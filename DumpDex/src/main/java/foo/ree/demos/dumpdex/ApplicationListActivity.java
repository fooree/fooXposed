package foo.ree.demos.dumpdex;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.pm.ApplicationInfo.FLAG_SYSTEM;
import static android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;

public class ApplicationListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.application_list);
        setTitle(R.string.applications);

        List<PackageInfo> packages = getInstalledPackages();

        ListView lv = (ListView) findViewById(R.id.applications);
        lv.setAdapter(new PackageAdapter(this, packages));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PackageInfo item = (PackageInfo) parent.getAdapter().getItem(position);
                setResult(Constants.CODE_SELECT);
                getSharedPreferences(Constants.HOOK_CONFIG, MODE_PRIVATE)
                        .edit()
                        .putString(Constants.SELECT_APPLICATION, item.packageName)
                        .apply();
                finish();
            }
        });
    }

    private List<PackageInfo> getInstalledPackages() {
        List<PackageInfo> list = new ArrayList<>();

        List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
        for (PackageInfo info : packages) {
            if ((info.applicationInfo.flags & (FLAG_SYSTEM | FLAG_UPDATED_SYSTEM_APP)) == 0) {
                list.add(info);
            }
        }
        Collections.sort(list, new Comparator<PackageInfo>() {
            @Override
            public int compare(PackageInfo lhs, PackageInfo rhs) {
                return lhs.packageName.compareTo(rhs.packageName);
            }
        });
        return list;
    }

    private class PackageAdapter extends BaseAdapter {
        private final Context context;
        private final List<PackageInfo> packages;
        private final LayoutInflater inflater;

        PackageAdapter(Context context, List<PackageInfo> packages) {
            this.context = context;
            this.packages = packages;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return packages.size();
        }

        @Override
        public Object getItem(int position) {
            return packages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                view = inflater.inflate(R.layout.app, parent, false);

                App app = new App();
                app.icon = (ImageView) view.findViewById(R.id.app_icon);
                app.name = (TextView) view.findViewById(R.id.app_name);
                app.pkgName = (TextView) view.findViewById(R.id.package_name);
                app.versionName = (TextView) view.findViewById(R.id.version_name);
                app.versionCode = (TextView) view.findViewById(R.id.version_code);

                view.setTag(app);
            }

            PackageInfo info = packages.get(position);
            PackageManager pm = context.getPackageManager();

            App app = (App) view.getTag();
            app.icon.setImageDrawable(info.applicationInfo.loadIcon(pm));
            app.name.setText(info.applicationInfo.loadLabel(pm));
            app.pkgName.setText(info.packageName);
            app.versionName.setText(info.versionName);
            app.versionCode.setText(String.valueOf(info.versionCode));

            return view;
        }

        class App {
            ImageView icon;
            TextView name;
            TextView pkgName;
            TextView versionName;
            TextView versionCode;
        }
    }
}
