package dev.tanpn;

import java.io.File;
import java.util.EnumSet;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.micrometer.Label;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.micrometer.backends.BackendRegistries;

public class MyLauncher extends Launcher {
	private final static String DEF_CONFIGPATH = "config/app.json";

	@Override
	public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
		final String configPath = System.getProperty("configPath", DEF_CONFIGPATH);
		File conf = new File(configPath);
		deploymentOptions.setConfig(this.getConfiguration(conf));
		super.beforeDeployingVerticle(deploymentOptions);
	}
	
	@Override
	public void beforeStartingVertx(VertxOptions options) {
		if (options == null) {
			options = new VertxOptions();
		}
		MicrometerMetricsOptions metricsOptions = new MicrometerMetricsOptions()
			      .setEnabled(true)
			      .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
				  .setLabels(EnumSet.of(Label.REMOTE, Label.LOCAL, Label.HTTP_CODE, Label.HTTP_PATH, Label.HTTP_METHOD));
		options.setMetricsOptions(metricsOptions);
		super.beforeStartingVertx(options);
	}

	@Override
	public void afterStartingVertx(Vertx vertx) {
		super.afterStartingVertx(vertx);
	
		/*
	     After the Vert.x instance has been created,
	     we can configure the metrics registry to enable histogram buckets
	     for percentile approximations.
	     
		PrometheusMeterRegistry registry = (PrometheusMeterRegistry) BackendRegistries.getDefaultNow();
		registry.config().meterFilter(new MeterFilter() {
			@Override
			public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
				return DistributionStatisticConfig.builder().percentilesHistogram(true).build().merge(config);
			}
		});
		*/
	}
	
	private JsonObject getConfiguration(File config) {
		JsonObject conf = new JsonObject();
		if (config.isFile()) {
			System.out.println("Reading config file: " + config.getAbsolutePath());
			try (Scanner scanner = new Scanner(config).useDelimiter("\\A")) {
				String sconf = scanner.next();
				conf = new JsonObject(sconf);
				ObjectMapper mapper = new ObjectMapper();
				System.out.println("App config: ");
				System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(conf.getMap()));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Config file not found: " + config.getAbsolutePath());
		}
		return conf;
	}

}
