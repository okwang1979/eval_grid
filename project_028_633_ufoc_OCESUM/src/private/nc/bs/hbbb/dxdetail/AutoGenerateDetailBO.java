package nc.bs.hbbb.dxdetail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.hbbb.contrast.ContrastBO;
import nc.bs.hbbb.contrast.ContrastFuncBO;
import nc.bs.hbbb.org.util.HbOrgUtilBO;
import nc.bs.logging.Logger;
import nc.bs.uif2.LockFailedException;
import nc.impl.hbbb.vouch.VouchMngSrvImpl;
import nc.itf.hbbb.contrast.IntrMeasProjectCache;
import nc.itf.hbbb.dxrelation.IDxFunctionConst;
import nc.itf.hbbb.hbrepstru.HBRepStruUtil;
import nc.itf.hbbb.vouch.IVouchQrySrv;
import nc.itf.hbbb.vouch.constants.IVouchType;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.BaseProcessor;
import nc.pub.hbbb.exception.UFOCUnThrowableException;
import nc.util.hbbb.HBAloneIDUtil;
import nc.util.hbbb.NumberFormatUtil;
import nc.util.hbbb.hbscheme.HBSchemeSrvUtils;
import nc.util.hbbb.param.HBBBParamUtil;
import nc.util.hbbb.service.HBBaseDocItfService;
import nc.util.hbbb.ucheck.HBUcheckUtil;
import nc.vo.bd.account.AccountVO;
import nc.vo.gateway60.itfs.AccountUtilGL;
import nc.vo.gl.contrast.iufo.ContrastHBBBQryVO;
import nc.vo.gl.contrast.iufo.util.UCheckProxy;
import nc.vo.gl.contrast.report.statusconst.ContrastReportStatusConst;
import nc.vo.gl.contrast.rule.ContrastRuleVO;
import nc.vo.glcom.tools.GLContrastProxy;
import nc.vo.hbbb.contrast.ContrastQryVO;
import nc.vo.hbbb.dxrelation.DXContrastVO;
import nc.vo.hbbb.dxrelation.DXRelationBodyVO;
import nc.vo.hbbb.dxrelation.DXRelationHeadVO;
import nc.vo.hbbb.dxrelation.IDXRelaConst;
import nc.vo.hbbb.dxtype.DXTypeValue;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.hbbb.vouch.VouchBodyVO;
import nc.vo.hbbb.vouch.VouchHeadVO;
import nc.vo.hbbb.vouch.VouchVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;
import nc.vo.util.BDPKLockUtil;

public class AutoGenerateDetailBO {
	
