package nc.itf.iufo.dataremove;

import java.util.Map;

import com.ufsoft.table.CellsModel;

import nc.pub.iufo.exception.UFOSrvException;
import nc.vo.iufo.balance.BalanceCondVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.SingleRepExpParam;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.iuforeport.rep.ReportVO;

public interface IDataRemoveSrv {

	/**
	 * removeDatas
	 *
	 * @param strStatements String[]
	 */
	public abstract int removeDatas(String[] strStatements)
			throws UFOSrvException;

	public abstract String[] findDataPKsByStatement(String[] strStatements)
			throws UFOSrvException;

	public abstract String[] findDataPKsByRecursion(
			nc.util.iufo.dataremove.base.ModuleRelaDesc relaDesc,
			String[] strStatements) throws UFOSrvException;

 

	public abstract Map<String,String> pushReport(String report_code, KeyVO[] keys,
			CellsModel model, MeasurePubDataVO pubVo, String repOrgStructCode,
			String orgCode, String taskCode);

}