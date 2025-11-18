import requests
from bs4 import BeautifulSoup
import re
import time
import os

# Number of search result pages to crawl per query keyword.
PAGES_PER_QUERY = 1

def safe_filename(s):
    """Convert keyword into safe file-friendly filename."""
    s = s.lower()
    s = re.sub(r"[^a-z0-9]+", "_", s)  # Replace non-alphanumeric with '_'
    return s.strip("_")


def crawl_itchio(keyword, start_id=1):
    base_url = f"https://itch.io/search?q={keyword}"
    headers = {"User-Agent": "Mozilla/5.0"}

    # Crawl search page
    response = requests.get(base_url, headers=headers)
    if response.status_code != 200:
        print(f"Failed to fetch {keyword}")
        return

    soup = BeautifulSoup(response.text, "html.parser")
    results = soup.select(".game_cell")

    # --- Generate UNIQUE filename using timestamp ---
    timestamp = int(time.time())
    filename_key = safe_filename(keyword)
    output_filename = f"itchio_{filename_key}_{timestamp}.trectext"
    # -------------------------------------------------

    doc_id = start_id

    with open(output_filename, "w", encoding="utf-8") as f:
        for game in results:
            title_tag = game.select_one(".game_title")
            link_tag = game.select_one("a")

            if not title_tag or not link_tag:
                continue

            title = title_tag.text.strip()
            link = link_tag["href"]

            # Step 2: crawling the game detail page
            game_page = requests.get(link, headers=headers)
            game_soup = BeautifulSoup(game_page.text, "html.parser")

            paragraphs = [p.get_text(" ", strip=True) for p in game_soup.select("p")]
            description = " ".join(paragraphs)

            # Text cleaning
            description = re.sub(r"\s+", " ", description)

            # Write TREC format
            f.write("<DOC>\n")
            f.write(f"<DOCNO>{keyword}_{doc_id}</DOCNO>\n")
            f.write(f"<TITLE>{title}</TITLE>\n")
            f.write(f"<TEXT>{description}</TEXT>\n")
            f.write("</DOC>\n\n")

            doc_id += 1
            time.sleep(0.5)  # polite crawling

    print(f"Saved file: {output_filename}")


if __name__ == "__main__":
    # INPUT: keywords
    keywords = ["Minit", "Jan Willem Nijman", "2D Adventure", "Time-Loop"]

# what I've crawled------------------------------------

# # A Short Hike
# keywords = ["A Short Hike", "Claire", "3D Adventure", "Exploration"]V

# # OneShot
# keywords = ["OneShot", "Niko", "RPG", "Meta-Narrative"]V

# # Baba Is You
# keywords = ["Baba Is You", "Baba", "Puzzle", "Rules"]V

# # Celeste Classic
# keywords = ["Celeste Classic", "Madeline", "Platformer", "Pico-8"]V

# # Night in the Woods
# keywords = ["Night in the Woods", "Mae Borowski", "Narrative", "Psychological"]V

# # Doki Doki Literature Club!
# keywords = ["Doki Doki Literature Club!", "Monika", "Visual Novel", "Horror"]V

# # Witch Beam 
# keywords = ["Witch Beam", "Witch Beam", "Developer", "Action"]V

# # Hidden Folks
# keywords = ["Hidden Folks", "Adriaan de Jongh", "Hidden Object", "Hand-Drawn"]V

# # Long Gone Days
# keywords = ["Long Gone Days", "Rourke", "JRPG", "Military"]V

# # Minit
# keywords = ["Minit", "Jan Willem Nijman", "2D Adventure", "Time-Loop"]V

# -----------------------------------------------------

    for kw in keywords:
        crawl_itchio(kw)
