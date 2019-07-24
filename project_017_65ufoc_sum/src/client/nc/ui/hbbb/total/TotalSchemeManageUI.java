package nc.ui.hbbb.total;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import com.borland.jbcl.layout.VerticalFlowLayout;
import com.ufida.web.html.Map;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.funcnode.ui.action.AbstractNCAction;
import nc.itf.hbbb.total.IHbTotalSchemeServer;
import nc.itf.uap.IUAPQueryBS;
import nc.ms.tb.tree.ITreeBuildPolicy;
import nc.ui.corg.ref.ReportCombineStruMultiVersionRefModel;
import nc.ui.hbbb.total.action.*;
import nc.ui.pub.beans.UIButton;
import nc.ui.pub.beans.UILabel;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UIRadioButton;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.beans.UISplitPane;
import nc.ui.pub.beans.UITree;
import nc.ui.pub.beans.ValueChangedEvent;
import nc.ui.pub.beans.ValueChangedListener;
import nc.ui.tb.tree.ComUITreePanel;
import nc.ui.tb.tree.ITbTreeSelectListener;
import nc.ui.tb.tree.TbFCTreeModel;
import nc.ui.tb.tree.TbTreeNode;
import nc.ui.tb.tree.UITreeCellRenderer;
import nc.ui.tb.tree.policy.StringTreeNodePolicy;
import nc.util.hbbb.service.HBBaseDocItfService;
import nc.vo.hbbb.total.HbTotalOrgTreeResultSet;
import nc.vo.hbbb.total.HbTotalOrgTreeVO;
import nc.vo.hbbb.total.HbTotalSchemeVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;

public class TotalSchemeManageUI extends AbstractUfoManageUI implements ValueChangedListener,ITbTreeSelectListener{
	
	
	private UIRefPane orgStruRef = new UIRefPane();// =   RefPubUtil.getRefModel("����ϲ���ϵ");
	
	private UIRefPane versionRef = new UIRefPane();
	
	private  ReportCombineStruMultiVersionRefModel versionModel = new ReportCombineStruMultiVersionRefModel();
	
	private UIRadioButton driectChildButton = new UIRadioButton("ֱ���¼�");
	
	private UIRadioButton allChildButton = new UIRadioButton("�����¼�");
	
	
	private UIRadioButton notTotalButton = new UIRadioButton("������");
	
	
	private UIRadioButton clearButton = new UIRadioButton("clear");
	
	
	
	
//	private UITree orgTree;
	
	
	
	
	
	private ComUITreePanel leftTreePanel = new ComUITreePanel() ;
	
	
	


	public UIRadioButton getDriectChildButton() {
		return driectChildButton;
	}


	public UIRadioButton getAllChildButton() {
		return allChildButton;
	}


	public UIRadioButton getNotTotalButton() {
		return notTotalButton;
	}


	@Override
	public void init() {
		initMenus();
		initTopPanel();
		
		initCenter();
		setEditEable();
	
		
 
		
	}
	
	
	 void setEditEable(){
		if (getState() == INIT){
			orgStruRef.setEnabled(true);
			versionRef.setEnabled(true);
			driectChildButton.setEnabled(false);
			allChildButton.setEnabled(false);
			notTotalButton.setEnabled(false);
			leftTreePanel.m_MainTree.setEnabled(true);
		}
		else if (getState() == EDIT){
			orgStruRef.setEnabled(false);
			versionRef.setEnabled(false);
			driectChildButton.setEnabled(true);
			allChildButton.setEnabled(true);
			notTotalButton.setEnabled(true);
			leftTreePanel.m_MainTree.setEnabled(false);
			
		}
		 
	 
	}
	
	
	private void initCenter() {


		UISplitPane sp = new UISplitPane();
		leftTreePanel.getMainTree().setCellRenderer(new UITreeCellRenderer(){
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				if (value instanceof TbTreeNode) {
					TbTreeNode node = (TbTreeNode) value;
					Object obj = node.getUserObject();
					if (obj != null && obj instanceof HbTotalOrgTreeVO) {
						HbTotalOrgTreeVO userObj = (HbTotalOrgTreeVO)obj;
						HbTotalSchemeVO scheme =  userObj.getScheme();
						if(scheme!=null){
							if(HbTotalSchemeVO.TOTAL_TYPE_DIRECT.equals(  scheme.getTotalType())){
//								setForeground(Color.ORANGE);
								
								setForeground(new Color(255, 200, 0));
//								 97 0
//								setFont(this.getFont().);
//								setForeground(new Color(0, 100, 0));
							}else if(HbTotalSchemeVO.TOTAL_TYPE_ALL.equals(  scheme.getTotalType())){
//								setForeground(Color.GREEN);
								setForeground(Color.RED);
							}else if(HbTotalSchemeVO.TOTAL_TYPE_NOT.equals(  scheme.getTotalType())){
								setForeground(Color.BLUE);
							}
						}
						 
							
					}
				}
				return this;
			}
		});
		
		
		leftTreePanel.setTreeModel(new TbFCTreeModel(new TbTreeNode("�ϲ���ϵ", new StringTreeNodePolicy()), new TotalOrgTreePolicy()));
//		this.initTreeModel();
		
