package nc.ms.tb.rule;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import antlr.Token;
import antlr.TokenStreamException;

import nc.itf.tb.rule.fmlset.ISingleSchema;
import nc.ms.tb.control.CtrlRuleCTL;
import nc.ms.tb.formula.script.core.parser.TbbLexer;
import nc.ms.tb.rule.fmlset.FormulaParser;
import nc.vo.mdm.cube.DataCell;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;
import nc.vo.tb.formula.DimFormulaVO;
import nc.vo.tb.querycdt.DataType;
import nc.vo.tb.rule.IdCtrlschemeVO;


/**
 * @author songrui
 *  UFind(ctrlsys, billtype, ctrldirection, ��������/�����Ƿ����δ��Ч�� 
 *  startdate,enddate, datetype,accctrollflag,pk_corp,  pk_currency, pk_ncentity,
 *  fromitem, pkidx, codeidx,nameidx ctrllevel)
 * @modify yuyonga
 *  UFind(ctrlsys, billtype, ctrldirection, ctrlobj,includeuneffected,
 *  startdate,enddate, datetype,accctrollflag,pk_org,org_book,datatype,
 *  pk_currency,fromitem,pkidx,codeidx,nameidx,ctrllevel);
 */
public class SingleSchema implements ISingleSchema {
	
	DimFormulaVO dimformula;
	private NtbContext m_context = new NtbContext();
	private FormulaDimCI m_env = new FormulaDimCI();
	private DataCell cell;
//	private DimFormulaChain dimformulachain;
	private String pkRuleClass;
	public  String[] CTLSIGNARR = new String[]{">="};
	
	/**
	 * ���滻��Ĺ�ʽ���ʽ������������
	 * (1)���滻���Find()���ʽ�������߹�ʽ�����ҵ��ƻ���
	 * (2)ʵ�������UFind()���ʽ��fromitem,codeidx,ctrllevel,stridx,pk_corp,
	 * */
	
	
	/**
	 * ��Ҫʵ�����Ķ����ڿ��Ʒ��������ú����������������У���Ҫʵ����2�θö���
	 * 1.��Ӧ��ģ��������
	 * 2.�ڹ�ʽ���䵽��Ԫ���� 
	 * */
	String formulaExpress;
    

	/**
	 * 
	 * */
	public SingleSchema(String formulaExpress){
		instanceSchema(formulaExpress);
	}
	
	public SingleSchema(String formulaExpress,String pk_ruleClass){
		pkRuleClass = pk_ruleClass;
		instanceSchema(formulaExpress);
	}
	/**
	 * @param dimformula ,formulaExpress,cell
	 * 
	 *ȡ�ƻ���
	 */
	public SingleSchema(String formulaExpress,DataCell cell){
		this.cell = cell;
		instanceSchema(formulaExpress);
		instanceProperty(cell);
	}
	
	public SingleSchema(String formulaExpress,DataCell cell,String pk_ruleClass){
		this.cell = cell;
		pkRuleClass = pk_ruleClass;
		instanceSchema(formulaExpress);
		instanceProperty(cell);
	}
	
	public SingleSchema(String formulaExpress,DataCell cell,String pk_ruleClass,boolean isProperty){
		this.cell = cell;
		pkRuleClass = pk_ruleClass;
		instanceSchema(formulaExpress);
		if(isProperty)
		  instanceProperty(cell);
	}
	
	public void instanceSchema(String formulaExpress){
		this.formulaExpress = formulaExpress;
	}

