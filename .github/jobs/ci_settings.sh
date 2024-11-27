#!/bin/sh

# Store artifacts/logs
export ARTIFACTS="/tmp/artifacts"
mkdir -p "$ARTIFACTS"

# Functions to annotate the Github actions logs
trace_on () {
    set -x
}
trace_off () {
    {
        set +x
    } 2>/dev/null
}

mysql_log () {
    # shellcheck disable=SC2086
    echo "$1" | mysql ${2:-} | tee -a "$ARTIFACTS"/mysql.txt
}

section_start () {
    trace_off
    echo "::group::$1"
    trace_on
}

section_end () {
    trace_off
    echo "::endgroup::"
    trace_on
}
