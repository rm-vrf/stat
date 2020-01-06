package cn.batchfile.stat.server.controller;

import cn.batchfile.stat.server.service.NamespaceService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NamespaceController {
    private static final Logger LOG = LoggerFactory.getLogger(NamespaceController.class);

    @Autowired
    private NamespaceService namespaceService;

    @PostMapping("/api/namespace/{namespace:.+}")
    public ResponseEntity<String> createNamespace(@PathVariable("namespace") String namespace) {
        LOG.info("create namespace: {}", namespace);

        String name = namespaceService.createNamespace(namespace);
        if (StringUtils.isNotEmpty(name)) {
            return new ResponseEntity<>(name, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/api/namespace")
    public ResponseEntity<List<String>> getNamespaces() {
        List<String> list = namespaceService.getNamespaces();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @DeleteMapping("/api/namespace/{namespace:.+}")
    public ResponseEntity<String> deleteNamespace(@PathVariable("namespace") String namespace) {
        LOG.info("delete namespace: {}", namespace);

        String name = namespaceService.deleteNamespace(namespace);
        if (StringUtils.isNotEmpty(name)) {
            return new ResponseEntity<>(name, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

}
