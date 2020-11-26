package nc.util.info.sysimp;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import nc.bs.framework.common.NCLocator;
import nc.bs.framework.common.RuntimeEnv;
import nc.bs.logging.Logger;
import nc.itf.uif.pub.IUifService;
import nc.vo.pfxx.pub.PostFile;
import nc.vo.pfxx.pub.SendResult;
import nc.vo.pub.BusinessException;
import nc.vo.pub.para.SysInitVO;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
/**
 * 通过外部交换平台传输
 * @author pzm
 *
 */
public class NCConnTool {
	
	
	public  static final String KEY_CODE ="resultcode";
	
	public static final String KEY_INFO ="content";
	
	
	public static String getResPath(){
		String path = RuntimeEnv.getInstance().getNCHome() + "\\resources" ;
		File refile = new File(path+"\\returninformation");
		if(!refile.exists()){refile.mkdir();}
		return refile.getPath();
	}
	
 
	

	public static Map<String,String> tranToNC(String urla) throws BusinessException {
		Logger.error("############NCTONC#################");
		// 获取servlet连接并设置请求的方法
		String url = null;
		String file = null;
		try {
			IUifService service = NCLocator.getInstance().lookup(IUifService.class);
			SysInitVO[] svos = (SysInitVO[]) service.queryByCondition(SysInitVO.class, "initcode in ('RimIP','fileurl')");
			if(svos != null && svos.length>0){
				for(SysInitVO vo :svos){
					if("RimIP".equals(vo.getInitcode())){
						url = vo.getValue();
					}
					if("fileurl".equals(vo.getInitcode())){
						file = vo.getValue();
					}
				}
			}
//			File refile = new File(file+"\returninformation");
//			if(!refile.exists()){refile.mkdir();}
			Logger.error("############NCTONC .url:"+url+"#################");
			SendResult result = PostFile.sendFileWithResults(new File(urla), url,getResPath(), null, false, null);
			Map<String,String> returnmap = new HashMap<>();
			Document document1 = result.getBackDoc();
			NodeList nl = document1.getElementsByTagName("sendresult");
			for(int i= 0 ;i<nl.getLength();i++){
				Element node = (Element)nl.item(i);
				String resultcode = node.getElementsByTagName("resultcode").item(0).getFirstChild().getNodeValue();
				String resultdescription = node.getElementsByTagName("resultdescription").item(0).getFirstChild().getNodeValue();
				returnmap.put("content", resultdescription);
				returnmap.put("resultcode", resultcode);
			}

			Logger.error("############NCTONC.return"+returnmap.toString()+"#################");
			return returnmap;
 
		} catch (Exception e) {
			
			Logger.error(e.getMessage());
			Logger.error(e);
			Map<String,String> rtnMap = new HashMap<>();
			rtnMap.put(NCConnTool.KEY_CODE, "err999");
			rtnMap.put(NCConnTool.KEY_INFO, e.getMessage());
			return rtnMap;
			
		} 
	}

}
