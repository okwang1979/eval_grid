package nc.ui.hbbb.quickquery.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import nc.bd.accperiod.AccperiodAccessor;
import nc.bd.accperiod.InvalidAccperiodExcetion;
import nc.bs.logging.Logger;
import nc.itf.hbbb.hbrepstru.HBRepStruUtil;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.pub.iufo.exception.UFOSrvException;
import nc.pubitf.accperiod.AccountCalendar;
import nc.ui.hbbb.common.schemequeryarea.HBBBSchemeQueryHelper;
import nc.ui.hbbb.quickquery.model.HBBBQueryHolder;
import nc.ui.hbbb.utils.HBBBAccPeriodUtil;
import nc.ui.hbbb.utils.HBBBUITimeGetUtil;
import nc.ui.hbbb.view.LayoutPanel;
import nc.ui.iufo.pub.UfoPublic;
import nc.ui.iufo.query.common.IUfoQueryExecutor;
import nc.ui.iufo.query.common.comp.IUfoOrgSelectedStrategyPanel;
import nc.ui.iufo.query.common.comp.OrgRepFilterPanel;
import nc.ui.iufo.query.common.event.IUfoQueryCondChangeListener;
import nc.ui.iufo.query.common.event.IUfoQueryHolder;
import nc.ui.iufo.query.common.filteritem.AccDateFilterItem;
import nc.ui.iufo.query.common.filteritem.IUfoFilterItem;
import nc.ui.iufo.query.common.filteritem.KeyFilterItem;
import nc.ui.iufo.query.common.filteritem.NatDateFilterItem;
import nc.ui.pub.beans.UILabel;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.beans.UIScrollPane;
import nc.ui.pub.beans.UISplitPane;
import nc.ui.pub.beans.ValueChangedEvent;
import nc.ui.pub.beans.ValueChangedListener;
import nc.ui.queryarea.UIStateChangedEvent;
import nc.ui.queryarea.UIStateChangedListener;
import nc.ui.queryarea.UIStatefulPanel;
import nc.ui.queryarea.component.ContainerMouseEnteredDetector;
import nc.ui.queryarea.state.AbsMinimizedUIState;
import nc.ui.queryarea.state.AbsRestoredUIState;
import nc.ui.queryarea.util.QueryAreaColor;
import nc.ui.queryarea.util.QueryAreaIcons;
import nc.ui.querytemplate.ICriteriaChangedListener;
import nc.ui.uif2.DefaultExceptionHanler;
import nc.util.hbbb.hbscheme.HBSchemeSrvUtils;
import nc.vo.bd.period.AccperiodVO;
import nc.vo.corg.ReportCombineStruMemberWithCodeNameVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.individual.HBBBIndividualUtil;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.query.IUfoDetailQueryCondVO;
import nc.vo.iufo.query.IUfoQueryCondVO;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.lang.UFDate;
import nc.vo.vorg.ReportCombineStruVersionVO;

import com.borland.jbcl.layout.VerticalFlowLayout;
import com.ufida.iufo.pub.tools.AppDebug;

/**
 * 快速查询面板
 * 
 * @version V6.1
 * @author litfb
 * @see nc.ui.iufo.query.common.area.IUfoQuickQueryArea
 */
public class HBBBQuickQueryArea extends UIStatefulPanel implements UIStateChangedListener, IUfoQueryCondChangeListener {
    private static final long serialVersionUID = -9166969927017138506L;

    private IUfoQueryExecutor queryExecutor;

    private UIScrollPane scrollPane;
    /** 标题Panel */
    private HBBBQSTitlePanel titlePanel;

    private UIPanel centerPanel;

    private UIPanel bottomPanel;

    private UIPanel buttonPanel;

    private BottomAction bottomAction;

    private IUfoQueryHolder queryHolder = null;

    private UISplitPane splitPane = null;

    private HBBBQueryOrgTree orgTreePane = null;

    private UIScrollPane scrollOrgTreePane = null;

    private Map<String, Integer> hashDivLocation = new HashMap<String, Integer>();

    private OrgRepFilterPanel orgRepFilterPanel = null;

    private boolean showRepSelectBtn = true;

    private IUfoOrgSelectedStrategyPanel orgSelectedStrategyPanel;

    private UIPanel scrollAndSelectedPanel = null;
    // 供初始化界面值时使用
    private List<IUfoFilterItem> itfFilterItem = null;
    /** 是否查询叶子节点 */
    private boolean isQueryLeaf = true;

    public HBBBQuickQueryArea(IUfoQueryHolder queryHolder, boolean showRepSelectBtn, boolean isQueryLeaf) {
        super();
        this.queryHolder = queryHolder;
        this.showRepSelectBtn = showRepSelectBtn;
        this.isQueryLeaf = isQueryLeaf;
        queryHolder.getQueryCondChangeHandler().addQueryCondChangeListener(this);
        this.uiState = new RestoredUIState();
        this.initUI();
        this.initListeners();
    }

