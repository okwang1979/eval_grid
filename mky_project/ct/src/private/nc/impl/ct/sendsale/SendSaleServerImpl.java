package nc.impl.ct.sendsale;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.ct.sendsale.ISendSaleServer;
import nc.itf.uif.pub.IUifService;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.vo.arap.receivable.AggReceivableBillVO;
import nc.vo.arap.receivable.ReceivableBillVO;
import nc.vo.bd.cust.CustomerVO;
import nc.vo.bd.defdoc.DefdocVO;
import nc.vo.bd.psn.PsndocVO;
import nc.vo.bd.supplier.SupplierVO;
import nc.vo.ct.entity.CtAbstractPayTermVO;
import nc.vo.ct.purdaily.entity.AggCtPuVO;
import nc.vo.ct.purdaily.entity.CtPaymentVO;
import nc.vo.ct.purdaily.entity.CtPuVO;
import nc.vo.ct.purdaily.entity.PayPlanVO;
import nc.vo.ct.saledaily.entity.AggCtSaleVO;
import nc.vo.ct.saledaily.entity.CtSaleFileJsonVO;
import nc.vo.ct.saledaily.entity.CtSaleJsonVO;
import nc.vo.ct.saledaily.entity.CtSaleVO;
import nc.vo.ct.saledaily.entity.JsonComeInfo;
import nc.vo.ct.saledaily.entity.JsonReceivableVO;
import nc.vo.ct.saledaily.entity.PaymentFeedback;
import nc.vo.ct.saledaily.entity.PaymentPlan;
import nc.vo.ct.saledaily.entity.PaymentPlanAndFeedbackInfo;
import nc.vo.ct.saledaily.entity.RecvPlanVO;
import nc.vo.org.DeptVO;
import nc.vo.org.OrgVO;
import nc.vo.pmpub.project.ProjectHeadVO;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.filesystem.NCFileVO;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nccloud.pubitf.platform.attachment.IAttachmentService;
//import nccloud.pubimpl.platform.attachment.GetFilePathService;
import nccloud.web.platform.attachment.vo.AttachPathVo;

public class SendSaleServerImpl implements ISendSaleServer {
	
//	private static String  TYPE_FILE_ZBTZS = GetFilePathService.FILT_TYPE_ZBTZS;//中标通知书
//	
//	private static String TYPE_FILE_HTZW=GetFilePathService.FILT_TYPE_HTXZW ;//合同正文
//	
////	private static String TYPE_FILE_LXYJ=GetFilePathService.FILT_TYPE_QT;//立项依据
//	
//	private static String TYPE_FILE_HTSPD=GetFilePathService.FILT_TYPE_HTSPD;//合同审批单
//	
//	private static String TYPE_FILE_WFSQWTS =GetFilePathService.FILT_TYPE_WFSQWTS;//我方授权委托书
//	private static String TYPE_FILE_DFSQWTS = GetFilePathService.FILT_TYPE_DFSQWTS;//对方授权委托书
//	
//	private static String TYPE_FILE_HTQSWB = GetFilePathService.FILT_TYPE_HTQSWB;//合同签署文本
//	
	

	
	
	public  static final String TYPE_FILE_HTZW ="zw" ;	
	public  static final String TYPE_FILE_HTSPD ="spattach" ;
	public  static final String TYPE_FILE_WFSQWTS ="wsattach" ;
	public  static final String TYPE_FILE_DFSQWTS ="dsattach" ;
	public  static final String TYPE_FILE_HTQSWB ="qsattach" ;
	public  static final String TYPE_FILE_ZBTZS ="zbattach" ;
	public  static final String FILT_TYPE_QT ="otherattach" ;
	
	
	public  static final String FILT_TYPE_HTXZW ="zw" ;	
	public  static final String FILT_TYPE_HTSPD ="spattach" ;
	public  static final String FILT_TYPE_WFSQWTS ="wsattach" ;
	public  static final String FILT_TYPE_DFSQWTS ="dsattach" ;
	public  static final String FILT_TYPE_HTQSWB ="qsattach" ;
	public  static final String FILT_TYPE_ZBTZS ="zbattach" ;

