
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

<img width="1080" height="1920" alt="chat_signin" src="https://github.com/user-attachments/assets/5201b854-debd-401c-8580-c3eeb85a4e5b" />

<img width="1080" height="1920" alt="chat_signup" src="https://github.com/user-attachments/assets/457a3210-315b-4059-aa0a-0193d6db5c37" />

<img width="1080" height="1920" alt="chat_chats" src="https://github.com/user-attachments/assets/1656eebd-f768-4edc-ac96-c6c88ed54d39" />

<img width="1080" height="1920" alt="chat_chat" src="https://github.com/user-attachments/assets/73add69f-c979-4966-bc46-582a03e2bcd3" />







