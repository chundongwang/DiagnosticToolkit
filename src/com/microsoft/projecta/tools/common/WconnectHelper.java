
package com.microsoft.projecta.tools.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WconnectHelper extends CommandHelper {
    public final static String DEFAULT_PIN = "1234";

    private WconnectHelper(Path wcPath, Path workingDirPath) {
        super(wcPath, workingDirPath);
    }

    public void killServer() throws InterruptedException, IOException, ExecuteException {
        exec("kill-server");
    }

    public void connect(String ipaddr) throws InterruptedException, IOException, ExecuteException {
        exec(ipaddr, DEFAULT_PIN);
    }

    public static WconnectHelper getInstance(String sdkTools, String workingDir) throws IOException {
        Path wcPath = Paths.get(sdkTools).resolve(Paths.get("tools", "wconnect.exe"));
        if (!Files.isExecutable(wcPath)) {
            throw new IOException("Cannot find executable wconnect under " + sdkTools);
        }
        return new WconnectHelper(wcPath, Paths.get(workingDir));
    }

}
