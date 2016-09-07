#!/usr/bin/env bash
CURRENT_DIR=$( pwd )
cd "$( dirname "${BASH_SOURCE[0]}" )"
mvn release:prepare release:clean
git commit -am "New development version"
cd "${CURRENT_DIR}"