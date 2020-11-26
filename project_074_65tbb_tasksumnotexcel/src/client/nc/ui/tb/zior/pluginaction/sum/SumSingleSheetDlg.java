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
 * ���ܽ���
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
	//private String[] varType = { NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000260")/*Ĭ��*/, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000275")/*���л���*/, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000276")/*�������*/, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000277")/*������ */ };
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
	
	// lrx 2015-8-4 ��¼����Ҫͨ�����ܸ���״̬������
	private HashSet<String> updStatusTaskPks;
	
	
	private boolean executeExcel = true;

	/**
	 * ����dlg���췽��
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
	 * ����dlg���췽��
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
	 * �ù��췽��רΪ�������ӡʹ��
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
 * ��ʼ������
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
	 * ��panel
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
					MessageDialog.showErrorDlg(this, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*��ʾ*/, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0243")/*@res "��ѡ����Ҫ���ܵı�"*/);
					return;
				}
			} catch (BusinessException e2) {
				e2.printStackTrace();
			}

			 Object[] Selectorg = getTreePanel().getSelectBusiObjs();
				if (sumStyle.equals(SUMSTYLE_USERDEF)) {
					if(Selectorg == null){
						//MessageDialog.showErrorDlg(this, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*��ʾ*/, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000279")/*��ѡ����ܵ�λ����л���*/);
						MessageDialog.showErrorDlg(this, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*��ʾ*/, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0244")/*@res "��ѡ��"*/+((DimDef)getSumDimDefBox().getSelectdItemValue()).getObjName()+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0245")/*@res "����л���"*/);
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
							MessageDialog.showErrorDlg(this, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0100")/*@res "��ʾ"*/, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0246")/*@res "�����ڼƻ��ڼ��Ͽ�㼶����,������ѡ������ڼ�!"*/);
						}else{
							closeOK();
						}
					}
				}else if (sumStyle.equals(SUMSTYLE_LEVLEByLEVLE)){//20141028 �𼶻��ܸ���ʾ
					String where =TBSumPlanCommonToolsWithExcel.getQuerySqlByTask(mdtask,getSumedDimlevel());
					MdTask[] tasks = null;
					try {
						tasks = TbTaskCtl.getMdTasksByWhere(where.toString(), true);
					} catch (BusinessException e1) {
						NtbLogger.printException(e1);
						return;
					}
//					if(tasks!=null&&tasks.length > 1000){ //�жϵ�ǰ�ڵ�������¼�
					if(tasks!=null&&getTreePanel()!=null&&getTreePanel().getTreeModel()!=null&&getTreePanel().getTreeModel().aryVOs.size() > 1000){
						MessageDialog.showErrorDlg(this, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0100")/*@res "��ʾ"*/, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0247")/*@res "�𼶻��������ܳ���1000��!"*/);
						return;
					}
					Map<String,String> map = filterSubmitAndApprovePassTasks(tasks,getSumedDimlevel());
					Set<String> keySet = map.keySet();
					if(!NtbEnv.isOutLineUI){
						if(tasks.length!=keySet.size()){
							int result = MessageDialog.showYesNoDlg(this,nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan001-0002")/*@res "��ʾ"*/,
									 nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0248")/*@res "��ǰ�����¼������д���������Ч�ķ�ĩ������(���ϱ�/����ͨ��),���ǵ����ݽ����ᱻ�޸�,��������?"*/);
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
	/*2013-05-09��Ӷ����Զ�������������ܸ������Ļ��ܷ�ʽ--------21406Ŀǰδʹ��*/
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
					//NtbLogger.print(task.getObjname()+"--------------------->�Ѿ���Чû�н��л���!");
					continue;
				}
				HashMap<String, String> userInfo = new HashMap<String, String>();
				userInfo.put("sumStyle",NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000278")/*�𼶻���*/ );
				iPlanExtZjService.sumPlanVarAreaByParamDimLevel(task.getPrimaryKey(),getSumedDimlevel(), fromUnicode,
						getUserVo(),getSelectedSheetPks(),getsumVartype(),userInfo);
			}
		}

	}
	/*2013-05-09��Ӷ����Զ�������������ܸ������Ļ��ܷ�ʽ----------Ŀǰδʹ��*/
	public void doSumOnlyVar() throws BusinessException {
		String pk_taskTo = mdtask.getPermPrimary();
		Object[] Selectorg = getTreePanel().getSelectBusiObjs();
		if (Selectorg == null) {
			MessageDialog.showErrorDlg(this, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000480")/*��ʾ*/, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000279")/*��ѡ����ܵ�λ����л���*/);
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
	 * �𼶻����л�ȡ���ϱ�/������/�Ѷ�������
	 * @return ���������PK,������uniqcode��map
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
	 * doLevelSum  �𼶻���
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
		// lrx 2015-8-28 �𼶻��ܷŵ���̨����
		HashMap<String, String> userInfo = new HashMap<String, String>();
		userInfo.put("sumStyle",NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000278")/*�𼶻���*/ );
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
//				if (getSumEffectiveData().isSelected()) {//�Ƿ�ֻ�����ϱ�����
//					fromUnicode=filterUnSubmit(levelTable.get(target)).toArray(new String [0]);
//				}else{
//					fromUnicode =  levelTable.get(target).toArray(new String [0]);
//				}
//				HashMap<String, String> userInfo = new HashMap<String, String>();
//				userInfo.put("sumStyle",NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000278")/*�𼶻���*/ );
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
	 * doSum  ����
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
		// lrx 2015-11-9 �첽���ܷ�ʽ,�ݲ�����ʹ��
//		if ((sumparam.pk_sumSheets==null || sumparam.pk_sumSheets.length==0) && fromUnicode.length > 20 ||
//				sumparam.pk_sumSheets.length*fromUnicode.length > 200 || fromUnicode.length > 50) {
//			isAsynSum = MessageDialog.showYesNoDlg(mainBord, dlgTitle, "Ҫ���ܵ�����϶�,�Ƿ���Ҫͨ���첽��ʽ���л���?")==UIDialog.ID_YES;
//		}
		if (isAsynSum) {
			String taskName = PlanAsynSumCtl.asynSumPlan(getMdTaskDef(), sumparam);
			MessageDialog.showHintDlg(mainBord, dlgTitle, "��̨�첽��������["+taskName+"]�Ѿ�����,����ܽ�����ˢ������鿴ִ�н��.");
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
	 * ��ȡ��ѡ�е������PK
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
//	 * ��ǰ����Ŀɼ�sheet��Χ���¼�����һ�£����ࣩʱ����Ҫ�������¼�����û�г��ֵ�sheet���ܡ�
//	 * @param tasks
//	 * @return
//	 * @throws BusinessException
//	 */
//	public  String [] getValidSumSheetpks(List <MdTask> tasks) throws BusinessException{
//		Set <String> sheetPk = new HashSet<String>();
//		List <String> list = new ArrayList<String>();
//		for (MdTask task : tasks) {
//			String[] sheetpks = TbZiorUiCtl.getTaskValidLookPkSheets(task, null, false);//�¼�����ɼ�����Χ
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
	// ���������ܷ�ʽ
	private int getsumVartype() {
		return varSumTypeMap.get(getVarStyle().getSelectdItemValue());
	}

	/**
	 * ���˵�δ�ϱ����ݵ�����
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
//							/*	||tasks[i].getPlanstatus().equals(ITaskStatus.LOCAL_PROMOTED)ֻ�����ϱ�����ֻ��Զ��׶��ϱ�����,�޸���20140703*/)
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
	 * ˢ����
	 */
	public void refreshTreeSelect() {
		getTreePanel().getTreeModel().refreshTreeOpt();
		Object[] selectedObjs = null;
		// Ҫ��ֱ���¼����������¼����������Ž�һ������
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
	// �ýڵ���ѡ��״̬
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
	 * չ�����нڵ㷽��
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
	 * �𼶻���--��ȡÿһ�㼶node�Լ���parentNode
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
	 * ��ȡֱ���¼�node
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
	 * ��ȡ����ĩ��node
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
	 * ��ģ��
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
	 * ���������--ά�ȳ�Ա��Դ
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
				// ͳ�Ƴ�Ա(����֯)�Ļ���
				List<String> virtualChildpk = SelfdimMember.getAllVirtualChildrenOrg();
				if (virtualChildpk != null) { // ����֯�¿��ܲ�����ֵ
					for (String dimpk : virtualChildpk) {
						Map<DimLevel, Object> keys = new HashMap<DimLevel, Object>();
						keys.put(dimlv, dimpk);
						alldimMembersList.add(dmr.getMemberByLevelValueKeys(keys));
					}
				}
			} else {
				// ��ȡ��ǰ����������¼�
				List<DimMember> list = dmr.getAllChildren(SelfdimMember);
				//ͨ��Ȩ�޹���  --- qy modify 20150707
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
			// lrx 2015-11-30 DimDef�а�������ά��,sumedDimlevel����MdTask��Ϊnull����߲�ȡֵ(�����꼾�¶��ǲ���ά,����Ϊ�յ����,����DimLevelȡ��)
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
	 * ѡ�ťpanel
	 */
	private  UIPanel getOptionPanle() {
		if (optionPanle == null) {
			optionPanle = new UIPanel();
			
			ButtonGroup group = new ButtonGroup();
			group.add(getDirLower());
			group.add(getAllLeaf());
			group.add(getStepSum());
			group.add(getFreeSelect());
			
			// lrx 2015-9-8 ����������ʱ�����ؼ�λ���Ա���ʾ����
			String langcode = NCLangRes4VoTransl.getNCLangRes().getCurrLanguage().getCode();
			boolean isChi = langcode == null || "simpchn".equals(langcode) || "tradchn".equals(langcode);
			if (isChi) {
			FlowLayout layout = new FlowLayout();
			layout.setAlignment(FlowLayout.LEFT);
			optionPanle.setLayout(layout);

			//2014-5-19�����������ά�ֶ�����ѡ��
			optionPanle.add(new UILabel(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0249")/*@res "ѡ�����ά��:"*/));
			optionPanle.add(getSumDimDefBox());

			optionPanle.add(new UILabel(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0250")/*@res "���ܷ�ʽ:"*//*NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000283")ѡ����ܵ�λ: */));
			optionPanle.add(getDirLower());
			optionPanle.add(getAllLeaf());
			optionPanle.add(getStepSum());
			optionPanle.add(getFreeSelect());
			optionPanle.add(getSumEffectiveData());
			//optionPanle.add(new UILabel(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000284")/*��������ܷ�ʽ*/));
			optionPanle.add(getVarStyle());
			}
			else {
				optionPanle.setLayout(new GridLayout(2, 1));
				UIPanel p1 = new UIPanel();
				p1.setLayout(new FlowLayout(FlowLayout.LEFT));
				p1.add(new UILabel(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0249")/*@res "ѡ�����ά��:"*/));
				p1.add(getSumDimDefBox());
				p1.add(getSumEffectiveData());
				p1.add(getVarStyle());
				p1.setBackground(c);
				optionPanle.add(p1);
				UIPanel p2 = new UIPanel();
				p2.setLayout(new FlowLayout(FlowLayout.LEFT));
				p2.add(new UILabel(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0250")/*@res "���ܷ�ʽ:"*//*NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000283")ѡ����ܵ�λ: */));
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
 *	������
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
 * �������¼�����
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
	 * ����ͱ�ѡ��
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

	// �������б�
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
	// ȫѡ����ȡ����ť
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

	// ȷ��ȡ����ť
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
	 * ��ť
	 * @return
	 */
	//ֱ���¼�
	private UIRadioButton getDirLower() {
		if (dirLower == null) {
			dirLower = new UIRadioButton(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000285")/*ֱ���¼�*/);
			dirLower.setBackground(c);

		}
		dirLower.addActionListener(this);
		return dirLower;
	}
	//����ĩ��
	private UIRadioButton getAllLeaf() {
		if (allLeaf == null) {
			allLeaf = new UIRadioButton(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000245")/*����ĩ��*/);
			allLeaf.setBackground(c);
			allLeaf.addActionListener(this);
		}
		return allLeaf;
	}
	//�𼶻���
	private UIRadioButton getStepSum() {
		if (stepSum == null) {
			stepSum = new UIRadioButton(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000278")/*�𼶻���*/);
			stepSum.setBackground(c);
			stepSum.addActionListener(this);
		}
		return stepSum;
	}
	//�Զ������
	private UIRadioButton getFreeSelect() {
		if (freeSelect == null) {
			freeSelect = new UIRadioButton(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000282")/*�Զ������*/);
			freeSelect.setBackground(c);
			freeSelect.addActionListener(this);
			freeSelect.setSelected(true);
		}
		return freeSelect;
	}
	//ֻ������Ч����
	private UICheckBox getSumEffectiveData() {
		if (sumEffectiveData == null) {
//			sumEffectiveData = new UICheckBox(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000286")/*ֻ�����ϱ�����*/);
			sumEffectiveData = new UICheckBox(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("tbb_plan_0","01050plan002-0251")/*@res "ֻ�������ύ����"*/)/*ֻ�������ύ����*/;
			sumEffectiveData.setBackground(c);
			// sumEffectiveData.setSelected(true);//Ĭ�ϲ�ѡ
			sumEffectiveData.addActionListener(this);
		}
		return sumEffectiveData;
	}
	//ȷ��
	private  UIButton getIsOk() {
		if (isOk == null) {
			isOk = new UIButton(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000523")/*ȷ��*/);
			isOk.addActionListener(this);
		}
		return isOk;
	}
	//ȡ��
	protected UIButton getIsCancle() {
		if (isCancle == null) {
			isCancle = new UIButton(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000464")/*ȡ��*/);
			isCancle.addActionListener(this);
		}
		return isCancle;
	}

	private UIButton Cancle = null;

	protected UIButton getCancle() {
		if (Cancle == null) {
			Cancle = new UIButton(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000242")/*ȫ��ѡ*/);
			Cancle.addActionListener(this);
		}
		return Cancle;
	}

	private UIButton selectAll = null;

	protected UIButton getSelectAll() {
		if (selectAll == null) {
			selectAll = new UIButton(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000241")/*ȫѡ*/);
			selectAll.addActionListener(this);

		}
		return selectAll;
	}
	//���������ܷ�ʽ
	private UIComboBox getVarStyle() {
		if (checkBox == null) {
			checkBox = new UIComboBox(varSumTypeMap.keySet().toArray(new String[0]));
			checkBox.setBackground(c);
			checkBox.setEditable(false);
			checkBox.setSelectedIndex(0);
			checkBox.addActionListener(this);
			checkBox.setEnabled(false);//2013-05-23 �޸� ,v63��ʱ���ſ��������ֻ��ܷ�ʽ.
			checkBox.setVisible(false);

		}
		return checkBox;
	}

	private  UIComboBox  sumDimDefBox=null;
	/**
	 * ��ȡ��Ҫ���ܵ�ά�ȶ���
	 * @return
	 */
	private  UIComboBox   getSumDimDefBox(){
		if(sumDimDefBox == null){

			Map<DimDef,DimLevel > map = TBSumPlanCommonToolsWithExcel.getDimLevelFromTask(mdtask);
			sumDimDefBox = new   UIComboBox(map.keySet().toArray(new DimDef[0]));
			for (DimLevel dmlv : map.values()) {
				//��ô����Ŀ����Ĭ�Ͻ�����һ������ά�Ⱦ�������
				if(dmlv.getBusiCode().equals(IDimLevelCodeConst.ENTITY)){
					sumDimDefBox.setSelectedItem(dmlv.getDimDef());
				}
			}

		}
		sumDimDefBox.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED){//Jcombobox��Ԫ��������״̬,ItemListener.itemStateChanged��itemState�й�
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
	 * ������Ļ���öԻ����С
	 */
	public Dimension getSizeByScreen() {
		int width = Toolkit.getDefaultToolkit().getScreenSize().width * 4/ 7;
		int height = Toolkit.getDefaultToolkit().getScreenSize().height * 4 / 7;
		return new Dimension(Math.max(width, 700), height);
	}

	/**
	 * �������
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
		sumstyleinfomap.put(SUMSTYLE_DIRLOWER, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000280")/*����ֱ���¼�*/);
		sumstyleinfomap.put(SUMSTYLE_ALLLEAF,  NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000281")/*��������ĩ��*/);
		sumstyleinfomap.put(SUMSTYLE_USERDEF, NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000282")/*�Զ������*/);

		varSumTypeMap.put(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000260")/*Ĭ��*/, -1);
		varSumTypeMap.put(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000275")/*���л���*/, 0);
		varSumTypeMap.put(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000276")/*�������*/, 1);
		varSumTypeMap.put(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000277")/*������ */, 2);

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