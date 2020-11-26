package nc.impl.ct.sendsale;

import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.sun.istack.internal.NotNull;

import nc.bs.framework.common.NCLocator;
import nc.itf.ct.sendsale.ISendSaleServer;
import nc.itf.uif.pub.IUifService;
import nc.vo.bd.cust.CustomerVO;
import nc.vo.bd.defdoc.DefdocVO;
import nc.vo.bd.psn.PsndocVO;
import nc.vo.ct.saledaily.entity.AggCtSaleVO;
import nc.vo.ct.saledaily.entity.CtSaleFileJsonVO;
import nc.vo.ct.saledaily.entity.CtSaleJsonVO;
import nc.vo.ct.saledaily.entity.CtSaleVO;
import nc.vo.ct.saledaily.entity.SaleParamCheckUtils;
import nc.vo.org.DeptVO;
import nc.vo.org.OrgVO;
import nc.vo.pmpub.project.ProjectHeadVO;
import nc.vo.pub.lang.UFDouble;
import nc.vo.vorg.DeptVersionVO;

public class SendSaleServerImpl implements ISendSaleServer {
	
	private static String  TYPE_FILE_ZBTZS = "ZBTZS";//中标通知书
	
	private static String TYPE_FILE_HTZW="HTZW";//合同正文
	
	private static String TYPE_FILE_LXYJ="LXYJ";//立项依据
	
	private static String TYPE_FILE_HTSPD="HTSPD";//合同审批单
	
	private static String TYPE_FILE_WFSQWTS = "WFSQWTS";//我方授权委托书
	private static String TYPE_FILE_DFSQWTS = "DFSQWTS";//对方授权委托书
	
	private static String TYPE_FILE_HTQSWB = "HTQSWB";//合同签署文本

