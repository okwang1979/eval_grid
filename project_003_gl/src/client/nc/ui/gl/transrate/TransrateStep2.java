package nc.ui.gl.transrate;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.TableColumn;

import nc.bd.accperiod.AccperiodParamAccessor;
import nc.bs.logging.Logger;
import nc.bs.uif2.BusinessExceptionAdapter;
import nc.desktop.ui.WorkbenchEnvironment;
import nc.itf.fi.pub.FIBException;
import nc.itf.glcom.para.GLParaAccessor;
import nc.itf.org.IOrgConst;
import nc.pubitf.bbd.CurrtypeQuery;
import nc.pubitf.uapbd.CurrencyRateUtil;
import nc.ui.gl.common.NCHoteKeyRegistCenter;
import nc.ui.gl.datacache.AccountCache;
import nc.ui.gl.exception.AdjustRateNotExitException;
import nc.ui.gl.gateway.glworkbench.GlWorkBench;
import nc.ui.gl.transfer.TransferGenBatchTransferReportDlg;
import nc.ui.pub.beans.UIDialogEvent;
import nc.ui.pub.beans.UIDialogFactory;
import nc.ui.pub.beans.UIDialogListener;
import nc.ui.pub.print.IDataSource;
import nc.ui.pub.print.PrintEntry;
import nc.vo.bd.account.AccountVO;
import nc.vo.bd.currinfo.CurrinfoVO;
import nc.vo.bd.currinfo.CurrrateObj;
import nc.vo.bd.currinfo.CurrrateVO;
import nc.vo.gateway60.accountbook.AccountBookUtil;
import nc.vo.gateway60.itfs.CalendarUtilGL;
import nc.vo.gateway60.itfs.Currency;
import nc.vo.gateway60.pub.GlBusinessException;
import nc.vo.gl.pubvoucher.DetailVO;
import nc.vo.gl.pubvoucher.VoucherVO;
import nc.vo.gl.transfer.TransferHistoryVO;
import nc.vo.gl.transrate.TransrateConst;
import nc.vo.gl.transrate.TransrateDefVO;
import nc.vo.gl.transrate.TransrateHeaderVO;
import nc.vo.gl.transrate.TransrateItemVO;
import nc.vo.gl.transrate.TransrateKey;
import nc.vo.gl.transrate.TransrateParamVO;
import nc.vo.gl.transrate.TransrateTableVO;
import nc.vo.gl.transrate.TransrateVO;
import nc.vo.glcom.nodecode.GlNodeConst;
import nc.vo.glcom.tools.GLPubProxy;
import nc.vo.org.FinanceOrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;

import org.apache.commons.lang.ArrayUtils;
@SuppressWarnings("deprecation")
public class TransrateStep2 extends nc.ui.pub.ToftPanel implements nc.ui.glpub.IUiPanel, IDataSource,UIDialogListener {
	/**
	 * ���к�
	 */
	private static final long serialVersionUID = 315803089474634043L;
	private nc.ui.pub.beans.UILabel ivjLblLAcc = null;
	private nc.ui.pub.beans.UILabel ivjLblLAccName = null;
	private nc.ui.pub.beans.UILabel ivjLblPAcc = null;
	private nc.ui.pub.beans.UILabel ivjLblPAccName = null;
	private nc.ui.pub.beans.UILabel ivjLblPeriod = null;
	private nc.ui.pub.beans.UILabel ivjLblPLAcc = null;
	private nc.ui.pub.beans.UILabel ivjLblPLAccName = null;
	private nc.ui.pub.beans.UILabel ivjLblVoucherType = null;
	private nc.ui.pub.beans.UILabel ivjLblYear = null;
	private nc.ui.pub.beans.UILabel ivjUILabel1 = null;
	private nc.ui.pub.beans.UILabel ivjUILabel13 = null;
	private nc.ui.pub.beans.UILabel ivjUILabel3 = null;
	private nc.ui.pub.beans.UILabel ivjUILabel5 = null;
	private nc.ui.pub.beans.UILabel ivjUILabel7 = null;
	private nc.ui.pub.beans.UITablePane ivjUITblTransrateTable = null;
	
	public Map<Integer, String>[] getMaps() {
		return maps;
	}
	public void setMaps(Map<Integer, String>[] maps) {
		this.maps = maps;
	}
	public String[] getPks() {
		return pks;
	}
	public void setPks(String[] pks) {
		this.pks = pks;
	}
	
	private Map<Integer, String>[] maps;
	private String[] pks;
	private boolean isBatch;
	private HashMap<String, Integer> resultMapIndex;

	public HashMap<String, Integer> getResultMapIndex() {
		return resultMapIndex;
	}
	public void setResultMapIndex(HashMap<String, Integer> resultMapIndex) {
		this.resultMapIndex = resultMapIndex;
	}
	public boolean isBatch() {
		return isBatch;
	}
	public void setBatch(boolean isBatch) {
		this.isBatch = isBatch;
	}

	private nc.ui.gl.pubvoucher.VoucherBridge m_VoucherBridge = null;

