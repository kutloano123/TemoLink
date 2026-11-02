package com.example.temolink.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.temolink.data.model.OrderStatus
import com.example.temolink.ui.theme.MutedText
import com.example.temolink.ui.theme.PanelBlue
import com.example.temolink.ui.theme.PrimaryBlue
import com.example.temolink.ui.theme.PrimaryGreen
import com.example.temolink.ui.theme.PrimaryText
import com.example.temolink.ui.theme.SecondaryNavy

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = PanelBlue
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )

            Text(
                text = title,
                fontSize = 12.sp,
                color = MutedText
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = PanelBlue
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = SecondaryNavy,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
            }

            // Simple arrow instead of the missing ChevronRight icon
            Text(
                text = "›",
                fontSize = 28.sp,
                color = MutedText,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun OrderStatusChip(
    status: OrderStatus
) {
    val color = when (status) {
        OrderStatus.PENDING -> Color(0xFFEAB308)
        OrderStatus.CONFIRMED -> PrimaryBlue
        OrderStatus.COMPLETED -> PrimaryGreen
        OrderStatus.CANCELLED -> Color.Red
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = status.name,
            color = color,
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
            style = MaterialTheme.typography.labelSmall
        )
    }
}