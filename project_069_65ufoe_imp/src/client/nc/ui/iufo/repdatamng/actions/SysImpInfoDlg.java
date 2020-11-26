package nc.ui.iufo.repdatamng.actions;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import com.ufsoft.iuforeport.tableinput.applet.IRepDataParam;

import nc.ui.pub.beans.UIButton;
import nc.ui.pub.beans.UIDialog;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UIScrollPane;
import nc.ui.pub.beans.UISplitPane;
import nc.ui.pub.beans.UITable;
import nc.util.info.sysimp.NCConnTool;
import nc.util.info.sysimp.SysImpUtil;
import nc.vo.uif2.LoginContext;

public class SysImpInfoDlg extends UIDialog implements ActionListener,SysImpUpdataUI{

	private UIPanel centerPanel = new UIPanel();

	private SysImpInfoTableModel model = new SysImpInfoTableModel();

	private UITable table = new UITable();
	
	
	private UIButton cancelBtn   = new UIButton("停止");
	
//	private  ui = new SysImpUpdataUI() {
//		
//		@Override
//		public void upUi(Object... param) {
//			// TODO Auto-generated method stub
//			
//		}
//	};
	private  boolean end = true;
	
	
//	public SysImpInfoDlg(Container parent, String title){
//		super(parent,title);
//		
//	}
 

	public SysImpInfoDlg(LoginContext context) {
		super(context.getEntranceUI());
	
		this.setTitle("推送返回信息");
		table.setModel(model);
		UIScrollPane scroll = new UIScrollPane(table);
		this.setLayout(new BorderLayout());
		this.add(scroll);
		table.getColumnModel().getColumn(0).setPreferredWidth(300);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(300);
		UIPanel buttonPanel = new UIPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(cancelBtn);
		cancelBtn.addActionListener(this);
		this.setSize(700, 200);
	}

	public void run(final SysImpExecutor executor) {
		
		
		new Thread(){

			@Override
			public void run() {
				try {
//					SysImpSendResultInfo info = new SysImpSendResultInfo("", code);
					executor.runImp(SysImpInfoDlg.this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}.start();
		
//		while(isEnd()){
//			try {
//				Thread.sleep(3*1000);
//			} catch (InterruptedException e) {
//				 
//			}
//			 
//		}
//		
//		this.closeOK();
	}
	
	public void addRow(SysImpSendResultInfo info){
		model.getInfos().add(info);
		this.table.updateUI();
		
	}
	public SysImpSendResultInfo getCurrentInfo(){
		if(model.getInfos().isEmpty()){
			return null;
		}else{
			return model.getInfos().get(model.getInfos().size()-1);
		}
		
	}

	public boolean isEnd() {
		return end;
	}

	public void setEnd(boolean end) {
		this.end = end;
	}

	@Override
	public void actionPerformed(ActionEvent paramActionEvent) {
		 
		
	}

	@Override
	public void upUi(Object... param) {
		
		
		if(param[0] instanceof String){
			this.setTitle("推送返回信息---完成");
		}
		Map<String,String> rtn  = (Map<String, String>)param[0];
//		if(model.getInfos().size()>0){
//			model.getInfos().get(model.getInfos().size()-1);
//		}
		
		SysImpSendResultInfo info = new SysImpSendResultInfo(rtn.get("report_name"));
		if("1".equals(rtn.get(NCConnTool.KEY_CODE))){
			info.setErrCode("推送成功！");
			info.setInfo("success");
		}else{
			info.setErrCode("推送失败！");
			info.setInfo("ErrCode:"+rtn.get(NCConnTool.KEY_CODE)+"info:"+rtn.get(NCConnTool.KEY_INFO));
		}
		
		model.getInfos().add(info);
		this.table.updateUI();
				
		
	}
	
	

}

class SysImpInfoTableModel extends DefaultTableModel {

	private List<SysImpSendResultInfo> infos = new ArrayList<>();

	private String[] columns = {"推送报表",  "结果", "服务器返回信息" };

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public String getColumnName(int index) {

		return columns[index];
	}
	
	

	@Override
	public int getRowCount() {
		if(infos==null)return 0;
		 return infos.size();
	}

	@Override
	public Object getValueAt(int row, int col) {

		SysImpSendResultInfo info = this.getInfos().get(row);
		if (info == null) {
			return "";
		}
		String rtn = "";
		switch (col) {
		case 0:
			rtn = info.getReport_name();
			break;
		case 1:
			rtn = info.getErrCode();
			break;
		case 2:
			rtn = info.getInfo();
			break;
		 

		 
		}
		return rtn;

	}

	public List<SysImpSendResultInfo> getInfos() {
		return infos;
	}

}
