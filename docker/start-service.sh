#!/usr/bin/env bash
echo "Starting the my-first-service on port 9000 ..."
docker run -d --name my-first-service -p 9000:9000 hbcdigital/service:my-first-service-0.1
echo "... done."
