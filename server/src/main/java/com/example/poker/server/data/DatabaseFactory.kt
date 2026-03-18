package com.example.poker.server.data

import com.example.poker.server.data.entity.Users
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val driverClassName = config.property("db.driver").getString()
        val jdbcURL = config.property("db.url").getString()
        val user = config.property("db.user").getString()
        val password = config.property("db.password").getString()
        val database = Database.connect(jdbcURL, driverClassName, user, password)

        // Создаем транзакцию для выполнения DDL-запросов (создание таблиц)
        transaction(database) {
            // SchemaUtils.create() проверяет, существует ли таблица Users,
            // и создает ее только в том случае, если она отсутствует.
            SchemaUtils.create(Users)
        }
    }

    /**
     * Вспомогательная функция для выполнения запросов к БД в корутинах.
     * Она гарантирует, что блокирующие JDBC-вызовы будут выполняться
     * в специальном пуле потоков Dispatchers.IO, не блокируя основной поток Ktor.
     */
    suspend fun <T> dbQuery(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        suspendTransaction { block() }
    }
}