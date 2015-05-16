
package com.microsoft.projecta.tools.console;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;

import com.microsoft.projecta.tools.ApkLauncher;
import com.microsoft.projecta.tools.config.Branch;
import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.workflow.WorkFlowProgressListener;
import com.microsoft.projecta.tools.workflow.WorkFlowResult;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public class LauncherConsole implements WorkFlowProgressListener {
    public static void main(String[] args) {
        new LauncherConsole().kickoff(args[args.length - 1]);
    }

    private LaunchConfig mConfig;
    private boolean mCompleted;
    private Thread mThread;

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
                    | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                System.err.println("Build stages chain failed.");
                e.printStackTrace(System.err);
            }
        }

        return stageStart;
    }

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
        mConfig.setUnzippedSdkToolsPath("C:\\tools\\ProjectA-windows");

        System.out.println("==========");
        System.out.println(mConfig.toString());
        System.out.println("==========");
        // WorkFlowStage startStage_full = buildStages(mConfig, ApkInjection.class,
        // DeviceConnection.class, ApkInstaller.class, ApkLauncher.class,
        // ApkKiller.class);
        WorkFlowStage startStage_single = buildStages(mConfig, ApkLauncher.class);
        startStage_single.addListener(this);
        startStage_single.start();

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
        if (result == WorkFlowResult.FAILED || result == WorkFlowResult.CANCELLED
                || stage == WorkFlowStatus.KILLED_SUCCESS) {
            mCompleted = true;
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
