package nc.impl.hbbb.hbflowexec;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.hbbb.hbflow.FunStepPreInfo;
import nc.bs.hbbb.hbflowexec.HBStepExecFactory;
import nc.bs.hbbb.hbflowexec.IHBStepExec;
import nc.bs.hbbb.hbflowexec.IUfocFuncodeConst;
import nc.bs.uap.lock.PKLock;
import nc.bs.uif2.LockFailedException;
import nc.itf.hbbb.hbflowexec.IHBFlowExecSrv;
import nc.jdbc.framework.SQLParameter;
import nc.pub.hbbb.exception.UFOCUnThrowableException;
import nc.pub.iufo.data.thread.IufoThreadLocalUtil;
import nc.pub.iufo.exception.UFOSrvException;
import nc.util.hbbb.pub.HBPubItfService;
import nc.util.iufo.pub.UFOString;
import nc.vo.gl.glreporttools.OrgCacheForUFO;
import nc.vo.hbbb.hbflow.AggHBStepVO;
import nc.vo.hbbb.hbflow.EnumStepStatus;
import nc.vo.hbbb.hbflow.HBDefaultInvestSetVO;
import nc.vo.hbbb.hbflow.HBFlowExecVO;
import nc.vo.hbbb.hbflow.HBStepStatusVO;
import nc.vo.hbbb.hbflow.HBStepVO;
import nc.vo.hbbb.hbflow.KeyValVO;
import nc.vo.hbbb.hbflow.PreStepVO;
import nc.vo.hbbb.schemekey.SchemeKeyQryVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.util.AuditInfoUtil;
import nc.vo.util.BDPKLockData;
import nc.vo.util.BDPKLockUtil;

import com.ufida.iufo.pub.tools.AppDebug;
import com.ufsoft.iufo.resource.ResourceUtil;

/**
 * 合并流执行
 * @author jiaah
 *
 */
public class HBFlowExecImpl implements IHBFlowExecSrv{

	public static final String LOCK_HB_KEY="UFOC_HBEXECLOCK";
	@Override
	public void execHBFlowByOrg(SchemeKeyQryVO keyQryVO,
			HBFlowExecVO[] execAllVOs, AggHBStepVO[] stepVO)
			throws UFOSrvException {
		int iTask = 0;
		List<AggHBStepVO> aggStepsWithOutVouch = new ArrayList<AggHBStepVO>();

		for(AggHBStepVO aggvo: stepVO){
			String funnode = ((HBStepVO)aggvo.getParentVO()).getFunnode();
			if(funnode.equals(IUfocFuncodeConst.DXVOUCHADD) || funnode.equals(IUfocFuncodeConst.SEPVOUCHADD)
					||funnode.equals(IUfocFuncodeConst.HBVOUCHADD)||funnode.equals(IUfocFuncodeConst.INVESTCONFIRM) ){
				continue;
			}
			aggStepsWithOutVouch.add(aggvo);
		}

		iTask = getITaskCount(execAllVOs, aggStepsWithOutVouch);

		if(isStepAllLock(execAllVOs, aggStepsWithOutVouch))
			throw new UFOSrvException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830005-0402")/*@res "选择的所有步骤都被锁定或不涉及"*/);
		
		for(HBFlowExecVO execAllVO : execAllVOs){
			
			//获取默认币种
			List<KeyValVO> defaultKeyValVOs = getKeyValBySchemeAndOrg(keyQryVO.getSchemevo().getPk_hbscheme(),execAllVO.getPk_org()); 
			
			keyQryVO.setContrastorg(execAllVO.getPk_org());
			Map<String, String> map = keyQryVO.getKeymap();
			map.put(KeyVO.CORP_PK, execAllVO.getPk_org());
			if (defaultKeyValVOs.size()>0) {
				for (KeyValVO kv:defaultKeyValVOs) {
					map.put(kv.getPk_key(), kv.getValue());
				}
			}
			keyQryVO.setKeymap(map);
			Map<String, HBStepStatusVO> statusVos = execAllVO.getStatusMap();
			for(AggHBStepVO aggvo: aggStepsWithOutVouch){
				//跳过所有锁定或不涉及的步骤
				HBStepStatusVO vo = statusVos.get(((HBStepVO)aggvo.getParentVO()).getPk_hbstep());
				if((vo.getStepstatus().equals(EnumStepStatus.Lock.ordinal()+"") )
						|| (vo.getStepstatus().equals(EnumStepStatus.UNUSE.ordinal()+""))){
					continue;
				}
				try {
					String funnode = ((HBStepVO)aggvo.getParentVO()).getFunnode();
					String operClassName = FunStepPreInfo.getInstance().getFunStepByNode(funnode).getEnterclass();
					IHBStepExec bizOper = HBStepExecFactory.getSingleton().getRecvBizOper(operClassName);
					if(bizOper != null) {
						// 执行
						bizOper.executed(keyQryVO, execAllVO, aggvo);
						// 解除全部动态锁
						PKLock.getInstance().releaseDynamicLocks();
					} else {
						throw new UFOSrvException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830005-0403")/*@res "业务操作对象初始化失败"*/ + operClassName);
					}
				} catch (Exception e) {
					AppDebug.error(e.getMessage());
					if(iTask == 1 ||!(e instanceof UFOCUnThrowableException)){//只选中了一个步骤或
						throw new UFOSrvException(e.getMessage());
					}
				}
			}
		}
		
	}

