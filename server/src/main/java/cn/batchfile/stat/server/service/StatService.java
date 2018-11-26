//package cn.batchfile.stat.server.service;
//
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//
//import org.apache.commons.lang.StringUtils;
//import org.elasticsearch.action.search.SearchRequestBuilder;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.index.query.QueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.search.aggregations.AggregationBuilder;
//import org.elasticsearch.search.aggregations.AggregationBuilders;
//import org.elasticsearch.search.aggregations.Aggregations;
//import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
//import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram.Bucket;
//import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;
//import org.joda.time.DateTime;
//import org.joda.time.DateTimeZone;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import cn.batchfile.stat.server.domain.Stat;
//
//@Service
//public class StatService {
//	protected static final Logger log = LoggerFactory.getLogger(StatService.class);
//	private static final ThreadLocal<DateFormat> TIME_FORMAT = new ThreadLocal<DateFormat>() {
//		@Override
//		protected DateFormat initialValue() {
//			return new SimpleDateFormat("MM-dd HH:mm");
//		}
//	};
//	
//	@Autowired
//	private ElasticService elasticService;
//
//	public Stat getDiskStat(String node, String time) {
//		Timespan tspan = getTimespan(time);
//		return getSumStat(node, tspan, "disk-stat-*", "data.total", "data.used", 
//				"data.diskReadBytesPerSecond", "data.diskReadsPerSecond", 
//				"data.diskWritesBytesPerSecond", "data.diskWritesPerSecond");
//	}
//	
//	public Stat getNetStat(String node, String time) {
//		Timespan tspan = getTimespan(time);
//		return getSumStat(node, tspan, "net-stat-*", "data.rxBytesPerSecond", "data.rxPacketsPerSecond", 
//				"data.txBytesPerSecond", "data.txPacketsPerSecond");
//	}
//	
//	public Stat getMemStat(String node, String time) {
//		Timespan tspan = getTimespan(time);
//		return getSumStat(node, tspan, "mem-stat-*", "data.actualUsed", "data.total");
//	}
//	
//	public Stat getCpuStat(String node, String time) {
//		Timespan tspan = getTimespan(time);
//		return getSumStat(node, tspan, "cpu-stat-*", "data.sys", "data.user", "data.idle", "data.wait");
//	}
//	
//	public Stat getOsStat(String node, String time) {
//		Timespan tspan = getTimespan(time);
//		return getSumStat(node, tspan, "os-stat-*", "data.cpus", "data.load");
//	}
//	
//	private Stat getSumStat(String node, Timespan tspan, String index, String... fields) {
//		String s = StringUtils.isEmpty(node) ? "*" : String.format("node:%s", node);
//		
//		QueryBuilder queryString = QueryBuilders.queryStringQuery(s).analyzeWildcard(true);
//		
//		QueryBuilder range = QueryBuilders.rangeQuery("time")
//				.from(tspan.getFrom())
//				.to(tspan.getTo())
//				.format("epoch_millis")
//				.includeLower(true)
//				.includeUpper(true);
//		
//		QueryBuilder query = QueryBuilders.boolQuery().must(queryString).must(range);
//		
//		AggregationBuilder aggregation = AggregationBuilders.dateHistogram("agg_").field("time").interval(60000).minDocCount(1).timeZone(DateTimeZone.getDefault());
//		for (int i = 0; i < fields.length; i ++) {
//			aggregation.subAggregation(AggregationBuilders.sum(String.valueOf(i)).field(fields[i]));
//		}
//		
//		SearchRequestBuilder search = elasticService.getNode().client().prepareSearch()
//				.setIndices(index).setTypes("stat")
//				.setFrom(0).setSize(0)
//				.setQuery(query).addAggregation(aggregation);
//		
//		SearchResponse resp = search.execute().actionGet();
//		log.debug(search.toString());
//		log.debug(resp.toString());
//		
//		Stat stat = new Stat();
//		for (int i = 0; i < fields.length; i ++) {
//			stat.getDatas().put(StringUtils.replace(fields[i], ".", "_"), new ArrayList<Object>());
//		}
//		
//		Aggregations aggs = resp.getAggregations();
//		if (aggs != null) {
//			InternalDateHistogram agg = (InternalDateHistogram)aggs.get("agg_");
//			List<Bucket> buckets = agg.getBuckets();
//			
//			for (Bucket bucket : buckets) {
//				DateTime key = (DateTime)bucket.getKey();
//				String time = TIME_FORMAT.get().format(new Date(key.getMillis()));
//				stat.getKeys().add(time);
//				
//				Aggregations agg2 = bucket.getAggregations();
//				for (int i = 0; i < fields.length; i ++) {
//					InternalSum sum = (InternalSum)agg2.get(String.valueOf(i));
//					stat.getDatas().get(StringUtils.replace(fields[i], ".", "_")).add(sum.getValue());
//					log.debug("time: {}, name: {}, value: {}", time, fields[i], sum.getValue());
//				}
//			}
//		}
//		
//		return stat;
//	}
//	
//	private Timespan getTimespan(String time) {
//		Calendar ca = Calendar.getInstance();
//		Timespan tspan = new Timespan();
//		long now = ca.getTimeInMillis();
//		ca.set(Calendar.HOUR_OF_DAY, 0);
//		ca.set(Calendar.SECOND, 0);
//		ca.set(Calendar.MINUTE, 0);
//		ca.set(Calendar.MILLISECOND, 0);
//		long today = ca.getTimeInMillis();
//		if (StringUtils.equalsIgnoreCase(time, "30min")) {
//			tspan.setFrom(now - 30L * 60000L);
//			tspan.setTo(now);
//		} else if (StringUtils.equalsIgnoreCase(time, "1h")) {
//			tspan.setFrom(now - 60L * 60000);
//			tspan.setTo(now);
//		} else if (StringUtils.equalsIgnoreCase(time, "4h")) {
//			tspan.setFrom(now - 240L * 60000L);
//			tspan.setTo(now);
//		} else if (StringUtils.equalsIgnoreCase(time, "8h")) {
//			tspan.setFrom(now - 480L * 60000L);
//			tspan.setTo(now);
//		} else if (StringUtils.equalsIgnoreCase(time, "24h")) {
//			tspan.setFrom(now - 1440L * 60000L);
//			tspan.setTo(now);
//		} else if (StringUtils.equalsIgnoreCase(time, "0day")) {
//			tspan.setFrom(today - 1440L * 60000L);
//			tspan.setTo(today);
//		} else if (StringUtils.equalsIgnoreCase(time, "-1day")) {
//			tspan.setFrom(today - 2880L * 60000L);
//			tspan.setTo(today - 1440L * 60000L);
//		} else if (StringUtils.equalsIgnoreCase(time, "-2day")) {
//			tspan.setFrom(today - 4320L * 60000L);
//			tspan.setTo(today - 2880L * 60000L);
//		}
//		return tspan;
//	}
//	
//	class Timespan {
//		private long from;
//		private long to;
//		
//		public long getFrom() {
//			return from;
//		}
//		
//		public void setFrom(long from) {
//			this.from = from;
//		}
//		
//		public long getTo() {
//			return to;
//		}
//		
//		public void setTo(long to) {
//			this.to = to;
//		}
//	}
//
//}
