package nccloud.web.ct.saledaily.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nc.bs.logging.Logger;
import nc.itf.ct.sendsale.ISendSaleServer;
import nc.vo.ct.enumeration.CtFlowEnum;
import nc.vo.ct.saledaily.entity.AggCtSaleVO;
import nc.vo.ct.saledaily.entity.CtSaleJsonVO;
import nc.vo.ct.saledaily.entity.PaymentPlanAndFeedbackInfo;
import nc.vo.ct.saledaily.entity.SaleParamCheckUtils;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessRuntimeException;
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
		ISendSaleServer service = (ISendSaleServer) ServiceLocator.find(ISendSaleServer.class);
		 AggCtSaleVO[] vos = queryVos(pks.toArray(new String[0]));
		 if(vos==null||vos.length==0) {
				return super.doAction(request);
		 }
		 Set<String> salePks = new HashSet<>();
		if(service.isUseSend(vos[0].getParentVO()).booleanValue()) {
//			 AggCtSaleVO[] vos = queryVos(pks.toArray(new String[0]));
				
				try {
					Logger.init("iufo");
						String appUser="KGJN";
						String secretKey="OXpXfaLG5v0LZedTEi2F2WcnGQmPoi5n0m+srzE1kmE=";
						
						SaleUrlConst url = SaleUrlConst.getUrlConst();
						Logger.error("��ȡToken:"+url.getRestLogin());;
						TokenInfo tInfo =   SaleSendRestUtil.restLogin( appUser, secretKey,url.getRestLogin());
						
						
					     if(!"200".equals(tInfo.getCode())) {
					    	Logger.error("��ȡtoken����:"+tInfo.getMessage());
					        ExceptionUtils.wrapBusinessException(tInfo.getMessage());
					     }
					     Logger.error("��ȡToken�ɹ���");
			 	
					for(AggCtSaleVO vo :vos) {
						  Logger.error("����ϴ��ļ���");
						String checkMessage  = service.getNCFileInfo(vo.getParentVO());
						
						if(checkMessage!=null&&checkMessage.length()>0) {
							 Logger.error("����ϴ��ļ��쳣��"+checkMessage);
							ExceptionUtils.wrapBusinessException(checkMessage);
						}
						
						CtSaleJsonVO jsonVO = service.pushSaleToService(vo);
						
//						SaleParamCheckUtils.doValidator(jsonVO);
						IJson json = JsonFactory.create();
						String jsonStr =  json.toJson(jsonVO);
						
						 Logger.init("iufo");
						 Logger.error("���ۺ�ͬjson��"+jsonStr);
						String rtn = SaleSendRestUtil.registerContractInfo(appUser, tInfo.getToken(), jsonStr, url.getRegisterContractInfo());
						
						TokenInfo info =  (TokenInfo)json.fromJson(rtn, TokenInfo.class);
					     if(!"200".equals(info.getCode())) {
					    	 Logger.error("���ۺ�ͬ�������"+info.getMessage());
					      ExceptionUtils.wrapBusinessException(info.getMessage());
					     }
					     Logger.error("���ۺ�ͬ����ɹ�:"+vo.getParentVO().getVbillcode() );
//					     service.updateSale(vo.getParentVO().getPk_ct_sale());
					     
					     salePks.add(vo.getParentVO().getPk_ct_sale());
					     
//					   //�տЭ��ƻ���Ϣ����
//						PaymentPlanAndFeedbackInfo planInfo = service.pushBillToService(vos[0]);
//						if(planInfo==null)  ExceptionUtils.wrapException(new BusinessRuntimeException("�տЭ��ƻ���Ϣ,ת��ʧ��!"));
//							
//						SaleParamCheckUtils.doValidator(planInfo);
//						IJson json1 = JsonFactory.create();
//						String jsonStrPlan =  json1.toJson(planInfo);
//						String resultStr = SaleSendRestUtil.receiptBillInfo(appUser, tInfo.getToken(), jsonStrPlan, url.getReceiptBillInfo());
//						TokenInfo info1 =  (TokenInfo)json1.fromJson(resultStr, TokenInfo.class);
//						if(!"200".equals(info1.getCode())) {
//							ExceptionUtils.wrapBusinessException("�տ�ƻ���" + info1.getMessage());
//						}
//						System.out.println("lalalala");
						
//						Logger.error(resultStr);
						
					}
						
		
			 	
					 
			 
					
					
				}catch(Exception ex){
					Logger.init();
					ExceptionUtils.wrapException(ex);
				}
		}
		
		 Object rtn = super.doAction(request);
		 if(salePks.size()>0) {
			 for(String salePk:salePks) {
				 service.updateSale(salePk);
			 }
			 
		 }
		
		 
		return rtn;
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