package nc.ui.tb.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JComponent;

import nc.bs.framework.common.NCLocator;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.itf.tb.cell.ICellInfoChangedListener;
import nc.itf.uif.pub.IUifService;
import nc.ms.mdm.dim.DimServiceGetter;
import nc.ms.tb.formula.core.CubeHelper;
import nc.ms.tb.pub.TbParamUtil;
import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.ui.bd.ref.RefInitializeCondition;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.tb.model.TBDataCellRefModel;
import nc.ui.tb.zior.TbMemberFilterUtil;
import nc.uif.pub.exception.UifException;
import nc.vo.mdm.cube.CubeDef;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.IDimLevelCodeConst;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.mdm.dim.MeasureUtil;
import nc.vo.mdm.integration.imp.MeasureTypeInfo;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.para.SysInitVO;
import nc.vo.tb.form.excel.ExVarDef;
import nc.vo.tb.formula.TbbExpensesBVO;
import nc.vo.tb.formula.TbbExpensesVO;
import nc.vo.tb.obj.LevelValueOfDimLevelVO;
import nc.vo.tb.task.MdTask;
import nc.vo.tb.task.MdTaskDef;

import com.ufsoft.table.re.IInputEditor;

@SuppressWarnings("restriction")
public class TbVarInputEditor  implements IInputEditor {

	private UIRefPane refPane=null;
	private ICellInfoChangedListener listener;
	private String cubeCode;
	private MdTask task;
	//private Map<DimLevel, LevelValue> levelValueMap = null;
	private String parentKey = null;
	private List<String> levelStrList = null;
	private Map<DimLevel, LevelValue> dvMap = new HashMap<DimLevel, LevelValue>();
	
	
//	private static Set<String> useTasks = new HashSet<>();
//	
//	private static String cnf = null;
//	private static String cnfType = null;
//	
//	private static String measure = null;
//	
//	static{
//		IUifService service = NCLocator.getInstance().lookup(IUifService.class);
//	    try {
//	    	//查询任务
//			SysInitVO[] svos = (SysInitVO[]) service.queryByCondition(SysInitVO.class, "initcode = 'TBB_TASK'");
//			
//			if(svos!=null&&svos.length>0){
//				SysInitVO sVo  = svos[0];
//				String taskNameDef = sVo.getValue();
//				String[] taskNames = taskNameDef.split(",");
//				StringBuffer sb = new StringBuffer();
//				for(String taskName:taskNames){
//					sb.append("'").append(taskName).append("'").append(",");
//					
//				}
//				sb.delete(sb.length()-1, sb.length());
//				MdTaskDef[] defs =  (MdTaskDef[])service.queryByCondition(MdTaskDef.class,"objname in ("+sb.toString()+")");
//				if(defs!=null&&defs.length>0){
//					for(MdTaskDef def:defs){
//						useTasks.add(def.getPk_obj());
//					}
//					
//					
//				}else{
//					NtbLogger.error("未查询到,对应的任务定义,"+sb.toString());
//				}
//			}
//			
//			
//			
//			svos = (SysInitVO[]) service.queryByCondition(SysInitVO.class, "initcode = 'TBB_CNF'");
//			
//			if(svos!=null&&svos.length>0){
//				SysInitVO sVo  = svos[0];
//				cnf = sVo.getValue();
//			 
//			}
//			
//			svos = (SysInitVO[]) service.queryByCondition(SysInitVO.class, "initcode = 'CNF_TYPE'");
//			
//			if(svos!=null&&svos.length>0){
//				SysInitVO sVo  = svos[0];
//				cnfType = sVo.getValue();
//			 
//			}
//			
//		svos = (SysInitVO[]) service.queryByCondition(SysInitVO.class, "initcode = 'TB_MEASURE'");
//			
//			if(svos!=null&&svos.length>0){
//				SysInitVO sVo  = svos[0];
//				measure = sVo.getValue();
//			 
//			}
//			
//	  
//		} catch (Exception e) {
//			 NtbLogger.error(new BusinessRuntimeException("得到规则成员"+e.getMessage()));
//		}
//		
//		
//	}

