package ssc.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.server.util.NewObjectService;
import nc.bs.logging.Logger;
import nc.itf.uap.IVOPersistence;
import nc.itf.uap.ml.DataMultiLangAccessor;
import nc.itf.uap.pf.metadata.IFlowBizItf;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.jdbc.framework.processor.MapListProcessor;
import nc.md.data.access.NCObject;
import nc.message.templet.bs.IMsgVarCalculater;
import nc.message.templet.bs.MDVarCalculater;
import nc.message.templet.bs.MsgContentCreator;
import nc.message.templet.bs.MsgTextConvertor;
import nc.message.templet.bs.SysVarCalculater;
import nc.message.templet.itf.IMsgtempletquery;
import nc.message.templet.vo.MsgtempletVO;
import nc.message.templet.vo.MsgtmptypeVO;
import nc.message.util.QuickMessageTool;
import nc.message.vo.NCMessage;
import nc.message.vo.SmartMsgVO;
import nc.pubitf.ssc.task.service.ISSCTaskOperateService;
import nc.pubitf.ssc.task.service.ISSCTaskOuterService;
import nc.pubitf.ssc.task.service.ITaskPreprocessor;
import nc.uap.lfw.core.LfwRuntimeEnvironment;
import nc.uap.ws.log.NCLogger;
import nc.ui.pub.print.IDataSource;
import nc.vo.cits.CuiBanTimeVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.ml.LanguageVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.pf.workflow.IPFActionName;
import nc.vo.ssc.task.service.SSCTaskPreVO;
import nc.vo.ssc.wf.BillApproveStatus;
import nc.vo.wfengine.core.activity.Activity;
import nc.vo.wfengine.core.application.WfGadgetContext;
import nc.vo.wfengine.core.application.WfGadgetControlSource;
import nc.vo.wfengine.core.parser.XPDLParserException;
import nc.wf.IBusinessOperator;
import nc.wf.SscObject;
import nc.wf.WFBusiOperType;
import nc.wf.WFCallBackContext;
import nc.wf.landmarkstate.WFLandMarkStateHelp;
import ssc.wf.util.ISSCWFUtil;
import ssc.wf.util.SSCWFUtil;
import ssc.wf.util.WF2SSCMap;

/**
 * 服务器端公用类
 * 
 * @author zhaojianc
 * 
 */
public class SSCWFUtilBS {
	static {

	}

