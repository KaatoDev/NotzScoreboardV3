<div align="center">
<img src="https://github.com/KaatoDev/NotzScoreboard/assets/107152563/e42230a1-3d52-4717-b592-23ab428467b7" alt="" height="300" >

#
NotzScoreboard é um plugin de Scoreboard completo e totalmente personalizável com suporte a placeholders (PlaceholderAPI), database via MySQL ou SQLite e atualização dinâmica para maior eficiência.

</div>

## Informações

###### Quando utilizado o placeholder {staff} ou {staff_list}.

## Dependências
- PlaceholderAPI

<details>
  
<summary><h2>Spoilers</h2></summary>

- ### Player Scoreboard
![player](https://github.com/KaatoDev/NotzScoreboard/assets/107152563/d03d92ae-b67a-4e67-a01d-30d6fc5d26c1)

</details>

## Placeholders
 - `{clan} `- Nome do clan. (Simpleclans)
 - `{clan_tag}` - Tag do clan. (Simpleclans)
 - `{clankdr}` - KDR do clan. (Simpleclans)
 - `{money}` - Dinheiro do player direto do Vault.
 - `{player_name}` - Nome do player.
 - `{player_displayname}` - Displayname do player.
 - `{ping} `- Ping do player.
 - `{rank} `- (yRanks do yPlugins) Ele utiliza a tag como cor e depois o nome.
 - `{status_rankup}` - (yRanks do yPlugins) Ele utiliza a tag como cor e depois o status.
 - `{tps} - `TPS do servidor.

 - `{staff}` - Pega o nick de um dos players online dos grupos visíveis daquele grupo. - Quantidade de players online nos grupos visíveis daquele grupo.
 - `{supstaff}` - Mesma coisa que o {staff}, porém retorna uma mensagem alternativa caso o player esteja offline
 - `{staff_list}` - Quantidade de players online nos grupos visíveis daquele grupo.
 - `{staff_'group'}` - Pega um dos players daquele grupo.
 - `{'group'_list}` - Pega a quantidade de player online naquele grupo.

## Permissões

- `notzscoreboard.admin` - Habilita o player a utilizar o comando /notzsb.

## Commandos
### `/notzsb`
 - `add` \<cargo> \<player> - Adiciona e atribui um player à um cargo.
 - `debug` - Acompanha em tempo real a atualização da scoreboard de cada staff setado.
 - `help` - Lista os comandos do plugin.
 - `list` - Lista todos os players salvos.
 - `reload` - Recarrega as configurações (.yml) do plugin.
 - `remove` \<player> - Remove um player da lista.
 - `reset` - Reseta a database salva inteira.
 - `setRole` \<cargo> (player) - Altera o cargo do player.
 - `setScore` \<cargo> (player) - Altera a scoreboard do player.

 ###### | <> argumento obrigatório. | () argumento opcional. |
 
#
###### Versões testadas: 1.8
