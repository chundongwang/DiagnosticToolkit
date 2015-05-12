
package com.microsoft.projecta.tools.ui;

import com.microsoft.projecta.tools.LaunchConfig;
import com.microsoft.projecta.tools.workflow.WorkFlowProgressListener;
import com.microsoft.projecta.tools.workflow.WorkFlowResult;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;


public final class FullLaunchManager {
    private LaunchConfig mConfig;
    private WorkFlowProgressListener mListener;
    private WorkFlowStatus mCurrentStage;

    public FullLaunchManager(LaunchConfig config) {
        mConfig = config;
    }
    
    public int getTotalStages() {
        // TODO: Use actual steps involved in current config
        return WorkFlowStatus.values().length;
    }
    
    public WorkFlowStatus getCurrentProgress() {
        return mCurrentStage;
    }
    
    public void setCurrentStage(WorkFlowStatus nextStage) {
        mCurrentStage = nextStage;
    }

    public void prepare() {
        ;
    }

    public void run(WorkFlowProgressListener listener) {
        ;
    }
    
    public void cancel() {
        mListener.onCompleted(WorkFlowStatus.RAW_APK, WorkFlowResult.CANCELLED);
    }
}
