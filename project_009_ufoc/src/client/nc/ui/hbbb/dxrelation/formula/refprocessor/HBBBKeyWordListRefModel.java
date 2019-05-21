/**
 * 
 */
package nc.ui.hbbb.dxrelation.formula.refprocessor;

import nc.ui.bd.ref.AbstractRefModel;
import nc.vo.iufo.keydef.KeyVO;

/**
 * 关键字参照的新参照模型
 * 参照模型的数据来自两部分
 * 第一部分是自定义档案，用参照模型默认的方式读取
 * 第二部分是配置文件中配置的预置档案，额外读取数据，合并到自定义档案中
 *
 */
public class HBBBKeyWordListRefModel extends AbstractRefModel {
	
 	public HBBBKeyWordListRefModel(){
		super();
		reset();
	}

	public void reset() {
		setRefTitle("关键字档案列表");
		setFieldCode(new String[] { KeyVO.NAME, KeyVO.CODE });
		setFieldName(new String[] { "关键字名称", "关键字编码"});

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
