# Bash.im wave-3 QA log (chunks 32–47)

## Independently verified clean
| chunk | lines | result |
|---|---|---|
| 32 | 1,000 | 0 problems |
| 33 | 1,000 | 0 problems (worker repaired soft-hyphen nicks in 416012) |
| 34 | 1,000 | 0 problems (worker repaired translated token <ответ> in 417081, restored blank lines) |
| 35 | 1,000 | 0 problems (worker rebuilt in source order after late appends) |
| 36 | 1,000 | 0 problems; 1 deliberate stage direction: 418257 `<прошло несколько часов>` → `<several hours passed>` |
| 37 | 1,000 | 0 problems |
| 38 | 1,000 | 0 problems; 1 deliberate placeholder: 420761 `<%сайт знакомств%>` → `<%dating site%>` (mid-line redaction, not a speaker) |
| 39 | 1,000 | 0 problems (soft-hyphen line prefixes in 421816/421975 retained verbatim) |
| 40 | 1,000 | 0 problems; genuine U+FFFD in 422179 preserved in `ru` per policy |
| 41 | 1,000 | 0 problems (worker restored translated tokens <название файла> / <прямая ссылка на файл на офсайте> in 423651) |
| 42 | 1,000 | 0 problems (worker repaired translated role attributes in 424266) |
| 43 | 1,000 | 0 problems (Cyrillic street name «ул. Ружейная» kept byte-identical as the joke's subject) |
| 44 | 1,000 | 0 problems (worker repaired 4 speaker-label typos, restored <название-форума> token) |
| 45 | 1,000 | 0 problems (worker did atomic reorder after late appends) |
| 46 | 1,000 | 0 problems |
| 47 | 1,000 | 0 problems |

## Known convention variance (open, not a hard-rule failure)
Non-angle-bracket speaker labels: chunk_27 keeps them Cyrillic (`Она:` stays `Она:` in `en`);
chunk_45 translates them idiomatically (`Я:`→`Me:`, `Муж:`→`Husband:`, `Она:`→`She:`, `Зевс:`→`Zeus:`).
The hard rule only covers `<...>` tokens (all byte-identical everywhere). Final QA should
decide one convention or accept the variance. Flagged for Clint/parent.
