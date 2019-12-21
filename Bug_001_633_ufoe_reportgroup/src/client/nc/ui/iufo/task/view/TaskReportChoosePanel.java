package nc.ui.iufo.task.view;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import com.ufida.iufo.pub.tools.AppDebug;

import nc.desktop.ui.WorkbenchEnvironment;
import nc.impl.iufo.utils.BusiPropUtil;
import nc.impl.iufo.utils.ReportSrvUtil;
import nc.pub.iufo.basedoc.OrgUtil;
import nc.pub.iufo.cache.ReportCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.exception.UFOSrvException;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.trade.component.IListDataViewer;
import nc.ui.trade.component.IListDataViewerFactory;
import nc.ui.trade.component.TreeListDataDefaultViewer;
import nc.ui.uap.rbac.DefaultListDataViewerFactory;
import nc.ui.uap.rbac.ListDataListViewer;

import nc.ui.uif2.AppEvent;
import nc.ui.uif2.AppEventListener;
import nc.ui.uif2.components.AutoShowUpEventSource;
import nc.ui.uif2.components.IAutoShowUpComponent;
import nc.ui.uif2.components.IAutoShowUpEventListener;
import nc.ui.uif2.components.IComponentWithActions;
import nc.ui.uif2.components.ITabbedPaneAwareComponent;
import nc.ui.uif2.components.ITabbedPaneAwareComponentListener;
import nc.ui.uif2.components.TabbedPaneAwareCompnonetDelegate;

import nc.ui.iufo.constants.IResMngConsants;
import nc.ui.iufo.task.view.TaskReportChooseModel.GroupObject;
import nc.ui.iufo.uf2.BusipropLoginContext;

import nc.util.iufo.pub.IDMaker;
import nc.utils.iufo.TaskSrvUtils;
import nc.vo.bd.accessor.IBDData;
import nc.vo.iufo.resmng.ResUserPermissionUtils;
import nc.vo.iufo.task.ICommitConfigConstant;
import nc.vo.iufo.task.TaskRep;
import nc.vo.iufo.task.TaskRepDir;
import nc.vo.iufo.task.TaskReportVO;
import nc.vo.iufo.task.TaskVO;
import nc.vo.iuforeport.rep.ReportDirVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;

/**
 * ���񱨱�ѡ�����
 * @author xulm
 * @created at 2010-5-25,����07:07:03
 *
 */
public class TaskReportChoosePanel extends JPanel implements AppEventListener,IComponentWithActions,ITabbedPaneAwareComponent, IAutoShowUpComponent{

	private static final long serialVersionUID = 872622773543866077L;
	private AutoShowUpEventSource autoShowUpComponent;
	private TabbedPaneAwareCompnonetDelegate tabbedPaneAwareComponent;
	private BusipropLoginContext context;
	private TaskReportListToListPanel listToListPanel = null;
	private TaskReportChooseModel chooserModel =null;
//	private TaskVO task=null;
	private List<Action> actions = null;
	private boolean isCalcGroupView = false;
	
	private Object[] uiTaskReports = null;

//	public TaskVO getTask() {
//		return task;
//	}
//	public void setTask(TaskVO task) {
//		this.task = task;
//	}

