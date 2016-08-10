package com.axon.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.axon.crawler.bean.infoBean;
import com.axon.mysql.MysqlUtils;

@SuppressWarnings("deprecation")
public class Crawler {
	private static Log logger = LogFactory.getLog(Crawler.class);
	private final static int DEFAULT_VALUE = 2 << 29;
	private BitSet bs = new BitSet(DEFAULT_VALUE);
	private int[] seeds = { 3, 11, 19, 29, 37, 43, 61, 83 };
	private BloomFiler[] bfFuncs = new BloomFiler[seeds.length];

	public Crawler() {
		init();
	}

	/**
	 * @param args
	 */
	/*
	 * public static void main(String[] args) { File file = new
	 * File("/home/sftpclient/file_backup");//"" Crawler cw = new Crawler();
	 * cw.getUrl(file); }
	 */
	// 获取正确的url
	public void getUrl(File fileDir) {
		logger.info("开始获取文件");
		FileReader fr = null;
		BufferedReader bufr = null;
		int count = 0;
		int fileCount = 0;
		Set<infoBean> set = new HashSet<infoBean>();
		MyFileFilter fileFilter = new MyFileFilter();
		File[] fileList = fileDir.listFiles();
		logger.info("文件个数：" + fileList.length);
		try {
			for (File file : fileList) {
				logger.info("文件名称为：" + file.getName());
				if (file.isFile() && fileFilter.accept(file)) {
					logger.info("符合标准的文件名称为：" + file.getName());
					fr = new FileReader(file);
					bufr = new BufferedReader(fr);
					String line = null;
					while ((line = bufr.readLine()) != null) {

						String[] str = line.split("\t");
						String phone = str[0];
						String date = str[1];
						String biz = str[2];
						String path = str[4];
						if (str.length < 4)
							continue;
						if (null != str[3] && !path.startsWith("/s?__biz=")) {
							continue;
						}
						if (isExist(biz)) {
							continue;
						}
						count++;
						// logger.info(count + "");
						infoBean info = new infoBean();
						info.setBiz(biz);
						info.setPhone(phone);
						info.setDate(date);
						info.setUrl("http://mp.weixin.qq.com" + path);
						logger.info("****************************************************************************************************************");
						logger.info(info.toString());
						logger.info("****************************************************************************************************************");
						set.add(info);
						System.out.println("文件总条数：" + count);
					}
				} else {
					continue;
				}
				if (count % 10000 == 0) {
					accessUrl(set);
					set.clear();
				}
				logger.info(count);
				logger.info("访问最后剩下的url");
			}
			if (set.size() != 0) {
				accessUrl(set);
			}
		} catch (IOException e) {
			logger.info("io异常" + e);
		}

	}

	// 通过获取到的doc解析该页面，获取相关的值
	/**
	 * @param doc
	 */
	public void parserHtml(Set<infoBean> doc) {
		logger.info("解析html");
		// TreeMap<String, String> tm = getConfig();
		ArrayList<infoBean> al = new ArrayList<infoBean>();
		for (infoBean info : doc) {
			logger.info("开始扫描每个infobean进行doc解析");
			if (null == info.getDoc())
				continue;
			String text = info.getDoc().body().text();
			/*
			 * Elements name = info.getDoc()
			 * .getElementsByClass("profile_nickname"); Elements insduction =
			 * info.getDoc().getElementsByClass( "profile_meta_value"); //
			 * 那些只有name没有说明的全部过滤掉 if (null == name || name.size() <= 0 || null
			 * == insduction || insduction.size() <= 1) { continue; } //
			 * 过滤值为空格的数据 if
			 * (Jsoup.parse("&nbsp;").text().equals(insduction.get(1).text())) {
			 * continue; } info.setName(name.text()); // 说明中有乱七不糟的符号给过滤掉 String
			 * regex = "[\u4E00-\u9FA5]+"; Pattern pattern =
			 * Pattern.compile(regex); Matcher matcher =
			 * pattern.matcher(insduction.get(1).text()); StringBuilder sb = new
			 * StringBuilder(); while (matcher.find()) {
			 * sb.append(matcher.group()); }
			 */
			String regex = "[\u4E00-\u9FA5]+";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(text);
			StringBuilder sb = new StringBuilder();
			while (matcher.find()) {
				sb.append(matcher.group());
			}
			if ("".equals(text))
				continue;
			// info.setInstruction(sb.toString());
			info.setInstruction(sb.toString());
			/*
			 * Set<Map.Entry<String, String>> set = tm.entrySet(); for
			 * (Iterator<Map.Entry<String, String>> it = set.iterator(); it
			 * .hasNext();) { Map.Entry<String, String> mp = it.next(); String
			 * key = mp.getKey(); String value = mp.getValue(); if
			 * (info.getName().contains(key) ||
			 * info.getInstruction().contains(key)) { info.setTag(value); } else
			 * { continue; } }
			 */
			/*
			 * if (null == info.getTag()) info.setTag("");
			 * logger.info(info.toString());
			 */
			al.add(info);
		}
		logger.info("解析完html，马上开始插入mysql");
		insertSQL(al);
	}

