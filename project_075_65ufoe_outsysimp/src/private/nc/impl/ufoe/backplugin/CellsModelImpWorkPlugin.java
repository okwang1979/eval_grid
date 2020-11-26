package nc.impl.ufoe.backplugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.pub.pa.PreAlertObject;
import nc.bs.pub.taskcenter.BgWorkingContext;
import nc.bs.pub.taskcenter.IBackgroundWorkPlugin;
import nc.itf.uif.pub.IUifService;
import nc.jdbc.framework.crossdb.CrossDBConnection;
import nc.jdbc.framework.generator.SequenceGenerator;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.cache.ReportCache;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.ui.iufo.data.RepDataBO_Client;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.RepDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.org.OrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.para.SysInitVO;

import com.ufida.dataset.Context;
import com.ufida.dataset.IContext;
import com.ufsoft.iufo.fmtplugin.datastate.CellsModelOperator;
import com.ufsoft.iufo.fmtplugin.formatcore.IUfoContextKey;
import com.ufsoft.iufo.fmtplugin.formatcore.UfoContextVO;
import com.ufsoft.report.ReportContextKey;
import com.ufsoft.table.Cell;
import com.ufsoft.table.CellsModel;

public class CellsModelImpWorkPlugin implements IBackgroundWorkPlugin {

	@Override
	public PreAlertObject executeTask(BgWorkingContext context)
			throws BusinessException {

		try {
			Logger.init("iufo");
			Logger.error("##start############################");
			String strRepCode = String.valueOf(context.getKeyMap().get("�������"));
			Logger.error("��ѯ������룺"+strRepCode);
			// ������������
			ReportCache repCache = IUFOCacheManager.getSingleton()
					.getReportCache();
			ReportVO repVO = repCache.getByCode(strRepCode);

			String paramPeriods = String.valueOf(context.getKeyMap().get("����ڼ�"));
			if (paramPeriods == null || paramPeriods.trim().length() < 5) {
				int month = new UFDate().getMonth();
				int year = new UFDate().getYear();
	
				String monthStr = month + "";
				if (month < 10) {
					monthStr = "0" + monthStr;
				}
				paramPeriods = year + "-" + monthStr;
			}
//			List<String> muPeriods = new ArrayList<>();
			String[] strPeriods = {paramPeriods}; 
			if(paramPeriods.contains(",")){
				strPeriods = paramPeriods.split(",");
			}
			for(String strPeriod:strPeriods){
				Logger.error("��ѯ�ڼ䣺"+strPeriod);
				IUifService service = NCLocator.getInstance().lookup(
						IUifService.class);
				SysInitVO[] svos = (SysInitVO[]) service.queryByCondition(
						SysInitVO.class, "initcode = 'EasOrg'");
				if (svos == null || svos.length == 0) {
					Logger.error("��ѯ���������������δ��ѯ�����ݡ�");
					throw new BusinessException("EasOrgϵͳ����δ���ã�");
				}
				Logger.error("����������룺"+svos[0].getValue());
				String[] orgCodes = svos[0].getValue().split(",");
			
				for (String orgCode : orgCodes) {
					
					OrgVO orgVO = nc.pub.iufo.basedoc.OrgUtil
							.getOrgVOByCode(orgCode);
					
					if(orgVO==null){
						Logger.error("��ѯ�������δ��ѯ����Ӧ��֯����ѯ���룺"+orgCode);
						continue;
						
					}
					Logger.error("*******��ʼ��ѯ��������,���壺"+orgVO.getCode());
					CellsModel cellsModel = getCellsModel(orgVO, strPeriod, repVO);
					if(cellsModel==null){
						Logger.error("��ѯ��������ʧ�ܣ�"+repVO.getCode()+";org code is:"+orgVO.getCode());
						continue;
						
					}
					Logger.error("��ѯ�����������.");
					Logger.error(cellsModel);
					Logger.error("��ʼ�������ݿ�.->>"+orgCode);
					updateReportData(repVO,cellsModel,orgVO,strPeriod);
					
					 
				}
			}


			return null;
		} catch (Exception ex) {
			Logger.error("��̨��ʱ����������."+ex.getMessage());
			Logger.error(ex);
			
			return null;
		} finally {
			Logger.init();
		}
	}

