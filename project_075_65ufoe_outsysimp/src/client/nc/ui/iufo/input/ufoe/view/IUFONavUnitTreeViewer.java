package nc.ui.iufo.input.ufoe.view;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import nc.bs.framework.common.NCLocator;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.itf.iufo.commit.ICommitQueryService;
import nc.itf.iufo.ufoe.vorp.IUfoeVorpQuerySrv;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pubitf.eaa.InnerCodeUtil;
import nc.ui.corg.importmember.OrgStruMemberTreeCreateStrategy;
import nc.ui.iufo.input.control.base.AbsRepDataControler;
import nc.ui.iufo.input.edit.base.AbsBaseRepDataEditor;
import nc.ui.iufo.input.key.OrgInputEditor;
import nc.ui.iufo.input.key.RefInputEditor;
import nc.ui.iufo.input.ufoe.comp.AbsCombRepDataControler;
import nc.ui.iufo.input.ufoe.comp.AbsNavUnitTreeViewer;
import nc.ui.iufo.input.ufoe.comp.OrgDCTreeCellRender;
import nc.ui.iufo.input.ufoe.control.IUFORepDataControler;
import nc.ui.iufo.input.ufoe.edit.IUFOCombRepDataEditor;
import nc.ui.iufo.input.ufoe.edit.IUFORepDataEditor;
import nc.ui.iufo.input.view.base.AbsKeyCondPanel;
import nc.ui.iufo.task.model.BusiPropTaskRefModel;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.beans.ValueChangedEvent;
import nc.ui.pub.beans.ValueChangedListener;
import nc.ui.queryarea.util.QueryAreaColor;
import nc.ui.ufoe.ref.RecieveTaskRefPanel;
import nc.util.iufo.pub.UFOString;
import nc.util.iufo.rms.RMSUtil;
import nc.utils.iufo.TaskSrvUtils;
import nc.utils.iufo.TotalOrgAttributeUtils;
import nc.vo.bd.userdefrule.UserdefitemVO;
import nc.vo.corg.ReportManaStruMemberWithCodeNameVO;
import nc.vo.iufo.commit.CommitStateEnum;
import nc.vo.iufo.commit.TaskCommitVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.task.ICommitConfigConstant;
import nc.vo.iufo.task.TaskInfoVO;
import nc.vo.iufo.task.TaskVO;
import nc.vo.iufo.total.ReportOrgInnerVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.org.ReportOrgVO;

import org.apache.commons.lang.StringUtils;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.zior.console.ActionHandler;
import com.ufida.zior.docking.core.Dockable;
import com.ufida.zior.docking.core.DockingManager;
import com.ufida.zior.docking.plaf.resources.ColorResourceHandler;
import com.ufida.zior.docking.view.actions.DefaultCloseAction;
import com.ufida.zior.docking.view.actions.DefaultMaximizeAction;
import com.ufida.zior.docking.view.actions.DefaultPinAction;
import com.ufida.zior.plugin.event.PluginActionEvent;
import com.ufida.zior.view.MainBoardContext;
import com.ufida.zior.view.Mainboard;
import com.ufida.zior.view.Viewer;
import com.ufsoft.iufo.fmtplugin.BDContextKey;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.inputlpugin.ufobiz.ufoe.total.OrgAttributeShowAction;
import com.ufsoft.iufo.view.AbsReportMiniAction;
import com.ufsoft.iuforeport.repdatainput.LoginEnvVO;
import com.ufsoft.report.util.UfoPublic;
import com.ufsoft.table.re.IInputEditor;

/**
 * 导航单位树面板
 * 
 * @author weixl
 * 
 */
@SuppressWarnings("restriction")
public class IUFONavUnitTreeViewer extends AbsNavUnitTreeViewer implements ValueChangedListener {
	private static final long serialVersionUID = 8232011838671032093L;

	transient public static String NAV_UNIT_TREE_ID = "iufo.input.dir_unit.view";

	private RecieveTaskRefPanel taskPane = null;

	// 组织自定义属性
	private UserdefitemVO[] m_Userdefitems = null;

	private TaskVO task = null;

	// editor tianjlc 2015-02-04 适配报表组织体系多版本添加，存储时间
	private String date = null;
	// 存储旧的任务PK
	protected String pk_task_old = null;
	// 是否第一次加载，减少远程调用
	private boolean isFirstLoad = true;
	// 日期是否需要转换
	private boolean isTransForm = true;

	// private UIPanel northmainpane = null;

	@Override
	public void startup() {
		super.startup();

		addTitleAction(new OrgAttributeShowAction(getMainboard()), 3);
		removeTitleAction(DefaultPinAction.class.getName());

		// wangqi 20130312 修改左侧树视图展开折叠的功能
		removeTitleAction(String.valueOf((new DefaultMaximizeAction()).getValue(Action.NAME)));
		removeTitleAction(String.valueOf((new DefaultCloseAction()).getValue(Action.NAME)));
		AbsReportMiniAction action = new AbsReportMiniAction(getMainboard());
		action.setMiniid("nc.ui.iufo.input.ufoe.view.IUFONaviUnitTreeMiniViewer");
		addTitleAction(action);
		// wangqi 20130312 修改左侧树视图展开折叠的功能 E
	}

