
package com.microsoft.projecta.tools.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WconnectServiceHelper implements Loggable {

    protected WconnectServiceHelper(Path executablePath, Path workingDirPath, Path sdkToolsPath,
            Path logDirPath) {
        mExecutablePath = executablePath.toAbsolutePath();
        mWorkingDirPath = workingDirPath;
        mSdkToolsPath = sdkToolsPath.toAbsolutePath();
        mLogDirPath = logDirPath;
    }

    private Path mSdkToolsPath;
    private Path mExecutablePath;
    private Path mWorkingDirPath;
    private Path mLogDirPath;

    public static WconnectServiceHelper getInstance(String sdkTools, String workingDir,
            String logDir)
            throws IOException {
        Path wsPath = Paths.get(sdkTools).resolve(Paths.get("tools", "wconnectsrv.exe"));
        if (!Files.isExecutable(wsPath)) {
            throw new IOException("Cannot find executable wconnect under " + sdkTools);
        }
        Path logDirPath = Paths.get(logDir);
        if (!Files.isDirectory(logDirPath)) {
            logDirPath = Files.createDirectories(logDirPath);
        }
        return new WconnectServiceHelper(wsPath, Paths.get(workingDir), Paths.get(sdkTools),
                logDirPath);
    }

    public void startDaemon() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // kill existing server first
                    WconnectHelper wcHelper = WconnectHelper.getInstance(mSdkToolsPath.toString(),
                            mWorkingDirPath.toString());
                    wcHelper.killServer();

                    // give the existing service process a chance to quit
                    Thread.sleep(1000);

                    CommandExecutor executor = new CommandExecutor(null,
                            WconnectServiceHelper.this,
                            "Wconnect Service Process");
                    ProcessBuilder pb = new ProcessBuilder().command(mExecutablePath.toString(),
                            "-logLevel", "3").directory(mWorkingDirPath.toFile());
                    executor.execute(pb);
                } catch (IOException | InterruptedException | ExecuteException e) {
                    logFile("Error while starting daemon process", e);
                }
            }
        }).start();
    }

    public void stopDaemon() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    WconnectHelper wcHelper = WconnectHelper.getInstance(mSdkToolsPath.toString(),
                            mWorkingDirPath.toString());
                    wcHelper.killServer();
                } catch (IOException | InterruptedException | ExecuteException e) {
                    logFile("Error while stopping daemon process", e);
                }
            }
        }).start();
    }

    protected void logFile(String message, Throwable e) {
        try {
            File logfile = mLogDirPath.resolve("wconnectsvc.log").toFile();
            logfile.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(logfile));
            writer.write(message);
            // TODO what about e?
            writer.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    @Override
    public void onLogOutput(Logger logger, Level level, String message, Throwable e) {
        logFile(message, e);
    }
}
