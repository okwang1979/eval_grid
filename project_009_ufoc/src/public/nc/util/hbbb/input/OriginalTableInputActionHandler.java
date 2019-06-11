package nc.util.hbbb.input;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import nc.bs.framework.common.NCLocator;
import nc.itf.iufo.balance.IBalanceCondService;
import nc.itf.iufo.balance.IBalanceService;
import nc.pub.iufo.cache.ReportCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.util.hbbb.HBAloneIDUtil;
import nc.util.hbbb.HBBBReportUtil;
import nc.util.hbbb.HBKeyGroupUtil;
import nc.util.hbbb.InputActionHandlerProxy;
import nc.util.iufo.input.BalanceReportExportUtil;
import nc.utils.iufo.TotalSrvUtils;
import nc.vo.iufo.balance.BalanceCondVO;
import nc.vo.iufo.data.MeasureDataUtil;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.RepDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iufo.total.TotalSchemeVO;
import nc.vo.iuforeport.rep.ReportVO;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.iufo.table.exarea.ExtendAreaCell;
import com.ufsoft.iufo.fmtplugin.datastate.CellsModelOperator;
import com.ufsoft.iufo.fmtplugin.dynarea.DynAreaUtil;
import com.ufsoft.iufo.fmtplugin.dynarea.DynamicAreaModel;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.fmtplugin.formatcore.UfoContextVO;
import com.ufsoft.iufo.fmtplugin.measure.MeasureModel;
import com.ufsoft.iufo.fmtplugin.service.ReportFormatSrv;
import com.ufsoft.iufo.inputplugin.biz.file.MenuStateData;
import com.ufsoft.iuforeport.repdatainput.LoginEnvVO;
import com.ufsoft.iuforeport.repdatainput.RepDataOperResultVO;
import com.ufsoft.iuforeport.repdatainput.TableInputActionHandler;
import com.ufsoft.iuforeport.repdatainput.TableInputHandlerHelper;
import com.ufsoft.iuforeport.tableinput.applet.IRepDataParam;
import com.ufsoft.report.IufoFormat;
import com.ufsoft.table.AreaPosition;
import com.ufsoft.table.Cell;
import com.ufsoft.table.CellPosition;
import com.ufsoft.table.CellsModel;
import com.ufsoft.table.format.CellFont;
import com.ufsoft.table.format.ICellFont;
import com.ufsoft.table.format.IFormat;

public class OriginalTableInputActionHandler extends TableInputActionHandler {

