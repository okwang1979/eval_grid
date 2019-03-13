package nc.ui.hbbb.quickquery.model;

import java.awt.BorderLayout;

import nc.ui.hbbb.quickquery.view.HBBBQueryArea;
import nc.ui.iufo.query.common.IUfoQueryExecutor;
import nc.ui.pub.beans.UIPanel;
import nc.ui.queryarea.QueryAreaPreferences;
import nc.ui.uif2.actions.ComponentMiniAction;
import nc.ui.uif2.components.IMiniminizedEventListener;
import nc.ui.uif2.components.IMinimizableComponent;
import nc.ui.uif2.components.MiniminizedEventSource;
import nc.vo.uif2.AppStatusRegisteryCallback;
import nc.vo.uif2.LoginContext;

/**
 * HBBBQueryAreaShell
 * 
 * @version V6.1
 * @author litfb
 * @see nc.ui.iufo.query.common.area.IUfoQueryAreaShell
 */
@SuppressWarnings("serial")
public class HBBBQueryAreaShell extends UIPanel implements IMinimizableComponent {

    private HBBBQueryArea queryArea = null;

    private IUfoQueryExecutor queryExecutor = null;

    private IMinimizableComponent miniDelegator = null;

    private LoginContext context = null;

    private static final String QUERYAREA_ID = "#QUERYAREA_ID_SHELL#";

    private boolean showRepSelectBtn = true;

    /**
     * @return the showRepSelectBtn
     */
    public boolean isShowRepSelectBtn() {
        return showRepSelectBtn;
    }

    /**
     * @param showRepSelectBtn
     *            the showRepSelectBtn to set
     */
    public void setShowRepSelectBtn(boolean showRepSelectBtn) {
        this.showRepSelectBtn = showRepSelectBtn;
    }

    public HBBBQueryAreaShell() {
        miniDelegator = new MiniminizedEventSource(this);
    }

    public void initUI() {
        if (getQueryArea() == null)
            throw new IllegalStateException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0",
                    "01830001-0358")/* @res "queryArea Ù–‘±ÿ–Î…Ë÷√£°" */);
        setLayout(new BorderLayout());
        add(getQueryArea(), BorderLayout.CENTER);

        resetByRegisteInfo();
        setMiniAction4QueryArea();
        registeCallback();
    }

    private void registeCallback() {
        if (getContext() == null)
            return;

        getContext().getStatusRegistery().addCallback(new AppStatusRegisteryCallback() {
            @Override
            public Object getID() {
                return QUERYAREA_ID;
            }

            @Override
            public Object getStatusObject() {
                return getQueryArea().getPreferences();
            }
        });
    }

    private void resetByRegisteInfo() {
        if (getContext() == null || getContext().getStatusRegistery() == null)
            return;
        Object obj = getContext().getStatusRegistery().getAppStatusObject(QUERYAREA_ID);
        if (obj != null && obj instanceof QueryAreaPreferences)
            getQueryArea().setPreferences((QueryAreaPreferences) obj);
    }

    public HBBBQueryArea getQueryArea() {
        return queryArea;
    }

    public void setQueryArea(HBBBQueryArea queryArea) {
        this.queryArea = queryArea;
    }

    private void setMiniAction4QueryArea() {
        ComponentMiniAction miniAction = new ComponentMiniAction();
        miniAction.setComponent(this);
        getQueryArea().setMiniAction(miniAction);
    }

    public boolean isMiniminized() {
        return miniDelegator.isMiniminized();
    }

    public void miniminized() {
        miniDelegator.miniminized();
    }

    public void setMiniminized(boolean isMini) {
        miniDelegator.setMiniminized(isMini);
    }

    public void setMiniminizedEventListener(IMiniminizedEventListener listener) {
        miniDelegator.setMiniminizedEventListener(listener);
    }

    public LoginContext getContext() {
        return context;
    }

    public void setContext(LoginContext context) {
        this.context = context;
    }

    public IUfoQueryExecutor getQueryExecutor() {
        return queryExecutor;
    }

    public void setQueryExecutor(IUfoQueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
        queryArea.setQueryExecutor(queryExecutor);
    }
}