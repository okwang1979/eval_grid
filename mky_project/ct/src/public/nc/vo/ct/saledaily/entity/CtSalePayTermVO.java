package nc.vo.ct.saledaily.entity;

import nc.vo.ct.entity.CtAbstractPayTermVO;
import nc.vo.ct.enumeration.CtEntity;
import nc.vo.pub.IVOMeta;
import nc.vo.pubapp.pattern.model.meta.entity.vo.VOMetaFactory;
import nc.vo.pubapp.res.NCModule;

public class CtSalePayTermVO extends CtAbstractPayTermVO
{
  public static final String PK_CT_SALE = "pk_ct_sale";
  public static final String PK_CT_SALE_PAYTERM = "pk_ct_sale_payterm";
  private static final long serialVersionUID = 5665553384760633533L;

  public IVOMeta getMetaData()
  {
    IVOMeta meta = VOMetaFactory.getInstance().getVOMeta(NCModule.CT
      .getName().toLowerCase() + "." + CtEntity.ct_sale_payterm
      .name());
    return meta;
  }

  public String getPk_ct_sale() {
    return (String)getAttributeValue("pk_ct_sale");
  }

  public String getPk_ct_sale_payterm() {
    return (String)getAttributeValue("pk_ct_sale_payterm");
  }

  public void setPk_ct_sale(String pk_ct_sale) {
    setAttributeValue("pk_ct_sale", pk_ct_sale);
  }

  public void setPk_ct_sale_payterm(String pk_ct_sale_payterm) {
    setAttributeValue("pk_ct_sale_payterm", pk_ct_sale_payterm);
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