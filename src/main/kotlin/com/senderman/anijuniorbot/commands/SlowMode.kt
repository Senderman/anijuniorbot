package com.senderman.anijuniorbot.commands

import com.senderman.anijuniorbot.AnijuniorBotHandler
import com.senderman.anijuniorbot.Services
import com.senderman.anijuniorbot.tempobjects.SlowUser
import com.senderman.neblib.CommandExecutor
import org.json.JSONObject
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import java.time.Duration

class SlowMode(private val handler: AnijuniorBotHandler) : CommandExecutor {
    private val minInterval: Int = 5
    override val command: String
        get() = "/slowmode"
    override val desc: String
        get() = "(reply) ограничение кол-ва сообщений в единицу времени (секундах). /slowmode 5." +
                "Только для админов. Минимальный интервал - $minInterval секунд"

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
        val time = try {
            val temp: Int = params[1].toInt()
            if (temp < minInterval) minInterval else temp
        } catch (e: NumberFormatException) {
            handler.sendMessage(chatId, "Неверный формат!")
            return
        }

        if (!message.from.canRestrictIn(chatId)) {
            handler.sendMessage(chatId, "У вас нет прав на это!")
            return
        }

        val userId = message.replyToMessage.from.id
        try {
            val request = RestrictChatMember()
                .setChatId(chatId)
                .setUserId(userId)
                .setCanSendMessages(false)
                .forTimePeriod(Duration.ofSeconds(time.toLong()))
            handler.execute(request)
        } catch (e: TelegramApiException) {
            handler.sendMessage(chatId, "У бота нет прав на это!")
            return
        }
        handler.slowUsers[userId] = SlowUser(userId, time)
        Services.db.addSlowUser(userId, time)
        handler.sendMessage(chatId, "✅ Слоумод для этого юзера активирован!", message.messageId)
    }

    private fun User.canRestrictIn(chatId: Long): Boolean {
        return canRestrictMembers(chatId, this.id)
    }

    companion object {
        private fun getChatMemberAsJSON(chatId: Long, userId: Int): String {
            val input = URL(
                "https://https://api.telegram.org/bot${Services.handler.botToken}/" +
                        "getChatMember?chat_id=$chatId&user_id=$userId"
            ).openConnection().getInputStream()
            val out = ByteArrayOutputStream()
            var length: Int
            val buffer = ByteArray(1024)
            while (input.read(buffer).also { length = it } != -1) {
                out.write(buffer, 0, length)
            }
            input.close()
            return out.toString()
        }

        fun canRestrictMembers(chatId: Long, userId: Int): Boolean {
            val json = try {
                getChatMemberAsJSON(chatId, userId)
            } catch (e: IOException) {
                return false
            }
            return JSONObject(json).getJSONObject("result")?.getBoolean("can_restrict_members") ?: false
        }
    }
}