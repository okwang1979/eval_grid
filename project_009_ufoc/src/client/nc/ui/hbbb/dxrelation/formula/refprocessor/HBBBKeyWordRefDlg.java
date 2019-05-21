package nc.ui.hbbb.dxrelation.formula.refprocessor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import nc.bs.framework.common.NCLocator;
import nc.itf.hbbb.dxfunction.IDxModelFunction;
import nc.md.MDBaseQueryFacade;
import nc.md.model.IBean;
import nc.ui.bd.ref.AbstractRefModel;
import nc.ui.bd.ref.RefPubUtil;
import nc.ui.bd.ref.RefRecentRecordsUtil;
import nc.ui.pub.beans.UIButton;
import nc.ui.pub.beans.UILabel;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.beans.UIScrollPane;
import nc.ui.pub.beans.ValueChangedEvent;
import nc.ui.querytemplate.component.SeparationLine;
import nc.vo.hbbb.dxchoosekey.ChooseKeyVO;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.iufo.fmtplugin.measure.DialogRefListener;
import com.ufsoft.report.dialog.UfoDialog;
import com.ufsoft.script.function.UfoFuncInfo;

public class HBBBKeyWordRefDlg extends UfoDialog implements ActionListener{

	private static final long serialVersionUID = 280706439391396212L;

	private JPanel mainPane = null;

	// 关键字选择
	private UIPanel keyPane = null;
	private UIRefPane keyRefPane = null;
	private UILabel keyLabel = null;

	// 界面按钮
	private UIPanel btnPane = null;
	private JButton okBtn = null;
	private JButton cancelBtn = null;

	// 关键字表格
	private JScrollPane tableScrollPane = null;
	private JTable table = null;

	// 关键字表格模型
	private KeyWordTableModel tablemodel = null;
	private HBBBKeyWordListRefModel keyrefmodel = null;

	// 关键字选择列表中显示的所有关键字
	private ChooseKeyVO[] vos = null;
	private ChooseKeyVO chkeyvo = null;
	private Object resultvo = null;

	public Object getResultvo() {
		return resultvo;
	}

	public void setResultvo(Object resultvo) {
		this.resultvo = resultvo;
	}

	// 选择的关键字
	private ChooseKeyVO selKeyVO;
	public boolean isInitOver = false;

	// 列名:名 称,说 明
	protected String[] colunmNames = {
			"编码","名称"
	};

	/**
	 * 构造方法
	 *
	 * @param parent
	 * @param hasDefVos
	 * @param funcInfo 
	 */
	public HBBBKeyWordRefDlg(Container parent, ChooseKeyVO[] hasDefVos, UfoFuncInfo funcInfo) {
		super(parent);
		if (hasDefVos == null)
			vos = new ChooseKeyVO[0];
		else
			vos = hasDefVos;
		initialize();
	}

	/**
	 * 构造方法
	 *
	 * @param parent
	 * @param hasDefVos
	 */
	public HBBBKeyWordRefDlg(Dialog parent, ChooseKeyVO[] hasDefVos) {
		super(parent);
		if (hasDefVos == null)
			vos = new ChooseKeyVO[0];
		else
			vos = hasDefVos;
		initialize();
	}

	/**
	 * 初始化界面
	 *
	 */
	private void initialize() {
		getContentPane().add(getMainPane());
		setTitle(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0259")/*@res "关键字参照"*/);
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);
		setSize(500, 600);
		isInitOver = true;
	}

	/**
	 * 主面板
	 * 
	 * @return
	 */
	private JPanel getMainPane() {
		if (mainPane == null) {
			mainPane = new UIPanel();
			mainPane.setLayout(new BorderLayout(0, 10));
			// 添加关键字参照选择panel
			mainPane.add(getKeyPane(), BorderLayout.NORTH);
			// mainPane.add(getChooseKeyPane(),BorderLayout.CENTER);
			mainPane.add(getScrollPane(), BorderLayout.CENTER);
			mainPane.add(getBtnPane(), BorderLayout.SOUTH);
		}
		return mainPane;
	}

	/**
	 * 
	 */
	private UIPanel getKeyPane(){
		if (keyPane == null){
			keyPane = new UIPanel();
			keyPane.setLayout(new FlowLayout(FlowLayout.LEFT));
			keyPane.add(getKeyLabel());
			keyPane.add(getKeyRef());
		}
		return keyPane;
	}

