package nc.ui.tb.zior.pluginaction.sum;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import nc.bs.framework.common.NCLocator;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.itf.mdm.dim.IDimManager;
import nc.itf.tb.ext.plan.IPlanExtZjService;
import nc.itf.tb.task.ITaskExcelService;
import nc.ms.mdm.dim.DimMemberReader;
import nc.ms.mdm.dim.DimServiceGetter;
import nc.ms.tb.ext.plan.PlanExtServiceGetter;
import nc.ms.tb.form.FormServiceGetter;
import nc.ms.tb.plan.SumTaskStateCheckTool;
import nc.ms.tb.plan.TBSumPlanCommonToolsWithExcel;
import nc.ms.tb.pubutil.CostTime;
import nc.ms.tb.task.PlanAsynSumCtl;
import nc.ms.tb.task.TbTaskCtl;
import nc.ms.tb.task.data.TaskDataModel;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.beans.UIButton;
import nc.ui.pub.beans.UICheckBox;
import nc.ui.pub.beans.UIComboBox;
import nc.ui.pub.beans.UIDialog;
import nc.ui.pub.beans.UILabel;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UIRadioButton;
import nc.ui.pub.beans.UITablePane;
import nc.ui.pub.beans.UITree;
import nc.ui.tb.tree.ComTbTreePanel;
import nc.ui.tb.tree.TbFCTreeModel;
import nc.ui.tb.tree.TbTreeNode;
import nc.ui.tb.tree.policy.StringTreeNodePolicy;
import nc.ui.tb.tree.policy.ZiorTreePolicy;
import nc.ui.tb.zior.ISearchActionHandler;
import nc.ui.tb.zior.SearchPanel;
import nc.ui.tb.zior.TbSearchObject;
import nc.vo.mdm.cube.DimSectionTuple;
import nc.vo.mdm.dim.DimDef;
import nc.vo.mdm.dim.DimHierarchy;
import nc.vo.mdm.dim.DimLevel;
import nc.vo.mdm.dim.DimMember;
import nc.vo.mdm.dim.IDimDefPKConst;
import nc.vo.mdm.dim.IDimLevelCodeConst;
import nc.vo.mdm.dim.LevelValue;
import nc.vo.mdm.pub.NtbEnv;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.tb.form.MdSheet;
import nc.vo.tb.form.MdWorkbook;
import nc.vo.tb.obj.UserLoginVO;
import nc.vo.tb.plan.TbSumParamVO;
import nc.vo.tb.task.ITaskConst;
import nc.vo.tb.task.ITaskStatus;
import nc.vo.tb.task.MdTask;
import nc.vo.tb.task.MdTaskDef;

import com.ufida.zior.view.Mainboard;
/**
 * 汇总界面
 * @author pengzhena
 *
 */
@SuppressWarnings("deprecation")
public class SumSingleSheetDlg extends UIDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	private ComTbTreePanel entTree = null;
	private String dlgTitle = "";
	protected UIButton isOk = null;
	private UIButton isCancle = null;
	private UIRadioButton dirLower = null;
	private UIRadioButton allLeaf = null;
	private UIRadioButton stepSum = null;
	private UIRadioButton freeSelect = null;
	private UICheckBox sumEffectiveData = null;
	//private UITextField queryEnt = null;
	private UIPanel optionPanle = null;
	private UIPanel centerPan = null;
	private TbFCTreeModel originalTreeModel = null;
	private Color c = new Color(196, 225, 253);
	protected UIPanel okorcancle = null;
	private UIComboBox checkBox = null;
	private UIPanel allCancle = null;
	private MdTask mdtask = null;
	private String sumStyle = "freesum";
	protected final static String  SUMSTYLE_USERDEF = "freesum";
	private final String  SUMSTYLE_LEVLEByLEVLE = "stepsum";
	private final static String  SUMSTYLE_DIRLOWER= "dir";
	private final static String  SUMSTYLE_ALLLEAF= "allleaf";
	private static  Map<String,String>  sumstyleinfomap =new   HashMap<String,String>();
	private UITablePane tablepan = null;
	protected SheetNameListModel model = null;
	private DimMember SelfdimMember = null;
	protected  List<String> sheetName=null;
	private Mainboard  mainBord=null;
	private static  Map<String,Integer>  varSumTypeMap =new   LinkedHashMap<String,Integer>();
	//private String[] varType = { NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000260")/*默认*/, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000275")/*罗列汇总*/, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000276")/*分类汇总*/, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000277")/*不汇总 */ };
	private int varAreaSumType = -1;
	private Map<DimMember, MdTask> allDimsAndTasks=new HashMap<DimMember, MdTask>();
	private TaskDataModel  taskDataModel=null;
	private String nodetype=null;
	private DimLevel  sumedDimlevel =null;
	private DimDef  toBeSumeddimdef = null;
	private boolean  isAutoSumCube=false;
	protected    List<?> list=null;
	protected UIComboBox  box=null;
	protected UICheckBox  ckBox=null;
	private String [] allVisibleSheetPks =null;
	private String [] calculateSheetPks =null;
	
	// lrx 2015-8-4 记录可能要通过汇总更新状态的任务
	private HashSet<String> updStatusTaskPks;
	
	
	private boolean executeExcel = true;

	/**
	 * 汇总dlg构造方法
	 * @param ui
	 * @param title
	 * @param task
	 * @param allsheet
	 * @param allDimsAndTasks
	 * @param isAutoSumCube
	 * @throws BusinessException
	 */
	public SumSingleSheetDlg(Container ui, String title, MdTask task,
			String [] allVisibleSheetPks,String [] calculateSheetPks, Map<DimMember, MdTask> allDimsAndTasks, boolean isAutoSumCube) throws BusinessException {
		super();
		dlgTitle = title;
		mdtask = task;
		this.allVisibleSheetPks = allVisibleSheetPks;
		this.calculateSheetPks= calculateSheetPks;
		mainBord=(Mainboard) ui;
		this.allDimsAndTasks=allDimsAndTasks;
		this.isAutoSumCube=isAutoSumCube;
		initView();
	}
	
	/**
	 * 汇总dlg构造方法
	 * @param ui
	 * @param title
	 * @param task
	 * @param allsheet
	 * @param allDimsAndTasks
	 * @param isAutoSumCube
	 * @throws BusinessException
	 */
	public SumSingleSheetDlg(Container ui, String title, MdTask task,
			String [] allVisibleSheetPks,String [] calculateSheetPks, Map<DimMember, MdTask> allDimsAndTasks, boolean isAutoSumCube,boolean isExecuteExcel) throws BusinessException {
		super();
		this.executeExcel = isExecuteExcel;
		dlgTitle = title;
		mdtask = task;
		this.allVisibleSheetPks = allVisibleSheetPks;
		this.calculateSheetPks= calculateSheetPks;
		mainBord=(Mainboard) ui;
		this.allDimsAndTasks=allDimsAndTasks;
		this.isAutoSumCube=isAutoSumCube;
		initView();
		
		 
	}
	/**
	 * 该构造方法专为多任务打印使用
	 * @param ui
	 * @param task
	 * @param allsheet
	 * @param allDimsAndTasks
	 * @param list
	 * @throws BusinessException
	 */
	public SumSingleSheetDlg(Container ui, MdTask task,
			String [] allVisibleSheetPks, Map<DimMember, MdTask> allDimsAndTasks,List<?> list ) throws BusinessException {
		super();
		mdtask = task;
		this.allVisibleSheetPks = allVisibleSheetPks;
		mainBord=(Mainboard) ui;
		this.list=list;
		this.allDimsAndTasks=allDimsAndTasks;
		initView();
	}
	
	

