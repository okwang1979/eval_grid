package nc.impl.hbbb.totalcheme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.logging.Logger;
import nc.itf.hbbb.total.IHbTotalSchemeServer;
import nc.vo.hbbb.total.HbTotalSchemeVO;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessRuntimeException;

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

}