	private UIRefPane getKeyRef() {
		if (keyRefPane == null) {
			keyRefPane = new UIRefPane();
			keyRefPane.setRefModel(getKeyrefmodel());
			keyRefPane.setDisabledDataButtonShow(true);
			keyRefPane.addValueChangedListener(new nc.ui.pub.beans.ValueChangedListener() {
						@Override
						public void valueChanged(ValueChangedEvent event) {
							try {
								Vector vec = (Vector) keyrefmodel.getSelectedData().elementAt(0);
								String strRefPK = (String) vec.lastElement();
								resultvo = vec.get(0);
								Object value = null;
								String sqlString = getAllRefSql(strRefPK);
								value = NCLocator.getInstance().lookup(IDxModelFunction.class).queryChooseKeyValue(sqlString);
								if (value == null) {
									return;
								} else {
									ArrayList<ChooseKeyVO> list = new ArrayList<ChooseKeyVO>();
									for (Object resultValue : (List) value) {
										Object[] val = (Object[]) resultValue;
										chkeyvo = new ChooseKeyVO();
										chkeyvo.setKeyval((String) val[0]);
										chkeyvo.setCode((String) val[1]);
										chkeyvo.setName1((String) val[2]);
										list.add(chkeyvo);
									}
									ChooseKeyVO[] result = list.toArray(new ChooseKeyVO[0]);
									vos = result;
									initTableModel();
									table.setModel(tablemodel);
								}
							} catch (Exception e) {
							}
						}
					});
		}
		RefRecentRecordsUtil.clear(keyRefPane.getRefModel());
		return keyRefPane;
	}
	
	private String getAllRefSql(String strRefPK) {
		String strNodeName=strRefPK;
		try{
			IBean bean=MDBaseQueryFacade.getInstance().getBeanByID(strRefPK);
			strNodeName=bean.getDefaultRefModelName();
		}catch(Exception e){
			AppDebug.debug(e);
		}
		AbstractRefModel  model = RefPubUtil.getRefModel(strNodeName);
		String code = model.getRefCodeField();
		String name  = model.getRefNameField();
		String pk  = model.getPkFieldCode();
		String[] sqls = RefPubUtil.getRefModel(strNodeName).getRefSql().split("from");
		return "select " + pk +","+ code +","+ name +" from " + sqls[1];
	}
	
	public HBBBKeyWordListRefModel getKeyrefmodel() {
		if(keyrefmodel==null){
			keyrefmodel=new HBBBKeyWordListRefModel();
		}
		return keyrefmodel;
	}

	private UILabel getKeyLabel(){
		if(keyLabel == null){
			keyLabel = new UILabel("关键字");
		}
		return keyLabel;
	}
	/**
	 * 关键字滚动面板
	 * 
	 * @return
	 */
	private JScrollPane getScrollPane() {
		if (tableScrollPane == null) {
			tableScrollPane = new UIScrollPane(getTable());
		}
		return tableScrollPane;
	}

