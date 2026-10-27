#include <assert.h>
#include <stdlib.h>
#include <string.h>

static void *expected_free;
static size_t expected_size;
static int fail_allocation;
static unsigned int frees;

static void *test_malloc(size_t size) {
    return fail_allocation ? NULL : malloc(size);
}

static void test_free(void *memory) {
    assert(memory == expected_free);
    for (size_t i = 0; i < expected_size; ++i) {
        assert(((unsigned char *)memory)[i] == 0);
    }
    ++frees;
    free(memory);
}

/* Inspect the allocation immediately before the real free, without reading freed memory. */
#define malloc test_malloc
#define free test_free
#include "../c/jump_srp_memory.c"
#undef malloc
#undef free

static unsigned char *secret(size_t size) {
    unsigned char *memory = malloc(size);
    assert(memory != NULL);
    memset(memory, 0xa5, size);
    expected_free = memory;
    expected_size = size;
    return memory;
}

int main(void) {
    jump_srp_free(NULL, 10);
    assert(frees == 0);
    jump_srp_free(secret(32), 32);
    assert(frees == 1);

    unsigned char *grown = jump_srp_realloc(secret(16), 16, 64);
    assert(grown != NULL && frees == 2);
    for (size_t i = 0; i < 16; ++i) assert(grown[i] == 0xa5);
    expected_free = grown;
    expected_size = 64;
    jump_srp_free(grown, 64);

    unsigned char *shrunk = jump_srp_realloc(secret(64), 64, 8);
    assert(shrunk != NULL && frees == 4);
    for (size_t i = 0; i < 8; ++i) assert(shrunk[i] == 0xa5);
    expected_free = shrunk;
    expected_size = 8;
    jump_srp_free(shrunk, 8);

    unsigned char *original = secret(32);
    fail_allocation = 1;
    assert(jump_srp_realloc(original, 32, 64) == NULL);
    assert(frees == 5);
    for (size_t i = 0; i < 32; ++i) assert(original[i] == 0xa5);
    fail_allocation = 0;
    jump_srp_free(original, 32);

    assert(jump_srp_realloc(secret(32), 32, 0) == NULL);
    assert(frees == 7);
    void *fresh = jump_srp_realloc(NULL, 0, 16);
    assert(fresh != NULL);
    expected_free = fresh;
    expected_size = 16;
    jump_srp_free(fresh, 16);
    assert(frees == 8);
    return 0;
}
