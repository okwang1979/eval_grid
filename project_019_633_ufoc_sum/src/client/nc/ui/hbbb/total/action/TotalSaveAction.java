package nc.ui.hbbb.total.action;

import java.awt.event.ActionEvent;

import com.ufida.iufo.pub.tools.DateUtil;

import nc.bs.framework.common.NCLocator;
import nc.funcnode.ui.action.AbstractNCAction;
import nc.itf.hbbb.total.IHbTotalSchemeServer;
import nc.pub.iufo.basedoc.UserUtil;
import nc.ui.hbbb.total.TotalSchemeManageUI;
import nc.vo.hbbb.total.HbTotalOrgTreeVO;
import nc.vo.hbbb.total.HbTotalSchemeVO;
import nc.vo.pub.lang.UFDateTime;

public class TotalSaveAction extends AbstractNCAction{
	 
	
	
		private TotalSchemeManageUI ui;
		
		
		public TotalSaveAction(TotalSchemeManageUI ui){
			super("save","±£´æ","±£´æ");
			this.ui = ui;
		}


		@Override
		public void actionPerformed(ActionEvent e) {
			
			
			HbTotalOrgTreeVO seletcVO = ui.getSelect() ;
			HbTotalSchemeVO scheme = null;
			if(seletcVO.getScheme()==null){
				scheme = new HbTotalSchemeVO();
				scheme.setApp_org(seletcVO.getPk_org());
				scheme.setPk_rms(ui.getCurrent_rcs());
				scheme.setPk_rmsversion(ui.getCurrent_svid());
				scheme.setCreator(UserUtil.getCurrentUser());
				scheme.setCreationtime(new UFDateTime(DateUtil.getCurTime()));
			
				
				
			}else{
				scheme = seletcVO.getScheme();
				scheme.setModifier(UserUtil.getCurrentUser());
				scheme.setModifiedtime(new UFDateTime(DateUtil.getCurTime()));
			}
			
			IHbTotalSchemeServer server = NCLocator.getInstance().lookup(IHbTotalSchemeServer.class);
			
			scheme.setTotalType(ui.getTotalType());
			String pk = server.saveOrUpdateScheme(scheme);
			scheme.setPk_hbscheme(pk);
			seletcVO.setScheme(scheme);
		
			ui.treeNodeSelected(seletcVO);
			ui.setState(TotalSchemeManageUI.INIT);
			 
			ui.refreshMenuActions();
			ui.updateUI();
			
		}
}
