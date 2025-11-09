## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `pom.xml`: the folder to maintain dependencies

ItchIoRetrievalProject/
├── src/
│   ├── main/       <-- All of your source code (.java files) goes here        
│   │     ├── Indexer.java     <-- Logic to read CSV and build the Lucene Index
│   │     └── Searcher.java    <-- Logic to handle queries and retrieve results
│   │
│   └── test/              <-- Folder for all unit tests (optional but good practice)
│
├── data/                    <-- Folder to hold your input data
│   └── itchio_games.csv     <-- Your collected dataset (500-1000 documents)
│
├── index/                   <-- Folder where Lucene saves the inverted index
│   └── ... (Lucene files)   <-- This folder should NOT be committed to Git
│
├── .gitignore               <-- File to tell Git which folders/files to ignore
└── pom.xml                  <-- Maven configuration and dependency list (Lucene)

## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).


## To Run
Have Apache Maven Installed via Bash.
Terminal command prompt:
    ' mvn clean compile ' <-- compile and package
Goal,Command
1. Build/Compile (after any code change),  " mvn clean package "
2. Index Data (Run Indexer),    " mvn exec:java -Dexec.mainClass=""main.java.Indexer"" -Dexec.args=""itchio_games.csv"" "
3. Search Index (Run Searcher), " mvn exec:java -Dexec.mainClass=""main.java.Searcher"" "

** disclaimer from Brianna - Searcher is still under construction.
4. What should appear:
    Enter a search query (or type 'quit' to exit):
Query>  


## To Push to Repository
git remote add origin https://github.com/bsw30/2140_Project.git
git branch -M main
git push origin main

## To Pull
git pull --rebase origin main

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `pom.xml`: the folder to maintain dependencies

ItchIoRetrievalProject/
├── src/
│   └── main/       <-- All of your source code (.java files) goes here        
│         ├── Indexer.java     <-- Logic to read CSV and build the Lucene Index
│         └── Searcher.java    <-- Logic to handle queries and retrieve results
│
├── data/                    <-- Folder to hold your input data
│   └── itchio_games.csv     <-- Your collected dataset (500-1000 documents)
│
├── index/                   <-- Folder where Lucene saves the inverted index
│   └── ... (Lucene files)   <-- This folder should NOT be committed to Git
│
├── .gitignore               <-- File to tell Git which folders/files to ignore
└── pom.xml                  <-- Maven configuration and dependency list (Lucene)

## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).


## To Run
Have Apache Maven Installed via Bash.
Terminal command prompt:
    ' mvn clean compile ' <-- compile and package
Goal,Command
1. Build/Compile (after any code change),  " mvn clean package "
2. Index Data (Run Indexer),    " mvn exec:java -Dexec.mainClass=""main.java.Indexer"" -Dexec.args=""itchio_games.csv"" "
3. Search Index (Run Searcher), " mvn exec:java -Dexec.mainClass=""main.java.Searcher"" "

** disclaimer from Brianna - Searcher is still under construction.
4. What should appear:
    Enter a search query (or type 'quit' to exit):
Query>  


## To Push to Repository
git remote add origin https://github.com/bsw30/2140_Project.git
git branch -M main
git push origin main

## To Pull
git pull --rebase origin main
