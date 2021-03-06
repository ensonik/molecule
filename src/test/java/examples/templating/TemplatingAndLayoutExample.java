package examples.templating;

import com.vtence.molecule.WebServer;
import com.vtence.molecule.middlewares.Layout;
import com.vtence.molecule.routing.DynamicRoutes;
import com.vtence.molecule.templating.JMustacheRenderer;
import com.vtence.molecule.templating.Template;
import com.vtence.molecule.templating.Templates;

import java.io.IOException;
import java.util.Map;

import static com.vtence.molecule.testing.ResourceLocator.locateOnClasspath;

public class TemplatingAndLayoutExample {

    public void run(WebServer server) throws IOException {
        // We use Mustache templates with an .html extension
        Templates templates = new Templates(
                new JMustacheRenderer().fromDir(locateOnClasspath("examples/templates")).extension("html"));
        final Template<Map<String, String>> layout = templates.named("layout");
        final Template<User> greeting = templates.named("greeting");

        // Apply a common layout to all rendered pages
        server.filter("/", Layout.html(layout))
              .start(new DynamicRoutes() {{
                  get("/hello").to((request, response) -> {
                      response.contentType("text/html; charset=utf-8");
                      String name = request.parameter("name") != null ? request.parameter("name") : "World";
                      // Mustache can use any object or a Map as a rendering context
                      response.done(greeting.render(new User(name)));
                  });
              }});
    }

    private static class User {
        public final String name;

        public User(String name) {
            this.name = name;
        }
    }

    public static void main(String[] args) throws IOException {
        TemplatingAndLayoutExample example = new TemplatingAndLayoutExample();
        // Run the default web server
        WebServer webServer = WebServer.create();
        example.run(webServer);
        System.out.println("Access at " + webServer.uri() + "/hello?name=Gandalf");
    }
}