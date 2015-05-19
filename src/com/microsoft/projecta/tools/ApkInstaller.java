
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

public final class ApkInstaller extends WorkFlowStage {
    private static Logger logger = Logger.getLogger(ApkInstaller.class.getSimpleName());
    // need the trailing '/' at the end
    private static String ANDROID_LOG_DIR = "/sdcard/diag/log/";
    private static String LOGFILE_TEMPLATE = "install-%s.log";
    private LaunchConfig mConfig;
    private AdbHelper mAdbHelper;

    public ApkInstaller(LaunchConfig config) {
        super(logger.getName(), "adb process to install apk");
        mConfig = config;
    }

    /**
     * dump logcat after install
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
            mAdbHelper.pull(ANDROID_LOG_DIR + String.format(LOGFILE_TEMPLATE, apk_name),
                    logPath.resolve(String.format(LOGFILE_TEMPLATE, apk_name)).toString());
        } catch (InterruptedException | IOException e) {
            fireOnLogOutput(logger, Level.SEVERE, "Error occured while clearing logcat for "
                    + apk_name, e);
        } catch (ExecuteException e) {
            fireOnLogOutput(logger, Level.SEVERE,
                    "Error occured while running adb for " + apk_name, e);
        }
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.INSTALLED_SUCCESS;
    }

    /**
     * clear logcat before installing
     */
    @Override
    protected boolean setup() {
        try {
            mAdbHelper = AdbHelper.getInstance(mConfig.getUnzippedSdkToolsPath(),
                    mConfig.getOutdirPath());
            mAdbHelper.logcat("-c");

        } catch (InterruptedException | IOException e) {
            fireOnLogOutput(logger, Level.SEVERE, "Error occured while clearing logcat", e);
        } catch (ExecuteException e) {
            fireOnLogOutput(logger, Level.SEVERE,
                    "Error occured while running adb to clear logcat ", e);
        }
        // even if logcat -c failed, we should still probably keep going
        return mAdbHelper != null;
    }

    /**
     * adb install <apk_file>
     */
    @Override
    protected ProcessBuilder startWorkerProcess() {
        String apkPath = mConfig.getOriginApkPath();
        if (mConfig.hasInjectedApkPath()) {
            File injectedApkPath = new File(mConfig.getInjectedApkPath());
            if (injectedApkPath.exists() && injectedApkPath.isFile()) {
                apkPath = injectedApkPath.getAbsolutePath();
            }
        }
        return new ProcessBuilder().command(mAdbHelper.getExecutablePath(), "install", apkPath).directory(
                new File(mConfig.getOutdirPath()));
    }

}
