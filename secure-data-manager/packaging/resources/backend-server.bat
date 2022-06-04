@echo off
rem Backend server startup and shutdown

setlocal

rem Set local environment variables
set "SDM_HOME=%cd%"
set "JAVA_HOME=%SDM_HOME%\${openjdk-jre.version}"
set "JRE_HOME=%SDM_HOME%\${openjdk-jre.version}"
set "keystore_location=%SDM_HOME%\sdm\config\keystore"

rem Create SDM backend service
set "SDM_SERVER_SERVICE=sdm-backend"


rem Get action "startup" or "shutdown"
if ""%1""=="""" goto errorAction
if ""%1""==""startup"" goto startupServers
if ""%1""==""shutdown"" goto shutdownServers

:errorAction
echo Missing action argument startup or shutdown
goto :end

:startupServers
rem Run Spring boot backend server
start "%SDM_SERVER_SERVICE%" %JAVA_HOME%\bin\java -Duser.home=%SDM_HOME% -jar %SDM_HOME%\sdm\sdm-backend.jar
goto end

:shutdownServers
rem Shutdown Spring boot backend server
taskkill /FI "WindowTitle eq %SDM_SERVER_SERVICE%*" /T /F
goto end

:end
