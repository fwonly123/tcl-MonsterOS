package com.monster.market.http;

import android.os.AsyncTask;

public class RequestTask extends AsyncTask<Object, Integer, Object> {
	
	protected Request request;
	  
    public RequestTask(Request request) {  
        this.request = request;
    }  

	@Override
	protected Object doInBackground(Object... arg0) {
		try {
			return HttpRequestUtil.excute(request);
		} catch (Exception e) {
			return e;
		}
	}

	@Override
	protected void onPostExecute(Object result) {
		if (request.iHttpCallback != null) {
			if (result instanceof Exception) {// 失败
				request.iHttpCallback.onErrorResponse((RequestError) result);
			} else {// 成功
				request.iHttpCallback.onResponse(result.toString());
			}
		}
	}

}
