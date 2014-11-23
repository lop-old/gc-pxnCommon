# (cd ${WORKSPACE}; sh build-ci.sh)



echo "Build.."
( cd "${WORKSPACE}/" && sh build-mvn.sh --build-number ${BUILD_NUMBER} ) || exit 1
( cd "${WORKSPACE}/" && sh build-rpm.sh --build-number ${BUILD_NUMBER} ) || exit 1



echo "Deploy.."
rm -fv "/home/pxn/www/yum/extras-testing/noarch/pxnCommon"-*.noarch.rpm
cp -fv "${WORKSPACE}/pxnCommon"-*.noarch.rpm "/home/pxn/www/yum/extras-testing/noarch/" || exit 1


