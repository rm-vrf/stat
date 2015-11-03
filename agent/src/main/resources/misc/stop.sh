#!/bin/sh

kill -15 `ps -ef | grep java | grep ispp-worker | awk '{print $2}'`
