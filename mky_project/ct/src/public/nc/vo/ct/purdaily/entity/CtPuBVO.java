package nc.vo.ct.purdaily.entity;

import nc.vo.ct.entity.CtAbstractBVO;
import nc.vo.ct.enumeration.CtEntity;
import nc.vo.pub.IVOMeta;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;
import nc.vo.pubapp.pattern.model.meta.entity.vo.VOMetaFactory;
import nc.vo.pubapp.res.NCModule;

public class CtPuBVO extends CtAbstractBVO
{
  public static final String CASSCUSTID = "casscustid";
  public static final String CBPROJECTID = "cbprojectid";
  public static final String CECBILLBID = "cecbillbid";
  public static final String CECBILLID = "cecbillid";
  public static final String CECTYPECODE = "cectypecode";
  public static final String CPRAYBILLROWNO = "cpraybillrowno";
  public static final String CQPBASESCHEMEID = "cqpbaseschemeid";
  public static final String NSCHEDULERNUM = "nschedulernum";
  public static final String PK_ARRVSTOCK = "pk_arrvstock";
  public static final String PK_ARRVSTOCK_V = "pk_arrvstock_v";
  public static final String PK_CT_PRICE = "pk_ct_price";
  public static final String PK_CT_PU = "pk_ct_pu";
  public static final String PK_CT_PU_B = "pk_ct_pu_b";
  public static final String PK_CTRELATING = "pk_ctrelating";
  public static final String PK_CTRELATING_B = "pk_ctrelating_b";
  public static final String PK_ECMCT = "pk_ecmct";
  public static final String PK_ECMCT_B = "pk_ecmct_b";
  public static final String PK_ORIGCTB = "pk_origctb";
  public static final String PK_PRAYBILL = "pk_praybill";
  public static final String PK_PRAYBILL_B = "pk_praybill_b";
  public static final String VCTBILLCODE = "vctbillcode";
  public static final String VCTBILLTYPE = "vctbilltype";
  public static final String VECBILLCODE = "vecbillcode";
  public static final String VECMCTBILLCODE = "vecmctbillcode";
  public static final String VPRAYBILLCODE = "vpraybillcode";
  
  //modify zhangfxs 增加自定义字段
  public static final String VBDEF21 = "vbdef21";
  public static final String VBDEF22 = "vbdef22";
  public static final String VBDEF23 = "vbdef23";
  public static final String VBDEF24 = "vbdef24";
  public static final String VBDEF25 = "vbdef25";
  public static final String VBDEF26 = "vbdef26";
  public static final String VBDEF27 = "vbdef27";
  public static final String VBDEF28 = "vbdef28";
  public static final String VBDEF29 = "vbdef29";
  public static final String VBDEF30 = "vbdef30";
  public static final String VBDEF31 = "vbdef31";
  public static final String VBDEF32 = "vbdef32";
  public static final String VBDEF33 = "vbdef33";
  public static final String VBDEF34 = "vbdef34";
  public static final String VBDEF35 = "vbdef35";
  public static final String VBDEF36 = "vbdef36";
  public static final String VBDEF37 = "vbdef37";
  public static final String VBDEF38 = "vbdef38";
  public static final String VBDEF39 = "vbdef39";
  public static final String VBDEF40 = "vbdef40";
  //end
  
  private static final long serialVersionUID = -1011715213772642474L;

  public UFBoolean getBtriatradeflag()
  {
    return (UFBoolean)getAttributeValue("btriatradeflag");
  }

  public String getCasscustid()
  {
    return (String)getAttributeValue("casscustid");
  }

  public String getCbprojectid()
  {
    return (String)getAttributeValue("cbprojectid");
  }

  public String getCecbillbid()
  {
    return (String)getAttributeValue("cecbillbid");
  }

  public String getCecbillid()
  {
    return (String)getAttributeValue("cecbillid");
  }

  public String getCectypecode()
  {
    return (String)getAttributeValue("cectypecode");
  }

