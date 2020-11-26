package nc.vo.ct.saledaily.entity;
 
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import javax.validation.constraints.Size;

import javax.validation.constraints.NotNull;

import nc.vo.pub.BusinessRuntimeException;
 
 
/**
 * @author Administrator
 *
 */
public class SaleParamCheckUtils {
     
    /**
     * 通过反射来获取javaBean上的注解信息，判断属性值信息，然后通过注解元数据
     * 来返回
     * @param t
     */
    public static <T> void doValidator(T t){
        Class<?> clazz = t.getClass();
        Field[] fields = clazz.getDeclaredFields();
        String message ="合同接口数据传输要求:";
        for(Field field:fields){
            NotNull notNull = field.getDeclaredAnnotation(NotNull.class);
            if(null!=notNull){
                Object value = getValue(t,field.getName());
                if(!notNull(value)){
                     throw new BusinessRuntimeException(message+field.getName()+"不允许为空!");
                }
            
            }
            Size size  = field.getDeclaredAnnotation(Size.class);
            if(size!=null) {
          	  Object value = getValue(t,field.getName());
          	  int max = size.max();
          	  if(value!=null) {
          		  if(String.valueOf(value).length()>max) {
          			throw new BusinessRuntimeException(message+field.getName()+"长度超长,最大允许:"+max);
          		  }
          	  }
            }
          
        }
    }
     
    public static <T> Object getValue(T t,String fieldName){
        Object value = null;
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(t.getClass());
            PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
            for(PropertyDescriptor property:props){
                if(fieldName.equals(property.getName())){
                    Method method = property.getReadMethod();
                    value = method.invoke(t,new Object[]{});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
     
    public static boolean notNull(Object value){
        if(null==value){
            return false;
        }
        if(value instanceof String && isEmpty((String)value)){
            return false;
        }
        if(value instanceof List && isEmpty((List<?>)value)){
            return false;
        }
        return null!=value;
    }
     
    public static boolean isEmpty(String str){
        return null==str || str.isEmpty();
    }
     
    public static boolean isEmpty(List<?> list){
        return null==list || list.isEmpty();
    }
     
     
}