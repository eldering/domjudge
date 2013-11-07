#!/bin/bash

# this is just a dirty hack to test the scoreboard in live mode
# Usage:
# ./livescoreboardfeed  <feed-url> <http-user> <http-passwd> <sleeptime>

# Example call:
# a) start feed first
# ./livescoreboardfeed http://example.com/domjudge/plugin/ext.php user pass 5 | nc -l 3333
# b) then start server
# ./spider
# c) last start client
# ./scoreboard

URL=$1
USER=$2
PASS=$3
SLEEPTIME=$4

OLD=`tempfile`
NEW=`tempfile`
while /usr/bin/env true; do
	wget -q --http-user=$USER --http-passwd=$PASS $URL -O - | xmllint --format - | grep -v "</contest>" > $NEW;
	LINES=`wc -l $OLD | awk '{print $1;}'`;
	LINES=$((LINES+1));
	tail -n +$LINES $NEW;
	cp $NEW $OLD;
	sleep ${SLEEPTIME}s;
done
