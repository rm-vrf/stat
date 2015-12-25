--
-- Database: `stat`
--

-- --------------------------------------------------------

--
-- 表的结构 `config`
--

CREATE TABLE IF NOT EXISTS `config` (
  `name` varchar(64) NOT NULL,
  `int_value` int(11) DEFAULT NULL,
  `string_value` varchar(128) DEFAULT NULL,
  `time_value` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `cpu_data`
--

CREATE TABLE IF NOT EXISTS `cpu_data` (
  `agent_id` varchar(32) NOT NULL,
  `time` datetime NOT NULL,
  `user` double DEFAULT NULL,
  `sys` double DEFAULT NULL,
  `nice` double DEFAULT NULL,
  `idle` double DEFAULT NULL,
  `wait` double DEFAULT NULL,
  `irq` double DEFAULT NULL,
  `soft_irq` double DEFAULT NULL,
  `stolen` double DEFAULT NULL,
  `combined` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `deployment`
--

CREATE TABLE IF NOT EXISTS `deployment` (
  `agent_id` varchar(32) NOT NULL,
  `name` varchar(128) NOT NULL,
  `description` text,
  `instance_count` int(11) DEFAULT NULL,
  `user` varchar(64) DEFAULT NULL,
  `work_directory` varchar(255) DEFAULT NULL,
  `environment` text,
  `start_command` varchar(255) DEFAULT NULL,
  `stop_command` varchar(255) DEFAULT NULL,
  `pid_file` varchar(255) DEFAULT NULL,
  `java_home` varchar(255) DEFAULT NULL,
  `jre_home` varchar(255) DEFAULT NULL,
  `main_class` varchar(255) DEFAULT NULL,
  `jar` varchar(255) DEFAULT NULL,
  `arguments` text,
  `vm_arguments` text,
  `classpath` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `disk`
--

CREATE TABLE IF NOT EXISTS `disk` (
  `agent_id` varchar(32) NOT NULL,
  `dir_name` varchar(128) NOT NULL,
  `dev_name` varchar(128) DEFAULT NULL,
  `type_name` varchar(128) DEFAULT NULL,
  `sys_type_name` varchar(128) DEFAULT NULL,
  `options` varchar(128) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `flags` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `disk_data`
--

CREATE TABLE IF NOT EXISTS `disk_data` (
  `agent_id` varchar(32) NOT NULL,
  `dir_name` varchar(128) NOT NULL,
  `time` datetime NOT NULL,
  `total` bigint(20) DEFAULT NULL,
  `free` bigint(20) DEFAULT NULL,
  `used` bigint(20) DEFAULT NULL,
  `avail` bigint(20) DEFAULT NULL,
  `files` bigint(20) DEFAULT NULL,
  `free_files` bigint(20) DEFAULT NULL,
  `disk_reads` bigint(20) DEFAULT NULL,
  `disk_reads_per_second` bigint(20) DEFAULT NULL,
  `disk_writes` bigint(20) DEFAULT NULL,
  `disk_writes_per_second` bigint(20) DEFAULT NULL,
  `disk_read_bytes` bigint(20) DEFAULT NULL,
  `disk_read_bytes_per_second` bigint(20) DEFAULT NULL,
  `disk_write_bytes` bigint(20) DEFAULT NULL,
  `disk_write_bytes_per_second` bigint(20) DEFAULT NULL,
  `disk_queue` double DEFAULT NULL,
  `disk_service_time` double DEFAULT NULL,
  `use_percent` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `gc`
--

CREATE TABLE IF NOT EXISTS `gc` (
  `command_id` varchar(32) NOT NULL,
  `agent_id` varchar(32) DEFAULT NULL,
  `pid` bigint(20) DEFAULT NULL,
  `begin_time` datetime DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `name` varchar(128) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `gc_data`
--

CREATE TABLE IF NOT EXISTS `gc_data` (
  `command_id` varchar(32) NOT NULL,
  `time` datetime NOT NULL,
  `agent_id` varchar(32) DEFAULT NULL,
  `pid` bigint(20) DEFAULT NULL,
  `s0c` double DEFAULT NULL,
  `s1c` double DEFAULT NULL,
  `s0u` double DEFAULT NULL,
  `s1u` double DEFAULT NULL,
  `ec` double DEFAULT NULL,
  `eu` double DEFAULT NULL,
  `oc` double DEFAULT NULL,
  `ou` double DEFAULT NULL,
  `mc` double DEFAULT NULL,
  `mu` double DEFAULT NULL,
  `ccsc` double DEFAULT NULL,
  `ccsu` double DEFAULT NULL,
  `ygc` int(11) DEFAULT NULL,
  `ygct` double DEFAULT NULL,
  `fgc` int(11) DEFAULT NULL,
  `fgct` double DEFAULT NULL,
  `gct` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `memory_data`
--

CREATE TABLE IF NOT EXISTS `memory_data` (
  `agent_id` varchar(32) NOT NULL,
  `time` datetime NOT NULL,
  `total` bigint(20) DEFAULT NULL,
  `ram` bigint(20) DEFAULT NULL,
  `used` bigint(20) DEFAULT NULL,
  `free` bigint(20) DEFAULT NULL,
  `actual_used` bigint(20) DEFAULT NULL,
  `actual_free` bigint(20) DEFAULT NULL,
  `used_percent` double DEFAULT NULL,
  `free_percent` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `metric`
--

CREATE TABLE IF NOT EXISTS `metric` (
  `agent_id` varchar(32) NOT NULL,
  `name` varchar(128) NOT NULL,
  `description` text,
  `script_type` varchar(64) DEFAULT NULL,
  `content_type` varchar(64) DEFAULT NULL,
  `format` varchar(128) DEFAULT NULL,
  `script` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `metric_data`
--

CREATE TABLE IF NOT EXISTS `metric_data` (
  `agent_id` varchar(32) NOT NULL,
  `name` varchar(128) NOT NULL,
  `metric` varchar(64) NOT NULL,
  `time` datetime NOT NULL,
  `value` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `network`
--

CREATE TABLE IF NOT EXISTS `network` (
  `agent_id` varchar(32) NOT NULL,
  `address` varchar(128) NOT NULL,
  `name` varchar(128) DEFAULT NULL,
  `hwaddr` varchar(128) DEFAULT NULL,
  `type` varchar(128) DEFAULT NULL,
  `description` varchar(128) DEFAULT NULL,
  `destination` varchar(128) DEFAULT NULL,
  `broadcast` varchar(128) DEFAULT NULL,
  `netmask` varchar(128) DEFAULT NULL,
  `flags` bigint(20) DEFAULT NULL,
  `mtu` bigint(20) DEFAULT NULL,
  `metric` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `network_data`
--

CREATE TABLE IF NOT EXISTS `network_data` (
  `agent_id` varchar(32) NOT NULL,
  `address` varchar(128) NOT NULL,
  `time` datetime NOT NULL,
  `rx_bytes` bigint(20) DEFAULT NULL,
  `rx_bytes_per_second` bigint(20) DEFAULT NULL,
  `rx_packets` bigint(20) DEFAULT NULL,
  `rx_packets_per_second` bigint(20) DEFAULT NULL,
  `rx_errors` bigint(20) DEFAULT NULL,
  `rx_errors_per_second` bigint(20) DEFAULT NULL,
  `rx_dropped` bigint(20) DEFAULT NULL,
  `rx_dropped_per_second` bigint(20) DEFAULT NULL,
  `rx_overruns` bigint(20) DEFAULT NULL,
  `rx_overruns_per_second` bigint(20) DEFAULT NULL,
  `rx_frame` bigint(20) DEFAULT NULL,
  `rx_frame_per_second` bigint(20) DEFAULT NULL,
  `tx_bytes` bigint(20) DEFAULT NULL,
  `tx_bytes_per_second` bigint(20) DEFAULT NULL,
  `tx_packets` bigint(20) DEFAULT NULL,
  `tx_packets_per_second` bigint(20) DEFAULT NULL,
  `tx_errors` bigint(20) DEFAULT NULL,
  `tx_errors_per_second` bigint(20) DEFAULT NULL,
  `tx_dropped` bigint(20) DEFAULT NULL,
  `tx_dropped_per_second` bigint(20) DEFAULT NULL,
  `tx_overruns` bigint(20) DEFAULT NULL,
  `tx_overruns_per_second` bigint(20) DEFAULT NULL,
  `tx_collisions` bigint(20) DEFAULT NULL,
  `tx_carrier` bigint(20) DEFAULT NULL,
  `tx_carrier_per_second` bigint(20) DEFAULT NULL,
  `speed` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `node`
--

CREATE TABLE IF NOT EXISTS `node` (
  `agent_id` varchar(32) NOT NULL,
  `schema` varchar(16) DEFAULT NULL,
  `address` varchar(64) DEFAULT NULL,
  `port` int(11) DEFAULT NULL,
  `hostname` varchar(128) DEFAULT NULL,
  `description` text,
  `location` varchar(64) DEFAULT NULL,
  `os_name` varchar(128) DEFAULT NULL,
  `os_version` varchar(128) DEFAULT NULL,
  `cpu` int(11) DEFAULT NULL,
  `architecture` varchar(128) DEFAULT NULL,
  `id_rsa` text,
  `id_rsa_pub` text,
  `create_time` datetime DEFAULT NULL,
  `enabled` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `node_data`
--

CREATE TABLE IF NOT EXISTS `node_data` (
  `agent_id` varchar(32) NOT NULL,
  `time` datetime NOT NULL,
  `load` double DEFAULT NULL,
  `available` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `node_tag`
--

CREATE TABLE IF NOT EXISTS `node_tag` (
  `agent_id` varchar(32) NOT NULL,
  `tag` varchar(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `process_data`
--

CREATE TABLE IF NOT EXISTS `process_data` (
  `instance_id` varchar(32) NOT NULL,
  `time` datetime NOT NULL,
  `agent_id` varchar(32) DEFAULT NULL,
  `name` varchar(128) DEFAULT NULL,
  `pid` bigint(20) DEFAULT NULL,
  `cpu_percent` double DEFAULT NULL,
  `threads` bigint(20) DEFAULT NULL,
  `vsz` bigint(20) DEFAULT NULL,
  `rss` bigint(20) DEFAULT NULL,
  `cpu_sys` bigint(20) DEFAULT NULL,
  `cpu_user` bigint(20) DEFAULT NULL,
  `cpu_total` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `process_instance`
--

CREATE TABLE IF NOT EXISTS `process_instance` (
  `instance_id` varchar(32) NOT NULL,
  `agent_id` varchar(32) DEFAULT NULL,
  `pid` bigint(20) DEFAULT NULL,
  `ppid` bigint(20) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `name` varchar(128) DEFAULT NULL,
  `group` varchar(64) DEFAULT NULL,
  `user` varchar(64) DEFAULT NULL,
  `work_directory` varchar(128) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `command` text,
  `args` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `process_monitor`
--

CREATE TABLE IF NOT EXISTS `process_monitor` (
  `name` varchar(128) NOT NULL,
  `description` text,
  `query` varchar(255) DEFAULT NULL,
  `instance_count` int(11) DEFAULT NULL,
  `enabled` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `stack`
--

CREATE TABLE IF NOT EXISTS `stack` (
  `command_id` varchar(32) NOT NULL,
  `agent_id` varchar(32) DEFAULT NULL,
  `pid` bigint(20) DEFAULT NULL,
  `begin_time` datetime DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `name` varchar(128) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 表的结构 `stack_data`
--

CREATE TABLE IF NOT EXISTS `stack_data` (
  `command_id` varchar(32) NOT NULL,
  `time` datetime NOT NULL,
  `agent_id` varchar(32) DEFAULT NULL,
  `pid` bigint(128) DEFAULT NULL,
  `count` int(11) DEFAULT NULL,
  `stacks` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `config`
--
ALTER TABLE `config`
  ADD PRIMARY KEY (`name`);

--
-- Indexes for table `cpu_data`
--
ALTER TABLE `cpu_data`
  ADD PRIMARY KEY (`agent_id`,`time`);

--
-- Indexes for table `deployment`
--
ALTER TABLE `deployment`
  ADD PRIMARY KEY (`agent_id`,`name`);

--
-- Indexes for table `disk`
--
ALTER TABLE `disk`
  ADD PRIMARY KEY (`agent_id`,`dir_name`);

--
-- Indexes for table `disk_data`
--
ALTER TABLE `disk_data`
  ADD PRIMARY KEY (`agent_id`,`dir_name`,`time`);

--
-- Indexes for table `gc`
--
ALTER TABLE `gc`
  ADD PRIMARY KEY (`command_id`),
  ADD KEY `status` (`status`);

--
-- Indexes for table `gc_data`
--
ALTER TABLE `gc_data`
  ADD PRIMARY KEY (`command_id`,`time`),
  ADD KEY `pid_time` (`pid`,`time`);

--
-- Indexes for table `memory_data`
--
ALTER TABLE `memory_data`
  ADD PRIMARY KEY (`agent_id`,`time`);

--
-- Indexes for table `metric`
--
ALTER TABLE `metric`
  ADD PRIMARY KEY (`agent_id`,`name`);

--
-- Indexes for table `metric_data`
--
ALTER TABLE `metric_data`
  ADD PRIMARY KEY (`agent_id`,`name`,`metric`,`time`);

--
-- Indexes for table `network`
--
ALTER TABLE `network`
  ADD PRIMARY KEY (`agent_id`,`address`);

--
-- Indexes for table `network_data`
--
ALTER TABLE `network_data`
  ADD PRIMARY KEY (`agent_id`,`address`,`time`);

--
-- Indexes for table `node`
--
ALTER TABLE `node`
  ADD PRIMARY KEY (`agent_id`),
  ADD KEY `enabled` (`enabled`);

--
-- Indexes for table `node_data`
--
ALTER TABLE `node_data`
  ADD PRIMARY KEY (`agent_id`,`time`);

--
-- Indexes for table `node_tag`
--
ALTER TABLE `node_tag`
  ADD PRIMARY KEY (`agent_id`,`tag`);

--
-- Indexes for table `process_data`
--
ALTER TABLE `process_data`
  ADD PRIMARY KEY (`instance_id`,`time`);

--
-- Indexes for table `process_instance`
--
ALTER TABLE `process_instance`
  ADD PRIMARY KEY (`instance_id`),
  ADD KEY `agent_id_status` (`agent_id`,`group`);

--
-- Indexes for table `process_monitor`
--
ALTER TABLE `process_monitor`
  ADD PRIMARY KEY (`name`),
  ADD KEY `enabled` (`enabled`);

--
-- Indexes for table `stack`
--
ALTER TABLE `stack`
  ADD PRIMARY KEY (`command_id`),
  ADD KEY `status` (`status`);

--
-- Indexes for table `stack_data`
--
ALTER TABLE `stack_data`
  ADD PRIMARY KEY (`command_id`,`time`),
  ADD UNIQUE KEY `pid_time` (`pid`,`time`);
