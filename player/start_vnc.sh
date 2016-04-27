#! /bin/bash

x11vnc -ncache -localhost -display :10 & 
sleep 2
vncviewer -viewonly -truecolour -quality 9 localhost