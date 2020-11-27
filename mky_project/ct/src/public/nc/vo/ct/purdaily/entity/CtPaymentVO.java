package nc.vo.ct.purdaily.entity;

import nc.vo.pub.IVOMeta;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pubapp.pattern.model.meta.entity.vo.VOMetaFactory;

public class CtPaymentVO extends SuperVO
{
  public static final String ACCRATE = "accrate";
  public static final String CHECKDATA = "checkdata";
  public static final String DR = "dr";
  public static final String EFFECTADDMONTH = "effectaddmonth";
  public static final String EFFECTDATEADDDATE = "effectdateadddate";
  public static final String EFFECTMONTH = "effectmonth";
  public static final String ISDEPOSIT = "isdeposit";
  public static final String OUTACCOUNTDATE = "outaccountdate";
  public static final String PAYMENTDAY = "paymentday";
  public static final String PK_BALATYPE = "pk_balatype";
  public static final String PK_CT_PU = "pk_ct_pu";
  public static final String PK_CT_PU_PAYMENT = "pk_ct_pu_payment";
  public static final String PK_GROUP = "pk_group";
  public static final String PK_ORG = "pk_org";
  public static final String PK_ORG_V = "pk_org_v";
  public static final String PK_PAYPERIOD = "pk_payperiod";
  public static final String PK_RATE = "pk_rate";
  public static final String PREPAYMENT = "prepayment";
  public static final String SHOWORDER = "showorder";
  public static final String TS = "ts";
  private static final long serialVersionUID = 3172191870795196072L;

  public UFDouble getAccrate()
  {
    return (UFDouble)getAttributeValue("accrate");
  }

  public Integer getCheckdata() {
    return (Integer)getAttributeValue("checkdata");
  }

  public Integer getDr() {
    return (Integer)getAttributeValue("dr");
  }

  public Integer getEffectaddmonth() {
    return (Integer)getAttributeValue("effectaddmonth");
  }

  public Integer getEffectdateadddate() {
    return (Integer)getAttributeValue("effectdateadddate");
  }

  public Integer getEffectmonth() {
    return (Integer)getAttributeValue("effectmonth");
  }

  public UFBoolean getIsdeposit() {
    return (UFBoolean)getAttributeValue("isdeposit");
  }

  public IVOMeta getMetaData()
  {
    IVOMeta meta = VOMetaFactory.getInstance().getVOMeta("ct.ct_pu_payment");
    return meta;
  }

  public Integer getOutaccountdate() {
    return (Integer)getAttributeValue("outaccountdate");
  }

  public Integer getPaymentday() {
    return (Integer)getAttributeValue("paymentday");
  }

  public String getPk_balatype() {
    return (String)getAttributeValue("pk_balatype");
  }

  public String getPk_ct_pu()
  {
    return (String)getAttributeValue("pk_ct_pu");
  }

  public String getPk_ct_pu_payment() {
    return (String)getAttributeValue("pk_ct_pu_payment");
  }

  public String getPk_group() {
    return (String)getAttributeValue("pk_group");
  }

  public String getPk_org() {
    return (String)getAttributeValue("pk_org");
  }

  public String getPk_org_v() {
    return (String)getAttributeValue("pk_org_v");
  }

  public String getPk_payperiod() {
    return (String)getAttributeValue("pk_payperiod");
  }

  public String getPk_rate() {
    return (String)getAttributeValue("pk_rate");
  }

  public UFBoolean getPrepayment() {
    return (UFBoolean)getAttributeValue("prepayment");
  }

  public Integer getShoworder() {
    return (Integer)getAttributeValue("showorder");
  }

  public UFDateTime getTs() {
    return (UFDateTime)getAttributeValue("ts");
  }

  public void setAccrate(UFDouble accrate) {
    setAttributeValue("accrate", accrate);
  }

  public void setCheckdata(Integer checkdata) {
    setAttributeValue("checkdata", checkdata);
  }

  public void setDr(Integer dr) {
    setAttributeValue("dr", dr);
  }

  public void setEffectaddmonth(Integer effectaddmonth) {
    setAttributeValue("effectaddmonth", effectaddmonth);
  }

  public void setEffectdateadddate(Integer effectdateadddate) {
    setAttributeValue("effectdateadddate", effectdateadddate);
  }

  public void setEffectmonth(Integer effectmonth) {
    setAttributeValue("effectmonth", effectmonth);
  }

