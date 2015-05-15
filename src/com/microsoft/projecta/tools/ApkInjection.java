
package com.microsoft.projecta.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.config.OS;
import com.microsoft.projecta.tools.workflow.WorkFlowOutOfProcStage;
import com.microsoft.projecta.tools.workflow.WorkFlowResult;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public final class ApkInjection extends WorkFlowOutOfProcStage {
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
                    if (Runtime.getRuntime().exec("RD /S /Q \"" + target + "\"").waitFor() != 0) {
                        deleted = false;
                        fireOnLogOutput(logger, Level.WARNING, "Non-zero exit of RD while deleting " + target);
                    }
                    break;
                case LINUX:
                case MAC:
                    if (Runtime.getRuntime().exec("rm -rf \"" + target + "\"").waitFor() != 0) {
                        deleted = false;
                        fireOnLogOutput(logger, Level.WARNING, "Non-zero exit of rm while deleting " + target);
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

    private static String getNameWithoutExtension(String fileName) {
        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            fileName = fileName.substring(0, pos);
        }
        return fileName;
    }

    @Override
    protected boolean setup() {
        boolean result = false;
        File remoteApkFile = new File(mConfig.getOriginApkPath());
        if (remoteApkFile.exists() && remoteApkFile.isFile()) {
            // local drop is <outdir>\\inject\\<apk_name>
            File localInjectDropDir = path(mConfig.getOutdirPath(), "inject",
                    getNameWithoutExtension(remoteApkFile.getName())).toFile();
            
            // First of all, check if we have just injected it
            if (localInjectDropDir.exists() && localInjectDropDir.isDirectory()) {
                // local origin apk is <outdir>\\inject\\<apk_name>\\<apk_name>.apk
                File localOriginApk = path(localInjectDropDir.getAbsolutePath(),
                        remoteApkFile.getName()).toFile();
                if (localOriginApk.exists()) {
                    // TODO check last modified date?
                    // Already injected
                    fireOnLogOutput("Found injected app locally. Will skip injection.");
                    mConfig.setInjectedApkPath(localOriginApk.getAbsolutePath());
                    result = true;
                }
            }
            // Need injection. clean up the folder and mkdirs afterwards
            try {
                mConfig.setInjectedApkPath(null);
                if (localInjectDropDir.exists()) {
                    delete(path(localInjectDropDir.getAbsolutePath()));
                }
                if (localInjectDropDir.mkdirs()) {
                    result = true;
                }
            } catch (IOException e) {
                result = false;
                logger.severe("Cannot use clean up " + localInjectDropDir.getAbsolutePath()
                        + " for injection purpose.");
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
            fireOnCompleted(WorkFlowResult.SUCCESS);
        }
        super.execute();
    }

    /**
     * lib\\jython.bat <auto_injection_py> --builddrop <build_drop> --output <out_dir> <origin_apk>
     */
    @Override
    protected ProcessBuilder startWorkerProcess() throws IOException {
        return new ProcessBuilder().command(join(".", "lib", "jython.bat"),
                join(mConfig.getInjectionScriptPath(), "AutoInjection.py"), "--builddrop",
                mConfig.getBuildDropPath(), "--output", join(mConfig.getOutdirPath(), "inject"),
                mConfig.getOriginApkPath()).directory(new File(mConfig.getOutdirPath()));
    }

}