	public void instanceProperty(DataCell cell){
		try{
//			dimformulachain = new DimFormulaChain(cell,pkRuleClass);
//			m_env = dimformulachain.getFormulaCI();
//			m_context.addProperty("��������", m_env);  
		}catch (Exception e) {
			NtbLogger.printException(e);
		}
	}
	
	
	public String getExpressFindOrSmFindFormula(IdCtrlschemeVO[] vos)throws Exception{
		//Find()
		int count = 0;
		//UFind()
		int count1 = 0;
		//SMFind()
		int count2 = 0;
		//PREFIND()
		int count3 = 0;
		
	    ArrayList<IdCtrlschemeVO> ufindvo = new ArrayList<IdCtrlschemeVO> ();
	    ArrayList<IdCtrlschemeVO> prefindvo = new ArrayList<IdCtrlschemeVO> (); 
	    
	    for(int n=0 ; n<(vos==null?0:vos.length) ; n++){
	    	IdCtrlschemeVO vo =  vos[n];
	    	if(vo.getVarno().indexOf("var")>=0){
	    		ufindvo.add(vo);
	    	}
	    	if(vo.getVarno().indexOf("rar")>=0){
	    		prefindvo.add(vo);
	    	}
	    }
	    formulaExpress = formulaExpress.replace("\'", "\"");
	    formulaExpress = CtrlRuleCTL.deposeRuleExpress(formulaExpress);
	    formulaExpress = formulaExpress.replace("\"", "\'");
	    formulaExpress = FormulaParser.getNoNameExp(formulaExpress);
		String varexpress = FormulaParser.parseToVarExpress(formulaExpress);
		String temp = varexpress;

		while(varexpress.indexOf("USB@@")>-1){
			varexpress = varexpress.replaceFirst("USB@@", ufindvo.get(count1).getVarno());
			temp = temp.replaceFirst("USB@@", ufindvo.get(count1).getVarno());
			count1++;
		}
		while(varexpress.indexOf("BSU@@")>-1){
			varexpress = varexpress.replaceFirst("BSU@@", prefindvo.get(count3).getVarno());
			temp = temp.replaceFirst("BSU@@", prefindvo.get(count3).getVarno());
			count3++;
		}
		
		return varexpress;
	
	}
	
	public String getExpressFindOrSmFindFormulaVar(IdCtrlschemeVO[] vos)throws Exception{
		//Find()
		int count = 0;
		//UFind()
		int count1 = 0;
		//SMFind()
		int count2 = 0;
		//PREFIND()
		int count3 = 0;
		 
	    ArrayList<IdCtrlschemeVO> ufindvo = new ArrayList<IdCtrlschemeVO> ();
	    ArrayList<IdCtrlschemeVO> prefindvo = new ArrayList<IdCtrlschemeVO> (); 
	    
	    for(int n=0 ; n<(vos==null?0:vos.length) ; n++){
	    	IdCtrlschemeVO vo =  vos[n];
	    	if(vo.getVarno().indexOf("var")>=0){
	    		ufindvo.add(vo);
	    	}
	    	if(vo.getVarno().indexOf("rar")>=0){
	    		prefindvo.add(vo);
	    	}
	    }
	    formulaExpress = formulaExpress.replace("\'", "\"");
	    formulaExpress = CtrlRuleCTL.deposeRuleExpress(formulaExpress);
	    formulaExpress = formulaExpress.replace("\"", "\'");
	    formulaExpress = FormulaParser.getNoNameExp(formulaExpress);
		String varexpress = FormulaParser.parseToVarExpress(formulaExpress);
		String temp = varexpress;
		
		 while(varexpress.indexOf("USB@@")>-1){
			   varexpress = varexpress.replaceFirst("USB@@", ufindvo.get(count1).getRundata()==null?"0":String.valueOf(ufindvo.get(count1).getRundata()));
			   temp = temp.replaceFirst("USB@@", ufindvo.get(count1).getRundata()==null?"0":String.valueOf(ufindvo.get(count1).getRundata()));   
			   count1++;
			  }
			  while(varexpress.indexOf("BSU@@")>-1){
			   varexpress = varexpress.replaceFirst("BSU@@", prefindvo.get(count3).getReadydata()==null?"0":String.valueOf(prefindvo.get(count3).getReadydata()));
			   temp = temp.replaceFirst("BSU@@", prefindvo.get(count3).getReadydata()==null?"0":String.valueOf(prefindvo.get(count3).getReadydata()));
			   count3++;
			  }
		
		return varexpress;
	
	}
	
