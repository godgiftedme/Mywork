#Param
Connect-VIServer -Server 10.206.5.44 -Credential (Get-Credential)

[String]$Name = Read-Host ("Customization Specification Name")
[String]$FullName = Read-Host ("Administrator's Full Name")
[String]$OrgName = Read-Host ("Organization Name")
[String]$AdminPassword = Read-Host ("Admin Password")
#[String]$Domain = Read-Host ("Domain Name")
#[String]$Domainusername = Read-Host ("Domain Username")
#[String]$Domainpassword = Read-Host ("Domain Password")
[String]$ip = Read-Host ("Provide IP")
[Int]$numbers =Read-Host("No. of customization")
$nip = @($ip)
[String]$Subnetmask = Read-Host ("Subnet Mask")
[String]$DefaultGateway = Read-Host ("Default Gateway")
#[String]$DNS = Read-Host ("DNS")

for ($i=1; $i -le $numbers-1; $i++) {
$octets = $ip.Split(".") # or $octets = $IP -split "\."
$octets[3] = [string]([int]$octets[3] + 1) # or other manipulation of the third octet
$newIP = $octets -join "."
$nip += $newIP
$ip = $newIP
}



for ($i=1; $i -le $numbers ; $i++) {
$CustomSpec = New-OSCustomizationSpec -Name "$Name$i" -OSType Windows -FullName "$FullName" -OrgName "$OrgName" -Workgroup "Workgroup" -AdminPassword "$AdminPassword" -ChangeSid -AutoLogonCount 1 #-GuiRunOnce
$CustomSpec | Get-OSCustomizationNicMapping | Set-OSCustomizationNicMapping -IpMode UseStaticIP -IPAddress $nip[$i-1] -SubnetMask "$Subnetmask" -DefaultGateway "$DefaultGateway" -Dns ""
}