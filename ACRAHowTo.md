

# This document is deprecated. It is related to ACRA v2.0.3 which is not supported anymore. Please use [ACRA v4.X](BasicSetup.md). #

# Introduction #

ACRA allows your Android application to send Crash Reports in a Google Docs spreadsheet.
This tutorial will guide you in installing ACRA in your application project.

# Setting-up your project #

Step by step installation of the ACRA library in an existing application project:
  * Get http://acra.googlecode.com/files/acra-2.0.3.zip and open the archive
  * Login to your Google Docs account
  * Import the `CrashReports-template.csv` contained in the archive (acra-2.0.3/CrashReport/doc)
  * Open the imported document
  * Rename it as you like
  * In the menu, click on Forms / Create form
  * Add anything in the form description just to enable the **Save** button
  * Save the form
  * Copy the `formId` displayed in the link at the bottom of the form creation page
  * Open your Eclipse project
  * Create a `lib` folder
  * Add the `acra-2.0.3.jar` from the archive (acra-2.0.3/CrashReport/build) in the `lib` folder
  * Right-click on the jar file / add to build path
  * Create a new class in your package root
    * Give it a name like: `MyApplication`, make it extend `org.acra.CrashReportingApplication`
    * Override the abstract `getFormId()` method, returning a simple String containing the form Id of your Google Docs form:

```
    @Override
    public String getFormId() {
        return "dGVacG0ydVHnaNHjRjVTUTEtb3FPWGc6MQ";
    }
```

  * Open the android manifest editor (`AndroidManifest.xml`
    * In the **Application tab**, click on the **Browse** button next to the **Name** field
    * Select your newly created Application class (`MyApplication`).
> > This adds an `android:name` attribute to your `application` element like this (put the full name with package if the application class package is not the same as manifest root element declared pakage):
```
<application android:icon="@drawable/icon" android:label="@string/app_name"
                android:name="MyApplication">
```
    * In the **Permissions** tab, add a **Uses Permission** object with value `android.permission.INTERNET`.
> > This adds the following element as a child of the `manifest` element:
```
<uses-permission android:name="android.permission.INTERNET"></uses-permission>
```
  * **_THE END_** - next time your application crashes, it adds a line to your Google Docs spreadsheet :-).

**Bonus :** Google Docs spreadsheet allow to configure notifications on changes. Juste set your preferences in  : Share (Top right button) / Set notification rules and get notified by mail when reports are sent !

# Advanced Usage #

## User notification ##

The default behaviour of ACRA is to send crash reports silently. From the application user point of view, the application has crashed with the usual "Force Close" dialog, and that's all.

As a developer, you might prefer notifying your users that a crash report has been sent, or even ask him the authorization so send one... and why not ask him to describe what he was doing during the crash...

ACRA offers all these options, and allow you to customize your application crash reporting notifications.

2 main notification modes are available:
  * display a simple Toast with your developer designed text
