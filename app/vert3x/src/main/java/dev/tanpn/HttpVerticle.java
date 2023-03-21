package dev.tanpn;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.exporter.common.TextFormat;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.micrometer.PrometheusScrapingHandler;
import io.vertx.micrometer.backends.BackendRegistries;

public class HttpVerticle extends AbstractVerticle {
	private static final Logger LOGGER = Logger.getLogger("HttpVerticle");
	
	@SuppressWarnings("deprecation")
	@Override
	public void start() throws Exception {
		super.start();

		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());
		router.route("/metrics").handler(PrometheusScrapingHandler.create());
		router.route("/pro-metrics").handler(this::handleProMetrics);
		router.route("/pro-metrics/:name").handler(this::handleProMetrics);
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
	
	
	private void handleProMetrics(RoutingContext rc) {
		final String pName = rc.pathParam("name");
		// name = vertx_http_server_requestCount_total
		
		JsonObject lvResponseObj = new JsonObject();
		JsonArray lvBodyResp = new JsonArray();
		lvResponseObj.put("code", 200);
		lvResponseObj.put("data", lvBodyResp);
		
		MeterRegistry registry = BackendRegistries.getDefaultNow();
		if (registry instanceof PrometheusMeterRegistry) {
			PrometheusMeterRegistry prometheusMeterRegistry = (PrometheusMeterRegistry) registry;
			Enumeration<MetricFamilySamples> lvMetrics = null;
			if (pName == null) {
				lvMetrics = prometheusMeterRegistry.getPrometheusRegistry().metricFamilySamples();
			}
			else {
				Set<String> lvFilterName = new HashSet<>();
				lvFilterName.add(pName);
				lvMetrics = prometheusMeterRegistry.getPrometheusRegistry().filteredMetricFamilySamples(lvFilterName);
			}
			// prometheusMeterRegistry.getPrometheusRegistry().filteredMetricFamilySamples(lvFilterName);
			// prometheusMeterRegistry.getPrometheusRegistry().metricFamilySamples();
			
			while (lvMetrics.hasMoreElements()) {
				MetricFamilySamples metric = lvMetrics.nextElement();
				List<Sample> samples = metric.samples;
				JsonArray lvSamples = new JsonArray();
				samples.forEach(sam -> {
					lvSamples.add(new JsonObject()
							.put("name", sam.name)
							.put("value", sam.value)
							.put("labelNames", sam.labelNames)
							.put("labelValues", sam.labelValues));
					lvBodyResp.add(new JsonObject().put("name", metric.name).put("samples", lvSamples));
				});
			}
			
			// final String scrapeValue = prometheusMeterRegistry.scrape();
			
			rc.response()
				.putHeader(HttpHeaders.CONTENT_TYPE, TextFormat.CONTENT_TYPE_004)
				.end(Json.encode(lvResponseObj));
		}
		else {
			String statusMessage = "Invalid registry: " + (registry != null ? registry.getClass().getName() : null);
			rc.response()
				.setStatusCode(500).setStatusMessage(statusMessage)
				.end();
		}
	}
}
