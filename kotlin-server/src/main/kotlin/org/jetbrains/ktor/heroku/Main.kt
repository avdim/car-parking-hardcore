package org.jetbrains.ktor.heroku

import com.zaxxer.hikari.*
import freemarker.cache.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.content.*
import org.jetbrains.ktor.features.*
import org.jetbrains.ktor.freemarker.*
import org.jetbrains.ktor.host.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.netty.*
import org.jetbrains.ktor.routing.*
import java.util.*
import kotlinx.html.*
import kotlinx.html.dom.*
import kotlinx.html.stream.appendHTML
import org.jetbrains.ktor.jetty.Jetty
import org.jetbrains.ktor.logging.logInfo
import org.jetbrains.ktor.request.host
import org.jetbrains.ktor.request.uri
import org.jetbrains.ktor.request.userAgent
import org.jetbrains.ktor.response.contentType
import org.jetbrains.ktor.response.header
import org.jetbrains.ktor.response.respondText
import java.io.File

val hikariConfig = HikariConfig().apply {
    jdbcUrl = System.getenv("JDBC_DATABASE_URL")
}

val dataSource = if (hikariConfig.jdbcUrl != null)
    HikariDataSource(hikariConfig)
else
    HikariDataSource()

val html_utf8 = ContentType.Text.Html.withCharset(Charsets.UTF_8)

var counter = 0;

fun Application.module() {
    intercept(ApplicationCallPipeline.Call) {
        if (call.request.uri == "/intercept")
            call.respondText("Test intercept")
    }
    install(DefaultHeaders)
    install(ConditionalHeaders)
    install(PartialContentSupport)

    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(environment.classLoader, "templates")
    }

    install(StatusPages) {
        exception<Exception> { exception ->
            call.respond(FreeMarkerContent("error.ftl", exception, "", html_utf8))
        }
    }

    install(Routing) {
        serveClasspathResources("public")

        get("/vk.html") {
            //logDb(call, call.request.queryParameters.get("from")) todo deeplink # parseUriParams
            logDb(call, "vk")
            printHtml(call)
        }

        get("/mm.html") {
            logDb(call, "mm")
            printHtml(call)
        }

        get("/ok.html") {
            logDb(call, "ok")
            printHtml(call)
        }

        get("/") {
            logDb(call, call.request.queryParameters.get("from"))
            printHtml(call)
        }

//        get("/test.html") {
//            printHtml(call)
//        }

        get("/db") {
            var result:String = ""
            val model = HashMap<String, Any>()
            dataSource.connection.use { connection ->
                val rs = connection.createStatement().run {
                    executeQuery("SELECT * FROM loads")
                }
                while (rs.next()) {
                    result += rs.getString("time") + "   "
                    result += rs.getString("frm") + "   "
                    result += rs.getString("host") + "   "
                    result += rs.getString("agent") + "\r\n"
                }
            }
            call.respond(result);
        }
    }
}

suspend fun  printHtml(call: ApplicationCall) {
    call.response.header("Content-Type", "text/html; charset=UTF-8")
    call.response.header("my_header", "my_value")
    call.response.status(HttpStatusCode.OK)
    val absolutePath = File(".").absolutePath
    val toString = File(".").listFiles().joinToString()
    val htmlContent = File("src/main/resources/public/index.html").readText()
    call.respond(htmlContent);
}

fun logDb(call: ApplicationCall, from: String?) {
    dataSource.connection.use { connection ->
        connection.createStatement().run {
//            executeUpdate("DROP TABLE IF EXISTS loads")
            executeUpdate("CREATE TABLE IF NOT EXISTS loads (time timestamp, frm text, host text, agent text)")
            executeUpdate("INSERT INTO loads VALUES (now()," +
                    " '" +
                    from +
                    "', " +
                    "'" +
                    call.request.local.remoteHost +
                    "'," +
                    " '" +
                    call.request.userAgent() +
                    "')")
        }
    }
}

fun main(args: Array<String>) {
    var port:Int = 5000
    try{
        port = Integer.valueOf(System.getenv("PORT"))
    } catch(e:Exception) {

    }
//    embeddedServer(Netty, port, reloadPackages = listOf("heroku"), module = Application::module).start()
    embeddedServer(Jetty, port, reloadPackages = listOf("heroku"), module = Application::module).start()
//    embeddedServer(MyServer,port, reloadPackages = listOf("heroku"), module = Application::module).start()
}

object MyServer : ApplicationHostFactory<ApplicationHost> {
    override fun create(environment: ApplicationHostEnvironment): ApplicationHost {
        return MyServer.create(applicationHostEnvironment {

        })
    }

}


