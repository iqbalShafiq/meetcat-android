# MeetCat REST API Documentation

## Overview

This document describes the REST API requirements for the MeetCat social media application. The API follows RESTful principles and uses JSON for request/response bodies.

**Total Endpoints**: 34
**Current Status**: Mock implementation only (no real backend yet)
**Authentication**: Token-based (Bearer token in Authorization header)

---

## Table of Contents

1. [General Specifications](#general-specifications)
2. [Authentication Endpoints](#authentication-endpoints)
3. [User Endpoints](#user-endpoints)
4. [Post Endpoints](#post-endpoints)
5. [Comment Endpoints](#comment-endpoints)
6. [Search History Endpoints](#search-history-endpoints)
7. [Location Endpoints](#location-endpoints)
8. [Data Models](#data-models)
9. [Error Handling](#error-handling)

---

## General Specifications

### Base URL
```
https://api.meetcat.com/v1
```

### Authentication
All authenticated endpoints require Bearer token in the Authorization header:
```
Authorization: Bearer {token}
```

### Pagination
All paginated endpoints follow this standard:

**Query Parameters**:
- `page` (integer, required): 0-based page index (0 = first page)
- `pageSize` (integer, optional): Items per page (default: 20, max: 100)

**Response Format**:
```json
{
  "data": [...],
  "pagination": {
    "page": 0,
    "pageSize": 20,
    "hasMore": true
  }
}
```

**End Detection**: `hasMore = false` when no more items available, or when `data.length < pageSize`

### Response Format
**Success Response**:
```json
{
  "success": true,
  "data": {...}
}
```

**Error Response**:
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human readable error message"
  }
}
```

### HTTP Status Codes
- `200 OK`: Successful request
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid request parameters
- `401 Unauthorized`: Missing or invalid authentication
- `403 Forbidden`: Authenticated but not authorized
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource conflict (e.g., duplicate email)
- `500 Internal Server Error`: Server error

---

## Authentication Endpoints

### 1. Login
```
POST /auth/login
```

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "user123",
    "email": "user@example.com",
    "username": "johndoe",
    "displayName": "John Doe",
    "profileImageUrl": "https://...",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Errors**:
- `401`: Invalid credentials

---

### 2. Register
```
POST /auth/register
```

**Request Body**:
```json
{
  "email": "user@example.com",
  "username": "johndoe",
  "displayName": "John Doe",
  "password": "password123"
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "id": "user123",
    "email": "user@example.com",
    "username": "johndoe",
    "displayName": "John Doe",
    "profileImageUrl": null,
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Validations**:
- Email must be unique
- Username must be unique
- Password minimum 8 characters

**Errors**:
- `409`: Email or username already exists
- `400`: Validation errors

---

### 3. Reset Password
```
POST /auth/reset-password
```

**Request Body**:
```json
{
  "email": "user@example.com"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "message": "Password reset email sent"
  }
}
```

**Errors**:
- `404`: Email not found

---

### 4. Check Authentication Status
```
GET /auth/is-logged-in
```

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "isLoggedIn": true
  }
}
```

---

### 5. Get Current User
```
GET /auth/current-user
```

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "user123",
    "email": "user@example.com",
    "username": "johndoe",
    "displayName": "John Doe",
    "profileImageUrl": "https://..."
  }
}
```

**Errors**:
- `401`: Not authenticated

---

### 6. Logout
```
POST /auth/logout
```

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": null
}
```

**Notes**: Should invalidate the token on server side

---

## User Endpoints

### 1. Get User Posts (Paginated)
```
GET /users/{userId}/posts?page={page}&pageSize={pageSize}
```

**Path Parameters**:
- `userId` (string, required): User ID

**Query Parameters**:
- `page` (integer, required): 0-based page index
- `pageSize` (integer, optional): Items per page (default: 20)

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "post123",
      "userId": "user123",
      "user": {
        "id": "user123",
        "username": "johndoe",
        "displayName": "John Doe",
        "profileImageUrl": "https://...",
        "followersCount": 150,
        "followingCount": 200,
        "postsCount": 50,
        "isFollowing": false
      },
      "caption": "Beautiful sunset!",
      "mediaItems": [
        {
          "type": "image",
          "url": "https://...",
          "thumbnailUrl": "https://...",
          "width": 1080,
          "height": 1350
        }
      ],
      "location": {
        "latitude": -6.2088,
        "longitude": 106.8456,
        "name": "Jakarta, Indonesia",
        "address": "Central Jakarta"
      },
      "lovesCount": 42,
      "commentsCount": 8,
      "repliesCount": 3,
      "isLoved": false,
      "createdAt": 1699123456789
    }
  ],
  "pagination": {
    "page": 0,
    "pageSize": 20,
    "hasMore": true
  }
}
```

---

### 2. Get User Replies (Paginated)
```
GET /users/{userId}/replies?page={page}&pageSize={pageSize}
```

**Path Parameters**:
- `userId` (string, required): User ID

**Query Parameters**:
- `page` (integer, required): 0-based page index
- `pageSize` (integer, optional): Items per page (default: 20)

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "reply123",
      "originalPostId": "post456",
      "originalPost": {
        "id": "post456",
        "userId": "user789",
        "caption": "Original post content",
        "mediaItems": [...],
        "lovesCount": 100,
        "commentsCount": 20,
        "isLoved": false,
        "createdAt": 1699123456789
      },
      "userId": "user123",
      "user": {
        "id": "user123",
        "username": "johndoe",
        "displayName": "John Doe",
        "profileImageUrl": "https://...",
        "isFollowing": false
      },
      "text": "This is my reply to the original post",
      "mediaItems": [
        {
          "type": "image",
          "url": "https://...",
          "width": 800,
          "height": 600
        }
      ],
      "location": null,
      "lovesCount": 15,
      "commentsCount": 3,
      "isLoved": false,
      "createdAt": 1699123456789
    }
  ],
  "pagination": {
    "page": 0,
    "pageSize": 20,
    "hasMore": true
  }
}
```

---

### 3. Get User Loved Items (Paginated)
```
GET /users/{userId}/loved-items?page={page}&pageSize={pageSize}
```

**Path Parameters**:
- `userId` (string, required): User ID

**Query Parameters**:
- `page` (integer, required): 0-based page index
- `pageSize` (integer, optional): Items per page (default: 20)

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "type": "post",
      "post": {
        "id": "post123",
        "userId": "user456",
        "caption": "Loved post",
        "mediaItems": [...],
        "lovesCount": 50,
        "commentsCount": 10,
        "isLoved": true,
        "createdAt": 1699123456789
      }
    },
    {
      "type": "reply",
      "reply": {
        "id": "reply789",
        "originalPostId": "post999",
        "text": "Loved reply",
        "mediaItems": [...],
        "lovesCount": 20,
        "commentsCount": 5,
        "isLoved": true,
        "createdAt": 1699123456789
      }
    }
  ],
  "pagination": {
    "page": 0,
    "pageSize": 20,
    "hasMore": true
  }
}
```

**Notes**: Returns a mix of posts and replies that the user has loved, ordered by love timestamp (most recent first)

---

### 4. Get User Followers (Paginated)
```
GET /users/{userId}/followers?page={page}&pageSize={pageSize}
```

**Path Parameters**:
- `userId` (string, required): User ID

**Query Parameters**:
- `page` (integer, required): 0-based page index
- `pageSize` (integer, optional): Items per page (default: 20)

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "user456",
      "username": "janedoe",
      "displayName": "Jane Doe",
      "bio": "Travel enthusiast",
      "profileImageUrl": "https://...",
      "followersCount": 500,
      "followingCount": 300,
      "postsCount": 120,
      "isFollowing": true,
      "createdAt": 1699123456789
    }
  ],
  "pagination": {
    "page": 0,
    "pageSize": 20,
    "hasMore": true
  }
}
```

---

### 5. Get User Following (Paginated)
```
GET /users/{userId}/following?page={page}&pageSize={pageSize}
```

**Path Parameters**:
- `userId` (string, required): User ID

**Query Parameters**:
- `page` (integer, required): 0-based page index
- `pageSize` (integer, optional): Items per page (default: 20)

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "user789",
      "username": "bobsmith",
      "displayName": "Bob Smith",
      "bio": "Photographer",
      "profileImageUrl": "https://...",
      "followersCount": 800,
      "followingCount": 200,
      "postsCount": 250,
      "isFollowing": true,
      "createdAt": 1699123456789
    }
  ],
  "pagination": {
    "page": 0,
    "pageSize": 20,
    "hasMore": true
  }
}
```

