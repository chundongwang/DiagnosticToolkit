
package com.microsoft.projecta.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.microsoft.projecta.tools.common.WconnectHelper;
import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.config.OS;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public final class DeviceConnection extends WorkFlowStage {
    private static Logger logger = Logger.getLogger(DeviceConnection.class.getSimpleName());
    private static final int UNZIP_BUFFER = 2048;

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
        return WorkFlowStatus.DEVICE_CONNECTED;
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
        boolean setup_result = false;
        if (!isSdkBuildDrop(mConfig.getSdkToolsPath())) {
            mConfig.setUnzippedSdkToolsPath(mConfig.getSdkToolsPath());
            setup_result = true;
        } else {
            Path zippedSdk = getOsSubdirZipFile(mConfig.getSdkToolsPath());
            Path unzippedSdkDir = path(mConfig.getOutdirPath(), "sdkTools");
            if (Files.exists(unzippedSdkDir) && Files.isDirectory(unzippedSdkDir)) {
                // TODO check if this is the same version as mConfig.getSdkToolsPath()
                mConfig.setUnzippedSdkToolsPath(unzippedSdkDir.toAbsolutePath().toString());
                setup_result = true;
            } else {
                // Unzip
                try {
                    unZipAll(zippedSdk.toFile(), unzippedSdkDir.toFile());
                    mConfig.setUnzippedSdkToolsPath(unzippedSdkDir.toAbsolutePath().toString());

                    mWcHelper = WconnectHelper.getInstance(mConfig.getUnzippedSdkToolsPath(),
                            mConfig.getOutdirPath());

                    setup_result = mWcHelper != null;
                } catch (IOException e) {
                    // TODO clean up the unzipped folder?
                    setup_result = false;
                    fireOnLogOutput(logger, Level.SEVERE, String.format(
                            "Error occurred while unzipping sdk tools from %s to %s", zippedSdk
                                    .toAbsolutePath().toString(), unzippedSdkDir.toAbsolutePath()
                                    .toString()), e);
                }
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
        return mWcHelper.build(mConfig.getDeviceIPAddr());
    }

    public void unZipAll(File zippedSdk, File unzippedSdkDir) throws ZipException, IOException {
        unZipAll(zippedSdk, unzippedSdkDir, false);
    }

    public void unZipAll(File source, File destination, boolean recursively) throws ZipException,
            IOException {
        fireOnLogOutput("Unzipping " + source.getName());
        ZipFile zip = new ZipFile(source);

        destination.getParentFile().mkdirs();
        Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();

        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(destination, currentEntry);
            // destFile = new File(newPath, destFile.getName());
            File destinationParent = destFile.getParentFile();

            // create the parent directory structure if needed
            destinationParent.mkdirs();

            if (!entry.isDirectory()) {
                BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                int currentByte;
                // establish buffer for writing file
                byte data[] = new byte[UNZIP_BUFFER];

                // write the current file to disk
                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, UNZIP_BUFFER);

                // read and write until last byte is encountered
                while ((currentByte = is.read(data, 0, UNZIP_BUFFER)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                fireOnLogOutput("Unzipped " + entry.getName());
                dest.close();
                fos.close();
                is.close();
            } else {
                // Create directory
                destFile.mkdirs();
                fireOnLogOutput("Creating " + destFile.getAbsolutePath());
            }

            if (recursively && currentEntry.endsWith(".zip")) {
                // found a zip file, try to unzip it as well
                unZipAll(destFile, destinationParent);
                // delete the unzipped file
                if (!destFile.delete()) {
                    fireOnLogOutput(logger, Level.WARNING, "Creating " + destFile.getAbsolutePath());
                }
            }
        }
        zip.close();
    }

}
