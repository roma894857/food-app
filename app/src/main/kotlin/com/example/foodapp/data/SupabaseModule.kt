package com.example.foodapp.data

import android.content.Context
import com.example.foodapp.BuildConfig
import com.russholder.dev.supabase.clients.AndroidSupabaseClient
import com.russholder.dev.supabase.createAndroidClient
import com.russholder.dev.supabase.storage.Storage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.GoogleProvider
import io.github.jan.supabase.auth.providers.FacebookProvider
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseModule {
    private val supabaseClient: SupabaseClient by lazy {
        createAndroidClient(
            baseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            // Configure Auth plugin with providers
            install(Auth) {
                // Configure Google provider
                Google {
                    clientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
                    redirectUrl = "foodapp://oauth/redirect"
                }
                
                // Configure Facebook provider  
                Facebook {
                    appId = BuildConfig.FACEBOOK_APP_ID
                    redirectUrl = "foodapp://oauth/redirect"
                }
                
                // Configure session storage for persistence
                sessionStorage = AndroidSessionStorage(ContextProvider.getContext())
            }
            
            // Configure Postgrest for database operations
            install(Postgrest)
            
            // Configure Storage for file uploads (optional)
            install(Storage)
        }
    }
    
    val client: SupabaseClient = supabaseClient
    
    // Helper function to get Auth instance
    fun getAuth(): Auth = supabaseClient.auth
    
    // Helper function to get Postgrest instance  
    fun getPostgrest(): Postgrest = supabaseClient.postgrest
    
    // Helper function to get Storage instance
    fun getStorage(): Storage = supabaseClient.storage
}

// Android session storage implementation
private class AndroidSessionStorage(private val context: Context) : com.russholder.dev.supabase.storage.SessionStorage {
    companion object {
        private const val PREF_NAME = "supabase_sessions"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }
    
    override suspend fun getItem(key: String): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(key, null)
    }
    
    override suspend fun setItem(key: String, value: String?) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(key, value)
            .apply()
    }
    
    override suspend fun removeItem(key: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(key)
            .apply()
    }
}

// Context provider for Android dependency injection
private object ContextProvider {
    private var context: Context? = null
    
    fun setContext(ctx: Context) {
        context = ctx
    }
    
    fun getContext(): Context {
        return context ?: throw IllegalStateException("Context not initialized")
    }
}

// BuildConfig placeholder (these should be actual values in your gradle properties)
object BuildConfig {
    const val SUPABASE_URL = "https://rbctkrlijlbdfyciagsj.supabase.co"
    const val SUPABASE_ANON_KEY = "Sb_publishable_cmLngPrOAUpCrs-5iNQ2zA_MzHaS0Q6"
    const val GOOGLE_WEB_CLIENT_ID = "your-google-web-client-id-aizaSyBVmdK0o2JQ-xlith5J0KAcDdSpGAPlclY"
    const val FACEBOOK_APP_ID = "your-facebook-app-id"
}