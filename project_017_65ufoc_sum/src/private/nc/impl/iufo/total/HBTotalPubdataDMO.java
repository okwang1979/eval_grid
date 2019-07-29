package nc.impl.iufo.total;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.iufo.DataManageObjectIufo;
import nc.impl.iufo.utils.StringConnectUtil;
import nc.itf.hbbb.hbrepstru.IHBRepstruQrySrv;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.pub.iufo.exception.UFOSrvException;
import nc.util.iufo.measurepubdata.MeasurePubdataSqlUtil;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.total.HbTotalSchemeVO;
import nc.vo.iufo.data.MeasureDataUtil;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.pub.IDatabaseType;
import nc.vo.iufo.pub.date.UFODate;
import nc.vo.jcom.lang.StringUtil;
public class HBTotalPubdataDMO  extends DataManageObjectIufo{
	
	/**
	 * ����ʱ��Ϊ�˼ӿ�����ٶȣ�����������Ӧ�ò��ҳ���iufo_measure_pubdata���еļ�¼���ҳ�����
	 * ����һ��ͬiufo_measure_pubdata��ṹ��ȫ��ͬ����ʱ�������ҳ����ļ�¼������ʱ����
	 * @param strCond
	 * @return
	 * @throws SQLException
	 * @throws UFOSrvException 
	 * @throws DAOException 
	 */
	public String createTempPubDataTableFromCond(String strCond,MeasurePubDataVO pubData,String[] reportIds,boolean bContainNotCommit,
			boolean bPermission,HbTotalSchemeVO totalScheme,HBSchemeVO hbSchemeVO) throws SQLException, UFOSrvException, DAOException {
		Connection con = null;
		Statement stmt = null;
		try {
			con = getConnection();

			// ��ʱ�����������
			String strTable ="iufo_tmp_pub_"+pubData.getKeyGroup().getTableName().substring(pubData.getKeyGroup().getTableName().length()-4);

			// oracle���ݿ����ַ��͡���ֵ�Ͷ�Ӧ���������������ݿⲻ��ͬ
			String strChar = null;
			String strNumber = null;
			if (getDBType().equals(IDatabaseType.DATABASE_ORACLE)) {
				strChar = " varchar2";
				strNumber = " number";
			} else {
				strChar = " varchar";
				strNumber = " float";
			}

			StringBuffer bufSQL = new StringBuffer();
			bufSQL.append("alone_id ").append(strChar).append("(32),");
			bufSQL.append("ver ").append(strNumber);
			int iKeyNum=pubData.getKeyGroup().getKeys().length;
			for (int i=0;i<iKeyNum;i++)
				bufSQL.append(",keyword").append(i+1).append(strChar).append("(64)");

//			InvocationInfoProxy.getInstance().setUserDataSource();
			nc.bs.mw.sqltrans.TempTable tmptab = new nc.bs.mw.sqltrans.TempTable();
			strTable = tmptab.createTempTable(con, strTable, bufSQL.toString(), null);

			StringBuffer bufCol = new StringBuffer();
			bufCol.append("alone_id,ver");
			for (int i=0;i<iKeyNum;i++)
				bufCol.append(",keyword").append(i+1);

			bufSQL = new StringBuffer();
			bufSQL.append("insert into ").append(strTable).append("(").append(bufCol.toString()).append(") ");
			bufSQL.append(" (");
			//�ϲ�����֯
			List<String> hborgs = new ArrayList<String>();
			//���𱨱�����
			List<String> gborgs = new ArrayList<String>();
//			String roleSQL  = StringConnectUtil.getInSqlGroupByArr(pk_roles, "pk_role");
			IHBRepstruQrySrv service = NCLocator.getInstance().lookup(IHBRepstruQrySrv.class);
			BaseDAO dao = new BaseDAO();
			Collection res = (Collection) dao.executeQuery(strCond, new ArrayListProcessor());
			gborgs.add(totalScheme.getPk_org());
			for (Object object : res) {
				Object[] row = (Object[])object;
				if (row != null && row.length == 1 && row[0] != null) {
					String pk_org = row[0].toString();
					if(service.isLeafMember(pk_org, totalScheme.getPk_rmsversion())||totalScheme.getTotalType().equals(HbTotalSchemeVO.TOTAL_TYPE_ALL)){
						gborgs.add(row[0].toString());
					}else{
						hborgs.add(row[0].toString());
					}
				}
			}
			//��ʱȥ��
//			bufSQL.append(" where alone_id in (").append(strCond).append(")");
			
			bufSQL.append("select ").append(bufCol.toString()).append(" from ").append(
					pubData.getKeyGroup().getTableName());
			if(StringUtil.isEmptyWithTrim(strCond))
			{
				bufSQL.append(" where 1=0");
			}else {
				bufSQL.append(" where ").append(StringConnectUtil.getInSqlGroupByArr(hborgs.toArray(new String[0]), "keyword1")).append(" and ver="+hbSchemeVO.getVersion());
				// ���ó�����λ���ؼ���֮��������ؼ���ֵ����
				String keyCond = getKeywordCond(pubData, pubData.getKeyGroup());
				if (strCond.length() > 0 && keyCond.length() > 0) {
					bufSQL.append(" and " +keyCond);
				}
			}
			
			bufSQL.append(" UNION ALL  ");
			
			bufSQL.append("select ").append(bufCol.toString()).append(" from ").append(
					pubData.getKeyGroup().getTableName());
			if(StringUtil.isEmptyWithTrim(strCond))
			{
				bufSQL.append(" where 1=0");
			}else {
				bufSQL.append(" where ").append(StringConnectUtil.getInSqlGroupByArr(gborgs.toArray(new String[0]), "keyword1")).append(" and ver="+0);
				// ���ó�����λ���ؼ���֮��������ؼ���ֵ����
				String keyCond = getKeywordCond(pubData, pubData.getKeyGroup());
				if (strCond.length() > 0 && keyCond.length() > 0) {
					bufSQL.append(" and " +keyCond);
				}
			}
			bufSQL.append(" )");
			stmt = con.createStatement();
			stmt.executeUpdate(bufSQL.toString());

			return strTable;
		} finally {
			if (stmt != null)
				stmt.close();
			if (con != null)
				con.close();
		}
	}
	
