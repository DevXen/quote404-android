#!/usr/bin/env python3
"""Translate bashim chunk_56 batch 4 (input lines 301-400, IDs 438437-438536)."""
import json

EN = {
438437: """zzz: And if a girl wants you to quit drinking, will you quit?
aaa: The first time, I'll limit myself to a warning. If it repeats, I'll dump her!""",
438438: """From the comments on a geektimes article about a human beating AI at Go:

XXX: I was rooting for AlphaGo too. AI is our future. Glory to the Robots!
YYY: And you seriously expect that after this comment they'll spare you?...
ZZZ: Humans have no chance anyway in the future war with Skynet, the only hope for survival is collaborationism :)""",
438439: """Status: Some time ago I took in an artist in my apartment. If you have the opportunity and desire, he'll be happy to turn one or several of your walls into a work of art.
Kudeyar: And how is he with, so to speak, the nude?
Shiver: You want him to paint naked? Monsieur knows what's what!""",
438440: """xxx: Standing on the store porch now, smoking and watching the flow of cars filigree-maneuver around potholes. Watching, watching, and then I realize they prepared us for this back in driving school. You get it? We will never have normal roads.""",
438441: """xxx:
at 10 I read a manual on conducting guerrilla warfare, published in 1943. Don't remember the author, my attempts to find it as an adult were unsuccessful. And back then I just picked it up at the village library...
Everything I've since come across about PR, terrorism and advertising is based on that manual!""",
438442: """xxx: Adulthood is when you fear the blood test results way more than the blood test itself.""",
438443: """xxx: Our current project is so fappy that if they gave us silk gloves and ebonite condoms, we'd power half the country""",
438444: """xxx: Still, floppies are way better than flash drives!
yyy: Floppies? That antique? How can you even compare
xxx: Heh, youth )) Back then authors brought me articles on floppies. Whoever gave me the most hassle, all kinds of errors, I'd put a hot cup on their floppy afterward :) Like on a coaster )) And such bliss! And what about a flash drive? You can't put a cup on it..""",
438445: """<+tomfarr> going to photograph a squirrel
< OpenStack> get drunk and take selfies?""",
438446: """Neradence: The only thing I regret is that with the surname Ivarsson it would have suited me so much more to want to scream "For the glory of Odin!" and whack my opponent with an axe between the eyes! ><""",
438447: """Instructions for quick column numbering in Excel:
5. Press and hold the left mouse button and the CTRL key on the keyboard. Meanwhile a small plus will appear next to the black cross, as in the picture on the right.

Attention! This trick is performed with different hands. :-) Believe it or not, once, while instructing by phone, I had to say this out loud.""",
438448: """xxx: A movie about assassins is coming out soon. They're going to shoot it based on Borda. If it's a commercial success, I'm afraid we'll watch the third Half before we play it...""",
438449: """xxx: D. asked me if there are orchatа in Portugal. Turns out it's a drink. But that wasn't what I thought.""",
438450: """xxx:
Oh yes! As a kid I had a book "1001 useful tips", there were gems like "attach a flashlight to the mop and then, while cleaning, you'll have light in the darkest corner of the apartment, or if your child has outgrown the stroller and you don't have a sitting stroller, you can cut a hole in the bottom of the stroller and insert a parcel box so the child can sit... If your clock bothers you during work, put it on a pillow and place a liter jar on top, you'll be able to see the time, and the ticking won't annoy you... ". So many years passed! And I remember for some reason... Big deal, brought a pillow to work, hop on the desk and a jar on top. And beaauuty...""",
438451: """Was at the institute. History faculty, just so you know. One sort of beauty is admiring herself in the mirror, complaining about her hair on the way. The dialogue goes:
- Gi-i-rls, does anyone know a good remedy for dandruff?
- I know. Guillotine (smiling at the joke)
- Reaally? And it definitely helps with dandruff?
- De-efinitely (keep smiling, surprised it's not getting through to her). And tested: one zip - and forever!)
- Really? French, probably? And expensive?
- F-fff... well by the name it's French, yeah. And probably not cheap...
She left... presumably to the stores to look. I'd like to see that.""",
438452: """xxx: If you believe the publications, this generator besides generating energy has some other interesting properties, for example the ability to glow and levitate, which can also be useful in the national economy.

zzz: All American thermal power plants have long been running on torsion fields. And they burn coal for show, so nobody guesses.""",
438453: """xxx: Congratulations on 8th of March!
yyy: And half of Russia's population talks so illiterately!
zzz: But the other half knows grammar and congratulates on 8th.
yyy: Aha, and the third knows not only grammar but math too. so it considers itself not the third, but the third =)""",
438454: """Comments about grandiose builds in Minecraft:

xxx: So much weed, and in the right hands. Spending hundreds of hours fiddling with cubes...

yyy: if you could build in reality with a mouse click
we would have colonized half the galaxy long ago))

zzz: And the second half via Ctrl+V

qqq: That's how Khrushchevkas get built""",
438455: """mivlad: Magma is underground, and when it reaches the surface it's a meteorite.
Mihail2706: did you mean meteorism?""",
438456: """St. Petersburg, an unexpectedly warm afternoon.
Ххх: something's off in the atmosphere
Ууу: the sun?""",
438457: """A scene from life:
A dude comes to get hired.
- where did you work before?
- here, here and here, deputy director.
- any contact phones of the management?
- mmmm..., in the first one the director is doing time now, and the second and third - died...
- ????
- heart attacks. Both of them. It wasn't me!!""",
438458: """xxx: No, the safest way to walk through a bad neighborhood at night is not with pepper spray and not with a bodybuilder, but with a teacher from the local vocational school. Mom once stayed late at such a teacher friend's place, got ready to go home, says, I'll call my husband now to come get me. The friend in response, like, no need, I'll walk you myself. Mom asks: "aren't you afraid?". "Come on", she says, "you'll see for yourself". And indeed, from every passing group of youth of the corresponding kind: "Hello, MarVanna!", "Good evening, MarVanna!". And - the cherry on top - "MarVanna, wait a minute, I'll hand in my essay now, I've still got a D and I'm up for expulsion! Just a sec, I'll just go home for it.".""",
438459: """xxx: At the sanatorium I met a neighbor, she was expecting her 4th child. The first two from a common-law husband, the third she doesn't know from whom, the fourth she also didn't know. And she also once asked me - can you get pregnant only in one way? I almost choked. I say, personally I only know one in this case. To which she reduced the number of suspects from three to two.""",
438460: """xxx: Granny is feeding a local mongrel in the park and tenderly, tenderly says: "Why are you barking, don't bark at me, those who barked don't live in this world anymore.\"""",
438461: """AAA> found in the mailing list a letter from the sysadmin: "Asking for the last time - if anyone has problematic UPSes..."
automatically finished it as "...I'll replace them with uninterruptible troublemakers", realized the depth of the thought, understood it's time for some tea.

БББ> And I generally think he shouldn't have written "for the last time...", he should have just pulled the switch. All these with problems would have come running on their own like good little ones.""",
438462: """xxx: On friends' advice (c) I went to the trauma center.
In general, the ligaments feel bad and there's a crack somewhere. They wanted to put on a cast, but I ran away :) somehow I'm not ready for a cast, I need to check my muffler :)))
yyy: now check your IQ""",
438463: """ххх: Damn, I hate Lego!
ххх: Constantly stepping with my heels on the pieces my daughter scatters.
yyy: That's nothing, we have 2 boys, for someone's birthday someone will definitely gift Lego to one of them. I've already made a list of those I'll take revenge on.""",
438464: """Pay the Ghost / Pay the Ghost (2015) HDRip
Actors: Nicolas Cage

Description:
There is an ancient belief that once a year, on Halloween, the gates between the world of the living and the world of the dead open.

SoloShaman: There is an ancient belief that once a year, on Halloween, the gates between the world of the living and the world of the dead open. On this day Nicolas Cage bursts into the world of the living like a demon of hell and starts furiously starring in the first trash movie that catches his eye. Sometimes this day falls ten times a year. In Nicolas Cage's world there are no calendars...""",
438465: """xxx: I haven't launched tanks since the New Year holidays
yyy: Yesterday there were new physics and sounds
xxx: When a tank and an anti-tank collide, do they annihilate and release gamma quanta?""",
438466: """xxx: You may think I know jack about music, but for some reason I can't catch the difference between punk and metal o.o
yyy: The difference is in the amplitude of the hair oscillation and the speed of wearing your fingers down on the strings!""",
438467: """nick_55:
And once, during Ivanych's profanity-laced sermon, we observed what in thermal physics is called an "escalation process". Ivanych was in charge of, among other equipment, a "Rigaku" X-ray unit that gave 2 kW in radiation. And, pardon the naturalistic details, once a little mouse crawled into it, got under that radiation and left our world forever, only in doing so fell out onto the floor and increased somewhat in size. And Ivanych had the habit, while delivering his sermon, of pacing with a detour into the X-ray booth. And so he entered the booth, didn't look under his feet, stepped on the deceased, there was a squelch, then an aroma spread, and the intensity of the sound signal rose to approximately a value comparable to the aforementioned power. Meanwhile any boatswain from the works of Stanyukovich or Leonid Sobolev would envy the lexical diversity of the expressions used. The consequences of the phenomenon were washed away with alcohol, and the ladies couldn't sense them when they came at 8:30. But Ivanych got cursed out for it by Korovman, who thought the alcohol could find a more worthy use, so the process also resembled a chain reaction.""",
438468: """Yesterday I'm sitting at a parents' meeting. They're telling about preparation for the EGE. The math teacher:

- ... What mistakes do they make? Computational. Poor knowledge of the multiplication table. They multiply seven by nine and get thirty-two.

At that moment I wanted to say:

- Dear madam, you taught them from fifth grade to graduation. They multiply seven by nine and get thirty-two. Tell me, have you never thought about harakiri?

Especially relevant after last year's story, when I discovered my daughter brings home A's in chemistry but doesn't know what valence is.""",
438469: """From geektimes:

xxx: Many times I've seen a cat wanting to be affectionate but by mistake rubbing against the nearest furniture leg or post. I generally think he doesn't quite grasp where the owner ends and the couch begins, and what the difference between them is.""",
438470: """ХХХ:
- The little ones are playing EMERCOM rescuers.
Kolya:
- Here we have bandages, here life vests, and here - little coffins.
Natasha:
- And why little coffins?
Kolya:
- Well you see, in a fire you can't do without the dead.

УУУ:
Don't remember why, but I had open on my comp the list of mandatory contents of a railway first-aid kit.
My daughter was especially interested in the item: "Body bags 18 pcs."
- What does that mean, in sleeper class - each gets a personal bag, in compartment - two per bag, and in platskart - three per bag they'll stuff in?!!!""",
438471: """Discussing the Google car crash:
xxx: And what if you cut off a self-driving car?
yyy: Another self-driving car will cut you off at the next intersection""",
438472: """Scrolling through one of the social networks, watching a video. First thought: - ham-handed!
The caption made me think. I had already forgotten that the word "ham-handed" can be replaced with the more decent "clumsy". O_o""",
438473: """ххх: as if someone doesn't know where the Korean submarine went! They went to the past to nuke America!
ууу: but the Korean pedaling exceeded the speed limit, and the sub was thrown too far into the past. But at least now we know for sure how the dinosaurs died out.""",
438474: """Fed up, I tell the director:
Me: I'm going on vacation for a week, no strength left.
D: Who would let you go!
Me: Me. I'm the HR inspector, if you remember.
D: That's abuse of office!""",
438475: """Article comparing film frames before and after processing

xxx: Deception is everywhere, soon they'll start shooting porn on a green screen in green suits.
yyy: long ago already.
zzz: no way =((( they've already reached the sacred""",
438476: """Buying tickets for Matsuev, found out the price, got thoughtful. The cashier: "Why pay for the name? We have another pianist coming soon, very democratic prices, and he plays the same thing, on the same grand piano. Go see him.\"""",
438477: """A colleague returned from a week on duty at the branch. Sits sprawled in an armchair and by phone briefs his replacement on what happened at the site over the week.
- In short, one comp died there, yeah, the one by the scanner. Lying with the case open, take a look at it, yeah. Also a new girl started in accounting, I set up her account but haven't trained her yet, drop by, maybe she needs help.

And a few more minutes, all in that spirit.

- You just reminded me of a cosmonaut on the ISS. – I tell him. - Like, you're standing, one foot already in the descent capsule, teaching newbies who just crawled out of the Progress-M hatch. "Okay guys, water leaked from the fridge, doesn't work, first eat the borscht from the tubes, its expiry date is ending. A hurricane is coming at Australia, Center asked not to forget to keep an eye on it... Also some green alien crap got stuck on the solar panel, I poked it with the manipulator but the bastard won't fall off. When you go on a spacewalk, take a mop with you, just in case it bites. Until you chase it off don't turn the light on full – not enough electricity.\"""",
438478: """- Girl on the train to Tula-1-Kurskaya, with a red scarf in a black jacket, we often crossed glances, then you closed your eyes and sometimes opened them, reply!
- That's called blinking...""",
438479: """Congratulating my three-year-old nephew on his birthday.
Asking him: And how old are you now?
Him (joyfully): Ten!
Me: Why so many?
Sister (gloomily): In this family a year counts as three.""",
438480: """xxx: Question about the cartoon "My Little Pony". MLP has unexpectedly gained a large number of adult fans around the world, and mostly men. What exactly do fans find in it?

yyy: I think mlp transmits a coded signal from the reptiloids of Nibiru. Those who watch the series are their hidden programmed agents, and don't even know it themselves. They'll activate when the invasion begins.

zzz: Don't blow our cover!""",
438481: """DoubleDJ: Once driving a girl in the front seat and my mom in the back.
Mom: Drive slower! I can't see anything – I'm scared!
Girl: I can see everything – I'm even more scared!""",
438482: """Night. all the kids put to bed, I'm hanging up laundry, husband helping. Suddenly from below, from the kitchen comes the characteristic click of a container lid, crunching and appetizing smacking with breaths and some delight, like right there "yum-yum-yum..."
Naturally, the first thought is that the "poor ever-hungry doggy", or simply our thieving labrador girl cracked something open in the kitchen and is feasting, the scoundrel! She does this often, even learned to open the oven.

The basics of training are precisely to catch and punish the beast at the crime scene, because pointing at it later is useless. It will guiltily squint and tuck its tail under its fat butt, but sincerely not understand: what for?!

The spouse, like a ninja, creeps in total darkness, jumps into the kitchen and in a scary voice shouts: "Caught you?!! Who's eating pickles at night?!! Huh?!! You ill-mannered dog!" - and, swinging his t-shirt, turns on the light.
There stands a frightened granny finishing the last pickle from the container over the sink.
Husband, embarrassed: "Why are you in pitch darkness?! That's why I took you for the dog, lucky I didn't smack you!"
To which granny calmly answered: "Wanted pickles and brine... was afraid to wake you all, so I didn't turn on the light... and anyway, I keep telling you: your poor poor doggy is hungry. You scold her, underfeed her, that's why she steals!\"""",
438483: """There once was a dude, let's call him Vasya. Vasya bought himself a black SUV, same age as him (19 years), a Pathfinder in "really shouldn't be driving anymore" condition. Vasya brags to me, revs in the yard on all six cylinders: smoke, burnt oil thoroughly mixed with coolant, misfires..

- You got the 33rd engine? - I say (it's a model, VG33)
- First so far! - Vasya answers me.

Let's wish Vasya luck.""",
438484: """xxx: when the war, Petka, ends, you and I will arrange a startup and be stakeholders
yyy: why a startup, Vasily Ivanych?
xxx: well, Petka! you know how tasty steaks are!""",
438485: """News: University students without a military department will be able to study right in military units
Comment: Exactly a year, enrollment in spring and autumn.""",
438486: """<ххх> An elderly German woman was boarding the bus ahead of me. In her wallet, where women usually keep photos of children and spouses, I saw a young Tom Cruise cut out of a newspaper. Check and mate, optimists.
<yyy> Maybe it was Tom Cruise's mom!""",
438487: """Standing at a traffic light looking at an ad poster on a truck standing next to me...

Uh-uh-uh.... Designers, my precious, when you have a text like "... We are leaders. ..." in front of you, a friendly tip: choose your font more carefully.""",
438488: """SergeyNik: a pool is evil. drank all the alcohol in the house with him.
abx: I once drank with a fridge, but with a pool...""",
438489: """hardworm: Found a phone and a bag of chargers in the nightstand - played Fort Boyard""",
438490: """xxx: There are two projects...
yyy: One of them has pointed peaks?
xxx: Uh, hmm. Basically, yes.""",
438491: """xxx: greetings all! Who can tell me what program to download to my comp to do photoshop""",
438492: """Polina: Holy crap, I'm sitting at work, and on Relax FM there's a segment "gourmania", and there a velvet male announcer voice declares: "Lasagna is an exquisite dish with juicy filling and tender sauce. The Italian masterpiece is prepared in different ways ...blahblahblah... Can you already feel this inimitable piquant aroma? Can you imagine how the tender dough melts in your mouth?"
The "masochism" segment""",
438493: """News on the feed: "The pregnant bride of Timur the goat disqualified"

First thought: gorged on meldonium, or what...
Completely fried my brain.""",
438494: """xxx: Did you hear that Chuck Norris turned seventy-six the other day?
yyy: How old he is!
xxx: It's not him who's old, it's old age that's Chuck-Norris-ish.""",
438495: """xxx: watch series all day
yyy: watching series isn't looking for work
xxx: is it hard to look or what?
xxx: "A simple 23-year-old girl earned a car for 4 million in just a month" <ссылка>
yyy: no thanks
yyy: my tissue regeneration isn't that good""",
438496: """AF: Great, banned on GitHub. They say their robot thinks I'm a robot. Ffs. Dizziness from AI!
SK: We thought the problem was how to make AI pass the Turing test. And it turned out the problem is passing the Turing test ourselves with the existing AIs.""",
438497: """xxx: as they say now: not a robot, prove it yo
yyy: prove you're not a Boston Dynamics camel robot
xxx: lol
xxx: walk straight, no prancing
yyy: and don't buzz
xxx: exactly""",
438498: """xxx: what do you know about senility?! Our tax office doesn't answer emails sent outside working hours.""",
438499: """MacIn: Do you need to know algorithms?
Ha three times. Personally knew a programmer who didn't know there were variables. And wrote programs, storing data in the fields of hidden window components.""",
438500: """Me: There's an old Eastern saying: "sit on the riverbank and wait until the current carries past the corpse of your enemy."
Girl, distracted from reading "The Godfather": and who do you need to call in the meantime?""",
438501: """1: IMHO, these smartphones will degenerate humanity. Or, at minimum, its civilized part. All smartphone users are total geeks and weaklings. Instead of poking touchscreens, they'd better go to the pull-up bars..
2: Not necessarily... What about me, am I a geek and weakling?
1: I didn't say that. Facts, you're a hell of a frame, a mountain of muscle.. You even do 10 pull-ups on one arm on each!
2: So that's why I trained myself to do one-arm pull-ups? Right, so I can read my VK with the other while hanging""",
438502: """Situation now (half past two at night, I'm sitting reconciling an estimate for 4 mil, chatting on Skype with Misha so I don't fall asleep):

Misha: I just carried so hard for Illidan
Me: if you knew how hard I'm carrying for the CFO right now
Misha: ahaha, live broadcasts in Excel, new Twitch channel
Me: Reconciling estimates live.... working with numbers lvl 80, pumping from sole proprietor to cartel before your eyes ...""",
438503: """ххх: a guy from radio engineering transferred somewhere to the programmers to study
ххх: walks around, bragging, all full of himself
ххх: and in such moments of his tail-puffing I remember how at the exam he said frequency is measured in dukes...""",
438504: """Voltov: Today I had a simply idiotic fail. For interest's sake started disassembling a used empty pepper spray can. While uncrimping the top part my hand slipped, scratched my finger on the sharp edges, on which there was a bit of potent pepper liquid. The wound started stinging like crazy, and I put my finger in my mouth.......""",
438505: """From the comments on a survival post:

once I took this kitty with me on survival... so while I was sleeping, this kitty took a shit somewhere and then came to warm his ass against my warm nose, which was sticking out of the sleeping bag... since then the kitty thinks I'm the scariest beast in the forest....""",
438506: """Dmitry:
And I remember asking Sergey something like "I've been away from civilization for 10 days. TELL ME WHAT HAPPENED IN THE WORLD IN THIS TIME."
and he just went and fell silent for a few minutes.

Sergey:
I was trying to comprehend
civilization
why the fuck are you asking me such hard things?))""",
438507: """ххх: the most powerful ship power plant in the world consists of one diesel type 14RT-flex96C with 81000 kW power
yyy: Does this diesel spin a generator or the screw directly?
xxx: I think such a diesel spins the whole planet around its carrier""",
438508: """xxx: my husband just delighted me with a non-standard accusation - "you toss at night worse than Freud in his coffin!\"""",
438509: """XXX: by the way, are croc-skin handbags veganism or not? after all I didn't eat it
YYY: Croc-skin handbags aren't veganism. in the sense - you wear it, so you're not vegan
XXX: and if the croc already died? and we like transplant its skin onto another croc
XXX: and the other croc turns out to be a bag?""",
438510: """Mom got on the scales
Mom: Oh! I think I'm losing weight...
Me: congrats
Mom: ...as of today.""",
438511: """xxx: Turned on a 20-minute workout, and there it's arms with dumbbells. No dumbbells... took a liter in one hand of rum, in the other of whiskey. 
xxx: Such a desperate striving of an alcoholic toward a healthy lifestyle""",
438512: """xxx: connected the smartphone and laptop with a cable, and the smartphone tells me, like, the connected device could work faster
xxx: oh the times, and before the laptop wrote that about the flash drive""",
438513: """Chadelbendere: Just had an amazing conversation with the statistics service.
Radamskin: ?
Chadelbendere: Me: The list of meat product codes even includes primates and snakes, gladdening to see. But tell me, what code to assign to smoked chicken?
SS: There's no smoked chicken in the list.
Me: Why?
SS: Smoking chicken doesn't relate to industrial production.
Me: And smoking snakes, dolphins and primates does?
SS: May I clarify and call you back?""",
438514: """xxx: Administrative liability for driving a vehicle in a state of alcoholic intoxication, as far as I know - is not provided.""",
438515: """ххх: Cat people will understand me. That feeling when at 3 at night you want to throw a slipper at a frantically scratching and tearing fluffy butt, you lower your hand under the bed, and instead of a slipper there's another cat...""",
438516: """ххх: A sysadmin from the CRH just called, asked if I have a laptop hard drive for sale.
ууу: And?
ххх: I ask, with what interface? What would you personally have answered?
ууу: IDE or SATA
ххх: Growing)) And he answered "something simpler".""",
438517: """From Habr
xxx: When interest in media stars fades - they post nudes, when interest in software fades — they post the source code""",
438518: """xxx: we want to congratulate our new girl-programmer on her birthday and dedicate to her the Nautilus Pompilius song "Mother of BUGs\"""",
438519: """Discussing the news "SCIENTISTS MANAGED TO CURE PARALYSIS BY REPROGRAMMING THE PATIENT'S BRAIN":
ХХХ: Looks like someday we'll live to see the release of the right_hands.sys driver
YYY: I can just picture when the profession of "brain programmer" becomes mass. Such a "brain-wrecker" comes to a patient, and after the session the patient can't be happier: wanted to move his hand - his leg twitches, wanted to move his leg - pissed himself""",
438520: """Sam258: Since on March 9th it's work early in the morning, I decided to make the wife a "romantic" night from the 7th to the 8th. While I was washing-shaving, opening wine, lighting candles and making half-darkness, the youngest woke up. Naturally, she came to our room and goes: "What's this New Year?". Blew out the candles. Rocked the youngest to sleep in her room. Romance with a sleeping child - not it. Decided to move to the kitchen. Lit candles, made half-darkness, opened wine.
On the kitchen on the radiator sits the cat, warming up, eyes shining. Romance with a cat sitting on the radiator - not it. Decided to kick the cat out to the hallway. The cat thought that since they're driving her out of the kitchen at night, they must want to eat something tasty without her. Started resisting to stay and eat tasty with everyone. During desperate resistance she knocked a flower off the windowsill. Good thing my wife and I weren't in negligee. The flower crashing to the floor woke up not only the youngest but also the older daughter. In the morning you can't get them up, but here they both trudged to the kitchen.
They go: "What's this New Year?"
I blew out the candles and go: "March 8th! Happy holiday, you..."
Sitting all in the kitchen. The youngest is unwrapping the "doctor's kit", the older is mastering a new smartphone, the cat is eating her food, the wife is sniffing flowers and sipping wine, and I'm swirling ice in my whiskey.
Basically everyone's happy... yeah... just pity the flower...
Happy March 8th, girls!""",
438521: """Walking down the street with my husband, a supermarket nearby.
Me: Ok, need to urgently remember what food we're missing at home.
Husband: Dumplings?
Me: There are dumplings, two packs in the freezer.
Husband: Then we have everything.""",
438522: """Discussing "Do you use a car dashcam"

Kirpich: Was gifted one recently. Turned it on once. Then watched what recorded - looks like Kolya and Lyusya from "one for all".
fobka: you should've turned the lens the other way)""",
438523: """xxx: Hi! What are you doing?
yyy: Removing curses by photos, damn.
xxx: Went into psychics?
yyy: Yup. They send me screenshots, and I try to figure out where the infection is hiding that the antivirus somehow can't catch.""",
438524: """Sodolyana: sitting, bothering no one, the work phone rings, unfamiliar number. Dialogue:
Me: "Good afternoon, hotel such-and-such, listening!"
Someone: "Giiiirl, do you have duplicates?"
Me: O_o "Duplicates of what, excuse me?"
Someone: "Well, of keys!"
Me: o_O "Keys to what, excuse me?"
Someone: "Well, to the room!!!"
Me: O_O "And to which room, excuse me?"
Someone: "Well, 219!!!!!! I stayed at "...", and my friends are already heading to the hotel, and I have the keys, so they can get into the room!"

Honestly, at the end I expected a phrase like "Don't you know anything at all?!!!" >_<""",
438525: """xxx: now I think Kolobok is a zombie.
yyy: no. Kolobok is a golem.""",
438526: """XXX: been fucking with the lease agreement for the second day
УУУ: interesting sexual preferences you have...
ХХХ: not mine - the client's
ХХХ: yeah, in this a lawyer's work doesn't differ much from a prostitute's work
УУУ: fiiiiifty shades of legal""",
438527: """Waited long for the electrician. And finally he'll come, tomorrow at two, no, at four, Saturday at four on Sunday, early in the morning of the fourteenth day of the month of Nisan in a white cloak with crimson lining. Lining under the left eye.""",
438528: """ххх: Listen, move the site recovery time forward on the user-facing stub.
ххх: Otherwise it pops up, but we still have 14:10
ннн: Ok.
ххх: Actually, you know what? Replace the stub itself.
ххх: Write: "We're fucked". Brief and capacious. Stylish and youthful.""",
438529: """ххх: I'm bottom-tier
ххх: I updated from git and my changes got wiped((
ххх: now I'm restoring this shit and nothing works
ххх: the funniest thing is that in the next tab an IQ test is waiting for me))
ххх: guess I can skip taking it""",
438530: """from LJ
OP: Where do intelligent Petersburgers 40+ hang out and meet
An intelligent well-rounded woman over forty wants to expand her circle of pleasant company. She doesn't have any narrow-focused hobbies. Please suggest any interest clubs, hangouts, events, centers, etc. Grateful for any leads.
1st comment: VK has a group of swingers meeting every two weeks in one of the saunas on Vasilyevsky Island.""",
438531: """xxx: I still find my ex's dirty socks around the house, godwhyme.
xxx: You'd think we don't have a small house but a huge mansion, I've cleaned many times already. And still there are reserves left.
yyy: well men's socks are living creatures ))
yyy: they live, hide, multiply )
xxx: Capturing new territories)
yyy: fighting competitors by population displacement )
xxx: How to stop them??!)
yyy: only by manual extermination )
yyy: or release into the wild socks of a stronger species""",
438532: """There are three of us in the department: me, Natasha and Natasha. The Natashas can be told apart by full names: one is Natalya, the other - Natalia.

The other day a letter came addressed to "dear Nataleia", which baffled absolutely everyone.""",
438533: """<xxx> Just rode home in the bus through a traffic jam. Overtaking a little car. No rear bumper. And no front one either. Behind the wheel a little girl is quickly typing something on her phone, not looking at the road.
<xxx> Some people life teaches nothing.""",
438534: """xxx: the cat came
yyy: what's it doing?
xxx: liking milk""",
438535: """xxx: Argued with a copypasta, the day wasn't wasted.
yyy: Only thing left is to fight a monument, for full satisfaction.""",
438536: """Forum. Talk about construction. Post from a mom who just finished building her house.
- Spreading the apple pie over the baking sheet. Eldest daughter: "Why are you smearing it, it'll heat up and spread like a self-leveling floor!" AFTER CONSTRUCTION THERE'S NO LIFE!""",
}

if __name__ == "__main__":
    print(json.dumps(EN, ensure_ascii=False))
