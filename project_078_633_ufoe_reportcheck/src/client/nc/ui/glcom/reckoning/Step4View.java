package nc.ui.glcom.reckoning;

/**
 * �˴���������˵����
 * �������ڣ�(2001-11-8 9:17:25)
 * @author��������
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
	 * �汾����
	 */
	private static final long serialVersionUID = 837536945105360440L;
	
	private SeparatorButtonObject m_nullButton = new SeparatorButtonObject();

	private ButtonObject m_reckButton = new ButtonObject(
			nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
					"UPP20021510-000116")/* @res "����" */,
			nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
					"UPP20021510-000116")/* @res "����" */, 2, "����"); /*-=notranslate=-*/

	private ButtonObject m_firstButton = new ButtonObject(
			nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
					"UPP20021510-000112")/* @res "�ײ�" */,
			nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
					"UPP20021510-000112")/* @res "�ײ�" */, 2, "�ײ�"); /*-=notranslate=-*/

	private ButtonObject m_lastButton = new ButtonObject(
			nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
					"UPP20021510-000080")/* @res "��һ��" */,
			nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
					"UPP20021510-000080")/* @res "��һ��" */, 2, "��һ��"); /*-=notranslate=-*/

	private ButtonObject[] m_arryCurrentButtons = new ButtonObject[3];

	private Step4Ui ivjStep4Ui1 = null;

	IParent m_parent;

	IData m_data;

	private Step4Model m_step4model = null;

	// �����˲�����(2005-01-17)
	private String m_pk_glorgbook;

	/**
	 * Step4View ������ע�⡣
	 */
	public Step4View() {
		super();
		initialize();
	}

	/***************************************************************************
	 * ����: ���UiManager����ǰ��Ĺ���ģ��Ҫ������ģ�� ��ĳЩ�¼���������ͨ���÷�����ӡ�
	 *
	 * ����: Object objListener ������ Object objUserdata ��ʶǰ���Ǻ��ּ�����
	 *
	 * ����ֵ: ��
	 *
	 * ע�� �÷�����ʵû�й̶���Ҫ��ֻҪ�����ߺͱ����� ��֮����ڸõ��õ����Э�飬���Ϳ�ʹ�øù���
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
	 * ���� Step4Ui1 ����ֵ��
	 *
	 * @return nc.ui.glcom.reckoning.Step4Ui
	 */
	/* ���棺�˷������������ɡ� */
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
	 * ����ʵ�ָ÷���������ҵ�����ı��⡣
	 *
	 * @version (00-6-6 13:33:25)
	 *
	 * @return java.lang.String
	 */
	public String getTitle() {
		return nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
				"UPP20021510-000057")/* @res "��ĩ����" */;
	}

	/**
	 * ÿ�������׳��쳣ʱ������
	 *
	 * @param exception
	 *            java.lang.Throwable
	 */
	private void handleException(java.lang.Throwable exception) {

		/* ��ȥ���и��е�ע�ͣ��Խ�δ��׽�����쳣��ӡ�� stdout�� */
		Logger.error(exception);
	}

	private void informRecked() {
		nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,
				nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
						"UPP20021510-000145",null,new String[]{getStep4Model().getResultVo().getPresentYear(),getStep4Model().getResultVo().getPresentMonth()})/* @res "{0}��{1}��" */
				+ nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
						"UPP20021510-000117")/* @res "ûͨ���������,���ܽ���!" */);
	}

	/**
	 * ��ʼ���ࡣ
	 */
	/* ���棺�˷������������ɡ� */
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
	 * ����: ���UiManager����ǰ��Ĺ���ģ����Ҫ���ñ�ģ ���ĳ�����������ĳ�����ܣ�������ͨ���÷��� �ﵽ��һĿ��
	 *
	 * ����: Object objData ��Ҫ���ݵĲ�������Ϣ Object objUserData ��Ҫ���ݵı�ʾ����Ϣ
	 *
	 * ����ֵ: Object
	 *
	 * ע�� �÷�����ʵû�й̶���Ҫ��ֻҪ�����ߺͱ����� ��֮����ڸõ��õ����Э�飬���Ϳ�ʹ�øù���
	 **************************************************************************/
	public java.lang.Object invoke(java.lang.Object objData,
			java.lang.Object objUserData) {
		return null;
	}

	/**
	 * ����ڵ� - ��������ΪӦ�ó�������ʱ���������������
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
			System.err.println("nc.ui.pub.ToftPanel �� main() �з����쳣");
			nc.bs.logging.Logger.error(exception.getMessage(), exception);
		}
	}

	/***************************************************************************
	 * ����: ��A����ģ�����B����ģ������B����ģ��ر�ʱ A����ģ��ͨ���÷����õ�֪ͨ
	 *
	 * ����: �� ����ֵ: ��
	 **************************************************************************/
	public void nextClosed() {
	}

	/**
	 * ����ʵ�ָ÷�������Ӧ��ť�¼���
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
							"UPP20021510-000116")/* @res "����" */)) {
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
				// ����ʽ��Ʒ�Ƿ���ˣ�2004-05-11 add by wdg��
				//���ʽ��ʲ���Ҫ����ʽ��Ƿ���� modify by Liyongru for V55 at 20081111
				/*else if (!getStep4Model().isFundSettled()) {
					nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,nc.ui.ml.NCLangRes.getInstance()
							.getStrByID("20021510", "UPP20021510-000118")
																			 * @res
																			 * "�ʽ������δ����,���˲��ܽ���!"
																			 );
				}*/
				// ��鱾���Ƿ��ڳ����� (2004-05-13 add by wdg)
				else if (!getStep4Model().isInitBuilt()) {
					nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,getStep4Model().getResultVo()
							.getPresentYear()
							+ nc.ui.ml.NCLangRes.getInstance().getStrByID(
									"20021510", "UPP20021510-000119")/*
																		 * @res
																		 * "�����δ�ڳ����ˣ����ܽ���!"
																		 */);
				} else if (getStep4Model().isExistRegulationVoucherUnTallied()) {
					nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,nc.ui.ml.NCLangRes.getInstance()
							.getStrByID("20021510", "UPP20021510-000120")/*
																			 * @res
																			 * "���ڼ估��ǰ�ڼ����δ���˵ĵ�����ƾ֤,���ܽ���!"
																			 */);
				}