	public static String LOCK_CREATEDXDETAIL_KEY ="UFOC_CREATEDXDETAIL";
	private ContrastQryVO qryvo;
	
	
	public AutoGenerateDetailBO(ContrastQryVO new_qryvo) throws BusinessException {
	    // 校验末级单位
        String pk_org = new_qryvo.getKeymap().get(KeyVO.CORP_PK);
        String pk_hbrepstru = new_qryvo.getPk_hbrepstru();
        boolean isLeaf = HBRepStruUtil.isLeafMember(pk_org, pk_hbrepstru);

        if (isLeaf) {
            throw new UFOCUnThrowableException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0",
                    "01830003-0035")/* @res "末级单位不能生成抵消分录!" */);
        }
	    
	    // 合并方案
        HBSchemeVO hbSchemeVO = HBSchemeSrvUtils.getHBSchemeByHBSchemeId(new_qryvo.getSchemevo().getPk_hbscheme());
        if (hbSchemeVO == null) {
            throw new UFOCUnThrowableException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0278")/* @ 请选择合并方案! */);
        }
        new_qryvo.setSchemevo(hbSchemeVO);
        // 填充抵销模板
		DXContrastVO[] dxmodels = new_qryvo.getDxmodels();
		for (DXContrastVO dxmodel : dxmodels) {
			DXRelationBodyVO[] bodyvos = ((nc.itf.hbbb.dxrelation.IDXRelationQrySrv) NCLocator.getInstance().lookup(nc.itf.hbbb.dxrelation.IDXRelationQrySrv.class.getName())).queryDXFormulas(dxmodel
					.getHeadvo().getPk_dxrela_head());
			dxmodel.setBodyvos(bodyvos);
		}
		this.setQryvo(new_qryvo);
	}
	
	public VouchVO[] doAutoGenerate() throws BusinessException {
		VouchVO[] result = null;
		ArrayList<VouchVO> list = new ArrayList<VouchVO>();
		try {
			String pk_hbrepstru = qryvo.getPk_hbrepstru();
			String pk_contrastorg = qryvo.getContrastorg();
			if (null != pk_hbrepstru && pk_hbrepstru.trim().length() > 0 && null != pk_contrastorg && pk_contrastorg.trim().length() > 0) {
				DXContrastVO[] dxvos = qryvo.getDxmodels();
				// 首先清理下历史已自动生成的分录
				ArrayList<String> lockKeys = new ArrayList<String>();
				String dxDetail_aloneid = HBAloneIDUtil.getAdjustVoucherAlone_id(qryvo, true);
				// 预先设置内部交易对账规则
				for (DXContrastVO vo : dxvos) {
					if(vo.getHeadvo().getPk_contrastrule() != null)
						vo.setContrastRuleVo(GLContrastProxy.getRemoteContrastRule().findByPrimaryKey(vo.getHeadvo().getPk_contrastrule()));
				}
				for (DXContrastVO vo : dxvos) {
					lockKeys.add(dxDetail_aloneid + vo.getHeadvo().getPk_dxrela_head());
				}
				// 动态锁
				BDPKLockUtil.lockString(lockKeys.toArray(new String[0]));
				// 存储最后要执行的cesum函数列表
				ArrayList<DXContrastVO> finalExcuteCesumlst = new ArrayList<DXContrastVO>();
				// 然后开始根据各个抵销模板来依次生成抵销分录
				for (DXContrastVO vo : dxvos) {
					// 删除已生成的分录记录
					VoucherBO.clearContrastedData(vo, qryvo);
					// 判断分录是否含有CESUM、ESELECT函数,不能和权益类函数混用
					boolean iscontain = false;
					DXRelationBodyVO[] dxbodyvos = vo.getBodyvos();
					if (!vo.getHeadvo().getType().equals(IDXRelaConst.DIRECT_UAP_RULE) && !vo.getHeadvo().getType().equals(IDXRelaConst.INDIRECT_UAP_RULE)) {
						for (int i = 0; i < dxbodyvos.length; i++) {
							DXRelationBodyVO dxRelationBodyVO = dxbodyvos[i];
							if (dxRelationBodyVO.getType().intValue() == IDXRelaConst.DIFF)
								continue;
							//央客：王志强  添加OESUM函数
							if ((dxRelationBodyVO.getFormula() != null && dxRelationBodyVO
									.getFormula().startsWith(
											IDxFunctionConst.CESUM))
									|| (dxRelationBodyVO.getFormula() != null && dxRelationBodyVO
											.getFormula().startsWith(
													IDxFunctionConst.ESELECT))
									||(dxRelationBodyVO.getFormula() != null && dxRelationBodyVO
											.getFormula().startsWith(
													"OESUM"))) {
								iscontain = true;
								break;
							}
						}
					}
					if (iscontain) {
						finalExcuteCesumlst.add(vo);
						continue;
					} else {
						//执行非cesum函数的自动生成抵销分录
						doAutoDetailWithOutCesum(list, vo);
					}
				}
				//执行CESUM
				excuteCeSumContrast(list, pk_contrastorg, finalExcuteCesumlst);
				if (list.size() > 0) {
					result = new VouchVO[list.size()];
					list.toArray(result);
				}
			}
			ArrayList<VouchVO> resultvouchers = new ArrayList<VouchVO>();
			AggregatedValueObject[] vouvherVOsByHbParameter = HBBBParamUtil.getVouvherVOsByHbParameter(result);
			if (vouvherVOsByHbParameter != null && vouvherVOsByHbParameter.length > 0) {
				for (int i = 0; i < vouvherVOsByHbParameter.length; i++) {
					VouchVO aggregatedValueObject = (VouchVO) vouvherVOsByHbParameter[i];
					resultvouchers.add(aggregatedValueObject);
				}
			}
			result = resultvouchers.toArray(new VouchVO[0]);
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			if (e instanceof LockFailedException) {
				throw new UFOCUnThrowableException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830003-0043")/* @res "其他用户正在执行该单位的自动生成抵消分录,请稍后再试!" */, e);
			} else if(e instanceof UFOCUnThrowableException) {
				throw new UFOCUnThrowableException(e.getMessage(), e);
			}else {
				throw new BusinessException(e.getMessage(), e);
			}
		}
		if(result != null && result.length>0) {
			List<String> pk_list = new ArrayList<String>();
			for(VouchVO vouch : result) {
				VouchHeadVO headvo = (VouchHeadVO)vouch.getParentVO();
				pk_list.add(headvo.getPk_vouchhead());
			}
			result = NCLocator.getInstance().lookup(IVouchQrySrv.class).qryVouchWithHBPrjNameByPKs(pk_list.toArray(new String[pk_list.size()]));
		}
		return result;
	}

	/**
	 * 其他类函数模板生成分录（除cesum之外）
	 * @param list
	 * @param vo
	 * @throws BusinessException
	 */
	private void doAutoDetailWithOutCesum(ArrayList<VouchVO> list,DXContrastVO vo) throws BusinessException {
		// 若抵销模板为直接总帐内部交易对帐类型，则直接根据模板去生成凭证
		if ((vo.getHeadvo()).getType().equals(IDXRelaConst.DIRECT_UAP_RULE)) {
			VouchVO vouchervo = new VouchVO();
			VouchHeadVO headvo = this.getHeadVO(vo);
			vouchervo.setParentVO(headvo);
			DXRelationBodyVO[] dxbodys = vo.getBodyvos();
			ArrayList<VouchBodyVO> dxbodylist = new ArrayList<VouchBodyVO>();
			if (null != dxbodys && dxbodys.length > 0) {
				for (DXRelationBodyVO dxbody : dxbodys) {
					try {
						genVoucherBody(dxbody, vo, dxbodylist);
					} catch (Exception e) {
						throw new BusinessException(e.getMessage());
					}

				}
			}
			if (null != dxbodylist && dxbodylist.size() > 0) {
				ArrayList<VouchBodyVO> resultBodyList = resetDetailnum(vo, dxbodylist.toArray(new VouchBodyVO[0]));
				vouchervo.setChildrenVO(resultBodyList.toArray(new VouchBodyVO[0]));
				isAllZero(list, vouchervo, resultBodyList);
			}
		} else {
			VouchVO vouchervo = new VouchVO();
			VouchHeadVO headvo = this.getHeadVO(vo);
			vouchervo.setParentVO(headvo);
			VouchBodyVO[] bodyvos = this.genBodyvos(vo);
			if (null != bodyvos && bodyvos.length > 0) {
				// 重新设置分录号
				ArrayList<VouchBodyVO> resultBodyList = resetDetailnum(vo, bodyvos);
				vouchervo.setChildrenVO(resultBodyList.toArray(new VouchBodyVO[0]));
				// 子表记录并非所有为0时，才保存
				isAllZero(list, vouchervo, resultBodyList);
			}
		}
	}

	/**
	 * cesum函数生成分录
	 * @param list
	 * @param pk_contrastorg
	 * @param finalExcute
	 * @throws BusinessException
	 */
	private void excuteCeSumContrast(ArrayList<VouchVO> list, String pk_contrastorg, ArrayList<DXContrastVO> finalExcute) throws BusinessException {
		qryvo.setMeaprojectcache(IntrMeasProjectCache.getSingleton().getInstance());//设置指标映射缓存			
		for (Iterator<DXContrastVO> iterator = finalExcute.iterator(); iterator.hasNext();) {
			DXContrastVO vo = (DXContrastVO) iterator.next();
			DXRelationBodyVO[] dxbodyvos = vo.getBodyvos();
			VouchHeadVO headvo = this.getHeadVO(vo);

			VouchVO vouchervo = new VouchVO();
			vouchervo.setParentVO(headvo);
			DXRelationBodyVO difsubvo = null;
			ArrayList<VouchBodyVO> dxbodylist = new ArrayList<VouchBodyVO>();
			for (DXRelationBodyVO subvo : dxbodyvos) {
				if (subvo.getType().intValue() == IDXRelaConst.DIFF) {
					difsubvo = subvo;
				} else {
					UFDouble genMeetdatasubvo = genMeetdatasubvo(subvo, true, vo);
					VouchBodyVO tmpbodyvo = new VouchBodyVO();
					if (subvo.getType().intValue() == IDXRelaConst.CREDIT) {
						tmpbodyvo.setCreditamount(genMeetdatasubvo);
					} else {
						tmpbodyvo.setDebitamount(genMeetdatasubvo);
					}
					tmpbodyvo.setPk_measure(subvo.getPk_measure());
					StringBuilder content = new StringBuilder();
					if(subvo.getDigest() != null)
						content.append(subvo.getDigest());
					
					tmpbodyvo.setDigest(content.toString());
					dxbodylist.add(tmpbodyvo);
				}
			}
			// 进行差额项目处理
			if (null != difsubvo) {
				VouchBodyVO subvo = this.genMeetdatasubvo(difsubvo, dxbodylist);
				if (null != subvo) {
					dxbodylist.add(subvo);
				}
			}
			if (null != dxbodylist && dxbodylist.size() > 0) {
				// 重新设置分录号
				ArrayList<VouchBodyVO> resultBodyList = resetDetailnum(vo, dxbodylist.toArray(new VouchBodyVO[0]));
				vouchervo.setChildrenVO(resultBodyList.toArray(new VouchBodyVO[0]));
				// 为了使CESUM能再执行中生效,只能没生成一张凭证就保存
				isAllZero(list, vouchervo, resultBodyList);
			}
		}
	}
	
	/**
	 * 生成的分录子表记录都为0，则不保存该分录
	 * @param list
	 * @param vouchervo
	 * @param resultBodyList
	 * @throws BusinessException
	 */
	private void isAllZero(ArrayList<VouchVO> list, VouchVO vouchervo,
			ArrayList<VouchBodyVO> resultBodyList) throws BusinessException {
		boolean bZero = true;
		for (VouchBodyVO subvo : resultBodyList) {
			if ((subvo.getCreditamount() != null && !subvo.getCreditamount().equals(UFDouble.ZERO_DBL ))|| (subvo.getDebitamount() != null && !subvo.getDebitamount().equals(UFDouble.ZERO_DBL))) {
				bZero = false;
				break;
			}
		}
		
		//如果所有都是0:不再生成抵销分录--modified by jiaah
		if(bZero == false){
			VouchMngSrvImpl impl = new VouchMngSrvImpl();
			VouchVO save = impl.save(vouchervo);
			list.add(save);
		}
	}

	/**
	 * 自动生成抵销分录详细与抵销模板详细的指标显示顺序一致
	 * @param vo
	 * @param bodyvos
	 * @return
	 */
	private ArrayList<VouchBodyVO> resetDetailnum(DXContrastVO vo, VouchBodyVO[] bodyvos) {
		DXRelationBodyVO[] dxbodys = vo.getBodyvos();
		Map<String, Integer> meapk_position_map = new HashMap<String, Integer>();
		for (DXRelationBodyVO dxvo:dxbodys) {
			meapk_position_map.put(dxvo.getPk_measure(), dxvo.getPosition());
		}
		ArrayList<VouchBodyVO> resultBodyList = new ArrayList<VouchBodyVO>();
		// 重新设置标题分录号
		for (int j = 0; j < bodyvos.length; j++) {
			bodyvos[j].setIorder(meapk_position_map.get(bodyvos[j].getPk_measure()));
			resultBodyList.add(bodyvos[j]);
		}
		return resultBodyList;
	}

	
	private UFDouble genMeetdatasubvo(DXRelationBodyVO subvo,boolean bself,DXContrastVO contrastVO) throws BusinessException {
		UFDouble data =UFDouble.ZERO_DBL;
		try {
			data = new UFDouble(ContrastFuncBO.callFunc(null,this.getQryvo(), bself,null,null, subvo, contrastVO,null));
			//四舍五入保留两位小数
			return new UFDouble(NumberFormatUtil.Number2(data.doubleValue()));
		} catch (Exception e) {
			throw new BusinessException(e.getMessage(), e);
		}
	}
	
	private VouchBodyVO genMeetdatasubvo(DXRelationBodyVO subvo,	ArrayList<VouchBodyVO> dxbodylist) {
		VouchBodyVO subvo1 = new VouchBodyVO();
		subvo1.setPk_measure(subvo.getPk_measure());
		VouchBodyVO[] subvos = dxbodylist.toArray(new VouchBodyVO[0]);
		double debit = 0;
		double credit = 0;
		double dif=0;
		for (VouchBodyVO vo : subvos) {
			if (vo.getDebitamount()==null ) {
				credit = credit + vo.getCreditamount().doubleValue();
			} else {
				debit = debit + vo.getDebitamount().doubleValue();
			}
		}
		dif=debit-credit;
		if(debit==credit){
//			return null;
		}
		if (dif > 0) {
			subvo1.setCreditamount(new UFDouble(dif));
		} else {
			subvo1.setDebitamount(new UFDouble(-dif));
		}

		return subvo1;
	}
	
	/**
	 * 总账内部交易对账生成分录
	 * @param dxbody
	 * @param contrastVO
	 * @param dxbodylist
	 * @throws Exception
	 */
	private void genVoucherBody(DXRelationBodyVO dxbody, DXContrastVO contrastVO, ArrayList<VouchBodyVO> dxbodylist) throws Exception {
		DXRelationHeadVO dxhead = contrastVO.getHeadvo();
		ContrastRuleVO rulevo = contrastVO.getContrastRuleVo();

		String pk_hbrepstru = this.getQryvo().getPk_hbrepstru();
		String pk_contrastorg = this.getQryvo().getContrastorg();
		if (null != pk_hbrepstru && pk_hbrepstru.trim().length() > 0 && null != pk_contrastorg && pk_contrastorg.trim().length() > 0) {
			//TODO:两次多余
			String innercode = new HbOrgUtilBO().getInnerCode(pk_hbrepstru, pk_contrastorg);
			if (null == innercode || innercode.trim().length() == 0) {
				throw new BusinessException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830001-0452")/* @res "当前对账公司没有找到inner code" */);
			}
			//TODO:两次多余
			String[] contrastorgs = ContrastBO.getContrastOrgs(pk_hbrepstru, innercode);
			if (null == contrastorgs || contrastorgs.length == 0) {
				return;
			}
			
			ContrastHBBBQryVO qryvo = null;
			if (rulevo.getContrastmoney().toString().equals(ContrastReportStatusConst.BALANCE_PK)) {
				// 设置余额取数类型,内部交易规则
				qryvo = new ContrastHBBBQryVO(rulevo, null, HBUcheckUtil.getBalance(dxhead));
			} else if (rulevo.getContrastmoney().toString().equals(ContrastReportStatusConst.OCCUR_PK)) {
				// 设置发生取数类型,内部交易规则
				qryvo = new ContrastHBBBQryVO(rulevo, HBUcheckUtil.getOccur(dxhead), null);
			}
			
			if(dxbody.getPk_accasoa() == null)
				return;
			AccountVO[] accounts = AccountUtilGL.queryByPks(new String[] {dxbody.getPk_accasoa()});
			if(accounts == null || accounts.length == 0)
				return;
			
			// 设置科目编码
			String subjcode = accounts[0].getCode();
			boolean bself = dxbody.getDatasource().intValue() == ContrastHBBBQryVO.DATASOURCE_SELF.intValue() ? true : false;
			qryvo.setAccountcode(subjcode);
			// 设置期间 还缺少支持半年会计期间类别
			qryvo = ContrastFuncBO.setPeriod(qryvo, this.getQryvo());
			// 设置取数类别
			qryvo.setContenttype(ContrastFuncBO.getContentType(dxhead));
			// 设置版本日期
//			qryvo.setStddate(WorkbenchEnvironment.getInstance().getBusiDate().toLocalString());
			qryvo.setStddate(new UFDate().toLocalString());

			qryvo.setDriect(true);
			UCheckProxy proxy = new UCheckProxy(qryvo, subjcode, bself,true,contrastorgs);
			
			Map<String, UFDouble> mapDouble = new HashMap<String, UFDouble>();
			mapDouble = proxy.getBatchExecValue();
			
			VouchBodyVO result = new VouchBodyVO();
			result.setPk_measure(dxbody.getPk_measure());
			result.setDigest(dxbody.getDigest());
			
			UFDouble value = UFDouble.ZERO_DBL;
			for (String str : contrastorgs) {
				//没有对应的核算账簿的时候mapDouble返回的为null -- jiaah
				UFDouble calcvalue = UFDouble.ZERO_DBL;
				if(mapDouble != null){
					calcvalue = mapDouble.get(str);
				}
				if (calcvalue != null && !calcvalue.equals(UFDouble.ZERO_DBL)) {
					value = value.add(calcvalue);
				}
			}
			
			if (dxbody.getType().intValue() == IDXRelaConst.CREDIT) {
				result.setCreditamount(value);
			} else {
				result.setDebitamount(value);
			}
			dxbodylist.add(result);
		}
	}

	
	/**
	 * 根据每个模板的生成分录的合计
	 * @param vo
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	private VouchBodyVO[] genBodyvos(final DXContrastVO vo) throws BusinessException {
		VouchBodyVO[] result = null;

		StringBuilder content = new StringBuilder();
		content.append("SELECT   SUM (CASE ");
		content.append("                 WHEN (sub.direction = '").append(IDXRelaConst.CREDIT).append("') ");
		content.append("                    THEN sub.adjust_amount ");
		content.append("                 ELSE 0 ");
		content.append("              END) credit,  ");
		content.append("         SUM (CASE ");
		content.append("                 WHEN (sub.direction = '").append(IDXRelaConst.DEBIT).append("') ");
		content.append("                    THEN sub.adjust_amount ");
		content.append("                 ELSE 0 ");
		content.append("              END) debit, ");
		content.append("         sub.pk_measure ");
		content.append("    FROM iufo_meetdata_head head INNER JOIN iufo_meetdata_body sub ");
		content.append("         ON head.pk_totalinfo = sub.details ");
		content.append("   WHERE head.ismeetable = 'Y' ");
		content.append("     AND head.pk_meetorg = ? ");
		content.append("     AND head.pk_hbscheme = ? ");
		content.append("     AND head.pk_dxrelation = ? ");
		content.append("     AND head.pk_keygroup = ? ");
		content.append("     AND head.alone_id = ? ");

		content.append("GROUP BY sub.pk_measure, ");
		content.append("         head.pk_meetorg, ");
		content.append("         head.pk_hbscheme, ");
		content.append("         head.pk_dxrelation,head.alone_id,head.pk_keygroup,sub.direction ");//有可能同一个模板上存在多个相同的指标，而借贷方向不同

		SQLParameter params = new SQLParameter();
		params.addParam(qryvo.getContrastorg());
		params.addParam(qryvo.getSchemevo().getPk_hbscheme());
		params.addParam(vo.getHeadvo().getPk_dxrela_head());
		params.addParam(qryvo.getSchemevo().getPk_keygroup());
		String aloneid = HBAloneIDUtil.getAdjustVoucherAlone_id(this.getQryvo(), true);
		params.addParam(aloneid);
		
		List<VouchBodyVO> list1 = (List<VouchBodyVO>) HBBaseDocItfService.getRemoteUAPQueryBS().executeQuery(content.toString(), params, new BaseProcessor() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object processResultSet(ResultSet rs) throws SQLException {
				List<VouchBodyVO> list = new ArrayList<VouchBodyVO>();
				while (rs.next()) {
					UFDouble newCreditamount = new UFDouble(rs.getDouble("credit"));
					UFDouble newDebitamount = new UFDouble(rs.getDouble("debit"));
					if (newCreditamount.equals(UFDouble.ZERO_DBL) && newDebitamount.equals(UFDouble.ZERO_DBL))
						continue;
					VouchBodyVO bodyvo = new VouchBodyVO();
					bodyvo.setCreditamount(newCreditamount.equals(UFDouble.ZERO_DBL) ? null : newCreditamount);
					bodyvo.setDebitamount(newDebitamount.equals(UFDouble.ZERO_DBL) ? null : newDebitamount);
					bodyvo.setPk_measure(rs.getString("pk_measure"));
					list.add(bodyvo);
				}
				return list;
			}
		});
		
		if (null != list1 && list1.size() > 0) {
			result = new VouchBodyVO[list1.size()];
			list1.toArray(result);
			
			//填充自动生成抵消分录的摘要 add by jiaah
			DXRelationBodyVO[] bodyVos = vo.getBodyvos();
			Map<String, String> map = new HashMap<String, String>();
			for(DXRelationBodyVO bodyVO : bodyVos){
				map.put(bodyVO.getPk_measure(), bodyVO.getDigest());
			}
			for(VouchBodyVO vouchBody : result){
				vouchBody.setDigest(map.get(vouchBody.getPk_measure()));
			}
		}
		return result;
	}

	private VouchHeadVO getHeadVO(DXContrastVO vo) {
		VouchHeadVO result = new VouchHeadVO();
		result.setPk_hbscheme(this.getQryvo().getSchemevo().getPk_hbscheme());
		result.setInput_date(new UFDate());
		result.setPk_dxrela(vo.getHeadvo().getPk_dxrela_head());
		result.setVouch_type(IVouchType.TYPE_AUTO_ENTRY);
		result.setPk_adjscheme(this.getQryvo().getSchemevo().getPk_adjustscheme());
		result.setPk_org(this.getQryvo().getContrastorg());
		result.setPk_keygroup(this.getQryvo().getSchemevo().getPk_keygroup());
		result.setAlone_id(HBAloneIDUtil.getAdjustVoucherAlone_id(this.getQryvo(), true));
		result.setPk_user(this.getQryvo().getPk_user());
		result.setPk_group(InvocationInfoProxy.getInstance().getGroupId());
		result.setPk_dxtype(getDxTypeByDxSchemeType(vo.getHeadvo().getType()));
		return result;
	}

	/**
	 * 根据模板类型获取对应的抵销分录类别
	 * @param type
	 * @return
	 */
	private String getDxTypeByDxSchemeType(String type) {
		if (IDXRelaConst.RIGHTANDRIGHT_INVEST.equals(type)
				|| IDXRelaConst.RIGHTANDRIGHT_UNINVEST.equals(type)
				|| IDXRelaConst.RIGHTANDRIGHT_ALLOWNERINVEST.equals(type)) {
			return DXTypeValue.QYTPYE_PK;
		}else if(IDXRelaConst.IUFO_CURRENT.equals(type)
				|| IDXRelaConst.INDIRECT_UAP_RULE.equals(type)
				|| IDXRelaConst.DIRECT_UAP_RULE.equals(type)){
			return DXTypeValue.WLTPYE_PK;
		}else if(IDXRelaConst.IUFO_TRANSACTION.equals(type)){
			return DXTypeValue.JYTPYE_PK;
		}else{
			return DXTypeValue.QTTPYE_PK;
		}
	}

	public ContrastQryVO getQryvo() {
		return qryvo;
	}

	private void setQryvo(ContrastQryVO new_qryvo) {
		this.qryvo = new_qryvo;
	}
	

	//按对账对生成抵销分录	
