package nc.pub.iufo.data.thread;

/**
 * @author 央客：王志强  缓存manager类。
 *
 */
public class IufoThreadLocalUtil {
	
	private static ThreadLocal<IufoThreadLocalObj>  handeler = new  ThreadLocal<IufoThreadLocalObj>(){
		
		@Override
		protected IufoThreadLocalObj initialValue() {
			return new IufoThreadLocalObj();
		}
	};
	public static void openCach(){
		  handeler.get().setUseCache(true);
	}
	public static void closeCach(){
		  handeler.get().setUseCache(false);
	}
	public static Object getValue(String key,AbstractQueryData queryData){
		return handeler.get().getValue(key, queryData);
	}
	public static void clean(){
		handeler.remove();
	}

}
