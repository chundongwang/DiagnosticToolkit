
package com.microsoft.projecta.tools.workflow;

public interface WorkFlowProgressListener {
    /**
     * Update listener with the progress of current step
     * @param sender Stage which is making progress
     * @param progress int of [0,100]
     * @param stage Current stage of the entire workflow
     */
    public void onProgress(WorkFlowStage sender, WorkFlowStatus status, int progress);

    /**
     * Current stage completed (success or failed or cancelled).
     * @param sender Stage which is completed
     * @param result success or failed or cancelled
     * @param stage Current stage of the entire workflow
     */
    public void onCompleted(WorkFlowStage sender, WorkFlowStatus status, WorkFlowResult result);
}
