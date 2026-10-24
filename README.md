# Grow Companion

Grow Companion ist eine datenschutzfreundliche Android-App, mit der du Dünger, Nährlösungen, Samen und die Wachstumsphasen deiner Pflanzen an einem Ort verwaltest. Die App funktioniert komplett offline: kein Konto, keine Cloud, alle Daten bleiben auf deinem Gerät.

## Download

Die fertigen APKs findest du unter [Releases](https://github.com/Suvukaraz/grow_companion/releases/latest):

| Variante | Datei | App-ID | Tabs |
| --- | --- | --- | --- |
| **Grow Companion** (Full) | `grow_companion_v1.5.apk` | `com.suvukaraz.Grow_Companion` | Dünger, Nährlösungen, Zeiten, SamenDB |
| **Grow Helper** (Timer) | `grow_helper_v1.5.apk` | `com.suvukaraz.Grow_Helper` | Zeiten, SamenDB |

Beide Varianten lassen sich parallel installieren. Voraussetzung ist Android 8.0 (API 26) oder neuer.

> **Umstieg von Version 1.2.5 oder älter:** Seit Version 1.5.0 haben die Apps neue App-IDs. Android installiert sie deshalb als neue Apps neben der alten Version, und die Daten werden nicht automatisch übernommen. Exportiere vorher in der alten App unter *Einstellungen → Backup exportieren* ein Backup und importiere es in der neuen App.

## Funktionen

- **Dünger**: Name, Hersteller, Typ (fest oder flüssig), Dichte und N-P-K-Werte speichern. Ein Tipp kopiert die Werte, langes Drücken öffnet die Bearbeitung.
- **Nährlösungen**: eigene Rezepte aus mehreren Düngern zusammenstellen. Die App berechnet den N-P-K-Gehalt der Lösung in % (g pro 100 ml) und zusätzlich in mg/L (ppm). Flüssigdünger werden über ihre Dichte von ml in Gramm umgerechnet.
- **Zeiten**: Pflanzen durch ihre Phasen begleiten (Sämling, Wachstum, Blüte, Trocknung oder eigene Phasen), jeweils mit Tageszähler. Die Phase wird aus einer Liste gewählt, und die nächste Phase ist schon vorausgewählt.
- **Eigene Phasen und Phasen-Farben**: In den Einstellungen legst du eigene Phasen an und gibst jeder Phase eine Farbe.
- **SamenDB**: Samenvorrat mit Anzahl, Auto/Fem, Strain, Breeder, Blütezeit und THC-Gehalt (auch mit Nachkommastellen). Jede Spalte ist sortierbar.
- **Backup & Wiederherstellung**: Alle Daten inklusive Phasen-Farben und eigener Phasen als JSON-Datei exportieren und wieder importieren.
- **Hell- und Dunkelmodus**: Die App folgt der Systemeinstellung.

Dezimalzahlen kannst du mit Komma oder Punkt eingeben.

## Hinweis zur N-P-K-Berechnung

Die Berechnung verwendet die Prozentangaben vom Etikett. Phosphor und Kalium werden auf deutschen Etiketten meist als P₂O₅ und K₂O angegeben. Das Ergebnis bezieht sich dann ebenfalls auf P₂O₅ und K₂O und nicht auf reines P und K.

## Selbst bauen

Voraussetzungen:

- Android Studio mit Android SDK 36
- JDK 11 oder neuer

Repository klonen, in Android Studio öffnen, den Gradle-Sync abwarten und eine Variante bauen:

```bash
./gradlew assembleFullDebug
./gradlew assembleTimerDebug
```

Unter Windows stattdessen `gradlew.bat` verwenden:

```bat
gradlew.bat assembleFullDebug
gradlew.bat assembleTimerDebug
```

Die APKs landen in `app/build/outputs/apk/`.

### Release-Builds signieren

Release-Builds werden mit einem Schlüssel aus der Datei `secrets.properties` im Projektordner signiert. Die Datei wird nicht eingecheckt:

```properties
storeFile=/pfad/zu/deinem/keystore.jks
storePassword=...
keyAlias=...
keyPassword=...
```

Ohne diese Datei entsteht ein unsigniertes Release-APK.

## Technik

- Kotlin und Jetpack Compose mit Material 3
- Room als lokale Datenbank
- Kotlin Serialization für das Backup
- Gradle Version Catalog, Android Gradle Plugin 9 mit KSP
- R8-Optimierung in Release-Builds

## Datenschutz

Grow Companion braucht weder ein Konto noch eine Internetverbindung. Alle Daten liegen in der lokalen Datenbank und den Einstellungen der App. Mach vor dem Deinstallieren oder einem Gerätewechsel ein Backup.

## Projektstatus

Grow Companion ist ein privates Projekt, das aktiv weiterentwickelt wird. Datenmodell, Oberfläche und unterstützte Android-Versionen können sich ändern.

## Lizenz

Bisher ist keine Lizenz festgelegt. Solange keine Lizenz im Repository liegt, behält sich der Autor alle Rechte vor.
