/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.android.dialer.list;

import com.mst.t9search.MstSearchContactsAdapter;
import com.mst.t9search.ContactsHelper;
import static android.Manifest.permission.CALL_PHONE;

import android.app.Activity;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.contacts.common.list.ContactEntryListAdapter;
import com.android.contacts.common.util.PermissionsUtil;
import com.android.dialer.dialpad.SmartDialCursorLoader;
import com.android.dialer.R;
import com.android.dialer.widget.EmptyContentView;
import com.mediatek.dialer.dialersearch.DialerSearchCursorLoader;
import com.mediatek.dialer.util.DialerFeatureOptions;

import java.util.ArrayList;

/**
 * Implements a fragment to load and display SmartDial search results.
 */
public class SmartDialSearchFragment extends SearchFragment
        implements EmptyContentView.OnEmptyViewActionButtonClickedListener {
    private static final String TAG = SmartDialSearchFragment.class.getSimpleName();
    private View digits_container;
    
    public SmartDialSearchFragment(int listViewTranslationYHeight,View digits_container){
    	this.listViewTranslationYHeight=listViewTranslationYHeight;
    	this.digits_container=digits_container;
    }
    
    public SmartDialSearchFragment(){
    	Log.d(TAG,"SmartDialSearchFragment");
    }
    

    @Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        mContactsHelper.setForDialpadT9(true);
        mContactsAdapter = new MstSearchContactsAdapter(getContext(),mContactsHelper
                        .getSearchContacts(),true);
        mListView.setAdapter(mContactsAdapter);
        
//        mListView.setBackgroundColor(super.getContext().getResources().getColor(R.color.mst_dialpad_listview_bg_color));
        ((View)mListView.getParent()).setBackgroundColor(super.getContext().getResources().getColor(R.color.mst_dialpad_listview_bg_color));
    }
    
    private static final int CALL_PHONE_PERMISSION_REQUEST_CODE = 1;

    /**
     * Creates a SmartDialListAdapter to display and operate on search results.
     */
    @Override
    protected ContactEntryListAdapter createListAdapter() {
        SmartDialNumberListAdapter adapter = new SmartDialNumberListAdapter(getActivity());
        adapter.setUseCallableUri(super.usesCallableUri());
        adapter.setQuickContactEnabled(true);
        // Set adapter's query string to restore previous instance state.
        adapter.setQueryString(getQueryString());
        return adapter;
    }
    
    private int listViewTranslationYHeight;
	@Override
    public void onStart() {
        super.onStart();
        if(digits_container!=null){
            digits_container.setVisibility(View.VISIBLE);
        }
        getView().setTranslationY(listViewTranslationYHeight);
    }
    /**
     * Creates a SmartDialCursorLoader object to load query results.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    	Log.d(TAG,"onCreateLoader,id:"+id+" args:"+args);
        // Smart dialing does not support Directory Load, falls back to normal search instead.
        if (id == getDirectoryLoaderId()) {
            return super.onCreateLoader(id, args);
        } else {
            final SmartDialNumberListAdapter adapter = (SmartDialNumberListAdapter) getAdapter();
            /// M: [MTK Dialer Search] @{
            if (DialerFeatureOptions.isDialerSearchEnabled()) {
                DialerSearchCursorLoader loader = new DialerSearchCursorLoader(super.getContext(),
                        usesCallableUri());
                adapter.configureLoader(loader);
                Log.d(TAG,"onCreateLoader1");
                return loader;
            /// @}
            } else {
                SmartDialCursorLoader loader = new SmartDialCursorLoader(super.getContext());
                adapter.configureLoader(loader);
                Log.d(TAG,"onCreateLoader2");
                return loader;
            }
        }
    }

    /**
     * Gets the Phone Uri of an entry for calling.
     * @param position Location of the data of interest.
     * @return Phone Uri to establish a phone call.
     */
    @Override
    protected Uri getPhoneUri(int position) {
        final SmartDialNumberListAdapter adapter = (SmartDialNumberListAdapter) getAdapter();
        return adapter.getDataUri(position);
    }

    @Override
    protected void setupEmptyView() {
        if (mEmptyView != null && getActivity() != null) {
            if (!PermissionsUtil.hasPermission(getActivity(), CALL_PHONE)) {
                mEmptyView.setImage(R.drawable.empty_contacts);
                mEmptyView.setActionLabel(R.string.permission_single_turn_on);
                mEmptyView.setDescription(R.string.permission_place_call);
                mEmptyView.setActionClickedListener(this);
            } else {
                mEmptyView.setImage(EmptyContentView.NO_IMAGE);
                mEmptyView.setActionLabel(EmptyContentView.NO_LABEL);
                mEmptyView.setDescription(EmptyContentView.NO_LABEL);
            }
        }
    }

    @Override
    public void onEmptyViewActionButtonClicked() {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        requestPermissions(new String[] {CALL_PHONE}, CALL_PHONE_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults) {
        if (requestCode == CALL_PHONE_PERMISSION_REQUEST_CODE) {
            setupEmptyView();
        }
    }

    public boolean isShowingPermissionRequest() {
        return mEmptyView != null && mEmptyView.isShowingContent();
    }
}
