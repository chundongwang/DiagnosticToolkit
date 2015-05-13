package com.microsoft.projecta.tools;



import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.workflow.WorkFlowOutOfProcStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public class ApkKiller extends WorkFlowOutOfProcStage {
    private static Logger logger = Logger.getLogger(ApkKiller.class
            .getSimpleName());
    private LaunchConfig mConfig;

    public ApkKiller(LaunchConfig config) {
        super(logger.getName(), "adb process to kill the apk");
        mConfig = config;
    }

    /**
     * adb shell am kill <package_name>
     */
    @Override
    protected ProcessBuilder startWorkerProcess() throws IOException {
        return new ProcessBuilder()
                .command(mConfig.getSdkToolsPath() + "\\SDK_19.1.0\\platform-tools\\adb.exe",
                        "install",
                        mConfig.getOriginApkPath())
                .directory(new File(mConfig.getOutdirPath()));
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.KILLED_SUCCESS;
    }

}
