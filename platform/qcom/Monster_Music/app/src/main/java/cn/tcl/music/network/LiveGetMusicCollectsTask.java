package cn.tcl.music.network;

import android.content.Context;

import java.util.HashMap;

import cn.tcl.music.R;
import cn.tcl.music.model.live.BaseSong;
import cn.tcl.music.model.live.LiveCollectsBean;
import cn.tcl.music.util.LogUtil;

public class LiveGetMusicCollectsTask extends RequestSongTask {
    private static final String TAG = LiveGetMusicCollectsTask.class.getSimpleName();
    private ILoadData mListener;
    private String mListId = "";

    public LiveGetMusicCollectsTask(Context context, ILoadData listener
            , String listId) {
        super(DataRequest.Method.METHOD_LIVE_GET_COLLECTS_FAVORITE, context, LiveCollectsBean.class);
        mListener = listener;
        mListId = listId;
    }

    @Override
    protected HashMap<String, Object> getParams() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("ids", mListId);
        return values;
    }

    @Override
    protected void postInBackground(BaseSong baseSong) {

    }

    @Override
    protected void onPostExecute(BaseSong baseSong) {
        super.onPostExecute(baseSong);
        if (mListener != null) {
            if (baseSong == null) {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_GET_COLLECTS_FAVORITE, mContext.getString(R.string.load_fail));
                return;
            }
            //请求成功
            if (baseSong.returnCode == 0) {
                mListener.onLoadSuccess(DataRequest.Type.TYPE_LIVE_GET_COLLECTS_FAVORITE, baseSong.returnArray);
            } else {
                mListener.onLoadFail(DataRequest.Type.TYPE_LIVE_GET_COLLECTS_FAVORITE, baseSong.returnMessage);
            }
        } else {
            LogUtil.d(TAG, "mListener is null");
        }

    }
}