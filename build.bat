@echo off
cls

color 0e
call gradlew assemble

if %errorlevel% == 0 (
   cls
   color
   java -jar build\libs\LeelaWatcher-1.0.2.jar . "C:\Users\Felix\Desktop\Leela Zero\autogtp.exe" -hideoutput
) else (
  color 0c
  echo "error QQ"
)
