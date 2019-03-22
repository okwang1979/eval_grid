package nc.ui.hbbb.adjustrep.input.plugin.action;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.ufida.zior.comp.treetable.DefaultKTreeTableModel;
import com.ufida.zior.comp.treetable.KTreeTable;
import com.ufida.zior.comp.treetable.KTreeTableModel;
import com.ufida.zior.comp.treetable.TreeTableModelListener;
import com.ufida.zior.console.ActionHandler;
import com.ufsoft.iuforeport.tableinput.applet.IFormulaTraceValueItem;

import nc.ui.pub.beans.UIPanel;
import nc.util.hbbb.input.HBBBTableInputActionHandler;

public class ConvertTablePanel extends UIPanel{
	
	
	private KTreeTable treeTable;
	
	public ConvertTablePanel(){
		super();
		DefaultKTreeTableModel tableModel = new DefaultKTreeTableModel(root);
		
//		root.setUserObject(userObject);
	
		treeTable = new KTreeTable(new KTreeTableModel)
		
//		treeTable = new KTreeTable(treeModel);
		
	}
	 

}
