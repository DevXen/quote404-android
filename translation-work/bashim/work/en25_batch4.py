#!/usr/bin/env python3
"""Translate bashim chunk_25 batch 4 (input lines 301-400, ids 407374-407473)."""
import json

EN = {
407374: """*a friend recently got married and said she's pregnant*
Me: ohh, so nice to fuss with the little one
Me: or the little girl
Her: exactly!!!!
Her: sleepless nights...
Her: headaches...
Her: dirty diapers...
Her: cooking..
Her: cleaning..
Her: drool...
Her: snot...
Her: and that's just the husband...""",
407375: """Lizochek: I'm outraged! and Irka decided to marry Sasha. What the hell! No, can you imagine?! IRA! For Sasha?! Is it just me who finds this funny?!
AdolFIK: Who are all these people?
Lizochek: You don't know them, forget it.""",
407376: """AAA: Every day the cat sleeps on the ironing board.
AAA: is he like, hinting at something?""",
407377: """<Homeboy> any Jabber specialists here?
<VzlomRZN> Homeboy: what exactly do you need?
<wiz> to strangle someone?""",
407378: """The company's internal mail gets a mailing from the caretaker:

Good morning everyone. Dear colleagues, lately I've gotten bored coming to the office alone in the evenings to turn off the lights and close the windows in the office. Starting today I'll be inviting department heads for company, we'll do it together. Not so boring.""",
407379: """xxx: To solve the chicken-and-egg paradox we need to introduce a facehugger stage for chickens...""",
407380: """yyy: that's it! I've done all the quests available to me in WoW, and almost finished the "Insane" achievement!
xxx: I was thinking... with your nerdiness you can't go ice fishing at all. you'll freeze your ass off %)""",
407381: """xxx: I have a programmer friend who, while renovating the dacha and painting the windowsill, explained to his little boy with feeling the purpose of drying oil.
xxx: "It's like a driver for wood paint\"""",
407382: """Anika: My biggest shame happened when I, still a teenager not telling my parents about my personal life, went as a pure angel with white bows with my father to the pharmacy (getting ready for vacation)
Anika: And the kind saleswoman, spotting a regular customer there, smilingly asked: "The usual, a big pack of thin condoms?".
Anika: Now Kirill is my husband, but back then it was embarrassing.""",
407383: """From LJ:
I have an ass.
no, I suspected it before, but now I know for sure - I have one.
and anyway, horsies are fun.""",
407384: """You could say I go hiking for a few wonderful moments:
1. To feel your feet are dry, won't get wet anymore and there's nowhere to go
2. To die of pleasure in a hot bath
3. To fall asleep on a soft bed, on clean sheets, with a normal pillow.""",
407385: """xxx: congrats to me. I finally nuked that Vista
yyy: And what did you install?
yyy: By the way, "finally" is written as one word.
yyy: Or are you cursing so elegantly?""",
407386: """Husband yesterday.
- Woman, - he said tiredly, - long hair, short wit...
Then he thoughtfully looked at his ponytail down to his shoulder blades and fell silent.""",
407387: """xxx: The priest had a dog...
xxx: And the priest's wife had a cat.
xxx: But it got off with a light scare and didn't fall into recursion.""",
407388: """Bagi: Sashka went away on business and asked not to look for a gift. Isn't he a bastard?
Bagi: The worst thing is the cat already found the gift under the bed...
Bagi: temptingly and loudly chewing the wraaaapping paaapeeer ((( """,
407389: """XXX: How's Igor's Jeep?
YYY: Fine. Likes it. Says he managed to drive it into such backwaters that in the evening he got robbed by a hedgehog family who swiped his sausages lying by the campfire.
YYY: So, probably a good jeep. I've never been to such backwaters on foot or by boat""",
407390: """XXX: 4 thousand left of my salary
XXX: of that I need to pay 5 for tuition and live on something.""",
407391: """her: In a wrinkled t-shirt again? What's with the habit of walking around like a pig?
him (thoughtfully): Probably my German roots...""",
407392: """Cox: My family is riddled with double standards
Cox: Didn't pass the exam on the 3rd try - happens to anyone, but if you didn't wash your hands before eating - they'll grind you into shit..""",
407393: """<ocaml> took up learning functional programming - the acne disappeared from my face... though soon after that gray hair, a family and two kids appeared.""",
407394: """xxx: yesterday my nephew asked what I played as a kid.
xxx: answered "cops-and-robbers"
xxx. the kid asks "where can I download it? Actually never mind - the graphics probably suck...\"""",
407395: """Questions to a sexologist:
06/07/2010
Hello. I'm 22. I have a girlfriend, we've been together four months, we have this problem <...>

07/17/2010
While waiting for your answer, we broke up.
You take a long time to respond.""",
407396: """StingeRX: My boss woke me up today
StingeRX: calls me all business-like at 8:35 with the question "where are you?"
StingeRX: to which I answered without hesitation "Driving"
StingeRX: But he viciously cut me down with the phrase "Go on, honk!"
StingeRX: And I don't have a horn in my bed =(
StingeRX: Need to buy a horn for the bed""",
407397: """<xxx> I figured out how to wake him. you need to turn off the computer!
<xxx> a minute later he wakes up and runs in panic to see what happened
<xxx> but no alarm clock helps...""",
407398: """B: do you just have questions on biology?
C: on a flash drive
C: but it's at Yasya's
B: and Yasya's in the duck
B: the duck's in the chest
B: the chest is in the ass
B: and I'm in the ass too
B: only I can't see the chest""",
407399: """xxx: Today in the canteen the menu had "bird cutlets". I took some.
xxx: While standing in line with colleagues, suggestions were voiced that the bird was a pigeon, a crow or an old pterodactyl.
xxx: But when I was eating that cutlet, I realized: the bird was the rubber duck from the bathtub. The cutlet fully preserved its taste and texture.""",
407400: """new'i'dimka: that's on the servers
new'i'dimka: who'd let them in there, they'll start poking, they can't just point a finger, they have to poke
new'i'dimka: my CEO after a system restore due to a half-erased file system, to the question what the hell he launched at 9:58 boldly climbed into some folder and launched a program with the words "This one!"
new'i'dimka: and the bastard added "it asked me something, I said YES"
Kalinnikov: curious
new'i'dimka: yeah, and mostly about me""",
407401: """xxx: Our supply guy bought 10 huge boxes of paper towels instead of paper....
xxx: He kept poking his finger at Paper Towel shouting "it says right here - papir tuvaletny\"""",
407402: """xxx: And then she stripped almost naked (=
yyy: whaddaya mean almost?
xxx: she didn't take off the chain and the ring...""",
407403: """<Silver_metal> the main rule when playing darts - don't miss a meter to the right and a meter and a half to the left
<Silver_metal> because on the right is the corporation's main commutation rack, and on the left sits the head of the IT department""",
407404: """xxx: did your ICQ get scared seeing my file size?
yyy: it's like an opossum. plays dead right away at approaching danger.""",
407405: """H7: Visitors of the "Rocket" ride in St. Petersburg got stuck at 70 meters height
redplait: visitors of the "Carriage" ride are still standing in traffic on Leningradka""",
407406: """Standing in line, my wife calls and asks:
- are you there yet?
- yes, just now.
- big line?
- 5-7 people.
- what number are you?
- sixth to eighth...""",
407407: """xxx: Wow! Who's online so late! Why aren't you sleeping?
yyy: I can't sleep, mosquitoes are bugging me, buzzing over my ear.
xxx: Play some relaxing CD.
yyy: You think they'll fall asleep?""",
407408: """xxx: and instead of a heart a plasma engine
xxx: *flaming""",
407409: """xxx: I need to catch you at work somehow. And arrange some flash mob. you work at the checkout, right?
yyy: Yeah. What are you planning?
xxx: Well... Say, zombies go to the checkout. 30 zomb-people will pass, and each will ask you at the checkout braaains...
yyy: So I have to explain 30 times that I don't have any?
yyy: oops...""",
407410: """xxx: In the morning paper I came across the horoscope: "...The day can safely be called the best in half a year, today you'll get what you've long lacked...."
xxx: Having made simple deductions I, being a reasonable person, decided to meet such a glorious day fully armed and bought condoms.
xxx: I should've bought an umbrella.""",
407411: """<NinerLP> You know you're a good programmer when you get enough sleep instead of fixing bugs all night.""",
407412: """yyy: Yeah, sometimes you can talk to him normally, but say he's a sheep and the insults and offenses begin right away.""",
407413: """A guard in the chat

ANARKHO: Hmm, some lady in glasses has been knocking on the transformer booth door for like five minutes for some reason.
ANARKHO: And I'm too lazy to get up to say the office entrance is five meters further.
ANARKHO: I guess I'm a bad guard......""",
407414: """Jenka)_Fight4Fun: Theban Sacred Band. An ancient Greek combat unit consisting of 150 homosexual couples. According to the theory of its creator Gorgidas, men "not indifferent to each other" would fight to the end and wouldn't flee the battlefield abandoning their comrades. Completely destroyed by young Alexander the Great in 338 BC.
mimicRy^_-: Alexander also banned gay pride parades""",
407415: """Mihuil: heh, and my body temperature is room temperature.
Mihuil: I mean, in the office - 36.5""",
407416: """a conversation about stories that happened to us in distant childhood:

DeFactor: ha! None of you have a thirst for science!
I stuck a hairpin in the socket with a very specific goaal — to pick out electrons.""",
407417: """Ane4kA: going to buy kefir
Lena: make sure it's not open, yesterday I bought one and it was open and half-drunk
Ane4kA: didn't you feel by the weight that it was half lighter?
Lena: I thought I'd gotten stronger""",
407418: """xxx: and anyway parents should be obeyed
yyy: lost your pocket money again?""",
407419: """Two guys are walking, dragging water canisters. One to the other:
- So, Vanya, do you now get that the drought in Modoc is no fucking joke? It's fucked!""",
407420: """xxx: and how are the excavations?
yyy: found mines, cartridges, I swiped myself a bayonet!
xxx: so you walked around with the swiped bayonet?""",
407421: """mashin: I think you shouldn't listen to my advice
mashin: in my opinion a genius phrase""",
407422: """Oleg: panic at the bank - they officially banned playing call of duty at lunch
Oleg: because one player punched another in the face for trash-talking him in the game =)""",
407423: """From LJ:

anamaliya: Well what, for a long time I was sure my car was called Peugeot Airbag. It's written on the steering wheel.""",
407424: """1: Good evening all community readers. Maybe this topic has been raised before, then I apologize.
Question for those into role-playing games: in what image do you prefer to see your girlfriend or boyfriend?
2: Usually an orc hunter, but if we're short on tanks I'd love to see him as a bear druid.""",
407425: """my wife works at the employment center. told me the other day. the work phone rings, she picks up, and there's a female voice:
- hello, is this the employment center?
- yes
- please open the bathroom door, the lock jammed from inside...""",
407426: """xxx: neighbors, renovation!
xxx: how cute, when I removed all the profanity from this sentence it shrank from 10 to 2 words""",
407427: """xxx: torrents are essentially theft. So soon there should appear torrent-shanson, in the spirit of:
I wanted to buy a disk about "Avatar",
But mom didn't give me money for the disk,
And then the crooked path-link
Suddenly led me to a torrent tracker
:-D""",
407428: """him: whatever, soon I'll buy myself a car.
her: oh, I also want a car for myself, a red one with body kits.
him: with body kits?
her: yeah, deck it out with all kinds of bunnies, bears.""",
407429: """Aggressor: So, what language did you study at school?
Linguaman: Pascal :(""",
407430: """Mikhailo Korleonenko:
My problem is that I don't liken myself to the Changshan snake:
"Therefore, one who wages war well is like the Shuaijan. The Shuaijan is the Changshan snake. When it's struck on the head, it strikes with its tail, when it's struck on the tail, it strikes with its head; when it's struck in the middle, it strikes with both head and tail."
Mikhailo Korleonenko:
I liken myself to a log. A log is a log, when a log is beaten on the log - it lies, when the log isn't beaten - it lies. A log wages war poorly. I am a log.""",
407431: """Vadim: everyone's discussing what original and preferably useful thing to gift a girl for March 8

Vadim: told them how my dad gifted my mom last March 8 a bathroom door. both useful and quite original actually. and mom's pleasantly surprised eyes did take place when dad joyfully shoved a DOOR with a bow into her hands in the morning...""",
407432: """zombiracer: I used to think that to force myself to catch up on my backlogs, it's enough to go to a remote village with a laptop full of study materials, without taking a mouse.
zombiracer: Today I beat my colleagues at Quake on a touchpad for a bet.""",
407433: """Sirion: bad when the laptop repair instructions are stored on the laptop""",
407434: """Another_Insane: look, it would seem, a blade of grass. And yet it breaks through asphalt! Can you break through asphalt? No. But of course you can pick the blade of grass. But that doesn't mean you're stronger than the blade of grass or the asphalt. It only means you're a dumbass who picked a blade of grass...)""",
407435: """xxx: Tried to get a job as a courier, proudly told the employer about my "excellent knowledge of Moscow", arranged an interview.
xxx: Epic fail crept up unnoticed: having arrived, I got lost coming out of the metro, and never found the employer.""",
407436: """Dad teaches me how to properly cut cucumbers:

Dad: This is how, son, cucumbers are cut.

Then he takes the cutting board with cucumbers and with a calm face sweeps them into the trash bin. He stood in a stupor for ten seconds trying to figure out what he'd done wrong.""",
407437: """Tatiana: took screenshots of gismeteo - to look at in winter, to warm up...""",
407438: """Oleg: well fuck you, some friend you are (
Oleg: I won't come to your funeral :-/
Wyrd: And I will come to yours ;-)""",
407439: """xxx: did I get it right, one person = one backpack
yyy: Without an explicit type conversion that assignment won't fly.""",
407440: """xxx: listen, I wanted to say yesterday: sorry please that I didn't invite you to my birthday. don't want to make excuses, just sorry.
yyy: you had a birthday?""",
407441: """XXX: my wife is going on vacation
YYY: and where are the kids going?
XXX: with her.
XXX: so the vacation is actually mine.""",
407442: """Kroshik: And where are you gonna bite me?
Milante: you'll see )
Kroshik&#8206;: Rrr...Well at least tell me the region
Milante: Chelyabinsk region""",
407443: """xxx: Every effort must be made to preserve the propiska institution. It's one of the few things reminding us of our glorious tradition - serfdom.""",
407444: """<_Al> one user is slowly downloading "Twilight" from me. From Anna German's album "Last Meeting". Maybe at least his mom will be happy""",
407445: """I work at a web studio. My friend calls:
F: where are you?
Me: at work
F: and why is it so quiet at your web studio all the time? no work noise.
Me: the site-building machines are in another room...""",
407446: """<xxx> "let me invite you for some keks?"
<@yyy> no keks before marriage!
<zzz> right - why the extra kilos beforehand""",
407447: """111: Yurik will definitely kill me today!
222: Forgot the tow cable?
111: took it, and even stretched it to the avenue
111: at the intersection I started off sharply, the cable snapped. just enough for him to roll to the middle of the intersection.
111: he called just now, cursing. listened to curses directed at him for 4 hours in the middle of the intersection...
222: and why didn't you go back for him?
111: I got home and thought: what the hell did I go downtown for? and went to sleep""",
407448: """at work.

Lyudmila: croc, do you lu me?
Crocodaill: can I not lu you?
Lyudmila: Croca, then will you do a good deed?
Crocodaill: strip
Lyudmila: damn, Dima...""",
407449: """Lippy: Oleg, did you know I starred in porn?
IIIyT: No
Lippy: Neither did I...""",
407450: """N: did I tell you about calling a husband-for-an-hour to fix my doorbell?
P: sounds like the start of a porn video""",
407451: """xxx: Funny state we have. Imagine, for my wife to get the benefit for the kid, I need to go to social security at my registration address and get a certificate that I didn't receive the benefit. Then she'll take it to social security at her registration address and get it there. Can't they check themselves???
yyy: Well if they don't check, get two benefits.
xxx: Make a fake certificate or what?
yyy: Why? First you get the certificate at your social security, then at hers, then at yours you say you changed your mind about getting it there and want to get it here. The wife does the same.""",
407452: """xxx:
I work at an auto repair center. My brother asked me to drive his car in to wash (our employee wash costs 100r) well, decided to oblige, in the morning I drove over in his car.

xxx:
wrote an entry application, took it to the director, he signed it, went to the cashier to pay, hand over a thousand, no change, went to accounting, no change, ran around 10 people until I found someone to break it, broke it, went back to accounting, got the stamp on the application, went with it to security to get an entry pass, got it, walking to my work spot, a guy from security calls "no car is registered to you, they won't let you in, go to the head of security", went, "write an application to register the car as personal transport", wrote it, took it to the director, he signed it, original to accounting for the stamp, copy to the head of security, and only now, fuck, can I drive in to WASH THE CAR.

xxx:
Don't like RPGs anymore...""",
407453: """xxx: So he's married??? Daaamn(((
xxx: Although, you know, I've long noticed there's something about him...
yyy: Yeah, a wedding ring""",
407454: """XXX: they're bringing me a kitten now. tell me, what to name it?
XXX: it's black and white
YYY: horizon )""",
407455: """xxx: Who's a freak of nature, are you a freak of nature?
xxx: I'm wrapped in a blanket because I'm cold. And I'm cold because I'm sitting naked because of the heat.
xxx: And also my surname is fucking Domoboev, thanks to that shitty passport lady. Thanks it's at least without a space.""",
407456: """<Рричи> the savior will come
<Hobober> ...and plug the oil well in the Gulf of Mexico ;)
<Hobober> preferably with BP top managers who failed to ensure deep drilling safety""",
407457: """Morning. Girl (G), Guy (B).
G: What should I cook for breakfast? I have yogurt, cottage cheese, can do muesli with milk?
B: Yogurt. Cottage cheese. Muesli. And something to EAT!?""",
407458: """xxx: Accidentally saw a kit for marking things - invisible marker + UV flashlight to see what's written. Now on my forehead invisibly flaunts "I am Alexey."
xxx: Bought it, as they say, on my own head.""",
407459: """xxx: I overhear a client, pointing at me, asking my employee something, to which she answers: "her? no. she doesn't work here. she's the director here!\"""",
407460: """<kotik> damn
<kotik> seen all kinds of things in this life
<kotik> but a drunk company of youths at 5 am listening to Pugacheva - that's beyond good and evil""",
407461: """xxx:
Question in a quiz: "What is the standard weight of a skittle?"
Options: 1)0.5m ; 2)1.5m ; 3)2.5 m

xxx:
Don't even know what to answer...""",
407462: """xxx: Today I saw our sysadmin in his "free from work time" - a bearded man, in his right hand a C++ book, in his left a can of beer, standing perfectly straight, like a soldier on parade, and now add roller skates to his feet - and speed him up to about 50 km/h, while keeping the perfectly straight body position (legs don't move) and eyes only on the book...
xxx: managed to photograph, everyone in the office renamed him Fumes on the wings of the night""",
407463: """Dialogue in a barbershop between a barber (B) and a grandpa (G) with bushy brows:
B: Let me trim your brows.
G: No!
B: Come on! they're climbing into your eyes, blocking your view.
G: WHAT, DO YOU WANT ME TO FORGET BREZHNEV?!?!?!""",
407464: """111: And then I'll go to the army
222: when?
111: Don't know, I'm joining the space marines
222: I asked not where, but when
111: In the next 50 years Russia will have its own space fleet to transport space marines?""",
407465: """xxx: why do you need a gun?
yyy: What do you mean, go hunting in spring, drink vodka, what else do people buy guns for? Not to shoot ducks, after all...""",
407466: """xxx: Should you trust a beardless person on programming language matters? ;)""",
407467: """xxx: and I went fishing with the guys))
yyy: did you ruin their fishing or decorate their drinking?
xxx: I arranged a group session for them :)
xxx: tangled all the tackle, they spent half a day screwing with it!""",
407468: """And my husband is also weird.
I, for example, write to him on ICQ: "Listen, move the paragraph about the shooting method on the site to another place and make the letters bold there".
He writes: "Didn't get it, once more".
I copy the previous message: "Listen, move the paragraph about the shooting method on the site to another place and make the letters bold there".
He writes: "Aa, got it, will do".""",
407469: """mom: I found a slimming belt here, only it doesn't fasten on me...""",
407470: """xxx: StarCraft 2 comes out tomorrow =)
yyy: I'm not a fan
xxx: anyway, it's a momentous event! it's like, say, Minesweeper 2 came out""",
407471: """Overslept for work, woken by the boss's (b) call
me: hello.
b: where are you?
me: well I'm... had to urgently...
b: lie faster!!
me: wait, don't distract me.""",
407472: """kOkO: Know the expression: "like an elephant in a china shop"?
kOkO: Well our cleaning lady's surname is Mamontenko""",
407473: """Sestra Pushkin: Why hasn't anyone invented a mirror in a black frame with magnetic letters yet, so you could stick demotivators on it in the mornings?""",
}
