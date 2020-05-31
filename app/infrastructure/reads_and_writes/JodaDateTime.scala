package infrastructure.reads_and_writes

import org.joda.time.DateTime
import play.api.libs.json.{JodaReads, JodaWrites, Reads, Writes}

trait JodaDateTime {
  implicit val jodaDateWrites: Writes[DateTime] = JodaWrites.jodaDateWrites(JodaDateTime.PATTERN)
  implicit val jodaDateReads: Reads[DateTime] = JodaReads.jodaDateReads(JodaDateTime.PATTERN)
}

object JodaDateTime {
  val PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
}
