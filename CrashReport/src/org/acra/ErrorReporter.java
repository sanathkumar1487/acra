/*
 *  Copyright 2010 Emmanuel Astier & Kevin Gaudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.acra;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.acra.CrashReportingApplication.ReportingInteractionMode;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.util.Log;
import android.widget.Toast;

/**
 * <p>
 * The ErrorReporter is a Singleton object in charge of collecting crash context
 * data and sending crash reports. It registers itself as the Application's
 * Thread default {@link UncaughtExceptionHandler}.
 * </p>
 * <p>
 * When a crash occurs, it collects data of the crash context (device, system,
 * stack trace...) and immediately tries to send it. If an error occurs while
 * sending a report, this report is stored in the application private file
 * system for later send attempt using {@link #checkAndSendReports(Context)}.
 * </p>
 */
public class ErrorReporter implements Thread.UncaughtExceptionHandler {
    final class ReportsSenderWorker extends Thread {
        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            checkAndSendReports(mContext);
        }
    }

    private static final String LOG_TAG = CrashReportingApplication.LOG_TAG;

    /**
     * This is the number of previously stored reports that we send in
     * {@link #checkAndSendReports(Context)}. The number of reports is limited
     * to avoid ANR on application start.
     */
    private static final int MAX_SEND_REPORTS = 5;

    // These are the fields names in the POST HTTP request sent to
    // the GoogleDocs form. Any change made on the structure of the form
    // will need a mapping check of these constants.
    private static final String VERSION_NAME_KEY = "entry.0.single";
    private static final String PACKAGE_NAME_KEY = "entry.1.single";
    private static final String FILE_PATH_KEY = "entry.2.single";
    private static final String PHONE_MODEL_KEY = "entry.3.single";
    private static final String ANDROID_VERSION_KEY = "entry.4.single";
    private static final String BOARD_KEY = "entry.5.single";
    private static final String BRAND_KEY = "entry.6.single";
    private static final String DEVICE_KEY = "entry.7.single";
    private static final String DISPLAY_KEY = "entry.8.single";
    private static final String FINGERPRINT_KEY = "entry.9.single";
    private static final String HOST_KEY = "entry.10.single";
    private static final String ID_KEY = "entry.11.single";
    private static final String MODEL_KEY = "entry.12.single";
    private static final String PRODUCT_KEY = "entry.13.single";
    private static final String TAGS_KEY = "entry.14.single";
    private static final String TIME_KEY = "entry.15.single";
    private static final String TYPE_KEY = "entry.16.single";
    private static final String USER_KEY = "entry.17.single";
    private static final String TOTAL_MEM_SIZE_KEY = "entry.18.single";
    private static final String AVAILABLE_MEM_SIZE_KEY = "entry.19.single";
    private static final String CUSTOM_DATA_KEY = "entry.20.single";
    private static final String STACK_TRACE_KEY = "entry.21.single";

    static final String USER_COMMENT_KEY = "user.comment";

    // This is where we collect crash data
    private Properties mCrashProperties = new Properties();

    // Some custom parameters can be added by the application developer. These
    // parameters are stored here.
    Map<String, String> mCustomParameters = new HashMap<String, String>();

    // A refernce to the system's previous default UncaughtExceptionHandler
    // kept in order to execute the defaut exception handling after sending
    // the report.
    private Thread.UncaughtExceptionHandler mDfltExceptionHandler;

    // Our singleton instance.
    private static ErrorReporter mInstanceSingleton;

    // The application context
    private Context mContext;

    // The text displayed in the report Toast message.
    private CharSequence mToastText = "Sending crash report...";

    private ReportingInteractionMode mReportingInteractionMode = ReportingInteractionMode.SILENT;

    // The Url we have to post the reports to.
    private static Uri mFormUri;

    /**
     * Use this method to provide the Url of the crash reports destination.
     * 
     * @param formUri
     *            The Url of the crash reports destination (HTTP POST).
     */
    public void setFormUri(Uri formUri) {
        mFormUri = formUri;
    }

    /**
     * <p>
     * Use this method to provide the ErrorReporter with data of your running
     * application. You should call this at several key places in your code the
     * same way as you would output important debug data in a log file. Only the
     * latest value is kept for each key (no history of the values is sent in
     * the report).
     * </p>
     * <p>
     * The key/value pairs will be stored in the GoogleDoc spreadsheet in the
     * "custom" column, as a text containing a 'key = value' pair on each line.
     * </p>
     * 
     * @param key
     *            A key for your custom data.
     * @param value
     *            The value associated to your key.
     */
    public void addCustomData(String key, String value) {
        mCustomParameters.put(key, value);
    }

    /**
     * Generates the string which is posted in the single custom data field in
     * the GoogleDocs Form.
     * 
     * @return A string with a 'key = value' pair on each line.
     */
    private String createCustomInfoString() {
        String CustomInfo = "";
        Iterator<String> iterator = mCustomParameters.keySet().iterator();
        while (iterator.hasNext()) {
            String CurrentKey = iterator.next();
            String CurrentVal = mCustomParameters.get(CurrentKey);
            CustomInfo += CurrentKey + " = " + CurrentVal + "\n";
        }
        return CustomInfo;
    }

    /**
     * Create or return the singleton instance.
     * 
     * @return the current instance of ErrorReporter.
     */
    public static ErrorReporter getInstance() {
        if (mInstanceSingleton == null) {
            mInstanceSingleton = new ErrorReporter();
        }
        return mInstanceSingleton;
    }

    /**
     * <p>
     * This is where the ErrorReporter replaces the default
     * {@link UncaughtExceptionHandler}.
     * </p>
     * 
     * @param context
     *            The android application context.
     */
    public void init(Context context) {
        mDfltExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = context;
    }

    /**
     * Calculates the free memory of the device. This is based on an inspection
     * of the filesystem, which in android devices is stored in RAM.
     * 
     * @return Number of bytes available.
     */
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * Calculates the total memory of the device. This is based on an inspection
     * of the filesystem, which in android devices is stored in RAM.
     * 
     * @return Total number of bytes.
     */
    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * Collects crash data.
     * 
     * @param context
     *            The application context.
     */
    private void retrieveCrashData(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi;
            // Version
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            mCrashProperties.put(VERSION_NAME_KEY, pi.versionName);
            // Package name
            mCrashProperties.put(PACKAGE_NAME_KEY, pi.packageName);
            // Device model
            mCrashProperties.put(PHONE_MODEL_KEY, android.os.Build.MODEL);
            // Android version
            mCrashProperties.put(ANDROID_VERSION_KEY,
                    android.os.Build.VERSION.RELEASE);

            // Android build data
            mCrashProperties.put(BOARD_KEY, android.os.Build.BOARD);
            mCrashProperties.put(BRAND_KEY, android.os.Build.BRAND);
            mCrashProperties.put(DEVICE_KEY, android.os.Build.DEVICE);
            mCrashProperties.put(DISPLAY_KEY, android.os.Build.DISPLAY);
            mCrashProperties.put(FINGERPRINT_KEY, android.os.Build.FINGERPRINT);
            mCrashProperties.put(HOST_KEY, android.os.Build.HOST);
            mCrashProperties.put(ID_KEY, android.os.Build.ID);
            mCrashProperties.put(MODEL_KEY, android.os.Build.MODEL);
            mCrashProperties.put(PRODUCT_KEY, android.os.Build.PRODUCT);
            mCrashProperties.put(TAGS_KEY, android.os.Build.TAGS);
            mCrashProperties.put(TIME_KEY, "" + android.os.Build.TIME);
            mCrashProperties.put(TYPE_KEY, android.os.Build.TYPE);
            mCrashProperties.put(USER_KEY, android.os.Build.USER);

            // Device Memory
            mCrashProperties.put(TOTAL_MEM_SIZE_KEY, ""
                    + getTotalInternalMemorySize());
            mCrashProperties.put(AVAILABLE_MEM_SIZE_KEY, ""
                    + getAvailableInternalMemorySize());

            // Application file path
            mCrashProperties.put(FILE_PATH_KEY, context.getFilesDir()
                    .getAbsolutePath());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error while retrieving crash data", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang
     * .Thread, java.lang.Throwable)
     */
    public void uncaughtException(Thread t, Throwable e) {
        // Generate crash report
        handleException(e);

        if (mReportingInteractionMode == ReportingInteractionMode.TOAST) {
            try {
                // Wait a bit to let the user read the toast
                Thread.sleep(4000);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                Log.e(LOG_TAG, "Error : ", e1);
            }
        }

        if (mReportingInteractionMode == ReportingInteractionMode.SILENT) {
            // If using silent mode, let the system default handler do it's job
            // and display the force close dialog.
            mDfltExceptionHandler.uncaughtException(t, e);
        } else {
            // If ACRA handles user notifications whit a Toast or a Notification
            // the Force Close dialog is one more notification to the user...
            // We choose to close the process ourselves using the same actions.
            CharSequence appName = "Application";
            try {
                PackageManager pm = mContext.getPackageManager();
                appName = pm.getApplicationInfo(mContext.getPackageName(), 0)
                        .loadLabel(mContext.getPackageManager());
                Log.e(LOG_TAG, appName + " fatal error : " + e.getMessage(), e);
            } catch (NameNotFoundException e2) {
                Log.e(LOG_TAG, "Error : ", e2);
            } finally {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(10);
            }
        }
    }

    /**
     * Try to send a report, if an error occurs stores a report file for a later
     * attempt.
     * 
     * @param e
     *            The exception to be sent.
     */
    public void handleException(Throwable e) {
        if (e == null) {
            e = new Exception("Report requested by developer");
        }
        if (mReportingInteractionMode == ReportingInteractionMode.TOAST) {
            new Thread() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see java.lang.Thread#run()
                 */
                @Override
                public void run() {
                    Looper.prepare();
                    Toast.makeText(mContext, mToastText, Toast.LENGTH_LONG)
                            .show();
                    Looper.loop();
                }

            }.start();
        }
        retrieveCrashData(mContext);
        // TODO: add a field in the googledoc form for the crash date.
        // Date CurDate = new Date();
        // Report += "Error Report collected on : " + CurDate.toString();

        // Add custom info, they are all stored in a single field
        mCrashProperties.put(CUSTOM_DATA_KEY, createCustomInfoString());

        // Build stack trace
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        Throwable cause = e.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        mCrashProperties.put(STACK_TRACE_KEY, result.toString());
        printWriter.close();

        // Always write the report file
        saveCrashReportFile();

        if (mReportingInteractionMode == ReportingInteractionMode.SILENT
                || mReportingInteractionMode == ReportingInteractionMode.TOAST) {
            checkAndSendReports(mContext);
        } else if (mReportingInteractionMode == ReportingInteractionMode.NOTIFICATION) {
            notifySendReport();
        }

    }

    /**
     * 
     */
    void notifySendReport() {
        // This notification can't be set to AUTO_CANCEL because after a crash,
        // clicking on it restarts the application and this triggers a check
        // for pending reports which issues the notification back.
        // Notification cancellation is done in the dialog activity displayed
        // on notification click.
        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        int icon = android.R.drawable.stat_notify_error;
        CharSequence tickerText = "Unexpected error, please send report.";
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, tickerText, when);
        PackageManager pm = mContext.getPackageManager();
        try {
            CharSequence appName = pm.getApplicationInfo(
                    mContext.getPackageName(), 0).loadLabel(
                    mContext.getPackageManager());
            CharSequence contentTitle = appName + " has crashed...";
            CharSequence contentText = "Please click here to help fix the issue.";
            Intent notificationIntent = new Intent(mContext,
                    CrashReportDialog.class);
            PendingIntent contentIntent = PendingIntent.getActivity(mContext,
                    0, notificationIntent, 0);
            notification.setLatestEventInfo(mContext, contentTitle,
                    contentText, contentIntent);
            notificationManager.notify(
                    CrashReportingApplication.NOTIF_CRASH_ID, notification);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            Log.e(LOG_TAG, "Error : ", e);
        }
    }

    /**
     * Sends the report in an HTTP POST to a GoogleDocs Form
     * 
     * @param context
     *            The application context.
     * @param errorContent
     *            Crash data.
     * @throws IOException
     *             If unable to send the crash report.
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     *             Might be thrown if sending over https.
     * @throws KeyManagementException
     *             Might be thrown if sending over https.
     */
    private static void sendCrashReport(Context context, Properties errorContent)
            throws UnsupportedEncodingException, IOException,
            KeyManagementException, NoSuchAlgorithmException {
        errorContent.put("pageNumber", "0");
        errorContent.put("backupCache", "");
        errorContent.put("submit", "Envoyer");

        URL reportUrl = new URL(mFormUri.toString());
        Log.d(LOG_TAG, "Connect to " + reportUrl.toString());
        HttpUtils.doPost(errorContent, reportUrl);
    }

    /**
     * When a report can't be sent, it is saved here in a file in the root of
     * the application private directory.
     */
    private void saveCrashReportFile() {
        try {
            Log.d(LOG_TAG, "Writing crash report file.");
            long timestamp = System.currentTimeMillis();
            String FileName = "stack-" + timestamp + ".stacktrace";
            FileOutputStream trace = mContext.openFileOutput(FileName,
                    Context.MODE_PRIVATE);
            mCrashProperties.store(trace, "");
            trace.flush();
            trace.close();
        } catch (Exception e) {
            Log.e(LOG_TAG, "An error occured while writing the report file...",
                    e);
        }
    }

    /**
     * Returns an array containing the names of available crash report files.
     * 
     * @return an array containing the names of available crash report files.
     */
    String[] getCrashReportFilesList() {
        File dir = mContext.getFilesDir();

        Log.d(LOG_TAG, "Looking for error files in " + dir.getAbsolutePath());

        // Filter for ".stacktrace" files
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".stacktrace");
            }
        };
        return dir.list(filter);
    }

    /**
     * <p>
     * You can call this method in your main {@link Activity} onCreate() method
     * in order to check if previously unsent crashes occured and immediately
     * send them.
     * </p>
     * <p>
     * This is called by default in any Application extending
     * {@link CrashReportingApplication}.
     * </p>
     * 
     * @param context
     *            The application context.
     */
    void checkAndSendReports(Context context) {
        try {

            String[] reportFilesList = getCrashReportFilesList();
            TreeSet<String> sortedFiles = new TreeSet<String>();
            sortedFiles.addAll(Arrays.asList(reportFilesList));
            if (reportFilesList != null && reportFilesList.length > 0) {
                Properties previousCrashReport = new Properties();
                // send only a few reports to avoid ANR
                int curIndex = 0;
                for (String curString : sortedFiles) {
                    if (curIndex < MAX_SEND_REPORTS) {
                        FileInputStream input = context
                                .openFileInput(curString);
                        previousCrashReport.load(input);
                        input.close();
                        // Insert the optional user comment written in
                        // CrashReportDialog, only on the latest report file
                        if (curIndex == sortedFiles.size() - 1
                                && mCustomParameters
                                        .containsKey(USER_COMMENT_KEY)) {
                            String custom = previousCrashReport
                                    .getProperty(CUSTOM_DATA_KEY);
                            if (custom == null) {
                                custom = "";
                            } else {
                                custom += "\n";
                            }
                            previousCrashReport.put(CUSTOM_DATA_KEY, custom
                                    + USER_COMMENT_KEY + " = "
                                    + mCustomParameters.get(USER_COMMENT_KEY));
                            mCustomParameters.remove(USER_COMMENT_KEY);

                        }
                        sendCrashReport(context, previousCrashReport);

                        // DELETE FILES !!!!
                        File curFile = new File(context.getFilesDir(),
                                curString);
                        curFile.delete();
                    }
                    curIndex++;
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Get rid of any user comment which would not be relevant anymore
            mCustomParameters.remove(USER_COMMENT_KEY);
        }
    }

    /**
     * If this method is called providing a non empty CharSequence, enables the
     * display of a dialog asking the user to confirm that he wants to send a
     * crash report.
     * 
     * @param text
     *            The question asked to the user.
     */
    void setToastText(CharSequence text) {
        if (text != null && !"".equals(text.toString().trim())) {
            mToastText = text;
        }
    }

    void setReportingInteractionMode(
            ReportingInteractionMode reportingInteractionMode) {
        mReportingInteractionMode = reportingInteractionMode;
    }

    void checkReportsOnApplicationStart() {
        String[] filesList = getCrashReportFilesList();
        if (filesList != null && filesList.length > 0) {
            if (mReportingInteractionMode == ReportingInteractionMode.SILENT
                    || mReportingInteractionMode == ReportingInteractionMode.TOAST) {
                if (mReportingInteractionMode == ReportingInteractionMode.TOAST) {
                    Toast.makeText(mContext, mToastText, Toast.LENGTH_LONG)
                            .show();
                }
                new ReportsSenderWorker().start();
            } else if (mReportingInteractionMode == ReportingInteractionMode.NOTIFICATION) {
                ErrorReporter.getInstance().notifySendReport();
            }
        }

    }

    public void deletePendingReports() {
        String[] filesList = getCrashReportFilesList();
        if (filesList != null) {
            for (String fileName : filesList) {
                new File(mContext.getFilesDir(), fileName).delete();
            }
        }
    }
}