//	private void excuteCeSumContrast(ArrayList<VouchVO> list, String pk_contrastorg, ArrayList<DXContrastVO> finalExcute) throws BusinessException {
//		String pk_hbrepstru = qryvo.getPk_hbrepstru();
//		String innercode = new HbOrgUtilBO().getInnerCode(pk_hbrepstru, pk_contrastorg);
//		String[] contrastorgs = ContrastBO.getContrastOrgs(pk_hbrepstru, innercode);
//		qryvo.setMeaprojectcache(IntrMeasProjectCache.getSingleton().getInstance());//设置指标映射缓存
//		qryvo.setContrastorgs(contrastorgs);
//		
//		HashSet<String> selfOrgSet = new HashSet<String>();//本方组织
//		HashSet<String> oppOrgSet = new HashSet<String>();//对方组织
//		for (String str : contrastorgs) {
//			String pk_self = str.trim().substring(0, 20);
//			selfOrgSet.add(pk_self);
//			String pk_other = str.trim().substring(20, 40);
//			oppOrgSet.add(pk_other);
//		}
//		qryvo.setSelfOrgs(selfOrgSet);
//		qryvo.setOppOrgs(oppOrgSet);
//		
//		for (Iterator<DXContrastVO> iterator = finalExcute.iterator(); iterator.hasNext();) {
//			DXContrastVO vo = (DXContrastVO) iterator.next();
//			DXRelationBodyVO[] dxbodyvos = vo.getBodyvos();
//			
////			Map<String, Map<String, UFDouble>> allMap = new HashMap<String, Map<String,UFDouble>>();
////			for(DXRelationBodyVO bodyvo : dxbodyvos){
////				if(allMap.get(bodyvo.getPk_dxrela_body()) == null){
////					Map<String, UFDouble> map = batchGenMeetdatasubvo(qryvo,true,selfOrgSet,oppOrgSet,bodyvo,vo.getHeadvo());
////					allMap.put(bodyvo.getPk_dxrela_body(), map);
////				}
////			}
//
//			VouchHeadVO headvo = this.getHeadVO(vo);
//			
//			for (String str : contrastorgs) {
//				String pk_self = str.trim().substring(0, 20);
//				String pk_other = str.trim().substring(20, 40);
//				VouchVO vouchervo = new VouchVO();
//				vouchervo.setParentVO(headvo);
//				DXRelationBodyVO difsubvo = null;
//				ArrayList<VouchBodyVO> dxbodylist = new ArrayList<VouchBodyVO>();
//				for (DXRelationBodyVO subvo : dxbodyvos) {
////					Map<String, UFDouble> result = allMap.get(subvo.getPk_dxrela_body());
////					if(result == null)
////						continue;
//					
//					if (subvo.getType().intValue() == IDXRelaConst.DIFF) {
//						difsubvo = subvo;
//					} else {
//						UFDouble genMeetdatasubvo = genMeetdatasubvo(subvo, true, pk_self, pk_other, vo);
//						VouchBodyVO tmpbodyvo = new VouchBodyVO();
//						if (subvo.getType().intValue() == IDXRelaConst.CREDIT) {
//							tmpbodyvo.setCreditamount(genMeetdatasubvo);
//						} else {
//							tmpbodyvo.setDebitamount(genMeetdatasubvo);
//						}
//						tmpbodyvo.setPk_measure(subvo.getPk_measure());
//						tmpbodyvo.setPk_selforg(pk_self);
//						tmpbodyvo.setPk_countorg(pk_other);
//						StringBuilder content = new StringBuilder();
//						content.append(OrgUtil.getOrgMultiName(pk_self)).append("->").append(OrgUtil.getOrgMultiName(pk_other));
//						if(subvo.getDigest() != null)
//							content.append(":" + subvo.getDigest());
//						
//						tmpbodyvo.setDigest(content.toString());
//						dxbodylist.add(tmpbodyvo);
//					}
//				}
//				// 进行差额项目处理
//				if (null != difsubvo) {
//					VouchBodyVO subvo = this.genMeetdatasubvo(difsubvo, dxbodylist);
//					if (null != subvo) {
//						dxbodylist.add(subvo);
//					}
//				}
//				if (null != dxbodylist && dxbodylist.size() > 0) {
//					// 重新设置分录号
//					ArrayList<VouchBodyVO> resultBodyList = resetDetailnum(vo, dxbodylist.toArray(new VouchBodyVO[0]));
//					vouchervo.setChildrenVO(resultBodyList.toArray(new VouchBodyVO[0]));
//					
//					isAllZero(list, vouchervo, resultBodyList);
//				}
//			}
//		}
//	}
	
	
//	private Map<String, UFDouble> batchGenMeetdatasubvo(ContrastQryVO qryvo,boolean bself,HashSet<String> selfOrgSet, HashSet<String> oppOrgSet,DXRelationBodyVO subvo,DXRelationHeadVO headvo) throws BusinessException {
//		Map<String, UFDouble> map = new HashMap<String, UFDouble>();
//		try {
//			map = ContrastFuncBO.batchCallFunc(qryvo, bself, selfOrgSet, oppOrgSet,subvo,headvo);
//		} catch (Exception e) {
//			throw new BusinessException(e.getMessage());
//		}
//		return map;
//	}
	
