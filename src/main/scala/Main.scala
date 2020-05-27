import java.util.concurrent.Executors
import cats.effect._
import cats.implicits._
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.{HttpRoutes, MediaType, UrlForm}
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.dsl.io._
import io.circe._
import org.http4s.circe._
import scala.concurrent.ExecutionContext

object Main extends IOApp {

  val blockingEC = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(5))
  val httpClient: Client[IO] = JavaNetClientBuilder[IO](Blocker.liftExecutionContext(blockingEC)).create

  val app = HttpRoutes.of[IO] {
    case GET -> Root / "crypto" =>
      Ok(
        """<form method="post">
          |<span>Enter crypto codes:</span>
          |<input type="text" id="names" name="names">
          |<input type="submit" value="Submit">
          |</form>""".stripMargin, `Content-Type`(MediaType.text.html))
    case req @ POST -> Root / "crypto" =>
      req.decode[UrlForm] { m =>
        val names = m.values("names").headOption.get.split(',').map(_.trim).toList
        val res = names.map(name => api + name).traverse(httpClient.expect[Json]).unsafeRunSync
        val values: List[Double] = res.flatMap(_.\\("USD")).map(_.as[Double].getOrElse(0))
        val cryptos = names.zip(values).sortBy(_._2).reverse
        val sum: Double = values.sum
        val mean = sum / values.length
        val html =
          s"""<span>Mean: </span>$mean
             |<br><br>
             |<span>Crypto: </span>
             |<br>""".stripMargin
        val cryptosHtml = cryptos.map((x: (String, Double)) => s"<span>${x._1}: </span>${x._2} USD").mkString("<br>")
        Ok(html+cryptosHtml, `Content-Type`(MediaType.text.html))
      }
  }.orNotFound

  val api = "https://min-api.cryptocompare.com/data/price?tsyms=USD&fsym="

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(app)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}