		sp.add(leftTreePanel, "left");
		sp.add(getCenterPanel(), "right");
		sp.setOneTouchExpandable(true);
		sp.setDividerLocation(320);
		add(sp, BorderLayout.CENTER);
		
	}
	
	private UIPanel getCenterPanel(){
		UIPanel centerPanel = new UIPanel();
		centerPanel.setLayout(new BorderLayout());
		UIPanel topPanel = new UIPanel( new FlowLayout( FlowLayout.LEFT));
		topPanel.add(driectChildButton);
		topPanel.add(allChildButton);
		topPanel.add(notTotalButton);
		ButtonGroup group = new ButtonGroup();
		group.add(driectChildButton);
		group.add(allChildButton);
		group.add(clearButton);
		group.add(notTotalButton);
//		driectChildButton.setSelected(true);
		centerPanel.add(topPanel,BorderLayout.NORTH);
		
		UIPanel bottom = new UIPanel(new FlowLayout( FlowLayout.LEFT));
		
		UILabel blueButton = new UILabel("  ");
		blueButton.setBackground(new Color(255, 200, 0));
//		blueButton.setEnabled(false);
		bottom.add(blueButton);
		centerPanel.add(bottom,BorderLayout.SOUTH);
		
		return centerPanel;
		
	}

	
	public Object getTreeSeletc(){
		return this.leftTreePanel.getSelectBusiObj();
	}
	 

	private void initMenus(){
		setInitActions(new AbstractNCAction[] {new TotalSetAction(this)});
		setEditActions(new AbstractNCAction[] {new TotalSaveAction(this),new TotalCancelAction(this)});
		this.refreshMenuActions();
	}
	
	private void initTopPanel(){
	
		
		orgStruRef.getUITextField().setShowMustInputHint(true);
		
		orgStruRef = new UIRefPane();
		orgStruRef.setRefNodeName("����ϲ���ϵ");
		orgStruRef.addValueChangedListener(this);
		orgStruRef.getUITextField().setShowMustInputHint(true);
		
		
		UIPanel refPanel = new UIPanel(new FlowLayout(FlowLayout.LEFT));
		refPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 5, 5));
		
//		refPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		
		refPanel.add(new UILabel("����ϲ���ϵ"));
		refPanel.add(orgStruRef);
		
		this.setLayout(new BorderLayout());
		versionRef.setRefModel(versionModel);
		versionRef.getUITextField().setShowMustInputHint(true);
		versionRef.addValueChangedListener(this);
		refPanel.add(new UILabel("  �汾"));
		refPanel.add(versionRef);
		
		this.add(refPanel,BorderLayout.NORTH);
	}

	@Override
	public void valueChanged(ValueChangedEvent e) {
		 if(e.getSource().equals(orgStruRef)){
			String pk_stru = orgStruRef.getRefPK();
			versionModel.setPk_rcs(pk_stru);
			versionModel.clearData();
			versionRef.getUITextField().setText("");
			leftTreePanel.setTreeModel(new TbFCTreeModel(new TbTreeNode("�ϲ���ϵ", new StringTreeNodePolicy()), new TotalOrgTreePolicy()));
			versionRef.updateUI();
			
		 }
		 if(e.getSource().equals(versionRef)){
			 String version = versionRef.getRefPK();
			 if(version!=null&&version.trim().length()>0){
				 initTreeModel();
			 }
		 }
		
	}
	
	
	
	
