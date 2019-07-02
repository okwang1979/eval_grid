package nc.ui.hbbb.hbreport.action;

import nc.itf.hbbb.commit.IHbSchemeChangeListener;
import nc.ui.hbbb.quickquery.action.AbsHBBBQueryAction;
import nc.ui.hbbb.quickquery.common.filteritem.HBBBCommitFilterItem;
import nc.ui.hbbb.quickquery.common.filteritem.HBBBIntrFilterItem;
import nc.ui.hbbb.quickquery.model.HBBBQueryHolder;
import nc.ui.hbbb.quickquery.model.HBBBTangramInitEntrance;
import nc.ui.iufo.query.common.filteritem.AssignTaskOrgFilterItem;
import nc.ui.iufo.query.common.filteritem.CheckFilterItem;
import nc.ui.iufo.query.common.filteritem.HastenDateFilterItem;
import nc.ui.iufo.query.common.filteritem.HastenPersonFilterItem;
import nc.ui.iufo.query.common.filteritem.HastenStateFilterItem;
import nc.ui.iufo.query.common.filteritem.IUfoFilterItem;
import nc.ui.iufo.query.common.filteritem.InputDateFilterItem;
import nc.ui.iufo.query.common.filteritem.InputPersonFilterItem;
import nc.ui.iufo.query.common.filteritem.InputStateFilterItem;
import nc.ui.iufo.query.common.filteritem.MustCommFlagFilterItem;
import nc.ui.iufo.query.common.filteritem.MustInputFlagFilterItem;
import nc.ui.iufo.query.common.filteritem.RepDataRightFilterItem;
import nc.ui.iufo.query.common.filteritem.RepFilterItem;
import nc.ui.iufo.query.common.filteritem.ReqbackDateFilterItem;
import nc.ui.iufo.query.common.filteritem.ReqbackPersonFilterItem;
import nc.ui.iufo.query.common.filteritem.ReqbackStateFilterItem;
import nc.vo.hbbb.commit.HBBBRepBuziQueryCondVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;

/**
 * 合并报表查询Action
 * 
 * @version V6.1
 * @author litfb
 */
public class HBReportQueryAction extends AbsHBBBQueryAction implements IHbSchemeChangeListener {

    private static final long serialVersionUID = 5285139027887311335L;

    private HBBBTangramInitEntrance entrance = null;

    @Override
    public IUfoFilterItem[] getFilterItems() {
        InputStateFilterItem inputStateFilterItem = new InputStateFilterItem();
        inputStateFilterItem.setCanRemove(true);
       /**
        *  央客：修改前-添加内部交易表
      
        return new IUfoFilterItem[] { new RepFilterItem(), inputStateFilterItem, new HBBBCommitFilterItem(false),
                new HBBBCommitFilterItem(true), new CheckFilterItem(false), new CheckFilterItem(true),
                new MustInputFlagFilterItem(), new MustCommFlagFilterItem(), new InputDateFilterItem(),
                new InputPersonFilterItem(), new AssignTaskOrgFilterItem(getLoginContext().getPk_group()),
                new RepDataRightFilterItem(), new HastenStateFilterItem(), new HastenPersonFilterItem(),
                new HastenDateFilterItem(), new ReqbackStateFilterItem(), new ReqbackPersonFilterItem(),
                new ReqbackDateFilterItem()};
                  */
        
        return new IUfoFilterItem[] { new RepFilterItem(), inputStateFilterItem, new HBBBCommitFilterItem(false),
                new HBBBCommitFilterItem(true), new CheckFilterItem(false), new CheckFilterItem(true),
                new MustInputFlagFilterItem(), new MustCommFlagFilterItem(), new InputDateFilterItem(),
                new InputPersonFilterItem(), new AssignTaskOrgFilterItem(getLoginContext().getPk_group()),
                new RepDataRightFilterItem(), new HastenStateFilterItem(), new HastenPersonFilterItem(),
                new HastenDateFilterItem(), new ReqbackStateFilterItem(), new ReqbackPersonFilterItem(),
                new ReqbackDateFilterItem(),new HBBBIntrFilterItem() };
    }

    protected HBBBQueryHolder genQueryHolder() {
        HBBBQueryHolder oneQueryHolder = new HBBBQueryHolder(getFilterItems(), getNodeEnv(), getKeyGroup(),
                getExceptionHandler(), taskInfoOrg);
        oneQueryHolder.setModuleName(getModuleName());
        oneQueryHolder.setQueryCondEditHandler(queryCondEditHandler);
        oneQueryHolder.setHbbbqueryAction(this);
        oneQueryHolder.setLoginContext(getLoginContext());
        return oneQueryHolder;
    }

    @Override
    protected boolean isActionEnable() {
        return innerIsActionEnable(((HBBBRepBuziQueryCondVO) entrance.getQueryCondVo()).getHbschemeVO());
    }

    private boolean innerIsActionEnable(HBSchemeVO hbSchemeVO) {
        if (hbSchemeVO == null) {
            return false;
        }
        return super.isActionEnable();
    }

    public HBBBTangramInitEntrance getEntrance() {
        return entrance;
    }

    public void setEntrance(HBBBTangramInitEntrance entrance) {
        this.entrance = entrance;
        entrance.registerHbSchemeChangeListener(this);
    }

    @Override
    public void onHbSchemeChange(HBSchemeVO hbSchemeVO, HBSchemeVO oldHbSchemeVO) {
        setEnabled(innerIsActionEnable(hbSchemeVO));
    }
}
