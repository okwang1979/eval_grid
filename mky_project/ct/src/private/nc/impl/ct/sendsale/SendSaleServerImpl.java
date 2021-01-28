package nc.impl.ct.sendsale;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.validation.Valid;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.common.RuntimeEnv;
import nc.bs.logging.Logger;
import nc.impl.pubapp.pattern.data.bill.BillQuery;
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
import nc.vo.ct.saledaily.entity.CtSalePayTermVO;
import nc.vo.ct.saledaily.entity.CtSaleVO;
import nc.vo.ct.saledaily.entity.JsonComeInfo;
import nc.vo.ct.saledaily.entity.JsonIntertemporal;
import nc.vo.ct.saledaily.entity.JsonReceivableVO;
import nc.vo.ct.saledaily.entity.PaymentFeedback;
import nc.vo.ct.saledaily.entity.PaymentPlan;
import nc.vo.ct.saledaily.entity.PaymentPlanAndFeedbackInfo;
import nc.vo.ct.saledaily.entity.RecvPlanVO;
import nc.vo.org.DeptVO;
import nc.vo.org.OrgVO;
import nc.vo.pmpub.project.ProjectHeadVO;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.ISuperVO;
import nc.vo.pub.SuperVO;
import nc.vo.pub.billtype.BilltypeVO;
import nc.vo.pub.filesystem.NCFileVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.para.SysInitVO;
import nccloud.pubimpl.platform.attachment.FtpUtil;
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
			
			if(hvo.getVdef2()!=null) {
				DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef2());
 
				if(defVo2!=null) {
					rtn.setContractSubject(defVo2.getCode());
				}

//					rtn.setContractType(defVo2.getCode());
			 
				
		 
			}

//			rtn.setContractSubject(hvo.getVdef2());
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
				if(hvo.getVdef5().contains(",")) {
					String[] pks = hvo.getVdef5().split(",");
					StringBuffer sb = new StringBuffer();
					for(String pk:pks) {
						DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, pk);
						if(defVo2!=null) {
							sb.append( defVo2.getCode()).append("|");
						}else {
							throw new BusinessRuntimeException("付款方式未查询到数据,接口传输失败:"+pk);
						}
						
					
					
					}
					
					rtn.setPaymentType( sb.substring(0, sb.length()-1));
				}else {
					DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef5());
					rtn.setPaymentType( defVo2.getCode());
				}

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
					rtn.setCreatorAccount(person.getCode());
//					rtn.setCreatorAccount("105000380");
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
			
			
			if(hvo.getVdef14()!=null&&hvo.getVdef14().length()>1) {
				String[] years = hvo.getVdef14().split("[|]");
				String[] moneys = new String[0];
				if(hvo.getVdef15()!=null&&hvo.getVdef15().length()>1) {
					 moneys = hvo.getVdef15().split("[|]");
				}
				for(int i=0;i<years.length;i++) {
					JsonIntertemporal intertem = new JsonIntertemporal();
					intertem.setIntertemporalYear(years[i]);
					if(i<moneys.length) {
						intertem.setEstimateAmount(getDouble(moneys[i],2));
					}else {
						intertem.setEstimateAmount(new UFDouble(0,2));
					}
					rtn.addIntertemporal(intertem);
				}
				
//				rtn.setIntertemporalYear(hvo.getVdef14());
//				rtn.setEstimateAmount(getDouble(hvo.getVdef15(),2));
			}
			
			if(hvo.getVdef26()!=null) {
				rtn.setRelatedDealItem(hvo.getVdef26());
			}
