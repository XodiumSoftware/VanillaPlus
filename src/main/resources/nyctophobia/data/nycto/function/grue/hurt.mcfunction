execute store result score @s grue.rng run loot spawn ~ ~ ~ loot nycto:rng/1-2

execute if score @s grue.rng matches 1 run playsound minecraft:grue.hurt1 hostile @a ~ ~ ~ 1
execute if score @s grue.rng matches 2 run playsound minecraft:grue.hurt2 hostile @a ~ ~ ~ 1