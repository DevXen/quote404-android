#!/usr/bin/env python3
"""Translate bashim chunk_27 batch 4 (input lines 301-400). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
409375: """<Зой444ик> I've got 6 Warcraft figurines at work
<Зой444ик> got them as a gift
<Зой444ик> 2 chicks, 2 dudes, a gnome and an ugly one
<Зой444ик> my husband said I just described my department""",
409376: """Мавро: which underground should I dig up a girl from?
RG: the second one. the graphics are better there""",
409377: """ххх: Don't you remember what's the omen about pigeons landing on the window?
ууу: They'll shit on it
ууу: That's a hundred-percent accurate omen, I'm telling you.""",
409378: """Suslichek: I've seen morons of course, but sending a contract and invoices as a PowerPoint presentation...""",
409379: """alconavt: wanna start a new life, find a wife, quit drinking... where do I start?
Сухарь: probably with your nick""",
409380: """<gmb> mom cheered me up: decided to take a bath with sea salt, took the little box, poured it in, then feels like it's somehow not dissolving, looked at the little box - "white beads\".""",
409381: """xxx: Freezing like a bitch, and at home there's no one to cuddle up to except the radiator :(
yyy: you had a parrot :)
xxx: It squeaks""",
409382: """My grandma on SKYPE: "A good thing! It's like having guests over, but you don't have to feed them!\"""",
409383: """xxx: Sharing is good in general. And here's a simple example: I was once just two cells, but division made me a human!""",
409384: """- Got an awl? I urgently need one!
- I do. But I can't pull it out.""",
409385: """xxx: was cutting with the wrong side of the knife, amazed at how dull it was
xxx: should've been amazed at my own dullness""",
409386: """born: well they definitely match my player design-wise )
blind: I've seen your player, design is very important to you
blind: the Nokia battery is taped to it so elegantly with glamorous transparent tape""",
409387: """xxx: Little brother is examining a crossword with a smart look.
xxx: Reads out loud: "Karl Marx's Work", and matter-of-factly answers: "Buratino\"""",
409388: """Mom calls:
- I stopped by your place, brought you a blouse. Grandma and I knitted it, such a beautiful one, it's in the trash can
- ??!!!
- I also bought you the trash can, like you asked!""",
409389: """From a squabble in the comments:
ххх: Yes, due to the PRESENCE of a sense of humor in me, I often use not the most common words, so that the JOKE has a place to be.
ууу: May I ask what made you capitalize those two words?
ххх: A stuck shift key.""",
409390: """From LJ:

Accidentally discovered fog lights in my car. Working ones. After a year and a half of driving it.

Need to reread the manual. Maybe there's more stuff in there.""",
409391: """Евгения: Evgeny, why are you red and offline?
Evgeny_T: I'm a tomato""",
409392: """From an online game forum. End of January.
xxx: I got hacked around the end of November. Couldn't write earlier because I was in sorrow""",
409393: """xxx: Yesterday I swam in an ice hole!
yyy: lucky
yyy: and we never found an ice hole
xxx: There must be one
xxx: You searched poorly )
yyy: it's +10 here, we searched well""",
409394: """xxx: Singing We Are The Champions in the kitchen.
xxx: Little brother comes up and asks who's singing it?
xxx: I say - Queen, I think.
xxx: He answers - well let THEM sing it, not you!""",
409395: """ыыы: my dad collects antique vases at the dacha.
ыыы: vaz-2101, vaz-2102, vaz 2105...""",
409396: """xxx: You also have the new torrent version? It's kinda ugly.
zzz: A swan is beautiful. A torrent should download.""",
409397: """ххх: a friend of mine changed 5 boyfriends in the last six months
ххх: and her status "honey, you're the best in the world!" hasn't been updated in 207 days""",
409398: """<Henry Kane> Oh, cool test. Determines physical fitness by heart b e a t i n g.
<Allgood> by what? :-о
<Henry Kane> heart b e a t i n g, damn...
<Henry Kane> I've already come to love the local anti-profanity filter.""",
409399: """x: freelancers suck, they can spend a month writing that everything's fine and then vanish for good. gotta rent an office and keep everyone in that office.
y: yeah, I had a friend, a great programmer B, who got up from his desk, said "be right back", and a month later appeared in ICQ from Irkutsk. turns out - he suddenly realized he needed to talk to a shaman, and went.""",
409400: """A company of programmers looking for an apartment for an office:
>illidan: although, I'll play on pity, like "we poor programmer, ya! there's 5 of us, we live quiet-quiet, fingers on keys tap-tap" ))""",
409401: """xxx: today I went to sign a contract with the firm LLC "Necropolis". suggested they change the wording "gen.director" to "supreme lich\"""",
409402: """MardJane: apparently I can make a man cry for hours
kobold: did you format his hard drive?""",
409403: """Олег: it's hard to talk to a person all of whose emails start with the phrase "you piece of moron, and don't even argue\"""",
409404: """From Lepra

Sergeyev: I once got a taxi driver who used the cracks on the broken windshield to explain our route. He imagined a map of Moscow in them, and got angry that I didn't get it.""",
409405: """НаташаНеВарлей: My sweetheart somehow learned that vitamin C dies at +60 degrees Celsius; meaning it's "useless" to drink hot tea with lemon in principle.
НаташаНеВарлей: Now he puts lemon in hot tea, and waits for it to cool down.
НаташаНеВарлей: Sincerely believing the little vitamins will come back to life in cold water.""",
409406: """discussing a kid's conjunctivitis
Deniska> and also rinse with tea brew. you just squeeze the bag right into the eye, only he'll scream....
Naevus> He won't, if you let the bag cool down first""",
409407: """xxx: ...this happens to me in half the cases!! Well maybe not half, but in one out of two for sure...""",
409408: """<F1ReB4LL> work isn't a wolf, but beatings, excuse me, aren't baby hares either""",
409409: """111: can you reinstall a computer?
222: windows?
222: or from place to place?""",
409410: """From LJ:

I recognize myself looking at my guinea pigs. They clean your cage, sprinkle deodorant, bedding, sawdust, hay, food, veggies, pour water, hang salt, put toys, wash the hammock, smooth everything out, fluff it up, prettify it... They let you, the idiot, in there - and you immediately start racing around the cage with tragic wails:

- Oh God, God, where's all the shit?!""",
409411: """Zoi4: they finally finished the building in our yard under the affordable housing program
Zoi4: the first low-income folks have already started moving in
Zoi4: because of their Lexuses and Infinitis you can't park in the evening for shit...""",
409412: """Raskalbas: we had a comrade in class who made himself an exact cardboard mockup of a computer, inserted a sheet of paper into the monitor, and wrote little Pascal programs on the sheet and enjoyed life.
Raskalbas: then they bought him a real PC, nobody's seen him since""",
409413: """xxx: Status: "You can't trust any one, ever...."
xxx: girl!
xxx: well, Rosenthal at least you can!""",
409414: """yyy: and why do pirates even need that hook? it's inconvenient in battle, and in life too
xxx: to pick up chicks""",
409415: """xxx: a car dealership site, in the BMW X5 section, topic: "Help"
"recently accidentally bought a new car, just can't get used to it..."
xxx: wish I could accidentally buy a new X5 like that...""",
409416: """(forum topic "Have you ever done inadequate things?")

X: A distant relative of mine, at about nine, left home alone, tore off all the leaves of a big ficus and by mom's arrival had sewn them all back on. With white thread. Mom didn't regain the gift of speech right away, and what guided him he could never explain, even grown up
Y: Amazing :)
X: And all his life he's been furious at the question - "But why WHITE thread?" :)""",
409417: """she: Dickunov!
he: honey, why do you always call me by my last name?!
she: I really like it, I want one like it : -[""",
409418: """danny_bo: She's actually an exceptionally cultured girl. She's got Dostoevsky lying in her bathroom. With whole pages.""",
409419: """This morning I'm cooking porridge for my kid, a comrade calls. I tell him: "Hold on, I'll finish the kid's porridge, turn on the computer and ping you on ICQ". The kid comes running from the room with an indescribable expression of happiness on his face: "Dad!!! Once I eat my porridge, I'll get strong and hit Vaska too!!!\"""",
409420: """1> We're moving to a new building here
1> Second week hauling equipment, pulling networks
1> We've already laid something like 15 km of twisted pair
1> Guess what was wrong with the rainbow's colors in my dream today :(""",
409421: """xxx: News on Yandex: "United Russia" again proposed removing Lenin from the Mausoleum
xxx: they might as well propose putting Putin in there right away
xxx: let him get used to it""",
409422: """Exam season, brains slowly boiling, all thoughts only about the anatomy I need to retake.
The phone rings, in it a bot with a cheerful voice:
-Hello! Are your joints bothering you...?
And I just yell across the apartment
-No-o-o! I passed the-em!!!""",
409423: """Наталья: I answered your anti-spam in ICQ, why won't it let me through?
Вадим: because the first president of the USSR is not Earth )""",
409424: """xxx: It's all lies, bullshit and a promo campaign...""",
409425: """VTsIOM: up to 15% of Russians are ready to bring money to MMM-2011.
Now we know the exact number of complete idiots in the country.""",
409426: """MaximAL:
Bought a book on effective management. The first chapter says you need to delegate more authority to others. My deputy is finishing it.""",
409427: """Activity: playing hockey with a sword
comments: knight, damn it.""",
409428: """<Henry Kane> To be known as a cool nonconformist on the internet, it's enough to speak properly, enjoy life and not belong to any parties or subcultures.""",
409430: """xxx: Group sex appeared when Cupid started using fragmentation grenades""",
409431: """Ezh: went to get the new Popular Mechanics today.
Ezh: can't find it anywhere!!!
Ezh: had to take Cosmopolitan (""",
409432: """SerW: Antivirus for Win 98 SE. Advise, plz. The comp's a Pentium 2, 64 megs of RAM.
qwe123: viruses have higher system requirements these days!""",
409433: """From LJ:

- And what's fashionable with 11-year-old boys now? My son needs it for his birthday so they won't laugh at him at school. Touchscreen, I guess? Probably not Samsung? Or what? I've fallen out of the trends. I know sites, but friends' help is more useful. Advise.

- Let him choose himself. Mine's 6. I gave him a psp - and it turns out he wanted a book about dinosaurs, had to give two presents.""",
409434: """xxx: I wonder, does the conservatory have a finance and management department yet?""",
409435: """Naoe: Listen, what's the most epic fail in your opinion?
Maya: Deleting your thesis from the computer a day before the defense.
Naoe: Now picture this. Frodo and Sam are walking to Mordor. Walking long. Almost dying already. Practically crawling up the mountain. And then Frodo stops and suddenly says: "Sam... I think I lost the ring.\"""",
409436: """Client <19:33>: We need to test IP blocking. Try it on my address: xx.xx.xx.xx
Client <19:41>: URGENT!! The site is down!!!""",
409437: """<ххх> Wikipedia: In Greece breakfast is often absent, while dinner is considered the main meal and is very heavy.

<ххх> we're all Greeks here in the dorm!""",
409438: """Ольга: my sister finally found her other half.
Ольга: she's sitting with her hubby in the next room right now, watching porn.
Ольга: laughing like idiots.
Ольга: I feel like they'll get married soon.:)""",
409439: """<ForNeVeR> That's why I don't like weekends. You just get in the mood to sit by the fireplace with a cup of coffee in a rocking chair...
<ForNeVeR> And then BAM. Turns out you have neither a fireplace, nor coffee, nor a rocking chair.""",
409440: """Amdi: can you call me?
Stray: why? miss me?
Amdi: as usual, can't find my phone )
Stray: calling
Stray: I'm probably saved in your ICQ as "Phone Finder\"""",
409441: """Fletch: So, I've got a friend from my old job who really showed off badly. Asked me to drive him to the trauma center - needed a cast removed from his leg. We arrive, I'm sitting waiting for him in the hallway. This handsome devil comes out of the office doors and, overjoyed that he can finally walk normally (spent 2 months in a cast), jumped, clicked heel to heel in the air, stumbled on the tile when landing, fell and broke his arm... They let him back in without a queue.""",
409442: """White Devil: I wanna go to the sea, just don't want our south anymore
Rapid: go to the Laptev Sea""",
409443: """xxx: Been to medical commissions.
xxx: First I was getting a driver's certificate and there the eye doctor said: your vision is totally fucked, you won't even see a traffic light.
xxx: And at the draft board they said: excellent vision!
xxx: In the end, they won't give me a license, but I'm fit for the army right now.
xxx: So I'm wondering, where did they screw me?""",
409444: """- Hey! Why aren't you picking up?
- I was in the bathroom.
- For two months?""",
409445: """tiro: a girl, upset, bought herself a hamster. The list of texts over an hour and a half:
1. "Honey, his food smells so tasty!"
2. "Honey he's bathing!"
3. "And he spins the wheel so cutely!!! He only eats and spins! But mostly spins. I'm afraid, what if he gets tired!"
4. "Honey, he's been sleeping for 20 minutes already! What if he's sick?"
5. "What if he didn't like me? I scrubbed my room clean for him!"
And I thought I loved strictly logical and rational girls...""",
409446: """Гошка: Turns out I had a girlfriend, imagine that.
Гошка: Today she dumped me!""",
409447: """Corporate party. Tourist base. A cabin with a Russian stove. our admin walks in with an armful of firewood. The immediate thought: "all correct - the admin came, brought firewood...\"""",
409448: """XXX: before the divorce I was snooping through my husband's phone
XXX: I confess, I'm guilty
XXX: found random voice recorder entries there (the recorder button is separate, inconvenient, gets pressed often)
XXX: and found one extremely interesting one. They were clearly driving in a car, he was saying something, periodically asking why she was sulking. And a female uh-huhing voice
XXX: Clearly offended
XXX: Good thing that before going to grab the rolling pin and shove it up his ass I listened to the end and waited for the female monologue.
XXX: in which I recognized myself with horror...""",
409449: """Dina: I'm always driving myself, but this time I walked to the store.
Dina: ice, slippery. a dude on the right slips and flies at me.
Dina: first thought: fuck the doors, the bumper and the front fender.""",
409450: """(15:32 24/01/2011)
xxx: they gave me my advance. for December. appreciated the joke - writing my resignation""",
409451: """"Toolbox talk" at the construction site on coordinating utilities between subcontractors.
Contractor 1:
- It's impossible to work like this! we just mounted a pipe - and they cut it right away!
Contractor 2:
- Bullshit! we're waiting for you to paint it!""",
409452: """Discussing the torrent "The Very Best Movie 3-D"

ххх: For a CAMRip the quality is excellent, the camera doesn't shake, you can't see people in the hall and there's almost no laughter
УУУ: Well the laughter part is clear...""",
409453: """from Habr:
Alas, last year TI pretty painfully "screwed" me with their Stellaris contest, where I managed to register, write the software, solder a small board with the necessary electronics waiting for the right processor, and then find out that under US laws it's forbidden to send high-tech products to underdeveloped countries like Zimbabwe, Russia, Ukraine, Somalia etc., so the natives won't get distracted from gathering coconuts.""",
409454: """<Prelest> so what's the problem, he seems like a normal guy, earns fine
<Lady_L> the problem is he's FUCKING NUTS!
<Prelest> but you love him :-*
<Lady_L> I can't even explain to you HOW nuts he is.
<Lady_L> like, for example...
<Lady_L> a year ago I complained that it's tough without a car. without much hope in principle.
<Lady_L> for my birthday he STOLE A FORD FOCUS FROM AN ACQUAINTANCE and rolled up to visit me in it!!
<Lady_L> now I'm afraid to bring up kids.
<Lady_L> what if he steals those for me too.""",
409455: """Russia is the homeland of Elephants (eared, high off-road capability — see Mammoth).
Besides, Russia has many: Universities, Factories, there are also Schools.
There are many hedgehogs in Russia.
And next to Russia there's Lithuania, which is the homeland of Socionics.

That's what the Russian Wikipedia looked like just a few years ago.""",
409456: """Our sysadmin writes:

xxx: Boss, I'm bored.
xxx: I'm tired of sitting on VK, is everything working for you?
xxx: Can I crash the server?""",
409457: """Юрьич: Yeah, it's hard to work under a person you'd never hire yourself.""",
409458: """xxxxx: Can you call a person who wrote "good" in the form field labeled "DO NOT FILL IN" a moron?
yyyyy: you can call the person who makes a form with a "do not fill in" field a moron!""",
409459: """XXX: one prof at our department got three liters of pure alcohol from some smart correspondence students, now he doesn't come to work""",
409460: """xxx: "Besides, the mouse is designed specifically for couch use. Its body moves freely over soft surfaces — upholstery, bedspreads, clothes."
xxx: The chances of getting off the couch keep shrinking.""",
409461: """xxx: my work day started fun today. Walking through the shop, the work mobile rings, the phone's new, I didn't give it to anyone except my colleagues. An unknown number shows up, I pick up. A dude goes hello, I called your home, your wife gave me your number, are you the one selling hay?
ххх: I didn't even know what to freak out about more, that I suddenly have a wife, or that I'm selling hay...""",
409462: """xxx: He's done for
xxx: Instead of an iPhone he bought a Roomba vacuum
xxx: Installed Linux on it
xxx: Plans to hook up a GSM module and make calls from the vacuum""",
409463: """xxx: How can we not have fun and not use the internet
xxx: a wonderful neighbor has moved into our building!!!
xxx: Thanks for the wifi!""",
409464: """xxx: something's wrong with the hard drive, probably need a new one
yyy: I need a new one too. a terabyte one
xxx: why so much?
yyy: so I stop being nervous
xxx: a new kind of claustrophobia, fear of a small hard drive volume?""",
409465: """White Wolf: Pope Benedict XVI called on people communicating on social networks to only list truthful information in their profiles. This was stated in an address on the occasion of the World Day of Social Communications. The pontiff noted that youth often give in to the temptation of creating a false virtual image, misleading their interlocutors.
Orlangur: friend thy neighbor as thou wouldst friend thyself
Orlangur: thou shalt not covet thy neighbor's account""",
409466: """A friend was going to a job interview the day before.
I text him in the morning "What time should I scold you at?"
My friend's reply: you can start already, I overslept""",
409467: """xxx: whoever watched the Langoliers sleeps on planes""",
409468: """maxwell: hey, whatcha doing?
yourdeadline1: watching groundhog day
maxwell: hey, whatcha doing?""",
409469: """VM: A new punishment for Russian officials: A Strict Presidential Reprimand on Twitter with entry into Facebook""",
409470: """Getting ready for the wedding and sorting out the car question.
- Lesh, can we count on your brother's car?
- Better not, it might be in not very good shape.
- You mean it might not be washed?
- I mean it might be missing a door.""",
409471: """bogomol: Yesterday my son saw the Timon and Pumbaa cartoon for the first time... never has anyone suffered so much from the house running out of cockroaches.""",
409472: """Lorat: you've got an energy-saving bulb in the bathroom?
Вчерашняя: yeah
Lorat: I got scared, washed my face, and it immediately got brighter...""",
409473: """<unhack|wrk> someone came up to me here and wants something
<unhack|wrk> let me take off my headphones...
<unhack|wrk> yeah. much clearer now""",
409474: """XXX: I've got "Axe: Dark Temptation" shower gel
XXX: If I understand English at all, that translates as "axe: dark temptation"
XXX: Raskolnikov's gel, damn""",
409475: """Happy people don't watch the clock, days, months. Russian Post.""",
}

if __name__ == "__main__":
    import sys
    src = "/home/hatch/workspace/translation-work/bashim/chunks/chunk_27.jsonl"
    dst = "/home/hatch/workspace/translation-work/bashim/out/chunk_27.jsonl"
    start, end = 301, 400  # 1-based line numbers
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
