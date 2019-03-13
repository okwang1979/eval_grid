package nc.impl.iufo.repdataquery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nc.impl.iufo.utils.StringConnectUtil;
import nc.pub.iufo.cache.ReportCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.iufo.pub.UfoPublic;
import nc.util.iufo.pub.UFOString;
import nc.utils.iufo.TaskSrvUtils;
import nc.vo.iufo.check.CheckSchemaVO;
import nc.vo.iufo.commit.CommitActionEnum;
import nc.vo.iufo.commit.CommitStateEnum;
import nc.vo.iufo.commit.RepDataCommitVO;
import nc.vo.iufo.commit.TaskCommitRecordVO;
import nc.vo.iufo.commit.TaskCommitVO;
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
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.vorg.ReportManaStruMemberVersionVO;

import org.apache.commons.lang.StringUtils;

import com.ufsoft.iufo.check.vo.CheckResultVO;
import com.ufsoft.script.util.ICheckResultStatus;


public class RepDataInfoQueryUtil implements ICheckResultStatus {
	public static String getRepDataQuerySQL(IUfoQueryCondVO queryCond,String[] showColumns,MeasurePubDataVO defaultPubData) throws Exception{
		KeyGroupVO keyGroup=UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(queryCond.getKeyGroupPK());
		Set<String> vShowColumn=new HashSet<String>(Arrays.asList(showColumns));

		//把报表按任务分组，一个任务一个查询语句，用union all串起来
		String[] strRepPKs=queryCond.getRepPKs();
		String[] strTaskPKs=queryCond.getTaskPKs();
		if (queryCond.getFilterRepPKs()!=null && queryCond.getFilterRepPKs().length>0){
			strRepPKs=queryCond.getFilterRepPKs();
			strTaskPKs=queryCond.getFilterTaskPKs();
		}

		Map<String,List<String>> hashRepByTask=new HashMap<String,List<String>>();
		for (int i=0;i<strRepPKs.length;i++){
			List<String> vRepPK=hashRepByTask.get(strTaskPKs[i]);
			if (vRepPK==null){
				vRepPK=new ArrayList<String>();
				hashRepByTask.put(strTaskPKs[i], vRepPK);
			}
			vRepPK.add(strRepPKs[i]);
		}
		// @edit by wuyongc at 2012-8-10,上午8:48:10 按照测算，一个任务，十张报表，拼成的SQL长度大概2048
		StringBuffer bufSQL=new StringBuffer(2048);
		boolean bFirst=true;
		Iterator<Map.Entry<String, List<String>>> set=hashRepByTask.entrySet().iterator();
		while (set.hasNext()){
			Entry<String, List<String>> entry=set.next();
			String strTaskPK=entry.getKey();
			String[] strOneRepPKs=entry.getValue().toArray(new String[0]);
			StringBuffer bufOneSQL=getOneTaskRepDataQuerySQL(queryCond, keyGroup, defaultPubData, vShowColumn, strTaskPK, strOneRepPKs);
			if (bufOneSQL!=null && bufOneSQL.length()>0){
				if (bFirst==false)
					bufSQL.append(" union ");
				bufSQL.append(bufOneSQL);
				bFirst=false;
			}
		}

		return bufSQL.toString();
	}

	private static StringBuffer getOneTaskRepDataQuerySQL(IUfoQueryCondVO queryCond,KeyGroupVO keyGroup,MeasurePubDataVO defaultPubData,
			Set<String> vShowColumn,String strTaskPK,String[] strRepPKs) throws Exception{
		boolean bHasTaskForm=isHasTaskFml(strTaskPK);

		//如果任务上没有审核方案，而查询条件要求审核通过，则直接返回null
		if (!bHasTaskForm && queryCond.getTaskCheckState()>=0 && queryCond.getTaskCheckState()!=CheckResultVO.CHECK_NO_FORMULA)
			return null;
		else if (bHasTaskForm && queryCond.getTaskCheckState()>=0 && queryCond.getTaskCheckState()==CheckResultVO.CHECK_NO_FORMULA)
			return null;

		StringBuffer bufSQL=new StringBuffer(2048);
		boolean bFirst=true;
		//将报表按有无审核公式分组，第一组为有审核公式的，第二组为无审核公式的
		String[][] strSplitRepPKs=splitRepPKsByRepCheckFml(strRepPKs);
		for (int i=0;i<strSplitRepPKs.length;i++){
			if (strSplitRepPKs[i]==null || strSplitRepPKs[i].length<=0)
				continue;

			if (i==0 && queryCond.getRepCheckState()>=0 && queryCond.getRepCheckState()==CheckResultVO.CHECK_NO_FORMULA)
				continue;
			else if (i==1 && queryCond.getRepCheckState()>=0 && queryCond.getRepCheckState()!=CheckResultVO.CHECK_NO_FORMULA)
				continue;

			StringBuffer oneSQL=getOneTaskRepCheckSQLByCheckFml(queryCond,i==0,bHasTaskForm,keyGroup,defaultPubData,vShowColumn,
					strTaskPK,strSplitRepPKs[i]);
			if (bFirst==false)
				bufSQL.append(" union all ");
			bufSQL.append(oneSQL);
			bFirst=false;
		}
		return bufSQL.length()>0?bufSQL:null;
	}


