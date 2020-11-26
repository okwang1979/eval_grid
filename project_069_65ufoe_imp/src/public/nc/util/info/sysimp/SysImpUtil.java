package nc.util.info.sysimp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.uif.pub.IUifService;
import nc.vo.iufo.data.MeasurePubDataVO;
import nc.vo.iufo.keydef.KeyVO;
import nc.vo.pub.para.SysInitVO;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.ufsoft.table.CellsModel;

/**
 * 导入导出通用工具类
 * 
 * @author 王志强
 * 
 */

public class SysImpUtil {
	
	
	/**
	 * 将传入的对象系列化后，存入参数string指定的文件，并将序列化后的对象转换成十六进制字符串返回
	 * @param object 可序列化的对象
	 * @param string 存储文件名
	 * @return string 序列化后的对象的十六进制字符串
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String writeObject(Object object)
			throws FileNotFoundException, IOException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//用于将对象转换成byte[]数组的ObjectOutputStream
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		//将对象写入ByteArrayOutputStream
		oos.writeObject(object);
		byte[] bytes = baos.toByteArray();
		//用于将将对象存入文件的ObjectOutputStream
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream oos2 = new ObjectOutputStream(byteArrayOutputStream);
		
//		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
//		objectStr.getBytes("ISO-8859-1"));
		//将对象写入string指定的文件中
		oos2.writeObject(object);
		oos.close();
		oos2.close();
		baos.close();
		return bytesToHexString(bytes);

	}

	/**
	 * 将序列化后且用十六进制字符表示的对象反序列化成对象
	 * @param hexString 序列化对象的十六进制表示形式的字符串
	 * @return 反序列化生成的对象
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object readObject(String hexString) throws IOException,
			ClassNotFoundException {
		byte[] bytes = hexStringToBytes(hexString);
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bais);
		return ois.readObject();
	}

	/**
	 * 将传入的byte[]数组转换成十六机制数的字符串
	 * @param src 要转换的byte数组
	 * @return 返回十六进制的字符串
	 */
	private static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			//将一个byte的二进制数转换成十六进制字符
			String hv = Integer.toHexString(v);
			//如果二进制数转换成十六进制数高位为0，则加入'0'字符
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * 将传进来的十六进制表示的字符串转换成byte数组
	 * @param hexString
	 * @return 二进制表示的byte[]数组
	 */
	private static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase(Locale.getDefault());
		int length = hexString.length() / 2;
		//将十六进制字符串转换成字符数组
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			//一次去两个字符
			int pos = i * 2;
			//两个字符一个对应byte的高四位一个对应第四位
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	/**
	 * 将传进来的字符代表的数字转换成二进制数
	 * @param c 要转换的字符
	 * @return 以byte的数据类型返回字符代表的数字的二进制表示形式
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

//	/**
//	 * 将序列化后且用十六进制字符表示的对象反序列化成对象
//	 * @param hexString 序列化对象的十六进制表示形式的字符串
//	 * @return 反序列化生成的对象
//	 * @throws IOException
//	 * @throws ClassNotFoundException
//	 */
//	public static Object readObject(String hexString) throws IOException,
//			ClassNotFoundException {
//		byte[] bytes = hexStringToBytes(hexString);
//		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
//		ObjectInputStream ois = new ObjectInputStream(bais);
//		return ois.readObject();
//	}

//	/**
//	 * 对象序列化为字符串
//	 * 
//	 * @param object
//	 * @return
//	 */
//	public static String serializeObject(Object object) throws Exception {
//		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//		ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream);
//		out.writeObject(object);
//		// 必须是ISO-8859-1
////		String objectStr = byteArrayOutputStream.toString("ISO-8859-1");
//		String objectStr = byteArrayOutputStream.toString("UTF-8");
//		out.close();
//		byteArrayOutputStream.close();
//		return objectStr;
//	}
//
//	/**
//	 * 字符串序列化为对象
//	 * 
//	 * @param objectStr
//	 * @return
//	 * @throws Exception
//	 */
//	public static Object stringSerializeObject(String objectStr)
//			throws Exception {
//		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
//				objectStr.getBytes("ISO-8859-1"));
//		ObjectInputStream objectInputStream = new ObjectInputStream(
//				byteArrayInputStream);
//		Object object = objectInputStream.readObject();
//		objectInputStream.close();
//		byteArrayInputStream.close();
//		return object;
//	}

