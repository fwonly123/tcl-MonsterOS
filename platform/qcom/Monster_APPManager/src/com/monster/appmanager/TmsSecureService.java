package com.monster.appmanager;

import tmsdk.common.TMSService;
import android.content.Intent;

/**
 * 常驻内存的后台服务
 * @author boyliang
 */
public final class TmsSecureService extends TMSService {

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}
	
}
