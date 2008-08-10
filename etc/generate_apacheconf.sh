#!/bin/sh
# $Id$

# Generate apache.conf from apache.template.conf.
# Replaces text beteen 'AUTOGENERATE HEADER START/END' tags with
# autogenerated message and fills in values for DOMjudge locations
# from configuration.

# Exit on any error:
set -e

CONFIG=apache.conf
TEMPLATE=apache.template.conf

COMMENT="#"
TAG="AUTOGENERATE HEADER"

if [ ! -r "$TEMPLATE" ]; then
	echo "Template '$TEMPLATE' does not exist."
	exit 1
fi

COMMANDLINE="$0 $@"

# Include (shell-version) of config for substitution of variables:
. ./config.sh

# Parse DOMjudge sub-directory location from WEBBASEURI:
WEBSUBDIR=`echo "$WEBBASEURI" | sed "s!^.*$WEBSERVER[^/]*/\(.*\)/!\1!"`
if [ "$WEBSUBDIR" = "$WEBBASEURI" ];
then
	echo "Failed to parse WEBSUBDIR from WEBBASEURI."
	echo "Perhaps WEBBASEURI doesn't end in a slash, which it should?"
	exit 1
fi

TMPFILE=$CONFIG.new
	
if [ `sed -n "/$TAG START/,/$TAG END/ p" $TEMPLATE | grep -c "$TAG"` -ne 2 ];
then
	echo "Template '$TEMPLATE' has not exactly one '$TAG' block"
	exit 1
fi

# This is where the variable replacement magic happens;
# We temporarily replace " -> % to avoid problems with bash quoting.
eval echo "\"`cat $TEMPLATE | sed 's/\"/%/g'`\"" | sed 's/%/"/g' > $TMPFILE

# Update the autogenerate header:
sed -n "0,/$TAG START/ p" $TMPFILE > $CONFIG
cat >>$CONFIG <<EOF
$COMMENT
$COMMENT This configuration file was automatically generated
$COMMENT with command '$COMMANDLINE'
$COMMENT on `date` on host '`hostname`'.
$COMMENT
$COMMENT Edit this file to suit your need; see $TEMPLATE
$COMMENT for more information.
$COMMENT
EOF
sed -n "/$TAG END/,$ p" $TMPFILE >> $CONFIG

rm -f $TMPFILE
