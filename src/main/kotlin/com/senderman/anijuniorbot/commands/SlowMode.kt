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

class SlowMode(private val handler: AnijuniorBotHandler) : CommandExecutor {
    private val minInterval: Int = 40
    private val maxInterval: Int = 31622400
    override val command: String
        get() = "/slowmode"
    override val desc: String
        get() = """
            (reply) ограничение кол-ва сообщений в единицу времени (секундах).
            e.g: /slowmode 45 level. Только для админов.
            Минимальный интервал - $minInterval секунд. Максимальный - $maxInterval
            Уровни: 1 - запрет на гифки, стикеры, инлайн ботов
            2 - запрет 1 уровня + аудио/видео/фото/войсы
            3 - полный запрет отправки сообщений
        """.trimIndent()

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
        if (params.size < 3) {
            handler.sendMessage(chatId, "Неверный формат!")
            return
        }
        val time = try {
            val temp = params[1].toInt()
            if (temp < minInterval) minInterval else if (temp > maxInterval) maxInterval else temp
        } catch (e: NumberFormatException) {
            handler.sendMessage(chatId, "Неверный формат!")
            return
        }

        val slowUser: SlowUser = try {
            newSlowUserWithLevel(message.replyToMessage.from.id, time, level = params[2])
        } catch (e: NumberFormatException) {
            handler.sendMessage(chatId, "Неверный формат!")
            return
        } catch (e: Exception) {
            handler.sendMessage(chatId, "Неверный уровень!")
            return
        }

        if (slowUser.userId in handler.slowUsers) {
            handler.sendMessage(chatId, "Юзер и так слоу!")
            return
        }

        if (slowUser.userId in handler.chatAdmins) {
            handler.sendMessage(chatId, "Нельзя рестриктить админов!")
            return
        }

        if (!message.from.canRestrictIn(chatId)) {
            handler.sendMessage(chatId, "У вас нет прав на это!")
            return
        }

        try {
            val request = RestrictChatMember()
                .setChatId(chatId)
                .setUserId(slowUser.userId)
                .setCanSendMessages(slowUser.canSendMessages)
                .setCanSendMediaMessages(slowUser.canSendMediaMessages)
                .setCanSendOtherMessages(slowUser.canSendOtherMessages)
                .setUntilDate((System.currentTimeMillis() / 1000).toInt() + time)
            handler.execute(request)
        } catch (e: TelegramApiException) {
            handler.sendMessage(chatId, "У бота нет прав на это!")
            return
        }
        handler.slowUsers[slowUser.userId] = slowUser
        Services.db.addSlowUser(slowUser)
        handler.sendMessage(chatId, "✅ Слоумод для этого юзера активирован!", message.messageId)
    }

    private fun User.canRestrictIn(chatId: Long) = canRestrictMembers(chatId, this.id)

    /**
     * @param userId - id of user
     * @param level - string, which is number, which represents restriction level
     * @param time - slowmode interval
     * @return SlowUser object with proper restrictions
     * @throws Exception if level is not between 1 and 3
     * @throws NumberFormatException if level cannot be converted to the number
     */
    private fun newSlowUserWithLevel(userId: Int, time: Int, level: String): SlowUser {
        return when (level.toInt()) {
            1 -> SlowUser(
                userId, time,
                canSendMessages = true,
                canSendMediaMessages = true,
                canSendOtherMessages = false
            )
            2 -> SlowUser(
                userId, time,
                canSendMessages = true,
                canSendMediaMessages = false,
                canSendOtherMessages = false
            )
            3 -> SlowUser(
                userId, time,
                canSendMessages = false,
                canSendMediaMessages = false,
                canSendOtherMessages = false
            )
            else -> throw Exception("Неверный уровень!")
        }
    }

    companion object {
        private fun getChatMemberAsJSON(chatId: Long, userId: Int): String {
            val input = URL(
                "https://api.telegram.org/bot${Services.handler.botToken}/" +
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
            val root = JSONObject(json)
            if (!root.has("result")) return false
            val result = root.getJSONObject("result")
            return when {
                result.has("can_restrict_members") -> result.getBoolean("can_restrict_members")
                result.has("status") -> result.getString("status") == "creator"
                else -> false
            }
        }
    }
}