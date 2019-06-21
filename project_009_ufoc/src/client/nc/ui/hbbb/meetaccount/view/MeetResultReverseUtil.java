package nc.ui.hbbb.meetaccount.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bd.accperiod.AccperiodParamAccessor;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.funcnode.ui.FuncletInitData;
import nc.itf.hbbb.dxfunction.IDxModelFunction;
import nc.itf.hbbb.dxrelation.IDXRelationQrySrv;
import nc.itf.hbbb.dxrelation.IDxFunctionConst;
import nc.itf.hbbb.hbrepstru.HBRepStruUtil;
import nc.itf.uap.IUAPQueryBS;
import nc.jdbc.framework.SQLParameter;
import nc.pub.iufo.cache.CacheManager;
import nc.pub.iufo.cache.KeywordCache;
import nc.pub.iufo.cache.ReportCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.exception.UFOSrvException;
import nc.ui.hbbb.utils.HBRepStruUIUtil;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.ui.pub.linkoperate.ILinkType;
import nc.ui.uap.sf.SFClientUtil2;
import nc.ui.uif2.ShowStatusBarMsgUtil;
import nc.util.hbbb.HBAloneIDUtil;
import nc.util.hbbb.HBKeyGroupUtil;
import nc.util.hbbb.HBVersionUtil;
import nc.util.hbbb.MeasureDataProxy;
import nc.util.hbbb.OffsetHanlder;
import nc.util.hbbb.dxrelation.formula.DXFmlParseUtil;
import nc.util.hbbb.dxrelation.formula.DXFormulaDriver;
import nc.util.hbbb.hbscheme.HBSchemeSrvUtils;
import nc.util.hbbb.pub.HBPubItfService;
import nc.util.hbbb.service.HBBaseDocItfService;
import nc.util.hbbb.workdraft.pub.ReportType;
import nc.utils.iufo.FmlParseUtil;
import nc.vo.gl.contrast.result.SumContrastQryVO;
import nc.vo.gl.contrast.rule.ContrastRuleVO;
import nc.vo.glcom.tools.GLContrastProxy;
import nc.vo.hbbb.adjustscheme.AdjustSchemeVO;
import nc.vo.hbbb.contrast.ContrastQryVO;
import nc.vo.hbbb.contrast.IContrastConst;
import nc.vo.hbbb.dxfunction.project.MeasureReportVO;
import nc.vo.hbbb.dxrelation.DXRelationBodyVO;
import nc.vo.hbbb.dxrelation.DXRelationHeadVO;
import nc.vo.hbbb.dxrelation.IDXRelaConst;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.meetaccount.AggMeetRltHeadVO;
import nc.vo.hbbb.meetaccount.MeetResultHeadVO;
import nc.vo.hbbb.meetaccount.RelaFormulaObj;
import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;
import nc.vo.ufoc.adjreport.AdjReportVO;
import nc.vo.uif2.LoginContext;
import nc.vo.vorg.ReportCombineStruMemberVersionVO;

import com.ufsoft.script.UfoFormulaProxy;
import com.ufsoft.script.base.UfoEElement;
import com.ufsoft.script.base.UfoVal;
import com.ufsoft.script.exception.CmdException;
import com.ufsoft.script.exception.CreateProxyException;
import com.ufsoft.script.exception.ParseException;
import com.ufsoft.script.expression.IntOperand;
import com.ufsoft.script.expression.UfoExpr;
import com.ufsoft.script.extfunc.MeasFuncDriver;
import com.ufsoft.script.function.ExtFunc;
import com.ufsoft.script.function.UfoFunc;
import com.ufsoft.script.spreadsheet.UfoCalcEnv;

/**
 *
 * <p>
 * 对账结果反解析工具
 * </p>
 *
 * 修改记录：<br>
 * <li>修改人：修改日期：修改内容：</li>
 * <br><br>
 *
 * @see
 * @author wangxwb
 * @version V6.0
 * @since V6.0 创建时间：2011-6-9 上午08:57:35
 */
public class MeetResultReverseUtil {

	private static final String MEETRESULT_NODE="20022022";

	/**
	 *
	 * 根据对账结果反解析该对账结果所对应公式
	 * <p>修改记录：</p>
	 * @param valueVO
	 * @return
	 * @see
	 * @since V6.0
	 */
	public static List<String> getFormulaByMeetResult(MeetResultHeadVO valueVO ){
		String pk_dxrelation = valueVO.getPk_dxrelation();
		String pk_measure = valueVO.getPk_measure();//合并项目
		List<String> formulas = new ArrayList<String>();
		try {
			DXRelationBodyVO[] queryDXFormulas = NCLocator.getInstance().lookup(IDXRelationQrySrv.class).queryDXFormulas(pk_dxrelation);
			for (int i = 0; i < queryDXFormulas.length; i++) {
				DXRelationBodyVO dxRelationBodyVO = queryDXFormulas[i];
					if(pk_measure.equals(dxRelationBodyVO.getPk_measure())){
						String formula = dxRelationBodyVO.getFormula();
						if( formula != null)
							formulas.add(formula);
					}
//				}
			}
		} catch (UFOSrvException e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
		}
		return formulas;
	}

