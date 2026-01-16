# Full-Stack-Chatbot

A full-stack AI chatbot platform that allows users to create projects, chat with an AI model, and manage conversation history.
Built with Spring Boot, MongoDB, and Vanilla JavaScript, and integrated with a Large Language Model (LLM) provider.

## 🚀 Live Demo

Frontend (Vercel):
https://full-stack-chatbot-dun.vercel.app/

Backend (Render):
https://full-stack-chatbot-3.onrender.com

## Screenshots

### Signup Page
![Signup Page](images/signup-page.png)

### Login Page
![Login Page](images/login-page.png)

### Dashboard
![Dashboard](images/dashboard.png)

### Chat Interface
![Chat Interface](images/chat-interface.png)

## Features

✅ JWT-based authentication (Register / Login)
✅ AI-powered chat per project
✅ Persistent chat history (MongoDB)
✅ Clean REST API design
✅ Chat History Management
✅ Secure API Endpoints
✅ CORS Configuration
✅ Pluggable LLM provider (extensible architecture)

## Tech Stack

## Frontend

HTML5
CSS3
Vanilla JavaScript
Hosted on Vercel

## Backend

Java 17
Spring Boot 3
Spring Security (JWT)
Spring Data MongoDB
WebClient (for AI API calls)
Hosted on Render

## Database

MongoDB Atlas (Cloud)

## AI / LLM

Groq API (OpenAI-compatible)

Model: mixtral-8x7b-32768

Easily replaceable with other providers (OpenAI / Hugging Face / OpenRouter)