//		 
//			rtn.setEstimateAmount(getDouble(hvo.getVdef15(),2));
//			 
//			rtn.setIsImportantRelatedDeal(getBooleanInt(hvo.getVdef16()));
//		 
//			rtn.setIsNeedPerfApprove(getBooleanInt(hvo.getVdef17()));
			 
			
			if(getBooleanInt(hvo.getVdef16())!=null) {
				
				DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef16());
				if(defVo2!=null) {
					rtn.setIsImportantRelatedDeal(getBooleanInt(defVo2.getCode()));
				}else {
					rtn.setIsImportantRelatedDeal(0);
				}
			
			}else {
				rtn.setIsImportantRelatedDeal(0);
			}
 		 
		
			
			
			
			if(getBooleanInt(hvo.getVdef17())!=null) {
				DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef17());
				if(defVo2!=null) {
					rtn.setIsNeedPerfApprove(getBooleanInt(defVo2.getCode()));
				}else {
					rtn.setIsNeedPerfApprove(0);
				}
			
			}else {
				rtn.setIsNeedPerfApprove(0);
			}
 	 
			
			
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
			CtSaleVO parent = (CtSaleVO)saleVO.getParent();
			RecvPlanVO[] vos = (RecvPlanVO[]) allChildren[6];
			CtSalePayTermVO[] CtAbstractPayTermVOs = (CtSalePayTermVO[]) allChildren[7];
			PaymentPlanAndFeedbackInfo billJsonVo = new PaymentPlanAndFeedbackInfo();
			billJsonVo.setContractUniqueId("114" + "_" + parent.getPk_ct_sale());
			billJsonVo.setSourceInfo("PLAN");
			List<PaymentPlan> planList = new ArrayList<PaymentPlan>();
			List<PaymentFeedback> feedbackList = new ArrayList<PaymentFeedback>();
			for (CtSalePayTermVO ctAbstractPayTermVO : CtAbstractPayTermVOs) {   
				PaymentPlan paymentPlan = new PaymentPlan();
				paymentPlan.setPlanId("114_" + ctAbstractPayTermVO.getPk_ct_sale_payterm() + "_" + ctAbstractPayTermVO.getShoworder());
            	paymentPlan.setSortNum(null);
            	if(null != ctAbstractPayTermVO.getVbdef1()) {
            		paymentPlan.setPerformItem(ctAbstractPayTermVO.getVbdef1());
            	}
            	else {
            		paymentPlan.setPerformItem("履行事项");//履行事项
            	}
            	if(null != ctAbstractPayTermVO.getDplaneffectdate()) {
            		paymentPlan.setPayDate(getDataTime(ctAbstractPayTermVO.getDplaneffectdate().toDate()));
            	}
            	else {
            		paymentPlan.setPayDate(getDataTime(new Date()));
            	}
            	paymentPlan.setReminderDay(null);
            	paymentPlan.setPayAmount(getDoubleStr(ctAbstractPayTermVO.getNplanrecmny(),2));
            	planList.add(paymentPlan);
			}
			billJsonVo.setPaymentPlanList(planList);
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
			   CtPuVO parent = (CtPuVO) purVO.getParent();
			   CtBillQueryDao ctSaleBillQueryDao = new CtBillQueryDao();
			   List<PayPlanVO> payPlanVOs = ctSaleBillQueryDao.queryCtPurPayplans(parent.getPk_ct_pu());
			//   PayPlanVO[] payPlanVOs = (PayPlanVO[]) allChildren[6];
			   PaymentPlanAndFeedbackInfo billJsonVo = new PaymentPlanAndFeedbackInfo();
			   billJsonVo.setContractUniqueId("114_" + parent.getPk_ct_pu());
			   billJsonVo.setSourceInfo("PLAN");
			   List<PaymentPlan> planList = new ArrayList<PaymentPlan>();
			//   List<PaymentFeedback> feedbackList = new ArrayList<PaymentFeedback>();
			   for (PayPlanVO ctAbstractPayTermVO : payPlanVOs) {   
			             PaymentPlan paymentPlan = new PaymentPlan();
			             paymentPlan.setPlanId(ctAbstractPayTermVO.getPk_ct_payplan());
			             paymentPlan.setSortNum(null);
			             List<CtPaymentVO> ctPaymentVOs = ctSaleBillQueryDao.queryCtPurPayments(parent.getPk_ct_pu());
			             for (CtPaymentVO ctPaymentVO : ctPaymentVOs) {
			            	 if(ctPaymentVO.getPk_ct_pu_payment().equals(ctAbstractPayTermVO.getPk_paytermch())) {
			            		 paymentPlan.setPerformItem(ctPaymentVO.getVbdef1());
			            	 }
							
						 }
			             if(null != ctAbstractPayTermVO.getDbegindate()) {
			            	 paymentPlan.setPayDate(getDataTime(ctAbstractPayTermVO.getDbegindate().toDate()));
			            	 
			             }
			             else {
			            	 paymentPlan.setPayDate(getDataTime(new Date()));
			             }
			             paymentPlan.setReminderDay(null);
			             paymentPlan.setPayAmount(getDoubleStr(ctAbstractPayTermVO.getNorigmny(),2));
			             planList.add(paymentPlan);
			   }
			   billJsonVo.setPaymentPlanList(planList);
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
		if(dateStr==null) {
			return null;
		}
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
		  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Date parse = sdf.parse(map.get("subscribedate"));
        String yearMonth = sdf.format(parse);
         yearMonth = yearMonth.replace("-", "");
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

			if(hvo.getVdef2()!=null) {
				DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef2());
 
				if(defVo2!=null) {
					rtn.setContractSubject(defVo2.getCode());
				}

