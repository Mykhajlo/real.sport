package com.gameiom.real.sports;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Map;

/**
 * @author Myhajlo.Rozputnyj
 */
public class RealSport extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(RealSport.class);

    @Override
    public void start() throws Exception {
        Map<String, String> users = new CsvReader().parseCSV();
        users.forEach((user, cur) -> {
            String env = config().getString("env_dev");
            Integer port = config().getInteger("http.port");
            vertx.createHttpClient().websocket(port, env, "/", socket -> {
                LOGGER.info("Connected to game engine. Socket ID: " + socket.textHandlerID());
                String userName = user;
                String currency = cur;
                String route = config().getString("route");
                Integer gameId = config().getInteger("gameId");
                Integer operatorId = config().getInteger("operatorId");
                Integer providerId = config().getInteger("providerId");

                vertx.createHttpClient(new HttpClientOptions().setSsl(true)).requestAbs(HttpMethod.GET, route + userName, event -> {
                    event.bodyHandler(body -> {
                        String response = body.toString();
                        JsonObject token = new JsonObject(response);
                        String sessionToken = token.getString("token");
                        // send auth message
                        JsonObject authMessage = new JsonObject()
                                .put("platform", new JsonObject()
                                        .put("gameId", gameId)
                                        .put("sessionToken", sessionToken)
                                        .put("username", userName)
                                        .put("operatorId", operatorId)
                                        .put("providerId", providerId)
                                        .put("currency", currency)
                                )
                                .put("publicState", new JsonObject()
                                        .put("state", "newRace"));
                        LOGGER.info("newRace message: " + authMessage);
                        socket.writeFinalTextFrame(authMessage.toString());
                        socket.handler(newRaceResponse -> {
                            LOGGER.info("newRace response: " + newRaceResponse.toString());
                            // newRace request and response
                            JsonObject newRace = new JsonObject(newRaceResponse.toString());
                            JsonObject publicState = new JsonObject(newRace.getValue("publicState").toString());
                            // bet request and response
                            JsonObject bet = new JsonObject()
                                    .put("platform", new JsonObject()
                                            .put("gameId", gameId)
                                            .put("sessionToken", sessionToken)
                                            .put("username", userName))
                                    .put("publicState", new JsonObject()
                                            .put("state", "bet")
                                            .put("traps", publicState.getValue("traps"))
                                            .put("bets", new JsonObject(Mock.getBets())));
                            socket.writeFinalTextFrame(bet.toString());
                            LOGGER.info("bet message: " + bet.toString());
                            socket.handler(betResponse -> {
                                LOGGER.info("bet response: " + betResponse.toString());
                                LOGGER.info("User game session is started.");
                                // bet with stakes request and response
                                JsonObject betWithStakes = new JsonObject()
                                        .put("platform", new JsonObject()
                                                .put("gameId", gameId)
                                                .put("sessionToken", sessionToken)
                                                .put("username", userName))
                                        .put("publicState", new JsonObject()
                                                .put("bets", new JsonObject(Mock.getStakes()))
                                                .put("state", "bet")
                                                .put("traps", publicState.getValue("traps")));
                                LOGGER.info("bet with stake message: " + betWithStakes.toString());
                                socket.writeFinalTextFrame(betWithStakes.toString());
                                socket.handler(betWithStakeResponse -> {
                                    LOGGER.info("bet with stake response: " + betWithStakeResponse.toString());
                                    // startRace request and response
                                    JsonObject startRace = new JsonObject()
                                            .put("platform", new JsonObject()
                                                    .put("gameId", gameId)
                                                    .put("sessionToken", sessionToken)
                                                    .put("username", userName))
                                            .put("publicState", new JsonObject()
                                                    .put("state", "startRace"));
                                    socket.writeFinalTextFrame(startRace.toString());
                                    LOGGER.info("startRace message:   " + startRace.toString());
                                    socket.handler(startRaceResponse -> {
                                        LOGGER.info("startRaceResponse response:  " + startRaceResponse.toString());
                                        // finishRace request and response
                                        JsonObject finishRace = new JsonObject()
                                                .put("platform", new JsonObject()
                                                        .put("gameId", gameId)
                                                        .put("sessionToken", sessionToken)
                                                        .put("username", userName))
                                                .put("publicState", new JsonObject()
                                                        .put("state", "finishRace"));
                                        socket.writeFinalTextFrame(finishRace.toString());
                                        LOGGER.info("finishRace message:   " + finishRace.toString());
                                        socket.handler(finishRaceResponse -> {
                                                    LOGGER.info("finishRaceResponce responce:    " + finishRaceResponse.toString());
                                                    // completeRace request and response
                                                    JsonObject completeRace = new JsonObject()
                                                            .put("platform", new JsonObject()
                                                                    .put("gameId", gameId)
                                                                    .put("sessionToken", sessionToken)
                                                                    .put("username", userName))
                                                            .put("publicState", new JsonObject()
                                                                    .put("state", "completeRace"));
                                                    socket.writeFinalTextFrame(completeRace.toString());
                                                    LOGGER.info("completeRace message:   " + completeRace.toString());
                                                    socket.handler(completeRaceResponse -> {
                                                                LOGGER.info("completeRaceResponse response:   " + completeRaceResponse.toString());
                                                            }
                                                    );

                                                }
                                        );
                                    });
                                });
                            });

                        });

                        //                                    socket.closeHandler(eventClose -> {
//                                        vertx.createHttpClient().websocket(port, env, "/", socketNew -> {
//
//                                            JsonObject finishRace = new JsonObject()
//                                                .put("platform", new JsonObject()
//                                                        .put("gameId", gameId)
//                                                        .put("sessionToken", sessionToken)
//                                                        .put("username", userName))
//                                                .put("publicState", new JsonObject()
//                                                        .put("state", "finishRace"));
//                                        socketNew.writeFinalTextFrame(finishRace.toString());
//                                        LOGGER.info("finishRace message:   " + finishRace.toString());
//                                            socketNew.handler(finishRaceResponse -> {
//                                                LOGGER.info("finishRaceResponce responce:    " + finishRaceResponse.toString());
//                                            });
//                                        });
//                                        LOGGER.info("Session ID closed");
//                                    });
//                                    socket.close();
                    });
                })
                        .putHeader("gameProvider", "1")
                        .end("");
            });
        });
    }

}

