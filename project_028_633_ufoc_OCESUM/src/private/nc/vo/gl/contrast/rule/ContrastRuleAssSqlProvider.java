package nc.vo.gl.contrast.rule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.exception.ComponentException;
import nc.bs.logging.Logger;
import nc.md.gl.metaData.GlAccAssinfoVO;
import nc.vo.fi.pub.SqlUtils;
import nc.vo.fip.pub.SqlTools;
import nc.vo.fipub.freevalue.Module;
import nc.vo.fipub.utils.StrTools;
import nc.vo.gl.contrast.DetailContrastVO;
import nc.vo.gl.contrast.SumContrastVO;
import nc.vo.gl.contrast.iufo.ContrastHBBBQryVO;
import nc.vo.gl.contrast.report.ContrastReportSubVO;
import nc.vo.gl.contrast.result.ResultDetailTabVO;
import nc.vo.gl.glreport.publictool.GlTools;
import nc.vo.gl.glreporttools.GlAssVOTools;
import nc.vo.gl.glreporttools.PrepareAssParse;
import nc.vo.glcom.ass.AssVO;
import nc.vo.pub.BusinessException;

import org.apache.commons.lang.StringUtils;

public class ContrastRuleAssSqlProvider {
	
	private ContrastRuleVO rulevo=null;
	
	
	private boolean needAss=false;
	
	private GlAccAssinfoVO[] assinfovos=null;
	
	private nc.vo.glcom.ass.AssVO[] assvos=null;
	
	
	
	
//	
	
	public ContrastRuleAssSqlProvider(ContrastRuleVO vo, nc.vo.glcom.ass.AssVO[] new_assvos,String pk_group){
		super();
		if(null==new_assvos || new_assvos.length==0){
			this.setAssvos(ContrastRuleAssUtil.getAssvos(vo));
			this.setRulevo(vo);
			this.setNeedAss(this.isNeedAss(vo));
			this.setAssinfovos(this.generateAssInfoVos(vo,pk_group));
		}else{
			this.setAssvos(new_assvos);
			this.setRulevo(vo);
			this.setNeedAss(this.isNeedAss(vo));
			this.setAssinfovos(this.generateAssInfoVos(vo,pk_group));
		}
		
		
		
		
		
	}
	
	public class AccTabAppend {

		private String acctab = "";

		public AccTabAppend(String tab) {
			super();
			this.setAcctab(tab);
		}
		
		public String appAccTabWherePart(String[] pk_selfaccountingbooks,
				String[] pk_otheraccountingbooks){
			return appAccTabWherePart(pk_selfaccountingbooks,pk_otheraccountingbooks,true);
		}

		public String appAccTabWherePart(String[] pk_selfaccountingbooks,
				String[] pk_otheraccountingbooks,boolean bSelf) {
			StringBuffer content = new StringBuffer();
			content.append("  ( ");

			try {
				if(bSelf&&pk_selfaccountingbooks.length==1){
					content.append(" cdata.pk_accasoa in (select  pk_accasoa from " + acctab+ " )  and cdata.pk_accountingbook='"+ pk_selfaccountingbooks[0] + "' " );
					content.append(" and  "+SqlTools.getInStr("CDATA.PK_OTHERORGBOOK", pk_otheraccountingbooks, true));
				}else if(!bSelf&&pk_otheraccountingbooks.length==1){
					content.append(" cdata.pk_accasoa in (select  pk_accasoa from " + acctab+ " ) and "+SqlTools.getInStr("CDATA.pk_accountingbook", pk_selfaccountingbooks, true) );
					content.append(" and  CDATA.PK_OTHERORGBOOK='" + pk_otheraccountingbooks[0] + "' ");
				}else{
					content.append(" cdata.pk_accasoa in (select  pk_accasoa from " + acctab+ " ) and "+SqlUtils.getInStr("CDATA.pk_accountingbook", pk_selfaccountingbooks, true) );
					content.append(" and " +SqlUtils.getInStr("CDATA.PK_OTHERORGBOOK",pk_otheraccountingbooks,true));
//					boolean bFirst = true;
//					for (int i = 0; i < pk_selfaccountingbooks.length; i++) {
//						for (int j = 0; j < pk_otheraccountingbooks.length; j++) {
//							String pk_selfaccountingbook = pk_selfaccountingbooks[i];
//							String pk_otheraccountingbook = pk_otheraccountingbooks[j];
//							if (bFirst) {
//								bFirst = false;
//							} else {
//								content.append("   or ");
//							}
//							content.append("  ( ");
//								content.append("   cdata.pk_accasoa in (select  "
//										+ AccAsoaVO.PK_ACCASOA + " from " + acctab
//										+ " )   and cdata.pk_accountingbook='"
//										+ pk_selfaccountingbook
//										+ "' and  CDATA.PK_OTHERORGBOOK='"
//										+ pk_otheraccountingbook + "'");
//							content.append("  ) ");
//						}
//					}
				}
			} catch (BusinessException e) {
				Logger.error(e.getMessage(),e);
			}
			content.append(" ) ");
			return content.toString();
		}

