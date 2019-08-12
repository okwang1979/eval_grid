package nc.impl.hbbb.contrast;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.hbbb.contrast.ContrastBO;
import nc.bs.hbbb.contrast.ContrastDMO;
import nc.bs.hbbb.contrast.ContrastOrgBO;
import nc.bs.iufo.data.MeasurePubDataBO;
import nc.itf.hbbb.contrast.IContrast;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.pub.hbbb.exception.UFOCUnThrowableException;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.exception.UFOSrvException;
import nc.pub.smart.util.SmartUtilities;
import nc.ui.hbbb.utils.HBRepStruUIUtil;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.util.hbbb.HBVersionUtil;
import nc.util.hbbb.hbscheme.HBSchemeSrvUtils;
import nc.util.hbbb.service.HBBaseDocItfService;
import nc.vo.bi.clusterscheduler.JobQueueVO;
import nc.vo.bi.clusterscheduler.SchedulerKeys;
import nc.vo.fi.pub.SqlUtils;
import nc.vo.hbbb.contrast.ContrastQryVO;
import nc.vo.hbbb.hbscheme.HBSchemeConstants;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.iufo.commit.RepDataCommitVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.mapping.IVOMappingMeta;
import nc.vo.iufo.mapping.VOMappingMeta;
import nc.vo.iufo.verctrl.IDataVersionConsts;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.BusinessException;
import nc.vo.vorg.ReportCombineStruMemberVersionVO;

public class ImpContrast implements IContrast{

	@Override
	public void doContrast(ContrastQryVO qryvo) throws BusinessException {
		new ContrastBO(qryvo).doContrast();
	}

	@Override
	public int doContrastBySubContrastorgs(ContrastQryVO qryvo) throws BusinessException {
		return new ContrastBO(qryvo).doContrastBySubContrastorgs();
	}
	
	/**
	 * 判断是否启用调度调度
	 * @create by zhoushuang at 2015-7-4,上午10:50:52
	 * 
	 * @param pk_hbrepstru
	 * @param pk_contrastorg
	 * @return
	 * @throws BusinessException 
	 */
	public boolean isStartSchedule(String pk_hbrepstru, String pk_contrastorg) throws BusinessException {
		return ContrastBO.isStartSchedule(pk_hbrepstru,pk_contrastorg);
	}
	
	/**
	 * 确定对账的公司对
	 * @create by jiaah at 2011-12-30,上午10:32:32
	 * @param pk_hbrepstru 报表合并体系版本主键
	 * @param innercode 当前对账组织innercode
	 * @return
	 * @throws BusinessException
	 */
	public String[] getContrastOrgs(String pk_hbrepstru, String pk_contrastorg) throws BusinessException {
		return ContrastBO.getContrastOrgs(pk_hbrepstru, pk_contrastorg);
	}
		
	
//	@Override
//	public void batchdoContrast(ContrastQryVO qryvo) throws BusinessException {
//		//批量对账
//		new ContrastBO(qryvo).batchdoContrast();
//	}

	@Override
	public void checkCommitOrCheckStatus(String pk_hbschme, String pk_org,
			Map<String, String> keyMap) throws BusinessException {
		checkReporCommitState(pk_hbschme, pk_org, keyMap);
	}
	
