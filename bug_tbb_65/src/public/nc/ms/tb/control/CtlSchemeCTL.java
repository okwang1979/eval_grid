package nc.ms.tb.control;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.pf.pub.PfDataCache;
import nc.itf.bd.config.mode.IBDMode;
import nc.itf.mdm.cube.IDataSetService;
import nc.itf.mdm.dim.IDimManager;
import nc.itf.org.IOrgMetaDataIDConst;
import nc.itf.tb.control.CtlSchemeEnum;
import nc.itf.tb.control.IBusiSysExecDataProvider;
import nc.itf.tb.control.IBusiSysReg;
import nc.itf.tb.control.IFormulaFuncName;
import nc.itf.tb.control.OutEnum;
import nc.itf.tb.control.manage.ICtrlSchemeConvertor;
import nc.itf.tb.control.manage.ICtrlSchemeQuery;
import nc.itf.tb.rule.INodeTypeConst;
import nc.itf.tb.sysmaintain.BdContrastCache;
import nc.itf.tb.sysmaintain.BusiSysReg;
import nc.itf.uap.pa.IPreAlertConfigService;
import nc.ms.mdm.convertor.IStringConvertor;
import nc.ms.mdm.convertor.StringConvertorFactory;
import nc.ms.mdm.cube.CubeServiceGetter;
import nc.ms.mdm.dim.DimServiceGetter;
import nc.ms.mdm.dim.NtbSuperServiceGetter;
import nc.ms.mdm.dim.TimeDimTool;
import nc.ms.tb.formula.context.DefaultFormulaContext;
import nc.ms.tb.formula.context.IFormulaContext;
import nc.ms.tb.formula.core.cutcube.WhereDataCellInfo;
import nc.ms.tb.formula.script.Calculator;
import nc.ms.tb.formula.script.core.parser.Expression;
import nc.ms.tb.formula.script.core.parser.TbbLexer;
import nc.ms.tb.formula.script.core.parser.TbbParser;
import nc.ms.tb.pub.IDimPkConst;
import nc.ms.tb.pubutil.DateUtil;
import nc.ms.tb.pubutil.UtilServiceGetter;
import nc.ms.tb.rule.BracketsHelper;
import nc.ms.tb.rule.CtlSchemeServiceGetter;
import nc.ms.tb.rule.FormulaDimCI;
import nc.ms.tb.rule.NtbContext;
import nc.ms.tb.rule.RuleServiceGetter;
import nc.ms.tb.rule.SingleSchema;
import nc.ms.tb.rule.SubLevelOrgGetter;
import nc.ms.tb.rule.fmlset.FormulaCTL;
import nc.ms.tb.rule.fmlset.FormulaMember;
import nc.ms.tb.rule.fmlset.FormulaParser;
import nc.ms.tb.rule.ruletype.IPKRuleConst;
import nc.ms.tb.task.TbTaskCtl;
import nc.ms.tb.task.TbTaskServiceGetter;
import nc.ms.tb.task.data.TaskDataCtl;
import nc.ms.tb.task.data.TaskDataModel;
import nc.pubitf.accperiod.AccountCalendar;
import nc.pubitf.bbd.CurrtypeQuery;
import nc.pubitf.bd.accessor.GeneralAccessorFactory;
import nc.pubitf.bd.accessor.IGeneralAccessor;
import nc.pubitf.org.IAccountingBookPubService;
import nc.pubitf.org.IFinanceOrgPubService;
import nc.pubitf.org.ILiabilityBookPubService;
import nc.pubitf.rbac.IFunctionPermissionPubService;
import nc.pubitf.uapbd.IAccountPubService;
import nc.vo.bd.accessor.IBDData;
import nc.vo.bd.account.AccountVO;
import nc.vo.mdm.cube.CubeDef;
import nc.vo.mdm.cube.DataCell;
import nc.vo.mdm.cube.DataCellValue;
import nc.vo.mdm.cube.DimSectionTuple;
import nc.vo.mdm.cube.DimVector;
import nc.vo.mdm.cube.ICubeDataSet;
import nc.vo.mdm.dim.DimHierarchy;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.IDimDefPKConst;
import nc.vo.mdm.dim.IDimHierarchyPkConst;
import nc.vo.mdm.dim.IDimLevelPKConst;
import nc.vo.mdm.dim.IDimMemberPkConst;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.mdm.pub.StringUtil;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.org.FinanceOrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.billtype.BilltypeVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.tb.control.ControlBillType;
import nc.vo.tb.control.ControlObjectType;
import nc.vo.tb.control.ConvertToCtrlSchemeVO;
import nc.vo.tb.control.CtlAggregatedVO;
import nc.vo.tb.control.CtrlInfoMacroConst;
import nc.vo.tb.control.CtrlSchemeVO;
import nc.vo.tb.control.DataContrastVO;
import nc.vo.tb.control.DimRelUapVO;
import nc.vo.tb.control.IdBdcontrastVO;
import nc.vo.tb.control.IdCtrlschmBVO;
import nc.vo.tb.control.IdCtrlschmVO;
import nc.vo.tb.control.IdFlexAreaTypeEnum;
import nc.vo.tb.control.IdSysregVO;
import nc.vo.tb.control.exception.CheckSchmException;
import nc.vo.tb.form.MdWorkbook;
import nc.vo.tb.form.iufo.TbIufoConst;
import nc.vo.tb.formula.DimFormulaMVO;
import nc.vo.tb.formula.DimFormulaVO;
import nc.vo.tb.formula.FormulaDataCell;
import nc.vo.tb.ntbenum.AccumulateEnum;
import nc.vo.tb.ntbenum.CtrlTypeEnum;
import nc.vo.tb.obj.NtbParamVO;
import nc.vo.tb.prealarm.IdAlarmDimVectorVO;
import nc.vo.tb.prealarm.IdAlarmschemeVO;
import nc.vo.tb.pubutil.BusiTermConst;
import nc.vo.tb.rule.AllotFormulaVo;
import nc.vo.tb.rule.BusiRuleVO;
import nc.vo.tb.rule.IRuleClassConst;
import nc.vo.tb.rule.IdCtrlInfoVO;
import nc.vo.tb.rule.IdCtrlformulaVO;
import nc.vo.tb.rule.IdCtrlschemeVO;
import nc.vo.tb.rule.IdFlexElementVO;
import nc.vo.tb.rule.IdFlexZoneVO;
import nc.vo.tb.task.ITaskConst;
import nc.vo.tb.task.ITaskStatus;
import nc.vo.tb.task.MdTask;
import nc.vo.uap.rbac.profile.IFunctionPermProfile;

import com.ufsoft.table.Cell;

public class CtlSchemeCTL { 

//	private static HashMap<String, String> systemMap = new HashMap<String, String>();
//	private static HashMap<String, String> systemOrgMap = new HashMap<String, String>();
//	private static long cacheTime = 300*1000; // 5分钟刷新
//	private static long lastInitTime = -1;
	private final static String text_seperator = "#";

	private static Map<String, Expression> parserMap = new HashMap<String, Expression>();

	public static Map<String, Expression> getParserMap() {
		return parserMap;
	}

	public static String getSysOrgByCode(String sysCode) {
		return BusiTermConst.getSysOrgByCode(sysCode);
//		getSystemMap();
//		return systemOrgMap.get(sysCode);
	}
	public static String getSysNameByCode(String sysCode) {
		return BusiTermConst.getSysNameByCode(sysCode);
//		String rtn = getSystemMap().get(sysCode);
//		if (rtn == null) {
//			lastInitTime = -1;
//			rtn = getSystemMap().get(sysCode);
//		}
//		return rtn;
	}

//	private static synchronized HashMap<String, String> getSystemMap() {
//		if (System.currentTimeMillis() - lastInitTime > cacheTime) {
//			systemMap.clear();
//			systemOrgMap.clear();
//			try {
//				SuperVO[] vos = NtbSuperServiceGetter.getINtbSuper().queryAll(BusiSystemVO.class);
//				if (vos != null) {
//					for (int i=0; i<vos.length; i++) {
//						systemMap.put(((BusiSystemVO)vos[i]).getSystemcode(), ((BusiSystemVO)vos[i]).getSystemname());
//						systemOrgMap.put(((BusiSystemVO)vos[i]).getSystemcode(), ((BusiSystemVO)vos[i]).getPk_orgstruct());
//					}
//				}
//				if (!systemMap.containsKey(IBusiTermConst.SYS_TB)) {
//					systemMap.put(IBusiTermConst.SYS_TB, NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01420ctl_000140")/*全面预算*/);
//				}
//			} catch (BusinessException e) {
//				NtbLogger.printException(e);
//			}
//			lastInitTime = System.currentTimeMillis();
//		}
//		return systemMap;
//	}

	public static HashMap<String,ArrayList<String>> reloadZeroCtrlScheme() throws BusinessException {
		try{
			HashMap<String,ArrayList<String>> map = CtlSchemeServiceGetter.getICtlScheme().reloadZeroCtrlScheme();
			return map;
		}catch (BusinessException e) {
			throw e;
		}
		catch(Exception ex){
			NtbLogger.error(ex);
			throw new BusinessException(ex.getMessage(),ex);
		}
	}

	/**
	 * 控制系统
	 * */
	public static IdSysregVO[] getRegVOs() throws Exception{
		return BusiSysReg.getSharedInstance().getAllSysVOs();
	}


    public static String getDimMCode(DataCell datacell){
		IStringConvertor cvt = StringConvertorFactory.getConvertor(DimVector.class);
		String pk_cell = cvt.convertToString(datacell.getDimVector());
		return pk_cell;
    }

	public static String getBdinfoType(String fromitem,String sysid) throws BusinessException{
		IdBdcontrastVO[] bdcontrasts = BdContrastCache.getNewInstance().getVoBySysid(sysid);
		StringBuffer buffer = new StringBuffer();
		String[] ss = fromitem.split(":");
		for(int i=0;i<ss.length;i++){
			boolean bFind = false;
			for(int j=0;j<bdcontrasts.length;j++){
				if(bdcontrasts[j].getAtt_fld().equals(ss[i])){
					String bdinfotype = bdcontrasts[j].getBdinfo_type();
					buffer.append(bdinfotype+":");
					bFind = true;
					break;
				}
			}
			if(!bFind){
				throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000261")/*设置的基本档案条件不存在！*/);
			}
		}

		return buffer.toString();

	}

	/**
	1：单项方案 CTL_SCHEMA_SINGLE
    2: 组方案  CTL_SCHEMA_GROUP
    3: 特殊方案 CTL_SCHEMA_COMPLEX
    4：零预算规则 CTL_SCHEMA_ZERO
    5: 控制所有公司 CTL_SCHEMA_ALL_CORP
	 **/
	public static String getCtlType(String formulaPk) throws Exception{

		DimFormulaVO vo = NtbFormulaCache.getNewInstance().getDimFormulaVOByPk(formulaPk);
		if(vo != null){
		  return vo.getPk_ruleclass();
		}else{
		  return IRuleClassConst.SCHEMA_FLEX;
		}
	}
	public static String getCtlType(DimFormulaVO vo) throws Exception{
		if(vo != null){
		  return vo.getPk_ruleclass();
		}else{
		  return IRuleClassConst.SCHEMA_FLEX;
		}
	}

	public static SingleSchema zeroSchema(DimFormulaVO d_vo,DimMember entity) throws BusinessException,Exception{
//		NtbDimFormula formula = NtbDimFormula.getInstance(d_vo);
//		String srcf = formula.getContent();  //得到公式的全部表达式

		String srcf = FormulaParser.getNoNameExp(d_vo.getFullcontent());
		SingleSchema schema = new SingleSchema(srcf,IPKRuleConst.SCHEMA_ZERO);
		DimFormulaMacro macro = new DimFormulaMacro();
		FormulaDimCI m_env = new FormulaDimCI();
		srcf = macro.getParsedFormula(m_env, srcf,d_vo.getPk_parent());
		schema.instanceSchema(srcf);
		srcf = macro.getZeroComplexParsedCorpAndCurrency(srcf,schema,entity);
		schema.instanceSchema(srcf);
		srcf = macro.getParsedZeroFormula(schema,entity);
		schema.instanceSchema(srcf);
        return schema;
	}

	public  static String[] addCtrlScheme(ArrayList<IdCtrlschemeVO> vos)throws BusinessException{
		try{
			String[] pks = CtlSchemeServiceGetter.getICtlScheme().addCtrlScheme(vos);
			return pks;
		}catch (BusinessException e) {
			throw e;
		}
		catch(Exception ex){
			NtbLogger.error(ex);
			throw new BusinessException(ex.getMessage(),ex);
		}
	}

	public  static String[] addCtrlformulas(ArrayList<IdCtrlformulaVO> vos)throws BusinessException{
		try{
			String[] pks = CtlSchemeServiceGetter.getICtlScheme().addCtrlformulas(vos);
			return pks;
		}catch (BusinessException e) {
			throw e;
		}
		catch(Exception ex){
			NtbLogger.error(ex);
			throw new BusinessException(ex.getMessage(),ex);
		}
	}

	public static void updateCtrlSchemeTable(NtbParamVO[] param) throws BusinessException {
		try{
			CtlSchemeServiceGetter.getICtlScheme().updateCtrlSchemeTable(param);
		}catch (BusinessException e) {
			throw e;
		}
		catch(Exception ex){
			NtbLogger.error(ex);
			throw new BusinessException(ex.getMessage(),ex);
		}
	}

	/**
	 *  通过系统得到取数接口
	 **/
	public static IBusiSysReg getBusiSysReg(String sys) throws BusinessException{
		BusiSysReg sysreg = BusiSysReg.getSharedInstance();
		IdSysregVO[] sysregvos = sysreg.getAllSysVOs();
		boolean isFind = false;
		IBusiSysReg sysReg = null;
        try {
			for(int i=0; i<sysregvos.length; i++){
				if(sysregvos[i].getSysid().equals(sys)){
					isFind = true;
					sysReg = (IBusiSysReg)Class.forName(sysregvos[i].getRegclass()).newInstance();
				}
			}
        }
        catch(Exception ex){
        	NtbLogger.print(ex.getMessage());
        	throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000262", null, new String[]{sys})/*初始化{0}系统失败,请检查注册相关类*/,ex);
        }
        if(!isFind)
        {
        	throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000262", null, new String[]{sys})/*初始化{0}系统失败,请检查注册相关类*/);
        }
        return sysReg;

	}

	public static ArrayList<DimFormulaVO> queryDimFormulas(ArrayList<String> pks) throws BusinessException {
		try{

			ArrayList<DimFormulaVO> vos = CtlSchemeServiceGetter.getICtlScheme().queryDimFormulas(pks);
			return vos;

		}catch (BusinessException e) {
			throw e;
		}catch (Exception e) {
			NtbLogger.error(e);
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000231", null, new String[]{e.getMessage()})/*查询语句出错:错误原因:{0}*/,e);
		}
	}

	public static ArrayList<IdCtrlformulaVO> queryCtrlFormula(String sWhere) throws BusinessException {
		try{

			ArrayList<IdCtrlformulaVO> vos = CtlSchemeServiceGetter.getICtlScheme().queryCtrlFormula(sWhere);
			return vos;

		}catch (BusinessException e) {
			throw e;
		}catch (Exception e) {
			NtbLogger.error(e);
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000231", null, new String[]{e.getMessage()})/*查询语句出错:错误原因:{0}*/,e);
		}
	}

	public static void deleteZeroCtrlScheme(ArrayList<String> pks,boolean deleteFormulaVO) throws BusinessException{
		try{
			CtlSchemeServiceGetter.getICtlScheme().deleteZeroCtrlScheme(pks,deleteFormulaVO);
		}catch (BusinessException e) {
			throw e;
		}
		catch(Exception ex){
			NtbLogger.error(ex);
			throw new BusinessException(ex.getMessage(),ex);
		}
	}

	protected static String getTextSeperator() {
		return text_seperator;
	}

	/**
	 * 简化控制方案单据类型的现实
	 * @param billtypes
	 * @return
	 */
	public static String simpleBillTypeDisp(String billtypes, int maxLength) {

		if(billtypes == null || "".equals(billtypes))
			return null;
		StringBuffer billTypeDisp = new StringBuffer();
		String[] billtypeArr = billtypes.split("#|,");

		for(int i = 0 ; i < billtypeArr.length ; i++) {
			String billtype  = billtypeArr[i];
			if(billtype.indexOf("]") > 0) {
				int index = billtype.indexOf("]");
				String billtypeName = billtype.substring(index + 1);

				int length = billTypeDisp.length();
				if((length + billtypeName.length()) > maxLength / 2) {

					int lastLen = maxLength / 2 - length - 2;
					if(lastLen < 0) {
						billTypeDisp.replace(billTypeDisp.length() - Math.abs(lastLen), billTypeDisp.length(), "..");
					} else {
						String lastBillTypeName = billtypeName.substring(0, lastLen) + "..";
						billTypeDisp.append(lastBillTypeName);
					}
					break;
				} else {
					billTypeDisp.append(billtypeName);
					if(i != billtypeArr.length - 1)
						billTypeDisp.append(",");
				}
			}
		}
		return billTypeDisp.toString();
	}

	public static String parseBillTypes(String billtyes){
		if(billtyes==null || billtyes.trim().length()==0){
			return null;
		}
		StringBuffer buffer = new StringBuffer();
        /**没有括号,表示单据格式不对*/
		if(billtyes.indexOf("]")==-1){
			buffer.append(billtyes);
		}
		/**表示有多个单据*/
		if(billtyes.indexOf(getTextSeperator())>=0){
			String[] billtypes = billtyes.split(getTextSeperator());
			for(int n=0;n<billtypes.length;n++){
				String tmp_bill = billtypes[n];

					while (tmp_bill.indexOf("]")>=0) {
						if(buffer.toString().length()==0){
							buffer.append(tmp_bill.substring(tmp_bill.indexOf("[")+1, tmp_bill.indexOf("]")));
						}else{
							buffer.append(getTextSeperator());
							buffer.append(tmp_bill.substring(tmp_bill.indexOf("[")+1, tmp_bill.indexOf("]")));

						}
						tmp_bill = tmp_bill.substring(tmp_bill.indexOf("]")+1);
					}

			}
		}else{
			while (billtyes.indexOf("]")>=0) {
				if(buffer.toString().length()==0){
					buffer.append(billtyes.substring(billtyes.indexOf("[")+1, billtyes.indexOf("]")));
				}else{
					buffer.append(",");
					buffer.append(billtyes.substring(billtyes.indexOf("[")+1, billtyes.indexOf("]")));

				}
				billtyes = billtyes.substring(billtyes.indexOf("]")+1);
			}
		}
		return buffer.toString();
	}

	public static ArrayList<IdBdcontrastVO> getMainOrgBdcontrastVO(String sysid,String billtype,boolean isDefault) throws Exception{
		ArrayList<IdBdcontrastVO> vos = new ArrayList<IdBdcontrastVO> ();
		IBusiSysReg reg = CtlSchemeCTL.getBusiSysReg(sysid);
//		ArrayList<ControlBillType> billtypeList = reg.getBillType();  //取单据类型
		ArrayList<ControlBillType> billtypeList = BillTypeBySysCache.getInstance().getUfindPanelBySysid(sysid);
		String pkMainOrg = reg.getMainPkOrg();

		/**如果没有单据,就一定需要实现getMainPkOrg这个方法*/
		if(billtypeList==null){
			IdBdcontrastVO vo = BdContrastCache.getNewInstance().getVoByPK(pkMainOrg);
			vos.add(vo);
			return vos;
		}
		if(billtype!=null&&OutEnum.MPPSYS.equals(sysid)){
			billtype = billtype.split("-")[0];
		}
		 /**如果有单据,但如果没有传递过来,就为空*/
		for(int n=0; n<(billtypeList==null?0:billtypeList.size()); n++){
			ControlBillType billtypeVO = billtypeList.get(n);

			String billtypeStr = billtypeVO.getBillType_code();

			if(billtype!=null&&billtype.indexOf(billtypeStr)>=0){
				ArrayList<String> orgTypeList = billtypeVO.getPk_orgs();
			    for(int m=0;m<(orgTypeList==null?0:orgTypeList.size());m++){
			    	IdBdcontrastVO vo = BdContrastCache.getNewInstance().getVoByPK(orgTypeList.get(m));
			    	vos.add(vo);
			    }
			    break;
			}
		}


		//项目档案特殊处理
		if(sysid.equals(OutEnum.ERMSYS)) {
			if(billtype != null) {
				IdBdcontrastVO vo = BdContrastCache.getNewInstance().getVOByField(sysid, "pk_project");
				vos.add(vo);
			}
		}
		if(sysid.equals(OutEnum.FIBILLSYS) && "BAL".equals(billtype)) {
			IdBdcontrastVO vo = BdContrastCache.getNewInstance().getVOByField(sysid, "arap_b_pk_project");
			vos.add(vo);
		}
		if(isDefault){
			ArrayList<IdBdcontrastVO> voList = new ArrayList<IdBdcontrastVO> ();
			if(vos!=null&&vos.size()>0){
			   voList.add(vos.get(0));   //默认取第一个
			}
			return voList;
		}else{
	    	return vos;
		}
	}

	public static IdBdcontrastVO getBdContrastVOByPk(String pk_contrast) throws BusinessException{
		IdBdcontrastVO m_vo = BdContrastCache.getNewInstance().getVoByPK(pk_contrast);
		return m_vo;
	}

	public static boolean getSysMainBdinfo(String sysid,String m_billtype,String pk_bdcontrast) throws BusinessException {
		String billtypeCode = CtlSchemeCTL.parseBillTypes(m_billtype);
		IBusiSysReg reg = CtlSchemeCTL.getBusiSysReg(sysid);
		String m_pk_bdcontrast = null;
		ArrayList<String> orgsList = null;
		if(reg.getBillType()==null){  //说明没有单据的系统,直接从接口方法里面获取主组织
			m_pk_bdcontrast = reg.getMainPkOrg();
		}else{
			//说明存在单据,那单据一定存在对应的主组织
			ArrayList<ControlBillType> billtypeList = reg.getBillType();

			for(int n=0 ; n<(billtypeList==null?0:billtypeList.size()) ;n++){
				ControlBillType billtype = billtypeList.get(n);
				if(billtype.getBillType_code().equals(billtypeCode)){
					orgsList = billtype.getPk_orgs();
					break;
				}
			}
		}
		if((orgsList!=null&&orgsList.contains(pk_bdcontrast))||pk_bdcontrast.equals(m_pk_bdcontrast)){
			return true;
		}else{
			return false;
		}
	}


	public static String getSysMainPKOrg(CtlAggregatedVO m_aggvo) {
		String pk_org = null;
		IdCtrlschmVO vo = (IdCtrlschmVO)m_aggvo.getParentVO();
		IdCtrlschmBVO[] childvos = (IdCtrlschmBVO[])m_aggvo.getChildrenVO();
		String billtypeCode = CtlSchemeCTL.parseBillTypes(vo.getBilltype());
		IBusiSysReg reg = null;
		try{
		   reg = CtlSchemeCTL.getBusiSysReg(vo.getCtrlsys());
	    }catch(BusinessException ex){
			NtbLogger.error(ex);
	    }
		String m_pk_bdcontrast = null;  //主组织
		ArrayList<String> orgsList = null;  //主组织,默认一个
		ArrayList<ControlBillType> billtypeList = BillTypeBySysCache.getInstance().getUfindPanelBySysid(vo.getCtrlsys());
		if(billtypeList==null){  //说明没有单据的系统,直接从接口方法里面获取主组织
			m_pk_bdcontrast = reg.getMainPkOrg();
		}else{
			//说明存在单据,那单据一定存在对应的主组织
//			ArrayList<ControlBillType> billtypeList = reg.getBillType();
			for(int n=0 ; n<(billtypeList==null?0:billtypeList.size()) ;n++){
				ControlBillType billtype = billtypeList.get(n);
				if(billtype.getBillType_code().equals(billtypeCode)){
					orgsList = billtype.getPk_orgs();
					break;
				}
			}
		}
		for(int n=0 ; n<childvos.length ; n++){
			IdCtrlschmBVO m_vo = childvos[n];
			if((m_pk_bdcontrast!=null&&m_pk_bdcontrast.equals(m_vo.getPk_bdcontrast()))||(orgsList!=null&&orgsList.contains(m_vo.getPk_bdcontrast()))){
				pk_org = m_vo.getPk_base();
			}else{
			}
		}
		return pk_org;
	}

	public static IdCtrlschmBVO[] filterCtrlschmBVO(IdCtrlschmBVO[] vos){
		ArrayList<IdCtrlschmBVO> list = new ArrayList<IdCtrlschmBVO> ();
		for(int n=0 ; n<(vos==null?0:vos.length) ;n++){
			if(vos[n].getPk_bdinfo()!=null){
				list.add(vos[n]);
			}
		}
		return list.toArray(new IdCtrlschmBVO[0]);
	}


	/**
	 * @desc 控制规则原始信息替换成实际显示的信息
	 * @author yuyonga
	 * @param mapvalue/在设置界面上显示的值对应的映射; ctrlinfo/在设置界面上的原始信息
	 * @return 替换后的信息
	 * */
	public static String getFinalCtrlInfoMessage(HashMap<String,String> mapvalue,String ctrlinfo){
		HashMap<String,String> mapInfo = CtrlInfoMacroConst.getAllCtrlMacro();
		Iterator map = mapInfo.entrySet().iterator();
		while(map.hasNext()){
			Map.Entry entry = (Map.Entry)map.next();
			String key = (String)entry.getKey();
			ctrlinfo = ctrlinfo.replaceAll(key, mapvalue.get(key));
		}
		return ctrlinfo;
	}

