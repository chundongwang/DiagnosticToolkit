
package com.microsoft.projecta.tools;

import java.util.logging.Logger;

import com.microsoft.projecta.tools.workflow.WorkFlowStage;

public class AppxLauncher extends WorkFlowStage {
    private static Logger logger = Logger.getLogger(AppxLauncher.class
            .getSimpleName());
    private LaunchConfig mConfig;

    public AppxLauncher(LaunchConfig config) {
        super(logger.getName());
        mConfig = config;
    }

    @Override
    public void execute() {
    }

}
