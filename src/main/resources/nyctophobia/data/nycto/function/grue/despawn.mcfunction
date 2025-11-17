particle smoke ~ ~ ~ 0.5 0.5 0.5 0 600 normal
execute store result score @s grue.rng run loot spawn ~ ~ ~ loot nycto:rng/1-3
execute if score @s grue.rng matches 1 run playsound minecraft:grue.snarl1 hostile @a ~ ~ ~ 1
execute if score @s grue.rng matches 2 run playsound minecraft:grue.snarl2 hostile @a ~ ~ ~ 1
execute if score @s grue.rng matches 3 run playsound minecraft:grue.snarl3 hostile @a ~ ~ ~ 1
tp @s ~ ~-256 ~
kill @s