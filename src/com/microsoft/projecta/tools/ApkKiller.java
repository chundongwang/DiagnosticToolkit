
package com.microsoft.projecta.tools;

import java.io.File;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public class ApkKiller extends WorkFlowStage {
    private static Logger logger = Logger.getLogger(ApkKiller.class.getSimpleName());
    private LaunchConfig mConfig;
    
    // progress, 0-100, and 0/100 are covered by parent class already
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
     * adb shell am kill <package_name>
     */
    @Override
    protected ProcessBuilder startWorkerProcess() {
        fireOnLogOutput("About to adb shell am kill "+mConfig.getApkPackageName());
        fireOnProgress(PROGRESS_ADB_KILL_STARTED);
        return new ProcessBuilder().command(
                join(mConfig.getUnzippedSdkToolsPath(), "SDK_19.1.0", "platform-tools", "adb.exe"),
                "shell", "am", "kill", mConfig.getApkPackageName()).directory(
                new File(mConfig.getOutdirPath()));
    }

}
