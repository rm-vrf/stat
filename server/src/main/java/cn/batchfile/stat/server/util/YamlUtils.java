package cn.batchfile.stat.server.util;

import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;

public abstract class YamlUtils {

    public static String toString(Object obj) {
        DumperOptions options = new DumperOptions();
        //options.setPrettyFlow(true);
        //options.setIndent(2);
        options.setLineBreak(DumperOptions.LineBreak.UNIX);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        StringWriter sw = new StringWriter();
        yaml.dump(obj, sw);

        String s = sw.toString();
        if (StringUtils.startsWith(s, "!!")) {
            String replace = StringUtils.substringBetween(s, "!!", DumperOptions.LineBreak.UNIX.getString());
            s = StringUtils.replace(s, "!!" + replace + DumperOptions.LineBreak.UNIX.getString(), "");
        }
        return s;
    }

    public static <T> T toObject(String s, Class<T> type) {
        DumperOptions options = new DumperOptions();
        //options.setPrettyFlow(true);
        //options.setIndent(2);
        options.setLineBreak(DumperOptions.LineBreak.UNIX);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        return yaml.loadAs(s, type);
    }

}
