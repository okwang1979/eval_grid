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
 * ������λ�����
 * 
 * @author weixl
 * 
 */
@SuppressWarnings("restriction")
public class IUFONavUnitTreeViewer extends AbsNavUnitTreeViewer implements ValueChangedListener {
	private static final long serialVersionUID = 8232011838671032093L;

	transient public static String NAV_UNIT_TREE_ID = "iufo.input.dir_unit.view";

	private RecieveTaskRefPanel taskPane = null;

	// ��֯�Զ�������
	private UserdefitemVO[] m_Userdefitems = null;

	private TaskVO task = null;

	// editor tianjlc 2015-02-04 ���䱨����֯��ϵ��汾��ӣ��洢ʱ��
	private String date = null;
	// �洢�ɵ�����PK
	protected String pk_task_old = null;
	// �Ƿ��һ�μ��أ�����Զ�̵���
	private boolean isFirstLoad = true;
	// �����Ƿ���Ҫת��
	private boolean isTransForm = true;

	// private UIPanel northmainpane = null;

	@Override
	public void startup() {
		super.startup();

		addTitleAction(new OrgAttributeShowAction(getMainboard()), 3);
		removeTitleAction(DefaultPinAction.class.getName());

		// wangqi 20130312 �޸��������ͼչ���۵��Ĺ���
		removeTitleAction(String.valueOf((new DefaultMaximizeAction()).getValue(Action.NAME)));
		removeTitleAction(String.valueOf((new DefaultCloseAction()).getValue(Action.NAME)));
		AbsReportMiniAction action = new AbsReportMiniAction(getMainboard());
		action.setMiniid("nc.ui.iufo.input.ufoe.view.IUFONaviUnitTreeMiniViewer");
		addTitleAction(action);
		// wangqi 20130312 �޸��������ͼչ���۵��Ĺ��� E
	}