//					rtn.setContractType(defVo2.getCode());
			 
				
		 
			}
			
//			rtn.setContractSubject(hvo.getVdef2());
			
			
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
			rtn.setPaymentDirection(1);
			if(hvo.getVdef5()!=null) {
				
				
				
				

				if(hvo.getVdef5().contains(",")) {
					String[] pks = hvo.getVdef5().split(",");
					StringBuffer sb = new StringBuffer();
					for(String pk:pks) {
						DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, pk);
						if(defVo2!=null) {
							sb.append( defVo2.getCode()).append("|");
						}else {
							throw new BusinessRuntimeException("付款方式未查询到数据,接口传输失败:"+pk);
						}
						
						rtn.setPaymentType( sb.substring(0, sb.length()-1));
					
					}
					
					
				}else {
					DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef5());
					rtn.setPaymentType( defVo2.getCode());
				}

			
				
				
//				DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef5());
//				rtn.setPaymentType( defVo2.getCode());
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
					rtn.setCreatorAccount(person.getCode());
//					rtn.setCreatorAccount("105000380");
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
			
			
			if(hvo.getVdef14()!=null&&hvo.getVdef14().length()>1) {
				

				String[] years = hvo.getVdef14().split("[|]");
				String[] moneys = new String[0];
				if(hvo.getVdef15()!=null&&hvo.getVdef15().length()>1) {
					 moneys = hvo.getVdef15().split("[|]");
				}
				for(int i=0;i<years.length;i++) {
					JsonIntertemporal intertem = new JsonIntertemporal();
					intertem.setIntertemporalYear(years[i]);
					if(i<moneys.length) {
						intertem.setEstimateAmount(getDouble(moneys[i],2));
					}else {
						intertem.setEstimateAmount(new UFDouble(0,2));
					}
					rtn.addIntertemporal(intertem);
				}
				
//				rtn.setIntertemporalYear(hvo.getVdef14());
//				rtn.setEstimateAmount(getDouble(hvo.getVdef15(),2));
			
				
				
//				String[] years = hvo.getVdef14().split("|");
//				String[] moneys = new String[0];
//				if(hvo.getVdef15()!=null&&hvo.getVdef15().length()>1) {
//					 moneys = hvo.getVdef15().split("|");
//				}
//				for(int i=0;i<years.length;i++) {
//					JsonIntertemporal intertem = new JsonIntertemporal();
//					intertem.setIntertemporalYear(years[i]);
//					if(i<moneys.length) {
//						intertem.setEstimateAmount(getDouble(moneys,2));
//					}else {
//						intertem.setEstimateAmount(new UFDouble(0,2));
//					}
//					rtn.addIntertemporal(intertem);
//				}
				
//				rtn.setIntertemporalYear(hvo.getVdef14());
//				rtn.setEstimateAmount(getDouble(hvo.getVdef15(),2));
			}
