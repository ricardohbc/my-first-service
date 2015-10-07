#!/usr/bin/env bash
echo "Stopping the sendorderemail-service ..."
docker stop sendorderemail-service; docker rm sendorderemail-service
echo "... done."