  public String getCpraybillrowno()
  {
    return (String)getAttributeValue("cpraybillrowno");
  }

  public String getCqpbaseschemeid()
  {
    return (String)getAttributeValue("cqpbaseschemeid");
  }

  public String getCrececountryid()
  {
    return (String)getAttributeValue("crececountryid");
  }

  public String getCsendcountryid()
  {
    return (String)getAttributeValue("csendcountryid");
  }

  public String getCtaxcountryid()
  {
    return (String)getAttributeValue("ctaxcountryid");
  }

  public Integer getFbuysellflag()
  {
    return (Integer)getAttributeValue("fbuysellflag");
  }

  public IVOMeta getMetaData()
  {
    IVOMeta meta = VOMetaFactory.getInstance()
      .getVOMeta(NCModule.CT
      .getName().toLowerCase() + "." + CtEntity.ct_pu_b
      .name());
    return meta;
  }

  public UFDouble getNcalcostmny()
  {
    return (UFDouble)getAttributeValue("ncalcostmny");
  }

  public UFDouble getNnosubtax()
  {
    return (UFDouble)getAttributeValue("nnosubtax");
  }

  public UFDouble getNnosubtaxrate()
  {
    return (UFDouble)getAttributeValue("nnosubtaxrate");
  }

  public UFDouble getNschedulernum() {
    return (UFDouble)getAttributeValue("nschedulernum");
  }

  public String getPk_arrvstock()
  {
    return (String)getAttributeValue("pk_arrvstock");
  }

  public String getPk_arrvstock_v()
  {
    return (String)getAttributeValue("pk_arrvstock_v");
  }

  public String getPk_ct_price() {
    return (String)getAttributeValue("pk_ct_price");
  }

  public String getPk_ct_pu() {
    return (String)getAttributeValue("pk_ct_pu");
  }

  public String getPk_ct_pu_b() {
    return (String)getAttributeValue("pk_ct_pu_b");
  }

  public String getPk_ctrelating() {
    return (String)getAttributeValue("pk_ctrelating");
  }

  public String getPk_ctrelating_b() {
    return (String)getAttributeValue("pk_ctrelating_b");
  }

  public String getPk_ecmct()
  {
    return (String)getAttributeValue("pk_ecmct");
  }

  public String getPk_ecmct_b()
  {
    return (String)getAttributeValue("pk_ecmct_b");
  }

  public String getPk_origctb()
  {
    return (String)getAttributeValue("pk_origctb");
  }

  public String getPk_praybill()
  {
    return (String)getAttributeValue("pk_praybill");
  }

  public String getPk_praybill_b()
  {
    return (String)getAttributeValue("pk_praybill_b");
  }

  public String getVctbillcode() {
    return (String)getAttributeValue("vctbillcode");
  }

  public String getVctbilltype() {
    return (String)getAttributeValue("vctbilltype");
  }

  public String getVecbillcode()
  {
    return (String)getAttributeValue("vecbillcode");
  }

  public String getVecmctbillcode()
  {
    return (String)getAttributeValue("vecmctbillcode");
  }

  public String getVpraybillcode()
  {
    return (String)getAttributeValue("vpraybillcode");
  }

  public void setBtriatradeflag(UFBoolean btriatradeflag)
  {
    setAttributeValue("btriatradeflag", btriatradeflag);
  }

  public void setCasscustid(String casscustid)
  {
    setAttributeValue("casscustid", casscustid);
  }

  public void setCbprojectid(String cbprojectid)
  {
    setAttributeValue("cbprojectid", cbprojectid);
  }

  public void setCecbillbid(String cecbillbid)
  {
    setAttributeValue("cecbillbid", cecbillbid);
  }

  public void setCecbillid(String cecbillid)
  {
    setAttributeValue("cecbillid", cecbillid);
  }

  public void setCectypecode(String cectypecode)
  {
    setAttributeValue("cectypecode", cectypecode);
  }

