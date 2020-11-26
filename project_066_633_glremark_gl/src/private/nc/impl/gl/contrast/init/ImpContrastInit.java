package nc.impl.gl.contrast.init;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.bd.baseservice.ManageTypeBaseService;
import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.gl.contrast.init.ContrastInitBO;
import nc.bs.logging.Logger;
import nc.bs.mw.sqltrans.TempTable;
import nc.bs.uap.lock.PKLock;
import nc.gl.utils.GLAdjustVODataUtil;
import nc.gl.utils.GLMultiLangUtil;
import nc.gl.utils.GLSqlUtil;
import nc.itf.gl.contrast.init.IContrastInit;
import nc.itf.gl.contrast.report.IContrastReport;
import nc.itf.gl.contrast.rule.IContrastRule;
import nc.jdbc.framework.ConnectionFactory;
import nc.jdbc.framework.JdbcPersistenceManager;
import nc.jdbc.framework.JdbcSession;
import nc.jdbc.framework.PersistenceManager;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.exception.DbException;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.md.data.access.NCObject;
import nc.md.gl.metaData.GlAccAssinfoVO;
import nc.md.persist.framework.MDPersistenceService;
import nc.pubitf.accperiod.AccountCalendar;
import nc.pubitf.uapbd.IAccountPubService;
import nc.vo.bd.accessor.IBDData;
import nc.vo.bd.account.AccAssVO;
import nc.vo.bd.account.AccountVO;
import nc.vo.bd.period.AccperiodVO;
import nc.vo.bd.period2.AccperiodmonthVO;
import nc.vo.fip.pub.SqlTools;
import nc.vo.fipub.freevalue.FiPubFreeValueProxy;
import nc.vo.fipub.freevalue.Module;
import nc.vo.fipub.freevalue.util.MD5;
import nc.vo.gateway60.accountbook.AccountBookUtil;
import nc.vo.gateway60.itfs.AccountUtilGL;
import nc.vo.gl.contrast.data.ContrastDataRemarkVO;
import nc.vo.gl.contrast.data.ContrastDataVO;
import nc.vo.gl.contrast.init.ContrastInitBalanceConditionVO;
import nc.vo.gl.contrast.init.ContrastInitSubVO;
import nc.vo.gl.contrast.init.ContrastInitVO;
import nc.vo.gl.contrast.report.ContrastReportVO;
import nc.vo.gl.contrast.report.statusconst.ContrastReportStatusConst;
import nc.vo.gl.contrast.rule.ContrastRuleAreaVO;
import nc.vo.gl.contrast.rule.ContrastRuleSubjVO;
import nc.vo.gl.contrast.rule.ContrastRuleVO;
import nc.vo.gl.contrast.rule.TransferNextYearInfoVO;
import nc.vo.glcom.account.Balanorient;
import nc.vo.glcom.exception.GLBusinessException;
import nc.vo.glcom.tools.GLPubProxy;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.org.AccountingBookVO;
import nc.vo.org.SetOfBookVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.util.BDVersionValidationUtil;

import org.apache.commons.lang.StringUtils;

public class ImpContrastInit  extends ManageTypeBaseService<ContrastInitVO> implements IContrastInit {
	
	private String FX = "FX_~";

	public ImpContrastInit() {
		super("c5534a09-a713-4c0b-bcd4-4db0a4d448e1", ContrastInitVO.class, null);
	}
	//借   期初设计有问题，在期初元数据中设置枚举，下版重构
	private String DEBIT = "Y";
	//贷
	private String CREDIT = "N";

	@Override
	public boolean delete(String pk_contrastinit) throws BusinessException {
		//删除同步gl_contrastdata数据,应该在执行过对账不允许删除
//		synChronizeInitDataByInitPK(pk_contrastinit);

		if(isContrastedInit(pk_contrastinit)) {
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("private20111017_0","02002001-0007")/*@res "已经对帐，不允许删除！"*/);
		}

