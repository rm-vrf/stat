###localStorage的写入/修改
	storage["a"]=1;  //写入a字段
    storage.b=1; //写入b字段        
    storage.setItem("c",3);  //写入c字段 

###localStorage的读取
	
   console.log(storage["a"]);  //打印a字段
   console.log(storage.b); //打印b字段       
   console.log(storage.getItem("c"));  //打印c字段        

###localStorage的删除

	storage.clear(); // 清除localStorage所有内容
 	storage.removeItem("a"); // 删除某个键值
###localStorage的键获取

	var storage=window.localStorage;
    storage.a=1;
    storage.setItem("c",3);
    for(var i=0;i<storage.length;i++){
        var key=storage.key(i);
        console.log(key);
    }

###注意事项
1. 官方推荐getItem\setItem这两种方法对其进行存取
2. <font color="red">localStorage会自动将localStorage对象转换成为字符串形式</font>，可以使用JSON.stringify()这个方法，来将JSON转换成为JSON字符串


----------
参考[https://www.cnblogs.com/st-leslie/p/5617130.html](https://www.cnblogs.com/st-leslie/p/5617130.html)