	/**
	 *
	 * 方法说明：检查报表上报状态(jiaah):对账和合并的时候有校验
	 * <p>修改记录：</p>
	 * @param schemevo
	 * @param pk_keygroup
	 * @param pk_org
	 * @param innercode
	 * @param keyMap
	 * @throws BusinessException
	 * @since V6.0
	 */
	private void checkReporCommitState(String pk_hbschme,String pk_org,Map<String, String> keyMap) throws BusinessException {
		HBSchemeVO schemevo=HBSchemeSrvUtils.getHBSchemeByHBSchemeId(pk_hbschme);

		if(schemevo==null){
			  throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1835005_0","01830001-0278")/*@res请选择合并方案!*/);
		}
		String ispermithb = schemevo.getIspermithb();
		if(ispermithb == null || HBSchemeConstants.ISPERMITHB_IS.equals(ispermithb)) return;
		MeasurePubDataVO pubdata = new MeasurePubDataVO();
		KeyGroupVO keyGroupVo = IUFOCacheManager.getSingleton().getKeyGroupCache().getByPK(schemevo.getPk_keygroup());
		pubdata.setKType(keyGroupVo.getKeyGroupPK());
		pubdata.setKeyGroup(keyGroupVo);
		if (null != keyMap && keyMap.size() > 0) {
			String[] keys = new String[keyMap.size()];
			keyMap.keySet().toArray(keys);
			if (null != keys && keys.length > 0) {
				for (String key : keys) {
					pubdata.setKeywordByPK(key, keyMap.get(key));
				}
			}
		}

		pubdata.setKeywordByPK(KeyVO.CORP_PK, pk_org);
//		HBSchemeVO schemevo=HBSchemeSrvUtils.getHBSchemeByHBSchemeId(hbScheme);
		//调整方案可能存在为空,为空的情况,业务上可能就是使用个别报表
//		String pk_adjustscheme = schemevo.getPk_adjustscheme();
		int[] needtoCheckVersion = null;
//		if(pk_adjustscheme!=null){
////			AdjustSchemeVO adjustSchemeVo = (AdjustSchemeVO)HBProxy.getRemoteUAPQueryBS().retrieveByPK(AdjustSchemeVO.class, pk_adjustscheme);
//			needtoCheckVersion = new int[]{HBVersionUtil.getHBAdjustByHBSchemeVO(schemevo),schemevo.getVersion()};
//		}else{
			needtoCheckVersion = new int[]{HBVersionUtil.getVersion(schemevo, IDataVersionConsts.VERTYPE_HBBB, false),HBVersionUtil.getVersion(schemevo, IDataVersionConsts.VERTYPE_HBBB_ADJUST, false)};
//		}
		//delete-----待检查上报报表版本,合并报表调整表,合并报表,个别报表调整表,个表报表
		//待检查上报报表版本:合并报表调整表,合并报表
		Set<String>   org_aloneidMap = new HashSet<String>();
		String innercode="";
		String pk_hbrepstru = HBRepStruUIUtil.getHBRepStruPK(keyMap, schemevo);
		ReportCombineStruMemberVersionVO[] vos = HBBaseDocItfService.getRemoteHBRepStru().queryReportCombineStruMemberVersionByVersionId(pk_hbrepstru);	
//		ReportManaStruMemberVO[] queryReportManaStruMemberVOSByReportManaStruID = NCLocator.getInstance().lookup(IReportManaStruMemberQryService.class).queryReportManaStruMemberVOSByReportManaStruID(schemevo.getPk_repmanastru());
		for (int i = 0; i < vos.length; i++) {
			ReportCombineStruMemberVersionVO reportManaStruMemberVO = vos[i];
			if(reportManaStruMemberVO.getPk_org().equals(pk_org)){
				innercode = reportManaStruMemberVO.getInnercode();
				break;
			}
		}
		for (int i = 0; i < vos.length; i++) {
			ReportCombineStruMemberVersionVO reportManaStruMemberVO = vos[i];
			if(reportManaStruMemberVO.getInnercode().startsWith(innercode)){
				for (int h = 0; h < needtoCheckVersion.length; h++) {
					int j = needtoCheckVersion[h];
					pubdata.setAloneID(null);
					pubdata.setVer(j);
					MeasurePubDataVO findByKeywords;
					String strAloneID ="";
					pubdata.setKeywordByPK(KeyVO.CORP_PK, reportManaStruMemberVO.getPk_org());
					try {
						findByKeywords = MeasurePubDataBO_Client.findByKeywords(pubdata);
						strAloneID  = findByKeywords.getAloneID();
					} catch (Exception e) {
						nc.bs.logging.Logger.error(e.getMessage(), e);
					}
					if(!StringUtil.isEmptyWithTrim(strAloneID)){
						org_aloneidMap.add(strAloneID);
						break;
					}
				}
			}
		}
		
		String aloneidwhere1 = SqlUtils.getInStr(RepDataCommitVO.ALONE_ID, org_aloneidMap.toArray(new String[0]), true);
//		String sql1 = "select count(*) from ufoc_adjreport " +
//				"left join ufoc_rep_commit " +
//				"on ufoc_rep_commit.pk_hbscheme=ufoc_adjreport.pk_hbscheme and ufoc_rep_commit.alone_id=ufoc_adjreport.aloneid and ufoc_rep_commit.pk_report=ufoc_adjreport.pk_report " +
//				"where ufoc_adjreport.pk_hbscheme='" + pk_hbschme + "' and " + aloneidwhere1 + " and isnull(commit_state,0) != 23 ";
		String sql1 = "select count(*) from iufo_rep_commit " +
				"where iufo_rep_commit.pk_task='" + pk_hbschme + "' and " + aloneidwhere1 + " and isnull(commit_state,0) != 23 ";
		Integer result1 = (Integer) HBBaseDocItfService.getRemoteUAPQueryBS().executeQuery(sql1, new ColumnProcessor());
		if(result1 != null && result1.intValue() > 0){
			throw new UFOCUnThrowableException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0153")/*@res "下级单位的单位报表需上报!"*/);
		}
		String aloneidwhere2 = SqlUtils.getInStr(RepDataCommitVO.ALONE_ID, org_aloneidMap.toArray(new String[0]), true);
//		String sql2 = "select count(*) from ufoc_hbreport " +
//		"left join ufoc_rep_commit " +
//		"on ufoc_rep_commit.pk_hbscheme=ufoc_hbreport.pk_hbscheme and ufoc_rep_commit.alone_id=ufoc_hbreport.alone_id and ufoc_rep_commit.pk_report=ufoc_hbreport.pk_report " +
//		"where ufoc_hbreport.pk_hbscheme='" + pk_hbschme + "' and " + aloneidwhere2 + " and isnull(commit_state,0) != 23 ";
		String sql2 = "select count(*) from iufo_rep_commit " +
				"where iufo_rep_commit.pk_task='" + pk_hbschme + "' and " + aloneidwhere2 + " and isnull(commit_state,0) != 23 ";
		Integer result2 = (Integer) HBBaseDocItfService.getRemoteUAPQueryBS().executeQuery(sql2, new ColumnProcessor());
		if(result2 != null && result2.intValue() > 0){
			throw new UFOCUnThrowableException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0153")/*@res "下级单位的单位报表需上报!"*/);
		}
		
//		//没有上报信息的报表
//		sql += " and (exists(select 1 from ufoc_adjreport where ufoc_adjreport.pk_hbscheme=ufoc_rep_state.pk_hbscheme and ))" 
//		//需要检查上报状态的报表
//		String[] reportIdByHBSchemeId = NCLocator.getInstance().lookup(IHBSchemeQrySrv.class).getReportIdByHBSchemeId(schemevo.getPk_hbscheme());
//		//上报的记录条数
//		int reportsendnum = reportIdByHBSchemeId.length*org_aloneidMap.values().size();
//		Set<String> keySet = org_aloneidMap.keySet();//报表组织
//		//检查上报状态
//		String reportWhere = SqlUtils.getInStr(nc.vo.hbbb.commit.UFOCRepDataStateVO.PK_REPORT, reportIdByHBSchemeId, false);
//		String aloneidwhere = SqlUtils.getInStr(nc.vo.hbbb.commit.UFOCRepDataStateVO.ALONE_ID, org_aloneidMap.values().toArray(new String[0]), true);
//		String wherecondition = reportWhere + "  and "+aloneidwhere+" and commit_state = 1";
//		//FIXME:直接countSQL
//		Collection<nc.vo.hbbb.commit.UFOCRepDataStateVO> rcols=	HBProxy.getRemoteUAPQueryBS().retrieveByClause(nc.vo.hbbb.commit.UFOCRepDataStateVO.class, wherecondition );
//		if(rcols.size()<reportsendnum){
//			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0153")/*@res "下级单位的单位报表需上报!"*/);
//		}
	}

