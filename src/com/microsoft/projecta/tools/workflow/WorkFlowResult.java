
package com.microsoft.projecta.tools.workflow;

public enum WorkFlowResult {
    SUCCESS, FAILED, CANCELLED;

    private String mReason;

    boolean hasReason() {
        return mReason == null;
    }

    void setReason(String reason) {
        mReason = reason;
    }

    String getReason() {
        return mReason;
    }
}
