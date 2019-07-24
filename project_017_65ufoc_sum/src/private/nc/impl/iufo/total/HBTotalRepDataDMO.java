/**
 *
 */
package nc.impl.iufo.total;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import nc.bs.iufo.DataManageObjectIufo;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.vo.iufo.data.MeasureDataUtil;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iufo.pub.date.UFODate;
import nc.vo.iufo.storecell.IStoreCell;
import nc.vo.iufo.total.TotalCellKeyValue;

/**
 * @author jiaah
 * @created at 2011-7-27,����06:48:45
 *
 */
public class HBTotalRepDataDMO  extends DataManageObjectIufo {

	public HBTotalRepDataDMO() throws javax.naming.NamingException, nc.bs.pub.SystemException {
	}

	/**
	 * ������Դʱ���Թ��йؼ��ֶ�̬����Դ
	 * @param measures��Ҫ��Դ��ָ��
	 * @param strCondSQL������������SQL���
	 * @param keys��Ҫ���ҵĹؼ���ֵ�Ĺؼ�������
	 * @param hashKeyPos��Ҫ���ҵĹؼ�����ؼ�������еĹؼ��ֵ�λ�õĶ�Ӧ��ϵ
	 * @param mainPubData������MeasurePubDataVO
	 * @param subPubData,�ӱ�MeasurePubDataVO
	 * @return
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public TotalCellKeyValue[] loadTotalSourValsByPub(IStoreCell[] measures, String strCondSQL, KeyVO[] keys,
			Hashtable hashKeyPos, MeasurePubDataVO mainPubData, MeasurePubDataVO subPubData) throws SQLException {
		Connection con =null;
		PreparedStatement stmt = null;
		ResultSet set=null;
		try {
			con=getConnection();

			Vector<String> vMeasID = getMeasIDFromMeasure(measures);
			boolean[] bTimeType = getKeywordTimeType(mainPubData, subPubData);

			KeyGroupVO measKeyGroup=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(measures[0].getKeyCombPK());
			KeyVO[] measKeys = measKeyGroup.getKeys();
			Vector<KeyVO> vMeasKey = new Vector<KeyVO>(Arrays.asList(measKeys));

			String strTable = measures[0].getDbtable();
			// ƴ��select���
			StringBuffer selBufSQL = new StringBuffer();

			selBufSQL.append("select t1.alone_id,t3.alone_id,");
			for (int i = 0; i < keys.length; i++) {
				if (vMeasKey.contains(keys[i])) {
					int iPos = ((Integer) hashKeyPos.get(Integer.valueOf(i))).intValue() + 1;
					selBufSQL.append("t2.keyword" + iPos + ",");
				}
				else if (measKeyGroup.isTTimeTypeAcc()){
					for (int j=0;j<vMeasKey.size();j++){
						if (vMeasKey.get(j).isAccPeriodKey()){
							selBufSQL.append("t3.keyword"+(j+1)+",");
							break;
						}
					}
				}
				//�ӱ�ؼ���Ϊ���ߴ˷�֧
				else{
					for (int j=0;j<vMeasKey.size();j++){
						if (vMeasKey.get(j).isTimeKeyVO()){
							selBufSQL.append("t3.keyword"+(j+1)+",");
							break;
						}
					}
				}
//					selBufSQL.append("t3.keyword2,");
			}

			for (int i = 0; i < measures.length; i++) {
				IStoreCell measure = measures[i];
				selBufSQL.append("t1." + measure.getDbcolumn());

				if (i < measures.length - 1)
					selBufSQL.append(",");
			}

			selBufSQL.append(getTotalJoinCond(strTable, strCondSQL, mainPubData, subPubData, null, bTimeType[0],
					bTimeType[1], bTimeType[2],true,true,false));

			stmt = con.prepareStatement(selBufSQL.toString());
			set = stmt.executeQuery();

			Vector<TotalCellKeyValue> vRetVal = new Vector<TotalCellKeyValue>();
			while (set.next()) {
				TotalCellKeyValue oneValue = new TotalCellKeyValue();
				oneValue.setDynAloneID(set.getString(1));
				oneValue.setMainAloneID(set.getString(2));
//				oneValue.setUnitPK(set.getString(3));

				String[] strKeyValues = new String[keys.length];
				Object[] strMeasValues = new Object[measures.length];
				for (int i = 0; i < keys.length + measures.length; i++) {
					if (i < keys.length)
//						strKeyValues[i] = set.getString(i + 4);
						strKeyValues[i] = set.getString(i + 3);
					else if (measures[i - keys.length].getType() == MeasureVO.TYPE_NUMBER)
//						strMeasValues[i - keys.length] = "" + set.getDouble(i + 4);
						strMeasValues[i - keys.length] = set.getDouble(i + 3);
					else if(measures[i - keys.length].getType() == MeasureVO.TYPE_BIGDECIMAL)
						strMeasValues[i - keys.length] = set.getBigDecimal(i + 3);
					else
//						strMeasValues[i - keys.length] = set.getString(i + 4);
						strMeasValues[i - keys.length] = set.getString(i + 3);
				}

				oneValue.setKeyVals(strKeyValues);
				oneValue.setMeasID(new Vector<String>(vMeasID));
				oneValue.setVals(new Vector<Object>(Arrays.asList(strMeasValues)));

				vRetVal.add(oneValue);
			}
			return vRetVal.toArray(new TotalCellKeyValue[0]);
		} catch (SQLException e) {
			throw e;
		} finally {
			if (set!=null){
				try{
					set.close();
				}catch(Exception e){
					
				}
			}
			if (stmt != null){
				try{
					stmt.close();
				}catch(Exception e){
					
				}
				
			}
			if (con != null){
				try{
					con.close();
				}catch(Exception e){
					
				}
			}
		}
	}

	/**
	 * ���ڲ����ڲμӻ��ܵı������е�ָ�꣬���л����¼�
	 * @param strTable��ָ�����ݱ�
	 * @param strCond������ɸѡ����SQL���
	 * @param vMeasure���μӻ��ܵ�ָ��
	 * @param mainPubData�������MeasurePubDataVO
	 * @param vSubPubData,��Ҫ���ɵ��ӱ��MeasurePubDataVO����
	 * @throws SQLException
	 */
	public void createTotalNoDynDatasWithHZSub(String strTable, String strCond, List<IStoreCell> vMeasure,
			MeasurePubDataVO mainPubData, List<MeasurePubDataVO> vSubPubData) throws SQLException {
		// ��MeasurePubDataVO�������Ҫ��insert����update������ɵ�
		Vector<Vector<MeasurePubDataVO>> vRetPubData = splitPubDataByExistRecord(strTable, vSubPubData);

		// ����update�������
		if (vRetPubData.get(0) != null && vRetPubData.get(0).size() > 0)
			createTotalNoDynDatasByUpdate(strTable, strCond, vMeasure, mainPubData, vRetPubData.get(0));

		// ����insert�������
		if (vRetPubData.get(1) != null && vRetPubData.get(1).size() > 0)
			createTotalNoDynDatasByInsert(strTable, strCond, vMeasure, mainPubData, vRetPubData.get(1));
	}
	
	/**
	 * ʹ�ò����¼�ķ��������ɹ��йؼ��ֵĶ�̬���Ļ��ܼ�¼
	 * @param strTable
	 * @param strCond
	 * @param vMeasure
	 * @param mainPubData
	 * @param vSubPubData
	 * @throws SQLException
	 */
	public void createTotalNoDynDatasByInsert(String strTable, String strCond, List<IStoreCell> vMeasure,
			MeasurePubDataVO mainPubData, List<MeasurePubDataVO> vSubPubData) throws SQLException {
		//�����¼�¼
		innerCreateTotalNoDynDatasByInsert(strTable, strCond, vMeasure, mainPubData, vSubPubData);

		//������ַ���ָ�꣬����ֵ��δ���ɣ���update������������
		boolean bNumberMeas=(vMeasure.get(0).getType()==IStoreCell.TYPE_NUMBER || vMeasure.get(0).getType()==IStoreCell.TYPE_BIGDECIMAL);
		if (!bNumberMeas){
			createTotalNoDynDatasByUpdate(strTable, strCond, vMeasure, mainPubData, vSubPubData);
		}
	}