---

### 6. Follow User
```
POST /users/{userId}/follow
```

**Path Parameters**:
- `userId` (string, required): User ID to follow

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": null
}
```

**Errors**:
- `404`: User not found
- `400`: Cannot follow yourself
- `409`: Already following this user

---

### 7. Unfollow User
```
POST /users/{userId}/unfollow
```

**Path Parameters**:
- `userId` (string, required): User ID to unfollow

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": null
}
```

**Errors**:
- `404`: User not found
- `400`: Cannot unfollow yourself
- `409`: Not following this user

---

## Post Endpoints

### 1. Get Explore Feed (Paginated)
```
GET /feed/explore?page={page}&pageSize={pageSize}
```

**Query Parameters**:
- `page` (integer, required): 0-based page index
- `pageSize` (integer, optional): Items per page (default: 20)

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "type": "post",
      "post": {
        "id": "post123",
        "userId": "user123",
        "user": {...},
        "caption": "Explore content",
        "mediaItems": [...],
        "location": {...},
        "lovesCount": 100,
        "commentsCount": 20,
        "repliesCount": 5,
        "isLoved": false,
        "createdAt": 1699123456789
      }
    },
    {
      "type": "reply",
      "reply": {
        "id": "reply456",
        "originalPostId": "post789",
        "originalPost": {...},
        "userId": "user456",
        "user": {...},
        "text": "Reply content",
        "mediaItems": [...],
        "lovesCount": 50,
        "commentsCount": 10,
        "isLoved": true,
        "createdAt": 1699123456789
      }
    }
  ],
  "pagination": {
    "page": 0,
    "pageSize": 20,
    "hasMore": true
  }
}
```

**Notes**:
- Returns a mix of posts and replies for discovery
- Algorithm should prioritize content from users you don't follow
- Should consider engagement metrics and recency

---

### 2. Get Random Posts
```
GET /posts/random?count={count}
```

**Query Parameters**:
- `count` (integer, required): Number of random posts to return (max: 50)

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "post123",
      "userId": "user123",
      "user": {...},
      "caption": "Random post",
      "mediaItems": [...],
      "lovesCount": 42,
      "commentsCount": 8,
      "isLoved": false,
      "createdAt": 1699123456789
    }
  ]
}
```

