@echo off
title Login
:start
REM -------------------------------------
REM Default parameters for a basic server.
REM java -Dfile.encoding=UTF-8 -Xmx256m net.sf.l2j.loginserver.L2LoginServer
"C:\Users\melni\.jdks\sapmachine-11.0.22\bin\java" -Dfile.encoding=UTF-8 -Xmx256m -cp ./it_mantaray_login.jar;libs/* net.sf.l2j.loginserver.L2LoginServer
REM -------------------------------------

SET CLASSPATH=%OLDCLASSPATH%

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Admin Restart ...
echo.
goto start
:error
echo.
echo Server terminated abnormaly
echo.
:end
echo.
echo server terminated
echo.
pause
