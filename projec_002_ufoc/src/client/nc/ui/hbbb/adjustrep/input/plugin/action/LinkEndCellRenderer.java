package nc.ui.hbbb.adjustrep.input.plugin.action;

import java.awt.Component;
import java.math.RoundingMode;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class LinkEndCellRenderer extends DefaultTableCellRenderer{
	
	
	 public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
		 Component rtn = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		 
		 if(column>0){
			 if(rtn instanceof JLabel){
				 JLabel label = (JLabel)rtn;
					// 设置对齐方式
				 label.setHorizontalAlignment(SwingConstants.RIGHT);
//					editor.setText();
					// editor.setFont(new Font("Serif", Font.BOLD, 14));
				 
			 }
			 
			 setText(returnDouble(String.valueOf(value)));
				 
		 }
	 
		 return rtn;
	 }
	 
	 private String returnDouble(String value) {
			NumberFormat numberFormat = NumberFormat.getInstance();
			numberFormat.setRoundingMode(RoundingMode.HALF_UP);
			numberFormat.setMaximumFractionDigits(2);
			numberFormat.setMinimumFractionDigits(2);
			numberFormat.setGroupingUsed(true);
			try{
				return numberFormat.format(Double.valueOf(value));
			}catch(Exception ex){
				return value;
			}
			

		}
	 
	    /**
	     * Sets the <code>String</code> object for the cell being rendered to
	     * <code>value</code>.
	     *
	     * @param value  the string value for this cell; if value is
	     *          <code>null</code> it sets the text value to an empty string
	     * @see JLabel#setText
	     *
	     */
	    protected void setValue(Object value) {
	        setText((value == null) ? "" : value.toString());
	    }

}
