
package com.microsoft.projecta.tools.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WconnectHelper extends CommandHelper {

    private WconnectHelper(Path wcPath, Path workingDirPath) {
        super(wcPath, workingDirPath);
    }

    public void killServer() throws InterruptedException, IOException, ExecuteException {
        List<String> commands = new ArrayList<String>();
        commands.add("kill-server");
        exec(commands);
    }

    public void connect(String ipaddr) throws InterruptedException, IOException, ExecuteException {
        List<String> commands = new ArrayList<String>();
        commands.add(ipaddr);
        exec(commands);
    }

    public static WconnectHelper getInstance(String sdkTools, String workingDir) throws IOException {
        Path wcPath = Paths.get(sdkTools).resolve(Paths.get("tools", "wconnect.exe"));
        if (!Files.isExecutable(wcPath)) {
            throw new IOException("Cannot find executable wconnect under " + sdkTools);
        }
        return new WconnectHelper(wcPath, Paths.get(workingDir));
    }

}
