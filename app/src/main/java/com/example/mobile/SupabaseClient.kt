package com.example.mobile

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

object SupabaseClient {
    // Replace with your Supabase project URL and anon key
    private val supabase= createSupabaseClient(
        supabaseUrl = "https://xzmltjlyybkuuidcaexi.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh6bWx0amx5eWJrdXVpZGNhZXhpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzA4OTYwODQsImV4cCI6MjA4NjQ3MjA4NH0.pDA8xTfRvsu6LLWo9O6iDlIPdnEqAd-w1YfJVatzgQQ"
    ){
        install(Postgrest)
    }

    val postgrest: Postgrest = supabase.postgrest

}
