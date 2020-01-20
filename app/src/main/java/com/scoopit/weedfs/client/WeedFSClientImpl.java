/*
 * (C) Copyright 2013 Scoop IT SAS (http://scoop.it/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 *     Philippe GASSMANN
 *     Jean-Baptiste BELLET
 */
package com.scoopit.weedfs.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.scoopit.weedfs.client.caching.LookupCache;
import com.scoopit.weedfs.client.net.AssignResult;
import com.scoopit.weedfs.client.net.LookupResult;
import com.scoopit.weedfs.client.net.WriteResult;
import com.scoopit.weedfs.client.outer.ProgressListener;
import com.scoopit.weedfs.client.outer.ProgressOutHttpEntity;
import com.scoopit.weedfs.client.status.MasterStatus;
import com.scoopit.weedfs.client.status.VolumeStatus;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.widget.AutoToolbar;

class WeedFSClientImpl implements WeedFSClient {

    final URL masterURL;
    final HttpClient httpClient;
    final LookupCache lookupCache;

    WeedFSClientImpl(URL masterURL, HttpClient httpClient, LookupCache lookupCache) {
        this.masterURL = masterURL;
        this.httpClient = httpClient;
        this.lookupCache = lookupCache;
    }

    @Override
    public Assignation assign(AssignParams params) throws IOException, WeedFSException {
        StringBuilder url = new StringBuilder(new URL(masterURL, "/dir/assign").toExternalForm());
        url.append("?count=");
        url.append(params.versionCount);
        if (params.replicationStrategy != null) {
            url.append("&replication=");
            url.append(params.replicationStrategy.parameterValue);
        }

        if (params.collection != null) {
            url.append("&collection=");
            url.append(params.collection);
        }

        HttpGet get = new HttpGet(url.toString());
        try {
            HttpResponse response = httpClient.execute(get);

            ObjectMapper mapper = new ObjectMapper();
            try {
                AssignResult result = mapper.readValue(response.getEntity().getContent(), AssignResult.class);

                if (result.error != null) {
                    throw new WeedFSException(result.error);
                }

                return new Assignation(result);
            } catch (JsonMappingException | JsonParseException e) {
                throw new WeedFSException("Unable to parse JSON from weed-fs", e);
            }
        } finally {
            get.abort();
        }
    }

    @Override
    public void delete(WeedFSFile file, Location location) throws IOException, WeedFSException {
        StringBuilder url = new StringBuilder();
        if (!location.publicUrl.contains("http")) {
            url.append("http://");
        }
        url.append(location.publicUrl);
        url.append("/");
        url.append(file.fid);

        HttpDelete delete = new HttpDelete(url.toString());
        try {
            HttpResponse response = httpClient.execute(delete);

            StatusLine line = response.getStatusLine();
            if (line.getStatusCode() < 200 || line.getStatusCode() > 299) {
                throw new WeedFSException("Error deleting file " + file.fid + " on " + location.publicUrl + ": " + line.getStatusCode() + " "
                        + line.getReasonPhrase());
            }
        } finally {
            delete.abort();
        }
    }

    @Override
    public List<Location> lookup(long volumeId) throws IOException, WeedFSException {
        if (lookupCache != null) {
            List<Location> ret = lookupCache.lookup(volumeId);
            if (ret != null) {
                return ret;
            }
        }

        StringBuilder url = new StringBuilder(new URL(masterURL, "/dir/lookup").toExternalForm());
        url.append("?volumeId=");
        url.append(volumeId);

        HttpGet get = new HttpGet(url.toString());
        try {
            HttpResponse response = httpClient.execute(get);

            ObjectMapper mapper = new ObjectMapper();
            try {
                LookupResult result = mapper.readValue(response.getEntity().getContent(), LookupResult.class);

                if (result.error != null) {
                    throw new WeedFSException(result.error);
                }

                if (lookupCache != null && result.locations != null && result.locations.size() > 0) {
                    lookupCache.setLocation(volumeId, result.locations);
                }

                return result.locations;
            } catch (JsonMappingException | JsonParseException e) {
                throw new WeedFSException("Unable to parse JSON from weed-fs", e);
            }
        } finally {
            get.abort();
        }

    }

    @Override
    public int write(WeedFSFile file, Location location, File fileToUpload) throws IOException, WeedFSException {
        if (fileToUpload.length() == 0) {
            throw new WeedFSException("Cannot write a 0-length file");
        }
        return write(file, location, fileToUpload, null, null, null, null);
    }

    @Override
    public int write(WeedFSFile file, Location location, byte[] dataToUpload, String fileName) throws IOException, WeedFSException {
        if (dataToUpload.length == 0) {
            throw new WeedFSException("Cannot write a 0-length data");
        }
        return write(file, location, null, dataToUpload, null, fileName, null);
    }

    @Override
    public int write(WeedFSFile file, Location location, InputStream inputToUpload, String fileName, ProgressListener cb) throws IOException, WeedFSException {
        return write(file, location, null, null, inputToUpload, fileName, cb);
    }

    @Override
    public int write(WeedFSFile file, Location location, File localFile, String fileName, ProgressListener cb) throws IOException {
        return write(file, location, localFile, null, null, fileName, cb);
    }

