package nc.util.iufo.batchrule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nc.bs.framework.common.NCLocator;
import nc.itf.iufo.batchrule.IBatchRuleQueryService;
import nc.vo.bi.clusterscheduler.IJob;
import nc.vo.bi.clusterscheduler.ITask;
import nc.vo.iufo.batchrule.BatchRuleVO;
import nc.vo.iufo.data.MD5Util;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufida.iufo.pub.tools.DateUtil;
import com.ufsoft.iufo.inputplugin.inputcore.IDMaker;

public class BatchCalcJob implements IJob,Serializable {
	private static final long serialVersionUID = -8194392788249875923L;

	private String businessId=null;
	private String jobName=null;
	private BatchRuleVO[] batchRules=null;
	private String strOperUserPK=null;
	private String language = null;

	public BatchCalcJob(String batchRuleIds,String strOperUserPK, String language){
		String[] batchPKs=batchRuleIds.split(",");
//		this.businessId=IDMaker.makeID(50);
		// 根据选择的批量规则生成信息摘要
		this.businessId=MD5Util.encrypt(batchRuleIds);
		this.strOperUserPK=strOperUserPK;
		this.language = language;
		batchRules=loadBatchRules(batchPKs);
	}

	public BatchCalcJob(BatchRuleVO[] batchRules,String strOperUserPK, String language){
		this.businessId=IDMaker.makeID(50);
		this.batchRules=batchRules;
		this.strOperUserPK=strOperUserPK;
		this.language = language;
	}

	@Override
	public String getBuinessID() {
		return businessId;
	}

	@Override
	public String getJobName() {
		if (jobName==null){
			StringBuffer buf=new StringBuffer();
			for (int i=0;i<batchRules.length;i++){
				buf.append(batchRules[i].getName());
				if (i<batchRules.length-1)
					buf.append(",");
			}
			jobName=buf.toString();
		}
		// liuchun 修改，作业名称限制长度为50
		if(jobName.length() >= 50) {
			jobName = jobName.substring(0, 50);
			jobName = jobName + "...";
		}
		jobName = nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-0184")/*@res "手工批量计算:("*/ + jobName + ")";
		// end
		return jobName;
	}

	private BatchRuleVO[] loadBatchRules(String[] strBatchPKs){
		if (batchRules==null){
			try{
				IBatchRuleQueryService service=(IBatchRuleQueryService)NCLocator.getInstance().lookup(IBatchRuleQueryService.class);
				batchRules=service.loadBatchRuleByPKs(strBatchPKs);
			}catch(Exception e){
				AppDebug.debug(e);
			}
		}
		return batchRules;
	}

	@Override
	public String getModuleClzName() {
		return BatchCalcModule.class.getName();
	}

	@Override
	public ITask[] split() {
		List<ITask> vRetTask=new ArrayList<ITask>();

		String strCurDate=DateUtil.getCurDay();
		for (int i=0;i<batchRules.length;i++){
			ITask[] tasks=BatchRuleTaskUtil.loadManualTasks(batchRules[i], strOperUserPK,i, strCurDate, language);
			if (tasks!=null)
				vRetTask.addAll(Arrays.asList(tasks));
		}
		return vRetTask.toArray(new ITask[0]);
	}

	@Override
	public boolean uniqueRunning() {
		return true;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}
}