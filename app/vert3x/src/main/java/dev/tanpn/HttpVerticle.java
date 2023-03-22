package dev.tanpn;

import java.util.*;
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
		router.route("/pro-metrics/:valueFilter").handler(this::handleProMetrics);
		router.post("/greeting").handler(rtx -> {
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
		final String valueFilter = rc.pathParam("valueFilter");

		JsonObject lvResponseObj = new JsonObject();
		JsonArray lvBodyResp = new JsonArray();
		lvResponseObj.put("code", 200);
		lvResponseObj.put("data", lvBodyResp);

		MeterRegistry registry = BackendRegistries.getDefaultNow();
		if (registry instanceof PrometheusMeterRegistry) {
			Enumeration<MetricFamilySamples> lvMetrics = ((PrometheusMeterRegistry) registry).getPrometheusRegistry().metricFamilySamples();

			while (lvMetrics.hasMoreElements()) {
				MetricFamilySamples metric = lvMetrics.nextElement();
				List<Sample> samples = metric.samples;
				JsonArray lvSamples = new JsonArray();

				switch (valueFilter) {
					case CommonValue.VERTX_VERTICLE_DEPLOYED:
						if (metric.name.equals(CommonValue.VERTX_VERTICLE_DEPLOYED)) {
							filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.VERTX_VERTICLE_DEPLOYED), null, null);
						}
						break;
					case CommonValue.VERTX_HTTP_SERVER_REQUEST:
						if (metric.name.equals(CommonValue.VERTX_HTTP_SERVER_REQUEST)) {
							filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.VERTX_HTTP_SERVER_REQUEST), null, null);
						}
						break;
					case CommonValue.VERTX_HTTP_SERVER_RESPONSE_TIME_SECONDS:
						if (metric.name.equals(CommonValue.VERTX_HTTP_SERVER_RESPONSE_TIME_SECONDS)) {
							filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.VERTX_HTTP_SERVER_RESPONSE_TIME_SECONDS,
									CommonValue.VERTX_HTTP_SERVER_RESPONSE_TIME_SECONDS_COUNT, CommonValue.VERTX_HTTP_SERVER_RESPONSE_TIME_SECONDS_SUM), null, null);
						}
						break;
					case CommonValue.VERTX_HTTP_SERVER_RESPONSE_TIME_SECONDS_MAX:
						if (metric.name.equals(CommonValue.VERTX_HTTP_SERVER_RESPONSE_TIME_SECONDS_MAX)) {
							filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.VERTX_HTTP_SERVER_RESPONSE_TIME_SECONDS_MAX), null, null);
						}
						break;
					case CommonValue.VERTX_HTTP_SERVER_CONNECTIONS:
						if (metric.name.equals(CommonValue.VERTX_HTTP_SERVER_CONNECTIONS)) {
							filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.VERTX_HTTP_SERVER_CONNECTIONS), null, null);
						}
						break;
					case CommonValue.VERTX_HTTP_SERVER_BYTES_RECEIVED_MAX:
						if (metric.name.equals(CommonValue.VERTX_HTTP_SERVER_BYTES_RECEIVED_MAX)) {
							filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.VERTX_HTTP_SERVER_BYTES_RECEIVED_MAX), null, null);
						}
						break;
					case CommonValue.VERTX_HTTP_SERVER_BYTES_RECEIVED:
						if (metric.name.equals(CommonValue.VERTX_HTTP_SERVER_BYTES_RECEIVED)) {
							filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.VERTX_HTTP_SERVER_BYTES_RECEIVED), null, null);
						}
						break;
					case CommonValue.VERTX_HTTP_SERVER_REQUEST_COUNT_TOTAL:
						if (metric.name.equals(CommonValue.VERTX_HTTP_SERVER_REQUEST_COUNT_TOTAL)) {
							filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.VERTX_HTTP_SERVER_REQUEST_COUNT_TOTAL), null, null);
						}
						break;
					case CommonValue.VERTX_HTTP_SERVER_BYTES_SENT:
						if (metric.name.equals(CommonValue.VERTX_HTTP_SERVER_BYTES_SENT)) {
							filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.VERTX_HTTP_SERVER_BYTES_SENT,
									CommonValue.VERTX_HTTP_SERVER_BYTES_SENT_COUNT, CommonValue.VERTX_HTTP_SERVER_BYTES_SENT_SUM), null, null);
						}
						break;
					case CommonValue.VERTX_EVENT_BUS_PROCESSING_TIME_SECONDS:
						if (metric.name.equals(CommonValue.VERTX_EVENT_BUS_PROCESSING_TIME_SECONDS)) {
							filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.VERTX_EVENT_BUS_PROCESSING_TIME_SECONDS), null, null);
						}
						break;
					case CommonValue.VERTX_EVENT_BUS_RECEIVED_TOTAL:
						if (metric.name.equals(CommonValue.VERTX_EVENT_BUS_RECEIVED_TOTAL)) {
							filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.VERTX_EVENT_BUS_RECEIVED_TOTAL), null, null);
						}
						break;
					case CommonValue.VERTX_EVENT_BUS_PENDING:
						if (metric.name.equals(CommonValue.VERTX_EVENT_BUS_PENDING)) {
							filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.VERTX_EVENT_BUS_PENDING), null, null);
						}
						break;
					case CommonValue.VERTX_EVENT_BUS_HANDLERS:
						if (metric.name.equals(CommonValue.VERTX_EVENT_BUS_HANDLERS)) {
							filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.VERTX_EVENT_BUS_HANDLERS), null, null);
						}
						break;
					case CommonValue.VERTX_EVENT_BUS_DELIVERED_TOTAL:
						if (metric.name.equals(CommonValue.VERTX_EVENT_BUS_DELIVERED_TOTAL)) {
							filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.VERTX_EVENT_BUS_DELIVERED_TOTAL), null, null);
						}
						break;
					case CommonValue.VERTX_EVENT_BUS_SENT_TOTAL:
						if (metric.name.equals(CommonValue.VERTX_HTTP_SERVER_REQUEST)) {
							filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.VERTX_EVENT_BUS_SENT_TOTAL), null, null);
						}
						break;
					case CommonValue.GET:
						if (metric.name.equals(CommonValue.VERTX_HTTP_SERVER_REQUEST_COUNT_TOTAL)) {
							filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.VERTX_HTTP_SERVER_REQUEST_COUNT_TOTAL), CommonValue.GET, null);
						}
						break;
					case CommonValue.POST:
						if (metric.name.equals(CommonValue.VERTX_HTTP_SERVER_REQUEST_COUNT_TOTAL)) {
							filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.VERTX_HTTP_SERVER_REQUEST_COUNT_TOTAL), CommonValue.POST, null);
						}
						break;
					case CommonValue.CODE_200:
						if (metric.name.equals(CommonValue.VERTX_HTTP_SERVER_REQUEST_COUNT_TOTAL)) {
							filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.VERTX_HTTP_SERVER_REQUEST_COUNT_TOTAL), null, CommonValue.CODE_200);
						}
						break;
					case CommonValue.CODE_404:
						if (metric.name.equals(CommonValue.VERTX_HTTP_SERVER_REQUEST_COUNT_TOTAL)) {
							filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.VERTX_HTTP_SERVER_REQUEST_COUNT_TOTAL), null, CommonValue.CODE_404);
						}
						break;
					default:
						filterData(samples, lvBodyResp, lvSamples, metric.name, Arrays.asList(CommonValue.ALL), null, null);
						break;
				}
			}

			rc.response()
					.putHeader(HttpHeaders.CONTENT_TYPE, TextFormat.CONTENT_TYPE_004)
					.end(Json.encode(lvResponseObj));
		} else {
			String statusMessage = "Invalid registry: " + (registry != null ? registry.getClass().getName() : null);
			rc.response()
					.setStatusCode(500).setStatusMessage(statusMessage)
					.end();
		}
	}

	private JsonArray filterData(List<Sample> samples, JsonArray lvBodyResp, JsonArray lvSamples, String metricName,
								  List<String> valueFilter, String method, String code) {
		samples.forEach(sam -> {
			if (valueFilter.contains(metricName)) {
				if (Objects.nonNull(method) && sam.labelValues.get(2).contains(method)) {
					addSampleData(lvSamples, lvBodyResp, sam, metricName);
				} else if (Objects.nonNull(code) && sam.labelValues.get(0).contains(code)) {
					addSampleData(lvSamples, lvBodyResp, sam, metricName);
				} else if (Objects.isNull(method) && Objects.isNull(code)) {
					addSampleData(lvSamples, lvBodyResp, sam, metricName);
				}
			} else if (valueFilter.contains(CommonValue.ALL)) {
				addSampleData(lvSamples, lvBodyResp, sam, metricName);
			}
		});

		lvBodyResp.add(new JsonObject().put("name", metricName).put("samples", lvSamples));
		return lvBodyResp;
	}

	private JsonArray addSampleData(JsonArray lvSamples, JsonArray lvBodyResp, Sample sam, String metricName) {
		return lvSamples.add(new JsonObject()
				.put("name", sam.name)
				.put("value", sam.value)
				.put("labelNames", sam.labelNames)
				.put("labelValues", sam.labelValues));
	}

	private class CommonValue {
		private static final String VERTX_VERTICLE_DEPLOYED = "vertx_verticle_deployed";
		// HTTP SERVER
		private static final String VERTX_HTTP_SERVER_REQUEST = "vertx_http_server_requests";
		private static final String VERTX_HTTP_SERVER_RESPONSE_TIME_SECONDS = "vertx_http_server_responseTime_seconds";
		private static final String VERTX_HTTP_SERVER_RESPONSE_TIME_SECONDS_COUNT = "vertx_http_server_responseTime_seconds_count";
		private static final String VERTX_HTTP_SERVER_RESPONSE_TIME_SECONDS_SUM = "vertx_http_server_responseTime_seconds_sum";
		private static final String VERTX_HTTP_SERVER_RESPONSE_TIME_SECONDS_MAX = "vertx_http_server_responseTime_seconds_max";
		private static final String VERTX_HTTP_SERVER_CONNECTIONS = "vertx_http_server_connections";
		private static final String VERTX_HTTP_SERVER_BYTES_RECEIVED_MAX = "vertx_http_server_bytesReceived_max";
		private static final String VERTX_HTTP_SERVER_BYTES_RECEIVED = "vertx_http_server_bytesReceived";
		private static final String VERTX_HTTP_SERVER_REQUEST_COUNT_TOTAL = "vertx_http_server_requestCount_total";
		private static final String VERTX_HTTP_SERVER_BYTES_SENT = "vertx_http_server_bytesSent";
		private static final String VERTX_HTTP_SERVER_BYTES_SENT_COUNT = "vertx_http_server_bytesSent_count";
		private static final String VERTX_HTTP_SERVER_BYTES_SENT_SUM = "vertx_http_server_bytesSent_sum";
		// EVENT BUS
		private static final String VERTX_EVENT_BUS_PROCESSING_TIME_SECONDS = "vertx_eventbus_processingTime_seconds";
		private static final String VERTX_EVENT_BUS_RECEIVED_TOTAL = "vertx_eventbus_received_total";
		private static final String VERTX_EVENT_BUS_PENDING = "vertx_eventbus_pending";
		private static final String VERTX_EVENT_BUS_HANDLERS = "vertx_eventbus_handlers";
		private static final String VERTX_EVENT_BUS_DELIVERED_TOTAL = "vertx_eventbus_delivered_total";
		private static final String VERTX_EVENT_BUS_SENT_TOTAL = "vertx_eventbus_sent_total";
		private static final String ALL = "all";

		// METHOD
		private static final String GET = "GET";
		private static final String POST = "POST";
		// CODE
		private static final String CODE_200 = "200";
		private static final String CODE_404 = "404";
	}
}
