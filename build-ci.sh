# (cd ${WORKSPACE}; sh build-ci.sh)



echo "Deploy.."
rm -fv "/home/pxn/www/yum/extras-testing/noarch/pxnCommon"-*.noarch.rpm
cp -fv "${WORKSPACE}/pxnCommon"-*.noarch.rpm "/home/pxn/www/yum/extras-testing/noarch/" || exit 1


