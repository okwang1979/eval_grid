package nc.ui.glcom.reckoning;

/**
 * 此处插入类型说明。
 * 创建日期：(2001-11-8 9:17:25)
 * @author：李晓冬
 */
import java.awt.BorderLayout;
import java.util.LinkedList;
import java.util.List;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.gl.pub.GLKeyLock;
import nc.itf.iufo.servive.ICaculateCheckSubmitService;
import nc.ui.gl.gateway.glworkbench.GlWorkBench;
import nc.ui.glpub.IParent;
import nc.ui.glpub.IUiPanel;
import nc.ui.pub.ButtonObject;
import nc.ui.pub.SeparatorButtonObject;
import nc.ui.pub.ToftPanel;
import nc.vo.pub.BusinessException;
import nc.vo.pub.param.TempParamVO;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.iufo.check.vo.CheckDetailVO;
import com.ufsoft.iufo.check.vo.CheckResultVO;


public class Step4View extends ToftPanel implements IUiPanel {
	/**
	 * 版本序列
	 */
	private static final long serialVersionUID = 837536945105360440L;
	
	private SeparatorButtonObject m_nullButton = new SeparatorButtonObject();

	private ButtonObject m_reckButton = new ButtonObject(
			nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
					"UPP20021510-000116")/* @res "结账" */,
			nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
					"UPP20021510-000116")/* @res "结账" */, 2, "结账"); /*-=notranslate=-*/

	private ButtonObject m_firstButton = new ButtonObject(
			nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
					"UPP20021510-000112")/* @res "首步" */,
			nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
					"UPP20021510-000112")/* @res "首步" */, 2, "首步"); /*-=notranslate=-*/

	private ButtonObject m_lastButton = new ButtonObject(
			nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
					"UPP20021510-000080")/* @res "上一步" */,
			nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
					"UPP20021510-000080")/* @res "上一步" */, 2, "上一步"); /*-=notranslate=-*/

	private ButtonObject[] m_arryCurrentButtons = new ButtonObject[3];

	private Step4Ui ivjStep4Ui1 = null;

	IParent m_parent;

	IData m_data;

	private Step4Model m_step4model = null;

	// 主体账簿主键(2005-01-17)
	private String m_pk_glorgbook;

	/**
	 * Step4View 构造子注解。
	 */
	public Step4View() {
		super();
		initialize();
	}

	/***************************************************************************
	 * 功能: 如果UiManager或者前面的功能模块要监听本模块 的某些事件，它可以通过该方法添加。
	 *
	 * 参数: Object objListener 监听器 Object objUserdata 表识前面是何种监听器
	 *
	 * 返回值: 无
	 *
	 * 注： 该方法其实没有固定的要求，只要调用者和被调用 者之间存在该调用的相关协议，它就可使用该功能
	 **************************************************************************/
	public void addListener(java.lang.Object objListener,
			java.lang.Object objUserdata) {

		if (objUserdata.toString().equals("IData")) {
			m_data = (IData) objListener;
			getStep4Model().setResultData(m_data.getResultData());
		}
		getStep4Model().refreshPeriod();

	}

	/**
	 *
	 * @version (00-6-8 16:17:27)
	 *
	 * @return ButtonObject[]
	 */
	public ButtonObject[] getButtons() {
//		m_arryCurrentButtons[0] = m_reckButton;
//		m_arryCurrentButtons[1] = m_firstButton;
//		m_arryCurrentButtons[2] = m_lastButton;
		List<ButtonObject> btnList = new LinkedList<ButtonObject>();
		btnList.add(m_reckButton);
		btnList.add(getNullButton());
		btnList.add(m_firstButton);
		btnList.add(m_lastButton);
		m_arryCurrentButtons = btnList.toArray(new ButtonObject[0]);
		return m_arryCurrentButtons;
	}

	private Step4Model getStep4Model() {
		if (m_step4model == null) {
			m_step4model = new Step4Model();
		}
		return m_step4model;
	}

	/**
	 * 返回 Step4Ui1 特性值。
	 *
	 * @return nc.ui.glcom.reckoning.Step4Ui
	 */
	/* 警告：此方法将重新生成。 */
	private Step4Ui getStep4Ui1() {
		if (ivjStep4Ui1 == null) {
			try {
				ivjStep4Ui1 = new nc.ui.glcom.reckoning.Step4Ui();
				ivjStep4Ui1.setName("Step4Ui1");
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return ivjStep4Ui1;
	}

	/**
	 * 子类实现该方法，返回业务界面的标题。
	 *
	 * @version (00-6-6 13:33:25)
	 *
	 * @return java.lang.String
	 */
	public String getTitle() {
		return nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
				"UPP20021510-000057")/* @res "月末结账" */;
	}

	/**
	 * 每当部件抛出异常时被调用
	 *
	 * @param exception
	 *            java.lang.Throwable
	 */
	private void handleException(java.lang.Throwable exception) {

		/* 除去下列各行的注释，以将未捕捉到的异常打印至 stdout。 */
		Logger.error(exception);
	}

	private void informRecked() {
		nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,
				nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
						"UPP20021510-000145",null,new String[]{getStep4Model().getResultVo().getPresentYear(),getStep4Model().getResultVo().getPresentMonth()})/* @res "{0}年{1}月" */
				+ nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
						"UPP20021510-000117")/* @res "没通过工作检查,不能结账!" */);
	}

	/**
	 * 初始化类。
	 */
	/* 警告：此方法将重新生成。 */
	private void initialize() {
		try {
			setName("Step4View");
			setLayout(new BorderLayout());
			add(getStep4Ui1(), BorderLayout.CENTER);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
		getStep4Model().addPropertyChangeListener(getStep4Ui1());
	}

	/***************************************************************************
	 * 功能: 如果UiManager或者前面的功能模块需要调用本模 块的某个方法以完成某个功能，它可以通过该方法 达到这一目标
	 *
	 * 参数: Object objData 所要传递的参数等信息 Object objUserData 所要传递的表示等信息
	 *
	 * 返回值: Object
	 *
	 * 注： 该方法其实没有固定的要求，只要调用者和被调用 者之间存在该调用的相关协议，它就可使用该功能
	 **************************************************************************/
	public java.lang.Object invoke(java.lang.Object objData,
			java.lang.Object objUserData) {
		return null;
	}

	/**
	 * 主入口点 - 当部件作为应用程序运行时，启动这个部件。
	 *
	 * @param args
	 *            java.lang.String[]
	 */
	public static void main(java.lang.String[] args) {
		try {
			javax.swing.JFrame frame = new javax.swing.JFrame();
			Step4View aStep4View;
			aStep4View = new Step4View();
			frame.setContentPane(aStep4View);
			frame.setSize(aStep4View.getSize());
			frame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					System.exit(0);
				};
			});
			frame.show();
			java.awt.Insets insets = frame.getInsets();
			frame.setSize(frame.getWidth() + insets.left + insets.right, frame
					.getHeight()
					+ insets.top + insets.bottom);
			frame.setVisible(true);
		} catch (Throwable exception) {
			System.err.println("nc.ui.pub.ToftPanel 的 main() 中发生异常");
			nc.bs.logging.Logger.error(exception.getMessage(), exception);
		}
	}

	/***************************************************************************
	 * 功能: 在A功能模块调用B功能模块后，如果B功能模块关闭时 A功能模块通过该方法得到通知
	 *
	 * 参数: 无 返回值: 无
	 **************************************************************************/
	public void nextClosed() {
	}

	/**
	 * 子类实现该方法，响应按钮事件。
	 *
	 * @version (00-6-1 10:32:59)
	 *
	 * @param bo
	 *            ButtonObject
	 */
	public void onButtonClicked(nc.ui.pub.ButtonObject bo) {
		try {
			if (bo.getName().equals(
					nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
							"UPP20021510-000116")/* @res "结账" */)) {
				String year = getStep4Model().getResultVo().getPresentYear();
				String month =  getStep4Model().getResultVo().getPresentMonth();
				ICaculateCheckSubmitService service = NCLocator.getInstance().lookup(ICaculateCheckSubmitService.class);
				final TempParamVO params = service.getParams(getStep4Model().getResultVo().getPk_accountingbook(), year, month);

				CheckDialog dlg = new CheckDialog(params);
				int rtnValue = dlg.showModal();
//				
				if( 0 == rtnValue ){
					return ;
				}
				
				if (!getStep4Model().getResultVo().isIsReckoningble())
					informRecked();
				// 检查资金产品是否结账（2004-05-11 add by wdg）
				//总帐结帐不需要检查资金是否结帐 modify by Liyongru for V55 at 20081111
				/*else if (!getStep4Model().isFundSettled()) {
					nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,nc.ui.ml.NCLangRes.getInstance()
							.getStrByID("20021510", "UPP20021510-000118")
																			 * @res
																			 * "资金结算尚未结账,总账不能结账!"
																			 );
				}*/
				// 检查本年是否期初建账 (2004-05-13 add by wdg)
				else if (!getStep4Model().isInitBuilt()) {
					nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,getStep4Model().getResultVo()
							.getPresentYear()
							+ nc.ui.ml.NCLangRes.getInstance().getStrByID(
									"20021510", "UPP20021510-000119")/*
																		 * @res
																		 * "年度尚未期初建账，不能结账!"
																		 */);
				} else if (getStep4Model().isExistRegulationVoucherUnTallied()) {
					nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,nc.ui.ml.NCLangRes.getInstance()
							.getStrByID("20021510", "UPP20021510-000120")/*
																			 * @res
																			 * "本期间及以前期间存在未记账的调整期凭证,不能结账!"
																			 */);
				}
