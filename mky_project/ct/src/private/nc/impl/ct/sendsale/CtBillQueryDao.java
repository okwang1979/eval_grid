package nc.impl.ct.sendsale;

import java.util.Collection;
import java.util.List;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.jdbc.framework.SQLParameter;
import nc.vo.ct.purdaily.entity.CtPaymentVO;
import nc.vo.ct.purdaily.entity.PayPlanVO;
import nc.vo.ct.saledaily.entity.CtSalePayTermVO;

public class CtBillQueryDao {
	/***
	 * 根据销售合同主键查询收款协议List
	 * @param pk_ct_sale
	 * @return
	 */
	public List<CtSalePayTermVO> queryCtSalePayterms(String pk_ct_sale)
	  {
		 BaseDAO dao = new BaseDAO();
		 SQLParameter params = new SQLParameter();
		 params.addParam(pk_ct_sale);
		 try {
			Collection<CtSalePayTermVO> rtns =  dao.retrieveByClause(CtSalePayTermVO.class, "pk_ct_sale = ?",params);
			return (List<CtSalePayTermVO>) rtns;
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	  }
	/***
	 * 根据采购合同主键查询付款计划List
	 * @param pk_ct_pu
	 * @return
	 */
	public List<PayPlanVO> queryCtPurPayplans(String pk_ct_pu)
	{
		BaseDAO dao = new BaseDAO();
		SQLParameter params = new SQLParameter();
		params.addParam(pk_ct_pu);
		try {
			Collection<PayPlanVO> rtns =  dao.retrieveByClause(PayPlanVO.class, "pk_ct_pu = ? and nrate != 100",params);
			return (List<PayPlanVO>) rtns;
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/***
	 * 根据采购合同主键查询付款计划List
	 * @param pk_ct_pu
	 * @return
	 */
	public List<CtPaymentVO> queryCtPurPayments(String pk_ct_pu)
	{
		BaseDAO dao = new BaseDAO();
		SQLParameter params = new SQLParameter();
		params.addParam(pk_ct_pu);
		try {
			Collection<CtPaymentVO> rtns =  dao.retrieveByClause(CtPaymentVO.class, "pk_ct_pu = ?",params);
			return (List<CtPaymentVO>) rtns;
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
