#!/usr/bin/env python3
"""Translate bashim chunk_30 batch 1 (input lines 1-100). ru is read verbatim
from the input file; only en translations are embedded below."""
import json
import re

EN = {
412082: """xxx: Fuck, left the car in the courtyard with the windows open
xxx: SHARED IT, SO TO SPEAK""",
412083: """xxx: this goddamn exam season!
xxx: I'm sitting here, you know, goofing off.
xxx: and suddenly I realize: something's eating at me...
xxx: I realize: it's my conscience - I'm sitting here goofing off, not studying anything.
yyy: but you turned in everything the day before yesterday
xxx: and that's what took me a while to get...""",
412084: """xxx: Regular sex protects your computer from the viruses you can catch on porn sites.""",
412085: """sup: describe, pls, your sequence of actions preceding the appearance of the error
us: Boiled the kettle, put a tea bag in the mug, poured the hot water. Walk up to the computer - and there's this shit...""",
412086: """My girl did a cleanup at my place and threw a fit: "What bitch is leaving hair clips at your place?!"
Spent ages explaining to her what a guitar capo is and how it works.""",
412087: """xxx: mom left a magazine on the table called "wedding"
yyy: leave a magazine on her table called "forever alone\"""",
412088: """Discussing driverless cars on public roads on Habr
xxx: wish they'd roll out automated buses already. sometimes it's way too scary riding with living drivers.
yyy: You think robots won't blast blatnyak at full volume? Naive! «Vladimirsky server, the ping is unmeasurable...»""",
412089: """xxx: According to folk wisdom, the first thing you let into a new home is the internet cable. And wherever it falls - put the bed there.
xxx: And the table.
xxx: And the computer.
yyy: And the grub.""",
412090: """hesses: I work at drilling sites. When the construction boss runs around yelling "We need more pylons!!", I feel like I'm playing good old Warcraft again...""",
412091: """xxx: guys I don't get it. are casters really the strongest class or what? I just started playing a caster (LVL 23) AND BEAT A LVL 29 ARCHER ) BUT THEN I TRIED ONE AGAINST A MORE SKILLED ONE AND HE KNOCKED OFF TWO THIRDS OF MY HP. AND THAT'S ONLY AT LVL 23.
yyy: TOO BAD YOU CAN'T MAKE THE LETTERS EVEN BIGGER, RIGHT?""",
412092: """xxx: "only in Russia could something like this happen..."
xxx: Have you actually lived in other countries, to say that?
xxx: Then don't talk about what you don't know!
yyy: yeah, only here do people talk like that""",
412093: """xxxx: Can it be that even in America, with all its dumb laws, nobody thought of banning barbers from shaving a long-haired person bald when they're in a state of passion or intoxication?!""",
412094: """xxx: why is she so angry?
yyy: not getting enough sex
xxx: what about her husband?
yyy: he's getting enough =D""",
412095: """<ххх> I'm increasingly convinced that whole-bean coffee is only grown so they can photograph that mountain of freshly roasted beans and slap it on a jar of instant crap.
<ххх> which, apparently, is genuinely mined in mines (((""",
412096: """xxx: All our projects are in such deep shit that the cleaning lady got named employee of the month""",
412097: """malivnik: well how do I put it...
malivnik: sometimes it seems to me the world has finally gone totally fucking insane.
malivnik: the rest of the time I'm firmly sure of it.""",
412098: """<montavista> I only believe in the German auto industry
<montavista> A friend told this story. There was this thing on the railroad: a train (not very fast) is approaching a crossing. A BMW X5 is about to pull onto the crossing. The driver slams the brakes, of course, but what good's that? He slams into the beamer, it flips over several times and lands in a ditch. A minute later a well-soused guy climbs out of the car (in one piece!), walks up to the train crew and asks a question unmatched in its epicness:
<montavista> "Guys, are you all okay?\"""",
412099: """Padus> I, by the way, don't understand what these cards are that "MUST NEVER BE TOUCHED". When I was a kid my mom told me about one thing that must never be touched, but it turned out that touching it is exactly when the fun begins.""",
412100: """[Little-StrawBerry]: Lyosh, how do you say strawberry in English?""",
412101: """Q-man: It's impossible to argue with you
Q-man: Can you argue your point of view somehow other than with "cool" and "not cool"?
Mousehead: I can
Mousehead: but that wouldn't be cool""",
412102: """Woke up today at 3 AM from a call.

man: Hello. What city is this?
me: Krasnoyarsk
man: Getting warmer...

and hung up""",
412103: """From Habr

xxx: Today I'm not going to tell you why I love Arch linux, you'll see for yourselves.
The task — install Arch linux so that the root partition mounts over the network from a Samba server

yyy: The task — dig around in your left ear with your big toe.
That's why I love acrobatics.""",
412104: """xxx: colleagues, good morning! tell me, who took on a student intern into the department? need a consultation...
yyy: so where do we put the body now?""",
412105: """Olya: my head started hurting
Olya: you're after me again?""",
412106: """Friend: Did he yield to me as an obstacle on the right, or as a girl?""",
412107: """xxx: did you talk to the landlord about the phone?
yyy: yeah. he swears he paid for everything, and as for why it doesn't work — well, probably rats in the basement chewed through the cable
xxx: yeah right, and the rats sent the non-payment notice too? So nobody would suspect them?""",
412108: """xxx: Don't wait for the girl to make the first move. Because she's waiting for it from you ...
yyy: Very relevant for a young father teaching his toddler daughter to walk.""",
412109: """xxx: Think with your ass successfully just once, and suddenly everyone's like - intuition!!!""",
412110: """Т.М.: Lyokh... I clearly have an extra chromosome.... I'm boiling potatoes and eggs in one pot, this is the second time I walk over, poke the eggs with a knife, feel they're still hard, which means they're not done, and sit back down at the computer""",
412111: """grother: yesterday this guy came to assemble our cabinets, all gloomy. The secretary asks him all cheerful - ARE YOU THE MASTER? And he says all sad - I'm a slave...""",
412112: """lizi: I love grannies.
Came in with a cane. Left without it. She briskly runs after her, takes it, and walks back, slowly limping.""",
412113: """xxx: pelmeni manufacturers are such sadists
xxx: I suffer because of them
xxx: they put exactly 5 more dumplings in the pack than I can eat
xxx: and I suffer, but I eat. can't throw them out, right""",
412114: """XXX:
C# for schoolkids
Course info
This book introduces the young reader (expected age - 12-16) to object-oriented programming using real programs in the Windows environment.

YYY:
cool, we only studied dumb BASIC in 11th grade

XXX:
Can't wait for the book for the littlest ones, "Assembly in pictures\"""",
412115: """XXX: stop making fun of the guy, he asked a normal question, he's just inattentive!
YYY: on the next street over there was an inattentive guy crossing on red. a truck banned him.""",
412116: """gamer: :'(
Дашенька: Paaaaash, are you crying or what?
gamer: the smiley is crying. but I'm a man. I'm outraged""",
412117: """Zzayo: Our whole life is a game. Right now I'm level 24, grinding and earning gold to buy a house where I can store all the artifacts dropped from bosses""",
412118: """XXX: 25 years have passed since the nuclear war. You're one of the survivors, emerging from the bunker for the first time and walking through post-apocalyptic Moscow!

Not far from you, you notice a shabby bench, miraculously not destroyed by the nuclear blast. You sit down and realize that in these 25 years nothing has changed... then you go back to your cozy bunker and spend 2 nights scrubbing the DAMN yellow PAINT off your anti-radiation armored pants!

XXX: I hate it!""",
412119: """zojl: It was a good pair, pity it fell apart.
orisava: Should've crimped it.""",
412120: """xxx: I bought my wife a netbook
yyy: I also bought my wife a netbook, because I had a desktop and a PDA phone, and she had nothing at all. Now I have a desktop, a netbook and a PDA phone. And she has nothing at all again. I need to think of what else to buy her.""",
412121: """xxx: I don't know about air fresheners, but my friend's mom really did add laxatives to his food starting a week before exams. and in the toilet, like, totally casually, there's the economics textbook lying there ) when he figured it out, he roared something fierce.
yyy: but did he pass?
xxx: he passed, what else could he do? the air freshener had long been read cover to cover )""",
412122: """ххх: what's the point arguing with you, you're on a computer and I'm on a phone""",
412123: """Оксана: Cheese is actually not a bad gift. For my school graduation my dad gave me a lawnmower.""",
412124: """today they asked me to teach the neighbor's son to more or less use a computer. the kid finished eighth grade. roughly this dialogue with his mother followed.
я: so he's not interested in it?
м: not at all. either at practice or out and about.
я: well that's actually great.
м: he already brings home some girls. I found fake nails.
я: well good for him, let him. why does he need a computer?
м: no. better he sits at home at the computer. always in sight""",
412125: """Habrahabr, "What do you name your servers?"

VitaZheltyakov:
Personally, I name them by the first letters of the project name. For example, kb1, rb1.

There was a practice of naming projects after girls — a bad one. Once the management overheard me talking to the admin and caught «Irina is lying down, waiting for you». Spent half a day explaining who «Irina» is and why she's «lying». Then we had to rename all the servers' names.""",
412126: """xxx: Continuing to methodically make the rounds of doctors. The last one prescribed me stockings, heels and a man...""",
412127: """ххх: never buy a girl an epilator! even if she hints at it herself or asks for one for her birthday, you'll still end up the asshole who wants to hurt her.""",
412128: """ххх: had an exam today. five people left, the prof all tired and angry. this girl goes up to answer, answers, stumbles on a question, and clearly the guy snapped — he gave a 20-minute speech, cursed us all out ten stories high, that we're talentless and nothing decent will come of us, and we're complete idiots, and blah-blah-blah
ххх: the girl sits there, about to cry, fine, they gave her a C, she walks out... and then from the hallway comes this juicy sound of a smack
ххх: the prof looked at us and said, surprised: "Looks like she got upset\"""",
412129: """xxx: go wash the dishes!
ууу: can't, I have my thesis...
ххх: you defended it today!!!
ууу: well it was worth a try...""",
412130: """Ирчег: On Sunday at 7:05 AM some asshole blasted Rammstein. Sleeping through that screaming is totally impossible! Listened to 3 songs. Decided to go check — the neighbors, or from the street. I come into the kitchen, and it's blaring from MY stereo. The cat was sleeping on top and turned it on by accident ))))

Managed to turn it off, the neighbors never came.""",
412131: """Silkysmooth: you wouldn't happen to have any women's mascara at home?
Trinadsat: Sorry, finished the last of it.
Trinadsat: Though maybe there's some left in the freezer, come over and look.
Trinadsat: Bring wine. A good Chianti.
Silkysmooth: mascara. MASCARA. Women's. idiot!
Silkysmooth: God, who did I get mixed up with...""",
412132: """Оленька
13.03.2010 09:17:48
Hello!
I bought a new laptop. It smelled like plastic and I decided to wash it with raspberry soap, and it stopped turning on. And I thought it was because of the cooling system and because there was water in it, so I vacuumed it with a vacuum cleaner and dried it with a hair dryer. The vacuum cleaner ripped out the Й, А, П, Ю keys. I glued them back on, but for some reason they don't press. Then I took it back to the store and told them to give me my money back, and they told me the product was void of warranty due to physical damage and is not subject to return or exchange.
Tell me, why do they say that, I didn't even take it apart!""",
412133: """Schwarze Seele: I really like this "Business lunch" thing. What kind of business must you have if that's your lunch?""",
412134: """xxx: I'm the president of Earth, muahaha!!!
yyy: wonderful — and how many acres of it?""",
412135: """xxx: "our little system unit won't turn on". fuck. they brought it in.
xxx: plugged it in. bang. zap. shower of sparks. another bang. coffee on the floor. room full of smoke.
xxx: their little system unit won't turn on, fuck me
xxx: power supply set to 115 volts. fuck.
xxx: my hands are still shaking.""",
412136: """@netneladno: Does anyone else associate the sound of a car alarm with car theft at all?""",
412137: """Standing in line at a market stall. In front of me a guy of about 18-20. When his turn comes he asks the saleswoman:
- Do you have cold "Jaguar"?
The saleswoman rummages in the fridge for a long time, then, saying there's no cold "Jaguar", takes out a can of "Red Devil" and offers it. The guy:
- No, don't need "Red Devil"... THAT'S CHEMICALS.""",
412138: """ххх: don't know what I want tonight — either company and sex, or solitude and music.
ууу: Why don't you have sex to music... )
zzz: ... alone.""",
412139: """yyy: she's waaaay past 40
yyy: her grandkids are older than my cat!""",
412140: """mrvladik: You can't even imagine how much ice cream, mayonnaise and sauerkraut I had to eat to sort all the screws, nails and self-tappers in the workshop...""",
412141: """Vyatich: Masturbation kills billions of potential taxpayers daily, %username%.

IMA: We need to introduce a masturbation tax!""",
412142: """она: Break into my mailbox!
я: Did you forget your password again?
она: God, you people are annoying, I lost the KEY to my mailbox, the KEEEY!""",
412143: """xxx: I love my wife's WoW account password so much
xxx: 23 characters, 5 digits, four capital letters, the phrase makes almost no sense, you'd never fucking remember it
xxx: and at the same time our router password is fucking 12345
xxx: because she keeps forgetting it""",
412144: """xxx> where did these scratches on the modem come from?
ууу> the bottle opener disappeared somewhere yesterday...""",
412145: """xxx: Have you seen how he writes code? If he needs to create a User class, he'll start with a Human class, and provide for the possibility of both carbon-based and silicon-based life forms.""",
412146: """xxx: Bought a tablecloth, turned out to be a Chinese "self-tearing tablecloth" (((( """,
412147: """xxx: Was walking with my girlfriend today, she was telling me her classmate got married, to a 67-year-old man...
yyy: Rich?
xxx: Don't know )
xxx: Maybe just love
yyy: For what?
yyy: For retro?!""",
412148: """xxx: We got a laptop with drivers at the warehouse.
xxx: In the branded packaging, two planks instead of the laptop.""",
412149: """Walking home from the lake with my kid just now. Saw a cat.
The kid says: rita's (the neighbor's) gray cat is about to give birth.
Я: Why?
Р: I saw other cats helping her give birth.
Я: How did they help?
Р: They climbed on her, and like this (shows with her hands) rubbed her back, so she'd give birth faster.
Я: What other cats?
Р: Our Vaska""",
412150: """Алена: studying, working, hanging out, living ) And you?
Илья: if it's not a secret, where do you work and study?
Алена: Studying at the Siberian Law Institute of the FSIN, working at the military enlistment office )
Илья: oh, bad timing on my part (""",
412151: """<=Max=>:
I remember at uni (year 94! ) they taught us about the 3 main world principles of cosmetics advertising.

1. Take any actor (or a bum, or a granny), dress them in a lab coat and call them a "Cosmetics Expert" of the Cosmetology Institute of the Russian Academy of Sciences or a dermatologist of the Government Hair Loss Center or any other (can be a totally nonexistent but loudly-named research institute). This "experto" must tell about their "comprehensive" positive studies of the product.
2. There must be a drawn effect of +55 to +237% improvement over 2-3 weeks (the period must be such that the buyer manages to buy a couple-three bottles of the product, but not too long, so the people buy it)
3. There must be a super-miraculous component (even if it doesn't exist in nature) — ceramide R, bifidobacterium essensis, supermineral Pro-Z, etc. :))

And that's it. The people will swallow it. To this day I keep seeing nothing has changed.""",
412152: """xxx: so what time should I expect you over tomorrow?
yyy: I'll be free at 7:10, if the boss doesn't talk me into staying )
xxx: why would he talk you into staying? excuses not accepted, unless the boss is a leggy blonde ;)
yyy: the boss is a broad jock, hard to say no to him""",
412153: """XXX: today I'm standing on the balcony smoking, watching a pigeon fly to the neighbors' balcony carrying a dragonfly in its beak.
XXX: obviously to feed the chicks. 3 seconds later it flies back out with an empty beak... but after 15 meters it makes a big circle and comes back...
XXX: Probably forgot to turn off the iron :D""",
412154: """ДД> want to rent an apartment with a young man. would really like to avoid agencies. price negotiable. with furniture and appliances
Н> is the young man yours or ours?""",
412155: """xxx: Mom, which phones do you like more — clamshells or candy bars?
yyy: I like "transformers"...
xxx: What's that?
yyy: Well, they go like this (waves hands in the air) first they fold in half, and then the sides lift up too...
xxx: Where have you seen those?
yyy: They're sold everywhere. 11 thousand.
xxx: Phones like that don't exist!
yyy: I'm talking about a couch... I don't need a phone.""",
412156: """Цыферка: when I come home late I always try to exclaim something like "oh that new haircut suits you so much". It instantly defuses the situation
Серожа: Basically right. Even if you crawled in on all fours and can't lift your head, a phrase like that will still make sense
Лео: You can look at her feet and praise the pedicure
Серожа: and if you have no strength to talk — lick and whimper""",
412157: """(from a chat with a colleague who jumped off a "bungee")

xxx: So, what, the wings didn't open behind your back? :)
yyy: No, they didn't open. But the jet engine tried to kick in :(""",
412158: """Handur: There was also a sad boy at the festival with swim trunks over his pants. Probably Superman.""",
412159: """I work in support for a local provider.
A girl calls: "Oh, my internet isn't working!"
Me: "Is your internet via modem or fiber optic?"
Girl: "Yes, yes, through the window, we have wi-fi\"""",
412160: """Oksana_Konfetka: Artyom, come home, your parents are worried. about "the last one is the daddy" — Yulia was joking XD""",
412161: """xxx: I know how to enlarge your dick by 10 centimeters, and not from hearsay
xxx: I work at an apiary""",
412162: """ххх: the cat jumped over me and misjudged, landed its paws on the keyboard — a variable vza got added to the code )))
ууу: in Cyrillic that's МЯФ""",
412163: """xxx: they're married
xxx: well, judging by the statuses — he's married, and she has "it's complicated\"""",
412164: """Antoine Blanco: Apple wants to introduce a new SIM card format
Massimo Maretti: apple-shaped?
Massimo Maretti: to activate it you have to take a bite""",
412165: """Айвенго: there are so many wires strung from our nine-story building that I'm already afraid it turns out it's held up by them alone...""",
412166: """xxx: "To prevent fires and fight them, 25 icons of the 'Unburnt Bush' have been sent to Krasnodar Krai."
xxx: There's your modernization, there's your Skolkovo, and there's your nanotechnology.""",
412167: """Жора: I'd like to live to old age if only to see how they'll live and what they'll look like — the old ladies who now, in the guise of glamorous chicks, splay their fingers behind the wheel of Lexuses and divide people into two categories — those who owe them something, and those who are shit.""",
412168: """From a women's forum:

Details are good. Once I visualized my future seatmate on a transatlantic flight — no joke, 10 hours of flying. I wished for someone young and athletic. I ended up flying next to a 13-year-old boy, a member of the Russian children's hockey team. So next time I went into detail: tall, buff, tanned, attentive, handsome, fun, younger than me but long since of age, cheerful. In the end, sitting next to me was a two-meter-tall buff Black guy of about twenty-five, who watched a movie about hamsters the whole way and laughed his ass off. Oh, and attentive, yes — he treated me to chips and offered one earbud from the hamsters.""",
412169: """xxx: I trained my dog to give its paw on the command "manicure!\"""",
412170: """xxx: Cinderella syndrome is sudden, cruel and merciless: got up at night for a drink of water, wanted to wipe a spot off the table and washed the whole kitchen...(""",
412171: """Ipkis: How? Tell me, how could someone translate the song title The Fluorescent - A shot in the dark as "Ашот в темноте"?""",
412172: """xxx: Be there soon, already at the monument
yyy: Which one?
xxx: Past the bridge
yyy: Sweetie
yyy: Darling
yyy: Sunshine
yyy: Kitten
yyy: Damn
yyy: goddammit
yyy: THIS IS PITER!
yyy: THERE ARE BRIDGES EVERYWHERE HERE
yyy: And monuments...""",
412173: """Kizhlo: the day before yesterday I met a funny couple on two mopeds
Kizhlo: on one rode a girl with a cast on her leg and crutches under her arm
Kizhlo: and a bit behind rode a guy with a cast on his arm, in pajamas and with a bandaged head
Kizhlo: "now that's a passion for motorsport, I thought\"""",
412174: """xxx: the kitty's sitting nearby, scratching, fur flying everywhere, sneezing, scratching again and sneezing again
yyy: he probably has an allergy to cat fur""",
412175: """The other day I was visiting my parents. Watched a show with my dad about how they dug the tunnel under the English Channel — dug from the mainland and from the island and met inch to inch...
Went out to the stairwell for a smoke, noticed the cable run through holes in the stair landings. The approximate mismatch between the lower and upper holes — 20-25 cm.
Remembered the show, got thoughtful...""",
412176: """a call to support:
Employee: "does your computer even see the flash drive?"
Client: "of course it sees it, here it is, I'm holding it in my hands\"""",
412177: """a phone conversation with a girlfriend:

she: darling, you used to say you wanted me to come over sooner, said every 5 minutes that you missed me, and lately you don't say it... Is everything okay?
he: no...
she: I knew it, just tell me honestly — is it Yana!?
he: my throat just hurts really bad...""",
412178: """ххх: My mom's back was hurting, I left her my laptop for fun, told her to lie there and watch movies. I come by a week later, and she's lying on it — guess why?
ххх: Warming up her back :)""",
412179: """ххх: How do you get a girl into bed?
yyy: girls don't need getting, they're not rabbits, you need to look after them )))
zzz: AND YOU THINK RABBITS DON'T NEED LOOKING AFTER?""",
412180: """ххх: Played Heroes V all day today. Turn on the TV in the evening, football's starting, the teams are standing on the field, the anthem's playing.
ххх: First thought — why isn't the battle grid showing? Second — how to position them better?""",
412181: """A comment on the news: "Patriarch Kirill: The Church should use information technology in dialogue with youth":

xxx: the church needs more followers!
xxx: like Orthodoxy!
xxx: lit a candle — tweet about it!""",
}

def main():
    src = open('chunks/chunk_30.jsonl', encoding='utf-8').read().splitlines()
    batch = [json.loads(l) for l in src[0:100]]
    assert len(batch) == 100, len(batch)
    ids = [r['id'] for r in batch]
    assert set(ids) == set(EN), (
        'missing:', sorted(set(ids) - set(EN)),
        'extra:', sorted(set(EN) - set(ids)),
    )
    out = open('out/chunk_30.jsonl', 'a', encoding='utf-8')
    n = 0
    for r in batch:
        en = EN[r['id']]
        assert en, r['id']
        assert en.count('\n') == r['text'].count('\n'), (
            'newline mismatch', r['id'],
            en.count('\n'), r['text'].count('\n'))
        for tok in set(re.findall(r'<[^<>\n]*>', r['text'])):
            assert tok in en, ('nick token lost', r['id'], repr(tok))
        out.write(json.dumps(
            {'id': r['id'], 'ru': r['text'], 'en': en},
            ensure_ascii=False) + '\n')
        n += 1
    out.close()
    print('appended', n)

if __name__ == '__main__':
    main()