	/**
	 * ��Ҫ���ǹ�ʽ�����Find()��SMFind()��ʽ�Ĺ�ʽ
	 * eg.(Find()+SMFind())*100%>UFind()+Find()+SMFind()
	 * **/
	public String[] getExpressFormula(IdCtrlschemeVO[] vos)throws Exception{
		//Find()
		int count = 0;
		//UFind()
		int count1 = 0;
		//SMFind()
		int count2 = 0;
		//PREFIND()
		int count3 = 0;
		
	    ArrayList<IdCtrlschemeVO> ufindvo = new ArrayList<IdCtrlschemeVO> ();
	    ArrayList<IdCtrlschemeVO> prefindvo = new ArrayList<IdCtrlschemeVO> ();

	    for(int n=0 ; n<(vos==null?0:vos.length) ; n++){
	    	IdCtrlschemeVO vo =  vos[n];
	    	if(vo.getVarno().indexOf("var")>=0){
	    		ufindvo.add(vo);
	    	}
	    	if(vo.getVarno().indexOf("rar")>=0){
	    		prefindvo.add(vo);
	    	}
	    }

        HashMap<String,Object> varMap = FormulaParser.parseToSingleSchemaExpress(formulaExpress);
		String varexpress = (String)varMap.get("STR");//FormulaParser.parseToVarExpress(formulaExpress);
		String temp = varexpress;

		while(varexpress.indexOf("USB@@")>-1){
			varexpress = varexpress.replaceFirst("USB@@", ufindvo.get(count1).getVarno());
			temp = temp.replaceFirst("USB@@", ufindvo.get(count1).getVarno());
			count1++;
		}
		while(varexpress.indexOf("BSU@@")>-1){
			varexpress = varexpress.replaceFirst("BSU@@", prefindvo.get(count3).getVarno());
			temp = temp.replaceFirst("BSU@@", prefindvo.get(count3).getVarno());
			count3++;
		}

		String[] planlist = FormulaParser.parseFormulaToPlanPk(temp);
		StringBuffer buffer = new StringBuffer();
		for(int i=0;i<planlist.length;i++){
			buffer.append(planlist[i]);
			if(i!=planlist.length-1)
				buffer.append(";");
		}
		String[] ss = new String[2];
		ss[0] = varexpress;
		ss[1] = buffer.toString();
		return ss;
	}
	
