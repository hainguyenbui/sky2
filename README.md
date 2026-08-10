# Party Games Hub 🎲🐺🕵️‍♂️

Welcome to **Party Games Hub** - a web-based platform hosting popular group party board games such as Werewolf, Spyfall, Undercover, and GoDuck. 

Built with **Spring Boot 3** and optimized with a modern mobile-first, dark-mode glassmorphism interface, this application allows players to instantly join games directly from their smartphones via QR code without installing any apps.

---

## 🎮 Supported Games

### 1. 🐺 Werewolf (Ma Sói)
* **Gameplay**: Villagers hunt down the hidden Werewolves among them. A classic social deduction game with a rich set of special roles (Witch, Seer, Bodyguard, Silencer, Assassin, Dictator, Cursed Werewolf, etc.).
* **Features**:
  * Visual Admin/Moderator dashboard to control the game and randomize roles.
  * Supports **Cupid** (romantic coupling) and **Werewolf Curse** (turning a villager into a werewolf during the game).
  * Comprehensive match history logs to review previous rounds. Werewolf history is kept in application memory and is cleared when the process restarts.

### 2. 🕵️ Spyfall (Gián điệp v1)
* **Gameplay**: Most players receive a common location (e.g., Airport, Hospital, Prison...), while the Spy receives a different location or none at all. Players ask each other questions to identify the Spy, while the Spy tries to guess the location.
* **Features**:
  * Integrated secure **Click-to-Reveal Card** to prevent screen-peeping on roles/locations.
  * Suspected locations cheat sheet grid to assist all players during discussions.

### 3. 🤫 Undercover / Spy2 (Gián điệp v2)
* **Gameplay**: Players receive closely related words (e.g., Rice vs. Bread, Wind vs. Tornado). Ordinary citizens receive Word A, Undercovers receive Word B, and White Hats receive a blank card. Players describe their words in turns and vote to eliminate the Undercover/White Hats.
* **Features**:
  * Extensive word lists covering various categories (Animals, Weather, Food, Objects...).
  * Admin dashboard to eliminate players and instantly reveal their roles.

### 4. 🦆 GoDuck
* **Gameplay**: A fast-paced information asymmetry game where players deduce identities based on dynamic visibility rules of other players' IDs.

---

## 🛠️ Tech Stack
- **Backend**: Java 17, Spring Boot 3.3.1, Spring MVC, Lombok.
- **Frontend**: HTML5, CSS3 (Custom Variables, Glassmorphism, Responsive Grid), Vanilla JavaScript.
- **QR Generation**: Google ZXing Library.

---

## 🚀 How to Run the Application

### 1. Local Development
Ensure you have **Java 17** and **Maven** installed on your system.

1. Clone this repository.
2. Open your terminal at the project root and compile:
   ```bash
   mvn clean compile
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8083
   ```
4. The application starts on port **`8083`**. Access the game hub via your browser at:
   * **Game Hub**: [http://localhost:8083](http://localhost:8083)
   * **Werewolf Setup**: [http://localhost:8083/ms/run](http://localhost:8083/ms/run)
   * **Spyfall Setup**: [http://localhost:8083/sp/2/4](http://localhost:8083/sp/2/4)

---

## 🌐 Playing Online via Ngrok

To play with friends remotely without deploying the app to a cloud server, you can tunnel your local server to the internet using **ngrok**:

### 1. Authenticate Ngrok (First time only)
1. Sign up for a free account at [ngrok.com](https://ngrok.com).
2. Retrieve your Authtoken from your ngrok dashboard and run the following command to configure it:
   ```bash
   ngrok config add-authtoken <YOUR_AUTHTOKEN>
   ```

### 2. Expose the Local Server
Start ngrok to forward traffic to your local Spring Boot port (`8083`):
```bash
ngrok http 8083
```
Ngrok will generate a secure public URL (e.g., `https://a1b2-c3d4.ngrok-free.app`). Simply share this URL with your friends to let them join the room and play together immediately!

---

## 📂 Project Structure

```text
sky2-v2/
├── src/
│   ├── main/
│   │   ├── java/com/example/spyfall/
│   │   │   ├── common/             # DTOs & Domain Models
│   │   │   ├── controller/         # Spring Boot Controllers (APIs & raw HTML rendering)
│   │   │   ├── service/            # Business Logic & static game data (word lists)
│   │   │   ├── util/               # Helper utilities (HtmlTemplate wrapper for responsive layout)
│   │   │   └── SpyfallApplication  # Main Spring Boot Application starter
│   │   └── resources/
│   │       ├── picture/            # Image asset directory
│   │       ├── application.properties # Server port configuration (8083)
│   │       └── messages.properties
├── pom.xml                         # Maven dependencies config
└── README.md
```

Have fun playing with your friends! 🏁🎉
