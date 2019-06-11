package nc.ui.hbbb.meetaccount.view;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import nc.ui.hbbb.meetaccount.view.cellrender.NoteCellRender;
import nc.ui.hbbb.meetaccount.view.cellrender.TotalShowCellRender;
import nc.ui.pub.beans.UITable;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.pub.bill.BillItem;
import nc.ui.pub.bill.BillItemHyperlinkEvent;
import nc.ui.pub.bill.BillItemHyperlinkListener;
import nc.ui.pub.bill.BillListData;
import nc.ui.pub.bill.BillModel;
import nc.ui.uif2.AppEvent;
import nc.ui.uif2.components.AutoShowUpEventSource;
import nc.ui.uif2.components.IAutoShowUpComponent;
import nc.ui.uif2.components.IAutoShowUpEventListener;
import nc.ui.uif2.components.IComponentWithActions;
import nc.ui.uif2.components.ITabbedPaneAwareComponent;
import nc.ui.uif2.components.ITabbedPaneAwareComponentListener;
import nc.ui.uif2.components.TabbedPaneAwareCompnonetDelegate;
import nc.ui.uif2.editor.BillListView;
import nc.vo.hbbb.meetaccount.AggMeetRltHeadVO;
import nc.vo.hbbb.meetaccount.MeetResultHeadVO;

/**
 *
 * <p>
 * ���յ���ģ������ʾ����
 * </p>
 *
 * �޸ļ�¼��<br>
 * <li>�޸��ˣ��޸����ڣ��޸����ݣ�</li> <br>
 * <br>
 *
 * @see
 * @author wangxwb
 * @version V6.0
 * @since V6.0 ����ʱ�䣺2011-1-20 ����11:34:28
 */
public class MeetResultDetailEditor extends BillListView implements  IAutoShowUpComponent, ITabbedPaneAwareComponent, IComponentWithActions,BillItemHyperlinkListener{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private NoteCellRender cellRender=null;
	
	private TotalShowCellRender  totalcellrander =null;

	/*private static final String GLBANLANCE_NODE="20023005";*/
	
	private TabbedPaneAwareCompnonetDelegate tabbedPaneAwareCompnonetDelegate=  new TabbedPaneAwareCompnonetDelegate();

	private AutoShowUpEventSource autoShowUpEventSource =new AutoShowUpEventSource(this);;

	private List<Action> actions = null;

	private MeetResultRelaDialog reladialog = null ;
	//ȥ��˵����
	//private static final String NOTE_COLUMN=nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("common","UC000-0003660")/*@res "˵��"*/;

/*	private FuncRegisterVO  funRegVo1;

	private FuncRegisterVO  funRegVo2;*/


public void handleEvent(AppEvent event) {
		super.handleEvent(event);
	    //getBillListPanel().getHeadTable().getColumn(NOTE_COLUMN).setCellRenderer(this.getCellRender());

	}


	@Override
	public void bodyRowChange(BillEditEvent e) {
		Map<String, Object> valueMap = getBillListPanel().getBillListData().getHeadBillModel().getBodyRowValueByMetaData(e.getRow());
		if(valueMap != null && valueMap.get(MeetResultHeadVO.PK_TOTALINFO) != null) {
			@SuppressWarnings("rawtypes")
			List data = getModel().getData();
			if(data != null) {
				for (int i = 0; i < data.size(); i++) {
					AggMeetRltHeadVO vo = (AggMeetRltHeadVO) data.get(i);
					if(((MeetResultHeadVO)vo.getParentVO()).getPk_totalinfo().equals(valueMap.get(MeetResultHeadVO.PK_TOTALINFO))) {
	//					BillEditEvent  newEvent =
	//			            new BillEditEvent(e.getSource(), e.getOldValue(), e.getValue(),e.getKey(), i, e.getPos());
	//					newEvent.setOldrows(e.getOldrows());
	//					newEvent.setTableCode(e.getTableCode());
						int oldRow = (i == 0 ? 1 : 0);
						BillEditEvent event = new BillEditEvent(e.getSource(), oldRow, i);
						super.bodyRowChange(event);
						return;
					}
				}
			}
		}
	}


