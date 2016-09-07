#!/usr/bin/env bash
set -e

CURRENT_DIR=$( pwd )
cd "$( dirname "${BASH_SOURCE[0]}" )"

VERSION=$(grep -oP "<version>\K(.*?)(?=-SNAPSHOT</version>)" pom.xml| head -n 1)

read -p "Enter release version (${VERSION}):" TMP
if [ ! -z "${TMP}" ]; then
  VERSION="${TMP}"
fi

TAG="${VERSION}"

TMP=$(($(echo ${VERSION} | rev | cut -d'.' -f 1 | rev) + 1))
NEW_DEV_VERSION=$(echo "$(echo ${VERSION} | rev | cut -d'.' -f 2- | rev)".${TMP}-SNAPSHOT)

read -p "Enter new development version (${NEW_DEV_VERSION}):" TMP
if [ ! -z "${TMP}" ]; then
  NEW_DEV_VERSION="${TMP}"
fi

echo ${VERSION}
echo ${TAG}
echo ${NEW_DEV_VERSION}
exit 0

mvn -B versions:set -DnewVersion="${VERSION}" -DgenerateBackupPoms=false
git commit -am "Version ${VERSION}"
git tag -a ${TAG} -m "Version ${VERSION}"
git push origin ${TAG}
git reset --hard HEAD^1

mvn -B versions:set -DnewVersion="${NEW_DEV_VERSION}" -DgenerateBackupPoms=false
git commit -am "New development version: ${NEW_DEV_VERSION}"
git push

cd "${CURRENT_DIR}"
