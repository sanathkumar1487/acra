# ACRA has moved to GitHub #
**The project home page is now http://acra.ch
Wiki, source code, issue tracker and everything else is now based at http://github.com/ACRA/acra.**

**_The following documentation is deprecated and kept here to prevent the internets from melting._**


---




ACRA-4.3.0RC:
  * Removed a few unnecessary Log.d traces.
  * Changed old spreadsheet.google.com default form post URL for the newer one docs.google.com/spreadsheet
  * After new discussion around [Issue 111](https://code.google.com/p/acra/issues/detail?id=111), GoogleFormSender and HttpPostSender get their former constructors back in addition to the new constructor without formKey/formUri. So, if the sender is initialized without formUri/formKey provided, it will always look at ACRA.getConfig() to get its destination so it can be changed dynamically. If the formKey/formUri is provided, then it is set once and never changed.
  * Various changes due to discussion on [Issue 111](https://code.google.com/p/acra/issues/detail?id=111):
    * Creation of a setDefaultReportSenders() method on ErrorReporter. This is the method which is called by ACRA.init() to instanciate the relevant default ReportSenders depending on the provision of mailTo, formUri and formKey parameters.
> > > The method should now be called whenever you wish to change the configuration by switching from one of these 3 parameter to the others.
    * HTTPPostSender and GoogleFormSender do not take their formURI/formKey as a constructor parameter anymore. They get the value from ACRA.getConfig() instead. If their parameters are changed programmatically, they don't need to be reinstanciated.
    * Addition of a warning message if ACRA.getConfig() is called before ACRA.init(). ACRA.getNewDefaultConfig() should be used instead.
    * ACRA.getNewDefaultConfig() now requires an Application as a parameter. This allows to create a new ACRAConfiguration instance with @ReportsCrashes defined values BEFORE calling ACRA.init()
  * added: new configuration item googleFormUrlFormat to allow, if Google Docs/Drive services evolve, to modify the Form URL structure without releasing a new ACRA version in emergency.

ACRA-4.3.0b2:
  * fixed: [issue 137](https://code.google.com/p/acra/issues/detail?id=137) - with JellyBean, we now send only logcat traces related to our app. But this does not require the READ\_LOGS permission

ACRA-4.3.0b1:
  * modified: MediaCodecListCollector now displays codec profiles and levels names instead of abstract int values.
  * added: [issue 135](https://code.google.com/p/acra/issues/detail?id=135) - 'deleteOldUnsentReportsOnApplicationStart' annotation to control whether to delete old reports (defaults to true).
  * modified: removed DEVICE\_ID from the default fields. INSTALLATION\_ID should be enough and is not a sensible private data.
  * modified: [issue 111](https://code.google.com/p/acra/issues/detail?id=111) - ACRAConfiguration is not a static fields container anymore. Each instance holds its own set of values, this allow to define various configuration instances and switch between one or the other with a single object reference switch (ACRA.setConfig()))
  * added: MediaCodecListCollector to retrieve the list of supported codecs and capabilities for the device. Field name: MEDIA\_CODEC\_LIST. (Since API Level 16 - Jelly Beans)
  * added: ThreadCollector to retrieve the broken thread Id and Name. Field name: THREAD\_DETAILS
  * modified: Refactored Configuration collection class, was not in the collectors package.
  * added back an handleException(Throwable e) without the endApplication boolean to enhance backward API compatibility.
  * added: new ReportField.APPLICATION\_LOGFILE including the latest ReportsCrashes.applicationLogFileLines lines from the file ReportsCrashes.applicationLogFile. Allows to collect application specific log file data.
  * added: [issue 70](https://code.google.com/p/acra/issues/detail?id=70) - option excludeMatchingSharedPreferencesKeys to exclude sensible data from collected SharedPreferences.
  * added: [issue 80](https://code.google.com/p/acra/issues/detail?id=80) - option sendReportsInDevMode can now be set to false to disable sending reports in development mode. Reports will be sent only by signed applications.
  * fixed: [issue 60](https://code.google.com/p/acra/issues/detail?id=60) - ACRA crashes in NOTIFICATION mode without user comments.
  * added: logcatFilterByPid configuration item. When set to true, logcat field collects only lines related to the application process.
  * modified: [Issue 66](https://code.google.com/p/acra/issues/detail?id=66) - ReportsCrashes annotation is now @Inherited.
  * modified: handleException() method do not need to return the worker thread anymore
  * fixed: [issue 69](https://code.google.com/p/acra/issues/detail?id=69)
  * modified: reduced TOAST wait from 4s to 3s.
  * fixed: stop calling ACRA when ACRA throws an exception but let the native exception handler do its job. Should fix [Issue 72](https://code.google.com/p/acra/issues/detail?id=72).
  * fixed: don't call dumpsys meminfo after on OutOfMemroryError. It looks like it hangs the app.
  * modified: trying to clarify the logic of deleting/notifying reports on application start.
  * fixed: direct DIALOG mode is working again. The dialog creation has to be the last thing to do before killing the app.
  * fixed: the async wait for toast and sender thread termination was... crap. It is now fixed but the direct DIALOG does not work anymore. Must be a timing issue, which lets me think that this might not be a future proof solution.
  * fixed [Issue 84](https://code.google.com/p/acra/issues/detail?id=84): all email reports got "null crash report" as a title...
  * modified: removed the DROPBOX, RADIOLOG and EVENTSLOG fields from default report templates. They should be activated only by devs who really need them.
  * new: full dynamic configuration
  * modified: asynchronous wait for Toast display and sender thread termination. No more chance to cause ANR.
  * modified: handleException and handleSilentException do not return the sender Thread anymore.
  * New: direct DIALOG mode, thanks to Julia Segal.
  * modified: lowered the default number of logcat lines to 100
  * New: configuration of resources now possible via setters. This is to fix [Issue 85](https://code.google.com/p/acra/issues/detail?id=85) with ADT 14 from which we can't pass Android Library Projects resources ids to ACRA as they are not final anymore.
  * Overall code cleaning and refactoring (William Ferguson)

ACRA-4.2.3:
ACRA-4.2.3RC:
  * Code cleaning and better files handling => should fix most report duplicates issues. (William Ferguson)

ACRA-4.2.2RC:
  * Fixed: prevent persistent IS\_SILENT
  * Fixed: prevent some report duplicates
  * Fixed: prevent persistent USER\_COMMENT
  * Fixed: prevent ACRA Crash if getCrashReportFileList() is called without initializing ACRA
  * Fixed: handle 4xx and 5xx http response codes as errors => throw exception and keep report and retry later

ACRA-4.2.1b:
  * Fixed: NOTIFICATION + mailTo was broken

ACRA-4.2.0b:
  * New: Added a SHARED\_PREFERENCES field containing K/V pairs of the application default shared preferences + sharedPreferences provided by developer with @ReportsCrashes(additionalSharedPreferences={"my.own.prefs","a.second.prefs"})
  * Fixed: do not send silent reports if acra.enabled or acra.disabled SharedProperties have been set to disable ACRA.
  * Fixed: after application restart with ACRA disabled from SharedPreferences settings, ACRA was not able to detect a change on this setting and reactivate.

ACRA-4.1.0b:
  * [Issue 53](https://code.google.com/p/acra/issues/detail?id=53): new @ReportsCrashes option to let the developer choose to display the Force Close dialog after the Toast.

ACRA-4.0.1b:
  * Forced HttpClient SocketBufferSize to 8192 bytes as it appears that on some devices, the default size is set to a huge 2Mb.
  * Refactoring: extract constants from @ReportsCrashes. This was causing compilations issues with javac.
  * [Issue 52](https://code.google.com/p/acra/issues/detail?id=52): log substring was crashing when using http post with a response < 200 chars.

ACRA-4.0.0b:
  * Fixed: Prevent ACRA from sending report duplicates when sending silent reports in simultaneous threads.
  * New: Added a IS\_SILENT field.
  * New: socketTimeout configuration item in ReportsCrashes annotation.
  * Changed: Replaced URLConnection POST implementation with HttpClient HttpPost. Fixes timeouts with Basic Http Auth. Might fix https stability issues.
  * Changed: removed includeDropBox, includeEventsLog and includeRadioLog configuration items. This is now done by customizing reports content. Final default report template will not include these fields.
  * New: Added a SETTINGS\_SECURE field for http://developer.android.com/reference/android/provider/Settings.Secure.html content
  * New: Added a SETTINGS\_SYSTEM field http://developer.android.com/reference/android/provider/Settings.System.html content
  * New: Added an ENVIRONMENT field for state and details about external storage http://developer.android.com/intl/fr/reference/android/os/Environment.html
  * WontDo: PackageStats could be interesting but there is no public API to retrieve it.
  * New: Added an INSTALLATION\_ID field containing http://android-developers.blogspot.com/2011/03/identifying-app-installations.html
  * New: Added a REPORT\_ID field based on UUID.
  * New: All reports content can now be customized using ReportsCrashes.customReportContent. It replaces the mailReportFields that had been implemented only for mail reports before. This requires to create your own .csv and import it in GoogleCode.
  * Changed: Build data are now put together in a single column except for data about device model/manufacturer
  * Changed: Moved some report columns to put user related data together
  * New: [Issue 46](https://code.google.com/p/acra/issues/detail?id=46) - added an optional field for user email input in crash dialog. Retrieves and stores value from already implemented SharedPreferene. Field activation similar to user comment (provide a resourceId for its label).
  * New: Added a DEVICE\_FEATURES field http://developer.android.com/intl/fr/reference/android/content/pm/PackageManager.html#getSystemAvailableFeatures()
  * New: Added an USER\_APP\_START\_DATE field
  * New: Added an includeDropBoxSystemTags config option
  * New: added an includeDropBox configuration item with default value to false. This is to avoid any performance issue due to DropBox events collection for people who don't need them.
  * Changed: removed debugging log event when examining SharedPreference update.
  * New: ACRA project has been mavenized: building and releasing is now handled with maven. See [Releasing](Releasing.md) and [Maven](Maven.md) pages for details.
  * Fixed: [Issue 42](https://code.google.com/p/acra/issues/detail?id=42) - prevent ACRA 3.2 from crashing because of pending reports from previous ACRA versions.
  * Fixed: in NOTIFICATION mode, user comments could be attached to wrong reports.
  * Fixed: in SILENT mode, only explicit SILENT exceptions (cast with handleSilentException) were sent on application crash. Standard exception were sent on application restart.
  * Fixed: possible NPE when enabling/disabling ACRA with SharedPreferences.
  * New: Added SharedPreferences items for:
    * include system logs (may contain private data)
    * include Device ID (IMEI) (this a private identifier)
    * include user email (the user inputs his email address in the preference field)
    * always accept sending reports
  * Changed: default behavior in NOTIFICATION mode when notif has been ignored: on next application start do not respawn the notification and delete non approved reports. A config item has been added to enable the old behaviour back.
  * Changed: renamed some report field names which could be confusing (related to android build)
  * New: added a Toast before collecting crash data
  * New: added 2 fields in reports: application version code (some devs don't always update version name) and phone device ID (IMEI). The latest is retrieved only if permission.READ\_PHONE\_STATE is granted. This is useful in enterprise apps.
  * New: added a new configuration field for providing the set of fields which should be included in the mail body.
  * New: as requested in [Issue 29](https://code.google.com/p/acra/issues/detail?id=29), first draft of an Email report sender.
  * New: added 2 parameters (formUriBasicAuthLogin and formUriBasicAuthPassword) to allow BASIC http authentication when using the formUri parameter.
  * Fixed: [Issue 31](https://code.google.com/p/acra/issues/detail?id=31) - in some rare cases, Context.getFilesDir() can return null (this has been filed in android issues: http://code.google.com/p/android/issues/detail?id=8886). ACRA will now log a Warning and won't throw a NPE.
  * Replaced usage of Properties with a specific reimplementation based on Harmony Properties source code:
    * specialization for handling ReportField keys - this is to future exposition of crash report data to ReportSender development
    * get rid of the stupid date comment in store() which leads to 2 seconds wait
  * Created a ReportField enum to list all crash data fields. Will be used in ReportSenders
  * Changed: Added an abstraction layer for Report Senders.
  * New: Added a DumpSysCollector to retrieve the output of "adb shell dumpsys meminfo &lt;pid&gt;"
  * Changed: deleteNonSilentReports() changed to deleteNonApprovedReports(): now deletes reports which are neither silent nor approved.
  * New: added a new 'approved' status to reports which were approved by the user to be sent through the NOTIFICATION mode Dialog. This should allow to avoid issuing a new notification for reports which are still pending because network was not available.
  * Changed: rewrote a part of the reports sending logic to dissociate user comments storage from the global reports sending loop.
  * Changed: eliminated an old Bundle which was used to pass configuration items between various classes. Replaced this with a direct exposition of the ReportsCrashes configuration annotation. This should save some (not much) memory.
  * New: If the application is granted the READ\_LOGS permission, reports data include a logcat extract and DropBoxManager events (system tags + developer additional tags).

ACRA-3.1.2:
  * Fixed: use https instead of http for GoogleDocs form post

ACRA-3.1.1:
  * Fixed: [Issue 36](https://code.google.com/p/acra/issues/detail?id=36) - in SILENT mode, only explicit SILENT exceptions (cast with handleSilentException) were sent on application crash. Standard exception were sent on application restart.
  * Fixed: [Issue 34](https://code.google.com/p/acra/issues/detail?id=34) - possible NPE when enabling/disabling ACRA with SharedPreferences.
  * Fixed: [Issue 31](https://code.google.com/p/acra/issues/detail?id=31) - in some rare cases, Context.getFilesDir() can return null (this has been filed in android issues: http://code.google.com/p/android/issues/detail?id=8886). ACRA will now log a Warning and won't throw a NPE.

ACRA-3.1.0: (no changes from 3.1.0-RC1)

ACRA-3.1.0-RC1:
  * Changed: ([Issue 15](https://code.google.com/p/acra/issues/detail?id=15)) deprecated addCustomData(), use putCustomData() instead.
  * New: ([Issue 15](https://code.google.com/p/acra/issues/detail?id=15)) added getCustomData() and removeCustomData()
  * Fixed: associate user comment from dialog to the latest non silent report instead of latest report.
  * New: ([Issue 20](https://code.google.com/p/acra/issues/detail?id=20)) added methods to delete pending silent or non silent reports.
  * Fixed: handleSilentException() was sending all pending reports. Now sends only pending silent reports. ([Issue 16](https://code.google.com/p/acra/issues/detail?id=16))
  * Fixed: when several pending reports are present with both silent and non-silent reports, silent reports were sent first. Now send in chronological order.
  * Fixed: ACRA was logging errors in logcat some specifically caught exceptions which are caused on purpose by ACRA for some android backward compatibility tests.
  * Fixed performance issues ([Issue 19](https://code.google.com/p/acra/issues/detail?id=19)) in report data collection for 1.X android devices (change of Properties storage method only for these android versions). Overall Report data collection time lowered from more than 4 seconds (8 for 1.X devices) to 1-2 seconds (emulator times).
  * Fixed: unwanted Toast notification on application start for Silent reports

ACRA-3.0.0
  * Fixed an issue on the SharedPreferences listener which could lead to user preferences changes for reports activation state not being applied before a full application restart.
  * Fixed [Issue 10](https://code.google.com/p/acra/issues/detail?id=10) : sending a report with handleException() or handleSilentException(), the report was sent on the UI Thread. This could lead to application hanging if network conditions were bad.
  * Addition of 5 fields in the reports:
    * initial\_config: the Configuration on application start
    * crash\_config: the Configuration when the application crashed
    * display: display specs
    * user\_comment: user comment has now it's own field and is not added in the dev custom data anymore
    * user\_crash\_date: the user exact local date/time of the crash (the google doc timestamp is the report reception time, but the transmission might have been delayed from the real crash time)
  * ACRA Configuration is now based on a @ReportsCrashes annotation and not on extending CrashReportingApplication anymore. See [@ReportsCrashes javadoc](http://acra.googlecode.com/svn/trunk/CrashReport/javadoc/org/acra/annotation/ReportsCrashes.html) for configuration details.

ACRA-2.1.0
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