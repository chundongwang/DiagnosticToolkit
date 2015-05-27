
package com.microsoft.projecta.tools.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AdbHelper extends CommandHelper {
    
    private Path mAdbPath;

    private AdbHelper(Path adbPath, Path workingDirPath) {
        super(adbPath, workingDirPath);
        mAdbPath = adbPath;
    }

    /**
     * Logcat commands
     * @param args logcat arguments
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecuteException
     */
    public void logcat(String... args) throws InterruptedException, IOException, ExecuteException {
        exec("logcat", args);
    }

    /**
     * Shell commands
     * @param args shell commands and its arguments
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecuteException
     */
    public void shell(String... args) throws InterruptedException, IOException, ExecuteException {
        exec("shell", args);
    }

    /**
     * pull files from android system
     * @param args file path and other arguments
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecuteException
     */
    public void pull(String... args) throws InterruptedException, IOException, ExecuteException {
        exec("pull", args);
    }
    
    /**
     * uninstall an app with given package name
     * @param pkg_name
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecuteException
     */
    public void uninstall(String pkg_name) throws InterruptedException, IOException, ExecuteException {
        exec("uninstall", pkg_name);
    }

    /**
     * @return the adbPath
     */
    public Path getAdbPath() {
        return mAdbPath;
    }

    public static AdbHelper getInstance(String sdkTools, String workingDir) throws IOException {
        Path adbPath = Paths.get(sdkTools).resolve(
                Paths.get("SDK_19.1.0", "platform-tools", "adb.exe"));
        if (!Files.isExecutable(adbPath)) {
            throw new IOException("Cannot find executable adb under " + sdkTools);
        }
        return new AdbHelper(adbPath, Paths.get(workingDir));
    }
}
