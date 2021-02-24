package nccloud.web.ct.purdaily.action;

import java.util.Map;

import nc.vo.ct.purdaily.entity.AggCtPuVO;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.SuperVO;
import nc.vo.pubapp.pattern.model.entity.bill.AbstractBill;
import nc.vo.scmpub.res.billtype.CTBillType;
import nccloud.dto.scmpub.script.entity.SCMScriptResultDTO;
import nccloud.framework.service.ServiceLocator;
import nccloud.pubitf.riart.pflow.CloudPFlowContext;
import nccloud.pubitf.scmpub.commit.service.IBatchRunScriptService;
import nccloud.web.ct.pub.action.BaseScriptAction;
import nccloud.web.ct.purdaily.utils.ResultUtil;
import nccloud.web.ct.purdaily.utils.ScriptActionUtil;
import nccloud.web.ct.saledaily.action.SaleSendAdapter;

public class ResendPu extends BaseScriptAction{
	public Map<String, Object> processSuccessResult(AbstractBill[] bills, AbstractBill[] orginalBills) {
		return ResultUtil.processScriptResult(bills, orginalBills);
	}

	public void beforeProcess(Object[] objs, Map<String, Object> userObj) {
		AbstractBill[] vos = (AbstractBill[]) objs;
	
		try {
			SaleSendAdapter adapter = new SaleSendAdapter();
			 
			adapter.doAction(vos);
			
//			
//			for(AbstractBill bill:vos) {
//				if(bill instanceof AggCtPuVO) {
//					AggCtPuVO aggVo = (AggCtPuVO) bill;
//		
//				}
//			}
		
				
				 
			 
		}catch(Exception ex) {
			throw new BusinessRuntimeException(ex.getMessage(),ex);
		}
	}

	public SCMScriptResultDTO execScript(AbstractBill[] bills) {
		CloudPFlowContext context = new CloudPFlowContext();
		context.setActionName("VALIDATE");
		context.setBillType("Z2");
		context.setBillVos(bills);

 		SCMScriptResultDTO result = new SCMScriptResultDTO();
 		
 		result.setSucessNum(1);
 		result.setData(bills[0]);
 		result.setSucessVOs(bills);
//		ScriptActionUtil.resetExecVOStatus(bills, 0);
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
