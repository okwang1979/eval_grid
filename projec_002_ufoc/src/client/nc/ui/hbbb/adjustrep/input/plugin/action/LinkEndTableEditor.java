package nc.ui.hbbb.adjustrep.input.plugin.action;

import java.math.RoundingMode;
import java.text.NumberFormat;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LinkEndTableEditor extends DefaultCellEditor {
	public LinkEndTableEditor() {
		super(new JTextField());
	}

	public java.awt.Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column) {
		// 获得默认表格单元格控件
		JTextField editor = (JTextField) super.getTableCellEditorComponent(
				table, value, isSelected, row, column);

		if (value != null)
			editor.setText(value.toString());
		if (column > 0) {
			// 设置对齐方式
			editor.setHorizontalAlignment(SwingConstants.RIGHT);
			editor.setText(returnDouble(String.valueOf(value)));
			// editor.setFont(new Font("Serif", Font.BOLD, 14));
		}
		return editor;
	}

	private String returnDouble(String value) {
		NumberFormat numberFormat = NumberFormat.getInstance();
		numberFormat.setRoundingMode(RoundingMode.HALF_UP);
		numberFormat.setMaximumFractionDigits(2);
		numberFormat.setMinimumFractionDigits(2);
		numberFormat.setGroupingUsed(true);

		return numberFormat.format(Double.valueOf(value));

	}
}
