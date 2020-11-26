package nc.vo.ct.purdaily.entity;

import nc.vo.ct.enumeration.CtEntity;
import nc.vo.pub.IVOMeta;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pubapp.pattern.model.meta.entity.vo.VOMetaFactory;
import nc.vo.pubapp.res.NCModule;
import nc.vo.scmpub.payterm.pay.AbstractPayPlanVO;

public class PayPlanVO extends AbstractPayPlanVO
{
  public static final String PK_CT_PAYPLAN = "pk_ct_payplan";
  public static final String PK_CT_PU = "pk_ct_pu";
  public static final String PK_PAYTERMCH = "pk_paytermch";
  private static final long serialVersionUID = -6195592734190988313L;
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

  public String getCcurrencyid()
  {
    return (String)getAttributeValue("ccurrencyid");
  }

  public String getCorigcurrencyid()
  {
    return (String)getAttributeValue("corigcurrencyid");
  }

  public String getCrowno()
  {
    return (String)getAttributeValue("crowno");
  }

  public UFDate getDbegindate()
  {
    return (UFDate)getAttributeValue("dbegindate");
  }

  public UFDate getDenddate()
  {
    return (UFDate)getAttributeValue("denddate");
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
      .getName().toLowerCase() + "." + CtEntity.ct_payplan
      .name());
    return meta;
  }

  public UFDouble getNaccumpayappmny()
  {
    return (UFDouble)getAttributeValue("naccumpayappmny");
  }

  public UFDouble getNaccumpayapporgmny()
  {
    return 
      (UFDouble)getAttributeValue("naccumpayapporgmny");
  }

  public UFDouble getNaccumpaymny()
  {
    return (UFDouble)getAttributeValue("naccumpaymny");
  }

  public UFDouble getNaccumpayorgmny()
  {
    return (UFDouble)getAttributeValue("naccumpayorgmny");
  }

  public UFDouble getNexchangerate()
  {
    return (UFDouble)getAttributeValue("nexchangerate");
  }

  public UFDouble getNmny()
  {
    return (UFDouble)getAttributeValue("nmny");
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

  public String getPk_ct_payplan()
  {
    return (String)getAttributeValue("pk_ct_payplan");
  }

  public String getPk_ct_pu()
  {
    return (String)getAttributeValue("pk_ct_pu");
  }

  public String getPk_financeorg()
  {
    return (String)getAttributeValue("pk_financeorg");
  }

  public String getPk_financeorg_v()
  {
    return (String)getAttributeValue("pk_financeorg_v");
  }

  public String getPk_group()
  {
    return (String)getAttributeValue("pk_group");
  }

  public String getPk_payterm()
  {
    return (String)getAttributeValue("pk_payterm");
  }

  public String getPk_paytermch()
  {
    return (String)getAttributeValue("pk_paytermch");
  }

  public UFDateTime getTs()
  {
    return (UFDateTime)getAttributeValue("ts");
  }

  public void setBpreflag(UFBoolean bpreflag)
  {
    setAttributeValue("bpreflag", bpreflag);
  }

  public void setCcurrencyid(String ccurrencyid)
  {
    setAttributeValue("ccurrencyid", ccurrencyid);
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

  public void setNaccumpayappmny(UFDouble naccumpayappmny)
  {
    setAttributeValue("naccumpayappmny", naccumpayappmny);
  }

  public void setNaccumpayapporgmny(UFDouble naccumpayapporgmny)
  {
    setAttributeValue("naccumpayapporgmny", naccumpayapporgmny);
  }

  public void setNaccumpaymny(UFDouble naccumpaymny)
  {
    setAttributeValue("naccumpaymny", naccumpaymny);
  }

  public void setNaccumpayorgmny(UFDouble naccumpayorgmny)
  {
    setAttributeValue("naccumpayorgmny", naccumpayorgmny);
  }

  public void setNexchangerate(UFDouble nexchangerate)
  {
    setAttributeValue("nexchangerate", nexchangerate);
  }

  public void setNmny(UFDouble nmny)
  {
    setAttributeValue("nmny", nmny);
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

  public void setPk_ct_payplan(String pk_ct_payplan)
  {
    setAttributeValue("pk_ct_payplan", pk_ct_payplan);
  }

  public void setPk_ct_pu(String pk_ct_pu)
  {
    setAttributeValue("pk_ct_pu", pk_ct_pu);
  }

  public void setPk_financeorg(String pk_financeorg)
  {
    setAttributeValue("pk_financeorg", pk_financeorg);
  }

  public void setPk_financeorg_v(String pk_financeorg_v)
  {
    setAttributeValue("pk_financeorg_v", pk_financeorg_v);
  }

  public void setPk_group(String pk_group)
  {
    setAttributeValue("pk_group", pk_group);
  }

  public void setPk_payterm(String pk_payterm)
  {
    setAttributeValue("pk_payterm", pk_payterm);
  }

  public void setPk_paytermch(String pk_paytermch)
  {
    setAttributeValue("pk_paytermch", pk_paytermch);
  }

  public void setTs(UFDateTime ts)
  {
    setAttributeValue("ts", ts);
  }
}