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
    private List<LevelValue>  canEditMvtypes;//�ڵ�ɱ���ҵ�񷽰�
    //��ҵ�񷽰�Ϊʵ����ʱ��Ԥ����ƺ�Ԥ������ڵ��ܹ��༭
	private boolean isActualCanEdit = false;
	//�ڵ�����
	private String nodeType = null;
	
	
	//by :��־ǿ at��2020-4-30  �������󣺵�Ԥ������ʱ������༭ʵ������Ԫ�񣬵�ʵ��������ʱ������༭Ԥ������Ԫ�񣬵�Ԫ��ҵ�񷽰�ȷ�Ϸ�ʽΪ�������������ݣ����ҵ���ҵ�񷽰����������Ԫ���ҵ�񷽰���
	//���������Ϊ�����Ż�Ч�ʣ�����ÿ�ζ����ҡ�
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
						//�汾��Ϣ�����ڿգ��򸡶��ж��Ƿ�ɱ༭���汾����null��Ϊ�ϰ汾�������ж�
						if(tsDataModel.getParentModel().getObjversion() != null){
							if(cInfo.isReadOnly()){
								return false;
							}
						}
					}
					if(cInfo.getDimVector() != null){
						CellFmlInfo fmlInfo = (CellFmlInfo) cell.getExtFmt(TbIufoConst.formulaKey);
						if (fmlInfo != null) {
							//ȡ����ʽ���ñ༭
							if(fmlInfo.getFmlType()==CellFmlInfo.fmlType_calaction){
								   return false;
							}
						}
					}else{
						if(cInfo.getExVarDef() != null && cInfo.getExVarDef() != null ){
							//��š�ֻ������
							if(cInfo.getExVarDef().cellType == ExVarDef.cellType_readonly
									|| cInfo.getExVarDef().cellType == ExVarDef.cellType_index){
								return false;
							}
							if(cInfo.getVarDimDef()!=null&&cInfo.getExVarAreaDef().isAutoExpandByDataCell==ExVarAreaDef.isAutoExpandByDataCell_ALL)
							    return false;
							if(cInfo.getVarDimDef()!=null&&!cInfo.isNewLine())
								return false;
							//ָ������ �ı�����������ָ������
							if(isIndexApprove){
								if(!(cInfo.getExVarDef().cellType == ExVarDef.cellType_dim)){
									return false;
								}
								
							}
							//by����־ǿ at��2020/05/06  ����ı�������,�ж���һ����û�в�ͬ��ҵ�񷽰�����������Ԫ������༭������ҽ������
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
//	 * �ڵ�ɱ༭ҵ�񷽰�---��ֵ���ڷ����ڵ�ʱ�����ˣ�  liyingm+
//	 * 
//	 * ûֵʱ��ÿ���ڵ�Ĭ�ϵĴ����߼�(�жϵ�Ԫ���ϵ�ҵ�񷽰������ά�ϵ�ҵ�񷽰��Ƿ���ͬ�� ����ά��ҵ�񷽰�������ά�ȵ�ҵ�񷽰��������Ͳ�һ��ʱ���ñ༭��)
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
	 * �жϵ�Ԫ���ϵ�ҵ�񷽰��Ƿ��ڵ�ǰ�ڵ����Ƿ���Ա༭   add  by pengzhena
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
				if(lvVs.getCode().equals(VERSION)/*&&dim.getObjName().endsWith("(����)")*/&&(cInfo.getExcelFormula()==null||cInfo.getExcelFormula().trim().equals(""))){
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
		
		
		
		/*TBPTZ410000000001CMF  ʵ����
		 * TBPTZ410000000001CME yusuan
		TBPTZ410000000008IC1  ����
		TBPTZ410000000008IC2   ������
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
	 * ��ȡ��ǰ�ڵ��code
	 * @return
	 */

	private  String   getCurrNodeType(){
//		String funcCode=PhTbReportDirView.getViewManager().getTbPlanContext().getNodeType();
		return nodeType;
		
	}
}
