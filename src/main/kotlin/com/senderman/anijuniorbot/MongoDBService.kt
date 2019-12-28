package com.senderman.anijuniorbot

import com.mongodb.client.model.Filters.eq
import com.senderman.anijuniorbot.tempobjects.SlowUser
import com.senderman.neblib.MongoClientKeeper
import org.bson.Document

class MongoDBService : DBService {
    private val database = MongoClientKeeper.client.getDatabase("anijunior")
    private val slowUsers = database.getCollection("slowusers")

    override fun addSlowUser(user: SlowUser) {
        slowUsers.insertOne(
            Document()
                .append("userId", user.userId)
                .append("time", user.time)
                .append("3", user.canSendMessages)
                .append("2", user.canSendMediaMessages)
                .append("1", user.canSendOtherMessages)
        )
    }

    override fun removeSlowUser(userId: Int) {
        slowUsers.deleteOne(eq("userId", userId))
    }

    override fun getSlowUsers(): Set<SlowUser> {
        val result = HashSet<SlowUser>()
        for (user in slowUsers.find()) {
            result.add(
                SlowUser(
                    user.getInteger("userId"),
                    user.getInteger("time"),
                    canSendMessages = user.getBoolean("3"),
                    canSendMediaMessages = user.getBoolean("2"),
                    canSendOtherMessages = user.getBoolean("1")
                )
            )
        }
        return result
    }
}