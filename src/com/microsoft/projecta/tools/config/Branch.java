
package com.microsoft.projecta.tools.config;

/**
 * Branch of Project Astoria L1=Master L2=Develop
 * 
 * @author Chundong
 */
public enum Branch {
    Develop("Develop"), Master("Master");

    private String _value;

    Branch(String v) {
        _value = v;
    }

    public String getValue() {
        return _value;
    }
}
