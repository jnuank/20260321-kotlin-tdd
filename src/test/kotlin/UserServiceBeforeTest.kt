import io.mockk.every
import io.mockk.mockk
import org.example.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class UserLoginRecordTest {
    @Nested
    inner class `１パターン目` {

        @Test
        fun 生成のテスト() {
            val user = User(
                id = 25L,
                name = "John",
                email = "john@example.com"
            )
            val record = LoginRecord(
                userId = 25L,
                loginAt = OffsetDateTime.parse("2024-12-10T10:15:30+09:00"),
                ipAddress = "192.168.1.1"
            )

            val actual = UserWithLastLogin.from(user, record)

            val expected = UserWithLastLogin(
                userId = 25L,
                lastLoginAt = OffsetDateTime.parse("2024-12-10T10:15:30+09:00")
            )
            assertEquals(expected, actual)
        }


        @Test
        fun 各ユーザーの最終ログイン履歴を取得する() {
            val loginRecord1 = LoginRecord(
                userId = 1L, loginAt = OffsetDateTime.parse("2026-01-02T10:15:30+09:00"), ipAddress = "192.168.1.1"
            )
            val loginRecord2 = LoginRecord(
                userId = 2L, loginAt = OffsetDateTime.parse("2026-01-22T10:15:30+09:00"), ipAddress = "192.168.1.2"
            )
            val loginRecords = listOf(loginRecord1, loginRecord2)

            val actual = buildLatestLoginMap(loginRecords)

            val expected = mapOf(
                1L to loginRecord1, 2L to loginRecord2
            )
            assertEquals(expected, actual)
        }

        @Test
        fun 各ユーザーの最終ログイン履歴を取得する_各ユーザーに複数レコードある場合() {
            val loginRecord1a = LoginRecord(
                userId = 1L, loginAt = OffsetDateTime.parse("2026-01-02T10:15:30+09:00"), ipAddress = "192.168.1.1"
            )
            val loginRecord2a = LoginRecord(
                userId = 2L, loginAt = OffsetDateTime.parse("2026-01-22T10:15:30+09:00"), ipAddress = "192.168.1.2"
            )
            val loginRecord2b = LoginRecord(
                userId = 2L, loginAt = OffsetDateTime.parse("2026-03-26T10:15:30+09:00"), ipAddress = "192.168.1.2"
            )
            val loginRecord1b = LoginRecord(
                userId = 1L, loginAt = OffsetDateTime.parse("2026-02-07T03:23:45+09:00"), ipAddress = "192.168.1.1"
            )
            val loginRecords = listOf(
                loginRecord1a,
                loginRecord2a,
                loginRecord2b,
                loginRecord1b,
            )

            val actual = buildLatestLoginMap(loginRecords)

            val expected = mapOf(
                1L to loginRecord1b, 2L to loginRecord2b
            )
            assertEquals(expected, actual)
        }

        @Test
        fun UserとLoginRecordのMapをマッチングしてペアを作る() {
            val user1 = User(id = 1L, name = "Alice", email = "alice@example.com")
            val user2 = User(id = 2L, name = "Bob", email = "bob@example.com")
            val users = listOf(user1, user2)

            val record1 = LoginRecord(
                userId = 1L, loginAt = OffsetDateTime.parse("2026-01-02T10:15:30+09:00"), ipAddress = "192.168.1.1"
            )
            val record2 = LoginRecord(
                userId = 2L, loginAt = OffsetDateTime.parse("2026-01-22T10:15:30+09:00"), ipAddress = "192.168.1.2"
            )
            val latestLoginMap = mapOf(1L to record1, 2L to record2)

            val actual = matchUsersWithLoginRecords(users, latestLoginMap)

            val expected = listOf(user1 to record1, user2 to record2)
            assertEquals(expected, actual)
        }

        @Test
        fun UserとLoginRecordのMapをマッチングしてペアを作る_Mapに存在しないuserIdは結果に含まれない() {
            val user1 = User(id = 1L, name = "Alice", email = "alice@example.com")
            val user2 = User(id = 2L, name = "Bob", email = "bob@example.com")
            val user3 = User(id = 3L, name = "Charlie", email = "charlie@example.com")
            val users = listOf(user1, user2, user3)

            val record1 = LoginRecord(
                userId = 1L, loginAt = OffsetDateTime.parse("2026-01-02T10:15:30+09:00"), ipAddress = "192.168.1.1"
            )
            val record2 = LoginRecord(
                userId = 2L, loginAt = OffsetDateTime.parse("2026-01-22T10:15:30+09:00"), ipAddress = "192.168.1.2"
            )
            // user3のレコードは存在しない
            val latestLoginMap = mapOf(1L to record1, 2L to record2)

            val actual = matchUsersWithLoginRecords(users, latestLoginMap)

            val expected = listOf(user1 to record1, user2 to record2)
            assertEquals(expected, actual)
        }

        @Test
        fun getUsersWithLastLogin_統合テスト() {
            val user1 = User(id = 1L, name = "Alice", email = "alice@example.com")
            val user2 = User(id = 2L, name = "Bob", email = "bob@example.com")
            val users = listOf(user1, user2)

            val record1a = LoginRecord(
                userId = 1L, loginAt = OffsetDateTime.parse("2026-01-02T10:15:30+09:00"), ipAddress = "192.168.1.1"
            )
            val record1b = LoginRecord(
                userId = 1L, loginAt = OffsetDateTime.parse("2026-02-07T03:23:45+09:00"), ipAddress = "192.168.1.1"
            )
            val record2 = LoginRecord(
                userId = 2L, loginAt = OffsetDateTime.parse("2026-01-22T10:15:30+09:00"), ipAddress = "192.168.1.2"
            )
            val loginRecords = listOf(record1a, record1b, record2)

            val actual = getUsersWithLastLogin(users, loginRecords)

            val expected = listOf(
                UserWithLastLogin(userId = 1L, lastLoginAt = OffsetDateTime.parse("2026-02-07T03:23:45+09:00")),
                UserWithLastLogin(userId = 2L, lastLoginAt = OffsetDateTime.parse("2026-01-22T10:15:30+09:00"))
            )
            assertEquals(expected, actual)
        }
    }

    @Nested
    inner class `2パターン目` {
        @Test
        fun ふたつのリストをもらって生成する() {

            val expect = listOf(
                UserWithLastLogin(
                    userId = 1L,
                    lastLoginAt = OffsetDateTime.parse("2026-01-02T10:15:30+09:00")
                )
            )

            val users = listOf(User(id = 1L, name = "", email = ""))
            val loginRecords = listOf(
                LoginRecord(
                    userId = 1L, loginAt = OffsetDateTime.parse("2026-01-02T10:15:30+09:00"), ipAddress = ""
                )
            )
            val actual = getUsersWithLastLogin2(users, loginRecords)
            assertEquals(expect, actual)
        }

        /*
        * usersが         loginRecordsが
        *  - 単一の場合     単一の場合
        *  - 単一の場合     複数の場合
        *  - 単一の場合     空の場合
        *  - 複数の場合     単一の場合
        *  - 複数の場合     複数の場合
        *  - 複数の場合     空の場合
        *  - 空の場合      単一の場合
        *  - 空の場合      複数の場合
        *  - 空の場合      空の場合
        *
        * 大体9パターン
        *
        * */
        @Test
        fun userIdが一致するものだけ生成する() {

            val users = listOf(
                User(id = 2L, name = "", email = ""),
                User(id = 3L, name = "", email = ""),
                User(id = 4L, name = "", email = ""),
            )
            val loginRecords = listOf(
                LoginRecord(
                    userId = 1L, loginAt = OffsetDateTime.parse("2026-01-01T10:15:30+09:00"), ipAddress = ""
                ),
                LoginRecord(
                    userId = 2L, loginAt = OffsetDateTime.parse("2026-02-01T10:15:30+09:00"), ipAddress = ""
                ),
                LoginRecord(
                    userId = 4L, loginAt = OffsetDateTime.parse("2026-04-01T10:15:30+09:00"), ipAddress = ""
                ),
            )
            val actual = getUsersWithLastLogin2(users, loginRecords)

            val expect = listOf(
                UserWithLastLogin(
                    userId = 2L, lastLoginAt = OffsetDateTime.parse("2026-02-01T10:15:30+09:00")
                ),
                UserWithLastLogin(
                    userId = 4L, lastLoginAt = OffsetDateTime.parse("2026-04-01T10:15:30+09:00")
                ),
            )

            assertEquals(expect, actual)
        }

        @Test
        fun userIdが一致するものだけを生成２() {
            val user1 = mockk<User>()
            val user2 = mockk<User>()
            val user3 = mockk<User>()
            val users = listOf(user1, user2, user3)

            val loginRecords = mockk<List<LoginRecord>>()

            val userWithLastLogin1 = mockk<UserWithLastLogin>()
            val userWithLastLogin3 = mockk<UserWithLastLogin>()
            every { loginRecords.logins(user1) } returns userWithLastLogin1
            every { loginRecords.logins(user2) } returns null
            every { loginRecords.logins(user3) } returns userWithLastLogin3

            val actual = getUsersWithLastLogin2(users, loginRecords)

            val expect = listOf(
                userWithLastLogin1,
                userWithLastLogin3,
            )
            assertEquals(expect, actual)
        }
    }
}
