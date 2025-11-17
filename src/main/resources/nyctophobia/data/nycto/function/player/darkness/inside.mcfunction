# Runs every tick the player is in light level 0 as the player inside

execute if score @s grue.darknesstimer matches 1.. run scoreboard players remove @s grue.darknesstimer 1

execute if score @s grue.darknesstimer matches 375 run playsound minecraft:grue.warning hostile @s ~ ~ ~ 0.8

execute if score @s grue.darknesstimer matches 238 run function nycto:player/warn

execute if score @s grue.darknesstimer matches 238 run effect give @s darkness 2 4 true
execute if score @s grue.darknesstimer matches 100 run effect give @s darkness 2 8 true
execute if score @s grue.darknesstimer matches 1 run effect give @s darkness 8 15 true
execute if score @s grue.darknesstimer matches 1 run effect give @s blindness 8 1 true
execute if score .diff grue.diff matches 3 if score @s grue.darknesstimer matches 1 run effect give @s slowness 8 2 true

execute if score @s grue.darknesstimer matches 1 run function nycto:grue/spawn
tag @s[tag=!nycto.indarkness] add nycto.indarkness