package nc.ui.iufo.query.common.area;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import nc.desktop.ui.WorkbenchEnvironment;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.iufo.checkexecute.filteritem.CheckExeContentFilterItem;
import nc.ui.iufo.input.ufoe.view.ReportOrgTreeCreateStrategy;
import nc.ui.iufo.input.ufoe.view.UnitAttributeTreeCellRender;
import nc.ui.iufo.pub.UfoPublic;
import nc.ui.iufo.query.common.IUfoQueryConditionDLG;
import nc.ui.iufo.query.common.IUfoQueryExecutor;
import nc.ui.iufo.query.common.comp.IUfoOrgSelectedStrategyPanel;
import nc.ui.iufo.query.common.comp.OrgRepFilterPanel;
import nc.ui.iufo.query.common.event.IUfoQueryCondChangeListener;
import nc.ui.iufo.query.common.event.IUfoQueryHolder;
import nc.ui.iufo.query.common.filteritem.AccDateFilterItem;
import nc.ui.iufo.query.common.filteritem.IUfoFilterItem;
import nc.ui.iufo.query.common.filteritem.InputStateFilterItem;
import nc.ui.iufo.query.common.filteritem.NatDateFilterItem;
import nc.ui.iufo.query.common.filteritem.RepFilterItem;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.UILabel;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.beans.UIScrollPane;
import nc.ui.pub.beans.UISplitPane;
import nc.ui.pub.beans.UITextField;
import nc.ui.pub.beans.ValueChangedEvent;
import nc.ui.pub.beans.ValueChangedListener;
import nc.ui.pub.beans.toolbar.ToolBarButton;
import nc.ui.queryarea.UIStateChangedEvent;
import nc.ui.queryarea.UIStateChangedListener;
import nc.ui.queryarea.UIStatefulPanel;
import nc.ui.queryarea.action.QSEditAction;
import nc.ui.queryarea.component.ContainerMouseEnteredDetector;
import nc.ui.queryarea.component.QSTitlePanel;
import nc.ui.queryarea.state.AbsMinimizedUIState;
import nc.ui.queryarea.state.AbsRestoredUIState;
import nc.ui.queryarea.util.QueryAreaColor;
import nc.ui.queryarea.util.QueryAreaIcons;
import nc.ui.querytemplate.ICriteriaChangedListener;
import nc.ui.querytemplate.QCDShowMode;
import nc.utils.iufo.TotalOrgAttributeUtils;
import nc.vo.bd.access.tree.BDTreeCreator;
import nc.vo.bd.userdefrule.UserdefitemVO;
import nc.vo.corg.ReportManaStruMemberWithCodeNameVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.query.IUfoDetailQueryCondVO;
import nc.vo.iufo.query.IUfoQueryCondVO;
import nc.vo.iufo.query.IUfoQueryLoginContext;
import nc.vo.iufo.total.ReportOrgInnerVO;
import nc.vo.pub.BusinessException;

import com.borland.jbcl.layout.VerticalFlowLayout;
import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.report.resource.ResConst;

@SuppressWarnings("restriction")
public class IUfoQuickQueryArea extends UIStatefulPanel implements UIStateChangedListener, IUfoQueryCondChangeListener {
	/**
	 *
	 */
	private static final String IS_NOT_ORG_PK = "isNotOrgPK";
	private static final long serialVersionUID = -9166969927017138506L;
	// 查询执行器
	private IUfoQueryExecutor queryExecutor;

	// 包含查询条件的待滚动条的面板
	private UIScrollPane scrollPane;

	// 查询标题panel 左边是 弹出 查询对话框的按钮，中间是title 右边是 向下收缩的按钮
	private QSTitlePanel titlePanel;

	// 包含查询条件面板 等的中间显示的面板
	private UIPanel centerPanel;

	// 底部显示的面板，可能包括 树面板 节点选择方式 按钮面板
	private UIPanel bottomPanel;
	//
	private UIPanel buttonPanel;

	// 向下收缩按钮
	private BottomAction bottomAction;

	// 持有者 环境类
	private IUfoQueryHolder queryHolder = null;

	// 分割面板
	private UISplitPane splitPane = null;

	// 任务分配组织树
	private IUfoQueryOrgTree orgTreePane = null;

	// 包含组织树的滚动条面板
	private UIScrollPane scrollOrgTreePane = null;

	private final Map<String, Integer> hashDivLocation = new HashMap<String, Integer>();

	// 报表选择面板
	private OrgRepFilterPanel orgRepPane = null;

	// 是否显示报表选择 如果显示，那么报表选择 显示在快速查询区域 查询条件的上面
	private boolean showRepSelect = true;

	// 组织节点选择方式面板
	private IUfoOrgSelectedStrategyPanel orgSelectedPanel;

	// 包含了组织树 ，组织选择节点的面板
	private UIPanel scrollAndSelectedPanel = null;

	private Action leftMiniAction;
	// editor tianjlc 2015-03-09 记录是否为第一次加载
	private boolean isFirstLoad = true;
	// editor tianjlc 2015-03-13 记录是否已经执行了查询，在执行刷新时间关键字时校验
	private boolean isQueryed = false;
	// editor tianjlc 2015-03-16 是否需要转换时间关键字
	private boolean isDateTransform = false;

	/**
	 * @return the showRepSelectBtn
	 */
	public boolean isShowRepSelect() {
		return showRepSelect;
	}

	/**
	 * @param showRepSelect
	 *            the showRepSelect to set
	 */
	public void setShowRepSelect(boolean showRepSelect) {
		this.showRepSelect = showRepSelect;
	}

	public JComponent getJComponent() {
		return this;
	}

	private ImageIcon okImage = ResConst.getImageIcon("reportquery/ok.gif");
	private IUfoQSTitlePanel iufoTitlePanel;

	public IUfoQuickQueryArea(IUfoQueryHolder queryHolder, boolean showRepSelectBtn) {
		super();
		this.queryHolder = queryHolder;
		this.showRepSelect = showRepSelectBtn;
		queryHolder.getQueryCondChangeHandler().addQueryCondChangeListener(this);
		this.uiState = new RestoredUIState();
		initUI();
		initListeners();
		QueryAreaColor.setBkgrdDefaultColor(this);
	}

