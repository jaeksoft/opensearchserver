#!/bin/sh
 
#	License Agreement for OpenSearchServer
#	
#	Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
#	
#	http://www.open-search-server.com
#	
#	This file is part of OpenSearchServer.
#	
#	OpenSearchServer is free software: you can redistribute it and/or
#	modify it under the terms of the GNU General Public License as published by
#	the Free Software Foundation, either version 3 of the License, or
#	(at your option) any later version.
#	
#	OpenSearchServer is distributed in the hope that it will be useful,
#	but WITHOUT ANY WARRANTY; without even the implied warranty of
#	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#	GNU General Public License for more details.
#	
#	You should have received a copy of the GNU General Public License
#	along with OpenSearchServer. 
#	If not, see <http://www.gnu.org/licenses/>.

cd `dirname "$0"`
	
EXECUTABLE=apache-tomcat-7.0.37/bin/startup.sh

if [ ! -x "$EXECUTABLE" ]; then
    echo "Cannot find $EXECUTABLE"
    echo "This file is needed to run this program"
    exit 1
fi

SYSCONFIGFILE=/etc/sysconfig/opensearchserver
if [ -f "$SYSCONFIGFILE" ]; then
    . "$SYSCONFIGFILE"
fi

CONFIGFILE=/etc/opensearchserver
if [ -f "$CONFIGFILE" ]; then
    . "$CONFIGFILE"
fi

if [ -z "$OPENSEARCHSERVER_DATA" ]; then
     OPENSEARCHSERVER_DATA=`pwd`/data
     export OPENSEARCHSERVER_DATA
fi

exec "$EXECUTABLE"
