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