	public TbVarInputEditor(List<LevelValue> lvs,ExVarDef exVarDef,String cubeCode,ICellInfoChangedListener listener,MdTask task ,String parentKey,List<String> levelStrList,Map<DimLevel, LevelValue> dvMap){
		this.listener = listener;
		this.task = task;
		this.parentKey = parentKey;
		this.levelStrList = levelStrList;
		this.dvMap = dvMap;
		getRefPane(lvs,cubeCode,exVarDef);
	}
	
	 

	public UIRefPane getRefPane(List<LevelValue> lvs,String cubeCode,ExVarDef exVarDef) {
		if(refPane == null){
//			refPane = new DatacellRefPane(cubeCode,new LevelValueOfDimLevelVO[]{ new LevelValueOfDimLevelVO(-1,dimLevelCode,levelValueList == null?null:levelValueList.toArray(new String[0]),lvs)},false);
			refPane = new UIRefPane("维度选择");/*-=notranslate=-*/
			refPane.setMultiSelectedEnabled(false);
			refPane.setEditable(true);
			refPane.setFilterDlgShow(true);
			//refPane.setMultiOrgSelected(true);

			if( !ITbPlanActionCode.NOTSUPPORT.equals(TbParamUtil.isStartDeptByOrg())&&IDimLevelCodeConst.DEPT.equals(exVarDef.dimLevelCode)){
				refPane.setMultiCorpRef(true);
			}
			refPane.setRefEditable(true);
			TBDataCellRefModel tBDataCellRefModel = (TBDataCellRefModel)refPane.getRefModel();

			LevelValueOfDimLevelVO levelValueOfDimLevelVO = new LevelValueOfDimLevelVO(-1,exVarDef.dimLevelCode,exVarDef.levelValueList == null?null:exVarDef.levelValueList.toArray(new String[0]),lvs,task);
			String pk_user = WorkbenchEnvironment.getInstance().getLoginUser().getPrimaryKey();
			String pk_group = WorkbenchEnvironment.getInstance().getGroupVO().getPrimaryKey();
			try {
				initTBDataCellRefModel(tBDataCellRefModel, levelValueOfDimLevelVO, pk_user, pk_group, cubeCode, exVarDef, parentKey);
			} catch (BusinessException e) {
				NtbLogger.print(e);
			}
			if(IDimLevelCodeConst.MEASURE.equals(exVarDef.dimLevelCode)){
				if(exVarDef.mesType != null && MeasureUtil.getMeasureName(exVarDef.mesType.name()) != null){
					tBDataCellRefModel.setDisplayDocName(exVarDef.mesType.name());
				}else{
					tBDataCellRefModel.setDisplayDocName(MeasureTypeInfo.tb_budgetsub.name());
					refPane.setMultiCorpRef(true);//如果过滤参照要显示，并且参照要设置为单选，则此设置为True,isMultiOrgSelected设置为false就可以
					refPane.setMultiOrgSelected(false);
				}
			}

		}
		return refPane;
	}

