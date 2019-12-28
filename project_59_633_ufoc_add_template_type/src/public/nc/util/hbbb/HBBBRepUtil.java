package nc.util.hbbb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.framework.common.NCLocator;
import nc.itf.hbbb.func.FuncReTurnObj;
import nc.itf.uap.IUAPQueryBS;
import nc.util.hbbb.service.HBBaseDocItfService;
import nc.vo.hbbb.adjustscheme.AdjustSchemeVO;
import nc.vo.hbbb.func.HBBBFuncQryVO;
import nc.vo.hbbb.func.HBBBFuncUtil;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.iufo.data.MeasureDataUtil;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.pub.BusinessException;
import nc.vo.ufoc.unionproject.ProjectVO;

import com.ufsoft.iufo.fmtplugin.datastate.CellsModelOperator;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.fmtplugin.formatcore.UfoContextVO;
import com.ufsoft.iufo.fmtplugin.formula.FormulaModel;
import com.ufsoft.iufo.fmtplugin.formula.FormulaVO;
import com.ufsoft.iufo.fmtplugin.formula.UfoFmlExecutor;
import com.ufsoft.iufo.fmtplugin.service.HBRepCalcSrv;
import com.ufsoft.iufo.fmtplugin.service.ReportCalcSrv;
import com.ufsoft.report.ReportContextKey;
import com.ufsoft.script.base.CommonExprCalcEnv;
import com.ufsoft.table.CellPosition;
import com.ufsoft.table.CellsModel;
import com.ufsoft.table.IArea;

/**
 * 合并报表工具类
 * 
 * @author yp
 * 
 */
public class HBBBRepUtil implements IUfoContextKey {
    
