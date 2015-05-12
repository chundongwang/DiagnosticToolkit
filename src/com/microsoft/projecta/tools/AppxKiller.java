
package com.microsoft.projecta.tools;

import java.util.logging.Logger;

import com.microsoft.projecta.tools.workflow.WorkFlowStage;

public class AppxKiller extends WorkFlowStage {
    private static Logger logger = Logger.getLogger(AppxKiller.class
            .getSimpleName());
    private LaunchConfig mConfig;

    public AppxKiller(LaunchConfig config) {
        super(logger.getName());
        mConfig = config;
    }

    @Override
    public void execute() {
    }

}
