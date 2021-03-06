
package com.microsoft.projecta.tools.common;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.workflow.WorkFlowResult;

public class CommandHelper {

    protected CommandHelper(Path executablePath, Path workingDir) {
        mExecutablePath = executablePath;
        mCommandName = Utils.getNameWithoutExtension(executablePath);
        mWorkingDir = workingDir;
    }

    protected String mCommandName;
    protected Path mExecutablePath;
    protected Path mWorkingDir;
    protected boolean mSuppressNonZeroException;

    public ProcessBuilder build(String... args) {
        String[] cmds = new String[args.length + 1];
        cmds[0] = mExecutablePath.toString();
        System.arraycopy(args, 0, cmds, 1, args.length);
        ProcessBuilder pb = new ProcessBuilder().command(cmds).directory(mWorkingDir.toFile());
        return pb;
    }

    protected static String launch(ProcessBuilder pb, String commandDesc) throws InterruptedException, IOException,
            ExecuteException {
        final StringBuilder output = new StringBuilder();
        CommandExecutor executor = new CommandExecutor(new Loggable() {
            @Override
            public void onLogOutput(Logger logger, Level level, String message, Throwable e) {
                output.append(message);
                output.append('\n');
                if (e!=null) {
                    output.append(e.toString());
                    output.append('\n');
                }
            }
        });
        
        WorkFlowResult result = executor.execute(pb);
        if (result != WorkFlowResult.SUCCESS) {
            throw new ExecuteException(commandDesc);
        }
        
        return output.toString();
    }

    public String exec(String arg1, String... args) throws InterruptedException, IOException,
            ExecuteException {
        String[] cmds = new String[args.length + 1];
        cmds[0] = arg1;
        System.arraycopy(args, 0, cmds, 1, args.length);

        return launch(build(cmds), mCommandName + " " + arg1);
    }

    public String exec(String arg) throws InterruptedException, IOException, ExecuteException {
        return launch(build(arg), mCommandName);
    }

    public String getWorkingDir() {
        return mWorkingDir.toString();
    }

    public String getExecutablePath() {
        return mExecutablePath.toString();
    }

    /**
     * @return the suppressNonZeroException
     */
    public boolean isSuppressNonZeroException() {
        return mSuppressNonZeroException;
    }

    /**
     * @param suppressNonZeroException the suppressNonZeroException to set
     */
    public void setSuppressNonZeroException(boolean suppressNonZeroException) {
        mSuppressNonZeroException = suppressNonZeroException;
    }

    public static CommandHelper getInstance(String executable, String workingDir) throws IOException {
        return new CommandHelper(Paths.get(executable), Paths.get(workingDir));
    }
}
