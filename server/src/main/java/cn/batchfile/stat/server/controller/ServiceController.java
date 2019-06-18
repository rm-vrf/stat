package cn.batchfile.stat.server.controller;

import cn.batchfile.stat.domain.service.Service;
import cn.batchfile.stat.server.service.ServiceService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ServiceController {
    private static final Logger log = LoggerFactory.getLogger(ServiceController.class);

    @Autowired
    private ServiceService serviceService;

    @GetMapping("/api/v2/service")
    public List<Service> getServices() {
        return serviceService.getServices();
    }

    @GetMapping("/api/v2/service/{namespace}")
    public List<Service> getServices(@PathVariable("namespace") String namespace) {
        return serviceService.getServices(namespace);
    }

    @GetMapping("/api/v2/service/{namespace}/{name}")
    public ResponseEntity<Service> getService(@PathVariable("namespace") String namespace,
                                             @PathVariable("name") String name) {

        Service service = serviceService.getService(namespace, name);
        if (service == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(service, HttpStatus.OK);
        }
    }

    @PutMapping("/api/v2/service")
    public String putService(@RequestBody Service service) {
        return serviceService.putService(service);
    }

    @DeleteMapping("/api/v2/service/{namespace}")
    public List<String> deleteServices(@PathVariable("namespace") String namespace) {
        return serviceService.deleteServices(namespace);
    }

    @DeleteMapping("/api/v2/service/{namespace}/{name}")
    public ResponseEntity<String> deleteService(@PathVariable("namespace") String namespace,
                              @PathVariable("name") String name) {

        String s = serviceService.deleteService(namespace, name);
        if (StringUtils.isEmpty(s)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(s, HttpStatus.OK);
        }
    }
}
