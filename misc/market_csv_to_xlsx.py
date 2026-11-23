#!/usr/bin/env python3
"""
THIS SCRIPT HAS BEEN AI GENERATED!

Converts a Create: Kontor market export (CSV) into an Excel file.

Every section of the CSV (#context, #company, #history, ...) becomes its own sheet.
If the file has a history section, the sheet gets charts for price, deviation and competitors.

Usage:
    python3 market_csv_to_xlsx.py market_create_brass_ingot_day79_20261123-121313.csv
    python3 market_csv_to_xlsx.py export.csv -o result.xlsx

Needs openpyxl once:
    python3 -m pip install openpyxl
"""

import argparse
import csv
import sys
from pathlib import Path

try:
    from openpyxl import Workbook
    from openpyxl.chart import LineChart, Reference
    from openpyxl.styles import Font
    from openpyxl.utils import get_column_letter
except ImportError:
    sys.exit("openpyxl is missing. Install it with:  python3 -m pip install openpyxl")

# The exporter prefixes text that starts with one of these with an apostrophe (formula protection).
FORMULA_STARTS = ("=", "+", "-", "@", "\t", "\r")

# Charts for the history sheet: (title, columns). The x axis is the "line" column.
HISTORY_CHARTS = [
    ("Price", ["displayed_price", "price_level"]),
    ("Deviation", ["deviation"]),
    ("Competitors", ["competitors"]),
    ("Demand and overflow", ["demand", "overflow"]),
]


def parse_cell(text):
    """Empty cell -> None, number -> int or float, anything else -> text."""
    if text == "":
        return None
    if len(text) > 1 and text[0] == "'" and text[1] in FORMULA_STARTS:
        return text[1:]
    try:
        return int(text)
    except ValueError:
        pass
    try:
        return float(text)
    except ValueError:
        return text


def read_sections(path):
    """Returns (comments, sections). A section is (name, header, rows)."""
    comments = []
    sections = []
    current = None  # [name, header, rows]
    expect_header = False

    with open(path, newline="", encoding="utf-8-sig") as handle:
        for row in csv.reader(handle):
            if not row:
                current = None
                expect_header = False
                continue
            first = row[0]
            if first.startswith("# "):
                comments.append(first[2:])
                continue
            if first.startswith("#") and len(row) == 1:
                current = [first[1:], None, []]
                sections.append(current)
                expect_header = True
                continue
            if current is None:
                continue  # stray line outside of a section
            if expect_header:
                current[1] = row
                expect_header = False
            else:
                current[2].append([parse_cell(cell) for cell in row])

    return comments, [(name, header, rows) for name, header, rows in sections if header is not None]


def sheet_title(name, used):
    title = name[:31] or "sheet"
    candidate, counter = title, 2
    while candidate in used:
        suffix = f"_{counter}"
        candidate = title[: 31 - len(suffix)] + suffix
        counter += 1
    used.add(candidate)
    return candidate


def write_sheet(workbook, title, header, rows, first):
    sheet = workbook.active if first else workbook.create_sheet()
    sheet.title = title
    sheet.append(header)
    for cell in sheet[1]:
        cell.font = Font(bold=True)
    for row in rows:
        sheet.append(row)
    # openpyxl stores text starting with "=" as a formula. Force it to plain text.
    for row in sheet.iter_rows(min_row=2):
        for cell in row:
            if isinstance(cell.value, str) and cell.value.startswith("="):
                cell.data_type = "s"
    sheet.freeze_panes = "A2"
    for index, name in enumerate(header, start=1):
        longest = max([len(str(name))] + [len(str(row[index - 1])) for row in rows[:200] if index <= len(row) and row[index - 1] is not None])
        sheet.column_dimensions[get_column_letter(index)].width = min(max(longest + 2, 8), 60)
    return sheet


def add_history_charts(sheet, header, row_count):
    if "line" in header:
        x_name = "line"
    elif "seq" in header:
        x_name = "seq"
    else:
        return
    if row_count < 2:
        return

    x_column = header.index(x_name) + 1
    categories = Reference(sheet, min_col=x_column, min_row=2, max_row=row_count + 1)
    anchor_column = len(header) + 2
    anchor_row = 1

    for title, names in HISTORY_CHARTS:
        columns = [header.index(name) + 1 for name in names if name in header]
        if not columns:
            continue
        chart = LineChart()
        chart.title = title
        chart.height = 8
        chart.width = 22
        chart.x_axis.title = x_name
        chart.x_axis.delete = False
        chart.y_axis.delete = False
        chart.x_axis.tickLblSkip = max(row_count // 12, 1)
        chart.x_axis.tickMarkSkip = max(row_count // 12, 1)
        for column in columns:
            chart.add_data(Reference(sheet, min_col=column, min_row=1, max_row=row_count + 1), titles_from_data=True)
        chart.set_categories(categories)
        for series in chart.series:
            series.smooth = False
            series.marker.symbol = "none"
        sheet.add_chart(chart, f"{get_column_letter(anchor_column)}{anchor_row}")
        anchor_row += 17


def main():
    parser = argparse.ArgumentParser(description="Converts a Create: Kontor market export CSV into an Excel file.")
    parser.add_argument("csv_file", type=Path)
    parser.add_argument("-o", "--output", type=Path, help="Target file. Default: same name with .xlsx")
    args = parser.parse_args()

    if not args.csv_file.is_file():
        sys.exit(f"File not found: {args.csv_file}")
    output = args.output or args.csv_file.with_suffix(".xlsx")

    comments, sections = read_sections(args.csv_file)
    if not sections:
        sys.exit("No sections found. Is this an export of /kontor market export?")

    workbook = Workbook()
    used = set()
    first = True
    for name, header, rows in sections:
        sheet = write_sheet(workbook, sheet_title(name, used), header, rows, first)
        first = False
        if name == "history":
            add_history_charts(sheet, header, len(rows))

    if comments:
        info = workbook.create_sheet(sheet_title("info", used))
        for comment in comments:
            info.append([comment])
        info.column_dimensions["A"].width = 120

    workbook.save(output)
    summary = ", ".join(f"{name} ({len(rows)})" for name, _, rows in sections)
    print(f"Written: {output}")
    print(f"Sheets: {summary}")


if __name__ == "__main__":
    main()
