
package com.microsoft.projecta.tools.workflow;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.common.CommandExecutor;
import com.microsoft.projecta.tools.common.Loggable;

/**
 * Abstract base class of all work step for work flow. Each step could have multiple steps to be the
 * next steps. The actual job would be executed in another thread while the progress would be
 * reported back to registered listeners.
 */
public abstract class WorkFlowStage implements Loggable {
    private static Logger logger = Logger.getLogger(WorkFlowStage.class.getSimpleName());

    private int CANCEL_PENDING_TIME = 500; // ms
    protected List<WorkFlowStage> mNextSteps;
    private List<WorkFlowProgressListener> mListeners;
    protected Thread mWorkerThread;
    protected String mName;
    private boolean mCompleted;
    protected CommandExecutor mExecutor;

    public WorkFlowStage() {
        this(null, null);
    }

    public WorkFlowStage(String name, String workerProcDesc) {
        mName = name;
        mNextSteps = new ArrayList<WorkFlowStage>();
        mListeners = new ArrayList<WorkFlowProgressListener>();
        mExecutor = new CommandExecutor(logger, this, workerProcDesc);
        mCompleted = false;
        mWorkerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                fireOnLogOutput(logger, Level.INFO, "Setting up...");
                if (setup()) {
                    fireOnLogOutput(logger, Level.INFO, "Setup done. Executing...");
                    execute();
                    fireOnLogOutput(logger, Level.INFO, "Execution done. Cleaning up...");
                    cleanup();
                    fireOnLogOutput(logger, Level.INFO, "Cleanup done.");
                } else {
                    fireOnLogOutput(logger, Level.INFO, "Setup failed.");
                    fireOnCompleted(WorkFlowResult.FAILED);
                }
            }
        });
    }

    /**
     * Add the specified listener to the list of listeners.
     * 
     * @param listener
     */
    public synchronized void addListener(WorkFlowProgressListener listener) {
        mListeners.add(listener);
    }

    /**
     * Add a step to the list of the next steps
     * 
     * @param next
     */
    public void addNextStep(WorkFlowStage next) {
        if (mWorkerThread.isAlive()) {
            throw new IllegalStateException(
                    "Adding next step is not allowed after started this work step.");
        }
        mNextSteps.add(next);
    }

    /**
     * Interrupt the worker thread and wait for CANCEL_PENDING_TIME milliseconds.
     * 
     * @throws InterruptedException as thrown by thread.join()
     */
    public void cancel() throws InterruptedException {
        cancel(CANCEL_PENDING_TIME);
    }

    /**
     * Interrupt the worker thread and wait for specified milliseconds.
     * 
     * @param mills Milliseconds to wait for worker thread to end. 0 means forever and -1 means
     *            immediate return.
     * @throws InterruptedException as thrown by thread.join()
     */
    public void cancel(int mills) throws InterruptedException {
        mWorkerThread.interrupt();
        if (mills >= 0) {
            mWorkerThread.join(mills);
        }
    }

    /**
     * Consist of post-mortem work to clean it up. This function will be executed on same thread as
     * setup/execute. Override this function to have your own logic of cleaning up.
     */
    protected void cleanup() {
    }

    /**
     * Override this function to execute the detailed job.
     */
    protected void execute() {
        WorkFlowResult result = mExecutor.execute(startWorkerProcess());
        fireOnCompleted(result);
    }

    protected void failfast() {
        failfast(null);
    }

    protected void failfast(String err) {
        String msg = getClass().getSimpleName() + " failfast";
        if (err != null) {
            msg = msg + " : " + err;
        }
        logger.severe(msg);
        throw new RuntimeException(msg);
    }

    protected void fireOnCompleted(WorkFlowResult result) {
        if (mCompleted) {
            failfast("Attempt to fire onCompleted multiple times");
        }

        mCompleted = true;
        ArrayList<WorkFlowProgressListener> listenersToNotify = new ArrayList<WorkFlowProgressListener>();
        listenersToNotify.addAll(mListeners);
        for (WorkFlowProgressListener listener : listenersToNotify) {
            listener.onCompleted(this, getStatus(), result);
        }
    }

    protected void fireOnLogOutput(Logger l, Level level, String msg) {
        fireOnLogOutput(logger, Level.INFO, msg, null);
    }

    protected void fireOnLogOutput(Logger l, Level level, String msg, Throwable e) {
        // l.log(level, msg, e);
        // only update listener on log output if not yet completed which means cleanup is muted.
        if (!mCompleted) {
            for (WorkFlowProgressListener listener : mListeners) {
                listener.onLogOutput(this, msg);
            }
        }
    }

    protected void fireOnLogOutput(String msg) {
        fireOnLogOutput(logger, Level.INFO, msg);
    }

    protected void fireOnProgress(int progress) {
        if (mCompleted) {
            failfast("Attempt to fire onProgress after completed");
        }

        for (WorkFlowProgressListener listener : mListeners) {
            listener.onProgress(this, getStatus(), progress);
        }
    }

    /**
     * Get the name of this stage.
     * 
     * @return
     */
    public String getName() {
        return mName;
    }

    /**
     * Get all the next steps of this step.
     * 
     * @return
     */
    public List<WorkFlowStage> getNextSteps() {
        return mNextSteps;
    }

    /**
     * The status represents current stage
     * 
     * @return The status represents current stage
     */
    public abstract WorkFlowStatus getStatus();

    /**
     * Tests if the worker thread of this work is alive. A thread is alive if it has been started
     * and has not yet died.
     * 
     * @return true if this thread is alive; false otherwise.
     */
    public boolean isAlive() {
        return mWorkerThread.isAlive();
    }

    /**
     * Helper function to java.nio.file.Paths.get() and it's the equivalent of: <code>
     * Paths.get(first, more).toAbsolutePath().normalize().toString()
     * </code>
     * 
     * @param first the path string or initial part of the path string
     * @param more additional strings to be joined to form the path string
     * @return
     */
    protected String join(String first, String... more) {
        return Paths.get(first, more).toAbsolutePath().normalize().toString();
    }

    @Override
    public void onLogOutput(Logger logger, Level level, String message, Throwable e) {
        fireOnLogOutput(logger, level, message, e);
    }

    /**
     * Helper function to java.nio.file.Paths.get() and it's the equivalent of: <code>
     * Paths.get(first, more).toAbsolutePath().normalize()
     * </code>
     * 
     * @param first the path string or initial part of the path string
     * @param more additional strings to be joined to form the path string
     * @return
     */
    protected Path path(String first, String... more) {
        return Paths.get(first, more).toAbsolutePath().normalize();
    }

    /**
     * Remove the specified listener from the list of listeners.
     * 
     * @param listener
     * @return true if this list contained the specified listener
     */
    public synchronized boolean removeListener(WorkFlowProgressListener listener) {
        return mListeners.remove(listener);
    }

    /**
     * Consist of pre-requisit steps before execute the work. This function will be executed on same
     * thread as execute/cleanup. Override this function to have your own logic of starting up.
     * 
     * @return true if setup succeeded; false otherwise.
     */
    protected boolean setup() {
        return true;
    }

    /**
     * Start the work on a new thread.
     */
    public void start() {
        mWorkerThread.start();
    }

    protected abstract ProcessBuilder startWorkerProcess();
}
