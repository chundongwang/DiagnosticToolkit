
package com.microsoft.projecta.tools.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TshellHelper extends CommandHelper {

    public final static String DEFAULT_POWERSHELL = "C:\\windows\\SysWOW64\\WindowsPowerShell\\v1.0\\powershell.exe";
    public final static String REG_PATH_POWERSHELL_KEY = "HKLM\\SOFTWARE\\Wow6432Node\\Microsoft\\PowerShell\\1\\PowerShellEngine";
    public final static String REG_PATH_POWERSHELL_VALUE = "ApplicationBase";
    public final static String REG_PATH_POWERSHELL_TYPE = "REG_SZ";
    public final static String GRABLOGS = "\\\\pan\\arcadia\\team\\aowselfhost\\Tools\\GrabLogs\\GrabLogs.ps1";
    public final static String EXEC_PS1_TEMPLATE = "\"& {cd %s; %s}\"";
    public final static String EXEC_CMD_TEMPLATE = "\"& {%s}\"";

    private TshellHelper(Path tshellPath, Path workingDirPath) {
        super(tshellPath, workingDirPath);
    }

    /**
     * filter out the desired line/piece of the whole output of the process. Only the first occurance which matches the desired pattern would be processed.
     * @param procOut the output of the process, should include stdout and stderr
     * @param prefix the start of the line to be recognized
     * @param feature the token lead the real value we want
     * @return the value hidden inside the process output
     */
    private static String filterOutput(String procOut, String prefix, String feature) {
        // System.getProperty("line.separator") doesn't fit here as it's process output not file
        // content
        String[] lines = procOut.split("\n");
        String result = null;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith(prefix)) {
                result = line;
                break;
            }
        }
        if (result != null && result.length() > feature.length()) {
            result = result.substring(result.lastIndexOf(feature) + feature.length(),
                    result.length()).trim();
        }
        return result;
    }

    public String getIpAddr() throws InterruptedException, IOException, ExecuteException {
        String output = execPs1(String.format(EXEC_CMD_TEMPLATE,
                "open-device 127.0.0.1; cmdd ipconfig"));
        return filterOutput(output, "IPv4 Address.", ":");
    }

    public void grabLogs() throws InterruptedException, IOException, ExecuteException {
        execPs1(String.format(EXEC_PS1_TEMPLATE, getWorkingDir(), GRABLOGS));
    }

    public String execPs1(String... args) throws InterruptedException, IOException,
            ExecuteException {
        return exec("-Command", args);
    }

    private static String getPowerShellPath() throws InterruptedException, IOException,
            ExecuteException {
        ProcessBuilder pb = new ProcessBuilder().command("reg", "query",
                REG_PATH_POWERSHELL_KEY, "/v", REG_PATH_POWERSHELL_VALUE);
        String output = launch(pb, "reg query");
        return filterOutput(output, REG_PATH_POWERSHELL_VALUE, REG_PATH_POWERSHELL_TYPE);
    }

    public static TshellHelper getInstance(String workingDir) throws IOException {
        String powershellBase = null;
        try {
            powershellBase = getPowerShellPath();
            if (powershellBase == null) {
                powershellBase = DEFAULT_POWERSHELL;
            }
        } catch (InterruptedException | ExecuteException e) {
            throw new IOException("Cannot find powershell base directory", e);
        }

        Path powershellPath = Paths.get(powershellBase, "powershell.exe");
        if (!Files.isExecutable(powershellPath)) {
            throw new IOException("Cannot find executable powershell under " + powershellPath);
        }
        return new TshellHelper(powershellPath, Paths.get(workingDir));
    }
}