	@Override
	public CtSaleJsonVO pushSaleToService(AggCtSaleVO saleVO) {

		try {
			CtSaleVO hvo = saleVO.getParentVO();

//			SaleParamCheckUtils.doValidator(hvo);
			

		 
			List<AttachPathVo> allFiles =  this.getFilePath(hvo.getPk_ct_sale());
			
			 
			
			CtSaleJsonVO rtn = new CtSaleJsonVO();

			IUifService service = NCLocator.getInstance().lookup(IUifService.class);
			
			OrgVO queryOrg = (OrgVO) service.queryByPrimaryKey(OrgVO.class, hvo.getPk_org());
			ProjectHeadVO project = null;
			if(hvo.getCprojectid()!=null) {
				project = (ProjectHeadVO)service.queryByPrimaryKey(ProjectHeadVO.class, hvo.getCprojectid());
			}
			
			

			// 1~11
			rtn.setContractUniqueId("114" + "_" + hvo.getPk_ct_sale());
			
	 
			if(hvo.getVdef1()!=null) {
				DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef1());
 
					rtn.setContractType(defVo2.getCode());
			 
				
		 
			} 

			rtn.setContractSubject(hvo.getVdef2());
			rtn.setContractName(hvo.getCtname());
			rtn.setContractSelfCode(hvo.getVbillcode());
			if(project!=null) {
				rtn.setRelatedProjectName(project.getProject_name());
				rtn.setRelatedProjectCode(project.getProject_code());
			}
 
			rtn.setBidFile(getFileType(hvo,TYPE_FILE_ZBTZS, allFiles));
			rtn.setContractAmount(getStringValue(hvo.getNtotalorigmny()));
			if(hvo.getVdef3()!=null) {
				DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef3());
				rtn.setValuationMode(getInteValue( defVo2.getCode()));
			}
			
			
			
			
			
		 
			
			//11~20
			if(hvo.getCorigcurrencyid()!=null) {
				//币种要编码还是名称.
				nc.vo.bd.currtype.CurrtypeVO    curr = (nc.vo.bd.currtype.CurrtypeVO )service.queryByPrimaryKey(nc.vo.bd.currtype.CurrtypeVO.class, hvo.getCorigcurrencyid());
				rtn.setCurrencyName(curr.getName());
			}else {
				rtn.setCurrencyName("人民币");
			}
			
			rtn.setExchangeRate(getStringValue(hvo.getNexchangerate()));
			rtn.setAmountExplain(hvo.getVdef4());
			rtn.setPaymentDirection(2);
			rtn.setBuyMethod(6); 
		 	//文档需要整数,但是支持多选这里改成String每个用|分隔表示多选,后续可能改成数组或在List
			if(hvo.getVdef5()!=null) {
				DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef5());
				rtn.setPaymentType( defVo2.getCode());
			}else {
				rtn.setPaymentType("0");//这个有矛盾需要处理
			}
		
			
			rtn.setPaymentMethod(hvo.getVdef6());
			rtn.setIsAdvancePayment(null);
			rtn.setSigningSubject("煤科院节能技术有限公司");
			rtn.setSigningSubjectCode("114");
			if(hvo.getPersonnelid()!=null) {
				PsndocVO person = (PsndocVO)service.queryByPrimaryKey(PsndocVO.class, hvo.getPersonnelid());
				if(person!=null) {
//					rtn.setCreatorAccount(person.getCode());
					rtn.setCreatorAccount("105000380");
					rtn.setCreatorName(person.getName());
				
				
				
				}
			}
		
			
//			
//			
//			
//			
			//21~30
			if( hvo.getDepid()!=null) {
				DeptVO dept = (DeptVO)service.queryByPrimaryKey(DeptVO.class, hvo.getDepid()) ;
				if(dept!=null) {
					rtn.setCreatorDeptCode(dept.getCode());
					rtn.setCreatorDeptName(dept.getName());
				}
			}
	

			rtn.setPerformAddress(hvo.getVdef7());
			rtn.setSignAddress(hvo.getVdef8());
			
			if( hvo.getVdef9()!=null) {
				DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef9());
				rtn.setContractPeriod(getInteValue( defVo2.getCode()));
			}
		
			
			rtn.setPerformPeriod(getDataByStr(hvo.getVdef10()));
			rtn.setPeriodExplain(hvo.getVdef11());
			rtn.setContractContent(null);
			rtn.setContractText(getFileType(hvo,TYPE_FILE_HTZW, allFiles));
		