    /**
     * 根据对账结果泛解析每个公式所对应的报表以及指标信息
     * <p>
     * 修改记录：
     * </p>
     *
     * @param valueVO
     * @param pk_self
     * @param pk_orther
     * @param pk_org
     * @param pk_group
     * @return
     * @see
     * @since V6.0
     */
    @SuppressWarnings("rawtypes")
    public static RelaFormulaObj[] getRelaFormulaInfoByMeetResult(MeetResultHeadVO valueVO, String pk_self,
            String pk_other_org, String pk_org, String pk_group) {
        String pk_dxrelation = valueVO.getPk_dxrelation();
        String pk_measure = valueVO.getPk_measure();// 合并项目
        String pk_hbscheme = valueVO.getPk_hbscheme();// 合并方案
        Map<String, String> keymap = new HashMap<String, String>();

        MeasurePubDataVO pubdata = null;
        try {
            pubdata = MeasurePubDataBO_Client.findByAloneID(valueVO.getPk_keygroup(), valueVO.getAlone_id());
        } catch (Exception e) {
            Logger.error("查询pubdata出错", e);
        }
        if (pubdata != null) {
            KeyGroupVO keyGroup = pubdata.getKeyGroup();
            KeyVO[] keys = keyGroup.getKeys();
            for (KeyVO key : keys) {
                keymap.put(key.getPk_keyword(), pubdata.getKeywordByPK(key.getPk_keyword()));
            }
        }

//        String formula = "";
        List<String> formulas = new ArrayList<String>();
        ArrayList<RelaFormulaObj> relainfoCol = new ArrayList<RelaFormulaObj>();
        try {
            DXRelationBodyVO[] queryDXFormulas = NCLocator.getInstance().lookup(IDXRelationQrySrv.class)
                    .queryDXFormulas(pk_dxrelation);
            for (int i = 0; i < queryDXFormulas.length; i++) {
                DXRelationBodyVO dxRelationBodyVO = queryDXFormulas[i];
                if (pk_measure.equals(dxRelationBodyVO.getPk_measure())) {
                	formulas.add(dxRelationBodyVO.getFormula());
//                    break;
                }
            }
        } catch (UFOSrvException e) {
            nc.bs.logging.Logger.error(e.getMessage(), e);
        }

        UfoCalcEnv env = new UfoCalcEnv(null, null, false, null);
        KeywordCache keyCache = UFOCacheManager.getSingleton().getKeywordCache();

        java.util.Vector<KeyVO> keyVector = keyCache.getAllKeys();
        env.setKeys(keyVector.toArray(new KeyVO[0]));
        try {
            UfoFormulaProxy parser = new UfoFormulaProxy(env);

            env.loadFuncListInst().registerExtFuncs(new DXFormulaDriver(env));
            env.loadFuncListInst().registerExtFuncs(new MeasFuncDriver(env));
            // 设置其他环境信息
            ContrastQryVO qryvo = new ContrastQryVO();
            HBSchemeVO hbschemeVO = null;
            try {
                hbschemeVO = (HBSchemeVO) NCLocator.getInstance().lookup(IUAPQueryBS.class)
                        .retrieveByPK(HBSchemeVO.class, pk_hbscheme);
                qryvo.setSchemevo(hbschemeVO);// 设置合并方案
            } catch (BusinessException e2) {
                Logger.error(e2.getMessage(), e2);
            }
            qryvo.setKeymap(keymap);
            String pk_hbrepstru = HBRepStruUIUtil.getHBRepStruPK(keymap, hbschemeVO);
            qryvo.setPk_hbrepstru(pk_hbrepstru);
            env.setExEnv(IContrastConst.CONTRASTQRYVO, qryvo);
            env.setExEnv(IContrastConst.PK_SELFCORP, pk_self);
            env.setExEnv(IContrastConst.PK_OPPCORP, pk_other_org);
            env.setExEnv(IContrastConst.PK_HBSCHEME, pk_hbscheme);
            env.setExEnv(IContrastConst.HBSCHEMEVO, hbschemeVO);

            for(String formula: formulas){
            	UfoExpr expr = parser.parseExpr(formula);
                UfoEElement[] elements = expr.getElements();
                
                //处理IF函数的解析，目前只支持一层IF，暂不支持嵌套，且IF的条件语句里面不能含有INTR、SREP等需要展示的项目
                //可解析类似这种形式：IF(K('月')=4,SREP('盈余公积/4101',1)*IPROPORTION(),SREP('盈余公积/4101',1)*7)
                if(((UfoFunc)elements[0].getObj()).getFuncName()
                		.toUpperCase().equals("IF")) {
                	List<UfoEElement> elementList = new ArrayList<UfoEElement>();
                	List params = ((UfoFunc)elements[0].getObj()).getParams();
                	for(int i=1; i<params.size(); i++) {
                		UfoEElement[] es = ((UfoExpr)params.get(i)).getElements();
                    	elementList.addAll(Arrays.asList(es));
                	}
                	elements = elementList.toArray(new UfoEElement[elementList.size()]);
                }
                
                // 解析得到每个公式
                ExtFunc[] tmpFormulas = new ExtFunc[elements.length];
                String[] projectCodes = new String[elements.length];
                Map<String, Boolean> proInstradeMap = new HashMap<String, Boolean>();
                UfoExpr[] ufoExprs = new UfoExpr[elements.length];
                int[] offSetValue = new int[elements.length];//偏移量表达式
                String[]  otherDynKeyToValPK = null;
                for (int i = 0; i < elements.length; i++) {
                    // 有类型为short的情况,目前直接忽略
                    if (!(elements[i].getObj() instanceof ExtFunc)) {
                        continue;
                    }
         
                    
                    ExtFunc tmpformula = (ExtFunc) elements[i].getObj();
                    tmpFormulas[i] = tmpformula;
                    
                    //key函数不支持联查
                    if(tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.KEYFUNC)){
                    	continue;
                    }
                    
                    boolean isinstrade = false;
                    if (null != tmpformula.getFuncName() && tmpformula.getFuncName().trim().length() > 0) {
                        if (tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.INTR)
                                || tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.DPSUM)||tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.INTRBYC)||tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.INTRBYKEY)) {
                            isinstrade = true;
                        }
                    }

                    List params = tmpformula.getParams();
                    String projectcode = String.valueOf(params.get(0));
                    if (StringUtil.isEmptyWithTrim(projectcode)) {
                        continue;
                    }
                    String[] splitprojectcode = projectcode.split("/");
                    if(splitprojectcode[1].endsWith("'")){
                    	 projectCodes[i] = splitprojectcode[1].replaceAll("'", "");
                    }else{
                    	 projectCodes[i] = splitprojectcode[1];
                    }
                   
                    proInstradeMap.put(projectCodes[i], isinstrade);

                    ufoExprs[i] = (UfoExpr) params.get(1);
                    if(params.size() >=3 && params.get(2) != null){
                        UfoExpr offExpr = (UfoExpr) params.get(2);
                    	offSetValue[i] = getOffSetValue(offExpr,env);
                    }
                    
                    if(tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.INTRBYC)){
                    	String keyword = String.valueOf(params.get(params.size()-1));
                    	keyword = keyword.replaceAll("\'", "");
            			String[] otherDynKeyToVal = keyword.split("=");
            			KeyVO keyvo = UFOCacheManager.getSingleton().getKeywordCache().getByName(otherDynKeyToVal[0]);
            			otherDynKeyToValPK = new String[2];
            			otherDynKeyToValPK[0] = keyvo.getPk_keyword();
            			otherDynKeyToValPK[1] =	HBPubItfService.getRemoteDxModelFunction().queryPKChooseKeyBYCode(keyvo,otherDynKeyToVal[1]);	
                    }
                }

                IDxModelFunction dxModelFunction = NCLocator.getInstance().lookup(IDxModelFunction.class);
                Map<String, MeasureReportVO> measureReportVOs = dxModelFunction.getMeasRepsBySchemeProjCode(pk_hbscheme,
                        pk_group, proInstradeMap);

                AdjustSchemeVO adjustSchemeVo = null;
                if(hbschemeVO.getPk_adjustscheme() != null)
                	adjustSchemeVo = (AdjustSchemeVO) HBBaseDocItfService.getRemoteUAPQueryBS().retrieveByPK(AdjustSchemeVO.class, hbschemeVO.getPk_adjustscheme());

                for (int i = 0; i < elements.length; i++) {
                    if (tmpFormulas[i] == null || projectCodes[i] == null) {
                        continue;
                    }
                    ExtFunc tmpformula = tmpFormulas[i];
                    String projectcode = projectCodes[i];

                    String pk_org1 = null;
                    String pk_other_org1 = null;
                    if (tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.SREP)
                            || tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.INTR)
                            || tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.INTRBYKEY)
                            || tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.TPSUM)
                            || tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.PTPSUM)
                            || tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.DPSUM)||tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.INTRBYC)) {
                        UfoExpr object = ufoExprs[i];
                        IntOperand intop = (IntOperand) object.getElements()[0].getObj();
                        double num = intop.getNum();
                        if (num == 0) {
                            pk_org1 = pk_self;
                            pk_other_org1 = pk_other_org;
                        } else {
                            pk_org1 = pk_other_org;
                            pk_other_org1 = pk_self;
                        }
                    }

                    RelaFormulaObj realinfoVo = new RelaFormulaObj();
                    realinfoVo.setPk_selef(pk_org1);// 对应报表的单位
                    // 对应采集表对方单位,此处的本对方单位与对账结果的本对方可能不一致,因为这里的本对方单位是根据模板公式取数得来
                    realinfoVo.setPk_countorg(pk_other_org1);
                    try {
                    	//jiaa 存在偏移量的时候
                    	Map<String, String> relaKeyMap = keymap;
						if(offSetValue[i] != 0){
							//重置keyMap，取数期间不是当前对账或成本法转权益法的期间
							relaKeyMap = OffsetHanlder.handOffset(hbschemeVO,keymap,offSetValue[i]);
						}
    					realinfoVo.setKeyMap(relaKeyMap);
                    	
                        MeasureReportVO measureVO = measureReportVOs.get(projectcode);
                        if (measureVO == null) {
                            throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0",
                                    "01830001-0342")/* @res "未找到对应合并项目!" */);
                        }
                        realinfoVo.setItemname(measureVO.getMeasVO().getName());
                        realinfoVo.setReportvo(measureVO);
                        realinfoVo.setEnv(env);
                        ReportVO byPK = ((ReportCache) CacheManager.getCache(ReportVO.class)).getByPK(measureVO
                                .getPk_report());
                        realinfoVo.setDataresurce(byPK.getName());
                        String alone_id = "";

                        String adjaloneID = "";
                        boolean ishb = false;
                        boolean isExistAdjrep = false;// 是否存在调整表,如果存在调整表则是直接从调整表取数
                        if (tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.TPSUM)
                                || tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.SREP)
                                || tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.PTPSUM)) {
                            ReportCombineStruMemberVersionVO[] vos = HBBaseDocItfService.getRemoteHBRepStru()
                                    .queryReportCombineStruMemberByVersionId(pk_hbrepstru, pk_org1);
                           
                            if (vos.length > 1 && !(pk_org1.equals(pk_org))) {
                                // 检查是否存在合并调整表,如果存在,则直连合并报表调整表
                            	if(adjustSchemeVo != null)
                            		adjaloneID = HBAloneIDUtil.getAloneID(pk_org1, relaKeyMap,measureVO.getMeasVO().getKeyCombPK(),
                                        HBVersionUtil.getHBAdjustByHBSchemeVO(hbschemeVO));
                                alone_id = HBAloneIDUtil.getAloneID(pk_org1, relaKeyMap, measureVO.getMeasVO().getKeyCombPK(),
                                        hbschemeVO.getVersion());
                                ishb = true;
                            } else {
                            	if(adjustSchemeVo != null)
                            		adjaloneID = HBAloneIDUtil.getAloneID(pk_org1, relaKeyMap,measureVO.getMeasVO().getKeyCombPK(), adjustSchemeVo.getVersion());
                                alone_id = HBAloneIDUtil.getAloneID(pk_org1, measureVO.getMeasVO().getKeyCombPK(), relaKeyMap);
                            }
                            
                            if(adjustSchemeVo == null){
                            	isExistAdjrep = false;
                            }else{
                            	// 检查是否存在调整表
                                AdjReportVO adjrepvo = new AdjReportVO();
                                adjrepvo.setPk_report(measureVO.getPk_report());
                                adjrepvo.setAloneid(adjaloneID);
                                adjrepvo.setPk_hbscheme(hbschemeVO.getPk_hbscheme());
                                adjrepvo.setPk_adjscheme(hbschemeVO.getPk_adjustscheme());
                                adjrepvo.setPk_keygroup(hbschemeVO.getPk_keygroup());
                                isExistAdjrep = HBPubItfService.getRemoteAdjReport().existAdjReport(adjrepvo);
                            }
                            
                        } else if (tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.DPSUM)
                                || tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.INTR)) {
//                            ProjectVO provo = getProjectVOByCode(projectcode);
//                            if (null != provo.getIsreportorg() && provo.getIsreportorg().booleanValue()) {// 是勾上了
//                                
//                            }
                            // 检查对方单位是不是虚组织
                            ReportCombineStruMemberVersionVO repmembervo = getRepManStrMemberVO(pk_other_org,
                                    pk_hbrepstru);
                            if (null != repmembervo.getIsmanageorg() && repmembervo.getIsmanageorg().booleanValue()) {
                                pk_other_org1 = repmembervo.getPk_entityorg(); // 将其替换为实体组织去取数据
                            }

                            ///解决内部客商联查内部客商信息add by jiaah
                            KeyGroupVO subKeyGroupVO = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(measureVO.getMeasVO().getKeyCombPK());
                			//动态区关键字pk 目前支持一个动态区关键字
                			String pk_dynkeyword = HBKeyGroupUtil.getPk_dynKeyValue(subKeyGroupVO,hbschemeVO.getPk_keygroup());
                			if(!pk_dynkeyword.equals(KeyVO.DIC_CORP_PK)){
                				Map<String, String> c = HBRepStruUtil.getOrgSuppliesMap(new String[]{pk_other_org1});
                				String pk_suply = c.get(pk_other_org1);
                				if( pk_suply != null)
                					pk_other_org1 = pk_suply;
                			}
                			realinfoVo.setPk_countorg(pk_other_org1);
                            env.setExEnv(IContrastConst.PKDYNKEY, pk_dynkeyword);
    						alone_id = HBAloneIDUtil.getAloneID(pk_org1, measureVO.getMeasVO().getKeyCombPK(), relaKeyMap,pk_other_org1,pk_dynkeyword);
                        }else if(tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.INTRBYKEY)) {
//                            ProjectVO provo = getProjectVOByCode(projectcode);
//                            if (null != provo.getIsreportorg() && provo.getIsreportorg().booleanValue()) {// 是勾上了
//                                
//                            }
                            // 检查对方单位是不是虚组织
                            ReportCombineStruMemberVersionVO repmembervo = getRepManStrMemberVO(pk_other_org,
                                    pk_hbrepstru);
                            if (null != repmembervo.getIsmanageorg() && repmembervo.getIsmanageorg().booleanValue()) {
                                pk_other_org1 = repmembervo.getPk_entityorg(); // 将其替换为实体组织去取数据
                            }

                            ///解决内部客商联查内部客商信息add by jiaah
                            KeyGroupVO subKeyGroupVO = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(measureVO.getMeasVO().getKeyCombPK());
                			//动态区关键字pk 目前支持一个动态区关键字
//                			String pk_dynkeyword = HBKeyGroupUtil.getPk_dynKeyValue(subKeyGroupVO,hbschemeVO.getPk_keygroup());
                			
                		
                			realinfoVo.setPk_countorg(pk_other_org1);
                          
                            
                            //chacun
                            
                			String result="";
                			MeasurePubDataVO s_pubdata  = new MeasurePubDataVO();
                	
                			s_pubdata.setKType( measureVO.getMeasVO().getKeyCombPK());
                			KeyGroupVO keygroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK( measureVO.getMeasVO().getKeyCombPK());
                			s_pubdata.setKeyGroup(keygroupVo);
                			KeyVO[] keyvos=keygroupVo.getKeys();
                			if(null!=keyvos && null!=relaKeyMap /*&& keyvos.length==keyMap.size()*/){
                				String[] keys=new String[relaKeyMap.size()];
                				relaKeyMap.keySet().toArray(keys);
                				if(null!=keys && keys.length>0){
                					for(String key:keys){
                						s_pubdata.setKeywordByPK(key, relaKeyMap.get(key));
                					}
                				}
                				s_pubdata.setKeywordByPK(KeyVO.CORP_PK,pk_org1);
                				
                				String[] pk_dynkeywords  = HBKeyGroupUtil.getPk_dynKeyValues(subKeyGroupVO,hbschemeVO.getPk_keygroup());
                        		if(pk_dynkeywords.length==2){
                        			int corp =0;
                        			int other =1;
                        			if(!pk_dynkeywords[0].equals(KeyVO.DIC_CORP_PK)){
                        				
                        				 corp =1;
                            			 other =0; 
                        			} 
                    				
//                        			Map<String, String> c = HBRepStruUtil.getOrgSuppliesMap(new String[]{pk_other_org1});
//                    				String pk_suply = c.get(pk_other_org1);
//                    				if( pk_suply != null)
//                    					pk_other_org1 = pk_suply;
                    				  env.setExEnv(IContrastConst.PKDYNKEY, pk_dynkeywords[corp]);
                    				 
                    				  s_pubdata.setKeywordByPK(pk_dynkeywords[corp],pk_other_org1);
                    				  s_pubdata.setKeywordByPK(pk_dynkeywords[other],valueVO.getNoteshow());
//                    				  s_pubdata.setKeywordByPK(otherDynKeyToValPK[0],otherDynKeyToValPK[1]);
                    				  Map<String,String> queryMap = new HashMap<String, String>();
                    				  queryMap.put(pk_dynkeywords[corp], pk_other_org1);
                    				  queryMap.put(pk_dynkeywords[other], valueVO.getNoteshow());
                    				  env.setExEnv("otherQueryDim", queryMap);
                        		}
                    			
                        		s_pubdata.setVer(0);
                        	
                			    try {
                					result=MeasurePubDataBO_Client.getAloneID(s_pubdata);
                				
                				} catch (Exception e) {
                					// TODO Auto-generated catch block
                					nc.bs.logging.Logger.error(e.getMessage(), e);
                					throw new BusinessException(e);
                				}
                			}
                			
                			alone_id = result;
                            
    						//alone_id = HBAloneIDUtil.getAloneID(pk_org1, measureVO.getMeasVO().getKeyCombPK(), relaKeyMap,pk_other_org1,"004");
                        }else if(tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.INTRBYC)){
                        	
                        	
                            // 检查对方单位是不是虚组织
                            ReportCombineStruMemberVersionVO repmembervo = getRepManStrMemberVO(pk_other_org,
                                    pk_hbrepstru);
                            if (null != repmembervo.getIsmanageorg() && repmembervo.getIsmanageorg().booleanValue()) {
                                pk_other_org1 = repmembervo.getPk_entityorg(); // 将其替换为实体组织去取数据
                            }

                            ///解决内部客商联查内部客商信息add by jiaah
                            KeyGroupVO subKeyGroupVO = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(measureVO.getMeasVO().getKeyCombPK());
                			//动态区关键字pk 目前支持一个动态区关键字
//                			String pk_dynkeyword = HBKeyGroupUtil.getPk_dynKeyValue(subKeyGroupVO,hbschemeVO.getPk_keygroup());
                			
                		
                			realinfoVo.setPk_countorg(pk_other_org1);
                          
                            
                            //chacun
                            
                			String result="";
                			MeasurePubDataVO s_pubdata  = new MeasurePubDataVO();
                	
                			s_pubdata.setKType( measureVO.getMeasVO().getKeyCombPK());
                			KeyGroupVO keygroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK( measureVO.getMeasVO().getKeyCombPK());
                			s_pubdata.setKeyGroup(keygroupVo);
                			KeyVO[] keyvos=keygroupVo.getKeys();
                			if(null!=keyvos && null!=relaKeyMap /*&& keyvos.length==keyMap.size()*/){
                				String[] keys=new String[relaKeyMap.size()];
                				relaKeyMap.keySet().toArray(keys);
                				if(null!=keys && keys.length>0){
                					for(String key:keys){
                						s_pubdata.setKeywordByPK(key, relaKeyMap.get(key));
                					}
                				}
                				s_pubdata.setKeywordByPK(KeyVO.CORP_PK,pk_org1);
                				
                				String[] pk_dynkeywords  = HBKeyGroupUtil.getPk_dynKeyValues(subKeyGroupVO,hbschemeVO.getPk_keygroup());
                        		if(pk_dynkeywords.length==2){
                        			int corp =0;
                        			int other =1;
                        			if(!pk_dynkeywords[0].equals(KeyVO.DIC_CORP_PK)){
                        				
                        				 corp =1;
                            			 other =0; 
                        			} 
                    				
//                        			Map<String, String> c = HBRepStruUtil.getOrgSuppliesMap(new String[]{pk_other_org1});
//                    				String pk_suply = c.get(pk_other_org1);
//                    				if( pk_suply != null)
//                    					pk_other_org1 = pk_suply;
                    				  env.setExEnv(IContrastConst.PKDYNKEY, pk_dynkeywords[corp]);
                    				 
                    				  s_pubdata.setKeywordByPK(pk_dynkeywords[corp],pk_other_org1);
//                    				  s_pubdata.setKeywordByPK(pk_dynkeywords[other],pk_other_org1);
                    				  s_pubdata.setKeywordByPK(otherDynKeyToValPK[0],otherDynKeyToValPK[1]);
//                    				  s_pubdata.setKeywordByPK(pk_dynkeywords[other],valueVO.getNoteshow());
                    				  Map<String,String> queryMap = new HashMap<String, String>();
                    				  queryMap.put(pk_dynkeywords[corp], pk_other_org1);
                    				  queryMap.put(otherDynKeyToValPK[0],otherDynKeyToValPK[1]);
                    				  
                    				  env.setExEnv("otherQueryDim", queryMap);
                        		}
                    			
                        		s_pubdata.setVer(0);
                        	
                			    try {
                					result=MeasurePubDataBO_Client.getAloneID(s_pubdata);
                				
                				} catch (Exception e) {
                					// TODO Auto-generated catch block
                					nc.bs.logging.Logger.error(e.getMessage(), e);
                					throw new BusinessException(e);
                				}
                			}
                			
                			alone_id = result;
                        	
                        }
                        // 目前直接取指标的值
                        if (tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.DPSUM)
                                || tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.INTR)|| tmpformula.getFuncName().toUpperCase().equals(IDxFunctionConst.INTRBYKEY)) {
                            // DPSUM和INTR直接从个别报表取数
                            UFDouble sepvalue = new UFDouble(getExpValue(measureVO, alone_id));
                            realinfoVo.setData(sepvalue);
                            realinfoVo.setAloneid(alone_id);
                            realinfoVo.setRelareporttype(ReportType.SEP);
                        } else {
                            UFDouble sepvalue = new UFDouble(getExpValue(measureVO, alone_id));
                            UFDouble adjvalue = new UFDouble(getExpValue(measureVO, adjaloneID));
                            if (!isExistAdjrep) {
                                realinfoVo.setData(sepvalue);
                                realinfoVo.setAloneid(alone_id);
                                if (ishb) {
                                    realinfoVo.setRelareporttype(ReportType.HB);
                                } else {
                                    realinfoVo.setRelareporttype(ReportType.SEP);
                                }
                            } else {
                                realinfoVo.setData(adjvalue);
                                realinfoVo.setAloneid(adjaloneID);
                                if (ishb) {
                                    realinfoVo.setRelareporttype(ReportType.HB_ADJ);
                                } else {
                                    realinfoVo.setRelareporttype(ReportType.SEP_ADJ);
                                }
                            }
                        }
                        relainfoCol.add(realinfoVo);
                    } catch (BusinessException e) {
                        Logger.error(e.getMessage(), e);
                    }
                }
            }

        } catch (CreateProxyException e) {
            Logger.error(e.getMessage(), e);
        } catch (ParseException e) {
            Logger.error(e.getMessage(), e);
        } catch (BusinessException e) {
            Logger.error(e.getMessage(), e);
        } catch (CmdException e) {
            Logger.error(e.getMessage(), e);
		}
        return relainfoCol.toArray(new RelaFormulaObj[0]);
    }
    
	/**
	 * 返回表达式所对应的偏移量的int值，有对-zmonth的情况的处理
	 * @create by jiaah at 2013-7-18,下午7:46:14
	 * @param offExpr
	 * @param env
	 * @return
	 * @throws CmdException
	 */
	public static int getOffSetValue(UfoExpr offExpr,UfoCalcEnv env) throws CmdException{
		if (offExpr.getElements() != null && offExpr.getElements().length > 0) {
			// 偏移量认为是-zmonth()
			Object obj = offExpr.getElements()[0].getObj();
			if (obj instanceof ExtFunc && ((ExtFunc) obj).getFuncName().toUpperCase().equals(IDxFunctionConst.ZMONTH)) {
				UfoVal[] vals = ((ExtFunc) obj).getValue(env);
				if(offExpr.toString().startsWith("-")){
					return 0 - Integer.valueOf(vals[0].toString());
				}else
					return Integer.valueOf(vals[0].toString());
			}else {
				return Integer.valueOf(offExpr.toString());
			}
		} 
		return 0;
	}

    private static double getExpValue(MeasureReportVO measureVO, String aloneid) {
        MeasureDataVO[] datavos = null;
        try {
            datavos = MeasureDataProxy.getRepData(aloneid, new MeasureVO[] { measureVO.getMeasVO() });
        } catch (BusinessException e) {
            Logger.error("查询出错", e);
        }
        if (null == datavos || datavos.length == 0) {
            return 0;
        }
        if (null != datavos[0]) {
            return datavos[0].getUFDoubleValue() == null ? 0.0 : datavos[0].getUFDoubleValue().doubleValue();
        } else {
            return 0;
        }
    }

	public static ReportCombineStruMemberVersionVO getRepManStrMemberVO(String pk_org,
			String pk_rms) throws BusinessException {
		ReportCombineStruMemberVersionVO result = null;
		StringBuilder content = new StringBuilder();
		content.append(" pk_svid=?  and pk_org=? and isnull(dr,0)=0 ");
		SQLParameter params = new SQLParameter();
		params.addParam(pk_rms);
		params.addParam(pk_org);
		@SuppressWarnings("unchecked")
		Collection<ReportCombineStruMemberVersionVO> list = NCLocator.getInstance().lookup(IUAPQueryBS.class).retrieveByClause(
				ReportCombineStruMemberVersionVO.class, content.toString(), null, params);
		if (null == list || list.size() == 0) {
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0451")/*@res "查询报表组织的时候，未能取得报表组织VO"*/);
		}
		result = list.toArray(new ReportCombineStruMemberVersionVO[0])[0];
		return result;
	}

