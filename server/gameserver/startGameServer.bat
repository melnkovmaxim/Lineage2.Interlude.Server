@echo off
title GameServer Console
:start
REM ----------- Set Class Paths and Calls setenv.bat -----------------
SET OLDCLASSPATH=%CLASSPATH%
call classpath.bat
REM ------------------------------------------------------------------

"C:\Program Files\Java\jdk1.8.0_202\bin\java" -Dfile.encoding=UTF-8 -Xms1024m -Xmx1024m -Xincgc ru.agecold.gameserver.GameServer

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
