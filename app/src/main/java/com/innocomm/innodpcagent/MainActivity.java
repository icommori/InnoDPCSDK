package com.innocomm.innodpcagent;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.innocomm.innodpcagent.databinding.ActMainBinding;
import com.innocomm.innoservice.IInnoBugReportCallback;
import com.innocomm.innoservice.IInnoFileReadWriteCallback;
import com.innocomm.innoservice.InnoManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActMainBinding binding;
    private String versionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            e.printStackTrace();
        }
        //test
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        boolean result = devicePolicyManager.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_DEVICE);
        Log.v(TAG, "isProvisioningAllowed " + result);
        Log.v(TAG, "airplane_mode_radios " +Application.getInstance().mInnoManager.settings_getSettingsGlobal("airplane_mode_radios",InnoManager.SETTING_TYPE_STRING));
        Log.v(TAG, "screen_off_timeout " +Application.getInstance().mInnoManager.settings_getSettingsSystem("screen_off_timeout",InnoManager.SETTING_TYPE_LONG));
        Log.v(TAG, "android_id " +Application.getInstance().mInnoManager.settings_getSettingsSecure("android_id",InnoManager.SETTING_TYPE_STRING));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Application.getInstance().mInnoManager.bugreport_unregistercallback(cb3);
    }

    @Override
    protected void onPause() {
        super.onPause();
        autotest = false;
        updateAutoTestButton();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateUIStatus();
    }

    private void updateUIStatus() {
        if (Application.getInstance().mInnoManager.dpc_isAdmin()) {
            binding.enterAdminmode.setText(R.string.exit_dpc);
            binding.dpcLoginGroup.setVisibility(View.GONE);
            binding.dpcTabsGroup.setVisibility(View.VISIBLE);
            initTabUI();
        } else {
            binding.enterAdminmode.setText(R.string.enter_dpc);
            binding.dpcLoginGroup.setVisibility(View.VISIBLE);
            binding.dpcTabsGroup.setVisibility(View.GONE);
        }

        invalidateOptionsMenu();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult " + requestCode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (!Application.getInstance().mInnoManager.dpc_isAdmin()) {
            menu.removeItem(R.id.action_exit);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_help) {
            showAbout();
            return true;
        } else if (id == R.id.action_exit) {
            showExitAdminModeConfirm();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void HandleEnterAdminMode(View view) {

        if (Application.getInstance().mInnoManager.dpc_isAdmin()) {
            showExitAdminModeConfirm();
        } else {
            showEnterDPCConfirm();
        }
    }

    private void showExitAdminModeConfirm() {
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle(R.string.exit_dpc_title)
                .setMessage(R.string.exit_dpc_msg)
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Application.getInstance().mInnoManager.dpc_unsetDeviceOwner(false);
                        Application.getInstance().mInnoManager.dpc_setDPCComponentEnable(false);
                        finish();
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNeutralButton(getString(R.string.wipe), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Application.getInstance().mInnoManager.dpc_unsetDeviceOwner(true);
                        finish();
                    }
                })
                .show();
    }

    private void showEnterDPCConfirm() {
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle(R.string.enter_dpc_title)
                .setMessage(R.string.enter_dpc_msg)
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (Application.getInstance().mInnoManager.dpc_setDPCComponentEnable(true)) {
                            if (Application.getInstance().mInnoManager.dpc_setDeviceOwner()) {
                                //init support message
                                Application.getInstance().mInnoManager.dpc_setShortSupportMessage(getString(R.string.short_support_msg));
                                Application.getInstance().mInnoManager.dpc_setLongSupportMessage(getString(R.string.long_support_msg));
                                updateUIStatus();
                            } else {
                                Toast.makeText(MainActivity.this, R.string.set_dpc_fail, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    private void showAbout() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View v = inflater.inflate(R.layout.about, null);
        TextView txt_app_ver = (TextView) v.findViewById(R.id.txt_app_ver);
        TextView txt_service_ver = (TextView) v.findViewById(R.id.txt_service_ver);

        txt_app_ver.setText(versionName);

        new AlertDialog.Builder(this)
                //.setTitle("??????????????????????????????id")
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    private TabLayout tabLayout;
    private myFragmentPagerAdapter tabsAdapter;

    private void initTabUI() {
        if (tabLayout != null) return;
        Log.v(TAG, "initTabUI");
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setTag("specific").setText(getString(R.string.specific_restrictions)));
        tabLayout.addTab(tabLayout.newTab().setTag("user").setText(getString(R.string.user_restrictions)));
        tabLayout.addTab(tabLayout.newTab().setTag("proprietaryapi").setText(getString(R.string.proprietary_api)));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabsAdapter = new myFragmentPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(tabsAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                Log.v(TAG, "onTabSelected " + tab.getTag());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    //String Test
    int count = 0;
    public static final String PATTERN = "0123456789ABCEDF";

    public void HandleTest0(View view) {
        Application.getInstance().mInnoManager.inputmethod_transferStringData(PATTERN + count, KeyEvent.KEYCODE_ENTER, 0);
        count++;
    }

    public boolean autotest = false;
    public List<Thread> mThreads = new ArrayList<>();
    public long lastUpdateTime;
    public int lastUpdateCount;
    public int failCompare = 0;
    public static final int FPS_PRINT_INTERVAL = 5;
    public List<String> mSendStrQueue = new ArrayList<String>();
    private Button mAutoTestButton = null;

    private void updateAutoTestButton() {
        if (mAutoTestButton != null) {
            if (autotest) {
                mAutoTestButton.setText("Stop");
            } else {
                mAutoTestButton.setText("AutoTest");
                mAutoTestButton = null;
            }
        }
    }

    public void HandleTestAutoTest(View view) {
        autotest = !autotest;
        mAutoTestButton = (Button) view;
        proprietaryapifragment = (ProprietaryAPIFragment) tabsAdapter.getFragment(2);
        if (proprietaryapifragment != null) {
            Log.v(TAG, "HandleTestAutoTest: clear text!");
            proprietaryapifragment.editText.setText("");
            if (autotest) proprietaryapifragment.inputlog.setText("Testing");
        }
        updateAutoTestButton();
        lastUpdateTime = Calendar.getInstance().getTimeInMillis();
        lastUpdateCount = 0;
        failCompare = 0;
        mSendStrQueue.clear();
        if (autotest) {

            for (int i = 0; i < 1; i++) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (autotest) {

                            if (mSendStrQueue.size() == 0) {
                                synchronized (mThreads) {
                                    String text = PATTERN + count;
                                    mSendStrQueue.add(text);

                                    Application.getInstance().mInnoManager.inputmethod_transferStringData(text, 505, 0);
                                    count++;
                                }
                            } else {
                                try {
                                    Random r = new Random();
                                    int i1 = r.nextInt(50) + 10;
                                    Thread.sleep(i1);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                continue;
                            }


                        }
                    }
                });
                thread.start();
                mThreads.add(thread);
            }

        } else {
            if (!mThreads.isEmpty()) {
                for (Thread thread : mThreads) {
                    thread.interrupt();
                }
                mThreads.clear();
            }
        }
    }

//~

    public void HandlePropertySet(View view) {
        proprietaryapifragment = (ProprietaryAPIFragment) tabsAdapter.getFragment(2);

        if (proprietaryapifragment != null) {
            String key = proprietaryapifragment.prop_key.getText().toString().trim();
            String val = proprietaryapifragment.prop_val.getText().toString().trim();
            Application.getInstance().mInnoManager.property_set(key, val);
        }
    }

    public void HandlePropertyGet(View view) {
        proprietaryapifragment = (ProprietaryAPIFragment) tabsAdapter.getFragment(2);

        if (proprietaryapifragment != null) {
            String key = proprietaryapifragment.prop_key.getText().toString().trim();
            String result = Application.getInstance().mInnoManager.property_get(key, null);
            proprietaryapifragment.prop_val.setText(result);
            Log.v(TAG, "HandlePropertyGet: " + key + " " + result);
        }
    }

    public void HandleUSBNONE(View view) {
        Application.getInstance().mInnoManager.usb_setConnectionType(InnoManager.USB_FUNCTION_NONE);
        ProprietaryAPIFragment proprietaryapifragment = (ProprietaryAPIFragment) tabsAdapter.getFragment(2);
        if (proprietaryapifragment != null) {
            proprietaryapifragment.UpdateUSBState(false);
        }

    }

    public void HandleUSBMTP(View view) {
        Application.getInstance().mInnoManager.usb_setConnectionType(InnoManager.USB_FUNCTION_MTP);
        proprietaryapifragment = (ProprietaryAPIFragment) tabsAdapter.getFragment(2);

        if (proprietaryapifragment != null) {
            proprietaryapifragment.UpdateUSBState(false);
        }
    }

    public void HandleListVolumes(View view) {
        String[] volumes = new String[10];
        Application.getInstance().mInnoManager.utils_getVolumePaths(volumes);
        List<String> mountedvolumes = new ArrayList<>();
        for (String s : volumes) {
            if (!TextUtils.isEmpty(s)) {
                mountedvolumes.add(s);
                Log.v(TAG, "storage: " + s);
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.mountedvolumes_title)
                .setItems((String[]) mountedvolumes.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public void HandleSetDisplayDensity(View view) {
        List<String> densityList = new ArrayList<>();
        densityList.add("RESET");
        densityList.add("160");
        densityList.add("240");
        densityList.add("320");

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.display_density_title)
                .setItems((String[]) densityList.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int density = 0;
                        if(which>0){
                            density = Integer.valueOf(densityList.get(which));
                        }
                        Application.getInstance().mInnoManager.settings_setDisplayDensity(density);
                    }
                })
                .show();

    }

    public void HandleSetFontSize(View view) {
        List<String> fontScaleList = new ArrayList<>();
        fontScaleList.add("0.85");
        fontScaleList.add("1.0");
        fontScaleList.add("1.15");
        fontScaleList.add("1.30");
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.font_scale_title)
                .setItems((String[]) fontScaleList.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        float fontScale = Float.valueOf(fontScaleList.get(which));

                        Application.getInstance().mInnoManager.settings_setSettingsSystem(Settings.System.FONT_SCALE,String.valueOf(fontScale),InnoManager.SETTING_TYPE_FLOAT);
                    }
                })
                .show();
    }

    public void HandleClearRecents(View view) {
        Application.getInstance().mInnoManager.utils_clearRecentTasks();
    }

    public void HandleScreenCapture(View view) {
        DateFormat df = new android.text.format.DateFormat();
        String date = (String) df.format("yyyy_MM_dd_hh_mm_ss.jpg", new java.util.Date());

        String path = "/sdcard/Pictures/"+date;
        Application.getInstance().mInnoManager.utils_screenCapture(new IInnoFileReadWriteCallback.Stub() {
            @Override
            public int read(byte[] bytes) throws RemoteException {
                return 0;
            }

            @Override
            public void write(byte[] bytes, int i, int i1) throws RemoteException {
                Log.v(TAG, "utils_screenCapture offset:" + i+", size: "+i1);
                if(bytes!=null) Log.v(TAG, "bytes.length:" +bytes.length);
                ByteArrayInputStream baos = new ByteArrayInputStream(bytes);
                binding.tabLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        showImage(baos);
                    }
                });

            }

            @Override
            public void close() throws RemoteException {

            }
        });
    }

    private void showImage(ByteArrayInputStream inputStream) {
        ImageView img = new ImageView(this);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        img.setImageBitmap(bitmap);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.capture_screen_title)
                .setView(img)
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    public void HandleUninstallApp(View view) {
        String[] list = getInstallApps();
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setItems(list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pkgName= list[which];

                        if(Application.getInstance().mInnoManager.utils_unInstallAPK(pkgName)){
                            Toast.makeText(MainActivity.this, "Succeeded!", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    public void HandleEnableApp(View view) {
        String[] list = getInstallApps();
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setItems(list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pkgName= list[which];

                        if(Application.getInstance().mInnoManager.utils_setApplicationEnabledSetting(pkgName,true)){
                            Toast.makeText(MainActivity.this, "Succeeded!", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }
    public void HandleDisableApp(View view) {
        String[] list = getInstallApps();
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setItems(list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pkgName= list[which];

                        if(Application.getInstance().mInnoManager.utils_setApplicationEnabledSetting(pkgName,false)){
                            Toast.makeText(MainActivity.this, "Succeeded!", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    //BugReport
    private static final class myIInnoBugReportCallback extends IInnoBugReportCallback.Stub {
        private WeakReference<MainActivity> mActivity;

        public myIInnoBugReportCallback(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void onProgress(final int i) throws RemoteException {
            Log.v(TAG, "onProgress " + i);
            final MainActivity activity = mActivity.get();
            if (activity != null) {

                activity.proprietaryapifragment.btn_bugreport.post(new Runnable() {
                    @Override
                    public void run() {
                        activity.proprietaryapifragment.bugreport_state.setText(Application.getInstance().getString(R.string.requestbugreport) + " " + i + "%");
                    }
                });
            }

        }

        @Override
        public void onComplete(final String s) throws RemoteException {
            Log.v(TAG, "onComplete: " + s);
            final MainActivity activity = mActivity.get();
            if (activity != null) {

                activity.proprietaryapifragment.btn_bugreport.post(new Runnable() {
                    @Override
                    public void run() {
                        activity.proprietaryapifragment.btn_bugreport.setEnabled(true);
                        activity.proprietaryapifragment.bugreport_state.setText(Application.getInstance().getString(R.string.requestbugreport) + " " + s);
                    }
                });
            }

        }
    }

    private myIInnoBugReportCallback cb3 = new myIInnoBugReportCallback(this);
    private ProprietaryAPIFragment proprietaryapifragment;

    public void HandleRequestBugreport(View view) {
        proprietaryapifragment = (ProprietaryAPIFragment) tabsAdapter.getFragment(2);
        if (proprietaryapifragment == null) return;


        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.requestbugreport_title)
                .setMessage(R.string.requestbugreport_msg)
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        proprietaryapifragment.btn_bugreport.setEnabled(false);
                        Application.getInstance().mInnoManager.bugreport_request(cb3);
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    public void HandleListBugreport(View view) {
        proprietaryapifragment = (ProprietaryAPIFragment) tabsAdapter.getFragment(2);
        if (proprietaryapifragment == null) return;

        String strings = Application.getInstance().mInnoManager.bugreport_listfile();

        if (strings != null) {
            final String[] files = strings.split(" ");
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.requestbugreport_list_title)
                    .setItems(files, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String latestFileName = files[which];
                            Log.v(TAG, "onClick " + which + ":" + latestFileName);

                            new AsyncTask<Void, Void, String>() {
                                @Override
                                protected String doInBackground(Void... params) {

                                    String message = "Save error!";
                                    FileOutputStream fis = null;
                                    String accessname = Application.getInstance().mInnoManager.bugreport_openfile(latestFileName);
                                    if (accessname == null) {
                                        Log.e(TAG, "Open " + latestFileName + " failed!");
                                        return message;
                                    }
                                    try {
                                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), accessname);
                                        String saveName = file.getAbsolutePath();
                                        Log.e(TAG, "Save to " + saveName);
                                        fis = new FileOutputStream(saveName);

                                        int content;
                                        byte[] data = new byte[1024];

                                        while ((content = Application.getInstance().mInnoManager.bugreport_readfile(latestFileName, data)) != -1) {
                                            fis.write(data, 0, content);
                                        }
                                        message = "File saved to " + saveName;
                                    } catch (Exception e) {
                                        Log.e(TAG, "IOException: " + e.toString());
                                    } finally {
                                        if (accessname != null)
                                            Application.getInstance().mInnoManager.bugreport_closefile(accessname);
                                        try {
                                            if (fis != null)
                                                fis.close();
                                        } catch (Exception ex) {
                                            Log.e(TAG, "Exception: " + ex.toString());
                                        }
                                    }

                                    return message;
                                }

                                @Override
                                protected void onPostExecute(String message) {
                                    proprietaryapifragment.bugreport_state.setText(Application.getInstance().getString(R.string.requestbugreport) + " " + message);
                                }

                            }.execute();


                        }
                    })
                    .show();
        }
    }

    public void HandleSetNavNar(View view) {
        String[] mode = {"NAV_BAR_MODE_3BUTTON_OVERLAY", "NAV_BAR_MODE_2BUTTON_OVERLAY", "NAV_BAR_MODE_GESTURAL_OVERLAY"};
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setItems(mode, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 1) {
                            Toast.makeText(MainActivity.this, "Not support!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Log.v(TAG, "setNavigationBarMode: " + Application.getInstance().mInnoManager.settings_setNavigationBarMode(which));
                    }
                })
                .show();
    }

    public void HandleSetHome(View view) {
        ComponentName[] list = getHomeActivitiesList(this);

        String[] homelist = new String[list.length];
        for (int i = 0; i < homelist.length; i++) {
            ComponentName n = list[i];
            Log.v(TAG, "ComponentName " + n.getPackageName() + "/" + n.getClassName());
            homelist[i] = n.getPackageName() + "/" + n.getClassName();
        }
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setItems(homelist, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ComponentName n = list[which];
                        Application.getInstance().mInnoManager.settings_setDefaultHomeLauncher(n.getPackageName(), n.getClassName());
                    }
                })
                .show();

    }

    private static ComponentName[] getHomeActivitiesList(Context context) {
        Intent queryIntent = new Intent("android.intent.action.MAIN");
        queryIntent.addCategory("android.intent.category.HOME");
        queryIntent.addCategory("android.intent.category.LAUNCHER_APP");
        List<ResolveInfo> resInfos = context.getPackageManager().queryIntentActivities(queryIntent, PackageManager.MATCH_DEFAULT_ONLY);
        ComponentName[] componentNames = new ComponentName[resInfos.size()];
        for (int i = 0; i < resInfos.size(); i++) {
            ActivityInfo activityInfo = resInfos.get(i).activityInfo;
            componentNames[i] = new ComponentName(activityInfo.packageName, activityInfo.name);
        }
        return componentNames;
    }

    private String[] getPermissionapp(String permission){
        List<String> lists = new ArrayList<>();
        final PackageManager pm = getPackageManager();
        int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        // Loop each package requesting <manifest> permissions
        for (final PackageInfo pi : pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)) {
            final String[] permissions = pi.requestedPermissions;
            String pkgName = pi.packageName;
            //Log.v(TAG, "Checking: " + pkgName);
            if (permissions == null) {
                // No permissions defined in <manifest>
                continue;
            }
            try {
                ApplicationInfo ai = pm.getApplicationInfo(pi.packageName, 0);
                if ((ai.flags & mask) != 0) continue;//skip system apps
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            // Loop each <uses-permission> tag to retrieve the permission flag
            for (int i = 0, len = permissions.length; i < len; i++) {
                final String requestedPerm = permissions[i];
                if(requestedPerm.equals(permission)){
                    Log.v(TAG, "Has "+permission+": " + pkgName);
                    lists.add(pkgName);
                    break;
                }
            }
        }

        return  lists.toArray(new String[lists.size()]);
    }
    private String[] getInstallApps(){
        List<String> lists = new ArrayList<>();
        final PackageManager pm = getPackageManager();
        int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        for (final PackageInfo pi : pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)) {
            try {
                ApplicationInfo ai = pm.getApplicationInfo(pi.packageName, 0);
                if ((ai.flags & mask) != 0) continue;//skip system apps
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            final String[] permissions = pi.requestedPermissions;
            String pkgName = pi.packageName;
            //Log.v(TAG, "Checking: " + pkgName);
            if (permissions == null) {
                // No permissions defined in <manifest>
                continue;
            }
            lists.add(pkgName);
        }

        return  lists.toArray(new String[lists.size()]);
    }

    public void HandleAllowInstallApps(View view) {
        String[] list = getPermissionapp("android.permission.REQUEST_INSTALL_PACKAGES");

        new MaterialAlertDialogBuilder(MainActivity.this)
                .setItems(list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pkgName= list[which];
                        if(Application.getInstance().mInnoManager.settings_setAdvanedPermission(pkgName,InnoManager.ADV_PERMISSION_OP_REQUEST_INSTALL_PACKAGES,true)){
                            Toast.makeText(MainActivity.this, "Succeeded!", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    public void HandleAllowDisplayOverOtehrApps(View view) {
        String[] list = getPermissionapp("android.permission.SYSTEM_ALERT_WINDOW");

        new MaterialAlertDialogBuilder(MainActivity.this)
                .setItems(list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pkgName= list[which];
                        if(Application.getInstance().mInnoManager.settings_setAdvanedPermission(pkgName, InnoManager.ADV_PERMISSION_OP_SYSTEM_ALERT_WINDOW,true)){
                            Toast.makeText(MainActivity.this, "Succeeded!", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }
    public void HandleAllowWriteSettings(View view) {
        String[] list = getPermissionapp("android.permission.WRITE_SETTINGS");

        new MaterialAlertDialogBuilder(MainActivity.this)
                .setItems(list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pkgName= list[which];
                        if(Application.getInstance().mInnoManager.settings_setAdvanedPermission(pkgName, InnoManager.ADV_PERMISSION_OP_WRITE_SETTINGS,true)){
                            Toast.makeText(MainActivity.this, "Succeeded!", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    private void processHandleGrantRuntimePermissions(String pkgName){
        String[] list = getAppPermissions(pkgName);
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setItems(list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String permission= list[which];
                        if(Application.getInstance().mInnoManager.dpc_setPermissionGrantState(pkgName,permission,DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED)){
                            Toast.makeText(MainActivity.this, "Succeeded!", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    public void HandleGrantRuntimePermissions(View view) {
        String[] list = getInstallApps();
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setItems(list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pkgName= list[which];
                        processHandleGrantRuntimePermissions(pkgName);
                    }
                })
                .show();
    }
    private String[] getAppPermissions(String packageName){
        List<String> lists = new ArrayList<>();
        final PackageManager pm = getPackageManager();

        Method protectionToString;
        try {
            protectionToString = PermissionInfo.class.getDeclaredMethod("protectionToString", int.class);
            if (!protectionToString.isAccessible()) protectionToString.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName,PackageManager.GET_PERMISSIONS);
            String appName = packageInfo.applicationInfo.loadLabel(pm).toString();

             for (String permission : packageInfo.requestedPermissions) {
                if(!permission.startsWith("android.permission")) continue;
                PermissionInfo permissionInfo;
                try {
                    permissionInfo = pm.getPermissionInfo(permission, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.i(TAG, String.format("unknown permission '%s' found in '%s'", permission, packageName));
                    continue;
                }

                if((permissionInfo.getProtection() != PermissionInfo.PROTECTION_DANGEROUS)) continue;
                // convert the protectionLevel to a string (not necessary, but useful info)
                String protLevel;
                try {
                    protLevel = (String) protectionToString.invoke(null, permissionInfo.getProtection());
                } catch (Exception ignored) {
                    protLevel = "????";
                }

                // Create the package's context to check if the package has the requested permission
                Context packageContext;
                try {
                    packageContext = createPackageContext(packageName, 0);
                } catch (PackageManager.NameNotFoundException wtf) {
                    continue;
                }

                int ret = packageContext.checkCallingPermission(permission);
                if (ret == PackageManager.PERMISSION_DENIED) {
                    Log.i(TAG, String.format("%s [%s] is denied permission %s (%s)",
                            appName, packageName, permission, protLevel));

                    lists.add(permission);
                } else {
                    //Log.i(TAG, String.format("%s [%s] has granted permission %s (%s)",   appName, packageName, permission, protLevel));
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return  lists.toArray(new String[lists.size()]);
    }

}