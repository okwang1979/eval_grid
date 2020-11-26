package nccloud.web.workflow.approvalcenter.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import itf.approvecenter.util.DataExchangeBean;
import itf.approvecenter.util.InteractiveExceptionContext;
import nc.bs.logging.Logger;
import nc.itf.ct.purdaily.IPurdailyMaintain;
import nc.itf.ct.saledaily.ISaledailyMaintain;
import nc.itf.ct.sendsale.ISendSaleServer;
import nc.vo.arap.gathering.GatheringBillItemVO;
import nc.vo.ct.purdaily.entity.AggCtPuVO;
import nc.vo.ct.saledaily.entity.AggCtSaleVO;
import nc.vo.ct.saledaily.entity.PaymentPlanAndFeedbackInfo;
import nc.vo.ct.saledaily.entity.SaleParamCheckUtils;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.billtype2.Billtype2VO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.pf.AssignableInfo;
import nc.vo.pub.pf.TransitionSelectableInfo;
import nc.vo.pub.workflownote.WorkflownoteVO;
import nc.vo.wfengine.definition.WorkflowTypeEnum;
import nccloud.base.exception.ExceptionUtils;
import nccloud.commons.lang.StringUtils;
import nccloud.framework.core.json.IJson;
import nccloud.framework.service.ServiceLocator;
import nccloud.framework.web.action.itf.ICommonAction;
import nccloud.framework.web.container.IRequest;
import nccloud.framework.web.container.RequestSysJsonVO;
import nccloud.itf.uap.pf.IApproveBusiHandler;
import nccloud.itf.uap.pf.NCCWorkFlowService;
import nccloud.pubitf.platform.approve.AbstractApproveBusiHandlerImpl;
import nccloud.putitf.riart.billtype.IBilltypeService;
import nccloud.web.ct.saledaily.action.SaleSendRestUtil;
import nccloud.web.ct.saledaily.action.SaleUrlConst;
import nccloud.web.ct.saledaily.action.TokenInfo;
import nccloud.web.workflow.approve.util.ApproveWorkitemAssistor;
import nccloud.web.workflow.approve.util.NCCFlowUtils;
import nccloud.web.workflow.approve.util.NCCMsgContext;
import nccloud.framework.web.json.JsonFactory;