  public void setCpraybillrowno(String cpraybillrowno)
  {
    setAttributeValue("cpraybillrowno", cpraybillrowno);
  }

  public void setCqpbaseschemeid(String cqpbaseschemeid)
  {
    setAttributeValue("cqpbaseschemeid", cqpbaseschemeid);
  }

  public void setCrececountryid(String crececountryid)
  {
    setAttributeValue("crececountryid", crececountryid);
  }

  public void setCsendcountryid(String csendcountryid)
  {
    setAttributeValue("csendcountryid", csendcountryid);
  }

  public void setCtaxcountryid(String ctaxcountryid)
  {
    setAttributeValue("ctaxcountryid", ctaxcountryid);
  }

  public void setFbuysellflag(Integer fbuysellflag)
  {
    setAttributeValue("fbuysellflag", fbuysellflag);
  }

  public void setNcalcostmny(UFDouble ncalcostmny)
  {
    setAttributeValue("ncalcostmny", ncalcostmny);
  }

  public void setNnosubtax(UFDouble nnosubtax)
  {
    setAttributeValue("nnosubtax", nnosubtax);
  }

  public void setNnosubtaxrate(UFDouble nnosubtaxrate)
  {
    setAttributeValue("nnosubtaxrate", nnosubtaxrate);
  }

  public void setNschedulernum(UFDouble nschedulernum) {
    setAttributeValue("nschedulernum", nschedulernum);
  }

  public void setPk_arrvstock(String pk_arrvstock)
  {
    setAttributeValue("pk_arrvstock", pk_arrvstock);
  }

  public void setPk_arrvstock_v(String pk_arrvstock_v)
  {
    setAttributeValue("pk_arrvstock_v", pk_arrvstock_v);
  }

  public void setPk_ct_price(String pk_ct_price) {
    setAttributeValue("pk_ct_price", pk_ct_price);
  }

  public void setPk_ct_pu(String pk_ct_pu) {
    setAttributeValue("pk_ct_pu", pk_ct_pu);
  }

  public void setPk_ct_pu_b(String pk_ct_pu_b) {
    setAttributeValue("pk_ct_pu_b", pk_ct_pu_b);
  }

  public void setPk_ctrelating(String pk_ctrelating) {
    setAttributeValue("pk_ctrelating", pk_ctrelating);
  }

  public void setPk_ctrelating_b(String pk_ctrelating_b) {
    setAttributeValue("pk_ctrelating_b", pk_ctrelating_b);
  }

  public void setPk_ecmct(String pk_ecmct)
  {
    setAttributeValue("pk_ecmct", pk_ecmct);
  }

  public void setPk_ecmct_b(String pk_ecmct_b)
  {
    setAttributeValue("pk_ecmct_b", pk_ecmct_b);
  }

  public void setPk_origctb(String pk_origctb)
  {
    setAttributeValue("pk_origctb", pk_origctb);
  }

  public void setPk_praybill(String pk_praybill)
  {
    setAttributeValue("pk_praybill", pk_praybill);
  }

  public void setPk_praybill_b(String pk_praybill_b)
  {
    setAttributeValue("pk_praybill_b", pk_praybill_b);
  }

  public void setVctbillcode(String vctbillcode) {
    setAttributeValue("vctbillcode", vctbillcode);
  }

  public void setVctbilltype(String vctbilltype) {
    setAttributeValue("vctbilltype", vctbilltype);
  }

  public void setVecbillcode(String vecbillcode)
  {
    setAttributeValue("vecbillcode", vecbillcode);
  }

  public void setVecmctbillcode(String vecmctbillcode)
  {
    setAttributeValue("vecmctbillcode", vecmctbillcode);
  }

  public void setVpraybillcode(String vpraybillcode)
  {
    setAttributeValue("vpraybillcode", vpraybillcode);
  }
  //modify zhangfxs 2020/11/03
  public String getVbdef21() {
	    return (String)getAttributeValue("vbdef21");
  }
  public void setVbdef21(String vbdef21) {
	    setAttributeValue("vbdef21", vbdef21);
  }
  
