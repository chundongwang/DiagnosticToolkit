
package com.microsoft.projecta.tools.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TshellHelper extends CommandHelper {

    public final static String DEFAULT_POWERSHELL = "C:\\windows\\SysWOW64\\WindowsPowerShell\\v1.0\\powershell.exe";
    public final static String GRABLOGS = "\\\\pan\\arcadia\\team\\aowselfhost\\Tools\\GrabLogs\\GrabLogs.ps1";
    public final static String EXEC_PS1_TEMPLATE = "\"& {cd %s; %s}\"";

    private TshellHelper(Path tshellPath, Path workingDirPath) {
        super(tshellPath, workingDirPath);
    }

    public void grabLogs() throws InterruptedException, IOException, ExecuteException {
        execPs1(String.format(EXEC_PS1_TEMPLATE, getWorkingDir(), GRABLOGS));
    }
    
    public void execPs1(String... args) throws InterruptedException, IOException, ExecuteException {
        exec("-Command", args);
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
