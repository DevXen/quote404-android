#!/usr/bin/env python3
"""Translate bashim chunk_22 batch 0 (input lines 1-100). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
404068: """vetR@g: Vaska, the crafty bastard

vetR@g: the moment a business trip looms for him, he starts meeting girls online from that city right away, chats them up properly, and at a convenient moment tells them a trip to that city just "happened" to come up

vetR@g: solves four problems at once - lodging, food, women, and saving money""",
404069: """Neka-chan: Reading a news headline like "Russia's missile-and-nuclear fleet to be built in 40 years", you can't help feeling like you're playing "Civilization".""",
404070: """Valet2: Once in the academy's hallways there were students standing around, shaking before their exams. We decided to recall all the bad omens and go scare them.

Idiots roam the floors: two carrying a pane of glass that's about to break, one wandering around with empty buckets, one spilling salt...""",
404071: """xxx: you're eating my brain
yyy: no wonder I'm always hungry""",
404072: """Anri
Dinus', a wedding happens once, and if you've decided to do it, with no money you could've waited.

Dinara
who says it's once? I hope I'm not jinxing it, of course...

Anri
silly, it SHOULD be once, and you live happily

Dinara
I was at a wedding in Samara once. When the groom was buying the bride a wreath, the girls making it were scolding the groom for throwing the bouquet together in a rush. Like, on the way to the registry office. And he said he'd done the same thing last time. You should've seen the salesgirls' faces.""",
404073: """Shpionych: remember the scrapers in printers, stapler staples, coins?
Zynx: and? a new masterpiece? =)
Shpionych: Well that's all nothing. In [Deleted], Anton pulled a wooden COMB out of an HP 1022. They were complaining it "cracks loudly when we turn it on".""",
404074: """xxx: these modern games have demands like terrorists.""",
404075: """xxx: So, how's uni going?
yyy: Total ass, had a pile of tails, closed what I could, the dean helped with some, they turned a blind eye to others... anyway got moved up to the next year.
xxx: Yeah, your situation reminds me of a card game.
yyy: What do you mean, what game?
xxx: It's called "Passing the Fool" )""",
404076: """xxx: the worst is when in your own program you accidentally find an obvious bug, fix it, and afterwards the program stops working""",
404077: """Hobbs: those 24 hours to Kiev just flew by
Hobbs: ate-movie-ate and sleep )
Hobbs: and an hour in ICQ.
Hobbs: that's it
Panda: the modern hamster""",
404078: """xxx: I can wish the Russian Railways management one thing
xxx: may you take a shit through those turnstiles!
xxx: all at once through a single one!!!""",
404079: """A new dance has appeared: the Teutonic! You do it like this: in winter you put a bucket on your head, go out onto a frozen lake and dance until you fall through the ice.""",
404080: """Mr. Smith: My computer is already six years old! Next year I'll send it off to school )""",
404081: """xxx: Tell me, how do I connect a laptop to a TV wirelessly, with what equipment, to watch movies?
yyy: Duct tape... Right in the middle of the screen.""",
404082: """Silver Fork: You know, from a goth in full war paint I expected to hear anything.
Silver Fork: Anything but the phrase "Hello, I'm an Oriflame sales consultant...\"""",
404083: """xxx: husband is bathing the kid, I go in to check how it's going
xxx: a rubber piggy toy got into the drain, the water's up to the kid's neck!
xxx: I say, get the piggy out of there, quick!!!
xxx: he starts pulling out the daughter...""",
404084: """zzz
we were adding cameras to the surveillance system. a side job came up. one guy installed them and ran the wires, and I only had to hook them up to the comp
zzz
yesterday we tried everything - no go
zzz
he says he ran power to the cameras
zzz
the cameras have power. the comp has no signal
zzz
turns out that while he was running the wires, he barked at the cleaning lady. and someone went and told her "go stick some needles in his antenna cable" and that dummy ruined the whole coax
zzz
I hope I'm not the last one to have seen that cleaning lady alive. if I were the installer I'd have killed her
zzz
good thing the first 11 cameras were run and hidden right away. he only has to replace five wires )
zzz
the main thing is, she didn't stick the needles in one place. the same cable in several different spots. and she bit every needle off on both sides with cutters, so you couldn't see them
zzz
for a sabotage squad she'd be a valuable specimen
zzz
if she were a granny, I'd bet my head it was her who sent fascist trains off the rails
zzz
and she wasn't too lazy to run around the building with a stepladder (or whatever she was climbing on)""",
404085: """lithium: No, all this Yandex Direct stuff is great, of course... But ever since I once looked up how to spell "spackle", it's been offered to me on every other site for a week now!""",
404086: """tim (Timur): We're coming to your place :)
tim (Timur): There's 6 of us :)
Shurik: who?
Shurik: I mean, what the fuck for?""",
404087: """Fox: and yesterday I went to bed today again""",
404088: """from a small town forum:

Nehiliy 13.07.2009 - 08:52
need a carpenter urgently, got two side jobs. call xxxxxxxxx
price negotiable.

Nehiliy 27.07.2009 - 08:47
looks like carpenters don't have internet...""",
404089: """me: buy bread
me: I want to eat
me: please
me: buy bread please
dad: I'll ban you!""",
404090: """Marusya ***: How's the business trip?
Runa: Fine, a couple days of solitude and I'm home again! Know what I'm doing?
Runa: I'm gonna check what pages my sweetheart visited while I was gone :)))))))
Marusya ***: Porn? )
Runa: Damn... worse... don't even know how to react...
Marusya ***: Child porn? Zoophilia, necrophilia??? :)
Runa: Every day... all sites on the same topic....
Runa: Looks like it's for medics... Like a young pathologist. :( About how to dismember corpses... with detailed descriptions...
Marusya ***: You haven't been cheating on him???
Marusya ***: Hey! don't scare me!
Marusya ***: Call the cops?
Runa: Oh! He's such a sweetie!!!
Runa: He made me a romantic dinner!!!!!
Runa: Poor thing... he spent 2 days getting ready to carve the duck!
Marusya *** (28.07.09 00:05) : Nah... I'll only leave sausage in the fridge for mine...""",
404091: """Me and my partner are sitting at our comps, we're admins
An accountant walks into the department, says "Seryozha, I'm reminding you" and leaves.
A couple minutes later Seryozha, completely unfazed: "She's been reminding me for two weeks now, only I can't for the life of me remember what about\"""",
404092: """iBrain: how do you tell a programmer from a gamer?
iBrain: a gamer's "asdw" is worn out, a programmer's "<:=->\"""",
404093: """from a discussion on one of the sites, "Reasons for job application rejections"

After uni I spent a long time looking for a job, got rejected in 3 places for "girl, you're not for us, try a modeling agency", after that I dyed my hair brunette, put on a sweater and didn't do my makeup, found a job in a week. Thanks for the truth!""",
404094: """A dialogue at a savate (kickboxing) training between 35-year-old women and the coach:
T- It's very hard to kick a living person in the stomach...
W- (with the most serious face, getting ready to memorize the sensei's instructions)- What, does the foot slip off?
T- actually I meant it's a pity""",
404095: """I work at the water utility, once they sent a crew to fix a leak in the water main.
Arrived at the site - a "spring is flowing", a wooden cross installed over it and people collecting water.
Anyway while we were digging up the pipe we heard from the grannies what villains and antichrists we are.""",
404096: """reine de chaos:
Yesterday we were out walking with the girls and met spherical gopniks in a vacuum. They stopped us in the middle of the street to ask for a mirror, because one of them needed to fix his cap.""",
404097: """sablin522: If only our people's persistence and energy were channeled into peaceful purposes!
Saw yesterday evening: two guys standing on a balcony smoking,
then one turns to the other with a "watch what I can do" look and throws his cigarette butt,
it flies three floors down, lands on the store awning below, rolls down onto the payphone booth
and bouncing off it lands right in the trash can!""",
404098: """Blizzaga: Yeah, healing there happens automatically.
Saphiroth: Marin, there are things I'm simply afraid to picture...""",
404099: """from a forum:
Folks, help me out, don't know what to do - turned on the comp, Everest showed me the HDD temp at 24, but it's +28 in the apartment. How do I heat the HDD to the right temperature? I'm worried...""",
404100: """I wonder, can I as a doctor forcibly examine a traffic cop and a bailiff for venereal diseases, especially dangerous infections, and send them to quarantine for 40 days? And then apologize and say there was a mix-up with a namesake.""",
404101: """xxx: Sanya, I wanted to search Google Images for something clubby, and there's a bunch of pictures of some unshaven grandpa with a cane, what's that?

A.Z.: let me guess. You googled "house"?""",
404102: """Antitanic: In the late 90s, working at an ad agency, I proposed launching a soap containing a substance that, on contact with water, would release a gray impurity. Then we could sell it saying: "Look how much dirt our soap washes out, unlike the others!"
But the project wasn't accepted - instead they tasked me with writing an ad for cedar parquet that kills moths...""",
404103: """MadPunk:
Yesterday near the end of the workday I did something in 1C. now me, the 1C programmer and the boss are sitting here trying to figure out what and HOW I could've done that. they said they're going to file for a Nobel prize, provided I can repeat it...

MadPunk:
I wonder if I'll get my salary? *scratch*

Nastenka:
As a Nobel prize.""",
404104: """A question from a property management forum:
Please name the employee who today sent your workers to mow the grass at 8-30 in the morning with gas trimmers under the windows of Solnechnaya 10 and 12. And while you're at it, please report the results of his IQ test. Thank you.""",
404105: """Malenkiy: Hi, let's get acquainted!
Evgenia: Sorry, men with that nick don't interest me. :)""",
404106: """xxx > Fuck, I'm shocked at our people.
xxx > It's no secret that in the former Soviet space it's very trendy to steal free sand from kids' sandboxes.
xxx > And in our yard they came up with a way to fight it.
xxx > They made a sandbox with a lid and a lock!""",
404107: """xxx: Turks are a strange people. left my phone and a condom on the nightstand in the room, come back - the phone's there, the condom's gone.""",
404108: """xxx: today I watched - a woman standing at the ATM and diligently covering the buttons on it with her palm, dictates OUT LOUD to her daughter, who is diligently pressing the buttons, her PIN code.""",
404109: """Opiy:
My users just delight me, it's something.
- everything's gone!!
- what's gone?
- everything's gone!!! ... hmm. no wait. it's back....
- what's back?
- everything
and hangs up.""",
404110: """<Beaver{rRr}> "Also notable is the case when five Russian tanks' radios stopped working and, so as not to get lost, they fired at each other from their KPVTs."
<Beaver{rRr}> that's just fucking awesome""",
404111: """Andrey:
Just got back from the bank, and there are gangsters sitting in the office :)
We owe the concrete plant a measly 550,000, and they sued us and sent thugs in parallel )

Andrey:
"We came to talk, strictly business," they say. And clink their gold chains.
It honestly brought a nostalgic tear - early 90s, I swear!
At first I thought my friends had pranked me and sent clowns :)
Pity I had no camera at hand - what material, gone to waste!

Andrey:
Such cuties...""",
404112: """xxx: Vanga's predictions are in there
yyy: what's she predicting?
xxx: a third world war in 2010, in November
xxx: chemical weapons, the whole deal
yyy: so maybe don't put up the wallpaper?""",
404113: """A friend scanned a handwritten development for a lab on integrated circuit manufacturing technology. The handwriting's illegible, he called his mom to help figure it out. They're sitting there, she's deciphering, he's writing it down, when an unfamiliar girl pings him on ICQ:
- Hi! Let's get acquainted!
- Hi, I'm busy.
- What are you doing?
- Doing homework with mom.
- How old are you?
- 20
The girl goes offline.""",
404114: """Comments on the post with System Administrator Day greetings:
1: Hurraaay! and it's Friday!!! What a coincidence!
2: Yeah, the last Friday of July usually falls on a Friday ;)""",
404115: """xxx:
anyway, at work our offices are next to the warehouses, and from those same warehouses dogs sometimes run into our offices, and of course we chase them out.

xxx:
Yesterday the boss's wife came to work with her 5-year-old daughter, walks into the transport department and with the door open leans down to her daughter and says "Come on, don't be afraid, come in!", and the girls in the transport department hadn't seen the daughter yet and thought the boss's wife was calling a dog, and they all burst out in chorus, loud: "like fuck I'll come in, bitch".
Poor kid....:)""",
404116: """Borman
Damn. I'm a master of formulations. Instead of "Lost the hardware", I wrote: "Some of the devices declared in the LAN equipment specification have not been detected by engineers to this day, which complicates the analysis of the network architecture.\"""",
404117: """Saphiroth:
And my dialogue with their little secretary went like this:
- Can I book a tanning bed for 3 hours?
- Only if you're firmly convinced your girlfriend values a good shashlik more than her boyfriend.""",
404118: """- from a thread about commuter train ticket prices -
Yeah, the best evidence of low salary levels is people with "iPhones" running from the ticket inspectors :)""",
404119: """xxx: We're for safe USB drive removal!""",
404120: """xxx: In Russia the phrase "Funds will be credited within 24 hours" means "no earlier than in 23 hours" (""",
404121: """A colleague who's into mountaineering ignores elevators on principle to stay in shape. We ask him why today he took the stairs DOWN to the canteen much longer than he took going back UP.
Says he, "I went down via the toilet.\"""",
404122: """safeman: Under the previous president I didn't even know who our prime minister was,
and under the current prime minister I sometimes forget who the president is...""",
404123: """smoke: Can you feel guilty for something you didn't do?
Boxan: Someone took a shit on the neighbor's doormat, and there are just the two of you living on the floor?""",
404124: """Decided to prank a colleague. He's a computer newbie and, while he was at lunch, I created a notepad file called "yourself" on his desktop, renamed the Recycle Bin to "the past" and dragged the first onto the second. The guy comes back, and on his monitor it says: "Do you really want to send yourself to the past?". He thought for a VERY long time about whether to hit "OK" or not.""",
404125: """Loshkaryov
They say every person loses 21 grams at the moment of death. That's the weight of a handful of five-cent coins, a chocolate bar, a hummingbird. No matter whether you fear death or not, it comes, and at that moment your body becomes 21 grams lighter. Maybe that's the weight of the human soul?

.::Odyssey™::.
That's the total volume of air in the body )""",
404126: """A boss in a phone call with the phone company's operator:
-Hello, can you hear me? Can't hear? Well, that's the kind of phones you made for us!""",
404127: """A piece of paper attached to a cable reel reads "9 cores"
Below, in different handwriting: "9 alive, 9 will live on\"""",
404128: """papa Egor:
Parents! If your kid asks for 100 rubles for the evening - don't you dare refuse! Because in 9 months he'll ask you for much more!""",
404129: """xxx: long ago someone scrawled "poisen" in the stairwell
xxx: today I walk by, the "i" is crossed out, a "y" written above it, and a note below: "and where the hell did they teach you padonki-speak".""",
404130: """xxx: Served a month ago, after five years of uni, and today I dreamed the orderly wouldn't let me out of the barracks, but I need to get to my first lecture on advanced math...""",
404131: """xxx: Time to switch to bicycles, then there'll be fewer accidents.
yyy: Yeah. He cuts you off, and you give him a kick in the ass.""",
404132: """xxx: The president promised that this year our roads will be like Germany's...
xxx: The bulldozers and excavators have already been sent over there.....""",
404133: """MissleMike
Today I rode the trolleybus.
KoTkOOO
Holy shit!
MissleMike
The conductress was selling tickets. And so, we're pulling up to the final stop. And then the sharp-eyed conductress (she's about 180, probably) spots four grim burly dudes waiting at the stop.
- Inspectors! - the old lady gasped in fright. - Have your passes ready! - that's her pulling herself together.
The inspectors grimly watch from the final stop, waiting for their next catch to fall into their net.
And then the trolleybus driver brakes about 50 meters before the final stop.
Have you ever seen people jump out of a burning tank?
Well, that's a pathetic imitation of how the passengers scattered from that trolleybus. The inspectors rushed at first, but then accepted defeat.
1:0 in favor of the passengers. And thanks to the driver, of course.""",
404134: """xxx:
That's what I love admins for, their peculiar sense of humor.

xxx:
My MP3 broke, I write to an admin friend of mine, like so and so, help me out, the comp doesn't recognize the player, what do I do. To which he sends me some link and writes: "pick one! :)"

xxx:
I'm thinking, he sent where I can download new firmware, or something...
I open it, and it's a catalog of trash cans.""",
404135: """Was teaching a girl to drive. After an hour she's already briskly steering around the lot and we take a break. After the break I notice she's trying to pull away in second gear.
Me: Why are you pulling away in second?
HER: Well I already drove in first, now it's second's turn, right?
And with no visible effort she pulls away. I'm thinking, stopping a horse at full gallop and walking into a burning hut - easy as pie for her.""",
404136: """long, funny, not copypasta, a genuine true story.
not long ago:
it started raining early in the morning, looked like it'd stop, but no, pouring again. and so all morning, by 11 I was dying for a smoke - went out for cigarettes (only 200 meters to the kiosk, or so). outside it's like after a flood - not puddles anymore, rivers. ran over, bought them, walking back smoking and then, bam, it starts pissing down again, so hard that cursing all mothers at once I ran under the nearest awning. shaking myself off, I look over and there's a guy next to me, about forty, a typical fisherman: rubber boots, work clothes, fishing rods, the whole deal. waiting it out like me (it's not raining there, just water coming down in a column), I'm smoking and watching the guy - he's got this vacant stare: straight ahead, but seeing nothing. and then, bam, his face lights up like he'd seen the Apostle Paul, I slowly raise my eyes and, lo and behold, at the curb, in the ditch, a huge fish is trying to either swim (not enough water) or dive deeper. I'm fucking stunned, the guy too, we stand like that for two minutes - watching the fish (I was never closer to understanding the wisdom of the world and universal peace), but suddenly there's a noise - flop, flop, flop. a granny runs up to the fish and tries to catch it. catches it, the bitch, stuffs it in her net with two others just like it and calmly fucks off. the guy stood another minute, shifting his gaze from the ditch to his fishing rods, swore and trudged off into the rain.
probably he attained zen...g""",
404137: """- I'm taking inventory of the alarm system right now, and I asked what a VAT 7012 battery is.
the director, the lawyer and the electrician started explaining to me with gestures, then decided to show me, opened the box hanging on the wall in the corner, and there's a tamper sensor on it apparently, the alarm went off, it started screeching, such a noise, worried people came running from the next office, the fire department rolled out... in short, total chaos here))))
- morons!!!!! if you ask them to explain what "pregnancy" is, make sure they don't demonstrate it on you, or the ambulance will come ))""",
404138: """Looks, you say? I walk into the utility payment office to pay for utilities. In front of me stands... can't tell if it's a late guy or an early man - hard to say. Looks like he stepped out of a war newsreel: pretty rough stubble, a tanned face covered in streaks of sweat and fine gray dust, tall tarpaulin boots, breeches and a khaki tunic with Soviet Army-era shoulder boards, a panama hat, on his shoulders something like an army load-bearing vest and a Wehrmacht "trapeze", with a sort of bag attached, out of which stick an axe, a shovel, a saw, the outlines of a plane visible. The breeches and boots are generously dusted with dust, sawdust and shavings. Now who could look like that - a carpenter, right? He walks up to the cashier, hands over his slips, and suddenly the cashier blurts out:
- Oh! My computer broke!
The guy, as if nothing happened:
- Alt-S.
The cashier doesn't get it:
- What?
- Alt plus Y. Control plus F5. F5 - with one key. Next. Next. Default option. Enter. There. And you say it's "broken".
The cashier, staring at him with huge eyes, hands back his receipts and asks:
- How do you know that?
The guy, tiredly:
- I wrote it. Don't rest your elbows on the keyboard anymore.""",
404139: """/\\eryxa:
And my neighbors say: why are you always vacuuming, and it's just the computer running :)""",
404140: """it happened at an American boy scout camp. once the American counselors call us over saying: girls, come here, we'll show you a monster. we walk up and see the picture: some incomprehensible insect (it did look monstrous, to be fair) sitting under a jar and next to it a sheet with instructions: Open at own risk. Camp T-bird is not responsible for any damage caused by the EVIL STALIN...""",
404141: """the news showed: Putin putting a tracking device on a big dolphin, you can hear the conversation: "so how are we going to release it?" "we'll cut the rope with a knife" "damn, what knife? they took everything from us\"""",
404142: """Recently in our building in the middle of the night, at like 3:00 AM, someone started hammering with a perforator or something like that. I'm thinking, what kind of idiot does repairs at that hour? in the end everyone got fed up and called the cops. So the authorities arrive and start giving the upstairs neighbor shit, extorting money, yelling, ignoring the fact that he did nothing and doesn't even own a perforator. In the end it turned out thieves were cutting through the wall into the car accessories and gadgets store on the ground floor. So right from under our cops' noses they hauled off a shitload of expensive tires, while the cops were busting the nerves of an old man a couple floors up and shaking him down for money. Whether to laugh or cry.""",
404143: """And if you mix jam with iron filings, you can catch flies with a magnet afterwards!""",
404144: """Yana: Honey, I'm such a silly girl.. I whip eggs with a drill...
Yana: I stuck a fork in it, makes an excellent mixer...""",
404145: """Sasha: I often dream of an elevator going sideways. by this sign I've learned to tell in my dreams that it's a dream, and I inform my fellow elevator riders that you know, gentlemen, you look very real of course, but you don't exist, you're my dream.
Leonid: And they answer: "Sasha, we're not in an elevator, we're in a tram!\"""",
404146: """xxx: at work we have positions: chief programmer, programmer, programmer... and Fedya
yyy: uh... what's Fedya?
xxx: Fedya's job is going to work
xxx: twice a month""",
404147: """XXX: We have a spider living in the loofah. Sits there all the time.
YYY: So cute. ) He's lazy, thinks it's a web and doesn't have to spin his own. :D""",
404148: """XXX: Don't know what I dreamed about... but clearly not a romantic dream. Today I wake up, my parents tell me:
They wake me at like eight, asking "Stas, don't you want to get up already?"... and my answer, completely clear: "I can't. I'm busy dividing things into an infinite number of pieces of infinitely small size", and then I immediately started snoring again.
XXX: the cybernetics faculty is, fuck, a life sentence.""",
404149: """Was talking recently with a friend who plays in a black-metal band. He likes to rehearse at home: growl, scream, the whole deal. He lives in a panel block and everything is very audible to everyone, but the neighbors, by and large, got used to it. But recently he decided to try a new type of vocals for himself - pig squeal. The neighbors, driven to white heat by the previous years of rehearsals, called the valiant guardians of order down on his head, on charges of, guess what. No, the neighbors didn't call the cops for disturbing the peace and public morals. Nooo, he was accused of animal cruelty: the next-door neighbor dialed 02 and said he's slaughtering a pig and can't manage it.""",
404150: """Was in Crimea the other day, saw a funny sight: a group of young people in elements of Indian clothing walk along, singing "Hari Krishna Hari", accompanying themselves on the bayan.""",
404151: """3D-Fanat: How do I get a rat out of the engine compartment?
In the morning I open the hood - it's sitting on the engine. Big, the bastard. Unhurriedly retreated into the engine compartment. Shone a flashlight - didn't find it. Started the engine, banged from below on the plastic guard - didn't see it run out...
So I'm thinking, if it survived the 1600 km trip (the car was shaking pretty hard for like 80% of the way), maybe it's still sitting in there? It'll gnaw through, the bastard, the hoses and wiring...
GPS-l: It didn't just ride 1600 km - all that time it was riding the alternator belt, spinning on the radiator fan and warming itself on the exhaust manifold!""",
404152: """xxx: daaamn... woke up this morning and almost bit the cat's head off
yyy: o_O how's that?
xxx: he was looking for food in my mouth, apparently.
xxx: I feel half like Ozzy Osbourne. :D""",
404153: """Viktor: I really want a case like that, but without the PSU. There's such a modification. And it's an order of magnitude cheaper. Don't want to overpay for a PSU that'll be replaced right away (not in terms of quality, but power...)
chieftec: Viktor, "an order of magnitude" is ten times less. If you know where to buy that case for 635 rubles - let me know; I'll buy two and gift you one.
Alexey: Programmers' orders are binary :)""",
404154: """<Antonics> girls from Seversk.... who wants to go for a ride in the car???? and not only.... write in private!!!
<Rаul> Antonics, bring me potatoes from the cellar instead""",
404155: """Walking to the store, minding my own business. I hear: someone's latched on behind me. I turn sharply: two little kids about 8 years old are hobbling after me in single file. Well, I think, whatever. But they won't leave! They keep walking and walking behind me, whispering. Started to get annoying. I stop. Pretend to call someone. The kids walked past. Then they latched on again. After like three meters I announce:
- I'm going into the forest. Coming with me?
They, not catching on: Let's go.
- I called an acquaintance, he just happens to need two boys for organs.
Gone like the wind.""",
404156: """Yyy: the 21st century - a six-year-old kid, seeing the headline of the Orthodox magazine GLAGOL, complained that he too often hits the hard sign when pressing Enter without looking.""",
404157: """Darth_Gelos: Smiled on the street. Was walking under the windows, heard Kaspersky's virus-detection sound, well everyone's heard that screech, and right after it the continuation of the Queen song: ...we are the champions my frieeend. :)""",
404158: """Today my girlfriends told me that yesterday they were chilling in an open-air cafe.
They stayed there quite a while. All that time, a guard standing nearby was zapping flies and mosquitoes with a stun gun.
And cackling ominously...""",
404159: """Artur: just fought an imaginary opponent and he dislocated my arm
Ya: good thing he didn't punch you in the eye and steal your phone )
Artur: that would've been something
Artur: punks breeding all over the place""",
404160: """Amaranth815: I work as a manager at a company. Down the hall there's a firm called "The Future". In the office there's a tiny little closet, a former fridge lined with iron sheets. Today workers were hammering in there with a perforator half the day, god knows what they were doing. Then they left, turned off the light.

Amaranth815: forty minutes later a bright light turns on, out of the corner of my eye I see glints off the iron sheets, the door opens and a guy and a girl walk out. I ask: where are you from? After the answer "we're from the future" I stayed in a stupor for a long time""",
404161: """Barabuh:
cyber sex? )
olchik.vv:
yeah, let's do it! I'm slowly taking the slipper off my right foot... what do you feel?
Barabuh:
I can smell your feet..""",
404162: """xxx:
They call me to accounting. Problem, they say, the bank access doesn't work. I'm sitting there, struggling, can't figure out what's wrong...

xxx:
And it turned out the account was locked! For multiple wrong password entries. I ask, like, what's the deal, she says she was entering the password, but not finding the needed letter on the keyboard, she entered a similar-looking one - "thought they wouldn't notice..."

xxx:
Took a day to get the account unlocked - that's how we live =)""",
404163: """Den:
Len, how are you with math?
spesh:
Exactly??? Higher?
Den:
need to solve one problem
Den:
How much is-the eighth root of the perimeter of a curvilinear trapezoid whose area was found by integration by parts of three cubic parabolloids of the identical transformation of the phi angle of the tangent to the graph of a composite function of a biquadratic equation of the decimal logarithm of a divergent sequence of a regular ellipsoid computed by adding two roots of a differential equation of a perfectly disjunctive normal form of a special case of 3 paired complex numbers of imaginary asymptotes??
spesh (15:33:09 6/08/2009)
Well I did math a very long time ago, can't even remember now! I'll try to ask around!!!
Den:
if it's not too much trouble of course
spesh:
EVERYONE KEEPS TELLING ME IT WAS SO LONG AGO THEY DON'T EVEN REMEMBER!!!!!! yOU TRY THE INTERNET SOMEHOW! OR SEND IT TO EVERYONE SOMEONE SMART WILL TURN UP!!!!!!!
Den:
Well thanks anyway!
spesh:
DON'T MENTION IT!!!!!i HAVE A HISTORIAN FRIEND MAYBE HE CAN HELP!!!!!i'LL GIVE YOU THE ANSWER RIGHT AWAY!
Den:
Much obliged!""",
404164: """Advanced users xerox on the copier, and unadvanced ones copy on the xerox.""",
404165: """An IT guy is late only for technical reasons""",
404166: """kropot: you know, if you open an MP3 file in notepad, there'll be notes and lyrics
Koval: don't know )
kropot: try it )
Koval: ЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄЄ
Koval: that must be the chorus )
kropot: You listen to some shitty stuff, lena :)""",
404167: """I call a friend, his father picks up and answers: "Vovka's out walking somewhere." Nothing special about it, except Vovka is a 30-year-old electric welder. I guess to them we'll always stay kids..""",
}

def main():
    src = open('chunks/chunk_22.jsonl', encoding='utf-8').read().splitlines()
    batch = [json.loads(l) for l in src[0:100]]
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