	private static StringBuffer getOneTaskRepCheckSQLByCheckFml(IUfoQueryCondVO queryCond,boolean hasCheckFml,
			boolean bHasTaskCheckFml,KeyGroupVO keyGroup,MeasurePubDataVO defaultPubData,Set<String> vShowColumn,
			String strTaskPK,String[] strRepPKs) throws Exception{
		boolean bHasOrgKey=keyGroup.getKeyByKeyPk(KeyVO.CORP_PK)!=null;

		//要查的表字段
		StringBuffer bufCol=new StringBuffer(500);
		boolean bNeedComma=addKeywordColumn(false,bufCol, keyGroup, vShowColumn);
		bNeedComma=addRepCommitColumn(bNeedComma, bufCol,queryCond, vShowColumn);
		bNeedComma=addTaskCommitColumn(bNeedComma, bufCol, queryCond, vShowColumn);
		bNeedComma=addTaskCommRecReqbackColumn(bNeedComma, bufCol, queryCond, vShowColumn);
		bNeedComma=addTaskCommRecHastenColumn(bNeedComma, bufCol, queryCond, vShowColumn);
		bNeedComma=addRepCheckColumn(bNeedComma, bufCol, queryCond, vShowColumn,hasCheckFml);
		bNeedComma=addTaskCheckColumn(bNeedComma, bufCol, queryCond, vShowColumn,bHasTaskCheckFml);
		bNeedComma=addTaskRepColumn(bNeedComma, bufCol, queryCond, vShowColumn);
		bNeedComma=bHasOrgKey?addTaskAssignColumn(bNeedComma, bufCol, queryCond, vShowColumn):bNeedComma;

		//生成要查找的表名及关联条件，用left outer join on关联
		StringBuffer bufFrom=new StringBuffer();
		bufFrom.append("("+getKeycondTable(queryCond, vShowColumn,defaultPubData, keyGroup, strTaskPK,strRepPKs)+") t1 ");
		boolean bRepCommitTable=addRepCommitTable(bufFrom, queryCond, vShowColumn);
		boolean bTaskCommitTable=addTaskCommitTable(bufFrom,queryCond,vShowColumn);
		boolean bTaskComRecReqTable=addTaskCommRecReqbackTable(bufFrom,queryCond,vShowColumn);
		boolean bTaskComRecHastTable=addTaskCommRecHastenTable(bufFrom,queryCond,vShowColumn);
		// @edit by wuyongc at 2013-7-27,上午11:07:57 不论有没有公式，也得关联iufo_repcheck 表查询，为了取出审核人和审核事件。
//		boolean bRepCheckTable=hasCheckFml?addRepCheckTable(bufFrom, queryCond, vShowColumn):false;
		boolean bRepCheckTable= addRepCheckTable(bufFrom, queryCond, vShowColumn);
		boolean bTaskCheckTable= addTaskCheckTable(bufFrom, queryCond, vShowColumn,strTaskPK);
//		boolean bTaskCheckTable=bHasTaskCheckFml?addTaskCheckTable(bufFrom, queryCond, vShowColumn,strTaskPK):false;
		boolean bTaskRepTable=addTaskRepTable(bufFrom,queryCond,vShowColumn);
		boolean bTaskAssignTable=bHasOrgKey?addTaskAssignTable(bufFrom,queryCond,vShowColumn):false;

		//生成筛选条件
		StringBuffer bufWhere=new StringBuffer();
		boolean bNeedAnd=bRepCommitTable?addRepCommitWhereCond(false, bufWhere, queryCond,null):false;
		bNeedAnd=bTaskCommitTable?addTaskCommitWhereCond(bNeedAnd, bufWhere, queryCond,null):bNeedAnd;
		bNeedAnd=bTaskComRecReqTable?addTaskCommRecReqbackWhereCond(bNeedAnd, bufWhere, queryCond,null):bNeedAnd;
		bNeedAnd=bTaskComRecHastTable?addTaskCommRecHastenWhereCond(bNeedAnd, bufWhere, queryCond,null):bNeedAnd;
		bNeedAnd=bRepCheckTable?addRepCheckWhereCond(bNeedAnd, bufWhere, queryCond,null):bNeedAnd;
		bNeedAnd=bTaskCheckTable?addTaskCheckWhereCond(bNeedAnd, bufWhere, queryCond,null):bNeedAnd;
		bNeedAnd=bTaskRepTable?addTaskRepWhereCond(bNeedAnd, bufWhere, queryCond):bNeedAnd;
		bNeedAnd=bTaskAssignTable?addTaskAssignWhereCond(bNeedAnd, bufWhere, queryCond,null):bNeedAnd;

		//拼装语句
		StringBuffer bufSQL=new StringBuffer(2000);
		bufSQL.append("select ").append(bufCol);
		bufSQL.append(" from ").append(bufFrom);
		if (bufWhere.length()>0){
			StringBuffer bufSQL1=new StringBuffer("select * from (").append(bufSQL).append(") othername ");
			bufSQL1.append(" where ").append(bufWhere);
			bufSQL=bufSQL1;
		}

		return bufSQL;
	}

	private static boolean addKeywordColumn(boolean bNeedComma,StringBuffer bufSQL,KeyGroupVO keyGroup,Set<String> vShowColumn){
		bNeedComma=addOneColumn(bNeedComma, bufSQL, "distinct t1.alone_id,t1.pk_task,t1.pk_report,t2.dataorigin");
		int iCorpIndex=keyGroup.getIndexByKeywordPK(KeyVO.CORP_PK);
		for (int i=0,len=keyGroup.getKeys().length;i<len;i++){
			int iIndex=i+1;
			bNeedComma=addOneColumn(bNeedComma,bufSQL,"t1.keyword"+iIndex);

			if (iCorpIndex==i && vShowColumn.contains("pk_org"))
				;
			else if (vShowColumn.contains("keyval"+iIndex)==false)
				continue;

			bNeedComma=addOneColumn(bNeedComma, bufSQL, "t1.keyval"+iIndex);
			if (i==iCorpIndex)
				bufSQL.append(" pk_org");
		}
		return bNeedComma;
	}

