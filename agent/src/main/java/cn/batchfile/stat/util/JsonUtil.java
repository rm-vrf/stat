//package cn.batchfile.stat.util;
//
//import java.lang.reflect.Type;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.TypeReference;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//
//public abstract class JsonUtil {
//
//	private static SerializerFeature[] features = new SerializerFeature[] {
//			SerializerFeature.PrettyFormat,
//			SerializerFeature.WriteMapNullValue,
//			SerializerFeature.WriteDateUseDateFormat,
//			SerializerFeature.BrowserCompatible,
//			SerializerFeature.DisableCircularReferenceDetect
//		};
//		
//		static {
//			JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
//		};
//		
//		public static String encode(Object object) {
//			return JSON.toJSONString(object, features);
//		}
//
//		public static <T> T decode(String json, Class<T> clazz) {
//			T t = JSON.parseObject(json, clazz);
//			return t;
//	    }
//		
//		public static <T> T decode(String json, Type type) {
//			T t = JSON.parseObject(json, type);
//			return t;
//		}
//		
//		public static <T> T decode(String json, TypeReference<T> typeRef) {
//	    	return (T)JSON.parseObject(json, typeRef);
//	    }
//}
