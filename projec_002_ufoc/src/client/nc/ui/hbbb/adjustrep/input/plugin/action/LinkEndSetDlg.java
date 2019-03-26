/**
 *
 */
package nc.ui.hbbb.adjustrep.input.plugin.action;

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
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import nc.bs.framework.common.NCLocator;
import nc.impl.hbbb.linkend.LinkEndQueryImpl;
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
import nc.ui.iufo.resmng.refmodel.AllReportInfoDefaultRefModel;
import nc.ui.ml.NCLangRes;
import nc.ui.pub.beans.UIButton;
import nc.ui.pub.beans.UICheckBox;
import nc.ui.pub.beans.UIComboBox;
import nc.ui.pub.beans.UIDialog;
import nc.ui.pub.beans.UIFileChooser;
import nc.ui.pub.beans.UILabel;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.beans.UIRadioButton;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.beans.UIScrollPane;
import nc.ui.pub.beans.UITextField;
import nc.ui.pub.beans.ValueChangedEvent;
import nc.ui.pub.beans.ValueChangedListener;
import nc.ui.pub.beans.constenum.DefaultConstEnum;
import nc.ui.querytemplate.component.SeparationLine;
import nc.util.bd.intdata.UFDSSqlUtil;
import nc.utils.iufo.TaskRepStatusUtil;
import nc.vo.bd.userdefrule.UserdefitemVO;
import nc.vo.iufo.balance.BalanceCondVO;
import nc.vo.iufo.commit.CommitStateEnum;
import nc.vo.iufo.data.IKeyDetailData;
import nc.vo.iufo.data.KeyDetailDataUtil;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.OrgKeyDetailData;
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
 * ��������
 * <b>Application name:</b>�Ϳ���Ŀ<br>
 * <b>Application describing:</b> <br>
 * <b>Copyright:</b>Copyright &copy; 2019 ��������ɷ����޹�˾��Ȩ���С�<br>
 * <b>Company:</b>yonyou<br>
 * <b>Date:</b>2019-3-26<br>
 * @author����־ǿ
 * @version �Ϳ�
 */ 
public class LinkEndSetDlg extends UIDialog implements ActionListener{
	
	private UICheckBox cb_Hbs = new UICheckBox("�ϲ���");
	private UICheckBox cb_Hjs = new UICheckBox("�ϼ���");
	private UICheckBox cb_Dxj = new UICheckBox("������");
	private UICheckBox cb_Dxd = new UICheckBox("������");
	private UICheckBox cb_Gbb = new UICheckBox("������");
	
	private UIRadioButton rbQueryAll = new UIRadioButton("�����¼�");
	private UIRadioButton rbQueryChild = new UIRadioButton("ֱ���¼�");
	private UIRadioButton rbQueryEnd = new UIRadioButton("ĩ��");
	
	private final UIButton okButton = new UIButton(NCLangUtil.getStrByID("1820001_0", "01820001-0029"));

	private final UIButton cancelButton = new UIButton(NCLangUtil.getStrByID("1820001_0", "01820001-0030"));

			


 

	/**
	 * {�ֶι�������}
	 */
	private static final long serialVersionUID = -7894170740147361557L;


	private UIPanel mainPanel;

 
	final private int DIALOG_WIDTH = 650;

	final private int DIALOG_HEIGHT = 500;
	
	private LinkEn

 

	public LinkEndSetDlg(Container parent, String title, String orgPK, RepDataQueryResultVO[] repQryResults) {
		super(parent);
		// 790 * 570
		this.setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		this.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
	 
		getContentPane().add(getMainPanel());
		setTitle(title);
		
	}

	private UIPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new UIPanel(new VerticalFlowLayout(FlowLayout.LEFT, 5, 0, true, false));
			mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 14, 0, 14));
		 
			
			ButtonGroup group = new ButtonGroup();
			group.add(rbQueryAll);
			group.add(rbQueryChild);
			group.add(rbQueryEnd);
			
			UIPanel filterPanel = new UIPanel(new FlowLayout(FlowLayout.LEFT));
			filterPanel.setBorder(BorderFactory.createTitledBorder("��ѯ����"));
			filterPanel.add(rbQueryAll);
			filterPanel.add(rbQueryChild);
			filterPanel.add(rbQueryEnd);
			mainPanel.add(filterPanel);


			UIPanel typePanel = new UIPanel(new FlowLayout(FlowLayout.LEFT));
			
			filterPanel.setBorder(BorderFactory.createTitledBorder("����ѡ��"));
		 
			filterPanel.add(cb_Hbs);
			filterPanel.add(cb_Hjs);
			filterPanel.add(cb_Dxj);
			filterPanel.add(cb_Dxd);
			filterPanel.add(cb_Gbb);
			mainPanel.add(filterPanel);
			
			UIPanel buttonPane = new UIPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
			okButton.setText(NCLangRes.getInstance().getStrByID("common", "UC001-0000044") + "(Y)");
			okButton.setMnemonic('Y');

			cancelButton.setText(nc.ui.ml.NCLangRes.getInstance().getStrByID("common", "UC001-0000008") + "(C)");
			cancelButton.setMnemonic('C');

			okButton.addActionListener(this);
			buttonPane.add(okButton);
			getRootPane().setDefaultButton(okButton);

			cancelButton.addActionListener(this);
			buttonPane.add(cancelButton);
			
			UIPanel bottomPanel = new UIPanel(new VerticalFlowLayout(FlowLayout.LEFT, 5, 5, true, false));
			bottomPanel.add(new SeparationLine(5, 5));
			bottomPanel.add(buttonPane);

			getContentPane().add(bottomPanel, BorderLayout.SOUTH);


		 
		}
		return mainPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			 
 
			setResult(ID_OK);
			close();
		} else if (e.getSource() == cancelButton) {
			setResult(ID_CANCEL);
			close();
		}
		
	}
	

}
