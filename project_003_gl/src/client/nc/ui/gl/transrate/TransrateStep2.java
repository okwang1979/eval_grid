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
	 * 序列号
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
					(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000103")/*@res "打印"*/
							,nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000103")/*@res "打印"*/,"打印");
	private nc.ui.pub.ButtonObject m_Generate = new nc.ui.pub.ButtonObject
				(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000403")/*@res "结转"*/
						,nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000403")/*@res "结转"*/
								,"结转");/*-=notranslate=-*/
	private nc.ui.pub.ButtonObject m_Cancel = new nc.ui.pub.ButtonObject
					(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000214")/*@res "返回"*/
							,nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000214")/*@res "返回"*/
							,"返回");/*-=notranslate=-*/
	private nc.ui.pub.ButtonObject[] m_arrButtonObject = new nc.ui.pub.ButtonObject[] { m_Print, m_Generate, m_Cancel };

	private nc.ui.glpub.IParent m_parent = null;

	private TransrateDefVO m_TransrateVO = null;
	private TransrateDataWrapper m_Model = null;

	private String pk_accountingbook=null;
	
	private TransferInputRateDlg ivjDlg = null;
	private TableColumn pk_unitCol;

/**
 * TransrateStep2 构造子注解。
 */
public TransrateStep2() {
	super();
	initialize();
}
/**************************************************
功能:
		如果UiManager或者前面的功能模块要监听本模块
		的某些事件，它可以通过该方法添加。

参数:
		Object objListener	监听器
		Object objUserdata	表识前面是何种监听器

返回值:
		无

注：	该方法其实没有固定的要求，只要调用者和被调用
		者之间存在该调用的相关协议，它就可使用该功能
**************************************************/
public void addListener(Object objListener, Object objUserdata) {}
/**
 *
 * 得到所有的数据项表达式数组
 * 也就是返回所有定义的数据项的表达式
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
	下面三种表格模版得到的结果都一样，但它的定义方式不同：	←↑→↓

		(1) 每一个数据项(制表人除外) 都是向下扩展,没有任何依赖关系
		------------------------------------------
	 	金额\科目	|  科目01	    |	 科目02
	  	----------------------------------
	   	(日期)	↓	| (科目01)↓	|	(科目02)↓
	    ------------------------------------------
	    制表人:	(制表人)

		(2) (日期) 下扩展 (科目) 右扩展 (金额) 依赖于 (科目)
	    ------------------------
	 	金额\科目	| (科目) →
	  	------------------------
	   	(日期)	↓	| (金额)
	     -----------------------
	    制表人:	(制表人)

	   	(3) (日期) 下扩展 (科目) 右扩展 (金额) 依赖于 (科目 日期)
		------------------------
	 	金额\科目	| (科目) →
	  	------------------------
	   	(日期)	↓	| (金额)
	    ------------------------
	 	制表人:	(制表人)

	    打印结果:
	     --------------------------------
	 	 金额\科目	|	科目1 	| 科目2
	 	 --------------------------------
	 		1999	|  	100	  	|	400
	 		2000	|  	200	 	| 	500
	 		3001	|  	300	 	| 	600
	 	 --------------------------------
	 	 制表人: xxx
	  */
public java.lang.String[] getAllDataItemNames() {
	return null;
}
/**
 * 此处插入方法说明。
 * 创建日期：(2001-12-18 13:42:41)
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
 * 返回依赖项的名称数组，该数据项长度只能为 1 或者 2
 * 返回 null : 		没有依赖
 * 长度 1 :			单项依赖
 * 长度 2 :			双向依赖
 *
 */
public java.lang.String[] getDependentItemExpressByExpress(java.lang.String itemName) {
	return null;
}
/*
 * 返回所有的数据项对应的内容
 * 参数： 数据项的名字
 * 返回： 数据项对应的内容，只能为 String[]；
 * 		  如果 itemName 拥有依赖项，则：
 * 		  1 个依赖项：打印系统将根据依赖项的内容的顺序来判断 String[] 中的存放的数据
 *		  2 个依赖项：打印系统将根据两个依赖项的索引来决定数据

 	模板 2 的情况:
 			[科目]      ==>	  [100 200 300 -->  400 500 600]

 	模板 3 的情况: 如果 getDependItemNamesByName("金额") ==

			[科目 日期]  ==>  [100 200 300 400 500 600] 先列后行
			[日期 科目]  ==>  [100 400 200 500 300 600]	先行后列

 */
public java.lang.String[] getItemValuesByExpress(java.lang.String itemExpress) {

	if (itemExpress.equals("vouchtype"))
		return new String[]{nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000417")/*@res "凭证类别"*/+"："+getLblVoucherType().getText()};
	else if (itemExpress.equals("corpname"))
	{
		FinanceOrgVO orgByPk_Accbook = AccountBookUtil.getOrgByPk_Accbook(getPk_accountintbook());
		if(orgByPk_Accbook != null) {
			return new String[]{orgByPk_Accbook.getName()};
		}
		return new String[]{""};
	}
	else if (itemExpress.equals("year"))
		return new String[]{nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000216")/*@res "年度"*/+"："+getLblYear().getText()};
	else if (itemExpress.equals("period"))
		return new String[]{nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000217")/*@res "期间"*/+"："+getLblPeriod().getText()};
	else if (itemExpress.equals("exchangesubj"))
		if(getLblPLAccName().isVisible())
			return new String[]{nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000419")/*@res "汇兑损益科目"*/+"："+getLblPLAccName().getText()};
		else
			return new String[]{nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000420")/*@res "汇兑收益科目"*/+"："+getLblPAccName().getText()+" "+nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000421")/*@res "  汇兑损失科目"*/+"："+getLblLAccName().getText()};
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
 * 返回 LblLAcc 特性值。
 * @return nc.ui.pub.beans.UILabel
 */
/* 警告：此方法将重新生成。 */
private nc.ui.pub.beans.UILabel getLblLAcc() {
	if (ivjLblLAcc == null) {
		try {
			ivjLblLAcc = new nc.ui.pub.beans.UILabel();
			ivjLblLAcc.setName("LblLAcc");
			ivjLblLAcc.setText(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000422")/*@res "汇兑损失科目："*/);
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
 * 返回 LblLAccName 特性值。
 * @return nc.ui.pub.beans.UILabel
 */
/* 警告：此方法将重新生成。 */
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
 * 返回 LblPAcc 特性值。
 * @return nc.ui.pub.beans.UILabel
 */
/* 警告：此方法将重新生成。 */
private nc.ui.pub.beans.UILabel getLblPAcc() {
	if (ivjLblPAcc == null) {
		try {
			ivjLblPAcc = new nc.ui.pub.beans.UILabel();
			ivjLblPAcc.setName("LblPAcc");
			ivjLblPAcc.setText(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000420")/*@res "汇兑收益科目："*/);
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
 * 返回 LblPAccName 特性值。
 * @return nc.ui.pub.beans.UILabel
 */
/* 警告：此方法将重新生成。 */
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
 * 返回 LblPeriod 特性值。
 * @return nc.ui.pub.beans.UILabel
 */
/* 警告：此方法将重新生成。 */
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
 * 返回 LblPLAcc 特性值。
 * @return nc.ui.pub.beans.UILabel
 */
/* 警告：此方法将重新生成。 */
private nc.ui.pub.beans.UILabel getLblPLAcc() {
	if (ivjLblPLAcc == null) {
		try {
			ivjLblPLAcc = new nc.ui.pub.beans.UILabel();
			ivjLblPLAcc.setName("LblPLAcc");
			ivjLblPLAcc.setText(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000419")/*@res "汇兑损益科目："*/);
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
 * 返回 LblPLAccName 特性值。
 * @return nc.ui.pub.beans.UILabel
 */
/* 警告：此方法将重新生成。 */
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
 * 返回 LblVoucherType 特性值。
 * @return nc.ui.pub.beans.UILabel
 */
/* 警告：此方法将重新生成。 */
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
 * 返回 LblYear 特性值。
 * @return nc.ui.pub.beans.UILabel
 */
/* 警告：此方法将重新生成。 */
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
 * 此处插入方法说明。
 * 创建日期：(2001-11-26 15:36:49)
 * @return nc.ui.gl.transrate.TransrateDataWrapper
 * @exception java.lang.Exception 异常说明。
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
 *  返回该数据源对应的节点编码
 */
public java.lang.String getModuleName() {
	return GlNodeConst.GLNODE_TRANSRATE;
}
/**
 * 此处插入方法说明。
 * 创建日期：(2001-11-26 16:46:04)
 * @return java.lang.String
 */

/**************************************************
功能:
		ToftPanel需要的实现方法，如果用户模块不是
		一个ToftPanel，它需要实现该方法，以便可以显示
		它的标题

参数:
		无

返回值:
		无
**************************************************/
public String getTitle() {
	return nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000399")/*@res "汇兑损益结转生成"*/;
}
/**
 * 返回 UILabel1 特性值。
 * @return nc.ui.pub.beans.UILabel
 */
/* 警告：此方法将重新生成。 */
private nc.ui.pub.beans.UILabel getUILabel1() {
	if (ivjUILabel1 == null) {
		try {
			ivjUILabel1 = new nc.ui.pub.beans.UILabel();
			ivjUILabel1.setName("UILabel1");
			ivjUILabel1.setText(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000417")/*@res "凭证类别："*/);
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
 * 返回 UILabel13 特性值。
 * @return nc.ui.pub.beans.UILabel
 */
/* 警告：此方法将重新生成。 */
private nc.ui.pub.beans.UILabel getUILabel13() {
    if (ivjUILabel13 == null) {
        try {
            ivjUILabel13 = new nc.ui.pub.beans.UILabel();
            ivjUILabel13.setName("UILabel13");
           // ivjUILabel13.setText(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UPP2002gl55-000468")/*@res "公式：⑥=①×/÷④，⑦＝⑥×/÷⑤，⑧＝⑥－②，⑨＝⑦－③"*/);
            ivjUILabel13.setBounds(27, 384, 583, 22);
            // user code begin {1}
            //if (getModel().getCurrinfotool().getCurrtypesys()==0)
           //     ivjUILabel13.setText( nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UPP2002gl55-000469")/*@res "公式：④=①×/÷③，⑤＝④－②"*/);
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
 * 返回 UILabel3 特性值。
 * @return nc.ui.pub.beans.UILabel
 */
/* 警告：此方法将重新生成。 */
private nc.ui.pub.beans.UILabel getUILabel3() {
	if (ivjUILabel3 == null) {
		try {
			ivjUILabel3 = new nc.ui.pub.beans.UILabel();
			ivjUILabel3.setName("UILabel3");
			ivjUILabel3.setText(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000216")/*@res "年度："*/);
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
 * 返回 UILabel5 特性值。
 * @return nc.ui.pub.beans.UILabel
 */
/* 警告：此方法将重新生成。 */
private nc.ui.pub.beans.UILabel getUILabel5() {
	if (ivjUILabel5 == null) {
		try {
			ivjUILabel5 = new nc.ui.pub.beans.UILabel();
			ivjUILabel5.setName("UILabel5");
			ivjUILabel5.setText(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000418")/*@res "核算体系："*/);
			ivjUILabel5.setBounds(27, 51, 120, 22);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjUILabel5;
}
/**
 * 返回 UILabel7 特性值。
 * @return nc.ui.pub.beans.UILabel
 */
/* 警告：此方法将重新生成。 */
private nc.ui.pub.beans.UILabel getUILabel7() {
	if (ivjUILabel7 == null) {
		try {
			ivjUILabel7 = new nc.ui.pub.beans.UILabel();
			ivjUILabel7.setName("UILabel7");
			ivjUILabel7.setText(nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000217")/*@res "期间："*/);
			ivjUILabel7.setBounds(460, 20, 42, 22);
		} catch (java.lang.Throwable ivjExc) {
			handleException(ivjExc);
		}
	}
	return ivjUILabel7;
}
/**
 * 返回 UITblTransrateTable 特性值。
 * @return nc.ui.pub.beans.UITablePane
 */
/* 警告：此方法将重新生成。 */
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
 * 每当部件抛出异常时被调用
 * @param exception java.lang.Throwable
 */
private void handleException(java.lang.Throwable exception) {

	Logger.error(exception);
}
/**
 * 初始化类。
 */
/* 警告：此方法将重新生成。 */
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

	m_Print.setCode(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UC001-0000007")/*@res "打印"*/);
	m_Generate.setCode(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UPP2002gl55-000470")/*@res "结转"*/);
	m_Cancel.setCode(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UC001-0000038")/*@res "返回"*/);

	// user code end
}
/**************************************************
功能:
		如果UiManager或者前面的功能模块需要调用本模
		块的某个方法以完成某个功能，它可以通过该方法
		达到这一目标

参数:
		Object objData	所要传递的参数等信息
		Object objUserData	所要传递的表示等信息

返回值:
		Object

注：	该方法其实没有固定的要求，只要调用者和被调用
		者之间存在该调用的相关协议，它就可使用该功能
**************************************************/
public Object invoke(Object objData, Object objUserData) {
	return invokeOpt(objData, objUserData);
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

public Object invokeOpt(Object objData, Object objUserData) {
	
	this.setPk_accountingbook(((Object[])objData)[2].toString());
	getModel().setPk_accountingbook(((Object[])objData)[2].toString());
	m_Generate.setEnabled(true); // 韩志民。 没有定义调整汇率。 不能借转
	updateButton(m_Generate);
	getModel().setVOWrapper((TransrateVO_Wrapper) ((Object[]) objData)[0]);
	m_TransrateVO = (TransrateDefVO) ((Object[]) objData)[1];
	getModel().setTransrateDef((TransrateDefVO) m_TransrateVO);
	
	//批量结转
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
			
			// addBy shaoguo.wang取得调整汇率和汇率精度 @2007-07-17
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
					//央客使用日汇率需求修改,改成取全局的日汇率。by：王志强  at：2019-4-3.
					//****start
					String year  =  m_TransrateVO.getYear();
					String month  = m_TransrateVO.getPeriod();
					
					UFDate date = new UFDate(year+"-"+month+"-01");
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(date.toDate());
					int days =   calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
					
					date =  new UFDate(year+"-"+month+"-"+days); 
					CurrrateObj currVo = CurrencyRateUtil.getGlobeInstance().getCurrrateAndRate(curType, getModel().getCurrinfotool(getPk_accountintbook()).getPk_LocalCurr(),date , 0);
					
					//查找对应月份数据如果找不到则不做处理
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
		m_Generate.setEnabled(false); // 韩志民。 没有定义调整汇率。 不能借转
		updateButton(m_Generate);
		nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,ae.getMessage());

	} catch (Exception ex) {
		m_Generate.setEnabled(false); // 韩志民。 没有定义调整汇率。 不能借转
		updateButton(m_Generate);
		Logger.error(ex);
		nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,ex.getMessage());
	}finally{
		
		//根据业务单元启用情况删除或者显示业务单元列
		//TODO 多核算账簿，一个启用一个没启用，下面的逻辑待修改，而且列是写死的
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
	 * V631 校验各个规则的调整汇率
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
				
				StringBuilder error = new StringBuilder();//所有财务核算账簿校验后的错误合计信息
				for (String pkBook : book2Curr.keySet()) {
					
					Set<String> errorAjustrateCurr = new HashSet<String>();//未设置组织本币调整汇率的币种
					Set<String> errorGroupAjustrateCurr = new HashSet<String>();//未设置集团本币调整汇率的币种
					Set<String> errorGlobalAjustrateCurr = new HashSet<String>();//未设置全局本币调整汇率的币种
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
									//基于原币计算
									errorGroupAjustrateCurr.add(pkCurr);
								}else{
									//基于本币计算
									errorGroupAjustrateCurr.add(getModel().getCurrinfotool(pkBook).getPk_LocalCurr());
								}
							}
						}
						
						if(Currency.isStartGlobalCurr()){
							aAjustrate = getAdjustRateVOByOrg(IOrgConst.GLOBEORG, m_TransrateVO.getYear(), m_TransrateVO.getPeriod(), pkCurr, getModel().getCurrinfotool(getPk_accountintbook()).getPk_globalCurr(), null);
							if(UFDouble.ZERO_DBL.equals(aAjustrate)){
								if(Currency.isGlobalRawConvertModel(IOrgConst.GLOBEORG)){
									//基于原币计算
									errorGlobalAjustrateCurr.add(pkCurr);
								}else{
									//基于本币计算
									errorGlobalAjustrateCurr.add(getModel().getCurrinfotool(pkBook).getPk_LocalCurr());
								}
							}
						}
					}
					if(errorAjustrateCurr.size()>0 || errorGroupAjustrateCurr.size()>0 || errorGlobalAjustrateCurr.size()>0){
						StringBuilder errorMsg = new StringBuilder(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UPP2002gl55-002824", null, new String[]{AccountBookUtil.getAccountingBookVOByPrimaryKey(pkBook).getCode()})/*@res "编码为{0}的财务核算账簿未设置下列调整汇率："*/);
						if(errorAjustrateCurr.size()>0){
							List<String> errorCurrNames = new ArrayList<String>();
							for (String errorCurrPk : errorAjustrateCurr) {
								errorCurrNames.add(CurrtypeQuery.getInstance().getCurrtypeName(errorCurrPk));
							}
							errorMsg.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UPP2002gl55-002825", null, new String[]{ArrayUtils.toString(errorCurrNames)})/*@res "币种为{0}的折本调整汇率"*/);
							errorMsg.append(", ");
						}
						if(errorGroupAjustrateCurr.size()>0){
							List<String> errorCurrNames = new ArrayList<String>();
							for (String errorCurrPk : errorGroupAjustrateCurr) {
								errorCurrNames.add(CurrtypeQuery.getInstance().getCurrtypeName(errorCurrPk));
							}
							errorMsg.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UPP2002gl55-002826", null, new String[]{ArrayUtils.toString(errorCurrNames)})/*@res "币种为{0}的集团本币折本调整汇率"*/);
							errorMsg.append(", ");
						}
						if(errorGlobalAjustrateCurr.size()>0){
							List<String> errorCurrNames = new ArrayList<String>();
							for (String errorCurrPk : errorGlobalAjustrateCurr) {
								errorCurrNames.add(CurrtypeQuery.getInstance().getCurrtypeName(errorCurrPk));
							}
							errorMsg.append(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55","UPP2002gl55-002827", null, new String[]{ArrayUtils.toString(errorCurrNames)})/*@res "币种为{0}的全局本币折本调整汇率"*/);
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
 * 取汇率精度
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
 * 取汇率精度 全局、集团
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
			if(!Currency.isGlobalRawConvertModel(pk_org)){  //如果基于组织本币计算
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
 * 返回该数据项是否为数字项
 * 数字项可参与运算；非数字项只作为字符串常量
 * 如“数量”为数字项、“存货编码”为非数字项
 */
public boolean isNumber(java.lang.String itemExpress) {
	return false;
}

/**
 * 取调整汇率
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
 * 取调整汇率
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
			if(!Currency.isGlobalRawConvertModel(pk_org)){  //如果基于组织本币计算
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
	 * 取币种精度
	 * @param pkCorp
	 * @param curytype
	 * @return
	 * @throws FIBException
	 */
	public Integer getRateDig (String pkCorp, String curytype) {
		return null;
	}

/**************************************************
功能:
		在A功能模块调用B功能模块后，如果B功能模块关闭时
		A功能模块通过该方法得到通知

参数:
		无
返回值:
		无
**************************************************/
public void nextClosed() {}
/**************************************************
功能:
		ToftPanel的需要的实现方法，如果用户模块不是
		一个ToftPanel，它需要实现该方法，以便UiManager
		可以将按钮事件通知它

参数:
		ButtonObject bo	发出事件的按钮

返回值:
		无
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
 * 此处插入方法说明。
 * 创建日期：(2001-11-24 15:23:30)
 */
private void onCancelClick()
{
	m_parent.closeMe();
}
/**
 * 此处插入方法说明。
 * 创建日期：(2001-11-24 15:22:48)
 */
private void onGenerateClick() {
	
	List<VoucherVO> aVoucherVOs = new ArrayList<VoucherVO>(); 
	
	if(m_TransrateVO.getTransrateVO() != null){
		
		if(!isBatch() && m_TransrateVO.getTransrateVO().size() == 1){
			
			//只选择一条定去结转，则要提示是否有结转历史
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
					int rst = this.showYesNoMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("UCMD1-000283","UCMD1-000283")/*@res "财务核算账簿"*/ + bookName + m_TransrateVO.getYear()+nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000135")/*@res "年"*/+m_TransrateVO.getPeriod()+nc.ui.ml.NCLangRes.getInstance().getStrByID("20021505","UPP20021505-000428")/*@res "期间的这个汇兑损益已经进行过结转，是否继续结转？"*/);
					if(rst==nc.ui.pub.beans.UIDialog.ID_NO||rst==nc.ui.pub.beans.UIDialog.ID_CANCEL)
						return;
				}
			} catch (BusinessException e) {
				Logger.error(e.getMessage(), e);
			}
		}
		
		List<String> msgList  = new ArrayList<String>();//后台生成记录结转结果
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
						msgList.add("编码为" + headVO.getTransferno() + "的规则定义生成凭证成功");
					}else{
						aVoucherVOs.add(aVoucherVO);
					}
					if(isBatch()){
						getMaps()[2].put(getResultMapIndex().get(headVO.getPk_glorgbook() + headVO.getTransferno()), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55", "UPP2002gl55-000400")/** @res* "生成凭证"*/);
					}
				}else{
					if(isBatch()){
						getMaps()[2].put(getResultMapIndex().get(headVO.getPk_glorgbook() + headVO.getTransferno()), nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55", "UPP2002gl55-000401")/** @res* "没生成任何凭证"*/);
					}else{
						msgList.add("编码为" + headVO.getTransferno() + "的规则定义没生成任何凭证");
						continue;
					}
				}
			} catch (Exception e) {
				Logger.error(e.getMessage(), e);
				if(isBatch()){
					getMaps()[2].put(getResultMapIndex().get(headVO.getPk_glorgbook() + headVO.getTransferno()),e.getMessage());
				}else{
					msgList.add("编码为" + headVO.getTransferno() + "的规则定义生成凭证错误：" + e.getMessage());
					continue;
				}
			}
			
		}
		
		
		if (!getModel().getTransrateDefVO().isBackground().booleanValue()) {
			//前台生成
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
					throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("2002gl55", "UPP2002gl55-000401")/** @res* "没生成任何凭证"*/);
				}
			} catch (Exception e) {
				//前台生成，跳转凭证桥出现异常时抛到前台
				Logger.error(e.getMessage(), e);
				throw new BusinessExceptionAdapter(new BusinessException(e.getMessage(), e));
			}
		}else{
			if(!isBatch()){
				//后台生成非批量结转，要提示信息
				StringBuilder message = new StringBuilder();
				for(String msg : msgList){
					message.append(msg).append("\n");
				}
				nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,message.toString());
			}
		}
		
		if(isBatch()){
			try {
				// 显示批量结转的报告
				TransferGenBatchTransferReportDlg dlg = UIDialogFactory.newDialogInstance(TransferGenBatchTransferReportDlg.class, null,nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("voucherclient1_0","02002005-0171")/*@res "批量结转结果报告"*/);
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
 * 此处插入方法说明。
 * 创建日期：(2001-11-24 15:23:30)
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
		// 用户取消
		return;

}

/**************************************************
功能:
		去除某个监听器

参数:
		Object objListener	监听器
		Object objUserdata	标识前面是何种监听器

返回值:
		无

注：	该方法其实没有固定的要求，只要调用者和被调用
		者之间存在该调用的相关协议，它就可使用该功能
**************************************************/
public void removeListener(Object objListener, Object objUserdata) {}
/**************************************************
功能:
		如果UiManager要显示某一个功能模块，它会调用
		该模块的showMe方法以完成显示功能

参数:
		IParent parent 功能模块访问UiManager中的某些
		数据的调用接口实现类
返回值:
		无
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
					m_Generate.setEnabled(false); // 韩志民。 没有定义调整汇率。 不能借转
					updateButton(m_Generate);
					nc.vo.fipub.utils.uif2.FiUif2MsgUtil.showUif2DetailMessage(this,null,ae.getMessage());

				} catch (Exception ex) {
					m_Generate.setEnabled(false); // 韩志民。 没有定义调整汇率。 不能借转
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
