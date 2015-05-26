
package com.microsoft.projecta.tools.workflow;

import com.microsoft.projecta.tools.ApkInjection;
import com.microsoft.projecta.tools.ApkInstaller;
import com.microsoft.projecta.tools.ApkKiller;
import com.microsoft.projecta.tools.ApkMainLauncher;
import com.microsoft.projecta.tools.DeviceConnection;
import com.microsoft.projecta.tools.ProvisionVM;

public enum WorkFlowStatus {
    /*
     * Order matters.
     */
    RAW_APK(null),
    PROVISION_VM(ProvisionVM.class),
    CONNECT_DEVICE(DeviceConnection.class),
    INJECT_APK(ApkInjection.class),
    INSTALL_APP(ApkInstaller.class),
    LAUNCH_APP(ApkMainLauncher.class),
    TAKE_SCREENSHOT(null),
    KILL_APP(ApkKiller.class);

    private Class<? extends WorkFlowStage> mKlazz;
    WorkFlowStatus(Class<? extends WorkFlowStage> klazz) {
        setKlazz(klazz);
    }
    /**
     * @return the klazz
     */
    public Class<? extends WorkFlowStage> getKlazz() {
        return mKlazz;
    }
    /**
     * @param klazz the klazz to set
     */
    public void setKlazz(Class<? extends WorkFlowStage> klazz) {
        mKlazz = klazz;
    }
}
