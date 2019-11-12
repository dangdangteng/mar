#! /bin/sh
#stop workerjar
for tmpworker in ./worker[0-9][0-9][0-9][0-9][0-9].jar
do
	if [ -f $tmpworker ];then
		pid=`pgrep -f $tmpworker`
		if [ $? -eq 0 ];then
			echo "About to kill the process:$tmpworker, pid=$pid";
			pkill -f $tmpworker
		fi
	fi
done
sleep 2
