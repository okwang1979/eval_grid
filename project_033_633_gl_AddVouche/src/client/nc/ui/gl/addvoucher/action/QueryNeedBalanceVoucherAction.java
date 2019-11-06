package nc.ui.gl.addvoucher.action;

import java.awt.event.ActionEvent;

import javax.swing.ListSelectionModel;

import com.ufsoft.iufo.fmtplugin.formula.IDatasetFuncConstant;

import nc.funcnode.ui.action.AbstractNCAction;
import nc.ui.gl.addvoucher.AddBalanceVoucherManageUI;
import nc.ui.pub.beans.UITable;

public class QueryNeedBalanceVoucherAction extends AbstractNCAction{
	 
		
		
		private AddBalanceVoucherManageUI ui;
		
		
		public QueryNeedBalanceVoucherAction(AddBalanceVoucherManageUI ui){
			super("Query","查询","查询");
			this.ui = ui;
		}


		@Override
		public void actionPerformed(ActionEvent e) {
			 
	 
		 
			
		}
		
		
		private void initTable(){
			UITable table = getTable();
//			table.setModel(new FilterTableModel());

			// 设置表属性
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.getTableHeader().setBackground(IDatasetFuncConstant.HEADER_BACK_COLOR);
			table.getTableHeader().setForeground(IDatasetFuncConstant.HEADER_FORE_COLOR);

			// 设置每列的编辑器
//			table.getColumn("").setCellEditor(new DefaultCellEditor(new UIComboBox(new String[]{"AND"})));
//			table.getColumn(DataSetFuncConst.FIELD_COLUMN_NAMES[1]).setCellEditor(new FieldValueEditor());
//			table.getColumn(DataSetFuncConst.FIELD_COLUMN_NAMES[2]).setCellEditor(new DefaultCellEditor(new UIComboBox(DataSetFuncConst.NUM_FILTER_OPERATION)));
//			table.getColumn(DataSetFuncConst.FIELD_COLUMN_NAMES[3]).setCellEditor(new DefaultCellEditor(new UIComboBox(DataSetFuncConst.VALUE_TYPES)));
//			table.getColumn(DataSetFuncConst.FIELD_COLUMN_NAMES[4]).setCellEditor(new FilterValueEditor());

		}


		private UITable getTable() {
			// TODO Auto-generated method stub
			return null;
		}
 
		
		 

}
