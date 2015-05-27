
package com.microsoft.projecta.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.common.Utils;
import com.microsoft.projecta.tools.common.XdeHelper;
import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public class ProvisionVM extends WorkFlowStage {
    private static Logger logger = Logger.getLogger(ProvisionVM.class.getSimpleName());
    private LaunchConfig mConfig;
    private XdeHelper mXdeHelper;

    // progress, 0-100, and 0/100 are covered by parent class already
    private static final int PROGRESS_NEED_COPY_VHD_CHECK = 15;
    private static final int PROGRESS_LOCAL_VHD_READY = 20;
    private static final int PROGRESS_XDE_READY = 30;
    private static final int PROGRESS_XDE_STARTED = 40;

    public ProvisionVM(LaunchConfig config) {
        super(logger.getName(), "provision vm process");
        mConfig = config;
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.PROVISION_VM;
    }

    @Override
    protected boolean setup() {
        boolean result = false;
        Path remoteVhdDir = path(mConfig.getPhoneBuildDropVhdPath());
        Path remoteFlashVhd = remoteVhdDir.resolve("Flash.vhd");
        Path remoteFlashDebugVhd = remoteVhdDir.resolve("Flash_Debug.vhd");
        Path localVhdDir = path(mConfig.getOutdirPath(), "vhd");
        Path localFlashVhd = localVhdDir.resolve("Flash.vhd");
        Path localFlashDebugVhd = localVhdDir.resolve("Flash_Debug.vhd");
        boolean copy_needed = true;
        // check if we need to copy the file
        try {
            if (Files.isRegularFile(remoteFlashVhd)
                    && Files.isRegularFile(remoteFlashDebugVhd)
                    && Files.isRegularFile(localFlashVhd)
                    && Files.isRegularFile(localFlashDebugVhd)
                    && Files.getLastModifiedTime(remoteFlashVhd).equals(
                            Files.getLastModifiedTime(localFlashVhd))
                    && Files.getLastModifiedTime(remoteFlashDebugVhd).equals(
                            Files.getLastModifiedTime(localFlashDebugVhd))) {
                copy_needed = false;
                mConfig.setLocalVhdPath(localVhdDir.toString());
            }
            fireOnProgress(PROGRESS_NEED_COPY_VHD_CHECK);
            if (copy_needed) {
                mConfig.setLocalVhdPath(null);
                fireOnLogOutput("Couldn't find specified vhd locally. Will copy one.");
                if (Files.exists(localVhdDir)) {
                    Utils.delete(localVhdDir);
                }
                if (Files.createDirectories(localVhdDir) != null) {
                    Files.copy(remoteFlashVhd, localFlashVhd, StandardCopyOption.COPY_ATTRIBUTES,
                            StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(remoteFlashDebugVhd, localFlashDebugVhd,
                            StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
                    // Use local copy of vhd for provisioning
                    mConfig.setLocalVhdPath(localFlashVhd.toString());
                }
                mConfig.setLocalVhdPath(localVhdDir.toString());
            }
            fireOnProgress(PROGRESS_LOCAL_VHD_READY);

            mXdeHelper = XdeHelper.getInstance(mConfig.getOutdirPath());
            fireOnProgress(PROGRESS_XDE_READY);

            if (mConfig.hasLocalVhdPath() && mXdeHelper != null) {
                result = true;
            }
        } catch (IOException e) {
            // swallow exception
            e.printStackTrace();
        }
        return result;
    }

    /**
     * powershell.exe <provision_script>
     */
    @Override
    protected ProcessBuilder startWorkerProcess() {
        // "C:\Program Files (x86)\Microsoft XDE\8.2\XDE.exe" /vhd <local vhd folder>\flash.vhd
        // /name <whatever> /memsize 2048

        fireOnProgress(PROGRESS_XDE_STARTED);
        return new ProcessBuilder().command(mXdeHelper.getXdePath().toString(), "/vhd",
                join(mConfig.getLocalVhdPath(), "Flash.vhd"), "/name", "diag_vm", "/memsize", "2048").directory(
                new File(mConfig.getOutdirPath()));
    }

}
