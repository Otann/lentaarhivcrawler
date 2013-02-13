import com.ning.http.client.{RequestBuilder, Response}
import com.weiglewilczek.slf4s.Logging
import dispatch._
import java.io._
import java.nio.charset.Charset

object Importer extends Logging {

  // Constants
  def allPersonsUrl = url("http://web.archive.org/web/20130116162216/http://lenta.ru/lib/")
  def personUrl(link: String) = url("http://web.archive.org%s" format link)

  val PersonPattern  = """/web/\d+/http://lenta.ru/lib/(\d+)/""".r
  val NamePattern    = """(?s).*<h1>(.+)</h1>.*""".r
  val ArticlePattern = """(?s).*<td.*id="article">(.+)</td>.*""".r

  val template =
    """
      |<!DOCTYPE html>
      |<html lang="en">
      |  <head>
      |    <meta charset="utf-8">
      |    <meta name="viewport" content="width=device-width, initial-scale=1.0">
      |    <link href="bootstrap/css/bootstrap.css" rel="stylesheet"/>
      |  </head>
      |  <body>
      |    <div class="container">
      |        <div class="row-fluid">
      |          <div class="span12">
      |            ##content##
      |          </div>
      |        </div>
      |    </div>
      |  </body>
      |</html>
    """.stripMargin

  val http = Http.configure(_
    .setFollowRedirects(true)
    .setMaximumNumberOfRedirects(1000)
  )

  // Helpers
  def fetch(remote: RequestBuilder) = http(remote OK as.String)()

  def writeToFile(name: String, data: String) {
    val utf8 = Charset.forName("UTF-8")
    val fos = new OutputStreamWriter(new FileOutputStream(name), utf8)
    fos.write("<meta charset=\"UTF-8\">")
    fos.write(data)
    fos.close()
  }

  def persistPerson(link: String) {
    try {
      val data = fetch(personUrl(link))
      val ArticlePattern(article) = data

      val name = article match {
        case NamePattern(s) => s
        case _ => { val PersonPattern(id) = link; id }
      }

      val fixed = article.replaceAll("/web/", "http://web.archive.org/web/")

      writeToFile("results/%s.html" format name, template.replace("##content##", fixed))
    } catch {
      case _: Throwable => logger.debug("Exception while doing %s" format link)
    }
  }

  // Dirty work
  def main(args: Array[String]) {

    logger.info("Start fetching")

    val allPersonsData = fetch(allPersonsUrl)
    val allPersons = (PersonPattern findAllIn allPersonsData).toList
    var index = 0

    //    persistPerson("/web/20130117211042/http://lenta.ru/lib/14180652/")

    for (example <- allPersons) {
      index += 1
      logger.info("%s of %s" format (index, allPersons.size))
      persistPerson(example)
    }

    logger.info("Done!")
  }


}
