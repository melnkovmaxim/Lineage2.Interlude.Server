#!/bin/bash

err=1
until [ $err == 0 ]; 
do
	java -Dfile.encoding=UTF-8 -Xms256m -Xmx256m -cp ./it_mantaray_login.jar:libs/* net.sf.l2j.loginserver.L2LoginServer > log/stdout.log 2>&1
	err=$?
#	/etc/init.d/mysql restart
	sleep 10;
done
