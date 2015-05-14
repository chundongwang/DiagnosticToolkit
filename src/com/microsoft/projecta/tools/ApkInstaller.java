package com.microsoft.projecta.tools;



import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.workflow.WorkFlowOutOfProcStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public final class ApkInstaller extends WorkFlowOutOfProcStage {
    private static Logger logger = Logger.getLogger(ApkInstaller.class
            .getSimpleName());
    private LaunchConfig mConfig;

    public ApkInstaller(LaunchConfig config) {
        super(logger.getName(), "adb process to install apk");
        mConfig = config;
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.INSTALLED_SUCCESS;
    }

    /**
     * adb install <origin_apk>
     */
    @Override
    protected ProcessBuilder startWorkerProcess() throws IOException {
        // TODO install injected apk
        return new ProcessBuilder()
                .command(join(mConfig.getSdkToolsPath() , "SDK_19.1.0", "platform-tools", "adb.exe"),
                        "install",
                        mConfig.getOriginApkPath())
                .directory(new File(mConfig.getOutdirPath()));
    }

}
