Summary:       Gmail API Proxy
Name:          groxy
Version:       %{_ver}
Release:       1
BuildArch:     noarch
Group:         Internet / Applications
Vendor:        Box UK
License:       GPL, MIT
Source:        %{name}-%{version}.tar.gz
Source1:	   chkconfig.conf
AutoReqProv:   no

BuildRequires: java-1.6.0-openjdk-devel

%define _gitrepository .
%define _prefix /opt/BoxUK
%define _lein lein

%description
A JSON web API that proxies OAuth IMAP access to Gmail.

%pretrans
service groxy stop
chkconfig groxy off

%prep
%setup

%build
curl -o %{_lein} https://raw.github.com/technomancy/leiningen/stable/bin/lein
chmod 755 %{_lein}
PATH=$PATH:.
rm -rf ~/.m2
%{_lein} bin

%install
rm -rf $RPM_BUILD_ROOT

mkdir -p $RPM_BUILD_ROOT%{_prefix}/%{name}/bin
cp target/%{name} $RPM_BUILD_ROOT%{_prefix}/%{name}/bin/

mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/init.d/
cp %{_sourcedir}/chkconfig.conf $RPM_BUILD_ROOT%{_sysconfdir}/init.d/%{name}

chmod 644 $RPM_BUILD_ROOT%{_sysconfdir}/init.d/
chmod 755 $RPM_BUILD_ROOT%{_sysconfdir}/init.d/%{name}

chkconfig --add groxy

%files
%defattr(-,root,root,-)
%attr(0755, root, root) /etc/init.d/%{name}
%attr(0744, root, root) %{_prefix}/%{name}/bin/%{name}

%posttrans
service groxy start
chkconfig groxy on
 
