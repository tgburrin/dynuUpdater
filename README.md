# dynuupdater
A simple dynu dns updating client

The purpose of this repository is to add in a simple dns updater for dynu's service.  It takes in a simple cli config file like this:
```
{
	"access_token": "<removed>",
	"interface_name": "bond0",
	"address_familes": ["ipv4"],
	"targets": {
		"mydomain.net": [null],
		"mydynamicdomain.dynu.net": [null],
		"myprofessionaldomain.com": ["firewall"]
	}
}
```
This will take the one and only one ipv4 address that is assumed to exist on bond0 and publish it as the address for the domain targets.
THe arguments for the targets including a 'null' target are indicating that the domain name itself should gain that IP address.  The case where a name is listed for the domain indicates that a host in the domain should get the address.  In the example above, the IP for bond0 will also be published as the ip for firewall.myprofessionaldomain.com, but not the domain itself.

Please be aware that ipv6 is not well supported yet and it will take the first ipv6 address availble even if it is a local link one.
