package nc.ui.hbbb.quickquery.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import nc.ui.hbbb.quickquery.action.AbsHBBBQueryAction;
import nc.ui.hbbb.quickquery.model.AbsHBBBQueryConfig;
import nc.ui.hbbb.quickquery.model.HBBBQueryHolder;
import nc.ui.iufo.query.common.event.IUfoQueryCondChangeListener;
import nc.ui.iufo.query.common.event.IUfoQueryHolder;
import nc.ui.iufo.query.common.filteritem.AbsDataDateFilterItem;
import nc.ui.iufo.query.common.filteritem.IUfoFilterItem;
import nc.ui.iufo.query.common.filteritem.InputDateFilterItem;
import nc.ui.iufo.query.common.model.IBillModelFilter;
import nc.ui.iufo.query.common.model.IUfoBillManageModel;
import nc.ui.pub.beans.UILabel;
import nc.ui.pub.beans.UIPanel;
import nc.ui.queryarea.util.QueryAreaColor;
import nc.ui.uif2.tangramlayout.CardLayoutToolbarPanel;
import nc.vo.iufo.query.IUfoQueryCondVO;

/**
 * 查询筛选面板
 * 
 * @version V6.1
 * @author litfb
 * @see nc.ui.iufo.query.common.comp.IUfoQueryFilterPanel
 */
