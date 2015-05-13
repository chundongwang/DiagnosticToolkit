package com.microsoft.projecta.tools.workflow;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class WorkFlowOutOfProcStage extends WorkFlowStage {
    private static Logger logger = Logger.getLogger(WorkFlowOutOfProcStage.class.getSimpleName());

    private String mWorkerProcDesc;
    private Process mWorkerProc;
    private Runnable mWorkerProcOutputHandler = new Runnable() {
        @Override
        public void run() {
            if (mWorkerProc != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        mWorkerProc.getInputStream()));

                String line = null;
                try {
                    while ((line = reader.readLine()) != null) {
                        if (Thread.currentThread().isInterrupted())
                            break;
                        fireOnLogOutput(line);
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error reading from worker process stdout/stderr", e);
                }
            }
        }
    };

    public WorkFlowOutOfProcStage(String name, String workerProcDesc) {
        super(name);
        mWorkerProc = null;
        mWorkerProcDesc = workerProcDesc;
    }

    /**
     * @return the workerProcDesc
     */
    public synchronized String getWorkerProcDesc() {
        return mWorkerProcDesc;
    }

    /**
     * @param workerProcDesc the workerProcDesc to set
     */
    public synchronized void setWorkerProcDesc(String workerProcDesc) {
        mWorkerProcDesc = workerProcDesc;
    }

    protected abstract ProcessBuilder startWorkerProcess() throws IOException;

    @Override
    public void execute() {
        if (mWorkerProc != null) {
            logger.warning("Double execution? Attempt to recover...");
            mWorkerProc.destroy();
            mWorkerProc = null;
        }

        Thread output_handler = null;
        WorkFlowResult result = WorkFlowResult.FAILED;
        try {
            // 1. kick off
            mWorkerProc = startWorkerProcess().redirectErrorStream(true).start();

            // 2. monitoring output
            output_handler = new Thread(mWorkerProcOutputHandler);
            output_handler.start();

            // 3. wait for exiting
            int exit_code = mWorkerProc.waitFor();
            mWorkerProc = null;
            if (exit_code == 0) {
                result = WorkFlowResult.SUCCESS;
            } else {
                result = WorkFlowResult.FAILED;
                result.setReason(getWorkerProcDesc() + " exited with " + exit_code);
            }

            // 4. should have completed
            output_handler.join();
            fireOnLogOutput(getWorkerProcDesc() + " exited with " + exit_code);

        } catch (InterruptedException e) {
            // Cancelled by user
            logger.log(Level.SEVERE, "Interupted while executing " + getWorkerProcDesc(), e);
            result = WorkFlowResult.CANCELLED;
            if (mWorkerProc != null) {
                mWorkerProc.destroy();
                mWorkerProc = null;
            }
            if (output_handler != null) {
                output_handler.interrupt();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE,
                    "Error reading from " + getWorkerProcDesc() + " stdout/stderr", e);
            result = WorkFlowResult.FAILED;
            result.setReason(getWorkerProcDesc() + " failed with " + e.getMessage());
        } finally {
            fireOnCompleted(result);
        }
    }
}
