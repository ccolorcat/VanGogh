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

package cc.colorcat.vangogh.sample;

import android.app.Application;

import cc.colorcat.vangogh.VanGogh;

/**
 * Author: cxx
 * Date: 2018-06-05
 * GitHub: https://github.com/ccolorcat
 */
public class Client extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initVanGogh();
    }

    private void initVanGogh() {
        VanGogh vanGogh = new VanGogh.Builder(this)
                .error(R.drawable.ic_error)
                .placeholder(R.drawable.ic_loading)
                .log(BuildConfig.DEBUG)
                .indicator(BuildConfig.DEBUG)
                .build();
        VanGogh.setSingleton(vanGogh);
    }
}
