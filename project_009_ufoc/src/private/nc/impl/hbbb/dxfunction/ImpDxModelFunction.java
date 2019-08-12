package nc.impl.hbbb.dxfunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.hbbb.dxmodelfunction.CESUMBO;
import nc.bs.hbbb.dxmodelfunction.DPSUMBO;
import nc.bs.hbbb.dxmodelfunction.ESELECTBO;
import nc.bs.hbbb.dxmodelfunction.INTRBO;
import nc.bs.hbbb.dxmodelfunction.INTRBYKEYBO;
import nc.bs.hbbb.dxmodelfunction.IPROPORTIONBO;
import nc.bs.hbbb.dxmodelfunction.OPCEBO;
import nc.bs.hbbb.dxmodelfunction.PTPSUMBO;
import nc.bs.hbbb.dxmodelfunction.SREPBO;
import nc.bs.hbbb.dxmodelfunction.TPSUMBO;
import nc.bs.hbbb.dxmodelfunction.UCHECKBO;
import nc.itf.hbbb.dxfunction.IDxModelFunction;
import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jdbc.framework.processor.ArrayProcessor;
import nc.pub.iufo.cache.CacheManager;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.MeasureCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.exception.UFOSrvException;
import nc.pub.iufo.processor.IDQryProcessor;
import nc.util.hbbb.UFOCSqlUtil;
import nc.util.hbbb.hbscheme.HBSchemeSrvUtils;
import nc.util.hbbb.processor.DoubleZeroQryProcessor;
import nc.util.hbbb.pub.util.StrTools;
import nc.util.hbbb.service.HBBaseDocItfService;
import nc.vo.hbbb.dxfunction.ESLECTQryVO;
import nc.vo.hbbb.dxfunction.OPCEQryVO;
import nc.vo.hbbb.dxfunction.TPSUMQryVO;
import nc.vo.hbbb.dxfunction.project.MeasureReportVO;
import nc.vo.hbbb.hbscheme.HBSchemeReportVO;
import nc.vo.iufo.calculate.DatePropVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasurePackVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.pub.BusinessException;
import nc.vo.ufoc.unionproject.MeasProjectVO;
import nc.vo.ufoc.unionproject.ProjectVO;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.script.base.ICalcEnv;

/**
 * @modified by jiaah at 2012-1-4 同步60
 *
 */
public class ImpDxModelFunction implements IDxModelFunction {  

	  

	@Override
	public double getINTR(String projectcode,int isself,int offset, ICalcEnv env) throws BusinessException{
		return new INTRBO().getINTR(projectcode, isself, offset, env);
	}

	@Override
	public double getOPCE(OPCEQryVO qryvo , ICalcEnv env) throws BusinessException {
		return  new OPCEBO().getOPCE(qryvo,env);
	}

	

	@Override
	public double getSINTR(String pk_account) throws BusinessException {
		return 0;
	}


	@Override 
	public double getUCHECK(String accountcode,int isself, ICalcEnv env) throws BusinessException{
		return new UCHECKBO().getUCHECK(accountcode, isself, env);
	}

	@Override
	public double getSREP(String projectcode,int isself,int offset, ICalcEnv env)
			throws BusinessException {
		return  new SREPBO().getSREP(projectcode, isself, offset, env);
	}

	@Override
	public double getCESUM(String projectcode, int cur_direction,
			 int offset, ICalcEnv env) throws BusinessException {
		return new CESUMBO().getCESUM(projectcode, cur_direction, offset, env);
	}

	@Override
	public double getDPSUM(TPSUMQryVO qryvo, ICalcEnv env)
			throws BusinessException {
		return new DPSUMBO().getDPSUM(qryvo, env);
	}

	@Override
	public double getPTPSUM(TPSUMQryVO qryvo, ICalcEnv env)
			throws BusinessException {
		return new PTPSUMBO().getPTPSUM(qryvo, env);
	}

	@Override
	public double getTPSUM(TPSUMQryVO qryvo, ICalcEnv env)
			throws BusinessException {
		return new TPSUMBO().getTPSUM(qryvo, env);
	}








	@Override
	public double getInvestSumData(String pk_investor, String pk_investee,
			String strdate, String pk_investscheme) throws BusinessException {
		double result=0;
//		String sql="select sum(investproportion) from org_stockinvest where investor=? and  investee=?  and investdate<=? and isnull(dr,0)=0  and assessmode = 0";
		String sql="select sum(investproportion) from org_stockinvest where pk_investscheme = ? and investor=? and  investee=?  and investdate<=? and isnull(dr,0)=0  ";
		SQLParameter param = new SQLParameter();
		param.addParam(pk_investscheme);
		param.addParam(pk_investor);
		param.addParam(pk_investee);
		param.addParam(strdate);
	    result = (Double)HBBaseDocItfService.getRemoteUAPQueryBS().executeQuery(sql, param,new DoubleZeroQryProcessor());
		return result;
	}
	
