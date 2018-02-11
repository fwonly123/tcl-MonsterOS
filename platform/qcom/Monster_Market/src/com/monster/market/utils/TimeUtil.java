package com.monster.market.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
	
	/**
	* @Title: getTimeString
	* @Description: 获取时间戳，格式为20140702102040
	* @param @param time
	* @param @return
	* @return String
	* @throws
	 */
	public static String getTimeString(long time) {
		Date date = new Date(time);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
		return dateFormat.format(date);
	}

	/**
	 * 获取现在时间
	 * @return 返回短时间格式 yyyy-MM-dd
	 */
	public static String getStringDateShort() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 获取时间 yyyy-MM-dd
	 * @param time
	 * @return
     */
	public static String getStringDateShort(long time) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = formatter.format(time);
		return dateString;
	}

}
