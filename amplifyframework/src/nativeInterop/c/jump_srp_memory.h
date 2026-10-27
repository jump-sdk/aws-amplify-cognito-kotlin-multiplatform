#ifndef JUMP_SRP_MEMORY_H
#define JUMP_SRP_MEMORY_H
#include <stddef.h>

void jump_srp_free(void *memory, size_t size);
void *jump_srp_realloc(void *memory, size_t old_size, size_t new_size);
#endif
