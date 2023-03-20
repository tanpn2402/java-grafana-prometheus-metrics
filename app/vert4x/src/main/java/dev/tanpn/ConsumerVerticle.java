package dev.tanpn;

import java.util.logging.Logger;

import io.vertx.core.AbstractVerticle;

public class ConsumerVerticle extends AbstractVerticle {
	private static final Logger LOGGER = Logger.getLogger("ConsumerVerticle");

	
	
	@Override
	public void start() throws Exception {
		super.start();
		
		vertx.eventBus().consumer("consumer_key", handler -> {
			LOGGER.info("Receive message: " + handler.body().toString());
			
			
			handler.reply(new String("Receive msg"));
		});
		
		LOGGER.info(String.format("%s started", this.getClass().getSimpleName()));
	}
}
