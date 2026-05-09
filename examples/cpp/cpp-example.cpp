// C++ example: Generate DynamicLayout JSON from C code
// Build: g++ -o cpp-example cpp-example.cpp
// Run:   ./cpp-example | json_pp

#include <stdio.h>
#include <string.h>
#include "../../dynamiclayout.h"

int main() {
    char json[8192];

    // === About page (static info) ===
    dl_builder_t b = dl_begin(json, sizeof(json), "About (from C++)");

    dl_fieldset_begin(&b, "About");
    dl_label(&b, "DynamicLayout Engine v1.0");
    dl_label(&b, "Generated from C++ code");
    dl_label(&b, "License: MIT");
    dl_fieldset_end(&b);

    dl_fieldset_begin(&b, "Contact");
    dl_label(&b, "Server-Driven UI for all platforms");
    dl_fieldset_end(&b);

    dl_actions_begin(&b);
    dl_button(&b, "ok", "OK", "primary", 1);
    dl_button(&b, "cancel", "Cancel", "secondary", 0);
    dl_actions_end(&b);

    dl_end(&b);

    printf("=== About Page ===\n");
    printf("%s\n\n", json);

    // === Feedback form ===
    memset(json, 0, sizeof(json));
    dl_builder_t b2 = dl_begin(json, sizeof(json), "Feedback (from C++)");

    dl_fieldset_begin(&b2, "Your Message");
    dl_input(&b2, "name", "Your Name", "STRING", 1);
    dl_input(&b2, "email", "Email", "STRING", 1);
    dl_input(&b2, "subject", "Subject", "STRING", 0);
    dl_fieldset_end(&b2);

    dl_actions_begin(&b2);
    dl_button(&b2, "send", "Send", "primary", 1);
    dl_button(&b2, "cancel", "Cancel", "secondary", 0);
    dl_actions_end(&b2);

    dl_end(&b2);

    printf("=== Feedback Form ===\n");
    printf("%s\n\n", json);

    printf("=== C++ generation OK ===\n");
    return 0;
}
