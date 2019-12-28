package com.senderman.anijuniorbot.tempobjects

data class SlowUser(
    val userId: Int,
    val time: Int,
    val canSendMessages: Boolean, // 3
    val canSendMediaMessages: Boolean, // 2
    val canSendOtherMessages: Boolean // 1
)