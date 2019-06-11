package nc.ui.hbbb.meetaccount.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import nc.bs.logging.Logger;
import nc.itf.iufo.individual.IUFOIndividualSettingUtil;
import nc.ui.hbbb.hbreport.input.control.UfocRepDataOpenHelpler;
import nc.ui.iufo.ClientEnv;
import nc.ui.pub.beans.UIButton;
import nc.ui.pub.beans.UIDialog;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.bill.BillCardPanel;
import nc.ui.pub.bill.BillData;
import nc.ui.pub.bill.BillItem;
import nc.ui.pub.bill.BillItemHyperlinkEvent;
import nc.ui.pub.bill.BillItemHyperlinkListener;
import nc.ui.pub.bill.BillModel;
import nc.util.hbbb.HBAloneIDUtil;
import nc.util.hbbb.datacenter.DataCenterType;
import nc.util.hbbb.workdraft.pub.ReportType;
import nc.vo.hbbb.contrast.ContrastQryVO;
import nc.vo.hbbb.contrast.IContrastConst;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.meetaccount.RelaFormulaObj;
import nc.vo.iufo.datasource.DataSourceVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.ml.NCLangRes4VoTransl;

import com.ufida.iufo.pub.tools.DateUtil;
import com.ufida.zior.view.MainBoardContext;
import com.ufsoft.iufo.fmtplugin.BDContextKey;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;

/**
 * 
 * <p>
 * TODO �ӿ�/�๦��˵����ʹ��˵�����ӿ��Ƿ�Ϊ�������������ʹ���ߣ����Ƿ��̰߳�ȫ�ȣ���
 * </p>
 * 
 * �޸ļ�¼��<br>
 * <li>�޸��ˣ��޸����ڣ��޸����ݣ�</li> <br>
 * <br>
 * 
 * @see
 * @author wangxwb
 * @version V6.0
 * @since V6.0 ����ʱ�䣺2011-6-7 ����02:38:57
 */
@SuppressWarnings("restriction")
public class MeetResultRelaDialog extends UIDialog implements BillItemHyperlinkListener {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private BillCardPanel cardpanel = null;

	private BillData billdata;

	private RelaFormulaObj[] infovos;

	public MeetResultRelaDialog(Container parent) {
		super(parent);
		init();
	}

	protected void init() {
		// ���öԻ������
		initUI();
		getCardPanel().getBodyItem("data").addBillItemHyperlinkListener(this);
	}

