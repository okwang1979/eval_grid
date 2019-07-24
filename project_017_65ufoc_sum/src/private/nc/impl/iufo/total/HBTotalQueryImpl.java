
package nc.impl.iufo.total;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.iufo.DataManageObjectIufo;
import nc.itf.hbbb.hbrepstru.IHBRepstruQrySrv;
import nc.itf.iufo.total.IHBTotalQueryService;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.pub.iufo.exception.UFOSrvException;
import nc.pubitf.eaa.InnerCodeUtil;
import nc.vo.hbbb.total.HbTotalSchemeVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.vorg.ReportCombineStruMemberVersionVO;

import com.ufida.iufo.pub.tools.AppDebug;

/**
 * 汇总查询服务实现类
 * @author xulm
 * @created at 2010-4-19,下午02:46:48
 *
 */
public class HBTotalQueryImpl  extends DataManageObjectIufo  implements IHBTotalQueryService{

	BaseDAO baseDao = null;
	
	public BaseDAO getBaseDAO(){
		if(baseDao == null){
			baseDao = new BaseDAO();
		}
		return baseDao;
	}
	
	@Override
	public HbTotalSchemeVO getTotalSchemeByPK(String totalSchemeId)
			throws UFOSrvException {
		try {
			String cond = "pk_hbscheme =? ";
			SQLParameter param = new SQLParameter();
			param.addParam(totalSchemeId);
			List<HbTotalSchemeVO> list =  (List<HbTotalSchemeVO>)getBaseDAO().retrieveByClause(HbTotalSchemeVO.class, cond, param);

			if (list!=null && list.size()>0)
			{
				return list.get(0);
			}else
			{
				return null;
			}
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		}
	}

	@Override
	public HbTotalSchemeVO getTotalScheme(String rmsId, String orgId,
			String strRmsVersionPK) throws UFOSrvException {
		
		return null;
	}

