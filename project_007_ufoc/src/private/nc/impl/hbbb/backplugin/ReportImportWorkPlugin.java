package nc.impl.hbbb.backplugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.logging.Logger;
import nc.bs.pub.pa.PreAlertObject;
import nc.bs.pub.pa.PreAlertReturnType;
import nc.bs.pub.taskcenter.BgWorkingContext;
import nc.bs.pub.taskcenter.IBackgroundWorkPlugin;
import nc.itf.iufo.data.IRepDataQuerySrv;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.crossdb.CrossDBConnection;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.RepFormatModelCache;
import nc.pub.iufo.cache.ReportCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.util.hbbb.HBVersionUtil;
import nc.util.hbbb.MeasurePubDataUtil;
import nc.util.hbbb.hbscheme.HBSchemeSrvUtils;
import nc.util.hbbb.pub.HBPubItfService;
import nc.util.hbbb.service.HBBaseDocItfService;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.iufo.data.MeasureDataVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.RepDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.measure.MeasureVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.mdm.pub.NtbLogger;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;

import com.ufida.dataset.*;
import com.ufsoft.iufo.fmtplugin.datastate.CellsModelOperator;
import com.ufsoft.iufo.fmtplugin.measure.MeasureModel;
import com.ufsoft.iufo.util.OrgUtil;
import com.ufsoft.report.ReportContextKey;
import com.ufsoft.table.CellPosition;
import com.ufsoft.table.CellsModel;

import nc.jdbc.framework.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * <b>Application name:</b>客开项目<br>
 * <b>Application describing:</b> 报表导入插件，报表数据从现有数据源导入到etl数据源
 * 节能，企业报表专用，导入，资产负债，等几张表 <br>
 * <b>Copyright:</b>Copyright &copy; 2019 用友软件股份有限公司版权所有。<br>
 * <b>Company:</b>yonyou<br>
 * <b>Date:</b>2019-4-24<br>
 * 
 * @author：王志强
 */
public class ReportImportWorkPlugin implements IBackgroundWorkPlugin {
	
	/**
	 * 当前期间
	 */
	private String currentPeriod;

