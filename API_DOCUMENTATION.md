# Notepad Application API Documentation

## Overview

The Notepad Application provides a RESTful API for managing notes with user authentication via Firebase. This API allows users to create, read, update, delete, search, and email their notes securely.

**Base URL**: `http://localhost:8080` (Development) / `https://your-backend-url.run.app` (Production)

**Authentication**: Bearer Token (Firebase JWT) required for all endpoints except health checks

## Authentication

All API endpoints (except health endpoints) require Firebase authentication. Include the Firebase JWT token in the Authorization header:

```
Authorization: Bearer <firebase-jwt-token>
```

### Authentication Flow
1. User authenticates with Firebase (Google OAuth or email/password)
2. Frontend receives Firebase JWT token
3. Include token in Authorization header for all API requests
4. Backend validates token and creates/retrieves user automatically

---

## Endpoints

### Health Endpoints

#### GET `/`
**Description**: Root endpoint to check if the application is running

**Authentication**: None required

**Response**:
```json
{
  "message": "Notepad Application is running!",
  "status": "UP"
}
```

#### GET `/health`
**Description**: Health check endpoint

**Authentication**: None required

**Response**:
```json
{
  "status": "UP",
  "service": "notepad-app"
}
```

---

### Authentication Endpoints

#### POST `/api/auth/verify-token`
**Description**: Verify Firebase JWT token and create/update user in database

**Authentication**: Bearer Token required

**Headers**:
```
Authorization: Bearer <firebase-jwt-token>
```

**Response** (Success - 200):
```json
{
  "valid": true,
  "uid": "firebase-user-uid",
  "email": "user@example.com",
  "name": "User Name",
  "userId": 123
}
```

**Response** (Error - 401):
```json
{
  "error": "Invalid token",
  "details": "Token verification failed"
}
```

#### GET `/api/auth/user`
**Description**: Get current authenticated user information

**Authentication**: Bearer Token required

**Response** (Success - 200):
```json
{
  "id": 123,
  "firebaseUid": "firebase-user-uid",
  "email": "user@example.com",
  "name": "User Name",
  "createdAt": "2025-09-15T10:30:00"
}
```

**Response** (Error - 401):
```json
{
  "error": "No authenticated user"
}
```

#### POST `/api/auth/logout`
**Description**: Logout user and clear security context

**Authentication**: Bearer Token required

**Response** (Success - 200):
```json
{
  "message": "Logged out successfully"
}
```

---

### Note Management Endpoints

#### GET `/api/notes`
**Description**: Get all notes for the authenticated user

**Authentication**: Bearer Token required

**Response** (Success - 200):
```json
[
  {
    "id": 1,
    "title": "My First Note",
    "content": "This is the content of my first note",
    "isFavorite": false,
    "createdAt": "2025-09-15T10:30:00",
    "updatedAt": "2025-09-15T10:30:00",
    "userId": 123,
    "userEmail": "user@example.com"
  },
  {
    "id": 2,
    "title": "Important Note",
    "content": "This is an important note",
    "isFavorite": true,
    "createdAt": "2025-09-15T11:00:00",
    "updatedAt": "2025-09-15T11:15:00",
    "userId": 123,
    "userEmail": "user@example.com"
  }
]
```

#### GET `/api/notes/{id}`
**Description**: Get a specific note by ID for the authenticated user

**Authentication**: Bearer Token required

**Parameters**:
- `id` (path parameter): Note ID (Long)

**Response** (Success - 200):
```json
{
  "id": 1,
  "title": "My First Note",
  "content": "This is the content of my first note",
  "isFavorite": false,
  "createdAt": "2025-09-15T10:30:00",
  "updatedAt": "2025-09-15T10:30:00",
  "userId": 123,
  "userEmail": "user@example.com"
}
```

**Response** (Error - 404):
Note not found or doesn't belong to the authenticated user

#### POST `/api/notes`
**Description**: Create a new note for the authenticated user

**Authentication**: Bearer Token required

**Request Body**:
```json
{
  "title": "My New Note",
  "content": "This is the content of my new note",
  "isFavorite": false
}
```

**Validation Rules**:
- `title`: Required, max 200 characters
- `content`: Optional, max 10,000 characters
- `isFavorite`: Optional, defaults to false

**Response** (Success - 201):
```json
{
  "id": 3,
  "title": "My New Note",
  "content": "This is the content of my new note",
  "isFavorite": false,
  "createdAt": "2025-09-15T12:00:00",
  "updatedAt": "2025-09-15T12:00:00",
  "userId": 123,
  "userEmail": "user@example.com"
}
```

**Response** (Error - 400):
```json
{
  "error": "Validation failed",
  "details": "Title is required"
}
```

#### PUT `/api/notes/{id}`
**Description**: Update an existing note for the authenticated user

**Authentication**: Bearer Token required

**Parameters**:
- `id` (path parameter): Note ID (Long)

**Request Body**:
```json
{
  "title": "Updated Note Title",
  "content": "Updated content",
  "isFavorite": true
}
```

**Validation Rules**:
- Same as POST `/api/notes`

