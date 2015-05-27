
package com.microsoft.projecta.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.microsoft.projecta.tools.common.Loggable;
import com.microsoft.projecta.tools.common.UnzipFilter;
import com.microsoft.projecta.tools.common.Utils;
import com.microsoft.projecta.tools.common.WconnectHelper;
import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.config.OS;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public final class DeviceConnection extends WorkFlowStage {
    private static Logger logger = Logger.getLogger(DeviceConnection.class.getSimpleName());
    private static final int UNZIP_BUFFER = 2048;

    // progress, 0-100, and 0/100 are covered by parent class already
    private static final int PROGRESS_UNZIPPED_CHECK = 15;
    private static final int PROGRESS_UNZIPPED_READY = 60;
    private static final int PROGRESS_WCONNECT_STARTED = 70;

    private LaunchConfig mConfig;
    private WconnectHelper mWcHelper;

    public DeviceConnection(LaunchConfig config) {
        super(logger.getName(), "wconnect process");
        mConfig = config;
    }

    private Path getOsSubdirZipFile(String sdkToolsPath) {
        Path subdirZip = null;
        if (OS.CurrentOS == OS.WINDOWS) {
            subdirZip = path(sdkToolsPath, "PC", "ProjectA-windows.zip");
        } else if (OS.CurrentOS == OS.MAC) {
            subdirZip = path(sdkToolsPath, "MAC", "ProjectA-darwin.zip");
        } else {
            failfast("Not supported platform: " + OS.CurrentOS);
        }
        return subdirZip;
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.CONNECT_DEVICE;
    }

    private boolean isSdkBuildDrop(String sdkToolsPath) {
        Path subdirToolsDir = path(sdkToolsPath, "tools");
        if (Files.exists(subdirToolsDir) && Files.isDirectory(subdirToolsDir)) {
            return false;
        } else {
            Path subdirOsZip = getOsSubdirZipFile(sdkToolsPath);
            if (Files.exists(subdirOsZip) && Files.isRegularFile(subdirOsZip)) {
                return true;
            }
        }
        // failfast should always throw a RuntimeException and won't actually return.
        failfast(sdkToolsPath + " is neither a sdk build drop nor a unpacked folder.");
        return false;
    }

    @Override
    protected boolean setup() {
        fireOnProgress(PROGRESS_STARTED);
        boolean setup_result = false;
        if (!isSdkBuildDrop(mConfig.getSdkToolsPath())) {
            mConfig.setUnzippedSdkToolsPath(mConfig.getSdkToolsPath());
            setup_result = true;
        } else {
            Path zippedSdk = getOsSubdirZipFile(mConfig.getSdkToolsPath());
            Path zippedSdkVersion = zippedSdk.getParent().resolve(Paths.get("VERSION.txt"));
            Path unzippedSdkDir = path(mConfig.getOutdirPath(), "sdkTools");
            Path unzippedSdkVersion = unzippedSdkDir.resolve(Paths.get("VERSION.txt"));
            if (Files.isRegularFile(zippedSdkVersion) && Files.isRegularFile(unzippedSdkVersion)) {
                // TODO check if this is the same version as mConfig.getSdkToolsPath()
                try {
                    if (Files.getLastModifiedTime(zippedSdkVersion).equals(
                            Files.getLastModifiedTime(unzippedSdkVersion))) {
                        // Already unzipped and same version
                        mConfig.setUnzippedSdkToolsPath(unzippedSdkDir.toAbsolutePath().toString());
                        setup_result = true;
                    }
                } catch (IOException e) {
                    fireOnLogOutput(logger, Level.WARNING, "Cannot compare last modified time of "
                            + zippedSdkVersion + " and " + unzippedSdkVersion
                            + ". Will unzip again. ", e);
                }
            }
            fireOnProgress(PROGRESS_UNZIPPED_CHECK);
            if (!setup_result) {
                // Unzip
                try {
                    if (Files.exists(unzippedSdkDir)) {
                        Utils.delete(unzippedSdkDir);
                    }
                    Utils.unZipAll(zippedSdk.toFile(), unzippedSdkDir.toFile(), new Loggable() {
                        @Override
                        public void onLogOutput(Logger logger, Level level, String message,
                                Throwable e) {
                            fireOnLogOutput(logger, level, message, e);
                        }
                    }, new UnzipFilter() {
                        @Override
                        public boolean shouldUnzip(ZipEntry entry) {
                            String currentEntryName = entry.getName();
                            // Only unzip platform-tools for adb and tools for wconnect
                            if (currentEntryName.startsWith("tools")
                                    || currentEntryName.startsWith("SDK_19.1.0/platform-tools")) {
                                return true;
                            }
                            return false;
                        }
                    });
                    Files.copy(zippedSdkVersion, unzippedSdkVersion);
                    mConfig.setUnzippedSdkToolsPath(unzippedSdkDir.toAbsolutePath().toString());
                    fireOnLogOutput("Unzipping sdk tools done.");
                    setup_result = true;
                } catch (IOException e) {
                    // TODO clean up the unzipped folder?
                    setup_result = false;
                    fireOnLogOutput(logger, Level.SEVERE, String.format(
                            "Error occurred while unzipping sdk tools from %s to %s", zippedSdk
                                    .toAbsolutePath().toString(), unzippedSdkDir.toAbsolutePath()
                                    .toString()), e);
                }
            }
            fireOnProgress(PROGRESS_UNZIPPED_READY);
        }
        if (setup_result) {
            try {
                mWcHelper = WconnectHelper.getInstance(mConfig.getUnzippedSdkToolsPath(),
                        mConfig.getOutdirPath());
                setup_result = mWcHelper != null;
            } catch (IOException e) {
                setup_result = false;
                fireOnLogOutput(logger, Level.SEVERE, String.format(
                        "Error occurred while locating wconnect.ext under %s",
                        mConfig.getUnzippedSdkToolsPath()), e);
            }
        }
        return setup_result;
    }

    /**
     * tools\\wconnect.exe <device_ip>
     */
    @Override
    protected ProcessBuilder startWorkerProcess() {
        // TODO save the log somewhere?
        fireOnProgress(PROGRESS_WCONNECT_STARTED);
        fireOnLogOutput("About to start wconnect.");
        return mWcHelper.build(mConfig.getDeviceIPAddr(), WconnectHelper.DEFAULT_PIN);
    }

}
