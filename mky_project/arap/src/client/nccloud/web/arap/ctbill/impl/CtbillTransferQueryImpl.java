package nccloud.web.arap.ctbill.impl;

import java.util.ArrayList;
import java.util.List;

import nc.impl.pubapp.pattern.data.bill.BillLazyQuery;
import nc.itf.arap.fieldmap.IBillFieldGet;
import nc.itf.ct.saledaily.ISaledailyMaintain;
import nc.ui.querytemplate.querytree.IQueryScheme;
import nc.vo.ct.saledaily.entity.AggCtSaleVO;
import nc.vo.pub.BusinessException;
import nccloud.framework.service.ServiceLocator;
import nccloud.framework.web.ui.pattern.billcard.SpecilAggBill;
import nccloud.web.riart.billref.src.INccSrcBillReferQuery;

public class CtbillTransferQueryImpl implements INccSrcBillReferQuery  {

	@Override
	public Object[] querySrcBill(IQueryScheme queryScheme, String srcbillOrTransType, String destbillOrTransType, String pkBusitype) throws BusinessException {

		queryScheme.put(IBillFieldGet.PK_BUSITYPE, pkBusitype);
//		// 执行转单查询
		BillLazyQuery<AggCtSaleVO> query = new BillLazyQuery<AggCtSaleVO>(
				AggCtSaleVO.class);
		
		
		AggCtSaleVO [] vos  = query.query(queryScheme, null);
		List<SpecilAggBill> newvos = new ArrayList<>();
		
		List<String> pid = new ArrayList<>();
		for(AggCtSaleVO vo : vos) {
			String pk_ct_sale = vo.getParentVO().getPk_ct_sale();
			pid.add(pk_ct_sale);
		}
		
		ISaledailyMaintain service = (ISaledailyMaintain) ServiceLocator.find(ISaledailyMaintain.class);
		
		vos = service.queryCtApVoByIds(pid.toArray(new String[pid.size()]));
		
		for(AggCtSaleVO vo : vos) {
			SpecilAggBill newvo = new SpecilAggBill();
			newvo.setHead(vo.getParentVO());
			newvo.setBodys(vo.getCtSaleBVO());
			newvos.add(newvo);
		}
		
		return newvos.toArray(new SpecilAggBill[vos.length] );
	}

}
