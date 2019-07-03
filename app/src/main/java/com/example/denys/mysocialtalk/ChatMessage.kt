package com.example.denys.mysocialtalk

import java.sql.Timestamp

class ChatMessage(val id: String, val text: String, val fromId: String, val toId: String, val time: String, timestamp: Long){
    constructor() : this("","","","","", -1)
}