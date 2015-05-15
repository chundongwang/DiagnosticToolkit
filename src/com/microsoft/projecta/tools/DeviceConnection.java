
package com.microsoft.projecta.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.config.OS;
import com.microsoft.projecta.tools.workflow.WorkFlowSingleProcStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public final class DeviceConnection extends WorkFlowSingleProcStage {
    private static Logger logger = Logger.getLogger(DeviceConnection.class
            .getSimpleName());
    private static final int UNZIP_BUFFER = 2048;

    public static void unZipAll(File source, File destination) throws ZipException, IOException
    {
        logger.info("Unzipping - " + source.getName());
        logger.entering("DeviceConnection", "unZipAll");
        ZipFile zip = new ZipFile(source);

        destination.getParentFile().mkdirs();
        Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();

        // Process each entry
        while (zipFileEntries.hasMoreElements())
        {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(destination, currentEntry);
            // destFile = new File(newPath, destFile.getName());
            File destinationParent = destFile.getParentFile();

            // create the parent directory structure if needed
            destinationParent.mkdirs();

            if (!entry.isDirectory())
            {
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
                logger.log(Level.INFO, "unzipped "+entry.getName());
                dest.close();
                fos.close();
                is.close();
            } else {
                // Create directory
                destFile.mkdirs();
                logger.log(Level.INFO, "creating "+destFile.getAbsolutePath());
            }

            if (currentEntry.endsWith(".zip"))
            {
                // found a zip file, try to unzip it as well
                unZipAll(destFile, destinationParent);
                // delete the unzipped file
                if (!destFile.delete()) {
                    logger.log(Level.WARNING, "Could not delete zip");
                }
            }
        }
        zip.close();
        logger.exiting("DeviceConnection", "unZipAll");
    }

    private LaunchConfig mConfig;

    public DeviceConnection(LaunchConfig config) {
        super(logger.getName(), "wconnect process");
        mConfig = config;
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.DEVICE_CONNECTED;
    }
    
    private File getOsSubdirZipFile(String sdkToolsPath) {
        File subdirZip = null;
        if (OS.CurrentOS == OS.WINDOWS) {
            subdirZip = path(sdkToolsPath, "PC", "ProjectA-windows.zip").toFile();
        } else if (OS.CurrentOS == OS.MAC) {
            subdirZip = path(sdkToolsPath, "MAC", "ProjectA-darwin.zip").toFile();
        } else {
            failfast("Not supported platform: "+OS.CurrentOS);
        }
        return subdirZip;
    }
    
    private boolean isSdkBuildDrop(String sdkToolsPath){
        File subdirTools = path(sdkToolsPath, "tools").toFile();
        if (subdirTools.exists() && subdirTools.isDirectory()) {
            return false;
        } else {
            File subdirOs = getOsSubdirZipFile(sdkToolsPath);
            if (subdirOs.exists() && subdirOs.isFile()) {
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
            File zippedSdk = getOsSubdirZipFile(mConfig.getSdkToolsPath());
            File unzippedSdk = path(mConfig.getOutdirPath(), "sdkTools").toFile();
            if (unzippedSdk.exists() && unzippedSdk.isDirectory()) {
                // TODO check if this is the same version as mConfig.getSdkToolsPath()
                mConfig.setUnzippedSdkToolsPath(unzippedSdk.getAbsolutePath());
                setup_result = true;
            } else {
                // Unzip
                try {
                    unZipAll(zippedSdk, unzippedSdk);
                    setup_result = true;
                } catch (IOException e) {
                    // TODO clean up the unzipped folder?
                    setup_result = false;
                    String error_msg = String.format(
                            "Error occurred while unzipping sdk tools from %s to %s",
                            zippedSdk.getAbsolutePath(), unzippedSdk.getAbsolutePath());
                    fireOnLogOutput(error_msg);
                    fireOnLogOutput(e.getMessage());
                    logger.log(Level.SEVERE, error_msg, e);
                }
            }
        }
        return setup_result;
    }

    /**
     * tools\\wconnect.exe <device_ip>
     */
    @Override
    protected ProcessBuilder startWorkerProcess() throws IOException {
        // TODO save the log somewhere?
        return new ProcessBuilder().command(
                join(mConfig.getUnzippedSdkToolsPath(), "tools", "wconnect.exe"),
                mConfig.getDeviceIPAddr()).directory(
                new File(mConfig.getOutdirPath()));
    }

}