	public static String getControlCtlMessage(IdCtrlformulaVO vo,IdCtrlschemeVO vos,HashMap exeVarnoMap,UFDouble zxs_complex,String[] arrayS,String valueNameType,int powerInt)throws Exception{
		boolean isNumber = false;
		if(vos.getCtrlobj() != null){
		    isNumber = OutEnum.OCCORAMOUNT.indexOf(vos.getCtrlobj())>=0;//是否是发生数量
		}
		String ctrlObjName = parseCtrlObjName(vos.getCtrlsys(),vos.getCtrlobj());
		MdTask plan = TbTaskCtl.getMdTaskByPk(vo.getPk_plan(),true);  //获取计划的信息
		String planname = null==plan?"":plan.getObjname();
		String planSysName="";
		if(plan!=null){
			planSysName = getSysNameByCode(plan.getAvabusisystem());
		}
		
		UFDouble rundata = new UFDouble(0);
		//特殊方案的控制信息特殊处理
		if((vo.getPk_parent()==null||"".equals(vo.getPk_parent()))&&vo.getSchemetype().equals(String.valueOf(CtlSchemeConst.CTL_SCHEMA_COMPLEX))){
			rundata = zxs_complex;
		}else{
			/**这里显示的数据上有问题 yuyonga*/
			rundata = sumRunData(exeVarnoMap);
		}
		String[] ss = vos.getNameidx().split(":");
		StringBuffer buffer = new StringBuffer();
		/**得到主体的名称 yuyonga,这个PK肯定是预算组织,用预算组织名称显示*/
		String entityName = "";
		String entityPk = vos.getPk_org();
		String[] pkidx = vos.getStridx().split(":");
        for(int n=0;n<pkidx.length;n++){
        	if(entityPk!=null&&entityPk.equals(pkidx[n])){
        		entityName = ss[n];
        		break;
        	}
        }
		/**end*/
		buffer.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000235", null, new String[]{planSysName})/*{0}组织【*/).append(entityName).append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000236")/*】的【*/);
		for(int i=0;i<ss.length;i++){
			buffer.append(ss[i]+"/");
		}
		if(!vo.getSchemetype().equals(String.valueOf(CtlSchemeConst.CTL_SCHEMA_GROUP))){
			buffer.append(vos.getStartdate())
			.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000237")/*－*/)
			.append(vos.getEnddate())
			.append("/");
		}
		buffer.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000050")/*】*/);
		String diminfo = buffer.toString();
		return CtlSchemeCTL.getControlHintMessage(vo,planname, diminfo, isNumber,ctrlObjName, getValue(arrayS[0])/**计划数*/,getValue(arrayS[1])/**执行数*/,valueNameType,false,powerInt);

	}

	public static String getControlAlarmMessage(IdCtrlformulaVO vo,IdCtrlschemeVO vos,HashMap exeVarnoMap,UFDouble zxs_complex,String[] arrayS,String name,int powerInt)throws Exception{
//		boolean isNumber = OutEnum.OCCORAMOUNT.indexOf(vos.getCtrlobj())>=0;//是否是发生数量
		boolean isNumber = false;
		if(vos.getCtrlobj() != null){
		    isNumber = OutEnum.OCCORAMOUNT.indexOf(vos.getCtrlobj())>=0;//是否是发生数量
		}
		String ctrlObjName = CtlSchemeCTL.parseCtrlObjName(vos.getCtrlsys(),vos.getCtrlobj());
		MdTask plan = TbTaskCtl.getMdTaskByPk(vo.getPk_plan(),true);  //获取计划的信息
		String planname = plan==null?"":plan.getObjname();
		//执行数zxs
		UFDouble rundata = new UFDouble(0);
		//特殊方案zxs的处理
		if((vo.getPk_parent()==null||"".equals(vo.getPk_parent()))&&vo.getSchemetype().equals(String.valueOf(CtlSchemeConst.CTL_SCHEMA_COMPLEX))){
			rundata = zxs_complex;
		}else{
			rundata = sumRunData(exeVarnoMap);
		}

		String[] ss = vos.getNameidx().split(":");
		StringBuffer buffer = new StringBuffer();
		/**得到主体的名称 yuyonga,这个PK肯定是预算组织,用预算组织名称显示*/
		String entityName = "";
		String entityPk = vos.getPk_org();
		String[] pkidx = vos.getStridx().split(":");
        for(int n=0;n<pkidx.length;n++){
        	if(entityPk!=null&&entityPk.equals(pkidx[n])){
        		entityName = ss[n];
        		break;
        	}
        }
		/**end*/
		buffer.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000239")/*主体【*/).append(entityName).append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000236")/*】的【*/);

		for(int i=0;i<ss.length;i++){
			buffer.append(ss[i]+"/");
		}
		if(!vo.getSchemetype().equals(String.valueOf(CtlSchemeConst.CTL_SCHEMA_GROUP))){
			buffer.append(vos.getStartdate())
			.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000237")/*－*/)
			.append(vos.getEnddate())
			.append("/");
		}
		buffer.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000050")/*】*/);
		String diminfo = buffer.toString();
		return CtlSchemeCTL.getControlHintMessage(vo,planname, diminfo, isNumber,ctrlObjName, getValue(arrayS[0])/**计划数*/,getValue(arrayS[1])/**执行数*/,name,false,powerInt);//wangwjc


	}

	/**如果是全局本币为0,如果是集团本币为1,如果是组织本币为2,其他都应该为原币为3*/
	public static int getCurrencyType(String pk_currency){
		if(IDimMemberPkConst.PK_GLOBE_CURRENCY.equals(pk_currency)){
			return 0;
		}else if(IDimMemberPkConst.PK_GROUP_CURRENCY.equals(pk_currency)){
			return 1;
		}else if(IDimMemberPkConst.PK_ORG_CURRENCY.equals(pk_currency)){
			return 2;
		}else {
			return 3;
		}
	}

	public static String parseCtrlObjName(String sysId,String objcode) throws BusinessException,Exception {
		IBusiSysReg glReg = CtlSchemeCTL.getBusiSysReg(sysId);
		ArrayList<ControlObjectType> objs =glReg.getControlableObjects();
		ControlObjectType obj = null;
		for(int n=0;n<objs.size();n++){
			ControlObjectType objTmp = objs.get(n);
			if(objcode.equals(objTmp.getM_code())){
				obj = objTmp;
			}
		}
		return obj==null?"":obj.getM_description();
	}

	public static UFDouble sumRunData(HashMap exeVarnoMap){
		Iterator iter = exeVarnoMap.values().iterator();
		UFDouble sum = new UFDouble(0);
		while(iter.hasNext()){
			UFDouble value = (UFDouble)iter.next();
			sum = sum.add(value);
		}
		return sum;
	}

	public static UFDouble getValue(String exp){
		Object reValue = null;
		try{
			nc.vo.bank_cvp.compile.datastruct.ArrayValue result = nc.ui.bank_cvp.formulainterface.RefCompilerClient.getExpressionResult(exp, null);
			if (result.getType() == nc.vo.bank_cvp.type.IType.STRING)
				reValue = (String) result.getValue();
			else if (result.getType() == nc.vo.bank_cvp.type.IType.UFDOUBLE)
				reValue = (nc.vo.pub.lang.UFDouble) result.getValue();
			else if (result.getType() == nc.vo.bank_cvp.type.IType.DOUBLE)
				reValue = (Double) result.getValue();
			else if (result.getType() == nc.vo.bank_cvp.type.IType.INT)
				reValue = new UFDouble(result.getValue().toString());//BigInteger类型在下面强转UFDouble会报错，这里直接new一下wangwjc
			else if (result.getType() == nc.vo.bank_cvp.type.IType.BIGDECIMAL)
				reValue = new UFDouble(result.getValue().toString());
			else if (result.getType() == nc.vo.bank_cvp.type.IType.BOOLEAN)
				reValue = UFBoolean.valueOf(result.getValue().toString());
		}catch (Exception e) {
			NtbLogger.error(e);
		}
		return (UFDouble)reValue;
	}

	public static String getControlHintMessage(IdCtrlformulaVO parentVO,String planname,String diminfo,boolean isNumber,String objName,UFDouble plnValue,UFDouble zxsValue,String name,boolean isStartCtrl,int powerInt) throws Exception {
		StringBuffer message = new StringBuffer();
		java.text.DecimalFormat formatter = null;
		String spliter = ":";
		String HHF = "\n";
		String FLEXMESSAGE = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_rule", "01801rul_000407")/*是否执行特殊审批？*/;
		String infoss = null;
		String ctrlType = null;
		String frontHint = null;
		String ctrltype = null;
		StringBuffer diminfoStr = new StringBuffer()/*.append("\"")*/.append(diminfo)/*.append("\"")*/;
		MdTask plan = TbTaskCtl.getMdTaskByPk(parentVO.getPk_plan(),true);  //获取计划的信息
		String planSysName = null == plan?"":getSysNameByCode(plan.getAvabusisystem());
		boolean isShowDimInfo = true;
		if(parentVO.getCtlmode().equals(CtrlTypeEnum.RigidityControl.toCodeString())){//刚性控制型
//			frontHint = NCLangRes4VoTransl.getNCLangRes().getStrByID("pf_tb", "CtlSchemeCTL-000011")/*抱歉!  */;
			frontHint = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000240", null, new String[]{planSysName})/*{0}刚性控制提示：*/+HHF;
			ctrltype = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000241")/*刚性控制方案*/;
//			infoss = NCLangRes4VoTransl.getNCLangRes().getStrByID("pf_tb", "CtlSchemeCTL-000012")/*,无法进行后续处理。*/;
			infoss = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000310")/*已经超出预算,不允许再进行开支 ！*/+HHF;
		}else if(parentVO.getCtlmode().equals(CtrlTypeEnum.WarningControl.toCodeString())){//预警型
//			frontHint = NCLangRes4VoTransl.getNCLangRes().getStrByID("pf_tb", "CtlSchemeCTL-000013")/*提示:  */;
			frontHint = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000243", null, new String[]{planSysName})/*{0}预警提示：*/+HHF;
//			infoss = NCLangRes4VoTransl.getNCLangRes().getStrByID("pf_tb", "CtlSchemeCTL-000014")+"\n"/*。*/;
			infoss = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000244")/*已经超出预算 。*/+HHF;
			ctrltype = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000245")/*预警型控制方案*/;
		}else if(parentVO.getCtlmode().equals(CtrlTypeEnum.FlexibleControl.toCodeString())){ //柔性控制
			frontHint = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000246", null, new String[]{planSysName})/*{0}柔性控制提示：*/+HHF;
//			infoss = NCLangRes4VoTransl.getNCLangRes().getStrByID("pf_tb", "CtlSchemeCTL-000015")/*,是否提交特殊审批?*/;
//			infoss = "已经超出预算，是否执行特殊审批？\n";
			infoss = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000247")/*已经超出预算，*/+FLEXMESSAGE+"\n";
			ctrltype = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000248")/*柔性控制方案*/;
		}else if(parentVO.getCtlmode().equals(String.valueOf(CtlSchemeConst.CTL_SCHEMA_ZERO))){
			frontHint = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000249", null, new String[]{planSysName})/*{0}外项目提示：*/;
			infoss = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000250")/*无法进行后续处理。*/;
			ctrltype = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000251")/*零预算控制方案*/;
		}

		if(parentVO.getSchemetype().equals(String.valueOf(CtlSchemeConst.CTL_SCHEMA_SINGLE))){
			ctrlType = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000201")/*单项控制规则*/;
			isShowDimInfo = true;
		}else if(parentVO.getSchemetype().equals(String.valueOf(CtlSchemeConst.CTL_SCHEMA_GROUP))){
			ctrlType = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000252")/*多项控制规则*/;
			isShowDimInfo = true;
		}else if(parentVO.getSchemetype().equals(String.valueOf(CtlSchemeConst.CTL_SCHEMA_COMPLEX))){
			ctrlType = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000203")/*特殊控制规则*/;
			isShowDimInfo = true;
		}else if(parentVO.getSchemetype().equals(String.valueOf(CtlSchemeConst.CTL_SCHEMA_FLEX))){
			ctrlType = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000204")/*弹性控制规则*/;
			isShowDimInfo = true;
		}else if(parentVO.getSchemetype().equals(String.valueOf(CtlSchemeConst.CTL_SCHEMA_ZERO))){
			ctrlType = "";
			isShowDimInfo = true;
		}

		String ctrlsign = replaceCtrlsign(parentVO.getCtrlsign());  //控制的数据比较符
		NtbCtrlMath.setPrice(plnValue.toString());
		String value = NtbCtrlMath.getPrice();

		StringBuffer sbStr = new StringBuffer("##,##0.");
		for(int n=0;n<Math.abs(powerInt);n++){
			sbStr.append("0");
		}
		DecimalFormat format = new DecimalFormat(sbStr.toString());
		String zValue = format.format(zxsValue.doubleValue());/*NtbCtrlMath.getPrice();*/
		message.append((isStartCtrl?NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000253")/*启动完成！*/:""))
					.append(HHF)
					.append(frontHint)
//					.append(ctrlType).append("[").append(parentVO.getCtrlname()).append("]")  //控制方案类型和控制方案名称
					.append(isShowDimInfo?diminfoStr:"")
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000254")/*的*/).append(name).append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000254")/*的*/)
					.append(objName)
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000007")/*【*/)
					.append(zValue)
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000050")/*】*/)
					.append(ctrlsign)
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000255")/*预算控制数*/)
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000007")/*【*/)
					.append(value)
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000256")/*】。*/+HHF)
					.append(isStartCtrl?"":infoss)
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000257", null, new String[]{planSysName})/*对应{0}系统：*/+HHF)
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000258", null, new String[]{planSysName})/*{0}表【*/)
					.append(planname)
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000259")/*】的*/)
					.append(ctrltype)
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000007")/*【*/).append(parentVO.getCtrlname()).append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000260")/*】。*/)  //控制方案类型和控制方案名称
                    .append("\n");

		return message.toString();
	}

	/**替换规则列表,只争对警告信息*/
	public static String replaceCtrlsign(String ctrlsign){
		String[][] ctrlSignReplacePattern = {
			{">=",">"},
			{"<=","<"},
			{">",">="},
			{"<","<="},
			{"=","<>"},
		};
 		for(int i = 0; i < ctrlSignReplacePattern.length; i ++){
			if(ctrlsign.equals(ctrlSignReplacePattern[i][0]))
				return ctrlSignReplacePattern[i][1];
		}
		return null;

	}
	public static String getActualPkcurrency(String pk_currency,String pk_org,String sysId){
		if(IDimMemberPkConst.PK_GLOBE_CURRENCY.equals(pk_currency)){
			   return CurrencyManager.getGlobalDefaultCurrencyPK();
		}else if(IDimMemberPkConst.PK_GROUP_CURRENCY.equals(pk_currency)){
			   String pk_group = null;/*WorkbenchEnvironment.getInstance().getGroupVO().getPrimaryKey();*/
			   return CurrencyManager.getLocalCurrencyPKByGroup(pk_group);
		}else if(IDimMemberPkConst.PK_ORG_CURRENCY.equals(pk_currency)){
			    return CurrencyManager.getLocalCurrencyPK(pk_org);
		}else {
			   return pk_currency;
		}
	}

	public static String getPk_currency(String pk_currency,String pk_org,String sysId){
		if(IDimMemberPkConst.PK_GLOBE_CURRENCY.equals(pk_currency)){
			if(!(OutEnum.FIBILLSYS.equals(sysId)/*||OutEnum.ERMSYS.equals(sysId)*/)){
			   return CurrencyManager.getGlobalDefaultCurrencyPK();
			}else{
			   return null;
			}
		}else if(IDimMemberPkConst.PK_GROUP_CURRENCY.equals(pk_currency)){
			String pk_group = null;/*WorkbenchEnvironment.getInstance().getGroupVO().getPrimaryKey();*/
			if(!(OutEnum.FIBILLSYS.equals(sysId)/*||OutEnum.ERMSYS.equals(sysId)*/)){
			   return CurrencyManager.getLocalCurrencyPKByGroup(pk_group);
			}else{
				return null;
			}
		}else if(IDimMemberPkConst.PK_ORG_CURRENCY.equals(pk_currency)){
			if(!(OutEnum.FIBILLSYS.equals(sysId)/*||OutEnum.ERMSYS.equals(sysId)*/)){
			    return CurrencyManager.getLocalCurrencyPK(pk_org);
			}else{
				return null;
			}
		}else {
			return pk_currency;
		}
	}
	private static String spliter = ":";

	/**通过系统ID和单据类型获取主组织*/
	public static HashMap<String,String> getMainOrgBySysidAndBillType(String sysid,String billtype,String pk_group) throws BusinessException {
		HashMap<String,String> pkList = new HashMap<String,String> ();
		if(sysid.equals("GL")){
			IFinanceOrgPubService ibFinanceOrg = (IFinanceOrgPubService)NCLocator.getInstance().lookup(IFinanceOrgPubService.class.getName());
			FinanceOrgVO[] vos = ibFinanceOrg.queryAllFinanceOrgVOSByGroupID(pk_group);
		    for(int n=0; n<vos.length ; n++){
		    	pkList.put(vos[n].getPk_financeorg(),vos[n].getCode());
		    }
		}else if (sysid.equals("FA")){
			IFinanceOrgPubService ibFinanceOrg = (IFinanceOrgPubService)NCLocator.getInstance().lookup(IFinanceOrgPubService.class.getName());
			ibFinanceOrg.queryAllFinanceOrgVOSByGroupID(pk_group);
			FinanceOrgVO[] vos = ibFinanceOrg.queryAllFinanceOrgVOSByGroupID(pk_group);
			for(int n=0; n<vos.length ; n++){
				pkList.put(vos[n].getPk_financeorg(),vos[n].getCode());
		    }
		}else if (sysid.equals("FP")){
			/**需要通过单据来确认主组织*/

		}
		return pkList;
	}

	public static ArrayList<String[]> getActualAccsubjPK(String pk_org,String pk_account,NtbParamVO ntbvo) throws BusinessException {
		/**首先通过pk_accasoa找到它对应的AccountVO*/
		ArrayList<String[]> pkList = new ArrayList<String[]> ();
		AccountVO  newvo = null;
		if(!ntbvo.getSys_id().equals("GL")){
			pkList.add(new String[]{pk_org,null,null});
			return pkList;
		}
		/**取pk_org对应的核算帐簿*/
		IAccountingBookPubService accountbook = NCLocator.getInstance().lookup(IAccountingBookPubService.class);
		String[] pk_accountbooks = accountbook.queryAccountingBookIDSByFinanceOrgID(pk_org);
		for(int n=0;n<(pk_accountbooks==null?0:pk_accountbooks.length);n++){
		   newvo = getAccountByBaAndAccount(pk_accountbooks[n],pk_account);
		   String[] tmpStr = new String[] {pk_accountbooks[n],newvo.getPk_accasoa(),newvo.getCode()};
		   pkList.add(tmpStr);
		}
		return pkList;
	}

	public static AccountVO getAccountByBaAndAccount(String pk_bookaccount,String pk_account) throws BusinessException {
		/**如果PK_ORG对应了核算帐簿,则核算帐簿对应了科目主建,如果没有核算帐簿,返回为空*/
		IAccountPubService account = NCLocator.getInstance().lookup(IAccountPubService.class);
		UFDate date = UFDate.getDate(System.currentTimeMillis());
		AccountVO[] vos = account.queryAccountVOs(pk_bookaccount,
				TimeDimTool.getUAPDataStr(String.valueOf(date.getYear()), String.valueOf(date.getMonth()), null),UFBoolean.valueOf(false)
				/*String.valueOf(date.getMonth()), String.valueOf(date.getYear())*/);
		AccountVO vo = null;
		for(int n=0 ;n<(vos==null?0:vos.length);n++){
			if(pk_account.equals(vos[n].getPk_account())){
				vo = vos[n];
				break;
			}
		}
		return vo;
	}


	public static void addMainOrgInfo(NtbParamVO param){
	    String pk_org = param.getPk_Org();
		ArrayList<String> typedim = new ArrayList<String> ();
		typedim.addAll(Arrays.asList(param.getTypeDim()));
		ArrayList<String> pkdim = new ArrayList<String> ();
		pkdim.addAll(Arrays.asList(param.getPkDim()));
		ArrayList<String> codedim = new ArrayList<String> ();
		codedim.addAll(Arrays.asList(param.getCode_dims()));
		pkdim.add(pk_org);
		codedim.add("");
		if(param.getSys_id().equals("GL")){
			typedim.add(OutEnum.ZHANGBU);
		}else if(param.getSys_id().equals("FA")){
			typedim.add(OutEnum.FINANCEORG);
		}else{
			typedim.add(OutEnum.FINANCEORG);
		}
		param.setTypeDim(typedim.toArray(new String[0]));
		param.setPkDim(pkdim.toArray(new String[0]));
		param.setCode_dims(codedim.toArray(new String[0]));
	}


	public static void addGroupDownAllOrgParams(NtbParamVO paramvo,ArrayList<NtbParamVO> paramvoList,IdCtrlschemeVO schemevo) throws BusinessException{
		/**获取该集团下所有的同一类型的组织,这也需要根据所选的业务系统和单据来确定*/
		HashMap<String,String> orgPkList = getMainOrgBySysidAndBillType(paramvo.getSys_id(),null,paramvo.getPk_Org());
		/**
		 * 控制公司下级和控制所有公司的情况，NtbParamVO需要根据下级公司的个数去复制多个NtbParamVO,去控制系统取数后再merge这些VO
		 * */
		try{
			String bdinfotype = getBdinfoType(schemevo.getFromitems(),schemevo.getCtrlsys());
			  String[] bdinfotypeidx = bdinfotype.split(spliter);
			  String[] pkidx = schemevo.getStridx().split(spliter);
			  String[] codeidx = schemevo.getCodeidx().split(spliter);
			  /**循环多次,跟财务组织下的核算帐簿数量一样*/
			  String pk_account = null;
			  String m_code = null;
			  for(int j=0; j<bdinfotypeidx.length; j++){
				  if(OutEnum.ACCSUBJDOC.equals(bdinfotypeidx[j])){
					  pk_account = pkidx[j];
					  m_code = codeidx[j];
				  }
			  }
			Iterator iter = orgPkList.entrySet().iterator();
			/**获得所有的财务组织,或者其他组织,如果是财务组织的并且是总帐取数,就需要获取到该财务组织下所有的核算帐簿,并且找到对应的科目PK*/
			while(iter.hasNext()){
			  /**一个组织单元,如果取数系统为总帐,就可能会根据组织单元下的核算帐簿生成对应数量的Ntbparam*/
			  Map.Entry entry = (Map.Entry)iter.next();
			  String pk_org = (String)entry.getKey(); //组织单元
			  String code = (String)entry.getValue(); //组织单元CODE
			  if(schemevo.getCtrlsys().equals("GL")){

				  if(pk_account!=null){
					  /**说明存在会计科目*/
					  ArrayList<String[]> tmpList = getActualAccsubjPK(pk_org,pk_account,paramvo);
					  for(int n=0 ; n<(tmpList==null?0:tmpList.size());n++){
						  String pk_fatherParamVO = paramvo.getPk_ctrl();
					      NtbParamVO param = (NtbParamVO)paramvo.clone();
						  param.setFatherCorpPk(pk_fatherParamVO);
						  param.setPk_Org(tmpList.get(n)[0]);
						  param.setCode_corp(code);
						  param.setCode_dims(schemevo.getCodeidx().replace(m_code, tmpList.get(n)[2]).split(spliter));
						  param.setPkDim(schemevo.getStridx().replace(pk_account, tmpList.get(n)[1]).split(spliter));
						  addMainOrgInfo(param);
						  paramvoList.add(param);
					  }
				  }else{
					  /**说明没有会计科目的基本档案*/
					  ArrayList<String> tmpList = getActualOrgPK(pk_org,paramvo);
					  for(int n=0 ; n<(tmpList==null?0:tmpList.size());n++){
						  String pk_fatherParamVO = paramvo.getPk_ctrl();
					      NtbParamVO param = (NtbParamVO)paramvo.clone();
						  param.setFatherCorpPk(pk_fatherParamVO);
						  param.setPk_Org(tmpList.get(n));
						  param.setCode_corp(code);
						  param.setCode_dims(codeidx);
						  param.setPkDim(pkidx);
						  addMainOrgInfo(param);
						  paramvoList.add(param);
					  }
				  }
			  }else{
				  /**不做循环,直接生成对应的Ntbparam*/
				    String pk_fatherParamVO = paramvo.getPk_ctrl();
					NtbParamVO param = (NtbParamVO)paramvo.clone();
					param.setFatherCorpPk(pk_fatherParamVO);
					param.setPk_Org(pk_org);
					param.setCode_corp(code);
					param.setCode_dims(codeidx);
					param.setPkDim(pkidx);
					addMainOrgInfo(param);
					paramvoList.add(param);
			  }
			}

		}catch(BusinessException ex){
			NtbLogger.error(ex);
			throw ex;
		}
	}

	public static ArrayList<String> getActualOrgPK(String pk_org,NtbParamVO ntbvo) throws BusinessException {
		/**取pk_org对应的核算帐簿*/
		ArrayList<String> pkList = new ArrayList<String> ();
		if(!ntbvo.getSys_id().equals("GL")){
			pkList.add(pk_org);
			return pkList;
		}
		IAccountingBookPubService accountbook = NCLocator.getInstance().lookup(IAccountingBookPubService.class);
		String[] pk_accountbooks = accountbook.queryAccountingBookIDSByFinanceOrgID(pk_org);
		for(int n=0;n<(pk_accountbooks==null?0:pk_accountbooks.length);n++){
		   pkList.add(pk_accountbooks[n]);
		}
		return pkList;
	}

	public static String getFullDest(ArrayList<DataContrastVO> voList,UFBoolean isPlDeal) throws BusinessException{
		String sExpress =null;
		/**如果isPlDeal为false,表示批量处理,每一个单元格都会有控制方案,如果isPlDeal为true,表示只分配到区域的第一个单元格上*/
		try{
			if(isPlDeal!=null&&isPlDeal.booleanValue()){//true
			   sExpress = CtrlRuleCTL.getSumFindString(voList,true);
			}else{
			   sExpress = CtrlRuleCTL.getSumFindString(voList,false);
            }
		}catch(BusinessException ex){
			NtbLogger.printException(ex);
			throw ex;
		}
		return sExpress;

	}

	public static String getUFindExpress(ArrayList<DataContrastVO> voList,CtlAggregatedVO[][] aggvos,boolean isPlDeal,boolean flag) throws BusinessException{

		CtlAggregatedVO[] memvos = aggvos[1];   //具体的基本档案信息
		CtlAggregatedVO vo = aggvos[0][0];
        IdCtrlschmVO parent = (IdCtrlschmVO)vo.getParentVO();
        String isAcctroll = parent.getAccctrollflag();
		StringBuffer express = new StringBuffer();
		if(isPlDeal){ //如果是组方案的话,走这分支
			flag = false;
			for(CtlAggregatedVO memvo : memvos) {
				ArrayList<CtlAggregatedVO> tmpList = new ArrayList<CtlAggregatedVO> ();
				tmpList.addAll(Arrays.asList(memvos));
				tmpList.remove(memvo);
				IdCtrlschmVO _parent = (IdCtrlschmVO)memvo.getParentVO();
				_parent.setAccctrollflag(isAcctroll);
				int arNo = ((IdCtrlschmVO)memvo.getParentVO()).getVarno().indexOf("var")>=0?0:1;
				String contentExp = null;
				if(arNo == 0)
					contentExp = "UFIND(\'" + toUFindAttrExpress(voList,_parent,(IdCtrlschmBVO[])memvo.getChildrenVO(),flag,tmpList.toArray(new CtlAggregatedVO[0])) + "\')";
				else
					contentExp = "PREFIND(\'" + toUFindAttrExpress(voList,_parent,(IdCtrlschmBVO[])memvo.getChildrenVO(),flag,tmpList.toArray(new CtlAggregatedVO[0])) + "\')";
				express.append(contentExp + "+");
			}
			express.replace(express.length() - 1, express.length(), "");
//				for(int m=0 ; m<voList.size() ; m++){
//				  for(int i=0;i<memvos.length;i++){
//					int arNo = ((IdCtrlschmVO)memvos[i].getParentVO()).getVarno().indexOf("var")>=0?0:1;
//					if(arNo == 0) {//执行数
//						IdCtrlschmVO _parent = (IdCtrlschmVO)memvos[i].getParentVO();
//						_parent.setAccctrollflag(isAcctroll);
//				        String ufindexpress = "UFIND(\'" + toUFindAttrExpress(voList.get(m),_parent,(IdCtrlschmBVO[])memvos[i].getChildrenVO(),flag,memvos) + "\')";
//				        express.append(ufindexpress);
//					}else{
//						IdCtrlschmVO _parent = (IdCtrlschmVO)memvos[i].getParentVO();
//						_parent.setAccctrollflag(isAcctroll);
//					    String ufindexpress = "PREFIND(\'" + toUFindAttrExpress(voList.get(m),_parent,(IdCtrlschmBVO[])memvos[i].getChildrenVO(),flag,memvos) + "\')";
//					    express.append(ufindexpress);
//					}
//				    if(i!=memvos.length-1||m!=voList.size()-1){
//					   express.append("+");
//				    }
//			      }
//				}
		}else{
			  //如果有多个单元格,批量的情况,就不能把具体的值写到公式中去,要在实例化的时候再读取单元格信息
			  DataContrastVO _vo = voList.get(0);
			  if(voList.size()>1){
				  _vo.setNoSaveLevelValue(true);
			  }

			  String runFormula = parent.getRunformula();
			  if(!StringUtil.isEmpty(runFormula))
				  express.append(parent.getRunformula());
			  //end
			  for(int i=0;i<memvos.length;i++){
				ArrayList<CtlAggregatedVO> tmpList = new ArrayList<CtlAggregatedVO> ();
				tmpList.addAll(Arrays.asList(memvos));
				tmpList.remove(i);
				String vars = ((IdCtrlschmVO)memvos[i].getParentVO()).getVarno();

				int arNo = ((IdCtrlschmVO)memvos[i].getParentVO()).getVarno().indexOf("var")>=0?0:1;
				if(arNo == 0) {//执行数
					IdCtrlschmVO _parent = (IdCtrlschmVO)memvos[i].getParentVO();
					_parent.setAccctrollflag(isAcctroll);
					List<DataContrastVO> contrastVos = new ArrayList<DataContrastVO>();
					contrastVos.add(_vo);
			        String ufindexpress = "UFIND(\'" + toUFindAttrExpress(contrastVos,_parent,(IdCtrlschmBVO[])memvos[i].getChildrenVO(),flag,tmpList.toArray(new CtlAggregatedVO[0])) + "\')";
			        int location = express.indexOf(vars);
			        if(location >= 0) {
			        	express.replace(location, location + vars.length(), ufindexpress);
			        } else
			        	express.append(ufindexpress);
				}else{
					IdCtrlschmVO _parent = (IdCtrlschmVO)memvos[i].getParentVO();
					_parent.setAccctrollflag(isAcctroll);
					List<DataContrastVO> contrastVos = new ArrayList<DataContrastVO>();
					contrastVos.add(_vo);
				    String ufindexpress = "PREFIND(\'" + toUFindAttrExpress(contrastVos,_parent,(IdCtrlschmBVO[])memvos[i].getChildrenVO(),flag,tmpList.toArray(new CtlAggregatedVO[0])) + "\')";
				    int location = express.indexOf(vars);
			        if(location >= 0) {
			        	express.replace(location, location + vars.length(), ufindexpress);
			        } else
			        	express.append(ufindexpress);
				}
//			    if(i!=memvos.length-1/*||m!=cell.length-1*/){
//				   express.append("+");
//			    }
		      }
		}
		return express.toString();
	}

	private static String getVirtualBilltype(CtlAggregatedVO[] memvos,IdCtrlschmVO parentvo){
        ArrayList<String> otherBilltype = new ArrayList<String> ();
        String varNo = parentvo.getVarno();   //如果为var,则需要寻找对应的rar,如果为rar,则需要寻找对应的var
        String othersizeNO = varNo.indexOf("var")==0?varNo.replaceAll("var", "rar"):varNo.replaceAll("rar", "var");
	    for(int n=0;n<memvos.length;n++){
	    	CtlAggregatedVO vo = memvos[n];
	    	IdCtrlschmVO voTmp = (IdCtrlschmVO)vo.getParentVO();
	    	if(othersizeNO.equals(voTmp.getVarno())){
		       String code = CtlSchemeCTL.parseBillTypes(voTmp.getBilltype());
		       otherBilltype.add(code);
	    	}
	    }
	    StringBuffer sbStr = new StringBuffer();
	    sbStr.append(CtlSchemeCTL.parseBillTypes(parentvo.getBilltype())).append("-");
	    for(int n=0;n<otherBilltype.size();n++){
	    	if(n!=otherBilltype.size()-1){
	    	   sbStr.append(otherBilltype.get(n)).append("-");
	    	}else{
	    	   sbStr.append(otherBilltype.get(n));
	    	}
	    }
	    return sbStr.toString();
	}

	/**检查采购计划设置的控制数条目是否完整*/
	private static boolean isCheckVarNO(CtlAggregatedVO[] memvos,IdCtrlschmVO parentvo){
        ArrayList<String> varNos = new ArrayList<String> ();
        ArrayList<String> rarNos = new ArrayList<String> ();
        String varNo = parentvo.getVarno();   //如果为var,则需要寻找对应的rar,如果为rar,则需要寻找对应的var
        if(varNo.indexOf("var")==0){
        	varNos.add(varNo);
        }
        if(varNo.indexOf("rar")==0){
        	rarNos.add(varNo);
        }
        for(int n=0;n<memvos.length;n++){
	    	CtlAggregatedVO vo = memvos[n];
	    	IdCtrlschmVO voTmp = (IdCtrlschmVO)vo.getParentVO();
	    	if(!(parentvo.m_ctrlsys!=null&&parentvo.m_ctrlsys.equals(voTmp.m_ctrlsys))){
	    		continue;
	    	}
	    	String rarNo = voTmp.getVarno();
	    	  if(rarNo.indexOf("var")==0){
	          	varNos.add(varNo);
	          }
	          if(rarNo.indexOf("rar")==0){
	          	rarNos.add(varNo);
	          }
	    }
	    if(varNos.size()==rarNos.size()){
	    	return true;
	    }else{
	    	return false;
	    }
	}

	public static String toUFindAttrExpress(List<DataContrastVO> contrastVos,IdCtrlschmVO parentvo,IdCtrlschmBVO[] ctrlSchmBvos,boolean flag,CtlAggregatedVO[] memvos) throws BusinessException {
		StringBuffer ufindexpress = new StringBuffer();
		HashMap<String, String> hm = new HashMap<String, String>();
		/**
		 * flag=true时，选中多个单元格设置单项方案
		 * flag=false时，选中一个单元格设置单项方案
		 * */
		String sysid = parentvo.getCtrlsys();
		if(flag){
			hm = getSingleAndComplexArrtibuteMaps(sysid,ctrlSchmBvos);
		}else{
			hm = getAttributeMaps(contrastVos,sysid,ctrlSchmBvos,parentvo.getBilltype()); //如果是单项和多项,就需要读取单元格的信息
		}
		ufindexpress.append(sysid);
		ufindexpress.append(",");
		//单据1：单据2：..
		/**特殊处理供应链的问题*/
		if(parentvo.getBilltypeObj()!=null&&parentvo.getBilltypeObj().isUseVirtualBilltype()){
			String billtype = null;
			billtype = getVirtualBilltype(memvos,parentvo);  //应该有顺序
			if(billtype!=null){
			   ufindexpress.append(billtype);
			}else{
			   ufindexpress.append(parentvo.getBilltype());
			}
		}else{
		    ufindexpress.append(parentvo.getBilltype());
		}
		ufindexpress.append(",");
		//控制方向
		ufindexpress.append(parentvo.getCtrldirection()==null?"":parentvo.getCtrldirection().trim());
		ufindexpress.append(",");
		//发生额、余额
		ufindexpress.append(parentvo.getCtrlobj());
		ufindexpress.append(",");
		//具体的发生额
		ufindexpress.append(parentvo.getCtrlObjValue());
		ufindexpress.append(",");
		ufindexpress.append(",");
		ufindexpress.append(parentvo.getStartdate()==null? "": parentvo.getStartdate()); //起始日期
		ufindexpress.append(",");
		ufindexpress.append(parentvo.getEnddate()==null? "" : parentvo.getEnddate()); //结束日期
		ufindexpress.append(",");

		ufindexpress.append(parentvo.getDateType());
		ufindexpress.append(",");
		ufindexpress.append(parentvo.getAccctrollflag());
		ufindexpress.append(",");
		ufindexpress.append(FormulaMember.PkCorp);
		ufindexpress.append(",");
//		ufindexpress.append(FormulaMember.PkCurrency);
		ufindexpress.append(parentvo.getPk_currency()==null?FormulaMember.PkCurrency:parentvo.getPk_currency());
		ufindexpress.append(",");
		ufindexpress.append(FormulaMember.PkNcEntity);
		ufindexpress.append(",");
		ufindexpress.append(hm.get("fromitem"));
		ufindexpress.append(",");
		ufindexpress.append(hm.get("stridx"));
		ufindexpress.append(",");
		ufindexpress.append(hm.get("codeidx"));
		ufindexpress.append(",");
		ufindexpress.append(hm.get("nameidx"));
		ufindexpress.append(",");
		ufindexpress.append(hm.get("ctrllevel"));
		ufindexpress.append(",");
		ufindexpress.append(hm.get("mainorgs"));

		String memo = parentvo.getMemo();
		if(memo == null||"".equals(memo)){
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000054")/*请输入控制规则名称*/);
		}else if(memo.getBytes().length > 100){
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl_0", "01050ctrl006-0004")/*控制规则名称超长*/);
		}

		/**检查是否有业务系统*/
		if(parentvo.getCtrlsys() == null){
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000055")/*请选择业务系统*/);
		}
		if(OutEnum.MPPSYS.equals(parentvo.getCtrlsys())){
			boolean isCheckVarNo = isCheckVarNO(memvos,parentvo);
			if(!isCheckVarNo){
				throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000316")/*采购计划控制条目没有一一对应,请修改*/);
			}
		}
		/**检查是否有单据类型,有单据类型的业务系统,应该存在单据类型的选择控件*/
		IBusiSysReg reg = CtlSchemeCTL.getBusiSysReg(parentvo.getCtrlsys());
		ArrayList<ControlBillType> billtypes = BillTypeBySysCache.getInstance().getUfindPanelBySysid(sysid);
		if(billtypes!=null&&billtypes.size()>0&&(parentvo.getBilltype()==null || parentvo.getBilltype().length()==0)){
			IdSysregVO vo = BusiSysReg.getSharedInstance().getSysregByName(parentvo.getCtrlsys());
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000056", null, new String[]{vo.toString()})/*{0}必须选择单据类型*/);
		}
		/**检查使用会计期间的系统,是否必输会计期间*/
		String billtype = CtlSchemeCTL.parseBillTypes(parentvo.getBilltype());
		if(reg.isUseAccountDate(billtype)){
            /**检查会计期间是否有先后*/
			if(parentvo.getStartdate()==null||parentvo.getEnddate()==null||"".equals(parentvo.getStartdate())||"".equals(parentvo.getEnddate())){
				if(((parentvo.getStartdate()==null||"".equals(parentvo.getStartdate()))&&(parentvo.getEnddate()!=null&&!"".equals(parentvo.getEnddate())))){
					throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000057")/*起始期间不能为空*/);
				}
				if(((parentvo.getEnddate()==null||"".equals(parentvo.getEnddate()))&&(parentvo.getStartdate()!=null&&!"".equals(parentvo.getStartdate())))){
					throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000058")/*结束期间不能为空*/);
				}
			}else{
				String _startdate = parentvo.getStartdate();
				String _enddate = parentvo.getEnddate();
				if(DateUtil.getDiffAccountDate(_startdate, _enddate)>0){
					throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000059")/*起始日期不能够大于结束日期*/);
				}
			}

		}else{
			/**检查普通时间的先后*/
			if(parentvo.getStartdate()==null||parentvo.getEnddate()==null||"".equals(parentvo.getStartdate())||"".equals(parentvo.getEnddate())){
				if(((parentvo.getStartdate()==null||"".equals(parentvo.getStartdate()))&&(parentvo.getEnddate()!=null&&!"".equals(parentvo.getEnddate())))){
					throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000060")/*起始日期不能为空*/);
				}
				if(((parentvo.getEnddate()==null||"".equals(parentvo.getEnddate()))&&(parentvo.getStartdate()!=null&&!"".equals(parentvo.getStartdate())))){
					throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000061")/*结束日期不能为空*/);
				}
			}else{
				String _startdate = parentvo.getStartdate();
				String _enddate =  parentvo.getEnddate();
				if(DateUtil.getDiff(_startdate, _enddate)>0){
					throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000059")/*起始日期不能够大于结束日期*/);
				}
			}
		}
		/**检查是否存在基本档案,至少为一*/
		if(ctrlSchmBvos==null || ctrlSchmBvos.length==0){
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01420ctl_000141")/*请为执行数函数选择至少一个基本档案*/);
		}
		for(int i=0; i<ctrlSchmBvos.length; i++){
			if(ctrlSchmBvos[i].getPk_bdcontrast()==null){
				throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000062")/*执行数函数基本档案类型不能为空*/);
			}
		}
		return ufindexpress.toString();
	}

	public static void checkSchmByCube(String acctrollFlag, IdCtrlschmVO schparentvo, CubeDef cubedef) throws BusinessException {
		String startDate = schparentvo.getStartdate();
		String endDate = schparentvo.getEnddate();

		if((startDate != null || endDate != null) && (!"".equals(startDate) || !"".equals(endDate))) {
			AccumulateEnum accEnum = AccumulateEnum.fromCodeString(acctrollFlag);
			if(accEnum.toInt() != -1)
				throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl_0","01050ctrl003-0000")/*@res "选择累计后不能再选定具体的起止日期"*/);
		}

		DimHierarchy dimhier = cubedef.getDimHierarchy(IDimDefPKConst.TIME);

		//自定义计划期间不支持从年、季、月初累计
		if(dimhier.getPrimaryKey().equals(IDimHierarchyPkConst.DEFPSPERIOD)) {
			AccumulateEnum accEnum = AccumulateEnum.fromCodeString(acctrollFlag);
			int type = accEnum.toInt();
			if(type == 0 || type == 1 || type == 2) {
				throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl_0","01050ctrl002-0000")/*@res "计划期间为自定义计划期间时不支持从年初、季初、月初累计"*/);
			}
		}

		//其他情况，用汇总结构中没有的DimLevel累计，除了年月按季累计外，都是不支持的
		if(dimhier.getTableex() != null && dimhier.getTableex().indexOf("tb_time") >= 0){
			List<DimLevel> levelList = dimhier.getDimLevels();
			AccumulateEnum accEnum = AccumulateEnum.fromCodeString(acctrollFlag);

			int type = accEnum.toInt();
			String accPk = null;
			if(type == 0) accPk = IDimLevelPKConst.MONTH;
			else if(type == 1) accPk = /*IDimLevelPKConst.QUARTER*/IDimLevelPKConst.YEAR;
			else if(type == 2) accPk = /*IDimLevelPKConst.YEAR*/IDimLevelPKConst.QUARTER;

			if(accPk != null) {
				boolean isContainInHier = false;
				List<String> levelPks = new ArrayList<String>();
				for(DimLevel level : levelList) {
					if(level.getPrimaryKey().equals(accPk))
						isContainInHier = true;
					levelPks.add(level.getPrimaryKey());
				}
				if(!isContainInHier) {

					//年月从季初累计已特殊处理
//					if(!(type == 2 && levelPks.contains(IDimLevelPKConst.YEAR) && levelPks.contains(IDimLevelPKConst.MONTH))) {
						throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl_0","01050ctrl002-0001")/*@res "计划期间为"*/ + dimhier.getObjName() + "," + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl_0","01050ctrl002-0002")/*@res ", 不支持"*/ + accEnum.toString());
//					}
				}
			}
		}
	}
	public static void checkSchmByCube(CtlAggregatedVO[][] aggvos, CubeDef cubedef) throws BusinessException {
		CtlAggregatedVO aggvo = aggvos[0][0];
		IdCtrlschmVO parentvo = (IdCtrlschmVO)aggvo.getParentVO();

		String acctrollFlag = parentvo.getAccctrollflag();

		CtlAggregatedVO schaggvo = aggvos[1][0];
		IdCtrlschmVO schparentvo = (IdCtrlschmVO)schaggvo.getParentVO();

		checkSchmByCube(acctrollFlag, schparentvo, cubedef);
	}

	public static void checkSchmLegal(CtlAggregatedVO[][] aggvos) throws BusinessException {
		CtlAggregatedVO[] memvos = aggvos[1];

		for(CtlAggregatedVO memvo : memvos) {
			IdCtrlschmVO parentvo = (IdCtrlschmVO)memvo.getParentVO();
			IdCtrlschmBVO[] schmvos = (IdCtrlschmBVO[])memvo.getChildrenVO();

			//如果是收付余额，不能选择原币
			if(parentvo.getCtrlsys().equals(OutEnum.FIBILLSYS) && parentvo.getBilltype() != null && parentvo.getBilltype().indexOf("BAL") >=0) {
				String currType = parentvo.getPk_currency();
				if(currType == null)
					throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl_0","01050ctrl006-0002")/*@res "余额表不支持币种类型选择空"*/);
			}

			//如果是采购管理，不能选择预警型控制
			if(parentvo.getCtrlsys().equals(OutEnum.MPPSYS)) {
				if(parentvo.getCtrlmode() != null && (parentvo.getCtrlmode().equals(CtrlTypeEnum.WarningControl.toCodeString())||parentvo.getCtrlmode().equals(CtrlTypeEnum.FlexibleControl.toCodeString())))
					throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl_0","01050ctrl001-0061")/*@res "采购计划不能选择预警型控制和柔性控制"*/);
			}
			//如果是营销费用管理，不能选择预警型控制
			if(parentvo.getCtrlsys().equals(OutEnum.MESYS)) {
				if(parentvo.getCtrlmode() != null && (parentvo.getCtrlmode().equals(CtrlTypeEnum.WarningControl.toCodeString())))
					throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl_0","01050ctrl006-00011")/*@res "营销费用管理不支持选择预警型控制"*/);
			}
			//如果是总账，必须输入会计科目
			if(parentvo.getCtrlsys().equals(OutEnum.GLSYS)) {
				boolean isHasAccount = false;
				for(IdCtrlschmBVO schmVO : schmvos) {
					if(schmVO.getDatafrom().equals("DETAIL103"))
						isHasAccount = true;
				}
				if(!isHasAccount)
					throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_rule", "01420rul_000141")/*总账系统公式必须包含会计科目档案*/);

				UFDateTime startTime = null;
				UFDateTime endTime = null;

				if(!StringUtil.isEmpty(parentvo.getStartdate()))
					startTime = new UFDateTime(parentvo.getStartdate());
				if(!StringUtil.isEmpty(parentvo.getEnddate()))
					endTime = new UFDateTime(parentvo.getEnddate());
				if(startTime != null && endTime != null) {
					int startYear = startTime.getYear();
					int endYear = endTime.getYear();
					if(startYear != endYear) {
						throw new CheckSchmException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl_0","01050ctrl001-0062")/*@res "总账系统，设置跨年取数或控制时，请检查相应的会计期间是否已经创建"*/, false);
					}
				}
			}

			//校验资金调拨单必须有收款单位或付款单位档案
			if(parentvo.getCtrlsys().equals(OutEnum.SFSYS) && parentvo.getBilltype() != null && parentvo.getBilltype().indexOf("36K6") >= 0) {
				boolean isHasCorrOrg = false;
				for(IdCtrlschmBVO schmVO : schmvos) {
					if(schmVO.getDatafrom().equals("pk_org_p36K6") || schmVO.getDatafrom().equals("pk_org_r36K6")) {
						isHasCorrOrg = true;
					}
				}
				if(!isHasCorrOrg)
					throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000320")/*资金调拨单据取数或控制时，基础档案中必须使用付款单位或收款单位*/);
			}

		}
	}


	public static HashMap<String, String> getSingleAndComplexArrtibuteMaps(String sys,IdCtrlschmBVO[] ctrlSchmBvos){
		HashMap<String,String> hm = new HashMap<String,String> ();

		StringBuffer fromitem = new StringBuffer();
		StringBuffer nameidx = new StringBuffer();
		StringBuffer codeidx = new StringBuffer();
		StringBuffer ctrllevel = new StringBuffer();
		StringBuffer stridx =  new StringBuffer();
		StringBuffer mainorgs = new StringBuffer();

		for(int i=0;i<ctrlSchmBvos.length;i++){
			if(i!=ctrlSchmBvos.length-1){
				fromitem.append(ctrlSchmBvos[i].getDatafrom()+":");
				nameidx.append(ctrlSchmBvos[i].getBasename()+":");
				codeidx.append(ctrlSchmBvos[i].getBasecode()+":");
				ctrllevel.append(ctrlSchmBvos[i].getCtllevelflag()+":");
				stridx.append(ctrlSchmBvos[i].getPk_base()+":");
				mainorgs.append(ctrlSchmBvos[i].isMainOrg()+":");
			}
			else{
				fromitem.append(ctrlSchmBvos[i].getDatafrom());
				nameidx.append(ctrlSchmBvos[i].getBasename());
				codeidx.append(ctrlSchmBvos[i].getBasecode());
				ctrllevel.append(ctrlSchmBvos[i].getCtllevelflag());
				stridx.append(ctrlSchmBvos[i].getPk_base());
				mainorgs.append(ctrlSchmBvos[i].isMainOrg());
			}
		}
		hm.put("fromitem", fromitem.toString());
		hm.put("nameidx", replace(nameidx.toString()));
		hm.put("codeidx", codeidx.toString());
		hm.put("ctrllevel", ctrllevel.toString());
		hm.put("stridx", stridx.toString());
		hm.put("mainorgs", mainorgs.toString());
		return hm;
	}

	/**
	 * 设置fromitem,codeidx,nameidx,ctrllevel
	 * (1)在Cube上设置控制方案，需要根据单元格设置一次fromitem..
	 * (3)在公式分配，需要根据当前fromitem，判断其codeidx是否存在，如果不存在，需要匹配单元格上当前fromitem字段对应的codeidx,如果没有，设置为null
	 * (4)还是应该以界面参照的基本档案为模板
	 * */
	public static HashMap<String, String> getAttributeMaps(List<DataContrastVO> contrastVos,String sys,IdCtrlschmBVO[] ctrlSchmBvos,String billtype) throws BusinessException {
		HashMap<String,String> hm = new HashMap<String,String>();
//		HashMap<String,LevelValue> map = new HashMap<String,LevelValue> ();

		Map<String, List<LevelValue>> map = new HashMap<String, List<LevelValue>>();
		StringBuffer fromitem = new StringBuffer();
		StringBuffer nameidx = new StringBuffer();
		StringBuffer codeidx = new StringBuffer();
		StringBuffer ctrllevel = new StringBuffer();
		StringBuffer stridx =  new StringBuffer();
        StringBuffer mainorgs = new StringBuffer();
//		try{
			IdBdcontrastVO[] bdcontrast = null;
			billtype = CtlSchemeCTL.parseBillTypes(billtype);
			if(billtype!=null&&!"".equals(billtype)){
				bdcontrast = BdContrastCache.getNewInstance().getVoBySysAndBill(sys, billtype);
			}else{
				bdcontrast = BdContrastCache.getNewInstance().getVoBySysid(sys);
			}
			for(DataContrastVO contrastVO : contrastVos) {
				LevelValue[] levelvalues = contrastVO.getLevelValueList().toArray(new LevelValue[0]);
				for(int i=0;i<levelvalues.length;i++){
					LevelValue levelvalue = levelvalues[i];
				    String classId = CtrlRuleCTL.getClassIDByDimLevel(levelvalue);
				    /**对于在普通维上的主体和部门需要做特殊处理*/
				    if(IDimLevelPKConst.ENT.equals(classId)){
				        for(int n=0;n<(ctrlSchmBvos==null?0:ctrlSchmBvos.length);n++){
				        	IdCtrlschmBVO bvo = ctrlSchmBvos[n];
				        	if(bvo.isMainOrg().booleanValue()){   //如果是主组织
				        		IdBdcontrastVO vo = BdContrastCache.getNewInstance().getVoByPK(bvo.getPk_bdcontrast());
				        		String realClassId = vo.getPk_bdinfo();
				        		IGeneralAccessor accesssor  = GeneralAccessorFactory.getAccessor(realClassId);
				        		IBDData data = accesssor.getDocByPk((String)levelvalue.getKey());
				        		if(data!=null) {
				        			if(map.containsKey(vo.getAtt_fld()))
				        				map.get(vo.getAtt_fld()).add(levelvalue);
				        			else {
				        				List<LevelValue> valueList = new ArrayList<LevelValue>();
				        				valueList.add(levelvalue);
				        				map.put(vo.getAtt_fld(), valueList);
				        			}
				        		}
				        	}
				        }
				    }
					if(bdcontrast!=null){
						boolean isExistExt = false;
						for (int j = 0; j < bdcontrast.length; j++) {
							if(classId!=null){
								if (classId.equals(bdcontrast[j].getPk_bdinfo())){
									if(map.containsKey(bdcontrast[j].getAtt_fld()))
				        				map.get(bdcontrast[j].getAtt_fld()).add(levelvalue);
				        			else {
				        				List<LevelValue> valueList = new ArrayList<LevelValue>();
				        				valueList.add(levelvalue);
				        				map.put(bdcontrast[j].getAtt_fld(), valueList);
				        			}
									isExistExt = true;
									break;
								}
							}
						}
						if(isExistExt){
							continue;
						}
					}
				}
			  }
				for(int i=0;i<ctrlSchmBvos.length;i++){
					//普通维字段
					if(map.containsKey(ctrlSchmBvos[i].getDatafrom())){
						List<LevelValue> dms = map.get(ctrlSchmBvos[i].getDatafrom());
						if(i!=ctrlSchmBvos.length-1){
							fromitem.append(ctrlSchmBvos[i].getDatafrom() + ":");
							ctrllevel.append(ctrlSchmBvos[i].getCtllevelflag() + ":");
							mainorgs.append(ctrlSchmBvos[i].isMainOrg() + ":");

							for(int j = 0 ; j < dms.size() ; j++) {
								if(dms.get(j).getName()!=null){
									if(contrastVos.get(0).isNoSaveLevelValue()){
										nameidx.append("null");
									}else{
										nameidx.append(dms.get(j).getName());
										if(j != dms.size() - 1)
											nameidx.append("#");
									}
								}
								else{
									nameidx.append("null");
								}
								if(dms.get(j).getCode()!=null){
									if(contrastVos.get(0).isNoSaveLevelValue()){
										codeidx.append("null");
									}else{
										codeidx.append(dms.get(j).getCode());
										if(j != dms.size() - 1)
											codeidx.append("#");
									}
								}
								else{
									codeidx.append("null");
								}
								if(dms.get(j).getKey()!=null){
									if(contrastVos.get(0).isNoSaveLevelValue()){
										stridx.append("null");
									}else{
										stridx.append(dms.get(j).getKey());
										if(j != dms.size() - 1)
											stridx.append("#");
									}
								}
								else{
									stridx.append("null");
								}
							}
//							if(!"null".equals(stridx.toString())) {
//								nameidx.replace(nameidx.length() - 1, nameidx.length(), ":");
//								codeidx.replace(codeidx.length() - 1, codeidx.length(), ":");
//								stridx.replace(stridx.length() - 1, stridx.length(), ":");
//							} else {
							nameidx.append(":");
							codeidx.append(":");
							stridx.append(":");
//							}
						}
						else{
							fromitem.append(ctrlSchmBvos[i].getDatafrom());
							ctrllevel.append(ctrlSchmBvos[i].getCtllevelflag());
							mainorgs.append(ctrlSchmBvos[i].isMainOrg());
							for(int j = 0 ; j < dms.size() ; j++) {
								if(dms.get(j).getName()!=null&&!contrastVos.get(0).isNoSaveLevelValue()){
									nameidx.append(dms.get(j).getName());
									if(j != dms.size() - 1)
										nameidx.append("#");
								}
								else{
									nameidx.append("null");
								}
								if(dms.get(j).getCode()!=null&&!contrastVos.get(0).isNoSaveLevelValue()){
									codeidx.append(dms.get(j).getCode());
									if(j != dms.size() - 1)
										codeidx.append("#");
								}
								else{
									codeidx.append("null");
								}
								if(dms.get(j).getKey()!=null&&!contrastVos.get(0).isNoSaveLevelValue()){
									stridx.append(dms.get(j).getKey());
									if(j != dms.size() - 1)
										stridx.append("#");
								}
								else{
									stridx.append("null");
								}
							}
//							if(!"null".equals(stridx.toString())) {
//								nameidx.replace(nameidx.length() - 1, nameidx.length(), "");
//								codeidx.replace(codeidx.length() - 1, codeidx.length(), "");
//								stridx.replace(stridx.length() - 1, stridx.length(), "");
//							}
							nameidx.append(":");
							codeidx.append(":");
							stridx.append(":");
						}

					}
					//参数为字段
					else{
						if(i!=ctrlSchmBvos.length-1){
							fromitem.append(ctrlSchmBvos[i].getDatafrom() + ":");
							nameidx.append(ctrlSchmBvos[i].getBasename() + ":");
							codeidx.append(ctrlSchmBvos[i].getBasecode() + ":");
							ctrllevel.append(ctrlSchmBvos[i].getCtllevelflag() + ":");
							mainorgs.append(ctrlSchmBvos[i].isMainOrg()+ ":");
							stridx.append(ctrlSchmBvos[i].getPk_base()+":");
						}
						else{
							fromitem.append(ctrlSchmBvos[i].getDatafrom());
							ctrllevel.append(ctrlSchmBvos[i].getCtllevelflag());
							mainorgs.append(ctrlSchmBvos[i].isMainOrg());
							stridx.append("".equals(ctrlSchmBvos[i].getPk_base()) ? "null" : ctrlSchmBvos[i].getPk_base());
							nameidx.append("".equals(ctrlSchmBvos[i].getBasename()) ? "null" : ctrlSchmBvos[i].getBasename());
							codeidx.append("".equals(ctrlSchmBvos[i].getBasecode()) ? "null" : ctrlSchmBvos[i].getBasecode());

						}
					}
				}
				hm.put("fromitem", fromitem.toString());
				hm.put("nameidx", replace(nameidx.toString()));
				hm.put("codeidx", codeidx.toString());
				hm.put("ctrllevel", ctrllevel.toString());
				hm.put("stridx", stridx.toString());
				hm.put("mainorgs", mainorgs.toString());

		return hm;
	}
	//公式里的组织名称逗号的特殊处理
	public static String replace(String content){
		String replace="@CONTENT@";
		if(content!=null&&content.contains(",")){
			content=content.replaceAll(",", replace);
		}
		content=BracketsHelper.replaceNotBack(content.toString());
		return content;
	}
	//公式里的特殊处理组织名称还原为有逗号的组织名称
	public static String place(String content){
		String replace="@CONTENT@";
		if(content!=null&&content.contains(replace)){
			content=content.replaceAll(replace, ",");
		}
		return content;
	}
	public static IBDData[] getBddataVo(String pk, String pk_bdinfo,String pk_org,String sys) throws Exception
    {
		List<IBDData> datavos = null;
        if(pk == null)
        {
            return null;
        } else
        {

            IdBdcontrastVO vo = BdContrastCache.getNewInstance().getVOByField(sys,pk_bdinfo);
            IGeneralAccessor accessor = GeneralAccessorFactory.getAccessor(vo.getPk_bdinfo());
            if(!(pk.indexOf("|")>=0)&&!pk.equals(OutEnum.NOSUCHBASEPKATSUBCORP)){
            	datavos = accessor.getChildDocs(pk_org, pk, false);
            }
            return datavos==null?null:datavos.toArray(new IBDData[0]);
        }
    }

	/**
	 * 计划上设置的启动和未启动的控制方案
	 * */
	public static ArrayList<IdCtrlformulaVO> getPlanStartAndStopCtrlformulaVO(String pk_cube) throws BusinessException{
		try{
			/**
			 * pk_parent=null是计划上设置的控制方案
			 * 模型上的控制方案没有停用状态，即isstarted = 'N'，只有计划上的控制方案有停用状态
			 * */
			HashMap<String, IdCtrlformulaVO> map = new HashMap<String, IdCtrlformulaVO>();
			String sWhere = "pk_cube = '" + pk_cube + "'";
			IdCtrlformulaVO[] vos = (IdCtrlformulaVO[])NtbSuperServiceGetter.getINtbSuper().queryByCondition(IdCtrlformulaVO.class, sWhere);
			ArrayList<IdCtrlformulaVO> list = new ArrayList<IdCtrlformulaVO> ();
			if(vos!=null){
				list.addAll(Arrays.asList(vos));
			}
			return list;
		}catch (Exception e) {
			NtbLogger.error(e);
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052")/*未知异常*/,e);
		}
	}

	public static SingleSchema singleAndSepSchema(String express,DataCell cell,CubeDef def,String scheme_type) throws BusinessException ,Exception{
		String srcf = express;
		SingleSchema schema = new SingleSchema(srcf,cell,scheme_type);
		DimFormulaMacro macro = new DimFormulaMacro();
		FormulaDimCI m_env = new FormulaDimCI();
		m_env.setDataCell(cell);
		schema.instanceSchema(srcf);
		srcf = macro.getComplexParsedCorpAndCurrency(m_env, srcf, schema);
		schema.instanceSchema(srcf);
		srcf = macro.getParsedSingleAndComplexFormula(m_env, srcf,schema,scheme_type);
		schema.instanceSchema(srcf);
		return schema;
	}

	public static SingleSchema groupSchema(String express ,DataCell cell,CubeDef def) throws BusinessException ,Exception{
		String srcf = express;
		/**通过单元格和公式的具体内容实例化*/
		SingleSchema schema = new SingleSchema(srcf,cell,IRuleClassConst.SCHEMA_GROUP);
		DimFormulaMacro macro = new DimFormulaMacro();
		FormulaDimCI m_env = new FormulaDimCI();
		m_env.setDataCell(cell);
		schema.instanceSchema(srcf);
		/**组织和币种替换*/
		srcf = macro.getComplexParsedCorpAndCurrency(m_env, srcf, schema);
		schema.instanceSchema(srcf);
		srcf = macro.getParsedGroupFormula(cell, srcf,schema);
		schema.instanceSchema(srcf);

		return schema;
	}


	public static ArrayList<IdCtrlschemeVO> convertIdCtrlscheme(SingleSchema schema,String formulaPk, UFndExecuteContext context) throws Exception{
		ArrayList<IdCtrlschemeVO> list = new ArrayList<IdCtrlschemeVO> ();
		String[] src_ufind = schema.getUFind();
		String[] src_prefind = schema.getPREUFind();
		String[] split = ((SingleSchema)schema).getFormulaExpress().split("FIND");
		if(src_ufind.length+src_prefind.length!=split.length-1){
			throw new RuntimeException("ParseError");
		}
		try{
			int count = 1;
			int pre_count = 1;
			for(int n=0 ; n<(src_ufind==null?0:src_ufind.length) ; n++ ){
			  convertUfindCtrlscheme(src_ufind[n],formulaPk,IFormulaFuncName.UFIND,list,context,count);
			  count++;
			}
			for(int m=0 ; m<(src_prefind==null?0:src_prefind.length) ; m++ ){
			  convertUfindCtrlscheme(src_prefind[m],formulaPk,IFormulaFuncName.PREFIND,list,context,pre_count);
			  pre_count++;
			}

		}catch(Exception ex){
			NtbLogger.error(ex);
			throw ex;
		}

		return list;
	}

	public static ArrayList convertUfindCtrlscheme(String express,String formulaPk,String methodName,ArrayList<IdCtrlschemeVO> list, UFndExecuteContext context, int i) throws Exception{

		ConvertToCtrlSchemeVO newConvertor = new ConvertToCtrlSchemeVO(express,methodName);
		
		ConvertToCtrlSchemeVO[] newConvertors = newConvertor.composeCtrlSchmByBillType();
		
		for(ConvertToCtrlSchemeVO convertor : newConvertors) {
			IdCtrlschemeVO schemevos = new IdCtrlschemeVO();

			if(convertor.getPkOrg().equals("null")||convertor.getPkOrg().equals("")){
				throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000177")/*预算主体没有关联组织单元！*/);
			}else{
				schemevos.setPk_org(convertor.getPkOrg());
			}
			if(convertor.getPkCurrency().equals("null")||convertor.getPkCurrency().equals("")){
				throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000178")/*币种没有参照外币档案！*/);
			}else{
				schemevos.setPk_currency(convertor.getPkCurrency());
			}
			schemevos.setMethodname(methodName);
//			schemevos.setStridx(convertor.getPkIdx());//CtlBdinfoCTL.getActualPk(convertor));
			schemevos.setStridx(CtlBdinfoCTL.getAccountActualPk(convertor, context));
			schemevos.setCtrlsys(convertor.getCtrlSys());
			schemevos.setBilltype(convertor.getBillType());
			schemevos.setCtrldirection(convertor.getCtrlDirection());
			schemevos.setCtrlobj(convertor.getCtrlObject());
			schemevos.setCtrlobjValue(convertor.getCtrlObjectValue());
			schemevos.setIncludeuneffected(convertor.getUneffenctdata());
			schemevos.setStartdate(convertor.getStartDate());
			schemevos.setEnddate(convertor.getEndDate());
			schemevos.setAccctrollflag(convertor.getAccCtrlFlag());
			schemevos.setCurrtype(getCurrencyType(convertor.getPkCurrency()));
			schemevos.setPk_ncentity(convertor.getPkNcentity());
			schemevos.setFromitems(convertor.getFromItem());
			schemevos.setCodeidx(convertor.getCodeIdx());
			schemevos.setCtllevels(convertor.getCtrlLevel());
			schemevos.setSchtype(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01420ctl_000092")/*控制方案*/);
			schemevos.setSchemetype(IPKRuleConst.SCHEMA_SPEC);
			schemevos.setRundata(new UFDouble(0));
			if(IFormulaFuncName.UFIND.equals(methodName)){
				schemevos.setVarno("var"+i);
			}else if(IFormulaFuncName.PREFIND.equals(methodName)){
				schemevos.setVarno("rar"+i);
			}
//			String[] pkIdx = schemevos.getStridx().split(ConvertToCtrlSchemeVO.SEPERATOR);
//
//			String[] nameIdx = convertor.getNameIdx().split(ConvertToCtrlSchemeVO.SEPERATOR);
//			String realOrgName = context.getOrgContext().replaceOrgName(pkIdx[convertor.getMainOrgIndex()], nameIdx[convertor.getMainOrgIndex()]);
//			String realNameIdx = convertor.getNameIdx().replaceFirst(UFndOrgContext.UN_DIMMEMBER_ORG, realOrgName);
			schemevos.setNameidx(convertor.getNameIdx());
			//日期类型，ARAP
			schemevos.setDatetype(convertor.getDataCatalg());
			schemevos.setIsstarted(UFBoolean.valueOf("Y"));
			/**对于多个会计科目的选择,在这里进行分解*/
			list.add(schemevos);
		}
		return list;
    }

	public static IdCtrlformulaVO convertIdCtrlFormula(DataCell cell, SingleSchema vo,IdCtrlschemeVO[] vos, /*String formulaPk*/DimFormulaVO dfvo) throws Exception {
		IdCtrlformulaVO formulavo = new IdCtrlformulaVO();
		// 公式类型
		String[] src_ufind = vo.getUFind();
		String[] src_prefind = vo.getPREUFind();
//		formulavo.setSchemetype(CtlSchemeCTL.getCtlType(formulaPk));
		formulavo.setSchemetype(getCtlType(dfvo));
		String[] temp = vo.getExpressFormula(vos); // 慢
		String formulaSrc = temp[0];
		String pkList = temp[1];
		formulavo.setExpressformula(formulaSrc);
		if (cell.getCellValue()!=null&&cell.getCellValue().getValue() != null) {
//			Number cellvalue = (Number) cell.getCellValue().getValue();
//			UFDouble value = new UFDouble(cellvalue.doubleValue());
//			int index = getIndex(formulaSrc);
			int index1 = formulaSrc.indexOf("%");
			String left_formula = formulaSrc.substring(0, index1);
			UFDouble complexPlanValue = null;
			try{
			    complexPlanValue = getComplexZxs(left_formula + "/100");
			}catch(Exception ex){
				NtbLogger.print(ex);
				formulavo.setPlanvalue(new UFDouble(0));
			}
			formulavo.setPlanvalue(complexPlanValue);
		} else {
			formulavo.setPlanvalue(new UFDouble(0));
		}
		/**
		 * 单项方案和组方案的处理,需要提前计算100/100(百分比)，在启动控制方案运算比在保存凭证做运算快，提高效率
		 * */
		if (formulavo.getSchemetype().equals(IRuleClassConst.SCHEMA_SINGLE)||formulavo.getSchemetype().equals(IRuleClassConst.SCHEMA_GROUP)) {

//			int index = formulaSrc.indexOf("/");
//			String endsrc = formulaSrc.substring(index + 4);
//
//			formulaSrc = formulaSrc.substring(0, index);
//			formulaSrc = formulaSrc + "/100";
//			UFDouble value = vo.calcPlanValue(formulaSrc);
//			/**
//			 * (1)单项方案和组方案应该是运算过的表达式 (2)计划值应该是运算过的数值，联查预算时计算余额用
//			 * */
//			formulavo.setExpressformula(value.toString() + endsrc);
//			formulavo.setPlanvalue(value);
		} else if (formulavo.getSchemetype().equals(IRuleClassConst.SCHEMA_SPEC)) {

			/**
			 * 特殊方案的表达式100*100/100>varno不能转换成100>varno
			 * (Find()+UFind()-SMFind())*90/100<SMFind()+UFind()-Find()替换的结果
			 * (100+Varno1-200)*90/100< 200+Varno2-100
			 *
			 * (1)所以特殊方案没法在启动控制方案的做公式运算 (2)特殊方案的planvalue不需要计算出来，因为联查预算不联查特殊方案
			 * (3)特殊方案和控制所有公司方案的planvalue=0;
			 * (4)ntb_id_ctrlformula表中的planvalue字段只用于在联查预算时候用到
			 * ，而联查预算时不需要特殊方案，所以可以设置特殊方案的planvalue=0
			 * */
			formulavo.setExpressformula(formulaSrc);
			if (cell.getCellValue().getValue() != null) {
				Number cellvalue = (Number) cell.getCellValue().getValue();
				UFDouble value = new UFDouble(cellvalue.doubleValue());
				formulavo.setPlanvalue(value);
			} else {
				formulavo.setPlanvalue(new UFDouble(0));
			}
		} else {
			formulavo.setExpressformula(formulaSrc);
			if (cell.getCellValue().getValue() != null) {
				Number cellvalue = (Number) cell.getCellValue().getValue();
				UFDouble value = new UFDouble(cellvalue.doubleValue());
				formulavo.setPlanvalue(value);
			} else {
				formulavo.setPlanvalue(new UFDouble(0));
			}
		}
//		DimFormulaVO temp_dimformulavo = FormulaCTL.getDimFormulaByPrimaryKey(formulaPk);
//		if(temp_dimformulavo == null)
//    		throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl_0","01050ctrl003-0001")/*@res "没有找到控制规则："*/ + formulaPk + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl_0","01050ctrl003-0002")/*@res "请刷新界面后重试"*/);
		DimFormulaVO temp_dimformulavo = dfvo;
		if(temp_dimformulavo == null)
    		throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl_0","01050ctrl003-0001")/*@res "没有找到控制规则："*/ + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl_0","01050ctrl003-0002")/*@res "请刷新界面后重试"*/);
		