	/**
	 * 将权限放开，在外部需要设置选中的组织
	 */
	@Override
	public void selectUnitTreeNode(String strUnitPK) {
		super.selectUnitTreeNode(strUnitPK);
	}

	@Override
	public void refresh() {
		if (treePane.getModel().getTreeCreateStrategy() instanceof OrgStruMemberTreeCreateStrategy) {
			initTreeModel();
		}
		// editor tianjlc at 2015-03-02 适配报表组织体系多版本添加，时间关键字值改变时，刷新树
		if (treePane.getModel().getTreeCreateStrategy() instanceof OrgMemberPKTreeCreateStrategy) {
			initTreeModel();
		} else {
			initTreeModelByOrgAttribute(this.m_Userdefitems);
		}
		IUFORepDataControler controler = (IUFORepDataControler) AbsRepDataControler.getInstance(getMainboard());
		selectUnitTreeNode(getNewSelecUnitTreeNodePK(controler.getSelectedUnitPK()));
	}

	/**
	 * 判断当前选中的组织PK是否在当前版本的报表组织体系版本中 若组织体系为空，返回null 若不在组织体系中，返回当前的根节点pk
	 * 在组织体系中，返回selectUnitPK
	 * 
	 * @creator tianjlc at 2015-3-6 上午9:17:37
	 * @param selectedUnitPK
	 * @return
	 * @return String
	 */
	public String getNewSelecUnitTreeNodePK(String selectedUnitPK) {
		Object[] allDatas = treePane.getModel().getAllDatas();
		if (allDatas.length == 0) {
			setUnitKeyValue(null);
			return null;
		}
		for (Object object : allDatas) {
			if (object instanceof ReportManaStruMemberWithCodeNameVO) {
				if (((ReportManaStruMemberWithCodeNameVO) object).getPk_org().equals(selectedUnitPK)) {
					setUnitKeyValue(selectedUnitPK);
					return selectedUnitPK;
				}
			} else if (object instanceof ReportOrgInnerVO) {
				ReportOrgVO reportOrgVO = ((ReportOrgInnerVO) object).getReportOrgVO();
				if (reportOrgVO.getPk_reportorg().equals(selectedUnitPK)) {
					setUnitKeyValue(selectedUnitPK);
					return selectedUnitPK;
				}
			}
		}
		setUnitKeyValue((String) getMainboard().getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK));
		return (String) getMainboard().getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK);

	}

	/**
	 * 设置选中的单位关键字的值
	 * 
	 * @creator tianjlc at 2015-3-6 上午10:44:51
	 * @param unitPK
	 * @return void
	 */
	private void setUnitKeyValue(String unitPK) {
		try {
			IInputEditor[] keyEditors = getM_keyCondPane().getM_jFieldKeywords();
			if (keyEditors != null) {
				for (IInputEditor iInputEditor : keyEditors) {
					if (iInputEditor instanceof OrgInputEditor) {
						keyEditors[0].setValue(unitPK);
					}
					if(iInputEditor instanceof RefInputEditor){
						if("部门".equals(((UIRefPane)((RefInputEditor)iInputEditor).getComponent()).getRefName())){
							((UIRefPane)((RefInputEditor)iInputEditor).getComponent()).setPk_org(unitPK);
						}
					}
				}
			}
			getM_keyCondPane().setM_jFieldKeywords(keyEditors);
		} catch (Exception e) {
			AppDebug.debug(e);
		}
	}

	@Override
	protected UIPanel getNorthPanel() {
		if (taskPane == null) {
			taskPane = new RecieveTaskRefPanel(true);
			taskPane.setValueChangeListener(this);
			// @edit by wuyongc at 2013-12-20,下午2:32:59
			final MainBoardContext context = getMainboard().getContext();
			BusiPropTaskRefModel model = new BusiPropTaskRefModel();
			model.setPk_group((String) context.getAttribute(BDContextKey.CUR_GROUP_PK));
			model.setRmsPK((String) context.getAttribute(IUfoContextKey.CUR_REPMNGSTRUCT_PK));

			model.setPk_org((String) context.getAttribute(IUfoContextKey.CUR_REPORG_PK));
			taskPane.setTaskRefModel(model);

			taskPane.innerInitUI();
			IUFORepDataControler controler = (IUFORepDataControler) AbsRepDataControler.getInstance(getMainboard());
			task = controler.getSelectedTaskInfo().getTaskVO();
			taskPane.getTask_refPane().setSelectedData(task.getPk_task(), task.getCode(), task.getName());
			taskPane.setBackground(ColorResourceHandler.parseHexColor("#EBEBEB"));
			task = null;
		}

		return taskPane;
	}

	// wangqi 20130627
	public String getTaskPK() {
		// edit by congdy 2013.6.28 有可能是从任务发布节点进入的，这时候pane并无实例
		if (taskPane == null) {
			return ((RecieveTaskRefPanel) getNorthPanel()).getTask_refPane().getRefPK();
		}
		return taskPane.getTask_refPane().getRefPK();
	}

	@Override
	public void onTreeSelectionChange(TreePath path) {
		AbsCombRepDataControler controler = (AbsCombRepDataControler) AbsRepDataControler.getInstance(getMainboard());
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
		if (treeNode.getUserObject() instanceof ReportManaStruMemberWithCodeNameVO) {
			ReportManaStruMemberWithCodeNameVO rmsMemVO = (ReportManaStruMemberWithCodeNameVO) treeNode.getUserObject();
			// 如果选中的单位发生了变化，刷新报表树范围
			if (!rmsMemVO.getPk_org().equals(controler.getSelectedUnitPK())) {
				controler.setSelectedUnitPK(rmsMemVO.getPk_org());
				// add by liuweiu 2015-04-11 按方向键也应该把单位pk设置到隐藏的组织输入框中（下面两处同理）
				m_keyCondPane.getM_jFieldKeywords()[0].setValue(rmsMemVO.getPk_org());
			}
		} else if (treeNode.getUserObject() instanceof ReportOrgInnerVO) {// xulm
																			// 2010-7-2
																			// 增加
																			// 按组织属性构建的树双击处理
			ReportOrgInnerVO reportInnerOrg = (ReportOrgInnerVO) treeNode.getUserObject();
			ReportOrgVO reportOrg = reportInnerOrg.getReportOrgVO();
			// 如果选中的单位发生了变化，刷新报表树范围
			if (!reportOrg.getPk_reportorg().equals(controler.getSelectedUnitPK())) {
				// 如果是自定义项
				if (StringUtil.isEmptyWithTrim(reportOrg.getCode())) {
					String pathName = getPathName(treeNode);
					controler.setSelectedUnitPK(pathName);
					((IUFORepDataControler) controler).setTotalReportOrgPKs(pathName, getTotalReportOrg(treeNode));
					m_keyCondPane.getM_jFieldKeywords()[0].setValue(pathName);
				} else {
					controler.setSelectedUnitPK(reportOrg.getPk_reportorg());
					((IUFORepDataControler) controler).setTotalReportOrgPKs(reportOrg.getPk_reportorg(),
							getTotalReportOrg(treeNode));
					m_keyCondPane.getM_jFieldKeywords()[0].setValue(reportOrg.getPk_reportorg());
				}
			}
		}
		refreshShowMode();
	}

	@Override
	public String[] createPluginList() {
		return super.createPluginList();
	}

	@Override
	public void initTreeModel() {
		if (treePane != null && treePane.getModel().getTreeCreateStrategy() instanceof ReportOrgTreeCreateStrategy) {
			closeFreeTotalTab();
		}
		super.initTreeModel();
		m_Userdefitems = null;
	}

	@Override
	public Object[] loadReportOrgAndRmsName(String strRmsPK, String strOrgPK) throws Exception {
		this.pk_task_old = getTaskPK();
		return NCLocator
				.getInstance()
				.lookup(IUfoeVorpQuerySrv.class)
				.queryRepManaStruMemAndRmsName(strRmsPK, strOrgPK, pk_task_old, getDate(),isTransForm);
	}

	/**
	 * 任务改变时，对单位树过滤的方法
	 */
	@Override
	public void valueChanged(ValueChangedEvent event) {
		if(event.getNewValue()!=null){
			
			getMainboard().getContext().setAttribute(IUfoContextKey.TASK_PK, getTaskPK());
		}
		if (treePane.getModel().getTreeCreateStrategy() instanceof OrgMemberPKTreeCreateStrategy) {
			task = null;
			this.isFirstLoad = true;
			IUfoeVorpQuerySrv srv = NCLocator.getInstance().lookup(IUfoeVorpQuerySrv.class);
			Mainboard mainBoard = getMainboard();
			String strRmsPK = (String) mainBoard.getContext().getAttribute(IUfoContextKey.CUR_REPMNGSTRUCT_PK);
			String mainOrgPK = (String) getMainboard().getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK);
			try {
				// 切换任务后关键字面板会刷新，此时将缓存的date清空，取出当前的业务时间
				this.date = null;
				ReportManaStruMemberWithCodeNameVO[] newMemebers = (ReportManaStruMemberWithCodeNameVO[]) srv
						.queryRepManaStruMemAndRmsName(strRmsPK, mainOrgPK, getTaskPK(),
								getDate(), isTransForm)[1];
				String strOrgPK = getNewEditorUnitPK(newMemebers);
				treePane.getModel().initModel(newMemebers);
				treePane.getTree().expandRow(1);
//				treePane.getTree().setCellRenderer(new OrgDCTreeCellRender(QueryAreaColor.BKGRD_COLOR_DEFAULT));
				treePane.invalidate();
				treePane.repaint();
//				treePane.updateUI();
				// 切换任务后,默认打开主组织的报表数据编辑器
				// 切换任务后,单位树选择当前主组织
				selectUnitTreeNode(strOrgPK);
				TreePath treePath = treePane.getTree().getSelectionPath();
				if (treePath == null) {
					return;
				}
				// 根据选择的单位树节点,打开报表数据编辑器
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
				openRepDataEdit(treeNode);
			} catch (Exception e) {
				AppDebug.debug(e);
			}
		} else if (treePane.getModel().getTreeCreateStrategy() instanceof ReportOrgTreeCreateStrategy) {
			// 按组织属性展示
			this.date = null;
			initTreeModelByOrgAttribute(this.m_Userdefitems);

			Mainboard mainBoard = getMainboard();
			String strOrgPK = (String) mainBoard.getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK);
			selectUnitTreeNode(strOrgPK);
			TreePath treePath = treePane.getTree().getSelectionPath();
			if (treePath == null) {
				return;
			}
			// 根据选择的单位树节点,打开报表数据编辑器
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
			openRepDataEdit(treeNode);
		}

		// 响应自由汇总按钮的可用性 modified by jiaah
		IUFORepDataControler controler = (IUFORepDataControler) AbsRepDataControler.getInstance(getMainboard());
		controler.setSelectedTaskPK(getTaskPK());

		PluginActionEvent actionEvent = new PluginActionEvent(this);
		getMainboard().getEventManager().dispatch(actionEvent);
	}

	/**
	 * 时间为空时返回当前业务日期
	 * 
	 * @creator tianjlc at 2015-1-20 下午1:52:56
	 * @return
	 * @return String
	 */
	protected String getDate() {
		if (date != null) {
			return date;
		}
		return WorkbenchEnvironment.getInstance().getBusiDate().toString();
	}

	/**
	 * 按组织属性初始化树模型
	 * 
	 * @create by xulm at 2010-7-2,上午09:05:13
	 */
	public void initTreeModelByOrgAttribute(UserdefitemVO[] userdefitems) {
		try {
			if (userdefitems != null && userdefitems.length > 0 ) {
				if( this.m_Userdefitems!=userdefitems){
					closeFreeTotalTab();
				}
				ReportOrgTreeCreateStrategy treeCreateStrategy = new ReportOrgTreeCreateStrategy();
				treeCreateStrategy.setRootName(userdefitems[0].getShowname());
				treePane.getModel().setTreeCreateStrategy(treeCreateStrategy);
				String strTaskPk = getTaskPK();
				//editor tianjlc 2015-04-23 组织属性的树刷新后，更新任务pk
				this.pk_task_old=strTaskPk;
				IUFORepDataControler controler = (IUFORepDataControler) AbsRepDataControler.getInstance(getMainboard());
				LoginEnvVO loginEnv = controler.getLoginEnv(getMainboard());
				String mainOrgPK = loginEnv.getLoginUnit();
				String rmsPK = loginEnv.getRmsPK();
				ReportOrgInnerVO[] reportOrgs = TotalOrgAttributeUtils.handleVersionAttribute(userdefitems, strTaskPk,
						mainOrgPK, rmsPK, getDate(), controler.isIncludeBalanUnitForFreeTotal());
				treePane.getModel().initModel(reportOrgs);
				this.m_Userdefitems = userdefitems;
				treePane.getTree().setCellRenderer(new UnitAttributeTreeCellRender());
				ToolTipManager.sharedInstance().registerComponent(treePane.getTree());
				((IUFORepDataControler) controler).setTotalReportOrgPKs(getPathName(treePane.getModel()
						.getSelectedNode()), getTotalReportOrg(treePane.getModel().getSelectedNode()));
			}
		} catch (Exception e) {
			AppDebug.debug(e);
		}
	}

	/**
	 * 关闭自由汇总页签
	 * 
	 * @creator tianjlc at 2015-4-17 下午2:09:58
	 * @return void
	 */
	private void closeFreeTotalTab() {
		Viewer[] allViews = getMainboard().getAllViews();
		List<String> editorIds = new ArrayList<String>();
		for (Viewer v : allViews) {
			if (v instanceof AbsBaseRepDataEditor && ((AbsBaseRepDataEditor) v).getPubData() != null) {
				String unitPk = ((AbsBaseRepDataEditor) v).getPubData().getUnitPK();
				// 判断当前单元pk是不是标准的20位pk
				if (!isPkRule(unitPk)) {
					editorIds.add(v.getId());
				}
			}
		}
		closeTab(editorIds.toArray(new String[editorIds.size()]));
	}

	private boolean isPkRule(String unitPk) {
		Pattern p = Pattern.compile("[0-9A-Z]{20}");
		Matcher m = p.matcher(unitPk);
		if (m.matches()) {
			return true;
		}
		return false;
	}

	/**
	 * 批量关闭页签
	 * 
	 * @creator tianjlc at 2015-4-17 下午2:00:13
	 * @param aloneIDs
	 * @return void
	 */
	private void closeTab(String[] editorIds) {
		DockingManager dmng = getMainboard().getDockingManager();
		for (String editorId : editorIds) {
			Dockable dockable = dmng.getDockable(editorId);
			dmng.close(dockable);
		}
	}

	/**
	 * 根据任务，rms，org获取当前的orgpks
	 * 
	 * @create by jiaah at 2011-2-28,上午11:37:51
	 * @return
	 * @throws UFOSrvException
	 */
	// private String[] getOrgPKS(UserdefitemVO[] userdefitems, String
	// strTaskId)
	// throws UFOSrvException {
	//
	// // 获得所选择的组织属性的类型
	// List<Integer> typelst = new ArrayList<Integer>();
	// for (int j = 0; j < userdefitems.length; j++) {
	// if (userdefitems[j].getDigits() != null
	// && !typelst.contains(userdefitems[j].getDigits())) {
	// typelst.add(userdefitems[j].getDigits());
	// }
	// }
	// Integer[] iTypes = typelst.toArray(new Integer[0]);
	// List<String> strType = new ArrayList<String>();
	// for (Integer iType : iTypes) {
	// if (iType != 0) {
	// strType.add("orgtype" + iType);
	// }
	// }
	//
	// // 通过任务过滤组织pk
	// String[] orgPKs = null;
	//
	// if (strTaskId != null) {
	// IUFORepDataControler controler = (IUFORepDataControler)
	// AbsRepDataControler
	// .getInstance(getMainboard());
	// LoginEnvVO loginEnv = controler.getLoginEnv(getMainboard());
	// String[] strAssignOrgPKs = TaskSrvUtils.getTaskAssignQueryService()
	// .getTaskAssignToOrgPKs(strTaskId, loginEnv.getLoginUnit(),
	// loginEnv.getRmsPK());
	// Set<String> hashAssignOrgPK = new HashSet<String>(
	// Arrays.asList(strAssignOrgPKs));
	// orgPKs = hashAssignOrgPK.toArray(new String[0]);
	// }
	// StringBuffer orgs = getConnectString(orgPKs);
	// StringBuffer sql = new StringBuffer();
	// for (int j = 0; j < strType.size(); j++) {
	// sql.append(strType.get(j).toString() + "='Y' and ");
	// }
	// sql.append("pk_org in (" + orgs.toString() + ")");
	// OrgVO[] org = OrgUtil.getOrgVOBySQL(sql.toString());
	//
	// if (org != null) {
	// String[] orgids = new String[org.length];
	// for (int j = 0; j < org.length; j++) {
	// orgids[j] = org[j].getPk_org();
	// }
	// return orgids;
	// }
	// return null;
	// }

	/**
	 * String[]拼接成'a','b','c'的形式,放置在in();
	 * 
	 * @create by jiaah at 2010-11-11,下午07:20:32
	 * 
	 * @return
	 */
	// private StringBuffer getConnectString(String[] arrayString) {
	// StringBuffer strBuffer = new StringBuffer("");
	// if (arrayString != null && arrayString.length > 0) {
	// for (int i = 0; i < arrayString.length; i++) {
	// strBuffer.append("'");
	// strBuffer.append(arrayString[i]);
	// strBuffer.append("'");
	// if (i < arrayString.length - 1) {
	// strBuffer.append(",");
	// }
	// }
	// }
	// return strBuffer;
	// }

	@Override
	public void reInitKeyCondPane(String strRepPK) {
		super.reInitKeyCondPane(strRepPK);
		AbsRepDataControler controler = AbsRepDataControler.getInstance(getMainboard());
		if (controler != null) {
			IUFOCombRepDataEditor repEditor = (IUFOCombRepDataEditor) getMainboard().getView(controler.getEditorID());
			if (repEditor != null && repEditor.getActiveRepDataEditor() instanceof IUFORepDataEditor
					&& !(repEditor instanceof com.ufsoft.iufo.inputlpugin.ufobiz.ufoe.data.AnalyseRepEditor)) {
				m_keyCondPane = repEditor.getKeyCondPane();
				if (m_keyCondPane != null && m_keyCondPane instanceof IUFOKeyCondPanel) {
					((IUFOKeyCondPanel) m_keyCondPane).getTimeValueChanageListener().setUnitTreeViewer(this);
					((IUFOKeyCondPanel) m_keyCondPane).getTimeValueChanageListener().valueChanged(
							new ValueChangedEvent(new Object()));// 初始化关键字面板的
																	// 时候也要对单位关键字参照设置相应的参数
				}
			}
		}
	}

	@Override
	public void setSelectedTask(String strTaskPK) {
		if (getTaskPK() != null && !getTaskPK().equals(strTaskPK)) {
			taskPane.getTask_refPane().setPK(strTaskPK);
			
			//modifior tianjlc 2015-04-23 适配报表组织体系多版本，在切换页签时报表体系数刷新由时间关键字控制
			// wangqi 20130701 使不同任务的editor切换时，任务对应的组织树相应更新
//			taskPane.getTask_refPane().setValueObjFireValueChangeEvent(strTaskPK);
//			IUfoeVorpQuerySrv srv = NCLocator.getInstance().lookup(IUfoeVorpQuerySrv.class);
//			Mainboard mainBoard = getMainboard();
//			String strRmsPK = (String) mainBoard.getContext().getAttribute(IUfoContextKey.CUR_REPMNGSTRUCT_PK);
//			String strOrgPK = (String) mainBoard.getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK);
//			try {
//				ReportManaStruMemberWithCodeNameVO[] newMemebers = (ReportManaStruMemberWithCodeNameVO[]) srv
//						.queryRepManaStruMemAndRmsName(strRmsPK, strOrgPK, getTaskPK(),
//								getDate(), isTransForm)[1];
//				treePane.getModel().initModel(newMemebers);
//				treePane.getTree().expandRow(1);
//				treePane.getTree().setCellRenderer(new OrgDCTreeCellRender(QueryAreaColor.BKGRD_COLOR_DEFAULT));
//				treePane.invalidate();
//				treePane.repaint();
//			} catch (Exception e) {
//				AppDebug.debug(e);
//			}
//			AbsCombRepDataEditor combEditor = (AbsCombRepDataEditor) mainBoard.getCurrentView();
//			if (combEditor != null && combEditor.getPubData() != null && combEditor.getPubData().getUnitPK() != null) {
//				selectUnitTreeNode(combEditor.getPubData().getUnitPK());
//			}

		}
		//
		// 查询或者双击组织树节点已打开视图之间切换
		task = null;
	}

	@Override
	protected ReportManaStruMemberWithCodeNameVO[] postAdjustTreeModel(ReportManaStruMemberWithCodeNameVO[] membervos) {
		return OrgUIUtil.trimRootNodes((DefaultTreeModel) treePane.getTree().getModel(), membervos);
	}

	/**
	 * @i18n miufohbbb00130=请选择报表，选择报表后才能打开报表数据界面
	 * @i18n miufohbbb00125=打开报表失败
	 */
	@Override
	protected void onDblClickTreeNode(JTree tree, MouseEvent event) {
		if (event != null) {
			int mods = event.getModifiers();
			if ((mods & InputEvent.BUTTON3_MASK) != 0) {
				// 右键不响应
				return;
			}
		}
		
		if (!hasOrgData()) {
			return;
		}

		DefaultMutableTreeNode treeNode = null;
		TreePath treePath = null;
		if (event != null) {
			treePath = tree.getPathForLocation(event.getX(), event.getY());
			if (treePath == null)
				return;
		} else {
			treePath = tree.getSelectionPath();
			if (treePath == null) 
				return;
		}

		treeNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
		if (treeNode == null)
			return;
		
		Object nodeObj = treeNode.getUserObject();
		if (nodeObj instanceof ReportManaStruMemberWithCodeNameVO) {
			ReportManaStruMemberWithCodeNameVO member = (ReportManaStruMemberWithCodeNameVO) treeNode.getUserObject();
			if (member.getTs() == null) {
				return;
			}
		} else if (nodeObj instanceof ReportOrgInnerVO) {
			
		} else
			return;
		if(!validateKeyVal(getM_keyCondPane().getInputKeyValues())){
			JOptionPane.showMessageDialog(getMainboard(), NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0", "01413007-0726")/*@ res "查询关键字不得为空"*/);
			return;
		}
		// 根据选择单位树节点,打开报表数据编辑器
		openRepDataEdit(treeNode);
	}

	private boolean validateKeyVal(String[] inputKeyValues) {
		for (String keyVal : inputKeyValues) {
			if(UFOString.isEmpty(keyVal)){
				return false;
			}
		}
		return true;
	}

	private void openRepDataEdit(DefaultMutableTreeNode treeNode) {
		Mainboard mainBoard = getMainboard();
		IUFORepDataControler controler = (IUFORepDataControler) AbsRepDataControler.getInstance(mainBoard);
		String strUnitPK = controler.getSelectedUnitPK();
		if (strUnitPK == null)
			return;

		String strTaskPK = getTaskPK();
		if (strTaskPK == null) {
			MessageDialog.showHintDlg(mainBoard, null,
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0354")/*@ res "请选择任务！"*/);
			return;
		}

		try {
			if (TaskSrvUtils.isTaskEnable(strTaskPK) == false) {
				JOptionPane.showMessageDialog(mainBoard,
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0424")/* @ res "当前任务已被停用" */);
				return;
			}
		} catch (Exception e) {
			AppDebug.debug(e);
			JOptionPane.showMessageDialog(mainBoard,
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0424")/* @ res "当前任务已被停用"*/);
			return;
		}

		// 如果按组织属性显示的单位树中选中了自定义项
		// modified by jiaah
		Object selectData = treeNode.getUserObject();
		if (selectData instanceof ReportOrgInnerVO
				&& StringUtil.isEmptyWithTrim(((ReportOrgInnerVO) selectData).getReportOrgVO().getCode())) {
			try {
				controler.setBFreeTotal(true);
				// 暂时这样传递，注意切换面板的问题
				// controler.setTotalReportOrgPKs(strUnitPK,
				// getTotalReportOrg(((HierachicalDataAppModel)treePane.getModel()).getSelectedNode()));
				// modified by jiaah
				// controler.setTotalReportOrgPKs(strUnitPK,
				// getTotalReportOrg(treeNode));
				controler.doOpenRepEditWin(mainBoard, true);
				// controler.setSelectedUnitPK(getPathName(((HierachicalDataAppModel)treePane.getModel()).getSelectedNode()));
				// modified by jiaah
				controler.setSelectedUnitPK(getPathName(treeNode));
				controler.setSelectedTaskPK(strTaskPK);
			} catch (Exception te) {
				AppDebug.debug(te);
				JOptionPane.showMessageDialog(mainBoard,
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0411")/* @ res "打开报表失败"*/);
			}

		} else {
			try {
				controler.setBFreeTotal(false);
				if (!TaskSrvUtils.isReceiveTask(strUnitPK, strTaskPK)) {
					JOptionPane.showMessageDialog(mainBoard,
							nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0423")/* @ res "任务未分配给查询组织,无法查询" */);
					return;
				}

				// wangqi 2011-3-15 层层上报下查询权限设置 START
				task = getCurrentTask();

				// 层层上报并且选中的不是当前登录组织
				if (!strUnitPK.equals(mainBoard.getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK))
						&& task.getCommitmode().intValue() == ICommitConfigConstant.COMMIT_MODE_BYLEVEL) {

					// 取出登录组织下所有组织
//					ReportManaStruMemberWithCodeNameVO[] members = RMSUtil.getRMSMemberVos((String) mainBoard
//							.getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK), (String) mainBoard.getContext()
//							.getAttribute(IUfoContextKey.CUR_REPMNGSTRUCT_PK));
					
					Object[] objs = loadReportOrgAndRmsName(
							(String) mainBoard.getContext().getAttribute(IUfoContextKey.CUR_REPMNGSTRUCT_PK), 
							(String) mainBoard.getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK));

					ReportManaStruMemberWithCodeNameVO[] members = (ReportManaStruMemberWithCodeNameVO[]) objs[1];
					// 取出当前选中组织的内部编码
					String innercode = "";
					String orgPKinnercode = "";
					for (ReportManaStruMemberWithCodeNameVO vo : members) {
						if (strUnitPK.equals(vo.getPk_org())) {
							innercode = vo.getInnercode();
						}
						if (((String) mainBoard.getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK)).equals(vo
								.getPk_org())) {
							orgPKinnercode = vo.getInnercode();
						}
						if (!"".equals(innercode) && !"".equals(orgPKinnercode)) {
							break;
						}
					}
					// 取出当前选中组织所在支的第二级节点
					String org = "";
					for (ReportManaStruMemberWithCodeNameVO vo : members) {
						if (vo.getInnercode().length() == (InnerCodeUtil.INNERCODELENGTH + orgPKinnercode.length())
								&& innercode.contains(vo.getInnercode())) {
							org = vo.getPk_org();
							break;
						}
					}

					// String aloneid = controler.getAloneId(mainBoard, org);

					AbsKeyCondPanel condpane = getM_keyCondPane();
					String rep_pk = condpane.getRepPK();
					// edit by tanyj 焦点处于管理报告，任务批注页签时，condpane.getRepPK()为null
					if (rep_pk == null || rep_pk.trim().equals("")) {
						TaskInfoVO taskInfo = controler.getSelectedTaskInfo();
						String[][] reps = taskInfo.getReportIds();
						rep_pk = reps[0][0];
					}

					String[] inputKeyVals = condpane.getInputKeyValues();
					inputKeyVals[0] = org;

					MeasurePubDataVO pubData = (MeasurePubDataVO) ActionHandler.execWithZip(
							controler.getRepDataActionHandlerClassName(), "getNewMeasurePubDataVO", new Object[] {
									mainBoard.getContext().getAttribute(IUfoContextKey.CUR_USER_ID), inputKeyVals,
									controler.getDataVer(), rep_pk, org, condpane.getAccschemepk() });
					String aloneid = pubData.getAloneID();
					// if (!"".equals(aloneid)) {
					ICommitQueryService commitSrv = NCLocator.getInstance().lookup(ICommitQueryService.class);
					TaskCommitVO[] commits = commitSrv.getTaskCommitsByAloneIDs(task.getPk_task(),
							new String[] { aloneid });
					if (!(commits != null && commits.length > 0 && commits[0].getCommit_state().intValue() >= CommitStateEnum.STATE_COMMITED
							.getIntValue())) {
						JOptionPane
								.showMessageDialog(
										mainBoard,
										nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0",
												"01820001-0283")/*
																 * @ res
																 * "层层上报模式不能查询下级组织未上报的报表"
																 */);
						return;
					}
					// }
				}
				// wangqi 2011-3-15 层层上报下查询权限设置 END
			} catch (Exception e) {
				UfoPublic.sendErrorMessage(e.getMessage(), mainBoard, e);
				return;
			}

			try {
				// 打开报表数据面板
				controler.setSelectedTaskPK(strTaskPK);
				controler.doOpenRepEditWin(mainBoard, false);

				// 在打开报表数据面板过程中，可能改变了单位树中选中的节点，此处确保单位树中选中的节点为原节点
				controler.setSelectedUnitPK(strUnitPK);
				setSelectedTask(strTaskPK);
			} catch (Exception te) {
				AppDebug.debug(te);
				JOptionPane.showMessageDialog(mainBoard,
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0411")/*
																											 * @
																											 * res
																											 * "打开报表失败"
																											 */);
			}
		}
		return;
	}

	private TaskVO getCurrentTask() {
		if (task != null)
			return task;

		task = IUFOCacheManager.getSingleton().getTaskCache().getTaskVO(getTaskPK());
		return task;
	}

	/**
	 * 得到当前选中的自定义项的所有孩子组织，即自定义项汇总组织
	 * 
	 * @create by xulm at 2010-7-2,下午05:01:15
	 * 
	 * @param selectNode
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private ArrayList<String> getTotalReportOrg(DefaultMutableTreeNode selectNode) {
		ArrayList<String> lstReportOrgPK = new ArrayList<String>();
		Enumeration e = selectNode.preorderEnumeration();
		e.nextElement();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			if (node.getUserObject() instanceof ReportOrgInnerVO
					&& !StringUtil
							.isEmptyWithTrim(((ReportOrgInnerVO) node.getUserObject()).getReportOrgVO().getCode())) {
				lstReportOrgPK.add(((ReportOrgInnerVO) node.getUserObject()).getReportOrgVO().getPk_reportorg());
			}
		}
		return lstReportOrgPK;
	}

	public ArrayList<String> getTotalReportOrg(String reportOrgPK) {
		ReportOrgVO reportOrg = new ReportOrgVO();
		reportOrg.setPk_reportorg(reportOrgPK);
		DefaultMutableTreeNode selectNode = treePane.getModel().findNodeByBusinessObject(reportOrg);
		return getTotalReportOrg(selectNode);
	}

	@Override
	public void onViewActive(Viewer oldView, Viewer newView, boolean editorRefresh) {
		// TODO Auto-generated method stub
		super.onViewActive(oldView, newView, editorRefresh);
		if (treePane.getModel().getAllDatas().length == 0) {
			setUnitKeyValue(null);
		}
		refreshShowMode();
	}

	/**
	 * 刷新关键字面板
	 * 
	 * @creator tianjlc at 2015-3-27 下午2:53:40
	 * @return void
	 */
	private void refreshShowMode() {
		if (getM_keyCondPane() != null) {
			TreePath path = treePane.getTree().getSelectionPath();
			if(path==null){
				return;
			}
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (treeNode.getUserObject() instanceof ReportManaStruMemberWithCodeNameVO) {
//				ReportManaStruMemberWithCodeNameVO rmsMemVO = (ReportManaStruMemberWithCodeNameVO) treeNode
//						.getUserObject();
				// 如果选中的单位发生了变化，刷新报表树范围
				getM_keyCondPane().initShowMode(false);
			} else if (treeNode.getUserObject() instanceof ReportOrgInnerVO) {// 按组织属性构建的树双击处理
				ReportOrgInnerVO reportInnerOrg = (ReportOrgInnerVO) treeNode.getUserObject();
				ReportOrgVO reportOrg = reportInnerOrg.getReportOrgVO();
				// 如果选中的单位发生了变化，刷新报表树范围
				// 如果是自定义项
				if (StringUtil.isEmptyWithTrim(reportOrg.getCode())) {
					getM_keyCondPane().initShowMode(true);
				} else {
					getM_keyCondPane().initShowMode(false);
				}
			}
		}
	}

	/**
	 * 默认在树初始加载时不刷新树
	 * 
	 * @creator tianjlc at 2015-3-2 下午4:35:58
	 * @param time
	 * @return void
	 */
	public void initTreeModelDate(String time) {
		if (isFirstLoad) {
			isFirstLoad = false;
			return;
		}
		if (treePane == null) {
			return;
		}
		if (time == null || time.trim().length() == 0) {
			return;
		}
		String pk_task = getTaskPK();
		if(pk_task == null || pk_task.trim().length() == 0){
			return;
		}
		//任务没变，时间没变，且不为空，不需要刷新左树
		if(pk_task_old != null && pk_task_old.equals(pk_task) && date != null && date.equals(time)){
			return;
		}
		this.date = time;
		refresh();
	}

	public void setTransForm(boolean isTransForm) {
		this.isTransForm = isTransForm;
	}
	
	/**
	 * 切换任务后，单位关键字为当前选择的单位关键字，如果已经查询到的组织不包含当前选择的组织，返回主组织
	 * 
	 * @return
	 */
	private String getNewEditorUnitPK(ReportManaStruMemberWithCodeNameVO[] memebers) {
		String selectUnitPK = ((IUFORepDataControler) IUFORepDataControler.getInstance(getMainboard()))
				.getSelectedUnitPK();
		String mainOrg = (String) getMainboard().getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK);
		if (memebers != null) {
			for (ReportManaStruMemberWithCodeNameVO member : memebers) {
				if (StringUtils.equals(member.getPk_org(), selectUnitPK)) {
					return StringUtils.isEmpty(selectUnitPK) ? mainOrg : selectUnitPK;
				}
			}
		}
		return mainOrg;
	}

}
