package nc.ui.glcom.reckoning;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;

import nc.bs.framework.common.NCLocator;
import nc.itf.iufo.servive.IControlAccountService;
import nc.itf.org.IOrgConst;
import nc.pubitf.para.SysInitQuery;
import nc.ui.pub.beans.UIButton;
import nc.ui.pub.beans.UIDialog;
import nc.ui.pub.beans.UILabel;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UISplitPane;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.param.CheckResultVO;
import nc.vo.pub.param.TempParamVO;

import com.ufida.iufo.pub.tools.AppDebug;

public class CheckDialog  extends UIDialog implements ActionListener{

	private static final long serialVersionUID = 3657561282620894292L;
	
	/*private UILabel upMonthLabel1 = new UILabel("人员大于10%请输入备注：    ");
	private UILabel onLineLabel1 = new UILabel( "在岗人员大于10%请输入备注：");
	
	private UILabel ysLabel1 = new UILabel("应收账款压降数 ：    ");
	private UILabel moreysLabel1 = new UILabel( "一年以上应收账款压降数：");
	
	
	private UILabel kcLabel1 = new UILabel("库存压降数 ：");
	private UILabel morekcLabel1 = new UILabel( "一年以上库存压降数：");
	private UILabel abnormalkcLabel1 = new UILabel( "一年以上库存压降数：");
	
	private UITextField infoField = new UITextField();
	
	private UITextField onLineField = new UITextField();
	
	private UITextField ysField = new UITextField();
	private UITextField moreysField = new UITextField();
	
	
	private UITextField kcField = new UITextField();
	private UITextField morekcField = new UITextField();
	private UITextField abnormalkcField = new UITextField();*/
	
	private UIButton queryBtn = new UIButton("确定");
	
	UIPanel centerPanel =new UIPanel();
	
	private UIButton canceltBtn = new UIButton("取消");
	
	private int checkIsOk = 1;
	
	
//	private UIPanel 
	
	
	public CheckDialog(TempParamVO params) throws BusinessException{
		setSize(new Dimension(500, 400));
		setResizable(true);
		this.setLayout(new BorderLayout());
		UIPanel buttonPanel = new UIPanel(new FlowLayout(FlowLayout.RIGHT));
		queryBtn.addActionListener(this);
		buttonPanel.add(queryBtn);
		canceltBtn.addActionListener(this);
		buttonPanel.add(canceltBtn);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.add(buttonPanel,BorderLayout.SOUTH);
		this.add(centerPanel);
		centerPanel.setLayout(new BorderLayout());
	
//		centerPanel.add()
		
		UISplitPane sPane = new UISplitPane(JSplitPane.VERTICAL_SPLIT, getUpMonthPanel( params), getTwoGoldPanel( params));
		sPane.setDividerLocation(130);
//		sPane.setAlignmentX(0.5f);
//		centerPanel.add();
//		centerPanel.add();
		centerPanel.add(sPane);
		centerPanel.updateUI();
		
	}
	
