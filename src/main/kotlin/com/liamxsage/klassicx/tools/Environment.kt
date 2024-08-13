package com.liamxsage.klassicx.tools

import io.github.cdimascio.dotenv.dotenv

object Environment {

    private val env = System.getenv()
    private val dotEnv = dotenv {
        ignoreIfMissing = true
    }

    /**
     * Retrieves the value of the environment variable with the specified key.
     *
     * @param key The key of the environment variable.
     * @return The value of the environment variable, or null if the variable is not found.
     */
    fun getString(key: String): String? {
        return dotEnv[key] ?: env[key]
    }

    fun getBoolean(key: String): Boolean {
        return getString(key)?.toBoolean() ?: false
    }

    fun getInt(key: String): Int {
        return getString(key)?.toIntOrNull() ?: 0
    }

    fun getLong(key: String): Long {
        return getString(key)?.toLongOrNull() ?: 0L
    }

    fun getDouble(key: String): Double {
        return getString(key)?.toDoubleOrNull() ?: 0.0
    }

    fun getFloat(key: String): Float {
        return getString(key)?.toFloatOrNull() ?: 0.0f
    }

}