	private static boolean addRepCommitColumn(boolean bNeedComma,StringBuffer bufSQL,IUfoQueryCondVO queryCond,Set<String> vShowColumn){
		if (queryCond.getRepCommitState()>IUfoQueryCondVO.INT_EMPTY_SELECT || vShowColumn.contains(RepDataQueryResultVO.REPCOMMITSTATE))
			bNeedComma=addOneColumn(bNeedComma,bufSQL,"coalesce(t2."+RepDataCommitVO.COMMIT_STATE+","
					+CommitStateEnum.STATE_NOCOMMIT.getIntValue()+") "+RepDataQueryResultVO.REPCOMMITSTATE);
		if (queryCond.getInputState()>IUfoQueryCondVO.INT_EMPTY_SELECT || vShowColumn.contains(RepDataQueryResultVO.INPUTSTATE))
			bNeedComma=addOneColumn(bNeedComma,bufSQL,"coalesce(t2."+RepDataCommitVO.FLAG_INPUT+","
					+"'N') "+RepDataQueryResultVO.INPUTSTATE);
	    if (!UfoPublic.stringIsNull(queryCond.getInputPerson()) || vShowColumn.contains(RepDataQueryResultVO.INPUTPERSON))
			bNeedComma=addOneColumn(bNeedComma,bufSQL,"t2."+RepDataCommitVO.INPUT_PERSON+" "+RepDataQueryResultVO.INPUTPERSON);
		if (!UfoPublic.stringIsNull(queryCond.getInputDate()) || vShowColumn.contains(RepDataQueryResultVO.INPUTTIME))
			bNeedComma=addOneColumn(bNeedComma,bufSQL,"t2."+RepDataCommitVO.INPUT_TIME+" "+RepDataQueryResultVO.INPUTTIME);

		return bNeedComma;
	}

	private static boolean addTaskCommitColumn(boolean bNeedComma,StringBuffer bufSQL,IUfoQueryCondVO queryCond,Set<String> vShowColumn){
		if (queryCond.getTaskCommitState()>IUfoQueryCondVO.INT_EMPTY_SELECT || vShowColumn.contains(RepDataQueryResultVO.TASKCOMMITSTATE))
			bNeedComma=addOneColumn(bNeedComma, bufSQL, "coalesce(t3."+TaskCommitVO.COMMIT_STATE+","
					+CommitStateEnum.STATE_NOCOMMIT.getIntValue()+") "+RepDataQueryResultVO.TASKCOMMITSTATE);
		if (queryCond.getReqBackFlag()>IUfoQueryCondVO.INT_EMPTY_SELECT || vShowColumn.contains(RepDataQueryResultVO.REQBACKFLAG))
			bNeedComma=addOneColumn(bNeedComma, bufSQL, "coalesce(t3."+TaskCommitVO.FLAG_REQUEST+",'N') "+RepDataQueryResultVO.REQBACKFLAG);
		if (queryCond.getHastenFlag()>IUfoQueryCondVO.INT_EMPTY_SELECT || vShowColumn.contains(RepDataQueryResultVO.HASTENFLAG))
			bNeedComma=addOneColumn(bNeedComma, bufSQL, "coalesce(t3."+TaskCommitVO.FLAG_HASTEN+",'N') "+RepDataQueryResultVO.HASTENFLAG);
		if (vShowColumn.contains(RepDataQueryResultVO.LASTOPERATION))
			bNeedComma=addOneColumn(bNeedComma, bufSQL, "t3."+TaskCommitVO.LAST_ACTION+" "+RepDataQueryResultVO.LASTOPERATION);
		if (vShowColumn.contains(RepDataQueryResultVO.LASTOPEPERSON))
			bNeedComma=addOneColumn(bNeedComma, bufSQL, "t3."+TaskCommitVO.LASTOPERATOR+" "+RepDataQueryResultVO.LASTOPEPERSON);
		if (vShowColumn.contains(RepDataQueryResultVO.LASTOPETIME))
			bNeedComma=addOneColumn(bNeedComma, bufSQL, "t3."+TaskCommitVO.LASTOPERTIME+" "+RepDataQueryResultVO.LASTOPETIME);

		return bNeedComma;
	}

	private static boolean addTaskCommRecReqbackColumn(boolean bNeedComma,StringBuffer bufSQL,IUfoQueryCondVO queryCond,Set<String> vShowColumn){
		if (!UfoPublic.stringIsNull(queryCond.getReqBackPerson()) || vShowColumn.contains(RepDataQueryResultVO.REQBACKPERSON))
			bNeedComma=addOneColumn(bNeedComma,bufSQL,"t4."+TaskCommitRecordVO.OPERATOR+" "+RepDataQueryResultVO.REQBACKPERSON);
		if (!UfoPublic.stringIsNull(queryCond.getReqBackDate()) || vShowColumn.contains(RepDataQueryResultVO.REQBACKTIME))
			bNeedComma=addOneColumn(bNeedComma,bufSQL,"t4."+TaskCommitRecordVO.OPERATE_TIME+" "+RepDataQueryResultVO.REQBACKTIME);

		return bNeedComma;
	}

	private static boolean addTaskCommRecHastenColumn(boolean bNeedComma,StringBuffer bufSQL,IUfoQueryCondVO queryCond,Set<String> vShowColumn){
		if (!UfoPublic.stringIsNull(queryCond.getHastenPerson()) || vShowColumn.contains(RepDataQueryResultVO.HASTENPERSON))
			bNeedComma=addOneColumn(bNeedComma,bufSQL,"t5."+TaskCommitRecordVO.OPERATOR+" "+RepDataQueryResultVO.HASTENPERSON);
		if (!UfoPublic.stringIsNull(queryCond.getHastenDate()) || vShowColumn.contains(RepDataQueryResultVO.HASTENTIME))
			bNeedComma=addOneColumn(bNeedComma,bufSQL,"t5."+TaskCommitRecordVO.OPERATE_TIME+" "+RepDataQueryResultVO.HASTENTIME);

		return bNeedComma;
	}