  public void setIsdeposit(UFBoolean isdeposit) {
    setAttributeValue("isdeposit", isdeposit);
  }

  public void setOutaccountdate(Integer outaccountdate) {
    setAttributeValue("outaccountdate", outaccountdate);
  }

  public void setPaymentday(Integer paymentday) {
    setAttributeValue("paymentday", paymentday);
  }

  public void setPk_balatype(String pk_balatype) {
    setAttributeValue("pk_balatype", pk_balatype);
  }

  public void setPk_ct_pu(String pk_ct_pu)
  {
    setAttributeValue("pk_ct_pu", pk_ct_pu);
  }

  public void setPk_ct_pu_payment(String pk_ct_pu_payment) {
    setAttributeValue("pk_ct_pu_payment", pk_ct_pu_payment);
  }

  public void setPk_group(String pk_group) {
    setAttributeValue("pk_group", pk_group);
  }

  public void setPk_org(String pk_org) {
    setAttributeValue("pk_org", pk_org);
  }

  public void setPk_org_v(String pk_org_v) {
    setAttributeValue("pk_org_v", pk_org_v);
  }

  public void setPk_payperiod(String pk_payperiod) {
    setAttributeValue("pk_payperiod", pk_payperiod);
  }

  public void setPk_rate(String pk_rate) {
    setAttributeValue("pk_rate", pk_rate);
  }

  public void setPrepayment(UFBoolean prepayment) {
    setAttributeValue("prepayment", prepayment);
  }

  public void setShoworder(Integer showorder) {
    setAttributeValue("showorder", showorder);
  }

  public void setTs(UFDateTime ts) {
    setAttributeValue("ts", ts);
  }
  
//modify zhangfxs 2020/11/23 付款计划添加自定义项字段
  public static final String VBDEF1 = "vbdef1";
  public static final String VBDEF2 = "vbdef2";
  public static final String VBDEF3 = "vbdef3";
  public static final String VBDEF4 = "vbdef4";
  public static final String VBDEF5 = "vbdef5";
  public static final String VBDEF6 = "vbdef6";
  public static final String VBDEF7 = "vbdef7";
  public static final String VBDEF8 = "vbdef8";
  public static final String VBDEF9 = "vbdef9";
  public static final String VBDEF10 = "vbdef10";
  //end
  //start
  public String getVbdef1() {
	  return (String)getAttributeValue("vbdef1");
  }
  public void setVbdef1(String vbdef1) {
	  setAttributeValue("vbdef1", vbdef1);
  }
  
  public String getVbdef2() {
	  return (String)getAttributeValue("vbdef2");
  }
  public void setVbdef2(String vbdef2) {
	  setAttributeValue("vbdef2", vbdef2);
  }
  
  public String getVbdef3() {
	  return (String)getAttributeValue("vbdef3");
  }
  public void setVbdef3(String vbdef3) {
	  setAttributeValue("vbdef3", vbdef3);
  }
  
  public String getVbdef4() {
	  return (String)getAttributeValue("vbdef4");
  }
  public void setVbdef4(String vbdef4) {
	  setAttributeValue("vbdef4", vbdef4);
  }
  
  public String getVbdef5() {
	  return (String)getAttributeValue("vbdef5");
  }
  public void setVbdef5(String vbdef5) {
	  setAttributeValue("vbdef5", vbdef5);
  }
  
  public String getVbdef6() {
	  return (String)getAttributeValue("vbdef6");
  }
  public void setVbdef6(String vbdef6) {
	  setAttributeValue("vbdef6", vbdef6);
  }
  
  public String getVbdef7() {
	  return (String)getAttributeValue("vbdef7");
  }
  public void setVbdef7(String vbdef7) {
	  setAttributeValue("vbdef7", vbdef7);
  }
  
  public String getVbdef8() {
	  return (String)getAttributeValue("vbdef8");
  }
  public void setVbdef8(String vbdef8) {
	  setAttributeValue("vbdef8", vbdef8);
  }
  
  public String getVbdef9() {
	  return (String)getAttributeValue("vbdef9");
  }
  public void setVbdef9(String vbdef9) {
	  setAttributeValue("vbdef9", vbdef9);
  }
  
  public String getVbdef10() {
	  return (String)getAttributeValue("vbdef10");
  }
  public void setVbdef10(String vbdef10) {
	  setAttributeValue("vbdef10", vbdef10);
  }
  //end
}