#!/usr/bin/env bash
echo "Stopping the hbc-microservice-template ..."
docker stop hbc-microservice-template; docker rm hbc-microservice-template
echo "... done."
