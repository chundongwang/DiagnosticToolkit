
package com.microsoft.projecta.tools.ui.res;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wb.swt.SWTResourceManager;

public enum StatusImages {
    PENDING("pending.png"), SKIPPED("skipped.png"), RUNNING("running.png"), SUCCESS("success.png"), FAILURE("failed.png");

    private Image mImage;

    StatusImages(String path) {
        mImage = SWTResourceManager.getImage(StatusImages.class, path);
    }

    /**
     * @return the image
     */
    public Image getImage() {
        return mImage;
    }
}