	public String[] getExpressFormula(IdCtrlschemeVO[] vos,boolean isZeroCtrl)throws Exception{
		//Find()
		int count = 0;
		//UFind()
		int count1 = 0;
		//SMFind()
		int count2 = 0;
		//PREFIND()
		int count3 = 0;
		
	    ArrayList<IdCtrlschemeVO> ufindvo = new ArrayList<IdCtrlschemeVO> ();
	    ArrayList<IdCtrlschemeVO> prefindvo = new ArrayList<IdCtrlschemeVO> ();
	    
	    for(int n=0 ; n<(vos==null?0:vos.length) ; n++){
	    	IdCtrlschemeVO vo =  vos[n];
	    	if(vo.getVarno().indexOf("var")>=0){
	    		ufindvo.add(vo);
	    	}
	    	if(vo.getVarno().indexOf("rar")>=0){
	    		prefindvo.add(vo);
	    	}
	    }

		String varexpress = FormulaParser.parseToVarExpress(formulaExpress,isZeroCtrl);
		String temp = varexpress;
		
		//value��˳���������Find()��˳�򱣳�һ�£�����ģ�����λ�ò�����������
//		String[][] value_SMFind = getPlandata_SMFind(formulaExpress);
//		String[][] value_Find = getPlandata_Find(formulaExpress);
		while(varexpress.indexOf("X@@")>-1){
			varexpress = varexpress.replaceFirst("X@@", "0");
			temp = temp.replaceFirst("X@@","0");
			count++;
			NtbLogger.print(varexpress);
			NtbLogger.print(temp);
		}
		while(varexpress.indexOf("Y@@")>-1){
			varexpress = varexpress.replaceFirst("Y@@", "0");
			temp = temp.replaceFirst("Y@@","0");
			count2++;
			NtbLogger.print(varexpress);
			NtbLogger.print(temp);
		}
		while(varexpress.indexOf("USB@@")>-1){
			varexpress = varexpress.replaceFirst("USB@@", ufindvo.get(count1).getVarno());
			temp = temp.replaceFirst("USB@@", ufindvo.get(count1).getVarno());
			count1++;
			NtbLogger.print(varexpress);
			NtbLogger.print(temp);
		}
		while(varexpress.indexOf("BSU@@")>-1){
			varexpress = varexpress.replaceFirst("BSU@@", prefindvo.get(count3).getVarno());
			temp = temp.replaceFirst("BSU@@", prefindvo.get(count3).getVarno());
			count3++;
			NtbLogger.print(varexpress);
			NtbLogger.print(temp);
		}
		String[] planlist = FormulaParser.parseFormulaToPlanPk(temp);
		StringBuffer buffer = new StringBuffer();
		for(int i=0;i<planlist.length;i++){
			buffer.append(planlist[i]);
			if(i!=planlist.length-1)
				buffer.append(";");
		}
		String[] ss = new String[2];
		ss[0] = varexpress;
		ss[1] = buffer.toString();
		return ss;
	}
	
	/**
	 * @param ufindexpress:UFind()+UFind()
	 * ����飬���⣬����UFind()���ʽ
	 * 
	 * */
	public String parseUfindExpress(String[] ufindexpress) throws BusinessException{
		try {
			int count = 0;
			String varexpress = FormulaParser.parseToFindSrc(formulaExpress);
			while(varexpress.indexOf("UFIND_USB")>-1){
				varexpress = varexpress.replaceFirst("UFIND_USB", ufindexpress[count]);
				count++;
			}
			return varexpress;
		} catch (Exception e) {
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052")/*δ֪�쳣*/);
		}
	}
	
	public String parsePreFindExpress(String[] ufindexpress) throws BusinessException{
		try {
			int count = 0;
			String varexpress = FormulaParser.parseToPreFindSrc(formulaExpress);
			while(varexpress.indexOf("PREFIND_BSU")>-1){
				varexpress = varexpress.replaceFirst("PREFIND_BSU", ufindexpress[count]);
				count++;
			}
			return varexpress;
		} catch (Exception e) {
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052")/*δ֪�쳣*/);
		}
	}
	
	public String parsePreFindAndUfindExpress(String[] ufindexpress) throws BusinessException{
		try {
			int count = 0;
			String varexpress = FormulaParser.parseToPreFindSrc(formulaExpress);
			while(varexpress.indexOf("PREFIND_BSU")>-1){
				varexpress = varexpress.replaceFirst("PREFIND_BSU", ufindexpress[count]);
				count++;
			}
			return varexpress;
		} catch (Exception e) {
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052")/*δ֪�쳣*/);
		}
	}
	
	public String parsePrefindAndUfindExpress(String[] ufindexpress,String[] prefindexpress) throws BusinessException{
		try {
			int count = 0;
			int m_count = 0;
			String varexpress = FormulaParser.parseToFindSrc(formulaExpress);
			while(varexpress.indexOf("UFIND_USB")>-1){
				varexpress = varexpress.replaceFirst("UFIND_USB", ufindexpress[count]);
				count++;
			}
			varexpress = FormulaParser.parseToPreFindSrc(varexpress);
			while(varexpress.indexOf("PREFIND_BSU")>-1){
				varexpress = varexpress.replaceFirst("PREFIND_BSU", prefindexpress[m_count]);
				m_count++;
			}
			return varexpress;
		} catch (Exception e) {
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052")/*δ֪�쳣*/);
		}
	}
	
