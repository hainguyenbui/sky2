# Party Games Hub 🎲🐺🕵️‍♂️

Welcome to **Party Games Hub** - a web-based platform hosting popular group party board games such as Werewolf, Spyfall, Undercover, and GoDuck. 

Built with **Spring Boot 3** and a mobile-first, dark interface, this application allows players to instantly join games directly from their smartphones via QR code without installing any apps.

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
- **Frontend**: Thymeleaf server-side rendering, CSS3 design tokens, Vanilla JavaScript. No build step.
- **QR Generation**: Google ZXing Library.

### Design system
All styling lives in `src/main/resources/static/css/` — no inline `<style>` blocks and no `style=""`
attributes in templates. Every page pulls in `app.css`; Werewolf pages add their own page-scoped file.

* **Tokens** — `app.css` defines the whole palette, spacing, radius, type scale, z-index layers and
  motion curves as CSS custom properties. Change a token, and every screen follows.
* **Colour rules** — amber (`--accent`) is the *only* colour used for interactive elements. Faction
  colours (wolf / village / neutral) are reserved for encoding information via dots and badges, and
  are never used as button backgrounds. This keeps "what can I tap" separate from "which side is this".
* **Shared head** — `templates/fragments/layout.html` exposes the `pageHead(title)` fragment carrying
  the meta tags, font and stylesheet links. Note the name: a fragment called `head` would collide with
  the `<head>` element in Thymeleaf's fragment selector.
* **Accessibility floor** — 44px minimum tap targets, ≥4.5:1 text contrast, pinch-zoom left enabled,
  and a `prefers-reduced-motion` fallback for every animation.

### Interaction layer (`static/js/ui.js`)

There are no `alert()` or `confirm()` calls anywhere in the app. Everything goes through one small
global:

| Call | Use it for |
|---|---|
| `UI.toast(message, tone)` | Transient feedback that nobody has to act on |
| `UI.notify({title, message})` | A result the user **must** read (night outcome, revealed role) — returns a Promise, dismissed by hand |
| `UI.confirm({title, message, confirmLabel, tone})` | Asking before a destructive action — returns `Promise<boolean>` |
| `UI.flash({...})` + `UI.reload()` / `UI.go(url)` | A message that has to survive the page reload it triggers |

`tone` is `'info'`, `'success'` or `'danger'`.

Two things worth knowing before you touch this file:

* **Dialogs resolve from the button handler, not the `<dialog>` `close` event.** That event was
  observed not firing on some pages even though `close()` had run and `returnValue` was set, which
  left the promise pending forever. The button already knows the answer — don't reintroduce the
  dependency.
* **`.dialog` needs an explicit `margin: auto`.** The global `* { margin: 0 }` reset wipes out the
  browser's own centering for `<dialog>`, and without it the modal sticks to the top of the screen.

Reveal cards are declarative: put `data-reveal` on the box and `ui.js` wires up the click, keyboard
support and ARIA state. They re-hide themselves when the page is backgrounded, so locking your phone
mid-game doesn't leave your role on screen for the person next to you.

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
   `application.properties` defaults to port **`80`**, which needs elevated privileges on macOS and
   Linux — hence the `--server.port` override above.
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
│   │   │   ├── controller/         # Spring Boot Controllers (page routes & JSON APIs)
│   │   │   ├── service/            # Business Logic & static game data (word lists)
│   │   │   ├── util/               # Helper utilities (device-id cookie)
│   │   │   └── SpyfallApplication  # Main Spring Boot Application starter
│   │   └── resources/
│   │       ├── static/css/         # Design system: app.css + per-page Werewolf styles
│   │       ├── templates/
│   │       │   ├── fragments/      # layout.html - shared pageHead(title) fragment
│   │       │   ├── masoi/          # Werewolf: run, admin, lobby, play, history
│   │       │   ├── spy/ spy2/      # Spyfall & Undercover: setup, lobby, play, list
│   │       │   ├── goduck/         # GoDuck: setup, lobby, play
│   │       │   └── index.html      # Game hub
│   │       ├── picture/            # Image asset directory
│   │       ├── application.properties # Server port configuration
│   │       └── messages.properties
├── pom.xml                         # Maven dependencies config
└── README.md
```

Have fun playing with your friends! 🏁🎉
