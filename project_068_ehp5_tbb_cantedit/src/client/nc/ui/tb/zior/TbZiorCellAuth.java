package nc.ui.tb.zior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.itf.mdm.dim.IDimManager;
import nc.ms.mdm.dim.DimServiceGetter;
import nc.ms.tb.task.data.TCell;
import nc.ms.tb.task.data.TaskSheetDataModel;
import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.ui.tb.dimdoc.constant.DimDocConstant;
import nc.view.tb.form.iufo.TbCellAuth;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.mdm.cube.DataCell;
import nc.vo.mdm.dim.DimDef;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.IDimDefPKConst;
import nc.vo.mdm.dim.IDimLevelCodeConst;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.tb.form.excel.ExVarAreaDef;
import nc.vo.tb.form.excel.ExVarDef;
import nc.vo.tb.form.iufo.CellExtInfo;
import nc.vo.tb.form.iufo.CellFmlInfo;
import nc.vo.tb.form.iufo.TbIufoConst;
import nc.vo.tb.task.MdTask;

import com.ufsoft.table.Cell;
import com.ufsoft.table.CellsModel;

public class TbZiorCellAuth extends TbCellAuth{

	private TaskSheetDataModel tsDataModel = null;
	private boolean isIndexApprove = false;
    private List<LevelValue>  canEditMvtypes;//节点可编制业务方案
    //当业务方案为实际数时，预算编制和预算桌面节点能够编辑
	private boolean isActualCanEdit = false;
	//节点类型
	private String nodeType = null;
	
	
	//by :王志强 at：2020-4-30  二开需求：当预算任务时不允许编辑实际数单元格，当实际数任务时不允许编辑预算数单元格，单元格业务方案确认方式为：查找整列数据，查找到的业务方案就是这个单元格的业务方案。
	//这个属性作为缓存优化效率，不用每次都查找。
	private  Map<String, Boolean> colCanEditCache  = new  HashMap<String, Boolean>();
	
	
	public TbZiorCellAuth(CellsModel csModel, int mode) {
		super(csModel, mode);
	}

	/**
	 * @param csModel
	 * @param mode
	 * @param tsDataModel
	 */
	public TbZiorCellAuth(CellsModel csModel, TaskSheetDataModel tsDataModel,int mode) {
		super(csModel, mode);
		this.tsDataModel = tsDataModel;
	}
	
	
	public TbZiorCellAuth(CellsModel csModel, TaskSheetDataModel tsDataModel,int mode,boolean isIndexApprove) {
		this(csModel,tsDataModel,mode);
		this.isIndexApprove =  isIndexApprove;
	}
	
	public TbZiorCellAuth(CellsModel csModel, TaskSheetDataModel tsDataModel,int mode,boolean isIndexApprove,String nodeType,List<LevelValue> canEditMvtypes) {
		this(csModel,tsDataModel,mode,isIndexApprove);
		this.nodeType = nodeType;
		this.canEditMvtypes=canEditMvtypes;
	}
	public TbZiorCellAuth(CellsModel csModel, TaskSheetDataModel tsDataModel,int mode,boolean isIndexApprove,String nodeType,List<LevelValue> canEditMvtypes,boolean isActualCanEdit) {
		this(csModel,tsDataModel,mode,isIndexApprove,nodeType,canEditMvtypes);
		this.isActualCanEdit = isActualCanEdit;
	}

	public TaskSheetDataModel getTsDataModel() {
		return tsDataModel;
	}
	
	
	private boolean isColumnWritable(int colNum){
		
		
		
		if(tsDataModel != null){
			String key = "col:"+colNum+"|sheet:"+tsDataModel.getShowName();
			if(tsDataModel.getColNum()<=colNum){
				return false; 
				
			}else{
				if(colCanEditCache.get(key)==null){
					for(int i=0;i<tsDataModel.getRowNum();i++){
						TCell tc = tsDataModel.getCellAt(i, colNum);
						if(tc.getDimVector()!=null){
							Cell cell = getCellsModel() == null ? null : getCellsModel().getCell(i,colNum);
							CellExtInfo cInfo = cell==null ? null : (CellExtInfo)cell.getExtFmt(TbIufoConst.tbKey);
							if(!TbZiorUiCtl.isSameMvType(cInfo,nodeType,tsDataModel.getMdTask(),canEditMvtypes,isActualCanEdit)){
								colCanEditCache.put(key, false);
								return false;
							}else{
								colCanEditCache.put(key, true);
								return true;
							}
							
						}
					
					}
					colCanEditCache.put(key, true);
					return true;
					
				}else{
					return colCanEditCache.get(key);
				}
				
//				TCell tc = tsDataModel.getCellAt(row, col);
			}
			
		}
		return true;
		
	}
	
