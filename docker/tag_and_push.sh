#!/bin/sh

echo "Tag (override if necessary) the my-first-service..."
sudo docker tag -f hbcdigital/service:my-first-service-0.1 hd1cutl01lx.saksdirect.com:5000/my-first-service-0.1

echo "Push my-first-service image to Internal Docker Registry..."
sudo docker push hd1cutl01lx.saksdirect.com:5000/my-first-service-0.1

echo "Completed pushing my-first-service image to Internal Docker Registry."