	@Override
	public CtSaleJsonVO pushSaleToService(AggCtSaleVO saleVO) {

		try {
			CtSaleVO hvo = saleVO.getParentVO();

			SaleParamCheckUtils.doValidator(hvo);
			
			CtSaleJsonVO rtn = new CtSaleJsonVO();

			IUifService service = NCLocator.getInstance().lookup(IUifService.class);
			OrgVO queryOrg = (OrgVO) service.queryByPrimaryKey(OrgVO.class, hvo.getPk_org());
			ProjectHeadVO project = null;
			if(hvo.getCprojectid()!=null) {
				project = (ProjectHeadVO)service.queryByPrimaryKey(ProjectHeadVO.class, hvo.getCprojectid());
			}
			
			

			// 1~11
			rtn.setContractUniqueId(queryOrg.getCode() + "_" + hvo.getPk_ct_sale());
			rtn.setContractType(getInteValue(hvo.getVdef1()));
			rtn.setContractSubject(hvo.getVdef2());
			rtn.setContractName(hvo.getCtname());
			rtn.setContractSelfCode(hvo.getVbillcode());
			if(project!=null) {
				rtn.setRelatedProjectName(project.getProject_name());
				rtn.setRelatedProjectCode(project.getProject_code());
			}

			rtn.setBuyMethod(null);
			rtn.setBidFile(getFileType(hvo,TYPE_FILE_ZBTZS));
			rtn.setContractAmount(getStringValue(hvo.getNtotalorigmny()));
			rtn.setValuationMode(getInteValue(hvo.getVdef3()));
			
			
			
			
		 
			
			//11~20
			rtn.setCurrencyName(hvo.getCorigcurrencyid());
			rtn.setExchangeRate(getStringValue(hvo.getNexchangerate()));
			rtn.setAmountExplain(hvo.getVdef4());
			rtn.setPaymentDirection(2);
		 	//文档需要整数,但是支持多选这里改成String每个用|分隔表示多选,后续可能改成数组或在List
			rtn.setPaymentType(hvo.getVdef5());//这个有矛盾需要处理
			rtn.setPaymentMethod(hvo.getVdef6());
			rtn.setIsAdvancePayment(null);
			rtn.setSigningSubject("煤科院节能技术有限公司");
			rtn.setSigningSubjectCode("114");

			PsndocVO person = (PsndocVO)service.queryByPrimaryKey(PsndocVO.class, hvo.getPersonnelid());
			if(person!=null) {
				rtn.setCreatorAccount(person.getCode());
			
				rtn.setCreatorName(person.getName());
			
			
			
			}
			
//			
//			
//			
//			
			//21~30
			DeptVO dept = (DeptVO)service.queryByPrimaryKey(DeptVO.class, hvo.getDepid()) ;
			if(dept!=null) {
				rtn.setCreatorDeptCode(dept.getCode());
				rtn.setCreatorDeptName(dept.getName());
			}

			rtn.setPerformAddress(hvo.getVdef7());
			rtn.setSignAddress(hvo.getVdef8());
			rtn.setContractPeriod(getInteValue( hvo.getVdef9()));
			rtn.setPerformPeriod(hvo.getVdef10());
			rtn.setPeriodExplain(hvo.getVdef11());
			rtn.setContractContent(null);
			rtn.setContractText(getFileType(hvo,TYPE_FILE_HTZW));
//			
//			
//			
//			//31~40
			rtn.setContractGist(getFileType(hvo, TYPE_FILE_LXYJ));
			rtn.setContractApprovalForm(getFileType(hvo, TYPE_FILE_HTSPD));
			rtn.setContractAttachment(null);
			CustomerVO cust = (CustomerVO)service.queryByPrimaryKey(CustomerVO .class,hvo.getPk_customer()) ;
			if(cust!=null) {
				rtn.setOppositeUniqueId(cust.getTaxpayerid());
				rtn.setOppositeName(cust.getName());
			}
			rtn.setOppositeRelName(hvo.getVdef12());
			rtn.setBankOfDeposit(null);
			rtn.setBankAccount(null);
			rtn.setBankAccountName(null);
			if(cust!=null) {
				rtn.setIsRelatedParty(getInteValue(cust.getDef2()));
				rtn.setRpType(getInteValue(cust.getDef3()));
				rtn.setIsRelatedDeal(getInteValue(cust.getDef4()));
				rtn.setDealType(cust.getDef5());
			}
			
//			
			//41~50
	 
		
		 
			rtn.setIsIntertemporal(getBooleanInt(hvo.getVdef13()));
			rtn.setIntertemporalYear(hvo.getVdef14());
		 
			rtn.setEstimateAmount(getDouble(hvo.getVdef15(),2));
			 
			rtn.setIsImportantRelatedDeal(getBooleanInt(hvo.getVdef16()));
		 
			rtn.setIsNeedPerfApprove(getBooleanInt(hvo.getVdef17()));
			 
			rtn.setSealTime(null);
			
			DefdocVO defVo1 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef18());
			if(defVo1!=null) {
				rtn.setSealType(defVo1.getCode());//是否需要转换用印类型
			}
			
//			
//			
//			//51~60
			rtn.setSignNum(null);
			rtn.setSignTime(hvo.getSubscribedate().toString());
			rtn.setOwnIsAuth(getBooleanInt(hvo.getVdef19()));
			
			DefdocVO defVo2 = (DefdocVO)service.queryByPrimaryKey(DefdocVO.class, hvo.getVdef20());
			if(defVo2!=null) {
			 rtn.setAuthType(getInteValue( defVo2.getCode()));
			}
			rtn.setOwnAuth(getFileType(hvo, TYPE_FILE_WFSQWTS));
			rtn.setOurName(null);
			rtn.setOpptName(null);
			rtn.setOpptIsAuth(getBooleanInt(hvo.getVdef21()));
			rtn.setSealTopptAuthype(getFileType(hvo, TYPE_FILE_DFSQWTS));
			rtn.setContractScanFile(getFileType(hvo, TYPE_FILE_HTQSWB));
			

			return rtn;
		} catch (Exception ex) {
			return null;
		}

	}
	
	
	private UFDouble getDouble(Object value,int smail) {
		if(value==null) {
			return null;
		}
		 UFDouble doubleValue = new UFDouble(value.toString(),smail);
		 return doubleValue;
	}
	
	private Integer getBooleanInt(Object value) {
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
	
	
	private List<CtSaleFileJsonVO> getFileType(CtSaleVO hvo,String type){
		List<CtSaleFileJsonVO> paths = new ArrayList<CtSaleFileJsonVO>();
		
		
		
		paths.add(new CtSaleFileJsonVO("filename", "filetime", ""));
		return paths;
		
	}
	
	private String getDataTime(Date date) {
//		Formatter format = new dateformat
		return "";
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

}
