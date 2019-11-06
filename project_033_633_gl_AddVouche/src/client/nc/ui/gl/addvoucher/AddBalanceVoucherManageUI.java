package nc.ui.gl.addvoucher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import nc.funcnode.ui.action.SeparatorAction;
import nc.itf.uap.IUAPQueryBS;
import nc.ms.tb.tree.ITreeBuildPolicy;
import nc.ui.corg.ref.ReportCombineStruMultiVersionRefModel;
import nc.ui.pub.beans.UIButton;
import nc.ui.pub.beans.UILabel;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UIRadioButton;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.beans.UISplitPane;
import nc.ui.pub.beans.UITextField;
import nc.ui.pub.beans.UITree;
import nc.ui.pub.beans.ValueChangedEvent;
import nc.ui.pub.beans.ValueChangedListener;
import nc.util.hbbb.service.HBBaseDocItfService;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;

public class AddBalanceVoucherManageUI extends AbstractUfoManageUI implements ValueChangedListener{
	
	
	private UIRefPane selfBookRef = new UIRefPane();// =   RefPubUtil.getRefModel("报表合并体系");
	
	private UIRefPane otherBookRef = new UIRefPane();
	
	private UIRefPane versionRef = new UIRefPane();
	
	private  ReportCombineStruMultiVersionRefModel versionModel = new ReportCombineStruMultiVersionRefModel();
	
	
	
	
//	private UITree orgTree;
	
	
	
	
 
	
	


 


	@Override
	public void init() {
		initMenus();
		initTopPanel();
		
		initCenter();
		setEditEable();
	
		
 
		
	}
	
	
	 void setEditEable(){
	 
		 
	 
	}
	
	
	private void initCenter() {


		UISplitPane sp = new UISplitPane();
		
		
//		leftTreePanel.setTreeModel(new TbFCTreeModel(new TbTreeNode("合并体系", new StringTreeNodePolicy()), new TotalOrgTreePolicy()));
////		this.initTreeModel();
		
//		sp.add(leftTreePanel, "left");
//		sp.add(getCenterPanel(), "right");
//		sp.setOneTouchExpandable(true);
//		sp.setDividerLocation(400);
		add(sp, BorderLayout.CENTER);
		
	}
	
	private UIPanel getCenterPanel(){
	 
		
		return new UIPanel();
		
	}
	
//	private void addColorDem(UIPanel panel,Color color,String text ){
//		
//		UILabel colorLabel = new UILabel("");
//		colorLabel.setPreferredSize(new Dimension(20,20));
//		colorLabel.setSize(20, 20);
//		colorLabel.setOpaque(true);
//		colorLabel.setBackground(color);
//		panel.add(colorLabel);
//		panel.add(new UILabel(text));
//		
//	}

	
 

	private void initMenus(){
		//setInitActions(new AbstractNCAction[] {new TotalSetAction(this),	new SeparatorAction(),new TotalDelAction(this)});
		this.refreshMenuActions();
	}
	
	private void initTopPanel(){
	
		
		selfBookRef.getUITextField().setShowMustInputHint(true);
		
		selfBookRef = new UIRefPane();
		selfBookRef.setRefNodeName("总账核算账簿");
//		bookRef.addValueChangedListener(this);
		selfBookRef.getUITextField().setShowMustInputHint(true);
		
		
		UIPanel refPanel = new UIPanel(new FlowLayout(FlowLayout.LEFT));
		refPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 5, 5));
		
//		refPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		
		refPanel.add(new UILabel("本方核算账簿"));
		refPanel.add(selfBookRef);
		
		this.setLayout(new BorderLayout());
		versionRef.setRefModel(versionModel);
		versionRef.getUITextField().setShowMustInputHint(true);
		versionRef.addValueChangedListener(this);
		refPanel.add(new UILabel("  对方核算账簿"));
		refPanel.add(versionRef);
		
		this.add(refPanel,BorderLayout.NORTH);
	}

	@Override
	public void valueChanged(ValueChangedEvent e) {
		
	}
	
	
	
	
