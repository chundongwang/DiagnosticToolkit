
package com.microsoft.projecta.tools.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import com.microsoft.projecta.tools.common.AndroidManifestInfo;
import com.microsoft.projecta.tools.common.Utils;
import com.microsoft.projecta.tools.workflow.WorkFlowStatus;

public final class LaunchConfig {

    public static final class Builder {
        private static Logger logger = Logger.getLogger(Builder.class.getSimpleName());
        private LaunchConfig mConfigInstance;
        private Branch mBranch;

        public Builder() {
            this(Branch.Develop);
        }

        public Builder(Branch branch) {
            mBranch = branch;
            mConfigInstance = new LaunchConfig();
        }

        public Builder addPhoneBuildDropVhd(String buildDropPath) {
            mConfigInstance.mPhoneBuildDropVhdPath = buildDropPath;
            return this;
        }

        public Builder addArtBuildDrop(String buildDropPath) {
            mConfigInstance.mARTBuildDropPath = buildDropPath;
            return this;
        }

        public Builder addDeviceIP(String deviceIPAddr) {
            mConfigInstance.mDeviceIPAddr = deviceIPAddr;
            return this;
        }

        public Builder addInjection(String injectionPath) {
            mConfigInstance.mInjectionScriptPath = injectionPath;
            return this;
        }

        public Builder addSdkTools(String sdkToolsPath) {
            mConfigInstance.mSdkToolsPath = sdkToolsPath;
            return this;
        }

        public Builder addOutDir(String outDir) {
            mConfigInstance.mOutdirPath = outDir;
            return this;
        }

        public Builder addTakehome(String takehomePath) {
            mConfigInstance.mTakehomeScriptPath = takehomePath;
            return this;
        }

        public LaunchConfig build() {
            if (mConfigInstance.mARTBuildDropPath == null) {
                mConfigInstance.mARTBuildDropPath = parseLatestPath("\\\\build\\release\\AppStrULBuild\\ART "
                        + mBranch.getValue() + " Nightly\\latest.txt");
            }
            if (mConfigInstance.mTakehomeScriptPath == null) {
                mConfigInstance.mTakehomeScriptPath = parseLatestPath("\\\\pan\\arcadia\\team\\AoWDailySetup\\"
                        + mBranch.getValue().toLowerCase() + "\\latest.txt");
            }
            if (mConfigInstance.mSdkToolsPath == null) {
                mConfigInstance.mSdkToolsPath = parseLatestPath("\\\\pan\\arcadia\\team\\SDK "
                        + mBranch.getValue() + " Nightly Build\\latest.txt");
            }
            if (mConfigInstance.mInjectionScriptPath == null) {
                mConfigInstance.mInjectionScriptPath = "\\\\pan\\arcadia\\team\\users\\chunwang\\Injection\\autoInjection";
            }
            if (mConfigInstance.mPhoneBuildDropVhdPath == null) {
                String phoneBuildDropBase = null;
                if (mBranch == Branch.Develop) {
                    phoneBuildDropBase = "\\\\build\\release\\Threshold\\FBL_AOW_DEV01";
                } else if (mBranch == Branch.Master) {
                    phoneBuildDropBase = "\\\\build\\release\\Threshold\\FBL_AOW";
                }

                // Check
                // \\build\release\Threshold\FBL_AOW_DEV01\<build>\x86_windowsphone_vm_allres_Test_fre_USA.done
                Path base = Paths.get(phoneBuildDropBase);
                File[] dirs = base.toFile().listFiles();
                Arrays.sort(dirs, new Comparator<File>() {
                    /**
                     * Reverse the order to get latest directory first
                     */
                    @Override
                    public int compare(File o1, File o2) {
                        return o2.compareTo(o1);
                    }
                });
                for (File dir : dirs) {
                    if (dir.isDirectory() && dir.getName().startsWith("FBL_AOW")) {
                        Path candidate = base.resolve(dir.getName());
                        Path done_flag = candidate
                                .resolve("x86_windowsphone_vm_allres_Test_fre_USA.done");
                        if (Files.isRegularFile(done_flag)) {
                            // MC.x86fre\Binaries\Images\vm_allres\Test\USA
                            Path vhd_base = Paths.get(candidate.toAbsolutePath().toString(),
                                    "MC.x86fre", "Binaries", "Images", "vm_allres", "Test", "USA");
                            Path flash_vhd = vhd_base.resolve("Flash.vhd");
                            Path flash_debug_vhd = vhd_base.resolve("Flash_Debug.vhd");
                            if (Files.isRegularFile(flash_vhd)
                                    && Files.isRegularFile(flash_debug_vhd)) {
                                mConfigInstance.mPhoneBuildDropVhdPath = vhd_base.toAbsolutePath()
                                        .toString();
                            }
                            break;
                        }
                    }
                }
            }
            logger.fine(String.format("Launch config loaded as\n%s", mConfigInstance));
            return mConfigInstance;
        }

