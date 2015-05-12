
package com.microsoft.projecta.tools;

import java.util.logging.Logger;

import com.microsoft.projecta.tools.workflow.WorkFlowResult;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public class ApkKiller extends WorkFlowStage {
    private static Logger logger = Logger.getLogger(ApkKiller.class
            .getSimpleName());
    private LaunchConfig mConfig;

    public ApkKiller(LaunchConfig config) {
        super(logger.getName());
        mConfig = config;
    }

    @Override
    public void execute() {
        // TODO pseudo execution
        try {
            for (int i = 0; i < 10; i++) {
                fireOnProgress(i * 10);
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            logger.severe(e.toString());
        }
        fireOnCompleted(WorkFlowResult.SUCCESS);
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.KILLED_SUCCESS;
    }

}
