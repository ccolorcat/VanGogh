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

import com.google.gson.annotations.SerializedName;

/**
 * Author: cxx
 * Date: 2018-06-06
 * GitHub: https://github.com/ccolorcat
 */
public class Course {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("picSmall")
    private String picSmallUrl;
    @SerializedName("picBig")
    private String picBigUrl;
    @SerializedName("description")
    private String description;
    @SerializedName("learner")
    private int numOfLearner;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicSmallUrl() {
        return picSmallUrl;
    }

    public void setPicSmallUrl(String picSmallUrl) {
        this.picSmallUrl = picSmallUrl;
    }

    public String getPicBigUrl() {
        return picBigUrl;
    }

    public void setPicBigUrl(String picBigUrl) {
        this.picBigUrl = picBigUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNumOfLearner() {
        return numOfLearner;
    }

    public void setNumOfLearner(int numOfLearner) {
        this.numOfLearner = numOfLearner;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", picSmallUrl='" + picSmallUrl + '\'' +
                ", picBigUrl='" + picBigUrl + '\'' +
                ", description='" + description + '\'' +
                ", numOfLearner=" + numOfLearner +
                '}';
    }
}