//				}else if(!getStep4Model().checkColseAccbook()){
//					nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,"本期间关账验证失败，无法结账。");
//				}
				else if (verifyRecked()) {
					// hurh 结账前关账处理
					// getStep4Model().closeAccBook();
					getStep4Model().reckoning();
					IUiPanel temp = (IUiPanel) m_parent.showFirst();
					temp.invoke(null, "refresh");
					showHintMessage(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002GL502","UPP2002GL502-000106")/*@res "结账成功!"*/);
					Thread thread=new Thread(new Runnable() {
					    @Override
					    public void run() {
					    	  AppDebug.error("开始=======计算，审核，上报======");
					    	  String action  = "not begin";
					      try {
					    	  ICaculateCheckSubmitService service = NCLocator.getInstance().lookup(ICaculateCheckSubmitService.class);
//					    	  TempParamVO params = service.getParams(getStep4Model().getResultVo().getPk_accountingbook(), getStep4Model().getResultVo().getPresentYear(), getStep4Model().getResultVo().getPresentMonth());
					    	  action = "start caculate";
					    	  String jobId = service.caculate(params);
					    	  AppDebug.debug("计算完成,开始检查任务.job is:"+jobId);
					    	  action = "start check";
					    	  CheckResultVO[] checkResults =  NCLocator.getInstance().lookup(ICaculateCheckSubmitService.class).check(params, jobId);
					    	  AppDebug.debug("检查任务完成.");
					    	  for(CheckResultVO result : checkResults){
					    		  if(result.getCheckState()!=3){
					    			  AppDebug.error("报表审核不通过,未提交任务.");  
					    			  int i=1;
					    			  for(CheckDetailVO detail: result.getDetailVO()){
					    				  AppDebug.error("检查信息("+i+"):"+detail.toString());  
					    				  i++;
					    			  }
					    			  
					    			  return ;
					    		  }
					    	  }
					    	  action = "start submit";
					    	  service.submit(params, jobId);
					      } catch (Exception e) {
					    	  AppDebug.error("==Action is:"+action+"message:"+e.getMessage());
					    	  AppDebug.error(e);
				 
						}
					      AppDebug.error("结束=======计算，审核，上报======");
					    }
					  });
					  thread.start();
				}
				
			} else if (bo.getName().equals(
					nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
							"UPP20021510-000080")/* @res "上一步" */)) {
				m_parent.closeMe();
			} else if (bo.getName().equals(
					nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
							"UPP20021510-000112")/* @res "首步" */)) {
				m_parent.showFirst();
			}
		} catch (java.rmi.RemoteException rmE) {
			nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,rmE.getMessage());
		} catch (Exception ee) {
			nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,ee.getMessage());
		}
		

	}

	/***************************************************************************
	 * 功能: 去除某个监听器
	 *
	 * 参数: Object objListener 监听器 Object objUserdata 标识前面是何种监听器
	 *
	 * 返回值: 无
	 *
	 * 注： 该方法其实没有固定的要求，只要调用者和被调用 者之间存在该调用的相关协议，它就可使用该功能
	 **************************************************************************/
	public void removeListener(java.lang.Object objListener,
			java.lang.Object objUserdata) {
	}

	/***************************************************************************
	 * 功能: 如果UiManager要显示某一个功能模块，它会调用 该模块的showMe方法以完成显示功能
	 *
	 * 参数: IParent parent 功能模块访问UiManager中的某些 数据的调用接口实现类 返回值: 无
	 **************************************************************************/

	public void showMe(IParent p_parent) {
		p_parent.getUiManager().removeAll();
		p_parent.getUiManager().add(this, this.getName());
		m_parent = p_parent;
	}

	private boolean verifyRecked() {
		int iResult = showYesNoMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510", "UPP20021510-000121", null, new String[] {
				getStep4Model().getResultVo().getPresentYear(), getStep4Model().getResultVo().getPresentMonth()
		})/* @res "真的要进行{0}年{1}月结账吗？" */);
		return iResult == 4;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2001-12-24 13:29:41)
	 */
	public void freeLock() {
		// 解锁
		try {
			//TODO ClientEvnironment无效
			GLKeyLock.freeKey(getStep4Model().getResultVo().getStrPk(),
					GlWorkBench.getLoginUser(),
					"sm_createcorp");

		} catch (Exception e) {
			nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,nc.ui.ml.NCLangRes.getInstance().getStrByID(
					"20021510", "UPP20021510-000081")/* @res "解锁时发生异常 " */
					+ e.getMessage());
		}
	}

	public boolean onClosing() {
		try {
			GLKeyLock.freeKey(new nc.vo.glcom.para.LockedPara()
					.getLockedPK(getPK_GLOrgBook()), null, null);
			return true;
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			return true;
		}
	}

	public java.lang.String getPK_GLOrgBook() {
		if (m_pk_glorgbook == null) {
			//m_pk_glorgbook = ((AccountingBookVO) getClientEnvironment()
				//	.getInstance().getValue(ClientEnvironment.GLORGBOOKPK)).getPk_accountingbook();
		}
		return m_pk_glorgbook;
	}
	
	private SeparatorButtonObject getNullButton() {
		m_nullButton.setName("");
		m_nullButton.setCode("");
		return m_nullButton;
	}
}