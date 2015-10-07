#!/usr/bin/env bash
echo "Starting the sendorderemail-service on port 9000 ..."
docker run -d --name sendorderemail-service -p 9000:9000 hbcdigital/service:sendorderemail-service-0.1
echo "... done."
