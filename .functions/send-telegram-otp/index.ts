/**
 * Edge Function: send-telegram-otp
 * Generates and sends a 6-digit OTP code to a user via Telegram bot
 */

import { serve } from "https://deno.land/std/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.38.3"
import { format } from "https://deno.land/std/datetime/mod.ts"

// Environment variables (set in Supabase project settings)
const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
const TELEGRAM_BOT_TOKEN = Deno.env.get("TELEGRAM_BOT_TOKEN")!
const WEB_APP_URL = Deno.env.get("WEB_APP_URL") || "https://t.me/your_foodapp_bot"

// Initialize Supabase client with service role key for database operations
const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY)

interface SendOtpRequest {
    phone: string
}

interface OtpData {
    id: string
    phone: string
    code: string
    expires_at: string
    used: boolean
}

Deno.serve(async (req) => {
    if (req.method !== 'POST') {
        return new Response('Method not allowed', { status: 405 })
    }

    try {
        // Parse request body
        const body = await req.json() as SendOtpRequest
        const { phone } = body

        if (!phone) {
            return new Response(
                JSON.stringify({ 
                    success: false, 
                    message: "Phone number is required" 
                }),
                { 
                    status: 400, 
                    headers: { "Content-Type": "application/json" } 
                }
            )
        }

        // Validate phone number format (basic validation)
        const phoneRegex = /^[+0-9\s-()]{10,20}$/
        if (!phoneRegex.test(phone.replace(/\s/g, ''))) {
            return new Response(
                JSON.stringify({ 
                    success: false, 
                    message: "Invalid phone number format" 
                }),
                { 
                    status: 400, 
                    headers: { "Content-Type": "application/json" } 
                }
            )
        }

        // Generate secure 6-digit OTP code
        const otp = Math.floor(100000 + Math.random() * 900000).toString()
        const expiresAt = new Date(Date.now() + 5 * 60 * 1000) // 5 minutes from now

        // Store OTP in database with expiration
        const { data: otpData, error: insertError } = await supabase
            .from('otp_requests')
            .insert({
                phone: phone,
                code: otp,
                expires_at: expiresAt.toISOString(),
                used: false,
                created_at: new Date().toISOString()
            })
            .select()
            .single() as { data: OtpData | null, error: any }

        if (insertError || !otpData) {
            console.error('Database insert error:', insertError)
            return new Response(
                JSON.stringify({ 
                    success: false, 
                    message: "Failed to store OTP code" 
                }),
                { 
                    status: 500, 
                    headers: { "Content-Type": "application/json" } 
                }
            )
        }

        console.log(`Generated OTP for ${phone}: ${otp} (expires at ${expiresAt.toISOString()})`)

        // Attempt to send OTP via Telegram
        const telegramResult = await sendOtpViaTelegram(phone, otp)

        // Always return success to the client (even if Telegram sending fails)
        // The OTP is stored in database, client can attempt retrieval manually if needed
        return new Response(
            JSON.stringify({
                success: true,
                phone: phone,
                code: otp, // For development - remove in production!
                message: telegramResult.success 
                    ? `OTP sent successfully to ${phone}`
                    : `OTP generated for ${phone}. You may need to contact support.`
            }),
            { 
                status: 200, 
                headers: { "Content-Type": "application/json" } 
            }
        )

    } catch (error) {
        console.error('Error in send-telegram-otp function:', error)
        return new Response(
            JSON.stringify({ 
                success: false, 
                message: "Internal server error" 
            }),
            { 
                status: 500, 
                headers: { "Content-Type": "application/json" } 
            }
        )
    }
})

/**
 * Send OTP via Telegram bot API
 * Uses deep link approach since chat_id can't be reliably determined from phone
 */
async function sendOtpViaTelegram(phone: string, otp: string): Promise<{ success: boolean, message: string }> {
    try {
        // Create a deep link that opens the bot with the OTP pre-filled
        const deepLink = `${WEB_APP_URL}?start=verify_${otp}`
        
        // Alternative: Send to a notification channel or admin
        const message = `🔐 FoodApp OTP Verification\n\nPhone: ${phone}\nCode: ${otp}\nExpires: 5 minutes\n\nDeep Link: ${deepLink}\n\nThis is an automated message.`
        
        // Send to configured Telegram channel/chat ID
        const telegramApiUrl = `https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage`
        
        const response = await fetch(telegramApiUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                chat_id: 'YOUR_TELEGRAM_CHAT_ID_OR_CHANNEL_ID', // Configure this
                text: message,
                parse_mode: 'Markdown'
            })
        })

        if (!response.ok) {
            const errorText = await response.text()
            console.error('Telegram API error:', errorText)
            
            // Fallback: Store OTP for manual review
            await supabase
                .from('otp_requests')
                .update({ 
                    telegram_status: 'failed',
                    telegram_error: errorText 
                })
                .eq('id', (await supabase.from('otp_requests').select('id').eq('phone', phone).select('id').single()).data?.id)
            
            return { 
                success: false, 
                message: `Failed to send via Telegram: ${errorText}` 
            }
        }

        // Log successful Telegram delivery
        await supabase
            .from('otp_requests')
            .update({ telegram_sent: true })
            .eq('phone', phone)

        return { 
            success: true, 
            message: 'OTP sent successfully via Telegram' 
        }

    } catch (error) {
        console.error('Telegram sending error:', error)
        return { 
            success: false, 
            message: `Technical error sending OTP via Telegram` 
        }
    }
}"