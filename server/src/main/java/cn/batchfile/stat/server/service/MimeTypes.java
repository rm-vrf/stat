package cn.batchfile.stat.server.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MimeTypes {
	private static final Logger logger = LoggerFactory.getLogger(MimeTypes.class);
	private static final String TEXT_PLAIN = "text/plain";
	private boolean inited;
	private Map<String, String> mimes = new HashMap<String, String>();

	public String getMimeByExtension(String filename) {
		if (!inited) {
			init();
		}
		String ext = getExtension(filename);
		if (mimes.containsKey(ext)) {
			return mimes.get(ext);
		} else {
			return TEXT_PLAIN;
		}
	}

	private String getExtension(String file) {
		String name = file;
		if (StringUtils.contains(name, '/')) {
			name = StringUtils.substringAfterLast(name, "/");
		}
		return StringUtils.substringAfterLast(name, ".");
	}

	synchronized private void init() {
		if (inited) {
			return;
		}

		InputStream stream = null;
		try {
			stream = getClass().getClassLoader().getResourceAsStream("mime.types");
			List<String> lines = IOUtils.readLines(stream, "UTF-8");
			for (String line : lines) {
				line = StringUtils.trim(line);
				if (StringUtils.startsWith(line, "#")) {
					continue;
				}

				line = StringUtils.replaceChars(line, '\t', ' ');
				String[] ary = StringUtils.split(line, ' ');
				if (ary == null || ary.length < 2) {
					continue;
				}

				String contentType = ary[0];
				for (int i = 1; i < ary.length; i++) {
					String ext = ary[i];
					mimes.put(ext, contentType);
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(stream);
		}

		inited = true;
	}
}