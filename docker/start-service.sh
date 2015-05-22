#!/usr/bin/env bash
echo "Starting the hbc-microservice-template on port 9000 ..."
docker run -d --name hbc-microservice-template -p 9000:9000 hbcdigital/service:hbc-microservice-template-0.1
echo "... done."
