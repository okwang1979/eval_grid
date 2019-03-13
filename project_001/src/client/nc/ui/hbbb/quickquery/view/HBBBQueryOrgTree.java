package nc.ui.hbbb.quickquery.view;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import nc.bs.logging.Logger;
import nc.impl.iufo.utils.MultiLangTextUtil;
import nc.itf.hbbb.hbrepstru.IHBRepstruQrySrv;
import nc.ui.corg.reportcombinestru.model.ReportCombineStruMemberVersionTreeCreateStrategy;
import nc.ui.hbbb.hbreport.model.ReportCombineStruMemberVerisonWithCodeNameHierModel;
import nc.ui.hbbb.quickquery.model.HBBBQueryOrgTreeCellRenderer;
import nc.ui.hbbb.view.ReportCombinStruMemberWithCodeNameTreeCreateStrategy;
import nc.ui.iufo.input.ufoe.comp.OrgTreeSelChangeListener;
import nc.ui.iufo.input.ufoe.comp.OrgTreeSelectionListener;
import nc.ui.pub.beans.UITree;
import nc.ui.queryarea.util.QueryAreaColor;
import nc.ui.uif2.model.HierachicalDataAppModel;
import nc.vo.bd.meta.BDObjectAdpaterFactory;
import nc.vo.corg.ReportCombineStruMemberWithCodeNameVO;
import nc.vo.pub.BusinessException;
import nc.vo.uif2.LoginContext;
import nc.vo.vorg.ReportCombineStruVersionVO;

/**
 * 组织树
 * 
 * @version V6.1
 * @author litfb
 * @see nc.ui.iufo.query.common.area.IUfoQueryOrgTree
 */
public class HBBBQueryOrgTree extends UITree implements OrgTreeSelChangeListener {

    private static final long serialVersionUID = -470851180390300469L;

    private HierachicalDataAppModel model;

    // 点击树上节点时，是否查询，如果对应当前方案，用户点过查询按钮，则置为true,下次点击树时，直接查询
    private boolean directQuery = false;

    public HBBBQueryOrgTree(LoginContext loginContext) {
        model = new ReportCombineStruMemberVerisonWithCodeNameHierModel();

        model.setContext(loginContext);

        BDObjectAdpaterFactory boadapterfacotry = new BDObjectAdpaterFactory();

        ReportCombinStruMemberWithCodeNameTreeCreateStrategy treeCreateStrategy = new ReportCombinStruMemberWithCodeNameTreeCreateStrategy();
        treeCreateStrategy.setClassName(ReportCombineStruMemberWithCodeNameVO.class.getName());
        treeCreateStrategy.setFactory(boadapterfacotry);

        model.setTreeCreateStrategy(treeCreateStrategy);
        model.setBusinessObjectAdapterFactory(boadapterfacotry);
        setModel(model.getTree());
        // 设置树只能选择一个节点
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        addTreeSelectionListener(new OrgTreeSelectionListener(this, this));

        setCellRenderer(new HBBBQueryOrgTreeCellRenderer(QueryAreaColor.BKGRD_COLOR_DEFAULT));
    }

    @Override
    public void onTreeSelectionChange(TreePath path) {
        if (directQuery == false)
            return;

        HBBBQuickQueryArea quickQueryArea = (HBBBQuickQueryArea) SwingUtilities.getAncestorOfClass(
                HBBBQuickQueryArea.class, this);
        if (quickQueryArea != null) {
            quickQueryArea.doQuery();
        }
    }

    public void setDirectQuery(boolean directQuery) {
        this.directQuery = directQuery;
    }

    public void refreshByQueryCond(ReportCombineStruVersionVO rcsvVO) throws Exception {
        //无法适配合并体系时重设model的数据
    	if (rcsvVO.getPrimaryKey() == null) {
        	model.initModel(null);
        	((ReportCombineStruMemberVersionTreeCreateStrategy)model.getTreeCreateStrategy()).setStruname("");
        	setModel(model.getTree());
        	return;
        }
        
        // 换了一个查询方案或查询方案内容发生变动，不支持直接点击树的查询
        directQuery = false;

        IHBRepstruQrySrv service = (IHBRepstruQrySrv) nc.bs.framework.common.NCLocator.getInstance().lookup(
                IHBRepstruQrySrv.class.getName());
        ReportCombineStruMemberWithCodeNameVO[] datas = null;
        try {
            datas = service.queryReportCombineStruMemberVOWithCodeNameByVersionId(rcsvVO.getPk_vid(),model.getContext());
        } catch (BusinessException e) {
            Logger.error(e.getMessage(), e);
        }
        // 树根节点：合并体系名称(合并体系版本名称)
        String strRcsName = MultiLangTextUtil.getCurLangText(rcsvVO, ReportCombineStruVersionVO.NAME);
        String strRcsvName = MultiLangTextUtil.getCurLangText(rcsvVO, ReportCombineStruVersionVO.VNAME);
        ((ReportCombineStruMemberVersionTreeCreateStrategy)model.getTreeCreateStrategy()).setStruname(strRcsName + "(" + strRcsvName + ")");
        model.initModel(datas);

        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getTree().getRoot();
        if (rootNode != null && rootNode.getChildCount() > 0) {
            model.setSelectedNode((DefaultMutableTreeNode) rootNode.getFirstChild());
        }

        setModel(model.getTree());

        DefaultMutableTreeNode selNode = model.getSelectedNode();
        if (selNode != null) {
            TreePath path = new TreePath(model.getTree().getPathToRoot(selNode));
            setSelectionPath(path);
            scrollPathToVisible(path);
        }
    }

}
