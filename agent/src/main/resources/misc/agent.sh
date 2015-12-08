#!/bin/sh

#unset DYLD_LIBRARY_PATH

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

JAVA_OPTS=
PORT=9091
FILE_ENCODING=UTF-8
TMP_DIR=/var/tmp
STAT_PIDFILE=$TMP_DIR/stat-agent.pid
LIB_DIR=$PRGDIR/lib
LIB_PATH=$PRGDIR/sigar
JAR_FILE=$PRGDIR/stat-agent.jar

# set customer vars
if [ -r "$PRGDIR/setenv.sh" ]; then
    . "$PRGDIR/setenv.sh"
fi

STAT_START="java $JAVA_OPTS -Djava.ext.dirs=$LIB_DIR -Djava.library.path=$LIB_PATH -Dfile.encoding=$FILE_ENCODING -jar $JAR_FILE --port=$PORT --address=$ADDRESS --dir=$PRGDIR --pid-file=$STAT_PIDFILE"

STAT_STATUS=""
STAT_PID=""
PID=""
ERROR=0

get_pid() {
    PID=""
    PIDFILE=$1
    # check for pidfile
    if [ -f "$PIDFILE" ] ; then
        PID=`cat $PIDFILE`
    fi
}

get_stat_pid() {
    get_pid $STAT_PIDFILE
    if [ ! "$PID" ]; then
        return
    fi
    if [ "$PID" -gt 0 ]; then
        STAT_PID=$PID
    fi
}

is_service_running() {
    PID=$1
    if [ "x$PID" != "x" ] && kill -0 $PID 2>/dev/null ; then
        RUNNING=1
    else
        RUNNING=0
    fi
    return $RUNNING
}

is_stat_running() {
    get_stat_pid
    is_service_running $STAT_PID
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
        STAT_STATUS="agent not running"
    else
        STAT_STATUS="agent already running"
    fi
    return $RUNNING
}

start_stat() {
    is_stat_running
    RUNNING=$?
    if [ $RUNNING -eq 1 ]; then
        echo "$0 $ARG: agent  (pid $STAT_PID) already running"
	exit
    fi
    $STAT_START &
    #echo $STAT_START
    COUNTER=40
    while [ $RUNNING -eq 0 ] && [ $COUNTER -ne 0 ]; do
        COUNTER=`expr $COUNTER - 1`
        sleep 3
        is_stat_running
        RUNNING=$?
    done
    if [ $RUNNING -eq 0 ]; then
        ERROR=1
    fi
    if [ $ERROR -eq 0 ]; then
	    echo "$0 $ARG: agent  started at port $PORT"
	    sleep 2
    else
	    echo "$0 $ARG: agent  could not be started"
	    ERROR=3
    fi
}

stop_stat() {
    NO_EXIT_ON_ERROR=$1
    is_stat_running
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
        echo "$0 $ARG: $STAT_STATUS"
        if [ "x$NO_EXIT_ON_ERROR" != "xno_exit" ]; then
            exit
        else
            return
        fi
    fi
	
	kill $STAT_PID

    COUNTER=40
    while [ $RUNNING -eq 1 ] && [ $COUNTER -ne 0 ]; do
        COUNTER=`expr $COUNTER - 1`
        sleep 3
        is_stat_running
        RUNNING=$?
    done

    is_stat_running
    RUNNING=$?
    if [ $RUNNING -eq 0 ]; then
            echo "$0 $ARG: agent stopped"
            rm -f $STAT_PIDFILE
        else
            echo "$0 $ARG: agent could not be stopped"
            ERROR=4
    fi
}

cleanpid() {
    rm -f $STAT_PIDFILE
}

if [ "x$1" = "xstart" ]; then
    start_stat
elif [ "x$1" = "xstop" ]; then
    stop_stat
elif [ "x$1" = "xstatus" ]; then
    is_stat_running
    echo "$STAT_STATUS"
elif [ "x$1" = "xcleanpid" ]; then
    cleanpid
fi

exit $ERROR