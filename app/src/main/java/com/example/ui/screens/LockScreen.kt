package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LockScreen(
    correctPin: String,
    onUnlocked: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "التطبيق مقفل بـ PIN",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "أدخل الرمز السري المكون من 4 أرقام لفتح التطبيق",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Visual dots for entered pin
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                val isFilled = i < enteredPin.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFilled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                )
            }
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Numeric keyboard grid
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("C", "0", "back")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            keys.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    if (key == "C" || key == "back") Color.Transparent
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable {
                                    errorMessage = ""
                                    when (key) {
                                        "C" -> enteredPin = ""
                                        "back" -> if (enteredPin.isNotEmpty()) {
                                            enteredPin = enteredPin.substring(0, enteredPin.length - 1)
                                        }
                                        else -> if (enteredPin.length < 4) {
                                            enteredPin += key
                                            if (enteredPin.length == 4) {
                                                if (enteredPin == correctPin) {
                                                    onUnlocked()
                                                } else {
                                                    errorMessage = "الرمز السري غير صحيح، حاول مجدداً"
                                                    enteredPin = ""
                                                }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (key == "back") {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Backspace",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            } else {
                                Text(
                                    text = key,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
