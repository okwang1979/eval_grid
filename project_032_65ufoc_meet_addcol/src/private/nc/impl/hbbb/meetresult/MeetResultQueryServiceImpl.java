package nc.impl.hbbb.meetresult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.itf.hbbb.meetresult.IMeetResultQueryService;
import nc.jdbc.framework.processor.BaseProcessor;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.md.persist.framework.MDPersistenceService;
import nc.util.hbbb.HBBBLangUtil;
import nc.util.hbbb.UFOCSqlUtil;
import nc.util.hbbb.UFOCVOUtil;
import nc.util.hbbb.UfocPkBatchGetter;
import nc.util.hbbb.contrast.ContrastMeetFilterUtil;
import nc.util.hbbb.param.HBBBParamUtil;
import nc.vo.hbbb.meetaccount.AggMeetRltHeadVO;
import nc.vo.hbbb.meetaccount.MeetResultBodyVO;
import nc.vo.hbbb.meetaccount.MeetResultHeadVO;
import nc.vo.org.OrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.sm.UserVO;
import nc.vo.ufoc.unionproject.ProjectVO;

import org.apache.commons.lang.StringUtils;

/**
 * 对账记录查询类
 * @author jiaah
 * @created at 2013-7-18,上午9:07:35
 *
 */
public class MeetResultQueryServiceImpl implements IMeetResultQueryService {
	
	@SuppressWarnings("unchecked")
	@Override
	public MeetResultBodyVO[] queryMeetBodyVoByCondition(String sqlWhere)throws BusinessException{	
		int max_count = 5000;
		int count = getAggVOCount(sqlWhere); 
		BaseDAO dao = new BaseDAO();
		if(count > max_count) {
			List<MeetResultBodyVO> bodyList = new ArrayList<MeetResultBodyVO>();
			
			UfocPkBatchGetter pkGetter = new UfocPkBatchGetter(MeetResultHeadVO.getDefaultTableName(), 
					MeetResultHeadVO.PK_TOTALINFO, "MEETDATA_HEAD", sqlWhere, null, max_count);
			
			while(pkGetter.hasNext()) {
				String[] pks = pkGetter.next();
				if(pks == null || pks.length == 0) 
					continue;
				StringBuilder bf = new StringBuilder();
				bf.append(UFOCSqlUtil.buildInSql(MeetResultBodyVO.DETAILS, pks)).append(" and measurecode is not null");
				List<MeetResultBodyVO> list = (List<MeetResultBodyVO>) dao.retrieveByClause(MeetResultBodyVO.class, bf.toString(), 
						new String[]{MeetResultBodyVO.PK_OPPORG,MeetResultBodyVO.ADJUST_AMOUNT,MeetResultBodyVO.MEASURECODE,MeetResultBodyVO.PK_SELFORG});
				if(list != null)
					bodyList.addAll(list);
			}
			pkGetter.distroy();
			return bodyList.toArray(new MeetResultBodyVO[bodyList.size()]);
		}
		else {
			List<MeetResultBodyVO> bodyList = new ArrayList<MeetResultBodyVO>();
			List<AggMeetRltHeadVO> list = (List<AggMeetRltHeadVO>) MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByCond(AggMeetRltHeadVO.class, sqlWhere, false);
			if(list != null){
				for(AggMeetRltHeadVO headvo : list){
					bodyList.addAll(Arrays.asList((MeetResultBodyVO[])headvo.getChildrenVO()));
				}
			}
			return bodyList.toArray(new MeetResultBodyVO[bodyList.size()]);
		}
	}
	
	
	

	@Override
	public AggMeetRltHeadVO[] queryAggMeetResultByCondition(String sqlWhere)
			throws BusinessException {
		int max_count = 5000;
		int count = getAggVOCount(sqlWhere); 
		if(count > max_count) {
			List<AggMeetRltHeadVO> aggMeetList = new ArrayList<AggMeetRltHeadVO>();
			
			UfocPkBatchGetter pkGetter = new UfocPkBatchGetter(MeetResultHeadVO.getDefaultTableName(), 
					MeetResultHeadVO.PK_TOTALINFO, "MEETDATA_HEAD", sqlWhere, null, max_count);
			
			while(pkGetter.hasNext()) {
				String[] pks = pkGetter.next();
				if(pks == null || pks.length == 0) 
					continue;
				@SuppressWarnings("unchecked")
				List<AggMeetRltHeadVO> list = (List<AggMeetRltHeadVO>) MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPKs(AggMeetRltHeadVO.class, pks, false);
				if(list != null)
					aggMeetList.addAll(list);
			}
			pkGetter.distroy();
			return aggMeetList.toArray(new AggMeetRltHeadVO[aggMeetList.size()]);
		}
		else {
			@SuppressWarnings("unchecked")
			List<AggMeetRltHeadVO> list = (List<AggMeetRltHeadVO>) MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByCond(AggMeetRltHeadVO.class, sqlWhere, false);
			return list == null ? null : list.toArray(new AggMeetRltHeadVO[list.size()]);
		}
	}
	
