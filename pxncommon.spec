Name            : pxnCommon
Summary         : Common library for PoiXson java projects
Version         : 3.5.%{BUILD_NUMBER}
Release         : 1
BuildArch       : noarch
Prefix          : %{_javadir}
Requires        : java >= 7
%define  _rpmfilename  %%{NAME}-%%{VERSION}-%%{RELEASE}.%%{ARCH}.rpm
%define  jarfile       "%{SOURCE_ROOT}/pxnCommon-%{version}-SNAPSHOT.jar"

Group           : Development
License         : (c) PoiXson
Packager        : PoiXson <support@poixson.com>
URL             : http://poixson.com/



%description
Library of common classes and utilities for PoiXson projects.



# avoid jar repack
%define __jar_repack %{nil}
# avoid centos 5/6 extras processes on contents (especially brp-java-repack-jars)
%define __os_install_post %{nil}

# disable debug info
# % define debug_package %{nil}



### Prep ###
%prep
echo
echo "Prep.."
# check for existing workspace
if [ -d "%{SOURCE_ROOT}" ]; then
	echo "Found source workspace: %{SOURCE_ROOT}"
else
	echo "Source workspace not found: %{SOURCE_ROOT}"
	exit 1
fi
# check for pre-built jar files
if [ ! -f "%{jarfile}" ]; then
	echo "%{jarfile} file is missing"
	exit 1
fi
echo
echo



### Build ###
%build



### Install ###
%install
echo
echo "Install.."
# delete existing rpm
if [[ -f "%{_rpmdir}/%{name}-%{version}-%{release}.noarch.rpm" ]]; then
	%{__rm} -f "%{_rpmdir}/%{name}-%{version}-%{release}.noarch.rpm" \
		|| exit 1
fi
# create directories
%{__install} -d -m 0755 \
	"${RPM_BUILD_ROOT}%{prefix}" \
		|| exit 1
# copy jar files
%{__install} -m 0555 \
	"%{jarfile}" \
	"${RPM_BUILD_ROOT}%{prefix}/pxnCommon-%{version}_%{release}.jar" \
		|| exit 1



%check



%clean
# %{__rm} -rf ${RPM_BUILD_ROOT} || exit 1



### Files ###

%files
%defattr(-,root,root,-)
%{prefix}/pxnCommon-%{version}_%{release}.jar


