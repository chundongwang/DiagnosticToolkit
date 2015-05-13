package com.microsoft.projecta.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.workflow.WorkFlowOutOfProcStage;
import com.microsoft.projecta.tools.workflow.WorkFlowResult;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
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
	protected Process startWorkerProcess() throws IOException {
		return new ProcessBuilder()
				.command(
						"lib\\jython.bat",
						mConfig.getInjectionScriptPath() + "\\AutoInjection.py",
						"--builddrop", mConfig.getBuildDropPath(), "--output",
						mConfig.getOutdirPath())
				.directory(new File(mConfig.getOutdirPath()))
				.redirectErrorStream(true).start();
	}

	@Override
	public WorkFlowStatus getStatus() {
		return WorkFlowStatus.INJECTED_GPINTEROP;
	}

}
