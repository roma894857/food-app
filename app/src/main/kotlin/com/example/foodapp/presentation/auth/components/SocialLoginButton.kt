package com.example.foodapp.presentation.auth.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SocialLoginButton(
    text: String,
    iconRes: Int,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            SocialLoginIcon(
                iconRes = iconRes,
                iconColor = textColor
            )
            
            Spacer(modifier = Modifier.padding(horizontal = 12.dp))
            
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Composable
private fun SocialLoginIcon(
    iconRes: Int,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    try {
        Icon(
            painter = IconPainter(iconRes),
            contentDescription = null,
            modifier = modifier.size(24.dp),
            tint = iconColor
        )
    } catch (e: Exception) {
        // Fallback icon if resource not found
        Icon(
            painter = IconPainter.Default,
            contentDescription = "Social login",
            modifier = modifier.size(24.dp),
            tint = iconColor
        )
    }
}

@Composable
private fun Spacer(modifier: Modifier) = Spacer(modifier = modifier)

// Helper functions
package com.example.foodapp.presentation.auth.components

fun interface IconPainter {
    fun paint(): Any
    
    object Default : IconPainter {
        override fun paint() = Any()
    }
}