	public RepDataOperResultVO innerOpenRepData(IRepDataParam param,LoginEnvVO loginEnv,String strBalCondPK,MeasureVO meassureVO,String pk_countorg) throws Exception{
//		int iRepDataAuthType = getRepDataRight(param);
//		if(iRepDataAuthType == IUfoContextKey.RIGHT_DATA_NULL)
//           throw new Exception("无查看当前报表的数据权限");
		String reportPK = param.getReportPK();

		int iRepDataAuthType=RIGHT_DATA_READ;
		ReportCache repCache = UFOCacheManager.getSingleton().getReportCache();
		ReportVO repvo=repCache.getByPK(reportPK);
		if(null!=repvo &&   null!=repvo.getDataauthflag() && repvo.getDataauthflag().booleanValue()){
			iRepDataAuthType = InputActionHandlerProxy.getRepDataRight(param);
			if(iRepDataAuthType == IUfoContextKey.RIGHT_DATA_NULL)
	           throw new Exception(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0345")/*@res "无查看当前报表的数据权限"*/);
		}


		MeasurePubDataVO pubData=param.getPubData();
        UfoContextVO context=TableInputHandlerHelper.getContextVO(param,loginEnv);
        CellsModel cellsModel=null;
        if (strBalCondPK==null || strBalCondPK.equals(BalanceCondVO.NON_SW_DATA_COND_PK)){
        	ReportFormatSrv repFormatSrv=new ReportFormatSrv(context,true);
        	cellsModel=repFormatSrv.getCellsModel();
        }else{
        	BalanceCondVO balanceCond=(BalanceCondVO)NCLocator.getInstance().lookup(IBalanceCondService.class).loadBalanceCondByPK(strBalCondPK);
        	RepDataVO repData=(RepDataVO)(((IBalanceService)NCLocator.getInstance().lookup(IBalanceCondService.class)).doSwBalance(pubData, balanceCond,reportPK, param.getRepMngStructPK()));
        	cellsModel=CellsModelOperator.getFormatModelByPK(context);
        	cellsModel=CellsModelOperator.doGetDataModelFromRepDataVO(cellsModel, repData, context);
        	BalanceReportExportUtil.processBalanceRepCellsModel(new ReportFormatSrv(context,cellsModel),false,balanceCond);
        	pubData.setVer(0);
            pubData.setAloneID(MeasureDataUtil.getAloneID(pubData));
        }
        
        if(meassureVO != null && meassureVO.getCode() != null){
        	//根据指标重新设置背景颜色
            CellPosition measurePosByPK = CellsModelOperator.getMeasureModel(cellsModel).getMeasurePosByPK(meassureVO.getCode());
            if(HBBBReportUtil.isIntrateRep(reportPK)){
            	MeasureModel measureModel = DynAreaUtil.getMeasureModel(cellsModel);
            	measurePosByPK = measureModel.getMeasurePosByPK(meassureVO.getCode());
            	DynamicAreaModel dynAreaModel = CellsModelOperator.getDynAreaModel(cellsModel);
            	ExtendAreaCell dynAreaCellByFmtPos = dynAreaModel.getDynAreaCellByFmtPos(measurePosByPK);
            	if(dynAreaCellByFmtPos!=null){
            		String exAreaPK = dynAreaCellByFmtPos.getExAreaPK();
            		String keyCombPK = meassureVO.getKeyCombPK();
            		KeyGroupVO keygroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(keyCombPK);
            		KeyVO[] keyvos=keygroupVo.getKeys();
            		String pk_selforg ="";
            		Map<String,String> keyMap = new HashMap<String,String>();
            		if(null!=keyvos && null!=keyMap /*&& keyvos.length==keyMap.size()*/){
            			for (int i = 0; i < keyvos.length; i++) {
            				KeyVO keyVO = keyvos[i];
            				String keywordByPK = pubData.getKeywordByPK(keyVO.getPk_keyword());
            				if(keyVO.getPk_keyword().equals(KeyVO.CORP_PK)){
            					pk_selforg = keywordByPK;
            				}
            				if(keywordByPK!=null)
            					keyMap.put(keyVO.getPk_keyword(), keywordByPK);
            			}
            		}
            		String pk_dynvalue = HBKeyGroupUtil.getPk_dynKeyValue(keygroupVo,repvo.getPk_key_comb());
            		//取得动态区的ALONEID
            		String findAloneID = HBAloneIDUtil.findAloneID(pk_selforg, keyCombPK, keyMap, pk_countorg,pk_dynvalue);
            		int ownerUnitAreaNumByAloneId = DynAreaUtil.getOwnerUnitAreaNumByAloneId(findAloneID, exAreaPK, cellsModel);
            		measurePosByPK = (CellPosition) DynAreaUtil.getMoveArea(cellsModel, exAreaPK, measurePosByPK, ownerUnitAreaNumByAloneId);
            	}else{
            		measurePosByPK= CellsModelOperator.getMeasureModel(cellsModel).getMeasurePosByPK(meassureVO.getCode());
            	}
            }else{
            	measurePosByPK= CellsModelOperator.getMeasureModel(cellsModel).getMeasurePosByPK(meassureVO.getCode());
            }
            if(measurePosByPK!=null){
            	Cell cell = cellsModel.getCell(measurePosByPK);
            	IufoFormat format = (IufoFormat) cell.getFormat();
            	CellFont font = (CellFont) format.getFont();
            	ICellFont instance = CellFont.getInstance(font.getFontname(), font.getFontstyle(), font.getFontsize(), Color.GRAY, font.getForegroundColor());
            	IFormat instance2 = IufoFormat.getInstance(format.getDataFormat(), instance, format.getAlign(), format.getLines());
            	cell.setFormat(instance2);
            	// 选中该指标
            	cellsModel.getSelectModel().setSelectedArea(AreaPosition.getInstance(measurePosByPK, measurePosByPK));
            }
        }
        
        RepDataOperResultVO result=new RepDataOperResultVO();
        result.setMenuState(TableInputHandlerHelper.getMenuStateData(context, param, loginEnv, iRepDataAuthType));
        result.setFmlCanInput(false);
        result.setCellsModel(cellsModel);

        MenuStateData menuData=result.getMenuState();
        processMenuState(menuData,param,loginEnv);
//		boolean bIsCanInput = UfoEFormulaEditControl.isFormulaEdit(param.getReportPK(),param.getTaskPK());


		return result;
	}
	
	
	private  RepDataOperResultVO innerOpenRepData(IRepDataParam param,LoginEnvVO loginEnv,String strBalCondPK,MeasureVO meassureVO,Map<String,String> otherDocAndValue) throws Exception{
//		int iRepDataAuthType = getRepDataRight(param);
//		if(iRepDataAuthType == IUfoContextKey.RIGHT_DATA_NULL)
//           throw new Exception("无查看当前报表的数据权限");
		String reportPK = param.getReportPK();

		int iRepDataAuthType=RIGHT_DATA_READ;
		ReportCache repCache = UFOCacheManager.getSingleton().getReportCache();
		ReportVO repvo=repCache.getByPK(reportPK);
		if(null!=repvo &&   null!=repvo.getDataauthflag() && repvo.getDataauthflag().booleanValue()){
			iRepDataAuthType = InputActionHandlerProxy.getRepDataRight(param);
			if(iRepDataAuthType == IUfoContextKey.RIGHT_DATA_NULL)
	           throw new Exception(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0345")/*@res "无查看当前报表的数据权限"*/);
		}


		MeasurePubDataVO pubData=param.getPubData();
        UfoContextVO context=TableInputHandlerHelper.getContextVO(param,loginEnv);
        CellsModel cellsModel=null;
        if (strBalCondPK==null || strBalCondPK.equals(BalanceCondVO.NON_SW_DATA_COND_PK)){
        	ReportFormatSrv repFormatSrv=new ReportFormatSrv(context,true);
        	cellsModel=repFormatSrv.getCellsModel();
        }else{
        	BalanceCondVO balanceCond=(BalanceCondVO)NCLocator.getInstance().lookup(IBalanceCondService.class).loadBalanceCondByPK(strBalCondPK);
        	RepDataVO repData=(RepDataVO)(((IBalanceService)NCLocator.getInstance().lookup(IBalanceCondService.class)).doSwBalance(pubData, balanceCond,reportPK, param.getRepMngStructPK()));
        	cellsModel=CellsModelOperator.getFormatModelByPK(context);
        	cellsModel=CellsModelOperator.doGetDataModelFromRepDataVO(cellsModel, repData, context);
        	BalanceReportExportUtil.processBalanceRepCellsModel(new ReportFormatSrv(context,cellsModel),false,balanceCond);
        	pubData.setVer(0);
            pubData.setAloneID(MeasureDataUtil.getAloneID(pubData));
        }
        
        if(meassureVO != null && meassureVO.getCode() != null){
        	//根据指标重新设置背景颜色
            CellPosition measurePosByPK = CellsModelOperator.getMeasureModel(cellsModel).getMeasurePosByPK(meassureVO.getCode());
            if(HBBBReportUtil.isIntrateRep(reportPK)){
            	MeasureModel measureModel = DynAreaUtil.getMeasureModel(cellsModel);
            	measurePosByPK = measureModel.getMeasurePosByPK(meassureVO.getCode());
            	DynamicAreaModel dynAreaModel = CellsModelOperator.getDynAreaModel(cellsModel);
            	ExtendAreaCell dynAreaCellByFmtPos = dynAreaModel.getDynAreaCellByFmtPos(measurePosByPK);
            	if(dynAreaCellByFmtPos!=null){
            		String exAreaPK = dynAreaCellByFmtPos.getExAreaPK();
            		String keyCombPK = meassureVO.getKeyCombPK();
            		KeyGroupVO keygroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(keyCombPK);
            		KeyVO[] keyvos=keygroupVo.getKeys();
            		String pk_selforg ="";
            		Map<String,String> keyMap = new HashMap<String,String>();
            		if(null!=keyvos && null!=keyMap /*&& keyvos.length==keyMap.size()*/){
            			for (int i = 0; i < keyvos.length; i++) {
            				KeyVO keyVO = keyvos[i];
            				String keywordByPK = pubData.getKeywordByPK(keyVO.getPk_keyword());
            				if(keyVO.getPk_keyword().equals(KeyVO.CORP_PK)){
            					pk_selforg = keywordByPK;
            				}
            				if(keywordByPK!=null)
            					keyMap.put(keyVO.getPk_keyword(), keywordByPK);
            			}
            		}
            		String pk_dynvalue = HBKeyGroupUtil.getPk_dynKeyValue(keygroupVo,repvo.getPk_key_comb());
            		//取得动态区的ALONEID
            		for(String key:otherDocAndValue.keySet()){
            			keyMap.put(key, otherDocAndValue.get(key));
            		}
            		String findAloneID = HBAloneIDUtil.findAloneID(pk_selforg, keyCombPK, keyMap);
            		int ownerUnitAreaNumByAloneId = DynAreaUtil.getOwnerUnitAreaNumByAloneId(findAloneID, exAreaPK, cellsModel);
            		measurePosByPK = (CellPosition) DynAreaUtil.getMoveArea(cellsModel, exAreaPK, measurePosByPK, ownerUnitAreaNumByAloneId);
            	}else{
            		measurePosByPK= CellsModelOperator.getMeasureModel(cellsModel).getMeasurePosByPK(meassureVO.getCode());
            	}
            }else{
            	measurePosByPK= CellsModelOperator.getMeasureModel(cellsModel).getMeasurePosByPK(meassureVO.getCode());
            }
            if(measurePosByPK!=null){
            	Cell cell = cellsModel.getCell(measurePosByPK);
            	IufoFormat format = (IufoFormat) cell.getFormat();
            	CellFont font = (CellFont) format.getFont();
            	ICellFont instance = CellFont.getInstance(font.getFontname(), font.getFontstyle(), font.getFontsize(), Color.GRAY, font.getForegroundColor());
            	IFormat instance2 = IufoFormat.getInstance(format.getDataFormat(), instance, format.getAlign(), format.getLines());
            	cell.setFormat(instance2);
            	// 选中该指标
            	cellsModel.getSelectModel().setSelectedArea(AreaPosition.getInstance(measurePosByPK, measurePosByPK));
            }
        }
        
        RepDataOperResultVO result=new RepDataOperResultVO();
        result.setMenuState(TableInputHandlerHelper.getMenuStateData(context, param, loginEnv, iRepDataAuthType));
        result.setFmlCanInput(false);
        result.setCellsModel(cellsModel);

        MenuStateData menuData=result.getMenuState();
        processMenuState(menuData,param,loginEnv);
//		boolean bIsCanInput = UfoEFormulaEditControl.isFormulaEdit(param.getReportPK(),param.getTaskPK());


		return result;
	}

	@SuppressWarnings({ "unchecked" })
	public RepDataOperResultVO proxyopenRepData(Object param) throws Exception{

		try{
			Object[] params=(Object[])param;
			IRepDataParam repDataParam=(IRepDataParam)params[0];

			boolean bFreeTotal = (Boolean) params[4];
			//如果组织为组织属性构建 :没有单位关键字时，不能通过单位关键字判断。
//			if (StringUtil.isEmptyWithTrim(OrgUtil.getOrgName(repDataParam.getPubData().getUnitPK())))
			if(bFreeTotal)
			{
				LoginEnvVO loginEnv=(LoginEnvVO)params[1];
				ArrayList<String> lstTotalReportOrgPK=(params!=null&&params.length>3)?(ArrayList<String>)params[3]:null;
				TotalSchemeVO totalScheme=new TotalSchemeVO();
				totalScheme.setOrg_type(TotalSchemeVO.TYPE_CUSTOMER);
				totalScheme.setOrg_content(lstTotalReportOrgPK);
				totalScheme.setPk_org(repDataParam.getRepOrgPK());
				RepDataOperResultVO result= TotalSrvUtils.createFreeTotalResults(repDataParam.getPubData(), totalScheme,repDataParam.getReportPK(), null);
				UfoContextVO context=TableInputHandlerHelper.getContextVO(repDataParam,loginEnv);
				context.setAttribute(MEASURE_PUB_DATA_VO, repDataParam.getPubData());
				MenuStateData  menuState=TableInputHandlerHelper.getMenuStateData(context, repDataParam, loginEnv, UfoContextVO.RIGHT_DATA_WRITE);
				menuState.setCanCommit(false);
				menuState.setCanRequestCancelCommit(false);
				menuState.setCanAreaCal(false);
				menuState.setCanExcelImp(false);
				menuState.setCanSW(false);
				result.setMenuState(menuState);
				return result;
			}else
			{
				try{
//					Object[] params=(Object[])param;
					if(params[6] instanceof Map){
						Map<String, String> mapPara = new HashMap<String, String>();
						Map values = (Map)params[6];
						for(Object key :values.keySet()){
							Object value = values.get(key);
							if(value!=null){
								String strValue = String.valueOf(value);
								mapPara.put(String.valueOf(key), strValue);
							}
							
						}
						return innerOpenRepData((IRepDataParam)params[0],(LoginEnvVO)params[1],(String)params[2],(MeasureVO)params[5],mapPara);
					}else{
						return innerOpenRepData((IRepDataParam)params[0],(LoginEnvVO)params[1],(String)params[2],(MeasureVO)params[5],(String)params[6]);
					}
					
				}catch(Exception e){
					AppDebug.debug(e);
					throw e;
				}
			}

		}catch(Exception e){
			AppDebug.debug(e);
			throw e;
		}


	}
	protected void processMenuState(MenuStateData menuData,IRepDataParam param,LoginEnvVO loginEnv) throws Exception{
		/*MeasurePubDataVO pubData=param.getPubData();
		if (menuData!=null){
			menuData.setHasTaskCheckFormula(IUfoInputActionUtil.isExistTaskCheckFormulas(param.getTaskPK()));

			TaskVO task=TaskSrvUtils.getTaskVOById(param.getTaskPK());
			MenuStateData taskCommitState=getTaskMenuState(pubData, task,loginEnv);;
			menuData.setCommited(taskCommitState.isCommited());
			menuData.setCanCommit(taskCommitState.isCanCommit());
			menuData.setCanRequestCancelCommit(taskCommitState.isCanRequestCancelCommit());
		}*/
		return ;
	}
}