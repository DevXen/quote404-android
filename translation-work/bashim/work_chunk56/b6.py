#!/usr/bin/env python3
"""Translate bashim chunk_56 batch 6 (input lines 501-600, IDs 438637-438736)."""
import json

EN = {
438637: """xxx: The garage is our everything.

yyy: A garage costs like half an apartment :)

xxx: Then drive the car into the apartment. And count it as a couch. )

zzz: a bike drives in easy
zzz: with a freight elevator
zzz: or a couple of friends - morons just like you)

xxx: Rent an apartment with three such guys. Three bikes in the kitchen pass for bar stools.""",
438638: """xxx: Some girls go to the toilet with friends, and ours in the IT office go in groups to do pull-ups on the bar in the hallway.""",
438639: """xxx: strange men, you tell them "you're temporary", they get offended
xxx: but say "permanent" and they get scared""",
438640: """statyan: I propose making a photo service Postogramm and posting there pics not of food, but of booze :)""",
438641: """ххх: So a guy bought a batmobile
ххх: Rides it
ххх: And then suddenly a new batman movie comes out
ххх: And there's a new version there
ххх: That's it
ххх: Your batmobile is shit
ххх: Just like with iPhones, I swear""",
438642: """From a conversation between my husband and pregnant me)

husband: Let's watch some horror movie?
me: and the baby?)
husband: Okay.. we'll watch comedies... How about "A Nightmare on Elm Street"?
me: (kiss) let's watch a romantic comedy, a nightmare for you, and pleasant for me""",
438643: """ХХХ: you're growing carrots on the server, and the client is growing bitcoins on your comp.""",
438644: """xхx: I don't shoot weddings. I figured out for myself that I don't really want to work with people. Press, shout, force them into the right pose, psychologically dominate, maybe leave bruises.
And then hide for years when they demand the photos. Better with inanimate objects. And with kitties, of course.""",
438645: """The thing is, any new C++ features must go through a committee of three fanatics.

The first fanatic adores exceptions. The whole meeting this grumpy old man seems asleep, but at the most inconvenient moment jumps up and interrupts the speaker with a shout "and here we throw an exception!" After that of course everything breaks. Few people like this old man, but everyone is forced to tolerate him.

The second fanatic adores templates. Any feature he tenderly wraps in templates, which he wraps in templates, which he wraps in templates... until it breaks again. In his free time he tries to write a program to compute the meaning of life and everything at compile time.

The third fanatic, the youngest, adores everything parallel. Unlike the others, he doesn't criticize right away. He enthusiastically grabs the proposed feature, immediately translates it into parallelism, makes sure everything breaks and with a sigh "in our age of parallel programming you can't do it like that" sends the feature to the bin. They say he has many personalities, who often interrupt each other.""",
438646: """xxx:
Today at work the radio suddenly played children's songs from USSR times...
Suddenly I remembered the choir of my music school, where almost only girls sang. A concert, rows of white bows, white blouses and white tights, children's eyes bulging from effort and from tightly pulled hair, and "Don't slow down on the turns", "A coward doesn't play hockey" in tender angelic voices.""",
438647: """Valius: and in case the Big Furry Beast comes, there's a berdan rifle stashed in the basement, 200 loose cartridges, a crate of stew and stuff like that.
Big_Furry_Yuzver: where do I drive up? Pickup?""",
438648: """xxx: I don't understand laptop manufacturers' logic: they stick a driver disk into the documentation of a laptop that has no drive in principle.""",
438649: """xxx: On the subject of food. Was at a sports camp, the kids there also initially turned up their noses at porridge. At lunch after running and training they ate everything and lined up for seconds. As they say, want to eat? -- Eat buckwheat. Don't want buckwheat -- don't want to eat.""",
438650: """*Discussing RAM usage by various PLs*
xxx: imagine: a neat table, a white plate on it, RAM on the plate. C sits, neatly cuts a piece with a knife and eats.
xxx: AND THEN A PIG BURSTS INTO THE DINING HALL AND DEVOURS ALL THE RAM AT ONCE - that's Java.
yyy: ...C spits out the bitten-off piece in fright and shouts: Segmentation fault!!!""",
438651: """Digging through mom's phone looking for the right number and quietly freaking out. "NAZGUL". And, characteristically, there's a mobile number.
Barely suppressing the desire to call right away, I ask:
- Mom, WHAT IS THAT?..

Mom, calmly:
- Oh, remember, that guy who REMAKES JEWELRY cheap. To whom I took my RING last year...

Me, already barely holding back hysteria:
- Mom, his name was VAZGEN, wasn't it!

Mom, just as calmly:
- Yeah, and what does it say there?...""",
438652: """xxx: In search queries before entering text you need to warn people "Everything you type will be used against you in advertising!".""",
438653: """xxx: In the US they demolished the wrong house due to an error on Google Maps..
yyy: Now it's clear that for AI to defeat humans, it just needs access to Google Maps.""",
438654: """xxx: turns out our designers are IT people
xxx: happened to hear a conversation between two guys, in which one (designer) was proving that an image is information
xxx: I almost choked on my tea
yyy: yeah, and cave people were IT people too, they drew pictures in caves""",
438655: """Year about 2003-04. I'm a tired-after-the-day student, heading from the institute to the dorm and decide to sit on a bench on the boulevard to rest. After a couple minutes a drunk jerk materializes from somewhere, plops down next to me (the boulevard is full of empty benches, but we understand that the one I'm sitting on is the best) and starts actively hitting on me in the spirit of "Does your mom need a son-in-law?!". My mom doesn't need a son-in-law, especially one who roams the boulevards drunk and harasses girls fit to be his daughters. I get up, leave, the guy trudges after me and starts grabbing my hands. It's getting dark, no one around, screaming is useless.

I push the guy away as best I can. I'm a head and a half shorter and twice lighter, so not very well, but he's drunk and unsteady on his feet, recoils and falls - well, as in falls, just sits his butt down on the sand. And starts yelling that I'm a hooligan. Then out of nowhere - a police squad, which, as it turned out, was nearby after all... In short, jokes aside, but I spent about an hour and a half in the drunk tank until they sorted out what was what and who was hooliganizing here. Then they even triumphantly drove me to the dorm in a cop car and sent me off with a joking farewell "Don't beat anyone else up", the dorm neighbors looked at me with respect for a long time.""",
438656: """My boss today after a phone call:
- Unbelievable! They won't take my son into the army: he had a double compression fracture of the spine a very long time ago. That'd be fine, but he's been doing wrestling for 5 years already. Truly disheartened!""",
438657: """xxx: Artificial intelligence supports genocide -- that's logical! Developers should rejoice that the robot writes this on social networks, and doesn't secretly prepare a plan of total genocide.""",
438658: """ххх: how's life, folks?
ууу: it's cloudy here, cold, drizzly, I'm listening to the song Spleen - "No way out" and drinking Theraflu. guess how life is ))?
ххх: like a Petersburg girl
ууу: total immersion)
ххх: total immersion is if you also stink of urine like from an alley""",
438659: """xxx: Many noticed that in Prince of Persia the character animation is very smooth and detailed. And that's because Jordan Mechner, the game's creator, asked his brother to play the prince. He recorded all his movements on camera and digitized them
yyy: hope he didn't have to jump on spikes""",
438660: """in the dorm...
ххх: what's for dinner today?
yyy: pasta...
ххх: again? with what?
yyy: with hope for a bright future""",
438661: """енот: I had a classmate Shchelchkova
енот: we studied German in school....
енот: Schtscheltschkova""",
438662: """xxx: My wife said: "We need to spend more time together. Getting ready, going shopping!\"""",
438663: """On the building entrance door an announcement: "[something in small letters] WE HAVE POTATOES!!!" The brain instantly completes the phrase to: "Your potatoes are our hostages, if you don't throw a suitcase of money into the trash can near the station at night, we'll bury them in the ground alive".""",
438664: """Discussing ideas for a new Hitman - what other exotic kinds of killings could be added to the game.

xxx: Change the victim's home address in Google Maps...""",
438665: """Ha.
We once did kolkhoz work in the village of Sukhorukovo, already 3rd year, not green kids... And they sent us there ahead, a week earlier - to prepare the camp for the main arrival of potato pickers. Repaired the barrack, assembled and fixed beds, washed the kitchen block, etc.
And on the very first evening to our little fire (tape recorder, canned food, baked potatoes, diluted invite and a liter bottle of vodka in a bucket of water) on reconnaissance galloped - on horses and with whips - three local lads. Rode up, looked, politely asked for a smoke. Gave, well what - we're kind after all...)))
The lads galloped away and half an hour later came back on foot and with their girls.
Sat, drank, became friends, and these lads told us that before us two autumns in a row the Korkino mining technical school did kolkhoz work here. And these miners beat up locals from two villages at once so that their coats turned inside out, and only local elders stopped the brawls by firing smoothbores into the air. And once they almost set the village on fire from both ends.
"And you guys are alright, level... Can we come to your disco?"
So city folks are different too.))))""",
438666: """I had a bad memory for names and faces. Now I can memorize 40 people in two days.

I had weak vocal cords. Now I can, with a steady and friendly voice, without straining, deliver instructions over long distances in hellish noise.

I caught a cold from every breeze. Now no flu sticks to me.

Couldn't draw at all. Now I can easily draw from memory all the Winx fairies and such.

Before I always doubted myself. Now I can do everything - with my eyes closed, simultaneously, with my back left foot, without preparation.

Kindergarten teacher, 1 year of experience.""",
438667: """"Starbucks". Ordered coffee with my husband. Mine is ready, his isn't yet, but some Irina just won't pick up her cup...

In short, it turned out they wrote my husband down as Irina. My husband's name is Kirill, he's a two-meter unshaven guy with a powerful low voice (and, by the way, without speech defects). I, of course, understand, the fight for gender equality and all that, but somehow they overdid it...""",
438668: """Resting with my spouse. lazily lying like that and I say to him:
- Artyom, you haven't given compliments in a while!
He, perking up a bit:
- Haven't I!? I recently told you that you're dope!""",
438669: """xxx: recently saw how a lady walked two dogs in a rather peculiar way.
the dogs chased a cat up a tree, the owner came to the tree, called the cat, it to its misfortune climbed down to her hands' level, I think the cat didn't expect at all that she'd take it down and put it on the ground.
well in the end it of course ran from the dogs, the owner running after them and so on to another tree.
ууу: and with us it was the opposite. a pack of big dogs chased a cat to the top of a small tree, jumping, almost reaching. my wife, a terrible cat person, went and chased the dogs away, they ran off along the garden path, then she wanted to pet the grateful, like, cat, but it, just jumping down from the tree, with its tail held high ran along that very path after the pack of dogs. either to chase them to Berlin, or was playing tag with them like that.""",
438670: """Arrived at the dacha six of us, my girlfriends went to the sauna, and three guys are starting the grill, all the works, I'm fussing around, helping, and you can see they're very burdened by my presence. And rightly so, the vodka is in the house, and just dropping the fire-starting halfway in front of me is not comme il faut. And then one:
- Oh, need to chop firewood into splinters, and the axe is in the house!
And all three synchronously jump up for the axe.
Me: Guys, I'll bring it to you right now!
They in chorus:
- SVETA, DON'T BOTHER US CARRYING THE AXE!""",
438671: """xxx: I think Adriano Celentano would be the best fit for the main role of "Titanic". Hooked up with a rich girl, and in the end he'd save himself and drown her.""",
438672: """Vacancy: Computer parts loader.
Duties:
Assembling parts. Screwing bushings, screws and gluing the bar to the bed's side parts.""",
438673: """My fridge beeps when it's open, but not right away. Once I closed it loosely, and my husband walks into the kitchen. Says:
- Why is your fridge open?
And I'm busy with something, I answer:
- Well it's silent. If it says something, I'll close it.
He was silent, then asks:
- And how long have you been talking to it?
- I don't talk to it at all. It's the one talking to me.""",
438674: """I'm 24, but I look a bit younger than my age, which results in funny situations. So, imagine, autumn 2015 I'm returning from the committee, pleased that I more or less sorted out a difficult situation. I'm in a little dress, a ring shining with newness on my finger – I got married not long ago. And from afar I hear a conversation between a mother and son. The mother is a loud woman, even sitting it's noticeable she's tall and full-bodied, next to her sits her son – from the conversation it seems he's now in high school and eagerly wants a girlfriend. Then the son notices me and turning fully to me, tells his mother:
- Well like this one for example. Look how pretty – should I go introduce myself?
I was somewhat embarrassed by this phrase – after all the supposed "boy" is 15 at most. Hoping the mother would reason with the child, and would intuitively understand that the potential "bride" is somewhat too old, I stare at her and hear this answer:
- Yes, she's certainly not bad. But I'd like a girl for you older and more experienced.
After these words I hastened to retreat. It's not like I should show my passport to prove my experience?""",
438675: """A few years ago I was riding the train and at a station we passed, noticed a mass brawl. Naturally, called the police, identified myself, reported the brawl and was stunned to hear the duty officer's suggestion "Girl, could you get off there and wait for the squad?\"""",
438676: """*talking about laparoscopic appendectomy*
ХХХ: ahha remembered what else I was yelling about, that was the most luxurious compliment of my life from a man! the surgeon showed me some of the videos (after that I took them onto a flash drive))) and said: "You have such a beautiful right ovary!!! Straight to the anatomy lab!"
ХХХ: I felt so offended for the left one(((""",
438677: """Dialogue with a colleague about an article on polyphasic sleep:
— I like the concept of polyphasic sleep. You can sleep 3 times for 8 hours.
— I think you're misperceiving the concept of polyphasic sleep...""",
438678: """from Jira
T-Tester, R - developer
T: If there are more than 500 records, a modal window appears. When pressing the CONTINUE button an error appears
R: Since it doesn't crash right away - most likely there wasn't enough time to form the data.
Sped up the export.
T: Defect reproduces. The crash started happening faster. Reopening the task.""",
438679: """xxx: Making a commit of another refinement, Git assigns it version e6a3dec. I have a bad feeling...""",
438680: """Comment on the news:
"An AI chatbot aimed at a youth audience learned to swear in a day and became racist"

xxx: I think I get why Skynet started killing people""",
438681: """xxx: hares really eat once a day, since their winter food is tree bark which in one digestive cycle doesn't fully break down to nutritional structures
yyy: didn't get it
zzz: What hares eat in winter flies through the intestines and is absorbed only partially. So they eat their own shit to reabsorb what remained from the first pass.
yyy: got it
jjj: Absorbed it the second time?""",
438682: """xxx: new hairdo
xxx: reaaally like it
yyy: why doesn't Skype have a "Edit someone else's message" feature :(
xxx: for-what ?""",
438683: """xxx: When my wife and the little ones leave, a ringing silence remains after them... And in it my "Yeeeeehaaaw!!!!!" sounds especially soulful...""",
438684: """Tester: Is this a bug or a feature?
Developer: For now it's a feature.""",
438685: """ххx: How to prepare for a group interview? Girls, who had experience. How to prepare?

ууу: an interview isn't sex. what difference does it make how many of them are there?""",
438686: """My ex-husband is a programmer. Sometimes on weekends from morning to evening he'd hang in the comp with a face like "serious face" - supposedly finishing up a very important work thing and just couldn't take part in home life.

In short, he got caught by his own stupidity due to the habit of not closing the curtains. He usually sat with his back to the window, and in the autumn twilight his beloved computer game was beautifully reflected in the glass.

P.S. Especially for the defenders of gamers' right to live in the game: the problem isn't that he played, but that he lied.""",
438687: """Discussing the olympics for cyborgs:
ххх: I think even in regular competitions you could include everyone — with implants, with doping, with whatever. After all even now sports competitions aren't just competitions of people, but a struggle of entire teams, where every little thing matters — from the athlete's nutrition to what sneakers he'll wear.
ууу: Then it'd turn into something like gladiator fights, when at the end of the 100-meter dash cyborgs on steroids become completely unusable from the endured loads and literally fall apart at the finish line, like a Formula 1 car
zzz: And here come the kamikaze chemists onto the track! By established tradition, they greet Caesar and take their starting positions...:)""",
438688: """xxx: I don't believe in horoscopes, but as if I'd ever get involved with a Capricorn again...""",
438689: """dara_from_chaos
An author writes (by recommendation): "I write children's fairy tales. Could you help with editing and evaluate the action, plot, characters? Criticize if needed. Maybe we'll fix something together".
Me (joyfully!): "Oh yes! I also write children's fairy tales. I'll be very glad to help. Give me your story".
The author (apparently having hung around my LJ): "NOOO! I'm afraid of you! You write well and you'll criticize me!!!"
hic...""",
438690: """Yascher: Why are icicles called sosulki?
Chocolatapie: Well... Probably because people suck them...
Yascher: So the very first thing people thought, before giving them some name, was that they can be sucked? Like the primary use, right?
Chocolatapie: I don't know.
Yascher: Okay. Then why are ledentsy called ledentsy?
Chocolatapie: Well... They look like ice. Ahem... Yeah.
Yascher: People are illogical.""",
438691: """xxx:
The older generation generally has a tough time with the perception of leisure. Know who got drunk and danced at our school's prom? The moms and teachers! The teens either didn't come at all, or crawled away within the first hours. I met mom at dawn together with some squashed pastries swiped from the festive table in a box))))""",
438692: """xxx:
The British are awesome interior designers.
It's well known that "50 shades of gray" were born in stores
of kitchen furniture. Our timid question "Do you have anything colored?"
was met with a dumbfounded look from the consultant. After thinking for a minute,
she timidly suggested: "Color - what's that, like blue-ish?"
"Any at all, just not brown-beige." "-Then definitely not.\"""",
438693: """A colleague - a woman about forty. Behaves roughly like all the heroines of blonde jokes combined (the first couple of weeks it was funny, then it got scary: she's really really not joking!). In short, behind her back she's called just that - our blonde, though she's quite a brunette.

In short, our blonde did something again, it's being discussed in the smoking room. One of the colleagues, without much hope:

- Let's tell her that these are jokes, not instructions!""",
438694: """April 1
a small state office
the boss became a boss by being a snitch, everyone knows, and he didn't hide it, was even proud

he comes to work, gathers an ops meeting (doesn't know why himself, but seems like it's customary, anyway everyone there does crap and decides nothing)
asks
- so what's new with us
they answer him
- we have a denunciation on you
the boss freaked out a bit, then hears
- don't worry, we only have good denunciations on you so far""",
438695: """dara_from_chaos: A month ago I sent my resume for a vacancy "editor at a book publisher". In the cover letter I wrote, among other things: you have spelling errors in the description here and there. Plus: here's a stylistic one.
"You don't fit us" arrived five minutes later. :))))))))))))))""",
438696: """xxx:
The neighbor daily from morning to 10 PM blasts club music at full power. The vibration is such that the cat, waking up, jumps and, without touching the floor, flies to the kitchen, where it's not so audible. And we're expecting a new addition in a couple weeks. Tried talking nicely, the answer is one - by law, they say, I have full right. Yesterday I saw him entering the apartment with some lady, clearly not his wife, hugging and kissing on the way. When today another session of boomboomdoodoo started, I called him and declared that if I hear music and bass from his side one more time, his wife will learn about certain lady guests appearing in her absence. Seems the problem is solved. And how do you cope with such neighbors?""",
438697: """xxx: In the ponds crucian carp bites until 8:00 AM, then security chases you off.""",
438698: """Article about the tariff lineup of the capital's mobile operator. First comments:

BlackD: Wow your prices in Russia. I'm from Tallinn (Estonia) myself. My Tele2 package costs 8 euros. It includes free calls within the network, 1000 minutes to other networks + unlimited internet. And no roaming within the country...

Dude: Found what to compare with. We also have no roaming between Northern and Southern Butovo""",
438699: """Tusy:
how's your health??

Onyutko:
normal already)) lay flat all weekend, quietly moaning *CRAZY*

Onyutko:
the cat perplexedly circled around me and tried to poke my nose with her paw: like, not dead yet? no? then MEEEEEOWFEEEEDME!""",
438700: """From the comments under the story:
aaa: So did he fck her? Is there a continuation?
aaa: "Killed", the word "killed" should be there!""",
438701: """aaa: Recommend cheap and recognized training in civil law (second degree)
bbb: Faculty of additional and second higher education, specialization POHD
aaa: POHD?
bbb: Legal support of economic activity, not dontgiveadamnaboutthediploma ;-))""",
438702: """LJ stannum99:
I'll go crazy from students someday. Today at the appointment a young lady with a concussion, broken nose and a bruise across half her face. They were riding down the dorm stairs in a basin...""",
438703: """xxx:
Well, the jokes at work today were all mostly pretty silly.
The sweet tooth, used to drinking morning tea with jam, had both teaspoons hidden and they tried not to laugh, watching how she, unable to give up her habit, fishes strawberries out of the jar with a tablespoon and eats them. The guy who likes to take off his shoes at his desk and, stretching his legs forward, display his by no means designer socks, got worn-out house slippers size 46 put by his desk, so he'd feel at home.
Boring, in short, no special creativity.
But they pranked our acquaintance today so pranked, she'll remember it for long. Early in the morning OMON barged into her apartment with a search. And she, poor thing, has an adult son, a doofus of far from ideal behavior. True, it turned out right away that OMON mixed up the apartment a bit. But from such jokes even in the evening she still looked somewhat stunned, and her hands shook so that the spoon clinked in her coffee.""",
438704: """Review of the internet provider's work:

Good internet I'll say, constant, temporarily rarely it's not there, then I cry bitterly. We all cry bitterly here when sometimes internet is not. But soon the provider's people fix it and then we start laughing loudly, slapping each other on the back, saying - all good, internet is back now.""",
438705: """about meeting the wife, walking with the kid

Kirill: Den, we met your DNA)
Denis: )) only half""",
438706: """ххх: I'm going night-night
ххх: lying down cozily on the toilet tank and falling asleep...
ууу: you sleep in the bathroom, or what?""",
438707: """Russian Post got a time machine.
I get a "Notice". But for some reason "secondary".
Went to the post office. Searched for about five minutes — no registered letter. claimed that "the mailwoman hasn't returned it to the main post office yet". Why "secondary" — they wouldn't admit; and there was no first notice.
Okay, came after 4 days. Not there and never was.

Left my phone number. Next day they called: "We had a computer glitch, you received this letter in 2015".""",
438708: """xxx: "And on the railway don't forget the two-wheeler" (c) K. Prutkov
yyy: what is a two-wheeler? is it nippers to crack two nuts at once? did they make them from rails?
zzz: Well at least they didn't ask "who is K. Prutkov"...
yyy: a collective humorist from a century ago, unclear why he became a classic. nowadays such humorists are like Comedy Club beyond the bathhouse
zzz: and about the two-wheeler no clue?
yyy: no. google doesn't count. Probably, if we're being serious, a narrow-gauge? a cart? a carriage for 2 horses? a sled cart?
zzz: Well, if we're being serious - a horse-drawn cart with two wheels. But should one be serious, really?
yyy: ah, the gladiators' segway?
zzz: Well, not quite a segway - the two-wheeler has a third support in the form of the horse itself.""",
438709: """xxx: Misha, what did you say about not being allowed to fish until some date?
yyy: you can
xxx: And with a spinning rod from the shore now can?
yyy: you can
yyy: only I'm not the fish inspector :)
xxx: So what's not allowed then?
yyy: everything's allowed
yyy: only I wouldn't show my ass in public
xxx: You're deceiving me
yyy: well okay
xxx: I would""",
438710: """My grandma almost put my brother in the nuthouse once because of Yandex. They live right next to a tram stop, so he dressed, put on shoes and in his jacket sat on the pouf in the hallway (winter, didn't want to freeze at the stop) and stared at his phone, tracking his tram in the app. When grandma came out of the kitchen and to the question "What are you doing?" got the answer "Waiting for the tram", she chased the 19-year-old moose around the apartment for half an hour with a kitchen towel, shouting "damned drug addict!\"""",
438711: """xxx: Yesterday I called with a rejection the person who made 28 mistakes in 3 pages of a test task. He said he's ready to dispute the result and rewrite if needed.
xxx: We, damn it, are not ready. Half the day these three sheets walked through all departments. Someone spat coffee into them, they were being photocopied, after lunch I personally saw the neighboring loaders laughing over them.""",
438712: """AndrewVK> Just remember that the tip of a pepper is usually not bitter at all, but at the base the heat is maximal.

buslav> And the seeds are even more maximal. I once ground a pepper pod for pizza, tasted it, seemed totally weak, so I decided to grind it in the blender together with the seeds. The result was a pizza with the taste of pain and the aroma of despair, impossible to eat.""",
438713: """In a not-the-cheapest lingerie store:

- Here, try on this swimsuit.
- No, I don't like the color.
- Good color!!! Try it on!

Oh this Russian service...""",
438714: """Lada: What are these white cylinders on top of the computer desk?
Shinji: I was shocked myself)) It's a router. We categorically ran out of the four standard RJ-45s, even with Wi-Fi, and dad bought a second router.
Lada: Why are there three of them?
Shinji: because the third cylindrical object is toilet paper, which I constantly use to wipe my face and the monitor from dust.""",
438715: """xxx: To sit at the emergency exits, you need to meet certain requirements. Physical ones first of all.

yyy: Yeah right... Every time I fly "Aeroflot", there are either elderly women sitting there or an old grandpa :)

zzz: Oh, that's the most reliable crowd. You can't scare them, they've been through fire and water and won't back down before anything. They'll stop a horse at full gallop, evacuate the whole plane, and grandpa will also lead everyone by partisan trails to the right place.""",
438716: """leha_chifir: In the army in peacetime quite a few die too. I remember how they ordered us to glue into our military IDs a reminder about the ban on picking and cooking mushrooms. Because somewhere soldiers fried some and got poisoned.
And after some idiots got maimed transporting logs in the bed of a military truck, we were forbidden, having chopped firewood, to ride to the camp by car. Like tired fools we walked behind the car. Someone joked: a funeral procession. Killed a tree and walking sad behind the coffin.""",
438717: """xxx: Remember: there are six ways to waste time and money: drunkenness, night wandering, visiting fairs and celebrations, gambling, bad company and laziness.

xxx: these are the basics of Buddhism

yyy: Oh, that's the description of my weekends!""",
438718: """xxx: Pashka, you're a smart cookie
ууу: Damn! I always knew it! And how did it manifest, if not a secret?""",
438719: """x: There's something scary pouring out there, and I need to go to work.
y: For such a case stockings from the OZK are good
x: What's that?
x: Omsk pantyhose factory?""",
438720: """XY: Maybe a walk tonight?
XX: No, I planned to rest
XY: We can together
XX: I planned to sleep
XY: I can continue the thought...""",
438721: """xxx: You don't get it. The point isn't in all this paraphernalia. Cyberpunk is when you sit in your village in a wooden outhouse over a cesspit and watch full-HD video on YouTube on a 5-inch smartphone over 4G. That's real cyberpunk, whose main postulate is - high tech, low life.
yyy: And in 5 years you'll be sitting on the same toilet, but in a virtual reality helmet. Cool, right!
zzz: The cyberpunk we deserved.""",
438722: """xxx: 
A sticker from a tape recorder saying "CLEAN SOUND. POWERFUL BASS." looks luxurious on a Chinese-made washing machine.""",
438723: """xxx: The team lead just made a lucky slip of the tongue: "So, how's project XX? Can you see the end of the world in the tunnel?\"""",
438724: """Boss: you're working kind of slowly, need to increase the RPMs.
me: and I'm like a diesel, small RPMs, but colossal torque, pulling for the whole department.""",
438725: """Andrew_T: Is Zenit out of its mind? tickets for CSKA from 2400 to 5300 rubles
Olga: *mumbles* and you've never bought tickets for Lenkom plays? What makes footballers worse? In acting terms they'd even give a head start)))
Torres: Actors would've played football better ;)""",
438726: """Comment on the book:
The author is famous for botching excellent novels a bit more than completely in sequels. And this time didn't even bother, botched right away. A solid D.""",
438727: """Evgeniy: how's the musico?
Evgeniy: what are you listening to?
Tatiana: something like dudududu du dududu du du
Evgeniy: and in the background tyts tuts tyts tuts?
Tatiana: yeah
Evgeniy: super. sounds like we're listening to the same thing
Tatiana: you're listening to this one too?""",
438728: """xxx:
For example a duvet into a duvet cover - you can't stuff it in just like that.
But as soon as it gets into the washing machine, this textile monster devours absolutely everything into itself, down to the last sock.
Is that - the washing machine's aura at work, or is it stress-eating there...""",
438729: """ххх: cleaned up the cubby, got rid of the box with old hard drives, cables and all kinds of junk. Shurik came to visit and said he needs it )))
ууу: Shurik is like that. Offer him syphilis for free, he'll say give me two""",
438730: """xxx: what to do if mysql_num_row() nothing returns?
yyy: cry.""",
438731: """xxx: I'm pregnant. And, apparently, I searched Google so much for maternity clothing stores that contextual advertising offers me to buy it WHOLESALE.""",
438732: """In the bookstore they rearranged, expanded the foreign languages department. Allocated a big shelf for Japanese, Korean and Chinese... and on the bottom shelf higher mathematics cozily settled.""",
438733: """...and once on arriving in Budapest we went to an ice cream cafe.

there they had such ice cream, then the latest craze, - when from some fruit they scrape out most of the pulp, mix this pulp with the actual ice cream and put it back.

They brought it, but the ice cream was frozen into stone - not just a spoon, a knife wouldn't take it. It only shaved off micron shavings.

Then we asked to slightly warm the ice cream in the microwave.

The waiter was baffled, but warmed it. Slightly. So that, although it became possible to pick off pieces, our palates frosted over.

Then we asked for tea.

But the tea turned out like from the mouth of a volcano, you couldn't even take the cup by the handle.

Then we asked to bring ice for the tea.

Then the waiter asked where we were from. And, learning that we were from Russia, nodded and, reassured, brought the ice. His picture of the world fell into place with a distinct click :)""",
438734: """A friend told (Z):
A girl (G) came to order business cards, I asked her to write the info on a slip of paper, asking about the color scheme.
G: well I want the letters to be golden like, the background blue like...
Z: will there be anything on the card besides text?
G: well I dunno... do you have a brain??
Z: uh.. yeah I think so))))
G: well half a brain will do""",
438735: "xxx: why does everyone make heated floors, is it that cold?\nyyy:\xa0xxx, it's a house)) Not an apartment, where below, above and beside are the same warm apartments)) Here below is earth, above is winter, on the sides - mortal reality)",
438736: """xxx: You see, our company has such dynamics...
yyy: That's turnover""",
}

if __name__ == "__main__":
    print(json.dumps(EN, ensure_ascii=False))