	private static boolean addRepCheckColumn(boolean bNeedComma,StringBuffer bufSQL,IUfoQueryCondVO queryCond,Set<String> vShowColumn,boolean hasCheckFml){
		if (queryCond.getRepCheckState()>IUfoQueryCondVO.INT_EMPTY_SELECT || vShowColumn.contains(RepDataQueryResultVO.REPCHECKSTATE)){
			String column=hasCheckFml?("coalesce(t6.checkstate,"+NOCHECK+")"):(""+CheckResultVO.CHECK_NO_FORMULA);
			bNeedComma=addOneColumn(bNeedComma,bufSQL,column+" "+RepDataQueryResultVO.REPCHECKSTATE);
		}
		// @edit by wuyongc at 2013-7-27,上午11:08:53 不论有没有公式，也得关联iufo_repcheck 表查询，为了取出审核人和审核事件。
		if (vShowColumn.contains(RepDataQueryResultVO.REPCHECKTIME)){
//			String column=hasCheckFml?"t6.checktime":"''";
			String column = "t6.checktime";
			bNeedComma=addOneColumn(bNeedComma,bufSQL,column+" "+RepDataQueryResultVO.REPCHECKTIME);
		}

		if (vShowColumn.contains(RepDataQueryResultVO.REPCHECKPERSON)){
			String column = "t6.checkperson";
			bNeedComma=addOneColumn(bNeedComma,bufSQL,column+" "+RepDataQueryResultVO.REPCHECKPERSON);
		}

		return bNeedComma;
	}

	private static boolean addTaskCheckColumn(boolean bNeedComma,StringBuffer bufSQL,IUfoQueryCondVO queryCond,Set<String> vShowColumn,boolean hasCheckFml){
		if (queryCond.getTaskCheckState()>IUfoQueryCondVO.INT_EMPTY_SELECT || vShowColumn.contains(RepDataQueryResultVO.TASKCHECKSTATE)){
			String column=hasCheckFml?("coalesce(t7.checkstate,"+NOCHECK+")"):(""+CheckResultVO.CHECK_NO_FORMULA);
			bNeedComma=addOneColumn(bNeedComma,bufSQL,column+" "+RepDataQueryResultVO.TASKCHECKSTATE);
		}
		if (vShowColumn.contains(RepDataQueryResultVO.TASKCHECKTIME)){
			String column=hasCheckFml?"t7.checktime":" t7.checktime ";
			bNeedComma=addOneColumn(bNeedComma,bufSQL,column+" "+RepDataQueryResultVO.TASKCHECKTIME);
		}

		if (vShowColumn.contains(RepDataQueryResultVO.TASKCHECKPERSON)){
//			String column=hasCheckFml?"t7.taskcheckperson":"t7.taskcheckperson";
			String column = "t7.taskcheckperson";
			bNeedComma=addOneColumn(bNeedComma,bufSQL,column+" "+RepDataQueryResultVO.TASKCHECKPERSON);
		}

		return bNeedComma;
	}

	private static boolean addTaskRepColumn(boolean bNeedComma,StringBuffer bufSQL,IUfoQueryCondVO queryCond,Set<String> vShowColumn){
		if (queryCond.getMustCommitFlag()>IUfoQueryCondVO.INT_EMPTY_SELECT || vShowColumn.contains(RepDataQueryResultVO.MUSCOMMFLAG)){
			String column="case t8."+TaskReportVO.COMMITATTR+" when 0 then 'Y' when 1 then 'Y' else 'N' end";
			bNeedComma=addOneColumn(bNeedComma,bufSQL,column+" "+RepDataQueryResultVO.MUSCOMMFLAG);
		}
		if (queryCond.getMustInputFlag()>IUfoQueryCondVO.INT_EMPTY_SELECT || vShowColumn.contains(RepDataQueryResultVO.MUSINPUTFLAG)){
			String column="case t8."+TaskReportVO.COMMITATTR+" when 0 then 'Y'  else 'N' end";
			bNeedComma=addOneColumn(bNeedComma,bufSQL,column+" "+RepDataQueryResultVO.MUSINPUTFLAG);
		}
		return bNeedComma;
	}

	private static boolean addTaskAssignColumn(boolean bNeedComma,StringBuffer bufSQL,IUfoQueryCondVO queryCond,Set<String> vShowColumn){
		if (vShowColumn.contains(RepDataQueryResultVO.ASSIGNORG))
			bNeedComma=addOneColumn(bNeedComma,bufSQL,"t9."+TaskAssignVO.PK_ASSIGNORG+" "+RepDataQueryResultVO.ASSIGNORG);

		return bNeedComma;
	}

	private static boolean addRepCommitTable(StringBuffer bufSQL,IUfoQueryCondVO queryCond,Set<String> vShowColumn){
		//判断应该用左连接还是内连接
		boolean bInnerJoin=false;
		if (!UfoPublic.stringIsNull(queryCond.getInputDate()) || !UfoPublic.stringIsNull(queryCond.getInputPerson())
				|| (queryCond.getRepCommitState()>IUfoQueryCondVO.INT_EMPTY_SELECT && queryCond.getRepCommitState()!=CommitStateEnum.STATE_NOCOMMIT.getIntValue())
				|| queryCond.getInputState()==1)
			bInnerJoin=true;
		else if (queryCond.getRepCommitState()>IUfoQueryCondVO.INT_EMPTY_SELECT || queryCond.getInputState()>IUfoQueryCondVO.INT_EMPTY_SELECT
				|| vShowColumn.contains(RepDataQueryResultVO.INPUTSTATE) || vShowColumn.contains(RepDataQueryResultVO.INPUTPERSON)
				|| vShowColumn.contains(RepDataQueryResultVO.INPUTTIME) || vShowColumn.contains(RepDataQueryResultVO.REPCOMMITSTATE))
			bInnerJoin=false;
		else
			return false;

		addJoinKey(bufSQL, bInnerJoin);
		bufSQL.append(RepDataCommitVO.getDefaultTableName()+" t2");
		// @edit by wuyongc at 2011-5-23,下午01:19:57 去掉任务关联. 为处理不同任务中有相同的报表,只要此相同的报表的录入状态改变,那么另外的任务中的录入状态也应该改变
		bufSQL.append(" on t1.alone_id=t2.alone_id and t1.pk_report=t2.pk_report ");
		if (bInnerJoin==false)
			return true;

		addRepCommitWhereCond(true, bufSQL, queryCond, "t2");
		return false;
	}

