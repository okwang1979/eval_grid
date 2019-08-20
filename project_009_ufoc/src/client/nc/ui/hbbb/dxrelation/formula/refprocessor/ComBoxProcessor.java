package nc.ui.hbbb.dxrelation.formula.refprocessor;




import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.text.PlainDocument;

import nc.ui.hbbb.combox.util.ComBoxManager;
import nc.ui.pub.beans.UIComboBox;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UITextField;
import nc.vo.hbbb.util.ComBoxVO;

import com.ufida.dataset.IContext;
import com.ufsoft.script.function.IParamRefProcessor;
import com.ufsoft.script.function.UfoFuncInfo;

/**
 * 处理COMBOX类型的
 * @author liyra
 * @DATE 20110418
 *
 */

public  class ComBoxProcessor  implements IParamRefProcessor{  
	
	
	protected UIComboBox[] comboxs;
	
	protected ComBoxVO[] comboxvos;
	
	protected UIComboBox[] getComboxs() {
		// TODO Auto-generated method stub
		ArrayList<ArrayList<?>> list=new ArrayList<ArrayList<?>>();
		list.add(ComBoxManager.getInstance().forArrayList(getComboxvos()));

		if(null==this.comboxs){
			comboxs=ComBoxManager.getInstance().getComBoxs(list);
		}
		
		textfield.setText(getComboxvos()[0].getValue());
	
		return comboxs;
	}

	@Override
	public void clearInputValue() {
		// TODO Auto-generated method stub
		
	}
	
	protected UITextField textfield;
	
	

	@Override
	public String getInputValue() {
		// TODO Auto-generated method stub
		if(textfield==null){
			return "";
		}
		return textfield.getText();
	}

	@Override
	public JPanel getRefPane(IContext context, UfoFuncInfo funcInfo,
			PlainDocument document) {
		// TODO Auto-generated method stub
		UIPanel pane=new UIPanel();
//		pane.setBorder(BorderFactory.createLineBorder(new Color(192, 192, 192)));
		pane.setLayout(new java.awt.BorderLayout());
		textfield=new UITextField();
		pane.add(textfield,java.awt.BorderLayout.NORTH);
		textfield.setDocument(document);
		textfield.setVisible(false);
		UIComboBox combox=getComboxs()[0];
		combox.setPreferredSize(new Dimension(189, 21));
		combox.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				Object value=((UIComboBox)e.getSource()).getSelectdItemValue();
				if(null!=value){
					textfield.setText(((ComBoxVO)value).getValue());
				}
			}});
	
//		combox.addActionListener(new )
//
//
//			@Override
//			public void propertyChange(PropertyChangeEvent evt) {
//				// TODO Auto-generated method stub
//				Object value=((UIComboBox)evt.getSource()).getSelectdItemValue();
//				if(null!=value){
//					textfield.setText(((ComBoxManager.ComBoxVO)value).getValue());
//				}
//			}});
//		
		pane.add(combox,java.awt.BorderLayout.CENTER);
		return pane;
	}

	@Override
	public String[] getRefProcessorNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRefOtherProcessor() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setInputValue(String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRefProcessors(IParamRefProcessor[] pros) {
		// TODO Auto-generated method stub
		
	}

	public  ComBoxVO[] getComboxvos(){ return null;} ;
	
	@Override
	public void reqFoucus(){
		if(textfield != null) {
			textfield.requestFocus();
		}
	}
}
