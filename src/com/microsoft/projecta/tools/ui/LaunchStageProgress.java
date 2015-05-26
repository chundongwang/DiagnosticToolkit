package com.microsoft.projecta.tools.ui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Button;

public class LaunchStageProgress extends Dialog {

    protected Object mresult;
    protected Shell mshell;

    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    public LaunchStageProgress(Shell parent, int style) {
        super(parent, style);
        setText("SWT Dialog");
    }

    /**
     * Open the dialog.
     * @return the result
     */
    public Object open() {
        createContents();
        mshell.open();
        mshell.layout();
        Display display = getParent().getDisplay();
        while (!mshell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return mresult;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        mshell = new Shell(getParent(), getStyle());
        mshell.setSize(569, 461);
        mshell.setText(getText());
        
        Button btnNewButton = new Button(mshell, SWT.NONE);
        btnNewButton.setBounds(101, 162, 105, 35);
        btnNewButton.setText("New Button");

    }
}