/**
 * 初始化界面
 * @throws BusinessException
 */
	public void initView() throws BusinessException {
		setTitle(dlgTitle);
		setSize(getSizeByScreen());
		add(BorderLayout.NORTH, getOptionPanle());
		add(BorderLayout.CENTER, getEntAndSheetPan());
		add(BorderLayout.SOUTH, getOkorcancle());
		setContainerCenter();
		initLeftTree(null);
		if (isAutoSumCube) {
			getDirLower().setSelected(true);
			getAllLeaf().setEnabled(false);
			getStepSum().setEnabled(false);
			getFreeSelect().setEnabled(false);
			sumStyle=SUMSTYLE_DIRLOWER;
		}
		refreshTreeSelect();
		model.sheetSelected(true);
		for (String str : sheetName) {
			model.getValueMap().put(str, true);
			model.setSelectedNameList(sheetName);
		}

	}

	protected void initLeftTree(DimDef sumdimdef) {
		getTreePanel().setTreeModel(getOriginalTreeModel(sumdimdef));
		((TbTreeNode) getTreePanel().getTreeModel().getRoot())
				.setCanBeSelected(false);
	}

	protected TbFCTreeModel getOriginalTreeModel(DimDef sumdimdef) {
		if (originalTreeModel == null) {
			originalTreeModel = loadOriginalTreeModel(sumdimdef);
		}
		return originalTreeModel;
	}

	/**
	 * 树panel
	 */

	public ComTbTreePanel getTreePanel() {
		if (entTree == null) {
			entTree = new ComTbTreePanel();
		}
		return entTree;

	}

	/**
	 * actionPerformed
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == getDirLower()) {
			sumStyle = SUMSTYLE_DIRLOWER;
			refreshTreeSelect();
		}
		else  if (e.getSource() == getAllLeaf()) {

			sumStyle = SUMSTYLE_ALLLEAF;
			refreshTreeSelect();
		}
		else if (e.getSource() == getStepSum()) {
			sumStyle = SUMSTYLE_LEVLEByLEVLE;
			refreshTreeSelect();
		}
		else if (e.getSource() == getFreeSelect()) {
			sumStyle = SUMSTYLE_USERDEF;
			refreshTreeSelect();
		}
		else if (e.getSource() == getSelectAll()) {
			model.sheetSelected(true);
			for (String str : sheetName) {
				model.getValueMap().put(str, true);
				model.setSelectedNameList(sheetName);
			}
		}
		else if (e.getSource() == getCancle()) {
			model.sheetSelected(false);
			for (String str : sheetName) {
				model.getValueMap().put(str, false);
			}
			model.setSelectedNameList(new ArrayList<String>());
		}
		else if (e.getSource() == getIsCancle()) {
			closeCancel();
		}
		else if (e.getSource() == getIsOk()) {
			String[] selectedSheetPks = null;
			try {
				selectedSheetPks = getSelectedSheetPks();
				if(selectedSheetPks==null || selectedSheetPks.length==0){
					MessageDialog.showErrorDlg(this, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*提示*/, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0243")/*@res "请选择需要汇总的表单"*/);
					return;
				}
			} catch (BusinessException e2) {
				e2.printStackTrace();
			}

			 Object[] Selectorg = getTreePanel().getSelectBusiObjs();
				if (sumStyle.equals(SUMSTYLE_USERDEF)) {
					if(Selectorg == null){
						//MessageDialog.showErrorDlg(this, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*提示*/, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000279")/*请选择汇总单位后进行汇总*/);
						MessageDialog.showErrorDlg(this, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*提示*/, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0244")/*@res "请选择"*/+((DimDef)getSumDimDefBox().getSelectdItemValue()).getObjName()+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0245")/*@res "后进行汇总"*/);
					}else{
						List<DimMember> dimlist = new ArrayList<DimMember>();
						List<DimMember> pardimlist = new ArrayList<DimMember>();
						for (int i = 0; i < Selectorg.length; i++) {
							dimlist.add(((DimMember) Selectorg[i]));
						}
						for (DimMember d : dimlist) {
							DimMember  parentdim =  d.getParentMember();
							if(dimlist.contains(parentdim)){
								pardimlist.add(parentdim);
							}
						}
						if(pardimlist.size()!=0&&getToBeSumeddimdef().getPrimaryKey().equals(IDimDefPKConst.TIME)){
							MessageDialog.showErrorDlg(this, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0100")/*@res "提示"*/, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0246")/*@res "不能在计划期间上跨层级汇总,请重新选择汇总期间!"*/);
						}else{
							closeOK();
						}
					}
				}else if (sumStyle.equals(SUMSTYLE_LEVLEByLEVLE)){//20141028 逐级汇总给提示
					String where =TBSumPlanCommonToolsWithExcel.getQuerySqlByTask(mdtask,getSumedDimlevel());
					MdTask[] tasks = null;
					try {
						tasks = TbTaskCtl.getMdTasksByWhere(where.toString(), true);
					} catch (BusinessException e1) {
						NtbLogger.printException(e1);
						return;
					}
//					if(tasks!=null&&tasks.length > 1000){ //判断当前节点的所有下级
					if(tasks!=null&&getTreePanel()!=null&&getTreePanel().getTreeModel()!=null&&getTreePanel().getTreeModel().aryVOs.size() > 1000){
						MessageDialog.showErrorDlg(this, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0100")/*@res "提示"*/, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0247")/*@res "逐级汇总任务不能超过1000个!"*/);
						return;
					}
					Map<String,String> map = filterSubmitAndApprovePassTasks(tasks,getSumedDimlevel());
					Set<String> keySet = map.keySet();
					if(!NtbEnv.isOutLineUI){
						if(tasks.length!=keySet.size()){
							int result = MessageDialog.showYesNoDlg(this,nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0002")/*@res "提示"*/,
									 nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0248")/*@res "当前汇总下级主体中存在数据生效的非末级任务(已上报/审批通过),它们的数据将不会被修改,继续汇总?"*/);
							if(MessageDialog.ID_YES== result){
								closeOK();
							}
						}else{
							closeOK();
						}
					}else{
						closeOK();
					}
				}else{
					closeOK();
				}
		}

	}
	/*2013-05-09添加对于自动汇总主体仅汇总浮动区的汇总方式--------21406目前未使用*/
	public void doLevelSumOnlyVar() throws BusinessException {
		TbTreeNode node = (TbTreeNode) getTreePanel().getTreeModel().getRoot();
		Hashtable<String, Hashtable<String, List<String>>> hashtable = new Hashtable<String, Hashtable<String, List<String>>>();
		Hashtable <String, Hashtable<String, List<String>>>  table = getLevelNode(node, 0,hashtable);
		String []	fromUnicode=null;
		ITaskExcelService service = NCLocator.getInstance().lookup(
				ITaskExcelService.class);
		IPlanExtZjService iPlanExtZjService = PlanExtServiceGetter
				.getPlanExtZjService();
		for (int i = table.size() - 1; i >= 0; i--) {
			Hashtable<String, List<String>> levelTable = table.get(String
					.valueOf(i));
			for (Iterator<String> itr = levelTable.keySet().iterator(); itr
					.hasNext();) {
				String target = itr.next();
				if (getSumEffectiveData().isSelected()) {

					fromUnicode=filterUnSubmit(levelTable.get(target)).toArray(new String [0]);
				}else{
					fromUnicode =  levelTable.get(target).toArray(new String [0]);
				}
				MdTask task = service.getMdTaskByTaskAndOrg(mdtask.getPermPrimary(), target);
				if(task.getPlanstatus().equals(ITaskStatus.APPROVE_PASS)||task.getPlanstatus().equals(ITaskStatus.PROMOTED)
						||task.getManagestatus().equals(ITaskConst.taskManageStatus_lock)){
					//NtbLogger.print(task.getObjname()+"--------------------->已经生效没有进行汇总!");
					continue;
				}
				HashMap<String, String> userInfo = new HashMap<String, String>();
				userInfo.put("sumStyle",NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000278")/*逐级汇总*/ );
				iPlanExtZjService.sumPlanVarAreaByParamDimLevel(task.getPrimaryKey(),getSumedDimlevel(), fromUnicode,
						getUserVo(),getSelectedSheetPks(),getsumVartype(),userInfo);
			}
		}

	}
	/*2013-05-09添加对于自动汇总主体仅汇总浮动区的汇总方式----------目前未使用*/
	public void doSumOnlyVar() throws BusinessException {
		String pk_taskTo = mdtask.getPermPrimary();
		Object[] Selectorg = getTreePanel().getSelectBusiObjs();
		if (Selectorg == null) {
			MessageDialog.showErrorDlg(this, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*提示*/, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000279")/*请选择汇总单位后进行汇总*/);
			return ;
		}
		List<DimMember> orgs = new ArrayList<DimMember>();
		for (int i = 0; i < Selectorg.length; i++) {
			orgs.add(((DimMember) Selectorg[i]));
		}

		String[] fromUnicode = new String[Selectorg.length];
		List<String> uniqcodelist = new ArrayList<String>();
		if (orgs != null) {
			for (DimMember dimMember : orgs) {

				uniqcodelist.add(dimMember.getLevelValue().getUniqCode());
			}

		}
		if (getSumEffectiveData().isSelected()) {
			fromUnicode=filterUnSubmit(uniqcodelist).toArray(new String[0]);
		}else{
			fromUnicode = uniqcodelist.toArray(new String[0]);
		}

		IPlanExtZjService iPlanExtZjService = PlanExtServiceGetter.getPlanExtZjService();
		HashMap<String, String> userInfo = new HashMap<String, String>();
		userInfo.put("sumStyle", sumstyleinfomap.get(sumStyle));
		iPlanExtZjService.sumPlanVarAreaByParamDimLevel(mdtask.getPrimaryKey(),getSumedDimlevel(), fromUnicode,
				getUserVo(),getSelectedSheetPks(),getsumVartype(),userInfo);

	}
	/**
	 * @author pengzhena
	 * 逐级汇总中获取已上报/已审批/已冻结任务
	 * @return 其他任务的PK,和主体uniqcode的map
	 */
	private Map<String,String>  filterSubmitAndApprovePassTasks(MdTask[] mdTasks,DimLevel  sumedLv){
		Map<String,String>  taskAfterFilter= new   HashMap<String, String>();
			if ( mdTasks!= null){
				for (MdTask  currtask:mdTasks) {
					List <String >  state =new  ArrayList <String >();
					state.add(currtask.getPlanstatus());
					state.add(currtask.getManagestatus());
					if(SumTaskStateCheckTool.checkValid(state)){
						List<LevelValue>  lvs = TBSumPlanCommonToolsWithExcel.getLevelValueFromTask(currtask, sumedLv);
						for (LevelValue levelValue : lvs) {
							taskAfterFilter.put(levelValue.getUniqCode(), currtask.getPk_obj());
						}
					}
				}
			}
		return taskAfterFilter;

	}
	/**
	 * doLevelSum  逐级汇总
	 */
	public TaskDataModel doLevelSum() throws BusinessException {
//		updStatusTaskPks = new HashSet<String>();
//		String  pkTaskTo="";
		String where =TBSumPlanCommonToolsWithExcel.getQuerySqlByTask(mdtask,getSumedDimlevel());
		MdTask []  tasks=TbTaskCtl.getMdTasksByWhere(where.toString(), true);
		Map<String,String> map=filterSubmitAndApprovePassTasks(tasks,getSumedDimlevel());
//		Set<String> keySet = map.keySet();
		TbTreeNode node = (TbTreeNode) getTreePanel().getTreeModel().getRoot();
		Hashtable<String, Hashtable<String, List<String>>> hashtable = new Hashtable<String, Hashtable<String, List<String>>>();
		Hashtable <String, Hashtable<String, List<String>>>  table = getLevelNode(node, 0,hashtable);
//		String []	fromUnicode=null;
		IPlanExtZjService iPlanExtZjService = PlanExtServiceGetter.getPlanExtZjService();
		// lrx 2015-8-28 逐级汇总放到后台进行
		HashMap<String, String> userInfo = new HashMap<String, String>();
		userInfo.put("sumStyle",NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000278")/*逐级汇总*/ );
		TbSumParamVO sumparam = TbSumParamVO.getInstance();
		sumparam.sumParamDimLevel = getSumedDimlevel();
		sumparam.userInfo = userInfo;
		sumparam.userVo = getUserVo();
		sumparam.pk_sumSheets = getSelectedSheetPks();
		sumparam.varAreaSumType = getsumVartype();
		sumparam.nodeType = getNodetype();
		sumparam.isSumDataCell = !isAutoSumCube;
		sumparam.calculateSheetPks = calculateSheetPks;
		HashMap<String, Object> rtn = iPlanExtZjService.doLevelSum(sumparam, getSumEffectiveData().isSelected(), map, table, mdtask, getSumedDimlevel());
		updStatusTaskPks = (HashSet<String>)rtn.get("updStatusTaskPks");
		taskDataModel = (TaskDataModel)rtn.get("taskDataModel");
//		for (int i = table.size() - 1; i >= 0; i--) {
//			Hashtable<String, List<String>> levelTable = table.get(String
//					.valueOf(i));
//			for (Iterator<String> itr = levelTable.keySet().iterator(); itr
//					.hasNext();) {
//				String target = itr.next();
////				NtbLogger.print("target------->"+target);
//				if (getSumEffectiveData().isSelected()) {//是否只汇总上报数据
//					fromUnicode=filterUnSubmit(levelTable.get(target)).toArray(new String [0]);
//				}else{
//					fromUnicode =  levelTable.get(target).toArray(new String [0]);
//				}
//				HashMap<String, String> userInfo = new HashMap<String, String>();
//				userInfo.put("sumStyle",NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000278")/*逐级汇总*/ );
//				if(keySet.contains(target)){
//					pkTaskTo=map.get(target);
//					NtbLogger.print("in processing of sum to----"+target);
//					TbSumParamVO sumparam = TbSumParamVO.getInstance();
//					sumparam.pk_taskTo = pkTaskTo;
//					sumparam.sumParamDimLevel = getSumedDimlevel();
//					sumparam.fromOrgUnicode = fromUnicode;
//					sumparam.userInfo = userInfo;
//					sumparam.userVo = getUserVo();
//					sumparam.pk_sumSheets = getSelectedSheetPks();
//					sumparam.varAreaSumType = getsumVartype();
//					sumparam.nodeType = getNodetype();
//					sumparam.isSumDataCell = !isAutoSumCube;
//					sumparam.calculateSheetPks = calculateSheetPks;
//					taskDataModel=iPlanExtZjService.sumPlanByparadimAndCalculate(sumparam);
//					updStatusTaskPks.add(pkTaskTo);
//				}
//			}
//		}
		return taskDataModel;
	}

	public HashSet<String> getUpdStatusTaskPks() {
		return updStatusTaskPks;
	}
	/**
	 * doSum  汇总
	 * @return
	 *
	 * @throws BusinessException
	 */
	//private long start=System.currentTimeMillis();
	public TaskDataModel doSum() throws BusinessException {
		updStatusTaskPks = new HashSet<String>();
		String pk_taskTo = mdtask.getPermPrimary();
		Object[] Selectorg = getTreePanel().getSelectBusiObjs();
		if (Selectorg == null) {
			return null;
		}
		List<DimMember> orgs = new ArrayList<DimMember>();
		for (int i = 0; i < Selectorg.length; i++) {
			orgs.add(((DimMember) Selectorg[i]));
		}
		String[] fromUnicode = new String[Selectorg.length];
		List<String> uniqcodelist = new ArrayList<String>();
		DimLevel  lv =null;
		if (orgs != null) {
			for (DimMember dimMember : orgs) {
				uniqcodelist.add(dimMember.getLevelValue().getUniqCode());
				lv=dimMember.getDimLevel();
			}
		}
		if (getSumEffectiveData().isSelected()) {
			fromUnicode=filterUnSubmit(uniqcodelist).toArray(new String[0]);
		}else{
			fromUnicode = uniqcodelist.toArray(new String[0]);
		}
		IPlanExtZjService iPlanExtZjService = PlanExtServiceGetter.getPlanExtZjService();
		HashMap<String, String> userInfo = new HashMap<String, String>();
		userInfo.put("sumStyle", sumstyleinfomap.get(sumStyle));
		CostTime  ct=new CostTime();
		DimLevel  sumDimLv = 	getSumedDimlevel().equals(lv)?getSumedDimlevel():lv;
		//String [] sumSheetPks = getSelectedSheetPks()/*getValidSumSheetpks(validTasks)*/;
		TbSumParamVO sumparam = TbSumParamVO.getInstance();
		sumparam.pk_taskTo = pk_taskTo;
		sumparam.sumParamDimLevel = sumDimLv;
		sumparam.fromOrgUnicode = fromUnicode;
		sumparam.userInfo = userInfo;
		sumparam.userVo = getUserVo();
		sumparam.pk_sumSheets = getSelectedSheetPks();
		sumparam.varAreaSumType = getsumVartype();
		sumparam.nodeType = getNodetype();
		sumparam.isSumDataCell = !isAutoSumCube;
		sumparam.calculateSheetPks = calculateSheetPks;
		boolean isAsynSum = false;
		// lrx 2015-11-9 异步汇总方式,暂不开启使用
//		if ((sumparam.pk_sumSheets==null || sumparam.pk_sumSheets.length==0) && fromUnicode.length > 20 ||
//				sumparam.pk_sumSheets.length*fromUnicode.length > 200 || fromUnicode.length > 50) {
//			isAsynSum = MessageDialog.showYesNoDlg(mainBord, dlgTitle, "要汇总的任务较多,是否需要通过异步方式进行汇总?")==UIDialog.ID_YES;
//		}
		if (isAsynSum) {
			String taskName = PlanAsynSumCtl.asynSumPlan(getMdTaskDef(), sumparam);
			MessageDialog.showHintDlg(mainBord, dlgTitle, "后台异步汇总任务["+taskName+"]已经启动,请汇总结束后刷新任务查看执行结果.");
			taskDataModel = null;
		}
		else {
			if(executeExcel){
				taskDataModel=iPlanExtZjService.sumPlanByparadimAndCalculate(sumparam);
			}else{
				taskDataModel=iPlanExtZjService.sumPlanByparadimAndOnlyRule(sumparam);
			}
			
		}
		updStatusTaskPks.add(pk_taskTo);
//		taskDataModel=iPlanExtZjService.sumPlanByparadimAndCalculate(pk_taskTo, sumDimLv, fromUnicode, getUserVo(),
//				sumSheetPks, getsumVartype(),userInfo,getNodetype(), !isAutoSumCube,calculateSheetPks);
		ct.printStepCost("sumPlanAndCalculate cost--------->:");
		return taskDataModel;


	}
	
	private MdTaskDef mdTaskDef;
	public MdTaskDef getMdTaskDef() {
		return mdTaskDef;
	}
	public void setMdTaskDef(MdTaskDef mdTaskDef) {
		this.mdTaskDef = mdTaskDef;
	}

	/**
	 * 获取被选中的任务表单PK
	 *
	 * @return
	 * @throws BusinessException
	 */
	public String[] getSelectedSheetPks() throws BusinessException {
		List<String> SelectedSheetName = this.model.getSelectedNameList();
		Map<String, String> pkmap = new HashMap<String, String>();
		List<String> pk_list = new ArrayList<String>();
		MdWorkbook book = getMdworkbook();
		MdSheet[] sheets = book.getSheets();
		for (int i = 0; i < sheets.length; i++) {
			pkmap.put(sheets[i].getObjname(), sheets[i].getPrimaryKey());
		}
		Iterator<String> keys = pkmap.keySet().iterator();
		while (keys.hasNext()) {
			Object key = keys.next();
			for (String str : SelectedSheetName) {
				if (key.equals(str)) {
					pk_list.add(pkmap.get(key));
				}
			}
		}
		return pk_list.toArray(new String[0]);

	}
//	/**
//	 * 当前任务的可见sheet范围与下级表单不一致（多余）时，需要不对在下级任务没有出现的sheet汇总。
//	 * @param tasks
//	 * @return
//	 * @throws BusinessException
//	 */
//	public  String [] getValidSumSheetpks(List <MdTask> tasks) throws BusinessException{
//		Set <String> sheetPk = new HashSet<String>();
//		List <String> list = new ArrayList<String>();
//		for (MdTask task : tasks) {
//			String[] sheetpks = TbZiorUiCtl.getTaskValidLookPkSheets(task, null, false);//下级任务可见表单范围
//			for (String string : sheetpks) {
//				sheetPk.add(string);
//			}
//		}
//		String[] selectedpks = getSelectedSheetPks();
//		for (String pk : selectedpks) {
//			if(sheetPk.contains(pk)){
//				list.add(pk);
//			}
//		}
//		return list.toArray(new String[0]);
//
//	}
	// 浮动区汇总方式
	private int getsumVartype() {
		return varSumTypeMap.get(getVarStyle().getSelectdItemValue());
	}

	/**
	 * 过滤掉未上报数据的任务
	 *
	 * @param Unicode
	 * @return
	 * @throws BusinessException
	 */
	public List<String> filterUnSubmit(List <String> ucodelist)
			throws BusinessException {
		return TBSumPlanCommonToolsWithExcel.filterUnSubmit(ucodelist, getSumedDimlevel(), mdtask);
//		DimHierarchy dimHierarchy = null;
//		DimLevel dimlevel = getSumedDimlevel();
//		List<String> taskUnicodefiltered = new ArrayList<String>();
//		String sqlWhere =TBSumPlanCommonToolsWithExcel.getQuerySqlByTask(mdtask,dimlevel);
//		MdTask []  tasks=TbTaskCtl.getMdTasksByWhere(sqlWhere, true);
//			if (tasks != null){
//				for (int i = 0; i < tasks.length; i++) {
//
//					if (/*allDimsAndTasks.values().contains(tasks[i])
//							&&*/(tasks[i].getPlanstatus().equals(ITaskStatus.PROMOTED)
//							/*	||tasks[i].getPlanstatus().equals(ITaskStatus.LOCAL_PROMOTED)只汇总上报数据只针对二阶段上报数据,修改于20140703*/)
//					){
//				String key = tasks[i].getPk_dataent();
//				String pk_orgStruct = tasks[i].getTaskDefWithoutDetail().getPk_orgstruct();
//				dimHierarchy = DimServiceGetter.getDimManager().getDimHierarchyByPK(pk_orgStruct);
//				IDimManager dimManager = DimServiceGetter.getDimManager();
//				DimLevel dsl = dimManager.getDimLevelByBusiCode(IDimLevelCodeConst.ENTITY);
//				DimDef dimDef = dimManager.getDimDefByBusiCode(IDimLevelCodeConst.ENTITY);
//				List<DimHierarchy> hierarchies = dimDef.getHierarchies();
//				if (hierarchies == null ) {
//					return null;
//				}
//				DimMemberReader dmr = dimHierarchy.getMemberReader();
//				LevelValue lv = dmr.getLevelValueByKey(dsl, key);
//				if(ucodelist.contains(lv.getUniqCode())){
//					taskUnicodefiltered.add(lv.getUniqCode());
//				}
//				}
//			}
//		}
//		return taskUnicodefiltered;
	}
	/**
	 * 刷新树
	 */
	public void refreshTreeSelect() {
		getTreePanel().getTreeModel().refreshTreeOpt();
		Object[] selectedObjs = null;
		// 要将直接下级或者所有下级遍历出来放进一个数组
		if (sumStyle.equals(SUMSTYLE_DIRLOWER)) {
			selectedObjs = getDirlowerNode().toArray();
			getTreePanel().setEnable(false);
			setSelectedtreeNode2SelectedStatus(selectedObjs);
			return;
		}
		 if (sumStyle.equals(SUMSTYLE_ALLLEAF)) {
			getAllLeafNode((TbTreeNode) getTreePanel().getTreeModel().getRoot());
			selectedObjs = selectNode.toArray();
			getTreePanel().setEnable(false);
			setSelectedtreeNode2SelectedStatus(selectedObjs);
			return;
		}
		 if (sumStyle.equals(SUMSTYLE_LEVLEByLEVLE)) {
			getTreePanel().setEnable(false);
			setSelectedtreeNode2SelectedStatus(selectedObjs);
			return;
		}
		 if (sumStyle.equals(SUMSTYLE_USERDEF)||getFreeSelect().isSelected()) {
			TbTreeNode node = (TbTreeNode) getTreePanel().getTreeModel()
					.getRoot();
			TreePath path = new TreePath(node.getPath());
			getTreePanel().getMainTree().expandPath(path);
			if(allDimsAndTasks.size()<=20){
				expandTree(	getTreePanel().getMainTree(), true);
			}
			setSelectedtreeNode2SelectedStatus(selectedObjs);
			return;
		}


	}
	// 让节点变成选种状态
	private   void  setSelectedtreeNode2SelectedStatus (Object[] selectedObjs){
		if (selectedObjs != null && selectedObjs.length != 0) {
			getTreePanel().setSelectBusiObjs(selectedObjs);
			for (Object selectedObj : selectedObjs) {
				TbTreeNode node = getTreePanel().getTreeModel().getNodeByVo(
						selectedObj);
				node.setSelectState(TbTreeNode.SELECTED);
				TreePath path = new TreePath(
						((TbTreeNode) node.getParent()).getPath());
				getTreePanel().getMainTree().expandPath(path);
			}

		}
	}
	/**
	 * 展开所有节点方法
	 * @param tree
	 * @param bo
	 */
	public static void expandTree(UITree tree,boolean bo) {
		  TreeNode root = (TbTreeNode) tree.getModel().getRoot();
		  expandAll(tree, new TreePath(root), bo);
		 }
		 private static void expandAll(UITree tree, TreePath parent, boolean expand) {
		  TreeNode node = (TreeNode) parent.getLastPathComponent();
		  if (node.getChildCount() >= 0) {
		   for (Enumeration e = node.children(); e.hasMoreElements(); ) {
		    TreeNode n = (TreeNode) e.nextElement();
		    TreePath path = parent.pathByAddingChild(n);
		    expandAll(tree, path, expand);
		   }
		  }
		  if (expand) {
		   tree.expandPath(parent);
		   } else {
		    tree.collapsePath(parent);
		   }
		 }
	/**
	 * 逐级汇总--获取每一层级node以及其parentNode
	 */
	private Hashtable <String, Hashtable<String, List<String>>> getLevelNode(TbTreeNode node, Integer level,Hashtable<String, Hashtable<String, List<String>>> table) {
		List<String> sonUnicodelist = new ArrayList<String>();
		List<TbTreeNode> sonlist = new ArrayList<TbTreeNode>();
		if (node.getChildCount() != 0) {
			for (int i = 0; i < node.getChildCount(); i++) {
				sonUnicodelist.add(((DimMember) ((TbTreeNode) getTreePanel()
						.getTreeModel().getChild(node, i)).getUserObject())
						.getLevelValue().getUniqCode());

				sonlist.add((TbTreeNode) getTreePanel().getTreeModel()
						.getChild(node, i));

			}
			Hashtable<String, List<String>> hashtable;
			if (table.containsKey(level.toString()))
				hashtable = table.get(level.toString());
			else
				hashtable = new Hashtable<String, List<String>>();
			Object obnode = node.getUserObject();
			DimMember dimm = (DimMember) obnode;
			hashtable.put(dimm.getLevelValue().getUniqCode(), sonUnicodelist);
			table.put(level.toString(), hashtable);
			level++;
		}

		for (TbTreeNode sonnode : sonlist) {
			getLevelNode(sonnode, level,table);
		}
		return table;
	}

	/**
	 * 获取直接下级node
	 *
	 * @return
	 * @return
	 */
	public List<Object> getDirlowerNode() {

		List<Object> selectNode = new ArrayList<Object>();
		TbFCTreeModel model = getTreePanel().getTreeModel();
		TbTreeNode root = (TbTreeNode) model.getRoot();
		//root.setCanBeSelected(false);
		int i = root.getChildCount();
		for (int j = 0; j < i; j++) {
			TbTreeNode node = (TbTreeNode) model.getChild(root, j);
			selectNode.add(node.getUserObject());
		}
		return selectNode;

	}

	/**
	 * 获取所有末级node
	 */
	List<Object> selectNode = new ArrayList<Object>();

	public void getAllLeafNode(TbTreeNode node) {
		TbFCTreeModel model = getTreePanel().getTreeModel();
		if (node.isLeaf()&&!node.isRoot()) {
			selectNode.add(node.getUserObject());
		} else {
			int i = model.getChildCount(node);
			for (int j = 0; j < i; j++) {
				TbTreeNode currnode = (TbTreeNode) model.getChild(node, j);
				if (currnode.isLeaf()) {
					selectNode.add(currnode.getUserObject());
				} else {
					getAllLeafNode(currnode);
				}
			}
		}
	}

	/**
	 * 树模型
	 * @throws BusinessException
	 */

	protected TbFCTreeModel loadOriginalTreeModel(DimDef sumdimdef) {
		List <DimMember>  childList=new  ArrayList<DimMember>();

		try {
			  childList=getDimMember(mdtask,sumdimdef);
		} catch (BusinessException e) {
			NtbLogger.print(e);
		}
		DimMember  rootDim=getSelfdimMember();
		return new TbFCTreeModel(new TbTreeNode(rootDim,
				new StringTreeNodePolicy()), childList.toArray(new DimMember[0]), new ZiorTreePolicy(childList));
	}
	/**
	 * 汇总左侧树--维度成员来源
	 *
	 * @param task
	 * @param dimDef
	 * @return
	 * @throws BusinessException
	 */
	public List<DimMember> getDimMember(MdTask task,DimDef sumdimdef) throws BusinessException {

		if(sumdimdef==null||sumdimdef.getPrimaryKey().equals(IDimDefPKConst.ENT)){
			String key = task.getPk_dataent();
			String pk_orgStruct = "";
			try {
				pk_orgStruct = task.getTaskDefWithoutDetail().getPk_orgstruct();
			} catch (BusinessException e) {
				NtbLogger.print(e);
			}
			DimHierarchy	dimHierarchy = DimServiceGetter.getDimManager().getDimHierarchyByPK(pk_orgStruct);
			IDimManager dimManager = DimServiceGetter.getDimManager();
			DimLevel	dimlv = dimManager.getDimLevelByBusiCode(IDimLevelCodeConst.ENTITY);
			setSumedDimlevel(dimlv);
			setToBeSumeddimdef(dimlv.getDimDef());
			DimMemberReader dmr = dimHierarchy.getMemberReader();
			LevelValue lv = dmr.getLevelValueByKey(dimlv, key);
			SelfdimMember = dmr.getMemberByLevelValues(lv);
			setSelfdimMember(SelfdimMember);
			List<DimMember>  validEntDimmembers=new ArrayList<DimMember>();
			List<DimMember> alldimMembersList =new ArrayList<DimMember>();
			if (SelfdimMember.isVirtualorg()) {
				// 统计成员(虚组织)的汇总
				List<String> virtualChildpk = SelfdimMember.getAllVirtualChildrenOrg();
				if (virtualChildpk != null) { // 虚组织下可能不存在值
					for (String dimpk : virtualChildpk) {
						Map<DimLevel, Object> keys = new HashMap<DimLevel, Object>();
						keys.put(dimlv, dimpk);
						alldimMembersList.add(dmr.getMemberByLevelValueKeys(keys));
					}
				}
			} else {
				// 获取当前主体的所有下级
				List<DimMember> list = dmr.getAllChildren(SelfdimMember);
				//通过权限过滤  --- qy modify 20150707
				alldimMembersList = new ArrayList<DimMember>();
				if(list != null && list.size() > 0 && allDimsAndTasks != null && allDimsAndTasks.size() > 0){
					for(DimMember dm : list){
						if(dm == null)
							continue;
						if(allDimsAndTasks.containsKey(dm))
							alldimMembersList.add(dm);
					}
				}
			}
			if(alldimMembersList != null && !alldimMembersList.isEmpty()){
				 validEntDimmembers=TBSumPlanCommonToolsWithExcel.filterNoTaskOrLockedTaskDimMember(mdtask,alldimMembersList,dimlv,dmr);
			}
				return validEntDimmembers;
		}else{
			Map<DimDef,DimHierarchy>  map  = TBSumPlanCommonToolsWithExcel.getDimHierysFromMdTask(task);
			DimHierarchy  dimhery =	map.get(sumdimdef);
			DimMemberReader dmr = dimhery.getMemberReader();
			// lrx 2015-11-30 DimDef中包含多层的维度,sumedDimlevel根据MdTask中为null的最高层取值(例如年季月都是参数维,季月为空的年表,汇总DimLevel取季)
			if (task.getExtrAttribute(task.getPk_dataent()) == null)
				TbTaskCtl.loadDimMemberValues(new MdTask[] {task}, true, false);
			DimSectionTuple taskparadim = TbTaskCtl.getTaskParadim(task);
			SelfdimMember = dmr.getMemberByLevelValues(taskparadim.getLevelValuesInDimDef(sumdimdef).toArray(new LevelValue[0]));
			MdTaskDef  taskdef = task.getTaskDefWithoutDetail();
			TbTaskCtl.loadDetail(new MdTaskDef[]{taskdef});
			DimLevel[] paradims = taskdef.getParaDims();
			HashSet<String> dlset = new HashSet<String>();
			for (DimLevel dl : paradims)
				dlset.add(dl.getPrimaryKey());
			List<DimLevel> dls = dimhery.getDimLevels();
			for (DimLevel dl : dls) {
				if (!dlset.contains(dl.getPrimaryKey()))
					continue;
				sumedDimlevel = dl;
				if (taskparadim.getLevelValue(dl) == null)
					break;
			}
//			Map<DimDef,DimLevel >   taskparammap = TBSumPlanCommonToolsWithExcel.getDimLevelFromTask(task);
//			sumedDimlevel = taskparammap.get(sumdimdef);
//			List<LevelValue> lvs = TBSumPlanCommonToolsWithExcel.getLevelValueFromTask(task, sumedDimlevel);
//			SelfdimMember = dmr.getMemberByLevelValues(lvs.get(0));
			setSelfdimMember(SelfdimMember);
			List<DimMember> dimMembers = dmr.getAllChildren(SelfdimMember);
			List<DimMember> dims = TBSumPlanCommonToolsWithExcel.filterNoTaskOrLockedTaskDimMember(mdtask,dimMembers,sumedDimlevel,dmr);
			return dims==null?new ArrayList<DimMember> ():dims;
		}

	}
	/**
	 * 选项按钮panel
	 */
	private  UIPanel getOptionPanle() {
		if (optionPanle == null) {
			optionPanle = new UIPanel();
			
			ButtonGroup group = new ButtonGroup();
			group.add(getDirLower());
			group.add(getAllLeaf());
			group.add(getStepSum());
			group.add(getFreeSelect());
			
			// lrx 2015-9-8 非中文语种时调整控件位置以便显示完整
			String langcode = NCLangRes4VoTransl.getNCLangRes().getCurrLanguage().getCode();
			boolean isChi = langcode == null || "simpchn".equals(langcode) || "tradchn".equals(langcode);
			if (isChi) {
			FlowLayout layout = new FlowLayout();
			layout.setAlignment(FlowLayout.LEFT);
			optionPanle.setLayout(layout);

			//2014-5-19添加其他参数维手动汇总选项
			optionPanle.add(new UILabel(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0249")/*@res "选择汇总维度:"*/));
			optionPanle.add(getSumDimDefBox());

			optionPanle.add(new UILabel(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0250")/*@res "汇总方式:"*//*NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000283")选择汇总单位: */));
			optionPanle.add(getDirLower());
			optionPanle.add(getAllLeaf());
			optionPanle.add(getStepSum());
			optionPanle.add(getFreeSelect());
			optionPanle.add(getSumEffectiveData());
			//optionPanle.add(new UILabel(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000284")/*浮动表汇总方式*/));
			optionPanle.add(getVarStyle());
			}
			else {
				optionPanle.setLayout(new GridLayout(2, 1));
				UIPanel p1 = new UIPanel();
				p1.setLayout(new FlowLayout(FlowLayout.LEFT));
				p1.add(new UILabel(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0249")/*@res "选择汇总维度:"*/));
				p1.add(getSumDimDefBox());
				p1.add(getSumEffectiveData());
				p1.add(getVarStyle());
				p1.setBackground(c);
				optionPanle.add(p1);
				UIPanel p2 = new UIPanel();
				p2.setLayout(new FlowLayout(FlowLayout.LEFT));
				p2.add(new UILabel(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0250")/*@res "汇总方式:"*//*NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000283")选择汇总单位: */));
				p2.add(getDirLower());
				p2.add(getAllLeaf());
				p2.add(getStepSum());
				p2.add(getFreeSelect());
				p2.setBackground(c);
				optionPanle.add(p2);
			}
			optionPanle.setBackground(c);
		}
		return optionPanle;

	}

/**
 *	搜索框
 */
	private SearchPanel serpan = null;
	private SearchPanel getserpan() {
		if (serpan == null) {
			serpan = new SearchPanel();
			serpan.addSearchResultHandler(new ISearchActionHandler() {

				@Override
				public void actionPerformed(Object searchobj, String... args) {
					searchHandlerPerformed(searchobj, args);
				}
			});
			List<TbSearchObject> commands = new ArrayList<TbSearchObject>();
			if (allDimsAndTasks!=null){
				for (DimMember dimme : allDimsAndTasks.keySet()) {
					TbSearchObject ob = new TbSearchObject();
					ob.setObj(dimme);
					commands.add(ob);
				}
			}
			serpan.setCommands(commands);
		}
		return serpan;

	}
/**
 * 搜索框事件处理
 * @param searchobj
 * @param args
 */
	private void searchHandlerPerformed(Object searchobj, String... args) {
		TbSearchObject obj = (TbSearchObject) searchobj;
		DimMember dm = (DimMember) obj.getObj();
		TbTreeNode node = getTreePanel().getTreeModel().getNodeByVo(dm);
		if (node != null) {
			TreeNode[] nodes = getTreePanel().getTreeModel()
					.getPathToRoot(node);
			TreePath path = new TreePath(nodes);
			getTreePanel().getMainTree().scrollPathToVisible(path);
			getTreePanel().getMainTree().setSelectionPath(path);
		}
	}

	/**
	 * 主体和表单选择
	 *
	 * @return
	 * @throws BusinessException
	 */
	protected UIPanel getEntAndSheetPan() throws BusinessException {
		centerPan = new UIPanel();
		GridLayout gl = new GridLayout(1, 2);
		BorderLayout b = new BorderLayout();
		centerPan.setLayout(b);
		UIPanel pan1 = new UIPanel();
		pan1.setLayout(gl);
		pan1.add(getserpan());
		pan1.add(getAllCancle());
		UIPanel pan2 = new UIPanel();
		pan2.setLayout(gl);
		pan2.add(getTreePanel());
		pan2.add(getTable());
		centerPan.add(BorderLayout.NORTH, pan1);
		centerPan.add(BorderLayout.CENTER, pan2);
		return centerPan;
	}

	// 表单名称列表
		private UITablePane getTable() throws BusinessException {
			sheetName = new ArrayList<String>();
			if (tablepan == null) {
				tablepan = new UITablePane();
				MdWorkbook book = getMdworkbook();
				MdSheet[] mdSheets = book.getSheets();
				List<String> pks = Arrays.asList(allVisibleSheetPks);
				for (MdSheet sheet : mdSheets) {
					String str = sheet.getObjname();
					if((!sheetName.contains(str))&&pks.contains(sheet.getPk_obj())){
						sheetName.add(sheet.getObjname());
					}
				}
				model = new SheetNameListModel(mdtask/*, sheetName*/);
				model.setSheetNameList(sheetName);
				tablepan.getTable().setModel(model);
				tablepan.getTable().setModel(model);
				int iWidth[] = {
						50,
						Toolkit.getDefaultToolkit().getScreenSize().width * 2 / 5 - 50 };
				tablepan.getTable().setColumnWidth(iWidth);

			}
			return tablepan;

		}
		public MdWorkbook getMdworkbook() throws BusinessException{
			MdWorkbook book = FormServiceGetter.getFormObjectService()
					.getWorkbookByPk(mdtask.getPk_workbook(), false);
			return book;
		}
	// 全选或者取消按钮
	public UIPanel getAllCancle() {
		if (allCancle == null) {
			allCancle = new UIPanel();
		}
		FlowLayout   f=new FlowLayout();
		f.setAlignment(FlowLayout.LEFT);
		allCancle.setLayout(f);
		allCancle.add(getSelectAll());
		allCancle.add(getCancle());
		return allCancle;
	}

	// 确定取消按钮
	public UIPanel getOkorcancle() {
		if (okorcancle == null) {
			okorcancle = new UIPanel();
		}
		FlowLayout  f=new FlowLayout();
		f.setAlignment(FlowLayout.RIGHT);
		okorcancle.setLayout(f);
		okorcancle.add(getIsOk());
		okorcancle.add(getIsCancle());
		return okorcancle;
	}
	/**
	 * 按钮
	 * @return
	 */
	//直接下级
	private UIRadioButton getDirLower() {
		if (dirLower == null) {
			dirLower = new UIRadioButton(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000285")/*直接下级*/);
			dirLower.setBackground(c);

		}
		dirLower.addActionListener(this);
		return dirLower;
	}
	//所有末级
	private UIRadioButton getAllLeaf() {
		if (allLeaf == null) {
			allLeaf = new UIRadioButton(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000245")/*所有末级*/);
			allLeaf.setBackground(c);
			allLeaf.addActionListener(this);
		}
		return allLeaf;
	}
	//逐级汇总
	private UIRadioButton getStepSum() {
		if (stepSum == null) {
			stepSum = new UIRadioButton(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000278")/*逐级汇总*/);
			stepSum.setBackground(c);
			stepSum.addActionListener(this);
		}
		return stepSum;
	}
	//自定义汇总
	private UIRadioButton getFreeSelect() {
		if (freeSelect == null) {
			freeSelect = new UIRadioButton(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000282")/*自定义汇总*/);
			freeSelect.setBackground(c);
			freeSelect.addActionListener(this);
			freeSelect.setSelected(true);
		}
		return freeSelect;
	}
	//只汇总生效数据
	private UICheckBox getSumEffectiveData() {
		if (sumEffectiveData == null) {
//			sumEffectiveData = new UICheckBox(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000286")/*只汇总上报数据*/);
			sumEffectiveData = new UICheckBox(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0251")/*@res "只汇总已提交数据"*/)/*只汇总已提交数据*/;
			sumEffectiveData.setBackground(c);
			// sumEffectiveData.setSelected(true);//默认不选
			sumEffectiveData.addActionListener(this);
		}
		return sumEffectiveData;
	}
	//确定
	private  UIButton getIsOk() {
		if (isOk == null) {
			isOk = new UIButton(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000523")/*确定*/);
			isOk.addActionListener(this);
		}
		return isOk;
	}
	//取消
	protected UIButton getIsCancle() {
		if (isCancle == null) {
			isCancle = new UIButton(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000464")/*取消*/);
			isCancle.addActionListener(this);
		}
		return isCancle;
	}

	private UIButton Cancle = null;

	protected UIButton getCancle() {
		if (Cancle == null) {
			Cancle = new UIButton(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000242")/*全不选*/);
			Cancle.addActionListener(this);
		}
		return Cancle;
	}

	private UIButton selectAll = null;

	protected UIButton getSelectAll() {
		if (selectAll == null) {
			selectAll = new UIButton(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000241")/*全选*/);
			selectAll.addActionListener(this);

		}
		return selectAll;
	}
	//浮动区汇总方式
	private UIComboBox getVarStyle() {
		if (checkBox == null) {
			checkBox = new UIComboBox(varSumTypeMap.keySet().toArray(new String[0]));
			checkBox.setBackground(c);
			checkBox.setEditable(false);
			checkBox.setSelectedIndex(0);
			checkBox.addActionListener(this);
			checkBox.setEnabled(false);//2013-05-23 修改 ,v63暂时不放开浮动各种汇总方式.
			checkBox.setVisible(false);

		}
		return checkBox;
	}

	private  UIComboBox  sumDimDefBox=null;
	/**
	 * 获取需要汇总的维度定义
	 * @return
	 */
	private  UIComboBox   getSumDimDefBox(){
		if(sumDimDefBox == null){

			Map<DimDef,DimLevel > map = TBSumPlanCommonToolsWithExcel.getDimLevelFromTask(mdtask);
			sumDimDefBox = new   UIComboBox(map.keySet().toArray(new DimDef[0]));
			for (DimLevel dmlv : map.values()) {
				//这么做的目的是默认进来第一个汇总维度就是主体
				if(dmlv.getBusiCode().equals(IDimLevelCodeConst.ENTITY)){
					sumDimDefBox.setSelectedItem(dmlv.getDimDef());
				}
			}

		}
		sumDimDefBox.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED){//Jcombobox的元素有两种状态,ItemListener.itemStateChanged与itemState有关
					DimDef  sumdimdef  =(DimDef)sumDimDefBox.getSelectdItemValue();
					setToBeSumeddimdef(sumdimdef);
					originalTreeModel=null;
					initLeftTree(sumdimdef);
					if(sumdimdef.getBusiCode().equals("TIME")||sumdimdef.getBusiCode().equals("CURR")){
						getStepSum().setEnabled(false);
						getAllLeaf().setEnabled(false);
					}else{
						getStepSum().setEnabled(true);
						getAllLeaf().setEnabled(true);
					}
				}
			}

		});
		return sumDimDefBox;
	}

	/**
	 * 根据屏幕设置对话框大小
	 */
	public Dimension getSizeByScreen() {
		int width = Toolkit.getDefaultToolkit().getScreenSize().width * 4/ 7;
		int height = Toolkit.getDefaultToolkit().getScreenSize().height * 4 / 7;
		return new Dimension(Math.max(width, 700), height);
	}

	/**
	 * 界面居中
	 */
	protected void setContainerCenter() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenWidth = screenSize.width;
		int screenHight = screenSize.height;
		setLocation((screenWidth - getWidth()) / 2,
				(screenHight - getHeight()) / 2);
	}

	public SheetNameListModel getModel() {
		return model;
	}

	public void setModel(SheetNameListModel model) {
		this.model = model;
	}

	public DimMember getSelfdimMember() {
		return SelfdimMember;
	}

	public void setSelfdimMember(DimMember selfdimMember) {
		SelfdimMember = selfdimMember;
	}

	private UserLoginVO userLoginVO = null;
	private UserLoginVO getUserVo() {
		//UserInfo info = new UserInfo();
		if (userLoginVO == null) {
		/*UserLoginVO*/ userLoginVO = UserInfo.getUserLoginVO();
		userLoginVO.setLogTime(WorkbenchEnvironment.getServerTime());
		}
		return userLoginVO;
	}

	public String getFlag() {
		return sumStyle;
	}

	public void setFlag(String sumStyle) {
		this.sumStyle = sumStyle;
	}
	static{
		sumstyleinfomap.put(SUMSTYLE_DIRLOWER, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000280")/*汇总直接下级*/);
		sumstyleinfomap.put(SUMSTYLE_ALLLEAF,  NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000281")/*汇总所有末级*/);
		sumstyleinfomap.put(SUMSTYLE_USERDEF, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000282")/*自定义汇总*/);

		varSumTypeMap.put(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000260")/*默认*/, -1);
		varSumTypeMap.put(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000275")/*罗列汇总*/, 0);
		varSumTypeMap.put(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000276")/*分类汇总*/, 1);
		varSumTypeMap.put(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000277")/*不汇总 */, 2);

	}
	public DimLevel getSumedDimlevel() {
		return sumedDimlevel;
	}

	public void setSumedDimlevel(DimLevel sumedDimlevel) {
		this.sumedDimlevel = sumedDimlevel;
	}


	public DimDef getToBeSumeddimdef() {
		return toBeSumeddimdef;
	}

	public void setToBeSumeddimdef(DimDef toBeSumeddimdef) {
		this.toBeSumeddimdef = toBeSumeddimdef;
	}
	public String getNodetype() {
		return nodetype;
	}

	public void setNodetype(String nodetype) {
		this.nodetype = nodetype;
	}
}