//			rtn.setIntertemporalYear(hvo.getVdef14());
//		 
//			rtn.setEstimateAmount(getDouble(hvo.getVdef15(),2));
			
			
			if(getBooleanInt(hvo.getVdef16())!=null) {
				
				DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef16());
				if(defVo2!=null) {
					rtn.setIsImportantRelatedDeal(getBooleanInt(defVo2.getCode()));
				}else {
					rtn.setIsImportantRelatedDeal(0);
				}
			
			}else {
				rtn.setIsImportantRelatedDeal(0);
			}
 		 
		
			
			
			
			if(getBooleanInt(hvo.getVdef17())!=null) {
				DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef17());
				if(defVo2!=null) {
					rtn.setIsNeedPerfApprove(getBooleanInt(defVo2.getCode()));
				}else {
					rtn.setIsNeedPerfApprove(0);
				}
			
			}else {
				rtn.setIsNeedPerfApprove(0);
			}
 	 
		
			
			
			
			if(hvo.getVdef26()!=null) {
				rtn.setRelatedDealItem(hvo.getVdef26());
			}
 		 
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
			
			
			BaseDAO dao = new BaseDAO();
			if(vo.getPk_tradetypeid()!=null) {
				BilltypeVO billType =   (BilltypeVO)dao.retrieveByPK(BilltypeVO.class, vo.getPk_tradetypeid());
				if(billType!=null&&"F0-Cxx-01".equals(billType.getPk_billtypecode()) ){
					
				}else {
					return null;
				}
			}
			
			

			
			rtn.setContractUniqueId("114_"+vo.getDef1());
			JsonComeInfo info = new JsonComeInfo();
			info.setIncomeAmount(getDoubleStr(vo.getLocal_money(),2));
			info.setCurrentPeriodAmount(getDoubleStr(getDouble(vo.getDef4(),2), 2));
			info.setIncomeId("114_"+vo.getPk_recbill() );
			
			info.setRemarks(vo.getDef9());
			List<Object[]> mess = (List<Object[]>) dao.executeQuery("select   filepath  from sm_pub_filesystem where  filepath like '"+vo.getPk_recbill()+"%' and isdoc is not null",  new ArrayListProcessor());
					CtSaleVO sale = (CtSaleVO)dao.retrieveByPK(CtSaleVO.class, vo.getDef1());
					rtn.setIncomeTotalAmount(getDoubleStr(sale.getNorigpshamount(),2));
					
			if(mess!=null&&mess.size()>0) {
				for(int i=0;i<mess.size();i++) {
					Object[]  objs = mess.get(i);
					String path = (String) objs[0];
					String[] paths = path.split("/");
					String name = paths[paths.length-1];
				    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
//	                Date parse = sdf.parse(sale.getSubscribedate()));
	                String yearMonth = sdf.format(sale.getSubscribedate().toDate());
					CtSaleFileJsonVO file = new CtSaleFileJsonVO(name, "/home/document/kgjn/"+yearMonth+"/"+sale.getVbillcode()+"/"+vo.getPk_recbill()+"/"+name, "", i+1);
					info.getAssistEvidence().add(file);
				}
			}
			List<JsonComeInfo> infos =  new ArrayList<JsonComeInfo>();
			infos.add(info);
			rtn.setIncomeInfoList(infos);
			
			
		  
			 
			 
			
