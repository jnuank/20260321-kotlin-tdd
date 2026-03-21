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
){
    companion object{
        fun from(user: User, record: LoginRecord): UserWithLastLogin =
            UserWithLastLogin( user.id, record.loginAt )
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
    val loginRecordMap = loginRecords
        .groupBy { it.userId }
        .mapValues { (_, records) -> records.maxBy { it.loginAt } }
    val pairs = loginRecordMap.mapNotNull { (userId, record) ->
        userMap[userId]?.let { user -> user to record }
    }
    return pairs
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

// ============================================
// テストコード例
// ============================================

//fun main() {
//    // テストデータ
//    val users = listOf(
//        User(id = 1L, name = "Alice", email = "alice@example.com"),
//        User(id = 2L, name = "Bob", email = "bob@example.com"),
//        User(id = 3L, name = "Charlie", email = "charlie@example.com")
//    )
//
//    val loginRecords = listOf(
//        LoginRecord(userId = 1L, loginAt = OffsetDateTime.parse("2025-01-01T10:00:00Z"), ipAddress = "192.168.1.1"),
//        LoginRecord(userId = 2L, loginAt = OffsetDateTime.parse("2025-01-02T11:00:00Z"), ipAddress = "192.168.1.2"),
//        // user_id=3のログインレコードは存在しない
//    )
//
//    // Before版の実行
//    println("=== BEFORE版 ===")
//    val resultBefore = UserServiceBefore.getUsersWithLastLogin(users, loginRecords)
//    resultBefore.forEach { println(it) }
//
//    // After版の実行
//    println("\n=== AFTER版 ===")
//    val resultAfter = UserServiceAfter.getUsersWithLastLogin(users, loginRecords)
//    resultAfter.forEach { println(it) }
//
//    // 独立した関数のテスト例
//    println("\n=== buildLoginRecordMapのテスト ===")
//    val map = UserServiceAfter.buildLoginRecordMap(loginRecords)
//    println("Map size: ${map.size}")
//    println("userId=1のレコード: ${map[1L]}")
//    println("userId=999のレコード（存在しない）: ${map[999L]}")
//}

// ============================================
// TDD的な観点での比較
// ============================================

/**
 * BEFORE版の問題:
 *
 * - `getUsersWithLastLogin()`は一つの大きな処理
 * - 内部のマッチングロジックを単独でテストできない
 * - テストするには常に users + loginRecords の両方を用意する必要がある
 * - ロジックの変更時にテストの書き直しが必要
 *
 * 例: 「重複したuserIdがあった場合の挙動」を確認したい
 *     → users も loginRecords も用意して、getUsersWithLastLogin()全体を実行するしかない
 */

/**
 * AFTER版の利点:
 *
 * - buildLoginRecordMap(): Map構築ロジックを独立してテスト可能
 *   テスト例: 重複したuserIdがあった場合、最後の要素が優先されるか？
 *
 * - mapUsersToLastLogin(): マッピングロジックを独立してテスト可能
 *   テスト例: Mapに存在しないuserIdがあった場合、結果から除外されるか？
 *
 * - 各関数が単一責任原則に従っている
 * - テストが高速（必要な部分だけテストできる）
 * - テストが読みやすい（何をテストしているか明確）
 */

// ============================================
// パフォーマンス比較（参考）
// ============================================

/**
 * データ量: users=100件, loginRecords=100件の場合
 *
 * BEFORE版（O(n × m)）:
 *   - users.mapNotNull の各要素（100回）に対して
 *   - loginRecords.firstOrNull で線形探索（最大100回）
 *   - 総操作回数: 最大 10,000回
 *
 * AFTER版（O(n + m)）:
 *   - associateBy で1回走査（100回）
 *   - mapNotNull でMap検索（100回 × O(1)）
 *   - 総操作回数: 200回
 *
 * 差: 約50倍
 */