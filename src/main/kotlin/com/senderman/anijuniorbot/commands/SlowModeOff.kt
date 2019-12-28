package com.senderman.anijuniorbot.commands

import com.senderman.anijuniorbot.AnijuniorBotHandler
import com.senderman.anijuniorbot.Services
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

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
        if (!message.from.canRestrictIn(chatId)) {
            handler.sendMessage(chatId, "У вас нет прав на это!")
            return
        }
        val userId = message.replyToMessage.from.id
        if (userId !in handler.slowUsers) {
            handler.sendMessage(chatId, "Этот юзер и так не слоу")
            return
        }

        try {
            val request = RestrictChatMember()
                .setChatId(chatId)
                .setUserId(userId)
                .setCanSendMessages(true)
                .setCanSendMediaMessages(true)
                .setCanAddWebPagePreviews(true)
                .setCanSendOtherMessages(true)
            handler.execute(request)
        } catch (e: TelegramApiException) {
            handler.sendMessage(chatId, "У бота нет прав на это!")
            return
        }
        handler.slowUsers.remove(userId)
        Services.db.removeSlowUser(userId)
        handler.sendMessage(chatId, "✅ Этот юзер больше не слоу!")
    }

    private fun User.canRestrictIn(chatId: Long) = SlowMode.canRestrictMembers(chatId, this.id)
}