@echo off

if "%OS%" == "Windows_NT" setlocal

rem Move to the directory containing this script
cd %cd%

set LANG=en_US.UTF-8
set JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=UTF-8 -Djava.protocol.handler.pkgs=jcifs

rem The directory containing the indexes
set OPENSEARCHSERVER_DATA=%cd%\data

rem The TCP port used by the server
set SERVER_PORT=9090

rem Any JAVA option. Often used to allocate more memory. Uncomment this line to allocate 1GB.
rem set JAVA_OPTS=%JAVA_OPTS% -Xms1G -Xmx1G

rem Starting the server
java %JAVA_OPTS% -jar opensearchserver.jar -extractDirectory server -httpPort %SERVER_PORT% -uriEncoding UTF-8