	private static boolean addTaskCommitTable(StringBuffer bufSQL,IUfoQueryCondVO queryCond,Set<String> vShowColumn){
		boolean bInnerJoin=false;
		if ((queryCond.getTaskCommitState()>IUfoQueryCondVO.INT_EMPTY_SELECT && queryCond.getTaskCommitState()!=CommitStateEnum.STATE_NOCOMMIT.getIntValue())
				|| queryCond.getReqBackFlag()==1 || queryCond.getHastenFlag()==1)
			bInnerJoin=true;
		else if (queryCond.getTaskCommitState()>IUfoQueryCondVO.INT_EMPTY_SELECT || queryCond.getReqBackFlag()>IUfoQueryCondVO.INT_EMPTY_SELECT
				|| queryCond.getHastenFlag()>IUfoQueryCondVO.INT_EMPTY_SELECT || vShowColumn.contains(RepDataQueryResultVO.TASKCOMMITSTATE)
				|| vShowColumn.contains(RepDataQueryResultVO.REQBACKFLAG) || vShowColumn.contains(RepDataQueryResultVO.HASTENFLAG)
				|| vShowColumn.contains(RepDataQueryResultVO.LASTOPERATION) || vShowColumn.contains(RepDataQueryResultVO.LASTOPEPERSON)
				|| vShowColumn.contains(RepDataQueryResultVO.LASTOPETIME))
			bInnerJoin=false;
		else
			return false;

		addJoinKey(bufSQL, bInnerJoin);
		bufSQL.append(TaskCommitVO.getDefaultTableName()+" t3");
		bufSQL.append(" on t1.alone_id=t3.alone_id and t1.pk_task=t3.pk_task ");
		if (bInnerJoin==false)
			return true;

		addTaskCommitWhereCond(true, bufSQL, queryCond,"t3");
		return false;
	}

	private static boolean addTaskCommRecReqbackTable(StringBuffer bufSQL,IUfoQueryCondVO queryCond,Set<String> vShowColumn){
		boolean bInnerJoin=false;
		if (!UfoPublic.stringIsNull(queryCond.getReqBackDate())	|| !UfoPublic.stringIsNull(queryCond.getReqBackPerson()))
			bInnerJoin=true;
		else if(vShowColumn.contains(RepDataQueryResultVO.REQBACKPERSON) || vShowColumn.contains(RepDataQueryResultVO.REQBACKTIME))
			bInnerJoin=false;
		else
			return false;

		addJoinKey(bufSQL, bInnerJoin);
		bufSQL.append(TaskCommitRecordVO.getDefaultTableName()+" t4");
		bufSQL.append(" on t1.alone_id=t4.alone_id and t1.pk_task=t4.pk_task and t4.action="+CommitActionEnum.ACTION_REQUESTBACK.getIntValue()+" ");
		if (bInnerJoin==false)
			return true;

		addTaskCommRecReqbackWhereCond(true, bufSQL, queryCond, "t4");
		return false;
	}

	private static boolean addTaskCommRecHastenTable(StringBuffer bufSQL,IUfoQueryCondVO queryCond,Set<String> vShowColumn){
		boolean bInnerJoin=false;
		if (!UfoPublic.stringIsNull(queryCond.getHastenDate())	|| !UfoPublic.stringIsNull(queryCond.getHastenPerson()))
			bInnerJoin=true;
		else if(vShowColumn.contains(RepDataQueryResultVO.HASTENPERSON) || vShowColumn.contains(RepDataQueryResultVO.HASTENTIME))
			bInnerJoin=false;
		else
			return false;

		addJoinKey(bufSQL, bInnerJoin);
		bufSQL.append(TaskCommitRecordVO.getDefaultTableName()+" t5");
		bufSQL.append(" on t1.alone_id=t5.alone_id and t1.pk_task=t5.pk_task and t5.action="+CommitActionEnum.ACTION_HASTEN.getIntValue()+" ");
		if (bInnerJoin==false)
			return true;

		addTaskCommRecHastenWhereCond(true, bufSQL, queryCond, "t4");
		return false;
	}

	private static boolean addRepCheckTable(StringBuffer bufSQL,IUfoQueryCondVO queryCond,Set<String> vShowColumn){
		boolean bInnerJoin=false;
		// 修正了产品BUG:报表数据查询按照报表审核状态查询时没有过滤没有审核公式的情况，导致查询结果不正确
		// if (queryCond.getRepCheckState()>IUfoQueryCondVO.INT_EMPTY_SELECT &&
		// queryCond.getRepCheckState() != NOCHECK )
		if (queryCond.getRepCheckState() > IUfoQueryCondVO.INT_EMPTY_SELECT
				&& queryCond.getRepCheckState() != NOCHECK
				&& queryCond.getRepCheckState() != CHECK_NO_FORMULA){
			bInnerJoin=true;
		}else if (queryCond.getRepCheckState()>IUfoQueryCondVO.INT_EMPTY_SELECT || vShowColumn.contains(RepDataQueryResultVO.REPCHECKPERSON)
				|| vShowColumn.contains(RepDataQueryResultVO.REPCHECKSTATE) || vShowColumn.contains(RepDataQueryResultVO.REPCHECKTIME)){
			bInnerJoin=false;
		}else
			return false;

		addJoinKey(bufSQL, bInnerJoin);
		bufSQL.append(IDatabaseNames.IUFO_REP_CHECK).append(" t6 on t1.alone_id=t6.aloneid and t1.pk_report=t6.repid ");
		if (bInnerJoin==false)
			return true;

		addRepCheckWhereCond(true, bufSQL, queryCond,"t6");
		return false;
	}

	private static boolean addTaskCheckTable(StringBuffer bufSQL,IUfoQueryCondVO queryCond,Set<String> vShowColumn,String strTaskPK){
		boolean bInnerJoin=false;
		if (queryCond.getTaskCheckState()>IUfoQueryCondVO.INT_EMPTY_SELECT && queryCond.getTaskCheckState() != NOCHECK)
			bInnerJoin=true;
		else if (queryCond.getTaskCheckState()>IUfoQueryCondVO.INT_EMPTY_SELECT || vShowColumn.contains(RepDataQueryResultVO.TASKCHECKPERSON)
				|| vShowColumn.contains(RepDataQueryResultVO.TASKCHECKSTATE) || vShowColumn.contains(RepDataQueryResultVO.TASKCHECKTIME))
			bInnerJoin=false;

		addJoinKey(bufSQL, bInnerJoin);
		bufSQL.append("("+getTaskCheckTable(strTaskPK)+") t7");
		bufSQL.append(" on t1.alone_id=t6.aloneid and t1.alone_id=t7.aloneid");
		if (bInnerJoin==false)
			return true;

		addTaskCheckWhereCond(true, bufSQL, queryCond,"t7");
		return false;
	}

