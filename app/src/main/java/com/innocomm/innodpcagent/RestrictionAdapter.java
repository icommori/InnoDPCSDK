package com.innocomm.innodpcagent;

import android.content.Context;
import android.content.DialogInterface;
import android.os.UserManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.innocomm.innoservice.InnoManager;

public class RestrictionAdapter extends BaseAdapter implements View.OnClickListener {

    private static final String TAG = "RestrictionAdapter";
    private LayoutInflater mLayInf;
    private InnoManager mInnoManager;
    private Context mCtx;

    public RestrictionAdapter(Context context) {
        mLayInf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInnoManager = Application.getInstance().mInnoManager;
        mCtx = context;
    }

    @Override
    public int getCount() {
        return DisallowMap.length;
    }

    @Override
    public Object getItem(int position) {
        return DisallowMap[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String data[] = (String[]) getItem(position);
        View v = mLayInf.inflate(R.layout.listview_item, parent, false);

        CheckBox chkbox = (CheckBox) v.findViewById(R.id.checkbox);
        chkbox.setChecked(mInnoManager.dpc_getUserRestriction(data[1]));
        chkbox.setOnCheckedChangeListener(null);
        chkbox.setText(data[0]);
        chkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mInnoManager.dpc_addUserRestriction(isChecked, data[1]);
            }
        });

        ImageButton imgBtn = v.findViewById(R.id.imgbutton);
        imgBtn.setOnClickListener(this);
        imgBtn.setTag(position);

        //Log.v(TAG, position + ":" + data[0] + " " + chkbox.isChecked());
        return v;
    }


    @Override
    public void onClick(View v) {

        int position = (int) v.getTag();
        String data[] = (String[]) getItem(position);
        showDetail(data[2]);
    }

    private void showDetail(String msg) {
        new MaterialAlertDialogBuilder(mCtx)
                .setTitle(R.string.restriction_title)
                .setMessage(msg)
                .setPositiveButton(mCtx.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    private static final String DisallowMap[][] = new String[][]{
            {"DISALLOW_CAMERA", "no_camera", "Specifies if a user is not allowed to use the camera."},
            {"DISALLOW_INSTALL_APPS", UserManager.DISALLOW_INSTALL_APPS, "Specifies if a user is disallowed from installing applications. This user restriction also prevents device owners and profile owners installing apps."},
            {"DISALLOW_UNINSTALL_APPS", UserManager.DISALLOW_UNINSTALL_APPS, "Specifies if a user is disallowed from uninstalling applications."},
            {"DISALLOW_APPS_CONTROL", UserManager.DISALLOW_APPS_CONTROL, "Specifies if a user is disallowed from modifying applications in Settings or launchers. The following actions will not be allowed when this\n" +
                    " restriction is enabled:\n" +
                    " uninstalling apps\n" +
                    " disabling apps\n" +
                    " clearing app caches\n" +
                    " clearing app data\n" +
                    " force stopping apps\n" +
                    " clearing app defaults"},
            {"DISALLOW_OUTGOING_CALLS", UserManager.DISALLOW_OUTGOING_CALLS, "Specifies that the user is not allowed to make outgoing phone calls. Emergency calls are still permitted."},
            {"DISALLOW_FACTORY_RESET", UserManager.DISALLOW_FACTORY_RESET, "Specifies if a user is disallowed from factory resetting from Settings."},
            {"DISALLOW_NETWORK_RESET", UserManager.DISALLOW_NETWORK_RESET, "Specifies if a user is disallowed from resetting network settings from Settings. This can only be set by device owners and profile owners on the primary user."},
            {"DISALLOW_USER_SWITCH", UserManager.DISALLOW_USER_SWITCH, "Specifies if user switching is blocked on the current user."},
            {"DISALLOW_PRINTING", UserManager.DISALLOW_PRINTING, "Specifies whether the user is allowed to print."},
            {"DISALLOW_SMS", UserManager.DISALLOW_SMS, "Specifies that the user is not allowed to send or receive SMS messages."},
            {"DISALLOW_MOUNT_PHYSICAL_MEDIA", UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, "Specifies if a user is disallowed from mounting physical external media."},
            {"DISALLOW_MODIFY_ACCOUNTS", UserManager.DISALLOW_MODIFY_ACCOUNTS, "Specifies if a user is disallowed from adding and removing accounts, unless they are added by Authenticator."},
            {"DISALLOW_CONFIG_WIFI", UserManager.DISALLOW_CONFIG_WIFI, "Specifies if a user is disallowed from changing Wi-Fi access points via Settings."},
            {"DISALLOW_CONFIG_LOCALE", UserManager.DISALLOW_CONFIG_LOCALE, "Specifies if a user is disallowed from changing the device language."},
            {"DISALLOW_SHARE_LOCATION", UserManager.DISALLOW_SHARE_LOCATION, "Specifies if a user is disallowed from turning on location sharing."},
            {"DISALLOW_AIRPLANE_MODE", UserManager.DISALLOW_AIRPLANE_MODE, "Specifies if airplane mode is disallowed on the device."},
            {"DISALLOW_CONFIG_BRIGHTNESS", UserManager.DISALLOW_CONFIG_BRIGHTNESS, "Specifies if a user is disallowed from configuring brightness. When device owner sets it, it'll only be applied on the target(system) user."},
            {"DISALLOW_AMBIENT_DISPLAY", UserManager.DISALLOW_AMBIENT_DISPLAY, "Specifies if ambient display is disallowed for the user."},
            {"DISALLOW_CONFIG_SCREEN_TIMEOUT", UserManager.DISALLOW_CONFIG_SCREEN_TIMEOUT, "Specifies if a user is disallowed from changing screen off timeout."},
            {"DISALLOW_INSTALL_UNKNOWN_SOURCES", UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, "Specifies if a user is disallowed from enabling the \"Unknown Sources\" setting, that allows installation of apps from unknown sources. Unknown sources exclude adb and special apps such as trusted app stores."},
            {"DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY", UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY, "This restriction is a device-wide version of {@link #DISALLOW_INSTALL_UNKNOWN_SOURCES}."},
            {"DISALLOW_CONFIG_BLUETOOTH", UserManager.DISALLOW_CONFIG_BLUETOOTH, "Specifies if a user is disallowed from configuring bluetooth via Settings. This does not restrict the user from turning bluetooth on or off."},
            {"DISALLOW_BLUETOOTH", UserManager.DISALLOW_BLUETOOTH, "Specifies if bluetooth is disallowed on the device. If bluetooth is disallowed on the device, bluetooth cannot be turned on or configured via Settings."},
            {"DISALLOW_BLUETOOTH_SHARING", UserManager.DISALLOW_BLUETOOTH_SHARING, "Specifies if outgoing bluetooth sharing is disallowed."},
            {"DISALLOW_USB_FILE_TRANSFER", UserManager.DISALLOW_USB_FILE_TRANSFER, "Specifies if a user is disallowed from transferring files over USB."},
            {"DISALLOW_CONFIG_CREDENTIALS", UserManager.DISALLOW_CONFIG_CREDENTIALS, "Specifies if a user is disallowed from configuring user credentials."},
            {"DISALLOW_REMOVE_USER", UserManager.DISALLOW_REMOVE_USER, "When set on the admin user this specifies if the user can remove users.\nWhen set on a non-admin secondary user, this specifies if the user can remove itself.\nThis restriction has no effect on managed profiles."},
            {"DISALLOW_REMOVE_MANAGED_PROFILE", UserManager.DISALLOW_REMOVE_MANAGED_PROFILE, "Specifies if managed profiles of this user can be removed, other than by its profile owner."},
            {"DISALLOW_DEBUGGING_FEATURES", UserManager.DISALLOW_DEBUGGING_FEATURES, "Specifies if a user is disallowed from enabling or accessing debugging features."},
            {"DISALLOW_CONFIG_VPN", UserManager.DISALLOW_CONFIG_VPN, "Specifies if a user is disallowed from configuring a VPN."},
            {"DISALLOW_CONFIG_LOCATION", UserManager.DISALLOW_CONFIG_LOCATION, "Specifies if a user is disallowed from enabling or disabling location providers. As a result, user is disallowed from turning on or off location via Settings."},
            {"DISALLOW_CONFIG_DATE_TIME", UserManager.DISALLOW_CONFIG_DATE_TIME, "Specifies configuring date, time and timezone is disallowed via Settings."},
            {"DISALLOW_CONFIG_TETHERING", UserManager.DISALLOW_CONFIG_TETHERING, "Specifies if a user is disallowed from configuring Tethering and portable hotspots via Settings."},
            {"DISALLOW_ADD_USER", UserManager.DISALLOW_ADD_USER, "Specifies if a user is disallowed from adding new users."},
            {"DISALLOW_ADD_MANAGED_PROFILE", UserManager.DISALLOW_ADD_MANAGED_PROFILE, "Specifies if a user is disallowed from adding managed profiles."},
            {"ENSURE_VERIFY_APPS", UserManager.ENSURE_VERIFY_APPS, "Specifies if a user is disallowed from disabling application verification."},
            {"DISALLOW_CONFIG_CELL_BROADCASTS", UserManager.DISALLOW_CONFIG_CELL_BROADCASTS, "Specifies if a user is disallowed from configuring cell broadcasts."},
            {"DISALLOW_CONFIG_MOBILE_NETWORKS", UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS, "Specifies if a user is disallowed from configuring mobile networks."},
            {"DISALLOW_UNMUTE_MICROPHONE", UserManager.DISALLOW_UNMUTE_MICROPHONE, "Specifies if a user is disallowed from adjusting microphone volume. If set, the microphone will be muted."},
            {"DISALLOW_ADJUST_VOLUME", UserManager.DISALLOW_ADJUST_VOLUME, "Specifies if a user is disallowed from adjusting the master volume. If set, the master volume will be muted. This can be set by device owners from API 21 and profile owners from API 24."},
            //{"DISALLOW_FUN",UserManager.DISALLOW_FUN,"Specifies if the user is not allowed to have fun. In some cases, the device owner may wish to prevent the user from experiencing amusement or joy while using the device."},
            {"DISALLOW_CREATE_WINDOWS", UserManager.DISALLOW_CREATE_WINDOWS, "Specifies that windows besides app windows should not be created. This will block the creation of the following types of windows."},
            {"DISALLOW_SYSTEM_ERROR_DIALOGS", UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS, "Specifies that system error dialogs for crashed or unresponsive apps should not be shown. In this case, the system will force-stop the app as if the user chooses the \"close app\" option on the UI. A feedback report isn't collected as there is no way for the user to provide explicit consent."},
            {"DISALLOW_CROSS_PROFILE_COPY_PASTE", UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE, "Specifies if the clipboard contents can be exported by pasting the data into other users or profiles. This restriction doesn't prevent import, such as someone pasting clipboard data from other profiles or users. "},
            {"DISALLOW_OUTGOING_BEAM", UserManager.DISALLOW_OUTGOING_BEAM, "Specifies if the user is not allowed to use NFC to beam out data from apps."},
            {"DISALLOW_WALLPAPER ", "no_wallpaper", "Hidden user restriction to disallow access to wallpaper manager APIs. This restriction generally means that wallpapers are not supported for the particular user. This user restriction is always set for managed profiles, because such profiles don't have wallpapers."},
            {"DISALLOW_SET_WALLPAPER", UserManager.DISALLOW_SET_WALLPAPER, "User restriction to disallow setting a wallpaper. Profile owner and device owner are able to set wallpaper regardless of this restriction."},
            {"DISALLOW_SAFE_BOOT ", UserManager.DISALLOW_SAFE_BOOT, "Specifies if the user is not allowed to reboot the device into safe boot mode."},
            {"DISALLOW_RUN_IN_BACKGROUND ", "no_run_in_background", "Specifies if a user is not allowed to run in the background and should be stopped during user switch."},
            {"DISALLOW_UNMUTE_DEVICE", "disallow_unmute_device", "Specifies if a user is not allowed to unmute the device's master volume."},
            {"DISALLOW_DATA_ROAMING", UserManager.DISALLOW_DATA_ROAMING, "Specifies if a user is not allowed to use cellular data when roaming."},
            {"DISALLOW_SET_USER_ICON", UserManager.DISALLOW_SET_USER_ICON, "Specifies if a user is not allowed to change their icon. Device owner and profile owner can set this restriction. When it is set by device owner, only the target user will be affected."},
            {"DISALLOW_OEM_UNLOCK", "no_oem_unlock", "Specifies if a user is not allowed to enable the oem unlock setting. Setting this restriction has no effect if the bootloader is already unlocked."},
            {"DISALLOW_UNIFIED_PASSWORD", UserManager.DISALLOW_UNIFIED_PASSWORD, "Specifies that the managed profile is not allowed to have unified lock screen challenge with the primary user."},
            {"ALLOW_PARENT_PROFILE_APP_LINKING", UserManager.ALLOW_PARENT_PROFILE_APP_LINKING, "Allows apps in the parent profile to handle web links from the managed profile."},
            {"DISALLOW_AUTOFILL", UserManager.DISALLOW_AUTOFILL, "Specifies if a user is not allowed to use Autofill Services."},
            {"DISALLOW_CONTENT_CAPTURE", UserManager.DISALLOW_CONTENT_CAPTURE, "Specifies if the contents of a user's screen is not allowed to be captured for artificial intelligence purposes."},
            {"DISALLOW_CONTENT_SUGGESTIONS", UserManager.DISALLOW_CONTENT_SUGGESTIONS, "Specifies if the current user is able to receive content suggestions for selections based on the contents of their screen."},
            {"DISALLOW_SHARE_INTO_MANAGED_PROFILE", UserManager.DISALLOW_SHARE_INTO_MANAGED_PROFILE, "Specifies whether the user can share file / picture / data from the primary user into the managed profile, either by sending them from the primary side, or by picking up data within an app in the managed profile."},
            {"DISALLOW_CONFIG_PRIVATE_DNS", UserManager.DISALLOW_CONFIG_PRIVATE_DNS, "Specifies whether the user is allowed to modify private DNS settings."},
            {"KEY_RESTRICTIONS_PENDING", UserManager.KEY_RESTRICTIONS_PENDING, "Application restriction key that is used to indicate the pending arrival of real restrictions for the app.\n" +
                    "Applications that support restrictions should check for the presence of this key. A  true value indicates that restrictions may be applied in the near future but are not available yet. It is the responsibility of any management application that sets this flag to update it when the final restrictions are enforced."},

    };

}
