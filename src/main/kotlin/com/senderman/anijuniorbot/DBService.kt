package com.senderman.anijuniorbot

import com.senderman.anijuniorbot.tempobjects.SlowUser

interface DBService {

    fun addSlowUser(userId:Int, time:Int)
    fun removeSlowUser(userId: Int)
    fun getSlowUsers(): Set<SlowUser>
}