//			
//			
//			
//			//31~40
			rtn.setContractGist(null);
			rtn.setContractApprovalForm(getFileType(hvo, TYPE_FILE_HTSPD, allFiles));
			rtn.setContractAttachment(null);
			CustomerVO cust = (CustomerVO)service.queryByPrimaryKey(CustomerVO .class,hvo.getPk_customer()) ;
			if(cust!=null) {
				if(cust.getTaxpayerid()==null) {
					rtn.setOppositeUniqueId("testData");
					rtn.setOppositeCode("testData");
				}else {
					rtn.setOppositeUniqueId(cust.getTaxpayerid());
					rtn.setOppositeCode(cust.getTaxpayerid());
				}

				rtn.setOppositeName(cust.getName());
			}else {
				rtn.setOppositeUniqueId("testData");
				rtn.setOppositeCode("testData");
				rtn.setOppositeName("testData");
			}
			rtn.setOppositeRelName(hvo.getVdef12());
			rtn.setBankOfDeposit(null);
			rtn.setBankAccount(null);
			rtn.setBankAccountName(null);
			if(cust!=null) {
				//是否关联项为必须录入项目,客商是否录入.
				if(cust.getDef2()==null) {
					rtn.setIsRelatedParty(0);
				}else {
					rtn.setIsRelatedParty(getBooleanInt(cust.getDef2()));
				}
				if(cust.getDef3()!=null) {
					DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, cust.getDef3());
					rtn.setRpType(getInteValue(defVo2.getCode()));
				}else {
					rtn.setRpType(0);
				}
				if(cust.getDef4()!=null) {
					DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, cust.getDef4());
					//这个必填需要int型
					if(getInteValue(defVo2.getCode())!=null) {
						rtn.setIsRelatedDeal(getInteValue(defVo2.getCode()));
					}else {
						rtn.setIsRelatedDeal(0);
					}
					
				}else {
					rtn.setIsRelatedDeal(0);
				}
			 
				if(cust.getDef5()!=null) {
					DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, cust.getDef5());
					//code必须在1~17范围内
					if(defVo2!=null) {
						rtn.setDealType(defVo2.getCode());
					}
					
					else {
						rtn.setDealType("17");
					}
					
					
				}else {
					//cust,def5,必须录入
					rtn.setDealType("17");
				}
				
			}
			
//			
			//41~50
	 
		
		 
			rtn.setIsIntertemporal(getBooleanInt(hvo.getVdef13()));
			rtn.setIntertemporalYear(hvo.getVdef14());
		 
			rtn.setEstimateAmount(getDouble(hvo.getVdef15(),2));
			 
			rtn.setIsImportantRelatedDeal(getBooleanInt(hvo.getVdef16()));
		 
			rtn.setIsNeedPerfApprove(getBooleanInt(hvo.getVdef17()));
			 
			rtn.setSealTime(null);
			if(hvo.getVdef18()!=null) {
				DefdocVO defVo1 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef18());
				if(defVo1!=null) {
					rtn.setSealType(defVo1.getCode());//是否需要转换用印类型
				}
			}
			
			
//			
//			
//			//51~60
			rtn.setSignNum(null);
			rtn.setSignTime(getDataTime(hvo.getSubscribedate().toDate()));
			rtn.setOurIsAuth(getBooleanInt(hvo.getVdef19()));
			if(hvo.getVdef20()!=null) {
				DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef20());
				if(defVo2!=null) {
				 rtn.setAuthType(getInteValue( defVo2.getCode()));
				}
			}
	
			rtn.setOwnAuth(getFileType(hvo, TYPE_FILE_WFSQWTS, allFiles));
			rtn.setOurName(null);
			rtn.setOpptName(null);
			rtn.setOpptIsAuth(getBooleanInt(hvo.getVdef21()));
			rtn.setOpptAuth(getFileType(hvo, TYPE_FILE_DFSQWTS, allFiles));
			rtn.setContractScanFile(getFileType(hvo, TYPE_FILE_HTQSWB, allFiles));
			

			return rtn;
		} catch (Exception ex) {
			throw new BusinessRuntimeException(ex.getMessage(),ex);
		}

	}
	
