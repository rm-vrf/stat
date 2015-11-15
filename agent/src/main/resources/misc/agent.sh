#!/bin/sh

# env
PORT=9091
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
JAR_FILE=$PRGDIR/stat-agent.jar
LIB_PATH=$PRGDIR/sigar
FILE_ENCODING=UTF-8

java $JAVA_OPTS \
-Djava.ext.dirs=$LIB_DIR \
-Djava.library.path=$LIB_PATH \
-Dfile.encoding=$FILE_ENCODING \
-jar $JAR_FILE \
$1 \
--port=$PORT \
--address=$ADDRESS