	/**
	 * 根据工作流组件进行任务驱动开始 <b>n只能服务器端调用</b>
	 * 
	 * @param gc
	 * @throws BusinessException
	 */
	public static void driveSSCTaskStart(WfGadgetContext gc, String sscActCode)
			throws BusinessException {
		ITaskPreprocessor tp = NCLocator.getInstance().lookup(
				ITaskPreprocessor.class);
		ISSCTaskOuterService ssctouts = NCLocator.getInstance().lookup(
				ISSCTaskOuterService.class);
		// 进入环节前，需要调用SSC任务接口压入任务
		// 要区分开是从前面环节或者是驳回时
		AggregatedValueObject billVO = (AggregatedValueObject) gc
				.getBillEntity();
		NCObject ncObj = NCObject.newInstance(billVO);
		IFlowBizItf itf = (IFlowBizItf) ncObj
				.getBizInterface(IFlowBizItf.class);

		String billPK = itf.getBillId();
		Object userObj = gc.getUserObj();

		String src_system = null;
		String relationid = billPK;
		SSCTaskPreVO vo = new SSCTaskPreVO();
		vo.setBillvo(billVO);

		String message = null;

		// 正常经过的任务
		int index = 1;
		// boolean b = SSCWFUtil.getIfStart(gc);
		if (userObj == null) {
			// 寻找会计初审的处理人 对应于SSC初审优先分配给会计初审人
			ISSCWFUtil util = NCLocator.getInstance().lookup(ISSCWFUtil.class);
			String checkman = util.findAccountExamCheckManPK(gc.getActivity()
					.getWorkflowProcess(), billPK, itf.getBilltype());

			// 获取需要SSC进行处理的活动
			// Map<String, Activity> mpAct =
			// getSSCActivity((BasicWorkflowProcess)
			// gc.getActivity().getWorkflowProcess(), itf.getBilltype());
			Map<String, Activity> mpAct = SSCWFUtil
					.getSSCActivityAccWorkflowSeq(gc, itf.getBilltype());
			String[] arySSCAct = new String[mpAct.size()];

			index = 1;
			for (Entry<String, Activity> entry : mpAct.entrySet()) {

				WF2SSCMap wf2sscmap = SSCWFUtil.getSSCTaskType(entry.getKey());
				if (wf2sscmap == null)
					throw new BusinessException("工作流活动类型" + sscActCode
							+ "与SSC任务没有定义映射关系");

				if (entry.getKey().equals(sscActCode))
					// 初审放到第一个
					arySSCAct[0] = wf2sscmap.getSscTaskAct();
				else
					arySSCAct[index++] = wf2sscmap.getSscTaskAct();
			}
			// 是否要加参数标示第一环节
			tp.add(checkman, src_system, relationid, vo, message, arySSCAct,
					arySSCAct[0]);// liningc

			// 改变状态
			String approvecol = itf.getColumnName(itf.ATTRIBUTE_APPROVESTATUS);
			CircularlyAccessibleValueObject parent = billVO.getParentVO();
			if (!(parent instanceof uap.lfw.dbl.vo.MetaDataBaseVO)) {
				// 当前状态
				int curStatus = itf.getApproveStatus();
				// 如果为-1，则需要更改为原来的状态，表明驳回直接提交的
				if (curStatus == -1) {
					// 从SSC单据状态表中获取上一次的保存的状态进行还原
					BillApproveStatus oldStatus = util.queryApproveStatus(itf
							.getBillId());
					if (oldStatus != null) {
						itf.setApproveStatus(oldStatus.getApprovestatus());
						BaseDAO dao = new BaseDAO();
						dao.updateVO((SuperVO) parent,
								new String[] { approvecol });
					}
					// 为保证数据干净，不管存在不存在都进行清除
					util.deleteApproveStatus(itf.getBillId());
				}
			}
		} else {
			if (!(userObj instanceof SscObject))
				throw new BusinessException("单据已进入共享服务中心，只能在共享服务中心进行处理");
			SscObject sscObject = (SscObject) userObj;

			WF2SSCMap wf2sscmap = SSCWFUtil.getSSCTaskType(sscActCode);
			if (wf2sscmap == null)
				throw new BusinessException("工作流活动类型" + sscActCode
						+ "与SSC任务没有定义映射关系");

			// 正常进入或驳回
			String[] taskids = { sscObject.getSscTaskID() };
			String userid = InvocationInfoProxy.getInstance().getUserId();
			ssctouts.nextSSCBusiActivity(userid, taskids,
					wf2sscmap.getSscTaskAct());

		}
		//=======================add by yzr 
		BaseDAO dao = new BaseDAO();
		String billid = billPK;
		String billtype = itf.getBilltype();
		String pk_org = itf.getPkorg();
		String userid = itf.getBillMaker();
		String sql_cb = "select code from bd_defdoc where pk_defdoclist ="
				+ " (select pk_defdoclist from bd_defdoclist where code='dxpt_0001')"
				+ " and code=(select code from org_orgs where pk_org='"
				+ pk_org + "')";
		List<Map<String, String>> lmap = (List<Map<String, String>>) dao
				.executeQuery(sql_cb, new MapListProcessor());
		if (lmap != null && lmap.size() > 0) {
			// 验证手机号是否为空
			String sql_user = "select p.mobile,u.user_name from sm_user u left join bd_psndoc p on p.pk_psndoc=u.pk_psndoc where u.cuserid='"
					+ userid + "'";
			List<Map<String, String>> lmap_user = (List<Map<String, String>>) dao
					.executeQuery(sql_user, new MapListProcessor());
			if (lmap_user != null && lmap_user.size() > 0) {
				Map<String, String> map1 = lmap_user.get(0);
				if (map1.get("mobile") != null
						&& map1.get("mobile").length() > 0) {
					// 8发送短信
					SmartMsgVO smsvo = new SmartMsgVO();
					smsvo.setMsgtype("sms");// 发送类型
					smsvo.setReceiver(userid);// 接收人
					smsvo.setSender(userid);// 发送人
					smsvo.setSubject("自动催办");// 主题
					String msg = "您提交的";
					Object djlxbm = billVO.getParentVO().getAttributeValue("djlxbm");
					String sql_djlx = "";
					if(djlxbm!=null){
						//如果是报销单的情况
						sql_djlx = "select djlxmc from er_djlx where nvl(dr,0)=0  and djlxbm='"+billVO.getParentVO().getAttributeValue("djlxbm")+"'";
					}else {
						//如果是费用预提单
						sql_djlx = "select djlxmc from er_djlx where nvl(dr,0)=0  and djlxbm='"+billVO.getParentVO().getAttributeValue("pk_billtype")+"'";
					}
					
					
					List<Map<String, String>> lmap_djlx = (List<Map<String, String>>) dao
							.executeQuery(sql_djlx, new MapListProcessor());
					//其它单据类型的情况：比如借款单、付款申请单
					String sql_djlx2 = "select billtypename from bd_billtype where nvl(dr,0)=0 and pk_billtypecode ='"+billVO.getParentVO().getAttributeValue("pk_trantypecode")+"'";
					List<Map<String, String>> lmap_djlx2 = (List<Map<String, String>>) dao
							.executeQuery(sql_djlx2, new MapListProcessor());
					if (lmap_djlx != null && lmap_djlx.size() > 0) {
						msg += "'" + lmap_djlx.get(0).get("djlxmc") + "'";
					} else if (lmap_djlx2 != null && lmap_djlx2.size() > 0) {
						msg += "'" + lmap_djlx2.get(0).get("billtypename") + "'";
					}else{
						
						sql_djlx = "select djlxmc from er_djlx where nvl(dr,0)=0  and djlxbm='"+billVO.getParentVO().getAttributeValue("pk_tradetype")+"'";
						lmap_djlx = (List<Map<String, String>>) dao
								.executeQuery(sql_djlx, new MapListProcessor());
						if (lmap_djlx != null && lmap_djlx.size() > 0) {
							msg += "'" + lmap_djlx.get(0).get("djlxmc") + "'";
						}else{
							throw new BusinessException("发送短信过程中，获取单据类型名称错误!");
						}
						
						//throw new BusinessException("发送短信过程中，获取单据类型名称错误!");
						
					}
					
							msg+= "报销单已流转至财务共享中心，请将审批单打印后和实体票据一并交至财务部";
//					MsgtempletVO msgtmp = new MsgtempletVO();
//					try {
//						List<MsgtempletVO> list = (List<MsgtempletVO>) dao.executeQuery("select * from pub_msgtemp where tempcode='99' and typecode ='264X'", new BeanListProcessor(MsgtempletVO.class));
//						if(list!=null&&list.size()>0){
//							msgtmp = list.get(0);
//						}
//						String langcode = msgtmp.getLangcode();
//						MsgContentCreator mcc = new MsgContentCreator();
//						MDVarCalculater vc = new MDVarCalculater(null, ncObj);
//						NCMessage ncm = new NCMessage();
//						Map<String, NCMessage> mncm  = mcc.createMessageUsingTemp("99","0001A2100000000007R8",new String[]{langcode},ncm,vc,ncObj,null);
//						ncm = mncm.get(langcode);
						
//						billVO.getParentVO().getAttributeValue(arg0)
//						// 构造消息中文本部分
//						InvocationInfoProxy.getInstance().setLangCode(langcode);
//						MsgTextConvertor textconv = new MsgTextConvertor();
//						textconv.installCalculater(new SysVarCalculater());
////						if(busivarcal!=null){
////							textconv.installCalculater(busivarcal);
////						}
//						IMsgtempletquery query = NCLocator.getInstance().lookup(IMsgtempletquery.class);
//						MsgtmptypeVO typevo = query.getTemptypeVOByCode(msgtmp.getTypecode());
//						String metaid = null;
//						if(typevo!=null){
//							metaid = typevo.getMetaid();
//						} 
//						if(ncObj!=null&&metaid!=null){
//							textconv.installCalculater(new MDVarCalculater(metaid,ncObj));
//						}	
//						msg = textconv.convertMsgText(msgtmp.getMessagetitle());
//					} catch (DAOException e) {
//						Logger.error(e.getMessage(), e);
//					} catch (BusinessException e) {
//						Logger.error(e.getMessage(), e);
//					}
					if(msg!=null&&msg.length()>0){
						smsvo.setContent(msg);// 消息内容
						CuiBanTimeVO cbvo = new CuiBanTimeVO();
						cbvo.setBillid(billid);
						cbvo.setCbuserid(userid);
						cbvo.setCbdate(new UFDateTime());
						dao.insertVO(cbvo);
						try {
							QuickMessageTool.sendMessage(smsvo);
						} catch (Exception e) {
							throw new BusinessException("短信催办制单人异常！");
						}
					}
				} /*else {
					throw new BusinessException(
							"制单人:" + map1.get("user_name")
							+ "未维护手机号无法为您催办");
				}*/

			}
			
		}else{
//			throw new BusinessException(("当前组织未开通，进入共享自动催办功能!"+sql_cb));
		}
		//============================================================end
	}
	
