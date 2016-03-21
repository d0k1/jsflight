sudo killall firefox
sudo killall Xvfb
sudo Xvfb :10 -ac &
export DISPLAY=:10