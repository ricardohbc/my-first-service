#!/usr/bin/env bash

echo "Building Distribution ZIP for sendorderemail-service..."
if ! [ -a ../target/universal/sendorderemail-service-0.1.zip ]  ; then
	echo "Distribution ZIP not found, building from source..."
	cd ../ && sbt "project sendorderemail-service" dist
	cd docker
fi

echo "Copying sendorderemail-service ZIP to current directory..."
cp ../target/universal/sendorderemail-service-0.1.zip .

tag=hbcdigital/service:sendorderemail-service-0.1

echo "Building sendorderemail-service Docker Container.."
sudo docker build -t ${tag} .

echo "Removing ZIP..."
rm sendorderemail-service-0.1.zip

echo "Completed building image: $tag"

