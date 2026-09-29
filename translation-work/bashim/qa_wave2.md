# Bash.im wave-2 QA log (chunks 16–31)

All 16 chunks independently verified clean: 1,000 lines each, valid JSON, exact keys `{id,ru,en}`,
ID order matches source, unique IDs, nonempty `ru`/`en`, `ru` == source text char-for-char,
newline parity, all source `<...>` tokens byte-identical in `en` (except documented exceptions).

| chunk | lines | result |
|---|---|---|
| 16 | 1,000 | 0 problems |
| 17 | 1,000 | 0 problems (worker caught and discarded a draft built from a wrong description; retranslated from real source texts; mixed-script prefix 399840 verified) |
| 18 | 1,000 | 0 problems |
| 19 | 1,000 | 0 problems |
| 20 | 1,000 | 0 problems |
| 21 | 1,000 | 0 problems |
| 22 | 1,000 | 0 problems |
| 23 | 1,000 | 0 problems |
| 24 | 1,000 | 0 problems |
| 25 | 1,000 | 0 problems |
| 26 | 1,000 | 0 problems |
| 27 | 1,000 | 0 problems (232 records keep Cyrillic non-angle-bracket speaker labels in `en` — allowed; only `<...>` nicks must be byte-identical) |
| 28 | 1,000 | 0 problems |
| 29 | 1,000 | 0 problems; 1 deliberate placeholder: 411371 `<ссылка на ортопедический матрас>` → `<link to an orthopedic mattress>` |
| 30 | 1,000 | 0 problems |
| 31 | 1,000 | 0 problems |

Wave-2 total: **16,000 / 16,000 records verified**.