		public String getAcctab() {
			return acctab;
		}

		public void setAcctab(String acctab) {
			this.acctab = acctab;
		}

	}
	
	 public AccTabAppend getAccTab(String accTab){
		 
		 AccTabAppend result=new AccTabAppend(accTab);
		 return result;
	 }
	 
  
	 
	 public AccTabAppend getAccTab(String dateStr,
			String[] pk_glorgbooks) {
		String accTab = "";
		String[] subjcodes = nc.vo.gl.contrast.rule.ContrastRuleSubjUtil
				.getSubjCodes(getRulevo());

		try {
			accTab = nc.vo.gateway60.itfs.AccountUtilGL
					.getEnableSubjTmpTableByDate(pk_glorgbooks, dateStr,
							InvocationInfoProxy.getInstance().getUserId(), subjcodes, null);
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			nc.bs.logging.Logger.error(e.getMessage(), e);
		}
		AccTabAppend result = new AccTabAppend(accTab);
		return result;

	}
  
	
	public ContrastRuleAssSqlProvider(ContrastRuleVO vo,String pk_group){
		super();
		this.setAssvos(ContrastRuleAssUtil.getAssvos(vo));
		this.setRulevo(vo);
		this.setNeedAss(this.isNeedAss(vo));
		this.setAssinfovos(this.generateAssInfoVos(vo,pk_group));
		
		
		
	}
	
	public ContrastRuleAssSqlProvider(ContrastHBBBQryVO qryvo,String pk_group){
		super();
		PrepareAssParse parse = new PrepareAssParse();
		
		try {
			AssVO[] assvos = parse.prepareAssitantToAssvos(qryvo.getAss(),pk_group);
			GlAssVOTools.suplyAssvosSubDocs(assvos,pk_group);
			this.setAssvos(StrTools.isEmptyStr(qryvo.getAss())
					?ContrastRuleAssUtil.getAssvos(qryvo.getContrastrulevo())
						:assvos);
		} catch (Exception e) {
			Logger.error(e.getMessage(),e);
		}
		this.setRulevo(qryvo.getContrastrulevo());
		this.setNeedAss(StrTools.isEmptyStr(qryvo.getAss())?this.isNeedAss(qryvo.getContrastrulevo()):true);
		this.setAssinfovos(this.generateAssInfoVos(qryvo.getContrastrulevo(),pk_group));
		
		
		
	}
	
	private String[]  getAssTypes(){
		
		String[] result=null;
		if(this.isNeedAss()){
			HashSet<String> set=new HashSet<String>();
			
			if(null!=this.getAssvos() && getAssvos().length>0){
				for(AssVO assvo:getAssvos()){
					set.add(assvo.getPk_Checktype().trim());
				}
				if(set.size()>0){
					result=new String[set.size()];
					set.toArray(result);
				}
			}
			
		}
		return result;
		
	}
	
