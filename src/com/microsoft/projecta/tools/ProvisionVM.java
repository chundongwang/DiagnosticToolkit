package com.microsoft.projecta.tools;

import java.util.logging.Logger;

import com.microsoft.projecta.tools.workflow.WorkFlowStage;

public class ProvisionVM extends WorkFlowStage {
    private static Logger logger = Logger.getLogger(ProvisionVM.class
            .getSimpleName());
    private LaunchConfig mConfig;

    public ProvisionVM(LaunchConfig config) {
        super(logger.getName());
        mConfig = config;
    }

    @Override
    public void execute() {
    }

}
