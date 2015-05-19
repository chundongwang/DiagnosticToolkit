
package com.microsoft.projecta.tools.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.workflow.WorkFlowResult;

public final class CommandExecutor {
    private static Logger logger = Logger.getLogger(CommandExecutor.class.getSimpleName());

    private static String commandLine(ProcessBuilder pb) {
        StringBuilder builder = new StringBuilder();
        for (String cmd : pb.command()) {
            builder.append(cmd);
            builder.append(' ');
        }
        // last space
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    private Loggable mListener;

    private Logger mLogger;

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
                    fireOnLogOutput("Error reading from worker process stdout/stderr", e);
                }
            }
        }
    };
    public CommandExecutor() {
        this(logger, null, null);
    }
    public CommandExecutor(Loggable listener) {
        this(logger, listener, null);
    }
    public CommandExecutor(Logger logger, Loggable listener) {
        this(logger, listener, null);
    }

    public CommandExecutor(Logger logger, Loggable listener, String desc) {
        if (logger != null) {
            mLogger = logger;
        } else {
            mLogger = CommandExecutor.logger;
        }
        mListener = listener;
        mWorkerProcDesc = desc;
    }

    public WorkFlowResult execute(ProcessBuilder pb) {
        if (mWorkerProc != null) {
            fireOnLogOutput(logger, Level.WARNING, "Double execution? Attempt to recover...");
            mWorkerProc.destroy();
            mWorkerProc = null;
        }

        Thread output_handler = null;
        WorkFlowResult result = WorkFlowResult.FAILED;
        try {
            // 1. kick off
            fireOnLogOutput("Starting " + getWorkerProcDesc() + " with " + commandLine(pb));
            mWorkerProc = pb.redirectErrorStream(true).start();

            // 2. monitoring output
            output_handler = new Thread(mWorkerProcOutputHandler);
            output_handler.start();

            // 3. wait for exiting
            int exit_code = mWorkerProc.waitFor();
            if (exit_code == 0) {
                result = WorkFlowResult.SUCCESS;
            } else {
                result = WorkFlowResult.FAILED;
                fireOnLogOutput(getWorkerProcDesc() + " exited with " + exit_code);
            }

            // 4. should have completed
            output_handler.interrupt();
            if (output_handler.isAlive()) {
                fireOnLogOutput("Waiting for " + getWorkerProcDesc() + " to finish...");
                output_handler.join(1000);
            }
            fireOnLogOutput(getWorkerProcDesc() + " exited with " + exit_code);

        } catch (InterruptedException e) {
            // Cancelled by user
            fireOnLogOutput("Interupted while executing " + getWorkerProcDesc(), e);
            result = WorkFlowResult.CANCELLED;
            if (mWorkerProc != null) {
                mWorkerProc.destroy();
                mWorkerProc = null;
            }
            if (output_handler != null) {
                output_handler.interrupt();
            }
        } catch (IOException e) {
            fireOnLogOutput("Error reading from " + getWorkerProcDesc() + " stdout/stderr", e);
            result = WorkFlowResult.FAILED;
        }

        return result;
    }

    private synchronized void fireOnLogOutput(Logger logger, Level level, String message) {
        if (mListener != null) {
            mListener.onLogOutput(logger, level, message, null);
        }
    }

    private synchronized void fireOnLogOutput(String message) {
        if (mListener != null) {
            mListener.onLogOutput(getLogger(), Level.INFO, message, null);
        }
    }

    private synchronized void fireOnLogOutput(String message, Throwable e) {
        if (mListener != null) {
            mListener.onLogOutput(getLogger(), Level.SEVERE, message, e);
        }
    }

    /**
     * @return the logger
     */
    public synchronized Logger getLogger() {
        return mLogger;
    }

    /**
     * @return the workerProcDesc
     */
    public String getWorkerProcDesc() {
        return mWorkerProcDesc;
    }

    /**
     * @param logger the logger to set
     */
    public synchronized CommandExecutor setLogger(Logger logger) {
        if (logger != null) {
            mLogger = logger;
        }
        return this;
    }

    /**
     * @param workerProcDesc the workerProcDesc to set
     */
    public CommandExecutor setWorkerProcDesc(String workerProcDesc) {
        mWorkerProcDesc = workerProcDesc;
        return this;
    }

}
