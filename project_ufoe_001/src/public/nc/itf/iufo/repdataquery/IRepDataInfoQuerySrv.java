package nc.itf.iufo.repdataquery;

import java.util.List;
import java.util.Map;

import nc.pub.iufo.exception.UFOSrvException;
import nc.vo.iufo.checkexecute.CheckExeResultVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.datasource.DataSourceVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.query.IUfoQueryCondVO;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.iuforeport.rep.RepShowPrintVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;

import com.ufsoft.table.CellsModel;
/**
 * 报表信息查询服务接口
 * @author yp
 *
 */
public interface IRepDataInfoQuerySrv {

	public List<RepDataQueryResultVO> queryRepDataInfo(String[] aryAloneIds,String[] aryRepIds,int iVer,int iRepCommitFlag,int iCheckState) throws UFOSrvException;
	
	public List<RepDataQueryResultVO> loadRepDataInfo(IUfoQueryCondVO queryCond,String[] showColumns,String busiDate) throws UFOSrvException;
	
	public List<RepDataQueryResultVO> loadPrintRepDataInfo(String[] aloneid,String taskpk,int inputstate,List<String> commitstate,String[] repPks) throws UFOSrvException;
	
	public Map<String,UFBoolean> queryAllSubCommited(String strRmsPK,MeasurePubDataVO[] pubDatas,KeyGroupVO keyGroup, String taskpk, int iCommitState) throws UFOSrvException;
	
	public Map<String,UFBoolean> queryDirectParentCommited(String[] strAloneIDs,KeyGroupVO keyGroup,String rms, String taskpk) throws UFOSrvException;
	
	public Map<String,String[]> queryTaskAssignOrgPK(String strTaskPK,String pk_rms,String strOrgInnCode) throws UFOSrvException;

	public List<CellsModel> getCellsModels(List<RepDataQueryResultVO> RepDataQueryResultVOs,DataSourceVO ds,String userId,UFDateTime ufDateTime) throws UFOSrvException;
	
	public List<CheckExeResultVO> loadCheckExeInfo(IUfoQueryCondVO queryCond,String[] showColumns) throws UFOSrvException;
	
	public Map<ReportVO,MeasurePubDataVO> getRepPubDatasByAloneIDs(String[] repPks, String[] aloneIDs) throws UFOSrvException;
	
	public List<List<RepShowPrintVO>> getRepCellsModelAndShowVOs(List<RepDataQueryResultVO> RepDataQueryResultVOs,DataSourceVO ds,String userId,UFDateTime ufDateTime, String strBalCondPK,String rmsPk) throws UFOSrvException;
	
	//editor tianjlc 2015-02-12 适配报表组织体系多版本添加
	public Map<String, String[]> queryTaskAssignVersionOrgPK(String strTaskPK,String strRmsVerPK, String strOrgInnCode) throws UFOSrvException;

	public Map<String, UFBoolean> queryDirectParentVersionCommited(String[] strAloneIDs, KeyGroupVO keyGroup, String strRmsVerPK,String taskpk) throws UFOSrvException;

	public Map<String, UFBoolean> queryAllVersionSubCommited(String strRmsVerPK,MeasurePubDataVO[] pubDatas, KeyGroupVO keyGroup, String taskpk,int iCommitState) throws UFOSrvException;
	
	/**
	 * 改动较大，暂时返回null,后续完善
	 * 
	 * @creator tianjlc at 2015-6-18 下午2:44:39
	 * @param repDataQueryResultVOs
	 * @param ds
	 * @param userId
	 * @param ufDateTime
	 * @param strBalCondPK
	 * @param strRmsVerPk
	 * @return
	 * @throws UFOSrvException
	 * @return List<List<RepShowPrintVO>>
	 */
	public List<List<RepShowPrintVO>> getRepVersionCellsModelAndShowVOs(List<RepDataQueryResultVO> repDataQueryResultVOs, DataSourceVO ds,	String userId, UFDateTime ufDateTime, String strBalCondPK, String strRmsVerPk) throws UFOSrvException;
}
