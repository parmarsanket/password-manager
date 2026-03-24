package com.sanket.tools.passwordmanager.data.db

/**
 * Platform-specific database builder.
 *
 * Android  → uses Context to locate the DB file
 * JVM      → uses user home directory
 * iOS      → uses NSHomeDirectory
 *
 * The `expect` here is the common contract; each platform
 * provides an `actual` implementation.
 */
expect class DatabaseFactory {
    fun create(): AppDatabase
}
