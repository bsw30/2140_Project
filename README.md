# ItchSearch -- Improved Search for Itch.io Games

This project is a Spring Boot + Lucene--based search engine that indexes
and retrieves game descriptions from the itch.io dataset.

The system includes: 
• A Java backend (Lucene index + search service) 
• An HTML/CSS frontend 
• A preprocessing pipeline and indexing step

## Project Structure

itchsearch/
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
├── data/
│   └── index/                # Will contain generated index files after Step 1
├── src/
│   └── main/
│       ├── java/
│       │   └── com/itchsearch/
│       │       ├── controller/       
│       │       │   └── SearchController.java
│       │       ├── model/       
│       │       │   ├── Game.java
│       │       │   └── GameDocument.java
│       │       ├── service/                       # Lucene indexing & search logic           
│       │       │   ├── GameIndexWriter.java
│       │       │   └── GameSearchService.java
│       │       ├── util/                          # Preprocessing helpers
│       │       │   ├── Stemmer.java
│       │       │   ├── TextPreprocessor.java
│       │       │   └── TrectextCorpusReader.java
│       │       ├── IndexBuilderMain.java          # Step 1: Build index
│       │       └── ItchsearchApplication.java     # Step 2: Start backend
│       └── resources/
│           ├── data/
│           │   └── itchio_dataset_full.trectext
│           ├── static/
│           │   ├── index.html
│           │   ├── index-styles.css
│           │   ├── results.html
│           │   └── results-styles.css
│           └── stopwords.txt
├── mvnw                                           # Maven Wrapper (no need to install Maven)
├── mvnw.cmd
├── pom.xml
└── README.md

## How to Run

### 1. Build the project

Windows:

    .\mvnw clean package

macOS/Linux:

    ./mvnw clean package

### 2. Generate index files

    java -cp "target/classes;target/dependency/*" com.itchsearch.IndexBuilderMain

### 3. Start backend

    java -jar target/itchsearch-0.0.1-SNAPSHOT.jar

Backend runs at: http://localhost:8080

### 4. Open frontend

Open: `src/main/resources/static/index.html`

Then enter keywords (e.g., "action", "date") to test search.

## Notes

• No need to install Maven manually (Maven Wrapper included).\
• Requires Java 17 or above.
