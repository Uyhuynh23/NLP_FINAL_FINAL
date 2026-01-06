import onnx

# Load your model
model = onnx.load("model.onnx")

# Clear existing metadata to avoid duplicates
while len(model.metadata_props) > 0:
    model.metadata_props.pop()

# Add all required metadata (matching the working Piper model)
metadata = {
    "has_espeak": "1",
    "n_speakers": "1",
    "sample_rate": "22050",
    "model_type": "vits",
    "comment": "piper",
    "language": "Vietnamese",
    "voice": "vi",
    "noise_scale": "0.667",
    "noise_scale_w": "0.8",
    "length_scale": "1.0",
    "espeak.voice": "vi",
    "num_speakers": "1",
}

for key, value in metadata.items():
    meta = model.metadata_props.add()
    meta.key = key
    meta.value = value

# Save the fixed model
onnx.save(model, "model_fixed.onnx")
print("Model saved with complete metadata!")
print("\nMetadata added:")
for m in model.metadata_props:
    print(f"  {m.key}={m.value}")