//			rtn.setIncomeTotalAmount(getDoubleStr(vo.getLocal_money(), 2) );
			
			return rtn;
		}catch(Exception ex) {
			Logger.init("iufo");
			Logger.error(ex);
			throw new BusinessRuntimeException(ex.getMessage(),ex);
		}finally {
			Logger.init();
		}

		
		
	}

	private String getDoubleStr(UFDouble local_money, int i) {
		if(local_money==null) {
			return null;
		}
		return new UFDouble(local_money.toDouble(),i).toString();
	}

	/***
	 * 合同收款计划反馈
	 * tuoxinx
	 * 
	 */
	@Override
	public PaymentPlanAndFeedbackInfo pushBillToService(String pk_ct_sale) {
		return pushBillToService(pk_ct_sale,null,null);
	}
	
	
	/**
	 * 付款单计划反馈信息报送  tuoxingx
	 */
	@Override
	public PaymentPlanAndFeedbackInfo pushPayBillToService(String pk_pu_sale) {
		return this.pushPayBillToService(pk_pu_sale,null,null);
	}

	@Override
	public JsonReceivableVO pushReceivables(Collection<String> billVo) {
		
		AggReceivableBillVO[] bills = null;
		try {
			BillQuery<AggReceivableBillVO> queryVO = new BillQuery(AggReceivableBillVO.class);
			bills = (AggReceivableBillVO[]) queryVO.query(billVo.toArray(new String[0]));
			if(bills!=null||bills.length>0) {
				return this.pusReceivable(bills[0]);
			}
		 
		}catch(Exception ex) {
			Logger.init("iufo");
			Logger.error(ex.getMessage(),ex);
			
		}finally {
			Logger.init();
		}

		
		
		
		
		
		return null;
	}

	@Override
	public UFBoolean isUseSend() {
		try {
			Logger.init("iufo");
			IUifService service = NCLocator.getInstance().lookup(IUifService.class);
			SysInitVO[] svos = (SysInitVO[]) service.queryByCondition(SysInitVO.class, "initcode = 'send_flag'");
			if(svos!=null&&svos.length>0) {
				return new UFBoolean(svos[0].getValue());
			}
		}catch(Exception ex) {
			Logger.error("查询发送参数错误:"+ex.getMessage(),ex);
			
		}finally {
			Logger.init();
		}
		return new UFBoolean(false);
		
	}

	@Override
	public String getSendUrl() {
		try {
			Logger.init("iufo");
			IUifService service = NCLocator.getInstance().lookup(IUifService.class);
			SysInitVO[] svos = (SysInitVO[]) service.queryByCondition(SysInitVO.class, "initcode = 'send_url'");
			if(svos!=null&&svos.length>0) {
				return svos[0].getValue();
			}
		}catch(Exception ex) {
			Logger.error("查询发送参数错误:"+ex.getMessage(),ex);
			
		}finally {
			Logger.init();
		}
		return "http://172.18.102.210:8888";
	}

	@Override
	public UFBoolean isUseSend(Object sendHeadOrOrgPk) {
		String pk_org = null;
		if(sendHeadOrOrgPk instanceof SuperVO) {
			pk_org =   (String)((SuperVO)sendHeadOrOrgPk). getAttributeValue("pk_org");
		}
		if(sendHeadOrOrgPk instanceof String) {
			pk_org = sendHeadOrOrgPk.toString();
		}
		
		
		try {
			if(pk_org==null) {
				return new UFBoolean(false);
			}
			
			Logger.init("iufo");
			IUifService service = NCLocator.getInstance().lookup(IUifService.class);
			OrgVO orgVo = (OrgVO)service.queryByPrimaryKey(OrgVO.class, pk_org);
			if(orgVo==null) {
				return new UFBoolean(false);
			}
			SysInitVO[] svos = (SysInitVO[]) service.queryByCondition(SysInitVO.class, "initcode = 'send_flag'");
			if(svos!=null&&svos.length>0) {
				String[] orgCodes = svos[0].getValue().split(",");
				for(String code:orgCodes) {
					if(code.equals(orgVo.getCode())) {
						return new UFBoolean(true);
					}
				}
				 
			}
			
		}catch(Exception ex) {
			Logger.error("查询发送参数错误:"+ex.getMessage(),ex);
			
		}finally {
			Logger.init();
		}
		return new UFBoolean(false);
		
		 
	}
	
	
	private static  Properties ftpProperties;
	
	private Properties readLogInfo() {
		
		if(ftpProperties==null) {
			
			
			  Properties properties = new Properties();
			    ClassLoader load = FtpUtil.class.getClassLoader();
			    String nchome = RuntimeEnv.getInstance().getCanonicalNCHome();
			    InputStream in = null;
			    String path = nchome + File.separator + "resources"+ File.separator + "kgjn" + File.separator  + "ftpinfo.properties";
					
//			    String str = getString(path);
//			    InputStream   is   =   new   ByteArrayInputStream(str.getBytes());
			    //InputStream is = load.getResourceAsStream("conf/vsftpd.properties");
			    try {
			    	FileInputStream is = new FileInputStream(path);
			        properties.load(is);
			       String host=properties.getProperty("ftpinfo.ip");
			       String  port=properties.getProperty("ftpinfo.port");
			       String   username=properties.getProperty("ftpinfo.user");
			       String   password=properties.getProperty("ftpinfo.pwd");
			        //服务器端 基路径
			       String   basePath=properties.getProperty("ftpinfo.remote.base.path");
			        //服务器端 文件路径
			       String   filePath=properties.getProperty("ftpinfo.remote.file.path");
			        //本地 下载到本地的目录
			       String  localPath=properties.getProperty("ftpinfo.local.file.path");
			       
			       
			       ftpProperties = properties;
			    } catch (IOException e) {
			        e.printStackTrace();
			    }
			
		}
		return ftpProperties;
		
	  
		
	}



	@Override
	public String getNCFileInfo(Object saleVoOrCpVo) {
	 
//	    String pk_ct = null;
//	    NCFileVO[] ncfiles = NCLocator.getInstance().lookup(IAttachmentService.class).queryNCFileByBill(pk_ct);
//   	    Map<String, String> map = new HashMap<String, String>();
//   	    for (int i = 0; i < ncfiles.length; i++) {
//			NCFileVO ncFileVO = ncfiles[i];
//			String name = ncFileVO.getName();
//			String fullPath = ncFileVO.getFullPath();
//			
//   	    }
		try {
	
   	 List<AttachPathVo>   allFiles = new ArrayList<AttachPathVo>();
   	    if(saleVoOrCpVo instanceof CtSaleVO) {
   	     allFiles =  this.getFilePath(((CtSaleVO)saleVoOrCpVo).getPk_ct_sale());
   	    }else if(saleVoOrCpVo instanceof CtPuVO) {
   	    	allFiles =  this.getPuFilePath(((CtPuVO)saleVoOrCpVo).getPk_ct_pu());
   	    }
   	    if(allFiles==null) {
   	    	return "请录入相关附件";
   	    }
   	    StringBuffer rtn = new StringBuffer();
 
   	    List<CtSaleFileJsonVO>  failsVo = null;
   	    failsVo =  getFileType(null,TYPE_FILE_ZBTZS, allFiles) ;// 中标通知书

   	    
   	    rtn.append(checkFtpPath(failsVo,"中标通知书"));
   	    
   	    
   	  failsVo =  getFileType(null,TYPE_FILE_HTZW, allFiles) ;// 中标通知书
 	    rtn.append(checkFtpPath(failsVo,"合同正文"));
 	    
 	   failsVo =  getFileType(null,TYPE_FILE_HTSPD, allFiles) ;// 中标通知书
 	   rtn.append(checkFtpPath(failsVo,"合同审批单"));
 	   
 	   boolean checkWf = false;
 	   boolean checkDf = false;
  	   if(saleVoOrCpVo instanceof CtSaleVO) {
  		 int value =  this.getBooleanInt(((CtSaleVO)saleVoOrCpVo).getVdef19());
  		 checkWf =  value==1;
  		 
  		 checkDf =  this.getBooleanInt(((CtSaleVO)saleVoOrCpVo).getVdef21())==1;
  		 
//  		IUifService service = NCLocator.getInstance().lookup(IUifService.class);
//  		 String type = ((CtSaleVO)saleVoOrCpVo).getVdef20();
//			if(type!=null) {
//			
//					DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, type);
//					if(defVo2!=null) {
//						 checkWf =  value==0&&"固定授权".equals(defVo2.getName());
//					}
//				}
				
			
			 
  		
  	   	    }else if(saleVoOrCpVo instanceof CtPuVO) {
  	   		 int value =  this.getBooleanInt(((CtPuVO)saleVoOrCpVo).getVdef19());
  	   		 checkWf =  value==1;
  	   		 
  	   		  checkDf =  this.getBooleanInt(((CtPuVO)saleVoOrCpVo).getVdef21())==1;
	   		 
//  	  		IUifService service = NCLocator.getInstance().lookup(IUifService.class);
//  	  		 String type = ((CtPuVO)saleVoOrCpVo).getVdef20();
//  				if(type!=null) {
//  				
//  						DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, type);
//  						if(defVo2!=null) {
//  							 checkWf =  value==0&&"固定授权".equals(defVo2.getName());
//  						}
//  					}
  	   }
 	   if(checkWf) {
 		  failsVo =  getFileType(null,TYPE_FILE_WFSQWTS, allFiles) ;// 中标通知书
 	 	  rtn.append(checkFtpPath(failsVo,"我方授权委托书")); 
 	   }
 	 
 	  if(checkDf) {
 		 failsVo =  getFileType(null,TYPE_FILE_DFSQWTS, allFiles) ;// 中标通知书
 	 	 rtn.append(checkFtpPath(failsVo,"对方授权委托书"));
 	  }
 
 	 
 	 failsVo =  getFileType(null,TYPE_FILE_HTZW, allFiles) ;// 中标通知书
 	 rtn.append(checkFtpPath(failsVo,"合同签署文本"));
   	    
   	    if(ftp!=null) {
   	    	try {
   	    		ftp.disconnect();
   	    		ftp = null;
   	    	}catch(Exception ex) {
   	    		
   	    	}
   	    }
   	    return rtn.toString();
		}catch(Exception ex) {
			Logger.init("iufo");
			Logger.error("检查合同附件错误"+ex.getMessage());
		}finally {
		}	
		return "";
   	    
   	    
  
   	  
	   	 
	 
	}
	
