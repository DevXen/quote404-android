#!/usr/bin/env python3
"""Translate bashim chunk_25 batch 10 (input lines 901-1000, ids 407975-408074)."""
import json

EN = {
407975: """xxx: Celentano's a cool guy. Pity he got old.
yyy: Well that didn't really depend on him much.""",
407976: """xxx: How do I cook a quail? hi)
yyy: hi. how many do you have?
xxx: one =D
yyy: give it to the cat""",
407977: """Night. Stayed up late at the comp. Decided to go smoke on the little balcony. Across the road three clearly drunk comrades of punky appearance are trudging along the sidewalk. And yelling a song. But which one and how!
The apple and pear trees were blooming,
Mists floated over the river. HOY!!!
Katyusha came out onto the shore,
Onto the high steep shore. HOY!!!

The cigarette flew down straight out of my mouth.""",
407978: """xxx: Knew one manager, he wrote d-link as d'Link. Like Count d'Link.
yyy: d'Link and the three musketeers - athos, porthos and asus.""",
407979: """From a forum:

X: We use condoms by the calendar method. Knock on wood, no misfires.

Y: and I no longer believe in the calendar method
seems like a 100%-safe day, but there's one-year-old Vovka running around - proof there are no safe days :)

Z: Yeah, and besides the calendar method, I don't believe in "Pharmatex", condoms and IUDs. For the same reason. ;) Only three proofs are running around :)""",
407980: """Discussing a video shot with a camera rotated 90 degrees.
xxx: in KMPlayer you can flip it while watching.
yyy: Yeah right, messing around. I flipped the monitor.""",
407981: """Going into the dean's (D) office to xerox a document:

Me: Hello, can I use the xerox?
D: Yes, only it needs to be turned on, you know how?
Me: Of course.
*30 seconds poking at buttons*
D(grinning): Start with the sokcet ;)""",
407982: """A friend tells about retaking an exam:

Jake: well I drew him the graph of the function fucking-off
BMX: maybe of decreasing? or was it decreasing really fast?""",
407983: """xxx: how are you?
UUU: like live beer - not moving, but blowing bubbles""",
407984: """neyron: I overplayed Baldur's Gate
neyron: lying asleep next to my girlfriend, she's got a cold
neyron: every half hour in my sleep "at the bottom of the screen" the message appears "saving throw vs cold successful"
neyron: when the alarm went off I saw the message that the saving throw failed. And indeed, got sick a couple days later )""",
407985: """About the Microsoft certificate: Microsoft .NET Framework - Application Development Foundation

Dorogusha: the most important thing is it's valid worldwide
Dorogusha: you can get a job with it in Manhattan
Dorogusha: and share secrets with Demi Moore
Dorogusha: and he's just a C# programmer, yo!""",
407986: """xxx: Well I couldn't do that
xxx: I'm not the last scumbag
yyy: Of course not, we're not an endangered species, there are still a lot of us ;)""",
407987: """him: where'd you disappear to?
her: went to dig potatoes at my parents'
him: what dragged you there?
her: I have a light hand, planting - I come, weeding - I come, so went to dig too. Only the potatoes are small, need fertilizer.
him: going again? 8))""",
407988: """XXX: yeah, we talked long and productively today, but you didn't reach my heart brother!!
YYY: I'm knocking on another door, called "brain", but apparently nobody's home""",
407989: """D - tram driver, S - route dispatcher.
D: OSA-1, answer 364.
S: listening, 364
D: I'm at the Belinsky stop toward Kuybyshev-Lunacharsky, I have a suspicious package in the cabin
S: 364, stay in place, await the sappers. Attention drivers, traffic on Kuybyshev from the Circus junction to Kuybyshev-Lunacharsky is closed in both directions, awaiting the militia.
... literally a minute later
D: OSA-1, this is 364, open traffic on Kuybyshev, we're moving.
S: Were the sappers there?
D: No, nobody was, the passengers threw the package out.
S: How threw it out? And what was in the package?
D: I don't know, they just threw it on the road.""",
407990: """RT: Looking for a person at #devpoint with a wheelchair who will stand up and leave during the IE9 talk""",
407991: """Deirz: yesterday Vova celebrated his b-day
Deirz: we (me, Andrey, Tyoma) got original
Deirz: room, all guests gathered, table set
Deirz: Tyoma, Andrey walk in and say
Deirz: Vova! Congratulations!
Deirz: we wish you...
Deirz: to steal...
Deirz: to kill...
Deirz: and then I burst in, screaming "Fuck geese!", handing him a goose
Deirz: alive, with a pink bow on its neck""",
407992: """Jee: Now I hear from the kitchen: "Max, control yourself! Max, put the chair back! Max, why do you need the knife?!! Seryozha, spit out the cat!!"
Jee: Max is our cat... Seryozha is my father... In the kitchen, besides them - nobody...""",
407993: """xxx: I have beautiful lingerie. Black fishnet stockings...
xxx: Waiting for someone
yyy: Santa Claus. hang them over the fireplace for New Year""",
407994: """xxx: went to a budget barbershop, say: cut 3 centimeters, no more, short haircuts don't suit me
xxx: I was watching TV meanwhile
xxx: when she finished cutting, says: and you say short ones don't suit you
xxx: the rest can't be put into words""",
407995: """xxx: Did you hear hair falls out from cell phones?
yyy: And did you hear it can also cause a brain tumor?
xxx: Well at least the brain isn't visible..""",
407996: """Call to tech support:
- I understand, of course, that you disconnected the internet for non-payment, but what did Skype do?""",
407997: """— got an old unneeded player?
— yeah, vlc 0.9""",
407998: """Tema: The presidium of the United Russia general council will meet with iPads. Actually it could be not only iPad, but everyone bought iPads.

xxx: Under Yeltsin they bought tennis rackets, now - iPads.
Russians, elect a sadomasochist as president already, we'll laugh.""",
407999: """syslik: and I was fully convinced Leshka loves me very much when, before bringing me home for the first time, he cleaned the keyboard. love and care :D""",
408000: """xxx: and actually he's a very kind person
xxx: even in Carmageddon he drives around the little people""",
408001: """Anonymous: does it support Russian?
pavlinux: Dа, vse normalno!""",
408002: """Programming school. The teacher bought a coffee maker and kept bragging it's got a processor.
Standing, drinking tea, discussing:
- They made a kettle with a processor, a fridge with a camera and internet access. soon there'll apparently be new programming courses...
- Yeah. Programming for dummies.""",
408003: """In the age of piracy real sea wolves calmly walked the ship's deck, even in the strongest storm, even tipsy, even with a wooden leg. But they still can't hold a candle to tram conductors.""",
408004: """her: when are we going to pick wedding rings?
him: isn't it a bit early? Maybe I'll change my mind :)
her: well you'll melt yourself a gold tooth.
him: what tooth?
her: the one I'll knock out!""",
408005: """tratata: saw in the store today win 7 ultimate for 11,000 rubles
tratata: thought of taking it, but was 10 grand short""",
408006: """From a forum "Why do people go work in a morgue?"
- One acquaintance of mine worked in a morgue, simply because you could rehearse there for free at night (he was a drummer in a punk band)""",
408007: """xxx: Tomorrow I'm going to pick a car, thinking who to take along.
yyy: Take Max
xxx: Why him?
yyy: Well we sent him for beer the day before yesterday, and he brought two bottles of vodka
yyy: So maybe you'll go pick a car with him, and come back with a yacht and a helicopter )))""",
408008: """Quiet Spectator: These days the concept of "working on yourself" comes down to editing your personal info on VKontakte""",
408009: """xxx: they found a mammoth in the neighboring city
xxx: people first called the militia to the find, but it found no crime in the mammoth's death""",
408010: """zlodey_tula: optimization miracles are when they poke wherever they want, and hit where they need!""",
408011: """xxx: searched "bugle lights-out" found what I needed
xxx: the neighbors will freak out when I go to bed""",
408012: """from a programmers' forum:

ran into a VS2005 bug — after the 128th nested if-else-if the conditions are simply ignored. Had to redo it as switch-case""",
408013: """XXX. And in the end - forgot my bag with passport and wallet at the club.
XXX. Now wondering what I won't find in the bag.
YYY. Probably they'll steal the algebra notebook after all.""",
408014: """- honey, run for bread.
- can't, dear, configuring the kernel.
- you go go, I'll finish..

pff, idyll, damn""",
408015: """Oduvan: just finished watching. cried so much. my whole face is swollen and eyes red
Matrix God: allergic to the movie?""",
408016: """xxx: What stupid vows you make under an IV drip!
yyy: promised never to bomb Iraq? And now can't pass the mission in the game?""",
408017: """Konstantin: I found myself a job for 20,000 rubles.
Irina: Buy it""",
408018: """doctor64 > nothing can fly on a netbook by definition
Stalker > it can. My son's Win XP crashed. Need to reinstall""",
408019: """Xxx: I think the idea that greens are healthy was invented by those who like them. To force those who can't stand them to eat them.""",
408020: """Prof: Passengers of the Moscow-Odessa train realized something went wrong when Steven Seagal walked through the car with a tense face.
Gulka: And how do you tell from Steven Seagal's face that it's tense?
kobold: with a voltmeter of course""",
408021: """Skif: I'm probably a shitty admin if only by the end of the accounting girl's monologue, which started with the words "All my forms suddenly changed", I realized she was talking about 1C...""",
408022: """WoW chat:
xxx: people, show me please the rogues' trainer
yyy: the wife's lover?""",
408023: """Aleksey: I'll go to bed too, I think. cuddle up to something soft and warm
Aleksey: I had a hard day. First my wife turned on the light and the chandelier burned out, then decided to defrost the fridge and it didn't turn on after that, then decided to reheat lunch and dropped the pot. Basically I've been guilty all day )""",
408024: """Ksyusha: Let's do virtual sex )
Leman Russ: Let's!
*Ksyusha slowly pulls off her blouse. She's warm and smells of pheromones
*Leman Russ with a cast-iron clang puts his left pauldron on the table and starts slowly removing his power armor. It's all in bullet holes and smells of gunpowder
Ksyusha: Go to hell, you damn space marine!""",
408025: """xxx: When talking to Misha, you need to choose your words carefully. In WoW he drove the "Brothers of Steel" guild to collapse with one innocent question "By whom?".""",
408026: """X: hi, I have a dilema
Y: *dilemma
X: there's a woman but I don't wanna have sex
Y: *there's a woman, but I don't want her
X: thanks for the consultation, finally figured myself out""",
408027: """KvaNTy: "Invigorating!!" is when you're sick with a cold, with a sore throat, you find a bottle of cough syrup in the cupboard, pour it in a spoon, drink it and a quarter of a second later realize it was pure alcohol""",
408028: """xxx: Sanya - can I ask a question?
yyy: of course, eat as much as you want...""",
408029: """in a game chat:
aaa: idiot
xxx: you talkin' to me?
yyy: you talkin' to me?
zzz: who, me?
aaa: you just watched the skit "children's self-esteem\"""",
408030: """xxx: Arranged with the nearest Sberkassa to exchange Lekha's small change for money in exchange for a box of chocolates. Today we went together.

xxx: A girl with a notepad coming toward us. Sociological survey: do you use a bank card, have you taken a bank loan, do you have savings in a bank...

xxx: So Lekha showed her - two liter jars of small change. Never in my life was it so hard not to laugh.""",
408031: """ВеСеJ|b||/|: so if you had no comp and no TV right now - what would you do? )
SERGant: I'd be in my 5th year at uni right now...""",
408032: """xxx: MOM!!!
xxx: damn talking ad
xxx: sitting, browsing some chick's page
xxx: and then a male voice
xxx: "Hi! Want to earn big money fast and effectively?"
xxx: I actually answered like an idiot ))""",
408033: """- Let's talk?
- Listen, let me step away a bit after work...
- Step away far?
- Well, about fifty meters...""",
408034: """aaa: my net was broken
aaa: the shitty node broke it
bbb: and what did the node do to the net?..
aaa: killed the connection and forbade touching the corpse""",
408035: """girl: I have a new boyfriend. He teaches at some technical institute.
girl: But don't think he's just a teacher, he also works.""",
408036: """- A colleague brought a book about Buryat traditions and fairy tales to the office. Started reading the page with name meanings: Erdem - "knowledge", Seseg - "flower", Bolod - "enough".

- Apparently, by tradition the fifteenth child in the family was named Bolod.""",
408037: """xxx: Is there some mystery you've long wanted to know? )

yyy: Mm... Yeah, I guess. 7 years ago in the men's toilet of the Bauman main building in one of the stalls there hung a warning from the cleaning lady - "Don't stand on the toilet with your feet")

yyy: I've already finished my studies, but in all these years never figured out what one could do there in that position...""",
408038: """xxx: Our leaders are the most honest in the world, not a single official has been punished, which testifies to the absence of corruption in the country in principle.""",
408039: """xxx: Why do you need your right hand? You're left-handed.
yyy: Well, actually I wash dishes with my right hand.
xxx: Aha! So I am your right hand.""",
408040: """him: Sweetie, if you buy a camera for a grand, we'll have to break up
her: why?
him: because you'll become ugly and I won't be able to live with you
her: why would I suddenly become ugly?
him: because your MUG WILL CRACK FROM A CAMERA FOR A GRAND!""",
408041: """xxx: Yeeah... life is a strange thing
xxx: Just yesterday he was the asshole who can't park and tore off your side mirror, and today he's your probability theory prof""",
408042: """xxx: why are you so pissed they call you computer guys?
yyy: What's your profession?
xxx: well accountant
yyy: A-a-a-a, moneycounter?
xxx: got it, you could've just said "like a sickle to the balls" - I'd have understood :)""",
408043: """The little one's got colic. 4th hour of the night. The husband, shuffling his feet, walks around the room with the little one in his arms and sings in a sleepy voice:

The birds went quiet in the pooond
The fish fell asleep in the gaaarden

I feel it's time to take over.""",
408044: """About 20 kilometers from Moscow along Yaroslavka there's a sign that explains EVERYTHING happening in Russia.
There are two arrows: Moscow - to the left, Truth - to the right...""",
408045: """Gnome without a tractor: took a new comp to the lab, transferred all data from the old one
Gnome without a tractor: 10 min later they call screaming their data is gone
Gnome without a tractor: I come, look
Gnome without a tractor: they open a pdf
Gnome without a tractor: on first launch it offers to accept the license agreement
Gnome without a tractor: they read it and scream in horror that this isn't their data""",
408046: """xxx: I know a story about a four-meter catfish..that lived somewhere on the Volga! It swallowed a little girl..and then her mom! The girl's father later caught it and ripped it open! Inside were his wife and daughter...

yyy: ...and two dozen ships
then they all came out and lived happily ever after""",
408047: """XXX:
I once worked on a hotline for wellness products, there was a case that showed there are people - walking jokes. so a guy and a woman call, saying:
- "we bought your tea, but it has no color or smell! we don't know what to do. did we buy a fake?".
for about 5 minutes I'm figuring out what's wrong with the tea, why they dip the bag in boiling water and nothing happens.
then it hit me...asked the guy to rustle the bag into the phone, he rustled. I tell him:
- "take the wrapper off the bag and brew it without the wrapper".
dead silence and mooing into the phone "aaaah....that's what it is".""",
408048: """xxx: So if a huge intergalactic helicopter is flying
xxx: And a speck of dust is flying toward it
xxx: Relative to the dust speck the dust speck is at rest, and the helicopter is flying. Relative to the helicopter the dust speck is flying, and the helicopter is at rest
yyy: relative to the helicopter the helicopter spins its blades.
xxx: it doesn't spin
xxx: in intergalactic space there's no atmosphere and spinning blades is pointless
yyy: then what's a helicopter doing in intergalactic space?
xxx: flying, I said""",
408049: """xxx: I feel it'll be hard with Natasha...
yyy: why?
xxx: I'm getting the suspicion she's already planning to have kids.
yyy: tell her that if she wants to raise children, first she'll have to build the Capitol""",
408050: """xxx: damn, cut my hand, hurts
yyy: relax, it'll heal before the wedding
xxx: the wedding's in 2 HOURS!!!!!!!""",
408051: """xxx: Listen. Who did Spartak beat there? My neighbor was so happy...
yyy: The Romans. But then they took revenge on him.""",
408052: """xxx: Shifting the research object... How to explain it simply...
xxx: Our time. Transgenic buckwheat. What's "transgenic"?
xxx: 22nd century AD. Transgenic buckwheat. What's "buckwheat"?!""",
408053: """xxx: I wish you were near right now...
yyy: Numerically?)
xxx: Idiot""",
408054: """X: Here accounting chipped in 50 rubles each and bought a cheap kettle at the market
X: Now they're chipping in 800 each for repairing the server with 1C""",
408055: """<pingvin> Guys, what do you recommend for LAN traffic accounting?
<levsha> unlimited""",
408056: """xxx: you're totally obsessed with your security service
yyy: why?
xxx: who was poking the poor cat's nose yesterday screaming "THAT'S AN INFRACTION, SOLDIER!!!"
yyy: shouldn't have pissed under my boots, next time I'll make him do push-ups and scrub floors.""",
408057: """xxx: Even harder for me to understand why they consider the ability to play music by tags, not by files, an incredible achievement and breathlessly tell how they used to have a dump of MP3s, and now a "normal music library" and they've "forgotten about files and folders"

yyy: the main trick is in smart playlists. whoever got to that - orgasms.

zzz: Is that the crap that can play Pugacheva after Cannibal Corpse?""",
408058: """xxx: Ten years of professional opera vocal training and rehearsals as a soloist in a heavy metal band are nothing compared to two lectures given to freshmen...
xxx: I lost my voice after those lectures ((( """,
408059: """XXX: Hello. my printer crumples paper =( can anything be done with it?
YYY: Put it in the toilet!""",
408060: """xxx: thinking of downloading the series "quantum jock"
xxx: uhhh leap
yyy: monologue "birth of a nickname\"""",
408061: """Vyacheslav: Lend me the car for the weekend?
Vyacheslav: And I'll give you Fallout!
Vyacheslav: =)
Vyacheslav: If I crash it you can give the disc back without the box!""",
408062: """Spravedlivy: FFS
Lena: And what's ffs?
Spravedlivy: Waaait Pweease A Bit
Lena: You ffs me too.""",
408063: """From Habr about a domestic smartphone:
xxx: No, but there really are innovations here — the phone won't have such parameters as "front" and "back"! You can grab it by any side since the displays will be on both sides! Revolution!
yyy: This phone will have an ass on both sides. :)""",
408064: """x: As the head of the urban development department Nikolai Zlobin said, the electricity consumption of LED lamps is 25 times greater than the lifespan of a regular lamp.
y: and a cow's weight is 250 times greater than a duck's lifespan""",
408065: """<Ocelote> I'll never buy an apartment downtown - there's nowhere to walk the dog there.
<cm_silence> Don't want to upset you, but you'll never buy a downtown apartment not only because of that...""",
408066: """From one forum, a topic about poisoning cockroaches:

DV: Better to poison cockroaches together with the neighbors, i.e. I meant jointly with the neighbors, so they don't run from apartment to apartment, I mean, the cockroaches...""",
408067: """Jeka : Did you read the article where the words mom and dad were banned?
Larri: Yeah
Jeka : As I understand, they want to abolish all gender division at all, so in 200 years only the proud "human" will remain, in another 200 "mammal", and in 500 years just "creature"
Jeka : The police will go crazy, a creature robbed a bank!
Larri : No distinguishing features
Larri : Creatures attacked creatures
Jeka : And took an object""",
408068: """her: why do you take so long to reply?
him: smart people think before they write
her: but I write right away!""",
408069: """Frank: Saw a female job applicant's questionnaire, and blurted "Ooo, let's take her, at least there'll be one pretty one"... In the accountants' department, where HR and lawyers were drinking coffee... You just feel that moment when even the fly on the window freezes...""",
408070: """From a drumming forum:
xxx: will Sonor drums do for playing thrash metal?
yyy: Will a Seagate hard drive do for storing photos?
zzz: Will gopniks do for a boxer?""",
408071: """xxx: I promised the neighbor girl a kilo chocolate bar if she loses weight to 65 kg.
yyy: cruel
yyy: that's like rewarding with a World of Warcraft collector's edition for good grades""",
408072: """xxx: remember I ran around the office with a joyful scream "HURRAY I FINALLY FOUND THESE DAMN STAPLES!!!!!!!!"?
YYY: yeah
xxx: didn't see where I put them after that?""",
408073: """xxx: Wanted the best, turned out as always: Wanted to wash the grater, but grated the sponge""",
408074: """xxx: When I was a kid, I often went to small computer clubs on weekends to game. The admin of one such club constantly created a CS game for 11 slots, though there were only 10 comps. I kept wondering what the trick was, since 1 slot was clearly extra.

xxx: Once I couldn't stand it and asked the admin. The answer stunned me: "It fills up faster this way\".""",
}