	@Override
	public void clearContrastedData(ContrastQryVO qryvo)
			throws BusinessException {
		// TODO Auto-generated method stub
		new ContrastBO(qryvo).clearContrastedData();;
	}

	@Override
	public int waitForJobComplete(String jobId) throws UFOSrvException {
		if(jobId==null || jobId.length()<=0){
			return -1;
		}
		try {
			String dsName=SmartUtilities.getDefDsName();
			BaseDAO baseDao = new BaseDAO(dsName);
			IVOMappingMeta meta = VOMappingMeta.getMappingMeta(JobQueueVO.class);
			Object result=null;
			JobQueueVO job=null;
			while(true){
				result=baseDao.retrieveByPK(JobQueueVO.class, meta, jobId, new String[]{"runstate"});
				if (result==null || !(result instanceof JobQueueVO)){
					return -1;
				}
				job=(JobQueueVO)result;
				if(job.getRunstate()==SchedulerKeys.STATE_COMPLETE ||
						job.getRunstate()==SchedulerKeys.STATE_ERROR){
					return job.getRunstate();
				}
				synchronized(this){
					try{
						wait(1500);
					}catch(Exception e){}
				}
			}
		} catch (DAOException e) {
			throw new UFOSrvException(e.getMessage(), e);
		}
		
	}
	
	@Override
	public Set<String> getcontrastOrg(String sql,boolean isDICCORP,Map<String,String> orgSupplier) throws BusinessException{
		try {
			return new ContrastDMO().getcontrastOrg(sql, isDICCORP, orgSupplier);
		} catch (SQLException e) {
			throw new BusinessException(e.getMessage());
		}
	}
	
	@Override
	public String createTempTablebyContrastOrgs(List<String[]> selfOppOrgs) throws BusinessException {
		try {
			String tempTable = new ContrastOrgBO().createTempTablebyContrastOrgs(selfOppOrgs);
			return tempTable;
		} catch (SQLException e) {
			throw new BusinessException(e.getMessage());
		}
	}

	@Override
	public MeasurePubDataVO[] findPubDataByContrastOrgs(String pk_keygroup, List<String[]> selfOppOrgs)
			throws BusinessException {
		try {
			String tempTable = new ContrastOrgBO().createTempTablebyContrastOrgs(selfOppOrgs);
			String whereSql = "(keyword1, keyword2,keyword3) in (select self_org, duration, opp_org from " + tempTable + ")";
			MeasurePubDataVO[] simplifiedPubData = new MeasurePubDataBO().findBySqlCondition(pk_keygroup, whereSql);
			return simplifiedPubData;
		} catch (SQLException e) {
			throw new BusinessException(e.getMessage());
		}
	}

}
