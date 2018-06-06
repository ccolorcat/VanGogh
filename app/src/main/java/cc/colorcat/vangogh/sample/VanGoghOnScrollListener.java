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

import android.support.v7.widget.RecyclerView;
import android.widget.AbsListView;

import cc.colorcat.vangogh.VanGogh;

/**
 * Author: cxx
 * Date: 2018-06-06
 * GitHub: https://github.com/ccolorcat
 */
public final class VanGoghOnScrollListener extends RecyclerView.OnScrollListener implements AbsListView.OnScrollListener {
    public static VanGoghOnScrollListener get() {
        return new VanGoghOnScrollListener(null);
    }

    public static VanGoghOnScrollListener newInstance(AbsListView.OnScrollListener listener) {
        return new VanGoghOnScrollListener(listener);
    }

    private final AbsListView.OnScrollListener mListener;

    private VanGoghOnScrollListener(AbsListView.OnScrollListener listener) {
        mListener = listener;
    }

    // RecyclerView
    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        updateVanGoghState(newState == RecyclerView.SCROLL_STATE_IDLE);
    }

    // ListView
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        updateVanGoghState(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE);
        if (mListener != null) {
            mListener.onScrollStateChanged(view, scrollState);
        }
    }

    // ListView
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mListener != null) {
            mListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    private void updateVanGoghState(boolean idle) {
        if (idle) {
            VanGogh.get().resume();
        } else {
            VanGogh.get().pause();
        }
    }
}