	/**
	 * @author yzr
	 * @param billtype
	 * @param busiobj
	 * @param busivarcal
	 * @return
	 */
	public String getMessage(String billtype,NCObject busiobj,IMsgVarCalculater busivarcal){
		String msg = null;
		MsgtempletVO msgtmp = new MsgtempletVO();
		try {
			BaseDAO dao = new BaseDAO();
			List<MsgtempletVO> list = (List<MsgtempletVO>) dao.executeQuery("select * from pub_msgtemp where tempcode='99' and typecode ='264X'", new BeanListProcessor(MsgtempletVO.class));
			if(list!=null){
				msgtmp = list.get(0);
			}else{
				return null;
			}
			String langcode = msgtmp.getLangcode();
			// 构造消息中文本部分
			InvocationInfoProxy.getInstance().setLangCode(langcode);
			MsgTextConvertor textconv = new MsgTextConvertor();
			textconv.installCalculater(new SysVarCalculater());
			if(busivarcal!=null){
				textconv.installCalculater(busivarcal);
			}
			IMsgtempletquery query = NCLocator.getInstance().lookup(IMsgtempletquery.class);
			MsgtmptypeVO typevo = query.getTemptypeVOByCode(msgtmp.getTypecode());
			String metaid = null;
			if(typevo!=null){
				metaid = typevo.getMetaid();
			} 
			if(busiobj!=null&&metaid!=null){
				textconv.installCalculater(new MDVarCalculater(metaid,busiobj));
			}	
			msg = textconv.convertMsgText(msgtmp.getMessagetitle());
		} catch (DAOException e) {
			Logger.error(e.getMessage(), e);
		} catch (BusinessException e) {
			Logger.error(e.getMessage(), e);
		}
		return msg;
	}

