package org.example

import java.time.OffsetDateTime

data class User(
        val id: Long,
        val name: String,
        val email: String
)

data class LoginRecord(
        val userId: Long,
        val loginAt: OffsetDateTime,
        val ipAddress: String
)

data class UserWithLastLogin(
        val userId: Long,
        val lastLoginAt: OffsetDateTime,
) {
    companion object {
        fun from(user: User, record: LoginRecord): UserWithLastLogin =
            UserWithLastLogin(user.id, record.loginAt)
    }
}

fun getUsersWithLastLogin(
        users: List<User>,
        loginRecords: List<LoginRecord>
): List<UserWithLastLogin> {
    val latestLoginMap = buildLatestLoginMap(loginRecords)
    return matchUsersWithLoginRecords(users, latestLoginMap)
        .map { (user, record) -> UserWithLastLogin.from(user, record) }
}

fun matchUsersWithLoginRecords(
        users: List<User>,
        latestLoginMap: Map<Long, LoginRecord>
): List<Pair<User, LoginRecord>> {
    return users.mapNotNull { user ->
        latestLoginMap[user.id]?.let { record ->
            user to record
        }
    }
}

fun buildLatestLoginMap(loginRecords: List<LoginRecord>): Map<Long, LoginRecord> {
    return loginRecords
        .groupBy { it.userId }
        .mapValues { (l, records) ->
            records.maxBy { it.loginAt }
        }
}