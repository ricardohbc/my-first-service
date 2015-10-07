#!/bin/sh

echo "Tag (override if necessary) the sendorderemail-service..."
sudo docker tag -f hbcdigital/service:sendorderemail-service-0.1 hd1cutl01lx.saksdirect.com:5000/sendorderemail-service-0.1

echo "Push sendorderemail-service image to Internal Docker Registry..."
sudo docker push hd1cutl01lx.saksdirect.com:5000/sendorderemail-service-0.1

echo "Completed pushing sendorderemail-service image to Internal Docker Registry."