		deleteSynData(pk_contrastinit);
		ContrastInitVO contrastinitvo = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(ContrastInitVO.class, pk_contrastinit, false);
		super.deleteVO(contrastinitvo);
//		return new ContrastInitBO().delete(pk_contrastinit);
		return true;

	}
	public boolean isContrastedInit(String pk_contrastinit)
			throws BusinessException {
		if (StringUtil.isEmpty(pk_contrastinit)) {
			return true;
		}

		StringBuffer sqlBuff = new StringBuffer();
		
		sqlBuff.append("SELECT  *  FROM gl_contrastdata cdata where ");

		sqlBuff.append("  EXISTS (SELECT 1 FROM GL_CONTRASTINITSUB WHERE ");
		sqlBuff.append(ContrastInitSubVO.PK_CONTRASTINIT + "='"
				+ pk_contrastinit + "'");
		sqlBuff.append(" and " + ContrastInitSubVO.PK_CONTRASTINITSUB + "= cdata."
				+ ContrastDataVO.PK_DETAIL);
		sqlBuff.append(" ) ");
		sqlBuff.append(" and (cdata." + ContrastDataVO.AMOUNTEQUAL + "='"
				+ UFBoolean.TRUE + "' or cdata." + ContrastDataVO.QUANTITYEQUAL
				+ "='" + UFBoolean.TRUE + "')");
		sqlBuff.append(" and exists (select 1 from GL_CORPCONTRAST where GL_CORPCONTRAST.PK_CORPCONTRAST=cdata.PK_CORPCONTRAST) ");

		Object executeQuery = new BaseDAO().executeQuery(sqlBuff.toString(), new ArrayListProcessor());
		
		if(executeQuery != null) {
			List list = (List) executeQuery;
			if(list.size()>0) {
				return true;
			}
		}
		return false;
	}


	@Override
	public ContrastInitVO insert(ContrastInitVO vo,boolean isResetPeriod) throws BusinessException {
		
		return manageInitVo(vo,isResetPeriod,false);
	}
	
	private ContrastInitVO manageInitVo(ContrastInitVO vo,boolean isResetPeriod,boolean isYearInit) throws BusinessException {
//         UFDateTime startdate = vo.getStartdate();
//		
//		boolean reportBuilded = NCLocator.getInstance().lookup(IContrastReport.class).isReportBuilded(vo.getPk_accountingbook(), vo.getPk_contrastrule(), vo.getSyear(), null, true);
//		
//		if(reportBuilded) {
//			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("private20111017_0","02002001-0206")/*@res "所选核算账簿与对账规则已有正式报告生成，不允许新增期初数据！"*/);
//		}
		
		//设置默认值(年度期间),以及子表冗余字段,合计信息
		setDefaultInfo(vo,isResetPeriod);
		//核算账簿+科目+客商主键+凭证号+凭证类型+凭证日期+币种+方向   共8项：生成contrastinitsubvo的主键
		/**********************这里通过assid判断是不是同一个辅助核算！如果业务上需要根据整个辅助核算组合判断，则应该修改下面生成Pk_contrastinitsub的逻辑*************************/
		if(isResetPeriod){//注：该方法目前只有“新增”和“结转下年”用到。"新增"时isResetPeriod参数为true。这时应该设置子表的主键，需要生成MD5
			ContrastInitSubVO[] tempSub = vo.getContrastinitsub();
			for (int i = 0; i < tempSub.length; i++) {
				StringBuilder srcpk = new StringBuilder();
				srcpk.append(vo.getPk_accountingbook());
				srcpk.append(vo.getPk_accasoa());
//			String pk_customer = (String) new BaseDAO().executeQuery("select f4 from gl_docfree1 where assid = '" + tempSub[i].getPk_customer() + "'", new ColumnProcessor());
//			srcpk.append(pk_customer);  //子表的“客商主键”便是assid.这里通过客商主键判断
				srcpk.append(tempSub[i].getPk_customer()); 
				srcpk.append(tempSub[i].getVoucherno());
				srcpk.append(tempSub[i].getPk_vouchertype());
				srcpk.append(tempSub[i].getVoucherdate().toStdString());
				srcpk.append(tempSub[i].getPk_currtype());
				srcpk.append(tempSub[i].getDirection().equals("Y")?1:-1);
//			srcpk.append(tempSub[i].getYear());//因为有结转下年，所以增加年来做区分
				String md5 = new MD5().getMD5ofStr(srcpk.toString());
				tempSub[i].setPk_contrastinitsub(md5.substring(6, 26)); //取md5的中间20位作为子表的主键
				tempSub[i].setPk_detail(md5.substring(6, 26));
			}
		}
//		else{//结转下年时，这里不能这样写，因为没有查出主键。。。
//			ContrastInitSubVO[] tempSub = vo.getContrastinitsub();
//			for (int i = 0; i < tempSub.length; i++) {
//				tempSub[i].setPk_detail(tempSub[i].getPk_contrastinitsub()); //将子表主键设置到子表的pk_detail列(只有结转下年才会用到pk_detail列)
//			}
//		}
		ContrastInitVO resultVO = super.insertVO(vo);
		//同步gl_contrastdata数据
		synChronizeInitDataByInitPK(resultVO.getPk_contrastinit(),isYearInit,resultVO.getPk_group());
		
		 ContrastInitVO rtInitVo = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(ContrastInitVO.class, vo.getPk_contrastinit(), false);
		 
		 GLAdjustVODataUtil.adjustZeroToNull(rtInitVo);
		GLAdjustVODataUtil.adjustZeroToNull(rtInitVo.getContrastinitsub());
		 
		 return rtInitVo;
		 
	}


	/**
	 *
	 * 设置默认值(年度期间),以及子表冗余字段,合计信息
	 * <p>修改记录：</p>
	 * @param vo
	 * @throws GLBusinessException
	 * @see
	 * @since V6.0
	 */
	protected void setDefaultInfo(ContrastInitVO vo,boolean isResetPeriod) throws GLBusinessException {
		ContrastInitSubVO[] contrastinitsub = vo.getContrastinitsub();
//		nc.vo.glcom.glperiod.GlPeriodVO periodvo=null;
//		GlPeriodBO bo=new GlPeriodBO();
		if (isResetPeriod) {
//			periodvo = bo.getPeriod(vo.getPk_accountingbook(), vo
//					.getStartdate().getDate());
//			vo.setSyear(periodvo.getYear());
			vo.setSperiod(IContrastInit.STRATPERIOD);
		}
		
		UFDouble quantity = UFDouble.ZERO_DBL;//数量合计
		UFDouble oriamount = UFDouble.ZERO_DBL;//原币合计
		UFDouble orgamount = UFDouble.ZERO_DBL;//组织本币合计
		UFDouble groupamount = UFDouble.ZERO_DBL;//集团本币合计
		UFDouble globalamount = UFDouble.ZERO_DBL;//全局本币合计
		if(contrastinitsub!=null){
			for (int i = 0; i < contrastinitsub.length; i++) {
				if(vo.getIscontrasted().booleanValue()){
					//已对付数据,则子表的期间根据凭证日期匹配
//					if (isResetPeriod) {
//						periodvo = bo.getPeriod(vo.getPk_accountingbook(),
//								contrastinitsub[i].getVoucherdate());
						contrastinitsub[i].setYear(vo.getSyear());
						contrastinitsub[i].setPeriod(vo.getSperiod());
//					}
					contrastinitsub[i].setIscontrasted(UFBoolean.TRUE);
				}else{
					//如果未对付数据,则子表的期间为主表的期间
//					if (isResetPeriod) {
						contrastinitsub[i].setYear(vo.getSyear());
						contrastinitsub[i].setPeriod(vo.getSperiod());
//					}
					contrastinitsub[i].setIscontrasted(UFBoolean.FALSE);
				}
				String direction = contrastinitsub[i].getDirection();
				//借方
				if(direction == null || DEBIT.equals(direction)) {
					quantity = quantity.add(contrastinitsub[i].getQuantity()== null ? UFDouble.ZERO_DBL:contrastinitsub[i].getQuantity());
					oriamount = oriamount.add(contrastinitsub[i].getAmount()== null ? UFDouble.ZERO_DBL:contrastinitsub[i].getAmount());
					orgamount = orgamount.add(contrastinitsub[i].getLocamount()== null ? UFDouble.ZERO_DBL:contrastinitsub[i].getLocamount());
					groupamount = groupamount.add(contrastinitsub[i].getGroupamount()== null ? UFDouble.ZERO_DBL:contrastinitsub[i].getGroupamount());
					globalamount = globalamount.add(contrastinitsub[i].getGlobalamount()== null ? UFDouble.ZERO_DBL:contrastinitsub[i].getGlobalamount());
				}else {//贷方
					quantity = quantity.sub(contrastinitsub[i].getQuantity()== null ? UFDouble.ZERO_DBL:contrastinitsub[i].getQuantity());
					oriamount = oriamount.sub(contrastinitsub[i].getAmount()== null ? UFDouble.ZERO_DBL:contrastinitsub[i].getAmount());
					orgamount = orgamount.sub(contrastinitsub[i].getLocamount()== null ? UFDouble.ZERO_DBL:contrastinitsub[i].getLocamount());
					groupamount = groupamount.sub(contrastinitsub[i].getGroupamount()== null ? UFDouble.ZERO_DBL:contrastinitsub[i].getGroupamount());
					globalamount = globalamount.sub(contrastinitsub[i].getGlobalamount()== null ? UFDouble.ZERO_DBL:contrastinitsub[i].getGlobalamount());
				}
				contrastinitsub[i].setPk_contrastrule(vo.getPk_contrastrule());
				contrastinitsub[i].setPk_accasoa(vo.getPk_accasoa());
				contrastinitsub[i].setPk_accountingbook(vo.getPk_accountingbook());
				contrastinitsub[i].setPk_group(vo.getPk_group());
			}
		}
		String pk_accasoa = vo.getPk_accasoa();
		UFDateTime startdate = vo.getStartdate();
		AccountVO accountVo = null;
		try {
			AccountVO[] queryAccountVosByPks = AccountUtilGL.queryAccountVosByPks(new String[]{pk_accasoa},startdate.toStdString());
			if(queryAccountVosByPks != null && queryAccountVosByPks.length>0) {
				accountVo = queryAccountVosByPks[0];
			}
		} catch (BusinessException e1) {
			Logger.error(e1.getMessage(), e1);
		}
		if(accountVo != null && Balanorient.CREDIT==accountVo.getBalanorient().intValue()) {//贷方
			vo.setQuantity(UFDouble.ZERO_DBL.sub(quantity));
			vo.setAmount(UFDouble.ZERO_DBL.sub(oriamount));
			vo.setLocamount(UFDouble.ZERO_DBL.sub(orgamount));
			vo.setGroupamount(UFDouble.ZERO_DBL.sub(groupamount));
			vo.setGlobalamount(UFDouble.ZERO_DBL.sub(globalamount));
		}else {
			vo.setQuantity(quantity);
			vo.setAmount(oriamount);
			vo.setLocamount(orgamount);
			vo.setGroupamount(groupamount);
			vo.setGlobalamount(globalamount);
		}
		
		String pk_contrastrule = vo.getPk_contrastrule();
		ContrastRuleVO ruleVo = null;
		try {
			ruleVo = NCLocator.getInstance().lookup(IContrastRule.class).findByPrimaryKey(pk_contrastrule);
		} catch (BusinessException e) {
			Logger.error(e.getMessage(), e);
		}
		
		if(ruleVo != null) {
			String contrastcontent = ruleVo.getContrastcontent();
			char[] charArray = contrastcontent.toCharArray();
			if(charArray[0] == "N".toCharArray()[0]) {
				vo.setQuantity(UFDouble.ZERO_DBL);
			}
			if(charArray[1] == "N".toCharArray()[0]) {
				vo.setAmount(UFDouble.ZERO_DBL);
			}
			if(charArray[2] == "N".toCharArray()[0]) {
				vo.setLocamount(UFDouble.ZERO_DBL);
			}
			if(charArray[3] == "N".toCharArray()[0]) {
				vo.setGroupamount(UFDouble.ZERO_DBL);
			}
			if(charArray[4] == "N".toCharArray()[0]) {
				vo.setGlobalamount(UFDouble.ZERO_DBL);
			}
		}
		
	}

	@Override
	public ContrastInitVO[] query(Object[] paramvalues, int[] paramtypes)
			throws BusinessException {
		return null;
	}

	@Override
	public ContrastInitVO[] query(String whereSql) throws BusinessException {
		ArrayList<ContrastInitVO>  list = new ArrayList<ContrastInitVO>();

		NCObject[] queryBillOfNCObjectByCond = MDPersistenceService.lookupPersistenceQueryService().queryBillOfNCObjectByCond(ContrastInitVO.class, whereSql, false);
		if(queryBillOfNCObjectByCond!=null && queryBillOfNCObjectByCond.length>0){
			for (int i = 0; i < queryBillOfNCObjectByCond.length; i++) {
				NCObject ncObject = queryBillOfNCObjectByCond[i];
				Object containmentObject = ncObject.getContainmentObject();
				ContrastInitVO initVo = (ContrastInitVO)containmentObject;
				GLAdjustVODataUtil.adjustZeroToNull(initVo);
				ContrastInitSubVO[] contrastinitsub = initVo.getContrastinitsub();
				GLAdjustVODataUtil.adjustZeroToNull(contrastinitsub);
				list.add((ContrastInitVO)containmentObject);
			}
		}
		return list.toArray(new ContrastInitVO[0]);
//		return new ContrastInitBO().query(whereSql);
	}

	@Override
	public ContrastInitVO update(ContrastInitVO vo) throws BusinessException {
		if(vo==null) throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("contrastprivate_0","02002002-0014")/*@res "ContrastInitVO不能为空!"*/);
		//设置冗余信息
		setDefaultInfo(vo,false);

		List<ContrastInitSubVO> updataAndInsertsubList = new ArrayList<ContrastInitSubVO>();//保存需要“更新||新增”的子表数据(需要重新新增)
		List<ContrastInitSubVO> updateAndDeletesubList = new ArrayList<ContrastInitSubVO>();//保存需要“更新||删除”的子表数据(需要全部删除)
		
		Set<String> updateSubPkSet = new HashSet<String>();
		ContrastInitSubVO[] contrastinitsubvos = vo.getContrastinitsub();//保存了变更的所有的子表数据
		//1.因为在后面明细对帐时，直接更新子表的字段，没有更新主表，需要校验子表版本
		if(contrastinitsubvos != null && contrastinitsubvos.length>0) {
			for (ContrastInitSubVO contrastInitSubVO : contrastinitsubvos) {
				if(contrastInitSubVO.getStatus() == VOStatus.UPDATED) {
					// 校验版本
					BDVersionValidationUtil.validateSuperVO(contrastInitSubVO);
					/********状态改为删除，删掉历史数据：避免修改“核算账簿+科目+辅助核算+凭证号+凭证类型+凭证日期+币种+方向”共8项数据，从而造成生成contrastinitsubvo的不同**********/
					contrastInitSubVO.setStatus(VOStatus.DELETED);
					updateAndDeletesubList.add(contrastInitSubVO);
					updataAndInsertsubList.add((ContrastInitSubVO)contrastInitSubVO.clone());
					updateSubPkSet.add(contrastInitSubVO.getPk_contrastinitsub());
				}else if(contrastInitSubVO.getStatus() == VOStatus.DELETED) {
					updateAndDeletesubList.add(contrastInitSubVO);
					updateSubPkSet.add(contrastInitSubVO.getPk_contrastinitsub());
				}else if(contrastInitSubVO.getStatus() == VOStatus.NEW){
					updataAndInsertsubList.add((ContrastInitSubVO)contrastInitSubVO.clone());
				}
			}
		}
		
		GLAdjustVODataUtil.adjustNullToZero(vo);
		ContrastInitSubVO[] contrastinitsub = vo.getContrastinitsub();
		GLAdjustVODataUtil.adjustNullToZero(contrastinitsub);
		//2.传入的子表修改或者删除部分--删除
		vo.setContrastinitsub(updateAndDeletesubList.toArray(new ContrastInitSubVO[0]));
		ContrastInitVO updateVO = super.updateVO(vo);
		//3.将修改过的期初表体数据从对账数据表中删除
		if(updateSubPkSet != null && updateSubPkSet.size()>0) {
			deleteSynDataByInitSubPks(updateSubPkSet.toArray(new String[0]));
		}
		/**********************重新设置子表主键：   核算账簿+科目+assid+凭证号+凭证类型+凭证日期+币种+方向    共8项：重新生成contrastinitsubvo的主键*************************/
		/**********************这里通过assid判断是不是同一个辅助核算！如果业务上需要根据部分辅助核算组合判断，则应该修改下面生成Pk_contrastinitsub的逻辑*************************/
		ContrastInitSubVO[] updataAndInsertsub = updataAndInsertsubList.toArray(new ContrastInitSubVO[0]);
		for (int i = 0; i < updataAndInsertsub.length; i++) {
			StringBuilder srcpk = new StringBuilder();
			srcpk.append(vo.getPk_accountingbook());
			srcpk.append(vo.getPk_accasoa());
//			String pk_customer = (String) new BaseDAO().executeQuery("select f4 from gl_docfree1 where assid = '" + updataAndInsertsub[i].getPk_customer() + "'", new ColumnProcessor());
//			srcpk.append(pk_customer);  //子表的“客商主键”便是assid.这里通过客商主键判断
			srcpk.append(updataAndInsertsub[i].getPk_customer());
			srcpk.append(updataAndInsertsub[i].getVoucherno());
			srcpk.append(updataAndInsertsub[i].getPk_vouchertype());
			srcpk.append(updataAndInsertsub[i].getVoucherdate().toStdString());
			srcpk.append(updataAndInsertsub[i].getPk_currtype());
			srcpk.append(updataAndInsertsub[i].getDirection().equals("Y")?1:-1);
//			srcpk.append(updataAndInsertsub[i].getYear());//因为有结转下年，所以增加年来做区分
			String md5 = new MD5().getMD5ofStr(srcpk.toString());
			updataAndInsertsub[i].setPk_contrastinitsub(md5.substring(6, 26)); //取md5的中间20位作为子表的主键
			updataAndInsertsub[i].setPk_detail(md5.substring(6, 26));
			updataAndInsertsub[i].setStatus(VOStatus.NEW);//全部按新增处理，同步修改造成的子表主键变动
			updataAndInsertsub[i].setTs(null);//重新设置时间戳，避免版本校验失败
		}
		//4.重新将“修改的数据和新增加的数据”写到数据库
		updateVO.setContrastinitsub(updataAndInsertsub);
		updateVO = super.updateVO(updateVO);
		
		//5.最后将4步中的数据同步到gl_contrastdata中
		ContrastInitVO initVo = MDPersistenceService.lookupPersistenceQueryService().queryBillOfVOByPK(ContrastInitVO.class, updateVO.getPk_contrastinit(), false);
		contrastinitsubvos = initVo.getContrastinitsub();
		
		if(contrastinitsubvos != null && contrastinitsubvos.length>0) {
			Set<String> pkSet = new HashSet<String>();
			for (ContrastInitSubVO contrastInitSubVO : contrastinitsubvos) {
				if(!contrastInitSubVO.getIscontrasted().booleanValue() || initVo.getIscontrasted().booleanValue()) {
					pkSet.add(contrastInitSubVO.getPk_contrastinitsub());
				}
			}
			
		if(pkSet != null && pkSet.size()>0) {
			Collection<ContrastDataVO> c = new BaseDAO().retrieveByClause(ContrastDataVO.class, SqlTools.getInStr(ContrastDataVO.PK_DETAIL, pkSet.toArray(new String[0]), true));
			Set<String> haveSet = new HashSet<String>();
			if(c != null && c.size()>0) {
				for (ContrastDataVO contrastDataVO : c) {
					haveSet.add(contrastDataVO.getPk_detail());
				}
			}
			Set<String> lastSet = new HashSet<String>();
			for (String key : pkSet) {
				if(!haveSet.contains(key)) {
					lastSet.add(key);
				}
			}
			
			if(lastSet != null && lastSet.size()>0) {
				//同步gl_contrastdata数据
				synChronizeInitDataByInitPK(updateVO.getPk_contrastinit(),lastSet.toArray(new String[0]),updateVO.getPk_group());
			}
		}
		}
		ContrastInitSubVO[] contrastinitsub2 = initVo.getContrastinitsub();
		//6.将最终数据显示到页面上
		List<ContrastInitSubVO> subList = new ArrayList<ContrastInitSubVO>();
		if(contrastinitsub2 != null && contrastinitsub2.length>0) {
			for (ContrastInitSubVO contrastInitSubVO : contrastinitsub2) {
				Object clone = contrastInitSubVO.clone();
				subList.add((ContrastInitSubVO) clone);
			}
		}
		
		//设置是为了重新计算列表上的值
		setDefaultInfo(initVo,false);
		initVo.setContrastinitsub(null);
		
		ContrastInitVO rtInitVo = super.updateVO(initVo);
		rtInitVo.setContrastinitsub(subList.toArray(new ContrastInitSubVO[0]));
		
		GLAdjustVODataUtil.adjustZeroToNull(rtInitVo);
		GLAdjustVODataUtil.adjustZeroToNull(subList.toArray(new ContrastInitSubVO[0]));
		
		return rtInitVo;
	}

	/**
	 * 给定对账规则、核算账簿下的所有的数据是否都已经结转套下年了
	 * @param year
	 * @param pk_contrastrules
	 * @param accbooks
	 * @param pk_group
	 * @return
	 */
	private boolean isConvert2NextYear(String year,String[] pk_contrastrules,String[] accbooks ,String pk_group){
		return false;
	}
	
	/**
	 * 同步期初数据到对账数据表，
	 * @param pk_contrastinit
	 * @param pk_contrastinitsubs  需要同步分录的pk
	 * @return
	 * @throws BusinessException
	 */
	public boolean synChronizeInitDataByInitPK(String pk_contrastinit,String[] pk_contrastinitsubs,String pk_group)
	throws BusinessException {
		// deleteSynData(pk_contrastinit);
		// 同步 SQL
		ContrastInitVO retrieveByPK = (ContrastInitVO) new BaseDAO()
				.retrieveByPK(ContrastInitVO.class, pk_contrastinit);
		
		String pk_contrastrule = retrieveByPK.getPk_contrastrule();
		
		ContrastRuleVO contrastRuleVo = NCLocator.getInstance().lookup(IContrastRule.class).findByPrimaryKey(pk_contrastrule);
		UFBoolean ismainorgcontrast = contrastRuleVo.getIsmainorgcontrast();
		
		StringBuffer sysSqlBuf = new StringBuffer();
		sysSqlBuf
				.append("insert into gl_contrastdata (")
				.append(" pk_detail,")
				.append(" pk_voucher, ")
				.append(" pk_accasoa, ")
				.append(" assid, ")
				.append(" debitquantity, ")
				.append(" debitamount, ")
				.append(" localdebitamount, ")
				.append(" groupdebitamount, ")
				.append(" globaldebitamount, ")
				.append(" creditquantity, ")
				.append(" creditamount, ")
				.append(" localcreditamount, ")
				.append(" groupcreditamount, ")
				.append(" globalcreditamount, ")
				.append(" pk_vouchertypev, ")
				.append(" yearv, ")
				.append(
						" prepareddatev, ")
				.append(" pk_managerv, ")
				.append(" pk_othercorp, ")
				.append(" pk_otherorgbook, ")
				.append(" pk_accountingbook, ")
				.append(" pk_setofbook, ")
				.append(" pk_group, ")
				.append(" assidarray, ")
				.append(" pk_innerorg, ")
				.append(" pk_innersob, ")
				.append(" innerbusdate, ")
				.append(" innerbusno, ")
				.append(" iscontrasted, ")
				.append(" pk_customer, ")
				.append(" voucherno, ")
				.append(" detailno, ")
				.append(" explanation, ")
				.append(" pk_currtype, ")
				.append(" checkstyle, ")
				.append(" checkno, ")
				.append(" checkdate, ")
				.append(" isinit, ")
				.append(" yearinit, ")
				.append(" pk_contrastdata,")
				.append(" discardflag,")
				.append(" pk_contrastrule, ")
				.append("AMOUNTEQUAL,")
				.append("QUANTITYEQUAL,")
				.append("periodv")
				.append(",pk_org")
				.append(",busireconno")
				.append(",ts")
				.append(
						" ) select sub.pk_contrastinitsub,  null, init.pk_accasoa, sub.pk_customer, ")
				.append(" case " + " when ")
				.append(" (")
				.append("      sub.direction = 'Y'")
				.append("  ) ")
				.append(" then sub.quantity ")
				.append(" else 0 ")
				.append(" end ")
				.append(" debitquantity, ")
				.append(" case ")
				.append("  when ")
				.append(" (")
				.append("     sub.direction = 'Y'")
				.append("  ) ")
				.append(" then sub.amount ")
				.append(" else 0 ")
				.append(" end ")
				.append(" debitamount, ")
				.append(" case ")
				.append(" when ")
				.append(" (")
				.append("  sub.direction = 'Y'")
				.append("  ) ")
				.append("  then sub.locamount ")
				.append(" else 0 ")
				.append(" end ")
				.append(" localdebitamount, ")
				.append(" case ")
				.append(" when ")
				.append(" (")
				.append("   sub.direction = 'Y'")
				.append("  ) ")
				.append("  then sub.groupamount ")
				.append("  else 0 ")
				.append(" end ")
				.append(" groupdebitamount, ")
				.append(" case ")
				.append(" when ")
				.append(
						" ( sub.direction = 'Y' )  then sub.globalamount   else 0  end  globaldebitamount, ")
				.append(
						" case when   (  sub.direction = 'N' ) then sub.quantity  else 0 end creditquantity, ")
				.append(
						" case  when (  sub.direction = 'N' )  then sub.amount  else 0  end  creditamount,  ")
				.append(
						" case when  (  sub.direction = 'N' ) then sub.locamount  else 0   end  localcreditamount, ")
				.append(
						" case when  ( sub.direction = 'N' )  then sub.groupamount  else 0 end  groupcreditamount, ")
				.append(
						" case   when  ( sub.direction = 'N')   then sub.globalamount  else 0  end   globalcreditamount, ")
				.append(
						" sub.pk_vouchertype, sub.year, sub.voucherdate, '~',    org_accountingbook.PK_RELORG , ")
				.append(
						" org_accountingbook.pk_accountingbook,  init.pk_accountingbook,  crule.pk_book, ")
				.append(
						" init.pk_group,  init.assid, null,  null, null,  null,  init.iscontrasted,   gl_docfree1."+FX+", sub.voucherno, sub.detailno, ")
				.append(
						" sub.explanation,  sub.pk_currtype, sub.checkstyle,  sub.checkno,   sub.checkdate,  'Y',  'N', pk_contrastinitsub ,'N', sub.pk_contrastrule , ")
				.append(
						" case when init.ISCONTRASTED='Y'  then 'Y' else 'N' end AMOUNTEQUAL,")
				.append(
						"  case when init.ISCONTRASTED='Y'  then 'Y' else 'N' end QUANTITYEQUAL ,")
				.append("   case when init.ISCONTRASTED='Y' then '"+IContrastInit.STRATPERIOD+"' end ")
				.append(" , (select pk_relorg from org_accountingbook where org_accountingbook.pk_accountingbook=init.pk_accountingbook) ")
				.append(",sub.busireconno").append(" , sub.ts ")
				.append(" from  gl_contrastinitsub sub ")
				.append(" inner join ")
				.append(" gl_contrastinit init ")
				.append(" on ")
				.append(" sub.pk_contrastinit      = init.pk_contrastinit ")
				.append(" and " + SqlTools.getInStr("sub.pk_contrastinitsub", pk_contrastinitsubs, true))
				.append(" inner join ")
				.append(" gl_contrastrule crule ")
				.append("  on ")
				.append(" init.pk_contrastrule = crule.pk_contrastrule ")
				.append(" inner join ")
				.append(
						" gl_docfree1  on  sub.pk_customer    = gl_docfree1.assid   and isnull(gl_docfree1."+FX+",'~')<> '~' ")
				.append(" and gl_docfree1."+FX+" <> 'NN/A'  ")
				.append(" inner join ")
				.append(" bd_cust_supplier supp ")
				.append(" on ")
				.append(" gl_docfree1."+FX+" = supp.pk_cust_sup")
				.append(" inner join ")
				.append(" org_accountingbook ")
				.append(" on ")
				.append(
						" supp.pk_financeorg = org_accountingbook.pk_relorg ");
				//根据对账规则是否按主账簿对账 增加条件
		         if(ismainorgcontrast == null || !ismainorgcontrast.booleanValue()) {
		        	 sysSqlBuf.append(" and org_accountingbook.pk_setofbook = crule.pk_book ");
		         }else {
		        	 sysSqlBuf.append(" and org_accountingbook.ACCOUNTTYPE=1 ");
		         }
		// String pk_custsupAccassitem =
		// nc.vo.gl.contrast.uap.proxy.AccassItemProxy.getInstance().getCustsuppluerAccassItemPk();
		String[] custSuppAccassitems = nc.vo.gl.contrast.uap.proxy.AccassItemProxy
				.getInstance().getCustSuppAccassitems();

		Set<String> pk_customerSet = nc.vo.gl.contrast.uap.proxy.AccassItemProxy
				.getInstance().getcustomerAccassItemPkSet();// 客户基本信息
		Set<String> pk_supplierSet = nc.vo.gl.contrast.uap.proxy.AccassItemProxy
				.getInstance().getsupplierAccassItemPkSet();// 供应商
		AccountVO[] queryAccountVOsByPks = NCLocator.getInstance().lookup(
				IAccountPubService.class).queryAccountVOsByPks(
				new String[] { retrieveByPK.getPk_accasoa() },
				new UFDate().toLocalString());
		AccAssVO[] accass = queryAccountVOsByPks[0].getAccass();
		String pk_custsupAccassitem = "";
		HashSet<String> set = new HashSet<String>();
		set.addAll(Arrays.asList(custSuppAccassitems));
		// 以科目的第一个(客商,内部客商,供应商)辅助核算为准,
		for (int i = 0; i < accass.length; i++) {
			AccAssVO accAssVO = accass[i];
			if (set.contains(accAssVO.getPk_entity())) {
				pk_custsupAccassitem = accAssVO.getPk_entity();
				break;
			}
		}

		// String[] custSuppAccassitems =
		// nc.vo.gl.contrast.uap.proxy.AccassItemProxy.getInstance().getCustSuppAccassitems();
		GlAccAssinfoVO[] accAssInfoByChecktypes = FiPubFreeValueProxy
				.getRemoteFreeMap().getAccAssInfoByChecktypes(
						new String[] { pk_custsupAccassitem },pk_group, Module.GL);
		GlAccAssinfoVO accAssinfoVO = accAssInfoByChecktypes[0];
		String synSql = sysSqlBuf.toString();
		synSql = synSql.replace("gl_docfree1", accAssinfoVO.getTableName()
				.trim());
		synSql = synSql.replace(FX, accAssinfoVO.getFieldName().trim());

		if (pk_customerSet != null && pk_customerSet.size() > 0
				&& pk_customerSet.contains(pk_custsupAccassitem)) {
			// 客户基本信息辅助核算
			synSql = synSql.replace("bd_cust_supplier", "bd_customer");
			synSql = synSql.replace("pk_cust_sup", "pk_customer");
		} else if (pk_supplierSet != null && pk_supplierSet.size() > 0
				&& pk_supplierSet.contains(pk_custsupAccassitem)) {
			// 供应商辅助核算
			synSql = synSql.replace("bd_cust_supplier", "bd_supplier");
			synSql = synSql.replace("pk_cust_sup", "pk_supplier");
		}
		
		new BaseDAO().executeUpdate(synSql);
		return true;
		// return new
		// ContrastInitSubBO().synChronizeInitDataByInitPK(pk_contrastinit);
}

	@Override
	public boolean synChronizeInitDataByInitPK(String pk_contrastinit,boolean isYearInit,String pk_group)
			throws BusinessException {
//		deleteSynData(pk_contrastinit);
		//同步 SQL
		ContrastInitVO retrieveByPK = (ContrastInitVO) new BaseDAO().retrieveByPK(ContrastInitVO.class, pk_contrastinit);
		
        String pk_contrastrule = retrieveByPK.getPk_contrastrule();
		
		ContrastRuleVO contrastRuleVo = NCLocator.getInstance().lookup(IContrastRule.class).findByPrimaryKey(pk_contrastrule);
		UFBoolean ismainorgcontrast = contrastRuleVo.getIsmainorgcontrast();
		
		StringBuffer sysSqlBuf = new StringBuffer();
		  sysSqlBuf.append("insert into gl_contrastdata (").append(
				" pk_detail,").append(
				" pk_voucher, " ).append(
				" pk_accasoa, " ).append(
				" assid, " ).append(
				" debitquantity, " ).append(
				" debitamount, " ).append(
				" localdebitamount, " ).append(
				" groupdebitamount, " ).append(
				" globaldebitamount, " ).append(
				" creditquantity, " ).append(
				" creditamount, " ).append(
				" localcreditamount, " ).append(
				" groupcreditamount, " ).append(
				" globalcreditamount, " ).append(
				" pk_vouchertypev, " ).append(
				" yearv, " ).append(
				" prepareddatev, " ).append(
				" pk_managerv, " ).append(
				" pk_othercorp, " ).append(
				" pk_otherorgbook, " ).append(
				" pk_accountingbook, " ).append(
				" pk_setofbook, " ).append(
				" pk_group, " ).append(
				" assidarray, " ).append(
				" pk_innerorg, " ).append(
				" pk_innersob, " ).append(
				" innerbusdate, " ).append(
				" innerbusno, " ).append(
				" iscontrasted, " ).append(
				" pk_customer, " ).append(
				" voucherno, " ).append(
				" detailno, " ).append(
				" explanation, " ).append(
				" pk_currtype, " ).append(
				" checkstyle, " ).append(
				" checkno, " ).append(
				" checkdate, " ).append(
				" isinit, " ).append(
				" yearinit, " ).append(
				" pk_contrastdata," ).append(
				" discardflag," ).append(
				" pk_contrastrule, " ).append(
				"AMOUNTEQUAL," ).append(
				"QUANTITYEQUAL," ).append(
				"periodv,pk_org").append(
				",busireconno").append(
				",ts").append(
				" ) select "+( isYearInit == true ? " sub.pk_detail, " : " sub.pk_contrastinitsub, " )+"  null, init.pk_accasoa, sub.pk_customer, " ).append(
				" case " +
				" when " ).append(
				" (" ).append(
				"      sub.direction = 'Y'" ).append(
				"  ) " ).append(
				" then sub.quantity " ).append(
				" else 0 " ).append(
				" end " ).append(
				" debitquantity, " ).append(
				" case " ).append(
				"  when " ).append(
				" (" ).append(
				"     sub.direction = 'Y'" ).append(
				"  ) " ).append(
				" then sub.amount " ).append(
				" else 0 " ).append(
				" end " ).append(
				" debitamount, " ).append(
				" case " ).append(
				" when " ).append(
				" (" ).append(
				"  sub.direction = 'Y'" ).append(
				"  ) " ).append(
				"  then sub.locamount " ).append(
				" else 0 " ).append(
				" end " ).append(
				" localdebitamount, " ).append(
				" case " ).append(
				" when " ).append(
				" (" ).append(
				"   sub.direction = 'Y'" ).append(
				"  ) " ).append(
				"  then sub.groupamount " ).append(
				"  else 0 " ).append(
				" end " ).append(
				" groupdebitamount, " ).append(
				" case " ).append(
				" when " ).append(
				" ( sub.direction = 'Y' )  then sub.globalamount   else 0  end  globaldebitamount, " ).append(
				" case when   (  sub.direction = 'N' ) then sub.quantity  else 0 end creditquantity, " ).append(
				" case  when (  sub.direction = 'N' )  then sub.amount  else 0  end  creditamount,  " ).append(
				" case when  (  sub.direction = 'N' ) then sub.locamount  else 0   end  localcreditamount, " ).append(
				" case when  ( sub.direction = 'N' )  then sub.groupamount  else 0 end  groupcreditamount, " ).append(
				" case   when  ( sub.direction = 'N')   then sub.globalamount  else 0  end   globalcreditamount, " ).append(
				" sub.pk_vouchertype,sub.year, sub.voucherdate, '~',    org_accountingbook.PK_RELORG, " ).append(
				" org_accountingbook.pk_accountingbook,  init.pk_accountingbook,  crule.pk_book, " ).append(
				" init.pk_group,  init.assid, null,  null, null,  null,  init.iscontrasted,   gl_docfree1."+FX+", sub.voucherno, sub.detailno, " ).append(
				" sub.explanation,  sub.pk_currtype, sub.checkstyle,  sub.checkno,   sub.checkdate,  'Y', ")
				.append(isYearInit?" 'Y' ":" 'N' ")
				.append(",pk_contrastinitsub ,'N', sub.pk_contrastrule , " ).append(
				" case when init.ISCONTRASTED='Y'  then 'Y' else 'N' end AMOUNTEQUAL," ).append(
				"  case when init.ISCONTRASTED='Y'  then 'Y' else 'N' end QUANTITYEQUAL,").append(
				" case when init.ISCONTRASTED='Y' then '"+IContrastInit.STRATPERIOD+"' end ")
				.append(" ,(select pk_relorg from org_accountingbook where org_accountingbook.pk_accountingbook=init.pk_accountingbook) ")
				.append(" ,sub.busireconno ").append(" ,sub.ts ")
				.append(
				" from  gl_contrastinitsub sub " ).append(
				" inner join " ).append(
				" gl_contrastinit init " ).append(
				" on " ).append(
				" sub.pk_contrastinit      = init.pk_contrastinit " ).append(
				" and init.pk_contrastinit = ? " ).append(
				" inner join " ).append(
				" gl_contrastrule crule " ).append(
				"  on " ).append(
				" init.pk_contrastrule = crule.pk_contrastrule " ).append(
				" inner join " ).append(
				" gl_docfree1  on  sub.pk_customer    = gl_docfree1.assid   and isnull(gl_docfree1."+FX+",'~')<> '~' " ).append(
				" and gl_docfree1."+FX+" <> 'NN/A'  " ).append(
				" inner join " ).append(
				" bd_cust_supplier supp " ).append(
				" on " ).append(
				" gl_docfree1."+FX+" = supp.pk_cust_sup" ).append(
				" inner join " ).append(
				" org_accountingbook " ).append(
				" on " ).append(
				" supp.pk_financeorg                  = org_accountingbook.pk_relorg " );
		//根据对账规则是否按主账簿对账 增加条件
	         if(ismainorgcontrast == null || !ismainorgcontrast.booleanValue()) {
	        	 sysSqlBuf.append(" and org_accountingbook.pk_setofbook = crule.pk_book ");
	         }else {
	        	 sysSqlBuf.append(" and org_accountingbook.ACCOUNTTYPE=1 ");
	         }
//		String pk_custsupAccassitem = nc.vo.gl.contrast.uap.proxy.AccassItemProxy.getInstance().getCustsuppluerAccassItemPk();
		String[] custSuppAccassitems = nc.vo.gl.contrast.uap.proxy.AccassItemProxy.getInstance().getCustSuppAccassitems();

		Set<String> pk_customerSet =nc.vo.gl.contrast.uap.proxy.AccassItemProxy.getInstance().getcustomerAccassItemPkSet();//客户基本信息
		Set<String> pk_supplierSet =nc.vo.gl.contrast.uap.proxy.AccassItemProxy.getInstance().getsupplierAccassItemPkSet();//供应商
		AccountVO[] queryAccountVOsByPks = NCLocator.getInstance().lookup(IAccountPubService.class).queryAccountVOsByPks(new String[]{retrieveByPK.getPk_accasoa()}, new UFDate().toLocalString());
		AccAssVO[] accass = queryAccountVOsByPks[0].getAccass();
		String pk_custsupAccassitem = "";
		HashSet<String> set= new HashSet<String>();
		set.addAll(Arrays.asList(custSuppAccassitems));
		if(accass != null) {
		//以科目的第一个(客商,内部客商,供应商)辅助核算为准,
		for (int i = 0; i < accass.length; i++) {
			AccAssVO accAssVO = accass[i];
			if(set.contains(accAssVO.getPk_entity())){
				pk_custsupAccassitem = accAssVO.getPk_entity();
				break;
			}
		}
		}
		
		if(StringUtils.isEmpty(pk_custsupAccassitem)) {
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("contrast_0","02002002-0505")/*@res " 科目没有对应客商、内部客商、客户或者供应商的辅助核算！"*/);
		}

