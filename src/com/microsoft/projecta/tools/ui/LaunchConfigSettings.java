
package com.microsoft.projecta.tools.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.microsoft.projecta.tools.config.LaunchConfig;
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

public class LaunchConfigSettings extends Dialog {

    protected Object mResult;
    protected Shell mShell;
    private LaunchConfig mConfig;
    private Text textBuildDrop;
    private Text textSdkDrop;
    private Text textInjectionScripts;

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
        composite_inner.setLayout(new FillLayout(SWT.VERTICAL));
        
        Group grpBasic = new Group(composite_inner, SWT.NONE);
        grpBasic.setText("Basic");
        grpBasic.setLayout(new FillLayout(SWT.HORIZONTAL));
        
        Composite compositeBasic = new Composite(grpBasic, SWT.NONE);
        compositeBasic.setBounds(0, 0, 64, 64);
        compositeBasic.setLayout(new GridLayout(2, false));
        
                Label labelApkPackageName = new Label(compositeBasic, SWT.NONE);
                labelApkPackageName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
                labelApkPackageName.setSize(204, 25);
                labelApkPackageName.setText("( No Apk selected )");
                new Label(compositeBasic, SWT.NONE);
                
                        Combo comboActivities = new Combo(compositeBasic, SWT.READ_ONLY);
                        comboActivities.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
                        comboActivities.setSize(99, 33);
                        
                                Combo comboSdkType = new Combo(compositeBasic, SWT.READ_ONLY);
                                comboSdkType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
                                comboSdkType.setSize(408, 33);
                                comboSdkType.setItems(new String[] {
                                        "GP Interop", "GP Interop (stubbed)"
                                });
                                comboSdkType.select(0);
                                
                                        Combo comboBranch = new Combo(compositeBasic, SWT.READ_ONLY);
                                        comboBranch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
                                        comboBranch.setSize(204, 33);
                                        comboBranch.setItems(new String[] {
                                                "Develop", "Master"
                                        });
                                        comboBranch.select(0);
        
        Group grpWorkFlow = new Group(composite_inner, SWT.NONE);
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
        grpPaths.setText("Paths");
        grpPaths.setLayout(new FillLayout(SWT.HORIZONTAL));
        
        Composite compositePaths = new Composite(grpPaths, SWT.NONE);
        compositePaths.setLayout(new GridLayout(2, false));
                                        
                                                textBuildDrop = new Text(compositePaths, SWT.BORDER);
                                                textBuildDrop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                                                textBuildDrop.setSize(467, 31);
                                                textBuildDrop.setText("( loading... )");
                                
                                        Button btnBuildDrop = new Button(compositePaths, SWT.NONE);
                                        btnBuildDrop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                                        btnBuildDrop.setSize(204, 35);
                                        btnBuildDrop.setText("Build Drop");
                                        
                                        textSdkDrop = new Text(compositePaths, SWT.BORDER);
                                        textSdkDrop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                                        textSdkDrop.setText("( loading... )");
                                        textSdkDrop.setBounds(0, 0, 467, 31);
                                        
                                        Button btnSdkDrop = new Button(compositePaths, SWT.NONE);
                                        btnSdkDrop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                                        btnSdkDrop.setText("Sdk Drop");
                                        btnSdkDrop.setBounds(0, 0, 97, 35);
                                        
                                        textInjectionScripts = new Text(compositePaths, SWT.BORDER);
                                        textInjectionScripts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                                        textInjectionScripts.setText("( loading... )");
                                        textInjectionScripts.setBounds(0, 0, 467, 31);
                                        
                                        Button btnInjectionScripts = new Button(compositePaths, SWT.NONE);
                                        btnInjectionScripts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                                        btnInjectionScripts.setText("Inject Scripts");
                                        btnInjectionScripts.setBounds(0, 0, 88, 35);

        scrolledComposite.setContent(composite_inner);
        scrolledComposite.setMinSize(composite_inner.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    }
}
