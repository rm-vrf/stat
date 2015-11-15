package cn.batchfile.stat.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

public class FastJsonConverter extends FastJsonHttpMessageConverter {
	
	public void setDateFormat(String dateFormat) {
		JSON.DEFFAULT_DATE_FORMAT = dateFormat;
	}
}