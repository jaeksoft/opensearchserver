@echo off

rem  License Agreement for Jaeksoft Open Search Server
rem	 
rem	 Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
rem	 
rem	 http://www.open-search-server.com
rem	 
rem	 This file is part of Jaeksoft Open Search Server.
rem	 
rem	 Jaeksoft Open Search Server is free software: you can redistribute it and/or
rem	 modify it under the terms of the GNU General Public License as published by
rem	 the Free Software Foundation, either version 3 of the License, or
rem	 (at your option) any later version.
rem	 
rem	 Jaeksoft Open Search Server is distributed in the hope that it will be useful,
rem	 but WITHOUT ANY WARRANTY; without even the implied warranty of
rem	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
rem	 GNU General Public License for more details.
rem	 
rem	 You should have received a copy of the GNU General Public License
rem	 along with Jaeksoft OpenSearchServer. 
rem	 If not, see <http://www.gnu.org/licenses/>.

if "%OS%" == "Windows_NT" setlocal

set EXECUTABLE=%cd%\apache-tomcat-6.0.18\bin\startup.bat

if exist "%EXECUTABLE%" goto okExec
echo Cannot find %EXECUTABLE%
echo This file is needed to run this program
goto end

:okExec

call "%EXECUTABLE%"

:end
