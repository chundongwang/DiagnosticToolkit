
package com.microsoft.projecta.tools;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.workflow.WorkFlowOutOfProcStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public final class ApkInjection extends WorkFlowOutOfProcStage {
    private static Logger logger = Logger.getLogger(ApkInjection.class
            .getSimpleName());
    private LaunchConfig mConfig;

    public ApkInjection(LaunchConfig config) {
        super(logger.getName(), "jython process with injection script");
        mConfig = config;
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.INJECTED_GPINTEROP;
    }

    /**
     * lib\\jython.bat <auto_injection_py> --builddrop <build_drop> --output <out_dir> <origin_apk>
     */
    @Override
    protected ProcessBuilder startWorkerProcess() throws IOException {
        return new ProcessBuilder()
                .command(
                        join(".", "lib", "jython.bat"),
                        join(mConfig.getInjectionScriptPath(), "AutoInjection.py"),
                        "--builddrop", mConfig.getBuildDropPath(),
                        "--output", join(mConfig.getOutdirPath(), "inject"),
                        mConfig.getOriginApkPath())
                .directory(new File(mConfig.getOutdirPath()));
    }

}