	private String getKeywordCond(MeasurePubDataVO pubData, KeyGroupVO keyGroup) {
		KeyVO[] keys = keyGroup.getKeys();
		StringBuffer sb = new StringBuffer();
		for (int i = 1; keys != null && i <= keys.length; i++) {
			String timeCode = null;
			if(keys[i-1].getPk_keyword().equals(KeyVO.CORP_PK)){
				continue;
			}
			if (keys[i-1].isTTimeKeyVO()){
				String strInputDate=pubData.getInputDate();
				if (strInputDate==null || strInputDate.trim().length()<=0)
					continue;

				if (keys[i-1].isTimeKeyVO() && nc.vo.iufo.pub.date.UFODate.NONE_DATE.equals(strInputDate))
					continue;
			}

			if (keys[i-1].isTTimeKeyVO()){
				if (sb.length() > 0) {
					sb.append(" and ");
				}

				timeCode = MeasurePubdataSqlUtil.makeTimeCodeCond4Time(i-1,keyGroup, pubData,"t"+i,new ArrayList<String>());

				if (timeCode==null){
//					sb.append("t0.keyword"+i+"='"+pubData.getInputDate()+"'");
					sb.append("keyword"+i+"='"+pubData.getInputDate()+"'");
				}else{
					sb.append(timeCode);
				}
			}else{
				String keyVal = pubData.getKeywordByPK(keys[i-1].getPk_keyword());
				if (keyVal != null && keyVal.length()>0) {
					if (sb.length() > 0)
						sb.append(" and ");
//					sb.append("t0.keyword" + i + "='" + convertSQL(keyVal) + "' ");
					sb.append("keyword" + i + "='" + convertSQL(keyVal) + "' ");
				}
			}
		}
		return sb.toString();
	}

