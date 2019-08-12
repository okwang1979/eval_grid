package nc.ui.hbbb.dxrelation.formula.refprocessor;

import javax.swing.text.PlainDocument;

import nc.itf.hbbb.constants.HBFmlConst;
import nc.itf.org.IOrgConst;
import nc.ui.bd.ref.AbstractRefModel;
import nc.ui.hbbb.unionreport.project.model.UnionProjectGridRefModel;
import nc.ui.pub.beans.UIRefPane;
import nc.vo.bd.pub.IPubEnumConst;


/**
 * 合并报表项目函数参数用参照
 * @date 20110512
 *
 * @author liyra
 *
 */

public class UnRepProjectProcessor extends RefProcessor  {

	@Override
	public AbstractRefModel getRefModel() {
		AbstractRefModel refModel = new nc.ui.hbbb.unionreport.project.model.UnionProjectGridRefModel();
//		(( HBAccountRefModel)refModel).setPk_hbaccchart(IContrastConst.PK_ACCCHART);
//		refModel.setPk_org(WorkbenchEnvironment.getInstance().getLoginUser().getPk_org()/*getContext().getAttribute(CUR_REPORG_PK).toString()*/);
		if(refModel instanceof UnionProjectGridRefModel)
		{	
			Object pk_group = this.getContext().getAttribute("pk_group");
			if(IOrgConst.GLOBEORG.equals(pk_group)) {
				((UnionProjectGridRefModel)refModel).setInGloble(true);
			}
			else {
				((UnionProjectGridRefModel)refModel).setInGloble(false);
			}
			((UnionProjectGridRefModel)refModel).setClassWherePart("isnull(ufoc_projectclass.dr,0)=0 and ufoc_projectclass.enablestate="+IPubEnumConst.ENABLESTATE_ENABLE);
		}
		return refModel;
	}

	@Override
	public String getRefname() {
		// TODO Auto-generated method stub
		return nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830001-0208")/*@res "合并报表项目"*/;
	}

	@Override
	public VALUETYPE getValueType() {
		// TODO Auto-generated method stub
		return VALUETYPE.PK;
	}

	@Override
	public String getInputValue() {
		// TODO Auto-generated method stub
		StringBuilder content=new StringBuilder();
		content.append("'");
		if(refpane==null){
			return "";
		}
//		content.append(((UnionProjectGridRefModel)refModel).getRefCodeValue());
//		content.append(HBFmlConst.FMLSPLIT);
		if(null==((UIRefPane)refpane).getRefName() || ((UIRefPane)refpane).getRefName().trim().length()==0){
			content.append("");
		}else{
			content.append(((UIRefPane)refpane).getRefName());
		}

		content.append(HBFmlConst.CODENAMESPLIT);
		if(null==((UIRefPane)refpane).getRefCode() || ((UIRefPane)refpane).getRefCode().trim().length()==0){
			content.append("");
		}else {
			content.append(((UIRefPane)refpane).getRefCode());
		}

		content.append("'");
		return content.toString();
	}

}