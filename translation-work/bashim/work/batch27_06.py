#!/usr/bin/env python3
"""Translate bashim chunk_27 batch 6 (input lines 501-600). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
409576: """Киса в Образе: I crippled my car
Killerboy: how are you?
Киса в Образе: I'm fine
Киса в Образе: a bruise and a scratch
Killerboy: and HIM?
Киса в Образе: I broke his bumper
Киса в Образе: he backed into me, pushed me. I fell. scraped my arm.
Киса в Образе: he got out and started yelling at me, what the fuck am I walking there for
Киса в Образе: so I grabbed the bumper with my hands and yanked it up
Killerboy: right. and smack him in the face with the bumper
Киса в Образе: the cops were right there though...
Киса в Образе: I only tore it off on one side, but ripped out the mounts
Киса в Образе: I used to sell them, whole and for parts. I know how to yank.
Киса в Образе: so the cops asked for my license. he started hemming and hawing
Киса в Образе: turned out he'd been stripped
Киса в Образе: I batted my eyelashes and said "Boys, can I go?"
Киса в Образе: the boys agreed..""",
409577: """Sunny: "Odnoklassniki" - now your wives and mistresses know each other by sight!
Mr.Laiho: that's why I don't register there )
Sunny: sooo... you have a mistress?""",
409578: """xxx: you feel old when at the alumni reunion people start bragging not about their own achievements, but about their kids' achievements (""",
409579: """xxx: we've got a war going with the neighbors. moving on to cunning strategic actions.
xxx: yesterday they used a drill to bore the wall to the loud bawling of the zombobox.
xxx: and today I played Bach for about an hour, and then SUDDENLY played some heavy metal.
xxx: the effect is stunning. A scream of "fuck!" and a hefty thud on the floor - that's all I could make out""",
409580: """xxx: When was the last time you played ball, or were with a girl? We're all — IT people — deprived of this
yyy: Well, ball doesn't do it for me, but I really love paintball. 3.5 years happily married. It's not us — IT people — who are deprived, it's you — nerds.""",
409581: """A conversation with a friend about zodiac signs.
me: I've got friends, twin brothers, totally different people
her: what are their zodiac signs?
me: aquarius
her: and the second one?
me: I've never seen twins born under different zodiac signs, and these two were no exception.

My friend got offended.""",
409582: """ххх:
let's put it this way
due to the hostility of the environment I've grown a tank around me""",
409583: """Parents talking.
Mom: How do I lose weight?
Dad: Try not opening the fridge!
Mom: Yeah right, any fool can lose weight without a fridge...""",
409584: """LesTaT: the female mantis is humane, she does it to the male's brain only once...""",
409585: """Looked at the share in the file exchanger - found great file paths:

\\\\audio\\Fairy tales and myths\\English in 2 weeks\\...mp3
\\\\audio\\Fairy tales and myths\\How to quickly learn to cook\\...mp3
\\\\audio\\Fairy tales and myths\\Quitting smoking\\...mp3

An honest guy.""",
409586: """xxx: Some people life teaches nothing
xxx: My friend, pregnant with her third, still believes you can't get knocked up from sex in the bathtub""",
409587: """Оранжевая: I'm such an idiot after midnight )
Оранжевая: probably because my head turns into a pumpkin""",
409588: """man (20:28:36 7/02/2011):
I got a text "hi cutie" from an unknown number

man:
oh, all good, it was sent February 8...
at 0 27

man:
I've still got 4 hours to become cute and leave my number""",
409589: """(xxx proving that Russia should've given the Kurils to Japan)

xxx: Russia's losses because of these damn islands equal a big lost war. Korea, Vietnam, confrontations with the US - all of it could've been if not avoided, then softened.

yyy: Oh totally! If we'd given away the islands, Happiness and Communism would've come long ago.
I was looking at a NASA satellite photo somewhere. I look: something triangular showing white. Looked closer - it's the whole world converging in a wedge on the Kuril Islands.""",
409590: """xxx: I look at all these crashed helicopters with VIP hunters and cars with VIP asses, and it seems to me they all have one problem. Due to the customs that have taken shape in such circles they forget how the laws of the RF differ from the laws of physics. And they differ in that the laws of physics apply even to those who have dough and power...""",
409591: """ххх: The new semester started well. A new subject, a new prof. Everything would be fine, if he hadn't finished the organizational matters with the phrase: "And remember - while you don't touch me - I'm kind and soft, but if you get me going - I'll become hard and firm".
Don't even know how to describe him in one word now.""",
409592: """From a job posting:
1+ years of experience. At least 5 successful websites.
"- So you won't hire Zuckerberg?
- We won't.\"""",
409593: """Talking to a buddy. He asks how's my personal life?
me: Ildar, it starts with P and ends with Ts
him: You got yourself a prince?""",
409594: """xxx: our boss is quite tongue-tied and oh god, the way he constructs sentences...
xxx: on Friday it was: "ok kiddos, I'll drive off, take my daughter to Leningradka, or I'll soon have to pay for my son's tuition"...""",
409595: """Yas: Speaking of security. Got a letter from the bank with some ad like "We've opened a new branch in the village of Yedryubryakovo...". In the "To" field A HUNDRED AND FIFTY e-mail addresses. And at the bottom a touching inscription: "IT'S CONFEDENTIAL WITH US. CONVENIENT. SAFE". Bingo, fuck!""",
409596: """otobpa: When will they finally invent a faucet that, when you turn it just a little, changes the temperature just a little, instead of blasting boiling water?""",
409597: """x: Wanna know which Crime and Punishment character you are?
y: Yeah.
x: You're the axe.""",
409598: """from auto.ru: The car was carefully driven by a girl, hence there are body flaws...""",
409599: """xxx: A good interview question: "How are you with Linux?"
yyy: regularly :)""",
409600: """From LJ: For the whole vacation I got only 1 text from home: "Where's your corkscrew?\"""",
409601: """From kids' nightmares: "What if a scary robot comes at night!"
From adults': "And indexes the site crookedly :(((\"""",
409602: """XXX: I wonder, what's his zodiac sign?
УУУ: Light, of the zodiac...
ХХХ: And which month is that?""",
409603: """xxx: we've got wind about 16 meters per second today, and it's gradually picking up. just got back from a smoke break, went into the server room. walked up to the window. then SUDDENLY from around the corner a storm gust carries a sparrow and slaps it onto the window glass like a fallen leaf. for a couple seconds we looked at each other, then it got torn off and carried further. his eyes were completely fucked-up...
xxx: and now I'm kinda scared to walk home after work""",
409604: """xxx: My Dear hung a printed photo on the wall. in a frame. on a screw. on a screw screwed in with a nail file...
yyy: A SCREW SCREWED INTO THE WALL WITH A NAIL FILE?
xxx: yeah, so, got me thinking, why wouldn't they stop horses at full gallop?""",
409605: """My mother-in-law once sent me to the market to a trusted supplier of "Her Majesty's" yard for cottage cheese. Told me to find the saleswoman Natasha with gold teeth and say I'm from N.I. (my mother-in-law). Searching for the "gold-tooth" was fun - there turned out to be four Natashas in the dairy rows. After the password was given, the found saleswoman asked: "And who is she to you?" I didn't deny it. - My mother-in-law, - I say. Natasha didn't take money from me. - Eat, you need vitamins to restore your nerves.""",
409606: """ххх: Yeah, three brothers. The eldest - smart, the middle - so-so, and the youngest - a complete fool.
ууу: By fairy tale laws I'm the smart one.
ххх: By fairy tale laws you should have two elder brothers.""",
409607: """he: and let kind little gnomes come to you then and...
she: no gnomes! I'm voting for sneakers!""",
409608: """xxx: I refuse to understand how people who've been granted digital photography can't pick a normal angle, a normal background, take a normal photo, look at the result and reshoot accounting for the flaws!
Why? Is it lupus or brain parasites? OK, you don't know how to use Photoshop — fine, but if your mug is shiny, dab it with foundation.
No, fuck it! I'll take a photo all sweaty at a shitty party against a carpet, sticking my ass out, and then I'll beg around the internet for someone to erase my carpet, remove the shine from my face and make my ass smaller! Gnomes! Staffies!!! They promise they're, fuck, simps!
zzz: I refuse to understand how people who've been granted an automatic pen can't write "War and Peace"!
xxx: I'm not saying their photos should be epochal, but they could meet their own requirements, right? These people don't aim to write "War and Peace", they just want to look cool. That's all.
As a kid I took photos. My friends and I picked the background we wanted to shoot against, told each other how to stand, how to turn our heads, light-shadow-all that. On a film soapbox! If you like a gothic interior — find one and take a photo there. But no, they take photos in the bathroom!!! That's like writing 4 volumes of text consisting of the word "dick" and then complaining that "War and Peace" isn't coming out.""",
409609: """she: my head kinda hurts
he: did your period end or something?""",
409610: """<iKrvdk> how do I remove ski wax from borscht?""",
409611: """Elims Reveroff: And the baby's gender is determined not by genes but by what the person ate before conception: carrot — boy, peach — girl!
drop any table: before you were conceived - they ate vodka.""",
409612: """Me: The freelancer got the money, didn't upload the project to our server, the client set a hard deadline! I'm panicking!
Director: The contract with the freelancer has his passport details, don't worry! We'll take out some big loan in his name.""",
409613: """R: do you even imagine working at a big company?
D: well I imagine
D: I've got a big company and I work in it :)
R: yeah? how many of you?
D: no, that's just how I imagine it :)""",
409614: """xxx: sunshine, don't be blue! gotta urgently infect you with optimism!!!!
yyy: preferably sexually""",
409615: """xxx: Why does the phrase "The problem can't be solved without having intercourse with Windows" send so many people into hysterics?""",
409616: """=>нескромна: Medvedev abolished winter time
=>Bellator_Mortem: Putin will soon abolish summer time
=>Bellator_Mortem: and Russia will be outside of time
=>Bellator_Mortem: and space""",
409617: """me: "What parents think if you don't pick up: Your battery died - 2%; You're dying - 49%; You're already dead - 49%"
mom: what's this crap?
me: it's what parents think when kids don't pick up
mom: I got that
me: so what do you think when I don't pick up?
mom: that you're a little shit""",
409618: """The domestic smartphone failed to solve the winter-time transition problem, so it was decided to cancel the clock change.""",
409619: """<Estrogen> Basically her chest is begging for silicate, begging really hard.
<segan> Moron, silicone
<segan> It's her mug that's begging for silicate...""",
409620: """xxx (03:33:19 10/02/2011)
OK, bye and good night, I need to sleep, 'cause tomorrow morning it's work. It's good for you Muscovites, you can sleep in the subway, but my work is in the neighboring building, I don't even have time for a smoke :'(""",
409621: """xxx: I love you
yyy: I love you too
xxx: and who loves stronger? )
yyy: King Kong""",
409622: """Наташа: and I just love you very much, I'm very afraid of losing you and that's why it affects you so badly )
Андрей: I wish you'd get zapped with current every time you pick my brain apart""",
409623: """xxx: do you even know your hands grow out of your ass?
yyy: it's more convenient for me, I do everything through my ass anyway, it's a shorter reach.""",
409624: """boink: Your last name's position at the start of the list when homework is checked at school gets compensated in the future when salaries are handed out )""",
409625: """Raduga: I said I'd come right now. why are you still in your underwear?
Котэ: taking them off now""",
409626: """And in our city there's a new thing called "WiFi on board". Free internet started appearing on social buses.
There are still not enough buses, and on weekdays you can only get on them with a running start, flying into the doors feet first. But now, standing on one leg and holding onto your neighbor's member for balance, passing tickets with your other hand, you can always kill time in a traffic jam reading your friends' LJ or visiting your VK page.""",
409627: """Before buying 25 running meters of 3m-wide linoleum in one piece, it's worth reminding yourself that it's a 170 kg roll and it doesn't fit in the elevator""",
409628: """Mr.X: Daaamn... The cell phone doesn't ring or take calls while charging!
Аггель: When I eat, I'm deaf and mute :D""",
409629: """xxx: we spent the whole honeymoon doing renovations.
yyy: very romantic
xxx: *renovantic""",
409630: """Turns out the Russian national football team plays to a backing track.""",
409631: """xxx: Our office-manager girl, to type in caps, puts something on the shift key.
xxx: And today she stuck a matchstick between the shift key and the case and goes: look what I came up with!""",
409632: """My downstairs neighbor is a criminal authority. Sometimes he likes to listen to chanson, quite loudly. Nothing surprising really, but for three days now he's been listening to Elton John, at the same volume. Apparently something has cardinally changed in his life.""",
409633: """HER: And what is love in your understanding
HIM: love... is when every day you think about your loved one, miss them and wait to meet, rejoice hearing their beloved voice, or getting a text. in general, read the wrappers of "Love is..." chewing gum""",
409634: """Dad: Igorek, the antivirus turned red again, what do I do?
Me: Don't sit in your underwear in front of the monitor, it's shy.""",
409635: """ххх: how many wars: senseless and fierce...
ууу: started because of fuckheads.""",
409636: """News headline: Vatican banned confessing via iPhone

xxx: Yeah, they've got a long score to settle with apples!""",
409637: """"Guys, never ask "Can I kiss you?". Kiss without asking, in a rush of feelings. And you'll be happy"

1st comment:

yeah right. A friend of mine froze to a swing like that once.""",
409638: """A friend(д) texts his mom(м) so she won't worry:
д: Mom, I'm at the Club, will be late
м: Sasha, what fucking club, it's 11 PM, you're 25 already!""",
409639: """talking to a guy on the phone, he sends an alarm to a friend via the agent
me: it's pointless, he's got QIP.
him: what's he got?
me: QIP.
him: what?
me: QIP.
him: is he sick or something?""",
409640: """lilumi: the best implementation I've read about was when the server with accounting was placed in a car in an underground parking lot with a Wi-Fi connection to it, and during an inspection the car just drove away
Vertex: yep. and then: guys, our accounting got stolen!!!""",
409641: """ZloVreD: when a tangerine has lots of seeds - is it a girl tangerine or a boy tangerine?
Katarina: a gopnik tangerine""",
409642: """Q: Where can I find a job where I don't have to grind and they pay well?
A: Go to a miser, he pays twice.""",
409643: """XXX: you're just who I need! I really need help from a normal physicist
YYY: I'm a girl, kind of
XXX: everyone has their flaws!""",
409644: """secretary: You count and count in your head and it comes out fine. But you take a calculator, count, and it comes out expensive.""",
409645: """hostadmin,
I've got a friend who both sailed and floated on a submarine and he said they were ordered strictly to navigate only by GLONASS. So the sub surfaces and waits from 30 minutes to 1.5 hours till it gets positioned. True, that was 2 years ago, maybe it's better now.

Koshelew,
Of course it's better — sink that many satellites, and submarines will have it good now.""",
409646: """Soapy answers,
xxx: wow. how do I drop a passenger from a 2-3 seater mount?
isden: man umount""",
409647: """Влад: and how do I become an alcoholic
Настя: you take booze and become an alcoholic
Влад: what's that about
Настя: about how when you write, I want to take a red marker and correct it right on the monitor""",
409648: """Теххи: my husband was just convincing me that Cthulhu has woken
Vadson: judging by my accountants - he's even had breakfast""",
409649: """XXX: In the Middle Ages the only normal way of reading was reading aloud. Reading "to yourself" was learned relatively recently

YYY: man, their subway must've been a madhouse""",
409650: """Mike: can't open my watch :( the battery died :(
DZ: the one in the bottom right corner?""",
409651: """xxx: in my youth I also went to the disco.
xxx: the local DJ, well acquainted with the ways of the golden youth, asked before starting: well, what are we doing? fighting or dancing?""",
409652: """Questionnaire:
So, everyone decided what you'll give your halves?

Comment:
Yeah. To my lower half I'll give new pants, and to my upper half - a reader.""",
409653: """xxx: sitting in the kitchen, minding my own business, from the neighboring apartment a piercing scream of a little girl: "I POOPED". it was hard to hold back, my yell rang out through the whole house: "Me too". in response wild laughter of the young parents from the same apartment.""",
409654: """I walk into a digital tech store, the seller is changing the e-book price tag from 11000 to 9000, I ask - why did it get so much cheaper? "Our New Year discounts have ended" - the seller honestly answers""",
409655: """ттт: Good afternoon) I need it for a project, so I'm asking for help. Can you suggest movies showing parents abusing their kids or raising that topic.
ккк: Would Star Wars 4-6 work?""",
409656: """<[sky]NinGAZ> it's not sugar outside (
<[Sky]Куплю_БК0011> [sky]NinGAZ: That's snow!""",
409657: """xxx: actually, I'm interested in all options - for parts, trade for a PDA, phone. But if there's really a chance to fix the display in a laptop within 3.5-4k rubles, it's worth the risk

xxx: my heart aches, I can still hear that crunch from under my brother's foot""",
409658: """xxx: everything fell into place - by the Indian horoscope I'm a deer.""",
409659: """xxx: Of all methods of changing government over the last decade, the Polish one should be recognized as the best.""",
409660: """xxx: I just saw that I've got a lucky passport number
xxx: out of 6 digits both here and there add up to 10
yyy: eat it)""",
409661: """xxx: Why does Egypt need democracy, there's no oil there?""",
409662: """Comments on the video of the hydrogen bomb (100 megatons of TNT)
x: And why does it have a parachute? So it falls slower?
y: so the locals have time to film it on their phones""",
409663: """manastorm: crazy owls killed me
manastorm: I fell off a mountain
manastorm: 3% left
manastorm: and they beat me to death
shenna: "Some dick from the mountain" — thought the owls
shenna: and killed him""",
409664: """She: hiiii how are things??????????????? what are you up to???????????
Me: Hi. I think I already asked you not to put a bunch of question and exclamation marks.
She: FINE I WON'T ABOUT QUESTIONS AND MARKS ANYMORE!SO HOW ARE THINGS WITH YOU?""",
409665: """xxx: on the question of inconspicuously carrying an AKS for outsiders
xxx: in the army during a surprise inspection we managed to roll mortars from trench to trench unnoticed by the inspectors, so they'd see ALL guns perfectly clean and painted, not just those two that actually existed.""",
409666: """- I bought my wife.
- How's that?
- I paid for 5 years of tuition, by the 5th year I realized I hadn't gained any knowledge. Decided that so the money wouldn't go to waste, I'd meet someone from the group. And we got married.""",
409667: """<xxx> Damn! Why, why? The ONLY two emails that landed in the "spam" folder in four years - job interview invitations? Noticed two weeks later.""",
409668: """xxx: This morning I opened the wardrobe and you know what?
xxx: better Narnia were in there than this fuckup!""",
409669: """ххх: how was your Valentine's day?
ууу: well how else would a single girl spend it?
ууу: I got up early in the morning, had breakfast, put on a snow-white winter jumpsuit, got into a snow-white car and drove to the snow-white mountains to snowboard, and there the sun is shining, birds are singing, I was completely alone there, imagine, such beauty, utter bliss!!!!! though for some reason the thought kept nagging me - what if I break my leg, then better the left one, or I won't get away, no, breaking isn't an option either, better to dislocate....
and you won't believe=(((((""",
409670: """xxx: Today I was walking to the post office to transfer money. In front of me a granny of about 80 slips on the ice and falls flat... I run up, help her up, offer to help her get wherever she needs, to which in response: "Fuck this post office!", she turns around and briskly toddles back to her warm little apartment...""",
409671: """My wife is talking on the phone with an insurance company rep, I hear a snippet of the conversation
- Yes, miss, that's right. The citizen's on my husband, the helmet's on me...""",
409672: """A comment on the movie Troll Hunter/Trolljegeren (2010)

xxx: The film tells the hard fate of a Forum Moderator. :)""",
409673: """xxx: Throwing myself into your arms and smoooch-smoooch-smoooch!!!
yyy: Where exactly are you throwing yourself?
xxx: What do you mean where? Right between the arms!
yyy: Well what can I say, you're lucky my arms don't grow out of my ass...""",
409674: """xxx:
I work at a cell phone store. Yesterday this domineering lady comes in, buys an electronic photo frame for 4000 rubles. Before leaving the store she stops at the display with all kinds of little cases, takes out her phone and calls (presumably her husband):

"Hello, well that's it, I bought Sashka an iPad for the end of exams...\"""",
409675: """From the conversation about canceling the switch to winter time:

xxx: All kinds of cows are indignantly freaking out that they're suddenly milked and fed an hour earlier/later than the usual schedule. Big Macs will have a hint of despair afterwards.""",
}

if __name__ == "__main__":
    import sys
    src = "/home/hatch/workspace/translation-work/bashim/chunks/chunk_27.jsonl"
    dst = "/home/hatch/workspace/translation-work/bashim/out/chunk_27.jsonl"
    start, end = 501, 600  # 1-based line numbers
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