	/**
	 * 生成xml方法
	 */
	public static String createXml(String report_code, KeyVO[] keys,
			CellsModel model,MeasurePubDataVO pubVo,String repOrgStructCode,String orgCode,String taskCode ) {
		try {
			// 1、创建document对象
			Document document = DocumentHelper.createDocument();
			// 2、创建根节点rss
			
//			account="develop" billtype="report_sys_imp" filename="" groupcode="" isexchange="Y" replace="Y" roottag="" sender="U8"
			Element rss = document.addElement("ufinterface");
			// 3、向rss节点添加version属性
			rss.addAttribute("account", "develop");
			rss.addAttribute("billtype", "report_sys_imp");
			rss.addAttribute("filename", "");
			rss.addAttribute("groupcode", "");
			rss.addAttribute("isexchange", "Y");
			rss.addAttribute("replace", "Y");
			rss.addAttribute("roottag", "");
			rss.addAttribute("sender", "default");
 
			
			// 4、生成子节点及子节点内容
			Element bill = rss.addElement("bill");
			bill.addAttribute("id","");
			
			Element billhead = bill.addElement("billhead");
			 
			Element reportCode = billhead.addElement("reportCode");
			reportCode.setText(report_code);
			
			Element taskCodeEle = billhead.addElement("taskCode");
			taskCodeEle.setText(taskCode);
 
			
			
			Element keyCode1 = billhead.addElement("keyCode1");
			 
			keyCode1.setText(keys[0].getCode());
			
			Element keyCode2 = billhead.addElement("keyCode2");
			 
			keyCode2.setText(keys[1].getCode());
			
			if(keys.length>2){
				Element keyCode3 = billhead.addElement("keyCode3");
				 
				keyCode3.setText(keys[2].getCode());
			}

			
			
			Element keyValue1 = billhead.addElement("keyValue1");
			 
			keyValue1.setText(orgCode);
			
			Element keyValue2 = billhead.addElement("keyValue2");
			 
			keyValue2.setText(pubVo.getKeyDatas()[1].getCode());
			if(keys.length>2){
			Element keyValue3 = billhead.addElement("keyValue3");
			 
			keyValue3.setText(pubVo.getKeyDatas()[2].getCode());
			}
		 
			
			if(pubVo.getVer()!=0){
				Element versionEle = billhead.addElement("keyValue5");
				 //这里先放固定的后续需要配置
				versionEle.setText(""+pubVo.getVer() );		
			}
			Element repStructCode = billhead.addElement("repStructCode");
			 //这里先放固定的后续需要配置
			repStructCode.setText(repOrgStructCode );			
			
			
			
			Element reportinfos = billhead.addElement("reportinfos");
			
			
			 
			reportinfos.setText("dd");
			
			
			Element reportData = billhead.addElement("reportData");
			String strValue = writeObject(model);
			reportData.setText(strValue);
		 
			
			
	
		 
			 
//			Object obj = readObject(strValue);
//			title.setText("sdfsdfsd");
			// 5、设置生成xml的格式
			
			
			OutputFormat format = OutputFormat.createPrettyPrint();
			// 设置编码格式
			format.setEncoding("UTF-8");

			// 6、生成xml文件
			//修改生成文件名称增加时间戳及增加配置文件begin pzm
			String fileurl = NCConnTool.getResPath();
//			IUifService service = NCLocator.getInstance().lookup(IUifService.class);
//			SysInitVO[] svos = (SysInitVO[]) service.queryByCondition(SysInitVO.class, "initcode = 'fileurl'");
//			if(svos != null && svos.length>0){
//				fileurl = svos[0].getValue();
//			}
			
			StringBuffer filename = new StringBuffer();
			filename.append(fileurl+"\\");
			filename.append(report_code);
			filename.append(System.currentTimeMillis()).append(".xml");
			File file = new File(filename.toString());
			XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
			// 设置是否转义，默认使用转义字符
			writer.setEscapeText(false);
			writer.write(document);
			writer.close();
//			System.out.println("生成rss.xml成功");
			return filename.toString();
		} catch (Exception e) {
			Logger.init("iufoRepCalcResult");
			Logger.error(e);
			Logger.init();

		}
		return null;

	}
	
	
	
}