**Notes**: Used for staggered grid displays. Posts should have varying aspect ratios.

---

### 3. Search Posts (Paginated)
```
GET /posts/search?query={query}&page={page}&pageSize={pageSize}
```

**Query Parameters**:
- `query` (string, required): Search query
- `page` (integer, required): 0-based page index
- `pageSize` (integer, optional): Items per page (default: 20)

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "post123",
      "userId": "user123",
      "user": {...},
      "caption": "Post matching search query",
      "mediaItems": [...],
      "lovesCount": 42,
      "commentsCount": 8,
      "isLoved": false,
      "createdAt": 1699123456789
    }
  ],
  "pagination": {
    "page": 0,
    "pageSize": 20,
    "hasMore": true
  }
}
```

**Search Fields**:
- Post caption (full-text search)
- User username
- User display name
- Case-insensitive matching

---

### 4. Get Nearby Posts
```
GET /posts/nearby?latitude={lat}&longitude={lon}&radiusKm={radius}&limit={limit}
```

**Query Parameters**:
- `latitude` (double, required): Current latitude
- `longitude` (double, required): Current longitude
- `radiusKm` (double, required): Search radius in kilometers
- `limit` (integer, optional): Max results (default: 20, max: 100)

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "post123",
      "userId": "user123",
      "user": {...},
      "caption": "Nearby post",
      "mediaItems": [...],
      "location": {
        "latitude": -6.2088,
        "longitude": 106.8456,
        "name": "Jakarta",
        "address": "Central Jakarta"
      },
      "distance": 1.5,
      "lovesCount": 42,
      "commentsCount": 8,
      "isLoved": false,
      "createdAt": 1699123456789
    }
  ]
}
```

