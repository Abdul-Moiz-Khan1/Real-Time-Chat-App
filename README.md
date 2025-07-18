
# Real-Time Chat App

An Android chat application built with Kotlin, XML UI, MVVM architecture, and Firebase. This app supports real-time 1-on-1 messaging, file sharing, offline capabilities, and delivering a smooth and modern chat experience.

## Description

This chat module allows users to:

    1. Log in with Gmail or phone number
    2. Chat in real-time
    3. Get push notifications
    4. Sync offline messages
    5. View messages with timestamps and sender info
        
## Technologies Used

    1. Kotlin as Core programming language
    2. XML for UI design
    3. MVVM Architecture for Clean separation of concerns
    4. Firebase Auth for Gmail & Phone login
    5. Firebase Realtime for DB	Live chat data
    6. Coroutines + LiveData for Async & lifecycle-safe updates

## Working
1. User Authentication

        a. Users sign in via Gmail or Phone number using Firebase Authentication.
        b. Secure signup/login flow.
        c. User presence (online/offline) tracked via Firebase onDisconnect() logic.

2. Real-Time Messaging

        a. Messages are sent and received in real-time using Firebase Realtime Database.
        b. UI updates automatically via LiveData/Flow observers.
        c. Each message includes: Text or file, Timestamp and Sender/receiver info

3. Offline Support

        a. Firebase offline persistence enabled.
        b. Messages are cached locally.
        c. If offline:
            Messages appear instantly
            Syncs automatically when connection is restored

4. Push Notifications
Notifications shows:
    
        a. Sender name
        b. Message snippet
        c. Sound/vibration
        d. Tapping a notification opens the specific chat screen.

5. Message Management

    Long-press to delete messages:
    
    For yourself
    
    For both users (if sender)
    
    Updates propagate in real-time to all affected users.

6. UI/UX Highlights
   
    Clean, WhatsApp-style chat layout
    
    Left/right aligned message bubbles
    
    Timestamps under each message

7. Typing indicators, Message delivered/read status

   
# Showcase
<p align="center">
    <img width="1024" height="768" alt="chat_app" src="https://github.com/user-attachments/assets/31a7e3b7-8060-474d-a70f-1f16c2995e9b" />
</p>












