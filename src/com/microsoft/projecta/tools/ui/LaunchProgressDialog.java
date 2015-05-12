
package com.microsoft.projecta.tools.ui;

import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import swing2swt.layout.FlowLayout;

import com.microsoft.projecta.tools.LaunchConfig;
import com.microsoft.projecta.tools.workflow.WorkFlowProgressListener;
import com.microsoft.projecta.tools.workflow.WorkFlowResult;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public class LaunchProgressDialog extends Dialog {
    private static Logger logger = Logger.getLogger(LaunchProgressDialog.class
            .getSimpleName());

    protected Object result;
    protected Shell shlLaunchProgress;
    private ProgressBar progressBarTotal;
    private ProgressBar progressBarStage;
    private Text textOutput;
    private LaunchConfig mConfig;
    private FullLaunchManager mLaunchManager;

    /**
     * Create the dialog.
     * 
     * @param parent
     * @param style
     */
    public LaunchProgressDialog(Shell parent, LaunchConfig config) {
        super(parent);
        mConfig = config;
    }

    private void init() {
        mLaunchManager = new FullLaunchManager(mConfig,
                new WorkFlowProgressListener() {

                    @Override
                    public void onProgress(WorkFlowStage sender, final WorkFlowStatus stage,
                            final int progress) {
                        getParent().getDisplay().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                progressBarTotal.setMaximum(mLaunchManager.getTotalStages());
                                if (progressBarTotal.getSelection() <= stage.ordinal()) {
                                    boolean shouldUpdate = progressBarTotal.getSelection() != stage
                                            .ordinal();
                                    progressBarTotal.setSelection(stage.ordinal());
                                    if (progress > progressBarStage.getSelection()
                                            || shouldUpdate) {
                                        progressBarStage.setSelection(progress);
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onCompleted(final WorkFlowStage sender,
                            final WorkFlowStatus stage, final WorkFlowResult result) {
                        getParent().getDisplay().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                progressBarTotal.setMaximum(mLaunchManager.getTotalStages());
                                if (progressBarTotal.getSelection() <= stage.ordinal()) {
                                    progressBarTotal.setSelection(stage.ordinal());
                                    progressBarStage.setSelection(100);
                                }
                                if (result == WorkFlowResult.FAILED
                                        || result == WorkFlowResult.CANCELLED
                                        || stage == WorkFlowStatus.KILLED_SUCCESS) {
                                    MessageBox messageBox = null;
                                    int icon = SWT.ICON_INFORMATION;
                                    String title = null;
                                    String msg = null;
                                    if (result == WorkFlowResult.FAILED) {
                                        icon = SWT.ICON_ERROR;
                                        title = "Something went wrong!";
                                        msg = String.format("Something went wrong during %s.", sender.getName());
                                    } else if (result == WorkFlowResult.CANCELLED) {
                                        icon = SWT.ICON_WARNING;
                                        title = "Cancelled.";
                                        msg = String.format("%s got cancelled by user.", sender.getName());
                                    } else if (stage == WorkFlowStatus.KILLED_SUCCESS) {
                                        icon = SWT.ICON_INFORMATION;
                                        title = "Apk Launch Process Succeeded!";
                                        msg = title;
                                    }
                                    if (title != null) {
                                        messageBox = new MessageBox(shlLaunchProgress, icon
                                                | SWT.OK);
                                        messageBox.setText(title);
                                        if (result.hasReason()) {
                                            messageBox.setMessage(result.getReason());
                                        } else {
                                            messageBox.setMessage(msg);
                                        }
                                        messageBox.open();
                                        shlLaunchProgress.close();
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onLogOutput(final WorkFlowStage sender, final String message) {
                        getParent().getDisplay().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                textOutput.append(String.format("[%s]%s\n", sender.getName(), message));
                            }
                        });
                    }

                });
        mLaunchManager.launch();
    }

    /**
     * Open the dialog.
     * 
     * @return the result
     */
    public Object open() {
        createContents();
        init();
        shlLaunchProgress.open();
        shlLaunchProgress.layout();
        Display display = getParent().getDisplay();
        while (!shlLaunchProgress.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        shlLaunchProgress = new Shell(getParent(), SWT.BORDER | SWT.RESIZE
                | SWT.TITLE);
        shlLaunchProgress.setSize(444, 227);
        shlLaunchProgress.setText("Launch Progress...");
        shlLaunchProgress.setLayout(new GridLayout(2, false));

        Label lblTotalProgress = new Label(shlLaunchProgress, SWT.NONE);
        lblTotalProgress.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
                false, 1, 1));
        lblTotalProgress.setText("Total Progress");

        progressBarTotal = new ProgressBar(shlLaunchProgress, SWT.NONE);
        progressBarTotal.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false, 1, 1));
        progressBarTotal.setMaximum(WorkFlowStatus.values().length);

        Label lblStepProgress = new Label(shlLaunchProgress, SWT.NONE);
        lblStepProgress.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
                false, 1, 1));
        lblStepProgress.setText("Step Progress");

        progressBarStage = new ProgressBar(shlLaunchProgress, SWT.NONE);
        progressBarStage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
                false, 1, 1));

        Label lblOutput = new Label(shlLaunchProgress, SWT.NONE);
        lblOutput.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false,
                1, 1));
        lblOutput.setText("Output");

        textOutput = new Text(shlLaunchProgress, SWT.BORDER | SWT.READ_ONLY
                | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
        textOutput.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
                1, 1));

        Composite composite = new Composite(shlLaunchProgress, SWT.NONE);
        composite.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, false,
                2, 1);
        gd_composite.heightHint = 34;
        composite.setLayoutData(gd_composite);

        final Button btnCancel = new Button(composite, SWT.NONE);
        btnCancel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                btnCancel.setEnabled(false);
                mLaunchManager.cancel();
            }
        });
        btnCancel.setText("Cancel");

    }
}
