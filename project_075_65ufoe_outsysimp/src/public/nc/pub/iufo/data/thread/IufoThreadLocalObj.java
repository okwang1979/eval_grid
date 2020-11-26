package nc.pub.iufo.data.thread;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ��ͣ���־ǿ at��2019-9-30 ������Ŀ�ֳ�����ר����
 *
 */
public class IufoThreadLocalObj {
	
//	private Set<String> 
	
	
	private boolean useCache = false;
	

	
	private Map<String,Object> valueMap =  new HashMap<String, Object>();
	
	

	
	
	
	public boolean isUseCache() {
		return useCache;
	}




	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}




	public Object getValue(String key,AbstractQueryData queryData){
		if(useCache){
			if(valueMap.containsKey(key)){
				return valueMap.get(key);
			}else{
				Object value = queryData.qqueryData();
				valueMap.put(key, value);
				return value;
			}
			
		}else{
			return queryData.qqueryData();
		}
	}

}