//	private UFDouble genMeetdatasubvo(DXRelationBodyVO subvo,boolean bself, String pk_self, String pk_other,DXContrastVO contrastVO) throws BusinessException {
//		UFDouble data =UFDouble.ZERO_DBL;
//		try {
//			data = new UFDouble(ContrastFuncBO.callFunc(this.getQryvo(), bself, pk_self, pk_other, subvo, contrastVO));
//		} catch (Exception e) {
//			throw new BusinessException(e.getMessage());
//		}
//		return data;
//	}
	
//	public String[] getContrastOrgs(String pk_scheme, String innercode) throws BusinessException {
//	String[] result = null;
//	HashSet<String> set = new HashSet<String>();
//	StringBuffer content = new StringBuffer();
//	if (this.getDmo().getDatabaseType() == CrossDBConnection.ORACLE) {
//		content.append("SELECT pk_org || innercode pk ");
//	} else {
//		content.append("SELECT pk_org + innercode pk ");
//	}
//	content.append("  FROM org_rcsmember_v ");
//	content.append(" WHERE pk_svid = ? AND innercode like '").append(innercode.trim()).append("%' ");
//
//	Object[] paramvalues = new String[] {
//		pk_scheme
//	};
//	int[] paramtypes = new int[] {
//		Types.CHAR
//	};
//	try {
//		String[] pks = getDmo().getPksBySql(content.toString(), paramvalues, paramtypes);
//		ArrayList<String> firstlist = new ArrayList<String>();
//		int firstsubnode = 20 + innercode.trim().length() + 4;
//		int rootnode = 20 + innercode.trim().length();
//		HashMap<String, String> pkmap = new HashMap<String, String>();
//		HashMap<String, String> innercodemap = new HashMap<String, String>();
//		String[] allpks = null;
//		String rootnodepk = "";
//		if (null != pks && pks.length > 0) {
//			for (String str : pks) {
//				if (null != str && str.trim().length() > 0) {
//					if (str.trim().length() == rootnode) {
//						rootnodepk = str.trim();
//					} else if (str.trim().length() == firstsubnode) {
//						firstlist.add(str.trim());
//					}
//					pkmap.put(str.trim().substring(0, 20), str.trim().substring(20, str.trim().length()));
//					innercodemap.put(str.trim().substring(20, str.trim().length()), str.trim().substring(0, 20));
//				}
//			}
//			HashSet<String> firstloop = new HashSet<String>();
//			firstloop.add(rootnodepk.trim().substring(0, 20));
//			if (firstlist.size() > 0) {
//				String[] subpks = new String[firstlist.size()];
//				firstlist.toArray(subpks);
//				for (String str : subpks) {
//					firstloop.add(str.trim().substring(0, 20));
//				}
//			}
//			String[] firstrootpks = new String[firstloop.size()];
//			firstloop.toArray(firstrootpks);
//			for (String pk1 : firstrootpks) {
//				for (String pk2 : firstrootpks) {
//					if (pk1.trim().equals(pk2.trim())) {
//
//					} else {
//						set.add(pk1.trim() + pk2.trim());
//					}
//				}
//			}
//			if (set.size() == 0) {
//				return null;
//			}
//			allpks = new String[pkmap.size()];
//			pkmap.keySet().toArray(allpks);
//			// 处理完毕根节点和第一层节点
//			// 开始处理利用根节点去遍历其他所有节点
//			String rootpk = rootnodepk.trim().substring(0, 20);
//			for (String pk3 : allpks) {
//				if (pk3.trim().equals(rootpk)) {
//				} else {
//					set.add(pk3 + rootpk);
//					set.add(rootpk + pk3);
//				}
//			}
//			// 开始处理第一层次的节点
//			for (String pkorg4 : firstrootpks) {
//				if (pkorg4.trim().equals(rootpk)) {
//				} else {
//					String[] my = this.getAllSubNodes(pkmap.get(pkorg4), innercodemap);
//					String[] otherpks = this.getOtherFirstNodepks(pkorg4, firstrootpks, rootpk);
//					if (null != otherpks && otherpks.length > 0 && null != my && my.length > 0) {
//						for (String str5 : otherpks) {
//							String[] others = this.getAllSubNodes(pkmap.get(str5), innercodemap);
//							for (String str6 : my) {
//								if (null != others && others.length > 0) {
//									for (String str7 : others) {
//										if (str6.trim().equals(str7.trim())) {
//										} else {
//											set.add(str6 + str7);
//											set.add(str7 + str6);
//										}
//									}
//								}
//
//							}
//						}
//					}
//
//				}
//			}
//			if (set.size() > 0) {
//				result = new String[set.size()];
//				set.toArray(result);
//			}
//		}
//	} catch (SQLException e) {
//		throw new BusinessException(e.getMessage());
//	} catch (InstantiationException e) {
//		throw new BusinessException(e.getMessage());
//	} catch (IllegalAccessException e) {
//		throw new BusinessException(e.getMessage());
//	}
//
//	return result;
//
//}