	/**
	 * 根据工作流组件进行任务驱动完成
	 * 
	 * @param gc
	 * @throws BusinessException
	 */
	public static void driveSSCTaskFinish(WfGadgetContext gc, String sscActCode)
			throws BusinessException {
		ISSCTaskOperateService ssctos = NCLocator.getInstance().lookup(
				ISSCTaskOperateService.class);
		ISSCTaskOuterService ssctouts = NCLocator.getInstance().lookup(
				ISSCTaskOuterService.class);
		Object billVO = gc.getBillEntity();
		NCObject ncObj = NCObject.newInstance(billVO);
		IFlowBizItf itf = (IFlowBizItf) ncObj
				.getBizInterface(IFlowBizItf.class);
		Object userObj = gc.getUserObj();
		if (userObj == null) {
			throw new BusinessException("单据已进入共享服务中心，只能在共享服务中心进行处理");
		}
		SscObject sscobject = (SscObject) userObj;

		String pk_org = itf.getPkorg();
		String userid = InvocationInfoProxy.getInstance().getUserId();
		String[] taskids = { sscobject.getSscTaskID() };
		String message = sscobject.getChecknote();
		Object otherparam = null;

		WF2SSCMap wf2sscmap = SSCWFUtil.getSSCTaskType(sscActCode);
		// 得到映射关系
		if (wf2sscmap == null)
			throw new BusinessException("工作流活动类型" + sscActCode
					+ "与SSC任务没有定义映射关系");

		ssctos.doOperate(pk_org, userid, wf2sscmap.getSscTaskAct(),
				wf2sscmap.getPasscode(), taskids, message, otherparam);// 改变任务状态接口

		// 如果此环节是SSC的结束环节，则调用SSC的任务完成接口
		// 2014-11-19 由于性能原因，先暂时取工作流参数设置的值
		// ISSCWFUtil util = NCLocator.getInstance().lookup(ISSCWFUtil.class);
		// boolean ifFinal = util.isSSCFinalActivity(gc.getActivity(),
		// itf.getTranstype());
		boolean ifFinal = SSCWFUtil.getIfFinal(gc);
		if (ifFinal) {
			ssctouts.sealSSCTask(userid, taskids);// 任务完成接口
		}

		// 处理领域业务注册的操作
		WFCallBackContext callContext = new WFCallBackContext();
		callContext.setGadgetContext(gc);
		callContext.setFlag(sscActCode);
		SSCWFUtilBS.executeBusiOper(sscActCode, itf.getBilltype(), callContext,
				WFBusiOperType.doAfter);
		// 工作流里程碑状态
		WFLandMarkStateHelp.getInstance().insertOperatLog(sscActCode,
				itf.getBillId(), itf.getTranstype(), sscActCode);
		return;
	}

