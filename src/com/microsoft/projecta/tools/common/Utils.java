
package com.microsoft.projecta.tools.common;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.microsoft.projecta.tools.config.OS;

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

    /**
     * Try delete target recursively via shell command for known platforms and use
     * platform-independent code if couldn't.
     * 
     * @param target to be deleted recursively
     * @throws IOException
     */
    public static void delete(Path target) throws IOException {
        boolean deleted = true;
        try {
            switch (OS.CurrentOS) {
                case WINDOWS:
                    if (Files.isDirectory(target)) {
                        CommandHelper command = CommandHelper.getInstance("cmd", ".");
                        try {
                            String output = command.exec("/C", "RD", "/S", "/Q", target.toString());
                            if (output.length() > 0) {
                                deleted = true;
                            }
                        } catch (ExecuteException e) {
                            deleted = false;
                            System.err.println("Non-zero exit of RD while deleting "
                                    + e.getMessage());
                            e.printStackTrace();
                        }

                        // if (Runtime.getRuntime().exec("cmd /C RD /S /Q \"" + target + "\"")
                        // .waitFor() != 0) {
                        // deleted = false;
                        // System.err.println("Non-zero exit of RD while deleting " + target);
                        // } else {
                        // deleted = true;
                        // }
                    }
                    break;
                case LINUX:
                case MAC:
                    if (Runtime.getRuntime().exec("rm -rf \"" + target + "\"").waitFor() != 0) {
                        deleted = false;
                        System.err.println("Non-zero exit of rm while deleting " + target);
                    }
                    break;
                default:
                    deleted = false;
                    break;
            }
        } catch (InterruptedException e) {
            System.err.println("Cannot use platform specific delete commands for " + target);
        }
        if (!deleted) {
            System.err.println("Delete " + target + " recursively with platform independent code.");
            deleteHelper(target);
        }
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
                    public FileVisitResult postVisitDirectory(Path dir, IOException e)
                            throws IOException {
                        if (e == null) {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        } else {
                            throw e;
                        }
                    }

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
                });
            } else {
                // Not a directory, try deleting it directly
                Files.delete(path);
            }
        }
    }

}
