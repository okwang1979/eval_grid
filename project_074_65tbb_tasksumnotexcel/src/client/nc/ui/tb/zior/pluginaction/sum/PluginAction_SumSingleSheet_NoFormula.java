package nc.ui.tb.zior.pluginaction.sum;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import nc.desktop.ui.WorkbenchEnvironment;
import nc.itf.mdm.cube.ICubeDefQueryService;
import nc.itf.mdm.permission.INtbPerm;
import nc.itf.mdm.permission.INtbPermConst;
import nc.itf.tb.limit.permission.IDataPermGetter;
import nc.ms.mdm.cube.CubeServiceGetter;
import nc.ms.mdm.limit.DataPermGetterUtil;
import nc.ms.tb.form.FormServiceGetter;
import nc.ms.tb.form.SheetGroupCtl;
import nc.ms.tb.plan.SumTaskStateCheckTool;
import nc.ms.tb.task.TbTaskCtl;
import nc.ms.tb.task.data.TaskDataModel;
import nc.ms.tb.zior.vo.ITbPlanActionCode;
import nc.ms.tb.zior.vo.TbActionName;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.beans.UIDialog;
import nc.ui.tb.zior.TbPlanFrameUtil;
import nc.ui.tb.zior.TbReportDirView;
import nc.ui.tb.zior.TbZiorUiCtl;
import nc.ui.tb.zior.pluginaction.AbstractTbRepPluginAction;
import nc.ui.tb.zior.pluginaction.TbPluginActionDescriptor;
import nc.view.tb.plan.TbPlanListPanel;
import nc.vo.mdm.cube.CubeDef;
import nc.vo.mdm.cube.CubeDimUsage;
import nc.vo.mdm.dim.DimDefType;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.pub.NtbEnv;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.tb.form.MdArea;
import nc.vo.tb.form.MdSheet;
import nc.vo.tb.form.MdWorkbook;
import nc.vo.tb.obj.UserLoginVO;
import nc.vo.tb.task.ITaskConst;
import nc.vo.tb.task.ITaskStatus;
import nc.vo.tb.task.MdTask;

import com.ufida.zior.plugin.PluginKeys.XPOINT;
/**
 * 汇总
 * @author pengzhena
 *
 */
@SuppressWarnings("restriction")
public class PluginAction_SumSingleSheet_NoFormula extends AbstractTbRepPluginAction {
	private MdTask [] tasks=null;
	private SumSingleSheetDlg sumDlg =null;
	private TaskDataModel taskDataModel=null;
	private Map<DimMember, MdTask> allValidDimMembers =null;
	//private boolean ShowProcess=false;
	private boolean  isAutoSumCube=false;
	private ArrayList<MdTask> updStatusTask;

	public PluginAction_SumSingleSheet_NoFormula(String name, String code) {
		super("无公式汇总", code);
	}

	@Override
	public TbPluginActionDescriptor getTbPluginActionDescriptor() {
		TbPluginActionDescriptor  desc =new TbPluginActionDescriptor();
		desc.setName("无公式汇总"); //汇总
		desc.setGroupPaths(TbActionName.getName_SumAnalyze());
		desc.setExtensionPoints(new XPOINT[]{XPOINT.MENU,XPOINT.TOOLBAR});
		desc.setIcon(ITbPlanActionCode.SUM_ICON);
		return desc;
	}

	@Override
	public void actionPerformed(ActionEvent actionevent)throws  BusinessException {
		allValidDimMembers =  getContext().getZiorOpenNodeModel().getValidOrgDimMemberMap();
		tasks=getContext().getTasks();
		isAutoSumCube=isAutoSumTask(tasks[0].getPk_obj());

		//yuyonga
		if(!NtbEnv.isOutLineUI){
			try{
				checkTaskPrv(tasks[0], UserInfo.getUserLoginVO(), getContext().getNodeType());
			}catch(BusinessException be){
				MessageDialog.showErrorDlg(this.getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*提示*/, be.getMessage());
				return;
			}
		}

		List <String>  states  =new ArrayList<String>();
		states.add(tasks[0].getManagestatus());
		states.add(tasks[0].getPlanstatus());
		String  ret = SumTaskStateCheckTool.checkUnValidMessage(states);
		if(ret!=null&&!ret.equals("")){
			MessageDialog.showErrorDlg(this.getMainboard(),nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0002")/*@res "提示"*/, ret );
			return;
		}

		if (isAutoSumCube) {
			//对已经设置自动汇总的任务提示
			int result = MessageDialog.showYesNoDlg(getMainboard(),nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0002")/*@res "提示"*/,nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0404")/*@res "当前任务开启了主体自动汇总，手动汇总仅对浮动行有效，继续汇总?"*/);
			if(MessageDialog.ID_YES== result){
				goOnSum();
			}
		}else{
			goOnSum();
		}
	}