	@Override
	public HbTotalSchemeVO getHBTotalSchemeInfo(String rmsId,String strRmsVersionPK,String innercode, final String orgId, MeasurePubDataVO pubData, String date)
			throws UFOSrvException {
		try {
			String cond = " pk_org = ? and pk_rmsversion = ?";
			SQLParameter param = new SQLParameter();
//			param.addParam(rmsId);
			param.addParam(orgId);
			param.addParam(strRmsVersionPK);
			

			List<HbTotalSchemeVO> list =(List<HbTotalSchemeVO>) getBaseDAO().retrieveByClause(HbTotalSchemeVO.class, cond, param);
			if (list!=null &&list.size()>0)
			{
				return list.get(0);
			}else
			{
				//如果是叶子节点，就不应该获取应用方案
				IHBRepstruQrySrv srv = NCLocator.getInstance().lookup(IHBRepstruQrySrv.class);
				if (srv.isLeafMember(strRmsVersionPK,orgId))
				{
					return null;
				}
				if (innercode==null ||innercode.trim().length()==0)
				{
					//如果本组织没有汇总方案，则默认上级组织汇总方案
					Collection set=baseDao.retrieveByClause(ReportCombineStruMemberVersionVO.class,"pk_svid='"+strRmsVersionPK+"' and pk_org='"+orgId+"'");
					if (set==null || set.size()<=0)
					{
						return null;
					}
					innercode=((ReportCombineStruMemberVersionVO)set.toArray()[0]).getInnercode();
				}
				StringBuffer buffer=new StringBuffer();

				for(int i=0;i<(innercode.length()/InnerCodeUtil.INNERCODELENGTH);i++)
				{
					if (i > 0) {
						buffer.append(",");
					}
					buffer.append("'"+innercode.substring(0, (i+1)*4)+"'");
				}
                //此处实际只需要取最近的那个
//				String sql=" select i.pk_totalscheme,o.pk_org,i.org_type,i.org_condition,i.org_content,i.org_cond_show,o.pk_rmsmember,o.pk_fathermember,o.innercode,i.ts,i.pk_rms from org_rmsmember o right join  iufo_total_scheme i on o.pk_org=i.pk_org where  o.innercode  in ("+buffer.toString()+")  order by o.innercode desc" ;
				String sql=" select i.pk_hbscheme,o.pk_org,i.totalType org_type,o.pk_rcs,o.innercode,i.ts, o.pk_svid as pk_rmsversion " +
						" from org_rcsmember_v o right join " +
						" (select * from iufo_hb_scheme where pk_rmsversion =  '"+strRmsVersionPK+"') i on o.pk_org=i.pk_org " +
								" where  o.innercode  in ("+buffer.toString()+")  order by o.innercode desc" ;
				list= (List<HbTotalSchemeVO>) baseDao.executeQuery(sql,new ResultSetProcessor() {

					@Override
					public Object handleResultSet(ResultSet rs) throws SQLException {

			            ArrayList<HbTotalSchemeVO> lstTotalScheme=new ArrayList<HbTotalSchemeVO>();
						while(rs.next()){
							HbTotalSchemeVO totalSchemeVO=new HbTotalSchemeVO();
							totalSchemeVO.setPk_hbscheme(rs.getString("pk_hbscheme"));
							totalSchemeVO.setPk_org(orgId);
							totalSchemeVO.setPk_rms(rs.getString("pk_rcs"));
							totalSchemeVO.setTotalType(rs.getInt("org_type"));
//							totalSchemeVO.setInnercode(rs.getString("innercode"));
							totalSchemeVO.setTs(rs.getString("ts")==null?null:new UFDateTime(rs.getString("ts")));
							
							totalSchemeVO.setPk_rmsversion(rs.getString("pk_rmsversion"));
							lstTotalScheme.add(totalSchemeVO);
						}
						return lstTotalScheme;
					}
				});
				if (list!=null &&list.size()>0)
				{
					HbTotalSchemeVO totalScheme= (HbTotalSchemeVO)baseDao.retrieveByPK(HbTotalSchemeVO.class, list.get(0).getPk_hbscheme());
					totalScheme.setPk_org(orgId);
					return totalScheme;
				}else
				{
					return null;
				}
			}
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		}
	}

	
	/*

//	String tempTableSql = " select i.pk_totalscheme pk_totalscheme,o.pk_org pk_org, o.innercode o.innercode" +
//			" from org_ReportManaStruMember_v o right join " +
//			" (select * from iufo_total_scheme where pk_rms='"+rmsId+"' and pk_rmsversion =  '"+strRmsVersionPK+"') i on o.pk_org=i.pk_org ";
	*//**
     * 批量填充时，存放汇总规则临时表
     *//*
    public static final String NEW_VERSION_ITEMPK = "temp_totalscheme";
	*//**
	 * 临时表列
	 *//*
    public static final String TEMPTABLE_COLUMNS = "pk_totalscheme char(20),pk_org char(20),innercode char(50)";

    *//**
     * 临时表索引列名
     *//*
    public static final String INDEX_COLUMN = "innercode";
	*//**
	 * 汇总规则临时中间表
	 *//*
	private String totalTemptable = null;
	
	 (non-Javadoc)
	 * @see nc.itf.iufo.total.ITotalQueryService#getTotalScheme(java.lang.String)
	 
	@SuppressWarnings("unchecked") 
	@Override
	public TotalSchemeVO getTotalSchemeByPK(String totalSchemeId) throws UFOSrvException {
		try {
			String cond = "pk_totalscheme=? ";
			SQLParameter param = new SQLParameter();
			param.addParam(totalSchemeId);
			BaseDAO baseDao = new BaseDAO();
			List<TotalSchemeVO> list =  (List<TotalSchemeVO>)baseDao.retrieveByClause(TotalSchemeVO.class, cond, param);

			if (list!=null && list.size()>0)
			{
				return list.get(0);
			}else
			{
				return null;
			}
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public TotalSchemeVO getTotalScheme(String rmsId,String orgId,String strRmsVersionPK) throws UFOSrvException {
		try {
			String cond = "pk_rms=? and  pk_org = ? and pk_rmsversion = ?";
			SQLParameter param = new SQLParameter();
			param.addParam(rmsId);
			param.addParam(orgId);
			param.addParam(strRmsVersionPK);
			BaseDAO baseDao = new BaseDAO();
			List<TotalSchemeVO> list =(List<TotalSchemeVO>) baseDao.retrieveByClause(TotalSchemeVO.class, cond, param);
			if (list!=null &&list.size()>0)
			{
				return list.get(0);
			}else
			{
				return null;
			}
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		}
	}

	 (non-Javadoc)
	 * @see nc.itf.iufo.total.ITotalQueryService#getTotalScheme(java.lang.String)
	 
	@SuppressWarnings({ "unchecked" })
	@Override
	public TotalSchemeVO getAppTotalScheme(String rmsId,String orgId,String innercode, String strRmsVersionPK ) throws UFOSrvException {
		try {
			String cond = "pk_rms=? and  pk_org = ? and pk_rmsversion = ?";
			SQLParameter param = new SQLParameter();
			param.addParam(rmsId);
			param.addParam(orgId);
			param.addParam(strRmsVersionPK);
			BaseDAO baseDao = new BaseDAO();

			List<TotalSchemeVO> list =(List<TotalSchemeVO>) baseDao.retrieveByClause(TotalSchemeVO.class, cond, param);
			if (list!=null &&list.size()>0)
			{
				return list.get(0);
			}else
			{
				//如果是叶子节点，就不应该获取应用方案
				ICorpQuerySrv srv = NCLocator.getInstance().lookup(ICorpQuerySrv.class);
				if (srv.isLeafOrgMemberInReportManaStruVersion(strRmsVersionPK,orgId))
				{
					return null;
				}
				if (innercode==null ||innercode.trim().length()==0)
				{
					//如果本组织没有汇总方案，则默认上级组织汇总方案
					Collection set=baseDao.retrieveByClause(ReportManaStruMemberVersionVO.class,"pk_svid='"+strRmsVersionPK+"' and pk_org='"+orgId+"'");
					if (set==null || set.size()<=0)
					{
						return null;
					}
					innercode=((ReportManaStruMemberVersionVO)set.toArray()[0]).getInnercode();
				}
				StringBuffer buffer=new StringBuffer();

				for(int i=0;i<(innercode.length()/InnerCodeUtil.INNERCODELENGTH);i++)
				{
					if (i > 0) {
						buffer.append(",");
					}
					buffer.append("'"+innercode.substring(0, (i+1)*4)+"'");
				}
                //此处实际只需要取最近的那个
//				String sql=" select i.pk_totalscheme,o.pk_org,i.org_type,i.org_condition,i.org_content,i.org_cond_show,o.pk_rmsmember,o.pk_fathermember,o.innercode,i.ts,i.pk_rms from org_rmsmember o right join  iufo_total_scheme i on o.pk_org=i.pk_org where  o.innercode  in ("+buffer.toString()+")  order by o.innercode desc" ;
				String sql=" select i.pk_totalscheme,o.pk_org,i.org_type,i.org_condition,i.org_content,i.org_cond_show,o.pk_rmsmember,o.pk_fathermember,o.innercode,i.ts,i.pk_rms,o.pk_svid as pk_rmsversion " +
						" from org_ReportManaStruMember_v o right join " +
						" (select * from iufo_total_scheme where pk_rms='"+rmsId+"' and pk_rmsversion =  '"+strRmsVersionPK+"') i on o.pk_org=i.pk_org " +
								" where  o.innercode  in ("+buffer.toString()+")  order by o.innercode desc" ;
				list= (ArrayList<TotalSchemeVO>) baseDao.executeQuery(sql,new TotalQryProcessor());
				if (list!=null &&list.size()>0)
				{
					TotalSchemeVO totalScheme= (TotalSchemeVO)baseDao.retrieveByPK(TotalSchemeVO.class, list.get(0).getPk_totalscheme());
					return totalScheme;
				}else
				{
					return null;
				}
			}
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		}
	}


	@SuppressWarnings({ "unchecked" })
	@Override
	public TotalSchemeVO[] getTotalSchemes(String rmsId,String orgId,String strRmsVersionPK,String clause) throws UFOSrvException
	{
		try {
			BaseDAO baseDao = new BaseDAO();
			TotalSchemeVO[] totalSchemes=new TotalSchemeVO[0];
			Collection set=baseDao.retrieveByClause(ReportManaStruMemberVersionVO.class,"pk_svid='"+strRmsVersionPK+"' and pk_org='"+orgId+"'");
//			Collection set=baseDao.retrieveByClause(ReportManaStruMemberVO.class,"pk_rms='"+rmsId+"' and pk_org='"+orgId+"'");
			if (set==null || set.size()<=0)
				return null;
			String strInnerCode=((ReportManaStruMemberVersionVO)set.toArray()[0]).getInnercode();
			String sql="";
			if (!StringUtil.isEmptyWithTrim(clause))
			{
				clause=clause.replace("pk_org", "o.pk_org").replace("org_type", "i.org_type");
				//此处sql 是否有优化的可能???
//				sql=" select i.pk_totalscheme,o.pk_org,i.org_type,i.org_condition,i.org_content,i.org_cond_show,o.pk_rmsmember,o.pk_fathermember,o.innercode,i.ts,o.pk_rms from org_rmsmember o left join  iufo_total_scheme i on o.pk_org=i.pk_org where o.pk_rms='"+rmsId+"' and o.innercode like '"+strInnerCode+"%'" +" and EXISTS (select 1 from org_rmsmember where o.pk_org=pk_fatherorg) and  " +clause +" order by o.innercode desc" ;
				sql=" select i.pk_totalscheme,o.pk_org,i.org_type,i.org_condition,i.org_content,i.org_cond_show,o.pk_rmsmember,o.pk_fathermember," +
					"o.innercode,i.ts,o.pk_rms,o.pk_svid as pk_rmsversion from org_ReportManaStruMember_v o left join (select * from iufo_total_scheme where pk_rms='"+rmsId+"' and pk_rmsversion =  '"+strRmsVersionPK+"') i " +
					"on o.pk_org=i.pk_org where o.pk_svid='"+strRmsVersionPK+"' and o.innercode like '"+strInnerCode+"%'" +" and " +
					"EXISTS (select 1 from org_ReportManaStruMember_v where o.pk_rmsmember=pk_fathermember and pk_svid='" + strRmsVersionPK + "') and  " +clause +" order by o.innercode desc,o.idx desc" ;
			}else
			{
//				sql=" select i.pk_totalscheme,o.pk_org,i.org_type,i.org_condition,i.org_content,i.org_cond_show,o.pk_rmsmember,o.pk_fathermember,o.innercode,i.ts,o.pk_rms from org_rmsmember o left join  iufo_total_scheme i on o.pk_org=i.pk_org where o.pk_rms='"+rmsId+"' and o.innercode like '"+strInnerCode+"%' and EXISTS (select 1 from org_rmsmember where o.pk_org=pk_fatherorg) order by o.innercode desc" ;
				sql=" select i.pk_totalscheme,o.pk_org,i.org_type,i.org_condition,i.org_content,i.org_cond_show,o.pk_rmsmember,o.pk_fathermember," +
					"o.innercode,i.ts,o.pk_rms,o.pk_svid as pk_rmsversion from org_ReportManaStruMember_v o left join  (select * from iufo_total_scheme where pk_rms='"+rmsId+"' and pk_rmsversion =  '"+strRmsVersionPK+"') i" +
					" on o.pk_org=i.pk_org where o.pk_svid='"+strRmsVersionPK+"' and o.innercode like '"+strInnerCode+"%' and " +
//					"EXISTS (select 1 from org_rmsmember where o.pk_org=pk_fatherorg) order by o.innercode desc" ;  // modified by jiaah
					"EXISTS (select 1 from org_ReportManaStruMember_v where o.pk_rmsmember=pk_fathermember and pk_svid='" + strRmsVersionPK + "') order by o.innercode desc,o.idx desc" ;
//					" o.pk_org in (select pk_fatherorg from org_rmsmember where pk_rms='" + rmsId + "') order by o.innercode desc" ;
			}

			ArrayList<TotalSchemeVO> lstTotalScheme= (ArrayList<TotalSchemeVO>) baseDao.executeQuery(sql,new TotalQryProcessor());
			if (lstTotalScheme!=null && lstTotalScheme.size()>0)
			{
				totalSchemes=lstTotalScheme.toArray(new TotalSchemeVO[lstTotalScheme.size()]);
				groupFillAppSchemePK(totalSchemes);
			}
			return totalSchemes;
		}catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	@Override
	public TotalSchemeVO[] getTotalSchemesByBusiTime(String rmsId,String orgId,String busiTime,String clause) throws UFOSrvException
	{
		try {
			BaseDAO baseDao = new BaseDAO();
			TotalSchemeVO[] totalSchemes=new TotalSchemeVO[0];
			String strRmsVersionPK = RMSUtil.getPKRmsVersionByPkRms(rmsId, busiTime);
			Collection set=baseDao.retrieveByClause(ReportManaStruMemberVersionVO.class,"pk_svid='"+strRmsVersionPK+"' and pk_org='"+orgId+"'");
//			Collection set=baseDao.retrieveByClause(ReportManaStruMemberVO.class,"pk_rms='"+rmsId+"' and pk_org='"+orgId+"'");
			if (set==null || set.size()<=0)
				return null;
			String strInnerCode=((ReportManaStruMemberVersionVO)set.toArray()[0]).getInnercode();
			String sql="";
			String newClause="";
			if (!StringUtil.isEmptyWithTrim(clause))
			{
				clause=clause.replace("pk_org", "o.pk_org").replace("org_type", "i.org_type");
				newClause=clause+" or i.org_type is null";
				//此处sql 是否有优化的可能???
//				sql=" select i.pk_totalscheme,o.pk_org,i.org_type,i.org_condition,i.org_content,i.org_cond_show,o.pk_rmsmember,o.pk_fathermember,o.innercode,i.ts,o.pk_rms from org_rmsmember o left join  iufo_total_scheme i on o.pk_org=i.pk_org where o.pk_rms='"+rmsId+"' and o.innercode like '"+strInnerCode+"%'" +" and EXISTS (select 1 from org_rmsmember where o.pk_org=pk_fatherorg) and  " +clause +" order by o.innercode desc" ;
				sql=" select i.pk_totalscheme,o.pk_org,i.org_type,i.org_condition,i.org_content,i.org_cond_show,o.pk_rmsmember,o.pk_fathermember," +
					"o.innercode,i.ts,o.pk_rms,o.pk_svid as pk_rmsversion from org_ReportManaStruMember_v o left join (select * from iufo_total_scheme where pk_rms='"+rmsId+"' and pk_rmsversion =  '"+strRmsVersionPK+"') i " +
					"on o.pk_org=i.pk_org where o.pk_svid='"+strRmsVersionPK+"' and o.innercode like '"+strInnerCode+"%'" +" and " +
					"EXISTS (select 1 from org_ReportManaStruMember_v where o.pk_rmsmember=pk_fathermember and pk_svid='" + strRmsVersionPK + "') and  (" +newClause +" )order by o.innercode desc,o.idx desc" ;
			}else
			{
//				sql=" select i.pk_totalscheme,o.pk_org,i.org_type,i.org_condition,i.org_content,i.org_cond_show,o.pk_rmsmember,o.pk_fathermember,o.innercode,i.ts,o.pk_rms from org_rmsmember o left join  iufo_total_scheme i on o.pk_org=i.pk_org where o.pk_rms='"+rmsId+"' and o.innercode like '"+strInnerCode+"%' and EXISTS (select 1 from org_rmsmember where o.pk_org=pk_fatherorg) order by o.innercode desc" ;
				sql=" select i.pk_totalscheme,o.pk_org,i.org_type,i.org_condition,i.org_content,i.org_cond_show,o.pk_rmsmember,o.pk_fathermember," +
					"o.innercode,i.ts,o.pk_rms,o.pk_svid as pk_rmsversion from org_ReportManaStruMember_v o left join  (select * from iufo_total_scheme where pk_rms='"+rmsId+"' and pk_rmsversion =  '"+strRmsVersionPK+"') i" +
					" on o.pk_org=i.pk_org where o.pk_svid='"+strRmsVersionPK+"' and o.innercode like '"+strInnerCode+"%' and " +
//					"EXISTS (select 1 from org_rmsmember where o.pk_org=pk_fatherorg) order by o.innercode desc" ;  // modified by jiaah
					"EXISTS (select 1 from org_ReportManaStruMember_v where o.pk_rmsmember=pk_fathermember and pk_svid='" + strRmsVersionPK + "') order by o.innercode desc,o.idx desc" ;
//					" o.pk_org in (select pk_fatherorg from org_rmsmember where pk_rms='" + rmsId + "') order by o.innercode desc" ;
			}

			ArrayList<TotalSchemeVO> lstTotalScheme= (ArrayList<TotalSchemeVO>) baseDao.executeQuery(sql,new TotalQryProcessor());
			if (lstTotalScheme!=null && lstTotalScheme.size()>0)
			{
				totalSchemes=lstTotalScheme.toArray(new TotalSchemeVO[lstTotalScheme.size()]);
				groupFillAppSchemePK(totalSchemes);
			}
			if (!StringUtil.isEmptyWithTrim(clause) && !"1=1".equals(clause.trim())){
				String typeSql="select distinct i.org_type from iufo_total_scheme i where " +clause;
				ArrayList<Integer> queryTotalSchemeList=(ArrayList<Integer>) baseDao.executeQuery(typeSql, new OrgTypeProcessor());
				List<TotalSchemeVO> newLstTotalScheme=new ArrayList<TotalSchemeVO>();
				for(TotalSchemeVO totalScheme:totalSchemes){
					if(queryTotalSchemeList.contains(totalScheme.getOrg_type())){
						newLstTotalScheme.add(totalScheme);
					}
				}
				totalSchemes=newLstTotalScheme.toArray(new TotalSchemeVO[newLstTotalScheme.size()]);
			}
			
			return totalSchemes;
		}catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	@Override
	public TotalSchemeVO[] getTotalSchemesByVersion(String rmsId,String orgId,String clause,String date) throws UFOSrvException
	{
		try {
			BaseDAO baseDao = new BaseDAO();
			ReportManaStruVersionVO rmsmVerVO=((IUfoeVorpQuerySrv)NCLocator.getInstance().lookup(IUfoeVorpQuerySrv.class.getName())).getRmsVerVOByDate(date, rmsId);
			String rmsVid=null;
			if(rmsmVerVO.getPk_vid()!=null){
				rmsVid=rmsmVerVO.getPk_vid();
			}else{
				return null;
			}
			TotalSchemeVO[] totalSchemes=new TotalSchemeVO[0];
			Collection set=baseDao.retrieveByClause(ReportManaStruMemberVersionVO.class,"pk_svid='"+rmsVid+"' and pk_org='"+orgId+"'");
			if (set==null || set.size()<=0)
				return null;
			String strInnerCode=((ReportManaStruMemberVersionVO)set.toArray()[0]).getInnercode();
			String sql="";
			if (!StringUtil.isEmptyWithTrim(clause))
			{
				clause=clause.replace("pk_org", "o.pk_org").replace("org_type", "i.org_type");
				//此处sql 是否有优化的可能???
//				sql=" select i.pk_totalscheme,o.pk_org,i.org_type,i.org_condition,i.org_content,i.org_cond_show,o.pk_rmsmember,o.pk_fathermember,o.innercode,i.ts,o.pk_rms from org_rmsmember o left join  iufo_total_scheme i on o.pk_org=i.pk_org where o.pk_rms='"+rmsId+"' and o.innercode like '"+strInnerCode+"%'" +" and EXISTS (select 1 from org_rmsmember where o.pk_org=pk_fatherorg) and  " +clause +" order by o.innercode desc" ;
				sql=" select i.pk_totalscheme,o.pk_org,i.org_type,i.org_condition,i.org_content,i.org_cond_show,o.pk_rmsmember,o.pk_fathermember," +
					"o.innercode,i.ts,o.pk_rms,o.pk_svid as pk_rmsversion from org_ReportManaStruMember_v o left join (select * from iufo_total_scheme where pk_rms='"+rmsId+"' and pk_rmsversion = '"+rmsVid+"' ) i " +
					"on o.pk_org=i.pk_org where o.pk_Svid='"+rmsVid+"' and o.innercode like '"+strInnerCode+"%'" +" and " +
					"EXISTS (select 1 from org_ReportManaStruMember_v where o.pk_rmsmember=pk_fathermember and pk_svid='" + rmsVid + "') and  " +clause +" order by o.innercode desc,o.idx desc" ;
			}else
			{
//				sql=" select i.pk_totalscheme,o.pk_org,i.org_type,i.org_condition,i.org_content,i.org_cond_show,o.pk_rmsmember,o.pk_fathermember,o.innercode,i.ts,o.pk_rms from org_rmsmember o left join  iufo_total_scheme i on o.pk_org=i.pk_org where o.pk_rms='"+rmsId+"' and o.innercode like '"+strInnerCode+"%' and EXISTS (select 1 from org_rmsmember where o.pk_org=pk_fatherorg) order by o.innercode desc" ;
				sql=" select i.pk_totalscheme,o.pk_org,i.org_type,i.org_condition,i.org_content,i.org_cond_show,o.pk_rmsmember,o.pk_fathermember," +
					"o.innercode,i.ts,o.pk_rms,o.pk_svid as pk_rmsversion from org_reportManaStruMember_v o left join  (select * from iufo_total_scheme where pk_rms='"+rmsId+"' and pk_rmsversion = '"+rmsVid+"' ) i" +
					" on o.pk_org=i.pk_org where o.pk_Svid='"+rmsVid+"' and o.innercode like '"+strInnerCode+"%' and " +
//					"EXISTS (select 1 from org_rmsmember where o.pk_org=pk_fatherorg) order by o.innercode desc" ;  // modified by jiaah
					"EXISTS (select 1 from org_reportManaStruMember_v where o.pk_rmsmember=pk_fathermember and pk_svid='" + rmsVid + "') order by o.innercode desc,o.idx desc" ;
//					" o.pk_org in (select pk_fatherorg from org_rmsmember where pk_rms='" + rmsId + "') order by o.innercode desc" ;
			}

			ArrayList<TotalSchemeVO> lstTotalScheme= (ArrayList<TotalSchemeVO>) baseDao.executeQuery(sql,new TotalQryProcessor());
			if (lstTotalScheme!=null && lstTotalScheme.size()>0)
			{
				totalSchemes=lstTotalScheme.toArray(new TotalSchemeVO[lstTotalScheme.size()]);
				groupFillAppSchemePK(totalSchemes);
			}
			return totalSchemes;
		}catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		}
	}
	@Override
	public TotalSchemeVO[] getTotalSchemes(String rmsId,String orgId,String pk_rmsversion) throws UFOSrvException {
        return this.getTotalSchemes(rmsId, orgId, pk_rmsversion, null);
	}
	
	@Override
	public TotalSchemeVO[] getTotalSchemes(String rmsId,String[] orgIds, String pk_rmsversion) throws UFOSrvException {
		try {
			ArrayList<TotalSchemeVO> lstAll = new ArrayList<TotalSchemeVO>();
			TotalSchemeVO[] totalSchemes=new TotalSchemeVO[0];
			if(orgIds != null && orgIds.length >0){
				StringBuffer pkBf = new StringBuffer("");
				//将每200个组织组成一串执行一次
				for(int i = 0; i < orgIds.length; i++){
					pkBf.append("'");
					pkBf.append(orgIds[i]);
					pkBf.append("'");
					if((i+1)%200 == 0){
						ArrayList<TotalSchemeVO> lstTotalScheme=getTotalSchemesByOrgCond(rmsId,pkBf.toString(),pk_rmsversion);
						lstAll.addAll(lstTotalScheme);
						pkBf = new StringBuffer("");
						continue;
					}
					if(i < orgIds.length - 1){
						pkBf.append(",");
					}
				}

				if(pkBf.length() > 0){
					ArrayList<TotalSchemeVO> lstTotalScheme=getTotalSchemesByOrgCond(rmsId,pkBf.toString(),pk_rmsversion);
					lstAll.addAll(lstTotalScheme);
				}
			}

			if(lstAll != null && lstAll.size() > 0){
				//按内部编码自下而上的排列
				Collections.sort(lstAll, new Comparator<TotalSchemeVO>()
				{
					@Override
					public int compare(TotalSchemeVO o1, TotalSchemeVO o2) {
						return -o1.getInnercode().compareTo(o2.getInnercode());
					}
				});
				totalSchemes= lstAll.toArray(new TotalSchemeVO[0]);
				groupFillAppSchemePK(totalSchemes);
			}

			return totalSchemes;
		}catch (UFOSrvException e) {
			AppDebug.debug(e);
			throw e;
		}
	}


	*//**
	 * 通过组织条件获取汇总方案
	 * @create by xulm at 2010-6-25,上午10:38:43
	 *
	 * @param rmsId
	 * @param orgCond
	 * @return
	 * @throws UFOSrvException
	 *//*
	@SuppressWarnings("unchecked")
	private ArrayList<TotalSchemeVO> getTotalSchemesByOrgCond(String rmsId, String orgCond, String pk_rmsversion) throws UFOSrvException
	{
		BaseDAO dao = new BaseDAO();
		try {
//			String sql=" select i.pk_totalscheme,o.pk_org,i.org_type,i.org_condition,i.org_content,i.org_cond_show,o.pk_rmsmember,o.pk_fathermember,o.innercode,i.ts,o.pk_rms from org_rmsmember o left join  iufo_total_scheme i on o.pk_org=i.pk_org where o.pk_rms='"+rmsId+"'  and o.pk_org in (" +orgCond+")" ;
			String sql=" select i.pk_totalscheme,o.pk_org,i.org_type,i.org_condition,i.org_content,i.org_cond_show,o.pk_rmsmember,o.pk_fathermember,o.innercode,i.ts,o.pk_rms,o.pk_svid as pk_rmsversion from org_ReportManaStruMember_v o left join  (select * from iufo_total_scheme where pk_rms='"+rmsId+"' and pk_rmsversion = '"+pk_rmsversion+"') i on o.pk_org=i.pk_org where o.pk_svid='" + pk_rmsversion + "'  and o.pk_org in (" +orgCond+")" ;
			ArrayList<TotalSchemeVO> lstTotalScheme= (ArrayList<TotalSchemeVO>) dao.executeQuery(sql,new TotalQryProcessor());
			return lstTotalScheme;
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		}
	}
	
	*//**
	 * add
	 * @param totalSchemes
	 * @throws UFOSrvException
	 *//*
	private void groupFillAppSchemePK(TotalSchemeVO[] totalSchemes) throws UFOSrvException {
		try {
			if (totalSchemes!=null && totalSchemes.length>0) {
				Map<String, String> pkorgInnerCodeMapper = new HashMap<String, String>();
				String rmsId = totalSchemes[0].getPk_rms();
				String strRmsVersionPK = totalSchemes[0].getPk_rmsversion();
				for(TotalSchemeVO schemeVO : totalSchemes) {
					pkorgInnerCodeMapper.put(schemeVO.getPk_org(), schemeVO.getInnercode());
				}
				String cond = "pk_rms=? and pk_rmsversion = ? ";
				SQLParameter param = new SQLParameter();
				param.addParam(rmsId);
				param.addParam(strRmsVersionPK);
				BaseDAO baseDao = new BaseDAO();
				//edit by congdy 2015.7.13 当继承自上级方案时，没有要一直往上级找，所以map中不能限制死pk_org的范围
//				cond += (" and " + UFDSSqlUtil.buildInSql("pk_org", pkorgInnerCodeMapper.keySet()));
				Map<String, TotalSchemeVO> existAppSchemes = new HashMap<String, TotalSchemeVO>();
				
				Map<String, TotalSchemeVO> schemes = new HashMap<String, TotalSchemeVO>();
				
				List<TotalSchemeVO> list =(List<TotalSchemeVO>) baseDao.retrieveByClause(TotalSchemeVO.class, cond, param);
				for(TotalSchemeVO appScheme : list) {
					existAppSchemes.put(appScheme.getPk_org(), appScheme);
					schemes.put(appScheme.getPk_totalscheme(), appScheme);
				}
				
				List<String> pkorgs = new ArrayList<String>();
				for (TotalSchemeVO totalScheme : totalSchemes) {
					pkorgs.add(totalScheme.getPk_org());
				}
				
				ICorpQuerySrv srv = NCLocator.getInstance().lookup(ICorpQuerySrv.class);
				Map<String, Boolean> isLeafMap = srv.groupIsLeafOrgMemberInReportManaStruVersion(strRmsVersionPK, pkorgs.toArray(new String[0]));
				
				totalTemptable = DBAUtil.createTempTable(NEW_VERSION_ITEMPK,TEMPTABLE_COLUMNS, null);
				String tempTableInsertSql = "insert into " + totalTemptable +"(pk_totalscheme, pk_org, innercode) select i.pk_totalscheme pk_totalscheme,o.pk_org pk_org, o.innercode innercode" +
						" from org_ReportManaStruMember_v o right join " +
						" (select * from iufo_total_scheme where pk_rms='"+rmsId+"' and pk_rmsversion =  '"+strRmsVersionPK+"') i on o.pk_org=i.pk_org ";
				baseDao.executeUpdate(tempTableInsertSql);
				
				for (TotalSchemeVO totalScheme : totalSchemes) {
					if (StringUtil.isEmptyWithTrim(totalScheme.getPk_totalscheme())) {
						TotalSchemeVO tempTotalScheme = existAppSchemes.get(totalScheme.getPk_org());
						if (tempTotalScheme == null){
							//如果是叶子节点，就不应该获取应用方案
							
							if (isLeafMap.get(totalScheme.getPk_org()) != null && isLeafMap.get(totalScheme.getPk_org()).booleanValue()) {
								continue;
							}
							String innerCode = totalScheme.getInnercode();
							if (innerCode == null || innerCode.trim().length() == 0) {
								//如果本组织没有汇总方案，则默认上级组织汇总方案
								Collection set = baseDao.retrieveByClause(ReportManaStruMemberVersionVO.class,"pk_svid='"+strRmsVersionPK+"' and pk_org='" + totalScheme.getPk_org()+"'");
								if (set == null || set.size() <= 0){
									continue;
								}
								innerCode = ((ReportManaStruMemberVersionVO)set.toArray()[0]).getInnercode();
							}
							StringBuffer buffer=new StringBuffer();

							for(int i=0;i<(innerCode.length()/InnerCodeUtil.INNERCODELENGTH);i++) {
								if (i > 0) {
									buffer.append(",");
								}
								buffer.append("'" + innerCode.substring(0, (i+1)*4) + "'");
							}
			                //此处实际只需要取最近的那个
//							pk_totalscheme char(20),pk_org char(20),innercode char(20)
							String sql = "select pk_totalscheme from " + totalTemptable 
									+ " where innercode in (" + buffer.toString() + ")"
									+ " order by innercode desc";
							List<String> templist = (ArrayList<String>) baseDao.executeQuery(sql,new ColumnListProcessor("pk_totalscheme"));
							if (templist!=null &&templist.size()>0) {
//								tempTotalScheme = (TotalSchemeVO)baseDao.retrieveByPK(TotalSchemeVO.class, templist.get(0));
								//TODO 待验证
								tempTotalScheme = schemes.get(templist.get(0));
							}else {
								continue;
							}
						}
						if(tempTotalScheme != null) {
							totalScheme.setPk_app_totalscheme(tempTotalScheme.getPk_totalscheme());
							totalScheme.setApp_org(tempTotalScheme.getPk_org());
							totalScheme.setPk_totalscheme(tempTotalScheme.getPk_totalscheme());
							//如果是顶级组织的话，需要直接显示应用的汇总规则
							totalScheme.setOrg_type(tempTotalScheme.getOrg_type());
						}
					}
				}
			}
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		} catch (BusinessException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		}finally{
			
		}

	}


	*//***
	 * 填充已按内部编码自下而上排序后的所属组织的汇总方案（无的时候可默认应用上级方案）
	 * @create by xulm at 2010-6-25,上午11:07:55
	 *
	 * @param totalSchemes
	 * @throws UFOSrvException
	 *//*
	private void fillAppSchemePK(TotalSchemeVO[] totalSchemes) throws UFOSrvException
	{

		HBTotalQueryImpl totalQuery= new HBTotalQueryImpl();
//		HashMap<String,TotalSchemeVO> mapTotalScheme=new HashMap<String,TotalSchemeVO>();
		if (totalSchemes!=null && totalSchemes.length>0){

			//解决没有级次关系的组织之间如何应用上级汇总方案的问题;
	        for (TotalSchemeVO totalScheme : totalSchemes) {
				if (StringUtil.isEmptyWithTrim(totalScheme.getPk_totalscheme())){
					TotalSchemeVO tempTotalScheme=totalQuery.getAppTotalScheme(totalScheme.getPk_rms(), totalScheme.getPk_org(), totalScheme.getInnercode(),totalScheme.getPk_rmsversion());
					if (tempTotalScheme!=null){
						totalScheme.setPk_app_totalscheme(tempTotalScheme.getPk_totalscheme());
						totalScheme.setApp_org(tempTotalScheme.getPk_org());
						totalScheme.setPk_totalscheme(tempTotalScheme.getPk_totalscheme());
						//如果是顶级组织的话，需要直接显示应用的汇总规则
						totalScheme.setOrg_type(tempTotalScheme.getOrg_type());
					}
				}
	        }
//			TotalSchemeVO topTotalScheme=totalSchemes[totalSchemes.length-1];
//			//获取顶级组织没有可用的汇总方案，则默认使用上级方案
//			if (StringUtil.isEmptyWithTrim(topTotalScheme.getPk_totalscheme()))
//			{
//				TotalSchemeVO tempTotalScheme=totalQuery.getAppTotalScheme(topTotalScheme.getPk_rms(), topTotalScheme.getPk_org(), topTotalScheme.getInnercode());
//				if (tempTotalScheme!=null)
//				{
//					topTotalScheme.setPk_app_totalscheme(tempTotalScheme.getPk_totalscheme());
//					topTotalScheme.setApp_org(tempTotalScheme.getPk_org());
//					//如果是顶级组织的话，需要直接显示应用的汇总规则   modified by jiaah at 2011-4-14
//					topTotalScheme.setPk_totalscheme(tempTotalScheme.getPk_totalscheme());
//					topTotalScheme.setOrg_type(tempTotalScheme.getOrg_type());
//				}
//			}
//			//如果存在着无汇总方案的，则默认使用上级方案
//		    for(int i=totalSchemes.length-1;i>-1;i--)
//		    {
//		        if (StringUtil.isEmptyWithTrim(totalSchemes[i].getPk_totalscheme()))
//		        {
//		        	String innercode=totalSchemes[i].getInnercode();
//		        	int m=0;
//		        	while(innercode.length()-m*InnerCodeUtil.INNERCODELENGTH>0)
//		        	{
//		        		TotalSchemeVO tempTotalScheme=mapTotalScheme.get(innercode.substring(0, innercode.length()-m*InnerCodeUtil.INNERCODELENGTH));
//		        		if(tempTotalScheme!=null)
//		        		{
//		        			totalSchemes[i].setPk_app_totalscheme(tempTotalScheme.getPk_totalscheme());
//		        			totalSchemes[i].setApp_org(tempTotalScheme.getPk_org());
//		        			break;
//		        		}else
//		        		{
//		        			m=m+1;
//		        		}
//		        	}
//		        }else
//		        {
//		        	mapTotalScheme.put(totalSchemes[i].getInnercode(), totalSchemes[i]);
//		        }
		    }
	}


	@SuppressWarnings("unchecked")
	private TotalCellKeyValue[][] innerLoadSourVals(TotalCellKeyValue[] totalVals,String strTaskID,String strSQL,String strAloneID,String strRepID,KeyVO[] destKeys,Hashtable hashKeyPos) throws Exception{
//		MeasureCache measCache=UFOCacheManager.getSingleton().getMeasureCache();
		KeyGroupCache kgCache=UFOCacheManager.getSingleton().getKeyGroupCache();
		ReportCache repCache=UFOCacheManager.getSingleton().getReportCache();

		ReportVO report=repCache.getByPks(new String[]{strRepID})[0];
		KeyGroupVO mainKeyGroup=kgCache.getByPK(report.getPk_key_comb());
//		KeyVO[] mainKeys=mainKeyGroup.getKeys();

		MeasurePubDataVO mainPubData=MeasurePubDataBO_Client.findByAloneID(report.getPk_key_comb(),strAloneID);
		if(mainPubData == null){
			mainPubData = new MeasurePubDataVO();
			mainPubData.setKeyGroup(mainKeyGroup);
			mainPubData.setKType(report.getPk_key_comb());
		}
//		mainPubData.setAccSchemePK(taskCache.getTaskVO(strTaskID).getAccPeriodScheme());

        //判判指标对应的关键字组合是否含有私有关键字
//        MeasureVO measure=measCache.getMeasure((String)totalVals[0].getMeasID().get(0));
//        KeyVO[] measKeys=kgCache.getByPK(measure.getKeyCombPK()).getKeys();
//        boolean bHasPrivate=isHavePriateKey(destKeys);

        //查找汇总结果来源
        TotalCellKeyValue[][] retVals=new TotalCellKeyValue[totalVals.length][];
        TotalRepDataDMO repDataDMO = new TotalRepDataDMO();
        for (int i=0;i<totalVals.length;i++){
        	TotalCellKeyValue[] oneRetVals=null;

        	TotalCellKeyValue oneSourVal=totalVals[i];
        	Vector<String> vMeasStoreCellID=oneSourVal.getMeasID();
        	Vector<String> vStoreCellID = new Vector<String>();
        	Vector<String> vMeasureID = new Vector<String>();
        	for(String pk : vMeasStoreCellID){
        		if(pk.startsWith("@"))
        			vStoreCellID.add(pk.substring(1,pk.length()));
        		else
        			vMeasureID.add(pk);
        	}
        	String[] strKeyValues=oneSourVal.getKeyVals();

        	//对指标按所处数据表进行分组
        	Hashtable<String,List<IStoreCell>> hashTable=groupMeasure(vMeasStoreCellID.toArray(new String[0]), strRepID);//????改了这个里面vMeasureID
//        	Hashtable<String,List<IStoreCell>> storeCellTable = groupStoreCell(vStoreCellID.toArray(new String[0]), strRepID);
//        	hashTable.putAll(storeCellTable);
        	Vector<String> vecSource= new Vector<String>();
        	vecSource.addAll(vStoreCellID);
        	vecSource.addAll(vMeasureID);
        	//对指标按数据表进行循环
        	Enumeration<String> enumTable=hashTable.keys();
        	while (enumTable.hasMoreElements()){
        		String strDBTable=enumTable.nextElement();
        		List<IStoreCell> vMeasure=hashTable.get(strDBTable);
        		IStoreCell[] measures=vMeasure.toArray(new IStoreCell[0]);

        		TotalCellKeyValue[] oneTmpVals=null;
        		//固定区内的指标
        		if (strKeyValues==null || strKeyValues.length<=0)
        		{
        			oneTmpVals=repDataDMO.loadTotalSourValsByPub(measures,strSQL,destKeys,hashKeyPos,mainPubData,mainPubData);
        		}
        		//公有关键字的指标
//        		else if (!bHasPrivate){
        		else
        		{
        			MeasurePubDataVO subPubData=getPubDataByKeyVals(mainPubData,strKeyValues,measures[0].getKeyCombPK());
        			oneTmpVals=repDataDMO.loadTotalSourValsByPub(measures,strSQL,destKeys,hashKeyPos,mainPubData,subPubData);
        		}
        		//私有关键字的指标
//        		else{
//        			MeasurePubDataVO tmpMainPubData=(MeasurePubDataVO)mainPubData.clone();
//        			tmpMainPubData.setKeywords(new String[mainPubData.getKeywords().length]);
//        			oneTmpVals=dataDMO.loadTotalSourValsByPriv(measures,strSQL,tmpMainPubData,mainKeys,measKeys,destKeys,hashKeyPos,strKeyValues);
//        		}

        		//将得到的源数据与上次查找到的数据进行合并
        		oneRetVals=mergeTotalSourVals(oneRetVals,oneTmpVals,vecSource);
        	}

        	//对结果进行排序
        	oneRetVals=doSortSourValue(oneRetVals,destKeys,hashKeyPos);
        	retVals[i]=oneRetVals;
        }
        return retVals;
	}

	*//**
	 * 得到汇总组织条件sql
	 * @create by xulm at 2010-5-10,下午04:52:14
	 *
	 * @param totalScheme
	 * @return
	 * @throws Exception
	 *//*
	@SuppressWarnings({ "unchecked"})
	private String getTotalOrgCondSQL(MeasurePubDataVO pubData, String busiTime,TotalSchemeVO totalScheme,MeasurePubDataVO balancePubData,String pk_task) throws UFOSrvException{

		try {
			String rmsVerPk=RMSUtil.getRmsVerPk(totalScheme.getPk_rms(), pubData,busiTime);
			
			BaseDAO dao=new BaseDAO();
			StringBuffer bufferCond = new StringBuffer();
			if (totalScheme.getOrg_type()==TotalSchemeVO.TYPE_FREE){
				//这里需要注意超过200个的处理
				ArrayList<String> lstOrgId=(ArrayList<String>)totalScheme.getOrg_content();
				if (lstOrgId!=null){
					if(lstOrgId.size() < 1000) {
						for(int i=0;i<lstOrgId.size();i++){
							if (i > 0) {
								bufferCond.append(",");
							}
							bufferCond.append("'"+lstOrgId.get(i)+"'");
						}
					}
					else{
						String subSql = "select pk_org from org_ReportManaStruMember_v o where o.pk_rms = '" + totalScheme.getPk_rms() + "' and pk_svid= '"+ rmsVerPk + "'";
						String inCond = UFDSSqlUtil.buildInSql("pk_org", lstOrgId.toArray(new String[0]));
						bufferCond.append(subSql).append(" and ").append(inCond);
					}
				}
			}
			else if (totalScheme.getOrg_type()==TotalSchemeVO.TYPE_CUSTOMER){
				//这里需要注意超过200个的处理
				ArrayList<String> lstOrgId=(ArrayList<String>)totalScheme.getOrg_content();
				if (lstOrgId!=null)
				{
					if(balancePubData!=null){
                    	// 差额单位查看汇总来源处理,把差额单位去掉,将上级单位加入
						lstOrgId.remove(balancePubData.getUnitPK());
						lstOrgId.add(pubData.getUnitPK());
                    }
//					StringBuffer tempBuffer = new StringBuffer();
//					for(int i=0;i<lstOrgId.size();i++)
//					{
//						if (i > 0) {
//							tempBuffer.append(",");
//						}
//						tempBuffer.append("'"+lstOrgId.get(i)+"'");
//					}
					//自定义时，汇总组织范围只保留当前组织及其下级--jiaah at 2011-5-12
//					Collection set = dao.retrieveByClause(ReportManaStruMemberVO.class,"pk_rms='" + totalScheme.getPk_rms() + "' and pk_org= '"+ totalScheme.getPk_org() + "'");
					Collection set = dao.retrieveByClause(ReportManaStruMemberVersionVO.class,"pk_svid='"+rmsVerPk +"' and pk_org='"+totalScheme.getPk_org()+"'");
					if (set == null || set.size() <= 0)
						return null;
					String strInnerCode = ((ReportManaStruMemberVersionVO)set.toArray()[0]).getInnercode();
					String tempBuffer = UFDSSqlUtil.buildInSql("o.pk_org", lstOrgId.toArray(new String[0]));
					String sql = "select o.pk_org from org_reportmanastrumember_v o where o.pk_svid = '" + rmsVerPk + "' and " + tempBuffer; //"o.pk_org in (" + tempBuffer + ") " ;
					if(balancePubData == null) {
						// 非差额单位只能看下级数据，不能包含本级数据
						sql = sql + " and o.innercode <> '" + strInnerCode + "' ";
					}
					sql = sql +	" and o.innercode like '" + strInnerCode + "%' " ;
					if(pk_task != null) {
						sql += " and pk_org in (SELECT PK_RECEIVEORG FROM IUFO_TASKASSIGN where pk_task = '" + pk_task + "')" ;
					}
					sql += " order by o.innercode desc";
					ArrayList<String> lstIds = (ArrayList<String>) dao.executeQuery(sql, new IDQryProcessor());

					if(lstIds != null && lstIds.size() > 0){
						bufferCond = new StringBuffer();
						if(lstIds.size() < 1000) {
							for(int i=0;i<lstIds.size();i++)
							{
								if (i > 0) {
									bufferCond.append(",");
								}
								bufferCond.append("'"+lstIds.get(i)+"'");
							}
						}
						else{
							String subSql = "select pk_org from org_ReportManaStruMember_v o where o.pk_rms = '" + totalScheme.getPk_rms() + "' and pk_svid= '"+ rmsVerPk + "'";
							String inCond = UFDSSqlUtil.buildInSql("pk_org", lstOrgId.toArray(new String[0]));
							bufferCond.append(subSql).append(" and ").append(inCond);
						}
					}
				}
			}else
			{
//				Collection set=dao.retrieveByClause(ReportManaStruMemberVO.class,"pk_rms='"+totalScheme.getPk_rms() +"' and pk_org='"+totalScheme.getPk_org()+"'");
				Collection set=dao.retrieveByClause(ReportManaStruMemberVersionVO.class,"pk_svid='"+rmsVerPk +"' and pk_org='"+totalScheme.getPk_org()+"'");
				
				if (set==null || set.size()<=0)
				{
					throw new UFOSrvException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1139")@res "没有找到组织成员");
				}
				String strInnerCode=((ReportManaStruMemberVersionVO)set.toArray()[0]).getInnercode();
			    String strOrgPk=null;
				if (!StringUtil.isEmptyWithTrim((String) totalScheme.getOrg_cond_sql()))
			    {
			    	bufferCond.append(" select t1.pk_org  from org_reportmanastrumember_v t1  left join  org_reportorg t2 on  t1.pk_org=t2.pk_reportorg  where t1.pk_svid='"+rmsVerPk+"' and t1.innercode like '"+strInnerCode+"%'") ;
			    	if (totalScheme.getOrg_type()==TotalSchemeVO.TYPE_DIRECTSUB)
					{
					    if (dao.getDBType()==DBConsts.ORACLE || dao.getDBType()==DBConsts.DB2)
					    {
					    	bufferCond.append(" and t1.innercode <> '"+strInnerCode+"'  and length(innercode)=" +(strInnerCode.trim().length()+InnerCodeUtil.INNERCODELENGTH));
					    }else
					    {
					    	bufferCond.append(" and t1.innercode <> '"+strInnerCode+"'  and len(innercode)=" +(strInnerCode.trim().length()+InnerCodeUtil.INNERCODELENGTH));
					    }
					}else if (totalScheme.getOrg_type()==TotalSchemeVO.TYPE_LEAFSUB)
					{
						bufferCond.append(" and t1.innercode <> '"+strInnerCode+"' and not EXISTS  (select 1 from org_reportmanastrumember_v where t1.pk_org=pk_fatherorg and pk_svid = '"+rmsVerPk+"' )");
					}
				    if (!StringUtil.isEmptyWithTrim((String) totalScheme.getOrg_cond_sql()))
				    {
				    	bufferCond.append(" and  EXISTS (select 1 from org_reportorg where pk_reportorg =t2.pk_reportorg and "+totalScheme.getOrg_cond_sql()+")");
				    }
				    strOrgPk="t1.pk_org";
			    }else
			    {
			    	bufferCond.append(" select pk_org from org_reportmanastrumember_v t1  where t1.pk_svid='"+rmsVerPk+"' and t1.innercode like '"+strInnerCode+"%'") ;
				    if (totalScheme.getOrg_type()==TotalSchemeVO.TYPE_DIRECTSUB)
					{
				    	if (dao.getDBType()==DBConsts.ORACLE || dao.getDBType()==DBConsts.DB2)
				    	{
				    		bufferCond.append(" and t1.innercode <> '"+strInnerCode+"'  and length(t1.innercode)=" +(strInnerCode.trim().length()+InnerCodeUtil.INNERCODELENGTH));
				    	}else
				    	{
				    		bufferCond.append(" and t1.innercode <> '"+strInnerCode+"'  and len(t1.innercode)=" +(strInnerCode.trim().length()+InnerCodeUtil.INNERCODELENGTH));
				    	}
					}else if (totalScheme.getOrg_type()==TotalSchemeVO.TYPE_LEAFSUB)
					{
						bufferCond.append(" and t1.innercode <> '"+strInnerCode+"' and not EXISTS  (select 1 from org_reportmanastrumember_v where t1.pk_org=pk_fatherorg and pk_svid = '" + rmsVerPk + "' )");
					}
				    strOrgPk="pk_org";
			    }
				if(balancePubData!=null){	//差额汇总处理
					bufferCond.append(" and ");
					bufferCond.append(strOrgPk);
					bufferCond.append("<>'");
					bufferCond.append(balancePubData.getUnitPK());
					bufferCond.append("' or ");
					bufferCond.append(strOrgPk);
					bufferCond.append("='");
					bufferCond.append(pubData.getUnitPK());
					bufferCond.append("'");
				}
				if(pk_task != null) {
					bufferCond.append(" and t1.pk_org in (SELECT PK_RECEIVEORG FROM IUFO_TASKASSIGN where pk_task = '" + pk_task + "')");
				}
			}
			
			
			StringBuffer buffer = new StringBuffer();
			if(bufferCond.length() == 0)
				return null;
			buffer= buffer.append( "select * from " +pubData.getKeyGroup().getTableName() + " where keyword1 in (").append(bufferCond).append(" )  and ver=0"); ;
			// 设置除“单位”关键字之外的其他关键字值条件
			String keyCond = MeasurePubdataSqlUtil.getKeywordCond(pubData, pubData.getKeyGroup());
			if (keyCond.length() > 0) {
				buffer.append(" and " +keyCond);
			}

	        return buffer.toString();
		} catch (Exception ex) {
			AppDebug.debug(ex);
			throw new UFOSrvException(ex.getMessage());
		}
	}


	*//**
	 * 汇总结果溯源的主方法
	 *//*
	@Override
	@SuppressWarnings("unchecked")
	public TotalCellKeyValue[][] getTotalSoure(TotalCellKeyValue[] totalVals,String strAloneID,String strRepID,KeyVO[] destKeys,Hashtable hashKeyPos,MeasurePubDataVO mainPubData,String busiTime,String strTaskID, String strRmsPK, String strOrgPK,TotalSchemeVO totalScheme) throws UFOSrvException{
		try{
			// @edit by wuyongc at 2013-6-18,上午9:20:51 获得前台的pubData，根据aloneid从后台可能获取不到，因为数据库中可能还不存在！
//			KeyGroupCache kgCache=UFOCacheManager.getSingleton().getKeyGroupCache();
//			ReportCache repCache=UFOCacheManager.getSingleton().getReportCache();
//			ReportVO report=repCache.getByPks(new String[]{strRepID})[0];
//			KeyGroupVO mainKeyGroup=kgCache.getByPK(report.getPk_key_comb());
//			MeasurePubDataVO mainPubData= MeasurePubDataBO_Client.findByAloneID(report.getPk_key_comb(),strAloneID);
//			if(mainPubData == null){
//				mainPubData = new MeasurePubDataVO();
//				mainPubData.setKeyGroup(mainKeyGroup);
//			}
	        //得到条件筛选的SQL语句
//			String strSQL=getTotalOrgCondSQL(mainPubData,strUserID,strTaskID,strOrgPK);
			
			//判断当前是否是差额单位
			MeasurePubDataVO balancePubData=null;
			MeasurePubDataVO targetPubData=(MeasurePubDataVO)mainPubData.clone();
			if(totalScheme == null || (totalScheme != null && totalScheme.getOrg_type()!=TotalSchemeVO.TYPE_FREE)){
				// 自由汇总不需要校验差额单位
				ReportManaStruMemberVersionVO memVO=RMSUtil.getRmsMemberVerVO(strOrgPK, strRmsPK,mainPubData,busiTime);
				if (memVO==null) {
					throw new UFOSrvException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1139")@res "没有找到组织成员");
				}
				if(memVO.getIsbalanceunit() != null && memVO.getIsbalanceunit().booleanValue()){	//是差额单位
					String fatherUnitPk=memVO.getPk_fatherorg();
					balancePubData=(MeasurePubDataVO)mainPubData.clone();
					targetPubData.setAloneID(null);
					targetPubData.setKeywordByPK(KeyVO.CORP_PK, fatherUnitPk);
					targetPubData.setAloneID(MeasurePubDataBO_Client.getAloneID(targetPubData));
					strOrgPK=fatherUnitPk;
				}
			}
			
			if (totalScheme==null)
			{
				String rmsverpk = RMSUtil.getRmsVerPk(strRmsPK, mainPubData, busiTime);
				//对于应用的方案，需要重新给方案设置所属组织的值
				totalScheme=getAppTotalScheme(strRmsPK,strOrgPK,null,rmsverpk);
				//如果汇总规则为不汇总，不能查看汇总来源 modified by jiaah
				if (totalScheme == null || totalScheme.getOrg_type() == TotalSchemeVO.TYPE_NONE){
					throw new Exception(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1143")@res "没有汇总方案，不能查看汇总来源");
				}
				totalScheme.setPk_org(strOrgPK);
			}
			if(totalScheme.getPk_rms() == null) {
				totalScheme.setPk_rms(strRmsPK);
			}
			String strSQL=getTotalOrgCondSQL(targetPubData,busiTime, totalScheme,balancePubData,strTaskID);
			
			if(strSQL == null){
				return null;
			}
			strSQL="("+strSQL+")";

			return innerLoadSourVals(totalVals, strTaskID,strSQL, strAloneID, strRepID, destKeys, hashKeyPos);
		}
		catch(Exception e){
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage(),e);
		}
	}

	*//**
	 * 由于一组可能在多个数据表中，而对于一个关键字值的筛选条件，其返回的源数据的行数可能是不相同的，这时需要将其组成一个并集
	 * @param vals1
	 * @param vals2
	 * @param vMeasID
	 * @return
	 *//*
	private TotalCellKeyValue[] mergeTotalSourVals(TotalCellKeyValue[] vals1,TotalCellKeyValue[] vals2,Vector<String> vMeasID){
		if (vals1==null && vals2==null)
			return null;

		vals2=fitByMeasure(vals2,vMeasID);

		if (vals1==null)
			return vals2;
		else if (vals2==null)
			return vals1;

		Vector<TotalCellKeyValue> vRetVal=new Vector<TotalCellKeyValue>();
		for (TotalCellKeyValue element : vals1) {
			int j=0;
			for (;j<vals2.length;j++){
				if (compKeyVals(element.getKeyVals(),vals2[j].getKeyVals())==0)
					break;
			}
			if (j<vals2.length)
				vRetVal.add(doMergeSourVals(element,vals2[j]));
			else
				vRetVal.add(element);
		}

		for (TotalCellKeyValue element : vals2) {
			int j=0;
			for (;j<vals1.length;j++){
				if (compKeyVals(vals1[j].getKeyVals(),element.getKeyVals())==0)
					break;
			}
			if (j>=vals1.length)
				vRetVal.add(element);
		}

		return vRetVal.toArray(new TotalCellKeyValue[0]);
	}

	*//**
	 * 两组结构完全相同的值，进行叠加
	 * @param val1
	 * @param val2
	 * @return
	 *//*
	private TotalCellKeyValue doMergeSourVals(TotalCellKeyValue val1,TotalCellKeyValue val2){
		TotalCellKeyValue retValue=new TotalCellKeyValue();
		retValue.setKeyVals(val1.getKeyVals());
		retValue.setMainAloneID(val1.getMainAloneID());
		retValue.setDynAloneID(val1.getDynAloneID());
		retValue.setUnitPK(val1.getUnitPK());

		Vector<String> vMeasID=new Vector<String>(Arrays.asList(val1.getMeasID().toArray(new String[0])));
		retValue.setMeasID(vMeasID);

		Vector<Object> vValue=new Vector<Object>();
		for (int i=0;i<vMeasID.size();i++){
			Object strVal1=val1.getVals().get(i);
			Object strVal2=val2.getVals().get(i);
			vValue.add(strVal1!=null?strVal1:strVal2);
		}
		retValue.setVals(vValue);
		return retValue;
	}

	*//**
	 * 比较两组关键值是否相等
	 * @param strVal1s
	 * @param strVal2s
	 * @return
	 *//*
	private int compKeyVals(String[] strVal1s,String[] strVal2s){
		for (int i=0;i<strVal1s.length;i++){
			int iComp=strVal1s[i].compareTo(strVal2s[i]);
			if (iComp!=0)
				return iComp;
		}
		return 0;
	}

	*//**
	 * 将TotalCellKeyValue中有些指标不在vMeasID中，将这些空的数组元素填上空值
	 * @param vals
	 * @param vMeasID
	 * @return
	 *//*
	private TotalCellKeyValue[] fitByMeasure(TotalCellKeyValue[] vals,Vector<String> vMeasID){
		if (vals==null || vals.length<=0)
			return null;

		for (TotalCellKeyValue oneVal : vals) {
			Vector<String> vOneMeasID=oneVal.getMeasID();
			Vector<Object> vOneValue=new Vector<Object>();
			for (int j=0;j<vMeasID.size();j++){
				int iIndex=vOneMeasID.indexOf(vMeasID.get(j));
				if (iIndex>=0)
					vOneValue.add(oneVal.getVals().get(iIndex));
				else
					vOneValue.add(null);
			}
			oneVal.setMeasID(new Vector<String>(Arrays.asList(vMeasID.toArray(new String[0]))));
			oneVal.setVals(vOneValue);
		}
		return vals;
	}

	*//**
	 * 将指标按所处数据表进行分组
	 * @param strMeasIDs
	 * @return
	 * @throws Exception
	 *//*
	private Hashtable<String,List<IStoreCell>> groupMeasure(String[] strMeasIDs,String pk_report) throws Exception{
//		MeasureCache measCache=UFOCacheManager.getSingleton().getMeasureCache();
//		Hashtable<String,List<IStoreCell>> hashTable=new Hashtable<String,List<IStoreCell>>();
//	    for(int i = 0; i < strMeasIDs.length; i++){
//	    	MeasureVO measure=measCache.getMeasure(strMeasIDs[i]);
////	        if(measure.getType() != MeasureVO.TYPE_NUMBER)
////	            continue;
//	        List<IStoreCell> vMeasure = hashTable.get(measure.getDbtable());
//	        if(vMeasure == null){
//	            vMeasure = new Vector<IStoreCell>();
//	            hashTable.put(measure.getDbtable(), vMeasure);
//	        }
//	        if(vMeasure.contains(measure) == false)
//	            vMeasure.add(measure);
//	    }
//	    return hashTable;


		MeasureCache measCache=UFOCacheManager.getSingleton().getMeasureCache();
		Hashtable<String,List<IStoreCell>> hashTable=new Hashtable<String,List<IStoreCell>>();
		IStoreCell measure = null;

	    for (String strMeasID : strMeasIDs) {
	    	if(strMeasID.startsWith("@")){
	    		IStoreCellPackQrySrv storeCellQry = NCLocator.getInstance().lookup(IStoreCellPackQrySrv.class);
	    		IStoreCell[] storeCells = storeCellQry.getStoreCellsByPosAry(pk_report, new String[]{strMeasID.substring(1, strMeasID.length())});
	    		measure = storeCells[0];
	    	}
	    	else
	    		measure = measCache.getMeasure(strMeasID);
//	        if(measure.getType() != MeasureVO.TYPE_NUMBER)
//	            continue;
	        List<IStoreCell> vMeasure = hashTable.get(measure.getDbtable());
	        if(vMeasure == null){
	            vMeasure = new Vector<IStoreCell>();
	            hashTable.put(measure.getDbtable(), vMeasure);
	        }
	        if(vMeasure.contains(measure) == false)
	            vMeasure.add(measure);
	    }
	    return hashTable;
	}

//	private Hashtable<String,List<IStoreCell>> groupStoreCell(String[] strStoreCellIDs,String pk_report) throws Exception{
//		IStoreCellPackQrySrv storeCellQry = NCLocator.getInstance().lookup(IStoreCellPackQrySrv.class);
//		Hashtable<String,List<IStoreCell>> hashTable=new Hashtable<String,List<IStoreCell>>();
//		IStoreCell[] storeCells = storeCellQry.getStoreCellsByPosAry(pk_report, strStoreCellIDs);
//	    for(int i = 0; i < storeCells.length; i++){
//	    	IStoreCell storeCell = storeCells[i];
////	        if(measure.getType() != MeasureVO.TYPE_NUMBER)
////	            continue;
//	        List<IStoreCell> vMeasure = hashTable.get(storeCell.getDbtable());
//	        if(vMeasure == null){
//	            vMeasure = new Vector<IStoreCell>();
//	            hashTable.put(storeCell.getDbtable(), vMeasure);
//	        }
//	        if(vMeasure.contains(storeCell) == false)
//	            vMeasure.add(storeCell);
//	    }
//	    return hashTable;
//	}


	*//**
	 * 将一组源数据的值按关键字进行顺序
	 * @param vals
	 * @param keys
	 * @param hashPos
	 *//*
	@SuppressWarnings("unchecked")
	private TotalCellKeyValue[] doSortSourValue(TotalCellKeyValue[] vals,KeyVO[] keys,Hashtable hashPos){
		if (vals==null || vals.length<=0)
			return vals;
		TotalValueSortVO[] sorts=new TotalValueSortVO[vals.length];
		for (int i=0;i<sorts.length;i++){
			sorts[i]=new TotalValueSortVO(vals[i],keys,hashPos);
		}
		Arrays.sort(sorts);
		TotalCellKeyValue[] retVals=new TotalCellKeyValue[sorts.length];
		for (int i=0;i<retVals.length;i++)
			retVals[i]=sorts[i].m_Val;
		return retVals;
	}

	*//**
	 * 根据主表关键字的值和动态区关键字的值，得到该行对应的MeasurePubDataVO
	 * @param mainPubData
	 * @param strKeyValues
	 * @param strDestKeyCombPK
	 * @return
	 * @throws Exception
	 *//*
	private MeasurePubDataVO getPubDataByKeyVals(MeasurePubDataVO mainPubData,String[] strKeyValues,String strDestKeyCombPK) throws Exception{
		MeasurePubDataVO retPubData=(MeasurePubDataVO)mainPubData.clone();
		KeyGroupCache kgCache=UFOCacheManager.getSingleton().getKeyGroupCache();
		retPubData.setKType(strDestKeyCombPK);
		retPubData.setKeyGroup(kgCache.getByPK(strDestKeyCombPK));
		KeyVO[] mainKeys=kgCache.getByPK(mainPubData.getKType()).getKeys();
		KeyVO[] destKeys=kgCache.getByPK(strDestKeyCombPK).getKeys();

		int iPos=0;
		for (KeyVO destKey : destKeys) {
			boolean bFind=false;
			for (KeyVO mainKey : mainKeys) {
				if (destKey.equals(mainKey)){
					bFind=true;
					break;
				}
			}
			if (!bFind)
				retPubData.setKeywordByName(destKey.getName(),strKeyValues[iPos++]);
			else
				retPubData.setKeywordByName(destKey.getName(),null);
		}
		return retPubData;
	}

	*//**
	 * 用于汇总源数据排序的辅助类
	 * @author weixl
	 *//*
	@SuppressWarnings("unchecked")
	class TotalValueSortVO implements Comparable{
	 	TotalCellKeyValue m_Val;
	 	KeyVO[] m_Keys;
	 	Hashtable m_hashPos;
//	 	UnitCache unitCache=UFOCacheManager.getSingleton().getUnitCache();
	 	public TotalValueSortVO(TotalCellKeyValue val,KeyVO[] keys,Hashtable hashPos){
	 		m_Val=val;
	 		m_Keys=keys;
	 		m_hashPos=hashPos;
	 	}
	 	@Override
		public int compareTo(Object o){
	 		String[] strKeyVals=m_Val.getKeyVals();
	 		String[] strOtherKeyVals=((TotalValueSortVO)o).m_Val.getKeyVals();
	 		for (int i=0;i<strKeyVals.length;i++){
	 			int iComp=0;
	 			int iPos=((Integer)m_hashPos.get(Integer.valueOf(i))).intValue();
	 			@SuppressWarnings("unused")
				KeyVO key=m_Keys[iPos];
//	 			if (key.isPrivate() && key.getName().equals("行号")){//MeasureData.KEY_NAME_ROW_INDEX)){
//	 				try{
//	 					int iVal1=Integer.parseInt(strKeyVals[i]);
//	 					int iVal2=Integer.parseInt(strOtherKeyVals[i]);
//	 					if (iVal1==iVal2)
//	 						iComp=0;
//	 					else if (iVal1>iVal2)
//	 						iComp=1;
//	 					else
//	 						iComp=-1;
//	 				}
//	 				catch(Exception e){
//	 				}
//	 			}
//	 			else{
	 			    //为什么比较单位关键字的时候，必须用编码比较
//	 				if (key.getPk_keyword().equals(KeyVO.CORP_PK)){
//	 					UnitInfoVO unitInfo=unitCache.getUnitInfoByPK(strKeyVals[i]);
//	 					UnitInfoVO unitInfoOther=unitCache.getUnitInfoByPK(strOtherKeyVals[i]);
//
//	 					if (unitInfo!=null && unitInfoOther!=null && unitInfo.getCode()!=null && unitInfoOther.getCode()!=null){
//	 						iComp=compKeyVals(new String[]{unitInfo.getCode()},new String[]{unitInfoOther.getCode()});
//	 					}else
//	 						iComp=compKeyVals(new String[]{strKeyVals[i]},new String[]{strOtherKeyVals[i]});
//	 				}
//	 				else
	 					iComp=compKeyVals(new String[]{strKeyVals[i]},new String[]{strOtherKeyVals[i]});
//	 			}
	 			if (iComp!=0)
	 				return iComp;
	 		}
	 		return 0;
	 	}
	}

	*//**
	 * 得到动态区关键字与主表关键字的对应关系，即对于主表中某一关键字，其在动态区关键字中位于第几的位置
	 * @param subPubData
	 * @param mainPubData
	 * @return
	 *//*
	@SuppressWarnings("unused")
	private Hashtable<Integer,Integer> genKeywordPos(MeasurePubDataVO subPubData,MeasurePubDataVO mainPubData){
	 	KeyVO[] mainKeys=mainPubData.getKeyGroup().getKeys();
	 	KeyVO[] subKeys=subPubData.getKeyGroup().getKeys();
		Hashtable<Integer,Integer> hashPos=new Hashtable<Integer,Integer>();
		for (int i=0;i<mainKeys.length;i++)
		{
			for (int j=0;j<subKeys.length;j++)
			{
				if (subKeys[j].equals(mainKeys[i]))
				{
					hashPos.put(Integer.valueOf(i),Integer.valueOf(j));
					break;
				}
			}
		}
		return hashPos;
	}

	private class TotalQryProcessor extends BaseProcessor{

		private static final long serialVersionUID = 1L;

		@Override
		public Object processResultSet(ResultSet rs) throws SQLException {
            ArrayList<TotalSchemeVO> lstTotalScheme=new ArrayList<TotalSchemeVO>();
			while(rs.next()){
				TotalSchemeVO totalSchemeVO=new TotalSchemeVO();
				totalSchemeVO.setPk_totalscheme(rs.getString("pk_totalscheme"));
				totalSchemeVO.setPk_org(rs.getString("pk_org"));
				totalSchemeVO.setPk_rms(rs.getString("pk_rms"));
				totalSchemeVO.setOrg_type(rs.getInt("org_type"));
				//org_condition改为blob类型  modified by jiaah
				byte[] objBytes = null;
				try {
					objBytes = ((CrossDBResultSet)rs).getBlobBytes("org_condition");
				} catch (IOException e) {
					AppDebug.debug(e.getMessage());
				}
				String format = null;
				if(objBytes != null && objBytes.length > 0){
					format = (String)convertBytesToObject(objBytes);
		        }
				totalSchemeVO.setOrg_condition(format);
//				totalSchemeVO.setOrg_condition("org_condition");
				totalSchemeVO.setId(rs.getString("pk_rmsmember"));
				totalSchemeVO.setPid(rs.getString("pk_fathermember"));
				totalSchemeVO.setInnercode(rs.getString("innercode"));
				totalSchemeVO.setTs(rs.getString("ts")==null?null:new UFDateTime(rs.getString("ts")));
				
				totalSchemeVO.setPk_rmsversion(rs.getString("pk_rmsversion"));

				lstTotalScheme.add(totalSchemeVO);
			}
			return lstTotalScheme;
		}
	}
	private class OrgTypeProcessor extends BaseProcessor{
		
		@Override
		public Object processResultSet(ResultSet rs) throws SQLException {
			  ArrayList<Integer> lstOrgType=new ArrayList<Integer>();
			  while(rs.next()){
				  int org_type=rs.getInt("org_type");
				  lstOrgType.add(org_type);
			  }
			return lstOrgType;
		}
		
	}
	*//**
	 * 当前汇总方案相关的汇总组织
	 * @create by jiaah at 2011-3-24,下午01:47:20
	 * @param totalSchemes
	 * @return String[] orgIds
	 * @throws Exception
	 *//*
	@SuppressWarnings("unchecked")
	private String[] getReviewOrgIds(TotalSchemeVO[] totalSchemes,String date) throws Exception {
		ArrayList<String> lstOrgId = new ArrayList<String>();
		BaseDAO dao = new BaseDAO();
		// @edit by wuyongc at 2013-6-8,上午10:23:37 修改为合并为一条SQL执行。
		StringBuilder sqlSb = new StringBuilder();
		//获取汇总方案关联的组织
		for(TotalSchemeVO totalScheme : totalSchemes){
			if (totalScheme.getOrg_type() == TotalSchemeVO.TYPE_CUSTOMER){
				lstOrgId.addAll((ArrayList<String>)totalScheme.getOrg_content());
			}else if (totalScheme.getOrg_type() == TotalSchemeVO.TYPE_NONE){

			}else{
				String rmsVerPk=RMSUtil.getPKRmsVersionByPkRms(totalScheme.getPk_rms(), date);
				//预览时，与任务无关
				String sql = new TotalManageImpl().getTotalOrgCondSQL(totalScheme,rmsVerPk,null);
				if(StringUtils.isNotEmpty(sql)){
					if(sqlSb.length() > 0){
						sqlSb.append(" union ");
					}
					sqlSb.append(sql);
				}
			}
		}
		if(sqlSb.length()>0){
			lstOrgId.addAll((ArrayList<String>)dao.executeQuery(sqlSb.toString(), new IDQryProcessor()));
		}
		return lstOrgId.toArray(new String[0]);
	}

	*//**
	 * String[]拼接成'a','b','c'的形式,放置在in();
	 * @create by jiaah at 2010-11-11,下午07:20:32
	 *
	 * @return
	 *//*
	private  StringBuffer getConnectString(String[] arrayString){
		StringBuffer strBuffer = new StringBuffer("");
		if(arrayString != null && arrayString.length >0){
			for(int i = 0; i < arrayString.length; i++){
				strBuffer.append("'");
				strBuffer.append(arrayString[i]);
				strBuffer.append("'");
				if(i < arrayString.length - 1){
					strBuffer.append(",");
				}
			}
		}
		return strBuffer;
	}

	*//**
	 * 预览汇总组织范围
	 * @author jiaah
	 * @throws Exception
	 * @created at 2011-3-24,上午11:23:37
	 * @return TotalSchemeVO[]
	 *
	 *//*
	@Override
	@SuppressWarnings({"unchecked" })
	public TotalSchemeVO[] getReviewTotalScheme(TotalSchemeVO[] totalSchemes,String date) throws Exception{
		try {
			BaseDAO baseDao = new BaseDAO();
			if(totalSchemes[0] == null){
				throw new Exception(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1144")@res "汇总方案已删除");
			}
			String rmsId = totalSchemes[0].getPk_rms();
			String orgId = totalSchemes[0].getPk_org();
			//获得当前规则相关的汇总组织id数组
			String[] orgIds = getReviewOrgIds(totalSchemes,date);
			//转换为in()查询条件字符串
			
//			StringBuffer sbOrgIds = getConnectString(orgIds);
			String sbOrgIds = UFDSSqlUtil.buildInSql("o.pk_org", orgIds);
			String strRmsVersionPK = RMSUtil.getPKRmsVersionByPkRms(rmsId, date);
			Collection set=baseDao.retrieveByClause(ReportManaStruMemberVersionVO.class,"pk_svid='"+strRmsVersionPK+"' and pk_org='"+orgId+"'");
			
			if (set == null || set.size() <= 0)
				return null;
			String strInnerCode = ((ReportManaStruMemberVersionVO)set.toArray()[0]).getInnercode();

			ArrayList<TotalSchemeVO> lstTotalScheme = new ArrayList<TotalSchemeVO>();

			String sql = null;
			//自定义时去掉本组织；加sql条件o.innercode <> '"+strInnerCode+"'  modified by jiaah
			if(sbOrgIds != null && sbOrgIds.length() > 0){
				sql = " select i.pk_totalscheme,o.pk_org,i.org_type,i.org_condition,i.org_content,i.org_cond_show,o.pk_rmsmember,o.pk_fathermember,o.innercode,i.ts,o.pk_rms,o.pk_svid as pk_rmsversion from org_ReportManaStruMember_v o " +
				"left join  (select * from iufo_total_scheme where pk_rms='" + rmsId +"' and pk_rmsversion ='" + strRmsVersionPK +"') i " +
				"on o.pk_org=i.pk_org where o.pk_rms='" + rmsId + "' and " + sbOrgIds +" and o.innercode <> '"+strInnerCode+"' and o.innercode like '" + strInnerCode + "%' order by o.innercode desc" ;
				lstTotalScheme = (ArrayList<TotalSchemeVO>) baseDao.executeQuery(sql,new TotalQryProcessor());
			}
			TotalSchemeVO[] totalSchemeVOs = new TotalSchemeVO[0];
			if (lstTotalScheme != null && lstTotalScheme.size() > 0){
				totalSchemeVOs = lstTotalScheme.toArray(new TotalSchemeVO[lstTotalScheme.size()]);
				groupFillAppSchemePK(totalSchemeVOs);
			}
			return totalSchemeVOs;
		}catch (Exception e) {
			AppDebug.debug(e);
			throw new Exception(e.getMessage());
		}
	}

    private class IDQryProcessor extends BaseProcessor{
    	private static final long serialVersionUID = 803213224379610245L;
    	@Override
    	public Object processResultSet(ResultSet rs) throws SQLException {
    		ArrayList<String> lstIds = new ArrayList<String>();
    		while(rs.next()){
    			lstIds.add(rs.getString(1));
    		}
    		return lstIds;
    	}
    }
    
    *//**
	 * 一次查询汇总所需信息，减少连接数
	 * 
	 * @param rmsId
	 * @param orgId
	 * @param taskPk
	 * @param pubData
	 * @return
	 * @throws UFOSrvException
	 *//*
    @Override
	public Object[] getTotalSchemeInfo(String rmsId,String orgId, String taskPk, MeasurePubDataVO pubData, String date) throws UFOSrvException {
    	Object[] result = new Object[5];
		try {
			String rmsVpk = RMSUtil.getRmsVerPk(rmsId, pubData, date);
			// 汇总方案
			TotalSchemeVO schemeVo = getAppTotalScheme(rmsId, orgId, null, rmsVpk);
			result[0] = schemeVo;

			// 对应aloneid
			String strAloneID = null;
			String[] aloneIds = MeasurePubDataBO_Client.getAloneIDsByPubDatas(new MeasurePubDataVO[] { pubData });
			if (aloneIds != null && aloneIds.length > 0) {
				strAloneID = aloneIds[0];
			}
			result[1] = strAloneID;

			// 任务包含的报表
			String[] reportIds = TaskSrvUtils.getReceiveReportId(taskPk);
			result[2] = reportIds;
			// add by congdy 2015.5.23 组织体系版本
			result[3] = rmsVpk;
			// add by congdy 2015.5.23 是否根节点
			boolean isLeaf = TotalUtil.isTotalLeafOrg(rmsVpk, taskPk, orgId);
			result[4] = new Boolean(isLeaf);
		} catch (Exception e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage(), e);
		}
		return result;
    }


	@Override
	public TotalSchemeVO getAppTotalSchemeByDate(String rmsId, String orgId, String innercode,
			String date) throws UFOSrvException {
		try {
			String rmsVpk = RMSUtil.getPKRmsVersionByPkRms(rmsId, date);
			return getAppTotalScheme(rmsId, orgId, innercode, rmsVpk);
		} catch (UFOSrvException e) {
			AppDebug.debug(e);
			throw e;
		}
	}


	@Override
	public boolean isTotalLeafOrg(String pk_rmsversion, String pk_task,
			String pk_org) throws UFOSrvException {
		try {
			BaseDAO dao = new BaseDAO();
			Collection<ReportManaStruMemberVO> set = dao.retrieveByClause(ReportManaStruMemberVersionVO.class, "pk_svid='" + pk_rmsversion + "' and pk_org='" + pk_org + "'  " +
					" and  EXISTS (select 1 from org_reportmanastrumember_v where pk_svid='" + pk_rmsversion + "' and pk_fatherorg='" + pk_org + "' and pk_org in (select pk_receiveorg from IUFO_TASKASSIGN where pk_task = '" + pk_task + "'))");
			if (set == null || set.size() <= 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			AppDebug.debug(ex);
			throw new UFOSrvException("TotalQueryImpl->isTotalLeafOrg:" + ex.getMessage());
		}
	}


	@Override
	public Object[] getCommitAndApproveState(MeasurePubDataVO pubData, String pk_task,
			String[] repPKs) throws UFOSrvException {
		try{
			Object[] obj = new Object[2];
			String strAloneID = MeasurePubDataBO_Client.getAloneID(pubData);
			//取当前报表的报送状态
			RepDataCommitVO[] lstReportCommit = CommitUtil.getReportCommitState(strAloneID, pk_task, repPKs);
			obj[0] = lstReportCommit;
			IApproveQueryService approveQuerySrv=NCLocator.getInstance().lookup(IApproveQueryService.class);
			//取审批状态
			Map<String,Integer> reportToStatesMap = new HashMap<String,Integer>();
			TaskApproveVO[] taskApproveVOs = approveQuerySrv.getAllApproveVOInTask(pk_task, strAloneID, TaskApproveVO.FLOWTYPE_COMMIT);
			if (taskApproveVOs != null && taskApproveVOs.length > 0) {
				for (int i = 0; i < taskApproveVOs.length; i++) {
					Vector<String> repPkVector = ((ApproveReportSet)(taskApproveVOs[i].getPk_report())).getReportPKs();
					for (String reppk : repPkVector) {
						reportToStatesMap.put(reppk, taskApproveVOs[i].getVbillstatus());
					}
				}
			}
			obj[1] = reportToStatesMap;
			return obj;
		}catch(Exception e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		}
	}


	@Override
	public Object[] getTotalSchemeWithOrgAttribute(String totalSchemeId,
			String curr_pk_org, String rmsId,
			String busiTime) throws UFOSrvException {
		Object[] obj = new Object[2];
		TotalSchemeVO tempTotalScheme=TotalSrvUtils.getTotalSchemeByPK(totalSchemeId);
		obj[0] = tempTotalScheme;	
		if(tempTotalScheme.getOrg_content() != null) {
			ArrayList<String> lstReportOrgId =(ArrayList<String>)tempTotalScheme.getOrg_content();
			ReportOrgVO[] reportOrg = TotalOrgAttributeUtils.getReportOrgByOrgPKs(lstReportOrgId.toArray(new String[0]), curr_pk_org, rmsId, busiTime);
			obj[1] = reportOrg;
		}
		return obj;
	}


	@Override
	public Object[] getTotalSchemesByVersionAndRmsVersion(String rmsId,
			String orgId, String clause, String date) throws UFOSrvException {
		TotalSchemeVO[] totalSchemeVOs = getTotalSchemesByVersion(rmsId, orgId, clause, date);
		String pk_rmsVersion = RMSUtil.getPKRmsVersionByPkRms(rmsId, date);
		return new Object[]{totalSchemeVOs, pk_rmsVersion};
	}


	@Override
	public Object[] getTotalSchemesByBusiTimeAndRmsVersion(String rmsId,
			String orgId, String busiTime, String clause)
			throws UFOSrvException {
		TotalSchemeVO[] totalSchemeVOs = getTotalSchemesByBusiTime(rmsId, orgId, busiTime,clause);
		String pk_rmsVersion = RMSUtil.getPKRmsVersionByPkRms(rmsId, busiTime);
		return new Object[]{totalSchemeVOs, pk_rmsVersion};
	}


	@Override
	public ReportOrgInnerVO[] createVersionTreeByOrgAttribute(
			UserdefitemVO[] userdefitems, String strTaskId, String mainOrgPK,
			String rmsPK, String date, boolean includeBalanUnit)
			throws UFOSrvException {
		date = RMSUtil.getTaskKeyDate(strTaskId, date)[1];
		String[] strOrgs = TotalOrgAttributeUtils.getOrgVersionPKS(userdefitems, strTaskId, mainOrgPK, rmsPK,date,includeBalanUnit);
		if(strOrgs==null){
			return null;
		}
		//组织属性+实际组织
		ReportOrgInnerVO[] orgVOs = TotalOrgAttributeUtils.createVersionTreeByOrgAttribute(strOrgs,userdefitems,rmsPK,date);
		return orgVOs;
	}
*/}