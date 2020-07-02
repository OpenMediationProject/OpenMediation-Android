package com.cloudtech.shell.utils;

import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.text.format.DateUtils.FORMAT_NUMERIC_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_YEAR;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

public class TimeUtils {

	//private final static SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	//private final static SimpleDateFormat dateFormater2 = new SimpleDateFormat("yyyy-MM-dd");

	public final static String YMD = "yyyy-MM-dd";
	public final static String YMDHM = "yyyy-MM-dd HH:mm";

	public final static String HMS = "HH:mm:ss.SSS";

	public final static String HM = "HH:mm";

	public final static String FULLFORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

	public final static String YMDHMFORMAT = "yyyy-MM-dd HH:mm:ss";

	public final static String YMDHMS_POINT = "yyyy.MM.dd HH:mm:ss";

	public final static String YMDHM_POINT = "yyyy.MM.dd HH:mm";

	public final static String MD_CN = "MM月dd日";

	public final static String MDHM_CN = "MM月dd日 HH:mm";

	private short now = 0;// 时
	private short minutes = 0;// 分
	private short seconds = 0;// 秒
	private int sumSeconds;//总共耗时多扫秒
	public static String getDateToString(Date date, String format) {
		if (date == null)
			return null;
		else {
			SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
			String dateString = formatter.format(date);
			return dateString;
		}
	}

	public static String getDateToString(long time, String format) {
		Date date=new Date(time);
			SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
			String dateString = formatter.format(date);
			return dateString;
	}

