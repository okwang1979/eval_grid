package nc.ui.tb.zior;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nc.bs.framework.common.NCLocator;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.itf.mdm.dim.IDimManager;
import nc.itf.uap.pf.IWorkflowMachine;
import nc.itf.uap.rbac.IRoleManageQuery;
import nc.ms.mdm.dim.DimServiceGetter;
import nc.ms.mdm.dim.NtbSuperServiceGetter;
import nc.ms.tb.asynchronous.XmlUtils;
import nc.ms.tb.form.FormServiceGetter;
import nc.ms.tb.form.SheetGroupCtl;
import nc.ms.tb.pub.GlobalParameter;
import nc.ms.tb.pub.TbParamUtil;
import nc.ms.tb.task.TbTaskServiceGetter;
import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.ui.tb.dimdoc.constant.DimDocConstant;
import nc.view.tb.form.iufo.TbCellNoteRenderer;
import nc.view.tb.form.iufo.TbColorSheetCellRenderer;
import nc.view.tb.form.iufo.TbDataCellRenderer;
import nc.view.tb.form.iufo.TbStringCellRenderer;
import nc.vo.mdm.dim.DimDef;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.IDimDefPKConst;
import nc.vo.mdm.dim.IDimLevelCodeConst;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pub.pf.workflow.IPFActionName;
import nc.vo.pub.workflownote.WorkflownoteVO;
import nc.vo.tb.form.MdSheet;
import nc.vo.tb.form.MdSheetGroup;
import nc.vo.tb.form.MdSheetGroupM;
import nc.vo.tb.form.iufo.CellExtInfo;
import nc.vo.tb.form.iufo.TbIufoConst;
import nc.vo.tb.plan.AggregatedMdTaskVO;
import nc.vo.tb.task.ITaskStatus;
import nc.vo.tb.task.MdTask;
import nc.vo.tb.task.MdTaskDef;
import nc.vo.tb.wf.SheetVO;
import nc.vo.uap.rbac.role.RoleVO;
import nc.vo.wfengine.core.data.DataField;

import com.ufida.zior.view.Mainboard;
import com.ufsoft.table.ReportTable;

/**
 * 编制UI工具类
 */
public class TbZiorUiCtl {
	
	/**
	 * 初始化渲染器和编辑器
	 *  --- 兼容新旧框架
	 */
	public static void initRender(ITbReportDesigner report) {
		if (report == null)
			return;
		ReportTable table = report.getTable();
		Mainboard mainboard = report.getMainboard();
		if(table == null || mainboard == null)
			return;
		TbPlanContext context = report.getTbPlanContext();
		// 备注信息渲染器 主要用在平衡模块，平衡模块中无数据的单元格显示相应的备注信息
		if (context != null
				&& context.getNodeType() != null
				&& (context.getNodeType().equals(ITbPlanActionCode.BALANCE_SUGGEST_NODETYPE)
						|| context.getNodeType().equals(ITbPlanActionCode.BALANCE_BACK_NODETYPE) || context.getNodeType().equals(
						ITbPlanActionCode.BALANCE_EFFECTIVE_NODETYPE))) {
			table.getReanderAndEditor().registRender(mainboard, TbIufoConst.cellNote, new TbCellNoteRenderer());
		}
		// 从参数中读取设置
		boolean isShowZero = TbParamUtil.isShowZeroCell();
		// 单元格上的数据信息渲染器
		/***********************************************************************/
		table.getReanderAndEditor().registRender(mainboard, String.class, new TbStringCellRenderer/* TbSheetCellRenderer */(isShowZero));
		table.getReanderAndEditor().registRender(mainboard, Double.class, new TbDataCellRenderer/* TbDataCellRenderer */(isShowZero));
		table.getReanderAndEditor().registRender(mainboard, UFDouble.class, new TbDataCellRenderer/* TbDataCellRenderer */(isShowZero));
		// 自定义渲染器(需要时增加)
		table.getReanderAndEditor().registRender(mainboard, TbIufoConst.tbKey, new TbColorSheetCellRenderer/* TbSheetCellRenderer */());
		/***********************************************************************/
		// 对中间国资委表做的特殊处理
		Boolean para = GlobalParameter.getInstance().getPara("isSpeaiclDeal", Boolean.class);
		if (para == null) {
			para = false;
		}
		if (para) {
			table.getReanderAndEditor().registEditor(mainboard, TbIufoConst.tbKey, new ZJTbKeyInputEditor());
		} else {
			table.getReanderAndEditor().registEditor(mainboard, TbIufoConst.tbKey, new TbKeyInputEditor());
		}
				
	}
	/**
	 * 
	 * 节点可编辑业务方案---有值是在发布节点时配置了，  liyingm+
	 * 
	 * 没值时走每个节点默认的处理逻辑(判断单元格上的业务方案与参数维上的业务方案是否相同（ 参数维度业务方案与行列维度的业务方案方案类型不一致时不让编辑）)
	 */
	public static boolean isSameMvType(CellExtInfo cInfo,String nodeType,MdTask task,List<LevelValue>  canEditMvtypes,boolean isActualCanEdit) {
		if(nodeType!=null&&(nodeType.equals(ITbPlanActionCode.BALANCE_BACK_NODETYPE)
				||nodeType.equals(ITbPlanActionCode.BALANCE_SUGGEST_NODETYPE)||
				nodeType.equals(ITbPlanActionCode.BALANCE_EFFECTIVE_NODETYPE))){
			return   true;
		}
		
		if(cInfo==null||cInfo.getDimVector()==null){
			return true;
		}
		IDimManager idm = DimServiceGetter.getDimManager();
		DimLevel dl = idm.getDimLevelByBusiCode(IDimLevelCodeConst.MVTYPE);
		String mvType = task.getPk_mvtype();
		DimDef dimdef = idm.getDimDefByPK(IDimDefPKConst.MVTYPE);
		LevelValue paramLevelValue = dimdef.getLevelValue(dl, mvType);
		LevelValue levelValue = cInfo.getDimVector().getLevelValue(dl);
	    if(canEditMvtypes!=null&&canEditMvtypes.size()>0){
			if(canEditMvtypes.contains(levelValue))
	    	{
				return true;
	    	}
	    }else{
	    	if(nodeType != null && (nodeType.equals(ITbPlanActionCode.COM_NODETYPE) ||
					nodeType.equals(ITbPlanActionCode.TABLEOFTOP_NODETYPE)
							|| nodeType.equals(ITbPlanActionCode.ADJUSTBILL_NODETYPE))){
				//业务方案参数配置为实际数可编辑只针对预算编制和预算桌面节点
				if(isActualCanEdit){
					return true;
				}
			}
	    	if(levelValue != null && paramLevelValue != null){
				String prop1 = (String) levelValue.getPropValue(DimDocConstant.DIMDOC_DATAATTR_ATTRTYPE);
				String prop2 = (String) paramLevelValue.getPropValue(DimDocConstant.DIMDOC_DATAATTR_ATTRTYPE);
				if(prop1 != null && prop2 != null){
					return prop1.equals(prop2);
				}
			}
	    }
		
	
		return false;
		
	}
	
