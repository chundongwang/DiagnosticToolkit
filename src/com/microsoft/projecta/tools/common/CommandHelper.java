
package com.microsoft.projecta.tools.common;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

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
    
    public ProcessBuilder build(List<String> args) {
        args.add(0, mExecutablePath.toString());
        ProcessBuilder pb = new ProcessBuilder().command(args.toArray(new String[0])).directory(mWorkingDir.toFile());
        return pb;
    }

    public void exec(List<String> args) throws InterruptedException, IOException,
            ExecuteException {
        int exitCode = build(args).start().waitFor();
        if (exitCode != 0 && !isSuppressNonZeroException()) {
            throw new ExecuteException("adb " + mCommandName + " failed with exit code: " + exitCode);
        }
    }

    public void exec(String... args) throws InterruptedException, IOException,
            ExecuteException {
        exec(Arrays.asList(args));
    }

    public String getAdbPath() {
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