	private int getAggVOCount(String sqlWhere) throws DAOException {
		BaseDAO dao = new BaseDAO();
		String countSql = "select count(" + MeetResultHeadVO.PK_TOTALINFO + ") from " + MeetResultHeadVO.getDefaultTableName();
		if(! StringUtils.isEmpty(sqlWhere)) {
			countSql = countSql +" where " + sqlWhere;
		}
		Object resObj = dao.executeQuery(countSql, new CountResSetProcessor() );
		int count = ((Integer)resObj).intValue();
		return count;
	}
	
	private class CountResSetProcessor implements ResultSetProcessor {
		private static final long serialVersionUID = 1L;

		public Object handleResultSet(ResultSet rs) throws SQLException{
			if(rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public AggMeetRltHeadVO[] queryAggMeetResultByPKs(String[] pks)
			throws BusinessException {
		List<AggMeetRltHeadVO> list = (List<AggMeetRltHeadVO>) MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPKs(AggMeetRltHeadVO.class, pks, false);
		List<AggMeetRltHeadVO> resultList = new ArrayList<AggMeetRltHeadVO>();
		boolean showNullDetail = HBBBParamUtil.getShowNullDetail();
		if(!showNullDetail && list != null && list.size()>0 ){
			resultList = ContrastMeetFilterUtil.filterNullDetail(list);
		}else{
			resultList = list;
		}
		return resultList == null ? null : resultList.toArray(new AggMeetRltHeadVO[0]);
	}

	@SuppressWarnings("serial")
	@Override
	public String[] queryMeetResultPKsByCondition(String sqlWhere)
			throws BusinessException {
		String[] vOsAtrsArray = null;
		if (sqlWhere != null) {
			if (!StringUtils.isEmpty(sqlWhere)) {
				sqlWhere += " and ";
			}
			sqlWhere = sqlWhere + " exists (select b.pk_detail from iufo_meetdata_body b where b.details=pk_totalinfo and (b.MEET_AMOUNT<>0 or b.adjust_amount<>0)  ) ";
			BaseDAO dao = new BaseDAO();
			// 判断数量，大于10000的使用临时表
			String countSql = "select count(pk_totalinfo) from iufo_meetdata_head " + " where " + sqlWhere;
			int count = (Integer) dao.executeQuery(countSql,
					new BaseProcessor() {
						public Object processResultSet(ResultSet rs)
								throws SQLException {
							if (rs.next()) {
								return rs.getInt(1);
							}
							return 0;
						}
					});

			int max_count = 10000;
			if (count < max_count) {
				@SuppressWarnings("unchecked")
				List<MeetResultHeadVO> list = (List<MeetResultHeadVO>) new BaseDAO().retrieveByClause(MeetResultHeadVO.class, sqlWhere);
				if (list == null) {
					return null;
				}
				vOsAtrsArray = UFOCVOUtil.getVOsAtrsArray(list.toArray(new MeetResultHeadVO[0]),MeetResultHeadVO.PK_TOTALINFO);
			} else {
				List<String> list = new ArrayList<String>();
				UfocPkBatchGetter dataGetter = new UfocPkBatchGetter(
						MeetResultHeadVO.getDefaultTableName(),
						MeetResultHeadVO.PK_TOTALINFO, "MEET_QRY", sqlWhere,
						null, max_count);
				while (dataGetter.hasNext()) {
					String[] strs = dataGetter.next();
					for (String str : strs) {
						list.add(str);
					}
				}
				dataGetter.distroy();
				vOsAtrsArray = new String[list.size()];
				list.toArray(vOsAtrsArray);
			}
		}
		return vOsAtrsArray;
	}

//	@Override
//	public String[] queryMeetResultPKsByCondition(String sqlWhere)
//			throws BusinessException {
//		
//		@SuppressWarnings("unchecked")
//		List<MeetResultHeadVO> list  = (List<MeetResultHeadVO>) new BaseDAO().retrieveByClause(MeetResultHeadVO.class, sqlWhere);
//		if(list == null) {
//			return null;
//		}
//		String[] vOsAtrsArray = UFOCVOUtil.getVOsAtrsArray(list.toArray(new MeetResultHeadVO[0]), MeetResultHeadVO.PK_TOTALINFO);
//		
//		return vOsAtrsArray;
//	}
	
	@Override
	public Object[] refreshMeetResult(Object[] objects)throws BusinessException {
		Set<String> pkMeasureSet= new HashSet<String>();
		Set<String> pkSelfOrgSet= new HashSet<String>();
		for(Object vo : objects){
			if(vo instanceof MeetResultHeadVO){
				pkMeasureSet.add(((MeetResultHeadVO)vo).getPk_measure());
				pkSelfOrgSet.add(((MeetResultHeadVO)vo).getPk_selforg());
				pkSelfOrgSet.add(((MeetResultHeadVO)vo).getPk_countorg());
				
			}
		}
		Map<String, String> projectMap= queryProjectCodeAndNameByPks(pkMeasureSet.toArray(new String[0]));
		Map<String, String> orgMap = queryOrgCodeAndName(pkSelfOrgSet.toArray(new String[0]));
		
		for (Object vo : objects){
			if(vo instanceof MeetResultHeadVO){
				MeetResultHeadVO meetResultHeadVO = (MeetResultHeadVO)vo;
				String pkMeasure=meetResultHeadVO.getPk_measure();
				String pkSelfOrg=meetResultHeadVO.getPk_selforg();
				String pkCountorg=meetResultHeadVO.getPk_countorg();
				if(projectMap.get(pkMeasure)!=null){
					meetResultHeadVO.setPk_measure(projectMap.get(pkMeasure));
				}
				if(orgMap.get(pkSelfOrg)!=null){
					meetResultHeadVO.setPk_selforg(orgMap.get(pkSelfOrg));
					meetResultHeadVO.setPk_countorg(orgMap.get(pkCountorg));
					
				}
				else {
					if(MeetResultHeadVO.PROJECT_TOTAL.equals(meetResultHeadVO.getIstotal())) {
						meetResultHeadVO.setPk_selforg(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830008-0062")/*@res "合计"*/);
					}
					else if(MeetResultHeadVO.DEBITANDCREDIT_TOTAL.equals(meetResultHeadVO.getIstotal())){
						meetResultHeadVO.setPk_selforg(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830008-0063")/*@res "合计"*/);
						
					}
					
				}
			}
		}
		
		return objects;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> queryProjectCodeAndNameByPks(String[] pks)throws BusinessException {
		Map<String, String> projectMap = new HashMap<String, String>();
		String wherePart= UFOCSqlUtil.buildInSql(ProjectVO.PK_PROJECT, pks);
		
		Collection<ProjectVO> projectVOs = new BaseDAO().retrieveByClause(ProjectVO.class, "isnull(dr,0)=0 and " + wherePart);
		
		for (ProjectVO vo : projectVOs) {
			projectMap.put(vo.getPk_project(), HBBBLangUtil.getMulLang(vo, ProjectVO.NAME));
			
		}
		return projectMap;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, String> queryOrgCodeAndName(String[] pks)throws BusinessException {
		Map<String, String> orgMap = new HashMap<String, String>();
		String wherePart= UFOCSqlUtil.buildInSql(OrgVO.PK_ORG, pks);
		Collection<OrgVO> orgVOs = new BaseDAO().retrieveByClause(OrgVO.class, "isnull(dr,0)=0 and "+wherePart);
		for (OrgVO vo : orgVOs) {
			orgMap.put(vo.getPk_org(), HBBBLangUtil.getMulLang(vo, OrgVO.NAME));
		}
		return orgMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> queryUserCodeAndName(String[] pks)
			throws BusinessException {
		Map<String, String> userMap = new HashMap<String, String>();
		String wherePart= UFOCSqlUtil.buildInSql("cuserid", pks);
		Collection<UserVO> userVOs = new BaseDAO().retrieveByClause(UserVO.class, "isnull(dr,0)=0 and "+wherePart);
		for (UserVO vo : userVOs) {
			userMap.put(vo.getCuserid(), HBBBLangUtil.getMulLang(vo, UserVO.USER_NAME));
		}
		return userMap;
	}

}
