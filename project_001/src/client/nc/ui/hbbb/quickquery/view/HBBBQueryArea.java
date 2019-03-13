package nc.ui.hbbb.quickquery.view;

import java.awt.BorderLayout;
import java.awt.Toolkit;

import javax.swing.Action;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import nc.ui.hbbb.quickquery.model.HBBBQueryHolder;
import nc.ui.iufo.query.common.IUfoQueryExecutor;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UISplitPane;
import nc.ui.queryarea.QueryAreaPreferences;
import nc.ui.queryarea.UIStateChangedEvent;
import nc.ui.queryarea.UIStateChangedListener;
import nc.vo.iufo.query.IUfoQueryCondVO;

/**
 * 查询区
 * 
 * @version V6.1
 * @author litfb
 * @see nc.ui.iufo.query.common.area.IUfoQueryArea
 */
public class HBBBQueryArea extends UIPanel {

    private static final long serialVersionUID = -8299110555818770425L;
    /** 查询执行器 */
    private IUfoQueryExecutor queryExecutor;

    private UISplitPane splitPane;
    /** 查询方案区 */
    private HBBBQuerySchemeArea querySchemeArea;
    /** 快速查询区 */
    private HBBBQuickQueryArea quickQueryArea;

    private HBBBQueryHolder queryHolder = null;

    private boolean showRepSelectBtn = true;
    /** 是否查询叶子节点组织 */
    private boolean isQueryLeaf = true;

    public static final int SCREEN_WIDTH_1024 = 1024;

    private int splitDividateLocation = -1;

    /**
     * 查询对话框对象
     * 
     * @param queryHolder
     * @param showRepSelectBtn
     * @param isQueryLeaf
     */
    public HBBBQueryArea(HBBBQueryHolder queryHolder, boolean showRepSelectBtn, boolean isQueryLeaf) {
        super();
        this.queryHolder = queryHolder;
        this.showRepSelectBtn = showRepSelectBtn;
        this.isQueryLeaf = isQueryLeaf;
        initUI();
        initListeners();
    }

    private void initUI() {
        this.setLayout(new BorderLayout());
        this.add(getSplitPane());
    }

    private UISplitPane getSplitPane() {
        if (splitPane == null) {
            splitPane = new UISplitPane(UISplitPane.VERTICAL_SPLIT);
            splitPane.setDividerSize(2);
            splitPane.setBorder(null);
            (((BasicSplitPaneUI) splitPane.getUI()).getDivider()).setBorder(null);
            getQuickQueryArea();

            // splitPane.add(getQuerySchemeArea());
            splitPane.add(getQuickQueryArea());
            getQuerySchemeArea().setQuickQueryArea(getQuickQueryArea());
            splitPane.setDividerLocation(calculateDividerLocation4MinQuerySchemeArea());
        }
        return splitPane;
    }

    private void initListeners() {
        initListeners4QuerySchemeArea();
        initListeners4QuickQueryArea();
    }

    private void initListeners4QuerySchemeArea() {
        // 查询方案区 和 快速查询区相互监听UI状态变化
        getQuerySchemeArea().addUIStateChangedListener(getQuickQueryArea());
    }

    private void initListeners4QuickQueryArea() {
        // 查询方案区 和 快速查询区相互监听UI状态变化
        getQuickQueryArea().addUIStateChangedListener(getQuerySchemeArea());

        // 查询区监听快查区的UI状态变更：根据快查区的状态调整SplitPane分隔条的位置
        getQuickQueryArea().addUIStateChangedListener(new UIStateChangedListener() {
            @Override
            public void stateChanged(UIStateChangedEvent event) {
                switch (event.getUIStateType()) {
                case UIStateChangedEvent.EXPAND: {// 快查区展开了
                    if (splitDividateLocation <= 0)
                        getSplitPane().setDividerLocation(calculateDividerLocation4MinQuerySchemeArea());
                    else
                        getSplitPane().setDividerLocation(splitDividateLocation);
                    break;
                }
                case UIStateChangedEvent.COLLAPSE: {// 快查区收缩了
                    splitDividateLocation = getSplitPane().getDividerLocation();
                    getSplitPane().setDividerLocation(calculateDividerLocation4MinQuickQueryArea());
                    break;
                }
                }
            }
        });
    }