//	/***
//	 * tuoxingx 合同收款协议计划取值逻辑
//	 * 
//	 */
//	@Override
//	public PaymentPlanAndFeedbackInfo pushBillToService(AggCtSaleVO saleVO) {
//
//		try {
//			SuperVO[][] allChildren = saleVO.getAllChildren();
//			RecvPlanVO[] vos = (RecvPlanVO[]) allChildren[6];
//			CtAbstractPayTermVO[] CtAbstractPayTermVOs = (nc.vo.ct.entity.CtAbstractPayTermVO[]) allChildren[7];
//			PaymentPlanAndFeedbackInfo billJsonVo = new PaymentPlanAndFeedbackInfo();
//			billJsonVo.setContractUniqueId("");
//			billJsonVo.setSourceInfo("PLAN");
//			List<PaymentPlan> planList = new ArrayList<PaymentPlan>();
//			List<PaymentFeedback> feedbackList = new ArrayList<PaymentFeedback>();
//			for (CtAbstractPayTermVO ctAbstractPayTermVO : CtAbstractPayTermVOs) {   
//				PaymentPlan paymentPlan = new PaymentPlan();
//            	paymentPlan.setPlanId(vos[0].getPk_ct_recvplan());
//            	paymentPlan.setSortNum(null);
//            	paymentPlan.setPerformItem("");//履行事项
//            	paymentPlan.setPayDate(getDataTime(vos[0].getDbegindate().toDate()));
//            	paymentPlan.setReminderDay(null);
//            	paymentPlan.setPayAmount(ctAbstractPayTermVO.getNplanrecmny());
//            	planList.add(paymentPlan);
//			}
//			billJsonVo.setPlanList(planList);
////			PaymentFeedback feedback =  new PaymentFeedback();
////			feedback.setPlanId("");
////			feedback.setSortNum(2);
////			feedback.setIsNormal(2);
////			feedback.setRealPayAmount("");
////			feedback.setAbnormalReason("");
////			feedback.setRealPayAmount("");
////			feedback.setFeedBackId("");
////			feedbackList.add(feedback);
//			return billJsonVo;
//		} catch (Exception ex) {
//			return null;
//		}
//
//	}
//	/***
//	 * tuoxingx 合同付款协议计划取值逻辑
//	 * 
//	 */
//	@Override
//	public PaymentPlanAndFeedbackInfo pushPayBillToService(AggCtPuVO purVO) {
//
//		try {
//			SuperVO[][] allChildren = purVO.getAllChildren();
//			PayPlanVO[] vos = (PayPlanVO[]) allChildren[6]; 
//			String oo = "";
//			CtPaymentVO[] CtAbstractPayTermVOs = (CtPaymentVO[]) allChildren[7];
//			PaymentPlanAndFeedbackInfo billJsonVo = new PaymentPlanAndFeedbackInfo();
//			billJsonVo.setContractUniqueId("");
//			billJsonVo.setSourceInfo("PLAN");
//			List<PaymentPlan> planList = new ArrayList<PaymentPlan>();
//			List<PaymentFeedback> feedbackList = new ArrayList<PaymentFeedback>();
//			for (CtPaymentVO ctAbstractPayTermVO : CtAbstractPayTermVOs) {   
//				PaymentPlan paymentPlan = new PaymentPlan();
//            	paymentPlan.setPlanId(vos[0].getPk_ct_payplan());
//            	paymentPlan.setSortNum(null);
//            	paymentPlan.setPerformItem("");//履行事项
//            	paymentPlan.setPayDate(getDataTime(vos[0].getDbegindate().toDate()));
//            	paymentPlan.setReminderDay(null);
//            	paymentPlan.setPayAmount(null);
//            	planList.add(paymentPlan);
//			}
//			billJsonVo.setPlanList(planList);
////			PaymentFeedback feedback =  new PaymentFeedback();
////			feedback.setPlanId("");
////			feedback.setSortNum(2);
////			feedback.setIsNormal(2);
////			feedback.setRealPayAmount("");
////			feedback.setAbnormalReason("");
////			feedback.setRealPayAmount("");
////			feedback.setFeedBackId("");
////			feedbackList.add(feedback);
//			return billJsonVo;
//		} catch (Exception ex) {
//			return null;
//		}
//
//	}
	
	
	
	
	
	
	
	
	
	
	
	
	/***
	 * tuoxingx 合同收款协议计划取值逻辑
	 * 
	 */
	@Override
	public PaymentPlanAndFeedbackInfo pushBillToService(AggCtSaleVO saleVO) {

		try {
			SuperVO[][] allChildren = saleVO.getAllChildren();
			RecvPlanVO[] vos = (RecvPlanVO[]) allChildren[6];
			CtAbstractPayTermVO[] CtAbstractPayTermVOs = (nc.vo.ct.entity.CtAbstractPayTermVO[]) allChildren[7];
			PaymentPlanAndFeedbackInfo billJsonVo = new PaymentPlanAndFeedbackInfo();
			billJsonVo.setContractUniqueId("");
			billJsonVo.setSourceInfo("PLAN");
			List<PaymentPlan> planList = new ArrayList<PaymentPlan>();
			List<PaymentFeedback> feedbackList = new ArrayList<PaymentFeedback>();
			for (CtAbstractPayTermVO ctAbstractPayTermVO : CtAbstractPayTermVOs) {   
				PaymentPlan paymentPlan = new PaymentPlan();
            	paymentPlan.setPlanId(vos[0].getPk_ct_recvplan());
            	paymentPlan.setSortNum(null);
            	paymentPlan.setPerformItem("");//履行事项
            	paymentPlan.setPayDate(getDataTime(vos[0].getDbegindate().toDate()));
            	paymentPlan.setReminderDay(null);
            	paymentPlan.setPayAmount(ctAbstractPayTermVO.getNplanrecmny());
            	planList.add(paymentPlan);
			}
			billJsonVo.setPlanList(planList);
//			PaymentFeedback feedback =  new PaymentFeedback();
//			feedback.setPlanId("");
//			feedback.setSortNum(2);
//			feedback.setIsNormal(2);
//			feedback.setRealPayAmount("");
//			feedback.setAbnormalReason("");
//			feedback.setRealPayAmount("");
//			feedback.setFeedBackId("");
//			feedbackList.add(feedback);
			return billJsonVo;
		} catch (Exception ex) {
			return null;
		}

	}
	/***
	 * tuoxingx 合同付款协议计划取值逻辑
	 * 
	 */
	@Override
	public PaymentPlanAndFeedbackInfo pushPayBillToService(AggCtPuVO purVO) {

		try {
			SuperVO[][] allChildren = purVO.getAllChildren();
			PayPlanVO[] vos = (PayPlanVO[]) allChildren[6]; 
			String oo = "";
			CtPaymentVO[] CtAbstractPayTermVOs = (CtPaymentVO[]) allChildren[7];
			PaymentPlanAndFeedbackInfo billJsonVo = new PaymentPlanAndFeedbackInfo();
			billJsonVo.setContractUniqueId("");
			billJsonVo.setSourceInfo("PLAN");
			List<PaymentPlan> planList = new ArrayList<PaymentPlan>();
			List<PaymentFeedback> feedbackList = new ArrayList<PaymentFeedback>();
			for (CtPaymentVO ctAbstractPayTermVO : CtAbstractPayTermVOs) {   
				PaymentPlan paymentPlan = new PaymentPlan();
            	paymentPlan.setPlanId(vos[0].getPk_ct_payplan());
            	paymentPlan.setSortNum(null);
            	paymentPlan.setPerformItem("");//履行事项
            	paymentPlan.setPayDate(getDataTime(vos[0].getDbegindate().toDate()));
            	paymentPlan.setReminderDay(null);
            	paymentPlan.setPayAmount(null);
            	planList.add(paymentPlan);
			}
			billJsonVo.setPlanList(planList);
//			PaymentFeedback feedback =  new PaymentFeedback();
//			feedback.setPlanId("");
//			feedback.setSortNum(2);
//			feedback.setIsNormal(2);
//			feedback.setRealPayAmount("");
//			feedback.setAbnormalReason("");
//			feedback.setRealPayAmount("");
//			feedback.setFeedBackId("");
//			feedbackList.add(feedback);
			return billJsonVo;
		} catch (Exception ex) {
			return null;
		}

	}
	
	
	
	
	public UFDouble getDouble(Object value,int smail) {
		if(value==null) {
			return null;
		}
		 UFDouble doubleValue = new UFDouble(value.toString(),smail);
		 return doubleValue;
	}
	
	public Integer getBooleanInt(Object value) {
		if(value==null) {
			return null;
		}
		if(value instanceof Boolean) {
			if((Boolean)value) {
				return 1;
			}else {
				return 0;
			}
		}
		if(value instanceof Number) {
			if (1==((Number)value).intValue()) {
				return 1;
			}else{
				return 0;
			}
		}
 
		if("Y".equals(value.toString().toUpperCase())||"T".equals(value.toString().toUpperCase())||"1".equals(value.toString().toUpperCase())||"TRUE".equals(value.toString().toUpperCase())) {
			return 1;
		}else {
			return 0;
		}
	}
	
	
	public List<CtSaleFileJsonVO> getFileType(CtSaleVO hvo,String type,List<AttachPathVo> allFiles){
		
		List<CtSaleFileJsonVO> paths = new ArrayList<CtSaleFileJsonVO>();
		
		int num =1;
		for(AttachPathVo file: allFiles) {
			
			if(type.equals(file.getAttachType())){
				
				StringBuffer sb = new StringBuffer();
				
//				/单位编码/202008/合同编码/zw/zw.doc
				sb.append("/home/document/").append(file.getCompCode()).append("/").append(file.getYearMonthStr()).append("/").append(file.getCtCode()).append("/").append(type).append("/").append(file.getFileName());
				paths.add(new CtSaleFileJsonVO(file.getFileName(), sb.toString(), "",num));
				num++;
			}
		}
		
	
		return paths;
		
	}
	
	public String getDataByStr(String dateStr) {
		UFDateTime date  = new UFDateTime(dateStr);
		return getDataTime(date.getDate().toDate());
		
	}
	
	public String getDataTime(Date date) {
		
		  SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
		return  f.format(date);
//		Formatter format = new dateformat
//		return "";
	}

	private Integer getInteValue(Object value) {
		if (value == null) {
			return null;
		}
		try {
			return Integer.valueOf(value.toString());
		} catch (Exception ex) {
			return null;
		}
	}
	
	private String getStringValue(Object value) {
		if(value==null) {
			return null;
		}
		return String.valueOf(value);
		
	}

	private void validata(@Valid CtSaleJsonVO vo) {

	}

	
	public List<AttachPathVo> getFilePath(String pk_ct) {
		try {
			NCFileVO[] ncfiles = null;
//	   	    pk_ct = "1001A11000000000VTTI";
			IAttachmentService service = NCLocator.getInstance().lookup(IAttachmentService.class);
		 	Map<String, String> map =	service.queryVbillCode(pk_ct);
	   	    ncfiles = service.queryNCFileByBill(pk_ct);

	   	  return getAttachpaths(ncfiles,map,pk_ct);
		} catch (Exception e) {
			throw new BusinessRuntimeException(e.getMessage());
		}
	}
	
	
	private List<AttachPathVo> getPuFilePath(String pk_ct) {
		
		try {
			NCFileVO[] ncfiles = null;
//	   	    pk_ct = "1001A11000000000VTTI";
			IAttachmentService service = NCLocator.getInstance().lookup(IAttachmentService.class);
		 	Map<String, String> map =	service.queryPurdailyMap(pk_ct);
	   	    ncfiles = service.queryNCFileByBill(pk_ct);
//	   	    Map<String, String> map = dao.queryVbillCode(pk_ct);
	      
	        if(map==null||map.isEmpty()) {
	        	return new ArrayList<AttachPathVo>();
	        }
	        if(ncfiles==null||ncfiles.length==0) {
	        	return new ArrayList<AttachPathVo>();
	        }
	        return getAttachpaths(ncfiles,map,pk_ct);
		} catch (Exception e) {
			throw new BusinessRuntimeException(e.getMessage());
		}
		
	}
	private List<AttachPathVo> getAttachpaths(NCFileVO[] ncfiles,Map<String, String> map,String pk ) throws Exception{
		List<AttachPathVo> resultList = new ArrayList<AttachPathVo>();
		  SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        Date parse = sdf.parse(map.get("subscribedate"));
        String yearMonth = sdf.format(parse);
        String ctCode = map.get("vbillcode");
   	    for (int i = 0; i < ncfiles.length; i++) {
			NCFileVO ncFileVO = ncfiles[i];
			String name = ncFileVO.getName();
			if(!pk.equals(name)) {
				AttachPathVo attachPathVo  = new AttachPathVo();
				attachPathVo.setCompCode("kgjn");
				attachPathVo.setYearMonthStr(yearMonth);
				attachPathVo.setCtCode(ctCode);
				String parentpath = ncFileVO.getParentpath();
				String attachType = attachTypeConvert(parentpath);
				attachPathVo.setAttachType(attachType);
				attachPathVo.setFileName(name);
				resultList.add(attachPathVo);
			}
		}
   	    return resultList;
		
	}
	
	/***
	   * 附件类型转换
	 * @param attachType
	 * @return
	 */
	public static String attachTypeConvert(String attachType) {
		if (attachType.contains("合同正文")) {
			attachType = FILT_TYPE_HTXZW;
		}
		if (attachType.contains("合同审批单")) {
			attachType = FILT_TYPE_HTSPD;
		}
		if (attachType.contains("我方授权委托书")) {
			attachType = FILT_TYPE_WFSQWTS;
		}
		if (attachType.contains("对方授权委托书")) {
			attachType = FILT_TYPE_DFSQWTS;
		}
		if (attachType.contains("合同签署文本")) {
			attachType = FILT_TYPE_HTQSWB;
		}
		if (attachType.contains("中标通知书")) {
			attachType = FILT_TYPE_ZBTZS;
		}
		if (attachType.contains("其它")) {
			attachType = FILT_TYPE_QT;
		}  
		return attachType;
	}


	@Override
	public CtSaleJsonVO pushPurdailyToService(AggCtPuVO saleVO) {



		try {
			Logger.init("init");
			CtPuVO hvo = saleVO.getParentVO();
		 
			List<AttachPathVo> allFiles =  this.getPuFilePath(hvo.getPk_ct_pu());
			
			if(allFiles.isEmpty()) {
				throw new BusinessRuntimeException("请上传相关附件！");
			}
			 
			
			CtSaleJsonVO rtn = new CtSaleJsonVO();

			IUifService service = NCLocator.getInstance().lookup(IUifService.class);
			
			OrgVO queryOrg = (OrgVO) service.queryByPrimaryKey(OrgVO.class, hvo.getPk_org());
			ProjectHeadVO project = null;
			if(hvo.getCprojectid()!=null) {
				project = (ProjectHeadVO)service.queryByPrimaryKey(ProjectHeadVO.class, hvo.getCprojectid());
			}
			
			

			// 1~11
			rtn.setContractUniqueId( "114_" + hvo.getPk_ct_pu());
			
	 
			if(hvo.getVdef1()!=null) {
				DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef1());
 
					rtn.setContractType(defVo2.getCode());
			 
				
		 
			} 

			
			
			rtn.setContractSubject(hvo.getVdef2());
			
			
			rtn.setContractName(hvo.getCtname());
			rtn.setContractSelfCode(hvo.getVbillcode());
			if(project!=null) {
				rtn.setRelatedProjectName(project.getProject_name());
				rtn.setRelatedProjectCode(project.getProject_code());
			}
			if(hvo.getVdef22()!=null) {
				DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef22());
	 
				rtn.setBuyMethod(getInteValue( defVo2.getCode()));
			}else {
				rtn.setBuyMethod(6); 
			}
			