	private CellsModel getCellsModel(OrgVO org, String period, ReportVO repVO) {

		try {
//			Logger.init("iufo");+
			IContext reportContext = new Context();
			reportContext.setAttribute(ReportContextKey.REPORT_PK,
					repVO.getPk_report());

			CellsModel formatModel = CellsModelOperator
					.getFormatModelByPKWithDataProcess(reportContext, true);

			String strReportPK = repVO.getPk_report();

			String strKeyGroupPk = repVO.getPk_key_comb();
			KeyGroupVO keyGroupVo = UFOCacheManager.getSingleton()
					.getKeyGroupCache().getByPK(strKeyGroupPk);

			MeasurePubDataVO pubVO = new MeasurePubDataVO();
			pubVO.setKType(strKeyGroupPk);
			pubVO.setVer(0);
			pubVO.setKeyGroup(keyGroupVo);
			for (KeyVO key : keyGroupVo.getKeys()) {
				if (key.isTTimeKeyVO()) {
					
					pubVO.setKeywordByPK(key.getPk_keyword(), period);
				} else if (key.isUnitKeyVO(key)) {
					pubVO.setKeywordByPK(key.getPk_keyword(), org.getPk_org());

				} else if ("coin".equals(key.getCode())) {
					pubVO.setKeywordByPK(key.getPk_keyword(), "1002Z0100000000001K1");
				}

			}

			String strAloneID;

			strAloneID = MeasurePubDataBO_Client.getAloneID(pubVO);

			pubVO = MeasurePubDataBO_Client.findByAloneID(strKeyGroupPk,
					strAloneID);

			String userId = InvocationInfoProxy.getInstance().getUserId();
			String pkOrg = pubVO.getUnitPK();
			// getContext().setAttribute(IUfoContextKey.CUR_REPORG_PK, );
			RepDataVO[] repDataVOs = RepDataBO_Client.loadRepData(strReportPK,
					userId, pubVO, pkOrg);
			String pk_group = InvocationInfoProxy.getInstance().getGroupId();
			UfoContextVO ufoContext = getUfoContextVO(strReportPK, pubVO,
					pkOrg, pk_group);

			CellsModel cellsModel = CellsModelOperator
					.doGetDataModelFromRepDataVO(formatModel, repDataVOs[0],
							ufoContext);
			return cellsModel;
		} catch (Exception e) {
			Logger.error("��ѯ�������ݴ���"+e.getMessage());
			Logger.error(e);
		} finally {
//			Logger.init();
		}

		return null;
	}

	private UfoContextVO getUfoContextVO(String repPK,
			MeasurePubDataVO pubData, String pk_org, String pk_group) {
		UfoContextVO context = new UfoContextVO();
		// TODO
		// setDataSource(context);

		context.setAttribute(IUfoContextKey.CUR_GROUP_PK, pk_group);
		context.setAttribute(IUfoContextKey.CUR_REPORG_PK, pk_org);
		context.setAttribute(ReportContextKey.REPORT_PK, repPK);
		context.setAttribute(IUfoContextKey.KEYGROUP_PK, pubData.getKeyGroup()
				.getKeyGroupPK());
		ReportVO rep = IUFOCacheManager.getSingleton().getReportCache()
				.getByPK(repPK);
		context.setAttribute(ReportContextKey.REPORT_NAME, rep.getChangeName());
		context.setAttribute(IUfoContextKey.MEASURE_PUB_DATA_VO, pubData);
		return context;
	}

