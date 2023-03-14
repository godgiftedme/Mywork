import groovy.json.JsonSlurper

pipeline {
    agent any

    parameters {
        string(name: 'VcenterServer', defaultValue: '10.199.0.22', description: 'Enter the Vcenter IP')
        string(name: 'Vcenterusername', defaultValue: 'administrator@nvtesting.local', description: 'Enter the username')
        password(name: 'VcenterPassword', description: 'Enter the Vcenter Password')
        string(name: 'Name', defaultValue: 'WCFServer', description: 'Enter the Name of Customization Server')
        string(name: 'OwnerName', defaultValue: 'NV', description: 'Enter the Owner Name')
        string(name: 'OrgName', defaultValue: 'CC', description: 'Enter the Name of Organization')
        password(name: 'AdminPwd', description: 'Enter the Admin Password')
        string(name: 'Count', defaultValue: '1', description: 'Enter the Customization Count')
        string(name: 'Timezone', defaultValue: '190', description: 'Enter the Valid TimeZone')
        string(name: 'Ip', defaultValue: '10.199.0.100', description: 'Enter the Static IP')
        string(name: 'Subnetmask', defaultValue: '255.255.0.0', description: 'Enter the Subnet Mask Value')
        string(name: 'Defaultgtwy', defaultValue: '10.199.0.50', description: 'Enter the Default Gateway')
        string(name: 'Primarydns', defaultValue: '10.199.0.10', description: 'Enter Primary DNS Value')
        string(name: 'Secondarydns', defaultValue: '10.199.0.11', description: 'Enter Secondary DNS Value')
        choice(name:'Group', choices:['1', '2'], description: 'Select 1 for Workgroup and 2 for Domain')
        string(name: 'Domainname', defaultValue: 'tushar.local', description: 'Enter the name of Domain')
        string(name: 'Domainusername', defaultValue: 'Administrator', description: 'Enter Domain Username')
        password(name: 'Domain_Password', description: 'Enter Domain Password')
        choice(
            name: 'GuiRunOnce',
            choices: ['1', '2'],
            description: 'Select 1 to pass the script or Select 2 to skip'
        )
        string(name: 'CentralServer', defaultValue: 'WIN-RSBL7FMKHCA', description: 'Enter the Central Server Hostname or IP')
        string(name: 'FolderName', defaultValue: 'NVSharing', description: 'Enter the Share Folder Name')
        string(name: 'CenntralServerUser', defaultValue: 'administrator', description: 'Enter the Central Server Username')
        string(name: 'FolderName', defaultValue: 'NVSharing', description: 'Enter the Share Folder Name')
        password(name: 'CentralServerPassword', description: 'Central Server Password')
        choice(
            name: 'Application',
            choices: ['WCF', 'WebServer', 'KMS', 'SSRS', 'RD', 'AppServer', 'PrimaryFileServer', 'SecondaryFileServer'],
            description: 'Select the Application Name'
        )
    }

    stages {
        stage('build') {
            steps {
                powershell('''#Param
                Connect-VIServer -Server $env:VcenterServer -user $env:Vcenterusername -Password $env:VcenterPassword
                #Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -scope CurrentUser
[String]$Name = $env:Name
[String]$OwnerName = $env:OwnerName
[String]$OrgName = $env:OrgName
[String]$AdminPwd = $env:AdminPwd
[String]$numbers = $env:Count
[String]$Timezone = $env:Timezone
[String]$ip = $env:Ip

[Int]$Group = $env:Group
if ($Group -eq 1) {
    [String]$Workgroup = "Workgroup"
}
if ($Group -eq 2) {
    [String]$Domain = $env:Domainname
    [String]$Domainusername = $env:Domainusername
    $DomainPwd= $env:Domain_Password
}
[String]$Workgroup = "Workgroup"
[String]$Subnetmask = $env:Subnetmask
[String]$DefaultGateway = $env:Defaultgtwy
[String]$Primary = $env:Primarydns
[String]$Secondary =$env:Secondarydns
[String]$GuiRunOnce =$env:GuiRunOnce
[String]$CentralServer = $env:CentralServer
[String]$FolderName = $env:FolderName
[String]$CenntralServerUser = $env:CenntralServerUser
[String]$CentralServerPassword = $env:CentralServerPassword
[String]$Application = $env:Application

if ("$numbers" -match "-") {
    $counts = $numbers.Split("-")

    $nip = @($ip)
    $count = @()

    for ($i = [int]$counts[0]; $i -le [int]$counts[1] - 1; $i++) {
        $octets = $ip.Split(".") # or $octets = $IP -split "/." 10.2.0.1- 10.2.0.10
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
    if ($GuiRunOnce -eq 1) {
    
    if ($Group -eq 1) {
        $CustomSpec = New-OSCustomizationSpec -Name "$CustName" -OSType Windows -FullName "$OwnerName" -OrgName "$OrgName" -Workgroup "$Workgroup" -AdminPassword "$AdminPwd" -ChangeSid -AutoLogonCount 3 -TimeZone "$Timezone" -GuiRunOnce "cmd.exe /C net use \\$CentralServer\\$FolderName /user:$CenntralServerUser $CentralServerPassword && Powershell.exe –ExecutionPolicy Bypass -file \\$CentralServer\\$FolderName\\$Application\\script.ps1''
        "
        }
    if ($Group -eq 2) {
        $CustomSpec = New-OSCustomizationSpec -Name "$CustName" -OSType Windows -FullName "$OwnerName" -OrgName "$OrgName" -Domain "$Domain" -DomainUsername "$Domainusername" -DomainPassword "$DomainPwd"  -AdminPassword "$AdminPwd" -ChangeSid -AutoLogonCount 3 -TimeZone "$Timezone" -GuiRunOnce "cmd.exe /C net use \\$CentralServer\\$FolderName /user:$CenntralServerUser $CentralServerPassword && Powershell.exe –ExecutionPolicy Bypass -file \\$CentralServer\\$FolderName\\$Application\\script.ps1'"
    }
    }
    if ($GuiRunOnce -eq 2) {
    
    if ($Group -eq 1) {
        $CustomSpec = New-OSCustomizationSpec -Name "$CustName" -OSType Windows -FullName "$OwnerName" -OrgName "$OrgName" -Workgroup "$Workgroup" -AdminPassword "$AdminPwd" -ChangeSid -AutoLogonCount 3 -TimeZone "$Timezone"
    }
    if ($Group -eq 2) {
        $CustomSpec = New-OSCustomizationSpec -Name "$CustName" -OSType Windows -FullName "$OwnerName" -OrgName "$OrgName" -Domain "$Domain" -DomainUsername "$Domainusername" -DomainPassword "$DomainPwd"  -AdminPassword "$AdminPwd" -ChangeSid -AutoLogonCount 3 -TimeZone "$Timezone"
    }
    }

    $CustomSpec | Get-OSCustomizationNicMapping | Set-OSCustomizationNicMapping -IpMode UseStaticIP -IPAddress $nip[$i] -SubnetMask "$Subnetmask" -DefaultGateway "$DefaultGateway" -Dns ("$Primary","$Secondary")

    $i += 1
}''')
            }
        }
    }
}
