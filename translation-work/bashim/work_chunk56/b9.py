#!/usr/bin/env python3
"""Translate bashim chunk_56 batch 9 (input lines 801-900, IDs 438937-439036)."""
import json

EN = {
438937: """Sam258: Saturday morning. Really wanted a soft-boiled egg. Put two eggs in the steamer to boil. Right there spins the Abyssinian cat. The cat is very curious and went to investigate - what's hissing and steaming there. So the cat doesn't get scalded I shout: "Don't sniff my eggs - you'll get burned!". My wife nearly tripped rushing from the bathroom to the kitchen to witness such an outrage...""",
438938: """xxx: We were heading to a friend's, wanted to bring booze, got picked up at the bus stop. Released in the morning, fell on the way. Went to the ER, got a cast on the leg. Friend went for crutches - fell, came back in the evening, cast on the arm. Brought the crutches. Decided not to go anywhere, invited the friend over, asked him to bring beer. He got picked up at the bus stop. Praying for his health.""",
438939: """xxx:
The best advice on building relationships with a partner I've heard from single people, on raising kids - from childless well-read theorists, on keeping pets from those allergic to them, but if they had animals, they know exactly how to keep them properly.
What can I say... half the country on the couch knows better how to play football, after eating a hamburger with cola will consult on a healthy lifestyle. My dad knows better than me how to wear stilettos, my friend - how I should feel during my critical days... The country fell apart, but the advice stock will last for 2 more generations!""",
438940: """xxx:
I think it's not only me this occurred to, but still one moment in movies and series with thriller notes pisses me off, when sneakily, for example, when a dude gets into a car, and from behind someone pulls a plastic bag over his head, and a transparent one at that! TRANSPARENT! So instead of grabbing the killer's hands, the steering wheel, all kinds of crap around, isn't it easier to tear this bag? And it's in every such episode!""",
438941: """xxx: Now on channels like RenTV and TV3 they keep telling versions that all ancient high-rises were built by aliens. And why hasn't anyone put forward a theory about aliens building the metro?
yyy: Excuse me! The metro was built by intraplanetians.""",
438942: """magnus_err: Kipling painted Indian culture, not preserved in the Russian translation, Bagheera in the original is "chief warrior, commander", Baalu - a brahman, Shere Khan - a robber etc.
cmpax_u_pagocmb: And who are the monkey and the snake?
magnus_err: Kaa - a Buddhist elder. And the banderlogs are banderlogs.
cmpax_u_pagocmb: And why does the Buddhist elder devour monkeys?
magnus_err: Tasty.""",
438943: """xxx: Are you a prophet? a seer?
yyy: I can think and use probability theory""",
438944: """SeaGlass: And to uncomfortable questions I can give uncomfortable answers, mom scolded me for a long time for "don't worry, if even YOU got married, then I have absolutely nothing to worry about\"""",
438945: """xxx: No need to distance yourself from society
yyy: Pfft. Away from society is where the best perks begin. You just don't notice
XXX: And what exactly begins?
xxx: Which ones?
yyy: You can not greet anyone. You can put on camo and go climb through dumpsters, and no one will ask what you forgot there. You can read some crap or write your own, and nobody will care. You can dress however you want. You can listen to whatever you want. You can carry a crowbar, a flashlight and a flute in your backpack around the city - and no one will have questions. You can pet all the cats you meet on the way. Or photograph a rusty Volga in the fog. Or translate comics. Or even draw.
And you won't have to explain to anyone why you do it.
That's just the first perk
yyy: Just don't say you never wanted to put a crowbar and a flute in your backpack""",
438946: """xxx: got up
xxx: wanted to write "woke up", but when I tried to type this word instead of the password in Skype - realized I hadn't woken up yet""",
438947: """Ad:

"Fill up a full tank of gas and get five more liters as a gift".

I have only one question. WHERE?!!""",
438948: """While my husband was at a friendly get-together, two handymen were replacing a door in one of the rooms at home. My husband came back drunk and, naturally, started meticulously evaluating the work done.
- So... hic... here everything's fine, did it well. And here... too. And here's clearly some flaw... BAM!... ow, my head... yeah, that's the flaw.""",
438949: """UUU: Funny thing happened the other day. Came to the local county capital. Went into a super-hyper market. Wandering around among stoves-TVs-sinks, and not a single bastard, characteristically, runs up with the question: "Can I help you with anything?".
UUU: After an hour or an hour and a half a dude comes up to me, pokes his finger at some rack and says: "Take this one!".
UUU: Me: "Huuuh?". Him: "No! The bigger one won't fit in a small apartment. You won't have room to turn around yourself".
UUU: Me: "Buuut...". Him: "Delivery to the station is free!". Me: "Uuuh...". Him: "Every morning at 9 o'clock a railcar goes your way. Make a deal with the driver, for half a liter they'll give you a lift! And from the station the duty car that drives the guards around will deliver it home in the best way!".
UUU: Me: "Just one question, who are you?". Him: "By education - a dropout officer. (MKhAT pause) And by calling - a self-taught logistician.".
UUU: Imagine, turned out to be our classmate! Only 5 years older. I ask: "How did you end up like this?". Says: "You won't believe!".
UUU: Turns out, he flunked the combat planning exam! Love my Alma Mater!!!""",
438950: """From Habr
XXX: They reported that someone is cracking a password — good for them. But how did they establish that this someone is related to special services? That's strange.
YYY: I think Google can tell more about a "hacker working for special services" than the FSB itself. It can tell: what diet the special officer's wife is on, what broke in his car a week ago, what porn the officer prefers, when he woke up yesterday, what his son got high on at school and why his sister had an abortion.""",
438951: """Service request registered under number ## (spelling preserved):
Request REPEATED!!!
Employees have access to %PROGRAMM%, but don't understand the instructions on how to export and import documents. It is ne cessary to perform these operations together with an IT department employee, since employees cannot perform these actions on their own.
Instructions attached.
Resolution: Users were seated, taught Russian language and mathematics for sequential reading and execution of the instruction items.""",
438952: """Neradence: They demanded I come tomorrow in "decent attire" and for some reason included in "decent attire" something white on top.
I only have two white t-shirts: one with "fuck apple" and a penguin eating an apple, and the one with that much-suffered Valhalla inscription that "die in the battle". The word fuck figures there too, yeah.
Question of the day, damn: which of these counts as more decent? :rofl:""",
438953: """Told today. Further from the narrator.

A dad with a four-year-old daughter are walking down the street ahead of me, the daughter is such a girly-girl in a little coat and beret, braids sticking out from under the beret. Dad is telling her something very impressively and sternly, talking, talking... The child glances at dad and listens silently and attentively. Finally, dad went for the decisive move. He even stopped, turned to his daughter and loudly asked:
- Well? Did you draw conclusions?
- Dad, what are conclusions? - the girl asked seriously, looking at him""",
438954: """xxx: where are my running pants?
yyy: I put them in the wash
xxx: damn
yyy: I forgot to start the wash anyway""",
438955: """xxx: Was picking up the kid from kindergarten, while we were getting ready, the teacher (T), a woman about 30, came up, further dialogue:
(T): You know, your Dima behaved very badly today!
(Me): What happened? (the kid sometimes misbehaves, but rarely).
(T): Today he argued almost to the point of hysteria with the kids and with me!!!
(Me): And what exactly were you arguing with him about, to the point of hysteria?
(T): He stubbornly proved that THE MOON ROTATES AROUND THE EARTH, and not THE OTHER WAY AROUND!!! Tell the child already that Copernicus proved that our Earth rotates around the Sun and OTHER CELESTIAL BODIES!""",
438956: """Geektimes, comments on the article "Google warned a Russian journalist about a wiretapping attempt by special services"

xxx: A friend sat at my computer for 5 minutes and now Google can't guess what porn I prefer. Keeps offering pregnant ladies... Been fighting it for half a year.""",
438957: """sss: I started working at a school at 19. There was a problem — getting into the school, the duty kids wouldn't let me in without indoor shoes.""",
438958: """xxx: It's raining 3D outside - pouring from above, from the side and from below""",
438959: """Usually I ride the trolleybus with headphones, but they broke, so I had to listen to real life on the way from work.
First next to me stood two boys, looking like - probably freshmen. One has another exam retake tomorrow, so the topic of their conversation was "which church is best to light a candle in for this". Discussed seriously, with statistical calculations.
Then they got off, a girl with a young man accompanying her got on. He was silent and occasionally shook his head, she talked non-stop. A few of her gems off the top of my head:
- I'm generally very aggressive, but I can keep myself in check. Want, I can show you where I need to be hit to make me furious.
- And if I completely lose my memory, will it be easier or harder for me to get up in the morning?
- I don't like people who talk off-topic. Except myself.

In short - turns out so much interesting stuff passes me by, I should repeat the trip without headphones sometime.""",
438960: """To the news about new banknotes of 200 and 2000 rubles:

xxx: Eldorado: no, 200 is not needed. Need 199.99. So no change needed""",
438961: """Working at a sports store (family business, all that). Not many tourist goods, but compasses are especially popular (a toy for a kid, and for school, and for mushroom picking in the forest). People often come in who have a weak idea of how to use one. So a mom with a kid came in, took it, twirled it in her hands and indignantly asked:
- Why does it always point one way with you?""",
438962: """From Geektimes
UUU: A 16-bit tale.
Ilya Muromets went out to fight Zmey Gorynych. Chopped off his head, and two grew back. Chopped off two, and four grew back... Ilya chopped off 32768 heads — and Zmey Gorynych died, because he was 16-bit.
VVV: Unlikely. Gorynych is alive, alive. The remaining 32767 heads are proof of that!
WWW: Why? It's overflow.
XXX: Because he was unsigned.
YYY: No, signed. He's a negative character.
ZZZ: Gorynych is never positive — so no need to store this flag specially )""",
438963: """Since April I've been sleeping with the vent window open, supposedly for better sleep and health. Unexpected bonus - the cat now comes to sleep with me, on her own little couch by the radiator she's not as comfortable now as with me""",
438964: """My husband and I, an American, have been living together for a year, planning to visit my parents in Russia, and the only phrase he memorized in Russian is: "Kitties, kitties - mandavoshki!\"""",
438965: """xxx: How's the dog?
yyy: Thank god, kicked the bucket!
xxx: My condolences :(((
yyy: Ate, played with a ball, ran around.
xxx: E-e-eh... you didn't mix it up with "recovered"?""",
438966: """xxx: Our philosophy professor pulled his signature apple trick today: laid out a couple on the desk and asked: why are all people different, but apples are the same?

xxx: so I came out like, thinking: I'll show him a vein here, a speck there... I come out, and they really are the same! Same size, equally green, polished, waxy...

xxx: I thought about it, and pulled out mine: one and a half times bigger, red, juicy, you can tell right away - delicious! And I say: well yeah, glamorous blondes are all the same too!""",
438967: """zzz: You could make a disaster movie about how this house slides into the Irtysh.
yyy: That's hard. You'd need to stretch the action for at least half an hour, and a house can't take thirty minutes to slide into the Irtysh? That wouldn't be a disaster movie anymore, but a leisurely narrative. The main hero woke up, melancholically screamed in horror for ten minutes, brushed his teeth, watered the cacti, ran out of the apartment, came back because he didn't turn off the iron, went downstairs, waited for the house to cross Irtyshskaya Embankment so as not to step onto the road...""",
438968: """xxx: Well, I've googled myself into it. Contextual ads now offer me not a cruise or a car, but to try myself in special forces for a day )))) Indeed, what else can you offer a person who googles Scotch whisky, green berets and helicopters? But from such dense study of the source material I'll probably refrain...""",
438969: """gray_bird: Studying three-phase power on unprotected stands - a most fascinating business, almost every lab someone's stand would start... um... smelling. :)
di_halt: Smelling? Ours exploded so that a pillar of flame reached the ceiling and the ceiling got sooty. Three-phase short circuit at 380 volts. And the teacher didn't even flinch. Tore himself away from reading the journal, looked at us like morons... and said only - broke it - fix it. So we fixed. Replaced all the burnt-out electronics, cleaned the stand of soot...
alex_avr2: Chelyabinsk? :)))
di_halt: Yep. SUSU, Electric Drive. Drive labs.""",
438970: """They were replacing faucets on the risers. The plumber refused to install mine, ball-type, exhumed from somewhere a couple of "good, proven" Soviet-style ones... On the third day they started leaking in broad daylight, when everyone was at work, flooding the neighbors. When I tried to make a scene at the housing office the chairwoman killed me on the spot with the phrase: "Excellent faucets, you probably just TURNED them". Fled this madhouse so as not to get infected, called a plumber from an ad, enjoying the result.""",
438971: """Decided to spend today in silence, so three days ago I moved my birth date in all social networks where I'm registered a month forward. So far it's working.""",
438972: """18:46:31 [Nastyona]: Can we talk?
18:47:06 [Sery]: Hold on a sec
19:53:58 [Nastyona]: Well...?
19:55:31 [Sery]: Okay, I'm drunk, talk""",
438973: """aaa: Today the younger cat was yelling, pestering the older one, twitching his tail, dashing around the apartment and jumping on the couch. Timych treated it all phlegmatically, only closer to evening starting to get feral.
aaa: When Richard refused dinner, and went back to suffer in the room, I had to think.
aaa: By the outlets stood a plastic tray for drying winter shoes, yesterday I washed it and put it in the closet - until next winter.
Had to return the tray.
aaa: The cat instantly quieted down, merged with the tray and is clearly happy.""",
438974: """xxx: "...the thin line separating a pathologist from a therapist has long since worn off." How to remember it. Simply magnificent!

yyy: I have a pathologist friend. Thanks to his recommendations I haven't caught colds for several winters now. That's how it is! ))) The author very subtly noticed this. )))

xxx: E-eh...Hmm...And how do you handle sunlight? :)))

yyy: Fine. And I love garlic. ;-)""",
438975: """boss: where'd you get this info?
me: worked with adjacent contractors, asked for documentation, they gave me a flash drive, said what you need - download it. and what, am I going to rummage through other people's flash drives? I downloaded all of it.""",
438976: """Denis: Jackie is barging into the room. Kicked her out. Because she sits, looks at me and whines again!
went to the bathroom, there's food. I get up - she runs!!!
Yulia: She's not whining. She's talking to you))
Denis: I don't understand cat language )
Yulia: Such a waste)
Denis: well yeah. So many years with cats and I don't understand _
Yulia: What if she wanted to tell you something really interesting)
Denis: Well I can guess what: run to the bathroom quick, look how I can jump into it!""",
438977: """A plump colleague complains to a friend: "Went into a healthy food store, wanted to buy a weight-loss shake, and there it's all gluconates and sorbents in the ingredients - pure chemistry, didn't buy that poison!"
Apparently, the phrase "gluconates and sorbents" means to her something like "glutamate and preservatives". Harmless calcium and activated charcoal got it for nothing.""",
438978: """Flood news, comments:
xxx: "draining water" is more applicable to a toilet, at a hydroelectric plant - they discharge it!
yyy: The water in the Kama is so dirty that I'll agree with the author - it's exactly "draining water".""",
438979: """xxx: today spent half the day lying on the couch reading the internet on a tablet, realized I was wasting time, you can lie around like that all day, sitting in a cafe reading the internet on a tablet""",
438980: """Nats: when we were kids, my older brother had a buddy. He was healthier than his peers and did judo, the whole yard called him strictly by his surname. I once asked my brother why they call him by his surname, to which my brother answered that the kid is the only child in the family, long-awaited and named after his grandpa, they just didn't think about the consequences. And the grandpa is named Sasun.""",
438981: """Dazz: remember the game Terraria, where you gotta run, dig, build stuff
Dazz: I built myself a castle, forged badass armor and weapons so nobody would beat me up
Dazz: and I go fishing at the neighboring lake :))
Dazz: and I gallop to the lake on a unicorn
Dazz: to catch some damn perch""",
438982: """xxx: One neighbor owes me a thousand, and a second neighbor owes me a thousand, and the neighbor below owes me a thousand, and there are no neighbors above. Now at any time I can make as much noise as I want, nobody comes. And how much is your comfort worth?""",
438983: """Sber just emailed me the "Auto-payment for traffic police fines" service.
Flashed through my head: "Unlimited?!".""",
438984: """Living alone in the apartment (but with a cat), still haven't taken down the Christmas tree (because it is, firstly, artificial, and secondly, damn it, beautiful and with a garland). Took a vacation, tidying the apartment - and thought that well it's about time to put it (the tree) back in the box... I don't even look at it anymore anyway. So decided - one last time I'll turn on the garland (first time in a couple months), admire it, and the next day (i.e. today) finally take it all down.

Fell asleep at night in the middle of cleaning, with the lights on in half the apartment, with the garland on. And I live in Petersburg, and we've had +8..+10 degrees for a week and a half. So... now I woke up, and outside it's snow, cold and a blizzard raging.

In general, if anything, excuse me... I turned off the garland, assembled the tree - hope it'll get warm again now.""",
438985: """xxx: My colleague and I were wondering over lunch: who interviews HR people when hiring? And do they ask them the same stupid questions?""",
438986: """foo: Good thing I walked - found 200 euros on Nevsky. Karma law in action)
bar: 16k...! Kherson! You're luck!
bar: *Kherase!
bar: Spend them on a good cause!
bar: *Spend
foo: spending on a good cause - I'm always ready!)""",
438987: """As soon as the kid grew up a bit and learned to mash buttons on the keyboard himself - the dad-admin's battle began against unauthorized use of the comp and the internet.
Having changed the internet password once again, the satisfied daddy-admin relaxed, but a couple days later suddenly discovered that someone at night still gets into the comp and browses the internet.
Started digging through logs on the comp and such, and accidentally found a fresh video file showing me typing my new admin super-secret login and password on the keyboard.
Turns out, this kinder-hacker took a camera, turned on video recording and put it next to the comp (like it's just lying there), so the lens looked at the keyboard, and he himself made a request.. like they assigned an essay at school, search please.
In general I busted his spy device, changed the password again, and a couple days later busted the kid at night on the internet again.
And after another password change the kid asks me to dig around on the internet, but I already feel in my gut that this is not without reason. I look around and see - the kid from the kitchen through the long corridor is looking through binoculars in my direction, and next to the comp stands a mirror on a stand, aimed at the keyboard...
the kid was about 8 then..""",
438988: """Walking outside with the kid, warm, +20C. Nearby are walking a mom, a grandma and a two-year-old boy. Then a dad comes up to them and hands the grandma and the mom each a hat. At the same time, the dad very strongly and loudly insists that the women put the hats on, it's cold outside, really freezing, since the kid is wearing a hat, and a woolen one at that...""",
438989: """xxx: omg, Chrome is already at version 50
yyy: time for a denomination!""",
438990: """ZRK47: Working in an office located in a basement you can find lots of interesting stuff. For example observed how a tiny spider caught a tiny ant in a tiny web and circles around it waiting for it to tire, then started climbing onto it and started tying the poor thing's legs. And all this was happening on a write-off act...""",
438991: """Sitting at work.
An employee swears at constant changes in the task. Can't get to the final version:
s: All change is for the better!
me: Uh, no. That was only true in school!""",
438992: """Nick> Mdas... whoever thinks flashing controllers via LPT port is dangerous, unstable, glitchy and inconvenient — just never tried flashing with a "slowpoke". After a "slowpoke" the simple rules of flashing via LPT (don't wear white, don't wear tight... that is, power the controller only from the comp itself, don't hot-plug, don't chase speed) — seem light, unburdensome and obvious, like not stepping onto the rails in the metro and not jumping into the shaft if the doors opened but there's no elevator there %)""",
438993: """xxx: he's like a bat. He tries to shit on opponents' heads from top to bottom, but forgets he's hanging upside down. And it all lands on his head""",
438994: """me: I read that lions have such a rough tongue, like sandpaper. They can eat their fill of meat without even biting it. Imagine!
wife: interesting, and how do they lick their balls then?""",
438995: """xxx: our corridor is long and narrow, I came out of the office, buried in my phone, stood facing down the corridor, feet shoulder-width apart. from the office at the other end of the corridor comes out a programmer, in exactly the same pose buried in his phone stands facing me
xxx: cowboys""",
438996: """- Why are you without cognac? Where's the cognac?
- I'll buy it in the evening.
- Why later, let's have a shot right now.
- Yeah, and how will I drive the car away later?
- Just like this: "Shoo, shoo, get out of here!\"""",
438997: """xxx: Spring. The office awaits the arrival of the economic crimes unit. Shredders chirped joyfully in the offices.""",
438998: """Xxx: and I went swimming)
Yyy: where?
xxx: to the river, at night)
yyy: what river do you have?
xxx: pripyat
Yyy: is it calling?)
xxx: what do you mean?
yyy: everything's clear with you))""",
438999: """xxx: I also want to smear three colored stripes on canvas and sell it for hundreds of thousands of dollars
yyy: that only works if you're either famous or an elephant""",
439000: """argenlant: "and add to the design some little enticer that wouldn't distract from the product and the slogan" (c) verbal brief from the client""",
439001: """xxx: 
Went up to the chief engineer at work and jokingly asked: "well, who should we load up with work?"
Now I'm sitting, working. Could've been drinking tea.""",
439002: """Put a My Little Pony horse on my avatar. Next day:
- Andryukha. change the avatar, my daughter thinks I'm chatting with Applejack and demands details and wants to write too ))))""",
439003: """Discussing forum topic content:
xxx: In first grade kids fight, and hard. Today the moms of the participants gathered to talk to the teacher, in the end 2 moms almost fought.
And you say controversial topics.""",
439004: """xxx: Mityai, by the way, tell us how you got into business.
yyy: Well... If starting from the very beginning, it was in '86, I was in the army then. The brass got piss in their heads to hold exercises, not combat ones, but, so to speak, domestic ones. Like how our unit can set up on the march, how the field kitchen deploys, how's supply, and then maybe shoot a bit. Well, in short then they were testing new rations, in packaging with air bubbles. The point was they could be floated down the river, like to send to the front line without blowing cover and risking suppliers.
Positioned the unit, explained the task, three days sitting by the river, catching rations. We were upstream... At first we just stole others' rations, like they drowned (those who didn't catch got another portion). Then we learned to identify officer rations and eat those. Then we started dealing rations. When Lyosha and Shpalera stretched a camo net across that narrow but swift river, a colonel arrived, gave us hell, gave the whole squad extra duties up to our ears, and then told the experimenters that such a supply method... Is not without its drawbacks)""",
439005: """Nick> and at that moment the electrical panel in the stairwell caught fire.
Nick> Good thing I hadn't been sleeping for a long time
Gluk> did you get to watch?
Nick> Managed to quickly figure out what to do
Nick> when they installed the dual-tariff meter — they didn't tighten a screw and the neutral sparked
Nick> red-hot wires — quite a sight
Nick> I called from the emergency panel, right away a completely shell-shocked electrician flew in, disconnected our phase and started unscrewing the burnt common ground
Nick> you already got the catch, right?
Nick> for several long tens of milliseconds his clueless body was the common neutral for the combined load of the other two apartments
Nick> in short, after him I decided to turn on my internal ones only after I checked everything I could with an indicator screwdriver twice %)""",
439006: """from Habr:
xxx: I like IE3...
No ads and other tracking crap. And if something's wrong it just crashes without opening any crap on the site...
zzz: So it's like the Roman emperor's food taster for you? Died — means the food is poisoned?)""",
439007: """xxx: A pigeon got hit by our house, right on the median strip, and the road isn't exactly narrow, just enough for two cars to pass. So now they hug the curb away from the carcass of that pigeon. I think I figured out what to make medians out of...""",
439008: """And again Geektimes

per: Because you should sleep with IT guys and not just anyone
0xd34df00d: You don't even have to sleep, just be friends.
zhigalin: But better to sleep:)
0xd34df00d: Better. But I'm sensibly assessing my chances.""",
439009: """xxx> what about salary, dunno?
yyy> yeah, dunno what about salary, eh, yup.
xxx> great and mighty is the language""",
439010: """"A woman went out for cigarettes and a day later was found in Biysk"

xxx: On packs they need to write as a warning: "Smoking leads to Biysk"
yyy: But first smoking causes a bus.""",
439011: """xxx: people buy iPhones for a year and a half, why be stingy with tools.

yyy: I picture the scene: a class reunion, for example. Everyone showing off different gadgets, and then I whip out a micrometer""",
439012: """xxx: Today, solving a crossword, to the question "Plant cell element" I confidently derived "Vacuole". Nearly teared up, I waited 30 long years for the moment to use this school knowledge...""",
439013: """recently learned that when choosing names for born pets it's customary to take the first letter by the litter's serial number. Now I'm wondering if dad gave himself away naming me Yasha""",
439014: """deimos: I think I'm a tyan-hipster girl.
deimos: Woke up at 4 AM. Rain, cold. Wrapped myself in a blanket, sat at the comp and smoking a cigarette.
deimos: not thinking about him yet.
deimos: Can I still be saved?
v.zhuravlev: it depends
v.zhuravlev: Answer three questions
v.zhuravlev: 1. Are you hard to find?
v.zhuravlev: 2. Are you easy to lose?
v.zhuravlev: 3. Are you impossible to forget?""",
439015: """(the forum is discussing the flooding of the Bypass road in Yekaterinburg)

xxx: slowed down fearing aquaplaning. didn't know about the potholes and thought that on the bypass everything's fine with that.

yyy: you from Moscow or something, where everything's fine on the MKAD. Our Bypass is called that because it's better to bypass it""",
439016: """UB3xxx: decided to remember my youth and build something radio-amateurish, but simple. advised a reflex superheterodyne on one transistor. what do you say?
RN3xxx: elite construction. perfectly combines two concepts - "digging a trench with crowbars" and "elusive Joe"
UB3xxx: explain
RN3xxx: "digging with crowbars" - that's about selecting the transistor and tuning the circuits, and "elusive Joe" - about broadcasting on MW and the construction as a whole""",
439017: """Nick> Damn, I have a 3D mess on my desk.
I had such a mess on my desk that I folded a folding lid onto it, leaning on the especially sturdy elements of the lower mess, and on top spread another mess.
I HAVE A THREE-DIMENSIONAL MESS!!!
Give me a fourth dimension — I'll stuff crap in there too... :-/
Gluk> and without the lid the layers were flat, yeah...
Nick> Well how flat — one item thick...
Nick> Although who am I kidding. I've had a three-dimensional mess for a long time :(
Gluk> yes yes, one composite item
Nick> I was quite able to pile a layer of fresh mess on top of the old one before :(
Gluk> archaeologists will appreciate your talent. someday..""",
439018: """Some winter a buddy comes to his car in the morning, it's open, the steering wheel lock is lying on the seat next to a note: "man, cool security system".
Apparently they tried to steal it, but couldn't. The main question: what's wrong, since everything was - opened. turns out, the buddy went to the car wash in the evening, parked as usual on the handbrake. The brakes froze solid, not letting the car be used until spring.""",
439019: """xxx: This story happened to me personally a few years ago..
yyy: with exactly these words, I always start telling tall tales, in which I lie a lot.""",
439020: """A colleague, a history teacher, showed a written history work of one of the students. Something about the Middle Ages. By the way the young historian's text often features the word "vossal".
This is the picture it paints: The castle square right after the auto-da-fe, a crowd of motley medieval inhabitants and a raging gray-haired inquisitor yelling -"Let us vossim, brethren!" And what? Very plausible.)))""",
439021: """den_stranger: It's hard to have geek parents probably. Right now my wife and I are trying to convince our kid that her favorite rocking horse is named Plotva.
Artyom: Normal! Just before school explain that the Russian anthem and the Imperial March are somewhat different pieces.""",
439022: """dyomka: Help a future Kalina owner. Often on fishing trips have to sleep in the car, hence the question: 1. How do you arrange yourselves? 2. Do the front seat backs fold level with the rear seat?
AHT: Much depends on the length of the members. But having a car lets you easily bring a tent, sleeping bag and mat. That's more comfortable anyway.
Alex: AHT, and what does the length of the member affect, so as not to touch a neighbor or a female neighbor with it? :)
AHT: Don't know what's there with the female neighbor, but I meant legs.""",
439023: """In Alexander Garden a young couple, looking at Przhevalsky's bust, discussed who they see.
- Look, look! He's got shoulder boards! He's some kind of general!
- And what's next to him?
- That's a camel! Then everything's clear! That's Suvorov!""",
439024: """xxx: I'm afraid to ride the elevator, in the cabin while moving you constantly hear some grinding, knocking, they're rarely serviced, only visual inspection.
yyy: And listen to yourself - you'd be surprised how your bones creak and crunch, blood gurgles, flowing around atherosclerotic plaques, something keeps rotting and rumbling in your stomach... And you're rarely serviced, at most inspected from the outside.""",
439025: """Question about thermal paste:

xxx: Sanya, hi
xxx: Listen, do you still have any cold-grease left for the laptop?""",
439026: """x: It's 2016, and they're still cranking out web servers... neanderthals!
y: Yeah, everyone's long switched to web browsers. After all, according to statistics, web browsers are much more popular than web servers, and the share of the latter keeps declining. Soon only browsers will remain.""",
439027: """Discussing weekend plans
xxx: *the person who at this time will be communing with the power of the earth, envies greatly*
yyy: Maybe you'll discover a second element in yourself, so it's not so bad :D
xxx: Shovel handle, give me strength!
xxx: Summoning the shit-demon to the plantings.
xxx: Horseradish, I'm ripping you out!
xxx: Onion, I am your father!
yyy: I'm slowly cleansing the carrot of sin x)
xxx: 50 shades of tomato""",
439028: """a: a friend has 5 cats who like to sleep in the dark corridor at night.
a: every night she plays "cat roulette" when walking through it
a: the height of chic is to walk there and back and not step on anyone""",
439029: """How my relatives travel.

19:40
(sms)
Already on the way. Will be soon.
(to the city = 1:20 of driving)

00:21
- Hello! Is everything okay with you?
- (clearly sleepy voice) Yes. Everything's fine with me. Sitting waiting.
- Are you in the city already? You've been gone long.
- No. We're near Ensk. (1 hour drive to the city) Two tires blew. Everyone left to repair. I'm in the car.

Sleeping. hm... True. Everything's fine.""",
439030: """xxx: And why does a squirrel need tufts on its ears? 
yyy: To paint the hollow!""",
439031: """Game review :

Bought it back in the day on Steam, when the game was in an even earlier beta. Scraped the ground with a paddle, killed a crab with a paddle, sailed on sand (still with the paddle!) to a neighboring island, and then the main character got eaten by a shark - the paddle trick didn't work on it for some reason.""",
439032: """xxx: I remember how at biology field practice we identified mushrooms collected in the neighboring forest.
Before handing out the field guides the teacher once said not to taste anything, even if the guide asks you to.
To the naive questions of freshmen: "how come, the guide can't ask to taste something dangerous", she brought an old guide on whose yellowed pages it said:
1.a) The mushroom has a weak garlic taste ... Go to p.67
Or 1.b) The mushroom is deadly poisonous ... Go to page 100

yyy: apparently botanists back then didn't feel sorry for other botanists 8-)""",
439033: """Liza: Today my violist friend stayed over. I told her that while I had no mattress I slept on the couch. And she completely deadpan answered that while she had no couch she slept in a double bass case.

Alexander: musicians are like that. I have a friend, a double bassist, so to speak stayed over, made a baby with his wife in a double bass case

Liza: damn, a universal thing!
What else can it do??

Alexander: You can even float on it

Liza: on the case?? or on the double bass?

Alexander: In the case on the double bass""",
439034: """Today on a torrent tracker they asked me to disable my ad blocker, because it prevents them from earning.
Turns out pirates don't like it when THEY get robbed.""",
439035: """Sent some text, a quote. Signed the page, publisher, note number, all as it should be. In response - give me the link!
-There's no link - I say. This is an excerpt from a printed book.
In response silence, stupor apparently. A book, printed, how come?""",
439036: """Got a call today inviting me to an interview at a company, there was a very entertaining dialogue with the HR lady (HR):
HR: Are you an optimist?
Me: Why do you ask?
HR: Well how do you cope with life's difficulties?
Me: So you're saying you delay salaries here?
HR: Yes...""",
}

if __name__ == "__main__":
    print(json.dumps(EN, ensure_ascii=False))
