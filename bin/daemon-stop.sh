#/bin/sh

if [ -f log/daemon.pid ] ; then
    kill `cat log/daemon.pid`
fi
