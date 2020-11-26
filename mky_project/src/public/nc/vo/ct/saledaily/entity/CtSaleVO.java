package nc.vo.ct.saledaily.entity;

import nc.vo.ct.entity.CtAbstractVO;
import nc.vo.ct.enumeration.CtEntity;
import nc.vo.pub.IVOMeta;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pubapp.pattern.model.meta.entity.vo.VOMetaFactory;
import nc.vo.pubapp.res.NCModule;

public class CtSaleVO extends CtAbstractVO
{
  public static final String BSHOWLATEST = "bshowlatest";
  public static final String BSRCECMCT = "bsrcecmct";
  public static final String MODIFYSTATUS = "modifystatus";
  public static final String NORIPREPAYMNY = "noriprepaymny";
  public static final String NPREPAYMNY = "nprepaymny";
  public static final String PK_CT_SALE = "pk_ct_sale";
  public static final String PK_CUSTOMER = "pk_customer";
//modify zhangfxs 2020/11/03 新增自定义字段
  public static final String VDEF21 = "vdef21";
  public static final String VDEF22 = "vdef22";
  public static final String VDEF23 = "vdef23";
  public static final String VDEF24 = "vdef24";
  public static final String VDEF25 = "vdef25";
  public static final String VDEF26 = "vdef26";
  public static final String VDEF27 = "vdef27";
  public static final String VDEF28 = "vdef28";
  public static final String VDEF29 = "vdef29";
  public static final String VDEF30 = "vdef30";
  public static final String VDEF31 = "vdef31";
  public static final String VDEF32 = "vdef32";
  public static final String VDEF33 = "vdef33";
  public static final String VDEF34 = "vdef34";
  public static final String VDEF35 = "vdef35";
  public static final String VDEF36 = "vdef36";
  public static final String VDEF37 = "vdef37";
  public static final String VDEF38 = "vdef38";
  public static final String VDEF39 = "vdef39";
  public static final String VDEF40 = "vdef40";
  //end
  private static final long serialVersionUID = 9141837154033409778L;

  public UFBoolean getBshowlatest()
  {
    return (UFBoolean)getAttributeValue("bshowlatest");
  }

  public UFBoolean getBsrcecmct()
  {
    return (UFBoolean)getAttributeValue("bsrcecmct");
  }

  public IVOMeta getMetaData()
  {
    IVOMeta meta = VOMetaFactory.getInstance()
      .getVOMeta(NCModule.CT
      .getName().toLowerCase() + "." + CtEntity.ct_sale
      .name());
    return meta;
  }

  public Integer getModifystatus()
  {
    return (Integer)getAttributeValue("modifystatus");
  }

  public UFDouble getNoriprepaymny()
  {
    return (UFDouble)getAttributeValue("noriprepaymny");
  }

  public UFDouble getNprepaymny() {
    return (UFDouble)getAttributeValue("nprepaymny");
  }

  public String getPk_ct_sale() {
    return (String)getAttributeValue("pk_ct_sale");
  }

  public String getPk_customer() {
    return (String)getAttributeValue("pk_customer");
  }

  public void setBshowlatest(UFBoolean bshowlatest)
  {
    setAttributeValue("bshowlatest", bshowlatest);
  }

  public void setBsrcecmct(UFBoolean bsrcecmct)
  {
    setAttributeValue("bsrcecmct", bsrcecmct);
  }

  public void setModifystatus(Integer modifystatus)
  {
    setAttributeValue("modifystatus", modifystatus);
  }

  public void setNoriprepaymny(UFDouble noriprepaymny) {
    setAttributeValue("noriprepaymny", noriprepaymny);
  }

  public void setNprepaymny(UFDouble nprepaymny) {
    setAttributeValue("nprepaymny", nprepaymny);
  }

  public void setPk_ct_sale(String pk_ct_sale) {
    setAttributeValue("pk_ct_sale", pk_ct_sale);
  }

