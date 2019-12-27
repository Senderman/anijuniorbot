package com.senderman.anijuniorbot

import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.BotModule
import com.annimon.tgbotsmodule.beans.Config
import com.annimon.tgbotsmodule.services.YamlConfigLoaderService

class AnijuniorBot : BotModule {
    override fun botHandler(config: Config): BotHandler {
        val configLoader = YamlConfigLoaderService<BotConfig>()
        val configFile = configLoader.configFile("botConfigs/anijunior", config.profile)
        val botConfig = configLoader.load(configFile, BotConfig::class.java)
        Services.config = botConfig
        return AnijuniorBotHandler()
    }
}