package nc.bs.iufo.sysimp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.pfxx.ISwapContext;
import nc.bs.pfxx.plugin.AbstractPfxxPlugin;
import nc.itf.hbbb.hbrepstru.HBRepStruUtil;
import nc.itf.iufo.individual.IUFOIndividualSettingUtil;
import nc.itf.iufo.ufoe.vorp.IUfoeVorpQuerySrv;
import nc.itf.uap.rbac.IUserManageQuery;
import nc.itf.uif.pub.IUifService;
import nc.jdbc.framework.SQLParameter;
import nc.pub.iufo.cache.UFOCacheManager;
import nc.ui.iufo.data.MeasurePubDataBO_Client;
import nc.util.hbbb.hbscheme.HBSchemeSrvUtils;
import nc.util.info.sysimp.SysImpUtil;
import nc.vo.bd.currtype.CurrtypeVO;
import nc.vo.hbbb.hbscheme.HBSchemeVO;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyGroupVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.iufo.sysimp.SysImpVO;
import nc.vo.iufo.task.TaskVO;
import nc.vo.iuforeport.rep.ReportVO;
import nc.vo.ml.AbstractNCLangRes;
import nc.vo.org.OrgVO;
import nc.vo.pfxx.auxiliary.AggxsysregisterVO;
import nc.vo.pfxx.util.PfxxUtils;
import nc.vo.pub.BusinessException;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.para.SysInitVO;
import nc.vo.sm.UserVO;
import nc.vo.vorg.ReportCombineStruVersionVO;
import nc.vo.vorg.ReportManaStruVersionVO;

import com.ufida.zior.console.ActionHandler;
import com.ufsoft.iuforeport.repdatainput.LoginEnvVO;
import com.ufsoft.iuforeport.repdatainput.RepDataOperResultVO;
import com.ufsoft.iuforeport.repdatainput.ufoe.IUfoTableInputActionHandler;
import com.ufsoft.iuforeport.tableinput.applet.RepDataParam;
import com.ufsoft.table.CellsModel;

public class SysImpPfxxPlugin extends AbstractPfxxPlugin{

