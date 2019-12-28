package com.senderman.anijuniorbot.commands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.anijuniorbot.AnijuniorBotHandler
import com.senderman.anijuniorbot.Services
import com.senderman.anijuniorbot.tempobjects.SlowUser
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.objects.Message
import java.time.Duration

class SlowMode(private val handler: AnijuniorBotHandler) : CommandExecutor {
    override val command: String
        get() = "/slowmode"
    override val desc: String
        get() = "(reply) ограничение кол-ва сообщений в единицу времени (минутах). /slowmode 5. Только для админов"

    override fun execute(message: Message) {
        val chatId = message.chatId
        if (message.isUserMessage) {
            handler.sendMessage(chatId, "Это нельзя использовать в личке!")
            return
        }
        if (!message.isReply) {
            handler.sendMessage(chatId, "Эта команда используется реплаем!")
            return
        }
        val params = message.text.split("\\s+".toRegex())
        val time: Int
        try {
            time = params[1].toInt()
        } catch (e: NumberFormatException) {
            handler.sendMessage(chatId, "Неверный формат!")
            return
        }

        if (!Methods.getMe().call(handler).id.canRestrictIn(chatId)) {
            handler.sendMessage(chatId, "У бота нет прав на это!")
            return
        }
        if (!message.from.id.canRestrictIn(chatId)) {
            handler.sendMessage(chatId, "У вас нет прав на это!")
            return
        }

        val userId = message.replyToMessage.from.id
        Methods.Administration.restrictChatMember()
            .setChatId(chatId)
            .setUserId(userId)
            .setCanSendMessages(false)
            .forTimePeriod(Duration.ofMinutes(time.toLong()))
            .call(handler)
        handler.slowUsers[userId] = SlowUser(userId, time)
        Services.db.addSlowUser(userId, time)
        handler.sendMessage(chatId, "✅ Слоумод для этого юзера активирован!", message.messageId)
    }

    private fun Int.canRestrictIn(chatId: Long) :Boolean{
        val admins = Methods.getChatAdministrators(chatId).call(handler)
        for(admin in admins){
            if (admin.user.id == this && admin.canRestrictUsers)
                return true
        }
        return false

    }
}