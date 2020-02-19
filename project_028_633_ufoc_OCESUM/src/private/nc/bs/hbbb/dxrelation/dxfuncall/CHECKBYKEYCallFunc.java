package nc.bs.hbbb.dxrelation.dxfuncall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.util.hbbb.pub.HBPubItfService;
import nc.vo.gl.contrast.iufo.ContrastHBBBQryVO;
import nc.vo.gl.contrast.iufo.util.UCheckProxy;
import nc.vo.hbbb.contrast.ContrastQryVO;
import nc.vo.hbbb.contrast.IContrastConst;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;

import com.ufsoft.script.base.ICalcEnv;

public class CHECKBYKEYCallFunc implements IDxCallFunc {

	@Override
	public Object callFunc(String strFuncName, Object[] objParams, ICalcEnv env)throws BusinessException {
		if(null==objParams || objParams.length==0){
			return null;
		}
		String accountcode=HBProjectParamGetUtil.getProjectByParam(objParams[0]);
		int isself = 0;
		if(objParams[1] instanceof Integer)
			isself = (Integer)objParams[1];
		
		int dataType = 0;
		String strCondition = null;
		if(objParams.length == 4) {
			dataType = (Integer)objParams[2];
			strCondition = (String)objParams[3];
		}
		
		
		try {
			
			
			
			return getUCHECKBYKEY(accountcode, isself, dataType, strCondition,null, env);
		} catch (BusinessException e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
			throw e;
		}
	}
	
	private double getUCHECKBYKEY(String accountcode,int isself, int dataType, String strCond, String oppOrgPk, ICalcEnv env) throws BusinessException{
	    String	pk_self=(String) env.getExEnv(IContrastConst.PK_SELFCORP);
	    String	pk_opp=(String) env.getExEnv(IContrastConst.PK_OPPCORP);
	    
		ContrastQryVO qryvo= (ContrastQryVO) env.getExEnv(IContrastConst.CONTRASTQRYVO);
		ContrastHBBBQryVO ucheckContextvo= (ContrastHBBBQryVO) env.getExEnv(IContrastConst.UCHECK_CONTEXTVO);
		String key=null;
		if(dataType==0){
		ucheckContextvo.setPk_timecontrol_b(null);
		ucheckContextvo.setAss(strCond);
	    key = accountcode + (ucheckContextvo.getOccur() == null ? "" : ucheckContextvo.getOccur())
				+ (ucheckContextvo.getBalance() == null ? "" : ucheckContextvo.getBalance())
			 + (ucheckContextvo.getAss() == null ? "" : ucheckContextvo.getAss());
		}else{
	    ucheckContextvo.setAss(null);
		ucheckContextvo.setPk_timecontrol_b(strCond);
		 key = accountcode + (ucheckContextvo.getOccur() == null ? "" : ucheckContextvo.getOccur())
					+ (ucheckContextvo.getBalance() == null ? "" : ucheckContextvo.getBalance())
				 + (ucheckContextvo.getPk_timecontrol_b() == null ? "" : ucheckContextvo.getPk_timecontrol_b());
		}
		
		//qryvo.getUcheckResultMap(): <��ǰ��ѯ��������Ŀ+isself+�������+����������ǰ�ϲ���Χ�µ����ж��˶�map<self+opp,value>>
		//map: ��ǰ�ϲ���Χ�µ����ж��˶�map<self+opp,value>
		Map<String, UFDouble> map = qryvo.getUcheckResultMap().get(key);
		
		if (oppOrgPk == null) {//�Է���֯Ϊ�գ�����ԭ�����߼��������Է���ѯ
			if(map == null ){
				getBatchUCHECKBYKEY(qryvo,ucheckContextvo,accountcode,isself,dataType);
				map = qryvo.getUcheckResultMap().get(key);
			}
			if(map == null){
				return 0.0;
			}
			UFDouble ufDouble = map.get(pk_self + pk_opp);
			if(ufDouble == null){
				return 0.0;
			}else{
				return ufDouble.getDouble();
			}
		}else {//�Է���֯��Ϊ�գ������µ��߼����̶��Է���λΪ���������õģ�������λΪ��ǰ�ϲ���Χ�����еĵ�λ
			if (oppOrgPk.equals(pk_opp)) {
				if(map == null ){
					getBatchUCHECKBYORG(qryvo,ucheckContextvo,accountcode,isself,dataType,oppOrgPk);
					map = qryvo.getUcheckResultMap().get(key);
				}
				return getValueByOppOrg(map, oppOrgPk);
			}else {
				return 0.0;
			}
			
		}
	}
	