	/**
	 * 获取taskde可见sheet范围 liyingm+
	 *  User2SheetCvsTools.getPkSheets(pk_user,pk_group,pk_责任主体,)，
	 * 返回用户分配职责的表单pk，多个职责设置表单有交叉的话pk有可能重复，
	 * 不过传给TaskDataModel的表单pk重复也没有关系。如没有可适配内容则返回null
	 * 可见表单为空，则打开任务可计算表单范围
	 * isApp=true为审批表单范围，在审批流节点配置
	 * @return
	 */
	public static String[] getTaskValidLookPkSheets(MdTask task, TbPlanContext context, boolean isApp) {
		String[] taskValidLookSheets = null;
		if (task == null)
			return taskValidLookSheets;
		String pk_user = WorkbenchEnvironment.getInstance().getLoginUser().getPrimaryKey();
        String pk_group = WorkbenchEnvironment.getInstance().getGroupVO().getPrimaryKey();
        String pk_org = WorkbenchEnvironment.getInstance().getLoginUser().getPk_org();
		// 发布节点时设置的表单范围
		// 可见表单范围为所有可设置地方的交集
		String[] publicNodePks = (context == null || context.getZiorOpenNodeModel() == null) ? null : context.getZiorOpenNodeModel().getTaskValidLookSheets();
		String[] formSheetPks = null;
		String[] publicAppPks = null;
		try {
			// 套表管理节点设置的表单分组
			formSheetPks = FormServiceGetter.getFormGroupService().getPkSheetsByUserperm(task.getPk_workbook(), pk_user, pk_org, pk_group);
		} catch (BusinessException e) {
			NtbLogger.print(e.getMessage());
		}
		if (context != null && context.getSheetGroupName() != null) {
			String sheetGroupName = context.getSheetGroupName();
			try {
				// 任务管理发布节点设置的范围
				SuperVO[] vos = NtbSuperServiceGetter.getINtbSuper().queryByCondition(MdSheetGroup.class, "pk_workbook='" + task.getPk_workbook() + "' and objname='" + sheetGroupName + "'");
				if (vos != null && vos.length > 0) {
					vos = NtbSuperServiceGetter.getINtbSuper().queryByCondition(MdSheetGroupM.class, "pk_sheetgroup='" + vos[0].getPrimaryKey() + "'");
					if (vos != null && vos.length > 0) {
						String[] pks = new String[vos.length];
						for (int i = 0; i < vos.length; i++)
							pks[i] = ((MdSheetGroupM) vos[i]).getPk_sheet();
						taskValidLookSheets = pks;
					}
				}
			} catch (BusinessException be) {
				return null;
			}
		} else {
			// 任务管理节点设置的范围
			taskValidLookSheets = SheetGroupCtl.getPkSheetsByTaskSheetList(task.getSheetlist(), true);

		}
		publicAppPks = getDefaultPkSheets(task);
		if (publicNodePks != null && publicNodePks.length > 0) {
			List<String> pks = new ArrayList<String>();
			List<String> lookPks = new ArrayList<String>();
			pks.addAll(Arrays.asList(publicNodePks));
			if (taskValidLookSheets != null && taskValidLookSheets.length > 0) {
				for (String k : taskValidLookSheets) {
					if (pks.contains(k)) {
						lookPks.add(k);
					} else {
						continue;
					}
				}
				if (lookPks.size() > 0)
					taskValidLookSheets = lookPks.toArray(new String[0]);
				else
					taskValidLookSheets = null;
			} else {
				taskValidLookSheets = publicNodePks;
			}
		}
		if (formSheetPks != null && formSheetPks.length > 0) {

			List<String> pks = new ArrayList<String>();
			List<String> lookPks = new ArrayList<String>();
			pks.addAll(Arrays.asList(formSheetPks));
			if (taskValidLookSheets != null && taskValidLookSheets.length > 0) {
				for (String k : taskValidLookSheets) {
					if (pks.contains(k)) {
						lookPks.add(k);
					} else {
						continue;
					}
				}
				if (lookPks.size() > 0)
					taskValidLookSheets = lookPks.toArray(new String[0]);
				else
					taskValidLookSheets = null;
			} else {
				taskValidLookSheets = formSheetPks;
			}

		}
		if (publicAppPks != null && publicAppPks.length > 0) {

			List<String> pks = new ArrayList<String>();
			List<String> lookPks = new ArrayList<String>();
			pks.addAll(Arrays.asList(publicAppPks));
			if (taskValidLookSheets != null && taskValidLookSheets.length > 0) {
				for (String k : taskValidLookSheets) {
					if (pks.contains(k)) {
						lookPks.add(k);
					} else {
						continue;
					}
				}
				if (lookPks.size() > 0)
					taskValidLookSheets = lookPks.toArray(new String[0]);
				else
					taskValidLookSheets = null;
			} else {
				taskValidLookSheets = publicAppPks;
			}

		}
		return taskValidLookSheets;
	}

