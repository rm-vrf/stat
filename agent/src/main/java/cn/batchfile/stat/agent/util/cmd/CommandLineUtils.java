package cn.batchfile.stat.agent.util.cmd;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

public abstract class CommandLineUtils {

	public static String replacePlaceholder(String s, Map<String, String> vars) {
		String ret = s;
		if (vars == null) {
			return ret;
		}
		
		String[] phs = StringUtils.substringsBetween(ret, "${", "}");
		if (phs != null) {
			for (String ph : phs) {
				// 替换环境变量
				String t = vars.containsKey(ph) ? vars.get(ph) : null;
				if (t != null) {
					ret = StringUtils.replace(ret, String.format("${%s}", ph), t);
				}
			}
		}
		return ret;
	}

	public static int execute(String command, String workDirectory, Map<String, String> vars, int timeoutInSeconds, 
			StringBuilder out, StringBuilder err) throws Exception {
		
		Commandline cmd = new Commandline();
		
		// 工作目录
		if (StringUtils.isNotEmpty(workDirectory)) {
			cmd.setWorkingDirectory(new File(workDirectory).getAbsolutePath());
		}
		
		// 环境变量
		if (vars != null) {
			for (Entry<String, String> entry : vars.entrySet()) {
				cmd.addEnvironment(entry.getKey(), replacePlaceholder(entry.getValue(), vars));
			}
		}
		
		// 运行参数
		String[] ary = org.codehaus.plexus.util.cli.CommandLineUtils.translateCommandline(command);
		for (String s : ary) {
			Arg argObject = cmd.createArg();
			argObject.setValue(replacePlaceholder(s, vars));
		}
		
		CommandLineCallable callable = CommandLineExecutor.executeCommandLine(cmd, null, new StreamConsumer() {
			@Override
			public void consumeLine(String line) {
				if (out != null) {
					out.append(line).append("\n");
				}
			}
		}, new StreamConsumer() {
			@Override
			public void consumeLine(String line) {
				if (err != null) {
					err.append(line).append("\n");
				}
			}
		}, timeoutInSeconds);
		return callable.call();
	}

}
