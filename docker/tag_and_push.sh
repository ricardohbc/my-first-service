#!/bin/sh

echo "Tag (override if necessary) the service..."
sudo docker tag -f hbcdigital/service:service-0.1 hd1cutl01lx.saksdirect.com:5000/service-0.1

echo "Push service image to Internal Docker Registry..."
sudo docker push hd1cutl01lx.saksdirect.com:5000/service-0.1

echo "Completed pushing service image to Internal Docker Registry."
