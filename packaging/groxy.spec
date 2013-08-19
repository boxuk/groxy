Summary:       Gmail API Proxy
Name:          groxy
Version:       %{_ver}
Release:       4
BuildArch:     noarch
Group:         Internet / Applications
Vendor:        Box UK
License:       GPL, MIT
Source:        %{name}-%{version}.tar.gz
Source1:       initd.conf
Source2:       update_app
AutoReqProv:   no

BuildRequires: java-1.6.0-openjdk-devel, boxuk-leiningen

%define _gitrepository .
%define _prefix /opt/BoxUK
%define _updateapp_prefix /opt/BoxUK

%description
A JSON web API that proxies OAuth IMAP access to Gmail.

%prep
%setup

%build
lein bin

%install
rm -rf $RPM_BUILD_ROOT

mkdir -p $RPM_BUILD_ROOT%{_prefix}/%{name}/bin
cp target/%{name} $RPM_BUILD_ROOT%{_prefix}/%{name}/bin/
echo -n %{_ver} > $RPM_BUILD_ROOT%{_prefix}/%{name}/release-version

mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/init.d/
cp %{_sourcedir}/initd.conf $RPM_BUILD_ROOT%{_sysconfdir}/init.d/%{name}

chmod 755 $RPM_BUILD_ROOT%{_sysconfdir}/init.d/%{name}

mkdir -p $RPM_BUILD_ROOT%{_updateapp_prefix}/%{name}/bin
cp %{_sourcedir}/update_app $RPM_BUILD_ROOT%{_updateapp_prefix}/%{name}/bin

%post
if [ "$1" = "1" ]; then
	chkconfig --add groxy
fi
exit 0

%files
%defattr(-,root,root,-)
%{_prefix}/%{name}
%attr(0755, root, root) /etc/init.d/%{name}
%attr(0744, root, root) %{_prefix}/%{name}/bin/%{name}
%attr(0744, root, root) %{_prefix}/%{name}/release-version
%attr(0744, root, root) %{_updateapp_prefix}/%{name}/bin/update_app

%pretrans
if [ -f %{_prefix}/%{name}/bin/%{name} ]; then
	service groxy stop
	chkconfig groxy off
fi

%posttrans
service groxy start
chkconfig groxy on