	private void updateReportData(ReportVO report, CellsModel cellsModel,
			OrgVO orgVo, String period) {


	 
		Connection con = null;
		PreparedStatement stmt = null;
		try {
//			Logger.init("iufo");
			String orgCode = orgVo.getCode();

			String orgName = orgVo.getName();
			String dsName = "ESB";

			BaseDAO dao = new BaseDAO(dsName);
			String delStr = "delete from  iufo_outsys_cellsmodel where orgcode='"
					+ orgCode
					+ "' and period = '"
					+ period
					+ "' and report_code = '" + report.getCode() + "'";
			Logger.init("del str"+delStr);
			dao.executeUpdate(delStr);
					

			con = nc.jdbc.framework.ConnectionFactory.getConnection(dsName);
			((CrossDBConnection) con).setAddTimeStamp(false);

			int maxCol = cellsModel.getColNum() < 12 ? cellsModel.getColNum()
					: 12;

			String colStr = "insert into iufo_outsys_cellsmodel(pk_obj,orgcode,orgname,period,report_code,reoprt_num,";
			for (int i = 0; i < maxCol; i++) {
				int colNum = i + 1;
				colStr = colStr + "col" + colNum;
				if (i < maxCol - 1) {
					colStr = colStr + ",";
				}
			}
			colStr = colStr + ")";
			String valueStr = "values(?,?,?,?,?,?,";
			for (int i = 0; i < maxCol; i++) {
				valueStr = valueStr + "?";
				if (i < maxCol - 1) {
					valueStr = valueStr + ",";
				}
			}
			valueStr = valueStr + ")";

			Logger.error("insert sql:"+colStr + valueStr);
			stmt = con.prepareStatement(colStr + valueStr);

			for (int i = 1; i < cellsModel.getRowNum(); i++) {
//				String pk = new SequenceGenerator(dsName).generate("Z1", 1)[0];
				
				String pk = getGuid();
				stmt.setString(1, pk);
			

				stmt.setString(2, orgCode);

				stmt.setString(3, orgName);
				
				stmt.setString(4, period);
				
				
				if(i==2){
					Logger.error("     pk param��"+pk);
					Logger.error("     orgCode param��"+orgCode);
					Logger.error("     orgName param��"+orgName);
					Logger.error("     period param��"+period);
				}

				stmt.setString(5, report.getCode());
				
				if(i==2){
				Logger.error("     reportCode param��"+report.getCode());
				}
				stmt.setInt(6, i);
				if(i==2){
				Logger.error("     rowNum param��"+i);
				}
				for (int j = 0; j < maxCol; j++) {
					String value = getCellText(cellsModel, i, j);
							
						
					stmt.setString(7 + j,value );
					if(i==2){
					Logger.error("     col"+j+" ("+j+7+"?)param��"+value);
					}
				}

				stmt.addBatch();

			}

			stmt.executeBatch();

		} catch (Exception ex) {
			Logger.error("���±������ݴ���"+ex.getMessage());
			Logger.error(ex);
//			throw new busine
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
//			Logger.init();
		}

	}

	private String getCellText(CellsModel model, int row, int col) {
		Cell cell = model.getCell(row, col);
		if (cell == null) {
			return "~";
		}
		Object value = cell.getValue();
		if (value == null) {
			return "";
		}
		if (value instanceof Number) {
			DecimalFormat df = new DecimalFormat("#.##");
			return df.format(((Number) value).doubleValue());

		} else {
			return String.valueOf(value);
		}

	}
	
	
	
	private  int Guid=100;
	 
	public  String getGuid() {
		
		Guid+=1;

		long now = System.currentTimeMillis();  
		//��ȡ4λ�������  
		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy");  
		//��ȡʱ���  
		String time=dateFormat.format(now);
		String info=now+"";
		//��ȡ��λ�����  
		//int ran=(int) ((Math.random()*9+1)*100); 
		//Ҫ��һ��ʱ���ڵ���������������ظ�������������������޸�
		int ran=0;
		if(Guid>999){
			Guid=100;    	
		}
		ran=Guid;
				
		return time+info.substring(2, info.length())+ran;  
	}


}
