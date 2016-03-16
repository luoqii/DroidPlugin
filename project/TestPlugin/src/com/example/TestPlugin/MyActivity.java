package com.example.TestPlugin;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import com.morgoo.droidplugin.pm.PluginManager;
import com.morgoo.helper.Log;
import com.morgoo.helper.compat.ActivityManagerCompat;
import com.morgoo.helper.compat.PackageManagerCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MyActivity extends AppCompatActivity {


    private static final String TAG = "MyActivity";

    private AlertDialog mInstallingDia;

    private ViewPager mViewPager;
    private FragmentStatePagerAdapter mFragmentStatePagerAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new InstalledFragment();
            } else {
                return new ApkFragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "已安装";
            } else {
                return "待安装";
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mFragmentStatePagerAdapter);
//        getPerms();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getPerms() {
        final PackageManager pm = getPackageManager();
        final List<PackageInfo> pkgs = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        new Thread() {
            @Override
            public void run() {
                try {
                    StringBuilder sb = new StringBuilder();
                    Log.e(TAG, "===========包权限start===========");
                    Set<String> ps = new TreeSet<String>();
                    for (PackageInfo pkg : pkgs) {
                        if (pkg.permissions != null && pkg.permissions.length > 0) {
                            for (PermissionInfo permission : pkg.permissions) {
                                ps.add(permission.name);
                            }
                        }
                    }
                    for (String p : ps) {
                        PermissionInfo permission = pm.getPermissionInfo(p, 0);
                        PackageInfo pkg = pm.getPackageInfo(permission.packageName, 0);
                        String re = String.format("<uses-permission android:name=\"%s\"/>", permission.name);
                        String ms = String.format("%s,%s,%s,%s,%s,%s,%s,%s", permission.packageName, pkg.applicationInfo.loadLabel(pm), permission.name, permission.group, permission.protectionLevel, permission.loadLabel(pm), permission.loadDescription(pm), re);
                        sb.append(ms).append("\r\n");
                        Log.e(TAG, "packageName=%s, name=%s group=%s protectionLevel=%s", permission.packageName, permission.name, permission.group, permission.protectionLevel);
                    }

                    FileWriter w = null;
                    try {
                        w = new FileWriter(new File(Environment.getExternalStorageDirectory(), "per.txt"));
                        w.write(sb.toString());

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (w != null) {
                            try {
                                w.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                    Log.e(TAG, "===========包权限end===========");
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_install:
                Intent act = new Intent(this, AllLauncherActivity.class);
                startActivityForResult(act, 0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){

            ResolveInfo r = data.getParcelableExtra(AllLauncherActivity.ActFragment.EXTRA_RESOVLEINFO);
            String packageName = "";
            if (TextUtils.isEmpty(packageName)){
                packageName = r.activityInfo.applicationInfo.packageName;
            }
            packageName += " " + getPackageManager().getApplicationLabel(r.activityInfo.applicationInfo);
            mInstallingDia = new AlertDialog.Builder(this)
                    .setTitle("install plugin " + packageName)
                    .setCancelable(false)
                    .create();

            mInstallingDia.show();

            new AsyncTask<ResolveInfo, Boolean, Boolean>(){

                @Override
                protected Boolean doInBackground(ResolveInfo... params) {

                    try {
                        int res = PluginManager.getInstance().installPackage(params[0].activityInfo.applicationInfo.publicSourceDir, 0);
                        return (res == PackageManagerCompat.INSTALL_SUCCEEDED) ? true : false;
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    return false;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    super.onPostExecute(result);
                    if (result){
                        mViewPager.setCurrentItem(0);
                    }
                    mInstallingDia.dismiss();
                }
            }.execute(r);


        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
