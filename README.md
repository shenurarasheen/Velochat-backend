# VeloChat Backend - REST API 🔧

This repository contains the **backend REST API** for the **VeloChat** React Native messaging app. It handles authentication, messaging, and real-time communication for the mobile client.

> This is a **NetBeans IDE** project built with **Apache Ant**.

---

## 🛠️ Tech Stack

- **Language**: Java
- **API Framework**: Java Servlets (REST)
- **Real-Time**: Java API for WebSocket
- **ORM**: Hibernate
- **Database**: MySQL
- **Application Server**: GlassFish Server
- **Build Tool**: Apache Ant
- **IDE**: NetBeans

---

## 🚀 Getting Started

### **Prerequisites**

- [Java Development Kit (JDK 17+)](https://www.oracle.com/java/technologies/downloads/)
- [NetBeans IDE](https://netbeans.apache.org/)
- [GlassFish Server](https://glassfish.org/)
- [MySQL Server](https://www.mysql.com/)
- [Ngrok](https://ngrok.com/) (Optional, for exposing the local server to the mobile app during testing)

### **1. Database Setup**

1. Create a MySQL database named `velochat_db`.
2. Configure your database credentials in the Hibernate configuration file (e.g. `src/main/resources/hibernate.cfg.xml`).

### **2. Open & Build the Project**

1. Open the project in **NetBeans IDE**.
2. Let NetBeans resolve dependencies via the included **Ant** build script (`build.xml`).
3. Build the project: **Run → Build Project** (or `ant build` from the command line).

### **3. Deploy**

1. Add **GlassFish Server** as a server instance in NetBeans (if not already configured).
2. Deploy the project to GlassFish: **Run → Deploy Project** (or `ant deploy`).

### **4. (Optional) Expose Locally via Ngrok**

To let the React Native app connect to your local backend during development:

```bash
ngrok http 8080
```

Use the generated Ngrok URL as the API/WebSocket host in the frontend app's `.env` file.

---

## 🔗 Related Project

- **Frontend**: VeloChat React Native (Expo) mobile app — connects to this backend via REST API and WebSocket.

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).
