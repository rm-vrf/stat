//package cn.batchfile.stat.agent.service;
//
//import java.util.List;
//
//import org.hyperic.sigar.SigarException;
//
//import cn.batchfile.stat.agent.domain.Cpu;
//import cn.batchfile.stat.agent.domain.Disk;
//import cn.batchfile.stat.agent.domain.Memory;
//import cn.batchfile.stat.agent.domain.Network;
//import cn.batchfile.stat.agent.domain.Os;
//import cn.batchfile.stat.agent.domain.State;
//
//public interface StateService {
//
//	State getState();
//	
//	Os getOs();
//
//	Cpu getCpu() throws SigarException;
//	
//	List<Disk> getDisks() throws SigarException;
//
//	Memory getMemory() throws SigarException;
//
//	List<Network> getNetworks() throws SigarException;
//}