	@Override
	public double getInvestSumDataByAssmode(String pk_investor, String pk_investee,
			String strdate, String pk_investscheme) throws BusinessException {
		double result=0;
		String sql="select sum(investproportion) from org_stockinvest where pk_investscheme = ? and investor=? and  investee=?  and investdate<=? and isnull(dr,0)=0  and assessmode = 0";
		SQLParameter param = new SQLParameter();
		param.addParam(pk_investscheme);
		param.addParam(pk_investor);
		param.addParam(pk_investee);
		param.addParam(strdate);
	    result = (Double)HBBaseDocItfService.getRemoteUAPQueryBS().executeQuery(sql, param,new DoubleZeroQryProcessor());
		return result;
	}


	@SuppressWarnings("unchecked")
	@Override
	public MeasureReportVO getMeasRepBySchemeProjectCode(String pk_hbScheme,
			String projectCode, String pk_group ,boolean isintrade) throws BusinessException {
		// TODO Auto-generated method stub
		MeasureReportVO result=null;
		
//		(Class className,  
//				
				StringBuilder condition=new StringBuilder();
		        condition.append(MeasProjectVO.PK_PROJECT).append(" in (");
		        condition.append("select pk_project from ufoc_project where code=? and ( pk_group=?   or pk_org = 'GLOBLE00000000000000') and  isnull(dr,0)=0 )  and ");
		        condition.append(MeasProjectVO.PK_REPORT).append("  in (" );
		        condition.append("select pk_report from iufo_hbsch_report where pk_hbscheme=?  and isnull(dr,0)=0 ) and isnull(dr,0)=0   ");
		        
		        SQLParameter param = new SQLParameter();
				param.addParam(projectCode);
				param.addParam(pk_group);
				param.addParam(pk_hbScheme);
				Collection<MeasProjectVO> list= HBBaseDocItfService.getRemoteUAPQueryBS().retrieveByClause(MeasProjectVO.class, condition.toString(), new String[]{MeasProjectVO.PK_MEASURE,MeasProjectVO.PK_REPORT}, param);
		        if(null!=list && list.size()>0){
		        	MeasProjectVO[] vos=new MeasProjectVO[list.size()];
		        	list.toArray(vos);
		        	if(vos.length==1){  //处理单个影射的情形
		        		result=new MeasureReportVO();
		        		result.setPk_report(vos[0].getPk_report());
		        		 MeasureCache meascache=  (MeasureCache) CacheManager.getCache(MeasurePackVO.class);
//		        	        MeasureVO measVO =meascache.l  .loadMeasuresByName(vos[0].getPk_report(),  measname);
		        		MeasureVO measvo=meascache.getMeasure(vos[0].getPk_measure());
		        		result.setMeasVO(measvo);
		        		if(isintrade){
		        			StringBuilder sqlwhere=new StringBuilder();
		        			sqlwhere.append(" pk_report='").append(measvo.getReportPK()).append("'  and isnull(dr,0)=0  and isintrade='Y'");
		        			ReportVO[] tvos= getReportsBySQL(sqlwhere.toString());
		        			if(null!=tvos && tvos.length==1){
		                		 return result;
		        			}else{
		        				return null;
		        			}
		        		}else{
		        			StringBuilder sqlwhere=new StringBuilder();
		        			sqlwhere.append(" pk_report='").append(measvo.getReportPK()).append("'  and isnull(dr,0)=0  and isintrade='N'");
		        			ReportVO[] tvos= getReportsBySQL(sqlwhere.toString());
		        			if(null!=tvos && tvos.length==1){
		                		  return result;
		        			}else{
		        				return null;
		        			}
		        		}
		        	}else{ //处理多个映射的情形
		        		return this.processMultiMap(vos, pk_hbScheme,isintrade);
		        	}
		        }
				
		
		return result;
	}
	
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, MeasureReportVO> getMeasRepsBySchemeProjCode(String pk_hbScheme, String pk_group,
            Map<String, Boolean> proIntradeMap) throws BusinessException {
        Map<String, MeasureReportVO> proCodeMeasRepMap = new HashMap<String, MeasureReportVO>();
        if (proIntradeMap == null || proIntradeMap.size() <= 0) {
            return null;
        }

        BaseDAO baseDAO = new BaseDAO();
        // projectCode条件
        String codeInSql = UFOCSqlUtil.buildInSql("code", proIntradeMap.keySet());

        // ProIdCodeMap<projectId, projectCode>
        Map<String, String> proIdCodeMap = new HashMap<String, String>();
        Collection<ProjectVO> projectVOs = baseDAO.retrieveByClause(ProjectVO.class, codeInSql);
        if (projectVOs != null && projectVOs.size() > 0) {
            for (ProjectVO projectVO : projectVOs) {
                proIdCodeMap.put(projectVO.getPk_project(), projectVO.getCode());
            }
        }

        // 查询MeasProjectVO
        StringBuilder condition = new StringBuilder();
        condition.append(" pk_project in (");
        condition.append(" select pk_project from ufoc_project ");
        condition.append(" where ").append(codeInSql);
        condition.append(" and ( pk_group=? or pk_org = 'GLOBLE00000000000000') and  isnull(dr,0)=0 ) ");
        condition.append(" and pk_report in (");
        condition.append(" select pk_report from iufo_hbsch_report ");
        condition.append(" where pk_hbscheme=? and isnull(dr,0)=0 ) and isnull(dr,0)=0 ");

        SQLParameter params = new SQLParameter();
        params.addParam(pk_group);
        params.addParam(pk_hbScheme);

        Collection<MeasProjectVO> measProjectVOs = baseDAO.retrieveByClause(MeasProjectVO.class, condition.toString(),
                new String[] { MeasProjectVO.PK_PROJECT, MeasProjectVO.PK_MEASURE, MeasProjectVO.PK_REPORT }, params);

        if (measProjectVOs != null && measProjectVOs.size() > 0) {
            // 报表内部交易表信息
            Map<String, Boolean> reportIntradeMap = genReportIntradeMap(pk_hbScheme);
            // 报表顺序信息
            Map<String, Integer> reportPositionMap = genReportPositionMap(pk_hbScheme);
            // 指标缓存
            MeasureCache measureCache = IUFOCacheManager.getSingleton().getMeasureCache();
            for (MeasProjectVO measProjectVO : measProjectVOs) {
                String projectCode = proIdCodeMap.get(measProjectVO.getPk_project());

                String pk_report = measProjectVO.getPk_report();
                // 是否内部交易表不匹配
                if (proIntradeMap.get(projectCode) ^ reportIntradeMap.get(pk_report)) {
                    continue;
                } else if (proCodeMeasRepMap.get(projectCode) != null) {
                    // 是否已有位置更靠前的报表
                    String pk_reptmp = proCodeMeasRepMap.get(projectCode).getPk_report();
                    if (reportPositionMap.get(pk_reptmp).compareTo(reportPositionMap.get(pk_report)) < 0) {
                        continue;
                    }
                }
                // measRepVO
                MeasureReportVO measRepVO = new MeasureReportVO();
                measRepVO.setPk_report(pk_report);
                // 指标
                MeasureVO measVO = measureCache.getMeasure(measProjectVO.getPk_measure());
                measRepVO.setMeasVO(measVO);
                proCodeMeasRepMap.put(projectCode, measRepVO);
            }
        }
        return proCodeMeasRepMap;
    }
    
