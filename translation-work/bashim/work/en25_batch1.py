#!/usr/bin/env python3
"""Translate bashim chunk_25 batch 1 (input lines 1-100, ids 407074-407173).
ru is read verbatim from the input file; only en translations are embedded below."""
import json

EN = {
407074: """xxx: Banner ad: "Deal with monsters online!"
xxx: Dealing with monsters online is all I ever do!
xxx: On ICQ, on forums, sometimes even over videoconference...""",
407075: """her: You know that lens I've wanted forever? It just dropped by 10 thousand rubles!
him: Yeah, now it costs exactly as much as we're short of buying it )""",
407076: """xxx: came back to my hotel room last night drunk as a skunk and found an interior door behind the carpet on the wall.
xxx: checked this morning, the door is really there
xxx: feeling like Pinocchio""",
407077: """[tottoro]: I'm horrified by the tangle of cables behind his computer. A male octopus would be guaranteed to get aroused at the sight...""",
407078: """X: it's a good thing she's only 2 years younger than me, or we'd never have worked out.
X: 16-17 year old girls are little and silly, but by 20 they're just right
Y: why? I got married at 17! and divorced by twenty.
X: well... that's what I'm saying...""",
407079: """xxx: also I'm really scared of the "Painters"
xxx: they're these huge mosquitoes - they keep flying into my room(
yyy: and they paint it? )""",
407080: """From a thread about the scheduled test of the city alert system:
xxx: Didn't know about the test. Woke up to the wail of sirens, immediately turned on the TV, they were showing a rocket launch from Baikonur, shat bricks.""",
407081: """Raven38:
Sitting at the computer, drinking tea, hear one of the kittens meowing. I turn around and observe the following:
1)The mama cat lay down right next to two of them, on top of the third. He's squeaking.
2)The mama cat got up, turned around, lay down on the first two. They're squeaking.
3)The mama cat got up, turned around, lay down on the third....""",
407082: """xxx: Selling an air conditioner through the forum. For 2500. Gave a guy my phone number, he's from another town, his father is supposed to come.
xxx: The father calls: "This is about the air conditioner. Father, so to speak, of Lucifer\"""",
407083: """Hello,skunk: Ugh. I want to call some helpline and start telling them about my programming problems... :-(""",
407084: """xxx: Has nobody thought of replacing airbags with an ejection mechanism? Bang — and you slowly descend by parachute, admiring the crash site from above.
I realize it's unrealistic, but it would be great.
yyy: yeah. only, my dear sir, you failed to consider that if the car rolled over — bang, and they slowly and carefully scrape you off the asphalt. or off a garage roof, if someone rammed your side on the "taxiway".
zzz: The Norwegians with their many tunnels wouldn't be thrilled either.
qqq: the trolleybus wire repairmen would also get sick of taking down shish-kebab""",
407085: """Nick: when there's no one in the house to wash the dishes, the sink turns into one big Tetris.""",
407086: """xxx: Walking down the street, talking on the phone. I go into a store. Before I even reach the counter I feel I've become an object of intense attention, I look up and indeed, the saleswoman and several shoppers are looking at me, mildly surprised, to say the least. I look around... I turn around... A glance at the door...
xxx: I remember, I still had time to think: "What kind of idiot took his shoes off by the entrance!" """,
407087: """GRIF: I remember when I was in my third year at uni
GRIF: It was winter, freezing...
GRIF: And I headed to the builders' market to get some stuff, walked up to the nearest old guy, and he's standing there in a sheepskin coat, frozen stiff, shaking. And his stall has everything you could imagine....
GRIF: Hardened self-tappers, regular self-tappers, nails of all sizes, nuts, screws, bolts and washers...
GRIF: And ever since, whenever I'm too lazy to work, I remember that market and realize who am I to be so picky. I'm sitting in a warm office while someone out there is selling nuts, dammit""",
407088: """At the flea market a young slim girl (G) is trying on jeans, a saleswoman (S) hovering nearby
G: They're a bit loose at the waist (pulling the jeans away from her waist)
S(in a hypnotic, soothing and penetrating voice): Don't worry. It'll grow in soon, it'll fit just right.
G(with fury and instant reaction): Are you SURE?!!!""",
407089: """EviGL:
My knife can do anything. Even if there's something my knife can't do, it can serve as a motivating factor to make someone else do it )""",
407090: """xxx: need to find a new best friend
xxx: cause the old one has gotten completely out of hand already""",
407091: """xxx: damn, this exam session is killing me! standing on the platform at 4 am, waiting for the train
xxx: here it comes. I'm dead on my feet, brain's gone. eyes closing. the conductress says something about tickets
xxx: without thinking I pull out my student's record book, open it to the right page and hand it to her. and I'm thinking, where do you even get tickets?!""",
407092: """xxx: get this, yesterday I baked pies with jam and forgot to hide them from the cat. I wake up in the morning and they're all scattered around the kitchen, each one nibbled from one side!
yyy: he was probably looking for meat ones""",
407093: """mig: the worst is when the neighbors stealing your shared wifi also knock on your door asking "do you have internet?\"""",
407094: """Echuy: Being near you, even diamonds begin to glow, reflecting the light of your beauty!
Echuy: And panthers and leopards freeze, struck by your grace and elegance.
Echuy: And lightning bolts halt in embarrassment, trying to sparkle in the sky covering a world worthy only of your shine.
Echuy: And mirrors, having reflected you, forever become photographs, unable to forget your beautiful image.
Echuy: And also I jerk off to you.
Echuy: There! Damn! Couldn't hold it in.""",
407095: """Tiss: my hands remind me of indefinite integrals""",
407096: """Marina: drink lots of water with lemon tea
Kondrat: holy shit
Kondrat: and where do I get lemon tea?""",
407097: """xxx: hurraaaaay!!!
xxx: my friend gave birth!!!
xxx: a boy, 3 kg, with a little tail!!""",
407098: """just now at work:
me: Alexey Viktorovich (the chief accountant), would you mind giving the lawyer the company seal and the "true copy" stamp
lyokha (handing over the seeal): In principle no, but tell me, my dear, for what purposes?
....
me (taking the seal): what's it to you? the investigator will tell you""",
407099: """1: you owe me 2 rubles
2: ok, put it on my tab )
1: fine, I'll add it to those 100 bucks
2: $100 for what?!
1: for opening the account""",
407100: """xxx: I broke up with my boyfriend again :(
yyy: But why him?! He's ideal!
xxx: Yeah. Ideal. He decided to surprise me. Somehow he talked my parents into giving him the apartment keys when they were leaving for the dacha. Decided to make dinner.
And picture this. Evening, dark, I come home tired, hear noise from the kitchen. Knowing my parents definitely left, no one's home, I immediately start thinking, who broke into my house?!
xxx: I decide it's a burglar. Then the burglar appears in the kitchen doorway, and I punch him in the nose with all my might.
And it's my boyfriend. He spent an hour whining "for what?!" and stopping the blood, then called me hysterical and left.
xxx: And my parents are cracking up. And I, dammit, was defending my home.""",
407101: """[Sed]: In normal families they inherit apartments, cars, jewelry, but in ours - passwords...""",
407102: """KIY> a significant downside of the Kalashnikov was the large bullet spread.
Semyel> Yeah, yesterday I read how our guys fired a Kalash "machine-gun style" by tying it to a tree. :)
Igor> tying to a tree is like?? first time I've heard of this
Globe> you take the rifle's MAC address and carve it into the bark.""",
407103: """zhsky: Colleague Ch. told me her 2-year-4-month-old daughter gets up very early. She sneaks to the computer, turns it on, then launches the browser. On the command line she types the letter "p" and launches the Poisson Rouge game, which she plays until her parents wake up.
- And then, - says Ch., - we went to the parent-teacher meeting at the nursery, and they told us strictly: "Dear parents! You must gradually potty-train your children!" """,
407104: """xxx: Fuck, my wife. Gave her 5 thousand rubles in one bill, like, go break it, we don't have any smaller money at home.
xxx: She went and "broke" it.
xxx: Still no small money.""",
407105: """Racer: I finally sold my Nissan and bought a Mitsubishi Eclipse GSX (the G2 one), now I'm tuning it. Happy. The difference is fucking enormous.
Yozhig: Lucky you. Mine's the opposite — sold my Zhiguli (2103) and bought a Zhiguli (2106), now I'm repairing it. Not happy. No difference at all.""",
407106: """XXX: damn, I get the feeling the mosquitoes are about to stick an apple in my mouth and start the final feast!""",
407107: """Alinko> When you tie your tie, take care of it
Advokat> After all, it's the same color as the flag of Mordor""",
407108: """<mib_fmrro7> how do I get bluetooth working under ubuntu 10.4, maybe the question sounds clearer this way?
<CTpaHHoe> five years ago I couldn't get ssh working. someone here advised me to install it and it helped
<Злобень> my TV won't turn on, what should I do?""",
407109: """her: This is the second time I'm dating a guy who can't swim. And the first time it was you too""",
407110: """Dark Water: I bought darts :) By the way, do you have any new photos?""",
407111: """xxx: I stopped trusting that company after the phrase "and our CFO is currently studying full-time\"""",
407112: """xxx: mom works at the Ministry of Culture, it's her birthday today. came home tipsy and cheerful.
xxx: says the brightest gift from her colleagues was a call from an unknown number with the question
"Hello, is this the laundry?" :)""",
407113: """78 km from St. Pete, a holiday camp, the neighbors in the next cabin punched a hole in their car's radiator.

me: so what are you planning to do?
neighbor: drinking for now, not helping yet.""",
407114: """Rogal Dorn: By day 190 I finally arrived at the thought that I'm not afraid of expulsion, and they won't keep me back a year, and I started gradually getting cocky, blowing off the commanders' wishes!""",
407115: """XXX: I look at my cat
XXX: wish I had that life
XXX: he's useless
YYY: you too""",
407116: """Oksi: DiDyuLya's track "Farewell Surface" is called
Oksi: so many associations right away
Oksi: like a drowning torus
Oksi: or a departing truncated pyramid
Oksi: or a burning cone""",
407117: """xxx: I go into a store now to buy some doshirak. I ask which one is tastiest
xxx: The saleswoman looked at me with pity somehow and said - they're all not tasty, better get some bread and sausage""",
407118: """Folkan: americans are pessimists. they call a 150-year prison sentence a life sentence )""",
407119: """Employee: Listen, don't you have some little program that changes the encoding in Excel?
Me (admin): You mean that file with Turkish characters?
Employee: Yes.
Me: Those are Turkish characters...""",
407120: """Boorawchick: Quest: the doorknob broke off from the inside, it can be opened with a key. The key is in my shorts, the shorts flew down to the balcony below. The neighbors downstairs left for the seaside.""",
407121: """xxx: They don't pay me enough to also maintain a conscience.""",
407122: """xxx: How are you doing? )
yyy: gorgeous! sitting in the window )
xxx: Why?
yyy: walking the cat""",
407123: """From a conversation with an army buddy of mine about our army...
-"Kalashnikov made a gas exhaust system so good they'll use it right up until they invent turbo-plasma rifles, and it's not even a fact they'll abandon it after that"...""",
407124: """xxx: If you cut an onion and rub the top of your foot with the cut side, in half an hour you'll taste the onion – volatile substances will spread through the bloodstream across the whole body

xxx: we wiped up that spilled beer yesterday for nothing, should have stomped around in it :)""",
407125: """xxx: don't know how to get rid of house ants?
yyy: you take tiny tiny uranium shavings, sprinkle them with sugar and lay them on the path. and when the anthill reaches critical mass...""",
407126: """Lord Antony: Today on the bus I saw a man in a purple shirt, with a purple bag, eating purple ice cream. I think I glimpsed universal harmony.""",
407127: """xxx: you can ask me any question, but then I get to ask you something too!
yyy: Literally any?
xxx: yes!
xxx: now it's my turn""",
407128: """Hello, I bought headphones from you. I listened to music in them. But one ear tore off the headphone. Can the torn-off ear be used as a microphone to talk over Skype? Thanks""",
407129: """B-Boyizm^: what's new in the new iphone?
iluha 3W: the glass back panel
iluha 3W: so if before everyone prayed the phone would land on the asphalt on its back cover, now they pray it would just hover in the air :-D""",
407130: """Dimka: on anti-malware there's news - An American homeless man discovered a leak of personal data! Further in the text - a local homeless man found a large number of confidential documents in a dumpster.""",
407131: """Him: ok so here's the plan...
tonight you wait for me, I pick you up from work, we go eat sushi, then we go to your place and have passionate sex.
Her: :O
Him: no, not oral""",
407132: """Rukov: sometimes in the morning I just want to draw a demotivating frame around the mirror =(""",
407133: """xxx: my cat
xxx: came up and stole some cheese
xxx: then a cookie
xxx: and then came up and washed it all down with my tea""",
407134: """dissolved: Yesterday dad renamed me in his phone from "Daughter" to "Dad, give me money" :D""",
407135: """xxx: Ohoho... did you read Chekhov's story "Fat and Thin"?
yyy: yes, moreover, recently :)
xxx: It's just that after the phrase "Dad works at Gazprom", I don't know how to address you anymore :) Your Excellency?
yyy: he's an electrician :)
yyy: so you can just say - your Radiance :)""",
407136: """XXX:
the cartridge started running out, I grab a syringe, fill it with ink (I have it in a huge jar). I fill it, feeling like a doctor. I remember how doctors, having filled a syringe, squirt a little out so no air remains. and I'm imagining myself a doctor...""",
407137: """moonspell:
I worked at Svyaznoy for 5 years and got a job at the City Administration.
Damn, I don't know the full names of the Administration people, but I perfectly remember the brands and models of their phones )""",
407138: """Director: someone put a curse on us
Director: the tech support email got a letter with the content: Curse""",
407139: """Movey: Maga was surrounded by enemies. He threw his arms forward, and they fell to the ground with a dull thud.""",
407140: """xxx: You know why Apple product owners are always in crowded places?
xxx: So the apple would have nowhere to fall""",
407141: """xxx: My son is going to grandma's for the summer. I urgently need to buy him a vuvuzela - let the mother-in-law enjoy it! ;)""",
407142: """xxx: Hi) I really need your help with chemistry one last time and that's it, can you help tomorrow??? I wouldn't bother you but I really need it
xxx: can you at 2?
yyy: I'll try
xxx: well good I'll call you then, thanks a lot bro=) what would I do without you
yyy: served in the army""",
407143: """Today my daughter (2 years old) tearfully demanded IT-shku. I handed her, in sequence, an iPhone, a laptop, a computer, a navigator... The tears wouldn't stop. Turns out she needed a "Rastishka" (a kids' yogurt).""",
407144: """I got sick, sitting at home, asking a colleague on ICQ how things are at the office. Note, the whole office is 8 people. I'm sick, one more is on a business trip. I get the colleague's reply:

xxx: Quiet so far. Nobody's calling, Oleg came from the clinic. Kostya left for the clinic. Yulia was at the clinic this morning and met Sanych there.
I'm the only one here, healthy like an idiot...""",
407145: """Leeroy Jenkins: the heat helps improve posture
Leeroy Jenkins: if you hunch over - your belly button sweats)""",
407146: """xxx: a client just wrote "...at current rates it's notprofitable for us to haul the freight..."
xxx: sitting here wondering where the space should go...""",
407147: """Luchezarnaya: my husband got ripped off again - they sold him regular blunt scissors as "children's atraumatic\"""",
407148: """QQQ: Was he alone? Are you sure?
ZZZ: Yes. I count very well up to one. Never once in my life made a mistake :)""",
407149: """from support comments:
Proctologist-illusionist. Turns a VPN connection into god-knows-what.""",
407150: """N00d: gonna hit lvl 30 soon, I'll wear the Dragon Pants!
DrakoN!: why do you need MY pants?""",
407151: """xxx: my dacha neighbor installs entrance doors, he's got old iron ones by the sea, he built a fence out of them
xxx: now his favorite pastime is watching guests first try to get in, then try to get out""",
407152: """Angel_perfect: I really miss clubs ^_^ but the kids won't let me =/ once they grow up and ask to go to a disco, I'll get my revenge >:[""",
407153: """xxx: back from the sea I realized not shaving on vacation was a dumb idea
xxx: thanks to the tan I now look like Homer Simpson""",
407154: """Him: Let's finally talk seriously!
Her: Let's
Her: :)""",
407155: """Her: Medvedev got edited over Scooter!
Him: Link?
Her: Link (li) (English link — a chain link) — an obsolete British and American unit of distance, equal to 20.1168 centimeters. What are you even on about?
Him: Then the hyperlink. Or is a hyperlink the place where they exiled intellectuals before the revolution?""",
407156: """BILBOS: and I'm still amazed at the degree of my blind trust in my parents. Mom regularly fooled me with this simple trick — when I didn't want to sleep, she'd say that as soon as I fell asleep, she'd give me a candy. Here, by the way, the question of trust might turn into a question of my intelligence. But that's not what this is about.""",
407157: """MilyFireDemon: So why in English are the words "guy" and "gay" so similar
gallon: And?
MilyFireDemon: Because of my terrible spelling I nearly offended some good people a couple of times...
gallon: Download spellpower.""",
407158: """Mius: fuck! the cat is chewing my cable!
Mius: Right now it'll get hit either by current or by me...""",
407159: """From a role-players forum:
xxx: Question for the GMs. At the Kolodishchi training ground, as far as I know, there's no water source (spring). For a multi-day game we'll have to bring a big supply with us, how will this be handled? on the players' conscience or centralized?
zzz: There is water there. At the cemetery there's a standpipe and a tap at the watchman's. The watchman is used to it, he'll pour even for necromancers with crossbows.""",
407160: """Hipoh: yesterday I was digging in my PDA while my wife and little son were busy in the kitchen, after some quiet I hear: "Son! Son, yuck! What are you doing! Let go. Let go of the kitty! Don't eat the kitty, your tummy will hurt, he's dirty!"
a few minutes later I walk up to the bathtub and see the kid with the cat in his arms by the sink. good boy, decided to wash it.""",
407161: """The heat. Riding from Zagorsk. In the commuter train a lady is selling pocket battery fans.
I tell my husband: come on, I'll buy you a fan, you'll be buzzing like Karlsson all the way to Moscow.
Husband: come on, better buy me three beers with that money and I'll be humming like a bumblebee all the way to Moscow.""",
407162: """Letargia: I love that, we're working with a shipping company LLC "Autobot". Need to make them a competitor - OJSC "Decepticon" )""",
407163: """Belo4ka: fact! he's crazy about you!
Rinspeed: he's crazy in general, I've got nothing to do with it""",
407164: """Lumiere: After the +40 degree heat in Moscow they promised a hurricane. Interesting, a rain like someone barely sprinkled from a watering can - is that their hurricane? We're freezing here.

Nastazy: you had a hurricane 2 weeks ago, the whole country was talking about it. good thing it didn't happen again )

Lumiere: why didn't I see that hurricane then? )

Nastazy: oh, I wouldn't know. Over there, they say, they were ripping five-story blocks up by the roots and cars were dying in herds under fallen century-old trees. maybe you were asleep?""",
407165: """We're going with friends to the dacha to chill.

Dimon: "would be nice to kick a football around right now, what with the world cup and all..."
Vasya: "Fuck, our guys are probably sitting at home right now thinking the same - would be nice to kick a football around right now, what with the world cup...\"""",
407166: """Comments on the news "Computer viruses have reached cameras":

MRT: What will Kaspersky say?
Pink Panther: WIIII !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!""",
407167: """From a programmers' forum:
Hello, could you tell me the method or algorithm for intercepting alien messages? I'm into extraterrestrial civilizations""",
407168: """In one MMORPG before moving to the next dungeon tier:
xxx: let's rest/die off
yyy: is that a forecast or were you too lazy to stretch for the ё?""",
407169: """My wife and I are getting ready to head out on the motorcycle. My wife is taking inventory of her little backpack. Thoughtfully pulls out an umbrella - stares at it for a minute and asks:
- Hm. I don't need the umbrella, do I?
Immediately picturing the click of an umbrella opening on a motorcycle and... Mary Poppins, goodbye!""",
407170: """Jedi: They brought office supplies to our office, you know those bendy pencils? We broke two of them right away.""",
407171: """xxx: Yeeah, our Katya with her requests...
yyy: SELECT * ?
xxx: no, hers are more complicated""",
407172: """XXX: My sister works as a secretary at a school. She says - a directive came in. State the number of working staff at the school who from 2011 to 2013 a) will quit; b) will go on maternity leave; c) will die.
XXX: Zheka suggested drawing lots...""",
407173: """– To avoid doing laundry, I need seven t-shirts.
– A week? :-)
– Half a year. Plus one – for holidays.""",
}
