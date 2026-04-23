![Java](https://img.shields.io/badge/Java-26+-orange)
![Maven](https://img.shields.io/badge/Maven-4.x-blue)
![Jsoup](https://img.shields.io/badge/Jsoup-HTML_Parsing-green)
![Jackson](https://img.shields.io/badge/Jackson-JSON_Processing-black)
# NBA Data Scraper

Scrape NBA player stats, contracts & headshots from Basketball-Reference and ESPN in one command.

This project gathers data from **Basketball-Reference** (Stats \& Contracts) and the **ESPN Hidden API** (Player Headshots \& Team Logos), combining them into clean, structured JSON files.

## Features

* **Dual-Source Scraping:** Merges HTML table parsing (Jsoup) with hidden JSON API consumption.

* **Resilience \& Anti-Ban System:** Built-in mechanisms to respect server rate limits, including random request delays (Jitter) and Exponential Backoff for retries.

* **Smart Resume:** Automatically tracks scraping progress. If interrupted, the script resumes exactly where it left off without duplicating data.

* **Modern Java Architecture:** Utilizes Java Records for immutable data models and follows clean code principles (separation of concerns, robust exception handling).

* **Automated Data Merging (Read-Merge-Write):** Dynamically updates JSON output files without overwriting previously scraped teams.



## Technologies Used

* **Java 14+** (Uses `Records` for data modeling)

* **Maven** (Dependency management)

* **Jsoup** (HTML parsing and API HTTP requests)

* **Jackson (FasterXML)** (JSON serialization/deserialization)

## Getting Started

### Prerequisites

* Java Development Kit (JDK) 14 or higher.

* Maven installed on your machine.

### Installation

1. Clone the repository:

```bash
git clone https://github.com/julien-croynar/nba-data-scraper.git
```

2. Navigate to the project directory:

```bash
cd nba-data-scraper
```

3. Build the project using Maven:

```bash
mvn clean install
```


## Usage

Run the `Main.java` class from your IDE, or execute the compiled jar.
Upon launching, the interactive console menu will ask you to choose an action:

```
=== WELCOME TO THE NBA DATA SCRAPER ===

What do you want to do?

1 - Fetch team logos

2 - Scrape player data

Choice (1/2):
```

**Option 1: Fetch Team Logos**

Downloads high-quality `.png` logos for all 30 NBA teams into 
`src/main/resources/images/team/`.

**Option 2: Scrape Player Data**

1. Scrapes the roster and contract details for each team.

2. Fetches individual player stats.

3. Downloads the player's official ESPN headshot into `src/main/resources/images/players/{TEAM}/`.

4. Saves the structured data into `src/main/resources/data/players-data.json`.

***Note: If you stop the execution during Option 2, restarting it will prompt you to resume from the last uncompleted team.***

## Output Example (JSON)

Here is an example of how the scraped data is formatted in `players-data.json`:

```json
{
  "name": "Stephen Curry",
  "teamId": "GSW",
  "age": 36,
  "contract": {
    "capHit": 51915615,
    "seasonRemaining": 2,
    "nextSeasonEarning": 55761217,
    "option": "NONE"
  },
  "stats": {
    "points": 26.4,
    "rebound": 4.5,
    "assists": 5.1,
    "gamePlayed": 74,
    "position": "PG"
  },
  "headShotPath": "curryst01.png"
}
```

## Project Structure

```Plaintext
src/main/java/com/yusuke/nbadatascraper/
├── config/        # Global constants, delays, and URLs
├── exception/     # Custom scraping exceptions
├── model/         # Immutable Java Records (Player, Contract, PlayerStats)
├── service/       # Core business logic (TeamScraper, PlayerScraper, EspnImageService)
├── util/          # Helpers (HtmlParser, DataSaver, Jitter)
└── Main.java      # Application entry point
```

## Disclaimer

This tool is for **educational purposes only**. Web scraping should be done responsibly. 
This project is configured by default with conservative delays (***JITTER_MS*** and ***BASE_DELAY_MS***) to mimic human behavior and respect the host servers' resources. 
Do not drastically reduce these delays, or you may face temporary IP bans (HTTP 429).



