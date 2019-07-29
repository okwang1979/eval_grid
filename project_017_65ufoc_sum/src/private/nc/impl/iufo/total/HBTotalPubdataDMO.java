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
	 * 汇总时，为了加快汇总速度，将汇总条件应该查找出的iufo_measure_pubdata表中的记录查找出来，
	 * 生成一张同iufo_measure_pubdata表结构完全相同的临时表，将查找出来的记录插入临时表中
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

			// 临时表名随机生成
			String strTable ="iufo_tmp_pub_"+pubData.getKeyGroup().getTableName().substring(pubData.getKeyGroup().getTableName().length()-4);

			// oracle数据库其字符型、数值型对应的类型与其他数据库不相同
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
			//合并的组织
			List<String> hborgs = new ArrayList<String>();
			//个别报表数据
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
			//暂时去掉
//			bufSQL.append(" where alone_id in (").append(strCond).append(")");
			
			bufSQL.append("select ").append(bufCol.toString()).append(" from ").append(
					pubData.getKeyGroup().getTableName());
			if(StringUtil.isEmptyWithTrim(strCond))
			{
				bufSQL.append(" where 1=0");
			}else {
				bufSQL.append(" where ").append(StringConnectUtil.getInSqlGroupByArr(hborgs.toArray(new String[0]), "keyword1")).append(" and ver="+hbSchemeVO.getVersion());
				// 设置除“单位”关键字之外的其他关键字值条件
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
				// 设置除“单位”关键字之外的其他关键字值条件
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
	 * 汇总时，根据汇总条件SQL语句、指标数据表、汇总结果主表MeasurePubDataVO、主表关键字组合、子表关键字组合,得到需要生成汇总结果的MeasurePubDataVO数组
	 * @param strCond，汇总条件SQL语句
	 * @param pubData，汇总结果主表的MeasurePubDataVO
	 * @param mainKeyGroup，主表关键字组合
	 * @param subKeyGroup，子表关键字组合
	 * @param strDBTable，指标数据表
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

			//判断是否主子表都有时间关键字，子表关键字是否有周、日
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

			//主子表相同关键字，其在各自关键字组合中的位置可能不相同，建立两个间的对应关系
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

			//查找的字段的语句
			StringBuffer bufItem = new StringBuffer();

			//生成要读取及分组用的列
			for (int i = 0; i < subKeys.length; i++) {
				// 判断是否子表中的关键字主表中存在，如果存在，且主表MeasurePubDataVO已经存在该关键字值，则不用从数据库中读取
				int iMainPos = -1;
				if (hashPos.get(Integer.valueOf(i)) != null)
					iMainPos = hashPos.get(Integer.valueOf(i)).intValue();

				if (iMainPos >= 0&& pubData.getKeywordByIndex(iMainPos + 1) != null)
					continue;

				if (bSubTime == false || subKeys[i].getTTimeKeyIndex() < 0)
					bufItem.append("t1.keyword" + (i + 1) + ",");
				else {
					// 处理子表为时间关键字
					// 返回select后的查询字段 (t3.time_week或其它)
					bufItem.append(getSubTimeKeySql(mainKeyGroup, subKeyGroup,
							bWeekTime, bDayTime, subKeys, i));
				}
			}


			if(bufItem.length() == 0)
				return new MeasurePubDataVO[0];

			//去掉查找字段语句的最后一个逗号
			bufItem.delete(bufItem.length() - 1, bufItem.length());

			//查找的SQL语句
			StringBuffer bufSQL = new StringBuffer();
			bufSQL.append(getAllTotalPubDatasSelectSql(strCond, mainKeyGroup, subKeyGroup, strDBTable,
					bSubTime, hashPos, bufItem));

			stmt = con.prepareStatement(bufSQL.toString());
			set = stmt.executeQuery();

			//*****************************************************
			//*****************************************************
			//至此得到子表的关键字的值；如果子表是日时间关键字，则返回{0920，0921，0922……}的值
			//*****************************************************
			//*****************************************************
			//下一步：生成最终的汇总结果的MeasurePubDataVO
			while (set.next()) {
				MeasurePubDataVO onePubData = new MeasurePubDataVO();
				onePubData.setKeyGroup(subKeyGroup);
				onePubData.setAccSchemePK(pubData.getAccSchemePK());//added by liulp,2008-06-13
				onePubData.setKType(subKeyGroup.getKeyGroupPK());

				//如果没有指标数据表，表示只是需要查找是否需要生成汇总结果的MeasurePubDataVO记录，直接返回一条记录即可
				if (strDBTable == null)
					return new MeasurePubDataVO[] { onePubData };

				int iValidPos = 1;
				boolean bAddRec=true;
				for (int i = 0; i < subKeys.length; i++) {
					int iMainPos = -1;
					if (hashPos.get(Integer.valueOf(i)) != null)
						iMainPos = hashPos.get(Integer.valueOf(i)).intValue();

					// 主表中已有的关键字，且主MeasurPubdataVO已经有此关键字值的，直接从主MeasurePubDataVO中取
					if (iMainPos >= 0 && pubData.getKeywordByIndex(iMainPos + 1) != null) {
						onePubData.setKeywordByIndex(i + 1, pubData.getKeywordByIndex(iMainPos + 1));
					}
					else {
						//如果是自然期间关键字则strKeyVal返回格式0000-00-00
						String strKeyVal = set.getString(iValidPos++);
						if (strKeyVal != null && (strKeyVal.trim().length() <= 0 || strKeyVal.trim().equalsIgnoreCase("null") == true))
							strKeyVal = null;

						// @edit by wuyongc at 2013-6-8,上午10:04:24 This method uses the same code to implement two branches of a conditional branch
						// 非子表时间关键字，直接设值
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
	 * 处理字表时间关键字
	 * 关键字为日返回 select t3.time_month || t3.time_day
	 * @create by jiaah at 2011-9-19,上午9:04:02
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
		//返回时间关键字的具体值
		bufItem.append(" t1.keyword" + isubIndex + ",");
//		if(bWeekTime){
//		}
//		else{
//			//日、旬
//			if (getDBType().equals(IDatabaseType.DATABASE_SQLSERVER)) {
//				// bufItem.append(" t3.time_month + t3.time_day,");//91补充为0901的格式
//				bufItem.append("(case when len(t3.time_month) = '1' then '0' + t3.time_month else substring(t3.time_month, 1, length(t3.time_month)) end) +");
//				bufItem.append("(case when len(t3.time_day) = '1' then '0' + t3.time_day else substring(t3.time_day, 1, length(t3.time_day)) end) ,");
//			} else {
//				// bufItem.append(" t3.time_month||t3.time_day,");//91补充为0901的格式
//				bufItem.append("(case when length(t3.time_month) = '1' then '0' || t3.time_month else substr(t3.time_month, 1, length(t3.time_month)) end) ||");
//				bufItem.append("(case when length(t3.time_day) = '1' then '0' || t3.time_day else substr(t3.time_day, 1, length(t3.time_day)) end) ,");
//			}
//		}
		return bufItem.toString();
	}
	
	/**
	 * 返回所有汇总表的PubData的select语句
	 * @create by jiaah at 2011-9-19,上午10:15:39
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

		//如果关联一张指标数据表，查找出来的记录可能有重复的，使用distinct
		if (strDBTable != null)
			bufSQL.append(" distinct ");

		bufSQL.append(bufItem);
		bufSQL.append(" from ");
		bufSQL.append(MeasureDataUtil.getMeasurePubTableName(subKeyGroup.getKeyGroupPK())+ " t1,");//t1 子表

		//如果主子表不是同时有时间关键字，则iufo_measure_pubdata表直接和查询条件表通过Alone_id关联即可
		bufSQL.append(strCond + " t2");//t2  主表

		//如果主子表包含时间关键字，则增加时间表iufo_keydetai_time关联表用keyval和字表的时间关键字相关联
		if(bSubTime)
			bufSQL.append(",iufo_keydetail_time t3");

		if (strDBTable != null) {
			bufSQL.append("," + strDBTable + " t4");
		}

		//以下获取where条件
		//新版本已经将不同的关键字组合的数据放在不同的表中，所以表中无关键字字段
		bufSQL.append(" where t1.ver=t2.ver ");

		//由于主子表关联字位置不相同，要根据位置间的对应关系建立关联条件
		Enumeration<Integer> enumPos = hashPos.keys();
		while (enumPos.hasMoreElements()) {
			Integer iSubKeyPos = enumPos.nextElement();
			Integer iMainKeyPos = hashPos.get(iSubKeyPos);
			bufSQL.append(" and t1.keyword" + (iSubKeyPos.intValue() + 1)+ " = t2.keyword" + (iMainKeyPos.intValue() + 1));
		}

		if (bSubTime) {
			// 建立时间关联条件
			// 时间关键字索引keyword2
			int iIndex = subKeyGroup.getIndexByKeywordPK(subKeyGroup.getTTimeKey().getPk_keyword()) + 1;
			// 子表和时间表关联条件
			bufSQL.append(" and t1.keyword" + iIndex + " = t3.keyval ");
			// 确保年份相同
			if (getDBType().equals(IDatabaseType.DATABASE_SQLSERVER)) {
				bufSQL.append(" and substring(t1.keyword" + iIndex + ",1,4) = t3.time_year ");
			} else {
				bufSQL.append(" and substr(t1.keyword" + iIndex + ",0,4) = t3.time_year ");
			}
		}

		//如果还有指标数据表，还要让指标数据表与指标公共数据表关联
		if (strDBTable != null)
			bufSQL.append(" and t4.alone_id = t1.alone_id ");

		bufSQL.append(" group by ");
		bufSQL.append(bufItem);
		return bufSQL.toString();
	}
}