	@Override
	public void execHBFlow(SchemeKeyQryVO keyQryVO, HBFlowExecVO[] execAllVO,AggHBStepVO[] stepVO)
			throws UFOSrvException {
		try{
			IufoThreadLocalUtil.openCach();
			int iTask = 0;
			List<AggHBStepVO> aggStepsWithOutVouch = new ArrayList<AggHBStepVO>();

			for(AggHBStepVO aggvo: stepVO){
				String funnode = ((HBStepVO)aggvo.getParentVO()).getFunnode();
				if(funnode.equals(IUfocFuncodeConst.DXVOUCHADD) || funnode.equals(IUfocFuncodeConst.SEPVOUCHADD)
						||funnode.equals(IUfocFuncodeConst.HBVOUCHADD)||funnode.equals(IUfocFuncodeConst.INVESTCONFIRM) ){
					continue;
				}
				aggStepsWithOutVouch.add(aggvo);
			}

			//组织自下而上排序执行，末级组织优先执行
			Arrays.sort(execAllVO,new Comparator<HBFlowExecVO>(){
				@Override
				public int compare(HBFlowExecVO o1, HBFlowExecVO o2) {
	                return o2.getInnercode().length()- o1.getInnercode().length();
				}
			});

			iTask = getITaskCount(execAllVO, aggStepsWithOutVouch);

			if(isStepAllLock(execAllVO, aggStepsWithOutVouch))
				throw new UFOSrvException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830005-0402")/*@res "选择的所有步骤都被锁定或不涉及"*/);

			for(HBFlowExecVO execVo: execAllVO){
				//获取默认币种
				List<KeyValVO> defaultKeyValVOs = getKeyValBySchemeAndOrg(keyQryVO.getSchemevo().getPk_hbscheme(),execVo.getPk_org()); 
				keyQryVO.setContrastorg(execVo.getPk_org());
				Map<String, String> map = keyQryVO.getKeymap();
				map.put(KeyVO.CORP_PK, execVo.getPk_org());
				if (defaultKeyValVOs.size()>0) {
					for (KeyValVO kv:defaultKeyValVOs) {
						map.put(kv.getPk_key(), kv.getValue());
					}
				}
				keyQryVO.setKeymap(map);
				Map<String, HBStepStatusVO> statusVos = execVo.getStatusMap();
				for(AggHBStepVO aggvo: aggStepsWithOutVouch){
					//跳过所有锁定或不涉及的步骤
					HBStepStatusVO vo = statusVos.get(((HBStepVO)aggvo.getParentVO()).getPk_hbstep());
					if((vo.getStepstatus().equals(EnumStepStatus.Lock.ordinal()+"") )
							|| (vo.getStepstatus().equals(EnumStepStatus.UNUSE.ordinal()+""))){
						continue;
					}
					try {
						String funnode = ((HBStepVO)aggvo.getParentVO()).getFunnode();
						String operClassName = FunStepPreInfo.getInstance().getFunStepByNode(funnode).getEnterclass();
						IHBStepExec bizOper = HBStepExecFactory.getSingleton().getRecvBizOper(operClassName);
						if(bizOper != null) {
							// 执行
							BDPKLockUtil.lockString(LOCK_HB_KEY + ((HBStepVO)aggvo.getParentVO()).getPk_hbstep() + execVo.getAloneid());
							bizOper.executed(keyQryVO, execVo, aggvo);
							// 解除全部动态锁
							PKLock.getInstance().releaseDynamicLocks();
						} else {
							throw new UFOSrvException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0","01830005-0403")/*@res "业务操作对象初始化失败"*/ + operClassName);
						}
					} catch (Exception e) {
						AppDebug.debug(e);
						if(iTask == 1 ){//只选中了一个步骤或
							if(e instanceof LockFailedException)
								throw new UFOSrvException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0141"));
							else
								throw new UFOSrvException(e.getMessage());
						}
					}
				}
			}
			
		}catch(Exception ex){
			throw new BusinessRuntimeException(ex.getMessage(),ex);
		}finally{
			IufoThreadLocalUtil.clean();
			
		}

	}