    private int calculateDividerLocation4MinQuerySchemeArea() {
        return isScreenWidth1024() || isQuerySchemeAreaEmpty() ? getQuerySchemeAreaMinHeight()
                : getQuerySchemeAreaDefaultHeight();
    }

    private int calculateDividerLocation4MinQuickQueryArea() {
        return getSplitPane().getHeight() - getQuickQueryAreaMinHeight() - getSplitPane().getDividerSize();
    }

    private int getQuerySchemeAreaMinHeight() {
        return (int) getQuerySchemeArea().getMinimumSize().getHeight();
    }

    private int getQuerySchemeAreaDefaultHeight() {
        return getQuerySchemeArea().getDefaultHeight();
    }

    private int getQuickQueryAreaMinHeight() {
        return (int) getQuickQueryArea().getMinimumSize().getHeight();
    }

    private boolean isQuerySchemeAreaEmpty() {
        return getQuerySchemeArea().isQuerySchemesEmpty();
    }

    private HBBBQuerySchemeArea getQuerySchemeArea() {
        if (querySchemeArea == null) {
            querySchemeArea = new HBBBQuerySchemeArea(queryHolder);
        }
        return querySchemeArea;
    }

    public HBBBQuickQueryArea getQuickQueryArea() {
        if (quickQueryArea == null) {
            quickQueryArea = new HBBBQuickQueryArea(queryHolder, isShowRepSelectBtn(), isQueryLeaf());
        }
        return quickQueryArea;
    }

    /**
     * 设置查询执行器
     * 
     * @param executor
     *            查询执行器
     */
    public void setQueryExecutor(IUfoQueryExecutor executor) {
        this.queryExecutor = executor;
        getQuerySchemeArea().setQueryExecutor(executor);
        getQuickQueryArea().setQueryExecutor(executor);

        IUfoQueryCondVO queryCond = (IUfoQueryCondVO) queryHolder.getQueryCond().clone();
        // 初始化界面的时候就应该设置QueryCond 到执行器中.
        getQueryExecutor().setQueryCond(queryCond);
    }

    /**
     * 返回查询执行器
     */
    public IUfoQueryExecutor getQueryExecutor() {
        return queryExecutor;
    }

    /**
     * 设置最小化按钮(由QueryAreaShell外部注入)
     * 
     * @param miniAction
     *            最小化按钮
     */
    public void setMiniAction(Action miniAction) {
        getQuerySchemeArea().setMiniAction(miniAction);
    }

    /**
     * 返回查询区个人偏好(提供给QueryAreaShell)
     */
    public QueryAreaPreferences getPreferences() {
        QueryAreaPreferences preferences = new QueryAreaPreferences();
        preferences.setQuickAreaMinimized(getQuickQueryArea().isMinimized());
        preferences.setSplitPaneDividerLocation(getSplitPane().getDividerLocation());
        return preferences;
    }

    /**
     * 设置查询区个人偏好(由QueryAreaShell外部注入)
     */
    public void setPreferences(QueryAreaPreferences preferences) {
        if (preferences != null) {
            boolean isQAMinimized = preferences.isQuickAreaMinimized();
            if (isQAMinimized) {
                getQuickQueryArea().minimizeUI();
            }
            getSplitPane().setDividerLocation(preferences.getSplitPaneDividerLocation());
        }
    }

    private boolean isScreenWidth1024() {
        return Toolkit.getDefaultToolkit().getScreenSize().width <= SCREEN_WIDTH_1024;
    }

    public boolean isShowRepSelectBtn() {
        return showRepSelectBtn;
    }

    public void setShowRepSelectBtn(boolean showRepSelectBtn) {
        this.showRepSelectBtn = showRepSelectBtn;
    }

    public boolean isQueryLeaf() {
        return isQueryLeaf;
    }

    public void setQueryLeaf(boolean isQueryLeaf) {
        this.isQueryLeaf = isQueryLeaf;
    }

}
