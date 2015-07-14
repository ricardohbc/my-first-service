read -p "Enter commit hash of your existing service: " COMMIT_HASH
COMMIT_COUNT=0
FOUND=0

for i in $(git rev-list --all --remotes); do

  if [[ "$i" == $COMMIT_HASH* ]]; then
    FOUND=$((FOUND+1))
    echo "Found $COMMIT_COUNT commits after '$COMMIT_HASH':"
    git log --pretty=oneline -$COMMIT_COUNT
    echo
  elif [[ "$FOUND" -eq 0 ]]; then
    COMMIT_COUNT=$((COMMIT_COUNT+1))
  fi

done

if [[ $FOUND == 0 ]]; then
  echo "Cannot find commit hash '$COMMIT_HASH'!"
  exit;
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
git branch -d patch_$COMMIT_HASH
git checkout -b patch_$COMMIT_HASH
git reset --soft HEAD~$(($COMMIT_COUNT + 2)) && git commit -m "squash commits for patch"
git format-patch HEAD --stdout > changes.patch



