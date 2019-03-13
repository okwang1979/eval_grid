/**
 *
 */
package nc.impl.iufo.repdataquery;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import nc.bs.dao.DAOException;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.iufo.pub.UfoPublic;
import nc.util.bd.intdata.UFDSSqlUtil;
import nc.util.iufo.pub.UFOString;
import nc.utils.iufo.TaskSrvUtils;
import nc.vo.corg.ReportManaStruMemberVO;
import nc.vo.iufo.check.CheckFormulaVO;
import nc.vo.iufo.check.CheckSchemaVO;
import nc.vo.iufo.checkexecute.CheckExeQueryCondVO;
import nc.vo.iufo.checkexecute.CheckExeResultVO;
import nc.vo.iufo.data.IKeyDetailData;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.pub.IDatabaseNames;
import nc.vo.iufo.pub.date.UFODate;
import nc.vo.iufo.query.IUfoQueryCondVO;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.iufo.task.TaskAssignVO;
import nc.vo.iufo.task.TaskReportVO;

import org.apache.commons.lang.StringUtils;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.iufo.check.vo.CheckConVO;
import com.ufsoft.iufo.check.vo.CheckResultVO;
import com.ufsoft.iufo.fmtplugin.formula.IUfoCheckVO;
import com.ufsoft.script.util.ICheckResultStatus;

/**
 * @author wuyongc
 * @created at 2011-7-12,上午10:47:23
 *
 */
public class CheckExeInfoQueryUtil implements ICheckResultStatus{
	public static String getCheckExeQuerySql(IUfoQueryCondVO queryCond,String[] showColumns,MeasurePubDataVO defaultPubData) throws Exception{


		KeyGroupVO keyGroup=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(queryCond.getKeyGroupPK());
		Set<String> vShowColumn=new HashSet<String>(Arrays.asList(showColumns));
		CheckConVO checkCon = ((CheckExeQueryCondVO)queryCond).getCheckCon();
		String[] strOneRepPKs = null;
		if(checkCon.isTaskCheck()){
			strOneRepPKs = queryCond.getRepPKs();
		}else{
			strOneRepPKs= ((CheckExeQueryCondVO)queryCond).getCheckCon().getReports();
		}

		String strTaskPK = queryCond.getTaskPKs()[0];

		StringBuilder bufOneSQL=getTaskRepCheckExeSQL((CheckExeQueryCondVO)queryCond, keyGroup, defaultPubData, vShowColumn, strTaskPK, strOneRepPKs);
		return bufOneSQL.toString();
	}
	private static StringBuilder getTaskRepCheckExeSQL(CheckExeQueryCondVO queryCond,KeyGroupVO keyGroup,MeasurePubDataVO defaultPubData,
			Set<String> vShowColumn,String strTaskPK,String[] strRepPKs) throws Exception{
		boolean bHasTaskFml=isHasTaskFml(strTaskPK);

		//如果任务上没有审核方案，而查询条件要求审核通过，则直接返回null
		if (!bHasTaskFml && queryCond.getTaskCheckState()>=0 && queryCond.getTaskCheckState()!=CheckResultVO.CHECK_NO_FORMULA)
			return null;
		else if (bHasTaskFml && queryCond.getTaskCheckState()>=0 && queryCond.getTaskCheckState()==CheckResultVO.CHECK_NO_FORMULA)
			return null;

		StringBuilder oneSQL=getOneTaskRepCheckExeSQLByCheckFml(queryCond,true,bHasTaskFml,keyGroup,defaultPubData,vShowColumn,
				strTaskPK,strRepPKs);
		return oneSQL.length()>0?oneSQL:null;
	}

