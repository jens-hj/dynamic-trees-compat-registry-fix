from pathlib import Path
from PIL import Image, ImageDraw

root = Path(__file__).resolve().parents[1]
image = Image.new('RGB', (32, 32), '#162a29')
draw = ImageDraw.Draw(image)
draw.rectangle((13, 13, 17, 26), fill='#ad784b')
draw.rectangle((6, 7, 24, 15), fill='#66bb77')
draw.rectangle((10, 3, 21, 19), fill='#66bb77')
draw.rectangle((5, 27, 26, 28), fill='#406f58')
draw.line([(19, 23), (22, 26), (28, 18)], fill='#f9d86a', width=3)
image.resize((256, 256), Image.Resampling.NEAREST).save(root / 'icon.png')
