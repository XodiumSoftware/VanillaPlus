execute store result score @s grue.rng run loot spawn ~ ~ ~ loot nycto:rng/1-6
execute if score @s grue.rng matches 1 run title @s actionbar [{"text": "Breathing is heard in the darkness","color": "dark_gray"}]
execute if score @s grue.rng matches 2 run title @s actionbar [{"text": "You feel as though you are being watched","color": "dark_gray"}]
execute if score @s grue.rng matches 3 run title @s actionbar [{"text": "A horrible chill creeps down your spine","color": "dark_gray"}]
execute if score @s grue.rng matches 4 run title @s actionbar [{"text": "You feel an unshakable sense of unease","color": "dark_gray"}]
execute if score @s grue.rng matches 5 run title @s actionbar [{"text": "You sense danger in the cloak of darkness","color": "dark_gray"}]
execute if score @s grue.rng matches 6 run title @s actionbar [{"text": "Heavy breathing approaches slowly","color": "dark_gray"}]