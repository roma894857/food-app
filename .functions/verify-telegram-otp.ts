import { serve } from "https://deno.land/std/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.38.3"

const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!
const SUPABASE_ANON_KEY = Deno.env.get("SUPABASE_ANON_KEY")!
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!

const supabaseAdmin = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY)
const supabasePublic = createClient(SUPABASE_URL, SUPABASE_ANON_KEY)

interface VerifyOtpRequest {
    phone: string;
    code: string;
}

interface VerifyOtpResponse {
    success: boolean;
    session?: {
        access_token: string;
        refresh_token: string;
        expires_in: number;
        token_type: string;
        user?: any;
    };
    message: string;
    expires_at?: string;
}

service.addHttp({ method: "POST", path: "/verify-telegram-otp" }, async (req: Request) => {
    try {
        const body = await req.json() as VerifyOtpRequest;
        const { phone, code } = body;

        if (!phone || !code) {
            return new Response(
                JSON.stringify({ 
                    success: false, 
                    message: "Phone number and OTP code are required" 
                }),
                { status: 400, headers: { "Content-Type": "application/json" } }
            )
        }

        // Validate OTP code
        const { data: otpData, error: otpError } = await supabaseAdmin
            .from("otp_requests")
            .select("code, expires_at, used, user_id")
            .eq("phone", phone)
            .eq("used", false)
            .gt("expires_at", new Date().toISOString())
            .single()

        if (otpError || !otpData) {
            return new Response(
                JSON.stringify({ 
                    success: false, 
                    message: "Invalid or expired OTP code" 
                }),
                { status: 400, headers: { "Content-Type": "application/json" } }
            )
        }

        // Check if OTP matches
        if (otpData.code !== code) {
            // Log failed attempt but don't reveal that code is wrong
            await supabaseAdmin
                .from("otp_requests")
                .update({ attempts: otpData.attempts + 1 })
                .eq("id", otpData.id)

            return new Response(
                JSON.stringify({ 
                    success: false, 
                    message: "Invalid OTP code" 
                }),
                { status: 400, headers: { "Content-Type": "application/json" } }
            )
        }

        // Mark OTP as used
        const { error: updateError } = await supabaseAdmin
            .from("otp_requests")
            .update({ used: true })
            .eq("id", otpData.id)

        if (updateError) {
            console.error("Failed to mark OTP as used:", updateError)
            return new Response(
                JSON.stringify({ 
                    success: false, 
                    message: "Failed to verify OTP" 
                }),
                { status: 500, headers: { "Content-Type": "application/json" } }
            )
        }

        // Generate new session for the user
        // If user doesn't exist, create one
        let { data: userData, error: userError } = await supabaseAdmin
            .from("user_profiles")
            .select("id")
            .eq("phone", phone)
            .single()

        let userId: string

        if (userError || !userData) {
            // Create new user profile
            const { data: newUser, error: createError } = await supabaseAdmin
                .from("user_profiles")
                .insert({
                    phone: phone,
                    created_at: new Date().toISOString(),
                    updated_at: new Date().toISOString()
                })
                .select("id")
                .single()

            if (createError || !newUser) {
                console.error("Failed to create user profile:", createError)
                return new Response(
                    JSON.stringify({ 
                        success: false, 
                        message: "Failed to create user profile" 
                    }),
                    { status: 500, headers: { "Content-Type": "application/json" } }
                )
            }

            userId = newUser.id
        } else {
            userId = userData.id
        }

        // Generate session tokens using Supabase Auth
        const { data: sessionData, error: sessionError } = await supabaseAdmin
            .auth
            .admin
            .generateUserInviteOrMagicLink(
                userId,
                {
                    email: `${phone}@temp.email`, // Temporary email for phone users
                    phone: phone,
                    data: {
                        source: "telegram_otp",
                        phone_verified: true
                    }
                }
            )

        if (sessionError) {
            console.error("Failed to generate session:", sessionError)
            return new Response(
                JSON.stringify({ 
                    success: false, 
                    message: "Failed to generate authentication session" 
                }),
                { status: 500, headers: { "Content-Type": "application/json" } }
            )
        }

        // Alternative approach: Use signInWithPassword if we can set a temporary password
        // For simplicity, we'll return the user session details
        const response: VerifyOtpResponse = {
            success: true,
            session: sessionData?.session,
            message: "OTP verified successfully",
            expires_at: new Date(Date.now() + 60 * 60 * 24 * 7).toISOString() // 7 days
        }

        return new Response(
            JSON.stringify(response),
            { status: 200, headers: { "Content-Type": "application/json" } }
        )
    } catch (error) {
        console.error("Error in verify-telegram-otp:", error)
        return new Response(
            JSON.stringify({ 
                success: false, 
                message: "Internal server error" 
            }),
            { status: 500, headers: { "Content-Type": "application/json" } }
        )
    }
})

serve()