	// 向mysql中插入数据
	private void insertSQL(List<infoBean> doc) {
		String sql = "insert into weixintext(date,phone,biz,url,infomation)values(?,?,?,?,?)";
		MysqlUtils wtm = new MysqlUtils();
		wtm.insertSQL(doc, sql);
	}

	/**
	 * 通过url，访问页面，并把该页面以doc形式返回
	 * 
	 * @param li
	 */
	@SuppressWarnings("unused")
	public void accessUrl(Set<infoBean> set) {
		Document doc = null;
		// System.out.println("accessUrl+++" + li.size());
		logger.info("开始获取页面信息");
		int count = 0;
		try {
			for (infoBean info : set) {
				count++;
				// doc =
				// Jsoup.connect("http://mp.weixin.qq.com/s?__biz=MzA3NTI2MjIyMQ==&idx=7&mid=2651113054&sn=e997f17cd4bb402e16997695a9fe7fcd").timeout(6000).get();
				doc = Jsoup.parse(getHtmlByUrl(info.getUrl()));
				// .parse(getHtmlByUrl("http://mp.weixin.qq.com/s?__biz=MzAxMDY1NTM3MA==&mid=2651910675&idx=1&sn=61449fa5261f8c4dec70dbdcacabf73c&scene=0&key=8dcebf9e179c9f3a8a3f9fc7595d5c00cb4d53b9775cf3ac591c8d66144f32c3dcd6d598de817ba9dabed6976644524d"));
				info.setDoc(doc);
				// System.out.println(info);
				/*
				 * System.out.println(doc); Elements name = doc
				 * .getElementsByClass("profile_nickname"); Elements insduction
				 * = doc.getElementsByClass( "profile_meta_value"); String text
				 * = doc.body().text();
				 */
				/*
				 * doc = Jsoup .connect(
				 * "http://mp.weixin.qq.com//mp/getappmsgext?__biz=MzA5NDEzMjYyNg==&appmsg_type=9&mid=2650700490&sn=3125459d7b0c50510ffff6921bcc94b9&idx=4&scene=0&title=%E4%B8%BA%E4%BB%80%E4%B9%88%E2%80%9C%E5%B0%8F%E4%B8%89%E2%80%9D%E8"
				 * ) .timeout(1000).get();
				 * 
				 * Elements name = doc.getElementsByClass("profile_nickname");
				 * Elements insduction = doc
				 * .getElementsByClass("profile_meta_value");
				 * 
				 * Element d = insduction.get(1); System.out.println(d.text());
				 * char a = d.text().charAt(0); String s = a + ""; String regex
				 * = "[\u4E00-\u9FA5]+"; Pattern pattern =
				 * Pattern.compile(regex); Matcher matcher =
				 * pattern.matcher(d.text()); while (matcher.find()) {
				 * System.out.println(matcher.group()); }
				 * System.out.println(matcher.find());
				 * System.out.println(d.text());
				 */

				// System.out.println(s.equals("\\?"));
				// if(" ".equals(insduction.get(1).text())){System.out.println("11111111");}
				// System.out.println("insduction大小："+insduction.size()+"详细情况"+insduction);
				/*
				 * if (insduction.size() == 0) continue; if
				 * (insduction.get(1).text()
				 * .contains(Jsoup.parse("&nbsp;").text())) { String[] str =
				 * insduction.get(1).text()
				 * .split(Jsoup.parse("&nbsp;").text());
				 * if(str.length==0)continue; if (str.length ==
				 * 2||str[0].length()==2) { logger.info("str cjangdu " +
				 * str.length); for (int i=0;i<str.length;i++) {
				 * logger.info("sb作者，居然用空格+&nbsp;害人");
				 * System.out.println(i+":::::::"
				 * +str[i]+"数组中字符串的长度"+str[i].length()); } }
				 */

				// logger.info(name.text()+"++++++++"+insduction.get(1).text());
			}
			// System.out.println(count);
		} catch (Exception e) {
			logger.error(e);
		}
		logger.info("结束获取页面信息，开始解析页面信息");
		parserHtml(set);
	}