        private String parseLatestPath(String latest_file_path) {
            File latest = new File(latest_file_path);
            if (latest.exists() && latest.isFile()) {
                String first_line = null;
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(latest_file_path));
                    first_line = reader.readLine();
                    reader.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return e.getMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                    return e.getMessage();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return e.getMessage();
                        }
                    }
                }
                return first_line;
            } else {
                return "Invalid latest.txt path: " + latest_file_path;
            }
        }

        public Builder shouldInject(boolean shouldInject) {
            mConfigInstance.mShouldInject = shouldInject;
            return this;
        }

        public Builder shouldProvisionVM(boolean shouldProvisionVM) {
            mConfigInstance.mShouldProvisionVM = shouldProvisionVM;
            return this;
        }

        public Builder shouldTakeSnapshot(boolean shouldTakeSnapshot) {
            mConfigInstance.mShouldTakeSnapshot = shouldTakeSnapshot;
            return this;
        }
    }

    private String mPhoneBuildDropVhdPath;
    private String mARTBuildDropPath;
    private String mTakehomeScriptPath;
    private String mSdkToolsPath;
    private String mInjectionScriptPath;
    private String mDeviceIPAddr;
    private String mOriginApkPath;
    private String mOutdirPath;
    private String mInjectedApkPath;
    private String mLocalVhdPath;
    private String mUnzippedSdkToolsPath;
    private AndroidManifestInfo mApkPackageInfo;
    private String mStartupActivity;
    private boolean mShouldProvisionVM;
    private boolean mShouldInject;
    private boolean mShouldTakeSnapshot;
    private boolean mShouldKillApp;
    private SdkType mSdkType;

    private LaunchConfig() {
        mShouldProvisionVM = false;
        mShouldInject = true;
        mShouldTakeSnapshot = false;
        mShouldKillApp = false;
        mSdkType = SdkType.GP_INTEROP;
    }

    public String getActivityToLaunch() {
        if (hasStartupActivity()) {
            return getStartupActivity();
        } else {
            return getApkMainActivity();
        }
    }

    /**
     * @return the activity list
     */
    public List<String> getApkActivities() {
        if (hasApkPackageInfo()) {
            return mApkPackageInfo.getActivities();
        }
        return null;
    }

    /**
     * @return the main activity
     */
    public String getApkMainActivity() {
        if (hasApkPackageInfo()) {
            return mApkPackageInfo.getMainActivity();
        }
        return null;
    }

    public String getApkName() {
        return Utils.getNameWithoutExtension(Paths.get(mOriginApkPath));
    }

    /**
     * @return the apkPackageName
     */
    public String getApkPackageName() {
        if (hasApkPackageInfo()) {
            return mApkPackageInfo.getPackageName();
        }
        return null;
    }

    /**
     * @return the buildDropPath
     */
    public String getArtBuildDropPath() {
        return mARTBuildDropPath;
    }

    /**
     * @return the deviceIPAddr
     */
    public String getDeviceIPAddr() {
        return mDeviceIPAddr;
    }

    /**
     * @return the injectedApkPath
     */
    public String getInjectedApkPath() {
        return mInjectedApkPath;
    }

    /**
     * @return the injectionScriptPath
     */
    public String getInjectionScriptPath() {
        return mInjectionScriptPath;
    }

    /**
     * @return the originApkPath
     */
    public String getOriginApkPath() {
        return mOriginApkPath;
    }

    /**
     * @return the outdirPath
     */
    public String getOutdirPath() {
        return mOutdirPath;
    }

    /**
     * @return the sdkToolsPath
     */
    public String getSdkToolsPath() {
        return mSdkToolsPath;
    }

    /**
     * @return the startupActivity
     */
    public String getStartupActivity() {
        return mStartupActivity;
    }

    /**
     * @return the takehomeScriptPath
     */
    public String getTakehomeScriptPath() {
        return mTakehomeScriptPath;
    }

    /**
     * Get directory for putting temporary files
     * 
     * @return
     */
    public String getTmpDir() {
        return Paths.get(getOutdirPath(), "tmp").toString();
    }

    /**
     * Get directory for putting log files
     * 
     * @return
     */
    public String getLogsDir() {
        if (this.hasOriginApkPath()) {
            return Paths.get(getOutdirPath(), "logs", this.getApkName()).toString();
        }
        return null;
    }

    /**
     * @return the unzippedSdkToolsPath
     */
    public String getUnzippedSdkToolsPath() {
        return mUnzippedSdkToolsPath;
    }

    /**
     * @return if has apk package name
     */
    public boolean hasApkPackageInfo() {
        return mApkPackageInfo != null;
    }

    /**
     * @return if has injected apk file
     */
    public boolean hasInjectedApkPath() {
        return mInjectedApkPath != null && mInjectedApkPath.length() > 0;
    }

    /**
     * @return if has origin apk path
     */
    public boolean hasOriginApkPath() {
        return mOriginApkPath != null && mOriginApkPath.length() > 0;
    }

    /**
     * @return if has output directory
     */
    public boolean hasOutdirPath() {
        return mOutdirPath != null && mOutdirPath.length() > 0;
    }

    /**
     * @return if startupActivity is specified
     */
    public boolean hasStartupActivity() {
        return mStartupActivity != null;
    }

    /**
     * @return if has device ip address
     */
    public boolean hasDeviceIPAddr() {
        return mDeviceIPAddr != null && mDeviceIPAddr.length() > 0;
    }

    /**
     * @param info AndroidManifestInfo of the apk
     */
    public void setApkPackageInfo(AndroidManifestInfo info) {
        mApkPackageInfo = info;
    }

    /**
     * @param buildDropPath the buildDropPath to set
     */
    public void setArtBuildDropPath(String buildDropPath) {
        mARTBuildDropPath = buildDropPath;
    }

    /**
     * @param deviceIPAddr the deviceIPAddr to set
     */
    public void setDeviceIPAddr(String deviceIPAddr) {
        mDeviceIPAddr = deviceIPAddr;
    }

    /**
     * @param injectedApkPath the injectedApkPath to set
     */
    public void setInjectedApkPath(String injectedApkPath) {
        this.mInjectedApkPath = injectedApkPath;
    }

    /**
     * @param injectionScriptPath the injectionScriptPath to set
     */
    public void setInjectionScriptPath(String injectionScriptPath) {
        mInjectionScriptPath = injectionScriptPath;
    }

    /**
     * @param originApkPath the originApkPath to set
     */
    public void setOriginApkPath(String originApkPath) {
        mOriginApkPath = originApkPath;
    }

    /**
     * @param outdirPath the outdirPath to set
     */
    public void setOutdirPath(String outdirPath) {
        mOutdirPath = outdirPath;
    }

    /**
     * @param sdkToolsPath the sdkToolsPath to set
     */
    public void setSdkToolsPath(String sdkToolsPath) {
        mSdkToolsPath = sdkToolsPath;
    }

    /**
     * @param shouldInject the shouldInject to set
     */
    public void setShouldInject(boolean shouldInject) {
        mShouldInject = shouldInject;
    }

    /**
     * @param shouldProvisionVM the shouldProvisionVM to set
     */
    public void setShouldProvisionVM(boolean shouldProvisionVM) {
        mShouldProvisionVM = shouldProvisionVM;
    }

    /**
     * @param shouldTakeSnapshot the shouldTakeSnapshot to set
     */
    public void setShouldTakeSnapshot(boolean shouldTakeSnapshot) {
        mShouldTakeSnapshot = shouldTakeSnapshot;
    }

    /**
     * @param startupActivity the startupActivity to set
     */
    public void setStartupActivity(String startupActivity) {
        mStartupActivity = startupActivity;
    }

    /**
     * @param takehomeScriptPath the takehomeScriptPath to set
     */
    public void setTakehomeScriptPath(String takehomeScriptPath) {
        mTakehomeScriptPath = takehomeScriptPath;
    }

    /**
     * @param unzippedSdkToolsPath the unzippedSdkToolsPath to set
     */
    public void setUnzippedSdkToolsPath(String unzippedSdkToolsPath) {
        mUnzippedSdkToolsPath = unzippedSdkToolsPath;
    }

    public boolean should(WorkFlowStatus stage) {
        switch (stage) {
            case RAW_APK:
                // TODO Parse original apk ahead of tthe entire process
                return false;
            case PROVISION_VM:
                return mShouldProvisionVM;
            case CONNECT_DEVICE:
                // not optional
                return true;
            case INJECT_APK:
                return mShouldInject;
            case INSTALL_APP:
                // not optional
                return true;
            case LAUNCH_APP:
                // not optional
                return true;
            case TAKE_SCREENSHOT:
                return mShouldTakeSnapshot;
            case KILL_APP:
                return mShouldKillApp;
            default:
                return false;
        }
    }

    /**
     * @return the shouldInject
     */
    public boolean shouldInject() {
        return mShouldInject;
    }

    /**
     * @return the shouldProvisionVM
     */
    public boolean shouldProvisionVM() {
        return mShouldProvisionVM;
    }

    /**
     * @return the shouldTakeSnapshot
     */
    public boolean shouldTakeSnapshot() {
        return mShouldTakeSnapshot;
    }

    /**
     * @return the shouldKillApp
     */
    public boolean shouldKillApp() {
        return mShouldKillApp;
    }

    /**
     * @param shouldKillApp the shouldKillApp to set
     */
    public void setShouldKillApp(boolean shouldKillApp) {
        mShouldKillApp = shouldKillApp;
    }

    /**
     * @return the sdkType
     */
    public SdkType getSdkType() {
        return mSdkType;
    }

    /**
     * @param sdkType the sdkType to set
     */
    public void setSdkType(SdkType sdkType) {
        mSdkType = sdkType;
    }

    /**
     * @return the phoneBuildDropPath
     */
    public String getPhoneBuildDropVhdPath() {
        return mPhoneBuildDropVhdPath;
    }

    /**
     * @param phoneBuildDropPath the phoneBuildDropPath to set
     */
    public void setPhoneBuildDropVhdPath(String phoneBuildDropPath) {
        mPhoneBuildDropVhdPath = phoneBuildDropPath;
    }

    /**
     * @return the local path of Flash.vhd
     */
    public String getLocalVhdPath() {
        return mLocalVhdPath;
    }

    /**
     * @param localVhdPath the local path of Flash.vhd to set
     */
    public void setLocalVhdPath(String localVhdPath) {
        mLocalVhdPath = localVhdPath;
    }

    /**
     * @return if has localVhdPath
     */
    public boolean hasLocalVhdPath() {
        return mLocalVhdPath != null && mLocalVhdPath.length() > 0;
    }

    /**
     * Validate this config to see if it's runnable
     * 
     * @return
     */
    public boolean validate() {
        if (hasOriginApkPath() && hasDeviceIPAddr() && hasOutdirPath()) {
            if (Files.isReadable(Paths.get(getOriginApkPath()))
                    && Files.isDirectory(Paths.get(getOutdirPath()))) {
                if (getDeviceIPAddr().equalsIgnoreCase("usb")
                        || getDeviceIPAddr().matches("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+")) {
                    return true;
                }
            }
        }
        return false;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("ART Build Drop Path=%s", mARTBuildDropPath));
        builder.append('\n');
        builder.append(String.format("Phone Build Drop Path=%s", mPhoneBuildDropVhdPath));
        builder.append('\n');
        builder.append(String.format("Takehome Script Path=%s", mTakehomeScriptPath));
        builder.append('\n');
        builder.append(String.format("Sdk Tools Path=%s", mSdkToolsPath));
        builder.append('\n');
        builder.append(String.format("Injection Script Path=%s", mInjectionScriptPath));
        builder.append('\n');
        builder.append(String.format("Target Device IP Address=%s", mDeviceIPAddr));
        builder.append('\n');
        builder.append(String.format("Origin Apk Path=%s", mOriginApkPath));
        builder.append('\n');
        builder.append(String.format("Output Dir Path=%s", mOutdirPath));
        builder.append('\n');
        builder.append(String.format("Should Provision VM? %s", String.valueOf(mShouldProvisionVM)));
        builder.append('\n');
        builder.append(String.format("Should Inject? %s", String.valueOf(mShouldInject)));
        builder.append('\n');
        builder.append(String.format("Should Take Snapshot? %s",
                String.valueOf(mShouldTakeSnapshot)));
        builder.append('\n');
        builder.append(String.format("Should Kill App? %s", String.valueOf(mShouldKillApp)));
        return builder.toString();
    }
}
