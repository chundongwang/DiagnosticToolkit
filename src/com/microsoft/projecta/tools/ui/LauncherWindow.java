
package com.microsoft.projecta.tools.ui;

import java.io.IOException;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.xmlpull.v1.XmlPullParserException;

import com.microsoft.projecta.tools.common.AndroidManifestInfo;
import com.microsoft.projecta.tools.config.Branch;
import com.microsoft.projecta.tools.config.LaunchConfig;

public class LauncherWindow {

    /**
     * Launch the application.
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            LauncherWindow window = new LauncherWindow();
            window.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Shell shlDiagnosticLauncher;
    private Display display;
    private Text mTextOriginApkPath;

    private Text mTextOutputDir;
    private LaunchConfig mConfig;
    private Text mTextBuildDrop;
    private Text mTextTakehomePath;
    private Text mTextSdkToolsPath;
    private Text mTextInjectionScriptPath;
    private Combo mComboDevice;
    private Button mBtnProvisionVm;
    private Button mBtnInjectApk;
    private Button mBtnTakeScreenshot;
    private Combo mComboActivities;
    private Label mLblPackagename;
    private Button mBtnKillApp;

    private Button mBtnGo;

    /**
     * Change branch to specified one which might cause reloading all default configs. If we decided
     * to reload default config, another thread will be spin'ed off to serve it and asyncExec back
     * to UI thread to update the related controls.
     * 
     * @param branch The branch to be switched to.
     * @param suppressConfirmDialog If we shall prompt a dialog to ask user
     */
    private void changeBranch(final Branch branch, boolean suppressConfirmDialog) {
        int response = SWT.YES;

        if (!suppressConfirmDialog) {
            MessageBox messageBox = new MessageBox(shlDiagnosticLauncher, SWT.ICON_QUESTION
                    | SWT.YES | SWT.NO);
            messageBox.setText("Change Branch");
            messageBox.setMessage("You've just changed branch to " + branch
                    + ". \nDo you want to reload default config for build drop, etc.?");
            response = messageBox.open();
        }

        if (response == SWT.YES) {
            // on UI thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final LaunchConfig config = new LaunchConfig.Builder(branch).build();
                    display.asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            syncConfigToUI(config);
                        }
                    });
                }
            }).start();
        }
    }

    /**
     * Create contents of the window.
     */
    protected void createContents() {
        shlDiagnosticLauncher = new Shell();
        shlDiagnosticLauncher.setMinimumSize(new Point(450, 576));
        shlDiagnosticLauncher.setSize(501, 620);
        shlDiagnosticLauncher.setText("Diagnostic Launcher");
        shlDiagnosticLauncher.setLayout(new GridLayout(1, false));

        Group grpInout = new Group(shlDiagnosticLauncher, SWT.NONE);
        grpInout.setLayout(new GridLayout(2, false));
        grpInout.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        grpInout.setText("In/Out");
        grpInout.setBounds(0, 0, 70, 82);

        mTextOriginApkPath = new Text(grpInout, SWT.BORDER);
        mTextOriginApkPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Button btnOriginApk = new Button(grpInout, SWT.NONE);
        btnOriginApk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent evt) {
                FileDialog openApkFileDialog = new FileDialog(shlDiagnosticLauncher, SWT.OPEN);
                openApkFileDialog.setText("Find the original Apk");
                openApkFileDialog.setFilterPath(System.getProperty("user.dir"));
                String[] filterExt = {
                    "*.apk"
                };
                openApkFileDialog.setFilterExtensions(filterExt);
                String originApkPath = openApkFileDialog.open();
                if (originApkPath != null) {
                    mConfig.setOriginApkPath(originApkPath);
                    try {
                        AndroidManifestInfo info = AndroidManifestInfo.parseAndroidManifest(mConfig
                                .getOriginApkPath());
                        mConfig.setApkPackageInfo(info);
                    } catch (IOException | XmlPullParserException e) {
                        System.err.println("AndroidManifest parsing failed...");
                        e.printStackTrace(System.err);
                        throw new RuntimeException("AndroidManifest parsing failed.", e);
                    }
                    syncConfigToUI();
                }
            }
        });
        btnOriginApk.setText("Origin Apk");

        mTextOutputDir = new Text(grpInout, SWT.BORDER);
        mTextOutputDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Button btnOutputDir = new Button(grpInout, SWT.NONE);
        btnOutputDir.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String outdirPath = pickDirectory(
                        "Pick the output dir",
                        "Select a folder for drop injected apk, various log files and screen shot images.",
                        mConfig.getOutdirPath());
                if (outdirPath != null) {
                    mConfig.setOutdirPath(outdirPath);
                    syncConfigToUI();
                }
            }
        });
        btnOutputDir.setText("Output Dir");

        Group grpSettings = new Group(shlDiagnosticLauncher, SWT.NONE);
        grpSettings.setLayout(new FillLayout(SWT.VERTICAL));
        GridData gd_grpSettings = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gd_grpSettings.heightHint = 370;
        grpSettings.setLayoutData(gd_grpSettings);
        grpSettings.setText("Settings");

        TabFolder tabFolder = new TabFolder(grpSettings, SWT.NONE);

        TabItem tbtmBasic = new TabItem(tabFolder, SWT.NONE);
        tbtmBasic.setText("Basic");

        Composite composite_basic = new Composite(tabFolder, SWT.NONE);
        tbtmBasic.setControl(composite_basic);
        composite_basic.setLayout(new FillLayout(SWT.HORIZONTAL));

        ScrolledComposite scrolledComposite_basic = new ScrolledComposite(composite_basic,
                SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite_basic.setExpandHorizontal(true);
        scrolledComposite_basic.setExpandVertical(true);

        Composite composite_basic_inner = new Composite(scrolledComposite_basic, SWT.NONE);
        composite_basic_inner.setLayout(new GridLayout(2, false));

        final Combo comboBranch = new Combo(composite_basic_inner, SWT.READ_ONLY);
        comboBranch.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                changeBranch(Branch.valueOf(comboBranch.getItem(comboBranch.getSelectionIndex())),
                        false);
            }
        });
        comboBranch.setItems(new String[] {
                "Develop", "Master"
        });
        comboBranch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        comboBranch.select(0);

        mTextBuildDrop = new Text(composite_basic_inner, SWT.BORDER);
        mTextBuildDrop.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

        Button btnBuildFinder = new Button(composite_basic_inner, SWT.NONE);
        btnBuildFinder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String buildDropPath = pickDirectory("Pick the build drop dir",
                        "Select a folder either from nightly build or your aosp output folder.",
                        mConfig.getBuildDropPath());
                if (buildDropPath != null) {
                    mConfig.setBuildDropPath(buildDropPath);
                    syncConfigToUI();
                }
            }
        });
        btnBuildFinder.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));
        btnBuildFinder.setText("Build Drop");

        Combo comboFlavor = new Combo(composite_basic_inner, SWT.READ_ONLY);
        GridData gd_comboFlavor = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
        gd_comboFlavor.widthHint = 264;
        comboFlavor.setLayoutData(gd_comboFlavor);
        String[] flavorItems = {
                "GP Interop", "GP Interop (stubbed)"
        };
        comboFlavor.setItems(flavorItems);
        comboFlavor.select(0);

        mComboDevice = new Combo(composite_basic_inner, SWT.BORDER);
        mComboDevice.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                mConfig.setDeviceIPAddr(mComboDevice.getText());
                syncConfigToUI();
            }
        });
        mComboDevice.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mConfig.setDeviceIPAddr(mComboDevice.getText());
                syncConfigToUI();
            }
        });
        mComboDevice.setItems(new String[] {
            "127.0.0.1"
        });
        mComboDevice.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        mComboDevice.select(0);

        mLblPackagename = new Label(composite_basic_inner, SWT.NONE);
        GridData gd_mLblPackagename = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
        gd_mLblPackagename.heightHint = 21;
        gd_mLblPackagename.horizontalIndent = 5;
        gd_mLblPackagename.verticalIndent = 5;
        mLblPackagename.setLayoutData(gd_mLblPackagename);
        mLblPackagename.setText("( No Apk selected )");

        mComboActivities = new Combo(composite_basic_inner, SWT.READ_ONLY);
        mComboActivities.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String activity = mComboActivities.getItem(mComboActivities.getSelectionIndex());
                mConfig.setStartupActivity(activity);
                syncConfigToUI();
            }
        });
        mComboActivities.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        scrolledComposite_basic.setContent(composite_basic_inner);
        scrolledComposite_basic.setMinSize(composite_basic_inner.computeSize(SWT.DEFAULT,
                SWT.DEFAULT));

        TabItem tbtmAdvance = new TabItem(tabFolder, SWT.NONE);
        tbtmAdvance.setText("Advance");

        Composite composite_advance = new Composite(tabFolder, SWT.NONE);
        tbtmAdvance.setControl(composite_advance);
        composite_advance.setLayout(new FillLayout(SWT.HORIZONTAL));

        ScrolledComposite scrolledComposite_advance = new ScrolledComposite(composite_advance,
                SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite_advance.setExpandHorizontal(true);
        scrolledComposite_advance.setExpandVertical(true);

        Composite composite_advance_inner = new Composite(scrolledComposite_advance, SWT.NONE);
        composite_advance_inner.setLayout(new GridLayout(2, false));

        mTextTakehomePath = new Text(composite_advance_inner, SWT.BORDER);
        mTextTakehomePath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Button btnTakehomeScript = new Button(composite_advance_inner, SWT.NONE);
        btnTakehomeScript.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String takehomePath = pickDirectory("Pick the take home dir",
                        "Select a folder with takehome setup scripts.",
                        mConfig.getTakehomeScriptPath());
                if (takehomePath != null) {
                    mConfig.setTakehomeScriptPath(takehomePath);
                    syncConfigToUI();
                }
            }
        });
        btnTakehomeScript.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnTakehomeScript.setText("TakeHome Script");

        mTextSdkToolsPath = new Text(composite_advance_inner, SWT.BORDER);
        mTextSdkToolsPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Button btnSdkTools = new Button(composite_advance_inner, SWT.NONE);
        btnSdkTools.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String sdkToolPath = pickDirectory("Pick the sdk tool dir",
                        "Select a folder with Project A sdk tools.", mConfig.getSdkToolsPath());
                if (sdkToolPath != null) {
                    mConfig.setSdkToolsPath(sdkToolPath);
                    syncConfigToUI();
                }
            }
        });
        btnSdkTools.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnSdkTools.setText("SDK Tools");

        mTextInjectionScriptPath = new Text(composite_advance_inner, SWT.BORDER);
        mTextInjectionScriptPath
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Button btnInjectionScript = new Button(composite_advance_inner, SWT.NONE);
        btnInjectionScript.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String injectScriptPath = pickDirectory("Pick the injection path",
                        "Select a folder with injection scripts and related tools.",
                        mConfig.getInjectionScriptPath());
                if (injectScriptPath != null) {
                    mConfig.setInjectionScriptPath(injectScriptPath);
                    syncConfigToUI();
                }
            }
        });
        btnInjectionScript.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnInjectionScript.setText("Injection Script");

        Composite composite = new Composite(composite_advance_inner, SWT.NONE);
        composite.setLayout(new RowLayout(SWT.HORIZONTAL));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

        mBtnProvisionVm = new Button(composite, SWT.CHECK);
        mBtnProvisionVm.setEnabled(false);
        mBtnProvisionVm.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mConfig.setShouldProvisionVM(((Button) e.widget).getSelection());
            }
        });
        mBtnProvisionVm.setText("Provision VM");

        mBtnInjectApk = new Button(composite, SWT.CHECK);
        mBtnInjectApk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mConfig.setShouldInject(((Button) e.widget).getSelection());
            }
        });
        mBtnInjectApk.setText("Inject Apk");

        mBtnTakeScreenshot = new Button(composite, SWT.CHECK);
        mBtnTakeScreenshot.setEnabled(false);
        mBtnTakeScreenshot.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mConfig.setShouldTakeSnapshot(((Button) e.widget).getSelection());
            }
        });
        mBtnTakeScreenshot.setText("Take screenshot");
        
        mBtnKillApp = new Button(composite, SWT.CHECK);
        mBtnKillApp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mConfig.setShouldKillApp(((Button) e.widget).getSelection());
            }
        });
        mBtnKillApp.setText("Kill After Launch");
        scrolledComposite_advance.setContent(composite_advance_inner);
        scrolledComposite_advance.setMinSize(composite_advance_inner.computeSize(SWT.DEFAULT,
                SWT.DEFAULT));

        Composite composite_button_panel = new Composite(shlDiagnosticLauncher, SWT.NONE);
        composite_button_panel.setLayout(new FormLayout());
        composite_button_panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

        mBtnGo = new Button(composite_button_panel, SWT.CENTER);
        mBtnGo.setEnabled(false);
        mBtnGo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                LaunchProgressDialog dialog = new LaunchProgressDialog(shlDiagnosticLauncher,
                        mConfig);
                dialog.open();
            }
        });
        mBtnGo.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
        FormData fd_btnGo = new FormData();
        fd_btnGo.height = 35;
        fd_btnGo.bottom = new FormAttachment(100);
        fd_btnGo.right = new FormAttachment(100);
        fd_btnGo.width = 50;
        mBtnGo.setLayoutData(fd_btnGo);
        mBtnGo.setText("Go!");

    }

    private void init() {
        // On UI thread
        changeBranch(Branch.Develop, true);
    }

    /**
     * Open the window.
     */
    public void open() {
        display = Display.getDefault();
        createContents();
        init();
        shlDiagnosticLauncher.open();
        shlDiagnosticLauncher.layout();
        while (!shlDiagnosticLauncher.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * Helper to pick a directory
     * 
     * @param title DirectoryDialog title
     * @param msg DirectoryDialog message
     * @param default_value default folder to start with
     * @return folder path user picked
     */
    private String pickDirectory(String title, String msg, String default_value) {
        DirectoryDialog dirPickerDialog = new DirectoryDialog(this.shlDiagnosticLauncher);
        dirPickerDialog.setText(title);
        dirPickerDialog.setMessage(msg);
        if (default_value != null) {
            dirPickerDialog.setFilterPath(default_value);
        }
        return dirPickerDialog.open();
    }

    private void syncConfigToUI() {
        syncConfigToUI(null);
    }

    /**
     * Sync launch config to UI controls
     * 
     * @param config The config to replace stored mConfig. Use null if just want to trigger a sync.
     */
    private void syncConfigToUI(LaunchConfig config) {
        // on UI thread
        if (config != null) {
            mConfig = config;
        }

        mTextBuildDrop.setText(mConfig.getBuildDropPath());
        mTextTakehomePath.setText(mConfig.getTakehomeScriptPath());
        mTextSdkToolsPath.setText(mConfig.getSdkToolsPath());
        mTextInjectionScriptPath.setText(mConfig.getInjectionScriptPath());
        mComboDevice.setText(mConfig.getDeviceIPAddr());
        if (mConfig.hasOriginApkPath()) {
            mTextOriginApkPath.setText(mConfig.getOriginApkPath());
        }
        if (mConfig.hasOutdirPath()) {
            mTextOutputDir.setText(mConfig.getOutdirPath());
        }
        mBtnProvisionVm.setSelection(mConfig.shouldProvisionVM());
        mBtnInjectApk.setSelection(mConfig.shouldInject());
        mBtnTakeScreenshot.setSelection(mConfig.shouldTakeSnapshot());
        mBtnKillApp.setSelection(mConfig.shouldKillApp());
        if (mConfig.hasApkPackageInfo()) {
            mLblPackagename.setText(mConfig.getApkPackageName());
            List<String> activities = mConfig.getApkActivities();
            String startupActivity = mConfig.getActivityToLaunch();
            int index = -1;
            for (String act : activities) {
                mComboActivities.add(act);
                if (act.equals(startupActivity)) {
                    index = mComboActivities.getItemCount()-1;
                }
            }
            mComboActivities.select(index);
        }

        if (mConfig.hasOriginApkPath() && mConfig.hasOutdirPath()) {
            mBtnGo.setEnabled(true);
        }
    }
}