	private int getITaskCount(HBFlowExecVO[] execAllVO,
			List<AggHBStepVO> aggStepsWithOutVouch) {
		int iTask = 0;
		for(HBFlowExecVO execVo: execAllVO){
			Map<String, HBStepStatusVO> statusVos = execVo.getStatusMap();
			for(AggHBStepVO aggvo: aggStepsWithOutVouch){
				//跳过所有锁定或不涉及的步骤
				HBStepStatusVO vo = statusVos.get(((HBStepVO)aggvo.getParentVO()).getPk_hbstep());
				if((vo.getStepstatus().equals(EnumStepStatus.Lock.ordinal()+"") )
						|| (vo.getStepstatus().equals(EnumStepStatus.UNUSE.ordinal()+""))){
					continue;
				}
				iTask++;
			}
		}
		return iTask;
	}

	/**
	 * 返回所有的步骤是否都锁定或不涉及
	 * @param execAllVO
	 * @param aggStepsWithOutVouch
	 * @return
	 */
	private boolean isStepAllLock(HBFlowExecVO[] execAllVO,
			List<AggHBStepVO> aggStepsWithOutVouch) {
		boolean isAllLock = true;
		for(HBFlowExecVO execVo: execAllVO){
			Map<String, HBStepStatusVO> statusVos = execVo.getStatusMap();
			for(AggHBStepVO aggvo: aggStepsWithOutVouch){
				HBStepStatusVO vo = statusVos.get(((HBStepVO)aggvo.getParentVO()).getPk_hbstep());
				if((!vo.getStepstatus().equals(EnumStepStatus.Lock.ordinal()+"") )
						&& (!vo.getStepstatus().equals(EnumStepStatus.UNUSE.ordinal()+""))){
					isAllLock = false;
				}
			}
		}
		return isAllLock;
	}

