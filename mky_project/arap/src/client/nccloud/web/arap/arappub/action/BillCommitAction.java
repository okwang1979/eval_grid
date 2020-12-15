package nccloud.web.arap.arappub.action;

import java.util.HashMap;
import java.util.Map;

import nc.bs.arap.bill.ArapBillPubUtil;
import nc.itf.arap.fieldmap.IBillFieldGet;
import nc.itf.arap.pub.IArapBillService;
import nc.itf.uap.pf.IWorkflowMachine;
import nc.vo.arap.basebill.BaseAggVO;
import nc.vo.arap.basebill.BaseBillVO;
import nc.vo.arap.exception.ArapTbbException;
import nc.vo.arap.pub.BillActionConstant;
import nc.vo.arap.pub.BillEnumCollection.ApproveStatus;
import nc.vo.arap.receivable.AggReceivableBillVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.pf.workflow.IPFActionName;
import nc.vo.pub.workflownote.WorkflownoteVO;
import nc.vo.pubapp.pflow.PfUserObject;
import nc.vo.uap.pf.PfProcessBatchRetObject;
import nc.vo.wfengine.definition.WorkflowTypeEnum;
import nccloud.framework.core.exception.ExceptionUtils;
import nccloud.framework.core.json.IJson;
import nccloud.framework.service.ServiceLocator;
import nccloud.framework.web.action.itf.ICommonAction;
import nccloud.framework.web.container.IRequest;
import nccloud.framework.web.json.JsonFactory;
import nccloud.framework.web.ui.pattern.billcard.BillCard;
import nccloud.framework.web.ui.pattern.billcard.BillCardOperator;
import nccloud.framework.web.ui.pattern.grid.Grid;
import nccloud.framework.web.ui.pattern.grid.GridOperator;
import nccloud.pubitf.arap.arappub.IArapFlowUtilService;
import nccloud.pubitf.riart.pflow.CloudPFlowContext;
import nccloud.pubitf.riart.pflow.ICloudScriptPFlowService;
import nccloud.web.arap.arappub.Info.SaveInfo;
import nccloud.web.arap.arappub.util.ArapBillScaleUtil;
import nccloud.web.arap.arappub.util.ArapBillUIUtil;
import nccloud.web.arap.arappub.util.BillCardUtil;
import nccloud.web.workflow.approve.util.NCCFlowUtils;

/**
 * 单据提交处理类
 * 
 * @author wangshyh
 * 
 */
public class BillCommitAction implements ICommonAction {

