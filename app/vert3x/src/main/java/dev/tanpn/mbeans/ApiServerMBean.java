package dev.tanpn.mbeans;

import java.util.Map;

public interface ApiServerMBean {

  // vertx_http_server_bytesReceived (_sum || _count)
  Map<String, Object> getBytesReceived();

  // vertx_http_server_bytesSent (_sum || _count)
  // Map<String, Object> getBytesSent();

  // vertx_verticle_deployed
  Long getVerticleDeployed();

  // vertx_eventbus_handlers
  Long getEventBusHandlers();

  // vertx_http_server_requestCount_total           Number of processed requests
  Map<String, Object> getRequestCount();

  // vertx_http_server_requests                     Number of requests being processed
  Map<String, Object> getRequest();

  // vertx_http_server_responseTime_seconds_sum     Total request processing time
  // vertx_http_server_responseTime_seconds_count   Count response
  Map<String, Object> getResponseTimeSeconds();

  // vertx_http_server_connections
  // Map<String, Object> getConnections();
}