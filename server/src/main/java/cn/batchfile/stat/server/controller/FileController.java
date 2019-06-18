package cn.batchfile.stat.server.controller;

import cn.batchfile.stat.domain.Name;
import cn.batchfile.stat.domain.service.File;
import cn.batchfile.stat.server.service.FileService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
public class FileController {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    @PostMapping("/api/v2/file")
    public String postFile(@RequestParam("file") MultipartFile file) throws IOException {
        return postFile(file, Name.DEFAULT_NAMESPACE, file.getOriginalFilename());
    }

    @PostMapping("/api/v2/file/{namespace:.+}")
    public String postFile(@RequestParam("file") MultipartFile file,
                           @PathVariable("namespace") String namespace) throws IOException {
        return postFile(file, namespace, file.getOriginalFilename());
    }

    @PostMapping("/api/v2/file/{namespace}/{name:.+}")
    public String postFile(@RequestParam("file") MultipartFile file,
                           @PathVariable("namespace") String namespace,
                           @PathVariable("name") String name) throws IOException {

        long size = file.getSize();
        String contentType = file.getContentType();
        return fileService.postFile(namespace, name, contentType, size, file.getInputStream());
    }

    @PutMapping("/api/v2/file")
    public String putFile(@RequestBody File file) {
        return fileService.putFile(file);
    }

    @DeleteMapping("/api/v2/file/{namespace:.+}")
    public List<String> deleteFiles(@PathVariable("namespace") String namespace) {
        return fileService.deleteFiles(namespace);
    }

    @DeleteMapping("/api/v2/file/{namespace}/{name:.+}")
    public ResponseEntity<String> deleteFile(@PathVariable("namespace") String namespace,
                                     @PathVariable("name") String name) {
        String s = fileService.deleteFile(namespace, name);
        if (StringUtils.isEmpty(s)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(s, HttpStatus.OK);
        }
    }

    @GetMapping("/api/v2/file")
    public List<File> getFiles() {
        return fileService.getFiles();
    }

    @GetMapping("/api/v2/file/{namespace:.+}")
    public List<File> getFiles(@PathVariable("namespace") String namespace) {
        return fileService.getFiles(namespace);
    }

    @GetMapping("/api/v2/file/{namespace}/{name:.+}")
    public ResponseEntity<InputStreamResource> getStream(@PathVariable("namespace") String namespace,
                                                         @PathVariable("name") String name,
                                                         WebRequest request)
            throws IOException, URISyntaxException {

        File info = fileService.getInfo(namespace, name);
        if (info == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (request.checkNotModified(info.getTimestamp().getTime())) {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLastModified(info.getTimestamp().getTime());

        if (StringUtils.isNotEmpty(info.getUrl())) {
            headers.setLocation(new URI(info.getUrl()));
            return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
        } else {
            headers.setContentDispositionFormData("inline", info.getName());
            try {
                InputStream stream = fileService.getStream(namespace, name);
                return ResponseEntity
                        .ok()
                        .headers(headers)
                        .contentLength(info.getSize())
                        .contentType(MediaType.parseMediaType(info.getContentType()))
                        .body(new InputStreamResource(stream));
            } catch (FileNotFoundException e) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
    }

    @GetMapping("/api/v2/file/{namespace}/{name}/info")
    public ResponseEntity<File> getInfo(@PathVariable("namespace") String namespace,
                          @PathVariable("name") String name) {

        File info = fileService.getInfo(namespace, name);
        if (info == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(info, HttpStatus.OK);
        }

    }
}