//		DimFormulaVO temp_dimformulavo = NtbFormulaCache.getNewInstance().getDimFormulaVOByPk(formulaPk);
//		formulavo.setPk_parent(formulaPk);
//		formulavo.setPk_plan(cell.getPk_plssan());
		formulavo.setPk_parent(temp_dimformulavo.getPrimaryKey());
		IStringConvertor cvt = StringConvertorFactory.getConvertor(DimVector.class);
		String pk_cell = cvt.convertToString(cell.getDimVector());
		formulavo.setPk_dimvector(pk_cell);
		formulavo.setPk_cube(cell.getCubeDef().getPrimaryKey());
		formulavo.setCtlmode(String.valueOf(temp_dimformulavo.getAtt() == null ? "": temp_dimformulavo.getAtt()));
		formulavo.setIsstarted(UFBoolean.valueOf("Y"));
		// 控制方案名称
		formulavo.setCtrlname(temp_dimformulavo.getObjname());
		// 控制百分比
		formulavo.setCtrlpercent(vo.getControlpercent(temp_dimformulavo.getFullcontent()));
		// 控制符
		formulavo.setCtrlsign(vo.getControlsign());
		// 计划pkList
		formulavo.setPlanlist(pkList);
		formulavo.setSpecialUsage(temp_dimformulavo.getSpecialUsage());

		return formulavo;
	}



	public static Map<String, List<NtbParamVO>> sortVOsBySys(
			IdCtrlschemeVO[] ctlvos, UFndExecuteContext context) throws Exception {
		try {
			// 构造NtbParamVO,包括公司控制下级的paramvo
			NtbParamVO[] params = parseCtrls(ctlvos, context);

			Map<String, List<NtbParamVO>> map = new HashMap<String, List<NtbParamVO>>();
			for (int i = 0; i < params.length; i++) {
				String sys = params[i].getSys_id();
				if (map.containsKey(sys)) {
					List<NtbParamVO> list = (List<NtbParamVO>) map
							.get(sys);
					list.add(params[i]);
				} else {
					List<NtbParamVO> list = new ArrayList<NtbParamVO>();
					list.add(params[i]);
					map.put(sys, list);
				}
			}

			return map;
		} catch (Exception ex) {
			NtbLogger.error(ex);
			throw ex;
		}

	}

	// 从业务系统查询控制点，判断参数是否包含未生效单据
	public static NtbParamVO[] setIncludeEff(IBusiSysExecDataProvider exeprovider,
			NtbParamVO[] params) throws BusinessException {
		HashMap hashCorp2Point = new HashMap();
		for (int i = 0; i < params.length; i++) {

			int ctlpoint = 0;
			boolean isIncludeeff = false;
			if (hashCorp2Point.containsKey(params[i].getPk_Org())) {
				ctlpoint = ((Integer) hashCorp2Point.get(params[i].getPk_Org()))
						.intValue();
			} else {
				try {
					ctlpoint = exeprovider.getCtlPoint(params[i].getPk_Org());
				} catch (Exception ex) {
					NtbLogger.error(ex);
					ctlpoint = 0;
				}
				hashCorp2Point.put(params[i].getPk_Org(), Integer.valueOf(ctlpoint));
			}

			if (ctlpoint == 0) {// 保存阶段
				isIncludeeff = true;
			} else if (ctlpoint == 1) {// 审核阶段
				isIncludeeff = false;
			}
			params[i].setIsUnInure(isIncludeeff);
		}

		return params;
	}


	private static NtbParamVO[] parseCtrls(IdCtrlschemeVO[] ctlvos, UFndExecuteContext context) throws Exception {

		try {
			String spliter = ":";
			IBusiSysReg resaReg = null;
			ArrayList<NtbParamVO> listParams = new ArrayList<NtbParamVO>();
			SubLevelOrgGetter orgLevGetter = new SubLevelOrgGetter();
			for (int i = 0; i < ctlvos.length; i++) {
				NtbParamVO paramvo = new NtbParamVO();
				String funName = null;
				if (ctlvos[i].getMethodname() != null) {
					funName = ctlvos[i].getMethodname();
				}

				String pk_org = ctlvos[i].getPk_org();
				String billtype = parseBillTypes(ctlvos[i].getBilltype());
				String sysId = ctlvos[i].getCtrlsys();
				paramvo.setMethodCode(funName);
				paramvo.setSys_id(sysId);

				/** 获取对应的动作,影响远程调用次数,做缓存处理 */
				if (billtype != null) {
					if (!(billtype.indexOf(",") > 0)) { // 单个单据
						HashMap<String, String> actionMap = CtrltacticsCache
								.getNewInstance()
								.getActionByBillTypeAndSysId(
										paramvo.getSys_id(),
										parseBillTypes(ctlvos[i].getBilltype()),
										paramvo.getMethodCode());
						paramvo.setActionMap(actionMap);
						HashMap<String, HashMap<String, String>> _map = new HashMap<String, HashMap<String, String>>();
						_map.put(billtype, actionMap);
						paramvo.setBillTypesActionMap(_map);
					} else {
						String[] billtypes = parseBillTypes(
								ctlvos[i].getBilltype()).split(",");
						HashMap<String, HashMap<String, String>> map = new HashMap<String, HashMap<String, String>>();
						for (int n = 0; n < billtypes.length; n++) {
							String _billtype = billtypes[n];
							HashMap<String, String> actionMap = CtrltacticsCache
									.getNewInstance()
									.getActionByBillTypeAndSysId(
											paramvo.getSys_id(), _billtype,
											paramvo.getMethodCode());
							map.put(_billtype, actionMap);
						}
						paramvo.setBillTypesActionMap(map);
					}
				}
				/** 结束 */
				/**
				 * UFind()取数用 songrui add 2008.11.25
				 * 控制方案取数时会在后面覆盖一遍，通过setIncludeEff()方法，UFind()取数和控制方案取数都不会有影响
				 * */
				/**
				 * 这个地方的setIsUnInure()方法对UFind()取数有用，对于控制来说没有意义，可以用这个字段设置是否包含未生效
				 * 字段includeuneffected字段的两个意义：(1)取数,包含未生效数据(2)控制,特殊方案+累进+包含期初余额
				 **/
				paramvo.setIsUnInure(ctlvos[i].getIncludeuneffected()
						.booleanValue());

				// 责任会计--是否是会计期间 特殊处理 dengyh 2011-05-31
				if (OutEnum.RESASYS.equalsIgnoreCase(sysId)) {
					resaReg = CtlSchemeCTL.getBusiSysReg(OutEnum.RESASYS);
					paramvo.setIsKjqj(resaReg.isUseAccountDate(billtype));
				} /*
				 * else if (OutEnum.GLSYS.equalsIgnoreCase(sysId) &&
				 * ctlvos[i].getCtrlobj().indexOf("balance") >= 0) {
				 * paramvo.setIsKjqj(true); }
				 */else {
					paramvo.setIsKjqj(false);
				}
				if (ctlvos[i].getStartdate() != null) {
					paramvo.setBegDate(ctlvos[i].getStartdate());
					paramvo.setEndDate(ctlvos[i].getEnddate());
				}
				paramvo.setPk_Org(pk_org);
				paramvo.setBill_type(billtype);
				paramvo.setData_attr(ctlvos[i].getCtrlobj());
				paramvo.setData_attrExt(ctlvos[i].getCtrlobjValue());
				/** 对各业务系统会计期间的统一处理 */
				dealAccountDate(paramvo);

				paramvo.setPk_ctrl(ctlvos[i].getPrimaryKey());
				/** 控制方案主表(公式表)pk */
				paramvo.setGroupname(ctlvos[i].getPk_ctrlformula());
				/** 业务单元 */
				paramvo.setPk_org_book(pk_org); // 针对总帐,责任会计
				paramvo.setPk_accentity(pk_org);
				String bdinfotype = CtlSchemeCTL.getBdinfoType(
						ctlvos[i].getFromitems(), ctlvos[i].getCtrlsys());
				/** 基本档案类型 */
				String[] bdinfotypeidx = bdinfotype.split(spliter);
				/** 基本档案控制下级 */
				String[] ctrllevel = ctlvos[i].getCtllevels().split(spliter);
				/** 判断主组织是否控制下级 */
				boolean isControlDownCorp = false;
				for (int j = 0; j < bdinfotypeidx.length; j++) {
					/** 现在暂时只有资金和销售能够体现上下级关系 */
					if (bdinfotypeidx[j].equals(OutEnum.ZJORG)
							|| bdinfotypeidx[j].equals(OutEnum.XSOGR)) {
						Boolean value = new Boolean(ctrllevel[j]);
						isControlDownCorp = value.booleanValue();
						break;
					}
				}
				/** 判断是否控制所有公司需要考虑部门档案做为主体的情况 */
				boolean isControlAllCorp = false;
				/**
				 * (1)部门档案作主体可以根据当前部门档案取到公司的PK
				 * (2)单项方案和组方案都会根据当前单元格实例化控制方案，所以肯定有公司PK，不能出现控制全部公司的情况
				 * (3)特殊方案设置如果没有公司目录时，应该实例化pk_corp = "0001",控制全部公司
				 * (4)控制所有公司的条件就是pk_corp=0001的特殊方案，单项方案和组方案不会出现这种情况
				 * */
				/** 判断pk_org是否为集团 */
				// boolean isGroupType =
				// OrgTypeManager.getInstance().isTypeOfByPk(pk_org,
				// IOrgConst.GROUPORGTYPE);
//				boolean isGroupType = NtbOrgTypeCache.getNewInstance()
//						.getOrgType(pk_org, IOrgConst.GROUPORGTYPE);
//				if (false/* isGroupType */) {
//					isControlAllCorp = true;
//				}
				/** 具体币种PK,本币也计算出来具体币种,报销和收付有特殊要求,如果是控制本币的话,是不需要传具体币种 */
				String pk_currency = CtlSchemeCTL.getPk_currency(
						ctlvos[i].getPk_currency(), ctlvos[i].getPk_org(),
						sysId);
				paramvo.setPk_currency(pk_currency);
				/** 全局本币(0),集团本币(1),组织本币(2),原币(3) */
				paramvo.setCurr_type(CtlSchemeCTL.getCurrencyType(ctlvos[i]
						.getPk_currency()));
				paramvo.setSys_id(ctlvos[i].getCtrlsys());
				// paramvo.setBill_type(parseBillTypes(ctlvos[i].getBilltype()));

				paramvo.setDateType(ctlvos[i].getDatetype());
				paramvo.setDirection(ctlvos[i].getCtrldirection());

				/* 控制取数---0 执行取数--1 */
				paramvo.setCtrlstatus(0);
				// 所有pk为null的字段都应该去掉，ex.外币档案，如果对应的本币或者是辅币，则pk=null,在参数NrbParamVO中不允许出现为null的字段，需要过滤
				String[] att = filterStridx(ctlvos[i], bdinfotypeidx);

				//判断是否有会计科目档案
				boolean isHasAccount = false;
				String[] fromItems = ctlvos[i].getFromitems().split(ConvertToCtrlSchemeVO.SEPERATOR);
				for(String fromItem : fromItems) {
					IdBdcontrastVO bdvo = BdContrastCache.getNewInstance().getVOByField(ctlvos[i].getCtrlsys(), fromItem);
					if(bdvo != null && (bdvo.getPk_bdinfo().equals(IMetaDataConst.STR_ID_ACCOUNT) || bdvo.getPk_bdinfo().equals(IMetaDataConst.STR_ID_ELEMENTSACCOUNT)))
						isHasAccount = true;
				}
				if(isHasAccount){
					CtlBdinfoCTL.getLinkActualPk(ctlvos[i], context);
					paramvo.setPkDim(ctlvos[i].getStridx().split(":"));
				}else{
					paramvo.setPkDim(ctlvos[i].getStridx().split(":"));
				}
//				paramvo.setPkDim(att[0].split(spliter/* ":" */));
				paramvo.setBusiAttrs(att[1].split(spliter/* ":" */));
				String[] ctrllevels = att[2].split(spliter/* ":" */);
				boolean[] value = new boolean[ctrllevels.length];
				HashMap<String, String[]> leveldownMap = new HashMap<String, String[]>();

				IdBdcontrastVO orgbdvo = BdContrastCache.getNewInstance().getVOByField(paramvo.getSys_id(), paramvo.getOrg_Attr());

				for (int j = 0; j < ctrllevels.length; j++) {
					value[j] = UFBoolean.valueOf(ctrllevels[j]).booleanValue();
					/** 如果是下级需要取数 */
					if (value[j]) {
						if(!paramvo.getPkDim()[j].equals(paramvo.getPk_Org())) {
							String[] levelDowsPks = CtlBdinfoCTL.getBdChilddataVO(
									paramvo.getPkDim()[j],
									paramvo.getBusiAttrs()[j], paramvo.getPk_Org(),
									paramvo.getSys_id(),orgbdvo.getPk_bdinfo(), paramvo.getEndDate(), true, context);
							leveldownMap.put(paramvo.getBusiAttrs()[j],
									levelDowsPks);
						} else {
							String[] levelDownPks = orgLevGetter.getSubLevelOrgsByOrgAndBd(
									paramvo.getPk_Org(),
									paramvo.getBusiAttrs()[j], paramvo.getSys_id());
							leveldownMap.put(paramvo.getBusiAttrs()[j], levelDownPks);
						}
					}
				}
				paramvo.setLowerArrays(leveldownMap);
				paramvo.setIncludelower(value);
				paramvo.setTypeDim(att[3].split(spliter));
				paramvo.setCode_dims(att[4].split(spliter));
				paramvo.setVarno(ctlvos[i].getVarno());
				paramvo.setCtrlscheme(ctlvos[i].getPrimaryKey());
				/** 对公司目录控制下级的处理,要考虑部门档案做为主体的情况 */
				if (isControlDownCorp) {
					listParams.add(paramvo);
					// CtlSchemeCTL.addCorpParams(paramvo,listParams,ctlvos[i],true,
					// bdAccCache);
				}/** 控制集团的处理 */
				else if (isControlAllCorp) {
					listParams.add(paramvo);
					CtlSchemeCTL.addGroupDownAllOrgParams(paramvo, listParams,
							ctlvos[i]);
				} else {
					listParams.add(paramvo);
				}
				validateNtbParamVO(paramvo);
			}
			return (NtbParamVO[]) listParams.toArray(new NtbParamVO[0]);
		} catch (Exception ex) {
			NtbLogger.error(ex);
			throw ex;
		}
	}

	public static String[] filterStridx(IdCtrlschemeVO vo, String[] bdinfotypeidx) throws Exception {
		String sysid = vo.getCtrlsys();
		String[] att = new String[5];
		StringBuffer bf_PkDim = new StringBuffer();
		StringBuffer bf_BusiAttrs = new StringBuffer();
		StringBuffer bf_Includelower = new StringBuffer();
		StringBuffer bf_TypeDim = new StringBuffer();
		StringBuffer bf_Code_dims = new StringBuffer();

		String[] stridx = vo.getStridx().split(":");
		String[] fromitem = vo.getFromitems().split(":");
		String[] ctllevel = vo.getCtllevels().split(":");
		String[] nameidx = vo.getNameidx().split(":");
		String[] codeidx = vo.getCodeidx().split(":");
		String[] bdinfotype = BudgetControlCTL.getBdinfoType(fromitem, sysid).split(":");
		// 过滤掉字段中为null的字段，ARAP取数不支持有null的字段,NOSUCHBASEPKATSUBCORP该字段只对外币档案做过滤
		for (int i = 0; i < fromitem.length; i++) {
			if (!(stridx[i].equals(OutEnum.NOSUCHBASEPKATSUBCORP) && bdinfotype[i]
					.equals(OutEnum.CURRDOC))) {
				bf_PkDim.append(stridx[i] + ":");
				bf_BusiAttrs.append(fromitem[i] + ":");
				bf_Includelower.append(ctllevel[i] + ":");
				bf_TypeDim.append(bdinfotypeidx[i] + ":");
				bf_Code_dims.append(codeidx[i] + ":");
			}
		}
		att[0] = bf_PkDim.toString();
		att[1] = bf_BusiAttrs.toString();
		att[2] = bf_Includelower.toString();
		att[3] = bf_TypeDim.toString();
		att[4] = bf_Code_dims.toString();
		return att;
}

	public static void dealAccountDate(NtbParamVO vo) throws BusinessException {
		/**
		 * 卫姐的新需求: 预算的计划期间和存货核算的会计期间匹配规则：
		 * 将“预算的计划期间的起始结束日期”与预算组织所属财务组织或对应财务组织主账簿的会计期间方案下的
		 * 具体会计期间的起始结束日期匹配后传给存货核算系统。
		 */
		String sysid = vo.getSys_id();
		boolean iskjqj = vo.isKjqj();
		IBusiSysReg resaReg = CtlSchemeCTL.getBusiSysReg(sysid);
		boolean isUseAccountDate = resaReg.isUseAccountDate(vo.getBill_type());
		if (isUseAccountDate || iskjqj) {
			/** 对会计期间的处理,暂时怎么处理,正常情况下,应该根据各业务系统的帐簿来确定期间 yuyonga */
			/** 通过预算组织找到财务组织,如果不是财务组织,直接截取日期 */

			IGeneralAccessor financeorg_accesssor = GeneralAccessorFactory
					.getAccessor(IOrgMetaDataIDConst.FINANCEORG);
			IBDData financeorg_bddata = financeorg_accesssor.getDocByPk(vo
					.getPk_Org());
			String pk_accountingBook = null;
			if (OutEnum.RESASYS.equals(vo.getSys_id())) { // 责任会计是的主组织是责任核算帐簿
				financeorg_accesssor = GeneralAccessorFactory
						.getAccessor(IOrgMetaDataIDConst.LIABILITYBOOK);
				financeorg_bddata = financeorg_accesssor.getDocByPk(vo
						.getPk_Org());
				if (financeorg_bddata != null) {
					pk_accountingBook = vo.getPk_Org();
				}
			}
			if (financeorg_bddata == null) {
				String start = vo.getBegDate();
				String end = vo.getEndDate();
				String _strat = start.substring(0, 7);
				vo.setBegDate(_strat);
				String _end = end.substring(0, 7);
				vo.setEndDate(_end);
			} else {
				/** 是财务组织,如果是财务组织的话,并且有核算帐薄的话,就取核算帐簿,如果没有,直接截取 */
				if (pk_accountingBook == null) {
					pk_accountingBook = getPKORGByFINANCEId(vo.getPk_Org());
				}
				if (pk_accountingBook == null) {
					String start = vo.getBegDate();
					String end = vo.getEndDate();
					String _strat = start.substring(0, 7);
					vo.setBegDate(_strat);
					String _end = end.substring(0, 7);
					vo.setEndDate(_end);
				} else {
					String accperiod = null;
					if (OutEnum.RESASYS.equals(vo.getSys_id())) {
						ILiabilityBookPubService bookPubService = (ILiabilityBookPubService) NCLocator
								.getInstance().lookup(
										ILiabilityBookPubService.class
												.getName());
						accperiod = bookPubService
								.queryAccperiodCalendarIDByLiabilityBookID(pk_accountingBook);
					} else {
						IAccountingBookPubService bookPubService = (IAccountingBookPubService) NCLocator
								.getInstance().lookup(
										IAccountingBookPubService.class
												.getName());
						accperiod = bookPubService
								.queryAccperiodSchemeByAccountingBookID(pk_accountingBook);
					}

					AccountCalendar accountCalendar = getAccountCalendar(accperiod);
					String start = vo.getBegDate();
					String end = vo.getEndDate();
					if (start.length() > 7) {
						accountCalendar.setDate(new UFDate(start));
						vo.setBegDate(accountCalendar.getMonthVO().getYearmth());
					}
					if (end.length() > 7) {
						accountCalendar.setDate(new UFDate(end));
						vo.setEndDate(accountCalendar.getMonthVO().getYearmth());
					}
				}
			}
			/** 针对供应链存货核算做时间上的处理,如果选中的是JC单据,则把开始期间和结束期间合并为一个,取结束的 */
			if (OutEnum.IASYS.equals(sysid) && "JC".equals(vo.getBill_type())) {
				vo.setBegDate(vo.getEndDate());
			} /*
			 * else if (OutEnum.GLSYS.equalsIgnoreCase(sysid) &&
			 * vo.getData_attr().indexOf("balance") >= 0) {
			 * vo.setBegDate(vo.getBegDate() + "-01");
			 * vo.setEndDate(vo.getEndDate() + "-01"); }
			 */

		}
	}

	private static AccountCalendar getAccountCalendar(String accperiod) throws BusinessException {
		AccountCalendar accountCalendar = AccountCalendar.getInstanceByPeriodScheme(accperiod);
		return accountCalendar;
    }



	private static String getPKORGByFINANCEId(String pk_finance) throws BusinessException {
		String[] pk_orgs = new String[] { pk_finance };
		String pk_accountingBook = null;
		/** 通过财务组织的PK去寻找财务核算账簿 */
		IAccountingBookPubService bookPubService = (IAccountingBookPubService) NCLocator
				.getInstance()
				.lookup(IAccountingBookPubService.class.getName());
		Map<String, String> map = bookPubService
				.queryAccountingBookIDByFinanceOrgIDWithMainAccountBook(pk_orgs);
		if (map == null) {
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes()
					.getStrByID("tbb_ctrl", "01801ctl_000207")/* 此预算组织没有对应总帐系统下的核算帐簿 */);
		} else {
			pk_accountingBook = map.get(pk_finance);
			if (pk_accountingBook == null) {
				throw new BusinessException(NCLangRes4VoTransl.getNCLangRes()
						.getStrByID("tbb_ctrl", "01801ctl_000207")/* 此预算组织没有对应总帐系统下的核算帐簿 */);
			}
		}
		return pk_accountingBook;
    }



	/**
	 * 获取币种精度
	 *
	 * @param pk_currtype
	 * @return
	 * @throws BusinessException
	 */
	private static int getCurrtypeDigit(String pk_currtype) throws BusinessException {
		if (pk_currtype == null) {
			return 2;
		} else {
			return CurrtypeQuery.getInstance().getCurrdigit(pk_currtype);
		}
	}

	/**
	 * 计算实际发生数
	 * */
	public static UFDouble sumRunData(UFDouble[] value) {
		UFDouble sum = new UFDouble(0);
		for (int i = 0; i < value.length; i++) {
			sum = sum.add(value[i]);
		}
		return sum;
	}

	public static int getIndex(String src){
		int index = -1;
		if(src.indexOf(OutEnum.CTLSIGNARR[0])>-1){//>=
			return src.indexOf(OutEnum.CTLSIGNARR[0]) + 2;
		}
		if(src.indexOf(OutEnum.CTLSIGNARR[3])>-1){//<=
			return src.indexOf(OutEnum.CTLSIGNARR[3])+ 2;
		}
		if(src.indexOf(OutEnum.CTLSIGNARR[1])>-1){//>
			return src.indexOf(OutEnum.CTLSIGNARR[1]) + 1;
		}
		if(src.indexOf(OutEnum.CTLSIGNARR[2])>-1){//=
			return src.indexOf(OutEnum.CTLSIGNARR[2]) + 1;
		}
		if(src.indexOf(OutEnum.CTLSIGNARR[4])>-1){//<
			return src.indexOf(OutEnum.CTLSIGNARR[4]) + 1;
		}
		return index;
	}

	/**
	 * 执行数和计划数的比较 map<pk_group,ArrayList<IdCtrlschemeVO>>
	 * map1<pk_group,IdCtrlFormulaVO> paramap<pk_group,ArrayList<NtbParam>>
	 *
	 * IdCtrlschemeVO与NtbParam是一一对应
	 * */
	public static String[] compare(NtbParamVO[] paramvos, HashMap map, HashMap map1,ArrayList<CtrlSchemeVO> ctrlvos)
			throws Exception {

		HashMap<String, ArrayList<NtbParamVO>> paramap = new HashMap<String, ArrayList<NtbParamVO>>();
		Map<String, String> currencyMap = new HashMap<String, String>();
		Map<String, String> ctrlObjNameMap = new HashMap<String, String>();
		Map<String,MdTask >  taskMap = new HashMap<String, MdTask>();
		// 返回控制信息
		ArrayList info = new ArrayList<String>();
		for (int i = 0; i < paramvos.length; i++) {
			String pk = paramvos[i].getGroupname();
			if (paramap.containsKey(pk)) {
				ArrayList list = paramap.get(pk);
				list.add(paramvos[i]);
			} else {
				ArrayList list = new ArrayList();
				list.add(paramvos[i]);
				paramap.put(pk, list);
			}
		}
		Iterator iter = map.keySet().iterator();

		while (iter.hasNext()) {
			String ctrlObjName = null;
			String pk = (String) iter.next();
			ArrayList ntbparamlist = (ArrayList) paramap.get(pk);
			// 每一个控制方案都对应一个NtbParamVO
			if (ntbparamlist == null) {
				continue;
			}
			NtbParamVO[] paramvo = (NtbParamVO[]) ntbparamlist.toArray(new NtbParamVO[0]);
			ArrayList schemelist = (ArrayList) map.get(pk);
			IdCtrlschemeVO[] schemevo = (IdCtrlschemeVO[]) schemelist.toArray(new IdCtrlschemeVO[0]);

			IdCtrlformulaVO vo = (IdCtrlformulaVO) map1.get(pk);
			String formulaExpress = vo.getExpressformula();
			String pk_plan = vo.getPk_plan();
			IdCtrlschemeVO vos = schemevo[0];
			ArrayList<UFDouble> runls = new ArrayList<UFDouble>();
			ArrayList<String> list = new ArrayList<String>();
			int powerInt = 0;

			Map<String, String> varMap = new HashMap<String, String>();

			for (int i = 0; i < schemevo.length; i++) {
				UFDouble rundata = new UFDouble();
				UFDouble readydata = new UFDouble();
				String var = schemevo[i].getVarno();
				for (int j = 0; j < paramvo.length; j++) {
					if (paramvo[j].getVarno().equals(var)
							&& paramvo[j].getRundata() != null) {
						int currtype = paramvo[j].getCurr_type();
						if (paramvo[j].getRundata()[currtype] != null) {
							rundata = paramvo[j].getRundata()[currtype];
						}
						list.add(NCLangRes4VoTransl.getNCLangRes().getStrByID(
								"tbb_ctrl", "01801ctl_000009")/* 执行数 */);
						runls.add(rundata);
					}
					if (paramvo[j].getVarno().equals(var)
							&& paramvo[j].getReadydata() != null) {
						int currtype = paramvo[j].getCurr_type();
						if (paramvo[j].getReadydata()[currtype] != null) {
							readydata = paramvo[j].getReadydata()[currtype];
						}
						list.add(NCLangRes4VoTransl.getNCLangRes().getStrByID(
								"tbb_ctrl", "01801ctl_000010")/* 预占数 */);
						runls.add(readydata);
					}
				}
				varMap.put(var, (rundata.add(readydata)).toString());
//				if (formulaExpress.indexOf(var) > -1) {
//					formulaExpress = formulaExpress.replaceAll(var,
//							(rundata.add(readydata)).toString());
//				}
				String pk_currency = 	currencyMap.get(schemevo[i].getPk_currency()+schemevo[i].getPk_org()+schemevo[i].getCtrlsys());
				if(pk_currency==null){
					  pk_currency = CtlSchemeCTL.getActualPkcurrency(
							schemevo[i].getPk_currency(), schemevo[i].getPk_org(),
							schemevo[i].getCtrlsys());
					  currencyMap.put(schemevo[i].getPk_currency()+schemevo[i].getPk_org()+schemevo[i].getCtrlsys(), pk_currency);
				}

				powerInt = getCurrtypeDigit(pk_currency);
				ctrlObjName = ctrlObjNameMap.get(schemevo[i].getCtrlsys()+schemevo[i].getCtrlobj());
				if(ctrlObjName==null){
					ctrlObjName = CtlSchemeCTL.parseCtrlObjName(
							schemevo[i].getCtrlsys(), schemevo[i].getCtrlobj());
					ctrlObjNameMap.put(schemevo[i].getCtrlsys()+schemevo[i].getCtrlobj(), ctrlObjName);
				}

			}

//			String express1 = formulaExpress.split(vo.getCtrlsign())[0];
//			String express2 = formulaExpress.split(vo.getCtrlsign())[1];
//			express2 = CtlSchemeCTL.replaceExpressWithVar(express2, varMap);
//			formulaExpress = express1 + vo.getCtrlsign() + express2;
			formulaExpress = new CtrlExprManager().getValueExpress(formulaExpress, varMap);

			StringBuffer sbStr = new StringBuffer(NCLangRes4VoTransl
					.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000007")/* 【 */);
			for (int n = 0; n < list.size(); n++) {
				if (n != list.size() - 1) {
					sbStr.append(list.get(n)).append(",");
				} else {
					sbStr.append(list.get(n));
				}
			}
			sbStr.append(NCLangRes4VoTransl.getNCLangRes().getStrByID(
					"tbb_ctrl", "01801ctl_000050")/* 】 */);

			// 单项方案和组方案的执行数计算规则是"+"，特殊方案需要处理
			UFDouble sumvalue = sumRunData((UFDouble[]) runls
					.toArray(new UFDouble[0]));
			String[] ss = vos.getNameidx().split(":");
			StringBuffer buffer = new StringBuffer();
			/**得到主体的名称 yuyonga,这个PK肯定是预算组织,用预算组织名称显示*/
			String entityName = "";
			String entityPk = vos.getPk_org();
			String[] pkidx = vos.getStridx().split(":");
	        for(int n=0;n<pkidx.length;n++){
	        	if(entityPk!=null&&entityPk.equals(pkidx[n])){
	        		entityName = ss[n];
	        		break;
	        	}
	        }

	        MdTask plan = taskMap.get(vo.getPk_plan());
	        if(plan==null){
	        	  plan = TbTaskCtl.getMdTaskByPk(vo.getPk_plan(),true);  //获取计划的信息
	        	  taskMap.put(vo.getPk_plan(),plan);
	        }

			String planSysName = getSysNameByCode((plan.getAvabusisystem()));
			/**end*/
			buffer.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000235", null, new String[]{planSysName})/*{0}组织【*/).append(entityName).append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000236")/*】的【*/);
			for(int i=0;i<ss.length;i++){
				buffer.append(ss[i]+"/");
			}
			if(!vo.getSchemetype().equals(CtlSchemeConst.CTL_SCHEMA_GROUP)){
				buffer.append(vos.getStartdate())
				.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000237")/*－*/)
				.append(vos.getEnddate())
				.append("/");
			}
			buffer.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000050")/*】*/);
			String diminfo = buffer.toString();
			formulaExpress = FormulaParser.parseToNumSrc(formulaExpress);

			/**
			 * 特殊控制方案计划数和执行数的处理，控制符左边的为计划数，右边为执行数
			 * */
			UFDouble complexPlanValue = new UFDouble(0);
			UFDouble complexZxsValue = new UFDouble(0);
			String leftValue = null;
			if(vo.getSchemetype().equals(IRuleClassConst.SCHEMA_FLEX)){
				/**启动弹性控制方案需要实时计算弹性因子的数据*/
				leftValue = parseFlexAlgorithm(ctrlvos);
				formulaExpress = formulaExpress.replaceAll("FLEXEXPRESS\\(\\)", leftValue);
			}else {
				int index = getIndex(formulaExpress);
				int index1 = formulaExpress.indexOf("/");
				String left_formula = formulaExpress.substring(0, index1);
				complexPlanValue = getComplexZxs(left_formula + "/100");
				String right_formula = formulaExpress.substring(index);
				complexZxsValue = getComplexZxs(right_formula);
			}
			/**
			 * 公式解析formulaExpress是否为true 控制方案的启动，不符合条件的启动，不抛异常，提示，事务执行完成
			 * */

			Boolean[] needctl = needCtl(formulaExpress);
			/**
			 * songrui add 2008.10.30 控制型方案超预算提示，预警型方案超预算提示
			 * */
			if (!needctl[0].booleanValue()) {
				UFDouble planvalue = new UFDouble(0);
				UFDouble zxsvalue = new UFDouble(0);
				/**
				 * 模型上和计划上的特殊控制方案都没有预算百分比
				 * */
				if(leftValue!=null&&vo.getSchemetype().equals(IRuleClassConst.SCHEMA_FLEX)){
					planvalue = new UFDouble(leftValue);
					zxsvalue = sumvalue;
				} else {
					planvalue = complexPlanValue;
					zxsvalue = complexZxsValue;
				} /*else {
					*//**
					 * 单项方案和组方案直接可以取到计划数
					 * *//*
					planvalue = vo.getPlanvalue();
					zxsvalue = sumvalue;
				}*/
				boolean isNumber = OutEnum.OCCORAMOUNT.equals(paramvo[0].getData_attr());// 是否是发生数量

				String planname = plan.getObjname();
	       		IdCtrlInfoVO infovo = RuleServiceGetter.getIBusiRuleQuery().queryCtrlInfoVOByPk(vo.getPk_parent());
	       		String message = null;
	       		String[] arrayExpress = formulaExpress.split(vo.getCtrlsign());
	       		HashMap<String, String> infoMap = BudgetControlCTL.getCtrlInfoMap(arrayExpress, vo, schemevo);
	       		if(infovo!=null&&infovo.getInfoexpress()!=null){
	       			message = CtlSchemeCTL.getFinalCtrlInfoMessage(infoMap,infovo.getInfoexpress());
		    	} else {
		    		message = CtlSchemeCTL.getControlHintMessage(vo,planname, diminfo, isNumber, ctrlObjName,
						planvalue, zxsvalue, sbStr.toString(), true, powerInt);
		    	}
				info.add(message);
			}
		}
		return (String[]) info.toArray(new String[0]);
	}

	public static IdFlexElementVO getFlexelement(String formulaPk) throws BusinessException {
		StringBuffer str = new StringBuffer();
		str.append(" pk_formula = '").append(formulaPk).append("'");
		SuperVO[] vos = NtbSuperServiceGetter.getINtbSuper().queryByCondition(IdFlexElementVO.class, str.toString());
		if (vos != null)
			return ((IdFlexElementVO)vos[0]);
		else
			return null;

	}

	public static UFDouble getExpressValue(String express,DataCell datacell) throws Exception {
		String className = "nc.ms.tb.formula.core.RuleExecuteHelper";
		String methodName = "getExpressResult";
		Object[] objs = new Object[] { express, datacell };
		Class cls = Class.forName(className);
		Class[] argclass = new Class[objs.length];
		argclass[0] = String.class;
		argclass[1] = DataCell.class;
		Method method = cls.getDeclaredMethod(methodName, argclass);
		Object value = (UFDouble)method.invoke(cls.newInstance(), objs);
		return (UFDouble)value;

	}

	public static String parseFlexAlgorithm(ArrayList<CtrlSchemeVO> ctrlvos/*String formulaExpress,IdCtrlformulaVO vo,HashMap exeVarnoMap,IdCtrlschemeVO[] vos*/) throws Exception {
		String lastFormulaExpress = "";
		CtrlSchemeVO ctrlvo = ctrlvos.get(0);
		DataCell datacell = ctrlvo.getAllotCell();
		String pk_formula = ctrlvo.getPk_formula();
		/**通过VO获取弹性因子*/
		IdFlexElementVO m_vo = getFlexelement(pk_formula);
		//得到弹性规则的表达式
		String ruleExpress = m_vo.getFlexruleexpress();
		String baseExpress = m_vo.getFlexbaseexpress();
		String minValue = m_vo.getMinexpress();  //最低限额
		String maxValue = m_vo.getMaxexpress();  //最高限额
		ArrayList<DimFormulaMVO> formulaMList =  RuleServiceGetter.getIBusiRuleQuery().queryMVOByPkFormula(pk_formula);
		for(int n=0;n<formulaMList.size();n++){
			DimFormulaMVO vo = formulaMList.get(n);
			String varNo = vo.getVarno();
			String express = vo.getContent();
			if(ruleExpress.indexOf(varNo)>=0){
//				double value = RuleExecuteHelper.getExpressResult(express, datacell);
				UFDouble _value = getExpressValue(express, datacell);
				ruleExpress = ruleExpress.replaceAll(varNo, _value.toString());
			}
			if(baseExpress!=null&&baseExpress.indexOf(varNo)>=0){
//				double value = RuleExecuteHelper.getExpressResult(express, datacell);
				UFDouble _value = getExpressValue(express, datacell);
				baseExpress = baseExpress.replaceAll(varNo, _value.toString());
			}
			if(minValue!=null&&minValue.indexOf(varNo)>=0){
//				double value = RuleExecuteHelper.getExpressResult(express, datacell);
				UFDouble _value = getExpressValue(express, datacell);
				minValue = minValue.replaceAll(varNo, _value.toString());
			}
			if(maxValue!=null&&maxValue.indexOf(varNo)>=0){
//				double value = RuleExecuteHelper.getExpressResult(express, datacell);
				UFDouble _value = getExpressValue(express, datacell);
				maxValue = maxValue.replaceAll(varNo, _value.toString());
			}
		}
		/**算出来左边的弹性区间计划规则的值*/
		UFDouble leftValue = getComplexZxs(ruleExpress);
		if(minValue!=null&&!"".equals(minValue)){
		   Boolean result = compareLeftValueAndMinValue(leftValue,new UFDouble(minValue));
		   if(result){
			   return minValue;
		   }
		}
		if(maxValue!=null&&!"".equals(maxValue)){
			Boolean result = compareLeftValueAndMaxValue(leftValue,new UFDouble(maxValue));
			 if(result){
			   return maxValue;
		   }
		}
		/**算控制基数的值*/
		UFDouble baseValue = getComplexZxs(baseExpress);  //控制基数只有在弹性区间的类型是比率的时候才用的
		lastFormulaExpress  = analysisFlexZone(pk_formula,baseValue,leftValue,ruleExpress);
		return lastFormulaExpress;
	}

    private static Boolean compareLeftValueAndMinValue(UFDouble leftvalue,UFDouble value){
		Boolean[] resultValue = null;
		StringBuffer str = new StringBuffer();
		str.append(leftvalue).append("<=").append(value);
		try{
			nc.vo.bank_cvp.compile.datastruct.ArrayValue result = nc.ui.bank_cvp.formulainterface.RefCompilerClient.getExpressionResult(str.toString(), null);
			resultValue = result.getBoolean();

		}catch (Exception e) {
			NtbLogger.error(e);
		}
		return resultValue[0];
    }

    private static String analysisFlexZone(String pk_dimformula,UFDouble baseValue,UFDouble leftValue,String leftExpress) throws Exception {
    	IdFlexZoneVO[] vos = getFlexZone(pk_dimformula); //升序查询
    	for(int n=0 ;n<(vos==null?0:vos.length) ; n++) {
    		IdFlexZoneVO vo = vos[n];
    		UFDouble downValue = vo.getZoneDown();  //如果都是空,表示无穷
    		if(downValue==null){
    			downValue = new UFDouble(-9999999999999999.99);
    		}
    		UFDouble upValue = vo.getZoneUp();
    		if(upValue==null){
    			upValue = new UFDouble(9999999999999999.99);
    		}
    		int type =  vo.getZoneType();
    		if(type == Integer.parseInt(IdFlexAreaTypeEnum.FinalValueType.toCodeString())){
    		/**固定值,如果是固定值的话,直接做比较*/
    			StringBuffer expressLeft = new StringBuffer();
    			StringBuffer expressRigth = new StringBuffer();
    			if(downValue !=null){
    				expressLeft.append(downValue).append("<=");
    				expressLeft.append(leftValue);
    			}
    			if(upValue != null){
    				expressRigth.append(leftValue);
    				expressRigth.append("<").append(upValue);
    			}
    			boolean needctl = needCtl(expressLeft.toString(),expressRigth.toString());
    			if(needctl){
    				return vo.getPlannum()==null?"0.00":vo.getPlannum().toString();
    			}else{
    				continue;
    			}
    		}else{
    		/**比率需要根据基准数和比率来计算计划值*/
    			StringBuffer expressLeft = new StringBuffer();
    			StringBuffer expressRigth = new StringBuffer();
    			if(downValue !=null){
    				expressLeft.append(downValue).append("<=");
    				expressLeft.append(leftValue);
    			}
    			if(upValue != null){
    				expressRigth.append(leftValue);
    				expressRigth.append("<").append(upValue);
    			}
    			boolean needctl = needCtl(expressLeft.toString(),expressRigth.toString());
    			if(needctl){
    				return getFormattedValue(recursiveFlexZone(vos,n,baseValue,leftExpress),2).toString();
    			}else{
    				continue;
    			}
    		}
    	}
    	return "";
    }

	private static boolean needCtl(String expLeft,String expRight){
		boolean valueLeft = true;
		boolean valueRight = true;
		try{
			if(!"".equals(expLeft)){
			  nc.vo.bank_cvp.compile.datastruct.ArrayValue resultLeft = nc.ui.bank_cvp.formulainterface.RefCompilerClient.getExpressionResult(expLeft, null);
			  valueLeft = resultLeft.getBoolean()[0].booleanValue();
			}
			if(!"".equals(expRight)){
			  nc.vo.bank_cvp.compile.datastruct.ArrayValue resultRight = nc.ui.bank_cvp.formulainterface.RefCompilerClient.getExpressionResult(expRight, null);
			  valueRight = resultRight.getBoolean()[0].booleanValue();
			}

		}catch (Exception e) {
			NtbLogger.error(e);
		}
		if(valueLeft&&valueRight){
			return true;
		}else{
			return false;
		}

	}

    private static UFDouble recursiveFlexZone(IdFlexZoneVO[] vos,int location,UFDouble baseValue,String leftExpress) throws Exception{
    	StringBuffer express = new StringBuffer();
    	for(int m=0 ; m<vos.length ; m++){
    		IdFlexZoneVO vo = vos[m];
    		Integer type = vo.getZoneType();
    		if(Integer.parseInt(IdFlexAreaTypeEnum.PrecentType.toCodeString())==type){
    			//说明是比率,需要进行转换
    			if(leftExpress.indexOf("/")>0){
    				/*这个方法简单的解析了左边表达式的分母的数值,是有问题的,需要想办法通过正常的手段获取分母*/
    				String result = leftExpress.substring(leftExpress.indexOf("/")+1, leftExpress.lastIndexOf(")"));
    				vo.setZoneDown(vo.getZoneDown()==null?null:vo.getZoneDown().multiply(new UFDouble(result)));
    				vo.setZoneUp(vo.getZoneUp()==null?null:vo.getZoneUp().multiply(new UFDouble(result)));
    			}
    		}
    	}

    	for(int n=location ; n>=0 ; n--){
    		if(n==location){
       	   UFDouble value = baseValue.sub(vos[n].getZoneDown()==null?new UFDouble(0):vos[n].getZoneDown());
       		//UFDouble planNum = value.multiply(vos[n].getPlannum());
       		  UFDouble planNum = value.multiply(vos[n].getPlannum()==null?new UFDouble(0):vos[n].getPlannum());
       		  express.append(planNum);
       		 //express.append("+");
       		  if(location!=0)
       		   express.append("+");
    		}
    		else if(n==0){ //取基数
    		  UFDouble value = new UFDouble(vos[n].getPlannum());
    		  express.append(value);
    		}else{
    		  UFDouble value = vos[n].getZoneUp().sub(vos[n].getZoneDown());
    		  UFDouble planNum = value.multiply(vos[n].getPlannum());
    		  express.append(planNum);
    		  express.append("+");
    		}
    	}
    	UFDouble lastValue = calcPlanValue(express.toString());
    	return lastValue;
    }

	public static UFDouble calcPlanValue(String src) throws Exception{
		nc.vo.bank_cvp.compile.datastruct.ArrayValue result = null;
		UFDouble value = null;
		NtbContext m_context = new NtbContext();
		result = nc.ui.bank_cvp.formulainterface.RefCompilerClient.getExpressionResult(src, m_context);
		Object tmpResult = result.getValue();
		if (tmpResult != null){
			value = new UFDouble((BigDecimal)tmpResult);
		    return value;
		}
		else {
			return new UFDouble(0);
		}
	}




	public static IdFlexZoneVO[] getFlexZone(String pk_dimformula) throws BusinessException {
		StringBuffer str = new StringBuffer();
		str.append(" pk_formula = '").append(pk_dimformula).append("'");
		str.append(" order by idx asc");  //升序查询
		SuperVO[] vos = NtbSuperServiceGetter.getINtbSuper().queryByCondition(IdFlexZoneVO.class, str.toString());
		if (vos != null)
			return ((IdFlexZoneVO[])vos);
		else
			return null;
	}

    private static Boolean compareLeftValueAndMaxValue(UFDouble leftvalue,UFDouble value){
		Boolean[] resultValue = null;
		StringBuffer str = new StringBuffer();
		str.append(leftvalue).append(">=").append(value);
		try{
			nc.vo.bank_cvp.compile.datastruct.ArrayValue result = nc.ui.bank_cvp.formulainterface.RefCompilerClient.getExpressionResult(str.toString(), null);
			resultValue = result.getBoolean();

		}catch (Exception e) {
			NtbLogger.error(e);
		}
		return resultValue[0];
    }


	private static UFDouble getComplexZxs(String complexformula) throws Exception {
		try {
			nc.vo.bank_cvp.compile.datastruct.ArrayValue result = nc.ui.bank_cvp.formulainterface.RefCompilerClient
					.getExpressionResult(complexformula, null);
			Object tmpResult = result.getValue();
			BigDecimal bvalue = (BigDecimal) tmpResult;
			Double temp = bvalue.doubleValue();
			return new UFDouble(temp);
		} catch (Exception e) {
			NtbLogger.error(e);
		}
		return new UFDouble(0);
	}

	public static Boolean[] needCtl(String exp) {
		Boolean[] value = null;
		try {
			nc.vo.bank_cvp.compile.datastruct.ArrayValue result = nc.ui.bank_cvp.formulainterface.RefCompilerClient
					.getExpressionResult(exp, null);
			value = result.getBoolean();

		} catch (Exception e) {
			NtbLogger.error(e);
		}
		return value;
	}

	public static String[] startCtrlScheme(ArrayList<CtrlSchemeVO> vos) throws BusinessException{
		String[] messages = null;
		messages = CtlSchemeServiceGetter.getICtlScheme().startCtrlScheme(vos);
		return messages;
	}


	public static HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> filterStopCtrlScheme(DataCell[] selectedcells){
		StringBuffer sWhere_plan = new StringBuffer();

		ArrayList<ArrayList> list = new ArrayList<ArrayList> ();
		HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> ctlmap_cube = null;
	    IStringConvertor cvt = StringConvertorFactory.getConvertor(DimVector.class);

		for(int n=0;n<selectedcells.length;n++){
			ArrayList<String> tmpList = new ArrayList<String> ();
		    String pk_cell = cvt.convertToString(selectedcells[n].getDimVector());
			tmpList.add(pk_cell==null?" ":pk_cell); //yuyonga
			tmpList.add(pk_cell);
			list.add(tmpList);
		}
		try{
			ctlmap_cube = CtlSchemeCTL.createNtbTempTable(selectedcells[0].getCubeDef(), list);
//			StringBuffer sWhere_cube = new StringBuffer();
//			sWhere_cube.append("pk_dimvector in (");
//			sWhere_cube.append("select DATACELLCODE from ").append(tmpTableName);
//			sWhere_cube.append(") and isstarted = 'Y' and pk_cube = '");
//			sWhere_cube.append(selectedcells[0].getCubeDef().getPrimaryKey());
//			sWhere_cube.append("' and pk_parent is not null");
//			ctlmap_cube= queryCtrlScheme(sWhere_cube.toString());
	//		CtlSchemeCTL.deleteTmpTable(tmpTableName);
	//		if(ctlmap_cube!=null&&!ctlmap_cube.isEmpty()){
	//			//模型上控制方案停用即删除
	//			if(ctlmap_cube!=null&&!ctlmap_cube.isEmpty()){
	//				deleteCtrlScheme(ctlmap_cube);
	//			}
	//			ui.showHintMessage(NCLangRes4VoTransl.getNCLangRes().getStrByID("pf_tb", "PlanButton_OnStopScheme-000003")/*停用方案完成*/);
	//		}
	//		else{
	//			MessageDialog.showWarningDlg(ui, NtbUILangRes.getInstance().getStrByID("UPPntbui-000033")/*@res "警告"*/, NCLangRes4VoTransl.getNCLangRes().getStrByID("pf_tb", "PlanButton_OnStopScheme-000004")/*单元格上没有启动的控制方案，不能停用控制方案！*/);
	//			ui.showHintMessage(NCLangRes.getInstance().getStrByID("3002v50","UPP3002v50-000101")/*@res "已取消"*/);
	//		}
		}catch(BusinessException ex){
			   NtbLogger.print(ex);
		}
		return ctlmap_cube;
	}
	
	public static Map<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> queryCtrlSchemeBySchemes(String sWhere) throws BusinessException {
		
		Map<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> map = CtlSchemeServiceGetter.getICtlScheme().queryCtrlSchemeBySchemes(sWhere);
		return map;
	}

	public static HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> queryCtrlSchemeByCtrlformula(String sWhere) throws BusinessException{
		try{

			HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> map = CtlSchemeServiceGetter.getICtlScheme().queryCtrlScheme(sWhere);
			return map;
		}catch (BusinessException e) {
			throw e;
		}catch (Exception e) {
			NtbLogger.error(e);
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052")/*未知异常*/);
		}
	}

	public static HashMap<IdCtrlformulaVO, List<Object>> queryCtrlSchemeAndRecord(String sWhere) throws BusinessException {
		ICtrlSchemeQuery iQuery = (ICtrlSchemeQuery)NCLocator.getInstance().lookup(ICtrlSchemeQuery.class);
		HashMap<IdCtrlformulaVO, List<Object>> map = iQuery.queryCtrlSchemeAndRecord(sWhere);
		return map;
	}

	public static void deleteTmpTable(String name) throws BusinessException{
		try{

			CtlSchemeServiceGetter.getICtlScheme().deleteTempTable(name);

		}catch (BusinessException e) {
			throw e;
		}catch (Exception e) {
			NtbLogger.error(e);
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052")/*未知异常*/);
		}
	}

	public static void deleteCtrlScheme(Map<String, List<String>> map) throws BusinessException{
		try{
			CtlSchemeServiceGetter.getICtlScheme().deleteCtrlScheme(map);
		}
		catch(BusinessException ex){
			throw ex;
		}catch (Exception e) {
			NtbLogger.error(e);
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052")/*未知异常*/,e);
		}
	}

	public static HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> createNtbTempTable(CubeDef cube,ArrayList<ArrayList> list) throws BusinessException {
		HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> ctlmap_cube = null;
		try{
			String tmpTableName = "NTB_TMP_CUBE_";
			if(cube != null) tmpTableName += cube.getObjcode();
			if(tmpTableName.length() > 30)
				tmpTableName = tmpTableName.substring(0, 29);
			ctlmap_cube = CtlSchemeServiceGetter.getICtlScheme().createNtbTempTable(cube,tmpTableName,list);
		}catch (BusinessException e) {
			throw e;
		}catch (Exception e) {
			NtbLogger.error(e);
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052")/*未知异常*/);
		}
		return ctlmap_cube;
	}
	public static String createNtbTempTable_new(CubeDef cube,String tempTableName,ArrayList<ArrayList> list) throws BusinessException {
		try{
			return CtlSchemeServiceGetter.getICtlScheme().createNtbTempTable_new(cube,tempTableName,list);
		}catch (BusinessException e) {
			throw e;
		}catch (Exception e) {
			NtbLogger.error(e);
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052")/*未知异常*/);
		}
	}
	public static  boolean orgCheck(SuperVO vo, int bdMode, String currPkGroup, String currOrgPk,int nodeType) {
		String pk_org = (String)vo.getAttributeValue("pk_org");
		String pk_group = (String)vo.getAttributeValue("pk_group");
		if (nodeType == INodeTypeConst.GLOBE) {
			// 全局
			switch (bdMode) {
			case IBDMode.SCOPE_GLOBE_GROUP:
			case IBDMode.SCOPE_GLOBE_GROUP_ORG:
				return IDimMemberPkConst.PK_ORG_GLOBE.equals(pk_org);
			case IBDMode.SCOPE_GLOBE_ORG:
				return IDimMemberPkConst.PK_ORG_GLOBE.equals(pk_org) ||
					(pk_org != null && pk_org.equals(pk_group));
			default :
				return true;
			}
		}
		else if (nodeType == INodeTypeConst.GROUP) {
			// 集团
			switch (bdMode) {
			case IBDMode.SCOPE_GLOBE_GROUP:
				return IDimMemberPkConst.PK_ORG_GLOBE.equals(pk_org) ||
					(currPkGroup != null && currPkGroup.equals(pk_org)) ||
					(currPkGroup != null && currPkGroup.equals(pk_group));
			case IBDMode.SCOPE_GLOBE_GROUP_ORG:
				return IDimMemberPkConst.PK_ORG_GLOBE.equals(pk_org) ||
					(currPkGroup != null && currPkGroup.equals(pk_org));
			case IBDMode.SCOPE_GLOBE_ORG:
			case IBDMode.SCOPE_GLOBE:
				return false;
			default :
				return true;
			}
		}
		else {
			// 组织
			switch (bdMode) {
			case IBDMode.SCOPE_GLOBE_GROUP:
			case IBDMode.SCOPE_GLOBE:
				return false;
			case IBDMode.SCOPE_GLOBE_GROUP_ORG:
				return IDimMemberPkConst.PK_ORG_GLOBE.equals(pk_org) ||
					(currPkGroup != null && currPkGroup.equals(pk_org)) ||
					(currOrgPk != null && currOrgPk.equals(pk_org));
			case IBDMode.SCOPE_GLOBE_ORG:
				return IDimMemberPkConst.PK_ORG_GLOBE.equals(pk_org) ||
					(currOrgPk != null && currOrgPk.equals(pk_org)) ||
					(pk_org != null && pk_org.equals(pk_group));
			default :
				return true;
			}
		}
	}

	/**
	 * 返回DataCell对应的基本档案信息
	 */
	public static String[] replaceDataCellAttributeItem(DataCell env,IdBdcontrastVO[] bdcontrast,String sysid) throws BusinessException {

		StringBuffer fromitem = new StringBuffer();
		StringBuffer nameidx = new StringBuffer();
		StringBuffer codeidx = new StringBuffer();
		StringBuffer ctrllevel = new StringBuffer();
		StringBuffer startTime = new StringBuffer();
		StringBuffer endTime = new StringBuffer();
		StringBuffer typeitem = new StringBuffer();
		StringBuffer pkidx = new StringBuffer();
		String[] src = new String[9];
		/**yuyonga 单元格所有维度成员信息*/
		DimMember[] dimmember = env.getDimVector().getDimMembers().toArray(new DimMember[0]);
		try {
			for (int i = 0; i < dimmember.length; i++) {
				/** 过滤一些不需要对照的维度成员 */
				if (CtlSchemeEnum.LISTTYPE.contains(dimmember[i].getDimDef().getPrimaryKey())) {
					  continue;
				}
				else if (bdcontrast != null) {
					/** 对其他维度做对照匹配的时候,需要先匹配存在业务系统对照的 */
					String classId = CtrlRuleCTL.getClassIDByDimLevel(dimmember[i].getLevelValue());  //元数据ID
					String pk_dimlevel = dimmember[i].getDimLevel().getPrimaryKey();   //PK层
					ArrayList<IdBdcontrastVO> resultVo = new ArrayList<IdBdcontrastVO> ();
					/**先通过pk_dimlevel从bdcontrast查询出对应的bdcontrast,如果没有,在根据classId去查询*/
					for (int j=0;j<bdcontrast.length;j++){
						DimRelUapVO vo = BdContrastCache.getNewInstance().getRelUapVOByPK(bdcontrast[j].getPrimaryKey());
						if(vo!=null&&vo.getPk_Dimlevel()!=null&&pk_dimlevel.equals(vo.getPk_Dimlevel())){
							resultVo.add(bdcontrast[j]);
							break;
						}
					}

					/**没有对应上,就宽范的比较*/
					if(resultVo.size()==0){
						for (int j = 0; j < bdcontrast.length; j++) {
							if(classId!= null&& classId.equals(bdcontrast[j].getPk_bdinfo())
									||(OutEnum.HRPSYS.equals(sysid)&&("985be8a4-3a36-4778-8afe-2d8ed3902659".equals(classId)||IDimLevelPKConst.ENT.equals(classId))&&"a0ec952c-e4e5-416a-b3e0-d402725f76be".equals(bdcontrast[j].getPk_bdinfo()))){
								resultVo.add(bdcontrast[j]);
							}
						}
					}
				    for (int n=0;n<resultVo.size();n++){
							fromitem.append(resultVo.get(n).getAtt_fld() + ":");
							nameidx.append(dimmember[i].getLevelValue().getName() + ":");
							if (dimmember[i].getLevelValue().getCode()!= null) {
								codeidx.append(dimmember[i].getLevelValue().getCode() + ":");
							} else {
								codeidx.append("null" + ":");
							}
							if (dimmember[i].getLevelValue().getKey() != null) {
								pkidx.append(dimmember[i].getLevelValue().getKey() + ":");
							} else {
								pkidx.append("null" + ":");
							}
							ctrllevel.append(resultVo.get(n).getLevelctlflag()+ ":");
							typeitem.append(resultVo.get(n).getBdinfo_type()+ ":");
					}
				}

			}
			String[] time = TimeDimTool.getStartEndDataByDataCell(env,false);
			startTime.append(time[0]);
			endTime.append(time[1]);

			src[0] = fromitem.toString();   //基本档案类型att_fld
			src[1] = nameidx.toString();    //基本档案的名称
			src[2] = codeidx.toString();    //基本档案的编码
			src[3] = ctrllevel.toString();  //基本档案是否可控下级
			src[4] = startTime.toString();  //开始时间
			src[5] = endTime.toString();    //结束时间
			src[6] = typeitem.toString();   //基本档案类型名称
			src[7] = pkidx.toString();      //基本档案的PK
			src[8] = time[2];

		} catch (BusinessException ex) {
			NtbLogger.error(ex);
			throw ex;
		}
		return src;
	}

	/**给HRP系统的*/
	public static NtbParamVO[] convertDataCell2NtbParamVO(List<DataCell> selectcells,String pk_task,String sysid) throws BusinessException{
		ArrayList<NtbParamVO> ntbParam = new ArrayList<NtbParamVO> ();
		MdTask plan = TbTaskCtl.getMdTaskByPk(pk_task, true);//getPlanByPK(pk_plan);
		for(int n=0;n<selectcells.size();n++){
			DataCell datacell = selectcells.get(n);
			if(datacell==null/*||datacell.getCellValue().getValue()==null*/)
			   continue;
			NtbParamVO paramvo = new NtbParamVO();
			DimVector dv = datacell.getDimVector();
			DimMember[] dimmember = dv.getDimMembers().toArray(new DimMember[0]);
			ArrayList<IdBdcontrastVO> voAllList = new ArrayList<IdBdcontrastVO> ();
			IdBdcontrastVO[] vos = BdContrastCache.getNewInstance().getVoBySysid(sysid);
			voAllList.addAll(Arrays.asList(vos));
			String[] strArrays = replaceDataCellAttributeItem(datacell,voAllList.toArray(new IdBdcontrastVO[0]),sysid);
			for(int m=0;m<dimmember.length;m++){
				DimMember dim = dimmember[m];
				/**获取主组织,也就是预算组织,至于是不是采购组织,业务系统自己去判断*/
				if(IDimDefPKConst.ENT.equals(dim.getDimDef().getPrimaryKey())){
					paramvo.setPk_Org((String)dim.getLevelValue().getKey());
				}
				/**获取币种*/
				if(IDimDefPKConst.CURR.equals(dim.getDimDef().getPrimaryKey())){
					paramvo.setPk_currency((String)dim.getLevelValue().getKey());
					paramvo.setCurr_type(CtlSchemeCTL.getCurrencyType((String)dim.getLevelValue().getKey()));
				}
			}
            paramvo.setBusiAttrs(strArrays[0].split(":"));
            paramvo.setTypeDim(strArrays[1].split(":"));
            paramvo.setCode_dims(strArrays[2].split(":"));
            paramvo.setPkDim(strArrays[7].split(":"));
            paramvo.setBegDate(strArrays[4]);
            paramvo.setEndDate(strArrays[5]);
			paramvo.setPk_plan(pk_task);
			paramvo.setHrAccountTime(strArrays[8]);
			/**获取预算科目的指标*/
			IDimManager dm=DimServiceGetter.getDimManager();
			DimMember dimm = datacell.getDimVector().getDimMember(dm.getDimDefByPK(IDimDefPKConst.MEASURE));
			if(dimm!=null&&dimm.getLevelValue().getKey().equals(OutEnum.HRPAMOUNT)){
				paramvo.setData_attr(OutEnum.HRPAMOUNT);
			}else if(dimm!=null&&dimm.getLevelValue().getKey().equals(OutEnum.HRPNUMBER)){
				paramvo.setData_attr(OutEnum.HRPNUMBER);
			}

			paramvo.setPlanname(plan.getObjname());  //计划名称
			paramvo.setPk_Group(plan.getPk_group());  //集团
			String _pk_currency = plan.getPk_currency()==null?"":plan.getPk_currency();
			String pk_entity = plan.getPk_planent()==null?"":plan.getPk_planent();
			if(IDimPkConst.PK_GLOBE_CURRENCY.equals(_pk_currency)||IDimPkConst.PK_GROUP_CURRENCY.equals(_pk_currency)||IDimPkConst.PK_ORG_CURRENCY.equals(_pk_currency)){

			}else{
				_pk_currency = plan.getPk_currency()==null?"":plan.getPk_currency();
			}
			String pk_currency = CtlSchemeCTL.getPk_currency(_pk_currency,pk_entity,sysid);
			paramvo.setPk_currency(pk_currency);  //集团币种
			if(datacell.getCellValue().getValue()==null){
				paramvo.setPlanData(null);
			}else{
				double value = ((Number)datacell.getCellValue().getValue()).doubleValue();
				paramvo.setPlanData(new UFDouble(value));
			}
			paramvo.setCreatePlanTime(plan.getCreationtime());


			ntbParam.add(paramvo);
		}
		if(ntbParam.size()==0){
			return null;
		}else{
		    return ntbParam.toArray(new NtbParamVO[0]);
		}
	}

	/**
	 * 根据单元格获得NtbParamVO
	 * @param selectcells 选取的单元格
	 * @param pk_task 任务pk
	 * @param pk_sheet sheet pk
	 * @param sysid 业务系统ID
	 * @param button_code 出发此操作的按钮pk
	 * @return NtbParamVO[]
	 * @throws Exception
	 */
	public static NtbParamVO[] convertDataCell2NtbParamVO(List<DataCell> selectcells, MdTask plan, String pk_sheet, String button_code, String sysId) throws Exception {
		List<NtbParamVO> ntbParam = new ArrayList<NtbParamVO>();
		for(DataCell datacell : selectcells) {
			NtbParamVO paramvo = convertDataCell2NtbParamVO(datacell, plan, pk_sheet, button_code, sysId, new ArrayList<String>());
			ntbParam.add(paramvo);
		}
		if(ntbParam.size() == 0)
			return null;
		else
			return ntbParam.toArray(new NtbParamVO[0]);
	}

