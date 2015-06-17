echo "Here are the last 20 commits:"
git log --pretty=oneline -20

read -p "How many commits do you want to include in this patch?: " COMMIT_COUNT

if [[ -z $COMMIT_COUNT || ! $COMMIT_COUNT =~ ^[0-9]+$ ]]
then
        echo "Please enter a number!"
        exit;
else
        echo "You have selected the following commits:"
        git log --pretty=oneline -$COMMIT_COUNT
        read -p "Is this correct? [y/n]: " -n 1 -r
        echo
        if [[ $REPLY =~ ^[^Yy]$ ]]
        then
                exit;
        fi
fi

read -p "What is the version of this patch (must follow semantic versioning, ex v1.0.0)?: " VERSION_TAG
if [[ -z $VERSION_TAG || ! $VERSION_TAG =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]]
then
        echo "Please enter a valid version! (http://semver.org/)"
        exit;
elif [[ $(git tag -l $VERSION_TAG | wc -l) -ne 0 ]]
then
        read -p "The tag $VERSION_TAG already exists. Continue? [y/n]" -n 1 -r
        echo
        if [[ $REPLY =~ ^[^Yy]$ ]]
        then
                exit;
        fi
else
        echo "You have entered: $VERSION_TAG"
        read -p "Is this correct? [y/n]: " -n 1 -r
        echo
        if [[ $REPLY =~ ^[^Yy]$ ]]
        then
                exit;
        fi
fi

COMMIT_HASH="$(git rev-parse HEAD | head -c7)"

sed -i "" s/template-version=[^$]*$/template-version=\"$VERSION_TAG\"/g conf/application.conf
sed -i "" s/template-commit-hash=[^$]*$/template-commit-hash=\"$COMMIT_HASH\"/g conf/application.conf

git tag -a "$VERSION_TAG" -m "Adding new tag for patching" -f
git push origin --tags -f

git add conf/application.conf
git commit -m "Adding commit for new application.conf"
git push origin $(git rev-parse --abbrev-ref HEAD)

git format-patch HEAD~$(($COMMIT_COUNT + 1))..HEAD --stdout > changes.patch



