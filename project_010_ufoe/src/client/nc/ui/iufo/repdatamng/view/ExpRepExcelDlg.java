/**
 *
 */
package nc.ui.iufo.repdatamng.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import nc.bs.framework.common.NCLocator;
import nc.impl.iufo.utils.MultiLangTextUtil;
import nc.impl.iufo.utils.NCLangUtil;
import nc.itf.bd.userdefitem.IUserdefitemQryService;
import nc.md.MDBaseQueryFacade;
import nc.md.model.IAttribute;
import nc.md.model.IBean;
import nc.message.msgboard.recon.VerticalFlowLayout;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.ReportCache;
import nc.ui.bd.ref.AbstractRefModel;
import nc.ui.iufo.query.common.checkboxtable.CheckBoxColumnVO;
import nc.ui.iufo.query.common.checkboxtable.CheckBoxTable;
import nc.ui.iufo.repdatamng.model.IFlexible;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.UIButton;
import nc.ui.pub.beans.UICheckBox;
import nc.ui.pub.beans.UIComboBox;
import nc.ui.pub.beans.UIDialog;
import nc.ui.pub.beans.UIFileChooser;
import nc.ui.pub.beans.UILabel;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.beans.UIScrollPane;
import nc.ui.pub.beans.UITextField;
import nc.ui.pub.beans.constenum.DefaultConstEnum;
import nc.ui.querytemplate.component.SeparationLine;
import nc.utils.iufo.TaskRepStatusUtil;
import nc.vo.bd.userdefrule.UserdefitemVO;
import nc.vo.iufo.balance.BalanceCondVO;
import nc.vo.iufo.commit.CommitStateEnum;
import nc.vo.iufo.data.IKeyDetailData;
import nc.vo.iufo.data.KeyDetailDataUtil;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.RepExpParam;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.iufo.task.TaskVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.org.ReportOrgVO;
import nc.vo.pub.BusinessException;

import org.apache.commons.lang.StringUtils;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.iufo.check.vo.TaskRepStatusVO;
import com.ufsoft.iufo.excel.SaveFileClientUtil;
import com.ufsoft.iufo.func.excel.text.ImpExpFileNameUtil;
import com.ufsoft.report.sysplugin.xml.ExtNameFileFilter;

/**
 * 报表数据查询 导出Excel
 *
 * @author wuyongc
 * @created at 2011-12-28,上午10:37:36
 * 
 */
public class ExpRepExcelDlg extends UIDialog implements ActionListener, IFlexible {

	private static final long serialVersionUID = -3774558502559804020L;

	private final String REP_CODE = NCLangUtil.getStrByID("1820001_0", "01820001-0109")/* @res "报表编码" */;
	private final String REP_NAME = NCLangUtil.getStrByID("1820001_0", "01820001-0110")/* @res "报表名称" */;
	private final String SHEET_NAME_CONTAIN = NCLangUtil.getStrByID("1820001_0", "01820001-1409")/* @res "Sheet名称包含" */;
	private final String TO_ZIP_FILE = NCLangUtil.getStrByID("1820001_0", "01820001-0547")/* @res "压缩成Zip文件" */;

	private static final String EXPORT_BALANCE = NCLangUtil.getStrByID("1820001_0", "01820001-0037")/* @res "舍位" */;

	private UIPanel mainPanel;

	private UICheckBox repNameCheckBox;
	private UICheckBox repCodeCheckBox;
	private UICheckBox zipCheckBox;
	private UIPanel keyGroupCheckBoxPanel;
	private UILabel taskKeywordLabel;

	private List<UICheckBox> keyCheckBoxList = null;

	private UIComboBox blanceComboBox;

	private final UIButton okButton = new UIButton(NCLangUtil.getStrByID("1820001_0", "01820001-0029"));

	private final UIButton cancelButton = new UIButton(NCLangUtil.getStrByID("1820001_0", "01820001-0030"));

	final private int DIALOG_WIDTH = 650;

	final private int DIALOG_HEIGHT = 500;

	private CheckBoxTable checkBoxTable;

	private UIScrollPane uiScrollPanel;

	private UITextField excelFilePath;

	private UIButton selPathBtn;

	private UIFileChooser m_fileChooser;

	private String[] defaultFileNames;

	private UICheckBox saveAsSingleBox;

	private String pk_accscheme; // 报表数据查询功能限制确定如果有会计期间，那么一定 只有一个！

	private final Map<String, MeasurePubDataVO> alonePubDataMap = new HashMap<String, MeasurePubDataVO>();

	private final TaskVO task;

	private UICheckBox repCodeCheckBox4FileName;

	private UICheckBox repNameCheckBox4FileName;

	private UIRefPane refPane;

	private UICheckBox otherFileItem;

	private UIPanel keyGroupFileCheckBoxPanel;

