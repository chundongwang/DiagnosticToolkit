
package com.microsoft.projecta.tools.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import swing2swt.layout.FlowLayout;

import com.microsoft.projecta.tools.FullLaunchManager;
import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.ui.res.StatusImages;
import com.microsoft.projecta.tools.workflow.WorkFlowProgressListener;
import com.microsoft.projecta.tools.workflow.WorkFlowResult;
import com.microsoft.projecta.tools.workflow.WorkFlowStage;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public class LaunchProgressDialog extends Dialog {
    private static Logger logger = Logger.getLogger(LaunchProgressDialog.class.getSimpleName());

    protected WorkFlowResult mResult;
    protected Shell shlLaunchProgress;
    private ProgressBar progressBarStage;
    private Text textOutput;
    private LaunchConfig mConfig;
    private FullLaunchManager mLaunchManager;
    private Button mBtnCancel;
    private WorkFlowStatus mFinalStage;
    private Label mImageLblPhoneVM;
    private Label mImageLblDeviceConn;
    private Label mImageLblApkInjection;
    private Label mImageLblAppInstall;
    private Label mImageLblAppLaunch;
    private Label mImageLblScreenShot;
    private Label mImageLblKillApp;
    private Label[] mImageLabels;

    /**
     * Create the dialog.
     * 
     * @param parent
     * @param style
     */
    public LaunchProgressDialog(Shell parent, LaunchConfig config) {
        super(parent);
        mFinalStage = WorkFlowStatus.KILL_APP;
        mConfig = config;
        if (!mConfig.shouldKillApp()) {
            mFinalStage = WorkFlowStatus.LAUNCH_APP;
        }
        mConfig = config;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        shlLaunchProgress = new Shell(getParent(), SWT.BORDER | SWT.RESIZE | SWT.TITLE);
        shlLaunchProgress.setSize(527, 383);
        shlLaunchProgress.setText("Launch Progress...");
        shlLaunchProgress.setLayout(new GridLayout(2, false));

        Composite composite = new Composite(shlLaunchProgress, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));

        Composite composite_StageImages = new Composite(composite, SWT.NONE);
        composite_StageImages.setLayout(new RowLayout(SWT.HORIZONTAL));

        mImageLblPhoneVM = new Label(composite_StageImages, SWT.NONE);
        mImageLblPhoneVM.setImage(SWTResourceManager.getImage(LaunchProgressDialog.class,
                "/com/microsoft/projecta/tools/ui/res/skipped.png"));

        mImageLblDeviceConn = new Label(composite_StageImages, SWT.NONE);
        mImageLblDeviceConn.setImage(SWTResourceManager.getImage(LaunchProgressDialog.class,
                "/com/microsoft/projecta/tools/ui/res/pending.png"));

        mImageLblApkInjection = new Label(composite_StageImages, SWT.NONE);
        mImageLblApkInjection.setImage(SWTResourceManager.getImage(LaunchProgressDialog.class,
                "/com/microsoft/projecta/tools/ui/res/pending.png"));

        mImageLblAppInstall = new Label(composite_StageImages, SWT.NONE);
        mImageLblAppInstall.setImage(SWTResourceManager.getImage(LaunchProgressDialog.class,
                "/com/microsoft/projecta/tools/ui/res/pending.png"));

        mImageLblAppLaunch = new Label(composite_StageImages, SWT.NONE);
        mImageLblAppLaunch.setImage(SWTResourceManager.getImage(LaunchProgressDialog.class,
                "/com/microsoft/projecta/tools/ui/res/pending.png"));

        mImageLblScreenShot = new Label(composite_StageImages, SWT.NONE);
        mImageLblScreenShot.setImage(SWTResourceManager.getImage(LaunchProgressDialog.class,
                "/com/microsoft/projecta/tools/ui/res/skipped.png"));

        mImageLblKillApp = new Label(composite_StageImages, SWT.NONE);
        mImageLblKillApp.setImage(SWTResourceManager.getImage(LaunchProgressDialog.class,
                "/com/microsoft/projecta/tools/ui/res/pending.png"));

        Composite composite_Stages = new Composite(composite, SWT.NONE);
        composite_Stages.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        composite_Stages.setLayout(new RowLayout(SWT.HORIZONTAL));

        Label lblPhoneVM = new Label(composite_Stages, SWT.NONE);
        lblPhoneVM.setLayoutData(new RowData(64, 64));
        lblPhoneVM.setAlignment(SWT.CENTER);
        lblPhoneVM.setText("Phone VM");

        Label lblDeviceConn = new Label(composite_Stages, SWT.NONE);
        lblDeviceConn.setAlignment(SWT.CENTER);
        lblDeviceConn.setLayoutData(new RowData(64, 64));
        lblDeviceConn.setText("Device\r\nConn");

        Label lblApkInjection = new Label(composite_Stages, SWT.NONE);
        lblApkInjection.setLayoutData(new RowData(64, 64));
        lblApkInjection.setText("Apk\r\nInjection");
        lblApkInjection.setAlignment(SWT.CENTER);

        Label lblAppInstall = new Label(composite_Stages, SWT.NONE);
        lblAppInstall.setLayoutData(new RowData(64, 64));
        lblAppInstall.setText("App\r\nInstall");
        lblAppInstall.setAlignment(SWT.CENTER);

        Label lblAppLaunch = new Label(composite_Stages, SWT.NONE);
        lblAppLaunch.setLayoutData(new RowData(64, 64));
        lblAppLaunch.setText("App\r\nLaunch");
        lblAppLaunch.setAlignment(SWT.CENTER);

        Label lblScreenShot = new Label(composite_Stages, SWT.NONE);
        lblScreenShot.setLayoutData(new RowData(64, 64));
        lblScreenShot.setText("Screen\r\nShot");
        lblScreenShot.setAlignment(SWT.CENTER);

        Label lblKillApp = new Label(composite_Stages, SWT.NONE);
        lblKillApp.setLayoutData(new RowData(64, 64));
        lblKillApp.setText("Kill\r\nApp");
        lblKillApp.setAlignment(SWT.CENTER);

        Label lblStepProgress = new Label(shlLaunchProgress, SWT.NONE);
        lblStepProgress.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        lblStepProgress.setText("Progress:");

        progressBarStage = new ProgressBar(shlLaunchProgress, SWT.NONE);
        progressBarStage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

        Label lblOutput = new Label(shlLaunchProgress, SWT.NONE);
        lblOutput.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));
        lblOutput.setText("Output");

        textOutput = new Text(shlLaunchProgress, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
        textOutput.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        Composite composite_BottomButtons = new Composite(shlLaunchProgress, SWT.NONE);
        composite_BottomButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        GridData gd_composite_BottomButtons = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
        gd_composite_BottomButtons.heightHint = 34;
        composite_BottomButtons.setLayoutData(gd_composite_BottomButtons);

        mBtnCancel = new Button(composite_BottomButtons, SWT.NONE);
        mBtnCancel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                mBtnCancel.setEnabled(false);
                if (!mLaunchManager.isStopped()) {
                    mLaunchManager.cancel();
                } else {
                    shlLaunchProgress.close();
                }
            }
        });
        mBtnCancel.setText("Cancel");

    }

    private void init() {
        // Order matters
        mImageLabels = new Label[] {
                mImageLblPhoneVM, mImageLblDeviceConn, mImageLblApkInjection, mImageLblAppInstall,
                mImageLblAppLaunch, mImageLblScreenShot, mImageLblKillApp
        };
        WorkFlowStatus[] status = WorkFlowStatus.values();
        for (int i = 0; i < mImageLabels.length; i++) {
            Label imageLabel = mImageLabels[i];
            if (mConfig.should(status[i + 1])) {
                // pending
                imageLabel.setImage(StatusImages.PENDING.getImage());
                imageLabel.setData(StatusImages.PENDING);
            } else {
                // skip
                imageLabel.setImage(StatusImages.SKIPPED.getImage());
                imageLabel.setData(StatusImages.SKIPPED);
            }
        }
        mLaunchManager = new FullLaunchManager(mConfig, new WorkFlowProgressListener() {

            @Override
            public void onCompleted(final WorkFlowStage sender, final WorkFlowStatus stage,
                    final WorkFlowResult result) {
                logger.log(Level.FINE, stage.toString() + " completed w/ " + result.toString());
                getParent().getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        // Update status
                        Label imageLabel = mImageLabels[stage.ordinal() - 1];
                        progressBarStage.setSelection(100);
                        if (result == WorkFlowResult.FAILED || result == WorkFlowResult.CANCELLED) {
                            imageLabel.setImage(StatusImages.FAILURE.getImage());
                            imageLabel.setData(StatusImages.FAILURE);
                        } else {
                            imageLabel.setImage(StatusImages.SUCCESS.getImage());
                            imageLabel.setData(StatusImages.SUCCESS);
                        }

                        // make the ending
                        if (result == WorkFlowResult.FAILED || result == WorkFlowResult.CANCELLED
                                || stage == mFinalStage) {
                            MessageBox messageBox = null;
                            int icon = SWT.ICON_INFORMATION;
                            String title = null;
                            String msg = null;
                            mResult = result;
                            if (result == WorkFlowResult.FAILED) {
                                icon = SWT.ICON_ERROR;
                                title = "Something went wrong!";
                                msg = String.format("Something went wrong during %s.",
                                        sender.getName());
                            } else if (result == WorkFlowResult.CANCELLED) {
                                icon = SWT.ICON_WARNING;
                                title = "Cancelled.";
                                msg = String.format("%s got cancelled by user.", sender.getName());
                            } else if (stage == mFinalStage) {
                                icon = SWT.ICON_INFORMATION;
                                title = "Apk Launch Process Succeeded!";
                                msg = title;
                            }
                            if (title != null) {
                                messageBox = new MessageBox(shlLaunchProgress, icon | SWT.YES
                                        | SWT.NO);
                                messageBox.setText(title);
                                if (result.hasReason()) {
                                    messageBox.setMessage(result.getReason());
                                } else {
                                    messageBox.setMessage(msg
                                            + "\nDo you want to close progress dialog?");
                                }
                                if (messageBox.open() == SWT.YES) {
                                    shlLaunchProgress.close();
                                } else {
                                    mBtnCancel.setEnabled(true);
                                    mBtnCancel.setText("Close");
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onLogOutput(final WorkFlowStage sender, final String message) {
                logger.log(Level.FINE, message);
                getParent().getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        textOutput.append(String.format("[%s] %s\n", sender.getName(), message));
                    }
                });
            }

            @Override
            public void onProgress(WorkFlowStage sender, final WorkFlowStatus stage,
                    final int progress) {
                getParent().getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        Label imageLabel = mImageLabels[stage.ordinal() - 1];
                        boolean shouldUpdate = imageLabel.getData() != StatusImages.RUNNING;
                        imageLabel.setImage(StatusImages.RUNNING.getImage());
                        imageLabel.setData(StatusImages.RUNNING);

                        if (progress > progressBarStage.getSelection() || shouldUpdate) {
                            progressBarStage.setSelection(progress);
                        }
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
        return mResult;
    }
}