	@Override
	public PreAlertObject executeTask(BgWorkingContext context)
			throws BusinessException {

		// 报表类别
		String strRepCode = String.valueOf(context.getKeyMap().get("报表类型"));

		// 查找所有主体
		ReportCache repCache = IUFOCacheManager.getSingleton().getReportCache();
		ReportVO repVO = repCache.getByCode(strRepCode);
		Integer version = 50001;
		String strPeriod = String.valueOf(context.getKeyMap().get("会计期间"));
		if(strPeriod==null||strPeriod.trim().length()<5){
			int month = new UFDate().getMonth();
			int year = new UFDate().getYear();
			if(month==1){
				year = year-1;
				month=12;
			}else{
				month = month-1;
			}
			String monthStr = month+"";
			if(month<10){
				monthStr="0"+monthStr;
			}
			strPeriod = year+"-"+monthStr;
		}
		//每次检查主体是否新增过
		insert_org();
		
		
		List<selfOrgVo> utils = queryUtilS();
		IRepDataQuerySrv qrySrv = HBPubItfService.getRemoteRepDataQry();
		// 查询指标数据
		List<ESBMeasrueVO> measures = queryTableMeasures(strRepCode);
		
		
		String verId = String.valueOf(context.getKeyMap().get("合并方案"));
		if(verId!=null&&verId.length()>15){
			HBSchemeVO schemeVO = HBSchemeSrvUtils.getHBSchemeByHBSchemeId(verId);
			
			version = schemeVO.getVersion();
			if(version==null){
				version = 50001;
			}
		}
		
	
		currentPeriod = strPeriod;
		// 删除历史数据
		BaseDAO dao = new BaseDAO(ReportImportConst.OTHER_DATASOURCE);
		
		dao.executeUpdate(getDelSql(strRepCode, strPeriod));
	
		printLog("Delete period data ->"+strRepCode+";"+strPeriod);

		//
		Map<String, UFBoolean> isLeaf = null;
		
		List<String> unitPks = new ArrayList<>();
		for(selfOrgVo orgVo:utils){
			unitPks.add(orgVo.getPk_org());
		}
		
		isLeaf = HBBaseDocItfService.getRemoteHBRepStru().isLeafMembers(
				unitPks.toArray(new String[0]), "0001X310000000004YLI");

		for (selfOrgVo util : utils) {
			String strCurrCode = "1002Z0100000000001K1";

			String strKeyGroupId = repVO.getPk_key_comb();

			MeasurePubDataVO pubdata = null;
			try {
				pubdata = getMeasurePubdata(strPeriod, util.getPk_org(),
						strCurrCode, strKeyGroupId, 0);
			} catch (Exception e) {
				throw new BusinessException(e.getMessage(),e);
			}
			RepDataVO[] vos = null;
			if(pubdata==null){
				this.printLog("查询合并"+pubdata+"为Null 主体："+util.getName()+";"+strRepCode);
				 
			}else{
				
				 
				try {
					vos = qrySrv.loadRepData(repVO.getPk_report(),
							util.getPk_org(), pubdata, "0");

					if (vos != null || vos.length != 0) {
					 
					 

						addData(strRepCode, vos[0], measures, util, "0");

					}
				} catch (Exception e) {
					Logger.error(e);
					throw new BusinessRuntimeException("查询联查主体合并数据错误!", e);
				}
			}


			
			//判读是否执行合并数
			if(isLeaf.get(util.getPk_org())!=null&&isLeaf.get(util.getPk_org()).booleanValue()==false){
				try {
					pubdata = getMeasurePubdata(strPeriod, util.getPk_org(),
							strCurrCode, strKeyGroupId,50001);
				} catch (Exception e) {

				}
				

				 
				try {
					if(pubdata!=null){
						vos = qrySrv.loadRepData(repVO.getPk_report(),
								util.getPk_org(), pubdata, version.toString());

						if (vos != null || vos.length != 0) {
						 
						 

							addData(strRepCode, vos[0], measures, util, "510");

						}
					}else{
						this.printLog("查询合并"+pubdata+"为Null 主体："+util.getName()+";"+strRepCode);
					}
					
				} catch (Exception e) {
					this.printLog("查询合并错误："+"主体："+util.getName()+e.getMessage());
					Logger.error(e);
					throw new BusinessRuntimeException("查询联查主体合并数据错误!", e);
				}
			}
			
			
//			if (1 == 1) {
//				break;
//			}

		}

		PreAlertObject retObj = new PreAlertObject();
		retObj.setReturnType(PreAlertReturnType.RETURNMESSAGE);
		StringBuffer errorMessage = new StringBuffer();
		retObj.setMsgTitle("后台自动取数完成!");
		if (errorMessage.length() > 0) {
			throw new BusinessException(errorMessage.toString());
		} else {
			retObj.setReturnObj("后台自动取数完成!");
		}
		return retObj;
	}

	private void addData(String reportCode, RepDataVO repDataVO,
			List<ESBMeasrueVO> measures, selfOrgVo util, String version) {

		if (reportCode.equals(ReportImportConst.reportCodes[0])) {

			insert_zcfz(reportCode, repDataVO, measures, util, version);

		} else if (reportCode.equals(ReportImportConst.reportCodes[2])) {
			insert_lr(reportCode, repDataVO, measures, util, version);

		} else if (reportCode.equals(ReportImportConst.reportCodes[3])) {
			insert_chash(reportCode, repDataVO, measures, util, version);
		} else if (reportCode.equals(ReportImportConst.reportCodes[1])) {
			
			insert_syzqy(reportCode, repDataVO, measures, util, version);

		} else {
			throw new BusinessRuntimeException(
					"class ReportImportWorkPlugin：报表选择错误错误");
		}

	}
	
	
	private void printLog(String message){
		Logger.init("iufo");
		Logger.error(message);
		Logger.init();
	}
	