	private void checkTaskPrv(MdTask diftask, UserLoginVO userVo, String key_nodeType)throws BusinessException {
		int type = INtbPermConst.TYPE_BUDGETTASK;// 预算任务资源实体类型
		String info = NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000436")/*编制*/;
		String[] opercodes = new String[] {INtbPermConst.RES_OPERCODE_PLAN };
		if(ITbPlanActionCode.DIRECTADJUST_NODETYPE.equals(key_nodeType)){
			opercodes = new String[] {INtbPermConst.RES_OPERCODE_ADJUST };
			info = NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000438")/*调整*/;
		}
		String pk_group = WorkbenchEnvironment.getInstance().getGroupVO().getPrimaryKey();
		List<MdTask> list = new ArrayList<MdTask>();
		list.add(diftask);
		Set<MdTask> tasks_filtered = filterObjectsByPrv(list, type, opercodes, userVo.getPk_user(), pk_group);
		if (tasks_filtered == null || tasks_filtered.size() != list.size()) {
			throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000705", null, new String[]{info})/*没有{0}权限*/);
		}
	}

	private <T extends INtbPerm> Set<T> filterObjectsByPrv(List<T> objlist, int type, String[] openCode, String user, String pk_group) throws BusinessException {
		if (objlist == null || objlist.size() == 0) {
			return null;
		}
		IDataPermGetter data = DataPermGetterUtil.getDataPermGetter();// 数据权限过滤的接口
		try {
			Set<T> result = data.getDatasWithPermission(objlist, type, openCode, user, pk_group); // 数据权限过滤接口调用的方法
			return result;
		} catch (BusinessException e) {
			throw e;
		}
	}

	private void  goOnSum(/*boolean sumOnlyVar*/) throws BusinessException{
		String [] allsheetpk = getAllSheetpk();
		String[] sheetpks = TbZiorUiCtl.getTaskValidLookPkSheets(getMdTask(), getContext(), false);//汇总范围
		String[] calculateSheetPks = SheetGroupCtl.getPkSheetsByTaskSheetList(getMdTask().getSheetlist(), false);//可计算范围
		String [] validVisiblesheetpk = sheetpks==null?allsheetpk:sheetpks;
		String [] validCalculateSheetPks = calculateSheetPks==null?allsheetpk:calculateSheetPks;
		sumDlg  = new SumSingleSheetDlg(this.getMainboard(),NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000273")/*选择汇总单位*/,
				tasks[0],validVisiblesheetpk, validCalculateSheetPks,allValidDimMembers,isAutoSumCube,false);
		sumDlg.setNodetype(	getContext().getNodeType());
		sumDlg.setVisible(true);
			if(sumDlg.getResult()==UIDialog.ID_OK){
				Thread thread = new Thread(){
					public void run(){
						onSum();
						UFDateTime serverTime = WorkbenchEnvironment.getServerTime();
						getContext().setCurrentTs(serverTime);
					}

				};
				if (TbPlanFrameUtil.getTbPlanFrame(getMainboard()) == null) {
					onSum();
				} else {
					TbPlanFrameUtil.getTbPlanFrame(getMainboard()).invokeWithPorgress(
							thread);
				}
		}
	}