//		String[] custSuppAccassitems = nc.vo.gl.contrast.uap.proxy.AccassItemProxy.getInstance().getCustSuppAccassitems();
		GlAccAssinfoVO[] accAssInfoByChecktypes = FiPubFreeValueProxy.getRemoteFreeMap().getAccAssInfoByChecktypes(new String[]{pk_custsupAccassitem},pk_group, Module.GL);
		GlAccAssinfoVO accAssinfoVO = accAssInfoByChecktypes[0];
		String synSql = sysSqlBuf.toString();
		synSql = synSql.replace("gl_docfree1", accAssinfoVO.getTableName().trim());
		synSql = synSql.replace(FX, accAssinfoVO.getFieldName().trim());

		if(pk_customerSet != null && pk_customerSet.size()>0 && pk_customerSet.contains(pk_custsupAccassitem)){
			//客户基本信息辅助核算
			synSql = synSql.replace("bd_cust_supplier", "bd_customer");
			synSql = synSql.replace("pk_cust_sup", "pk_customer");
		}else if(pk_supplierSet != null && pk_supplierSet.size()>0 && pk_supplierSet.contains(pk_custsupAccassitem)){
			//供应商辅助核算
			synSql = synSql.replace("bd_cust_supplier", "bd_supplier");
			synSql = synSql.replace("pk_cust_sup", "pk_supplier");
		}
		SQLParameter para = new SQLParameter();
		para.addParam(pk_contrastinit);
		new BaseDAO().executeUpdate(synSql, para);
		
		//add by yinxtf 解决结转缺失协同号和备注问题
		StringBuffer sqlwhere = new StringBuffer();
		sqlwhere.append("ts like '").append(new String().valueOf(new UFDate()).substring(0, 10)).append("%'");
		List<ContrastDataRemarkVO> newlcs = (List<ContrastDataRemarkVO>) new BaseDAO().retrieveByClause(ContrastDataRemarkVO.class, sqlwhere.toString());
		if(newlcs.size()>0){
			for(ContrastDataRemarkVO vo:newlcs){
				if(vo.getVoucherno()== null || "".equals(vo.getVoucherno())) continue;
				StringBuffer where = new StringBuffer();
				if(vo.getGroupcreditamount() != null)where.append("groupcreditamount ='").append(vo.getGroupcreditamount()).append("'").append(" and ");
				if(vo.getDetailno() != null)where.append("detailno ='").append(vo.getDetailno()).append("'").append(" and ");
				if(vo.getAssid() != null)where.append("assid ='").append(vo.getAssid()).append("'").append(" and ");
				if(vo.getPk_voucher() != null)where.append("pk_voucher ='").append(vo.getPk_voucher()).append("'").append(" and ");
				
				//添加查询字段有可能有重复数据，需要加上这个过滤条件at：2020/04/22  --央客王志强
				if(vo.getPk_detail() != null)where.append("pk_detail ='").append(vo.getPk_detail()).append("'").append(" and ");				
				
				if(vo.getYearv() != null)where.append("yearv ='").append(vo.getYearv()== null?"":new Integer(vo.getYearv())-1).append("'").append(" and ");
				if(vo.getPeriodv() != null)where.append("periodv ='").append(vo.getPeriodv()).append("'").append(" and ");
				if(vo.getVoucherkind() != null)where.append("voucherkind ='").append(vo.getVoucherkind()).append("'").append(" and ");
				where.append("voucherno ='").append(vo.getVoucherno()).append("'");
				List<ContrastDataRemarkVO> oldlcs = (List<ContrastDataRemarkVO>) new BaseDAO().retrieveByClause(ContrastDataRemarkVO.class, where.toString());
				if(oldlcs.size()>0){
					vo.setRemark(oldlcs.get(0).getRemark());
					vo.setInnerbusno(oldlcs.get(0).getInnerbusno());
					new BaseDAO().updateVO(vo);
				}
			}
		}
		//end
		return true;
