package nc.ui.hbbb.total.action;

import java.awt.event.ActionEvent;

import nc.bs.framework.common.NCLocator;
import nc.funcnode.ui.action.AbstractNCAction;
import nc.itf.hbbb.total.IHbTotalSchemeServer;
import nc.ui.hbbb.total.TotalSchemeManageUI;
import nc.ui.pub.beans.MessageDialog;
import nc.vo.hbbb.total.HbTotalOrgTreeVO;
import nc.vo.hbbb.total.HbTotalSchemeVO;

public class TotalDelAction extends AbstractNCAction{
	
	private TotalSchemeManageUI ui;
	
	
	public TotalDelAction(TotalSchemeManageUI ui){
		super("del","ɾ��","ɾ��");
		this.ui = ui;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		HbTotalOrgTreeVO seletcVO = ui.getSelect() ;
	 
		if(seletcVO.getScheme()!=null){
			int oper  = MessageDialog.showYesNoDlg(ui, "ɾ����ʾ", "�Ƿ�ȡ����ǰ����ĺϲ����ܹ���");
			
			  if(oper == MessageDialog.ID_YES){
					
					IHbTotalSchemeServer server = NCLocator.getInstance().lookup(IHbTotalSchemeServer.class);
					server.deleleSchemeByPk(seletcVO.getScheme().getPk_hbscheme());
					seletcVO.setScheme(null);
					ui.treeSelectUpdataUi(ui.getSelect());
					this.setEnabled(false);
					ui.updateUI();
				}
			  }
		
		
	}

}