//	private void initLeftTree() {
//		
//		leftTreePanel =  new ComUITreePanel();
//	
// 
//	}
	
	
//	private List<HbTotalOrgTreeVO> getOrgList()  {
//		
//		String pk_rcs  = orgStruRef.getRefPK();
//		String pk_svid =versionRef.getRefPK();
//		IUAPQueryBS service = NCLocator.getInstance().lookup(IUAPQueryBS.class);
//		String sql = "select  t1.pk_org,t1.code,t1.name,t2.innercode,t2.pk_rcs,t2.pk_svid,t2.pk_fatherorg  " +
//				"from org_orgs t1 " +
//				"inner join   (select idx, pk_org,innercode,pk_rcs, pk_svid, pk_fatherorg  from org_rcsmember_v where  pk_rcs ='"+pk_rcs+"' and   pk_svid ='"+pk_svid+"'    and pk_org in (select   pk_fatherorg  from org_rcsmember_v where   pk_rcs ='"+pk_rcs+"' and    pk_svid ='"+pk_svid+"'    )) as t2 on t1.pk_org=t2.pk_org  " +
//				" order by   t1.code,t2.idx ";
//		List<HbTotalOrgTreeVO> vos;
//		try {
//			vos = (List)service.executeQuery(sql, new HbTotalOrgTreeResultSet());
//		} catch (BusinessException e) {
//			Logger.error(e);
//			throw new BusinessRuntimeException(e.getMessage()+"---->query sql:"+sql,e);
//		}
//		if(vos==null||vos.isEmpty()){
//			return new ArrayList<HbTotalOrgTreeVO>();
//		}
//		Set<String> orgs = new HashSet<String>();
//		for(HbTotalOrgTreeVO vo:vos){
//			orgs.add(vo.getPk_org());
//		}
////		isLeaf = HBBaseDocItfService.getRemoteHBRepStru().isLeafMembers(pk_orgs, versionVO.getPk_vid());
//		IHbTotalSchemeServer query = NCLocator.getInstance().lookup(IHbTotalSchemeServer.class);
//		List<HbTotalSchemeVO> schemes = query.queryBy(pk_rcs, pk_svid);
//		java.util.Map<String,HbTotalSchemeVO> map = new HashMap<String,HbTotalSchemeVO>();
//		for(HbTotalSchemeVO scheme:schemes){
//			map.put(scheme.getApp_org(), scheme);
//		}
//		if(map.size()>0){
//			for(HbTotalOrgTreeVO vo:vos){
//				if(map.get(vo.getPk_org())!=null){
//					vo.setScheme(map.get(vo.getPk_org()));
//				}
//			}
//		}
//		
//		
//		
//		return vos;
//	}
//
//
//	@Override
//	public void treeCheckNodeChecked(Object[] arg0) {
//		 
//		
//	}
//
//
//	@Override
//	public void treeNodeSelected(Object obj) {
//		if(obj instanceof HbTotalOrgTreeVO){
//			HbTotalOrgTreeVO vo = (HbTotalOrgTreeVO)obj;
//			treeSelectUpdataUi(vo);
//			if(vo.getScheme()!=null){
//				this.getInitActions()[2].setEnabled(true);
//			}else{
//				this.getInitActions()[2].setEnabled(false);
//			}
//		}else{
//			this.getInitActions()[2].setEnabled(false);
//		}
//		
//		 
//		
//		
//	}
//	
//	public HbTotalOrgTreeVO getSelect(){
//		Object obj = leftTreePanel.getSelectBusiObj();
//		if(obj instanceof HbTotalOrgTreeVO){
//			return (HbTotalOrgTreeVO)obj;
//		}
//		return null;
//	}
//	
//	
//	public void treeSelectUpdataUi(HbTotalOrgTreeVO vo){
//		if(vo.getScheme()!=null){
//			HbTotalSchemeVO scheme = vo.getScheme();
//			if(scheme.getTotalType()!=null){
//				setSelect(scheme.getTotalType());
//			}
//		
//				 
//		}else{
////			TreeNode[] nodes = leftTreePanel.getTreeModel().getPathToRoot(leftTreePanel.getTreeModel().getNodeByPk(vo.getPk_org()));
//			
//			TreeNode node =  leftTreePanel.getTreeModel().getNodeByPk(vo.getPk_org());
//			if(node.getParent()!=null){
//				node = node.getParent();
//			}else{
//				clearButton.setSelected(true);
//			}
//			if(node instanceof TbTreeNode){
//				TbTreeNode tbNode = (TbTreeNode)node;
//				
//				
//				if(tbNode.getUserObject() instanceof HbTotalOrgTreeVO){
//					treeSelectUpdataUi((HbTotalOrgTreeVO)tbNode.getUserObject());
//				}else{
//					clearButton.setSelected(true);
//				}
//			}else{
//				clearButton.setSelected(true);
//			}
//		
//		}
//		
//	}
//	
//	private void setSelect(Integer totalType ){
//		
//		
//		if(totalType.equals(HbTotalSchemeVO.TOTAL_TYPE_DIRECT)){
//			driectChildButton.setSelected(true);
//		}
//		else if(totalType.equals(HbTotalSchemeVO.TOTAL_TYPE_ALL)){
//			allChildButton.setSelected(true);
//		}
//		else if(totalType.equals(HbTotalSchemeVO.TOTAL_TYPE_NOT)){
//			notTotalButton.setSelected(true);
//		}else{
//			clearButton.setSelected(true);
//		}
//		
//	}
//	
//	
//	
//	public String getCurrent_rcs(){
//		String pk_rcs  = orgStruRef.getRefPK();
//		return pk_rcs;
//	}
//	public String getCurrent_svid(){
//		String pk_svid =versionRef.getRefPK();
//		return pk_svid;
//	}
//	
//	
//	public Integer getTotalType(){
//		if(driectChildButton.isSelected()){
//			return HbTotalSchemeVO.TOTAL_TYPE_DIRECT;
//		}
//		else if(allChildButton.isSelected()){
//			return HbTotalSchemeVO.TOTAL_TYPE_ALL ;  
//		}
//		else if(notTotalButton.isSelected()){
//			return HbTotalSchemeVO.TOTAL_TYPE_NOT;
//		}
//		return null;
//	}
//	
	
	
	
	
	
	

}


 