	private GlAccAssinfoVO[] generateAssInfoVos(ContrastRuleVO vo,String pk_group){
		GlAccAssinfoVO[]  result=null;
		if(this.isNeedAss()){
			String[] types = getAssTypes();
			try {
				result=( (nc.itf.fipub.freevalueset.IFreeMap) NCLocator.getInstance().lookup(nc.itf.fipub.freevalueset.IFreeMap.class.getName())).getAccAssInfoByChecktypes(types,pk_group,Module.GL);
			} catch (ComponentException e) {
				// TODO Auto-generated catch block
				nc.bs.logging.Logger.error(e.getMessage(), e);
			} catch (BusinessException e) {
				// TODO Auto-generated catch block
				nc.bs.logging.Logger.error(e.getMessage(), e);
			}
		}
		return result;
	}

	public ContrastRuleVO getRulevo() {
		return rulevo;
	}

	private void setRulevo(ContrastRuleVO rulevo) {
		this.rulevo = rulevo;
	}
	
	
	private boolean isNeedAss( ContrastRuleVO contrastrulevo){
		boolean result=false;
		nc.vo.glcom.ass.AssVO[] assvos=ContrastRuleAssUtil.getAssvos(contrastrulevo);
		if(null!=assvos && assvos.length>0){
			result=true;
		}
		
		return result;
	}
	
	public String getGroupFieldSql(){
		String result="";
		if(this.isNeedAss()){
			String[] fields=new String[this.getAssinfovos().length];
			for(int i=0;i<this.getAssinfovos().length;i++){
				fields[i]=this.getAssinfovos()[i].getTableName()+"."+ this.getAssinfovos()[i].getFieldName();
			}
			result=GlTools.getFormValuesFromStrArray(fields);
		}else{
			return result;
		}
		
		return result;
	}
	
	public String getAppendTabFieldSql(String tabname){
		String result="";
		if(this.isNeedAss()){
			String[] fields=new String[this.getAssinfovos().length];
			for(int i=0;i<this.getAssinfovos().length;i++){
				fields[i]=tabname+"."+ this.getAssinfovos()[i].getFieldName();
			}
			result=GlTools.getFormValuesFromStrArray(fields);
		}else{
			return result;
		}
		
		return result;
	}
	
	public String getFieldSql(){
		String result="";
		if(this.isNeedAss()){
			String[] fields=new String[this.getAssinfovos().length];
			for(int i=0;i<this.getAssinfovos().length;i++){
				fields[i]= this.getAssinfovos()[i].getFieldName();
			}
			result=GlTools.getFormValuesFromStrArray(fields);
		}else{
			return result;
		}
		
		return result;
	}
	
	public String getDetailContrastTypeValueSql(){
		String result= "";
		if(this.isNeedAss()){
			for(int i=0;i<this.getAssinfovos().length;i++){
				if(i==0){
					result= this.getAssinfovos()[i].getFieldName();
				}else{
				    result+= " || " + this.getAssinfovos()[i].getFieldName();	
				}
			}
			result += "  " + DetailContrastVO.TYPEVALUE + "  ";
		}else{
			result= "'N/A'" + DetailContrastVO.TYPEVALUE + "  ";
		}
		return result;
	}
	
	
	

	public String getSubjRowAssFieldSql(){
		String result="";
		if(this.isNeedAss()){
			String[] fields=new String[this.getAssinfovos().length];
			for(int i=0;i<this.getAssinfovos().length;i++){
				StringBuffer content=new StringBuffer();
				content.append("  ");
				content.append("         ");
				content.append("            '"+ this.getAssinfovos()[i].getPk_checktype() +"' ||  "+this.getAssinfovos()[i].getTableName()+"."+ this.getAssinfovos()[i].getFieldName());
			
				switch (i) {
				case 0: {
					content.append("      " + SumContrastVO.TYPEVALUE1 );
					break;
				}
				case 1: {
					content.append("     " + SumContrastVO.TYPEVALUE2 );
					break;
				}
				case 2: {
					content.append("      " + SumContrastVO.TYPEVALUE3 );
					break;
				}
				case 3: {
					content.append("    " + SumContrastVO.TYPEVALUE4 );
					break;
				}
				case 4: {
					content.append("   " + SumContrastVO.TYPEVALUE5 );
					break;
				}
				case 5: {
					content.append("   " + SumContrastVO.TYPEVALUE6 );
					break;
				}
				case 6: {
					content.append("   " + SumContrastVO.TYPEVALUE7 );
					break;
				}
				case 7: {
					content.append("   " + SumContrastVO.TYPEVALUE8 );
					break;
				}
				}
				fields[i]= content.toString();
			}
			result=GlTools.getFormValuesFromStrArray(fields);
		}
		return result;
	}
	
	
	
	
	
