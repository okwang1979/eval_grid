package nc.pub.iufo.data.thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 央客：王志强 at：2019-9-30 国旅项目现场缓存专用类
 *
 */
public class IufoThreadLocalObj {
	
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
				System.out.println(key);
				Object value = queryData.qqueryData();
				valueMap.put(key, value);
				return value;
			}
			
		}else{
			return queryData.qqueryData();
		}
	}

}