	/**
	 * 合并抵销表
	 * 抵销借贷方、调整借贷方只计算区域公式
	 * @author jiaah
	 */
	public static FuncReTurnObj[] calcHBConvertFormulasWithOutMSelectFunc(HBBBFuncQryVO qryvo) throws Exception {
		try {
			MeasurePubDataVO pubdata = qryvo.getPubdata();
			HBSchemeVO hbSchemeVo = qryvo.getHbSchemeVo();
			pubdata.setAccSchemePK(hbSchemeVo.getPk_accperiodscheme());
			Boolean bAddLeft = qryvo.getbAddLeft();
//			MeasureVO[] measures = qryvo.getMeasures();
			String strUserID = qryvo.getStrUserID();
			String[] aryRepIDs = qryvo.getAryRepIDs();
			List<FuncReTurnObj> list = new ArrayList<FuncReTurnObj>();
			for (int i = 0; i < aryRepIDs.length; i++) {
				FuncReTurnObj result = new FuncReTurnObj();
				UfoContextVO contextVO = new UfoContextVO();

				contextVO.setAttribute(ReportContextKey.REPORT_NAME, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0116")/* @res "合并报表计算" */);// "合并报表计算"
				contextVO.setAttribute(ReportContextKey.REPORT_PK, aryRepIDs[i]);
				// contextVO.setAttribute(CREATE_UNIT_ID, strCreateUnitId);
				contextVO.setAttribute(CUR_REPORG_PK, pubdata.getUnitPK());
				contextVO.setAttribute(CUR_USER_ID, strUserID);
				contextVO.setAttribute(TYPE, UfoContextVO.REPORT_TABLE);
				contextVO.setAttribute(MEASURE_PUB_DATA_VO, pubdata);
				contextVO.setAttribute(KEYGROUP_PK, pubdata.getKType());
				// UI端计算
				contextVO.setAttribute(ON_SERVER, true);
				// 设置报表状态：格式态或数据态
				contextVO.setAttribute(ReportContextKey.OPERATION_STATE, ReportContextKey.OPERATION_INPUT);
				
				CellsModel model = qryvo.getFormatCellsModel();
				if(model == null)
					model = CellsModelOperator.getFormatModelByPK(contextVO);
				HBRepCalcSrv repCalcSrv = new HBRepCalcSrv(contextVO,model);
				if (bAddLeft.booleanValue() == true) {
					repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_ADDLEFT, CommonExprCalcEnv.EX_VALUE_ON);
					repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_CONVERTDIRECT, CommonExprCalcEnv.EX_VALUE_ON);
					repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_NOTCALCMSELEC, CommonExprCalcEnv.EX_VALUE_ON);
				} else {
					repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_NOTCALCMSELEC, CommonExprCalcEnv.EX_VALUE_OFF);
				}
				repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CHECKEXFUNC, CommonExprCalcEnv.EX_VALUE_OFF);
				// qryvo.setIsconvert(isconvert);
				qryvo.setPk_report(aryRepIDs[i]);
				qryvo.setSupMselect(false);
				// qryvo.setNeedreplaceAdd(needreplaceAdd);
				
				/*//报表指标中含有公式的指标不运行公式,折算不适用
*/				Map<IArea, FormulaVO> fmlMap = new HBBBFuncUtil(qryvo).get();
				/*if (!qryvo.isIsconvert()) {
					Map<String, ProjectVO> measureProjectMap = HBProxy.getRemoteHBProjectMapQry().loadMappingsByReportId(aryRepIDs[i]);
					Set<String> keySet = measureProjectMap.keySet();
					if(keySet.size()>0){
						Map<IArea, FormulaVO> tmplmap= new HashMap<IArea, FormulaVO>();
						Set<IArea> keySet2 = fmlMap.keySet();
						for (Iterator<IArea> iterator = keySet2.iterator(); iterator.hasNext();) {
							IArea iArea = (IArea) iterator.next();
							Map<CellPosition, MeasureVO> mainMeasureVOByArea = CellsModelOperator.getMeasureModel(repCalcSrv.getCellsModel()).getMainMeasureVOByArea(iArea);
							Collection<MeasureVO> values = mainMeasureVOByArea.values();
							boolean isHashFormula = false;
							for (Iterator<MeasureVO> iterator2 = values.iterator(); iterator2.hasNext();) {
								MeasureVO measureVO = (MeasureVO) iterator2.next();
								if(keySet.contains(measureVO.getCode())){
									isHashFormula = true;
									break;
								}
							}
							//如果不包含公式
							if(!isHashFormula){
								tmplmap.put(iArea, fmlMap.get(iArea));
							}
						}
						repCalcSrv.calcAllHBFormulas(tmplmap, null);
					}else{
						repCalcSrv.calcAllHBFormulas(fmlMap, null);
					}
				}else{
					repCalcSrv.calcAllHBFormulas(fmlMap, null);
				}*/
				repCalcSrv.calcAllHBFormulas(fmlMap, null,FormulaModel.TYPE_CONS_FML);

				// 保存报表数据
				CellsModelOperator.saveDataToDB(repCalcSrv.getCellsModel(), repCalcSrv.getContextVO());

				result.setCellmodel(repCalcSrv.getCellsModel());
				result.setAloneid(pubdata.getAloneID());
				result.setContextVO(contextVO);
				result.setPk_report(aryRepIDs[i]);
				list.add(result);
			}
			return list.toArray(new FuncReTurnObj[0]);
		} catch (Exception e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
			StringBuilder content = new StringBuilder();

			if (qryvo.isIsconvert()) {
				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0118")/* @res "计算表样上的折算公式" */);
			} else {
				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0119")/* @res "计算表样上的合并调整公式" */);
			}
			if (qryvo.isSupMselect()) {
				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0120")/* @res "中的MSELECT,MSELECTA公式出错！" */);
			} else {
				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0121")/* @res "中的非(MSELECT,MSELECTA)类的区域类公式出错！" */);
			}
			throw new BusinessException(content.toString());
		}

	}
	

	/**
	 * 运行折算公式 
	 * （主要是为了在折算中降低以下语句造成的重复sql过多的问题：
	 * adjustSchemeVo = (AdjustSchemeVO) NCLocator.getInstance().lookup(IUAPQueryBS.class).retrieveByPK(AdjustSchemeVO.class, hbSchemeVo.getPk_adjustscheme());）
	 * @create by fengzhy at 2012-5-14,上午10:12:32
	 *
	 * @param qryvo
	 * @return
	 * @throws Exception
	 */
	public static FuncReTurnObj[] calcZSFormulas(HBBBFuncQryVO qryvo, AdjustSchemeVO adjustSchemeVo) throws Exception {
		try {
			MeasurePubDataVO pubdata = qryvo.getPubdata();
			HBSchemeVO hbSchemeVo = qryvo.getHbSchemeVo();
			
			pubdata.setAccSchemePK(hbSchemeVo.getPk_accperiodscheme());

			Boolean bAddLeft = qryvo.getbAddLeft();
			String strUserID = qryvo.getStrUserID();
			String[] aryRepIDs = qryvo.getAryRepIDs();
			boolean isconvert = qryvo.isIsconvert();
			List<FuncReTurnObj> list = new ArrayList<FuncReTurnObj>();
			for (int i = 0; i < aryRepIDs.length; i++) {
				try{
					
					UfoContextVO contextVO = new UfoContextVO();
					FuncReTurnObj result = new FuncReTurnObj();
//					contextVO.setAttribute(ReportContextKey.REPORT_NAME, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0116")/* @res "合并报表计算" */);// "合并报表计算"
					contextVO.setAttribute(ReportContextKey.REPORT_PK, aryRepIDs[i]);
					contextVO.setAttribute(CUR_REPORG_PK, pubdata.getUnitPK());
					contextVO.setAttribute(CUR_USER_ID, strUserID);
					contextVO.setAttribute(TYPE, UfoContextVO.REPORT_TABLE);
					contextVO.setAttribute(MEASURE_PUB_DATA_VO, pubdata);
					contextVO.setAttribute(KEYGROUP_PK, pubdata.getKType());
					ConvertMselectVertionUtil measversion = new ConvertMselectVertionUtil(hbSchemeVo, adjustSchemeVo);

					measversion.setHbSchemeVo(hbSchemeVo);
					measversion.setPk_report(aryRepIDs[i]);
					measversion.setPubdata((MeasurePubDataVO)pubdata.clone());
					contextVO.setAttribute(CommonExprCalcEnv.VERSION_FETCHER, measversion);
					// UI端计算
					contextVO.setAttribute(ON_SERVER, true);
					// 设置报表状态：格式态或数据态
					contextVO.setAttribute(ReportContextKey.OPERATION_STATE, ReportContextKey.OPERATION_INPUT);
					ReportCalcSrv repCalcSrv = new ReportCalcSrv(contextVO, true);
					UfoFmlExecutor fmlExecutor = UfoFmlExecutor.getInstance(repCalcSrv.getContextVO(), repCalcSrv.getCellsModel());
//					
					if (bAddLeft.booleanValue() == true) {
						fmlExecutor.setExEnv(CommonExprCalcEnv.EX_CALC_ADDLEFT, CommonExprCalcEnv.EX_VALUE_ON);
						fmlExecutor.setExEnv(CommonExprCalcEnv.EX_CALC_CONVERTDIRECT, CommonExprCalcEnv.EX_VALUE_ON);
						fmlExecutor.setExEnv(CommonExprCalcEnv.EX_CALC_NOTCALCMSELEC, CommonExprCalcEnv.EX_VALUE_ON);
					} else {
						fmlExecutor.setExEnv(CommonExprCalcEnv.EX_CALC_NOTCALCMSELEC, CommonExprCalcEnv.EX_VALUE_OFF);
					}
					fmlExecutor.setExEnv(CommonExprCalcEnv.EX_CHECKEXFUNC, CommonExprCalcEnv.EX_VALUE_OFF);
					fmlExecutor.setExEnv(CommonExprCalcEnv.VERSION_FETCHER, measversion);
					
					qryvo.setIsconvert(isconvert);
					qryvo.setPk_report(aryRepIDs[i]);
					qryvo.setSupMselect(true);
					Map<IArea, FormulaVO> fmlMap = new HBBBFuncUtil(qryvo).getMselectAndAreaFormula(fmlExecutor);
					fmlExecutor.calcAllHBFormulas(fmlMap, null,FormulaModel.TYPE_ZS_FML);

					// 保存报表数据
					CellsModelOperator.saveDataToDB(repCalcSrv.getCellsModel(), repCalcSrv.getContextVO());

					repCalcSrv.getCellsModel();

					result.setCellmodel(repCalcSrv.getCellsModel());
					result.setAloneid(pubdata.getAloneID());
					result.setContextVO(contextVO);
					result.setPk_report(aryRepIDs[i]);
					list.add(result);
				}catch(Exception ex ){
					
					
					UfoContextVO contextVO = new UfoContextVO();
					FuncReTurnObj result = new FuncReTurnObj();
//					contextVO.setAttribute(ReportContextKey.REPORT_NAME, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0116")/* @res "合并报表计算" */);// "合并报表计算"
					contextVO.setAttribute(ReportContextKey.REPORT_PK, aryRepIDs[i]);
					contextVO.setAttribute(CUR_REPORG_PK, pubdata.getUnitPK());
					contextVO.setAttribute(CUR_USER_ID, strUserID);
					contextVO.setAttribute(TYPE, UfoContextVO.REPORT_TABLE);
					contextVO.setAttribute(MEASURE_PUB_DATA_VO, pubdata);
					contextVO.setAttribute(KEYGROUP_PK, pubdata.getKType());
					ConvertMselectVertionUtil measversion = new ConvertMselectVertionUtil(hbSchemeVo, adjustSchemeVo);

					measversion.setHbSchemeVo(hbSchemeVo);
					measversion.setPk_report(aryRepIDs[i]);
					measversion.setPubdata((MeasurePubDataVO)pubdata.clone());
					contextVO.setAttribute(CommonExprCalcEnv.VERSION_FETCHER, measversion);
					// UI端计算
					contextVO.setAttribute(ON_SERVER, true);
					// 设置报表状态：格式态或数据态
					contextVO.setAttribute(ReportContextKey.OPERATION_STATE, ReportContextKey.OPERATION_INPUT);
					ReportCalcSrv repCalcSrv = new ReportCalcSrv(contextVO, true);
					UfoFmlExecutor fmlExecutor = UfoFmlExecutor.getInstance(repCalcSrv.getContextVO(), repCalcSrv.getCellsModel());
//					
					if (bAddLeft.booleanValue() == true) {
						fmlExecutor.setExEnv(CommonExprCalcEnv.EX_CALC_ADDLEFT, CommonExprCalcEnv.EX_VALUE_ON);
						fmlExecutor.setExEnv(CommonExprCalcEnv.EX_CALC_CONVERTDIRECT, CommonExprCalcEnv.EX_VALUE_ON);
						fmlExecutor.setExEnv(CommonExprCalcEnv.EX_CALC_NOTCALCMSELEC, CommonExprCalcEnv.EX_VALUE_ON);
					} else {
						fmlExecutor.setExEnv(CommonExprCalcEnv.EX_CALC_NOTCALCMSELEC, CommonExprCalcEnv.EX_VALUE_OFF);
					}
					fmlExecutor.setExEnv(CommonExprCalcEnv.EX_CHECKEXFUNC, CommonExprCalcEnv.EX_VALUE_OFF);
					fmlExecutor.setExEnv(CommonExprCalcEnv.VERSION_FETCHER, measversion);
					
					qryvo.setIsconvert(isconvert);
					qryvo.setPk_report(aryRepIDs[i]);
					qryvo.setSupMselect(true);
					Map<IArea, FormulaVO> fmlMap = new HBBBFuncUtil(qryvo).getMselectAndAreaFormula(fmlExecutor);
					fmlExecutor.calcAllHBFormulas(fmlMap, null,FormulaModel.TYPE_ZS_FML);

					// 保存报表数据
					CellsModelOperator.saveDataToDB(repCalcSrv.getCellsModel(), repCalcSrv.getContextVO());

					repCalcSrv.getCellsModel();

					result.setCellmodel(repCalcSrv.getCellsModel());
					result.setAloneid(pubdata.getAloneID());
					result.setContextVO(contextVO);
					result.setPk_report(aryRepIDs[i]);
					list.add(result);
				}

			}
			return list.toArray(new FuncReTurnObj[0]);
		} catch (Exception e) {

			nc.bs.logging.Logger.error(e.getMessage(), e);
			StringBuilder content = new StringBuilder();

			if (qryvo.isIsconvert()) {
				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0046")/* @res "计算表样上的折算公式出错" */);
			} 
			throw new BusinessException(content.toString());

		}

	}
	
	/**
	 * 适用：调整表当期数的公式计算
	 * 计算mselect与区域公式
	 * 
	 * @modity litfb 调整当期数计算效率优化
	 * @param qryvo
	 * @throws Exception
	 */
	public static void calcHBBBFormulasWithMselectAndAreaFunc(HBBBFuncQryVO qryvo) throws Exception {
		try {
            MeasurePubDataVO pubdata = qryvo.getPubdata();
            HBSchemeVO hbSchemeVo = qryvo.getHbSchemeVo();
            pubdata.setAccSchemePK(hbSchemeVo.getPk_accperiodscheme());
            
            // 调整方案
            AdjustSchemeVO adjustSchemeVo = null;
            if (hbSchemeVo.getPk_adjustscheme() != null) {
                adjustSchemeVo = (AdjustSchemeVO) NCLocator.getInstance().lookup(IUAPQueryBS.class)
                        .retrieveByPK(AdjustSchemeVO.class, hbSchemeVo.getPk_adjustscheme());
            }

			Boolean bAddLeft = qryvo.getbAddLeft();
			String strUserID = qryvo.getStrUserID();
			String[] aryRepIDs = qryvo.getAryRepIDs();

			for (int i = 0; i < aryRepIDs.length; i++) {
				UfoContextVO contextVO = new UfoContextVO();
				contextVO.setAttribute(ReportContextKey.REPORT_NAME, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0116")/* @res "合并报表计算" */);// "合并报表计算"
				contextVO.setAttribute(ReportContextKey.REPORT_PK, aryRepIDs[i]);
				contextVO.setAttribute(CUR_REPORG_PK, pubdata.getUnitPK());
				contextVO.setAttribute(CUR_USER_ID, strUserID);
				contextVO.setAttribute(TYPE, UfoContextVO.REPORT_TABLE);
				contextVO.setAttribute(MEASURE_PUB_DATA_VO, pubdata);
				contextVO.setAttribute(KEYGROUP_PK, pubdata.getKType());
				
				MSelectVersionUtil measversion = new MSelectVersionUtil(hbSchemeVo, adjustSchemeVo);
				measversion.setPk_report(aryRepIDs[i]);
				MeasurePubDataVO clone = (MeasurePubDataVO) pubdata.clone();
				
				int ver = clone.getVer();
				String strVer = String.valueOf(ver);
				String strSrcVer = strVer.substring(0, strVer.length() - 1);
				clone.setVer(Integer.valueOf(strSrcVer));
				clone.setAloneID(null);
				String aloneID = MeasureDataUtil.getAloneID(clone);
				clone.setAloneID(aloneID);
				measversion.setPubdata(clone);
				contextVO.setAttribute(CommonExprCalcEnv.VERSION_FETCHER, measversion);
				contextVO.setAttribute(ON_SERVER, true);
				// 设置报表状态：格式态或数据态
				contextVO.setAttribute(ReportContextKey.OPERATION_STATE, ReportContextKey.OPERATION_INPUT);
				
				ReportCalcSrv repCalcSrv = new ReportCalcSrv(contextVO, true);
				
				if (bAddLeft.booleanValue() == true) {
				    repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_ADDLEFT, CommonExprCalcEnv.EX_VALUE_ON);
				    repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_CONVERTDIRECT, CommonExprCalcEnv.EX_VALUE_ON);
				    repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_NOTCALCMSELEC, CommonExprCalcEnv.EX_VALUE_ON);
				} else {
				    repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_NOTCALCMSELEC, CommonExprCalcEnv.EX_VALUE_OFF);
				}
				repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CHECKEXFUNC, CommonExprCalcEnv.EX_VALUE_OFF);
				repCalcSrv.setExEnv(CommonExprCalcEnv.VERSION_FETCHER, measversion);
				qryvo.setPk_report(aryRepIDs[i]);
				qryvo.setSupMselect(true);
				
				// 获得mselect和区域公式
				Map<IArea, FormulaVO> fmlMap = new HBBBFuncUtil(qryvo).getMselectAndAreaFormula(repCalcSrv.getFormHandler());
				// 执行公式
				repCalcSrv.calcAllHBFormulas(fmlMap, null,FormulaModel.TYPE_CONS_FML);
				// 保存报表数据
				CellsModelOperator.saveDataToDB(repCalcSrv.getCellsModel(), repCalcSrv.getContextVO());
			}
		} catch (Exception e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
			StringBuilder content = new StringBuilder();
			if (qryvo.isIsconvert()) {
				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0118")/* @res "计算表样上的折算公式" */);
			} else {
				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0119")/* @res "计算表样上的合并调整公式" */);
			}
			if (qryvo.isSupMselect()) {
				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0120")/* @res "中的MSELECT,MSELECTA公式出错！" */);
			} else {
				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0121")/* @res "中的非(MSELECT,MSELECTA)类的区域类公式出错！" */);
			}
			throw new BusinessException(content.toString());
		}
	}

	/**
	 * 适用：报表调整后列的数计算和合并报表的合并列的计算
	 * 算法：与Mselect无关，有映射有公式（只计算映射）；有映射无公式不计算；无映射则计算公式
	 * modified by jiaah
	 */
	public static FuncReTurnObj[] calcFormulasWithOutMSelectFuncWithOutTotalMeas(HBBBFuncQryVO qryvo) throws Exception {
		try {
			MeasurePubDataVO pubdata = qryvo.getPubdata();
			HBSchemeVO hbSchemeVo = qryvo.getHbSchemeVo();
			pubdata.setAccSchemePK(hbSchemeVo.getPk_accperiodscheme());
			Boolean bAddLeft = qryvo.getbAddLeft();
//			MeasureVO[] measures = qryvo.getMeasures();
			String strUserID = qryvo.getStrUserID();
			String[] aryRepIDs = qryvo.getAryRepIDs();
			List<FuncReTurnObj> list = new ArrayList<FuncReTurnObj>();
			for (int i = 0; i < aryRepIDs.length; i++) {
				FuncReTurnObj result = new FuncReTurnObj();
				UfoContextVO contextVO = new UfoContextVO();

				contextVO.setAttribute(ReportContextKey.REPORT_NAME, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0116")/* @res "合并报表计算" */);// "合并报表计算"
				contextVO.setAttribute(ReportContextKey.REPORT_PK, aryRepIDs[i]);
				// contextVO.setAttribute(CREATE_UNIT_ID, strCreateUnitId);
				contextVO.setAttribute(CUR_REPORG_PK, pubdata.getUnitPK());
				contextVO.setAttribute(CUR_USER_ID, strUserID);
				contextVO.setAttribute(TYPE, UfoContextVO.REPORT_TABLE);
				contextVO.setAttribute(MEASURE_PUB_DATA_VO, pubdata);
				contextVO.setAttribute(KEYGROUP_PK, pubdata.getKType());
				// UI端计算
				contextVO.setAttribute(ON_SERVER, true);
				// 设置报表状态：格式态或数据态
				contextVO.setAttribute(ReportContextKey.OPERATION_STATE, ReportContextKey.OPERATION_INPUT);

				CellsModel model = qryvo.getFormatCellsModel();
				if(model == null)
					model = CellsModelOperator.getFormatModelByPK(contextVO);
				HBRepCalcSrv repCalcSrv = new HBRepCalcSrv(contextVO,model);
				if (bAddLeft.booleanValue() == true) {
					repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_ADDLEFT, CommonExprCalcEnv.EX_VALUE_ON);
					repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_CONVERTDIRECT, CommonExprCalcEnv.EX_VALUE_ON);
					repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_NOTCALCMSELEC, CommonExprCalcEnv.EX_VALUE_ON);
				} else {
					repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_NOTCALCMSELEC, CommonExprCalcEnv.EX_VALUE_OFF);
				}
				repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CHECKEXFUNC, CommonExprCalcEnv.EX_VALUE_OFF);
				qryvo.setPk_report(aryRepIDs[i]);
				qryvo.setSupMselect(false);
				
				//报表指标中含有公式的指标不运行公式,折算不适用
				Map<IArea, FormulaVO> fmlMap = new HBBBFuncUtil(qryvo).get();
				if (!qryvo.isIsconvert()) {
					Map<String, ProjectVO> measureProjectMap = HBBaseDocItfService.getRemoteHBProjectMapQry().loadMappingsByReportId(aryRepIDs[i]);
					Set<String> keySet = measureProjectMap.keySet();
					if(keySet.size()>0){
						Map<IArea, FormulaVO> tmplmap= new HashMap<IArea, FormulaVO>();
						Set<IArea> keySet2 = fmlMap.keySet();
						for (Iterator<IArea> iterator = keySet2.iterator(); iterator.hasNext();) {
							IArea iArea = (IArea) iterator.next();
							Map<CellPosition, MeasureVO> mainMeasureVOByArea = CellsModelOperator.getMeasureModel(repCalcSrv.getCellsModel()).getMainMeasureVOByArea(iArea);
							Collection<MeasureVO> values = mainMeasureVOByArea.values();
							boolean isHashFormula = false;
							for (Iterator<MeasureVO> iterator2 = values.iterator(); iterator2.hasNext();) {
								MeasureVO measureVO = (MeasureVO) iterator2.next();
								//有映射有公式则只计算公式；有映射无公式不计算；无映射则计算公式--jiaah
								//更改为有映射有公式，只计算映射
								if(keySet.contains(measureVO.getCode())){
									isHashFormula = true;
									break;
								}
							}
							//如果不包含公式
							if(!isHashFormula){
								tmplmap.put(iArea, fmlMap.get(iArea));
							}
						}
						repCalcSrv.calcAllHBFormulas(tmplmap, null,FormulaModel.TYPE_CONS_FML);
					}else{
						repCalcSrv.calcAllHBFormulas(fmlMap, null,FormulaModel.TYPE_CONS_FML);
					}
				}else{
					repCalcSrv.calcAllHBFormulas(fmlMap, null,FormulaModel.TYPE_CONS_FML);
				}

				// 保存报表数据
				CellsModelOperator.saveDataToDB(repCalcSrv.getCellsModel(), repCalcSrv.getContextVO());

				result.setCellmodel(repCalcSrv.getCellsModel());
				result.setAloneid(pubdata.getAloneID());
				result.setContextVO(contextVO);
				result.setPk_report(aryRepIDs[i]);
				list.add(result);
			}
			return list.toArray(new FuncReTurnObj[0]);
		} catch (Exception e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
			StringBuilder content = new StringBuilder();

			if (qryvo.isIsconvert()) {
				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0118")/* @res "计算表样上的折算公式" */);
			} else {
				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0119")/* @res "计算表样上的合并调整公式" */);
			}
			if (qryvo.isSupMselect()) {
				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0120")/* @res "中的MSELECT,MSELECTA公式出错！" */);
			} else {
				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0121")/* @res "中的非(MSELECT,MSELECTA)类的区域类公式出错！" */);
			}
			throw new BusinessException(content.toString());
		}

	}
	
	
	/**
	 * 
	 * 适用：合并报表合计数列计算
	 * 算法：先计算mselect公式后计算区域公式--modified by jiaah
	 * <p>修改记录：</p>
	 * @param qryvo
	 * @throws Exception
	 * @see 
	 * @since V6.0
	 */
	public static void calcHBBBTotalFormulasWithMselectFunc(HBBBFuncQryVO qryvo) throws Exception {
		try {
			MeasurePubDataVO pubdata = qryvo.getPubdata();
			HBSchemeVO hbSchemeVo = qryvo.getHbSchemeVo();
			AdjustSchemeVO adjustSchemeVo = null;
			if(hbSchemeVo.getPk_adjustscheme() != null){
				adjustSchemeVo = (AdjustSchemeVO) NCLocator.getInstance().lookup(IUAPQueryBS.class).retrieveByPK(AdjustSchemeVO.class, hbSchemeVo.getPk_adjustscheme());	
			}
			pubdata.setAccSchemePK(hbSchemeVo.getPk_accperiodscheme());

			Boolean bAddLeft = qryvo.getbAddLeft();
			String strUserID = qryvo.getStrUserID();
			String[] aryRepIDs = qryvo.getAryRepIDs();
			boolean isconvert = qryvo.isIsconvert();

			for (int i = 0; i < aryRepIDs.length; i++) {
				UfoContextVO contextVO = new UfoContextVO();

				contextVO.setAttribute(ReportContextKey.REPORT_NAME, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0116")/* @res "合并报表计算" */);// "合并报表计算"
				contextVO.setAttribute(ReportContextKey.REPORT_PK, aryRepIDs[i]);
				contextVO.setAttribute(CUR_REPORG_PK, pubdata.getUnitPK());
				contextVO.setAttribute(CUR_USER_ID, strUserID);
				contextVO.setAttribute(TYPE, UfoContextVO.REPORT_TABLE);
				contextVO.setAttribute(MEASURE_PUB_DATA_VO, pubdata);
				contextVO.setAttribute(KEYGROUP_PK, pubdata.getKType());
				//begin-ncm-rendp-NC2016022000051-2016-3-10-专项 
				/** 
				* 此补丁修正了产品BUG:专项需求：合并报表的合计列计算mselect要求取合并报表不取合并调整表
				*/ 
				//MSelectVersionUtil measversion = new MSelectVersionUtil(hbSchemeVo, adjustSchemeVo);
				HBUnionMselectVersion measversion = new HBUnionMselectVersion(hbSchemeVo, adjustSchemeVo);
				//end-ncm-rendp-NC2016022000051-2016-3-10-专项 

				// 合并报表调整,先取合并报表调整表数据
				MeasurePubDataVO clone = (MeasurePubDataVO) pubdata.clone();
				measversion.setPk_report(aryRepIDs[i]);
				// 合并报表
				clone.setVer(hbSchemeVo.getVersion());
				clone.setAloneID(null);
				String aloneID = MeasureDataUtil.getAloneID(clone);
				clone.setAloneID(aloneID);
				measversion.setPubdata(clone);	
				
				contextVO.setAttribute(ON_SERVER, true);
				// 设置报表状态：格式态或数据态
				contextVO.setAttribute(ReportContextKey.OPERATION_STATE, ReportContextKey.OPERATION_INPUT);
				contextVO.setAttribute(CommonExprCalcEnv.VERSION_FETCHER, measversion);
				
				CellsModel model = qryvo.getFormatCellsModel();
				if(model == null)
					model = CellsModelOperator.getFormatModelByPK(contextVO);
				HBRepCalcSrv repCalcSrv = new HBRepCalcSrv(contextVO,model);
				if (bAddLeft.booleanValue() == true) {
					repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_ADDLEFT, CommonExprCalcEnv.EX_VALUE_ON);
					repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_CONVERTDIRECT, CommonExprCalcEnv.EX_VALUE_ON);
					repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_NOTCALCMSELEC, CommonExprCalcEnv.EX_VALUE_ON);
				} else {
					repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_NOTCALCMSELEC, CommonExprCalcEnv.EX_VALUE_OFF);
				}
				repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CHECKEXFUNC, CommonExprCalcEnv.EX_VALUE_OFF);
				repCalcSrv.setExEnv(CommonExprCalcEnv.VERSION_FETCHER, measversion);
				qryvo.setIsconvert(isconvert);
				qryvo.setPk_report(aryRepIDs[i]);
				qryvo.setSupMselect(true);
				Map<IArea, FormulaVO> fmlMap =  new HBBBFuncUtil(qryvo).getMselectAndAreaFormula(repCalcSrv.getFormHandler());
				if(fmlMap != null && fmlMap.size() > 0){
					//先执行mselect后执行区域公式
					repCalcSrv.calcAllHBFormulas(fmlMap, null,FormulaModel.TYPE_CONS_FML);
					// 保存报表数据
					CellsModelOperator.saveDataToDB(repCalcSrv.getCellsModel(), repCalcSrv.getContextVO());
				}
			}
		} catch (Exception e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
			StringBuilder content = new StringBuilder();

			if (qryvo.isIsconvert()) {
				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0118")/* @res "计算表样上的折算公式" */);
			} else {
				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0119")/* @res "计算表样上的合并调整公式" */);
			}
			if (qryvo.isSupMselect()) {
				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0120")/* @res "中的MSELECT,MSELECTA公式出错！" */);
			} else {
				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0121")/* @res "中的非(MSELECT,MSELECTA)类的区域类公式出错！" */);
			}
//			content.append(e.getMessage());
			throw new BusinessException(content.toString());

		}

	}
	

	
