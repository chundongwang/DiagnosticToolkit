
package com.microsoft.projecta.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.common.AdbException;
import com.microsoft.projecta.tools.common.AdbHelper;
import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.workflow.WorkFlowSingleProcStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public class ApkMainLauncher extends WorkFlowSingleProcStage {
    private static Logger logger = Logger.getLogger(ApkMainLauncher.class.getSimpleName());
    // need the trailing '/' at the end
    private static String ANDROID_LOG_DIR = "/sdcard/diag/log/";
    private static String LOGFILE_TEMPLATE = "launch-%s.log";
    private LaunchConfig mConfig;
    private AdbHelper mAdbHelper;

    public ApkMainLauncher(LaunchConfig config) {
        super(logger.getName(), "adb process to launch the activity");
        mConfig = config;
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.LAUNCH_SUCCESS;
    }

    /**
     * Clear logcat before launch
     */
    @Override
    protected boolean setup() {
        boolean result = false;
        try {
            // even if logcat -c failed, we should still probably keep going
            mAdbHelper = AdbHelper.getInstance(mConfig.getUnzippedSdkToolsPath(),
                    mConfig.getOutdirPath());
            result = true;

            // clear logcat before launch
            mAdbHelper.logcat("-c");
        } catch (InterruptedException | IOException e) {
            fireOnLogOutput(logger, Level.SEVERE,
                    "Error occured while parsing AndroidManifest.xml and/or clearing logcat", e);
        } catch (AdbException e) {
            fireOnLogOutput(logger, Level.SEVERE,
                    "Error occured while running adb to clear logcat ", e);
        }
        return result;
    }

    /**
     * dump logcat after launch
     */
    @Override
    protected void cleanup() {
        String apk_name = mConfig.getApkName();
        try {
            // prepare folders on both end
            Path logPath = Files.createDirectories(path(mConfig.getOutdirPath(), "logs"));
            mAdbHelper.shell("mkdir", "-p", ANDROID_LOG_DIR);
            // dump logcat to remote folder
            mAdbHelper.logcat("-d", "-v", "time", "-f",
                    ANDROID_LOG_DIR + String.format(LOGFILE_TEMPLATE, apk_name));
            // pull dump file to local folder
            mAdbHelper.exec("pull", ANDROID_LOG_DIR + String.format(LOGFILE_TEMPLATE, apk_name),
                    logPath.resolve(String.format(LOGFILE_TEMPLATE, apk_name)).toString());
        } catch (InterruptedException | IOException e) {
            fireOnLogOutput(logger, Level.SEVERE, "Error occured while clearing logcat for "
                    + apk_name, e);
        } catch (AdbException e) {
            fireOnLogOutput(logger, Level.SEVERE,
                    "Error occured while running adb for " + apk_name, e);
        }
    }

    /**
     * Do some pre-check
     */
    @Override
    public void execute() {
        super.execute();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // Cancelled by user
            fireOnLogOutput("Interupted while waiting after executed " + getWorkerProcDesc());
        }
    }

    /**
     * adb shell am start <INTENT> <INTENT> = -a <ACTION> -c <CATEGORY> -n <COMPONENT> For example
     * <INTENT> = -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n
     * com.kokteyl.goal/com.kokteyl.content.Splash
     */
    @Override
    protected ProcessBuilder startWorkerProcess() throws IOException {
        return new ProcessBuilder().command(
                join(mConfig.getUnzippedSdkToolsPath(), "SDK_19.1.0", "platform-tools", "adb.exe"),
                "shell", "am", "start", "-a", "android.intent.action.MAIN", "-c",
                "android.intent.category.LAUNCHER", "-n",
                mConfig.getApkPackageName() + "/" + mConfig.getActivityToLaunch()).directory(
                new File(mConfig.getOutdirPath()));
    }

}
