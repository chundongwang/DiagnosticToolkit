package com.microsoft.projecta.tools;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Composite;
import swing2swt.layout.FlowLayout;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.custom.CCombo;

public class ApkLauncher {

    protected Shell shlDiagnosticLauncher;
    private Text textApkPath;
    private Text textOutDir;
    private Text textBuildDrop;

    /**
     * Launch the application.
     * @param args
     */
    public static void main(String[] args) {
        try {
            ApkLauncher window = new ApkLauncher();
            window.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Open the window.
     */
    public void open() {
        Display display = Display.getDefault();
        createContents();
        textBuildDrop.setText("hellow");
        shlDiagnosticLauncher.open();
        shlDiagnosticLauncher.layout();
        while (!shlDiagnosticLauncher.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * Create contents of the window.
     */
    protected void createContents() {
        shlDiagnosticLauncher = new Shell();
        shlDiagnosticLauncher.setSize(450, 576);
        shlDiagnosticLauncher.setText("Diagnostic Launcher");
        shlDiagnosticLauncher.setLayout(new GridLayout(1, false));
        
        Group grpInout = new Group(shlDiagnosticLauncher, SWT.NONE);
        grpInout.setLayout(new GridLayout(1, false));
        grpInout.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        grpInout.setText("In/Out");
        grpInout.setBounds(0, 0, 70, 82);
        
        Composite composite_inout_apk = new Composite(grpInout, SWT.NONE);
        composite_inout_apk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        composite_inout_apk.setLayout(new FormLayout());
        
        textApkPath = new Text(composite_inout_apk, SWT.BORDER);
        FormData fd_textApkPath = new FormData();
        fd_textApkPath.top = new FormAttachment(0);
        fd_textApkPath.left = new FormAttachment(0);
        textApkPath.setLayoutData(fd_textApkPath);
        
        Button btnApkFinder = new Button(composite_inout_apk, SWT.NONE);
        fd_textApkPath.right = new FormAttachment(btnApkFinder, -6);
        fd_textApkPath.bottom = new FormAttachment(btnApkFinder, 0, SWT.BOTTOM);
        FormData fd_btnApkFinder = new FormData();
        fd_btnApkFinder.width = 80;
        fd_btnApkFinder.top = new FormAttachment(0);
        fd_btnApkFinder.right = new FormAttachment(100);
        fd_btnApkFinder.bottom = new FormAttachment(100);
        btnApkFinder.setLayoutData(fd_btnApkFinder);
        btnApkFinder.setText("Original Apk");
        
        Composite composite_inout_outdir = new Composite(grpInout, SWT.NONE);
        composite_inout_outdir.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        composite_inout_outdir.setBounds(0, 0, 64, 64);
        composite_inout_outdir.setLayout(new FormLayout());
        
        textOutDir = new Text(composite_inout_outdir, SWT.BORDER);
        FormData fd_textOutDir = new FormData();
        fd_textOutDir.top = new FormAttachment(0);
        fd_textOutDir.left = new FormAttachment(0);
        textOutDir.setLayoutData(fd_textOutDir);
        
        Button btnOutDirFinder = new Button(composite_inout_outdir, SWT.NONE);
        fd_textOutDir.right = new FormAttachment(btnOutDirFinder, -6);
        fd_textOutDir.bottom = new FormAttachment(btnOutDirFinder, 0, SWT.BOTTOM);
        FormData fd_btnOutDirFinder = new FormData();
        fd_btnOutDirFinder.width = 80;
        fd_btnOutDirFinder.top = new FormAttachment(0);
        fd_btnOutDirFinder.right = new FormAttachment(100);
        fd_btnOutDirFinder.bottom = new FormAttachment(100);
        btnOutDirFinder.setLayoutData(fd_btnOutDirFinder);
        btnOutDirFinder.setText("Output Dir");
        
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
        
        ScrolledComposite scrolledComposite_basic = new ScrolledComposite(composite_basic, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        scrolledComposite_basic.setExpandHorizontal(true);
        scrolledComposite_basic.setExpandVertical(true);
        
        Composite composite_basic_inner = new Composite(scrolledComposite_basic, SWT.NONE);
        composite_basic_inner.setLayout(new GridLayout(2, false));
        
        textBuildDrop = new Text(composite_basic_inner, SWT.BORDER);
        textBuildDrop.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        
        Button btnBuildFinder = new Button(composite_basic_inner, SWT.NONE);
        btnBuildFinder.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));
        btnBuildFinder.setText("Build Drop");
        
        Combo comboFlavor = new Combo(composite_basic_inner, SWT.BORDER);
        GridData gd_comboFlavor = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
        gd_comboFlavor.widthHint = 264;
        comboFlavor.setLayoutData(gd_comboFlavor);
        String[] flavorItems = {"GP Interop", "GP Interop (stubbed)"};
        comboFlavor.setItems(flavorItems);
        comboFlavor.select(0);
        
        Combo comboDevice = new Combo(composite_basic_inner, SWT.BORDER);
        comboDevice.setItems(new String[] {"127.0.0.1"});
        comboDevice.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
        comboDevice.select(0);
        scrolledComposite_basic.setContent(composite_basic_inner);
        scrolledComposite_basic.setMinSize(composite_basic_inner.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        
        TabItem tbtmAdvance = new TabItem(tabFolder, SWT.NONE);
        tbtmAdvance.setText("Advance");
        
        Composite composite_button_panel = new Composite(shlDiagnosticLauncher, SWT.NONE);
        composite_button_panel.setLayout(new FormLayout());
        composite_button_panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        
        Button btnGo = new Button(composite_button_panel, SWT.CENTER);
        btnGo.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
        FormData fd_btnGo = new FormData();
        fd_btnGo.height = 35;
        fd_btnGo.bottom = new FormAttachment(100);
        fd_btnGo.right = new FormAttachment(100);
        fd_btnGo.width = 50;
        btnGo.setLayoutData(fd_btnGo);
        btnGo.setText("Go!");

    }
}