	private static StringBuilder getOneTaskRepCheckExeSQLByCheckFml(CheckExeQueryCondVO queryCond,boolean hasCheckFml,
			boolean bHasTaskCheckFml,KeyGroupVO keyGroup,MeasurePubDataVO defaultPubData,Set<String> vShowColumn,
			String strTaskPK,String[] strRepPKs) throws Exception{
		CheckConVO checkCon = queryCond.getCheckCon();
		if(checkCon.isTaskCheck())
			return getTaskCheckSql(queryCond,hasCheckFml,bHasTaskCheckFml,keyGroup,defaultPubData,vShowColumn,strTaskPK,strRepPKs);
		else if(checkCon.isFmlCheck()){
			//TODO getSchemeFmlCheckSql
			StringBuilder scheme = new StringBuilder();
			boolean bHasSchemeFml = false;
			if(checkCon.getSchemaFmls() != null){
				Iterator<Map.Entry<Object, Vector<Object>>> it = checkCon.getSchemaFmls().entrySet().iterator();
				while(it.hasNext()){
					Map.Entry<Object, Vector<Object>> entry = it.next();
					if(entry.getValue()!= null && entry.getValue().size()>0){
						bHasSchemeFml = true;
						break;
					}
				}
			}

			if(bHasSchemeFml){
				scheme = getSchemeFmlCheckSql(queryCond, hasCheckFml, bHasTaskCheckFml, keyGroup, defaultPubData, vShowColumn, strTaskPK, strRepPKs);
			}
			if(checkCon.getRepFormulas() != null){
				Iterator<Map.Entry<String, Vector<IUfoCheckVO>>> repMapit = checkCon.getRepFormulas().entrySet().iterator();
				boolean bHasRepFml = false;
				while(repMapit.hasNext()){
					Map.Entry<String, Vector<IUfoCheckVO>> entry = repMapit.next();
					if(entry.getValue()!= null && entry.getValue().size()>0){
						bHasRepFml = true;
						break;
					}
				}
				StringBuilder rep = null;
				if(bHasRepFml){
					rep = getRepFmlCheckSql(queryCond,hasCheckFml,bHasTaskCheckFml,keyGroup,defaultPubData,vShowColumn,strTaskPK,strRepPKs);
					if(bHasSchemeFml){
						scheme.append(" union all ").append(rep);
					}else{
						scheme.append(rep);
					}
				}
			}

			return scheme;
		} else
			return getRepSchemeCheckSql(queryCond,hasCheckFml,bHasTaskCheckFml,keyGroup,defaultPubData,vShowColumn,strTaskPK,strRepPKs);
	}
	private static StringBuilder getTaskCheckSql(CheckExeQueryCondVO queryCond,boolean hasCheckFml,
			boolean bHasTaskCheckFml,KeyGroupVO keyGroup,MeasurePubDataVO defaultPubData,Set<String> vShowColumn,
			String strTaskPK,String[] strRepPKs) throws Exception{
		CheckConVO checkCon = queryCond.getCheckCon();
		boolean bHasOrgKey=keyGroup.getKeyByKeyPk(KeyVO.CORP_PK)!=null;
		//要查的表字段
		StringBuilder bufCol=new StringBuilder();
		bufCol.append("t1.alone_id,t1.pk_task,'' pk_report,-1 repcheckstatus ");
		boolean bNeedComma=addKeywordColumn(true,queryCond,bufCol, keyGroup, vShowColumn);

		bNeedComma=addRepSchemeFmlColumn(bNeedComma,bufCol,hasCheckFml);


		bufCol.append(", coalesce(t7.taskcheckstate,"+NOCHECK+") taskcheckstatus,-1 schemecheckstatus");

		bufCol.append(",(select name from iufo_task where pk_task=t1.pk_task) ").append(CheckExeResultVO.CHECKCONTENT);
		bufCol.append(", coalesce(t7.checkstate, 1) checkstatus, ");
		bufCol.append(checkCon.getCheckCategory()).append(" ").append(CheckExeResultVO.CHECKTYPE);
		//生成要查找的表名及关联条件，用left outer join on关联
		StringBuilder bufFrom=new StringBuilder();
		bufFrom.append("("+getKeycondTable(queryCond, vShowColumn,defaultPubData, keyGroup, strTaskPK,strRepPKs)+") t1 ");

//		boolean bRepCheckTable=hasCheckFml?addRepCheckTable(bufFrom, queryCond, vShowColumn):false;
		bHasTaskCheckFml = true;
		//TODO  没有审核公式 如何处理。。。。。。。。。。。。。。。。。。。。
		boolean bTaskCheckTable=bHasTaskCheckFml?addTaskCheckTable(bufFrom,hasCheckFml, queryCond, vShowColumn,strTaskPK,strRepPKs):false;
		boolean bTaskRepTable=addTaskRepTable(bufFrom,queryCond,vShowColumn);
		boolean bTaskAssignTable=bHasOrgKey?addTaskAssignTable(bufFrom,queryCond,vShowColumn):false;

		//生成筛选条件
		StringBuilder bufWhere=new StringBuilder();
		boolean bNeedAnd=false;
//		bNeedAnd=bRepCheckTable?addRepCheckWhereCond(bNeedAnd, bufWhere, queryCond,null):bNeedAnd;
		bNeedAnd=bTaskCheckTable?addTaskCheckWhereCond(bNeedAnd, bufWhere, queryCond,null):bNeedAnd;
		bNeedAnd=bTaskRepTable?addTaskRepWhereCond(bNeedAnd, bufWhere, queryCond):bNeedAnd;
		bNeedAnd=bTaskAssignTable?addTaskAssignWhereCond(bNeedAnd, bufWhere, queryCond,null):bNeedAnd;

		//拼装语句
		StringBuilder bufSQL=new StringBuilder();
		bufSQL.append("select distinct ").append(bufCol);
		bufSQL.append(" from ").append(bufFrom);
		if (bufWhere.length()>0){
			StringBuilder bufSQL1=new StringBuilder("select * from (").append(bufSQL).append(")");
			bufSQL1.append(" where ").append(bufWhere);
			bufSQL=bufSQL1;
		}

		return bufSQL;
	}
	private static StringBuilder getRepFmlCheckSql(CheckExeQueryCondVO queryCond,boolean hasCheckFml,
			boolean bHasTaskCheckFml,KeyGroupVO keyGroup,MeasurePubDataVO defaultPubData,Set<String> vShowColumn,
			String strTaskPK,String[] strRepPKs) throws Exception{
		CheckConVO checkCon = queryCond.getCheckCon();
		boolean bHasOrgKey=keyGroup.getKeyByKeyPk(KeyVO.CORP_PK)!=null;
		//要查的表字段
		StringBuilder bufCol=new StringBuilder();
		bufCol.append("t1.alone_id,t1.pk_task, t7.pk_report ");
		boolean bNeedComma=addKeywordColumn(true,queryCond,bufCol, keyGroup, vShowColumn);

		bNeedComma=addRepSchemeFmlColumn(bNeedComma,bufCol,hasCheckFml);

		bufCol.append(", -1 taskcheckstatus, repcheckstatus,");
		if(hasCheckFml){
			bufCol.append("t7." + CheckExeResultVO.SCHEMECHECKSTATUS);

		}else{
			bufCol.append(CheckResultVO.CHECK_NO_FORMULA + " " + CheckExeResultVO.SCHEMECHECKSTATUS);
		}
		bufCol.append("," + CheckExeResultVO.CHECKCONTENT);

		bufCol.append(", coalesce(t7.checkstate, 1) checkstatus, ");
		bufCol.append("t7.checkperson,t7.checktime,");
		bufCol.append(checkCon.getCheckCategory()).append(" ").append(CheckExeResultVO.CHECKTYPE);
		//生成要查找的表名及关联条件，用left outer join on关联
		StringBuilder bufFrom=new StringBuilder();
		bufFrom.append("("+getKeycondTable(queryCond, vShowColumn,defaultPubData, keyGroup, strTaskPK,strRepPKs)+") t1 ");

//		boolean bRepCheckTable=hasCheckFml?addRepCheckTable(bufFrom, queryCond, vShowColumn):false;
		bHasTaskCheckFml = true;
		//TODO  没有审核公式 如何处理。。。。。。。。。。。。。。。。。。。。
		boolean bTaskCheckTable=bHasTaskCheckFml?addRepFmlCheckTable(bufFrom,hasCheckFml, queryCond, vShowColumn,strTaskPK,strRepPKs):false;
		boolean bTaskRepTable=addTaskRepTable(bufFrom,queryCond,vShowColumn);
		boolean bTaskAssignTable=bHasOrgKey?addTaskAssignTable(bufFrom,queryCond,vShowColumn):false;

		//生成筛选条件
		StringBuilder bufWhere=new StringBuilder();
		boolean bNeedAnd=false;
//		bNeedAnd=bRepCheckTable?addRepCheckWhereCond(bNeedAnd, bufWhere, queryCond,null):bNeedAnd;
		bNeedAnd=bTaskCheckTable?addTaskCheckWhereCond(bNeedAnd, bufWhere, queryCond,null):bNeedAnd;
		bNeedAnd=bTaskRepTable?addTaskRepWhereCond(bNeedAnd, bufWhere, queryCond):bNeedAnd;
		bNeedAnd=bTaskAssignTable?addTaskAssignWhereCond(bNeedAnd, bufWhere, queryCond,null):bNeedAnd;

		//拼装语句
		StringBuilder bufSQL=new StringBuilder();
		bufSQL.append("select distinct ").append(bufCol);
		bufSQL.append(" from ").append(bufFrom);
		if (bufWhere.length()>0){
			StringBuilder bufSQL1=new StringBuilder("select * from (").append(bufSQL).append(")");
			bufSQL1.append(" where ").append(bufWhere);
			bufSQL=bufSQL1;
		}

		return bufSQL;
	}

