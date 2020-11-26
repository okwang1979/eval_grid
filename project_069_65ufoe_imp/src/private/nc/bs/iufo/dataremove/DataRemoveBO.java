package nc.bs.iufo.dataremove;
import java.util.HashMap;
import java.util.Map;

import nc.bs.logging.Logger;
import nc.itf.iufo.dataremove.IDataRemoveSrv;
import nc.pub.iufo.exception.UFOSrvException;
import nc.util.info.sysimp.NCConnTool;
import nc.util.info.sysimp.SysImpUtil;
import nc.vo.iufo.balance.BalanceCondVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.data.SingleRepExpParam;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.repdataquery.RepDataQueryResultVO;
import nc.vo.iuforeport.rep.ReportVO;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.table.CellsModel;

/**
 * <p>Title:Êý¾ÝÉ¾³ýBOÀà </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c)  2004</p>
 * <p>Company: Ufsoft</p>
 * @author not attributable
 * @version 3.1.0.1
 */

public class DataRemoveBO implements IDataRemoveSrv {
    public DataRemoveBO() {
    }

    public int removeDatas(String[] strStatements) throws UFOSrvException {
        try{
            DataRemoveDMO dmo=new DataRemoveDMO();
            return dmo.removeDatas(strStatements);
        }
        catch(Exception e){
AppDebug.debug(e);//@devTools             AppDebug.debug(e);
            throw new UFOSrvException("DataRemoveBO:removeDatas:"+e.getMessage());
        }
    }

    public String[] findDataPKsByStatement(String[] strStatements) throws UFOSrvException{
        try{
            DataRemoveDMO dmo=new DataRemoveDMO();
            return dmo.findDataPKsByStatement(strStatements);
        }
        catch(Exception e){
AppDebug.debug(e);//@devTools             AppDebug.debug(e);
            throw new UFOSrvException("DataRemoveBO:findDataPKsByStatement:"+e.getMessage());
        }
    }

    public String[] findDataPKsByRecursion(nc.util.iufo.dataremove.base.ModuleRelaDesc relaDesc,String[] strStatements) throws UFOSrvException{
        try {
            DataRemoveDMO dmo = new DataRemoveDMO();
            return dmo.findDataPKsByRecursion(relaDesc,strStatements);
        }
        catch (Exception e) {
AppDebug.debug(e);//@devTools             AppDebug.debug(e);
            throw new UFOSrvException("DataRemoveBO:findDataPKsByStatement:" +e.getMessage());
        }
    }

	@Override
	public Map<String, String> pushReport(String report_code, KeyVO[] keys,
			CellsModel model, MeasurePubDataVO pubVo, String repOrgStructCode,
			String orgCode, String taskCode) {
		 try{
			 String url =  SysImpUtil.createXml(report_code, keys, model, pubVo, repOrgStructCode, orgCode, taskCode);
			return NCConnTool.tranToNC(url);
		 }catch(Exception ex){
				Logger.error(ex.getMessage());
				Logger.error(ex);
				Map<String,String> rtnMap = new HashMap<>();
				rtnMap.put(NCConnTool.KEY_CODE, "err999");
				rtnMap.put(NCConnTool.KEY_INFO, ex.getMessage());
				return rtnMap;
			 
		 }
		
	}

 
	
	
}
