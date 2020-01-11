package cn.batchfile.stat.server.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import cn.batchfile.stat.server.domain.resource.FileInstance;
import cn.batchfile.stat.server.service.FileService;

@RestController
public class FileController {
    private static final Logger LOG = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    @PostMapping("/api/file/{namespace}/**/_mkdir")
    public ResponseEntity<FileInstance> mkdir(HttpServletRequest request,
    		@PathVariable("namespace") String namespace) throws IOException {

        String begin = "/api/file/" + namespace + "/";
        String end = "/_mkdir";
        String name = StringUtils.substringBetween(request.getRequestURI(), begin, end);
        LOG.info("mkdir {}/{}", namespace, name);

        FileInstance dir = fileService.createDirectory(namespace, name);
        if (dir == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(dir, HttpStatus.OK);
        }
    }

    @GetMapping(value = {"/api/file/{namespace}/**/_ls", "/api/file/{namespace}/_ls"})
    public ResponseEntity<Page<FileInstance>> ls(HttpServletRequest request,
    		@PathVariable("namespace") String namespace,
    		@PageableDefault(value = 10, sort = {"name"}, direction = Sort.Direction.ASC) Pageable pageable) {

        String begin = "/api/file/" + namespace + "/";
        String end = "/_ls";
        String name = StringUtils.substringBetween(request.getRequestURI(), begin, end);
        LOG.debug("ls {}/{}", namespace, name);

        Page<FileInstance> files = fileService.listFiles(namespace, name, pageable);
        if (files == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(files, HttpStatus.OK);
        }
    }

    @DeleteMapping("/api/file/{namespace}/**/_rmdir")
    public ResponseEntity<FileInstance> rmdir(HttpServletRequest request,
    		@PathVariable("namespace") String namespace) {

        String begin = "/api/file/" + namespace + "/";
        String end = "/_rmdir";
        String name = StringUtils.substringBetween(request.getRequestURI(), begin, end);
        LOG.info("rmdir {}/{}", namespace, name);

        FileInstance file = fileService.deleteFile(namespace, name);
        if (file == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(file, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/api/file/{namespace}/**", method = RequestMethod.POST, consumes = "multipart/form-data")
    public ResponseEntity<FileInstance> createFile(HttpServletRequest request,
    		@RequestParam("file") MultipartFile file,
    		@PathVariable("namespace") String namespace) throws IOException {

        String begin = "/api/file/" + namespace + "/";
        String name = StringUtils.substringAfter(request.getRequestURI(), begin);
        LOG.info("upload multipart file {}/{}, {}", namespace, name, file.getSize());

        InputStream stream = file.getInputStream();
        try {
        	long size = file.getSize();
        	FileInstance f = fileService.createFile(namespace, name, size, stream);
	        if (f == null) {
	            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	        } else {
	            return new ResponseEntity<>(f, HttpStatus.OK);
	        }
        } finally {
        	try {
        		stream.close();
        	} catch (Exception e) {}
        }
    }

    @RequestMapping(value = "/api/file/{namespace}/**", method = RequestMethod.POST, consumes = "application/octet-stream")
    public ResponseEntity<FileInstance> createFile(HttpServletRequest request,
    		@PathVariable("namespace") String namespace) throws IOException {

        String begin = "/api/file/" + namespace + "/";
        String name = StringUtils.substringAfter(request.getRequestURI(), begin);
        long size = request.getContentLengthLong();
        LOG.info("upload body file {}/{}, {}", namespace, name, size);

        InputStream stream = request.getInputStream();
        try {
        	FileInstance f = fileService.createFile(namespace, name, size, stream);
	        if (f == null) {
	            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	        } else {
	            return new ResponseEntity<>(f, HttpStatus.OK);
	        }
        } finally {
        	try {
        		stream.close();
        	} catch (Exception e) {}
        }
    }

    @RequestMapping(value = "/api/file/{namespace}/**", method = RequestMethod.HEAD)
    public ResponseEntity<Void> headFile(HttpServletRequest request,
    		@PathVariable("namespace") String namespace) {

        String begin = "/api/file/" + namespace + "/";
        String name = StringUtils.substringAfter(request.getRequestURI(), begin);
        LOG.debug("head {}/{}", namespace, name);

        FileInstance file = fileService.getFile(namespace, name);
        if (file == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentLength(file.getSize());
            headers.setLastModified(file.getTimestamp().getTime());
            return new ResponseEntity<>(headers, HttpStatus.OK);
        }
    }

    @GetMapping("/api/file/{namespace}/**")
    public ResponseEntity<Void> getFile(HttpServletRequest request,
    		WebRequest req,
    		HttpServletResponse response,
    		@PathVariable("namespace") String namespace) throws IOException {

        String begin = "/api/file/" + namespace + "/";
        String name = StringUtils.substringAfter(request.getRequestURI(), begin);
        LOG.debug("get {}/{}", namespace, name);

        FileInstance file = fileService.getFile(namespace, name);
        if (file == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders headers = new HttpHeaders();
        //headers.setLastModified(file.getTimestamp().getTime());

        boolean modified = req.checkNotModified(file.getTimestamp().getTime());
        if (modified) {
            headers.setContentLength(0);
            return new ResponseEntity<>(headers, HttpStatus.NOT_MODIFIED);
        }

        if (file.getDirectory()) {
        	headers.setContentLength(0);
        	return new ResponseEntity<>(headers, HttpStatus.OK);
        } else {
	        InputStream inputStream = fileService.getStream(namespace, name);
	        OutputStream outputStream = response.getOutputStream();
	        try {
		        headers.setContentLength(file.getSize());
		        response.setHeader("Content-Length", String.valueOf(file.getSize()));
		        IOUtils.copyLarge(inputStream, outputStream);
		        return new ResponseEntity<>(headers, HttpStatus.OK);
	        } finally {
	        	try {
	        		inputStream.close();
	        	} catch (Exception e) {}
	        	try {
	        		outputStream.close();
	        	} catch (Exception e) {}
	        }
        }
    }

    @DeleteMapping("/api/file/{namespace}/**")
    public ResponseEntity<FileInstance> deleteFile(HttpServletRequest request,
    		@PathVariable("namespace") String namespace) {
        
        String begin = "/api/file/" + namespace + "/";
        String name = StringUtils.substringAfter(request.getRequestURI(), begin);
        LOG.info("rm {}/{}", namespace, name);
        
        FileInstance file = fileService.deleteFile(namespace, name);
        if (file == null) {
        	return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
        	return new ResponseEntity<>(file, HttpStatus.OK);
        }
    }

}
