
package com.microsoft.projecta.tools.ui;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.microsoft.projecta.tools.common.Utils;
import com.microsoft.projecta.tools.config.Branch;
import com.microsoft.projecta.tools.config.LaunchConfig;
import com.microsoft.projecta.tools.config.SdkType;

public class LaunchConfigSettings extends Dialog {

    protected Shell mShell;
    private Display mDisplay;
    private LaunchConfig mConfig;
    private Text mTextArtBuildDrop;
    private Text mTextSdkDrop;
    private Text mTextInjectionScripts;
    private Label mLabelApkPackageName;
    private Combo mComboActivities;
    private Combo mComboSdkType;
    private Text mTextPhoneBuildDrop;
    private Button btnPhoneVmProvisioning;
    private Button btnApkInjection;
    private Button btnDeviceConnection;
    private Button btnApkInstallation;
    private Button btnApkLaunch;
    private Button btnTakeScreenshots;
    private Button btnKillAfterLaunched;

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
                if (mConfig.shouldProvisionVM() != btnPhoneVmProvisioning.getSelection()) {
                    btnPhoneVmProvisioning.setSelection(mConfig.shouldProvisionVM());
                }
                if (mConfig.shouldInject() != btnApkInjection.getSelection()) {
                    btnApkInjection.setSelection(mConfig.shouldInject());
                }
                if (mConfig.shouldTakeSnapshot() != btnTakeScreenshots.getSelection()) {
                    btnTakeScreenshots.setSelection(mConfig.shouldTakeSnapshot());
                }
                if (mConfig.shouldKillApp() != btnKillAfterLaunched.getSelection()) {
                    btnKillAfterLaunched.setSelection(mConfig.shouldKillApp());
                }
                if (!mConfig.getArtBuildDropPath().equals(mTextArtBuildDrop.getText())) {
                    mTextArtBuildDrop.setText(mConfig.getArtBuildDropPath());
                }
                if (!mConfig.getPhoneBuildDropVhdPath().equals(mTextPhoneBuildDrop.getText())) {
                    mTextPhoneBuildDrop.setText(mConfig.getPhoneBuildDropVhdPath());
                }
                if (!mConfig.getSdkToolsPath().equals(mTextSdkDrop.getText())) {
                    mTextSdkDrop.setText(mConfig.getSdkToolsPath());
                }
                if (!mConfig.getInjectionScriptPath().equals(mTextInjectionScripts.getText())) {
                    mTextInjectionScripts.setText(mConfig.getInjectionScriptPath());
                }
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

