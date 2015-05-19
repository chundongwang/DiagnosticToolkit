
package com.microsoft.projecta.tools;

import java.io.File;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public class ProvisionVM extends WorkFlowStage {
    private static Logger logger = Logger.getLogger(ProvisionVM.class
            .getSimpleName());
    private LaunchConfig mConfig;

    public ProvisionVM(LaunchConfig config) {
        super(logger.getName(), "provision vm process");
        mConfig = config;
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.PROVISIONED_VM;
    }

    /**
     * powershell.exe <provision_script>
     */
    @Override
    protected ProcessBuilder startWorkerProcess() {
        return new ProcessBuilder()
                .command("powershell.exe",
                        mConfig.getOriginApkPath())
                .directory(new File(mConfig.getOutdirPath()));
    }

}
