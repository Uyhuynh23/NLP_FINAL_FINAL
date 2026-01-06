import json

# Load the phoneme_id_map from your model's JSON file
with open("model.onnx.json", "r", encoding="utf-8") as f:
    data = json.load(f)

phoneme_id_map = data["phoneme_id_map"]

# Create list of (token, id) sorted by id
tokens = []
for phoneme, ids in phoneme_id_map.items():
    for token_id in ids:
        tokens.append((phoneme, token_id))

# Sort by token ID
tokens.sort(key=lambda x: x[1])

# Write tokens.txt
with open("tokens.txt", "w", encoding="utf-8") as f:
    for phoneme, token_id in tokens:
        f.write(f"{phoneme} {token_id}\n")

print(f"Created tokens.txt with {len(tokens)} tokens")
print("First 10 tokens:")
for phoneme, token_id in tokens[:10]:
    print(f"  [{phoneme}] -> {token_id}")
print("Last 10 tokens:")
for phoneme, token_id in tokens[-10:]:
    print(f"  [{phoneme}] -> {token_id}")
