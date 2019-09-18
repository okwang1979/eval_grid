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
 * 抵销模板函数
 * @author liyra
 * @date 20110310
 *
 */
public interface IDxModelFunction {
	/**
	 * SREP 报表取数函数
	 * @param pk_account
	 * @return
	 * @throws BusinessException
	 */
	public double getSREP(String projectcode,int isself,int offset, ICalcEnv env) throws BusinessException;
	
	
	public double getESELECT(ESLECTQryVO qryvo, ICalcEnv env) throws BusinessException;
	
    /**
     * CESUM  抵销分录合计值取数函数
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
	 * DPSUM  已收到累计净利润计算函数
	 * @param pk_account
	 * @return
	 * @throws BusinessException
	 */
	public double getDPSUM(TPSUMQryVO qryvo,ICalcEnv env) throws BusinessException;
	
	

    /**
     * INTR 内部交易取数函数
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
	 * INTRBYKEY 内部交易取数函数
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
	 * IPROPORTION 直接投资比例函数
	 * @param pk_account
	 * @return
	 * @throws BusinessException
	 */
	public double getIPROPORTION(DatePropVO datevo,int offset, ICalcEnv env) throws BusinessException;
	
	/**
	 * OPCE 抵销分录对方项目发生额函数
	 * @param pk_account
	 * @return
	 * @throws BusinessException
	 */
	public double getOPCE(OPCEQryVO qryvo, ICalcEnv env) throws BusinessException;
	
	
	/**
	 * PTPSUM 当年应享有净利润计算函数
	 * @param pk_account
	 * @return
	 * @throws BusinessException
	 */
	public double getPTPSUM(TPSUMQryVO qryvo,ICalcEnv env) throws BusinessException;
	
	
	/**
	 * SINTR 内部购销交易取数函数
	 * @param pk_account
	 * @return
	 * @throws BusinessException
	 */
	public double getSINTR(String pk_account) throws BusinessException;
	
	
	/**
	 * TPSUM 应享有累计净利润计算函数
	 * @param pk_account
	 * @return
	 * @throws BusinessException
	 */
	public double getTPSUM(TPSUMQryVO qryvo,ICalcEnv env) throws BusinessException;
	
	
	/**
	 * UCHECK UAP内部交易对账规则函数
	 * @param pk_account
	 * @return
	 * @throws BusinessException
	 */
	public double getUCHECK(String accountcode,int isself, ICalcEnv env) throws BusinessException;
	/**
	 * UCHECK UAP内部交易对账规则函数
	 * @param pk_account
	 * @return
	 * @throws BusinessException
	 */
	public double getUCHECKBYKEY(String accountcode,int isself, int dataType, String strCond, String oppOrgPk, ICalcEnv env) throws BusinessException;
	
	/**
	 * 只有IPROPTION函数的计算不加核算方式的条件过滤
	 * @param pk_investor
	 * @param pk_investee
	 * @param strdate
	 * @return
	 * @throws BusinessException
	 */
	public double getInvestSumData(String pk_investor, String pk_investee,
			String strdate)  throws BusinessException;
	
	/**
	 * 累计投资比例：加核算方式的条件过滤
	 * @param pk_investor
	 * @param pk_investee
	 * @param strdate
	 * @return
	 * @throws BusinessException
	 */
	public double getInvestSumDataByAssmode(String pk_investor, String pk_investee,
			String strdate) throws BusinessException;
	
	/**
	 * //目前为内部交易类的函数只有intr,dpsum俩函数
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
     * 目前为内部交易类的函数只有intr,dpsum俩函数
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
     * 根据选择的关键字条件，查询相应关键字内容 
     * @param tableName
     * @return
     * @throws BusinessException
     */
    public Object queryChooseKeyValue(String tableName) throws BusinessException;
//    /**
//     * 查询关键字表 iufo_keyword，根据name得主键
//     * @param keyWordName
//     * @return
//     * @throws BusinessException
//     */
//    public String queryPKKeyWordBYKeyName(String keyWordName) throws BusinessException;
    /**
     * 查询相应关键字内容的pk
     * @param keyWordName 
     * @param code
     * @return <keywordpk,valuepk>
     * @throws BusinessException
     */
    public String[] queryPKChooseKeyBYCode(String keyName, String code) throws BusinessException;
}
