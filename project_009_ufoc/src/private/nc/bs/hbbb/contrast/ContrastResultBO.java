package nc.bs.hbbb.contrast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.itf.hbbb.vouch.constants.IVouchType;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.pub.hbbb.exception.UFOCUnThrowableException;
import nc.util.hbbb.HBAloneIDUtil;
import nc.util.hbbb.UfocLangLibUtil;
import nc.vo.hbbb.contrast.ContrastQryVO;
import nc.vo.hbbb.dxrelation.DXContrastVO;
import nc.vo.hbbb.meetaccount.MeetResultBodyVO;
import nc.vo.hbbb.meetaccount.MeetResultHeadVO;
import nc.vo.hbbb.vouch.VouchHeadVO;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;

public class ContrastResultBO {
	
	public ContrastResultBO(){
		super();
	}
	
	/**
	 * 按模板删除对账记录
	 * 删除in语句，没有用，还浪费时间
	 * @edit by zhoushuang at 2015-6-1,下午2:57:45
	 *
	 * @param vo
	 * @param selfOrgs
	 * @param oppOrgs
	 * @param qryvo
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<String, String> clearContrastedData(DXContrastVO vo, ContrastQryVO qryvo) throws BusinessException{
		StringBuilder content = new StringBuilder();
		BaseDAO dmo = new BaseDAO();
//		content.append(UFOCSqlUtil.buildInSql("pk_selforg", selfOrgs, true));
//		content.append(" and "+UFOCSqlUtil.buildInSql("pk_countorg", oppOrgs, true));
//		content.append(" pk_meetorg = ? ");
		content.append(" pk_hbscheme = ? ");
		content.append(" AND pk_dxrelation = ? ");
		content.append(" AND alone_id = ? ");
//		content.append("                  AND pk_keygroup = ? ");
		String headWhere = content.toString();
		content.append(" AND isnull(dataorigin,'~')<>'~' ");
		
		String disDataWhere = content.toString();
		
		SQLParameter params = new SQLParameter();
//		params.addParam(qryvo.getContrastorg());
		params.addParam(qryvo.getSchemevo().getPk_hbscheme());
		params.addParam(vo.getHeadvo().getPk_dxrela_head());
		String aloneid = HBAloneIDUtil.getAdjustVoucherAlone_id(qryvo, true);
		params.addParam(aloneid);
//		params.addParam(qryvo.getSchemevo().getPk_keygroup());
		
		//校验对账记录是否是分布式传过来的数据
		Collection disData = dmo.retrieveByClause(MeetResultHeadVO.class, disDataWhere, params);
		if(disData != null && disData.size()>0){
			throw new BusinessException(NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0051"));
		}
		
		content = new StringBuilder();
//		content.append("  pk_org = ? ");
//		content.append("                  AND pk_keygroup = ? ");
		content.append("  alone_id = ? ");
		content.append("  AND pk_hbscheme = ? ");
		content.append("  AND pk_dxrela = ? ");
		content.append("  AND dr = 0  ");
		content.append("  AND (checker <>'~' ");
		content.append("  or isnull(dataorigin,'~')<>'~' )");//已审核的分录或者数据来源有值的情况，都不能重新执行对账
		
		content.append("  AND vouch_type =  ").append(IVouchType.TYPE_AUTO_ENTRY);
		
		SQLParameter vouchParams = new SQLParameter();
//		vouchParams.addParam(qryvo.getContrastorg());
//		vouchParams.addParam(qryvo.getSchemevo().getPk_keygroup());
		vouchParams.addParam(HBAloneIDUtil.getAdjustVoucherAlone_id(qryvo, true));
		vouchParams.addParam(qryvo.getSchemevo().getPk_hbscheme());
		vouchParams.addParam(vo.getHeadvo().getPk_dxrela_head());

		// 查询自动生成的抵销分录是否已经存在审核，是否是来自于分布式的数据
		Collection retrieveByClause = dmo.retrieveByClause(VouchHeadVO.class, content.toString(), vouchParams);
		if (retrieveByClause != null && retrieveByClause.size() > 0) {
			VouchHeadVO headvo  = (VouchHeadVO)retrieveByClause.toArray(new VouchHeadVO[0])[0];
			if(headvo.getDataorigin() != null){
				throw new UFOCUnThrowableException(NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0051"));
			}
			else{
				throw new UFOCUnThrowableException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830003-0021")/* @当前执行条件下,抵消模板为 */+ "'" + UfocLangLibUtil.toCurrentLang(vo.getHeadvo()) + "'"
						+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830003-0022")/* @已生成凭证且已审核! 请取消审核再执行! */);
			}
		}
		
		content = new StringBuilder();

		content.append(" exists ( ");
		content.append("        SELECT pk_totalinfo ");
		content.append("             FROM iufo_meetdata_head ");
