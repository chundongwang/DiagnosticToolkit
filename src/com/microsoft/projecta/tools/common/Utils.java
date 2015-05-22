package com.microsoft.projecta.tools.common;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class Utils {

    /**
     * Getting file name without extension.
     * 
     * @param path Path to the file
     * @return
     */
    public static String getNameWithoutExtension(Path path) {
        String fileName = path.getFileName().toString();
        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            fileName = fileName.substring(0, pos);
        }
        return fileName;
    }

    public static void CenteredFrame(Shell objFrame) {
        Dimension objDimension = Toolkit.getDefaultToolkit().getScreenSize();
        int iCoordX = (objDimension.width - objFrame.getSize().x) / 2;
        int iCoordY = (objDimension.height - objFrame.getSize().y) / 2;
        objFrame.setLocation(iCoordX, iCoordY);
    }

    /**
     * Helper to pick a directory
     * 
     * @param title DirectoryDialog title
     * @param msg DirectoryDialog message
     * @param default_value default folder to start with
     * @param shell the parent shell of DirectoryDialog
     * @return folder path user picked
     */
    public static String pickDirectory(String title, String msg, String default_value, Shell shell) {
        DirectoryDialog dirPickerDialog = new DirectoryDialog(shell);
        dirPickerDialog.setText(title);
        dirPickerDialog.setMessage(msg);
        if (default_value != null) {
            dirPickerDialog.setFilterPath(default_value);
        }
        return dirPickerDialog.open();
    }

    public static String pickApkFile(String title, String msg, String default_value, Shell shell) {
        FileDialog openApkFileDialog = new FileDialog(shell, SWT.OPEN);
        openApkFileDialog.setText(title);
        openApkFileDialog.setFilterPath(default_value);
        String[] filterExt = {
                "*.apk"
        };
        openApkFileDialog.setFilterExtensions(filterExt);
        return openApkFileDialog.open();
    }
    
    public static String retrieveDeviceIPAddr() {
        final StringBuilder deviceIpAddr = new StringBuilder("( Need the non-loopback IP address )");
        try {
            TshellHelper tshell = TshellHelper.getInstance(System.getProperty("user.dir"));
            String ipAddr = tshell.getIpAddr();
            if (ipAddr != null) {
                deviceIpAddr.delete(0, deviceIpAddr.length());
                deviceIpAddr.append(ipAddr);
            }
        } catch (IOException | InterruptedException | ExecuteException e) {
            // swallow
            e.printStackTrace();
        }
        return deviceIpAddr.toString();
    }
}
