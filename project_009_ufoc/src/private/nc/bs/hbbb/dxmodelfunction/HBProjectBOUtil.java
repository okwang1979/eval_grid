package nc.bs.hbbb.dxmodelfunction;

import java.util.Collection;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.jdbc.framework.SQLParameter;
import nc.pub.iufo.basedoc.OrgUtil;
import nc.util.hbbb.pub.HBPubItfService;
import nc.vo.hbbb.contrast.ContrastQryVO;
import nc.vo.hbbb.contrast.IContrastConst;
import nc.vo.hbbb.dxfunction.project.MeasureReportVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.pub.BusinessException;
import nc.vo.ufoc.unionproject.ProjectVO;

import com.ufsoft.script.base.ICalcEnv;

public class HBProjectBOUtil {
	
	//目前为内部交易类的函数只有intr,dpsum俩函数
	public static MeasureReportVO getProjectMeasVO(ICalcEnv env,String projectcode,boolean isintrade){
		MeasureReportVO result=null;
		String pk_hbScheme=(String) env.getExEnv(IContrastConst.PK_HBSCHEME);
		
		try {
			String pk_selforg = (String) env.getExEnv(IContrastConst.PK_SELFCORP);
			if(StringUtil.isEmptyWithTrim(pk_selforg)){
				ContrastQryVO qryvo = (ContrastQryVO) env.getExEnv(IContrastConst.CONTRASTQRYVO);
				pk_selforg = qryvo.getContrastorg();
			}
//			String pk_grouptest = getOrgUnitQryService().getOrg(pk_selforg).getPk_group();
			String pk_group = OrgUtil.getOrgInfo(pk_selforg).getPk_group();
			result=	HBPubItfService.getRemoteDxModelFunction().getMeasRepBySchemeProjectCode(pk_hbScheme, projectcode, pk_group,isintrade);
		} catch (BusinessException e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
		}
		return result;
	}
	
	
	@SuppressWarnings("unchecked")
	public static ProjectVO getProjectVOByCode(String projectcode) throws BusinessException {
		ProjectVO result = null;

		StringBuilder content = new StringBuilder();
		content.append(ProjectVO.CODE).append("=? ");
		BaseDAO dao = new BaseDAO();
		SQLParameter params = new SQLParameter();
		params.addParam(projectcode);
		Collection<ProjectVO> list = dao.retrieveByClause(ProjectVO.class, content.toString(), params);
		result = list.toArray(new ProjectVO[0])[0];

		return result;
	}
	
	/**
	 * @author zhaojian8
	 * @param pk_hbScheme
	 * @param pk_selforg
	 * @param projectcode
	 * @param isintrade
	 * @return
	 */
	public static MeasureReportVO getProjectMeasVOByCode(String pk_hbScheme,String pk_selforg,String projectcode,boolean isintrade){
		MeasureReportVO result=null;
		
		try {
			String pk_group = OrgUtil.getOrgInfo(pk_selforg).getPk_group();
			result=	HBPubItfService.getRemoteDxModelFunction().getMeasRepBySchemeProjectCode(pk_hbScheme, projectcode, pk_group,isintrade);
		} catch (BusinessException e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
		}
		return result;
	}
	
	/**
	 * 根据当前的合并项目得到其对应的成本法转权益内部调整项目
	 * 如果有，则查询其映射关系，没有返回空
	 * jiaah
	 * @param env
	 * @param projectcode
	 * @param isintrade
	 * @return
	 */
	public static MeasureReportVO getProjectMeasVOByPkProjAndSchemePK(String pk_hbscheme,String pk_project,boolean isintrade){
		MeasureReportVO result = null;
		try {
			BaseDAO dao = new BaseDAO();
			ProjectVO list = (ProjectVO) dao.retrieveByPK(ProjectVO.class, pk_project);
			if(list != null && list.getPk_costmeasure() != null){
				ProjectVO costvo = (ProjectVO) dao.retrieveByPK(ProjectVO.class, list.getPk_costmeasure());
				if(costvo != null){
					String pk_group = InvocationInfoProxy.getInstance().getGroupId();
					result = HBPubItfService.getRemoteDxModelFunction().getMeasRepBySchemeProjectCode(pk_hbscheme, costvo.getCode(), pk_group,isintrade);
				}
			}
		} catch (Exception e) {
			nc.bs.logging.Logger.error(e.getMessage(), e);
		}
		return result;
	}
}