	private static StringBuilder getSchemeFmlCheckSql(CheckExeQueryCondVO queryCond,boolean hasCheckFml,
			boolean bHasTaskCheckFml,KeyGroupVO keyGroup,MeasurePubDataVO defaultPubData,Set<String> vShowColumn,
			String strTaskPK,String[] strRepPKs) throws Exception{
		CheckConVO checkCon = queryCond.getCheckCon();
		boolean bHasOrgKey=keyGroup.getKeyByKeyPk(KeyVO.CORP_PK)!=null;
		//要查的表字段
		StringBuilder bufCol=new StringBuilder();
		bufCol.append("t1.alone_id,t1.pk_task, '' pk_report ");
		boolean bNeedComma=addKeywordColumn(true,queryCond,bufCol, keyGroup, vShowColumn);

		bNeedComma=addRepSchemeFmlColumn(bNeedComma,bufCol,hasCheckFml);

		bufCol.append(", -1 taskcheckstatus, -1 repcheckstatus,");
		if(hasCheckFml){
			bufCol.append("t7." + CheckExeResultVO.SCHEMECHECKSTATUS);

		}else{
			bufCol.append(CheckResultVO.CHECK_NO_FORMULA + " " + CheckExeResultVO.SCHEMECHECKSTATUS);
		}
		bufCol.append("," + CheckExeResultVO.CHECKCONTENT);

		bufCol.append(", coalesce(t7.checkstate, 1) checkstatus, ");
		bufCol.append("t7.checkperson,t7.checktime,");
		bufCol.append(checkCon.getCheckCategory()).append(" ").append(CheckExeResultVO.CHECKTYPE);
		//生成要查找的表名及关联条件，用left outer join on关联
		StringBuilder bufFrom=new StringBuilder();
		bufFrom.append("("+getSchemeCheckKeycondTable(queryCond, vShowColumn,defaultPubData, keyGroup, strTaskPK,strRepPKs)+") t1 ");

//		boolean bRepCheckTable=hasCheckFml?addRepCheckTable(bufFrom, queryCond, vShowColumn):false;
		bHasTaskCheckFml = true;
		//TODO  没有审核公式 如何处理。。。。。。。。。。。。。。。。。。。。
		boolean bTaskCheckTable=bHasTaskCheckFml?addSchemeFmlCheckTable(bufFrom,hasCheckFml, queryCond, vShowColumn,strTaskPK,strRepPKs):false;
		boolean bTaskRepTable=addTaskRepTable(bufFrom,queryCond,vShowColumn);
		boolean bTaskAssignTable=bHasOrgKey?addTaskAssignTable(bufFrom,queryCond,vShowColumn):false;

		//生成筛选条件
		StringBuilder bufWhere=new StringBuilder();
		boolean bNeedAnd=false;
//		bNeedAnd=bRepCheckTable?addRepCheckWhereCond(bNeedAnd, bufWhere, queryCond,null):bNeedAnd;
		bNeedAnd=bTaskCheckTable?addTaskCheckWhereCond(bNeedAnd, bufWhere, queryCond,null):bNeedAnd;
		bNeedAnd=bTaskRepTable?addTaskRepWhereCond(bNeedAnd, bufWhere, queryCond):bNeedAnd;
		bNeedAnd=bTaskAssignTable?addTaskAssignWhereCond(bNeedAnd, bufWhere, queryCond,null):bNeedAnd;

		//拼装语句
		StringBuilder bufSQL=new StringBuilder();
		bufSQL.append("select distinct ").append(bufCol);
		bufSQL.append(" from ").append(bufFrom);
		if (bufWhere.length()>0){
			StringBuilder bufSQL1=new StringBuilder("select * from (").append(bufSQL).append(")");
			bufSQL1.append(" where ").append(bufWhere);
			bufSQL=bufSQL1;
		}

		return bufSQL;
	}

	private static StringBuilder getRepSchemeCheckSql(CheckExeQueryCondVO queryCond,boolean hasCheckFml,
			boolean bHasTaskCheckFml,KeyGroupVO keyGroup,MeasurePubDataVO defaultPubData,Set<String> vShowColumn,
			String strTaskPK,String[] strRepPKs) throws Exception{
		CheckConVO checkCon = queryCond.getCheckCon();
		boolean bHasOrgKey=keyGroup.getKeyByKeyPk(KeyVO.CORP_PK)!=null;
		//要查的表字段
		StringBuilder bufCol=new StringBuilder();

		bufCol.append("t1.alone_id,t1.pk_task,");
		if(strRepPKs == null || strRepPKs.length == 0){
			bufCol.append(" '' pk_report");
		}else{
			bufCol.append("case t7.pk_scheme when t7.pk_scheme then '' else coalesce(t7.pk_report, t1.pk_report) end pk_report");
		}


		bufCol.append(" ,coalesce(repcheckstatus,1) repcheckstatus ");
		boolean bNeedComma=addKeywordColumn(true,queryCond,bufCol, keyGroup, vShowColumn);

		bNeedComma=addRepSchemeFmlColumn(bNeedComma,bufCol,hasCheckFml);

		bufCol.append(", -1 taskcheckstatus,case t7.pk_scheme when t7.pk_scheme then  t7.schemecheckstatus else -1 end schemecheckstatus ");

		if(strRepPKs == null || strRepPKs.length == 0){
			bufCol.append(", checkcontent ");
		}else{
			bufCol.append(",coalesce(checkcontent,t1.name) ");
		}

		bufCol.append(CheckExeResultVO.CHECKCONTENT);

		bufCol.append(", coalesce(t7.checkstate, 1) checkstatus, ");
		bufCol.append("t7.checkperson, t7.checktime,");
		bufCol.append(checkCon.getCheckCategory()).append(" ").append(CheckExeResultVO.CHECKTYPE);
		// 审核方式
		bNeedComma = addOneColumn(true, bufCol, checkCon.getCheckCategory()+ " " +CheckExeResultVO.CHECKTYPE);
		//生成要查找的表名及关联条件，用left outer join on关联
		StringBuilder bufFrom=new StringBuilder();
		bufFrom.append("("+getKeycondTable(queryCond, vShowColumn,defaultPubData, keyGroup, strTaskPK,strRepPKs)+") t1 ");

//		boolean bRepCheckTable=hasCheckFml?addRepCheckTable(bufFrom, queryCond, vShowColumn):false;
		bHasTaskCheckFml = true;
		//TODO  没有审核公式 如何处理。。。。。。。。。。。。。。。。。。。。
		boolean bTaskCheckTable=bHasTaskCheckFml?addRepSchemeCheckTable(bufFrom,hasCheckFml, queryCond, vShowColumn,strTaskPK,strRepPKs):false;
		boolean bTaskRepTable=addTaskRepTable(bufFrom,queryCond,vShowColumn);
		boolean bTaskAssignTable=bHasOrgKey?addTaskAssignTable(bufFrom,queryCond,vShowColumn):false;

		//生成筛选条件
		StringBuilder bufWhere=new StringBuilder();
		boolean bNeedAnd=false;
//		bNeedAnd=bRepCheckTable?addRepCheckWhereCond(bNeedAnd, bufWhere, queryCond,null):bNeedAnd;
		bNeedAnd=bTaskCheckTable?addTaskCheckWhereCond(bNeedAnd, bufWhere, queryCond,null):bNeedAnd;
		bNeedAnd=bTaskRepTable?addTaskRepWhereCond(bNeedAnd, bufWhere, queryCond):bNeedAnd;
		bNeedAnd=bTaskAssignTable?addTaskAssignWhereCond(bNeedAnd, bufWhere, queryCond,null):bNeedAnd;

		//拼装语句
		StringBuilder bufSQL=new StringBuilder();
		bufSQL.append("select distinct ").append(bufCol);
		bufSQL.append(" from ").append(bufFrom);
		if (bufWhere.length()>0){
			StringBuilder bufSQL1=new StringBuilder("select * from (").append(bufSQL).append(")");
			bufSQL1.append(" where ").append(bufWhere);
			bufSQL=bufSQL1;
		}

		return bufSQL;
	}
	private static boolean addTaskRepWhereCond(boolean bNeedAnd,StringBuilder bufSQL,IUfoQueryCondVO queryCond){
		if (queryCond.getMustInputFlag()>IUfoQueryCondVO.INT_EMPTY_SELECT)
			bNeedAnd=addOneCondSQL(bNeedAnd, bufSQL, RepDataQueryResultVO.MUSINPUTFLAG+"="+int2BoolChar(queryCond.getTaskCheckState()));

		if (queryCond.getMustCommitFlag()>IUfoQueryCondVO.INT_EMPTY_SELECT)
			bNeedAnd=addOneCondSQL(bNeedAnd, bufSQL, RepDataQueryResultVO.MUSCOMMFLAG+"="+int2BoolChar(queryCond.getTaskCheckState()));

		return bNeedAnd;
	}


