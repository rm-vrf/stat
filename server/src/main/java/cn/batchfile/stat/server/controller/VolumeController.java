package cn.batchfile.stat.server.controller;

import cn.batchfile.stat.domain.service.Volume;
import cn.batchfile.stat.server.service.VolumeService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class VolumeController {
    private static final Logger log = LoggerFactory.getLogger(VolumeController.class);

    @Autowired
    private VolumeService volumeService;

    @GetMapping("/api/v2/volume")
    public List<Volume> getVolumes() {
        return volumeService.getVolumes();
    }

    @GetMapping("/api/v2/volume/{namespace:.+}")
    public List<Volume> getVolumes(@PathVariable("namespace") String namespace) {
        return volumeService.getVolumes(namespace);
    }

    @GetMapping("/api/v2/volume/{namespace}/{name:.+}")
    public ResponseEntity<Volume> getVolume(@PathVariable("namespace") String namespace,
                                            @PathVariable("name") String name) {

        Volume volume = volumeService.getVolume(namespace, name);
        if (volume == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(volume, HttpStatus.OK);
        }
    }

    @PutMapping("/api/v2/volume")
    public String putVolume(@RequestBody Volume volume) {
        return volumeService.putVolume(volume);
    }

    @DeleteMapping("/api/v2/volume/{namespace:.+}")
    public List<String> deleteVolumes(@PathVariable("namespace") String namespace) {
        return volumeService.deleteVolumes(namespace);
    }

    @DeleteMapping("/api/v2/volume/{namespace}/{name:.+}")
    public ResponseEntity<String> deleteVolume(@PathVariable("namespace") String namespace,
                                                @PathVariable("name") String name) {

        String s = volumeService.deleteVolume(namespace, name);
        if (StringUtils.isEmpty(s)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(s, HttpStatus.OK);
        }
    }
}
