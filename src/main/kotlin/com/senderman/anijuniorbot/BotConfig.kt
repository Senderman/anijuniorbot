package com.senderman.anijuniorbot

import com.fasterxml.jackson.annotation.JsonProperty

class BotConfig {
    @JsonProperty(required = true)
    lateinit var login: String
}