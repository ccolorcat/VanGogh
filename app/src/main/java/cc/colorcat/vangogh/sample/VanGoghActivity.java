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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cc.colorcat.adapter.RvAdapter;
import cc.colorcat.adapter.RvHolder;
import cc.colorcat.adapter.SimpleRvAdapter;
import cc.colorcat.vangogh.CornerTransformation;
import cc.colorcat.vangogh.SquareTransformation;
import cc.colorcat.vangogh.Transformation;
import cc.colorcat.vangogh.VanGogh;

/**
 * Author: cxx
 * Date: 2018-06-05
 * GitHub: https://github.com/ccolorcat
 */
public class VanGoghActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Result<List<Course>>> {
    private static final int LOADER_ID = 23;
    private static final String TAG = "VanGogh";

    private List<Course> mData = new ArrayList<>();
    private SwipeRefreshLayout mRefreshLayout;
    private RvAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vangogh);

        RecyclerView rv = findViewById(R.id.rv_item);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addOnScrollListener(VanGoghOnScrollListener.get(TAG));
        mAdapter = new CourseAdapter(mData, R.layout.item_course);
        rv.setAdapter(mAdapter);

        mRefreshLayout = findViewById(R.id.srl_root);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
        refreshData();
    }

    private void refreshData() {
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this).forceLoad();
    }

    @NonNull
    @Override
    public Loader<Result<List<Course>>> onCreateLoader(int id, @Nullable Bundle args) {
        return new CourseLoader(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Result<List<Course>>> loader, Result<List<Course>> data) {
        if (data != null && data.getStatus() == Result.STATUS_OK) {
            mData.clear();
            mData.addAll(data.getData());
            mAdapter.notifyDataSetChanged();
        }
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Result<List<Course>>> loader) {
    }


    private static class CourseLoader extends AsyncTaskLoader<Result<List<Course>>> {
        private CourseLoader(@NonNull Context context) {
            super(context);
        }

        @Nullable
        @Override
        public Result<List<Course>> loadInBackground() {
            BufferedReader reader = null;
            try {
                Gson gson = new GsonBuilder().create();
                reader = new BufferedReader(new InputStreamReader(getContext().getResources().openRawResource(R.raw.data)));
                return gson.fromJson(reader, new TypeToken<Result<List<Course>>>() {
                }.getType());
            } finally {
                Utils.close(reader);
            }
        }
    }


    private static class CourseAdapter extends SimpleRvAdapter<Course> {
        private Transformation mSquare = new SquareTransformation();
        private Transformation mTrBl;
        private Transformation mTlBr;

        CourseAdapter(List<? extends Course> data, int layoutResId) {
            super(data, layoutResId);
        }

        @Override
        public void bindView(@NonNull RvHolder holder, @NonNull Course data) {
            if (mTlBr == null) createTransformation(holder.itemView.getContext());
            RvHolder.Helper helper = holder.getHelper();
            helper.setText(R.id.tv_serial_number, Integer.toString(helper.getPosition()))
                    .setText(R.id.tv_name, data.getName())
                    .setText(R.id.tv_description, data.getDescription());
            ImageView icon = helper.get(R.id.iv_icon);
            Log.i(TAG, String.format("icon, size(%d, %d), measuredSize(%d, %d)", icon.getWidth(), icon.getHeight(), icon.getMeasuredWidth(), icon.getMeasuredHeight()));
            VanGogh.with(icon.getContext())
                    .load(data.getPicBigUrl())
                    .tag(TAG)
                    .addTransformation(mSquare)
                    .addTransformation((helper.getPosition() & 1) == 0 ? mTrBl : mTlBr)
                    .into(icon);
        }

        private void createTransformation(Context context) {
            float radius = Utils.dpToPx(context.getResources(), 16F);
            float[] tlBrCorner = new float[]{radius, radius, 0F, 0F, radius, radius, 0F, 0F};
            float[] trBlCorner = new float[]{0F, 0F, radius, radius, 0F, 0F, radius, radius};
            mTlBr = CornerTransformation.create(tlBrCorner);
            mTrBl = CornerTransformation.create(trBlCorner);
        }
    }
}