//				}else if(!getStep4Model().checkColseAccbook()){
//					nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,"���ڼ������֤ʧ�ܣ��޷����ˡ�");
//				}
				else if (verifyRecked()) {
					// hurh ����ǰ���˴���
					// getStep4Model().closeAccBook();
					getStep4Model().reckoning();
					IUiPanel temp = (IUiPanel) m_parent.showFirst();
					temp.invoke(null, "refresh");
					showHintMessage(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002GL502","UPP2002GL502-000106")/*@res "���˳ɹ�!"*/);
					Thread thread=new Thread(new Runnable() {
					    @Override
					    public void run() {
					    	  AppDebug.error("��ʼ=======���㣬��ˣ��ϱ�======");
					    	  String action  = "not begin";
					      try {
					    	  ICaculateCheckSubmitService service = NCLocator.getInstance().lookup(ICaculateCheckSubmitService.class);
//					    	  TempParamVO params = service.getParams(getStep4Model().getResultVo().getPk_accountingbook(), getStep4Model().getResultVo().getPresentYear(), getStep4Model().getResultVo().getPresentMonth());
					    	  action = "start caculate";
					    	  String jobId = service.caculate(params);
					    	  AppDebug.debug("�������,��ʼ�������.job is:"+jobId);
					    	  action = "start check";
					    	  CheckResultVO[] checkResults =  NCLocator.getInstance().lookup(ICaculateCheckSubmitService.class).check(params, jobId);
					    	  AppDebug.debug("����������.");
					    	  for(CheckResultVO result : checkResults){
					    		  if(result.getCheckState()!=3){
					    			  AppDebug.error("������˲�ͨ��,δ�ύ����.");  
					    			  int i=1;
					    			  for(CheckDetailVO detail: result.getDetailVO()){
					    				  AppDebug.error("�����Ϣ("+i+"):"+detail.toString());  
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
					      AppDebug.error("����=======���㣬��ˣ��ϱ�======");
					    }
					  });
					  thread.start();
				}
				
			} else if (bo.getName().equals(
					nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
							"UPP20021510-000080")/* @res "��һ��" */)) {
				m_parent.closeMe();
			} else if (bo.getName().equals(
					nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510",
							"UPP20021510-000112")/* @res "�ײ�" */)) {
				m_parent.showFirst();
			}
		} catch (java.rmi.RemoteException rmE) {
			nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,rmE.getMessage());
		} catch (Exception ee) {
			nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,ee.getMessage());
		}
		

	}

	/***************************************************************************
	 * ����: ȥ��ĳ��������
	 *
	 * ����: Object objListener ������ Object objUserdata ��ʶǰ���Ǻ��ּ�����
	 *
	 * ����ֵ: ��
	 *
	 * ע�� �÷�����ʵû�й̶���Ҫ��ֻҪ�����ߺͱ����� ��֮����ڸõ��õ����Э�飬���Ϳ�ʹ�øù���
	 **************************************************************************/
	public void removeListener(java.lang.Object objListener,
			java.lang.Object objUserdata) {
	}

	/***************************************************************************
	 * ����: ���UiManagerҪ��ʾĳһ������ģ�飬������� ��ģ���showMe�����������ʾ����
	 *
	 * ����: IParent parent ����ģ�����UiManager�е�ĳЩ ���ݵĵ��ýӿ�ʵ���� ����ֵ: ��
	 **************************************************************************/

	public void showMe(IParent p_parent) {
		p_parent.getUiManager().removeAll();
		p_parent.getUiManager().add(this, this.getName());
		m_parent = p_parent;
	}

	private boolean verifyRecked() {
		int iResult = showYesNoMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021510", "UPP20021510-000121", null, new String[] {
				getStep4Model().getResultVo().getPresentYear(), getStep4Model().getResultVo().getPresentMonth()
		})/* @res "���Ҫ����{0}��{1}�½�����" */);
		return iResult == 4;
	}

	/**
	 * �˴����뷽��˵���� �������ڣ�(2001-12-24 13:29:41)
	 */
	public void freeLock() {
		// ����
		try {
			//TODO ClientEvnironment��Ч
			GLKeyLock.freeKey(getStep4Model().getResultVo().getStrPk(),
					GlWorkBench.getLoginUser(),
					"sm_createcorp");

		} catch (Exception e) {
			nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,nc.ui.ml.NCLangRes.getInstance().getStrByID(
					"20021510", "UPP20021510-000081")/* @res "����ʱ�����쳣 " */
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