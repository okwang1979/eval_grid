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
 * ��ѯ��
 * 
 * @version V6.1
 * @author litfb
 * @see nc.ui.iufo.query.common.area.IUfoQueryArea
 */
public class HBBBQueryArea extends UIPanel {

    private static final long serialVersionUID = -8299110555818770425L;
    /** ��ѯִ���� */
    private IUfoQueryExecutor queryExecutor;

    private UISplitPane splitPane;
    /** ��ѯ������ */
    private HBBBQuerySchemeArea querySchemeArea;
    /** ���ٲ�ѯ�� */
    private HBBBQuickQueryArea quickQueryArea;

    private HBBBQueryHolder queryHolder = null;

    private boolean showRepSelectBtn = true;
    /** �Ƿ��ѯҶ�ӽڵ���֯ */
    private boolean isQueryLeaf = true;

    public static final int SCREEN_WIDTH_1024 = 1024;

    private int splitDividateLocation = -1;

    /**
     * ��ѯ�Ի������
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
        // ��ѯ������ �� ���ٲ�ѯ���໥����UI״̬�仯
        getQuerySchemeArea().addUIStateChangedListener(getQuickQueryArea());
    }

    private void initListeners4QuickQueryArea() {
        // ��ѯ������ �� ���ٲ�ѯ���໥����UI״̬�仯
        getQuickQueryArea().addUIStateChangedListener(getQuerySchemeArea());

        // ��ѯ�������������UI״̬��������ݿ������״̬����SplitPane�ָ�����λ��
        getQuickQueryArea().addUIStateChangedListener(new UIStateChangedListener() {
            @Override
            public void stateChanged(UIStateChangedEvent event) {
                switch (event.getUIStateType()) {
                case UIStateChangedEvent.EXPAND: {// �����չ����
                    if (splitDividateLocation <= 0)
                        getSplitPane().setDividerLocation(calculateDividerLocation4MinQuerySchemeArea());
                    else
                        getSplitPane().setDividerLocation(splitDividateLocation);
                    break;
                }
                case UIStateChangedEvent.COLLAPSE: {// �����������
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
     * ���ò�ѯִ����
     * 
     * @param executor
     *            ��ѯִ����
     */
    public void setQueryExecutor(IUfoQueryExecutor executor) {
        this.queryExecutor = executor;
        getQuerySchemeArea().setQueryExecutor(executor);
        getQuickQueryArea().setQueryExecutor(executor);

        IUfoQueryCondVO queryCond = (IUfoQueryCondVO) queryHolder.getQueryCond().clone();
        // ��ʼ�������ʱ���Ӧ������QueryCond ��ִ������.
        getQueryExecutor().setQueryCond(queryCond);
    }

    /**
     * ���ز�ѯִ����
     */
    public IUfoQueryExecutor getQueryExecutor() {
        return queryExecutor;
    }

    /**
     * ������С����ť(��QueryAreaShell�ⲿע��)
     * 
     * @param miniAction
     *            ��С����ť
     */
    public void setMiniAction(Action miniAction) {
        getQuerySchemeArea().setMiniAction(miniAction);
    }

    /**
     * ���ز�ѯ������ƫ��(�ṩ��QueryAreaShell)
     */
    public QueryAreaPreferences getPreferences() {
        QueryAreaPreferences preferences = new QueryAreaPreferences();
        preferences.setQuickAreaMinimized(getQuickQueryArea().isMinimized());
        preferences.setSplitPaneDividerLocation(getSplitPane().getDividerLocation());
        return preferences;
    }

    /**
     * ���ò�ѯ������ƫ��(��QueryAreaShell�ⲿע��)
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
