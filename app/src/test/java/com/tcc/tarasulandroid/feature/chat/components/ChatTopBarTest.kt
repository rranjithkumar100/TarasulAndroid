package com.tcc.tarasulandroid.feature.chat.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.tcc.tarasulandroid.feature.home.model.Contact
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for ChatTopBar component.
 */
class ChatTopBarTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun chatTopBar_displaysContactName() {
        // Given
        val contact = Contact(
            id = "test-id",
            name = "John Doe",
            isOnline = false
        )
        
        // When
        composeTestRule.setContent {
            ChatTopBar(
                contact = contact,
                onBackClick = {},
                onProfileClick = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    }
    
    @Test
    fun chatTopBar_showsOnlineStatus_whenContactIsOnline() {
        // Given
        val contact = Contact(
            id = "test-id",
            name = "Jane Smith",
            isOnline = true
        )
        
        // When
        composeTestRule.setContent {
            ChatTopBar(
                contact = contact,
                onBackClick = {},
                onProfileClick = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("Online").assertIsDisplayed()
    }
    
    @Test
    fun chatTopBar_hidesOnlineStatus_whenContactIsOffline() {
        // Given
        val contact = Contact(
            id = "test-id",
            name = "Bob Johnson",
            isOnline = false
        )
        
        // When
        composeTestRule.setContent {
            ChatTopBar(
                contact = contact,
                onBackClick = {},
                onProfileClick = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("Online").assertDoesNotExist()
    }
    
    @Test
    fun chatTopBar_backButton_triggersOnBackClick() {
        // Given
        var backClicked = false
        val contact = Contact(id = "test-id", name = "Test", isOnline = false)
        
        // When
        composeTestRule.setContent {
            ChatTopBar(
                contact = contact,
                onBackClick = { backClicked = true },
                onProfileClick = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assert(backClicked)
    }
    
    @Test
    fun chatTopBar_profileArea_triggersOnProfileClick() {
        // Given
        var profileClicked = false
        val contact = Contact(id = "test-id", name = "Test User", isOnline = false)
        
        // When
        composeTestRule.setContent {
            ChatTopBar(
                contact = contact,
                onBackClick = {},
                onProfileClick = { profileClicked = true }
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("Test User").performClick()
        assert(profileClicked)
    }
    
    @Test
    fun chatTopBar_displaysFirstLetter_asProfilePicture() {
        // Given
        val contact = Contact(
            id = "test-id",
            name = "Alice",
            isOnline = false
        )
        
        // When
        composeTestRule.setContent {
            ChatTopBar(
                contact = contact,
                onBackClick = {},
                onProfileClick = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("A").assertIsDisplayed()
    }
}
