package com.microsoft.projecta.tools.ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.graphics.Point;

public class SimplisticLauncher {
    private static Text mtext;
    private static Text mtext_1;
    private static Text mtext_2;

    /**
     * Launch the application.
     * @param args
     */
    public static void main(String[] args) {
        Display display = Display.getDefault();
        Shell shell = new Shell();
        shell.setMinimumSize(new Point(509, 191));
        shell.setSize(509, 191);
        shell.setText("SWT Application");
        shell.setLayout(new GridLayout(2, false));
        
        mtext = new Text(shell, SWT.BORDER);
        mtext.setText("Raw APK Location");
        mtext.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Button btnFindRawApk = new Button(shell, SWT.NONE);
        btnFindRawApk.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnFindRawApk.setText("Find");
        
        mtext_1 = new Text(shell, SWT.BORDER);
        mtext_1.setText("Output Folder");
        mtext_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Button btnFindOutDir = new Button(shell, SWT.NONE);
        btnFindOutDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnFindOutDir.setText("Find");
        
        mtext_2 = new Text(shell, SWT.BORDER);
        mtext_2.setText("Device IP Address");
        mtext_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Button btnRefresh = new Button(shell, SWT.NONE);
        btnRefresh.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnRefresh.setText("Refresh");
        
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayout(new FillLayout(SWT.HORIZONTAL));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        
        Button btnGo = new Button(composite, SWT.NONE);
        btnGo.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.NORMAL));
        btnGo.setText("Go!");
        
        Menu menu = new Menu(shell, SWT.BAR);
        shell.setMenuBar(menu);
        
        MenuItem mntmSettings = new MenuItem(menu, SWT.NONE);
        mntmSettings.setText("Settings");

        shell.open();
        shell.layout();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }
}