	/**
	 * ����ʱ�����ݻ�������SQL��䡢ָ�����ݱ����ܽ������MeasurePubDataVO������ؼ�����ϡ��ӱ�ؼ������,�õ���Ҫ���ɻ��ܽ����MeasurePubDataVO����
	 * @param strCond����������SQL���
	 * @param pubData�����ܽ�������MeasurePubDataVO
	 * @param mainKeyGroup������ؼ������
	 * @param subKeyGroup���ӱ�ؼ������
	 * @param strDBTable��ָ�����ݱ�
	 * @return
	 */
	public MeasurePubDataVO[] loadAllTotalPubDatas(String strCond,
			MeasurePubDataVO pubData, KeyGroupVO mainKeyGroup,
			KeyGroupVO subKeyGroup, String strDBTable)
			throws java.sql.SQLException {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet set = null;
		try {
			Vector<MeasurePubDataVO> vRetData = new Vector<MeasurePubDataVO>();
			con = getConnection();

			//�ж��Ƿ����ӱ���ʱ��ؼ��֣��ӱ�ؼ����Ƿ����ܡ���
			boolean bSubTime = false;
			boolean bWeekTime = false;
			boolean bDayTime = false;
			String strMainTime = mainKeyGroup.getTTimeProp();
			String strSubTime = subKeyGroup.getTTimeProp();
			if (strMainTime.equals(nc.vo.iufo.pub.date.UFODate.NONE_PERIOD) == false
					&& strSubTime.equals(nc.vo.iufo.pub.date.UFODate.NONE_PERIOD) == false
					&& strMainTime.equals(strSubTime) == false) {
				bSubTime = true;
				bWeekTime = UFODate.WEEK_PERIOD.equals(subKeyGroup.getTTimeProp());
				bDayTime =UFODate.DAY_PERIOD.equals(subKeyGroup.getTTimeProp());
			}

			//���ӱ���ͬ�ؼ��֣����ڸ��Թؼ�������е�λ�ÿ��ܲ���ͬ������������Ķ�Ӧ��ϵ
			Hashtable<Integer, Integer> hashPos = new Hashtable<Integer, Integer>();
			KeyVO[] mainKeys = mainKeyGroup.getKeys();
			KeyVO[] subKeys = subKeyGroup.getKeys();
			for (int i = 0; i < subKeys.length; i++) {
				for (int j = 0; j < mainKeys.length; j++) {
					if (mainKeys[j].equals(subKeys[i])) {
						hashPos.put(Integer.valueOf(i), Integer.valueOf(j));
						break;
					}
				}
			}

			//���ҵ��ֶε����
			StringBuffer bufItem = new StringBuffer();

			//����Ҫ��ȡ�������õ���
			for (int i = 0; i < subKeys.length; i++) {
				// �ж��Ƿ��ӱ��еĹؼ��������д��ڣ�������ڣ�������MeasurePubDataVO�Ѿ����ڸùؼ���ֵ�����ô����ݿ��ж�ȡ
				int iMainPos = -1;
				if (hashPos.get(Integer.valueOf(i)) != null)
					iMainPos = hashPos.get(Integer.valueOf(i)).intValue();

				if (iMainPos >= 0&& pubData.getKeywordByIndex(iMainPos + 1) != null)
					continue;

				if (bSubTime == false || subKeys[i].getTTimeKeyIndex() < 0)
					bufItem.append("t1.keyword" + (i + 1) + ",");
				else {
					// �����ӱ�Ϊʱ��ؼ���
					// ����select��Ĳ�ѯ�ֶ� (t3.time_week������)
					bufItem.append(getSubTimeKeySql(mainKeyGroup, subKeyGroup,
							bWeekTime, bDayTime, subKeys, i));
				}
			}


			if(bufItem.length() == 0)
				return new MeasurePubDataVO[0];

			//ȥ�������ֶ��������һ������
			bufItem.delete(bufItem.length() - 1, bufItem.length());

			//���ҵ�SQL���
			StringBuffer bufSQL = new StringBuffer();
			bufSQL.append(getAllTotalPubDatasSelectSql(strCond, mainKeyGroup, subKeyGroup, strDBTable,
					bSubTime, hashPos, bufItem));

			stmt = con.prepareStatement(bufSQL.toString());
			set = stmt.executeQuery();

			//*****************************************************
			//*****************************************************
			//���˵õ��ӱ�Ĺؼ��ֵ�ֵ������ӱ�����ʱ��ؼ��֣��򷵻�{0920��0921��0922����}��ֵ
			//*****************************************************
			//*****************************************************
			//��һ�����������յĻ��ܽ����MeasurePubDataVO
			while (set.next()) {
				MeasurePubDataVO onePubData = new MeasurePubDataVO();
				onePubData.setKeyGroup(subKeyGroup);
				onePubData.setAccSchemePK(pubData.getAccSchemePK());//added by liulp,2008-06-13
				onePubData.setKType(subKeyGroup.getKeyGroupPK());

				//���û��ָ�����ݱ���ʾֻ����Ҫ�����Ƿ���Ҫ���ɻ��ܽ����MeasurePubDataVO��¼��ֱ�ӷ���һ����¼����
				if (strDBTable == null)
					return new MeasurePubDataVO[] { onePubData };

				int iValidPos = 1;
				boolean bAddRec=true;
				for (int i = 0; i < subKeys.length; i++) {
					int iMainPos = -1;
					if (hashPos.get(Integer.valueOf(i)) != null)
						iMainPos = hashPos.get(Integer.valueOf(i)).intValue();

					// ���������еĹؼ��֣�����MeasurPubdataVO�Ѿ��д˹ؼ���ֵ�ģ�ֱ�Ӵ���MeasurePubDataVO��ȡ
					if (iMainPos >= 0 && pubData.getKeywordByIndex(iMainPos + 1) != null) {
						onePubData.setKeywordByIndex(i + 1, pubData.getKeywordByIndex(iMainPos + 1));
					}
					else {
						//�������Ȼ�ڼ�ؼ�����strKeyVal���ظ�ʽ0000-00-00
						String strKeyVal = set.getString(iValidPos++);
						if (strKeyVal != null && (strKeyVal.trim().length() <= 0 || strKeyVal.trim().equalsIgnoreCase("null") == true))
							strKeyVal = null;

						// @edit by wuyongc at 2013-6-8,����10:04:24 This method uses the same code to implement two branches of a conditional branch
						// ���ӱ�ʱ��ؼ��֣�ֱ����ֵ
//						if (bSubTime == false || subKeys[i].getTTimeKeyIndex() < 0)
//							onePubData.setKeywordByIndex(i + 1, strKeyVal);
//						else {
//							onePubData.setKeywordByIndex(i + 1, strKeyVal);
//						}
						
						onePubData.setKeywordByIndex(i + 1, strKeyVal);
					}
				}
				if (bAddRec)
					vRetData.add(onePubData);
			}
			return vRetData.toArray(new MeasurePubDataVO[0]);
		} finally {
			if (set != null)
				try {
					set.close();
				} catch (SQLException e) {
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
				}
			}
		}
	}
	
	/**
	 * �����ֱ�ʱ��ؼ���
	 * �ؼ���Ϊ�շ��� select t3.time_month || t3.time_day
	 * @create by jiaah at 2011-9-19,����9:04:02
	 * @param mainKeyGroup
	 * @param subKeyGroup
	 * @param bWeekTime
	 * @param bDayTime
	 * @param subKeys
	 * @param bufItem
	 * @param i
	 */
	private String getSubTimeKeySql(KeyGroupVO mainKeyGroup,
			KeyGroupVO subKeyGroup, boolean bWeekTime, boolean bDayTime,
			KeyVO[] subKeys, int i) {
		StringBuffer bufItem = new StringBuffer();
		int isubIndex = subKeyGroup.getIndexByKeywordPK(subKeyGroup.getTTimeKey().getPk_keyword()) + 1;
		//����ʱ��ؼ��ֵľ���ֵ
		bufItem.append(" t1.keyword" + isubIndex + ",");
//		if(bWeekTime){
//		}
//		else{
//			//�ա�Ѯ
//			if (getDBType().equals(IDatabaseType.DATABASE_SQLSERVER)) {
//				// bufItem.append(" t3.time_month + t3.time_day,");//91����Ϊ0901�ĸ�ʽ
//				bufItem.append("(case when len(t3.time_month) = '1' then '0' + t3.time_month else substring(t3.time_month, 1, length(t3.time_month)) end) +");
//				bufItem.append("(case when len(t3.time_day) = '1' then '0' + t3.time_day else substring(t3.time_day, 1, length(t3.time_day)) end) ,");
//			} else {
//				// bufItem.append(" t3.time_month||t3.time_day,");//91����Ϊ0901�ĸ�ʽ
//				bufItem.append("(case when length(t3.time_month) = '1' then '0' || t3.time_month else substr(t3.time_month, 1, length(t3.time_month)) end) ||");
//				bufItem.append("(case when length(t3.time_day) = '1' then '0' || t3.time_day else substr(t3.time_day, 1, length(t3.time_day)) end) ,");
//			}
//		}
		return bufItem.toString();
	}
	
	/**
	 * �������л��ܱ��PubData��select���
	 * @create by jiaah at 2011-9-19,����10:15:39
	 * @param strCond
	 * @param mainKeyGroup
	 * @param subKeyGroup
	 * @param strDBTable
	 * @param bSubTime
	 * @param hashPos
	 * @param bufItem
	 * @param bufSQL
	 */
	private String getAllTotalPubDatasSelectSql(String strCond, KeyGroupVO mainKeyGroup,
			KeyGroupVO subKeyGroup, String strDBTable, boolean bSubTime,
			Hashtable<Integer, Integer> hashPos, StringBuffer bufItem
			) {
		StringBuffer bufSQL = new StringBuffer();
		bufSQL.append("select ");

		//�������һ��ָ�����ݱ����ҳ����ļ�¼�������ظ��ģ�ʹ��distinct
		if (strDBTable != null)
			bufSQL.append(" distinct ");

		bufSQL.append(bufItem);
		bufSQL.append(" from ");
		bufSQL.append(MeasureDataUtil.getMeasurePubTableName(subKeyGroup.getKeyGroupPK())+ " t1,");//t1 �ӱ�

		//������ӱ���ͬʱ��ʱ��ؼ��֣���iufo_measure_pubdata��ֱ�ӺͲ�ѯ������ͨ��Alone_id��������
		bufSQL.append(strCond + " t2");//t2  ����

		//������ӱ����ʱ��ؼ��֣�������ʱ���iufo_keydetai_time��������keyval���ֱ��ʱ��ؼ��������
		if(bSubTime)
			bufSQL.append(",iufo_keydetail_time t3");

		if (strDBTable != null) {
			bufSQL.append("," + strDBTable + " t4");
		}

		//���»�ȡwhere����
		//�°汾�Ѿ�����ͬ�Ĺؼ�����ϵ����ݷ��ڲ�ͬ�ı��У����Ա����޹ؼ����ֶ�
		bufSQL.append(" where t1.ver=t2.ver ");

		//�������ӱ������λ�ò���ͬ��Ҫ����λ�ü�Ķ�Ӧ��ϵ������������
		Enumeration<Integer> enumPos = hashPos.keys();
		while (enumPos.hasMoreElements()) {
			Integer iSubKeyPos = enumPos.nextElement();
			Integer iMainKeyPos = hashPos.get(iSubKeyPos);
			bufSQL.append(" and t1.keyword" + (iSubKeyPos.intValue() + 1)+ " = t2.keyword" + (iMainKeyPos.intValue() + 1));
		}

		if (bSubTime) {
			// ����ʱ���������
			// ʱ��ؼ�������keyword2
			int iIndex = subKeyGroup.getIndexByKeywordPK(subKeyGroup.getTTimeKey().getPk_keyword()) + 1;
			// �ӱ��ʱ����������
			bufSQL.append(" and t1.keyword" + iIndex + " = t3.keyval ");
			// ȷ�������ͬ
			if (getDBType().equals(IDatabaseType.DATABASE_SQLSERVER)) {
				bufSQL.append(" and substring(t1.keyword" + iIndex + ",1,4) = t3.time_year ");
			} else {
				bufSQL.append(" and substr(t1.keyword" + iIndex + ",0,4) = t3.time_year ");
			}
		}

		//�������ָ�����ݱ���Ҫ��ָ�����ݱ���ָ�깫�����ݱ����
		if (strDBTable != null)
			bufSQL.append(" and t4.alone_id = t1.alone_id ");

		bufSQL.append(" group by ");
		bufSQL.append(bufItem);
		return bufSQL.toString();
	}
}
