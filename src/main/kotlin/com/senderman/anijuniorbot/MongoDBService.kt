package com.senderman.anijuniorbot

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.exists
import com.senderman.anijuniorbot.tempobjects.SlowUser
import com.senderman.neblib.MongoClientKeeper
import org.bson.Document

class MongoDBService : DBService {
    private val database = MongoClientKeeper.client.getDatabase("anijunior")
    private val users = database.getCollection("users")

    private fun createUser(userId: Int) {
        val doc = users.find(eq("userId", userId)).first()
        if (doc == null)
            users.insertOne(Document("userId", userId))
    }

    override fun addSlowUser(userId: Int, time: Int) {
        createUser(userId)
        users.updateOne(
            eq("userId", userId),
            Document(
                "\$set",
                Document("userId", userId).append("slowtime", time)
            )
        )
    }

    override fun removeSlowUser(userId: Int) {
        users.updateOne(
            eq("userId", userId),
            Document(
                "\$unset",
                Document("slowtime", 0)
            )
        )
    }

    override fun getSlowUsers(): Set<SlowUser> {
        val result = HashSet<SlowUser>()
        for (user in users.find(exists("slowtime", true))) {
            result.add(
                SlowUser(
                    user.getInteger("userId"),
                    user.getInteger("slowtime")
                )
            )
        }
        return result
    }
}