	private static boolean addTaskRepTable(StringBuffer bufSQL,IUfoQueryCondVO queryCond,Set<String> vShowColumn){
		if (queryCond.getMustCommitFlag()>IUfoQueryCondVO.INT_EMPTY_SELECT || queryCond.getMustInputFlag()>IUfoQueryCondVO.INT_EMPTY_SELECT
				|| vShowColumn.contains(RepDataQueryResultVO.MUSCOMMFLAG) || vShowColumn.contains(RepDataQueryResultVO.MUSINPUTFLAG)){
			bufSQL.append(" inner join "+TaskReportVO.getDefaultTableName()+" t8");
			bufSQL.append(" on t1.pk_report=t8.pk_report and t8.pk_task=t1.pk_task ");
			return true;
		}
		return false;
	}

	private static boolean addTaskAssignTable(StringBuffer bufSQL,IUfoQueryCondVO queryCond,Set<String> vShowColumn){
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

	private static String genOneTimeCond(String strColumn,String strInputDate){
		String cond=strColumn+">='"+strInputDate+" 00:00:00'";
		cond+=" and "+strColumn+"<='"+strInputDate+" 23:59:59'";
		return cond;
	}

	private static boolean addRepCommitWhereCond(boolean bNeedAnd,StringBuffer bufSQL,IUfoQueryCondVO queryCond,String tableName){
		if (queryCond.getRepCommitState()>IUfoQueryCondVO.INT_EMPTY_SELECT){
			String colName=tableName!=null?(tableName+"."+RepDataCommitVO.COMMIT_STATE):RepDataQueryResultVO.REPCOMMITSTATE;
			bNeedAnd=addOneCondSQL(bNeedAnd, bufSQL, colName+"="+queryCond.getRepCommitState());
		}

		if (!UfoPublic.stringIsNull(queryCond.getInputDate())){
			String colName=tableName!=null?("t2."+RepDataCommitVO.INPUT_TIME):RepDataQueryResultVO.INPUTTIME;
			bNeedAnd=addOneCondSQL(bNeedAnd, bufSQL, genOneTimeCond(colName,queryCond.getInputDate()));
		}

		if (!UfoPublic.stringIsNull(queryCond.getInputPerson())){
			String colName=tableName!=null?("t2."+RepDataCommitVO.INPUT_PERSON):RepDataQueryResultVO.INPUTPERSON;
			bNeedAnd=addOneCondSQL(bNeedAnd,bufSQL,colName+"='"+queryCond.getInputPerson()+"'");
		}

		if (queryCond.getInputState()>IUfoQueryCondVO.INT_EMPTY_SELECT){
			String colName=tableName!=null?("t2."+RepDataCommitVO.FLAG_INPUT):RepDataQueryResultVO.INPUTSTATE;
			bNeedAnd=addOneCondSQL(bNeedAnd,bufSQL,colName+"="+int2BoolChar(queryCond.getInputState()));
		}

		return bNeedAnd;
	}

	private static boolean addTaskCommitWhereCond(boolean bNeedAnd,StringBuffer bufSQL,IUfoQueryCondVO queryCond,String tableName){
		if (queryCond.getTaskCommitState()>IUfoQueryCondVO.INT_EMPTY_SELECT){
			String colName=tableName!=null?(tableName+"."+TaskCommitVO.COMMIT_STATE):RepDataQueryResultVO.REPCOMMITSTATE;
			bNeedAnd=addOneCondSQL(bNeedAnd, bufSQL, colName+"="+queryCond.getTaskCommitState());
		}

		if (queryCond.getReqBackFlag()>IUfoQueryCondVO.INT_EMPTY_SELECT){
			String colName=tableName!=null?(tableName+"."+TaskCommitVO.FLAG_REQUEST):RepDataQueryResultVO.REQBACKFLAG;
			bNeedAnd=addOneCondSQL(bNeedAnd, bufSQL, colName+"="+int2BoolChar(queryCond.getReqBackFlag()));
		}

		if (queryCond.getHastenFlag()>IUfoQueryCondVO.INT_EMPTY_SELECT){
			String colName=tableName!=null?(tableName+"."+TaskCommitVO.FLAG_HASTEN):RepDataQueryResultVO.HASTENFLAG;
			bNeedAnd=addOneCondSQL(bNeedAnd, bufSQL, colName+"="+int2BoolChar(queryCond.getHastenFlag()));
		}

		return bNeedAnd;
	}

	private static boolean addTaskCommRecReqbackWhereCond(boolean bNeedAnd,StringBuffer bufSQL,IUfoQueryCondVO queryCond,String tableName){
		if (!UfoPublic.stringIsNull(queryCond.getReqBackDate())){
			String colName=tableName!=null?(tableName+"."+TaskCommitRecordVO.OPERATE_TIME):RepDataQueryResultVO.REQBACKTIME;
			bNeedAnd=addOneCondSQL(bNeedAnd, bufSQL,genOneTimeCond(colName,queryCond.getReqBackDate()));
		}

		if (!UfoPublic.stringIsNull(queryCond.getReqBackPerson())){
			String colName=tableName!=null?(tableName+"."+TaskCommitRecordVO.OPERATOR):RepDataQueryResultVO.REQBACKPERSON;
			bNeedAnd=addOneCondSQL(bNeedAnd, bufSQL, colName+"='"+queryCond.getReqBackPerson()+"'");
		}

		return bNeedAnd;
	}

	private static boolean addTaskCommRecHastenWhereCond(boolean bNeedAnd,StringBuffer bufSQL,IUfoQueryCondVO queryCond,String tableName){
		if (!UfoPublic.stringIsNull(queryCond.getHastenDate())){
			String colName=tableName!=null?(tableName+"."+TaskCommitRecordVO.OPERATE_TIME):RepDataQueryResultVO.HASTENTIME;
			bNeedAnd=addOneCondSQL(bNeedAnd, bufSQL, genOneTimeCond(colName,queryCond.getHastenDate()));
		}

		if (!UfoPublic.stringIsNull(queryCond.getHastenPerson())){
			String colName=tableName!=null?(tableName+"."+TaskCommitRecordVO.OPERATOR):RepDataQueryResultVO.HASTENPERSON;
			bNeedAnd=addOneCondSQL(bNeedAnd, bufSQL, colName+"='"+queryCond.getHastenPerson()+"'");
		}

		return bNeedAnd;
	}

	private static boolean addRepCheckWhereCond(boolean bNeedAnd,StringBuffer bufSQL,IUfoQueryCondVO queryCond,String tableName){
		if (queryCond.getRepCheckState()>IUfoQueryCondVO.INT_EMPTY_SELECT){
			String colName=tableName!=null?(tableName+".checkstate"):RepDataQueryResultVO.REPCHECKSTATE;
			bNeedAnd=addOneCondSQL(bNeedAnd, bufSQL, colName+"="+queryCond.getRepCheckState());
		}

		return bNeedAnd;
	}

	private static boolean addTaskCheckWhereCond(boolean bNeedAnd,StringBuffer bufSQL,IUfoQueryCondVO queryCond,String tableName){
		if (queryCond.getTaskCheckState()>IUfoQueryCondVO.INT_EMPTY_SELECT){
			String colName=tableName!=null?(tableName+".checkstate"):RepDataQueryResultVO.TASKCHECKSTATE;
			bNeedAnd=addOneCondSQL(bNeedAnd, bufSQL, colName+"="+queryCond.getTaskCheckState());
		}

		return bNeedAnd;
	}

	private static boolean addTaskRepWhereCond(boolean bNeedAnd,StringBuffer bufSQL,IUfoQueryCondVO queryCond){
		if (queryCond.getMustInputFlag()>IUfoQueryCondVO.INT_EMPTY_SELECT)
			bNeedAnd=addOneCondSQL(bNeedAnd, bufSQL, RepDataQueryResultVO.MUSINPUTFLAG+"="+int2BoolChar(queryCond.getTaskCheckState()));

		if (queryCond.getMustCommitFlag()>IUfoQueryCondVO.INT_EMPTY_SELECT)
			bNeedAnd=addOneCondSQL(bNeedAnd, bufSQL, RepDataQueryResultVO.MUSCOMMFLAG+"="+int2BoolChar(queryCond.getTaskCheckState()));

		return bNeedAnd;
	}

	private static boolean addTaskAssignWhereCond(boolean bNeedAnd,StringBuffer bufSQL,IUfoQueryCondVO queryCond,String tableName){
		if (!UfoPublic.stringIsNull(queryCond.getAssignTaskOrg())){
			String colName=tableName!=null?(tableName+"."+TaskAssignVO.PK_ASSIGNORG):RepDataQueryResultVO.ASSIGNORG;
			bNeedAnd=addOneCondSQL(bNeedAnd, bufSQL, colName+"='"+queryCond.getAssignTaskOrg()+"'");
		}

		return bNeedAnd;
	}

	private static boolean addOneCondSQL(boolean bNeedAnd,StringBuffer bufSQL,String strOneCond){
		if (bNeedAnd){
			bufSQL.append(" and ");
		}
		bufSQL.append(strOneCond);
		return true;
	}

	public static String arrayToString(String[] aryString) {
		return UFOString.getSqlStrByArr(aryString);
	}

	private static StringBuffer getOrgCondSQL(IUfoQueryCondVO queryCond,String pk_task) throws Exception{
		StringBuffer bufSQL=new StringBuffer("select distinct pk_org from "+ReportManaStruMemberVersionVO.getDefaultTableName()+" t1 where ");
		String strOrgPK=queryCond.getKeyVal(KeyVO.CORP_PK);
		// @edit by wuyongc at 2013-2-21,下午1:44:52 如果是方案查询，取不到strOrgPK，直接走到后面。
		if (StringUtils.isNotEmpty(strOrgPK) && queryCond.getOrgType()==IUfoQueryCondVO.ORGTYPE_SELF){
			bufSQL.append("pk_org='"+strOrgPK+"'");
			bufSQL.append(" and pk_org in(select PK_RECEIVEORG from iufo_taskassign where pk_task='"+pk_task+"')");
			return bufSQL;
		}

		//add by jiaah 如果是汇总时，组织条件
		if(queryCond.getOrgType()==IUfoQueryCondVO.ORGTYPE_TOTAL){
//			String orgs = UFOString.getSqlStrByArr(queryCond.getOrgPKs());
			String orgs = StringConnectUtil.getInSqlGroupByArr(queryCond.getOrgPKs(),"pk_org");
//			bufSQL.append(" pk_org in " + orgs);
			bufSQL.append(orgs);
			return bufSQL;
		}

		/*BaseDAO dao=new BaseDAO();
		SQLParameter param=new SQLParameter();
		param.addParam(queryCond.getPk_rms());
		param.addParam(strOrgPK);
		Collection set=dao.retrieveByClause(ReportManaStruMemberVO.class,"pk_rms=? and pk_org=?",param);
		if (set==null || set.size()<=0)
			throw new Exception("组织错误！");

		String strInnerCode=((ReportManaStruMemberVO)set.toArray()[0]).getInnercode();
		// @edit by wuyongc at 2011-7-8,下午04:53:58 末级查询SQL拼接修改。 末级节点判断时应该加上任务分配组织的条件。
		String orgSqlIn = UFOString.getSqlStrByArr(queryCond.getOrgPKs());

		if (queryCond.getOrgType()==IUfoQueryCondVO.ORGTYPE_ALL){
			bufSQL.append("innercode like '"+strInnerCode+"%'");
		}else if (queryCond.getOrgType()==IUfoQueryCondVO.ORGTYPE_DIRECT){
			String lenStr = "";
			switch (dao.getDBType()) {
			case DBConsts.ORACLE:
				lenStr = "length(innercode)";
				break;
			case DBConsts.SQLSERVER:
				lenStr = "len(innercode)";
				break;
			case DBConsts.DB2:
				lenStr = "length(innercode)";
				break;
			case DBConsts.POSTGRESQL:
				lenStr = "char_length(innercode)";
				break;
			}
			bufSQL.append("(pk_org='"+strOrgPK+"' or (innercode like '"+strInnerCode+"%' and "+ lenStr + "="+(strInnerCode.length()+4)+"))");
		}else{
			bufSQL.append("innercode like '"+strInnerCode+"%' and not exists (select 1 from org_rmsmember t2 " +
					"where t1.pk_org=t2.pk_fatherorg and t2.pk_rms='"+queryCond.getPk_rms()+"' and t2.pk_org in"+ orgSqlIn +	")");
		}*/
		String orgSqlIn = null;
		if(StringUtils.isNotEmpty(strOrgPK)){
//			orgSqlIn = UFOString.getSqlStrByArr(queryCond.getSelectedOrgPKs());
			orgSqlIn = StringConnectUtil.getInSqlGroupByArr(queryCond.getSelectedOrgPKs(), "pk_org");
		}else{
			//相当于方案查询
//			orgSqlIn = UFOString.getSqlStrByArr(queryCond.getOrgPKs());
			orgSqlIn = StringConnectUtil.getInSqlGroupByArr(queryCond.getOrgPKs(), "pk_org");
		}
//		bufSQL.append(" pk_org in"+ orgSqlIn);
		bufSQL.append(orgSqlIn);
		bufSQL.append(" and pk_org in(select PK_RECEIVEORG from iufo_taskassign where pk_task='"+pk_task+"')");
		return bufSQL;
	}

	private static StringBuffer getKeycondTable(IUfoQueryCondVO queryCond,Set<String> vShowColumn,MeasurePubDataVO defaultPubData
			,KeyGroupVO keyGroup,String strTaskPK,String[] strRepPKs) throws Exception{
		StringBuffer bufSQL=new StringBuffer("select t1.alone_id,'"+strTaskPK+"' pk_task,");

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
		bufSQL.append("t2.pk_report from ");

		StringBuffer orgSQL=null;
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
		bufSQL.append(" inner join iufo_report t2");
		bufSQL.append(" on t2.pk_report in"+UFOString.getSqlStrByArr(strRepPKs));
		if(orgSQL == null || defaultPubData == null)
			bufSQL.append(" and t1.ver=0 ");
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
			bufSQL.append(" where t1.ver=0 and ");
			if (orgSQL!=null)
				bufSQL.append("t1.keyword1 in(").append(orgSQL).append(")");
			innerAddKeyCond(orgSQL!=null,bufSQL, queryCond, keyGroup);
		}

		return bufSQL;
	}

