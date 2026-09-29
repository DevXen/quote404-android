#!/usr/bin/env python3
"""Translate bashim chunk_25 batch 6 (input lines 501-600, ids 407574-407673)."""
import json

EN = {
407574: """pen_guin:
I told you how I did the medical checkup, specifically the ENT?

pen_guin:
to get a second higher education you need to submit a pile of documents to the admissions office, including a medical certificate form 086/u.
picture me - a 30-year-old 100-kilo hulk with two-day stubble, barging into the ENT's office yelling from the door:
- doctor, I want to study!
he looked at me so strangely and says:
- that's not for me. the psychiatrist is the next door.

pen_guin:
then, when I explained the situation, he asked me:
- ears don't hurt? fit)""",
407575: """Flagman: Lost contact with a unit in Yakutia. Satellite channel. The on-site personnel checked everything, there shouldn't be any problems. Raised the signalmen. A signalman came, looked. Turns out some asshole parked a Ural truck right in front of the satellite dish and left. Can't find the asshole, no channel :)""",
407576: """Lenka: Look what my horoscope says:) Pisces, devote today to the person you love.
Dark Water: You already devote whole days to yourself""",
407577: """nnn: Do people change? I think people change. You just need a strong enough influence.
rrr: of course they change. Not necessarily for the better
lll: Of course they change. Prince Golitsyn, I remember, once traded an excellent English greyhound puppy for two yard wenches and a coachman""",
407578: """A chief-accountant wife about a field tax audit:

Anna: The tax inspectors pissed me off
Anna: They drink water from the cooler and say the cooler can't be expensed. Kostya the director tells them: well go drink tap water then... and took the cooler to his office""",
407579: """xxx: With my handwriting I could make quality captchas.""",
407580: """On the StarCraft 2 forum, a topic titled "Looking for a girl to play SC2 2v2".
...
SirNikotin: "I'm slooowly building a battlecruiser... Mmmm, how big it is..."
...""",
407581: """XXX: My dog just caught a fly mid-flight and ate it
XXX: no, damn. turns up his nose at sausage, but a fly - yes, tastier of course
YYY: throw the sausage in the air and buzz""",
407582: """xeon (18:04:24 6/08/2010)
today saw Chubais's motorcade
they say they brought nanotanks

Jumper (18:06:44 6/08/2010)
they shot nanoshells from nanocannons at nanosparrows - broke one shod flea. the lefty drowned himself from grief""",
407583: """dias: how am I still alive with my luck?
KOI-8: what's up?
dias: threw an awl at the wall, it bounced off and stuck in my nose
dias: now there's a hole in my nose
KOI-8: took a photo?""",
407584: """xxx: mom called today. Said yesterday she heard on TV that kids sit at the comp because parents don't pay them attention.
xxx: then she declared that from this day on they pay me attention
xxx: and asked me to wash her sundress....""",
407585: """L: now I'll be fucking with this design
L: but I have Misha
M: will Misha be fucking? )
L: no, Misha won't be fucking
M: until he makes the design? )""",
407586: """chChch:
This network is getting scarier and scarier, now they're adding geolocation, it's great, they can grab you right on the street through your page

chChch:
The time is near when at a trial it'll be something like:
Prosecutor: Were you home at 21:00 on August 8, 2010?
Defendant: *sweating, nervously biting his lip* No!
Prosecutor: *solemnly* And your 234 friends, after being questioned via instant messaging, claim you were online and tagged yourself in 750 photos, here are the screenshots, your honor, attach to the case, *applause in the hall*
Defendant: Nooo! It's not me, not me, my page was hacked!! *throwing a hysterical fit*""",
407587: """xxx: Damn, why do I need her?!
xxx: I can boil potatoes myself, fry eggs myself...
xxx: And anyway I bought myself dumplings""",
407588: """What I like most about the new job is our sysadmin, who appears behind you out of nowhere with the words: "I need your computers!!!".""",
407589: """xxx: Before, when I drove an old BMW, all kinds of "street racers" on tuned Ladas kept trying to overtake me. It really pissed me off, and now, when my business finally took off, I could afford a Porsche 911. And you know what changed?

yyy: What changed?

xxx: Fucking nothing changed! Those damn street racers on tuned Ladas are still trying to overtake me. God, give them brains, at least a little...""",
407590: """xxx: My guinea pig looks like a Neanderthal. Shaggy, hunchbacked and always looking for food.
yyy: Teach it to make fire.
yyy: Let it move to a new stage of development!""",
407591: """xxx: I don't look for easy ways!
yyy: You get your teeth done at the proctologist's?""",
407592: """xxx: I'm bored
yyy: me too, but I try to fix it
xxx: me too, but I get banned for it""",
407593: """xxx> well I know English better than Dimon
yyy> well he might, yeah
yyy> I nearly fell off my chair when he translated a function call as "function challenge"
yyy> the function challenges you to a duel, bitch!!!!
yyy> why did you write me so unoptimally, scoundrel!!!
yyy> you sorted with a bubble in me!!!""",
407594: """yak 42: my job is normal!
Don Sun: If it were a normal job, like all normal people you'd be counting down the days to vacation in your VK status!
Don Sun: Not hours""",
407595: """predteche: I'm living at the dacha now, and the cat roams constantly, this morning it brought a mouse. would've been fine, only it brought the mouse with the mousetrap, in the evening I'll go around the neighbors to find out whose mousetrap it is""",
407596: """a conversation about a fired employee

boss: and still he worked well, really excellent. too bad he turned out to be a psychopath
me: Van Gogh was also a genius and crazy
boss: in that case you should work like Van Gogh too. As a freelancer.""",
407597: """Andy: I think "colleague" is an intellectually higher definition
Andy: Well, like two professors
Johnik: yeah, you're probably right
Johnik: me and a colleague were unclogging the toilet yesterday...
Johnik: and you know, we didn't agree on Kant
Andy: hard to decide whether to smear the rim with sealant or lay hemp""",
407598: """Discussion of the news about a robotic arm prosthesis.

xxx: Interesting — if you bend the pinky, will the ring finger bend too? Or did they fix that bug in the prosthesis?""",
407599: """jezzarax: the entry threshold is higher there
jezzarax: you can learn to crank out a hello world on asp.net in 10 minutes (8 minutes to launch the studio, 2 for coding), that won't fly on java
jezzarax: while you download one library, then another, while you glue them with half a meter of xml config, while you set up the mapping for hibernate, while you draw the database, while you bring up the web services
jezzarax: you're writing hello world, but two weeks have passed and everyone thinks it's an accounting system for small business""",
407600: """xxx: My girlfriend and I planned the exact date of our wedding, to prepare in time.
xxx: Now we're sitting, celebrating minus the first anniversary.""",
407601: """xxx: And from our window, only the school is visible!!!
yyy: And from our little window only a little bench...""",
407602: """<KiraSun> Guys, can you recommend a program for finding rings in the network?
<oni> Frodo, is that you?""",
407603: """A: Hot cognac is clearly too much...
B: I know cooler. Tried hot ice cream?
B: When I was little grandma boiled my ice cream "so I wouldn't catch a cold"
B: motivated by the fact it melts in the stomach anyway.
B: it's funny to you, but you don't know how I suffered""",
407604: """xxx: Once I stared long, puzzled, at the "1080p." price tag on a small portable LT TV...""",
407605: """Echuy: They look at my sneakers, jeans, t-shirt, backpack with a laptop sticking out, even some stubble, and say, just needs glasses and a tambourine...
Echuy: And I stand there in a stupor thinking what to take out of the backpack first, the lens case or the flute...""",
407606: """kopez: "at the first signs of carbon monoxide poisoning immediately leave the room and go outside"
kopez: they just forgot to add - "that's where you're fucked\"""",
407607: """xxx: When I was about 3, my parents started teaching me to wipe my own ass.
xxx: And I thought my parents were so lazy that I had to wipe my own ass.""",
407608: """Went to the store with two girlfriends:
Semyon, buy us 2 curd snacks!
-Give us 2 more curd snacks!
-Different ones?!
-WHAT ARE YOU TALKING ABOUT? they'll fight over these curd snacks, give us identical ones!""",
407609: """Stunder: How I like the feature - shook the phone and the track switched. Now I don't listen to music anymore, I switch it
P-Johann: My UAZ had cooler automation. turned the wheel - the radio switched =)
P-Johann: still a mystery how it was connected
Stunder: But I bet they were scared of you on the road when you were catching the right station""",
407610: """xxx: I think it's very telling that of all faculties, ours - management - failed to organize a graduation party...""",
407611: """xx:
they released the iPhone 4 Black Diamond, 10 grand. diamonds, gold, some wood.
programmers need something like that too
xx:
"this program was compiled on a gold-encrusted compiler"
"the code contains diamond dust"
"the programmer ate exclusively Batuin skholompasy during programming\"""",
407612: """*shkoda* (16:19) :
damn, with this job I'll have to put sex on the shelf and occasionally dust it off ((( """,
407613: """xxx: Good to talk to fish - they always understand everything and listen attentively, don't interrupt
yyy: and flap their mouths confirming they understand
zzz: Yeah, and they poop
zzz: Confirming they don't give a shiiit""",
407614: """Doddy: A traffic cop pulled me over...
Gfif: What'd he want?
Doddy: Well what, what? Friday evening. Asked stupid questions and carefully sniffed my answers...""",
407615: """xxx: "and the evil stepmother sent the patch-girl to the forest. for..."
xxx: uh, what did she send her for?
yyy: for patches!""",
407616: """xxx: invitations to go hiking always scared me
yyy: why? it's fun! fresh air, songs by the guitar... catamarans are totally awesome!
xxx: you have an enthusiastic attitude
xxx: and I have completely different associations
xxx: you don't get enough sleep like hell
xxx: everyone who's not lazy hunts your food
xxx: taking a piss, pardon me, is a whole problem, won't even mention washing
xxx: you're all sweaty, sticky and constantly stink of campfire
xxx: and I have fun
xxx: for nights now I've been sleeping max 3 hours - stuffy, no AC
xxx: on the 1st they turned off hot water - can't wash
xxx: yesterday Vadik came, ate my whole weekly (!!) food supply and broke the toilet
xxx: won't even mention the smell of burning
yyy: if Volodya doesn't go hiking, hiking comes to Volodya )
yyy: sorry, buddy )))""",
407617: """xxx: Those who survived the summer of 2010 will be kicked out of Hell.
xxx: For laughing in the cauldron.""",
407618: """xxx: What mosquitoes? ) Has anyone seen even one in the city this summer? Especially in the smoke...

yyy: A couple keeps flying into my place.

xxx: The same couple?

yyy: Well, if you believe in reincarnation...""",
407619: """xxx: yesterday I set the alarm for half past five.
xxx: and didn't get up... =(
yyy: for half past five? it won't get up!""",
407620: """aaa: I know consultations are banned in the community, but.. A dental question - what can be done in field conditions (I'm on a train, three days to go) when a flux appears?

bbb: At the nearest station put your head under a wheel

ccc: of course this isn't a consultation (but it should help) - put your head under a wheel

ddd: don't listen to them, they're incompetent. At the nearest station put your head under a wheelset""",
407621: """A> everyone's laughing at me!
B> why?
A> when talking to the tax office I said I have penalties since 2003
A> and they heard it as one word!""",
407622: """XXXX: screwed around till 8pm
XXXX: and the problem turned out to be the Ravshans
YYYY: and what were they doing there?
XXXX: drying laundry on our fiber""",
407623: """xxx In the army strength matters, not brains. Me for example last year couldn't move the piles in the village, but pumped up over winter and removed all the piles
yyy And if you had brains, you'd have thought of using a lever last year...""",
407624: """Lee Kuan Yew once said that only after he shot several best friends did Singapore realize his reforms were serious.""",
407625: """him: Dog is man's best friend, never heard? ;)
her: Hee, I love cats
her: And I've hated dogs since the second Call of Duty!!!""",
407626: """kotofei7777:
mom dropped the ficus into the fan
the fan turned out hardy and thoroughly and methodically chopped the ficus into liquid mush right onto mom

kotofei7777:
mom's in ficus
the fan's in shock""",
407627: """Grow3r: let's go to the pull-up bars
Grow3r: smoke""",
407628: """WoLchenok: working with a partner in security in two shifts. I'm nights, he's days. he just came back from vacation today, asks me to call him at 6:00 to wake him. at exactly 6 I call and yell: "I'M AN ALARM CLOCK! get up to work the work!" in response I hear: daaamn!!! then the line drops. this genius thought he dreamed a talking alarm clock and in fear threw the phone at the wall. now he's sitting over the wreckage trying to hold back tears...""",
407629: """Valery: smoke is coming from the server room... what to do?
tots.go): Don't open""",
407630: """xxx: it'd be cool if a racing wheel for the comp came with a centrifuge simulating accelerations during the race... crashed into a wall - your cabin detaches and flies into the wall - game realism will reach a new level
xxx: and people won't be run over in games
xxx: - humanity""",
407631: """yyy: say something.. that gives me goosebumps!
xxx: DISK BOOT FAILURE - INSERT SYSTEM DISK AND PRESS ENTER""",
407632: """<ajax16384> my wall clock is melting
<ajax16384> and the keychain
<ajax16384> oh wait - that's the keychain from the Dali museum
<ajax16384> it was like that""",
407633: """WLAD: The most useful thing I've done at work lately - oiled the door so you can't hear me leaving an hour early...""",
407634: """her: do you happen to have pinky-size accumulators?
him: AAA?
her: I'm asking, don't you have any accumulator-batteries?""",
407635: """xxx: funny this morning. Was trying on a wedding suit, going to a wedding on Saturday, need to look decent. And the internet installer was supposed to come, he calls, there's a guy, of the visible clothing only shorts and flip-flops. Sorry, he says, for looking like this. And I open the door all dressed up, in pressed pants, white shirt and tie.

xxx: and to his apology it just burst out: "Well I got all dressed up for nothing!\"""",
407636: """xxx: Hey! Why weren't you on ICQ yesterday?
yyy: The chief accountant loves me a lot!
xxx: ?
yyy: Telling it calmly now. In the morning, trying to get into the office, the key doesn't fit. I go to her. She says with an icy voice: "Sergey, write a resignation at your own will, or we'll call the militia". I recall all my sins, ask what the hell, actually. She shrieks, kicks me out of the office and locks herself in. That's not all!
I go to the director, he says: "Seryoga, get treated while it's not too late, it's treatable!" I'm totally dazed, I go to the guard, he goes: "Why are you shooting up at work? Couldn't find another place or what?" Now I get that they want to set me up big time, I pressure the director, and learn that in the morning the cleaning lady pulled five syringes from my trash and took them straight to the chief accountant. And she, accordingly, started this whole mess. Menopause, you see!
xxx: Thermal paste?
yyy: Yeah, and one with machine oil residue.""",
407637: """PsychoQueen: what to wear a cream-gold silk scarf with?
Sawyer: with pathos""",
407638: """xxx: this morning the alarm rings at 8:15
xxx: I turn it off, thinking, I'll get up now
xxx: fell asleep, waking up
xxx: look at the clock - 8:16
xxx: overslept by a minute
xxx: thinking, fine, need to get up
xxx: opened my eyes, blinked
xxx: look at the clock, and it's 9:30
xxx: won't blink anymore""",
407639: """Gennadich: As the classic said - I know for sure: the impossible is possible..
Gennadich: Our programmers yesterday managed to break a broken kettle""",
407640: """Major at 14:22
- Hi! Please return my account. I didn't delete it, my girlfriend did!
Admin at 14:30
- Hello. Need proof.
Major at 14:31
- Provided it to you in ICQ.
Admin at 14:34
- Returned everything.
Major at 14:34
- Thanks, I fixed everything too.
Admin at 14:35
- What could you have fixed?
Major at 14:35
- Dumped the girlfriend""",
407641: """lisei: fuck, I'm tired
lisei: was loading pipes
lisei: then it turned out I was stealing them
lisei: and nobody told me""",
407642: """xxx: andrey's asking how to check the zip code of an email?
yyy: have you lost your minds""",
407643: """Bormotron:
Cool people work at NASA:
Test of Apollo stage 3, rocket on the stand, fueled, waiting. The engineer counts down - 3, 2, 1, IGNITION! they give ignition.

Bormotron:
KABOOOOM !!!!!!!!!!!!!!!!!!!
the rocket is blown into a thousand pieces, everything's burning, flying in all directions, anywhere, far, rumble, panic!

Bormotron:
Engineer:
- "Guys, I think something went wrong..."

Bormotron:
I wish I had nerves like that...""",
407644: """xxx: Vanga predicts the start of World War III in November 2010!
yyy: Fuck! I'll only have a week to beat Fallout and Fable 3!!!""",
407645: """online game, quest discussion

Max Art: Kruz can't come with us in the evenings anymore - his wife threw a fit :)
Max Art: like someone calls his mobile and hangs up. Who? Obviously a mistress!
Max Art: Kruz tried to explain he's being called to the forest to beat orcs, but for some reason his wife didn't believe him""",
407646: """xxx: ...and don't forget to take a floppy!
yyy: umm.. Maybe better take a flash drive?
xxx: are you planning to watch the solar eclipse through a flash drive?:)""",
407647: """XXX: I thought daily 6am workouts would make me a morning person.
XXX: Yeah right.
XXX: They made me a gloomy, sleepy and tired night owl.""",
407648: """- we have two tanks in the raid, both with brains.
- Yeah, only one's brains are inside the helmet, and the other's outside.""",
407649: """ifrond: In the Karsun district officials decided to buy themselves a new Toyota for 1,336,000 rubles. For comparison — the whole utilities sector of the district got 1,400,000 rubles.
Al9000: If not for this shitty utilities sector, they could've bought two Toyotas!""",
407650: """xxx: they'll waste money on rebranding... could probably launch half the country into space
yyy: The most expensive rebranding in human history was renaming the CPSU to United Russia.""",
407651: """Mitrich: Never take a cat to the beach
Mitrich: It might just freak out at such a huge litter box with sand!""",
407652: """xxx: Julius Caesar's younger brother could do three things at once: envy, cry, and wear hand-me-downs.""",
407653: """Lana00: The aunties under the window, I'm amazed
All summer they nursed a luxurious flowerbed. Took turns on duty, weeded, watered, chased cats and kids, put in enormous effort
Lana00: A week ago they installed water meters in the whole building.
Lana00: Everything died instantly.
Lana00: They'd chase a hare for a ruble...""",
407654: """Her: Lazily poking around in Photoshop
Him: With your photofinger?""",
407655: """Nimphette (17:05): how do you spell "videesh" or "vidish"?
Sashka (17:05): "ish"
Sashka (17:07): But in your place I'd first sort out the word "kag"
Nimphette (17:07): what's wrong with "tag"?""",
407656: """xxx: And at our school canteen there were outright cheaters working.
xxx: Once I took a sausage in dough, started eating, and the sausage sticks out at both ends but it's empty in the middle. And if you put both pieces together it's even less than half a sausage!
xxx: And the canteen lady said the dough "tore" the sausage.""",
407657: """xxx: hedgehogs die from the smoke, and hedgehogs are the forest's sanitizers. Without hedgehogs foxes start starving and run out to people more often, and foxes are rabid and bite...
xxx: also the mosquitoes disappeared from the smoke, frogs have nothing to eat and die, no frogs - no herons.
yyy: Are you saying now foxes will bite us instead of mosquitoes?""",
407658: """User|1|: Why in all movies do aliens always land in the USA???
User|2|: Here traffic cops would fine them for parking...
User|3|: There are no ads like "BUYING NON-FERROUS METAL. EXPENSIVE". You can calmly step out of the saucer and take a walk.""",
407659: """him: my mouth's numb again
her: maybe you're allergic to me?
him: no... I'd be dead already...""",
407660: """Him: you haven't seen the volcano in my pants yet
Her: the one that wakes up once every thousand years?))""",
407661: """x: gorgeous song. Those should be performed with a hoarse voice to an out-of-tune guitar, sitting on haunches, cracking sunflower seeds and wiping tears with a cap
y: a stingy tear must roll unwiped through the stubble. more real that way
x: yeah! and the performer must be female
y: then it has a long way to roll to the stubble...""",
407662: """<RJ45> after such a week you're supposed to get a month of vacation damn
<RJ45> monday. A soldering iron explodes in front of my face
<RJ45> tuesday. from a raging PSU I get 220 volts of goodness and joy
<RJ45> wednesday. the guard locked the always-open turnstile at the checkpoint, through which I somersaulted.
<RJ45> thursday. trying to move a cabinet with the stationery archive alone, behind which is a power outlet, to the laughter of the loaders standing nearby I drop the cabinet on myself, beat up the loaders, get a reprimand
<RJ45> friday. the fucking step turned out earlier than I expected it
<RJ45> good thing I don't work Saturdays
<VikinGr1m> tough times you're having, though
<DimmRock> RJ45: don't go to the black mesa
<VikinGr1m> but it's worth getting a red crowbar)""",
407663: """luli: I mix up right and left)
rider: and how do you drive?
luli: intuitively)
luli: I have a hint - on my right hand I have a finger with a scar
luli: though once I forgot what it means. I remember the finger with the scar means something.. but what I don't remember""",
407664: """†NormaN†: Now you understand why people measure life in the number of YEARS lived? )""",
407665: """xxx: I'll personally never forget how I passed the midterm in metrology with a 4, firmly believing it was electronics...""",
407666: """xxx: damn, why at the most crucial moment do you have to step wrong???? *WALL*
yyy: step where?""",
407667: """xxx: nah, I get things fast
xxx: ..just not right away""",
407668: """From a video card discussion forum:
xxx: Don't GPUs shut off after reaching some degrees?
yyy: They shut off, of course. At 232 degrees the CPU desolders itself and falls to the bottom of the case to cool down""",
407669: """apelsino: Oh how funny these designers are: they so subtly wrote the "POLYCLINIC" sign in the "Necropolis" font...""",
407670: """xxx: Somehow the day didn't go well today – a pile of hassle and no light at the end of the tunnel
yyy: what does that say? That you're moving your ass in the wrong direction... :)""",
407671: """xxx: dentists are such perfectionists. they're always sure you can open your mouth EVEN wider :'‑(""",
407672: """<koi-8[пых]> ugh. remember, once worked at a children's horse camp
<koi-8[пых]> besides me, one hippie and one stoned punk there were no men at all. all the teachers - girls
<koi-8[пых]> and so, once, I headed to the pond at night to swim
<koi-8[пых]> so, dove in, swimming, nice. water's warm, the moon hanging close-close. bliss
<koi-8[пых]> and then noise from the camp
<koi-8[пых]> I duck into the reeds. and see this picture:
<koi-8[пых]> running, tearing everything off themselves, all ten girl-teachers. and they're twenty. just got into teacher colleges and came for practice. and so, this crowd of naked girls flies into the pond and starts swimming, splashing and everything
<koi-8[пых]> and I'm standing in the reeds. and I feel I'm not alone here
<koi-8[пых]> I look left. standing next to me the hippie, handing me a bottle of beer
<koi-8[пых]> turns out he'd figured out this show long ago and went with beer every night to watch
<koi-8[пых]> I did set him up later though
<koi-8[пых]> once I slipped notes under all these girls' doors. where I set dates for everyone under the big tree in the forest
<koi-8[пых]> and so, me and the hippie climbed that tree. sitting, watching. one comes. stands, waits. then a second appeared. exchanged puzzled looks
<koi-8[пых]> then a third appeared
<koi-8[пых]> the hippie is already biting his hand. so as not to laugh
<koi-8[пых]> and when he fell off the tree from ha-ha, that's when they lynched him""",
407673: """xxx: today saw porn where the word "Fuck" is bleeped
xxx: probably so kids don't hear..""",
}
