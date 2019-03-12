# ChatMate
This is a group project for a school assigment. 

The goal was to build a chat client and a server in java. There are chatrooms which are created when someone joins them (much like how IRC works). Users can also send private messages (whispers) to other users. 

## Features
* Client starts by prompting a "login screen".
* Chat client shows timestamps on all messages. 
* Server sends a warning message to spammers. 
* Client saves last connected server IP, username and chat history. 
* Client can delete/clear all chat history. 
* Usernames and channelnames are validated on server with regex to block bad language and unwanted characters. 
* User can toggle ignore/unignore on other users. Ignored users are marked as ignored in userlist. 
* Client has two themes: Light and Dark.
* Whispers, channel messages, events, errors/warnings are represented in different colors. 
* A notification sound is played when the client receives a new message (in a channel that is not currently shown). 
* There is an option to mute the sound. 
* Channels with unread messages are marked with a red circle. 
* Only channel messages and whispers trigger the notifications. 
* The server has a console interface where an admin can display different statistics about the server (clients connected, logs, what users are in a specific channel, uptime etc.). 
* All messages are being sent with encryption to prevent outsiders from seing what is being sent.
* The server uses regex to swap bad language in messages for nicer words like "tomato" or "flower" etc. 
* The client tries to reconnect 10 times if the connection to the server is lost (connecting with increasing pauses). 
* An easter egg can be activated by naming yourself "spider", typing "man" into the chat (without sending it) and clicking the logo. 
