#!/usr/bin/env bash

echo "Building Distribution ZIP for hbc-microservice-template..."
if ! [ -a ../target/universal/hbc-microservice-template-0.1.zip ]  ; then
	echo "Distribution ZIP not found, building from source..."
	cd ../ && sbt "project hbc-microservice-template" dist
	cd docker
fi

echo "Copying hbc-microservice-template ZIP to current directory..."
cp ../target/universal/hbc-microservice-template-0.1.zip .

tag=hbcdigital/service:hbc-microservice-template-0.1

echo "Building hbc-microservice-template Docker Container.."
sudo docker build -t ${tag} .

echo "Removing ZIP..."
rm hbc-microservice-template-0.1.zip

echo "Completed building image: $tag"