	private List<EsbOrgVO> getReportOrgs(){
		String[] result = null;
		StringBuffer content = new StringBuffer();

		content.append(" select t3.name,t3.code,t2.code pcode from org_rcsmember_v t1  ");

		content.append(" left join (select t5.code,t4.innercode from org_rcsmember_v t4  inner join org_orgs t5 on t4.pk_org=t5.pk_org )   t2 on t2.innercode  = substr(t1.innercode,1,length(t1.innercode)-4) ");
		content.append(" inner join org_orgs t3 on t1.pk_org=t3.pk_org ");
		content.append(" where t1.pk_svid=? ");
 

		SQLParameter param = new SQLParameter();
		param.addParam("0001X310000000004YLI");
		BaseDAO dao = new BaseDAO();
		try {
			List<EsbOrgVO> pks = (List<EsbOrgVO>) dao.executeQuery(
					content.toString(), param, new EsbOrgVOProcessor());
			return pks;
			 
		} catch (Exception e) {
			NtbLogger.error(e);
			
			
		}
		return new ArrayList<>();
		
	}
	
	
	private List<EsbOrgVO> getEsbOrgs(){
		String[] result = null;
		StringBuffer content = new StringBuffer();

		content.append(" select  t1.UNIT_CODE code,t1.UNIT_NAME name,t1.UNIT_PROP29  pcode from dim_unit_65 t1  ");

	 
 
 
		BaseDAO dao = new BaseDAO(ReportImportConst.OTHER_DATASOURCE);
		try {
			List<EsbOrgVO> pks = (List<EsbOrgVO>) dao.executeQuery(
					content.toString(),  new EsbOrgVOProcessor());
			return pks;
			 
		} catch (Exception e) {
			NtbLogger.error(e);
			
			
		}
		return new ArrayList<>();
		
	}
	
	
	
	
	/**
	 * insert org
	 * 
	 * @param reportCode
	 * @param repDataVO
	 * @param measures
	 * @param string
	 * @author: 王志强
	 * @param util
	 */
	private void insert_org() {
		
		
		List<EsbOrgVO>  reports = getReportOrgs();
		
		
		List<EsbOrgVO>  esbs = getEsbOrgs();
		List<EsbOrgVO> inserts  =  new ArrayList<>();
		if(esbs==null||esbs.isEmpty()){
			inserts = reports;
		}else{
			Map<String, EsbOrgVO> maps = new HashMap<String, EsbOrgVO>();
			for(EsbOrgVO vo:esbs){
				maps.put(vo.getCode(), vo);
			}
			for(EsbOrgVO vo:reports){
				if(maps.get(vo.getCode())==null){
					inserts.add(vo);
				}
			}
		}
		 
		if(inserts==null||inserts.isEmpty()){
			return ;
		}

		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = ConnectionFactory
					.getConnection(ReportImportConst.OTHER_DATASOURCE);
			((CrossDBConnection)con).setAddTimeStamp(false);
//			CrossDBConnection conn
			String str = "insert into dim_unit_65(unit_code,unit_name,UNIT_PROP29) values(?,?,?)";
			stmt = con.prepareStatement(str);
		 
			for (EsbOrgVO measure : inserts) {

			 
 
				stmt.setString(1, measure.getCode());
				stmt.setString(2, measure.getName());
				
				stmt.setString(3, measure.getPcode());
 
				stmt.addBatch();
				 
			
				
				 
				


			}
		
		
			
		 
				this.printLog("******需要插入表：dim_unit"+inserts.size()+"条数据");
				stmt.executeBatch();
				this.printLog("******插入成功。");
			 

		} catch (Exception ex) {
			
			Logger.error(ex);
			this.printLog("执行 orgs 错误"+ex.getMessage());
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
			}
			try {
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
			}
		}



	}

	/**
	 * insert资产负债
	 * 
	 * @param reportCode
	 * @param repDataVO
	 * @param measures
	 * @param string
	 * @author: 王志强
	 * @param util
	 */
	private void insert_zcfz(String reportCode, RepDataVO repDataVO,
			List<ESBMeasrueVO> measures, selfOrgVo util, String string) {

		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = ConnectionFactory
					.getConnection(ReportImportConst.OTHER_DATASOURCE);
			((CrossDBConnection)con).setAddTimeStamp(false);
//			CrossDBConnection conn
			String str = "insert into T_IUFO_zcfz_65(unit_code,unit_name,item_code,item_name,input_date,i_year,i_month,value_m,value_ytd,ver) values(?,?,?,?,?,?,?,?,?,?)";
			stmt = con.prepareStatement(str);
			boolean executeBatch = false;
			int count = 0 ;
			for (ESBMeasrueVO measure : measures) {
				// 取field1 、2
				UFDouble field1Value = getMeasureNumberValue(repDataVO,measure.getField1());
				UFDouble field2Value = getMeasureNumberValue(repDataVO,measure.getField2());
				
				if(field1Value.doubleValue()!=0||field2Value.doubleValue()!=0){
					count++;
					executeBatch = true;
					stmt.setString(1, util.getCode());
					stmt.setString(2, util.getName());
					
					stmt.setString(3, measure.getItem_code());
					stmt.setString(4,  measure.getItem_name());
					
					stmt.setString(5, currentPeriod);

					stmt.setString(6, currentPeriod.substring(0,4));
					
					stmt.setString(7, currentPeriod);
					stmt.setBigDecimal(8, field1Value.toBigDecimal());
					stmt.setBigDecimal(9, field2Value.toBigDecimal());
					stmt.setString(10, string);
					stmt.addBatch();
					 
				}
				


			}
		
		
			
			if(executeBatch){
				this.printLog("******需要插入表：T_IUFO_zcfz_65"+count+"条数据。主体："+util.getName()+"版本 is:"+string);
				stmt.executeBatch();
				this.printLog("******插入成功。");
			}

		} catch (Exception ex) {
			
			Logger.error(ex);
			this.printLog("执行"+reportCode+"错误"+ex.getMessage());
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
			}
			try {
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
			}
		}



	}
	
	
	
	/**
	 * insert所有者权益表
	 * 
	 * @param reportCode
	 * @param repDataVO
	 * @param measures
	 * @param string
	 * @author: 王志强
	 * @param util
	 */
	private void insert_syzqy(String reportCode, RepDataVO repDataVO,
			List<ESBMeasrueVO> measures, selfOrgVo util, String string) {

		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = ConnectionFactory
					.getConnection(ReportImportConst.OTHER_DATASOURCE);
			((CrossDBConnection)con).setAddTimeStamp(false);
//			CrossDBConnection conn
			String str = "insert into t_iufo_syzqy_65(unit_code,unit_name,item_code,item_name,input_date,i_year,i_month,value_y,ver) values(?,?,?,?,?,?,?,?,?)";
			stmt = con.prepareStatement(str);
			boolean executeBatch = false;
			int count = 0;
			for (ESBMeasrueVO measure : measures) {
				// 取field1 、2
				UFDouble field1Value = getMeasureNumberValue(repDataVO,measure.getField1());
//				UFDouble field2Value = getMeasureNumberValue(repDataVO,measure.getField2());
				
				if(field1Value.doubleValue()!=0){
					count++;
					executeBatch = true;
					stmt.setString(1, util.getCode());
					stmt.setString(2, util.getName());
					
					stmt.setString(3, measure.getItem_code());
					stmt.setString(4,  measure.getItem_name());
					
					stmt.setString(5, currentPeriod);

					stmt.setString(6, currentPeriod.substring(0,4));
					
					stmt.setString(7, currentPeriod);
					stmt.setBigDecimal(8, field1Value.toBigDecimal());
					
					stmt.setString(9, string);
					stmt.addBatch();
					 
				}
				


			}
//			this.printLog("******需要插入表：T_IUFO_chash"+count+"条数据。版本 is:"+string);
			
			if(executeBatch){
				this.printLog("******需要插入表：t_iufo_syzqy_65"+count+"条数据。主体："+util.getName()+"版本 is:"+string);
				stmt.executeBatch();
				this.printLog("******插入成功。");
			}

		} catch (Exception ex) {
			Logger.error(ex);
			this.printLog("执行"+reportCode+"错误"+ex.getMessage());
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
			}
			try {
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
			}
		}



	}
	
	
	/**
	 * insert利润表
	 * 
	 * @param reportCode
	 * @param repDataVO
	 * @param measures
	 * @param string
	 * @author: 王志强
	 * @param util
	 */
	private void insert_lr(String reportCode, RepDataVO repDataVO,
			List<ESBMeasrueVO> measures, selfOrgVo util, String string) {

		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = ConnectionFactory
					.getConnection(ReportImportConst.OTHER_DATASOURCE);
			((CrossDBConnection)con).setAddTimeStamp(false);
//			CrossDBConnection conn
			String str = "insert into T_IUFO_lr_65(unit_code,unit_name,item_code,item_name,input_date,i_year,i_month,value_m,value_ytd,value_lm,ver) values(?,?,?,?,?,?,?,?,?,?,?)";
			stmt = con.prepareStatement(str);
			boolean executeBatch = false;
			int count =0;
			for (ESBMeasrueVO measure : measures) {
				count++;
				// 取field1 、2
				UFDouble field1Value = getMeasureNumberValue(repDataVO,measure.getField1());
				UFDouble field2Value = getMeasureNumberValue(repDataVO,measure.getField2());
				UFDouble field3Value = getMeasureNumberValue(repDataVO,measure.getField3());
				
				if(field1Value.doubleValue()!=0||field2Value.doubleValue()!=0||field2Value.doubleValue()!=0){
					executeBatch = true;
					stmt.setString(1, util.getCode());
					stmt.setString(2, util.getName());
					
					stmt.setString(3, measure.getItem_code());
					stmt.setString(4,  measure.getItem_name());
					
					stmt.setString(5, currentPeriod);

					stmt.setString(6, currentPeriod.substring(0,4));
					
					stmt.setString(7, currentPeriod);
					stmt.setBigDecimal(8, field1Value.toBigDecimal());
					stmt.setBigDecimal(9, field2Value.toBigDecimal());
					stmt.setBigDecimal(10, field3Value.toBigDecimal());
					stmt.setString(11, string);
					stmt.addBatch();
					 
				}
				


			}
	
			
			if(executeBatch){
				this.printLog("******需要插入表：T_IUFO_lr"+count+"条数据。版本 is:"+string);
				stmt.executeBatch();
				this.printLog("******插入成功。");
			}

		} catch (Exception ex) {
			Logger.error(ex);
			this.printLog("执行"+reportCode+"错误"+ex.getMessage());
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
			}
			try {
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
			}
		}



	}
	
	
	
	/**
	 * insert现金流量
	 * 
	 * @param reportCode
	 * @param repDataVO
	 * @param measures
	 * @param string
	 * @author: 王志强
	 * @param util
	 */
	private void insert_chash(String reportCode, RepDataVO repDataVO,
			List<ESBMeasrueVO> measures, selfOrgVo util, String string) {

		Connection con = null;
		PreparedStatement stmt = null;
		int count=0;
		try {
			con = ConnectionFactory
					.getConnection(ReportImportConst.OTHER_DATASOURCE);
			((CrossDBConnection)con).setAddTimeStamp(false);
//			CrossDBConnection conn
			String str = "insert into T_IUFO_chash_65(unit_code,unit_name,item_code,item_name,input_date,i_year,i_month,value_m,value_ytd,value_lm,ver) values(?,?,?,?,?,?,?,?,?,?,?)";
			stmt = con.prepareStatement(str);
			boolean executeBatch = false;
		
			for (ESBMeasrueVO measure : measures) {
				// 取field1 、2
				UFDouble field1Value = getMeasureNumberValue(repDataVO,measure.getField1());
				UFDouble field2Value = getMeasureNumberValue(repDataVO,measure.getField2());
				UFDouble field3Value = getMeasureNumberValue(repDataVO,measure.getField3());
				
				if(field1Value.doubleValue()!=0||field2Value.doubleValue()!=0||field2Value.doubleValue()!=0){
					count++;
					executeBatch = true;
					stmt.setString(1, util.getCode());
					stmt.setString(2, util.getName());
					
					stmt.setString(3, measure.getItem_code());
					stmt.setString(4,  measure.getItem_name());
					
					stmt.setString(5, currentPeriod);

					stmt.setString(6, currentPeriod.substring(0,4));
					
					stmt.setString(7, currentPeriod);
					stmt.setBigDecimal(8, field1Value.toBigDecimal());
					stmt.setBigDecimal(9, field2Value.toBigDecimal());
					stmt.setBigDecimal(10, field3Value.toBigDecimal());
					stmt.setString(11, string);
					stmt.addBatch();
					 
				}
				


			}
		 
			
			if(executeBatch){
				this.printLog("******需要插入表：T_IUFO_chash_65"+count+"条数据。版本 is:"+string);
				stmt.executeBatch();
				this.printLog("******插入成功。");
			}

		} catch (Exception ex) {
			Logger.error(ex);
			this.printLog("执行"+reportCode+"错误"+ex.getMessage());
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (Exception e) {
			}
			try {
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
			}
		}



	}
	

	private UFDouble getMeasureNumberValue(RepDataVO repDataVO,
			String code_measure) {

		MeasureDataVO[] datas = repDataVO.getMeasureDatas(code_measure);
		if (datas != null && datas.length > 0) {
			UFDouble value = datas[0].getUFDoubleValue();
			if (value != null && value.doubleValue() != 0) {
				return value;
			}
		}
		return new UFDouble(0);
	}

	private String getDelSql(String reportCode, String strPeriod) {

		StringBuffer sb = new StringBuffer();
		sb.append(" delete from ");
		if (reportCode.equals(ReportImportConst.reportCodes[0])) {
			sb.append(ReportImportConst.getTableNameMap().get(reportCode));

		} else if (reportCode.equals(ReportImportConst.reportCodes[2])) {
			sb.append(ReportImportConst.getTableNameMap().get(reportCode));

		} else if (reportCode.equals(ReportImportConst.reportCodes[3])) {
			sb.append(ReportImportConst.getTableNameMap().get(reportCode));

		} else if (reportCode.equals(ReportImportConst.reportCodes[1])) {
			sb.append(ReportImportConst.getTableNameMap().get(reportCode));

		} else {
			throw new BusinessRuntimeException(
					"class ReportImportWorkPlugin：报表选择错误错误");
		}
		sb.append(" where I_MONTH = '" + strPeriod + "'");
		return sb.toString();

	}

	MeasurePubDataVO getMeasurePubdata(String strAccPeriod, String strUnitId,
			String strCurrCode, String strKeyGroupId, int iVer)
			throws Exception {
		KeyGroupVO keyGroupVO = IUFOCacheManager.getSingleton()
				.getKeyGroupCache().getByPK(strKeyGroupId);
		MeasurePubDataVO pubData = new MeasurePubDataVO();
		pubData.setKeyGroup(keyGroupVO);
		pubData.setKType(strKeyGroupId);
		// 单位关键字赋值
		pubData.setKeywordByPK(KeyVO.CORP_PK, strUnitId);
		// 日期关键字赋值
		pubData.setKeywordByPK(keyGroupVO.getTTimeKey().getPk_keyword(),
				strAccPeriod);
		// 币种关键字赋值
		String strCoinKeywordPK = getCurrKeyPK(keyGroupVO);
		if (strCoinKeywordPK != null)
			pubData.setKeywordByPK(KeyVO.COIN_PK, strCurrCode);

		// iVer = iVer==0 ? HBBBSysParaUtil.VER_SEPARATE :
		// HBBBSysParaUtil.VER_HBBB;
		pubData.setVer(iVer);
		pubData = MeasurePubDataBO_Client.findByKeywords(pubData);

		// rtnPubData.setAloneID(MeasurePubDataBO_Client.getAloneID(rtnPubData));
		return pubData;

	}



	String getCurrKeyPK(KeyGroupVO keyGroupVO) {
		KeyVO[] keys = keyGroupVO.getKeys();
		for (int i = 0; i < keys.length; i++) {
			if (keys[i].getPk_keyword().equals(KeyVO.COIN_PK))
				return keys[i].getPk_keyword();
		}
		return null;
	}



	private List<ESBMeasrueVO> queryTableMeasures(String reportCode) {
		String tableName = ReportImportConst.getNameMap().get(reportCode);
		if (tableName == null) {
			return new ArrayList<>();
		}
		String[] result = null;
		StringBuffer sb = new StringBuffer();

		sb.append(ESBMeasrueVO.selectSql);

		sb.append(" WHERE TABLE_NAME = ? ");

		SQLParameter param = new SQLParameter();
		param.addParam(tableName);
		BaseDAO dao = new BaseDAO(ReportImportConst.OTHER_DATASOURCE);
		try {
			List<ESBMeasrueVO> rtn = (List<ESBMeasrueVO>) dao.executeQuery(
					sb.toString(), param, new ESBMeasureProcessor());
			return rtn;
		} catch (Exception e) {
			throw new BusinessRuntimeException(e.getMessage());
		}

	}

	private List<selfOrgVo> queryUtilS() {
		String[] result = null;
		StringBuffer content = new StringBuffer();

		content.append("select t1.pk_org,t2.code,t2.name from  org_rcsmember_v t1,org_orgs t2 ");
		content.append(" WHERE t1.pk_svid=? and t1.isbalanceunit='N' and t1.pk_org = t2.pk_org");

		SQLParameter param = new SQLParameter();
		param.addParam("0001X310000000004YLI");
		BaseDAO dao = new BaseDAO();
		try {
			List<selfOrgVo> pks = (List<selfOrgVo>) dao.executeQuery(
					content.toString(), param, new ResSetProcessor());
			return pks;
		} catch (Exception e) {
			throw new BusinessRuntimeException(e.getMessage());
		}
	}

	private class ResSetProcessor implements ResultSetProcessor {
		private static final long serialVersionUID = 8715819462600958845L;

		@Override
		public List<selfOrgVo> handleResultSet(ResultSet rs)
				throws SQLException {
			List<selfOrgVo> rsList = new ArrayList<selfOrgVo>();
			while (rs.next()) {
				String pk = rs.getString(1);
				String code = rs.getString(2);
				String name = rs.getString(3);
				rsList.add(new selfOrgVo(pk, name, code));
			}
			return rsList;
		}

	}

	private class selfOrgVo {
		private String pk_org;
		private String code;
		private String name;

		public selfOrgVo(String pk, String name, String code) {
			this.pk_org = pk;
			this.code = code;
			this.name = name;
		}

		public String getPk_org() {
			return pk_org;
		}

		public void setPk_org(String pk_org) {
			this.pk_org = pk_org;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

}
