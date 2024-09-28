package models

import java.time.LocalDate
import slick.jdbc.SQLiteProfile.api._

case class Concert(id: Option[Int] = None, date: LocalDate, name: String)
case class Track(id: Option[Int] = None, name: String, duration: Int, concertId: Int)
case class TrackInput(name: String, duration: Int)

case class ConcertWithSetlist(concert: Concert, setlist: Seq[Track])

class Concerts(tag: Tag) extends Table[Concert](tag, "concerts") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def date = column[String]("date")
  def name = column[String]("name")

  def * = (id.?, date, name) <> (
    { case (id, dateStr, name) => Concert(id, LocalDate.parse(dateStr), name) },
    { concert: Concert => Some((concert.id, concert.date.toString, concert.name)) }
  )
}

class Tracks(tag: Tag) extends Table[Track](tag, "tracks") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def duration = column[Int]("duration")
  def concertId = column[Int]("concert_id")
  def * = (id.?, name, duration, concertId) <> (Track.tupled, Track.unapply)
  def concert = foreignKey("concert_fk", concertId, TableQuery[Concerts])(_.id)
}

/*
Example of data:

POST http://localhost:9000/concerts

{
  "date": "2024-09-30",
  "name": "Rock Festival 2024"
}

POST http://localhost:9000/concerts/1/tracks

{
  "name": "Stairway to Heaven",
  "duration": 482
}
 */