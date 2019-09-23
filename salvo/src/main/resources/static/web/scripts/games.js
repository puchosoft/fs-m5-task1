$(function(){

  function getTotal(name){
    return scoreList.filter(s => s.name == name).reduce((total, s) => total + s.score, 0);
  }

  function getWons(name){
    return scoreList.filter(s => s.name == name && s.score == 1).length;
  }

  function getLosts(name){
    return scoreList.filter(s => s.name == name && s.score == 0).length;
  }

  function getTieds(name){
      return scoreList.filter(s => s.name == name && s.score == 0.5).length;
  }

  // Devuelve un JSON con la tabla de jugadores con puntajes mas altos
  function getLeaderTable(gameList){
    scoreList=[]; // Lista de nombres y puntajes de todos los juegos
    var nameList=[]; // Lista de nombres de jugadores (no repetidos)
    var leaderTable=[]; // JSON de la LeaderTable

    // Obtiene la lista de jugadores con puntajes
    gameList.forEach(game => {
      game.gamePlayers.forEach(gp => {
        scoreList.push(
          {
            name: gp.player.email,
            score: gp.score
          }
        );
      });
    });
    // Elimina de la lista los puntajes de juegos no terminados
    scoreList = scoreList.filter(player => player.score != null);

    // Obtiene la lista de nombres unicos (no repetidos)
    nameList = scoreList.map(s => s.name).
      filter((n,i,nl) => nl.indexOf(n)==i);

    // Para cada jugador de la lista, agrega una linea de datos al JSON
    nameList.forEach(name => {
      leaderTable.push(
        {
          name: name,
          total: getTotal(name).toFixed(1),
          won: getWons(name),
          lost: getLosts(name),
          tied: getTieds(name)
        }
      );
    });

    // Ordena las lineas del JSON por puntajes decrecientes
    return leaderTable.sort((l1, l2) => l2.total - l1.total);
  }

  // Construye una lista ordenada de Games con su informacion y la devuelve al HTML
  function showGameList(gameList){
    var table = "<tr>\
                  <th>Nº</th><th>Created</th><th>Player 1</th><th>Player 2</th>\
                </tr>";
    gameList.forEach(g => {
      var date = new Date(g.created).toLocaleString();
      var players = g.gamePlayers.map(gp => gp.player.email);
      players.push('');
      table+= '<tr>\
                <td>' + g.id + '</td>\
                <td>' + date + '</td>\
                <td class="text-left">' + players[0] + '</td>\
                <td class="text-left">' + players[1] + '</td>\
              </tr>';
    });
    $('#gameList').html(table);
  }

  // Construye una tabla de estadisticas de puntajes de jugadores y la devuelve al HTML
  function showLeaderBoard(gameList){
    var table = "<tr>\
                  <th>Name</th><th>Total</th><th>Won</th><th>Lost</th><th>Tied</th>\
                 </tr>";
    getLeaderTable(gameList).forEach(player => {
      table += '<tr>\
                  <td class="text-left">' + player.name + '</td>\
                  <td>' + player.total + '</td>\
                  <td>' + player.won + '</td>\
                  <td>' + player.lost + '</td>\
                  <td>' + player.tied + '</td>\
                </tr>';
    });

    $('#leaderBoard').html(table);
  }

  function loadData(){
      $.getJSON("/api/games")
      .done(
        function(data) {
          showGameList(data);
          showLeaderBoard(data);
        }
      );
  }

  loadData();
});
