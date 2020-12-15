package nc.impl.iufo.cacu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.iufo.individual.IUFOIndividualSettingUtil;
import nc.itf.iufo.servive.ICaculateCheckSubmitService;
import nc.itf.org.IOrgConst;
import nc.pub.bi.clusterscheduler.SchedulerUtilities;
import nc.pub.bi.clusterscheduler.exception.ClusterSchedulerException;
import nc.pub.iufo.cache.IUFOCacheManager;
import nc.pub.iufo.exception.UFOSrvException;
import nc.pub.smart.util.SmartUtilities;
import nc.util.iufo.multicalc.MultiRepCalcJob;
import nc.util.iufo.pub.IDMaker;
import nc.utils.iufo.CommitUtil;
import nc.utils.iufo.TaskCheckRunUtil;
import nc.vo.bi.clusterscheduler.ITask;
import nc.vo.bi.clusterscheduler.JobQueueVO;
import nc.vo.bi.clusterscheduler.SchedulerKeys;
import nc.vo.iufo.commit.CommitActionEnum;
import nc.vo.iufo.commit.CommitActionSelRepVO;
import nc.vo.iufo.commit.CommitParamVO;
import nc.vo.iufo.commit.CommitVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.mapping.IVOMappingMeta;
import nc.vo.iufo.mapping.VOMappingMeta;
import nc.vo.iufo.task.AllCommitStateEnum;
import nc.vo.iufo.task.TaskAnnotationVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.param.TempParamVO;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.iufo.check.vo.CheckDetailVO;
import com.ufsoft.iufo.check.vo.CheckResultVO;
import com.ufsoft.iuforeport.repdatainput.LoginEnvVO;
import com.ufsoft.iuforeport.tableinput.applet.IRepDataParam;

/**
 * 
 * @author xulink
 *
 */
public class CaculateCheckSubmitServiceImpl extends BaseService implements ICaculateCheckSubmitService{
	
	private  static String SUCCESS = "Success";
	
//	public IRepDataParam repParam; 
	
	@Override
	public TempParamVO getParams(String pk_accountingbook,String year,String month) throws BusinessException{
		TempParamVO param = getTempParam(pk_accountingbook, year, month);
		 return param;
	}
	
	@Override
	public String caculate(TempParamVO param) throws BusinessException{

		//计算前清楚录入标志
		IRepDataParam repParam = getReportDataParam(param);
 
		MultiRepCalcJob job = new MultiRepCalcJob(param.getTaskid(), param.getInputKeys(), param.getRepPks(), param.getUserid(), param.getCurRmsPK());
		String jobId= "";
		job.setLanguage(param.getLangCode());
		job.setPkGroup(param.getPk_group());
		try {
			 jobId = SchedulerUtilities.addJob(job);
		} catch (ClusterSchedulerException e) {
			AppDebug.debug(e);
			e.printStackTrace();
		}
		return  jobId;
	}

	@Override
	public CheckResultVO[]  check(TempParamVO param, String jobId) throws BusinessException{
		LoginEnvVO loginEnv = getLoginEnvVO(param);
		CheckResultVO[] doCheckInTask = null;
		try {
			waitForJobComplete(jobId);
			doCheckInTask = TaskCheckRunUtil.doCheckInTask( getReportDataParam(param), loginEnv, true);
		} catch (Exception e) {
			AppDebug.debug(e);
			e.printStackTrace();
		}
		return doCheckInTask;
	}

	@Override
	public void submit(TempParamVO param,String jobId) throws BusinessException{
		IRepDataParam repParam = getReportDataParam(param);
		String[] aloneIds = new String []{repParam.getAloneID()};
		CommitVO[] commits = new CommitVO[aloneIds.length];
		for (int i = 0; i < aloneIds.length; i ++) {
			commits[i] = new CommitVO();
			commits[i].setAlone_id(aloneIds[i]);
			commits[i].setPk_task(param.getTaskid());
			commits[i].setOperator(param.getUserid());
		}
		
		List<CommitActionSelRepVO[]> selReps = new ArrayList<CommitActionSelRepVO[]>();
		
		ReportVO[] reportvos =  (ReportVO[]) IUFOCacheManager.getSingleton().getReportCache().get(param.getRepPks());
		int i = 0;
		CommitActionSelRepVO [] selRepVOs = new CommitActionSelRepVO[reportvos.length];
		for(ReportVO rep : reportvos){
			CommitActionSelRepVO comRepVO = new CommitActionSelRepVO();
			comRepVO.setPk_report(rep.getPk_report());
			comRepVO.setNoneinputflag(new UFBoolean(true));
			comRepVO.setRepname(rep.getName());
			comRepVO.setRepcode(rep.getCode());
			comRepVO.setCommitattr(2);
			comRepVO.setInputstate(1);
			comRepVO.setCommmitstate(21);
			comRepVO.setDr(0);
			selRepVOs[i] = comRepVO;
			i++;
		}
		selReps.add(selRepVOs);
		
		TaskAnnotationVO[] taskAnnotations = getTaskAnnotationsVO(repParam);
		CommitParamVO commitparam = new CommitParamVO(CommitActionEnum.ACTION_COMMIT,param.getTaskid(), param.getPk_org(), param.getCurRmsPK(), param.getUserid(), param.getPk_group(), 2,taskAnnotations,null);
		CommitUtil.commitTask(new MeasurePubDataVO[]{repParam.getPubData()},commits, selReps, commitparam);
	}