//	getFileType(hvo,TYPE_FILE_ZBTZS, allFiles)  中标通知书
//	getFileType(hvo,TYPE_FILE_HTZW, allFiles)    合同正文
//	getFileType(hvo, TYPE_FILE_HTSPD, allFiles)  合同审批单
//	getFileType(hvo, TYPE_FILE_WFSQWTS, allFiles)  我方授权委托书	
//	getFileType(hvo, TYPE_FILE_DFSQWTS, allFiles)  对方授权委托书
//	getFileType(hvo, TYPE_FILE_HTQSWB, allFiles)   合同签署文本
	
	private String checkFtpPath( List<CtSaleFileJsonVO>  failsVos,String typeName) {
		String rtn = "";
   	    if(failsVos==null) {
   	    	rtn  = "请上传"+typeName+"\n";
   	    }
   	    for(CtSaleFileJsonVO vo:failsVos) {
   	    	if(!isExsits(vo.getFilepath())) {
   	    		rtn = rtn +typeName+":"+ vo.getFilename()+"未上传成功,请重新上传.";
   	    	}
   	    }
		return rtn;
		
	}
	
	
	
	


    /***
     * 判断文件是否存在
     * @param ftpPath
     * @return
     */
    public  boolean isExsits(String ftpPath){
    	  try {
    	if(ftp==null) {
    		
    		  String   username=readLogInfo().getProperty("ftpinfo.user");
    		    String   password=readLogInfo().getProperty("ftpinfo.pwd");
    	    	String url =readLogInfo().getProperty("ftpinfo.ip");
    	    	int port = Integer.valueOf( readLogInfo().getProperty("ftpinfo.port"));
    	    	
    	         ftp = new FTPClient();
    	        ftp.setConnectTimeout(5000);
    	        ftp.setAutodetectUTF8(true);
    	        ftp.setCharset(java.nio.charset.Charset.forName("UTF-8"));
    	        ftp.setControlEncoding(java.nio.charset.Charset.forName("UTF-8").name());
    	        ftp.connect(url, port);
                ftp.login(username, password);// 登錄
                if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                    ftp.disconnect();
                    throw new IOException("login fail!");
                }
    	}
 
	  
  

    	
    	
 
//        FTPClient ftpx = getFTPClient( url,  port,  username,  password);
      
       
         
       
            ftp.changeWorkingDirectory(ftpPath);
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
//        	ftpx.enterLocalPassiveMode();
            FTPFile[] files =ftp.listFiles(ftpPath);
            if(files!=null&&files.length>0){
             
                return true;
            }else {
                return false;
            }
        } catch (Exception e) {
        	return true;
        }finally {
        	 
        }
    }

    private static FTPClient ftp;
    public  static FTPClient getFTPClient(String url, int port, String username, String password){
        if(ftp!=null)return ftp;
        FTPClient ftptemp = new FTPClient();
        try {
            int reply;
            ftptemp.connect(url, port);
            ftptemp.login(username, password);
            reply = ftptemp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftptemp.disconnect();
            }
            ftp = ftptemp;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return ftp;
    }

	@Override
	public void setSendFlag(SuperVO vo) {
		 
		
	}

	@Override
	public CtPuVO queryByContractNO(String puNo) {
		BaseDAO dao = new BaseDAO();
		try {
			Collection<CtPuVO> datas =  dao.retrieveByClause(CtPuVO.class, "vbillcode = '"+puNo+"'");
			if(datas!=null&&datas.size()>0) {
				return datas.toArray(new CtPuVO[0])[0];
			}
			
		} catch (Exception e) {
		 return null;
		}
		return null;
	}

	@Override
	public PaymentPlanAndFeedbackInfo pushBillToService(String pk_ct_sale, String isNormal, String abnormalReason) {

		try {
			
			CtBillQueryDao ctSaleBillQueryDao = new CtBillQueryDao();
			PaymentPlanAndFeedbackInfo billJsonVo = new PaymentPlanAndFeedbackInfo();
			//合同id
			billJsonVo.setContractUniqueId("114_" + pk_ct_sale);
			billJsonVo.setSourceInfo("FEEDBACK");
			List<CtSalePayTermVO> queryCtSalePayterms = ctSaleBillQueryDao.queryCtSalePayterms(pk_ct_sale);
			
			List<PaymentFeedback> feedbackList = new ArrayList<PaymentFeedback>();
		
		
			for (CtSalePayTermVO ctAbstractPayTermVO : queryCtSalePayterms) {   
				UFDouble recMoney = new UFDouble(0).setScale(2, UFDouble.ROUND_HALF_UP);
				PaymentFeedback feedback =  new PaymentFeedback();
				//计划ID
//				feedback.setPlanId(ctAbstractPayTermVO.getPk_ct_sale_payterm());
				feedback.setPlanId("114_" + ctAbstractPayTermVO.getPk_ct_sale_payterm() + "_" + ctAbstractPayTermVO.getShoworder());
				
				//反馈ID
				feedback.setFeedBackId(ctAbstractPayTermVO.getPk_ct_sale_payterm() + "_FK");
				feedback.setSortNum(null);
				if(isNormal!=null) {
					feedback.setIsNormal(this.getBooleanInt(isNormal ));
	    			feedback.setAbnormalReason(abnormalReason);
				}else {
					feedback.setIsNormal(1 );
				}
	 	
    			feedback.setRealPayDate(getDataTime(new Date()));
    			UFDouble addValue = new UFDouble(0D,2);
    			if(ctAbstractPayTermVO.getNctrecvmny()!=null) {
    				addValue = new UFDouble( ctAbstractPayTermVO.getNctrecvmny().toString(),2);
    			}
//    			recMoney = recMoney.add(addValue);
    			
    			feedback.setRealPayAmount(addValue.toString());
    			feedbackList.add(feedback);
			}
			
			billJsonVo.setPaymentFeedbackList(feedbackList);
			return billJsonVo;
		} catch (Exception ex) {
			return null;
		}
	
	}

	@Override
	public PaymentPlanAndFeedbackInfo pushPayBillToService(String pk_pu_sale, String isNormal, String abnormalReason) {

        try {
			
			CtBillQueryDao ctSaleBillQueryDao = new CtBillQueryDao();
			PaymentPlanAndFeedbackInfo billJsonVo = new PaymentPlanAndFeedbackInfo();
			//合同id
			billJsonVo.setContractUniqueId("114_" + pk_pu_sale);
			billJsonVo.setSourceInfo("FEEDBACK");
			List<PayPlanVO> ctPurPayplans = ctSaleBillQueryDao.queryCtPurPayplans(pk_pu_sale);
			
			
			List<PaymentFeedback> feedbackList = new ArrayList<PaymentFeedback>();
			
			for (PayPlanVO payPlanVO : ctPurPayplans) {   
				PaymentFeedback feedback =  new PaymentFeedback();
				//计划ID
				feedback.setPlanId(payPlanVO.getPk_ct_payplan());
				//反馈ID
				feedback.setFeedBackId(payPlanVO.getPk_ct_payplan() + "_FK");
				feedback.setSortNum(null);
			     if(isNormal==null) {
			    	 feedback.setIsNormal(1 );
//			   			feedback.setAbnormalReason(payPlanVO.getVbdef3());
			     }else {
			    	 feedback.setIsNormal( getBooleanInt(isNormal));
			   			feedback.setAbnormalReason(abnormalReason);
			     }
   			
   			feedback.setRealPayDate(getDataTime(new Date()));
   			if(payPlanVO.getNaccumpayorgmny()==null) {
   				feedback.setRealPayAmount((new UFDouble(0,2)).toString() );
   			}else {
   				feedback.setRealPayAmount(getDoubleStr(payPlanVO.getNaccumpayorgmny(), 2) );
   			}
   			
   			feedbackList.add(feedback);
			}
		
			billJsonVo.setPaymentFeedbackList(feedbackList);
			return billJsonVo;
		} catch (Exception ex) {
			return null;
		}
	
	}
	
	
	
	
	
