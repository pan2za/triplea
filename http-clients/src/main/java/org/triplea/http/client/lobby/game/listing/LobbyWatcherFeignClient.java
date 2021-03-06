package org.triplea.http.client.lobby.game.listing;

import feign.HeaderMap;
import feign.Headers;
import feign.RequestLine;
import java.util.Map;
import org.triplea.http.client.HttpConstants;

@Headers({HttpConstants.CONTENT_TYPE_JSON, HttpConstants.ACCEPT_JSON})
interface LobbyWatcherFeignClient {
  @RequestLine("POST " + LobbyWatcherClient.POST_GAME_PATH)
  String postGame(@HeaderMap Map<String, Object> headers, LobbyGame lobbyGame);

  @RequestLine("POST " + LobbyWatcherClient.UPDATE_GAME_PATH)
  void updateGame(@HeaderMap Map<String, Object> headers, UpdateGameRequest updateGameRequest);

  @RequestLine("POST " + LobbyWatcherClient.REMOVE_GAME_PATH)
  void removeGame(@HeaderMap Map<String, Object> headers, String gameId);

  @RequestLine("POST " + LobbyWatcherClient.KEEP_ALIVE_PATH)
  boolean sendKeepAlive(@HeaderMap Map<String, Object> headers, String gameId);
}
