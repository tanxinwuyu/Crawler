package com.axon.crawler;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

public class MyFileFilter implements FileFilter {
	private static Logger logger = Logger.getLogger(MyFileFilter.class);

	public boolean accept(File pathname) {
		logger.info("传过来的文件名："+pathname.getName());
		// String string ="3_weixin.log.2016-08-10-10";
		if (pathname.getName().startsWith("3_weixin.log." + getCal())) {
			
			return true;
		}
		return false;
	}

	public String getCal() {
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = sdf.format(date);
		return dateString;

	}
	/*
	 * public static void main(String [] arges){
	 * 
	 * String string ="3_weixin.log.2016-08-10-10"; MyFileFilter myFileFilter =
	 * new MyFileFilter();
	 * System.out.println(string.startsWith("3_weixin.log."+myFileFilter
	 * .getCal())); }
	 */
}
