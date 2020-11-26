package nc.vo.tb.form.excel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExVarAreaDef implements Serializable, Cloneable {
	private static final long serialVersionUID = -1648839781081186551L;

	public final static int varAreatType_ROW = 0;	 // �и���
	public final static int varAreatType_COL = 1;	 // �и���
	public final static int varAreaSumType_default = -1; // ���ܷ�ʽ��Ĭ��
	public final static int varAreaSumType_LIST = 0; // ���л���
	public final static int varAreaSumType_SUM = 1;  // �������
	public final static int varAreaSumType_NONE = 2; // ������
	public final static int isAutoExpandByDataCell_YES = 1;	// �ø�����δ�༭ǰ�������ж�ά���ݼ���
	public final static int isAutoExpandByDataCell_NO = -1;	// �ø����������ն�ά���ݼ���
	public final static int isAutoExpandByDataCell_ALL = 2;	// �ø�����ʼ�հ��ն�ά���ݼ��� -->> lrx 2014-7-21 V635����
	
	/** ��������ʶ */
	public String varID;
	
	/** ���������ͣ���/�У�Ĭ��Ϊ�и����� */
	public int varAreaType = varAreatType_ROW;
	
	/** �������������ͣ�����/����/�����ܣ�Ĭ��Ϊ���л���==>>Ĭ��ΪĬ��==��ά�����࣬�Ƕ�ά������ */
	public int varAreaSumType = varAreaSumType_default/*varAreaSumType_LIST*/;
	
//	/** �������ʱ��д�����ܻ�׼��/�����(ȡֵͬExVarDef.index) */
//	public List<Integer> varSumIndexes;
	
	/** lrx 2013-11-21 ��������������û���ѱ�����Ϣʱ���Ƿ����Ѵ��ڵĶ�ά���ݼ���(Ĭ��Ϊ��) */
	public int isAutoExpandByDataCell = isAutoExpandByDataCell_NO;

	/** ��������/��ά�ȶ������ݼ���, �μ�ExVarDef���� */
	public List<ExVarDef> varDefList;
	
	public ExVarAreaDef cloneNew() {
		ExVarAreaDef rtn = new ExVarAreaDef();
		rtn.varID = varID;
		rtn.varAreaType = varAreaType;
		rtn.varAreaSumType = varAreaSumType;
		rtn.isAutoExpandByDataCell = isAutoExpandByDataCell;
		if (varDefList != null) {
			rtn.varDefList = new ArrayList<ExVarDef>();
			for (ExVarDef vd : varDefList)
				rtn.varDefList.add(vd);
		}
		return rtn;
	}
}
