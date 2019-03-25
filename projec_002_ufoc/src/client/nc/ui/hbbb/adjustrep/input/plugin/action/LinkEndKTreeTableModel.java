package nc.ui.hbbb.adjustrep.input.plugin.action;

import javax.swing.tree.DefaultMutableTreeNode;

import nc.util.hbbb.input.hbreportdraft.LinkEndTreeModel;
import nc.util.hbbb.input.hbreportdraft.LinkEndTreeUserObj;
import nc.vo.iufo.measure.MeasureVO;

import com.ufida.zior.comp.treetable.DefaultKTreeTableModel;

public class LinkEndKTreeTableModel extends DefaultKTreeTableModel {

	
	
	private LinkEndTreeModel linkModel;
 

	public LinkEndKTreeTableModel(LinkEndTreeModel linkModel) {
		super(linkModel.getRoot());
		
		this.linkModel =  linkModel;
	}

	@Override
	public Object getValueAt(Object node, int column) {
		DefaultMutableTreeNode row = (DefaultMutableTreeNode)node;
		LinkEndTreeUserObj userObj = (LinkEndTreeUserObj) row.getUserObject();
		if(column==0){
			return userObj.getOrgDisName();
		}
//		DefaultMutableTreeNode row = (DefaultMutableTreeNode)node;
//		LinkEndTreeUserObj userObj = (LinkEndTreeUserObj) row.getUserObject();
		MeasureVO selectM =  linkModel.getMeasures().get(column-1);
		return userObj.getData().get(selectM);

	}

	@Override
	public String getColumnName(int column) {
		if(column==0){
			return "Ö÷Ìå";
		}else{
			String name = linkModel.getMeasures().get(column-1).getFullName();
			String[] keyAndName = name.split("->");
			if(keyAndName.length>1){
				return keyAndName[1];
			}
			return keyAndName[0];
		}

		 
	}

	@Override
	public int getColumnCount() {

		return linkModel.getMeasures().size()+1;
	}

}
