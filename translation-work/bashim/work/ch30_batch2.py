#!/usr/bin/env python3
"""Translate bashim chunk_30 batch 2 (input lines 101-200). ru is read verbatim
from the input file; only en translations are embedded below."""
import json
import re

EN = {
412182: """xxx: I dream that Google would release a new service - Google Water
xxx: You enter three paragraphs from your thesis intro, Google spits out three and a half pages of coherent text.""",
412183: """xxx: new neighbors. haven't seen them yet, but I think they look like the bunnies from the duracell ad =)""",
412184: """xxx: A couple years ago we brought grandma a scratching post for her kitties (she's got like 10 of them), so they'd sharpen their claws on it instead of the couch. Today I accidentally discover the scratching post in the closet. I ask - "Why didn't you set it up for the cats?". She answers: "Why? So they scratch it to pieces?!\"""",
412185: """xxx: walk into my little brother's room today, and there's a big manly leg sticking out from under the blanket
xxx: then it kind of dawns on me. how fast little brothers grow up... )""",
412186: """she: we should hold off on sex for a while...
he: why? How long?
she: until my period comes...
she: in about 9 months...""",
412187: """kontrass: today at the sea I heard a wonderful phrase addressed to me, the best example in my memory of both a compliment and an insult at the same time. And personally I never figured out which it was

kontrass: it went like this: "You swim like shit\"""",
412188: """A friend drove pretty far in winter. The windshield washer fluid tank had plain water in it, which froze. It was decided to thin it with vodka.
So, two boys with deadpan faces walk into a store and ask for a bottle and a pack of cigarettes.
Saleswoman: you're sturdy lads, of course, but let me at least cut you some bread?""",
412189: """xxx: so why did you break up? he seemed like a normal guy
yyy: well, I started suspecting something when his previous girlfriend started calling me and insistently begging me to take him away...""",
412190: """xxx: I build incredible pyramids out of trash, just to avoid taking it out.
xxx: Besides, if someone else dares to throw something else away, it'll definitely all collapse on them, and that'll be their problem. And me? I did nothing""",
412191: """She: my neighbors would do better to sleep! sounds like a young couple behind my wall )
He: who's beating whom right now? )
She: let's put it this way, she has a very emotional man )
He: and her?
She: I sometimes doubt she's even there )""",
412192: """~~$юльчик$~~: PEOPLE TELL ME WHERE CAN I BUY HOOKAH TOBACCO????
~AnomaliyA~: yulechka...
~AnomaliyA~: sneakers on your feet...
~AnomaliyA~: butt in shorts and off for a run.
~AnomaliyA~: what the hell tobacco? )))
~~$юльчик$~~: FOR A HOOKAH""",
412193: """Вадим: yeah, sitting here thinking where to go on vacation — could fly to Turkey now and not sweat it
Вадим: or save up some more money and next year go to Sochi like a rich kid...""",
412194: """xxx: Back around year 3 I worked at MTS and was 4 minutes late (they stopped the Gazelle van, were writing the driver a ticket).
xxx: They asked me to write an explanatory note and gave me a sample that said the following:
"I was 2 minutes late to work because I left home 2 minutes later".""",
412195: """el_Archangel: Judging by the documents lying on our table, our company's corporate symbols are a semicircular stain from a coffee mug, a size-45 boot print and a dead mosquito...""",
412196: """xxx: You know what I think about on a warm summer night, when the pouring rain rustles in the leaves outside the window?
xxx: That maybe at least after the rain my hood will be cleaned of bird shit""",
412197: """kuchamusora: At our dacha a snail ate all the cabbage. Dad tracked it down and threw it over to the neighbor. To my question whether it would crawl back, he answered: "It won't crawl back, it hit its head".""",
412198: """ххх: how much time the internet takes away from humanity! if we deleted all entertainment portals and left only Wikipedia — that would be great!
ууу: the crowd of idiots from the deleted sites would start editing articles and Wikipedia would quickly become an entertainment portal.""",
412199: """xxx: defrosting the fridge. I hear sounds from the kitchen, think the cat's up to mischief again, go to look. the bastard climbs into the freezer, turns around clumsily in there, a chunk of ice falls on him, he bolts with a wild meow, plops into the puddle leaked from the fridge, yells even louder and takes off in an unknown direction, slipping on the laminate...
xxx: now I'll have a stuttering cat :(""",
412200: """xxx: The grannies are mocking me — they come every evening to the bench next to the dorm and feed the local cats.
xxx: Who'd feed me like that, fuck?!""",
412201: """plomBEER: My sister just asked me to buy a "phallofumigator", since the mosquitoes are driving her crazy""",
412202: """ххх: okay, what signs of syphilis did you find in yourself?
ууу: a twenty-five-liter enameled bucket grew on my back
ххх: I want to reassure you
ххх: you've had it since birth
ххх: you usually call it your head""",
412203: """Lihach: Got my diploma! New Year is a holiday again!""",
412204: """ххх: Just THREE products form your belly.. don't eat them and you'll lose weight fast!
ууу: Fats, carbs and proteins — those are the products:)""",
412205: """xxx: we're sitting at the dacha, about to have tea
ххх: I went to put the kettle on, everything as usual
ххх: I come back with it, see Svetka has tea leaves in her cup, so I pour her the boiling water first
ххх: and she gets up, with such a sad-sad look pours the tea into the bushes and leaves
ххх: everyone's cracking up, I can't figure out what's going on
ххх: - Guys, what happened?
ххх: - You're already the third one today to pour boiling water into her kvass""",
412206: """Спирт: damn, the L and R letters wore off my headphones, now I don't know which goes where.
fozzy: it's fine, the main thing is to stick them in different ears.""",
412207: """see ya: Sometimes I pretend to be a Tamagotchi so someone feeds me""",
412208: """flanker_87
Didn't you love the "find the differences" game as kids?

Zlojsmile
I didn't. At our school there were two twin brothers, I wanted to punch one in the face and didn't want to punch the other, but I couldn't find the differences and got really upset""",
412209: """@Inattendue Maristry
Auntie declared that cleaning performed by me looks like a Goblin-style movie dub (""",
412210: """xxx: Hey! What are you up to?
yyy: Decided to cook something to eat
yyy: Standing in the kitchen, frying sunflower seeds""",
412211: """discussion of the Mac OS X Lion prerelease build
xxx: no, fuck that, I have a rule: no new OSes until the second or third service pack
yyy: what, childhood trauma from win 98? :)""",
412212: """xxx: From the life of our relatives. My auntie's latest husband, no stranger to a drink, always came home to sleep in any condition, walks down the street, you'd never guess he's drunk, straight as a string. But as soon as he opened the apartment door and saw his wife, he'd instantly relax and fall over, if really drunk. Once, when he was walking in such a state, the auntie had the misfortune to look out the open window — they live on the first floor — he saw her, the reflex kicked in, and he sprawled right there, under the building entrance door.""",
412213: """From the discussion of the Strauss-Kahn case.

levyi_botinok: A girl doesn't always understand herself whether she was talked into it, or it all happened so fast she didn't have time to object.""",
412214: """xxx: guess the riddle: Doesn't know literacy, but writes all its life. who is it?
yyy: a blogger :D
xxx: actually it's a pen. but yours is more accurate.""",
412215: """xxx: Hooray, I won 100 rubles for my mobile! Only sent 10 SMS!
ууу: and how much does 1 SMS cost?
ххх: Let me check
ххх: 27.5 rubles
ууу: you're a dangerous guy, I see""",
412216: """xxx: Yesterday I got drunk to a blue screen of death
ххх: The memory could not be "read", fuck""",
412217: """Avadaro: In his place I wouldn't seal the crack, I'd do graffiti on the wall — a space-time rift opened in the wall and a hellish satan is crawling out of there. The plus of this solution is that the crack will grow over time, and the graffiti will only become more realistic because of it.
Avadaro: And he'll also gain a reputation as an aesthete and a refined gothic original ;)
Avadaro: And to questions like — "what the fuck is that on your wall?" he'll answer — "that's not some fuckery — that's performance art...".""",
412218: """xxx: A note in the kitchen: not only the operating system, but I too failed to detect the mouse!""",
412219: """xxx: Love my mom. She got it into her head to fill the empty corner with a decorative fireplace-heater. And, remembering last summer, instead of the heater she wanted to stick an air conditioner in there. Called the company: "Hello, I'd like to order a fireplace, to build an air conditioner into it". =D
yyy: Mind blown. Have her also order a stove, to build a fridge into it.""",
412220: """From a women's community:
xxx: If a condom broke, should I have heard or felt something?
yyy: Like a greeting?
xxx: Well, maybe some sharp sound or a pop
zzz: ...the roar of an explosion.""",
412221: """xxx: What drivers do I need for a shashlik?
yyy: download them from the manufacturer's site""",
412222: """From a forum:
aaa: I have a friend who just bailed to Cyprus to dodge the draft. first, in his second year he was obsessed with the word startup. in his third year he hated everyone who said that word. second, he always disliked Macs. and as soon as he left, he immediately bought two of them. cool guy, achieved a lot, but his early-stage ideas were doomed. After graduation he immediately got hired at some company as a DB programmer, in half a year did fuck-all and even totally crashed the whole system. after that I didn't see him for half a year, and then he surfaced in Cyprus. what's that about?

bbb: The cement was low quality""",
412223: """<[SkY]Молчащий_Игнил> people, if the Chip 'n Dale song got stuck in my head — is that clinical already?
<Якодзуна_Возвращается> [SkY]Молчащий_Игнил: Knock it out with the song "Утки у-уу!!\"""",
412224: """xxx: I didn't understand a damn thing
xxx: It's just that asking "Whaaat??" for the sixth time felt awkward, so I said "got it" with a smart face""",
412225: """Кирилл: FUCK
Кирилл: now I'm gonna eat bread with melted cheese and condensed milk
Кирилл: wanted them separately
Кирилл: but gravity decided for me""",
412226: """xxx: The fact that you'll never fucking see an insurance payout is printed in the insurance contract in invisible font.""",
412227: """ххх: I eat whatever and don't get fat. I just have that kind of constitution!
ууу: you'll put on weight
ууу: any constitution accepts amendments x)""",
412228: """begbeder: Sanyok's a guy with no complexes at all, to his girlfriend's question whether he likes variety in sex he answered with zero emotion and a certain pensiveness: yeah, sometimes I jerk off with my left.""",
412229: """droid: Colleagues, who drank my lemonade? Put it back!
ALex: droid, I'd be afraid to consume lemonade that was first drunk and then put back.""",
412230: """First you hear faint moans through your sleep and don't understand what it is.
Then they get louder, you, still without opening your eyes, hope it's just your imagination and it'll end now.
A minute later you're already lying with your eyes open and realize it's not a dream and you didn't imagine it. The sounds get louder.
A slight chill runs down your spine. A feeling of total doom settles in your soul.
You understand that now it's definitely, the end... Yes, that's it. Your child woke up in the next room.""",
412231: """ххх: I went to bed early yesterday.
ууу: I went to bed early too, though then I had to go home...=(""",
412232: """Kulichik: I wonder what happens in hell to those who were gloomy all their lives?
Ольга: They'll be sat on a little stool in the middle of an empty gray space, from which you can't go anywhere and there'll be absolutely nothing to do. And so they'll sit there... sit... for eternity.
Kulichik: that's pretty much how my workday goes... Т_Т""",
412233: """xxx: my parents are totally illogical. I asked for a dog — they gave me a little sister. I asked for money — they gave me a dog""",
412234: """YonBrover:
We all know there are cameras everywhere, that you can shoot video on any mobile phone, that cars get dashcams, that there are tiny hidden cameras.

Has technology "gotten to the point" where there's a tiny dashcam the size of a coin that you could hang on a cat's neck, to find out what it does at home when the owners are away?

igor:
listen, let's be direct — what exactly do you suspect her of?

YonBrover:
Do the flowers wilt on their own, or does she secretly eat them?
Does she jump on the table when the owners are away? (She's afraid to jump with the owners around, because they scold her)
Does she scratch at the door hoping the owners will come because of it?
Does she meow when there are no people around?
Or maybe she just sleeps all the time?
Or maybe she's joyfully going wild that the owners left?

pingvin:
And the most important question: WHO DOES SHE KEEP GETTING KNOCKED UP BY???!!!1111""",
412235: """xxx: you know how sometimes people don't get along? well here they couldn't agree on who's on top""",
412236: """xxx: Walking home from an install job, backpack on my back. Half the way I kept hallucinating that someone was chasing me on a moped behind me and revving. Kept looking back, thinking maybe I should move aside, someone's short on road. At home it turned out the screwdriver wasn't on safety and the liter bottle of beer lying in the backpack kept pressing the trigger...""",
412237: """xxx: Here's another one for "I hate the future". A promising client, damn. Harsh Swedish lumberjacks wrote an ICS in .NET. The dispatcher looks at the map, circles a chunk of forest with the mouse and sends some harvester-like thing there. An all-terrain vehicle with a claw and another little machine with a bed attached. Inside a touchscreen, satellite navigation, the whole deal. All thin client. Looks awesome.
xxx: But here's the catch: when their business-logic service goes down in the data center, the harsh Swedish lumberjacks sit dumb in the forest for days. The problem isn't so much that you can't explain over the phone where to cut and where not (though that's part of it), it's that afterwards you'll be damned entering the offline-cut timber into the system by hand.
yyy: Mark my words. Soon NATO will be conquering countries by banning them on Facebook and excluding them from Google results.""",
412238: """ххх: some girl just tried to sell me that she doesn't like February 23rd because it once fell on Friday the 13th and something bad happened to her then!""",
412239: """I work as a 1C programmer at a company. A woman from HR calls: "I posted a document here, and I need to unpost it, but it won't let me, it writes an error "fat client something..." — well, I know I'm a fat client, but what's it to him?\"""",
412240: """ххх: is it just my email that's not working?
yyy: I just got out of the shower. My soap works. Both bars. Didn't check the laundry soap.""",
412241: """yyy> Our neighbors communicate by renaming their access points""",
412242: """ххх: mom made pirozhki. they didn't turn out. she threw them all out in a fit.
ххх: now we've got a hellish scam — you walk into the kitchen, it smells like pirozhki, but there are NO pirozhki
ххх: hello, depression =(""",
412243: """Света ***: If you can imagine it, you can achieve it!
Михаил ***: I'll be able to shoot lasers from my eyes??""",
412244: """Teaching mom to use the computer.
- Well, congratulations, you went online all by yourself!
- (long pause) WHAT ARE YOU TALKING ABOUT!!!!!!!!! I'M IN MY BATHROBE!""",
412245: """Katy: It's like when I was booking a psychologist appointment and they asked: "What is the range of problems you'd like to talk about?" I answer: "Relationship with a man" Psychologist: "A man that already exists, or do we still need to find him?\"""",
412246: """alex: The quality of my clients' work would significantly improve if I were given the authority to beat them up""",
412247: """xxx: so, I decided to join the group of my future department, where I'm going to apply. Went into an album called "Hair-of-the-dog-2011". There are dudes playing football on ice in winter. With an American football. Everyone in felt boots
yyy: you picked the right uni""",
412248: """ххх: And what also pisses me off is that you don't call me back when I hang up on you!!!!!!""",
412249: """ХХХ: Why didn't you eat the salad this morning? It has a surprise in it, by the way!
УУУ: What surprise?
ХХХ: I added dumplings to it ХD""",
412250: """xxx: By the way, they stuck cameras all over our workplace. Should pitch an idea. Film an online TV show "Warehouse-2"
xxx: With you is Ksyusha Sapchak. This morning Pyotr came to work with a wild hangover. Will he manage to receive and distribute across storage zones one and a half pallets of blanks (90 slots, 45 items, 8180 pieces) before lunch? Watch after the commercial.""",
412251: """xxx: One of the great hitchhikers said that if you're not clicking with the driver and the cabin fills with oppressive silence, you should look out the window and utter the magic phrase: «They've brought the country to this, fuck...» After that even the gloomiest driver livens up and you'll spend the next two hours in lively conversation, where you don't even have to reply.
yyy: That works in any country, that's how I sometimes study conversational Hungarian""",
412252: """From diary.ru: Well I understand you're so advanced and it seems to you that everything here is perfectly clear. But my grandma, for example, still thinks that if she has a Nokia phone and I have a Nokia, we can talk for free.""",
412253: """xxx: today two black cats fought over the right to cross my path""",
412254: """a forum post in the Cars, Moto section (Toyota, Maintenance, repair)
yyy: Tried disconnecting 1 spark plug, the car drives worse now, but I'm in no hurry and the gas consumption went down! Can I drive like this?
xxx: Disconnect all the spark plugs and drain the gas, put up a sail or pedals — total savings!""",
412255: """xxx:
Crying our eyes out.
A question came in for the lawyer:

We filed a lawsuit and the trial has been going on for 3 years, and the court has been on our side for three years, i.e. in our favor. but the neighbor keeps filing for epilation. We've already spent so much money on courts!
How long can he keep filing for epilation???

xxx:
Indeed, FOR HOW LONG?!""",
412256: """<DoS> Yesterday my wife's niece, eight years old, sitting next to the laptop says to me "I didn't do anything". I burst out laughing and can't stop. To her silent question I answer: Do you know how many grown girls tell me these words during the day? Confess yourself! :)""",
412257: """On the subject of trips:

One of my female pen-pals from LiveJournal was going to join me for Romanian Transylvania this August. We were discussing trip plans and suddenly she writes to me "Sasha, you know, my tickets to Romania cost way more than yours". I didn't quite get what she was trying to say. Yes, I'm going to Romania from Minsk, and she's from Novosibirsk. Logically, it's somewhat more expensive for her to get to Romania than for me. You know what she said next? She declared that I'm an egoist and could've come up with a different route. I objected that I had posted my summer plans in my LiveJournal and invited travel companions, that she wrote to me herself, that my tickets were long bought. Why did she decide I'd change all my plans just because it's "far from Novosibirsk" for her? She got mortally offended at me and removed me from friends, and after removing me, wrote as a parting shot "Well you're an idiot, nobody has ever humiliated me like this. You're not a gentleman, you're a scumbag who only thinks of himself!". I didn't reply.""",
412258: """Ran into classmates in town by chance, hadn't seen them in ages. Standing around, sharing news. Then a disheveled little man runs up to us, clearly tipsy.
Man: You haven't seen the parking attendant around here? an old friend of mine... He couldn't have abandoned his workplace!
Me: no, haven't seen him. (turning back to my friends, continuing the interrupted phrase)... Lyokha's moving to Dnepr for permanent residence, already quit his job...
Man: Quit? Then have him get hired here as a parking attendant! The work's not hard, and the money's not bad. And I'm around here a lot, if anything.
Me: Sir, go away, let us talk!
Man: And you think I don't want to shoot the shit? The parking attendant's not at his post, after all...""",
412259: """Юль4ик: My kitten is an optimist. Eats food for adult large-breed dogs.""",
412260: """ххх: hey! How was your weekend?
ууу: it went great!
ххх: go away yourself, you dumb sheep!""",
412261: """Дядя Вася: why do girls get called "transformer substations" (ТП)? what's the point?""",
412262: """xxx: I only have two questions for this life
xxx: WHY THE FUCK MOW THE LAWN WITH A LAWNMOWER AT 8 AM EXACTLY?
xxx: and YOU ONLY JUST MOWED YESTERDAY, IS BAMBOO GROWING THERE OR WHAT, FUCK?""",
412263: """on Habr in the "questions and answers" section:

eldarmusin: How do I wean an intern off shitcode?

...
butteff:
Well I litter-trained my cat like this:
Stuck his nose in the shitcode and smacked him! Now he programs in the litter box like a pro.""",
412264: """ххх: Honey, why aren't you going to bed?
ууу: Waiting for the laundry to finish.
ххх: Go lie down, I'll do time.
ууу: Thanks)
ххх: No problem. When it's washed, I'll wake you.""",
412265: """Morning discussion of a new mortise lock among the installers:
ххх: No, this one clearly won't fit him
ууу: what if we punch it in?
ххх: well we could punch him, sure, but the lock still won't fit""",
412266: """Vladimir: mom found my tabletop D&D playlist and thought I'd joined a cult.
First there's a list of deities you can worship, then lists of prayers.""",
412267: """ххх: just rode the metro: across from me sat a dudyka in pants, a shirt, sandals and with a cart (a dacha guy)
ххх: he got off, but right away another dudyka got on (also a dacha guy) also in pants, also in a shirt and in absolutely the same sandals. and sat in the same seat!
ууу: what did you expect, the law of conservation of dudyka )""",
412268: """xxx: in our mall instead of one wall there's a huge mirror
xxx: and so people don't crash into it at full speed, there's this wide red line in the middle
xxx: today I saw some dalbayob try to run under it and he smashed the mirror! хD
yyy: xxx> YOU'RE THE DALBAYOB!!!!!""",
412269: """A comment on a movie reviews page:

Overall I liked the film, but it leaves an aftertaste of a blurred ending.""",
412270: """neperap: how do I wash off the green stuff?
alienpat: you don't!
neperap: what
neperap: for life now?
alienpat: well you could tattoo over it
neperap: over the carpet?""",
412271: """Today I noticed a kid writing the word "атцкий" on a fence. Asked him about the spelling, he answered that it's the new fashion: to make mistakes in words, but so the pronunciation stays. He believed the word is correctly spelled "аццкий" and has nothing to do with hell.""",
412272: """xxx: me and my bro are gonna play worms now!
yyy: you mean Worms?
xxx: no, fuck! we'll crawl out onto the asphalt after the rain and wait till someone squishes us""",
412273: """<Kote_v_rabote> reincarnation is a funny thing
<Kote_v_rabote> today you're a banker
<Kote_v_rabote> and tomorrow a butterfly""",
412274: """1: The cafes in Lviv are just one continuous feast! Everywhere we went it was delicious. The tastiest borscht, super coffee with the most delicate "Napoleon" for dessert...
2: You describe it so interestingly. Makes me want to go too! Is there a McDonald's there?""",
412275: """xxx: everything secret sooner or later becomes a drunken confession.""",
412276: """walking to work, a courier is hovering at the gate

courier: I need SUPER-POWER
guard: we don't have SUPER-POWER
courier: but how come?! I was told you have SUPER-POWER!
guard: SUPER-POWER is in the next building!

for a second I thought I'd ended up in Futurama..""",
412277: """Антоша:
Blurted out in answer to "what should I make your soup with tonight?" — "with pineapple". Now I'm trying pineapple soup. Good thing I didn't answer "out of seven dicks" like I was tempted to.""",
412278: """Марина: even though a fly flew into my mouth during my bike ride to work, I still insist that a slightly open female mouth is sexy.""",
412279: """Today on the "Volna" radio net:
G(traffic cop): "Volna", this is XXXX, on XXXXXX st. a manhole cover is missing, requesting action..
D(dispatcher): determine the manhole's ownership, the water utility, telecom guys?
G: and how do I determine that? it's not written on it.
Someone: sniff it, you'll know right away..
G: (after a pause) it doesn't smell like anything...
D: Copy that.""",
412280: """Анна: How are you feeling? )
Леша: Great! Energy's bursting out of me, work capacity is awesome ) Feel like running, jumping and everything)
Леша: But most of all burying my face in the keyboard and sleeping...""",
412281: """mmm: Where's the zoom on your camera?
vvv: One step forward, one step back. )""",
}

def main():
    src = open('chunks/chunk_30.jsonl', encoding='utf-8').read().splitlines()
    batch = [json.loads(l) for l in src[100:200]]
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
