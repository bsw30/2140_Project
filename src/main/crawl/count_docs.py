def count_documents(file_path):
    count = 0
    with open(file_path, "r", encoding="utf-8") as f:
        for line in f:
            if "<DOC>" in line:
                count += 1
    return count

if __name__ == "__main__":
    path = "src/main/resources/data/merged_itchio_dataset.trectext"  # ← dataset
    total = count_documents(path)
    print(f"Total documents: {total}")

# For merged_itchio_dataset.trectext
# Total documents: 2679