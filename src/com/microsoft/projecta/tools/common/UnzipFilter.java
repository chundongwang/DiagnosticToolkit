
package com.microsoft.projecta.tools.common;

import java.util.zip.ZipEntry;

public interface UnzipFilter {

    boolean shouldUnzip(ZipEntry entry);

}