//		return new ContrastInitSubBO().synChronizeInitDataByInitPK(pk_contrastinit);
	}
	
	protected void deleteSynDataByInitSubPks(String[] pk_contrastinitsub) throws BusinessException {
		// 根据期初数据主键同步gl_contrastdata
		// 先删除已同步的数据
		try {
			String delSql = "delete from  gl_contrastdata  where "+SqlTools.getInStr("pk_detail", pk_contrastinitsub, true);
			new BaseDAO().executeUpdate(delSql);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new BusinessException(e.getMessage(),e);
		}
	}

	protected void deleteSynData(String pk_contrastinit) throws DAOException {
		// 根据期初数据主键同步gl_contrastdata
		// 先删除已同步的数据
		try {
			int[] types = { java.sql.Types.CHAR };
			// ContrastInitSubVO[] queryArray = new
			// ContrastInitSubBO().query(new String[]{" pk_contrastinit "}, new
			// String[]{pk_contrastinit},types);
			// Collection collection=new
			// BaseDAO().retrieveByClause(ContrastInitVO.class,
			// " pk_contrastinit='"+pk_contrastinit+"'");
			// ArrayList<String>sqlArray = new ArrayList<String>();
			// if(collection!= null && collection.size()>0){
			// ContrastInitVO
			// mainArray[]=(ContrastInitVO[])collection.toArray(new
			// ContrastInitVO[0]);
			// ContrastInitVO mainVO = mainArray[0];
			// Collection collectionSub=new
			// BaseDAO().retrieveByClause(ContrastInitSubVO.class,
			// " pk_contrastinit='"+pk_contrastinit+"'");
			// /**
			// * 根据期初初始化字表中的数据信息去删除对账数据表中的数据
			// */
			// ContrastInitSubVO[]subArray=
			// (ContrastInitSubVO[])collectionSub.toArray(new
			// ContrastInitSubVO[0]);
			// StringBuffer buffer = new StringBuffer();
			//
			// for (int i = 0; i < subArray.length; i++) {
			// buffer.delete(0, buffer.length());
			// buffer.append(" delete from gl_contrastdata where ");
			// buffer.append(" pK_ACCOUNTINGBOOK = '"+mainVO.getPk_accountingbook()+"' ");
			// buffer.append(" and PK_ACCASOA = '"+mainVO.getPk_accasoa()+"' ");
			// buffer.append(" and isinit='Y' ");
			// buffer.append(" and yearv='"+subArray[i].getYear()+"' ");
			// buffer.append(" and periodv='"+subArray[i].getPeriod()+"' ");
			// buffer.append(" and pk_contrastrule='"+mainVO.getPk_contrastrule()+"' ");
			// buffer.append(" and pk_currtype='"+subArray[i].getPk_currtype()+"' ");
			// buffer.append(" and debitamount='"+subArray[i].getAmount()+"' ");
			// //
			// buffer.append(" and pk_customer='"+subArray[i].getPk_customer()+"' ");
			// sqlArray.add(buffer.toString());
			// }
			// }
			String delSql = "delete from  gl_contrastdata  where pk_contrastdata in (select pk_contrastinitsub from  gl_contrastinitsub  where  pk_contrastinit  ='"
					+ pk_contrastinit + "')  ";
			new BaseDAO().executeUpdate(delSql);

			// new SingleTabDMO().execute(sqlArray.toArray(new String[0]));
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new DAOException(e);
		}
	}

	protected void deleteSynData(String[] pk_contrastinit) throws BusinessException {
		//根据期初数据主键同步gl_contrastdata
		//先删除已同步的数据
		String buildInSql = GLSqlUtil.buildInSql(ContrastInitVO.PK_CONTRASTINIT, pk_contrastinit);
		String delSql = "delete from  gl_contrastdata  where pk_detail in (select pk_contrastinitsub from  gl_contrastinitsub  where  "+buildInSql+")  ";

		new BaseDAO().executeUpdate(delSql);
	}

	@Override
	public ContrastInitVO delete(ContrastInitVO contrastInitVO)throws BusinessException {
	/*	ContrastInitVO[] tcontrastInitVO=this.query(" a."+contrastInitVO.PK_CONTRASTINIT+"='"+contrastInitVO.getPk_contrastinit()+"' ");
		ContrastInitVO returnVo=null;
		if(tcontrastInitVO!=null && tcontrastInitVO.length>0)
			returnVo=tcontrastInitVO[0];
		if(this.delete(contrastInitVO.getPk_contrastinit()))
			return returnVo;
		return null;*/
		//删除同步gl_contrastdata数据,应该在执行过对账不允许删除
		deleteSynData(contrastInitVO.getPk_contrastinit());
		super.deleteVO(contrastInitVO);
//		synChronizeInitDataByInitPK(contrastInitVO.getPk_contrastinit());
		return contrastInitVO;
	}

	@Override
	public boolean initBalanceCompute(ContrastInitBalanceConditionVO countVO)
			throws BusinessException {
		String pk_accountingbook = countVO.getPk_accountBook();
		
		if(StringUtil.isEmpty(pk_accountingbook))
			return false;
		
		String periodyear = countVO.getYear();
		
		boolean initBuild = this.isInitBuild(pk_accountingbook, String.valueOf(periodyear));
		//如果期初未建账则抛异常，不允许计算期初余额
		if(!initBuild) {
			AccountingBookVO bookVo = AccountBookUtil.getAccountingBookVOByPrimaryKey(pk_accountingbook);

			throw new BusinessException(GLMultiLangUtil.getMultiName(bookVo)+nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("private20111017_0","02002001-0008")/*@res " 账簿未建账，无法计算期初余额！"*/);
		}
		//校验上一年是否有对账数据
		boolean checkHaveContrastedData = NCLocator.getInstance().lookup(IContrastInit.class).checkHaveContrastedData(countVO.getCountrastRuleVO().getPk_contrastrule(), pk_accountingbook,String.valueOf(Integer.valueOf(periodyear).intValue()-1), countVO.getPk_group());
		
		if(checkHaveContrastedData) {
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl
					.getNCLangRes().getStrByID("contrast_0", "02002002-0497",null,new String[]{periodyear})/*
					 * @res
					 * "上一年已经有对账数据，不允许对{0}年数据做期初初始化！"
					 */);
		}
		
		boolean canInitData = NCLocator.getInstance().lookup(IContrastInit.class).checkCanInitData(countVO.getCountrastRuleVO().getPk_contrastrule(), pk_accountingbook, periodyear, countVO.getPk_group());
		
		if(!canInitData) {
		    throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("private20111017_0","02002001-0205")/*@res " 所选核算账簿与对账规则已经结转下年或者已有正式报告生成，不允许再次计算期初余额！"*/);	
		}
		
		//当前规则启用日期
		UFDate ruleStartDate = countVO.getCountrastRuleVO().getStartdate();
		
		AccountingBookVO accountingBookVo = AccountBookUtil.getAccountingBookVOByPrimaryKey(pk_accountingbook);
			AccountCalendar calendar = AccountCalendar.getInstanceByAccperiodMonth(accountingBookVo.getPk_accountperiod());
			UFDate bookStartDate = calendar.getMonthVO().getBegindate();
			
			//设置会计年，
			calendar.set(periodyear);
			UFDate yearStartDate = calendar.getMonthVO().getBegindate();
		
		if(bookStartDate.after(ruleStartDate)) {
			ruleStartDate = bookStartDate;
		}
		
		if(yearStartDate.after(ruleStartDate)) {
			ruleStartDate = yearStartDate;
		}
		
		countVO.setStartDate(ruleStartDate);
		
		ContrastInitBO initBO = new ContrastInitBO();
		return initBO.initBalanceCompute(countVO);
	}

	/**
	 * 是否已建账
	 *
	 * @throws BusinessException
	 */
	private boolean isInitBuild(String pk_acctingbook,String year) throws BusinessException {
		int count = GLPubProxy.getRemoteInitBalance().isBuiltByGlOrgBook(
				pk_acctingbook, year);
		if (count == 0)
			return false;
		else
			return true;
	}

	private IContrastReport contrastReportService;
	private IContrastReport getContrastReportService() {
		if(contrastReportService == null)
			contrastReportService = NCLocator.getInstance().lookup(IContrastReport.class);
		return contrastReportService;
	}
	/*
	 *
	 * transferToNextYear方法在ImpContrastInit中的实现
	 * @see nc.itf.gl.contrast.init.IContrastInit#transferToNextYear(java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String[]  transferToNextYear(String year,String[] pk_contrastrules,String[] accbooks ,String pk_group )throws BusinessException {
		if(pk_contrastrules==null || pk_contrastrules.length==0) return new String[0];

		String[] lockKeys = new String[pk_contrastrules.length];
		for (int i = 0; i < pk_contrastrules.length; i++) {
			lockKeys[i]= "gl_contrasttransfertonextyear_"+ pk_contrastrules[i]+pk_group;
		}
		//结转加锁,动态锁
		boolean addBatchDynamicLock = PKLock.getInstance().addBatchDynamicLock(lockKeys);
		//FIXME:改成单个加锁,加锁成功继续执行结转?
		if(!addBatchDynamicLock){
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("contrastprivate_0","02002002-0015")/*@res "其他用户正在执行结转操作!\n请稍后再试!"*/);
		}
		//将内部交易对账规则按照余额,发生额分组
		ArrayList<ContrastRuleVO>  balanceRules = new ArrayList<ContrastRuleVO>();
		ArrayList<ContrastRuleVO>  allRules = new ArrayList<ContrastRuleVO>();
		Map<String,ContrastRuleVO>  map_rule = new HashMap<String,ContrastRuleVO>();

		ContrastRuleVO[] query = nc.vo.glcom.tools.GLContrastProxy.getRemoteContrastRule().query(GLSqlUtil.buildInSql(ContrastRuleVO.PK_CONTRASTRULE, pk_contrastrules));
		for (int i = 0; i < query.length; i++) {
			ContrastRuleVO contrastRuleVO = query[i];
			if(contrastRuleVO.getContrastmoney().booleanValue()){
				balanceRules.add(contrastRuleVO);
			}
			allRules.add(contrastRuleVO);
			map_rule.put(contrastRuleVO.getPk_contrastrule(), contrastRuleVO);
		}
		//对余额规则,需要结转上一年期余额+对付发生数据作为当年以对付数据;对发生额规则,只需结转上一年的未对付数据至下一年;
		//对所有规则生成未对付期初,由于未对付数据没有与对账规则绑定,所以由此创建临时表以便于规则批量
		StringBuffer columnsSb = new StringBuffer();
		columnsSb.append("pk_contrastrule CHAR(20),pk_accasoa CHAR(20),pk_setofbook CHAR(20),")
		         .append("pk_accountingbook CHAR(20) ,pk_otheraccbook CHAR(20) ,iscontrasted CHAR(1) ,ts CHAR(19)");
		//创建临时表
		String userid = InvocationInfoProxy.getInstance().getUserId();//用户id
		//预警或者后台任务调用时,userid=#UAP#,在此做特殊的处理
		String userStr = userid;
		if(userStr != null && userStr.length() > 15) {
			userStr = userStr.substring(15);
		}
		//表名--表结构会动态变化，这里使用动态的表名，在所有操作完成之后会drop掉临时表
		String tblName = "GL_"+"cit"+System.currentTimeMillis()+userStr;
		tblName = createTempTable(tblName,columnsSb.toString(),null);
		StringBuffer insertSb = new StringBuffer();
		insertSb.append("insert into ").append(tblName)
		        .append("(pk_contrastrule,pk_accasoa,pk_setofbook,pk_accountingbook,pk_otheraccbook,iscontrasted)values(?,?,?,?,?,?)");
		List<SQLParameter> paramList=new ArrayList<SQLParameter>();

		//拆分对账规则,按科目,财务核算账簿,是否对付维度拆分
		if(accbooks ==null || accbooks.length==0){
			String sql = "select distinct pk_accountingbook from  gl_contrastdata  union select distinct pk_otherorgbook from  gl_contrastdata  ";
			List<Object[]> executeQuery = (List<Object[]>) new BaseDAO().executeQuery(sql, new ArrayListProcessor());

			ArrayList<String> allaccbooks = new ArrayList<String>();
			if (executeQuery !=null) {
				for (int i = 0; i < executeQuery.size(); i++) {
					allaccbooks.add((String) executeQuery.get(i)[0]);
				}
			}
			accbooks = allaccbooks.toArray(new String[0]);
		}
		
		for (String pk_acctingbook : accbooks) {
//			AccountCalendar calendar = CalendarUtilGL.getAccountCalendarByAccountBook(pk_acctingbook);
//			AccperiodmonthVO monthVo = calendar.getLastMonthOfCurrentScheme();
			for (String pk_contrastrule : pk_contrastrules) {
				boolean reportChecked = getContrastReportService().isAllReportChecked(pk_acctingbook, pk_contrastrule, year);
				if(!reportChecked) {
					throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("private20111017_0","02002001-0009")/*@res "上一年报告未全部审核！"*/);
				}
			}
		}
		
		//处理结转下年的年初数据的制单日期
		Map<String,String> ruleBookMap = new HashMap<String, String>();
		
		Set<String> ruleKeySet = map_rule.keySet();
		for (String pk_contrastrule : ruleKeySet) {
			ContrastRuleVO contrastRuleVO = map_rule.get(pk_contrastrule);
			String pk_book = contrastRuleVO.getPk_book();
			ruleBookMap.put(pk_contrastrule, pk_book);
		}
		
