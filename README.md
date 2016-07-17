# experience

Running standalone spark on mac laptop
Eclipse
Scala
git

Download Spark
http://spark.apache.org/downloads.html

Choose 1.6.2 , Prebuild with hadoop

Dont worry, Do not need to have hadoop setup for running in standalone mode. I tried with pre-build user provided hadoop, did not go as 
expected. Spark requires hadoop jars to run so it is better to download with prebuild hadoop.


untar the download spark and go to the untar folder

Start Spark Master

./sbin/start-master.sh
visit http://localhost:8080 and get master address "spark://XXXX.local:7077"

Now lets test spark setup through shell

./bin/spark-shell --master spark://XXXX.local:7077

A scala prompt will come. Run few commands

scala>  val textFile = sc.textFile("access.log")
textFile: org.apache.spark.rdd.RDD[String] = access.log MapPartitionsRDD[1] at textFile at <console>:27

scala> textFile.map(line => line.length).reduce((a, b) => if (a > b) a else b)
An error will come
[Stage 0:>                                                          (0 + 0) / 2]16/07/16 15:52:35 WARN TaskSchedulerImpl: Initial job has not accepted any resources; check your cluster UI to ensure that workers are registered and have sufficient resources
16/07/16 15:52:42 ERROR LiveListenerBus: SparkListenerBus has already stopped! Dropping event SparkListenerStageCompleted(org.apache.spark.scheduler.StageInfo@23d6faa5)
16/07/16 15:52:42 ERROR LiveListenerBus: SparkListenerBus has already stopped! Dropping event SparkListenerJobEnd(0,1468709562176,JobFailed(org.apache.spark.SparkException: Job 0 cancelled because SparkContext was shut down))

This is due to the fact that no processing core has been assinged. Kill this and restart spark shell with below command, which provide codes

./bin/spark-shell --total-executor-cores 2 --master spark://XXXX.local:7077 

Run same command again

scala>  val textFile = sc.textFile("access.log")
textFile: org.apache.spark.rdd.RDD[String] = access.log MapPartitionsRDD[1] at textFile at <console>:27

scala> textFile.map(line => line.length).reduce((a, b) => if (a > b) a else b)
res0: Int = 13

//Successful.


Lets setup spark cluster

Read this

http://spark.apache.org/docs/latest/cluster-overview.html

http://spark.apache.org/docs/latest/img/cluster-overview.png

In our standalone cluster, the master would to cluster manager job. (In production we will connect with YARN / MESOS .. coming up next)

Lets have atleast one worker (we need atleast one worker to do any work)

./sbin/start-slave.sh spark://XXXX.local:7077

Now lets write driver program, I chose scala. (Assuming one has eclipse with scala and git plugins installed)

Start writing a sample program following http://spark.apache.org/docs/latest/programming-guide.html

Here is one mine

package com.gajab.experience

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

object AccessLog {
  def main(args: Array[String]): Unit = {
    println("Hello, world!")
    
    val conf = new SparkConf().setAppName("accesslog").setMaster("local[*]").set("total-executor-cores","1");;
    val sc = new SparkContext(conf)
    
    val data = Array(1, 2, 3, 4, 5);
    val distData = sc.parallelize(data);
    
    val v= distData.reduce((a, b) => a + b);
    println(v);

  }
}

Run as scala program and it gave below error

java.lang.RuntimeException: java.io.InvalidClassException: org.apache.spark.rpc.netty.RequestMessage; local class incompatible: stream classdesc serialVersionUID = -5447855329526097695, local class serialVersionUID = -2221986757032131007


Lets make sure all versions are correct, I tried a lot of combination and this finally worked

Spark installed version 1.6.2
<dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-core_2.10</artifactId>
            <version>1.6.2</version>
</dependency>
and

Scala 2.10.6 and Java 8.

I also posted the comment on stackoverflow
http://stackoverflow.com/questions/35485662/local-class-incompatible-exception-when-running-spark-standalone-from-ide/38414501#38414501


Also ensure that maven is using 1.8. For some reason, probably due to eclipse settings, it was defaulting to 1.5.
In eclipse, Poject properties changed the java compiler version to 1.8 but run time dependencies were still on 1.5.

Configure compiler plugin-in in maven

<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.2</version>
				<configuration>
					<source>1.8</source>
					<target>1.8</target>
				</configuration>
			</plugin>
		</plugins>
	</build>


Default scala libraries would be 2.11.8. To remove incompatible class version it must be 2.10.6. 
To change scala version 
Right click on "Scala Library Container" -> Build Path -> Configure Build Path -> Edit and select Latest 2.10 Bundle

Run As Scala Program



It must work like a charm.

If you get this error

WARN TaskSchedulerImpl: Initial job has not accepted any resources; check your cluster UI to ensure that workers are registered and have sufficient resources

That means, your spark worker is not working. You must need a worker to do work. http://stackoverflow.com/a/35663422/1733158


If you get error java.lang.ClassNotFoundException: HelloSpark$$anonfun$1
Read
http://stackoverflow.com/a/33245734/1733158

Note "local[*]" in val conf = new SparkConf().setAppName("accesslog").setMaster("local[*]").set("total-executor-cores","1");
do not use "spark://XXXX.local:7077"

Also refer 
http://www.datastax.com/dev/blog/common-spark-troubleshooting





//how to setup in AWS http://blog.insightdatalabs.com/spark-cluster-step-by-step/


