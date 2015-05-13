package com.microsoft.projecta.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

public final class LaunchConfig {

	private String mBuildDropPath;
	private String mTakehomeScriptPath;

	private String mSdkToolsPath;
	private String mInjectionScriptPath;
	private String mDeviceIPAddr;
	private String mOriginApkPath;
	private String mOutdirPath;

	private boolean mShouldProvisionVM;
	private boolean mShouldInject;
	private boolean mShouldTakeSnapshot;

	private LaunchConfig() {
		mShouldProvisionVM = false;
		mShouldInject = true;
		mShouldTakeSnapshot = true;
	}

	/**
	 * @param buildDropPath
	 *            the buildDropPath to set
	 */
	public void setBuildDropPath(String buildDropPath) {
		mBuildDropPath = buildDropPath;
	}

	/**
	 * @param takehomeScriptPath
	 *            the takehomeScriptPath to set
	 */
	public void setTakehomeScriptPath(String takehomeScriptPath) {
		mTakehomeScriptPath = takehomeScriptPath;
	}

	/**
	 * @param sdkToolsPath
	 *            the sdkToolsPath to set
	 */
	public void setSdkToolsPath(String sdkToolsPath) {
		mSdkToolsPath = sdkToolsPath;
	}

	/**
	 * @param injectionScriptPath
	 *            the injectionScriptPath to set
	 */
	public void setInjectionScriptPath(String injectionScriptPath) {
		mInjectionScriptPath = injectionScriptPath;
	}

	/**
	 * @return if has origin apk path
	 */
	public boolean hasOriginApkPath() {
		return mOriginApkPath != null && mOriginApkPath.length() > 0;
	}

	/**
	 * @return the originApkPath
	 */
	public String getOriginApkPath() {
		return mOriginApkPath;
	}

	/**
	 * @param originApkPath
	 *            the originApkPath to set
	 */
	public void setOriginApkPath(String originApkPath) {
		mOriginApkPath = originApkPath;
	}

	/**
	 * @return if has output directory
	 */
	public boolean hasOutdirPath() {
		return mOutdirPath != null && mOutdirPath.length() > 0;
	}

	/**
	 * @return the outdirPath
	 */
	public String getOutdirPath() {
		return mOutdirPath;
	}

	/**
	 * @param outdirPath
	 *            the outdirPath to set
	 */
	public void setOutdirPath(String outdirPath) {
		mOutdirPath = outdirPath;
	}

	/**
	 * @return the buildDropPath
	 */
	public String getBuildDropPath() {
		return mBuildDropPath;
	}

	/**
	 * @return the takehomeScriptPath
	 */
	public String getTakehomeScriptPath() {
		return mTakehomeScriptPath;
	}

	/**
	 * @return the sdkToolsPath
	 */
	public String getSdkToolsPath() {
		return mSdkToolsPath;
	}

	/**
	 * @return the injectionScriptPath
	 */
	public String getInjectionScriptPath() {
		return mInjectionScriptPath;
	}

	/**
	 * @return the shouldProvisionVM
	 */
	public boolean shouldProvisionVM() {
		return mShouldProvisionVM;
	}

	/**
	 * @param shouldProvisionVM
	 *            the shouldProvisionVM to set
	 */
	public void setShouldProvisionVM(boolean shouldProvisionVM) {
		mShouldProvisionVM = shouldProvisionVM;
	}

	/**
	 * @return the shouldInject
	 */
	public boolean shouldInject() {
		return mShouldInject;
	}

	/**
	 * @param shouldInject
	 *            the shouldInject to set
	 */
	public void setShouldInject(boolean shouldInject) {
		mShouldInject = shouldInject;
	}

	/**
	 * @return the shouldTakeSnapshot
	 */
	public boolean shouldTakeSnapshot() {
		return mShouldTakeSnapshot;
	}

	/**
	 * @param shouldTakeSnapshot
	 *            the shouldTakeSnapshot to set
	 */
	public void setShouldTakeSnapshot(boolean shouldTakeSnapshot) {
		mShouldTakeSnapshot = shouldTakeSnapshot;
	}

	/**
	 * @return the deviceIPAddr
	 */
	public String getDeviceIPAddr() {
		return mDeviceIPAddr;
	}

	/**
	 * @param deviceIPAddr the deviceIPAddr to set
	 */
	public void setDeviceIPAddr(String deviceIPAddr) {
		mDeviceIPAddr = deviceIPAddr;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("Build Drop Path=%s", mBuildDropPath));
		builder.append('\n');
		builder.append(String.format("Takehome Script Path=%s",
				mTakehomeScriptPath));
		builder.append('\n');
		builder.append(String.format("Sdk Tools Path=%s", mSdkToolsPath));
		builder.append('\n');
		builder.append(String.format("Injection Script Path=%s",
				mInjectionScriptPath));
		builder.append('\n');
		builder.append(String.format("Target Device IP Address=%s",
				mDeviceIPAddr));
		builder.append('\n');
		builder.append(String.format("Origin Apk Path=%s", mOriginApkPath));
		builder.append('\n');
		builder.append(String.format("Output Dir Path=%s", mOutdirPath));
		builder.append('\n');
		builder.append(String.format("Should Provision VM?",
				String.valueOf(mShouldProvisionVM)));
		builder.append('\n');
		builder.append(String.format("Should Inject?",
				String.valueOf(mShouldInject)));
		builder.append('\n');
		builder.append(String.format("Should Take Snapshot?",
				String.valueOf(mShouldTakeSnapshot)));
		return builder.toString();
	}

	public static final class Builder {
		private static Logger logger = Logger.getLogger(Builder.class
				.getSimpleName());
		private LaunchConfig mConfigInstance;
		private Branch mBranch;

		public Builder() {
			this(Branch.Develop);
		}

		public Builder(Branch branch) {
			mBranch = branch;
			mConfigInstance = new LaunchConfig();
		}

		public Builder addBuildDrop(String buildDropPath) {
			mConfigInstance.mBuildDropPath = buildDropPath;
			return this;
		}

		public Builder addTakehome(String takehomePath) {
			mConfigInstance.mTakehomeScriptPath = takehomePath;
			return this;
		}

		public Builder addSdkTools(String sdkToolsPath) {
			mConfigInstance.mSdkToolsPath = sdkToolsPath;
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

		private String parseLatestPath(String latest_file_path) {
			File latest = new File(latest_file_path);
			if (latest.exists() && latest.isFile()) {
				String first_line = null;
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(
							new FileReader(latest_file_path));
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

		public LaunchConfig build() {
			if (mConfigInstance.mBuildDropPath == null) {
				mConfigInstance.mBuildDropPath = parseLatestPath("\\\\build\\release\\AppStrULBuild\\ART "
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
			if (mConfigInstance.mDeviceIPAddr == null) {
				mConfigInstance.mDeviceIPAddr = "127.0.0.1";
			}
			if (mConfigInstance.mOutdirPath == null) {
				mConfigInstance.mOutdirPath = System.getProperty("user.dir");
			}
			logger.fine(String.format("Launch config loaded as\n%s",
					mConfigInstance));
			return mConfigInstance;
		}
	}
}
