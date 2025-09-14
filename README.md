# Notepad Application

A cloud-native notepad application built with Spring Boot and React, featuring Firebase authentication and email integration.

## Architecture

- **Backend**: Spring Boot with PostgreSQL (Supabase)
- **Frontend**: React with Vite
- **Authentication**: Firebase (Google OAuth + Email/Password)
- **Email**: SendGrid integration
- **Deployment**: Google Cloud Run (two-service architecture)

## Local Development

### Prerequisites

- Docker and Docker Compose
- Git

### Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd leopad
   ```

2. **Configure environment variables**
   ```bash
   # Backend configuration
   cp .env.example .env
   # Edit .env with your database, SendGrid, and Firebase credentials

   # Frontend configuration
   cp frontend/.env.example frontend/.env
   # Edit frontend/.env with your Firebase configuration
   ```

3. **Start the application**
   ```bash
   docker-compose up --build -d
   ```

4. **Access the application**
   - Frontend: http://localhost:5173
   - Backend API: http://localhost:8080

5. **Cleaning up**
   ```bash
   # Remove containers and volumes
   docker-compose down -v --remove-orphans
   ```

## Cloud Deployment

### Prerequisites

- Google Cloud CLI installed and authenticated
- Google Cloud Project with billing enabled
- Firebase project configured

### Deploy

1. **Configure cloudbuild.yaml**
   - Update environment variables in the `substitutions` section
   - Set correct Firebase configuration values

2. **Deploy to Cloud Run**
   ```bash
   gcloud builds submit --config cloudbuild.yaml
   ```

3. **Get service URLs**
   ```bash
   gcloud run services list --region=europe-west1
   ```

4. **Update Firebase authorized domains**
   - Add your frontend Cloud Run URL to Firebase Console -> Authentication -> Settings  Authorized domains

## Features

- User authentication (Google OAuth and email/password)
- Create, edit, delete notes
- Mark notes as favorites
- Search notes by title/content
- Email notes to your address
- Responsive web interface

## Project Structure

```
├── backend/          # Spring Boot application
├── frontend/         # React application
├── cloudbuild.yaml   # Google Cloud Build configuration
└── docker-compose.yml # Local development setup
```

## Environment Variables

See `.env.example` and `frontend/.env.example` for required configuration variables.
