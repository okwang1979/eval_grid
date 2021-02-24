/**
 *
 */
package nc.impl.iufo.commit;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.ds.PhysicalDataSource;
import nc.bs.pub.bap.toolkit.AppExecutor;
import nc.impl.pubapp.pattern.database.DBTool;
import nc.itf.iufo.commit.ICommitCommonService;
import nc.itf.iufo.commit.ICommitManageService;
import nc.itf.iufo.msg.IMessageUserQry;
import nc.itf.iufo.task.ITaskQueryService;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.processor.ColumnListProcessor;
import nc.message.sendtype.vo.IMsgSendTypeConst;
import nc.message.templet.bs.MsgContentCreator;
import nc.message.util.MessageCenter;
import nc.message.vo.MessageVO;
import nc.message.vo.NCMessage;
import nc.pub.iufo.basedoc.OrgUtil;
import nc.pub.iufo.basedoc.UserUtil;
import nc.pub.iufo.exception.UFOSrvException;
import nc.pubitf.rbac.IPermissionDomainService;
import nc.ui.iufo.taskmsgsel.TaskMsgCalculator;
import nc.ui.iufo.taskmsgsel.UfoeMsgContext;
import nc.util.iufo.pub.AuditUtil;
import nc.utils.iufo.TaskSrvUtils;
import nc.vo.iufo.commit.CommitActionEnum;
import nc.vo.iufo.commit.CommitActionSelRepVO;
import nc.vo.iufo.commit.CommitParamVO;
import nc.vo.iufo.commit.CommitStateEnum;
import nc.vo.iufo.commit.CommitVO;
import nc.vo.iufo.commit.RepDataCommitVO;
import nc.vo.iufo.commit.TaskCommitVO;
import nc.vo.iufo.data.IKeyDetailData;
import nc.vo.iufo.data.MeasurePubDataUtil;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.task.AllCommitStateEnum;
import nc.vo.iufo.task.ICommitConfigConstant;
import nc.vo.iufo.task.TaskAnnotationVO;
import nc.vo.iufo.task.TaskMsgSelVO;
import nc.vo.iufo.task.TaskVO;
import nc.vo.jcom.lang.StringUtil;
import nc.vo.ml.NCLangRes4VoTransl;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.util.SqlWrapper;

import org.apache.commons.collections.CollectionUtils;

import com.ufida.iufo.pub.tools.AppDebug;

/**
 * 报送管理服务
 * @author xulm
 * @created at 2010-9-10,下午05:02:27
 *
 */
public class CommitManageImpl implements ICommitManageService{

	private static Object LOCK_OBJECT = new Object();
	
