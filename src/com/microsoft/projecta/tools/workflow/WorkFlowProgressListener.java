
package com.microsoft.projecta.tools.workflow;

public interface WorkFlowProgressListener {
    /**
     * Update listener with the progress of current step
     * @param stage Current stage of the entire workflow
     * @param progress int of [0,100]
     */
    public void onProgress(WorkFlowStatus stage, int progress);

    /**
     * Current stage completed (success or failed or cancelled).
     * @param stage Current stage of the entire workflow
     * @param result success or failed or cancelled
     */
    public void onCompleted(WorkFlowStatus stage, WorkFlowResult result);
}
