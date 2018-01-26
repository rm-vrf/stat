//package cn.batchfile.stat.util;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.commons.lang.StringUtils;
//
//public class ArgumentUtils {
//	public static String getCommand(String[] args) {
//		String command = StringUtils.EMPTY;
//		for (String arg : args) {
//			if (!StringUtils.startsWithIgnoreCase(arg, "-")) {
//				command = arg;
//				break;
//			}
//		}
//		return command;
//	}
//
//	public static List<String> getArguments(String[] args, String name, String shortName) {
//		String fulltext = String.format("--%s=", name);
//		String shorttext = String.format("-", shortName);
//		
//		List<String> list = new ArrayList<String>();
//		
//		for (String arg : args) {
//			if (StringUtils.startsWithIgnoreCase(arg, fulltext)) {
//				list.add(StringUtils.substring(arg, fulltext.length()));
//			} else if (StringUtils.startsWithIgnoreCase(arg, shorttext)) {
//				list.add(StringUtils.substring(arg, shorttext.length()));
//			}
//		}
//		return list;
//	}
//
//	public static String getArgument(String[] args, String name, String shortName) {
//		String fulltext = String.format("--%s=", name);
//		String shorttext = String.format("-%s", shortName);
//		
//		for (String arg : args) {
//			if (StringUtils.startsWithIgnoreCase(arg, fulltext)) {
//				return StringUtils.substring(arg, fulltext.length());
//			} else if (StringUtils.startsWithIgnoreCase(arg, shorttext)) {
//				return StringUtils.substring(arg, shorttext.length());
//			}
//		}
//		return null;
//	}
//}
