Summary:       Gmail API Proxy
Name:          groxy
Version:       %{_ver}
Release:       2%{?dist}
BuildArch:     noarch
Group:         Internet / Applications
Vendor:        Box UK
License:       GPL, MIT
Source:        %{name}-%{version}.tar.gz
AutoReqProv:   no

BuildRequires: java-1.6.0-openjdk-devel

Requires:      tomcat6, tomcat6-webapps

%define _gitrepository .
%define _webapps /var/lib/tomcat6/webapps
%define _lein ./lein

%description
A JSON web API that proxies OAuth IMAP access to Gmail.

%prep
%setup

%build
curl -o %{_lein} https://raw.github.com/technomancy/leiningen/stable/bin/lein
chmod 755 %{_lein}
%{_lein} ring uberwar %{name}.war

%install
mkdir -p $RPM_BUILD_ROOT%{_webapps}

cp target/%{name}.war $RPM_BUILD_ROOT%{_webapps}/

%files
%defattr(-,root,tomcat,-)
/var/lib/tomcat6/webapps/%{name}.war

%pre
service tomcat6 stop
rm -rf %{_webapps}/%{name}/

%post
service tomcat6 start

