#!/usr/bin/env bash

echo "Building Distribution ZIP for my-first-service..."
if ! [ -a ../target/universal/my-first-service-0.1.zip ]  ; then
	echo "Distribution ZIP not found, building from source..."
	cd ../ && sbt "project my-first-service" dist
	cd docker
fi

echo "Copying my-first-service ZIP to current directory..."
cp ../target/universal/my-first-service-0.1.zip .

tag=hbcdigital/service:my-first-service-0.1

echo "Building my-first-service Docker Container.."
sudo docker build -t ${tag} .

echo "Removing ZIP..."
rm my-first-service-0.1.zip

echo "Completed building image: $tag"
