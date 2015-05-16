
package com.microsoft.projecta.tools.config;

public enum OS {
    LINUX, WINDOWS, MAC, SOLARIS, UNKNOWN;

    public final static OS CurrentOS = getCurrentOS();

    private static OS getCurrentOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        OS osType = UNKNOWN;
        if (osName.contains("linux")) {
            osType = LINUX;
        } else if (osName.contains("windows")) {
            osType = WINDOWS;
        } else if (osName.contains("solaris") || osName.contains("sunos")) {
            osType = SOLARIS;
        } else if (osName.contains("mac os") || osName.contains("macos")
                || osName.contains("darwin")) {
            osType = MAC;
        }
        return osType;
    }
}
