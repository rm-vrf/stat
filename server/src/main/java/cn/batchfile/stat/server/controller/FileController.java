package cn.batchfile.stat.server.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import cn.batchfile.stat.domain.resource.File;
import cn.batchfile.stat.server.service.FileService;

@RestController
public class FileController {
    private static final Logger LOG = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    @PostMapping("/api/file/{namespace}/**/_mkdir")
    public ResponseEntity<File> makeDirectory(HttpServletRequest request,
                                                @PathVariable("namespace") String namespace) throws IOException {

        String begin = "/api/file/" + namespace + "/";
        String end = "/_mkdir";
        String name = StringUtils.substringBetween(request.getRequestURI(), begin, end);
        LOG.info("mkdir {}/{}", namespace, name);

        File dir = fileService.createDirectory(namespace, name);
        if (dir == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(dir, HttpStatus.OK);
        }
    }

    @GetMapping(value = {"/api/file/{namespace}/**/_ls", "/api/file/{namespace}/_ls"})
    public ResponseEntity<List<File>> listDirectory(HttpServletRequest request,
                                                    @PathVariable("namespace") String namespace) {

        String begin = "/api/file/" + namespace + "/";
        String end = "/_ls";
        String name = StringUtils.substringBetween(request.getRequestURI(), begin, end);
        LOG.debug("ls {}/{}", namespace, name);

        List<File> files = fileService.listFiles(namespace, name);
        if (files == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(files, HttpStatus.OK);
        }
    }

    @DeleteMapping("/api/file/{namespace}/**/_rmdir")
    public ResponseEntity<File> removeDirectory(HttpServletRequest request,
                                                @PathVariable("namespace") String namespace) {

        String begin = "/api/file/" + namespace + "/";
        String end = "/_rmdir";
        String name = StringUtils.substringBetween(request.getRequestURI(), begin, end);
        LOG.info("rmdir {}/{}", namespace, name);

        File file = fileService.deleteFile(namespace, name);
        if (file == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(file, HttpStatus.OK);
        }
    }

    @PostMapping("/api/file/{namespace}/**/_mv")
    public ResponseEntity<File> moveFile(HttpServletRequest request,
                                         @PathVariable("namespace") String namespace,
                                         @RequestParam("target") String target) throws IOException {

        String begin = "/api/file/" + namespace + "/";
        String end = "/_mv";
        String name = StringUtils.substringBetween(request.getRequestURI(), begin, end);
        LOG.info("mv {}/{} {}", namespace, name, target);

        File file = fileService.moveFile(namespace, name, target);
        if (file == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(file, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/api/file/{namespace}/**", method = RequestMethod.POST, consumes = "multipart/form-data")
    public ResponseEntity<File> createFile(HttpServletRequest request,
                                           @RequestParam("file") MultipartFile file,
                                           @PathVariable("namespace") String namespace) throws IOException {

        String begin = "/api/file/" + namespace + "/";
        String name = StringUtils.substringAfter(request.getRequestURI(), begin);
        LOG.info("upload multipart file {}/{}, {}", namespace, name, file.getSize());

        InputStream stream = file.getInputStream();
        try {
	        File f = fileService.createFile(namespace, name, file.getSize(), stream);
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
    public ResponseEntity<File> createFile(HttpServletRequest request,
                                           @PathVariable("namespace") String namespace) throws IOException {

        String begin = "/api/file/" + namespace + "/";
        String name = StringUtils.substringAfter(request.getRequestURI(), begin);
        long size = request.getContentLengthLong();
        LOG.info("upload body file {}/{}, {}", namespace, name, size);

        InputStream stream = request.getInputStream();
        try {
	        File f = fileService.createFile(namespace, name, size, stream);
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

        File file = fileService.getFile(namespace, name);
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

        File file = fileService.getFile(namespace, name);
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

    @DeleteMapping("/api/file/{namespace}/**")
    public ResponseEntity<File> deleteFile(HttpServletRequest request,
                                           @PathVariable("namespace") String namespace) {
        
        String begin = "/api/file/" + namespace + "/";
        String name = StringUtils.substringAfter(request.getRequestURI(), begin);
        LOG.info("rm {}/{}", namespace, name);
        
        File file = fileService.deleteFile(namespace, name);
        if (file == null) {
        	return new ResponseEntity<File>(HttpStatus.NO_CONTENT);
        } else {
        	return new ResponseEntity<File>(file, HttpStatus.OK);
        }
    }

}
