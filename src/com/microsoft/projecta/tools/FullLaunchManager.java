
package com.microsoft.projecta.tools;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.common.ExecuteException;
import com.microsoft.projecta.tools.common.TshellHelper;
import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.workflow.WorkFlowProgressListener;
import com.microsoft.projecta.tools.workflow.WorkFlowResult;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public final class FullLaunchManager implements WorkFlowProgressListener {
    private static Logger logger = Logger.getLogger(FullLaunchManager.class.getSimpleName());

    private static void failfast(Throwable e) {
        String msg = "FullLaunchManager failfast with " + e.getMessage();
        logger.severe(msg);
        throw new RuntimeException(msg, e);
    }

    private LaunchConfig mConfig;
    private WorkFlowProgressListener mListener;
    private List<WorkFlowStage> mCurrentStages;
    private int mTotalStages;

    private boolean mCancelled;

    private boolean mStopped;

    public FullLaunchManager(LaunchConfig config, WorkFlowProgressListener listener) {
        mConfig = config;
        mListener = listener;
        mCurrentStages = new ArrayList<WorkFlowStage>();
        mTotalStages = 0;
        mCancelled = false;
        mStopped = false;
    }

    /**
     * @param currentStage the currentStage to add
     */
    public synchronized void addCurrentStage(WorkFlowStage currentStage) {
        mCurrentStages.add(currentStage);
    }

    /**
     * Build the chain of actions for launch workflow. Will need to be updated each time we add
     * steps to the picture.
     * 
     * @param config LaunchConfig of this launch
     * @return WorkFlowStage to start with
     */
    @SuppressWarnings("rawtypes")
    private WorkFlowStage buildStages() {
        WorkFlowStage stageStart = null;
        WorkFlowStage stageCurrent = null;
        WorkFlowStage stage = null;

        // 1. Provision VM
        // 2. Device connection
        // 3. Inject GP-Interop
        // 4. App installation (logcat)
        // 5. App launch (logcat, snapshot)
        // 6. Kill the app
        WorkFlowStatus[] steps = WorkFlowStatus.values();
        mTotalStages = 0;
        for (WorkFlowStatus step : steps) {
            Class<? extends WorkFlowStage> k = step.getKlazz();
            if (mConfig.should(step)) {
                Constructor ctor;
                try {
                    ctor = k.getConstructor(LaunchConfig.class);
                    stage = (WorkFlowStage) ctor.newInstance(mConfig);
                    if (stageCurrent != null) {
                        stageCurrent.addNextStep(stage);
                    }
                    stageCurrent = stage;
                    mTotalStages++;
                    if (stageStart == null) {
                        stageStart = stageCurrent;
                    }
                } catch (NoSuchMethodException | SecurityException | InstantiationException
                        | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException e) {
                    failfast(e);
                }
            }
        }

        return stageStart;
    }

    /**
     * cancel current train.
     * 
     * @throws InterruptedException
     */
    public void cancel() {
        if (!isStopped() && !isCancelled()) {
            setCancelled(true);
            ArrayList<WorkFlowStage> stages = new ArrayList<WorkFlowStage>();
            stages.addAll(getCurrentStages());
            for (WorkFlowStage stage : stages) {
                try {
                    stage.cancel(-1);
                } catch (InterruptedException e) {
                    logger.warning("Unexpected InterruptedException while cancelling. ");
                }
            }
        }
    }

    /**
     * Execute specified stage and register this as the listener with it.
     * 
     * @param stageStart Stage to be started.
     */
    private void executeStage(WorkFlowStage stageStart) {
        addCurrentStage(stageStart);
        stageStart.addListener(this);
        stageStart.start();
    }

    /**
     * @return the currentStage
     */
    public synchronized List<WorkFlowStage> getCurrentStages() {
        return mCurrentStages;
    }

    public int getTotalStages() {
        return mTotalStages;
    }

    /**
     * @return the cancelled
     */
    public synchronized boolean isCancelled() {
        return mCancelled;
    }

    /**
     * @return the stopped
     */
    public synchronized boolean isStopped() {
        return mStopped;
    }

    /**
     * Build the chain of actions and kick off.
     */
    public void launch() {
        setCancelled(false);
        setStopped(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                executeStage(buildStages());
            }
        }).start();
    }

    @Override
    public void onCompleted(final WorkFlowStage sender, WorkFlowStatus status, WorkFlowResult result) {
        mListener.onCompleted(sender, status, result);
        sender.removeListener(this);

        if (result == WorkFlowResult.SUCCESS && !isCancelled()) {
            List<WorkFlowStage> nextStages = sender.getNextSteps();
            for (WorkFlowStage nextStage : nextStages) {
                executeStage(nextStage);
            }
            if (nextStages.isEmpty()) {
                // the end of everything
                setStopped(true);
            }
        } else {
            // failed or cancelled
            setStopped(true);
        }

        if (isStopped()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // grab log
                    try {
                        TshellHelper tshell = TshellHelper.getInstance(mConfig.getLogsDir());
                        String output = tshell.grabLogs();
                        mListener.onLogOutput(sender, output);
                    } catch (IOException | InterruptedException | ExecuteException e) {
                        mListener.onLogOutput(
                                sender,
                                "Error occured while grabing ETL from connected device: "
                                        + e.getMessage());
                    }
                }
            }).start();
        }

    }

    @Override
    public void onLogOutput(WorkFlowStage sender, String message) {
        mListener.onLogOutput(sender, message);
    }

    @Override
    public void onProgress(WorkFlowStage sender, WorkFlowStatus status, int progress) {
        mListener.onProgress(sender, status, progress);
    }

    /**
     * @param currentStage the currentStage to remove
     */
    public synchronized void removeCurrentStage(WorkFlowStage currentStage) {
        mCurrentStages.remove(currentStage);
    }

    /**
     * @param cancelled the cancelled to set
     */
    public synchronized void setCancelled(boolean cancelled) {
        mCancelled = cancelled;
    }

    /**
     * @param stopped the stopped to set
     */
    public synchronized void setStopped(boolean stopped) {
        mStopped = stopped;
    }
}