	protected void initUI() {
		setTitle(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0337")/* @res "ԭʼ����" */);
		UIPanel btnPanel = new UIPanel();
		final UIButton okBtn = new UIButton();
		okBtn.setText(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("common", "UC001-0000044")/* @res "ȷ��" */);
		okBtn.setBounds(35, 168, 106, 28);
		okBtn.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				closeOK();
			}
		});
		btnPanel.add(okBtn);

		setSize(611, 500);
		UIPanel container = new UIPanel();
		container.setLayout(new BorderLayout());
		container.add(getCardPanel(), BorderLayout.CENTER);
		getContentPane().add(container, "Center");
		getContentPane().add(btnPanel, "South");
	}

	public BillCardPanel getCardPanel() {
		if (cardpanel == null) {
			cardpanel = new BillCardPanel();
			cardpanel.setBillData(getBillData());
		}
		return cardpanel;
	}

	protected BillData getBillData() {
		if (billdata == null) {
			billdata = new BillData();

			List<BillItem> listhead = new ArrayList<BillItem>();
			BillItem headitem = null;

			headitem = new BillItem();
			headitem.setKey("dxrelafomula");
			headitem.setName(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0338")/* @res "����������ģ�幫ʽ" */);
			headitem.setDataType(BillItem.TEXTAREA);
			headitem.setNull(false);
			headitem.setWidth(600);
			headitem.setRefType("(600,100)");
			headitem.setLength(3000);
			headitem.setEdit(false);

			listhead.add(headitem);

			billdata.setHeadItems(listhead.toArray(new BillItem[0]));

			List<BillItem> list = new ArrayList<BillItem>();
			BillItem item = null;

			item = new BillItem();
			item.setKey("itemname");
			item.setName(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0339")/* @res "������Ŀ����" */);
			item.setNull(false);
			item.setDataType(BillItem.STRING);
			item.setWidth(200);
			item.setLength(20);
			item.setEdit(false);

			list.add(item);

			item = new BillItem();
			item.setKey("dataresurce");
			item.setName(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0340")/* @res "����Դ���" */);
			item.setNull(false);
			item.setDataType(BillItem.STRING);
			item.setWidth(200);
			item.setLength(20);
			item.setEdit(false);
			list.add(item);

			item = new BillItem();
			item.setKey("data");
			item.setName(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0341")/* @res "��ֵ" */);
			item.setListHyperlink(true);
			item.setDataType(BillItem.DECIMAL);
			item.setNull(false);
			item.setWidth(150);
			item.setLength(20);
			item.setEdit(false);
			list.add(item);

			billdata.setBodyItems(list.toArray(new BillItem[0]));
		}
		return billdata;
	}

	public void setValue(RelaFormulaObj[] infovos) {
		this.infovos = infovos;
		getCardPanel().getBillModel().setBodyDataVO(infovos);
		getCardPanel().getBillTable().setSortEnabled(false);
		setErrorVoucherColor();
	}

	public void setErrorVoucherColor() {
		BillModel bodyBillModel = getCardPanel().getBillModel();
		int rowCount = bodyBillModel.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			bodyBillModel.setForeground(Color.BLUE, i, bodyBillModel.getBodyColByKey("data"));
		}
	}

	@Override
	public void hyperlink(BillItemHyperlinkEvent event) {
		int row = event.getRow();
		
		final MainBoardContext context = new MainBoardContext();
		initContext(row, context);

		// context.setAttribute(BDContextKey.CUR_GROUP_PK, getModel().getContext().getPk_group());
		// ��Ҫ�ȹرղ��ܴ򿪱���
		closeOK();
		ReportType relareporttype = infovos[row].getRelareporttype();
		if(relareporttype.equals(ReportType.SEP)){
//			context.setAttribute(DataCenterType.HBBB_DATACENTER,ReportType.SEP);
			Thread t = new Thread() {
				public void run() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Logger.error(e.getMessage(), e);
					}
					UfocRepDataOpenHelpler.getInstance().openRepDataMainboard("ufoc/zior/zior-relareportinput.xml", context);
				}
			};
			SwingUtilities.invokeLater(t);
		}else if(relareporttype.equals(ReportType.HB)){
			context.setAttribute(DataCenterType.HBBB_DATACENTER,DataCenterType.HB);
			context.setAttribute(RelaFormulaObj.RELA_TITTILE, NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0127"));
			Thread t = new Thread() {
				public void run() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Logger.error(e.getMessage(), e);
					}
					UfocRepDataOpenHelpler.getInstance().openRepDataMainboard("ufoc/zior/zior-hbrepinput.xml", 
							context,NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0127"));
				}
			};
			SwingUtilities.invokeLater(t);
		}else if(relareporttype.equals(ReportType.HB_ADJ)){
			context.setAttribute(DataCenterType.HBBB_DATACENTER,DataCenterType.HB_ADJUST);
			context.setAttribute(RelaFormulaObj.RELA_TITTILE, NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0128"));
			Thread t = new Thread() {
				public void run() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Logger.error(e.getMessage(), e);
					}
					UfocRepDataOpenHelpler.getInstance().openRepDataMainboard("ufoc/zior/zior-hbadjrepinput.xml", 
							context,NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0128"));
				}
			};
			SwingUtilities.invokeLater(t);
		}else if(relareporttype.equals(ReportType.SEP_ADJ)){
			context.setAttribute(DataCenterType.HBBB_DATACENTER,DataCenterType.SEP_ADJUST);
			context.setAttribute(RelaFormulaObj.RELA_TITTILE, NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0126"));
			Thread t = new Thread() {
				public void run() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Logger.error(e.getMessage(), e);
					}
					UfocRepDataOpenHelpler.getInstance().openRepDataMainboard("ufoc/zior/zior-adjrepinput.xml", 
							context,NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0126"));
				}
			};
			SwingUtilities.invokeLater(t);
		}
		// �򿪱���
		// RepDataOpenHelpler.getInstance().openRepDataMainboard("ufoc/zior/zior-relareportinput.xml", context);
		// HBReportOpenUtil.openReportByHbScheme(infovos[row]);
		// BillModel bodyBillModel = getCardPanel().getBillModel();
		// String itemname = (String) getCardPanel().getBillModel().getValueAt(row, bodyBillModel.getBodyColByKey("itemname"));
		// String dataresurce = (String) getCardPanel().getBillModel().getValueAt(row, bodyBillModel.getBodyColByKey("dataresurce"));
		// event.getItem().get
	}

	protected void initContext(int row, MainBoardContext context) {
		// ��ʼ��������Ϣ
		try {
			ContrastQryVO contrastqryVO = (ContrastQryVO) infovos[row].getEnv().getExEnv(IContrastConst.CONTRASTQRYVO);
//			String pk_hbscheme = (String) infovos[row].getEnv().getExEnv(IContrastConst.PK_HBSCHEME);
			HBSchemeVO hbschemeVO = (HBSchemeVO) infovos[row].getEnv().getExEnv(IContrastConst.HBSCHEMEVO);
//			HBSchemeVO hbschemeVO = HBSchemeSrvUtils.getHBSchemeByHBSchemeId(pk_hbscheme);
			String pk_keygroup = hbschemeVO.getPk_keygroup();
			Map<String, String> keymap = infovos[row].getKeyMap();
			if(keymap == null)
				keymap = contrastqryVO.getKeymap();
			keymap.put(KeyVO.CORP_PK, infovos[row].getPk_selef());
			String pk_keyCorp = keymap.get(KeyVO.CORP_PK);
			// infovos[row].getPk_org();
			// context.setAttribute(IUfoContextKey.CUR_REPORG_PK, pk_keyCorp);
			context.setAttribute(IUfoContextKey.CUR_REPORG_PK, infovos[row].getPk_selef());
			context.setAttribute(BDContextKey.CUR_USER_ID, ClientEnv.getInstance().getLoginUserID());
			context.setAttribute(BDContextKey.CUR_GROUP_PK, ClientEnv.getInstance().getGroupID());
			context.setAttribute(IUfoContextKey.PERSPECTIVE_ID, IUfoContextKey.PERS_DATA_INPUT);
			context.setAttribute(IUfoContextKey.DATA_RIGHT, IUfoContextKey.RIGHT_DATA_READ);
			context.setAttribute(IUfoContextKey.TASK_PK, hbschemeVO.getPk_hbscheme());
			context.setAttribute(IUfoContextKey.HBSCHEME_VO,hbschemeVO);
			context.setAttribute(IUfoContextKey.IS_HBBBDATA,true);
			context.setAttribute(IUfoContextKey.CUR_REPMNGSTRUCT_PK, hbschemeVO.getPk_repmanastru());
			context.setAttribute(IUfoContextKey.LOGIN_DATE, DateUtil.getCurDay());
			context.setAttribute(IUfoContextKey.OPERATION_STATE, IUfoContextKey.OPERATION_INPUT);

			String pk_dynvalue =  (String) infovos[row].getEnv().getExEnv(IContrastConst.PKDYNKEY);
			
			if(infovos[row].getEnv().getExEnv("otherQueryDim")!=null){
				Map<String,String> thorDims = (Map) infovos[row].getEnv().getExEnv("otherQueryDim");
				for(String key :thorDims.keySet()){
					String value = thorDims.get(key);
					context.setAttribute(key, value);
				}
			}
			
			if(pk_dynvalue == null)
				pk_dynvalue = KeyVO.DIC_CORP_PK;
			// ���öԷ���λ
			context.setAttribute(pk_dynvalue, infovos[row].getPk_countorg());
			if(infovos[row].getRelareporttype().equals(ReportType.SEP)){
				String aloneID = HBAloneIDUtil.getAloneID(pk_keyCorp, keymap, pk_keygroup, 0);
				context.setAttribute(IUfoContextKey.ALONE_ID, aloneID);
			}else{
				context.setAttribute(IUfoContextKey.ALONE_ID, infovos[row].getAloneid());
			}

			context.setAttribute(RelaFormulaObj.MEASURE_PK, infovos[row].getReportvo().getMeasVO());

			context.setAttribute(IUfoContextKey.KEYGROUP_PK, hbschemeVO.getPk_keygroup());
			context.setAttribute(IUfoContextKey.REPORT_PK, infovos[row].getReportvo().getPk_report());
			
			DataSourceVO dsVo = IUFOIndividualSettingUtil.getDefaultDataSourceVo();
			if(dsVo != null)
				context.setAttribute(IUfoContextKey.DATA_SOURCE, dsVo);
		} catch (Exception e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
		}
	}

	public RelaFormulaObj[] getInfovos() {
		return infovos;
	}

	public void setInfovos(RelaFormulaObj[] infovos) {
		this.infovos = infovos;
	}

}