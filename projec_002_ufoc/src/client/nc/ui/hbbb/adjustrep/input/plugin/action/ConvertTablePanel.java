package nc.ui.hbbb.adjustrep.input.plugin.action;

import java.awt.BorderLayout;

import javax.swing.JTable;

import nc.ui.hbbb.adjustrep.input.edit.AdjustRepDataEditor;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UIScrollPane;
import nc.util.hbbb.input.hbreportdraft.LinkEndTreeModel;

import com.ufida.zior.comp.treetable.KTreeTable;

public class ConvertTablePanel extends  UIPanel{
	
	
	private KTreeTable treeTable;
	
	
//	public ConvertTablePanel(IWorkDraft newworkdraft,AbsCombRepDataEditor parentEditor){
//		super(newworkdraft,parentEditor);
//	
//	}  
	
	
	public ConvertTablePanel(LinkEndTreeModel linkModel){
//		super();
		LinkEndKTreeTableModel tableModel = new LinkEndKTreeTableModel(linkModel);
//		
////		root.setUserObject(userObject);
//	
//		treeTable = new KTreeTable(new KTreeTableModel)
//		
		KTreeTable treeTable = new KTreeTable(tableModel);
		treeTable.setDefaultRenderer(Object.class, new LinkEndCellRenderer());
		treeTable.setDefaultEditor(Object.class, new LinkEndTableEditor());
		
		
		treeTable.getTableHeader().getColumnModel().getColumn(0).setMinWidth(360);
		for(int i=1;i<	treeTable.getTableHeader().getColumnModel().getColumnCount();i++){
			treeTable.getTableHeader().getColumnModel().getColumn(i).setMinWidth(180);
		}
	
		
		this.setLayout(new BorderLayout());
		UIScrollPane scroll = new UIScrollPane();
		treeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scroll.setViewportView(treeTable);
 
		
		this.add(scroll);
		
	}
	 

}