	// httpclient进行网页爬虫
	public String getHtmlByUrl(String url) {
		logger.info("开始访问页面！！！！！！！！！！！！！！");
		String html = null;
		@SuppressWarnings("resource")
		HttpClient httpClient = new DefaultHttpClient();// 创建httpClient对象
		HttpGet httpget = new HttpGet(url);// 以get方式请求该URL
		try {
			HttpResponse responce = httpClient.execute(httpget);// 得到responce对象
			int resStatu = responce.getStatusLine().getStatusCode();// 返回码
			if (resStatu == HttpStatus.SC_OK) {// 200正常 其他就不对
				// 获得相应实体
				HttpEntity entity = responce.getEntity();
				if (entity != null) {
					html = EntityUtils.toString(entity);// 获得html源代码

				}
			}
		} catch (Exception e) {
			logger.error("出错" + e);
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return html;
	}

	// 只用于计算hash值
	public static class BloomFiler {
		private int cap;
		// seed为计算hash值的一个key值，具体对应上文中的seeds数组
		private int seed;

		public BloomFiler(int cap, int seed) {
			this.cap = cap;
			this.seed = seed;
		}

		public BloomFiler() {
		}

		public int getCap() {
			return cap;
		}

		public void setCap(int cap) {
			this.cap = cap;
		}

		public int getSeed() {
			return seed;
		}

		public void setSeed(int seed) {
			this.seed = seed;
		}

		public int hash(String value) {
			int result = 0;
			int length = value.length();
			for (int i = 0; i < length; i++) {
				result = seed * result + value.charAt(i);
			}
			return (cap - 1) & result;

		}
	}

	// 已经入库的biz号把他们映射到bitset中
	public void init() {
		logger.info("开始获取bloomfilter的位数据");
		setBitset();
		String sql = "SELECT DISTINCT biz from weixintext";
		MysqlUtils mysqlUtil = new MysqlUtils();
		Connection conn = mysqlUtil.connectionDB();
		PreparedStatement preparedStatement = null;
		// List<String> list = new ArrayList<String>();\ResultSet rs
		ResultSet rs = null;
		try {
			preparedStatement = conn.prepareStatement(sql);
			rs = preparedStatement.executeQuery();
			if (null == rs) {
				return;
			} else {
				while (rs.next()) {
					add(rs.getString(1));
				}
			}
		} catch (SQLException e) {
			logger.error(e);
		} finally {
			try {
				rs.close();
				conn.close();
			} catch (SQLException e) {
				logger.info("关闭异常", e);
			}

		}

	}

	// 给bitset设置值
	public void setBitset() {
		for (int i = 0; i < seeds.length; i++) {
			bfFuncs[i] = new BloomFiler(DEFAULT_VALUE, seeds[i]);
		}
	}

	// 给某些string设置其bitset8个位置的值
	public void add(String value) {
		for (BloomFiler bf : bfFuncs) {
			bs.set(bf.hash(value), true);
		}
	}

	// 判断该字符串是否已经爬过
	public boolean isExist(String str) {
		if (null == str)
			return false;
		// 如果判断8个hash函数值中有一个位置不存在即可判断为不存在Bloofilter中
		for (BloomFiler bf : bfFuncs) {
			if (!bs.get(bf.hash(str))) {
				return false;
			}
		}
		return true;

	}
}
