package com.ufsoft.iuforeport.reporttool.dialog;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;

import com.borland.jbcl.layout.VerticalFlowLayout;
import com.ufsoft.report.sysplugin.xml.ExtNameFileFilter;
import com.ufsoft.report.sysplugin.xml.ZipNameFileFilter;

import nc.impl.iufo.utils.NCLangUtil;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.UIButton;
import nc.ui.pub.beans.UICheckBox;
import nc.ui.pub.beans.UIDialog;
import nc.ui.pub.beans.UIFileChooser;
import nc.ui.pub.beans.UILabel;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UITextField;
import nc.ui.querytemplate.component.SeparationLine;


/**
 * excel文件选择对话框
 * @author wuyongc
 * @created at 2012-2-20,下午3:28:09
 *
 */
public class ExcelFileChooserDlg extends UIDialog implements ActionListener{

	private static final long serialVersionUID = 3963308377735063836L;
	private final Container m_Report;
	private int state;
	private UIFileChooser excelFileChooser;
	private UITextField selectedFileName;
	private UIButton selectedButton;
	private UICheckBox checkAutoCal;

	private UIButton okBtn = null;
	private UIButton cancelBtn = null;

	private File selectedFile;

	private UIPanel mainPane = null;

	final private int WIDTH = 420;
	final private int HEIGH = 150;
	private UILabel taskLabel;
	/**
	 * @i18n report00001=选择Excel数据文件
	 */
	public ExcelFileChooserDlg(Container parent,boolean bWithTaskPanel) {
		super(parent, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413003_0","01413003-0393")/*@res "选择Excel数据文件"*/);
		m_Report = parent;

		setSize(WIDTH, HEIGH);
		getContentPane().add(getMainPanel(bWithTaskPanel));
		setLocationRelativeTo(parent);
	}

	public ExcelFileChooserDlg(Container parent){
		this(parent,true);
	}
	private UIPanel getMainPanel(boolean bWithTaskPanel){
		if(mainPane == null){
			mainPane = new UIPanel();
			mainPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
			mainPane.setLayout(new VerticalFlowLayout(VerticalFlowLayout.LEFT,5, 0, true, false));
			if(bWithTaskPanel)
				mainPane.add(getTaskPanel());
			mainPane.add(getImportExcelPane());

			UIPanel autoCalPanel = new UIPanel(new FlowLayout(FlowLayout.LEFT,5,5));
//			autoCalPanel.add(getCheckAutoCal());
			mainPane.add(autoCalPanel);
			mainPane.add(getButtonPane());
		}
		return mainPane;
	}

	/**
	 * @create by wuyongc at 2012-2-20,下午3:33:50
	 *
	 * @return
	 */
	private UIPanel getTaskPanel() {
		UIPanel taskPanel = new UIPanel(new FlowLayout(FlowLayout.LEFT));
		taskPanel.add(new UILabel(NCLangUtil.getStrByID("1820001_0", "01820002-0065"/*任务：*/)));
		taskPanel.add(getTaskLabel());
		return taskPanel;
	}

	/**
	 * @create by wuyongc at 2012-2-20,下午3:38:52
	 *
	 * @return
	 */
	public UILabel getTaskLabel() {
		if(taskLabel ==null){
			taskLabel = new UILabel();
		}
		return taskLabel;
	}

	private UIPanel getImportExcelPane() {
		UIPanel importExcelPanel = new UIPanel(new FlowLayout(FlowLayout.LEFT));
		if (excelFileChooser == null) {
			excelFileChooser = new UIFileChooser();
			//begin pzm 
			ZipNameFileFilter xf = new ZipNameFileFilter("zip");
			excelFileChooser.setFileFilter(xf);
//			excelFileChooser.addChoosableFileFilter(new ExtNameFileFilter("rar"));
			//end
			excelFileChooser.setMultiSelectionEnabled(false);
		}
		if (selectedFileName == null) {
			importExcelPanel.add(new UILabel("导入文件："));
			selectedFileName = new UITextField();
			selectedFileName.setPreferredSize(new Dimension(200, 20));

			selectedFileName.setShowMustInputHint(true);
		}
		if (selectedButton == null) {
			selectedButton = new UIButton(
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413003_0","01413003-0395")/*@res "浏览.."*/);
			selectedButton.setPreferredSize(new Dimension(80, 20));
			selectedButton.setSize(120, 20);
			selectedButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					state = excelFileChooser.showOpenDialog(m_Report);
					File selectedFile = excelFileChooser.getSelectedFile();
					if (selectedFile != null
							&& state == JFileChooser.APPROVE_OPTION) {
						selectedFileName.setText(selectedFile.getPath());
					}
				}

			});
		}
		importExcelPanel.add(selectedFileName);
		importExcelPanel.add(selectedButton);

		return importExcelPanel;
	}



	/**
	 * @i18n report00002=导入后是否计算
	 */
	private UICheckBox getCheckAutoCal() {
		if (checkAutoCal == null) {
			checkAutoCal = new UICheckBox(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413003_0","01413003-0394")/*@res "导入后计算"*/);
		}
		return checkAutoCal;
	}

	/**
	 * @i18n report00003=浏览..
	 */



	private UIPanel getButtonPane() {
		UIPanel pal = new UIPanel(new VerticalFlowLayout(VerticalFlowLayout.LEFT, 0, 5, true, false));
		UIPanel btnPanel = new UIPanel(new FlowLayout(FlowLayout.TRAILING));
		SeparationLine spLine = new SeparationLine(5, 5);
		btnPanel.add(getOkBtn());
		btnPanel.add(getCancelBtn());

		pal.add(spLine);
		pal.add(btnPanel);
		return pal;
	}

	// @edit by wuyongc at 2011-7-6,下午06:23:26 取输入框内的值。
	public File getSelectedFile() {
		return selectedFile;
	}

	private UIButton getOkBtn() {
		if(okBtn == null){
			okBtn = new UIButton();
			okBtn.setText(NCLangRes.getInstance().getStrByID("common", "UC001-0000044") + "(Y)");
			okBtn.setMnemonic('Y');
			okBtn.addActionListener(this);
		}
		return okBtn;
	}
	private UIButton getCancelBtn() {
		if(cancelBtn == null){
			cancelBtn = new UIButton();
			cancelBtn.setText(nc.ui.ml.NCLangRes.getInstance().getStrByID("common", "UC001-0000008") + "(C)");
			cancelBtn.setMnemonic('C');
			cancelBtn.addActionListener(this);
		}
		return cancelBtn;
	}

	public boolean isAutoCal() {
		boolean isAutoCal = false;
		if (checkAutoCal != null) {
			isAutoCal = checkAutoCal.isSelected();
		}
		return isAutoCal;
	}
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == getOkBtn()){
			String pathStr = selectedFileName.getText();
			if(StringUtils.isEmpty(pathStr)){
				JOptionPane.showMessageDialog(getContentPane(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413003_0","01413003-0396")/*@res "请选择文件！"*/);
//				selectedFileName.setHitStr(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0943")/*@res "请点击浏览选择文件导出路径！"*/);
				selectedFileName.setShowWarning(true);
				getOkBtn().requestFocus();
				return;
			}
			selectedFile = new File(pathStr);
			if (!selectedFile.exists()) {
				JOptionPane.showMessageDialog(getContentPane(), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1413003_0","01413003-0397")/*@res "请选择正确的文件！"*/);
				return;
			}
			setResult(ID_OK);
			close();
		}else if(e.getSource() == getCancelBtn()){
			setResult(ID_CANCEL);
			close();
		}
	}
}