public class HBBBQueryFilterPanel extends UIPanel implements IUfoQueryCondChangeListener, ComponentListener,
        IBillModelFilter, ActionListener {

    private static final long serialVersionUID = 420477421751569886L;

    private AbsHBBBQueryAction queryAction;

    private UIPanel filterPane;

    private JSplitPane splitPane;

    private IUfoBillManageModel model;

    private IUfoFilterItem[] filterItems;

    private CardLayoutToolbarPanel north;

    private AbsHBBBQueryConfig queryConfig;

    public HBBBQueryFilterPanel() {
        super();
        setBackground(QueryAreaColor.BKGRD_COLOR_DEFAULT);
    }

    public void initUI() {
        setLayout(new BorderLayout());
        if (getNorth() != null) {
            add(getNorth(), BorderLayout.NORTH);
        }
        getFilterPane();
        if (getFilterPane() != null) {
            add(getFilterPane(), BorderLayout.CENTER);
            addComponentListener(this);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
//        if (filterItems != null && e.getSource() == null) {
//        	for (IUfoFilterItem item : filterItems) {
//                item.clearData();
//            }
//            getModel().reFilterModel();
//        }
    }

    @Override
    public Object filterModel(Object datas) {
        saveFilterCondVal();
        //清空查询条件以后，直接返回为空 edit by dongjch @2015-06-02
        if(null == queryAction.getQuickQueryHolder().getQueryCond()){
        	return null;
        }
        if (filterItems == null || datas == null) {
            return datas;
        }

        Object[] results = (Object[]) datas;
        if (results == null || results.length <= 0) {
            return datas;
        }

        List<Object> vRetObj = new ArrayList<Object>();
        for (Object result : results) {
            boolean bAccept = true;
            for (IUfoFilterItem item : filterItems) {
                if (!item.isAcceptResult(result)) {
                    bAccept = false;
                    break;
                }
            }
            if (bAccept) {
                vRetObj.add(result);
            }
        }

        Object[] retObjs = (Object[]) Array.newInstance(results.getClass().getComponentType(), vRetObj.size());
        System.arraycopy(vRetObj.toArray(), 0, retObjs, 0, vRetObj.size());

        return retObjs;
    }

    private void saveFilterCondVal() {
        if (filterItems == null || filterItems.length <= 0)
            return;

        IUfoQueryHolder queryHolder = queryAction.getQuickQueryHolder();
        if (queryHolder.getQueryCond() == null || queryHolder.getQueryCond().getPk_querycond() == null) {
            return;
        }

        IUfoQueryCondVO cond = queryHolder.getQueryCondEditHandler().getInputFilterCond(
                queryHolder.getQueryCond().getPk_querycond());
        if (cond == null) {
            cond = (IUfoQueryCondVO) queryHolder.getQueryCond().clone();
        }
        for (IUfoFilterItem item : filterItems) {
            boolean bOnCandidate = item.isOnCandidate();
            item.setOnCandidate(false);
            item.onQueryCondSave(cond);
            item.setOnCandidate(bOnCandidate);
        }
        queryHolder.getQueryCondEditHandler().addInputFilterCond(cond);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (getSplitPane() != null) {
            if (getFilterPane() != null) {
                GridLayout layout = (GridLayout) filterPane.getLayout();
                int iLocation = 40 + layout.getRows() * 30;
                getSplitPane().setDividerLocation(iLocation);
            } else {
                getSplitPane().setDividerLocation(30);
            }
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        componentResized(e);
    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    @Override
    public void onQueryCondChange(IUfoQueryCondVO oldQueryCond, IUfoQueryCondVO newQueryCond, Object eventSource) {
        reInitFilterPane();
    }

    public void reInitFilterPane() {
        UIPanel oldFilterPane = filterPane;
        filterPane = getFilterPane();

        if (oldFilterPane != null) {
            remove(oldFilterPane);
        }
        if (filterPane != null) {
            add(filterPane, BorderLayout.CENTER);
        }
        updateUI();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                componentResized(null);
            }
        });
    }

    @Override
    public void onQueryCondUpdate(IUfoQueryCondVO oldQueryCond, IUfoQueryCondVO newQueryCond, Object eventSource) {

    }

    @Override
    public void onQueryCondClear(Object eventSource) {

    }

    @Override
    public String[] onQueryCondSave(IUfoQueryCondVO queryCond) {
        return null;
    }

    private List<IUfoFilterItem> doFilterDateItem(List<IUfoFilterItem> vItem) {
        if (vItem == null || vItem.size() <= 0) {
            return vItem;
        }

        boolean bHasDateItem = false;
        for (IUfoFilterItem item : vItem) {
            if (item instanceof AbsDataDateFilterItem) {
                if (((AbsDataDateFilterItem) item).getDateType() == AbsDataDateFilterItem.DATE) {
                    bHasDateItem = true;
                    break;
                }
            }
        }

        if (bHasDateItem) {
            for (int i = vItem.size() - 1; i >= 0; i--) {
                IUfoFilterItem item = vItem.get(i);
                if (item instanceof AbsDataDateFilterItem) {
                    if (((AbsDataDateFilterItem) item).getDateType() != AbsDataDateFilterItem.DATE) {
                        vItem.remove(i);
                    }
                }
            }
        }
        return vItem;
    }

    /**
     * 根据定制列表，筛选列表定制过滤
     * 
     * @param vItem
     * @param vShowColumn
     * @return
     */
    private List<IUfoFilterItem> doFilterItemByConfig(List<IUfoFilterItem> vItem, String[] vShowColumn) {
        if (vItem == null || vItem.size() <= 0) {
            return vItem;
        }

        List<IUfoFilterItem> vRetItem = new ArrayList<IUfoFilterItem>();
        // 首先根据定制/筛选 的项目进行循环，保证原来的列表顺序。
        for (String columnCode : vShowColumn) {
            for (IUfoFilterItem item : vItem) {
                if (columnCode.equals(item.getMetaFieldName())) {
                    vRetItem.add(item);
                    break;
                }
            }
        }
        return vRetItem;
    }

    public AbsHBBBQueryAction getQueryAction() {
        return queryAction;
    }

    public void setQueryAction(AbsHBBBQueryAction queryAction) {
        this.queryAction = queryAction;
        queryAction.getQuickQueryHolder().getQueryCondChangeHandler().addQueryCondChangeListener(this);
    }

    public UIPanel getFilterPane() {
        if (filterPane == null) {
            filterItems = null;
            HBBBQueryHolder queryHolder = getQueryAction().getQuickQueryHolder();
            List<IUfoFilterItem> vItem = queryHolder.getCandidateItem();
            vItem = doFilterDateItem(vItem);
            vItem = doFilterItemByConfig(vItem, queryConfig.getShowColumns());
            vItem = doFilterItemByConfig(vItem, queryConfig.getFilterColumns());
            if (vItem == null || vItem.size() <= 0) {
                return null;
            }

            IUfoQueryCondVO inputCond = null;
            if (queryHolder.getQueryCond() != null && queryHolder.getQueryCond().getPk_querycond() != null) {
                inputCond = queryHolder.getQueryCondEditHandler().getInputFilterCond(
                        queryHolder.getQueryCond().getPk_querycond());
            }

            filterItems = vItem.toArray(new IUfoFilterItem[0]);
            filterPane = new UIPanel();

            int row = filterItems.length / 3 + (filterItems.length % 3 == 0 ? 0 : 1);
            GridLayout gl = new GridLayout(row, 4, 0, 5);
            filterPane.setLayout(gl);
            filterPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            UIPanel itemPanel = null;
            for (IUfoFilterItem item : filterItems) {
                itemPanel = new UIPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
                itemPanel.add(new UILabel(item.getName()));

                boolean bOnCandidate = item.isOnCandidate();
                item.initFromQueryCond(inputCond);
                item.setOnCandidate(bOnCandidate);
                item.setModel(getModel());
                item.getEditComponent().setPreferredSize(new Dimension(90, 30));
                itemPanel.add(item.getEditComponent());
                item.getEditComponent().setBackground(QueryAreaColor.BKGRD_COLOR_DEFAULT);
                itemPanel.setBackground(QueryAreaColor.BKGRD_COLOR_DEFAULT);
                // 田川2012.5.18增加，如果是录入时间条件，那么没有必要设为当前时间，设为空
                if (item instanceof InputDateFilterItem) {
                    item.clearData();
                }
                filterPane.add(itemPanel);
            }
            if (filterItems.length % 3 != 0) {
                for (int i = 0; i < 3 - (filterItems.length % 3); i++) {
                    filterPane.add(new JLabel());
                }
            }
            filterPane.setBackground(QueryAreaColor.BKGRD_COLOR_DEFAULT);
        }
        return filterPane;
    }

    public void setFilterPane(UIPanel filterPane) {
        this.filterPane = filterPane;
    }

    public JSplitPane getSplitPane() {
        if (splitPane == null) {
            splitPane = (JSplitPane) SwingUtilities.getAncestorOfClass(JSplitPane.class, this);
        }
        return splitPane;
    }

    public void setSplitPane(JSplitPane splitPane) {
        this.splitPane = splitPane;
    }

    public IUfoBillManageModel getModel() {
        return model;
    }

    public void setModel(IUfoBillManageModel model) {
        this.model = model;
    }

    public IUfoFilterItem[] getFilterItems() {
        return filterItems;
    }

    public void setFilterItems(IUfoFilterItem[] filterItems) {
        this.filterItems = filterItems;
    }

    public CardLayoutToolbarPanel getNorth() {
        return north;
    }

    public void setNorth(CardLayoutToolbarPanel north) {
        this.north = north;
    }

    public AbsHBBBQueryConfig getQueryConfig() {
        return queryConfig;
    }

    public void setQueryConfig(AbsHBBBQueryConfig queryConfig) {
        this.queryConfig = queryConfig;
    }

}
