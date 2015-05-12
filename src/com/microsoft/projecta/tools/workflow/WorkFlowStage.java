
package com.microsoft.projecta.tools.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Abstract base class of all work step for work flow. Each step could have multiple steps to be the
 * next steps. The actual job would be executed in another thread while the progress would be
 * reported back to registered listeners.
 */
public abstract class WorkFlowStage {
    private static Logger logger = Logger.getLogger(WorkFlowStage.class
            .getSimpleName());

    private int CANCEL_PENDING_TIME = 500; // ms
    protected List<WorkFlowStage> mNextSteps;
    protected List<WorkFlowProgressListener> mListeners;
    protected WorkFlowStatus mCurrentStatus;
	protected Thread mWorkerThread;
    protected String mName;

    public WorkFlowStage() {
        this(null);
    }

    public WorkFlowStage(String name) {
        mName = name;
        mNextSteps = new ArrayList<WorkFlowStage>();
        mListeners = new ArrayList<WorkFlowProgressListener>();
        mWorkerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info(String.format("Worker[%s] stared.", mName));
                if (setup()) {
                    execute();
                    cleanup();
                }
            }
        });
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
     * Get all the next steps of this step.
     * 
     * @return
     */
    public List<WorkFlowStage> getNextSteps() {
        return mNextSteps;
    }

    /**
     * Add the specified listener to the list of listeners.
     * 
     * @param listener
     */
    public void addListener(WorkFlowProgressListener listener) {
        mListeners.add(listener);
    }

    /**
     * Remove the specified listener from the list of listeners.
     * 
     * @param listener
     * @return true if this list contained the specified listener
     */
    public boolean removeListener(WorkFlowProgressListener listener) {
        return mListeners.remove(listener);
    }
    
    /**
	 * @return the currentStatus
	 */
	public WorkFlowStatus getCurrentStatus() {
		return mCurrentStatus;
	}

	/**
	 * @param currentStatus the currentStatus to set
	 */
	public void setCurrentStatus(WorkFlowStatus currentStatus) {
		mCurrentStatus = currentStatus;
	}

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
     * Interrupt the worker thread and wait for CANCEL_PENDING_TIME milliseconds.
     * 
     * @throws InterruptedException as thrown by thread.join()
     */
    public void cancel() throws InterruptedException {
        mWorkerThread.interrupt();
        mWorkerThread.join(CANCEL_PENDING_TIME);
    }

    /**
     * Consist of pre-requisit steps before execute the work. This function will be executed on same
     * thread as execute/cleanup. Override this function to have your own logic of starting up.
     * 
     * @return true if setup succeeded; false otherwise.
     */
    protected boolean setup() {
        logger.info(String.format("Worker[%s] setup done.", mName));
        return true;
    }

    /**
     * Consist of post-mortem work to clean it up. This function will be executed on same thread as
     * setup/execute. Override this function to have your own logic of cleaning up.
     */
    protected void cleanup() {
        logger.info(String.format("Worker[%s] cleaned up.", mName));
    }

    /**
     * Override this function to execute the detailed job.
     */
    protected abstract void execute();

    /**
     * Start the work on a new thread.
     */
    public void start() {
        mWorkerThread.start();
    }
}