	@Override
	public void initUI() {
	    super.initUI();
	    reset();
//	    getBillListPanel().getBillListData().getHeadBillModel()
//	    getBillListPanel().setListData(getBillListPanel().getBillListData());s
	}


	public void reset() {
//		BillListData billListData = getBillListPanel().getBillListData();
//		getBillListPanel().setListData(billListData);
		getBillListPanel().getHeadItem(MeetResultHeadVO.DEBITAMOUNT).addBillItemHyperlinkListener(this);
	    getBillListPanel().getHeadItem(MeetResultHeadVO.DEBITAMOUNT).setListHyperlink(true);
	    getBillListPanel().getHeadItem(MeetResultHeadVO.CREDITAMOUNT).addBillItemHyperlinkListener(this);
	    getBillListPanel().getHeadItem(MeetResultHeadVO.CREDITAMOUNT).setListHyperlink(true);
	    /*TableCellMouseListener l = new TableCellMouseListener();
		getBillListPanel().getHeadTable().addMouseListener(l);
	    getBillListPanel().getHeadTable().addKeyListener(l);*/
//	    getBillListPanel().getHeadItem(MeetResultHeadVO.VOUCHERDEBITAMOUNT).addBillItemHyperlinkListener(this);
//	    getBillListPanel().getHeadItem(MeetResultHeadVO.VOUCHERCREDITAMOUNT).addBillItemHyperlinkListener(this);
	    String name = getBillListPanel().getBillListData().getHeadItem(MeetResultHeadVO.PK_SELFORG).getName();
	    getBillListPanel().getHeadTable().getColumn(name).setCellRenderer(getTotalCellRender());
	    getBillListPanel().getHeadTable().setSortEnabled(false);
	}
	class TableCellMouseListener extends MouseAdapter implements KeyListener {

//		private   boolean isctrl = false;
		public void mouseClicked(MouseEvent e) {

			if (e.getClickCount() == 1) {

				UITable table = getBillListPanel().getHeadTable();
				if (table.getRowCount() == 0)
					return;
				Point p = e.getPoint();
				int col = table.columnAtPoint(p);
				int row = table.rowAtPoint(p);
				if (col < 0 || row < 0)
					return;

				Object value = getBillListPanel().getHeadTable().getValueAt(row, col);

				BillItem[] items = null;
				BillModel bmodel = getBillListPanel().getHeadBillModel();
				if (bmodel != null)
					items = bmodel.getBodyItems();
				int convertColumnIndexToModel = table.convertColumnIndexToModel(col);
				BillItem selectedbillItem = items[convertColumnIndexToModel];
				if((selectedbillItem.getKey().equals(MeetResultHeadVO.DEBITAMOUNT)||MeetResultHeadVO.CREDITAMOUNT.equals(selectedbillItem.getKey()))/*&&isctrl*/){
					BillItemHyperlinkEvent event = new BillItemHyperlinkEvent(this, selectedbillItem, value, row);
					hyperlink(event);
//					isctrl=false;
				}
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {

		}

		@Override
		public void keyPressed(KeyEvent e) {
		/*	//�����ctrl����
			if(e.getKeyCode()==KeyEvent.VK_CONTROL){
				isctrl=true;
			}*/
//			char keyChar = e.getKeyChar();

		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub

		}

	}
	@Override
	protected void synchronizeDataFromModel() {
		super.synchronizeDataFromModel();
		setErrorVoucherColor();
	}

	public void setErrorVoucherColor() {
		BillModel bodyBillModel = billListPanel.getParentListPanel().getTableModel();
		int rowCount = bodyBillModel.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			Object obj = bodyBillModel.getBodyValueRowVO(i, MeetResultHeadVO.class.getName());
			if (obj != null) {
				MeetResultHeadVO vo = (MeetResultHeadVO) obj;
				if(!nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0332")/*@res "С��"*/.equals(vo.getNote()) && !nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("common","UC000-0001146")/*@res "�ϼ�"*/.equals(vo.getNote())){
					bodyBillModel.setForeground(Color.BLUE, i, billListPanel.getHeadBillModel().getBodyColByKey(MeetResultHeadVO.DEBITAMOUNT));
					bodyBillModel.setForeground(Color.BLUE, i, billListPanel.getHeadBillModel().getBodyColByKey(MeetResultHeadVO.CREDITAMOUNT));
//					bodyBillModel.setForeground(Color.BLUE, i, billListPanel.getHeadBillModel().getBodyColByKey(MeetResultHeadVO.VOUCHERDEBITAMOUNT));
//					bodyBillModel.setForeground(Color.BLUE, i, billListPanel.getHeadBillModel().getBodyColByKey(MeetResultHeadVO.VOUCHERCREDITAMOUNT));
				}
			}
		}
	}
	@Override
	public void addTabbedPaneAwareComponentListener(
			ITabbedPaneAwareComponentListener l) {
		tabbedPaneAwareCompnonetDelegate.addTabbedPaneAwareComponentListener(l);
	}

