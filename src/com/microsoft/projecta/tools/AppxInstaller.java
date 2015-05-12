
package com.microsoft.projecta.tools;

import java.util.logging.Logger;

import com.microsoft.projecta.tools.workflow.WorkFlowStage;

public final class AppxInstaller extends WorkFlowStage {
    private static Logger logger = Logger.getLogger(AppxInstaller.class
            .getSimpleName());
    private LaunchConfig mConfig;

    public AppxInstaller(LaunchConfig config) {
        super(logger.getName());
        mConfig = config;
    }

    @Override
    public void execute() {
    }

}
