package nc.webimpl.pub;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.arap.IProcurementInvoiceMaintain;
import nc.itf.uap.busibean.ISysInitQry;
import nc.pubitf.para.SysInitQuery;
import nc.vo.arap.proinvoice.AggProcurementInvoiceHVO;
import nc.vo.log.pub.ItfLogVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.para.SysInitVO;
import nc.webitf.pub.ISynDataService;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultDocument;

public class SynDataServiceImpl implements ISynDataService{

	@Override
	public String synchronizeData(String xmlStr) {
		String urlStr =this.getURL();
		if ("".equals(xmlStr) || xmlStr == null) {
			String retXML=getErrorReturnXml("传输的数据为空，不做处理!");
			try {
				writeLog(xmlStr, retXML,"新增");
			} catch (Exception e1) {
			}
			return retXML;
		}
		int sysint_basews03 = 1000 * 60 * 30;
		String sysinit_basews01 = null;
		try {
			sysinit_basews01 = SysInitQuery.getParaString(
					"GLOBLE00000000000000", "ZMKK001");// 外部交换平台接口地址参数
		} catch (Exception e) {
			String retXML=getErrorReturnXml("NC获取外部交换平台接口地址参数值异常：" + e.getMessage());
			try {
				writeLog(xmlStr, retXML,"新增");
			} catch (Exception e1) {
			}
			return retXML;
		}
		StringBuffer strbuf = new StringBuffer();
		try {
			Logger.error("nc.webimpl.pub.SynDataServiceImpl数据交换平台接口地址"+sysinit_basews01);
			
			URL realURL = new URL("http://"+urlStr+"/service/XChangeServlet?account=001&groupcode=0001");
			Logger.error("nc.webimpl.pub.SynDataServiceImpl网址地址"+realURL);
			HttpURLConnection connection = (HttpURLConnection) realURL
					.openConnection();
			connection.setConnectTimeout(sysint_basews03);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-type", "text/xml");
			connection.setRequestMethod("POST");
			// 解决生僻汉字乱码问题
			// Document doc = DocumentHelper.parseText(xmlstr);
			DefaultDocument doc = (DefaultDocument) DocumentHelper
					.parseText(xmlStr);
			doc.setXMLEncoding("GBK");
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("GBK");
			OutputStreamWriter osw = new OutputStreamWriter(
					connection.getOutputStream(), "GBK");
			// end

			XMLWriter writer = new XMLWriter(osw, format);
			writer.write(doc);
			writer.close();
			InputStream inputStream = connection.getInputStream();
			byte tempBytes[] = new byte[2048];
			for (int count = 0; (count = inputStream.read(tempBytes)) != -1;)
				strbuf.append(new String(tempBytes, 0, count, "utf-8"));

			if (inputStream != null)
				inputStream.close();
			connection.disconnect();
		} catch (Exception e) {
			Logger.error("nc.webimpl.pub.SynDataServiceImpl异常"+e);
			String retXML=getErrorReturnXml("NC内部处理异常：" + e.getMessage());
			try {
				writeLog(xmlStr, getErrorReturnXml("NC内部处理异常：" + e.getMessage()),"新增");
			} catch (Exception e1) {
			}
			return retXML;
		}
		try {
			writeLog(xmlStr, strbuf.toString(),"新增");
		} catch (Exception e) {
		}
		return strbuf.toString();
	}
	
	
	private  String strUrl = "";
	