	private nc.ui.pub.ButtonObject m_Print = new nc.ui.pub.ButtonObject
					(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000103")/*@res "��ӡ"*/
							,nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000103")/*@res "��ӡ"*/,"��ӡ");
	private nc.ui.pub.ButtonObject m_Generate = new nc.ui.pub.ButtonObject
				(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000403")/*@res "��ת"*/
						,nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000403")/*@res "��ת"*/
								,"��ת");/*-=notranslate=-*/
	private nc.ui.pub.ButtonObject m_Cancel = new nc.ui.pub.ButtonObject
					(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000214")/*@res "����"*/
							,nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000214")/*@res "����"*/
							,"����");/*-=notranslate=-*/
	private nc.ui.pub.ButtonObject[] m_arrButtonObject = new nc.ui.pub.ButtonObject[] { m_Print, m_Generate, m_Cancel };

	private nc.ui.glpub.IParent m_parent = null;

	private TransrateDefVO m_TransrateVO = null;
	private TransrateDataWrapper m_Model = null;

	private String pk_accountingbook=null;
	
	private TransferInputRateDlg ivjDlg = null;
	private TableColumn pk_unitCol;

/**
 * TransrateStep2 ������ע�⡣
 */
public TransrateStep2() {
	super();
	initialize();
}
/**************************************************
����:
		���UiManager����ǰ��Ĺ���ģ��Ҫ������ģ��
		��ĳЩ�¼���������ͨ���÷�����ӡ�

����:
		Object objListener	������
		Object objUserdata	��ʶǰ���Ǻ��ּ�����

����ֵ:
		��

ע��	�÷�����ʵû�й̶���Ҫ��ֻҪ�����ߺͱ�����
		��֮����ڸõ��õ����Э�飬���Ϳ�ʹ�øù���
**************************************************/
public void addListener(Object objListener, Object objUserdata) {}
/**
 *
 * �õ����е���������ʽ����
 * Ҳ���Ƿ������ж����������ı��ʽ
 *
 */
public java.lang.String[] getAllDataItemExpress() {
    return new String[] {
        "vouchtype",
        "year",
        "period",
        "accountsys",
        "exchangesubj",
        "formula",
        "subjcode",
        "subjname",
        "currtype",
        "accitem",
        "count1",
        "count2",
        "count3",
        "count4",
        "count5",
        "count6",
        "count7",
        "count8",
        "count9", "bookname", "transrateno"};
}
	/*
	�������ֱ��ģ��õ��Ľ����һ���������Ķ��巽ʽ��ͬ��	��������

		(1) ÿһ��������(�Ʊ��˳���) ����������չ,û���κ�������ϵ
		------------------------------------------
	 	���\��Ŀ	|  ��Ŀ01	    |	 ��Ŀ02
	  	----------------------------------
	   	(����)	��	| (��Ŀ01)��	|	(��Ŀ02)��
	    ------------------------------------------
	    �Ʊ���:	(�Ʊ���)

		(2) (����) ����չ (��Ŀ) ����չ (���) ������ (��Ŀ)
	    ------------------------
	 	���\��Ŀ	| (��Ŀ) ��
	  	------------------------
	   	(����)	��	| (���)
	     -----------------------
	    �Ʊ���:	(�Ʊ���)

	   	(3) (����) ����չ (��Ŀ) ����չ (���) ������ (��Ŀ ����)
		------------------------
	 	���\��Ŀ	| (��Ŀ) ��
	  	------------------------
	   	(����)	��	| (���)
	    ------------------------
	 	�Ʊ���:	(�Ʊ���)

	    ��ӡ���:
	     --------------------------------
	 	 ���\��Ŀ	|	��Ŀ1 	| ��Ŀ2
	 	 --------------------------------
	 		1999	|  	100	  	|	400
	 		2000	|  	200	 	| 	500
	 		3001	|  	300	 	| 	600
	 	 --------------------------------
	 	 �Ʊ���: xxx
	  */
public java.lang.String[] getAllDataItemNames() {
	return null;
}
/**
 * �˴����뷽��˵����
 * �������ڣ�(2001-12-18 13:42:41)
 * @return java.lang.String[]
 * @param iKey int
 */
private String[] getColData(int iKey) {
	ComputeTableModel model = (ComputeTableModel) getUITblTransrateTable().getTable().getModel();
	String[] sRet = new String[model.getRowCount()];
	int iCol = -1;
	if (iKey == TransrateKey.K_AccCode)
		iCol = 1000;
	try {
		for (int k = 0; iCol == -1 && k < model.getColumnKeys().length; k++) {
			if (iKey == model.getColumnKeys()[k]) {
				iCol = k;
			}
		}

		sRet[0] = "";
		for (int i = 0; i < sRet.length; i++) {
			Object o = null;
			if (iCol == 1000)
				o = model.getTransrateTableVO()[i].getValue(iKey);
			else
				o = model.getValueAt(i, iCol);
			sRet[i] = o == null ? "" : o.toString();
		}
	}
	catch (Exception ex) {
nc.bs.logging.Logger.error(ex.getMessage(), ex);
	}

	return sRet;
}
/**
 *
 * ������������������飬���������ֻ��Ϊ 1 ���� 2
 * ���� null : 		û������
 * ���� 1 :			��������
 * ���� 2 :			˫������
 *
 */
public java.lang.String[] getDependentItemExpressByExpress(java.lang.String itemName) {
	return null;
}
/*
 * �������е��������Ӧ������
 * ������ �����������
 * ���أ� �������Ӧ�����ݣ�ֻ��Ϊ String[]��
 * 		  ��� itemName ӵ���������
 * 		  1 ���������ӡϵͳ����������������ݵ�˳�����ж� String[] �еĴ�ŵ�����
 *		  2 ���������ӡϵͳ�������������������������������

 	ģ�� 2 �����:
 			[��Ŀ]      ==>	  [100 200 300 -->  400 500 600]

 	ģ�� 3 �����: ��� getDependItemNamesByName("���") ==

			[��Ŀ ����]  ==>  [100 200 300 400 500 600] ���к���
			[���� ��Ŀ]  ==>  [100 400 200 500 300 600]	���к���

 */
public java.lang.String[] getItemValuesByExpress(java.lang.String itemExpress) {

	if (itemExpress.equals("vouchtype"))
		return new String[]{nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000417")/*@res "ƾ֤���"*/+"��"+getLblVoucherType().getText()};
	else if (itemExpress.equals("corpname"))
	{
		FinanceOrgVO orgByPk_Accbook = AccountBookUtil.getOrgByPk_Accbook(getPk_accountintbook());
		if(orgByPk_Accbook != null) {
			return new String[]{orgByPk_Accbook.getName()};
		}
		return new String[]{""};
	}
	else if (itemExpress.equals("year"))
		return new String[]{nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000216")/*@res "���"*/+"��"+getLblYear().getText()};
	else if (itemExpress.equals("period"))
		return new String[]{nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000217")/*@res "�ڼ�"*/+"��"+getLblPeriod().getText()};
	else if (itemExpress.equals("exchangesubj"))
		if(getLblPLAccName().isVisible())
			return new String[]{nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000419")/*@res "��������Ŀ"*/+"��"+getLblPLAccName().getText()};
		else
			return new String[]{nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000420")/*@res "��������Ŀ"*/+"��"+getLblPAccName().getText()+" "+nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000421")/*@res "  �����ʧ��Ŀ"*/+"��"+getLblLAccName().getText()};
	else if (itemExpress.equals("formula"))
		return new String[]{""+getUILabel13().getText()};
	else if (itemExpress.equals("subjcode"))
		return getColData(TransrateKey.K_AccCode);
	else if (itemExpress.equals("subjname"))
		return getColData(TransrateKey.K_AccName);
	else if (itemExpress.equals("currtype"))
		return getColData(TransrateKey.K_CurrName);
	else if (itemExpress.equals("accitem"))
		return getColData(TransrateKey.K_Ass);
	else if (itemExpress.equals("count1"))
		return getColData(TransrateKey.K_Balance);
	else if (itemExpress.equals("count2"))
		return getColData(TransrateKey.K_FracBalance);
	else if (itemExpress.equals("count3"))
		return getColData(TransrateKey.K_LocalBalance);
	else if (itemExpress.equals("count4"))
		return getColData(TransrateKey.K_FracAdjustRate);
	else if (itemExpress.equals("count5"))
		return getColData(TransrateKey.K_LocalAdjustRate);
	else if (itemExpress.equals("count6"))
		return getColData(TransrateKey.K_AdjustFracBalance);
	else if (itemExpress.equals("count7"))
		return getColData(TransrateKey.K_AdjustLocalBalance);
	else if (itemExpress.equals("count8"))
		return getColData(TransrateKey.K_FracDiff);
	else if (itemExpress.equals("count9"))
		return getColData(TransrateKey.K_LocalDiff);
	else if (itemExpress.equals("bookname"))
		return getColData(TransrateKey.K_PK_OrgBook);
	else if (itemExpress.equals("transrateno"))
		return getColData(TransrateKey.K_TransferNO);

	return null;

}
/**
 * ���� LblLAcc ����ֵ��
 * @return nc.ui.pub.beans.UILabel
 */
/* ���棺�˷������������ɡ� */
private nc.ui.pub.beans.UILabel getLblLAcc() {
	if (ivjLblLAcc == null) {
		try {
			ivjLblLAcc = new nc.ui.pub.beans.UILabel();
			ivjLblLAcc.setName("LblLAcc");
			ivjLblLAcc.setText(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000422")/*@res "�����ʧ��Ŀ��"*/);
			ivjLblLAcc.setBounds(620, 50, 160, 22);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjLblLAcc;
}
/**
 * ���� LblLAccName ����ֵ��
 * @return nc.ui.pub.beans.UILabel
 */
/* ���棺�˷������������ɡ� */
private nc.ui.pub.beans.UILabel getLblLAccName() {
	if (ivjLblLAccName == null) {
		try {
			ivjLblLAccName = new nc.ui.pub.beans.UILabel();
			ivjLblLAccName.setName("LblLAccName");
			ivjLblLAccName.setText("UILabel12");
			ivjLblLAccName.setBounds(800, 50, 152, 22);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjLblLAccName;
}
/**
 * ���� LblPAcc ����ֵ��
 * @return nc.ui.pub.beans.UILabel
 */
/* ���棺�˷������������ɡ� */
private nc.ui.pub.beans.UILabel getLblPAcc() {
	if (ivjLblPAcc == null) {
		try {
			ivjLblPAcc = new nc.ui.pub.beans.UILabel();
			ivjLblPAcc.setName("LblPAcc");
			ivjLblPAcc.setText(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000420")/*@res "��������Ŀ��"*/);
			ivjLblPAcc.setBounds(318, 50, 160, 22);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjLblPAcc;
}
/**
 * ���� LblPAccName ����ֵ��
 * @return nc.ui.pub.beans.UILabel
 */
/* ���棺�˷������������ɡ� */
private nc.ui.pub.beans.UILabel getLblPAccName() {
	if (ivjLblPAccName == null) {
		try {
			ivjLblPAccName = new nc.ui.pub.beans.UILabel();
			ivjLblPAccName.setName("LblPAccName");
			ivjLblPAccName.setText("UILabel4");
			ivjLblPAccName.setBounds(460, 50, 152, 22);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjLblPAccName;
}
/**
 * ���� LblPeriod ����ֵ��
 * @return nc.ui.pub.beans.UILabel
 */
/* ���棺�˷������������ɡ� */
private nc.ui.pub.beans.UILabel getLblPeriod() {
	if (ivjLblPeriod == null) {
		try {
			ivjLblPeriod = new nc.ui.pub.beans.UILabel();
			ivjLblPeriod.setName("LblPeriod");
			ivjLblPeriod.setText("UILabel8");
			ivjLblPeriod.setBounds(508, 20, 42, 22);
			//ivjLblPeriod.setLocation(437, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjLblPeriod;
}
/**
 * ���� LblPLAcc ����ֵ��
 * @return nc.ui.pub.beans.UILabel
 */
/* ���棺�˷������������ɡ� */
private nc.ui.pub.beans.UILabel getLblPLAcc() {
	if (ivjLblPLAcc == null) {
		try {
			ivjLblPLAcc = new nc.ui.pub.beans.UILabel();
			ivjLblPLAcc.setName("LblPLAcc");
			ivjLblPLAcc.setText(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000419")/*@res "��������Ŀ��"*/);
			ivjLblPLAcc.setBounds(318, 50, 160, 22);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjLblPLAcc;
}
/**
 * ���� LblPLAccName ����ֵ��
 * @return nc.ui.pub.beans.UILabel
 */
/* ���棺�˷������������ɡ� */
private nc.ui.pub.beans.UILabel getLblPLAccName() {
	if (ivjLblPLAccName == null) {
		try {
			ivjLblPLAccName = new nc.ui.pub.beans.UILabel();
			ivjLblPLAccName.setName("LblPLAccName");
			ivjLblPLAccName.setText("UILabel10");
			ivjLblPLAccName.setBounds(490, 50, 152, 22);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjLblPLAccName;
}
/**
 * ���� LblVoucherType ����ֵ��
 * @return nc.ui.pub.beans.UILabel
 */
/* ���棺�˷������������ɡ� */
private nc.ui.pub.beans.UILabel getLblVoucherType() {
	if (ivjLblVoucherType == null) {
		try {
			ivjLblVoucherType = new nc.ui.pub.beans.UILabel();
			ivjLblVoucherType.setName("LblVoucherType");
			ivjLblVoucherType.setText("UILabel2");
			ivjLblVoucherType.setBounds(150, 20, 158, 22);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjLblVoucherType;
}
/**
 * ���� LblYear ����ֵ��
 * @return nc.ui.pub.beans.UILabel
 */
/* ���棺�˷������������ɡ� */
private nc.ui.pub.beans.UILabel getLblYear() {
	if (ivjLblYear == null) {
		try {
			ivjLblYear = new nc.ui.pub.beans.UILabel();
			ivjLblYear.setName("LblYear");
			ivjLblYear.setText("UILabel6");
			ivjLblYear.setBounds(357, 20, 42, 22);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjLblYear;
}
/**
 * �˴����뷽��˵����
 * �������ڣ�(2001-11-26 15:36:49)
 * @return nc.ui.gl.transrate.TransrateDataWrapper
 * @exception java.lang.Exception �쳣˵����
 */
public TransrateDataWrapper getModel() {
	if(m_Model==null)
	{
		m_Model=new TransrateDataWrapper();
		m_Model.setTransrateDef(m_TransrateVO);
	}
	return m_Model;
}
/*
 *  ���ظ�����Դ��Ӧ�Ľڵ����
 */
public java.lang.String getModuleName() {
	return GlNodeConst.GLNODE_TRANSRATE;
}
/**
 * �˴����뷽��˵����
 * �������ڣ�(2001-11-26 16:46:04)
 * @return java.lang.String
 */

/**************************************************
����:
		ToftPanel��Ҫ��ʵ�ַ���������û�ģ�鲻��
		һ��ToftPanel������Ҫʵ�ָ÷������Ա������ʾ
		���ı���

����:
		��

����ֵ:
		��
**************************************************/
public String getTitle() {
	return nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000399")/*@res "��������ת����"*/;
}
/**
 * ���� UILabel1 ����ֵ��
 * @return nc.ui.pub.beans.UILabel
 */
/* ���棺�˷������������ɡ� */
private nc.ui.pub.beans.UILabel getUILabel1() {
	if (ivjUILabel1 == null) {
		try {
			ivjUILabel1 = new nc.ui.pub.beans.UILabel();
			ivjUILabel1.setName("UILabel1");
			ivjUILabel1.setText(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000417")/*@res "ƾ֤���"*/);
			ivjUILabel1.setBounds(27, 20, 120, 22);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjUILabel1;
}
/**
 * ���� UILabel13 ����ֵ��
 * @return nc.ui.pub.beans.UILabel
 */
/* ���棺�˷������������ɡ� */
private nc.ui.pub.beans.UILabel getUILabel13() {
    if (ivjUILabel13 == null) {
        try {
            ivjUILabel13 = new nc.ui.pub.beans.UILabel();
            ivjUILabel13.setName("UILabel13");
           // ivjUILabel13.setText(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UPP2002gl55-000468")/*@res "��ʽ����=�١�/�¢ܣ��ߣ��ޡ�/�¢ݣ��ࣽ�ޣ��ڣ��᣽�ߣ���"*/);
            ivjUILabel13.setBounds(27, 384, 583, 22);
            // user code begin {1}
            //if (getModel().getCurrinfotool().getCurrtypesys()==0)
           //     ivjUILabel13.setText( nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UPP2002gl55-000469")/*@res "��ʽ����=�١�/�¢ۣ��ݣ��ܣ���"*/);
            // user code end
        }
        catch (java.lang.Throwable ivjExc) {
            // user code begin {2}
            // user code end
            handleException(ivjExc);
        }
    }
    return ivjUILabel13;
}
/**
 * ���� UILabel3 ����ֵ��
 * @return nc.ui.pub.beans.UILabel
 */
/* ���棺�˷������������ɡ� */
private nc.ui.pub.beans.UILabel getUILabel3() {
	if (ivjUILabel3 == null) {
		try {
			ivjUILabel3 = new nc.ui.pub.beans.UILabel();
			ivjUILabel3.setName("UILabel3");
			ivjUILabel3.setText(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000216")/*@res "��ȣ�"*/);
			ivjUILabel3.setBounds(318, 20, 42, 22);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjUILabel3;
}
/**
 * ���� UILabel5 ����ֵ��
 * @return nc.ui.pub.beans.UILabel
 */
/* ���棺�˷������������ɡ� */
private nc.ui.pub.beans.UILabel getUILabel5() {
	if (ivjUILabel5 == null) {
		try {
			ivjUILabel5 = new nc.ui.pub.beans.UILabel();
			ivjUILabel5.setName("UILabel5");
			ivjUILabel5.setText(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000418")/*@res "������ϵ��"*/);
			ivjUILabel5.setBounds(27, 51, 120, 22);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjUILabel5;
}
/**
 * ���� UILabel7 ����ֵ��
 * @return nc.ui.pub.beans.UILabel
 */
/* ���棺�˷������������ɡ� */
private nc.ui.pub.beans.UILabel getUILabel7() {
	if (ivjUILabel7 == null) {
		try {
			ivjUILabel7 = new nc.ui.pub.beans.UILabel();
			ivjUILabel7.setName("UILabel7");
			ivjUILabel7.setText(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000217")/*@res "�ڼ䣺"*/);
			ivjUILabel7.setBounds(460, 20, 42, 22);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjUILabel7;
}
/**
 * ���� UITblTransrateTable ����ֵ��
 * @return nc.ui.pub.beans.UITablePane
 */
/* ���棺�˷������������ɡ� */
private nc.ui.pub.beans.UITablePane getUITblTransrateTable() {
    if (ivjUITblTransrateTable == null) {
        try {
            ivjUITblTransrateTable = new nc.ui.pub.beans.UITablePane();
            ivjUITblTransrateTable.setName("UITblTransrateTable");
            ivjUITblTransrateTable.setBounds(16, 79, 1000, 292);
            // user code begin {1}
            getModel().setPk_accountingbook(getPk_accountintbook());
            ivjUITblTransrateTable.getTable().setModel(new ComputeTableModel(getModel()));
            ivjUITblTransrateTable.getTable().setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);

            for (int i = 0; i < ivjUITblTransrateTable.getTable().getColumnCount(); i++) {
                if (ivjUITblTransrateTable.getTable().getColumnClass(i).equals(nc.vo.pub.lang.UFDouble.class)) {

                    ivjUITblTransrateTable.getTable().getColumnModel().getColumn(i).setPreferredWidth(
                        ivjUITblTransrateTable.getTable().getColumnModel().getColumn(i).getPreferredWidth());
                    javax.swing.table.DefaultTableCellRenderer tcr = new javax.swing.table.DefaultTableCellRenderer();
                    tcr.setHorizontalAlignment(javax.swing.JLabel.RIGHT);
                    ivjUITblTransrateTable.getTable().getColumnModel().getColumn(i).setCellRenderer(tcr);
                }
            }
        } catch (java.lang.Throwable ivjExc) {
            handleException(ivjExc);
        }
    }
    return ivjUITblTransrateTable;
}
/**
 * ÿ�������׳��쳣ʱ������
 * @param exception java.lang.Throwable
 */
private void handleException(java.lang.Throwable exception) {

	Logger.error(exception);
}
/**
 * ��ʼ���ࡣ
 */
/* ���棺�˷������������ɡ� */
private void initialize() {
	try {
		// user code begin {1}
		// user code end
		setName("TransrateStep2");
		setLayout(null);
		setSize(774, 419);
		add(getUILabel1(), getUILabel1().getName());
		add(getLblVoucherType(), getLblVoucherType().getName());
		add(getUILabel3(), getUILabel3().getName());
//		add(getLblCurrSys(), getLblCurrSys().getName());
		add(getUILabel5(), getUILabel5().getName());
		add(getLblYear(), getLblYear().getName());
		add(getUILabel7(), getUILabel7().getName());
		add(getLblPeriod(), getLblPeriod().getName());
		add(getLblPLAcc(), getLblPLAcc().getName());
		add(getLblPLAccName(), getLblPLAccName().getName());
		add(getLblLAcc(), getLblLAcc().getName());
		add(getLblLAccName(), getLblLAccName().getName());
		add(getUITblTransrateTable(), getUITblTransrateTable().getName());
		add(getUILabel13(), getUILabel13().getName());
		add(getLblPAcc(), getLblPAcc().getName());
		add(getLblPAccName(), getLblPAccName().getName());
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	}
	// user code begin {2}
	NCHoteKeyRegistCenter.buildAction(this, m_arrButtonObject);
	this.setButtons(m_arrButtonObject);

	m_Print.setCode(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UC001-0000007")/*@res "��ӡ"*/);
	m_Generate.setCode(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UPP2002gl55-000470")/*@res "��ת"*/);
	m_Cancel.setCode(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UC001-0000038")/*@res "����"*/);

	// user code end
}
/**************************************************
����:
		���UiManager����ǰ��Ĺ���ģ����Ҫ���ñ�ģ
		���ĳ�����������ĳ�����ܣ�������ͨ���÷���
		�ﵽ��һĿ��

����:
		Object objData	��Ҫ���ݵĲ�������Ϣ
		Object objUserData	��Ҫ���ݵı�ʾ����Ϣ

����ֵ:
		Object

ע��	�÷�����ʵû�й̶���Ҫ��ֻҪ�����ߺͱ�����
		��֮����ڸõ��õ����Э�飬���Ϳ�ʹ�øù���
**************************************************/
public Object invoke(Object objData, Object objUserData) {
	return invokeOpt(objData, objUserData);
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

public Object invokeOpt(Object objData, Object objUserData) {
	
	this.setPk_accountingbook(((Object[])objData)[2].toString());
	getModel().setPk_accountingbook(((Object[])objData)[2].toString());
	m_Generate.setEnabled(true); // ��־�� û�ж���������ʡ� ���ܽ�ת
	updateButton(m_Generate);
	getModel().setVOWrapper((TransrateVO_Wrapper) ((Object[]) objData)[0]);
	m_TransrateVO = (TransrateDefVO) ((Object[]) objData)[1];
	getModel().setTransrateDef((TransrateDefVO) m_TransrateVO);
	
	//������ת
	setMaps((Map<Integer, String>[]) ((Object[])objData)[3]);
	setPks((String[]) ((Object[])objData)[4]);
	setResultMapIndex((HashMap<String, Integer>) ((Object[])objData)[5]);
	if(getMaps() != null && getPks() != null && getResultMapIndex()!=null){
		setBatch(true);
	}else{
		setBatch(false);
	}
	
	try {
		if (m_TransrateVO != null) {
			getLblYear().setText(m_TransrateVO.getYear());
			getLblPeriod().setText(m_TransrateVO.getPeriod());
			if(!isBatch() && m_TransrateVO.getTransrateVO().size() == 1){
				TransrateHeaderVO aHeader = (TransrateHeaderVO) m_TransrateVO.getTransrateVO().get(0).getParentVO();
				getLblVoucherType().setText(aHeader.getVoucherTypeName());
				if (aHeader.getPk_accsubjPL() == null) {
					getLblPLAcc().setVisible(false);
					getLblPLAccName().setVisible(false);
					getLblPAcc().setVisible(true);
					getLblPAccName().setVisible(true);
					getLblLAcc().setVisible(true);
					getLblLAccName().setVisible(true);
					getLblPAccName().setText(aHeader.getAccsubjProfitName());
					getLblLAccName().setText(aHeader.getAccsubjLossName());
				} else {
					getLblPLAcc().setVisible(true);
					getLblPLAccName().setVisible(true);
					getLblPAcc().setVisible(false);
					getLblPAccName().setVisible(false);
					getLblLAcc().setVisible(false);
					getLblLAccName().setVisible(false);
					getLblPLAccName().setText(aHeader.getAccsubjPLName());
				}
			}else{
				getUILabel1().setVisible(true);
				getLblVoucherType().setText("");
				getLblPLAcc().setVisible(true);
				getLblPLAccName().setText("");
				getLblPAcc().setVisible(false);
				getLblPAccName().setText("");
				getLblLAcc().setVisible(false);
				getLblLAccName().setText("");
			}
			
			// addBy shaoguo.wangȡ�õ������ʺͻ��ʾ��� @2007-07-17
			if(!isBatch){
				Set<String> curTypes = new HashSet<String>();
				for (int i = 0; i < m_TransrateVO.getTransrateVO().size(); i++) {
					for(int j=0; j<m_TransrateVO.getTransrateVO().get(i).getChildrenVO().length; j++){
						curTypes.add(((TransrateItemVO) m_TransrateVO.getTransrateVO().get(i).getChildrenVO()[j]).getAttributeValue("pk_currtype").toString());
					}
				}
				if (curTypes.size() == 1) {
					String curType = curTypes.toArray(new String[0])[0];
					List<TransrateParamVO> params = new ArrayList<TransrateParamVO>();
					String accperiodscheme = CalendarUtilGL.getAccountCalendarByAccountBook(getPk_accountintbook()).getYearVO().getPk_accperiodscheme();
					UFDouble aAjustrate = getAdjustRateVO(getPk_accountintbook(), m_TransrateVO.getYear(), m_TransrateVO.getPeriod(), curType, getModel().getCurrinfotool(getPk_accountintbook()).getPk_LocalCurr(), accperiodscheme);
					int ratedigit = getRateDig(getPk_accountintbook(),getModel().getPk_org(), curType);
					TransrateParamVO vo = new TransrateParamVO(TransrateConst.LOCALRATE,aAjustrate,ratedigit);
					params.add(vo);
					
					if(Currency.isStartGroupCurr(GlWorkBench.getLoginGroup())){
						aAjustrate = getAdjustRateVOByOrg(GlWorkBench.getLoginGroup(), m_TransrateVO.getYear(), m_TransrateVO.getPeriod(), curType, getModel().getCurrinfotool(getPk_accountintbook()).getPk_groupCurr(), null);
						ratedigit = getRateDigByOrg(GlWorkBench.getLoginGroup(),getModel().getPk_org(), curType);
						vo = new TransrateParamVO(TransrateConst.GROUPRATE,aAjustrate,ratedigit);
						params.add(vo);
					}
					
					if(Currency.isStartGlobalCurr()){
						aAjustrate = getAdjustRateVOByOrg(IOrgConst.GLOBEORG, m_TransrateVO.getYear(), m_TransrateVO.getPeriod(), curType, getModel().getCurrinfotool(getPk_accountintbook()).getPk_globalCurr(), null);
						ratedigit = getRateDigByOrg(IOrgConst.GLOBEORG,getModel().getPk_org(), curType);
						vo = new TransrateParamVO(TransrateConst.GLOBALRATE,aAjustrate,ratedigit);
						params.add(vo);
					}
					//���ʹ���ջ��������޸�,�ĳ�ȡȫ�ֵ��ջ��ʡ�by����־ǿ  at��2019-4-3.
					//****start
					String year  =  m_TransrateVO.getYear();
					String month  = m_TransrateVO.getPeriod();
					
					UFDate date = new UFDate(year+"-"+month+"-01");
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(date.toDate());
					int days =   calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
					
					date =  new UFDate(year+"-"+month+"-"+days); 
					CurrrateObj currVo = CurrencyRateUtil.getGlobeInstance().getCurrrateAndRate(curType, getModel().getCurrinfotool(getPk_accountintbook()).getPk_LocalCurr(),date , 0);
					
					//���Ҷ�Ӧ�·���������Ҳ�����������
					CurrrateVO[] cVos  = currVo.getCurrinfoVO().getCurrrate();
					UFDouble rate = new UFDouble(0);
					if(cVos!=null&&cVos.length>0){
						boolean haveCurrentMonthRate =  false;
						for(CurrrateVO cVo:cVos){
							UFDate rateDate  =  new UFDate(cVo.getRatedate().toDate());
							if(date.getYear()==rateDate.getYear()&&date.getMonth()==rateDate.getMonth()){
								haveCurrentMonthRate = true;
								break;
							}
							
						}
						if(haveCurrentMonthRate){
							rate = currVo.getRate();
						}
					} 
					for(TransrateParamVO paramVo:params){
						paramVo.setRate(rate);
						
					}
					//****end
					getDlg(curType).showModal(params);
				} else {
//					checkRate();
					((ComputeTableModel) getUITblTransrateTable().getTable().getModel()).setModelData(getModel().getTransRateTable(true, null, null));
				}
			}else{
//				checkRate();
				((ComputeTableModel) getUITblTransrateTable().getTable().getModel()).setModelData(getModel().getTransRateTable(true, null, null));
			}
			
		}
	} catch (AdjustRateNotExitException ae) {
		((ComputeTableModel) getUITblTransrateTable().getTable().getModel()).setModelData(new TransrateTableVO[0]);
		getUITblTransrateTable().getTable().updateUI();
		getModel().setTransRateTable(new HashMap<String,TransrateTableVO[]>());
		m_Generate.setEnabled(false); // ��־�� û�ж���������ʡ� ���ܽ�ת
		updateButton(m_Generate);
		nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,ae.getMessage());

	} catch (Exception ex) {
		m_Generate.setEnabled(false); // ��־�� û�ж���������ʡ� ���ܽ�ת
		updateButton(m_Generate);
		Logger.error(ex);
		nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,ex.getMessage());
	}finally{
		
		//����ҵ��Ԫ�������ɾ��������ʾҵ��Ԫ��
		//TODO ������˲���һ������һ��û���ã�������߼����޸ģ���������д����
		if(!isBuSupport()&&(pk_unitCol==null||getUITblTransrateTable().getTable().getColumnModel().getColumn(2).equals(pk_unitCol))){
			pk_unitCol =  getUITblTransrateTable().getTable().getColumnModel().getColumn(2);
			getUITblTransrateTable().getTable().getColumnModel().removeColumn(pk_unitCol);
		}else if(isBuSupport()&&pk_unitCol!=null&&!getUITblTransrateTable().getTable().getColumnModel().getColumn(2).equals(pk_unitCol)){
			getUITblTransrateTable().getTable().getColumnModel().addColumn(pk_unitCol);
			getUITblTransrateTable().getTable().getColumnModel().moveColumn(getUITblTransrateTable().getTable().getColumnModel().getColumnCount()-1, 2);
		}
	}
	return null;
}
	
	/**
	 * V631 У���������ĵ�������
	 * @author zhaoyangm
	 * @param selectedVOs
	 * @deprecated
	 */
	private void checkRate() throws AdjustRateNotExitException{
		
		try{
			List<TransrateVO> selectedVOs = m_TransrateVO.getTransrateVO();
			Map<String,HashSet<String>> book2Curr = new HashMap<String, HashSet<String>>();
			if(selectedVOs != null){
				for (TransrateVO transrateVO : selectedVOs) {
					TransrateHeaderVO headVO = (TransrateHeaderVO)transrateVO.getParentVO();
					if(!book2Curr.containsKey(headVO.getPk_glorgbook())){
						book2Curr.put(headVO.getPk_glorgbook(), new HashSet<String>());
					}
					if(transrateVO.getChildrenVO() != null){
						for(int j=0; j<transrateVO.getChildrenVO().length; j++){
							book2Curr.get(headVO.getPk_glorgbook()).add(((TransrateItemVO) transrateVO.getChildrenVO()[j]).getAttributeValue("pk_currtype").toString());
						}
					}
				}
				
				StringBuilder error = new StringBuilder();//���в�������˲�У���Ĵ���ϼ���Ϣ
				for (String pkBook : book2Curr.keySet()) {
					
					Set<String> errorAjustrateCurr = new HashSet<String>();//δ������֯���ҵ������ʵı���
					Set<String> errorGroupAjustrateCurr = new HashSet<String>();//δ���ü��ű��ҵ������ʵı���
					Set<String> errorGlobalAjustrateCurr = new HashSet<String>();//δ����ȫ�ֱ��ҵ������ʵı���
					for (String pkCurr : book2Curr.get(pkBook)) {
						
						String pk_org = AccountBookUtil.getPk_orgByAccountBookPk(pkBook);
						String accperiodscheme = AccperiodParamAccessor.getInstance().getAccperiodschemePkByPk_org(pk_org);
						UFDouble aAjustrate = getAdjustRateVO(getPk_accountintbook(), m_TransrateVO.getYear(), m_TransrateVO.getPeriod(), pkCurr, getModel().getCurrinfotool(pkBook).getPk_LocalCurr(), accperiodscheme);
						if(UFDouble.ZERO_DBL.equals(aAjustrate)){
							errorAjustrateCurr.add(pkCurr);
						}
						
						if(Currency.isStartGroupCurr(GlWorkBench.getLoginGroup())){
							aAjustrate = getAdjustRateVOByOrg(GlWorkBench.getLoginGroup(), m_TransrateVO.getYear(), m_TransrateVO.getPeriod(), pkCurr, getModel().getCurrinfotool(getPk_accountintbook()).getPk_groupCurr(), null);
							if(UFDouble.ZERO_DBL.equals(aAjustrate)){
								if(Currency.isGroupRawConvertModel(GlWorkBench.getLoginGroup())){
									//����ԭ�Ҽ���
									errorGroupAjustrateCurr.add(pkCurr);
								}else{
									//���ڱ��Ҽ���
									errorGroupAjustrateCurr.add(getModel().getCurrinfotool(pkBook).getPk_LocalCurr());
								}
							}
						}
						
						if(Currency.isStartGlobalCurr()){
							aAjustrate = getAdjustRateVOByOrg(IOrgConst.GLOBEORG, m_TransrateVO.getYear(), m_TransrateVO.getPeriod(), pkCurr, getModel().getCurrinfotool(getPk_accountintbook()).getPk_globalCurr(), null);
							if(UFDouble.ZERO_DBL.equals(aAjustrate)){
								if(Currency.isGlobalRawConvertModel(IOrgConst.GLOBEORG)){
									//����ԭ�Ҽ���
									errorGlobalAjustrateCurr.add(pkCurr);
								}else{
									//���ڱ��Ҽ���
									errorGlobalAjustrateCurr.add(getModel().getCurrinfotool(pkBook).getPk_LocalCurr());
								}
							}
						}
					}
					if(errorAjustrateCurr.size()>0 || errorGroupAjustrateCurr.size()>0 || errorGlobalAjustrateCurr.size()>0){
						StringBuilder errorMsg = new StringBuilder(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UPP2002gl55-002824", null, new String[]{AccountBookUtil.getAccountingBookVOByPrimaryKey(pkBook).getCode()})/*@res "����Ϊ{0}�Ĳ�������˲�δ�������е������ʣ�"*/);
						if(errorAjustrateCurr.size()>0){
							List<String> errorCurrNames = new ArrayList<String>();
							for (String errorCurrPk : errorAjustrateCurr) {
								errorCurrNames.add(CurrtypeQuery.getInstance().getCurrtypeName(errorCurrPk));
							}
							errorMsg.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UPP2002gl55-002825", null, new String[]{ArrayUtils.toString(errorCurrNames)})/*@res "����Ϊ{0}���۱���������"*/);
							errorMsg.append(", ");
						}
						if(errorGroupAjustrateCurr.size()>0){
							List<String> errorCurrNames = new ArrayList<String>();
							for (String errorCurrPk : errorGroupAjustrateCurr) {
								errorCurrNames.add(CurrtypeQuery.getInstance().getCurrtypeName(errorCurrPk));
							}
							errorMsg.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UPP2002gl55-002826", null, new String[]{ArrayUtils.toString(errorCurrNames)})/*@res "����Ϊ{0}�ļ��ű����۱���������"*/);
							errorMsg.append(", ");
						}
						if(errorGlobalAjustrateCurr.size()>0){
							List<String> errorCurrNames = new ArrayList<String>();
							for (String errorCurrPk : errorGlobalAjustrateCurr) {
								errorCurrNames.add(CurrtypeQuery.getInstance().getCurrtypeName(errorCurrPk));
							}
							errorMsg.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UPP2002gl55-002827", null, new String[]{ArrayUtils.toString(errorCurrNames)})/*@res "����Ϊ{0}��ȫ�ֱ����۱���������"*/);
							errorMsg.append(", ");
						}
						error.append(errorMsg.toString().substring(0, errorMsg.toString().length()-2)).append("\n");
					}
				}
				if(error.length()>0){
					throw new AdjustRateNotExitException(error.toString());
				}
			}
		}catch (BusinessException e){
			throw new AdjustRateNotExitException(e.getMessage());
		}
		
	}

/**
 * ȡ���ʾ���
 * 
 * @param pkCorp
 * @param curytype
 * @return
 * @throws FIBException
 */
public Integer getRateDig(String pk_glorgbook, String pkCorp, String curytype) throws FIBException {
	Integer rateDig = null;
	rateDig = CurrtypeQuery.getInstance().getCurrdigit(curytype);
	try {
		CurrinfoVO currinfoVO = CurrencyRateUtil.getInstanceByAccountingBook(pk_glorgbook).getCurrinfoVO(curytype, getModel().getCurrinfotool(pk_glorgbook).getPk_LocalCurr());
		if (currinfoVO != null) {
			rateDig = currinfoVO.getRatedigit();
		}
	} catch (BusinessException e) {
		Logger.error(e.getMessage(), e);
		throw new GlBusinessException(e.getMessage());
	}
	return rateDig;
}
/**
 * ȡ���ʾ��� ȫ�֡�����
 * 
 * @param pkCorp
 * @param curytype
 * @return
 * @throws FIBException
 */
public Integer getRateDigByOrg(String pk_org, String pkCorp, String curytype) throws FIBException {
	Integer rateDig = null;
	String dest_currency_pk = "";
	try {
		if(pk_org.equals(IOrgConst.GLOBEORG)){
			dest_currency_pk = getModel().getCurrinfotool(getPk_accountintbook()).getPk_globalCurr();
			if(!Currency.isGlobalRawConvertModel(pk_org)){  //���������֯���Ҽ���
				curytype = getModel().getCurrinfotool(getPk_accountintbook()).getPk_LocalCurr();
			}
			rateDig = CurrtypeQuery.getInstance().getCurrdigit(curytype);
		}else{
			dest_currency_pk = getModel().getCurrinfotool(getPk_accountintbook()).getPk_groupCurr();
			if(!Currency.isGroupRawConvertModel(pk_org)){  
				curytype = getModel().getCurrinfotool(getPk_accountintbook()).getPk_LocalCurr();
			}
			rateDig = CurrtypeQuery.getInstance().getCurrdigit(curytype);
		}
		CurrinfoVO currinfoVO = CurrencyRateUtil.getInstanceByOrg(
				pk_org).getCurrinfoVO(curytype, dest_currency_pk);
		if (currinfoVO != null) {
			rateDig = currinfoVO.getRatedigit();
		}
	} catch (BusinessException e) {
		Logger.error(e.getMessage(), e);
		throw new GlBusinessException(e.getMessage());
	}
	return rateDig;
}
/*
 * ���ظ��������Ƿ�Ϊ������
 * ������ɲ������㣻��������ֻ��Ϊ�ַ�������
 * �硰������Ϊ�������������롱Ϊ��������
 */
public boolean isNumber(java.lang.String itemExpress) {
	return false;
}

/**
 * ȡ��������
 * @param pkCorp
 * @param year
 * @param period
 * @param curyType
 * @param localCury
 * @param accperiodscheme
 * @return
 */
public UFDouble getAdjustRateVO(String pk_accountingbook, String year, String period, String curyType, String localCury, String accperiodscheme) {
	UFDouble adjustrate = null;
	try {
		adjustrate = Currency.getAdjustRate(pk_accountingbook,curyType, localCury, accperiodscheme, year, period);
	} catch (Exception e) {
		Logger.error(e.getMessage(), e);
	}
	return adjustrate;
}

/**
 * ȡ��������
 * @param pkCorp
 * @param year
 * @param period
 * @param curyType
 * @param localCury
 * @param accperiodscheme
 * @return
 */
public UFDouble getAdjustRateVOByOrg(String pk_org, String year, String period, String curyType, String localCury, String accperiodscheme) {
	UFDouble adjustrate = null;
	try {
		if(pk_org.equals(IOrgConst.GLOBEORG)){
			if(!Currency.isGlobalRawConvertModel(pk_org)){  //���������֯���Ҽ���
				curyType = getModel().getCurrinfotool(getPk_accountintbook()).getPk_LocalCurr();
			}
		}else{
			if(!Currency.isGroupRawConvertModel(pk_org)){  
				curyType = getModel().getCurrinfotool(getPk_accountintbook()).getPk_LocalCurr();
			}
		}
		adjustrate = Currency.getAdjustRateByOrg(pk_org,curyType, localCury, accperiodscheme, year, period);
	} catch (Exception e) {
		Logger.error(e.getMessage(), e);
	}
	return adjustrate;
}


	/**
	 * ȡ���־���
	 * @param pkCorp
	 * @param curytype
	 * @return
	 * @throws FIBException
	 */
	public Integer getRateDig (String pkCorp, String curytype) {
		return null;
	}

/**************************************************
����:
		��A����ģ�����B����ģ������B����ģ��ر�ʱ
		A����ģ��ͨ���÷����õ�֪ͨ

����:
		��
����ֵ:
		��
**************************************************/
public void nextClosed() {}
/**************************************************
����:
		ToftPanel����Ҫ��ʵ�ַ���������û�ģ�鲻��
		һ��ToftPanel������Ҫʵ�ָ÷������Ա�UiManager
		���Խ���ť�¼�֪ͨ��

����:
		ButtonObject bo	�����¼��İ�ť

����ֵ:
		��
**************************************************/
public void onButtonClicked(nc.ui.pub.ButtonObject bo)
{
	if (bo.equals(m_Generate))
	{
		onGenerateClick();
		bo.setEnabled(false);
	}
	if (bo.equals(m_Cancel))
	{
		onCancelClick();
	}
	if (bo.equals(m_Print))
	{
		onPrintClick();
	}
}
/**
 * �˴����뷽��˵����
 * �������ڣ�(2001-11-24 15:23:30)
 */
private void onCancelClick()
{
	m_parent.closeMe();
}
/**
 * �˴����뷽��˵����
 * �������ڣ�(2001-11-24 15:22:48)
 */
private void onGenerateClick() {
	
	List<VoucherVO> aVoucherVOs = new ArrayList<VoucherVO>(); 
	
	if(m_TransrateVO.getTransrateVO() != null){
		
		if(!isBatch() && m_TransrateVO.getTransrateVO().size() == 1){
			
			//ֻѡ��һ����ȥ��ת����Ҫ��ʾ�Ƿ��н�ת��ʷ
			TransrateHeaderVO headVO = (TransrateHeaderVO)m_TransrateVO.getTransrateVO().get(0).getParentVO();
			TransferHistoryVO aHistoryVO=new TransferHistoryVO();
			aHistoryVO.setPeriod(m_TransrateVO.getPeriod());
			aHistoryVO.setYear(m_TransrateVO.getYear());
			aHistoryVO.setPk_glorgbook(headVO.getPk_glorgbook());
			aHistoryVO.setPk_group(GlWorkBench.getLoginGroup());
			aHistoryVO.setPk_transfer(headVO.getPk_transRate());
			TransferHistoryVO[] historys;
			try {
				historys = GLPubProxy.getRemoteITransferHistory().queryByVO(aHistoryVO,Boolean.TRUE);
				if(historys!=null && historys.length>0){
					String bookName = new AccountBookUtil().getAccountingBookNameByPk(headVO.getPk_glorgbook());
					int rst = this.showYesNoMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("UCMD1-000283","UCMD1-000283")/*@res "��������˲�"*/ + bookName + m_TransrateVO.getYear()+nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000135")/*@res "��"*/+m_TransrateVO.getPeriod()+nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000428")/*@res "�ڼ�������������Ѿ����й���ת���Ƿ������ת��"*/);
					if(rst==nc.ui.pub.beans.UIDialog.ID_NO||rst==nc.ui.pub.beans.UIDialog.ID_CANCEL)
						return;
				}
			} catch (BusinessException e) {
				Logger.error(e.getMessage(), e);
			}
		}
		
		List<String> msgList  = new ArrayList<String>();//��̨���ɼ�¼��ת���
		for (TransrateVO transrateVO : m_TransrateVO.getTransrateVO()) {
			
			TransrateHeaderVO headVO = (TransrateHeaderVO)transrateVO.getParentVO();
			
			try {
				TransferHistoryVO aHistoryVO=new TransferHistoryVO();
				aHistoryVO.setPeriod(m_TransrateVO.getPeriod());
				aHistoryVO.setYear(m_TransrateVO.getYear());
				aHistoryVO.setPk_glorgbook(headVO.getPk_glorgbook());
				aHistoryVO.setPk_group(GlWorkBench.getLoginGroup());
				aHistoryVO.setPk_transfer(headVO.getPk_transRate());
				
				GenerateVoucher aGenerateVoucher = new GenerateVoucher();
				aGenerateVoucher.setTransrateVO(transrateVO);
				aGenerateVoucher.setTransrateTableVO(getModel().getFinalTransRateTable(transrateVO));
				aGenerateVoucher.setPk_orgbook(headVO.getPk_glorgbook());
				aGenerateVoucher.setVOWrapper(getModel().getVOWrapper());
				
				VoucherVO aVoucherVO = aGenerateVoucher.generate(aHistoryVO.getYear(), aHistoryVO.getPeriod());
				if(aVoucherVO!=null){
					for(int i=0;i<aVoucherVO.getDetail().size();i++){
						DetailVO detail = (DetailVO)aVoucherVO.getDetail().get(i);
						AccountVO accvo = AccountCache.getInstance().getAccountVOByPK(aVoucherVO.getPk_accountingbook(), detail.getPk_accasoa(), aVoucherVO.getPrepareddate().toStdString());
						if(accvo.getIncurflag() != null && accvo.getIncurflag().booleanValue()){
							switch (accvo.getBalanorient().intValue()) {
							case 0: {
								if (!detail.getLocalcreditamount().equals(UFDouble.ZERO_DBL)) {
									detail.setLocaldebitamount(detail.getLocalcreditamount().multiply(-1));
									detail.setLocalcreditamount(UFDouble.ZERO_DBL);
									detail.setDebitamount(detail.getCreditamount().multiply(-1));
									detail.setCreditamount(UFDouble.ZERO_DBL);
								}
								
								if (!detail.getGroupcreditamount().equals(UFDouble.ZERO_DBL)) {
									detail.setGroupdebitamount(detail.getGroupcreditamount().multiply(-1));
									detail.setGroupcreditamount(UFDouble.ZERO_DBL);
								}
								
								if (!detail.getGlobalcreditamount().equals(UFDouble.ZERO_DBL)) {
									detail.setGlobaldebitamount(detail.getGlobalcreditamount().multiply(-1));
									detail.setGlobalcreditamount(UFDouble.ZERO_DBL);
								}
								
								break;
							}
							case 1: {
								if (!detail.getLocaldebitamount().equals(UFDouble.ZERO_DBL)) {
									detail.setLocalcreditamount(detail.getLocaldebitamount().multiply(-1));
									detail.setLocaldebitamount(UFDouble.ZERO_DBL);
									detail.setCreditamount(detail.getDebitamount().multiply(-1));
									detail.setDebitamount(UFDouble.ZERO_DBL);
								}
								
								if (!detail.getGroupdebitamount().equals(UFDouble.ZERO_DBL)) {
									detail.setGroupcreditamount(detail.getGroupdebitamount().multiply(-1));
									detail.setGroupdebitamount(UFDouble.ZERO_DBL);
								}
								
								if (!detail.getGlobaldebitamount().equals(UFDouble.ZERO_DBL)) {
									detail.setGlobalcreditamount(detail.getGlobaldebitamount().multiply(-1));
									detail.setGlobaldebitamount(UFDouble.ZERO_DBL);
								}
								
								break;
							}
							}
						}
					}
					if (getModel().getTransrateDefVO().isBackground().booleanValue()) {
						GLPubProxy.getRemoteVoucher().save(aVoucherVO, true);
						msgList.add("����Ϊ" + headVO.getTransferno() + "�Ĺ���������ƾ֤�ɹ�");
					}else{
						aVoucherVOs.add(aVoucherVO);
					}
					if(isBatch()){
						getMaps()[2].put(getResultMapIndex().get(headVO.getPk_glorgbook() + headVO.getTransferno()), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55", "UPP2002gl55-000400")/** @res* "����ƾ֤"*/);
					}
				}else{
					if(isBatch()){
						getMaps()[2].put(getResultMapIndex().get(headVO.getPk_glorgbook() + headVO.getTransferno()), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55", "UPP2002gl55-000401")/** @res* "û�����κ�ƾ֤"*/);
					}else{
						msgList.add("����Ϊ" + headVO.getTransferno() + "�Ĺ�����û�����κ�ƾ֤");
						continue;
					}
				}
			} catch (Exception e) {
				Logger.error(e.getMessage(), e);
				if(isBatch()){
					getMaps()[2].put(getResultMapIndex().get(headVO.getPk_glorgbook() + headVO.getTransferno()),e.getMessage());
				}else{
					msgList.add("����Ϊ" + headVO.getTransferno() + "�Ĺ���������ƾ֤����" + e.getMessage());
					continue;
				}
			}
			
		}
		
		
		if (!getModel().getTransrateDefVO().isBackground().booleanValue()) {
			//ǰ̨����
			try {
				if(aVoucherVOs.size() == 1){
					m_VoucherBridge =(nc.ui.gl.pubvoucher.VoucherBridge) m_parent.showNext("nc.ui.gl.pubvoucher.VoucherBridge",new Integer[] { Integer.valueOf(2)});
					m_VoucherBridge.setVoucher(aVoucherVOs.toArray(new VoucherVO[0]));
					m_VoucherBridge.setGenerateStyle(2);
					m_VoucherBridge.startEditing();
				}else if(aVoucherVOs.size() > 1){
					m_VoucherBridge =(nc.ui.gl.pubvoucher.VoucherBridge) m_parent.showNext("nc.ui.gl.pubvoucher.VoucherBridge");
					m_VoucherBridge.setVoucher(aVoucherVOs.toArray(new VoucherVO[0]));
					m_VoucherBridge.invoke(null, "FIPBATCHSAVE");
				}else{
					throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55", "UPP2002gl55-000401")/** @res* "û�����κ�ƾ֤"*/);
				}
			} catch (Exception e) {
				//ǰ̨���ɣ���תƾ֤�ų����쳣ʱ�׵�ǰ̨
				Logger.error(e.getMessage(), e);
				throw new BusinessExceptionAdapter(new BusinessException(e.getMessage(), e));
			}
		}else{
			if(!isBatch()){
				//��̨���ɷ�������ת��Ҫ��ʾ��Ϣ
				StringBuilder message = new StringBuilder();
				for(String msg : msgList){
					message.append(msg).append("\n");
				}
				nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,message.toString());
			}
		}
		
		if(isBatch()){
			try {
				// ��ʾ������ת�ı���
				TransferGenBatchTransferReportDlg dlg = UIDialogFactory.newDialogInstance(TransferGenBatchTransferReportDlg.class, null,nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("voucherclient1_0","02002005-0171")/*@res "������ת�������"*/);
				dlg.setMaps(getMaps()[2], getMaps()[0], getMaps()[1], getMaps()[3]);
				dlg.showModal();
			} catch (Exception e) {
				Logger.error(e.getMessage(), e);
				throw new BusinessExceptionAdapter(new BusinessException(e.getMessage(), e));
			}
		}
	}
	
}
/**
 * �˴����뷽��˵����
 * �������ڣ�(2001-11-24 15:23:30)
 */
private void onPrintClick() {
       
    PrintEntry print = new PrintEntry(null, this);
	String pk_user = GlWorkBench.getLoginUser();
	// print.setTemplateID(null, getModuleName(), null, null);
	String pk_group = WorkbenchEnvironment.getInstance().getGroupVO().getPk_group();
	print.setTemplateID(pk_group, getModuleName(), pk_user, null, null, null);
	if (print.selectTemplate() > 0)
		print.preview();
	else
		// �û�ȡ��
		return;

}

/**************************************************
����:
		ȥ��ĳ��������

����:
		Object objListener	������
		Object objUserdata	��ʶǰ���Ǻ��ּ�����

����ֵ:
		��

ע��	�÷�����ʵû�й̶���Ҫ��ֻҪ�����ߺͱ�����
		��֮����ڸõ��õ����Э�飬���Ϳ�ʹ�øù���
**************************************************/
public void removeListener(Object objListener, Object objUserdata) {}
/**************************************************
����:
		���UiManagerҪ��ʾĳһ������ģ�飬�������
		��ģ���showMe�����������ʾ����

����:
		IParent parent ����ģ�����UiManager�е�ĳЩ
		���ݵĵ��ýӿ�ʵ����
����ֵ:
		��
**************************************************/
public void showMe(nc.ui.glpub.IParent parent)
{
    parent.getUiManager().removeAll();
    parent.getUiManager().add(this, this.getName());
    m_parent = parent;

}

	public String getPk_accountintbook(){
		if (pk_accountingbook == null) {
			return GlWorkBench.getDefaultMainOrg();
		}
		return pk_accountingbook;
	}
	
	public void setPk_accountingbook(String pk_accountingbook) {
		this.pk_accountingbook = pk_accountingbook;
	}
	@Override
	public void dialogClosed(UIDialogEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == ivjDlg) {
			if (e.m_Operation == UIDialogEvent.WINDOW_CANCEL)
			{
				m_Generate.setEnabled(false);
				((ComputeTableModel) getUITblTransrateTable().getTable().getModel()).setModelData(null);
				return;
			}
			if (e.m_Operation == UIDialogEvent.WINDOW_OK) {
				HashMap<Integer,UFDouble> rates =ivjDlg.getRate();
				try {
					((ComputeTableModel) getUITblTransrateTable().getTable().getModel()).setModelData(getModel().getTransRateTable(true, rates, null));
				} catch (AdjustRateNotExitException ae) {
					((ComputeTableModel) getUITblTransrateTable().getTable().getModel()).setModelData(new TransrateTableVO[0]);
					getUITblTransrateTable().getTable().updateUI();
					getModel().setTransRateTable(new HashMap<String,TransrateTableVO[]>());
					m_Generate.setEnabled(false); // ��־�� û�ж���������ʡ� ���ܽ�ת
					updateButton(m_Generate);
					nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,ae.getMessage());

				} catch (Exception ex) {
					m_Generate.setEnabled(false); // ��־�� û�ж���������ʡ� ���ܽ�ת
					updateButton(m_Generate);
					Logger.error(ex);
				}
			}
		}
	}
	
	private boolean isBuSupport(){
		try {
			return GLParaAccessor.isSecondBUStart(getPk_accountintbook()).booleanValue();
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			
		}
		return false;
	}
	
	public TransferInputRateDlg getDlg(String curType) throws Exception{
		if(ivjDlg == null){
			ivjDlg = new TransferInputRateDlg(this);
			ivjDlg.addUIDialogListener(this);
		}
		return ivjDlg;
	}
	
	
	
}
