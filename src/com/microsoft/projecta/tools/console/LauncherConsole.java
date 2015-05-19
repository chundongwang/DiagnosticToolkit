
package com.microsoft.projecta.tools.console;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import com.microsoft.projecta.tools.ApkInjection;
import com.microsoft.projecta.tools.ApkInstaller;
import com.microsoft.projecta.tools.ApkKiller;
import com.microsoft.projecta.tools.ApkMainLauncher;
import com.microsoft.projecta.tools.DeviceConnection;
import com.microsoft.projecta.tools.common.AndroidManifestInfo;
import com.microsoft.projecta.tools.config.Branch;
import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.workflow.WorkFlowProgressListener;
import com.microsoft.projecta.tools.workflow.WorkFlowResult;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public class LauncherConsole implements WorkFlowProgressListener {
    @SuppressWarnings("rawtypes")
    private static WorkFlowStage buildStages(LaunchConfig config, Class<?>... steps) {
        WorkFlowStage stageStart = null;
        WorkFlowStage stageCurrent = null;
        WorkFlowStage stage = null;

        for (Class<?> k : steps) {
            Constructor ctor;
            try {
                ctor = k.getConstructor(LaunchConfig.class);
                stage = (WorkFlowStage) ctor.newInstance(config);
                if (stageCurrent != null) {
                    stageCurrent.addNextStep(stage);
                }
                stageCurrent = stage;
                if (stageStart == null) {
                    stageStart = stageCurrent;
                }
            } catch (NoSuchMethodException | SecurityException | InstantiationException
                    | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                System.err.println("Build stages chain failed.");
                e.printStackTrace(System.err);
            }
        }

        return stageStart;
    }

    public static void main(String[] args) {
        new LauncherConsole().kickoff(args[args.length - 1]);
    }
    private LaunchConfig mConfig;
    private boolean mCompleted;

    private Thread mThread;

    public LauncherConsole() {
        mConfig = new LaunchConfig.Builder(Branch.Develop).build();
        mCompleted = false;
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mCompleted) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.err.println("Daemon thread interrupted. Rechecking...");
                    }
                }
            }
        });
        mThread.setDaemon(true);
    }

    public void kickoff(String apkPath) {
        mConfig.setOutdirPath(Paths.get(System.getProperty("user.dir"), "tmp").normalize()
                .toAbsolutePath().toString());
        mConfig.setInjectionScriptPath("z:\\build\\tools\\autoInjection");
        mConfig.setDeviceIPAddr("10.81.209.142");
        mConfig.setOriginApkPath(apkPath);
        // mConfig.setUnzippedSdkToolsPath("C:\\tools\\ProjectA-windows");

        System.out.println("==========");
        System.out.println(mConfig.toString());
        System.out.println("==========");

        // parse AndroidManifest.xml
        System.out.println("Parsing AndroidManifest.xml...");
        try {
            AndroidManifestInfo info = AndroidManifestInfo.parseAndroidManifest(mConfig
                    .getOriginApkPath());
            mConfig.setApkPackageInfo(info);
        } catch (IOException | XmlPullParserException e) {
            System.err.println("AndroidManifest parsing failed...");
            e.printStackTrace(System.err);
            throw new RuntimeException("AndroidManifest parsing failed.", e);
        }

        WorkFlowStage startStage = buildStages(mConfig, ApkInjection.class, DeviceConnection.class,
                ApkInstaller.class, ApkMainLauncher.class, ApkKiller.class);
        // WorkFlowStage startStage = buildStages(mConfig, ApkMainLauncher.class);
        startStage.addListener(this);
        startStage.start();

        try {
            mThread.start();
            mThread.join();
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted. Exiting...");
        }
    }

    @Override
    public void onCompleted(final WorkFlowStage sender, final WorkFlowStatus stage,
            final WorkFlowResult result) {
        System.out.println(String.format("[%s]%s completed with %s", sender.getName(),
                stage.toString(), result.toString()));
        if (result == WorkFlowResult.FAILED || result == WorkFlowResult.CANCELLED) {
            mCompleted = true;
        }
        if (result == WorkFlowResult.SUCCESS) {
            if (stage == WorkFlowStatus.KILLED_SUCCESS) {
                mCompleted = true;
            } else {
                List<WorkFlowStage> nextStages = sender.getNextSteps();
                for (WorkFlowStage nextStage : nextStages) {
                    nextStage.addListener(this);
                    nextStage.start();
                }
            }
        }
    }

    @Override
    public void onLogOutput(final WorkFlowStage sender, final String message) {
        System.err.println(String.format("[%s]: %s", sender.getName(), message));
    }

    @Override
    public void onProgress(WorkFlowStage sender, final WorkFlowStatus stage, final int progress) {
        System.out.print(String.format("\r[%s]%s: %d%%", sender.getName(), stage.toString(),
                progress));
    }
}
