package nccloud.web.ct.saledaily.action;

import java.util.HashMap;
import java.util.Map;
import nc.itf.ct.saledaily.ISaledailyMaintain;
import nc.vo.ct.saledaily.entity.AggCtSaleVO;
import nc.vo.pub.BusinessException;
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
		    NCFileVO[] ncfiles = ServiceLocator.find(IAttachmentService.class).queryNCFileByBill(pk_ct);
//	   	     dao.queryFileVOsByPath(pk_ct);
	   	    Map<String, String> map = new HashMap<String, String>();
	   	    Map<String, String> map1 = new HashMap<String, String>();
	   	    Map<String, String> map2 = new HashMap<String, String>();
	   	    for (int i = 0; i < ncfiles.length; i++) {
				NCFileVO ncFileVO = ncfiles[i];
				String name = ncFileVO.getName();
				String fullPath = ncFileVO.getFullPath();
				if(busiaction.contains("销售合同维护-确定")) {
					if(fullPath.contains("合同审批单")) {
						map.put(name, "0");
					}
					else {
						map.put(name, "1");
					}
					if(!fullPath.contains("合同签署文本")) {
					    map1.put(name, "0");
					}
					else {
						map1.put(name, "1");
					}
				}
				if("销售合同维护-提交".equals(busiaction)) {
					if(!fullPath.contains("合同正文")) {
						map2.put(name, "0");
					}
					else {
						map2.put(name, "1");
					}
				}
	   	    }
	   	     String i = "";
		   	 for (String s : map.values()) {
		   		 i = i + s;
		   	 }
//		   	 if(!"".equals(i) && !i.contains("0")) {
//		   		ExceptionUtils.wrapBusinessException("合同审批单附件未上传!");
//		   	 }
//		   	 String i1 = "";
//		   	 for (String s : map1.values()) {
//		   		 i1 = i1 + s;
//		   	 }
//		   	 if(!"".equals(i1) && !i1.contains("0")) {
//		   		ExceptionUtils.wrapBusinessException("合同签署文本（签字盖章扫描件）附件未上传!");
//		   	 }
//		   	 String i2 = "";
//		   	 for (String s : map2.values()) {
//		   		 i2 = i2 + s;
//		   	 }
//		   	 if(!"".equals(i2) && !i2.contains("0")) {
//		   		ExceptionUtils.wrapBusinessException("合同正文附件未上传!");
//		   	 }
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