//		SetOfBookVO[] setOfBookVOs = NCLocator.getInstance().lookup(ISetOfBookQryService.class).queryVOsByPks(ruleBookMap.values().toArray(new String[0]));
//		
//		Map<String,String> bookSchemeMap = new HashMap<String, String>();
//		for (SetOfBookVO setOfBookVO : setOfBookVOs) {
//			bookSchemeMap.put(setOfBookVO.getPk_setofbook(), setOfBookVO.getPk_accperiodscheme());
//		}
		
		//获取财物核算账簿对应会计期间的第一天
		String pk_accperiodscheme = AccountBookUtil.getAccPeriodSchemePKByAccountingbookPk(accbooks[0]);
		
		//用于存储规则启用日期
		Map<String,UFDate> ruleDateMap = new HashMap<String, UFDate>();
		Set<String> ruleBookSet = ruleBookMap.keySet();
		for (String pk_contrastrule : ruleBookSet) {
//			String pk_book = ruleBookMap.get(pk_contrastrule);
//			String pk_periodscheme = bookSchemeMap.get(pk_book);
			AccountCalendar calendar = AccountCalendar.getInstanceByPeriodScheme(pk_accperiodscheme);
			calendar.set(String.valueOf(Integer.valueOf(year).intValue()+1));
			AccperiodmonthVO monthVO = calendar.getMonthVO();
			UFDate begindate = monthVO.getBegindate();
			ruleDateMap.put(pk_contrastrule, begindate);
		}
		
		
		ArrayList<TransferNextYearInfoVO> explianRuleVos = explianRuleVos(allRules, Arrays.asList(accbooks));
		for (Iterator<TransferNextYearInfoVO> iterator = explianRuleVos.iterator(); iterator.hasNext();) {
			TransferNextYearInfoVO transferNextYearInfoVO = (TransferNextYearInfoVO) iterator.next();
			SQLParameter sqlParam=new SQLParameter();
			sqlParam.addParam(transferNextYearInfoVO.getPk_contrasrrule());
			sqlParam.addParam(transferNextYearInfoVO.getPk_accasoa());
			sqlParam.addParam(transferNextYearInfoVO.getPk_setofbook());
			sqlParam.addParam(transferNextYearInfoVO.getPk_accountingbook());
			sqlParam.addParam(transferNextYearInfoVO.getPk_otheraccbook());
			sqlParam.addParam("N");
			paramList.add(sqlParam);
		}
		execBatchSql(insertSb.toString(),paramList);
		//对付数据要做汇总,所以分开处理,先查询未对付数据
		String sql = "  select a.pk_contrastrule ,a.pk_accasoa ,a.pk_accountingbook ,b.prepareddatev ,b.pk_vouchertypev ,b.voucherno,b.assid," +
				" case when debitamount =0  then  'N' else 'Y' end orient ,case when debitamount =0 then creditquantity else debitquantity end quantity," +
				" b.pk_currtype,case when debitamount =0 then creditamount else debitamount end amount," +
				" case when debitamount =0 then localcreditamount else localdebitamount end localamount," +
				" case when debitamount =0 then groupcreditamount else groupdebitamount end groupamount," +
				" case when debitamount =0 then globalcreditamount else globaldebitamount end globalamount ," +
				" b.detailno , b.explanation,b.busireconno,b.pk_detail " +
				" from ( select tmp.pk_contrastrule,tmp.pk_accasoa,tmp.pk_setofbook,tmp.pk_accountingbook,tmp.pk_otheraccbook,tmp.iscontrasted, " +
				" account.code " +
				" from " +
				" "+tblName+" tmp ,bd_accasoa accasoa ,bd_account account where" +
				" tmp.pk_accasoa = accasoa.pk_accasoa and  accasoa.pk_account = account.pk_account )  a  " +
				" inner join " +
				" (select data.voucherno,data.prepareddatev,data.pk_accasoa,data.pk_accountingbook,data.pk_currtype,data.pk_detail,data.pk_setofbook," +