	public String getSumContrastAssFieldSql(){
		String result="";
		if(this.isNeedAss()){
			String[] fields=new String[this.getAssinfovos().length];
			for(int i=0;i<this.getAssinfovos().length;i++){
				StringBuffer content=new StringBuffer();
				content.append(" CASE ");
				content.append("          WHEN (isnull(selftab.pk_accountingbook,'~')='~') ");
				content.append("             THEN  '"+ this.getAssinfovos()[i].getPk_checktype() +"' ||  othertab."+ this.getAssinfovos()[i].getFieldName());
				content.append("          ELSE  '"+ this.getAssinfovos()[i].getPk_checktype() +"' ||   selftab."+ this.getAssinfovos()[i].getFieldName());
				switch (i) {
				case 0: {
					content.append("       END  " + SumContrastVO.TYPEVALUE1 );
					break;
				}
				case 1: {
					content.append("       END  " + SumContrastVO.TYPEVALUE2 );
					break;
				}
				case 2: {
					content.append("       END  " + SumContrastVO.TYPEVALUE3 );
					break;
				}
				case 3: {
					content.append("       END  " + SumContrastVO.TYPEVALUE4 );
					break;
				}
				case 4: {
					content.append("       END  " + SumContrastVO.TYPEVALUE5 );
					break;
				}
				case 5: {
					content.append("       END  " + SumContrastVO.TYPEVALUE6 );
					break;
				}
				case 6: {
					content.append("       END  " + SumContrastVO.TYPEVALUE7 );
					break;
				}
				case 7: {
					content.append("       END  " + SumContrastVO.TYPEVALUE8 );
					break;
				}
				}
				fields[i]= content.toString();
			}
			result=GlTools.getFormValuesFromStrArray(fields);
		}
		return result;
	}
	
	public String[] getSumContrastFields(){
		String[] result=null;
		if(this.isNeedAss()){
			String[] fields=new String[this.getAssinfovos().length];
			for(int i=0;i<this.getAssinfovos().length;i++){
				switch (i) {
				case 0: {
					fields[i]= SumContrastVO.TYPEVALUE1;
					break;
				}
				case 1: {
					fields[i]= SumContrastVO.TYPEVALUE2;
					break;
				}
				case 2: {
					fields[i]= SumContrastVO.TYPEVALUE3;
					break;
				}
				case 3: {
					fields[i]= SumContrastVO.TYPEVALUE4;
					break;
				}
				case 4: {
					fields[i]= SumContrastVO.TYPEVALUE5;
					break;
				}
				case 5: {
					fields[i]= SumContrastVO.TYPEVALUE6;
					break;
				}
				case 6: {
					fields[i]= SumContrastVO.TYPEVALUE7;
					break;
				}
				case 7: {
					fields[i]= SumContrastVO.TYPEVALUE8;
					break;
				}
				}
				
			}
			result=fields;
		}
		
		return result;
	}
	
	
	public String getSumBalanceResultQryAssFieldSql(){
		String result="";
		if(this.isNeedAss()){
			String[] fields=new String[this.getAssinfovos().length];
			for(int i=0;i<this.getAssinfovos().length;i++){
				StringBuffer content=new StringBuffer();
				content.append(" CASE ");
				content.append("          WHEN (isnull(selftab.pk_accountingbook,'~')='~') ");
				content.append("             THEN  '"+ this.getAssinfovos()[i].getPk_checktype() +"' ||  othertab."+ this.getAssinfovos()[i].getFieldName());
				content.append("          ELSE  '"+ this.getAssinfovos()[i].getPk_checktype() +"' ||   selftab."+ this.getAssinfovos()[i].getFieldName());
				switch (i) {
				case 0: {
					content.append("       END  " + ResultDetailTabVO.TYPEVALUE1 );
					break;
				}
				case 1: {
					content.append("       END  " + ResultDetailTabVO.TYPEVALUE2 );
					break;
				}
				case 2: {
					content.append("       END  " + ResultDetailTabVO.TYPEVALUE3 );
					break;
				}
				case 3: {
					content.append("       END  " + ResultDetailTabVO.TYPEVALUE4 );
					break;
				}
				case 4: {
					content.append("       END  " + ResultDetailTabVO.TYPEVALUE5 );
					break;
				}
				case 5: {
					content.append("       END  " + ResultDetailTabVO.TYPEVALUE6 );
					break;
				}
				case 6: {
					content.append("       END  " + ResultDetailTabVO.TYPEVALUE7 );
					break;
				}
				case 7: {
					content.append("       END  " + ResultDetailTabVO.TYPEVALUE8 );
					break;
				}
				}
				fields[i]= content.toString();
			}
			result=GlTools.getFormValuesFromStrArray(fields);
		}
		return result;
	}
	
