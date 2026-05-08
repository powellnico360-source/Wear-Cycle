package com.chichi.project

import android.app.Application
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

class ProjectApplication : Application() {

    companion object {
        lateinit var supabase: SupabaseClient
    }

    override fun onCreate() {
        super.onCreate()

        supabase = createSupabaseClient(
            supabaseUrl = "https://avnyhctzguimnzezwpzo.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImF2bnloY3R6Z3VpbW56ZXp3cHpvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgxNDM0NjQsImV4cCI6MjA5MzcxOTQ2NH0.hZvKN62YKJbHpjWDBIEh7rAseesSgjhlqL-GL2JKbEE"
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }
}