// 
			rtn.setBidFile(getFileType(null,TYPE_FILE_ZBTZS, allFiles));
			rtn.setContractAmount(getStringValue(hvo.getNtotalorigmny()));
			if(hvo.getVdef3()!=null) {
				DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef3());
				rtn.setValuationMode(getInteValue( defVo2.getCode()));
			}
			

//			
//			
//			
//			
//			
//		 
//			
//			//11~20
			if(hvo.getCorigcurrencyid()!=null) {
				//币种要编码还是名称.
				nc.vo.bd.currtype.CurrtypeVO    curr = (nc.vo.bd.currtype.CurrtypeVO )service.queryByPrimaryKey(nc.vo.bd.currtype.CurrtypeVO.class, hvo.getCorigcurrencyid());
				rtn.setCurrencyName(curr.getName());
			}else {
				rtn.setCurrencyName("人民币");
			}
//			
			rtn.setExchangeRate(getStringValue(hvo.getNexchangerate()));
			rtn.setAmountExplain(hvo.getVdef4());
			rtn.setPaymentDirection(0);
			if(hvo.getVdef5()!=null) {
				DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef5());
				rtn.setPaymentType( defVo2.getCode());
			}else {
				rtn.setPaymentType("0");//这个有矛盾需要处理
			}