  public String getVbdef22() {
	    return (String)getAttributeValue("vbdef22");
  }
  public void setVbdef22(String vbdef22) {
	    setAttributeValue("vbdef22", vbdef22);
  }

  public String getVbdef23() {
    return (String)getAttributeValue("vbdef23");
  }
  public void setVbdef23(String vbdef23) {
    setAttributeValue("vbdef23", vbdef23);
  }

  public String getVbdef24() {
    return (String)getAttributeValue("vbdef24");
  }
  public void setVbdef24(String vbdef24) {
    setAttributeValue("vbdef24", vbdef24);
  }

  public String getVbdef25() {
    return (String)getAttributeValue("vbdef25");
  }
  public void setVbdef25(String vbdef25) {
    setAttributeValue("vbdef25", vbdef25);
  }

  public String getVbdef26() {
    return (String)getAttributeValue("vbdef26");
  }
  public void setVbdef26(String vbdef26) {
    setAttributeValue("vbdef26", vbdef26);
  }

  public String getVbdef27() {
    return (String)getAttributeValue("vbdef27");
  }
  public void setVbdef27(String vbdef27) {
    setAttributeValue("vbdef27", vbdef27);
  }

  public String getVbdef28() {
    return (String)getAttributeValue("vbdef28");
  }
  public void setVbdef28(String vbdef28) {
    setAttributeValue("vbdef28", vbdef28);
  }

  public String getVbdef29() {
    return (String)getAttributeValue("vbdef29");
  }
  public void setVbdef29(String vbdef29) {
    setAttributeValue("vbdef29", vbdef29);
  }

  public String getVbdef30() {
    return (String)getAttributeValue("vbdef30");
  }
  public void setVbdef30(String vbdef30) {
    setAttributeValue("vbdef30", vbdef30);
  }

  public String getVbdef31() {
    return (String)getAttributeValue("vbdef31");
  }
  public void setVbdef31(String vbdef31) {
    setAttributeValue("vbdef31", vbdef31);
  }

  public String getVbdef32() {
    return (String)getAttributeValue("vbdef32");
  }
  public void setVbdef32(String vbdef32) {
    setAttributeValue("vbdef32", vbdef32);
  }

  public String getVbdef33() {
    return (String)getAttributeValue("vbdef33");
  }
  public void setVbdef33(String vbdef33) {
    setAttributeValue("vbdef33", vbdef33);
  }

  public String getVbdef34() {
    return (String)getAttributeValue("vbdef34");
  }
  public void setVbdef34(String vbdef34) {
    setAttributeValue("vbdef34", vbdef34);
  }

  public String getVbdef35() {
    return (String)getAttributeValue("vbdef35");
  }
  public void setVbdef35(String vbdef35) {
    setAttributeValue("vbdef35", vbdef35);
  }

  public String getVbdef36() {
    return (String)getAttributeValue("vbdef36");
  }
  public void setVbdef36(String vbdef36) {
    setAttributeValue("vbdef36", vbdef36);
  }

  public String getVbdef37() {
    return (String)getAttributeValue("vbdef37");
  }
  public void setVbdef37(String vbdef37) {
    setAttributeValue("vbdef37", vbdef37);
  }

  public String getVbdef38() {
    return (String)getAttributeValue("vbdef38");
  }
  public void setVbdef38(String vbdef38) {
    setAttributeValue("vbdef38", vbdef38);
  }

  public String getVbdef39() {
    return (String)getAttributeValue("vbdef39");
  }
  public void setVbdef39(String vbdef39) {
    setAttributeValue("vbdef39", vbdef39);
  }

  public String getVbdef40() {
    return (String)getAttributeValue("vbde40");
  }
  public void setVbdef40(String vbdef40) {
    setAttributeValue("vbdef40", vbdef40);
  }
//end
}