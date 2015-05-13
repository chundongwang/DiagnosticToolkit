package com.microsoft.projecta.tools.workflow;




public enum WorkFlowStatus {
    /*
     * Order matters.
     */
    RAW_APK, PROVISIONED_VM, DEVICE_CONNECTED, INJECTED_GPINTEROP, INSTALLED_SUCCESS, LAUNCH_SUCCESS, KILLED_SUCCESS;
}