//	/**
//	 * 更新销售合同状态
//	 * @param pk_sale
//	 */
//	void updateSale(String pk_sale);//CtSaleVO  ct_sale  def25
//	
//	/**
//	 * 更新采购合同状态
//	 * @param pk_pu
//	 */
//	void updatePu(String pk_pu);//CtPuVO  ct_pu  def25
//	
//	/**
//	 * 更新收入确认单
//	 * @param pk_receivable
//	 */
//	void updateReceivable(String pk_receivable);//ReceivableBillVO   ar_recbill  def8
//	
//	
//	/**更新付款单标志
//	 * @param pk_pay
//	 */
//	void updatePayBill(String pk_pay);//付款单 PayBillVO   ap_paybill   def8
//	
//	
//	/**
//	 * 更新收款单标志
//	 * @param pk_gethering
//	 */
//	void updateGathering(String pk_gethering);//GatheringBillVO   ar_gatherbill def8
//	

	@Override
	public void updateSale(String pk_sale) {
		executeUpdateSql("update ct_sale set vdef25='已上报' where pk_ct_sale = '"+pk_sale+"'");
		
	}
	
	private  void executeUpdateSql(String sql) {
		
		try {
			BaseDAO dao = new BaseDAO();
			dao.executeUpdate(sql);
		}catch(Exception ex) {
			Logger.init("iufo");
			Logger.error("##execute sql err,Sql is:"+sql+";errMessage is :"+ex.getMessage());
			Logger.error(ex);
		}finally {
			Logger.init();
		}
		
	
		
	}

	@Override
	public void updatePu(String pk_pu) {
		executeUpdateSql("update ct_pu set vdef25='已上报' where pk_ct_pu = '"+pk_pu+"'");
		
	}

	@Override
	public void updateReceivable(String pk_receivable) {
		executeUpdateSql("update ar_recbill set def8='已上报' where pk_recbill = '"+pk_receivable+"'");
		
	}

	@Override
	public void updatePayBill(String pk_pay) {
		executeUpdateSql("update ap_paybill set def8='已上报' where pk_paybill = '"+pk_pay+"'");
		
	}

	@Override
	public void updateGathering(String pk_gethering) {
		executeUpdateSql("update ar_gatherbill set def8='已上报' where pk_gatherbill = '"+pk_gethering+"'");
		
	}
}
