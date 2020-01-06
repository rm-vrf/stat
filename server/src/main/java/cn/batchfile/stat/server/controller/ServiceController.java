package cn.batchfile.stat.server.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import cn.batchfile.stat.server.domain.service.Service;
import cn.batchfile.stat.server.service.ServiceService;

@RestController
public class ServiceController {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceController.class);

    @Autowired
    private ServiceService serviceService;

    @GetMapping("/api/service/{namespace:.+}")
    public ResponseEntity<List<Service>> getServices(@PathVariable("namespace") String namespace) {

        List<Service> list = serviceService.getServices(namespace);
        if (list == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(list, HttpStatus.OK);
        }
    }

    @GetMapping("/api/service/{namespace}/{serviceName:.+}")
    public ResponseEntity<Service> getService(@PathVariable("namespace") String namespace,
                                              @PathVariable("serviceName") String serviceName) {

        Service service = serviceService.getService(namespace, serviceName);
        if (service == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(service, HttpStatus.OK);
        }
    }

    @PostMapping("/api/service/{namespace:.+}")
    public ResponseEntity<Service> postService(@PathVariable("namespace") String namespace,
                                               @RequestBody Service service) {

        LOG.info("post service, namespace: {}, name: {}", namespace, service.getName());
        Service svc = serviceService.createService(namespace, service);
        return new ResponseEntity<>(svc, HttpStatus.OK);
    }

    @PostMapping("/api/service/{namespace:.+}/_compose")
    public ResponseEntity<List<String>> postServices(@PathVariable("namespace") String namespace,
                                                     @RequestBody Service[] services) {

        LOG.info("post compose, namespace: {}", namespace);
        List<String> list = new ArrayList<>();
        for (Service service : services) {
            Service svc = serviceService.createService(namespace, service);
            list.add(svc.getName());
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PutMapping("/api/service/{namespace}/{serviceName:.+}")
    public ResponseEntity<Service> putService(@PathVariable("namespace") String namespace,
                                              @PathVariable("serviceName") String serviceName,
                                              @RequestBody Service service) {

        LOG.info("put service, namespace: {}, name: {}", namespace, serviceName);
        service.setName(serviceName);
        Service svc = serviceService.updateService(namespace, service);
        return new ResponseEntity<>(svc, HttpStatus.OK);
    }

    @DeleteMapping("/api/service/{namespace:.+}")
    public ResponseEntity<List<String>> deleteServices(@PathVariable("namespace") String namespace) {

        LOG.info("delete service, namespace: {}", namespace);
        List<String> list = serviceService.deleteServices(namespace);
        if (list == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(list, HttpStatus.OK);
        }
    }

    @DeleteMapping("/api/service/{namespace}/{serviceName:.+}")
    public ResponseEntity<Service> deleteService(@PathVariable("namespace") String namespace,
                                                 @PathVariable("serviceName") String serviceName) {

        LOG.info("delete service, namespace: {}, name: {}", namespace, serviceName);
        Service svc = serviceService.deleteService(namespace, serviceName);
        if (svc == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(svc, HttpStatus.OK);
        }
    }

}
