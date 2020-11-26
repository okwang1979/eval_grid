package nc.impl.tbb.backplugin;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.pub.pa.PreAlertObject;
import nc.bs.pub.taskcenter.BgWorkingContext;
import nc.bs.pub.taskcenter.IBackgroundWorkPlugin;
import nc.itf.epmp.plan.txtdata.IPlanTxtValueManager;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;

public class PushTbbTaskDataWorkPlugin implements IBackgroundWorkPlugin{

	@Override
	public PreAlertObject executeTask(BgWorkingContext context)
			throws BusinessException {
		try{
			Logger.init("iufo");
		 
			String taskDefName = String.valueOf(context.getKeyMap().get("��������"));
			Logger.error("############��ʼ���������ƶ���"+taskDefName);
			if(taskDefName==null||taskDefName.trim().isEmpty()||"null".contains(taskDefName)){
				throw new BusinessRuntimeException("��������δ¼�룡");
			}
			IPlanTxtValueManager manager = NCLocator.getInstance().lookup(IPlanTxtValueManager.class);
			
			if(taskDefName.contains(",")) {
				String[] taskNames = taskDefName.split(",");
				for(String currentTaskName:taskNames) {
					taskDefName = currentTaskName;
					if(taskDefName==null||taskDefName.trim().isEmpty()||"null".contains(taskDefName)){
						 continue;
					}
					manager.getDataFromTask(taskDefName);
				}
				
			}else {
				manager.getDataFromTask(taskDefName);
			}
			
			
			Logger.error("�������");
		}catch(Exception ex){
			Logger.error(ex.getMessage(),ex);
			throw new BusinessRuntimeException(ex.getMessage(),ex);
		}finally{
			Logger.init();
		}
	
		return null;
	}

}
