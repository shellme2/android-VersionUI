package com.eebbk.tool.versionupdate;

/**
 * @author hesn
 * 2018/6/27
 */
public interface BfcVersionStateListener {

    int STATE_SILENT_READY = 0;

    void onVersionState(int state);
}
