
package com.microsoft.projecta.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.common.Utils;
import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.workflow.WorkFlowResult;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public final class ApkInjection extends WorkFlowStage {
    private static Logger logger = Logger.getLogger(ApkInjection.class.getSimpleName());

    private LaunchConfig mConfig;
    
    // progress, 0-100, and 0/100 are covered by parent class already
    private static final int PROGRESS_NEED_INJECTION_CHECK = 15;
    private static final int PROGRESS_RAW_APK_CACHED = 30;
    private static final int PROGRESS_INJECTION_STARTED = 40;

    public ApkInjection(LaunchConfig config) {
        super(logger.getName(), "jython process with injection script");
        mConfig = config;
    }

    /**
     * Do some pre-check
     */
    @Override
    public WorkFlowResult execute() {
        WorkFlowResult result = WorkFlowResult.FAILED;
        if (mConfig.hasInjectedApkPath()) {
            // skip execution as we've injected the exact apk before
            result = WorkFlowResult.SUCCESS;
        } else {
            fireOnProgress(PROGRESS_INJECTION_STARTED);
            result = super.execute();

            if (result == WorkFlowResult.SUCCESS) {
                // Naming pattern: <inject_root>\<apk_name_wo_ext>-injected-signed.apk
                Path localInjectedApk = path(mConfig.getOutdirPath(), "inject").resolve(
                        mConfig.getApkName() + "-injected-signed" + ".apk");
                if (Files.isRegularFile(localInjectedApk)) {
                    mConfig.setInjectedApkPath(localInjectedApk.toString());
                } else {
                    result = WorkFlowResult.FAILED;
                }
            }
        }
        return result;
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.INJECT_APK;
    }

    @Override
    protected boolean setup() {
        boolean result = false;
        Path remoteApkFile = Paths.get(mConfig.getOriginApkPath());
        if (Files.exists(remoteApkFile) && Files.isRegularFile(remoteApkFile)) {
            boolean needInjection = true;

            // local drop is <outdir>\\inject\\<apk_name>
            Path localInjectDropDir = path(mConfig.getOutdirPath(), "inject", mConfig.getApkName());

            // local origin apk is <outdir>\\inject\\<apk_name>\\<apk_name>.apk
            Path localOriginApk = path(mConfig.getOutdirPath(), "inject").resolve(
                    remoteApkFile.getFileName().toString());
            Path localInjectedApk = path(mConfig.getOutdirPath(), "inject").resolve(
                    mConfig.getApkName() + "-injected-signed" + ".apk");
            // First of all, check if we have just injected it
            if (Files.isDirectory(localInjectDropDir) && Files.isRegularFile(localInjectedApk)
                    && Files.isRegularFile(localOriginApk)) {
                try {
                    if (Files.getLastModifiedTime(localOriginApk).equals(
                            Files.getLastModifiedTime(remoteApkFile))) {
                        // Already injected and skip execution as we've injected the exact apk
                        // before
                        fireOnLogOutput("Found injected app locally. Will skip injection.");
                        mConfig.setInjectedApkPath(localInjectedApk.toString());
                        result = true;
                        needInjection = false;
                    }
                } catch (IOException e) {
                    fireOnLogOutput(logger, Level.WARNING, "Cannot compare last modified time of "
                            + remoteApkFile + " and " + localOriginApk + ". Will re-inject. ", e);
                }
            }
            fireOnProgress(PROGRESS_NEED_INJECTION_CHECK);
            if (needInjection) {
                // Need injection. clean up the folder and mkdirs afterwards
                try {
                    mConfig.setInjectedApkPath(null);
                    fireOnLogOutput("Couldn't find injected app locally. Will do injection.");
                    if (Files.exists(localInjectDropDir)) {
                        Utils.delete(localInjectDropDir);
                    }
                    if (Files.createDirectories(localInjectDropDir) != null) {
                        Files.copy(remoteApkFile, localOriginApk,
                                StandardCopyOption.COPY_ATTRIBUTES,
                                StandardCopyOption.REPLACE_EXISTING);
                        // Use local copy of origin apk for injection
                        mConfig.setOriginApkPath(localOriginApk.toString());
                        result = true;
                    }
                    fireOnProgress(PROGRESS_RAW_APK_CACHED);
                } catch (IOException e) {
                    result = false;
                    fireOnLogOutput(logger, Level.SEVERE, "Cannot clean up " + localInjectDropDir
                            + " for injection purpose.", e);
                }
            }
        }
        return result;
    }

    /**
     * lib\\jython.bat <auto_injection_py> --builddrop <build_drop> --output <out_dir> <origin_apk>
     */
    @Override
    protected ProcessBuilder startWorkerProcess() {
        // TODO save the log somewhere?
        fireOnLogOutput("About to start injection.");
        return new ProcessBuilder().command(join(".", "libs", "jython.bat"),
                join(mConfig.getInjectionScriptPath(), "AutoInjection.py"), "--builddrop",
                mConfig.getBuildDropPath(), "--output", join(mConfig.getOutdirPath(), "inject"),
                mConfig.getOriginApkPath()).directory(new File(mConfig.getOutdirPath()));
    }

}
