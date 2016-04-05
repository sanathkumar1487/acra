# Introduction #

[A new version](http://code.google.com/p/acra/downloads/detail?name=acra-2.1.0-test.jar) of the ACRA library has to be tested. It includes bugfixes for the Status Bar Notification user interaction mode and on demand silent reports, plus a couple of new API features regarding library activation in applications preferences.

Please test it and [report any issue](http://code.google.com/p/acra/issues/list) so we can try to go out of the testing phase with a clean new version of the library in a couple of weeks.

# Details #

What has to be tested ?

  * Status Bar Notification / Dialog
    * Relaunching crashed app from home key recent app shortcut should not display crash report dialog again ([Issue 6](https://code.google.com/p/acra/issues/detail?id=6)). You will need to update your manifest to add two attributes to your `CrashReportDialog` Activity declaration (`excludeFromRecents and finishOnTaskLaunch`):
```
    <activity android:name="org.acra.CrashReportDialog"
        android:theme="@android:style/Theme.Dialog"
        android:launchMode="singleInstance"
        android:excludeFromRecents="true"
        android:finishOnTaskLaunch="true" />
```

  * Pending on-demand silent notifications ([Issue 7](https://code.google.com/p/acra/issues/detail?id=7)). When using ACRA with Toast or Notification mode, sending a silent report with `ErrorReporter.handleSilentException()` could result to a user notification if the silent report could not be sent immediately. There was nothing stored indicating that it was silent so they were processed like any other crash report. This should now be fixed in ACRA 2.1.

  * Provide a way to override the preferences name for storing 'acra.disable' ([Issue 4](https://code.google.com/p/acra/issues/detail?id=4)). You can now override `CrashReportingApplication.getACRASharedPreferences()` in your `Application` class and return any SharedPreferences object. ACRA will listen to changes on these `SharedPreferences` to detect user enabling/disabling crash reports in your preferences screen. If you don't override the method, the `Application` default `SharedPreferences` are used.

  * Provide a way to let the developers chose to make users **check** a preference **to enable** crash reports (requested in a comment of [Issue 4](https://code.google.com/p/acra/issues/detail?id=4)) instead of **checking to disable**. You can now alternatively use the preference name '`acra.enable`'. It has the opposite effect of '`acra.disable`'.

# Feedback #
Please add new issues to the [issue tracker](http://code.google.com/p/acra/issues/list) to allow proper management of any problem with the new library.

Comments on this page are also welcome if you tested the library and everything was ok (a few details about what you did test would be nice too).

Thank you for your help !