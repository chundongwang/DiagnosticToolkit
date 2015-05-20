
package com.microsoft.projecta.tools.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TshellHelper extends CommandHelper {

    public final static String DEFAULT_POWERSHELL = "C:\\windows\\SysWOW64\\WindowsPowerShell\\v1.0\\powershell.exe";
    public final static String GRABLOGS = "\\\\pan\\arcadia\\team\\aowselfhost\\Tools\\GrabLogs\\GrabLogs.ps1";
    public final static String EXEC_PS1_TEMPLATE = "\"& {cd %s; %s}\"";
    public final static String EXEC_CMD_TEMPLATE = "\"& {%s}\"";

    private TshellHelper(Path tshellPath, Path workingDirPath) {
        super(tshellPath, workingDirPath);
    }

    public String getIpAddr() throws InterruptedException, IOException, ExecuteException {
        String output = execPs1(String.format(EXEC_CMD_TEMPLATE, "open-device 127.0.0.1; cmdd ipconfig"));
        // System.getProperty("line.separator") doesn't fit here as it's process output not file content
        String[] lines = output.split("\n");
        String ipAddr = null;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("IPv4 Address.")) {
                ipAddr = line.substring(line.lastIndexOf(':')+1, line.length()).trim();
                break;
            }
        }
        return ipAddr;
    }

    public void grabLogs() throws InterruptedException, IOException, ExecuteException {
        execPs1(String.format(EXEC_PS1_TEMPLATE, getWorkingDir(), GRABLOGS));
    }
    
    public String execPs1(String... args) throws InterruptedException, IOException, ExecuteException {
        return exec("-Command", args);
    }

    public static TshellHelper getInstance(String workingDir) throws IOException {
        // TODO Get powershell path via: reg query
        // HKLM\SOFTWARE\Microsoft\PowerShell\1\PowerShellEngine ApplicationBase
        Path powershellPath = Paths.get(DEFAULT_POWERSHELL);
        if (!Files.isExecutable(powershellPath)) {
            throw new IOException("Cannot find executable powershell under " + powershellPath);
        }
        return new TshellHelper(powershellPath, Paths.get(workingDir));
    }
}