	@Override
	public Object doAction(IRequest request) {
		String str = request.read();
		IJson json = JsonFactory.create();
		SaveInfo info = json.fromJson(str, SaveInfo.class);
		AggregatedValueObject aggVO = null;
		try {
			IArapBillService service = ServiceLocator.find(IArapBillService.class);
			BaseAggVO[] bills = service.queryArapBillByPKs(new String[] { info.getPk_bill() }, info.getBillType());
			if (bills == null || (bills.length == 1 && bills[0] == null) || bills.length == 0) {
				ExceptionUtils.wrapBusinessException(
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2006pub_0", "02006pub-0765")/** @res "数据异常，数据不存在！"*/);
			}
			BaseAggVO bill = bills[0];
			if (info.getExtype() != null && ("1").equals(info.getExtype())) {
				bill.setAlarmPassed(info.isFlag());
			}
			if (info.getExtype() != null && ("2").equals(info.getExtype())) {
				bill.setCrossCheckPassed(info.isFlag());
			}
			BaseBillVO parent = (BaseBillVO) bill.getParentVO();
			
			if (!parent.getTs().equals(info.getTs())) {
				ExceptionUtils.wrapBusinessException(
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2006pub_0", "02006pub-0771")/** @res "单据已经被他人修改，请刷新界面，重做业务！"*/);
			}
//			if(bill instanceof AggReceivableBillVO) {
//				SendRecbillAction send = new SendRecbillAction();
//				send.setAggReceivableBillVO((AggReceivableBillVO)bill);
//				
//			}
			// 检查单据是否可以提交
			if (parent.getApprovestatus().intValue() != ApproveStatus.NOSTATE.VALUE.intValue()) {
				ExceptionUtils.wrapBusinessException(
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2006pub_0", "02006pub-0774")/** @res "只有自由态单据才可以提交"*/);
			}
			
			parent.setAttributeValue("ts", info.getTs());

			boolean hasApproveflowDef = false;
			// 传审批中心消息没有表头值
			ArapBillPubUtil.refreshChildVO2HeadVO(bill);
			String actionCode = ServiceLocator.find(IArapFlowUtilService.class).getCommitActionCode(bill.getHeadVO().getPk_org(), bill.getHeadVO().getPk_billtype());
			if (BillActionConstant.COMMIT.equals(actionCode)) {
				hasApproveflowDef = NCCFlowUtils.hasflowDef(parent.getPk_tradetype(), bill, WorkflowTypeEnum.Approveflow);
			} else {
				hasApproveflowDef = NCCFlowUtils.hasflowDef(parent.getPk_tradetype(), bill, WorkflowTypeEnum.Workflow);
				// 配置了共享委托关系，但是没有配置工作流，提示
				if (!hasApproveflowDef) {
					throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2006pub_0", "02006pub-0759")/* @res "配置了共享委托关系，但找不到工作流定义，无法启动工作流" */);
				}
			}
			// 判断是否有流程
			if (hasApproveflowDef) {
				/** 增加提交即指派功能 add by liubao since 2018-09-17 BEGIN **/
				Object obj = executeBatchPM(actionCode, bill, info);
				if (obj instanceof AggregatedValueObject) {
					aggVO = (AggregatedValueObject) obj;
				} else {
					return obj;
				}
				/** 增加提交即指派功能 add by liubao since 2018-09-17 END **/
			} else {
				CloudPFlowContext cloudPFlowContext = new CloudPFlowContext();
				cloudPFlowContext.setActionName(BillActionConstant.COMMIT);
				cloudPFlowContext.setBillType(parent.getPk_billtype());
				cloudPFlowContext.setTrantype(parent.getPk_tradetype());
				cloudPFlowContext.setBillVos(bills);
				cloudPFlowContext.setUserObj(new PfUserObject[] { new PfUserObject() });
				cloudPFlowContext.setBatch(false);
				Object[] objs = ServiceLocator.find(ICloudScriptPFlowService.class).exeScriptPFlow_CommitNoFlowBatch(cloudPFlowContext);
				PfProcessBatchRetObject retObj = (PfProcessBatchRetObject) objs[0];
				if (StringUtil.isEmptyWithTrim(retObj.getExceptionMsg())) {
					aggVO = (AggregatedValueObject) retObj.getRetObj()[0];
				} else {
					String exc = retObj.getExceptionInfo().getHm_index_exception().get(0).toString();
					Throwable exception = ExceptionUtils.unmarsh(retObj.getExceptionInfo().getHm_index_exception().get(0));
					if (exc.indexOf("nc.vo.arap.exception.ArapTbbException") != -1) {
						// ExceptionUtils.wrapBusinessException("exType1"+retObj.getExceptionMsg());
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("message", exception.getMessage());
						map.put("exType", "1");
						return map;
					} 
					// 本版本简化处理，直接抛异常。不做交互处理 add by zhaoyanf
//					else if (exception.indexOf("nc.vo.credit.exception.CreditCheckException") != -1) {
//
//					} else if (exception.indexOf("nc.vo.arap.exception.ARAP4CmpAuthorizationException") != -1) {
//
//					}
					ExceptionUtils.wrapBusinessException(retObj.getExceptionMsg());
				}
			}
			/**
			 * 设置表体第一行的一些field数值， 到表头显示
			 */
			ArapBillUIUtil.refreshChildVO2HeadVO(aggVO);
			if (info.getType() != null && 1 == info.getType()) {
				CircularlyAccessibleValueObject headVO = aggVO.getParentVO();
				GridOperator operator = new GridOperator(info.getPageId());
				operator.setTransFlag(false);//设置隐藏字段不翻译
				Grid grid = operator.toGrid(new CircularlyAccessibleValueObject[] { headVO });
				new ArapBillScaleUtil().processListScale(grid);
				return grid;
			} else {
			
				BillCardOperator operator = new BillCardOperator(info.getPageId());
				operator.setTransFlag(false);//设置隐藏字段不翻译
				BillCard billCard = operator.toCard(aggVO);
				new ArapBillScaleUtil().processBillCardScale(billCard);
				//特殊处理票据号查询翻译问题
				String pk_billType= String.valueOf(aggVO.getParentVO().getAttributeValue(IBillFieldGet.PK_BILLTYPE));
				if("F2".equals(pk_billType) || "F3".equals(pk_billType)){
					BillCardUtil.dealSubjcodeAndCheckNo(billCard);
				}
				return billCard;
			}
		} catch (Exception e) {
			// 抛出的是基类异常，转成具体子类异常；
			Throwable exception = ExceptionUtils.unmarsh(e);
			if (exception instanceof ArapTbbException) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("message", exception.getMessage());
				map.put("exType", "1");
				return map;
			} else {
				ExceptionUtils.wrapException(e);
			}
			// 本版本简化处理，不处理交互异常
			// else if (exception instanceof CreditCheckException) {
			//
			// } else if (exception instanceof ARAP4CmpAuthorizationException) {
			//
			// }
			
		}
		return null;

	}

	private Object executeBatchPM(String actionCode, BaseAggVO bill, SaveInfo info) throws Exception {
		String tranType = bill.getHeadVO().getPk_tradetype();
		if (BillActionConstant.START.equals(actionCode)) {
			WorkflownoteVO noteVO = null;
			try {
				noteVO = ServiceLocator.find(IWorkflowMachine.class).checkWorkFlow(IPFActionName.SIGNAL, tranType, bill, null);
			} catch (Exception ex) {
				// do nothing
			}
			if (noteVO != null) {
				actionCode = IPFActionName.SIGNAL;
			}
		}
		/******************** 提交重走流程处理 END ********************/
		Object executePM = ArapBillUIUtil.executeBatchPM(actionCode, bill, info);
		return executePM;
	}

}
