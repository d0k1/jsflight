#!/usr/bin/env bash
set -e

CURRENT_DIR=$( pwd )
cd "$( dirname "${BASH_SOURCE[0]}" )"

VERSION=$(grep -oP "<version>\K(.*?)(?=(-SNAPSHOT)?</version>)" pom.xml| head -n 1)

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

echo "Version: ${VERSION}"
echo "Tag: ${TAG}"
echo "New development version: ${NEW_DEV_VERSION}"

mvn -B versions:set -DnewVersion="${VERSION}" -DgenerateBackupPoms=false
git commit -am "Version ${VERSION}"
git tag -a ${TAG} -m "Version ${VERSION}"
git push origin ${TAG}
git reset --hard HEAD^1

mvn -B versions:set -DnewVersion="${NEW_DEV_VERSION}" -DgenerateBackupPoms=false
git commit -am "New development version: ${NEW_DEV_VERSION}"

push_changes() {
    echo "Which remote you want to use?"
    remotes=( $(git remote) )
    select remote_name in ${remotes[@]}; do
        if [ ! -z "${remote_name}" ]; then
            branch_name=$( git branch | grep '*' | cut -d' ' -f 2 )
            read -p "Which branch to use (default: ${branch_name}): " TMP
            if [ ! -z "${TMP}" ]; then
                branch_name="${TMP}"
            fi
            git push ${remote_name} ${branch_name}
            break
        fi
    done
}

echo "Do you want to push development version change to remote repo?"
select TMP in "Yes" "No"; do
    case "${TMP}" in
        "Yes") push_changes; break;;
        "No") break;;
    esac
done

cd "${CURRENT_DIR}"
