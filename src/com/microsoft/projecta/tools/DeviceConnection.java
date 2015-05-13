
package com.microsoft.projecta.tools;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.workflow.WorkFlowOutOfProcStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public final class DeviceConnection extends WorkFlowOutOfProcStage {
    private static Logger logger = Logger.getLogger(DeviceConnection.class
            .getSimpleName());
    private LaunchConfig mConfig;

    public DeviceConnection(LaunchConfig config) {
        super(logger.getName(), "wconnect process");
        mConfig = config;
    }

    /**
     * tools\\wconnect.exe <device_ip>
     */
    @Override
    protected ProcessBuilder startWorkerProcess() throws IOException {
        return new ProcessBuilder()
                .command(mConfig.getSdkToolsPath() + "\\tools\\wconnect.exe",
                        mConfig.getDeviceIPAddr())
                .directory(new File(mConfig.getOutdirPath()));
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.DEVICE_CONNECTED;
    }

}
