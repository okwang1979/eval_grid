package nccloud.web.ct.purdaily.action;

import java.util.Map;

import nc.itf.ct.sendsale.ISendSaleServer;
import nc.vo.ct.purdaily.entity.AggCtPuVO;
import nc.vo.ct.purdaily.entity.CtPuVO;
import nc.vo.pub.BusinessException;
import nc.vo.pubapp.pattern.model.entity.bill.AbstractBill;
import nc.vo.pubapp.pub.power.PowerActionEnum;
import nc.vo.scmpub.res.billtype.CTBillType;
import nccloud.dto.scmpub.script.entity.SCMScriptResultDTO;
import nccloud.framework.core.exception.ExceptionUtils;
import nccloud.framework.service.ServiceLocator;
import nccloud.pubitf.ct.purdaily.service.IPurdailyService;
import nccloud.web.ct.pub.action.BaseScriptAction;
import nccloud.web.ct.purdaily.utils.ResultUtil;

/**
 * @description 提交
 * @author xiahui
 * @date 创建时间：2019-1-18 上午8:45:58
 * @version ncc1.0
 **/
public class CommitAction extends BaseScriptAction {

	@Override
	public Map<String, Object> processSuccessResult(AbstractBill[] bills, AbstractBill[] orginalBills) {
		return ResultUtil.processScriptResult(bills, orginalBills);
	}

	@Override
	public SCMScriptResultDTO execScript(AbstractBill[] bills) {
		try {
//			//央客王志强添加附件验证
			String info = "请录入正文";
			
			
			ISendSaleServer sendService = ServiceLocator.find(ISendSaleServer.class);
			info = sendService.checkPuAdjs_ZW((AggCtPuVO[]) bills);
			if(info!=null&&info.length()>1) {
				throw new BusinessException(info);
			} 
//			//end
			
			return ServiceLocator.find(IPurdailyService.class).commit((AggCtPuVO[]) bills);
		} catch (BusinessException e) {
			ExceptionUtils.wrapException(e);
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getClazz() {
		return AggCtPuVO.class;
	}

	@Override
	public String getPermissioncode() {
		return CTBillType.PurDaily.getCode();
	}

	@Override
	public String getActioncode() {
		return PowerActionEnum.COMMIT.getActioncode();
	}

	@Override
	public String getBillCodeField() {
		return CtPuVO.VBILLCODE;
	}

	@Override
	protected Boolean isHandleResumeException() {
		return Boolean.TRUE;
	}

}
