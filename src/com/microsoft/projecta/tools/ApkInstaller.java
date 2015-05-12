
package com.microsoft.projecta.tools;

import java.util.logging.Logger;

import com.microsoft.projecta.tools.workflow.WorkFlowResult;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public final class ApkInstaller extends WorkFlowStage {
    private static Logger logger = Logger.getLogger(ApkInstaller.class
            .getSimpleName());
    private LaunchConfig mConfig;

    public ApkInstaller(LaunchConfig config) {
        super(logger.getName());
        mConfig = config;
    }

    @Override
    public void execute() {
        // TODO pseudo execution
        int i = 0;
        try {
            for (; i < 10; i++) {
                fireOnProgress(i * 10);
                fireOnLogOutput("Loading "+i);
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            logger.severe(e.toString());
        }
        fireOnCompleted(i>=9?WorkFlowResult.SUCCESS:WorkFlowResult.CANCELLED);
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.INSTALLED_SUCCESS;
    }

}
