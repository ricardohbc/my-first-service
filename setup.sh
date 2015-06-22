read -p "Please enter service name: " SERVICE_NAME

if [ -z "$SERVICE_NAME" ]
then
        echo "Please enter a valid service name!"
        exit;
elif ! [[ "$SERVICE_NAME" =~ ^[a-z][a-z-]*-service$ ]]
then
        echo "Service name does not conform to current naming scheme! Please check README.mkd for correct scheme!"
        read -p "Are you sure you want to continue? [y/n]: " -n 1 -r
        echo
        if [[ $REPLY =~ ^[^Yy]$ ]]
        then
                exit;
        fi
fi

URL_REGEX='(https?|ftp|file)://[-A-Za-z0-9\+&@#/%?=~_|!:,.;]*[-A-Za-z0-9\+&@#/%=~_|]'

read -p "Please enter url of new service repo: " REPO_URL

if [[ -z "$REPO_URL" || ! $REPO_URL =~ $URL_REGEX ]]
then
        echo "Please enter a valid url!"
        exit;
fi

git remote rm origin
git remote add origin $REPO_URL
git config master.remote origin
git config master.merge refs/heads/master

echo "Removing setup.sh and README.mkd (these are related to the template, not the new service)"
rm -f setup.sh
rm -f README.mkd


echo "Renaming README-template.mkd to README.mkd"
mv README-template.mkd README.mkd

echo "Replacing all instances of 'hbc-microservice-template' with '$SERVICE_NAME'"
LC_ALL=C find . -path ./.git -prune -o -type f -exec sed -i '' -E "s/hbc-microservice-template/$SERVICE_NAME/g" {} \; > /dev/null

git commit -a -m "Setup new service"
git push -u origin master

echo "Renaming current folder from 'hbc-microservice-template' to '$SERVICE_NAME'"
cd ..
mv hbc-microservice-template $SERVICE_NAME
cd $SERVICE_NAME

echo "Done! Enjoy your new micro service!"
echo "Please run \"cd .\" command, as the current directory you are on right now has been changed."
