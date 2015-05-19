
package com.microsoft.projecta.tools.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class AdbHelper extends CommandHelper {

    private AdbHelper(Path adbPath, Path workingDirPath) {
        super(adbPath, workingDirPath);
    }

    public void logcat(String... args) throws InterruptedException, IOException, ExecuteException {
        List<String> commands = Arrays.asList(args);
        commands.add(0, "logcat");
        exec(commands);
    }

    public void shell(String... args) throws InterruptedException, IOException, ExecuteException {
        List<String> commands = Arrays.asList(args);
        commands.add(0, "shell");
        exec(commands);
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
