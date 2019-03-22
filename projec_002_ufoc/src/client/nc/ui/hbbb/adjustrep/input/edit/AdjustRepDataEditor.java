package nc.ui.hbbb.adjustrep.input.edit;

import java.util.HashMap;
import java.util.Map;

import nc.bs.logging.Logger;
import nc.itf.hbbb.convertrule.IConvertRuleConstants;
import nc.itf.hbbb.workdrafttemp.IWorkDraftConst;
import nc.ui.hbbb.adjustrep.input.control.AdjRepDataControler;
import nc.ui.iufo.constants.IUfoeActionCode;
import nc.ui.iufo.input.control.base.AbsRepDataControler;
import nc.ui.iufo.input.ufoe.comp.AbsCombRepDataEditor;
import nc.ui.iufo.input.ufoe.comp.AbsRepDataEditorInComb;
import nc.util.hbbb.HBVersionUtil;
import nc.util.hbbb.datacenter.DataCenterType;
import nc.util.hbbb.workdraft.pub.IWorkDraft;
import nc.util.hbbb.workdraft.pub.ReportType;
import nc.util.iufo.funcpermission.FuncPermissionCheckUtil;
import nc.vo.hbbb.adjustscheme.AdjustSchemeVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.meetaccount.RelaFormulaObj;
import nc.vo.iufo.balance.BalanceCondVO;
import nc.vo.iufo.data.IKeyDetailData;
import nc.vo.iufo.data.MeasureDataUtil;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.uap.rbac.FuncSubInfo;

import com.ufida.zior.console.ActionHandler;
import com.ufida.zior.context.ComContextKey;
import com.ufsoft.iufo.check.vo.CheckResultVO;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iuforeport.repdatainput.RepDataOperResultVO;
import com.ufsoft.iuforeport.tableinput.applet.IRepDataParam;
import com.ufsoft.table.CellPosition;
import com.ufsoft.table.UFOTable;
 
/**
 * 合并各数据中心报表页签，包括折算中心
 * @modified by jiaah at 2012-1-4 同步60
 *
 */
@SuppressWarnings({ "serial", "restriction" })
public class AdjustRepDataEditor extends AbsRepDataEditorInComb {
	
	private Map<String,HBSchemeVO>  hbschemeMap = new HashMap<String,HBSchemeVO>();
	
	private Map<String,AdjustSchemeVO>  adjSchemeVOMap = new HashMap<String,AdjustSchemeVO>();
	
	public AdjustRepDataEditor(IWorkDraft newworkdraft,AbsCombRepDataEditor parentEditor){
		super(newworkdraft.getPk_report(),parentEditor);
		this.workdraft=newworkdraft;
	}  
	
	protected IWorkDraft workdraft;

	@Override
	protected RepDataOperResultVO innerOpenRepDataResult(String strBalCondPK) {
		Object attribute = this.getMainboard().getContext().getAttribute(RelaFormulaObj.MEASURE_PK);
		MeasureVO measureVo= null;
		RepDataOperResultVO vo = null;
		if(AbsRepDataControler.getInstance(getMainboard()) instanceof AdjRepDataControler){
			AdjRepDataControler controler=(AdjRepDataControler)AbsRepDataControler.getInstance(getMainboard());
			String pk_convertrule=(String) this.getMainboard().getContext().getAttribute(IConvertRuleConstants.PK_CONVERTRULE);
			if(attribute!=null )
				measureVo=(MeasureVO) attribute;
			if(null!=pk_convertrule && pk_convertrule.trim().length()>0){
				workdraft.setPk_convertrule(pk_convertrule);
			}
			DataCenterType datatype= (DataCenterType) getContext().getAttribute(DataCenterType.HBBB_DATACENTER);
			Object[] objParams={getRepDataParam(),controler.getLoginEnv(getMainboard()),strBalCondPK,null,controler.isBFreeTotal(),workdraft,measureVo,datatype.ordinal()};
			vo = (RepDataOperResultVO) ActionHandler.execWithZip(this.getWorkdraft().getEditor()/*HBBBTableInputActionHandler.class.getName()*/,"proxyopenRepData",objParams);
		}
		return vo;
	}
	
	
	public Object[] getRemotParam(){
		
		
		Object attribute = this.getMainboard().getContext().getAttribute(RelaFormulaObj.MEASURE_PK);
		MeasureVO measureVo= null;
		RepDataOperResultVO vo = null;
		if(AbsRepDataControler.getInstance(getMainboard()) instanceof AdjRepDataControler){
			AdjRepDataControler controler=(AdjRepDataControler)AbsRepDataControler.getInstance(getMainboard());
			String pk_convertrule=(String) this.getMainboard().getContext().getAttribute(IConvertRuleConstants.PK_CONVERTRULE);
			if(attribute!=null )
				measureVo=(MeasureVO) attribute;
			if(null!=pk_convertrule && pk_convertrule.trim().length()>0){
				workdraft.setPk_convertrule(pk_convertrule);
			}
			DataCenterType datatype= (DataCenterType) getContext().getAttribute(DataCenterType.HBBB_DATACENTER);
			Object[] objParams={getRepDataParam(),controler.getLoginEnv(getMainboard()),"",null,controler.isBFreeTotal(),workdraft,measureVo,datatype.ordinal()};
			return objParams;
		}
		
		Object[] objParams={getRepDataParam(),null,"",null,null,workdraft,measureVo,null};
		return objParams;
		
		
		 
		
	}

