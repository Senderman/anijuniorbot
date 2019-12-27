package com.senderman.anijuniorbot

import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.api.methods.Methods
import com.annimon.tgbotsmodule.api.methods.send.SendMessageMethod
import com.senderman.anijuniorbot.tempobjects.SlowUser
import com.senderman.neblib.AbstractExecutorKeeper
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.*
import kotlin.collections.HashMap

class AnijuniorBotHandler internal constructor() : BotHandler() {
    private val executorKeeper: AbstractExecutorKeeper
    val slowUsers: MutableMap<Int, SlowUser>

    init {
        executorKeeper = ExecutorKeeper(this)
        Services.db = MongoDBService()
        slowUsers = HashMap()
        for (slowpoke in Services.db.getSlowUsers()) {
            slowUsers[slowpoke.userId] = slowpoke
        }
    }

    override fun onUpdate(update: Update): BotApiMethod<*>? {

        if (!update.hasMessage())
            return null

        val message = update.message

        val chatId = message.chatId
        if (!message.isUserMessage && chatId != -1001123020018L) {
            sendMessage(chatId, "Бот в этом чатике не работает!")
            Methods.leaveChat(chatId).call(this)
            return null
        }

        slowUsers[message.from.id]?.let { user ->
            Methods.Administration.restrictChatMember()
                .setChatId(chatId)
                .setUserId(user.userId)
                .setCanSendMessages(false)
                .setUntilDateInSeconds(user.time * 60)
                .call(this)
        }

        if (!message.hasText())
            return null

        val text = message.text
        /* bot should only trigger on general commands (like /command) or on commands for this bot (/command@mybot),
         * and NOT on commands for another bots (like /command@notmybot)
         */
        val command = text.split("\\s+".toRegex(), 2)[0]
            .toLowerCase(Locale.ENGLISH)
            .replace("@$botUsername", "")
        if ("@" in command)
            return null

        executorKeeper.findExecutor(command)?.execute(message)

        return null
    }

    fun sendMessage(chatId: Long, text: String, replyToMessageId: Int? = null): Message {
        val sm = SendMessageMethod()
            .setChatId(chatId)
            .setText(text)
            .setReplyToMessageId(replyToMessageId)
        return sendMessage(sm)
    }

    fun sendMessage(sm: SendMessageMethod): Message {
        return with(sm) {
            enableHtml()
            enableWebPagePreview()
            call(this@AnijuniorBotHandler)
        }
    }

    override fun getBotUsername(): String {
        return Services.config.login.split(" ".toRegex(), 2)[0]
    }

    override fun getBotToken(): String {
        return Services.config.login.split(" ".toRegex(), 2)[1]
    }
}