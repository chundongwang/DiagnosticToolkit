package com.microsoft.projecta.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.workflow.WorkFlowOutOfProcStage;
import com.microsoft.projecta.tools.workflow.WorkFlowResult;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public final class DeviceConnection extends WorkFlowOutOfProcStage {
	private static Logger logger = Logger.getLogger(DeviceConnection.class
			.getSimpleName());
	private LaunchConfig mConfig;
	
	public DeviceConnection(LaunchConfig config) {
		super(logger.getName(), "wconnect process");
		mConfig = config;
	}

	@Override
	protected Process startWorkerProcess() throws IOException {
		return new ProcessBuilder()
				.command(mConfig.getSdkToolsPath() + "\\tools\\wconnect.exe",
						mConfig.getDeviceIPAddr())
				.directory(new File(mConfig.getOutdirPath()))
				.redirectErrorStream(true).start();
	}

	@Override
	public WorkFlowStatus getStatus() {
		return WorkFlowStatus.DEVICE_CONNECTED;
	}

}
