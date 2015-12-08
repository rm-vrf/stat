CREATE TABLE IF NOT EXISTS `config` (
  `name` varchar(64) NOT NULL,
  `int_value` int(11) DEFAULT NULL,
  `string_value` varchar(128) DEFAULT NULL,
  `time_value` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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

CREATE TABLE IF NOT EXISTS `deployment` (
  `agent_id` varchar(32) NOT NULL,
  `name` varchar(128) NOT NULL,
  `description` text,
  `instance_count` int(11) DEFAULT NULL,
  `user` varchar(64) DEFAULT NULL,
  `working_directory` varchar(255) DEFAULT NULL,
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

CREATE TABLE IF NOT EXISTS `gc` (
  `command_id` varchar(32) NOT NULL,
  `agent_id` varchar(32) DEFAULT NULL,
  `pid` bigint(20) DEFAULT NULL,
  `begin_time` datetime DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `name` varchar(128) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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

CREATE TABLE IF NOT EXISTS `metric` (
  `agent_id` varchar(32) NOT NULL,
  `name` varchar(128) NOT NULL,
  `description` text,
  `script_type` varchar(64) DEFAULT NULL,
  `content_type` varchar(64) DEFAULT NULL,
  `script` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `metric_data` (
  `agent_id` varchar(32) NOT NULL,
  `name` varchar(128) NOT NULL,
  `time` datetime NOT NULL,
  `value` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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

CREATE TABLE IF NOT EXISTS `node_data` (
  `agent_id` varchar(32) NOT NULL,
  `time` datetime NOT NULL,
  `load` double DEFAULT NULL,
  `available` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `node_tag` (
  `agent_id` varchar(32) NOT NULL,
  `tag` varchar(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `process_data` (
  `instance_id` varchar(32) NOT NULL,
  `time` datetime NOT NULL,
  `agent_id` varchar(32) DEFAULT NULL,
  `name` varchar(128) DEFAULT NULL,
  `pid` bigint(20) DEFAULT NULL,
  `cpu_percent` double DEFAULT NULL,
  `memory_percent` double DEFAULT NULL,
  `vsz` bigint(20) DEFAULT NULL,
  `rss` bigint(20) DEFAULT NULL,
  `tt` varchar(64) DEFAULT NULL,
  `stat` varchar(64) DEFAULT NULL,
  `cpu_time` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `process_instance` (
  `instance_id` varchar(32) NOT NULL,
  `agent_id` varchar(32) DEFAULT NULL,
  `pid` bigint(20) DEFAULT NULL,
  `deployment_name` varchar(128) DEFAULT NULL,
  `monitor_name` varchar(128) DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `user` varchar(64) DEFAULT NULL,
  `type` varchar(16) DEFAULT NULL,
  `started` varchar(32) DEFAULT NULL,
  `command` text,
  `main_class` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `process_monitor` (
  `name` varchar(128) NOT NULL,
  `description` text,
  `query` varchar(255) DEFAULT NULL,
  `instance_count` int(11) DEFAULT NULL,
  `enabled` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `stack` (
  `command_id` varchar(32) NOT NULL,
  `agent_id` varchar(32) DEFAULT NULL,
  `pid` bigint(20) DEFAULT NULL,
  `begin_time` datetime DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `name` varchar(128) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `stack_data` (
  `command_id` varchar(32) NOT NULL,
  `time` datetime NOT NULL,
  `agent_id` varchar(32) DEFAULT NULL,
  `pid` bigint(128) DEFAULT NULL,
  `count` int(11) DEFAULT NULL,
  `stacks` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `config`
  ADD PRIMARY KEY (`name`);

ALTER TABLE `cpu_data`
  ADD PRIMARY KEY (`agent_id`,`time`);

ALTER TABLE `deployment`
  ADD PRIMARY KEY (`agent_id`,`name`);

ALTER TABLE `disk`
  ADD PRIMARY KEY (`agent_id`,`dir_name`);

ALTER TABLE `disk_data`
  ADD PRIMARY KEY (`agent_id`,`dir_name`,`time`);

ALTER TABLE `gc`
  ADD PRIMARY KEY (`command_id`),
  ADD KEY `status` (`status`);

ALTER TABLE `gc_data`
  ADD PRIMARY KEY (`command_id`,`time`),
  ADD KEY `pid_time` (`pid`,`time`);

ALTER TABLE `memory_data`
  ADD PRIMARY KEY (`agent_id`,`time`);

ALTER TABLE `metric`
  ADD PRIMARY KEY (`agent_id`,`name`);

ALTER TABLE `metric_data`
  ADD PRIMARY KEY (`agent_id`,`name`,`time`);

ALTER TABLE `network`
  ADD PRIMARY KEY (`agent_id`,`address`);

ALTER TABLE `network_data`
  ADD PRIMARY KEY (`agent_id`,`address`,`time`);

ALTER TABLE `node`
  ADD PRIMARY KEY (`agent_id`),
  ADD KEY `enabled` (`enabled`);

ALTER TABLE `node_data`
  ADD PRIMARY KEY (`agent_id`,`time`);

ALTER TABLE `node_tag`
  ADD PRIMARY KEY (`agent_id`,`tag`);

ALTER TABLE `process_data`
  ADD PRIMARY KEY (`instance_id`,`time`);

ALTER TABLE `process_instance`
  ADD PRIMARY KEY (`instance_id`),
  ADD KEY `agent_id_status` (`agent_id`,`status`);

ALTER TABLE `process_monitor`
  ADD PRIMARY KEY (`name`),
  ADD KEY `enabled` (`enabled`);

ALTER TABLE `stack`
  ADD PRIMARY KEY (`command_id`),
  ADD KEY `status` (`status`);

ALTER TABLE `stack_data`
  ADD PRIMARY KEY (`command_id`,`time`),
  ADD UNIQUE KEY `pid_time` (`pid`,`time`);
