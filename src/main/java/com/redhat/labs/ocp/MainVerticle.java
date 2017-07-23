package com.redhat.labs.ocp;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.shell.ShellServer;
import io.vertx.ext.shell.term.HttpTermOptions;
import io.vertx.ext.shell.term.TermServer;
import io.vertx.ext.web.Router;

import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

public class MainVerticle extends AbstractVerticle {

    private static final Logger LOG = Logger.getLogger(MainVerticle.class.getName());

    private static final String HOCON = "hocon";
    private static final String CONFIGMAP = "configmap";
    private static final String NAMESPACE = "ambassadors-service";
    private static final String SECRET = "secret";
    private static final String OPTIONAL = "optional";

    @Override
    public void start(Future startFuture) {
        ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions();
        if (System.getenv().containsKey("OPENSHIFT_BUILD_NAMESPACE")) {
            ConfigStoreOptions kubeConfig = new ConfigStoreOptions()
                    .setType(CONFIGMAP)
                    .setFormat(HOCON)
                    .setConfig(new JsonObject()
                            .put(OPTIONAL, true)
                            .put("name", "ambassador-service"));
            configRetrieverOptions
                .addStore(kubeConfig);        // Values here will override identical keys from above
        }

        ConfigRetriever retriever = ConfigRetriever.create(vertx, configRetrieverOptions);
        retriever.getConfig(res -> {
            if (res.succeeded()) {
                LOG.log(WARNING, res.result().encodePrettily());
                Router router = Router.router(vertx);

                ShellServer server = ShellServer.create(vertx);

                Router shellRouter = Router.router(vertx);
                shellRouter.route("/").handler(ctx -> {
                    ctx.response().putHeader("Location", "/shell.html");
                    ctx.next();
                });
                router.mountSubRouter("/shell", shellRouter);
                TermServer httpTermServer = TermServer.createHttpTermServer(vertx, router);

                server.registerTermServer(httpTermServer);

                vertx.createHttpServer().requestHandler(router::accept).listen(8080, http -> {
                    if (http.succeeded()) {
                        startFuture.complete();
                    } else {
                        startFuture.fail(http.cause());
                    }
                });
            } else {
                LOG.log(SEVERE, "Error loading config");
                startFuture.fail(res.cause());
            }
        });
    }

}