        syncConfigToUI();

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
        mShell = new Shell(getParent(), SWT.BORDER | SWT.RESIZE | SWT.TITLE | SWT.CLOSE);
        mShell.setSize(611, 529);
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
        mComboActivities.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String activity = mComboActivities.getItem(mComboActivities.getSelectionIndex());
                mConfig.setStartupActivity(activity);
                syncConfigToUI();
            }
        });
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
                Combo c = (Combo) e.widget;
                changeBranch(Branch.valueOf(c.getItem(c.getSelectionIndex())), false);
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

        btnPhoneVmProvisioning = new Button(grpWorkFlow, SWT.CHECK);
        btnPhoneVmProvisioning.setEnabled(false);
        btnPhoneVmProvisioning.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mConfig.setShouldProvisionVM(((Button) e.widget).getSelection());
            }
        });
        btnPhoneVmProvisioning.setText("Phone VM Provisioning");

        btnApkInjection = new Button(grpWorkFlow, SWT.CHECK);
        btnApkInjection.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mConfig.setShouldInject(((Button) e.widget).getSelection());
            }
        });
        btnApkInjection.setSelection(true);
        btnApkInjection.setText("Apk Injection");

        btnDeviceConnection = new Button(grpWorkFlow, SWT.CHECK);
        btnDeviceConnection.setEnabled(false);
        btnDeviceConnection.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            }
        });
        btnDeviceConnection.setSelection(true);
        btnDeviceConnection.setText("Device Connection");

        btnApkInstallation = new Button(grpWorkFlow, SWT.CHECK);
        btnApkInstallation.setEnabled(false);
        btnApkInstallation.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            }
        });
        btnApkInstallation.setSelection(true);
        btnApkInstallation.setText("Apk Installation");

        btnApkLaunch = new Button(grpWorkFlow, SWT.CHECK);
        btnApkLaunch.setEnabled(false);
        btnApkLaunch.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            }
        });
        btnApkLaunch.setSelection(true);
        btnApkLaunch.setText("Apk Launch");

        btnTakeScreenshots = new Button(grpWorkFlow, SWT.CHECK);
        btnTakeScreenshots.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mConfig.setShouldTakeSnapshot(((Button) e.widget).getSelection());
            }
        });
        btnTakeScreenshots.setText("Take screenshots");
        btnTakeScreenshots.setEnabled(false);

        btnKillAfterLaunched = new Button(grpWorkFlow, SWT.CHECK);
        btnKillAfterLaunched.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mConfig.setShouldKillApp(((Button) e.widget).getSelection());
            }
        });
        btnKillAfterLaunched.setText("Kill After Launched");

        Group grpPaths = new Group(composite_inner, SWT.NONE);
        grpPaths.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpPaths.setText("Paths");
        grpPaths.setLayout(new FillLayout(SWT.HORIZONTAL));

        Composite compositePaths = new Composite(grpPaths, SWT.NONE);
        compositePaths.setLayout(new GridLayout(2, false));

        mTextArtBuildDrop = new Text(compositePaths, SWT.BORDER);
        mTextArtBuildDrop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        mTextArtBuildDrop.setSize(467, 31);
        mTextArtBuildDrop.setText("( loading... )");
        mTextArtBuildDrop.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                mConfig.setArtBuildDropPath(((Text) e.widget).getText());
                syncConfigToUI();
            }
        });

        Button btnArtBuildDrop = new Button(compositePaths, SWT.NONE);
        btnArtBuildDrop.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String buildDropPath = Utils.pickDirectory("Pick the build drop dir",
                        "Select a folder either from nightly build or your aosp output folder.",
                        mConfig.getArtBuildDropPath(), mShell);
                if (buildDropPath != null) {
                    mTextArtBuildDrop.setText(buildDropPath);
                }
            }
        });
        btnArtBuildDrop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnArtBuildDrop.setSize(204, 35);
        btnArtBuildDrop.setText("ART Build");

        mTextSdkDrop = new Text(compositePaths, SWT.BORDER);
        mTextSdkDrop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        mTextSdkDrop.setText("( loading... )");
        mTextSdkDrop.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                mConfig.setSdkToolsPath(((Text) e.widget).getText());
                syncConfigToUI();
            }
        });
        mTextSdkDrop.setBounds(0, 0, 467, 31);

        Button btnSdkDrop = new Button(compositePaths, SWT.NONE);
        btnSdkDrop.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String sdkToolPath = Utils.pickDirectory("Pick the sdk tool dir",
                        "Select a folder with Project A sdk tools.", mConfig.getSdkToolsPath(),
                        mShell);
                if (sdkToolPath != null) {
                    mTextArtBuildDrop.setText(sdkToolPath);
                }
            }
        });
        btnSdkDrop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnSdkDrop.setText("Sdk Drop");
        btnSdkDrop.setBounds(0, 0, 97, 35);

        mTextInjectionScripts = new Text(compositePaths, SWT.BORDER);
        mTextInjectionScripts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        mTextInjectionScripts.setText("( loading... )");
        mTextInjectionScripts.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                mConfig.setInjectionScriptPath(((Text) e.widget).getText());
                syncConfigToUI();
            }
        });
        mTextInjectionScripts.setBounds(0, 0, 467, 31);

        Button btnInjectionScripts = new Button(compositePaths, SWT.NONE);
        btnInjectionScripts.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String injectScriptPath = Utils.pickDirectory("Pick the injection path",
                        "Select a folder with injection scripts and related tools.",
                        mConfig.getInjectionScriptPath(), mShell);
                if (injectScriptPath != null) {
                    mTextInjectionScripts.setText(injectScriptPath);
                }
            }
        });
        btnInjectionScripts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnInjectionScripts.setText("Inject Scripts");
        btnInjectionScripts.setBounds(0, 0, 88, 35);

        mTextPhoneBuildDrop = new Text(compositePaths, SWT.BORDER);
        mTextPhoneBuildDrop.setText("( loading... )");
        mTextPhoneBuildDrop.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                mConfig.setPhoneBuildDropVhdPath(((Text) e.widget).getText());
                syncConfigToUI();
            }
        });
        mTextPhoneBuildDrop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Button btnPhoneBuild = new Button(compositePaths, SWT.NONE);
        btnInjectionScripts.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String phoneBuildDropPath = Utils
                        .pickDirectory(
                                "Pick the phone build path",
                                "Select a folder either from daily build or your phone build output which contains flash.vhd.",
                                mConfig.getPhoneBuildDropVhdPath(), mShell);
                if (phoneBuildDropPath != null) {
                    mTextPhoneBuildDrop.setText(phoneBuildDropPath);
                }
            }
        });
        btnPhoneBuild.setText("Phone Build");

        scrolledComposite.setContent(composite_inner);
        scrolledComposite.setMinSize(composite_inner.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    }

    protected void changeBranch(Branch valueOf, boolean b) {
        // TODO Auto-generated method stub

    }
}
