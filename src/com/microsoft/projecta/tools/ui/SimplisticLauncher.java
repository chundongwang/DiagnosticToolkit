
package com.microsoft.projecta.tools.ui;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.xmlpull.v1.XmlPullParserException;

import com.microsoft.projecta.tools.common.AndroidManifestInfo;
import com.microsoft.projecta.tools.common.ExecuteException;
import com.microsoft.projecta.tools.common.TshellHelper;
import com.microsoft.projecta.tools.common.Utils;
import com.microsoft.projecta.tools.config.Branch;
import com.microsoft.projecta.tools.config.LaunchConfig;

public class SimplisticLauncher {
    private final static Point FIXED_SIZE = new Point(325, 210);
    private Display display;
    private Shell shlSimplisticLauncher;
    private Text mTextRawApk;
    private Text mTextOutDir;
    private Text mTextDeviceIP;
    private Button mBtnGo;
    private LaunchConfig mConfig;
    
    private String mDesiredOutDir;

    /**
     * @return the config
     */
    public synchronized LaunchConfig getConfig() {
        return mConfig;
    }

    /**
     * @param config the config to set
     */
    public synchronized void setConfig(LaunchConfig config) {
        mConfig = config;
        syncConfigToUI();
    }

    private void syncConfigToUI() {
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                if (mConfig.hasDeviceIPAddr()
                        && !mTextDeviceIP.getText().equals(mConfig.getDeviceIPAddr())) {
                    mTextDeviceIP.setText(mConfig.getDeviceIPAddr());
                }
                if (mConfig.hasOriginApkPath()
                        && !mTextRawApk.getText().equals(mConfig.getOriginApkPath())) {
                    mTextRawApk.setText(mConfig.getOriginApkPath());
                }
                if (mConfig.hasOutdirPath()
                        && !mTextOutDir.getText().equals(mConfig.getOutdirPath())) {
                    mTextOutDir.setText(mConfig.getOutdirPath());
                }
                mBtnGo.setEnabled(mConfig.validate());
            }
        });
    }

    private void initializeConfig(Branch branch) {
        // build launch config from default of the specified branch
        setConfig(new LaunchConfig.Builder(branch).addOutDir(System.getProperty("user.dir"))
                .addDeviceIP(retrieveDeviceIPAddress()).build());
    }

    private String retrieveDeviceIPAddress() {
        // get device ip
        StringBuilder deviceIpAddr = new StringBuilder("( Need the non-loopback IP address )");
        try {
            TshellHelper tshell = TshellHelper.getInstance(System.getProperty("user.dir"));
            String ipAddr = tshell.getIpAddr();
            if (ipAddr != null) {
                deviceIpAddr.delete(0, deviceIpAddr.length());
                deviceIpAddr.append(ipAddr);
            }
        } catch (IOException | InterruptedException | ExecuteException e) {
            // swallow
            e.printStackTrace();
        }

        return deviceIpAddr.toString();
    }

    /**
     * Launch the application.
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            SimplisticLauncher window = new SimplisticLauncher();
            if (args.length > 0) {
                window.setDesiredOutDir(args[0]);
            }
            window.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Open the window.
     * 
     * @wbp.parser.entryPoint
     */
    public void open() {
        display = Display.getDefault();
        createContents();

        //initializeConfig(Branch.Develop);

        shlSimplisticLauncher.open();
        shlSimplisticLauncher.layout();
        Utils.CenteredFrame(shlSimplisticLauncher);
        while (!shlSimplisticLauncher.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * Create contents of the window.
     */
    protected void createContents() {
        shlSimplisticLauncher = new Shell();
        shlSimplisticLauncher.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Shell shl = (Shell) e.widget;
                if (shl.getSize().x > FIXED_SIZE.x || shl.getSize().y > FIXED_SIZE.y) {
                    shl.setSize(FIXED_SIZE);
                }
            }
        });
        shlSimplisticLauncher.setMinimumSize(FIXED_SIZE);
        shlSimplisticLauncher.setSize(325, 210);
        shlSimplisticLauncher.setText("Diagnostic Tool");
        shlSimplisticLauncher.setLayout(new GridLayout(2, false));

        mTextRawApk = new Text(shlSimplisticLauncher, SWT.BORDER);
        mTextRawApk.setText("Raw APK Location");
        mTextRawApk.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                getConfig().setOriginApkPath(((Text) e.widget).getText());
                try {
                    AndroidManifestInfo info = AndroidManifestInfo.parseAndroidManifest(mConfig
                            .getOriginApkPath());
                    mConfig.setApkPackageInfo(info);
                } catch (IOException | XmlPullParserException e1) {
                    System.err.println("AndroidManifest parsing failed...");
                    e1.printStackTrace();
                    throw new RuntimeException("AndroidManifest parsing failed.", e1);
                }
                syncConfigToUI();
            }
        });
        mTextRawApk.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Button btnFindRawApk = new Button(shlSimplisticLauncher, SWT.NONE);
        btnFindRawApk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String apkPath = Utils.pickApkFile("Find Apk", "Find the original Apk",
                        System.getProperty("user.dir"), shlSimplisticLauncher);
                if (apkPath != null) {
                    mTextRawApk.setText(apkPath);
                }
            }
        });
        btnFindRawApk.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnFindRawApk.setText("Find");

        mTextOutDir = new Text(shlSimplisticLauncher, SWT.BORDER);
        mTextOutDir.setText("Output Folder");
        mTextOutDir.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                getConfig().setOutdirPath(((Text) e.widget).getText());
                syncConfigToUI();
            }
        });
        mTextOutDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Button btnFindOutDir = new Button(shlSimplisticLauncher, SWT.NONE);
        btnFindOutDir.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String outdirPath = Utils
                        .pickDirectory(
                                "Pick the output dir",
                                "Select a folder for drop injected apk, various log files and screen shot images.",
                                mConfig.getOutdirPath(), shlSimplisticLauncher);
                if (outdirPath != null) {
                    mTextOutDir.setText(outdirPath);
                }
            }
        });
        btnFindOutDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnFindOutDir.setText("Find");

        mTextDeviceIP = new Text(shlSimplisticLauncher, SWT.BORDER);
        mTextDeviceIP.setText("Device IP Address");
        mTextDeviceIP.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                getConfig().setDeviceIPAddr(((Text) e.widget).getText());
                syncConfigToUI();
            }
        });
        mTextDeviceIP.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Button btnRefresh = new Button(shlSimplisticLauncher, SWT.NONE);
        btnRefresh.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mTextDeviceIP.setText(retrieveDeviceIPAddress());
                syncConfigToUI();
            }
        });
        btnRefresh.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnRefresh.setText("Refresh");

        Composite composite = new Composite(shlSimplisticLauncher, SWT.NONE);
        composite.setLayout(new FillLayout(SWT.HORIZONTAL));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1));

        mBtnGo = new Button(composite, SWT.NONE);
        mBtnGo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                LaunchProgressDialog dialog = new LaunchProgressDialog(shlSimplisticLauncher,
                        mConfig);
                dialog.open();
            }
        });
        mBtnGo.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
        mBtnGo.setText("Go!");

        Menu menu = new Menu(shlSimplisticLauncher, SWT.BAR);
        shlSimplisticLauncher.setMenuBar(menu);

        MenuItem mntmSettings = new MenuItem(menu, SWT.NONE);
        mntmSettings.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                LaunchConfigSettings settingsDialog = new LaunchConfigSettings(
                        shlSimplisticLauncher, mConfig);
                Object result = settingsDialog.open();
                if (result != null) {
                    setConfig((LaunchConfig) result);
                }
            }
        });
        mntmSettings.setText("Settings");
    }

    /**
     * @return the desiredOutDir
     */
    public String getDesiredOutDir() {
        return mDesiredOutDir;
    }

    /**
     * @param desiredOutDir the desiredOutDir to set
     */
    public void setDesiredOutDir(String desiredOutDir) {
        mDesiredOutDir = desiredOutDir;
    }
}
