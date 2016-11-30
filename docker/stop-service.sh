#!/usr/bin/env bash
echo "Stopping the my-first-service ..."
docker stop my-first-service; docker rm my-first-service
echo "... done."