	/**
	 * 更新步骤的状态：唯一确定一个状态的是aloneid + pk_hbstep
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void updateStepStatus(HBFlowExecVO execAllVO, AggHBStepVO stepVO,
			String stepstatus, String error) throws UFOSrvException{
		try {
			BaseDAO dao = new BaseDAO();
			String aloneid = execAllVO.getAloneid();
			String pk_hbstep = ((HBStepVO)stepVO.getParentVO()).getPk_hbstep();

			SQLParameter params = new SQLParameter();
			params.addParam(aloneid);
			params.addParam(pk_hbstep);
			String cond = "aloneid = ? and pk_hbstep = ?";
			Collection<HBStepStatusVO> statusResult = dao.retrieveByClause(HBStepStatusVO.class, cond, params);
			HBStepStatusVO oldvo = statusResult.toArray(new HBStepStatusVO[statusResult.size()])[0];
			HBStepStatusVO newvo = (HBStepStatusVO) oldvo.clone();
			BDPKLockUtil.lock(new BDPKLockData(newvo));
			newvo.setPrestatus(oldvo.getStepstatus());
			newvo.setStepstatus(stepstatus);
			newvo.setExceptioninfo(error);

			newvo.setExecutime(AuditInfoUtil.getCurrentTime());
			newvo.setExecutor(AuditInfoUtil.getCurrentUser());
			dao.updateVO(newvo);
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		} catch (BusinessException e) {
			if(e instanceof LockFailedException){
				throw new UFOSrvException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0141")/* @res "其他用户正在执行操作,请稍后再试!" */,e);
			}
			else
				throw new UFOSrvException(e.getMessage(), e);
		}
	}

	@Override
	public void batchLockStepStatus(HBFlowExecVO[] execAllVO,AggHBStepVO[] steps)
			throws UFOSrvException {
		try {
			BaseDAO dao = new BaseDAO();
			List<HBStepStatusVO> lstStatus = new ArrayList<HBStepStatusVO>();
			for(HBFlowExecVO execVO : execAllVO){
				Map<String, HBStepStatusVO> statusmap = execVO.getStatusMap();
				for(AggHBStepVO vo : steps){
					HBStepVO parentvo = (HBStepVO) vo.getParentVO();
					HBStepStatusVO statusVO = statusmap.get(parentvo.getPk_hbstep());
					BDPKLockUtil.lockString(LOCK_HB_KEY + parentvo.getPk_hbstep() + statusVO.getAloneid());
					//制单和审核类节点：只要不是锁定和未执行的状态就可以直接锁定
					if(parentvo.getFunnode().equals(IUfocFuncodeConst.DXVOUCHADD)
							||parentvo.getFunnode().equals(IUfocFuncodeConst.SEPVOUCHADD)
							||parentvo.getFunnode().equals(IUfocFuncodeConst.SEPADJUST)//增加调整节点允许直接锁定；jiaah
							||parentvo.getFunnode().equals(IUfocFuncodeConst.HBADJUST)
							||parentvo.getFunnode().equals(IUfocFuncodeConst.INVESTCONFIRM)
							||parentvo.getFunnode().equals(IUfocFuncodeConst.HBVOUCHADD)
							||parentvo.getFunnode().equals(IUfocFuncodeConst.DXVOUCHCHECK)
							||parentvo.getFunnode().equals(IUfocFuncodeConst.SEPVOUCHCHECK)
							||parentvo.getFunnode().equals(IUfocFuncodeConst.HBVOUCHCHECK)){
						if(!statusVO.getStepstatus().equals(EnumStepStatus.Lock.ordinal()+"")
								&& !statusVO.getStepstatus().equals(EnumStepStatus.UNUSE.ordinal()+"")){
							statusVO.setPrestatus(statusVO.getStepstatus());//锁定时记录锁定前一状态
							statusVO.setStepstatus(EnumStepStatus.Lock.ordinal() +"");
							statusVO.setExecutime(AuditInfoUtil.getCurrentTime());
							statusVO.setExecutor(AuditInfoUtil.getCurrentUser());
							lstStatus.add(statusVO);
						}
					}else{
						//只有执行成功的状态才可以锁定
						if(statusVO.getStepstatus().equals(EnumStepStatus.Success.ordinal()+"")){
							statusVO.setPrestatus(statusVO.getStepstatus());//锁定时记录锁定前一状态
							statusVO.setStepstatus(EnumStepStatus.Lock.ordinal() +"");
							statusVO.setExecutime(AuditInfoUtil.getCurrentTime());
							statusVO.setExecutor(AuditInfoUtil.getCurrentUser());
							lstStatus.add(statusVO);
						}
					}
				}
			}
			dao.updateVOArray(lstStatus.toArray(new HBStepStatusVO[lstStatus.size()]));
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		} catch (BusinessException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("pub_0", "01830008-0141"));
		}

	}

	@Override
	public Object[] batchUnLockStepStatus(SchemeKeyQryVO keyQryVO,HBFlowExecVO[] execAllVO,AggHBStepVO[] steps)
			throws UFOSrvException {
		try {
			BaseDAO dao = new BaseDAO();
			List<HBStepStatusVO> lstStatus = new ArrayList<HBStepStatusVO>();

			//同级之间的串行解锁
			Set<String> allUnlockSteps = getPreStepStatus(steps);
			for(HBFlowExecVO execVO : execAllVO){
				Map<String, HBStepStatusVO> statusmap = execVO.getStatusMap();
				if(allUnlockSteps.size() > 0){
					for(String s: allUnlockSteps){
						HBStepStatusVO preStatusVO = statusmap.get(s);
						if(preStatusVO.getStepstatus().equals(EnumStepStatus.Lock.ordinal()+"")){
							preStatusVO.setStepstatus(EnumStepStatus.Unlock.ordinal()+"");
							preStatusVO.setExecutime(AuditInfoUtil.getCurrentTime());
							preStatusVO.setExecutor(AuditInfoUtil.getCurrentUser());
							lstStatus.add(preStatusVO);
						}
					}
				}
			}
			dao.updateVOArray(lstStatus.toArray(new HBStepStatusVO[lstStatus.size()]));

			Object[] returnSteps = new Object[2];
			returnSteps[0] = new Object[]{execAllVO,allUnlockSteps.toArray(new String[allUnlockSteps.size()])};
			//处理上级级的解锁
			returnSteps[1] = handleSubOrgUnlock(keyQryVO, execAllVO, allUnlockSteps);
			return returnSteps;
		} catch (Exception e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		}
	}

	public void batchUnLockStepStatus(Collection<HBStepStatusVO> statusVOs) throws UFOSrvException{
		if(statusVOs == null || statusVOs.size() == 0)
			return;
		try {
			BaseDAO dao = new BaseDAO();
			List<HBStepStatusVO> lstStatus = new ArrayList<HBStepStatusVO>();
			for(HBStepStatusVO vo : statusVOs){
				String status = EnumStepStatus.Lock.ordinal() + "";
				//依赖个别报表状态的步骤为锁定状态,
				if(status.equals(vo.getStepstatus())){
					vo.setStepstatus(EnumStepStatus.Unlock.ordinal()+"");
					vo.setExecutime(AuditInfoUtil.getCurrentTime());
					vo.setExecutor(AuditInfoUtil.getCurrentUser());
					lstStatus.add(vo);
				}
			}
			dao.updateVOArray(lstStatus.toArray(new HBStepStatusVO[lstStatus.size()]));
		} catch (Exception e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		}
	}

	private Object[] handleSubOrgUnlock(SchemeKeyQryVO keyQryVO,
			HBFlowExecVO[] execAllVO, Set<String> allUnlockSteps)throws Exception {
		String pk_hb = null;//合并步骤
		String pk_hbadj = null;//合并调整步骤
		AggHBStepVO[] allSteps = new HBFlowExecQryImpl().getAllHBStepsByHBFlow(execAllVO[0].getPk_hbflow());
		Map<String, UFBoolean> map = new HashMap<String, UFBoolean>();//是否需要校验下级组织的步骤
		boolean isContainCheckSub = false;

		if(allSteps != null && allSteps.length >0){
			for(AggHBStepVO vo : allSteps){
				HBStepVO parentVO = (HBStepVO) vo.getParentVO();
				if(parentVO.getChecksuborg() != null && parentVO.getChecksuborg().booleanValue() == true)
					isContainCheckSub = true;
				map.put(parentVO.getPk_hbstep(), parentVO.getChecksuborg());
				if(parentVO.getFunnode().equals(IUfocFuncodeConst.HBREPORT)){
					pk_hb = parentVO.getPk_hbstep();
				}else if(parentVO.getFunnode().equals(IUfocFuncodeConst.HBADJUST)){
					pk_hbadj = parentVO.getPk_hbstep();
				}
			}
			if(pk_hbadj != null && pk_hb != null)
				pk_hb = null;
		}

		//上下级之间的解锁:条件1：需要解锁的步骤中存在合并或合并调整节点，条件2：寻找直接上级的对账或合并步骤使其解锁
		if(((pk_hb != null && allUnlockSteps.contains(pk_hb))
				|| (pk_hbadj != null && allUnlockSteps.contains(pk_hbadj)))
				&& isContainCheckSub == true){
			//合并调整步骤：只需要解锁当前直接上级
			//合并步骤：需要递归解锁直接上级
			HBFlowExecVO[] execs = HBPubItfService.getRemoteHBExecQrySrv().getHBStepStatusVos(keyQryVO.getPk_hbrepstru(), keyQryVO);
			Map<String, HBFlowExecVO> execMap = new HashMap<String, HBFlowExecVO>();
			for(HBFlowExecVO exevo : execs){
				execMap.put(exevo.getInnercode(), exevo);
			}

			Set<String> innercodes = new HashSet<String>();
			if(pk_hbadj != null){
				for(HBFlowExecVO vo:execAllVO){
					int innercode = vo.getInnercode().length() -4;
					if(innercode != 0){
						String direct = vo.getInnercode().substring(0, innercode);
						innercodes.add(direct);
					}
				}

			}else if(pk_hb != null){
				for(HBFlowExecVO vo:execAllVO){
					String init = vo.getInnercode();
					for(int i = 0 ; i < init.length(); i++){
						int innercode = init.length() -4;
						if(innercode == 0)
							break;
						String direct = init.substring(0, innercode);
						innercodes.add(direct);
						init = direct;
					}

				}
			}
			if(innercodes.size() > 0){
				List<HBStepStatusVO> lstSubOrgs = new ArrayList<HBStepStatusVO>();
				List<HBFlowExecVO> returnExecVO = new ArrayList<HBFlowExecVO>();
				for(String s : innercodes){
					HBFlowExecVO vo = execMap.get(s);
					if(vo == null)
						continue;
					Map<String, HBStepStatusVO> statusmap = vo.getStatusMap();
					for(Map.Entry<String, HBStepStatusVO> enter : statusmap.entrySet()){
						String pk_hbstep = enter.getKey();
						if(map.get(pk_hbstep).booleanValue() == true){
							HBStepStatusVO stavo = enter.getValue();
							if(stavo.getStepstatus().equals(EnumStepStatus.Lock.ordinal()+"")){
								stavo.setStepstatus(EnumStepStatus.Unlock.ordinal()+"");
								stavo.setExecutime(AuditInfoUtil.getCurrentTime());
								stavo.setExecutor(AuditInfoUtil.getCurrentUser());
								lstSubOrgs.add(stavo);
							}
						}
					}
					returnExecVO.add(vo);
				}
				if(lstSubOrgs.size() > 0){
					BaseDAO dao = new BaseDAO();
				 	dao.updateVOArray(lstSubOrgs.toArray(new HBStepStatusVO[lstSubOrgs.size()]));
				 	Set<String> pks = map.keySet();
				 	return new Object[]{returnExecVO.toArray(new HBFlowExecVO[returnExecVO.size()]),pks.toArray(new String[pks.size()])};
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private Set<String> getPreStepStatus(AggHBStepVO[] steps){
		BaseDAO dao = new BaseDAO();
		Set<String> hbStep = new HashSet<String>();
		Set<String> allUnlockPks = new HashSet<String>();
		for(AggHBStepVO vo : steps){
			HBStepVO parentvo = (HBStepVO) vo.getParentVO();
			allUnlockPks.add(parentvo.getPk_hbstep());
			//当前状态是锁定状态才允许解锁
			//把它作为前置任务的步骤都解锁（未考虑上下级关系）
			SQLParameter params = new SQLParameter();
			params.addParam(parentvo.getPk_hbstep());
			try {
				Collection<PreStepVO> c = dao.retrieveByClause(PreStepVO.class,"pk_hbstep = ?" , params);
				if(c != null && c.size() > 0){
					for(PreStepVO preStep : c){
						hbStep.add(preStep.getPrestep());
					}
				}
			} catch (DAOException e) {
				AppDebug.debug(e);
			}
		}
		for(int i = 0 ; i < 100 ; i++){
			if(hbStep.size() == 0)
				break;

			allUnlockPks.addAll(hbStep);
			hbStep = getPrePks(hbStep);
		}
		return allUnlockPks;
	}

	@SuppressWarnings("unchecked")
	private Set<String> getPrePks(Set<String> hbStep){
		BaseDAO dao = new BaseDAO();
		Set<String> hb = new HashSet<String>();
		try {
			for(String s : hbStep){
				//递归查找把它作为前置任务的步骤都解锁（未考虑上下级关系）
				SQLParameter params = new SQLParameter();
				params.addParam(s);
				Collection<PreStepVO> c = dao.retrieveByClause(PreStepVO.class,"pk_hbstep = ?" , params);
				if(c != null && c.size() > 0){
					for(PreStepVO preStep : c){
						hb.add(preStep.getPrestep());
					}
				}
			}
		} catch (DAOException e) {
			AppDebug.debug(e);
		}
		return hb;
	}

	@Override
	public void updateStepStatusByVo_RequiresNew(HBStepStatusVO[] statusVO, String stepstatus, String error)
			throws UFOSrvException {
		try {
			BaseDAO dao = new BaseDAO();
			List<HBStepStatusVO> status = new ArrayList<HBStepStatusVO>();
			for(HBStepStatusVO vo : statusVO){
				HBStepStatusVO newvo = (HBStepStatusVO) vo.clone();
				newvo.setStepstatus(stepstatus);
				newvo.setExceptioninfo(error);
				newvo.setExecutime(AuditInfoUtil.getCurrentTime());
				newvo.setExecutor(AuditInfoUtil.getCurrentUser());
				newvo.setPrestatus(vo.getStepstatus());
				status.add(newvo);
			}
			dao.updateVOArray(status.toArray(new HBStepStatusVO[status.size()]));
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		}
	}

	@Override
	public void saveDefaultInvestSet(List<HBDefaultInvestSetVO> investSetList, String pk_hbscheme, String pk_investor,String funnodeType)
			throws UFOSrvException {
		BaseDAO dao = new BaseDAO();
		try {
			StringBuilder strCond = new StringBuilder();
			strCond.append(HBDefaultInvestSetVO.PK_HBSCHEME);
			strCond.append("='");
			strCond.append(pk_hbscheme);
			strCond.append("' and ");
			strCond.append(HBDefaultInvestSetVO.PK_INVESTOR);
			strCond.append("='");
			strCond.append(pk_investor);
			strCond.append("' and ");
			strCond.append(HBDefaultInvestSetVO.FUNNODETYPE);
			strCond.append("='");
			strCond.append(funnodeType);
			strCond.append("'");
			//先删除合并方案投资方对应的数据，再插入对应的新增数据
			dao.deleteByClause(HBDefaultInvestSetVO.class, strCond.toString());
			dao.insertVOList(investSetList);
		} catch (Exception e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage(), e);
		}
	}

	public void saveKeyVal(List<KeyValVO> addDefaultKVList) throws UFOSrvException{
		List<String> pk_orgs = new ArrayList<String>();
		String pk_hbscheme = addDefaultKVList.get(0).getPk_hbscheme();
 		//先删后加
		for (KeyValVO kv:addDefaultKVList) {
			pk_orgs.add(kv.getPk_org());
		}
		deleteKeyValVOs(pk_hbscheme,pk_orgs);
		
		List<KeyValVO> KVList = new ArrayList<KeyValVO>();
		for (KeyValVO vo:addDefaultKVList) {
			if (vo.getValue()==null||vo.getValue().toString().equals("")) {
				continue;
			}
			KVList.add(vo);
		}
		try {
			BaseDAO dao = new BaseDAO();
			dao.insertVOArrayWithPK(KVList.toArray(new KeyValVO[0]));
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<KeyValVO> getKeyValBySchemeAndOrg(String pk_hbscheme,String pk_org) throws UFOSrvException {
		String con = "pk_hbscheme = ? and pk_org = ?";
		SQLParameter params = new SQLParameter();
		params.addParam(pk_hbscheme);
		params.addParam(pk_org);
		List<KeyValVO> defaultKeyValVOs = new ArrayList<KeyValVO>();
		try {
			defaultKeyValVOs = (List<KeyValVO>) new BaseDAO().retrieveByClause(KeyValVO.class, con, params);
		} catch (DAOException e) {
			AppDebug.debug(e);
		}
		return defaultKeyValVOs;
	} 
	
	private void deleteKeyValVOs(String pk_hbscheme,List<String> pk_orgs) {
		BaseDAO dao = new BaseDAO();
		String wherestr = "pk_hbscheme = ? and pk_org in " + UFOString.getSqlStrByStrList(pk_orgs);
		SQLParameter params = new SQLParameter();
		params.addParam(pk_hbscheme);
		try {
			dao.deleteByClause(KeyValVO.class, wherestr, params);
		} catch (DAOException e) {
			AppDebug.debug(e);
		}
	}
	
	public void deleteKeyVal(String pk_hbscheme, String pk_keyword, List<String> orgCodeList){
		BaseDAO dao = new BaseDAO();
		List<String> pk_orgs = new ArrayList<String>();
		for (String code:orgCodeList) {
			String pk_org = OrgCacheForUFO.getInstance().getOrgPk(code);
			pk_orgs.add(pk_org);
		}
		String wherestr = "pk_hbscheme = ? and pk_key = ? and pk_org in " + UFOString.getSqlStrByStrList(pk_orgs);
		SQLParameter params = new SQLParameter();
		params.addParam(pk_hbscheme);
		params.addParam(pk_keyword);
		try {
			dao.deleteByClause(KeyValVO.class, wherestr, params);
		} catch (DAOException e) {
			AppDebug.debug(e);
		}
	}
	
	public void updateKeyVal(List<KeyValVO> upDefaultKVList) throws UFOSrvException{
		try {
			BaseDAO dao = new BaseDAO();
			dao.updateVOArray(upDefaultKVList.toArray(new KeyValVO[upDefaultKVList.size()]));
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		}
	}

	@Override
	public void batchUpdateStepStatus(HBFlowExecVO[] execAllVO,
			AggHBStepVO[] steps, String status) throws UFOSrvException {
		try {
			BaseDAO dao = new BaseDAO();
			List<HBStepStatusVO> lstStatus = new ArrayList<HBStepStatusVO>();
			for(HBFlowExecVO execVO : execAllVO){
				Map<String, HBStepStatusVO> statusmap = execVO.getStatusMap();
				for(AggHBStepVO vo : steps){
					HBStepVO parentvo = (HBStepVO) vo.getParentVO();
					HBStepStatusVO statusVO = statusmap.get(parentvo.getPk_hbstep());
					if(!statusVO.getStepstatus().equals(EnumStepStatus.UNUSE.ordinal()+"")){
						statusVO.setPrestatus(statusVO.getStepstatus());
						statusVO.setStepstatus(status);
						statusVO.setExecutime(AuditInfoUtil.getCurrentTime());
						statusVO.setExecutor(AuditInfoUtil.getCurrentUser());
						lstStatus.add(statusVO);
					}
				}
			}
			dao.updateVOArray(lstStatus.toArray(new HBStepStatusVO[lstStatus.size()]));
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		}
	}
	
	/**
	 * 读取文件hbFlow.properties，得到起调度任务数
	 * 
	 * @create by zhoushuang at 2014-6-9,上午9:38:43
	 *
	 * @return
	 */
	public int getTaskCount() throws Exception{
		int scheduleNum = 100;
		try {
			String paramPath = ResourceUtil.getResourceDir() + File.separator + "ufoc" + File.separator +"hbflow"+ File.separator + "hbFlow.properties";
			Properties props = com.ufida.iufo.pub.tools.FileUtil.loadProperties(paramPath);
			scheduleNum = Integer.parseInt(props.getProperty("Number"));
		} catch(Exception e) {
			AppDebug.debug(e);
		}
		return scheduleNum;
	}
}