///**
// * 取的第一层次的某一个节点和其余节点的俩俩互对关系
// *
// * @param firstpk
// * @param firstpks
// * @param rootpk
// * @return
// */
//private String[] getOtherFirstNodepks(String firstorgpk, String[] firstpks,
//		String rootpk) {
//	String[] result = null;
//	HashSet<String> set = new HashSet<String>();
//	for (String str : firstpks) {
//		if (str.trim().equals(firstorgpk)) {
//
//		} else if (str.trim().equals(rootpk)) {
//		} else {
//			set.add(str.trim());
//		}
//
//	}
//	result = new String[set.size()];
//	set.toArray(result);
//	return result;
//}
//private SingleTabDMO getDmo() throws BusinessException {
//	if (null == dmo) {
//		try {
//			dmo = new SingleTabDMO();
//		} catch (NamingException e) {
//			Logger.error(e.getMessage(), e);
//		}
//	}
//	return dmo;
//}

//private String[] getAllSubNodes(String innercode,
//		HashMap<String, String> nodemap) {
//	String[] result = null;
//	String[] nodekeys = new String[nodemap.size()];
//	nodemap.keySet().toArray(nodekeys);
//	ArrayList<String> list = new ArrayList<String>();
//	int len = innercode.trim().length();
//	for (String str : nodekeys) {
//		if (str.trim().length() > len) {
//			if (str.trim().substring(0, len).equals(innercode.trim())) {
//				list.add(nodemap.get(str));
//			}
//		}
//
//	}
//	if (list.size() > 0) {
//		result = new String[list.size()];
//		list.toArray(result);
//	}
//	return result;
//}
	
	
//	private String toCurrentLang(OrgVO vo) {
//		if (vo != null) {
//			int index = MultiLangContext.getInstance().getCurrentLangSeq();
//			if (index == 1) {
//				return vo.getName();
//			} else if (index == 2) {
//				return vo.getName2();
//			} else if (index == 3) {
//				return vo.getName3();
//			} else if (index == 4) {
//				return vo.getName4();
//			} else if (index == 5) {
//				return vo.getName5();
//			} else if (index == 6) {
//				return vo.getName6();
//			}
//			return vo.getName();
//		} else {
//			return null;
//		}
//	}
//
//	private String toCurrentLang(DXRelationHeadVO vo) {
//		if (vo != null) {
//			int index = MultiLangContext.getInstance().getCurrentLangSeq();
//			if (index == 1) {
//				return vo.getName();
//			} else if (index == 2) {
//				return vo.getName2();
//			} else if (index == 3) {
//				return vo.getName3();
//			} else if (index == 4) {
//				return vo.getName4();
//			} else if (index == 5) {
//				return vo.getName5();
//			} else if (index == 6) {
//				return vo.getName6();
//			}
//			return vo.getName();
//		} else {
//			return null;
//		}
//	}

