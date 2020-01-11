package cn.batchfile.stat.server.controller;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.server.service.NamespaceService;

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
    public ResponseEntity<Page<String>> getNamespaces(
    		@PageableDefault(value = 10, sort = {"name"}, direction = Sort.Direction.ASC) Pageable pageable) {
        Page<String> list = namespaceService.getNamespaces(pageable);
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
