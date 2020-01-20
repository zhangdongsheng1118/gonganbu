package com.yixin.tinode.util.glide;

public interface ProgressListener {

    void progress(long bytesRead, long contentLength, boolean done);

}
