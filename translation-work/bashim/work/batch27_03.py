#!/usr/bin/env python3
"""Translate bashim chunk_27 batch 3 (input lines 201-300). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
409275: """xxx: Where can I buy a 3.5mm headphone extension cable?
yyy: 3.5mm? why so short?""",
409276: """xxx: Exam season...
xxx: My neighbor left, gave me his keys to look after the apartment, I locked myself in at his place: no internet, no phone, just textbooks and notes.
xxx: Never slept so well...""",
409277: """Speakerrr: don't look free internet in the ping""",
409278: """BAUM58: John Doe, you're a rare moron, a petty tyrant and a very unpleasant person. You've got nose hairs sticking out and you smell like long-unwashed rags.
BAUM58: I don't give a damn, I'm from a different uni anyway, but some guys I know are complaining...""",
409279: """ххх: I wanna eat ...
ууу: And what were you doing before this? It's already midnight.
ххх: I wanted to eat ...""",
409280: """SHE: They finally hooked up the internet for me!
ххх: what's the speed?
SHE: 10 megapixels
ххх: yeah""",
409281: """From Wikipedia:
In 2006, Animal Liberation Front activists snuck into the Swiss Royal Circus to free a white Siberian tiger. Finding themselves next to the predator's cage, the animal defenders changed their minds. Instead of the tiger, a white rabbit was "liberated" - the pet of the clown's daughter.""",
409282: """From a Russian-language forum in Canada, about hockey:
-- Yeah, our guys beat their guys pretty good yesterday...""",
409283: """a comment on an emo song:

xxx:
that's how my friend died
choked on his bangs""",
409284: """years ago my little daughter used to torment me daily with: "why am I not a boy? I want to be a boy!". we were already starting to wonder if something was wrong with the kid's sexual orientation. It turned out to be dead simple. Before nap time the kindergarten group was sent to the toilet. and while the girls stood in line, the boys, grouping around one toilet, were done in one minute. The clueless nannies praised the boys - and scolded the girls.""",
409285: """ууу: ...she's like that with me, don't put your finger in her mouth... you'll die of orgasm!""",
409286: """From a forum about pets:
- My daughter has her first jubilee tomorrow, she'll be 5, all my relatives are coming who think I've lost my mind because I have 4 cats. But I actually have 9...""",
409287: """Lori: Based on the Christmas fortune-telling with throwing boots at passersby, my betrothed will be called "Fuck!" =/""",
409288: """111: tonight's the fortune-telling night!!! )
222: cool!
111: figured out how you'll learn your fate? )
222: yep. I'll ask my husband what the plans are for tomorrow.""",
409289: """when my mom saw my light elf in L2, her first phrase was...:
- those ears would make excellent aspic!""",
409290: """From the news:
Angelina Jolie finally got married!

Comment: Seryoga's gonna be upset...""",
409291: """Slam: I've got a friend who's a true economist. when his pencils run out, he rides the free bus to IKEA to grab free pencils there""",
409292: """Zima: I've figured it out, a beautiful woman is one who looks the least ugly in modern fashionable clothes for her era.""",
409293: """xxx: what are you doing today?
yyy: as always, Pinky...
yyy: I'll try to do at least something.""",
409294: """me: Lena, marry me!
she: what brought this on?
me: well, I decided so
she: you seem like a good person, why do this to yourself...""",
409295: """Guild chat of an online game. One of the newcomers finished describing the obvious aspects of the game mechanics he'd discovered.
ххх: Captain?
ууу: Just got promoted to Major, how'd you know?!""",
409296: """ххх: damn. You're clairvoyant. I just woke up
ууу: Are you still asleep? Then I'm writing to you!""",
409297: """ххх: And I renamed the "Sort Out" folder to "Archive" and don't stress about it...""",
409298: """xxx: You got anything for tomorrow's exam?
yyy: Yep. Faith, hope, optimism )""",
409299: """Котенок: and it's better to sleep on silk bedding ))
S2i3V: Sleeping is best done lying down, and I tell you this as a student.""",
409300: """Market.
Seller: We've got squid at 110r and 140r
Me: What's the difference?
Seller: The 140 ones are imported, the 110 ones are local
Me: Local - from the Volga or something?""",
409301: """ПуфФысТик: ЫЫЫЫ! cool)))))))))))()))
odmin: you write in Lisp too?""",
409302: """yyy: sweetie, let's do virtual?
xxx: There's been an inspection sitting at my work for three days, and I'm working 12-hour days. Sex-wise I want only one thing - for everyone to get the f"ck off me.""",
409303: """xxx: You know why our bathtub is so dirty? I screwed in a brighter bulb there.""",
409304: """ХХХ: got a text from mom: "caught a mouse with the vacuum cleaner. what do I do?"
ХХХ: don't even know what to answer her...""",
409305: """dima: Discovered something amazing
dima: turns out Nastyka thought the English "Why" was the Russian "Vkhui" and that's why she wasn't answering me!""",
409306: """xxx: and I'll spend 21.12.2012 where the best years of my life passed
zzz: in Piter?
xxx: nah, I'll be playing Heroes 3""",
409307: """<Maroz> how do I get into the chattik?
<black_cat> /join #channelName
<Maroz> and into the chattik?
<Kult> is that a threat?""",
409308: """xxx: In the morning I woke up to the alarm ringing, grabbed it and threw it with all my might. Heard glass shatter. Thought that when I'd thrown it before it never broke anything, opened my eyes and it turned out I'd spent the night at my girlfriend's parents' place..........""",
409309: """xxx: Seryoga decided to get discharged from the army, on blood pressure grounds
yyy: What's he gonna press on?
xxx: On pity )""",
409310: """From the Happy Farmer game forum:
xxx: why can't I steal eggs from my friend?
yyy: Pull harder""",
409311: """DON: Political Economy. Textbook. — P.izdat.
DON: good textbook.""",
409312: """Discussing TES V:
Сот: I wonder if there'll be vampires or not
Сот: and if there are, what kind
ganondorf: And if a dragon bites you, you can play as dragons!
Сот: If a dragon bites you, you'll be playing as a stump""",
409313: """hett: you girls have it good: want to eat - go cook""",
409314: """olya.lion: can I find out - what are the virtues of programmers?
Ping.Win: Many of you are familiar with a programmer's virtues. There are only three of them, and of course they are: laziness, impatience and hubris. (C) Larry Wall
olya.lion: and what are the flaws?
Ping.Win: programmers are perfect. they have no flaws""",
409315: """Sister: There! Mom bit me!
Me: So what? Now you've become a mom too?
Sister: I thought it wasn't done that way...""",
409316: """from a blog:

I've gotten much calmer about power outages, especially on days off when I don't need to save the laptop. Learned to filter water from the well and heat food in the stove.
But watching "I Am Legend" in a de-energized village - I slightly overestimated my nerves there.""",
409317: """before the exam
ххх: let's buy flowers, a fancy cake, order him 2-3 blondes, hennessy..
ууу: that's *****ov
ууу: all he cares about is matrices and vectors
ххх: let's buy matrices, a fancy basis, order 2-3 vectors, a dot product..""",
409318: """xxx: eh, if Krylov were our contemporary, his fables would be called "the monkey and the camera", "the monkey and the computer"....""",
409319: """Вася: I never even said the word "ass" in front of a girl
Маша: well, you just did
Вася: daaamn
Маша: :(
Вася: Fuck, what kind of day is this""",
409320: """^June^: the Photoshop installer just gave me valuable exam-prep advice
^June^: "To continue, close the following applications: firefox.exe\"""",
409321: """kudjomd: haven't drunk vodka in so long I forgot what orange juice tastes like.""",
409322: """xxx: among our accountants there are legends of some key combination that switches NumPad into Roman numeral mode :)""",
409323: """No matter how you're dressed in an online game, anyone dressed worse than you is a noob, and anyone better is a nolifer.""",
409324: """she: I believe everyone has their own guardian angel behind their back!
*he looks behind his back
he: There's only the chair back there. I think I crushed him... (""",
409325: """drwinter-knives: Finally - hot greetings to the captain of flight QZ7138. By all appearances - a former military pilot. And one who served on "Harriers" or some other crap with vertical takeoff. We took off from Java like on a scramble alert - straight up like a candle. We gained our echelon in maybe a minute and a half, as if "Junkers" were bombing our airfield. For clarity - this isn't a prop-driven cropduster (those all climb fast), it's an A320 "Airbus". A normal mid-range beast. The moment we left the ground - gear retracted and sharply up. I'm used to everything, but my vision went dark. Someone in the cabin howled foully. But overall there was no panic - with those g-forces you can't really scream, it's hard to breathe. And then bam - unfasten your seatbelts, you may queue for the lavatory to change your underpants. Good plane, the "Airbus". Sturdy, didn't fall apart.""",
409326: """I turned on channel 1 for a second and immediately heard words in the Malakhov+ show: "..and then the tram passes further into the intestines...\"""",
409327: """a comment from netlore:
Remembered how on our tracker someone posted the video "5 wet chicks having fun in bath", the dude got banned right away, and then it turned out it's five ducklings swimming in a bathtub.""",
409328: """Sitting, reading a book, my girlfriend(д) is playing WoW as a tauren, hits level 69 and brags about the new abilities
д: now I'm a tree!
me: only now? what were you before?
д: before? before I was just a cow
д: damn...""",
409329: """xxx: You always find an answer that doesn't satisfy me but that I can't argue with.""",
409330: """ххх: my mom repeated "you're on fire" 4 times before I got that I wasn't killing it, I was burning my shirt while ironing...(((""",
409331: """<Мира> I like brutal ones with interesting faces
<неолбанец> like who for example?
<Мира> well, like Travolta, Nicolas Cage
<Мира> Pyramid Head from Silent Hill""",
409332: """xxx: insomnia was tormenting me yet again
xxx: well I think, sweet, don't feel like sleeping, I can work nights
xxx: fell asleep with these very thoughts""",
409333: """xxx: he called me at 1 AM when I was asleep, and I yelled at him... but then I called him back at 6 AM and apologized""",
409334: """Recently dreamed:
standing at a bus stop, waiting for the bus, a huge line. Suddenly a van of our valiant OMON pulls up and everyone gets loaded in for an "illegal mass gathering" =/""",
409335: """xxx: mom says as a kid I was so cultured that on the bus I'd give up my seat on her lap""",
409336: """lopata67(11:59:38): you said you need it by 12?""",
409337: """- Miss, I was just talking to your tech support and I got cut off... Can you connect me again?""",
409338: """Boorawchick: when I come to accounting, they stop working :)
Юлька: what do you do to them?)
Boorawchick: they call it "a man break", and I call it updating the base 1C Accounting package :)""",
409339: """Shato_On
Surgut. A colleague at work started telling about how in Piter they draw the bridges at night, at which another employee got indignant and asked "why would they draw them nowadays", so they explained to her that the Neva is a navigable river and ships sail on it, and they draw the bridges so the ships can pass, at which she was very surprised and declared that she'd thought bridges used to be drawn to defend against the invasion of, attention, the Tatar-Mongols!

Shato_On
I laughed like a Tatar-Mongol horse""",
409340: """> Dear employees!
> Please be careful, the buffet is selling ryazhenka 2 days past its expiration date.
Writing from my phone over Wi-Fi from the third stall? :)""",
409341: """Дмитрий Казанцев: an interesting theory: the Highlander was killed by the pension fund because he'd worn it out
Никита Ивашин: yeah. Duncan MacLeod bankrupted Scotland's pension fund
Дмитрий Казанцев: since then everyone there walks around without pants""",
409342: """xxx: Honey, there's money lying on the dresser. Pay off the loan and with the change (5 grand left) buy yourself those little shoes you wanted!
yyy: And how do you know how much my shoes cost?
ххх: Now YOU know how much they cost.""",
409343: """Renovations. Dad and I are hauling a huge heavy Soviet TV. Setting it down in the snow, dad says contentedly:
- How long I've dreamed of doing this! And mom wouldn't let me, arguing that it works.
- Dad, it doesn't work. My brother and I gutted the radio parts out of it 15 years ago. We were just scared to admit it right away!""",
409344: """A: See, Lenka knows exactly where kids come from. She's got three already, from different men, true.
B: By the way, maybe she doesn't know. Doesn't quite grasp, so to speak, the connection.""",
409345: """A conversation with my 7-year-old:
- ...you see, honey, Ded Moroz has keys to every apartment.
- Now I get why there's so much crime in our country.""",
409346: """...
zzz: If a guy lets a girl play with his joystick, their games are usually not computer ones
ффф: You mean they play on the X-box?""",
409347: """Кот!!!: Who's planning to do what on December 21, 2012?
female_officer: wait, is that a Friday? funny question :-)""",
409348: """Elka799: I'll clean your apartment, sit with your child. Pretty, 2 higher ed degrees. Fast and quality. Preferably the upper part of town.

Neplokhov: Child. 42 years old, boy. Sit with him in some cozy cafe for a couple hours, Nizhegorodsky, Sovetsky district, how much would that cost?""",
409349: """ххх: I had a fight with his mom
ууу: what did she say this time?
ххх: she goes: Ira, go eat, look what I cooked
ххх: opens the duck-roaster and there's a goose, overcooked so much the meat fell off the bones and wings like that sticking out of the broth
ххх: and I blurt out: mmm! yummy! pterodactyl!""",
409350: """ххх: so when are you planning to head out?
ууу: probably leave on the 28th. the bus, I see, runs either Mon through Fri or on Mon and Fri
ххх: excellent, and now the key to the cipher...""",
409351: """xxx: my girlfriend has only one flaw
xxx: she doesn't exist""",
409352: """xxx: And then the seven dwarfs pulled Snow White out of the internet, Photoshop, the e-library, took away her easel, sword, bow, ballet shoes, sewing machine, took her off the horse, took her books and textbooks and stuffed her into a crystal coffin so she could finally get some SLEEEP!!!""",
409353: """from Habr:

Siddthartha:
by the way, the elvish and dwarvish languages invented by Tolkien are way more alive than Esperanto and Klingon, and all other artificial languages. alive in the sense that fan roleplayers communicate in them and have written tons of poetry and prose.
AnatolyB:
Tolkienists are nothing!
Stroustrup invented C++, and so much poetry, prose and fables have been written in it...
Mezomish:
And don't get me started on the jokes...""",
409354: """Husband feeding his son porridge.
A spoonful for mom...
A spoonful for dad...
A spoonful for Ner'zhul...""",
409355: """xxx: Eh, sometimes a remote desktop isn't enough. Quite often you need remote working hands.
yyy: Sometimes even to strangle the client""",
409356: """20 girls and one young man work in the department

Elena: Feb 23rd is coming soon and all...
Elena: I wonder if Misha will have to chip in for a gift?
Elena: and if so - will he get sick on March 8th...""",
409357: """From the women's forum:

Татьяна: girls, I found kefir in the fridge that expired 3 weeks ago. tell me, can I make pancakes out of it?
Bambinа: A friend of mine drinks that kind of kefir to lose weight. Turbo-slim express, she calls it.""",
409358: """<Mut> I see orders to kill in the captcha. what do I do?""",
409359: """xxx: The situation where on Odnoklassniki you're offered friendship by previously unattainable school beauties reminds me of a second-hand store ad.
xxx: These out-of-fashion things have already been worn by someone.""",
409360: """ххх: HELLO, HELP ME, I CAN'T LOG INTO THE DATABASE, IT SAYS THE PASSWORD IS WRONG
ххх: oh, never mind, figured it out""",
409361: """xxx: today I saw the country's computerization with my own eyes
xxx: the janitor had broom twigs tied to the broom handle with a USB extension cable""",
409362: """xxx: And why do pirated programs keep the license agreement for you to read?""",
409363: """xxx: what, what, my Adequate one, stability?
xxx: you propose to me, then tell me to fuck off, then [take me] to Vyborg.
xxx: I might as well open a travel agency - I've gotten so many trips""",
409364: """Яна: my eyebrows are now as sharp as the lads in the stairwell""",
409365: """<OmJen> Parallel parking is when you've parked and you couldn't care less where everyone else parks""",
409366: """xxx: Here we are suffering, not sleeping at night, studying, while our prof is snoring away right now, the bastard..
yyy: He's entitled, he went to classes during the semester""",
409367: """ххх: I really try to understand girls
ххх: fine, panties should match the blouse even though you can't see them under clothes
ххх: but why is she so happy about the pretty little flowers on the ABSORBENT side of the pad?!""",
409368: """xxx: Don't give me that "things were better before". Before was before.
yyy: And better was better. And was was was.""",
409369: """xxx: Explain to a newbie, please :) My passenger seat belt buckle doesn't latch. The car was in an accident, supposedly the pyro charge went off, and now because of that it won't latch, supposedly these buckles are single-use. And you have to replace the whole belt assembly, and that costs 5k rubles! At the junkyard we couldn't find that buckle, since they sell them together with the seats, they don't unscrew them separately. Tell me, can I still replace that buckle somehow or fix that pyro charge? What do I do?

yyy: pyro charges are responsible for separating the pilot's seat from the fuselage during ejection.
did the seat separate?""",
409370: """xxx: And my grandma still has a brand new Sony VCR lying in her storage room, which she, as promised, will give me for my wedding...""",
409371: """absolute_idiot: Everyone just dreamed of becoming a cosmonaut. We all played at being them together, boys and girls, when Tereshkova flew. Before that we didn't accept girls. I remember our orbital food was - the girls ate "Pomorin" toothpaste, and I ate real cream from a tube, for shoes. They got scolded afterwards, and I was the only one taken to the hospital. Mom said it was for a medical, for the squad of real cosmonauts. For some reason they didn't take me, though I'm still on their register.""",
409372: """9th: I've never met worse developers
9th: their hands don't just grow out of their ass, their hands grow INTO their ass""",
409373: """xxx: I want to hear you sing
yyy: I eat quietly...""",
409374: """I once had a VAZ 2115, I was happy with it and almost everything suited me. The only thing that didn't suit me was that there was no air conditioner. And I kept topping up the antifreeze, changed the thermostat twice, and the front struts. Once there was a short circuit, the spark plugs and 3 of the 4 cylinders burned out. The resonator conked out somehow and while driving the sound was street-racerish. What amused me most were the parking lights and the low beams. When I pressed the brake, the parking lights came on, and if I turned them on, the brake lights came on. In winter in the dark the low beams wouldn't turn off on the first try, and if I signaled a right turn, the high beams came on. Then I bought a Nissan, drove a couple kilometers and thought that when I get to hell, part of my punishments will count as served.""",
}

