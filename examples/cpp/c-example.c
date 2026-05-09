// C example: Generate DynamicLayout JSON
// Build: gcc -o c-example c-example.c
// Run:   ./c-example

#include <stdio.h>
#include <string.h>
#include "../../dynamiclayout.h"

int main() {
    char json[4096];

    DL_Builder b = dl_begin(json, sizeof(json), "About Page (C)");
    dl_about(&b, "Info", "DynamicLayout Engine v0.1.0");
    dl_about(&b, "License", "GPLv3 — generated from C code");
    dl_end(&b);

    printf("=== About Page ===\n%s\n", json);

    // Validate: should contain title, layout, FIELDSET
    if (strstr(json, "\"title\"") && strstr(json, "\"layout\"") && strstr(json, "FIELDSET")) {
        printf("=== JSON validation OK ===\n");
        return 0;
    } else {
        printf("=== JSON validation FAILED ===\n");
        return 1;
    }
}
