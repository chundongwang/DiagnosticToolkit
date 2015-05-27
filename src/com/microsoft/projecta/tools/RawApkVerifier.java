package com.microsoft.projecta.tools;

import java.util.logging.Logger;

import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public class RawApkVerifier extends WorkFlowStage {
    private static Logger logger = Logger.getLogger(RawApkVerifier.class
            .getSimpleName());
    private LaunchConfig mConfig;

    public RawApkVerifier(LaunchConfig config) {
        super(logger.getName(), "provision vm process");
        mConfig = config;
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.RAW_APK;
    }

    @Override
    protected ProcessBuilder startWorkerProcess() {
        // TODO Auto-generated method stub
        return null;
    }

}
