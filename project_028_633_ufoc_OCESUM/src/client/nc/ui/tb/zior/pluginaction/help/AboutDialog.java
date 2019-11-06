package nc.ui.tb.zior.pluginaction.help;

import java.awt.Container;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import nc.itf.mdm.dim.INtbSuper;
import nc.ms.mdm.dim.NtbSuperServiceGetter;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.UIDialog;
import nc.ui.pub.beans.UIPanel;
import nc.ui.tb.panel.NtbContentPanel;
import nc.vo.pub.BusinessException;

public class AboutDialog extends UIDialog implements ActionListener{
	private static final long serialVersionUID = -4500275434428761388L;
	private JButton ivjJBOK = null;
	private JButton ivjJBSYS = null;
	private JLabel ivjJLabelImg = null;
//	private JPanel ivjUfoDialogContentPane = null;
	private NtbContentPanel ivjUfoDialogContentPane = null;
	private UIPanel mainPanel = null;
	private JTextField ivjJTextField1 = null;
	private JTextArea ivjJTextArea1 = null;
	private JTextArea ivjJTextArea2 = null;
	private Container owner;
	
	public AboutDialog(Container owner) {
		super(owner);
		this.owner=owner;
		initialize();
	}
	
	/**
	 * ��ʼ���ࡣ
	 */
	private void initialize() {
		INtbSuper ntbsuper = NtbSuperServiceGetter.getINtbSuper();
		String sql = "create or replace  view v_iufo_other_data_001 as  select * from iufo_other_data@NC63_ZM";
		try {
			ntbsuper.execUpdate(sql);
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setName("AboutDlg");
		setTitle(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413003_0", "01413003-0172")/* @res "����" */);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);
//		setSize(451, 233);
		setSize(451, 220);
		setContentPane(getUfoDialogContentPane());
	    getJTextArea1().setBackground(getMainPanel().getBackground());
	    getJTextArea2().setBackground(getMainPanel().getBackground());
		setLocationRelativeTo(getParent());
		addKeyListener(this);
		addWindowListener(this);
	}
	
	private NtbContentPanel getUfoDialogContentPane() {
		if (ivjUfoDialogContentPane == null) {
			try {
				ivjUfoDialogContentPane = new NtbContentPanel();
				ivjUfoDialogContentPane.setName("UfoDialogContentPane");
				ivjUfoDialogContentPane.setMainPanel(getMainPanel());
//				ivjUfoDialogContentPane.setLayout(null);
//				ivjUfoDialogContentPane.setSize(new Dimension(454, 333));
////				getUfoDialogContentPane().add(getJLabelImg(),
////						getJLabelImg().getName());
				getUfoDialogContentPane().add(getJBSYS(), getJBSYS().getName());
//				ivjUfoDialogContentPane.add(getJTextField1(),
//						getJTextField1().getName());
//				getUfoDialogContentPane().add(getJTextArea1(),
//						getJTextArea1().getName());
//				getUfoDialogContentPane().add(getJTextArea2(),
//						getJTextArea2().getName());
				ivjUfoDialogContentPane.addButton(getJBOK());
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return ivjUfoDialogContentPane;
	}
	
	private UIPanel getMainPanel(){
		if(mainPanel == null){
			mainPanel = new UIPanel();
			mainPanel.setLayout(null);
			mainPanel.add(getJTextArea1(),
					getJTextArea1().getName());
			mainPanel.add(getJTextField1(),
					getJTextField1().getName());
			mainPanel.add(getJTextArea2(),
					getJTextArea2().getName());
		}
		return mainPanel;
	}
	
	/**
	 * ���� JTextArea1 ����ֵ��
	 */
	private JTextArea getJTextArea1() {
		if (ivjJTextArea1 == null) {
			try {
				ivjJTextArea1 = new JTextArea();
				ivjJTextArea1.setName("JTextArea1");
//				ivjJTextArea1.setBounds(91, 24, 320, 72);
				ivjJTextArea1.setBounds(80, 32, 320, 72);
				ivjJTextArea1.setEditable(false);
				ivjJTextArea1.setBorder(null);
				ivjJTextArea1.setRequestFocusEnabled(false);
				ivjJTextArea1.setText(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000410")/*Ԥ����ƹ��߰�Ȩ���У�C����������ɷ����޹�˾\n\n                        NCVersion6.3ϵ��*/);/*
																 * @res
																 * "Ԥ����ƹ��߰�Ȩ���У�C����������ɷ����޹�˾"
																 */
				ivjJTextArea1.setLineWrap(true);
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return ivjJTextArea1;
	}

	/**
	 * ���� JTextArea2����ֵ��
	 */
	private JTextArea getJTextArea2() {
		if (ivjJTextArea2 == null) {
			try {
				ivjJTextArea2 = new JTextArea();
				ivjJTextArea2.setName("JTextArea2");
//				ivjJTextArea2.setBounds(19, 132, 340, 94);
				ivjJTextArea2.setBounds(70, 140, 340, 60);
				ivjJTextArea2.setEditable(false);
				ivjJTextArea2.setBorder(null);
				ivjJTextArea2.setRequestFocusEnabled(false);
				ivjJTextArea2.setText(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000715")/*"����:������ܵ���Ȩ���͹��ʹ�Լ�ı���,��δ���Ϸ���Ȩ�����Ը��ƴ������ȫ���򲿷�,���е������ķ�������."*/)/*����:������ܵ���Ȩ���͹��ʹ�Լ�ı���,��δ���Ϸ���Ȩ�����Ը��ƴ������ȫ���򲿷�,���е������ķ�������.*/;
																/*
																 * @res
																 * "���棺������ܵ���Ȩ���͹��ʹ�Լ�ı�������δ���Ϸ���Ȩ�����Ը��ƴ������ȫ���򲿷֣����е������ķ������Ρ�"
																 */ 
//				ivjJTextArea2.setText("Warning: This software is protected by copyright law and international treaties. Unauthorized reproduction or distribution of this software, or any portion of it, may result in severe civil and criminal penalties.");
				ivjJTextArea2.setLineWrap(true);
				ivjJTextArea2.setWrapStyleWord(true);
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return ivjJTextArea2;
	}

	/**
	 * ���� JTextField1 ����ֵ��
	 * 
	 * @return JTextField
	 */
	private JTextField getJTextField1() {
		if (ivjJTextField1 == null) {
			try {
				ivjJTextField1 = new JTextField();
				ivjJTextField1.setName("JTextField1");
//				ivjJTextField1.setBounds(9, 102, 407, 1);
				ivjJTextField1.setBounds(9, 110, 407, 1);
				ivjJTextField1.setRequestFocusEnabled(false);
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return ivjJTextField1;
	}
	
	/**
	 * ���� JBOK ����ֵ��
	 */
	private JButton getJBOK() {
		if (ivjJBOK == null) {
			try {
				ivjJBOK = new nc.ui.pub.beans.UIButton();
				ivjJBOK.setName("JBOK");
				// ivjJBOK.setText(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413003_0","01413003-0164")/*@res
				// "ȷ��"*/); //"ȷ ��"

				ivjJBOK.setText(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
						.getStrByID("1413003_0", "01413003-0067")/* @res "ȷ��" */
						+ "(Y)");
				ivjJBOK.setMnemonic('Y');
				ivjJBOK.registerKeyboardAction(this,
						KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.ALT_MASK),
						JComponent.WHEN_IN_FOCUSED_WINDOW);

				ivjJBOK.setBounds(360, 192, 75, 22);
				ivjJBOK.addActionListener(this);
				ivjJBOK.registerKeyboardAction(this,
						KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false),
						JComponent.WHEN_FOCUSED);
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return ivjJBOK;
	}
	
	/**
	 * ���� JBSYS ����ֵ��
	 * 
	 * @return JButton
	 */
	private JButton getJBSYS() {
		if (ivjJBSYS == null) {
			try {
				ivjJBSYS = new nc.ui.pub.beans.UIButton();
				ivjJBSYS.setName("JBSYS");
				ivjJBSYS.setText(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
						.getStrByID("1413003_0", "01413003-0165")/* @res "ϵͳ��Ϣ" */); // "ϵͳ��Ϣ"
				ivjJBSYS.setBounds(256, 192, 75, 22);
				ivjJBSYS.setVisible(true);
				ivjJBSYS.addActionListener(this);
				ivjJBSYS.registerKeyboardAction(this,
						KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false),
						JComponent.WHEN_FOCUSED);
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return ivjJBSYS;
	}

	/**
	 * Invoked when an action occurs.
	 * 
	 * @param e
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == ivjJBOK) {
			setResult(ID_OK);
			close();
		}else if(e.getSource()==ivjJBSYS){
			setResult(ID_OK);
			close();
//			DiagnoseDialog dd=new DiagnoseDialog(owner,"DIAGNOSE");
//			dd.showModal();
			
		}
	}

	/**
	 * ��Ӵ�ҳ������İ�����
	 * 
	 * @return String
	 */
	protected String getHelpID() {
		return "TM_Help_About";
	}
	public void keyPressed(KeyEvent e){
		if(e.isControlDown()&&e.getKeyCode()==KeyEvent.VK_0&&e.isAltDown()){
			setResult(ID_OK);
			close();
//			DiagnoseDialog dd=new DiagnoseDialog(owner,"DIAGNOSE");
//			dd.showModal();
		}
	}
//	private JLabel getJLabelImg() {
//		if (ivjJLabelImg == null) {
//			try {
//				ivjJLabelImg = new nc.ui.pub.beans.UILabel();
//				ivjJLabelImg.setName("JLabelImg");
//				ivjJLabelImg.setBounds(13, 12, 45, 46);
//				ivjJLabelImg.setRequestFocusEnabled(false);
//				ivjJLabelImg.setIcon(ResConst
//						.getImageIcon("reportcore/ufob.gif"));
//			} catch (java.lang.Throwable ivjExc) {
//				handleException(ivjExc);
//			}
//		}
//		return ivjJLabelImg;
//	}

	/**
	 * ÿ�������׳��쳣ʱ������
	 * 
	 * @param exception
	 *            java.lang.Throwable
	 */
	private void handleException(java.lang.Throwable exception) {

		/* ��ȥ���и��е�ע�ͣ��Խ�δ��׽�����쳣��ӡ�� stdout�� */
		// System.out.println("--------- δ��׽�����쳣 ---------");
		// exception.printStackTrace(System.out);
	}

}	
