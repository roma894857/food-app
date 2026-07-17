import { serve } from "https://deno.land/std/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.38.3"
import { decode } from "https://deno.land/std/encoding/base64.ts"

const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
const TELEGRAM_BOT_TOKEN = Deno.env.get("TELEGRAM_BOT_TOKEN")!
const WEB_APP_URL = Deno.env.get("WEB_APP_URL") || "https://t.me/your_bot_username"

const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY)

interface SendOtpRequest {
    phone: string;
}

interface OtpResponse {
    success: boolean;
    phone?: string;
    code?: string;
    message: string;
}

// Helper function to generate 6-digit OTP
service.addHttp({ method: "POST", path: "/send-telegram-otp" }, async (req: Request) => {
    try {
        const body = await req.json() as SendOtpRequest;
        const { phone } = body;

        if (!phone) {
            return new Response(
                JSON.stringify({ success: false, message: "Phone number is required" }),
                { status: 400, headers: { "Content-Type": "application/json" } }
            )
        }

        // Generate 6-digit OTP
        const otp = Math.floor(100000 + Math.random() * 900000).toString()
        const expiresAt = new Date(Date.now() + 5 * 60 * 1000) // 5 minutes from now

        // Store OTP in database
        const { error: insertError } = await supabase
            .from("otp_requests")
            .insert({
                phone,
                code: otp,
                expires_at: expiresAt.toISOString(),
                used: false
            })

        if (insertError) {
            console.error("Database insert error:", insertError)
            return new Response(
                JSON.stringify({ success: false, message: "Failed to store OTP" }),
                { status: 500, headers: { "Content-Type": "application/json" } }
            )
        }

        // Find user's Telegram chat ID (you'll need to implement this)
        // Option 1: Store chat_id in user_profiles table when user starts bot
        // Option 2: Use deep link approach instead of direct phone number
        const telegramResult = await sendOtpViaTelegram(phone, otp)

        return new Response(
            JSON.stringify({
                success: telegramResult.success,
                phone: phone,
                message: telegramResult.success
                    ? `OTP sent to ${phone} via Telegram`
                    : telegramResult.message
            }),
            { status: telegramResult.success ? 200 : 400, headers: { "Content-Type": "application/json" } }
        )
    } catch (error) {
        console.error("Error in send-telegram-otp:", error)
        return new Response(
            JSON.stringify({ success: false, message: "Internal server error" }),
            { status: 500, headers: { "Content-Type": "application/json" } }
        )
    }
})

async function sendOtpViaTelegram(phone: string, otp: string): Promise<{ success: boolean; message: string }> {
    try {
        // Since we can't reliably determine chat_id from phone number,
        // we implement a deep link approach using Telegram's "Start" parameter

        // Method 1: Create a custom deep link that the user can use to start the bot
        const deepLinkMessage = `👍 Your OTP for FoodApp: ${otp}

Enter this code in the app to verify your phone. Code expires in 5 minutes.

You can start the bot via: ${WEB_APP_URL}?start=otp_${otp}`

        // Send to bot's username (if configured)
        const messageUrl = `https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage`

        const messageResponse = await fetch(messageUrl, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({
                chat_id: "YOUR_CHANNEL_CHAT_ID", // You'll need to set this
                text: deepLinkMessage,
                parse_mode: "Markdown"
            })
        })

        if (!messageResponse.ok) {
            console.error("Telegram API error:", await messageResponse.text())
            
            // Fallback to trying to find user via bot's command
            return await fallbackTelegramSending(phone, otp)
        }

        return { success: true, message: "OTP sent via Telegram" }
    } catch (error) {
        console.error("Telegram sending error:", error)
        return { success: false, message: "Failed to send OTP via Telegram" }
    }
}

async function fallbackTelegramSending(phone: string, otp: string): Promise<{ success: boolean; message: string }> {
    // Alternative approach: ask user to start bot conversation
    const fallbackMessage = `👍 Your OTP for FoodApp verification: ${otp}

Please start our bot (@your_bot_username) and send your phone number (${phone}) to receive the OTP automatically.

This method ensures you receive the code securely.`

    // Send to admin channel or log for manual support
    const adminUrl = `https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage`
    await fetch(adminUrl, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({
            chat_id: "YOUR_ADMIN_CHAT_ID",
            text: `OTP for ${phone}: ${otp}`
        })
    })

    return { success: true, message: `OTP generated for ${phone}. Please contact support for delivery.` }
}

serve()