	@Override
	public void reInitContent(boolean bOpenData) {
		super.reInitContent(bOpenData);
		
		// 所有底稿不允许修改
		if (workdraft.isdraft()) {
		    getContext().setAttribute(IUfoContextKey.DATA_RIGHT, IUfoContextKey.RIGHT_DATA_READ);
		    UFOTable table = getTable();
		    if(bOpenData && !table.isFreezing()){
		    	  //冻结底稿
		    	CellPosition anchor = null;
				anchor = CellPosition.getInstance(5, 1);//表从第5行开始冻结
				//动态表底稿锁定第6行,第2列-modified by jiaah at 20130619
			    if(ReportType.HB_DYN1_DRAFT.equals(workdraft.getReporttype()) || ReportType.HB_DYN0_DRAFT.equals(workdraft.getReporttype())){
			    	anchor = CellPosition.getInstance(6, 2);
			    }else{
			    	if((workdraft.getWorkDraftTempVO() != null && 
				    		workdraft.getWorkDraftTempVO().getDxshowtype() == IWorkDraftConst.DXSHOWTYPE_DX_NORMAL) ){
				    	anchor = CellPosition.getInstance(6, 1);//分类抵销借贷；锁定第6行
				    }
			    }
				if (table.getSeperateRow() == 0 && table.getSeperateCol() == 0) {
					table.setFrozenNoSplit(true);
					table.setSeperatePos(anchor.getRow(), anchor.getColumn());
				}
				table.setFreezing(true);
		    }
		}
		
		// 合并调整中心的合并报表页签不允许修改
		DataCenterType datatype= (DataCenterType) getContext().getAttribute(DataCenterType.HBBB_DATACENTER);
		if (datatype.equals(DataCenterType.HB_ADJUST) && workdraft.getReporttype().equals(ReportType.HB)){
			getContext().setAttribute(IUfoContextKey.DATA_RIGHT, IUfoContextKey.RIGHT_DATA_READ);
		}
		
		// 合并中心的合并页签可以修改
		//@editted by zhoushuang at 2015.3.12  需求更改-山东绿叶专项-調整表可以修改
		if (datatype.equals(DataCenterType.SEP_ADJUST) && workdraft.getReporttype().equals(ReportType.SEP_ADJ)){
            getContext().setAttribute(IUfoContextKey.DATA_RIGHT, IUfoContextKey.RIGHT_DATA_WRITE);
        }
		if (datatype.equals(DataCenterType.HB_ADJUST) && workdraft.getReporttype().equals(ReportType.HB_ADJ)){
            getContext().setAttribute(IUfoContextKey.DATA_RIGHT, IUfoContextKey.RIGHT_DATA_WRITE);
        }
		if (datatype.equals(DataCenterType.HB) && workdraft.getReporttype().equals(ReportType.HB)){
            getContext().setAttribute(IUfoContextKey.DATA_RIGHT, IUfoContextKey.RIGHT_DATA_WRITE);
        }
		
		// 个别调整表，合并调整表和折算表不允许修改
		//@editted by zhoushuang at 2015.3.12  需求更改-山东绿叶专项-調整表可以修改
		 if ( workdraft.getReporttype().equals(ReportType.CONVERT)) {
	            getContext().setAttribute(IUfoContextKey.DATA_RIGHT, IUfoContextKey.RIGHT_DATA_READ);
	    }
	}

//	@Override 
	public void setRepCheckResult(CheckResultVO arg0) {
		
	}

