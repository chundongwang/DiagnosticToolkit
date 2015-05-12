
package com.microsoft.projecta.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.workflow.WorkFlowResult;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public final class ApkInjection extends WorkFlowStage {
    private static Logger logger = Logger.getLogger(ApkInjection.class
            .getSimpleName());
    private LaunchConfig mConfig;

    public ApkInjection(LaunchConfig config) {
        super(logger.getName());
        mConfig = config;
    }

    @Override
    public void execute() {
        // TODO Jython integration
        String scriptPath = mConfig.getInjectionScriptPath();
        Thread jython_output_handler = null;
        WorkFlowResult result = WorkFlowResult.FAILED;
        try {
            final Process jython_proc = new ProcessBuilder()
                    .command("lib/jython.bat", scriptPath + "/AutoInjection.py")
                    .directory(new File(mConfig.getOutdirPath()))
                    .redirectErrorStream(true)
                    .start();
            jython_output_handler = new Thread(new Runnable() {
                @Override
                public void run() {
                    InputStream in = jython_proc.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    try {
                        while (in.available() <= 0) {
                            Thread.sleep(100);
                        }
                        line = reader.readLine();
                        while (line != null) {
                            fireOnLogOutput(line);
                            line = reader.readLine();
                        }
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Error reading from jython process stdout/stderr",
                                e);
                    } catch (InterruptedException e) {
                        logger.log(Level.WARNING,
                                "Interrupted while reading from jython process stdout/stderr",
                                e);
                    }

                }
            });
            jython_output_handler.start();
            int exit_code = jython_proc.waitFor();
            if (exit_code == 0) {
                result = WorkFlowResult.SUCCESS;
            } else {
                result.setReason("jython with injection script exited with " + exit_code);
            }
            jython_output_handler.interrupt();
            jython_output_handler.join();
            fireOnLogOutput("jython process of injection script exited with " + exit_code);

            // Process jython_proc = Runtime.getRuntime().exec(new String[] {
            // "lib/jython.bat",
            // "--version"
            // }, null, new File(mConfig.getOutdirPath()));
            // int exit_code = jython_proc.waitFor();
            // if (exit_code == 0) {
            // result = WorkFlowResult.SUCCESS;
            // } else {
            // result.setReason("jython with injection script exited with " + exit_code);
            // }
            // BufferedReader reader =
            // new BufferedReader(new InputStreamReader(jython_proc.getInputStream()));
            // String line = "";
            // StringBuilder sb = new StringBuilder();
            // while ((line = reader.readLine()) != null) {
            // sb.append(line + "\n");
            // }
            // fireOnLogOutput(sb.toString());
            // fireOnLogOutput("jython process of injection script exited with " + exit_code);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading from jython process stdout/stderr",
                    e);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Interupted while executing jython with injection script",
                    e);
            result = WorkFlowResult.CANCELLED;
            if (jython_output_handler != null) {
                jython_output_handler.interrupt();
            }
        } finally {
            fireOnCompleted(result);
        }
    }

    @Override
    public WorkFlowStatus getStatus() {
        return WorkFlowStatus.INJECTED_GPINTEROP;
    }

}
