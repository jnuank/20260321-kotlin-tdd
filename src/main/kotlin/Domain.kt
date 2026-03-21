package org.example

// ============================================
// ブログ記事用サンプルコード
// テーマ: TDDのしづらさからリファクタリングへ
// ============================================

import java.time.OffsetDateTime

// ドメインモデル
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


// ============================================
// BEFORE: O(n²)版 - テストしづらい実装
// ============================================

/**
 * 問題点:
 * 1. パフォーマンス: O(n × m)の計算量
 * 2. テスタビリティ: 内部ロジック（マッチング処理）を単独でテストできない
 * 3. 可読性: ネストしたロジックで意図が読み取りにくい
 */

fun getUsersWithLastLogin(
        users: List<User>,
        loginRecords: List<LoginRecord>
): List<UserWithLastLogin> {

    val pairs = matching(users, loginRecords)

    return pairs.map { (user, record) ->
        UserWithLastLogin.from(user, record)
    }

//    return users.mapNotNull { user ->
//        // ↓ この内部ループが問題
//        // - 毎回リスト全体を探索（O(m)）
//        // - マッチングロジックを単独でテストできない
//        loginRecords
//            .lastOrNull() { it.userId == user.id }
//            ?.let { record ->
//                UserWithLastLogin.from(user, record)
//            }
//    }
}

fun matching(
        users: List<User>,
        loginRecords: List<LoginRecord>
): List<Pair<User, LoginRecord>> {
    val userMap = users.associateBy { it.id }
    return loginRecords
        .groupBy { it.userId }
        .mapValues { (_, records) -> records.maxBy { it.loginAt } }
        .mapNotNull { (userId, record) ->
            userMap[userId]?.let { user -> user to record }
        }
}


// ============================================
// AFTER: O(n)版 - テストしやすい実装
// ============================================

/**
 * 改善点:
 * 1. パフォーマンス: O(n + m)の計算量
 * 2. テスタビリティ: 各ステップを独立してテストできる
 *    - buildLoginRecordMap()をテスト可能
 *    - mapUsersToLastLogin()をテスト可能
 * 3. 可読性: 処理が明示的に分離されている
 */
object UserServiceAfter {
    fun getUsersWithLastLogin(
            users: List<User>,
            loginRecords: List<LoginRecord>
    ): List<UserWithLastLogin> {
        // Step 1: O(m)でHashMapを構築（テスト可能）
        val loginRecordMap = buildLoginRecordMap(loginRecords)

        // Step 2: O(n)で変換（テスト可能）
        return mapUsersToLastLogin(users, loginRecordMap)
    }

    /**
     * テスト可能な独立した関数
     * - 入力: LoginRecordのリスト
     * - 出力: userId -> LoginRecord のMap
     * - テストしたいこと: 重複したuserIdがあった場合、最後の要素が優先されること
     */
    fun buildLoginRecordMap(loginRecords: List<LoginRecord>): Map<Long, LoginRecord> {
        return loginRecords.associateBy { it.userId }
    }

    /**
     * テスト可能な独立した関数
     * - 入力: Userリストとマッチング用Map
     * - 出力: UserWithLastLoginのリスト
     * - テストしたいこと: Mapに存在しないuserIdはnullになること
     */
    fun mapUsersToLastLogin(
            users: List<User>,
            loginRecordMap: Map<Long, LoginRecord>
    ): List<UserWithLastLogin> {
        return users.mapNotNull { user ->
            loginRecordMap[user.id]?.let { record ->
                UserWithLastLogin(
                    userId = user.id,
                    lastLoginAt = record.loginAt,
                )
            }
        }
    }
}