	private void onSum() {
		try{
			sumDlg.setMdTaskDef(getContext().getZiorOpenNodeModel().getSelectTaskDef());
			if(sumDlg.getFlag().equals("stepsum")){
				taskDataModel=sumDlg.doLevelSum();
			}else{
				taskDataModel=	sumDlg.doSum();
			}
			// lrx 2015-8-4 汇总后更新主体树上任务状态
			HashSet<String> updTaskPks = sumDlg.getUpdStatusTaskPks();
			if (updTaskPks != null && updTaskPks.size() > 0) {
				updStatusTask = new ArrayList<MdTask>();
				MdTask[] allTasks = getContext().getZiorOpenNodeModel().getUiMdTasks();
				if (allTasks != null) {
					for (int i=0; i<allTasks.length; i++) {
						if (updTaskPks.contains(allTasks[i].getPrimaryKey())) {
							if (!ITaskStatus.COMPILING.equals(allTasks[i].getPlanstatus()) && 
									!ITaskStatus.ADJUSTING.equals(allTasks[i].getPlanstatus())) {
								allTasks[i].setPlanstatus(ITaskConst.taskVersionStatus_adjust.equals(allTasks[i].getVersionstatus()) ? ITaskStatus.ADJUSTING : ITaskStatus.COMPILING);
								updStatusTask.add(allTasks[i]);
							}
						}
					}
				}
			}
		}catch(final BusinessException be){
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					MessageDialog.showErrorDlg(getMainboard(), NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*提示*/, be.getMessage());
				}
			});
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (taskDataModel!=null) {
					getCurrentViewer().getViewManager().refreshByTaskDataModel(taskDataModel);
				}
				if (updStatusTask != null && !updStatusTask.isEmpty()) {
					TbPlanListPanel taskListPan = getTbPlanListViewer();
					if (taskListPan != null) {
						TableModel model = taskListPan.getPlanListTablePane().getTable().getModel();
						if (model != null && model instanceof AbstractTableModel) {
							((AbstractTableModel)model).fireTableDataChanged();
						}
					}
					TbReportDirView taskDirPan = getTbReportDirView();
					if (taskDirPan != null) {
						refeshTreeRender(updStatusTask.toArray(new MdTask[0]));
					}
				}
			}
		});
	}

	protected String getLeftBottomInfo(){
		return NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000269")/*汇总完成*/;
	}


	@Override
	public boolean isShowProgress(){
	    	return false;
	    }


	@Override
	public boolean isActionEnabled() {
		if(getContext().getTaskNumber()!=1){
			return false;
		}
		int valueCale = getContext().getAttributeScale();
		if (valueCale != -1){
			return false;
		}
		return true;
	}
	private boolean isAutoSumTask(String pk_task) throws BusinessException {
		MdTask task = TbTaskCtl.getMdTaskByPk(pk_task, false);
	//	IDimManager dimManager = DimServiceGetter.getDimManager();
	//	DimDef dimDef = dimManager.getDimDefByBusiCode(IDimLevelCodeConst.ENTITY);
		if (task == null)
			throw new BusinessException(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000424")/*没有找到对应任务！*/);
		HashMap<String, MdArea[]> areaMap = FormServiceGetter.getFormObjectService().getMdAreasByWorkbook(task.getPk_workbook(), null);
		if (areaMap != null && !areaMap.isEmpty()) {
			HashMap<String, Object> cdMap = new HashMap<String, Object>();
			ICubeDefQueryService icdqs = CubeServiceGetter.getCubeDefQueryService();
			for (MdArea[] areas : areaMap.values()) {
				if (areas == null)
					continue;
				for (MdArea area : areas) {
					String cubeCode = area.getCubecode();
					if (cdMap.containsKey(cubeCode))
						continue;
					try {
						CubeDef cd = icdqs.queryCubeDefByBusiCode(cubeCode);
						if (cd != null) {
							List<CubeDimUsage> cdus = cd.getCubeDimUsages();
							if (cdus != null) {
								for (CubeDimUsage cdu : cdus) {
									if (cdu.getSumFlag() != null && cdu.getSumFlag().booleanValue()&&
										cdu.getDimHierarchy().getDimDef().getDimType().equals(DimDefType.ENTITY))
										return true;
								}
							}
						}
					} catch (BusinessException be) {
						NtbLogger.printException(be);
					}
					cdMap.put(cubeCode, null);
				}
			}
		}
		return false;
	}
	/**
	 * 任务上所有表单pk
	 * @return
	 * @throws BusinessException
	 */
	public String []  getAllSheetpk() throws BusinessException{
		MdWorkbook book = FormServiceGetter.getFormObjectService()
				.getWorkbookByPk(getMdTask().getPk_workbook(), false);
		MdSheet[] sheets = book.getSheets();
		List <String> allsheetPks = new ArrayList <String>();
		for (int i = 0; i < sheets.length; i++) {
			allsheetPks.add(sheets[i].getPrimaryKey());
		}
		String [] pkarr = allsheetPks.toArray(new String[0]);
		return pkarr;
	}
}