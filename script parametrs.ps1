#Connect-VIServer -Server 10.199.0.22 -Credential (Get-Credential)

#Param
[String]$Name = Read-Host ("Customization Specification Name")
[String]$OwnerName = Read-Host ("Owner Name")
[String]$OrgName = Read-Host ("Organization Name")
$apassword = Read-Host ("Admin password") -AsSecureString
$AdminPwd = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($apassword))
[String]$numbers = Read-Host("Customization count")
[String]$Timezone = Read-Host("TimeZone")


#[String]$Script = Read-Host("GuiRunOnce")
[Int]$Script = Read-Host ("Select 1 If You want to pass Script or Select 2 to skip")
if ($Script -eq 1) {
    [String]$Host = Read-Host ("Provide Host VM")
    [String]$Folder = Read-Host ("Provide Folder")
    [String]$Server = Read-Host ("Provide Server Name")
}
if ($Script -eq 2) {
    [String]$GUIRunOnce = $null
}


[String]$ip = Read-Host ("Provide IP")
[Int]$Group = Read-Host ("Select 1 for Workgroup or Select 2 for Domain")
if ($Group -eq 1) { 
    [String]$Workgroup = Read-Host ("Workgroup") 
}
if ($Group -eq 2) { 
    [String]$Domain = Read-Host ("Domain Name")
    [String]$Domainusername = Read-Host ("Domain Username")
    $dpwd= Read-Host ("Domain Password") -AsSecureString
    $DomainPwd = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($dpwd))
}
[String]$Subnetmask = Read-Host ("Subnet Mask")
[String]$DefaultGateway = Read-Host ("Default Gateway")
[String]$Primary = Read-Host ("Primary DNS")
[String]$Secondary = Read-Host ("Seconday DNS")


if ("$numbers" -match "-") {
    $counts = $numbers.Split("-")

    $nip = @($ip)
    $count = @()

    for ($i = [int]$counts[0]; $i -le [int]$counts[1] - 1; $i++) {
        $octets = $ip.Split(".") # or $octets = $IP -split "\." 10.2.0.1- 10.2.0.10
        $octets[3] = [string]([int]$octets[3] + 1) # or other manipulation of the third octet
        $newIP = $octets -join "."
        $nip += $newIP
        $ip = $newIP
    }
    for ($i = [int]$counts[0]; $i -le [int]$counts[1] ; $i++) {
        $count += "$i" 
    }
}
else {
    if ("$numbers" -match ",") {
    $count = $numbers.Split(",")
    $nip = $ip.Split(",")                        
}
else {
    $count = $numbers
    $nip = @($ip)
}
}
$i = 0
foreach ($c in $count) {
    if ([int]$c -le 9) {
        $CustName = $Name + "0" + $c
    }
    else { 
        $CustName = $Name + $c
    } 
    
    if ($script -eq 1) {


    if ($Group -eq 1) {
        $CustomSpec = New-OSCustomizationSpec -Name "$CustName" -OSType Windows -FullName "$OwnerName" -OrgName "$OrgName" -Workgroup "$Workgroup" -AdminPassword "$AdminPwd" -ChangeSid -AutoLogonCount 0 -TimeZone "$Timezone" -GuiRunOnce "Powershell.exe –ExecutionPolicy Bypass -file \\$Host\$Folder\$Server\script.ps1"
    }
    if ($Group -eq 2) {
        $CustomSpec = New-OSCustomizationSpec -Name "$CustName" -OSType Windows -FullName "$OwnerName" -OrgName "$OrgName" -Domain "$Domain" -DomainUsername "$Domainusername" -DomainPassword "$DomainPwd"  -AdminPassword "$AdminPwd" -ChangeSid -AutoLogonCount 0 -TimeZone "$Timezone" -GuiRunOnce "Powershell.exe –ExecutionPolicy Bypass -file \\$Host\$Folder\$Server\script.ps1"
    }
    $CustomSpec | Get-OSCustomizationNicMapping | Set-OSCustomizationNicMapping -IpMode UseStaticIP -IPAddress $nip[$i] -SubnetMask "$Subnetmask" -DefaultGateway "$DefaultGateway" -Dns ("$Primary","$Secondary")

    $i += 1
}


 if ($script -eq 2) {


    if ($Group -eq 1) {
        $CustomSpec = New-OSCustomizationSpec -Name "$CustName" -OSType Windows -FullName "$OwnerName" -OrgName "$OrgName" -Workgroup "$Workgroup" -AdminPassword "$AdminPwd" -ChangeSid -AutoLogonCount 0 -TimeZone "$Timezone"
    }
    if ($Group -eq 2) {
        $CustomSpec = New-OSCustomizationSpec -Name "$CustName" -OSType Windows -FullName "$OwnerName" -OrgName "$OrgName" -Domain "$Domain" -DomainUsername "$Domainusername" -DomainPassword "$DomainPwd"  -AdminPassword "$AdminPwd" -ChangeSid -AutoLogonCount 0 -TimeZone "$Timezone"
    }
    $CustomSpec | Get-OSCustomizationNicMapping | Set-OSCustomizationNicMapping -IpMode UseStaticIP -IPAddress $nip[$i] -SubnetMask "$Subnetmask" -DefaultGateway "$DefaultGateway" -Dns ("$Primary","$Secondary")

    $i += 1
}
}