	@Override
	public IRepDataParam getRepDataParam(){
		IRepDataParam repDataParam = super.getRepDataParam();
		MeasurePubDataVO pubData = repDataParam.getPubData();
		// @edit by zhoushuang at 2015-9-25,上午9:56:24 空指针，不稳定重现，加个判空处理吧。。
		if (pubData != null) {
			IKeyDetailData keyDataByPK = pubData.getKeyDataByPK(KeyVO.CORP_PK);
			String repOrgPK = repDataParam.getRepOrgPK();
			if(StringUtil.isEmptyWithTrim(repOrgPK)){
				if(keyDataByPK!=null)
					repDataParam.setRepOrgPK(keyDataByPK.getValue());
			}
			try {
				HBSchemeVO hbScheme = workdraft.getHbSchemevo();
				if(workdraft.getReporttype().equals(ReportType.HB)){
					pubData.setVer(hbScheme.getVersion());
				}else if(workdraft.getReporttype().equals(ReportType.SEP_ADJ)){
					pubData.setVer(workdraft.getAdjVersion());
				}else if(workdraft.getReporttype().equals(ReportType.HB_CONTRAST)){
					pubData.setVer(HBVersionUtil.getHBContrastByHBSchemeVO(hbScheme));
				}else if(workdraft.getReporttype().equals(ReportType.HBDIFF)){
					pubData.setVer(HBVersionUtil.getDiffByHBSchemeVO(hbScheme));
				}else{
					pubData.setVer(workdraft.getVersion(hbScheme,0));
				}
				pubData.setAloneID(null);
				boolean isAllKeyData = true;
				String[] keysvalues = pubData.getKeywords();
				if(keysvalues != null && keysvalues.length > 0){
					for(String s : keysvalues){
						if(s == null){
							isAllKeyData = false;
							break;
						}
					}
				}
				if(isAllKeyData)
					pubData.setAloneID(MeasureDataUtil.getAloneID(pubData));//不用远程调用
				repDataParam.setAloneID(pubData.getAloneID());
				
			} catch (Exception e) {
				Logger.error(e.getMessage(), e);
			}
		}else {
			repDataParam.setAloneID(null);
		}
		//设置合并体系主键
		AdjRepDataControler controle = (AdjRepDataControler)AbsRepDataControler.getInstance(getMainboard());
		//null异常 ，不知道怎么点出来的，加个判空。。。。
		if(controle != null){
			repDataParam.setRepMngStructPK(controle.getSelectedHBRepStruPK());
		}

		return repDataParam;
	}
	
	
	@Override
	public boolean isDirty() {
		// @edit by zhoushuang at 2015-3-26,下午5:00:14  没有保存权限的情况下，不触发保存，一律返回false
		FuncSubInfo funcSubInfo = (FuncSubInfo)getContext().getAttribute(ComContextKey.FUNC_NODE_INFO);
		if(!FuncPermissionCheckUtil.checkAllActionPermission(new String[]{IUfoeActionCode.FILE_SAVECURR}, funcSubInfo)){
			return false;
		}
		
		if (getMenuState() != null && !getMenuState().isRepCanModify())
			return false;

		IRepDataParam param = getRepDataParam();
		if (param == null || param.getAloneID() == null)
			return false;

		if (m_parentEditor.getBalCondPK() != null && !m_parentEditor.getBalCondPK().equals(BalanceCondVO.NON_SW_DATA_COND_PK))
			return false;
		if(workdraft.getReporttype().equals(ReportType.SEP_ADJ)||workdraft.getReporttype().equals(ReportType.HB_ADJ)||workdraft.getReporttype().equals(ReportType.CONVERT)
				||workdraft.getReporttype().equals(ReportType.HB)){
			if(getCellsModel() != null && getCellsModel().isDirty()){
				return true;
			}
	
		}
		return false;
	}

	public IWorkDraft getWorkdraft() {
		return workdraft;
	}

	public Map<String, HBSchemeVO> getHbschemeMap() {
		return hbschemeMap;
	}

	public Map<String, AdjustSchemeVO> getAdjSchemeVOMap() {
		return adjSchemeVOMap;
	}
	
}
