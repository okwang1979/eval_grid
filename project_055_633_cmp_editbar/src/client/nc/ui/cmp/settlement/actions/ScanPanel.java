package nc.ui.cmp.settlement.actions;

import java.awt.Canvas;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nc.bs.logging.Logger;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.filesystem.SWTThread;
import nc.vo.pub.BusinessException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleClientSite;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * shiwla
 */
public class ScanPanel extends Canvas {
	/**
	 * �����촴active X�ؼ���������̳�Canvas�Է���UIDialog��
	 */
	private static final long serialVersionUID = 3861936053592909001L;
	public static final String NEW = "10000";
	public static final String Append = "10010";
	public static final String Browse = "11000";
	private String busiSerialNo;
	private String billNo;
	private String classID;
	private boolean isEdit;
	private String tradecode = null;
	// ����Ĵ���
	private Shell shell = null;
	// ����ActiveX������
	private OleClientSite site = null;
	// ִ��ActiveX�ķ���
	private OleAutomation auto = null;
	// ���ÿؼ��Ŀ��
	private OleFrame frame = null;

	// ���Կؼ���
	private static boolean isSuccess = false;

	public ScanPanel(String rootDirStr, boolean isEdit, String tradecode, String billNo) {
		super();
		this.busiSerialNo = rootDirStr;
		this.classID = getClassID(isEdit);
		this.tradecode = tradecode;
		this.isEdit = isEdit;
		this.billNo = billNo;
	}

	private String getClassID(boolean isEdit) {
		if (isEdit == false)
			// չʾҳ��ע�ᵽϵͳ�е�ID��
			return "{10DC279F-2E45-49F8-BD7E-A09D84F665F5}";
		else
			// ɨ��ҳ��ע�ᵽϵͳ�е�ID��
			return "{A5C98A27-3666-467A-9710-6E967F5BA1B2}";
	}

	public void tryPanel() throws BusinessException {
		if (isSuccess == false) {
			Display display = new Display();
			Shell shell = new Shell(display, SWT.NONE);
			OleFrame oleFrame = null;
			OleClientSite site = null;
			try {
				oleFrame = new OleFrame(shell, SWT.NONE);
				site = new OleClientSite(oleFrame, SWT.NONE, getClassID(true));
				site.doVerb(OLE.OLEIVERB_SHOW);
				// ScanParamUtil.getParamScan(WorkbenchEnvironment.getInstance().getGroupVO().getPk_group());
				isSuccess = true;
			} finally {
				if (oleFrame != null && !oleFrame.isDisposed()) {
					oleFrame.dispose();
				}
				if (site != null && !site.isDisposed()) {
					site.dispose();
				}
				if (shell != null && !shell.isDisposed()) {
					shell.dispose();
				}
			}
		}
	}

	/**
	 * ͨ������ Canvas ���ӵ�һ��������Ļ��Դʹ���Ϊ����ʾ�ġ� �˷����ɹ��߰��ڲ����ã���Ӧֱ���ɳ�����á�
	 */
	@Override
	public void addNotify() {
		super.addNotify();
		SWTThread.startThread();
		synchronized (SWTThread.class) {
			if (!SWTThread.getInstance().isStarted()) {
				try {
					SWTThread.class.wait();
				} catch (InterruptedException e) {
					Logger.error(e);
				}
			}
		}
		Runnable run = new Runnable() {
			@Override
			public void run() {
				initOleControl();
			}
		};
		getDisplay().syncExec(run);
	}

	public Display getDisplay() {
		return SWTThread.getInstance().getDisplay();
	}

