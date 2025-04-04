
# 🎮 Uno Game Project TEMPLATE
## **🔹 Phase 2 - Backend Development Plan**
### **📌 1. Database Design (MariaDB)**
You'll need tables for users, games, and leaderboards:
- **Users Table** (`users`)
    - `id` (UUID, PK)
    - `username` (VARCHAR, unique)
    - `email` (VARCHAR, unique)
    - `password_hash` (VARCHAR, hashed)
    - `created_at` (TIMESTAMP)

- **Games Table** (`games`)
    - `id` (UUID, PK)
    - `player1_id` (FK → users)
    - `player2_id` (FK → users)
    - `winner_id` (FK → users, nullable)
    - `status` (ENUM: IN_PROGRESS, FINISHED)
    - `created_at` (TIMESTAMP)

- **Leaderboard Table** (`leaderboard`)
    - `user_id` (FK → users)
    - `score` (INT, default: 0)
    - `period` (ENUM: WEEK, MONTH, ALL_TIME)
    - `updated_at` (TIMESTAMP)

- **Password Reset Table** (`password_reset_tokens`)
    - `id` (UUID, PK)
    - `user_id` (FK → users)
    - `token` (VARCHAR, unique)
    - `expires_at` (TIMESTAMP)

---

### **📌 2. API Endpoints (Spring Boot - REST)**
#### **👤 Authentication**
- `POST /register` → Create a new user.
- `POST /login` → Authenticate user (JWT).
- `POST /reset-password` → Send reset link via email.
- `POST /update-password` → Update password using reset token.

#### **🎮 Game Management**
- `POST /start-game` → Initialize a game session.
- `POST /play-card` → Handle a player's move.
- `POST /challenge-wild-draw-four` → Process challenge mechanics.
- `POST /end-game` → Conclude a game and update scores.

#### **📊 Leaderboard**
- `GET /leaderboard?period=week`
- `GET /leaderboard?period=month`
- `GET /leaderboard?period=all-time`

---

### **📌 3. Security & Password Handling**
- **Spring Security & JWT** for user authentication.
- **BCrypt** to store passwords securely.
- **Email-based password reset** with expiring reset tokens.

---

### **📌 4. Testing & Documentation**
- **Unit Tests:** Use `JUnit & Mockito`.
- **API Testing:** Postman collection.
- **Swagger Documentation:** Auto-generate API docs.
- **Git Wiki Documentation:** High-level architecture & setup guides.

---

### **📌 5. Deployment**
- **Local:** Run **MariaDB, Spring Boot & Tomcat** locally.
- **Render:** Deploy backend with auto-handled **Tomcat**.
- **Database:** Use the provided **MariaDB server**.

---

## **🔹 Next Steps**



