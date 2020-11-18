package it.aspirin.flinkx

import java.sql.{Connection, DriverManager, PreparedStatement}
import java.util.Properties

import org.apache.flink.api.common.functions.{IterationRuntimeContext, RichFunction, RuntimeContext}
import org.apache.flink.api.common.serialization.{SimpleStringEncoder, SimpleStringSchema}
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer010, FlinkKafkaProducer010}
import org.apache.flink.streaming.api.scala._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector

object KafkaDemo {
  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val kafkaDS = addKafkaSource(env, "hello102119")
    val splitedDS = kafkaDS.map(JSON.parseObject(_))
      .process(new ProcessFunction[JSONObject, JSONObject] {
        override def processElement(obj: JSONObject, context: ProcessFunction[JSONObject, JSONObject]#Context, collector: Collector[JSONObject]): Unit = {
          if(obj.containsKey("process") && obj.getJSONObject("process").containsKey("name")) {
            obj.getJSONObject("process").getString("name") match {
              case "idea" => context.output(new OutputTag[JSONObject]("idea"), obj)
              case "Google Chrome H" => context.output(new OutputTag[JSONObject]("Google Chrome H"), obj)
              case "Mac OS X" => context.output(new OutputTag[JSONObject]("Mac OS X"), obj)
              case "metricbeat" => context.output(new OutputTag[JSONObject]("metricbeat"), obj)
              case "JavaApplication" => context.output(new OutputTag[JSONObject]("JavaApplication"), obj)
              case _ => collector.collect(obj)
            }
          } else {
            context.output(new OutputTag[JSON]("unknown"),obj)
          }
        }
      })
//    splitedDS.print()
    splitedDS.getSideOutput(new OutputTag[JSONObject]("unknown"))
      .print()

    //    addConsoleSink(kafkaDS)
    //    addKafkaSink(value)
    env.execute("ddddd")
  }

  /**
   * 添加kafka数据源
   *
   * @param env 流处理环境
   */
  def addKafkaSource(env: StreamExecutionEnvironment, groupId: String = "test") = {
    val props = new Properties()
    props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    env.addSource(new FlinkKafkaConsumer010[String]("metric-topic", new SimpleStringSchema(), props))
  }

  def addConsoleSink(dataStream: DataStream[String]): Unit = {
    dataStream.print("kafka")
  }

  def addKafkaSink(dataStream: DataStream[String]) = {
    dataStream.addSink(new FlinkKafkaProducer010[String]("", "", new SimpleStringSchema()))
  }

  def addESSink(dataStream: DataStream[String]) = {

  }

  def addJdbcSink(dataStream: DataStream[String]) = {

  }

}

case class SensorReading(id: String, time: Long, tem: Double)












