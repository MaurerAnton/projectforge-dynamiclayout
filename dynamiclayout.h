#ifndef DYNAMICLAYOUT_H
#define DYNAMICLAYOUT_H

/*
 * DynamicLayout C API — generate layout JSON from C/C++.
 * Build: gcc -o layout layout.c -lm
 *
 * Example:
 *   char json[4096];
 *   DL_Builder b = dl_begin(json, sizeof(json), "My Page");
 *   dl_fieldset(&b, "Info",
 *     dl_label(&b, "Hello from C!"), NULL);
 *   dl_end(&b);
 *   printf("%s", json);
 */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdarg.h>

#ifdef __cplusplus
extern "C" {
#endif

/* === Builder === */
typedef struct {
    char *buf;
    size_t size;
    size_t pos;
    int depth;
    int comma;  /* 1 = need comma before next value */
} DL_Builder;

/* === Internal helpers === */
static void dl_putc(DL_Builder *b, char c) {
    if (b->pos < b->size - 1) b->buf[b->pos++] = c;
}

static void dl_puts(DL_Builder *b, const char *s) {
    while (*s && b->pos < b->size - 1) b->buf[b->pos++] = *s++;
}

static void dl_indent(DL_Builder *b) {
    dl_putc(b, '\n');
    for (int i = 0; i < b->depth; i++) dl_puts(b, "  ");
}

static void dl_comma(DL_Builder *b) {
    if (b->comma) dl_putc(b, ',');
}

static void dl_escape(DL_Builder *b, const char *s) {
    dl_putc(b, '"');
    while (*s) {
        switch (*s) {
            case '"': dl_puts(b, "\\\""); break;
            case '\\': dl_puts(b, "\\\\"); break;
            case '\n': dl_puts(b, "\\n"); break;
            case '\r': dl_puts(b, "\\r"); break;
            case '\t': dl_puts(b, "\\t"); break;
            default: dl_putc(b, *s); break;
        }
        s++;
    }
    dl_putc(b, '"');
}

static void dl_str(DL_Builder *b, const char *key, const char *val) {
    if (!val) return;
    dl_comma(b);
    dl_indent(b);
    dl_escape(b, key);
    dl_puts(b, ": ");
    dl_escape(b, val);
    b->comma = 1;
}

static void dl_bool(DL_Builder *b, const char *key, int val) {
    if (!val) return;
    dl_comma(b);
    dl_indent(b);
    dl_escape(b, key);
    dl_puts(b, ": true");
    b->comma = 1;
}

static void dl_arr(DL_Builder *b, const char *key) {
    dl_comma(b);
    dl_indent(b);
    dl_escape(b, key);
    dl_puts(b, ": [");
    b->depth++;
    b->comma = 0;
}

static void dl_arr_end(DL_Builder *b) {
    b->depth--;
    dl_indent(b);
    dl_putc(b, ']');
    b->comma = 1;
}

static void dl_obj(DL_Builder *b) {
    dl_comma(b);
    dl_indent(b);
    dl_putc(b, '{');
    b->depth++;
    b->comma = 0;
}

static void dl_obj_end(DL_Builder *b) {
    b->depth--;
    dl_indent(b);
    dl_putc(b, '}');
    b->comma = 1;
}

/* === Public API === */

/* Start building a layout. Returns a builder. */
static DL_Builder dl_begin(char *buffer, size_t size, const char *title) {
    DL_Builder b = { buffer, size, 0, 0, 0 };
    dl_puts(&b, "{\n  \"ui\": {");
    b.depth = 2;
    b.comma = 0;
    dl_str(&b, "title", title);
    dl_comma(&b);
    dl_indent(&b);
    dl_puts(&b, "\"layout\": [");
    b.depth = 3;
    b.comma = 0;
    return b;
}

/* Finish layout JSON. Call at the end. */
static void dl_end(DL_Builder *b) {
    // Close layout array elements
    b->depth = 2;  // back to layout array level
    dl_comma(b);
    // 
    // Actually let me just write the ending directly
    b->depth = 2;
    dl_indent(b);
    dl_puts(b, "],");  // close layout array
    dl_indent(b);
    dl_puts(b, "\"translations\": {},");
    dl_indent(b);
    dl_puts(b, "\"userAccess\": { \"cancel\": true }");
    b->depth = 1;
    dl_indent(b);
    dl_puts(b, "}");   // close "ui"
    b->depth = 0;
    dl_indent(b);
    dl_puts(b, "}");   // close root
    b->buf[b->pos] = 0; /* null-terminate */
}

/* Add a FIELDSET. */
static void dl_fieldset(DL_Builder *b, const char *title, ...) {
    dl_obj(b);
    dl_str(b, "type", "FIELDSET");
    dl_str(b, "key", "fs");
    dl_str(b, "title", title);

    /* Children are passed as variadic arguments terminated by NULL */
    va_list args;
    va_start(args, title);

    /* Since children can't be function calls directly with va_args,
       we handle them differently. See dl_label for the pattern.
       For now, just open the content array. */
    dl_arr(b, "content");

    /* Process children */
    const char *child_type;
    while ((child_type = va_arg(args, const char *)) != NULL) {
        if (strcmp(child_type, "label") == 0) {
            const char *text = va_arg(args, const char *);
            dl_obj(b);
            dl_str(b, "type", "LABEL");
            dl_str(b, "key", "lbl");
            dl_str(b, "label", text);
            dl_obj_end(b);
        } else if (strcmp(child_type, "input") == 0) {
            const char *id = va_arg(args, const char *);
            const char *label = va_arg(args, const char *);
            dl_obj(b);
            dl_str(b, "type", "INPUT");
            dl_str(b, "key", "inp");
            dl_str(b, "id", id);
            dl_str(b, "label", label);
            dl_obj_end(b);
        } else if (strcmp(child_type, "button") == 0) {
            const char *id = va_arg(args, const char *);
            const char *title2 = va_arg(args, const char *);
            const char *color = va_arg(args, const char *);
            dl_obj(b);
            dl_str(b, "type", "BUTTON");
            dl_str(b, "key", "btn");
            dl_str(b, "id", id);
            dl_str(b, "title", title2);
            dl_str(b, "color", color);
            dl_obj_end(b);
        }
    }

    dl_arr_end(b);
    dl_obj_end(b);
}

/* Quick helper: add a single label with fieldset */
static void dl_about(DL_Builder *b, const char *title, const char *text) {
    dl_obj(b);
    dl_str(b, "type", "FIELDSET");
    dl_str(b, "key", "fs1");
    dl_str(b, "title", title);
    dl_arr(b, "content");
    dl_obj(b);
    dl_str(b, "type", "LABEL");
    dl_str(b, "key", "l1");
    dl_str(b, "label", text);
    dl_obj_end(b);
    dl_arr_end(b);
    dl_obj_end(b);
}

/* Add actions array */
static void dl_actions(DL_Builder *b, ...) {
    dl_arr(b, "actions");
    va_list args;
    va_start(args, b);
    const char *type;
    while ((type = va_arg(args, const char *)) != NULL) {
        if (strcmp(type, "button") == 0) {
            const char *id = va_arg(args, const char *);
            const char *title = va_arg(args, const char *);
            const char *color = va_arg(args, const char *);
            dl_obj(b);
            dl_str(b, "type", "BUTTON");
            dl_str(b, "key", "btn");
            dl_str(b, "id", id);
            dl_str(b, "title", title);
            dl_str(b, "color", color);
            dl_obj_end(b);
        }
    }
    va_end(args);
    dl_arr_end(b);
}

#ifdef __cplusplus
}
#endif

#endif /* DYNAMICLAYOUT_H */
