#!/bin/sh

#####################
# set env
#####################

export JAVA_HOME=/usr/local/jdk1.7.0_51
export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar

#####################
# resolve links - $0 may be a softlink
#####################

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

#####################
# start
#####################

PNAME=ispp-worker

ifrun=$(ps -ef | grep $PNAME | grep java)
if [ "$ifrun" != "" ];then
    echo "$PNAME is running..."
else
    echo "$PNAME was stopped."
    echo "$PNAME is starting..."
    nohup sh "$PRGDIR"/start.sh >/dev/null 2>&1 &
    echo "$PNAME was started."
fi