	/**
	 * <b> 只能服务器端调用 </b> 在工作流组件中调用业务插件进行业务操作
	 * 
	 * @throws BusinessException
	 */
	public static void executeBusiOper(String actCode, String billType,
			WFCallBackContext context, WFBusiOperType operType)
			throws BusinessException {
		Map<String, List<String>> billTypeOperClass = SSCWFCachePoolProxy
				.getInstance().getAllBillTypeOperClassMap();
		String key = actCode + "-" + billType;
		if (!billTypeOperClass.containsKey(key))
			return;
		List<String> lstoper = billTypeOperClass.get(key);
		for (String opers : lstoper) {
			String[] aryOpers = opers.split("\\|");
			String module = aryOpers[0];
			String busiclass = aryOpers[1];
			try {
				executeClass(module, busiclass, context, operType);
			} catch (Exception e) {
				Logger.error(e);
				throw new BusinessException(e.getMessage());
			}
		}
	}

	/**
	 * 根据工作流组件进行任务驳回 <b>只能服务器端调用</b>
	 * 
	 * @param gc
	 * @throws BusinessException
	 */
	public static void driveSSCTaskReject(WfGadgetContext gc, String sscActCode)
			throws BusinessException {
		ISSCTaskOperateService ssctos = NCLocator.getInstance().lookup(
				ISSCTaskOperateService.class);

		// 如果回退来源于流程管理中心，则什么都不做
		if (gc.getControlSource().equals(WfGadgetControlSource.Manage))
			return;

		if (IPFActionName.RECALL.endsWith(gc.getPfParameterVO().m_actionName)
				|| IPFActionName.UNAPPROVE
						.endsWith(gc.getPfParameterVO().m_actionName)
				|| IPFActionName.UNSAVE
						.endsWith(gc.getPfParameterVO().m_actionName))
			throw new BusinessException("单据已进入共享服务中心，只能在共享服务中心进行处理");

		// 撤销需要调用SSC任务接口移除任务，用这个方法处理驳回时任务从SSC中移除
		Object billVO = gc.getBillEntity();
		NCObject ncObj = NCObject.newInstance(billVO);
		IFlowBizItf itf = (IFlowBizItf) ncObj
				.getBizInterface(IFlowBizItf.class);

		Object userObj = gc.getUserObj();

		// 在共享服务中心前一环节执行回退，会执行回退环节此方法，需要做特殊处理
		// 回退对于任务的处理在driveSSCTaskRollBack进行处理
		if (IPFActionName.ROLLBACK.endsWith(gc.getPfParameterVO().m_actionName)) {
			// 如果useObj==null，表明是共享服务之外的环节在回退，这种情况不允许
			// 回退
			if (userObj == null || !(userObj instanceof SscObject)) {
				throw new BusinessException("单据已进入共享服务中心，只能在共享服务中心进行处理");
			} else
				return;
		}

		// 其他情况下，userObj为null，表明其他环节正常驳回，略过
		if (userObj == null)
			return;

		SscObject sscObj = (SscObject) userObj;

		WF2SSCMap wf2sscmap = SSCWFUtil.getSSCTaskType(sscActCode);
		// 得到映射关系
		if (wf2sscmap == null)
			throw new BusinessException("工作流活动类型" + sscActCode
					+ "与SSC任务没有定义映射关系");

		// 表明是驳回回来的
		if (sscObj.getSscSourceAction().equals("R")) {
			// 判断是否处理节点为当前节点，用于防止不重走流程时，多环节同时作废时而引发的问题
			if (gc.getActivity().getId().equals(sscObj.getSscSourceActiveID())) {
				String pk_org = itf.getPkorg();
				String userid = InvocationInfoProxy.getInstance().getUserId();
				SscObject sscobject = (SscObject) userObj;
				String[] taskids = { sscobject.getSscTaskID() };
				String message = null;
				// Object otherparam = null;
				Object otherparam = sscobject.getBusActCons(); // liningc
				ssctos.doOperate(pk_org, userid, wf2sscmap.getSscTaskAct(),
						wf2sscmap.getRejectcode(), taskids, message, otherparam);// 改变任务状态接口
			}
		}
	}

