/*
 * Copyright 2018 cxx
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
 */

package cc.colorcat.vangogh;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Author: cxx
 * Date: 2017-07-11
 * GitHub: https://github.com/ccolorcat
 */
class HttpDownloader implements Downloader {
    private HttpURLConnection conn;

    @Override
    public Result load(Task task) throws IOException {
        Uri uri = task.uri();
        conn = (HttpURLConnection) new URL(uri.toString()).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(task.connectTimeOut());
        conn.setReadTimeout(task.readTimeOut());
        int code = conn.getResponseCode();
        if (code == HttpURLConnection.HTTP_OK) {
            InputStream is = conn.getInputStream();
            if (is != null) {
                long contentLength = conn.getContentLength();
                if (contentLength > 0) {
                    return new Result(is, contentLength, From.NETWORK);
                } else {
                    return new Result(is, From.NETWORK);
                }
            }
        }
        throw new IOException("network onFailed, code = " + code + ", msg = " + conn.getResponseMessage());
    }

    @Override
    public void shutDown() {
        if (conn != null) {
            conn.disconnect();
        }
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Downloader clone() {
        return new HttpDownloader();
    }
}
