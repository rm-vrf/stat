#!/bin/sh

# env
PORT=9090
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
JAR_FILE=$PRGDIR/stat-server.jar
FILE_ENCODING=UTF-8
WEB_ROOT=webapp

java $JAVA_OPTS \
-Djava.ext.dirs=$LIB_DIR \
-Dfile.encoding=$FILE_ENCODING \
-jar $JAR_FILE \
$1 \
--port=$PORT \
--address=$ADDRESS \
--webapp=$WEB_ROOT \
--dir=$PRGDIR
