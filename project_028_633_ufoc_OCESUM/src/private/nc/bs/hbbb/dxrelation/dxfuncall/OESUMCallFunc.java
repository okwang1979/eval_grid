package nc.bs.hbbb.dxrelation.dxfuncall;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.hbbb.dxmodelfunction.HBProjectBOUtil;
import nc.bs.logging.Logger;
import nc.itf.hbbb.contrast.IntrMeasProjectCache;
import nc.itf.hbbb.vouch.constants.IVouchType;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.BaseProcessor;
import nc.pub.iufo.basedoc.OrgUtil;
import nc.util.hbbb.HBAloneIDUtil;
import nc.util.hbbb.OffsetHanlder;
import nc.util.hbbb.dxrelation.formula.DXFmlEditConst;
import nc.util.hbbb.hbscheme.HBSchemeSrvUtils;
import nc.util.hbbb.pub.HBPubItfService;
import nc.vo.hbbb.contrast.ContrastQryVO;
import nc.vo.hbbb.contrast.IContrastConst;
import nc.vo.hbbb.dxfunction.project.MeasureReportVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.vouch.VouchBodyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iufo.verctrl.IDataVersionConsts;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDouble;

import com.ufsoft.script.base.ICalcEnv;

public class OESUMCallFunc implements   IDxCallFunc {