	/* (non-Javadoc)
	 * @see nc.itf.iufo.commit.ICommitManageService#commitTask(java.lang.String, java.lang.String)
	 */
	@Override
	public void commitTask(final MeasurePubDataVO[] pubDatas,final CommitVO[] commits,final List<CommitActionSelRepVO[]> selReps,CommitParamVO param)throws UFOSrvException {

		try {
			CommitActionEnum action = param.getAction();
			String taskpk = param.getTaskpk();
			final String orgpk = param.getOrgpk();
			final String rmspk = param.getRmspk();
			final String userid = param.getUserid();
			final String grouppk = param.getGrouppk();
			final int commitflg = param.getCommitflg();
			TaskAnnotationVO[] taskAnnotations = param.getTaskAnnotations();
			final Object[] hastenmsgs = param.getHastenmsgs();
			
			final TaskVO task=TaskSrvUtils.getTaskVOById(taskpk);
			List<String> vRepPK;

			ICommitCommonService commitCommonService = NCLocator.getInstance().lookup(ICommitCommonService.class);

			for (int i = 0;i < commits.length; i ++) {
				CommitVO commit = commits[i];
				MeasurePubDataVO pubData = pubDatas[i];
				CommitActionSelRepVO[] selRep = selReps.get(i);

				vRepPK = new ArrayList<String>();
				for (CommitActionSelRepVO element : selRep) {
					vRepPK.add(element.getPk_report());
				}
				String[] reports = vRepPK.toArray(new String[0]);
				TaskVO taskvo=TaskSrvUtils.getTaskVOById(commit.getPk_task());
				commitCommonService.commitCommonTask(pubData, commit.getPk_task(), reports, action,taskvo.getCommit_end_day(),
						taskvo.getCommit_end_time(),taskvo.getPk_accscheme());

			}
			if(taskAnnotations != null)
				TaskSrvUtils.addTaskAnnotations(taskAnnotations);

			if (!(task.getCommstrategy().intValue()==ICommitConfigConstant.COMMIT_STRAGY_ALL &&
					commitflg == AllCommitStateEnum.TASK_COMMIT_VAL && task.getCommitmode().intValue()==ICommitConfigConstant.COMMIT_MODE_DIRECT.intValue())) {

				//新线程发送报送消息
				final String ds = InvocationInfoProxy.getInstance().getUserDataSource();
				//取出主线程的语种信息，设定在新线程的语种信息里
				final String langcode = InvocationInfoProxy.getInstance().getLangCode();

//				new Thread() {
//					@Override
//					public void run() {
//						try {
//							InvocationInfoProxy.getInstance().setUserDataSource(ds);
//							InvocationInfoProxy.getInstance().setLangCode(langcode);
//							for (int i = 0;i < commits.length; i ++) {
//								MeasurePubDataVO pubData = pubDatas[i];
//								CommitActionSelRepVO[] selRep = selReps.get(i);
//								sendCommitMsg(pubData.getUnitPK(),selRep,orgpk,rmspk,userid,task,pubData,grouppk,commitflg);
//							}
//
//						} catch (Exception e) {
//							AppDebug.debug(e);
//						}
//					}
//				}.start();
				
				new AppExecutor() {
					@Override
					public void execute() {
						try {
							InvocationInfoProxy.getInstance().setUserDataSource(ds);
							InvocationInfoProxy.getInstance().setLangCode(langcode);
							for (int i = 0;i < commits.length; i ++) {
								MeasurePubDataVO pubData = pubDatas[i];
								CommitActionSelRepVO[] selRep = selReps.get(i);
								sendCommitMsg(pubData.getUnitPK(),selRep,orgpk,rmspk,userid,task,pubData,grouppk,commitflg,hastenmsgs);
							}
						} catch (Exception e) {
							AppDebug.debug(e);
						}
					}
				}.start();
			}

		}catch (Exception e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		}
	}

//	@SuppressWarnings("unchecked")
//	@Override
//	public void hastenTask(CommitVO[] commits,String taskpk)throws UFOSrvException {
//
//		try {
//			String[] reportids = TaskSrvUtils.getReportIdByTaskId(taskpk);
//
//			for (int i = 0;i < commits.length; i ++) {
//				CommitVO commit = commits[i];
//
//				String strCurUserPK = "NC_USER0000000000000";
//				UFDateTime curTime=AuditUtil.getCurrentTime();
//
//				BaseDAO dao = new BaseDAO();
//				String alone_id = MeasurePubDataBO_Client.getAloneID(commit.getPubData());
//				List<TaskCommitVO> lstTaskCommit = (List<TaskCommitVO>)dao.retrieveByClause(TaskCommitVO.class, "alone_id='"+alone_id+"' and pk_task='" + taskpk+"'");
//
//				TaskCommitVO taskCommit=null;
//				if(lstTaskCommit!=null && lstTaskCommit.size() > 0){
//					taskCommit=lstTaskCommit.get(0);
//				}else{
//					taskCommit=new TaskCommitVO();
//					taskCommit.setAlone_id(alone_id);
//					taskCommit.setPk_task(taskpk);
//					taskCommit.setCommit_state(CommitStateEnum.STATE_NOCOMMIT.getIntValue());
//				}
//
//				commit.setOperate_time(new UFDateTime(TimeService.getInstance().getTime()));
//				boolean bCommitLate=false;
//
//				if (taskCommit.getCommit_state().intValue()>=CommitStateEnum.STATE_COMMITED.getIntValue())
//					continue;
//				taskCommit.setFlag_hasten(UFBoolean.TRUE);
//
//				if(lstTaskCommit!=null && lstTaskCommit.size() > 0){
//					dao.updateVO(taskCommit);
//				}else{
//				    dao.insertVO(taskCommit);
//				}
//
//				List<TaskCommitRecordVO> lstTaskCommitRecord= (List<TaskCommitRecordVO>)dao.retrieveByClause(TaskCommitRecordVO.class,  "alone_id='"+alone_id+"' and pk_task='" + taskpk+"' and action="+CommitActionEnum.ACTION_HASTEN.getIntValue());
//				TaskCommitRecordVO taskCommitRecord=null;
//				if(lstTaskCommitRecord!=null && lstTaskCommitRecord.size()>0){
//	            	taskCommitRecord=lstTaskCommitRecord.get(0);
//	            	taskCommitRecord.setAction_times(taskCommitRecord.getAction_times()+1);
//	            	taskCommitRecord.setOperator(strCurUserPK);
//	            	taskCommitRecord.setOperate_time(curTime);
//	            	dao.updateVO(taskCommitRecord);
//	            }else{
//	            	taskCommitRecord=new TaskCommitRecordVO();
//	            	taskCommitRecord.setAlone_id(alone_id);
//	            	taskCommitRecord.setPk_task(taskpk);
//	            	taskCommitRecord.setAction(CommitActionEnum.ACTION_HASTEN.getIntValue());
//	            	taskCommitRecord.setAction_times(1);
//	            	taskCommitRecord.setOperator(strCurUserPK);
//	            	taskCommitRecord.setOperate_time(curTime);
//	            	dao.insertVO(taskCommitRecord);
//	            }
//	            commitReport(commit.getPk_task(),commit.getAlone_id(),reportids,CommitActionEnum.ACTION_HASTEN,taskCommit.getCommit_state(),bCommitLate,strCurUserPK,curTime);
//			}
//		} catch (DAOException e) {
//			AppDebug.debug(e);
//			throw new UFOSrvException(e.getMessage());
//		}catch (Exception e) {
//			AppDebug.debug(e);
//			throw new UFOSrvException(e.getMessage());
//		}
//	}

