read -p "Please enter service name: " SERVICE_NAME

if [ -z "$SERVICE_NAME" ]
then
        echo "Please enter a valid service name!"
        exit;
elif ! [[ "$SERVICE_NAME" =~ ^[a-z]+-service$ ]]
then
        echo "Service name does not conform to current naming scheme! Please check README.mkd for correct scheme!"
        read -p "Are you sure you want to continue? [y/n]: " -n 1 -r
        echo
        if [[ $REPLY =~ ^[^Yy]$ ]]
        then
                exit;
        fi
fi

rm -rf .git
LC_ALL=C find ./ -type f -exec sed -i '' -E "s/hbc-microservice-template/$SERVICE_NAME/g" {} \; > /dev/null
cd ..
mv hbc-microservice-template $SERVICE_NAME
cd $SERVICE_NAME

echo "Removing setup.sh and README.mkd since you don't need it anymore"
rm -f setup.sh
rm -f README.mkd

echo "Renaming README-template.mkd to README.mkd"
mv README-template.mkd README.mkd

git init

echo "Done! Enjoy your new micro service!"
echo "Please run \"cd .\" command, as the current directory you are on right now has been changed."
