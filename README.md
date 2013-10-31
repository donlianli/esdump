esdump
======

elasticsearch backup and restore tool
#备份模式(backup mode,attention,use tcp port 9300)
java -cp esdump-0.0.1-jar-with-dependencies.jar com.donlian.esdump.Esdump -Daddr=localhost:9300 -Dcluster=elasticsearch