	private final String FILE_NAME_CONTAIN = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0",
			"01820004-0105")/* @res "文件名称包含" */;

	private final String orgPK;

	private UIPanel seniorPanel;

	private List<UICheckBox> fileKeyCheckBoxList = null;

	private RepExpParam repExpParam;

	public ExpRepExcelDlg(Container parent, String title, TaskVO task, String orgPK) {
		super(parent);
		// 790 * 570
		this.setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		this.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
		this.task = task;
		this.orgPK = orgPK;
		getContentPane().add(getMainPanel());
		setTitle(title);
	}

	private UIPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new UIPanel(new VerticalFlowLayout(FlowLayout.LEFT, 5, 0, true, false));
			mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 14, 0, 14));
			uiScrollPanel = new UIScrollPane(getCheckBoxTable(), UIScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					UIScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			uiScrollPanel.setPreferredSize(new Dimension(DIALOG_WIDTH, 340));
			mainPanel.add(uiScrollPanel);
			mainPanel.add(getFileSelectPanel());
			// UIPanel seniorPanel = new UIPanel(new VerticalFlowLayout(FlowLayout.LEFT,5, 0, true, false));
			//
			// //构造名称的panel
			// seniorPanel.add(getSheetNamePanel());
			//
			// UIPanel otherPanel = getOtherPanel();
			// seniorPanel.add(otherPanel);

			FlexiblePanel flexiblePanel = new FlexiblePanel(this, getSeniorPanel(), true, NCLangUtil.getStrByID(
					"1820001_0", "01820001-0237"));

			mainPanel.add(flexiblePanel);

			UIPanel buttonPane = new UIPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));

			UIPanel bottomPanel = new UIPanel(new VerticalFlowLayout(FlowLayout.LEFT, 5, 5, true, false));

			bottomPanel.add(new SeparationLine(5, 5));
			bottomPanel.add(buttonPane);

			getContentPane().add(bottomPanel, BorderLayout.SOUTH);

			okButton.setText(NCLangRes.getInstance().getStrByID("common", "UC001-0000044") + "(Y)");
			okButton.setMnemonic('Y');

			cancelButton.setText(nc.ui.ml.NCLangRes.getInstance().getStrByID("common", "UC001-0000008") + "(C)");
			cancelButton.setMnemonic('C');

			okButton.addActionListener(this);
			buttonPane.add(okButton);
			getRootPane().setDefaultButton(okButton);

			cancelButton.addActionListener(this);
			buttonPane.add(cancelButton);
		}
		return mainPanel;
	}

	private UIPanel getOtherPanel() {
		UIPanel otherBalance = new UIPanel(new GridLayout(1, 3));

		UIPanel balancePanel = new UIPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		UILabel blanceLabel = new UILabel(EXPORT_BALANCE);
		balancePanel.add(blanceLabel);

		balancePanel.add(getBalanceComboBox());

		otherBalance.add(balancePanel);

		otherBalance.add(getSaveAsSingleCheckBox());

		UIPanel otherPanel = new UIPanel(new VerticalFlowLayout(FlowLayout.LEFT));
		otherPanel.setBorder(BorderFactory.createTitledBorder(NCLangUtil
				.getStrByID("1820001_0", "01820001-1443"/* 其他 */)));

		otherPanel.add(otherBalance);
		otherBalance.add(getZipCheckBox());
		return otherPanel;
	}

	public UICheckBox getSaveAsSingleCheckBox() {
		if (saveAsSingleBox == null) {
			saveAsSingleBox = new UICheckBox(NCLangUtil.getStrByID("1820001_0", "01820001-1450"/* "单一文件" */));
			saveAsSingleBox.setPreferredSize(new Dimension(200, 20));
			saveAsSingleBox.setSelected(true);
			getOtherFileItem().setEnabled(true);
//			getFileChooser().setSelectedFile(new File(""));
			// fileKeyCheckBoxList
			boolean bSing = true;
			getRepNameChkBox4FileName().setEnabled(!bSing);
			getRepCodeChkBox4FileName().setEnabled(!bSing);
			if (bSing) {// 如果是单一页签文件导出，则按照报表表样，关键字期间一起拼接为文件名称
				getRepCodeChkBox4FileName().setSelected(true);
			}
			if (!bSing) {
				getRepCodeChkBox4FileName().setSelected(false);
			}
			getOtherFileItem().setEnabled(!bSing);
			saveAsSingleBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getFileChooser().setSelectedFile(new File(getDefaultFileName()));
					// fileKeyCheckBoxList
					boolean bSing = saveAsSingleBox.isSelected();
					getRepNameChkBox4FileName().setEnabled(!bSing);
					getRepCodeChkBox4FileName().setEnabled(!bSing);
					if (bSing) {// 如果是单一页签文件导出，则按照报表表样，关键字期间一起拼接为文件名称
						getRepCodeChkBox4FileName().setSelected(true);
					}
					if (!bSing) {
						getRepCodeChkBox4FileName().setSelected(false);
					}
					for (UICheckBox checkBox : fileKeyCheckBoxList) {
						checkBox.setEnabled(!bSing);
						checkBox.setSelected(bSing);
					}
					getOtherFileItem().setEnabled(!bSing);
					// getFileChooser().setFileSelectionMode(bSing?JFileChooser.DIRECTORIES_ONLY:JFileChooser.FILES_AND_DIRECTORIES);
				}
			});
		}

		return saveAsSingleBox;
	}

	private String getDefaultFileName() {
		return getSaveAsSingleCheckBox().isSelected() ? defaultFileNames[0] : defaultFileNames[1];
	}

	/**
	 * @create by wuyongc at 2012-1-4,上午11:29:17
	 * 
	 * @return
	 */
	private UIComboBox getBalanceComboBox() {
		if (blanceComboBox == null) {
			blanceComboBox = new UIComboBox();
			blanceComboBox.setPreferredSize(new Dimension(110, 30));
		}
		return blanceComboBox;
	}

	/**
	 * @create by wuyongc at 2011-12-28,下午4:05:17
	 * 
	 * @return
	 */
	private UICheckBox getZipCheckBox() {
		if (zipCheckBox == null) {
			zipCheckBox = new UICheckBox(TO_ZIP_FILE);
			zipCheckBox.setPreferredSize(new Dimension(140, 20));
		}
		return zipCheckBox;
	}

	/**
	 * @create by wuyongc at 2011-12-28,下午2:54:58
	 * 
	 * @return
	 */
	// private UIPanel getSheetSequencePanel() {
	// UIPanel sheetSequencePanel = new UIPanel();
	// sheetSequencePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
	// sheetSequencePanel.add(new UILabel(SHEET_SEQUENCE));
	// sheetSequencePanel.add(getSheetOrderComboBox());
	// return sheetSequencePanel;
	// }

	public UILabel gettaskKeywordLabel() {
		if (taskKeywordLabel == null) {
			taskKeywordLabel = new UILabel();
			// taskKeywordLabel.setPreferredSize(new Dimension(500, 20));
		}
		return taskKeywordLabel;
	}

	/**
	 * @create by wuyongc at 2011-12-28,下午2:42:29
	 * 
	 * @param sheetNameLabelPanel
	 * @return
	 */
	private UIPanel getSheetNamePanel() {
		UIPanel sheetNamePanel = new UIPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

		sheetNamePanel.setBorder(BorderFactory.createTitledBorder(SHEET_NAME_CONTAIN));
		UIPanel repNameCodeCheckBoxPanel = new UIPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		repNameCodeCheckBoxPanel.add(getRepNameCheckBox());
		repNameCodeCheckBoxPanel.add(getRepCodeCheckBox());
		UIPanel sheetNameAssumeItem = new UIPanel(new VerticalFlowLayout());
		sheetNameAssumeItem.add(repNameCodeCheckBoxPanel);
		// repNameCodeCheckBoxPanel.setPreferredSize(new Dimension(300, 20));
		sheetNameAssumeItem.add(getKeyGroupCheckBoxPanel());
		sheetNamePanel.add(sheetNameAssumeItem);
		// sheetNamePanel.setPreferredSize(new Dimension(300, 90));
		return sheetNamePanel;
	}

	/**
	 * @create by wuyongc at 2011-12-28,下午4:15:42
	 * 
	 * @return
	 */
	private UIPanel getKeyGroupCheckBoxPanel() {
		if (keyGroupCheckBoxPanel == null) {
			keyGroupCheckBoxPanel = new UIPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		}

		return keyGroupCheckBoxPanel;
	}

	public void setKeyGroupCheckBoxPanel(KeyVO[] keys) {
		if (keyCheckBoxList == null) {
			keyCheckBoxList = new ArrayList<UICheckBox>();
			fileKeyCheckBoxList = new ArrayList<UICheckBox>();
		} else {
			keyCheckBoxList.clear();
			fileKeyCheckBoxList.clear();
		}
		UICheckBox checkBox = null;
		for (KeyVO key : keys) {
			checkBox = new UICheckBox(MultiLangTextUtil.getCurLangText(key));
			keyCheckBoxList.add(checkBox);
			getKeyGroupCheckBoxPanel().add(checkBox);

			checkBox = new UICheckBox(MultiLangTextUtil.getCurLangText(key));
			fileKeyCheckBoxList.add(checkBox);
			//begin pzm
			checkBox.setEnabled(false);
			checkBox.setSelected(true);
			//end
			getKeyGroupFileCheckBoxPanel().add(checkBox);
		}
	}

	private UICheckBox getRepNameCheckBox() {
		if (repNameCheckBox == null) {
			repNameCheckBox = new UICheckBox(REP_NAME);
		}
		return repNameCheckBox;
	}

	private UICheckBox getRepCodeCheckBox() {
		if (repCodeCheckBox == null) {
			repCodeCheckBox = new UICheckBox(REP_CODE);
			repCodeCheckBox.setSelected(true);
		}
		return repCodeCheckBox;
	}

	public void setTaskReports(RepDataQueryResultVO[] repQueryRs) {
		Vector<String> columnNames = new Vector<String>();
		columnNames.add(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0109")/*
																											 * @res
																											 * "报表编码"
																											 */);
		columnNames.add(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0110")/*
																											 * @res
																											 * "报表名称"
																											 */);
		columnNames.add(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0825")/*
																											 * @res
																											 * "关键字组合值"
																											 */);
		columnNames.add(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0892")/*
																											 * @res
																											 * "录入状态"
																											 */);
		// columnNames.add(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0823")/*@res
		// "报表审核状态"*/);
		// columnNames.add(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0822")/*@res
		// "任务审核状态"*/);
		columnNames.add(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0891")/*
																											 * @res
																											 * "报送状态"
																											 */);
		int size = repQueryRs.length;
		Vector<CheckBoxColumnVO> vector = new Vector<CheckBoxColumnVO>();

		ReportVO rep = null;

		Set<String> repList = new HashSet<String>();
		Set<String> aloneIdList = new HashSet<String>();

		for (RepDataQueryResultVO queryRs : repQueryRs) {
			repList.add(queryRs.getPk_report());
			aloneIdList.add(queryRs.getAlone_id());
		}

		alonePubDataMap.clear();

		TaskRepStatusVO[] taskRepStatusVOs = TaskRepStatusUtil.getTaskRepStatus(aloneIdList.toArray(new String[0]),
				repList.toArray(new String[0]));

		Map<String, TaskRepStatusVO> map = new HashMap<String, TaskRepStatusVO>(taskRepStatusVOs.length);
		for (TaskRepStatusVO taskRepStatus : taskRepStatusVOs) {
			if (taskRepStatus != null)
				map.put(taskRepStatus.getId(), taskRepStatus);
		}

		CheckBoxColumnVO columnVO = null;
		List<String> pks = new ArrayList<String>();
		String pk = null;
		String aloneId = null;
		final String inputText = NCLangUtil.getStrByID("1820001_0", "01820001-0720"/* @res "已录入" */);
		final String notInputText = NCLangUtil.getStrByID("1820001_0", "01820001-0222"/* @res "未录入" */);
		ReportCache repCache = IUFOCacheManager.getSingleton().getReportCache();
		for (int j = 0; j < size; j++) {
			rep = repCache.getByPK(repQueryRs[j].getPk_report());
			aloneId = repQueryRs[j].getAlone_id();
			if (!alonePubDataMap.containsKey(aloneId)) {
				alonePubDataMap.put(aloneId, repQueryRs[j].getPubData());
			}

			// 因为可能有不同的期间，所有把 aloneId 和报表pk拼起来作为主键，得到选中的记录时再解析。
			pk = repQueryRs[j].getAlone_id() + "@" + rep.getPk_report();
			columnVO = new CheckBoxColumnVO(pk);
			columnVO.setSelected(true);
			pks.add(pk);
			columnVO.getVector().add(rep.getCode());
			columnVO.getVector().add(rep.getChangeName());
			columnVO.getVector().add(getKeygroupValue(repQueryRs[j].getPubData(), repQueryRs[j].getPk_task()));

			TaskRepStatusVO taskRepStatusVO = map.get(pk);
			String inputStatus = taskRepStatusVO != null && taskRepStatusVO.getInputStatus() == 1 ? inputText
					: notInputText;
			columnVO.getVector().add(inputStatus);


			String commitStatus = CommitStateEnum.getCommitStateName(taskRepStatusVO == null ? 21 : taskRepStatusVO
					.getCommitStatus());
			columnVO.getVector().add(commitStatus);

			vector.add(columnVO);
		}
		getCheckBoxTable().initModel(columnNames.toArray(new String[0]), vector);
		getCheckBoxTable().setPKs(pks);
		getMainPanel().repaint();

	}

	/**
	 * @return the keygroupValue
	 */
	public String getKeygroupValue(MeasurePubDataVO pubData, String taskPK) {
		StringBuilder keywordGroupValue = new StringBuilder();
		if (pubData == null) {
			return null;
		} else {
			KeyVO[] keys = pubData.getKeyGroup().getKeys();
			String[] keyVals = pubData.getKeywords();
			IKeyDetailData keyDetailData = null;
			for (int i = 0; i < keys.length; i++) {
				keyDetailData = KeyDetailDataUtil.getKeyDetailData(keys[i], keyVals[i], getPk_accscheme(taskPK));
				if (keywordGroupValue.length() == 0) {
					keywordGroupValue.append(keyDetailData.getMultiLangText());
				} else
					keywordGroupValue.append(",").append(keyDetailData.getMultiLangText());
			}
		}
		return keywordGroupValue.toString();
	}

	// 填充报表信息到table的时候 记录下 pk_accscheme;
	private String getPk_accscheme(String taskPK) {
		if (pk_accscheme == null) {
			pk_accscheme = task.getPk_accscheme();
		}
		return pk_accscheme;
	}

	public CheckBoxTable getCheckBoxTable() {
		if (checkBoxTable == null) {
			checkBoxTable = new CheckBoxTable();
			checkBoxTable.setAutoscrolls(true);
		}
		return checkBoxTable;
	}

	/**
	 * @param balConds
	 *            the balConds to set
	 */
	public void setBalConds(BalanceCondVO[] balConds) {
		// this.balConds = balConds;

		UIComboBox box = getBalanceComboBox();

		List<DefaultConstEnum> vItem = new ArrayList<DefaultConstEnum>();
		vItem.add(new DefaultConstEnum(BalanceCondVO.NON_SW_DATA_COND_PK, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
				.getStrByID("1820001_0", "01820001-0038")/* @res "原表数据" */));

		if (balConds != null && balConds.length > 0) {
			Arrays.sort(balConds, new Comparator<BalanceCondVO>() {
				@Override
				public int compare(BalanceCondVO o1, BalanceCondVO o2) {
					return o1.getSwbit() - o2.getSwbit();
				}
			});

			for (BalanceCondVO balCond : balConds) {
				vItem.add(new DefaultConstEnum(balCond.getPk_balancecond(), balCond.getCondname()));
			}
		}
		box.removeAllItems();
		box.addItems(vItem.toArray());
	}

	private UIPanel getFileSelectPanel() {
		UIPanel fileSelectPanel = new UIPanel(new FlowLayout(FlowLayout.LEFT));
		fileSelectPanel.add(new UILabel("文件名")/* @res "Excel文件名" */);
		fileSelectPanel.add(getExcelFilePath());
		fileSelectPanel.add(getSelPathBtn());
		return fileSelectPanel;
	}

	/**
	 * @return the suitRepFileNameField
	 */
	public UITextField getExcelFilePath() {
		if (excelFilePath == null) {
			excelFilePath = new UITextField();
			excelFilePath.setPreferredSize(new Dimension(260, 22));
			excelFilePath.setEditable(false);
			excelFilePath.setShowMustInputHint(true);
		}
		return excelFilePath;
	}

	/**
	 * @return
	 */
	private UIButton getSelPathBtn() {
		if (selPathBtn == null) {
			selPathBtn = new UIButton(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0",
					"01820001-0500")/* @res "浏览..." */);
			selPathBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					UIFileChooser fileChooser = getFileChooser();
					//tiachuan 20150318 默认取上次选的路径，以及记录本次选取的路径
					String lastPath=SaveFileClientUtil.getLastSelExpDataPath();
					if(lastPath!=null && lastPath.length()>0){
						File lastPathFile=new File(lastPath);
						if(lastPathFile.exists()){
							fileChooser.setCurrentDirectory(lastPathFile);
						}
					}
					// @edit by wuyongc at 2013-9-3,下午2:53:00 如果文件名称那所有的选项都没有选择，则表示所有的内容导出到一个Excel文件中，这个时候可以选择文件，其它场合则选择目录
					boolean bSelectFileDir = getSaveAsSingleCheckBox().isSelected()	|| getRepNameChkBox4FileName().isSelected() || getRepCodeChkBox4FileName().isSelected();
					if (bSelectFileDir) {
						for (int i = 0; i < fileKeyCheckBoxList.size(); i++) {
							if (fileKeyCheckBoxList.get(i).isSelected()) {
								bSelectFileDir = true;
								break;
							}
						}
						if (bSelectFileDir) {
							bSelectFileDir = getOtherFileItem().isSelected() && refPane.getRefPK() == null;
						}
					}
//					fileChooser.setFileSelectionMode(bSelectFileDir ? JFileChooser.DIRECTORIES_ONLY: JFileChooser.FILES_AND_DIRECTORIES);



					if (fileChooser.showDialog(ExpRepExcelDlg.this, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes()
							.getStrByID("1820001_0", "01820001-0029")/* @res "确定" */) == UIFileChooser.APPROVE_OPTION) {
						if (getFileChooser().getFileSelectionMode() != JFileChooser.DIRECTORIES_ONLY) {
							SaveFileClientUtil.putLastSelExpDataPath(fileChooser.getSelectedFile().getParent());
						}else{	//如果只选路径，则直接把路径记录
							SaveFileClientUtil.putLastSelExpDataPath(fileChooser.getSelectedFile().getPath());
						}
						
						String filePath = fileChooser.getSelectedFile().getPath();
						if (getFileChooser().getFileSelectionMode() != JFileChooser.DIRECTORIES_ONLY) {
							ExtNameFileFilter extNameFileFilter = (ExtNameFileFilter) getFileChooser().getFileFilter();
							String extendName = extNameFileFilter.getExtendName();
							filePath = ImpExpFileNameUtil.getExcelFileName(filePath, extendName);
						}
						getExcelFilePath().setText(filePath);
						// 焦点定位于路径输入框 ,避免必输标志出现后，点击确定，设置了路径后，仍然出现必输的标志
						getExcelFilePath().requestFocus();
						getExcelFilePath().repaint();

					}
				}
			});
		}
		return selPathBtn;
	}

	private UIFileChooser getFileChooser() {
		if (m_fileChooser == null) {
			m_fileChooser = new UIFileChooser();

			if (m_fileChooser.getFileFilter() != null) {
				m_fileChooser.removeChoosableFileFilter(m_fileChooser.getFileFilter());
			}
			if (!StringUtils.isEmpty(getExcelFilePath().getText())) {
				m_fileChooser.setSelectedFile(new File(getExcelFilePath().getText() + ".xls"));
			} else {
				m_fileChooser.setSelectedFile(new File(getDefaultFileName()));
			}
			m_fileChooser.addChoosableFileFilter(new ExtNameFileFilter("xls"));
			m_fileChooser.addChoosableFileFilter(new ExtNameFileFilter("xlsx"));
			m_fileChooser.setMultiSelectionEnabled(false);
		}
		return m_fileChooser;
	}

	private UIPanel getSeniorPanel() {
		if (seniorPanel == null) {
			seniorPanel = new UIPanel(new VerticalFlowLayout(FlowLayout.LEFT, 5, 5, true, false));
			seniorPanel.add(getFileSheetNamePanel());
			seniorPanel.add(getOtherPanel());
			seniorPanel.setPreferredSize(new Dimension(600, 220));
		}
		return seniorPanel;
	}

	private UIPanel getFileSheetNamePanel() {
		UIPanel fileSheetNamePanel = new UIPanel(new GridLayout(1, 2));
		fileSheetNamePanel.add(getSheetNamePanel());
		fileSheetNamePanel.add(getFileNamePanel());
		return fileSheetNamePanel;
	}

	private Component getFileNamePanel() {

		UIPanel repNameCodeCheckBoxPanel = new UIPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

		repNameCodeCheckBoxPanel.add(getRepNameChkBox4FileName());
		repNameCodeCheckBoxPanel.add(getRepCodeChkBox4FileName());

		// repNameCodeCheckBoxPanel.add(refPane);

		UIPanel sheetNameAssumeItem = new UIPanel(new VerticalFlowLayout());
		sheetNameAssumeItem.setBorder(BorderFactory.createTitledBorder(FILE_NAME_CONTAIN));
		// repNameCodeCheckBoxPanel.setPreferredSize(new Dimension(277, 22));
		sheetNameAssumeItem.add(repNameCodeCheckBoxPanel);

		UIScrollPane scrollPane = new UIScrollPane(getKeyGroupFileCheckBoxPanel());
		scrollPane.setPreferredSize(new Dimension(277, 22));
		sheetNameAssumeItem.add(scrollPane);

		refPane = new UIRefPane();
		refPane.setEnabled(false);
		refPane.setPreferredSize(new Dimension(90, 22));
		ReportOrgAttModel orgAttModel = new ReportOrgAttModel();
		orgAttModel.setPk_org(orgPK);
		orgAttModel.setRefTitle(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820007-0039")/*@res "业务单元自定义属性"*/);
		refPane.setRefModel(orgAttModel);
		refPane.setReturnCode(false);
		UIPanel other = new UIPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

		other.add(getOtherFileItem());
		other.add(refPane);
		//
		sheetNameAssumeItem.add(other);
		return sheetNameAssumeItem;
	}

	private UICheckBox getOtherFileItem() {
		if (otherFileItem == null) {
			otherFileItem = new UICheckBox(NCLangUtil.getStrByID("1820001_0", "01820001-1443"/* 其他 */));
			// otherFileItem.setEnabled(false);
			otherFileItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					refPane.setEnabled(otherFileItem.isSelected());
					// refPane.setEditable(otherFileItem.isSelected());
				}
			});
		}
		return otherFileItem;
	}

	private UICheckBox getRepNameChkBox4FileName() {
		if (repNameCheckBox4FileName == null) {
			repNameCheckBox4FileName = new UICheckBox(REP_NAME);
			// repNameCheckBox4FileName.setEnabled(false);
		}
		return repNameCheckBox4FileName;
	}

	private UIPanel getKeyGroupFileCheckBoxPanel() {
		if (keyGroupFileCheckBoxPanel == null) {
			keyGroupFileCheckBoxPanel = new UIPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		}

		return keyGroupFileCheckBoxPanel;
	}

	private UICheckBox getRepCodeChkBox4FileName() {
		if (repCodeCheckBox4FileName == null) {
			repCodeCheckBox4FileName = new UICheckBox(REP_CODE);
			// repCodeCheckBox4FileName.setSelected(true);
			// repCodeCheckBox4FileName.setEnabled(false);
		}
		return repCodeCheckBox4FileName;
	}

	public RepExpParam getRepExpParam() {
		return repExpParam;
	}

	private RepExpParam buildParam() {
		repExpParam = new RepExpParam();
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		// 注意此处的pks 是 aloneId+@+repPK 的数组，需要解析。
		String[] pks = getCheckBoxTable().getSelectedPKs();
		int index = 0;
		String aloneId = null;
		String repPK = null;
		for (String pk : pks) {
			index = pk.indexOf("@");
			aloneId = pk.substring(0, index);
			repPK = pk.substring(index + 1);
			if (map.containsKey(aloneId)) {
				map.get(aloneId).add(repPK);
			} else {
				List<String> repList = new ArrayList<String>();
				repList.add(repPK);
				map.put(aloneId, repList);
			}
			if (!repExpParam.getAlonePubDataMap().containsKey(aloneId)) {
				repExpParam.getAlonePubDataMap().put(aloneId, alonePubDataMap.get(aloneId));
			}
		}
		repExpParam.getAloneRepMap().putAll(map);

		repExpParam.setBalancePK(getBalanceComboBox().getSelectdItemValue().toString());
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < keyCheckBoxList.size(); i++) {
			if (keyCheckBoxList.get(i).isSelected())
				list.add(i);
		}
		int[] keywordNO = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			keywordNO[i] = list.get(i);
		}
		repExpParam.setContainKeywordNO(keywordNO);
		repExpParam.setbContainRepCode(getRepCodeCheckBox().isSelected());
		repExpParam.setbContainRepName(getRepNameCheckBox().isSelected());
		repExpParam.setbZip(getZipCheckBox().isSelected());

		String finalPath = getExcelFilePath().getText();

		repExpParam.setFilePath(finalPath);

		repExpParam.setbSingleFile(getSaveAsSingleCheckBox().isSelected());
		repExpParam.setPk_accscheme(pk_accscheme);
		repExpParam.setDefFileName(getOtherFileItem().isSelected());

		List<Integer> fileKeyNumList = new ArrayList<Integer>();
		for (int i = 0; i < fileKeyCheckBoxList.size(); i++) {
			if (fileKeyCheckBoxList.get(i).isSelected())
				fileKeyNumList.add(i);
		}
		if (!fileKeyNumList.isEmpty()) {
			int[] fileKeyNo = new int[fileKeyNumList.size()];
			for (int i = 0; i < fileKeyNumList.size(); i++) {
				fileKeyNo[i] = fileKeyNumList.get(i);
			}
			repExpParam.setFileKeyNo(fileKeyNo);
		}

		repExpParam.setFileCode(getRepCodeChkBox4FileName().isSelected());
		repExpParam.setFileName(getRepNameChkBox4FileName().isSelected());
		if (repExpParam.isDefFileName()) {
			String code = refPane.getRefCode();
			if (code != null) {
				repExpParam.setCode(code);
				if (code.startsWith("def")) {
					IUserdefitemQryService userdefitemQryService = NCLocator.getInstance().lookup(
							IUserdefitemQryService.class);
					UserdefitemVO userDef2 = null;
					try {
						String pk = refPane.getRefPK();
						userDef2 = userdefitemQryService.qeuryUserdefitemVOByMDPropertyID(pk, orgPK);
					} catch (BusinessException e) {
						AppDebug.error(e);
					}
					// IBDData bdData = null;
					if (userDef2 != null) {
						repExpParam.setClassid(userDef2.getClassid());
					}
				}
			}
		}

		return repExpParam;
	}

	public void setDefaultFileName(String[] fileName) {
		this.defaultFileNames = fileName;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			String filePath = getExcelFilePath().getText();
			if (StringUtils.isEmpty(filePath)) {
				JOptionPane.showMessageDialog(this,
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0943")/*
																											 * @res
																											 * "请点击浏览选择文件导出路径！"
																											 */);
				getExcelFilePath().setShowWarning(true);
				getExcelFilePath().setHitStr(
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0943")/*
																											 * @res
																											 * "请点击浏览选择文件导出路径！"
																											 */);
				getSelPathBtn().requestFocus();
				return;
			}
			if (getCheckBoxTable().getSelectedPKs().length == 0) {
				JOptionPane.showMessageDialog(this,
						nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820001-0904")/*
																											 * @res
																											 * "请选择导出的报表！"
																											 */);
				return;
			}
			repExpParam = buildParam();
			if (repExpParam.isSaveAll2OneFile()) {
				if (filePath != null) {
					if (filePath.lastIndexOf(".") <= 0) {// 表示选择的是文件夹
						getExcelFilePath().setText(null);
						JOptionPane
								.showMessageDialog(
										this,
										nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0",
												"01820001-0943")/* @res "请点击浏览选择文件导出路径！" */);
						getExcelFilePath().setShowWarning(true);
						getExcelFilePath()
								.setHitStr(
										nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0",
												"01820001-0943")/* @res "请点击浏览选择文件导出路径！" */);
						getSelPathBtn().requestFocus();
						return;
					}
				}
			}

			setResult(ID_OK);
			close();
		} else if (e.getSource() == cancelButton) {
			setResult(ID_CANCEL);
			close();
		}
	}

	public void setDefaultFileNames(String[] fileNames) {
		this.defaultFileNames = fileNames;
	}

	class ReportOrgAttModel extends AbstractRefModel {
		// 经过排序后的需要忽略的字段
		String[] ignoreProperty = { "dataoriginflag", "dr", "enablestate", "islastversion", "pk_org", "pk_reportorg",
				"pk_vid", "sourceorgtype", "status", "ts", "venddate", "vname", "vno", "vstartdate" };

		@Override
		public String[] getFieldCode() {
			return new String[] { "name", "displayName", "ID" };
		}

		@Override
		public String[] getFieldName() {
			return new String[] {
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820004-0003")/* @res "名称" */,
					nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820007-0040")/*@res "显示名称"*/ };
		}

		@Override
		public String getTableName() {
			return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0", "01820004-0107")/*
																									 * @res
																									 * "组织_业务单元_报表组织"
																									 */;
		}

		//
		@Override
		public String getPkFieldCode() {
			return "ID";
		}

		// @Override
		// public String getRefShowNameField(){
		// return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820004-0003")/*@res "名称"*/;
		// }
		// @Override
		// public String[] getHiddenFieldCode() {
		// return new String[]{"ID"};
		// }
		@Override
		public java.util.Vector getData() {
			Vector<Vector<String>> vector = new Vector<Vector<String>>();
			IBean bean;
			IUserdefitemQryService defitemQrySrv = NCLocator.getInstance().lookup(IUserdefitemQryService.class);
			try {
				UserdefitemVO[] items = defitemQrySrv.queryUserdefitemVOsByUserdefruleCode("orgunit_reportorg",
						getPk_org());

				bean = MDBaseQueryFacade.getInstance().getBeanByFullClassName(ReportOrgVO.class.getName());
				// 获取bean中所有属性
				List<IAttribute> attributes = bean.getAttributes();
				for (IAttribute iAttribute : attributes) {
					Vector<String> sub = new Vector<String>(2);
					String name = iAttribute.getName();
					if (Arrays.binarySearch(ignoreProperty, name) < 0) {
						if (name.startsWith("def")) {
							try {
								int index = Integer.parseInt(name.substring(3));
								boolean hasUserDef = false;
								String displayName = null;
								for (int i = 0; i < items.length; i++) {
									if (items[i].getPropindex().intValue() == index) {
										hasUserDef = true;
										displayName = MultiLangTextUtil.getCurLangText(items[i], "showname");
										break;
										// IGeneralAccessor accessor =
										// GeneralAccessorFactory.getAccessor(items[i].getClassid());
										// if(accessor != null){
										// // accessor.getDocByPk(docPk)
										// }
									}
								}
								if (!hasUserDef) {
									continue;
								} else {
									sub.add(name);
									sub.add(displayName);
									sub.add(iAttribute.getID());
								}
							} catch (Exception e) {

							}
						} else {
							sub.add(name);
							sub.add(iAttribute.getDisplayName());
							sub.add(iAttribute.getID());
						}
						vector.add(sub);
					}
				}

			} catch (BusinessException e) {
				AppDebug.debug(e);
			}

			return vector;
		}
	}

	/*
	 * 展开
	 *
	 * @see nc.ui.iufo.repdatamng.model.IFlexible#relaxation()
	 */
	@Override
	public void relaxation() {
		uiScrollPanel.setPreferredSize(new Dimension(DIALOG_WIDTH, 350));
		uiScrollPanel.setSize(new Dimension(DIALOG_WIDTH, 350));
		getCheckBoxTable().validate();
		getCheckBoxTable().repaint();
		uiScrollPanel.validate();
		uiScrollPanel.repaint();
		this.repaint();
	}

	/*
	 * 收缩
	 *
	 * @see nc.ui.iufo.repdatamng.model.IFlexible#shrink()
	 */
	@Override
	public void shrink() {
		uiScrollPanel.setPreferredSize(new Dimension(DIALOG_WIDTH, 200));
		uiScrollPanel.setSize(new Dimension(DIALOG_WIDTH, 200));
		uiScrollPanel.getViewport().validate();
		getCheckBoxTable().validate();
		getCheckBoxTable().repaint();
		uiScrollPanel.validate();
		uiScrollPanel.repaint();
		this.repaint();
	}

}
