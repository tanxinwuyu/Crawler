package com.axon.crawler;

import java.io.File;
import java.io.FileFilter;

public class MyFileFilter implements FileFilter {

	public boolean accept(File pathname) {
		if(pathname.getName().startsWith("3_weixin.log.2016"))
		     return true;
		return false;
	}

}