	@Override
	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions)
	{
		this.actions = actions;
	}

	public TaskReportChooseModel getChooserModel() {
		if (chooserModel==null)
		{
			chooserModel= new TaskReportChooseModel(null,null);
		}
		return chooserModel;
	}

	public void initUI() {
		setLayout(new BorderLayout());
		add(getListToListPanel(), BorderLayout.CENTER);
		autoShowUpComponent = new AutoShowUpEventSource(this);
		tabbedPaneAwareComponent = new TabbedPaneAwareCompnonetDelegate();
	}

	public TaskReportListToListPanel  getListToListPanel() {
		if(listToListPanel == null){
			IListDataViewerFactory leftDataViewerFactory = new IListDataViewerFactory(){
				@Override
				public IListDataViewer createIListDataViwer() {
					return new TreeListDataDefaultViewer(new TaskReportTreeCreateStrategy(),new TaskReportTreeCellRenderer(false));
				}
			};


			//�������
			IListDataViewerFactory rightDataViewerFactory2 = new IListDataViewerFactory(){
				@Override
				public IListDataViewer createIListDataViwer() {
					return new TaskRepChooseTreeViewer(new TaskReportBusinessTreeCreateStrategy(),new TaskReportTreeCellRenderer(true));
				}
			};

			//����
			IListDataViewerFactory rightDataViewerFactory = new DefaultListDataViewerFactory(){
				@SuppressWarnings("serial")
				@Override
				public IListDataViewer createIListDataViwer() {
					ListDataListViewer viewer=new ListDataListViewer();
					viewer.setCellRenderer(new DefaultListCellRenderer(){
						@Override
						public Component getListCellRendererComponent(
								JList list, Object value, int index,
								boolean isSelected, boolean cellHasFocus) {
							Object showVal=value;
							Color color=null;
							if (value instanceof ReportVO){
								ReportVO report=(ReportVO)value;
								showVal=report.toString();

								if (report.getFmledit_type()!= null &&report.getFmledit_type().intValue()==ICommitConfigConstant.COMMIT_REP_MUSTINPUT.intValue()){
									showVal=showVal+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1029")/*@res " (�ر���¼)"*/;
									color=Color.red;
								}else if (report.getFmledit_type()!= null && report.getFmledit_type().intValue()==ICommitConfigConstant.COMMIT_REP_MUSTTCOMMIT.intValue()){
									showVal=showVal+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1030")/*@res " (�ر��Ǳ�¼)"*/;
									color=Color.blue;
								}else if (report.getFmledit_type()!= null){
									showVal=showVal+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1031")/*@res " (�Ǳر�)"*/;
								}
							}else if (value instanceof TaskRep){
								TaskRep report=(TaskRep)value;
								showVal=report.toString();

								if (report.getCommitattr()!= null &&report.getCommitattr().intValue()==ICommitConfigConstant.COMMIT_REP_MUSTINPUT.intValue()){
									showVal=showVal+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1029")/*@res " (�ر���¼)"*/;
									color=Color.red;
								}else if (report.getCommitattr()!= null && report.getCommitattr().intValue()==ICommitConfigConstant.COMMIT_REP_MUSTTCOMMIT.intValue()){
									showVal=showVal+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1030")/*@res " (�ر��Ǳ�¼)"*/;
									color=Color.blue;
								}else if (report.getCommitattr()!= null){
									showVal=showVal+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1031")/*@res " (�Ǳر�)"*/;
								}
							}

							JLabel label=(JLabel)super.getListCellRendererComponent(list, showVal, index, isSelected,cellHasFocus);
							if (color!=null)
								label.setForeground(color);

							list.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
							return label;
						}
					});
					return viewer;
				}
			};
//			listToListPanel = new TaskReportListToListPanel(getLeftTitle(),null,getRightTitle(),null,getChooserModel(),false,leftDataViewerFactory,rightDataViewerFactory2);

			listToListPanel = new TaskReportListToListPanel(getLeftTitle(),null,getRightTitle(),null,getChooserModel(),false,leftDataViewerFactory,rightDataViewerFactory,rightDataViewerFactory2);
//			listToListPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
		}
		if(isCalcGroupView()){
			listToListPanel.remove(listToListPanel.getLeftPanel());
			listToListPanel.remove(listToListPanel.getMovePane());
		}else{
			listToListPanel.initLayout();
		}
		return listToListPanel;
	}


	public String getLeftTitle() {
		return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0168")/*@res "��ѡ�������"*/;
	}


	public String getRightTitle() {
		return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0169")/*@res "��ѡ�������"*/;
	}


	@Override
	public void handleEvent(AppEvent event) {

	}


	public void disableChooseButton(){
		getListToListPanel().disableChooseButton();
	}

	public void enableChooseButton(){
		getListToListPanel().enableChooseButton();
	}

	public void initLeftAndRightDatas(TaskVO task){
		if(task == null || task.getPk_keygroup() == null){
			setLeftAndRightData(null, null);
			updateUI();
		    return;
		}
		try {
//			this.task=task;

			String userId = WorkbenchEnvironment.getInstance().getLoginUser().getCuserid();
			String pk_group = WorkbenchEnvironment.getInstance().getGroupVO().getPk_group();
			String reportPerSql = ResUserPermissionUtils.getUfoDocUsePermission(userId, pk_group,IResMngConsants.RES_CODE_REPORT,IResMngConsants.OPER_CODE_DEFAULT);

			String[] orgs = TaskSrvUtils.getTaskRefOrgIds(getContext());
			Object[] leftDatas = ReportSrvUtil.getOrgReportsByKeyGroup(orgs, task.getPk_keygroup(),getContext().getPk_busiprop(),reportPerSql);
			if(leftDatas != null){
				//��ȡ��֯Ŀ¼
				leftDatas = getLeftDataVOs(orgs,leftDatas);
			}

			Object[] rightDatas=getGroupObjectByTaskId(task.getPk_task());

			if (leftDatas!=null && leftDatas.length !=0) {
				ArrayList<Object> lstLeftData= new ArrayList<Object>(Arrays.asList(leftDatas));

				if (rightDatas!=null && rightDatas.length !=0)
				{

					ArrayList<Object> lstRightData= new ArrayList<Object>(Arrays.asList(rightDatas));
					for(int i=0;i<lstLeftData.size();i++)
					{
						for (int j=0;j<lstRightData.size();j++)
						{
							if (lstLeftData.get(i) instanceof ReportVO && lstRightData.get(j) instanceof TaskRep && ((ReportVO)lstLeftData.get(i)).getPk_report().equals(((TaskRep)lstRightData.get(j)).getPk_report()))
							{
								lstLeftData.remove(i);
								if (i>0)
								{
									i=i-1;
								}

								break;
							}
						}
					}

					rightDatas=lstRightData.toArray();
				}
				leftDatas=lstLeftData.toArray();
				for (Object obj:leftDatas){
					if (obj instanceof ReportVO){
						ReportVO report=(ReportVO)obj;
						report.setFmledit_type(ICommitConfigConstant.COMMIT_REP_NOMUSTCOMMIT);
					}
				}
			}

			setLeftAndRightData(leftDatas, rightDatas);
			updateUI();
		} catch (BusinessException e) {
			AppDebug.debug(e);
			MessageDialog.showErrorDlg(getContext().getEntranceUI(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0075")/*@res "����"*/, e.getMessage());
		}
	}


	/**
	 * Ϊ����������ù�������֯Ŀ¼
	 * @create by jiaah at 2011-7-23,����02:40:56
	 * @param orgs
	 * @return
	 */
	private Object[] getLeftDataVOs(String[] orgs,Object[] leftDatas){
		//��ȡ��֯Ŀ¼
		List<ReportDirVO> lstOrgDir = new ArrayList<ReportDirVO>();

		String busiPropName = "";
		String busiPk = getContext().getPk_busiprop();
		IBDData defaultData = BusiPropUtil.getDefaultBusiProp();
		if (busiPk != null	&& !busiPk.equals(defaultData.getPk())) {
			busiPropName = "("+ BusiPropUtil.getPropNameByPK(busiPk) + ")";
		}

		for(String s : orgs){
			ReportDirVO vo = new ReportDirVO();
			vo.setPk_dir(s);
			vo.setPk_parent("~");
			vo.setDir_name(OrgUtil.getOrgName(s) + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1032")/*@res "�������"*/+busiPropName);
			vo.setPk_org(s);
			lstOrgDir.add(vo);
		}

		List<SuperVO> lstLefts = new ArrayList<SuperVO>();
		lstLefts.addAll(lstOrgDir);
		for(Object o :leftDatas)
			lstLefts.add((SuperVO) o);
		leftDatas = lstLefts.toArray(new Object[lstLefts.size()]);
		return leftDatas;
	}

	public void initRightDatas(TaskVO task)
	{
		Object[] rightDatas=null;
		if (task!=null)
		{
		   try {
		       rightDatas = getGroupObjectByTaskId(task.getPk_task());
		   } catch (UFOSrvException e) {
				AppDebug.debug(e);
				MessageDialog.showErrorDlg(getContext().getEntranceUI(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0075")/*@res "����"*/, e.getMessage());
		    } catch (BusinessException e) {
				AppDebug.debug(e);
				MessageDialog.showErrorDlg(getContext().getEntranceUI(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0075")/*@res "����"*/, e.getMessage());
		    }
		}
		setLeftAndRightData(null, rightDatas);
		updateUI();
	}


	public void setLeftAndRightData(Object[] leftDatas,Object[] rightDatas){
        getListToListPanel().setLeftAndRightData(leftDatas, rightDatas);
        getListToListPanel().getRightList().setListData(chooserModel.getRightData());
	}

	/**
	 * ����������Ϊ��
	 * @create by jiaah at 2011-3-15,����01:39:57
	 *
	 * @param leftDatas
	 * @param rightDatas
	 */
	public void setLeftData(Object[] leftDatas){
        getListToListPanel().setLeftData(leftDatas);
	}


	/**
	 * ��ȡ�������õı�����Ϣ
	 * @param�Ƿ���Ҫ���˷���vo
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "static-access" })
	public TaskReportVO[] getSelectTaskReport(String taskId) throws Exception{
		Object[] right= chooserModel.getRightData();
		ArrayList<TaskReportVO> taskReports = new ArrayList<TaskReportVO>();
		boolean isExistSort = false;
		if (right!=null && right.length>0){
			//���֧����ק�Ļ���ReportDirVOδ����ReportVoǰ�棬������Ҫ�Ȼ�ȡ���з���Ŀ¼����Ϣ
			final Map<String, TaskRepDir> mapReportDirs = new HashMap<String,TaskRepDir>();
			List<TaskRep> lstRepVos = new ArrayList<TaskRep>();
			int dirSeq = 0;
			for (Object element : right) {
				if (element instanceof TaskRepDir) {
					TaskRepDir dirVO = (TaskRepDir) element;
					//֮ǰ�������ReportDirVO�������ݣ������ڽ�dir_type �洢����˳���ˡ�����
					dirVO.setType_dir_order(dirSeq);
					mapReportDirs.put(dirVO.getPk_dir(),dirVO);
					dirSeq++;
				}else if(element instanceof TaskRep){
					TaskRep taskRep = (TaskRep)element;
					lstRepVos.add(taskRep);
				}
			}

			isExistSort = mapReportDirs.size()>0;
			int repSeq = 0;
			Map<String,Integer> repSeqMap = new HashMap<String, Integer>();
			if(!isCalcGroupView){
//				List<ReportVO> repList = (List<ReportVO>) DeepCopyUtilities.copy(lstRepVos);
/*				Collections.sort(lstRepVos, new Comparator<TaskRep>() {
					@Override
					public int compare(TaskRep o1, TaskRep o2) {

						int i = 0;
						if(o1.getPk_dir() != null && o2.getPk_dir() != null){//Ҫ���з��࣬һ�����з��ࡣ
							TaskRepDir d1 = mapReportDirs.get(o1.getPk_dir());
							TaskRepDir d2 = mapReportDirs.get(o2.getPk_dir());
							Integer dirOrder1 = d1.getType_dir_order();
							Integer dirOrder2 = d2.getType_dir_order();
							i = dirOrder1.intValue() - dirOrder2.intValue();
						}
						if(i == 0){
							Integer repSeq1 = o1.getReport_order();
							Integer repSeq2 = o2.getReport_order();
							if(repSeq1 != null && repSeq2 != null){
								i  = repSeq1.intValue() - repSeq2.intValue();
							}
						}

						return i;
					}
				});*/
//				String tempDir = null; // ��¼����
//				for (TaskRep taskRep : lstRepVos) {
//					if(taskRep instanceof GroupObject)
//						continue;
//					if(tempDir != null && !tempDir.equals(taskRep.getType_dir())){
//						repSeq =0;
//					}
//					repSeqMap.put(taskRep.getPk_taskreport(), repSeq);
//					tempDir = taskRep.getType_dir();
//					repSeq++;
//				}
				String tempDir = null; // ��¼����
				for (TaskRep taskRep : lstRepVos) {
					if(taskRep instanceof GroupObject)
						continue;
					if(isExistSort && tempDir != null && !tempDir.equals(taskRep.getPk_dir())){
						repSeq =0;
					}
					repSeqMap.put(taskRep.getPk_taskreport(), repSeq);
					tempDir = taskRep.getPk_dir();
					repSeq++;
				}
			}


			TaskRep[] repVos = lstRepVos.toArray(new TaskRep[0]);
			if(repVos != null && repVos.length > 0){
				int group_number = 0;
				int group_position = 0;
				for (TaskRep repVO : repVos) {
					//������Ǽ�����顣��������������
					if(!isCalcGroupView && repVO instanceof GroupObject){
						continue;
					}
			    	if (repVO instanceof GroupObject){
			    		if(taskReports.size()!=0) {
			    			group_number=group_number+1;
				    		group_position=0;
			    		}
			    		continue;
			    	}
		    		TaskReportVO taskReport=new TaskReportVO();
		    		if(!isCalcGroupView){//TODO
//		    			group_number = repVO.getGroup_number();
//		    			group_position = repVO.getGroup_position();
		    		}
		    		taskReport.setGroup_number(group_number);
		    		taskReport.setGroup_position(group_position);
		    		taskReport.setPk_task(taskId);
		    		taskReport.setPk_report(repVO.getRepVO().getPk_report());
		    		taskReport.setCommitattr(repVO.getCommitattr());
		    		taskReport.setRepcode(repVO.getRepVO().getCode());
		    		
		    		taskReport.setPk_taskreport(repVO.getPk_taskreport());


		    		//���ñ�¼���ر�����
		    		if (repVO.getCommitattr()!= null && repVO.getCommitattr().intValue()==ICommitConfigConstant.COMMIT_REP_MUSTINPUT.intValue()){
		    			taskReport.setBcommit(UFBoolean.TRUE);
			    		taskReport.setBinput(UFBoolean.TRUE);
					}else if (repVO.getCommitattr()!= null && repVO.getCommitattr().intValue()==ICommitConfigConstant.COMMIT_REP_MUSTTCOMMIT.intValue()){
						taskReport.setBcommit(UFBoolean.TRUE);
			    		taskReport.setBinput(UFBoolean.FALSE);
					}else if(repVO.getCommitattr()!= null){
						taskReport.setBcommit(UFBoolean.FALSE);
			    		taskReport.setBinput(UFBoolean.FALSE);
					}

		    		taskReport.setId(IDMaker.makeID(20));
		    		//ReportVO �� pk_org�д洢�����ı������
		    		String pk_dir=repVO.getPk_dir();

		    		//����б���ҵ�����Ļ�����ô���еı���Ӧ�ý����ڷ�����
		    		if (mapReportDirs.size()>0){
			    		if (pk_dir == null || (mapReportDirs.get(pk_dir)==null)){
		    			    throw new Exception(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1033")/*@res "������������񱨱��ҵ����࣬�����еı���Ӧ�����񱨱�ҵ�������"*/);
		    			}
		    		}
		    		TaskRepDir repDir = mapReportDirs.get(pk_dir);
		    		String type_dir= repDir ==null? "": repDir.getType_dir();

		    		taskReport.setType_dir(type_dir==null?"":type_dir);
		    		taskReports.add(taskReport);

		    		if(!isCalcGroupView){
		    			taskReport.setReport_order(repSeqMap.get(repVO.getPk_taskreport()));
			    		//ע�⡣�����񱨱���ಢû�ж�Ӧ��VO���˴�ֻ�ǽ�ReportDirVO���洢���ݶ��ѡ�ͬ��ǰ�����������ReportDirVO��dir_type���洢�������š�
			    		Integer type_dir_order = repDir == null ? null : repDir.getType_dir_order();
			    		taskReport.setType_dir_order(type_dir_order);
		    		}

		    		group_position++;
				}
			}
		}
		return taskReports.toArray(new TaskReportVO[0]);
	}


	/**
	 * ��ȡ������������(��ҵ����飬������飬�Լ�����)
	 * @param taskId
	 * @return
	 * @throws BusinessException
	 */
	private  Object[] getGroupObjectByTaskId(String taskId) throws BusinessException
	{
		TaskReportVO[] taskReport = null;
		if(uiTaskReports != null && uiTaskReports.length > 0) {
			List<TaskReportVO> tempRepList = new ArrayList<TaskReportVO>();
			for(Object uiTaskReprot : uiTaskReports) {
				if(uiTaskReprot instanceof TaskReportVO) {
					String repPk = ((TaskReportVO) uiTaskReprot).getPk_report();
					if(repPk != null) {
						tempRepList.add((TaskReportVO) uiTaskReprot);
					}
				}
			}
			
			if(isCalcGroupView) {
				// ���������ͼ����������
				Collections.sort(tempRepList, new Comparator<TaskReportVO>() {
					@Override
					public int compare(TaskReportVO o1, TaskReportVO o2) {
						if(o1.getGroup_number() == null || o2.getGroup_number() == null) {
							return 0;
						}
						if(o1.getGroup_number().intValue() == o2.getGroup_number().intValue()) {
							if(o1.getGroup_position() == null || o2.getGroup_position() == null) {
								return 0;
							}
							return o1.getGroup_position() - o2.getGroup_position();
						} else {
							return o1.getGroup_number() - o2.getGroup_number();
						}
					}
				});
			}
			
			taskReport = tempRepList.toArray(new TaskReportVO[tempRepList.size()]);
		} else {
			taskReport= TaskSrvUtils.getTaskReportByTaskId(taskId);
		}
		
		ReportCache repCache = UFOCacheManager.getSingleton().getReportCache();
		List<Object> lstObject = new ArrayList<Object>();

		if (taskReport!=null && taskReport.length>0)
		{
			
			resumeCalcGroupInfo(taskReport);
			
//			Map<String, ReportDirVO> mapReportDirs = new HashMap<String,ReportDirVO>();
			Map<String, TaskRepDir> mapReportDirs = new HashMap<String,TaskRepDir>();
			for (TaskReportVO element : taskReport) {
				if (element.getType_dir()!=null && element.getType_dir().toString().trim().length()>0)
				{
					String type_dir=element.getType_dir().toString().trim();
					if (mapReportDirs.get(type_dir)==null)
					{
						TaskRepDir td = new TaskRepDir();
						td.setType_dir(type_dir);
						td.setType_dir_order(element.getType_dir_order());
						td.setPk_dir(IDMaker.makeID(20));
						mapReportDirs.put(type_dir, td);
						lstObject.add(td);
					}
				}
			}

			int group_number=0;
			for (TaskReportVO element : taskReport) {
				ReportVO reportVO=repCache.getByPK(element.getPk_report());

				if (reportVO!=null){
					TaskRep tr = new TaskRep(reportVO);
//					reportVO=getNewReportVO(reportVO);
					tr.setPk_taskreport(IDMaker.makeID(20));
					if (element.getType_dir()!=null && element.getType_dir().toString().trim().length()>0)
					{
						String type_dir=element.getType_dir().toString().trim();
						if (mapReportDirs.get(type_dir)!=null)
						{
//							reportVO.setPk_org(mapReportDirs.get(type_dir).getPk_dir());
							tr.setPk_dir(mapReportDirs.get(type_dir).getPk_dir());
							tr.setPk_taskreport(element.getPk_taskreport());
							tr.setReport_order(element.getReport_order());
							tr.setType_dir(element.getType_dir());
						}
					}

					if (element.getGroup_number() != null && !element.getGroup_number().equals(group_number))
					{
						group_number=element.getGroup_number();
						GroupObject groupObject = (GroupObject) chooserModel.createGroupObject();
						groupObject.setName(groupObject.getCode() + "("+group_number +")");
						lstObject.add(groupObject);
					}
//					reportVO.setFmledit_type(element.getCommitattr());
					//TODO
					tr.setCommitattr(element.getCommitattr());



					lstObject.add(tr);
				}
			}
		}
		return lstObject.toArray(new Object[0]);
	}
	
	private void resumeCalcGroupInfo(TaskReportVO[] reportVos) {
		// ��ѡ�񱨱�
		if(uiTaskReports == null || uiTaskReports.length == 0) {
			// ��û��ѡ�������
			return;
		}
		// ������Ϣ�ָ�Ϊ�������ݵķ�����Ϣ
		Map<String, Integer> groupNumMap = new HashMap<String, Integer>();
		Map<String, Integer> groupPosMap = new HashMap<String, Integer>();
		for(Object o : uiTaskReports){
			if(o != null && ((TaskReportVO)o).getPk_report() != null){
				groupNumMap.put( ((TaskReportVO)o).getPk_report(), ((TaskReportVO)o).getGroup_number());
				groupPosMap.put( ((TaskReportVO)o).getPk_report(), ((TaskReportVO)o).getGroup_position());
			}
		}
		
		for(TaskReportVO vo : reportVos){
			vo.setGroup_number(groupNumMap.get(vo.getPk_report()));
			vo.setGroup_position(groupPosMap.get(vo.getPk_report()));
		}
		
		// ������Ϣ�ָ�Ϊ�������ݵķ�����Ϣ
		Map<String, String> map = new HashMap<String, String>();
		for(Object o : uiTaskReports){
			if(o != null && ((TaskReportVO)o).getPk_report() != null){
				map.put( ((TaskReportVO)o).getPk_report(), ((TaskReportVO)o).getType_dir());
			}
		}
		//ȡ��ģ���еı���˳��ͷ���˳�򡣡�
		Map<String,Integer[]> sortSeqMap = new HashMap<String, Integer[]>(uiTaskReports.length);
		for (Object object : uiTaskReports) {
			TaskReportVO tr = (TaskReportVO)object;
			sortSeqMap.put(tr.getPk_task()+"@"+tr.getPk_report(),new Integer[]{tr.getType_dir_order(),tr.getReport_order()});
		}

		String key = null;
		Integer[] sortSeq = null;
		for(TaskReportVO vo : reportVos){
			vo.setType_dir(map.get(vo.getPk_report()));
			key = vo.getPk_task()+"@"+vo.getPk_report();
			sortSeq = sortSeqMap.get(key);
			if(sortSeq != null) {
				vo.setType_dir_order(sortSeq[0]);
				vo.setReport_order(sortSeq[1]);
			}
		}
	}

	private ReportVO getNewReportVO(ReportVO report){
		ReportVO rep=new ReportVO();
		rep.setPk_report(report.getPk_report());
		rep.setPk_key_comb(report.getPk_key_comb());
		rep.setName(report.getChangeName());
		rep.setName2(report.getName2());
		rep.setName3(report.getName3());
		rep.setCode(report.getCode());

		return rep;
	}


	/**
	 * �õ�ѡ��ı���ID����
	 * @return
	 */
    public String[][] getSelectReportIds()
    {
		Object[] objs =getListToListPanel().getRightData();
	    List<String> lstDatas = new ArrayList<String>();
		if (objs != null && objs.length > 0)
		{
			for (Object obj : objs) {
				if (obj instanceof ReportVO) {
					lstDatas.add(((ReportVO) obj).getPk_report());
				}
			}
		}
		String[][] aryRepIds = new String[1][];
		aryRepIds[0] = lstDatas.toArray(new String[0]);
		return aryRepIds;
    }


    public void SwitchView()
    {
    	 getListToListPanel().SwitchView();
    }

	public BusipropLoginContext getContext() {
		return context;
	}

	public void setContext(BusipropLoginContext context) {
		this.context = context;
	}
	@Override
	public void addTabbedPaneAwareComponentListener(ITabbedPaneAwareComponentListener l) {
		tabbedPaneAwareComponent.addTabbedPaneAwareComponentListener(l);
	}

	@Override
	public boolean canBeHidden() {
		return tabbedPaneAwareComponent.canBeHidden();
	}

	@Override
	public boolean isComponentVisible() {
		return tabbedPaneAwareComponent.isComponentVisible();
	}

	@Override
	public void setComponentVisible(boolean visible) {
		tabbedPaneAwareComponent.setComponentVisible(visible);

	}

	@Override
	public void setAutoShowUpEventListener(IAutoShowUpEventListener l) {
		autoShowUpComponent.setAutoShowUpEventListener(l);
	}

	@Override
	public void showMeUp() {
		autoShowUpComponent.showMeUp();

	}

	public boolean isCalcGroupView() {
		return isCalcGroupView;
	}
	public void setCalcGroupView(boolean isCalcGroupView) {
		this.isCalcGroupView = isCalcGroupView;
	}
	public void setUiTaskReports(Object[] uiTaskReports) {
		this.uiTaskReports = uiTaskReports;
	}
	public Object[] getUiTaskReports() {
		return uiTaskReports;
	}


}