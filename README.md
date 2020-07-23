# Hazelcast Client Service App

Use [Procrun](http://commons.apache.org/proper/commons-daemon/procrun.html)

```
prunsrv.exe install "HZC1" --DisplayName="HZC1"   --ServiceUser="ACME\hzqe$"  --Jvm="C:\tools\zulu11.39.15-ca-jdk11.0.7-win_x64\bin\server\jvm.dll" --Startup=manual --Classpath="C:\tools\hazelcast-client-service.jar" --StartMode=jvm --StartClass=cz.cacek.test.ClientServiceStarter --StartMethod=start --StopMode=jvm --StopClass=cz.cacek.test.ClientServiceStarter --StopMethod=stop --LogPath="C:\tools\logs" --LogPrefix=service --StdOutput=c:\tools\logs\hazelcast-client-out.log --StdError=c:\tools\logs\hazelcast-client-err.log
```
