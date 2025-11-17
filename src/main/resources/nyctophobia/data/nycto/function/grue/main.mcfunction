execute unless predicate nycto:in_darkness run function nycto:grue/despawn

execute unless entity @a[distance=..30] run function nycto:grue/despawn
execute if entity @a[distance=..30,gamemode=!creative,gamemode=!spectator,nbt={active_effects:[{id:"minecraft:night_vision"}]}] run function nycto:grue/despawn

execute as @s[nbt={HurtTime:10s}] at @s run function nycto:grue/hurt

