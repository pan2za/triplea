package games.strategy.engine.framework.startup.launcher;

import games.strategy.engine.data.GameData;
import games.strategy.engine.framework.ServerGame;
import games.strategy.engine.framework.message.PlayerListing;
import games.strategy.engine.framework.startup.launcher.local.PlayerCountrySelection;
import games.strategy.engine.framework.startup.mc.GameSelectorModel;
import games.strategy.engine.framework.startup.ui.PlayerType;
import games.strategy.engine.player.Player;
import games.strategy.engine.random.IRandomSource;
import games.strategy.engine.random.PlainRandomSource;
import games.strategy.net.LocalNoOpMessenger;
import games.strategy.net.Messengers;
import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lombok.extern.java.Log;
import org.triplea.java.Interruptibles;

/** Implementation of {@link ILauncher} for a headed local or network client game. */
@Log
public class LocalLauncher extends AbstractLauncher<ServerGame> {
  private final GameData gameData;
  private final GameSelectorModel gameSelectorModel;
  private final IRandomSource randomSource;
  private final PlayerListing playerListing;
  private final Component parent;
  private final LaunchAction launchAction;

  public LocalLauncher(
      final GameSelectorModel gameSelectorModel,
      final IRandomSource randomSource,
      final PlayerListing playerListing,
      final Component parent,
      final LaunchAction launchAction) {
    this.randomSource = randomSource;
    this.playerListing = playerListing;
    this.gameSelectorModel = gameSelectorModel;
    this.gameData = gameSelectorModel.getGameData();
    this.parent = parent;
    this.launchAction = launchAction;
  }

  @Override
  protected void launchInternal(@Nullable final ServerGame game) {
    try {
      if (game != null) {
        game.startGame();
      }
    } finally {
      // todo(kg), this does not occur on the swing thread, and this notifies setupPanel observers
      // having an oddball issue with the zip stream being closed while parsing to load default
      // game. might be caused
      // by closing of stream while unloading map resources.
      Interruptibles.sleep(100);
      gameSelectorModel.loadDefaultGameNewThread();
      SwingUtilities.invokeLater(() -> JOptionPane.getFrameForComponent(parent).setVisible(true));
    }
  }

  @Override
  Optional<ServerGame> loadGame() {
    try {
      gameData.doPreGameStartDataModifications(playerListing);
      final Messengers messengers = new Messengers(new LocalNoOpMessenger());
      final Set<Player> gamePlayers =
          gameData.getGameLoader().newPlayers(playerListing.getLocalPlayerTypeMap());
      final ServerGame game =
          new ServerGame(gameData, gamePlayers, new HashMap<>(), messengers, launchAction);
      game.setRandomSource(randomSource);
      gameData.getGameLoader().startGame(game, gamePlayers, launchAction, null);
      return Optional.of(game);
    } catch (final Exception ex) {
      log.log(Level.SEVERE, "Failed to start game", ex);
      return Optional.empty();
    }
  }

  /** Creates a launcher for a single player local (no network) game. */
  public static LocalLauncher create(
      final GameSelectorModel gameSelectorModel,
      final Collection<? extends PlayerCountrySelection> playerRows,
      final Component parent,
      final LaunchAction launchAction) {

    final Map<String, PlayerType> playerTypes =
        playerRows.stream()
            .collect(
                Collectors.toMap(
                    PlayerCountrySelection::getPlayerName, PlayerCountrySelection::getPlayerType));

    final Map<String, Boolean> playersEnabled =
        playerRows.stream()
            .collect(
                Collectors.toMap(
                    PlayerCountrySelection::getPlayerName,
                    PlayerCountrySelection::isPlayerEnabled));

    // we don't need the playerToNode list, the disable-able players, or the alliances list, for a
    // local game
    final PlayerListing pl =
        new PlayerListing(
            null,
            playersEnabled,
            playerTypes,
            gameSelectorModel.getGameData().getGameVersion(),
            gameSelectorModel.getGameName(),
            gameSelectorModel.getGameRound(),
            null,
            null);
    return new LocalLauncher(gameSelectorModel, new PlainRandomSource(), pl, parent, launchAction);
  }
}