//		
//			
			rtn.setPaymentMethod(hvo.getVdef6());
			
			rtn.setIsAdvancePayment(null);
			rtn.setSigningSubject("煤科院节能技术有限公司");
			rtn.setSigningSubjectCode("114");
			if(hvo.getPersonnelid()!=null) {
				PsndocVO person = (PsndocVO)service.queryByPrimaryKey(PsndocVO.class, hvo.getPersonnelid());
				if(person!=null) {
//					rtn.setCreatorAccount(person.getCode());
					rtn.setCreatorAccount("105000380");
					rtn.setCreatorName(person.getName());
				
				
				
				}
			}
//		
//			
////			
////			
////			
////			
			//21~30
			if( hvo.getDepid()!=null) {
				DeptVO dept = (DeptVO)service.queryByPrimaryKey(DeptVO.class, hvo.getDepid()) ;
				if(dept!=null) {
					rtn.setCreatorDeptCode(dept.getCode());
					rtn.setCreatorDeptName(dept.getName());
				}
			}
//	
//
			rtn.setPerformAddress(hvo.getVdef7());
			rtn.setSignAddress(hvo.getVdef8());
//			
			if( hvo.getVdef9()!=null) {
				DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef9());
				rtn.setContractPeriod(getInteValue( defVo2.getCode()));
			}
		
