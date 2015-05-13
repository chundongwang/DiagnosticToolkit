
package com.microsoft.projecta.tools;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.workflow.WorkFlowOutOfProcStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public class ApkLauncher extends WorkFlowOutOfProcStage {
    private static Logger logger = Logger.getLogger(ApkLauncher.class
            .getSimpleName());
    private LaunchConfig mConfig;

    public ApkLauncher(LaunchConfig config) {
        super(logger.getName(), "adb process to launch the activity");
        mConfig = config;
    }

    /**
     * adb shell am start <INTENT>
     * <INTENT> = -a <ACTION> -c <CATEGORY> -n <COMPONENT> 
     * For example
     * <INTENT> = -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.kokteyl.goal/com.kokteyl.content.Splash 
     */
    @Override
    protected ProcessBuilder startWorkerProcess() throws IOException {
        return new ProcessBuilder()
                .command(mConfig.getSdkToolsPath() + "\\SDK_19.1.0\\platform-tools\\adb.exe",
                        "shell", "am", "start", 
                        "-a", "android.intent.action.MAIN",
                        "-c", "android.intent.category.LAUNCHER",
                        mConfig.getOriginApkPath())
                .directory(new File(mConfig.getOutdirPath()));
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.LAUNCH_SUCCESS;
    }

}
