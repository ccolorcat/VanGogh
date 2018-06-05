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

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cc.colorcat.adapter.RvAdapter;
import cc.colorcat.adapter.RvHolder;
import cc.colorcat.adapter.SimpleRvAdapter;
import cc.colorcat.vangogh.CornerTransformation;
import cc.colorcat.vangogh.VanGogh;

/**
 * Author: cxx
 * Date: 2018-06-05
 * GitHub: https://github.com/ccolorcat
 */
public class VanGoghActivity extends AppCompatActivity {
    private static final List<String> IMAGES = Arrays.asList(
            "http://img.mukewang.com/55237dcc0001128c06000338-300-170.jpg",
            "http://img.mukewang.com/55249cf30001ae8a06000338-300-170.jpg",
            "http://img.mukewang.com/5523711700016d1606000338-300-170.jpg",
            "http://img.mukewang.com/551e470500018dd806000338-300-170.jpg",
            "http://img.mukewang.com/551de0570001134f06000338-300-170.jpg",
            "http://img.mukewang.com/552640c300018a9606000338-300-170.jpg",
            "http://img.mukewang.com/551b92340001c9f206000338-300-170.jpg",
            "http://img.mukewang.com/5518c3d7000175af06000338-300-170.jpg",
            "http://img.mukewang.com/551b98ae0001e57906000338-300-170.jpg",
            "http://img.mukewang.com/550b86560001009406000338-300-170.jpg",
            "http://img.mukewang.com/551916790001125706000338-300-170.jpg",
            "http://img.mukewang.com/5518ecf20001cb4e06000338-300-170.jpg",
            "http://img.mukewang.com/5518bbe30001c32006000338-300-170.jpg",
            "http://img.mukewang.com/551380400001da9b06000338-300-170.jpg",
            "http://img.mukewang.com/550a33b00001738a06000338-300-170.jpg",
            "http://img.mukewang.com/5513a1b50001752806000338-300-170.jpg",
            "http://img.mukewang.com/5513e20600017c1806000338-300-170.jpg",
            "http://img.mukewang.com/550a78720001f37a06000338-300-170.jpg",
            "http://img.mukewang.com/550a836c0001236606000338-300-170.jpg",
            "http://img.mukewang.com/550a87da000168db06000338-300-170.jpg",
            "http://img.mukewang.com/530f0ef700019b5906000338-300-170.jpg",
            "http://img.mukewang.com/549bda090001c53e06000338-300-170.jpg",
            "http://img.mukewang.com/547d5a45000156f406000338-300-170.jpg",
            "http://img.mukewang.com/54780ea90001f3b406000338-300-170.jpg",
            "http://img.mukewang.com/547ed1c9000150cc06000338-300-170.jpg",
            "http://img.mukewang.com/54214727000160e306000338-300-170.jpg",
            "http://img.mukewang.com/54125edc0001ce6306000338-300-170.jpg",
            "http://img.mukewang.com/548165820001b4b006000338-300-170.jpg",
            "http://img.mukewang.com/53d74f960001ae9d06000338-300-170.jpg",
            "http://img.mukewang.com/54c87c73000150cf06000338-300-170.jpg"
    );

    private List<String> mData = new ArrayList<>();
    private SwipeRefreshLayout mRefreshLayout;
    private RvAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vangogh);

        float corner = toPx(16F);
        final float[] tlBrCorner = new float[]{corner, corner, 0F, 0F, corner, corner, 0F, 0F};
        final float[] trBlCorner = new float[]{0F, 0F, corner, corner, 0F, 0F, corner, corner};

        RecyclerView rv = findViewById(R.id.rv_item);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        mAdapter = new SimpleRvAdapter<String>(mData, R.layout.item_image) {
            float borderWidth = 0F;
            CornerTransformation tlBr = CornerTransformation.create(tlBrCorner, borderWidth, Color.RED);
            CornerTransformation trBl = CornerTransformation.create(trBlCorner, borderWidth, Color.BLUE);

            @Override
            public void bindView(@NonNull RvHolder holder, @NonNull String data) {
                RvHolder.Helper helper = holder.getHelper();
                ImageView imageView = helper.get(R.id.image);
                VanGogh.with(imageView.getContext())
                        .load(data)
                        .addTransformation((helper.getPosition() & 1) == 0 ? trBl : tlBr)
                        .into(imageView);
            }
        };
        rv.setAdapter(mAdapter);

        mRefreshLayout = findViewById(R.id.srl_root);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        refresh();
    }

    private void refresh() {
        mData.clear();
        mData.addAll(IMAGES);
        mAdapter.notifyDataSetChanged();
        mRefreshLayout.setRefreshing(false);
    }

    private float toPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
