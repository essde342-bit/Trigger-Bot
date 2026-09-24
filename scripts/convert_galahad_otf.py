#!/usr/bin/env python3
from __future__ import annotations

import argparse
from copy import deepcopy
from pathlib import Path

from fontTools.pens.cu2quPen import Cu2QuPen
from fontTools.pens.ttGlyphPen import TTGlyphPen
from fontTools.ttLib import TTFont


COPY_LAYOUT_TABLES = ("GDEF", "GPOS", "GSUB", "kern", "gasp", "vhea", "vmtx")
DROP_TABLES = {"CFF ", "CFF2", "fvar", "gvar", "avar", "STAT"}


def convert(source: Path, target: Path) -> None:
    source_font = TTFont(str(source), recalcBBoxes=False, recalcTimestamp=False)

    if "CFF " not in source_font and "CFF2" not in source_font:
        source_font.flavor = None
        target.parent.mkdir(parents=True, exist_ok=True)
        source_font.save(str(target))
        return

    glyph_order = source_font.getGlyphOrder()
    glyph_set = source_font.getGlyphSet()
    glyphs = {}

    for glyph_name in glyph_order:
        tt_pen = TTGlyphPen(glyph_set)
        cu2qu_pen = Cu2QuPen(
            tt_pen,
            max_err=1.0,
            reverse_direction=False,
        )
        glyph_set[glyph_name].draw(cu2qu_pen)
        glyphs[glyph_name] = tt_pen.glyph(dropImpliedOnCurves=True)

    from fontTools.fontBuilder import FontBuilder

    fb = FontBuilder(source_font["head"].unitsPerEm, isTTF=True)
    fb.setupGlyphOrder(glyph_order)
    fb.setupCharacterMap(source_font.getBestCmap())
    fb.setupGlyf(glyphs)
    fb.setupHorizontalMetrics(deepcopy(source_font["hmtx"].metrics))
    fb.setupHorizontalHeader(
        ascent=source_font["hhea"].ascent,
        descent=source_font["hhea"].descent,
        lineGap=source_font["hhea"].lineGap,
    )

    source_names = {}
    for name in source_font["name"].names:
        if name.nameID in {1, 2, 3, 4, 5, 6, 16, 17}:
            source_names[name.nameID] = name.toUnicode()

    fb.setupNameTable(source_names)
    fb.setupOS2()
    fb.setupPost()
    fb.setupMaxp()

    out = fb.font
    out["head"].glyphDataFormat = 0

    for tag in COPY_LAYOUT_TABLES:
        if tag in source_font:
            out[tag] = deepcopy(source_font[tag])

    if "OS/2" in source_font:
        source_os2 = source_font["OS/2"]
        target_os2 = out["OS/2"]
        for field, value in source_os2.__dict__.items():
            if field.startswith("_") or field == "tableTag":
                continue
            if hasattr(target_os2, field):
                try:
                    setattr(target_os2, field, deepcopy(value))
                except Exception:
                    pass

    for tag in list(out.keys()):
        if tag in DROP_TABLES:
            del out[tag]

    target.parent.mkdir(parents=True, exist_ok=True)
    out.save(str(target), reorderTables=False)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("source", type=Path)
    parser.add_argument("target", type=Path)
    args = parser.parse_args()

    convert(args.source, args.target)

    result = TTFont(
        str(args.target),
        recalcBBoxes=False,
        recalcTimestamp=False,
    )
    if result.sfntVersion != "\x00\x01\x00\x00":
        raise RuntimeError(
            f"Conversion did not produce a TrueType font: {result.sfntVersion!r}"
        )
    if "glyf" not in result or "CFF " in result or "CFF2" in result:
        raise RuntimeError("Output font is not a TrueType glyf font")
    if not result.getBestCmap():
        raise RuntimeError("Output font has no usable character map")

    print(
        f"Converted {args.source} -> {args.target}: "
        f"{len(result.getGlyphOrder())} glyphs"
    )


if __name__ == "__main__":
    main()
