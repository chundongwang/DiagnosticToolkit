
package com.microsoft.projecta.tools.ui;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.microsoft.projecta.tools.config.Branch;
import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.config.SdkType;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.GridData;

import swing2swt.layout.FlowLayout;

import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class LaunchConfigSettings extends Dialog {

    protected Shell mShell;
    private Display mDisplay;
    private LaunchConfig mConfig;
    private Text mTextBuildDrop;
    private Text mTextSdkDrop;
    private Text mTextInjectionScripts;
    private Label mLabelApkPackageName;
    private Combo mComboActivities;
    private Combo mComboSdkType;

    /**
     * Create the dialog.
     * 
     * @param parent
     * @param config
     */
    public LaunchConfigSettings(Shell parent, LaunchConfig config) {
        super(parent);
        mConfig = config;
    }

    private void syncConfigToUI() {
        mDisplay.asyncExec(new Runnable() {
            @Override
            public void run() {
                if (mConfig.hasApkPackageInfo()) {
                    mLabelApkPackageName.setText(mConfig.getApkPackageName());
                    List<String> activities = mConfig.getApkActivities();
                    String startupActivity = mConfig.getActivityToLaunch();
                    // reload activities and select the main one
                    if (mComboActivities.getItemCount() != activities.size()) {
                        mComboActivities.removeAll();
                        int index = -1;
                        for (String act : activities) {
                            mComboActivities.add(act);
                            if (act.equals(startupActivity)) {
                                index = mComboActivities.getItemCount() - 1;
                            }
                        }
                        mComboActivities.select(index);
                    }
                }
                if (mComboSdkType.getSelectionIndex() != mConfig.getSdkType().ordinal()) {
                    mComboSdkType.select(mConfig.getSdkType().ordinal());
                }
            }
        });
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
        mDisplay = getParent().getDisplay();
        while (!mShell.isDisposed()) {
            if (!mDisplay.readAndDispatch()) {
                mDisplay.sleep();
            }
        }
        return mConfig;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        mShell = new Shell(getParent(), SWT.BORDER | SWT.RESIZE
                | SWT.TITLE | SWT.CLOSE);
        mShell.setSize(611, 591);
        mShell.setText("Launch Configuration");
        mShell.setLayout(new FillLayout(SWT.HORIZONTAL));

        ScrolledComposite scrolledComposite = new ScrolledComposite(mShell, SWT.BORDER
                | SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setMinSize(new Point(309, 228));

        Composite composite_inner = new Composite(scrolledComposite, SWT.NONE);
        composite_inner.setLayout(new GridLayout(1, false));

        Group grpBasic = new Group(composite_inner, SWT.NONE);
        grpBasic.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpBasic.setText("Basic");
        grpBasic.setLayout(new FillLayout(SWT.HORIZONTAL));

        Composite compositeBasic = new Composite(grpBasic, SWT.NONE);
        compositeBasic.setBounds(0, 0, 64, 64);
        compositeBasic.setLayout(new GridLayout(1, false));

        mLabelApkPackageName = new Label(compositeBasic, SWT.NONE);
        mLabelApkPackageName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        mLabelApkPackageName.setSize(204, 25);
        mLabelApkPackageName.setText("( No Apk selected )");

        mComboActivities = new Combo(compositeBasic, SWT.READ_ONLY);
        mComboActivities.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        mComboActivities.setSize(99, 33);
        
        Label lblSdkType = new Label(compositeBasic, SWT.NONE);
        lblSdkType.setText("SDK Type:");

        mComboSdkType = new Combo(compositeBasic, SWT.READ_ONLY);
        mComboSdkType.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mConfig.setSdkType(SdkType.ValueOf(((Combo) e.widget).getSelectionIndex()));
                syncConfigToUI();
            }
        });
        mComboSdkType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        mComboSdkType.setSize(408, 33);
        mComboSdkType.setItems(new String[] {
                "GP Interop", "Stubbed Interop", "Analytics V2 Interop"
        });
        mComboSdkType.select(0);
        
        Label lblBranch = new Label(compositeBasic, SWT.NONE);
        lblBranch.setText("Branch:");

        Combo comboBranch = new Combo(compositeBasic, SWT.READ_ONLY);
        comboBranch.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Combo c = (Combo)e.widget;
                changeBranch(Branch.valueOf(c.getItem(c.getSelectionIndex())),
                        false);
            }
        });
        comboBranch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        comboBranch.setSize(204, 33);
        comboBranch.setItems(new String[] {
                "Develop", "Master"
        });
        comboBranch.select(0);

        Group grpWorkFlow = new Group(composite_inner, SWT.NONE);
        grpWorkFlow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpWorkFlow.setText("Work Flow");
        RowLayout rl_grpWorkFlow = new RowLayout(SWT.VERTICAL);
        grpWorkFlow.setLayout(rl_grpWorkFlow);

        Button btnPhoneVmProvisioning = new Button(grpWorkFlow, SWT.CHECK);
        btnPhoneVmProvisioning.setText("Phone VM Provisioning");
        btnPhoneVmProvisioning.setEnabled(false);

        Button btnApkInjection = new Button(grpWorkFlow, SWT.CHECK);
        btnApkInjection.setText("Apk Injection");

        Button btnDeviceConnection = new Button(grpWorkFlow, SWT.CHECK);
        btnDeviceConnection.setText("Device Connection");

        Button btnApkInstallation = new Button(grpWorkFlow, SWT.CHECK);
        btnApkInstallation.setText("Apk Installation");

        Button btnApkLaunch = new Button(grpWorkFlow, SWT.CHECK);
        btnApkLaunch.setText("Apk Launch");

        Button btnTakeScreenshots = new Button(grpWorkFlow, SWT.CHECK);
        btnTakeScreenshots.setText("Take screenshots");
        btnTakeScreenshots.setEnabled(false);

        Button btnKillAfterLaunched = new Button(grpWorkFlow, SWT.CHECK);
        btnKillAfterLaunched.setText("Kill After Launched");

        Group grpPaths = new Group(composite_inner, SWT.NONE);
        grpPaths.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpPaths.setText("Paths");
        grpPaths.setLayout(new FillLayout(SWT.HORIZONTAL));

        Composite compositePaths = new Composite(grpPaths, SWT.NONE);
        compositePaths.setLayout(new GridLayout(2, false));

        mTextBuildDrop = new Text(compositePaths, SWT.BORDER);
        mTextBuildDrop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        mTextBuildDrop.setSize(467, 31);
        mTextBuildDrop.setText("( loading... )");

        Button btnBuildDrop = new Button(compositePaths, SWT.NONE);
        btnBuildDrop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnBuildDrop.setSize(204, 35);
        btnBuildDrop.setText("Build Drop");

        mTextSdkDrop = new Text(compositePaths, SWT.BORDER);
        mTextSdkDrop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        mTextSdkDrop.setText("( loading... )");
        mTextSdkDrop.setBounds(0, 0, 467, 31);

        Button btnSdkDrop = new Button(compositePaths, SWT.NONE);
        btnSdkDrop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnSdkDrop.setText("Sdk Drop");
        btnSdkDrop.setBounds(0, 0, 97, 35);

        mTextInjectionScripts = new Text(compositePaths, SWT.BORDER);
        mTextInjectionScripts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        mTextInjectionScripts.setText("( loading... )");
        mTextInjectionScripts.setBounds(0, 0, 467, 31);

        Button btnInjectionScripts = new Button(compositePaths, SWT.NONE);
        btnInjectionScripts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnInjectionScripts.setText("Inject Scripts");
        btnInjectionScripts.setBounds(0, 0, 88, 35);

        scrolledComposite.setContent(composite_inner);
        scrolledComposite.setMinSize(composite_inner.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    }

    protected void changeBranch(Branch valueOf, boolean b) {
        // TODO Auto-generated method stub
        
    }
}
