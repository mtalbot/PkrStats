package components

import play.api.libs.json.Writes
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import play.api.libs.json.JsValue
import play.api.libs.json.JsValueSerializer
import java.io.StringWriter
import play.api.libs.json.Json
import play.api.libs.json.Reads

object JsonSupport {
  val mapper = new ObjectMapper() with ScalaObjectMapper

  mapper.registerModule(DefaultScalaModule)
}

class JsonSerializer[A] extends Writes[A] {
  override def writes(o: A): JsValue = {
    val writer = new StringWriter()

    JsonSupport.mapper.writeValue(writer, o)

    Json.parse(writer.toString())
  }
}