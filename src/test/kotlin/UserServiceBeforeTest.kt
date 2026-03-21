import org.example.LoginRecord
import org.example.User
import org.example.UserWithLastLogin
import org.example.getUsersWithLastLogin
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class UserServiceBeforeTest {
    @Test
    fun 各ユーザーの最終ログイン履歴を取得する() {
        val users = listOf(
            User(
                id = 1L,
                name = "ユーザー1",
                email = "user1@example.com"
            ),
            User(
                id = 2L,
                name = "ユーザー2",
                email = "user2@example.com"
            )
        )

        val loginRecords = listOf(
            LoginRecord(
                userId = 1L,
                loginAt = OffsetDateTime.parse("2026-01-02T10:15:30+09:00"),
                ipAddress = "user1@example.com"
            ),
            LoginRecord(
                userId = 2L,
                loginAt = OffsetDateTime.parse("2026-01-22T10:15:30+09:00"),
                ipAddress = "user2@example.com"
            ),
        )

        val actual = getUsersWithLastLogin(
            users,
            loginRecords = loginRecords
        )
        val expected = listOf(
            UserWithLastLogin(
                user = User(
                    id = 1L,
                    name = "ユーザー1",
                    email = "user1@example.com"
                ),
                lastLoginAt = OffsetDateTime.parse("2026-01-02T10:15:30+09:00"),
                ipAddress = "user1@example.com"
            ),
            UserWithLastLogin(
                user = User(
                    id = 2L,
                    name = "ユーザー2",
                    email = "user2@example.com"
                ),
                lastLoginAt = OffsetDateTime.parse("2026-01-22T10:15:30+09:00"),
                ipAddress = "user2@example.com"
            )
        )
        assertEquals(expected, actual)
    }

}