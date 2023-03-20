package dev.tanpn;

import java.util.logging.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.micrometer.PrometheusScrapingHandler;

public class HttpVerticle extends AbstractVerticle {
	private static final Logger LOGGER = Logger.getLogger("HttpVerticle");
	
	@SuppressWarnings("deprecation")
	@Override
	public void start() throws Exception {
		super.start();

		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());
		router.route("/metrics").handler(PrometheusScrapingHandler.create());
		router.get("/greeting").handler(rtx -> {
			vertx.eventBus().<String>request("consumer_key", new String(), handler -> {
				LOGGER.info(String.format("Send message to [%s]: %s", "consumer_key", "value1"));
				
				if (handler.succeeded()) {
					rtx.response()
						.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
						.end(Json.encode(new JsonObject().put("code", 200).put("data", handler.result().body())));
				}
				else {
					rtx.response()
						.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
						.end(Json.encode(new JsonObject().put("code", 500)));
				}
			});
		});
		HttpServerOptions httpOption = new HttpServerOptions();
		HttpServer httpServer = vertx.createHttpServer(httpOption);
		final int port = config().getInteger("http.server.port", 8082);
		httpServer.requestHandler(router::accept).listen(port, handler -> {
			LOGGER.info(String.format("Start HTTP server at [%s] %s", port, handler.succeeded() ? "success": "fail"));
			if (!handler.succeeded()) {
				LOGGER.warning("Cause by: " + handler.cause().getMessage());
				vertx.close();
			}
		});
		
		LOGGER.info(String.format("%s started", this.getClass().getSimpleName()));
	}
}
