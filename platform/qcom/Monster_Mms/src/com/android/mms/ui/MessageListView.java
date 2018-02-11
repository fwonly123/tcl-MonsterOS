/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.ui;

import android.content.Context;
import android.text.ClipboardManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
//import android.widget.ListView;
import mst.widget.MstListView;

public final class MessageListView extends MstListView {
    private OnSizeChangedListener mOnSizeChangedListener;

    public MessageListView(Context context) {
        super(context);
    }

    public MessageListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_C:
            MessageListItem view = (MessageListItem)getSelectedView();
            if (view == null) {
                break;
            }
            MessageItem item = view.getMessageItem();
            if (item != null && item.isSms()) {
                ClipboardManager clip =
                    (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                clip.setText(item.mBody);
                return true;
            }
            break;
        }

        return super.onKeyShortcut(keyCode, event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mOnSizeChangedListener != null) {
            mOnSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
        }
    }

    /**
     * Set the listener which will be triggered when the size of
     * the view is changed.
     */
    void setOnSizeChangedListener(OnSizeChangedListener l) {
        mOnSizeChangedListener = l;
    }

    public interface OnSizeChangedListener {
        void onSizeChanged(int width, int height, int oldWidth, int oldHeight);
    }

    public int getCheckedPosition(){
        if ( getCheckedItemPositions() != null ) {
            return getCheckedItemPositions().indexOfValue(true);
        }
        return INVALID_POSITION;
    }

    //begin tangyisen
    private boolean isEditState = false;
    public void setEditState (boolean flag) {
        isEditState = flag;
    }

    private boolean isCanClick = false;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        //return super.onInterceptTouchEvent(ev);
        int action = ev.getAction();
        if(isEditState && action == MotionEvent.ACTION_DOWN) {
            isCanClick = true;
        } else {
            isCanClick = false;
        }
        if(isCanClick) {
            return true;
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    public boolean getIsCanClick() {
        return this.isCanClick;
    }
    //end tangyisen
}

