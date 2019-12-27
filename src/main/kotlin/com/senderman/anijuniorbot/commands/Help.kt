package com.senderman.anijuniorbot.commands

import com.senderman.anijuniorbot.AnijuniorBotHandler
import com.senderman.neblib.CommandExecutor
import org.telegram.telegrambots.meta.api.objects.Message

class Help(
    private val handler: AnijuniorBotHandler,
    private val executors: Map<String, CommandExecutor>
) : CommandExecutor {
    override val command: String
        get() = "/help"
    override val desc: String
        get() = "помощь"
    override val showInHelp: Boolean
        get() = false

    override fun execute(message: Message) {
        val text = StringBuilder("Привет! Я - бот для чятика анникома! Вот мои команды:\n\n")
        for ((cmd, executor) in executors) {
            if (!executor.showInHelp)
                continue

            val helpline = "$cmd - ${executor.desc}"
            text.append(helpline)
        }
        handler.sendMessage(message.chatId, text.toString())
    }
}