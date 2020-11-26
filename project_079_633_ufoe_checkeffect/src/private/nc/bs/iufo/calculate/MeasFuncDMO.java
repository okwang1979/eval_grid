package nc.bs.iufo.calculate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.pub.iufo.cache.KeyGroupCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pubitf.bd.accessor.GeneralAccessorFactory;
import nc.pubitf.bd.accessor.IGeneralAccessor;
import nc.vo.bd.accessor.IBDData;
import nc.vo.iufo.data.IKeyDetailData;
import nc.vo.iufo.data.KeyDetailDataUtil;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.MeasureTraceVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iufo.storecell.IStoreCell;
import nc.vo.pub.lang.UFDouble;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.script.expression.UfoExpr;
import com.ufsoft.script.extfunc.MeasFuncDriver;
import com.ufsoft.script.spreadsheet.UfoCalcEnv;
import com.ufsoft.table.CellPosition;

/**
 * ָ�꺯��DMO�������ͳ�ƺ�����ȡ�������ļ��㹦��
 * 
 * @author liuchuna
 * @created at 2010-6-8,����11:31:26
 *
 */
@SuppressWarnings("unchecked")
public class MeasFuncDMO extends nc.bs.iufo.DataManageObjectIufo {
	/**
	 * FuncDMO ������ע�⡣
	 * 
	 * @exception javax.naming.NamingException
	 *                �쳣˵����
	 * @exception nc.bs.pub.SystemException
	 *                �쳣˵����
	 */
	public MeasFuncDMO() throws javax.naming.NamingException,
			nc.bs.pub.SystemException {
		super();
	}

	/**
	 * FuncDMO ������ע�⡣
	 * 
	 * @param dbName
	 *            java.lang.String
	 * @exception javax.naming.NamingException
	 *                �쳣˵����
	 * @exception nc.bs.pub.SystemException
	 *                �쳣˵����
	 */
	public MeasFuncDMO(String dbName) throws javax.naming.NamingException,
			nc.bs.pub.SystemException {
		super(dbName);
	}

	/**
	 * ��ȡָ��ͳ�ƺ��������޶���ָ��ͳ��ֵ�� �������ڣ�(2003-8-11 20:53:46)
	 * 
	 * @param objMeasures
	 *            MeasureVO
	 * @param strCond
	 *            java.lang.String
	 * @param measFuncType
	 *            key=ָ��pk, value=hashtable(key=ͳ�ƺ�����,��Ӧ���ݿ�ͳ�ƺ���)
	 * @exception java.sql.SQLException
	 *                �쳣˵����
	 * @return hashtable(key=ָ��pk,value=hashtable(key=�ֶ�ͳ�ƺ�����(MSUM,MMAX,MMIN,MAVG),value=�ų���ǰ�ؼ�ֵ��ĺϼơ������С��������)
	 *         ��ָ��ͳ�ƺ���Ϊmavgʱ,�ֶ�ͳ�ƺ�����ΪMSUM,MAVG(��ʵΪ����)
	 */
	public java.util.Hashtable getAggrDatas(IStoreCell[] objMeasures,
			Map measFuncType, String strCond) throws java.sql.SQLException {
		// ִ��SQL���
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		java.util.Hashtable hashValue = new java.util.Hashtable();
		try {
			con = getConnection();
			// System.out.println("����ͳ�ƺ���ֵ��SQL��" + strCond);
			stmt = con.createStatement();
			rs = stmt.executeQuery(strCond);
			while (rs.next()) {
				// NEW_ADD
				Hashtable hashTemp = null;
				for (int i = 0; i < objMeasures.length; i++) {
					if (objMeasures[i].getType() == MeasureVO.TYPE_NUMBER 
							|| objMeasures[i].getType() == MeasureVO.TYPE_BIGDECIMAL) {

						Hashtable hashStatTemp = (Hashtable) measFuncType
								.get(objMeasures[i].getCode());
						if (hashStatTemp != null) {
							hashTemp = (Hashtable) hashValue.get(objMeasures[i]
									.getCode());
							if (hashTemp == null) {
								hashTemp = new Hashtable();
								hashValue.put(objMeasures[i].getCode(),
										hashTemp);
							}

							Iterator iter = hashStatTemp.keySet().iterator();
							while (iter.hasNext()) {

								String strStatType = (String) iter.next();
								String strColName = MeasFuncBsUtil.COLALIASPRFIX
										+ strStatType + "_" + (i + 1);

								if (strStatType.equals(MeasFuncDriver.MAVG)){
									hashTemp.put(strStatType, Integer.valueOf(rs
											.getInt(strColName)));
								}
								else{
									String value = rs.getString(strColName);
									if(value == null || value.length() == 0){
										if(strStatType.equals(MeasFuncDriver.MMAX) || 
												strStatType.equals(MeasFuncDriver.MMIN))
										continue;
									}
									hashTemp.put(strStatType, new Double(rs
											.getDouble(strColName)));
								}
							}
						}
					}

				}
			}

		} finally {
//			try
//			{
//				if (rs != null)
//					rs.close();
//				if (stmt != null)
//					stmt.close();
//				if (con != null)
//					con.close();
//			} catch (Exception e)
//			{
//
//			}
			
			//wangqi 20131225 �޸�sonar
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
					
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					
				}
				
			if (con != null)
				try {
					con.close();
				} catch (Exception e) {
					
				}
		}

