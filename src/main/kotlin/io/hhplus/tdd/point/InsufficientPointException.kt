package io.hhplus.tdd.point

class InsufficientPointException(
    val userId: Long,
    val requestedAmount: Long,
    val currentAmount: Long,
) : RuntimeException("포인트가 부족합니다. (userId=$userId, requested=$requestedAmount, current=$currentAmount)")
