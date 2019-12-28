package com.senderman.anijuniorbot

import com.senderman.anijuniorbot.tempobjects.SlowUser

interface DBService {

    fun addSlowUser(user: SlowUser)
    fun removeSlowUser(userId: Int)
    fun getSlowUsers(): Set<SlowUser>
}