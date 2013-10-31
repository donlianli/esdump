esdump
======
build project
mvn assembly:assembly -Dmaven.test.skip=true

elasticsearch backup and restore tool
#备份模式(backup mode,attention,use tcp port 9300)
java -cp esdump-0.0.1-jar-with-dependencies.jar com.donlian.esdump.Esdump -Daddr=localhost:9300 -Dcluster=elasticsearch -Dbackupdir=/bak/es 

#恢复模式(restore mode)
java -cp esdump-0.0.1-jar-with-dependencies.jar com.donlian.esdump.Esdump -Dmode=restore -Daddr=localhost:9300 -Dcluster=elasticsearch -Drestoredir=/bak/es

可以指定index进行备份,也可以备份集群下面的所有索引（如果未指定具体的index).
当需要备份多个索引时，支持多线程备份，多线程还原模式，后期还要增加集群对拷模式。