if __name__ == "__main__":
    import sys
    src = "/home/hatch/workspace/translation-work/bashim/chunks/chunk_27.jsonl"
    dst = "/home/hatch/workspace/translation-work/bashim/out/chunk_27.jsonl"
    start, end = 201, 300  # 1-based line numbers
    lines = open(src, encoding="utf-8").read().splitlines()
    assert len(lines) >= end, f"only {len(lines)} lines"
    # Build and validate everything BEFORE appending (avoids partial writes).
    out = []
    want_ids = []
    for ln in range(start, end + 1):
        r = json.loads(lines[ln - 1])
        en = EN.get(r["id"])
        assert en is not None, f"missing translation for id {r['id']} (line {ln})"
        assert en, f"empty en for id {r['id']}"
        assert en.count("\n") == r["text"].count("\n"), (
            f"newline mismatch id {r['id']}: en={en.count(chr(10))} ru={r['text'].count(chr(10))}")
        out.append(json.dumps({"id": r["id"], "ru": r["text"], "en": en}, ensure_ascii=False))
        want_ids.append(r["id"])
    # Resume safety: skip ids already present in dst.
    have = set()
    try:
        with open(dst, encoding="utf-8") as f:
            for l in f:
                l = l.strip()
                if l:
                    have.add(json.loads(l)["id"])
    except FileNotFoundError:
        pass
    new = [o for o, i in zip(out, want_ids) if i not in have]
    with open(dst, "a", encoding="utf-8") as f:
        for o in new:
            f.write(o + "\n")
    print(f"batch lines {start}-{end}: {len(new)} appended "
          f"({len(out) - len(new)} already present)")
