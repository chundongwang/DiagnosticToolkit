
package com.microsoft.projecta.tools.workflow;

public enum WorkFlowResult {
    SUCCESS, FAILED, CANCELLED;

    private String mReason;

    public String getReason() {
        return mReason;
    }

    public boolean hasReason() {
        return mReason != null;
    }

    public void setReason(String reason) {
        mReason = reason;
    }
}
