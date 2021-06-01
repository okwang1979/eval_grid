package nccloud.web.ct.purdaily.action;

import java.util.Map;

import nc.itf.ct.sendsale.ISendSaleServer;
import nc.vo.ct.enumeration.CtFlowEnum;
import nc.vo.ct.purdaily.entity.AggCtPuVO;
import nc.vo.ct.saledaily.entity.CtSaleJsonVO;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.SuperVO;
import nc.vo.pubapp.pattern.model.entity.bill.AbstractBill;
import nc.vo.scmpub.res.billtype.CTBillType;
import nc.vo.scmpub.util.ArrayUtil;
import nccloud.dto.scmpub.script.entity.SCMScriptResultDTO;
import nccloud.framework.service.ServiceLocator;
import nccloud.pubitf.riart.pflow.CloudPFlowContext;
import nccloud.pubitf.scmpub.commit.service.IBatchRunScriptService;
import nccloud.web.ct.pub.action.BaseScriptAction;
import nccloud.web.ct.purdaily.utils.ResultUtil;
import nccloud.web.ct.purdaily.utils.ScriptActionUtil;
import nccloud.web.ct.saledaily.action.SaleSendAdapter;

public class ValidateAction extends BaseScriptAction {
	public Map<String, Object> processSuccessResult(AbstractBill[] bills, AbstractBill[] orginalBills) {
		return ResultUtil.processScriptResult(bills, orginalBills);
	}

	public void beforeProcess(Object[] objs, Map<String, Object> userObj) {
		AbstractBill[] vos = (AbstractBill[]) objs;
		String reason = (String) userObj.get("reason");
		ScriptActionUtil.addNewExecVO(vos, Integer.valueOf(CtFlowEnum.APPROVE.toIntValue()), reason,
				ScriptActionUtil.getVALIDATE());
		for (AbstractBill vo : vos) {
			AggCtPuVO aggVo = (AggCtPuVO) vo;
			SuperVO[][] allChildren = aggVo.getAllChildren();
			for (SuperVO[] superVOs : allChildren) {
				if (!ArrayUtil.isEmpty(superVOs)) {
					setFakeRowNO(superVOs);
				}
			}
		}

	}
	
	

	public SCMScriptResultDTO execScript(AbstractBill[] bills) {
		CloudPFlowContext context = new CloudPFlowContext();
		context.setActionName("VALIDATE");
		context.setBillType("Z2");
		context.setBillVos(bills);
		SCMScriptResultDTO result = null;

		try {
			SaleSendAdapter adapter = new SaleSendAdapter();
			 
			adapter.doAction(bills);
				
			result = ((IBatchRunScriptService) ServiceLocator.find(IBatchRunScriptService.class))
					.runBacth(context, AggCtPuVO.class);

			ScriptActionUtil.resetExecVOStatus(result.getSucessVOs(), 0); 
			
			ISendSaleServer service = (ISendSaleServer) ServiceLocator.find(ISendSaleServer.class);
			
			for(AbstractBill bill:bills) {
			if(bill instanceof AggCtPuVO) {
				AggCtPuVO aggVo = (AggCtPuVO) bill;
				
			
//				CtSaleJsonVO jsonVO = service.pushPurdailyToService(aggVo);
			
				service.updatePu(aggVo.getParentVO().getPk_ct_pu());
			}
			}
		}catch(Exception ex) {
			throw new BusinessRuntimeException(ex.getMessage(),ex);
		}
		return result;
	}

	public Class getClazz() {
		return AggCtPuVO.class;
	}

	public String getPermissioncode() {
		return CTBillType.PurDaily.getCode();
	}

	protected Boolean isHandleResumeException() {
		return Boolean.TRUE;
	}

	protected void setFakeRowNO(SuperVO[] vos) {
		for (int i = 0; i < vos.length; i++) {
			vos[i].setAttributeValue("pseudocolumn", Integer.valueOf(i));
		}
	}
}