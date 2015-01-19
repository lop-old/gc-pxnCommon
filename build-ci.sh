#!/bin/bash
# sh build-ci.sh  --dl-path=/home/pxn/www/dl/pxnCommon  --yum-path=/home/pxn/www/yum/extras-testing/noarch



PWD=`pwd`
# load build_utils.sh script
if [ -e "${PWD}/build_utils.sh" ]; then
	source ${PWD}/build_utils.sh"
elif [ -e "/usr/local/bin/pxn/build_utils.sh" ]; then
	source "/usr/local/bin/pxn/build_utils.sh"
else
	wget "https://raw.githubusercontent.com/PoiXson/shellscripts/master/pxn/build_utils.sh" \
		|| exit 1
	source "${PWD}/build_utils.sh"
fi


NAME="pxnCommon"
[ -z "${WORKSPACE}" ] && WORKSPACE=`pwd`
rm -vf "${WORKSPACE}/${NAME}"-*.jar
rm -vf "${WORKSPACE}/${NAME}"-*.noarch.rpm


title "Build.."
( cd "${WORKSPACE}/" && sh build-mvn.sh --build-number ${BUILD_NUMBER} ) || exit 1
( cd "${WORKSPACE}/" && sh build-rpm.sh --build-number ${BUILD_NUMBER} ) || exit 1


title "Deploy.."
cp -fv "${WORKSPACE}/${NAME}"-*.jar        "${DL_PATH}/" || exit 1
cp -fv "${WORKSPACE}/${NAME}"-*.noarch.rpm "${DL_PATH}/" || exit 1
latest_version "${DL_PATH}/${NAME}-*.noarch.rpm"         || exit 1
echo "Latest version: "${LATEST_FILE}
ln -fs "${LATEST_FILE}" "${YUM_PATH}/${NAME}.noarch.rpm" || exit 1

