package com.axon.mysql;

import java.io.IOException;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.axon.crawler.bean.infoBean;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

/**
 * Hello world!
 *
 */
public class WriteToMysql {

	private static Log logger = LogFactory.getLog(WriteToMysql.class);
	private Connection conn = null;
	private final String url = "jdbc:mysql://192.200.196.14:3306/weixin";
	private final String username = "probe";
	private final String password = "probe@2011";
    private String sql = "insert into icase(date,phone,host,path)values(?,?,?,?)";

	// 在创建对象的时候就初始化一个连接对象
	public WriteToMysql() {
		conn = connectionDB();
	}

	@SuppressWarnings("finally")
	public Connection connectionDB() {
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

	// 从文件中读取数据存到读取数据
	@SuppressWarnings("resource")
	public void getDataFromFile() throws SQLException {
		InputStream in = this.getClass().getResourceAsStream("/aa.txt");
		List<String> line = null;
		PreparedStatement pStatement = (PreparedStatement) conn
				.prepareStatement(sql);
		try {
			line = IOUtils.readLines(in);
			int count = 0;
			for (String s : line) {
				String[] str = s.split("\\s+");
				// "insert into icase(date,phone,host,path)values(?,?,?)";
				conn.setAutoCommit(false);// 设置数据手动提交，自己管理事务
				count++;
				pStatement.setString(1, str[0].trim() + " " + str[1].trim());
				pStatement.setString(2, str[2].trim());
				pStatement.setString(3, str[3].trim());
				pStatement.setString(4, str[4].trim());
				pStatement.addBatch();
				if (count % 500 == 0) {// 当增加了500个批处理的时候再提交
					pStatement.executeBatch();// 执行批处理
					conn.commit();// 提交
					conn.close();// 关闭数据库
					conn = connectionDB();// 重新获取一次连接
					conn.setAutoCommit(false);
					pStatement = (PreparedStatement) conn.prepareStatement(sql);
				}
			}
			if (count % 500 != 0) {// while循环外的判断，为了防止上面判断后剩下最后少于500条的数据没有被插入到数据库
				pStatement.executeBatch();
				conn.commit();
			}
			pStatement.close();
			conn.close();
			pStatement.close();
		} catch (IOException e) {
			logger.error(e);
		}
	}

	// 对数据进行批处理
	// "insert into weixininfo(date,phone,biz,url,instruction)values(?,?,?,?,?)"";
	public void insertSQL(List<infoBean> list, String sql) {
		logger.info("插入的bean大小为："+list.size());
		PreparedStatement pStatement = null;
		infoBean info = null;
		//conn.setCharacterEncoding("utf-8");
		try {
			int count =0;
			pStatement = (PreparedStatement) conn.prepareStatement(sql);
			for (int i=0; i<list.size();i++ ) {
				count++;
				info = list.get(i);
				logger.info("电话号码：" + info.getPhone() + "+++++url:"
						+ info.getUrl() 
						+ "+++++getInstruction:" + info.getInstruction());
				//info= list.get(i);
				conn.setAutoCommit(false);// 设置数据手动提交，自己管理事务
				pStatement.setString(1, info.getDate().trim());
				pStatement.setString(2, info.getPhone().trim());
				pStatement.setString(3, info.getBiz().trim());
				pStatement.setString(4, info.getUrl().trim());
				pStatement.setString(5, info.getInstruction());
				pStatement.addBatch();
				//pStatement.execute();
			}
			logger.info("数量"+count);
			pStatement.executeBatch();
			conn.commit();
			pStatement.close();
			conn.close();
			logger.info("插入的bean大小为："+list.size());
			logger.info("结束插入");
		} catch (SQLException e) {
			logger.error("+++++url:"
					+ info.getUrl()+"Instruction::::"+info.getInstruction());
			logger.error(e);
			System.exit(0);
		}
	}
}