//				" (select data.voucherno,data.prepareddatev,data.pk_accasoa,data.pk_accountingbook,data.pk_currtype,sub.pk_detail,data.pk_setofbook," +
				" data.pk_vouchertypev,data.groupdebitamount," +
				" data.iscontrasted,data.isinit,data.localcreditamount,data.localdebitamount," +
				" data.periodv,data.yearv ,data.assid,data.creditamount,data.creditquantity,data.debitamount,data.debitquantity,data.detailno," +
/*				" data.discardflag,data.globalcreditamount,data.globaldebitamount,data.groupcreditamount, account.code , data.explanation,sub.busireconno  " +*/
" data.discardflag,data.globalcreditamount,data.globaldebitamount,data.groupcreditamount, account.code , data.explanation,data.busireconno  " +
				" from  gl_contrastdata data left join gl_contrastinitsub sub on sub.pk_contrastinitsub = data.pk_contrastdata, bd_accasoa accasoa , bd_account account where " +
				"   data.pk_accasoa = accasoa.pk_accasoa and  accasoa.pk_account = account.pk_account " +
				" and data.yearv = '"+year+"' and data.pk_group = '"+pk_group+"' and data.DISCARDFLAG='N' and (data.PREPAREDDATEV >='"+allRules.get(0).getStartdate().toString()+"' or data.isinit='Y')) b " +
				" on ( " +
				" a.code = b.code and a.pk_accountingbook = b.pk_accountingbook and a.iscontrasted = b.iscontrasted )" ;


		ArrayList<ContrastInitSubVO> contrastInitsubVOs = new ArrayList<ContrastInitSubVO> ();
		List<Object[]> weiresult = (List<Object[]>)new BaseDAO().executeQuery(sql, new ArrayListProcessor());
		if(null!= weiresult && weiresult.size()>0){
			for (Iterator<Object[]> iterator = weiresult.iterator(); iterator.hasNext();) {
				Object[] objects = (Object[]) iterator.next();
				ContrastInitSubVO  initSubVo = new ContrastInitSubVO();

				initSubVo.setPk_accountingbook((String)objects[2]);
				initSubVo.setPk_accasoa((String)objects[1]);
				initSubVo.setPk_contrastrule((String)objects[0]);
				
//				UFDate beginDate = ruleDateMap.get(initSubVo.getPk_contrastrule());
				initSubVo.setPk_customer((String)objects[6]);
				if("Y".equals(((String)objects[7]))){
					initSubVo.setDirection("Y");
				}else {
					initSubVo.setDirection("N");
				}
				initSubVo.setQuantity(getUFDouble(objects[8]));//数量
				initSubVo.setAmount(getUFDouble(objects[10]));//原币
				initSubVo.setLocamount(getUFDouble(objects[11]));//本币
				initSubVo.setGroupamount(getUFDouble(objects[12]));
				initSubVo.setGlobalamount(getUFDouble(objects[13]));
				initSubVo.setVoucherdate(new UFDate((String)objects[3]));//凭证日期
				if(objects[5] != null) {
					if(objects[5] instanceof Integer) {
						initSubVo.setVoucherno((Integer)objects[5]);//凭证号
					} 
				}
				initSubVo.setPk_vouchertype((String)objects[4]);//凭证类别
				initSubVo.setIstransfered(UFBoolean.TRUE);
				initSubVo.setPk_currtype((String)objects[9]);//设置币种
				initSubVo.setIscontrasted(UFBoolean.FALSE);
				Object obj = objects[14];
				if(obj!=null)
					initSubVo.setDetailno(Integer.parseInt(String.valueOf(obj)));
				
				Object explanation = objects[15];
				if(explanation != null) {
					initSubVo.setExplanation((String)explanation);
				}
				initSubVo.setBusireconno((String)objects[16]);//协同号
				initSubVo.setPk_detail((String)objects[17]);//只有结转下年时才会用到的子表pk_detail列
				initSubVo.setYear(String.valueOf(Integer.parseInt(year)+1));
				initSubVo.setPeriod("00");
				initSubVo.setStatus(VOStatus.NEW);
				contrastInitsubVOs.add(initSubVo);
			}
		}

		sql = "delete  from "+tblName+" ";
		new BaseDAO().executeUpdate(sql);
		paramList=new ArrayList<SQLParameter>();		

		explianRuleVos = explianRuleVos(balanceRules, Arrays.asList(accbooks));
		for (Iterator<TransferNextYearInfoVO> iterator = explianRuleVos.iterator(); iterator.hasNext();) {
			TransferNextYearInfoVO transferNextYearInfoVO = (TransferNextYearInfoVO) iterator.next();
			SQLParameter sqlParam=new SQLParameter();
			sqlParam.addParam(transferNextYearInfoVO.getPk_contrasrrule());
			sqlParam.addParam(transferNextYearInfoVO.getPk_accasoa());
			sqlParam.addParam(transferNextYearInfoVO.getPk_setofbook());
			sqlParam.addParam(transferNextYearInfoVO.getPk_accountingbook());
			sqlParam.addParam(transferNextYearInfoVO.getPk_otheraccbook());
			sqlParam.addParam("Y");
			paramList.add(sqlParam);
		}
		execBatchSql(insertSb.toString(),paramList);
		//查询已对付数据
		sql = "  select a.pk_contrastrule ,a.pk_accasoa ,a.pk_accountingbook ,b.assid," +
				" b.orient ,sum (case when debitamount =0 then creditquantity else debitquantity end ) quantity," +
				" b.pk_currtype,sum(case when debitamount =0 then creditamount else debitamount end) amount," +
				" sum(case when debitamount =0 then localcreditamount else localdebitamount end) localamount," +
				" sum(case when debitamount =0 then groupcreditamount else groupdebitamount end) groupamount," +
				" sum(case when debitamount =0 then globalcreditamount else globaldebitamount end) globalamount " +
