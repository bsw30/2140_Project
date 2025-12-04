# ItchSearch -- Improved Search for Itch.io Games

This project is a Spring Boot + Lucene--based search engine that indexes
and retrieves game descriptions from the itch.io dataset.

The system includes: \
• A Java backend (Lucene index + search service) \
• An HTML/CSS frontend \
• A preprocessing pipeline and indexing step

## Project Structure
```
2140_Project/
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
├── data/
│   └── index/                # Will contain generated index files after Step 1
├── src/
│   └── main/
│       ├── crawl/
│       |   ├── crawl_itchio.py                   # web crawler for itch.io
│       |   └── merge_trectext.py                 # merge every search dataset
│       ├── java/
│       │   └── com/itchsearch/
│       │       ├── controller/       
│       │       │   └── SearchController.java
│       │       ├── model/       
│       │       │   ├── Game.java
│       │       │   └── GameDocument.java
│       │       ├── service/                       # Lucene indexing & search logic           
│       │       │   └── GameSearchService.java
│       │       ├── util/                          # Preprocessing helpers
│       │       │   ├── Stemmer.java
│       │       │   ├── TextPreprocessor.java
│       │       │   └── TrectextCorpusReader.java
│       │       ├── IndexBuilderMain.java          # Step 1: Build index
│       │       └── ItchsearchApplication.java     # Step 2: Start backend
│       └── resources/
│           ├── data/
│           │   ├── itchio_dataset_full.trectext
│           │   └── merged_itchio_dataset.trectext # New Dataset
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
```

## How to Run

Before running the commands below, open a terminal and cd into the folder that directly contains pom.xml.

### 1. Build the project

Windows:

    .\mvnw clean package

macOS/Linux:

    chmod +x mvnw

    ./mvnw clean package

### 2. Generate index files

Windows:

    java -cp "target/classes;target/dependency/*" com.itchsearch.IndexBuilderMain

macOS/Linux:

    java -cp "target/classes:target/dependency/*" com.itchsearch.IndexBuilderMain

### 3. Start backend

    java -jar target/itchsearch-0.0.1-SNAPSHOT.jar

Backend runs at: http://localhost:8080

### 4. Open frontend

The frontend is a static webpage, so you do not run it through the terminal. \
Instead, simply open the HTML file in a browser. \
Open: `src/main/resources/static/index.html`

Then, based on the current dataset, enter keywords (e.g., "action", "horror", "echoes", "whispers", "pack") to test the search.


## SearchEvaluationTest Run Instructions
./mvnw clean package
java -jar target/itchsearch-0.0.1-SNAPSHOT.jar --evaluation


## Notes

• No need to install Maven manually (Maven Wrapper included).\
• Requires Java 17 or above.

## Generating Custom Data

If you wish to create and index your own personalized dataset, follow these two steps using Python 3:

### 1. Customize Scraping Keywords

To specify which game data to scrape, you need to modify the list of target keywords:

* **File Location:** Open `crawl_itchio.py`.
* **Action:** Edit the `keywords` list located inside the `main` function.

```python
# Example of modification inside crawl_itchio.py
def main():
    # Change this list to your desired search terms
    keywords = ["keyword1", "keyword2", "etc"] 
```
### 2. Run the Data Pipeline
Execute the following two Python scripts sequentially in your terminal to scrape the data and then merge it into the required `.trectext` format for indexing:

#### Run Scraper:

    python3 src/main/crawl/crawl_itchio.py

#### Merge Data Files:

    python3 src/main/crawl/merge_trectext.py

#### Dataset Size

Our merged Itch.io dataset contains **2,679 TREC-formatted documents**. \
Each document corresponds to one game entry retrieved from multiple keyword-based crawling sessions. \
The dataset size exceeds the minimum course requirement (500–1,000 documents), providing sufficient material for retrieval, ranking, and evaluation experiments.

## Indexed Data Samples

This list provides examples of the entities and keywords indexed by the Lucene search engine. The values in the **Keywords** column can be used to test the search functionality and demonstrate data coverage.

| Game / Entity | Core Genre / Category | Relevant Keywords / Entity | Demo Status |
| :--- | :--- | :--- | :--- |
| A Short Hike | 3D Adventure, Exploration | Hawk Peak Provincial Park | ❗ No relevant keywords found |
| OneShot | RPG, Meta-Narrative | surreal puzzle | ✅ Passed |
| Baba Is You | Puzzle, Rules | award-winning puzzle game | ✅ Passed |
| Celeste Classic | Platformer, Pico-8 | Maddy Thorson, Noel Berry | ✅ Passed |
| Night in the Woods | Narrative, Psychological | Mae Borowski | ✅ Passed |
| Doki Doki Literature Club! | Visual Novel, Horror | Monika, Sayori, Natsuki, Yuri | ⚠️ Partial |
| Hidden Folks | Hidden Object, Hand-Drawn | Adriaan de Jongh, crocodiles | ✅ Passed(crocodiles) |
| Long Gone Days | JRPG, Military | Rourke | ✅ Passed |
| Minit | 2D Adventure, Time-Loop | Jan Willem Nijman | ✅ Passed |
| Echoes of the Fey | Woodsy Studio, Visual Novel | Sofya Rykov | ? |
| ~~Witch Beam~~ | ~~Developer, Action~~ | ~~Witch Beam~~ | ~~-~~ |
