import glob

output = "merged_itchio_dataset.trectext"

with open(output, "w", encoding="utf-8") as outfile:
    for filename in glob.glob("itchio_*.trectext"):
        with open(filename, "r", encoding="utf-8") as infile:
            outfile.write(infile.read())
            outfile.write("\n")

print("Merged into merged_itchio_dataset.trectext")