//		content.append(" where " + UFOCSqlUtil.buildInSql("pk_selforg", selfOrgs));
//		content.append(" and "+UFOCSqlUtil.buildInSql("pk_countorg", oppOrgs));
		content.append("        where pk_totalinfo = iufo_meetdata_body.details");
//		content.append("              AND pk_meetorg = ? ");
		content.append("              AND pk_hbscheme = ? ");
		content.append("              AND pk_dxrelation = ? ");
		content.append("              AND alone_id = ? ");
//		content.append("                  AND pk_keygroup = ? ");
		content.append("             ) ");
		
		String bodywhere = content.toString();
		
		//记录所有有对账说明的记录
		String sql = " select iufo_meetdata_head.pk_selforg,pk_countorg,alone_id,pk_hbscheme,pk_dxrelation,pk_measure,meetnote from iufo_meetdata_head,iufo_meetdata_body " +
				"where details = pk_totalinfo and pk_hbscheme =?  AND pk_dxrelation = ? AND alone_id = ? and meetnote != '~'";
		
		ArrayList bodyLst = new ArrayList();
		bodyLst = (ArrayList) dmo.executeQuery(sql, params, new ArrayListProcessor());
		Map<String, String> map = new HashMap<String, String>();
		if(bodyLst.size() > 0){
			for(int i = 0 ;i < bodyLst.size(); i++){
				Object[] objs = (Object[])bodyLst.get(i);
				StringBuffer connectPK = new StringBuffer();
				for(int j = 0 ; j < objs.length -1 ; j++){
					connectPK.append(objs[j].toString());
				}
				map.put(connectPK.toString(), objs[6].toString());
			}
		}
	    
		dmo.deleteByClause(MeetResultBodyVO.class, bodywhere, params);
		dmo.deleteByClause(MeetResultHeadVO.class, headWhere, params);
		
		return map;
	}
	
	