    private String sanitizeFileName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "file";
        } else if (fileName.length() > 256) {
            return fileName.substring(0, 255);
        }
        return fileName;

    }

    public String inputStream2String(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

    private int write(WeedFSFile file, Location location, File fileToUpload, byte[] dataToUpload, InputStream inputToUpload, String fileName, ProgressListener cb)
            throws IOException, WeedFSException {
        StringBuilder url = new StringBuilder();
        if (!location.publicUrl.contains("http")) {
            url.append("http://");
        }
        url.append(location.publicUrl);
        url.append('/');
        url.append(file.fid);

        if (file.version > 0) {
            url.append('_');
            url.append(file.version);
        }

        HttpPost post = new HttpPost(url.toString());
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        if (fileToUpload != null) {
            if (fileName == null) {
                fileName = fileToUpload.getName();
            }
            LogUtils.w("upload file size:" + fileToUpload.length());
            multipartEntityBuilder.addBinaryBody("file", fileToUpload, ContentType.APPLICATION_OCTET_STREAM, sanitizeFileName(fileName));
        } else if (dataToUpload != null) {
            multipartEntityBuilder.addBinaryBody("file", dataToUpload, ContentType.APPLICATION_OCTET_STREAM, sanitizeFileName(fileName));
        } else {
            multipartEntityBuilder.addBinaryBody("file", inputToUpload, ContentType.APPLICATION_OCTET_STREAM, sanitizeFileName(fileName));
        }

        HttpEntity httpEntity = multipartEntityBuilder.build();
        long totalSize = httpEntity.getContentLength();
        ProgressOutHttpEntity progressHttpEntity = new ProgressOutHttpEntity(
                httpEntity, cb);
        post.setEntity(progressHttpEntity);

        try {
            HttpResponse response = httpClient.execute(post);
            ObjectMapper mapper = new ObjectMapper();
            try {
                WriteResult result = mapper.readValue(response.getEntity().getContent(), WriteResult.class);
                if (result.error != null) {
                    throw new WeedFSException(result.error);
                }

                if (cb != null) {
                    cb.transferred(result.size, 100);
                }

                return result.size;
            } catch (JsonMappingException | JsonParseException e) {
                throw new WeedFSException("Unable to parse JSON from weed-fs", e);
            }
        } finally {
            post.abort();
        }

        /*
        OkGo.<String>post(url.toString()).tag(this).isMultipart(true).params("file", fileToUpload).execute(new StringCallback() {

            @Override
            public void onSuccess(Response<String> response) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    WriteResult result = mapper.readValue(response.body(), WriteResult.class);
                    if (result.error != null) {
                        throw new WeedFSException(result.error);
                    }

                    if (cb != null) {
                        cb.transferred(0, 100);
                    }
                } catch (Exception e) {
                }
            }

            @Override
            public void uploadProgress(Progress progress) {
                if (cb != null) {
                    com.blankj.utilcode.util.LogUtils.w("imple upload and get url transferedBytes:" + progress.currentSize + "    progress:" + (int) Math.ceil(progress.fraction*100));
                    cb.transferred(progress.currentSize, (int) Math.ceil(progress.fraction*100));
                }
            }
        });

        return 0;*/
    }

    @Override
    public InputStream read(WeedFSFile file, Location location) throws IOException, WeedFSException, WeedFSFileNotFoundException {
        StringBuilder url = new StringBuilder();
        if (!location.publicUrl.contains("http")) {
            url.append("http://");
        }
        url.append(location.publicUrl);
        url.append('/');
        url.append(file.fid);

        if (file.version > 0) {
            url.append('_');
            url.append(file.version);
        }
        HttpGet get = new HttpGet(url.toString());
        HttpResponse response = httpClient.execute(get);
        StatusLine line = response.getStatusLine();
        if (line.getStatusCode() == 404) {
            get.abort();
            throw new WeedFSFileNotFoundException(file, location);
        }
        if (line.getStatusCode() != 200) {
            get.abort();
            throw new WeedFSException("Error reading file " + file.fid + " on " + location.publicUrl + ": " + line.getStatusCode() + " "
                    + line.getReasonPhrase());
        }
        return response.getEntity().getContent();
    }

    @Override
    public MasterStatus getMasterStatus() throws IOException {
        URL url = new URL(masterURL, "/dir/status");

        HttpGet get = new HttpGet(url.toString());

        try {
            HttpResponse response = httpClient.execute(get);
            StatusLine line = response.getStatusLine();

            if (line.getStatusCode() != 200) {
                throw new IOException("Not 200 status recieved for master status url: " + url.toExternalForm());
            }

            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readValue(response.getEntity().getContent(), MasterStatus.class);

            } catch (JsonMappingException | JsonParseException e) {
                throw new WeedFSException("Unable to parse JSON from weed-fs", e);
            }
        } finally {
            get.abort();
        }
    }

    @Override
    public VolumeStatus getVolumeStatus(Location location) throws IOException {
        StringBuilder url = new StringBuilder();
        if (!location.publicUrl.contains("http")) {
            url.append("http://");
        }
        url.append(location.publicUrl);
        url.append("/status");

        HttpGet get = new HttpGet(url.toString());

        try {
            HttpResponse response = httpClient.execute(get);
            StatusLine line = response.getStatusLine();

            if (line.getStatusCode() != 200) {
                throw new IOException("Not 200 status recieved for master status url: " + url.toString());
            }

            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readValue(response.getEntity().getContent(), VolumeStatus.class);

            } catch (JsonMappingException | JsonParseException e) {
                throw new WeedFSException("Unable to parse JSON from weed-fs", e);
            }
        } finally {
            get.abort();
        }
    }
}
