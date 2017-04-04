package com.gameiom.real.sports;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.IOException;
import java.util.logging.LogManager;

/**
 * @author Myhajlo.Rozputnyj
 */
public class MainVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Future<Void> fut) throws Exception {
        try {
            LogManager.getLogManager().readConfiguration(MainVerticle.class.getResourceAsStream("/vertx-logging.properties"));
        } catch (IOException e) {
            LOGGER.info("Could not setup logger configuration: " + e.toString());
        }
        vertx.deployVerticle(new RealSport(), new DeploymentOptions().setConfig(config()).setWorker(true), result -> {
            if (result.succeeded()) {
                LOGGER.info(RealSport.class.getName() + " deploy succeeded");
            } else {
                LOGGER.info(RealSport.class.getName() + " deploy error " + result.cause());
            }
        });
        fut.complete();
    }
}
