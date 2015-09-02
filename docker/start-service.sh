#!/usr/bin/env bash
echo "Starting the service on port 9000 ..."
docker run -d --name service -p 9000:9000 hbcdigital/service:service-0.1
echo "... done."
