/*
* Copyright (C) 2015 The Android Open Source Project
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

package com.monster.permission.ui;

import android.annotation.Nullable;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.impl.GetCommand;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import mst.preference.SwitchPreference;
import mst.preference.Preference;
import mst.preference.Preference.OnPreferenceChangeListener;
import mst.preference.Preference.OnPreferenceClickListener;
import mst.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.monster.appmanager.R;
import com.monster.appmanager.widget.PermissionsSelectPreference;
import com.monster.appmanager.utils.AppPermissionGroup;
import com.monster.appmanager.utils.AppPermissions;
import com.monster.appmanager.utils.LocationUtils;
import com.monster.appmanager.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public final class AppPermissionsFragment extends PermissionsFrameFragment
        implements OnPreferenceChangeListener {

    private static final String LOG_TAG = "ManagePermsFragment";

    static final String EXTRA_HIDE_INFO_BUTTON = "hideInfoButton";

    private List<AppPermissionGroup> mToggledGroups;
    private AppPermissions mAppPermissions;
    private PreferenceScreen mExtraScreen;

    private boolean mHasConfirmedRevoke;

    public static AppPermissionsFragment newInstance(String packageName) {
        return setPackageName(new AppPermissionsFragment(), packageName);
    }

    private static <T extends Fragment> T setPackageName(T fragment, String packageName) {
        Bundle arguments = new Bundle();
        arguments.putString(Intent.EXTRA_PACKAGE_NAME, packageName);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        final ActionBar ab = getActivity().getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        String packageName = getArguments().getString(Intent.EXTRA_PACKAGE_NAME);
        Activity activity = getActivity();
        PackageInfo packageInfo = getPackageInfo(activity, packageName);
        if (packageInfo == null) {
            Toast.makeText(activity, R.string.app_not_found_dlg_title, Toast.LENGTH_LONG).show();
            activity.finish();
            return;
        }

        mAppPermissions = new AppPermissions(activity, packageInfo, null, true, new Runnable() {
            @Override
            public void run() {
                getActivity().finish();
            }
        });
        loadPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAppPermissions.refresh();
        setPreferencesCheckedState();
        onSetEmptyText();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                getActivity().finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mAppPermissions != null) {
            bindUi(getActivity(), mAppPermissions.getPackageInfo());
        }
    }

    private static void bindUi(Activity activity, PackageInfo packageInfo) {
        PackageManager pm = activity.getPackageManager();
        ApplicationInfo appInfo = packageInfo.applicationInfo;
        Intent infoIntent = null;
        if (!activity.getIntent().getBooleanExtra(EXTRA_HIDE_INFO_BUTTON, false)) {
            infoIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", packageInfo.packageName, null));
        }

        Drawable icon = appInfo.loadIcon(pm);
        CharSequence label = appInfo.loadLabel(pm);

        ActionBar ab = activity.getActionBar();
        if (ab != null) {
            ab.setTitle(R.string.app_permissions);
        }
    }

    private void loadPreferences() {
        Context context = getContext();
        if (context == null) {
            return;
        }

        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();

        if (mExtraScreen != null) {
            mExtraScreen.removeAll();
        }

        final Preference extraPerms = new Preference(context);
        extraPerms.setIcon(R.drawable.ic_toc);
        extraPerms.setTitle(R.string.additional_permissions);
        
        PackageManager pm = getContext().getPackageManager();
        ApplicationInfo appInfo = mAppPermissions.getPackageInfo().applicationInfo;
        Drawable appIcon = appInfo.loadIcon(pm);
        CharSequence label = appInfo.loadLabel(pm);
    
        //头部，包括图标,名称,版本号
        Preference preferenceHead = new Preference(context);
        preferenceHead.setLayoutResource(R.layout.preference_app_info);
        preferenceHead.setIcon(appIcon);
        preferenceHead.setTitle(label);
		try {
			PackageInfo pi = pm.getPackageInfo(appInfo.packageName, 0);
	        preferenceHead.setSummary(getResources().getString(R.string.version_text, pi.versionName));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}  
        screen.addPreference(preferenceHead);

        Preference preferenceTitle = new Preference(context);
        preferenceTitle.setLayoutResource(R.layout.preference_app_info);
        screen.addPreference(preferenceTitle);
        int i=0;
        for (AppPermissionGroup group : mAppPermissions.getPermissionGroups()) {
            if (!Utils.shouldShowPermission(group, mAppPermissions.getPackageInfo().packageName)) {
                continue;
            }

            boolean isPlatform = group.getDeclaringPackage().equals(Utils.OS_PKG);
            
            SwitchPreference preference = new SwitchPreference(context);
            preference.setOnPreferenceChangeListener(this);
            preference.setKey(group.getName());
            Drawable icon = Utils.loadDrawable(context.getPackageManager(), group.getIconPkg(), group.getIconResId());
            /*preference.setIcon(Utils.applyTint(getContext(), icon,
                    android.R.attr.colorControlNormal));*/
            preference.setIcon(icon);
            preference.setTitle(group.getLabel());
            if (group.isPolicyFixed()) {
                preference.setSummary(getString(R.string.permission_summary_enforced_by_policy));
            }
            preference.setPersistent(false);
            preference.setEnabled(!group.isPolicyFixed());
            preference.setChecked(group.areRuntimePermissionsGranted());

            if (isPlatform) {
                screen.addPreference(preference);
                i++;
                preferenceTitle.setTitle(getResources().getString(R.string.sensitive_authority, Integer.valueOf(i).toString()));
            } else {
                if (mExtraScreen == null) {
                    mExtraScreen = getPreferenceManager().createPreferenceScreen(context);
                }
                mExtraScreen.addPreference(preference);
            }
        }

        if (mExtraScreen != null) {
            extraPerms.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AdditionalPermissionsFragment frag = new AdditionalPermissionsFragment();
                    setPackageName(frag, getArguments().getString(Intent.EXTRA_PACKAGE_NAME));
                    frag.setTargetFragment(AppPermissionsFragment.this, 0);
                    getFragmentManager().beginTransaction()
                            .replace(com.mst.R.id.content, frag)
                            .addToBackStack(null)
                            .commit();
                    return true;
                }
            });
            int count = mExtraScreen.getPreferenceCount();
            extraPerms.setSummary(getResources().getQuantityString(
                    R.plurals.additional_permissions_more, count, count));
            screen.addPreference(extraPerms);
        }
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, Object newValue) {
        String groupName = preference.getKey();
        final AppPermissionGroup group = mAppPermissions.getPermissionGroup(groupName);

        if (group == null) {
            return false;
        }

        ManagePermissionsInfoActivity activity = (ManagePermissionsInfoActivity) getActivity();
        if (activity.isObscuredTouch()) {
            activity.showOverlayDialog();
            return false;
        }

        addToggledGroup(group);

        if (LocationUtils.isLocationGroupAndProvider(group.getName(), group.getApp().packageName)) {
            LocationUtils.showLocationDialog(getContext(), mAppPermissions.getAppLabel());
            return false;
        }
        if (newValue == Boolean.TRUE) {
            group.grantRuntimePermissions(false);
        } else {
            final boolean grantedByDefault = group.hasGrantedByDefaultPermission();
            if (grantedByDefault || (!group.hasRuntimePermission() && !mHasConfirmedRevoke)) {
                new AlertDialog.Builder(getContext())
                        .setMessage(grantedByDefault ? R.string.system_warning
                                : R.string.old_sdk_deny_warning)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.grant_dialog_button_deny,
                                new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((SwitchPreference) preference).setChecked(false);
                                group.revokeRuntimePermissions(false);
                                if (!grantedByDefault) {
                                    mHasConfirmedRevoke = true;
                                }
                            }
                        })
                        .show();
                return false;
            } else {
                group.revokeRuntimePermissions(false);
            }
        }

        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        logToggledGroups();
    }

    private void addToggledGroup(AppPermissionGroup group) {
        if (mToggledGroups == null) {
            mToggledGroups = new ArrayList<>();
        }
        // Double toggle is back to initial state.
        if (mToggledGroups.contains(group)) {
            mToggledGroups.remove(group);
        } else {
            mToggledGroups.add(group);
        }
    }

    private void logToggledGroups() {
        if (mToggledGroups != null) {
            mToggledGroups = null;
        }
    }

    private void setPreferencesCheckedState() {
        setPreferencesCheckedState(getPreferenceScreen());
        if (mExtraScreen != null) {
            setPreferencesCheckedState(mExtraScreen);
        }
    }

    private void setPreferencesCheckedState(PreferenceScreen screen) {
        int preferenceCount = screen.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = screen.getPreference(i);
            if (preference instanceof SwitchPreference) {
                SwitchPreference switchPref = (SwitchPreference) preference;
                AppPermissionGroup group = mAppPermissions.getPermissionGroup(switchPref.getKey());
                if (group != null) {
                    switchPref.setChecked(group.areRuntimePermissionsGranted());
                }
            }
        }
    }

    private static PackageInfo getPackageInfo(Context activity, String packageName) {
        try {
            return activity.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static class AdditionalPermissionsFragment extends PermissionsFrameFragment {
        AppPermissionsFragment mOuterFragment;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            mOuterFragment = (AppPermissionsFragment) getTargetFragment();
            super.onCreate(savedInstanceState);
            onCreatePreferences();
        }

        public void onCreatePreferences() {
            setPreferenceScreen(mOuterFragment.mExtraScreen);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            String packageName = getArguments().getString(Intent.EXTRA_PACKAGE_NAME);
            bindUi(getActivity(), getPackageInfo(getActivity(), packageName));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
