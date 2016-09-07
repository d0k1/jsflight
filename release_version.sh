#!/usr/bin/env bash
mvn release:prepare release:clean && echo "Success" || echo "Failed"
git commit -am "New development version"