    /**
     * 报表内部交易表信息
     * 
     * @param hbSchemeId
     * @return
     * @throws UFOSrvException
     */
    private Map<String, Boolean> genReportIntradeMap(String hbSchemeId) throws UFOSrvException {
        String[] reportIds = HBSchemeSrvUtils.getReportIdByHBSchemeId(hbSchemeId);
        ReportVO[] reportVOs = IUFOCacheManager.getSingleton().getReportCache().getByPks(reportIds);
        Map<String, Boolean> reportIntradeMap = new HashMap<String, Boolean>();
        for (ReportVO reportVO : reportVOs) {
            reportIntradeMap.put(reportVO.getPk_report(), reportVO.getIsintrade() != null ? reportVO.getIsintrade()
                    .booleanValue() : false);
        }
        return reportIntradeMap;
    }

    /**
     * 报表顺序信息
     * 
     * @param hbSchemeId
     * @return
     * @throws UFOSrvException
     */
    private Map<String, Integer> genReportPositionMap(String hbSchemeId) throws UFOSrvException {
        HBSchemeReportVO[] hbSchemeReportVOs = HBSchemeSrvUtils.getHBReportVOByHBSchemeId(hbSchemeId);
        Map<String, Integer> reportPositionMap = new HashMap<String, Integer>();
        for (HBSchemeReportVO hbSchemeReportVO : hbSchemeReportVOs) {
            reportPositionMap.put(hbSchemeReportVO.getPk_report(), hbSchemeReportVO.getPosition());
        }
        return reportPositionMap;
    }
	
