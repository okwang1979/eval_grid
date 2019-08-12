/**
 * 
 */
package nc.ui.hbbb.dxrelation.formula;

import javax.swing.JTextField;

import nc.ui.iufo.formula.common.ComFmlEditPane;
import nc.ui.iufo.formula.wizard.FunctionWizarPanel;
import nc.ui.pub.beans.UIComboBox;
import nc.util.hbbb.dxrelation.formula.DXFmlEditConst;

import com.ufida.dataset.IContext;
import com.ufsoft.iufo.fmtplugin.formatcore.UfoContextVO;
import com.ufsoft.iufo.util.parser.IFuncType;
import com.ufsoft.iufo.util.parser.UfoSimpleObject;
import com.ufsoft.script.function.FuncListInst;
import com.ufsoft.table.CellsModel;

/**
 * ����ģ�庯��������panel
 * 
 * @author jiaah
 * @created at 2010-10-20,����10:05:50
 * 
 */
public class DXFunctionWizarPanel extends FunctionWizarPanel
	
 {
	
	public void setFuncContent(){
		m_isEditClear = false;
		funcContentField.setText(strFuncContent);
		m_isEditClear = true;
	};

	private static final long serialVersionUID = 1L;
	
	
	protected JTextField funcContentField = new JTextField();
	
	protected boolean m_isEditClear = true;
	
	// �����Ļ�����Ϣ
	private IContext context;
	
	/**
	 * DXFunctionWizarPanel������
	 * 
	 * @create by jiaah at 2010-10-20,����10:06:56
	 * @param parent
	 * @param comFmlEditDlg
	 * @param cellsPane
	 * @param contextVo
	 * @param function
	 * @param ufoFuncList
	 * @param cellsModel
	 */
// public DXFunctionWizarPanel(ComFmlEditPane parent,
// /*ComFmlEditPane*//*CommonFmlEditDlg comFmlEditDlg,*//* CellsPane
// cellsPane,*/
// IContext contextVo, UfoSimpleObject function,
// FuncListInst ufoFuncList, CellsModel cellsModel) {
// super(parent,contextVo,function, ufoFuncList,
// cellsModel);
//		
// // super(parent, comFmlEditDlg, cellsPane, contextVo, function, ufoFuncList,
// // cellsModel);
// }
	
	
	public DXFunctionWizarPanel(ComFmlEditPane comFmlEditDlg, IContext context,
			UfoSimpleObject function, FuncListInst ufoFuncList,CellsModel cellsModel) {
	
		
// ExtFuncWizardPanel(ComFmlEditPane comFmlEditDlg,
// CellsPane cellsPane, UfoSimpleObject function,
// FuncListInst ufoFuncList, CellsModel cellsModel)
		super(comFmlEditDlg,  context,
			 function, ufoFuncList, cellsModel);
		 UfoContextVO contextVO = new UfoContextVO();
		 
// super.setFmlExecutor(this.getFmlExecutor(contextVO, cellsModel));
		this.setContext(context);
		
		
	}
	
// private UfoFmlExecutor getFmlExecutor(Context contextVO, CellsModel
// cellModel){
// RowFilterExprFmlExecutor fmlExecutor = new RowFilterExprFmlExecutor(
// contextVO, cellModel);
//		
// return fmlExecutor;
// }
	
	
	
	
// /**
// * ��д���෽����
// * ������¼������
// */
// @Override
// protected JPanel getParamInputPane(final int i) {
// JPanel inputPanel = new JPanel(new BorderLayout());
// /*int paramType */IParamRefProcessor paramType= funcParamRefType[i];
//		
//		
// //��ʼ���ϲ������Ŀ���Ʋ���
// if(paramType == IFuncType.PARAM_REF_TYPE_ACCOUNT){
// AbstractRefModel refModel =new
// nc.ui.hbbb.account.model.HBAccountPropRefModel(); /*new
// HBAccountRefModel();*/
// //
// refModel.setPk_org(super.getContext().getAttribute(CUR_REPORG_PK).toString());//add
// by jah �����Ļ���
// initBDRefPane(i, inputPanel, refModel);
// }
// //��ʼ�����˿�Ŀ����(Ӧ����λ�ȡ��)
// else if(paramType == IFuncType.PARAM_REF_TYPE_UAPACCOUNT){
// AbstractRefModel refModel = new HBAccountRefModel();
// // refModel.setPk_org(getContext().getAttribute(CUR_REPORG_PK).toString());
// // Object rule = getContext().getAttribute("pk_contrastrule");
// // if(rule != null)
// // refModel.setPk_org(rule.toString());//add by jah ���ݵ�ǰ�Ķ��˹�����ز���ģ�ͣ�����ɣ�
// initBDRefPane(i, inputPanel, refModel);
// }
// //��ʼ����������Ϊcombox��������
// else if(paramType > IFuncType.PARAM_REF_TYPE_ACCOUNT){
//			
// final UIComboBox combox = new UIComboBox();
// combox.setPreferredSize(new Dimension(188, 21));
// combox.setBorder(null);
//			
// addComboxItem(combox,paramType);//��������б�����
//
// funcParamfields[i] = new JTextField();
// funcParamfields[i].setText(String.valueOf(combox.getSelectedIndex()));
// funcParamfields[i].setVisible(false);
//
// inputPanel.add(funcParamfields[i],BorderLayout.CENTER);
// inputPanel.add(combox,BorderLayout.CENTER);
// inputPanel.setBorder(BorderFactory.createLineBorder(new Color(192, 192,
// 192)));
//			
// /**
// * ���������¼�����comboxֵ�ı�ʱ������funcParamfields[i]��ֵ�ı仯
// */
// combox.addItemListener(new ItemListener(){
// @Override
// public void itemStateChanged(ItemEvent e) {
// funcParamfields[i].setText(combox.getSelectedIndex() + "");
// }});
// }
// else{
// return super.getParamInputPane(i);
// }
// return inputPanel;
// }
	
// /**
// * �������ݲ�������ʼ��(ֱ�ӿ���������,�����Ϊpublic �� protected)
// * @create by liuchuna at 2010-4-23,����10:54
// * @param i
// * @param inputPanel
// */
// private void initBDRefPane(final int i, JPanel inputPanel, AbstractRefModel
// refModel) {
// UIRefPane currRefPane = new UIRefPane();
//		
//	
// currRefPane.setRefModel(refModel);
// currRefPane.setButtonFireEvent(true);
// currRefPane.setMultiCorpRef(true); //add by jah ��ʾ��������
// ValueChangeDocument document = new ValueChangeDocument();
// currRefPane.getUITextField().setDocument(document);
// funcParamfields[i] = currRefPane.getUITextField();
// funcParamfields[i].setPreferredSize(new Dimension(170,21));
// inputPanel.add(currRefPane.getUIButton(),BorderLayout.EAST);
// inputPanel.add(funcParamfields[i],BorderLayout.WEST);
// inputPanel.setBorder(BorderFactory.createLineBorder(new Color(192, 192,
// 192)));
//		
// currRefPane.addValueChangedListener(new ValueChangedListener(){
//
// @Override
// public void valueChanged(ValueChangedEvent event) {
// // TODO Auto-generated method stub
// if(((UIRefPane)event.getSource()).getRefModel() instanceof
// HBAccountPropRefModel ){
// StringBuilder content=new StringBuilder();
// content.append("'");
// content.append(((HBAccountPropRefModel)((UIRefPane)event.getSource()).getRefModel()).getHbaccchartcode());
// content.append(HBFmlConst.FMLSPLIT);
// content.append(((UIRefPane)event.getSource()).getRefName());
// content.append(HBFmlConst.CODENAMESPLIT);
// content.append(((UIRefPane)event.getSource()).getRefCode());
// content.append("'");
// funcParamfields[i].setText(content.toString()/*"'"+((UIRefPane)event.getSource()).getText()
// + "'"*/);
// }else{
// // funcParamfields[i].setText("'"+((UIRefPane)event.getSource()).getText() +
// "'");
// }
//				
// }
//			
// });
// }
	
	
	/**
	 * ��������б�����
	 * 
	 * @create by jiaah at 2010-10-25,����10:35:43
	 * @param combox
	 * @param paramType
	 */
	private void addComboxItem(UIComboBox combox,int paramType){
		switch(paramType){
		case IFuncType.PARAM_COMBOX_TYPE_DATASOURCE :
			combox.addItem(DXFmlEditConst.DATASOUCE[0]);// ������Դ������������
			combox.addItem(DXFmlEditConst.DATASOUCE[1]);
			break;
		case IFuncType.PARAM_COMBOX_TYPE_IDATASOURCE :
			combox.addItem(DXFmlEditConst.IDATASOUCE[0]);// ������Դ�����ԶԷ�
			combox.addItem(DXFmlEditConst.IDATASOUCE[1]);
			break;
		case IFuncType.PARAM_COMBOX_TYPE_CREDIT :
			combox.addItem(DXFmlEditConst.CREDIT[0]);// ��������跽
			combox.addItem(DXFmlEditConst.CREDIT[1]);
			break;
		case IFuncType.PARAM_COMBOX_TYPE_CHECKTYPE :
			combox.addItem(DXFmlEditConst.CHECKTYPE[0]);// �������ͣ�����λ
			combox.addItem(DXFmlEditConst.CHECKTYPE[1]);					
			combox.addItem(DXFmlEditConst.CHECKTYPE[2]);
			break;	
		default:
			break;
		}
	}
	
	
	
// /**
// * ���հ�ť�����¼�
// */
// @Override
// protected void doReferAction(ActionEvent e){
// for (int i = 0; i < paramNum; i++) {
// if (e.getSource() == paramRefBtns[i] || e.getSource() == funcParamfields[i])
// {
// int paramType = funcParamRefType[i];
//				
// if (paramType == IFuncType.PARAM_REF_TYPE_ACCOUNT) {
// // ����ϲ���Ŀ���գ�����ɣ�
// // ���������Ĳ��ղ��ڴ˴����
// return;
// }
// else{
// super.doReferAction(e);
// }
// }
// }
// }



	public IContext getContext() {
		return context;
	}



	public void setContext(IContext context) {
		this.context = context;
	}
}
