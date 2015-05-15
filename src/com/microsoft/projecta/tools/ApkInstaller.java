
package com.microsoft.projecta.tools;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.workflow.WorkFlowResult;
import com.microsoft.projecta.tools.workflow.WorkFlowSingleProcStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public final class ApkInstaller extends WorkFlowSingleProcStage {
    private static Logger logger = Logger.getLogger(ApkInstaller.class
            .getSimpleName());
    private LaunchConfig mConfig;

    public ApkInstaller(LaunchConfig config) {
        super(logger.getName(), "adb process to install apk");
        mConfig = config;
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.INSTALLED_SUCCESS;
    }

    /**
     * clear logcat before installing
     */
    @Override
    protected boolean setup() {
        try {
            return Runtime.getRuntime().exec(new String[] {
                    join(mConfig.getSdkToolsPath(), "SDK_19.1.0", "platform-tools", "adb.exe"),
                    "logcat", "-c"
            }).waitFor() == 0;
        } catch (InterruptedException | IOException e) {
            fireOnLogOutput(logger, Level.SEVERE, "Error occured while clearing logcat", e);
        }
        // even if logcat -c failed, we should still probably keep going
        return true;
    }

    /**
     * dump logcat after installing
     */
    @Override
    protected void cleanup() {
        String apk_name = getNameWithoutExtension(new File(mConfig.getOriginApkPath()).getName());
        try {
            Runtime.getRuntime().exec(new String[] {
                    join(mConfig.getSdkToolsPath(), "SDK_19.1.0", "platform-tools", "adb.exe"),
                    "logcat", "-v", "time", "-f", "/data/data/install-"+apk_name+".log"
            }).waitFor();
            Runtime.getRuntime()
                    .exec(new String[] {
                            join(mConfig.getSdkToolsPath(), "SDK_19.1.0", "platform-tools",
                                    "adb.exe"),
                            "pull", "/data/data/install-"+apk_name+".log",
                            join(mConfig.getOutdirPath(), "logs", "install-"+apk_name+".log")
                    }).waitFor();
        } catch (InterruptedException | IOException e) {
            fireOnLogOutput(logger, Level.SEVERE, "Error occured while clearing logcat for "+apk_name, e);
        }
    }

    /**
     * adb install <origin_apk>
     */
    @Override
    protected ProcessBuilder startWorkerProcess() throws IOException {
        // TODO install injected apk
        String apkPath = mConfig.getOriginApkPath();
        if (mConfig.hasInjectedApkPath()) {
            File injectedApkPath = new File(mConfig.getInjectedApkPath());
            if (injectedApkPath.exists() && injectedApkPath.isFile()) {
                apkPath = injectedApkPath.getAbsolutePath();
            }
        }
        return new ProcessBuilder()
                .command(
                        join(mConfig.getSdkToolsPath(), "SDK_19.1.0", "platform-tools", "adb.exe"),
                        "install",
                        apkPath)
                .directory(new File(mConfig.getOutdirPath()));
    }

}