//	public static NtbParamVO[] convertDataCell2NtbParamVO(List<DataCell> selectcells, String pk_task, String pk_sheet, String button_code, String sysid, ArrayList<String> busiSysList) throws Exception {
//		List<NtbParamVO> ntbParam = new ArrayList<NtbParamVO>();
//		for(DataCell datacell : selectcells) {
//			NtbParamVO paramvo = convertDataCell2NtbParamVO(datacell, pk_task, pk_sheet, button_code, sysid, busiSysList);
//			ntbParam.add(paramvo);
//		}
//		if(ntbParam.size() == 0)
//			return null;
//		else
//			return ntbParam.toArray(new NtbParamVO[0]);
//	}

	public static NtbParamVO[] convertDataCell2NtbParamVO(List<Cell> selectcells,String pk_task,String pk_sheet,String button_code,String sysid,ArrayList<String> busiSysList) throws Exception{
		ArrayList<NtbParamVO> ntbParam = new ArrayList<NtbParamVO> ();
		MdTask plan = TbTaskCtl.getMdTaskByPk(pk_task, true);
		for(int n=0;n<selectcells.size();n++){
			Cell cell = selectcells.get(n);
			DataCell datacell = cell==null ? null : (DataCell)cell.getExtFmt(TbIufoConst.dataccellKey);
			if(datacell==null/*||datacell.getCellValue().getValue()==null*/)
			   continue;


			NtbParamVO paramvo = convertDataCell2NtbParamVO(datacell, plan, pk_sheet, button_code, sysid, busiSysList);
			ntbParam.add(paramvo);
		}
		if(ntbParam.size()==0){
			return null;
		}else{
		    return ntbParam.toArray(new NtbParamVO[0]);
		}
	}

	public static NtbParamVO[] convertDataCell2NtbParamVO(List<Cell> selectcells,MdTask plan,String pk_sheet,String button_code,String sysid,ArrayList<String> busiSysList) throws Exception{
		ArrayList<NtbParamVO> ntbParam = new ArrayList<NtbParamVO> ();
		for(int n=0;n<selectcells.size();n++){
			Cell cell = selectcells.get(n);
			DataCell datacell = cell==null ? null : (DataCell)cell.getExtFmt(TbIufoConst.dataccellKey);
			if(datacell==null/*||datacell.getCellValue().getValue()==null*/)
			   continue;


			NtbParamVO paramvo = convertDataCell2NtbParamVO(datacell, plan, pk_sheet, button_code, sysid, busiSysList);
			ntbParam.add(paramvo);
		}
		if(ntbParam.size()==0){
			return null;
		}else{
		    return ntbParam.toArray(new NtbParamVO[0]);
		}
	}

	private static NtbParamVO convertDataCell2NtbParamVO(DataCell datacell,MdTask plan,String pk_sheet,String button_code,String sysid,ArrayList<String> busiSysList) throws Exception {
		NtbParamVO paramvo = new NtbParamVO();
		DimVector dv = datacell.getDimVector();
		DimMember[] dimmember = dv.getDimMembers().toArray(new DimMember[0]);
		ArrayList<IdBdcontrastVO> voAllList = new ArrayList<IdBdcontrastVO> ();
		IdBdcontrastVO[] vos = BdContrastCache.getNewInstance().getVoBySysid(sysid);
		if(vos != null && vos.length > 0)
			voAllList.addAll(Arrays.asList(vos));
		for(int m=0;m<busiSysList.size();m++){
			IdBdcontrastVO[] vTmps = BdContrastCache.getNewInstance().getVoBySysid(busiSysList.get(m));
			if(vos != null && vos.length > 0)
				voAllList.addAll(Arrays.asList(vTmps));
		}
		IdBdcontrastVO[] voAllArr = voAllList == null ? null : voAllList.toArray(new IdBdcontrastVO[0]);
		String[] strArrays = replaceDataCellAttributeItem(datacell,voAllArr,sysid);

		DimHierarchy dimhier = datacell.getCubeDef().getDimHierarchy(IDimDefPKConst.TIME);
		List<DimLevel> dimLevs = dimhier.getDimLevels();

		DimLevel bottomLevel = null;
		if(dimLevs != null && dimLevs.size() > 0) {
			for(int i = dimLevs.size() - 1 ; i >= 0 ; i--) {
				LevelValue value = datacell.getDimVector().getLevelValue(dimLevs.get(i));
				if(value != null) {
					bottomLevel = dimLevs.get(i);
					break;
				}
			}
		}

		if(bottomLevel != null)
			paramvo.setTbbDateType(bottomLevel.getObjCode());

		for(int m=0;m<dimmember.length;m++){
			DimMember dim = dimmember[m];
			/**获取主组织,也就是预算组织,至于是不是采购组织,业务系统自己去判断*/
			if(IDimDefPKConst.ENT.equals(dim.getDimDef().getPrimaryKey())){
				paramvo.setPk_Org((String)dim.getLevelValue().getKey());
			}
			/**获取币种*/
			if(IDimDefPKConst.CURR.equals(dim.getDimDef().getPrimaryKey())){
				paramvo.setPk_currency((String)dim.getLevelValue().getKey());
				paramvo.setCurr_type(CtlSchemeCTL.getCurrencyType((String)dim.getLevelValue().getKey()));
			}
		}
		if(strArrays[0] != null && !"".equals(strArrays[0])) {
			paramvo.setBusiAttrs(strArrays[0].split(":"));
			paramvo.setTypeDim(strArrays[1].split(":"));
			paramvo.setCode_dims(strArrays[2].split(":"));
			paramvo.setPkDim(strArrays[7].split(":"));
		}
        paramvo.setBegDate(strArrays[4]);
        paramvo.setEndDate(strArrays[5]);
		paramvo.setPk_plan(plan.getPrimaryKey());
		/**获取预算科目的指标*/
		IDimManager dm=DimServiceGetter.getDimManager();
		DimMember dimm = datacell.getDimVector().getDimMember(dm.getDimDefByPK(IDimDefPKConst.MEASURE));
		if(dimm!=null&&dimm.getLevelValue().getKey().equals(IDimPkConst.PK_CG_AMOUNT)){
			paramvo.setData_attr(IDimPkConst.BZ_CG_AMOUNT);
		}else if(dimm!=null&&dimm.getLevelValue().getKey().equals(IDimPkConst.PK_CG_NUMBER)){
			paramvo.setData_attr(IDimPkConst.BZ_CG_NUMBER);
		}
		paramvo.setPk_measure(dimm.getUniqKey());
//		MdTask plan = TbTaskCtl.getMdTaskByPk(pk_task, true);//getPlanByPK(pk_plan);
		paramvo.setPlanname(plan.getObjname());  //计划名称
		paramvo.setPk_Group(plan.getPk_group());  //集团
		String _pk_currency = plan.getPk_currency()==null?"":plan.getPk_currency();
		String pk_entity = plan.getPk_planent()==null?"":plan.getPk_planent();
		if(IDimPkConst.PK_GLOBE_CURRENCY.equals(_pk_currency)||IDimPkConst.PK_GROUP_CURRENCY.equals(_pk_currency)||IDimPkConst.PK_ORG_CURRENCY.equals(_pk_currency)){

		}else{
			_pk_currency = plan.getPk_currency()==null?"":plan.getPk_currency();
		}
		String pk_currency = CtlSchemeCTL.getPk_currency(_pk_currency,pk_entity,sysid);
		paramvo.setPk_currency(pk_currency);  //集团币种
		paramvo.setButton_code(button_code);
		IStringConvertor cvt = StringConvertorFactory.getConvertor(DimVector.class);
		String pk_datacell = cvt.convertToString(datacell.getDimVector());
		paramvo.setPk_datacell(pk_sheet);  //这里面是sheet的PK
		double value = datacell.getCellValue().getValue() == null ? 0 : ((Number)datacell.getCellValue().getValue()).doubleValue();
		paramvo.setPlanData(new UFDouble(value));
		paramvo.setCreatePlanTime(plan.getCreationtime());
		//审批时间等待刘姐提供接口
//		if(PlanCTL.getProcDate(pk_plan, "REVEFFECT")!=null){
//		   UFDateTime time = new UFDateTime(PlanCTL.getProcDate(pk_plan, "REVEFFECT").toDate());
//		   paramvo.setSpPlanTime(time);
//		}
	    if(!OutEnum.MPPSYS.equals(sysid) && !OutEnum.FPSYS.equals(sysid) && !OutEnum.SOPSYS.equals(sysid)){
			/**付款排程计算[可下拨金额=预算数-执行数-预占执行数]*/
				StringBuffer sWhere_cube = new StringBuffer();
				sWhere_cube.append("pk_dimvector in (");
				sWhere_cube.append("'").append(pk_datacell).append("'");
				sWhere_cube.append(") and isstarted = 'Y' and pk_plan = '");
				sWhere_cube.append(plan.getPrimaryKey());
				sWhere_cube.append("' and pk_parent is not null");
				UFDouble values = new UFDouble();
				HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> tempNotStartCtrlscheme= CtlSchemeCTL.queryCtrlSchemeByCtrlformula(sWhere_cube.toString());
				if(tempNotStartCtrlscheme.size()>0){
				Iterator iter = tempNotStartCtrlscheme.entrySet().iterator();
				while(iter.hasNext()){
					Map.Entry entry = (Map.Entry)iter.next();
					ArrayList<IdCtrlschemeVO> schemevos = (ArrayList<IdCtrlschemeVO>)entry.getValue();
					IdCtrlformulaVO vo = (IdCtrlformulaVO)entry.getKey();
					String expressformula = vo.getExpressformula();
					for(int a=0;a<schemevos.size();a++){
						UFDouble tmpValue = new UFDouble();
						expressformula = expressformula.replaceAll(schemevos.get(a).getVarno(), tmpValue.add(schemevos.get(a).getReadydata()).add(schemevos.get(a).getRundata()).toString());
					}
					expressformula = expressformula.replaceAll(">=", "-");
					UFDouble _value = BudgetControlCTL.getComplexZxs(expressformula);
					values = values.add(_value);
				}
				paramvo.setPlanData(values);
			}
	    }
	    return paramvo;
	}

	public static void createBillType(NtbParamVO[] ntbParamvos,String syscode) throws BusinessException{
		try{
			CtlSchemeServiceGetter.getICtlScheme().createBillType(ntbParamvos,syscode);
		}catch (BusinessException e) {
			throw e;
		}catch (Exception e) {
			NtbLogger.error(e);
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01420ctl_000164")/*异常*/,e);
		}
	}

	/**
	 * 批复生效的时候自动启动控制方案
	 * @param plans
	 */
	public static void startControlSchemeWhenRevEffected(MdTask[] tasks) throws BusinessException {
		//将计划按照流程类型分类为编制流程和调整流程
		//编制流程可能需要启动控制方案，调整流程需要更新控制方案
		ArrayList<MdTask> comPlanList = new ArrayList<MdTask>();
		ArrayList<MdTask> adjPlanList = new ArrayList<MdTask>();
		for(MdTask task : tasks){
			String status = task.getVersionstatus();
			if(!"TBPTZ410000000001CME".equals(task.getPk_mvtype())){
				continue;
			}
			//编制流程
			if(ITaskConst.taskVersionStatus_compile.equals(status)
					||ITaskConst.taskVersionStatus_compileeffect.equals(status)
					||ITaskConst.taskVersionStatus_excute.equals(status)){
				comPlanList.add(task);
			}else if(ITaskConst.taskVersionStatus_adjusteffect.equals(status)
					||ITaskConst.taskVersionStatus_adjust.equals(status)
					||ITaskConst.taskVersionStatus_billadjust.equals(status)
					||ITaskConst.taskVersionStatus_billadjusteffect.equals(status)){
				adjPlanList.add(task);
			}
		}
		try {
			if(!comPlanList.isEmpty()){//只有启动控制方案的时候才需要检查参数
				UFBoolean bb = UtilServiceGetter.getIUtil().getParaBoolan(IDimPkConst.PK_GLOBE, "TBB017");
	            if(bb!=null && bb.booleanValue()){
	            	MdTask[] comPlanArr = comPlanList.toArray(new MdTask[0]);
                    onUseAllCrelSchemeInClient(comPlanArr);

	            }
			}

			if(!adjPlanList.isEmpty()){//只有启动控制方案的时候才需要检查参数
				UFBoolean bb = UtilServiceGetter.getIUtil().getParaBoolan(IDimPkConst.PK_GLOBE, "TBB017");
	            if(bb!=null && bb.booleanValue()){
	            	MdTask[] comPlanArr = adjPlanList.toArray(new MdTask[0]);
                   onUseAllCrelSchemeInClient(comPlanArr);
	            }
			}

			//调整流程只需要更新控制方案
			if(!adjPlanList.isEmpty()){
	             updateExistCtrlSchemeFind(adjPlanList);
			}

        } catch (Exception e) {
        	NtbLogger.error(e);
        	throw new BusinessException(e);
        }
	}

	public static HashMap<String,HashMap<DataCell,ArrayList<String>>> getCubeStartCtrlformulaVO(MdTask[] plans) throws BusinessException{
		try{
			String tmpTableName = "NTB_TMP_CUBE_DATA";
			ArrayList<ArrayList> _list = new ArrayList<ArrayList> ();
			for(int n=0;n<plans.length;n++){
				ArrayList<String> tmpList = new ArrayList<String> ();
				tmpList.add(plans[n].getPrimaryKey()==null?" ":plans[n].getPrimaryKey()); //yuyonga
				tmpList.add(plans[n].getPrimaryKey()==null?" ":plans[n].getPrimaryKey());
				_list.add(tmpList);
			}
			CtlSchemeCTL.createNtbTempTable(null, _list);
			StringBuffer sWhere_cube = new StringBuffer();
			sWhere_cube.append("pk_plan in (");
			sWhere_cube.append("select DATACELLCODE from ").append(tmpTableName);
			sWhere_cube.append(") and pk_parent is not null");
			SuperVO[] vos = NtbSuperServiceGetter.getINtbSuper().queryByCondition(IdCtrlformulaVO.class, sWhere_cube.toString());
			CtlSchemeCTL.deleteTmpTable(tmpTableName);

			//一个任务有多少单元格,一个单元格对应多少公式
			HashMap<String,HashMap<DataCell,ArrayList<String>>> map = new HashMap<String,HashMap<DataCell,ArrayList<String>>> ();


			if(vos!=null&&vos.length>0){
				for(int i=0;i<vos.length;i++){
					IdCtrlformulaVO vo = (IdCtrlformulaVO)vos[i];
					String pk_cubeformula = vo.getPk_parent();    //公式
					String pk_plan = vo.getPk_plan();             //任务
					String pk_cube = vo.getPk_cube();             //模型
					String dimcode = vo.getPk_dimvector();        //维度组合
					HashMap<DataCell,ArrayList<String>> tmpMap = map.get(pk_plan);   //一个任务有多少个模型
					DataCell tempcell = getDataCellByDimVector(pk_cube, new String[] {dimcode}).get(dimcode);
					if(tmpMap==null){
						tmpMap = new HashMap<DataCell,ArrayList<String>> ();
						map.put(pk_plan, tmpMap);
					}
					if(tmpMap.containsKey(tempcell)){
						ArrayList<String> list = (ArrayList<String>)tmpMap.get(tempcell);
						list.add(pk_cubeformula);
					}else{
						ArrayList<String> list = new ArrayList<String>();
						list.add(pk_cubeformula);
						tmpMap.put(tempcell, list);
					}
				}
			}
			return map;
		}catch (Exception e) {
			NtbLogger.error(e);
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052")/*未知异常*/);
		}

	}

	/**
	 * 根据维度成员Code组合得到对应的DateCell
	 * */
	public static HashMap<String,DataCell> getDataCellByDimVector(String pk_cubeDef,String[] src) throws Exception{
		IDataSetService idss = CubeServiceGetter.getDataSetService();
		CubeDef cubedef = CubeServiceGetter.getCubeDefQueryService().queryCubeDefByPK(pk_cubeDef);
		IStringConvertor cvt = StringConvertorFactory.getConvertor(DimVector.class);
		ArrayList<DimVector> dimvectors = new ArrayList<DimVector> ();
		HashMap<String,DataCell> map = new HashMap<String,DataCell> ();
		for(int n=0;n<src.length;n++){
			String pk_dimvector = src[n];
			DimVector dimvector = (DimVector)cvt.fromString(pk_dimvector);
			dimvectors.add(dimvector);
		}
		ICubeDataSet dataSet = idss.queryDataSet(cubedef, dimvectors);
		for(int n=0;n<src.length;n++){
			DataCell datacell = dataSet.getDataCell(dimvectors.get(n));
			if(map.get(src[n])==null){
				map.put(src[n], datacell);
			}
		}
		return map;
	}

	public static HashMap<String,DataCell> getDataCellPkCubeByDimVector(String pk_cubeDef,String[] src) throws Exception{
		IDataSetService idss = CubeServiceGetter.getDataSetService();
		CubeDef cubedef = CubeServiceGetter.getCubeDefQueryService().queryCubeDefByPK(pk_cubeDef);
		IStringConvertor cvt = StringConvertorFactory.getConvertor(DimVector.class);
		ArrayList<DimVector> dimvectors = new ArrayList<DimVector> ();
		HashMap<String,DataCell> map = new HashMap<String,DataCell> ();
		for(int n=0;n<src.length;n++){
			String pk_dimvector = src[n];
			DimVector dimvector = (DimVector)cvt.fromString(pk_dimvector);
			dimvectors.add(dimvector);
		}
		ICubeDataSet dataSet = idss.queryDataSet(cubedef, dimvectors);
		for(int n=0;n<src.length;n++){
			DataCell datacell = dataSet.getDataCell(dimvectors.get(n));
			if(map.get(src[n])==null&&dimvectors.get(n).matchesCubeDef(cubedef)){
				map.put(pk_cubeDef+src[n], datacell);
			}
		}
		return map;
	}


	public static HashMap<String,DataCell> getDataCellPkCubeByDimVector(String pk_cubeDef,String[] src, Map<String,Map<DimVector, DataCellValue>> cubeMap) throws BusinessException {
		IDataSetService idss = CubeServiceGetter.getDataSetService();
		CubeDef cubedef = CubeServiceGetter.getCubeDefQueryService().queryCubeDefByPK(pk_cubeDef);
		IStringConvertor cvt = StringConvertorFactory.getConvertor(DimVector.class);
		ArrayList<DimVector> dimvectors = new ArrayList<DimVector> ();
		HashMap<String,DataCell> map = new HashMap<String,DataCell> ();
		for(int n=0;n<src.length;n++){
			String pk_dimvector = src[n];
			DimVector dimvector = (DimVector)cvt.fromString(pk_dimvector);
			dimvectors.add(dimvector);
		}
		ICubeDataSet dataSet = idss.queryDataSet(cubedef, dimvectors);
		for(int n=0;n<src.length;n++){
			DataCell datacell = dataSet.getDataCell(dimvectors.get(n));
			DataCellValue value = null;
			Map<DimVector, DataCellValue> valueMap = cubeMap.get(pk_cubeDef);
			if(valueMap != null) {
				value = valueMap.get(dimvectors.get(n));
			}
			if(value != null)
				datacell.setCellValue(value);
			if(map.get(src[n])==null){
				map.put(pk_cubeDef+src[n], datacell);
			}
		}
		return map;
	}