//	private IVouchQrySrv vouchQrySrv;

//	private IVouchQrySrv getVouchQrtSrv() {
//		if (vouchQrySrv == null) {
//			vouchQrySrv = NCLocator.getInstance().lookup(IVouchQrySrv.class);
//		}
//		return vouchQrySrv;
//	}

	// private Integer getIorder() {
	// Integer result = null;
	// try {
	// int iOrder = getVouchQrtSrv().getCompleteOrdrer(
	// HBAloneIDUtil.getAdjustVoucherAlone_id(this.getQryvo(),
	// true), IVouchType.TYPE_AUTO_ENTRY,
	// this.getQryvo().getSchemevo().getPk_hbscheme(),
	// this.getQryvo().getSchemevo().getPk_keygroup());
	// result = Integer.valueOf(iOrder);
	// } catch (UFOSrvException e) {
	// AppDebug.debug(e.getMessage());
	// }
	// return result;
	// }
	
//	private String toCurrentLang(DXRelationHeadVO vo) {
//		if (vo != null) {
//			int index = MultiLangContext.getInstance().getCurrentLangSeq();
//			if (index == 1) {
//				return vo.getName();
//			} else if (index == 2) {
//				return vo.getName2();
//			} else if (index == 3) {
//				return vo.getName3();
//			} else if (index == 4) {
//				return vo.getName4();
//			} else if (index == 5) {
//				return vo.getName5();
//			} else if (index == 6) {
//				return vo.getName6();
//			}
//			return vo.getName();
//		} else {
//			return null;
//		}
//	}
//	
//	private String toCurrentLang(OrgVO vo) {
//		if (vo != null) {
//			int index = MultiLangContext.getInstance().getCurrentLangSeq();
//			if (index == 1) {
//				return vo.getName();
//			} else if (index == 2) {
//				return vo.getName2();
//			} else if (index == 3) {
//				return vo.getName3();
//			} else if (index == 4) {
//				return vo.getName4();
//			} else if (index == 5) {
//				return vo.getName5();
//			} else if (index == 6) {
//				return vo.getName6();
//			}
//			return vo.getName();
//		} else {
//			return null;
//		}
//	}

}