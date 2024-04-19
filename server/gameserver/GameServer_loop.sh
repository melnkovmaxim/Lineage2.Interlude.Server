#!/bin/sh
# exit codes of GameServer:
#  0 normal shutdown
#  2 reboot attempt

while :; do
	[ -f log/java0.log.0 ] && mv log/java0.log.0 "log/z_`date +%Y-%m-%d_%H-%M-%S`_java.log"
	[ -f log/stdout.log ] && mv log/stdout.log "log/z_`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
	java -server -Dfile.encoding=UTF-8 -Xincgc -Xms7G -Xmx14G -cp ./../libs/smrt-core-1.0.jar:./../libs/json-simple-1.1.1.jar:./../libs/lameguard-1.9.5.jar:./../libs/bsf.jar:./../libs/bsh-2.0b4.jar:./../libs/bonecp-0.8.0.jar:./../libs/slf4j-api-1.7.12.jar:./../libs/slf4j-nop-1.7.12.jar:./../libs/guava-17.0.jar:./../libs/c30-0.91.2.jar:./../libs/jython.jar:./../libs/ecj.jar:./../libs/java-engine.jar:./../libs/commons-logging-1.1.jar:./../libs/commons-pool-1.5.4.jar:./../libs/netty-3.6.3.Final.jar:./../libs/javolution-5.5.1.jar:./../libs/mysql-connector-java-5.1.30-bin.jar:./../libs/core-gs.jar ru.agecold.gameserver.GameServer > log/stdout.log 2>&1
	[ $? -ne 2 ] && break
#	/etc/init.d/mysql restart
	sleep 10
done