	@Override
	public boolean canBeHidden() {
		return tabbedPaneAwareCompnonetDelegate.canBeHidden();
	}

	@Override
	public boolean isComponentVisible() {
		return tabbedPaneAwareCompnonetDelegate.isComponentVisible();
	}

	@Override
	public void setComponentVisible(boolean visible) {
		tabbedPaneAwareCompnonetDelegate.setComponentVisible(visible);
	}

	@Override
	public void setAutoShowUpEventListener(IAutoShowUpEventListener l) {
		autoShowUpEventSource.setAutoShowUpEventListener(l);
	}

	@Override
	public void showMeUp() {
		autoShowUpEventSource.showMeUp();
	}

	@Override
	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	@Override
	public void hyperlink(BillItemHyperlinkEvent event){
//		Object valueObject = event.getItem().getValueObject();
		int row = event.getRow();
		
		Object valueAt = billListPanel.getHeadBillModel().getValueAt(row, MeetResultHeadVO.PK_TOTALINFO);
		if(valueAt==null){
			//�ϼ��в�����
			return;
		}
		BillModel bodyBillModel = billListPanel.getParentListPanel().getTableModel();
		MeetResultHeadVO valueVO = (MeetResultHeadVO) bodyBillModel.getBodyValueRowVO(row, MeetResultHeadVO.class.getName());

		//ȡ�ñ��Է���֯,�Լ��ؼ�����Ϣ
		Object[] datas = getModel().getData().toArray();
		MeetResultReverseUtil.linkQuery(valueVO, datas, getReladialog(), getModel().getContext());
	}

	public MeetResultRelaDialog getReladialog() {
		if(reladialog==null){
			reladialog = new MeetResultRelaDialog(getModel().getContext().getEntranceUI());
		}
		return reladialog;
	}

	public NoteCellRender getCellRender() {
		if(null==cellRender){
			cellRender=new NoteCellRender();
		}
		return cellRender;
	}
	
	public TotalShowCellRender getTotalCellRender() {
		if(null==totalcellrander){
			totalcellrander=new TotalShowCellRender(getBillListPanel().getHeadBillModel());
		}
		return totalcellrander;
	}


	public void setCellRender(NoteCellRender cellRender) {
		this.cellRender = cellRender;
	}


	/*@SuppressWarnings("unchecked")
	@Override
	protected void synchronizeDataFromModel() {
		List<Object> data = ((BillManageModel) getModel()).getData();
		setValue(data.toArray(new Object[0]));

		BillModel billModel = billCardPanel.getBillModel();
		int rowCount = billModel.getRowCount();
		for (int i = 0 ;i<rowCount;i++) {
			MeetResultBodyVO bodyvo = (MeetResultBodyVO) billModel.getBodyValueRowVO(i, MeetResultBodyVO.class.getName());
			//�跽
			if(null!= bodyvo.getDirection() && bodyvo.getDirection().intValue()==0){
				billModel.setValueAt(bodyvo.getMeet_amount(), i, "debitamount");
				billModel.setValueAt(bodyvo.getAdjust_amount(), i, "voucherdebitamount");
			}else{
				billModel.setValueAt(bodyvo.getMeet_amount(), i, "creditamount");
				billModel.setValueAt(bodyvo.getAdjust_amount(), i, "vouchercreditamount");
			}
		}
	}*/

}