	public static StringBuilder getSchemeCheckKeycondTable(IUfoQueryCondVO queryCond,Set<String> vShowColumn,MeasurePubDataVO defaultPubData
			,KeyGroupVO keyGroup,String strTaskPK,String[] strRepPKs) throws DAOException{
		StringBuilder bufSQL=new StringBuilder("select t1.alone_id,'"+strTaskPK+"' pk_task,");

		KeyVO[] keys=keyGroup.getKeys();
		for (int i=0;i<keys.length;i++){
			int iIndex=i+1;
			if (defaultPubData==null){
				bufSQL.append("t1.keyword"+iIndex+",");
				if (keys[i].getPk_keyword().equals(KeyVO.CORP_PK) ||keys[i].isTTimeKeyVO() || keys[i].getDetailtable()==null || !vShowColumn.contains("keyval"+iIndex))
					bufSQL.append("t1.keyword"+iIndex);
				else
					bufSQL.append("t2"+iIndex+".name1");
				bufSQL.append(" keyval"+iIndex+",");
			}else{
				if (keys[i].getPk_keyword().equals(KeyVO.CORP_PK)){
					bufSQL.append("t3.pk_org keyword1,");
					bufSQL.append("t3.pk_org keyval1,");
				}else{
					IKeyDetailData keyData=defaultPubData.getKeyDataByIndex(iIndex);
					bufSQL.append("'"+keyData.getValue()+"' keyword"+iIndex+",");
					bufSQL.append("'"+keyData.getMultiLangText()+"' keyval"+iIndex+",");
				}
			}
		}
		if(bufSQL.toString().endsWith(",")){
			bufSQL.deleteCharAt(bufSQL.length()-1);
		}
		bufSQL.append(" from ");

		StringBuilder orgSQL=null;
		if (keyGroup.getKeyByKeyPk(KeyVO.CORP_PK)!=null){
			orgSQL=getOrgCondSQL(queryCond,strTaskPK);
			if (defaultPubData!=null){
				bufSQL.append("(").append(orgSQL).append(") t3 left outer join ");
			}
		}
		bufSQL.append(keyGroup.getTableName()+" t1");
//		bufSQL.append(" on t1.ver=0 ");
		if (orgSQL!=null && defaultPubData!=null){
			bufSQL.append(" on t1.ver=0 and t3.pk_org=t1.keyword1");
			innerAddKeyCond(true,bufSQL, queryCond, keyGroup);
		}
//		bufSQL.append(" inner join iufo_report t2");
//		bufSQL.append(" on t2.pk_report in"+UFOString.getSqlStrByArr(strRepPKs));

		if (defaultPubData==null){
			for (int i=0;i<keys.length;i++){
				int iIndex=i+1;
				if (keys[i].getPk_keyword().equals(KeyVO.CORP_PK) || keys[i].isTTimeKeyVO())
					continue;

				if (keys[i].getDetailtable()!=null && vShowColumn.contains("keyval"+iIndex)){
					bufSQL.append(" inner join "+keys[i].getDetailtable()+" t2"+iIndex);
					bufSQL.append(" on t1.keyword"+iIndex+"=t2"+iIndex+".keyval");
				}
			}
		}

		if (orgSQL==null || defaultPubData==null){
			bufSQL.append(" where ");
			if (orgSQL!=null)
				bufSQL.append("t1.keyword1 in(").append(orgSQL).append(")");
			innerAddKeyCond(orgSQL!=null,bufSQL, queryCond, keyGroup);
		}

		return bufSQL;
	}

