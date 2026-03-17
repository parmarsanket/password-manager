package com.sanket.tools.passwordmanager

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform