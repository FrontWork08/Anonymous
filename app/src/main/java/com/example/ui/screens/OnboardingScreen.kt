package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RevelaCoral
import com.example.ui.theme.RevelaPurple
import com.example.ui.theme.RevelaYellow

/**
 * Tela 3: Onboarding explicativo em 3 etapas pós-cadastro.
 * Apresenta a mecânica inovadora do Revela de forma amigável e dinâmica.
 */
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }

    val steps = listOf(
        OnboardingStep(
            emoji = "🎭",
            title = "Mensagens Anônimas & Públicas",
            description = "Compartilhe pensamentos sinceros no Feed de forma pública ou receba comentários anônimos de quem quer fazer amizade sincera.",
            color = RevelaYellow
        ),
        OnboardingStep(
            emoji = "💬",
            title = "Conversas Misteriosas",
            description = "Inicie conversas com duas identidades: 💬 Azul para conversa normal e identificada, ou 🎭 Amarelo/Roxo para segredos e anonimato completo.",
            color = RevelaPurple
        ),
        OnboardingStep(
            emoji = "✨",
            title = "O Modo Surpresa!",
            description = "Troque pelo menos 5 mensagens no chat anônimo para desbloquear o botão Revelar. Se ambos aceitarem, as identidades se revelam num grande Match!",
            color = RevelaCoral
        )
    )

    val currentStep = steps[currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .testTag("onboarding_root"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            // Top Bar com pular
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onOnboardingComplete,
                    modifier = Modifier.testTag("skip_button")
                ) {
                    Text("Pular", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            }

            // Conteúdo principal (animado ao mudar página)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = currentStep.emoji,
                    fontSize = 110.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Text(
                    text = currentStep.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = currentStep.color,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentStep.description,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Rodapé com indicadores de página e botão avançar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Indicadores de página
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    steps.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentPage) 18.dp else 10.dp, 10.dp)
                                .padding(horizontal = 2.dp)
                                .background(
                                    color = if (index == currentPage) currentStep.color else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Botão de Avanço
                Button(
                    onClick = {
                        if (currentPage < steps.size - 1) {
                            currentPage++
                        } else {
                            onOnboardingComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("next_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = currentStep.color),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (currentPage == steps.size - 1) "Começar Descoberta!" else "Avançar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (currentStep.color == RevelaYellow) Color.Black else Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private data class OnboardingStep(
    val emoji: String,
    val title: String,
    val description: String,
    val color: Color
)