	/**
	 * 关键字表格
	 * 
	 * @return
	 */
	private JTable getTable() {
		table = null;
		initTableModel();
		table = new nc.ui.pub.beans.UITable();
		table.setAutoCreateColumnsFromModel(false);
		//            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		initTableModel();
		table.setModel(tablemodel);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		TableColumn column;
		for (int k = 0; k < colunmNames.length; k++) {
			TableCellRenderer renderer = new DefaultTableCellRenderer();
			TableCellEditor editor = null;
			column = new TableColumn(k, 168, renderer, editor);
			table.addColumn(column);
		}

		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getSource() == table) {
					if(e.getClickCount() == 2) {
						doOkAction(e);
					}
				}
			}
		});
		return table;
	}

	/**
	 * 按钮面板
	 * 
	 * @return
	 */
	private JPanel getBtnPane() {
		if (btnPane == null) {
			btnPane = new UIPanel();
			btnPane.setLayout(new BorderLayout());

			SeparationLine line = new SeparationLine(2, 2);
			btnPane.add(line, BorderLayout.CENTER);

			UIPanel btnPane2 = new UIPanel();
			btnPane2.setLayout(new FlowLayout(FlowLayout.RIGHT));
			btnPane2.add(getJButtonOk());
			btnPane2.add(getJButtonCancel());
			btnPane.add(btnPane2, BorderLayout.SOUTH);
		}
		return btnPane;
	}

	public void actionPerformed(java.awt.event.ActionEvent event) {
		if (event.getSource() == okBtn) {

			doOkAction(event);

			//        }else if(event.getSource() == keyBtn){
			//        	try {
			//				doKeyAction(event);
			//			} catch (BusinessException e) {
			//				// TODO Auto-generated catch block
			//				e.printStackTrace();
			//			}
		}else {
			setResult(ID_CANCEL);
			if(getParent() instanceof DialogRefListener)
				((DialogRefListener) getParent()).beforeDialogClosed(this);
			close();
		}
	}

	@SuppressWarnings("restriction")
	private void doOkAction(EventObject event) {


		int selIndex = table.getSelectedRow();
		if(selIndex < 0) {
			return;
		}
		selKeyVO = tablemodel.getSelRow(selIndex);
		setResult(ID_OK);
			if(getParent() instanceof DialogRefListener){
				((DialogRefListener) this.getParent()).onRef(event);
			}else {//公式中关键字参照添加的分支。
				close();
			}
	}

	public void dispose() {
		setResult(ID_CANCEL);
		if(getParent() instanceof DialogRefListener){
			((DialogRefListener) getParent()).beforeDialogClosed(this);
		}
		super.dispose();
	}

	/**
	 * 取消按钮
	 *
	 * @create by liuchuna at 2011-5-18,上午10:42:40
	 *
	 * @return
	 */
	private JButton getJButtonCancel() {
		if (cancelBtn == null) {
			cancelBtn = new UIButton();
			cancelBtn.setText(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0257")/*@res "关闭"*/); //"关 闭"
			cancelBtn.addActionListener(this);
		}
		return cancelBtn;
	}

	/**
	 * 确定按钮
	 *
	 * @create by liuchuna at 2011-5-18,上午10:43:04
	 *
	 * @return
	 */
	private JButton getJButtonOk() {
		if (okBtn == null) {
			okBtn = new nc.ui.pub.beans.UIButton();
			okBtn.setText(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413007_0","01413007-0097")/*@res "参照"*/); //"参 照"
			if (this.getParent() instanceof DialogRefListener) {
				((DialogRefListener) this.getParent()).setRefDialogAndRefOper(this, okBtn);
				if (((DialogRefListener) this.getParent()).getRefOper() != okBtn)
					okBtn.setEnabled(false);
			}
			okBtn.addActionListener(this);
		}
		return okBtn;
	}

	/**
	 * 得去选择的关键字
	 * 
	 * @return
	 */
	public ChooseKeyVO getRefVO() {
		return selKeyVO;
	}

	/**
	 * 取到全部的关键字,初始化tablemodel
	 *
	 * @create by liuchuna at 2011-5-18,上午10:40:24
	 *
	 */
	private void initTableModel() {
		tablemodel = new KeyWordTableModel(vos);
	}

	/**
	 * 此处插入方法描述。 创建日期：(2003-3-5 15:20:36)
	 *
	 * @param event
	 *            java.awt.event.ActionEvent
	 */
	protected void innerActionPerformed(java.awt.event.ActionEvent event) {
		if (event.getSource() == okBtn) {
			int selIndex = table.getSelectedRow();
			selKeyVO = tablemodel.getSelRow(selIndex);
			setResult(ID_OK);
			close();
		}
		else {
			setResult(ID_CANCEL);
			close();
		}
	}

	public class KeyWordTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 4132353172793854147L;

		//可以参照的关键字集合
		private Vector<ChooseKeyVO> m_RefKeyVec = new Vector<ChooseKeyVO>();

		public KeyWordTableModel(ChooseKeyVO[] vos) {
			if(m_RefKeyVec.size()>0) {
				m_RefKeyVec.removeAllElements();
			}
			m_RefKeyVec.addAll(Arrays.asList(vos));
		}
		public int getColumnCount() {
			return colunmNames.length;
		}
		public String getColumnName(int col) {
			return colunmNames[col];
		}
		public int getRowCount() {
			return m_RefKeyVec.size();
		}
		public ChooseKeyVO getVO(int index) {
			return (ChooseKeyVO) m_RefKeyVec.get(index);
		}
		public boolean isCellEditable(int row, int col) {
			return false;
		}
		//返回选用的关键字所在的行
		public ChooseKeyVO getSelRow(int index) {
			return (ChooseKeyVO) m_RefKeyVec.get(index);
		}
		public void setValueAt(Object obj, int row, int column) {
			switch (column) {
			case 0 :
				break;
			case 1 :
				break;

			default :
				break;
			}
		}
		public Object getValueAt(int row, int column) {
			ChooseKeyVO vo = m_RefKeyVec.get(row);
			switch (column) {
			case 0 :
				return vo.getCode();
			case 1 :
				return vo.getName1();
				    
			default :
				break;
			}
			return null;
		}
	}

}
