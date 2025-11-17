execute store result score @s grue.rng run loot spawn ~ ~ ~ loot nycto:rng/1-3
execute if score @s grue.rng matches 1 run playsound minecraft:grue.growl1 hostile @a ~ ~ ~ 1
execute if score @s grue.rng matches 2 run playsound minecraft:grue.growl2 hostile @a ~ ~ ~ 1
execute if score @s grue.rng matches 3 run playsound minecraft:grue.growl3 hostile @a ~ ~ ~ 1