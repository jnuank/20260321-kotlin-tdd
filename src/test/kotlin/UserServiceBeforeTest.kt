import org.example.LoginRecord
import org.example.User
import org.example.UserWithLastLogin
import org.example.getUsersWithLastLogin
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class UserLoginRecordTest {
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

        val expected =UserWithLastLogin(
            userId = 25L,
            lastLoginAt = OffsetDateTime.parse("2024-12-10T10:15:30+09:00")
        )
        assertEquals(expected, actual)
    }


    @Test
    fun 各ユーザーの最終ログイン履歴を取得する() {
        val user1 = User(id = 1L, name = "ユーザー1", email = "user1@example.com")
        val user2 = User(id = 2L, name = "ユーザー2", email = "user2@example.com")
        val users = listOf(user1, user2)

        val loginRecords = listOf(
            LoginRecord(
                userId = 1L, loginAt = OffsetDateTime.parse("2026-01-02T10:15:30+09:00"), ipAddress = "192.168.1.1"
            ),
            LoginRecord(
                userId = 2L, loginAt = OffsetDateTime.parse("2026-01-22T10:15:30+09:00"), ipAddress = "192.168.1.2"
            ),
        )

        val actual = getUsersWithLastLogin(users, loginRecords = loginRecords)
        val expected = listOf(
            UserWithLastLogin(
                userId = 1L, lastLoginAt = OffsetDateTime.parse("2026-01-02T10:15:30+09:00"),
            ),
            UserWithLastLogin(
                userId = 2L, lastLoginAt = OffsetDateTime.parse("2026-01-22T10:15:30+09:00"),
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun 各ユーザーの最終ログイン履歴を取得する_各ユーザーに複数レコードある場合() {
        val user1 = User(id = 1L, name = "ユーザー1", email = "user1@example.com")
        val user2 = User(id = 2L, name = "ユーザー2", email = "user2@example.com")
        val users = listOf(user1, user2)

        val loginRecords = listOf(
            LoginRecord(
                userId = 1L, loginAt = OffsetDateTime.parse("2026-01-02T10:15:30+09:00"), ipAddress = "192.168.1.1",
            ),
            LoginRecord(
                userId = 2L, loginAt = OffsetDateTime.parse("2026-01-22T10:15:30+09:00"), ipAddress = "192.168.1.2"
            ),
            LoginRecord(
                userId = 2L, loginAt = OffsetDateTime.parse("2026-03-26T10:15:30+09:00"), ipAddress = "192.168.1.2"
            ),
            LoginRecord(
                userId = 1L, loginAt = OffsetDateTime.parse("2026-02-07T03:23:45+09:00"), ipAddress = "192.168.1.1"
            ),
        )

        val actual = getUsersWithLastLogin(users, loginRecords = loginRecords)
        val expected = listOf(
            UserWithLastLogin(
                userId = 1L, lastLoginAt = OffsetDateTime.parse("2026-02-07T03:23:45+09:00")
            ),
            UserWithLastLogin(
                userId = 2L, lastLoginAt = OffsetDateTime.parse("2026-03-26T10:15:30+09:00")
            )
        )
        assertEquals(expected, actual)
    }

}