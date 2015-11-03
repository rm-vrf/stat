@echo off

java -classpath ispp-worker.jar;.\local\cr.jar -Djava.ext.dirs=.\lib;.\lib\optional ^
-Djava.library.path=.\sigar ^
-Xbootclasspath/a:script ^
-Dfile.encoding=UTF-8 ^
com.iflytek.ispp.worker.Main ^
--port=5700 ^
--address=