	private UIPanel getUpMonthPanel(TempParamVO params) throws BusinessException{
		
//		UISplitPane sPane = new UISplitPane(JSplitPane.VERTICAL_SPLIT, getUpMonthPanel( params), getTwoGoldPanel( params));
//		sPane.setDividerLocation(130);
		
		UIPanel mainPanel = new UIPanel();
		mainPanel.setLayout(new BorderLayout());
		
		UIPanel upMonthPanel = new UIPanel(new GridLayout(3,1));
		
		boolean isEditPersion = true ;
		UILabel perInfoLabel = null;
		IControlAccountService accService = NCLocator.getInstance().lookup(IControlAccountService.class);
		List<CheckResultVO> result = accService.checkKB(params);
//		if(!flag){
//			nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,"企业财务快报1，人数没录入请录入数据");
//		}
		for(CheckResultVO vo : result){
			perInfoLabel =  new UILabel(vo.getMsg());
			perInfoLabel.setBackground(Color.BLUE);
			upMonthPanel.add(perInfoLabel);
			if(!vo.isCheckflag()){
				
				checkIsOk = 0;
				
			}
//			else{
//				if(m_nResult!=0){
//					m_nResult = 1;
//				}
				
//			}
		}
	/*	
		if(isEditPersion){
			perInfoLabel =  new UILabel("人员录入完成!");
			perInfoLabel.setBackground(Color.BLUE);
		}else{
			perInfoLabel =  new UILabel("快报人员未录入!");
			perInfoLabel.setBackground(Color.red);
		}
		
		upMonthPanel.add(perInfoLabel);
		
//		upMonthPanel.add(upMonthLabel1);
		UIPanel rowPanel = new UIPanel(new BorderLayout());
		rowPanel.add(upMonthLabel1,BorderLayout.WEST);
		rowPanel.add(infoField);
//		infoField.set`
		upMonthPanel.add(rowPanel);
		
		
		UIPanel twoPanel = new UIPanel(new BorderLayout());
		twoPanel.add(onLineLabel1,BorderLayout.WEST);
		twoPanel.add(onLineField);
//		infoField.set`
		upMonthPanel.add(twoPanel);*/
		
		upMonthPanel.setBorder(BorderFactory.createTitledBorder("企业财务快报一"));
		mainPanel.add(upMonthPanel);
		
		UIPanel downPanel = new UIPanel();
		downPanel.add(new UILabel());
		downPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		downPanel.setBorder(BorderFactory.createTitledBorder("内部采购未对外出售存货（无库龄）"));
		
		List<CheckResultVO> checkN03B = accService.checkN03B(params);
		for(CheckResultVO vo : checkN03B){
			downPanel.add(new UILabel(vo.getMsg()));
			if(!vo.isCheckflag()){
				checkIsOk = 0;
			}
//			else{
//				 
//				m_nResult = 1;
//			}
		}
		mainPanel.add(downPanel,BorderLayout.SOUTH);
		return mainPanel;
		
	}
	
	
	private UIPanel getTwoGoldPanel(TempParamVO params) throws BusinessException{
		UIPanel twoGoldPanel = new UIPanel(new GridLayout(5,1));
		UILabel perInfoLabel = null;
		IControlAccountService accService = NCLocator.getInstance().lookup(IControlAccountService.class);
		List<CheckResultVO> result = accService.checkPressureControl(params);
	
		for(CheckResultVO vo : result){
			perInfoLabel =  new UILabel(vo.getMsg());
			perInfoLabel.setBackground(Color.BLUE);
			twoGoldPanel.add(perInfoLabel);
			if(!vo.isCheckflag()){
				checkIsOk = 0;
			}
//			else{
//				m_nResult = 1;
//			}
		}
		
		twoGoldPanel.setBorder(BorderFactory.createTitledBorder("中央企业“两金”压控统计表"));
		return twoGoldPanel;
	}
	
	private UIPanel getN03BPanel(TempParamVO params) throws BusinessException{
		UIPanel twoGoldPanel = new UIPanel(new GridLayout(1,1));
		UILabel perInfoLabel = null;
		IControlAccountService accService = NCLocator.getInstance().lookup(IControlAccountService.class);
		List<CheckResultVO> result = accService.checkN03B(params);
	
		for(CheckResultVO vo : result){
			perInfoLabel =  new UILabel(vo.getMsg());
			perInfoLabel.setBackground(Color.BLUE);
			twoGoldPanel.add(perInfoLabel);
			if(!vo.isCheckflag()){
				checkIsOk = 0;
			}else{
				checkIsOk = 1;
			}
		}
		
		twoGoldPanel.setBorder(BorderFactory.createTitledBorder("内03表"));
		return twoGoldPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(queryBtn)){
			try {
				if(isForceControl()){
					if(checkIsOk == 1){
						this.closeOK();	
					}else{
						this.close();	
					}
				}else{
					this.closeOK();	
				}
			} catch (BusinessException e1) {
				e1.printStackTrace();
			}
		
//			centerPanel.removeAll();
//			UISplitPane sPane = new UISplitPane(JSplitPane.VERTICAL_SPLIT, getUpMonthPanel(), getTwoGoldPanel());
//			sPane.setDividerLocation(130);
//			sPane.setAlignmentX(0.5f);
//			centerPanel.add();
//			centerPanel.add();
//			centerPanel.add(sPane);
//			centerPanel.updateUI();
//			return ;
		}
		if(e.getSource().equals(canceltBtn)){
			this.close();	
		}
	}
	
	@Override
	public int showModal() {
		return	 super.showModal();
	}
	
	public boolean isForceControl() throws BusinessException{
		Map<String, UFBoolean> paraValueMap = new HashMap<String,UFBoolean>();
		List<String> pk_orgs = new ArrayList<String>();
		pk_orgs.add(IOrgConst.GLOBEORG); 
		try {
			paraValueMap = SysInitQuery.getBatchParaBoolean(pk_orgs.toArray(new String[pk_orgs.size()]), "IUFO403");
		} catch (BusinessException e) {
			AppDebug.debug(e);
			throw new BusinessException();
		}
		if(paraValueMap == null || paraValueMap.isEmpty()){
			return true;
		}
		UFBoolean forceFlag = paraValueMap.get(IOrgConst.GLOBEORG);
		return forceFlag.booleanValue();
	}
}
