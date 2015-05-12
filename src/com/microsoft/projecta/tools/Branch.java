
package com.microsoft.projecta.tools;

/**
 * Branch of Project Astoria L1=Master L2=Develop
 * 
 * @author Chundong
 */
public enum Branch {
    Develop("Develop"), Master("Master"), L1("Develop"), L2("Master");

    private String _value;

    Branch(String v) {
        _value = v;
    }

    public String getValue() {
        return _value;
    }
}
