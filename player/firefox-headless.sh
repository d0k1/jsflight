killall xvfb
Xvfb :10 -ac & 
export DISPLAY=:10
firefox
killall xvfb