//	//此方法没有被References
//	@SuppressWarnings("unchecked")
//	public static void clearContrastedData(DXContrastVO vo,String pk_self,String pk_opp,ContrastQryVO qryvo) throws BusinessException{
//		StringBuilder content=new StringBuilder();
//		content.append("               pk_org = ? ");
//		content.append("                  AND pk_keygroup = ? ");
//		content.append("                  AND alone_id = ? ");
//		content.append("                  AND pk_hbscheme = ? ");
//		content.append("                  AND pk_dxrela = ? ");
//		content.append("                  AND checker <>'~' ");
//
//		content.append("   AND vouch_type =  ").append(IVouchType.TYPE_AUTO_ENTRY);
//
//		SQLParameter params = new SQLParameter();
//		params.addParam(qryvo.getContrastorg());
//		params.addParam(qryvo.getSchemevo().getPk_keygroup());
//		params.addParam(HBAloneIDUtil.getAdjustVoucherAlone_id(qryvo, true));
//		params.addParam(qryvo.getSchemevo().getPk_hbscheme());
//		params.addParam(vo.getHeadvo().getPk_dxrela_head());
//
//		BaseDAO dmo = new BaseDAO();
//		// 查询凭证是否已经存在审核
//		Collection<VouchHeadVO> retrieveByClause = dmo.retrieveByClause(VouchHeadVO.class, content.toString(), params);
//		if (retrieveByClause != null && retrieveByClause.size() > 0) {
//			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830003-0021")/* @当前执行条件下,抵消模板为 */+ "'" + UfocLangLibUtil.toCurrentLang(vo.getHeadvo()) + "'"
//					+ nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830003-0022")/* @已生成凭证且已审核! 请取消审核再执行! */);
//		}
//		content=new StringBuilder();
//
//		content.append("               pk_selforg = ? ");
//		content.append("                  AND pk_countorg = ? ");
//		content.append("                  AND pk_meetorg = ? ");
//		content.append("                  AND pk_hbscheme = ? ");
//		content.append("                  AND pk_dxrelation = ? ");
//		content.append("                  AND alone_id = ? ");
//		content.append("                  AND pk_keygroup = ? ");
//		String headWhere=content.toString();
//		content=new StringBuilder();
//
//		
//		content.append("      details IN ( ");
//		content.append("               SELECT pk_totalinfo ");
//		content.append("                 FROM iufo_meetdata_head ");
//		content.append("                WHERE pk_selforg = ? ");
//		content.append("                  AND pk_countorg = ? ");
//		content.append("                  AND pk_meetorg = ? ");
//		content.append("                  AND pk_hbscheme = ? ");
//		content.append("                  AND pk_dxrelation = ? ");
//		content.append("                  AND alone_id = ? ");
//		content.append("                  AND pk_keygroup = ? ");
//		content.append("             ) ");
//		
////		content.append("     or  pk_meetresult IN ( ");
////		content.append("               SELECT pk_totalinfo ");
////		content.append("                 FROM iufo_meetdata_head ");
////		content.append("                WHERE pk_selforg = ? ");
////		content.append("                  AND pk_countorg = ? ");
////		content.append("                  AND pk_meetorg = ? ");
////		content.append("                  AND pk_hbscheme = ? ");
////		content.append("                  AND pk_dxrelation = ? ");
////		content.append("                  AND alone_id = ? ");
////		content.append("                  AND pk_keygroup = ? ");
////		content.append("             ) ");
//		String bodywhere=content.toString();
//		params=new SQLParameter();
//		params.addParam(pk_self);
//		params.addParam(pk_opp);
//		params.addParam(qryvo.getContrastorg());
//		params.addParam(qryvo.getSchemevo().getPk_hbscheme());
//		params.addParam(vo.getHeadvo().getPk_dxrela_head());
//		String aloneid=HBAloneIDUtil.getAdjustVoucherAlone_id(qryvo, true);
//		params.addParam(aloneid);
//		params.addParam(qryvo.getSchemevo().getPk_keygroup());
//		
//		StringBuilder sbHead=new StringBuilder();
//		sbHead.append(" ").append(MeetdataVO.PK_SELF).append("= ? ");
//		sbHead.append(" and ").append(MeetdataVO.PK_OPP).append("=? and ");
//		sbHead.append(MeetdataVO.PK_CONTRASTORG).append("=?  and ");
//		sbHead.append(MeetdataVO.PK_SCHEME).append("=? and ");
//		sbHead.append(MeetdataVO.PK_DXRELA).append("=? and ");
//		sbHead.append(MeetdataVO.ALONEID).append("=? and ");
//		sbHead.append(MeetdataVO.PK_KEYGROUP).append("=? ");
//		
//		StringBuilder sbBody=new StringBuilder();
////		sbBody.append("  ").append(MeetdatasubVO.PK_MEETDATA).append(" in ( select ");
////		sbBody.append(MeetdataVO.PK_MEETDATA).append("  from ").append(MeetdataVO.getDefaultTableName());
////		sbBody.append(" where ").append(sbHead.toString()).append(" )" );
//		
//		sbBody.append("  subvos  ").append(" in ( select ");
//		sbBody.append(MeetdataVO.PK_MEETDATA).append("  from ").append(MeetdataVO.getDefaultTableName());
//		sbBody.append(" where ").append(sbHead.toString()).append(" )" );
//		
//		
//		BaseDAO dao=new BaseDAO();
//		dao.deleteByClause(MeetdatasubVO.class,sbBody.toString(), params);
//		dao.deleteByClause(MeetdataVO.class, sbHead.toString(), params);
//		dao.deleteByClause(MeetResultBodyVO.class,bodywhere, params);
//		dao.deleteByClause(MeetResultHeadVO.class, headWhere, params);
//	}
	
	
}