	public static Date getStringToDate(String date, String format) {
		if(date!=null){
			SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
			try {
				return formatter.parse(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 当前时间 精确到 年月日
	 *
	 * @return 年月日
	 */
	public static String currentDateStr() {
		SimpleDateFormat sdf = new SimpleDateFormat(YMD, Locale.getDefault());
		return sdf.format(new Date(System.currentTimeMillis()));
	}

	/**
	 * 当前时间 精确到 年月日
	 *
	 * @return date
	 */
	public static Date currentDate() {
		return new Date(System.currentTimeMillis());
	}

	public static String getFriendlyTime(long timeStamp){
		if(timeStamp == 0l){
			return "未知";
		}
		if(System.currentTimeMillis() - timeStamp <= 1000 * 60 * 3){
			return "刚刚";
		}
		SimpleDateFormat sdf = new SimpleDateFormat(HM, Locale.getDefault());
		return sdf.format(timeStamp);
	}

	/**
	 * 获取当前的年月日 时分秒
	 * @param format 格式定义
	 * @return
	 */
	public static String getCurrentTime(String format) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		return formatter.format(curDate);
	}

	/**
	 *  获取当前的年月日
	 * @param format 格式定义
	 * @return
	 */
	public static String getCurrentYMD(String format) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		return formatter.format(curDate);
	}
	public void setSumSeconds(int sumSeconds){
		this.sumSeconds=sumSeconds;
	}

	public int getSumSeconds(){
		return sumSeconds;
	}
	/**
	 * 计时
	 * 每调用一次加一秒，从0开始最大单位为小时超出24小时全部清零
	 * @return 比如：00:01或者01:01:34
	 */
	public String reckonByTime() {
		StringBuilder sb = new StringBuilder();
		seconds++;
		if (seconds > 59) {
			seconds = 0;
			minutes++;
			if (minutes > 59) {
				minutes = 0;
				now++;
				if (now > 24) {
					now = 0;
					minutes = 0;
					seconds = 0;
				}
			}
		}
		boolean flag = false;
		if (now > 0) {
			if (now <= 9) {
				sb.append("0").append(now);
			} else {
				sb.append(now);
			}
			flag=true;
		}
		if (minutes > 0) {
			if(flag)
				sb.append(":");
			else
				flag=true;
			if (minutes <= 9) {
				sb.append("0").append(minutes);
			} else {
				sb.append(minutes);
			}

		}else{
			sb.append("00:");
		}
		if (seconds > 0) {
			if(flag)
				sb.append(":");
			if (seconds <= 9) {
				sb.append("0").append(seconds);
			} else {
				sb.append(seconds);
			}
		}else{
			sb.append(":00");
		}
		sumSeconds++;
		return sb.toString();
	}

	/**
	 * 根据日期计算星期几
	 */
	public static int dayForWeek(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int dayForWeek = 0;
		if (c.get(Calendar.DAY_OF_WEEK) == 1) {
			dayForWeek = 7;
		} else {
			dayForWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
		}
		return dayForWeek;
	}

	/**
	 * 当前时间 精确到 年月日 时分秒
	 *
	 * @return 年月日 时分秒
	 */
	public static String currentTime() {
		long millis = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat(YMDHMFORMAT,
				Locale.getDefault());
		return sdf.format(new Date(millis));
	}


	/**
	 * 判断月份有多少天
	 */
	public static int monthToDay(Date date){
		Calendar c= Calendar.getInstance();
		c.setTime(date);
		int month=c.get(Calendar.MONTH)+1;
		if(month==1 || month==3 || month==5 || month==7 || month==8 || month==10 || month==12){
			return 31;
		}
		if(month==4 || month==6 || month==9 || month==11){
			return 30;
		}
		if(isLeapYear(c.get(Calendar.YEAR))){
			return 29;
		}else{
			return 28;
		}
	}

	/**
	 * 判断是否是今天
	 */
	public static boolean isToday(Date date) {
		Calendar c = Calendar.getInstance();
		int date1 = 0, date2 = 0;
		date1 += c.get(Calendar.YEAR);
		date1 += c.get(Calendar.MONTH) + 1;
		date1 += c.get(Calendar.DAY_OF_MONTH);
		c.setTime(date);
		date2 += c.get(Calendar.YEAR);
		date2 += c.get(Calendar.MONTH) + 1;
		date2 += c.get(Calendar.DAY_OF_MONTH);
		if (date1 == date2)
			return true;
		return false;
	}

	/**
	 * 根据周几数字返回对应字符
	 * @param day
	 * @return
	 */
	public static String dayToStr(int day){
		if(day==1){
			return "周一";
		}else if(day==2){
			return "周二";
		}else if(day==3){
			return "周三";
		}else if(day==4){
			return "周四";
		}else if(day==5){
			return "周五";
		}else if(day==6){
			return "周六";
		}else if(day==7){
			return "周日";
		}
		return null;
	}

	/**
	 * 判断是否是闰年
	 * @param year
	 * @return
	 */
	public static boolean isLeapYear(int year) {
		if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) {
			return true;
		} else {
			return false;
		}
	}
	private final static ThreadLocal<SimpleDateFormat> dateFormater = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		}
	};

	private final static ThreadLocal<SimpleDateFormat> dateFormater2 = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		}
	};

	/**
	 * 将字符串转位日期类型
	 *
	 */
	public static Date toDate(String sdate) {
		try {
			return dateFormater.get().parse(sdate);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * 以友好的方式显示时间
	 *
	 */
	public static String friendlyTime(String sdate) {
		Date time = toDate(sdate);
		if (time == null) {
			return "Unknown";
		}
		String ftime = "";
		Calendar cal = Calendar.getInstance();

		//判断是否是同一天
		String curDate = dateFormater2.get().format(cal.getTime());
		String paramDate = dateFormater2.get().format(time);
		if (curDate.equals(paramDate)) {
			int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
			if (hour == 0)
				ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000, 1) + "分钟前";
			else
				ftime = hour + "小时前";
			return ftime;
		}

		long lt = time.getTime() / 86400000;
		long ct = cal.getTimeInMillis() / 86400000;
		int days = (int) (ct - lt);
		if (days == 0) {
			int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
			if (hour == 0)
				ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000, 1) + "分钟前";
			else
				ftime = hour + "小时前";
		} else if (days == 1) {
			ftime = "昨天";
		} else if (days == 2) {
			ftime = "前天";
		} else if (days > 2 && days <= 10) {
			ftime = days + "天前";
		} else if (days > 10) {
			ftime = dateFormater2.get().format(time);
		}
		return ftime;
	}

	/**
	 * 判断给定字符串时间是否为今日
	 *
	 */
	public static boolean isToday(String sdate) {
		boolean b = false;
		Date time = toDate(sdate);
		Date today = new Date();
		if (time != null) {
			String nowDate = dateFormater2.get().format(today);
			String timeDate = dateFormater2.get().format(time);
			if (nowDate.equals(timeDate)) {
				b = true;
			}
		}
		return b;
	}


	/**
	 * Get relative time for date
	 *
	 */
	public static CharSequence getRelativeTime(final Date date) {
		long now = System.currentTimeMillis();
		if (Math.abs(now - date.getTime()) > 60000)
			return DateUtils.getRelativeTimeSpanString(date.getTime(), now,
					MINUTE_IN_MILLIS, FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR
							| FORMAT_NUMERIC_DATE);
		else
			return "just now";
	}
}
