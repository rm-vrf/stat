#!/bin/sh

# env
PORT=8700
JMX_PORT=8701
ADDRESS=
JAVA_OPTS=

# resolve links - $0 may be a softlink
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`

# set customer vars
if [ -r "$PRGDIR/setenv.sh" ]; then
  . "$PRGDIR/setenv.sh"
fi

LIB_DIR=$PRGDIR/lib
LOCAL_DIR=$PRGDIR/local
JAR_FILE=$PRGDIR/ispp-worker.jar
LIB_PATH=$PRGDIR/sigar
SCRIPT_ROOT=$PRGDIR/script
MAIN_CLASS=com.iflytek.ispp.worker.Main
FILE_ENCODING=UTF-8

CP=$JAR_FILE

for i in `ls $LIB_DIR`
do
  CP=$CP:$LIB_DIR/$i
done

if [ -e $LOCAL_DIR ]
then
  for i in `ls $LOCAL_DIR`
  do
    CP=$CP:$LOCAL_DIR/$i
  done
fi

java $JAVA_OPTS -classpath $CP \
-Djava.library.path=$LIB_PATH \
-Xbootclasspath/a:$SCRIPT_ROOT \
-Dfile.encoding=$FILE_ENCODING \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=$JMX_PORT \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false \
$MAIN_CLASS \
--port=$PORT \
--address=$ADDRESS
