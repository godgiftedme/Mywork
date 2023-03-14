import groovy.json.JsonSlurper
pipeline {
    agent any
    parameters {
        string(name: 'VcenterServer', defaultValue: '10.199.0.22', description: 'Enter the Vcenter IP')
        string(name: 'Vcenterusername', defaultValue: 'administrator@nvtesting.local', description: 'Enter the username')
        password(name: 'VcenterPassword', description: 'Enter the Vcenter Password')
        string(name: 'ClientName', defaultValue: 'WCF', description: 'Enter the name of clinet')
        choice(
            name: 'ServerType',
            choices: ['WCF', 'WebServer', 'KMS', 'SSRS', 'RD', 'AppServer', 'PrimaryFileServer', 'SecondaryFileServer'],
            description: 'Select the type of Server'
        )
        choice(name:'choicevar', choices:['1', '2'], description: 'Select 1 for ResourcePool or 2 for VMHost')
        string(name: 'ServerCount', defaultValue: '1', description: 'Enter server count')
        string(name: 'VMhostval', defaultValue: '10.199.0.20', description: 'Enter ip of host')
        string(name: 'DatastoreName', defaultValue: 'DS01', description: 'Enter datastore name')
        string(name: 'ResourcePool', defaultValue: 'ResourceTest', description: 'Enter resource pool')
        string(name: 'UniqueName', defaultValue: 'mastercopy', description: 'Enter unique name of template')
    }
    stages {
        stage('build') {
            steps {
                    powershell('''
Connect-VIServer -Server $env:VcenterServer -user $env:Vcenterusername -Password $env:VcenterPassword

[String]$client = $env:ClientName
[String]$server =  $env:ServerType
[String]$numbers = $env:ServerCount
[Int]$choice =  $env:choicevar
write-host "$choice"
if ($choice -eq 2) {
    Get-VMHost
    $VMhost = $env:VMhostval
}
#Fetching required variables from variable file
$datastore =   $env:DatastoreName
$resourcepool =  $env:ResourcePool
$uniquename =  $env:UniqueName
if ("$numbers" -match "-") {
    $counts = $numbers.Split("-")
    $count = @()
    for ($i = [int]$counts[0]; $i -le [int]$counts[1] ; $i++) {
        $count += "$i"
    }
}
else {
    if ("$numbers" -match ",") {
    $count = $numbers.Split(",")
    }
else {
    $count = $numbers
}
}
$template = $client + $server + $uniquename
write-host "$template"
#Fetching Template, Datastore & ResourcePool and assigning them in variables
$temp = Get-Template -Name $template
$ds = Get-Datastore -Name $datastore
foreach ($c in $count) {
    if ([int]$c -le 9) {
        $name = $client + $server + "0" + $c
    }
    else {
        $name = $client + $server + $c
    }
    #Fetching CustomizationSpec and assigning it in variable
    $customization = Get-OSCustomizationSpec -Name $name
write-host "$customization"
    #Creating New-VM using CustomizationSpec, Template, Datastore & ResourcePool or VM Host variables
    if ($choice -eq 1) {
        New-VM -Name $customization.Name -Template $temp -OSCustomizationSpec $customization -ResourcePool $rp -Datastore $ds -Confirm:$false
    write-host "choice $choice"
    }
    if ($choice -eq 2) {
       New-VM -Name $customization.Name -Template $temp -OSCustomizationSpec $customization -VMHost $VMhost -Datastore $ds -Confirm:$false
    write-host "choice $choice"
    }
    #To start VM
    Start-VM -VM $customization.Name -Confirm:$false
} ''')
            }
        }
    }
}