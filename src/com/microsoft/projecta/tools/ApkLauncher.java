
package com.microsoft.projecta.tools;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.workflow.WorkFlowSingleProcStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public class ApkLauncher extends WorkFlowSingleProcStage {
    private static Logger logger = Logger.getLogger(ApkLauncher.class
            .getSimpleName());
    private LaunchConfig mConfig;

    public ApkLauncher(LaunchConfig config) {
        super(logger.getName(), "adb process to launch the activity");
        mConfig = config;
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.LAUNCH_SUCCESS;
    }

    /**
     * clear logcat before installing
     */
    @Override
    protected boolean setup() {
        try {
            Runtime.getRuntime()
                    .exec(new String[] {
                            "%COMSPEC%", "/C",
                            join(mConfig.getSdkToolsPath(), "SDK_19.1.0", "build-tools",
                                    "android-4.4.4", "aapt.exe"),
                            "dump", "xmltree", "\"" + mConfig.getOriginApkPath() + "\"",
                            "AndroidManifest.xml", ">",
                            join(mConfig.getOutdirPath(), "tmp", "AndroidManifest.xml")
                    }).waitFor();

            // TODO parse AndroidManifest.xml and grab activities

            return Runtime.getRuntime().exec(new String[] {
                    join(mConfig.getSdkToolsPath(), "SDK_19.1.0", "platform-tools", "adb.exe"),
                    "logcat", "-c"
            }).waitFor() == 0;
        } catch (InterruptedException | IOException e) {
            fireOnLogOutput(logger, Level.SEVERE, "Error occured while clearing logcat", e);
        }
        // even if logcat -c failed, we should still probably keep going
        return true;
    }

    /**
     * dump logcat after installing
     */
    @Override
    protected void cleanup() {
        String apk_name = getNameWithoutExtension(new File(mConfig.getOriginApkPath()).getName());
        try {
            Runtime.getRuntime().exec(new String[] {
                    join(mConfig.getSdkToolsPath(), "SDK_19.1.0", "platform-tools", "adb.exe"),
                    "logcat", "-v", "time", "-f", "/data/data/launch-" + apk_name + ".log"
            }).waitFor();
            Runtime.getRuntime()
                    .exec(new String[] {
                            join(mConfig.getSdkToolsPath(), "SDK_19.1.0", "platform-tools",
                                    "adb.exe"),
                            "pull", "/data/data/launch-" + apk_name + ".log",
                            join(mConfig.getOutdirPath(), "logs", "launch-" + apk_name + ".log")
                    }).waitFor();
        } catch (InterruptedException | IOException e) {
            fireOnLogOutput(logger, Level.SEVERE, "Error occured while clearing logcat for "
                    + apk_name, e);
        }
    }

    /**
     * adb shell am start <INTENT> <INTENT> = -a <ACTION> -c <CATEGORY> -n <COMPONENT> For example
     * <INTENT> = -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n
     * com.kokteyl.goal/com.kokteyl.content.Splash
     */
    @Override
    protected ProcessBuilder startWorkerProcess() throws IOException {
        return new ProcessBuilder()
                .command(
                        join(mConfig.getSdkToolsPath(), "SDK_19.1.0", "platform-tools", "adb.exe"),
                        "shell", "am", "start",
                        "-a", "android.intent.action.MAIN",
                        "-c", "android.intent.category.LAUNCHER",
                        mConfig.getOriginApkPath())
                .directory(new File(mConfig.getOutdirPath()));
    }

}
