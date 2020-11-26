package nccloud.web.ct.saledaily.action;

import java.util.ArrayList;
import java.util.List;

import nc.bs.logging.Logger;
import nc.itf.ct.sendsale.ISendSaleServer;
import nc.vo.ct.enumeration.CtFlowEnum;
import nc.vo.ct.saledaily.entity.AggCtSaleVO;
import nc.vo.ct.saledaily.entity.CtSaleJsonVO;
import nc.vo.ct.saledaily.entity.PaymentPlanAndFeedbackInfo;
import nc.vo.ct.saledaily.entity.SaleParamCheckUtils;
import nc.vo.ml.NCLangRes4VoTransl;
import nccloud.dto.so.pub.entity.SimpleQueryInfo;
import nccloud.framework.core.exception.ExceptionUtils;
import nccloud.framework.core.json.IJson;
import nccloud.framework.service.ServiceLocator;
import nccloud.framework.web.container.IRequest;
import nccloud.framework.web.json.JsonFactory;
import nccloud.web.ct.saledaily.utils.SaleDailyUtil;

public class SaleDailyCardValidateAction extends SaleDailyCardCommonAction {
	
	
	
	
	
	@Override
	public Object doAction(IRequest request) {
		
		
		SimpleQueryInfo[] infos = buildParams(request);

		List<String> pks = new ArrayList<String>();
		for(SimpleQueryInfo info:infos) {
			pks.add(info.getPk());
		}
		 AggCtSaleVO[] vos = queryVos(pks.toArray(new String[0]));
		
			try {
				if(vos!=null&&vos.length>0) {
					String appUser="KGJN";
					String secretKey="OXpXfaLG5v0LZedTEi2F2WcnGQmPoi5n0m+srzE1kmE=";
					SaleUrlConst url = SaleUrlConst.getUrlConst();
					TokenInfo tInfo =   SaleSendRestUtil.restLogin( appUser, secretKey,url.getRestLogin());
					
					
				     if(!"200".equals(tInfo.getCode())) {
				      ExceptionUtils.wrapBusinessException(tInfo.getMessage());
				     }
		 	
				
					ISendSaleServer service = (ISendSaleServer) ServiceLocator.find(ISendSaleServer.class);
					CtSaleJsonVO jsonVO = service.pushSaleToService(vos[0]);
					
					SaleParamCheckUtils.doValidator(jsonVO);
					IJson json = JsonFactory.create();
					String jsonStr =  json.toJson(jsonVO);
					
					 Logger.init("iufo");
					 Logger.error(jsonStr);
					String rtn = SaleSendRestUtil.registerContractInfo(appUser, tInfo.getToken(), jsonStr, url.getRegisterContractInfo());
					
					TokenInfo info =  (TokenInfo)json.fromJson(rtn, TokenInfo.class);
				     if(!"200".equals(info.getCode())) {
				      ExceptionUtils.wrapBusinessException(info.getMessage());
				     }
				     
				   //收款单协议计划信息推送
					PaymentPlanAndFeedbackInfo planInfo = service.pushBillToService(vos[0]);
					if(planInfo==null) 	return super.doAction(request);
					SaleParamCheckUtils.doValidator(planInfo);
					IJson json1 = JsonFactory.create();
					String jsonStrPlan =  json1.toJson(planInfo);
					String resultStr = SaleSendRestUtil.receiptBillInfo(appUser, tInfo.getToken(), jsonStrPlan, url.getReceiptBillInfo());
					TokenInfo info1 =  (TokenInfo)json1.fromJson(resultStr, TokenInfo.class);
					if(!"200".equals(info1.getCode())) {
						ExceptionUtils.wrapBusinessException("收款计划：" + info1.getMessage());
					}
					Logger.init("iufo");
					Logger.error(resultStr);
		 	
				}
		 
				
				
			}catch(Exception ex){
				Logger.init();
				ExceptionUtils.wrapException(ex);
			}
		 
		return super.doAction(request);
	}

	public String getPFActionName() {
		return "VALIDATE";
	}

	public String getActioncode() {
		return "validate";
	}

	protected void beforeGetVos(AggCtSaleVO[] vos) {
		SaleDailyUtil.addNewExecVO(vos, (Integer) CtFlowEnum.APPROVE.value(), reason,
				NCLangRes4VoTransl.getNCLangRes().getStrByID("4004132_0", "04004132-0012"));
	}
}