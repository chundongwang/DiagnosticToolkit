package com.microsoft.projecta.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public final class LaunchConfig {

	private String mBuildDropPath;
	private String mTakehomeScriptPath;

	private String mSdkToolsPath;
	private String mInjectionScriptPath;
	private String mOriginApkPath;
	private String mOutdirPath;

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

	private LaunchConfig() {
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

	public static final class Builder {
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

		public Builder addInjection(String injectionPath) {
			mConfigInstance.mInjectionScriptPath = injectionPath;
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
			if (mConfigInstance.mOutdirPath == null) {
				mConfigInstance.mOutdirPath = System.getProperty("user.dir");
			}
			return mConfigInstance;
		}
	}
}