	/**
	 * ���ݿ����Ѿ����ڸ��м�¼����Ҫ��update���������ֵ
	 * @param strTable��ָ�����ݱ�
	 * @param strCond������ɸѡ����SQL���
	 * @param vMeasure���μӻ��ܵ�ָ��
	 * @param mainPubData�������MeasurePubDataVO
	 * @param vSubPubData����Ҫ���ɵ��ӱ��MeasurePubDataVO����
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public void createTotalNoDynDatasByUpdate(String strTable, String strCond, List<IStoreCell> vMeasure,
			MeasurePubDataVO mainPubData, List<MeasurePubDataVO> vSubPubData) throws SQLException {
		Connection con =null;
		Statement selStmt = null;
		PreparedStatement updateStmt = null;
		ResultSet set = null;
		try {
			con=getConnection();
 
			boolean bNumberMeas=(vMeasure.get(0).getType()==MeasureVO.TYPE_NUMBER || vMeasure.get(0).getType()==MeasureVO.TYPE_BIGDECIMAL);

			// ������select�����ҳ���¼������update���������
			MeasurePubDataVO subPubData = vSubPubData.get(0);
			boolean[] bTimeType = getKeywordTimeType(mainPubData, subPubData);
			boolean bDayTime = bTimeType[2];
			boolean bSubTime = bTimeType[0];

			// ����update���SQL��䣬sum�е�SQL���
			StringBuffer selSumMeasSQL = new StringBuffer();
			StringBuffer updateBufSQL = new StringBuffer();
			updateBufSQL.append("update ");
			updateBufSQL.append(strTable);
			updateBufSQL.append(" set ");

			for (int i = 0; i < vMeasure.size(); i++) {
				IStoreCell measure =vMeasure.get(i);

				updateBufSQL.append(measure.getDbcolumn() + "=?");

				if (bNumberMeas)
					selSumMeasSQL.append("sum(" + measure.getDbcolumn() + ")");
				else
					selSumMeasSQL.append(measure.getDbcolumn());

				if (i < vMeasure.size() - 1) {
					updateBufSQL.append(",");
					selSumMeasSQL.append(",");
				}
			}
			//xulm line_no �ֶ��Ѿ����±��б�ȥ��
//			updateBufSQL.append(" where alone_id=? and line_no=? ");
			updateBufSQL.append(" where alone_id=?  ");

			// ��¼���ҳ��Ľ��
			Vector<Vector<Object>> vData = new Vector<Vector<Object>>();
			Vector<List<MeasurePubDataVO>> vSplitSubPubData = splitObject(vSubPubData, (bSubTime == true ? 1 : 100));
			for (int i = 0; i < vSplitSubPubData.size(); i++) {
				vSubPubData =vSplitSubPubData.get(i);

				subPubData = vSubPubData.get(0);
				String strAloneID = subPubData.getAloneID();

				// ƴselect���
				StringBuffer selBufSQL = new StringBuffer();
				selBufSQL.append("select ");

				if (subPubData.getKType().equals(mainPubData.getKType()) || bSubTime == true)
					selBufSQL.append("'" + strAloneID + "',");
				else
					selBufSQL.append("t5.alone_id,");

				//xulm line_no �ֶ��Ѿ����±��б�ȥ��
//				selBufSQL.append("0,");

				selBufSQL.append(selSumMeasSQL.toString());
				selBufSQL.append(getTotalJoinCond(strTable, strCond, mainPubData, subPubData, vSubPubData,
						bTimeType[0], bTimeType[1], bTimeType[2],false,bNumberMeas,true));

				try{
					selStmt = con.createStatement();
					set = selStmt.executeQuery(selBufSQL.toString());
					Hashtable<String,Boolean> hashUsedData=new Hashtable<String,Boolean>();
					while (set != null && set.next()) {
						// ��¼���ҳ���һ�еĽ��
						Vector<Object> vOneData = new Vector<Object>();
	
						vOneData.add(set.getString(1));
						//xulm ��2������Ϊ�ַ���
	//					vOneData.add(Integer.valueOf(set.getInt(2)));
	//					for (int j = 0; j < vMeasure.size(); j++){
	//						if (bNumberMeas)
	//							vOneData.add(new Double(set.getDouble(j + 3)));
	//						else
	//							vOneData.add(set.getString(j+3));
	//					}
	
						for (int j = 0; j < vMeasure.size(); j++){
							if (bNumberMeas)
								vOneData.add(Double.valueOf(set.getDouble(j + 2)));
							else
								vOneData.add(set.getString(j+2));
						}
	
						if (bNumberMeas)
							vData.add(vOneData);
						else{
							if (hashUsedData.get(vOneData.get(0)+"\r\n"+vOneData.get(1))==null){
								vData.add(vOneData);
								hashUsedData.put((String)vOneData.get(0)+"\r\n"+vOneData.get(1),Boolean.TRUE);
							}
						}
					}
				}finally{
					if(set != null){
						try{
							set.close();
						}catch(Exception e){
//							set.close();
						}
					}
					if(selStmt!=null){
						try{
							selStmt.close();
						}catch(Exception e){
//							selStmt.close();
						}
					}
				}
			}

			// ִ��update��������������ʵ��
			int iBatchCount = 0;
			updateStmt = con.prepareStatement(updateBufSQL.toString());
			for (int i = 0; i < vData.size(); i++) {
				Vector vOneData = vData.get(i);
//				for (int j = 2; j < vOneData.size(); j++) {
//					if (bNumberMeas)
//						updateStmt.setDouble(j-1, ((Double) vOneData.get(j)).doubleValue());
//					else
//						updateStmt.setString(j-1,(String)vOneData.get(j));
//				}

				for (int j = 1; j < vOneData.size(); j++) {
					if (bNumberMeas)
						updateStmt.setDouble(j, ((Double) vOneData.get(j)).doubleValue());
					else
						updateStmt.setString(j,(String)vOneData.get(j));
				}

				updateStmt.setString(vMeasure.size() + 1, (String) vOneData.get(0));
//				updateStmt.setInt(vMeasure.size()+2,((Integer)vOneData.get(1)).intValue());
				updateStmt.addBatch();

				if (iBatchCount++ == 100 || i == vData.size() - 1) {
					updateStmt.executeBatch();
					iBatchCount = 0;
					updateStmt.clearBatch();
				}
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if (set!=null){
				try{
					set.close();
				}catch(Exception e){
					
				}
			}
				
			if (selStmt != null){
				try{
					selStmt.close();
				}catch(Exception e){
//					selStmt.close();
				}
			}
			if (updateStmt != null){
				try{
					updateStmt.close();
				}catch(Exception e){
//					updateStmt.close();
				}
			}
				
			if (con!=null){
				try{
					con.close();
				}catch(Exception e){
//					con.close();
				}
			}
		}
	}

	/*
	 * �����ܣ���ֵ��
	 */
	public void createBalanceTotalNumberDatas(String strTable, String strCond, List<IStoreCell> vMeasure,
			MeasurePubDataVO mainPubData, List<MeasurePubDataVO> vSubPubData,String parentOrgPk) throws SQLException {
		if(vSubPubData!=null && vSubPubData.size()>0){
			//��һ��������0
			insertZeroForBalanceTotal(strTable, strCond, vMeasure, mainPubData, vSubPubData);
			//�ڶ�����update���ݣ�����ǰ��ֵ��ȥsum(�¼���)
			updateBySubsForBalanceTotal(strTable, strCond, vMeasure, mainPubData, vSubPubData,parentOrgPk);
			//��������update���ݣ�����ǰ��ֵ�����ϼ���λ��
			updateByParentForBalanceTotal(strTable, strCond, vMeasure, mainPubData, vSubPubData,parentOrgPk);
		}
	}
	