	/**
	 * 得到审批中的任务表单范围 {方法功能中文描述}
	 * 
	 * @return
	 * @author: hubina@yonyou.com
	 */
	public static String[] getDefaultPkSheets(MdTask task) {
		String[] pk_sheets = null;
		if (task != null && ITaskStatus.APPROVING.equals(task.getPlanstatus())) {
			try {
				AggregatedMdTaskVO billvo = new AggregatedMdTaskVO();
				billvo.setParentVO(task);
				MdTaskDef taskDef = TbTaskServiceGetter.getTaskObjectService().getMdTaskDefByPk(task.getPk_taskdef());
				String billType = taskDef.getPk_transtype();

				IWorkflowMachine wfMachine = NCLocator.getInstance().lookup(IWorkflowMachine.class);
				WorkflownoteVO wfNoteVo = wfMachine.checkWorkFlow(IPFActionName.APPROVE, billType, billvo, null);
				List list = wfNoteVo.getApplicationArgs();
				for (int i = 0; i < list.size(); i++) {
					DataField field = (DataField) list.get(i);
					ArrayList<SheetVO> sheetVos = (ArrayList<SheetVO>) XmlUtils.fromXML(field.getInitialValue());
					for (SheetVO sVo : sheetVos) {
						boolean isMatch = false;
						if (sVo.getUserId() != null) {
							if (sVo.getUserId().equals(WorkbenchEnvironment.getInstance().getLoginUser().getPrimaryKey()))
								isMatch = true;
						}
						if (sVo.getRoleId() != null && !isMatch) {
							IRoleManageQuery roleBS = (IRoleManageQuery) NCLocator.getInstance().lookup(IRoleManageQuery.class.getName());
							RoleVO[] roles = roleBS.queryRoleByUserID(WorkbenchEnvironment.getInstance().getLoginUser().getPrimaryKey(),
									WorkbenchEnvironment.getInstance().getLoginUser().getPk_org());
							if (roles != null) {
								for (int j = 0; j < roles.length; j++) {
									if (sVo.getRoleId().equals(roles[i].getPrimaryKey())) {
										isMatch = true;
										break;
									}
								}
							}
						}
						if (isMatch) {
							List<MdSheet> sheetList = sVo.getSheetList();
							if (sheetList != null) {
								pk_sheets = new String[sheetList.size()];
								for (int j = 0; j < pk_sheets.length; j++)
									pk_sheets[j] = sheetList.get(j).getPrimaryKey();
							}
							break;
						}
					}
				}
			} catch (Throwable t) {
				NtbLogger.printException(t);
				pk_sheets = null;
			}
		}
		if (pk_sheets != null && pk_sheets.length == 0)
			pk_sheets = null;
		return pk_sheets;
	}
}