//				" b.pk_vouchertypev ,b.voucherno,b.busireconno "+
				" from ( select tmp.pk_contrastrule,tmp.pk_accasoa,tmp.pk_setofbook,tmp.pk_accountingbook,tmp.pk_otheraccbook,tmp.iscontrasted " +
				" from " +
				" "+tblName+" tmp " +
				" )  a  " +
				" inner join " +
				" (select data.pk_contrastrule,data.voucherno,data.prepareddatev,data.pk_accasoa,data.pk_accountingbook,data.pk_currtype,data.pk_detail,data.pk_setofbook," +
				" data.pk_vouchertypev,data.groupdebitamount," +
				" data.iscontrasted,data.isinit,data.localcreditamount,data.localdebitamount," +
				" data.periodv,data.yearv ,data.assid,data.creditamount,data.creditquantity,data.debitamount,data.debitquantity,data.detailno," +
				" data.discardflag,data.globalcreditamount,data.globaldebitamount,data.groupcreditamount,  case when data.debitamount = 0 " +
				"  then 'N' else 'Y' end   orient  " +
				" from  gl_contrastdata data  where  " +
				" data.yearv = '"+year+"' and data.pk_group = '"+pk_group+"' and data.DISCARDFLAG='N' and (data.PREPAREDDATEV >='"+allRules.get(0).getStartdate().toString()+"' or data.isinit='Y')) b " +
				" on ( " +
				" a.pk_accountingbook = b.pk_accountingbook and a.iscontrasted = b.iscontrasted and " +
				" a.pk_contrastrule = b.pk_contrastrule and a.pk_accasoa = b.pk_accasoa )" +
				" group by a.pk_contrastrule ,a.pk_accasoa ,a.pk_accountingbook ,b.assid,b.pk_currtype,b.orient ";
		List<Object[]> contrastedResult = (List<Object[]>)new BaseDAO().executeQuery(sql, new ArrayListProcessor());
		if(null!= contrastedResult && contrastedResult.size()>0){
			for (Iterator<Object[]> iterator = contrastedResult.iterator(); iterator.hasNext();) {
				Object[] objects = (Object[]) iterator.next();
				ContrastInitSubVO  initSubVo = new ContrastInitSubVO();
				initSubVo.setPk_accountingbook((String)objects[2]);
				initSubVo.setPk_accasoa((String)objects[1]);
				initSubVo.setPk_contrastrule((String)objects[0]);
				
				UFDate beginDate = ruleDateMap.get(initSubVo.getPk_contrastrule());
				
				initSubVo.setPk_customer((String)objects[3]);
				if("Y".equals(((String)objects[4]))){
					initSubVo.setDirection("Y");
				}else {
					initSubVo.setDirection("N");
				}
				initSubVo.setQuantity(getUFDouble(objects[5]));//数量
				initSubVo.setAmount(getUFDouble(objects[7]));//原币
				initSubVo.setLocamount(getUFDouble(objects[8]));//本币
				initSubVo.setGroupamount(getUFDouble(objects[9]));
				initSubVo.setGlobalamount(getUFDouble(objects[10]));
				initSubVo.setVoucherdate(beginDate);//凭证日期
//				initSubVo.setVoucherno(Integer.parseInt((String)objects[5]));//凭证号
//				initSubVo.setPk_vouchertype((String)objects[4]);//凭证类别
				initSubVo.setIstransfered(UFBoolean.TRUE);
				initSubVo.setPk_currtype((String)objects[6]);//设置币种
				initSubVo.setIscontrasted(UFBoolean.TRUE);
//				initSubVo.setVoucherno(Integer.parseInt((String)objects[12]));//凭证号
//				initSubVo.setPk_vouchertype((String)objects[11]);//凭证类别
//				initSubVo.setBusireconno((String)objects[13]);//协同号
//				initSubVo.setDetailno(Integer.parseInt((String)objects[13]));
				initSubVo.setYear(String.valueOf(Integer.parseInt(year)+1));
				initSubVo.setPeriod("00");
				initSubVo.setStatus(VOStatus.NEW);
				contrastInitsubVOs.add(initSubVo);
			}
		}
		
		//对对账期初,分组按照财务核算账簿,科目,对账规则,是否已对账
		Map<String, List<ContrastInitSubVO>> map = new HashMap<String, List<ContrastInitSubVO>>();
		for (Iterator<ContrastInitSubVO> iterator = contrastInitsubVOs.iterator(); iterator.hasNext();) {
			ContrastInitSubVO contrastInitSubVO = (ContrastInitSubVO) iterator.next();
			String key = contrastInitSubVO.getPk_accountingbook()+	contrastInitSubVO.getPk_contrastrule()+	contrastInitSubVO.getPk_accasoa()+	contrastInitSubVO.getIscontrasted();

			if(map.containsKey(key)){
				List<ContrastInitSubVO> list = map.get(key);
				list.add(contrastInitSubVO);
			}else{
				List<ContrastInitSubVO> list = new ArrayList<ContrastInitSubVO>();
				list.add(contrastInitSubVO);
				map.put(key, list);
			}
		}
		Set<String> keySet = map.keySet();
		for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			List<ContrastInitSubVO> list = map.get(key);
			ContrastInitVO  initVO = new ContrastInitVO();
			initVO.setPk_accasoa(list.get(0).getPk_accasoa());
			initVO.setPk_accountingbook(list.get(0).getPk_accountingbook());
			initVO.setPk_contrastrule(list.get(0).getPk_contrastrule());
			initVO.setIscontrasted(list.get(0).getIscontrasted());
			initVO.setStatus(VOStatus.NEW);
			initVO.setSyear(String.valueOf(Integer.parseInt(year)+1));
			initVO.setSperiod("00");
			initVO.setIstransfered(UFBoolean.TRUE);
			initVO.setContrastinitsub(list.toArray(new ContrastInitSubVO[0]));
			ContrastRuleVO contrastRuleVO = map_rule.get(list.get(0).getPk_contrastrule());
			initVO.setStartdate(new UFDateTime(ruleDateMap.get(contrastRuleVO.getPk_contrastrule()).toString()));
			initVO.setPk_group(pk_group);
			// FIXME:批量保存
			manageInitVo(initVO,false,true);
		}
		
		//因为目前没有存储已经结转的表示，暂时处理成，结转后在contrastdata表中存储一条临时数据（期初作废数据DISCARDFLAG=‘Y’ and isinit=‘Y’），包括规则+本方核算账簿+年度
		ContrastDataVO trasferDataVo = getTrasferDataVo(pk_contrastrules[0],accbooks[0],String.valueOf(Integer.parseInt(year)+1),pk_group);
		
		new BaseDAO().insertVO(trasferDataVo);
		
		return pk_contrastrules;
	}
	
	private ContrastDataVO getTrasferDataVo(String pk_contrastrule,String pk_accountingbook,String year,String pk_group) {
		
		ContrastDataVO dataVo = new ContrastDataVO();
		dataVo.setPk_contrastrule(pk_contrastrule);
		dataVo.setPk_accountingbook(pk_accountingbook);
		dataVo.setYearv(year);
		dataVo.setIsinit(UFBoolean.TRUE);
		dataVo.setDiscardflag(UFBoolean.TRUE);
		dataVo.setPk_group(pk_group);
		return dataVo;
	} 

	private UFDouble getUFDouble(Object obj) {
		if(obj == null) {
			return UFDouble.ZERO_DBL;
		}
		
		if(obj instanceof BigDecimal) {
			return new UFDouble((BigDecimal)obj);
		}
		
		if(obj instanceof Integer) {
			return new UFDouble((Integer)obj);
		}
		
		if(obj instanceof String) {
			return new UFDouble(obj.toString());
		}
		return UFDouble.ZERO_DBL;
	}

	protected ArrayList<TransferNextYearInfoVO>  explianRuleVos(ArrayList<ContrastRuleVO> allRules, List<String> allaccbooks) {
		ArrayList<TransferNextYearInfoVO> resultinfovos = new ArrayList<TransferNextYearInfoVO>();

		for (Iterator<ContrastRuleVO> iterator = allRules.iterator(); iterator.hasNext();) {
			ContrastRuleVO contrastRuleVO = (ContrastRuleVO) iterator.next();

			ArrayList<TransferNextYearInfoVO> tmpInfoVos = new ArrayList<TransferNextYearInfoVO>();

			/**
			 * 这里要进行科目表的转换
			 */
			Set<String> assoas = new HashSet<String>();
			ContrastRuleSubjVO[] subjvos = contrastRuleVO.getSubjvos();
			for (int i = 0; i < subjvos.length; i++) {
				ContrastRuleSubjVO contrastRuleSubjVO = subjvos[i];
				assoas.add(contrastRuleSubjVO.getCode());
			}
			assoas = this.getAccasoas(allaccbooks.get(0), assoas.toArray(new String[0]) , new UFDate().toString());
			ContrastRuleAreaVO[] areaVos = contrastRuleVO.getAreaVos();
			List<String> accbooks = new ArrayList<String>();
			if(null!=areaVos && areaVos.length>0){
				//得到适用范围的财务核算账簿
				for (int i = 0; i < areaVos.length; i++) {
					ContrastRuleAreaVO contrastRuleAreaVO = areaVos[i];
					accbooks.add(contrastRuleAreaVO.getPk_org());
				}
			}else{
				//取得gl_ContrastData里的所有财务核算账簿
				accbooks = allaccbooks;
			}
			String[] assoaArrays = assoas.toArray(new String[0]);
			String[] accbooksArrays = accbooks.toArray(new String[0]);
			for(int i =0 ;i<assoaArrays.length;i++){
				for (int j = 0; j < accbooksArrays.length; j++) {
					TransferNextYearInfoVO  infovo = new TransferNextYearInfoVO();
					infovo.setPk_contrasrrule(contrastRuleVO.getPk_contrastrule());
					infovo.setPk_setofbook(contrastRuleVO.getPk_book());
					infovo.setPk_accasoa(assoaArrays[i]);
					infovo.setPk_accountingbook(accbooksArrays[j]);
					infovo.setPk_otheraccbook(accbooksArrays[j]);
//					infovo.setIscontrasted("N");
					tmpInfoVos.add(infovo);
				}
			}
			resultinfovos.addAll(tmpInfoVos);
		}

		return resultinfovos;
	}
	public  Set<String> getAccasoas(String pk_accountingBook,String codess[],String stddate){
		Set<String> assoas = new HashSet<String>();
//			if(true){
				String[] codes=codess;
				try {
//					String pk_org = AccountBookUtil.getPk_orgByAccountBookPk(pk_accountingBook);
					IBDData[] bdDatas = nc.vo.gateway60.itfs.AccountUtilGL.getEndDocByCodesVersion(pk_accountingBook,codes,stddate);
//					AccountVO[] accasoas=nc.vo.gateway60.itfs.AccountUtilGL.queryAccountVosByCodes(pk_accountingBook, codes, stddate);
//					/**
//					 * 修改为对科目多版本支持的缓存
//					 */
//					AccountVO[] accasoas= null;
//					ArrayList<AccountVO> accountList = new ArrayList<AccountVO>();
//					for (int  i = 0; codes!=null&& i < codes.length;  i++) {
//						accountList.add(AccountCache.getInstance().getAccountVOByCode(pk_accountingBook, codes[i]));
//					}
//					accasoas= accountList.toArray(new AccountVO[0]);
//					if(null!=accasoas && accasoas.length>0){
//				    	ArrayList<String> tlist=new ArrayList<String>();
//				    	for(AccountVO avo:accasoas){
//				    		tlist.add(avo.getPk_accasoa());
//				    	}
//				    	return tlist;
//				    }
					if(bdDatas != null && bdDatas.length>0) {
						for (IBDData data : bdDatas) {
							String pk = data.getPk();
							assoas.add(pk);
						}
					}
				} catch (Exception e) {
					nc.bs.logging.Logger.error(e.getMessage(), e);
				}
//			}
		return assoas;
	}


	/**
	 * 创建一个临时表
	 */
	protected String createTempTable(String tableName,String columns,String indexs) throws BusinessException {
		String vtn = null;
		try {
			vtn = new TempTable().createTempTable(getConnection(),tableName, columns, indexs);
		} catch (SQLException e) {
			Logger.error(e.getMessage(), e);
			throw new BusinessException(e.getMessage());
		}
		return vtn;
	}

	protected static void execBatchSql(String sql, List<SQLParameter> list) throws BusinessException {
		PersistenceManager manager = null;
		try {
			manager = PersistenceManager.getInstance();
			JdbcSession session = manager.getJdbcSession();
			for (SQLParameter param : list) {
				session.addBatch(sql, param);
			}
			session.executeBatch();
		} catch (DbException e) {
			Logger.error(e.getMessage(), e);
			throw new BusinessException(e.getMessage());
		} finally {
			if (manager != null) {
				manager.release();
			}
		}
	}

	protected Connection getConnection() throws SQLException{
		return ConnectionFactory.getConnection();
	}

	protected JdbcPersistenceManager getPersistenceManager() {
		if(persistenceManager == null)
			try {
				persistenceManager = (JdbcPersistenceManager) JdbcPersistenceManager.getInstance();
			} catch (DbException e) {
				Logger.error(e.getMessage());
				nc.bs.logging.Logger.error(e.getMessage(), e);
			}
		return persistenceManager;
	}

	protected JdbcPersistenceManager persistenceManager = null;

	/*
	 *
	 * checkIsTransfer方法在ImpContrastInit中的实现
	 * @see nc.itf.gl.contrast.init.IContrastInit#checkIsTransfer(java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public String[] checkIsTransfer(String year, String[] pk_contrastrules, String pk_gourp) throws BusinessException {
		ArrayList<String> resultList = new ArrayList<String>();
		if(pk_contrastrules==null || pk_contrastrules.length>0 ) return resultList.toArray(new String[0]);
		String buildInSql = GLSqlUtil.buildInSql(ContrastInitVO.PK_CONTRASTRULE, pk_contrastrules);
		String  wherecondition = ContrastInitVO.PK_CONTRASTINIT+" in (select pk_contrastinit from gl_contrastinitsub  where yearinit='"+year+"' and istransfered ='Y')  and "+buildInSql;
		ContrastInitVO[] query = query(wherecondition);
		if(query!=null && query.length>0){
			for (int i = 0; i < query.length; i++) {
				ContrastInitVO contrastInitVO = query[i];
				resultList.add(contrastInitVO.getPk_contrastrule());

			}
		}
		return  resultList.toArray(new String[0]);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void cancleTransferToNextYear(String year, String[] pk_contrastrules,String[] accbooks , String pk_group) throws BusinessException {
		if(pk_contrastrules==null || pk_contrastrules.length==0) return ;
		
		Collection<ContrastRuleVO> ruleVOs = new BaseDAO().retrieveByClause(ContrastRuleVO.class, SqlTools.getInStr(ContrastRuleVO.PK_CONTRASTRULE, pk_contrastrules, true));
		
		//如果为规则启用年度则返回
		for (ContrastRuleVO contrastRuleVO : ruleVOs) {
			UFDate startdate = contrastRuleVO.getStartdate();
			if(startdate.getYear() == Integer.valueOf(year).intValue()) {
				return ;
			}
		}
		
		boolean check = checkContrastDataAndReport(pk_contrastrules,accbooks,year);
		
		if(!check) {
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("contrastprivate_0","02002002-0027")/*@res "已进行对账或生成正式报告，不允许取消结转"*/);
		}
		
		String[] lockKeys = new String[pk_contrastrules.length];
		for (int i = 0; i < pk_contrastrules.length; i++) {
			lockKeys[i]= "gl_contrasttransfertonextyear_"+ pk_contrastrules[i]+pk_group;
		}
		//结转加锁,动态锁
		boolean addBatchDynamicLock = PKLock.getInstance().addBatchDynamicLock(lockKeys);
		//FIXME:改成单个加锁,加锁成功继续执行结转?
		if(!addBatchDynamicLock){
			throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("contrastprivate_0","02002002-0015")/*@res "其他用户正在执行结转操作!\n请稍后再试!"*/);
		}
		String buildInSql = GLSqlUtil.buildInSql(ContrastInitVO.PK_CONTRASTRULE, pk_contrastrules);
		String sql  = " select pk_contrastinit from  gl_contrastinit  where syear ='"+String.valueOf(year)+"' and istransfered ='Y' and  pk_group = '"+pk_group+"'  and " + buildInSql;
		if(accbooks!=null && accbooks.length>0){
			sql = sql + " and "+GLSqlUtil.buildInSql(ContrastInitVO.PK_ACCOUNTINGBOOK, accbooks);
		}
		ArrayList<Object[]> executeQuery = (ArrayList<Object[]>) new BaseDAO().executeQuery(sql, new ArrayListProcessor());
