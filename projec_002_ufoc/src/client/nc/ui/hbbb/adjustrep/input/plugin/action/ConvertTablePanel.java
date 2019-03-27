package nc.ui.hbbb.adjustrep.input.plugin.action;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.tree.DefaultTreeCellRenderer;

import nc.ui.hbbb.adjustrep.input.edit.AdjustRepDataEditor;
import nc.ui.pub.beans.UILabel;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UIScrollPane;
import nc.util.hbbb.input.hbreportdraft.LinkEndTreeModel;

import com.ufida.zior.comp.treetable.KTreeTable;
import com.ufida.zior.comp.treetable.KTreeTableModel;
import com.ufida.zior.comp.treetable.TreeTableCellRenderer;

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
//		treeTable.setDefaultEditor(Object.class, new LinkEndTableEditor());
		
		TreeTableCellRenderer treeRender = (TreeTableCellRenderer)treeTable.getDefaultRenderer(KTreeTableModel.class);
//		treeRender.setFont(new Font("Serif", Font.BOLD, 20));
		
		DefaultTreeCellRenderer treerenderer =  (DefaultTreeCellRenderer)treeRender.getCellRenderer();
		treerenderer.setFont(new Font("Serif", Font.BOLD, 17));
		
		treeTable.setDefaultRenderer(KTreeTableModel.class, treeRender);
		treeTable.getTableHeader().setFont(new Font("Serif", Font.BOLD, 17));
		
		treeTable.getTableHeader().getColumnModel().getColumn(0).setMinWidth(380);
		for(int i=1;i<	treeTable.getTableHeader().getColumnModel().getColumnCount();i++){
			treeTable.getTableHeader().getColumnModel().getColumn(i).setMinWidth(200);
		}
	
		
		this.setLayout(new BorderLayout());
		UIScrollPane scroll = new UIScrollPane();
		treeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scroll.setViewportView(treeTable);
 
		
		this.add(scroll);
		
		UIPanel reportName = new UIPanel();
		UILabel label = new UILabel(linkModel.getReportName());
		label.setFont(new Font("Serif", Font.BOLD, 25));
		
		reportName.add(label);
		this.add(reportName,BorderLayout.NORTH);
		
	}
	 

}
