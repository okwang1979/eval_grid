package nc.impl.ufoe.backplugin;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.logging.Logger;
import nc.bs.pub.pa.PreAlertObject;
import nc.bs.pub.taskcenter.BgWorkingContext;
import nc.bs.pub.taskcenter.IBackgroundWorkPlugin;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.utils.iufo.TaskSrvUtils;
import nc.utils.iufo.TotalSrvUtils;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.lang.UFDate;

import com.ufsoft.iuforeport.repdatainput.RepDataOperResultVO;

public class ReportTotlalSumWorkPlugin  implements IBackgroundWorkPlugin{

	@Override
	public PreAlertObject executeTask(BgWorkingContext context)
			throws BusinessException {
		try{
			String pk_task = "1001A210000000GJS6CE";
			
			//key编码,主体,期间,币种
			String keyCodePeriod = "accmonth";
			String keyCodeCurr =   "coin";
			String keyCodeEntity = "corp";
			
			String rmsId = "1001A21000000000FSY8";
//			String orgId = "0001A210000000001XQJ";
			 
			Logger.init("iufo");
			Logger.error("####开始汇总....");
			//币种参数
			String pk_curr = String.valueOf(context.getKeyMap().get("币种"));
			if(pk_curr==null||pk_curr.trim().length()<6){
				pk_curr = "1002Z0100000000001K1";
			}
			
			Logger.error("币种:"+pk_curr);
			//汇总主体pk
			
//			String mainOrgPK  = "0001A210000000001XQJ";
			String pk_entrty = "0001A210000000001XQJ";
			
//			String mainOrgPK  = "0001A2100000000XTLR1";
//			String pk_entrty = "0001A2100000000XTLR1";
//			String pk_entrty = "0001A2100000003AMOK2";  //205234A 日上免税行（上海）有限公司（合并）
//			String pk_entrty = "0001A210000000001F6Q";  //20A  中免集团  中免集团
						  
			
			
			
			
		 
			String accountPeriod = String.valueOf(context.getKeyMap().get("会计期间"));
			
			if(accountPeriod==null||accountPeriod.trim().length()<6){
				accountPeriod = "";
				
			 
			 
					int month = new UFDate().getMonth();
					int year = new UFDate().getYear();
		
					String monthStr = month + "";
					if (month < 10) {
						monthStr = "0" + monthStr;
					}
					accountPeriod = year + "-" + monthStr;
			 
			}
			
			Logger.error("期间:"+accountPeriod);
//			ReportVO repVo = UFOCacheManager.getSingleton().getReportCache().getByCode(code_report);
			
			
			String strKeyGroupPk = "00000000000000000010";
			KeyGroupVO keyGroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPk);
			
			KeyVO[] keys = keyGroupVo.getKeys();

			MeasurePubDataVO pubVO = new MeasurePubDataVO();
			pubVO.setKType(strKeyGroupPk);
			pubVO.setVer(0);
			pubVO.setKeyGroup(keyGroupVo);
			pubVO.setAccSchemePK("0001Z000000000000001");
			
			
//			 BaseDAO dao = new BaseDAO();
//			TaskVO task =  (TaskVO)dao.retrieveByPK(TaskVO.class, pk_task);
//			
			

			//期间
			for(KeyVO key:keys){
				 if(key.getCode().equals(keyCodePeriod)){
					 
						pubVO.setKeywordByName(key.getName(), accountPeriod);
						Logger.error("4.报表期间："+accountPeriod);
						break; 
				 }
				
				 
				
			}
			
//			//币种
//			 Collection<CurrtypeVO> currs =  dao.retrieveByClause(CurrtypeVO.class, " code=? ",pk_curr);
//			 
//			 
//			 if(currs.size()!=1){
//				 String message = "导入失败，查询币种失败";
//				 
//				 throw new BusinessRuntimeException(message);
//			 }
//			 
		
			
			for(KeyVO key:keys){
				if(key.getCode().equals(keyCodeCurr)){
				 
					pubVO.setKeywordByName(key.getName(), pk_curr);
					Logger.error("4.(1)报表币种："+pk_curr);
					break;
				}
				
			}
			
			//主体
			
			for(KeyVO key:keys){
				if(key.getCode().equals(keyCodeEntity)){
					 
					pubVO.setKeywordByName(key.getName(), pk_entrty);
				 
					break;
				}
				
			}
			
			String strAloneID;

			strAloneID = MeasurePubDataBO_Client.getAloneID(pubVO);
			
			Logger.error("AloneID:"+strAloneID);

			pubVO = MeasurePubDataBO_Client.findByAloneID(strKeyGroupPk,
					strAloneID);

			pubVO.setAccSchemePK("0001Z000000000000001");
 
	      boolean[] extendParams =  TotalSrvUtils.getTotalIndividualParams();
	      
	      String[] reportIds = TaskSrvUtils.getReceiveReportId(pk_task);
	      
//	      MeasurePubDataVO mainPubData, String rmsId, String orgId, String[] reportIds, boolean[] extendParams, String pk_task, String oper_user, String mainOrgPK
	      
	      String pk_user = InvocationInfoProxy.getInstance().getUserId();
	      
	      RepDataOperResultVO resultVO = TotalSrvUtils.createTotalSubResults(pubVO,rmsId,pk_entrty,reportIds,extendParams,pk_task,pk_user,pk_entrty);
	      
	  
	      if (resultVO.isOperSuccess()){
	    	  Logger.error("推送成功!");
  		}else{
  			Logger.error("推送失败:"+resultVO.getHintMessage());
  			throw new BusinessException(resultVO.getHintMessage());
  		}
		 
			
		}catch(Exception ex){
			Logger.error(ex.getMessage(),ex);
			throw new BusinessRuntimeException(ex.getMessage(),ex);
		}finally{
			Logger.init();
		}
	
		return null;
	}


}
