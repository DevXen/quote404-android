#!/usr/bin/env python3
"""Translate bashim chunk_22 batch 1 (input lines 101-200). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
404168: """Got a business trip to New York, flew there with great pleasure of course. Checking into the hotel, I specifically asked if there were any Russians, so it wouldn't be boring. They said no. A meeting's scheduled for the morning, I'm getting ready, the room door is already ajar, I hear shuffling steps in the hallway and a hoarse voice:
- Oh, God... My fucking head... (dull thud) Damn, I'm already cursing in the infidel tongue, fuck it all, going home tomorrow...""",
404169: """Standing in the checkout line at the supermarket. In front of me a lady is ringing up a pile of groceries, the cashier's a woman of about forty, forty-five. And everything's so proper and peaceful. calm and without idiocy (if only it were always like this!), the customer is even handing her the discount card, and then the cashier blurts out:

- Eh, what a nice round total! Two hundred fifty-six rubles!

looks at the "discount card", bites her lip, swipes it through the reader and then so sadly:

- That'll be two hundred forty rubles...""",
404170: """xxx: read recently - a gamer's "asdw" is worn out, a programmer's "<:=->"
yyy: and only my down arrow is worn out
xxx: the one that's down?
yyy: uhh... UHH!!!""",
404171: """Our locksmith was absent from work for two days and shows up today, I ask him to write an explanatory note. Five minutes later he brings:
"_Did not miss_ work for two days because my stomach hurt"
I look at what's written, then say: Did you even read what you wrote? - and hand him the paper. He reads aloud: "Did not miss work for 2 days because my stomach hurt - what's wrong with that?"
- "Well probably you should've written "missed"?"
He's silent. Then "why are you nitpicking?"
And indeed, why am I nitpicking, I'll just tell the management that: that in a month he didn't miss a full two days""",
404172: """xxx: hey! how's it going? why so quiet?
yyy: hey. it's fucked... yesterday the department had a corporate party, I told you, in the evening we moved to a pub.
xxx: had fun?
yyy: today everyone's sick, silent, not a word about yesterday - half don't remember a damn thing, the other half remembers, but wishes it didn't, then the boss walks in (he left halfway through the party and didn't see anything terrible) and says, like, everyone write explanations (
xxx: and then?
yyy: so we all silently started repenting and explaining the reasons for our indecent behavior, the boss collected the papers and left. walks in 10 minutes later laughing like a herd of horses and says: "If only you'd read what you wrote here! I wanted to collect explanations saying that yesterday you all got food poisoning at the cafe, and I'd have let you all off, but what you wrote here changes things... now we'll work till we drop"
xxx: what a setup...""",
404173: """xxx: SIIIIILENCE IN THE FOREST
xxx: only the oooowl isn't slee-ee-eping
xxx: the owl's installing drivers on the videocard, that's why the oooowl isn't slee-ee-eping""",
404174: """xx: what's going on in your Yekaterinburg - horrible! Students of the equestrian sports school whipped their coach with horsewhips...
yy: that's nothing, they killed the tsar here not so long ago...""",
404175: """B9t: I'm thinking, what could I take from home to the seaside so everyone sees me and immediately goes "holy shit"
Kerdyapkin: take your skis""",
404176: """Sognator
Two girls standing there, one with a kids' magazine. Their dialogue:
- let me read your horoscope!
- ok!
- what sign are you?
- Tanya!""",
404177: """Anna: a breast is what fits in a palm!!! everything else is an udder!!!
DenG: I think I get it. so for example, a year-old hamster is a breast, and a wall clock with a cuckoo is an udder?""",
404178: """About the sphinx cat:
xxx: I came to Katya's yesterday, I say, show me the kitten. I turned away, talking to Lesha. Katya: Dasha. Dasha turns around: AAAAAAAAAAAAAA! I didn't know I could scream like that. Really got scared. I thought kitten: fluffy, small, whiskered. And such stress.. And then they tell me: don't scream, he's scared! As if I'm not scared...""",
404179: """Sitting on the balcony last night, smoking. Watching the scene: in the building across the way on the 5th floor balcony stands a woman yelling into the void something like 'Murzya, come home!' After a couple minutes from somewhere in the bushes comes a happy 'Meeeow!' and the cat starts answering her.
She: Murzya! You're found!
Cat: Meow!
She: Come home quick!
Cat: Meow! Meow! (Are you stupid? The doors are locked, am I supposed to climb the wall?!)
She: Come home, I'm telling you! Am I supposed to come down from the 5th floor for you?
Cat, pitifully: Meooow :(
She: FUCK!!!
And leaves the balcony..""",
404180: """dipus: need to quit diablo. tried to fall asleep, decided to count elephants. all would've been fine, but the sixteenth elephant turned out to have immunities to poison and lightning, so I had to kill it manually >.<""",
404181: """Wife told me.
As a kid, when she stayed at her grandma's dacha, thieves got into the habit of coming for the harvest. Usually came at night when everyone's asleep, and the nights in that region, I should say, are very dark. Unfortunately there were no men around with the family at the time, father and grandpa had left for work, so the women were afraid to fight the thieves off. So one evening grandma, admiring the sunset - fell asleep outside, woke up from strange rustling in the middle of the deep night. As it turned out, the thieves were a few meters away from her. In horror she didn't even dare move, so as not to give away her presence, until she felt someone's hand on her leg. In shock she could only utter one phrase: "I've been waiting for you here, you bitches!" They never came back to steal from that dacha.""",
404182: """Realized I'd found the "right" job when I asked my new colleagues what programs, besides the standard office ones and the internet, I'd often have to use at work, and got in reply: well, Solitaire, Minesweeper...""",
404183: """girlfriend: "As a kid I really wanted a little house and a Ken for my Barbie.. but we were poor, so Barbie lived in a shoebox, and she was fucked by some pirate ( \"""",
404184: """xxx: Harsh army everyday life.
xxx: Been serving on polygon duty for three months now, living on the watchtower.
xxx: No electricity for cooking for two weeks now, have to cook on a bonfire.
xxx: Truth is we have no axe, so we chop firewood with a shovel by hammering it into the log with a wooden block...""",
404185: """lisnake: Hate to admit it, but ever since I cleaned my room, the wifi signal changed from "weak" to "very good\"""",
404186: """yyy: I'll take care of you like a kitten :-)
xxx: Like, take out the litter box?""",
404187: """(from a programmers' forum)

Jaxx: recently started learning web programming, just can't get the difference between a redirect and a forward :(
Andronix: look, imagine you walk out to a kiosk to buy a bottle of vodka... and the saleswoman tells you the vodka's out and sends you to another kiosk - that's like a redirect.
Jaxx: and a forward?
Andronix: and a forward is when the saleswoman says she'll go look, and herself quietly slips out of the kiosk, runs across the road to the next kiosk, buys vodka there, then just as quietly comes back and resells you that bottle with a markup on top :)""",
404188: """From an airsoft forum:

Hard to feel like a cool special forces guy when your telnyashka after your wife's wash smells like "violet meadow".""",
404189: """<A> so what field are you working in now, what does the company do?
<B> doing a bit of programming
they do information security
<A> so there's no way to foist a used excavator on you?""",
404190: """A call with the Boss (B) about users' wishes for the program:
B: - Are they fucking out of their minds there???!!
Me: - I think so.
B: - You think so?
Me: - Well, I can't say it out loud in the office, so I'm just agreeing.
B: - Time to call things by their names! I understand the Petersburg director doesn't allow calling an ass an ass in his office, but in Moscow it's a must!""",
404191: """xxx: A wave of cleaning came over me...
xxx: It knocked me down and I fell asleep.""",
404192: """Necro_KOT: A guru at work told how he went to Ecuador with his wife.
Necro_KOT: Their preachers there just walk the streets yelling sermons, drowning out even the music in the car.
Necro_KOT: The wife stares at it with round eyes, and the preacher decided she was interested, came up and started preaching at her.
Necro_KOT: After like 15 minutes, the guru's wife, who after all barely understood Spanish, turned to a colleague and blurted "How he's fucking annoyed me!"
Necro_KOT: To which the preacher, unfazed, in pure Russian (!!!) blurted "Then why the fuck didn't you say so, you dumb bitch", turned around and left""",
404193: """Albina:
The director's gotten totally lazy - comes to me with a pile of documents and says: sign all this for me, you're so good at forging my signature

Albina:
And then pensively says - you're a dangerous person, Albina...""",
404194: """mymilk:
I throw trash where there are no bins, so that bins appear

dims_art:
Logic's fucked.
you guys should also take a shit so toilets appear.

From LJ.""",
404195: """Met my wife in the uni canteen. She was short five rubles for the cashier, and I was standing behind her, so she asked me for them.
I didn't know then it was only the beginning...""",
404196: """DimonS: he-he, our water-jet boat collided with a jeep
most: how's that?
most: in shallow water?
DimonS: the jeep drove into the sea, to wash up like
DimonS: and a drunk guy on the boat rammed into it
DimonS: the boat's fine, but the jeep's bumper, hood and windshield are totaled
DimonS: called the traffic cops, they laughed for half an hour, said - we're not involved, it's not on the road
DimonS: the water cop came, fined the boat guy 300 rub for being drunk and the jeep driver 2000 for washing his car""",
404197: """Toivonen> sewed a little veil
Cathody> Getting ready for a wedding?
Toivonen> yep. marrying off the window""",
404198: """Citadell:
I only watch the animal channel on TV

sositefse:
all the channels are about animols
except Discovery. that one's about beasts""",
404199: """XXX: Can anyone tell me if there's a specialized jammer for car alarms?

YYY: We had a goat with a hyper-nervous alarm, annoyed everyone. One morning, leaving for work, I put an open canister of gasoline on the roof of his Ten and a box of matches next to it. Now he parks in another yard.
I'm saying, the symbolism of combining ordinary objects gives a lasting effect""",
404200: """Deuce:
In a district school a primary teacher had to get certified for a category. Prepared for a month, wrote not a lesson but a whole fairy tale. Day "D", hour "H". Two ladies from the education department come, sit at a back desk. The lesson setup:
"Children, today we'll have a very interesting lesson. To make it really interesting, I invited two little monkeys to the lesson." With these words she climbs under the desk to get two plush toys. And the kids, as if on command, turn around to the back desk, at the "monkeys".

Deuce:
But she passed.""",
404201: """Beach. Along the seashore stands a whole street of tents with vacationers. Down this street toward the shops walks an exhausted couple, worn out by vacation - a boy and a girl a bit over twenty. And the girl says to the boy in a voice full of tiredness, slight resentment at life's injustice and love and tenderness for the boy: "Well how are we going to save up for port wine if we don't approach anyone?\"""",
404202: """The boss sent me his chat log in ICQ, don't know what to do (

Boss: A week ago we needed to deliver goods to a client, did you deliver it?
Uvolit 01.09(idiot): Forgot (""",
404203: """Black Cat: another wedding under the window, how much more
Black Cat: the limo will get stuck turning around by the dumpster again""",
404204: """From a site for expectant moms, topic: what's needed for a newborn:
(lots of info)...
...pierce the nipples (with a needle heated red-hot over a flame carefully burn a hole in the nipple so water drips out of it in frequent drops)...

And the stress?! Where's the stress?!!""",
404205: """Den: Wife and son went on vacation. Some hellish flies bred at home.
PZ: hellish - what kind?
Den: Lots of them. The rest is details.
PZ: fruit or some other?
Den: Don't know, didn't try them.""",
404206: """Elena:
Doctor says - possible brain tumor.
Whole family goes, gets an MRI. Shell out 10,000 rubles. Waiting for results.
Doctor comes out, says: All good, no tumor found!
Brother: Well, money wasted for nothing...""",
404207: """xxx: Congrats!
yyy: On what?
xxx: On Ecuador's Independence Day!!!
yyy: Oh, I thought no one would congratulate me...""",
404208: """xxxx: how much did Ilya Muromets bench press? and what was his own weight?
yyyy: Ilya Muromets benched about 200 Mongols""",
404209: """111: Studied to be a programmer, work as admin. The number of crooked-handed dumbasses whose mistakes I have to fix is already getting to me. It's just mockery!
222: You're telling me?
222: I'm a philologist :)""",
404210: """he
Have you read Nabokov's Lolita?
she
No.....what does she write?""",
404211: """in the smoking room:

accountant (hysterical): ...and anyway, I haven't seen your work yet, which means it doesn't exist!
admin (tired): and how many times have you seen your own brains?""",
404212: """<Ihtiandr> Employees of the Kemerovo drug control detained a man with two bags of marijuana.
<~KoveRs~> He wasn't storing it) He was just carrying it)
<Ihtiandr> So they won't put him in jail. They'll just ask him to wait a few years in a locked room""",
404213: """<Shinji> Merc, PP, mom told a mega-story from the village ) Grandpa complained his cats started losing weight for some reason, even though they permanently have two basin-sized bowls of chow by the shed. Two months later a three-kilo hedgehog was caught in the shed, which was devouring all the cat food and chasing the cats themselves away from the food )""",
404214: """V@n4o: today I attained zen
V@n4o: walk into our admin's server room
V@n4o: and, I should say, our Romych is a typical admin - beard, sweater, beer in moderation
V@n4o: and since we all work in the territorial administration, with his job title and workplace names he turns out to be the king of kings )))
V@n4o: but that's not the point
V@n4o: so I walk in, standing there, we're talking, he's sitting at one of the servers and in parallel with our conversation doing cleaning-tuning
V@n4o: and then from the neighboring comp comes the sound of a mechanical alarm clock followed by the characteristic sound of a hammer on a rail
V@n4o: he looks at the window that popped up there - ok, that's it, lunch
V@n4o: without getting up, he reaches under the table and pulls out OF THE SYSTEM UNIT A SMOKING METAL PLATE OF SHCHI
V@n4o: calmly starts eating
V@n4o: and the comp's working!!! O_o
V@n4o: I lost my speech, was in a stupor
V@n4o: and it turns out he put an electric hotplate in an empty old system unit so it wouldn't be seen and wouldn't get in the way
V@n4o: but I didn't get it right away
V@n4o: I peek in, and there's also, fuck, an electric kettle in there
V@n4o: and his working system unit turns out to be behind the curtain on the window
V@n4o: and when the characteristic toaster sound came from the printer side, I just abruptly bailed so as not to traumatize my psyche further
V@n4o: the toaster, as it turned out, was in a closed cabinet (!) but that doesn't make it easier for me
V@n4o: I even imagined him making salad in the shredder""",
404215: """From a dating site:

helo beautiful creacher....want me to give you the sweetest and most tender cuni and treat you with shampagne...tenderly and passionatly take you to the seventh heaven of fairytail bliss..not a gigolo with a good body and manhood..have a place to meet..my photo in the misc folder where the moderator moved it..with my sincere respect sergey...photo in the misc folder..the moderator hid it""",
404216: """Mi-1 disasters happened for various reasons, but several cases were tied to a technical feature of its design: the helicopter cabin's entry doors opened against the flight direction. When the pilot cracked the door open in the air, for example to flick a cigarette butt out through the crack, the oncoming airflow flung the door wide open. At that, the pilot's fingers involuntarily clenched the door handle, and the swinging door hurled the pilot out of the cabin.""",
404217: """Kristya (23:20:34)
Tomorrow we'll look for a swimsuit

Redric Shuhart (23:20:57)
might as well look for a needle in a haystack

Redric Shuhart (23:21:18)
we know hay burns and needles don't ^_^""",
404218: """At our institute there were three friends Seryozha Sidorov, Sasha Ivanov and Valera Petrov. Went for beer once. Overdid it. Ended up in the drunk tank.
In the morning they come to class all gloomy, and Seryozha with a broken nose too.
Here's his story:
- The sergeant asked our surnames, so we answer - Ivanov, Petrov...
- Well, and you're Sidorov of course?
- Sidorov.
- Have it in the kisser!
- Had to introduce myself as Rozen, Kyukhelbeker and Naryshkin...""",
404219: """Discussion of Swedish speakers for 5 million $. (like the trendiest)
1: wtf is in there
1: unless the speaker coils use gold wire instead of copper
2: can't figure it out myself, the Swedes have been acting weird since the Battle of Poltava. must still be in shock""",
404220: """Damn, mine left for the village, and I'd been wanting to clean my room for ages, took everything, tidied up, vacuumed, beauty. Then mom comes and starts yelling at me, like I brought girls over and all that, I'm standing there dumbfounded.
-I didn't bring anyone!!!
And she:
-I couldn't get you to clean for a month, and now we leave and it's clean, you definitely brought a girl over!""",
404221: """MaRmElAd'Ka: Tell me something, please... Interesting!!! I wanna chat so bad!
* Phantom *:) and what should I tell about?
MaRmElAd'Ka: Anything...
* Phantom *:) they're loading my dump truck with eggplants,
and I see they're all with tails. why do I need a dump truck of eggplants with tails?
so I say unload and cut the tails off all the eggplants. so they unloaded, did it, loaded it back.
MaRmElAd'Ka: How funny... Had to come up with that!!! Where do you work, study?...
* Phantom *:) and I just hop into the dump truck! when suddenly it starts raining, fish rain! and then mushroom rain,
and as you know eggplants go dull under mushroom rain, so I start hiding the dump truck in the strawberries
* Phantom *:) I work )))
MaRmElAd'Ka: That's not even the end..?
MaRmElAd'Ka: where?
* Phantom *:) (01:41:27 12/08/2009)
I'm telling you, in the strawberries,
where else can you hide a 7-ton dump truck from mushroom rain?""",
404222: """xxx: just had a fight with my husband.
xxx: he, you see, wanted to go to sleep, and in his bed there are two kids, three books and a calculator""",
404223: """1: hockey players should be banned from wearing black hooded clothes!
2: justify
1: coming home late yesterday. And walking toward me - black hood, darkness under the hood, a scythe on the shoulder. By the time I made out in the dark that it was a hockey stick, I almost had a heart attack! (""",
404224: """Elessar
how do I remove all highlights in a document with javascript?

torkvemada
document.pollutions.remove()
and stop leaving your discharges in the document anymore )""",
404225: """Me:
- Imagine, decided to buy a pack of beans in reserve, for a rainy day; bought it, open the cupboard to hide it - and there are already two identical packs hidden there!...
Husband:
- Heh. Now you've got reserves for three rainy days :)""",
404226: """tusur>
heard on discovery: we're in Alaska, it's a dangerous and harsh place, because from here to Russia it's 80 kilometers...""",
404227: """Senya: Surely there are lots of kids' products that can be adapted for adults. Like that book where you brush water and colors appear. Could put naked chicks in it.
Pasha: Yeah you could do that with all kids' products :)
Senya: Yeah right! What would you do with markers?
Pasha: Well special markers that only draw naked chicks. Otherwise they don't draw.
Senya: Damn, they kept selling me those as a kid :(""",
404228: """Amdi
So the other day a very handsome biker invited me to a restaurant, and I said I have a boyfriend...

Stray
And recently some girl left me her number, and I also told her I have a girlfriend..

Stray
So we're both losers, basically""",
404229: """Our director is named Yuri Yurich. And today he promised to leave for another city on business for the whole day. Everyone was already happy, imagining how they'd start morally decomposing at work. But he didn't go anywhere. Only one thought ran through my head: "Well, grandma, so much for St. Yuri's Day"...""",
404230: """Alyona:
I fit into the dress they sewed for me 5 years ago )

Alyona:
now I can't get out of it (""",
404231: """Riding in a taxi. Quietly, calmly the radio mumbles, sometimes interrupted by the hoarse radio with phrases like "this is 17th, heading there". The radio, as far as I can tell, is broadcasting about the city's historic places and the city's history itself. The announcer's voice is well trained, generally pleasant to listen to - not like modern DJs :). We're standing at the crossing, so the radio became easier to hear. Suddenly through the static breaks someone's yell, sounds like from the taxi drivers:
- Lyokha, turn off the mic, all the taxis can hear you!
- Mine's off anyway, - the "announcer" calmly parries, practically without interruption continuing. - And now we're passing our famous sewing factory, unfortunately...
Then I stopped hearing - the train arrived.
The driver spat his cigarette butt out the window, grinned:
- Our local celebrity, used to work as a translator and tour guide at the local history museum. Quarreled with the director, left. Now drives a taxi. Tourists love him. Even more than the museum.""",
404232: """xxx: going offline for 5 minutes, gonna wipe the keyboard, it's sticking to my fingers""",
404233: """LoL: Were at a cozy cafe the other day. Tried a dish called "Windmill". The deep meaning of the name revealed itself in the morning...
Kenelm: Like what? :)
LoL: Well, say, have you ever tried to sh*t out a windmill?.. :((""",
404234: """ViT: I "petted" another jellyfish in Yevpatoria like that, then regretted for half a day that I scratched my nose with the same finger
Dr.Freez: and I liked biting my nails
Dr.Freez: my back hurt once, so I rubbed it with Finalgon
Dr.Freez: and whether I washed my hands or not, it stays on them
Dr.Freez: so I bit my nails
Dr.Freez: Chinese cuisine can't compare
Dr.Freez: ran around the house with my mouth open
Dr.Freez: took a sip of water - got even worse...""",
404235: """Dantes: More and more often the free SMS "call me back" gets answered with a free SMS "top up my account" :(""",
404236: """from an emigrants-in-Germany forum:

1: Please tell me, can they take my kids away if my account is minus 5 thousand and I get money from social services?
2: Joining the question. I have +25,000 and social services aren't in the picture, won't they bring me someone else's kids?""",
404237: """xxx: Why won't you go see her in Tolyatti?
yyy: Because I have bad memories associated with that city.
xxx: So what? Half the country has unpleasant memories associated with it""",
404238: """xxx: Today the IT guys were aligning a satellite dish using a kid's compass, I was rolling on the floor laughing
xxx: Then I stopped rolling, they aligned it...""",
404239: """Stas> saw your dad in your car today, did you swap?
TiMoHa> Yeah there's a story. We went on vacation, I took dad's car, his is a minivan, fits more people and bags, and anyway on the way back we blew all 4 tires, 8 holes in each.
TiMoHa> Dad didn't know I took his car, he was on a business trip. When I got home, he asked why so long, I told him about the tires. He laughed so hard he almost cried... Then I told him I'd driven his car...""",
404240: """xxx: I watched a movie and concluded you're a scoundrel!
xxx: rather, that all you guys are scoundrels!
xxx: but everything's fine at the end, so I forgive you""",
404241: """Evgeny: Vilnius is divine! At 4 AM I was sitting in the middle of the road yelling at the top of my lungs that I'm not leaving here. The police came, put 2 cones around me and left...""",
404242: """lena
when the laces on black boots are white, it means that skinhead already killed someone with those boots. like notches on a knife, yeah.

VadeG Myuller
I have black skates with white laces! O_o

lena
how maany I've stabbed, how maany I've slashed...""",
404243: """A friend's monologue:
"Once I came home drunk - said something wrong to mom, she got offended, then we argued more. But I come home stoned - smiling, good mood, cheerful. Ate well everything mom cooked - means it was tasty! Mom's happy.\"""",
404244: """Recently rode a parachute at the seaside, you know the one launched from a boat, and wanted to unclip the carabiners and jump down - get an adrenaline rush. But then I remembered that trick doesn't work in Far Cry and changed my mind :)""",
404246: """<dimonishe> Illiteracy is just fucking awful! Especially the soft sign in verbs. It seemЬs! I'd kill )
<ping_ving> КМПВ, berserks will be driven into battle rage with verbs written exactly like that.""",
404247: """Alamaru: nostalgic for the pager times. You'd call the paging service, introduce yourself with someone else's name, give some bastard's number and ask them to repeat all messages from the last four-five months. Wild times!""",
404248: """she: and, is it a good car?
he: well, once I buy it I'll give you a ride, once I get my license. if you behave :-D
she: and if I misbehave - will you give me a ride before you get your license?)""",
404249: """WolanD> Eh, I love how long sentences sometimes get cut with an ellipsis. Right now on Yandex in the TV guide they're obviously advertising a good movie and the channel it'll soon be on.

23:40 Assholes from... NTV-Petersburg""",
404250: """xxx: Lyokha, tell me a fairy tale
yyy: Weeell... in a faraway kingdom, there lived a boy, and everything was just terrible for himфывафыукрдлиорывпоьрпвАРУроУАЫВЮЮ
xxx: that bad?""",
404251: """Overheard in the park, said with a tease:
- ...the advantages of the socialist system? Remember the cartoon "Moydodyr"? What was the water like there? Your Oblivion, Pasha, doesn't even come close. And your Vomgla, Petya, neither.""",
404252: """Poll: Now THIS I understand, 21st century news on a state channel:
1 a dry cargo ship went missing - people track its coordinates on a website and take screenshots as proof it was found.
2 fighter jets collided - they show the pilots' audio negotiations, with a RADIOSCANNER ru copyright!""",
404253: """xxx: Was recently at a friend's wedding
xxx: The parents with the groom and bride went into a room to bless the newlyweds, the guests stayed behind the door
xxx: Someone in the crowd asked: What are they doing in there?
xxx: Someone immediately answered: They're buffing them!""",
404254: """talking with a classmate:
me: so you probably programmed all summer?
her: no, barely. Only in the evenings.
me: well, evenings are fine too. I already forgot how begin and end are written!
her: {}""",
404255: """xxx:
I hear the TV working in the next room. I listen, and there's Putin pontificating. You know how he likes, with emotions, with feeling. And suddenly, when he gets to the words: (with rising emotion) "it's just..." - there's a beep, like they usually bleep out swears. Beeped for like two seconds and Putin's voice again. I'm in extreme shock, thinking, well, we've come to it, in our country the PM is already swearing on TV.

xxx:
Turns out it's mom, who switched to another channel to not listen to Putin, and hit the maintenance break right then. And so she switched back to Putin.""",
404256: """discussion of Kurt Cobain's death.
above it was said that no fingerprints were found on the shotgun he shot himself with.

Mrats | 2007-07-14
Actually the fingerprints thing is true! I think Kurt shot himself wearing gloves and then just took them off...""",
404257: """VampirBFW
Nope, today a black guy came to the warehouse - such a solid fellow, about 35
Standing there, receiving goods, and then across the whole warehouse starts playing the song
"Ay yay yay yay they killed a black guy they killed
Ay yay yay yay for nothing at all"
Etc.
He takes out his phone, the screen says "WIFE"...""",
404258: """The legend that man can't live without work was invented by those who never worked, for those who never rested.""",
404259: """Aerojet (13:18) :
I have a pattern already: every morning I put 500 rubles in my wallet, and in the evening there's 100 rubles more in the wallet than the previous evening! Try it! It's a real way to earn money!""",
404260: """fff: My upstairs neighbors were distilling moonshine in the kitchen all day yesterday and by evening apparently their still couldn't take it and exploded!
fff: And I got flooded!!!
fff: At first I was even confused about what to do...
fff: Where to collect it )""",
404261: """rode the train. Got insanely "lucky" - ended up in a platzkart compartment with three demobilized paratroopers. Thought - that's it, they'll get drunk and smash me into the wall. So the whole way I lay on my spot on the top bunk. The guys paid me no attention and drank. Finally their strength ran out too and they went to sleep. I was about to peacefully doze off too. A wild crash woke me. Turns out one of the paratroopers decided to go about his business and jumped up like at reveille. But he didn't account for the top bunk. He smashed his forehead into the 3rd bunk and "went" back to sleep. A minute later he jumped up again and again slammed into the bunk at full swing. The calm voice from below finished the rout. "Kolya, you're a blockhead. Imagine you're in a BMD and dismount crouching"

P.S. the guys turned out quite adequate and on the second day we were already drinking and eating together""",
404262: """xxx: I have old trusty Kliver-100 speakers (like S-90). soldered the amp myself for the matching power.
hooked up to the comp and controlled from the phone via bluetooth.
in our hole, few people use the internet, let alone such a remote control system.
so. I'm blasting some heavy shit at full volume, changing screws in the open window. a typical gopnik walks down the street with an unlit cigarette in his mouth. and apparently asks for a light. so for accuracy (just in case) I take the phone from the windowsill, pause it, ask again.
and I see the gopnik's cigarette fall out of his mouth with insanely round eyes.
he mumbles something incoherent. after a few seconds he waves his hands and quickly walks away, often looking back.
I, as if nothing happened, unpause.. then he just started running.
yyy: Aren't you afraid they'll whack you now for the super phone? :)""",
404263: """Talk about washing machine dimensions:
Girl: by the way, measure the width and depth there, oh!
Boy: ok, I'll measure, only depth of what?
G: well, the machine's maximum depth
G: there's height, width and depth
B: that's length
G: for girls it's called depth!""",
404264: """...cryptohistory of Russia as England's cryptocony. Like, since 1905 to this day we're ruled by the Crown's colonial administration.

That's how many alternatively gifted people there are around.
I remember, for fun I tried to calculate (based on the versions I know) who Lenin worked for and how much they paid him in total. Turned out it's easier to count who Lenin DIDN'T work for, and the sums came out so astronomical that you could just buy up half of Eurasia, and with the change carry out total electrification.""",
404265: """Koul: What do you even understand... Getting caught red-handed is when you split a peach. Exactly in half. Right in front of dad.
Koul: With your tongue.""",
404266: """xxx: Yesterday evening I was just in a good mood, started messing with my mom. She was shocked of course, asked "are you stoned?".
xxx: I say "want me to give you what I'm hooked on?". she says "yeah". I go to the kitchen, make a pill out of bread crumb, and give it to her.
xxx: She takes it, throws it into the bathtub and now thinks I'm a drug addict. (""",
404267: """xxx: why did you leave your husband home alone? he'll be drinking beer all day, bringing friends over!
yyy: I want to defrost the fridge; someone needs to eat everything up""",
404268: """xxx: damn
xxx: I want to hear dial-up
xxx: haven't listened in a long time
xxx: miss it""",
}

def main():
    src = open('chunks/chunk_22.jsonl', encoding='utf-8').read().splitlines()
    batch = [json.loads(l) for l in src[100:200]]
    assert len(batch) == 100, len(batch)
    ids = [r['id'] for r in batch]
    assert set(ids) == set(EN), (
        'missing:', sorted(set(ids) - set(EN)),
        'extra:', sorted(set(EN) - set(ids)),
    )
    nl_bad = [r['id'] for r in batch if EN[r['id']].count('\n') != r['text'].count('\n')]
    assert not nl_bad, ('newline mismatch:', nl_bad)
    out = open('out/chunk_22.jsonl', 'a', encoding='utf-8')
    n = 0
    for r in batch:
        en = EN[r['id']]
        assert en, r['id']
        out.write(json.dumps(
            {'id': r['id'], 'ru': r['text'], 'en': en},
            ensure_ascii=False) + '\n')
        n += 1
    out.close()
    print('appended', n)

if __name__ == '__main__':
    main()
