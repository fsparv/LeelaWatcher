#!/bin/bash

echo [Desktop Entry] > LeelaWatcher.desktop
echo Name=LeelaWatcher >> LeelaWatcher.desktop
echo Comment=Now saves progress on exit. >> LeelaWatcher.desktop
echo Path=$(pwd) >> LeelaWatcher.desktop
echo Exec=\"$(pwd)/RunLeelaWatcher.sh\" >> LeelaWatcher.desktop
echo Terminal=false >> LeelaWatcher.desktop
echo Type=Application >> LeelaWatcher.desktop
echo Icon=$(pwd)/LeelaWatcher.ico >> LeelaWatcher.desktop
echo Categories=Game\; >> LeelaWatcher.desktop
echo TryExec=$(pwd)/RunLeelaWatcher.sh >> LeelaWatcher.desktop
chmod u+rwx LeelaWatcher.desktop