//    private static ProjectVO getProjectVOByCode(String projectcode) throws BusinessException {
//        ProjectVO result = null;
//
//        StringBuilder content = new StringBuilder();
//        content.append(ProjectVO.CODE).append("=? ");
//        SQLParameter params = new SQLParameter();
//        params.addParam(projectcode);
//        @SuppressWarnings("unchecked")
//        Collection<ProjectVO> list = NCLocator.getInstance().lookup(IUAPQueryBS.class)
//                .retrieveByClause(ProjectVO.class, content.toString(), null, params);
//        result = list.toArray(new ProjectVO[0])[0];
//
//        return result;
//    }

	public static void linkQuery(MeetResultHeadVO valueVO, Object[] datas, MeetResultRelaDialog reladialog, LoginContext context) {
		String pk_self = null;
		String pk_orther = null;
		String pk_org =null;
		for (int i = 0; i < datas.length; i++) {
			MeetResultHeadVO object = null;
			if(datas[i] instanceof AggMeetRltHeadVO) {
				object = (MeetResultHeadVO) ((AggMeetRltHeadVO) datas[i]).getParentVO();
			}
			else {
				object = (MeetResultHeadVO) datas[i];
			}
			if(object.getPk_totalinfo()!=null && object.getPk_selforg()!=null && object.getPk_countorg()!=null &&
					object.getPk_totalinfo().equals(valueVO.getPk_totalinfo()
					)){
				pk_self = object.getPk_selforg();
				pk_orther = object.getPk_countorg();
				pk_org = object.getPk_meetorg();
				break;
			}
		}
		List<String> formulaByMeetResult = MeetResultReverseUtil.getFormulaByMeetResult(valueVO);
		String pk_measure = valueVO.getPk_measure();//合并项目
		String pk_hbscheme = valueVO.getPk_hbscheme();//合并方案
		if(pk_measure==null || pk_hbscheme==null) return;

		DXRelationHeadVO dxheadVO = null;
		HBSchemeVO hbschemevo =null;
		try {
			hbschemevo = HBSchemeSrvUtils.getHBSchemeByHBSchemeId(pk_hbscheme);
			dxheadVO = (DXRelationHeadVO) NCLocator.getInstance().lookup(IUAPQueryBS.class).retrieveByPK(DXRelationHeadVO.class, valueVO.getPk_dxrelation());
		} catch (UFOSrvException e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
		} catch (BusinessException e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
		}
		if(hbschemevo == null || dxheadVO == null) {
			return;
		}

		if(IDXRelaConst.DIRECT_UAP_RULE.equals(dxheadVO.getType()) || IDXRelaConst.INDIRECT_UAP_RULE.equals(dxheadVO.getType())){
			//UCHECK函数联查
			relaUcheckFunc(valueVO, hbschemevo, context, dxheadVO);
		}else{
			RelaFormulaObj[] relaFormulaInfoByMeetResult = MeetResultReverseUtil.getRelaFormulaInfoByMeetResult(valueVO, pk_self, pk_orther, pk_org, WorkbenchEnvironment.getInstance().getGroupVO().getPk_group());
			StringBuilder formulas = new StringBuilder();
			if(formulaByMeetResult.size() > 1){
				formulas.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830006-0123")/*@res "该合并报表项目对应"*/);
				formulas.append(formulaByMeetResult.size());
				formulas.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830006-0124")/*@res "条取数公式:"*/);
			}
			for(String s : formulaByMeetResult){
				formulas.append(convertCheckFml(s, false));
			}

			reladialog.getCardPanel().getHeadItem("dxrelafomula").setValue(formulas.toString());
			reladialog.setValue(relaFormulaInfoByMeetResult);
			reladialog.getCardPanel().getBillModel().loadLoadRelationItemValue();
			reladialog.showModal();
		}
	}

	private static String convertCheckFml(String formula, boolean userDefine){
		UfoCalcEnv env = new UfoCalcEnv(null,null,false,null);
		String fmlContent = null;
		try {
			fmlContent = FmlParseUtil.parseFormula(formula, userDefine,DXFmlParseUtil.getConRunEnv(),env);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
		}
		return fmlContent;
	}

	private static void relaUcheckFunc(MeetResultHeadVO valueVO, HBSchemeVO hbschemevo, LoginContext context, DXRelationHeadVO dxheadVO) {
		try {
			ContrastRuleVO conRuleVo = GLContrastProxy.getRemoteContrastRule().findByPrimaryKey(dxheadVO.getPk_contrastrule());
			MeasurePubDataVO pubdata = null;
			try {
				pubdata = MeasurePubDataBO_Client.findByAloneID(valueVO.getPk_keygroup(), valueVO.getAlone_id());
			} catch (Exception e) {
				 nc.bs.logging.Logger.error(e.getMessage(), e);
			}
			if(pubdata == null)
				return;

			KeyGroupVO keyGroup = pubdata.getKeyGroup();
			KeyVO[] keys = keyGroup.getKeys();
			KeyVO timeKey = null;
			//当前对账期间的对应的自然日期
			String date = null;
			for (KeyVO key : keys) {
				if(key.isTTimeKeyVO()){
					timeKey = key;
					date = pubdata.getKeywordByPK(key.getPk_keyword());
				}
			}
			if(timeKey == null || date == null){
				return;
			}
			//目前只支持会计月和月
			if(!timeKey.getPk_keyword().equals(KeyVO.ACC_MONTH_PK) && !timeKey.getPk_keyword().equals(KeyVO.MONTH_PK)){
				ShowStatusBarMsgUtil.showErrorMsg(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830006-0003")/*@res "提示"*/, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830006-0125")/*@res "目前总账模板只支持会计月和月关键字"*/, context);
				return;
			}

//			if(timeKey.getPk_keyword().equals(KeyVO.ACC_MONTH_PK)){
//				String pk_accperiodscheme = hbschemevo.getPk_accperiodscheme();
//				String[] beginAndEnd = AccPeriodSchemeUtil.getInstance().getNatDateByAccPeriod(pk_accperiodscheme, KeyVO.ACC_MONTH_PK, date);
//				if(beginAndEnd != null)
//					date = beginAndEnd[1];
//			}

			final String pk_contrast = conRuleVo.getPk_contrastrule();
			String year = date.substring(0, 4);
			String m = date.substring(5, 7);
			SumContrastQryVO qryVO = new SumContrastQryVO();
			qryVO.setPk_contrastrule(new String[]{pk_contrast});
			qryVO.setYear(year);
			qryVO.setAccperiod(m);
			qryVO.setReportstatus(new String[]{"1"});
			qryVO.setPk_accperiodscheme(hbschemevo.getPk_accperiodscheme() == null ? 
					AccperiodParamAccessor.getInstance().getDefaultSchemePk() : hbschemevo.getPk_accperiodscheme());
			FuncletInitData initData = new FuncletInitData(ILinkType.LINK_TYPE_QUERY, qryVO);
			SFClientUtil2.openFuncNodeDialog(context.getEntranceUI(), MEETRESULT_NODE,  initData, null, true, false);
		} catch (BusinessException e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
		}
	}

