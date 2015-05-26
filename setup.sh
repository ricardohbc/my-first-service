echo "Please enter service name: "
read SERVICE_NAME

if [ -z "$SERVICE_NAME" ]
then
	echo "Please enter a valid service name!"
	exit;
fi

rm -rf .git
find ./ -type f -exec sed -i '' -E "s/hbc-microservice-template/$SERVICE_NAME/g" {} \; > /dev/null
cd ..
mv hbc-microservice-template $SERVICE_NAME
cd $SERVICE_NAME
git init

echo "Done! Please be sure to remove this setup.sh prior to checking it into git!"
