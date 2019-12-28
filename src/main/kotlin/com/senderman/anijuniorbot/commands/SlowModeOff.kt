package com.senderman.anijuniorbot.commands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.anijuniorbot.AnijuniorBotHandler
import com.senderman.anijuniorbot.Services
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.objects.Message

class SlowModeOff(private val handler: AnijuniorBotHandler) : CommandExecutor {
    override val command: String
        get() = "/slowmodeoff"
    override val desc: String
        get() = "(reply) выключение слоумода"

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
        if (!Methods.getMe().call(handler).id.canRestrictIn(chatId)) {
            handler.sendMessage(chatId, "У бота нет прав на это!")
            return
        }
        if (!message.from.id.canRestrictIn(chatId)) {
            handler.sendMessage(chatId, "У вас нет прав на это!")
            return
        }
        val userId = message.replyToMessage.from.id
        if (userId !in handler.slowUsers) {
            handler.sendMessage(chatId, "Этот юзер и так не слоу")
            return
        }

        handler.slowUsers.remove(userId)
        Services.db.removeSlowUser(userId)
        Methods.Administration.restrictChatMember()
            .setChatId(chatId)
            .setUserId(userId)
            .setCanSendMessages(true)
            .setCanSendMediaMessages(true)
            .setCanAddWebPagePreviews(true)
            .setCanSendOtherMessages(true)
            .call(handler)
        handler.sendMessage(chatId, "✅ Этот юзер больше не слоу!")
    }

    private fun Int.canRestrictIn(chatId: Long): Boolean {
        val admins = Methods.getChatAdministrators(chatId).call(handler)
        for (admin in admins) {
            if (admin.user.id == this && admin.canRestrictUsers)
                return true
        }
        return false

    }
}