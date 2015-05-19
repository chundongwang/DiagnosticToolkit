
package com.microsoft.projecta.tools.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AdbHelper extends CommandHelper {

    private AdbHelper(Path adbPath, Path workingDirPath) {
        super(adbPath, workingDirPath);
    }

    public void logcat(String... args) throws InterruptedException, IOException, ExecuteException {
        exec("logcat", args);
    }

    public void shell(String... args) throws InterruptedException, IOException, ExecuteException {
        exec("shell", args);
    }

    public void pull(String... args) throws InterruptedException, IOException, ExecuteException {
        exec("pull", args);
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
