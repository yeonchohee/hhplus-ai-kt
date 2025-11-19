package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.springframework.stereotype.Service

@Service
class PointService(
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable,
) {

    fun getPoint(id: Long): UserPoint {
        return userPointTable.selectById(id)
    }

    fun getHistories(id: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(id)
    }

    fun charge(id: Long, amount: Long): UserPoint {
        require(amount > 0) { "충전 금액은 0보다 커야 합니다." }
        val current = userPointTable.selectById(id)
        val updatedPoint = current.point + amount
        val updated = userPointTable.insertOrUpdate(id, updatedPoint)
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, updated.updateMillis)
        return updated
    }

    fun use(id: Long, amount: Long): UserPoint {
        require(amount > 0) { "사용 금액은 0보다 커야 합니다." }
        val current = userPointTable.selectById(id)
        if (current.point < amount) {
            throw InsufficientPointException(id, amount, current.point)
        }
        val updatedPoint = current.point - amount
        val updated = userPointTable.insertOrUpdate(id, updatedPoint)
        pointHistoryTable.insert(id, amount, TransactionType.USE, updated.updateMillis)
        return updated
    }
}
