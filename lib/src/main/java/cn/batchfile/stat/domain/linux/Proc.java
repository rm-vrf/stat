package cn.batchfile.stat.domain.linux;

import java.util.Date;
import java.util.Map;

/**
 * Linux 进程信息
 */
public class Proc {
    private Long pid;
    private String node;
    private String container;
    private Boolean up;
    private Date createTime;
    private Date updateTime;
    private Map<String, String> attr;
    private String autogroup;
    private String cgroup;
    private String cmdline;
    private String cpuset;
    private String environ;
    private String limits;
    private String loginuid;
    private String mountinfo;
    private String mounts;
    private String mountstats;
    private String sched;
    private String schedstat;
    private String sessionid;
    private String setgroups;
    private String smaps;
    private String stack;
    private String stat;
    private String statm;
    private String status;
    private String syscall;

    /**
     * 进程号
     * @return 进程号
     */
    public Long getPid() {
        return pid;
    }

    /**
     * 进程号
     * @param pid 进程号
     */
    public void setPid(Long pid) {
        this.pid = pid;
    }

    /**
     * 节点
     * @return 节点
     */
    public String getNode() {
        return node;
    }

    /**
     * 节点
     * @param node 节点
     */
    public void setNode(String node) {
        this.node = node;
    }

    /**
     * 容器
     * @return 容器
     */
    public String getContainer() {
        return container;
    }

    /**
     * 容器
     * @param container 容器
     */
    public void setContainer(String container) {
        this.container = container;
    }

    /**
     * 是否启动
     * @return 是否启动
     */
    public Boolean isUp() {
        return up;
    }

    /**
     * 是否启动
     * @param up 是否启动
     */
    public void setUp(Boolean up) {
        this.up = up;
    }

    /**
     * 创建时间
     * @return 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 更新时间
     * @return 更新时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 更新时间
     * @param updateTime 更新时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 创建时间
     * @param createTime 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Map<String, String> getAttr() {
        return attr;
    }

    public void setAttr(Map<String, String> attr) {
        this.attr = attr;
    }

    public String getAutogroup() {
        return autogroup;
    }

    public void setAutogroup(String autogroup) {
        this.autogroup = autogroup;
    }

    public String getCgroup() {
        return cgroup;
    }

    public void setCgroup(String cgroup) {
        this.cgroup = cgroup;
    }

    public String getCmdline() {
        return cmdline;
    }

    public void setCmdline(String cmdline) {
        this.cmdline = cmdline;
    }

    public String getCpuset() {
        return cpuset;
    }

    public void setCpuset(String cpuset) {
        this.cpuset = cpuset;
    }

    public String getEnviron() {
        return environ;
    }

    public void setEnviron(String environ) {
        this.environ = environ;
    }

    public String getLimits() {
        return limits;
    }

    public void setLimits(String limits) {
        this.limits = limits;
    }

    public String getLoginuid() {
        return loginuid;
    }

    public void setLoginuid(String loginuid) {
        this.loginuid = loginuid;
    }

    public String getMountinfo() {
        return mountinfo;
    }

    public void setMountinfo(String mountinfo) {
        this.mountinfo = mountinfo;
    }

    public String getMounts() {
        return mounts;
    }

    public void setMounts(String mounts) {
        this.mounts = mounts;
    }

    public String getMountstats() {
        return mountstats;
    }

    public void setMountstats(String mountstats) {
        this.mountstats = mountstats;
    }

    public String getSched() {
        return sched;
    }

    public void setSched(String sched) {
        this.sched = sched;
    }

    public String getSchedstat() {
        return schedstat;
    }

    public void setSchedstat(String schedstat) {
        this.schedstat = schedstat;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public String getSetgroups() {
        return setgroups;
    }

    public void setSetgroups(String setgroups) {
        this.setgroups = setgroups;
    }

    public String getSmaps() {
        return smaps;
    }

    public void setSmaps(String smaps) {
        this.smaps = smaps;
    }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public String getStatm() {
        return statm;
    }

    public void setStatm(String statm) {
        this.statm = statm;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSyscall() {
        return syscall;
    }

    public void setSyscall(String syscall) {
        this.syscall = syscall;
    }
}
