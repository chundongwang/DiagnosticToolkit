package com.microsoft.projecta.tools.ui;

import java.util.List;

import com.microsoft.projecta.tools.LaunchConfig;
import com.microsoft.projecta.tools.ProvisionVM;
import com.microsoft.projecta.tools.workflow.WorkFlowProgressListener;
import com.microsoft.projecta.tools.workflow.WorkFlowResult;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public final class FullLaunchManager implements WorkFlowProgressListener {
	private LaunchConfig mConfig;
	private WorkFlowProgressListener mListener;
	private WorkFlowStatus mCurrentStatus;
	private WorkFlowStage mCurrentStage;

	public FullLaunchManager(LaunchConfig config,
			WorkFlowProgressListener listener) {
		mConfig = config;
		mListener = listener;
	}

	public int getTotalStages() {
		// TODO: Use actual steps involved in current config
		return WorkFlowStatus.values().length;
	}

	/**
	 * @return the current overall progress
	 */
	public synchronized WorkFlowStatus getCurrentProgress() {
		return mCurrentStatus;
	}

	/**
	 * @param nextStatus the nextStatus to set
	 */
	public synchronized void setCurrentStatus(WorkFlowStatus nextStatus) {
		mCurrentStatus = nextStatus;
	}

	/**
	 * @return the currentStage
	 */
	public synchronized WorkFlowStage getCurrentStage() {
		return mCurrentStage;
	}

	/**
	 * @param currentStage the currentStage to set
	 */
	public synchronized void setCurrentStage(WorkFlowStage currentStage) {
		mCurrentStage = currentStage;
	}

	private static WorkFlowStage buildStages(LaunchConfig config) {
		WorkFlowStage stageStart = null;
		// TODO: build up the train
		if (config.shouldProvisionVM()) {
			stageStart = new ProvisionVM(config);
		}
		return stageStart;
	}

	private void executeStage(WorkFlowStage stageStart) {
		setCurrentStage(stageStart);
		stageStart.addListener(this);
		stageStart.start();
	}

	public void launch() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				executeStage(buildStages(mConfig));
			}
		}).start();
	}

	public void cancel() {
		mListener.onCompleted(getCurrentStage(), getCurrentProgress(), WorkFlowResult.CANCELLED);
	}

	@Override
	public void onProgress(WorkFlowStage sender, WorkFlowStatus status, int progress) {
		setCurrentStage(sender);
		setCurrentStatus(status);
		mListener.onProgress(sender, status, progress);
	}

	@Override
	public void onCompleted(WorkFlowStage sender, WorkFlowStatus status, WorkFlowResult result) {
		setCurrentStage(sender);
		setCurrentStatus(status);
		mListener.onCompleted(null, status, result);
		
		if (result == WorkFlowResult.SUCCESS) {
			WorkFlowStage prevStage = getCurrentStage();
			List<WorkFlowStage> nextStages = prevStage.getNextSteps();
			for(WorkFlowStage nextStage : nextStages) {
				executeStage(nextStage);
			}
		}
	}
}
