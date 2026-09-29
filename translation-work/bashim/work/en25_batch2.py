#!/usr/bin/env python3
"""Translate bashim chunk_25 batch 2 (input lines 101-200, ids 407174-407273)."""
import json

EN = {
407174: """xxx: Dad and I took apart the wardrobe. Suddenly, out of nowhere, a stash falls out of the "between-shelves" - 37 rubles. Though grandpa Lenin is drawn on the back. Dad sighed so sadly and carefully tucked the money into his pocket""",
407175: """xxx: "Senility, absurdity and humiliation"
xxx: Practically the three pillars for the God of Chaos.
xxx: Or some Evil Empire X)
yyy: Or wedding contests...""",
407176: """1: In her purse separately lie: the wallet and the money, the cigarette case and the cigarettes, the makeup bag and the makeup. Damn, I really do feel like this purse is used by two people.""",
407177: """sbp: Why do birds chirp under everyone's windows, but under mine they SCREECH?
Ursula: oh, Seryoga, and we've got a dove on the balcony in the mornings, it feels like it's puking
Ursula: grrrlggrrrl""",
407178: """Pas'ha: and today I was amused by a vegetable vendor in the underpass who was pitching his goods with the slogan "trading cabbage for cabbage\"""",
407179: """Straga: want a laugh?
Straga: when designing the station they forgot to make a toilet for the staff, and in the architecture they allocated a 2x25m corridor for the toilet, as a result you walk into the toilet and immediately run into the sink, you turn your head sideways and before you appears a 25m corridor and at the end of the corridor a toilet bowl gleams white
Straga: all that's missing is a sign "don't overestimate yourself - step closer\"""",
407180: """Today, sitting in the kitchen, my son's phone rings.

xxx: ...
Son: I'll tell you in a sec. I think it's Murphy's Law.
xxx: ...
Son: Don't mention it. Are you hooked on the stock market again?!
xxx: ...
Son: Fuuuuuck!!! You'd be better off drinking, less money would go!!!""",
407181: """Pirog: Fright is when your boss's phone plays the melody of your alarm clock""",
407182: """yyy: and you never wrote me fairy tales :(
xxx: and she doesn't take a year to reply to my messages either :)
yyy: you're mean((((
xxx: no, Yulya, deciding a guy is a loser and therefore running off god-knows-where without a word, then, learning he succeeded in life, starting to flirt with him again, that's not called "you're mean", it's called "I blew it\"""",
407183: """Her: Well, I'm beautiful, slim, a blonde )
Me: hmm... Can I get a photo?
Incoming file: My photo.bmp Size: 94.23 MB
File transfer request declined
Me: Fine, I believe you.""",
407184: """xxx: Dima, your car didn't used to beep like that before
yyy: It used to have gas before""",
407185: """Tech> It'd also be great if that very busy tone played when I call, not when they call me. Otherwise I'd hook up some "anti-ringback" for myself - to hear ordinary rings instead of all those melodies that the called subscriber likes, not me.""",
407186: """Mr. Muscle: my 4 years studying to be a programmer weren't wasted after all!
Mr. Muscle: today at the factory I produced 1024 kg of polyethylene film per shift!""",
407187: """today at my appointment I had a proper lad
I ask "how are you feeling now"
the answer - "like a lad\"""",
407188: """My brother's pregnant wife Svetka writes to me.
Svetka: Hi
Me: Hi Svet
Svetka: It's not me
Me: And I'm not talking to you.
Svetka: This is your unborn nephew Nikita :)
Me: Nikita, tell your mom to pull the keyboard out of #$@^%, it's dangerous for her health.""",
407189: """xxx: Today I saw women in the subway in red jackets with the inscription on the back: "for cleanliness SPS" (SPS - sanitary patrol service)
xxx: Thought of walking up and saying: "Da, NZ" - but thought better of it, sensing a trap""",
407190: """xxx: a registered letter arrived.
xxx: looking at me "are your parents home?"
xxx: I haven't shaved in a week, I'm married, I've been working for 2 years, and they keep asking for my parents!
xxx: maybe because I opened the door in a bathrobe with teddy bears?..""",
407191: """[Bird]: here's happiness!!!! not breakfast in bed, but dinner brought to the computer!!!)))))""",
407192: """Docker: well judging by the fact that she dressed not like a princess but like a jockey, she wasn't waiting for a prince but for a horse...""",
407193: """xxx: saw a blonde
xxx: harsh Rostov glamour - knee-high boot-sandals, a skirt from dad's tie, silicone tits, silicone lips and two little kiddies next to her, tiny ones...
yyy: also silicone?""",
407194: """xxx:
The older the year, the earlier the exams end.
Last year at this time I was still retaking the session.

xxx:
And this year I already failed it :'(""",
407195: """(talking about the punishment for not having a first-aid kit, fire extinguisher and warning triangle in the car)

xxx: If it comes to a protocol, then "disagree. Driving in compliance with the necessary precautions to purchase a new fire extinguisher and first-aid kit. Gave mine to another driver, didn't remember the license plate number".
yyy: and how can you use a warning triangle? Like Oh-shit!... forgot it on the road!
yyy: or placed it at the proper distance - turned around - it got swiped :)
xxx: yeah. and then from the frustration ate the whole first-aid kit and squeezed out the whole fire extinguisher""",
407196: """xxx: Our head of the client work department is a horrible woman. It's written right on her door: "Head of CWD".""",
407197: """X: The minimum income ensuring the ability to buy housing in Moscow currently stands at 230 thousand rubles a month per family.
Y: a trifle, it's enough to have 20-23 people in the family...""",
407198: """Irjik: the other day I realized what it means to pour too much oil in the pan when frying)))
It's when, as the oil boils, not only splashes fly out of the pan but also macaroni %)""",
407199: """Woland-rostovka: God gives to those who rise early, and those who don't lie down at all write their thesis...""",
407200: """comment on the new requirement for car first-aid kits now consisting exclusively of bandages

FELiX_Cat
Address: Khabarovsk
they should've added stretchers. get in an accident, bandage up, lie down on the stretcher and wait :)""",
407201: """Lord> Does anyone happen to have maps of Crete for a Garmin lying around?
Naevus> Afraid of getting lost in the labyrinth?""",
407202: """Max_Volk: I hate you
WooDy: I know
WooDy: it warms me
Max_Volk: damn
Max_Volk: then I wuv you
WooDy: I'm flattered
Max_Volk: DAMN
Max_Volk: then I'm indifferent to you
WooDy: I'm indifferent to it
Max_Volk: A THOUSAND DEVILS!!!""",
407203: """Mosquitoes grow strawberries in the forest, it's bait for humans, that's how they hunt :(""",
407204: """xxx: Scratching the cat's belly with the key to the newly bought apartment. The cat is happy.
xxx: Finally bought the cat a belly scratcher.""",
407205: """xxx: how can you fuck something that's constantly yelling at you?!
yyy: in the mouth?""",
407206: """designchief: so when our designer sends me "жопиздан" over ICQ, that means everything's ready
designchief: job is done, I mean )""",
407207: """from a forum:
xxx: please recommend some hunting simulator.
yyy: manhunt""",
407208: """xxx: why are you walking around like a drowned man?
yyy: more like, not admitted to the exams..""",
407209: """-Did you take your temperature?
-Not yet. Right now. Taking the thermometer. Going to smoke.
-What!? Don't smoke the thermometer!""",
407210: """magenta: When home?
del: the 10th
magenta: Packing? )
del: I'm not Gentoo, it's only the 3rd""",
407211: """<REal_SM> on friday a client complained to me that the antivirus doesn't work: it doesn't see viruses on the flash drive
<REal_SM> me to him: so what, is there something on the flash drive?
<REal_SM> him: well, they should appear on their own, right?""",
407212: """From a conversation with a tech support employee:
xxx: and the guy is proper
xxx: to the question about which OS, he answered - "whaddaya mean, a normal one\"""",
407213: """Leonid Alladin: don't know whether to scold or cheer you on, so good luck on the exam, go pass it with flying colors, you disgraceful loser!""",
407214: """xxx: Passed your exams? Give dad back his smack on the head!""",
407215: """* Gal saw in a store with all kinds of chemicals a certain container with the inscription "Gala cleaning powder" on it
* Gal ran away from there in horror""",
407216: """xxx: My cat fell off the balcony from the 4th floor the day before yesterday, took him to the vet, they took 600 rub from me for everything. yesterday he fell again, vet again, the doctors' bewilderment plus my flat joke "the parachute didn't open again" plus 600 rub for everything. TODAY THE BITCH FELL AGAIN.
yyy: maybe tie him up? :)
xxx: Yeah, by the neck, so he thinks next time. Turns out the neighbor's cat is in heat on the balcony, so he, the fucker-terrorist, wanted to get to her. Total: three dislocated limbs, six shots in three days and two thousand rubles gone to nowhere, saved for a video card""",
407217: """Photo where a cat has a computer mouse pinned in its paws.
Comment: Tell the cat that a plastic mouse is the first step to a rubber cat""",
407218: """forum.

user: why, when you ask a journalist what he does, do you so often hear - I'm a representative of the oldest profession, well after prostitution of course. And why exactly after prostitution? That's what bothers me...

moderator: Flooders are the oldest. Someone drew mammoths on walls, while flooders drew lots and lots of deer.""",
407219: """xxx: Nothing will work :(
yyy: Nothing is impossible! The main thing is to believe in yourself!
xxx: Bite yourself on the forehead.""",
407220: """xxx: remember, in basements there hang signs "Stop! You forgot to turn off the light!"?
yyy: yeah?
xxx: too bad there are no signs "Stop! You locked Stepan Mikhalych in!"...""",
407221: """xxx: After I fished ready macaroni out of the pot with a fork for lack of a spoon, I feel like I know each one by face.""",
407222: """Alesya: Good evening, Nikolay? How are you? Do you have an axe? I really need one!

Nikolay: Hi! No, I don't have an axe for such purposes! What happened?

Alesya: Tomorrow at 11:00 I need to email the commercial director the business plan for a patchcord manufacturing plant. I've only ever written business plans about media for tests and exams. And this one is real. I'm in absolute shock! And if I don't write it or write it badly, the investors won't give the money (and it'll be all my fault)! I need to include absolutely everything in it! Starting from choosing the location for building this plant and ending with deductions to pension and insurance funds. + all the equipment and raw materials for production, personnel, salaries, profitability, expected annual output.
How am I supposed to know how many patchcords are produced per year?! I only learned today for the first time that such a thing exists...

Nikolay: Stay home! Don't go anywhere! The axe is on its way...""",
407223: """ggg: moldy mushrooms. what irony!""",
407224: """<Кратос> God of War III is coming out on PlayStation 3 – Kratos is filled with furious desire to avenge the gods who betrayed him and the whole world of Ancient Greece.
<Кратос> because he doesn't have a PS3!!!""",
407225: """Arti: I remember a fairy tale about a fisherman's daughter whom the tsar asked several tricky questions - something like "what is fastest on earth", "what is softest on earth" - and the answers were so... philosophical.
Well, I know for certain what is heaviest on earth.
Your own ass. You just can't lift it.""",
407226: """xxx: and in school to the question "Darwin's achievements" I answered that he invented the monkey""",
407227: """xxx: and why did you leave your old job? shitty team or did they hold up your salary?
yyy: The shitty team held up my salary""",
407228: """<404_user_not_found>: I fell in love with a penguin,
<404_user_not_found>: Not all of him, just half
<404_user_not_found>: - The lower half,
<404_user_not_found>: Bright and lively )
<xyZ>: You banging linups too?""",
407229: """sunrise: listen, kitty
sunrise: if we run unfiltered beer through our filter - will it become filtered?""",
407230: """her: Tell me...
her: what do you need from a girl?
her: I mean, what's the main criterion?
her: like, she should be able to cook, or be beautiful, or smart...
him: you know, I'm exactly the case where they say "a man should be just a bit more handsome than a monkey"
him: and also a boring and tedious type.
him: and I work as a sales consultant.
him: and I still live with my parents.
him: so the main criterion for a girlfriend is that I have one!!!""",
407231: """xxx: City-wide concerts are bullshit. I realized we're really popular while rehearsing in a small garage in a bad neighborhood, when an alkie who dropped in for a light approvingly said: "All the bum-chicks are getting down to you guys!" """,
407232: """Sergey: I look at your avatar and it feels like you smoked one :)
Ekaterina: you know, for my stupid head it's not necessary! for THIS mug there are certain reasons
Sergey: Reasons? Mug? it feels like Salvador Dali photographed you with a homemade camera""",
407233: """Moscow region governor Boris Gromov offered, in a very original way, to ordinary Russians stuck in traffic jams on Leningradskoye Highway a solution to the problem of getting to their destination. Mr. Gromov advised everyone to buy helicopters. "I fly by helicopter. People should buy helicopters instead of cars - no roads needed," the servant of the people declared.""",
407234: """RiZN: today I laughed at Roma
RiZN: he's sitting at the comp and then a program pops up, "take a 5-minute break from the computer"
RiZN: he leans back in his chair, sits for like 10 seconds...
RiZN: takes out his laptop and starts working on it )""",
407235: """Question on a forum.

KoI8
Posted May 20 2006 - 00:35
Why are deleted, old threads stored on the forum??? what's the point?

KoI8
Posted July 02 2010 - 19:35
thanks for the detailed answer! :)))""",
407236: """my sister came from the hairdresser. she says, a man around 40 walks in. respectable looking.

man: do you pierce ears?
hairdresser: yeah
man: can you do it so today it's visible the ear is pierced, and tomorrow it's not?
hairdresser(after a minute of silence): what, lost a bet?""",
407237: """kontrass: I just did something unforgivably stupid
kontrass: I sat ass-first (without looking) onto a charger lying plug-up!
kontrass: my butt is now a blank-two domino""",
407238: """Justadreamer: here in the next room my sister's husband is putting the kid to sleep, singing "bayu-bai". He sings well, the bastard, my eyes are closing...""",
407239: """From mail.ru Q&A

xxx: What should I do, I wiped the screen with solvent, thought it'd increase the CPU's cleanliness? And it stopped showing!!!!

yyy: Doctor, I wiped my eyes with acetone and my scrotum with turpentine. Now when I pee I cry. And crying is exactly what hurts. I wanted to increase potency and get Superman vision, help!""",
407240: """xxx: he's always in headphones - hears nothing, sees no one
xxx: and after he bought roller skates, generally - hears nothing, sees no one, and can't be caught""",
407241: """horror movies forum

aaa: In your opinion what's the best horror movie?
bbb: The best horrors are the Japanese ones
ccc: A Nightmare on Elm Street, Helloween
ddd: The ballet "Swan Lake". If you see it on TV, you can shit a ton of bricks.""",
407242: """xxx: Occupational deformation is when they tell you "buy tulips" and you think "what does she need RCA connectors for?\"""",
407243: """1: How far is it to the ocean from your place?
2: Used to be 15 minutes on foot from home, now it's about 15 hours by car...
1: Low tide?""",
407244: """xxx: The hardest stage of disassembling a laptop is the stage "Fuck, I've already unscrewed everything, why won't it come off!?".
xxx: And this stage repeats at least 3 times (""",
407245: """<Paladin> should've taken a photo, showed us the pic
<f-n_P-N> Pal, I have a slightly different principle of action )
<UncleFedor> Pal still doesn't get the literal meaning of the phrase "pick up a chick" :)""",
407246: """xxx: driving around the city, the radio says today is World Kiss Day
xxx: so I, saddening to myself, think "sigh, there's not even anyone to kiss me"
xxx: ...and right then at the traffic light a Lada 9 "kisses" my rear (""",
407247: """<zeon> They say carrots help with diarrhea.
<zeon> I'm afraid to imagine how.""",
407248: """Me: It's so cool how in "Twilight" they came up with the vampire's skin sparkling like diamonds when he steps into the sun!
Anya: And in reality???
Me: And in reality, Anya, vampires don't exist...""",
407249: """Kisa: on half the room the TV, a big laptop, two small ones. soon they'll bring Sashka's comp with the home theater. we both have 2 phones each, on the desk my PS3 and a hair dryer, the camera, 10 chargers for all this tech and 2 more cables. internet and cable.
Kisa: but no socks lying around
Kisa: there's just nowhere for them to lie...""",
407250: """xxx: What are you doing?
yyy: Browsing the tracker, they're not posting anything new, just seeding the same old stuff
xxx: Which section?
yyy: USSR Cinema""",
407251: """lyzh: With these sick kidneys I can't have fatty, salty, sour, spicy, alcohol or sex. But whatever, once I recover, I'll cook myself a juicy steak, squeeze a lemon wedge on it for tenderness, season it with pepper, pour Jack Daniels sauce on it and fuck it!!""",
407252: """AmigoRico: Two days ago I really wanted to draw, took a sheet of paper and a pencil, and started drawing a portrait of a girl. Drew it, put it down and forgot. Today I met a charming girl on the internet, she's Russian but lives in the states now. Imagine my surprise when we were talking on Skype, she turned on the camera, and after a few minutes I saw a sheet on the floor, picked it up, and realized I'd drawn the girl I'm talking to right now...
jugf: draw money, moooney!""",
407253: """x: Nah, phones are nothing. I truly understood the purpose of reflashing when a surgeon left a clamp inside me.""",
407254: """Today I called the uni to find out if the payment went through.
A girl picks up, I describe the problem, and then she says "Galina Petrovna handles that, call back a bit later", "after lunch?", "no, better in a month, she's on vacation\"""",
407255: """<ONOTOLE> So when, say, you tell a marshrutka driver: At the stop, if you please!, how do you then check his emotional state? Was he pleased at the stop or not?
<pov> slam the door""",
407256: """- Ok, bunny. Going to sleep. Kissing, loving, hugging, purring and cozily sniffling in your ear :)
- Eeeee.... and why the sniffling in the ear?""",
407257: """XXX: How great it is on a hot, hot summer day to drink hot tea
NNN: in a room with air conditioning?""",
407258: """xxx: And it's all because at night cookies turn into belly""",
407259: """xxx: fly fly, mp3, through the gateway, through the host, through the routing, through the bridge. As soon as the server shakes you — earn, bitch fuck!""",
407260: """xxx: I can't live without music
xxx: with music I do everything three times faster than without it
yyy: and I always do nothing equally fast""",
407261: """Tanechka: just happened. The phone rings, I pick up as usual, they say connect me to the head of delivery, I say "Introduce yourself, please", he, with a sense of dignity "***ov", I start asking him what his business is and so on. He sighs heavily, listens to me and says "Tatyana, I understand everything, but I'm your commercial director" )""",
407262: """xxx: if I were an admin - I'd walk around in a suit, with a tie
xxx: respectable
yyy: I'd like to see you crawling under desks in a suit with a tie. I can just picture it: you lay a running system unit on its side, take off the cover, lean over it, the tie gets sucked into the cooler, your head gets pulled to the wildly spinning blades and the red-hot radiator, the smell of burnt meat, blood splatter, and you can't turn off the comp - the boss is playing solitaire
xxx: damn
xxx: well, a clip-on tie... so far I can only get my tie into salad
yyy: yeah. the end of the tie still hangs down and manages to get into the cooler anyway. The clip doesn't hold, rips off and stabs right in the eye""",
407263: """Tata: how do I get to some forum? for example, if I want to find something about canon or nokia or type "samsungЪ camera", I land not on a forum but on several dozen links to stores
Tata: and I don't need ads, I need objective info..
Misha: I always do it like this: I write "*** is shit". and then you get all the forums""",
407264: """xxx: something's wrong with me...
yyy: huh?
xxx: I really wanted blinis. but they're cold, so I poured boiling water into a metal bowl and put the blinis on top, they warmed up, I spread them with sour cream.
xxx: turned off the light in the kitchen, took them and carried them to the comp. Sat down, launched VKontakte, replied to messages, I look - there's a bowl of boiling water in front of me.
xxx: so I think - I took the water to steam my nails, but didn't bring the manicure tools. went and found the nail file and clippers
xxx: did my manicure, suddenly got hungry, went to the kitchen. and there's the cat on the table, finishing my blinis!""",
407265: """Today I heard in the admissions office:
someone's mom: Congratulations on the upcoming holiday
the admissions guy: Which one??
someone's mom: The day my son gets admitted to your university!""",
407266: """chook: today in Lineage our sysadmin came up to me. Said no gaming at work and killed me.""",
407267: """xxx: AAAAAAAA!!! Get me out of here! A woman just came in, sat at the comp, with a completely serious face took a PAPER HAT MADE OF NEWSPAPER out of her bag, put it on and works like that!!!!!!! What if it's contagious!!! :S
yyy: she's protecting herself from mind reading and brain scanning
yyy: and you're laughing at her, you're facing a serious person. Nanotech at the service of the people
yyy: maybe it's a nanonewspaper
xxx: yeah, you all joke around, I'm barely holding it in as it is :)
yyy: or a nanohat
zzz: a nanoblocker of nanothoughts
zzz: tell her "You're irresistible in that hat. France?\"""",
407268: """Riding bikes with my girlfriend, another cyclist coming toward us warns: traffic cops with a radar ahead. We ride on – the cops, point the radar at us and say: "Ladies, you can push it a bit, you're only doing 23 km/h!".""",
407269: """xxx: after watching "Twilight. Eclipse" I have only one question left: why does Edward always have that face, like he thinks he left the iron on at home but isn't quite sure, and the whole movie it's tormenting him...?""",
407270: """Sn1peR: my radius bone hurts
Arkodiy: replace it with an LCD bone""",
407271: """xxx: I was just thinking
xxx: The scholarship can cover my monthly WoW
yyy: Uh-huh
yyy: And a monthly WoW can ensure no scholarship""",
407272: """xxx: what's Kazakhstan's top-level domain?
yyy: ,kz
yyy: *fuck
yyy: dunno""",
407273: """xxx: looks like we finally found an apartment!
xxx: only the windows aren't great and the plumbing isn't exactly a fountain (
yyy: well it's even good that the sewer isn't a fountain :)""",
}
