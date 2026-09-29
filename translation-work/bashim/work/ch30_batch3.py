#!/usr/bin/env python3
"""Translate bashim chunk_30 batch 3 (input lines 201-300). ru is read verbatim
from the input file; only en translations are embedded below."""
import json
import re

EN = {
412282: """From Habr. A topic about Google+ development costing $585 million USD.

xxx: Freelancers would've done it for $800 :)
yyy: And imagine what else you could get if you added 300 bucks to your sum? :)
zzz: They'd write Windows 8 from scratch.""",
412283: """Bought all of Celentano's albums on eight discs. Not all the songs turned out good. Decided to burn a compilation of the best ones. Spent eight hours listening, selecting, sorting.
The last disc turned out to be the best of Celentano. The selection matched about 80 percent.""",
412284: """xxx: basically, I'm a visual learner
xxx: in one ear and out the other with me
xxx: well, there's a skull at the back, so what goes in through the eyes doesn't come back out""",
412285: """xxx: Found grandpa's primus stove at the dacha, will give it to a black cat cheap.""",
412286: """ххх: It finally dawned on me why the touchscreen Nokia comes with a stylus it doesn't really need
ууу: And why? )
ххх: Turns out it's vitally necessary for fishing strawberries out of the jar with it and not poking the screen with sticky fingers""",
412287: """кирхен: I want to sleep. why is all this crap crawling into my head? 8(
Doom-e-r: it wants to sleep too and is asking to go home )""",
412288: """Мёртвый Душ: Either allow the free sale of weapons, or ban the free sale of karaoke.""",
412289: """She: Who were you just talking to about eight centimeters and lube?
He: Silly! A friend asked me to fix the cooler on his computer.""",
412290: """Kei: Noon. The power's out, I'm bored. A neighbor comes, asks me to look at his computer — says it won't turn on. I say I'll look when the power comes back on. He apologized and left.""",
412291: """xxx: why does he keep asking how things are on my personal front?
yyy: he wants you on the front line""",
412292: """From LiveJournal:
When you put a condom "just in case" in your purse — I strongly advise against keeping it in the same pocket where a spool of thread with a needle has long been lying just in case. That's a really bad idea.""",
412293: """xxx: Our office is under the table ))
xxx: A dude is returning parts and there are several fields about the reason for return.
xxx: Opposite the "Defect" field he wrote "Not married\"""",
412294: """ххх: and if a black jaguar crossed your path in the forest, is that bad luck too?
ууу: yes! that's at least soiled pants""",
412295: """xxx> What a bastard you are!
xxx> I bought you a chocolate bar and nearly broke my teeth on it!""",
412296: """A friend told me.
Wakes up in the morning to furious ringing at the door (8:00 AM).
Slowly trudges to the door, mentally going through all possible ways to kill the ringer.
Friend: Who's there?
From behind the door: Plumber, came to check the riser.
Friend, automatically lowering his eyes: Everything's fine.
And goes back to sleep.""",
412297: """Echuy: It was a genius idea to attach the subtitles from the latest X-Men to the porn!""",
412298: """3яка: The more I sleep, the less harm I do""",
412299: """xxx: I used to think about taking up paintball, then decided it's expensive
xxx: then airsoft but came to the same conclusion
xxx: now I've taken up photography and the spending on airsoft and paintball seems quite small""",
412300: """ххх: so we wouldn't wash in their machine, the neighbors took the detergent drawer out of it.
ххх: we were very upset — had to pour the powder straight into the drum""",
412301: """xxx: Felt like a real woman when I sat in ICQ for 4 hours, waiting for Him to come online and write "Hi", so I could tell him I'm not talking to him.""",
412302: """ххх: I don't like other people's kids
ууу: you know, me neither... all those eggs, larvae, embryos. And they try to crawl out through the ribcage.""",
412303: """ххх: Good for South Sudan, but now schools have to change all their maps =(""",
412304: """xxx: The doctors at our clinic have only two types of reaction: well what did you come here for and where were you before?""",
412305: """xxx: I run into a former department employee in the institute hallway, an old lady of about 70. Chatted about this and that. At the end of the conversation she says to me:
- How is Professor Orlov? Give him my regards.
- You know, Professor Orlov has been dead for about four years...
- You don't say?!! What a pity, he was such a wonderful person... A pity, a real pity... Well, give him my regards anyway...""",
412306: """ххх: the phone lives its own life. Found itself some update, downloaded it itself, installed it, rebooted and lies there with an innocent look, like nothing happened...
ххх: kind of creepy when some thingy is lying next to you doing stuff for itself =\\
ууу: soon it'll decide it doesn't need you.""",
412307: """Dad overfried the cutlets.
Me: See, I told you — needed more oil! You can tell right away, a person who never lived in a dorm.
Dad: Says you, like you never lived there. They always eat overfried ones there.
Me: Well yeah, the first two times, and then it turns out fine.
Dad: And then uni is already over""",
412308: """xxx: What do you know about backup?! I have two rolls of paper hanging in my bathroom.""",
412309: """tech support: Are you working in Internet Explorer?
client: yes
tech support: what's your Internet Explorer called?
client (reading): Mozilla Firefox...""",
412310: """tarasenko: What bad thing could I do?
РыЖиЙ <ёЖж_: Go to the pharmacy.
РыЖиЙ <ёЖж_: Buy some laxative, feed it to the pigeons. Everyone will feel bad.""",
412311: """Ирина: Hi. I can't log into the store program, it says the password is case-sensitive
Дмитрий: hi. Maybe caps lock is on?
Ирина: and how do I turn it off, I was on vacation for 2 weeks, I don't know what was turned on or off""",
412312: """xxx: more precisely, the launcher isn't registered
yyy: is it supposed to be like that or did something break? or was it left unfinished?
xxx: the programmer wasn't paid enough
xxx: that's a common error""",
412313: """111: Dim, a client is asking what type the "Parameter" parameter is?
222: Apparently of type "type"...""",
412314: """xxx: bought a double cheeseburger for 75. While chewing — studied the prices. So, I could've bought a regular cheeseburger for 40 and a hamburger for 30, thrown out half a bun from each and combined them: would've gotten a double cheeseburger for 70 (plus a bonus extra pickle).
yyy: What you're proposing here, in Microsoft's language, is called a license agreement violation.""",
412315: """from Habr

ncix: I was catching Yota at the dacha with a big enameled basin.
That's how my mother-in-law caught me: at night by the window in the glow of the laptop screen with a basin and a modem in my hands.
I explained I was catching the internet. She didn't seem to believe me.""",
412316: """xxx: my daughter (4 years old) came up to dad (my husband, a sysadmin), said she needs a jump rope. he, without thinking twice, took out a couple of his parts, worked on them a bit and on the pipe left over from the plumbing repair, stuck board stubs into the tubes, wrapped it all in electrical tape, said these are the handles, now he's sitting there crimping cables of different lengths, says the jump rope will be for the daughter to grow into. now I'm sitting here afraid he'll start soldering a jump counter.""",
412317: """<xxx> Kolyan is a really fucking awesome specialist! Only he noticed that the chick with the angle grinder in the Satisfaction video is grinding with a cutting disc...""",
412318: """xxx: you're an ass with handles, not a programmer!
yyy: no need for dirt! I have legs too!""",
412319: """ххх: My husband went to the tailor to have his pants altered. There's also a jacket, hanging at home — that's the suit from the wedding. The lady there asks him: -Why did you buy such a big size? He: -Well that was 5 years ago. She: -Wow. And what kind of diet is that? He: -I just got married.""",
412320: """From the discussion of Google+ and Gmail integration on Habr:

netto: I love borscht and strawberries with cream. But I prefer not to integrate them in one bowl.""",
412321: """Настя: Igor, do you have a hobby?
Игорь: no. I have two kids :)""",
412322: """There's a discussion of music for sports
xxx: classical — helps you concentrate, calms you down.
yyy: Just the thing — relax to Vivaldi under the barbell""",
412323: """I work at an auto parts store.
A call on Sunday evening.

he: - Hello, do you have a small rear window for a 21099?
me: - Which one do you need, right or left?
he: - what do you have? We just can't decide which one to smash.""",
412324: """ХХХ: how did she even survive school?
YYY: she was a D-student and a bad girl. The class teacher kept egging us on to boycott her for re-education.
YYY: and she, in turn, egged us on to skip the class teacher's lessons
YYY: that's when I learned for good that no guild wars in any MMoRPG even come close to real life =))""",
412325: """XXX: My hair is so unruly that even the "fell asleep with a wet head" hairstyle only holds for half a day.""",
412326: """XXX: I want competent, quality, long shopping.
Until my arms and legs go numb. Until my eyes turn red. Until deadly fatigue. Until exhaustion.
To come home, take off my shoes, pour cold juice, take a sip and look at this mountain of bags and smile blissfully...

YYY: I've had that. competent feeling! 40 sheets of drywall, 30 bags of cement, drill bits, self-tappers... so much stuff!""",
412327: """Юля: our tastes match so much that he likes my friend too:(""",
412328: """xxx: I don't want to install a sub — since I only listen to RukiVVerh
yyy: With such preferences I'd sincerely recommend not installing any speakers at all.
Headphones exclusively.""",
412329: """xxx: We talked to her ... in cold tones.
xxx: So cold that I'm starting to think the fridge is a heating appliance.""",
412330: """Лучик: If I were sober, I wouldn't get behind the wheel drunk!""",
412331: """xxx: if she's the same in real life as on the internets, and turns out to be a cutie too, and if I really did catch her fancy
xxx: I'm scared
yyy: Uhhh
yyy: What's there to be scared of?
xxx: I'M SCARED I'LL GET MARRIED!""",
412332: """Солнышко: You bet! My intellect is growing before your eyes!
Me: My dear, that's a stye.""",
412333: """From Habr:
Evengard: Russia is itself a third-world country.
Rezzet: You've been fooled, a third-world country can't be a monopolist in launching human and cargo satellites into space, and Russia currently is one.
pnick: And not only into space! Russia is the only country with an underwater constellation of global positioning satellites!""",
412334: """JamiK: damn 21st century:(
STUbbS: what now?
JamiK: a girl's coming over soon, and the movie downloaded in 57 seconds...
JamiK: guess we'll have to watch it :-((((""",
412335: """xxx: I had this couple I knew, they'd been dating for several years
xxx: she was already set on a wedding, and he wasn't exactly enthusiastic about it
xxx: so they're sitting with friends once, and the conversation turns to how everyone's life is going
xxx: she says to him with a hint, like "All my girlfriends are already married!"
xxx: and he looked at her intently and goes:
- And you, silly, why aren't you married?""",
412336: """rfund:
A true old Petersburg intellectual will never say: "Fuck your mother!", he'll say: "Why, I'm old enough to be your father!

loy_yver:
Oh he will. And I've long noticed that the more genuine and Petersburger, the more refined the command of profanity. My total mind-blown moment was about fifteen years ago, when in a tram a little old lady of the most intellectual appearance, a "dandelion", with lilac hair, in a hat with a veil, gloves and a reticule, at the conductor's announcement: "The tram will turn right, they're laying asphalt straight ahead!" in a voice smoked — by "Belomor", no doubt — with such a bass boomed across the whole tram No. 14: "Fuck your mother, it's not "they lays", it's "they lay"!!!" :-D""",
412337: """xxx: So imagine, you're a 17-year-old tender boy, working as a courier in the summer, carrying a volume of de Sade, and you're greeted by a dude with a bare torso, in army pants, and German industrial is coming from the room, something about black uniform...
xxx: Would you have stayed for some tea?""",
412338: """xxx: Came to submit my admission documents.
xxx: Took the form, sat down, filling it out. Then a girl pokes me and asks: "What's my GPA on my certificate if it's all A's?\"""",
412339: """xxx: I'm arriving at 09:53
yyy: you said 9:15, something with the train?
xxx: yeah, I looked at the ticket more carefully))""",
412340: """xxx: Mobile operators, along with the "subscriber is unavailable, please try calling later" message, should also introduce "The subscriber is on vacation and DON'T FUCKING CALL HERE!\"""",
412341: """vera: yesterday Vasya, you know, the ugly one, invited me on a date...
динь-динь: and why did you refuse?
vera: what makes you think I refused?
динь-динь: you agreed???!!!
vera: no, I refused)))""",
412342: """xxx: Lying here now with the window open, outside some dude is arguing with someone on the phone
xxx: "Are you gonna make up with her? For what? To fuck her? Anton, she's MY wife!!!!!!!\"""",
412343: """xxx: Does anyone have a set of drawn little men, all the same but in different poses/actions?
yyy: Arthur Conan Doyle has""",
412344: """Неогорчаемый: wishing you happiness
Юличка: thanks, and big big to you too )
Неогорчаемый: thanks, but I'm happy with my size""",
412345: """If you have a small flat neodymium magnet with a 100-kilogram pull force, you shouldn't hang it in the very center of a steel safe.""",
412346: """xxx: Call me a shrimp one more time and I'll smack you in the face
xxx: with my tail""",
412347: """At our hospital:
- Who's last in line to the neurologist out of turn?""",
412348: """Yesterday I was with my mistress. Suddenly her husband came home two hours early. Naturally, he caught us, as they say, "at the most interesting moment". He walked into the room, looked at her, then at me, was silent for 5 seconds... Then he burst out laughing, took a bottle of beer from the fridge in the kitchen and left! Now I'm gonna get complexes because of him. He should've just punched my face in, FUCK!!!""",
412349: """xxx: I don't even know what to offer you anymore
xxx: Whatever I offer, you just keep agreeing and agreeing...""",
412350: """xxx: These idiots played all night a game of who finds the longest "tree" in the registry...""",
412351: """xxx: A slightly masochistic riddle: a pear is hanging — can't eat it. An apple is lying — can't eat it. A little sandwich is prepared — can't either.
ххх: That's toothache (""",
412352: """xxx: Why is our server down?
yyy: Power problems in the data center.
xxx: They're not feeding the admin?""",
412353: """Анигиллятор: I got into a brawl today
Анигиллятор: Walking home, I see a dude run out from around the corner, a second one running after him yelling at the top of his lungs "You're fucked!!! She's mine!!!", then a third dude runs out yelling "You're both fucked!!! She's mine!!!", then from around the corner comes a girlie (really pretty, though) and watches them with a surprised look. Decided, while they're running around, to go meet her. My jaw's broken now, but I also landed a couple good ones on someone.
Dron™: And who did the chick go with?
Анигиллятор: She didn't go with anyone, she ran off yelling: "What, are there only guys in this town or what?!!". But she really was pretty. Very.
Dron™: Well, I already figured that.""",
412354: """Ксюнечка: Dear friends! Does anyone have any props for a pirate party? Heeelp!!!!
Игорь: mp3 discs, camrip movies, cracked software""",
412355: """Was in Irkutsk the other day. Walk into a store, thinking of getting cheese. See Adyghe cheese for sale, well, I think, they brought it from far away (I'm from Rostov myself) and I ask — "Fresh?" the saleswoman nodded convincingly.

And only in the morning, reading the label, I learned that it was tomorrow's.
It was too late(""",
412356: """xxx: just saw a werecat in the supermarket
xxx: in the guise of a woman, at around one in the morning, it was buying like twenty packs of cat food
xxx: on a full moon..""",
412357: """M@G: turned out a cool resume
M@G: I should add an item "have a level 74 gnome on official"...
M@G: although no, even a child knows the max level is 85
M@G: they'll think I don't finish what I start""",
412358: """xxx: To all people: If you see an open sewer manhole — don't rush to close it with the lid lying nearby!
xxx: To utility workers personally: You need to put the helmet on your head and mark the work area with flags!""",
412359: """xxx: Bought Doshirak at a kiosk with a platinum credit card. There's something to that... Something strange... :)""",
412360: """xxx: hung a pull-up bar on the balcony
xxx: decided that every time I go out for a smoke, I'll do pull-ups to exhaustion
xxx: I didn't think I smoke THAT much""",
412361: """xxx: So I'm sitting there, playing the guitar...
xxx: Dad comes up and says: "gimme, I'll show you how we played back then".
xxx: Takes the guitar and breaks a string with the very first strum.
xxx: Pause...
xxx: Dad, without missing a beat, goes: "yeah, that's how we played" and hands the guitar back... ='(""",
412362: """Sitting in the office, testing software.
I ask a colleague to throw me the latest update, hear "catch" in reply, I'm loading my email and then... whack, a flash drive to the head.""",
412363: """Dmitriy_Fox: There's a proposal to build a new MOSCOW-RUBBER train station near Podolsk""",
412364: """xxx: My life belongs to the Horde!!!!
ххх: and my wallet to Blizzard (""",
412365: """ххх: hey, do you know what "пати" is in English?""",
412366: """xxx: my wife sympathized with me today, I drove a taxi for 2 days.
xxx: she says, don't work today, rest, let's lay the linoleum instead""",
412367: """Worked as a security guard at a car dealership once. After some holiday there was a fuckload of red caviar left at home, which relatives from the Far East had brought. Took a jar to work. Sitting there, staring at the monitor with the surveillance cameras, in one hand a huge cup of coffee, in the other a huge sandwich with a centimeter-thick layer of caviar. The director walks in, sees this picture, freezes for about 10 seconds, then blurts out:
- Holy shit, our security eats well!
Freezes for another 10 seconds and then quieter, aside:
- Maybe I should change jobs...""",
412368: """Козька: All cats are cats, only my redhead dummy is afraid of the scratching post. She gets her claws stuck in it and meows pitifully >_<""",
412369: """PP: Fighting with my husband, yelling, waving my arms. And meanwhile I'm thinking: I should smash the old mobile against the wall, like in anger, so he buys me a new one later""",
412370: """Sofanya: say I want to go out with a green purse, what should I wear with it?
Airee: black, of course!
Airee: you'll look like a little console! :)""",
412371: """Азаззель: WOW! Dad's bringing me money for three months!!!
Азаззель: Ninety thousand!!!
Азаззель: Aaaaa!!!
Морр: A-a-and?...
Азаззель: I'll buy a mop.
Морр: What simple, feminine happiness...""",
412372: """At work:
- Our blinds broke here, the slat won't come out.
- Pull the string harder, maybe it'll fix itself somehow.
- Already pulled, now it definitely won't fix itself.""",
412373: """xxx: I had no idea I knew computers so well until I sat my parents down at one""",
412374: """Heck–Aitomix: I have two rats living with me. Both only respond to the name "Lunch!\"""",
412375: """XXX: The more kids in the camp, the bigger the mess.
УУУ: The ideal option is when there's just one kid.
ХХХ: And even that one's lying tied to the bed :)""",
412376: """maikl_never: I remember, I was 23, and I was already serving in the police. And I was buying cognac at the store. And the saleswoman suddenly asks: "Isn't it a bit early?".
I, like an idiot, immediately looked at my watch.""",
412377: """News: Russia is releasing its first 3D porno-drama!
Comment: What kind of genre is porno-drama, does everyone come badly in it?""",
412378: """ххх: Small change spilled out of my bag onto my bed. Was very tired after the day, fell asleep. In the morning I wake up, walk around the apartment, and coins are pouring out of my underwear. The Golden Antelope, fuck...""",
412379: """ххх: It's just my stupid habit, developed in the army, of waking up 5 minutes before reveille. I can't explain it, but if I need to get up at 6:00 — my eyes open at 5:55. If I can lounge till 12:00, my eyes open at 11:55.
yyy: and I somehow developed a habit of waking up 65 minutes before the alarm.
zzz: and I possess the incredible ability to wake up forty minutes after the alarm""",
412380: """xxx: "An unknown hooligan pelted American singer Lady Gaga with eggs"
xxx: And there's the new stage dress ready!""",
412381: """Two spam emails in a row. Both signed "Raisa". The first one's subject is "Air conditioner sale", the second's — "I'll become your mistress".
Looks like Raisa's sales aren't going well.""",
}

def main():
    src = open('chunks/chunk_30.jsonl', encoding='utf-8').read().splitlines()
    batch = [json.loads(l) for l in src[200:300]]
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
