package nc.sso.bs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.identityverify.vo.IAConstant;
import nc.itf.rtx.sendNotify.INotifyQy;
import nc.itf.uap.busibean.ISysInitQry;
import nc.jdbc.framework.DataSourceCenter;
import nc.jdbc.framework.generator.SequenceGenerator;
import nc.jdbc.framework.util.SQLHelper;
import nc.login.bs.IAConfigJudger;
import nc.login.bs.INCUserQueryService;
import nc.login.vo.INCUserTypeConstant;
import nc.sso.vo.SSOLogVO;
import nc.uap.portal.log.PortalLogger;
import nc.uap.portal.login.itf.ILfwSsoService;
import nc.uap.portal.login.vo.LfwSsoRegVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.para.SysInitVO;
import nc.vo.sm.UserVO;
@SuppressWarnings("all")
public class SSOLogin extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static INotifyQy iqry = null;
	private static INotifyQy getQrySender() {
		try{
			if (iqry == null) {
				iqry = (INotifyQy) NCLocator.getInstance().lookup(
						INotifyQy.class);
			}
			return iqry;
		}catch(Exception e){
			e.printStackTrace();
			Logger.warn(e.getMessage(), e);
			// WARN::即使发送失败，也不向外抛出异常
			return null;
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		SSOLogVO logvo = new SSOLogVO();
		String errormsg = "";
		INotifyQy qry=getQrySender();
		PrintWriter pw = response.getWriter();
		try{
			String corpPk = SQLHelper.getCorpPk();
			String dataSource="design";//DataSourceCenter.getInstance().getSourceName();
//			String[] loginpk = new SequenceGenerator(dataSource).generate(
//					corpPk, 1);
//			if(loginpk.length>0)
//				logvo.setLogin_id(loginpk[0]);
			Logger.info("start sso doGet");
			logvo.setLogin_ip(request.getRemoteAddr());
			String LoginFrom = request.getParameter("LoginFrom");// 微软sso，rtx
			if(LoginFrom==null)
				LoginFrom="sso";
			logvo.setLogin_from(LoginFrom);
			String LoginTo = request.getParameter("LoginTo");// NC,Portal
			if(LoginTo==null)
				LoginTo="nc";
			logvo.setLogin_to(LoginTo);
			String ticket = request.getParameter("ticket");// ms   ???????
			String rtxtemp = request.getParameter("userid");// 用户名

			String usercode = "";//request.getParameter("usercode");
			String website = "http://" + getQrySender().getNCServerIPPortFromConfig();

			String appCode="";  
			boolean isChecked=false;
			String ncUsercode="";
			INCUserQueryService qryservice = (INCUserQueryService) NCLocator
					.getInstance().lookup(INCUserQueryService.class);
			String dsName = dataSource;//"cwgs65";//InvocationInfoProxy.getInstance().getUserDataSource();
			if(LoginTo.toLowerCase().equals("portal"))
				appCode="PORTAL";
			else
				appCode="NC";
			if (LoginFrom==null || LoginFrom.toLowerCase().equals("sso")) {
				String ssoUsercode = getQrySender().checkSso(appCode,ticket);
				if (ssoUsercode != null &!"".equals(ssoUsercode)) {
					isChecked=true;
					//ncUsercode=getQrySender().getUserCodeBySSOUser(rtxtemp);
					ncUsercode=ssoUsercode;
					
				}else{
					ncUsercode="错误用户:"+ ssoUsercode==null?"":ssoUsercode;
					logvo.setLogin_usercode(ncUsercode);
					errormsg="校验SSO用户失败";
					
					pw.write("通过ticket:"+ticket + "获取用户名失败");
				}
//				isChecked=true;
//				ncUsercode ="xugang1";
			}
			Logger.warn(errormsg);
			logvo.setLogin_usercode(ncUsercode);
			String token=Long.toString(System.currentTimeMillis());//Long.toString(Calendar.getInstance().getTimeInMillis());
			String urlIp = this.getNcAndPortalIp();
			if(isChecked == false){
				String loginUrl=getQrySender().getLoginUrl();
				if(loginUrl==null){
					loginUrl=getQrySender().getLoginUrl();
				}
				//response.sendRedirect(loginUrl);
			}else if (LoginTo.toLowerCase().equals("nc")) {
//				String loginurl = "http://10.200.20.28" //website 
				String loginurl = "http://"+ urlIp//website 
						+ "/service/ssoRegServlet?ssoKey="
						+ token + "&userCode=" + ncUsercode + "";// +"/service/ssoRegServlet?userCode="+username;
				HttpURLConnection connection;
				try {
					URL url = new URL(loginurl);
					connection = (HttpURLConnection) url.openConnection();
					connection.connect();
					BufferedReader in = null;
					in = new BufferedReader(new InputStreamReader(
							connection.getInputStream()));
					System.out.print(loginurl);
					Logger.warn(loginurl);
					String inputLine;
					String ssoKey = "";
					while ((inputLine = in.readLine()) != null) {
						ssoKey += inputLine;
					}
					if(ssoKey.toLowerCase().startsWith("error:")){
						pw.write(ssoKey);
					}
					else{
						response.sendRedirect("uclient://start/"+website + "/?ssoKey=" + ssoKey);
						Logger.warn("uclient://start/" + website + "/?ssoKey=" + ssoKey);
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}else if (LoginTo.toLowerCase().equals("portal")) {
				UserVO user = null;
				try {
					user = qryservice.findUserVO(dsName, ncUsercode);
				} catch (BusinessException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				String iamodecode = user.getIdentityverifycode();
				// 如果是配置了强制ca认证的帐套，那么业务中心管理员必须ca认证
				if (isForceCA(user.getUser_type())) {
					iamodecode = IAConstant.NCCACODE;
				}
				// username = user.getUser_code();

//				String loginurl = "http://10.200.20.28"//website
				String loginurl = "http://"+urlIp//website
						+ "/portal/registerServlet?type=2&dsname=" + dsName
						+ "&userid=" + ncUsercode;
				PortalLogger.debug(loginurl);
				HttpURLConnection connection;
				String key = UUID.randomUUID().toString();
				System.out.println(key);
				try {
					URL url = new URL(loginurl);
					connection = (HttpURLConnection) url.openConnection();
					connection.connect();
					BufferedReader in = null;
					in = new BufferedReader(new InputStreamReader(
							connection.getInputStream()));
					Logger.warn(loginurl);
					String inputLine;
					String ssoKey = "";
					while ((inputLine = in.readLine()) != null) {
						ssoKey += inputLine;
					}
					if(ssoKey.toLowerCase().startsWith("error:")){
						pw.write(ssoKey);
					}
					else{
						PortalLogger.debug(website + "/portal/pt/home/index?dsname="
								+ dsName + "&ssoKey="
								+ ssoKey.substring(2, ssoKey.length()));
						response.sendRedirect(website + "/portal/pt/home/index?dsname="
								+ dsName + "&ssoKey="
								+ ssoKey.substring(2, ssoKey.length()));
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			errormsg+="Login OK!";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			errormsg+="\r\n"+e.getMessage();
			pw.write(errormsg);
		}
		if(errormsg==null || errormsg=="")
			errormsg="Login OK!";
		logvo.setLog_content(errormsg);
		logvo.setDr(0);
		logvo.setTs(new UFDateTime(new Date()));
		try{
			qry.SaveSSOLog(logvo);
		}catch(Exception e){
			pw.write(e.getMessage());
			System.out.print(e.getMessage());
		}
	}
	
	
	private  String strUrl = "";
	
	private String getNcAndPortalIp(){
		String defUrl = "10.200.20.28";
		if(strUrl==null||strUrl.trim().length()==0){
			String strWhere = "  pub_sysinittemp.initcode  = 'OASSO_IP'  ";
//			String strWhere = "  initcode  = 'EOP_IP' and  pk_org = 'GLOBLE00000000000000'  ";
			String ordeStr  =" groupcode, pub_sysinittemp.initcode";
			
			ISysInitQry queryServer =  NCLocator.getInstance().lookup(ISysInitQry.class);
			try {
				SysInitVO[] querys =queryServer.getSysInitVOsFromJoinTable(strWhere,ordeStr);
				if(querys!=null&&querys.length>0){
					strUrl = querys[0].getValue();
					return strUrl;
				}else{
					return defUrl;
				}
				
			} catch (Exception e) {
				
				Logger.error("查询设置信息失败使用默认URL:"+defUrl);
				return defUrl;
				 
			}
		}else{
			return strUrl;
		}
		
		 
		
		
	}
	
	
	private boolean isForceCA(Integer user_type) {
		if (user_type == null)
			return false;
		if (INCUserTypeConstant.USER_TYPE_BUSICNETER_ADM != user_type
				.intValue())
			return false;
		return IAConfigJudger.getInstance().isCADataSource(
				InvocationInfoProxy.getInstance().getUserDataSource());
	}
	
	private void doReg(String userid, String ssoKey) throws BusinessException {
		HashMap<String, String> p = getParameterMap(userid);
		LfwSsoRegVO vo = new LfwSsoRegVO();
		vo.doSetRegmap(p);
		vo.setSsokey(ssoKey);
		getSsoService().creatSsoInfo(vo);
	}
	
	private String genKey() {
		SecureRandom sRandom = new SecureRandom();
		long t = System.currentTimeMillis();
		long t1 = sRandom.nextLong();
		return "" + t + t1;
	}

	/**
	 * 构造单点登陆参数集
	 */
	private HashMap<String, String> getParameterMap(String userid) {
		HashMap<String, String> p = new HashMap<String, String>();
		p.put("userid", userid);
		p.put("needverifypasswd", "N");
		return p;
	}

	protected ILfwSsoService getSsoService() {
		return NCLocator.getInstance().lookup(ILfwSsoService.class);
	}
}