	public void updateBySubsForBalanceTotal(String strTable, String strCond, List<IStoreCell> vMeasure,
			MeasurePubDataVO mainPubData, List<MeasurePubDataVO> vSubPubData,String parentOrgPk) throws SQLException {
		Connection con =null;
		Statement selStmt = null;
		PreparedStatement updateStmt = null;
		ResultSet set = null;
		try {
			con=getConnection();
			boolean bNumberMeas=(vMeasure.get(0).getType()==MeasureVO.TYPE_NUMBER || vMeasure.get(0).getType()==MeasureVO.TYPE_BIGDECIMAL);

			// ������select�����ҳ���¼������update���������
			MeasurePubDataVO subPubData = vSubPubData.get(0);
			boolean[] bTimeType = getKeywordTimeType(mainPubData, subPubData);
			boolean bDayTime = bTimeType[2];
			boolean bSubTime = bTimeType[0];

			// ����update���SQL��䣬sum�е�SQL���
			StringBuffer selSumMeasSQL = new StringBuffer();
			StringBuffer updateBufSQL = new StringBuffer();
			updateBufSQL.append("update ");
			updateBufSQL.append(strTable);
			updateBufSQL.append(" set ");

			for (int i = 0; i < vMeasure.size(); i++) {
				IStoreCell measure =vMeasure.get(i);

				updateBufSQL.append(measure.getDbcolumn() + "=?");

				if (bNumberMeas)
					selSumMeasSQL.append("0-sum(" + measure.getDbcolumn() + ")");
				else
					selSumMeasSQL.append(measure.getDbcolumn());

				if (i < vMeasure.size() - 1) {
					updateBufSQL.append(",");
					selSumMeasSQL.append(",");
				}
			}
			//xulm line_no �ֶ��Ѿ����±��б�ȥ��
//			updateBufSQL.append(" where alone_id=? and line_no=? ");
			updateBufSQL.append(" where alone_id=?  ");

			// ��¼���ҳ��Ľ��
			Vector<Vector<Object>> vData = new Vector<Vector<Object>>();
			Vector<List<MeasurePubDataVO>> vSplitSubPubData = splitObject(vSubPubData, (bSubTime == true ? 1 : 100));
			for (int i = 0; i < vSplitSubPubData.size(); i++) {
				vSubPubData =vSplitSubPubData.get(i);

				subPubData = vSubPubData.get(0);
				String strAloneID = subPubData.getAloneID();

				// ƴselect���
				StringBuffer selBufSQL = new StringBuffer();
				selBufSQL.append("select ");

				if (subPubData.getKType().equals(mainPubData.getKType()) || bSubTime == true)
					selBufSQL.append("'" + strAloneID + "',");
				else
					selBufSQL.append("t5.alone_id,");

				//xulm line_no �ֶ��Ѿ����±��б�ȥ��
//				selBufSQL.append("0,");

				selBufSQL.append(selSumMeasSQL.toString());
				selBufSQL.append(getBalanceTotalUpdateJoinCond(strTable, strCond, mainPubData, subPubData, vSubPubData,
						parentOrgPk,false,bTimeType[0], bTimeType[1], bTimeType[2],false,bNumberMeas,true));

				try{
					selStmt = con.createStatement();
					set = selStmt.executeQuery(selBufSQL.toString());
					Hashtable<String,Boolean> hashUsedData=new Hashtable<String,Boolean>();
					while (set != null && set.next()) {
						// ��¼���ҳ���һ�еĽ��
						Vector<Object> vOneData = new Vector<Object>();
	
						vOneData.add(set.getString(1));
						//xulm ��2������Ϊ�ַ���
	//					vOneData.add(Integer.valueOf(set.getInt(2)));
	//					for (int j = 0; j < vMeasure.size(); j++){
	//						if (bNumberMeas)
	//							vOneData.add(new Double(set.getDouble(j + 3)));
	//						else
	//							vOneData.add(set.getString(j+3));
	//					}
	
						for (int j = 0; j < vMeasure.size(); j++){
							if (bNumberMeas)
								vOneData.add(Double.valueOf(set.getDouble(j + 2)));
							else
								vOneData.add(set.getString(j+2));
						}
	
						if (bNumberMeas)
							vData.add(vOneData);
						else{
							if (hashUsedData.get(vOneData.get(0)+"\r\n"+vOneData.get(1))==null){
								vData.add(vOneData);
								hashUsedData.put((String)vOneData.get(0)+"\r\n"+vOneData.get(1),Boolean.TRUE);
							}
						}
					}
				}finally{
					if(set != null){
						try{
							set.close();
						}catch(Exception e){
//							set.close();
						}
					}
					if(selStmt!=null){
						try{
							selStmt.close();
						}catch(Exception e){
//							selStmt.close();
						}
					}
				}
			}

			// ִ��update��������������ʵ��
			int iBatchCount = 0;
			updateStmt = con.prepareStatement(updateBufSQL.toString());
			for (int i = 0; i < vData.size(); i++) {
				Vector vOneData = vData.get(i);
//				for (int j = 2; j < vOneData.size(); j++) {
//					if (bNumberMeas)
//						updateStmt.setDouble(j-1, ((Double) vOneData.get(j)).doubleValue());
//					else
//						updateStmt.setString(j-1,(String)vOneData.get(j));
//				}

				for (int j = 1; j < vOneData.size(); j++) {
					if (bNumberMeas)
						updateStmt.setDouble(j, ((Double) vOneData.get(j)).doubleValue());
					else
						updateStmt.setString(j,(String)vOneData.get(j));
				}

				updateStmt.setString(vMeasure.size() + 1, (String) vOneData.get(0));
//				updateStmt.setInt(vMeasure.size()+2,((Integer)vOneData.get(1)).intValue());
				updateStmt.addBatch();

				if (iBatchCount++ == 100 || i == vData.size() - 1) {
					updateStmt.executeBatch();
					iBatchCount = 0;
					updateStmt.clearBatch();
				}
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if (set!=null){
				try{
					set.close();
				}catch(Exception e){
					
				}
			}
				
			if (selStmt != null){
				try{
					selStmt.close();
				}catch(Exception e){
				}
			}
			if (updateStmt != null){
				try{
					updateStmt.close();
				}catch(Exception e){
				}
			}
				
			if (con!=null){
				try{
					con.close();
				}catch(Exception e){
				}
			}
		}
	}
	
	public void updateByParentForBalanceTotal(String strTable, String strCond, List<IStoreCell> vMeasure,
			MeasurePubDataVO mainPubData, List<MeasurePubDataVO> vSubPubData,String parentOrgPk) throws SQLException {
		Connection con =null;
		Statement selStmt = null;
		PreparedStatement updateStmt = null;
		ResultSet set = null;
		try {
			con=getConnection();
			boolean bNumberMeas=(vMeasure.get(0).getType()==MeasureVO.TYPE_NUMBER || vMeasure.get(0).getType()==MeasureVO.TYPE_BIGDECIMAL);

			// ������select�����ҳ���¼������update���������
			MeasurePubDataVO subPubData = vSubPubData.get(0);
			boolean[] bTimeType = getKeywordTimeType(mainPubData, subPubData);
			boolean bDayTime = bTimeType[2];
			boolean bSubTime = bTimeType[0];

			// ����update���SQL��䣬sum�е�SQL���
			StringBuffer selSumMeasSQL = new StringBuffer();
			StringBuffer updateBufSQL = new StringBuffer();
			updateBufSQL.append("update ");
			updateBufSQL.append(strTable);
			updateBufSQL.append(" set ");

			for (int i = 0; i < vMeasure.size(); i++) {
				IStoreCell measure =vMeasure.get(i);

				updateBufSQL.append(measure.getDbcolumn() + "=?+"+measure.getDbcolumn());

				if (bNumberMeas)
					selSumMeasSQL.append("sum(" + measure.getDbcolumn() + ")");
				else
					selSumMeasSQL.append(measure.getDbcolumn());

				if (i < vMeasure.size() - 1) {
					updateBufSQL.append(",");
					selSumMeasSQL.append(",");
				}
			}
			//xulm line_no �ֶ��Ѿ����±��б�ȥ��
//			updateBufSQL.append(" where alone_id=? and line_no=? ");
			updateBufSQL.append(" where alone_id=?  ");

			// ��¼���ҳ��Ľ��
			Vector<Vector<Object>> vData = new Vector<Vector<Object>>();
			Vector<List<MeasurePubDataVO>> vSplitSubPubData = splitObject(vSubPubData, (bSubTime == true ? 1 : 100));
			for (int i = 0; i < vSplitSubPubData.size(); i++) {
				vSubPubData =vSplitSubPubData.get(i);

				subPubData = vSubPubData.get(0);
				String strAloneID = subPubData.getAloneID();

				// ƴselect���
				StringBuffer selBufSQL = new StringBuffer();
				selBufSQL.append("select ");

				if (subPubData.getKType().equals(mainPubData.getKType()) || bSubTime == true)
					selBufSQL.append("'" + strAloneID + "',");
				else
					selBufSQL.append("t5.alone_id,");

				//xulm line_no �ֶ��Ѿ����±��б�ȥ��
//				selBufSQL.append("0,");

				selBufSQL.append(selSumMeasSQL.toString());
				selBufSQL.append(getBalanceTotalUpdateJoinCond(strTable, strCond, mainPubData, subPubData, vSubPubData,
						parentOrgPk,true,bTimeType[0], bTimeType[1], bTimeType[2],false,bNumberMeas,true));

				try{
					selStmt = con.createStatement();
					set = selStmt.executeQuery(selBufSQL.toString());
					Hashtable<String,Boolean> hashUsedData=new Hashtable<String,Boolean>();
					while (set != null && set.next()) {
						// ��¼���ҳ���һ�еĽ��
						Vector<Object> vOneData = new Vector<Object>();
	
						vOneData.add(set.getString(1));
						//xulm ��2������Ϊ�ַ���
	//					vOneData.add(Integer.valueOf(set.getInt(2)));
	//					for (int j = 0; j < vMeasure.size(); j++){
	//						if (bNumberMeas)
	//							vOneData.add(new Double(set.getDouble(j + 3)));
	//						else
	//							vOneData.add(set.getString(j+3));
	//					}
	
						for (int j = 0; j < vMeasure.size(); j++){
							if (bNumberMeas)
								vOneData.add(Double.valueOf(set.getDouble(j + 2)));
							else
								vOneData.add(set.getString(j+2));
						}
	
						if (bNumberMeas)
							vData.add(vOneData);
						else{
							if (hashUsedData.get(vOneData.get(0)+"\r\n"+vOneData.get(1))==null){
								vData.add(vOneData);
								hashUsedData.put((String)vOneData.get(0)+"\r\n"+vOneData.get(1),Boolean.TRUE);
							}
						}
					}
				}finally{
					if(set != null){
						try{
							set.close();
						}catch(Exception e){
//							set.close();
						}
					}
					if(selStmt!=null){
						try{
							selStmt.close();
						}catch(Exception e){
//							selStmt.close();
						}
					}
				}
			}

			// ִ��update��������������ʵ��
			int iBatchCount = 0;
			updateStmt = con.prepareStatement(updateBufSQL.toString());
			for (int i = 0; i < vData.size(); i++) {
				Vector vOneData = vData.get(i);
//				for (int j = 2; j < vOneData.size(); j++) {
//					if (bNumberMeas)
//						updateStmt.setDouble(j-1, ((Double) vOneData.get(j)).doubleValue());
//					else
//						updateStmt.setString(j-1,(String)vOneData.get(j));
//				}

				for (int j = 1; j < vOneData.size(); j++) {
					if (bNumberMeas)
						updateStmt.setDouble(j, ((Double) vOneData.get(j)).doubleValue());
					else
						updateStmt.setString(j,(String)vOneData.get(j));
				}

				updateStmt.setString(vMeasure.size() + 1, (String) vOneData.get(0));
//				updateStmt.setInt(vMeasure.size()+2,((Integer)vOneData.get(1)).intValue());
				updateStmt.addBatch();

				if (iBatchCount++ == 100 || i == vData.size() - 1) {
					updateStmt.executeBatch();
					iBatchCount = 0;
					updateStmt.clearBatch();
				}
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if (set!=null){
				try{
					set.close();
				}catch(Exception e){
					
				}
			}
				
			if (selStmt != null){
				try{
					selStmt.close();
				}catch(Exception e){
				}
			}
			if (updateStmt != null){
				try{
					updateStmt.close();
				}catch(Exception e){
				}
			}
				
			if (con!=null){
				try{
					con.close();
				}catch(Exception e){
				}
			}
		}
	}
	
//	public void createBalanceTotalNoDynDatasByInsert(String strTable, String strCond, List<IStoreCell> vMeasure,
//			MeasurePubDataVO mainPubData, List<MeasurePubDataVO> vSubPubData,String parentOrgPk) throws SQLException {
//		//��һ��������0
//		insertZeroForBalanceTotal(strTable, strCond, vMeasure, mainPubData, vSubPubData);
//		//�ڶ�����update
//		createBalanceTotalNoDynDatasByUpdate111(strTable, strCond, vMeasure, mainPubData, vSubPubData,parentOrgPk);
//		//��������update
//		createBalanceTotalNoDynDatasByUpdate222(strTable, strCond, vMeasure, mainPubData, vSubPubData,parentOrgPk);
//		
//		
//		//������ַ���ָ�꣬����ֵ��δ���ɣ���update������������
////		boolean bNumberMeas=(vMeasure.get(0).getType()==IStoreCell.TYPE_NUMBER || vMeasure.get(0).getType()==IStoreCell.TYPE_BIGDECIMAL);
////		if (!bNumberMeas){
////			createBalanceTotalNoDynDatasByUpdate(strTable, strCond, vMeasure, mainPubData, vSubPubData);
////		}
//	}
	
	private void insertZeroForBalanceTotal(String strTable, String strCond, List<IStoreCell> vMeasure,
			MeasurePubDataVO mainPubData, List<MeasurePubDataVO> vSubPubData) throws SQLException {
		Connection con =null;
		Statement stmt = null;
		try {
			con=getConnection();
			stmt = con.createStatement();

			//�ַ���ָ������ֵ
			boolean bNumberMeas=(vMeasure.get(0).getType()==MeasureVO.TYPE_NUMBER || vMeasure.get(0).getType()==MeasureVO.TYPE_BIGDECIMAL);

			MeasurePubDataVO subPubData = vSubPubData.get(0);
			boolean[] bTimeType = getKeywordTimeType(mainPubData, subPubData);
			boolean bDayTime = bTimeType[2];
			boolean bSubTime = bTimeType[0];

			Vector<List<MeasurePubDataVO>> vSplitSubPubData = splitObject(vSubPubData, (bSubTime == true ? 1 : 100));
			int iBatchCount = 0;
			//��һ���������ϼ���λ����
			for (int i = 0; i < vSplitSubPubData.size(); i++) {
				vSubPubData =vSplitSubPubData.get(i);

				subPubData = vSubPubData.get(0);
				String strAloneID = subPubData.getAloneID();

				// ƴ��insert into select���
				StringBuffer bufSQL = new StringBuffer();
				bufSQL.append("insert into ");
				bufSQL.append(strTable);//ָ�����ݱ�
				bufSQL.append("(alone_id,");

				for (int j = 0; j < vMeasure.size(); j++) {
					IStoreCell storeCell = vMeasure.get(j);
					bufSQL.append(storeCell.getDbcolumn());

					if (j < vMeasure.size() - 1)
						bufSQL.append(",");
				}

				bufSQL.append(") ");

				StringBuffer selBufSQL = new StringBuffer();
				//������
				StringBuffer colBufSQL=new StringBuffer();

				//�ӱ����ʱ��ؼ���
				if (subPubData.getKType().equals(mainPubData.getKType()) || bSubTime == true) {
					colBufSQL.append("'" + strAloneID + "',");
				}
				else {
					colBufSQL.append("t5.alone_id, ");
				}

				for (int j = 0; j < vMeasure.size(); j++) {
					IStoreCell measure = vMeasure.get(j);
					if (bNumberMeas){
						colBufSQL.append(measure.getDbcolumn());
					}
					else{
						colBufSQL.append("''");
					}

					if (j < vMeasure.size() - 1)
						colBufSQL.append(",");
				}
				//*******************************************************************************************************
				//���Ϸ���"insert into iufo_measure_data_30dm1c3r (alone_id/t5.alone_id, m10000, m10001, m10003, m10002)"

				//��һ����ȡ�û��ܽ������Ļ��ܵ�ָ�����ݱ���
				if (!bNumberMeas && (subPubData.getKType().equals(mainPubData.getKType()) || bSubTime == true)) {
					selBufSQL.append(" values(");
					selBufSQL.append(colBufSQL);
					selBufSQL.append(")");
				}
				else{
//					selBufSQL.append(" values(");
					StringBuffer zeroBufSQL=new StringBuffer();
					if (subPubData.getKType().equals(mainPubData.getKType()) || bSubTime == true) {
						zeroBufSQL.append("'" + strAloneID + "',");
					}
					else {
						zeroBufSQL.append("t5.alone_id, ");
					}
					for (int j = 0; j < vMeasure.size(); j++) {
						zeroBufSQL.append("0");
						if (j < vMeasure.size() - 1)
							zeroBufSQL.append(",");
					}
					
					selBufSQL.append("select distinct ");
					selBufSQL.append(zeroBufSQL);
					selBufSQL.append(getTotalJoinCond(strTable, strCond, mainPubData, subPubData, vSubPubData,
							bTimeType[0], bTimeType[1], bTimeType[2],false,true,true));
				}

				bufSQL.append(selBufSQL);
				stmt.addBatch(bufSQL.toString());

				if (iBatchCount++ == 100 || i == vSplitSubPubData.size() - 1) {
					stmt.executeBatch();
					iBatchCount = 0;
					stmt.clearBatch();
				}
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if (stmt != null){
				try{
					stmt.close();
				}catch(Exception e){
					
				}
			}
			if (con!=null){
				try{
					con.close();
				}catch(Exception e){
					
				}
			}
		}
	}
	
	/**
	 * ����ʱ�����ɼ��ű����������Ĺ�������
	 * @param strTable
	 * @param strCond
	 * @param mainPubData
	 * @param subPubData
	 * @param vSubPubData
	 * @param bSubTime
	 * @param bWeekTime
	 * @param bDayTime
	 * @param bNeedPubData2
	 * @param bNumberMeas
	 * @param bUseTempTable
	 * @return
	 * @throws SQLException
	 */
	private String getTotalJoinCond(String strTable,String strCond,MeasurePubDataVO mainPubData,MeasurePubDataVO subPubData,List<MeasurePubDataVO> vSubPubData,boolean bSubTime,boolean bWeekTime,boolean bDayTime,boolean bNeedPubData2,boolean bNumberMeas,boolean bUseTempTable) throws SQLException{
	    StringBuffer selBufSQL=new StringBuffer();
	    selBufSQL.append(" from ");

	    //������ӱ�ͬʱ��ʱ��ؼ��֣����ӵ�6�ű�iufo_keydetail_time
	    //������ӱ���ͬʱ��ʱ��ؼ��֣���iufo_measure_pubdata��ֱ�ӺͲ�ѯ������ͨ��Alone_id��������

	    selBufSQL.append(strTable + " t1,");//t1 �ӱ�
	    if (!mainPubData.getKType().equals(subPubData.getKType())){//���ӱ��ϵ��֧
	    	selBufSQL.append(MeasureDataUtil.getMeasurePubTableName(subPubData.getKType())+" t2, ");//t2 �ӱ��pubDataVO�� iufo_measpub_109O
	        if (bUseTempTable)
	        	selBufSQL.append(strCond + " t3 ");// t3 �����pubDataVO�� iufo_measpub_0007
	        else{
	        	selBufSQL.append(MeasureDataUtil.getMeasurePubTableName(mainPubData.getKType())+" t3, ");//�ĳ�mainpubdata
	        	selBufSQL.append(strCond+" t4 ");
	        }

	        if (!bSubTime && vSubPubData != null)
	        	selBufSQL.append("," + MeasureDataUtil.getMeasurePubTableName(subPubData.getKType()) + " t5 ");

	        //�ӱ����ʱ��ؼ���
	        if (bSubTime)
	        	selBufSQL.append(", iufo_keydetail_time t6 ");

	        //***********************************************
	        //������ɱ�����
	        //��һ����where����

	        selBufSQL.append(" where t1.alone_id=t2.alone_id ");
	        if (!bUseTempTable)
	        	selBufSQL.append(" and t3.alone_id = t4.alone_id ");
	        selBufSQL.append(" and t2.ver=t3.ver and ");
	        if (!bSubTime && vSubPubData!=null){
	        	selBufSQL.append(" t5.alone_id in(");
	        	for (int i = 0;i < vSubPubData.size(); i++){
	        		selBufSQL.append("'"+vSubPubData.get(i).getAloneID()+"'");
	        		if (i<vSubPubData.size() -1 )
	        			selBufSQL.append(",");
	        		else
	        			selBufSQL.append(") ");
	        	}
	        }
	        //ȷ���鿴������Դ�Լ�����ִ��ʱsql��ȷ�ԣ�modified by jiaah at 2011-3-9
	        else
	        	selBufSQL.append(" 1=1 ");

	        //�ӱ����ʱ��ؼ���
	        selBufSQL.append(getTotalTimeJoinCond("t3","t2",mainPubData,subPubData,vSubPubData,bSubTime,bWeekTime,bDayTime,"t5"));
	        if (!bSubTime && vSubPubData != null && bNumberMeas){
	        	selBufSQL.append(" group by t5.alone_id ");
	        }
	    }
	    //�����ӱ��ϵ�Ļ���
	    else{
	    	if (bNeedPubData2){
	    		selBufSQL.append(strCond+" t3, "+MeasureDataUtil.getMeasurePubTableName(subPubData.getKType())+" t2 ");
	    		selBufSQL.append(" where t1.alone_id=t2.alone_id and t3.alone_id=t2.alone_id ");
	    	}else{
	    		selBufSQL.append(""+strCond+" t3 ");
	    		selBufSQL.append(" where t1.alone_id=t3.alone_id ");
	    	}
	    }

	    return selBufSQL.toString();
	}
	
	/*
	 * ��Բ����ܸ���ʱ���������
	 */
	private String getBalanceTotalUpdateJoinCond(String strTable,String strCond,MeasurePubDataVO mainPubData,MeasurePubDataVO subPubData,List<MeasurePubDataVO> vSubPubData,String parentOrgPk,boolean bParentOrg,boolean bSubTime,boolean bWeekTime,boolean bDayTime,boolean bNeedPubData2,boolean bNumberMeas,boolean bUseTempTable) throws SQLException{
		String unitKeyColName=null;
		String tempTableT=null;
		if(mainPubData!=null){
			int index=mainPubData.getKeyByPK(KeyVO.CORP_PK)+1;
			unitKeyColName="keyword"+index;
		}
		
		StringBuffer selBufSQL=new StringBuffer();
	    selBufSQL.append(" from ");

	    //������ӱ�ͬʱ��ʱ��ؼ��֣����ӵ�6�ű�iufo_keydetail_time
	    //������ӱ���ͬʱ��ʱ��ؼ��֣���iufo_measure_pubdata��ֱ�ӺͲ�ѯ������ͨ��Alone_id��������

	    selBufSQL.append(strTable + " t1,");//t1 �ӱ�
	    if (!mainPubData.getKType().equals(subPubData.getKType())){//���ӱ��ϵ��֧
	    	selBufSQL.append(MeasureDataUtil.getMeasurePubTableName(subPubData.getKType())+" t2, ");//t2 �ӱ��pubDataVO�� iufo_measpub_109O
	        if (bUseTempTable){
	        	selBufSQL.append(strCond + " t3 ");// t3 �����pubDataVO�� iufo_measpub_0007
	        	tempTableT="t3";
	        }
	        else{
	        	selBufSQL.append(MeasureDataUtil.getMeasurePubTableName(mainPubData.getKType())+" t3, ");//�ĳ�mainpubdata
	        	selBufSQL.append(strCond+" t4 ");
	        	tempTableT="t4";
	        }

	        if (!bSubTime && vSubPubData != null)
	        	selBufSQL.append("," + MeasureDataUtil.getMeasurePubTableName(subPubData.getKType()) + " t5 ");

	        //�ӱ����ʱ��ؼ���
	        if (bSubTime)
	        	selBufSQL.append(", iufo_keydetail_time t6 ");

	        //***********************************************
	        //������ɱ�����
	        //��һ����where����

	        selBufSQL.append(" where t1.alone_id=t2.alone_id ");
	        if (!bUseTempTable)
	        	selBufSQL.append(" and t3.alone_id = t4.alone_id ");
	        selBufSQL.append(" and t2.ver=t3.ver and ");
	        if (!bSubTime && vSubPubData!=null){
	        	selBufSQL.append(" t5.alone_id in(");
	        	for (int i = 0;i < vSubPubData.size(); i++){
	        		selBufSQL.append("'"+vSubPubData.get(i).getAloneID()+"'");
	        		if (i<vSubPubData.size() -1 )
	        			selBufSQL.append(",");
	        		else
	        			selBufSQL.append(") ");
	        	}
	        }
	        //ȷ���鿴������Դ�Լ�����ִ��ʱsql��ȷ�ԣ�modified by jiaah at 2011-3-9
	        else
	        	selBufSQL.append(" 1=1 ");

	        //�ӱ����ʱ��ؼ���
	        selBufSQL.append(getTotalTimeJoinCond("t3","t2",mainPubData,subPubData,vSubPubData,bSubTime,bWeekTime,bDayTime,"t5"));
	      //�����ܵĴ���
		    selBufSQL.append(" and ");
		    selBufSQL.append(tempTableT);
		    selBufSQL.append(".");
		    selBufSQL.append(unitKeyColName);
		    if(bParentOrg){
		    	selBufSQL.append("=");
		    }else{
		    	selBufSQL.append("<>");
		    }
		    selBufSQL.append("'");
		    selBufSQL.append(parentOrgPk);
		    selBufSQL.append("'");
		    //�����ܵĴ���end
	        
	        if (!bSubTime && vSubPubData != null && bNumberMeas){
	        	selBufSQL.append(" group by t5.alone_id ");
	        }
	    }
	    //�����ӱ��ϵ�Ļ���
	    else{
	    	if (bNeedPubData2){
	    		selBufSQL.append(strCond+" t3, "+MeasureDataUtil.getMeasurePubTableName(subPubData.getKType())+" t2 ");
	    		selBufSQL.append(" where t1.alone_id=t2.alone_id and t3.alone_id=t2.alone_id ");
	    	}else{
	    		selBufSQL.append(""+strCond+" t3 ");
	    		selBufSQL.append(" where t1.alone_id=t3.alone_id ");
	    	}
	    	tempTableT="t3";
	    	//�����ܵĴ���
		    selBufSQL.append(" and ");
		    selBufSQL.append(tempTableT);
		    selBufSQL.append(".");
		    selBufSQL.append(unitKeyColName);
		    if(bParentOrg){
		    	selBufSQL.append("=");
		    }else{
		    	selBufSQL.append("<>");
		    }
		    selBufSQL.append("'");
		    selBufSQL.append(parentOrgPk);
		    selBufSQL.append("'");
		    //�����ܵĴ���end
	    }
	    
	    return selBufSQL.toString();
	}
	
	
	/**
	 * ����ʱ�����ɼ��ű����������Ĺ�������
	 * @param strMainTable�������Ӧ��iufo_measure_pubdata��
	 * @param strSubTable����̬����Ӧ��iufo_measure_pubdata��
	 * @param mainPubData�������Ӧ��MeasurePubDataVO
	 * @param subPubData����̬����Ӧ��MeasurePubDataVO
	 * @param vSubPubData,
	 * @param bSubTime,�Ƿ������ӱ���ʱ��
	 * @param bWeekTime���ӱ��Ƿ�����ʱ��
	 * @param bDayTime���ӱ��Ƿ�����
	 * @param strAppendTable
	 * @return
	 */
	private String getTotalTimeJoinCond(String strMainTable, String strSubTable, MeasurePubDataVO mainPubData,
			MeasurePubDataVO subPubData, List<MeasurePubDataVO> vSubPubData, boolean bSubTime, boolean bWeekTime, boolean bDayTime,
			String strAppendTable) {
		StringBuffer selBufSQL = new StringBuffer();

		// ���ӱ�ͬ�ؼ��֣����ڸ��Թؼ�������е�λ�ÿ��ܲ���ͬ������������Ķ�Ӧ��ϵ
		Hashtable<Integer,Integer> hashPos = new Hashtable<Integer,Integer>();
		KeyVO[] mainKeys = mainPubData.getKeyGroup().getKeys();
		KeyVO[] subKeys = subPubData.getKeyGroup().getKeys();
		boolean bMainKeyNoTime =UFODate.NONE_PERIOD.equalsIgnoreCase(mainPubData.getKeyGroup().getTTimeProp()) && UFODate.NONE_PERIOD.equalsIgnoreCase(mainPubData.getKeyGroup().getTTimeProp());
		for (int i = 0; i < subKeys.length; i++) {
			for (int j = 0; j < mainKeys.length; j++) {
				if (mainKeys[j].equals(subKeys[i])) {
					hashPos.put(Integer.valueOf(i), Integer.valueOf(j));
					break;
				}
			}
		}

		if (bSubTime == true || vSubPubData == null)
			selBufSQL.append(genWhereSQL(subPubData, strSubTable, bSubTime));
		else {
			for (int i = 0; i < subKeys.length; i++) {
				if ((bMainKeyNoTime || subKeys[i].getTTimeKeyIndex() < 0) && hashPos.get(Integer.valueOf(i)) == null){
					selBufSQL.append(" and " + strSubTable + ".keyword" + (i + 1) + "=" + strAppendTable + ".keyword"
							+ (i + 1));
				}
			}
		}

		// ��t2���t3��������������
		// ���ݹؼ��ֶ�Ӧ��ϵ������keyword����������
		Enumeration enumPos = hashPos.keys();
		while (enumPos.hasMoreElements()) {
			Integer iSubKeyPos = (Integer) enumPos.nextElement();
			Integer iMainKeyPos = hashPos.get(iSubKeyPos);
			selBufSQL.append(" and " + strSubTable + ".keyword" + (iSubKeyPos.intValue() + 1) + "=" + strMainTable
					+ ".keyword" + (iMainKeyPos.intValue() + 1));
		}

		String time = subPubData.getInputDate();
		//�ӱ�����ʱ��ؼ��� �ӱ����ʱ��ؼ��֣� ����t2���t3��TimeCode����������
		if (!bSubTime) {
			return selBufSQL.toString();
		}
		else {
			String timeCode = null;
			if(!subPubData.getKeyGroup().isTTimeTypeAcc())
				timeCode = UFODate.getTimeCode(subPubData.getInputDate());
			else{
				timeCode = time.substring(time.length() -2, time.length());
			}
			selBufSQL.append(" and ");
			//�ӱ�ʱ��ؼ��ֵ�����
			int isubIndex = subPubData.getKeyGroup().getIndexByKeywordPK(subPubData.getKeyGroup().getTTimeKey().getPk_keyword()) + 1;
			selBufSQL.append("  t6.keyval = t2.keyword" + isubIndex);
			selBufSQL.append(" and '");
			//��Ϊ�����ӱ��洢һ��ʱ�����Զ�����������������
			if(bWeekTime)
				selBufSQL.append(timeCode.substring(timeCode.length() -4, timeCode.length()-2));//�����ܣ�
			else if(subPubData.getKeyGroup().isTTimeTypeAcc()){
				selBufSQL.append(timeCode);
			}
			else{
				selBufSQL.append(timeCode.substring(8,10)).append("' = t6.time_month and '");

				selBufSQL.append(timeCode.substring(timeCode.length() -2, timeCode.length()));//���������λ����
			}
			selBufSQL.append("'=");
			if(bWeekTime)
				selBufSQL.append(" t6.time_week");
			//����ڼ�ؼ���
			else if(subPubData.getKeyGroup().isTTimeTypeAcc()){
				selBufSQL.append(" t6.time_month");
			}
			else
				selBufSQL.append(" t6.time_day");

			//������
			String strYearCode = time.substring(0, 4);
			selBufSQL.append(" and '");
			selBufSQL.append(strYearCode + " '=");
			selBufSQL.append(" t6.time_year");

			//TODO : ��ǰʱ������
//			String strMonthCode = timeCode.substring(8, 10);
//			if (strMonthCode.equals("04") || strMonthCode.equals("06") || strMonthCode.equals("09")
//					|| strMonthCode.equals("11")) {
//				selBufSQL.append(" (case when "
//						+ getSubFuncSQL(strSubFunc, strSubTable, "time_code", iSubIndex1 + 1, iSubLen1) + ">'30' ");
//				selBufSQL.append(" then '30' else "
//						+ getSubFuncSQL(strSubFunc, strSubTable, "time_code", iSubIndex1 + 1, iSubLen1) + " end) ");
//			} else if (strMonthCode.equals("02")) {
//				int iYear = Integer.parseInt(timeCode.substring(0, 4));
//				if (((double) iYear / 4) == iYear / 4) {
//					selBufSQL.append(" (case when "
//							+ getSubFuncSQL(strSubFunc, strSubTable, "time_code", iSubIndex1 + 1, iSubLen1) + ">'29' ");
//					selBufSQL.append(" then '29' else "
//							+ getSubFuncSQL(strSubFunc, strSubTable, "time_code", iSubIndex1 + 1, iSubLen1) + " end) ");
//				} else {
//					selBufSQL.append(" (case when "
//							+ getSubFuncSQL(strSubFunc, strSubTable, "time_code", iSubIndex1 + 1, iSubLen1) + ">'28' ");
//					selBufSQL.append(" then '28' else "
//							+ getSubFuncSQL(strSubFunc, strSubTable, "time_code", iSubIndex1 + 1, iSubLen1) + " end) ");
//				}
//			} else
//				selBufSQL.append(getSubFuncSQL(strSubFunc, strSubTable, "time_code", iSubIndex1 + 1, iSubLen1));
		}

		return selBufSQL.toString();
	}


	/**
	 * ���ݿ��в����ڸ��м�¼����insert����������¼�¼
	 * @param strTable��ָ�����ݱ�
	 * @param strCond������ɸѡ����SQL���
	 * @param vMeasure���μӻ��ܵ�ָ��
	 * @param mainPubData�������MeasurePubDataVO
	 * @param vSubPubData����Ҫ���ɵ��ӱ��MeasurePubDataVO����
	 * @throws SQLException
	 */
	private void innerCreateTotalNoDynDatasByInsert(String strTable, String strCond, List<IStoreCell> vMeasure,
			MeasurePubDataVO mainPubData, List<MeasurePubDataVO> vSubPubData) throws SQLException {
		Connection con =null;
		Statement stmt = null;
		try {
			con=getConnection();
			stmt = con.createStatement();

			//�ַ���ָ������ֵ
			boolean bNumberMeas=(vMeasure.get(0).getType()==MeasureVO.TYPE_NUMBER || vMeasure.get(0).getType()==MeasureVO.TYPE_BIGDECIMAL);

			MeasurePubDataVO subPubData = vSubPubData.get(0);
			boolean[] bTimeType = getKeywordTimeType(mainPubData, subPubData);
			boolean bDayTime = bTimeType[2];
			boolean bSubTime = bTimeType[0];

			Vector<List<MeasurePubDataVO>> vSplitSubPubData = splitObject(vSubPubData, (bSubTime == true ? 1 : 100));
			int iBatchCount = 0;
			for (int i = 0; i < vSplitSubPubData.size(); i++) {
				vSubPubData =vSplitSubPubData.get(i);

				subPubData = vSubPubData.get(0);
				String strAloneID = subPubData.getAloneID();

				// ƴ��insert into select���
				StringBuffer bufSQL = new StringBuffer();
				bufSQL.append("insert into ");
				bufSQL.append(strTable);//ָ�����ݱ�
				bufSQL.append("(alone_id,");

				for (int j = 0; j < vMeasure.size(); j++) {
					IStoreCell storeCell = vMeasure.get(j);
					bufSQL.append(storeCell.getDbcolumn());

					if (j < vMeasure.size() - 1)
						bufSQL.append(",");
				}

				bufSQL.append(") ");

				StringBuffer selBufSQL = new StringBuffer();
				//������
				StringBuffer colBufSQL=new StringBuffer();

				//�ӱ����ʱ��ؼ���
				if (subPubData.getKType().equals(mainPubData.getKType()) || bSubTime == true) {
					colBufSQL.append("'" + strAloneID + "',");
				}
				else {
					colBufSQL.append("t5.alone_id, ");
				}

				for (int j = 0; j < vMeasure.size(); j++) {
					IStoreCell measure = vMeasure.get(j);
					if (bNumberMeas)
						colBufSQL.append("sum(" + measure.getDbcolumn() + ")");
					else
						colBufSQL.append("''");

					if (j < vMeasure.size() - 1)
						colBufSQL.append(",");
				}
				//*******************************************************************************************************
				//���Ϸ���"insert into iufo_measure_data_30dm1c3r (alone_id/t5.alone_id, m10000, m10001, m10003, m10002)"

				//��һ����ȡ�û��ܽ������Ļ��ܵ�ָ�����ݱ���
				if (!bNumberMeas && (subPubData.getKType().equals(mainPubData.getKType()) || bSubTime == true)) {
					selBufSQL.append(" values(");
					selBufSQL.append(colBufSQL);
					selBufSQL.append(")");
				}
				else{
					selBufSQL.append("select ");
					selBufSQL.append(colBufSQL);
					selBufSQL.append(getTotalJoinCond(strTable, strCond, mainPubData, subPubData, vSubPubData,
							bTimeType[0], bTimeType[1], bTimeType[2],false,true,true));
				}

				bufSQL.append(selBufSQL);
				stmt.addBatch(bufSQL.toString());

				if (iBatchCount++ == 100 || i == vSplitSubPubData.size() - 1) {
					stmt.executeBatch();
					iBatchCount = 0;
					stmt.clearBatch();
				}
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if (stmt != null){
				try{
					stmt.close();
				}catch(Exception e){
					
				}
			}
			if (con!=null){
				try{
					con.close();
				}catch(Exception e){
					
				}
			}
		}
	}

	private Vector<String> getMeasIDFromMeasure(IStoreCell[] measures) {
		Vector<String> vMeasID = new Vector<String>();
		for (IStoreCell measure : measures)
			vMeasID.add(measure.getCode());
		return vMeasID;
	}

	/**
	 * �õ����ӱ���ʱ�����͹�ϵ
	 * @param mainPubData
	 * @param subPubData
	 * @return
	 */
	private boolean[] getKeywordTimeType(MeasurePubDataVO mainPubData, MeasurePubDataVO subPubData) {
		boolean bSubTime = false;
		boolean bWeekTime = false;
		boolean bDayTime = false;

		String strMainTime = mainPubData.getKeyGroup().getTTimeProp();
		String strSubTime = subPubData.getKeyGroup().getTTimeProp();
		if (strMainTime.equals(nc.vo.iufo.pub.date.UFODate.NONE_PERIOD) == false
				&& strSubTime.equals(nc.vo.iufo.pub.date.UFODate.NONE_PERIOD) == false
				&& strMainTime.equals(strSubTime) == false) {
			bSubTime = true;
			bWeekTime = subPubData.getKeyGroup().getTTimeProp().equals(nc.vo.iufo.pub.date.UFODate.WEEK_PERIOD);
			bDayTime = subPubData.getKeyGroup().getTTimeProp().equals(nc.vo.iufo.pub.date.UFODate.DAY_PERIOD);
		}

		return new boolean[] { bSubTime, bWeekTime, bDayTime };
	}

	/**
	 * ����ʱ������iufo_measure_pubdata�����pubdata�еĹؼ���ֵ����������ϵ
	 * @param pubData
	 * @param strNewTableName
	 * @param bOmitTime
	 * @return
	 */
	private String genWhereSQL(MeasurePubDataVO pubData, String strNewTableName, boolean bOmitTime) {
		if (strNewTableName.length() > 0)
			strNewTableName += ".";
		StringBuffer bufSQL = new StringBuffer();
		//ȥ���ؼ�������
//		if (pubData.getKType() != null)
//			bufSQL.append(strTableName + "ktype='" + pubData.getKType() + "' ");

		KeyVO[] keys = pubData.getKeyGroup().getKeys();

		for (int i = 0; i < 10; i++) {
			String strKeyVal = pubData.getKeywordByIndex(i + 1);
			if (strKeyVal == null || strKeyVal.trim().length() <= 0 || strKeyVal.trim().equalsIgnoreCase("null"))
				continue;

			if (bOmitTime && keys.length > i && keys[i] != null && keys[i].getTTimeKeyIndex() >= 0)
				continue;

//			if (bufSQL.length() > 0)
				bufSQL.append(" and ");

			bufSQL.append(strNewTableName + "keyword" + (i + 1) + "='" + strKeyVal + "'");
		}

		return bufSQL.toString();
	}
	/**
	 * ��MeasurePubDataVO���鰴�Ƿ���ָ�����ݱ����м�¼���ֳ�����
	 * @param strTable��ָ�����ݱ�
	 * @param vPubData��MeasurePubDataVO����
	 * @return�������������飬һ������ָ�����ݱ����м�¼��MeasurePubDataVO���飬һ����û�м�¼��MeasurePubDataVO����
	 * @throws SQLException
	 */
	private Vector<Vector<MeasurePubDataVO>> splitPubDataByExistRecord(String strTable, List<MeasurePubDataVO> vPubData) throws SQLException {
		Connection con=null;
		Statement stmt = null;
		ResultSet set = null;
		try {
			con = getConnection();

			Vector<String> vExistAloneID = new Vector<String>();

			// ��MeasurePubDataVO�����ɶ��С���飬��ֹin����¼����
			Vector<List<MeasurePubDataVO>> vSplitPubData = splitObject(vPubData, 100);

			// ���ҳ������м�¼��aloneid
			for (int i = 0; i < vSplitPubData.size(); i++) {
				List<MeasurePubDataVO> vOnePubData =vSplitPubData.get(i);
				StringBuffer sqlBuf = new StringBuffer();
				sqlBuf.append("select alone_id from " + strTable + " where alone_id in(");
				for (int j = 0; j < vOnePubData.size(); j++) {
					sqlBuf.append("'" + vOnePubData.get(j).getAloneID() + "'");
					if (j < vOnePubData.size() - 1)
						sqlBuf.append(",");
				}
				sqlBuf.append(")");

				try{
					stmt = con.createStatement();
					set = stmt.executeQuery(sqlBuf.toString());
					while (set != null && set.next()) {
						vExistAloneID.add(set.getString(1));
					}
				}finally{
					if(set!=null){
						try{
							set.close();
						}catch(Exception e){
//							set.close();
						}
					}
					if(stmt!=null){
						try{
							stmt.close();
						}catch(Exception e){
//							stmt.close();
						}
					}
				}
//				
//				
//				set.close();
//				set = null;
//				stmt.close();
//				stmt = null;
			}

			// ��MeasurePubDataVO���з���
			Vector<MeasurePubDataVO> vExistPubData = new Vector<MeasurePubDataVO>();
			Vector<MeasurePubDataVO> vNoExistPubData = new Vector<MeasurePubDataVO>();
			for (int i = 0; i < vPubData.size(); i++) {
				MeasurePubDataVO pubData = vPubData.get(i);
				if (vExistAloneID.contains(pubData.getAloneID())) {
					vExistPubData.add(pubData);
				} else
					vNoExistPubData.add(pubData);
			}

			Vector<Vector<MeasurePubDataVO>> vRetPubData=new Vector<Vector<MeasurePubDataVO>>();
			vRetPubData.add(vExistPubData);
			vRetPubData.add(vNoExistPubData);
			return vRetPubData;
		} finally {
			if (set != null){
				try{
					set.close();
				}catch(Exception e){
					
				}
			}
				
			if (stmt != null){
				try{
					stmt.close();
				}catch(Exception e){
					
				}
			}
			if (con != null){
				try{
					con.close();
				}catch(Exception e){
					
				}
			}
		}
	}

	/**
	 * ��һ����������ֳɼ���С������
	 *
	 * @param vObj��ԭ����
	 * @param iSize��һ�����������С
	 * @return�����������
	 */
	private Vector<List<MeasurePubDataVO>> splitObject(List<MeasurePubDataVO> vObj, int iSize) {
		Vector<List<MeasurePubDataVO>> vRet = new Vector<List<MeasurePubDataVO>>();
		if (vObj == null || vObj.size() <= 0)
			return vRet;

		int iCount = 0;
		Vector<MeasurePubDataVO> vArray = new Vector<MeasurePubDataVO>();
		for (int i = 0; i < vObj.size(); i++) {
			vArray.add(vObj.get(i));
			if (++iCount == iSize || i == vObj.size() - 1) {
				vRet.add(vArray);
				vArray = new Vector<MeasurePubDataVO>();
				iCount = 0;
			}
		}
		return vRet;
	}

}
