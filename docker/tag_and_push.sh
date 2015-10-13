#!/bin/sh

echo "Tag (override if necessary) the hbc-microservice-template..."
sudo docker tag -f hbcdigital/service:hbc-microservice-template-0.1 hd1cutl01lx.saksdirect.com:5000/hbc-microservice-template-0.1

echo "Push hbc-microservice-template image to Internal Docker Registry..."
sudo docker push hd1cutl01lx.saksdirect.com:5000/hbc-microservice-template-0.1

echo "Completed pushing hbc-microservice-template image to Internal Docker Registry."
