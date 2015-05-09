package com.microsoft.projecta.tools;

import java.io.File;

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
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;

public class ApkLauncher {

    protected Shell shlDiagnosticLauncher;
    private Text textBuildDrop;
    private Text textOriginApkPath;
    private Text textOutputDir;
    
    private final class ApkLauncherShellAdapter extends ShellAdapter {
    	@Override
    	public void shellActivated(ShellEvent e) {
            textBuildDrop.setText("hellow");
    	}
    }

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
        shlDiagnosticLauncher.addShellListener(new ApkLauncherShellAdapter());
        shlDiagnosticLauncher.setSize(450, 576);
        shlDiagnosticLauncher.setText("Diagnostic Launcher");
        shlDiagnosticLauncher.setLayout(new GridLayout(1, false));
        
        Group grpInout = new Group(shlDiagnosticLauncher, SWT.NONE);
        grpInout.setLayout(new GridLayout(2, false));
        grpInout.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        grpInout.setText("In/Out");
        grpInout.setBounds(0, 0, 70, 82);
        
        textOriginApkPath = new Text(grpInout, SWT.BORDER);
        textOriginApkPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Button btnOriginApk = new Button(grpInout, SWT.NONE);
        btnOriginApk.setText("Origin Apk");
        
        textOutputDir = new Text(grpInout, SWT.BORDER);
        textOutputDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Button btnOutputDir = new Button(grpInout, SWT.NONE);
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
