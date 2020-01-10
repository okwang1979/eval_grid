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
	// Oracle������
	private static final String driver = "oracle.jdbc.driver.OracleDriver";
	// ����Oracle���ݿ��URL
	// TODO: 2016/11/11 �������ݿ�URL���û�����������Զ�̬����
	private static String url = null;
	// ���ݿ��û���
	private static String username = null;
	// ���ݿ�����
	private static String pwd = null;

	// ���Ӷ���
	private static Connection conn;
	// ����������
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
	 * ��ȡ���Ӷ���
	 * 
	 * @return ���Ӷ���
	 * @throws BusinessException
	 *             ��������ʱ�׳����쳣
	 */
	public static Connection getConnection() throws BusinessException {
		try {
			// ��������Oracle��jdbc������
			Class.forName(driver);

			// �������� ���ƶ����ӵ�����ȥjdbc:oracle:thin: ip��ַ : �˿ں� : ����
			conn = DriverManager.getConnection(url, username, pwd);

			if (conn != null) {
				System.out.println("���ӳɹ�");
			}
		} catch (ClassNotFoundException e) {
			throw new BusinessException("��������ʧ�ܣ�����������");
		} catch (SQLException e) {
			throw new BusinessException("�������ݿ������ʧ�ܣ������������ز���");
		}
		return conn;
	}

	/**
	 * ֻ�ر�����
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
	 * �رմ��������
	 * 
	 * @param conn
	 *            ���Ӷ���
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
	 * �ر�ָ�������ӺͲ���������
	 * 
	 * @param conn
	 *            ���Ӷ���
	 * @param ps
	 *            ����������
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
	 * �ر����ӺͲ��������󼰽����
	 * 
	 * @param conn
	 *            ���Ӷ���
	 * @param ps
	 *            ����������
	 * @param resultSet
	 *            �����
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
	 * �ر������õ����ӺͲ���������
	 * 
	 * @param ps
	 *            ����������
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
	 * �ر������õ����ӺͲ��������󼰽����
	 * 
	 * @param ps
	 *            ����������
	 * @param resultSet
	 *            �����
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
	 * �ر������õ����Ӻ�������������󼰽����
	 * 
	 * @param resultSet
	 *            �����
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
	 * �ر������ȡ�����Ӻ�CallableStatement�ͽ����
	 * 
	 * @param prepareCall
	 * @param resultSet
	 *            �����
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
	 * ֧�ֲ�����ѯ�����ؽ����
	 * 
	 * @param sql
	 *            sql���
	 * @param objects
	 *            ��̬�����б�
	 * @return �����
	 * @throws BusinessException
	 *             ����SQL�������׳����쳣
	 */
	// TODO: 2016/11/11 ���Խ�sql����֣�ͨ����������ϳ�SQL���
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
			throw new BusinessException("����SQL������ʧ��");
		}
		return resultSet;
	}
	
	
	
	/**
	 * ֧�ֲ�����ѯ�����ؽ����
	 * 
	 * @param sql
	 *            sql���
	 * @param objects
	 *            ��̬�����б�
	 * @return �����
	 * @throws BusinessException
	 *             ����SQL�������׳����쳣
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
			throw new BusinessException("����SQL������ʧ��");
		}
		 
	}

	/**
	 * ֧�ֲ�����ѯ������һ������ ����select count(*) from emp;
	 * 
	 * @param sql
	 *            sql���
	 * @param objects
	 *            ��̬�����б�
	 * @return ��ѯ���Ķ���
	 * @throws BusinessException
	 *             ����SQL�������׳����쳣
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
			throw new BusinessException("����SQL������ʧ��");
		}
		return object;
	}

	/**
	 * ִ�в��롢ɾ�����޸ĵȸ������ݿ�Ĳ���
	 * 
	 * @param sql
	 *            SQL���
	 * @param objects
	 *            ����������
	 * @return Ӱ�������
	 * @throws BusinessException
	 *             ����SQL�������׳����쳣
	 */
	// TODO: 2016/11/11 ���Խ�����ɾ���ĵȲ����ֿ��ɶ����ķ���������sql����֣�ͨ����������ϳ�SQL���
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
			throw new BusinessException("����SQL������ʧ��");
		}finally{
			close(conn,ps);
		}
		return rows;
	}

	/**
	 * ��������(��������������)
	 * 
	 * @param conn
	 *            ���Ӷ���
	 * @throws BusinessException
	 *             ����ʳ������׳����쳣
	 */
	public static void beginTransction(Connection conn) throws BusinessException {
		try {
			conn.setAutoCommit(false);// �ر������Զ��ύ����
		} catch (SQLException e) {
			throw new BusinessException("����ʳ�����");
		}
	}

	/**
	 * �ύ����
	 * 
	 * @param conn
	 *            ���Ӷ���
	 * @throws BusinessException
	 *             �ύʳ������׳����쳣
	 */
	public static void commit(Connection conn) throws BusinessException {
		try {
			conn.commit();
			conn.setAutoCommit(true);// �ָ������Զ��ύ����
		} catch (SQLException e) {
			throw new BusinessException("�����ύʧ��");
		}
	}

	/**
	 * �ع�����
	 * 
	 * @param conn
	 *            ���Ӷ���
	 * @throws BusinessException
	 *             �ع�ʳ������׳����쳣
	 */
	public static void rollback(Connection conn) throws BusinessException {
		try {
			conn.rollback();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			throw new BusinessException("�ع�����ʧ��");
		}
	}

}