@echo off

if "%OS%" == "Windows_NT" setlocal

rem Move to the directory containing this script
cd %cd%

rem The directory containing the indexes
set OPENSEARCHSERVER_DATA=%cd%\data

rem The TCP port used by the server
SERVER_PORT=9090

rem Any JAVA option. Often used to allocate more memory. Uncomment this line to allocate 1GB.
JAVA_OPTS="-Xms1G -Xmx1G"

# Starting the server
java %JAVA_OPTS% -jar opensearchserver.jar -extractDirectory server -httpPort %SERVER_PORT%