**Notes**:
- Results sorted by distance (closest first)
- `distance` field in kilometers from provided coordinates
- Use Haversine formula for distance calculation

---

### 5. Get Single Post
```
GET /posts/{postId}
```

**Path Parameters**:
- `postId` (string, required): Post ID

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "post123",
    "userId": "user123",
    "user": {...},
    "caption": "Post content",
    "mediaItems": [...],
    "location": {...},
    "lovesCount": 42,
    "commentsCount": 8,
    "repliesCount": 3,
    "isLoved": false,
    "createdAt": 1699123456789
  }
}
```

**Errors**:
- `404`: Post not found

---

### 6. Get Single Reply
```
GET /replies/{replyId}
```

**Path Parameters**:
- `replyId` (string, required): Reply ID

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "reply123",
    "originalPostId": "post456",
    "originalPost": {...},
    "userId": "user123",
    "user": {...},
    "text": "Reply content",
    "mediaItems": [...],
    "location": {...},
    "lovesCount": 15,
    "commentsCount": 3,
    "isLoved": false,
    "createdAt": 1699123456789
  }
}
```

**Errors**:
- `404`: Reply not found

---

### 7. Love Post
```
POST /posts/{postId}/love
```

**Path Parameters**:
- `postId` (string, required): Post ID to love

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "postId": "post123",
    "lovesCount": 43,
    "isLoved": true
  }
}
```

**Errors**:
- `404`: Post not found
- `409`: Already loved this post

---

### 8. Unlove Post
```
POST /posts/{postId}/unlove
```

**Path Parameters**:
- `postId` (string, required): Post ID to unlove

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "postId": "post123",
    "lovesCount": 42,
    "isLoved": false
  }
}
```

**Errors**:
- `404`: Post not found
- `409`: Not loved this post

---

### 9. Love Reply
```
POST /replies/{replyId}/love
```

**Path Parameters**:
- `replyId` (string, required): Reply ID to love

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "replyId": "reply123",
    "lovesCount": 16,
    "isLoved": true
  }
}
```

**Errors**:
- `404`: Reply not found
- `409`: Already loved this reply

---

### 10. Unlove Reply
```
POST /replies/{replyId}/unlove
```

**Path Parameters**:
- `replyId` (string, required): Reply ID to unlove

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "replyId": "reply123",
    "lovesCount": 15,
    "isLoved": false
  }
}
```

**Errors**:
- `404`: Reply not found
- `409`: Not loved this reply

---

## Comment Endpoints

### 1. Get Post Comments
```
GET /posts/{postId}/comments
```

**Path Parameters**:
- `postId` (string, required): Post ID

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "comment123",
      "postId": "post123",
      "userId": "user456",
      "user": {
        "id": "user456",
        "username": "janedoe",
        "displayName": "Jane Doe",
        "profileImageUrl": "https://...",
        "isFollowing": false
      },
      "text": "Great post!",
      "lovesCount": 5,
      "isLoved": false,
      "createdAt": 1699123456789
    }
  ]
}
```

**Notes**: Comments ordered by creation time (oldest first)

---

### 2. Get Reply Comments
```
GET /replies/{replyId}/comments
```

**Path Parameters**:
- `replyId` (string, required): Reply ID

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "comment456",
      "replyId": "reply123",
      "userId": "user789",
      "user": {...},
      "text": "Nice reply!",
      "lovesCount": 3,
      "isLoved": true,
      "createdAt": 1699123456789
    }
  ]
}
```