	@Override
	protected Object processBill(Object arg0, ISwapContext arg1,
			AggxsysregisterVO arg2) throws BusinessException {
		
		if(arg0 instanceof SysImpVO){
			SysImpVO vo = (SysImpVO)arg0;
			Object obj = null;
			try {
				Logger.init("iufo");
				Logger.error("#########################��ϵͳ�������Ϳ�ʼ######"+(new UFDateTime()).toStdString());
				IUifService service = NCLocator.getInstance().lookup(IUifService.class);
				SysInitVO[] svos = (SysInitVO[]) service.queryByCondition(SysInitVO.class, "initcode = 'reportUser'");
				String pk_user = null;
				 BaseDAO dao = new BaseDAO();
				 String datasource = InvocationInfoProxy.getInstance().getUserDataSource();
				
				 Logger.error("Current ds is:"+datasource);
				 if("design".equals(InvocationInfoProxy.getInstance().getUserDataSource())){
					 Logger.error("reload datasource,use account "+arg1.getAccount());
					 datasource = PfxxUtils.getDataSourceByAccountCode(arg1.getAccount());
					 Logger.error("reload datasource is:"+datasource);
					 InvocationInfoProxy.getInstance().setUserDataSource(datasource);
				 }
				if(svos!=null&&svos.length>0){
					Logger.error("Begin find user:"+svos[0].getValue());
					UserVO user = NCLocator.getInstance().lookup(IUserManageQuery.class).findUserByCode(svos[0].getValue(),datasource);
					if(user!=null){
						Logger.error("Find user code is :"+user.getUser_code());
						pk_user = user.getCuserid();
						if(pk_user!=null){
							InvocationInfoProxy.getInstance().setUserId(pk_user);
						}
					}else{
						
					}
				
				}
				
				
			
				int hbCode = 0;
				 obj = SysImpUtil.readObject(vo.getReportData());
					Logger.error("1.����CellsModel�ɹ���");
				 
					Logger.error("2.��ѯ����"+vo.getReportCode());
					ReportVO repVo = UFOCacheManager.getSingleton().getReportCache().getByCode(vo.getReportCode());
					Logger.error("  ��ѯ������ɣ�"+repVo.getCode()+"_"+repVo.getName());
					String strKeyGroupPk = repVo.getPk_key_comb();
					KeyGroupVO keyGroupVo = UFOCacheManager.getSingleton().getKeyGroupCache().getByPK(strKeyGroupPk);
					
					KeyVO[] keys = keyGroupVo.getKeys();

					MeasurePubDataVO pubVO = new MeasurePubDataVO();
					pubVO.setKType(strKeyGroupPk);
					
					boolean isHB = false;
					if(vo.getKeyValue5()==null){
						pubVO.setVer(0);
					}else{
						int ver = Integer.parseInt(vo.getKeyValue5());
						if(ver!=0){
							//����
							isHB = true;
							hbCode = ver-50000; 
							SysInitVO[] hbParam = (SysInitVO[]) service.queryByCondition(SysInitVO.class, "initcode = 'HBCODE'");
							if(hbParam==null||hbParam.length==0){
								Logger.error("HBCODE is:null");
								
								
							}else{
								
								Logger.error("HBCODE is:"+hbParam[0].getValue());
								String[] verReplaces = hbParam[0].getValue().split(";");
							 
								for(String verStr:verReplaces){
									Logger.error("HBCODE param is :"+verStr);
									String[] repStr = verStr.split(":");
									if(repStr!=null||repStr.length==2){
										String findStr = hbCode+"";
										if(findStr.equals(repStr[0])){
											hbCode = Integer.valueOf(repStr[1]);
											ver = 50000+hbCode;
											
											break;
											
										}
									}
									
//									if(ver==50001){
//										ver=ver+1;
//									}else if(ver==50002){
//										ver=ver+3;
//									}
								}
								
							}
							
							
						}
						
						pubVO.setVer(ver);
					}
					
					Logger.error("3.����汾��"+pubVO.getVer());
					
					pubVO.setKeyGroup(keyGroupVo);
					
					
				
					 
					 
					 SQLParameter strParams = new SQLParameter();
					 
					 
					 
					 strParams.addParam(vo.getRepStructCode());
					  
			
					 if(isHB){
						 

					
						
						
						//����
						
						if(vo.getKeyCode3()==null){
							
							//�ڼ�
							for(KeyVO key:keys){
								if(key.getCode().equals(vo.getKeyCode2())){
									String time = vo.getKeyValue2();
									pubVO.setKeywordByName(key.getName(), time);
									Logger.error("4.�����ڼ䣺"+time);
									break;
								}
								
							}
							
						}else{
							 SQLParameter currParams = new SQLParameter();
							 currParams.addParam(vo.getKeyValue2());
							 
					
							 Collection<CurrtypeVO> currs =  dao.retrieveByClause(CurrtypeVO.class, " code=? ",currParams);
							 
							 
							 if(currs.size()!=1){
								 String message = "����ʧ�ܣ���ѯ����ʧ�ܣ�"+vo.getKeyValue2();
								 
								 throw new BusinessRuntimeException(message);
							 }
							 
						
							
							for(KeyVO key:keys){
								if(key.getCode().equals(vo.getKeyCode2())){
									String pk_curr = currs.iterator().next().getPrimaryKey();
									pubVO.setKeywordByName(key.getName(), pk_curr);
									Logger.error("4.(1)������֣�"+pk_curr+"_"+pk_curr);
									break;
								}
								
							}
							
							//�ڼ�
							for(KeyVO key:keys){
								if(key.getCode().equals(vo.getKeyCode3())){
									String time = vo.getKeyValue3();
									pubVO.setKeywordByName(key.getName(), time);
									Logger.error("4.(2)�����ڼ䣺"+time);
									break;
								}
								
							}
						}
					 
						 SQLParameter orgParam = new SQLParameter();
						 orgParam.addParam(vo.getKeyValue1());
						Collection<OrgVO> orgs =  dao.retrieveByClause(OrgVO.class, " code=? ",orgParam);
						if(orgs.isEmpty()||orgs.size()!=1){
							String message = "��ѯ�������������룺"+vo.getKeyValue1();
							throw new BusinessRuntimeException(message);
							
						}
						OrgVO org = 			orgs.iterator().next();
						
						for(KeyVO key:keys){
							if(key.getCode().equals(vo.getKeyCode1())){
								 
								pubVO.setKeywordByName(key.getName(), org.getPk_org());
								break;
							}
							
						}
						

						String strAloneID = MeasurePubDataBO_Client.getAloneID(pubVO);
						pubVO = MeasurePubDataBO_Client.findByAloneID(strKeyGroupPk, strAloneID);
//						pubVO = MeasurePubDataBO_Client.findByAloneID(strKeyGroupPk, strAloneID);
						
						RepDataParam dataParam = new RepDataParam();
						dataParam.setAloneID(strAloneID);
						dataParam.setPubData(pubVO);
					
						dataParam.setOperType("repdata_inpt");
						
						
					     HBSchemeVO hbSchemeVO = HBSchemeSrvUtils.getHBSchemeByCode(hbCode+"");
					     
					     
			                // �ؼ������
			                KeyGroupVO keyGroupVO = pubVO.getKeyGroup();
			                // ʱ��ؼ���
			                KeyVO key = keyGroupVO.getTTimeKey();
			                // ����ϲ���ϵ�汾
			                ReportCombineStruVersionVO memberVO = null;
			                if (key != null) {
			                    String keyValue = pubVO.getKeywordByPK(key.getPk_keyword());
			                    if (key.isAccPeriodKey()) {
			                        memberVO = HBRepStruUtil.getHBStruVersionVO(pubVO.getAccSchemePK(), key.getPk_keyword(),
			                                keyValue, hbSchemeVO.getPk_repmanastru());
			                    } else {
			                        memberVO = HBRepStruUtil.getHBStruVersionVO(keyValue, hbSchemeVO.getPk_repmanastru());
			                    }
			                } else {
			                    memberVO = HBRepStruUtil.getLastVersion(hbSchemeVO.getPk_repmanastru());
			                }
					     
						dataParam.setRepMngStructPK(memberVO.getPk_vid());
						dataParam.setRepOrgPK(org.getPk_org());
						dataParam.setReportPK(repVo.getPk_report());
						dataParam.setTaskPK(hbSchemeVO.getPk_hbscheme());
						if(pk_user!=null){
							
							dataParam.setOperUserPK(pk_user);
						}
						
					
						
						Object[]  objs = new Object[4];
						objs[0] = "sheet1";
						objs[1] = repVo.getCode();
						 
						objs[2] = "-1";
						objs[3] =  obj;
						
						

						List<Object[]> list = new ArrayList<Object[]>();
						list.add(objs);
						LoginEnvVO env = getLoginEnvVO(org.getPk_org(),"");
//						env.set
						RepDataOperResultVO errMsgs=(RepDataOperResultVO)ActionHandler.execWithZip("nc.util.hbbb.input.HBBBTableInputActionHandler", "importExcelData",
								new Object[]{dataParam,getLoginEnvVO("",""),list,new Boolean(false)});
						
						CellsModel model =  errMsgs.getCellsModel();
						Logger.error("Finish Cells model:"+model); 
						Logger.error("*********************�ϲ������������");
					 
						 
					 }else{
						 Collection<ReportManaStruVersionVO> strus =  dao.retrieveByClause(ReportManaStruVersionVO.class, " code=? ",strParams);
						 ReportManaStruVersionVO struVo = strus.iterator().next();
						 for(ReportManaStruVersionVO version:strus){
							 struVo = version;
						 }
						 
							
						  
						
//					
						 String pk_stru = struVo.getPk_reportmanastru();
						 
						 
						 SQLParameter taskParams = new SQLParameter();
//						 String svid = vo.getRepStructCode();
						 taskParams.addParam(vo.getTaskCode());
						Collection<TaskVO> tasks =  dao.retrieveByClause(TaskVO.class, " code=? ",taskParams);
						
						if(tasks==null||tasks.isEmpty()){
							String message = "��ѯTaskVOʧ�ܣ�������룺"+vo.getTaskCode();
							 
							 throw new BusinessRuntimeException(message);
						}
						TaskVO task = tasks.iterator().next();
						 
						IUfoeVorpQuerySrv vorpSrv = NCLocator.getInstance().lookup(IUfoeVorpQuerySrv.class);
						String endData = new UFDateTime(new Date()).toStdString();
						if(vo.getKeyValue3()==null){
							 endData = vorpSrv.getTaskKeyDate(task.getPk_task(), vo.getKeyValue2())[1];
						}else{
							 endData = vorpSrv.getTaskKeyDate(task.getPk_task(), vo.getKeyValue3())[1];
						}
					
						ReportManaStruVersionVO rmsVO = (ReportManaStruVersionVO)NCLocator.getInstance().lookup(IUfoeVorpQuerySrv.class).getRmsVerVOByDate(endData, pk_stru);
							String svid = rmsVO.getPk_vid();
						//�����������ѯ����
						
						
						
						 SQLParameter params = new SQLParameter();
						 
						 
						 
						 params.addParam(vo.getKeyValue1());
						 params.addParam(svid);
				
						 Collection<OrgVO> rtns =  dao.retrieveByClause(OrgVO.class, " code=? and pk_org in (  select pk_org from  org_reportmanastrumember_v  WHERE pk_svid = ?)",params);
						 
						 
						 if(rtns.size()!=1){
							 String message = "����ʧ�ܣ�δ��ѯ����Ӧ���壬������룺"+vo.getKeyValue1()+"��ϵpk:"+vo.getRepStructCode();
							 if(rtns.size()>1){
								 message = "����ʧ�ܣ���ѯ��������������ͬ���޸ı�����֯��ϵ��������룺"+vo.getKeyValue1()+"��ϵpk:"+vo.getRepStructCode();
							 }
							 throw new BusinessRuntimeException(message);
						 }
						 String pk_org =null;
						
						for(KeyVO key:keys){
							if(key.getCode().equals(vo.getKeyCode1())){
								pk_org = rtns.iterator().next().getPk_org();
								pubVO.setKeywordByName(key.getName(), pk_org);
								break;
							}
							
						}
						
						
						//����
						
						if(vo.getKeyCode3()==null){
							
							//�ڼ�
							for(KeyVO key:keys){
								if(key.getCode().equals(vo.getKeyCode2())){
									String time = vo.getKeyValue2();
									pubVO.setKeywordByName(key.getName(), time);
									break;
								}
								
							}
						}else{
							 SQLParameter currParams = new SQLParameter();
							 currParams.addParam(vo.getKeyValue2());
							 
					
							 Collection<CurrtypeVO> currs =  dao.retrieveByClause(CurrtypeVO.class, " code=? ",currParams);
							 
							 
							 if(currs.size()!=1){
								 String message = "����ʧ�ܣ���ѯ����ʧ�ܣ�"+vo.getKeyValue2();
								 
								 throw new BusinessRuntimeException(message);
							 }
							 
						
							
							for(KeyVO key:keys){
								if(key.getCode().equals(vo.getKeyCode2())){
									String pk_curr = currs.iterator().next().getPrimaryKey();
									pubVO.setKeywordByName(key.getName(), pk_curr);
									break;
								}
								
							}
							
							//�ڼ�
							for(KeyVO key:keys){
								if(key.getCode().equals(vo.getKeyCode3())){
									String time = vo.getKeyValue3();
									pubVO.setKeywordByName(key.getName(), time);
									break;
								}
								
							}
						}
					 
						
						
						

						String strAloneID = MeasurePubDataBO_Client.getAloneID(pubVO);
						pubVO = MeasurePubDataBO_Client.findByAloneID(strKeyGroupPk, strAloneID);
						pubVO = MeasurePubDataBO_Client.findByAloneID(strKeyGroupPk, strAloneID);
						
						RepDataParam dataParam = new RepDataParam();
						dataParam.setAloneID(strAloneID);
						dataParam.setPubData(pubVO);
					
						dataParam.setOperType("repdata_inpt");
						dataParam.setRepMngStructPK(pk_stru);
						dataParam.setRepOrgPK(pk_org);
						dataParam.setReportPK(repVo.getPk_report());
						dataParam.setTaskPK(task.getPk_task());
						
						
						   InvocationInfoProxy infoProxy = InvocationInfoProxy.getInstance();
							dataParam.setCurGroupPK(  infoProxy.getGroupId());
						
						
						Object[]  objs = new Object[5];
						objs[0] = dataParam;
						objs[1] = getLoginEnvVO(pk_org,pk_stru);
						List cells = new ArrayList<>();
						Object[] cellsObj = new Object[4];
						cellsObj[0] = vo.getReportCode();
						cellsObj[1] = vo.getReportCode();
						cellsObj[3] = obj;
						cells.add(cellsObj);
						objs[2] = cells;
						objs[3] = new Boolean(false);
						
						

						objs[4] = task;
						List<Object[]> list = new ArrayList<Object[]>();
						list.add(objs);
						
	 					String[] errMsgs=(String[])ActionHandler.execWithZip(IUfoTableInputActionHandler.class.getName(), "importExcelDataByMultiKeygroupVal",
								list);
	 					
	 					StringBuffer sb = new StringBuffer();
	 					for(String info: errMsgs){
	 						sb.append(info).append("\n");
	 					}
	 					
	 					Logger.error("�����������Ϣ��"+sb.toString());
						Logger.error("*********************�ϲ������������");
	 					
					 }

					
 					
			 
			} catch (Exception e) {
				Logger.error("���ݵ�����������Ϣ"+e.getMessage());
				Logger.error("                 ", e);
				throw new BusinessRuntimeException(e.getMessage(),e);
				
			}
			finally{
				Logger.init();
			}
		}
		 
		return "success";
	}
	
	private LoginEnvVO getLoginEnvVO(String org,String rsmPk){
		LoginEnvVO loginEnv = new LoginEnvVO();

		loginEnv.setCurLoginDate((new UFDateTime(new java.util.Date())).toStdString());
		loginEnv.setDataExplore(true);
		loginEnv.setDataSource(IUFOIndividualSettingUtil.getDefaultDataSourceVo());
		loginEnv.setLangCode( AbstractNCLangRes.getDefaultLanguage().getCode());
		loginEnv.setLoginUnit(org);
		loginEnv.setRmsPK(rsmPk);
		return loginEnv;
	}

 

}