  public void setPk_customer(String pk_customer) {
    setAttributeValue("pk_customer", pk_customer);
  }
//modify zhangfxs 2020/11/03
  public String getVdef21() {
	return (String)getAttributeValue("vdef21");
  }
  public void setVdef21(String vdef21) {
    setAttributeValue("vdef21", vdef21);
  }
	
  public String getVdef22() {
	 return (String)getAttributeValue("vdef22");
  }
  public void setVdef22(String vdef22) {
    setAttributeValue("vdef22", vdef22);
  }
	
  public String getVdef23() {
    return (String)getAttributeValue("vdef23");
  }
  public void setVdef23(String vdef23) {
    setAttributeValue("vdef23", vdef23);
  }
	
  public String getVdef24() {
    return (String)getAttributeValue("vdef24");
  }
  public void setVdef24(String vdef24) {
    setAttributeValue("vdef24", vdef24);
  }
	
  public String getVdef25() {
    return (String)getAttributeValue("vdef25");
  }
  public void setVdef25(String vdef25) {
    setAttributeValue("vdef25", vdef25);
  }

  public String getVdef26() {
    return (String)getAttributeValue("vdef26");
  }
  public void setVdef26(String vdef26) {
    setAttributeValue("vdef26", vdef26);
  }
	
  public String getVdef27() {
    return (String)getAttributeValue("vdef27");
  }
  public void setVdef27(String vdef27) {
    setAttributeValue("vdef27", vdef27);
  }

  public String getVdef28() {
    return (String)getAttributeValue("vdef28");
  }
  public void setVdef28(String vdef28) {
    setAttributeValue("vdef28", vdef28);
  }
	
  public String getVdef29() {
    return (String)getAttributeValue("vdef29");
  }
  public void setVdef29(String vdef29) {
    setAttributeValue("vdef29", vdef29);
  }
	
  public String getVdef30() {
    return (String)getAttributeValue("vdef30");
  }
  public void setVdef30(String vdef30) {
    setAttributeValue("vdef30", vdef30);
  }
	
  public String getVdef31() {
    return (String)getAttributeValue("vdef31");
  }
  public void setVdef31(String vdef31) {
    setAttributeValue("vdef31", vdef31);
  }
	
  public String getVdef32() {
    return (String)getAttributeValue("vdef32");
  }
  public void setVdef32(String vdef32) {
    setAttributeValue("vdef32", vdef32);
  }
	
  public String getVdef33() {
    return (String)getAttributeValue("vdef33");
  }
  public void setVdef33(String vdef33) {
    setAttributeValue("vdef33", vdef33);
  }

  public String getVdef34() {
    return (String)getAttributeValue("vdef34");
  }
  public void setVdef34(String vdef34) {
    setAttributeValue("vdef34", vdef34);
  }
	
  public String getVdef35() {
    return (String)getAttributeValue("vdef35");
  }
  public void setVdef35(String vdef35) {
    setAttributeValue("vdef35", vdef35);
  }
	
  public String getVdef36() {
    return (String)getAttributeValue("vdef36");
  }
  public void setVdef36(String vdef36) {
    setAttributeValue("vdef36", vdef36);
  }
	
  public String getVdef37() {
    return (String)getAttributeValue("vdef37");
  }
  public void setVdef37(String vdef37) {
    setAttributeValue("vdef37", vdef37);
  }
	
  public String getVdef38() {
    return (String)getAttributeValue("vdef38");
  }
  public void setVdef38(String vdef38) {
    setAttributeValue("vdef38", vdef38);
  }
	
  public String getVdef39() {
    return (String)getAttributeValue("vdef39");
  }
  public void setVdef39(String vdef39) {
    setAttributeValue("vdef39", vdef39);
  }
	
  public String getVdef40() {
    return (String)getAttributeValue("vdef40");
  }
  public void setVdef40(String vdef40) {
    setAttributeValue("vdef40", vdef40);
  }
  //end
}