	/**
	 * ��ʼ�����ؿؼ�,��dialog.showModel֮��ִ�и��߳�
	 * 
	 * @throws BusinessException
	 */
	protected void initOleControl() {
		try {
			Display display = getDisplay();
			shell = SWT_AWT.new_Shell(display, this);
			shell.setLayout(new FillLayout());
			shell.setSize(300, 200);
			OleFrame oleFrame = new OleFrame(shell, SWT.NONE);
			oleFrame.setSize(300, 200);
			setFrame(oleFrame);
			site = new OleClientSite(oleFrame, SWT.NONE, classID);
			if (site != null) {
				// ϵͳ��װ�ؼ�
				shell.open();
				shell.layout();
				auto = new OleAutomation(site);
				site.doVerb(OLE.OLEIVERB_SHOW);
				if (isEdit == true) {
					ScanEx(busiSerialNo, tradecode);
				} else {
					ShowImages(busiSerialNo);
				}
				Dimension d = getSize();
				shell.setSize(d.width, d.height);
			}
		} catch (Exception e) {
			Logger.error(e);
			dispose();
		}
	}

	/**
	 * ���ÿؼ���AsyncShowImages�������첽���ã��ȿؼ�������ɲ���Ӧ�ú���
	 */
	public boolean ShowImages(String busiSerialNo) {
		int mid[] = auto.getIDsOfNames(new String[] { "AsyncShowImages" });
		int siid = mid[0];
		if (siid < 0)
			return false;
		Variant[] rgvarg = new Variant[1];
		rgvarg[0] = new Variant(createXmlShow(busiSerialNo));
		try {
			Variant a = auto.invoke(siid, rgvarg);
			if (a.getString().contains("����Ӱ�����"))
				return false;
			return true;
		} catch (NullPointerException e) {
			// ���ú���ʧ��
			MessageDialog.showWarningDlg(null, nc.ui.ml.NCLangRes.getInstance().getStrByID("smcomm", "UPP1005-000070")/*
																													 * @
																													 * res
																													 * "����"
																													 */, "���ÿؼ�����ʧ��");
			Logger.error(e.getMessage(), e);
			return false;
		}
	}

	/*
	 * ����AsyncShowImages���������xml����������dom4j��������
	 */
	public String createXmlShow(String pk_jkbx) {
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GBK");
		Element root = doc.addElement("ZScanApp");
		root.addElement("AppSystemCode").addText("TRB");
		root.addElement("OrgCode").addText("20002");
		root.addElement("SubBankNo").addText("20002");
		root.addElement("TellerNo").addText("20002");
		root.addElement("Author").addText(WorkbenchEnvironment.getInstance().getLoginUser().getUser_name());
		root.addElement("AuthoritySet").addText("1");
		root.addElement("TermNo").addText("tty001");
		root.addElement("LocalIMG").addText("0");
		root.addElement("OutputDir").addText("c:/image");
		root.addElement("BatchID");
		root.addElement("BusinessSerialNo").addText(pk_jkbx);
		root.addElement("billNo").addText(billNo);
		return doc.asXML();
	}

