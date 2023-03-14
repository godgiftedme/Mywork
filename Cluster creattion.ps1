#Creates new cluster with name Mycluster
New-Cluster -Name "MyTestCluster" -Location "CCDataCenter"
#Apply Host Profile
Get-Cluster -Name "MyTestCluster" | Set-Cluster -Name "TestCluster" -HAEnabled:$true -HAAdmissionControlEnabled:$true -HAFailoverLevel 2 -VMSwapfilePolicy "InHostDatastore" -HARestartPriority "Low" -HAIsolationResponse "PowerOff"