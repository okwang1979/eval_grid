package nc.vo.ct.saledaily.entity;

import nc.vo.ct.enumeration.CtEntity;
import nc.vo.pub.IVOMeta;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pubapp.pattern.model.meta.entity.vo.VOMetaFactory;
import nc.vo.pubapp.res.NCModule;
import nc.vo.scmpub.payterm.recv.AbstractRecvPlanVO;

public class RecvPlanVO extends AbstractRecvPlanVO
{
  public static final String BPREFLAG = "bpreflag";
  public static final String CORIGCURRENCYID = "corigcurrencyid";
  public static final String DBEGINDATE = "dbegindate";
  public static final String DENDDATE = "denddate";
  public static final String DINSIDEENDDATE = "dinsideenddate";
  public static final String DR = "dr";
  public static final String FEFFDATETYPE = "feffdatetype";
  public static final String IACCOUNTTERMNO = "iaccounttermno";
  public static final String IITERMDAYS = "iitermdays";
  public static final String NORIGMNY = "norigmny";
  public static final String NRATE = "nrate";
  public static final String NTOTALORIGMNY = "ntotalorigmny";
  public static final String PK_CT_RECVPLAN = "pk_ct_recvplan";
  public static final String PK_CT_SALE = "pk_ct_sale";
  public static final String PK_PAYTERM = "pk_payterm";
  public static final String TS = "ts";
  public static final String VBILLCODE = "vbillcode";
  private static final long serialVersionUID = 4684054072960817194L;
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

  public UFBoolean getBpreflag()
  {
    return (UFBoolean)getAttributeValue("bpreflag");
  }

  public String getCorigcurrencyid()
  {
    return (String)getAttributeValue("corigcurrencyid");
  }

  public UFDate getDbegindate()
  {
    return (UFDate)getAttributeValue("dbegindate");
  }

  public UFDate getDenddate()
  {
    return (UFDate)getAttributeValue("denddate");
  }

  public UFDate getDinsideenddate()
  {
    return (UFDate)getAttributeValue("dinsideenddate");
  }

  public Integer getDr()
  {
    return (Integer)getAttributeValue("dr");
  }

  public String getFeffdatetype()
  {
    return (String)getAttributeValue("feffdatetype");
  }

  public Integer getIaccounttermno()
  {
    return (Integer)getAttributeValue("iaccounttermno");
  }

  public Integer getIitermdays()
  {
    return (Integer)getAttributeValue("iitermdays");
  }

  public IVOMeta getMetaData()
  {
    IVOMeta meta = VOMetaFactory.getInstance().getVOMeta(NCModule.CT
      .getName().toLowerCase() + "." + CtEntity.ct_recvplan
      .name());
    return meta;
  }

  public UFDouble getNorigmny()
  {
    return (UFDouble)getAttributeValue("norigmny");
  }

  public UFDouble getNrate()
  {
    return (UFDouble)getAttributeValue("nrate");
  }

  public UFDouble getNtotalorigmny()
  {
    return (UFDouble)getAttributeValue("ntotalorigmny");
  }

  public String getPk_ct_recvplan()
  {
    return (String)getAttributeValue("pk_ct_recvplan");
  }

  public String getPk_ct_sale()
  {
    return (String)getAttributeValue("pk_ct_sale");
  }

  public UFDateTime getTs()
  {
    return (UFDateTime)getAttributeValue("ts");
  }

  public String getVbillcode() {
    return (String)getAttributeValue("vbillcode");
  }

  public void setBpreflag(UFBoolean bpreflag)
  {
    setAttributeValue("bpreflag", bpreflag);
  }

  public void setCorigcurrencyid(String corigcurrencyid)
  {
    setAttributeValue("corigcurrencyid", corigcurrencyid);
  }

  public void setCrowno(String crowno)
  {
    setAttributeValue("crowno", crowno);
  }

  public void setDbegindate(UFDate dbegindate)
  {
    setAttributeValue("dbegindate", dbegindate);
  }

  public void setDenddate(UFDate denddate)
  {
    setAttributeValue("denddate", denddate);
  }

  public void setDinsideenddate(UFDate dinsideenddate)
  {
    setAttributeValue("dinsideenddate", dinsideenddate);
  }

  public void setDr(Integer dr)
  {
    setAttributeValue("dr", dr);
  }

  public void setFeffdatetype(String feffdatetype)
  {
    setAttributeValue("feffdatetype", feffdatetype);
  }

  public void setIaccounttermno(Integer iaccounttermno)
  {
    setAttributeValue("iaccounttermno", iaccounttermno);
  }

  public void setIitermdays(Integer iitermdays)
  {
    setAttributeValue("iitermdays", iitermdays);
  }

  public void setNorigmny(UFDouble norigmny)
  {
    setAttributeValue("norigmny", norigmny);
  }

  public void setNrate(UFDouble nrate)
  {
    setAttributeValue("nrate", nrate);
  }

  public void setNtotalorigmny(UFDouble ntotalorigmny)
  {
    setAttributeValue("ntotalorigmny", ntotalorigmny);
  }

  public void setPk_ct_recvplan(String pk_ct_recvplan)
  {
    setAttributeValue("pk_ct_recvplan", pk_ct_recvplan);
  }

  public void setPk_ct_sale(String pk_ct_sale)
  {
    setAttributeValue("pk_ct_sale", pk_ct_sale);
  }

  public void setPk_payterm(String pk_paytem)
  {
    setAttributeValue("pk_payterm", pk_paytem);
  }

  public void setPk_paytermch(String pk_paytermch)
  {
    setAttributeValue("pk_paytermch", pk_paytermch);
  }

  public void setTs(UFDateTime ts)
  {
    setAttributeValue("ts", ts);
  }

  public void setVbillcode(String vbillcode)
  {
    setAttributeValue("vbillcode", vbillcode);
  }
}