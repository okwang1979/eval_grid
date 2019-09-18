package nc.itf.hbbb.dxfunction;

import java.util.Map;

import nc.vo.hbbb.dxfunction.ESLECTQryVO;
import nc.vo.hbbb.dxfunction.OPCEQryVO;
import nc.vo.hbbb.dxfunction.TPSUMQryVO;
import nc.vo.hbbb.dxfunction.project.MeasureReportVO;
import nc.vo.iufo.calculate.DatePropVO;
import nc.vo.pub.BusinessException;

import com.ufsoft.script.base.ICalcEnv;

/**
 * ����ģ�庯��
 * @author liyra
 * @date 20110310
 *
 */
public interface IDxModelFunction {
	/**
	 * SREP ����ȡ������
	 * @param pk_account
	 * @return
	 * @throws BusinessException
	 */
	public double getSREP(String projectcode,int isself,int offset, ICalcEnv env) throws BusinessException;
	
	
	public double getESELECT(ESLECTQryVO qryvo, ICalcEnv env) throws BusinessException;
	
    /**
     * CESUM  ������¼�ϼ�ֵȡ������
     * @param accchartcode
     * @param accpropcode
     * @param isself
     * @param datastr
     * @param offset
     * @param env
     * @return
     * @throws BusinessException
     */
	public double getCESUM(String projectcode,int cur_direction,int offset, ICalcEnv env) throws BusinessException; 
	
	/**
	 * DPSUM  ���յ��ۼƾ�������㺯��
	 * @param pk_account
	 * @return
	 * @throws BusinessException
	 */
	public double getDPSUM(TPSUMQryVO qryvo,ICalcEnv env) throws BusinessException;
	
	

    /**
     * INTR �ڲ�����ȡ������
     * @param projectcode
     * @param isself
     * @param datastr
     * @param offset
     * @param env
     * @return
     * @throws BusinessException
     */
	public double getINTR(String projectcode,int isself,int offset, ICalcEnv env) throws BusinessException;
	/**
	 * INTRBYKEY �ڲ�����ȡ������
	 * @param projectcode
	 * @param isself
	 * @param offset
	 * @param env
	 * @param keyword
	 * @return
	 * @throws BusinessException
	 */
	public double getINTRBYKEY(String projectcode,int isself,int offset, String[] otherDynKeyToVal, ICalcEnv env) throws BusinessException;
	
	/**
	 * IPROPORTION ֱ��Ͷ�ʱ�������
	 * @param pk_account
	 * @return
	 * @throws BusinessException
	 */
	public double getIPROPORTION(DatePropVO datevo,int offset, ICalcEnv env) throws BusinessException;
	
	/**
	 * OPCE ������¼�Է���Ŀ�������
	 * @param pk_account
	 * @return
	 * @throws BusinessException
	 */
	public double getOPCE(OPCEQryVO qryvo, ICalcEnv env) throws BusinessException;
	
	
	/**
	 * PTPSUM ����Ӧ���о�������㺯��
	 * @param pk_account
	 * @return
	 * @throws BusinessException
	 */
	public double getPTPSUM(TPSUMQryVO qryvo,ICalcEnv env) throws BusinessException;
	
	
	/**
	 * SINTR �ڲ���������ȡ������
	 * @param pk_account
	 * @return
	 * @throws BusinessException
	 */
	public double getSINTR(String pk_account) throws BusinessException;
	
	
	/**
	 * TPSUM Ӧ�����ۼƾ�������㺯��
	 * @param pk_account
	 * @return
	 * @throws BusinessException
	 */
	public double getTPSUM(TPSUMQryVO qryvo,ICalcEnv env) throws BusinessException;
	
	
	/**
	 * UCHECK UAP�ڲ����׶��˹�����
	 * @param pk_account
	 * @return
	 * @throws BusinessException
	 */
	public double getUCHECK(String accountcode,int isself, ICalcEnv env) throws BusinessException;
	/**
	 * UCHECK UAP�ڲ����׶��˹�����
	 * @param pk_account
	 * @return
	 * @throws BusinessException
	 */
	public double getUCHECKBYKEY(String accountcode,int isself, int dataType, String strCond, String oppOrgPk, ICalcEnv env) throws BusinessException;
	
	/**
	 * ֻ��IPROPTION�����ļ��㲻�Ӻ��㷽ʽ����������
	 * @param pk_investor
	 * @param pk_investee
	 * @param strdate
	 * @return
	 * @throws BusinessException
	 */
	public double getInvestSumData(String pk_investor, String pk_investee,
			String strdate)  throws BusinessException;
	
	/**
	 * �ۼ�Ͷ�ʱ������Ӻ��㷽ʽ����������
	 * @param pk_investor
	 * @param pk_investee
	 * @param strdate
	 * @return
	 * @throws BusinessException
	 */
	public double getInvestSumDataByAssmode(String pk_investor, String pk_investee,
			String strdate) throws BusinessException;
	
	/**
	 * //ĿǰΪ�ڲ�������ĺ���ֻ��intr,dpsum������
	 * @param pk_hbScheme
	 * @param projectCode
	 * @param pk_group
	 * @param isintrade
	 * @return
	 * @throws BusinessException
	 */
	public MeasureReportVO getMeasRepBySchemeProjectCode(String pk_hbScheme,String projectCode,String pk_group,boolean isintrade)  throws BusinessException;
	
	public MeasureReportVO getMeasRepBySchemeProject(String pk_hbScheme,String pk_project,String pk_group,boolean isintrade)  throws BusinessException;
	
    /**
     * ĿǰΪ�ڲ�������ĺ���ֻ��intr,dpsum������
     * 
     * @param pk_hbScheme
     * @param pk_group
     * @param proIntradeMap<projectCode, isIntrade>
     * @return Map<projectCode, MeasureReportVO>
     * @throws BusinessException
     */
    public Map<String, MeasureReportVO> getMeasRepsBySchemeProjCode(String pk_hbScheme, String pk_group,
            Map<String, Boolean> proIntradeMap) throws BusinessException;
    /**
     * ����ѡ��Ĺؼ�����������ѯ��Ӧ�ؼ������� 
     * @param tableName
     * @return
     * @throws BusinessException
     */
    public Object queryChooseKeyValue(String tableName) throws BusinessException;
//    /**
//     * ��ѯ�ؼ��ֱ� iufo_keyword������name������
//     * @param keyWordName
//     * @return
//     * @throws BusinessException
//     */
//    public String queryPKKeyWordBYKeyName(String keyWordName) throws BusinessException;
    /**
     * ��ѯ��Ӧ�ؼ������ݵ�pk
     * @param keyWordName 
     * @param code
     * @return <keywordpk,valuepk>
     * @throws BusinessException
     */
    public String[] queryPKChooseKeyBYCode(String keyName, String code) throws BusinessException;
}
