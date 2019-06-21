package cn.batchfile.stat.server.controller;

import cn.batchfile.stat.domain.Name;
import cn.batchfile.stat.domain.container.ContainerInstance;
import cn.batchfile.stat.server.service.ContainerService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ContainerController {
    private static final Logger LOG = LoggerFactory.getLogger(ContainerController.class);

    @Autowired
    private ContainerService containerService;

    @GetMapping("/api/v2/node/{node}/container")
    public List<ContainerInstance> getContainersByNode(@PathVariable("node") String node) {
        return containerService.getContainersByNode(node);
    }

    @GetMapping("/api/v2/service/{namespace}/{name}/container")
    public List<ContainerInstance> getContainersByService(@PathVariable("namespace") String namespace,
                                                          @PathVariable("name") String name) {
        Name n = new Name(namespace, name);
        return containerService.getContainersByService(n.toString());
    }

    @GetMapping("/api/v2/container/{id}")
    public ResponseEntity<ContainerInstance> getContainer(@PathVariable("id") String id) {
        ContainerInstance container = containerService.getContainer(id);
        if (container == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(container, HttpStatus.OK);
        }
    }

    @PutMapping("/api/v2/container")
    public ResponseEntity<String> putContainer(@RequestBody ContainerInstance container) {
        String id = containerService.putContainer(container);
        if (StringUtils.isEmpty(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(id, HttpStatus.OK);
        }
    }

    @PatchMapping("/api/v2/container/{id}")
    public ResponseEntity<String> patchContainer(@PathVariable("id") String id,
                                                 @RequestBody ContainerInstance container) {
        container.setId(id);
        String s = containerService.patchContainer(container);
        if (StringUtils.isEmpty(s)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(s, HttpStatus.OK);
        }
    }

    @GetMapping("/api/v2/container/{id}/config")
    public ResponseEntity<String> getConfig(@PathVariable("id") String id) {
        String config = containerService.getConfig(id);
        if (config == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (StringUtils.isEmpty(config)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(config, HttpStatus.OK);
        }
    }

    @PutMapping("/api/v2/container/{id}/config")
    public ResponseEntity<String> putConfig(@PathVariable("id") String id, @RequestBody String config) {
        String s = containerService.putConfig(id, config);
        if (StringUtils.isEmpty(s)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(s, HttpStatus.OK);
        }
    }

    @DeleteMapping("/api/v2/container/{id}")
    public ResponseEntity<String> deleteContainer(@PathVariable("id") String id) {
        String s = containerService.deleteContainer(id);
        if (StringUtils.isEmpty(s)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(s, HttpStatus.OK);
        }
    }
}