**Response** (Success - 200):
```json
{
  "id": 1,
  "title": "Updated Note Title",
  "content": "Updated content",
  "isFavorite": true,
  "createdAt": "2025-09-15T10:30:00",
  "updatedAt": "2025-09-15T12:30:00",
  "userId": 123,
  "userEmail": "user@example.com"
}
```

**Response** (Error - 404):
Note not found or doesn't belong to the authenticated user

#### DELETE `/api/notes/{id}`
**Description**: Delete a note for the authenticated user

**Authentication**: Bearer Token required

**Parameters**:
- `id` (path parameter): Note ID (Long)

**Response** (Success - 204):
No content returned

**Response** (Error - 404):
Note not found or doesn't belong to the authenticated user

---

### Search and Filter Endpoints

#### GET `/api/notes/search`
**Description**: Search notes by keyword in title or content

**Authentication**: Bearer Token required

**Query Parameters**:
- `keyword` (required): Search keyword (String)

**Example**: `/api/notes/search?keyword=important`

**Response** (Success - 200):
```json
[
  {
    "id": 2,
    "title": "Important Note",
    "content": "This is an important note",
    "isFavorite": true,
    "createdAt": "2025-09-15T11:00:00",
    "updatedAt": "2025-09-15T11:15:00",
    "userId": 123,
    "userEmail": "user@example.com"
  }
]
```

#### GET `/api/notes/favorites`
**Description**: Get all favorite notes for the authenticated user

**Authentication**: Bearer Token required

**Response** (Success - 200):
```json
[
  {
    "id": 2,
    "title": "Important Note",
    "content": "This is an important note",
    "isFavorite": true,
    "createdAt": "2025-09-15T11:00:00",
    "updatedAt": "2025-09-15T11:15:00",
    "userId": 123,
    "userEmail": "user@example.com"
  }
]
```

---

### Email Endpoint

#### POST `/api/notes/{id}/send-email`
**Description**: Send a specific note to the authenticated user's email address

**Authentication**: Bearer Token required

**Parameters**:
- `id` (path parameter): Note ID (Long)

**Response** (Success - 200):
```json
{
  "message": "Note sent successfully to your email address",
  "noteId": 1,
  "noteTitle": "My First Note",
  "sentTo": "user@example.com"
}
```

**Response** (Error - 404):
Note not found or doesn't belong to the authenticated user

**Response** (Error - 500):
```json
{
  "error": "Failed to send email",
  "message": "SendGrid service unavailable"
}
```

**Note**: Emails are sent using SendGrid and may take a few moments to arrive. Check spam folder if not received.

---

## Data Models

### NoteRequest (Input)
```json
{
  "title": "string (required, max 200 chars)",
  "content": "string (optional, max 10,000 chars)",
  "isFavorite": "boolean (optional, default: false)"
}
```

### NoteResponse (Output)
```json
{
  "id": "number",
  "title": "string",
  "content": "string",
  "isFavorite": "boolean",
  "createdAt": "ISO 8601 datetime string",
  "updatedAt": "ISO 8601 datetime string",
  "userId": "number",
  "userEmail": "string"
}
```

### User (Auth Response)
```json
{
  "id": "number",
  "firebaseUid": "string",
  "email": "string",
  "name": "string",
  "createdAt": "ISO 8601 datetime string"
}
```

---

## Error Handling

### HTTP Status Codes
- `200 OK`: Request successful
- `201 Created`: Resource created successfully
- `204 No Content`: Resource deleted successfully
- `400 Bad Request`: Invalid request data or validation errors
- `401 Unauthorized`: Authentication required or invalid token
- `404 Not Found`: Resource not found or access denied
- `500 Internal Server Error`: Server error

### Error Response Format
```json
{
  "error": "Error type",
  "message": "Detailed error message",
  "details": "Additional error details (optional)"
}
```

### Common Error Scenarios
1. **Missing or invalid Firebase token**: 401 Unauthorized
2. **Validation errors**: 400 Bad Request with validation details
3. **Resource not found or access denied**: 404 Not Found
4. **Email service failure**: 500 Internal Server Error

---

## Security Notes

1. **Authentication**: All endpoints (except health) require valid Firebase JWT token
2. **Authorization**: Users can only access their own notes
3. **Data Validation**: All input is validated according to defined constraints
4. **CORS**: Configured to allow cross-origin requests from specific origins:
   - Local development: `http://localhost:5173` (default React dev server)
   - Production: Frontend Cloud Run URL (configured via environment variables)
5. **Email Privacy**: Users can only send notes to their own email address

---

## Rate Limiting

Currently, no rate limiting is implemented. In production, consider implementing rate limiting for:
- Email sending endpoints
- Search endpoints
- Authentication endpoints

---

## Testing

Use tools like Postman, curl, or Thunder Client to test the API:

### Example cURL Request
```bash
# Get all notes
curl -X GET "http://localhost:8080/api/notes" \
  -H "Authorization: Bearer YOUR_FIREBASE_JWT_TOKEN" \
  -H "Content-Type: application/json"

# Create a new note
curl -X POST "http://localhost:8080/api/notes" \
  -H "Authorization: Bearer YOUR_FIREBASE_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Note",
    "content": "This is a test note",
    "isFavorite": false
  }'
```
