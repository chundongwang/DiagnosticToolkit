package com.microsoft.projecta.tools;



import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.workflow.WorkFlowSingleProcStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public class ApkKiller extends WorkFlowSingleProcStage {
    private static Logger logger = Logger.getLogger(ApkKiller.class
            .getSimpleName());
    private LaunchConfig mConfig;

    public ApkKiller(LaunchConfig config) {
        super(logger.getName(), "adb process to kill the apk");
        mConfig = config;
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.KILLED_SUCCESS;
    }

    /**
     * adb shell am kill <package_name>
     */
    @Override
    protected ProcessBuilder startWorkerProcess() throws IOException {
        return new ProcessBuilder()
                .command(join(mConfig.getSdkToolsPath() , "SDK_19.1.0", "platform-tools", "adb.exe"),
                        "install",
                        mConfig.getOriginApkPath())
                .directory(new File(mConfig.getOutdirPath()));
    }

}
