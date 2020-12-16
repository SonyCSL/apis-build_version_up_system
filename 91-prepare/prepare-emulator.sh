#!/bin/bash

DIR=`dirname $0`
BASEDIR=$DIR/..

DO_MAIN() {
	cd ${WORK_DIR}
	echo ">> git clone ${GIT_BASE_URL}/apis-emulator.git"
	git clone ${GIT_BASE_URL}/apis-emulator.git
}

. ${BASEDIR}/00-util/main.sh