	/**
	 * @param tBDataCellRefModel
	 * @param levelValueOfDimLevelVO
	 * @param pk_user
	 * @param pk_group
	 * @param cubeCode
	 * @param exVarDef
	 * @param parentKey
	 * @throws BusinessException
	 */
	private void initTBDataCellRefModel(TBDataCellRefModel tBDataCellRefModel,LevelValueOfDimLevelVO levelValueOfDimLevelVO,
				String pk_user, String pk_group,String cubeCode, ExVarDef exVarDef,String parentKey)throws BusinessException{
		try {
			List<DimMember> dimMembers = TbMemberFilterUtil.getDimMembers( tBDataCellRefModel, levelValueOfDimLevelVO,
				 pk_user,  pk_group, cubeCode,  exVarDef, parentKey,this.getDvMap(),this.task.getPk_taskdef(),this.task.getPk_planent()) ;
			
//			List<DimMember> dimMembers = DimServiceGetter.getVarMemberService().getVarMemberByTask(new LevelValueOfDimLevelVO[]{levelValueOfDimLevelVO}, cubeCode,pk_user,pk_group,exVarDef.mesType == null ? null:new String[]{exVarDef.mesType.name()},parentKey,dvMap);
			
			
			tBDataCellRefModel.setDimMembers(dimMembers);
			DimLevel dimlevel = DimServiceGetter.getDimManager().getDimLevelByBusiCode(levelValueOfDimLevelVO.dimlevelCode);
			tBDataCellRefModel.setRootName(dimlevel.getObjName());
			tBDataCellRefModel.setCanSelectLevelCode(levelValueOfDimLevelVO.dimlevelCode);
			tBDataCellRefModel.setTableName(levelValueOfDimLevelVO.dimlevelCode);
			tBDataCellRefModel.setRaradims(levelValueOfDimLevelVO, cubeCode,pk_user,pk_group,exVarDef,parentKey,dvMap);
			if(IDimLevelCodeConst.MEASURE.equals(exVarDef.dimLevelCode)){
				tBDataCellRefModel.setFilterRefNodeName(new String[] { nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_bean_0","01050bean002-0000")/*@res "指标档案"*/ });
				RefInitializeCondition condition = refPane.getRefUIConfig().getRefFilterInitconds()[0];
				String[] pks = {};
				condition.setFilterPKs(pks);
				refPane.getRefUIConfig().setRefFilterInitconds(new RefInitializeCondition[] { condition });
				
			}else if(IDimLevelCodeConst.DEPT.equals(exVarDef.dimLevelCode)){
				tBDataCellRefModel.setFilterRefNodeName(new String[] { nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_bean_0","01050bean002-0001")/*@res "业务单元"*/ });
				RefInitializeCondition condition = refPane.getRefUIConfig().getRefFilterInitconds()[0];
		        condition.setDefaultPk(levelValueOfDimLevelVO.task.getPk_dataent());
		        try {
					List<String> pks=DimServiceGetter.getIBDModeService().getCommsionOrg(task.getPk_dataent());
					if(pks!=null&&pks.size()>0){
						condition.setFilterPKs(pks.toArray(new String[0]));
					}
				   } catch (BusinessException e) {
					NtbLogger.print(e.getMessage());
				   }
		        refPane.getRefUIConfig().setRefFilterInitconds(new RefInitializeCondition[] { condition });

			}

		} catch (Exception e) {
			NtbLogger.print(e);
		}
	}
	


	@Override
	public JComponent getComponent() {
		return refPane;
	}

	@Override
	public String getValue() {
		Object objs =  refPane.getRefModel().getSelectedData();
		if(objs != null && objs instanceof Vector){
			Vector vector = (Vector)objs;
			if(vector.size() > 0){
				Vector vct = (Vector)vector.get(0);
				if(vct.size() > 0 && vct.size() > 5){
					String showCode = (String)vct.get(0);
					String showName = (String)vct.get(1);
					DimMember dimMember = (DimMember)vct.get(5);
					if(listener!=null){
						listener.cellDataChanged(dimMember);
					}

					return showName;
				}
			}
		}
		return null;
//		Object[] objs =  refPane.getSelectedBusiObjs();
//		if(objs!=null&&objs.length==1){
//			Object obj = objs[0];
//			String showText = null;
//			if(obj instanceof LevelValue)
//			{
//				LevelValue lv = (LevelValue)obj;
//				showText = lv.getName();
//			}
//			else if(obj instanceof DimMember){
//				DimMember dimMember=(DimMember)obj;
//				showText = dimMember.getObjName();
//			}
//			else{
//				showText = obj == null?"":(String)obj;
//			}
//			refPane.setText(showText);
//			if(listener!=null)
//				listener.cellDataChanged(obj);
//			return showText;
//		}else
//		return null;

	}

	@Override
	public void setValue(String newValue) {
		refPane.setValue(newValue);
	}

	@Override
	public boolean isReturValIDName() {
		return true;
	}

	public Map<DimLevel, LevelValue> getDvMap() {
		return dvMap;
	}

	public void setDvMap(Map<DimLevel, LevelValue> dvMap) {
		this.dvMap = dvMap;
	}
}