//	private void initLeftTree() {
//		
//		leftTreePanel =  new ComUITreePanel();
//	
// 
//	}
//	
	private void initTreeModel(){
		
		TbFCTreeModel treeModel = new TbFCTreeModel(new TbTreeNode("�ϲ���ϵ", new StringTreeNodePolicy()),getOrgList().toArray(),new TotalOrgTreePolicy());
		this.leftTreePanel.setTreeModel(treeModel);
		this.leftTreePanel.updateUI();
		leftTreePanel.addTbTreeSelectListener(this);

	}
	
	
	private List<HbTotalOrgTreeVO> getOrgList()  {
		
		String pk_rcs  = orgStruRef.getRefPK();
		String pk_svid =versionRef.getRefPK();
		IUAPQueryBS service = NCLocator.getInstance().lookup(IUAPQueryBS.class);
		String sql = "select  t1.pk_org,t1.code,t1.name,t2.innercode,t2.pk_rcs,t2.pk_svid,t2.pk_fatherorg  " +
				"from org_orgs t1 " +
				"inner join   (select idx, pk_org,innercode,pk_rcs, pk_svid, pk_fatherorg  from org_rcsmember_v where  pk_rcs ='"+pk_rcs+"' and   pk_svid ='"+pk_svid+"'    and pk_org in (select   pk_fatherorg  from org_rcsmember_v where   pk_rcs ='"+pk_rcs+"' and    pk_svid ='"+pk_svid+"'    )) as t2 on t1.pk_org=t2.pk_org  " +
				" order by   t2.idx ";
		List<HbTotalOrgTreeVO> vos;
		try {
			vos = (List)service.executeQuery(sql, new HbTotalOrgTreeResultSet());
		} catch (BusinessException e) {
			Logger.error(e);
			throw new BusinessRuntimeException(e.getMessage()+"---->query sql:"+sql,e);
		}
		if(vos==null||vos.isEmpty()){
			return new ArrayList<>();
		}
		Set<String> orgs = new HashSet<>();
		for(HbTotalOrgTreeVO vo:vos){
			orgs.add(vo.getPk_org());
		}
//		isLeaf = HBBaseDocItfService.getRemoteHBRepStru().isLeafMembers(pk_orgs, versionVO.getPk_vid());
		IHbTotalSchemeServer query = NCLocator.getInstance().lookup(IHbTotalSchemeServer.class);
		List<HbTotalSchemeVO> schemes = query.queryBy(pk_rcs, pk_svid);
		java.util.Map<String,HbTotalSchemeVO> map = new HashMap<>();
		for(HbTotalSchemeVO scheme:schemes){
			map.put(scheme.getApp_org(), scheme);
		}
		if(map.size()>0){
			for(HbTotalOrgTreeVO vo:vos){
				if(map.get(vo.getPk_org())!=null){
					vo.setScheme(map.get(vo.getPk_org()));
				}
			}
		}
		
		
		
		return vos;
	}


	@Override
	public void treeCheckNodeChecked(Object[] arg0) {
		 
		
	}


	@Override
	public void treeNodeSelected(Object obj) {
		if(obj instanceof HbTotalOrgTreeVO){
			HbTotalOrgTreeVO vo = (HbTotalOrgTreeVO)obj;
			treeSelectUpdataUi(vo);
		}
		
		 
		
		
	}
	
	public HbTotalOrgTreeVO getSelect(){
		Object obj = leftTreePanel.getSelectBusiObj();
		if(obj instanceof HbTotalOrgTreeVO){
			return (HbTotalOrgTreeVO)obj;
		}
		return null;
	}
	
	
	public void treeSelectUpdataUi(HbTotalOrgTreeVO vo){
		if(vo.getScheme()!=null){
			HbTotalSchemeVO scheme = vo.getScheme();
			if(scheme.getTotalType()!=null){
				setSelect(scheme.getTotalType());
			}
		
				 
		}else{
//			TreeNode[] nodes = leftTreePanel.getTreeModel().getPathToRoot(leftTreePanel.getTreeModel().getNodeByPk(vo.getPk_org()));
			
			TreeNode node =  leftTreePanel.getTreeModel().getNodeByPk(vo.getPk_org());
			if(node.getParent()!=null){
				node = node.getParent();
			}else{
				clearButton.setSelected(true);
			}
			if(node instanceof TbTreeNode){
				TbTreeNode tbNode = (TbTreeNode)node;
				
				
				if(tbNode.getUserObject() instanceof HbTotalOrgTreeVO){
					treeSelectUpdataUi((HbTotalOrgTreeVO)tbNode.getUserObject());
				}else{
					clearButton.setSelected(true);
				}
			}else{
				clearButton.setSelected(true);
			}
		
		}
		
	}
	
	private void setSelect(Integer totalType ){
		
		
		if(totalType.equals(HbTotalSchemeVO.TOTAL_TYPE_DIRECT)){
			driectChildButton.setSelected(true);
		}
		else if(totalType.equals(HbTotalSchemeVO.TOTAL_TYPE_ALL)){
			allChildButton.setSelected(true);
		}
		else if(totalType.equals(HbTotalSchemeVO.TOTAL_TYPE_NOT)){
			notTotalButton.setSelected(true);
		}else{
			clearButton.setSelected(true);
		}
		
	}
	
	
	
	public String getCurrent_rcs(){
		String pk_rcs  = orgStruRef.getRefPK();
		return pk_rcs;
	}
	public String getCurrent_svid(){
		String pk_svid =versionRef.getRefPK();
		return pk_svid;
	}
	
	
	public Integer getTotalType(){
		if(driectChildButton.isSelected()){
			return HbTotalSchemeVO.TOTAL_TYPE_DIRECT;
		}
		else if(allChildButton.isSelected()){
			return HbTotalSchemeVO.TOTAL_TYPE_ALL ;  
		}
		else if(notTotalButton.isSelected()){
			return HbTotalSchemeVO.TOTAL_TYPE_NOT;
		}
		return null;
	}
	
	
	
	
	
	
	

}


 
