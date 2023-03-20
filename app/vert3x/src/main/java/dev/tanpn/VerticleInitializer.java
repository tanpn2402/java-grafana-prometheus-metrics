package dev.tanpn;

import java.util.logging.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;

public class VerticleInitializer extends AbstractVerticle {
	private static final Logger LOGGER = Logger.getLogger("VerticleInitializer");
	
	@Override
	public void start() throws Exception {
		deployVerticle(HttpVerticle.class);
		deployVerticle(ConsumerVerticle.class);
	}
	
	private void deployVerticle(Class<?> pClass) {
		vertx.deployVerticle(pClass.getName(), new DeploymentOptions().setConfig(config()), res -> {
			LOGGER.info(String.format("Deploy verticle [%s]: %s", pClass.getName(), res.succeeded() ? "success": "fail"));
			if (!res.succeeded()) {
				LOGGER.warning(res.cause().getMessage());
			}
		});
	}
}