	/**
	 * ���ݶԷ���λ��ȡ���еĶ������ݵĻ���ֵ
	 * @param map
	 * @param oppOrgPk
	 * @return
	 */
	private double getValueByOppOrg(Map<String, UFDouble> map, String oppOrgPk) {
		if(map == null){
			return 0.0;
		}
		UFDouble ufDouble = UFDouble.ZERO_DBL;
		Set<String> pk_self2pk_oppSet = map.keySet();
		for (String pk_self2pk_opp : pk_self2pk_oppSet) {
			if (pk_self2pk_opp.substring(20).equals(oppOrgPk)) {
				ufDouble = ufDouble.add(map.get(pk_self2pk_opp));
			}
		}
		if(ufDouble==null){
			return 0.0;
		}else{
			return ufDouble.getDouble();
		}
		
	}
	
	/**
	 * ����ȡ�ڲ����׶�������
	 * @param qryvo
	 * @param ucheckContextvo
	 * @param accountcode
	 * @param isself
	 * @throws BusinessException
	 */
	private void getBatchUCHECKBYKEY(ContrastQryVO qryvo,ContrastHBBBQryVO ucheckContextvo,String accountcode,int isself,int dataType) throws BusinessException{
		//���ж��˵�λ
		String[] contrastorgs = qryvo.getContrastorgs();

		UCheckProxy proxy = new UCheckProxy(ucheckContextvo, accountcode, (isself==0 ? true : false), ucheckContextvo.isDriect(),contrastorgs);
	
		Map<String, Map<String,UFDouble>>  resultMap = new HashMap<String, Map<String,UFDouble>>();
		resultMap = qryvo.getUcheckResultMap();
		
		String key=null;
		if(dataType==0){
		    key = accountcode + (ucheckContextvo.getOccur() == null ? "" : ucheckContextvo.getOccur())
					+ (ucheckContextvo.getBalance() == null ? "" : ucheckContextvo.getBalance())
				 + (ucheckContextvo.getAss() == null ? "" : ucheckContextvo.getAss());
			}else{
			 key = accountcode + (ucheckContextvo.getOccur() == null ? "" : ucheckContextvo.getOccur())
						+ (ucheckContextvo.getBalance() == null ? "" : ucheckContextvo.getBalance())
					 + (ucheckContextvo.getPk_timecontrol_b() == null ? "" : ucheckContextvo.getPk_timecontrol_b());
			}
		if(resultMap.get(key)== null) {
			resultMap.put(key, proxy.getBatchExecValue());
		}
		qryvo.setUcheckResultMap(resultMap);
	}
	
	/**
	 * ����ȡ�ڲ����׶�������
	 * @param qryvo
	 * @param ucheckContextvo
	 * @param accountcode
	 * @param isself
	 * @throws BusinessException
	 */
	private void getBatchUCHECKBYORG(ContrastQryVO qryvo,ContrastHBBBQryVO ucheckContextvo,String accountcode,int isself,int dataType,String oppOrgPk) throws BusinessException{
		//���ж��˵�λ
		List<String> contrastorgs = qryvo.getUcheckbyorgcontrastorgs();
		
		//�ǵ��ȵ�ʱ��ʹ��ԭ���Ķ��˶�
		if (contrastorgs == null || contrastorgs.size()<=0) {
			contrastorgs = Arrays.asList(qryvo.getContrastorgs());
		}
		List<String> contrastOrgsList = new ArrayList<String>();
		
		// @edit by zhoushuang at 2016-3-6,����10:25:09 ֻ��Է���λΪ����������
		for (String selfopp : contrastorgs) {
			if (selfopp.substring(20, 40).equals(oppOrgPk)) {
				contrastOrgsList.add(selfopp);
			}
		}
		
		UCheckProxy proxy = new UCheckProxy(ucheckContextvo, accountcode, (isself==0 ? true : false), ucheckContextvo.isDriect(),contrastOrgsList.toArray(new String[0]));
	
		Map<String, Map<String,UFDouble>>  resultMap = new HashMap<String, Map<String,UFDouble>>();
		resultMap = qryvo.getUcheckResultMap();
		
		String key=null;
		if(dataType==0){
		    key = accountcode + (ucheckContextvo.getOccur() == null ? "" : ucheckContextvo.getOccur())
					+ (ucheckContextvo.getBalance() == null ? "" : ucheckContextvo.getBalance())
				 + (ucheckContextvo.getAss() == null ? "" : ucheckContextvo.getAss());
			}else{
			 key = accountcode + (ucheckContextvo.getOccur() == null ? "" : ucheckContextvo.getOccur())
						+ (ucheckContextvo.getBalance() == null ? "" : ucheckContextvo.getBalance())
					 + (ucheckContextvo.getPk_timecontrol_b() == null ? "" : ucheckContextvo.getPk_timecontrol_b());
			}
		if(resultMap.get(key)== null) {
			resultMap.put(key, proxy.getBatchExecValue());
		}
		qryvo.setUcheckResultMap(resultMap);
	}

	@Override
	public Object callFunc(String strFuncName, String strParam)throws BusinessException {
		return null;
	}

}
