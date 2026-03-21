import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
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

        val expected = UserWithLastLogin(
            userId = 25L,
            lastLoginAt = OffsetDateTime.parse("2024-12-10T10:15:30+09:00")
        )
        assertEquals(expected, actual)
    }


    @Test
    fun 各ユーザーの最終ログイン履歴を取得する() = mockkObject(UserWithLastLogin.Companion) {
        val user1 = User(id = 1L, name = "ユーザー1", email = "user1@example.com")
        val user2 = User(id = 2L, name = "ユーザー2", email = "user2@example.com")
        val users = listOf(user1, user2)

        val loginRecord1 = LoginRecord(
            userId = 1L, loginAt = OffsetDateTime.parse("2026-01-02T10:15:30+09:00"), ipAddress = "192.168.1.1"
        )
        val loginRecord2 = LoginRecord(
            userId = 2L, loginAt = OffsetDateTime.parse("2026-01-22T10:15:30+09:00"), ipAddress = "192.168.1.2"
        )
        val loginRecords = listOf(loginRecord1, loginRecord2)

        val userWithLastLogin1 = mockk<UserWithLastLogin>()
        val userWithLastLogin2 = mockk<UserWithLastLogin>()
        every { UserWithLastLogin.from(user1, loginRecord1) } returns userWithLastLogin1
        every { UserWithLastLogin.from(user2, loginRecord2) } returns userWithLastLogin2

        val actual = getUsersWithLastLogin(users, loginRecords)

        val expected = listOf(userWithLastLogin1, userWithLastLogin2)
        assertEquals(expected, actual)
    }

    @Test
    fun 各ユーザーの最終ログイン履歴を取得する_各ユーザーに複数レコードある場合() =
        mockkObject(UserWithLastLogin.Companion) {
            val user1 = User(id = 1L, name = "ユーザー1", email = "user1@example.com")
            val user2 = User(id = 2L, name = "ユーザー2", email = "user2@example.com")
            val users = listOf(user1, user2)
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

            val userWithLastLogin1 = mockk<UserWithLastLogin>()
            val userWithLastLogin2 = mockk<UserWithLastLogin>()
            every { UserWithLastLogin.from(user1, loginRecord1b) } returns userWithLastLogin1
            every { UserWithLastLogin.from(user2, loginRecord2b) } returns userWithLastLogin2

            val actual = getUsersWithLastLogin(users, loginRecords = loginRecords)

            val expected = listOf(userWithLastLogin1, userWithLastLogin2)
            assertEquals(expected, actual)
        }

}