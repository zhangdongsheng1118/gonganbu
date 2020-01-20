package com.scoopit.weedfs.client.outer;

public interface ProgressListener {
    public void transferred(long transferedBytes, int progress);
}