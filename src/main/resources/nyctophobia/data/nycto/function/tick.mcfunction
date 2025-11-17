## ---------------------------
##
## Nyctophobia - Tick Function
## Do not redistribute without permission
##
## ---------------------------

# - New Player

execute as @a[tag=!nycto.registered] at @s run function nycto:player/register

# - Darkness - #

execute as @a[predicate=nycto:in_darkness,gamemode=!spectator,gamemode=!creative,nbt=!{active_effects:[{id:"minecraft:night_vision"}]}] at @s if block ~ ~ ~ #nycto:nonsolid run function nycto:player/darkness/inside
execute as @a[predicate=!nycto:in_darkness] at @s run function nycto:player/darkness/outside
execute as @a[nbt={active_effects:[{id:"minecraft:night_vision"}]}] at @s run function nycto:player/darkness/outside

# - Grue

execute as @e[type=husk,tag=grue.gruemob] at @s run function nycto:grue/main