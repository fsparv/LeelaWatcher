#!/bin/bash

#wait until process dies
while ps -p $1 > /dev/null
do
  sleep 1
done

#send q+Enter
echo -e "q\n"

#kill off process cat children
kill -9 $1