---

### 3. Love Comment
```
POST /comments/{commentId}/love
```

**Path Parameters**:
- `commentId` (string, required): Comment ID to love

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "commentId": "comment123",
    "lovesCount": 6,
    "isLoved": true
  }
}
```

**Errors**:
- `404`: Comment not found
- `409`: Already loved this comment

---

### 4. Unlove Comment
```
POST /comments/{commentId}/unlove
```

**Path Parameters**:
- `commentId` (string, required): Comment ID to unlove

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "commentId": "comment123",
    "lovesCount": 5,
    "isLoved": false
  }
}
```

**Errors**:
- `404`: Comment not found
- `409`: Not loved this comment

---

## Search History Endpoints

### 1. Get Search History
```
GET /search-history
```

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    "sunset beach",
    "coffee shop",
    "mountain hiking"
  ]
}
```

**Notes**:
- Returns last 10 search queries
- Ordered by recency (most recent first)
- Personal to authenticated user

---

### 2. Save Search Query
```
POST /search-history/save
```

**Headers**: Requires `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "query": "sunset beach"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": null
}
```

**Behavior**:
- If query already exists, move it to top (most recent)
- If history exceeds 10 items, remove oldest
- Empty or whitespace-only queries should not be saved

---

### 3. Delete Search Query
```
DELETE /search-history/{query}
```

**Path Parameters**:
- `query` (string, required): Query string to delete (URL-encoded)

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": null
}
```

**Notes**: URL-encode the query parameter (e.g., "sunset beach" becomes "sunset%20beach")

---

### 4. Clear All Search History
```
DELETE /search-history/all
```

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": null
}
```

---

## Location Endpoints

### 1. Get Current Location
```
GET /location/current
```

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "latitude": -6.2088,
    "longitude": 106.8456,
    "name": "Jakarta",
    "address": "Central Jakarta, Indonesia"
  }
}
```

**Notes**:
- Location should be determined from client device
- This endpoint may not be needed if location is always sent by client
- Alternative: Client sends location data in relevant requests

---

### 2. Get Location Permission Status
```
GET /location/permission-status
```

**Headers**: Requires `Authorization: Bearer {token}`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "granted": true
  }
}
```

**Notes**:
- This is client-side permission status
- May not require server-side implementation
- Can be managed entirely on mobile app

---

### 3. Update Location Permission Status
```
PUT /location/permission-status
```

**Headers**: Requires `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "granted": true
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": null
}
```

**Notes**: Optional endpoint for tracking user permission preferences on server

---

## Data Models

### AuthUser
```json
{
  "id": "string",
  "email": "string",
  "username": "string",
  "displayName": "string",
  "profileImageUrl": "string | null",
  "token": "string"
}
```

### User
```json
{
  "id": "string",
  "username": "string",
  "displayName": "string",
  "bio": "string | null",
  "profileImageUrl": "string | null",
  "followersCount": "integer",
  "followingCount": "integer",
  "postsCount": "integer",
  "isFollowing": "boolean",
  "createdAt": "long (timestamp in milliseconds)"
}
```

### Post
```json
{
  "id": "string",
  "userId": "string",
  "user": "User (embedded)",
  "caption": "string",
  "mediaItems": "MediaItem[]",
  "location": "Location | null",
  "lovesCount": "integer",
  "commentsCount": "integer",
  "repliesCount": "integer",
  "isLoved": "boolean",
  "createdAt": "long (timestamp in milliseconds)"
}
```

### Reply
```json
{
  "id": "string",
  "originalPostId": "string",
  "originalPost": "Post (embedded)",
  "userId": "string",
  "user": "User (embedded)",
  "text": "string",
  "mediaItems": "MediaItem[] | null",
  "location": "Location | null",
  "lovesCount": "integer",
  "commentsCount": "integer",
  "isLoved": "boolean",
  "createdAt": "long (timestamp in milliseconds)"
}
```

### Comment
```json
{
  "id": "string",
  "postId": "string (post or reply ID)",
  "userId": "string",
  "user": "User (embedded)",
  "text": "string",
  "lovesCount": "integer",
  "isLoved": "boolean",
  "createdAt": "long (timestamp in milliseconds)"
}
```

### MediaItem (Polymorphic)
**Image Type**:
```json
{
  "type": "image",
  "url": "string",
  "thumbnailUrl": "string | null",
  "width": "integer",
  "height": "integer"
}
```

**Video Type**:
```json
{
  "type": "video",
  "url": "string",
  "thumbnailUrl": "string | null",
  "duration": "long (milliseconds)",
  "width": "integer",
  "height": "integer"
}
```

### Location
```json
{
  "latitude": "double",
  "longitude": "double",
  "name": "string | null",
  "address": "string | null"
}
```

### FeedItem (Polymorphic)
**Post Item**:
```json
{
  "type": "post",
  "post": "Post"
}
```

**Reply Item**:
```json
{
  "type": "reply",
  "reply": "Reply"
}
```

---

## Error Handling

### Error Response Format
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human readable error message",
    "details": {
      "field": "Additional error details (optional)"
    }
  }
}
```

