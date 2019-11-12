#! /bin/sh
#start workerjar
port=23451
WORKER_DIR=$(cd $(dirname $0); pwd)
data_time=`date +'%Y-%m-%d'`
WORKER_OUTFILE=$WORKER_DIR/../logs/workerout/worker$port-$data_time.out

source /etc/profile;
for tmpworker in ./worker[0-9][0-9][0-9][0-9][0-9].jar
do
	if [ -f $tmpworker ];then
		echo "tmpworker=$tmpworker, port=$port";
		netstat -anp|grep $port > /dev/null 2>&1
		if [ $? -eq 0 ];then
			echo "port:$port already occupied, try use $[$port+1]";
			for ((i=0;i<10;i++))
			do
				port=$[$port+1];
				netstat -anp|grep $port > /dev/null 2>&1
				if [ ! $? -eq 0 ];then
					nohup java -Xms1024m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=512m -XX:MaxNewSize=512m -jar $tmpworker --server.port=$port > $WORKER_OUTFILE 2>&1 &
					break;
				else
					echo "port:$port already occupied, try use $[$port+1]";
				fi
			done
		else
			echo "port:$port idle, try use it";
			nohup java -Xms1024m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=512m -XX:MaxNewSize=512m -jar $tmpworker --server.port=$port > $WORKER_OUTFILE 2>&1 &
		fi
		port=$[$port+1];
		sleep 5;
	fi
done