	private static StringBuilder getKeycondTable(IUfoQueryCondVO queryCond,Set<String> vShowColumn,MeasurePubDataVO defaultPubData
			,KeyGroupVO keyGroup,String strTaskPK,String[] strRepPKs) throws DAOException{
		StringBuilder bufSQL=new StringBuilder("select t1.alone_id,'"+strTaskPK+"' pk_task,");

		KeyVO[] keys=keyGroup.getKeys();
		for (int i=0;i<keys.length;i++){
			int iIndex=i+1;
			if (defaultPubData==null){
				bufSQL.append("t1.keyword"+iIndex+",");
				if (keys[i].getPk_keyword().equals(KeyVO.CORP_PK) ||keys[i].isTTimeKeyVO() || keys[i].getDetailtable()==null || !vShowColumn.contains("keyval"+iIndex))
					bufSQL.append("t1.keyword"+iIndex);
				else
					bufSQL.append("t2"+iIndex+".name1");
				bufSQL.append(" keyval"+iIndex+",");
			}else{
				if (keys[i].getPk_keyword().equals(KeyVO.CORP_PK)){
					bufSQL.append("t3.pk_org keyword1,");
					bufSQL.append("t3.pk_org keyval1,");
				}else{
					IKeyDetailData keyData=defaultPubData.getKeyDataByIndex(iIndex);
					bufSQL.append("'"+keyData.getValue()+"' keyword"+iIndex+",");
					bufSQL.append("'"+keyData.getMultiLangText()+"' keyval"+iIndex+",");
				}
			}
		}
		if(strRepPKs != null && strRepPKs.length > 0){
			bufSQL.append("t2.pk_report,t2.name ");
		}else{
			if(bufSQL.toString().endsWith(","))
				bufSQL.deleteCharAt(bufSQL.length()-1);
		}
		bufSQL.append(" from ");

		StringBuilder orgSQL=null;
		if (keyGroup.getKeyByKeyPk(KeyVO.CORP_PK)!=null){
			orgSQL=getOrgCondSQL(queryCond,strTaskPK);
			if (defaultPubData!=null){
				bufSQL.append("(").append(orgSQL).append(") t3 left outer join ");
			}
		}
		bufSQL.append(keyGroup.getTableName()+" t1");
//		bufSQL.append(" on t1.ver=0 ");
		if (orgSQL!=null && defaultPubData!=null){
			bufSQL.append(" on t1.ver=0 and t3.pk_org=t1.keyword1");
			innerAddKeyCond(true,bufSQL, queryCond, keyGroup);
		}
		if(strRepPKs != null && strRepPKs.length > 0){
			bufSQL.append(" inner join iufo_report t2");
			bufSQL.append(" on t2.pk_report in"+UFOString.getSqlStrByArr(strRepPKs));
		}


		if (defaultPubData==null){
			for (int i=0;i<keys.length;i++){
				int iIndex=i+1;
				if (keys[i].getPk_keyword().equals(KeyVO.CORP_PK) || keys[i].isTTimeKeyVO())
					continue;

				if (keys[i].getDetailtable()!=null && vShowColumn.contains("keyval"+iIndex)){
					bufSQL.append(" inner join "+keys[i].getDetailtable()+" t2"+iIndex);
					bufSQL.append(" on t1.keyword"+iIndex+"=t2"+iIndex+".keyval");
				}
			}
		}

		if (orgSQL==null || defaultPubData==null){
			// @edit by wuyongc at 2013-7-23,上午10:36:00
			bufSQL.append(" where t1.ver=0 and ");
			if (orgSQL!=null)
				bufSQL.append("t1.keyword1 in(").append(orgSQL).append(")");
			innerAddKeyCond(orgSQL!=null,bufSQL, queryCond, keyGroup);
		}

		return bufSQL;
	}

	private static StringBuilder getOrgCondSQL(IUfoQueryCondVO queryCond,String pk_task) throws DAOException{
		StringBuilder bufSQL=new StringBuilder("select distinct pk_org from "+ReportManaStruMemberVO.getDefaultTableName()+" t1 where ");
		String strOrgPK=queryCond.getKeyVal(KeyVO.CORP_PK);
		try {
			String[] orgs = null;
			if (StringUtils.isNotEmpty(strOrgPK)) {
				if (queryCond.getOrgType() == IUfoQueryCondVO.ORGTYPE_SELF) {
					bufSQL.append("pk_org='" + strOrgPK + "'");
					bufSQL.append(" and pk_org in(select PK_RECEIVEORG from iufo_taskassign where pk_task='"
							+ pk_task + "')");
					return bufSQL;
				}
				orgs = queryCond.getSelectedOrgPKs();
			} else {
				orgs = queryCond.getOrgPKs();
			}

			// String orgSqlIn = UFOString.getSqlStrByArr(orgs);
			// bufSQL.append(" pk_org in"+ orgSqlIn);
			String orgSqlIn = UFDSSqlUtil.buildInSql(
					ReportManaStruMemberVO.PK_ORG, orgs);
			bufSQL.append(orgSqlIn);
			bufSQL.append(" and pk_org in(select PK_RECEIVEORG from iufo_taskassign where pk_task='"
					+ pk_task + "')");
		}catch (Exception e) {
			AppDebug.debug(e);
		}
		return bufSQL;
	}

	private static void innerAddKeyCond(boolean bNeedAnd,StringBuilder bufSQL,IUfoQueryCondVO queryCond,KeyGroupVO keyGroup){
		KeyVO timeKey=keyGroup.getTTimeKey();
		int iTimeKeyIndex=timeKey==null?-1:keyGroup.getIndexByKeywordPK(timeKey.getPk_keyword());
		KeyVO[] keys=keyGroup.getKeys();

		if (!UfoPublic.stringIsNull(queryCond.getDate())){
			bNeedAnd=addOneCondSQL(bNeedAnd,bufSQL, "t1.keyword"+(iTimeKeyIndex+1)+"='"+getNatDate(queryCond.getDate(),keyGroup)+"'");
		}else{
			if (!UfoPublic.stringIsNull(queryCond.getBeginDate())){
				bNeedAnd=addOneCondSQL(bNeedAnd,bufSQL, "t1.keyword"+(iTimeKeyIndex+1)+">='"+getNatDate(queryCond.getBeginDate(),keyGroup)+"'");
			}

			if (!UfoPublic.stringIsNull(queryCond.getEndDate())){
				bNeedAnd=addOneCondSQL(bNeedAnd,bufSQL, "t1.keyword"+(iTimeKeyIndex+1)+"<='"+getNatDate(queryCond.getEndDate(),keyGroup)+"'");
			}
		}

		for (int i=0;i<keys.length;i++){
			if (keys[i].getPk_keyword().equals(KeyVO.CORP_PK))
				continue;

			String strKeyVal=queryCond.getKeyVal(keys[i].getPk_keyword());
			if (UfoPublic.stringIsNull(strKeyVal))
				continue;

			bNeedAnd=addOneCondSQL(bNeedAnd,bufSQL, "t1.keyword"+(i+1)+"='"+strKeyVal+"'");
		}
	}

	private static String getNatDate(String strDate,KeyGroupVO keyGroup){
		if (strDate==null || keyGroup.getTimeKey()==null)
			return strDate;

		return new UFODate(strDate).getEndDay(keyGroup.getTimeProp()).toString();
	}

