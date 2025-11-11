package com.tcc.tarasulandroid.feature.chat

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.tcc.tarasulandroid.feature.home.model.Contact
import org.junit.Rule
import org.junit.Test

/**
 * Integration tests for ChatScreen.
 * 
 * Note: These are UI tests that verify component integration.
 * Full end-to-end tests require instrumentation tests with DI.
 */
class ChatScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun chatScreen_displaysContactName() {
        // Given
        val contact = Contact(
            id = "test-id",
            name = "Test Contact",
            isOnline = true
        )
        
        // When
        composeTestRule.setContent {
            ChatScreen(
                contact = contact,
                onBackClick = {},
                onProfileClick = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("Test Contact").assertIsDisplayed()
    }
    
    @Test
    fun chatScreen_backButton_triggersCallback() {
        // Given
        var backClicked = false
        val contact = Contact(id = "1", name = "Test", isOnline = false)
        
        // When
        composeTestRule.setContent {
            ChatScreen(
                contact = contact,
                onBackClick = { backClicked = true },
                onProfileClick = {}
            )
        }
        
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        
        // Then
        assert(backClicked)
    }
    
    @Test
    fun chatScreen_profileClick_triggersCallback() {
        // Given
        var profileClicked = false
        val contact = Contact(id = "1", name = "Test User", isOnline = false)
        
        // When
        composeTestRule.setContent {
            ChatScreen(
                contact = contact,
                onBackClick = {},
                onProfileClick = { profileClicked = true }
            )
        }
        
        composeTestRule.onNodeWithText("Test User").performClick()
        
        // Then
        assert(profileClicked)
    }
    
    @Test
    fun chatScreen_hasInputField() {
        // Given
        val contact = Contact(id = "1", name = "Test", isOnline = false)
        
        // When
        composeTestRule.setContent {
            ChatScreen(
                contact = contact,
                onBackClick = {},
                onProfileClick = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("Type a message...").assertExists()
    }
    
    @Test
    fun chatScreen_hasSendButton() {
        // Given
        val contact = Contact(id = "1", name = "Test", isOnline = false)
        
        // When
        composeTestRule.setContent {
            ChatScreen(
                contact = contact,
                onBackClick = {},
                onProfileClick = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithContentDescription("Send").assertExists()
    }
    
    @Test
    fun chatScreen_hasAttachButton() {
        // Given
        val contact = Contact(id = "1", name = "Test", isOnline = false)
        
        // When
        composeTestRule.setContent {
            ChatScreen(
                contact = contact,
                onBackClick = {},
                onProfileClick = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithContentDescription("Attach").assertExists()
    }
}
