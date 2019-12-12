/*     */ package nc.ui.bd.ref.model;
/*     */ 
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ import javax.swing.tree.DefaultMutableTreeNode;
/*     */ import nc.bs.sec.esapi.NCESAPI;
/*     */ import nc.ui.bd.ref.AbstractRefModel;
/*     */ import nc.vo.bd.psn.PsnjobVO;
/*     */ import nc.vo.ml.AbstractNCLangRes;
/*     */ import nc.vo.ml.NCLangRes4VoTransl;
/*     */ import nc.vo.org.DeptVO;
/*     */ import nc.vo.org.OrgVO;
/*     */ import nc.vo.org.util.OrgTreeCellRendererIconPolicy;
/*     */ import nc.vo.pub.lang.UFBoolean;
/*     */ import nc.vo.relation.AdminDeptVO;
/*     */ import nc.vo.relation.BusiFuncVO;
/*     */ import nc.vo.util.SqlWhereUtil;
/*     */ import org.apache.commons.lang.ArrayUtils;
/*     */ import org.apache.commons.lang.StringUtils;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class PsndocDefaultNCRefModel
/*     */   extends PsndocDefaultRefModel
/*     */ {
/*  33 */   private String busifuncode = null;
/*     */   
/*     */   private String[] orgFilterPks;
/*     */   private Map<String, AbstractRefModel> filterRefMap;
/*     */   
/*     */   public String[] getOrgFilterPks()
/*     */   {
/*  40 */     return this.orgFilterPks;
/*     */   }
/*     */   
/*     */   public void setOrgFilterPks(String[] orgFilterPks) {
/*  44 */     this.orgFilterPks = orgFilterPks;
/*     */   }
/*     */   
/*     */   public PsndocDefaultNCRefModel() {
/*  48 */     reset();
/*     */   }
/*     */   
/*     */   public void reset()
/*     */   {
/*  53 */     super.reset();
/*  54 */     if (StringUtils.isNotBlank(getBusifuncode())) {
/*  55 */       setClassFieldCode(new String[] { "code", "name", "pk_org", "pk_fatherorg", "isbusinessunit" });
/*     */       
/*  57 */       setFatherField("pk_fatherorg");
/*  58 */       setChildField("pk_dept");
/*  59 */       setClassTableName("(select code ,name,name2,name3,name4,name5,name6,pk_org,case when orgtype3 = 'Y' and isnull(pk_fatherorg, '~') = '~' then pk_ownorg else pk_fatherorg end as pk_fatherorg,isbusinessunit,enablestate,pk_ownorg , orgtype3 from " + OrgVO.getDefaultTableName() + " where (" + "orgtype3" + " = 'Y' or " + "isbusinessunit" + " = 'Y')) temp_dept");
/*     */       
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*  68 */       setClassJoinField("pk_org");
/*     */     }
/*  70 */     setTreeIconPolicy(new OrgTreeCellRendererIconPolicy("Bu") {
/*     */       public String getSpecialNodeIcon(Object curTreeNode) {
/*  72 */         if (((curTreeNode instanceof DefaultMutableTreeNode)) && (StringUtils.isNotBlank(PsndocDefaultNCRefModel.this.getBusifuncode())))
/*     */         {
/*  74 */           DefaultMutableTreeNode n = (DefaultMutableTreeNode)curTreeNode;
/*  75 */           Object o = n.getUserObject();
/*  76 */           if ((o instanceof Vector))
/*     */           {
/*  78 */             Vector v = (Vector)o;
/*  79 */             String isbusinessunit = (String)v.get(PsndocDefaultNCRefModel.this.getClassFieldIndex("isbusinessunit"));
/*  80 */             if (UFBoolean.valueOf(isbusinessunit).booleanValue()) {
/*  81 */               return "Bu";
/*     */             }
/*  83 */             return "Department";
/*     */           }
/*     */         }
/*     */         
/*  87 */         return null;
/*     */       }
/*  89 */     });
/*  90 */     setFieldCode(new String[] { "bd_psndoc.code", "bd_psndoc.name", "bd_psnjob.pk_dept" });
/*  91 */     setFieldName(new String[] { NCLangRes4VoTransl.getNCLangRes().getStrByID("10140psn", "010140psn0065"), NCLangRes4VoTransl.getNCLangRes().getStrByID("10140psn", "010140psn0066"), NCLangRes4VoTransl.getNCLangRes().getStrByID("10140psn", "010140psn0085") });
/*     */     
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*  97 */     String deptFomula = "getMLCValue(\"" + DeptVO.getDefaultTableName() + "\",\"" + "name" + "\",\"" + "pk_dept" + "\"," + "bd_psnjob.pk_dept" + ")";
/*     */     
/*     */ 
/* 100 */     setFormulas(new String[][] { { "bd_psnjob.pk_dept", deptFomula } });
/* 101 */     setHiddenFieldCode(new String[] { "bd_psndoc.pk_psndoc", "bd_psnjob.pk_psnjob", "bd_psndoc.idtype", "bd_psndoc.id" });
/*     */     
/* 103 */     setDefaultFieldCount(3);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public void setFilterRefMap(Map<String, AbstractRefModel> filterRefMap)
/*     */   {
/* 112 */     this.filterRefMap = filterRefMap;
/* 113 */     if ((!ArrayUtils.isEmpty(this.orgFilterPks)) && (getFilterRefModel("业务单元") != null)) {
/* 114 */       getFilterRefModel("业务单元").setFilterPks(this.orgFilterPks);
/*     */     }
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   public AbstractRefModel getFilterRefModel(String refNodeName)
/*     */   {
/* 122 */     if (this.filterRefMap == null) {
/* 123 */       return null;
/*     */     }
/* 125 */     return (AbstractRefModel)this.filterRefMap.get(refNodeName);
/*     */   }
/*     */   
/*     */   public void setBusifuncode(String busifuncode) {
/* 129 */     this.busifuncode = busifuncode;
/* 130 */     reset();
/*     */   }
/*     */   
/*     */   public String getBusifuncode() {
/* 134 */     if ((getKeyValueExtendMap() != null) && 
/* 135 */       (getKeyValueExtendMap().get(IBusiConst.BUSIFUNCODE) != null)) {
/* 136 */       return ((String)getKeyValueExtendMap().get(IBusiConst.BUSIFUNCODE)).toLowerCase();
/*     */     }
/*     */     
/* 139 */     return this.busifuncode;
/*     */   }
/*     */   
/*     */   public String getClassWherePart()
/*     */   {
/* 144 */    	String rtn = " pk_org in(select pk_org from org_orgs where innercode like 'DV8Q7T2NE0RF%' ) or  pk_org in(  select pk_dept from org_dept where pk_org in(select pk_org from org_orgs where innercode like 'DV8Q7T2NE0RF%'))";
return rtn;
/*     */   }
/*     */   
/*     */   protected String getEnvWherePart()
/*     */   {
	
	StringBuffer sb = new StringBuffer();
	sb.append("11=11");
//	sb.append(PsnjobVO.getDefaultTableName() + "." + "pk_org" + " = '"
//			+ NCESAPI.clientSqlEncode(getPk_org()) + "'");
	sb.append(getIsLeaveCondition());
//	sb.append(" and ( bd_psnjob.pk_dept  in(  select pk_dept from org_dept where pk_org in(select pk_org from org_orgs where innercode like 'DV8Q7T2NE0RF%'))) ");
	return sb.toString();
}
/*     */   
/*     */ 
/*     */   private String getBusifuncCondition()
/*     */   {
/* 192 */     if (getBusifuncode().equals("all")) {
/* 193 */       return "pk_busirole in (select pk_busichild from " + BusiFuncVO.getDefaultTableName() + " where " + "pk_org" + " = '" + NCESAPI.clientSqlEncode(getPk_org()) + "')";
/*     */     }
/*     */     
/*     */ 
/* 197 */     return "pk_busirole in (select pk_busichild from " + BusiFuncVO.getDefaultTableName() + " where " + "pk_org" + " = '" + NCESAPI.clientSqlEncode(getPk_org()) + "' and " + "org_function" + " like '%" + NCESAPI.clientSqlEncode(getBusifuncode()) + "%')";
/*     */   }
/*     */ }

