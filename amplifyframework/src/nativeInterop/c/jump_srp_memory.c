#include "jump_srp_memory.h"
#include <stdlib.h>
#include <string.h>

void jump_srp_free(void *memory, size_t size) {
    if (memory == NULL) return;
    /* Volatile writes must survive optimization, including before free. */
    volatile unsigned char *bytes = memory;
    while (size-- > 0) *bytes++ = 0;
    free(memory);
}

void *jump_srp_realloc(void *memory, size_t old_size, size_t new_size) {
    if (new_size == 0) {
        jump_srp_free(memory, old_size);
        return NULL;
    }
    /* realloc may release the original allocation without clearing it. */
    void *replacement = malloc(new_size);
    if (replacement == NULL) return NULL;
    if (memory != NULL) {
        memcpy(replacement, memory, old_size < new_size ? old_size : new_size);
        jump_srp_free(memory, old_size);
    }
    return replacement;
}
