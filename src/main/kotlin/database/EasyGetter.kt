package com.algorithmlx.astragone.database

import org.jetbrains.exposed.sql.Database

interface EasyGetter<GET> {
    fun fromDatabase(db: Database): List<GET>
}