	/**
	 * 根据查询条件查询报表
	 * @param sqlwhere
	 * @return
	 * @throws UFOSrvException
	 */
	private ReportVO[] getReportsBySQL(String sqlwhere) throws UFOSrvException{
		try {
			String sql = "select pk_report from iufo_report where " + sqlwhere;
		    String[] aryRepIds = (String[])((IUAPQueryBS) nc.bs.framework.common.NCLocator.getInstance().lookup(IUAPQueryBS.class.getName())).
		    	executeQuery(sql, new IDQryProcessor());

			if(aryRepIds != null && aryRepIds.length > 0){
				return UFOCacheManager.getSingleton().getReportCache().getByPks(aryRepIds);
			}else
				return new ReportVO[0];
		} catch (BusinessException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage(),e);
		}

	}
	
	private MeasureReportVO processMultiMap(MeasProjectVO[] vos,String pk_hbScheme,boolean isintrade) throws BusinessException {
//		MeasureReportVO result=null;
		ArrayList<String> strList=new ArrayList<String>();
		for(MeasProjectVO vo:vos){
			strList.add(vo.getPk_report());
		}
		String[] reports=new String[strList.size()];
		strList.toArray(reports);
		
		//首先根据是否内部交易表建立确定映射关系
	
		
		if(isintrade){
			StringBuilder sqlwhere=new StringBuilder();
			sqlwhere.append(" pk_report in (").append(StrTools.getValuesFromStrArrayForSql(reports)).append(")  and isnull(dr,0)=0  and isintrade='Y'");
			ReportVO[] tvos= getReportsBySQL(sqlwhere.toString());
			if(null!=tvos && tvos.length==1){
        		   return this.getMatchMeasVo(vos, tvos[0].getPk_report());
			}else{
       				MeasProjectVO[] vos1 = new MeasProjectVO[tvos.length];
       				List<String> pk_reports = new ArrayList<String>();
       				for(ReportVO vo :tvos){
       					pk_reports.add(vo.getPk_report());
       				}
       				int i = 0;
       				for(MeasProjectVO vo : vos){
       					if(pk_reports.contains(vo.getPk_report())){
       						vos1[i] = vo;
       						i++;
       					}
       				}
       				vos = vos1;
       				reports = pk_reports.toArray(new String[0]);
	   
			}
		}else{
			StringBuilder sqlwhere=new StringBuilder();
			sqlwhere.append(" pk_report in (").append(StrTools.getValuesFromStrArrayForSql(reports)).append(")  and isnull(dr,0)=0  and isintrade='N'");
			ReportVO[] tvos= getReportsBySQL(sqlwhere.toString());
			if(null!=tvos && tvos.length==1){
        		   return this.getMatchMeasVo(vos, tvos[0].getPk_report());
			}
		}
		
		StringBuilder content=new StringBuilder();
		content.append("SELECT pk_report ");
		content.append("  FROM iufo_hbsch_report ");
		content.append(" WHERE POSITION = (SELECT MIN (POSITION) ");
		content.append("                     FROM iufo_hbsch_report ");
		content.append("                    WHERE pk_hbscheme = ?  and isnull(dr,0)=0 and pk_report in (").append(StrTools.getValuesFromStrArrayForSql(reports)).append(")) AND pk_hbscheme = ?  and isnull(dr,0)=0  and pk_report in  (").append(StrTools.getValuesFromStrArrayForSql(reports)).append(")");
		 SQLParameter param1 = new SQLParameter();
			
		 param1.addParam(pk_hbScheme);
		 param1.addParam(pk_hbScheme);
		String[] ids=(String[])HBBaseDocItfService.getRemoteUAPQueryBS().executeQuery(content.toString(), param1, new IDQryProcessor());
		return this.getMatchMeasVo(vos, ids[0]);
//		return result;
	}

	
	private MeasureReportVO  getMatchMeasVo(MeasProjectVO[] vos,String pk_report){
		MeasureReportVO result=null;
		for(MeasProjectVO vo:vos){
			if(vo.getPk_report().equals(pk_report)){
				result=new MeasureReportVO();
        		result.setPk_report(vo.getPk_report());
        		 MeasureCache meascache=  (MeasureCache) CacheManager.getCache(MeasurePackVO.class);
        		MeasureVO measvo=meascache.getMeasure(vo.getPk_measure());
        		result.setMeasVO(measvo);
        		return result;
			}
		}
		return result;
	}


	@Override
	public double getESELECT(ESLECTQryVO qryvo, ICalcEnv env)
			throws BusinessException {
		// TODO Auto-generated method stub
		return new ESELECTBO().getESELECT(qryvo, env);
	}



	@SuppressWarnings("unchecked")
	@Override
	public MeasureReportVO getMeasRepBySchemeProject(String pk_hbScheme,
			String pk_project, String pk_group, boolean isintrade)
			throws BusinessException {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		MeasureReportVO result=null;
		
//		(Class className,  
//				
				StringBuilder condition=new StringBuilder();
		        condition.append(MeasProjectVO.PK_PROJECT).append(" in (");
		        condition.append("select pk_project from ufoc_project where pk_project=? and ( pk_group=?  or   pk_org ='GLOBLE00000000000000') and  isnull(dr,0)=0 )  and ");
		        condition.append(MeasProjectVO.PK_REPORT).append("  in (" );
		        condition.append("select pk_report from iufo_hbsch_report where pk_hbscheme=?  and isnull(dr,0)=0 ) and isnull(dr,0)=0   ");
		        
		        SQLParameter param = new SQLParameter();
				param.addParam(pk_project);
				param.addParam(pk_group);
				param.addParam(pk_hbScheme);
				Collection<MeasProjectVO> list= HBBaseDocItfService.getRemoteUAPQueryBS().retrieveByClause(MeasProjectVO.class, condition.toString(), new String[]{MeasProjectVO.PK_MEASURE,MeasProjectVO.PK_REPORT}, param);
		        if(null!=list && list.size()>0){
		        	MeasProjectVO[] vos=new MeasProjectVO[list.size()];
		        	list.toArray(vos);
		        	if(vos.length==1){  //处理单个影射的情形
		        		result=new MeasureReportVO();
		        		result.setPk_report(vos[0].getPk_report());
		        		 MeasureCache meascache=  (MeasureCache) CacheManager.getCache(MeasurePackVO.class);
//		        	        MeasureVO measVO =meascache.l  .loadMeasuresByName(vos[0].getPk_report(),  measname);
		        		MeasureVO measvo=meascache.getMeasure(vos[0].getPk_measure());
		        		result.setMeasVO(measvo);
		        		return result;
		        	}else{ //处理多个映射的情形
		        		return this.processMultiMap(vos, pk_hbScheme,isintrade);
		        	}
		        }
				
		
		return result;
	}
	
	@Override
	public double getIPROPORTION(DatePropVO datevo, int offset,ICalcEnv env)
			throws BusinessException {
		return new IPROPORTIONBO().getIPROPORTION(datevo, offset,env);
	}

	@Override
	public String queryPKChooseKeyBYCode(KeyVO keyVo, String code) throws BusinessException {
		// TODO Auto-generated method stub
		String result = null;

		if(keyVo != null && code != null){
			String tabName = keyVo.getDetailtable();
			if (tabName != null){
				StringBuffer sql = new StringBuffer();
				sql.append(" select keyval from ");
				sql.append(tabName.toString());
				sql.append(" where code = '");			
				sql.append(code);
				sql.append("'");

				Object[] value = (Object [])new BaseDAO().executeQuery(sql.toString(),new ArrayProcessor()); 
				if(getArrayLength(value) > 0 && value[0] != null){
					result = value[0].toString();
				}
			}
		}
		return result;
	}
	
	//获得数据的长度
		private static int getArrayLength(Object[] value) {
			// TODO Auto-generated method stub
			return value==null ? 0 : value.length;
		}

		@Override
		public double getINTRBYKEY(String projectcode,int isself,int offset, String[] otherDynKeyToVal, ICalcEnv env) throws BusinessException{
			return new INTRBYKEYBO().getINTRBYKEY(projectcode, isself, offset, otherDynKeyToVal, env);
		}
	

		public Object queryChooseKeyValue(String sql) throws BusinessException{
//			StringBuffer table = new StringBuffer();
//			table.append(" select detailtable from iufo_keyword where pk_keyword = '");
//			table.append(tableName.toString());
//			table.append("'");
//			String tabName = null;
//			Object tablevalue = new BaseDAO().executeQuery(table.toString(), new ArrayListProcessor());
//			for (Object resultValue : (List)tablevalue) {
//				Object[] val = (Object[])resultValue;
//				tabName = val[0].toString();
//			}
//			StringBuffer sql = new StringBuffer();
//			sql.append(" select keyval,code,name1,name2 from ");
//			sql.append(tabName.toString());
			Object value = new BaseDAO().executeQuery(sql.toString(), new ArrayListProcessor());

			return value;
		}
		

}
