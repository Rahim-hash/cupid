package com.cqu.cupid.profile;

/**
 * Single setting controlling FR_Profile_Keep_Ethics.
 * When true (default), profile deletion is soft (difficult but not impossible).
 * When false, profile deletion is immediate/permanent.
 */
public class EthicsConfig {

    private boolean keepEthicsEnabled = true;

    public boolean isKeepEthicsEnabled() {
        return keepEthicsEnabled;
    }

    public void setKeepEthicsEnabled(boolean keepEthicsEnabled) {
        this.keepEthicsEnabled = keepEthicsEnabled;
    }

}