	@Override
	public boolean isWritable(int row, int col) {
		if(super.isWritable(row, col)){
			Cell cell = getCellsModel() == null ? null : getCellsModel().getCell(row, col);
			CellExtInfo cInfo = cell==null ? null : (CellExtInfo)cell.getExtFmt(TbIufoConst.tbKey);
			if (cInfo != null) {
				if (cInfo.getVarId() != null && cInfo.getVarId().length() > 0){
					if(tsDataModel != null && tsDataModel.getParentModel() != null){
						//版本信息不等于空，则浮动判断是否可编辑。版本等于null的为老版本，不做判断
						if(tsDataModel.getParentModel().getObjversion() != null){
							if(cInfo.isReadOnly()){
								return false;
							}
						}
					}
					if(cInfo.getDimVector() != null){
						CellFmlInfo fmlInfo = (CellFmlInfo) cell.getExtFmt(TbIufoConst.formulaKey);
						if (fmlInfo != null) {
							//取数公式不让编辑
							if(fmlInfo.getFmlType()==CellFmlInfo.fmlType_calaction){
								   return false;
							}
						}
					}else{
						if(cInfo.getExVarDef() != null && cInfo.getExVarDef() != null ){
							//序号、只读区域
							if(cInfo.getExVarDef().cellType == ExVarDef.cellType_readonly
									|| cInfo.getExVarDef().cellType == ExVarDef.cellType_index){
								return false;
							}
							if(cInfo.getVarDimDef()!=null&&cInfo.getExVarAreaDef().isAutoExpandByDataCell==ExVarAreaDef.isAutoExpandByDataCell_ALL)
							    return false;
							if(cInfo.getVarDimDef()!=null&&!cInfo.isNewLine())
								return false;
							//指标审批 文本浮动都不让指标审批
							if(isIndexApprove){
								if(!(cInfo.getExVarDef().cellType == ExVarDef.cellType_dim)){
									return false;
								}
								
							}
							//by：王志强 at：2020/05/06  添加文本浮动区,判断这一列有没有不同的业务方案如果有这个单元格不允许编辑，环球医疗需求
							//************start
							if(cInfo.getExVarDef().cellType == ExVarDef.cellType_fd_str||cInfo.getExVarDef().cellType == ExVarDef.cellType_fd_number||cInfo.getExVarDef().cellType == ExVarDef.cellType_strlist){
								return this.isColumnWritable(col);
							}
							//***********End
						}
					}
					
					
				}
			}
			if(tsDataModel != null){
				TCell tc = tsDataModel.getCellAt(row, col);
				if(tc != null &&( tc.getFormula() != null||!tc.isWritable())){
					return false;
				}
				if(!TbZiorUiCtl.isSameMvType(cInfo,nodeType,tsDataModel.getMdTask(),canEditMvtypes,isActualCanEdit)){
					return false;
				}
				
				DataCell dc = (DataCell)cell.getExtFmt(TbIufoConst.dataccellKey);
				if(dc != null && (!dc.canSetValue() || dc.isFlag_Locked())){
					return false;
				}
				if(!checkMvtypeCanEditor(getCurrNodeType(), cInfo)){
					return false;
				}
			}
			return true;
		}
		return false;
	}
//	/**
//	 * 
//	 * 节点可编辑业务方案---有值是在发布节点时配置了，  liyingm+
//	 * 
//	 * 没值时走每个节点默认的处理逻辑(判断单元格上的业务方案与参数维上的业务方案是否相同（ 参数维度业务方案与行列维度的业务方案方案类型不一致时不让编辑）)
//	 */
//	private boolean isSameMvType(CellExtInfo cInfo) {
//		if(nodeType!=null&&(nodeType.equals(ITbPlanActionCode.BALANCE_BACK_NODETYPE)
//				||nodeType.equals(ITbPlanActionCode.BALANCE_SUGGEST_NODETYPE)||
//				nodeType.equals(ITbPlanActionCode.BALANCE_EFFECTIVE_NODETYPE))){
//			return   true;
//		}
//		
//		if(cInfo==null||cInfo.getDimVector()==null){
//			return true;
//		}
//		IDimManager idm = DimServiceGetter.getDimManager();
//		DimLevel dl = idm.getDimLevelByBusiCode(IDimLevelCodeConst.MVTYPE);
//		MdTask task = tsDataModel.getMdTask();
//		String mvType = task.getPk_mvtype();
//		DimDef dimdef = idm.getDimDefByPK(IDimDefPKConst.MVTYPE);
//		LevelValue paramLevelValue = dimdef.getLevelValue(dl, mvType);
//		LevelValue levelValue = cInfo.getDimVector().getLevelValue(dl);
//	    if(canEditMvtypes!=null&&canEditMvtypes.size()>0){
//			if(canEditMvtypes.contains(levelValue))
//	    	{
//				return true;
//	    	}
//	    }else{
//	    	if(levelValue != null && paramLevelValue != null){
//				String prop1 = (String) levelValue.getPropValue(DimDocConstant.DIMDOC_DATAATTR_ATTRTYPE);
//				String prop2 = (String) paramLevelValue.getPropValue(DimDocConstant.DIMDOC_DATAATTR_ATTRTYPE);
//				if(prop1 != null && prop2 != null){
//					return prop1.equals(prop2);
//				}
//			}
//	    }
//		
//	
//		return false;
//		
//	}
	/**
	 * 判断单元格上的业务方案是否在当前节点下是否可以编辑   add  by pengzhena
	 */
	private static  final String VERSION="vp";
	private boolean checkMvtypeCanEditor(String nodeType ,CellExtInfo cInfo) {
		IDimManager idm = DimServiceGetter.getDimManager();
		DimLevel dimlEnt = idm.getDimLevelByBusiCode(IDimLevelCodeConst.ENTITY);
		DimLevel dimlevelMV = idm.getDimLevelByBusiCode(IDimLevelCodeConst.MVTYPE);
		DimLevel dimlevelVs = idm.getDimLevelByBusiCode(IDimLevelCodeConst.VERSION);
		if(StringUtil.isEmptyWithTrim(nodeType))
			return true;
		if(nodeType.equals(ITbPlanActionCode.BALANCE_BACK_NODETYPE)
				||nodeType.equals(ITbPlanActionCode.BALANCE_SUGGEST_NODETYPE)||
				nodeType.equals(ITbPlanActionCode.BALANCE_EFFECTIVE_NODETYPE)){
			if(cInfo != null && cInfo.getDimVector() != null){
				DimMember dim = cInfo.getDimVector().getDimMember(dimlEnt.getDimDef());
				LevelValue lvVs = cInfo.getDimVector().getLevelValue(dimlevelVs);
				if(lvVs.getCode().equals(VERSION)/*&&dim.getObjName().endsWith("(汇总)")*/&&(cInfo.getExcelFormula()==null||cInfo.getExcelFormula().trim().equals(""))){
					return  true;
				}
				LevelValue levelValue = cInfo.getDimVector().getLevelValue(dimlevelMV);
				String lvKey=levelValue.getKey().toString();
				if(nodeType.equals(ITbPlanActionCode.BALANCE_BACK_NODETYPE)){
					if(lvKey.equals("TBPTZ410000000008IC1")
							||lvKey.equals("TBPTZ410000000008IC3")
							||lvKey.equals("TBPTZ410000000001CME")){
						return false;
					}
				}
				 if(nodeType.equals(ITbPlanActionCode.BALANCE_SUGGEST_NODETYPE)
						){
					if(lvKey.equals("TBPTZ410000000008IC2")
							||lvKey.equals("TBPTZ410000000008IC3")
							||lvKey.equals("TBPTZ410000000001CME")){
						return false;
					}
				}
				 if(nodeType.equals(ITbPlanActionCode.BALANCE_EFFECTIVE_NODETYPE)){
						if(lvKey.equals("TBPTZ410000000008IC1")||lvKey.equals("TBPTZ410000000008IC2")
								||lvKey.equals("TBPTZ410000000001CME")){
							return false;
						}
					}
			}
		}
		
		
		
		/*TBPTZ410000000001CMF  实际数
		 * TBPTZ410000000001CME yusuan
		TBPTZ410000000008IC1  建议
		TBPTZ410000000008IC2   反馈数
		TBPTZ410000000008IC3 xiada
		10012710000000015XT8 beizhu*/
		
		return true;
		
	}
	public List<LevelValue> getCanEditMvtypes() {
		return canEditMvtypes;
	}

	public void setCanEditMvtypes(List<LevelValue> canEditMvtypes) {
		this.canEditMvtypes = canEditMvtypes;
	}
	
	public void setActualCanEdit(boolean isActualCanEdit) {
		this.isActualCanEdit = isActualCanEdit;
	}

	/**
	 * 获取当前节点的code
	 * @return
	 */

	private  String   getCurrNodeType(){
//		String funcCode=PhTbReportDirView.getViewManager().getTbPlanContext().getNodeType();
		return nodeType;
		
	}
}