    private void initUI() {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createLineBorder(QueryAreaColor.BORDER_COLOR));
        this.add(this.getTitlePanel(), BorderLayout.NORTH);
        this.add(this.getSplitPane(), BorderLayout.CENTER);
    }

    private void initListeners() {
        this.initMouseListener4ScrollPane(this.getScrollPane());
    }

    /**
     * 在光标移到JscrollPane面板时，设置JscrollPane的滚动条显示策略
     * 
     * @param c
     */
    private void initMouseListener4ScrollPane(final JScrollPane c) {
        new ContainerMouseEnteredDetector(c, new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                c.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                c.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                c.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                c.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            }
        });
    }

    public IUfoQueryCondVO genQueryCond(boolean withCheck) throws Exception {
        IUfoQueryCondVO queryCond = this.getQueryScheme(withCheck);
        // 设置组织节点选择策略
        int type = this.getOrgType();
        queryCond.setOrgType(type);
        // 当前选中组织
        String pk_org = getCurrSelectTreeNode();
        queryCond.setPk_mainOrg(pk_org);
        // 根据当前选中组织和组织节点选择策略获得所有选中组织
        String[] orgPKs = getSelectTreeNode();
        queryCond.setSelectedOrgPKs(orgPKs);
        return queryCond;
    }

    private IUfoQueryCondVO getQueryScheme(boolean withCheck) throws Exception {
		HBSchemeVO hbSchemeVO = ((HBBBQueryHolder) queryHolder).getSchemeVO();
		if(hbSchemeVO == null){
			throw new Exception(NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830006-0088"));
		}
        IUfoQueryCondVO queryCond = (IUfoQueryCondVO) this.queryHolder.getQueryCond().clone();
        String checkMessage = this.checkCondition(queryCond);
        if (withCheck && checkMessage != null) {
            throw new RuntimeException(checkMessage);
        }
        return queryCond;
    }

    private String checkCondition(IUfoQueryCondVO queryCond) {
        String[] strErrMsgs = this.queryHolder.getQueryCondChangeHandler().fireQueryCondSave(queryCond);
        if (strErrMsgs != null && strErrMsgs.length > 0) {
            StringBuffer bufMsg = new StringBuffer();
            for (String strErrMsg : strErrMsgs) {
                bufMsg.append(strErrMsg + "\r\n");
            }
            return bufMsg.toString().trim();
        }
        return null;
    }

    /**
     * 取得节点选择方式
     * 
     * @return
     */
    public int getOrgType() {
        return this.getOrgSelectedStrategyPanel().getOrgType();
    }

    public void doQuery() {
        try {
            IUfoQueryCondVO queryCond = this.genQueryCond(true);
            this.getQueryExecutor().doQuery(queryCond);
            this.getOrgTreePane().setDirectQuery(true);
        } catch (Exception e) {
            AppDebug.debug(e);
            this.getQueryHolder().getExceptionHanlder().handlerExeption(e);
        }
    }

    @Override
    public void onQueryCondChange(IUfoQueryCondVO oldQueryCond, IUfoQueryCondVO newQueryCond, Object eventSource) {
        KeyGroupVO keyGroup = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(newQueryCond.getKeyGroupPK());
        if (keyGroup == null) {
            // 移除监听 ，之前给此scrollPane 面板加上了ContainerMouseEnteredDetector 监听器。
            for (MouseListener mousListener : this.getScrollPane().getMouseListeners()) {
                this.getScrollPane().removeMouseListener(mousListener);
            }
            return;
        }

        final UIPanel panel = new UIPanel();
        QueryAreaColor.setBkgrdDefaultColor(panel);
        VerticalFlowLayout verticalFlowLayout = new VerticalFlowLayout();
        verticalFlowLayout.setVgap(5);
        verticalFlowLayout.setHgap(0);
        panel.setLayout(verticalFlowLayout);
        List<IUfoFilterItem> vFilterItem = this.queryHolder.getQuickQueryItem();
        this.setItfFilterItem(vFilterItem);
        
        // 获得合并方案
        HBSchemeVO hbSchemeVO = null;
        try {
        	if(queryHolder != null && queryHolder instanceof HBBBQueryHolder)
        		hbSchemeVO = ((HBBBQueryHolder)queryHolder).getSchemeVO();
        	else
        		hbSchemeVO = HBSchemeSrvUtils.getHBSchemeByHBSchemeId(newQueryCond.getPk_task());
        } catch (UFOSrvException e) {
            AppDebug.debug(e);
        }
        //设置关键字默认值
        setDefaultValue(vFilterItem,hbSchemeVO);
        
        IUfoQueryCondVO inputCond = this.queryHolder.getQueryCondEditHandler().getInputQueryCond(
                newQueryCond.getPk_querycond());
        if (inputCond != null) {
            newQueryCond.setDetailcond(((IUfoDetailQueryCondVO) inputCond.getDetailcond()).clone());
        }

        int row = vFilterItem.size() + (this.isShowRepSelectBtn()?1:0);
        LayoutPanel layoutPanel = new LayoutPanel(row, 2, false, new Dimension(120, 20));
        layoutPanel.setAlignInParent("center");
//        layoutPanel.setPreferredSize(new Dimension(240, 60));
        QueryAreaColor.setBkgrdDefaultColor(layoutPanel);

        int otherCompNum = 0;
        if (this.isShowRepSelectBtn()) {
        	otherCompNum = 1;
            UILabel label = new UILabel(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0361")
            /* @res "报表选择" */);
            layoutPanel.add(1, 1, "east", label);
            OrgRepFilterPanel orgRepFilterPane = this.getOrgRepFilterPanel();
            orgRepFilterPane.initUI();
            layoutPanel.add(1,2,this.getOrgRepFilterPanel());
            // 清空已经存在的数据
            this.getOrgRepFilterPanel().setRepTaskPKs(new String[2][]);
        }
        QueryAreaColor.setBkgrdDefaultColor(panel);

        // 报表合并体系版本
        ReportCombineStruVersionVO rcsvVO = null;

        for (int i=0;i<vFilterItem.size(); i++) {
        	IUfoFilterItem item = vFilterItem.get(i);
            UILabel lbl = new UILabel(item.getName());
            layoutPanel.add(i+1+otherCompNum, 1, "east", lbl);

            JComponent comp = item.getEditComponent();
            HBBBSchemeQueryHelper.setShowMustInputHint(comp);
            layoutPanel.add(i+1+otherCompNum, 2, comp);

//            if (KeyVO.MONTH_PK.equals(item.getKeyPK()) || KeyVO.YEAR_PK.equals(item.getKeyPK())) {
//                KeyWordUtil.setMonthRefValue((UIRefPane) comp);
//            }

            if (hbSchemeVO != null) {
                if (KeyVO.isTTimeKey(item.getKeyPK())) {
                    final UIRefPane refcom = (UIRefPane) comp;
                    final HBSchemeVO schemeVO = hbSchemeVO;
                    final String pk_key = item.getKeyPK();
                    String stdValue = HBBBUITimeGetUtil.getStdTimeFromRefPane((UIRefPane) comp);
                    refcom.addValueChangedListener(new ValueChangedListener() {

                        @Override
                        public void valueChanged(ValueChangedEvent event) {
                            if (schemeVO != null) {
                            	String time = HBBBUITimeGetUtil.getStdTimeFromRefPane(refcom);
                            	ReportCombineStruVersionVO rcsvVO = getRcsvVO(pk_key, time,
                                        schemeVO.getPk_repmanastru(), schemeVO.getPk_accperiodscheme());

                                // 默认最新版本
//                                if (rcsvVO == null || rcsvVO.getPk_vid() == null) {
//                                    rcsvVO = HBRepStruUtil.getLastVersion(schemeVO.getPk_repmanastru());
//                                }
                                if (rcsvVO != null) {
                                    queryHolder.getQueryCond().setPk_rms(rcsvVO.getPk_vid());
                                    try {
                                        getOrgTreePane().refreshByQueryCond(rcsvVO);
                                    } catch (Exception e) {
                                        AppDebug.debug(e);
                                    }
                                }
                            }
                        }
                    });

                    rcsvVO = getRcsvVO(item.getKeyPK(), stdValue, hbSchemeVO.getPk_repmanastru(),
                            hbSchemeVO.getPk_accperiodscheme());
                }
                
                if (rcsvVO != null && rcsvVO.getPk_vid() != null) {
                    this.queryHolder.getQueryCond().setPk_rms(rcsvVO.getPk_vid());
                }
            }
        }
        panel.add(layoutPanel);
        QueryAreaColor.setBkgrdDefaultColor(panel);
        this.scrollPane.setViewportView(panel);
        Dimension size = panel.getPreferredSize();

        this.splitPane.setDividerLocation(this.getDivideLocation(size));
        // centerPanel = null;
        this.getCenterPanel().setMinimumSize(new Dimension(0, this.splitPane.getDividerLocation()));

        this.getCenterPanel().revalidate();
        this.getCenterPanel().repaint();
        this.getBottomPanel().remove(this.getButtonPanel());

        // getBottomPanel().remove(getScrollOrgTreePane());
        this.getBottomPanel().remove(this.getScrollAndSelectedPanel());
        if (keyGroup.getKeyByKeyPk(KeyVO.CORP_PK) != null) {
            this.addSelectedPanel();

            this.getBottomPanel().add(this.getScrollAndSelectedPanel(), BorderLayout.CENTER);

            this.getBottomPanel().add(this.getButtonPanel(), BorderLayout.SOUTH);
            try {
                getOrgTreePane().refreshByQueryCond(rcsvVO);
            } catch (Exception te) {
                com.ufida.iufo.pub.tools.AppDebug.debug(te);
            }
        } else {
            this.getBottomPanel().add(this.getButtonPanel(), BorderLayout.NORTH);
        }
        this.scrollPane.validate();
        this.scrollPane.repaint();
        this.getBottomPanel().revalidate();
        this.getBottomPanel().repaint();
    }
    
    private void setDefaultValue(List<IUfoFilterItem> vFilterItem,HBSchemeVO hbSchemeVO) {
		if(vFilterItem != null) {
			for (IUfoFilterItem iUfoFilterItem : vFilterItem) {
				if(iUfoFilterItem instanceof NatDateFilterItem) {
					UIRefPane editComponent = (UIRefPane) iUfoFilterItem.getEditComponent();
					editComponent.setValue(new UFDate().toLocalString());
				}
                else if(iUfoFilterItem instanceof AccDateFilterItem) {
                	
                	String timeValue = null;
                	UIRefPane refPane = (UIRefPane)iUfoFilterItem.getEditComponent();
//                	AbstractRefModel refModel = refPane.getRefModel();
                	String pk_accScheme = hbSchemeVO.getPk_accperiodscheme();
//                	if(refModel instanceof AccperiodRefModel){
//                		pk_accScheme = ((AccperiodRefModel)refModel).getDefaultpk_accperiodscheme();
//                	}
                	
                	String pk_timekey = ((AccDateFilterItem)iUfoFilterItem).getKeyPK();
                	timeValue = HBBBAccPeriodUtil.getDefaultAccPeriod(pk_accScheme, pk_timekey);
                	if(KeyVO.ACC_HALFYEAR_PK.equals(pk_timekey)) {
                		
                		AccountCalendar calendar = AccountCalendar.getInstanceByPeriodScheme(pk_accScheme);
    					String strYear = timeValue.substring(0,4);
    					String strHalfYear = timeValue.substring(5,7);
    					try {
							calendar.setYearAndHalfyear(strYear, Integer.parseInt(strHalfYear));
							String pkAccHalfyear = calendar.getHalfYearVO().getPk_acchalfyear();
	    					refPane.setPK(pkAccHalfyear);
						} catch (NumberFormatException e) {
							Logger.error(e.getMessage(),e);
						} catch (InvalidAccperiodExcetion e) {
							Logger.error(e.getMessage(),e);
						}
                	}
                	else if(KeyVO.ACC_YEAR_PK.equals(pk_timekey)) {
                		try{
            				AccperiodAccessor instance=AccperiodAccessor.getInstance();
            				AccperiodVO period=instance.queryAccperiodVOByYear(pk_accScheme, timeValue);
            				if (period!=null)
            					refPane.setPK(period.getPk_accperiod());
            			}catch(Exception e){
            				Logger.error(e.getMessage(),e);
            			}
                	}
                	else {
                		refPane.setPK(timeValue);
                	}
                }
                else if(iUfoFilterItem instanceof KeyFilterItem &&((KeyFilterItem)iUfoFilterItem).getKeyPK().equals(KeyVO.COIN_PK)) {
                	String defaultCurrencyPK = HBBBIndividualUtil.getDefCurrencyType();
                	((UIRefPane)iUfoFilterItem.getEditComponent()).setPK(defaultCurrencyPK);
                }
			}
		}
	}

    /**
     * 获得报表合并体系版本
     * 
     * @param pk_key
     * @param keyValue
     * @param pk_rcs
     * @param pk_accperiodscheme
     * @return
     */
    private static ReportCombineStruVersionVO getRcsvVO(String pk_key, String keyValue, String pk_rcs,
            String pk_accperiodscheme) {
        ReportCombineStruVersionVO rcsvVO = null;
        if (KeyVO.isTimeKey(pk_key)) {
            rcsvVO = HBRepStruUtil.getHBStruVersionVO(keyValue, pk_rcs);
        } else if (KeyVO.isAccPeriodKey(pk_key)) {
            rcsvVO = HBRepStruUtil.getHBStruVersionVO(pk_accperiodscheme, pk_key, keyValue, pk_rcs);
        }
        return rcsvVO;
    }

    /**
     * 添加 节点选择面板
     */
    private void addSelectedPanel() {
        this.getScrollAndSelectedPanel().add(this.getOrgSelectedStrategyPanel(), BorderLayout.SOUTH);
        this.getScrollAndSelectedPanel().setBorder(BorderFactory.createLineBorder(QueryAreaColor.BORDER_COLOR));
        this.getScrollAndSelectedPanel().setBackground(Color.WHITE);
    }

    @Override
    public void onQueryCondUpdate(IUfoQueryCondVO oldQueryCond, IUfoQueryCondVO newQueryCond, Object eventSource) {

    }

    @Override
    public void onQueryCondClear(Object eventSource) {

    }

    @Override
    public String[] onQueryCondSave(IUfoQueryCondVO queryCond) {
    	// @edit by zhoushuang at 2015-6-11,下午2:08:16 校验合并方案(dongjch的方法有问题，不要根据树的选择路径判断)
    	HBSchemeVO hbSchemeVO = ((HBBBQueryHolder) queryHolder).getSchemeVO();
		if(hbSchemeVO == null){
			 return new String[] { nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830002-0024")/* @res "请选择合并方案!" */};
		}

        String strSelOrgPK = null;
        TreePath treePath = this.getOrgTreePane().getSelectionPath();
        if (treePath != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
            if (node.getUserObject() instanceof ReportCombineStruMemberWithCodeNameVO) {
                ReportCombineStruMemberWithCodeNameVO mem = (ReportCombineStruMemberWithCodeNameVO) node
                        .getUserObject();
                strSelOrgPK = mem.getPk_org();
            }
        }

        if (UfoPublic.stringIsNull(strSelOrgPK)) {
            return new String[] { nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0362")
            /* @res "请选择组织树上的一个节点" */};
        }
        queryCond.setKeyVal(KeyVO.CORP_PK, strSelOrgPK);

        return null;
    }

    @Override
    public void stateChanged(UIStateChangedEvent event) {
        // 不是自己发出的事件并且有UI展开了，则本界面最小化
        if (event.getSource() != this) {
            if (event.getUIStateType() == UIStateChangedEvent.EXPAND) {
                this.minimizeUI();
                this.fireUIStateChanged(UIStateChangedEvent.COLLAPSE);
            } else if (event.getUIStateType() == UIStateChangedEvent.COLLAPSE_SELF) {
                this.restoreUI();
                this.fireUIStateChanged(UIStateChangedEvent.EXPAND);
            }
        }
    }

    /**
     * 生成查询方案名称(即快速查询)
     */
    private String createQuerySchemeName() {
        return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0363")/* @res "快速查询" */;
    }

    public void clearQuickQueryArea() {
        this.getBottomPanel().removeAll();
        this.getBottomPanel().add(this.getButtonPanel(), BorderLayout.NORTH);
        // @edit by wuyongc at 2011-6-18,下午03:00:58
        // 移除监听 ，之前给此scrollPane 面板加上了ContainerMouseEnteredDetector 监听器。
        for (MouseListener mousListener : this.getScrollPane().getMouseListeners()) {
            this.getScrollPane().removeMouseListener(mousListener);
        }

        this.getScrollPane().removeAll();
        this.getSplitPane().setDividerLocation(0);
        this.updateUI();
    }

    public void clearData() {
        this.queryHolder.getQueryCondChangeHandler().fireQueryCondClear(this);
    }

    public void addCriteriaChangedListener(ICriteriaChangedListener listener) {
        this.listenerList.remove(ICriteriaChangedListener.class, listener);
        this.listenerList.add(ICriteriaChangedListener.class, listener);
    }

    public JComponent getJComponent() {
        return this;
    }

    @Override
    public Dimension getMinimumSize() {
        return this.getTitlePanel().getPreferredSize();
    }

    /**
     * 获得当前选中节点
     * 
     * @return
     */
    private String getCurrSelectTreeNode() {
        TreeNode node = null;
        TreePath[] treePaths = getOrgTreePane().getSelectionModel().getSelectionPaths();
        if (treePaths != null && treePaths.length > 0) {
            node = (TreeNode) treePaths[0].getLastPathComponent();
        }
        String pk_org = null;
        if (node != null && node instanceof DefaultMutableTreeNode) {
            Object userObj = ((DefaultMutableTreeNode) node).getUserObject();
            if (userObj instanceof ReportCombineStruMemberWithCodeNameVO) {
                pk_org = ((ReportCombineStruMemberWithCodeNameVO) userObj).getPk_org();
            }
        }
        return pk_org;
    }

    /**
     * 根据当前选中的节点和节点选择策略得到的组织PK
     * 
     * @return
     */
    private String[] getSelectTreeNode() {
        // 节点选择策略
        int orgType = getOrgType();
        Object[] objects = null;
        switch (orgType) {
        // 不包含
        case IUfoQueryCondVO.ORGTYPE_SELF:
            objects = getSelectNodeOfNoSub();
            break;
        // 所有下级
        case IUfoQueryCondVO.ORGTYPE_ALL:
            objects = getSelectNodeOfAll();
            break;
        // 直接下级
        case IUfoQueryCondVO.ORGTYPE_DIRECT:
            objects = getSelectNodeOfSub();
            break;
        // 末级
        case IUfoQueryCondVO.ORGTYPE_LEAF:
            objects = getSelectNodeOfLeafSub();
            break;
        default:
        	objects = new Object[0];
        	break;
        }
        List<String> orgPkList = new ArrayList<String>();
        Object userObj = null;
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof DefaultMutableTreeNode) {
                userObj = ((DefaultMutableTreeNode) objects[i]).getUserObject();
                if (userObj instanceof ReportCombineStruMemberWithCodeNameVO) {
                    orgPkList.add(((ReportCombineStruMemberWithCodeNameVO) userObj).getPk_org());
                }
            }
        }
        return orgPkList.toArray(new String[orgPkList.size()]);
    }

    /**
     * 在"不包含"子节点模式下获取选择的节点
     * 
     * @return
     */
    private TreeNode[] getSelectNodeOfNoSub() {
        List<TreeNode> nodeList = new ArrayList<TreeNode>();
        TreePath[] treePaths = getOrgTreePane().getSelectionModel().getSelectionPaths();
        if (treePaths != null) {
            for (TreePath treePath : treePaths) {
                TreeNode treeNode = (TreeNode) treePath.getLastPathComponent();
                // 已选节点
                if (isQueryLeaf() || !treeNode.isLeaf()) {
                    nodeList.add(treeNode);
                }
            }
        }
        return nodeList.toArray(new TreeNode[nodeList.size()]);
    }

    /**
     * 在"所有下级"子节点模式下获取选择的节点
     * 
     * @return
     */
    private Object[] getSelectNodeOfAll() {
        Set<TreeNode> nodeSet = new HashSet<TreeNode>();
        TreePath[] treePaths = getOrgTreePane().getSelectionModel().getSelectionPaths();
        if (treePaths != null) {
            for (TreePath treePath : treePaths) {
                TreeNode treeNode = (TreeNode) treePath.getLastPathComponent();
                // 已选节点
                if (isQueryLeaf() || !treeNode.isLeaf()) {
                    nodeSet.add(treeNode);
                }
                // 已选节点所有下级
                List<TreeNode> subNodeList = getAllNodes(treeNode);
                nodeSet.addAll(subNodeList);
            }
        }
        return nodeSet.toArray(new TreeNode[nodeSet.size()]);
    }

    /**
     * 在"直接下级"子节点模式下获取选择的节点
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private TreeNode[] getSelectNodeOfSub() {
        Set<TreeNode> nodeSet = new HashSet<TreeNode>();
        TreePath[] treePaths = getOrgTreePane().getSelectionModel().getSelectionPaths();
        if (treePaths != null) {
            for (TreePath treePath : treePaths) {
                TreeNode treeNode = (TreeNode) treePath.getLastPathComponent();
                // 已选节点
                if (isQueryLeaf() || !treeNode.isLeaf()) {
                    nodeSet.add(treeNode);
                }
                // 已选节点直接下级
                if (!treeNode.isLeaf() && treeNode.getChildCount() >= 0) {
                    Enumeration<TreeNode> e = treeNode.children();
                    while (e.hasMoreElements()) {
                        TreeNode childNode = e.nextElement();
                        if (isQueryLeaf() || !childNode.isLeaf()) {
                            nodeSet.add(childNode);
                        }
                    }
                }
            }
        }
        return nodeSet.toArray(new TreeNode[nodeSet.size()]);
    }

    /**
     * 在"末级"子节点模式下获取选择的节点
     * 
     * @return
     */
    private Object[] getSelectNodeOfLeafSub() {
        Set<TreeNode> nodeSet = new HashSet<TreeNode>();
        TreePath[] treePaths = getOrgTreePane().getSelectionModel().getSelectionPaths();
        if (treePaths != null) {
            for (TreePath treePath : treePaths) {
                TreeNode treeNode = (TreeNode) treePath.getLastPathComponent();
                // 选中节点
                if (isQueryLeaf() || !treeNode.isLeaf()) {
                    nodeSet.add(treeNode);
                }
                if (isQueryLeaf()) {
                    List<TreeNode> subNodeList = getAllLeafNodes(treeNode);
                    nodeSet.addAll(subNodeList);
                }
            }
        }
        return nodeSet.toArray(new TreeNode[nodeSet.size()]);
    }

    /**
     * 获取所有包含的子节点
     * 
     * @param parentNode
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<TreeNode> getAllNodes(TreeNode parentNode) {
        List<TreeNode> nodeList = new ArrayList<TreeNode>();
        if (parentNode.getChildCount() >= 0) {
            Enumeration<TreeNode> e = parentNode.children();
            while (e.hasMoreElements()) {
                TreeNode childNode = e.nextElement();
                if (childNode.isLeaf()) {
                    if (isQueryLeaf()) {
                        nodeList.add(childNode);
                    }
                } else {
                    nodeList.add(childNode);
                    // 迭代寻找子节点
                    List<TreeNode> subNodeList = getAllNodes(childNode);
                    nodeList.addAll(subNodeList);
                }
            }
        }
        return nodeList;
    }

    /**
     * 获取包含的叶子节点
     * 
     * @param parentNode
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<TreeNode> getAllLeafNodes(TreeNode parentNode) {
        List<TreeNode> nodeList = new ArrayList<TreeNode>();
        if (parentNode.getChildCount() >= 0) {
            Enumeration<TreeNode> e = parentNode.children();
            while (e.hasMoreElements()) {
                TreeNode childNode = e.nextElement();
                // 叶子节点
                if (childNode.isLeaf()) {
                    nodeList.add(childNode);
                } else {
                    // 非叶子节点 迭代寻找子节点叶子
                    List<TreeNode> subNodeList = getAllLeafNodes(childNode);
                    nodeList.addAll(subNodeList);
                }
            }
        }
        return nodeList;
    }

    private int getDivideLocation(Dimension size) {
        int divideLocation = -1;
        if (this.queryHolder.getQueryCond() != null && this.queryHolder.getQueryCond().getPk_querycond() != null
                && this.hashDivLocation.get(this.queryHolder.getQueryCond().getPk_querycond()) != null) {
            divideLocation = this.hashDivLocation.get(this.queryHolder.getQueryCond().getPk_querycond());
        }

        List<IUfoFilterItem> vFilterItem = this.queryHolder.getQuickQueryItem();
        int divideLocation1 = vFilterItem.size() > 0 ? (int) size.getHeight() + 5 : 0;
        if (divideLocation >= (int) size.getHeight() + 10) {
            divideLocation = divideLocation1;
        } else if (divideLocation <= 5) {
            if (this.getSplitPane().getSize().getHeight() > 100) {
                Dimension parentSize = this.getSplitPane().getSize();
                int iMaxDivide = (int) parentSize.getHeight() - 340;
                divideLocation = Math.min(iMaxDivide, divideLocation1);
            } else {
                divideLocation = divideLocation1;
            }
        }

        return divideLocation;
    }

    @SuppressWarnings("serial")
    class BottomAction extends AbstractAction {
        public BottomAction() {
            this.putValue(Action.NAME, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0364")
            /* @res "置底" */);
            this.putValue(Action.SMALL_ICON, QueryAreaIcons.BOTTOM_ICON);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (HBBBQuickQueryArea.this.isMinimized()) {
                HBBBQuickQueryArea.this.restoreUI();
            } else {
                HBBBQuickQueryArea.this.minimizeUI();
            }
        }
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
            HBBBQuickQueryArea quickQA = (HBBBQuickQueryArea) panel;
            quickQA.getBottomAction().putValue(Action.NAME,
                    nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0364")/* @res "置底" */);
            quickQA.getBottomAction().putValue(Action.SMALL_ICON, QueryAreaIcons.BOTTOM_ICON);
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
            HBBBQuickQueryArea quickQA = (HBBBQuickQueryArea) panel;
            quickQA.getBottomAction().putValue(Action.NAME,
                    nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0365")/* @res "恢复" */);
            quickQA.getBottomAction().putValue(Action.SMALL_ICON, QueryAreaIcons.TOP_ICON);
            quickQA.setUIState(new MinimizedUIState());
            quickQA.fireUIStateChanged(UIStateChangedEvent.COLLAPSE);
        }
    }

    @SuppressWarnings("serial")
    private class MySplitPane extends UISplitPane {
        MySplitPane(int direction) {
            super(direction);
        }

        @Override
        public void setDividerLocation(int location) {
            super.setDividerLocation(location);
            HBBBQuickQueryArea.this.saveDivideLocation(location);
        }
    }

    private void saveDivideLocation(int location) {
        if (this.queryHolder.getQueryCond() != null && this.queryHolder.getQueryCond().getPk_querycond() != null) {
            this.hashDivLocation.put(this.queryHolder.getQueryCond().getPk_querycond(), location);
        }
    }

    public boolean isMinimized() {
        return this.getUIState() instanceof MinimizedUIState;
    }

    public IUfoQueryExecutor getQueryExecutor() {
        return queryExecutor;
    }

    public void setQueryExecutor(IUfoQueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
    }

    public UIScrollPane getScrollPane() {
        if (scrollPane == null) {
            scrollPane = new UIScrollPane(new UIPanel(), ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        }
        return scrollPane;
    }

    public void setScrollPane(UIScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    public HBBBQSTitlePanel getTitlePanel() {
        if (titlePanel == null) {
            titlePanel = new HBBBQSTitlePanel(createQuerySchemeName());
        }
        return titlePanel;
    }

    public void setTitlePanel(HBBBQSTitlePanel titlePanel) {
        this.titlePanel = titlePanel;
    }

    public UIPanel getCenterPanel() {
        if (centerPanel == null) {
            centerPanel = new UIPanel();
            QueryAreaColor.setBkgrdDefaultColor(this.centerPanel);
            centerPanel.setPreferredSize(new Dimension((int) this.getScrollPane().getPreferredSize().getWidth() + 40,
                    (365 + 40)));
            centerPanel.setLayout(new BorderLayout());
            centerPanel.add(this.getScrollPane(), BorderLayout.CENTER);
        }
        return centerPanel;
    }

    public void setCenterPanel(UIPanel centerPanel) {
        this.centerPanel = centerPanel;
    }

    public UIPanel getBottomPanel() {
        if (bottomPanel == null) {
            bottomPanel = new UIPanel();
            bottomPanel.setLayout(new BorderLayout());
            bottomPanel.add(this.getButtonPanel(), BorderLayout.NORTH);
            bottomPanel.setBackground(Color.WHITE);
        }
        return bottomPanel;
    }

    public void setBottomPanel(UIPanel bottomPanel) {
        this.bottomPanel = bottomPanel;
    }

    public UIPanel getButtonPanel() {
        if (buttonPanel == null) {
            // 根据是否有单位关键字来判断是否显示树节点选择策略
            KeyGroupVO keyGroup = UFOCacheManager.getSingleton().getKeyGroupCache()
                    .getByPK(this.queryHolder.getQueryCond().getKeyGroupPK());
            if (keyGroup != null && keyGroup.getKeyByKeyPk(KeyVO.CORP_PK) != null) {
                buttonPanel = new HBBBQuickQueryAreaButtonPanel(this, false, this.isShowRepSelectBtn());
            } else {
                buttonPanel = new HBBBQuickQueryAreaButtonPanel(this, false, this.isShowRepSelectBtn());
            }
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
            QueryAreaColor.setBkgrdDefaultColor(this.buttonPanel);
        }
        return buttonPanel;
    }

    public void setButtonPanel(UIPanel buttonPanel) {
        this.buttonPanel = buttonPanel;
    }

    public BottomAction getBottomAction() {
        if (bottomAction == null) {
            bottomAction = new BottomAction();
        }
        return bottomAction;
    }

    public void setBottomAction(BottomAction bottomAction) {
        this.bottomAction = bottomAction;
    }

    public void setBottomActionEnabled(boolean enabled) {
        this.getBottomAction().setEnabled(enabled);
    }

    public IUfoQueryHolder getQueryHolder() {
        return queryHolder;
    }

    public void setQueryHolder(IUfoQueryHolder queryHolder) {
        this.queryHolder = queryHolder;
    }

    public UISplitPane getSplitPane() {
        if (splitPane == null) {
            splitPane = new MySplitPane(JSplitPane.VERTICAL_SPLIT);
            ((BasicSplitPaneUI) this.splitPane.getUI()).getDivider().setBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE));

            splitPane.add(this.getCenterPanel());
            splitPane.add(this.getBottomPanel());
            splitPane.setDividerLocation(0);
        }
        return splitPane;
    }

    public void setSplitPane(UISplitPane splitPane) {
        this.splitPane = splitPane;
    }

    public HBBBQueryOrgTree getOrgTreePane() {
        if (orgTreePane == null) {
            DefaultExceptionHanler errorHanler = (DefaultExceptionHanler) getQueryHolder().getExceptionHanlder();
            orgTreePane = new HBBBQueryOrgTree(errorHanler.getContext());
            orgTreePane.setBorder(BorderFactory.createEmptyBorder(3, 2, 1, 1));
            QueryAreaColor.setBkgrdDefaultColor(orgTreePane);
        }
        return orgTreePane;
    }

    public void setOrgTreePane(HBBBQueryOrgTree orgTreePane) {
        this.orgTreePane = orgTreePane;
    }

    public UIScrollPane getScrollOrgTreePane() {
        if (scrollOrgTreePane == null) {
            UIPanel pal = new UIPanel();
            pal.setLayout(new BorderLayout());
            pal.add(new HBBBOrgBuildTitlePanel(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0",
                    "01830003-0094")/* @res "报表合并体系" */), BorderLayout.NORTH);
            pal.add(getOrgTreePane(), BorderLayout.CENTER);
            QueryAreaColor.setBkgrdDefaultColor(pal);
            scrollOrgTreePane = new UIScrollPane(pal, UIScrollPane.VERTICAL_SCROLLBAR_NEVER,
                    UIScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            initMouseListener4ScrollPane(scrollOrgTreePane);
            scrollOrgTreePane.setBackground(Color.WHITE);
        }
        return scrollOrgTreePane;
    }

    public void setScrollOrgTreePane(UIScrollPane scrollOrgTreePane) {
        this.scrollOrgTreePane = scrollOrgTreePane;
    }

    public Map<String, Integer> getHashDivLocation() {
        return hashDivLocation;
    }

    public void setHashDivLocation(Map<String, Integer> hashDivLocation) {
        this.hashDivLocation = hashDivLocation;
    }

    public OrgRepFilterPanel getOrgRepFilterPanel() {
        if (orgRepFilterPanel == null) {
            orgRepFilterPanel = new OrgRepFilterPanel(this.queryHolder.getLoginContext());
            orgRepFilterPanel.setQueryAction(this.queryHolder.getQueryAction());
        }
        return orgRepFilterPanel;
    }

    public void setOrgRepFilterPanel(OrgRepFilterPanel orgRepFilterPanel) {
        this.orgRepFilterPanel = orgRepFilterPanel;
    }

    public boolean isShowRepSelectBtn() {
        return showRepSelectBtn;
    }

    public void setShowRepSelectBtn(boolean showRepSelectBtn) {
        this.showRepSelectBtn = showRepSelectBtn;
    }

    public IUfoOrgSelectedStrategyPanel getOrgSelectedStrategyPanel() {
        if (orgSelectedStrategyPanel == null) {
            orgSelectedStrategyPanel = new IUfoOrgSelectedStrategyPanel();
            orgSelectedStrategyPanel.setBackground(Color.WHITE);
        }
        return orgSelectedStrategyPanel;
    }

    public void setOrgSelectedStrategyPanel(IUfoOrgSelectedStrategyPanel orgSelectedStrategyPanel) {
        this.orgSelectedStrategyPanel = orgSelectedStrategyPanel;
    }

    public UIPanel getScrollAndSelectedPanel() {
        if (scrollAndSelectedPanel == null) {
            scrollAndSelectedPanel = new UIPanel(new BorderLayout());
            scrollAndSelectedPanel.add(this.getScrollOrgTreePane(), BorderLayout.CENTER);
        }
        return scrollAndSelectedPanel;
    }

    public void setScrollAndSelectedPanel(UIPanel scrollAndSelectedPanel) {
        this.scrollAndSelectedPanel = scrollAndSelectedPanel;
    }

    public List<IUfoFilterItem> getItfFilterItem() {
        return itfFilterItem;
    }

    public void setItfFilterItem(List<IUfoFilterItem> itfFilterItem) {
        this.itfFilterItem = itfFilterItem;
    }

    public boolean isQueryLeaf() {
        return isQueryLeaf;
    }

    public void setQueryLeaf(boolean isQueryLeaf) {
        this.isQueryLeaf = isQueryLeaf;
    }

}