	public String[] getSumBalanceResultQryFields(){
		String[] result=null;
		if(this.isNeedAss()){
			String[] fields=new String[this.getAssinfovos().length];
			for(int i=0;i<this.getAssinfovos().length;i++){
				switch (i) {
				case 0: {
					fields[i]= ResultDetailTabVO.TYPEVALUE1;
					break;
				}
				case 1: {
					fields[i]= ResultDetailTabVO.TYPEVALUE2;
					break;
				}
				case 2: {
					fields[i]= ResultDetailTabVO.TYPEVALUE3;
					break;
				}
				case 3: {
					fields[i]= ResultDetailTabVO.TYPEVALUE4;
					break;
				}
				case 4: {
					fields[i]= ResultDetailTabVO.TYPEVALUE5;
					break;
				}
				case 5: {
					fields[i]= ResultDetailTabVO.TYPEVALUE6;
					break;
				}
				case 6: {
					fields[i]= ResultDetailTabVO.TYPEVALUE7;
					break;
				}
				case 7: {
					fields[i]= ResultDetailTabVO.TYPEVALUE8;
					break;
				}
				}
				
			}
			result=fields;
		}
		
		return result;
	}
	
	
	
	public String getContrastReportBuildAssFieldSql(){
		String result="";
		if(this.isNeedAss()){
			String[] fields=new String[this.getAssinfovos().length];
			for(int i=0;i<this.getAssinfovos().length;i++){
				StringBuffer content=new StringBuffer();
				content.append(" CASE ");
				content.append("          WHEN (isnull(selftab.pk_accountingbook,'~')='~') ");
				content.append("             THEN  '"+ this.getAssinfovos()[i].getPk_checktype() +"' ||  othertab."+ this.getAssinfovos()[i].getFieldName());
				content.append("          ELSE  '"+ this.getAssinfovos()[i].getPk_checktype() +"' ||   selftab."+ this.getAssinfovos()[i].getFieldName());
				switch (i) {
				case 0: {
					content.append("       END  " + ContrastReportSubVO.TYPEVALUE1 );
					break;
				}
				case 1: {
					content.append("       END  " + ContrastReportSubVO.TYPEVALUE2 );
					break;
				}
				case 2: {
					content.append("       END  " + ContrastReportSubVO.TYPEVALUE3 );
					break;
				}
				case 3: {
					content.append("       END  " + ContrastReportSubVO.TYPEVALUE4 );
					break;
				}
				case 4: {
					content.append("       END  " + ContrastReportSubVO.TYPEVALUE5 );
					break;
				}
				case 5: {
					content.append("       END  " + ContrastReportSubVO.TYPEVALUE6 );
					break;
				}
				case 6: {
					content.append("       END  " + ContrastReportSubVO.TYPEVALUE7 );
					break;
				}
				case 7: {
					content.append("       END  " + ContrastReportSubVO.TYPEVALUE8 );
					break;
				}
				}
				fields[i]= content.toString();
			}
			result=GlTools.getFormValuesFromStrArray(fields);
		}
		return result;
	}
	
	
	public String[] getContrastReportBuildFields(){
		String[] result=null;
		if(this.isNeedAss()){
			String[] fields=new String[this.getAssinfovos().length];
			for(int i=0;i<this.getAssinfovos().length;i++){
				switch (i) {
				case 0: {
					fields[i]= ContrastReportSubVO.TYPEVALUE1;
					break;
				}
				case 1: {
					fields[i]= ContrastReportSubVO.TYPEVALUE2;
					break;
				}
				case 2: {
					fields[i]= ContrastReportSubVO.TYPEVALUE3;
					break;
				}
				case 3: {
					fields[i]= ContrastReportSubVO.TYPEVALUE4;
					break;
				}
				case 4: {
					fields[i]= ContrastReportSubVO.TYPEVALUE5;
					break;
				}
				case 5: {
					fields[i]= ContrastReportSubVO.TYPEVALUE6;
					break;
				}
				case 6: {
					fields[i]= ContrastReportSubVO.TYPEVALUE7;
					break;
				}
				case 7: {
					fields[i]= ContrastReportSubVO.TYPEVALUE8;
					break;
				}
				}
				
			}
			result=fields;
		}
		
		return result;
	}
	
