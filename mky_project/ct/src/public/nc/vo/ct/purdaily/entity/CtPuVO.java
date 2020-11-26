package nc.vo.ct.purdaily.entity;

import nc.vo.ct.entity.CtAbstractVO;
import nc.vo.ct.enumeration.CtEntity;
import nc.vo.pub.IVOMeta;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pubapp.pattern.model.meta.entity.vo.VOMetaFactory;
import nc.vo.pubapp.res.NCModule;

public class CtPuVO extends CtAbstractVO
{
  public static final String BBRACKETORDER = "bbracketorder";
  public static final String BPROTSUPPLY = "bprotsupply";
  public static final String BPUBLISH = "bpublish";
  public static final String BSHOWLATEST = "bshowlatest";
  public static final String BSRCECMCT = "bsrcecmct";
  public static final String CVENDORID = "cvendorid";
  public static final String IPRICETYPE = "ipricetype";
  public static final String IRESPSTATUS = "irespstatus";
  public static final String MODIFYSTATUS = "modifystatus";
  public static final String PK_CT_PU = "pk_ct_pu";
  public static final String PK_PUBPSN = "pk_pubpsn";
  public static final String PK_PURCORP = "pk_purcorp";
  public static final String PK_RESPPSN = "pk_resppsn";
  public static final String TPUBTIME = "tpubtime";
  public static final String TRESPTIME = "tresptime";
  public static final String VREASON = "vreason";
  
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
  
  private static final long serialVersionUID = -6057905148775268085L;

  public UFBoolean getBbracketOrder()
  {
    return (UFBoolean)getAttributeValue("bbracketorder");
  }

  public UFBoolean getBprotsupply()
  {
    return (UFBoolean)getAttributeValue("bprotsupply");
  }

  public UFBoolean getBpublish()
  {
    return (UFBoolean)getAttributeValue("bpublish");
  }

  public UFBoolean getBsc()
  {
    return (UFBoolean)getAttributeValue("bsc");
  }

  public UFBoolean getBshowLatest()
  {
    return (UFBoolean)getAttributeValue("bshowlatest");
  }

  public UFBoolean getBsrcecmct()
  {
    return (UFBoolean)getAttributeValue("bsrcecmct");
  }

  public String getCvendorid() {
    return (String)getAttributeValue("cvendorid");
  }

  public Integer getIpricetype()
  {
    return (Integer)getAttributeValue("ipricetype");
  }

  public Integer getIrespstatus()
  {
    return (Integer)getAttributeValue("irespstatus");
  }

  public IVOMeta getMetaData()
  {
    IVOMeta meta = VOMetaFactory.getInstance().getVOMeta(NCModule.CT
      .getName().toLowerCase() + "." + CtEntity.ct_pu.name());
    return meta;
  }

  public Integer getModifyStatus()
  {
    return (Integer)getAttributeValue("modifystatus");
  }

  public String getPk_ct_pu() {
    return (String)getAttributeValue("pk_ct_pu");
  }

  public String getPk_pubpsn()
  {
    return (String)getAttributeValue("pk_pubpsn");
  }

  public String getPk_purcorp()
  {
    return (String)getAttributeValue("pk_purcorp");
  }

  public String getPk_resppsn()
  {
    return (String)getAttributeValue("pk_resppsn");
  }

  public UFDateTime getTpubtime()
  {
    return (UFDateTime)getAttributeValue("tpubtime");
  }

  public UFDateTime getTresptime()
  {
    return (UFDateTime)getAttributeValue("tresptime");
  }

  public String getVreason()
  {
    return (String)getAttributeValue("vreason");
  }

  public void setBbracketOrder(UFBoolean bbracketorder)
  {
    setAttributeValue("bbracketorder", bbracketorder);
  }

  public void setBprotsupply(UFBoolean bprotsupply)
  {
    setAttributeValue("bprotsupply", bprotsupply);
  }

  public void setBpublish(UFBoolean bpublish)
  {
    setAttributeValue("bpublish", bpublish);
  }

  public void setBsc(UFBoolean bsc)
  {
    setAttributeValue("bsc", bsc);
  }

  public void setBshowLatest(UFBoolean bshowlatest)
  {
    setAttributeValue("bshowlatest", bshowlatest);
  }

  public void setBsrcecmct(UFBoolean bsrcecmct)
  {
    setAttributeValue("bsrcecmct", bsrcecmct);
  }

  public void setCvendorid(String cvendorid) {
    setAttributeValue("cvendorid", cvendorid);
  }

  public void setIpricetype(Integer ipricetype)
  {
    setAttributeValue("ipricetype", ipricetype);
  }

  public void setIrespstatus(Integer irespstatus)
  {
    setAttributeValue("irespstatus", irespstatus);
  }

  public void setModifyStatus(Integer modifystatus)
  {
    setAttributeValue("modifystatus", modifystatus);
  }

  public void setPk_ct_pu(String pk_ct_pu) {
    setAttributeValue("pk_ct_pu", pk_ct_pu);
  }

  public void setPk_pubpsn(String pk_pubpsn)
  {
    setAttributeValue("pk_pubpsn", pk_pubpsn);
  }

  public void setPk_purcorp(String pk_purcorp)
  {
    setAttributeValue("pk_purcorp", pk_purcorp);
  }

  public void setPk_resppsn(String pk_resppsn)
  {
    setAttributeValue("pk_resppsn", pk_resppsn);
  }

  public void setTpubtime(UFDateTime tpubtime)
  {
    setAttributeValue("tpubtime", tpubtime);
  }

  public void setTresptime(UFDateTime tresptime)
  {
    setAttributeValue("tresptime", tresptime);
  }

  public void setVreason(String vreason)
  {
    setAttributeValue("vreason", vreason);
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