	// 查询
	public void doQuery() {
		try {
			IUfoQueryCondVO queryCond = getQueryScheme();
			if(queryCond.getKeyGroupPK()==null){
				return;
			}
			// 设置组织节点选择策略
			int type = getOrgType();
			if (type == IUfoQueryCondVO.ORGTYPE_SELF && IS_NOT_ORG_PK.equals(queryCond.getKeyVal(KeyVO.CORP_PK))) {
				throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820003-0058")/*@res "当前不是有效组织！"*/);
			}

			queryCond.setOrgType(type);
			if (getOrgTreePane().getAllData().length == 0) {
				queryCond.setOrgPKs(new String[0]);
				queryCond.setSelectedOrgPKs(new String[0]);
				queryCond.setKeyVal(KeyVO.CORP_PK, null);
			} else {
				String[] orgPKs = getSelectTreeNode();
				queryCond.setSelectedOrgPKs(orgPKs);
			}

			String[][] strSelRepTaskPKs = getOrgRepFilterPane().getRepTaskPKs();
			queryCond.setFilterRepPKs(strSelRepTaskPKs[0]);
			queryCond.setFilterTaskPKs(strSelRepTaskPKs[1]);
			resetRepPks(queryCond);
			getQueryExecutor().doQuery(queryCond);
			// editor tianjlc 2015-03-13 设置为已经执行了查询
			isQueryed = true;
			getOrgTreePane().setDirectQuery(true);
		} catch (Exception te) {
			com.ufida.iufo.pub.tools.AppDebug.debug(te);
			getQueryHolder().getExceptionHanlder().handlerExeption(te);
		}
	}

	private void resetRepPks(IUfoQueryCondVO queryCond) {
		List<IUfoFilterItem> filterItems = queryHolder.getFilterItem();
		for (IUfoFilterItem filterItem : filterItems) {
			if(filterItem instanceof RepFilterItem){
				filterItem.onQueryCondSave(queryCond);
			}
		}
		
	}

	// 取得节点选择方式
	public int getOrgType() {
		return ((UfoeFilterTreePanel)((UIPanel)getScrollOrgTreePane().getViewport().getView()).getComponent(1)).getOrgType();
	}

