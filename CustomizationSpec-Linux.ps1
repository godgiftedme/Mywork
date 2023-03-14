#Param

[String]$Name = Read-Host ("Customization Specification Name")
[String]$numbers = Read-Host("Customization count")
#[String]$Domain = Read-Host ("Domain Name")
#[String]$Primary = Read-Host ("Primary DNS")
#[String]$Secondary = Read-Host ("Seconday DNS")
[String]$ip = Read-Host ("Provide IP")
[String]$Subnetmask = Read-Host ("Subnet Mask")
[String]$DefaultGateway = Read-Host ("Default Gateway")
#[String]$Timezone = Read-Host ("TimeZone")

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

$CustomSpec = New-OSCustomizationSpec -Name "$CustName" -OSType Linux -Server "10.206.5.44" -Type Persistent -Domain "testlinux"  #-TimeZone "$Timezone" -GuiRunOnce
$CustomSpec | Get-OSCustomizationNicMapping | Set-OSCustomizationNicMapping -IpMode UseStaticIP -IPAddress $nip[$i] -SubnetMask "$Subnetmask" -DefaultGateway "$DefaultGateway" 


    $i += 1
    }