	private TaskAnnotationVO[] getTaskAnnotationsVO(IRepDataParam repParam){
		TaskAnnotationVO[] taskAnnotations = new TaskAnnotationVO[1];
		for(int i=0; i<taskAnnotations.length; i++){
			TaskAnnotationVO taskAnnotation = new TaskAnnotationVO();
			taskAnnotation.setPk_taskAnnotation(IDMaker.makeID(20));
			taskAnnotation.setBusi_type(AllCommitStateEnum.COLLECTION_VAL);

			taskAnnotation.setAnnotation_content("上报");
			String annotation_person = InvocationInfoProxy.getInstance().getUserId();
			taskAnnotation.setAnnotation_person(annotation_person);
			long busyTime = InvocationInfoProxy.getInstance().getBizDateTime();
			String annotation_time = new UFDateTime(busyTime).toStdString();
			taskAnnotation.setAnnotation_time(annotation_time);

			taskAnnotation.setAlone_id(repParam.getAloneID());
			taskAnnotation.setPk_task(repParam.getTaskPK());
			taskAnnotations[i] = taskAnnotation;
		}
		return taskAnnotations;
}



	/**
	 * 获取任务和组织体系参数
	 * @param pk_accountingbook
	 * @param year
	 * @param month
	 * @return
	 * @throws BusinessException
	 */
	private TempParamVO getTempParam(String pk_accountingbook,String year,String month) throws BusinessException{
		TempParamVO param = new TempParamVO();
		List<String> pk_orgs = new ArrayList<String>();
		pk_orgs.add(IOrgConst.GLOBEORG); 
		//任务参数
		Map<String, String> paraTask = getParaString(pk_orgs, "IUFO401");
		//组织体系参数
		Map<String, String> paraStru = getParaString(pk_orgs, "IUFO402");
		param = getTempParam(pk_accountingbook, year, month, paraTask.get(IOrgConst.GLOBEORG), paraStru.get(IOrgConst.GLOBEORG));
		return param;
	}
	/**
	 * 
	 * 获取登录信息
	 * @author xulink
	 * @Email xulink@yonyou.com
	 * @Date 2020-7-9 下午7:17:06
	 */
	private LoginEnvVO getLoginEnvVO(TempParamVO param){
		LoginEnvVO loginEnv = new LoginEnvVO();

		loginEnv.setDataExplore(true);
		loginEnv.setDataSource(IUFOIndividualSettingUtil.getDefaultDataSourceVo());
		loginEnv.setLangCode( InvocationInfoProxy.getInstance().getLangCode());
		loginEnv.setLoginUnit(param.getPk_org());
		loginEnv.setRmsPK(param.getCurRmsPK());
		return loginEnv;
	}
	