//			
			rtn.setPerformPeriod(getDataByStr( hvo.getVdef10()));
			rtn.setPeriodExplain(hvo.getVdef11());
			rtn.setContractContent(null);
			rtn.setContractText(getFileType(null,TYPE_FILE_HTZW, allFiles));
//		
////			
////			
////			
//			//31~40
			rtn.setContractGist(null);
			rtn.setContractApprovalForm(getFileType(null, TYPE_FILE_HTSPD, allFiles));
			rtn.setContractAttachment(null);
			SupplierVO cust = (SupplierVO)service.queryByPrimaryKey(SupplierVO .class,hvo.getCvendorid ()) ;
			if(cust!=null) {
				if(cust.getTaxpayerid()==null) {
					rtn.setOppositeUniqueId("testData");
					rtn.setOppositeCode("testData");
				}else {
					rtn.setOppositeUniqueId(cust.getTaxpayerid());
					rtn.setOppositeCode(cust.getTaxpayerid());
				}

				rtn.setOppositeName(cust.getName());
			}else {
				rtn.setOppositeUniqueId("testData");
				rtn.setOppositeCode("testData");
				rtn.setOppositeName("testData");
			}
			rtn.setOppositeRelName(hvo.getVdef12());
			rtn.setBankOfDeposit(null);
			rtn.setBankAccount(null);
			rtn.setBankAccountName(null);
			if(cust!=null) {
				//是否关联项为必须录入项目,客商是否录入.
				if(cust.getDef2()==null) {
					rtn.setIsRelatedParty(0);
				}else {
					rtn.setIsRelatedParty(getBooleanInt(cust.getDef2()));
				}
				if(cust.getDef3()!=null) {
					DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, cust.getDef3());
					rtn.setRpType(getInteValue(defVo2.getCode()));
				}else {
					rtn.setRpType(0);
				}
				if(cust.getDef4()!=null) {
					DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, cust.getDef4());
					//这个必填需要int型
					if(getInteValue(defVo2.getCode())!=null) {
						rtn.setIsRelatedDeal(getInteValue(defVo2.getCode()));
					}else {
						rtn.setIsRelatedDeal(0);
					}
					
				}else {
					rtn.setIsRelatedDeal(0);
				}
			 
				if(cust.getDef5()!=null) {
					DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, cust.getDef5());
					//code必须在1~17范围内
					if(defVo2!=null) {
						rtn.setDealType(defVo2.getCode());
					}
					
					else {
						rtn.setDealType("17");
					}
					
					
				}else {
					//cust,def5,必须录入
					rtn.setDealType("17");
				}
				
			}
