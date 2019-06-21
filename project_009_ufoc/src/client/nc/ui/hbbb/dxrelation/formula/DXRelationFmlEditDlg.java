/**
 *
 */
package nc.ui.hbbb.dxrelation.formula;

import java.awt.Container;

import nc.ui.iufo.formula.common.ComFmlEditDlg;

import com.ufida.dataset.IContext;
import com.ufsoft.script.cmdcontrol.ConditionOfRun;

/**
 * ����ģ�幫ʽ����
 * @author jiaah
 * @created at 2010-10-12,����07:04:10
 *
 */
public class DXRelationFmlEditDlg extends ComFmlEditDlg {

	private static final long serialVersionUID = 1L;

	protected DXRelationFmlEditPane fmlEditPane = null;


	private String pk_accchart;

	/**
	 * ���췽�������ڵ���ģ�幫ʽ
	 * @create by jiaah at 2010-10-12,����07:16:58
	 * @param parent
	 * @param conditionRun
	 * @param fmlContent
	 * @param contextVO
	 * @param isInnerCheck
	 */
	public DXRelationFmlEditDlg(Container parent, ConditionOfRun conditionRun,
			String fmlContent, IContext contextVO, boolean isInnerCheck,String newpk_accchart,String schemeType) {
		super(parent, nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0055")/*@res "����ģ�幫ʽ�༭"*/);//"����ģ�幫ʽ�༭"
		this.setPk_accchart(newpk_accchart);
		contextVO.setAttribute("1001Z31000000000F0W9",newpk_accchart);
		this.fmlEditPane = new DXRelationFmlEditPane(this, conditionRun,
				fmlContent, contextVO, isInnerCheck,schemeType);
		this.setContentPane(fmlEditPane);
//		this.setSize(600, 530);
		this.setSize(750, 640);
	}

	/**
	 * ��������
	 */
	@Override
	protected void setFocusGained() {
		fmlEditPane.setFocus();
	}

	public void setEditPaneEnable(boolean enabled){
		fmlEditPane.setEditPaneEnable(enabled);
	}

	/**
	 * ��ȡ��ʽ����
	 * @return
	 */
	public String getFormulaContent(){
		return fmlEditPane.getFormulaContent();
	}

	public void setFormulaContent(String fmlContent){
		fmlEditPane.setFormulaContent(fmlContent);
	}

	/**
	 * ��ȡ������Ĺ�ʽ����
	 */
	public String getParsedCheckFm() {
		return fmlEditPane.getParsedCheckFm();
	}

	public String getPk_accchart() {
		return pk_accchart;
	}

	private void setPk_accchart(String pk_accchart) {
		this.pk_accchart = pk_accchart;
	}

}
