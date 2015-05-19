
package com.microsoft.projecta.tools.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AdbHelper {
    public static AdbHelper getInstance(String sdkTools, String workingDir) throws IOException {
        Path adbPath = Paths.get(sdkTools).resolve(
                Paths.get("SDK_19.1.0", "platform-tools", "adb.exe"));
        if (!Files.isExecutable(adbPath)) {
            throw new IOException("Cannot find executable adb under " + sdkTools);
        }
        return new AdbHelper(adbPath, Paths.get(workingDir));
    }
    private Path mAdbPath;
    private Path mWorkingDir;

    private boolean mSuppressNonZeroException;

    private AdbHelper(Path adbPath, Path workingDir) {
        mAdbPath = adbPath.toAbsolutePath();
        mWorkingDir = workingDir;
    }

    public void exec(String command, String... args) throws InterruptedException, IOException,
            AdbException {
        String[] cmd_line = new String[args.length + 2];
        cmd_line[0] = mAdbPath.toString();
        cmd_line[1] = command;
        System.arraycopy(args, 0, cmd_line, 2, args.length);
        ProcessBuilder pb = new ProcessBuilder().command(cmd_line).directory(mWorkingDir.toFile());
        int exitCode = pb.start().waitFor();
        if (exitCode != 0 && !isSuppressNonZeroException()) {
            throw new AdbException("adb " + command + " failed with exit code: " + exitCode);
        }
    }

    public String getAdbPath() {
        return mAdbPath.toString();
    }

    /**
     * @return the suppressNonZeroException
     */
    public boolean isSuppressNonZeroException() {
        return mSuppressNonZeroException;
    }

    public void logcat(String... args) throws InterruptedException, IOException, AdbException {
        exec("logcat", args);
    }

    /**
     * @param suppressNonZeroException the suppressNonZeroException to set
     */
    public void setSuppressNonZeroException(boolean suppressNonZeroException) {
        mSuppressNonZeroException = suppressNonZeroException;
    }

    public void shell(String... args) throws InterruptedException, IOException, AdbException {
        exec("shell", args);
    }
}