	/**
	 * ���ÿؼ���AsyncScanEx�������첽���ã��ȿؼ�������ɲ���Ӧ�ú���
	 * 
	 * @throws BusinessException
	 */
	public boolean ScanEx(String pk_jkbx, String tradecode) throws BusinessException {
		// �õ�Ӱ��ɨ����� 1����ÿ��ɨ��һ�ŵ���
		String NoteStyle = "1";// ScanParamUtil.getParamScan(WorkbenchEnvironment.getInstance().getGroupVO().getPk_group());
		int mid[] = auto.getIDsOfNames(new String[] { "AsyncScanEx" });
		int scanexid = mid[0];
		if (scanexid < 0)
			return false;
		Variant[] rgvarg = new Variant[1];
		rgvarg[0] = new Variant(createXmlScan(pk_jkbx, tradecode, NoteStyle));
		try {
			Variant a = auto.invoke(scanexid, rgvarg);
			if (a.getString().contains("����Ӱ�����"))
				return false;
			return true;
		} catch (NullPointerException e) {
			// ���ú���ʧ��
			MessageDialog.showWarningDlg(null, nc.ui.ml.NCLangRes.getInstance().getStrByID("smcomm", "UPP1005-000070")/*
																													 * @
																													 * res
																													 * "����"
																													 */, "���ÿؼ�����ʧ��");
			Logger.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * ����AsyncScanEx���������xml����������dom4j��������
	 */
	public String createXmlScan(String pk_jkbx, String tradecode, String notestyle) {
		Document doc = DocumentHelper.createDocument();
		doc.setXMLEncoding("GBK");
		Element root = doc.addElement("ZScanApp");
		root.addElement("BusinessSerialNo").addText(pk_jkbx);
		root.addElement("billNo").addText(billNo);
		root.addElement("AppSystemCode").addText("TRB");

		Element tradecodes = root.addElement("TradeCodes");
		tradecodes.addElement("TradeCode").addText(tradecode);

		Element docnames = root.addElement("DocNames");
		docnames.addElement("DocName").addText("����");

		root.addElement("TradeType").addText("0");
		root.addElement("BusynessType").addText("1000");
		root.addElement("Channel").addText("3");
		root.addElement("OrgCode").addText("00080000");
		root.addElement("TellerNo").addText("1101111");
		root.addElement("FinanceCard").addText("0");
		root.addElement("BatchID");
		root.addElement("OutputDir").addText("C:/Program Files/gsip/ScanCMdrvOcx/Image");
		root.addElement("ImportIMG").addText("1");
		root.addElement("GroupType").addText("1");
		root.addElement("DateTime").addText("20120312121012");
		root.addElement("IP").addText("127.0.0.1");
		root.addElement("Port").addText("123455");
		root.addElement("Priority").addText("1");
		root.addElement("NoteStyle").addText(notestyle);
		root.addElement("UpdateParam").addText("0");
		return doc.asXML();
	}

	/**
	 * Ӱ�񱣴���ɺ󣬵��ô˺�����ʶ����ĵ���չʾ���б����
	 */
	public List<String> GetSerialnoList() {
		int mid[] = auto.getIDsOfNames(new String[] { "GetSerialnoList" });
		int getfilelistid = mid[0];
		if (getfilelistid < 0)
			return null;
		try {
			Variant a = auto.invoke(getfilelistid);
			return getAllNodes(a.getString());
		} catch (Exception e) {
			// ���ú���ʧ��
			MessageDialog.showWarningDlg(null, nc.ui.ml.NCLangRes.getInstance().getStrByID("smcomm", "UPP1005-000070")/*
																													 * @
																													 * res
																													 * "����"
																													 */, "���ÿؼ�����ʧ��");
			Logger.error(e.getMessage(), e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private List<String> getAllNodes(String xml) {
		List<String> result = new ArrayList<String>();
		try {
			Document document = DocumentHelper.parseText(xml);
			Element root = document.getRootElement();
			List list = root.elements("Serialno");
			for (Iterator it = list.iterator(); it.hasNext();) {
				Element elm = (Element) it.next();
				result.add(elm.getTextTrim());
			}
			return result;
		} catch (DocumentException e) {
			return null;
		}
	}

	public void dispose() {
		setVisible(false);
		if (getDisplay() == null)
			return;
		getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				if (!shell.isDisposed())
					shell.dispose();
				SWTThread.getInstance().dispose();
			}
		});

	}

	public void exec(Runnable run) {
		if (getDisplay() != null) {
			getDisplay().syncExec(run);
		}
	}

	public Shell getshell() {
		return shell;
	}

	public OleClientSite getSite() {
		return site;
	}

	public OleAutomation getAuto() {
		return auto;
	}

	public OleFrame getFrame() {
		return frame;
	}

	public void setFrame(OleFrame f) {
		this.frame = f;
	}

	public void SetFrameSize(int i, int j) {
		frame.setSize(i, j);
	}

	public void SetClientSize(int cx, int cy) {
		frame.setSize(cx, cy);
	}
}
