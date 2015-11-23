package com.example.TestPlugin;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.morgoo.droidplugin.am.ComponentSelector;
import com.morgoo.droidplugin.pm.PluginManager;
import com.morgoo.helper.Log;
import com.morgoo.helper.compat.PackageManagerCompat;
import com.morgoo.helper.compat.ProcessCompat;
import com.morgoo.helper.compat.UserHandleCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MyActivity extends
//        ActionBar
        FragmentActivity {


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

//        init();

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        ProcessCompat.setArgV0("bysong.name");
//        DdmHandleAppNameCompat.setAppName("new ddms name", UserHandleCompat.myUserId());

//        setContentView(R.layout.main);


        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                while (!PluginManager.getInstance().isConnected()) {

                    try {
                        Log.d(TAG, "sleep 1000");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    Toast.makeText(MyActivity.this, "插件服务正在初始化，请稍后再试。。。", Toast.LENGTH_SHORT).show();
                }

                try {
                    List<PackageInfo> pgks = PluginManager.getInstance().getInstalledPackages(0);
                    if (pgks != null && pgks.size() > 0){
                        return null;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/youku_plugin");
                dir = getDir("youku_plugin", 0);
                dir.mkdirs();
                for (File f: dir.listFiles()) {
                    f.delete();
                }
                extractAssetFile(getAssets(), "extract2sdcard", dir);
                for (File f: dir.listFiles()) {
                    try {
                        Log.d(TAG, "install apk: " + f);
                        PluginManager.getInstance().installPackage(f.getPath(), PackageManagerCompat.INSTALL_REPLACE_EXISTING);


                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                try {
                    PackageManager pm = MyActivity.this.getPackageManager();
                    Intent intent = null;
                    intent = pm.getLaunchIntentForPackage(PluginManager.getInstance().getInstalledPackages(0).get(0).packageName);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
//                mViewPager = (ViewPager) findViewById(R.id.pager);
//                mViewPager.setAdapter(mFragmentStatePagerAdapter);
            }
        }.execute((Void[]) null);
//        getPerms();
    }

    private void init() {

        ComponentSelector.getInsance().setHook(new ComponentSelector.Hook() {
            @Override
            public ActivityInfo selectStubActivityInfo(ActivityInfo targetActivityInfo) {
                return null;
            }

            @Override
            public ServiceInfo selectStubServiceInfo(ServiceInfo targetActivityInfo) {
                try {
                    if ("com.youku.pushsdk.service.PushService".equals(targetActivityInfo.name)) {
                        return getPackageManager().getServiceInfo(new ComponentName(getPackageName(), "com.example.TestPlugin.stub.Stub$PushService"), 0);
                    }
                    if ("com.youku.service.acc.AcceleraterService".equals(targetActivityInfo.name)){
                        return getPackageManager().getServiceInfo(new ComponentName(getPackageName(), "com.example.TestPlugin.stub.Stub$AcceleraterService"), 0);
                    }
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public ProviderInfo selectStubProviderInfo(ProviderInfo targetActivityInfo) {
                return null;
            }

            @Override
            public String getProcessName(String stubProcessName) {
                if ("com.cibn.tv.launcher.debug".equals(stubProcessName)){
                    return "com.cibn.tv.bysong" + ":PluginP7";
                }
                return null;
            }
        });
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
            mInstallingDia = new AlertDialog.Builder(this)
                    .setTitle("install plugin " + r.activityInfo.applicationInfo.packageName)
                    .setCancelable(false)
                    .create();

            mInstallingDia.show();

            new AsyncTask<ResolveInfo, Void, Void>(){

                @Override
                protected Void doInBackground(ResolveInfo... params) {

                    try {
                        PluginManager.getInstance().installPackage(params[0].activityInfo.applicationInfo.publicSourceDir, 0);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    mInstallingDia.dismiss();
                }
            }.execute(r);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public static ArrayList<File> extractAssetFile(AssetManager am, String assetDir,
                                                   File destDir) {
        ArrayList<File> copiedFiles = new ArrayList<File>();
        try {
            destDir.mkdirs();
            String[] files = am.list(assetDir);
            if (null == files || files.length == 0){
                //==========123456789012345678
                android.util.Log.w(TAG, "empty assets dir:" + assetDir);
            } else {
                for (String fp : files) {
                    File destF = new File(destDir, fp);
                    destF.delete();
                    destF.createNewFile();
                    copyStream(am.open(assetDir + "/" + fp),
                            new FileOutputStream(destF));
                    copiedFiles.add(destF);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return copiedFiles;
    }

    public static void copyStream(InputStream in, OutputStream out){
        try {
            int byteCount = 1024 * 1024;
            byte[] buffer = new byte[byteCount];
            int count = 0;
            while ((count = in.read(buffer, 0, byteCount)) != -1){
                out.write(buffer, 0, count);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