//			
////			
//			//41~50
//	 
//		
//		 
			rtn.setIsIntertemporal(getBooleanInt(hvo.getVdef13()));
			rtn.setIntertemporalYear(hvo.getVdef14());
		 
			rtn.setEstimateAmount(getDouble(hvo.getVdef15(),2));
//			 
			rtn.setIsImportantRelatedDeal(getBooleanInt(hvo.getVdef16()));
//		 
			rtn.setIsNeedPerfApprove(getBooleanInt(hvo.getVdef17()));
//			 
			rtn.setSealTime(null);
			if(hvo.getVdef18()!=null) {
				DefdocVO defVo1 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef18());
				if(defVo1!=null) {
					rtn.setSealType(defVo1.getCode());//是否需要转换用印类型
				}
			}
//			
//			
////			
////			
////			//51~60
			rtn.setSignNum(null);
			rtn.setSignTime(getDataTime(hvo.getSubscribedate().toDate()));
			rtn.setOurIsAuth(getBooleanInt(hvo.getVdef19()));
			if(hvo.getVdef20()!=null) {
				DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef20());
				if(defVo2!=null) {
				 rtn.setAuthType(getInteValue( defVo2.getCode()));
				}
			}
//	
			rtn.setOwnAuth(getFileType(null, TYPE_FILE_WFSQWTS, allFiles));
			rtn.setOurName(null);
			rtn.setOpptName(null);
			rtn.setOpptIsAuth(getBooleanInt(hvo.getVdef21()));
			rtn.setOpptAuth(getFileType(null, TYPE_FILE_DFSQWTS, allFiles));
			rtn.setContractScanFile(getFileType(null, TYPE_FILE_HTQSWB, allFiles));
//			
//
//			return rtn;
//		} catch (Exception ex) {
//			throw new BusinessRuntimeException(ex.getMessage(),ex);
//		}
			return rtn;

		}catch(Exception ex) {
			Logger.error(ex);
			throw new BusinessRuntimeException(ex.getMessage(),ex);
			
		}finally {
			Logger.init();
		}
		 
	}

	@Override
	public JsonReceivableVO pusReceivable(AggReceivableBillVO billVo) {
		
		JsonReceivableVO rtn = new JsonReceivableVO();
		ReceivableBillVO vo = billVo.getHeadVO();
		try {
			

			
			rtn.setContractUniqueId("114_"+vo.getDef1());
			JsonComeInfo info = new JsonComeInfo();
			info.setIncomeAmount(vo.getLocal_money());
			info.setCurrentPeriodAmount(new UFDouble());
			info.setIncomeId("114_"+vo.getPk_recbill() );
			
			BaseDAO dao = new BaseDAO();
			List<Object[]> mess = (List<Object[]>) dao.executeQuery("select filepath from sm_pub_filesystem where filepath like '"+vo.getPk_recbill()+"%'",  new ArrayListProcessor());
			CtSaleVO sale = (CtSaleVO)dao.retrieveByPK(CtSaleVO.class, vo.getDef1());
			
			if(mess!=null&&mess.size()>0) {
				for(int i=0;i<mess.size();i++) {
					Object[]  objs = mess.get(i);
					String path = (String) objs[0];
					String[] paths = path.split("/");
					String name = paths[paths.length-1];
				    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
//	                Date parse = sdf.parse(sale.getSubscribedate()));
	                String yearMonth = sdf.format(sale.getSubscribedate());
					CtSaleFileJsonVO file = new CtSaleFileJsonVO(name, "/home/document/kgjn/"+yearMonth+"/"+sale.getPk_ct_sale()+"/"+vo.getPk_recbill()+"/"+name, "", i+1);
					info.getAssistEvidence().add(file);
				}
			}
			
			
			
			
		  
			 
			rtn.setIncomeTotalAmount(vo.getLocal_money());
			
			return rtn;
		}catch(Exception ex) {
			Logger.init("iufo");
			Logger.error(ex);
			throw new BusinessRuntimeException(ex.getMessage(),ex);
		}finally {
			Logger.init();
		}

		
		
	}

	@Override
	public PaymentPlanAndFeedbackInfo pushBillToService(String pk_ct_sale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PaymentPlanAndFeedbackInfo pushPayBillToService(String pk_pu_sale) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