	public void waitForJobComplete(String jobId) throws UFOSrvException {
		if(jobId==null || jobId.length()<=0){
			return;
		}
		try {
			String dsName=SmartUtilities.getDefDsName();
			BaseDAO baseDao = new BaseDAO(dsName);
			IVOMappingMeta meta = VOMappingMeta.getMappingMeta(JobQueueVO.class);
			Object result=null;
			JobQueueVO job=null;
			int times = 0;
			while(true){
				if(times==120){
					AppDebug.debug("多表计算已经耗时2小时可能出现异常，审核，上报不进行执行");
					throw new UFOSrvException("多表计算已经耗时2小时可能出现异常，审核，上报不进行执行");
				}
				result=baseDao.retrieveByPK(JobQueueVO.class, meta, jobId, new String[]{"runstate"});
				if (result==null || !(result instanceof JobQueueVO)){
					break;
				}
				job=(JobQueueVO)result;
				if(job.getRunstate()==SchedulerKeys.STATE_COMPLETE ||
						job.getRunstate()==SchedulerKeys.STATE_ERROR){
					break;
				}
				synchronized(this){
					try{
						wait(60000);
					}catch(Exception e){}
				}
				times++;
			}
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage(), e);
		}
		
	}

	@Override
	public String doAll(TempParamVO param) {
		String actionInfo = "";
		try{
			actionInfo = "开始计算报表:";
			 AppDebug.error("==>"+actionInfo);
			//计算前清楚录入标志
//			IRepDataParam repParam = getReportDataParam(param);
			String calInfo = NCLocator.getInstance().lookup(ICaculateCheckSubmitService.class).caculate_RequiresNew(param);
//			String calInfo =SUCCESS;
			if(SUCCESS.equals(calInfo)){
				actionInfo = "计算完成,开始进行审核:";
				
				
				 AppDebug.error("==>"+actionInfo);
				
				LoginEnvVO loginEnv = getLoginEnvVO(param);
				CheckResultVO[] doCheckInTask = TaskCheckRunUtil.doCheckInTask( getReportDataParam(param), loginEnv, true);
					
					
			    	  for(CheckResultVO result : doCheckInTask){
			    		  if(result.getCheckState()!=3){
			    			  
			    			  actionInfo = actionInfo+"报表审核不通过,未提交任务.";
			    			  AppDebug.error("报表审核不通过,未提交任务.");  
			    			  StringBuffer sb = new StringBuffer();
			    			  for(CheckDetailVO detail: result.getDetailVO()){
			    				  sb.append("ErrInfo:"+detail.toString()).append(";");
			    				
			    				 
			    			  }
			    			  actionInfo = actionInfo+"审核错误信息:"+sb.toString();
			    			  AppDebug.error("==>"+actionInfo);
			    			return   actionInfo;
			    			 
			    		  }
			    	  }
				 
				 
				
				
				actionInfo = "计算与审核完成,开始提交:";
				 AppDebug.error("==>"+actionInfo);
				String submitInfo = NCLocator.getInstance().lookup(ICaculateCheckSubmitService.class).submit_RequiresNew(param);
				if(SUCCESS.equals(submitInfo)){
					return SUCCESS;
				}else{
					 AppDebug.error("==>"+actionInfo);
					return actionInfo+"发生错误,"+submitInfo;
					
				}
			}else{
				 AppDebug.error("==>"+actionInfo);
				return actionInfo+"发生错误,"+calInfo;
			}
			
			
			
		}catch(Exception ex){
			 AppDebug.error("==>"+actionInfo);
			 Logger.init("iufo");
			 Logger.error(ex);
			actionInfo = actionInfo +"发生错误,错误信息:"+ex.getMessage();
			return actionInfo;
		}finally{
			Logger.init();
		}
		
		
	
	}

	@Override
	public String caculate_RequiresNew(TempParamVO param) {
		
		try{
			MultiRepCalcJob job = new MultiRepCalcJob(param.getTaskid(), param.getInputKeys(), param.getRepPks(), param.getUserid(), param.getCurRmsPK());
			 
			job.setLanguage(param.getLangCode());
			job.setPkGroup(param.getPk_group());
			
			ITask[] itasks = job.split();
			for(ITask itask:itasks){
				itask.execute();
			}
			return SUCCESS;
			
		}catch(Exception ex){
			throw new BusinessRuntimeException("计算错误:"+ex.getMessage(),ex);
		}
		
		 
	}
	
	

	@Override
	public String submit_RequiresNew(TempParamVO param)  {
		try{
			
		IRepDataParam repParam = getReportDataParam(param);
			String[] aloneIds = new String []{repParam.getAloneID()};
			CommitVO[] commits = new CommitVO[aloneIds.length];
			for (int i = 0; i < aloneIds.length; i ++) {
				commits[i] = new CommitVO();
				commits[i].setAlone_id(aloneIds[i]);
				commits[i].setPk_task(param.getTaskid());
				commits[i].setOperator(param.getUserid());
			}
			
			List<CommitActionSelRepVO[]> selReps = new ArrayList<CommitActionSelRepVO[]>();
			
			ReportVO[] reportvos =  (ReportVO[]) IUFOCacheManager.getSingleton().getReportCache().get(param.getRepPks());
			int i = 0;
			CommitActionSelRepVO [] selRepVOs = new CommitActionSelRepVO[reportvos.length];
			for(ReportVO rep : reportvos){
				CommitActionSelRepVO comRepVO = new CommitActionSelRepVO();
				comRepVO.setPk_report(rep.getPk_report());
				comRepVO.setNoneinputflag(new UFBoolean(true));
				comRepVO.setRepname(rep.getName());
				comRepVO.setRepcode(rep.getCode());
				comRepVO.setCommitattr(2);
				comRepVO.setInputstate(1);
				comRepVO.setCommmitstate(21);
				comRepVO.setDr(0);
				selRepVOs[i] = comRepVO;
				i++;
			}
			selReps.add(selRepVOs);
			
			TaskAnnotationVO[] taskAnnotations = getTaskAnnotationsVO(repParam);
			CommitParamVO commitparam = new CommitParamVO(CommitActionEnum.ACTION_COMMIT,param.getTaskid(), param.getPk_org(), param.getCurRmsPK(), param.getUserid(), param.getPk_group(), 2,taskAnnotations,null);
			CommitUtil.commitTask(new MeasurePubDataVO[]{repParam.getPubData()},commits, selReps, commitparam);
			return SUCCESS;
		}catch(Exception ex){
			Logger.init("iufo");
			Logger.error(ex);
			return "提交失败!错误信息:"+ex.getMessage();
		}finally{
			Logger.init();
		}
		
	}
}
