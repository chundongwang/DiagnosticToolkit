
package com.microsoft.projecta.tools;

import java.util.logging.Logger;

import com.microsoft.projecta.tools.workflow.WorkFlowStage;

public final class DeviceConnection extends WorkFlowStage {
    private static Logger logger = Logger.getLogger(DeviceConnection.class
            .getSimpleName());
    private LaunchConfig mConfig;

    public DeviceConnection(LaunchConfig config) {
        super(logger.getName());
        mConfig = config;
    }

    @Override
    public void execute() {
    }

}
