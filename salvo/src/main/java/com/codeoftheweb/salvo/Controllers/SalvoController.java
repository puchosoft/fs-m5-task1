package com.codeoftheweb.salvo.Controllers;

import com.codeoftheweb.salvo.Entities.GamePlayer;
import com.codeoftheweb.salvo.Entities.Player;
import com.codeoftheweb.salvo.Repositories.GamePlayerRepository;
import com.codeoftheweb.salvo.Repositories.GameRepository;
import com.codeoftheweb.salvo.Repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
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

  @Autowired
  private PasswordEncoder passwordEncoder;

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

  private boolean isGuest(Authentication auth) {
    return auth == null || auth instanceof AnonymousAuthenticationToken;
  }

  private Player getCurrentPlayer(Authentication auth) {
    return isGuest(auth)? null : playerRepo.findByUsername(auth.getName());
  }

  @RequestMapping(path = "/players", method = RequestMethod.POST)
  public ResponseEntity<Map<String, Object>> createPlayer(@RequestParam String username, @RequestParam String password, Authentication auth){
    Map<String, Object> map = new LinkedHashMap<>();
    HttpStatus status;

    if(!isGuest(auth)){ // Si ya hay un usuario conectado ...
      map.put("error", "User logged in ");
      status = HttpStatus.CONFLICT;
    }
    else if (username.isEmpty()){ // Si el username está vacio
      map.put("error", "No name");
      status = HttpStatus.EXPECTATION_FAILED;
    }
    else if (playerRepo.findByUsername(username) != null){ // Si el username ya está en uso
      map.put("error", "Name in use");
      status = HttpStatus.FORBIDDEN;
    }
    else { // Si es correcto ...
      Player player = playerRepo.save(new Player(username, passwordEncoder.encode(password)));
      map.put("username", player.getUsername());
      status = HttpStatus.CREATED;
    }
    return new ResponseEntity<>(map, status );
  }

}
