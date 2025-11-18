package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.assertj.core.api.Assertions.assertThat

class PointServiceTest {

    private lateinit var userPointTable: UserPointTable
    private lateinit var pointHistoryTable: PointHistoryTable
    private lateinit var pointService: PointService

    @BeforeEach
    fun setUp() {
        userPointTable = UserPointTable()
        pointHistoryTable = PointHistoryTable()
        pointService = PointService(userPointTable, pointHistoryTable)
    }

    // 초기 데이터가 없을 때 정상적으로 동작하는지 확인
    fun getPoint_shouldReturnZero() {
        val userPoint = pointService.getPoint(1L)

        assertThat(userPoint.id).isEqualTo(1L)
        assertThat(userPoint.point).isZero()
    }

    // 포인트를 증가시키고 잔고를 확인, 내역이 기록되는지 확인
    @Test
    fun charge_shouldIncreaseBalance() {
        val updatedPoint = pointService.charge(1L, 1_000L)

        assertThat(updatedPoint.point).isEqualTo(1_000L)
        val histories = pointService.getHistories(1L)
        assertThat(histories).hasSize(1)
        assertThat(histories.first().type).isEqualTo(TransactionType.CHARGE)
        assertThat(histories.first().amount).isEqualTo(1_000L)
    }

    // 포인트 사용 시 잔고를 확인, 내역이 기록되는지 확인
    @Test
    fun use_shouldDecreaseBalance() {
        pointService.charge(1L, 1_000L)

        val updatedPoint = pointService.use(1L, 400L)

        assertThat(updatedPoint.point).isEqualTo(600L)
        val histories = pointService.getHistories(1L)
        assertThat(histories).hasSize(2)
        assertThat(histories.last().type).isEqualTo(TransactionType.USE)
        assertThat(histories.last().amount).isEqualTo(400L)
    }

    // 잔고 부족 시, 사용 거절 되는지 확인
    @Test
    fun use_shouldThrowOnInsufficient() {
        val exception = assertThrows<InsufficientPointException> {
            pointService.use(1L, 100L)
        }

        assertThat(exception.userId).isEqualTo(1L)
        assertThat(exception.requestedAmount).isEqualTo(100L)
    }
}