	public void sendCommitMsg(String selectedOrgPK,CommitActionSelRepVO[] selRep,String loginOrgPK,String rmsPK,String userID,TaskVO task,
			MeasurePubDataVO pubData,String grouppk,int commitflg,Object[] hastenmsgs) throws Exception{
		NCMessage ncMessage;
		MessageVO messageVO = new MessageVO();
		List<NCMessage> msgs = new ArrayList<NCMessage>();

		//查找用户接口
		IPermissionDomainService domainSrv = NCLocator.getInstance().lookup(IPermissionDomainService.class);
		
//		ITaskQueryService taskQueryService = new TaskQueryImpl();
		ITaskQueryService taskQueryService = NCLocator.getInstance().lookup(ITaskQueryService.class);
		//查找用户语种接口
		IMessageUserQry messageUserQry = NCLocator.getInstance().lookup(IMessageUserQry.class);
		
		String[] uservo = null;
		//是否发送消息
		boolean ncsend = true;
		//是否发送短信
		boolean smssend = false;
		
		//备份当前主线程语种
		String langcodeback = InvocationInfoProxy.getInstance().getLangCode();
		
		try {
			String fatherorg = null;
			if(commitflg == AllCommitStateEnum.TASK_COMMIT_VAL
					|| commitflg == AllCommitStateEnum.REQUEST_BACK_COMMITED_VAL
					|| commitflg == AllCommitStateEnum.REQUEST_BACK_AFFIRMED_VAL
					|| commitflg == AllCommitStateEnum.CANCEL_AFFIRMED_VAL){
		        //上报组织的直接上级
			    fatherorg = taskQueryService.queryFatherOrgByOrg(rmsPK,selectedOrgPK,task.getPk_task(), task.getCommitmode());
			    if (fatherorg == null) {
			    	uservo = domainSrv.getPkUserWithOrgPermission("18200RCMMT", selectedOrgPK, grouppk);
			    } else {
			    	uservo = domainSrv.getPkUserWithOrgPermission("18200RCMMT", fatherorg, grouppk);
			    }
			} else {
				
				if (commitflg == AllCommitStateEnum.HASTEN_COMMIT_VAL) {
					//wangqi 20140411 移动开发修改
					if (hastenmsgs != null) {
						//根据人员找到相对应的用户
						Map<String, String[]> hastenusers = (Map<String, String[]>)hastenmsgs[0];
						String[] humanspks = hastenusers.get(selectedOrgPK);
						uservo = taskQueryService.getUserPKByHumanPK(humanspks);
						//是否发送消息
						ncsend = (Boolean)hastenmsgs[1];
						//是否发送短信
						smssend = (Boolean)hastenmsgs[2];
					} else {
						//不限定集团
						uservo = this.getPkUserWithOrgPermission("18200RDQRY", selectedOrgPK);
					}
				} else {
					uservo = domainSrv.getPkUserWithOrgPermission("18200RDQRY", selectedOrgPK, grouppk);
				}
				
			}

			String strUnitPKName = OrgUtil.getOrgName(selectedOrgPK);

			Integer type;
			String subject;
			String kindname;
			if (commitflg == AllCommitStateEnum.TASK_COMMIT_VAL) {
				if (task.getCommstrategy().intValue()==ICommitConfigConstant.COMMIT_STRAGY_SELECT) {
					subject = strUnitPKName + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1120")/*@res "（单位）的上报（手选）"*/;
				} else {
					subject = strUnitPKName + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1121")/*@res "（单位）的上报（层层上报）"*/;
				}
				kindname = CommitActionEnum.ACTION_COMMIT.getTag();
				type = 2;
			} else if (commitflg == AllCommitStateEnum.REQUEST_BACK_COMMITED_VAL){
				subject = strUnitPKName + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1122")/*@res "（单位）的请求退回（已上报）"*/;
				kindname = CommitActionEnum.ACTION_REQUESTBACK.getTag();
				type = 7;
			} else if (commitflg == AllCommitStateEnum.REQUEST_BACK_AFFIRMED_VAL){
				subject = strUnitPKName + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1123")/*@res "（单位）的请求退回（已确认）"*/;
				kindname = CommitActionEnum.ACTION_REQUESTBACK.getTag();
				type = 7;
			} else if (commitflg == AllCommitStateEnum.CANCEL_COMMIT_VAL){
				subject = strUnitPKName + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1124")/*@res "（单位）的取消上报"*/;
				kindname = CommitActionEnum.ACTION_CANCELCOMMIT.getTag();
				type = 3;
			} else if (commitflg == AllCommitStateEnum.CANCEL_AFFIRMED_VAL){
				subject = strUnitPKName + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1125")/*@res "（单位）的取消确认"*/;
				kindname = CommitActionEnum.ACTION_CANCELAFFIRM.getTag();
				type = 5;
			} else if (commitflg == AllCommitStateEnum.HASTEN_COMMIT_VAL){
				subject = strUnitPKName + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1126")/*@res "（单位）的催报"*/;
				kindname = CommitActionEnum.ACTION_HASTEN.getTag();
				type = 1;
			} else if (commitflg == AllCommitStateEnum.AFFIRM_VAL){
				subject = strUnitPKName + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1127")/*@res "（单位）的确认"*/;
				kindname = CommitActionEnum.ACTION_AFFIRM.getTag();
				type = 4;
			} else {
				subject = strUnitPKName + nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("1820001_0","01820001-1128")/*@res "（单位）的退回"*/;
				kindname = CommitActionEnum.ACTION_BACK.getTag();
				type = 6;
			}

			String userIDName = UserUtil.getUserName(userID);

			//关键字名字
			String keysname = "";
			if (pubData.getKeyDatas() == null || pubData.getKeyDatas()[0] == null) {
				MeasurePubDataUtil.initMeasurePubDataKeyData(new MeasurePubDataVO[] { pubData });
			}
			IKeyDetailData[] keydatas = pubData.getKeyDatas();
			
			if (keydatas != null && keydatas.length > 0){
				for (int i = 0;i < keydatas.length;i ++) {
					IKeyDetailData data = keydatas[i];
					if (i == 0) {
						keysname = data.getMultiLangText();
					} else {
						keysname = keysname + "," + data.getMultiLangText();
					}
				}
			}

			String content = "";
			if (commitflg == AllCommitStateEnum.TASK_COMMIT_VAL && task.getCommstrategy().intValue()==ICommitConfigConstant.COMMIT_STRAGY_SELECT) {
				for (int i = 0; i < selRep.length; i ++) {
					if (i == 0) {
						content = selRep[i].getRepname();
					} else {
						content = content + "," + selRep[i].getRepname();
					}
				}
			} else {
				content = task.getChangeName() + "," + keysname;
			}
			
			TaskMsgSelVO taskMsgSelVO = taskQueryService.getMsgTempByTaskIdAndType(task.getPk_task(), type);
			NCMessage tempNCMsg;
			//任务是否关联消息模板
			if (taskMsgSelVO != null && taskMsgSelVO.getMsgtemp() != null) {
				//变量解析器
				UfoeMsgContext context = new UfoeMsgContext();
				context.setTaskcode(task.getCode());
				context.setTaskname(task.getName());
				context.setUser(userIDName);
				context.setKeycomb(keysname);
				context.setDate((new UFDateTime()).toString());
				context.setKind(kindname);

				TaskMsgCalculator calculator = new TaskMsgCalculator(context);
				
				KeyGroupVO keyGroup = pubData.getKeyGroup();
				KeyVO[] keys = keyGroup.getKeys();
				for(int i = 0; i < keys.length; i++){
					if (keys[i].getIsbuiltin().booleanValue()) {
						//系统预置关键字，关键字编码为变量编码
						calculator.getVarMap().put(keys[i].getCode(), keydatas[i].getMultiLangText());
					} else {
						//非预置，pk为变量编码
						calculator.getVarMap().put(keys[i].getPk_keyword(), keydatas[i].getMultiLangText());
					}
				}
				
				tempNCMsg = new NCMessage();
				MsgContentCreator creator = new MsgContentCreator();
				
				String langcode = InvocationInfoProxy.getInstance().getLangCode();
//				LanguageVO langvo = DataMultiLangAccessor.getInstance().getDefaultLang();
//				String langcode = langvo.getLangcode();
				
				Map<String,NCMessage> msgmap = creator.createMessageUsingTemp(taskMsgSelVO.getMsgtemp(), 
						taskMsgSelVO.getPk_org(), new String[]{langcode}, tempNCMsg, calculator, null, null);
				tempNCMsg = msgmap.get(langcode);
//				tempNCMsg = creator.createMessageUsingTemp(taskMsgSelVO.getMsgtemp(), tempNCMsg, calculator, null, null);
				messageVO = tempNCMsg.getMessage();
			} else {
				//TODO ? commitflg  后面哪用到了？
				messageVO.setSubject(subject);
				messageVO.setContent(content);
			}

			String detail = rmsPK + "," + selectedOrgPK + "," + task.getPk_task() + "," + commitflg;
			messageVO.setDetail(detail);
			messageVO.setSender(userID);
			messageVO.setMsgsourcetype("commitnotice");
			messageVO.setMsgtype(IMsgSendTypeConst.NC);

			messageVO.setSendtime(new UFDateTime());
			messageVO.setSendstate(UFBoolean.valueOf(true));
			
			//防止取消确认给同一用户发重复消息
			List<String> userpks = new ArrayList<String>();
			
			if (uservo != null && uservo.length > 0) {	
				if (ncsend) {
					for (String userpk : uservo) {
						userpks.add(userpk);
						//取得接收者的语种
						String userlangcode = messageUserQry.getLangCodeByUserid(userpk);
						InvocationInfoProxy.getInstance().setLangCode(userlangcode);
						
						MessageVO messagenew = (MessageVO)messageVO.clone();
						messagenew.setReceiver(userpk);
						ncMessage = new NCMessage();
						ncMessage.setMessage(messagenew);
						msgs.add(ncMessage);
					}
				}
				
				if (smssend) {
					for (String userpk : uservo) {
						//取得接收者的语种
						String userlangcode = messageUserQry.getLangCodeByUserid(userpk);
						InvocationInfoProxy.getInstance().setLangCode(userlangcode);
						
						MessageVO messagenew = (MessageVO)messageVO.clone();
						messagenew.setReceiver(userpk);
						messagenew.setMsgtype(IMsgSendTypeConst.SMS);
						ncMessage = new NCMessage();
						ncMessage.setMessage(messagenew);
						msgs.add(ncMessage);
					}
				}
			}
			if (commitflg == AllCommitStateEnum.CANCEL_AFFIRMED_VAL) {
				uservo = domainSrv.getPkUserWithOrgPermission("18200RDQRY", selectedOrgPK, grouppk);
				if (uservo != null && uservo.length > 0) {
					for (String vo : uservo) {
						if (!userpks.contains(vo)) {
							//取得接收者的语种
							String userlangcode = messageUserQry.getLangCodeByUserid(vo);
							InvocationInfoProxy.getInstance().setLangCode(userlangcode);
							
							MessageVO messagenew = (MessageVO)messageVO.clone();
							messagenew.setReceiver(vo);
							ncMessage = new NCMessage();
							ncMessage.setMessage(messagenew);
							msgs.add(ncMessage);
						}
					}
				}
			}

			if (msgs.size() > 0) {
				//还原主线程的语种
				InvocationInfoProxy.getInstance().setLangCode(langcodeback);
				MessageCenter.sendMessage(msgs.toArray(new NCMessage[0]));
			}
		} catch (Exception e) {
			AppDebug.debug(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addRepInputSate(String strTaskPK,String strAloneID, String strRepPK, String strUserPK, boolean bInput, UFDateTime lastCalcTime) throws UFOSrvException{
		try {
			BaseDAO dao = new BaseDAO();

			UFDateTime time = AuditUtil.getCurrentTime();
			SQLParameter param=new SQLParameter();
			param.addParam(strAloneID);
			param.addParam(strRepPK);

			RepDataCommitVO repState=null;
			ArrayList<RepDataCommitVO> lstRepDataState=(ArrayList<RepDataCommitVO>)dao.retrieveByClause(RepDataCommitVO.class, "alone_id=? and pk_report=?",param);
			if (lstRepDataState!=null && lstRepDataState.size()>0)
				repState=lstRepDataState.get(0);

			if (repState==null){
				repState=new RepDataCommitVO();
				repState.setAlone_id(strAloneID);
				repState.setPk_report(strRepPK);
				repState.setCommit_state(CommitStateEnum.STATE_NOCOMMIT.getIntValue());
				repState.setPk_task(strTaskPK);
			}

			if (bInput){
				repState.setInput_person(strUserPK);
				repState.setInput_time(time);
				repState.setInput_task(strTaskPK);
			}
			repState.setFlag_input(UFBoolean.valueOf(bInput));

			if(lastCalcTime != null) {
				repState.setLastcalctime(lastCalcTime);
			}

			if (lstRepDataState!=null && lstRepDataState.size()>0)
				dao.updateVO(repState);
			else
				dao.insertVO(repState);

			if (bInput){
				if(strTaskPK == null)
					return;
				synchronized (LOCK_OBJECT) {					
					param=new SQLParameter();
					param.addParam(strAloneID);
					param.addParam(strTaskPK);
					List<TaskCommitVO> lstTaskCommit = (List<TaskCommitVO>)dao.retrieveByClause(TaskCommitVO.class, "alone_id=? and pk_task=?",param);
					TaskCommitVO taskCommit=null;
					if(lstTaskCommit!=null && lstTaskCommit.size() > 0){
						taskCommit=lstTaskCommit.get(0);
					}else{
						taskCommit=new TaskCommitVO();
						taskCommit.setAlone_id(strAloneID);
						taskCommit.setPk_task(strTaskPK);
						taskCommit.setCommit_state(CommitStateEnum.STATE_NOCOMMIT.getIntValue());
					}

					taskCommit.setInput_person(strUserPK);
					taskCommit.setInput_time(time);

					if(lstTaskCommit!=null && lstTaskCommit.size() > 0){
						
						String sql = "update iufo_task_commit set Input_person='"+strUserPK+"' ,Input_time='"+time+"' where alone_id='"+strAloneID+"' and pk_task='"+strTaskPK+"' ";
						
						
						Connection connection = null;
						Statement n_stmt = null;
						  try {
								
							DBTool tool = new DBTool();
						
						
//						   connection = new PhysicalDataSource().getConnection();
						   connection = tool.getConnection();
						   n_stmt = connection.createStatement();
						   n_stmt.executeUpdate(sql);
						   connection.commit();
						  
						  } catch (SQLException ex) {
							  if(connection!=null){
								  try {
									connection.rollback();
								} catch (SQLException e) {
								 
								}
							  }
							  
						  } finally {
						   if (connection != null) {
						    try {
						     connection.close();
						    } catch (SQLException ex) {
						      
						    }
						   }
						  }
						//dao.updateVO(taskCommit);
					}else{
						dao.insertVO(taskCommit);
					}
				}
			}
		} catch (DAOException e) {
			AppDebug.debug(e);
			throw new UFOSrvException(e.getMessage());
		}
	}
	
	private String[] getPkUserWithOrgPermission(String funcode,String pk_org) throws BusinessException {
		
		if(StringUtil.isEmptyWithTrim(funcode) || StringUtil.isEmptyWithTrim(pk_org))
			throw new IllegalArgumentException(NCLangRes4VoTransl.getNCLangRes().getStrByID("RBAC","0rbac0193")/*@res "传入参数不能为空！"*/);
		String sql1 = "select funcperm.subjectid from sm_perm_func as funcperm, sm_resp_func as respfunc where respfunc.pk_responsibility=funcperm.ruleid and respfunc.busi_pk={funcode?}";
		String sql = "select distinct ur.cuserid from sm_user_role ur,sm_subject_org subjectorg where ur.pk_role = subjectorg.subjectid " 
				+" and subjectorg.pk_org = {pk_org?} and ur.pk_role in ("+sql1+")" 
				+" and ur.enabledate <= {nowtime?} and (ur.disabledate > {nowtime?} or isnull(cast(ur.disabledate as char),'~') = '~')";
		SqlWrapper sw = new SqlWrapper(sql);
		sw.bind("pk_org", pk_org);
		sw.bind("funcode", funcode);
		sw.bind("nowtime", new UFDate());
		
		BaseDAO dao = new BaseDAO();
		
		List<String> cuseridList = (List<String>)dao.executeQuery(sw.getSql(), sw.getSqlParameter(), new ColumnListProcessor());
		
		return CollectionUtils.isEmpty(cuseridList) ? null : cuseridList.toArray(new String[0]);
	}
}