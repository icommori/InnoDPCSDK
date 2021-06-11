package com.innocomm.innodpcagent;

import android.app.admin.DevicePolicyManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.innocomm.innodpcagent.databinding.ActMainBinding;

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
        }
        //test
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        boolean result = devicePolicyManager.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_DEVICE);
        Log.v(TAG, "isProvisioningAllowed " + result);
        String s = (String) devicePolicyManager.getDeviceOwnerLockScreenInfo();
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
                //.setTitle("è«è¼¸å¥ä½ çid")
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    private TabLayout tabLayout;

    private void initTabUI() {
        if (tabLayout != null) return;
        Log.v(TAG, "initTabUI");
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setTag("specific").setText(getString(R.string.specific_restrictions)));
        tabLayout.addTab(tabLayout.newTab().setTag("user").setText(getString(R.string.user_restrictions)));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        myFragmentPagerAdapter tabsAdapter = new myFragmentPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(tabsAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                Log.v(TAG, "onTabSelected "+tab.getTag());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

}