	private static boolean addOneCondSQL(boolean bNeedAnd,StringBuilder bufSQL,String strOneCond){
		if (bNeedAnd){
			bufSQL.append(" and ");
		}
		bufSQL.append(strOneCond);
		return true;
	}

	private static boolean isHasTaskFml(String strTaskPK) throws Exception{
		CheckSchemaVO[] schemes=TaskSrvUtils.getTaskCheckSchemeVOsByTaskId(strTaskPK);
		return schemes!=null && schemes.length>0;
	}


	private static boolean addRepSchemeFmlColumn(boolean bNeedComma,StringBuilder bufSQL,boolean hasCheckFml){
		if(bNeedComma)
			bufSQL.append(",");
		if(hasCheckFml)
			bufSQL.append(" t7.pk_scheme,t7.pk_formula ");
		else
			bufSQL.append("null pk_scheme, '' pk_formula ");
		return true;
	}
	private static boolean addKeywordColumn(boolean bNeedComma,CheckExeQueryCondVO queryCond,StringBuilder bufSQL,KeyGroupVO keyGroup,Set<String> vShowColumn){

		int iCorpIndex=keyGroup.getIndexByKeywordPK(KeyVO.CORP_PK);
		for (int i=0,len=keyGroup.getKeys().length;i<len;i++){
			int iIndex=i+1;
			bNeedComma=addOneColumn(bNeedComma,bufSQL,"t1.keyword"+iIndex);

			if (iCorpIndex==i && vShowColumn.contains("pk_org"));
			else if (vShowColumn.contains("keyval"+iIndex)==false)
				continue;

			bNeedComma=addOneColumn(bNeedComma, bufSQL, "t1.keyval"+iIndex);
			if (i==iCorpIndex)
				bufSQL.append(" pk_org");
		}
		return bNeedComma;
	}
	private static boolean addOneColumn(boolean bNeedComma,StringBuilder bufSQL,String strColumn){
		if (bNeedComma)
			bufSQL.append(",");
		bufSQL.append(strColumn);
		return true;
	}


	private static String int2BoolChar(int iVal){
		return iVal==1?"'Y'":"'N'";
	}

	private static void addJoinKey(StringBuilder bufSQL,boolean bInnerJoin){
		bufSQL.append(bInnerJoin?" inner join ":" left outer join ");
	}

	private static boolean addTaskCheckTable(StringBuilder bufSQL,boolean hasCheckFml,CheckExeQueryCondVO queryCond,Set<String> vShowColumn,String strTaskPK,String[] repPKs){
		boolean bInnerJoin=false;
		if (queryCond.getTaskCheckState()>IUfoQueryCondVO.INT_EMPTY_SELECT && queryCond.getTaskCheckState() != NOCHECK)
			bInnerJoin=true;
		else if (queryCond.getTaskCheckState()>IUfoQueryCondVO.INT_EMPTY_SELECT
				|| vShowColumn.contains(CheckExeResultVO.TASKCHECKSTATUS))
			bInnerJoin=false;

		addJoinKey(bufSQL, bInnerJoin);
		bufSQL.append("("+ getCheckTable(queryCond,strTaskPK,repPKs,false)+") t7");
		bufSQL.append(" on t1.alone_id=t7.aloneid ");
		if (bInnerJoin==false)
			return true;

		addTaskCheckWhereCond(true, bufSQL, queryCond,"t7");
		return false;
	}
	private static boolean addRepSchemeCheckTable(StringBuilder bufSQL,boolean hasCheckFml,CheckExeQueryCondVO queryCond,Set<String> vShowColumn,String strTaskPK,String[] repPKs){
		boolean bInnerJoin=false;
		if (queryCond.getTaskCheckState()>IUfoQueryCondVO.INT_EMPTY_SELECT && queryCond.getTaskCheckState() != NOCHECK)
			bInnerJoin=true;
		else if (queryCond.getTaskCheckState()>IUfoQueryCondVO.INT_EMPTY_SELECT
				|| vShowColumn.contains(CheckExeResultVO.TASKCHECKSTATUS))
			bInnerJoin=false;

		addJoinKey(bufSQL, bInnerJoin);
		bufSQL.append("("+ getCheckTable(queryCond,strTaskPK,repPKs,false)+") t7");
		bufSQL.append(" on t1.alone_id=t7.aloneid ");
		if (bInnerJoin==false)
			return true;

		addTaskCheckWhereCond(true, bufSQL, queryCond,"t7");
		return false;
	}
	private static boolean addRepFmlCheckTable(StringBuilder bufSQL,boolean hasCheckFml,CheckExeQueryCondVO queryCond,Set<String> vShowColumn,String strTaskPK,String[] repPKs){
		boolean bInnerJoin=false;
		if (queryCond.getTaskCheckState()>IUfoQueryCondVO.INT_EMPTY_SELECT && queryCond.getTaskCheckState() != NOCHECK)
			bInnerJoin=true;
		else if (queryCond.getTaskCheckState()>IUfoQueryCondVO.INT_EMPTY_SELECT
				|| vShowColumn.contains(CheckExeResultVO.TASKCHECKSTATUS))
			bInnerJoin=false;

		addJoinKey(bufSQL, bInnerJoin);
		bufSQL.append("("+ getCheckTable(queryCond,strTaskPK,repPKs,true)+") t7");
		bufSQL.append(" on t1.alone_id=t7.aloneid ");
		if (bInnerJoin==false)
			return true;

		addTaskCheckWhereCond(true, bufSQL, queryCond,"t7");
		return false;
	}
	private static boolean addSchemeFmlCheckTable(StringBuilder bufSQL,boolean hasCheckFml,CheckExeQueryCondVO queryCond,Set<String> vShowColumn,String strTaskPK,String[] repPKs){
		boolean bInnerJoin=true;
//		if (queryCond.getTaskCheckState()>IUfoQueryCondVO.INT_EMPTY_SELECT && queryCond.getTaskCheckState() != NOCHECK)
//			bInnerJoin=true;
//		else if (queryCond.getTaskCheckState()>IUfoQueryCondVO.INT_EMPTY_SELECT
//				|| vShowColumn.contains(CheckExeResultVO.TASKCHECKSTATUS))
//			bInnerJoin=false;

		addJoinKey(bufSQL, bInnerJoin);
		bufSQL.append("("+ getCheckTable(queryCond,strTaskPK,repPKs,false)+") t7");
		bufSQL.append(" on t1.alone_id=t7.aloneid ");
		if (bInnerJoin==false)
			return true;

		addTaskCheckWhereCond(true, bufSQL, queryCond,"t7");
		return false;
	}
	private static boolean addTaskCheckWhereCond(boolean bNeedAnd,StringBuilder bufSQL,IUfoQueryCondVO queryCond,String tableName){
		if (queryCond.getTaskCheckState()>IUfoQueryCondVO.INT_EMPTY_SELECT){
			String colName=tableName!=null?(tableName+".checkstate"):CheckExeResultVO.TASKCHECKSTATUS;
			bNeedAnd=addOneCondSQL(bNeedAnd, bufSQL, colName+"="+queryCond.getTaskCheckState());
		}

		return bNeedAnd;
	}
	private static boolean addTaskRepTable(StringBuilder bufSQL,IUfoQueryCondVO queryCond,Set<String> vShowColumn){
		if (queryCond.getMustCommitFlag()>IUfoQueryCondVO.INT_EMPTY_SELECT || queryCond.getMustInputFlag()>IUfoQueryCondVO.INT_EMPTY_SELECT
				|| vShowColumn.contains(RepDataQueryResultVO.MUSCOMMFLAG) || vShowColumn.contains(RepDataQueryResultVO.MUSINPUTFLAG)){
			bufSQL.append(" inner join "+TaskReportVO.getDefaultTableName()+" t8");
			bufSQL.append(" on t1.pk_report=t8.pk_report and t8.pk_task=t1.pk_task ");
			return true;
		}
		return false;
	}

