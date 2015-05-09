package com.microsoft.projecta.tools;

public final class LaunchConfig {
	private String mBuildDropPath;
	private String mTakehomeScriptPath;
	private String mSdkToolsPath;
	private String mInjectionScriptPath;

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

		public LaunchConfig build() {
			if (mConfigInstance.mBuildDropPath == null) {
				// TODO: get latest build drop
				mConfigInstance.mBuildDropPath = "\\\\"+mBranch.getValue()+"\\";
			}
			if (mConfigInstance.mTakehomeScriptPath == null) {
				// TODO: get latest takehome script
				mConfigInstance.mTakehomeScriptPath = "\\\\"+mBranch.getValue()+"\\";
			}
			if (mConfigInstance.mSdkToolsPath == null) {
				// TODO: get latest sdk tools
				mConfigInstance.mSdkToolsPath = "\\\\"+mBranch.getValue()+"\\";
			}
			if (mConfigInstance.mInjectionScriptPath == null) {
				// TODO: get latest injection script
				mConfigInstance.mInjectionScriptPath = "\\\\"+mBranch.getValue()+"\\";
			}
			return mConfigInstance;
		}
	}
}