	public String getInnerJoinTabSql(String tabName){
		String result="";
		if(this.isNeedAss()){
			try {
				result=	( (nc.itf.fipub.freevalue.IFreevalueReportQry) NCLocator.getInstance().lookup(nc.itf.fipub.freevalue.IFreevalueReportQry.class.getName())).formSqlByAssvos(tabName, this.getAssvos(),Module.GL);
			} catch (ComponentException e) {
				// TODO Auto-generated catch block
				nc.bs.logging.Logger.error(e.getMessage(),e);
			} catch (BusinessException e) {
				// TODO Auto-generated catch block
				nc.bs.logging.Logger.error(e.getMessage(),e);
			}
		}
		
	    return result;
	}
	
	
	public String getJoinTabFieldSql(String tabname,String joinTabname){
		String result="";
		if(this.isNeedAss()){
		    StringBuffer content=new StringBuffer();
			for(int i=0;i<this.getAssinfovos().length;i++){
				content.append( " and  " + tabname+"."+ this.getAssinfovos()[i].getFieldName() + "  ="+ joinTabname+  "." + this.getAssinfovos()[i].getFieldName()  );
			}
			result=content.toString();
		}else{
			return result;
		}
		
		return result;
	}



	public  boolean isNeedAss() {
		return needAss;
	}

	private void setNeedAss(boolean needAss) {
		this.needAss = needAss;
	}

	private GlAccAssinfoVO[] getAssinfovos() {
		return assinfovos;
	}

	private void setAssinfovos(GlAccAssinfoVO[] assinfovos) {
		this.assinfovos = assinfovos;
	}

	private nc.vo.glcom.ass.AssVO[] getAssvos() {
		return assvos;
	}

	private void setAssvos(nc.vo.glcom.ass.AssVO[] assvos) {
		if(assvos != null && assvos.length>0) {
			List<AssVO> assVOList = new ArrayList<AssVO>();
			for (AssVO assVO : assvos) {
				String pk_Checkvalue = assVO.getPk_Checkvalue();
				if(!StringUtils.isEmpty(pk_Checkvalue) && pk_Checkvalue.contains(",")) {
					String[] values = pk_Checkvalue.split(",");
					for (String value : values) {
						Object cloneObj = assVO.clone();
						AssVO cloneAssVo = (AssVO) cloneObj;
						cloneAssVo.setPk_Checkvalue(value);
						assVOList.add(cloneAssVo);
					}
				}else {
					assVOList.add(assVO);
				}
			}
			assvos = assVOList.toArray(new AssVO[0]);
		}
		this.assvos = assvos;
	}

	

}
