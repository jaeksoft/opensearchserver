#!/bin/sh

# Move to the directory containing this script
cd `dirname "$0"`

# The location of the PID file
OSS_PID_FILE=logs/oss.pid
if ! [ -f "$OSS_PID_FILE" ]; then
    echo "PID file not found. Stop aborted."
    exit 1
fi

#Extract the PID
OSS_PID=`cat "$OSS_PID_FILE"`

# Check if the process exists
kill -0 $OSS_PID >/dev/null 2>&1
if [ $? -gt 0 ]; then
    echo "No matching process was found. Stop aborted."
    exit 1
fi

# Stopping the process and removing the PID file
kill $OSS_PID & rm $OSS_PID_FILE