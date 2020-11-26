package nccloud.web.ct.saledaily.action;

import nc.itf.ct.sendsale.ISendSaleServer;
import nc.vo.ct.saledaily.entity.AggCtSaleVO;
import nc.vo.ct.saledaily.entity.CtSaleJsonVO;
import nc.vo.ct.saledaily.entity.CtSaleVO;
import nc.vo.ct.saledaily.entity.SaleParamCheckUtils;
import nc.vo.pubapp.pub.power.PowerActionEnum;
import nc.vo.scmpub.res.billtype.CTBillType;
import nccloud.framework.service.ServiceLocator;
import nccloud.framework.web.action.itf.ICommonAction;
import nccloud.framework.web.container.IRequest;
import nccloud.framework.web.ui.pattern.extbillcard.ExtBillCardOperator;
import nccloud.web.scmpub.pub.action.DataPermissionAction;
import nccloud.web.scmpub.pub.utils.SCMEditCheckUtils;
import nccloud.framework.core.exception.ExceptionUtils;


import nccloud.framework.core.json.IJson;
import nccloud.framework.web.json.JsonFactory;


public class SaleDailyCardEditAction extends DataPermissionAction implements ICommonAction {
	public Object doAction(IRequest request) {
		ExtBillCardOperator operator = new ExtBillCardOperator("400600200_card");
//		System.out.println("info");
		AggCtSaleVO vo = (AggCtSaleVO) operator.toBill(request);
//		try {
//			//得到token对象
//			String appUser="KGJN";
//			String secretKey="OXpXfaLG5v0LZedTEi2F2WcnGQmPoi5n0m+srzE1kmE=";
//			SaleUrlConst url = SaleUrlConst.getUrlConst();
//			TokenInfo tInfo =   SaleSendRestUtil.restLogin( appUser, secretKey,url.getRestLogin());
//			
//		
//			ISendSaleServer service = (ISendSaleServer) ServiceLocator.find(ISendSaleServer.class);
//			CtSaleJsonVO jsonVO = service.pushSaleToService(vo);
//			
//			SaleParamCheckUtils.doValidator(jsonVO);
//			IJson json = JsonFactory.create();
//			String jsonStr =  json.toJson(jsonVO);
//			String rtn = SaleSendRestUtil.registerContractInfo(appUser, tInfo.getToken(), jsonStr, url.getRegisterContractInfo());
//			System.out.println(rtn);
// 
//			
//		}catch(Exception ex){
//			ExceptionUtils.wrapException(ex);
//		}
//		
		
		
		
		
		
		CtSaleVO hvo = vo.getParentVO();
		String pk = hvo.getPk_ct_sale();
		String vtrantypecode = hvo.getVtrantypecode();
		Integer fstatusflag = hvo.getFstatusflag();
		if (7 == fstatusflag.intValue()) {
			SCMEditCheckUtils.checkEdit(pk, vtrantypecode);
		}
		checkPermission(new AggCtSaleVO[]{vo});
		return null;
	}

	public String getPermissioncode() {
		return CTBillType.SaleDaily.getCode();
	}

	public String getActioncode() {
		return PowerActionEnum.EDIT.getActioncode();
	}

	public String getBillCodeField() {
		return "vbillcode";
	}
}