<div align="center">
  
##### **NotzPlugins**
<a href="https://modrinth.com/plugin/notzexcavate">
<img src="https://repo.kaato.dev/images/plugins/NotzExcavate2.png" alt="Notz Excavate" width="150"/>
</a>
<img src="https://repo.kaato.dev/images/plugins/NotzScoreboard2.png" alt="Notz Scoreboard" width="150"/>
<a href="https://modrinth.com/plugin/notzwarps">
<img src="https://repo.kaato.dev/images/plugins/NotzWarps2.png" alt="Notz Warps" width="150"/>
</a>
  
#
<img src="https://repo.kaato.dev/images/plugins/NotzScoreboard2.png" alt="NotzScoreboardV3" height="300" >

#
NotzScoreboard is a complete and fully customizable scoreboard plugin that features: support for multiple simultaneous scoreboards; its own placeholders and PlaceholderAPI; database via MySQL or SQLite and dynamic updating for greater efficiency.

<br/>

## Information

### `Scoreboards`
The plugin has dynamic scoreboards that only update the placeholders inserted in each line, without having to reload the scoreboard completely!
It also contains multiple customization commands so that they can be created and modified directly from the server without having to mess with the files.

### `Groups`
The scoreboards are separated by groups and pre-programmed so that it is possible to view players from other groups (and even from the same group) on your scoreboard or from the group itself on other scoreboards.

### `Templates`
It also has a system of templates that can be created in the plugin's configuration file and set through the game using them as Header, Footer or the main template, thus allowing greater customization and standardization of each scoreboard.

### Placeholders
You can choose to use dynamic placeholders from other plugins via the PlaceholderAPI or your own static placeholders that can be created via the plugin's configuration file.

<br/>

## Scoreboard Demo
<br/>

### Player example with staff on and off
<div>
<img src="https://repo.kaato.dev/images/plugins/NotzScoreboard/Player.png" alt="Player" width="300"/>
</div>

<br/>

### Helper and Trial example
<div>
<img src="https://repo.kaato.dev/images/plugins/NotzScoreboard/Helper.png" alt="Helper" width="300"/>
<img src="https://repo.kaato.dev/images/plugins/NotzScoreboard/Trial.png" alt="Trial" width="300"/>
</div>

<br/>

### Mod and Admin example
<div>
<img src="https://repo.kaato.dev/images/plugins/NotzScoreboard/Mod.png" alt="Mod" width="300"/>
<img src="https://repo.kaato.dev/images/plugins/NotzScoreboard/Admin.png" alt="Admin" width="300"/>
</div>

<br/>

### Manager and Owner example
<div>
<img src="https://repo.kaato.dev/images/plugins/NotzScoreboard/Manager.png" alt="Manager" width="300"/>
<img src="https://repo.kaato.dev/images/plugins/NotzScoreboard/Owner.png" alt="Owner" width="300"/>
</div>
</div>

<br/>

## Dependencies
- PlaceholderAPI

<br/>

## Placeholders
 - `{clan}` - Clan name (Simpleclans) [PlaceholderAPI]
 - `{clan_tag}` - Clan tag (Simpleclans) [PlaceholderAPI]
 - `{clankdr}` - KDR of the clan. (Simpleclans) [PlaceholderAPI]
 - `{money}` - Player money straight from the Vault. [PlaceholderAPI]
 - `{player_name}` - Player's name.
 - `{player_displayname}` - Player display name.
 - `{ping}` - Player ping. [PlaceholderAPI]
 - `{rank}` - (yRanks from yPlugins) It uses the tag as the color and then the name. [PlaceholderAPI]
 - `{status_rankup}` - (yRanks from yPlugins) It uses the tag as the color and then the status. [PlaceholderAPI]
 - `{tps}` - TPS of the server. [PlaceholderAPI]

 - `{staff}` - Get the nick of one of the online players in the visible groups for that group. - Number of players online in the visible groups for that group.
 - `{supstaff}` - Same thing as {staff}, but returns an alternative message if the player is offline
 - `{staff_list}` - Number of players online in the visible groups for that group.
 - `{staff_'group'}` - Get one of the players in that group.
 - `{'group'_list}` - Gets the number of players online in that group.

<br/>

## Permissions
- `notzscoreboard.admin` - Enables the player to use the /notzsb admin command.

<br/>

## Commands
### `/notzsb`
 - `create` \<name> \<display> (header) (template) (footer) - Creates the scoreboard with the option of already setting the templates.
 - `delete` \<scoreboard> - Deletes a scoreboard.
 - `list` - Lists all the scoreboards created.
 - `players` - Lists all registered players and their respective scoreboards.
 - `reload` - Reloads parts of the plugin.
 - `reset` \<player> - Resets the player's scoreboard to the default scoreboard.
 - `set` \<scoreboard> - Sets the scoreboard itself.
 - `update` - Updates all scoreboards.
 - `<scoreboard>`
   - `addplayer` <player> - Adds a player to the scoreboard.
   - `addgroup` <group> - Adds a group to the scoreboard's VisibleGroups.
   - `clearheader` - Clears the scoreboard header.
   - `clearfooter` - Clears the footer of the scoreboard.
   - `cleartemplate` - Clears the scoreboard template.
   - `pause` (minutes) - Pause the scoreboard update for X minutes (by default it is paused for 1 minute).
   - `players` - View the players registered on the scoreboard.
   - `remplayer` \<player> - Remove a player from the scoreboard.
   - `remgroup` \<group> - Remove a VisibleGroups group from the scoreboard
   - `setcolor` \<color> - Sets a new color for the scoreboard.
   - `setdisplay` \<display> - Sets a new display on the scoreboard.
   - `setheader` \<template> - Set a new header on the scoreboard.
   - `setfooter` \<template> - Set a new footer on the scoreboard.
   - `settemplate` \<template> - Sets a new template on the scoreboard.
   - `view` - View the scoreboard without having to set it.
   - `visiblegroups` - View the visible groups inserted in the group.

<br/>
<sub> | <> required argument. | ( ) optional argument. | </sub>

#

<sub> Tested versions: 1.8 - 1.14.4 </sub>
