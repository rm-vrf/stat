package cn.batchfile.stat.agent.controller;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import cn.batchfile.stat.agent.service.SystemService;
import cn.batchfile.stat.domain.Disk;
import cn.batchfile.stat.domain.Memory;
import cn.batchfile.stat.domain.Network;
import cn.batchfile.stat.domain.Os;
import cn.batchfile.stat.domain.Process_;

@RestController
public class SystemController {
	
	protected static final Logger log = LoggerFactory.getLogger(ServiceController.class);
	
	@Autowired
	private SystemService systemService;
	
	@GetMapping(value="/api/v2/system/ps")
	public ResponseEntity<List<Process_>> getPs(WebRequest request,
			@RequestParam(value = "grep", required = false, defaultValue = StringUtils.EMPTY) String grep) throws SigarException {
		List<Process_> ps = systemService.ps(grep);
		
		HttpHeaders headers = new HttpHeaders();
		//headers.setLastModified(lastModified);
		headers.setCacheControl("no-cache");
		return new ResponseEntity<List<Process_>>(ps, headers, HttpStatus.OK);
	}

	@GetMapping(value="/api/v2/system/ps/{pid}")
	public ResponseEntity<Process_> getP(WebRequest request,
			@PathVariable("pid") Long pid) throws SigarException {
		
		Process_ p = systemService.ps(pid);
		if (p == null) {
			return new ResponseEntity<Process_>(HttpStatus.NOT_FOUND);
		}
		
		HttpHeaders headers = new HttpHeaders();
		//headers.setLastModified(lastModified);
		headers.setCacheControl("no-cache");
		return new ResponseEntity<Process_>(p, headers, HttpStatus.OK);
	}
	
	@DeleteMapping(value="/api/v2/system/ps/{pid}")
	public ResponseEntity<Boolean> kill(WebRequest request,
			@PathVariable("pid") Long pid,
			@RequestParam(value = "signal", required = false, defaultValue = "15") Integer signal) throws SigarException {
		
		HttpHeaders headers = new HttpHeaders();
		try {
			systemService.kill(pid, signal);
			return new ResponseEntity<Boolean>(Boolean.TRUE, headers, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<Boolean>(Boolean.FALSE, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	@GetMapping(value="/api/v2/system/hostname")
	public ResponseEntity<String> getHostname(WebRequest request) {
		String hostname = systemService.getHostname();
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");
		return new ResponseEntity<String>(hostname, headers, HttpStatus.OK);
	}
	
	@GetMapping(value="/api/v2/system/address")
	public ResponseEntity<String> getAddress(WebRequest request) {
		String hostname = systemService.getAddress();
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");
		return new ResponseEntity<String>(hostname, headers, HttpStatus.OK);
	}
	
	@GetMapping(value="/api/v2/system/os")
	public ResponseEntity<Os> getOs(WebRequest request) {
		Os os = systemService.getOs();
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");
		return new ResponseEntity<Os>(os, headers, HttpStatus.OK);
	}

	@GetMapping(value="/api/v2/system/memory")
	public ResponseEntity<Memory> getMemory(WebRequest request) throws SigarException {
		Memory memory = systemService.getMemory();
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");
		return new ResponseEntity<Memory>(memory, headers, HttpStatus.OK);
	}

	@GetMapping(value="/api/v2/system/disk")
	public ResponseEntity<List<Disk>> getDisks(WebRequest request) throws SigarException {
		List<Disk> disks = systemService.getDisks();
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");
		return new ResponseEntity<List<Disk>>(disks, headers, HttpStatus.OK);
	}

	@GetMapping(value="/api/v2/system/network")
	public ResponseEntity<List<Network>> getNetworks(WebRequest request) throws SigarException {
		List<Network> networks = systemService.getNetworks();
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");
		return new ResponseEntity<List<Network>>(networks, headers, HttpStatus.OK);
	}

}
