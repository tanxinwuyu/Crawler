package com.axon.crawler;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class LoadJar {
	public static void main(String[] args) throws Exception {
		File rootDir = new File(".");
		File libDir = new File(rootDir, "lib");
		List<URL> urlList = new ArrayList<URL>();
		loadJarList(libDir, urlList);
		URLClassLoader classLoader = new URLClassLoader(urlList.toArray(new URL[0]), LoadJar.class.getClassLoader());
		Thread.currentThread().setContextClassLoader(classLoader);// 设置主线程的类加载器
		File file = new File("/home/tagdata");
		Crawler cw = new Crawler();
		cw.getUrl(file);
	}
	
	private static void loadJarList(File dir, List<URL> list)
	{
		File[] files = dir.listFiles();
		for (File file : files)
		{
			if (file.isDirectory())
			{
				loadJarList(file, list);
			}
			else if (file.isFile())
			{
				String fileName = file.getName().toLowerCase();
				if (fileName.endsWith(".jar"))
				{
					try
					{
						URL url = file.toURI().toURL();
						list.add(url);
					}
					catch (MalformedURLException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

}
