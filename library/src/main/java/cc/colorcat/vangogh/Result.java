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

import android.graphics.Bitmap;

import java.io.InputStream;

/**
 * Author: cxx
 * Date: 2017-07-10
 * GitHub: https://github.com/ccolorcat
 */
public final class Result {
    private final Bitmap bitmap;
    private final InputStream stream;
    private final long contentLength;
    private final From from;

    public static Result create(Bitmap bitmap, From from) {
        if (bitmap == null) {
            throw new NullPointerException("bitmap == null");
        }
        if (from == null) {
            throw new NullPointerException("from == null");
        }
        return new Result(bitmap, null, -1L, from);
    }

    public static Result create(InputStream stream, From from) {
        return create(stream, -1L, from);
    }

    public static Result create(InputStream stream, long contentLength, From from) {
        if (stream == null) {
            throw new NullPointerException("stream == null");
        }
        if (from == null) {
            throw new NullPointerException("from == null");
        }
        return new Result(null, stream, contentLength, from);
    }

    private Result(Bitmap bitmap, InputStream stream, long contentLength, From from) {
        this.bitmap = bitmap;
        this.stream = stream;
        this.contentLength = contentLength;
        this.from = from;
    }

    Bitmap bitmap() {
        return bitmap;
    }

    InputStream stream() {
        return stream;
    }

    long contentLength() {
        return contentLength;
    }

    From from() {
        return from;
    }
}
