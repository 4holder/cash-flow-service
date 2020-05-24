package infrastructure.reads_and_writes

import org.joda.time.DateTime
import play.api.libs.json.{JodaReads, JodaWrites, Reads, Writes}

trait JodaDateTime {
  private val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  implicit val jodaDateWrites: Writes[DateTime] = JodaWrites.jodaDateWrites(pattern)
  implicit val jodaDateReads: Reads[DateTime] = JodaReads.jodaDateReads(pattern)
}