//	private static void relaUcheckFunc(MeetResultHeadVO valueVO, String pk_measure, HBSchemeVO hbschemevo, LoginContext context, DXRelationHeadVO dxheadVO) {
//		//
//		try {
////			DXRelationBodyVO[] queryDXFormulas = NCLocator.getInstance().lookup(IDXRelationQrySrv.class).queryDXFormulas(valueVO.getPk_dxrelation());
////			DXRelationBodyVO currentBodyvo =null;
////			String pk_keygroup = hbschemevo.getPk_keygroup();
////			KeyGroupVO[] loadKeyGroupsByIds =null ;
////			try {
////				loadKeyGroupsByIds = KeyGroupBO_Client.loadKeyGroupsByIds(new String[]{pk_keygroup});
////			} catch (Exception e2) {
////				e2.printStackTrace();
////			}
////			KeyVO[] keys = loadKeyGroupsByIds[0].getKeys();
//			Map<String, String> keymap = new HashMap<String,String>();
//
////			for (int j = 0; j < keys.length; j++) {
//////					KeyVO keyVO = keys[j];
////				//这个地方存的是40位的,前20位是关键字PK,后20位是关键字值
////				String attributeValue = (String) valueVO.getAttributeValue("key"+(j+1));
////				keymap.put(attributeValue.substring(0, 20), (String)attributeValue.substring(20));
////			}
//
//			MeasurePubDataVO pubdata = null;
//			try {
//				pubdata = MeasurePubDataBO_Client.findByAloneID(valueVO.getPk_keygroup(), valueVO.getAlone_id());
//			} catch (Exception e) {
//				Logger.error("查询pubdata出错", e);
//			}
//			if(pubdata != null) {
//				KeyGroupVO keyGroup = pubdata.getKeyGroup();
//				KeyVO[] keys = keyGroup.getKeys();
//				for (KeyVO key : keys) {
//					keymap.put(key.getPk_keyword(), pubdata.getKeywordByPK(key.getPk_keyword()));
//				}
//			}
//
////			for (int i = 0; i < queryDXFormulas.length; i++) {
////				DXRelationBodyVO dxRelationBodyVO = queryDXFormulas[i];
////				if(pk_measure.equals(dxRelationBodyVO.getPk_measure())){
////					currentBodyvo = dxRelationBodyVO;
////					break;
////				}
////			}
//			ContrastRuleVO conRuleVo = GLContrastProxy.getRemoteContrastRule().findByPrimaryKey(dxheadVO.getPk_contrastrule());
////			ContrastHBBBQryVO ucheckcontextvo=null;
////			if(null!=conRuleVo){
////
////				if(conRuleVo.getContrastmoney().toString().equals(ContrastReportStatusConst.BALANCE_PK)){
////					//设置余额取数类型,内部交易规则
////					ucheckcontextvo	=new ContrastHBBBQryVO(conRuleVo,null,HBUcheckUtil.getBalance( dxheadVO));
////				}else if (conRuleVo.getContrastmoney().toString().equals(ContrastReportStatusConst.OCCUR_PK)){
////					//设置发生取数类型,内部交易规则
////					ucheckcontextvo	=new ContrastHBBBQryVO(conRuleVo,HBUcheckUtil.getOccur( dxheadVO),null);
////				}
////
////				if(conRuleVo.getContrastmoney().toString().equals(ContrastReportStatusConst.BALANCE_PK)){
////					ucheckcontextvo	=new UCheckContextVO(conRuleVo,null,getBalance( dxheadVO));
////				}else if (conRuleVo.getContrastmoney().toString().equals(ContrastReportStatusConst.OCCUR_PK)){
////					ucheckcontextvo	=new UCheckContextVO(conRuleVo,getOccur( dxheadVO),null);
////				}
////			}
//			if("8".equals(dxheadVO.getType()) || "7".equals(dxheadVO.getType())){
//
//				String period = null;
//
//				String date = keymap.get(KeyVO.MONTH_PK);
//
//				if(date != null) {
//					String pk_accperiodscheme = hbschemevo.getPk_accperiodscheme();
//					AccountCalendar calendar=AccountCalendarUtils.getAccountCalendarByScheme(pk_accperiodscheme);
//					UFDate newDate = new UFDate(date);
//					//根据对账规则取得该日期所处期间
//					calendar.set(date.substring(0, 5));
//					AccperiodmonthVO[] monthVOsOfCurrentYear = calendar.getMonthVOsOfCurrentYear();
//					for (int i = 0; i < monthVOsOfCurrentYear.length; i++) {
//						AccperiodmonthVO accperiodmonthVO = monthVOsOfCurrentYear[i];
//						if(accperiodmonthVO.getBegindate().beforeDate(newDate)&&accperiodmonthVO.getEnddate().afterDate(newDate)){
//							period = accperiodmonthVO.getAccperiodmth();
//							break;
//						}
//					}
//				}
//				else {
//					period = keymap.get(KeyVO.ACC_MONTH_PK);
//				}
//				final String pk_contrast = conRuleVo.getPk_contrastrule();
//
//				String year = period.substring(0, 4);
//				String m = period.substring(5, 7);
//				SumContrastQryVO qryVO = new SumContrastQryVO();
//				qryVO.setPk_contrastrule(new String[]{pk_contrast});
//				qryVO.setYear(year);
//				qryVO.setAccperiod(m);
////				qryVO.setReportstatus(new String[]{"1", "2", "3", "4"});
//				qryVO.setReportstatus(new String[]{"1"});
//				FuncletInitData initData = new FuncletInitData(ILinkType.LINK_TYPE_QUERY, qryVO);
//
//				SFClientUtil2.openFuncNodeDialog(context.getEntranceUI(), MEETRESULT_NODE,  initData, null, true, false);
//
////				GlQueryLinkUFOVO gllinkQryVO = UCHECKRreverUtil.getLinkedQryVO(valueVO, hbschemevo, currentBodyvo, keymap, conRuleVo, ucheckcontextvo);
////	    		SFClientUtil.openLinkedQueryDialog(GlNodeConst.GLNODE_BALANCEBOOK, context.getEntranceUI(), gllinkQryVO);
////				FuncletWindowLauncher.openFuncNodeFrame(getModel().getContext().getEntranceUI(), funRegVo1);
////			}else if(){
//////				FuncletWindowLauncher.openFuncNodeFrame(getModel().getContext().getEntranceUI(), funRegVo2);
////				GlQueryLinkUFOVO gllinkQryVO = UCHECKRreverUtil.getLinkedQryVO(valueVO, hbschemevo, currentBodyvo, keymap, conRuleVo, ucheckcontextvo);
////				SFClientUtil.openLinkedQueryDialog(MEETRESULT_NODE, context.getEntranceUI(), gllinkQryVO);
//			}
//		} catch (BusinessException e) {
//			nc.bs.logging.Logger.error(e.getMessage(), e);
//		}
//	}
}