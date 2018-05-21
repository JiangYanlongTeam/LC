package weaver.interfaces.jiangyl.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sdicons.json.mapper.JSONMapper;
import com.sdicons.json.mapper.MapperException;
import com.sdicons.json.model.JSONValue;

public class JsonUtil {

	/**
	 * JAVA对象转换成JSON字符串
	 * @param obj
	 * @return
	 * @throws MapperException
	 */	
	public static String objectToJsonStr(Object obj) throws MapperException{
        JSONValue jsonValue = JSONMapper.toJSON(obj);  
        String jsonStr = jsonValue.render(false);
        return jsonStr;
	}
	
	/**
	 * 重载objectToJsonStr方法
	 * @param obj 需要转换的JAVA对象
	 * @param format 是否格式化
	 * @return
	 * @throws MapperException
	 */
	public static String objectToJsonStr(Object obj,boolean format) throws MapperException{
        JSONValue jsonValue = JSONMapper.toJSON(obj);  
        String jsonStr = jsonValue.render(format);
        return jsonStr;
	}	
	
	/**
	 * 将JAVA对象转换成JSON字符串
	 * @param obj
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("rawtypes")
	public static String simpleObjectToJsonStr(Object obj,List<Class> claList) throws IllegalArgumentException, IllegalAccessException{
		if(obj==null){
			return "null";
		}
		String jsonStr = "{";
		Class<?> cla = obj.getClass();
		Field fields[] = cla.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			if(field.getType() == long.class){
				jsonStr += "\""+field.getName()+"\":"+field.getLong(obj)+",";
			}else if(field.getType() == double.class){
				jsonStr += "\""+field.getName()+"\":"+field.getDouble(obj)+",";
			}else if(field.getType() == float.class){
				jsonStr += "\""+field.getName()+"\":"+field.getFloat(obj)+",";
			}else if(field.getType() == int.class){
				jsonStr += "\""+field.getName()+"\":"+field.getInt(obj)+",";
			}else if(field.getType() == boolean.class){
				jsonStr += "\""+field.getName()+"\":"+field.getBoolean(obj)+",";
			}else if(field.getType() == Integer.class||field.getType() == Boolean.class
					||field.getType() == Double.class||field.getType() == Float.class					
					||field.getType() == Long.class){				
				jsonStr += "\""+field.getName()+"\":"+field.get(obj)+",";
			}else if(field.getType() == String.class){
				jsonStr += "\""+field.getName()+"\":\""+field.get(obj)+"\",";
			}else if(field.getType() == List.class){
				String value = simpleListToJsonStr((List<?>)field.get(obj),claList);
				jsonStr += "\""+field.getName()+"\":"+value+",";				
			}else{		
				if(claList!=null&&claList.size()!=0&&claList.contains(field.getType())){
					String value = simpleObjectToJsonStr(field.get(obj),claList);
					jsonStr += "\""+field.getName()+"\":"+value+",";					
				}else{
					jsonStr += "\""+field.getName()+"\":null,";
				}
			}
		}
		jsonStr = jsonStr.substring(0,jsonStr.length()-1);
		jsonStr += "}";
			return jsonStr;		
	}
	
	/**
	 * 将JAVA的LIST转换成JSON字符串
	 * @param list
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("rawtypes")
	public static String simpleListToJsonStr(List<?> list,List<Class> claList) throws IllegalArgumentException, IllegalAccessException{
		if(list==null||list.size()==0){
			return "[]";
		}
		String jsonStr = "[";
		for (Object object : list) {
			jsonStr += simpleObjectToJsonStr(object,claList)+",";
		}
		jsonStr = jsonStr.substring(0,jsonStr.length()-1);
		jsonStr += "]";		
		return jsonStr;
	}	
	
	/**
	 * 将JAVA的MAP转换成JSON字符串，
	 * 只转换第一层数据
	 * @param map
	 * @return
	 */
	public static String simpleMapToJsonStr(Map<?,?> map){
		if(map==null||map.isEmpty()){
			return "null";
		}
		String jsonStr = "{";
		Set<?> keySet = map.keySet();
		for (Object key : keySet) {
			jsonStr += "\""+key+"\":\""+map.get(key)+"\",";		
		}
		jsonStr = jsonStr.substring(0,jsonStr.length()-1);
		jsonStr += "}";
		return jsonStr;
	}
}
