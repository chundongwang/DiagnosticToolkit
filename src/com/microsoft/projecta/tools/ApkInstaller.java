
package com.microsoft.projecta.tools;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.workflow.WorkFlowOutOfProcStage;
import com.microsoft.projecta.tools.workflow.WorkFlowResult;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public final class ApkInstaller extends WorkFlowOutOfProcStage {
    private static Logger logger = Logger.getLogger(ApkInstaller.class
            .getSimpleName());
    private LaunchConfig mConfig;

    public ApkInstaller(LaunchConfig config) {
        super(logger.getName(), "adb process to install apk");
        mConfig = config;
    }

	@Override
	protected Process startWorkerProcess() throws IOException {
		return new ProcessBuilder()
				.command(mConfig.getSdkToolsPath() + "\\SDK_19.1.0\\platform-tools\\adb.exe",
						"install",
						mConfig.getOriginApkPath())
				.directory(new File(mConfig.getOutdirPath()))
				.redirectErrorStream(true).start();
	}

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.INSTALLED_SUCCESS;
    }

}
