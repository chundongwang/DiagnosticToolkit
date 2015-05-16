
package com.microsoft.projecta.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.config.OS;
import com.microsoft.projecta.tools.workflow.WorkFlowSingleProcStage;
import com.microsoft.projecta.tools.workflow.WorkFlowResult;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public final class ApkInjection extends WorkFlowSingleProcStage {
    private static Logger logger = Logger.getLogger(ApkInjection.class.getSimpleName());
    private LaunchConfig mConfig;

    public ApkInjection(LaunchConfig config) {
        super(logger.getName(), "jython process with injection script");
        mConfig = config;
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.INJECTED_GPINTEROP;
    }

    /**
     * Best effort platform independent recursive deletion.
     * 
     * @param path to be deleted recursively
     */
    private static void deleteHelper(Path path) throws IOException {
        File file = path.toFile();
        if (file.exists()) {
            if (file.isDirectory()) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException e)
                            throws IOException {
                        // try to delete the file anyway, even if its attributes
                        // could not be read, since delete-only access is
                        // theoretically possible
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException e)
                            throws IOException {
                        if (e == null) {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        } else {
                            throw e;
                        }
                    }
                });
            } else {
                // Not a directory, try deleting it directly
                Files.delete(path);
            }
        }
    }

    /**
     * Try delete target recursively via shell command for known platforms and use
     * platform-independent code if couldn't.
     * 
     * @param target to be deleted recursively
     * @throws IOException
     */
    private void delete(Path target) throws IOException {
        boolean deleted = true;
        try {
            switch (OS.CurrentOS) {
                case WINDOWS:
                    if (Files.isDirectory(target)) {
                        if (Runtime.getRuntime().exec("cmd /C RD /S /Q \"" + target + "\"")
                                .waitFor() != 0) {
                            deleted = false;
                            fireOnLogOutput(logger, Level.WARNING,
                                    "Non-zero exit of RD while deleting " + target);
                        } else {
                            deleted = true;
                        }
                    }
                    break;
                case LINUX:
                case MAC:
                    if (Runtime.getRuntime().exec("rm -rf \"" + target + "\"").waitFor() != 0) {
                        deleted = false;
                        fireOnLogOutput(logger, Level.WARNING,
                                "Non-zero exit of rm while deleting " + target);
                    }
                    break;
                default:
                    deleted = false;
                    break;
            }
        } catch (InterruptedException e) {
            logger.warning("Cannot use platform specific delete commands for " + target);
        }
        if (!deleted) {
            logger.info("Delete " + target + " recursively with platform independent code.");
            deleteHelper(target);
        }
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
            if (Files.exists(localInjectDropDir) && Files.isDirectory(localInjectDropDir)
                    && Files.exists(localInjectedApk) && Files.isRegularFile(localInjectedApk)
                    && Files.exists(localOriginApk) && Files.isRegularFile(localOriginApk)) {
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
            if (needInjection) {
                // Need injection. clean up the folder and mkdirs afterwards
                try {
                    mConfig.setInjectedApkPath(null);
                    if (Files.exists(localInjectDropDir)) {
                        delete(localInjectDropDir);
                    }
                    if (Files.createDirectories(localInjectDropDir) != null) {
                        Files.copy(remoteApkFile, localOriginApk,
                                StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
                        // Use local copy of origin apk for injection
                        mConfig.setOriginApkPath(localOriginApk.toString());
                        result = true;
                    }
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
     * Do some pre-check
     */
    @Override
    public void execute() {
        if (mConfig.hasInjectedApkPath()) {
            // skip execution as we've injected the exact apk before
            fireOnCompleted(WorkFlowResult.SUCCESS);
        } else {
            super.execute();
            // Naming pattern: <inject_root>\<apk_name_wo_ext>-injected-signed.apk
            Path localInjectedApk = path(mConfig.getOutdirPath(), "inject").resolve(
                    mConfig.getApkName() + "-injected-signed" + ".apk");
            mConfig.setInjectedApkPath(localInjectedApk.toString());
        }
    }

    /**
     * lib\\jython.bat <auto_injection_py> --builddrop <build_drop> --output <out_dir> <origin_apk>
     */
    @Override
    protected ProcessBuilder startWorkerProcess() throws IOException {
        // TODO save the log somewhere?
        return new ProcessBuilder().command(join(".", "lib", "jython.bat"),
                join(mConfig.getInjectionScriptPath(), "AutoInjection.py"), "--builddrop",
                mConfig.getBuildDropPath(), "--output", join(mConfig.getOutdirPath(), "inject"),
                mConfig.getOriginApkPath()).directory(new File(mConfig.getOutdirPath()));
    }

}