![http://acra.googlecode.com/svn/wiki/files/captures/notif_toast.png](http://acra.googlecode.com/svn/wiki/files/captures/notif_toast.png)
  * display a status bar notification, then offering the user a dialog asking him to send the report or not, with an optional comment field you can chose to add or not.
![http://acra.googlecode.com/svn/wiki/files/captures/notif_ticker.png](http://acra.googlecode.com/svn/wiki/files/captures/notif_ticker.png) ![http://acra.googlecode.com/svn/wiki/files/captures/notif.png](http://acra.googlecode.com/svn/wiki/files/captures/notif.png) ![http://acra.googlecode.com/svn/wiki/files/captures/notif_dialog_full.png](http://acra.googlecode.com/svn/wiki/files/captures/notif_dialog_full.png)

Enabling user notification only requires you to override one method in your Application class (where you provide your formId) :

  * Toast notification:

```
@Override
public Bundle getCrashResources() {
    Bundle result = new Bundle();
    result.putInt(RES_TOAST_TEXT, R.string.crash_toast_text);
    return result;
}
```

In your `strings.xml` :
```
<string name="crash_toast_text">Ooooops ! I crashed, but a report has been sent to my developer to help him fix the issue !</string>
```

  * Status bar notification:

```
@Override
public Bundle getCrashResources() {
    Bundle result = new Bundle();
    result.putInt(RES_NOTIF_TICKER_TEXT, R.string.crash_notif_ticker_text);
    result.putInt(RES_NOTIF_TITLE, R.string.crash_notif_title);
    result.putInt(RES_NOTIF_TEXT, R.string.crash_notif_text);
    result.putInt(RES_NOTIF_ICON, android.R.drawable.stat_notify_error); // optional. default is a warning sign
    result.putInt(RES_DIALOG_TEXT, R.string.crash_dialog_text);
    result.putInt(RES_DIALOG_ICON, android.R.drawable.ic_dialog_info); //optional. default is a warning sign
    result.putInt(RES_DIALOG_TITLE, R.string.crash_dialog_title); // optional. default is your application name 
    result.putInt(RES_DIALOG_COMMENT_PROMPT, R.string.crash_dialog_comment_prompt); // optional. when defined, adds a user text field input with this text resource as a label
    result.putInt(RES_DIALOG_OK_TOAST, R.string.crash_dialog_ok_toast); // optional. Displays a Toast when the user accepts to send a report ("Thank you !" for example)
    return result;
}
```

In your `strings.xml`:
```
<string name="crash_notif_ticker_text">Unexpected error, please send a report...</string>
<string name="crash_notif_title">CrashTest has crashed...</string>
<string name="crash_notif_text">Please click here to help fix the issue.</string>
	
<string name="crash_dialog_title">CrashTest has crashed</string>
<string name="crash_dialog_text">An unexpected error occurred forcing the
	application to stop. Please help us fix this by sending us error data,
	all you have to do is click \'OK\'.</string>
<string name="crash_dialog_comment_prompt">You might add your comments about the problem below:</string>
<string name="crash_dialog_ok_toast">Thank you !</string>
```

In your `AndroidManifest.xml`
```
<application ...>

    ....

    <activity android:name="org.acra.CrashReportDialog"
        android:theme="@android:style/Theme.Dialog"
        android:launchMode="singleInstance"
        android:excludeFromRecents="true"
        android:finishOnTaskLaunch="true" />

    ....

</application>
```

## May I send reports to my own php/java/python/whateveryouwant self-hosted script ? ##

Just override the getFormUri() in your Application class extending CrashReportingApplication :

```
public Uri getFormUri() {
    return Uri.parse("http://myserver.com/myscript");
}
```

Then your script has to follow the fields mapping exposed in the [ErrorReporter](http://code.google.com/p/acra/source/browse/trunk/CrashReport/src/org/acra/ErrorReporter.java#70) class.

## Can I add my own variables content in the crash report ? ##

Absolutely !

Simply use the following method in key places in your code :
```
ErrorReporter.getInstance().addCustomData("myVariable", myVariable);
```

All your custom data (only latest value for each one) will be added in the column "custom" just before the stack trace, one key = value pair per line.

## Can I let the user disable error reporting ? ##

Yes, you can !

All you have to do is add to your preferences xml file a CheckBoxPreference :
```
<CheckBoxPreference android:key="acra.disable"
	android:title="@string/pref_disable_acra"
	android:summaryOn="@string/pref_acra_disabled"
	android:summaryOff="@string/pref_acra_enabled"
	android:defaultValue="false"/>
```

Then add to your strings.xml files the 3 corresponding string resources.

## Can I send reports for caught exceptions ? or for unexpected application state without any exception ? ##

As a good programmer, your code is full of try/catch statements, and sometimes an interesting (unexpected) exception might be caught in one of these.

You could also want your application to send a report without any Exception thrown, just because you know that your application is in an unexpected state.

Both of these needs can be covered by this :

```
ErrorReporter.getInstance().handleException(caughtException);
```

You can provide any caught or custom Exception, or even `null` if you don't have any to provide.

If you need to add silent trace reports whatever notification mode you configured for your application, you can also use:

```
ErrorReporter.getInstance().handleSilentException(caughtException);
```