//	/**
//	 * 目前只在折算中用。
//	 */
//	public static void calcHBBBFormulasWithMselectFunc(HBBBFuncQryVO qryvo) throws Exception {
//		try {
//			MeasurePubDataVO pubdata = qryvo.getPubdata();
//			HBSchemeVO hbSchemeVo = qryvo.getHbSchemeVo();
//			AdjustSchemeVO adjustSchemeVo = null;
//			if(hbSchemeVo.getPk_adjustscheme() != null) {
//				adjustSchemeVo = (AdjustSchemeVO) NCLocator.getInstance().lookup(IUAPQueryBS.class).retrieveByPK(AdjustSchemeVO.class, hbSchemeVo.getPk_adjustscheme());
//			}
//			pubdata.setAccSchemePK(hbSchemeVo.getPk_accperiodscheme());
//
//			Boolean bAddLeft = qryvo.getbAddLeft();
////			MeasureVO[] measures = qryvo.getMeasures();
//			String strUserID = qryvo.getStrUserID();
//			String[] aryRepIDs = qryvo.getAryRepIDs();
//			boolean isconvert = qryvo.isIsconvert();
//
//			for (int i = 0; i < aryRepIDs.length; i++) {
//				UfoContextVO contextVO = new UfoContextVO();
//
//				contextVO.setAttribute(ReportContextKey.REPORT_NAME, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0116")/* @res "合并报表计算" */);// "合并报表计算"
//				contextVO.setAttribute(ReportContextKey.REPORT_PK, aryRepIDs[i]);
//				// contextVO.setAttribute(CREATE_UNIT_ID, strCreateUnitId);
//				contextVO.setAttribute(CUR_REPORG_PK, pubdata.getUnitPK());
//				contextVO.setAttribute(CUR_USER_ID, strUserID);
//				contextVO.setAttribute(TYPE, UfoContextVO.REPORT_TABLE);
//				contextVO.setAttribute(MEASURE_PUB_DATA_VO, pubdata);
//				contextVO.setAttribute(KEYGROUP_PK, pubdata.getKType());
//				MSelectVersionUtil measversion = new MSelectVersionUtil(hbSchemeVo, adjustSchemeVo);
//
//				if (qryvo.isIsconvert()) {
//					// 折算
//					measversion.setPk_report(aryRepIDs[i]);
//					measversion.setPubdata(pubdata);
//					contextVO.setAttribute(CommonExprCalcEnv.VERSION_FETCHER, measversion);
//				} else {
//					// 是个别报表调整,先取个别报表调整表
//					measversion.setPk_report(aryRepIDs[i]);
//					MeasurePubDataVO clone = (MeasurePubDataVO) pubdata.clone();
//					int ver = clone.getVer();
//					String valueOf = String.valueOf(ver);
//					String newversion = valueOf.substring(0, valueOf.length() - 1);
//					clone.setVer(Integer.valueOf(newversion));
//					clone.setAloneID(null);
//					String aloneID = MeasureDataUtil.getAloneID(clone);
//					clone.setAloneID(aloneID);
//					measversion.setPubdata(clone);
//					contextVO.setAttribute(CommonExprCalcEnv.VERSION_FETCHER, measversion);
//				}
//				// UI端计算
//				contextVO.setAttribute(ON_SERVER, true);
//
//				CellsModel model = qryvo.getFormatCellsModel();
//				if(model == null)
//					model = CellsModelOperator.getFormatModelByPK(contextVO);
//				//
//				HBRepCalcSrv repCalcSrv = new HBRepCalcSrv(contextVO,model);
//				
////				ReportCalcSrv repCalcSrv = new ReportCalcSrv(contextVO, true);
//				if (bAddLeft.booleanValue() == true) {
//					repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_ADDLEFT, CommonExprCalcEnv.EX_VALUE_ON);
//					repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_CONVERTDIRECT, CommonExprCalcEnv.EX_VALUE_ON);
//					repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_NOTCALCMSELEC, CommonExprCalcEnv.EX_VALUE_ON);
//				} else {
//					repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_NOTCALCMSELEC, CommonExprCalcEnv.EX_VALUE_OFF);
//				}
//				repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CHECKEXFUNC, CommonExprCalcEnv.EX_VALUE_OFF);
//				repCalcSrv.setExEnv(CommonExprCalcEnv.VERSION_FETCHER, measversion);
//				// HBBBFuncQryVO qryvo=new HBBBFuncQryVO();
//				qryvo.setIsconvert(isconvert);
//				qryvo.setPk_report(aryRepIDs[i]);
//				qryvo.setSupMselect(true);
//				Map<IArea, FormulaVO> fmlMap = new HBBBFuncUtil(qryvo).get();
//
//				repCalcSrv.calcAllHBFormulas(fmlMap, null);
//
//				//
//				if (!qryvo.isIsconvert()) {
//					// 是个别报表调整
//					// 先个别报表调整表数据,如果都为0,则取上年个别报表
//					boolean isAllZero = true;
//					Set<IArea> keySet = fmlMap.keySet();
//					for (Iterator<IArea> iterator = keySet.iterator(); iterator.hasNext();) {
//						IArea iArea = (IArea) iterator.next();
//						CellPosition[] split = iArea.split();
//						for (int j = 0; j < split.length; j++) {
//							CellPosition cellPosition = split[j];
//							Object cellValue = repCalcSrv.getCellsModel().getCellValue(cellPosition);
//							if (cellValue != null && !new UFDouble(String.valueOf(cellValue)).equals(UFDouble.ZERO_DBL)) {
//								isAllZero = false;
//								break;
//							}
//						}
//
//					}
//					if (isAllZero) {
//						// 如果都为0,则取上一年个别报表
//						measversion = new MSelectVersionUtil(hbSchemeVo, adjustSchemeVo);
//						measversion.setPk_report(aryRepIDs[i]);
//
//						MeasurePubDataVO clone = (MeasurePubDataVO) pubdata.clone();
//						int ver = clone.getVer();
//						String valueOf = String.valueOf(ver);
//						String newversion = valueOf.substring(0, valueOf.length() - 1);
//						Integer valueOf2 = Integer.valueOf(newversion);
//						if(adjustSchemeVo != null) {
//							if (valueOf2.intValue() == adjustSchemeVo.getVersion().intValue()) {
//								// 个别报表
//								clone.setVer(IDataVersionConsts.VER_SEPERATE);
//							} else {
//								// 合并报表
//								clone.setVer(hbSchemeVo.getVersion());
//							}
//						}
//						clone.setAloneID(null);
//						String aloneID = MeasureDataUtil.getAloneID(clone);
//						clone.setAloneID(aloneID);
//						measversion.setPubdata(clone);
//
//						// measversion.setPubdata(pubdata);
//						contextVO.setAttribute(CommonExprCalcEnv.VERSION_FETCHER, measversion);
//
//
//						repCalcSrv = new HBRepCalcSrv(contextVO, model);
////						repCalcSrv = new ReportCalcSrv(contextVO, true);
//						if (bAddLeft.booleanValue() == true) {
//							repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_ADDLEFT, CommonExprCalcEnv.EX_VALUE_ON);
//							repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_CONVERTDIRECT, CommonExprCalcEnv.EX_VALUE_ON);
//							repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_NOTCALCMSELEC, CommonExprCalcEnv.EX_VALUE_ON);
//						} else {
//							repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CALC_NOTCALCMSELEC, CommonExprCalcEnv.EX_VALUE_OFF);
//						}
//						repCalcSrv.setExEnv(CommonExprCalcEnv.EX_CHECKEXFUNC, CommonExprCalcEnv.EX_VALUE_OFF);
//						repCalcSrv.setExEnv(CommonExprCalcEnv.VERSION_FETCHER, measversion);
//						// 再次执行
//						fmlMap = new HBBBFuncUtil(qryvo).get();
//						repCalcSrv.calcAllHBFormulas(fmlMap, null);
//					}
//
//				}
//				//
//				// 保存报表数据
//				CellsModelOperator.saveDataToDB(repCalcSrv.getCellsModel(), repCalcSrv.getContextVO());
//
//				repCalcSrv.getCellsModel();
//
//			}
//		} catch (Exception e) {
//
//			nc.bs.logging.Logger.error(e.getMessage(), e);
//			StringBuilder content = new StringBuilder();
//
//			if (qryvo.isIsconvert()) {
//				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0118")/* @res "计算表样上的折算公式" */);
//			} else {
//				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0119")/* @res "计算表样上的合并调整公式" */);
//			}
//			if (qryvo.isSupMselect()) {
//				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0120")/* @res "中的MSELECT,MSELECTA公式出错！" */);
//			} else {
//				content.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0121")/* @res "中的非(MSELECT,MSELECTA)类的区域类公式出错！" */);
//			}
//			throw new BusinessException(content.toString());
//
//		}
//
//	}
	
	
//	/**
//	 * 运行折算公式
//	 * @create by fengzhy at 2012-6-28,上午9:03:17
//	 *
//	 * @param qryvo
//	 * @return
//	 * @throws Exception
//	 */
//	public static FuncReTurnObj[] calcZSFormulas(HBBBFuncQryVO qryvo) throws Exception {
//		HBSchemeVO hbSchemeVo = qryvo.getHbSchemeVo();
//		AdjustSchemeVO adjustSchemeVo = null;
//		if(hbSchemeVo.getPk_adjustscheme() != null) {
//			adjustSchemeVo = (AdjustSchemeVO) NCLocator.getInstance().lookup(IUAPQueryBS.class).retrieveByPK(AdjustSchemeVO.class, hbSchemeVo.getPk_adjustscheme());
//		}
//		return calcZSFormulas(qryvo, adjustSchemeVo);
//	}
}
