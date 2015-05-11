package com.microsoft.projecta.tools.workflow;

public interface WorkStepProgressListener {
	public void onProgress(WorkFlowStage stage, int progress);
	public void onCompleted(WorkFlowStage stage, WorkFlowResult result);
}
