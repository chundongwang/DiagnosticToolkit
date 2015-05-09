package com.microsoft.projecta.tools;

public final class LaunchConfig {
	private String _buildDropPath;
	private String _takehomeScriptPath;
	private String _sdkToolsPath;
	private String _injectionScriptPath;
	
	private LaunchConfig() {
	}
	
	public static final class Builder {
		private LaunchConfig _configInstance;
		private Branch _branch;
		
		public Builder(Branch branch) {
			_branch = branch;
			_configInstance = new LaunchConfig();
		}
		
		public Builder() {
			this(Branch.Develop);
		}
		
		public Builder addBuildDrop(String buildDropPath) {
			_configInstance._buildDropPath = buildDropPath;
			return this;
		}
		
		public Builder addTakehome(String takehomePath) {
			_configInstance._takehomeScriptPath = takehomePath;
			return this;
		}
		
		public Builder addSdkTools(String sdkToolsPath) {
			_configInstance._sdkToolsPath = sdkToolsPath;
			return this;
		}
		
		public Builder addInjection(String injectionPath) {
			_configInstance._injectionScriptPath = injectionPath;
			return this;
		}
		
		public LaunchConfig build() {
			if (_configInstance._buildDropPath == null) {
				// TODO: get latest				
			}
			return _configInstance;
		}
	}
}