	// 在光标移到JscrollPane面板时，设置JscrollPane的滚动条显示策略
	private void initMouseListener4ScrollPane(final JScrollPane c) {
		new ContainerMouseEnteredDetector(c, new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				c.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				c.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				c.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
				c.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			}
		});
	}

	private void initUI() {

		this.setLayout(new BorderLayout());
		// this.setBorder(BorderFactory.createLineBorder(QueryAreaColor.BORDER_COLOR));
		this.add(getTitlePanel(), BorderLayout.NORTH);
		this.add(getSplitPane(), BorderLayout.CENTER);
		QueryAreaColor.setBkgrdDefaultColor(this);
	}

	@Override
	public void onQueryCondChange(IUfoQueryCondVO oldQueryCond, final IUfoQueryCondVO newQueryCond, Object eventSource) {
		KeyGroupVO keyGroup = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(newQueryCond.getKeyGroupPK());
		if (keyGroup == null) {
			// 移除监听 ，之前给此scrollPane 面板加上了ContainerMouseEnteredDetector 监听器。
			for (MouseListener mousListener : getScrollPane().getMouseListeners()) {
				getScrollPane().removeMouseListener(mousListener);
			}
			return;
		}

		final UIPanel panel = new UIPanel();
		QueryAreaColor.setBkgrdDefaultColor(panel);
		VerticalFlowLayout verticalFlowLayout = new VerticalFlowLayout();
		verticalFlowLayout.setVgap(5);
		verticalFlowLayout.setHgap(0);
		panel.setLayout(verticalFlowLayout);
		List<IUfoFilterItem> vFilterItem = queryHolder.getQuickQueryItem();

		IUfoQueryCondVO inputCond = queryHolder.getQueryCondEditHandler().getInputQueryCond(newQueryCond.getPk_querycond());

		if (inputCond != null)
			newQueryCond.setDetailcond(((IUfoDetailQueryCondVO) inputCond.getDetailcond()).clone());

		final Dimension inputDimension = new Dimension(120, 22);
		final Dimension labelDimension = new Dimension(80, 22);

		Boolean mustinputflg = true;
		for (IUfoFilterItem item : vFilterItem) {
			if ((item instanceof InputStateFilterItem) || (item instanceof CheckExeContentFilterItem)) {
				mustinputflg = false;
				break;
			}
		}

		// wangqi 20120719 修改多语显示问题
		String itemname;
		if (isShowRepSelect()) {
			itemname = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0699")/* @res"报表选择"*/;
		} else {
			itemname = "";
		}

		for (IUfoFilterItem item : vFilterItem) {
			if (item.getName().length() > itemname.length()) {
				itemname = item.getName();
			}
		}

		if (itemname.length() > 24) {
			itemname = itemname.substring(0, 24);
		}

		FlowLayout gl = new FlowLayout(FlowLayout.CENTER, 5, 1);
		UIPanel itemPanel = null;
		if (isShowRepSelect()) {
			itemPanel = new UIPanel(gl);
			UILabel label = new UILabel(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0699")/*
																															 * @
																															 * res
																															 * "报表选择"
																															 */);

			label.setHorizontalAlignment(JLabel.RIGHT);

			int width = label.getFontMetrics(label.getFont()).stringWidth(itemname);
			// @edit by wuyongc at 2013-7-9,上午11:12:59 设置一个最小宽度。
			if (width < 60) {
				width = 60;
			}
			label.setPreferredSize(new Dimension(width, 22));

			// label.setPreferredSize(labelDimension);
			itemPanel.add(label);
			OrgRepFilterPanel orgRepFilterPane = getOrgRepFilterPane();
			orgRepFilterPane.initUI();
			orgRepFilterPane.setPreferredSize(inputDimension);
			itemPanel.add(getOrgRepFilterPane());
			// 清空已经存在的数据
			getOrgRepFilterPane().setRepTaskPKs(new String[2][]);
			QueryAreaColor.setBkgrdDefaultColor(itemPanel);
			panel.add(itemPanel);

		}
		// 20120301 TODO wangqi 报送管理和批量上报增加必录标记 todo
		for (IUfoFilterItem item : vFilterItem) {

			UILabel lbl = new UILabel(item.getName());
			int width = lbl.getFontMetrics(lbl.getFont()).stringWidth(itemname);
			if (width < 60) {
				width = 60;
			}
			lbl.setPreferredSize(new Dimension(width, 22));
			itemPanel = new UIPanel(gl);

			itemPanel.add(lbl);
			lbl.setHorizontalAlignment(JLabel.RIGHT);
			JComponent comp = item.getEditComponent();
			if (KeyVO.isTTimeKey(item.getKeyPK())) {
				final UIRefPane refcom = (UIRefPane) comp;
				refcom.addValueChangedListener(new ValueChangedListener() {

					@Override
					public void valueChanged(ValueChangedEvent event) {
						try {
							refreshOrgTreePane(newQueryCond);
							if (getOrgTreePane().getAllData().length == 0) {
								getQueryHolder().getQueryCond().setSelectedOrgPKs(new String[0]);
							} else {

							}
						} catch (Exception e) {
							AppDebug.debug(e);
						}
					}
				});
			}
			comp.setPreferredSize(inputDimension);
			if (comp instanceof UITextField) {
				// wangqi 20120905 和参照输入框等保持同样的宽度
				((UITextField) comp).setColumns(11);
			}

			// 20120301 TODO wangqi 报送管理和批量上报增加必录标记 todo
			if (mustinputflg) {
				if (comp instanceof UIRefPane) {
					((UIRefPane) comp).getUITextField().setShowMustInputHint(true);
				}

				if (comp instanceof UITextField) {
					((UITextField) comp).setShowMustInputHint(true);
				}
			}

			itemPanel.add(comp);
			QueryAreaColor.setBkgrdDefaultColor(itemPanel);
			panel.add(itemPanel);
		}
		QueryAreaColor.setBkgrdDefaultColor(panel);
		getScrollPane().setViewportView(panel);
		Dimension size = panel.getPreferredSize();

		splitPane.setDividerLocation(getDivideLocation(size));
		getCenterPanel().setMinimumSize(new Dimension(0, splitPane.getDividerLocation()));
		getCenterPanel().validate();
		getBottomPanel().remove(getButtonPanel());
		getBottomPanel().remove(getScrollPanelAndSelectedPanel());
		if (keyGroup.getKeyByKeyPk(KeyVO.CORP_PK) != null) {
//			addSelectedPanel();
			getBottomPanel().add(getScrollPanelAndSelectedPanel(), BorderLayout.CENTER);
			getBottomPanel().add(getButtonPanel(), BorderLayout.SOUTH);
			refreshOrgTreePane(newQueryCond);
		} else {
			getBottomPanel().add(getButtonPanel(), BorderLayout.NORTH);
		}

		getBottomPanel().revalidate();
		getBottomPanel().repaint();
	}

	/**
	 * 适配报表组织体系添加，更新报表组织体系树
	 * 
	 * @creator tianjlc at 2015-2-4 下午3:20:46
	 * @return void
	 */
	private void refreshOrgTreePane(IUfoQueryCondVO queryCond) {
		if (isFirstLoad) {
			isFirstLoad = false;
			return;
		}
		getOrgTreePane().setDate(getKeyDate());
		getOrgTreePane().setDateTranform(isDateTransform);
		try {
			getOrgTreePane().refreshByQueryCond(queryCond, getQueryHolder().getLoginContext().getInitParam().getRepStruPK(), (String) getQueryHolder().getLoginContext().getInitParam().getMainOrgPK(), queryCond.getOrgPKs(), queryCond.getKeyVal(KeyVO.CORP_PK));
			// editor tianjlc 2015-03-13 刷新右面列表前先校验是否已经执行了查询，若执行了查询，执行属性，默认为未执行
			if (isQueryed) {
				doQuery();
			}
		} catch (Exception e) {
			AppDebug.debug(e);
		}

	}

	public void clearQuickQueryArea() {

		getBottomPanel().removeAll();
		getBottomPanel().add(getButtonPanel(), BorderLayout.NORTH);
		// @edit by wuyongc at 2011-6-18,下午03:00:58
		// 移除监听 ，之前给此scrollPane 面板加上了ContainerMouseEnteredDetector 监听器。
		for (MouseListener mousListener : getScrollPane().getMouseListeners()) {
			getScrollPane().removeMouseListener(mousListener);
		}

		getScrollPane().removeAll();
		if (getScrollPane().getViewport() != null)
			getScrollPane().getViewport().removeAll();

		getCenterPanel().removeAll();
		getCenterPanel().add(getScrollPane(), BorderLayout.CENTER);
		getSplitPane().setDividerLocation(0);
		validate();
		// updateUI();
	}

	@Override
	public void onQueryCondUpdate(IUfoQueryCondVO oldQueryCond, IUfoQueryCondVO newQueryCond, Object eventSource) {
	}

	@Override
	public void onQueryCondClear(Object eventSource) {
	}

	@Override
	public String[] onQueryCondSave(IUfoQueryCondVO queryCond) {
		String strSelOrgPK = null;
		TreePath treePath = getOrgTreePane().getSelectionPath();
		if (treePath != null) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
			if (node.getUserObject() instanceof ReportManaStruMemberWithCodeNameVO) {
				ReportManaStruMemberWithCodeNameVO mem = (ReportManaStruMemberWithCodeNameVO) node.getUserObject();
				strSelOrgPK = mem.getPk_org();
			} else if (node.getUserObject() instanceof ReportOrgInnerVO) {
				ReportOrgInnerVO repOrg = (ReportOrgInnerVO) node.getUserObject();
				strSelOrgPK = repOrg.getReportOrgVO().getPk_reportorg();
				// 暂时以此来区分是否是 业务单元或者组织。
				if (repOrg.getReportOrgVO().getSourceorgtype() == null) {
					strSelOrgPK = IS_NOT_ORG_PK;
				}
			}
		}

		if (UfoPublic.stringIsNull(strSelOrgPK)) {
			return new String[] { nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0700") /*
																													 * @
																													 * res
																													 * "请选择组织树上的一个节点"
																													 */};
		}
		queryCond.setKeyVal(KeyVO.CORP_PK, strSelOrgPK);

		return null;
	}

	public IUfoQueryOrgTree getOrgTreePane() {
		if (orgTreePane == null) {

			orgTreePane = new IUfoQueryOrgTree(queryHolder.getLoginContext(), getKeyDate(), isDateTransform);

			orgTreePane.setBorder(BorderFactory.createEmptyBorder(3, 2, 1, 1));
			// orgTreePane.setBackground(Color.WHITE);
			QueryAreaColor.setBkgrdDefaultColor(orgTreePane);
		}
		return orgTreePane;
	}

	// 设置监听
	private void initListeners() {
		initMouseListener4ScrollPane(getScrollPane());
	}

	// 取得组织节点选择方式面板
	private IUfoOrgSelectedStrategyPanel getOrgSelectedStrategyPanel() {
		if (orgSelectedPanel == null) {
			orgSelectedPanel = new IUfoOrgSelectedStrategyPanel();
			QueryAreaColor.setBkgrdDefaultColor(orgSelectedPanel);
		}
		return orgSelectedPanel;
	}

	public void initOrgTreeByStru() {
		getOrgTreePane().initOrgTreeByStru();
		// getOrgSelectedStrategyPanel().setOrgSelfEnabled(false);
	}

	protected UISplitPane getSplitPane() {
		if (splitPane == null) {
			splitPane = new MySplitPane(UISplitPane.VERTICAL_SPLIT);
			// (((BasicSplitPaneUI)
			// splitPane.getUI()).getDivider()).setBorder(BorderFactory.createEmptyBorder());
			(((BasicSplitPaneUI) splitPane.getUI()).getDivider()).setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, Color.WHITE));
			splitPane.add(getCenterPanel());
			splitPane.add(getBottomPanel());
			splitPane.setDividerLocation(0);
			QueryAreaColor.setBkgrdDefaultColor(splitPane);
		}
		return splitPane;
	}

	/**
	 * 树面板 ,包含节点选择策略
	 * 
	 * @create by wuyongc at 2011-5-18,上午11:08:38
	 * 
	 * @return
	 */
	public UIScrollPane getScrollOrgTreePane() {
		if (scrollOrgTreePane == null) {
			UIPanel pal = new UIPanel();
			pal.setLayout(new BorderLayout());
			pal.setBorder(BorderFactory.createEmptyBorder());
			//editor tianjlc 2015-04-15 在数据中心下的节点中，按组织组织属性是否需要还待商榷，适配报表组织体系多版本，暂时将这个按钮注释掉
//			final JPopupMenu menu = new JPopupMenu();
//			QueryAreaColor.setBkgrdDefaultColor(menu);
//			final JMenuItem item1 = new JMenuItem(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0074")/*
//																																	 * @
//																																	 * res
//																																	 * "按组织属性显示"
//																																	 */);
//			final JMenuItem item2 = new JMenuItem(NCLangUtil.getStrByID("1820001_0", "01820001-0079"/* 按组织体系显示 */), okImage);
//
//			menu.add(item1);
//			menu.addSeparator();
//			menu.add(item2);
//			QueryAreaColor.setBkgrdDefaultColor(item1);
//			item1.addActionListener(new ActionListener() {
//				private OrgAttributeConstructDlg orgAttributeDlg;
//
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					String orgPK = getOrgTreePane().getLoginContext().getInitParam().getMainOrgPK();
//					if (orgAttributeDlg == null) {
//						orgAttributeDlg = new OrgAttributeConstructDlg(getTopLevelAncestor(), orgPK);
//					} else {
//						try {
//							if (!orgAttributeDlg.getMainOrgPK().equals(orgPK)) {
//								orgAttributeDlg.setMainOrgPK(orgPK);
//								orgAttributeDlg.setItemsVO(null);
//							}
//							orgAttributeDlg.initLeftAndRightDatas();
//						} catch (BusinessException e1) {
//							AppDebug.debug(e1);
//						}
//					}
//
//					if (orgAttributeDlg.showModal() == UIDialog.ID_OK) {
//						UserdefitemVO[] userdefitems = orgAttributeDlg.getSelectUserdefitem();
//
//						if (userdefitems != null && userdefitems.length > 0) {
//							initTreeModelByOrgAttribute(userdefitems);
//							item1.setIcon(okImage);
//							item2.setIcon(null);
//						}
//					}
//				}
//			});

//			item2.addActionListener(new ActionListener() {
//
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					item1.setIcon(null);
//					item2.setIcon(okImage);
//					initOrgTreeByStru();
//				}
//			});

//			final UIButton orgShowBtn = new UIButton();
//			orgShowBtn.setPreferredSize(new Dimension(20, 20));
//			orgShowBtn.setIcon(ResConst.getImageIcon("reportcore/data_relation.gif"));
//
//			orgShowBtn.setHideActionText(true);
//			orgShowBtn.setToolTipText(NCLangUtil.getStrByID("1820001_0", "01820002-0104"/* 组织显示方式 */));
//			orgShowBtn.addMouseListener(new MouseAdapter() {
//				@Override
//				public void mouseClicked(MouseEvent e) {
//					menu.show(orgShowBtn, e.getX() + 15, e.getY() + 5);
//				}
//			});

			IUfoQSTitlePanel iufoTitlePanel = getIufoQSTitlePanel();
//			iufoTitlePanel.getRightPanel().add(orgShowBtn);
			iufoTitlePanel.addAction2RightToolbar(new Action[] { getBottomAction() });

			// pal.add(new
			// OrgBuildTitlePanel(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0425")/*@res
			// "报表组织"*/,this),BorderLayout.NORTH);
			pal.add(iufoTitlePanel,BorderLayout.NORTH);
			// tianchuan 20150305 报送管理左侧界面去掉多余的滚动条
			//tianjlc 2015-09-01 报表树数据过多时，不能全部加载
			UfoeFilterTreePanel filterTreePanel = new UfoeFilterTreePanel(getOrgTreePane());
//			FilterTreePanel filterTreePanel = new FilterTreePanel(getOrgTreePane());
//			filterTreePanel.setPreferredSize(new Dimension(260, 500));
			QueryAreaColor.setBkgrdDefaultColor(filterTreePanel);

			pal.add(filterTreePanel,BorderLayout.CENTER);
			QueryAreaColor.setBkgrdDefaultColor(pal);
			scrollOrgTreePane = new UIScrollPane(pal, UIScrollPane.VERTICAL_SCROLLBAR_NEVER, UIScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//			scrollOrgTreePane.setViewportView(filterTreePanel);
//			initMouseListener4ScrollPane(scrollOrgTreePane);
			// scrollOrgTreePane.setBorder(BorderFactory.createEmptyBorder());
			QueryAreaColor.setBkgrdDefaultColor(pal);
			QueryAreaColor.setBkgrdDefaultColor(scrollOrgTreePane);
		}

		return scrollOrgTreePane;
	}


	/**
	 * @create by wuyongc at 2013-12-23,下午1:27:00
	 * 
	 * @return
	 */
	private IUfoQSTitlePanel getIufoQSTitlePanel() {
		if (iufoTitlePanel == null) {
			iufoTitlePanel = new IUfoQSTitlePanel(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820007-0023")/*
																																	 * @
																																	 * res
																																	 * "报表组织"
																																	 */);
			QueryAreaColor.setBkgrdDefaultColor(iufoTitlePanel);
		}
		return iufoTitlePanel;
	}

	protected UIPanel getBottomPanel() {
		if (bottomPanel == null) {
			bottomPanel = new UIPanel();
			bottomPanel.setLayout(new BorderLayout());
			bottomPanel.add(getButtonPanel(), BorderLayout.NORTH);
			// QueryAreaColor.setBkgrdDefaultColor(bottomPanel);
			QueryAreaColor.setBkgrdDefaultColor(bottomPanel);
		}
		return bottomPanel;
	}

	protected UIPanel getCenterPanel() {
		if (centerPanel == null) {
			centerPanel = new UIPanel();
//			centerPanel.setPreferredSize(new Dimension((int) getScrollPane().getPreferredSize().getWidth() + 40,
//					(365 + 40)));
			// new
			// Dimension((int)getScrollPanelAndSelectedPanel().getPreferredSize().getWidth()+40,
			centerPanel.setLayout(new BorderLayout());
			centerPanel.add(getScrollPane(), BorderLayout.CENTER);
			// centerPanel.add(getScrollPanelAndSelectedPanel(),BorderLayout.CENTER);
			QueryAreaColor.setBkgrdDefaultColor(centerPanel);
		}
		return centerPanel;
	}

	protected UIPanel getButtonPanel() {
		if (buttonPanel == null) {// 根据是否有单位关键字来判断是否显示树节点选择策略
			buttonPanel = new IUfoQuickQueryAreaButtonPanel(this);
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
			QueryAreaColor.setBkgrdDefaultColor(buttonPanel);
		}
		return buttonPanel;
	}

	protected UIScrollPane getScrollPane() {
		if (scrollPane == null) {
			final UIPanel p0 = new UIPanel();
			QueryAreaColor.setBkgrdDefaultColor(p0);
			scrollPane = new UIScrollPane(p0, UIScrollPane.VERTICAL_SCROLLBAR_NEVER, UIScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			QueryAreaColor.setBkgrdDefaultColor(scrollPane);
		}
		return scrollPane;
	}

	private UIPanel getScrollPanelAndSelectedPanel() {
		if (scrollAndSelectedPanel == null) {
			scrollAndSelectedPanel = new UIPanel(new BorderLayout());
			scrollAndSelectedPanel.add(getScrollOrgTreePane(), BorderLayout.CENTER);
			QueryAreaColor.setBkgrdDefaultColor(scrollAndSelectedPanel);
		}
		return scrollAndSelectedPanel;
	}

	// 添加 节点选择面板
	//modifier tianjlc 在这里加的话，数据过多时，浏览器加载不全
//	private void addSelectedPanel() {
//		getScrollPanelAndSelectedPanel().add(getOrgSelectedStrategyPanel(), BorderLayout.SOUTH);
//		// getScrollPanelAndSelectedPanel().setBorder(BorderFactory.createLineBorder(QueryAreaColor.BORDER_COLOR));
//	}

	private IUfoQueryCondVO getQueryScheme() throws Exception {
		// IUfoQueryCondVO
		// queryCond=(IUfoQueryCondVO)queryHolder.getQueryCond().clone();
		IUfoQueryCondVO queryCond = queryHolder.getQueryCond();

		String checkMessage = checkCondition(queryCond);
		if (checkMessage != null) {
			throw new RuntimeException(checkMessage);
		}
		queryHolder.getQueryCondEditHandler().addInputQueryCond(queryCond);
		return queryCond;
	}

	/**
	 * 生成查询方案名称(即快速查询)
	 */
	private String createQuerySchemeName() {
		return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0741")/*
																								 * @
																								 * res
																								 * "查询条件"
																								 */;
	}

	private QSTitlePanel getTitlePanel() {
		if (titlePanel == null) {
			titlePanel = new QSTitlePanel(createQuerySchemeName());
			QSEditAction action = (QSEditAction) createQSEditAction();
			titlePanel.setQsEditAction(action);
			titlePanel.addAction2LeftToolbar(action);
			titlePanel.addAction2RightToolbar(getBottomAction());
			QueryAreaColor.setBkgrdDefaultColor(titlePanel);
		}
		return titlePanel;
	}

	public void initTreeModelByOrgAttribute(UserdefitemVO[] userdefitems) {
		try {
			if (userdefitems != null && userdefitems.length > 0) {
				ReportOrgTreeCreateStrategy treeCreateStrategy = new ReportOrgTreeCreateStrategy();
				treeCreateStrategy.setRootName(userdefitems[0].getShowname());

				IUfoQueryLoginContext ctx = getOrgTreePane().getLoginContext();
				String strTaskPK = ctx.getInitParam().getCurTaskPK();
				String mainOrgPK = ctx.getInitParam().getMainOrgPK();
				String rsmPK = ctx.getInitParam().getRmsData().getPk();

				ReportOrgInnerVO[] reportOrgs = TotalOrgAttributeUtils.handleAttribute(userdefitems, strTaskPK, mainOrgPK, rsmPK, true);

				getOrgTreePane().setModel(BDTreeCreator.createTree(reportOrgs, treeCreateStrategy));

				getOrgTreePane().setCellRenderer(new UnitAttributeTreeCellRender());
				ToolTipManager.sharedInstance().registerComponent(getOrgTreePane());

				if (getOrgType() == IUfoQueryCondVO.ORGTYPE_SELF) {
					getOrgSelectedStrategyPanel().setOrgType(IUfoQueryCondVO.ORGTYPE_ALL);
					// getOrgSelectedStrategyPanel().setOrgSelfEnabled(false);
				}
				getOrgTreePane().initSelectedNode();

			}
		} catch (Exception e) {
			AppDebug.debug(e);
		}
	}

	@SuppressWarnings("serial")
	private Action createQSEditAction() {
		QSEditAction editAction = new QSEditAction() {
			@Override
			public void actionPerformed(ActionEvent e) {

				if (!this.isEnabled())
					return;
				IUfoQueryConditionDLG qcd = getQueryHolder().getQueryDlg();
				qcd.doInitUI();
				// 取得查询对话框的方案树容器,设置方案选中为快速查询区域的选中的查询方案
				if (getQueryHolder().getQueryCond().equals(qcd.getQueryHolder().getQueryCond())) {
					try {
						qcd.getQSTreeContainer().setQSSelected(getQueryScheme());
					} catch (Exception e1) {
						AppDebug.debug(e1);
					}
					getQueryHolder().setSchemaChanged(true);
				}

				// 需要更换 查询按钮 的文本显示，免得点查询按钮时数据区没有相应造成误解
				qcd.setShowMode(QCDShowMode.VIEW_MODE);
				qcd.showModal();
				qcd.setShowMode(QCDShowMode.QUERY_MODE);
			}
		};
		editAction.setEnabled(getQueryHolder().getNodeEnv().getCurrTaskPK() != null);
		return editAction;
	}

	public void setBottomActionEnabled(boolean enabled) {
		getBottomAction().setEnabled(enabled);
	}

	private BottomAction getBottomAction() {
		if (bottomAction == null) {
			bottomAction = new BottomAction();
		}
		return bottomAction;
	}

	@SuppressWarnings("serial")
	class BottomAction extends AbstractAction {
		public BottomAction() {
			putValue(Action.NAME, NCLangRes.getInstance().getStrByID("_template", "UPP_NewQryTemplate-0092")/* 置顶 */);
			putValue(SMALL_ICON, QueryAreaIcons.BOTTOM_ICON);
			putValue(ToolBarButton.HIGHLIGHT_ICON, QueryAreaIcons.BOTTOM_ICON);
			putValue(Action.SHORT_DESCRIPTION, NCLangRes.getInstance().getStrByID("_template", "UPP_NewQryTemplate-0092")/* 置顶 */);
			putValue("TOOLBAR_SHOWNAME_KEY", false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (isMinimized()) {
				restoreUI();
			} else {
				minimizeUI();
			}
		}
	}

	public boolean isMinimized() {
		return getUIState() instanceof MinimizedUIState;
	}

	private String checkCondition(IUfoQueryCondVO queryCond) {
		String[] strErrMsgs = queryHolder.getQueryCondChangeHandler().fireQueryCondSave(queryCond);
		if (strErrMsgs != null && strErrMsgs.length > 0) {
			StringBuffer bufMsg = new StringBuffer();
			for (String strErrMsg : strErrMsgs) {
				bufMsg.append(strErrMsg + "\r\n");
			}
			return bufMsg.toString().trim();
		}
		return null;
	}

	public IUfoQueryExecutor getQueryExecutor() {
		return queryExecutor;
	}

	public void setQueryExecutor(IUfoQueryExecutor queryExecutor) {
		this.queryExecutor = queryExecutor;
	}

	/**
	 * 最小化的UI状态
	 */
	private static class MinimizedUIState extends AbsMinimizedUIState {

		/**
		 * 恢复UI界面，置换Bottom按钮图标，将查询条件Panel和查询按钮Panel重新加载并展现
		 */
		@Override
		public void restoreUI(UIStatefulPanel panel) {
			IUfoQuickQueryArea quickQA = (IUfoQuickQueryArea) panel;
			quickQA.getBottomAction().putValue(Action.NAME, NCLangRes.getInstance().getStrByID("_template", "UPP_NewQryTemplate-0092")/* 置顶 */);
			quickQA.getBottomAction().putValue(Action.SMALL_ICON, QueryAreaIcons.BOTTOM_ICON);
			quickQA.getBottomAction().putValue(ToolBarButton.HIGHLIGHT_ICON, QueryAreaIcons.BOTTOM_ICON);
			quickQA.getBottomAction().putValue(Action.SHORT_DESCRIPTION, NCLangRes.getInstance().getStrByID("_template", "UPP_NewQryTemplate-0092")/* 置顶 */);
			// quickQA.getTitlePanel().addAction2RightToolbar(quickQA.getBottomAction());
			quickQA.getIufoQSTitlePanel().addAction2RightToolbar(new Action[] { quickQA.getBottomAction() });
			quickQA.setUIState(new RestoredUIState());
			quickQA.fireUIStateChanged(UIStateChangedEvent.EXPAND);
		}
	}

	/**
	 * 恢复后的UI状态
	 */
	private static class RestoredUIState extends AbsRestoredUIState {

		/**
		 * 最小化UI界面，置换Bottom按钮图标，将查询条件Panel和查询按钮Panel移除
		 */
		@Override
		public void minimizeUI(UIStatefulPanel panel) {
			IUfoQuickQueryArea quickQA = (IUfoQuickQueryArea) panel;
			quickQA.getBottomAction().putValue(Action.NAME, NCLangRes.getInstance().getStrByID("_template", "UPP_NewQryTemplate-0120")/* 恢复 */);
			quickQA.getBottomAction().putValue(Action.SMALL_ICON, QueryAreaIcons.TOP_ICON);
			quickQA.getBottomAction().putValue(ToolBarButton.HIGHLIGHT_ICON, QueryAreaIcons.TOP_ICON);
			quickQA.getBottomAction().putValue(Action.SHORT_DESCRIPTION, NCLangRes.getInstance().getStrByID("_template", "UPP_NewQryTemplate-0120")/* 恢复 */);
			// quickQA.getTitlePanel().addAction2RightToolbar(quickQA.getBottomAction());
			quickQA.getIufoQSTitlePanel().addAction2RightToolbar(new Action[] { quickQA.getBottomAction() });
			quickQA.setUIState(new MinimizedUIState());
			quickQA.fireUIStateChanged(UIStateChangedEvent.COLLAPSE);
		}
	}

	@Override
	public void stateChanged(UIStateChangedEvent event) {
		// 不是自己发出的事件并且有UI展开了，则本界面最小化
		if (event.getSource() != this) {
			if (event.getUIStateType() == UIStateChangedEvent.EXPAND) {
				minimizeUI();
				fireUIStateChanged(UIStateChangedEvent.COLLAPSE);
			} else if (event.getUIStateType() == UIStateChangedEvent.COLLAPSE_SELF) {
				restoreUI();
				fireUIStateChanged(UIStateChangedEvent.EXPAND);
			} else {
				if (event.getUIStateType() == IUfoQuerySchemeArea.SCHEME_EXPAND || event.getUIStateType() == event.getUIStateType()) {
					restoreUI();
					fireUIStateChanged(event.getUIStateType());
				}
			}
		}
	}

	public void clearData() {
		queryHolder.getQueryCondChangeHandler().fireQueryCondClear(this);
	}

	public void addCriteriaChangedListener(ICriteriaChangedListener listener) {
		listenerList.remove(ICriteriaChangedListener.class, listener);
		listenerList.add(ICriteriaChangedListener.class, listener);
	}

	/**
	 * 最小化尺寸，要能够容纳标题栏
	 * 
	 * @see javax.swing.JComponent#getMinimumSize()
	 */
	@Override
	public Dimension getMinimumSize() {
		Dimension d = getTitlePanel().getPreferredSize();
		// 横向最小宽度，否则容易造成 组织显示方式按钮被遮盖。
		return new Dimension(280, (int) d.getHeight());
	}

	public IUfoQueryHolder getQueryHolder() {
		return queryHolder;
	}

	private void saveDivideLocation(int location) {
		if (queryHolder.getQueryCond() != null && queryHolder.getQueryCond().getPk_querycond() != null)
			hashDivLocation.put(queryHolder.getQueryCond().getPk_querycond(), location);
	}

	protected int getDivideLocation(Dimension size) {
		int divideLocation = -1;
		if (queryHolder.getQueryCond() != null && queryHolder.getQueryCond().getPk_querycond() != null && hashDivLocation.get(queryHolder.getQueryCond().getPk_querycond()) != null)
			divideLocation = hashDivLocation.get(queryHolder.getQueryCond().getPk_querycond());

		List<IUfoFilterItem> vFilterItem = queryHolder.getQuickQueryItem();
		int divideLocation1 = vFilterItem.size() > 0 ? ((int) size.getHeight() + 5) : 0;
		if (divideLocation >= (int) size.getHeight() + 10) {
			divideLocation = divideLocation1;
		} else if (divideLocation <= 5) {
			if (getSplitPane().getSize().getHeight() > 100) {
				Dimension parentSize = getSplitPane().getSize();
				int iMaxDivide = (int) parentSize.getHeight() - 340;
				divideLocation = Math.min(iMaxDivide, divideLocation1);
			} else
				divideLocation = divideLocation1;
		}

		return divideLocation;
	}

	@SuppressWarnings("serial")
	private class MySplitPane extends UISplitPane {
		MySplitPane(int direction) {
			super(direction);
		}

		@Override
		public void setDividerLocation(int location) {
			super.setDividerLocation(location);
			saveDivideLocation(location);
		}
	}

	private OrgRepFilterPanel getOrgRepFilterPane() {
		if (orgRepPane == null) {
			orgRepPane = new OrgRepFilterPanel(queryHolder.getLoginContext());
			orgRepPane.setQueryAction(queryHolder.getQueryAction());
		}
		return orgRepPane;
	}

	/**
	 * @return 根据当前选中的节点和节点选择选择策略得到的组织PK
	 */
	private String[] getSelectTreeNode() {
		Object[] objects = new Object[0];
		int orgType = getOrgType();
		switch (orgType) {
		case IUfoQueryCondVO.ORGTYPE_SELF:
			objects = getSelectNodeOfNoSub();
			break;
		case IUfoQueryCondVO.ORGTYPE_ALL:
			objects = getSelectNodeOfAll();
			break;
		case IUfoQueryCondVO.ORGTYPE_DIRECT:
			objects = getSelectNodeOfSub();
			break;
		case IUfoQueryCondVO.ORGTYPE_LEAF:
			objects = getSelectNodeOfLEAFSUB();
		}
		if (objects == null)
			return null;
		String[] selectedOrgPKs = new String[objects.length];
		List<String> orgPkList = new ArrayList<String>();
		Object userObj = null;
		for (int i = 0; i < selectedOrgPKs.length; i++) {
			if (objects[i] instanceof DefaultMutableTreeNode) {
				userObj = ((DefaultMutableTreeNode) objects[i]).getUserObject();
				if (userObj instanceof ReportOrgInnerVO) {
					// NullPointException ?
					orgPkList.add(((ReportOrgInnerVO) userObj).getReportOrgVO().getPk_reportorg());
				} else if (userObj instanceof ReportManaStruMemberWithCodeNameVO) {
					orgPkList.add(((ReportManaStruMemberWithCodeNameVO) userObj).getPk_org());
				}
			}
			// selectedOrgPKs[i] = objects[i];
		}
		return orgPkList.toArray(new String[0]);
	}

	/**
	 * 在不包含子节点模式下获取选择的节点
	 * 
	 * @return
	 */
	private TreeNode[] getSelectNodeOfNoSub() {
		ArrayList<TreeNode> arrayList = new ArrayList<TreeNode>();
		TreePath[] treePaths = getOrgTreePane().getSelectionModel().getSelectionPaths();
		if (treePaths == null) {
			return null;
		}

		for (TreePath treePath : treePaths) {
			TreeNode treeNode = (TreeNode) treePath.getLastPathComponent();
			arrayList.add(treeNode);
		}
		return arrayList.toArray(new TreeNode[0]);
	}

	/**
	 * 在包含全部子节点的模式下获取选择的节点
	 * 
	 * @return
	 */
	private Object[] getSelectNodeOfAll() {
		ArrayList<TreeNode> arrayList = new ArrayList<TreeNode>();
		TreePath[] treePaths = getOrgTreePane().getSelectionModel().getSelectionPaths();
		if (treePaths == null) {
			return null;
		}

		for (TreePath treePath : treePaths) {
			TreeNode treeNode = (TreeNode) treePath.getLastPathComponent();
			if (!arrayList.contains(treeNode))
				arrayList.add(treeNode);

			ArrayList<TreeNode> arrTempList = getAllNodes(treeNode);
			for (int j = 0; j < arrTempList.size(); j++) {
				TreeNode node = arrTempList.get(j);
				if (!arrayList.contains(node))
					arrayList.add(node);
			}
		}
		return arrayList.toArray(new TreeNode[0]);
	}

	/**
	 * 在包含直接节点的模式下获取选择的节点
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private TreeNode[] getSelectNodeOfSub() {
		ArrayList<TreeNode> arrayList = new ArrayList<TreeNode>();
		TreePath[] treePaths = getOrgTreePane().getSelectionModel().getSelectionPaths();
		if (treePaths == null) {
			return null;
		}
		for (TreePath treePath : treePaths) {
			TreeNode treeNode = (TreeNode) treePath.getLastPathComponent();
			if (!arrayList.contains(treeNode))
				arrayList.add(treeNode);
			if (!treeNode.isLeaf() && treeNode.getChildCount() >= 0) {
				for (Enumeration e = treeNode.children(); e.hasMoreElements();) {
					TreeNode childNode = (TreeNode) e.nextElement();
					if (!arrayList.contains(childNode))
						arrayList.add(childNode);
				}
			}
		}
		return arrayList.toArray(new TreeNode[0]);
	}

	/**
	 * 在包含末级节点的方式下获取选择的节点
	 * 
	 * @return
	 */
	private Object[] getSelectNodeOfLEAFSUB() {
		return getSelectNodeOfAll();
	}

	/**
	 * 获取指定节点和所有包含的节点
	 * 
	 * @param parentNode
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<TreeNode> getAllNodes(TreeNode parentNode) {
		ArrayList<TreeNode> arrayList = new ArrayList<TreeNode>();
		if (parentNode.getChildCount() >= 0) {
			for (Enumeration<TreeNode> e = parentNode.children(); e.hasMoreElements();) {
				TreeNode childNode = e.nextElement();
				if (childNode.isLeaf() == true) {
					arrayList.add(childNode);
				} else {
					// why need the logic
					if (getOrgType() == IUfoQueryCondVO.ORGTYPE_ALL) {
						arrayList.add(childNode);
					}
					ArrayList<TreeNode> arrTempList = getAllNodes(childNode);
					for (int i = 0; i < arrTempList.size(); i++) {
						arrayList.add(arrTempList.get(i));
					}
				}
			}
		}
		return arrayList;
	}

	/**
	 * 设置最小化按钮(外部注入)
	 * 
	 * @param miniAction
	 *            最小化按钮
	 */
	public void setMiniAction(Action miniAction) {
		leftMiniAction = miniAction;
		// Action[] actions = {miniAction};
		getTitlePanel().addAction2RightToolbar(leftMiniAction);
	}

	/**
	 * 适配报表组织体系多版本添加，得到快速查询面板的时间关键字的值，若没有时间关键字则返回当前的业务日期
	 * 若时间关键字值为空，返回当前的业务日期，在后台转换为所需时间，会计期间返回当前的会计区间，但是也需要在后台转换时间
	 * 
	 * @creator tianjlc at 2015-2-3 上午8:53:32
	 * @return
	 * @return String
	 */
	public String getKeyDate() {
		List<IUfoFilterItem> filterItems = queryHolder.getQuickQueryItem();
		String date = WorkbenchEnvironment.getInstance().getBusiDate().toString();
		for (IUfoFilterItem filterItem : filterItems) {
			if (filterItem instanceof AccDateFilterItem || filterItem instanceof NatDateFilterItem) {
				if (filterItem.getInputValue() != null && ((String) filterItem.getInputValue()).trim().length() < 1) {
					isDateTransform = true;
				} else {
					date = filterItem.getInputValue().toString();
					isDateTransform = true;
				}
			} else if (filterItem instanceof NatDateFilterItem) {
				if (filterItem.getInputValue() != null && ((String) filterItem.getInputValue()).trim().length() < 1) {
					isDateTransform = false;
				} else {
					isDateTransform = false;
					date =filterItem.getInputValue().toString();
				}
			}
		}
		return date;
	}

}