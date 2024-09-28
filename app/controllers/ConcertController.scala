package controllers

import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json._
import play.api.mvc._
import slick.jdbc.JdbcProfile

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConcertController @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider,
  cc: ControllerComponents
)(implicit ec: ExecutionContext) extends AbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val concerts = TableQuery[Concerts]
  private val tracks = TableQuery[Tracks]

  private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

  implicit val localDateReads: Reads[LocalDate] =
    Reads.localDateReads("yyyy-MM-dd")

  implicit val localDateWrites: Writes[LocalDate] =
    Writes.temporalWrites[LocalDate, DateTimeFormatter](dateFormatter)

  implicit val concertReads: Reads[Concert] = (
    (JsPath \ "id").readNullable[Int] and
      (JsPath \ "date").read[LocalDate] and
      (JsPath \ "name").read[String]
    )(Concert.apply _)

  implicit val concertWrites: Writes[Concert] = (
    (JsPath \ "id").writeNullable[Int] and
      (JsPath \ "date").write[LocalDate] and
      (JsPath \ "name").write[String]
    )(unlift(Concert.unapply))

  implicit val concertFormat: Format[Concert] = Format(concertReads, concertWrites)

  implicit val trackInputReads: Reads[TrackInput] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "duration").read[Int]
    )(TrackInput.apply _)

  implicit val trackWrites: Writes[Track] = (
    (JsPath \ "id").writeNullable[Int] and
      (JsPath \ "name").write[String] and
      (JsPath \ "duration").write[Int] and
      (JsPath \ "concertId").write[Int]
    )(unlift(Track.unapply))

  implicit val trackFormat: Format[Track] = Json.format[Track]

  def listConcerts = Action.async { implicit request =>
    db.run(concerts.result).map { concertList =>
      Ok(Json.toJson(concertList))
    }
  }

  def addConcert = Action.async(parse.json) { implicit request =>
    request.body.validate[Concert].fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
      concert => {
        db.run(concerts += concert).map { _ =>
          Created(Json.obj("message" -> "Concert created successfully"))
        }
      }
    )
  }

  def getSetlist(concertId: Int) = Action.async { implicit request =>
    db.run(tracks.filter(_.concertId === concertId).result).map { trackList =>
      Ok(Json.toJson(trackList))
    }
  }

  def addTrack(concertId: Int) = Action.async(parse.json) { implicit request =>
    request.body.validate[TrackInput].fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
      trackInput => {
        val newTrack = Track(None, trackInput.name, trackInput.duration, concertId)
        db.run(tracks += newTrack).map { _ =>
          Created(Json.obj("message" -> "Track added successfully"))
        }
      }
    )
  }

  def showConcerts = Action.async { implicit request =>
    val query = for {
      (concert, tracks) <- concerts joinLeft tracks on (_.id === _.concertId)
    } yield (concert, tracks)

    db.run(query.result).map { results =>
      val concertsWithSetlists = results.groupBy(_._1).map { case (concert, groupedResults) =>
        val setlist = groupedResults.flatMap(_._2)
        ConcertWithSetlist(concert, setlist)
      }.toSeq

      Ok(views.html.concerts(concertsWithSetlists))
    }
  }
}