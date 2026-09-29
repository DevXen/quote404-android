#!/usr/bin/env python3
"""Translate bashim chunk_25 batch 3 (input lines 201-300, ids 407274-407373)."""
import json

EN = {
407274: """My husband was a witness the other day.

Strogino.
A guy is walking down the street with a phone, muttering into it:
- Yeah. Yeah I'm here, arrived. Walking along the railroad. YOU'RE the stoned one! A railroad, and little red trains ride on it. Yeah, a tram.""",
407275: """P> An ostrich also has wings, but it doesn't fly. Its legs are developed, but its wings aren't. It's a question of evolution. An airplane is another matter, I just don't get how they fly.
T> An airplane's wings are developed, and its legs aren't — obviously.""",
407276: """Kraft: what are you up to?
Lexw: Resting. Tired, read a book this morning. Heinlein, in the original, in English.
Lexw: They sat me in the office for a while, next to the boss.
Lexw: He thinks I'm reading documentation.
Lexw: he comes up, looks thoughtfully
Lexw: nods or says "mmm... good job"
Lexw: and walks away without questions.
Lexw: I can't tell him I figured out the Cisco this morning in like 30 minutes.
Lexw: he might think configuring Ciscos and showing sparrows the fig is the same difficulty level of work.""",
407277: """Admin: You need to wash your mug sometimes, or its volume decreases.""",
407278: """ilya: vova, in your whole life have you read anything besides WoW CD-keys?""",
407279: """xxx: he's such a doormat, what did she see in him?
yyy: maybe 100% cotton?""",
407280: """xxx:
Today I found myself in the role of Ostap Bender: I got run over by a horse. Walking along the pedestrian road and then this miracle rams into my back. The horse was on a bicycle and in female form. She was cursing and demanding apologies.""",
407281: """1: you've been staying late at work ;)
2: well there's a chair. Can't stand all day, can I""",
407282: """From mail.ru answers:

Question: Atheists, what are you afraid of?

Answer: Afraid of gorging on cucumbers, washing down herring with milk and then suddenly remembering you have to go to a seminar or negotiations soon""",
407283: """hozzzhar: oh, that's all bullshit
hozzzhar: my wife switched to Linux because Solitaire there lets you undo a move""",
407284: """The chat has people with the following nicks: evil killer, BlackForce, eXtreme_Monster, Dark_Lord, Maniak, blood killer, Death, BlackDevil, Falling Angel, Lucifer and Slaughterer.

** "Rosochka" enters the chat.
Rosochka: What, did you all conspire or what
** "Rosochka" leaves the chat.""",
407285: """xxx: And how's her figure?
yyy: Well, how do I put it... Flat.
xxx: What's that?
yyy: 90x90x90
xxx: At least she's got tits )
yyy: No tits. There's a chest circumference.""",
407286: """myiworm: My cat once hunted a pigeon the same way, sneaking, sneaking, and fell asleep halfway.""",
407287: """niceDEVIL: I'm in shock
niceDEVIL: either they bring a printer with a church candle or there's a Nurofen pill inside
niceDEVIL: are they trying to heal them like that?""",
407288: """xxx: Korea would've won 10:0 if they played against the Terrans""",
407289: """Polinka: lightning ( I'm scared (
CaspeR: don't be scared, if it strikes you won't have time to feel it""",
407290: """My neighbor complained about snoring from our apartment: "Tell your husband to stop!". I sleep soundly anyway, and decided not to bother my husband. Until one night I woke up in the middle of the night and saw him quietly sleeping on his side, pressing a pillow tight to his ear...""",
407291: """mcmartyn: every time on a train or plane I hope a fat sweaty guy with shortness of breath sits next to me. then, if SHE sits down on the other side, I'll look good against his background.

mcmartyn: although, who am I kidding...""",
407292: """[amst]: psh
[amst]: I need you as a man
Gehennas: oh yeah?
[amst]: look
[amst]: suppose you're a girl""",
407293: """xxx: And our dad is a programmer. And so, when all normal kids play wizards and cast spells on each other, our Vanechka plays high-voltage EIM and imposes disturbance on the setpoint channel. And viciously whispers: "You won't beat me, Siemens PID controller"
xxx: I'm afraid to send him to school.""",
407294: """xxx oh, my friend has a chihuahua, the poor hua constantly gets kicks in the butt from the cat, she's already totally oхуа'd by it, but can't do nihуа about it, since the cat is 4 times bigger )))""",
407295: """<xxx>
Recommend an OS for a Network Access Server
A couple are currently running under Alt, the rest on FreeBSD. Need to set up one more server now

<yyy>
install Windows, Cisco, NetWare and then quit""",
407296: """Dinarushka: for a while I wanted a stressful irregular job, to somehow justify having no personal life""",
407297: """I work at a mobile company in the call center. A subscriber calls and asks:
- Can our phone be tapped?
- Technically it's only possible for law enforcement. If you're not doing anything criminal, you won't be tapped.
- We're in prison, that's why we're asking!
... The irony of fate...""",
407298: """akcuc:
Sometimes I hate my job. And sometimes I'm a day off.""",
407299: """xxx: Went hiking with friends, the weather was utterly disgusting, rain then downpour
xxx: One very important thing I learned for life: In the rain you can eat soup forever.""",
407300: """xxx: the best gift is a book!
xxx: an e-book
xxx: in a laptop""",
407301: """Fountain: I can already picture the next championships. Two teams come out onto the field, line up... the anthems play. In the middle of the field stands a pedestal, on it Paul in an aquarium. The opening part is done, the teams gather on opposite sides of the field. The referee blows the whistle and Paul is served two dishes, he chooses, the winners rip off their shirts, run around, yell, rejoice and hug.""",
407302: """GRRemlin: Fuck, dropped a terabyte HDD and caught it ten cm from the floor. In a second all the files from that HDD flashed before my eyes...""",
407303: """xxx: My younger brother just came up to me with a "Kiev" film camera and asks "Where do I look at the photos?\"""",
407304: """xxx: And today instead of my male dog I registered my daughter for the dog show
xxx: In the "name" field I wrote my daughter's full name, in the "parents" field - myself
xxx: Now my daughter is of the staff breed
xxx: I wouldn't have even noticed if a reply hadn't come from the club representative in the evening that in the "parents" field you write not the owner's surname but the dog's parents. And that the male dog is called Eseniya Borisovna didn't bother them""",
407305: """Sendzhak: found a way to sit and work in the heat! you take and wet the top with water – the t-shirt, the blouse and voila – you can sit and work
Thistle: found a way to distract all colleagues from work. told everyone I'm sitting at the comp in a wet t-shirt""",
407306: """xxx: You're the zoophile! Gayka is not an animal, she's a GODDESS!""",
407307: """xxx: if you mix a barrel of shit and a barrel of statistics you get two barrels of statistics
xxx: I keep remembering the statistics course.""",
407308: """xxx: You can skip studying and work as a sales consultant
xxx: Or you can get a higher education and work as a senior sales consultant""",
407309: """Cyberpunk: the most popular first phrase on any hike: "You know what we forgot?!" """,
407310: """xxx:
"Luntik" magazine
Game task - "What came first? Connect the pictures with an arrow in the right order"
Task #1
Cloudy, a rain cloud, puddles
Task #2
Chicken, egg

xxx:
Tough Russian kids solve philosophical problems as learning game tasks.""",
407311: """len': I noticed - all my boyfriends are of one zodiac sign
piece of something: aries?
len': Yes! How did you guess?
piece of something: Aries (same as ram) — a male sheep.""",
407312: """xxx: no, getting up early in the morning is really hard if you don't have to go anywhere
xxx: and if you do, it's even harder ))""",
407313: """dedmorozzz: overslept for work today
dedmorozzz: I call, say there's no water
dedmorozzz: I go to the bathroom - and indeed, there's no water
dedmorozzz: next time I'll say the door got blocked with money from the inside""",
407314: """vovenarg: ffmpeg was swearing at 6-channel
resetled: A thousand devils, 6-channel!""",
407315: """# Mikhail: Please tell me, if I'm going to Sevastopol on someone else's ticket, what problems could I have? There's no way to exchange it anymore. All other documents (passport) are in order.
# Denis Major: there can be one problem - they won't let you on the train.""",
407316: """XXX: life is shit. I'm missing a GOAL!
YYY: Break one big goal into several small ones. For example, my friend's goal is to fuck Angelina Jolie. The first small goal - cure impotence""",
407317: """xxx: he-he-he, Fursenko promised that by 2012 all Russian students will use e-diaries :)
yyy: yeah, "Petrov, diary here!" "- Marivanna, can't - DNS is down" ))))""",
407318: """nico-izo: In a Gentoo user's mouth the phrase "Bodybuilding" takes on a special, secret meaning...""",
407319: """xxx: Vika is giving birth already )
xxx: just wrote on ICQ
xxx: contractions started in the morning, they took her away
yyy: fucked up generation... :) "Vika's giving birth, just wrote on ICQ" :D""",
407320: """xxx: You don't get it, running from ticket inspectors on commuter trains is fun and exciting.
xxx: It's like playing cops-and-robbers for money.""",
407321: """xxx: There's no profanity filter that could stop a Russian!""",
407322: """From a forum

Masteroman: Work for everyone. Looking for employees of any age with or without experience.
...
Valyunchik: for work at work in the position of employee, payment in money!
EDW: I'm an employee with experience and without.
kintis: You're not suitable. Any age required.""",
407323: """Lightning: Pasha was telling yesterday how the cat again knocked the long cactus off his desk. The setup there is like this - Pasha sits at the desk, in front of him sits the cat, and behind the cat sits the cactus. So Pasha's story started like this - I sneezed...""",
407324: """<REal_SM> today I sold four rotary phones. caught myself thinking "did I delete all entries from the phone books?\"""",
407325: """y: how's work goingg?
x: without the extra g. You?""",
407326: """Mora: no, he really is an amazing person.
Mora: having managed on the road to kiss a neighbor's foreign car in the rear, he decided to settle it with money
Mora: and then, he was going on vacation
Mora: and then, not finding the neighbor but finding the car, he decided to put the money in an envelope and slip it through the gap of the slightly lowered window
Mora: and of course, having watched movies, he decided to lower that window himself.
Mora: well then of course he paid for the rear, and the window, and the new window-lifter mechanism...""",
407327: """Shkoda: I'm an Engineer
IP: engineer of what?
Shkoda: Measuring equipment
IP: can you now use a ruler professionally?""",
407328: """xxx: enough with the self-flagellation?
xxx: I'm a total idiot myself and whatever, I live with it somehow""",
407329: """Piu: I need to cause one person inconvenience via mobile phone
Piu: what do I do?
GREGox: hit him on the head with a Nokia 3310?""",
407330: """Kondrat: So, how are our lovebirds?
Dimon DJS: Oh, they're already in the immediate vicinity of the immediate vicinity...""",
407331: """Aspid: How not to fall into depression?
dryer: You need to warn them, there in the depression, in advance, not to let you in!""",
407332: """xxx: They recently hired a sysadmin, he once served in the Northern Fleet, that's where he supposedly learned to admin. And the funny thing is he's got a tattoo on his shoulder "Server Fleet" )""",
407333: """Dark Stranger: Yeah-yeah, go ahead, go ahead. Just remember that the line between "I'll just tweak it a bit and it'll be good" and "Oh fuck, what have I done!" is veeery thin :) Good luck)""",
407334: """xxx: Let's call it a streak of bad luck.
xxx: But really it's a fucking clusterfuck.""",
407335: """Volshebnitsa: By the way! today I'm out choosing a bed. the slats, the base. a guy is advising me and offering different options, and I'm rejecting them because I need an iron base. I say: "it's more reliable. I'm buying this one because the old one broke"
Volshebnitsa: THE WAY HE LOOKED AT ME!!! )))
Kostya: oh you naughty girl!! )
Volshebnitsa: well, I didn't explain to him that my five-year-old nephew came to visit...""",
407336: """xxx: And all murders, in their opinion, ended in death.
yyy: Phenomenal""",
407337: """xxx: Retiring no longer leads to a sharp drop in living standards. For example, Murtaza Rakhimov's pension is three times his salary.""",
407338: """Jolano: What are you doing?
Villikan: Trashing the apartment )
Jolano: ???
Villikan: My parents figured out several times, by the ideal order in the apartment, which never usually happened, that while they were at the dacha I was bringing girls home. And today they're coming back again )""",
407339: """The heat. I look out the window, a big black dog is running around the yard.
I call my wife: Look, what a huge poodle! Looks like it had shepherds in its family!
Wife: Wow, only not shepherds but newf... HOLY SHIT!!! They shaved a diver.""",
407340: """stuck in traffic, climbing up an incline.
rushan: at moments like this I want an automatic
zorro: even if you shoot everyone, they won't leave here.
rushan: an automatic gearbox!""",
407341: """Her: And they cut our water for 10 days, another scheduled outage, so get ready too.
Him: WHAT?! Why don't they go to hell?!
Her: They won't go. They've gone there so many times they've settled there forever and now they're cutting off the water right from there.""",
407342: """From a discussion on how not to sleep through the alarm and get up at 6.00.
xxx: You need to drink three glasses of water, go to bed and then I'll wake up by 6 o'clock because I'll want to pee.
Dad: you'll wake up at two and at four, and at six you'll sleep like the dead.""",
407343: """xxx: You need to live so you don't fear picking up the phone from an unknown number""",
407344: """Topic on a forum about self-cleaning cat litter boxes...

xxx: There are versions that clean immediately. There's another problem there - cats who think they need to go right into the fresh litter. The cat goes, the box cleans, the cat goes, the box cleans, the cat strains but goes, the box cleans, the cat has a hysterical freeze in the litter box.""",
407345: """xxx: What's a vacation?
yyy: it's when you can be late to work and nothing happens.""",
407346: """xxx: photographing proof that I'm not a gnome girl)
yyy: the skills panel?""",
407347: """Dania: yesterday morning I look at Yandex, and in the job listings: sapper, APC driver, tank driver-mechanic.
Dania: think it's time to watch the news...""",
407348: """<beapiv> Guys tell me what's wrong, my WoW won't log in, it says HOLY -- no access ((
<25_01> and what holy did you want?""",
407349: """xxx: just had tea with grandma, she kept soaking the crusts
yyy: ?
xxx: what do you mean "?", she was soaking the crusts, I'm telling you
yyy: well tell me
xxx: damn, she was dipping the bread crusts in her tea.""",
407350: """Alka:
walking a few days ago at 4 am from the store with macaroni. well, 4 am, naturally no makeup, home t-shirt, hair in a bun, glasses, the whole thing... a guy comes up to meet me (and I'm with macaroni, yeah, I'm totally up for meeting people), we talk for like five minutes while I walk home, I lazily brush him off. and he tells me "and you also love the band Aria". I stared at him, I say, ten years ago I used to listen to that. and he says: you brushed me off 10 years ago already, we met just like that and I owe you an Aria cassette, he says... we even exchanged numbers over that.
it's a tradition now - brushing him off once every 10 years.""",
407351: """Silencio: and I'm an only child. I want an older brother, but mom for some reason doesn't agree.""",
407352: """xxx: They'll soon jail me for mac
yyy: Eh, dude! How so, and now what? Drying rusks and packing warm socks?
xxx: Damn... At work. At work they're moving me to a new comp. Moving me to a Macintosh.""",
407353: """arseniiv: First prove that the product of any n consecutive numbers is divisible by n!
Wadim: is that a factorial or an order?""",
407354: """A: Well I have this problem
A: You see, I'm used to loving irrationally...
B: Sash, I understand everything of course, but don't extract the root from boys.""",
407355: """xxx: no, you just don't get it all. he and I have been studying together since first grade. then went to the same group at uni, driving school together. worked together... we're going to the army together. even my son looks like him. he's my best friend! I don't doubt his loyalty one bit..

xxx: FUCK! MY SON LOOKS LIKE HIM!!!!!!!""",
407356: """yyy: a sheet of paper doesn't DROWN! why?
xxx: mysticism
xxx: pfft
xxx: physics!""",
407357: """MasterTO: They'll never prove I watched it. Downloaded - yes. Didn't watch. I'm a psycho, I download movies, write them to flash drives, feed the flash drives to stray dogs in mincemeat, it gives me an erection, and it's not prohibited by any law. In the world.""",
407358: """Mindwar: It's unbearably hard to talk about economic progress in a country where its main engine is the kickback :)""",
407359: """L: Dachas were invented by Satan, as little branches of hell""",
407360: """niceneasy: the boss sharply raised the office guys' interest in my tits
niceneasy: left two folders of docs on his desk, stuck a post-it: study both of them attentively!
niceneasy: he returned them
zap-it: let me guess. and he wrote on your note something like, "studied, felt, enjoyed", for the amusement of passing colleagues?
niceneasy: if only. He couldn't think of anything smarter than sticking my note into my badge on the jacket hanging on the chair
niceneasy: walked around all day not getting why everyone was studying it all so attentively""",
407361: """ANNI: come up with a VK status for me
ANNI: so He knows he's a losre, and I'm awesmoe
Lyokha-ha: I think you can just leave it like: "You're a losre, and I'm awesmoe.\"""",
407362: """<inok> well a buddy of mine once nearly got killed on a GTR
<inok> cops pulled him over and noticed the wheel was left on one nut
<inok> he nearly shit himself
<inok> was doing 150
<@Poooh> I also had a buddy driving like that with a hernia )
<@Poooh> was hauling from Komsa, also thought he'd make it. got out to take a leak, kicked the wheel - it blew
<@Poooh> shit himself too in the process""",
407363: """Quiet Spectator: I'm walking down Nevsky in the evening, all positive, having shed one important and painful tail, Aquarium in my ears, a bunch of new colorful summer clothes in my bag, the sun is shining, warm, good...
and then coming toward me DARTH VADER WITH A BALLOON!!!""",
407364: """xxx: you're a loser
yyy: lol, prove it
xxx: youLoser("no girlfriend", "living with parents", "about to be kicked out of uni");""",
407365: """xxx: talent - for some it's like Ilya Muromets, sleeps till 33, for others it's like Cthulhu... *ROFL*""",
407366: """<ivan> girls wanting to celebrate Victory Day write me in PM
<nwu> you meant grandpas?""",
407367: """Arsis: just looked at the Q key on the keyboard
Arsis: very interesting key :)
Arsis: 2 letters, what a meaning""",
407368: """Odo: Got a haircut today. The girl cutting was about 20. She had a blouse on. The top button was undone. I sat there, enjoying it.
Odo: "How do you want it cut? - Slowly!\"""",
407369: """nud: damn, I'm like a Sim. went to bed at 5, woke up at 9, ran errands, came back - ate dumplings and sleep. what kind of idiot is playing me? :)""",
407370: """Bobmarley: And why did my uTorrent start showing American flags instead of Ukrainian ones?
Uncle Vova: Go watch the news - you'll find out...""",
407371: """from the corporate correspondence of the head of our IT department (hereinafter IT) with the head of the accounting department (hereinafter U)

U - This file won't open for some reason!
IT - What are you opening it with?
U - With the mouse.
IT - With what program?
U - I don't know. I open all files with the mouse.""",
407372: """Pukh: And anyway. When I have my own apartment - I'll definitely put a power outlet in the bathroom.""",
407373: """iSmersh: I also often dream bullshit on near-computer topics
iSmersh: last night I'm sleeping, my girlfriend lying next to me, and I'm dreaming that summarized routes from eigrp are coming from her""",
}