		return hashValue;
	}

	/**
	 * �������ָ��ͳ�ƺ�������
	 * 
	 * @param objMeasure
	 *            �����е�ָ��
	 * @param objKeys
	 *            ָ���Ӧ�Ĺؼ��ּ���
	 * @param strBatchCondSql
	 *            ����������ݵ�sql���
	 * @param strExprRefKeyPks
	 *            �����е��������ʽ�����õĶ�̬����ؼ���pk����
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Hashtable batchGetAggrDatas(IStoreCell objMeasure,
			nc.vo.iufo.keydef.KeyVO[] objKeys, String strBatchCondSql,
			String[] strExprRefKeyPks) throws java.sql.SQLException {

		// ִ��SQL���
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;

		// ��¼����ֵ(key=hashtable ���ö�̬����ؼ���ֵ����; value=hashtableָ��ؼ���ֵ�����Ӧָ��ֵ����)
		Hashtable hashReturn = null;
		// Hashtable hashRefKeyValue=null;
		StringBuffer strBufKey = null;
		Hashtable hashTemp = null;
		List listRefKey = Arrays.asList(strExprRefKeyPks);

		try {
			con = getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery(strBatchCondSql);

			hashReturn = new Hashtable();

			while (rs.next()) {

				StringBuffer strMeasKeyValue = new StringBuffer();

				// ��¼���õĹؼ��ֵ�ֵ
				// hashRefKeyValue=new Hashtable();
				strBufKey = new StringBuffer();

				if (objKeys != null && objKeys.length > 0) {
					for (int i = 0; i < objKeys.length; i++) {
						String strKeyValue = rs
								.getString(MeasFuncBsUtil.COLALIASPRFIX
										+ objKeys[i].getPk_keyword());
						strMeasKeyValue.append(strKeyValue);
						strMeasKeyValue.append("\r\n");

						if (listRefKey != null
								&& listRefKey.contains(objKeys[i]
										.getPk_keyword())) {
							strBufKey.append(objKeys[i].getPk_keyword());
							strBufKey.append("=");
							strBufKey.append(strKeyValue);
							strBufKey.append(";");
							// hashRefKeyValue.put(objKeys[i].getKeywordPK(),strKeyValue);
						}
					}
				}

				if (hashReturn.get(strBufKey.toString()) == null) {
					hashTemp = new Hashtable();
					hashReturn.put(strBufKey.toString(), hashTemp);
				} else {
					hashTemp = (Hashtable) hashReturn.get(strBufKey.toString());
				}

				if (objMeasure.getType() == MeasureVO.TYPE_NUMBER) {
					hashTemp.put(strMeasKeyValue.toString(), new Double(rs
							.getDouble(MeasFuncBsUtil.COLALIASPRFIX + "1")));
				} else if(objMeasure.getType() == MeasureVO.TYPE_BIGDECIMAL) {
					BigDecimal bigValue = rs.getBigDecimal(MeasFuncBsUtil.COLALIASPRFIX + "1");
					if(bigValue == null) {
						hashTemp.put(strMeasKeyValue.toString(), new UFDouble(0.0));
					} else {
						hashTemp.put(strMeasKeyValue.toString(), new UFDouble(bigValue));
					}
				} else {
					hashTemp.put(strMeasKeyValue.toString(), rs
							.getString(MeasFuncBsUtil.COLALIASPRFIX + "1"));
				}

			}
		} finally {
//			try
//			{
//				if (rs != null)
//					rs.close();
//				if (stmt != null)
//					stmt.close();
//				if (con != null)
//					con.close();
//			} catch (Exception e)
//			{
//
//			}
			
			//wangqi 20131225 �޸�sonar
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
					
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					
				}
				
			if (con != null)
				try {
					con.close();
				} catch (Exception e) {
					
				}
		}
		if (hashReturn.size() == 0)
			return null;

		return hashReturn;
	}

	/**
	 * ��ȡָ��ͳ�ƺ��������޶����������ֵ���˷�����ȡ��ǰ����̬����ָ������ֵ �������ڣ�(2003-8-11 20:53:46)
	 * 
	 * @return java.util.Hashtable key=�ؼ���value+\r\n��ɵ��ַ�����value=Double|String
	 * @param objMeasure
	 *            MeasureVO
	 * @param objKeys
	 *            nc.vo.iufo.keydef.KeyVO[]
	 * @param strCond
	 *            java.lang.String
	 * @exception java.sql.SQLException
	 *                �쳣˵����
	 */
	public java.util.Hashtable getAggrDatas(IStoreCell objMeasure,
			nc.vo.iufo.keydef.KeyVO[] objKeys, String strCond)
			throws java.sql.SQLException {
		// ִ��SQL���
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		java.util.Hashtable hashValue = null;
		try {
			con = getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery(strCond);
			while (rs.next()) {
				if (hashValue == null) {
					hashValue = new java.util.Hashtable();
				}
				String strPrvKeyValue = "";
				if (objKeys != null) {
					for (int i = 0; i < objKeys.length; i++) {
						String strKeyValue = rs
								.getString(MeasFuncBsUtil.COLALIASPRFIX
										+ objKeys[i].getPk_keyword());
						strPrvKeyValue += strKeyValue + "\r\n";
					}
				}
				if (objMeasure.getType() == MeasureVO.TYPE_NUMBER) {
					hashValue.put(strPrvKeyValue, new Double(rs
							.getDouble(MeasFuncBsUtil.COLALIASPRFIX + "1")));
				} else if(objMeasure.getType() == MeasureVO.TYPE_BIGDECIMAL) {
					BigDecimal bigValue = rs.getBigDecimal(MeasFuncBsUtil.COLALIASPRFIX + "1");
					if(bigValue == null) {
						hashValue.put(strPrvKeyValue, new UFDouble(0.0));
					} else {
						hashValue.put(strPrvKeyValue, new UFDouble(bigValue));
					}
				} else {
					hashValue.put(strPrvKeyValue, rs
							.getString(MeasFuncBsUtil.COLALIASPRFIX + "1"));
				}

			}
		} finally {
//			try
//			{
//				if (rs != null)
//					rs.close();
//				if (stmt != null)
//					stmt.close();
//				if (con != null)
//					con.close();
//			} catch (Exception e)
//			{
//
//			}
			//wangqi 20131225 �޸�sonar
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
					
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					
				}
				
			if (con != null)
				try {
					con.close();
				} catch (Exception e) {
					
				}
		}

		return hashValue;
	}

	/**
	 * �����뵱ǰ����̬�����޹ص�ָ���mcount����ֵ�� �������ڣ�(2003-8-11 20:53:46)
	 * 
	 * @return java.util.Hashtable key=˽�йؼ���value+\r\n��ɵ��ַ�����value=Double|String
	 * @param objMeasure
	 *            MeasureVO
	 * @param objKeys
	 *            nc.vo.iufo.keydef.KeyVO[]
	 * @param strCond
	 *            java.lang.String
	 * @exception java.sql.SQLException
	 *                �쳣˵����
	 */
	public java.util.Hashtable getAggrValue(IStoreCell[] objMeasures,
			String strCond) throws java.sql.SQLException {
		// ִ��SQL���
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		java.util.Hashtable hashValue = new java.util.Hashtable();
		try {
			con = getConnection();
			// System.out.println("����ͳ�ƺ���ֵ��SQL��" + strCond);
			stmt = con.createStatement();
			rs = stmt.executeQuery(strCond);
			if (rs.next()) {
				for (int i = 0; i < objMeasures.length; i++) {
					if(objMeasures[i].getType() == IStoreCell.TYPE_NUMBER) {
						hashValue
						.put(objMeasures[i].getCode(), new Double(rs
								.getDouble(MeasFuncBsUtil.COLALIASPRFIX
										+ (i + 1))));
					} else {
						BigDecimal bigValue = rs.getBigDecimal(MeasFuncBsUtil.COLALIASPRFIX + (i + 1));
						if(bigValue == null) {
							hashValue.put(objMeasures[i].getCode(), new UFDouble(0.0));
						} else {
							hashValue.put(objMeasures[i].getCode(), new UFDouble(bigValue));
						}
					}
				}

			} else {
				for (int i = 0; i < objMeasures.length; i++) {
					hashValue.put(objMeasures[i].getCode(), new Double(0));
				}
			}
		} finally {
//			try
//			{
//				if (rs != null)
//					rs.close();
//				if (stmt != null)
//					stmt.close();
//				if (con != null)
//					con.close();
//			} catch (Exception e)
//			{
//
//			}
			
			//wangqi 20131225 �޸�sonar
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
					
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					
				}
				
			if (con != null)
				try {
					con.close();
				} catch (Exception e) {
					
				}
		}

		return hashValue;
	}

	/**
	 * ��ȡ�뵱ǰ����̬�����޹ص�ͳ�ƺ�����ص�ֵ�������ָ����ͬһ��ؼ����£��������ݱ�����ͬһ�����еġ� �������ڣ�(2003-8-11
	 * 20:53:46)
	 * 
	 * @return java.util.Hashtable key=MeasurePubDataVO,
	 *         �����˽�йؼ�����ôvalue��һ��Hashtable,����key=˽�йؼ���+\r\n��ɵ��ַ�����value��һ��Hashtable(key=prvkeyPK|measurePK,
	 *         value=its value)Measure.code value=Double|String
	 *         ����value��һ��Hashtable������key=MeasurePK,value=MeasureValue
	 * @param objMeasures
	 *            MeasureVO[]
	 * @param objKeys
	 *            nc.vo.iufo.keydef.KeyVO[]
	 * @param strCond
	 *            java.lang.String
	 * @exception java.sql.SQLException
	 *                �쳣˵����
	 */
	public java.util.Hashtable getAggrValues(IStoreCell[] objMeasures,
			nc.vo.iufo.keydef.KeyVO[] objKeys, String strCond)
			throws java.sql.SQLException {
		// ִ��SQL���
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		Hashtable hashValue = null;
		try {
			con = getConnection();
			// System.out.println("����ͳ�ƺ���ֵ��SQL��" + strCond);
			stmt = con.createStatement();
			rs = stmt.executeQuery(strCond);
			while (rs.next()) {
				int nPubPos = 0;
				if (hashValue == null) {
					hashValue = new java.util.Hashtable();
				}
				Hashtable hashMeasValue = new Hashtable();
				MeasurePubDataVO objMeasurePubDataVO = new MeasurePubDataVO();
				//��Ҫ���� ����ڼ䷽��PK��ҵ���߼� //added by liulp,2008-06-16
				objMeasurePubDataVO.setKType(rs.getString("ktype"));
//				objMeasurePubDataVO.setFormulaID(rs.getString("formula_id"));
				objMeasurePubDataVO.setVer(rs.getInt("ver"));
				if (objKeys != null) {
					KeyGroupCache keyGroupCache = UFOCacheManager.getSingleton().getKeyGroupCache();
					//tianchuan 2012.10.25  ����ȡָ�����Ĺؼ������PK
					String keyCombPk=null;
					if(objMeasures.length>0){
						if(objMeasures[0].getKeyCombPK()!=null){
							keyCombPk=objMeasures[0].getKeyCombPK();
						}
					}
					if(keyCombPk==null){	//���ȡ�������Ļ�����ȡ���ݿ������е�
						keyCombPk=rs.getString("ktype");
					}
					KeyGroupVO keyGroupVO = keyGroupCache.getByPK(keyCombPk);
					objMeasurePubDataVO.setKeyGroup(keyGroupVO);
					for (int i = 0; i < objKeys.length; i++) {
						nPubPos++;
						String strKeyVal=rs.getString(MeasFuncBsUtil.COLALIASPRFIX+ objKeys[i].getPk_keyword());
						objMeasurePubDataVO.setKeywordByIndex(nPubPos,strKeyVal);
					}
				}
				for (int i = 0; i < objMeasures.length; i++) {
					if (objMeasures[i].getType() == MeasureVO.TYPE_NUMBER) {
						hashMeasValue.put(objMeasures[i].getCode(), new Double(
								rs.getDouble(MeasFuncBsUtil.COLALIASPRFIX
										+ (i + 1))));
					} else if(objMeasures[i].getType() == MeasureVO.TYPE_BIGDECIMAL) {
						BigDecimal bigValue = rs.getBigDecimal(MeasFuncBsUtil.COLALIASPRFIX + (i + 1));
						if(bigValue == null) {
							hashMeasValue.put(objMeasures[i].getCode(), new UFDouble(0.0));
						} else {
							hashMeasValue.put(objMeasures[i].getCode(), new UFDouble(bigValue));
						}
					} else {
						hashMeasValue.put(objMeasures[i].getCode(), rs
								.getString(MeasFuncBsUtil.COLALIASPRFIX
										+ (i + 1)));
					}
				}
				Hashtable hashMeasVal = null;
				if (hashValue.containsKey(objMeasurePubDataVO)) {
					hashMeasVal = (java.util.Hashtable) hashValue.get(objMeasurePubDataVO);
					hashMeasVal.putAll(hashMeasValue);
				} else {
					hashValue.put(objMeasurePubDataVO, hashMeasValue);
				}

			}
		} finally {
//			try
//			{
//				if (rs != null)
//					rs.close();
//				if (stmt != null)
//					stmt.close();
//				if (con != null)
//					con.close();
//			} catch (Exception e)
//			{
//
//			}
			//wangqi 20131225 �޸�sonar
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
					
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					
				}
				
			if (con != null)
				try {
					con.close();
				} catch (Exception e) {
					
				}
		}

		return hashValue;
	}
	
	public java.util.Hashtable getAggrValues(final IStoreCell[] objMeasures,
			final nc.vo.iufo.keydef.KeyVO[] objKeys, String strCond, SQLParameter sqlParam)
			throws java.sql.SQLException, DAOException {
		// ִ��SQL���
		Hashtable reaultHash = null;
		try {
			BaseDAO baseDao = new BaseDAO();
			reaultHash = (Hashtable)baseDao.executeQuery(strCond, sqlParam,  new ResultSetProcessor(){
				private static final long serialVersionUID = -4253910663912535675L;
				@Override
				public Object handleResultSet(ResultSet rs) throws SQLException {
					Hashtable hashValue = new java.util.Hashtable();

					while (rs.next()) {
						int nPubPos = 0;
						Hashtable hashMeasValue = new Hashtable();
						MeasurePubDataVO objMeasurePubDataVO = new MeasurePubDataVO();
						//��Ҫ���� ����ڼ䷽��PK��ҵ���߼� //added by liulp,2008-06-16
						objMeasurePubDataVO.setKType(rs.getString("ktype"));
//						objMeasurePubDataVO.setFormulaID(rs.getString("formula_id"));
						objMeasurePubDataVO.setVer(rs.getInt("ver"));
						if (objKeys != null) {
							KeyGroupCache keyGroupCache = UFOCacheManager.getSingleton().getKeyGroupCache();
							
							//tianchuan 2012.10.25  ����ȡָ�����Ĺؼ������PK
							String keyCombPk=null;
							if(objMeasures.length>0){
								if(objMeasures[0].getKeyCombPK()!=null){
									keyCombPk=objMeasures[0].getKeyCombPK();
								}
							}
							if(keyCombPk==null){	//���ȡ�������Ļ�����ȡ���ݿ������е�
								keyCombPk=rs.getString("ktype");
							}
							KeyGroupVO keyGroupVO = keyGroupCache.getByPK(keyCombPk);
							
//							KeyGroupVO keyGroupVO = keyGroupCache.getByPK(rs.getString("ktype"));
							objMeasurePubDataVO.setKeyGroup(keyGroupVO);
							for (int i = 0; i < objKeys.length; i++) {
								nPubPos++;
								String strKeyVal=rs.getString(MeasFuncBsUtil.COLALIASPRFIX+ objKeys[i].getPk_keyword());
								objMeasurePubDataVO.setKeywordByIndex(nPubPos,strKeyVal);
							}
						}
						for (int i = 0; i < objMeasures.length; i++) {
							if (objMeasures[i].getType() == MeasureVO.TYPE_NUMBER) {
								hashMeasValue.put(objMeasures[i].getCode(), new Double(
										rs.getDouble(MeasFuncBsUtil.COLALIASPRFIX
												+ (i + 1))));
							} else if(objMeasures[i].getType() == MeasureVO.TYPE_BIGDECIMAL) {
								BigDecimal bigValue = rs.getBigDecimal(MeasFuncBsUtil.COLALIASPRFIX + (i + 1));
								if(bigValue == null) {
									hashMeasValue.put(objMeasures[i].getCode(), new UFDouble(0.0));
								} else {
									hashMeasValue.put(objMeasures[i].getCode(), new UFDouble(bigValue));
								}
							} else {
								hashMeasValue.put(objMeasures[i].getCode(), rs
										.getString(MeasFuncBsUtil.COLALIASPRFIX
												+ (i + 1)));
							}
						}
						Hashtable hashMeasVal = null;
						if (hashValue.containsKey(objMeasurePubDataVO)) {
							hashMeasVal = (java.util.Hashtable) hashValue.get(objMeasurePubDataVO);
							hashMeasVal.putAll(hashMeasValue);
						} else {
							hashValue.put(objMeasurePubDataVO, hashMeasValue);
						}

					}
					return hashValue;
				}
			});
			
			
		} finally {
		}

		return reaultHash;
	}

	/**
	 * �˴����뷽�������� �������ڣ�(2002-9-19 14:53:25)
	 * 
	 * @return java.lang.String
	 * @param code
	 *            java.lang.String
	 * @param name
	 *            java.lang.String
	 */
	public static final String getCodeNameSQL(String code, String name) {
		String sql = "";
		if (code != null && name != null) {
			sql = "SELECT content FROM iufo_codedata , iufo_code WHERE iufo_code.code_name = '"
					+ name
					+ "' AND iufo_codedata.data_id = '"
					+ code
					+ "' and  iufo_codedata.code_id = iufo_code.code_id";
		}
		return sql;
	}

