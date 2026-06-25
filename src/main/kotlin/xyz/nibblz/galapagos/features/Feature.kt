package xyz.nibblz.galapagos.features

interface Feature {
    val id: String
    val name: String

    fun init()
}