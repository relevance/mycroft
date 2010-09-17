#/bin/sh
# based on http://barelyenough.org/blog/2005/03/java-daemon/
# leaves out the clean shutdown hook for now

. bin/env.sh

launch_daemon()
{
  /bin/sh <<EOC
     java $JVM_ARGS $JAVA_ENV -cp $JARS:$SRC_DIR clojure.main -i $SRC_DIR/examples/daemon.clj -e "(use 'examples.daemon) (daemonize)" <&- &
     pid=\$!
     echo \${pid}
EOC
}

determine_if_daemon_is_running()
{
  old_daemon_pid=`cat log/daemon.pid|tr -d '\n'`
  ps -p "${old_daemon_pid}" >/dev/null 2>&1
  if [ "$?" -eq "0" ]
  then
    echo "NOT STARTING NEW INSTANCE. PID file daemon.pid already exists and the process id referred to in it is running."
    exit 1
  else
    # remove stale pid file
    rm log/daemon.pid
  fi
}

if [ -f log/daemon.pid ]
then
  determine_if_daemon_is_running
fi

daemon_pid=`launch_daemon`

sleep 3 # give it a chance to boot up 

ps -p "${daemon_pid}" >/dev/null 2>&1
if [ "$?" -eq "0" ] 
then
  echo ${daemon_pid} > log/daemon.pid
else
  echo "Daemon did not start."
  exit 1
fi