//	/**
//	 * mselectָ�깫ʽ׷�� liuyy.
//	 * 
//	 */
//	public MeasureTraceVO transMeasureTraceValue(String where)
//			throws java.sql.SQLException {
//		// ִ��SQL���
//		Connection con = null;
//		Statement stmt = null;
//		ResultSet rs = null;
//		
//		MeasureTraceVO tracevo = new MeasureTraceVO();
//		
//		try {
//
//			String sqlStatement = "select iufo_measure_pubdata.keyword1,iufo_measure_pubdata.keyword2,iufo_measure_pubdata.keyword3,iufo_measure_pubdata.keyword4,iufo_measure_pubdata.keyword5,"
//					+ "iufo_measure_pubdata.keyword6,iufo_measure_pubdata.keyword7,iufo_measure_pubdata.keyword8,iufo_measure_pubdata.keyword9,iufo_measure_pubdata.keyword10,"
//					+ "iufo_measure_pubdata.alone_id from " + where;
//			AppDebug.debug(sqlStatement);
//			con = getConnection();
//			stmt = con.createStatement();
//			rs = stmt.executeQuery(sqlStatement);
//
//			if (rs.next()) {
//				String[] keyvals = new String[10];
//				for (int i = 0; i < 10; i++) {
//					keyvals[i] = rs.getString("keyword" + (i + 1));
//				}
//				tracevo.setKeyvalues(keyvals);
//				tracevo.setAloneID(rs.getString("alone_id"));
//			}
//
//		} finally {
//			if (rs != null) {
//				rs.close();
//			}
//			if (stmt != null) {
//				stmt.close();
//			}
//			if (con != null) {
//				con.close();
//			}
//		}
//		
//		return tracevo;
//		
//	}
	

	/**
	 * mselect,msum,mavg,msma��ָ�깫ʽ׷�� 
	 * 
	 * liuyy.
	 * 
	 */
	public MeasureTraceVO[] measureTraces(UfoCalcEnv objEnv, UfoExpr objCondExpr, IStoreCell[] mvos,
			String where,KeyGroupVO keyGroup,String strAccSchemePK) throws java.sql.SQLException {
		// ִ��SQL���
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {

			con = getConnection();
			stmt = con.createStatement();

			ArrayList<MeasureTraceVO> list = new ArrayList<MeasureTraceVO>();
			String strPubTable=keyGroup.getTableName();
			KeyVO[] keys=keyGroup.getKeys();
			for (int i = 0; i < mvos.length; i++) {
				IStoreCell mvo = mvos[i];
				if(mvo == null){
					continue;
				}
				String sqlStatement = "select "
						+ mvo.getDbtable() + "." + mvo.getDbcolumn()
						+ ",  ";
//				for (int j = 1; j <= 10; j++) {
//					sqlStatement += mvo.getDbtable() + ".key" + j + ", ";
//				}
				sqlStatement+=strPubTable+".ver,";
				
				for (int j=0;j<keys.length;j++){
					sqlStatement+=strPubTable+".keyword"+(j+1)+",";
				}
				
				sqlStatement+=strPubTable+".alone_id from " + where;

				AppDebug.debug(sqlStatement);

				rs = stmt.executeQuery(sqlStatement);
				
				while (rs.next()) {
					MeasureTraceVO tracevo = new MeasureTraceVO();
					tracevo.setMeasurePK(mvo.getCode());
					if(mvo instanceof MeasureVO) {
						tracevo.setMeasureTrace(true);
					} else {
						tracevo.setMeasureTrace(false);
					}
					if(mvo instanceof MeasureVO) {
						tracevo.setReportpk(((MeasureVO)mvo).getReportPK());
					} else {
						tracevo.setReportpk(mvo.getReportPK());
					}

					tracevo.setAloneID(rs.getString("alone_id"));
					Object measVal=null;
					if(mvo.getType()==MeasureVO.TYPE_NUMBER){
						measVal=new Double(rs.getDouble(mvo.getDbcolumn()));
					}else if(mvo.getType()==MeasureVO.TYPE_BIGDECIMAL){
						measVal=rs.getBigDecimal(mvo.getDbcolumn());
					}else {
						measVal=rs.getString(mvo.getDbcolumn());
					}
					String measStrVal=null;
					//�����ѧ������
					if(measVal instanceof Double){
						UFDouble ufDbVal=new UFDouble((Double)measVal,2);
						measStrVal=String.valueOf(ufDbVal);
					}else{
						measStrVal=String.valueOf(measVal);
					}
					
					tracevo.setValue(measStrVal);
					tracevo.setVer(rs.getInt("ver"));
					
					String[] strKeyValues = new String[keys.length];
//					for (int j = 0; j < 10; j++) {
//						strKeyValues[j] = rs.getString("keyword" + (j + 1));
//					}
					for (int j = 0; j < keys.length; j++) {
						strKeyValues[j] = rs.getString("keyword" + (j + 1));
					}
					
					IKeyDetailData[] keyvals = tranKeyVOs(mvo, strKeyValues,strAccSchemePK);
					
					tracevo.setKeyvalues(keyvals);
					
					// liuchun �޸ģ���װ׷�ٶ���̬��pk����̬���ؼ��ֵ�ֵ
					String strDynAreaPK = null;
//					Hashtable hashValues = null;
					if(mvo instanceof MeasureVO) {
						strDynAreaPK=objEnv.getDynPKByMeasurePK(mvo.getCode());
//						hashValues =strDynAreaPK==null?null:objEnv.getUfoDataChannel().getDatasByMeta(strDynAreaPK,mvo);
					} else {
						strDynAreaPK=objEnv.getDynPKByStoreCellPos(CellPosition.getInstance(mvo.getCode()));
//						hashValues =strDynAreaPK==null?null:objEnv.getUfoDataChannel().getDatasByMeta(strDynAreaPK,mvo);
					}
					tracevo.setDynAreaPK(strDynAreaPK);
//					if (hashValues != null) {
//
//						ReportDynCalcEnv env = new ReportDynCalcEnv(null, objEnv.getRepPK(), objEnv.getMeasureEnv(),
//								objEnv.getExEnv(),  true, null);
//						
//						com.ufsoft.iuforeport.reporttool.temp.KeyDataGroup objKeyValueInEnv = env.getKeyDatas();
//						objEnv.setExEnv(CommonExprCalcEnv.EX_ZKEYVALUES, objKeyValueInEnv);
//						
//						Enumeration enKey = hashValues.keys();
//						while (enKey.hasMoreElements()) {
//							com.ufsoft.iuforeport.reporttool.temp.KeyDataGroup objKeyValues =
//								(com.ufsoft.iuforeport.reporttool.temp.KeyDataGroup) enKey.nextElement();
//							if (objKeyValues != null) {
//								env.setDynAreaInfo(strDynAreaPK, objKeyValues);
//								
//								try {
//									if (objCondExpr.calcExpr(env)[0].doubleValue() == 1) {
//										
//										if(objEnv.isMeasureTrace() && objEnv.getExEnv(ICalcEnv.MEASURE_TRACE_FLAG) != null){
//											MeasureTraceVO[] arrtracevo = objEnv.getMeasureTraceVOs();
//											if(arrtracevo != null){
//												tracevo.setDynAreaPK(strDynAreaPK);
//												tracevo.setObjKeyValues(objKeyValues);			
//											}			
//										}
//										break;
//									}
//								} catch (UfoValueException e) {
//									AppDebug.debug(e);
//								} catch (CmdException e) {
//									AppDebug.debug(e);
//								}
//							}
//						}
//					}
					// �޸Ľ���
					
					list.add(tracevo);
				}

				rs.close();

			}

			return list.toArray(new MeasureTraceVO[0]);

		} finally {
//			try {
//				if (rs != null) {
//					rs.close();
//				}
//				if (stmt != null) {
//					stmt.close();
//				}
//				if (con != null) {
//					con.close();
//				}
//			} catch (Throwable e) {
//			}
			
			//wangqi 20131225 �޸�sonar
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
					
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					
				}
				
			if (con != null)
				try {
					con.close();
				} catch (Exception e) {
					
				}
		}
	}

	/**
	 * ����SQL������mselect,msum,mavg,msma��ָ�깫ʽ׷�� 
	 * 
	 */
	public MeasureTraceVO[] measureTracesWithSqlParam(final UfoCalcEnv objEnv, UfoExpr objCondExpr, IStoreCell[] mvos,
			String where,SQLParameter sqlParam,KeyGroupVO keyGroup,final String strAccSchemePK) throws java.sql.SQLException {
		// ִ��SQL���
		Connection con = null;
//		PreparedStatement stmt = null;
		try {
			ArrayList<MeasureTraceVO> list = new ArrayList<MeasureTraceVO>();
			String strPubTable=keyGroup.getTableName();
			final KeyVO[] keys=keyGroup.getKeys();
			for (int i = 0; i < mvos.length; i++) {
				final IStoreCell mvo = mvos[i];
				if(mvo == null){
					continue;
				}
				String sqlStatement = "select "
						+ mvo.getDbtable() + "." + mvo.getDbcolumn()
						+ ",  ";
				sqlStatement+=strPubTable+".ver,";
				
				for (int j=0;j<keys.length;j++){
					sqlStatement+=strPubTable+".keyword"+(j+1)+",";
				}
				
				sqlStatement+=strPubTable+".alone_id from " + where;

				AppDebug.debug(sqlStatement);
				
				con = getConnection();
//				stmt = con.prepareStatement(sqlStatement);
				BaseDAO baseDao = new BaseDAO();
				List<MeasureTraceVO> traceVOList=null;
				try {
					//TODO ѭ�����н������ݿ����  
					traceVOList = (List<MeasureTraceVO>)baseDao.executeQuery(sqlStatement, sqlParam,  new ResultSetProcessor(){
						private static final long serialVersionUID = -4253910663912535675L;
						@Override
						public Object handleResultSet(ResultSet rs) throws SQLException {
							List<MeasureTraceVO> traceList = new ArrayList<MeasureTraceVO>();
							while (rs.next()) {
								MeasureTraceVO tracevo = new MeasureTraceVO();
								tracevo.setMeasurePK(mvo.getCode());
								if(mvo instanceof MeasureVO) {
									tracevo.setMeasureTrace(true);
								} else {
									tracevo.setMeasureTrace(false);
								}
								if(mvo instanceof MeasureVO) {
									tracevo.setReportpk(((MeasureVO)mvo).getReportPK());
								} else {
									tracevo.setReportpk(mvo.getReportPK());
								}

								tracevo.setAloneID(rs.getString("alone_id"));
								Object measVal=null;
								if(mvo.getType()==MeasureVO.TYPE_NUMBER){
									measVal=new Double(rs.getDouble(mvo.getDbcolumn()));
								}else if(mvo.getType()==MeasureVO.TYPE_BIGDECIMAL){
									measVal=rs.getBigDecimal(mvo.getDbcolumn());
								}else {
									measVal=rs.getString(mvo.getDbcolumn());
								}
								String measStrVal=null;
								//�����ѧ������
								if(measVal instanceof Double){
									UFDouble ufDbVal=new UFDouble((Double)measVal,2);
									measStrVal=String.valueOf(ufDbVal);
								}else{
									measStrVal=String.valueOf(measVal);
								}
								
								tracevo.setValue(measStrVal);
								tracevo.setVer(rs.getInt("ver"));
								
								String[] strKeyValues = new String[keys.length];
								for (int j = 0; j < keys.length; j++) {
									strKeyValues[j] = rs.getString("keyword" + (j + 1));
								}
								
								IKeyDetailData[] keyvals = tranKeyVOs(mvo, strKeyValues,strAccSchemePK);
								
								tracevo.setKeyvalues(keyvals);
								
								// liuchun �޸ģ���װ׷�ٶ���̬��pk����̬���ؼ��ֵ�ֵ
								String strDynAreaPK = null;
								if(mvo instanceof MeasureVO) {
									strDynAreaPK=objEnv.getDynPKByMeasurePK(mvo.getCode());
								} else {
									strDynAreaPK=objEnv.getDynPKByStoreCellPos(CellPosition.getInstance(mvo.getCode()));
								}
								tracevo.setDynAreaPK(strDynAreaPK);
								// �޸Ľ���
								
								traceList.add(tracevo);
							}
							return traceList;
						}
					});
					if(traceVOList!=null){
						list.addAll(traceVOList);
					}
				} catch (DAOException e) {
					AppDebug.debug(e);
				}
			}
			if (con != null) {
				con.close();
			}
			return list.toArray(new MeasureTraceVO[0]);

		} finally {
//			try {
////				if (stmt != null) {
////					stmt.close();
////				}
//				if (con != null) {
//					con.close();
//				}
//			} catch (Throwable e) {
//			}
			
			//wangqi 20131225 �޸�sonar
			if (con != null)
				try {
					con.close();
				} catch (Exception e) {
					
				}
		}
	}
	
	private IKeyDetailData[] tranKeyVOs(IStoreCell mvo, String[] keyvals,String strAccSchemePK) {
		ArrayList<IKeyDetailData> listKeyVal = new ArrayList<IKeyDetailData>();
		KeyGroupVO kg = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(mvo.getKeyCombPK());
		KeyVO[] kvos = kg.getKeys();
//		boolean doPrivate = false;
		int index = 0;
		for (int j = 0; j < kvos.length; j++) {
			KeyVO kvo = kvos[j];
//			if(!doPrivate && kvo.isPrivate()){
//				index = 10;
//				doPrivate = true;
//			}
			String val = keyvals[index];
			listKeyVal.add(KeyDetailDataUtil.getKeyDetailData(kvo, val, strAccSchemePK));
			index++;
		}

		return listKeyVal.toArray(new IKeyDetailData[listKeyVal.size()]);
	}
	
	

	/**
	 * ����select������ֵ�����ﴦ�������ָ��ȡ�������ؼ��������ͬ��ȡ��������ͬ���������ݱ����ڲ�ͬ�ı��� �������ڣ�(2003-8-11
	 * 20:53:46)
	 * 
	 * @return java.util.Hashtable key=Measure.code value=Double|String
	 * @param hashMeasSQLs
	 *            java.util.Hashtable[][0]�Ǳ��Ӧ��from֮ǰ���ֶΣ�key=dbtable,value=columns��[1]��ʾ��һ�����е�ָ�꣨key=dbtable,
	 *            value=ArrayList(MeasureVO)��[0]��[1]��keyһ��
	 * @param strTables
	 *            java.lang.String
	 * @param strCond
	 *            java.lang.String
	 * @param hashPrvKeyTbl
	 *            java.util.Hashtable key�������е�˽�йؼ��ֵ�tbl.col��value��˽�йؼ��ֵ��ֶ�����
	 * @exception java.sql.SQLException
	 *                �쳣˵����
	 */
	public java.util.Hashtable getSelectValues(IStoreCell[] objMeasures,
			String sqlStatement) throws java.sql.SQLException {
		// ִ��SQL���
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			if (sqlStatement == null) {
				return new java.util.Hashtable();
			}
			con = getConnection();
			java.util.Hashtable hashValue = new java.util.Hashtable();
			stmt = con.createStatement();
			rs = stmt.executeQuery(sqlStatement);

			boolean bDataExisted = false;
			if (rs.next()) {
				for (int i = 0; i < objMeasures.length; i++) {
					if (objMeasures[i].getType() == MeasureVO.TYPE_NUMBER) {
						hashValue.put(objMeasures[i].getCode(), new Double(rs
								.getDouble(MeasFuncBsUtil.COLALIASPRFIX
										+ (i + 1))));
					} else if (objMeasures[i].getType() == MeasureVO.TYPE_BIGDECIMAL){
						BigDecimal bigValue = rs.getBigDecimal(MeasFuncBsUtil.COLALIASPRFIX + (i + 1));
						if(bigValue == null) {
							hashValue.put(objMeasures[i].getCode(), new UFDouble(0.0));
						} else {
							hashValue.put(objMeasures[i].getCode(), new UFDouble(bigValue));
						}
					} else {

						String strValue = rs
								.getString(MeasFuncBsUtil.COLALIASPRFIX
										+ (i + 1));
						if (strValue == null)
							strValue = "";
						hashValue.put(objMeasures[i].getCode(), strValue);
					}
				}
				bDataExisted = true;
			}
			// �����������ݲ������������ʱ������0��մ�
			if (bDataExisted == false) {
				for (int i = 0; i < objMeasures.length; i++) {
					if (objMeasures[i].getType() == MeasureVO.TYPE_NUMBER) {
						hashValue.put(objMeasures[i].getCode(), new Double(0));
					} else if(objMeasures[i].getType() == MeasureVO.TYPE_BIGDECIMAL) {
						hashValue.put(objMeasures[i].getCode(), new UFDouble(0));
					}else {
						hashValue.put(objMeasures[i].getCode(), "");
					}
				}
			}

			return hashValue;
		} finally {
//			try {
//				if (rs != null) {
//					rs.close();
//				}
//				if (stmt != null) {
//					stmt.close();
//				}
//				if (con != null) {
//					con.close();
//				}
//			} catch (Throwable e) {
//			}
			
			//wangqi 20131225 �޸�sonar
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
					
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					
				}
				
			if (con != null)
				try {
					con.close();
				} catch (Exception e) {
					
				}
		}
	}
	//2007-06-04, by ll
	public java.util.Hashtable getSelectValuesForTask(
		    IStoreCell[] objMeasures,
		    String sqlStatement, Hashtable measData)
		    throws java.sql.SQLException {
		
			String fromSql = sqlStatement.substring(sqlStatement.indexOf(" from "));
			String getAllSql = " select * " + fromSql;
			
			if(measData == null)
				measData = new Hashtable();
			if(!measData.containsKey(fromSql)){//ִ��sql����ȡȫ��ָ������
				Hashtable allValues = getAllValues(getAllSql);
				measData.put(fromSql, allValues);
			}
			Hashtable values = (Hashtable)measData.get(fromSql);//��ͬ����������ָ��ֵ
			
	        Hashtable hashValue = new Hashtable();
	        boolean bDataExisted = false;
	        if (values.size()>0) {
	            for (int i = 0; i < objMeasures.length; i++) {
	            	String str = (String)values.get(objMeasures[i].getDbcolumn());
	                if (objMeasures[i].getType() == MeasureVO.TYPE_NUMBER) {
	                	if(str != null)
	                		hashValue.put(objMeasures[i].getCode(),new Double(str));
	                	else
	                		hashValue.put(objMeasures[i].getCode(), new Double(0));
	                } else if(objMeasures[i].getType() == MeasureVO.TYPE_BIGDECIMAL) {
	                	if(str != null)
	                		hashValue.put(objMeasures[i].getCode(),new UFDouble(str));
	                	else
	                		hashValue.put(objMeasures[i].getCode(), new UFDouble(0));
	                } else if(str != null){// @edit by wuyongc at 2013-9-3,����2:50:13 str����ΪNull ��Hashtable ��value����Ϊnull
	                    hashValue.put(objMeasures[i].getCode(),str );
	                }
	            }
	            bDataExisted = true;
	        }
	        //�����������ݲ������������ʱ������0��մ�
	        if (bDataExisted == false) {
	            for (int i = 0; i < objMeasures.length; i++) {
	                if (objMeasures[i].getType() == MeasureVO.TYPE_NUMBER) {
	                    hashValue.put(objMeasures[i].getCode(), new Double(0));
	                } else if(objMeasures[i].getType() == MeasureVO.TYPE_BIGDECIMAL) {
	                	hashValue.put(objMeasures[i].getCode(), new UFDouble(0));
	                }else {
	                    hashValue.put(objMeasures[i].getCode(), "");
	                }
	            }
	        }
	        return hashValue;
	}
	private java.util.Hashtable getAllValues(
		    String sqlStatement)
		    throws java.sql.SQLException {
		    //ִ��SQL���
		    Connection con = null;
		    Statement stmt = null;
		    ResultSet rs = null;
		    try {
		        if (sqlStatement == null) {
		            return new java.util.Hashtable();
		        }
		        con = getConnection();
		        java.util.Hashtable hashValue = new java.util.Hashtable();
		 //       System.out.println("����mselect����ֵ��SQL��" + sqlStatement);
		        stmt = con.createStatement();
		        rs = stmt.executeQuery(sqlStatement);
		        ResultSetMetaData metaData = rs.getMetaData();
	        	if(rs.next()){
		        	for (int i = 0; i < metaData.getColumnCount(); i++) {
		        		if(metaData.getColumnName(i+1).startsWith("M")){
		        			String value = rs.getString(i+1);
		        			if(value != null)
		        				hashValue.put(metaData.getColumnName(i+1).toLowerCase(), value);
		        		}
				}
		    }
		        return hashValue;
		    } finally {
//				try {
//					if (rs != null) {
//						rs.close();
//					}
//					if (stmt != null) {
//						stmt.close();
//					}
//					if (con != null) {
//						con.close();
//					}
//				} catch (Throwable e) {
//				}
				
				//wangqi 20131225 �޸�sonar
				if (rs != null)
					try {
						rs.close();
					} catch (Exception e) {
						
					}
				if (stmt != null)
					try {
						stmt.close();
					} catch (Exception e) {
						
					}
					
				if (con != null)
					try {
						con.close();
					} catch (Exception e) {
						
					}
		    }
		}
	/**
	 * ִ��SQL��䣬����ֵ�� �������ڣ�(2001-11-26 14:23:42)
	 * 
	 * @return Object ��ѯ���
	 * @param sqlStatement
	 *            java.lang.String select ���
	 * @param bIsNum
	 *            boolean �Ƿ�Ϊ��ֵ
	 * @param bNullAllowed
	 *            boolean = false ��ʾ�����ѯ���Ϊ�գ�������쳣
	 */
	public Object getValue(String sqlStatement, boolean bIsNum,
			boolean bNullAllowed) throws SQLException {
		// ִ��SQL���
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery(sqlStatement);

			Object objVal = null;
			boolean bDataExisted = false;
			if (rs.next()) {
				if (bIsNum) {
					objVal = new Double(rs.getDouble(1));
				} else {
					objVal = rs.getString(1);
				}
				bDataExisted = true;
			}

			// �����������ݲ������������ʱ������0��մ�
			if (!bNullAllowed && bDataExisted == false) {
				if (bIsNum) {
					objVal = new Double(0);
				} else {
					objVal = "";
				}
				// throw new SQLException();
			}

			/*
			 * if( objVal == null ){ if( bIsNum){ objVal = new Double(0); }else{
			 * objVal = ""; } }
			 */
			return objVal;
		} finally {
//			try {
//				if (rs != null) {
//					rs.close();
//				}
//				if (stmt != null) {
//					stmt.close();
//				}
//				if (con != null) {
//					con.close();
//				}
//			} catch (Throwable e) {
//			}
			
			//wangqi 20131225 �޸�sonar
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
					
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					
				}
				
			if (con != null)
				try {
					con.close();
				} catch (Exception e) {
					
				}
		}
	}

	/**
	 * �������ָ��ȡ������ֵ
	 * 
	 * @param objMeasures
	 * @param sqlStatement
	 * @param strExprRefKeyPks
	 *            ָ�꺯�����������õĶ�̬����ؼ���PK����
	 * @return
	 * @throws java.sql.SQLException
	 */
	public java.util.Hashtable batchDynSelectValues(IStoreCell[] objMeasures,
			String sqlStatement, nc.vo.iufo.keydef.KeyVO[] objKeys)
			throws java.sql.SQLException {

		if (sqlStatement == null || objKeys == null || objKeys.length == 0) {
			return null;
		}

		// ִ��SQL���
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery(sqlStatement);

			Object objVal = null;
			StringBuffer strBufKeyValues = null;
			Hashtable hashTemp = null;
			java.util.Hashtable hashValue = new java.util.Hashtable();

			while (rs.next()) {
				strBufKeyValues = new StringBuffer();
				int iLen = objKeys.length;
				for (int i = 0; i < iLen; i++) {
					strBufKeyValues.append(rs
							.getString(MeasFuncBsUtil.COLALIASPRFIX
									+ objKeys[i].getPk_keyword()));
					strBufKeyValues.append("\r\n");
				}
				hashTemp = (Hashtable) hashValue
						.get(strBufKeyValues.toString());
				if (hashTemp == null) {
					hashTemp = new Hashtable();
					hashValue.put(strBufKeyValues.toString(), hashTemp);
				}

				for (int i = 0; i < objMeasures.length; i++) {
					if (objMeasures[i] == null)
						continue;

					if (objMeasures[i].getType() == MeasureVO.TYPE_NUMBER) {
						objVal = new Double(rs
								.getDouble(MeasFuncBsUtil.COLALIASPRFIX
										+ (i + 1)));
						if (objVal == null)
							objVal = new Double(0);

					} else if(objMeasures[i].getType() == MeasureVO.TYPE_BIGDECIMAL){
						BigDecimal bigValue = rs.getBigDecimal(MeasFuncBsUtil.COLALIASPRFIX + (i + 1));
						if(bigValue == null) {
							objVal = new UFDouble(0.0);
						} else {
							objVal = new UFDouble(bigValue);
						}
						if (objVal == null)
							objVal = new UFDouble(0);
					} else {
						objVal = rs.getString(MeasFuncBsUtil.COLALIASPRFIX
								+ (i + 1));
						if (objVal == null)
							objVal = "";
					}

					hashTemp.put(objMeasures[i].getCode(), objVal);
				}

			}
			// ����ֵ key=�ؼ���ֵ�ִ�, value=(hashtable key=ָ��code, value=ָ��ȡ������ֵ)
			return hashValue;

		} catch (SQLException e) {
			AppDebug.debug(e);//@devTools e.printStackTrace(System.out);
			throw e;
		} finally {
//			try {
//				if (rs != null) {
//					rs.close();
//				}
//				if (stmt != null) {
//					stmt.close();
//				}
//				if (con != null) {
//					con.close();
//				}
//			} catch (Throwable e) {
//			}
			
			//wangqi 20131225 �޸�sonar
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
					
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					
				}
				
			if (con != null)
				try {
					con.close();
				} catch (Exception e) {
					
				}
		}
	}
	
	/**
	 * �������ָ��ȡ������ֵfor vlookup
	 * 
	 * @param objMeasures
	 * @param sqlVO
	 * @param objKeys
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Hashtable<String, Hashtable<String, Object>> getLookUpDynMeasValues(MeasureVO[] objMeasures, String sqlStatement, KeyVO[] objKeys)
			throws java.sql.SQLException {
		if (sqlStatement == null || objKeys == null || objKeys.length == 0) {
			return null;
		}

		// ִ��SQL���
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery(sqlStatement);
			
			// ����ֵ��key;keywords value string;value:hashtable<keypk/measpk,val>
			Hashtable<String, Hashtable<String, Object>> hashValue = new Hashtable<String, Hashtable<String, Object>>();

			StringBuffer strBufKeyValues = new StringBuffer();
			Hashtable<String, Object> hashTemp = null;
			Object objVal = null;
			while (rs.next()) {
				hashTemp = new Hashtable<String, Object>();
				// keywords value
				for (int i = 0; i < objKeys.length; i++) {
					objVal = rs.getString(MeasFuncBsUtil.COLALIASPRFIX + objKeys[i].getPk_keyword());

					// ƴװ�ؼ����ַ���
					strBufKeyValues.append(objVal).append("\r\n");

					// ����ǵ�λ���߶Է���λ���룬��pkת��Ϊcode
//					if (KeyVO.isUnitKeyVO(objKeys[i]) || KeyVO.isDicUnitKeyVO(objKeys[i])) {
//						IKeyDetailData orgDetailData=KeyDetailDataUtil.getOrgKeyDetailData((String) objVal);
//						if (orgDetailData != null) {
//							objVal = orgDetailData.getCode();
//						}
//					}
					if(objKeys[i].getRef_pk()!=null){
						IGeneralAccessor accessor = (IGeneralAccessor) GeneralAccessorFactory.getAccessor(objKeys[i].getRef_pk());
						if(accessor!=null){
							IBDData bdData=accessor.getDocByPk((String) objVal);
							if(bdData!=null && bdData.getCode()!=null){
								objVal=bdData.getCode();
							}
						}
					}
					
					
					hashTemp.put(objKeys[i].getPk_keyword(), objVal);
				}

				if (hashValue.get(strBufKeyValues.toString()) != null) {
					hashTemp.putAll(hashValue.get(strBufKeyValues.toString()));
				}
				hashValue.put(strBufKeyValues.toString(), hashTemp);

				// measures value
				for (int i = 0; i < objMeasures.length; i++) {
					if (objMeasures[i].getType() == MeasureVO.TYPE_NUMBER) {
						objVal = new Double(rs.getDouble(MeasFuncBsUtil.COLALIASPRFIX + (i + 1)));
						if (objVal == null) {
							objVal = new Double(0);
						}
					} else {
						objVal = rs.getString(MeasFuncBsUtil.COLALIASPRFIX + (i + 1));
						if (objVal == null) {
							objVal = "";
						}
					}
					hashTemp.put(objMeasures[i].getCode(), objVal);
				}
			}
			return hashValue;
		} catch (SQLException e) {
			AppDebug.debug(e);
			throw e;
		} finally {
//			try {
//				if (rs != null) {
//					rs.close();
//				}
//				if (stmt != null) {
//					stmt.close();
//				}
//				if (con != null) {
//					con.close();
//				}
//			} catch (Throwable e) {
//			}
			
			//wangqi 20131225 �޸�sonar
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
					
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					
				}
				
			if (con != null)
				try {
					con.close();
				} catch (Exception e) {
					
				}
		}
	}
	
}
