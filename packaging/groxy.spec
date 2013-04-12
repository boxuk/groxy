Summary:   Gmail API Proxy
Name:      groxy
Version:   0.1.2
Release:   1%{?dist}
Requires:  tomcat6, tomcat6-webapps
BuildArch: noarch
Group:     Internet / Applications
Vendor:    Box UK
License:   GPL, MIT

# todo - remove hard-coded path
%define _gitrepository ~/groxy

%define _webapps /var/lib/tomcat6/webapps

%description
A JSON web API that proxies OAuth IMAP access to Gmail.

%prep
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT%{_webapps}

pushd `pwd`
cd %{_gitrepository}
lein clean
lein ring uberwar
popd

%install
cp %{_gitrepository}/target/groxy-%{version}-standalone.war $RPM_BUILD_ROOT%{_webapps}/%{name}.war

%files
%defattr(-,root,tomcat,-)
/var/lib/tomcat6/webapps/groxy.war

%pre
service tomcat6 stop
rm -rf %{_webapps}/groxy*

%post
service tomcat6 start

