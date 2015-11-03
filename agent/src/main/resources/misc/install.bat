@echo off

java -classpath ispp-worker.jar;.\local\cr.jar -Djava.ext.dirs=.\lib;.\lib\optional ^
-Xbootclasspath/a:script ^
com.iflytek.ispp.worker.install.Installer ^
--port=5700 ^
--address=