	private static void executeClass(String module, String busiclass,
			WFCallBackContext context, WFBusiOperType operType)
			throws BusinessException {
		Object classObject = null;
		classObject = NewObjectService.newInstance(module, busiclass);
		if (classObject != null && classObject instanceof IBusinessOperator) {
			IBusinessOperator busioper = (IBusinessOperator) classObject;
			switch (operType) {
			case doBefore:
				busioper.doBefore(context);
				break;
			case doAfter:
				busioper.doAfter(context);
				break;
			case undoBefore:
				busioper.undoBefore(context);
				break;
			case undoAfter:
				busioper.undoAfter(context);
				break;
			}
		}
	}

	/**
	 * 处理回退动作
	 * 
	 * @param gc
	 * @param reexamact
	 * @throws BusinessException
	 * @throws XPDLParserException
	 */
	public static void driveSSCTaskRollBack(WfGadgetContext gc,
			String sscActCode) throws BusinessException {
		ITaskPreprocessor tp = NCLocator.getInstance().lookup(
				ITaskPreprocessor.class);
		ISSCTaskOperateService ssctos = NCLocator.getInstance().lookup(
				ISSCTaskOperateService.class);
		ISSCTaskOuterService ssctouts = NCLocator.getInstance().lookup(
				ISSCTaskOuterService.class);

		// 如果回退来源于流程管理中心，则什么都不做
		if (gc.getControlSource().equals(WfGadgetControlSource.Manage))
			return;
		Object userObj = gc.getUserObj();

		// 只处理回退
		if (!IPFActionName.ROLLBACK.equals(gc.getPfParameterVO().m_actionName)) {
			return;
		}

		if (!(userObj instanceof SscObject)) {
			throw new BusinessException("单据已进入共享服务中心，只能在共享服务中心进行处理");
		}

		// 当前处理环节
		SscObject sscobject = (SscObject) userObj;
		String onAct = sscobject.getSscOnActivity();

		AggregatedValueObject billVO = (AggregatedValueObject) gc
				.getBillEntity();
		NCObject ncObj = NCObject.newInstance(billVO);
		IFlowBizItf itf = (IFlowBizItf) ncObj
				.getBizInterface(IFlowBizItf.class);
		String billId = itf.getBillId();
		String pk_org = itf.getPkorg();

		WF2SSCMap wf2sscmap = SSCWFUtil.getSSCTaskType(sscActCode);
		if (wf2sscmap == null) {
			throw new BusinessException("工作流活动类型" + sscActCode
					+ "与SSC任务没有定义映射关系");
		}

		String userid = InvocationInfoProxy.getInstance().getUserId();
		String[] taskids = { sscobject.getSscTaskID() };
		String message = null;

		WFCallBackContext callContext = new WFCallBackContext();
		callContext.setGadgetContext(gc);
		callContext.setFlag(sscActCode);
		SSCWFUtilBS.executeBusiOper(sscActCode, itf.getBilltype(), callContext,
				WFBusiOperType.undoAfter);

		ssctos.doOperate(pk_org, userid, onAct, wf2sscmap.getRevoke(), taskids,
				message, wf2sscmap.getSscTaskAct());// 改变任务状态接口

		if (StringUtil.isEmptyWithTrim(onAct)) {
			SSCTaskPreVO vo = new SSCTaskPreVO();
			vo.setBillvo(billVO);

			// 获取需要SSC进行处理的活动
			Map<String, Activity> mpAct = SSCWFUtil
					.getSSCActivityAccWorkflowSeq(gc, itf.getBilltype());
			String[] arySSCAct = new String[mpAct.size()];

			int index = 1;
			for (Entry<String, Activity> entry : mpAct.entrySet()) {

				wf2sscmap = SSCWFUtil.getSSCTaskType(entry.getKey());
				if (wf2sscmap == null)
					throw new BusinessException("工作流活动类型" + sscActCode
							+ "与SSC任务没有定义映射关系");

				if (entry.getKey().equals(sscActCode))
					// 初审放到第一个
					arySSCAct[0] = wf2sscmap.getSscTaskAct();
				else
					arySSCAct[index++] = wf2sscmap.getSscTaskAct();
			}
			// 是否要加参数标示第一环节
			tp.add(null, null, billId, vo, message, arySSCAct, arySSCAct[0]);
		} else {
			// 正常进入或驳回
			ssctouts.nextSSCBusiActivity(userid, taskids,
					wf2sscmap.getSscTaskAct());
		}
	}
}
