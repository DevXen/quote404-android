#!/usr/bin/env python3
"""Translate bashim chunk_25 batch 8 (input lines 701-800, ids 407774-407874 sans 407867)."""
import json

EN = {
407774: """*Nesmeyana*: Saw tiny Acer laptops for 15,000. They're so cute, but I never figured out why I'd need one.""",
407775: """xxx: he's a shit actor anyway! even worse than Van Damme.
yyy: give me back my debt and my hard drive today.
xxx: what? offended? come on, everyone has their own tastes, what's the big deal?
yyy: nah. Just after you called Chuck Norris a shit-actor, I think it's better this way
yyy: you might die tomorrow""",
407776: """Dialogue with mom.
M: sweetie, tomorrow I have a very serious business meeting planned
Me: that means tomorrow evening I need to vacate the premises, mommy?
M: exactly, my daughter
Me: fine, mommy. but soon I'll also need to discuss serious business with an important person. you know what I mean?
M: sweetie, you can discuss your business as much as you want during the day
Me: well who would know better than you, mommy, that serious negotiations go easier in the evening?""",
407777: """NSaturday: Oleg, you walk into accounting - nobody... You walk into the kitchen - oppa! accounting!""",
407778: """xxx: Used to bring a handkerchief to work to wipe sweat. Yesterday they finally installed the air conditioner.
xxx: Now I use the handkerchief for its intended purpose.""",
407779: """Me: by the way, yesterday I finally downloaded this update for AIMP )
Friend: what's good in it?
Me: the new-version notification doesn't pop up""",
407780: """Shadow: Those who complain that after spilled coffee their keyboard "sticks", apparently never spilled condensed milk on it... =(""",
407781: """xxx: how do I write in the plan that I'm not going to do anything today?
yyy: Write "fault diagnostics and preventive server maintenance" )""",
407782: """Malinka: By the way, how did you take the news that you're still my student this semester?
Malinka: You realize you and I are now bound for another half a year by tight physical connections?
Malinka: Do you swear, Maxim, to diligently prepare for classes, not cheat and learn all the routines till the army do us part?
DrakON: Nope
Malinka: Then it will part us""",
407783: """x: why the sad status?
y: my girlfriend dumped me..
x: eh, you'd tell anyway, so I'll ask: why?
y: I told her 50% of my brain is occupied with information about her!
x: didn't believe it's that much? or offended it's that little?:)
y: she thought the other 50% is occupied by another..""",
407784: """from Google Q&A
question: a comb got tangled in my hair what to do
answer: tangle scissors in there too. maybe they'll sort it out.""",
407785: """Djoser: I'm a tough Siberian man! not afraid of anything except mom""",
407786: """Kristi: know the difference between epilation and depilation?
Olya: depilation is hair removal, epilation is something legal/judicial
Kristi: don't call me anymore""",
407787: """=V!peR=: When I said I don't drink, they asked - what happened to you?""",
407788: """Little sister, 19.
sss: who knew everything's so complicated with people, I wanna go back to kindergarten...
sss: there relationships are proper, immediately clear who stole your Chupa Chups""",
407789: """xxx: Our boss has an anti-bot...
xxx: Whoever enters his office - the first thing they hear: "'по собственному' is written with two Ns!\"""",
407790: """xxx: You think this is a drag
xxx: A drag is when you have tonsillitis and urgently need to run to a classmate, otherwise your diploma is screwed
xxx: You go out and!.. Damn rain!
xxx: And you stand under some big stinking tree for half an hour, waiting it out, feeling your temperature climbing
xxx: And an acquaintance passes and says - what are you standing here for, the rain ended 20 minutes ago, it's the wind shaking water off the tree""",
407791: """xxx: what do you think, if I write in my resume under key skills: PC work (experienced user), people work (experienced user) - will that be OK?
yyy: an experienced user of people? oooook!""",
407792: """xxx: has Natasha already done the peresch-otsos-kidkoy?
yyy: to whom?! with what?!!""",
407793: """Litien: gentlemen, has anyone converted pdf to jpeg via php?
Mekor: monsieur knows his perversions
Litien: can't deny it""",
407794: """xxx: I have a Zenit ET
I inserted the film, set it up, secured it, made sure it winds, but took a test shot, opened it and there's no image.
What's the deal?""",
407795: """from a forum:

xxx: TELL me is there a site where you can look up a mobile phone i.e. find out who the number is registered to
not for fraud just urgently need it PLEEEASE!!!!

yyy: Federal Security Service, hold yourself together!""",
407796: """<bred> a guy is running around the office yelling that a pine fell on the gas pipeline
<Gillan> calm him down, ask exactly whether it was a pine
<Gillan> maybe a fir or a spruce""",
407797: """her: how does so much food fit in you?
him: I have a good archiver!""",
407798: """<serg> Clients called, say our antivirus causes BSOD on their machines, asked to URGENTLY fix the problem.
<serg> URGENTLY - because the clients are a nuclear power plant near Rostov""",
407799: """texhosky: As a kid I tested a floor lamp. If you take the bulb out, the electricity stays inside. And if you stick your fingers in, quickly pull them out and don't scream, you can avoid getting it from the turned-away grandpa and dad who were changing that bulb.""",
407800: """xxx: I also love order
xxx: But my own order )
xxx: And many don't understand it and call it a mess (""",
407801: """Tsarapkin:
In PW I'm sitting selling herbs for 4k, a guy runs up puts 3.5k. I put 3.3, he puts 2.8, I put 2.5, he 2.3k. I stand up, buy everything from him and put back my 4k. And he PMs me "You scumbag!\"""",
407802: """xxx: I slept with her in a state of affect.
yyy: Being drunk at the corporate party is now called "a state of affect"?""",
407803: """From a site with hotel reviews:

Towels are raggedy, poorly washed-out stains on the sheet and pillowcases. Linens weren't changed. To cover yourself - only a waffle blanket of dubious freshness, no sheet, no blanket.

But the toilet had 4(!) rolls of toilet paper, which, considering the cook's qualification, turned out not superfluous.""",
407804: """megasem: the boss says "today the internet is just flying"
megasem: and that's me forgetting to start the torrents )))""",
407805: """xxx: how's your tooth and arm?
yyy: like bad employees
xxx: what do you mean?
yyy: one's not at his place, the other's sick )""",
407806: """xxx:
celebrated the new year at work, well drank.
a kid works with me, 23 years old, doesn't drink, well he got carried away...
bugged the CEO with the question "tell me, how do I work?"
"no, tell me honestly, how do I work?", totally wore him out.
"you work well, it's just that what you produce is shitty!\"""",
407807: """Basya: Where'd he go.
Ser: Yeah the wife took the computer. Sitting and already 15 minutes watching something with great interest.
Ser: Got tired of waiting, figured I'd take a look.
Ser: And she saw a gif in someone's forum signature, with a wolf running against the moon.
Ser: And turns out she was sitting, waiting, where he'd run to
Ser: I'm in shock.
Basya: So did he run there?""",
407808: """Kad: yesterday me and a friend swapped laptops. He's screwing with Linux and I'm with Windows )
Lom: swingers )""",
407809: """xxx: Damn, half an hour trying to download a book from the net, and every second link sends me to prostitutes!!!!!!!
yyy: maybe they have the book?""",
407810: """Knight|GTR: Today I saw the scariest dream of my life. To wake up, I had to close a pile of pop-up flash banners. Barely woke up...""",
407811: """Agent 007: hi
Masta gaFka: hi, who's this?
Agent 007: don't recognize?
Masta gafka: James?..""",
407812: """pumko: just saw an inscription on a house wall - "gonorrhea infects those who are blidn"
pumko: the person clearly felt they missed a letter, and that it had to be stuck somewhere...""",
407813: """Ogre: how to run 32-bit Civilization 2 on 64-bit Windows 7 ?
Kirych: by a miracle
Ogre: and where do I download it?""",
407814: """xxx: have you tried screwing in bright light, with a crowd of people, when they yell at you and point how you should do it, and for four hours?

yyy: If you replace "screwing" with "writing code", and 4 hours with 8, that'd be my previous job.""",
407815: """From a forum:
Girl: Want to buy a laptop with a 15' screen, with a webcam, for work, for watching movies, for the net. Inexpensive, max 25000. Help me choose.
comment: Pretty much any laptop fits the requirements in the first post. So choose like a true lady: by color!""",
407816: """xxx: by the way in all rooms there are built-in fridges - windows, they're equipped with a special compartment between the panes for storing lots of food )
yyy: in my distant childhood dad used to lock the cat between the window frames like that )
xxx: was the cat tough? or dad?
yyy: just two fools. the cat was still small and fit perfectly sideways, dad was checking - whether it's growing or not )""",
407817: """xxx: what to expect from a generation that discusses online how to summon a seductive succubus to shag her?..
yyy: a birth rate decline""",
407818: """Analia: don't judge a person by their nick)""",
407819: """xxx: our brain combines the brain of a reptile, a primate and a mammal. the primate brain tells us "let's live in peace!", the mammal brain "let's live in peace, but first let's kill that bastard!", and the reptile brain "fuck it all! let's go to a peaceful march and screw!"
yyy: you apparently only listen to the reptile""",
407820: """From a forum:

xxx:
is it normal - when in Linux you have to do something through the ass - Linux users don't need that

yyy:
Once more explaining the principles of FOSS.
See a bare ass - sew underwear and give it to the ass's owner.
Can't sew - tell the ass's owner where underwear can be bought.
Don't know where to buy - just tell the ass's owner "Your ass is bare!".
Don't want to do any of the above - shut up, none of your business.""",
407821: """xxx: Once a guy came to our editorial office bragging: "look what a fish I caught, damn!" and shows a photo. May 14, he says, it was. Print it! and May 14 is the peak of spawning season, when fishing is banned, fines are tens of thousands of rubles. And he has no clue about it.

vvv: I'd print it. Why not :-D

xxx: new column - "CAUGHT IN THE ACT\"""",
407822: """Darkness: Fucking cold snap... The morning quests to find matching socks started again :(""",
407823: """xxx: The whole enterprise hates me! My profession is such that I rat everyone out terribly!
yyy: Come on, what, they'll kill you or something?
xxx: Well you know how it goes in reports.. Near an enterprise of 153 people, an unknown organized group of 152 people beat up an employee of that enterprise..""",
407824: """xxx: Was funny at the bank yesterday. A girl asks what I do for work, I say – technical maintenance of computing equipment, she out loud while writing – PROGRAMMER. I say, no, I'm not a programmer. She was so surprised, I thought her eyebrows would stay on the back of her head forever – WHO ARE YOU THEN? I say – a programmer writes programs, and I set up equipment. She goes, aaaaah, it's just that we common folk call you that.

xxx: I wanted to tell her the parable of Glaznyuk and Pizdyuk, but didn't.""",
407825: """p: I've been married 3 years
d: and who is this lucky girl?
p: my wife""",
407826: """Styu): if I open the internet, and the window immediately closes - is that a virus?
onic: actually that's a draft
onic: but how it's connected to the internet I don't know""",
407827: """K: all, bye! I'm off to the shower, call if anything
N: ok )
K: do you know my shower number?
K: don't call the faucet - the money ran out there!""",
407828: """xxx: and this morning we had a funny one: a couple days ago a developer drew a scheme on a piece of paper for a girl-tester who checks his creations (the scheme was called "this is how cool it should work").
This morning he ran in, silently grabbed that paper and ran back - the girl asked after him "What is it??" - the developer muttered over his shoulder: "I'll xerox it for myself, I no longer understand how it's supposed to work\"""",
407829: """From LJ.

replikator: In the evening took the shopping list, the wallet and went to the store for groceries. Also, since it's on the way, decided to throw out the microwave.
For an ordinary person, to buy groceries in a store three actions are needed: 1) walk from home to the store; 2) buy groceries and 3) return home. In my case, as always, everything went off-script. And no matter how I tried, there were more actions: 1) walk from the dumpster to the store; 2) grab groceries and stand in the checkout line; 3) carry all the groceries back to the shelves; 4) return to the dumpster; 5) find the thrown-out oven and open the door; 6) pull the wallet out of the oven, because when leaving home there was nowhere to put it, and my hands were busy with the oven; 7) walk to the store; 8) buy groceries and 9) return home.""",
407830: """Dialogue at a job interview:
- How's your Russian?
- Normal.
- Can you elaborate?
- Normal, yo.
- Thanks, clear.""",
407831: """Jony: So, how are you? All good? How do you feel? Do you even understand what you did yesterday?!!
Spring: Yes! I tried to drownd myself, but got scared of the jellyfish!. They're so scary!!! ='(""",
407832: """Sebastian Pereira: and I found sprats
Sebastian Pereira: it says - "sprats in oil"
Sebastian Pereira: lying bastards
Sebastian Pereira: in oil: the floor, shorts, plate, sink, rag""",
407833: """Dialogue of a husband (H) and wife (W):

H: Done all my work stuff, might as well go home.
W: Well go.
H: Nah, I'll play StarCraft at work, at home there's nothing to play.
W: Play the washing machine, or the vacuum cleaner.""",
407834: """x: hi
y: hey
x: seen Sanya?
y: yeah. yesterday
x: you don't know why his nose is broken? I asked but he waves it off %(
y: well that's how he greeted me yesterday
x: what do you mean? what did he say?? what did you do to him?
y: nah, in the literal sense greeted
y: wanted to shake my hand
y: while riding past on a bike......""",
407835: """xxx: My beloved says - "You don't love me". What to do?
yyy: do nothing. she'll get offended in the end anyway.""",
407836: """uuu: Once found in the city a big, beautifully designed stand with a historical reference about the city and district.
uuu: It was very pleasant to read the article that I myself had edited on Wikipedia =)""",
407837: """26.08.10
Sitting with a friend on a bench. Gloomy like. Sitting. Silence. Between us slowly, buzzing, flies a May beetle and leisurely circles near a tree.
Me: I wonder what he's doing now?
Friend: (sadly) Probably looking for May.(""",
407838: """xxx: how's the new dorm?
yyy: was pretty dirty. and I don't like that we have only one hole for two of us...
yyy: and the next dorm has 2 holes, soo unfair!
xxx: sunshine, what are you talking about?
yyy: well the hole where you stick the internet cable, what?""",
407839: """xxx: On the weekend I'll run for a second fridge )
yyy: Ran out of magnet space on the first one?""",
407840: """ice tea: from the physics point of view is a lightsaber a luminous body?
Siren: from the physics point of view a lightsaber appears only to mentally ill people""",
407841: """Skeggr: damn, I'll have to shave
Killer-XXX: and get a haircut
Skeggr: oh wait, never mind, the script worked...""",
407842: """xxx: they used to go out, now they go offline...""",
407843: """xxx: you're probably right about the psycho
xxx: had a case the other day
yyy: Which?
xxx: so I'm standing at the bus stop, waiting for the bus. remembered I need to buy some crap at the pharmacy, the pharmacy's at the stop. go in, ask, and they tell me I came in 5 minutes ago and already bought it. just forgot the change...""",
407844: """discussing Mafia 2's unfinished bits:

PriceDent: The cops are totally getting to me, there are way too many for such a town. You can't really settle into the game, that's the plot, and in general - you can't choose and make vital decisions. In the end you play the main hero who almost turns out to be a sucker, because everyone uses him.

Odin_iz_nih: Welcome to games that resemble reality.""",
407845: """xxx: this morning me and Tema noticed the rat cage is open, and they're sitting inside not giving a damn...
xxx: only now I got why they climbed out XD those bastards got cold at night, opened the cage, stole a towel and came back""",
407846: """xxx: they love me so much at the office!
xxx: today they bought me a laptop, a radio modem and data!!!
xxx: so I could work on vacation… :'(
xxx: bastards""",
407847: """XXX: You're staying at my place this weekend
YYY: only this weekend? I thought forever
XXX: What about the test drive?""",
407848: """P-Johann: Remember, you mentioned something about working in an online store
NoylTrase: Yes, I mentioned
P-Johann: Now give me details
NoylTrasе: Work in an online store. details in the office
P-Johann: And why can't you tell in more detail?
NoylTrasе: Because I can't write everything, and there's so much to tell...
P-Johann: Give me the short version
NoylTrase: Work in an onilne store
P-Johann: Fuck... They should take you into counterintelligence. As an American double agent.
- What did you learn about the Russian super-weapon?
- It's a Russian super-weapon""",
407849: """her: I love you, why would I respect you?""",
407850: """her: And what kind of girls do you like?
him: Non-smokers
him: Non-drinkers
her: And those are all the requirements? :)
him: Well, looks matter too, of course!
him: I think a girl should be gentle, kind. So she doesn't hit!
him: And she should also breed opossums!
him: She must have some zest to her...""",
407851: """xxx: They promised to switch both power plants to gas soon, and theoretically central hot water supply should get cheaper, but I strongly doubt it.

yyy: When did anything get cheaper in this country?)

xxx: Well, for example periodically our ruble gets a lot cheaper.""",
407852: """xxx: Heard a monologue at the beach: "sonny, put on your panama hat, you shouldn't walk barefoot on the sand\"""",
407853: """Alisa Li: Every night, exactly at 1:46, the fucking alarm clock starts ringing that half a year ago somehow fell behind the wardrobe, and nobody can get it out of there...""",
407854: """Eefrit: You know, I drink beer in the evenings only because just sitting and watching series/KVN and stuff is boring
Eefrit: With beer there's something to do - either go to the toilet, or get another one""",
407855: """ccc:
A normal day at a software company. 3 floors of assorted IT people working hard.
A guard runs into the openspace where 20 admins and 100 devs sit and shouts:
- Guys, does anyone know about computers?

ccc:
Turned out his CCTV camera program had minimized to the tray""",
407856: """Loko: I want to decorate the house in medieval Japanese style, with hi-tech elements
Glob: is that when a server rack stands in the middle of a sakura garden?""",
407857: """xxx: Give me the Elven manuscript
xxx: for the epidemic
yyy: don't have it
yyy: does it help?""",
407858: """xxx: if only apartments were like cats...
xxx: you take an apartment, and in a year it gives birth to 4-5 apartment-kittens from the neighbor's apartment""",
407859: """xxx: At an immodest hour, between evening and light
xxx: Without a mother, alone, half-dressed...
xxx: ...
xxx: ...
xxx: ...
xxx: I started setting up the internet!""",
407860: """A conversation about how to cook charlotte between girls.
x - Tanyusha, hi, tell me please the charlotte recipe how you cook it
y - a glass of sugar, a glass of flour and 3 eggs, slice the apples on the bottom of the pan
x - and how much milk?
y - we make it without milk
x - no but how much of it
y - how would I know, we make it without milk
x - and a glass is how much?
y - a glass is a glass
x - and what diameter should the eggs be
y - normal, normal chicken eggs
x - should the batter be runny or thick
y - runny
x - and how runny?
y - a bit thicker than for pancakes
x - and what diameter the pan?
y - how would I know what, any pan
x - aaah got it, and then cover with a lid and on open fire right?
y - how can you stew a pie, it needs to go in the oven
x - really??? and for how long?
y - for 25 minutes
x - aaaah, got it, and definitely in the oven?
y - well if you know how to stew pies, then please stew
x - Tanyusha, should I boil the apples before putting them in the pie?
y - WHY BOIL THEM?, raaaaw on the paaaaan
x - and definitely raw?
y - YES, YES, YEEEES
x - oh fine, I think I got it, thanks! oh, one more question.....and how much milk?
y - FUUUUUCK... Go buy some dumplings...""",
407861: """From a resume: "I possess a high intellectual"...""",
407862: """xxx: Remove the smile from the Mona Lisa and what do you get?
yyy: A Silent Hill poster ;)""",
407863: """xxx: I realized it wouldn't be easy with her when I saw in the bathroom 2 razors labeled "left" and "right"...
yyy: hopefully those are legs.""",
407864: """xxx: all normal people have 1 AM and 1 PM, but I have 1 in the evening and 1 in the morning..""",
407865: """Him: Wake up, sleepyhead.
Her: Mmm, I'm trying. But my eyes are closing.
Him: Yeah, "Careful, eyes are closing. Next stop half past one.\"""",
407866: """niceneasy: you don't understand, in your face I finally found a person who understands me!""",
407868: """wesst: remember how our students took their tests

wesst: such a lady came in, all dolled up, drew a ticket on computer hardware. and off she went - the computer consists of this, of that... and she got stuck on the CPU's structure. like the CPU consists of a core, cache, pipelines. so we, not being fools, brought her a "processor diagram" and asked her to point everything out on it. she pointed, explained, got a fail. little did she know that "processor" diagram was from a "Beryozka" black-and-white TV.""",
407869: """vimpire: ..want something sweet... and bloody.. ;)
yourlady: hematogen? x)""",
407870: """Masiakasian: Are we seeing off the summer today?
KatreiNa: Get lost, Masya, with your summer!
KatreiNa: If I keep seeing things off every day, they'll quickly see me off from work""",
407871: """xxx: The SKP RF denied reports that during the robbery of the building of the department for especially important cases of the investigative directorate for the Moscow region important documentation went missing. Only a TV and a video camera were stolen. At the same time the SKP confirmed that three unknown masked men cracked 18 offices and 25 safes.

xxx: Cracking 25 safes and carrying off a TV - that's very plausible :)""",
407872: """agk: finally bought cigarettes with threatening inscriptions
agk: can note it has the reverse effect
agk: bought 4 packs, all 4 inscriptions different
agk: wanna buy more, to learn what else they'll write""",
407873: """Lensky: My brother watched Sailor Moon as a kid.
catsmile: Is your brother gay now?
Lensky: My brother's a sailor now!""",
407874: """Fris: my dog makes strange clicking sounds
%username%: the HDD's old""",
}
