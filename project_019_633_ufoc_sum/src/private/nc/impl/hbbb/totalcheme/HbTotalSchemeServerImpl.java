package nc.impl.hbbb.totalcheme;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.iuforeport.repdatainput.RepDataOperResultVO;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.impl.iufo.total.hb.HBTotalManageImpl;
import nc.itf.hbbb.hbrepstru.IHBRepstruQrySrv;
import nc.itf.hbbb.total.IHbTotalSchemeServer;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.pub.iufo.exception.UFOSrvException;
import nc.pubitf.eaa.InnerCodeUtil;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.total.HbTotalSchemeVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.vorg.ReportCombineStruMemberVersionVO;

public class HbTotalSchemeServerImpl implements IHbTotalSchemeServer{

	@Override
	public String saveOrUpdateScheme(HbTotalSchemeVO vo) {
		 BaseDAO dao =  new BaseDAO();
		 if(vo.getPk_hbscheme()==null){
			 try {
					return dao.insertVO(vo);
				} catch (DAOException e) {
					 throw new BusinessRuntimeException(e.getMessage()+":新增方案错误",e);
				}
		 }
		 try {
				  dao.updateVO(vo);
				  return vo.getPk_hbscheme();
			} catch (DAOException e) {
				 throw new BusinessRuntimeException(e.getMessage()+":修改方案错误",e);
			}
	
		 
	}

	@Override
	public boolean deleleSchemeByPk(String pk_scheme) {
		 BaseDAO dao =  new BaseDAO();
		 try {
			dao.deleteByPK(HbTotalSchemeVO.class, pk_scheme);
			return true;
		} catch (DAOException e) {
			 throw new BusinessRuntimeException(e.getMessage()+":删除HbTotalSchemeVO错误，pk is :"+pk_scheme,e);
		}
		 
	}

	@Override
	public List<HbTotalSchemeVO> queryBy(String pk_rms, String pk_rmsversion) {
		List<HbTotalSchemeVO> rtn= new ArrayList<HbTotalSchemeVO>();
		 BaseDAO dao =  new BaseDAO();
		 try {
			Collection<HbTotalSchemeVO>   querys = dao.retrieveByClause(HbTotalSchemeVO.class, "pk_rms ='"+pk_rms+"'  and pk_rmsversion = '"+pk_rmsversion+"'");
			if(querys!=null&&querys.size()>0){
				rtn.addAll(querys);
			}
		} catch (Exception e) {
			Logger.error(e);
			throw new BusinessRuntimeException(e.getMessage(),e);
		 
		}
		return rtn;
	}
	
	
//	public BaseDAO getBaseDAO(){
//		 
//			return new BaseDAO();
//		 
//	}
	
	@Override
	public HbTotalSchemeVO getTotalSchemeByPK(String totalSchemeId)
			throws UFOSrvException {
		try {
			BaseDAO baseDao =  new BaseDAO();
			 
			String cond = "pk_hbscheme =? ";
			SQLParameter param = new SQLParameter();
			param.addParam(totalSchemeId);
			List<HbTotalSchemeVO> list =  (List<HbTotalSchemeVO>)baseDao.retrieveByClause(HbTotalSchemeVO.class, cond, param);

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
			BaseDAO baseDao =  new BaseDAO();
			String cond = " pk_org = ? and pk_rmsversion = ?";
			SQLParameter param = new SQLParameter();
//			param.addParam(rmsId);
			param.addParam(orgId);
			param.addParam(strRmsVersionPK);
			

			List<HbTotalSchemeVO> list =(List<HbTotalSchemeVO>) baseDao.retrieveByClause(HbTotalSchemeVO.class, cond, param);
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

	@Override
	public RepDataOperResultVO createTotalResults(MeasurePubDataVO mainPubData,
			String busiTime, HbTotalSchemeVO totalScheme, String[] reportIds,
			boolean[] extendParams, HBSchemeVO hbschemeVO, String oper_user,
			String mainOrgPK) throws UFOSrvException {
		return new  HBTotalManageImpl().createTotalResults(mainPubData, busiTime, totalScheme, reportIds, extendParams, hbschemeVO, oper_user, mainOrgPK);
	}

	@Override
	public String getTotalOrgCondSQL(HbTotalSchemeVO totalScheme,
			String rcsVerPk, String pk_hbscheme) throws UFOSrvException {
		return new HBTotalManageImpl().getTotalOrgCondSQL(totalScheme, rcsVerPk, pk_hbscheme);
	}


}
