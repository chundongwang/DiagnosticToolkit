
package com.microsoft.projecta.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.common.AdbHelper;
import com.microsoft.projecta.tools.common.ExecuteException;
import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public class ApkKiller extends WorkFlowStage {
    private static Logger logger = Logger.getLogger(ApkKiller.class.getSimpleName());
    // need the trailing '/' at the end
    private static String ANDROID_LOG_DIR = "/sdcard/diag/log/";
    private static String LOGFILE_TEMPLATE = "kill-%s.log";
    private LaunchConfig mConfig;
    private AdbHelper mAdbHelper;

    // progress, 0-100, and 0/100 are covered by parent class already
    private static final int PROGRESS_LOGCAT_CLEANED = 10;
    private static final int PROGRESS_ADB_KILL_STARTED = 50;

    public ApkKiller(LaunchConfig config) {
        super(logger.getName(), "adb process to kill the apk");
        mConfig = config;
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.KILL_APP;
    }

    /**
     * Clear logcat before launch
     */
    @Override
    protected boolean setup() {
        try {
            // even if logcat -c failed, we should still probably keep going
            mAdbHelper = AdbHelper.getInstance(mConfig.getUnzippedSdkToolsPath(),
                    mConfig.getOutdirPath());

            if (mAdbHelper != null) {
                // clear logcat before launch
                mAdbHelper.logcat("-c");
                fireOnProgress(PROGRESS_LOGCAT_CLEANED);
                fireOnLogOutput("Logcat cleared before killing the app.");
            }
        } catch (InterruptedException | IOException e) {
            fireOnLogOutput(logger, Level.SEVERE,
                    "Error occured while parsing AndroidManifest.xml and/or clearing logcat", e);
        } catch (ExecuteException e) {
            fireOnLogOutput(logger, Level.SEVERE,
                    "Error occured while running adb to clear logcat ", e);
        }
        // even if logcat -c failed, we should still probably keep going
        return mAdbHelper != null;
    }

    /**
     * dump logcat after launch
     */
    @Override
    protected void cleanup() {
        String apk_name = mConfig.getApkName();
        try {
            // prepare folders on both end
            Path logPath = Files.createDirectories(path(mConfig.getLogsDir()));
            mAdbHelper.shell("mkdir", "-p", ANDROID_LOG_DIR);
            // dump logcat to remote folder
            mAdbHelper.logcat("-d", "-v", "time", "-f",
                    ANDROID_LOG_DIR + String.format(LOGFILE_TEMPLATE, apk_name));
            // pull dump file to local folder
            mAdbHelper.pull(ANDROID_LOG_DIR + String.format(LOGFILE_TEMPLATE, apk_name), logPath
                    .resolve(String.format(LOGFILE_TEMPLATE, apk_name)).toString());
        } catch (InterruptedException | IOException e) {
            fireOnLogOutput(logger, Level.SEVERE, "Error occured while clearing logcat for "
                    + apk_name, e);
        } catch (ExecuteException e) {
            fireOnLogOutput(logger, Level.SEVERE,
                    "Error occured while running adb for " + apk_name, e);
        }
    }

    /**
     * adb shell am kill <package_name>
     */
    @Override
    protected ProcessBuilder startWorkerProcess() {
        fireOnLogOutput("About to adb shell am kill " + mConfig.getApkPackageName());
        fireOnProgress(PROGRESS_ADB_KILL_STARTED);
        return new ProcessBuilder().command(
                mAdbHelper.getAdbPath().toString(),
                "shell", "am", "kill", mConfig.getApkPackageName()).directory(
                new File(mConfig.getOutdirPath()));
    }

}