	public String parsePrefindAndUfindExpConsiderSign(String[] ufindExps, String[] prefindExps) {
		
		//��ѡ�����ֽ���ʽʱ������UFIND��PREFINDǰ�ķ��ţ��������"+"�ŵĻ��滻ʱע�����С����
		//��������,Ҫ��UFIND��REFINDǰ��Ҫ�������հ��ַ�
		
		//������һ���޸�
		//����Ҽ����ŵĻ���������������Ŀ���Ԥ���������������Ų��ñ��
		String vareExpress = FormulaParser.parseToFindSrc(formulaExpress);
		
		int index = 0, count0 = 0, count1 = 0;
		while((index = vareExpress.indexOf("UFIND_USB")) > -1) {
			char beforeVar = vareExpress.charAt(index - 1);
			if(beforeVar == '-' && index != 0) {
				
				String replaceStr = FormulaParser.parseToFindSrc(ufindExps[count0]);
				
				boolean isOneUfind = true;
				int interIndex = 0;
				while((interIndex = replaceStr.indexOf("UFIND_USB", interIndex)) > -1) {
					if(interIndex != 0)
						isOneUfind = false;
					interIndex++;
				}
				if(!isOneUfind)
					vareExpress = vareExpress.replaceFirst("UFIND_USB", "(" + ufindExps[count0] + ")");
				else
					vareExpress = vareExpress.replaceFirst("UFIND_USB", ufindExps[count0]);
			} else
				vareExpress = vareExpress.replaceFirst("UFIND_USB", ufindExps[count0]);
			count0++;
		}
		
		vareExpress = FormulaParser.parseToPreFindSrc(vareExpress);
		while((index = vareExpress.indexOf("PREFIND_BSU")) > -1) {
			char beforeVar = vareExpress.charAt(index - 1);
			if(beforeVar == '-' &&  index != 0) {
				String replaceStr = FormulaParser.parseToPreFindSrc(prefindExps[count1]);
				
				boolean isOnePrefind = true;
				int interIndex = 0;
				while((interIndex = replaceStr.indexOf("PREFIND_BSU", interIndex)) > -1) {
					if(interIndex != 0)
						isOnePrefind = false;
					interIndex++;
				}
				if(!isOnePrefind)
					vareExpress = vareExpress.replaceFirst("PREFIND_BSU", "(" + prefindExps[count1] + ")");
				else
					vareExpress = vareExpress.replaceFirst("PREFIND_BSU", prefindExps[count1]);
			} else
				vareExpress = vareExpress.replaceFirst("PREFIND_BSU", prefindExps[count1]);
			count1++;
		}
		return vareExpress;
	}

	/**
	 * 
	 * */
	public String[] getUFind(){
		if(formulaExpress ==null){
	       return null;
		}else{
		   return FormulaParser.parseFormulaToUFind(formulaExpress);
	    }
	}
	
	public String[] getPREUFind(){
		if(formulaExpress == null){
			return null;
		}else{
		    return FormulaParser.parseFormulaToPreUFind(formulaExpress);
		}
	}

	/**
	 * 
	 * */
	public String[] getFind(){
		return FormulaParser.parseFormulaToFind(formulaExpress);
	}
	//����getFind()����ʽ������
	public String[] getFind(String src){
		return FormulaParser.parseFormulaToFind(src);
	}
	/**
	 * 
	 * */
	public String getSMFind(){
		return FormulaParser.parseFormulaToSMFind(formulaExpress);
	}
	
