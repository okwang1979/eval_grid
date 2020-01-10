package nc.impl.hbbb.backplugin;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.vo.pub.BusinessException;

public class DBUtil {
	// Oracle驱动包
	private static final String driver = "oracle.jdbc.driver.OracleDriver";
	// 连接Oracle数据库的URL
	// TODO: 2016/11/11 对于数据库URL、用户名、密码可以动态输入
	private static String url = null;
	// 数据库用户名
	private static String username = null;
	// 数据库密码
	private static String pwd = null;

	// 连接对象
	private static Connection conn;
	// 参数语句对象
	private static PreparedStatement ps;

	static {
		try {
			url = PropertyTool.GetValueByKey(PropertyTool.DBFILE, "url");//"jdbc:oracle:thin:@10.16.3.84:1521/orcl";
			username = PropertyTool.GetValueByKey(PropertyTool.DBFILE, "username");
			pwd = PropertyTool.GetValueByKey(PropertyTool.DBFILE, "pwd");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * 获取连接对象
	 * 
	 * @return 连接对象
	 * @throws BusinessException
	 *             创建对象时抛出的异常
	 */
	public static Connection getConnection() throws BusinessException {
		try {
			// 加载驱动Oracle的jdbc驱动包
			Class.forName(driver);

			// 建立连接 ：制定连接到哪里去jdbc:oracle:thin: ip地址 : 端口号 : 服务
			conn = DriverManager.getConnection(url, username, pwd);

			if (conn != null) {
				System.out.println("连接成功");
			}
		} catch (ClassNotFoundException e) {
			throw new BusinessException("驱动加载失败，请检查驱动包");
		} catch (SQLException e) {
			throw new BusinessException("连接数据库服务器失败，请检查网络或相关参数");
		}
		return conn;
	}

	/**
	 * 只关闭连接
	 */
	public static void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 关闭传入的连接
	 * 
	 * @param conn
	 *            连接对象
	 */
	public static void close(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 关闭指定的连接和参数语句对象
	 * 
	 * @param conn
	 *            连接对象
	 * @param ps
	 *            参数语句对象
	 */
	public static void close(Connection conn, PreparedStatement ps) {

		try {
			if (ps != null) {
				ps.close();
			}
			if (conn != null) {

				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭连接和参数语句对象及结果集
	 * 
	 * @param conn
	 *            连接对象
	 * @param ps
	 *            参数语句对象
	 * @param resultSet
	 *            结果集
	 */
	public static void close(Connection conn, PreparedStatement ps, ResultSet resultSet) {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭最近获得的连接和参数语句对象
	 * 
	 * @param ps
	 *            参数语句对象
	 */
	public static void close(PreparedStatement ps) {

		try {
			if (ps != null) {
				ps.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭最近获得的连接和参数语句对象及结果集
	 * 
	 * @param ps
	 *            参数语句对象
	 * @param resultSet
	 *            结果集
	 */
	public static void close(PreparedStatement ps, ResultSet resultSet) {

		try {
			if (resultSet != null) {
				resultSet.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭最近获得的连接和最近参数语句对象及结果集
	 * 
	 * @param resultSet
	 *            结果集
	 */
	public static void close(ResultSet resultSet) {

		try {
			if (resultSet != null) {
				resultSet.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭最近获取的连接和CallableStatement和结果集
	 * 
	 * @param prepareCall
	 * @param resultSet
	 *            结果集
	 */
	public static void Close(CallableStatement prepareCall, ResultSet resultSet) {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
			if (prepareCall != null) {
				prepareCall.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 支持参数查询并返回结果集
	 * 
	 * @param sql
	 *            sql语句
	 * @param objects
	 *            动态参数列表
	 * @return 结果集
	 * @throws BusinessException
	 *             创建SQL语句对象抛出的异常
	 */
	// TODO: 2016/11/11 可以将sql语句拆分，通过输入参数合成SQL语句
	public static ResultSet search(String sql, Object... objects) throws BusinessException {
		Connection conn = getConnection();
		ResultSet resultSet;
		try {
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < objects.length; i++) {
				ps.setObject(i + 1, objects[i]);
			}
			resultSet = ps.executeQuery();
		} catch (SQLException e) {
			throw new BusinessException("创建SQL语句对象失败");
		}
		return resultSet;
	}
	
	
	
	/**
	 * 支持参数查询并返回结果集
	 * 
	 * @param sql
	 *            sql语句
	 * @param objects
	 *            动态参数列表
	 * @return 结果集
	 * @throws BusinessException
	 *             创建SQL语句对象抛出的异常
	 */
	public static Object queryData(String sql,ResultSetProcessor process, Object... objects) throws BusinessException {
		Connection conn = getConnection();
		ResultSet resultSet;
		
		PreparedStatement ps= null;
		try {
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < objects.length; i++) {
				ps.setObject(i + 1, objects[i]);
			}
			resultSet = ps.executeQuery();
			
			return process.handleResultSet(resultSet);
		} catch (Exception e) {
			throw new BusinessException("创建SQL语句对象失败");
		}
		 
	}

	/**
	 * 支持参数查询并返回一个对象 类似select count(*) from emp;
	 * 
	 * @param sql
	 *            sql语句
	 * @param objects
	 *            动态参数列表
	 * @return 查询到的对象
	 * @throws BusinessException
	 *             创建SQL语句对象抛出的异常
	 */
	public static Object searchObjects(String sql, Object... objects) throws BusinessException {
		Connection conn = getConnection();
		ResultSet resultSet;
		Object object = null;
		try {
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < objects.length; i++) {
				ps.setObject(i + 1, objects[i]);
			}
			resultSet = ps.executeQuery();
			resultSet.next();
			object = resultSet.getObject(1);
			close(ps, resultSet);
		} catch (SQLException e) {
			throw new BusinessException("创建SQL语句对象失败");
		}
		return object;
	}

	/**
	 * 执行插入、删除、修改等更新数据库的操作
	 * 
	 * @param sql
	 *            SQL语句
	 * @param objects
	 *            参数化对象
	 * @return 影响的行数
	 * @throws BusinessException
	 *             创建SQL语句对象抛出的异常
	 */
	// TODO: 2016/11/11 可以将增、删、改等操作分开成独立的方法，并将sql语句拆分，通过输入参数合成SQL语句
	public static int update(String sql, Object... objects) throws BusinessException {
		Connection conn = DBUtil.getConnection();
		PreparedStatement	ps =null;
		int rows;
		try {
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < objects.length; i++) {
				ps.setObject(i + 1, objects[i]);
			}
			rows = ps.executeUpdate();
			close(ps);
		} catch (SQLException e) {
			throw new BusinessException("创建SQL语句对象失败");
		}finally{
			close(conn,ps);
		}
		return rows;
	}

	/**
	 * 开启事务(多用于批量数据)
	 * 
	 * @param conn
	 *            连接对象
	 * @throws BusinessException
	 *             开启食物出错抛出的异常
	 */
	public static void beginTransction(Connection conn) throws BusinessException {
		try {
			conn.setAutoCommit(false);// 关闭事务自动提交机制
		} catch (SQLException e) {
			throw new BusinessException("开启食物出错");
		}
	}

	/**
	 * 提交事务
	 * 
	 * @param conn
	 *            连接对象
	 * @throws BusinessException
	 *             提交食物出错抛出的异常
	 */
	public static void commit(Connection conn) throws BusinessException {
		try {
			conn.commit();
			conn.setAutoCommit(true);// 恢复事务自动提交机制
		} catch (SQLException e) {
			throw new BusinessException("事务提交失败");
		}
	}

	/**
	 * 回滚事务
	 * 
	 * @param conn
	 *            连接对象
	 * @throws BusinessException
	 *             回滚食物出错抛出的异常
	 */
	public static void rollback(Connection conn) throws BusinessException {
		try {
			conn.rollback();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			throw new BusinessException("回滚事务失败");
		}
	}

}