//		ArrayList<String>  pk_contrastinit = new ArrayList<String>();
		if(executeQuery!=null && executeQuery.size()>0){
			for (Iterator<Object[]> iterator = executeQuery.iterator(); iterator.hasNext();) {
				Object[] objects = (Object[]) iterator.next();
//				pk_contrastinit.add((String)objects[0]);
				//FIXME:PILIANG
				delete((String)objects[0]);
			}
		}
		
		//因为现在结转标识是在contrastdata中存储一条作废的期初数据，取消结转时将该条数据删除
		
		StringBuffer delContrastDt = new StringBuffer();
		delContrastDt.append(" delete from gl_contrastdata where gl_contrastdata.YEARV=? and gl_contrastdata.PK_GROUP = ? ");
		delContrastDt.append(" and "+GLSqlUtil.buildInSql(ContrastDataVO.PK_CONTRASTRULE, pk_contrastrules));
		delContrastDt.append(" and "+ContrastDataVO.ISINIT+"='"+UFBoolean.TRUE.toString()+"'");
		delContrastDt.append(" and "+ContrastDataVO.DISCARDFLAG+"='"+UFBoolean.TRUE.toString()+"'");
		if(accbooks!=null && accbooks.length>0){
			delContrastDt.append(" and "+GLSqlUtil.buildInSql(ContrastDataVO.PK_ACCOUNTINGBOOK, accbooks));
		}
		SQLParameter param = new SQLParameter();
		param.addParam(year);
		param.addParam(pk_group);
		//将结转标识数据删除
		new BaseDAO().executeUpdate(delContrastDt.toString(), param);
	}

	@Override
	public String getMaxYearTransferToNext(String[] pk_acctingbooks,
			String pk_contrastrule,String pk_group) throws BusinessException {
		if(StringUtils.isEmpty(pk_contrastrule) || pk_acctingbooks == null || pk_acctingbooks.length==0) {
			return null;
		}
		
		ContrastRuleVO ruleVo = NCLocator.getInstance().lookup(IContrastRule.class).findByPrimaryKey(pk_contrastrule);
		
		StringBuffer sqlBuff = new StringBuffer();
		sqlBuff.append("select max(yearv) yearv from gl_contrastdata where ");
		sqlBuff.append(SqlTools.getInStr(ContrastDataVO.PK_ACCOUNTINGBOOK, pk_acctingbooks, true));
		sqlBuff.append(" and "+ContrastDataVO.PK_CONTRASTRULE+"= ? ");
		sqlBuff.append(" and "+ContrastDataVO.PK_GROUP+"=?");
		sqlBuff.append(" and "+ContrastDataVO.ISINIT+"=?");
		sqlBuff.append(" and "+ContrastDataVO.DISCARDFLAG+"=?");
		sqlBuff.append(" group by "+ContrastDataVO.PK_ACCOUNTINGBOOK);
		
		SQLParameter para = new SQLParameter();
		para.addParam(pk_contrastrule);
		para.addParam(pk_group);
		para.addParam(UFBoolean.TRUE.toString());
		para.addParam(UFBoolean.TRUE.toString());
		
		
		Object executeQuery = new BaseDAO().executeQuery(sqlBuff.toString(), para,new BeanListProcessor(ContrastDataVO.class));
		String lastYear = null;//取多个核算账簿中最小的结转年度
		if(executeQuery == null) {
//			return ruleVo.getStartdate().getYear()+"";
		}else {
			List list = (List) executeQuery;
			if(list.size()>0) {
				for (Object object : list) {
					ContrastDataVO initVo = (ContrastDataVO) object;
					String middleYear = initVo.getYearv();
					if(StringUtils.isEmpty(middleYear)) {
						middleYear = ruleVo.getStartdate().getYear()+"";
					}
					if(lastYear == null || Integer.valueOf(lastYear).intValue() > Integer.valueOf(middleYear).intValue()) {
						lastYear = middleYear;
					}
				}
			}else {
//				lastYear = ruleVo.getStartdate().getYear()+"";
			}
//			return lastYear;
		}
		//如果为空则取规则启用日期所在年
		if(StringUtils.isEmpty(lastYear)) {
			SetOfBookVO setOfBookVO = AccountBookUtil.getSetOfBookVOByPk_accountingBook(pk_acctingbooks[0]);
			AccountCalendar calendar = AccountCalendar.getInstanceByPeriodScheme(setOfBookVO.getPk_accperiodscheme());
			calendar.setDate(ruleVo.getStartdate());
			lastYear = calendar.getYearVO().getPeriodyear();
		}
		return lastYear;
	}
	
	public Map<String,String> getMaxTransferYear(String[] pk_contrastrules,String[] pk_accountingbooks,String pk_group) throws BusinessException {
		
		Map<String,String> trasferYearMap = new HashMap<String, String>();
		if(pk_contrastrules == null || pk_contrastrules.length==0 || pk_accountingbooks == null || pk_accountingbooks.length==0) {
			return trasferYearMap;
		}
		
		Collection<ContrastRuleVO> c = new BaseDAO().retrieveByClause(ContrastRuleVO.class, SqlTools.getInStr(ContrastRuleVO.PK_CONTRASTRULE, pk_contrastrules, true));
		
		Map<String,ContrastRuleVO> ruleMap = new HashMap<String, ContrastRuleVO>();
		
		if(c != null && c.size()>0) {
			for (ContrastRuleVO contrastRuleVO : c) {
				ruleMap.put(contrastRuleVO.getPk_contrastrule(), contrastRuleVO);
			}
		}
		
		StringBuffer sqlBuff = new StringBuffer();
		
		sqlBuff.append(" select max(yearv) "+ContrastDataVO.YEARV+","+ContrastDataVO.PK_ACCOUNTINGBOOK+","+ContrastDataVO.PK_CONTRASTRULE);
		sqlBuff.append(" from "+ContrastDataVO.getDefaultTableName());
		sqlBuff.append(" where 1=1 ");
		sqlBuff.append(" and "+SqlTools.getInStr(ContrastDataVO.PK_CONTRASTRULE, pk_contrastrules, true));
		sqlBuff.append(" and "+SqlTools.getInStr(ContrastDataVO.PK_ACCOUNTINGBOOK, pk_accountingbooks, true));
		sqlBuff.append(" and "+ContrastDataVO.ISINIT+"=?");
		sqlBuff.append(" and "+ContrastDataVO.DISCARDFLAG+"=?");
		sqlBuff.append(" group by "+ContrastDataVO.PK_CONTRASTRULE+","+ContrastDataVO.PK_ACCOUNTINGBOOK);
		
		SQLParameter param = new SQLParameter();
		param.addParam(UFBoolean.TRUE.toString());
		param.addParam(UFBoolean.TRUE.toString());
		
		Object executeQuery = new BaseDAO().executeQuery(sqlBuff.toString(), param, new BeanListProcessor(ContrastDataVO.class));
		
		if(executeQuery != null) {
			List list = (List) executeQuery;
			if(list.size()>0) {
				for (Object obj : list) {
					ContrastDataVO dataVo = (ContrastDataVO) obj;
					String pk_contrastrule = dataVo.getPk_contrastrule();
					String pk_accountingbook = dataVo.getPk_accountingbook();
					if(!StringUtils.isEmpty(pk_contrastrule) && !StringUtils.isEmpty(pk_accountingbook)) {
						String key = pk_contrastrule+"_"+pk_accountingbook;
						trasferYearMap.put(key, dataVo.getYearv());
					}
				}
			}
		}
		//处理第一年启用情况，如果没有结转说明为第一年启用，返回规则启用
		for (String pk_contrastrule : pk_contrastrules) {
			for (String pk_accountingbook : pk_accountingbooks) {
				String key = pk_contrastrule+"_"+pk_accountingbook;
				if(!trasferYearMap.containsKey(key)) {
					if(ruleMap.containsKey(pk_contrastrule)) {
						ContrastRuleVO ruleVo = ruleMap.get(pk_contrastrule);
						SetOfBookVO setOfBookVo = AccountBookUtil.getSetOfBookVOByPk_accountingBook(pk_accountingbook);
						AccountCalendar calendar = AccountCalendar.getInstanceByPeriodScheme(setOfBookVo.getPk_accperiodscheme());
						calendar.setDate(ruleVo.getStartdate());
						AccperiodVO yearVO = calendar.getYearVO();
						String year = yearVO.getPeriodyear();
						trasferYearMap.put(key, year);
					}
				}
			}
		}
		
		return trasferYearMap;
	}
	
	public boolean checkContrastDataAndReport(String[] pk_contrastrules,String[] pk_accountingbooks,String year) throws BusinessException {
		if(pk_contrastrules == null || pk_contrastrules.length==0 || pk_accountingbooks==null || pk_accountingbooks.length==0 || StringUtils.isEmpty(year)) {
			return false;
		}
		
		//首先检查是否有对账数据
		StringBuffer sqlData = new StringBuffer();
		sqlData.append(ContrastDataVO.YEARV+"='"+year+"'");
		sqlData.append(" and ("+ContrastDataVO.ISINIT+"='N')");
		sqlData.append(" and "+SqlTools.getInStr(ContrastDataVO.PK_CONTRASTRULE, pk_contrastrules, true));
		sqlData.append(" and ("+SqlTools.getInStr(ContrastDataVO.PK_ACCOUNTINGBOOK, pk_accountingbooks, true));
		sqlData.append(" or "+SqlTools.getInStr(ContrastDataVO.PK_OTHERORGBOOK, pk_accountingbooks, true)+")");
		sqlData.append(" and "+ContrastDataVO.ISCONTRASTED+"='Y'");
		
		Collection<ContrastDataVO> dataC = new BaseDAO().retrieveByClause(ContrastDataVO.class, sqlData.toString());
		
		//如果查询出非期初的已对付数据则检查不通过
		
		if(dataC != null && dataC.size()>0) {
			return false;
		}
		
		StringBuffer sqlReport = new StringBuffer();
		sqlReport.append(ContrastReportVO.SYEAR+"='"+year+"'");
		sqlReport.append(" and "+ContrastReportVO.ISBUILDED+"="+ContrastReportStatusConst.FORMCREATED_CODE);
		
		sqlReport.append(" and "+SqlTools.getInStr(ContrastReportVO.PK_CONTRASTRULE, pk_contrastrules, true));
		
		sqlReport.append(" and ("+SqlTools.getInStr(ContrastReportVO.PK_ACCOUNTINGBOOK, pk_accountingbooks, true));
		sqlReport.append(" or "+SqlTools.getInStr(ContrastReportVO.OTHERACCOUNTBOOK, pk_accountingbooks, true)+")");
		
		Collection<ContrastReportVO> reportC = new BaseDAO().retrieveByClause(ContrastReportVO.class, sqlReport.toString());
		//如果存在已正式生成的对账报告则检查不通过
		if(reportC != null && reportC.size()>0) {
			return false;
		}
		return true;
	}
	@Override
	public boolean checkContrastRuleDelete(String[] pk_contrastrules)
			throws BusinessException {
		if(pk_contrastrules == null || pk_contrastrules.length==0) {
			return false;
		}
		
		//校验是否有期初数据
		StringBuffer initSql = new StringBuffer();
		
		initSql.append(SqlTools.getInStr(ContrastInitVO.PK_CONTRASTRULE, pk_contrastrules, true));
		
		Collection<ContrastInitVO> initC = new BaseDAO().retrieveByClause(ContrastInitVO.class, initSql.toString());
		
		if(initC != null && initC.size()>0) {
			return false;
		}
		
		//校验是否有对账数据
		StringBuffer contrastSql = new StringBuffer();
		
		contrastSql.append(SqlTools.getInStr(ContrastDataVO.PK_CONTRASTRULE, pk_contrastrules, true));
		contrastSql.append(" and "+ContrastDataVO.ISCONTRASTED+"='Y' ");
		
		Collection<ContrastDataVO> contrastC = new BaseDAO().retrieveByClause(ContrastDataVO.class, contrastSql.toString());
		
		if(contrastC != null && contrastC.size()>0) {
			return false;
		}
		
		//校验是否有正式报告
		StringBuffer reportSql = new StringBuffer();
		
		reportSql.append(SqlTools.getInStr(ContrastReportVO.PK_CONTRASTRULE, pk_contrastrules, true));
		reportSql.append(" and "+ContrastReportVO.ISBUILDED+"="+ContrastReportStatusConst.FORMCREATED_CODE);
		
		Collection<ContrastReportVO> reportC = new BaseDAO().retrieveByClause(ContrastReportVO.class, reportSql.toString());
		
		if(reportC != null && reportC.size()>0) {
			return false;
		}
		
		return true;
	}
	@Override
	public boolean checkIsTransferNextYear(String pk_contrastrule,String[] pk_accountingbooks
			, String year,String pk_group) throws BusinessException {
		
		Map<String, String> yearMap = this.getMaxTransferYear(new String[]{pk_contrastrule}, pk_accountingbooks, pk_group);
		
		Integer newYear = 0;
		try{
			newYear = Integer.valueOf(year).intValue();
		}catch(Exception e) {
			Logger.error(e);
			return false;
		}
		
		for (String pk_accountingbook : pk_accountingbooks) {
			String keySelf = pk_contrastrule+"_"+pk_accountingbook;
			String transferYearSelf = yearMap.get(keySelf);
			
			//下年已经结转
			if(Integer.valueOf(transferYearSelf).intValue()>newYear) {
				return true;
			}
		}
		
		return false;
	}
	@Override
	public boolean checkCanInitData(String pk_contrastrule,
			String pk_accountingbook, String year, String pk_group)
			throws BusinessException {
		boolean checkIsTransferNextYear = this.checkIsTransferNextYear(pk_contrastrule, new String[] {pk_accountingbook}, year, pk_group);
		
		if(checkIsTransferNextYear) {
			return false;
		}
		
		boolean checkReportBuild = NCLocator.getInstance().lookup(IContrastReport.class).checkReportBuild(pk_contrastrule, pk_accountingbook, null, year, null);
		
		return !checkReportBuild;
	}
	@Override
	public boolean checkIsTransferDataToNextYear(String pk_accountingbook, String syear)
			throws BusinessException {
		
        StringBuffer sqlBuff = new StringBuffer();		
        
        sqlBuff.append(" select * from "+ContrastDataVO.getDefaultTableName());
        sqlBuff.append(" a where ");
        sqlBuff.append(" a.ISCONTRASTED='Y' ");
        sqlBuff.append(" and a.YEARV='"+syear+"'");
        sqlBuff.append(" and a.pk_accountingbook='"+pk_accountingbook+"'");
        sqlBuff.append(" and a.DISCARDFLAG='N' ");
        sqlBuff.append(" and not exists (");
        
        sqlBuff.append(" select 1 from GL_CONTRASTDATA b where b.ISINIT='Y' and b.DISCARDFLAG='Y' ");
        sqlBuff.append(" and a.PK_ACCOUNTINGBOOK=b.PK_ACCOUNTINGBOOK ");
        sqlBuff.append(" and b.YEARV='"+(Integer.valueOf(syear).intValue()+1)+"' ");
        
        sqlBuff.append(" ) ");
        
        Object executeQuery = new BaseDAO().executeQuery(sqlBuff.toString(),new BeanListProcessor(ContrastDataVO.class));
        
        if(executeQuery != null && executeQuery instanceof List) {
        	List list = (List) executeQuery;
        	if(list.size() >0) {
        		return false;
        	}
        }
		return true;
	}
	
	@Override
	public boolean checkHaveContrastedData(String pk_contrastrule,
			String pk_accountingbook, String syear, String pk_group)
			throws BusinessException {
		
		StringBuffer sqlBuff = new StringBuffer();
		
		sqlBuff.append(ContrastDataVO.PK_CONTRASTRULE+"='"+pk_contrastrule+"'");
		sqlBuff.append(" and ("+ContrastDataVO.PK_ACCOUNTINGBOOK+"='"+pk_accountingbook+"' or "+ContrastDataVO.PK_OTHERORGBOOK+"='"+pk_accountingbook+"' )");
		sqlBuff.append(" and "+ContrastDataVO.ISCONTRASTED+"='Y'");
		sqlBuff.append(" and "+ContrastDataVO.YEARV+"='"+syear+"'");
		
		Collection<ContrastDataVO> c = new BaseDAO().retrieveByClause(ContrastDataVO.class, sqlBuff.toString(), ContrastDataVO.PK_CONTRASTDATA);
		
		if(c == null || c.size()==0)
			return false;
		return true;
	}
	
	
	
}