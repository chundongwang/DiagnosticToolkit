
package com.microsoft.projecta.tools.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

import com.microsoft.projecta.tools.config.OS;

public class XdeHelper extends CommandHelper {

    private Path mXdePath;

    /**
     * @return the xdePath
     */
    public Path getXdePath() {
        return mXdePath;
    }

    private XdeHelper(Path xdePath, Path workingDirPath) {
        super(xdePath, workingDirPath);
        mXdePath = xdePath.toAbsolutePath().normalize();
    }

    public static XdeHelper getInstance(String workingDir) throws IOException {
        // "C:\Program Files (x86)\Microsoft XDE\8.2\XDE.exe" /vhd <local vhd folder>\flash.vhd
        // /name <whatever> /memsize 2048
        if (OS.CurrentOS == OS.WINDOWS) {
            String programFilePath = System.getenv("ProgramFiles(x86)");
            if (programFilePath == null) {
                programFilePath = System.getenv("ProgramFiles");
            }

            Path xdeDir = Paths.get(programFilePath, "Microsoft XDE");
            File[] xdeSubDirs = xdeDir.toFile().listFiles();
            Arrays.sort(xdeSubDirs, new Comparator<File>() {
                /**
                 * Reverse the order to get latest directory first
                 */
                @Override
                public int compare(File o1, File o2) {
                    return o2.compareTo(o1);
                }
            });
            for (File xdeSubDir : xdeSubDirs) {
                if (xdeSubDir.getName().startsWith("10.0")) {
                    Path xdeExe = Paths.get(xdeSubDir.getAbsolutePath(), "XDE.exe");
                    if (Files.isExecutable(xdeExe)) {
                        // found it
                        return new XdeHelper(xdeExe, Paths.get(workingDir));
                    }
                }
            }
            throw new IOException("Cannot find executable Xde under " + xdeDir);
        } else {
            return null;
        }
    }
}
