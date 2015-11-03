#!/bin/sh

# env
JAVA_OPTS=
MAIN_CLASS=com.iflytek.ispp.worker.install.Installer

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
JAR_FILE=$PRGDIR/ispp-worker.jar

CP=$JAR_FILE

for i in `ls $LIB_DIR`
do
  CP=$CP:$LIB_DIR/$i
done

java $JAVA_OPTS -classpath $CP $JAVA_OPTS -Djava.ext.dirs=$LIB_DIR \
-Xbootclasspath/a:$SCRIPT_ROOT \
-Dfile.encoding=UTF-8 \
$MAIN_CLASS
