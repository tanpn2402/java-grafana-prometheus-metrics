package dev.tanpn.mbeans;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.micrometer.backends.BackendRegistries;

public class ApiServer implements ApiServerMBean {

  private CollectorRegistry getCollectorRegistry() {
    MeterRegistry registry = BackendRegistries.getDefaultNow();
    if (registry instanceof PrometheusMeterRegistry) {
      CollectorRegistry prometheus = ((PrometheusMeterRegistry) registry).getPrometheusRegistry();
      return prometheus;
    }
    return null;
  }
  
  private final Map<String, Object> dataMap = new HashMap<>();

  public ApiServer() {
    Runnable runnable = new Runnable() {
      public void run() {
        MeterRegistry registry = BackendRegistries.getDefaultNow();
        if (registry instanceof PrometheusMeterRegistry) {
          Enumeration<MetricFamilySamples> lvMetrics = ((PrometheusMeterRegistry) registry).getPrometheusRegistry().metricFamilySamples();

          while (lvMetrics.hasMoreElements()) {
            MetricFamilySamples metric = lvMetrics.nextElement();
            List<Sample> samples = metric.samples;
            JsonArray lvSamples = new JsonArray();

            System.out.println("Name: " + metric.name);

            samples.forEach(sam ->{
              lvSamples.add(new JsonObject()
              .put("name", sam.name)
              .put("value", sam.value)
              .put("labelNames", sam.labelNames)
              .put("labelValues", sam.labelValues));
            });

            dataMap.put(metric.name, lvSamples);
          }
        }
      }
    };
    Executors.newSingleThreadScheduledExecutor()
        .scheduleAtFixedRate(runnable, 0, 5, TimeUnit.SECONDS);
  }

  @Override
  public Long getVerticleDeployed() {
    CollectorRegistry registry = this.getCollectorRegistry();
    if (registry == null)
      return null;
    Double value = registry.getSampleValue("vertx_verticle_deployed");
    return value == null ? 0 : value.longValue();
  }

  @Override
  public Long getEventBusHandlers() {
    CollectorRegistry registry = this.getCollectorRegistry();
    if (registry == null)
      return null;
    Double value = registry.getSampleValue("vertx_eventbus_handlers");
    return value == null ? 0 : value.longValue();
  }

  @Override
  public Map<String, Object> getRequest() {
    CollectorRegistry registry = this.getCollectorRegistry();
    if (registry == null)
      return null;
    
    Set<String> lvFilters = new HashSet<>();
    lvFilters.add("vertx_http_server_requests");
    lvFilters.add("vertx_http_server_bytesReceived_sum");
    lvFilters.add("vertx_http_server_connections");
    lvFilters.add("vertx_http_server_requestCount_total");
    
    Enumeration<MetricFamilySamples> lvMetrics = registry.filteredMetricFamilySamples(lvFilters);
    System.out.println("metric :" + String.valueOf(lvMetrics) +", " + lvMetrics.hasMoreElements());

    if (lvMetrics != null) {
      while (lvMetrics.hasMoreElements()) {
        MetricFamilySamples metrics = lvMetrics.nextElement();

        System.out.println(String.valueOf(metrics.name));

      }
    }

    return null;
  }
  
  @Override
  public Map<String, Object> getBytesReceived() {
    Map<String, Object> dataMap = new HashMap<>();

    Object data = this.dataMap.get("vertx_http_server_requests");
    System.out.println("data: " + String.valueOf(data));

    return dataMap;
  }

  @Override
  public Map<String, Object> getRequestCount() {
    Map<String, Object> dataMap = new HashMap<>();

    Object data = this.dataMap.get("vertx_http_server_requestCount_total");
    System.out.println("data: " + String.valueOf(data));

    return dataMap;
  }

  @Override
  public Map<String, Object> getResponseTimeSeconds() {
    Map<String, Object> dataMap = new HashMap<>();

    Object data = this.dataMap.get("vertx_http_server_requestCount_total");
    System.out.println("data: " + String.valueOf(data));

    return dataMap;
  }
}