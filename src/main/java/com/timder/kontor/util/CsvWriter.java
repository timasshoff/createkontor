package com.timder.kontor.util;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class CsvWriter implements Closeable {

    private static final String EOL = "\n";

    private final Writer out;
    private final char separator;
    private final boolean decimalComma;
    private boolean started = false;

    public CsvWriter(Writer out) {
        this(out, ',', false);
    }

    public CsvWriter(Writer out, char separator, boolean decimalComma) {
        this.out = out;
        this.separator = separator;
        this.decimalComma = decimalComma;
    }

    public static CsvWriter create(Path file) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
        return new CsvWriter(writer);
    }

    /**
     * Writes a comment line
     * @param comment The comment to write
     * @return This writer
     * @throws IOException If something fails
     */
    public CsvWriter comment(String comment) throws IOException {
        out.append("# ").append(comment).append(EOL);
        started = true;
        return this;
    }

    /**
     * Starts a new section.
     * A section begins with an empty line, then a line containing the name as a comment and then the header.
     * @param name The name of this section
     * @param columns The columns
     * @return This writer
     * @throws IOException If something fails
     */
    public CsvWriter section(String name, String... columns) throws IOException {
        if (started) {
            out.append(EOL);
        }
        out.append('#').append(name).append(EOL);
        started = true;
        return row((Object[]) columns);
    }

    /**
     * Writes a row with exactly two cells
     * @param key The key
     * @param value The value
     * @return This writer
     * @throws IOException If something fails
     */
    public CsvWriter keyValue(String key, Object value) throws IOException {
        return row(key, value);
    }

    /**
     * Writes one row
     * @param cells The cells
     * @return This writer
     * @throws IOException If something fails
     */
    public CsvWriter row(Object... cells) throws IOException {
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                out.append(separator);
            }
            out.append(cell(cells[i]));
        }
        out.append(EOL);
        started = true;
        return this;
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    private String cell(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Double d) {
            return number(d);
        }
        if (value instanceof Float f) {
            return number(f.doubleValue());
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Enum<?> e) {
            return e.name().toLowerCase(Locale.ROOT);
        }
        return text(value.toString());
    }

    private String number(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "";
        }
        String plain = BigDecimal.valueOf(value).toPlainString();
        return decimalComma ? plain.replace('.', ',') : plain;
    }

    private String text(String raw) {
        String safe = raw;
        if (!safe.isEmpty()) {
            char first = safe.charAt(0);
            if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
                safe = "'" + safe;
            }
        }
        boolean needsQuotes = safe.indexOf(separator) >= 0
                || safe.indexOf('"') >= 0
                || safe.indexOf('\n') >= 0
                || safe.indexOf('\r') >= 0;
        if (!needsQuotes) {
            return safe;
        }
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
