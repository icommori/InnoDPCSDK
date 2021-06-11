package com.innocomm.innodpcagent;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.innocomm.innoservice.IInnoBugReportCallback;
import com.innocomm.innoservice.IInnoClearAppUserDataCallback;
import com.innocomm.innoservice.IInnoInstallSystemUpdateCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static android.app.Activity.RESULT_OK;

public class SpecificDPCFragment extends Fragment {

    private static final String TAG = SpecificDPCFragment.class.getSimpleName();
    private static final int REQUEST_CODE_PICK_CA_FILE = 0;
    private static final int REQUEST_CODE_PICK_OTA_FILE = 1;
    private static final int REQUEST_CODE_CONFIRM_CREDENTIAL = 2;
    private CheckBox hideUnusedApps;
    private static CheckBox requestbugreport;
    private DevicePolicyManager devicePolicyManager;
    private KeyguardManager mKeyguardMgr;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        devicePolicyManager = getContext().getSystemService(DevicePolicyManager.class);
        mKeyguardMgr = getContext().getSystemService(KeyguardManager.class);
        loadCACert();
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy ");
        Application.getInstance().mInnoManager.bugreport_unregistercallback(cb3);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView ");
        return inflater.inflate(R.layout.dpc_specific, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "onViewCreated ");

        CheckBox disableStatusbar = view.findViewById(R.id.disable_statusbar);
        disableStatusbar.setOnCheckedChangeListener(null);
        disableStatusbar.setChecked(Application.getInstance().getPref(Application.KEY_DISABLE_STATUSBAR, false));
        disableStatusbar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v(TAG, "dpc_setStatusBarDisabled " + isChecked + ": " + Application.getInstance().mInnoManager.dpc_setStatusBarDisabled(isChecked));
                if (!isChecked) {//trick
                    Application.getInstance().mInnoManager.dpc_setStatusBarDisabled(true);
                    Application.getInstance().mInnoManager.dpc_setStatusBarDisabled(false);
                }
                Application.getInstance().setPref(Application.KEY_DISABLE_STATUSBAR, isChecked);
            }
        });

        CheckBox disallowDebuggingFeatures = view.findViewById(R.id.disallow_debugging_features);
        disallowDebuggingFeatures.setOnCheckedChangeListener(null);
        disallowDebuggingFeatures.setChecked(Application.getInstance().mInnoManager.dpc_getUserRestriction(UserManager.DISALLOW_DEBUGGING_FEATURES));
        disallowDebuggingFeatures.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Application.getInstance().mInnoManager.dpc_disallowADB(isChecked);
            }
        });

        hideUnusedApps = view.findViewById(R.id.hide_unused_apps);
        hideUnusedApps.setOnCheckedChangeListener(null);
        hideUnusedApps.setChecked(Application.getInstance().getPref(Application.KEY_HIDE_APPS, false));
        hideUnusedApps.setOnCheckedChangeListener(mHiddenCheckBox);

        CheckBox setscreencapturedisabled = view.findViewById(R.id.setscreencapturedisabled);
        setscreencapturedisabled.setOnCheckedChangeListener(null);
        setscreencapturedisabled.setChecked(Application.getInstance().mInnoManager.dpc_getScreenCaptureDisabled());
        setscreencapturedisabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Application.getInstance().mInnoManager.dpc_setScreenCaptureDisabled(isChecked);
            }
        });

        CheckBox clearapplicationuserdata = view.findViewById(R.id.clearapplicationuserdata);
        clearapplicationuserdata.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setChecked(false);
                showAppList();
            }
        });
        CheckBox installcacert = view.findViewById(R.id.installcacert);
        installcacert.setOnCheckedChangeListener(null);
        installcacert.setChecked(Application.getInstance().mInnoManager.dpc_hasCaCertInstalled(myCABytes));
        installcacert.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                /*buttonView.setChecked(false);
                pickCACert();*/
                boolean result = false;
                if(isChecked){
                    result = Application.getInstance().mInnoManager.dpc_installCaCert(myCABytes);
                }else{
                    if(Application.getInstance().mInnoManager.dpc_hasCaCertInstalled(myCABytes)){
                        result = Application.getInstance().mInnoManager.dpc_uninstallCaCert(myCABytes);
                    }
                }

                Log.v(TAG,"installcacert result: "+isChecked+" "+result);
            }
        });

        CheckBox getwifimacaddress = view.findViewById(R.id.getwifimacaddress);
        getwifimacaddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getwifimacaddress.setChecked(false);
                String mac = Application.getInstance().mInnoManager.dpc_getWifiMacAddress();
                showCommonConfirm(getString(R.string.getwifimacaddress_title), mac != null ? mac : "NULL");
            }
        });
        CheckBox setdeviceownerlockscreeninfo = view.findViewById(R.id.setdeviceownerlockscreeninfo);
        setdeviceownerlockscreeninfo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setdeviceownerlockscreeninfo.setChecked(false);
                Application.getInstance().mInnoManager.dpc_setDeviceOwnerLockScreenInfo(getString(R.string.setdeviceownerlockscreeninfo_msg));
                showScreenLockInfo();
            }
        });
        CheckBox installsystemupdate = view.findViewById(R.id.installsystemupdate);
        installsystemupdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setChecked(false);
                pickOTAFile();
            }
        });
        CheckBox reboot = view.findViewById(R.id.reboot);
        reboot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setChecked(false);
                showRebootConfirm();
            }
        });
        requestbugreport = view.findViewById(R.id.requestbugreport);
        requestbugreport.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setChecked(false);
                showrequestBugreportConfirm();
            }
        });

        ImageButton imgbutton_bugreport = view.findViewById(R.id.imgbutton_bugreport);
        imgbutton_bugreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandleListBugReport(v);
            }
        });

        CheckBox resetpassword = view.findViewById(R.id.resetpassword);
        resetpassword.setText(getString(R.string.resetpassword));
        resetpassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setChecked(false);
                showInputPassword();
            }
        });

        CheckBox maximumfailedpasswordsforwipe = view.findViewById(R.id.maximumfailedpasswordsforwipe);
        maximumfailedpasswordsforwipe.setOnCheckedChangeListener(null);
        maximumfailedpasswordsforwipe.setChecked(Application.getInstance().mInnoManager.dpc_getMaximumFailedPasswordsForWipe()>0);
        maximumfailedpasswordsforwipe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Application.getInstance().mInnoManager.dpc_setMaximumFailedPasswordsForWipe(isChecked?3:0);
                Log.v(TAG, "dpc_getMaximumFailedPasswordsForWipe: " + Application.getInstance().mInnoManager.dpc_getMaximumFailedPasswordsForWipe());
            }
        });

        CheckBox wipedata = view.findViewById(R.id.wipedata);
        wipedata.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setChecked(false);
                showWipeDataPrompt();
            }
        });

        CheckBox ismastervolumemuted = view.findViewById(R.id.ismastervolumemuted);
        ismastervolumemuted.setOnCheckedChangeListener(null);
        ismastervolumemuted.setChecked(Application.getInstance().mInnoManager.dpc_isMasterVolumeMuted());
        ismastervolumemuted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Application.getInstance().mInnoManager.dpc_setMasterVolumeMuted(isChecked);
            }
        });
        CheckBox settime = view.findViewById(R.id.settime);
        settime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setChecked(false);
                showSetTimePrompt();
            }
        });
        CheckBox settimezone = view.findViewById(R.id.settimezone);
        settimezone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setChecked(false);
                showSetTimeZonePrompt();
            }
        });
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult " + requestCode);

        if ((REQUEST_CODE_PICK_CA_FILE == requestCode) && (resultCode == RESULT_OK) && data != null) {
            HandleImportCACert(data);
        } else if ((REQUEST_CODE_PICK_OTA_FILE == requestCode) && (resultCode == RESULT_OK) && data != null) {
            HandleOTAFile(data);
        }else if (requestCode == REQUEST_CODE_CONFIRM_CREDENTIAL) {
            if (resultCode == RESULT_OK) {
                Log.v(TAG, "setting new password 2:" + mCurrentPassword);
                Application.getInstance().mInnoManager.dpc_resetPasswordWithToken(mCurrentPassword,passwordToken,0);
                devicePolicyManager.lockNow();
            } else {
                Log.v(TAG, "Provision password token failed.");
            }
        }
    }

    private void HandleOTAFile(Intent data) {
        String path = InnoUtil.getPath(getActivity(), data.getData());
        Log.v(TAG, "HandleOTAFile start: " + path + " " + Calendar.getInstance().getTime());

        Application.getInstance().mInnoManager.dpc_installSystemUpdate(path, cb2);
        Log.v(TAG, "HandleOTAFile end: " + path + " " + Calendar.getInstance().getTime());
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        byte[] result = byteBuffer.toByteArray();
        byteBuffer.close();
        return result;
    }

    private void pickCACert() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/x-pem-file");
        startActivityForResult(Intent.createChooser(intent, null), REQUEST_CODE_PICK_CA_FILE);

    }

    private void pickOTAFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/zip");
        startActivityForResult(Intent.createChooser(intent, null), REQUEST_CODE_PICK_OTA_FILE);

    }

    private CompoundButton.OnCheckedChangeListener mHiddenCheckBox = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            showHiddenAppsConfirm(isChecked);
        }
    };

    private void showHiddenAppsConfirm(final boolean isChecked) {
        hideUnusedApps.setEnabled(false);
        new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.hidden_app_title)
                .setMessage(R.string.hidden_app_msg)
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (String pkgName : Application.hideList) {
                                    if (isChecked) {
                                        if (!Application.getInstance().mInnoManager.dpc_isApplicationHidden(pkgName)) {
                                            Application.getInstance().mInnoManager.dpc_setApplicationHidden(true, pkgName);
                                        }
                                    } else {
                                        if (Application.getInstance().mInnoManager.dpc_isApplicationHidden(pkgName)) {
                                            Application.getInstance().mInnoManager.dpc_setApplicationHidden(false, pkgName);
                                        }
                                    }

                                }
                                Application.getInstance().setPref(Application.KEY_HIDE_APPS, isChecked);

                                hideUnusedApps.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        hideUnusedApps.setEnabled(true);
                                    }
                                });
                            }
                        }).start();
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        hideUnusedApps.setOnCheckedChangeListener(null);
                        hideUnusedApps.setChecked(!isChecked);
                        hideUnusedApps.setEnabled(true);
                        hideUnusedApps.setOnCheckedChangeListener(mHiddenCheckBox);
                    }
                })
                .show();
    }

    private void clearApplicationUserData(String packageName) {
        Application.getInstance().mInnoManager.dpc_clearApplicationUserData(packageName, cb1);
    }

    private myIInnoClearAppUserDataCallback cb1 = new myIInnoClearAppUserDataCallback();

    private static final class myIInnoClearAppUserDataCallback extends IInnoClearAppUserDataCallback.Stub {
        private MainActivity mContext;

        @Override
        public void onApplicationUserDataCleared(String s, boolean b) throws RemoteException {
            Log.v(TAG, "onApplicationUserDataCleared " + s + ": " + b);
        }
    }

    private myIInnoInstallSystemUpdateCallback cb2 = new myIInnoInstallSystemUpdateCallback();

    private static final class myIInnoInstallSystemUpdateCallback extends IInnoInstallSystemUpdateCallback.Stub {
        private MainActivity mContext;

        @Override
        public void onInstallUpdateError(int i, String s) throws RemoteException {
            Log.v(TAG, "onInstallUpdateError " + i + ": " + s);
        }
    }

    private static final class myIInnoBugReportCallback extends IInnoBugReportCallback.Stub {

        @Override
        public void onProgress(final int i) throws RemoteException {
            Log.v(TAG, "onProgress " + i);
            if (requestbugreport == null) return;
            requestbugreport.post(new Runnable() {
                @Override
                public void run() {
                    requestbugreport.setText(Application.getInstance().getString(R.string.requestbugreport) + " " + i + "%");
                }
            });
        }

        @Override
        public void onComplete(final String s) throws RemoteException {
            Log.v(TAG, "onComplete: " + s);
            if (requestbugreport == null) return;

            requestbugreport.post(new Runnable() {
                @Override
                public void run() {
                    requestbugreport.setEnabled(true);
                    requestbugreport.setText(Application.getInstance().getString(R.string.requestbugreport) + " " + s);
                }
            });
        }
    }

    private myIInnoBugReportCallback cb3 = new myIInnoBugReportCallback();

    private void showAppList() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> pkgAppsList = getActivity().getPackageManager().queryIntentActivities(mainIntent, 0);
        final List<String> pkgSelected = new ArrayList<String>();

        for (ResolveInfo info : pkgAppsList) {
            ApplicationInfo applicationInfo = info.activityInfo.applicationInfo;
            /*if(!((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)) {
                pkgSelected.add(applicationInfo.packageName);
            }*/
            pkgSelected.add(applicationInfo.packageName);
        }

        new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.clearapplicationuserdata_title)
                .setItems((String[]) pkgSelected.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.v(TAG, "onClick " + which + ":" + pkgSelected.get(which));
                        showAppListConfirm(pkgSelected.get(which));
                    }
                })
                .show();
    }

    private void showAppListConfirm(final String packageName) {
        String msg = String.format(getResources().getString(R.string.clearapplicationuserdata_msg), packageName);
        new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.clearapplicationuserdata_title)
                .setMessage(msg)
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        clearApplicationUserData(packageName);
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    private void showCommonConfirm(String title, String msg) {
        new MaterialAlertDialogBuilder(getActivity())
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    private void showImportCACertConfirm(int result) {
        int res = R.string.installcacert_fail_msg;
        if (result == 2) {
            res = R.string.installcacert_already_msg;
        } else if (result == 1) {
            res = R.string.installcacert_success_msg;
        }

        new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.installcacert_title)
                .setMessage(res)
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    private void showScreenLockInfo() {
        new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.setdeviceownerlockscreeninfo_title)
                .setMessage(R.string.setdeviceownerlockscreeninfo_msg)
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }


    private void showRebootConfirm() {

        new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.reboot_title)
                .setMessage(R.string.reboot_msg)
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Application.getInstance().mInnoManager.dpc_reboot();
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }


    private void showrequestBugreportConfirm() {
        new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.requestbugreport_title)
                .setMessage(R.string.requestbugreport_msg)
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestbugreport.setEnabled(false);
                        Application.getInstance().mInnoManager.dpc_bugreport_request(cb3);
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    private void HandleImportCACert(Intent data) {
        InputStream iStream = null;
        try {
            iStream = getActivity().getContentResolver().openInputStream(data.getData());
            byte[] inputData = getBytes(iStream);
            int importResult = 0;
            if (inputData != null) {
                Log.v(TAG, "inputData " + inputData.length);
                if (!Application.getInstance().mInnoManager.dpc_hasCaCertInstalled(inputData)) {
                    importResult = Application.getInstance().mInnoManager.dpc_installCaCert(inputData) ? 1 : 0;
                } else {
                    Log.v(TAG, "CA Cert Already installed " + inputData.length);
                    importResult = 2;
                }
            }
            showImportCACertConfirm(importResult);
        } catch (Exception e) {
            Log.v(TAG, "REQUEST_CODE_PICK_FILE " + e.toString());
        }
    }

    private void importCA() {
        InputStream inputStream = null;
        ByteArrayOutputStream output = null;
        try {
            inputStream = getActivity().getAssets().open("myCA.pem");
            byte[] buffer = new byte[8192];
            int bytesRead;
            output = new ByteArrayOutputStream();
            while (true) {


                if (!((bytesRead = inputStream.read(buffer)) != -1)) break;
                output.write(buffer, 0, bytesRead);

            }
            byte file[] = output.toByteArray();
            Application.getInstance().mInnoManager.dpc_installCaCert(file);
        } catch (Exception e) {
            Log.v(TAG, "importCA " + e.toString());
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (Exception e) {
                Log.v(TAG, "importCA " + e.toString());
            }
            try {
                if (output != null) output.close();
            } catch (Exception e) {
                Log.v(TAG, "importCA " + e.toString());
            }
        }
    }

    public void HandleListBugReport(View view) {
        String strings = Application.getInstance().mInnoManager.dpc_bugreport_listfile();

        if (strings != null) {
            final String[] files = strings.split(" ");
            new MaterialAlertDialogBuilder(getActivity())
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
                                    String accessname = Application.getInstance().mInnoManager.dpc_bugreport_openfile(latestFileName);
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

                                        while ((content = Application.getInstance().mInnoManager.dpc_bugreport_readfile(latestFileName, data)) != -1) {
                                            fis.write(data, 0, content);
                                        }
                                        message = "File saved to "+saveName;
                                    } catch (Exception e) {
                                        Log.e(TAG, "IOException: " + e.toString());
                                    } finally {
                                        if (accessname != null)
                                            Application.getInstance().mInnoManager.dpc_bugreport_closefile(accessname);
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
                                    requestbugreport.setText(Application.getInstance().getString(R.string.requestbugreport) + " " + message);
                                }

                            }.execute();


                        }
                    })
                    .show();
        }
    }

    public byte[] passwordToken;
    public String mCurrentPassword;
    private void showInputPassword() {
        if(passwordToken==null){
            passwordToken = InnoUtil.generateRandomPasswordToken();

        }
        Log.e(TAG, "Current PasswordToken " + (passwordToken != null ? Base64.getEncoder().encodeToString(passwordToken)
                : "N/A"));

        if(passwordToken==null) return;

        LayoutInflater inflater =  getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_resetpassword, null);
        final EditText edittext = view.findViewById(R.id.password);
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.resetpassword_title)
                .setView(view)
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mCurrentPassword = edittext.getText().toString().trim();
                        if(TextUtils.isEmpty(mCurrentPassword)) mCurrentPassword=null;//null to remove password
                        Log.v(TAG, "showInputPassword " + mCurrentPassword);
                        if(Application.getInstance().mInnoManager.dpc_setResetPasswordToken(passwordToken)){
                            if(Application.getInstance().mInnoManager.dpc_isResetPasswordTokenActive()){
                                Log.v(TAG, "setting new password 1: " + mCurrentPassword);
                                Application.getInstance().mInnoManager.dpc_resetPasswordWithToken(mCurrentPassword,passwordToken,0);
                                devicePolicyManager.lockNow();
                            }else{
                                Log.v(TAG, "password token not active " + edittext.getText().toString());
                                Intent intent = mKeyguardMgr.createConfirmDeviceCredentialIntent(null, null);
                                if (intent != null) {
                                    startActivityForResult(intent, REQUEST_CODE_CONFIRM_CREDENTIAL);
                                }
                            }

                        }else{
                            Log.v(TAG, "dpc_setResetPasswordToken failed.");
                        }

                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        materialAlertDialogBuilder.setCancelable(false);
        materialAlertDialogBuilder.create();
        materialAlertDialogBuilder.show();
    }
    private byte[] myCABytes;
    private void loadCACert(){
        InputStream stream= null;
        try {
            stream =getActivity(). getAssets().open("myCA.pem");
            myCABytes=new byte[stream.available()];
            stream.read(myCABytes);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void showWipeDataPrompt(){
        new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.wipe_data_title)
                .setMessage(R.string.wipe_data_confirmation)
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int flags = 0;
                        flags |= DevicePolicyManager.WIPE_EXTERNAL_STORAGE;
                        flags |= DevicePolicyManager.WIPE_RESET_PROTECTION_DATA;
                        Application.getInstance().mInnoManager.dpc_wipeData(flags,"user_wipe");
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    private void showSetTimePrompt(){
        LayoutInflater inflater =  getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_settime, null);
        final EditText edittext = view.findViewById(R.id.settime);
        final String currentTime = Long.toString(System.currentTimeMillis());
        edittext.setText(currentTime);

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.settime_title)
                .setView(view)
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newTimeString = edittext.getText().toString().trim();
                        if (newTimeString.isEmpty()) {
                            showToast(R.string.no_set_time);
                            return;
                        }
                        long newTime = 0;
                        try {
                            newTime = Long.parseLong(newTimeString);
                        } catch (NumberFormatException e) {
                            showToast(R.string.invalid_set_time);
                            return;
                        }
                        boolean result = Application.getInstance().mInnoManager.dpc_setTime(newTime);
                        showToast("SetTime: "+result);
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        materialAlertDialogBuilder.setCancelable(false);
        materialAlertDialogBuilder.create();
        materialAlertDialogBuilder.show();
    }
    private void showSetTimeZonePrompt(){
        LayoutInflater inflater =  getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_settime, null);
        final EditText edittext = view.findViewById(R.id.settime);
        final String currentTime =  Calendar.getInstance().getTimeZone().getID();
        edittext.setText(currentTime);

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(getActivity())
                .setTitle(R.string.settimezone_title)
                .setView(view)
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Application.getInstance().mInnoManager.dpc_setGlobalSetting(Settings.Global.AUTO_TIME_ZONE,"0");
                        final String newTimezone = edittext.getText().toString();
                        if (newTimezone.isEmpty()) {
                            showToast(R.string.no_timezone);
                            return;
                        }
                        final String[] ids = TimeZone.getAvailableIDs();
                        if (!Arrays.asList(ids).contains(newTimezone)) {
                            showToast(R.string.invalid_timezone);
                            return;
                        }

                        boolean result = Application.getInstance().mInnoManager.dpc_setTimeZone(newTimezone);
                        showToast("setTimeZone: "+result);
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        materialAlertDialogBuilder.setCancelable(false);
        materialAlertDialogBuilder.create();
        materialAlertDialogBuilder.show();
    }
    private void showToast(int msgId, Object... args) {
        showToast(getString(msgId, args));
    }

    private void showToast(String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    private void showToast(String msg, int duration) {
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        Toast.makeText(activity, msg, duration).show();
    }
}