	private String getURL(){
		String defUrl = "fssc.cdfg.com.cn";
		if(strUrl==null||strUrl.trim().length()==0){
			String strWhere = "  pub_sysinittemp.initcode  = 'EOP_IP'  ";
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
	
	
	@Override
	public String deleteSynData(String xmlStr) {
		if(xmlStr==null || xmlStr.length()<=0){
			String retXML=getErrorReturnXml("xml不能为空");
			try {
				writeLog(xmlStr, retXML,"删除");
			} catch (Exception e1) {
			}
			return retXML;
		}
		Document document = null;
		try {
			document = DocumentHelper.parseText(xmlStr);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		Element root = document.getRootElement();
		root.attributes();
		List<Element> childElements = root.elements();
		HashMap<String, String> map=new HashMap<String, String>();
		for (Element child : childElements) {
			  readXml(child, map);
		}
		if(map==null || map.size()<=0 ){
			String retXML=getErrorReturnXml("XML解析错误");
			try {
				writeLog(xmlStr, retXML,"删除");
			} catch (Exception e1) {
			}
			return retXML;
		}
		try {
			 IProcurementInvoiceMaintain itf=NCLocator.getInstance().lookup(IProcurementInvoiceMaintain.class);
			 AggProcurementInvoiceHVO[]  aggs=itf.queryBillByPK(new String[]{map.get("vbillpk")});
			 if(aggs==null || aggs.length<=0){
				 String retXML=getErrorReturnXml("数据不存在");
				 writeLog(xmlStr, retXML,"删除");
				 return retXML;
			 }
			 if(aggs[0].getParentVO().getVbillstatus()!=-1){
				 String retXML=getErrorReturnXml("不是自由态的单据,不允许删除!");
				 writeLog(xmlStr, retXML,"删除");
				 return retXML;
			 }
			 itf.delete(aggs, aggs);
		} catch (BusinessException e) {
			String retXML=getErrorReturnXml("数据操作错误："+e.getMessage());
			try {
				writeLog(xmlStr, retXML,"删除");
			} catch (Exception e1) {
			}
			return retXML;
		}
		String retXML=getReturnXml("删除完成");
		writeLog(xmlStr, retXML,"删除");
		return retXML;
	}
	private String getErrorReturnXml(String msg) {
		return "<?xml version='1.0' encoding='UTF-8'?>" + "<ufinterface>"
				+ "<sendresult>" + "<billpk/>" + "<bdocid/>" + "<filename/>"
				+ "<resultcode>-1</resultcode>" + "<resultdescription>" + msg
				+ "</resultdescription>" + "<content/>" + "</sendresult>"
				+ "</ufinterface>";
	}
	private String getReturnXml(String msg) {
		return "<?xml version='1.0' encoding='UTF-8'?>" + "<ufinterface>"
				+ "<sendresult>" + "<billpk/>" + "<bdocid/>" + "<filename/>"
				+ "<resultcode>1</resultcode>" + "<resultdescription>" + msg
				+ "</resultdescription>" + "<content/>" + "</sendresult>"
				+ "</ufinterface>";
	}
	private void writeLog(String recXML,String retXML,String type){
		Document document = null;
		try {
			document = DocumentHelper.parseText(retXML);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		Element root = document.getRootElement();
		root.attributes();
		List<Element> childElements = root.elements();
		HashMap<String, String> map=new HashMap<String, String>();
		for (Element child : childElements) {
			  readXml(child, map);
		}
		insertLog(recXML, retXML, map,type);
	}
	private  void readXml(Element element,HashMap<String, String> hashMap){
		element.getName();
		if(element.elements()!=null && element.elements().size()>0){
			 List<Element> elementList = element.elements();
			 for(Element ele : elementList) {
				readXml(ele, hashMap); 
			 }
		}else{
			hashMap.put(element.getName(), element.getText());
		}
	}
	private void insertLog(String recXML,String retXML,HashMap<String, String> map,String type){
		ItfLogVO logVO=new ItfLogVO();
		logVO.setDr(0);
		logVO.setReceive_date(new UFDate());
		if(recXML!=null && recXML.length()>2000){
			String recXML1=recXML.substring(0, 2000);
			String recXML2=recXML.substring(2000, recXML.length());
			logVO.setReceive_xml(recXML1);
			logVO.setReceive_xml2(recXML2);
			if(recXML2.length()>2000){
				logVO.setReceive_xml2(recXML2.substring(0, 2000)+"...");
			}
		}else{
			logVO.setReceive_xml(recXML);
		}
		if("新增".equals(type)){
			logVO.setAction("新增");
			if(map.get("content")!=null){
				String [] strs=map.get("content").split(",");
				logVO.setBilltype(strs[0]);
				logVO.setBillid(strs[1]);
				logVO.setBillcode(strs[2]);
			}
		}
		logVO.setReturnxml(retXML);

		if("1".equals(map.get("resultcode"))){
			logVO.setIssuccess(UFBoolean.TRUE);
		}else if("-1".equals(map.get("resultcode"))){
			logVO.setIssuccess(UFBoolean.FALSE);
		}
		try {
			new BaseDAO().insertVO(logVO);
		} catch (DAOException e) {
			e.printStackTrace();
		}
	}
}
