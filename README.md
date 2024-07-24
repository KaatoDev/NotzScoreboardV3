<div align="center">
<img src="https://github.com/KaatoDev/NotzScoreboard/assets/107152563/e42230a1-3d52-4717-b592-23ab428467b7" alt="" height="300" >

#
O NotzScoreboardV3 é um plugin de Scoreboard completo e totalmente personalizável que conta com suporte a mútiplas scoreboards simutâneas, placeholders próprias e PlaceholderAPI, database via MySQL ou SQLite e atualização dinâmica para maior eficiência.

<br/>

## Informações

### `Scoreboards`
O plugin conta com scoreboards dinâmicas que atualizam somente placeholders inseridas nela em cada linha, sem que precise recarregar a scoreboard completamente!
Contém também mútiplos comandos de personalização para que possam ser criadas e modificadas diretamente do servidor sem precisar mexer nos arquivos.

### `Grupos`
As scoreboards são separadas por grupos e pré-programadas para que seja possível visualizar players de outros grupos (e até do mesmo) na sua scoreboard ou do próprio grupo em outras scoreboards.

### `Templates`
Possui também um sistema de templates que podem ser criados no arquivo de configuração do plugin e setados através do jogo utilizando-os como Header, Footer ou o template principal, permitindo assim, ao mesmo tempo, uma maior customização e padronização de cada scoreboard.

### Placeholders
Você pode optar por utilizar as placeholders dinâmicas de outros plugins através do PlaceholderAPI ou placeholders estáticas próprias que podem ser criadas atravé do arquivo de configuração do plugin.

<br/>

## Scoreboard Demo
![player v3](https://github.com/user-attachments/assets/33363177-049a-4e31-938b-29ff63b6f173)

</div>

<br/>

## Dependências
- PlaceholderAPI

<br/>

## Placeholders
 - `{clan}` - Nome do clan. (Simpleclans) [PlaceholderAPI]
 - `{clan_tag}` - Tag do clan. (Simpleclans) [PlaceholderAPI]
 - `{clankdr}` - KDR do clan. (Simpleclans) [PlaceholderAPI]
 - `{money}` - Dinheiro do player direto do Vault. [PlaceholderAPI]
 - `{player_name}` - Nome do player.
 - `{player_displayname}` - Displayname do player.
 - `{ping}` - Ping do player.
 - `{rank}` - (yRanks do yPlugins) Ele utiliza a tag como cor e depois o nome. [PlaceholderAPI]
 - `{status_rankup}` - (yRanks do yPlugins) Ele utiliza a tag como cor e depois o status. [PlaceholderAPI]
 - `{tps}` - TPS do servidor. [PlaceholderAPI]

 - `{staff}` - Pega o nick de um dos players online dos grupos visíveis daquele grupo. - Quantidade de players online nos grupos visíveis daquele grupo.
 - `{supstaff}` - Mesma coisa que o {staff}, porém retorna uma mensagem alternativa caso o player esteja offline
 - `{staff_list}` - Quantidade de players online nos grupos visíveis daquele grupo.
 - `{staff_'group'}` - Pega um dos players daquele grupo.
 - `{'group'_list}` - Pega a quantidade de player online naquele grupo.

<br/>

## Permissões
- `notzscoreboard.admin` - Habilita o player a utilizar o comando de admin /notzsb.

<br/>

## Commandos
### `/notzsb`
 - `delete` \<scoreboard> - Cria a scoreboard com opção de já setar as templates.
 - `list` - Deleta uma scoreboard
 - `players` - Lista todas as scoreboards criadas.
 - `reload` - Lista todos os players registrados e suas respectivas scoreboards.
 - `reset` \<player> - Recarrega partes o plugin.
 - `set` \<scoreboard> - Reseta a scoreboard do player para a scoreboard padrão.
 - `update` - Seta a própria scoreboard.
 - `<scoreboard>`
   - `addplayer` <player> - Adiciona um player à scoreboard.
   - `addgroup` <group> - Adiciona um grupo ao VisibleGroups da scoreboard
   - `clearheader` - Limpa a header da scoreboard.
   - `clearfooter` - Limpa a footer da scoreboard.
   - `cleartemplate` - Limpa o template da scoreboard.
   - `pause` (minutes) - Pausa a atualização da scoreboard por X minutos (por padrão é pausado por 1 minuto).
   - `players` - Vê os players cadastrados na scoreboard.
   - `remplayer` <player> - Remove um player da scoreboard.
   - `remgroup` <group> - Remove um grupo do VisibleGroups da scoreboard
   - `setcolor` <color> - Seta uma nova cor da scoreboard.
   - `setdisplay` <display> - Seta um novo display na scoreboard.
   - `setheader` <template> - Seta uma nova header na scoreboard.
   - `setfooter` <template> - Seta uma nova footer na scoreboard.
   - `settemplate` <template> - Seta um novo template na scoreboard.
   - `view` - Visualiza a scoreboard sem precisar setar.
   - `visiblegroups` - Vê os grupos visíveis inseridos no grupo.
<br/>
<sub> | <> argumento obrigatório. | ( ) argumento opcional. | </sub>

#

<sub> Versões testadas: 1.8 </sub>