### Common Error Codes

**Authentication Errors**:
- `AUTH_INVALID_CREDENTIALS`: Invalid email or password
- `AUTH_TOKEN_EXPIRED`: Authentication token has expired
- `AUTH_TOKEN_INVALID`: Invalid authentication token
- `AUTH_UNAUTHORIZED`: User not authenticated

**Validation Errors**:
- `VALIDATION_ERROR`: Request validation failed
- `VALIDATION_EMAIL_INVALID`: Invalid email format
- `VALIDATION_USERNAME_INVALID`: Invalid username format
- `VALIDATION_PASSWORD_WEAK`: Password doesn't meet requirements

**Resource Errors**:
- `RESOURCE_NOT_FOUND`: Requested resource not found
- `RESOURCE_ALREADY_EXISTS`: Resource already exists (duplicate)
- `RESOURCE_CONFLICT`: Operation conflicts with current state

**Permission Errors**:
- `PERMISSION_DENIED`: User doesn't have permission for this action
- `PERMISSION_LOCATION_DENIED`: Location permission not granted

**Server Errors**:
- `INTERNAL_SERVER_ERROR`: Unexpected server error
- `SERVICE_UNAVAILABLE`: Service temporarily unavailable

### Example Error Responses

**Validation Error**:
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed",
    "details": {
      "email": "Email is already registered",
      "password": "Password must be at least 8 characters"
    }
  }
}
```

**Authentication Error**:
```json
{
  "success": false,
  "error": {
    "code": "AUTH_INVALID_CREDENTIALS",
    "message": "Invalid email or password"
  }
}
```

**Not Found Error**:
```json
{
  "success": false,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Post not found"
  }
}
```

---

## Implementation Notes

### Priority Features
1. **Authentication**: Login, Register, Token management
2. **Core Feed**: Explore feed with pagination
3. **User Profiles**: Posts, Followers, Following with pagination
4. **Engagement**: Love/Unlike, Follow/Unfollow
5. **Search**: Post search with pagination and history
6. **Location**: Nearby posts based on geo-coordinates

### Optional Features (Lower Priority)
- Comments (currently mocked but not in UI)
- Replies creation (read-only implementation in current app)
- Advanced search filters
- Real-time notifications

### Performance Considerations
- Implement caching for frequently accessed data (user profiles, posts)
- Use database indexes on searchable fields (username, caption)
- Optimize image delivery with CDN and multiple sizes (thumbnail, medium, full)
- Consider rate limiting on expensive operations (search, nearby posts)
- Use database pagination efficiently (cursor-based or offset-based)

### Security Considerations
- Hash passwords with bcrypt or similar (minimum cost factor 10)
- Validate and sanitize all user inputs
- Implement rate limiting on authentication endpoints (prevent brute force)
- Use HTTPS for all API communication
- Validate file uploads (type, size, dimensions)
- Implement CORS policies appropriately
- Token expiration and refresh token mechanism

### Media Storage
- Store images/videos in object storage (AWS S3, Google Cloud Storage, etc.)
- Generate multiple sizes for responsive delivery
- Implement video transcoding for consistent playback
- Consider maximum file sizes (e.g., 10MB for images, 100MB for videos)

### Database Design Recommendations
- User authentication table (users, tokens)
- Posts table with foreign key to users
- Replies table with foreign key to posts and users
- Comments table with polymorphic relation to posts/replies
- Loves table (user_id, lovable_type, lovable_id) for posts/replies/comments
- Follows table (follower_id, following_id)
- Search history table (user_id, query, created_at)
- Locations table or embedded location in posts

### Testing Recommendations
- Unit tests for authentication logic
- Integration tests for all API endpoints
- Load testing for pagination endpoints
- Security testing for authentication and authorization
- Performance testing for search and nearby posts

---

## Appendix: Endpoints Summary

| Method | Endpoint | Authentication | Pagination | Description |
|--------|----------|----------------|------------|-------------|
| POST | /auth/login | No | No | User login |
| POST | /auth/register | No | No | User registration |
| POST | /auth/reset-password | No | No | Password reset |
| GET | /auth/is-logged-in | Yes | No | Check auth status |
| GET | /auth/current-user | Yes | No | Get current user |
| POST | /auth/logout | Yes | No | User logout |
| GET | /users/{userId}/posts | Yes | Yes | Get user posts |
| GET | /users/{userId}/replies | Yes | Yes | Get user replies |
| GET | /users/{userId}/loved-items | Yes | Yes | Get user loved items |
| GET | /users/{userId}/followers | Yes | Yes | Get user followers |
| GET | /users/{userId}/following | Yes | Yes | Get user following |
| POST | /users/{userId}/follow | Yes | No | Follow user |
| POST | /users/{userId}/unfollow | Yes | No | Unfollow user |
| GET | /feed/explore | Yes | Yes | Get explore feed |
| GET | /posts/random | Yes | No | Get random posts |
| GET | /posts/search | Yes | Yes | Search posts |
| GET | /posts/nearby | Yes | No | Get nearby posts |
| GET | /posts/{postId} | Yes | No | Get single post |
| GET | /replies/{replyId} | Yes | No | Get single reply |
| POST | /posts/{postId}/love | Yes | No | Love post |
| POST | /posts/{postId}/unlove | Yes | No | Unlove post |
| POST | /replies/{replyId}/love | Yes | No | Love reply |
| POST | /replies/{replyId}/unlove | Yes | No | Unlove reply |
| GET | /posts/{postId}/comments | Yes | No | Get post comments |
| GET | /replies/{replyId}/comments | Yes | No | Get reply comments |
| POST | /comments/{commentId}/love | Yes | No | Love comment |
| POST | /comments/{commentId}/unlove | Yes | No | Unlove comment |
| GET | /search-history | Yes | No | Get search history |
| POST | /search-history/save | Yes | No | Save search query |
| DELETE | /search-history/{query} | Yes | No | Delete search query |
| DELETE | /search-history/all | Yes | No | Clear search history |
| GET | /location/current | Yes | No | Get current location |
| GET | /location/permission-status | Yes | No | Get permission status |
| PUT | /location/permission-status | Yes | No | Update permission status |

**Total**: 34 endpoints
**Authenticated**: 31 endpoints
**Paginated**: 8 endpoints

---

## Contact & Feedback

For questions or clarifications about this API specification, please contact the development team.

**Version**: 1.0
**Last Updated**: 2025-11-06
**Status**: Draft - Mock Implementation Only