	private static boolean addTaskAssignTable(StringBuilder bufSQL,IUfoQueryCondVO queryCond,Set<String> vShowColumn){
		boolean bInnerJoin=false;
		if (!UfoPublic.stringIsNull(queryCond.getAssignTaskOrg()))
			bInnerJoin=true;
		else if(vShowColumn.contains(RepDataQueryResultVO.ASSIGNORG))
			bInnerJoin=false;
		else
			return false;

		addJoinKey(bufSQL, bInnerJoin);
		bufSQL.append("iufo_taskassign t9");
		bufSQL.append(" on t1.keyword1=t9.pk_receiveorg and t9.pk_task=t1.pk_task ");
		if (bInnerJoin==false)
			return true;

		addTaskAssignWhereCond(true, bufSQL, queryCond,"t9");
		return false;
	}
	private static boolean addTaskAssignWhereCond(boolean bNeedAnd,StringBuilder bufSQL,IUfoQueryCondVO queryCond,String tableName){
		if (!UfoPublic.stringIsNull(queryCond.getAssignTaskOrg())){
			String colName=tableName!=null?(tableName+"."+TaskAssignVO.PK_ASSIGNORG):RepDataQueryResultVO.ASSIGNORG;
			bNeedAnd=addOneCondSQL(bNeedAnd, bufSQL, colName+"='"+queryCond.getAssignTaskOrg()+"'");
		}

		return bNeedAnd;
	}

	private static StringBuilder getRepSchemeCheckTable(CheckExeQueryCondVO queryCond,String strTaskPK,String[] repPKs){
		// 报表和方案审核 TODO
		// 方案
		CheckConVO checkCon = queryCond.getCheckCon();
		StringBuilder bufSQL=new StringBuilder();
		StringBuilder schemeSql = new StringBuilder("");
		StringBuilder schemeSubSql = new StringBuilder(" select a.aloneid, a.checkstate,-1 repcheckstatus,a.checkstate schemecheckstatus,-1 taskcheckstate,b.name checkcontent,a.schemepk pk_scheme, '' pk_report,a.checkperson, a.checktime,'' pk_formula  from " + IDatabaseNames.IUFO_SCHEME_CHECK + " a ," +
				"iufo_check_schema b,iufo_taskscheme c where a.schemepk = b.pk_check_schema and c.pk_scheme= a.schemepk and ");
		schemeSubSql.append(" c.pk_task= '").append(strTaskPK).append("' and ");
		String[] schemes = checkCon.getSchemas();
		String schemesInsql = UFOString.getSqlStrByArr(schemes);
		if(!StringUtils.isEmpty(schemesInsql)){
			schemeSql.append(schemeSubSql);
			schemeSql.append(" a.schemepk in ").append(schemesInsql);
		}
		// 报表

		StringBuilder  repSql = new StringBuilder();
		StringBuilder repSubSql = new StringBuilder("select a.aloneid, a.checkstate,a.checkstate repcheckstatus,-1 schemecheckstatus,-1 taskcheckstate, b.name checkcontent, null pk_scheme, a.repid pk_report,a.checkperson, a.checktime,'' pk_formula  from " + IDatabaseNames.IUFO_REP_CHECK + " a ," +
		"iufo_report b where a.repid = b.pk_report and ");


		String[] reps = checkCon.getReports();
		String repInsql = UFOString.getSqlStrByArr(reps);
		if(!StringUtils.isEmpty(repInsql)){
			repSql.append(repSubSql);
			repSql.append(" a.repid in ").append(repInsql);
		}
		if(schemeSql.length()>0){
			if(repSql.length()>0){
				bufSQL.append("(").append(schemeSql).append(" union ").append(repSql).append(")");
			}else{
				bufSQL.append(schemeSql);
			}
		}else{
			if(repInsql.length()>0){
				bufSQL.append(repSql);
			}
		}
		return bufSQL;
	}

