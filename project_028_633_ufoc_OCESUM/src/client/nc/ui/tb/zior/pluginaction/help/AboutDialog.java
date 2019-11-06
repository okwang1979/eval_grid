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
	 * 初始化类。
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
		setTitle(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413003_0", "01413003-0172")/* @res "关于" */);
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
	 * 返回 JTextArea1 特性值。
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
				ivjJTextArea1.setText(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000410")/*预算编制工具版权所有（C）用友软件股份有限公司\n\n                        NCVersion6.3系列*/);/*
																 * @res
																 * "预算编制工具版权所有（C）用友软件股份有限公司"
																 */
				ivjJTextArea1.setLineWrap(true);
			} catch (java.lang.Throwable ivjExc) {
				handleException(ivjExc);
			}
		}
		return ivjJTextArea1;
	}

	/**
	 * 返回 JTextArea2特性值。
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
				ivjJTextArea2.setText(NCLangRes.getInstance().getStrByID("tbb_plan", "01812pln_000715")/*"警告:本软件受到版权法和国际公约的保护,若未经合法授权而擅自复制此软件的全部或部分,将承担严厉的法律责任."*/)/*警告:本软件受到版权法和国际公约的保护,若未经合法授权而擅自复制此软件的全部或部分,将承担严厉的法律责任.*/;
																/*
																 * @res
																 * "警告：本软件受到版权法和国际公约的保护，若未经合法授权而擅自复制此软件的全部或部分，将承担严厉的法律责任。"
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
	 * 返回 JTextField1 特性值。
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
	 * 返回 JBOK 特性值。
	 */
	private JButton getJBOK() {
		if (ivjJBOK == null) {
			try {
				ivjJBOK = new nc.ui.pub.beans.UIButton();
				ivjJBOK.setName("JBOK");
				// ivjJBOK.setText(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413003_0","01413003-0164")/*@res
				// "确认"*/); //"确 认"

				ivjJBOK.setText(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
						.getStrByID("1413003_0", "01413003-0067")/* @res "确定" */
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
	 * 返回 JBSYS 特性值。
	 * 
	 * @return JButton
	 */
	private JButton getJBSYS() {
		if (ivjJBSYS == null) {
			try {
				ivjJBSYS = new nc.ui.pub.beans.UIButton();
				ivjJBSYS.setName("JBSYS");
				ivjJBSYS.setText(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
						.getStrByID("1413003_0", "01413003-0165")/* @res "系统信息" */); // "系统信息"
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
	 * 添加此页面关联的帮助。
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
	 * 每当部件抛出异常时被调用
	 * 
	 * @param exception
	 *            java.lang.Throwable
	 */
	private void handleException(java.lang.Throwable exception) {

		/* 除去下列各行的注释，以将未捕捉到的异常打印至 stdout。 */
		// System.out.println("--------- 未捕捉到的异常 ---------");
		// exception.printStackTrace(System.out);
	}

}	
