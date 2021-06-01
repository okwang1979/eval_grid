package nccloud.web.ct.saledaily.action;

import java.util.HashMap;
import java.util.Map;

import nc.bs.logging.Logger;
import nc.itf.ct.saledaily.ISaledailyMaintain;
import nc.itf.ct.sendsale.ISendSaleServer;
import nc.vo.ct.purdaily.entity.AggCtPuVO;
import nc.vo.ct.saledaily.entity.AggCtSaleVO;
import nc.vo.ct.saledaily.entity.CtSaleJsonVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.ISuperVO;
import nc.vo.pub.IVOMeta;
import nc.vo.pub.filesystem.NCFileVO;
import nc.vo.pubapp.pattern.model.meta.entity.bill.IBillMeta;
import nc.vo.pubapp.pattern.tool.performance.DeepCloneTool;
import nc.vo.scmpub.res.billtype.CTBillType;
import nccloud.dto.ct.saledaily.entity.SaleDailyReasonInfo;
import nccloud.dto.scmpub.pflow.SCMCloudPFlowContext;
import nccloud.dto.scmpub.script.entity.SCMScriptResultDTO;
import nccloud.dto.so.pub.entity.BatchOprInfo;
import nccloud.dto.so.pub.entity.SimpleQueryInfo;
import nccloud.framework.core.exception.ExceptionUtils;
import nccloud.framework.core.json.IJson;
import nccloud.framework.service.ServiceLocator;
import nccloud.framework.web.container.IRequest;
import nccloud.framework.web.container.RequestSysJsonVO;
import nccloud.framework.web.json.JsonFactory;
import nccloud.framework.web.ui.pattern.extbillcard.ExtBillCard;
import nccloud.pubitf.platform.attachment.IAttachmentService;
import nccloud.pubitf.scmpub.commit.service.IBatchRunScriptService;
import nccloud.web.ct.pub.action.AbstractGridAction;
import nccloud.web.ct.saledaily.utils.SaleDailyCompareUtil;
import nccloud.web.scmpub.pub.operator.SCMExtBillCardOperator;

public abstract class SaleDailyCardCommonAction extends AbstractGridAction<AggCtSaleVO> {
	protected String reason = null;

	protected abstract String getPFActionName();

	public Object doAction(IRequest request) {
		
		String read = request.read();
		IJson json = JsonFactory.create();
		SaleDailyReasonInfo info = (SaleDailyReasonInfo) json.fromJson(read, SaleDailyReasonInfo.class);
		RequestSysJsonVO readSysParam = request.readSysParam();
		String busiaction = readSysParam.getBusiaction();
		
		
	
		
		String appcode = readSysParam.getAppcode();
		if("400600200".equals(appcode) || "400400604".equals(appcode)) {
		
			BatchOprInfo infos = (BatchOprInfo)json.fromJson(read, BatchOprInfo.class);
		    SimpleQueryInfo[] qryinfo = infos.getQryinfo();
		    String pk_ct = qryinfo[0].getPk();
		    String[] ids = {pk_ct};
		    AggCtSaleVO[] sales =  this.queryVos(ids);
		    if(readSysParam!=null&&readSysParam.getBusiaction().contains("Ìá½»")) {
		    	
		    	
		    	ISendSaleServer sendService = ServiceLocator.find(ISendSaleServer.class);
				String rtnInfo = sendService.checkSaleAdjs_ZW(sales);
				if(rtnInfo!=null&&rtnInfo.length()>1) {
					throw new BusinessRuntimeException(rtnInfo);
				} 
				 
			}


		}
		this.reason = info.getReason();
		return super.doAction(request);
	}

	protected AggCtSaleVO[] queryVos(String[] ids) {
		ISaledailyMaintain service = (ISaledailyMaintain) ServiceLocator.find(ISaledailyMaintain.class);

		try {
			AggCtSaleVO[] vos = service.queryCtApVoByIds(ids);
			return vos;
		} catch (BusinessException var4) {
			ExceptionUtils.wrapException(var4);
			return null;
		}
	}

	protected Object action(AggCtSaleVO[] vos) {
		DeepCloneTool tool = new DeepCloneTool();
		AggCtSaleVO origvo = (AggCtSaleVO) tool.deepClone(vos[0]);
		this.beforeGetVos(vos);
		if (this.getActioncode() != null) {
			this.checkPermission(vos);
		}

		SCMExtBillCardOperator operator = SaleDailyCompareUtil.getBillCardOperator();
		this.appendPseudoColumn(vos);
		SCMCloudPFlowContext context = new SCMCloudPFlowContext();
		context.setBillVos(vos);
		context.setTrantype(vos[0].getParentVO().getVtrantypecode());
		context.setBillType(CTBillType.SaleDaily.getCode());
		context.setActionName(this.getPFActionName());
		this.proContext(context);
		SCMScriptResultDTO dto = ((IBatchRunScriptService) ServiceLocator.find(IBatchRunScriptService.class))
				.runBacth(context, AggCtSaleVO.class, new String[]{"SAVE", "APPROVE", "UNSAVEBILL", "UNAPPROVE",
						"TERMINATE", "UNTERMINATE", "FREEZE", "UNFREEZE", "UNVALIDATE", "VALIDATE"});
		Object obj = dto.getData();
		if (obj != null && obj instanceof Map) {
			return dto.getData();
		} else {
			ExtBillCard billcard = SaleDailyCompareUtil.operator(operator, dto.getSucessVOs()[0], origvo);
			return billcard;
		}
	}

	protected void proContext(SCMCloudPFlowContext context) {
	}

	public String getPermissioncode() {
		return CTBillType.SaleDaily.getCode();
	}

	public String getActioncode() {
		return null;
	}

	public String getBillCodeField() {
		return "vbillcode";
	}

	protected void beforeGetVos(AggCtSaleVO[] vos) {
	}

	private void appendPseudoColumn(AggCtSaleVO[] bills) {
		if (bills != null && bills.length > 0) {
			AggCtSaleVO[] var2 = bills;
			int var3 = bills.length;

			for (int var4 = 0; var4 < var3; ++var4) {
				AggCtSaleVO bill = var2[var4];
				IBillMeta billMeta = bill.getMetaData();
				IVOMeta[] childMetas = billMeta.getChildren();
				IVOMeta[] var8 = childMetas;
				int var9 = childMetas.length;

				for (int var10 = 0; var10 < var9; ++var10) {
					IVOMeta childMeta = var8[var10];
					ISuperVO[] clientVOs = bill.getChildren(childMeta);

					for (int i = 0; i < clientVOs.length; ++i) {
						clientVOs[i].setAttributeValue("pseudocolumn", i);
					}
				}
			}
		}

	}
}