	private static StringBuilder getSchemeFmlCheckTable(CheckExeQueryCondVO queryCond,String strTaskPK,String[] repPKs){
		CheckConVO checkCon = queryCond.getCheckCon();
		StringBuilder bufSQL=new StringBuilder();
		StringBuilder schemeFmlSql = new StringBuilder();
		schemeFmlSql.append("select distinct t2.aloneid, (t4.name ||'-' || t3.formula_name) checkcontent, t3.checkstate,t2.checkstate schemecheckstatus,-1 repcheckstate, t1.pk_scheme,'' pk_report,t2.checkperson,t2.checktime,t3.formula_id pk_formula" +
						" from iufo_taskscheme t1, " + IDatabaseNames.IUFO_SCHEME_CHECK + " t2, iufo_checkresult_note t3,iufo_check_schema t4 " +
							"where t1.pk_scheme = t2.schemepk  and t1.scheme_type = 1  and t2.pk_schemecheck = t3.pk_checkresult  " +
							"and t2.schemepk = t4.pk_check_schema and t1.pk_task='").append(strTaskPK).append("' and ");
		Map<Object,Vector<Object>> schemeFmlMap = checkCon.getSchemaFmls();
		Iterator<Map.Entry<Object, Vector<Object>>> it = schemeFmlMap.entrySet().iterator();
		// 方案公式审核 sql串拼接
		StringBuilder checkSchemeParam = new StringBuilder("(");
		String[] schemefmlIds = null;
		Vector<Object>schemeFmlVOs = null;
		String schemefmlParam = "";
		CheckSchemaVO checkScheme;
		while(it.hasNext()){
			Map.Entry<Object, Vector<Object>> entry = it.next();

			checkScheme = (CheckSchemaVO) entry.getKey();
			if(checkSchemeParam.length()>1)
				checkSchemeParam.append(" or ");
			checkSchemeParam.append("(t2.schemepk='").append(checkScheme.getPk_check_schema()).append("'");
			schemeFmlVOs = schemeFmlMap.get(checkScheme);
			if(schemeFmlVOs != null && schemeFmlVOs.size()>0){
				schemefmlIds = new String[schemeFmlVOs.size()];
				for(int i=0; i<schemeFmlVOs.size(); i++)
					schemefmlIds[i] = ((CheckFormulaVO)schemeFmlVOs.get(i)).getPk_check_formula();
				schemefmlParam = UFOString.getSqlStrByArr(schemefmlIds);
				checkSchemeParam.append(" and t3.formula_id in").append(schemefmlParam).append(")");
			}

		}
		checkSchemeParam.append(")");
		if(checkSchemeParam.length()>2){
			bufSQL.append(schemeFmlSql).append(checkSchemeParam);
		}
		return bufSQL;
	}
	private static StringBuilder getRepFmlCheckTable(CheckExeQueryCondVO queryCond,String strTaskPK,String[] repPKs){
//////////
		CheckConVO checkCon = queryCond.getCheckCon();
		StringBuilder bufSQL=new StringBuilder();

		StringBuilder repFmlSql = new StringBuilder();
		repFmlSql.append("select distinct t2.aloneid,(t4.name ||'-' || t3.formula_name) ").append(CheckExeResultVO.CHECKCONTENT).append(", t3.checkstate ,-1 schemecheckstatus,  t2.checkstate repcheckstatus," +
				" null pk_scheme,t4.pk_report,t2.checkperson,t2.checktime,t3.formula_id pk_formula from " + IDatabaseNames.IUFO_REP_CHECK + " t2," +
				" iufo_checkresult_note t3, iufo_report t4 where t2.pk_repcheck=t3.pk_checkresult and t2.repid = t4.pk_report and ");
		// 报表公式的sql串拼接
		Map<String,Vector<IUfoCheckVO>> repFmlMap = checkCon.getRepFormulas();
		Iterator<Map.Entry<String, Vector<IUfoCheckVO>>> repFmlit = repFmlMap.entrySet().iterator();
		StringBuilder checkRepParam = new StringBuilder("(");
		String[] repFmlIds = null;
		Vector<IUfoCheckVO>repFmlVOs = null;
		String repFmlParam = "";
		String repPK = "";
		while(repFmlit.hasNext()){
			Map.Entry<String, Vector<IUfoCheckVO>> entry = repFmlit.next();
			repPK = entry.getKey();
			if(checkRepParam.length()>1)
				checkRepParam.append(" or ");
			checkRepParam.append("(t2.repid='").append(repPK).append("'");
			repFmlVOs = repFmlMap.get(repPK);
			if(repFmlVOs != null && repFmlVOs.size()>0){
				repFmlIds = new String[repFmlVOs.size()];
				for(int i=0; i<repFmlVOs.size(); i++){
					IUfoCheckVO obj = repFmlVOs.get(i);
						repFmlIds[i] = obj.getID();
				}

				repFmlParam = UFOString.getSqlStrByArr(repFmlIds);
				checkRepParam.append(" and t3.formula_id in").append(repFmlParam).append(")");
			}

		}
		checkRepParam.append(")");

		if(checkRepParam.length()>2){
			bufSQL.append(repFmlSql).append(checkRepParam);
		}
		return bufSQL;

	}

	private static StringBuilder getCheckTable(CheckExeQueryCondVO queryCond,String strTaskPK,String[] repPKs,boolean isRepCheck){
		CheckConVO checkCon = queryCond.getCheckCon();
		if(checkCon.isTaskCheck()){
			return getTaskCheckTable(queryCond, strTaskPK, repPKs);
		}else if(checkCon.isFmlCheck()){
			if(isRepCheck)
				return getRepFmlCheckTable(queryCond, strTaskPK, repPKs);
			else
				return getSchemeFmlCheckTable(queryCond, strTaskPK, repPKs);
		}else
			return getRepSchemeCheckTable(queryCond, strTaskPK, repPKs);
	}
	//TODO 查询时为了避免出现一些问题，前台查询时验证是否选择了必要的条件
	private static StringBuilder getTaskCheckTable(CheckExeQueryCondVO queryCond,String strTaskPK,String[] repPKs){
		StringBuilder sbSQL=new StringBuilder();
		String inSql = UFOString.getSqlStrByArr(repPKs);
		if(StringUtils.isEmpty(inSql)){
			/*
			 *  如果没有报表pk，前面对报表pk分组了，分为有审核公式的和没有审核公式的，所以此处可能存在没有报表pk的情况
			 *  直接取方案审核的结果，否则 取方案审核的结果 和 表内审核的结果的并集
			 */
			// edit by liuweiu 2015-04-16 要审核的任务没有报表的情况（这么多年都没走到过这）
			// 改了两个地方：892行的第一个t2改为t1，末尾的group by 加了个条件t1.pk_scheme，否则会有错误ORA-00979
			sbSQL.append("select t2.aloneid, min(t2.checkstate) checkstate , min(t2.checkstate) taskcheckstate,-1 repcheckstatus," +
					"-1 schemecheckstatus,t1.pk_scheme,'' pk_formula from iufo_taskscheme t1, " + IDatabaseNames.IUFO_SCHEME_CHECK + " t2");
			sbSQL.append(" where t1.pk_scheme = t2.schemepk and t1.scheme_type = 1");
			sbSQL.append(" and t1.pk_task='"+strTaskPK+"' group by t2.aloneid,t1.pk_scheme");
		}else{

			sbSQL.append("select aloneid,min(checkstate) checkstate, min(checkstate) taskcheckstate,-1 repcheckstatus,-1 schemecheckstatus, null pk_scheme,'' pk_formula from (select t2.aloneid, t2.checkstate from iufo_taskscheme t1, " + IDatabaseNames.IUFO_SCHEME_CHECK + " t2");
			sbSQL.append(" where t1.pk_scheme = t2.schemepk and t1.scheme_type = 1");
			sbSQL.append(" and t1.pk_task='"+strTaskPK+"' union all ");
			sbSQL.append("select t2.aloneid,t2.checkstate from " + IDatabaseNames.IUFO_REP_CHECK + " t2 where t2.repid in").append(inSql);
			sbSQL.append(") b group by aloneid ");
		}
		return sbSQL;
	}
}
