package com.codeoftheweb.salvo.Controllers;

import com.codeoftheweb.salvo.Entities.GamePlayer;
import com.codeoftheweb.salvo.Entities.Player;
import com.codeoftheweb.salvo.Repositories.GamePlayerRepository;
import com.codeoftheweb.salvo.Repositories.GameRepository;
import com.codeoftheweb.salvo.Repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.tools.jconsole.JConsole;

import java.net.Authenticator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@RestController
@RequestMapping("/api") // Todos los controladores cuelgan de /api
public class SalvoController {

  @Autowired
  private GameRepository gameRepo;

  @Autowired
  private GamePlayerRepository gamePlayerRepo;

  @Autowired
  private PlayerRepository playerRepo;

  //@Autowired
  //private PasswordEncoder passwordEncoder;

  // Genera un JSON con la informacion de los games en la URL /api/games
  @RequestMapping("/games")
  public Map<String, Object> getGameInfo(Authentication auth) {
    Map<String, Object> gameInfo = new LinkedHashMap<>();
    gameInfo.put("player", isGuest(auth)? null : getCurrentPlayer(auth).toDTO());
    gameInfo.put("games", gameRepo
        .findAll()
        .stream()
        .map(game -> game.toDTO())
        .collect(toList()));
    return gameInfo;
  }

  // Genera un JSON con la informacion de un game especifico en la URL /api/game_view/nn
  @RequestMapping("/game_view/{gamePlayerId}")
  public Map<String, Object> getGameView(@PathVariable long gamePlayerId) {
    GamePlayer gamePlayer = gamePlayerRepo.getOne(gamePlayerId);
    Map<String, Object> gameDTO = gamePlayer.getGame().toDTO();

    gameDTO.put("ships", gamePlayer.getShips()
        .stream()
        .map(ship -> ship.toDTO())
    );

    gameDTO.put("salvoes", gamePlayer.getGame().getGamePlayers()
        .stream()
        .map(game_gamePlayer -> game_gamePlayer.toSalvoDTO())
        .collect(toSet())
    );
    return gameDTO;
  }

  private boolean isGuest(Authentication authentication) {
    return authentication == null || authentication instanceof AnonymousAuthenticationToken;
  }

  private Player getCurrentPlayer(Authentication auth) {
    return isGuest(auth)? null : playerRepo.findByUsername(auth.getName());
  }

}
