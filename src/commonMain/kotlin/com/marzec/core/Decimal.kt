package com.marzec.core

expect class Decimal {

    operator fun plus(other: Decimal): Decimal

    operator fun minus(other: Decimal): Decimal

    operator fun times(other: Decimal): Decimal

    operator fun div(other: Decimal): Decimal

}