import org.example.LoginRecord
import org.example.User
import org.example.UserWithLastLogin
import org.example.latestLoginEachUser
import org.example.matching
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
    fun 各ユーザーの最終ログイン履歴を取得する() {
        val loginRecord1 = LoginRecord(
            userId = 1L, loginAt = OffsetDateTime.parse("2026-01-02T10:15:30+09:00"), ipAddress = "192.168.1.1"
        )
        val loginRecord2 = LoginRecord(
            userId = 2L, loginAt = OffsetDateTime.parse("2026-01-22T10:15:30+09:00"), ipAddress = "192.168.1.2"
        )
        val loginRecords = listOf(loginRecord1, loginRecord2)

        val actual = latestLoginEachUser(loginRecords)

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

        val actual = latestLoginEachUser(loginRecords)

        val expected = mapOf(
            1L to loginRecord1b, 2L to loginRecord2b
        )
        assertEquals(expected, actual)
    }

}