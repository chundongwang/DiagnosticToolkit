
package com.microsoft.projecta.tools.common;

import java.io.IOException;
import java.nio.file.Path;

public class CommandHelper {

    protected CommandHelper(Path executablePath, Path workingDir) {
        mExecutablePath = executablePath.toAbsolutePath();
        mCommandName = Utils.getNameWithoutExtension(executablePath);
        mWorkingDir = workingDir;
    }

    protected String mCommandName;
    protected Path mExecutablePath;
    protected Path mWorkingDir;
    protected boolean mSuppressNonZeroException;

    public ProcessBuilder build(String... args) {
        ProcessBuilder pb = new ProcessBuilder().command(args).directory(mWorkingDir.toFile());
        return pb;
    }

    public void exec(String arg1, String... args) throws InterruptedException, IOException,
            ExecuteException {
        String[] cmds = new String[args.length + 2];
        cmds[0] = mExecutablePath.toString();
        cmds[1] = arg1;
        System.arraycopy(args, 0, cmds, 2, args.length);

        int exitCode = build(cmds).start().waitFor();
        if (exitCode != 0 && !isSuppressNonZeroException()) {
            throw new ExecuteException(mCommandName + " " + arg1 + " failed with exit code: "
                    + exitCode);
        }
    }

    public void exec(String... args) throws InterruptedException, IOException, ExecuteException {
        String[] cmds = new String[args.length + 1];
        cmds[0] = mExecutablePath.toString();
        System.arraycopy(args, 0, cmds, 1, args.length);

        int exitCode = build(cmds).start().waitFor();
        if (exitCode != 0 && !isSuppressNonZeroException()) {
            throw new ExecuteException(mCommandName + " failed with exit code: " + exitCode);
        }
    }

    public void exec(String arg) throws InterruptedException, IOException, ExecuteException {
        String[] cmds = new String[2];
        cmds[0] = mExecutablePath.toString();
        cmds[1] = arg;

        int exitCode = build(cmds).start().waitFor();
        if (exitCode != 0 && !isSuppressNonZeroException()) {
            throw new ExecuteException(mCommandName + " failed with exit code: " + exitCode);
        }
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

}
