#!/usr/bin/env bash
echo "Stopping the service ..."
docker stop service; docker rm service
echo "... done."
