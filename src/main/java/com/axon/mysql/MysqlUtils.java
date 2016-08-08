package com.axon.mysql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;





import com.axon.crawler.bean.ResultBean;
import com.mysql.jdbc.Connection;

public class MysqlUtils {
	private static Logger logger = Logger.getLogger(MysqlUtils.class);
	private final String username = "root";
	private final String password = "root123";
	private final String url = "jdbc:mysql://localhost:3306/weixin";

	// private Connection conn = null;

	public MysqlUtils() {
		// conn = connectionDB();
	}

	@SuppressWarnings("finally")
	public Connection connectionDB() {
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = (Connection) DriverManager.getConnection(url, username,
					password);
			// 关闭事务自动提交
			conn.setAutoCommit(false);
			System.out.println("连接数据库成功");
		} catch (Exception e) {
			logger.error(e);
		} finally {
			return conn;
		}
	}

	
	public void getData(File file) {
		FileReader fr = null;
		BufferedReader bufr = null;
		List<ResultBean> awbList = null;//new ArrayList<AddWeiBean>();
		int count1 = 0;
		try {
			fr = new FileReader(file);
			bufr = new BufferedReader(fr);
			String line = null;
			//int count1 = 0;
			awbList = new ArrayList<ResultBean>();
			HashMap<String, String> bizTagInfo = getBizInfo();
			while ((line = bufr.readLine()) != null) {
				   // str[tel,time,biz]
				   count1++;
				   String[] str = line.split("\t");
				   ResultBean info = new ResultBean();
				   String tag = bizTagInfo.get(str[2]);
					if (null == tag) {
						info.setTag("-1");
					} else {
						info.setTag(tag);
					}
					info.setTel(str[0]);
					awbList.add(info);
					logger.info(count1);
					logger.info("===============================================================");
					if(awbList.size()%100000==0){
						insert(awbList);
						awbList = new  ArrayList<ResultBean>();
					}
			}
		insert(awbList);
		logger.info("总共多少条数据："+count1);
		} catch (Exception e) {
			logger.error(e);
		}finally{
			try {
				bufr.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}
		
	}

	// 从数据库中获取biz号以及tag的对应
	public HashMap<String, String> getBizInfo() {
		HashMap<String, String> bizInfo = new HashMap<String, String>();
		String sql = "SELECT DISTINCT biz,tag from weixininfo";
		PreparedStatement pStatement = null;
		Connection conn = connectionDB();
		try {
			pStatement = conn.prepareStatement(sql);
			ResultSet rs = pStatement.executeQuery();
			while (rs.next()) {
				String biz = rs.getString(1);
				String tag = rs.getString(2);
				bizInfo.put(biz, tag);
			}
		} catch (SQLException e) {
			logger.error(e);
		} finally {
			try {
				pStatement.close();
				conn.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return bizInfo;
	}

	public void matchTag(HashMap<String, List<ResultBean>> hmFre) {
		HashMap<String, String> bizTagInfo = getBizInfo();
		Set<Map.Entry<String, List<ResultBean>>> set = hmFre.entrySet();
		List<ResultBean> AllList = new ArrayList<ResultBean>();
		for (Iterator<Map.Entry<String, List<ResultBean>>> it = set.iterator(); it
				.hasNext();) {
			Map.Entry<String, List<ResultBean>> me = it.next();
			//String tel = me.getKey();
			List<ResultBean> awList = me.getValue();
			for (int i = 0; i < awList.size(); i++) {
				ResultBean awb = awList.get(i);
				String tag = bizTagInfo.get(awb.getBiz());
				if (null == tag) {
					awb.setTag("-1");
				} else {
					awb.setTag(tag);
				}
			}
			AllList.addAll(awList);
			// if(AllList.size()>20000)return AllList;
		}

		insert(AllList);

	}

	public void insert(List<ResultBean> list) {
		logger.info("开始插入数据");
		String sql = "insert into weixintag (tel,tag)VALUES(?,?)";
		PreparedStatement pStatement = null;
		Connection conn = connectionDB();
		try {
			@SuppressWarnings("unused")
			int count = 0;
			pStatement = conn.prepareStatement(sql);
			for (ResultBean awb : list) {
				count++;
				pStatement.setString(1, awb.getTel());
				pStatement.setString(2, awb.getTag());
				pStatement.addBatch();
			}
			pStatement.executeBatch();
			conn.commit();
			pStatement.close();
			conn.close();
			conn = connectionDB();
			logger.info("插入结束");
		} catch (SQLException e) {
			logger.error(e);
		}

	}

}
