/**
 * 
 */
package com.microsoft.projecta.tools.config;

/**
 *
 */
public enum SdkType {
    GP_INTEROP("gpinterop"), STUBBED_INTEROP("stub-gpinterop"), ANALYTICS_V2("interop-analytics-v2");
    
    private String mTypeString;
    
    SdkType(String type) {
        mTypeString = type;
    }
    
    public String toString() {
        return mTypeString;
    }
    
    public static SdkType ValueOf(int ordinal) {
        if (ordinal == GP_INTEROP.ordinal()) return GP_INTEROP;
        else if (ordinal == STUBBED_INTEROP.ordinal()) return STUBBED_INTEROP;
        else if (ordinal == ANALYTICS_V2.ordinal()) return ANALYTICS_V2;
        else throw new IllegalArgumentException("Invalid ordinal for SdkType: "+ordinal);
    }
}
