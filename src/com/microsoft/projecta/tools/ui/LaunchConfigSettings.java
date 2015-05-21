
package com.microsoft.projecta.tools.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.microsoft.projecta.tools.config.LaunchConfig;

public class LaunchConfigSettings extends Dialog {

    protected Object mResult;
    protected Shell mShell;
    private LaunchConfig mConfig;

    /**
     * Create the dialog.
     * 
     * @param parent
     * @param style
     */
    public LaunchConfigSettings(Shell parent, LaunchConfig config) {
        super(parent);
        mConfig = config;
    }

    /**
     * Open the dialog.
     * 
     * @return the result
     */
    public Object open() {
        createContents();
        mShell.open();
        mShell.layout();
        Display display = getParent().getDisplay();
        while (!mShell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return mResult;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        mShell = new Shell(getParent(), SWT.BORDER | SWT.RESIZE
                | SWT.TITLE);
        mShell.setSize(450, 300);
        mShell.setText("");

    }

}
