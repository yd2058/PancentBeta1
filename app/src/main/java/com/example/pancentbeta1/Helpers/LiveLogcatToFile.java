package com.example.pancentbeta1.Helpers;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The type Live logcat to file.
 */
public class LiveLogcatToFile {

    private static final String LOG_TAG = "LiveLogcatToFile";
    private static Process logcatProcess;
    private static File logFile;
    private static FileOutputStream fileOutputStream;
    private static final String LOGCAT_COMMAND = "logcat";

    /**
     * Start logging.
     *
     * @param context the context
     */
    public static void startLogging(Context context) {
        // Check if a logging session is already in progress
        if (logcatProcess != null) {
            Log.w(LOG_TAG, "Logging is already in progress.");
            return;
        }

        try {
            // Create a log file in the internal storage
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String filename = "live_logcat_" + timestamp + ".txt";
            logFile = new File(context.getFilesDir(), filename); // Use getFilesDir() for internal storage

            fileOutputStream = new FileOutputStream(logFile, true); // Append mode

            // Start the logcat process
            logcatProcess = Runtime.getRuntime().exec(LOGCAT_COMMAND);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(logcatProcess.getInputStream()));

            // Create a thread to read from the logcat process and write to the file
            Thread logcatThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            // Write the line to the file
                            fileOutputStream.write((line + "\n").getBytes());
                        }
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error reading or writing logcat output: " + e.getMessage());
                    } finally {
                        try {
                            reader.close();
                            fileOutputStream.close();
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "Error closing logcat stream: " + e.getMessage());
                        }
                    }
                }
            });
            logcatThread.start();
            Log.i(LOG_TAG, "Live Logcat logging started to: " + logFile.getAbsolutePath());

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error starting logcat process: " + e.getMessage());
            stopLogging();
        }
    }

    /**
     * Stop logging.
     */
    public static void stopLogging() {
        if (logcatProcess != null) {
            logcatProcess.destroy();
            logcatProcess = null;
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                    fileOutputStream = null;
                }
                Log.i(LOG_TAG, "Live Logcat logging stopped.");
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error closing log file: " + e.getMessage());
            }
        }
    }



}