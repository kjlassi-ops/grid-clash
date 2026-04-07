# Grid Clash — Tic-Tac-Toe Android

Jeu de Morpion Android moderne avec :
- **Mode Solo** contre un bot (Facile / Moyen / Difficile avec Minimax)
- **Multijoueur local Wi-Fi** sans Internet ni serveur cloud (TCP LAN)
- Design cyberpunk neon inspiré du design Stitch

---

## Déploiement 100% cloud (sans rien installer)

### Étape 1 — Créer le dépôt GitHub

1. Va sur **github.com** → connecte-toi ou crée un compte gratuit
2. Clique **"New repository"**
3. Nom : `grid-clash`
4. Visibilité : **Private** (recommandé — ton code reste privé, GitHub Actions donne 2 000 min/mois gratuites)
5. **Ne coche rien** (pas de README, pas de .gitignore)
6. Clique **"Create repository"**

### Étape 2 — Uploader le code

GitHub te propose directement "upload files" sur le repo vide :

1. Sur la page de ton nouveau repo, clique **"uploading an existing file"**
2. Glisse-dépose **tout le contenu** du dossier `PlayGrid/`
3. En bas, écris un message de commit : `Initial commit — Grid Clash MVP`
4. Clique **"Commit changes"**

> **Important :** Tu dois uploader en conservant l'arborescence des dossiers.
> Utilise l'upload par dossier ou zippe le projet et dézipe dans l'interface.

### Étape 3 — Lancer le build

1. Dans ton repo GitHub, clique sur l'onglet **"Actions"**
2. Tu vois le workflow **"Build & Release APK"** — il se lance automatiquement
3. Attends 3-5 minutes que le build se termine (indicateur vert ✅)

### Étape 4 — Télécharger et installer l'APK

**Option A — Via les Releases (recommandé) :**
1. Dans ton repo, clique sur **"Releases"** (colonne de droite)
2. Tu vois `Grid Clash Build #1`
3. Sous "Assets", télécharge `app-debug.apk`

**Option B — Via les Artifacts :**
1. Onglet **"Actions"** → clique sur le build
2. Tout en bas, sous "Artifacts" → télécharge `GridClash-debug-1`
3. Dézipe pour obtenir l'APK

**Installer sur Android :**
1. Envoie le fichier APK sur ton téléphone (WhatsApp, email, Google Drive...)
2. Ouvre-le depuis le téléphone
3. Si demandé : **Paramètres → Sécurité → Sources inconnues** → Activer
4. Installe et joue !

---

## Architecture du projet

```
app/src/main/java/com/gridclash/app/
│
├── GridClashApplication.kt      ← Application + conteneur de dépendances
├── MainActivity.kt              ← Point d'entrée Compose
│
├── core/model/
│   └── GameModels.kt            ← Sealed classes, enums, GameUiState
│
├── game/
│   ├── engine/
│   │   ├── WinChecker.kt        ← Détection victoire/égalité
│   │   └── GameEngine.kt        ← Application des coups
│   └── ai/
│       └── BotAI.kt             ← EasyBot / MediumBot / HardBot (Minimax)
│
├── network/
│   ├── NetworkMessage.kt        ← Protocole JSON sérialisé
│   ├── GameServer.kt            ← TCP ServerSocket (hôte)
│   ├── GameClient.kt            ← TCP Socket (client)
│   └── NetworkRepository.kt    ← Abstraction + utilitaire IP
│
└── ui/
    ├── theme/                   ← Color, Type, Theme (Material3 dark)
    ├── navigation/              ← AppNavGraph + Routes
    ├── home/                    ← HomeScreen
    ├── solo/                    ← SoloSetupScreen
    ├── multiplayer/             ← MultiplayerScreen + ViewModel
    ├── game/                    ← GameScreen + GameViewModel
    ├── rules/                   ← RulesScreen
    └── settings/                ← SettingsScreen
```

## Stack technique

| Composant | Technologie |
|-----------|-------------|
| Langage | Kotlin 1.9 |
| UI | Jetpack Compose + Material3 |
| Navigation | Navigation Compose 2.7 |
| State | StateFlow + collectAsStateWithLifecycle |
| Async | Coroutines (viewModelScope) |
| Réseau LAN | TCP Sockets (java.net) + JSON (kotlinx.serialization) |
| IA Bot | Minimax avec élagage alpha-bêta (Hard) |
| Build | Gradle 8.6 / AGP 8.2.2 |

---

## Tester en multijoueur local

1. Les deux téléphones doivent être sur le **même réseau Wi-Fi**
2. **Téléphone A (hôte)** : Multijoueur → "Héberger" → note l'IP affichée
3. **Téléphone B (client)** : Multijoueur → "Rejoindre" → entre l'IP du téléphone A → "Se connecter"
4. La partie démarre automatiquement quand les deux sont connectés

---

## Roadmap V2

- [ ] Sélection du symbole X/O avant la partie
- [ ] Animations de victoire (confettis, ligne lumineuse)
- [ ] Persistance des scores (DataStore)
- [ ] Grille 4×4 / 5×5 (mode avancé)
- [ ] Chat en jeu (multijoueur)
- [ ] Thème clair / sombre
- [ ] Sons et musique (MediaPlayer)
- [ ] Intégration police Space Grotesk en local
- [ ] APK signé (release) via GitHub Actions + keystore secret
- [ ] Découverte automatique des hôtes par UDP broadcast
