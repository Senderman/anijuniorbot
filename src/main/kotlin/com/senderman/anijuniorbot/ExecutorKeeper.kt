package com.senderman.anijuniorbot

import com.senderman.anijuniorbot.commands.Help
import com.senderman.anijuniorbot.commands.SlowMode
import com.senderman.anijuniorbot.commands.SlowModeOff
import com.senderman.neblib.AbstractExecutorKeeper

class ExecutorKeeper(handler: AnijuniorBotHandler) : AbstractExecutorKeeper() {
    init {
        register(Help(handler, commandExecutors))
        register(SlowMode(handler))
        register(SlowModeOff(handler))
    }
}