//	public static HashMap<String,DimFormulaVO[]> getCtrlFormulaByPlanPks(MdTask[] plans) throws BusinessException {
//
//    HashMap<String,DimFormulaVO[]> map = new HashMap<String,DimFormulaVO[]> ();
//
//    for(int n=0;n<plans.length;n++){
//    	MdTask plan = plans[n];
//    	String pk_cube = plan.getMetaPlan().getPrimaryKey();
//    	if(map.get(pk_cube)==null){
//    		StringBuffer swhere = new StringBuffer();
//			swhere.append("pk_cube = '").append(pk_cube).append("'");
//			swhere.append(" and pk_ruleclass in (").append("'").append(
//					IRuleClassConst.SCHEMA_SINGLE).append("',").append("'").append(
//					IRuleClassConst.SCHEMA_GROUP).append("',").append("'").append(
//					IRuleClassConst.SCHEMA_SPEC).append("',").append("'").append(
//					IRuleClassConst.SCHEMA_FLEX).append("'").append(")");
//			SuperVO[] vos = NtbSuperServiceGetter.getINtbSuper().queryByCondition(DimFormulaVO.class, swhere.toString());
//			if(vos!=null&&vos.length>0){
//			    map.put(pk_cube, (DimFormulaVO[])vos);
//			}else{
//				map.put(pk_cube,new DimFormulaVO[0]);
//			}
//    	}
//    }
//
//	return map;
//    }


	public static HashMap<String,List<AllotFormulaVo>> allotFormula(MdTask[] plans) throws BusinessException {
//		ITaskBusinessService dataService = TbTaskServiceGetter.getTaskBusinessService();
		HashMap<String,List<AllotFormulaVo>> map = new HashMap<String,List<AllotFormulaVo>>  ();
		for(int n=0;n<plans.length;n++){
			List<AllotFormulaVo>  formulaVO = new ArrayList<AllotFormulaVo> ();
			ArrayList<BusiRuleVO> allRules = RuleServiceGetter.getIBusiRuleQuery().queryByRuleAndMdWorkbook(plans[n].getPk_workbook());
			DimSectionTuple defaultSectionTuple = TbTaskCtl.getTaskParadim(plans[n]);
//			CubeDataCellSet dataSet = dataService.getDataCellsByTask(plans[n].getPrimaryKey(), null);
			if (allRules != null && !allRules.isEmpty()) {

//				formulaVO = RuleServiceGetter.getRuleExecuteService().executeCtrlAllotFormula(defaultSectionTuple, allRules, dataSet.getDataCellMap(), ICutCube.TYPE_ALLOT);
//				formulaVO = RuleServiceGetter.getRuleExecuteService().
			//	TaskDataModel	taskDataModel = TaskDataCtl.getTaskDataModel(plans[n].getPrimaryKey(), null, false, null);
			//	formulaVO = RuleServiceGetter.getRuleExecuteService().getCtrlAllotFormulaForWorkBook(defaultSectionTuple, allRules, taskDataModel);
				formulaVO = TbTaskServiceGetter.getTaskRuleExecuteAdapter().getCtrlAllotForWorkBook(plans[n], allRules);

//				dataSet.setFmlExpress(formulaVO, CellFmlInfo.fmlType_control);
			}
			map.put(plans[n].getPrimaryKey(), formulaVO);
		}
		return map;
	}
	/**前台完成启动控制方案*/
	public static void onUseAllCrelSchemeInClient(MdTask[] plans) throws BusinessException {
	    /**yuyonga 停止计划上所有的控制方案,因为如果不在这个地方停用,就无法全部启用计划上的控制方案,累计的问题就不能够解决*/
		HashMap<String,List<AllotFormulaVo>>  formulaVO = new HashMap<String,List<AllotFormulaVo>> ();
		stopCtrlScheme(plans);
		formulaVO = allotFormula(plans);
		ArrayList<CtrlSchemeVO> list = new ArrayList<CtrlSchemeVO> ();
		for(int m=0;m<plans.length;m++){
			String pk_plan = plans[m].getPrimaryKey();
			List<AllotFormulaVo> vos = formulaVO.get(plans[m].getPrimaryKey());
			for(int n=0;n<vos.size();n++){
			   AllotFormulaVo vo = vos.get(n);
			   String express = vo.getCellExpress().replaceAll("\'", "\"");
			   String formulaBd = formatBudgetData(2, express);
			   CtrlSchemeVO schemevo = new CtrlSchemeVO (formulaBd,vo.getFormulaVoPk(),vo.getAllotCell(),pk_plan);
			   list.add(schemevo);
			}
		}
		if(list.size() > 0)
			CtlSchemeCTL.startCtrlScheme(list);

	}


	/**前台完成启动控制方案*/
	/**
	 * 有时候会在方法外已经查询出公式单元格，为提高效率，调用这个方法
	 * @param plans
	 * @param formulaVO
	 * @throws BusinessException
	 */
	public static Object onUseAllCrelSchemeInClient(MdTask[] plans, Map<String, List<AllotFormulaVo>> formulaVO) throws BusinessException {
	    /**yuyonga 停止计划上所有的控制方案,因为如果不在这个地方停用,就无法全部启用计划上的控制方案,累计的问题就不能够解决*/
//		HashMap<String,List<AllotFormulaVo>>  formulaVO = new HashMap<String,List<AllotFormulaVo>> ();
		stopCtrlScheme(plans);
//		formulaVO = allotFormula(plans);
		ArrayList<CtrlSchemeVO> list = new ArrayList<CtrlSchemeVO> ();
		for(int m=0;m<plans.length;m++){
			String pk_plan = plans[m].getPrimaryKey();
			List<AllotFormulaVo> vos = formulaVO.get(plans[m].getPrimaryKey());
			for(int n=0;n<vos.size();n++){
			   AllotFormulaVo vo = vos.get(n);
			   String express = vo.getCellExpress().replaceAll("\'", "\"");
			   String formulaBd = formatBudgetData(2, express);
			   CtrlSchemeVO schemevo = new CtrlSchemeVO (formulaBd,vo.getFormulaVoPk(),vo.getAllotCell(),pk_plan);
			   list.add(schemevo);
			}
		}
		return CtlSchemeCTL.startCtrlScheme(list);

	}

	public static String formatBudgetData(int digit, String formula) {
		String planData = formula.substring(0, formula.indexOf('*'));
		boolean isDigit = true;
		if(planData.indexOf("FLEXEXPRESS")>=0){
			isDigit = false;
		}
		try {
			Double.parseDouble(planData);
		} catch(NumberFormatException e) {
			isDigit = false;
		}

		if(isDigit) {

			UFDouble data = new UFDouble(planData).setScale(digit, UFDouble.ROUND_HALF_UP);
			String anoFormula = formula.replaceFirst(planData, data.toString());
			return anoFormula;

		}
		return formula;
	}

	public static String asynStartControl(MdTask[] plans) throws BusinessException {
		IPreAlertConfigService access = (IPreAlertConfigService) NCLocator.getInstance().lookup(IPreAlertConfigService.class.getName());
		try {
			access.startReportLikeWork(new AsynStartContrlWork(plans), null);
		} catch (BusinessException e) {
			throw new BusinessException(e);
		}
		return null;

	}


	public static void stopCtrlScheme(MdTask[] plans) throws BusinessException{
		try {
//			StringBuffer sWhere = new StringBuffer();
//			sWhere.append("pk_plan in (");
//			for(int i=0;i<plans.length;i++){
//				sWhere.append("'");
//				sWhere.append(plans[i].getPrimaryKey());
//				sWhere.append("'");
//				if(i != plans.length-1){
//					sWhere.append(",");
//				}
//			}
//			sWhere.append(") and isstarted = 'Y'");
//			String sWhere_cube = sWhere.toString() + " and pk_parent is not null";
//			String sWhere_plan = sWhere.toString() + " and "+VoConvertor.getIsNullSql("pk_parent");
			/**批量停用控制方案时根据pk查询，大数据量的情况下改为使用临时表，否则查询数据报错*/
			if(plans == null)
				return;
			List<String> listPks = new ArrayList<String>();
			for(MdTask plan : plans){
				if(plan == null)
					continue;
				listPks.add(plan.getPrimaryKey());
			}

			//模型上启动的控制方案
//			Map<String, List<String>> ctlmap_Cube= CtlSchemeCTL.queryCtrlSchemeSimply(sWhere_cube);
			Map<String, List<String>> ctlmap_Cube= CtlSchemeCTL.queryCtrlSchemeSimply(listPks);
			//计划上的控制方案
//			HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> ctlmap_Plan= CtlSchemeCTL.queryCtrlScheme(sWhere_plan);

			if(ctlmap_Cube!=null&&!ctlmap_Cube.isEmpty()){
				CtlSchemeCTL.deleteCtrlScheme(ctlmap_Cube);
			}
//			计划上的控制方案停用
//			if(ctlmap_Plan!=null&&!ctlmap_Plan.isEmpty()){
//				Iterator<IdCtrlformulaVO> iteraKey = ctlmap_Plan.keySet().iterator();
//				while(iteraKey.hasNext()){
//					IdCtrlformulaVO vo = (IdCtrlformulaVO)iteraKey.next();
//					vo.setIsstarted(UFBoolean.valueOf("N"));
//					ArrayList<IdCtrlschemeVO> templist = ctlmap_Plan.get(vo);
//					for(int i=0;i<templist.size();i++){
//						IdCtrlschemeVO tempchildrenvo = templist.get(i);
//						tempchildrenvo.setIsstarted(UFBoolean.valueOf("N"));
//						tempchildrenvo.setStartdate(null);
//						tempchildrenvo.setEnddate(null);
//					}
//				}
//				CtlSchemeCTL.updateCtrlSchemeVOs(ctlmap_Plan);
//			}



		}catch (BusinessException ex) {
			throw ex;
		}
		catch (Exception e) {
			throw new BusinessException(e);//NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052")/*未知异常*/);
		}

	}

	public static Map<String, List<String>> queryCtrlSchemeSimply(String sWhere) throws BusinessException{
		try{

			Map<String, List<String>> map = CtlSchemeServiceGetter.getICtlScheme().queryCtrlSchemeSimply(sWhere);
			return map;
		}catch (BusinessException e) {
			throw e;
		}catch (Exception e) {
			NtbLogger.error(e);
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052")/*未知异常*/);
		}
	}
	
	public static Map<String, List<String>> queryCtrlSchemeSimply(List<String> listPks) throws BusinessException{
		try{

			Map<String, List<String>> map = CtlSchemeServiceGetter.getICtlScheme().queryCtrlSchemeSimply(listPks);
			return map;
		}catch (BusinessException e) {
			throw e;
		}catch (Exception e) {
			NtbLogger.error(e);
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000052")/*未知异常*/,e);
		}
	}

	public static void updateCtrlSchemeVOs(HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> notStartCtrlscheme) throws BusinessException{
		try {
			CtlSchemeServiceGetter.getICtlScheme().updateCtrlSchemeVOs(notStartCtrlscheme);
		}
		catch (BusinessException ex) {
			throw ex;
		}
	}

	public static void updateExistCtrlSchemeFind(ArrayList<MdTask> planLists) throws Exception {

		StringBuffer sWhere  = new StringBuffer();
		sWhere.append("isstarted = 'Y' and pk_plan in ('");
		for(int n=0;n<(planLists==null?0:planLists.size());n++){
			if(n!=planLists.size()-1){
		    	sWhere.append(planLists.get(n).getPrimaryKey()).append("','");
			}else{
				sWhere.append(planLists.get(n).getPrimaryKey());
			}
		}
		sWhere.append("')");
		HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> ctrlscheme= CtlSchemeCTL.queryCtrlSchemeByCtrlformula(sWhere.toString());
//		/**取相关的控制方案*/
//		String sqlWhere  = " select pk_dimvector from tb_ctrlformula where "+sWhere;
//		StringBuffer _sWhere  = new StringBuffer();
//		_sWhere.append("isstarted = 'Y' and pk_dimvector in (").append(sqlWhere).append(")");
//
//		HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> _ctrlscheme= CtlSchemeCTL.queryCtrlScheme(_sWhere.toString());
//		ctrlscheme.putAll(_ctrlscheme);
		/**end*/
		HashMap<String,DataCell> map = new HashMap<String,DataCell> ();
		HashMap<String,ArrayList<String>> pkMap = new HashMap<String,ArrayList<String>> ();
		Iterator _iter= ctrlscheme.entrySet().iterator();
		while(_iter.hasNext()){
			Map.Entry entry = (Map.Entry)_iter.next();
			IdCtrlformulaVO vo = (IdCtrlformulaVO)entry.getKey();
            if(pkMap.get(vo.getPk_cube())==null){
            	ArrayList<String> list = new ArrayList<String> ();
            	list.add(vo.getPk_dimvector());
            	pkMap.put(vo.getPk_cube(),list);
            }else{
            	pkMap.get(vo.getPk_cube()).add(vo.getPk_dimvector());
            }
		}
		Iterator iterPkMap= pkMap.entrySet().iterator();
		while(iterPkMap.hasNext()){
			Map.Entry entry = (Map.Entry)iterPkMap.next();
			String str = (String)entry.getKey();
			ArrayList<String> sList = (ArrayList<String>)entry.getValue();
			map.putAll(getDataCellPkCubeByDimVector(str,sList.toArray(new String[0])));
		}

		ArrayList<IdCtrlformulaVO> updatevo = new ArrayList<IdCtrlformulaVO> ();
		HashMap<String,DimFormulaVO> formulaMap = new HashMap<String,DimFormulaVO> ();

		List<IdCtrlformulaVO> changedFormulaDvs = new ArrayList<IdCtrlformulaVO>();
		Iterator iter= ctrlscheme.entrySet().iterator();
		parserMap.clear();
		while(iter.hasNext()){
			Map.Entry entry = (Map.Entry)iter.next();
			IdCtrlformulaVO vo = (IdCtrlformulaVO)entry.getKey();
			if(formulaMap.get(vo.getPk_parent())==null){
				DimFormulaVO dimformulavo = FormulaCTL.getDimFormulaByPrimaryKey(vo.getPk_parent());  //这里有一次远程调用
				formulaMap.put(vo.getPk_parent(), dimformulavo);
			}
			ArrayList<IdCtrlschemeVO> vos = (ArrayList<IdCtrlschemeVO>)entry.getValue();
			IdCtrlschemeVO[] schemeArr = new CtrlExprManager().getLinkedSchemes(vos.toArray(new IdCtrlschemeVO[0]));

			String express = againCalculate(map.get(vo.getPk_cube()+vo.getPk_dimvector()),schemeArr,formulaMap.get(vo.getPk_parent()));
			express = formatBudgetData(2, express);
			vo.setExpressformula(express);
			UFDouble orginPlanValue = vo.getPlanvalue();
			UFDouble nextPlanValue = getPlanValue(express, vos);
			if(!orginPlanValue.equals(nextPlanValue))
				changedFormulaDvs.add(vo);

			vo.setPlanvalue(nextPlanValue);
			updatevo.add(vo);
		}

		/**取相关的控制方案*/
//		String sqlWhere  = " select pk_dimvector from tb_ctrlformula where "+sWhere;
		if(changedFormulaDvs.size() > 0) {
			StringBuffer sqlWhere = new StringBuffer();
			int index = 0;
			for(IdCtrlformulaVO vo : changedFormulaDvs) {
				sqlWhere.append("'").append(vo.getPk_dimvector()).append("'");
				if(++index != changedFormulaDvs.size())
					sqlWhere.append(",");
			}
			StringBuffer _sWhere  = new StringBuffer();
			_sWhere.append("isstarted = 'Y' and pk_dimvector in (").append(sqlWhere.toString()).append(")");

			//查询其它任务上同一个单元格上的控制方案
			HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> _ctrlscheme= CtlSchemeCTL.queryCtrlSchemeByCtrlformula(_sWhere.toString());
			
			//查询其他任务上可能通过累计涉及到本任务单元格上的控制方案
			StringBuffer sqlWhere2 = new StringBuffer();
			sqlWhere2.append("accctrollflag != 'DONOTACC' and accctrollflag != 'N' and pk_ctrlformula in( select pk_obj from tb_ctrlformula where pk_parent in (");
			for(IdCtrlformulaVO vo : changedFormulaDvs) {
				sqlWhere2.append("'").append(vo.getPk_parent()).append("',");
			}
			sqlWhere2.deleteCharAt(sqlWhere2.length() - 1);
			sqlWhere2.append(")");
			sqlWhere2.append(")");
			
			Map<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> _ctrlscheme_acc = CtlSchemeCTL.queryCtrlSchemeBySchemes(sqlWhere2.toString());
			
			_ctrlscheme.putAll(_ctrlscheme_acc);
			
			HashMap<String,ArrayList<String>> pkMap1 = new HashMap<String,ArrayList<String>> ();
			Iterator _iter1 = _ctrlscheme.entrySet().iterator();
			while(_iter1.hasNext()){
				Map.Entry entry = (Map.Entry)_iter1.next();
				IdCtrlformulaVO vo = (IdCtrlformulaVO)entry.getKey();
	            if(pkMap1.get(vo.getPk_cube())==null){
	            	ArrayList<String> list = new ArrayList<String> ();
	            	list.add(vo.getPk_dimvector());
	            	pkMap1.put(vo.getPk_cube(),list);
	            }else{
	            	pkMap1.get(vo.getPk_cube()).add(vo.getPk_dimvector());
	            }
			}
			Iterator iterPkMap1 = pkMap1.entrySet().iterator();
			while(iterPkMap1.hasNext()){
				Map.Entry entry = (Map.Entry)iterPkMap1.next();
				String str = (String)entry.getKey();
				ArrayList<String> sList = (ArrayList<String>)entry.getValue();
				map.putAll(getDataCellPkCubeByDimVector(str,sList.toArray(new String[0])));
			}
			 
			Iterator iterNext = _ctrlscheme.entrySet().iterator();
			while(iterNext.hasNext()){
				Map.Entry entry = (Map.Entry)iterNext.next();
				IdCtrlformulaVO vo = (IdCtrlformulaVO)entry.getKey();

				if(map.containsKey(vo.getPk_cube()+vo.getPk_dimvector())) {
					if(formulaMap.get(vo.getPk_parent())==null){
						DimFormulaVO dimformulavo = FormulaCTL.getDimFormulaByPrimaryKey(vo.getPk_parent());  //这里有一次远程调用
						formulaMap.put(vo.getPk_parent(), dimformulavo);
					}
					ArrayList<IdCtrlschemeVO> vos = (ArrayList<IdCtrlschemeVO>)entry.getValue();

					IdCtrlschemeVO[] schemeArr = new CtrlExprManager().getLinkedSchemes(vos.toArray(new IdCtrlschemeVO[0]));
					String express = againCalculate(map.get(vo.getPk_cube()+vo.getPk_dimvector()),schemeArr,formulaMap.get(vo.getPk_parent()));
					express = formatBudgetData(2, express);
					vo.setExpressformula(express);

					vo.setPlanvalue(getPlanValue(express,vos));
					updatevo.add(vo);
				}
			}
		}

		CtlSchemeServiceGetter.getICtlScheme().updateCtrl(updatevo.toArray(new IdCtrlformulaVO[0]));

	}

	public static String checkExistCtrlSchemeFindByDv(Map<String, Map<DimVector, DataCellValue>> cubeMap) throws BusinessException {
		return CtlSchemeServiceGetter.getICtlScheme().checkExistCtrlSchemeFindByDv(cubeMap);
	}

	public static String checkExistCtrlSchemeFindByDvInDirectAdjust(Map<String, List<DataCell>> changedDataCells) throws BusinessException {

		Map<String, Map<DimVector, DataCellValue>> cubeToDvAndValue = new HashMap<String, Map<DimVector, DataCellValue>>();

		for(Map.Entry<String, List<DataCell>> entry : changedDataCells.entrySet()) {
			CubeDef cubeDef = CubeServiceGetter.getCubeDefQueryService().queryCubeDefByBusiCode(entry.getKey());
			String pk_cube = cubeDef.getPk_obj();
			List<DataCell> dataCellList = entry.getValue();

			Map<DimVector, DataCellValue> dvToValue = cubeToDvAndValue.containsKey(pk_cube) ? cubeToDvAndValue.get(pk_cube) : new HashMap<DimVector, DataCellValue>();
			for(DataCell dataCell : dataCellList) {
				DimVector dimvector = dataCell.getDimVector();
				DataCellValue value = dataCell.getCellValue();
				dvToValue.put(dimvector, value);
			}

			if(!cubeToDvAndValue.containsKey(pk_cube))
				cubeToDvAndValue.put(pk_cube, dvToValue);
		}
		return checkExistCtrlSchemeFindByDv(cubeToDvAndValue);
	}

