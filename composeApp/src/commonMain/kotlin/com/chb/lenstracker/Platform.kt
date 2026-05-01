package com.chb.lenstracker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform