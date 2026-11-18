from pathlib import Path
from PIL import Image, ImageDraw

root = Path(__file__).resolve().parents[1]
background = "#162a29"
# All artwork uses this one absolute 16 x 16 grid, without antialiasing.
image = Image.new("RGB", (16,16), background)
draw = ImageDraw.Draw(image)
draw.rectangle((6,6,8,12), fill="#ad784b")
draw.rectangle((3,4,11,7), fill="#66bb77")
draw.rectangle((5,2,9,9), fill="#66bb77")
draw.rectangle((2,13,6,13), fill="#406f58")

# Identical 16-grid checkmark in both project icons. Each # is one pixel.
CHECKMARK = (
    ".......##",
    "......###",
    ".....###.",
    "....###..",
    "##.###...",
    "#####....",
    ".###.....",
    "..#......",
)
# One grid-cell background border separates the check from the artwork.
cells = {(6+x, 7+y) for y, row in enumerate(CHECKMARK)
         for x, cell in enumerate(row) if cell == "#"}
for x, y in cells:
    for dx, dy in ((-1,0),(1,0),(0,-1),(0,1)):
        if (x+dx,y+dy) not in cells:
            draw.point((x+dx,y+dy), fill=background)
for cell in cells:
    draw.point(cell, fill="#ffe39b")
image.resize((256,256), Image.Resampling.NEAREST).save(root / "icon.png")
