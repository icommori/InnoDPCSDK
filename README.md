<img src="/app/src/main/ic_launcher-playstore.png" width="70" height="70">

# InnoService SDK for DPC:

InnoDPCSDK is an SDK designed to help EMM developer to use Innocom DPC interface for controlling device policy. The sample app will illustrate how to use InnoDPCSDK. 

## Precondtion

* InnoDPC Service enabled on Innocomm devices.
* Devices must define the following uses-feature:
   * android.software.managed_users
   * android.software.device_admin

## Features

* Enter or Exit Device owner mode anytime you want.[no need to set it before setupwizard or through adb]. 
* Easy programming with an manager that provided by Innocomm SDK. 
* The InnoManager user can be restricted to only for system apps or platform apps.
* Easy to integrate with Cloud  functions.
* Support 60 restrictions and many addtional functions.

## Restriction Type

* User Restrictions: Set a user restriction specified by the key.
* Specific Restrictions: Dedicated DPC API operation or combind some APIs call.

### User Restrictions


|   |     |   |
| ------------- | -------------   | :------------- |
| 1 | DISALLOW_CAMERA | Specifies if a user is not allowed to use the camera. |
| 2 | DISALLOW_INSTALL_APPS | Specifies if a user is disallowed from installing applications. This user restriction also prevents device owners and profile owners installing apps. |
| 3 | DISALLOW_UNINSTALL_APPS | Specifies if a user is disallowed from uninstalling applications. |
| 4 | DISALLOW_APPS_CONTROL | Specifies if a user is disallowed from modifying applications in Settings or launchers. The following actions will not be allowed when this:    restriction is enabled    uninstalling apps    disabling apps    clearing app caches    clearing app data    force stopping apps    Clearing app defaults  |
| 5 | DISALLOW_OUTGOING_CALLS | Specifies that the user is not allowed to make outgoing phone calls. Emergency calls are still permitted. |
| 6 | DISALLOW_FACTORY_RESET | Specifies if a user is disallowed from factory resetting from Settings. |
| 7 | DISALLOW_NETWORK_RESET | Specifies if a user is disallowed from resetting network settings from Settings. This can only be set by device owners and profile owners on the primary user. |
| 8 | DISALLOW_USER_SWITCH | Specifies if user switching is blocked on the current user. |
| 9 | DISALLOW_PRINTING | Specifies whether the user is allowed to print. |
| 10 | DISALLOW_SMS | Specifies that the user is not allowed to send or receive SMS messages. |
| 11 | DISALLOW_MOUNT_PHYSICAL_MEDIA | Specifies if a user is disallowed from mounting physical external media. |
| 12 | DISALLOW_MODIFY_ACCOUNTS | Specifies if a user is disallowed from adding and removing accounts  unless they are added by Authenticator. |
| 13 | DISALLOW_CONFIG_WIFI | Specifies if a user is disallowed from changing Wi-Fi access points via Settings. |
| 14 | DISALLOW_CONFIG_LOCALE | Specifies if a user is disallowed from changing the device language. |
| 15 | DISALLOW_SHARE_LOCATION | Specifies if a user is disallowed from turning on location sharing. |
| 16 | DISALLOW_AIRPLANE_MODE | Specifies if airplane mode is disallowed on the device. |
| 17 | DISALLOW_CONFIG_BRIGHTNESS | Specifies if a user is disallowed from configuring brightness. When device owner sets it,  it'll only be applied on the target(system) user.  |
| 18 | DISALLOW_AMBIENT_DISPLAY | Specifies if ambient display is disallowed for the user. |
| 19 | DISALLOW_CONFIG_SCREEN_TIMEOUT | Specifies if a user is disallowed from changing screen off timeout. |
| 20 | DISALLOW_INSTALL_UNKNOWN_SOURCES | Specifies if a user is disallowed from enabling the  |
| 21 | DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY | This restriction is a device-wide version of {@link #DISALLOW_INSTALL_UNKNOWN_SOURCES}. |
| 22 | DISALLOW_CONFIG_BLUETOOTH | Specifies if a user is disallowed from configuring bluetooth via Settings. This does not restrict the user from turning bluetooth on or off. |
| 23 | DISALLOW_BLUETOOTH | Specifies if bluetooth is disallowed on the device. If bluetooth is disallowed on the device,  bluetooth cannot be turned on or configured via Settings.  |
| 24 | DISALLOW_BLUETOOTH_SHARING | Specifies if outgoing bluetooth sharing is disallowed. |
| 25 | DISALLOW_USB_FILE_TRANSFER | Specifies if a user is disallowed from transferring files over USB. |
| 26 | DISALLOW_CONFIG_CREDENTIALS | Specifies if a user is disallowed from configuring user credentials. |
| 27 | DISALLOW_REMOVE_USER | When set on the admin user this specifies if the user can remove users. When set on a non-admin secondary user,  this specifies if the user can remove itself. This restriction has no effect on managed profiles.  |
| 28 | DISALLOW_REMOVE_MANAGED_PROFILE | Specifies if managed profiles of this user can be removed,  other than by its profile owner.  |
| 29 | DISALLOW_DEBUGGING_FEATURES | Specifies if a user is disallowed from enabling or accessing debugging features. |
| 30 | DISALLOW_CONFIG_VPN | Specifies if a user is disallowed from configuring a VPN. |
| 31 | DISALLOW_CONFIG_LOCATION | Specifies if a user is disallowed from enabling or disabling location providers. As a result,  user is disallowed from turning on or off location via Settings.  |
| 32 | DISALLOW_CONFIG_DATE_TIME | Specifies configuring date,  time and timezone is disallowed via Settings.  |
| 33 | DISALLOW_CONFIG_TETHERING | Specifies if a user is disallowed from configuring Tethering and portable hotspots via Settings. |
| 34 | DISALLOW_ADD_USER | Specifies if a user is disallowed from adding new users. |
| 35 | DISALLOW_ADD_MANAGED_PROFILE | Specifies if a user is disallowed from adding managed profiles. |
| 36 | ENSURE_VERIFY_APPS | Specifies if a user is disallowed from disabling application verification. |
| 37 | DISALLOW_CONFIG_CELL_BROADCASTS | Specifies if a user is disallowed from configuring cell broadcasts. |
| 38 | DISALLOW_CONFIG_MOBILE_NETWORKS | Specifies if a user is disallowed from configuring mobile networks. |
| 39 | DISALLOW_UNMUTE_MICROPHONE | Specifies if a user is disallowed from adjusting microphone volume. If set,  the microphone will be muted.  |
| 40 | DISALLOW_ADJUST_VOLUME | Specifies if a user is disallowed from adjusting the master volume. If set,  the master volume will be muted. This can be set by device owners from API 21 and profile owners from API 24.  |
| 41 | DISALLOW_CREATE_WINDOWS | Specifies that windows besides app windows should not be created. This will block the creation of the following types of windows. |
| 42 | DISALLOW_SYSTEM_ERROR_DIALOGS | Specifies that system error dialogs for crashed or unresponsive apps should not be shown. In this case,  the system will force-stop the app as if the user chooses the  |
| 43 | DISALLOW_CROSS_PROFILE_COPY_PASTE | Specifies if the clipboard contents can be exported by pasting the data into other users or profiles. This restriction doesn't prevent import,  such as someone pasting clipboard data from other profiles or users.   |
| 44 | DISALLOW_OUTGOING_BEAM | Specifies if the user is not allowed to use NFC to beam out data from apps. |
| 45 | DISALLOW_WALLPAPER  | Hidden user restriction to disallow access to wallpaper manager APIs. This restriction generally means that wallpapers are not supported for the particular user. This user restriction is always set for managed profiles,  because such profiles don't have wallpapers.  |
| 46 | DISALLOW_SET_WALLPAPER | User restriction to disallow setting a wallpaper. Profile owner and device owner are able to set wallpaper regardless of this restriction. |
| 47 | DISALLOW_SAFE_BOOT  | Specifies if the user is not allowed to reboot the device into safe boot mode. |
| 48 | DISALLOW_RUN_IN_BACKGROUND  | Specifies if a user is not allowed to run in the background and should be stopped during user switch. |
| 49 | DISALLOW_UNMUTE_DEVICE | Specifies if a user is not allowed to unmute the device's master volume. |
| 50 | DISALLOW_DATA_ROAMING | Specifies if a user is not allowed to use cellular data when roaming. |
| 51 | DISALLOW_SET_USER_ICON | Specifies if a user is not allowed to change their icon. Device owner and profile owner can set this restriction. When it is set by device owner,  only the target user will be affected.  |
| 52 | DISALLOW_OEM_UNLOCK | Specifies if a user is not allowed to enable the oem unlock setting. Setting this restriction has no effect if the bootloader is already unlocked. |
| 53 | DISALLOW_UNIFIED_PASSWORD | Specifies that the managed profile is not allowed to have unified lock screen challenge with the primary user. |
| 54 | ALLOW_PARENT_PROFILE_APP_LINKING | Allows apps in the parent profile to handle web links from the managed profile. |
| 55 | DISALLOW_AUTOFILL | Specifies if a user is not allowed to use Autofill Services. |
| 56 | DISALLOW_CONTENT_CAPTURE | Specifies if the contents of a user's screen is not allowed to be captured for artificial intelligence purposes. |
| 57 | DISALLOW_CONTENT_SUGGESTIONS | Specifies if the current user is able to receive content suggestions for selections based on the contents of their screen. |
| 58 | DISALLOW_SHARE_INTO_MANAGED_PROFILE | Specifies whether the user can share file / picture / data from the primary user into the managed profile,  either by sending them from the primary side or by picking up data within an app in the managed profile.  |
| 59 | DISALLOW_CONFIG_PRIVATE_DNS | Specifies whether the user is allowed to modify private DNS settings. |
| 60 | KEY_RESTRICTIONS_PENDING | Application restriction key that is used to indicate the pending arrival of real restrictions for the app.Applications that support restrictions should check for the presence of this key.A  true value indicates that restrictions may be applied in the near future but are not available yet. It is the responsibility of any management application that sets this flag to update it when the final restrictions are enforced. |

### Specific Restrictions:

|   |     |   |
| ------------- | -------------   | :------------- |
| 1 | Disable Statusbar | Disabling the status bar blocks notifications and quick settings. |
| 2 | Disallow ADB Debug | Specifies if a user is disallowed from enabling or accessing debugging features. |
| 3 | Apps visibility | Hide or unhide packages. |
| 4 | Disallow screen capture | To set whether the screen capture is disabled. Disabling screen capture also prevents the content from being shown on display devices that do not have a secure video output.  |
| 5 | Clear User data | Clear application user data of a given package. The behavior of this is equivalent to the target application calling [ActivityManager#clearApplicationUserData()]. |
| 6 | Install CA | Installs the given certificate as a user CA. |
| 7 | WIFI MAC | Get the MAC address of the Wi-Fi device. |
| 8 | Lock Screen Info | Sets the device owner information to be shown on the lock screen.  |
| 9 | System Update | Install  a system update from the given file..  |
| 10 | Reboot Device | Reboot the device. If there is an ongoing call on the device. |
| 11 | BugReport | Request a bugreport.  |
| 12 | ResetPassword | Set a new device unlock password.  |
| 13 | Max password Retry |  Will perform a device wipe after too many incorrect device-unlock passwords  |
| 14 | Wipe data |  Ask that all user data be wiped.  |
| 15 | Master volume Muted |  Set the master volume mute on or off. |
| 16 | Settings |  Update  android.provider.Settings.Global/System/Secure settings |
## How to use InnoDPC SDK

1. Import jar file that provided from Innocomm.
```
Ex. Add following line in your build.gradle:
compileOnly project(path: ':libInnoService')
```

2.  To use the library from the device we need to add <uses-library> tag to the manifest file.
```
<application
      ...
      android:theme="@style/AppTheme">
      <uses-library android:name="com.innocomm.innoservice" android:required="true" />
      <activity android:name=".MainActivity">
               <intent-filter>
                   <action android:name="android.intent.action.MAIN" />
                   <category android:name="android.intent.category.LAUNCHER" />
               </intent-filter>
      </activity>
   </application>
```

3. Now just create an object from the InnoManager class and use it.
```
InnoManager iman = InnoManager.getInstance();
iman.dpc_setDeviceOwner();
```
  
  
