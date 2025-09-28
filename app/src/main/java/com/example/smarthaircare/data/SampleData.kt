package com.example.smarthaircare.data

data class HairOil(
    val name: String,
    val benefit: String,
    val imageUrl: String = ""
)

val sampleOils = listOf(
    HairOil("Coconut Oil", "Deep moisturizing"),
    HairOil("Argan Oil", "Shine & smoothness"),
    HairOil("Jojoba Oil", "Scalp nourishment"),
    HairOil("Castor Oil", "Hair growth boost"),
    HairOil("Olive Oil", "Damage repair")
)
