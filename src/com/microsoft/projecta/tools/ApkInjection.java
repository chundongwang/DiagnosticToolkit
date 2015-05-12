
package com.microsoft.projecta.tools;

import java.util.logging.Logger;

import com.microsoft.projecta.tools.workflow.WorkFlowStage;

public final class ApkInjection extends WorkFlowStage {
    private static Logger logger = Logger.getLogger(ApkInjection.class
            .getSimpleName());
    private LaunchConfig mConfig;
    
    public ApkInjection(LaunchConfig config) {
        super(logger.getName());
        mConfig = config;
    }

    @Override
    public void execute() {   
        String scriptPath = mConfig.getInjectionScriptPath();
    }

}
