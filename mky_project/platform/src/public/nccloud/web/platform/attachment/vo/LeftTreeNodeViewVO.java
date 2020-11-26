/*    */ package nccloud.web.platform.attachment.vo;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class LeftTreeNodeViewVO {
/*    */    private LeftTreeNodeViewVO[] children;
/*    */    private String fullPath;
/*    */    private String label;
/*    */ 
/*    */    public LeftTreeNodeViewVO[] getChildren() {
/* 12 */       return this.children;
/*    */    }
/*    */ 
/*    */    public String getFullPath() {
/* 16 */       return this.fullPath;
/*    */    }
/*    */ 
/*    */    public String getLabel() {
/* 20 */       return this.label;
/*    */    }
/*    */ 
/*    */    public void setChildren(LeftTreeNodeViewVO[] children) {
/* 24 */       this.children = children;
/* 25 */    }
/*    */ 
/*    */    public void setFullPath(String fullPath) {
/* 28 */       this.fullPath = fullPath;
/* 29 */    }
/*    */ 
/*    */    public void setLabel(String label) {
/* 32 */       this.label = label;
/* 33 */    }
/*    */ }