	private static String getNatDate(String strDate,KeyGroupVO keyGroup){
		if (strDate==null || keyGroup.getTimeKey()==null)
			return strDate;

		return new UFODate(strDate).getEndDay(keyGroup.getTimeProp()).toString();
	}

	private static void innerAddKeyCond(boolean bNeedAnd,StringBuffer bufSQL,IUfoQueryCondVO queryCond,KeyGroupVO keyGroup){
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

	private static StringBuffer getTaskCheckTable(String strTaskPK){
		StringBuffer bufSQL=new StringBuffer();
		bufSQL.append("select t2.aloneid,case max(coalesce(checktime,'#')) when '#' then '' else max(coalesce(checktime, '#')) " +
				"end checktime")
				.append(",case max(coalesce(t2.checkperson,'#')) when '#' then '' else max(coalesce(t2.checkperson, '#')) " +
		"end taskcheckperson");

		bufSQL.append(", min(t2.checkstate) checkstate from iufo_taskscheme t1,").append(IDatabaseNames.IUFO_SCHEME_CHECK).append("  t2");
		bufSQL.append(" where t1.pk_scheme = t2.schemepk and t1.scheme_type = 1");
		bufSQL.append(" and t1.pk_task='"+strTaskPK+"' group by t2.aloneid");
		return bufSQL;
	}

	private static boolean addOneColumn(boolean bNeedComma,StringBuffer bufSQL,String strColumn){
		if (bNeedComma)
			bufSQL.append(",");
		bufSQL.append(strColumn);
		return true;
	}

	private static String[][] splitRepPKsByRepCheckFml(String[] strRepPKs){
		List<String> vHasFormRepPK=new ArrayList<String>();
		List<String> vNoFormRepPK=new ArrayList<String>();

		ReportCache repCache=UFOCacheManager.getSingleton().getReportCache();
		for (String strRepPK:strRepPKs){
			ReportVO report=repCache.getByPK(strRepPK);
			if (report==null)
				continue;

			if (repCache.isExistCheckFormula(report))
				vHasFormRepPK.add(strRepPK);
			else
				vNoFormRepPK.add(strRepPK);
		}

		String[][] retRepPKs=new String[2][];
		retRepPKs[0]=vHasFormRepPK.toArray(new String[0]);
		retRepPKs[1]=vNoFormRepPK.toArray(new String[0]);
		return retRepPKs;
	}

	private static boolean isHasTaskFml(String strTaskPK) throws Exception{
		CheckSchemaVO[] schemes=TaskSrvUtils.getTaskCheckSchemeVOsByTaskId(strTaskPK);
		return schemes!=null && schemes.length>0;
	}

	private static String int2BoolChar(int iVal){
		return iVal==1?"'Y'":"'N'";
	}

	private static void addJoinKey(StringBuffer bufSQL,boolean bInnerJoin){
		bufSQL.append(bInnerJoin?" inner join ":" left outer join ");
	}
}