	/**
	 * ��Ȩ�޷ſ������ⲿ��Ҫ����ѡ�е���֯
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
		// editor tianjlc at 2015-03-02 ���䱨����֯��ϵ��汾��ӣ�ʱ��ؼ���ֵ�ı�ʱ��ˢ����
		if (treePane.getModel().getTreeCreateStrategy() instanceof OrgMemberPKTreeCreateStrategy) {
			initTreeModel();
		} else {
			initTreeModelByOrgAttribute(this.m_Userdefitems);
		}
		IUFORepDataControler controler = (IUFORepDataControler) AbsRepDataControler.getInstance(getMainboard());
		selectUnitTreeNode(getNewSelecUnitTreeNodePK(controler.getSelectedUnitPK()));
	}

	/**
	 * �жϵ�ǰѡ�е���֯PK�Ƿ��ڵ�ǰ�汾�ı�����֯��ϵ�汾�� ����֯��ϵΪ�գ�����null ��������֯��ϵ�У����ص�ǰ�ĸ��ڵ�pk
	 * ����֯��ϵ�У�����selectUnitPK
	 * 
	 * @creator tianjlc at 2015-3-6 ����9:17:37
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
	 * ����ѡ�еĵ�λ�ؼ��ֵ�ֵ
	 * 
	 * @creator tianjlc at 2015-3-6 ����10:44:51
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
						if("����".equals(((UIRefPane)((RefInputEditor)iInputEditor).getComponent()).getRefName())){
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
			// @edit by wuyongc at 2013-12-20,����2:32:59
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
		// edit by congdy 2013.6.28 �п����Ǵ����񷢲��ڵ����ģ���ʱ��pane����ʵ��
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
			// ���ѡ�еĵ�λ�����˱仯��ˢ�±�������Χ
			if (!rmsMemVO.getPk_org().equals(controler.getSelectedUnitPK())) {
				controler.setSelectedUnitPK(rmsMemVO.getPk_org());
				// add by liuweiu 2015-04-11 �������ҲӦ�ðѵ�λpk���õ����ص���֯������У���������ͬ��
				m_keyCondPane.getM_jFieldKeywords()[0].setValue(rmsMemVO.getPk_org());
			}
		} else if (treeNode.getUserObject() instanceof ReportOrgInnerVO) {// xulm
																			// 2010-7-2
																			// ����
																			// ����֯���Թ�������˫������
			ReportOrgInnerVO reportInnerOrg = (ReportOrgInnerVO) treeNode.getUserObject();
			ReportOrgVO reportOrg = reportInnerOrg.getReportOrgVO();
			// ���ѡ�еĵ�λ�����˱仯��ˢ�±�������Χ
			if (!reportOrg.getPk_reportorg().equals(controler.getSelectedUnitPK())) {
				// ������Զ�����
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
	 * ����ı�ʱ���Ե�λ�����˵ķ���
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
				// �л������ؼ�������ˢ�£���ʱ�������date��գ�ȡ����ǰ��ҵ��ʱ��
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
				// �л������,Ĭ�ϴ�����֯�ı������ݱ༭��
				// �л������,��λ��ѡ��ǰ����֯
				selectUnitTreeNode(strOrgPK);
				TreePath treePath = treePane.getTree().getSelectionPath();
				if (treePath == null) {
					return;
				}
				// ����ѡ��ĵ�λ���ڵ�,�򿪱������ݱ༭��
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
				openRepDataEdit(treeNode);
			} catch (Exception e) {
				AppDebug.debug(e);
			}
		} else if (treePane.getModel().getTreeCreateStrategy() instanceof ReportOrgTreeCreateStrategy) {
			// ����֯����չʾ
			this.date = null;
			initTreeModelByOrgAttribute(this.m_Userdefitems);

			Mainboard mainBoard = getMainboard();
			String strOrgPK = (String) mainBoard.getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK);
			selectUnitTreeNode(strOrgPK);
			TreePath treePath = treePane.getTree().getSelectionPath();
			if (treePath == null) {
				return;
			}
			// ����ѡ��ĵ�λ���ڵ�,�򿪱������ݱ༭��
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
			openRepDataEdit(treeNode);
		}

		// ��Ӧ���ɻ��ܰ�ť�Ŀ����� modified by jiaah
		IUFORepDataControler controler = (IUFORepDataControler) AbsRepDataControler.getInstance(getMainboard());
		controler.setSelectedTaskPK(getTaskPK());

		PluginActionEvent actionEvent = new PluginActionEvent(this);
		getMainboard().getEventManager().dispatch(actionEvent);
	}

	/**
	 * ʱ��Ϊ��ʱ���ص�ǰҵ������
	 * 
	 * @creator tianjlc at 2015-1-20 ����1:52:56
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
	 * ����֯���Գ�ʼ����ģ��
	 * 
	 * @create by xulm at 2010-7-2,����09:05:13
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
				//editor tianjlc 2015-04-23 ��֯���Ե���ˢ�º󣬸�������pk
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
	 * �ر����ɻ���ҳǩ
	 * 
	 * @creator tianjlc at 2015-4-17 ����2:09:58
	 * @return void
	 */
	private void closeFreeTotalTab() {
		Viewer[] allViews = getMainboard().getAllViews();
		List<String> editorIds = new ArrayList<String>();
		for (Viewer v : allViews) {
			if (v instanceof AbsBaseRepDataEditor && ((AbsBaseRepDataEditor) v).getPubData() != null) {
				String unitPk = ((AbsBaseRepDataEditor) v).getPubData().getUnitPK();
				// �жϵ�ǰ��Ԫpk�ǲ��Ǳ�׼��20λpk
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
	 * �����ر�ҳǩ
	 * 
	 * @creator tianjlc at 2015-4-17 ����2:00:13
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
	 * ��������rms��org��ȡ��ǰ��orgpks
	 * 
	 * @create by jiaah at 2011-2-28,����11:37:51
	 * @return
	 * @throws UFOSrvException
	 */
	// private String[] getOrgPKS(UserdefitemVO[] userdefitems, String
	// strTaskId)
	// throws UFOSrvException {
	//
	// // �����ѡ�����֯���Ե�����
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
	// // ͨ�����������֯pk
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
	 * String[]ƴ�ӳ�'a','b','c'����ʽ,������in();
	 * 
	 * @create by jiaah at 2010-11-11,����07:20:32
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
							new ValueChangedEvent(new Object()));// ��ʼ���ؼ�������
																	// ʱ��ҲҪ�Ե�λ�ؼ��ֲ���������Ӧ�Ĳ���
				}
			}
		}
	}

	@Override
	public void setSelectedTask(String strTaskPK) {
		if (getTaskPK() != null && !getTaskPK().equals(strTaskPK)) {
			taskPane.getTask_refPane().setPK(strTaskPK);
			
			//modifior tianjlc 2015-04-23 ���䱨����֯��ϵ��汾�����л�ҳǩʱ������ϵ��ˢ����ʱ��ؼ��ֿ���
			// wangqi 20130701 ʹ��ͬ�����editor�л�ʱ�������Ӧ����֯����Ӧ����
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
		// ��ѯ����˫����֯���ڵ��Ѵ���ͼ֮���л�
		task = null;
	}

	@Override
	protected ReportManaStruMemberWithCodeNameVO[] postAdjustTreeModel(ReportManaStruMemberWithCodeNameVO[] membervos) {
		return OrgUIUtil.trimRootNodes((DefaultTreeModel) treePane.getTree().getModel(), membervos);
	}

	/**
	 * @i18n miufohbbb00130=��ѡ�񱨱�ѡ�񱨱����ܴ򿪱������ݽ���
	 * @i18n miufohbbb00125=�򿪱���ʧ��
	 */
	@Override
	protected void onDblClickTreeNode(JTree tree, MouseEvent event) {
		if (event != null) {
			int mods = event.getModifiers();
			if ((mods & InputEvent.BUTTON3_MASK) != 0) {
				// �Ҽ�����Ӧ
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
			JOptionPane.showMessageDialog(getMainboard(), NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0", "01413007-0726")/*@ res "��ѯ�ؼ��ֲ���Ϊ��"*/);
			return;
		}
		// ����ѡ��λ���ڵ�,�򿪱������ݱ༭��
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
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0354")/*@ res "��ѡ������"*/);
			return;
		}

		try {
			if (TaskSrvUtils.isTaskEnable(strTaskPK) == false) {
				JOptionPane.showMessageDialog(mainBoard,
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0424")/* @ res "��ǰ�����ѱ�ͣ��" */);
				return;
			}
		} catch (Exception e) {
			AppDebug.debug(e);
			JOptionPane.showMessageDialog(mainBoard,
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0424")/* @ res "��ǰ�����ѱ�ͣ��"*/);
			return;
		}

		// �������֯������ʾ�ĵ�λ����ѡ�����Զ�����
		// modified by jiaah
		Object selectData = treeNode.getUserObject();
		if (selectData instanceof ReportOrgInnerVO
				&& StringUtil.isEmptyWithTrim(((ReportOrgInnerVO) selectData).getReportOrgVO().getCode())) {
			try {
				controler.setBFreeTotal(true);
				// ��ʱ�������ݣ�ע���л���������
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
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0411")/* @ res "�򿪱���ʧ��"*/);
			}

		} else {
			try {
				controler.setBFreeTotal(false);
				if (!TaskSrvUtils.isReceiveTask(strUnitPK, strTaskPK)) {
					JOptionPane.showMessageDialog(mainBoard,
							nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0423")/* @ res "����δ�������ѯ��֯,�޷���ѯ" */);
					return;
				}

				// wangqi 2011-3-15 ����ϱ��²�ѯȨ������ START
				task = getCurrentTask();

				// ����ϱ�����ѡ�еĲ��ǵ�ǰ��¼��֯
				if (!strUnitPK.equals(mainBoard.getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK))
						&& task.getCommitmode().intValue() == ICommitConfigConstant.COMMIT_MODE_BYLEVEL) {

					// ȡ����¼��֯��������֯
//					ReportManaStruMemberWithCodeNameVO[] members = RMSUtil.getRMSMemberVos((String) mainBoard
//							.getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK), (String) mainBoard.getContext()
//							.getAttribute(IUfoContextKey.CUR_REPMNGSTRUCT_PK));
					
					Object[] objs = loadReportOrgAndRmsName(
							(String) mainBoard.getContext().getAttribute(IUfoContextKey.CUR_REPMNGSTRUCT_PK), 
							(String) mainBoard.getContext().getAttribute(IUfoContextKey.CUR_REPORG_PK));

					ReportManaStruMemberWithCodeNameVO[] members = (ReportManaStruMemberWithCodeNameVO[]) objs[1];
					// ȡ����ǰѡ����֯���ڲ�����
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
					// ȡ����ǰѡ����֯����֧�ĵڶ����ڵ�
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
					// edit by tanyj ���㴦�ڹ����棬������עҳǩʱ��condpane.getRepPK()Ϊnull
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
																 * "����ϱ�ģʽ���ܲ�ѯ�¼���֯δ�ϱ��ı���"
																 */);
						return;
					}
					// }
				}
				// wangqi 2011-3-15 ����ϱ��²�ѯȨ������ END
			} catch (Exception e) {
				UfoPublic.sendErrorMessage(e.getMessage(), mainBoard, e);
				return;
			}

			try {
				// �򿪱����������
				controler.setSelectedTaskPK(strTaskPK);
				controler.doOpenRepEditWin(mainBoard, false);

				// �ڴ򿪱��������������У����ܸı��˵�λ����ѡ�еĽڵ㣬�˴�ȷ����λ����ѡ�еĽڵ�Ϊԭ�ڵ�
				controler.setSelectedUnitPK(strUnitPK);
				setSelectedTask(strTaskPK);
			} catch (Exception te) {
				AppDebug.debug(te);
				JOptionPane.showMessageDialog(mainBoard,
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0411")/*
																											 * @
																											 * res
																											 * "�򿪱���ʧ��"
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
	 * �õ���ǰѡ�е��Զ���������к�����֯�����Զ����������֯
	 * 
	 * @create by xulm at 2010-7-2,����05:01:15
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
	 * ˢ�¹ؼ������
	 * 
	 * @creator tianjlc at 2015-3-27 ����2:53:40
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
				// ���ѡ�еĵ�λ�����˱仯��ˢ�±�������Χ
				getM_keyCondPane().initShowMode(false);
			} else if (treeNode.getUserObject() instanceof ReportOrgInnerVO) {// ����֯���Թ�������˫������
				ReportOrgInnerVO reportInnerOrg = (ReportOrgInnerVO) treeNode.getUserObject();
				ReportOrgVO reportOrg = reportInnerOrg.getReportOrgVO();
				// ���ѡ�еĵ�λ�����˱仯��ˢ�±�������Χ
				// ������Զ�����
				if (StringUtil.isEmptyWithTrim(reportOrg.getCode())) {
					getM_keyCondPane().initShowMode(true);
				} else {
					getM_keyCondPane().initShowMode(false);
				}
			}
		}
	}

	/**
	 * Ĭ��������ʼ����ʱ��ˢ����
	 * 
	 * @creator tianjlc at 2015-3-2 ����4:35:58
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
		//����û�䣬ʱ��û�䣬�Ҳ�Ϊ�գ�����Ҫˢ������
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
	 * �л�����󣬵�λ�ؼ���Ϊ��ǰѡ��ĵ�λ�ؼ��֣�����Ѿ���ѯ������֯��������ǰѡ�����֯����������֯
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
