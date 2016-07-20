#!/bin/bash
killall Xvfb
for i in `seq 0 200`; do
	Xvfb :$i -ac -dpms -noreset +extension GLX +render -shmem &
done