//	public static void updateExistCtrlSchemeFindByDv(ArrayList<ICubeDataSet> dataSets) throws Exception {
//		HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> ctrlscheme = new HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> ();
//		for(int n=0;n<dataSets.size();n++){
//	            String pk_cube = dataSets.get(n).getCubeDef().getPrimaryKey();
//	            List<DataCell>  datacells =  dataSets.get(n).getDataResult();
//	            ArrayList<DimVector> dvList = new ArrayList<DimVector> ();
//	            for(int m=0;m<datacells.size();m++){
//	            	DataCell datacell = datacells.get(m);
//	            	dvList.add(datacell.getDimVector());
//	            }
//
//	    		String tmpTableName = "NTB_TMP_CUBE_DATA";
//				ArrayList<ArrayList> _list = new ArrayList<ArrayList> ();
//	    		IStringConvertor cvt = StringConvertorFactory.getConvertor(DimVector.class);
//				for(int m=0;m<dvList.size();m++){
//					ArrayList<String> tmpList = new ArrayList<String> ();
//					tmpList.add(cvt.convertToString(dvList.get(m))==null?" ":cvt.convertToString(dvList.get(m))); //yuyonga
//					tmpList.add(cvt.convertToString(dvList.get(m))==null?" ":cvt.convertToString(dvList.get(m)));
//					_list.add(tmpList);
//				}
//				//tmpTableName = CtlSchemeCTL.createNtbTempTable_new(null,tmpTableName, _list);
////				StringBuffer sWhere_cube = new StringBuffer();
////				sWhere_cube.append("isstarted = 'Y' and pk_dimvector in (");
////				sWhere_cube.append("select DATACELLCODE from ").append(tmpTableName);
//////				sWhere_cube.append(") and pk_parent is not null");
////				sWhere_cube.append(")").append(" and pk_cube = '").append(pk_cube).append("'");
////				SuperVO[] vos = NtbSuperServiceGetter.getINtbSuper().queryByCondition(IdCtrlformulaVO.class, sWhere_cube.toString());
//				HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> qryResult = CtlSchemeCTL.createNtbTempTable(dataSets.get(n).getCubeDef(), _list);
//				ctrlscheme.putAll(qryResult);
////				CtlSchemeCTL.deleteTmpTable(tmpTableName);
//
//
////	    		StringBuffer sWhere  = new StringBuffer();
////	    		IStringConvertor cvt = StringConvertorFactory.getConvertor(DimVector.class);
////	    		sWhere.append("isstarted = 'Y' and pk_dimvector in ('");
////	    		for(int i=0;i<(dvList==null?0:dvList.size());i++){
////	    			if(i!=dvList.size()-1){
////	    		    	sWhere.append(cvt.convertToString(dvList.get(i))).append("','");
////	    			}else{
////	    				sWhere.append(cvt.convertToString(dvList.get(i)));
////	    			}
////	    		}
////	    		sWhere.append("')").append(" and pk_cube = '").append(pk_cube).append("'");
////	    		ctrlscheme.putAll(CtlSchemeCTL.queryCtrlScheme(sWhere.toString()));
//		}
//
//		HashMap<String,DataCell> map = new HashMap<String,DataCell> ();
//		HashMap<String,ArrayList<String>> pkMap = new HashMap<String,ArrayList<String>> ();
//		Iterator _iter= ctrlscheme.entrySet().iterator();
//		while(_iter.hasNext()){
//			Map.Entry entry = (Map.Entry)_iter.next();
//			IdCtrlformulaVO vo = (IdCtrlformulaVO)entry.getKey();
//            if(pkMap.get(vo.getPk_cube())==null){
//            	ArrayList<String> list = new ArrayList<String> ();
//            	list.add(vo.getPk_dimvector());
//            	pkMap.put(vo.getPk_cube(),list);
//            }else{
//            	pkMap.get(vo.getPk_cube()).add(vo.getPk_dimvector());
//            }
//		}
//		Iterator iterPkMap= pkMap.entrySet().iterator();
//		while(iterPkMap.hasNext()){
//			Map.Entry entry = (Map.Entry)iterPkMap.next();
//			String str = (String)entry.getKey();
//			ArrayList<String> sList = (ArrayList<String>)entry.getValue();
//			map.putAll(getDataCellPkCubeByDimVector(str,sList.toArray(new String[0])));
//		}
//
//		ArrayList<IdCtrlformulaVO> updatevo = new ArrayList<IdCtrlformulaVO> ();
//		HashMap<String,DimFormulaVO> formulaMap = new HashMap<String,DimFormulaVO> ();
//		Iterator iter= ctrlscheme.entrySet().iterator();
//		parserMap.clear();
//		while(iter.hasNext()){
//			Map.Entry entry = (Map.Entry)iter.next();
//			IdCtrlformulaVO vo = (IdCtrlformulaVO)entry.getKey();
//			if(formulaMap.get(vo.getPk_parent())==null){
//				DimFormulaVO dimformulavo = FormulaCTL.getDimFormulaByPrimaryKey(vo.getPk_parent());  //这里有一次远程调用
//				formulaMap.put(vo.getPk_parent(), dimformulavo);
//			}
//			ArrayList<IdCtrlschemeVO> vos = (ArrayList<IdCtrlschemeVO>)entry.getValue();
//			IdCtrlschemeVO[] schemeArr = new CtrlExprManager().getLinkedSchemes(vos.toArray(new IdCtrlschemeVO[0]));
//			String express = againCalculate(map.get(vo.getPk_cube()+vo.getPk_dimvector()),schemeArr,formulaMap.get(vo.getPk_parent()));
//			vo.setExpressformula(express);
//			vo.setPlanvalue(getPlanValue(express,vos));
//			updatevo.add(vo);
//		}
//		CtlSchemeServiceGetter.getICtlScheme().updateCtrl(updatevo.toArray(new IdCtrlformulaVO[0]));
//
//	}

	public static String againCalculate(DataCell datacell,IdCtrlschemeVO[] vosDB,DimFormulaVO fvo) throws Exception {

		String  fullcontent = getExpress(fvo);

//		DataCell datacell = getDataCellByDimVector(pk_cube, new String[]{pk_dimvector}).get(pk_dimvector);
		FormulaDimCI m_env = new FormulaDimCI();
		m_env.setDataCell(datacell);
		fullcontent = DimFormulaMacro.getParsedFormula(m_env,fullcontent, IRuleClassConst.BIZ_CALC);
		fullcontent = fullcontent.substring(0,fullcontent.length()-1);
		if(parserMap.get(fullcontent)==null){
			StringReader reader = new StringReader(fullcontent);
			TbbLexer lexer  = new TbbLexer(reader);
			TbbParser parser = new TbbParser(lexer);
			parserMap.put(fullcontent, parser.parse());

		}


		Calculator calculator = new Calculator();
		IFormulaContext context = new DefaultFormulaContext(datacell.getCubeDef());
		context.setOwnerCell(new FormulaDataCell(datacell));
		context.setCurrentCubeDef(datacell.getCubeDef());
		calculator.setContext(context);
		context.putExecuteRangeCell(datacell.getDimVector(), datacell);


		WhereDataCellInfo whereDataCellInfo = new WhereDataCellInfo();
		List<DataCell> dCells = new ArrayList<DataCell>();

				dCells.add(datacell);

		whereDataCellInfo.addCells(dCells);
		calculator.getContext().setValue("AbstractCutCube.WhereCells", whereDataCellInfo);


		String result = parserMap.get(fullcontent).toValue(calculator);
		SingleSchema schema = new SingleSchema(result,datacell); //构造SingleSchema
		String returnValue = schema.getExpressFindOrSmFindFormula(vosDB);
		return returnValue;
	}

	public static String againCalculate(DataCell datacell,IdCtrlschemeVO[] vosDB,DimFormulaVO fvo,Map<DimVector,DataCellValue> cubeMap) throws BusinessException {

		  DataCellValue value = cubeMap.get(datacell.getDimVector());
		  datacell.setCellValue(value);
		  String  fullcontent = getExpress(fvo);

		//  DataCell datacell = getDataCellByDimVector(pk_cube, new String[]{pk_dimvector}).get(pk_dimvector);
		  FormulaDimCI m_env = new FormulaDimCI();
		  m_env.setDataCell(datacell);
		  fullcontent = DimFormulaMacro.getParsedFormula(m_env,fullcontent, IRuleClassConst.BIZ_CALC);
		  String returnValue = null;

		  try {
			  fullcontent = fullcontent.substring(0,fullcontent.length()-1);
			  if(parserMap.get(fullcontent)==null){
				  StringReader reader = new StringReader(fullcontent);
				  TbbLexer lexer  = new TbbLexer(reader);
				  TbbParser parser = new TbbParser(lexer);
				  parserMap.put(fullcontent, parser.parse());

			  }


			  Calculator calculator = new Calculator();
			  IFormulaContext context = new DefaultFormulaContext(datacell.getCubeDef());
			  context.setOwnerCell(new FormulaDataCell(datacell));
			  context.setCurrentCubeDef(datacell.getCubeDef());
			  calculator.setContext(context);
			  if(cubeMap!=null){
				  for(DimVector dv:cubeMap.keySet()){
					  DataCell cachCell = new DataCell(datacell.getCubeDef(), dv);
					  if(cubeMap.get(dv)!=null){
						  cachCell.setCellValue(cubeMap.get(dv));
					  }else{
						  cachCell.setCellValue(new DataCellValue(0));
					  }
					  context.putExecuteRangeCell(dv, cachCell);
				  }
			  }
		
			  context.putExecuteRangeCell(datacell.getDimVector(), datacell);
			  

			  WhereDataCellInfo whereDataCellInfo = new WhereDataCellInfo();
			  List<DataCell> dCells = new ArrayList<DataCell>();

			  dCells.add(datacell);

			  whereDataCellInfo.addCells(dCells);
			  calculator.getContext().setValue("AbstractCutCube.WhereCells", whereDataCellInfo);



			  String result = parserMap.get(fullcontent).toValue(calculator);

			  //  DataCellValue value = cubeMap.get(datacell.getDimVector());
			  DataCellValue oldvalue = datacell.getCellValue();

			  SingleSchema schema = new SingleSchema(result,datacell); //构造SingleSchema
			  returnValue = schema.getExpressFindOrSmFindFormulaVar(vosDB);
			  datacell.setCellValue(oldvalue);
		  } catch(Exception e) {
			  throw new BusinessException(e.getMessage());
		  }
		  return returnValue;
	}

	private static String getExpress(DimFormulaVO fvo) throws BusinessException{

//			 DimFormulaVO vo =
			StringBuffer sbStr = new StringBuffer();
				DimFormulaVO vo =fvo;
				if(vo.getPk_ruleclass().equals(IRuleClassConst.SCHEMA_FLEX)){
					String s=vo.getExeFullcontent().substring(6, vo.getExeFullcontent().length());
					String[] ss=s.split("from");
					sbStr.append(FormulaCTL.getFullExpress(ss[0].substring(0, ss[0].length()-1),vo.getPrimaryKey())).append(";");
				}else{
					sbStr.append(FormulaCTL.getFullExpress(vo.getFullcontent(),vo.getPrimaryKey())).append(";");
				}
				
			return sbStr.toString();


	}

	public static UFDouble getPlanValue(String express,ArrayList<IdCtrlschemeVO> vos) throws Exception {
		UFDouble planvalue = null;
		String tmpExpress = express;
		for(int n=0;n<vos.size();n++){
			UFDouble tmpValue = new UFDouble(0);
			UFDouble readydata = vos.get(n).getReadydata()==null?UFDouble.ZERO_DBL:vos.get(n).getReadydata();
			UFDouble rundata = vos.get(n).getRundata()==null?UFDouble.ZERO_DBL:vos.get(n).getRundata();
			tmpValue = tmpValue.add(readydata).add(rundata);
			tmpExpress = tmpExpress.replaceAll(vos.get(n).getVarno(), tmpValue.toString());
		}
	    String[] strs = tmpExpress.split(">=");
	    strs[0] = strs[0].replaceAll("%","/100");
	    planvalue = getComplexZxs(strs[0]);
		return planvalue;
	}

	public static ArrayList<DataContrastVO> getDataVOByFindFormula(DimFormulaVO fmlVO, MdWorkbook book) throws BusinessException {
		String pk_formula = fmlVO.getPrimaryKey();
		ArrayList<DimFormulaMVO> formulaMList = RuleServiceGetter.getIBusiRuleQuery().queryMVOByPkFormula(pk_formula);
		return getDataVOByFindFormula(fmlVO.getPk_cube(), formulaMList, book);
	}

	public static ArrayList<DataContrastVO> getDataVOByFindFormula(String pk_cube, List<DimFormulaMVO> formulaMList, MdWorkbook book) throws BusinessException {
		ArrayList<DataContrastVO> newVos = new ArrayList<DataContrastVO>();

		IDimManager dm=DimServiceGetter.getDimManager();
		HashMap<String,ArrayList<LevelValue>> mapValue = new HashMap<String,ArrayList<LevelValue>> ();
		CubeDef cubedef = null;
		try{
		    cubedef = CubeServiceGetter.getCubeDefQueryService().queryCubeDefByPK(pk_cube);
		}catch(BusinessException ex){
			NtbLogger.print(ex);
		}
		ArrayList root = new ArrayList ();
		for(int n=0;n<(formulaMList==null?0:formulaMList.size());n++){
			DimFormulaMVO vo = formulaMList.get(n);
			/**一个VO,对应一个DIMLEVEL和多个LEVELVALUE值*/
			String content = vo.getContent();
			String[] strs = parseDimExpressValues(content);
			if(strs[0].charAt(0) == '\'') continue;
			/**第一个为pk_dimlevel,后面为LevelValue*/
			DimLevel dimlevel = dm.getDimLevelByPK(strs[0]);

			for(int m=1;m<strs.length;m++){
				String pk_levelValue = strs[m];
				LevelValue levelvalue = null;

				if(pk_levelValue.indexOf("@")==0){
					levelvalue = new LevelValue(dimlevel, pk_levelValue.substring(1));
				} else if(pk_levelValue.indexOf("N")==0){   //滚动维的特殊判断
//					levelvalue = dimlevel.getLevelValueByKey(dimlevel.getKeyByString(pk_levelValue));
					levelvalue = new LevelValue(dimlevel,pk_levelValue);
				}else{
					levelvalue = dimlevel.getLevelValueByKey(dimlevel.getKeyByString(pk_levelValue));
				}
			    if(levelvalue==null)
			    	continue;
			    if(mapValue.get(dimlevel.getPrimaryKey())==null){
			    	ArrayList<LevelValue> values = new ArrayList<LevelValue> ();
			    	values.add(levelvalue);
			    	root.add(values);
			    	mapValue.put(dimlevel.getPrimaryKey(), values);
			    }else{
			    	/**检查一下重复数据*/
			    	ArrayList<LevelValue> values = mapValue.get(dimlevel.getPrimaryKey());
			    	if(pk_levelValue.indexOf("@")==0){
			    		values.add(levelvalue);
			    		continue;
			    	}
			    	boolean isExist = false;
			    	for(int k=0;k<values.size();k++){
			    		LevelValue _levelvalue = values.get(k);
			    		if(_levelvalue.getUniqCode().equals(levelvalue.getUniqCode())){
			    			isExist = true;
			    		}
			    	}
			    	if(!isExist){
			    	   values.add(levelvalue);
			    	}
			    }
			}
		}
		/**组合DataContrastVO*/
		DescartesMultiplication cation = new DescartesMultiplication();
        if(root.size()>0){
            ArrayList nodeList = (ArrayList) root.get(0);
            Stack<String> stack= new Stack();
            cation.traverse(root,0,nodeList,stack);
	    }
	    ArrayList list = cation.getResult();
	    for(int n=0;n<list.size();n++){
	    	DataContrastVO vo = new DataContrastVO(cubedef,(Object[])list.get(n));
	    	vo.setBook(book);
	    	newVos.add(vo);
	    }

		return newVos;
	}

	public static String[] parseDimExpressValues(String express){
		String reg = "\\'[^\\)]*\\'";
		Pattern p = Pattern.compile(reg);
		Matcher m = p.matcher(express);
		ArrayList<String> alFunc = new ArrayList<String>();
		while (m.find()) {
			String exp = m.group();
			alFunc.add(exp);
		}
		String finds = alFunc.get(0);
		String[] strs = finds.split(",");
		alFunc.clear();
		for(int n=0;n<strs.length;n++){
			String str = strs[n].substring(strs[n].indexOf("'")+1,strs[n].lastIndexOf("'"));
			alFunc.add(str);
		}
		return alFunc.toArray(new String[0]);
	}

	/**
	 * @param String[] express 一个DataCell上对应的多个模型控制方案公式
	 * songrui 2008.12.22
	 * 根据分配到单元格上的公式表达式实例化HashMap,用于在计划上查看模型上控制方案
	 * */
	public static HashMap<IdCtrlformulaVO, Map<IdCtrlschemeVO, String>> convertFormulaExpress2CtrlVOs(String[] express,String[] formulaPks,DataCell cell,HashMap<IdCtrlformulaVO, ArrayList<IdCtrlschemeVO>> map) throws Exception{

		ICtrlSchemeConvertor convertor = (ICtrlSchemeConvertor)NCLocator.getInstance().lookup(ICtrlSchemeConvertor.class);

		return convertor.convertFormulaExpress2CtrlVOs(express, formulaPks, cell, map);
	}

	//修改和查看控制方案时，VO的转换
	public static HashMap<CtlAggregatedVO, ArrayList<CtlAggregatedVO>> ConvertCtrlFormulaVO2AggregateVO(Map<IdCtrlformulaVO, Map<IdCtrlschemeVO, String>> ctlmap,MdTask plan) throws Exception{

		ICtrlSchemeConvertor convertor = (ICtrlSchemeConvertor)NCLocator.getInstance().lookup(ICtrlSchemeConvertor.class);

		return convertor.convertCtrlFormulaVO2AggregateVO(ctlmap, plan);
	}

	public static void validateNtbParamVO(NtbParamVO vo) throws BusinessException {

		String sysId = vo.getSys_id();
		String[] busiAttr = vo.getBusiAttrs();

		if(sysId.equals(OutEnum.GLSYS)) {
			boolean hasAccount = false;
			for(String busiStr : busiAttr) {
				if(busiStr.equals("DETAIL103")) {
					hasAccount = true;
					break;
				}
			}
			if(!hasAccount) {
				String e = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000321")/*总账系统，控制和取数必须包含会计科目信息，请检查套表和规则中是否包含此类相关信息*/;
				throw new BusinessException(e);
			}
		}
	}


	/**
	 * Cube上公式表达式实例化VO，只是为了显示用，构造VO不需要完整的信息
	 * */
	public static Map convertIdCtrlscheme(DataCell cell,SingleSchema schema,String formulaPk) throws Exception{
		Map<IdCtrlschemeVO, String> schemeMap = new HashMap<IdCtrlschemeVO, String>();
		String[] src_ufind = schema.getUFind();
		String[] src_prefind = schema.getPREUFind();
		String[] split = ((SingleSchema)schema).getFormulaExpress().split("FIND");
		if(src_ufind.length+src_prefind.length!=split.length-1){
			throw new RuntimeException("ParseError");
		}
		try{
			for(int i=0;i<src_ufind.length;i++){
//				src_ufind[i] = src_ufind[i].replaceAll("'", "\"");   //yuyonga修改,重要
				ConvertToCtrlSchemeVO convertor = new ConvertToCtrlSchemeVO(src_ufind[i]);
				IdCtrlschemeVO schemevos = new IdCtrlschemeVO();
				//分配主键
				String[] temp = convertor.getFromItem().split(":");
				StringBuffer buffer = new StringBuffer();
				for(int j=0;j<temp.length;j++){
					buffer.append("null");
					buffer.append(":");
				}
				schemevos.setStridx(buffer.toString());
				schemevos.setCtrlsys(convertor.getCtrlSys());
				schemevos.setBilltype(convertor.getBillType());
				schemevos.setCtrldirection(convertor.getCtrlDirection());
				schemevos.setCtrlobj(convertor.getCtrlObject());
				schemevos.setCtrlobjValue(convertor.getCtrlObjectValue());
				schemevos.setStartdate(convertor.getStartDate());
				schemevos.setEnddate(convertor.getEndDate());
				schemevos.setAccctrollflag(convertor.getAccCtrlFlag());
				schemevos.setFromitems(convertor.getFromItem());
				schemevos.setCodeidx(convertor.getCodeIdx());
				schemevos.setCtllevels(convertor.getCtrlLevel());
				schemevos.setNameidx(convertor.getNameIdx());
				schemevos.setPk_currency(convertor.getPkCurrency());
				//控制方案&&预警方案
				schemevos.setSchtype(OutEnum.SCHEMETYPE[0]);
				schemevos.setSchemetype(getCtlType(formulaPk));
				schemevos.setVarno("var"+(i==0?1:i+1));
				//日期类型，ARAP
				schemevos.setDatetype(convertor.getDataCatalg());
				schemevos.setIsstarted(UFBoolean.valueOf("N"));

				String mainOrg = null;
				String[] isMainOrgs = convertor.getMainOrg().split(":");
				for(int j = 0 ; j < isMainOrgs.length ; j++) {
					UFBoolean isMainOrg = UFBoolean.valueOf(isMainOrgs[j]);
					if(isMainOrg.booleanValue())
						mainOrg = temp[j];
				}
				schemeMap.put(schemevos, mainOrg);
			}

			for(int i=0;i<src_prefind.length;i++){
				ConvertToCtrlSchemeVO convertor = new ConvertToCtrlSchemeVO(src_prefind[i],IFormulaFuncName.PREFIND);
				IdCtrlschemeVO schemevos = new IdCtrlschemeVO();
				//分配主键
				String[] temp = convertor.getFromItem().split(":");
				StringBuffer buffer = new StringBuffer();
				for(int j=0;j<temp.length;j++){
					buffer.append("null");
					buffer.append(":");
				}
				schemevos.setStridx(buffer.toString());
				schemevos.setCtrlsys(convertor.getCtrlSys());
				schemevos.setBilltype(convertor.getBillType());
				schemevos.setCtrldirection(convertor.getCtrlDirection());
				schemevos.setCtrlobj(convertor.getCtrlObject());
				schemevos.setCtrlobjValue(convertor.getCtrlObjectValue());
				schemevos.setPk_currency(convertor.getPkCurrency());
				schemevos.setStartdate(convertor.getStartDate());
				schemevos.setEnddate(convertor.getEndDate());
				schemevos.setAccctrollflag(convertor.getAccCtrlFlag());
				schemevos.setFromitems(convertor.getFromItem());
				schemevos.setCodeidx(convertor.getCodeIdx());
				schemevos.setCtllevels(convertor.getCtrlLevel());
				schemevos.setNameidx(convertor.getNameIdx());
				//控制方案&&预警方案
				schemevos.setSchtype(OutEnum.SCHEMETYPE[0]);
				schemevos.setSchemetype(getCtlType(formulaPk));
				schemevos.setVarno("rar"+(i==0?1:i+1));
				//日期类型，ARAP
				schemevos.setDatetype(convertor.getDataCatalg());
				schemevos.setIsstarted(UFBoolean.valueOf("N"));
				String mainOrg = null;
				String[] isMainOrgs = convertor.getMainOrg().split(":");
				for(int j = 0 ; j < isMainOrgs.length ; j++) {
					UFBoolean isMainOrg = UFBoolean.valueOf(isMainOrgs[j]);
					if(isMainOrg.booleanValue())
						mainOrg = temp[j];
				}
				schemeMap.put(schemevos, mainOrg);
			}

		}catch(Exception ex){
			NtbLogger.error(ex);
			throw ex;
		}

		return schemeMap;
	}

	public static void saveDimRelUapVO(ArrayList<DimRelUapVO> addvos,ArrayList<DimRelUapVO> updatevos,ArrayList<DimRelUapVO> delvos) {
		try{
		    NtbSuperServiceGetter.getINtbSuper().updateDimRelUapVos(addvos, updatevos, delvos);
		    BdContrastCache.getNewInstance().refreshDimRelUapDoc();
		}catch(BusinessException ex){
			NtbLogger.print(ex);
		}
	}

	public static ArrayList<DimRelUapVO> getAllDimRelUapVO() {
		ArrayList<DimRelUapVO> voList = new ArrayList<DimRelUapVO> ();
		try{
			DimRelUapVO[] vos = (DimRelUapVO[])NtbSuperServiceGetter.getINtbSuper().queryAll(DimRelUapVO.class);
			voList.addAll(Arrays.asList(vos));
		}catch(BusinessException ex){
			NtbLogger.print(ex);
		}
		return voList;
	}

	public static String getControlHintMessage_new(String ctrlruletype,String ctrltype)  {
		/**#控制类型#:
			#维度信息#的实际发生数【#发生数#】>预算控制数【#控制数#】。
			已经触发预算控制方案!
			对应控制方案信息：
			#任务信息#的控制方案【#控制规则名称#】。*/
		StringBuffer message = new StringBuffer();
		message.append(CtrlInfoMacroConst.ctrlTypeMacro).append(":").append("\n")
		       .append(CtrlInfoMacroConst.diminfoMacro).append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01420ctl_000165")/*的实际发生数*/).append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000007")/*【*/).append(CtrlInfoMacroConst.rundataMacro).append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000050")/*】*/)
		       .append(">")
		       .append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000255")/*预算控制数*/).append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000007")/*【*/).append(CtrlInfoMacroConst.controldataMacro).append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01420ctl_000166")/*】,*/)
		       .append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01420ctl_000167")/*已经触发预算控制方案!*/).append("\n")
		       .append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01420ctl_000168")/*对应控制方案信息：*/).append("\n")
		       .append(CtrlInfoMacroConst.taskinfoMacro).append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01420ctl_000169")/*的控制方案*/).append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000007")/*【*/).append(CtrlInfoMacroConst.ctrlschemeMacro).append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000050")/*】*/)
		       .append("\n");
		return message.toString();
	}



	/**通过*/
	public static String getControlHintMessage_new_(String ctrlruletype,String ctrltype)  {
		StringBuffer message = new StringBuffer();
		java.text.DecimalFormat formatter = null;
		String spliter = ":";
		String HHF = "\n";
		String FLEXMESSAGE = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_rule", "01801rul_000407")/*是否执行特殊审批？*/;
		String infoss = null;
		String ctrlType = null;
		String frontHint = null;
		StringBuffer diminfoStr = new StringBuffer().append(CtrlInfoMacroConst.diminfoMacro);

		boolean isShowDimInfo = true;
		if(ctrltype.equals(CtrlTypeEnum.RigidityControl.toCodeString())){//刚性控制型
//			frontHint = NCLangRes4VoTransl.getNCLangRes().getStrByID("pf_tb", "CtlSchemeCTL-000011")/*抱歉!  */;
			frontHint = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000240", null, new String[]{CtrlInfoMacroConst.sysinfoMacro})/*{0}刚性控制提示：*/+HHF;
			ctrltype = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000241")/*刚性控制方案*/;
//			infoss = NCLangRes4VoTransl.getNCLangRes().getStrByID("pf_tb", "CtlSchemeCTL-000012")/*,无法进行后续处理。*/;
			infoss = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000310")/*已经超出预算,不允许再进行开支 ！*/+HHF;
		}else if(ctrltype.equals(CtrlTypeEnum.WarningControl.toCodeString())){//预警型
//			frontHint = NCLangRes4VoTransl.getNCLangRes().getStrByID("pf_tb", "CtlSchemeCTL-000013")/*提示:  */;
			frontHint = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000243", null, new String[]{CtrlInfoMacroConst.sysinfoMacro})/*{0}预警提示：*/+HHF;
//			infoss = NCLangRes4VoTransl.getNCLangRes().getStrByID("pf_tb", "CtlSchemeCTL-000014")+"\n"/*。*/;
			infoss = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000244")/*已经超出预算 。*/+HHF;
			ctrltype = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000245")/*预警型控制方案*/;
		}else if(ctrltype.equals(CtrlTypeEnum.FlexibleControl.toCodeString())){ //柔性控制
			frontHint = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000246", null, new String[]{CtrlInfoMacroConst.sysinfoMacro})/*{0}柔性控制提示：*/+HHF;
//			infoss = NCLangRes4VoTransl.getNCLangRes().getStrByID("pf_tb", "CtlSchemeCTL-000015")/*,是否提交特殊审批?*/;
//			infoss = "已经超出预算，是否执行特殊审批？\n";
			infoss = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000247")/*已经超出预算，*/+FLEXMESSAGE+"\n";
			ctrltype = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000248")/*柔性控制方案*/;
		}else if(ctrltype.equals(String.valueOf(CtlSchemeConst.CTL_SCHEMA_ZERO))){
			frontHint = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000249", null, new String[]{CtrlInfoMacroConst.sysinfoMacro})/*{0}外项目提示：*/;
			infoss = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000250")/*无法进行后续处理。*/;
			ctrltype = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000251")/*零预算控制方案*/;
		}

		if(ctrlruletype.equals(String.valueOf(CtlSchemeConst.CTL_SCHEMA_SINGLE))){
			ctrlType = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000201")/*单项控制规则*/;
			isShowDimInfo = true;
		}else if(ctrlruletype.equals(String.valueOf(CtlSchemeConst.CTL_SCHEMA_GROUP))){
			ctrlType = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000252")/*多项控制规则*/;
			isShowDimInfo = true;
		}else if(ctrlruletype.equals(String.valueOf(CtlSchemeConst.CTL_SCHEMA_COMPLEX))){
			ctrlType = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000203")/*特殊控制规则*/;
			isShowDimInfo = true;
		}else if(ctrlruletype.equals(String.valueOf(CtlSchemeConst.CTL_SCHEMA_FLEX))){
			ctrlType = NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000204")/*弹性控制规则*/;
			isShowDimInfo = true;
		}else if(ctrlruletype.equals(String.valueOf(CtlSchemeConst.CTL_SCHEMA_ZERO))){
			ctrlType = "";
			isShowDimInfo = true;
		}

		String ctrlsign =""/* CtrlInfoMacroConst.ctrlSignMacro*/;//replaceCtrlsign(CtrlInfoMacroConst.ctrlSignMacro);  //控制的数据比较符
//		NtbCtrlMath.setPrice(CtrlInfoMacroConst.budgetdataMacro);
//		String value = NtbCtrlMath.getPrice();

//		StringBuffer sbStr = new StringBuffer("##,##0.");
//		for(int n=0;n<Math.abs(powerInt);n++){
//			sbStr.append("0");
//		}
//		DecimalFormat format = new DecimalFormat(sbStr.toString());
//		String zValue = format.format(zxsValue.doubleValue());/*NtbCtrlMath.getPrice();*/
		message.append(CtrlInfoMacroConst.isStartFinishMacro)
					.append(HHF)
					.append(frontHint)
//					.append(ctrlType).append("[").append(parentVO.getCtrlname()).append("]")  //控制方案类型和控制方案名称
					.append(isShowDimInfo?diminfoStr:"")
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000254")/*的*/).append(CtrlInfoMacroConst.taskinfoMacro).append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000254")/*的*/)
					.append(CtrlInfoMacroConst.taskinfoMacro)
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000007")/*【*/)
					.append(CtrlInfoMacroConst.rundataMacro)
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000050")/*】*/)
					.append(ctrlsign)
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000255")/*预算控制数*/)
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000007")/*【*/)
					.append(CtrlInfoMacroConst.budgetdataMacro)
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000256")/*】。*/+HHF)
					.append(infoss)
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000257", null, new String[]{CtrlInfoMacroConst.taskinfoMacro})/*对应{0}系统：*/+HHF)
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000258", null, new String[]{CtrlInfoMacroConst.taskinfoMacro})/*{0}表【*/)
					.append(CtrlInfoMacroConst.taskinfoMacro)
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000259")/*】的*/)
					.append(ctrltype)
					.append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000007")/*【*/).append(CtrlInfoMacroConst.taskinfoMacro).append(NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_ctrl", "01801ctl_000260")/*】。*/)  //控制方案类型和控制方案名称
                    .append("\n");

		return message.toString();
	}

	/**
	 * 判断当前用户是否有单据的功能权限
	 * @param billTypeCode
	 * @return
	 * @throws BusinessException
	 */
	public static boolean isUserHasBillFuncPermission(String billTypeCode) throws BusinessException {
		if(StringUtil.isEmpty(billTypeCode))
			return true;
		BilltypeVO vo = PfDataCache.getBillType(billTypeCode);
		if(vo != null) {
			String userId = InvocationInfoProxy.getInstance().getUserId();
			String groupId = InvocationInfoProxy.getInstance().getGroupId();
			IFunctionPermissionPubService funcPermServ = NCLocator.getInstance().lookup(IFunctionPermissionPubService.class);
			IFunctionPermProfile funcPermProfile = funcPermServ.getFunctionPermProfileWithGroup(userId, groupId);

			return funcPermProfile.hasPermissionOfFuncode(vo.getNodecode());
		}
		return true;
	}

	public static boolean checkTaskStatus(MdTask task){
		boolean checkin = true ;
		String taskStatus = task.getPlanstatus() ;
		if(!taskStatus.equals(ITaskStatus.APPROVE_PASS)){
			checkin = false ;
		}
		return checkin ;
	}

	public static String[] addAlarmScheme(IdAlarmschemeVO[] vos) throws BusinessException{
		try {
			String[] pks = CtlSchemeServiceGetter.getICtlScheme().addAlarmScheme(vos) ;
			return pks ;
		} catch (BusinessException e) {
			// TODO: handle exception
			throw e ;
		}catch(Exception ex){
			NtbLogger.error(ex) ;
			throw new BusinessException(ex.getMessage(),ex) ;
		}
	}

	public static String[] addAlarmDimVector(IdAlarmDimVectorVO[] vos) throws BusinessException {
		String[] pks = CtlSchemeServiceGetter.getICtlScheme().addAlarmDimVector(vos) ;
		return pks ;
	}

	public static Collection<IdAlarmschemeVO> queryAlarmScheme(List<Cell> cells, MdTask task) throws BusinessException{
		try {
			Collection<IdAlarmschemeVO> vos = CtlSchemeServiceGetter.getICtlScheme().queryAlarmScheme(cells,task) ;
			return vos ;
		} catch (BusinessException e) {
			// TODO: handle exception
			throw e ;
		}catch(Exception ex){
			NtbLogger.error(ex) ;
			throw new BusinessException(ex.getMessage(),ex) ;
		}
	}

	public static Map<String,Boolean> queryAlarmSchemeByCell(List<Cell> cells,MdTask task,boolean delAll) throws BusinessException{
		try {
			Map<String,Boolean> map = CtlSchemeServiceGetter.getICtlScheme().queryAlarmSchemeByCell(cells, task,delAll) ;
			return map ;
		} catch (BusinessException e) {
			// TODO: handle exception
			throw e ;
		}catch(Exception ex){
			NtbLogger.error(ex) ;
			throw new BusinessException(ex.getMessage(),ex) ;
		}
	}

	public static Collection<IdAlarmDimVectorVO> queryAlarmDimvector(String sqlWhere) throws BusinessException {
		Collection<IdAlarmDimVectorVO> vos = CtlSchemeServiceGetter.getICtlScheme().queryAlarmDimvector(sqlWhere) ;
		return vos ;
	}

	public static void updateAlarmScheme(IdAlarmschemeVO[] vos) throws BusinessException{
		try {
			CtlSchemeServiceGetter.getICtlScheme().updateAlarmScheme(vos) ;
		} catch (BusinessException e) {
			throw e ;
		}catch(Exception ex){
			NtbLogger.error(ex) ;
			throw new BusinessException(ex.getMessage(),ex) ;
		}
	}

	public static void deleteAlarmScheme(ArrayList<IdAlarmschemeVO> vos) throws BusinessException{
		CtlSchemeServiceGetter.getICtlScheme().deleteAlarmScheme(vos) ;
	}

	public static void deleteAlarmDimVector(List<IdAlarmDimVectorVO> list) throws BusinessException{
		CtlSchemeServiceGetter.getICtlScheme().deleteAlarmDimVector(list) ;
	}
	private static String getFormattedValue(Object value, int digit) {
		try {
			java.text.Format fmt = getFormatter(digit);
			if (fmt != null) {
				if (value != null && value instanceof String) {
					return (String) value;
				}
				return (value == null) ? null : fmt.format(value);
			}
		} catch (Exception e) {
			NtbLogger.printException(e);
		}
		return (value == null) ? "" : value.toString();
	}

	private static java.text.Format getFormatter(int digit) throws BusinessException {
		String formatString = getNumberFormatString(digit);
		return new DecimalFormat(formatString);
	}
	
	private static String getNumberFormatString(int digit) {
		String formatString = "#";
		formatString += "0";
		if (digit > 0) {
			formatString += ".";
			for (int i = 0; i < digit; i++) {
				formatString += "0";
			}
		}
		return formatString;
	}
	
//	/**
//	 * 将NtbParamVO转换成公式
//	 * @param vo
//	 * @return ConvertToCtrlSchemeVO
//	 */
//	public static ConvertToCtrlSchemeVO convertNtbParamVOToFuncExps(NtbParamVO vo) {
//		ConvertToCtrlSchemeVO convertor = new ConvertToCtrlSchemeVO();
//		convertor.setMethodFunc(vo.getMethodCode());
//		return null;
////		convertor.set
//	}
}