	public String[] getSMFinds(String express){
		return FormulaParser.parseFormulaToSMFinds(express);
	}
	public UFDouble getRunData(){
		
		return null;
	}
	/**
	 * ͨ��Find()��ʽ�����õ��ƻ���
	 * �鷽���ǰ�SMFind()����ΪFind()+Find()����ʽ������÷�����
	 * 
	 * planValue�ֶ�ֻ���ڵ�������鷽�������飬����ֻ��Ҫ����Find()���ʽ���������
	 * 
	 * songrui modify 2008.12.11
	 * */
//	public String getPlandata()throws Exception{
//		//Find()
//		int count = 0;
//		int count1 = 0;
//		String express = getFormulaExpress();
//		//����������鷽���۽�SMFIND()�͵�����鷽����FIND()
//		express = express.substring(0, express.indexOf("*"));
//		String varexpress = FormulaParser.parseToVarExpress(express);
//		
//		String[][] value_SMFind = getPlandata_SMFind(express);
//		
//		String[][] value_Find = getPlandata_Find(express);
//		
//		Compiler compiler = new Compiler(varexpress,null,null);
//		Vector words = compiler.getWords();
//		
//		while(varexpress.indexOf("X@@")>-1){
//			varexpress = varexpress.replaceFirst("X@@", value_Find[count]);
//			count++;
//			NtbLogger.print2(varexpress);
//		}
//		
//		while(varexpress.indexOf("Y@@")>-1){
//			varexpress = varexpress.replaceFirst("Y@@", value_SMFind[count1]);
//			count1++;
//			NtbLogger.print2(varexpress);
//		}
//		
//		return varexpress;
//	}
	

	
	/**
	 * songrui modify 2008.12.11
	 * */
//	public UFDouble getPlanValue() throws Exception{
//		try{
//			String src = getPlandata();
//			UFDouble value = calcPlanValue(src);
//			return value;			
//		}catch (Exception e) {
//			
//			throw e;
//		}
//	
//	
//	}

	
	public String getFormulaExpress(){
		return this.formulaExpress;
	}
	public String getConvertMonthFrequenceExpress(String express,String dm_quartor){
		//Find()
		int count = 0;
		//UFind()
		int count1 = 0;
		//SMFind()
		int count2 = 0;
		
		/**
		 * ֧�ָ��ӵĹ�ʽ��ʽ
		 * */
		//(FIND()+SMFIND())*100%>UFIND()----->(X+Y)*100%>USB@@
		String varexpress = FormulaParser.parseToVarExpress(express);
		//UFind()
		String[] ufind = FormulaParser.parseFormulaToUFind(express);

		while(varexpress.indexOf("USB@@")>-1){//�滻UFind()
			varexpress = varexpress.replaceFirst("USB@@", ufind[count1]);
			count1++;
		}
		return varexpress;
	}



	public UFDouble getControlpercent(String ss){
		double value = 0.0;

		String regex = "(\\d+\\d+)%";
		Pattern p = Pattern.compile(regex);
		Matcher match = p.matcher(ss);
		while(match.find()){
			String controlpercent = match.group(1);
			return new UFDouble(Double.valueOf(controlpercent));
		}
		return null;
	}
	
	public String getControlsign(){
		int index = -1;
		if(formulaExpress.indexOf(CTLSIGNARR[0])>-1){//>=
			index = formulaExpress.indexOf(CTLSIGNARR[0]);
			formulaExpress = formulaExpress.substring(index, index+2);
			return formulaExpress;
		}
		return null;
	}
	
	public UFDouble calcPlanValue(String src) throws Exception{
		nc.vo.bank_cvp.compile.datastruct.ArrayValue result = null;
		UFDouble value = null;
		if(src == null||"".equals(src))
			return null;
		result = nc.ui.bank_cvp.formulainterface.RefCompilerClient.getExpressionResult(src, m_context);
		Object tmpResult = result.getValue();
		if (tmpResult != null){
			 value = new UFDouble((BigDecimal)tmpResult);
		}	
		return value;
	}
	
}
