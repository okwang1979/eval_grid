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
 * �������˹�����
 * 
 * @author zhaojianc
 * 
 */
public class SSCWFUtilBS {
	static {

	}

	/**
	 * ���ݹ����������������������ʼ <b>nֻ�ܷ������˵���</b>
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
		// ���뻷��ǰ����Ҫ����SSC����ӿ�ѹ������
		// Ҫ���ֿ��Ǵ�ǰ�滷�ڻ����ǲ���ʱ
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

		// ��������������
		int index = 1;
		// boolean b = SSCWFUtil.getIfStart(gc);
		if (userObj == null) {
			// Ѱ�һ�Ƴ���Ĵ����� ��Ӧ��SSC�������ȷ������Ƴ�����
			ISSCWFUtil util = NCLocator.getInstance().lookup(ISSCWFUtil.class);
			String checkman = util.findAccountExamCheckManPK(gc.getActivity()
					.getWorkflowProcess(), billPK, itf.getBilltype());

			// ��ȡ��ҪSSC���д���Ļ
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
					throw new BusinessException("�����������" + sscActCode
							+ "��SSC����û�ж���ӳ���ϵ");

				if (entry.getKey().equals(sscActCode))
					// ����ŵ���һ��
					arySSCAct[0] = wf2sscmap.getSscTaskAct();
				else
					arySSCAct[index++] = wf2sscmap.getSscTaskAct();
			}
			// �Ƿ�Ҫ�Ӳ�����ʾ��һ����
			tp.add(checkman, src_system, relationid, vo, message, arySSCAct,
					arySSCAct[0]);// liningc

			// �ı�״̬
			String approvecol = itf.getColumnName(itf.ATTRIBUTE_APPROVESTATUS);
			CircularlyAccessibleValueObject parent = billVO.getParentVO();
			if (!(parent instanceof uap.lfw.dbl.vo.MetaDataBaseVO)) {
				// ��ǰ״̬
				int curStatus = itf.getApproveStatus();
				// ���Ϊ-1������Ҫ����Ϊԭ����״̬����������ֱ���ύ��
				if (curStatus == -1) {
					// ��SSC����״̬���л�ȡ��һ�εı����״̬���л�ԭ
					BillApproveStatus oldStatus = util.queryApproveStatus(itf
							.getBillId());
					if (oldStatus != null) {
						itf.setApproveStatus(oldStatus.getApprovestatus());
						BaseDAO dao = new BaseDAO();
						dao.updateVO((SuperVO) parent,
								new String[] { approvecol });
					}
					// Ϊ��֤���ݸɾ������ܴ��ڲ����ڶ��������
					util.deleteApproveStatus(itf.getBillId());
				}
			}
		} else {
			if (!(userObj instanceof SscObject))
				throw new BusinessException("�����ѽ��빲��������ģ�ֻ���ڹ���������Ľ��д���");
			SscObject sscObject = (SscObject) userObj;

			WF2SSCMap wf2sscmap = SSCWFUtil.getSSCTaskType(sscActCode);
			if (wf2sscmap == null)
				throw new BusinessException("�����������" + sscActCode
						+ "��SSC����û�ж���ӳ���ϵ");

			// ��������򲵻�
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
			// ��֤�ֻ����Ƿ�Ϊ��
			String sql_user = "select p.mobile,u.user_name from sm_user u left join bd_psndoc p on p.pk_psndoc=u.pk_psndoc where u.cuserid='"
					+ userid + "'";
			List<Map<String, String>> lmap_user = (List<Map<String, String>>) dao
					.executeQuery(sql_user, new MapListProcessor());
			if (lmap_user != null && lmap_user.size() > 0) {
				Map<String, String> map1 = lmap_user.get(0);
				if (map1.get("mobile") != null
						&& map1.get("mobile").length() > 0) {
					// 8���Ͷ���
					SmartMsgVO smsvo = new SmartMsgVO();
					smsvo.setMsgtype("sms");// ��������
					smsvo.setReceiver(userid);// ������
					smsvo.setSender(userid);// ������
					smsvo.setSubject("�Զ��߰�");// ����
					String msg = "���ύ��";
					Object djlxbm = billVO.getParentVO().getAttributeValue("djlxbm");
					String sql_djlx = "";
					if(djlxbm!=null){
						//����Ǳ����������
						sql_djlx = "select djlxmc from er_djlx where nvl(dr,0)=0  and djlxbm='"+billVO.getParentVO().getAttributeValue("djlxbm")+"'";
					}else {
						//����Ƿ���Ԥ�ᵥ
						sql_djlx = "select djlxmc from er_djlx where nvl(dr,0)=0  and djlxbm='"+billVO.getParentVO().getAttributeValue("pk_billtype")+"'";
					}
					
					
					List<Map<String, String>> lmap_djlx = (List<Map<String, String>>) dao
							.executeQuery(sql_djlx, new MapListProcessor());
					//�����������͵��������������������뵥
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
							throw new BusinessException("���Ͷ��Ź����У���ȡ�����������ƴ���!");
						}
						
						//throw new BusinessException("���Ͷ��Ź����У���ȡ�����������ƴ���!");
						
					}
					
							msg+= "����������ת�����������ģ��뽫��������ӡ���ʵ��Ʊ��һ����������";
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
//						// ������Ϣ���ı�����
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
						smsvo.setContent(msg);// ��Ϣ����
						CuiBanTimeVO cbvo = new CuiBanTimeVO();
						cbvo.setBillid(billid);
						cbvo.setCbuserid(userid);
						cbvo.setCbdate(new UFDateTime());
						dao.insertVO(cbvo);
						try {
							QuickMessageTool.sendMessage(smsvo);
						} catch (Exception e) {
							throw new BusinessException("���Ŵ߰��Ƶ����쳣��");
						}
					}
				} /*else {
					throw new BusinessException(
							"�Ƶ���:" + map1.get("user_name")
							+ "δά���ֻ����޷�Ϊ���߰�");
				}*/

			}
			
		}else{
//			throw new BusinessException(("��ǰ��֯δ��ͨ�����빲���Զ��߰칦��!"+sql_cb));
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
			// ������Ϣ���ı�����
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
	 * ���ݹ�����������������������
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
			throw new BusinessException("�����ѽ��빲��������ģ�ֻ���ڹ���������Ľ��д���");
		}
		SscObject sscobject = (SscObject) userObj;

		String pk_org = itf.getPkorg();
		String userid = InvocationInfoProxy.getInstance().getUserId();
		String[] taskids = { sscobject.getSscTaskID() };
		String message = sscobject.getChecknote();
		Object otherparam = null;

		WF2SSCMap wf2sscmap = SSCWFUtil.getSSCTaskType(sscActCode);
		// �õ�ӳ���ϵ
		if (wf2sscmap == null)
			throw new BusinessException("�����������" + sscActCode
					+ "��SSC����û�ж���ӳ���ϵ");

		ssctos.doOperate(pk_org, userid, wf2sscmap.getSscTaskAct(),
				wf2sscmap.getPasscode(), taskids, message, otherparam);// �ı�����״̬�ӿ�

		// ����˻�����SSC�Ľ������ڣ������SSC��������ɽӿ�
		// 2014-11-19 ��������ԭ������ʱȡ�������������õ�ֵ
		// ISSCWFUtil util = NCLocator.getInstance().lookup(ISSCWFUtil.class);
		// boolean ifFinal = util.isSSCFinalActivity(gc.getActivity(),
		// itf.getTranstype());
		boolean ifFinal = SSCWFUtil.getIfFinal(gc);
		if (ifFinal) {
			ssctouts.sealSSCTask(userid, taskids);// ������ɽӿ�
		}

		// ��������ҵ��ע��Ĳ���
		WFCallBackContext callContext = new WFCallBackContext();
		callContext.setGadgetContext(gc);
		callContext.setFlag(sscActCode);
		SSCWFUtilBS.executeBusiOper(sscActCode, itf.getBilltype(), callContext,
				WFBusiOperType.doAfter);
		// ��������̱�״̬
		WFLandMarkStateHelp.getInstance().insertOperatLog(sscActCode,
				itf.getBillId(), itf.getTranstype(), sscActCode);
		return;
	}

	/**
	 * <b> ֻ�ܷ������˵��� </b> �ڹ���������е���ҵ��������ҵ�����
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
	 * ���ݹ���������������񲵻� <b>ֻ�ܷ������˵���</b>
	 * 
	 * @param gc
	 * @throws BusinessException
	 */
	public static void driveSSCTaskReject(WfGadgetContext gc, String sscActCode)
			throws BusinessException {
		ISSCTaskOperateService ssctos = NCLocator.getInstance().lookup(
				ISSCTaskOperateService.class);

		// ���������Դ�����̹������ģ���ʲô������
		if (gc.getControlSource().equals(WfGadgetControlSource.Manage))
			return;

		if (IPFActionName.RECALL.endsWith(gc.getPfParameterVO().m_actionName)
				|| IPFActionName.UNAPPROVE
						.endsWith(gc.getPfParameterVO().m_actionName)
				|| IPFActionName.UNSAVE
						.endsWith(gc.getPfParameterVO().m_actionName))
			throw new BusinessException("�����ѽ��빲��������ģ�ֻ���ڹ���������Ľ��д���");

		// ������Ҫ����SSC����ӿ��Ƴ��������������������ʱ�����SSC���Ƴ�
		Object billVO = gc.getBillEntity();
		NCObject ncObj = NCObject.newInstance(billVO);
		IFlowBizItf itf = (IFlowBizItf) ncObj
				.getBizInterface(IFlowBizItf.class);

		Object userObj = gc.getUserObj();

		// �ڹ����������ǰһ����ִ�л��ˣ���ִ�л��˻��ڴ˷�������Ҫ�����⴦��
		// ���˶�������Ĵ�����driveSSCTaskRollBack���д���
		if (IPFActionName.ROLLBACK.endsWith(gc.getPfParameterVO().m_actionName)) {
			// ���useObj==null�������ǹ������֮��Ļ����ڻ��ˣ��������������
			// ����
			if (userObj == null || !(userObj instanceof SscObject)) {
				throw new BusinessException("�����ѽ��빲��������ģ�ֻ���ڹ���������Ľ��д���");
			} else
				return;
		}

		// ��������£�userObjΪnull���������������������أ��Թ�
		if (userObj == null)
			return;

		SscObject sscObj = (SscObject) userObj;

		WF2SSCMap wf2sscmap = SSCWFUtil.getSSCTaskType(sscActCode);
		// �õ�ӳ���ϵ
		if (wf2sscmap == null)
			throw new BusinessException("�����������" + sscActCode
					+ "��SSC����û�ж���ӳ���ϵ");

		// �����ǲ��ػ�����
		if (sscObj.getSscSourceAction().equals("R")) {
			// �ж��Ƿ���ڵ�Ϊ��ǰ�ڵ㣬���ڷ�ֹ����������ʱ���໷��ͬʱ����ʱ������������
			if (gc.getActivity().getId().equals(sscObj.getSscSourceActiveID())) {
				String pk_org = itf.getPkorg();
				String userid = InvocationInfoProxy.getInstance().getUserId();
				SscObject sscobject = (SscObject) userObj;
				String[] taskids = { sscobject.getSscTaskID() };
				String message = null;
				// Object otherparam = null;
				Object otherparam = sscobject.getBusActCons(); // liningc
				ssctos.doOperate(pk_org, userid, wf2sscmap.getSscTaskAct(),
						wf2sscmap.getRejectcode(), taskids, message, otherparam);// �ı�����״̬�ӿ�
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
	 * ������˶���
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

		// ���������Դ�����̹������ģ���ʲô������
		if (gc.getControlSource().equals(WfGadgetControlSource.Manage))
			return;
		Object userObj = gc.getUserObj();

		// ֻ�������
		if (!IPFActionName.ROLLBACK.equals(gc.getPfParameterVO().m_actionName)) {
			return;
		}

		if (!(userObj instanceof SscObject)) {
			throw new BusinessException("�����ѽ��빲��������ģ�ֻ���ڹ���������Ľ��д���");
		}

		// ��ǰ������
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
			throw new BusinessException("�����������" + sscActCode
					+ "��SSC����û�ж���ӳ���ϵ");
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
				message, wf2sscmap.getSscTaskAct());// �ı�����״̬�ӿ�

		if (StringUtil.isEmptyWithTrim(onAct)) {
			SSCTaskPreVO vo = new SSCTaskPreVO();
			vo.setBillvo(billVO);

			// ��ȡ��ҪSSC���д���Ļ
			Map<String, Activity> mpAct = SSCWFUtil
					.getSSCActivityAccWorkflowSeq(gc, itf.getBilltype());
			String[] arySSCAct = new String[mpAct.size()];

			int index = 1;
			for (Entry<String, Activity> entry : mpAct.entrySet()) {

				wf2sscmap = SSCWFUtil.getSSCTaskType(entry.getKey());
				if (wf2sscmap == null)
					throw new BusinessException("�����������" + sscActCode
							+ "��SSC����û�ж���ӳ���ϵ");

				if (entry.getKey().equals(sscActCode))
					// ����ŵ���һ��
					arySSCAct[0] = wf2sscmap.getSscTaskAct();
				else
					arySSCAct[index++] = wf2sscmap.getSscTaskAct();
			}
			// �Ƿ�Ҫ�Ӳ�����ʾ��һ����
			tp.add(null, null, billId, vo, message, arySSCAct, arySSCAct[0]);
		} else {
			// ��������򲵻�
			ssctouts.nextSSCBusiActivity(userid, taskids,
					wf2sscmap.getSscTaskAct());
		}
	}
}
