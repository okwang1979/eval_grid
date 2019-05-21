/**
 * 
 */
package nc.ui.hbbb.dxrelation.formula.refprocessor;

import nc.ui.bd.ref.AbstractRefModel;
import nc.vo.iufo.keydef.KeyVO;

/**
 * �ؼ��ֲ��յ��²���ģ��
 * ����ģ�͵���������������
 * ��һ�������Զ��嵵�����ò���ģ��Ĭ�ϵķ�ʽ��ȡ
 * �ڶ������������ļ������õ�Ԥ�õ����������ȡ���ݣ��ϲ����Զ��嵵����
 *
 */
public class HBBBKeyWordListRefModel extends AbstractRefModel {
	
 	public HBBBKeyWordListRefModel(){
		super();
		reset();
	}

	public void reset() {
		setRefTitle("�ؼ��ֵ����б�");
		setFieldCode(new String[] { KeyVO.NAME, KeyVO.CODE });
		setFieldName(new String[] { "�ؼ�������", "�ؼ��ֱ���"});

		setHiddenFieldCode(new String[] { KeyVO.PK_KEYWORD,KeyVO.REF_PK });
		setRefCodeField(KeyVO.NAME);
		setRefNameField(KeyVO.NAME);
		setTableName("iufo_keyword");
		setPkFieldCode(KeyVO.PK_KEYWORD);
		setCaseSensive(true);
		
		resetFieldName();
	}
	
	
	protected String getEnvWherePart() {
		return "ref_pk <> '~'and ( CODE is null or code!= 'corp')";
	}

}
