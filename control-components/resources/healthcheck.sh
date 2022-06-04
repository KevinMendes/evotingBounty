#!/usr/bin/env bash
if [  $(pgrep java) -eq 1 ] ; then
  echo "Healthcheck: OK, Java process running"
else
  echo "Healthcheck: NOK: Java main process (pid 1) not found"
  exit 1
fi