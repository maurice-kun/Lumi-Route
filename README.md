# LumiRoute

LumiRoute ist eine Navigationslösung für Motorradfahrer, die mithilfe von LEDs im Helm visuelle Navigationsanweisungen liefert. Die Verbindung zwischen Smartphone und Helm erfolgt über Bluetooth, während eine Android-App die Steuerung ermöglicht.

---

## Features

- **Visuelle Navigation**: LEDs im Helm zeigen Richtungsanweisungen (links, rechts, geradeaus, halt).
- **Bluetooth-Integration**: Echtzeitübertragung von Navigationsdaten an das Arduino-Board.

---

## Voraussetzungen

- Android-Smartphone (Version 12 oder höher)
- Arduino-Board (mit HC-05 Bluetooth-Modul)
- Helm mit LED-Integration oder Prototyp
- Android Studio (für Entwickler)

---

## Installation

### Für Nutzer
1. Lade die **LumiRoute-App** (APK-Datei) herunter und installiere sie auf deinem Android-Gerät.
2. Aktiviere Bluetooth und koppel dein Smartphone mit dem HC-05-Modul.
3. Öffne die App, wähle „Helm verbinden“ und plane deine Route.

### Für Entwickler
1. Klone dieses Repository:
2. Öffne das Projekt in Android Studio.
3. Stelle sicher, dass alle Abhängigkeiten korrekt geladen sind.
4. Nutze ein physisches Android-Gerät für Tests (Emulator unterstützt keine Bluetooth-Funktionen).
5. Führe die App aus und debugge bei Bedarf.

---

## Nutzung

1. **Helm verbinden**: Stelle die Bluetooth-Verbindung zwischen deinem Smartphone und dem Arduino-Board her.
2. **Route planen**: Gib dein Ziel in der App ein (Google Maps Integration in Planung).
3. **Navigation starten**: Folge den Navigationsanweisungen, die durch die LEDs im Helm angezeigt werden.

---

## Geplante Features

- Integration der Google Maps API für vollständige Routenplanung.
- Sprachsteuerung für noch einfachere Bedienung.
- Erweiterte Unterstützung für weitere Sprachen und Regionen.

---

## Credits

Während der Entwicklung von LumiRoute hat das folgende Projekt als Inspiration und Grundlage gedient:
- [Arduino Bluetooth LED Control Beispiel](https://github.com/The-Frugal-Engineer/ArduinoBTExampleLEDControl) von The-Frugal-Engineer.

---

Vielen Dank, dass du LumiRoute unterstützt! 🚴‍♂️💡