public class ApprovePassAction
  implements ICommonAction
{
  Set<String> busiexceptionCodeSet = new HashSet();

  public Object doAction(IRequest request)
  {
    String jsonStr = request.read();
    JSONObject json2 = JSONObject.parseObject(jsonStr);

    HashMap hmPfExParams = new HashMap();

    DataExchangeBean bean = new DataExchangeBean();
    bean.setCode("200");
    NCCMsgContext nccMsg = new NCCMsgContext(request.read());
    Map paramInfoMap = nccMsg.getMsgINfoContext();

    String[] flowParameterArray = { "billtype", "billid", "pk_checkflow" };

    if (NCCFlowUtils.isFlowParameterArrayNull(flowParameterArray, paramInfoMap))
    {
      return NCCFlowUtils.exchangeDataMsg(bean, "500", 
        NCLangRes4VoTransl.getNCLangRes().getStrByID("0020nccloud_0", "0ncc0020001-0428"));
    }

    String billTypeOrTransType = paramInfoMap.get("billtype")
      .toString();

    String billId = paramInfoMap.get("billid")
      .toString();
    String pk_checkflow = paramInfoMap.get("pk_checkflow").toString();

    String check_note = json2.getString("check_note");

    String skipcodes = json2.getString("skipCodes");

    NCCFlowUtils.checkIsExistMessage(billTypeOrTransType, billId);

    String paramBusiCodes = null;
    if ((paramInfoMap.get("skipCodes") != null) && 
      (!"[]"
      .equals(paramInfoMap
      .get("skipCodes")
      .toString())))
    {
      paramBusiCodes = paramInfoMap.get("skipCodes")
        .toString().substring(1, paramInfoMap.get("skipCodes")
        .toString().length() - 1)
        .replace("\"", "");
    }

    List paraMapList = new ArrayList();
    Map paraMap = new HashMap();	
    paraMap.put("NCCFlowParamter", "NCCloud");

    if (paramBusiCodes != null) {
      paraMap.put("NCC_BusiCodes", paramBusiCodes);
    }

    paraMapList.add(paraMap);

    NCCWorkFlowService nccWorkFlowService = (NCCWorkFlowService)ServiceLocator.find(NCCWorkFlowService.class);

    AggregatedValueObject billvo = null;

    WorkflownoteVO note = null;
    try
    {
      billvo = nccWorkFlowService.mobileAppUtilForQueryBillEntity(billTypeOrTransType, billId);
    }
    catch (BusinessException e)
    {
      ExceptionUtils.wrapBusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("0020nccloud_0", "0ncc0020001-0430"));
    }

    try
    {     
    	RequestSysJsonVO readSysParam = request.readSysParam();
	    String busiaction = readSysParam.getBusiaction();
	    if("D2".equals(billTypeOrTransType)  && busiaction.contains("审批中心-批准")) {
	    	GatheringBillItemVO[] childrenVO = (GatheringBillItemVO[]) billvo.getChildrenVO();
	    	//合同主键
	    	String pk_ct_sale = "";
	    	if(null != childrenVO && childrenVO.length > 0) {
	    		pk_ct_sale = childrenVO[0].getTop_billid();
	    	}
	    	//根据合同主键查询
//	    	CtSaleBillQueryDao  ctSaleBillQueryDao = new CtSaleBillQueryDao();
//	    	List<GatheringBillItemVO> queryCtSalePayterms = ctSaleBillQueryDao.queryCtSalePayterms(pk_ct_sale);
//	    	for (GatheringBillItemVO gatheringBillItemVO : queryCtSalePayterms) {
			try {
				String appUser="KGJN";
				String secretKey="OXpXfaLG5v0LZedTEi2F2WcnGQmPoi5n0m+srzE1kmE=";
				SaleUrlConst url = SaleUrlConst.getUrlConst();
				TokenInfo tInfo =   SaleSendRestUtil.restLogin( appUser, secretKey,url.getRestLogin());
				
				
			     if(!"200".equals(tInfo.getCode())) {
			      ExceptionUtils.wrapBusinessException(tInfo.getMessage());
			     }
			
				ISendSaleServer service = (ISendSaleServer) ServiceLocator.find(ISendSaleServer.class);
			     
			   //收款单协议计划反馈信息报送
				PaymentPlanAndFeedbackInfo planInfo = service.pushBillToService(pk_ct_sale);
				SaleParamCheckUtils.doValidator(planInfo);
				IJson json1 = JsonFactory.create();
				String jsonStrPlan =  json1.toJson(planInfo);
				String resultStr = SaleSendRestUtil.receiptBillInfo(appUser, tInfo.getToken(), jsonStrPlan, url.getReceiptBillInfo());
				TokenInfo info1 =  (TokenInfo)json1.fromJson(resultStr, TokenInfo.class);
				if(!"200".equals(info1.getCode())) {
					ExceptionUtils.wrapBusinessException("收款计划反馈：" + info1.getMessage());
				}
			 
				}catch(Exception ex){
					Logger.init();
					ExceptionUtils.wrapException(ex);
				}
			}
//	      }
	    //付款单计划、反馈信息报送
	    if("D3".equals(billTypeOrTransType)  && busiaction.contains("审批中心-批准")) {
	    	GatheringBillItemVO[] childrenVO = (GatheringBillItemVO[]) billvo.getChildrenVO();
	    	//合同主键
	    	String pk_pu_sale = "";
	    	if(null != childrenVO && childrenVO.length > 0) {
	    		pk_pu_sale = childrenVO[0].getTop_billid();
	    	}
	    	//根据合同主键查询
//	    	CtSaleBillQueryDao  ctSaleBillQueryDao = new CtSaleBillQueryDao();
//	    	List<GatheringBillItemVO> queryCtSalePayterms = ctSaleBillQueryDao.queryCtSalePayterms(pk_ct_sale);
//	    	for (GatheringBillItemVO gatheringBillItemVO : queryCtSalePayterms) {
			try {
				String appUser="KGJN";
				String secretKey="OXpXfaLG5v0LZedTEi2F2WcnGQmPoi5n0m+srzE1kmE=";
				SaleUrlConst url = SaleUrlConst.getUrlConst();
				TokenInfo tInfo =   SaleSendRestUtil.restLogin( appUser, secretKey,url.getRestLogin());
				
				
			     if(!"200".equals(tInfo.getCode())) {
			      ExceptionUtils.wrapBusinessException("付款计划：" + tInfo.getMessage());
			     }
			
				ISendSaleServer service = (ISendSaleServer) ServiceLocator.find(ISendSaleServer.class);
			     
			    //付款单计划信息报送
				String[] ids = {pk_pu_sale};
			    IPurdailyMaintain service1 = (IPurdailyMaintain) ServiceLocator.find(IPurdailyMaintain.class);
			    AggCtPuVO[] vos = service1.queryCtPuVoByIds(ids);
			    PaymentPlanAndFeedbackInfo planInfo = service.pushPayBillToService(vos[0]);
				SaleParamCheckUtils.doValidator(planInfo);
				IJson json = JsonFactory.create();
				String jsonStrPlan =  json.toJson(planInfo);
				String resultStr = SaleSendRestUtil.receiptBillInfo(appUser, tInfo.getToken(), jsonStrPlan, url.getReceiptBillInfo());
				TokenInfo info0 =  (TokenInfo)json.fromJson(resultStr, TokenInfo.class);
				if(!"200".equals(info0.getCode())) {
					ExceptionUtils.wrapBusinessException("付款计划反馈：" + info0.getMessage());
				}
				
				
			    //付款单计划反馈信息报送
				PaymentPlanAndFeedbackInfo feedBackInfo = service.pushPayBillToService(pk_pu_sale);
				SaleParamCheckUtils.doValidator(feedBackInfo);
				IJson json1 = JsonFactory.create();
				String jsonStrPlan1 =  json1.toJson(feedBackInfo);
				String resultStr1 = SaleSendRestUtil.receiptBillInfo(appUser, tInfo.getToken(), jsonStrPlan1, url.getReceiptBillInfo());
				TokenInfo info1 =  (TokenInfo)json1.fromJson(resultStr1, TokenInfo.class);
				if(!"200".equals(info1.getCode())) {
					ExceptionUtils.wrapBusinessException("收款计划反馈：" + info1.getMessage());
				}
			 
				}catch(Exception ex){
					Logger.init();
					ExceptionUtils.wrapException(ex);
				}
			}
      note = nccWorkFlowService.checkWorkflowActions(billTypeOrTransType, billId, pk_checkflow);

      if (note == null) {
        hmPfExParams.put("notechecked", "notechecked");
      }
      else {
        hmPfExParams.put("worknote", note);
      }

      NCCFlowUtils.checkIsCopySend(note, JSONObject.parseObject(request.read()));
    } catch (BusinessException e2) {
      ExceptionUtils.wrapException(e2);
    }

    if ((paramInfoMap.get("isAssgin") == null) || ((paramInfoMap.get("isAssgin") != null) && (!((Boolean)paramInfoMap.get("isAssgin")).booleanValue())))
    {
      JSONObject assginre = new AssginUtil().getAssginResult(note);

      if (assginre != null)
        return assginre;
    }
    Vector tSelectInfos;
    Iterator iterator;
    if ((paramInfoMap.get("isAssgin") != null) && (((Boolean)paramInfoMap.get("isAssgin")).booleanValue()))
    {
      Vector assignInfos;
      Iterator localIterator1;
      Iterator localIterator2;
      if ((note.getWorkflow_type().intValue() == 2) || (note.getWorkflow_type().intValue() == 3) || (note.getWorkflow_type().intValue() == 6))
      {
        if (paramInfoMap.get("assgininfo") != null) {
          JSONObject jsondata = (JSONObject)paramInfoMap.get("assgininfo");

          assignInfos = note.getTaskInfo().getAssignableInfos();

          List jsonArayList = (List)jsondata.get("content");

          JSONArray jsonAray = new JSONArray(jsonArayList);

          for (localIterator1 = jsonAray.iterator(); localIterator1.hasNext(); ) { Object obj = localIterator1.next();
            JSONObject json = new JSONObject((Map)obj);

            if ((assignInfos != null) && (assignInfos.size() > 0))
            {
              for (int i = 0; i < assignInfos.size(); i++)
              {
                AssignableInfo assignInfo = (AssignableInfo)assignInfos.get(i);
                String activitydefid = json.getString("activitydefid");
                if (activitydefid.equals(assignInfo.getActivityDefId())) {
                  Vector userPKs = assignInfo.getAssignedOperatorPKs();
                  userPKs.clear();
                  JSONArray listStr = new JSONArray((List)json.get("uservos"));
                  for (localIterator2 = listStr.iterator(); localIterator2.hasNext(); ) { Object jsonobj = localIterator2.next();
                    JSONObject json11 = new JSONObject((Map)jsonobj);

                    userPKs.addElement(json11.getString("userpk"));
                  }
                  break;
                }

              }

            }

          }

        }

      }
      else if (paramInfoMap.get("assgininfo") != null) {
        JSONObject jsondata = (JSONObject)paramInfoMap.get("assgininfo");
        if (jsondata.get("muplityWithOutAssgin") != null)
        {
          if (jsondata.getBooleanValue("muplityWithOutAssgin"))
          {
            assignInfos = note.getTaskInfo().getAssignableInfos();

            List jsonArayList = (List)jsondata.get("content");

            JSONArray jsonAray = new JSONArray(jsonArayList);

            for (localIterator1 = jsonAray.iterator(); localIterator1.hasNext(); ) { Object obj = localIterator1.next();
              JSONObject json = new JSONObject((Map)obj);

              if ((assignInfos != null) && (assignInfos.size() > 0))
              {
                for (int i = 0; i < assignInfos.size(); i++)
                {
                  AssignableInfo assignInfo = (AssignableInfo)assignInfos.get(i);
                  String activitydefid = json.getString("selectpath");
                  if (assignInfo.getActivityDefId().equals(activitydefid)) {
                    Vector userPKs = assignInfo.getAssignedOperatorPKs();
                    userPKs.clear();
                    JSONArray listStr = new JSONArray((List)json.get("assginUsers"));
                    for (localIterator2 = listStr.iterator(); localIterator2.hasNext(); ) { Object jsonobj = localIterator2.next();
                      JSONObject json11 = new JSONObject((Map)jsonobj);

                      userPKs.addElement(json11.getString("pk"));
                    }
                    break;
                  }
                }

              }

            }

          }
          else
          {
            assignInfos = note.getTaskInfo().getAssignableInfos();
            tSelectInfos = note.getTaskInfo().getTransitionSelectableInfos();

            JSONObject jsondata11 = (JSONObject)paramInfoMap.get("assgininfo");
            List jsonArayList = (List)jsondata11.get("content");

            JSONArray jsonAray = new JSONArray(jsonArayList);

            for (iterator = jsonAray.iterator(); iterator.hasNext(); ) {
              JSONObject jsonObject = (JSONObject)iterator.next();

              if (jsonObject.getBoolean("isChoice").booleanValue())
              {
                AssignableInfo _assignInfos;
                for (int j = 0; j < assignInfos.size(); j++) {
                  _assignInfos = (AssignableInfo)assignInfos.get(j);

                  if (_assignInfos.getAfferentTransitions().containsKey(jsonObject.getString("selectpath"))) {
                    List userJsonList = (List)jsonObject.get("assginUsers");
                    if ((userJsonList != null) && (userJsonList.size() > 0)) {
                      JSONArray userListJson = new JSONArray(userJsonList);
                      _assignInfos.getAssignedOperatorPKs().clear();
                      for (localIterator2 = userListJson.iterator(); localIterator2.hasNext(); ) { Object obj = localIterator2.next();
                        JSONObject jsonu = (JSONObject)obj;
                        _assignInfos.getAssignedOperatorPKs().add(jsonu.getString("pk"));
                      }
                    }
                  }

                }

              }

            }

            for (iterator = jsonAray.iterator(); iterator.hasNext(); )
            {
              JSONObject jsonObject = (JSONObject)iterator.next();
              if (jsonObject.getBoolean("isChoice").booleanValue()) {
                for (int i = 0; i < tSelectInfos.size(); i++) {
                  TransitionSelectableInfo trans = (TransitionSelectableInfo)tSelectInfos.get(i);
                  if (jsonObject.getString("selectpath").equals(trans.getTransitionDefId()))
                    trans.setChoiced(true);
                  else {
                    trans.setChoiced(false);
                  }

                }

              }

            }

          }

        }

      }

    }

    String billtype2RegistClass = "";
    IApproveBusiHandler approveBusiAction = null;
    try
    {
      IBilltypeService billTypeServiceImpl = (IBilltypeService)ServiceLocator.find(IBilltypeService.class);

      String billType = nccWorkFlowService.getRealBilltype(billTypeOrTransType);

      if ((billTypeServiceImpl.queryBilltypeVOsFromCache(billType, 25) != null) && (billTypeServiceImpl.queryBilltypeVOsFromCache(billType, 25).size() > 0))
      {
        billtype2RegistClass = ((Billtype2VO)billTypeServiceImpl.queryBilltypeVOsFromCache(billType, 25).get(0)).getClassname();
      }

      if ((!"".equals(billtype2RegistClass)) && (billtype2RegistClass != null))
      {
        Class clazz = Class.forName(billtype2RegistClass);

        if (AbstractApproveBusiHandlerImpl.class.isAssignableFrom(clazz))
        {
          AbstractApproveBusiHandlerImpl approveBusiClass = (AbstractApproveBusiHandlerImpl)clazz.newInstance();

          InteractiveExceptionContext context = new InteractiveExceptionContext();
          context.setBilltype(billTypeOrTransType);
          context.setArgsList(note.getApplicationArgs());
          context.setBillId(billId);
          if ((StringUtils.isNotEmpty(skipcodes)) && (!"[]".equals(skipcodes)))
            context.setSkipcodes(String.valueOf(skipcodes).substring(1, skipcodes.length() - 1).split(","));
          else {
            context.setSkipcodes(null);
          }
          context.setBillVos(new AggregatedValueObject[] { billvo });

          if ((approveBusiClass.checkBeforeIsRunMethodParam(context) != null) && ("am".equals(approveBusiClass.checkBeforeIsRunMethodParam(context).getCode())))
          {
            bean = approveBusiClass.checkBeforeApproveWithArgList(context);
            if ((bean != null) && (StringUtils.isNotEmpty(bean.getCode())) && (!"200".equals(bean.getCode()))) {
              return bean;
            }
          }

          bean = approveBusiClass.checkBeforeApprove(billTypeOrTransType, billId, billvo);
          if ((bean != null) && (StringUtils.isNotEmpty(bean.getCode())) && (!"200".equals(bean.getCode())))
            return bean;
        }
        else
        {
          approveBusiAction = (IApproveBusiHandler)clazz.newInstance();

          bean = approveBusiAction.checkBeforeApprove(billTypeOrTransType, billId, billvo);
          if ((bean != null) && (StringUtils.isNotEmpty(bean.getCode())) && (!"200".equals(bean.getCode()))) {
            return bean;
          }
        }
      }
    }
    catch (Exception e1)
    {
      ExceptionUtils.wrapException(e1);
      return NCCFlowUtils.exchangeDataMsg(bean, "500", 
        NCLangRes4VoTransl.getNCLangRes().getStrByID("0020nccloud_0", "0ncc0020001-0431") + e1.getMessage());
    }

    note.setApproveresult(UFBoolean.TRUE.toString());
    if (StringUtils.isEmpty(check_note)) {
      check_note = NCCFlowUtils.getIndividualCheckNote(PfChecknoteEnum.PASS);
      note.setChecknote(check_note);
    } else {
      note.setChecknote(check_note);
    }

    NCCFlowUtils.handleAttrfiles(json2, note);

    String result = ApproveWorkitemAssistor.sign(note, 
      getCheckNotePulsApproveResult(check_note, PfChecknoteEnum.PASS));

    if (StringUtils.isEmpty(result)) {
      try
      {
        if ((WorkflowTypeEnum.fromIntValue(note.getWorkflow_type().intValue()).equals(WorkflowTypeEnum.Approveflow)) || (WorkflowTypeEnum.fromIntValue(note.getWorkflow_type().intValue()).equals(WorkflowTypeEnum.SubApproveflow)))
        {
          bean = ApproveOrSignalActionUtil.processAction("APPROVE", billTypeOrTransType, note, billvo, paraMapList, null, billtype2RegistClass);
        }
        else
        {
          bean = ApproveOrSignalActionUtil.processAction("SIGNAL", billTypeOrTransType, note, billvo, paraMapList, null, billtype2RegistClass);
        }

      }
      catch (BusinessException e)
      {
        ExceptionUtils.wrapException(e);
      }
    }

    return (JSONObject)JSON.toJSON(bean);
  }

  public String getCheckNotePulsApproveResult(String checknote, PfChecknoteEnum check)
  {
    if (check == PfChecknoteEnum.PASS)
    {
      checknote = checknote + UFBoolean.valueOf(true).toString();
    } else if (check == PfChecknoteEnum.NOPASS)
    {
      checknote = checknote + UFBoolean.valueOf(false).toString();
    } else if (check == PfChecknoteEnum.REJECT)
    {
      checknote = checknote + "R";
    }
    return checknote;
  }
}