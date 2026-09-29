# Bash.im translation — Wave 1 QA log (chunks 00–15)

Date: 2026-09-20. All 16 chunks reached 1000/1000 lines; progress.txt marked done for all.

## Automated audit (post-completion)
Script: /tmp/verify_wave1.py — checks per record: valid JSON, keys exactly {id,ru,en},
ids in input order, ru == source text, en non-empty, ru/en `\n` parity, and every
`<...>` token in ru present byte-identical in en.

## Issues found and fixed
### Speaker-nick / markup mistranslations (fixed mechanically, backups in repair_backup/)
| chunk | line | id | ru token | was in en | restored to |
|---|---|---|---|---|---|
| 00 | 513 | 986 | `<Юзер>` | `<User>` | `<Юзер>` |
| 01 | 107 | 3195 | `<10-летняя Катюша>` | `<10-year-old Katyusha>` | verbatim |
| 01 | 407 | 4988 | `<Cepг>` (Latin homoglyphs) | `<Серг>` | `<Cepг>` |
| 01 | 618 | 6909 | `<///Мытищи\\\\\\BFG® [$теб]>` | backslash count altered | verbatim (ordered pairing) |
| 04 | 535 | 98301 | `<такой-то>` | `<such-and-such>` | `<такой-то>` |
| 06 | 905 | 214781 | `<d'n'd\|cry[посеял_телефон]>` | `[lost_phone]` | verbatim |
| 08 | 275 | 285197 | `<Гриззли\xa0эвэй>` (NBSP) | regular space | NBSP restored |
| 09 | 139 | 346212 | `<Она>`x2 `<Он>` | `<She>` `<He>` | verbatim |
| 09 | 461 | 381058 | `<зачеркнуто>` `</зачеркнуто>` | `<crossed out>` | verbatim |
| 09 | 804 | 390573 | `<Он>`x2 `<Она>`x3 | `<He>` `<She>` | verbatim |
| 09 | 919 | 390713 | `<Он>` `<Она>`x4 | `<He>` `<She>` | verbatim |
| 12 | 498 | 393544 | `pol="мужской"` | `pol="male"` | verbatim |
| 13 | 152 | 394247 | `<бог>` | `<god>` | `<бог>` |
| 14 | 325 | 395556 | `<АндрейК>`x2 `<Аминатеп>`x2 | `<AndreyK>` `<Aminatep>` | verbatim |
| 14 | 849 | 396146 | `<Он>` | `<He>` | `<Он>` |

### Deliberately KEPT (translator judgment, not errors)
- Stage directions: `<а здесь такой хардкор с дисторшенами>`, `<...сцылко...>`,
  `<..цитата..>`, `<хочется странного>`, `<..через несколько секунд..>`,
  `<... проходит несколько минут...>`, `<Повысив голос от возмущения>` — prose, translated.
- Code/shell: `< /dev/винт >`→`< /dev/hdd >` (slang preserved in translation);
  `<?php echo"сайт на реконструкции";?>` string literal translated (joke needs it).
- Mock-emote markup with sentence content: `<Спасибо большое!>` etc. (id 397014).

### Newline damage (repaired 2026-09-20)
Repair worker fixed all 15 assigned records plus the `<KaMuKag3e>` dropped
speaker line (chunk_03 line 232, id 28452); byte-diff vs backups confirmed
only the target lines changed. Post-repair audit caught one more the worker
was never told about (it was hidden past the first scan's display cutoff):
- chunk_03 line 997 (id 59092): translator had turned a `\t` into `\n`
  (`[01:44]\tartjom:`) — tab restored; now ru/en both 0 newlines.
- chunk_03: lines 128 (id 24735), 164 (id 25777, ~12 lines incl. ASCII art
  restored), 232 (id 28452), 279, 327, 782 — all fixed, verified.
- chunk_06: lines 547, 934, 936, 1000 — all fixed, verified.
- chunk_12: lines 672, 686, 845, 922 — all fixed, verified.
Final state: **newline_mismatch=0 on all 16 chunks**; every remaining
`<...>` flag is a deliberate KEEP (stage direction / code / shell, see below).

## Lessons encoded into wave-2+ worker briefs
1. Speaker nicks byte-identical, never transliterated/translated (incl. `<Она>`/`<Он>`).
2. Homoglyph nicks and invisible chars (NBSP) preserved exactly.
3. Formatting markup and tag attributes verbatim.
4. `\n` parity on every record; never drop/merge lines (ASCII art especially).
5. Final self-verification must include nick-verbatim and newline-parity checks.

### Remaining `<...>` flags after nick repair (2026-09-20) — all DELIBERATE KEEPs
Each is a stage direction, emote markup, shell token, or code literal whose
Russian content was correctly translated rather than preserved as a nick:
- chunk_00 line 993 (id 2752): `< /dev/винт >` — shell token; en `</dev/hdd>` correct.
- chunk_01 line 975 (id 20804): `<а здесь такой хардкор с дисторшенами>` — stage direction.
- chunk_03 line 396 (id 38435): `<...сцылко...>` — stage direction ("link...").
- chunk_03 line 406 (id 38593, x2): `<..цитата..>` — stage direction ("quote...").
- chunk_03 line 618 (id 45124): `<реклама>` — stage direction ("advertising").
- chunk_03 line 897 (id 54750): `<хочется странного>` — stage direction ("wants something weird").
- chunk_04 line 138 (id 66020): `<..через несколько секунд..>` — stage direction ("after a few seconds").
- chunk_08 line 139 (id 388964): `<Повысив голос от возмущения>` — stage direction ("raising voice in indignation").
- chunk_09 line 380 (id 391007): `<... проходит несколько минут...>` — stage direction ("several minutes pass").
- chunk_13 line 610 (id 394460): `<?php echo"сайт на реконструкции";?>` — PHP string literal.
- chunk_15 line 14 (id 397618): `<*начинает дико жестикулировать*>` — emote markup.
- chunk_15 line 42 (id 397646): `<*достает из подсумка шрапнель и кидает в толпу*>` — emote markup.
- chunk_15 line 44 (id 397648): `<*неприлично матерясь*>` — emote markup.

## Final wave-1 status (2026-09-20)
- **16/16 chunks verified**: each 1,000 physical lines, valid JSON, exact keys
  `{id,ru,en}`, all IDs unique and in source order, all fields non-empty,
  **newline parity 100% (0 mismatches across 16,000 records)**, all speaker
  prefixes byte-identical except the 13 deliberate KEEPs above.
- Total: **16,000 / 81,497 records** (19.6%).
- Wave-2 (chunks 16-31) dispatched 2026-09-20 ~00:25 UTC; in progress.
