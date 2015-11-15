package cn.batchfile.stat.agent.controller;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.batchfile.stat.agent.domain.File;
import cn.batchfile.stat.agent.service.FileService;

@Controller
public class FileController {
	
	@Resource(name="fileService")
	private FileService fileService;
	
	@RequestMapping(value="/file", method=RequestMethod.GET)
	@ResponseBody
	public File getFile(@RequestParam(value="path") String path) throws IOException {
		return fileService.getFile(path);
	}

	@RequestMapping(value="/file/_list", method=RequestMethod.GET)
	@ResponseBody
	public List<File> listFiles(@RequestParam(value="path", required=false) String path) throws IOException {
		if (StringUtils.isEmpty(path)) {
			return fileService.listRoots();
		} else {
			return fileService.listFiles(path);
		}
	}
}
