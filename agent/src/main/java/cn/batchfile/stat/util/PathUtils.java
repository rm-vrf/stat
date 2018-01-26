//package cn.batchfile.stat.util;
//
//import java.io.UnsupportedEncodingException;
//import java.net.URLEncoder;
//
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.lang.StringUtils;
//
//public class PathUtils {
//	private static final String DIR_SEPARATOR_UNIX = "" + IOUtils.DIR_SEPARATOR_UNIX;
//	private static final String DIR_SEPARATOR_WINDOWS = "" + IOUtils.DIR_SEPARATOR_WINDOWS;
//	private static final String DIR_SEPARATOR = "" + IOUtils.DIR_SEPARATOR;
//	private static final String DOUBLE_DIR_SEPARATOR = DIR_SEPARATOR + DIR_SEPARATOR;
//	
//	public static String concat(String... args) {
//		String s = StringUtils.EMPTY;
//		for (String arg : args) {
//			if (StringUtils.isEmpty(s)) {
//				s = arg;
//			} else {
//				s += DIR_SEPARATOR + arg;
//			}
//		}
//		
//		s = StringUtils.replaceEach(s, 
//				new String[] {DIR_SEPARATOR_UNIX, DIR_SEPARATOR_WINDOWS}, 
//				new String[] {DIR_SEPARATOR, DIR_SEPARATOR});
//		while (StringUtils.contains(s, DOUBLE_DIR_SEPARATOR)) {
//			s = StringUtils.replace(s, DOUBLE_DIR_SEPARATOR, DIR_SEPARATOR);
//		}
//		
//		return s;
//	}
//	
//	public static String encodeUrl(String s) {
//		try {
//			return URLEncoder.encode(s, "utf-8");
//		} catch (UnsupportedEncodingException e) {
//			throw new RuntimeException(e.getMessage(), e);
//		}
//	}
//}