	@Override
	public Object callFunc(String strFuncName, Object[] objParams, ICalcEnv env)throws BusinessException {
		if(null==objParams || objParams.length==0  ){
			return null;
		}
		
		
		HBSchemeVO schemevo = HBSchemeSrvUtils.getHBSchemeByCode(String.valueOf(objParams[0]));
		
		
		int voucherType = Integer.valueOf(String.valueOf( objParams[1]));
		
		
		String projectcode=HBProjectParamGetUtil.getProjectByParam(objParams[2]);//项目
		int cur_direction = 0;
		if(objParams[3] instanceof Integer)
			cur_direction = (Integer)objParams[3];//借贷方向
		
		int offset=0;//偏移量
		if(null!=objParams[4]){
			offset = new UFDouble(String.valueOf(objParams[4])).intValue();
		}
		
		try {
			return getCESUM(projectcode, cur_direction,offset, env,schemevo,voucherType);
		} catch (BusinessException e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
			throw e;
		}
	}
	
	
	
	
	public double getCESUM(String projectcode, int cur_direction,int offset, ICalcEnv env, HBSchemeVO schemevo, int voucherType) throws BusinessException {
		//环境变量
		String pk_hbScheme =schemevo.getPk_hbscheme();
		ContrastQryVO qryvo = (ContrastQryVO) env.getExEnv(IContrastConst.CONTRASTQRYVO);
		
		//预加载的映射关系
		Map<String, MeasureReportVO> prjoectMeasMapCache = new HashMap<String, MeasureReportVO>();
		IntrMeasProjectCache cacheinstance = qryvo.getIntrMeaProjectinstance();
		if(cacheinstance != null){
			prjoectMeasMapCache= cacheinstance.getMeasRepVOs();
		}

		String key = pk_hbScheme + projectcode;
		//合并项目映射关系
		MeasureReportVO measrepvo = prjoectMeasMapCache.get(key);
		if(measrepvo == null){
			
			
			
		 
		 
			
			try {
				String pk_selforg = (String) env.getExEnv(IContrastConst.PK_SELFCORP);
				if(StringUtil.isEmptyWithTrim(pk_selforg)){
				 
					pk_selforg = qryvo.getContrastorg();
				}
//				String pk_grouptest = getOrgUnitQryService().getOrg(pk_selforg).getPk_group();
				String pk_group = OrgUtil.getOrgInfo(pk_selforg).getPk_group();
				measrepvo=	HBPubItfService.getRemoteDxModelFunction().getMeasRepBySchemeProjectCode(pk_hbScheme, projectcode, pk_group,false);
				if(measrepvo==null){
					measrepvo=	HBPubItfService.getRemoteDxModelFunction().getMeasRepBySchemeProjectCode(pk_hbScheme, projectcode, pk_group,true);
				}
			} catch (BusinessException e) {
				nc.bs.logging.Logger.error(e.getMessage(), e);
			}
			 
			
			
		//	measrepvo = HBProjectBOUtil.getProjectMeasVO(env, projectcode, true);
			if (null != measrepvo && null != cacheinstance) {
				cacheinstance.insertMearepVO(key, measrepvo);
			}
			else{
				//映射关系不存在则抛出异常
				throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0106")/*@res "CESUM函数"*/ + projectcode + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830002-0107")/*@res "未正确映射!"*/);
			}
		}
		
		MeasureVO measVO = measrepvo.getMeasVO();
		return getResultTest(cur_direction,qryvo,offset,projectcode,measVO,schemevo,voucherType);
	}

	@SuppressWarnings("serial")
	private double getResultTest(int cur_direction,ContrastQryVO qryvo,int offset,String projectcode,MeasureVO measVO, HBSchemeVO schemevo, int voucherType){
//		String pk_group = ((IOrgUnitQryService) NCLocator.getInstance().lookup(IOrgUnitQryService.class.getName())).getOrg((String) env.getExEnv(IContrastConst.PK_SELFCORP)).getPk_group();
		double result = 0.0;
		String pk_group = InvocationInfoProxy.getInstance().getGroupId();
		StringBuilder projectcondition=new StringBuilder();
		projectcondition.append(VouchBodyVO.PK_MEASURE).append(" in (");
		projectcondition.append("select pk_project from ufoc_project where code=? and ( pk_group=?   or pk_org = 'GLOBLE00000000000000') and  isnull(dr,0)=0 ) ");
		
		BaseDAO dao = new BaseDAO();
		StringBuilder content = new StringBuilder();
		try {
			if( cur_direction == DXFmlEditConst.CREDIT_DIRECTION){
				content.append("SELECT sum( iufo_vouch_body.debitamount) ");
			}else{
				content.append("SELECT sum(iufo_vouch_body.creditamount) ");
			}
			content.append("  FROM iufo_vouch_body INNER JOIN iufo_vouch_head ");
			content.append("       ON iufo_vouch_body.pk_vouchhead = iufo_vouch_head.pk_vouchhead ");
			content.append(" WHERE ( iufo_vouch_head.alone_id = ?  )");
			content.append("   AND iufo_vouch_head.pk_hbscheme = ? ");
			content.append("   AND iufo_vouch_body.");
			content.append(projectcondition);
			if(voucherType==1){
				content.append("   AND iufo_vouch_head.vouch_type = ?  ");
			}if(voucherType==2){
				content.append("   AND iufo_vouch_head.vouch_type = ?  ");
			}else{
				content.append("   AND (iufo_vouch_head.vouch_type = ? OR iufo_vouch_head.vouch_type = ?) ");
			}
			content.append("  and iufo_vouch_body.dr = 0 and iufo_vouch_head.dr = 0  " );
			content.append("   group by iufo_vouch_head.alone_id ");
			
			SQLParameter param = new SQLParameter();
			String contrastorg = qryvo.getContrastorg();
			//处理时间偏移量
			Map<String, String> handOffset = OffsetHanlder.handOffset(schemevo,qryvo.getKeymap(), offset);
			String salone_id=HBAloneIDUtil.findAloneID(contrastorg, handOffset,schemevo.getPk_keygroup(),IDataVersionConsts.VER_VOUCHER);
			param.addParam(salone_id);
			param.addParam(schemevo.getPk_hbscheme());
			param.addParam(projectcode);
			param.addParam(pk_group);
			if(voucherType==1){
				param.addParam(IVouchType.TYPE_AUTO_ENTRY);
			}if(voucherType==2){
				param.addParam(IVouchType.TYPE_MANU_ENTRY);
			}else{
				param.addParam(IVouchType.TYPE_AUTO_ENTRY);
				param.addParam(IVouchType.TYPE_MANU_ENTRY);
			}
//			param.addParam(IVouchType.TYPE_AUTO_ENTRY);
//			param.addParam(IVouchType.TYPE_MANU_ENTRY);

			result =(Double) dao.executeQuery(content.toString(), param,  new BaseProcessor() {

				public Object processResultSet(ResultSet rs) throws SQLException {
					double result=0;
					if (rs.next()){
						result=rs.getDouble(1);
					}
				    return result;
				}
			});
			return result;
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			return 0;
		}
		
	}
	

	@Override
	public Object callFunc(String strFuncName, String strParam)throws BusinessException {
		return null;
	} 

}
