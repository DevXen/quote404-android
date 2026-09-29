# -*- coding: utf-8 -*-
"""Translate chunk 22, records 400-499 (IDs 404469-404568)."""
import json, re

PATH = "/home/hatch/workspace/translation-work/bashim"
SRC = f"{PATH}/chunks/chunk_22.jsonl"
OUT = f"{PATH}/out/chunk_22.jsonl"

src_lines = open(SRC, encoding="utf-8").read().splitlines()
by_id = {json.loads(l)["id"]: json.loads(l) for l in src_lines}

batch = [json.loads(l) for l in src_lines[400:500]]
assert len(batch) == 100
ids = [r["id"] for r in batch]
assert ids == list(range(404469, 404569)), (ids[0], ids[-1])

# Homoglyph nick from 404554, taken byte-identically from source
nick554 = by_id[404554]["text"].split("\n")[0].split(":")[0]

en = {}

en[404469] = (
'<Archy> So you say car alarms screaming at night piss you off. A week ago this happened - couldn\'t sleep half the night, at half past two went to the kitchen to make tea. Suddenly I hear - an alarm howling, and my car\'s alarm is a beast, howls so that the cars standing next to it start howling along. I look out the window and fuck - mine\'s blinking and two ghouls are digging around it.\n'
'<Archy> Adrenaline in my head, grab the bat, fly down to the first floor, run out of the entrance - those two saw me and immediately bolted, I chased them for two blocks, then spat.\n'
'<Boryan> did they steal anything?\n'
'<Archy> Didn\'t get the chance, and from what I figured out later, they wanted to steal it. Anyway the next day I meet a familiar car owner from the next entrance in the yard - says, my alarm woke his whole family (and it was screaming the whole time I was chasing those fuckers), cursing, strongly recommends I take his example and, quote, "turn off the damn alarm at night".\n'
'<Archy> And the next night his car got stolen. Keep turning it off, woodpecker.'
)
en[404470] = (
'milechka: traded my beloved Max Factor mascara... bought damn Lumene...\n'
'milechka: how could I listen to the saleswoman who recommended a blueberry-flavored mascara?'
)
en[404471] = (
'der_sicht: Today I had a meeting at work. The bosses ask - like, everyone take turns saying what your main driving force at work is\n'
'\n'
'der_sicht: Well I blurted out - the energy of carbohydrate combustion and ATP cleavage'
)
en[404472] = (
'Boblin: I came back yesterday morning\n'
'Boblin: Here, hauling stones from the forest since morning...\n'
'WONDERFUL PERSON: from the mine?\n'
'Boblin: Yep, playing Warcraft. Mom pressed "build bathhouse", dad said: "Need more stone!" Yura was sent to work...'
)
en[404473] = (
'Bav_Killer™\n'
'Just rode with one guy to the warehouse. When he was filling out the waybill, he asked me what year it is, what month and date. I got really scared! Thought he\'d say - "I need your clothes and your motorcycle!"'
)
en[404474] = (
'sphere: imho, soon apartment sale ads will say "catches free wifi"'
)
en[404475] = (
'Went to the pharmacy with my wife. There I saw a bunch of multicolored, different-sized and different-priced pregnancy tests. Asked the seller what the difference was. He rambled for a long time, and at the end said that among other things the color.\n'
'- And recently, - he said - there were pink ones, specially for women.'
)
en[404476] = (
'xxx: Dimka went to music school after all. Don\'t know what they teach him there, but he\'s already played the funeral march on my ICQ...'
)
en[404477] = (
'From an apartment search ad:\n'
'"...no kids or animals, (have a bike, but it\'s calm)"'
)
en[404478] = (
'xxx:\n'
'My grandma was rushing to some event, had little time and couldn\'t find her jewelry. As a result she put on A FLASH DRIVE ON A CHAIN (mine\'s this yellowish one, damn) and left...'
)
en[404479] = (
'My director\'s getting her fifth call from an insurance agent, our intern picks up:\n'
'Agent: get me N\n'
'Intern (to director): Agent\'s asking for you\n'
'Director (stressed): tell him I\'m dead!\n'
'Intern (into phone, unfazed): she\'s dead.\n'
'Agent: how, already?\n'
'Intern: not quite, but in progress.\n'
'Agent: well maybe, while it\'s not too late, insure her?..'
)
en[404480] = (
'zavtra_v_sadu: They say you can cleanse yourself through suffering.\n'
'Can a potato cleanse itself that way?\n'
'if yes, how do you make a potato suffer?'
)
en[404481] = (
'2 AM, pulling up to a kiosk. Walk up. Ask for tomato juice. Child of the Mountains stares blankly at me. Poke my finger at the carton. Get the juice, walk to the car. Contemptuously in my back: You must speak Russian! TOMATO GIVE!!!'
)
en[404482] = (
'xxx: And I really got it that passing the GIBDD exam is nothing, but passing the driving exam for my husband so he lets me take the car - that\'s a problem!\n'
'xxx: Second month suffering'
)
en[404483] = (
'letter to tech support:\n'
'\n'
'Hello. Yesterday finally finished the site, and today I can\'t get in: to the site - "link doesn\'t work", to the panel and server via SSH - password doesn\'t fit. "Forgot your password" says password recovery is impossible due to error #cp1005, contact your provider.\n'
'P.S. something tells me (the error code) that the problem might have arisen from changing mySQL encodings from cp1251 to utf8, although googling the error points to some "Brewing press - teapot DEKOK CP-1005". Could my host be installed on it?'
)
en[404484] = (
'he: If you want now, come over. Or I\'ll come\n'
'she: Better you. I have further to go.'
)
en[404485] = (
'xxx: They\'re in the sanatorium again, but a different one. % days there, and home for the weekend.\n'
'yyy: Did you write the capital 5?'
)
en[404486] = (
'kamilgr: Well we have a dress code too. At work you have to wear a suit. If you\'re on vacation, you can come to work in jeans.'
)
en[404487] = (
'liko: reeeally want to eat, damn I\'m gonna die right now\n'
'Sandra: hmm... here, eat!!! () it\'s a pie)\n'
'liko: Is it empty?\n'
'Sandra: (2) - here, with meat\n'
'liko: ( @ ) - with meat, that\'s like this )'
)
en[404488] = (
'Paulot: I wanted to buy my dog a spiked collar. Saw such things on Google =-O'
)
en[404489] = (
'xxx: Honey, sometimes I miss you, and think, he\'ll call right now, and sure enough - you call. Does that happen to you?\n'
'yyy: Of course it happens! Like, for example, we\'re busting our asses hauling a 33U LAN cabinet weighing like 100 kg to the 3rd floor with my partner. I\'m thinking - he\'ll call right now. And sure enough - you call! )\n'
'xxx: Asshole'
)
en[404490] = (
'A client orders the same stamp for the fourth time this month. Asking the bosses:\n'
'- Is this guy, like, an idiot?\n'
'– He\'s the CEO, drinks hard. Every Friday gets drunk and loses his briefcase. Though lately he\'s been carrying a bag, briefcases are expensive these days...'
)
en[404491] = (
'xxx: why aren\'t you at uni?\n'
'yyy: ... I got expelled (((\n'
'xxx: hehe, do your parents know? )\n'
'yyy: they found out first...\n'
'xxx: ???\n'
'yyy: they\'re the ones who expelled me!!!'
)
en[404492] = (
'Ezzz-q: I caught the green wave once in my life\n'
'Ezzz-q: bought a cheeseburger at McDrive and wanted to eat it at the nearest traffic light'
)
en[404493] = (
'ххх: You\'re laughing, but near our house there\'s a funeral office, and above it for two years there\'s been a sign: "Zdravsvuyte! We\'ve otrylis!". I get that one letter is just not visible in the fold. But that doesn\'t make it easier...'
)
en[404494] = (
'xxx: Today cleaning the room. Too bad my hands don\'t grow out of my ass\n'
'xxx: Wouldn\'t have to bend over...'
)
en[404495] = (
'xxx: In Linux, the attempt to launch a game is sometimes more interesting than the game itself'
)
en[404496] = (
'Numbe®One: You know your dad killed 2 hares with one shot?\n'
'Numbe®One: And raised a son and a tree.\n'
'Derevo: *ROFL*'
)
en[404497] = (
'xxx:\n'
'in our organization a dress code isn\'t introduced, but is needed :(\n'
'\n'
'xxx:\n'
'once every six months I came in a blouse baring my back when sitting. so our Nikolaich stamped the organization\'s seal on my back while I was sitting at the comp! and then chased me for half an hour with a pen yelling "I didn\'t get to put the inventory number on!"\n'
'\n'
'xxx:\n'
'the guy\'s 70, God grant him health..'
)
en[404498] = (
'Blue Bagel: driving with the instructor yesterday, pulling onto the roundabout: "watch on the right, even though we have priority - they might hit you, especially the ones I taught"'
)
en[404499] = (
'byzon: yesterday did the tambourine dance around the server - wanted to set up the Internet, and it started raining'
)
en[404500] = (
'z_a_r_a_z_a:\n'
'Pashka (husband) got up at night and went about his night business. Sleepy son also got up and pattered after him. Caught up, took his hand and led him back. Put dad to bed and lay down and fell asleep himself. Cause a family man shouldn\'t be wandering at night.'
)
en[404501] = (
'xxx:\n'
'Leaving work. See a guy. A normal guy. Pumped, manly, t-shirt, jeans, could easily pass for an auto mechanic. With a tiny bald chihuahua under his arm. A big mongrel runs up to sniff them, which the guy confidently fends off.\n'
'\n'
'xxx:\n'
'Then the guy climbs the stairs, kisses the little dog on the forehead and booms:\n'
'"Don\'t shit yourself - I\'m here!"'
)
en[404502] = (
'Niece, 8, holding a coconut, declares\n'
'- "It looks like a baboon\'s egg".\n'
'We quietly start cracking up, thinking how the street corrupts the kid. So I decided to carefully do some explanatory work.\n'
'- "Where did you hear that?".\n'
'And she so calmly\n'
'- "Why heard? I saw, it\'s just as hairy".\n'
'Damn, the zoo trip wasn\'t wasted, the kid remembered a lot.'
)
en[404503] = (
'1: you there?\n'
'2: yeah\n'
'1: there\'s an unfamiliar guy here, inventorying the computers. is he yours or should I call security?\n'
'2: yeah he\'s ours\n'
'2: name\'s Mikhail\n'
'1: ok. I\'ll tell the guys to untie him'
)
en[404504] = (
'Sikstep\n'
'Riding the bus down the avenue, the bus stops before the crosswalk at the red light, next to it pulls up a guy on a scooter, not small, stocky, bearded. Stopped, blankly looked at the traffic light, got off the scooter, took it under his arm, crossed the crosswalk =) Put the scooter back and rode on. Everyone envied him =)'
)
en[404505] = (
'1 - decided to get teeth fixed\n'
'2 - and?\n'
'1 - went to a couple clinics to get an estimate for fixing everything, came out 10 grand for the whole mouth at both\n'
'2 - well that\'s fine\n'
'1 - yeah, only at these damn clinics not a single tooth matched )'
)
en[404506] = (
'xxx:\n'
'Colleague - econ faculty grad. In the office canteen, always orders two half-portions of borscht. Asked why, answer:\n'
'- Turns out one portion of borscht with two portions of sour cream.\n'
'\n'
'xxx:\n'
'5 years of study not wasted.'
)
en[404507] = (
'xxx: So you\'re a tourist, a hiker. Tell me, where can I go for mushrooms on the weekend? Just not too far, and guaranteed enough for a frying pan? :)\n'
'yyy: To the market, to the grannies'
)
en[404508] = (
'Tanchik: Where are you going on vacation?\n'
'РыЖиЙ <ёЖж_: to Yaroslavl\n'
'Tanchik: Why there?\n'
'РыЖиЙ <ёЖж_: there\'s a festival\n'
'Tanchik: Is there sea there?\n'
'Tanchik: Planning to?\n'
'РыЖиЙ <ёЖж_: there\'s no sea in Yaroslavl\n'
'РыЖиЙ <ёЖж_: and I\'m not planning to make one'
)
en[404509] = (
'xxx:\n'
'I love him so. He\'s a boy. I search for him, search... And I keep running into seals.\n'
'\n'
'xxx:\n'
'The cartoon "Umka is looking for a friend".\n'
'So true to life...'
)
en[404510] = (
'xxx: Actually Russian roads are made according to Feng Shui, it doesn\'t tolerate straight, even and smooth'
)
en[404511] = (
'xxx:\n'
'Here two female workers got caught figuring out which of them works more\n'
'\n'
'xxx:\n'
'"..all you do is sit on VKontakte, I see you there all day.."\n'
'\n'
'xxx:\n'
'in the boss\'s presence.'
)
en[404512] = (
'xxx: Today at the site two workers, apparently seeing a construction detector for the first time in their lives (a device for detecting cavities, metal, pipes and other foreign things in walls), were testing it on each other. One holds it to the other\'s head.\n'
'xxx: The device beeps and writes "Wood".'
)
en[404513] = (
'ххх: guess what we did in the first class of third year?\n'
'ууу: diff eqs?\n'
'ххх: we had basics of computer tech. and we drew an Excel table in a notebook.'
)
en[404514] = (
'ххх: what are you wearing right now?\n'
'ууу: my room...\n'
'ууу: I\'m slowly taking off... a room for 10000 a month...'
)
en[404515] = (
'xxx: never leave a Rubik\'s cube in the toilet...'
)
en[404516] = (
'A sales-department girl calls:\n'
'- Sash, would it be hard to install Office 2007 for us?\n'
'- What for?\n'
'- Oh, never mind then'
)
en[404517] = (
'Princess Maryana: what about sweets?\n'
'Poil: sweets take long to make. although my last cake was pretty good!\n'
'Princess Maryana: how it sounds! the last cake... I haven\'t even had a first!\n'
'Poil: it\'s not hard, especially in the dorm. take a loaf, take mayo... )'
)
en[404518] = (
'Vitaminka : Do you smoke?\n'
'FrostFX : Like a locomotive...\n'
'Vitaminka : What, that much?!'
)
en[404519] = (
'TunGer: today it\'s two and a half months since my girlfriend and I have had sex every day! wonder how much longer we\'ll last? )\n'
'~ФЕРЗЬ~: Don\'t the periods get in the way? )\n'
'TunGer: she hasn\'t had them yet )\n'
'TunGer: fuuuuuuuuuuuuck!!!!!'
)
en[404520] = (
'xxx: They say ficuses grow better if you talk to them\n'
'xxx: Was moving it, dropped the pot on my foot. For ten minutes helped it grow...'
)
en[404521] = (
'xxx: The phrases "a year ago you asked" and "hastening to inform" don\'t go together in one text'
)
en[404522] = (
'matt: yeah.. of course, the idea of addressing some impressive-looking guy on the first day at the military department as "comrade three stars, two stripes" wasn\'t very good and I didn\'t like painting the parade ground, but now he remembers my name, and I learned that three stars and two stripes is "comrade colonel"'
)
en[404523] = (
'<leshka> hey everyone! who has "final destination 4" or photoshop?\n'
'<Beerman> what, if you don\'t find it, you\'ll draw it yourself?'
)
en[404524] = (
'A colleague has had some stick standing in his office for two days. To the question "what\'s that" he says: Magic staff. +5 to life. First thought: the guy\'s lost his mind on Warcraft grounds. Turned out simpler: it\'s a twenty-minute walk to work through eight crosswalks. And all during rush hour, naturally. You\'ll never wait for them to let you pass. So now he carries a two-meter stick with him. They started letting him through right away. Really five minutes a day saved )'
)
en[404525] = (
'xxx: The most common way to debug five hundred lines of assembler code is a very long, very intent and very thoughtful stare.'
)
en[404526] = (
'egoist: Today the kickboxers of Shadrinsk established experimentally that an ordinary gopnik\'s "turn off the Bull" button is in his head and should be pressed with a precise spinning kick.'
)
en[404527] = (
'ххх: they\'ll call the IT guy soon, let him fix it\n'
'ххх: he\'s so funny working when the insides are visible )\n'
'ууу: uh, the IT guy?'
)
en[404528] = (
'Kolo: Every client should have a comments field\n'
'Kolo: definitely\n'
'differentlocal: yeah\n'
'my provider has about me: "Smart. Refused setup, let him fuck himself now".\n'
'differentlocal: verbatim'
)
en[404529] = (
'xxx: Inheritance is simple. Look, you\'re inherited from class dad, which has the property money. Object wife addresses you, asks for money, and through you gets it from dad.\n'
'xxx: And if you\'re inherited from interface dad, then dad doesn\'t give money, but says: "you\'ve got arms, legs, a head, go earn"'
)
en[404530] = (
'Lyokha Dus: what\'s correct - platina or plotina?'
)
en[404531] = (
'_Astral_\n'
'I once rode rollerblades into an underpass down the stroller ramp. The imprints of that ramp stayed on my back for 2 weeks. )\n'
'spec\n'
'that\'s because you had no handles to hold onto )\n'
'_Astral_\n'
'That\'s because I had no brains'
)
en[404532] = (
'cin: Poor kids, their parents are forced to earn money, depriving them of attention. Kids protest, of course... some. Like my sister, she has a kid - a boy almost 4. My sister\'s the type "I\'m the horse I\'m the bull I\'m the woman I\'m the man". Always building something at home, hammering, drilling. The kid\'s without attention, of course. But he expressed his protest too.\n'
'He brought a hacksaw from the closet, put it in the middle of the living room and shit on it.'
)
en[404533] = (
'winer: and in general, our office is best described by the office fridge with corvalol and vaseline oil standing in the middle of an empty shelf.'
)
en[404534] = (
'A flatmate brought her glamorous cat from the dacha.\n'
'I ask: Probably picked up fleas in a month, roaming your hacienda?\n'
'She (with sincere surprise): No, we have a high fence. Where would fleas come from?'
)
en[404535] = (
'xxx: Look, if I recorded photos to a flash drive. And there was a virus. It lay in my closet for a month... Could the virus have eaten all the photos? because they\'re kind of gone now :(\n'
'yyy: if there was seasoning, it could'
)
en[404536] = (
'Necro KOT: what irony, I\'m urgently (!!!) rushing to the "no problems" company to solve their problems'
)
en[404537] = (
'Standing by a kiosk I hear this dialogue between the saleswoman (п) and a boy (м):\n'
'(м) Can I get Winston lights?\n'
'(п) Are you eighteen?\n'
'(м) Rubles?'
)
en[404538] = (
'inpills\n'
'at least in the residential districts there are already too many dogs.\n'
'\n'
'tritopora\n'
'They\'re countless everywhere. Can\'t walk past unbarked. They\'re afraid of me - I\'m big, but I personally saw how near "Perekrestok" on Chernorechenskaya dogs took a bag of sausages from a lady, surrounding her and growling.\n'
'\n'
'rewol88\n'
'and I heard they jacked a phone!\n'
'\n'
'tritopora\n'
'And drove off in a black BMW.'
)
en[404539] = (
'xxx\n'
'What\'s a synonym for the word \'stupil\'?\n'
'\n'
'yyy\n'
'\'stormozil\'\n'
'\n'
'xxx\n'
'Out of 15 surveyed only three wrote \'shagnul\''
)
en[404540] = (
'xxx: they brilliantly named the construction company - "Tetris".'
)
en[404541] = (
'>> I\'m stuffed! now lying on the couch feeling like Rocky from Rescue Rangers )\n'
'\n'
'>> ) And I pinched my fingers in the iron door ) Feeling like Rocky from Rocky )'
)
en[404542] = (
'xxx: and I was at a bachelorette party yesterday, all the girls were dressed in 50s style, like stilyagi ) had a great time\n'
'\n'
'yyy: And I was at a potato-picking yesterday, everyone was dressed in 90s style like gopniks ) my back really hurts'
)
en[404543] = (
'romanova: Nikita, why does the comp freeze when I press the "cure viruses" button?\n'
'estet: because the viruses really don\'t want you to press that button and push it back'
)
en[404544] = (
'Evangelicall: Now working with dad at the dacha. He went behind the garage for some crap and a wasp stings him in the temple, all by itself. He, swatting, knocks off and breaks his glasses, twists his leg, slips and falls into a puddle.. Cursing, goes for an onion, hits his head on a big branch. Smeared his temple - onion juice got in his eyes. Sitting, can\'t work - eyes watering.\n'
'Here I sit thinking, should I remind him how he asked this morning about the cup I broke, "Who do I take after being so clumsy", or will he kill me dead?'
)
en[404545] = (
'<r.shamilov> The night before an exam is like the night before Christmas. You don\'t sleep and hope for a miracle.'
)
en[404546] = (
'vibro_toy: yesterday came a shaggy black monster, switching from Iola to ADSL, brought a modem..\n'
'vibro_toy: Stands in the corner, watches me configure, jingling chains and pentagrams.\n'
'vibro_toy: Typing the IP, 10.100.110.9\n'
'vibro_toy: he leans in and says: "Can the last digit be 10 instead of 9", and holds out 50 rubles..\n'
'vibro_toy: and only now, helping my brother convert from binary to decimal, I understood the monster\'s satanic plan..'
)
en[404547] = (
'CnaMuk: Today at ethics they told us friendship leads to love\n'
'CnaMuk: That\'s exactly why, Tolyan, I decided not to be friends with you anymore'
)
en[404548] = (
'ххх (22:28:12 15/09/2009)\n'
'Kaluga-1 station, 4 AM, frost -20 degrees. An electric train leisurely pulls up to the platform after the night\'s rest, rolls up to the crowd (in Kaluga everyone always hangs out by cars 1-2), the doors open, the assistant comes out and loudly announces: "Comrade penguins, the refrigerator to Moscow has arrived!"'
)
en[404549] = (
'xxx: yesterday I found two stripes at my sister\'s...\n'
'yyy: your sister\'s pregnant?\n'
'xxx: no, fuck, she\'s a chipmunk!'
)
en[404550] = (
'A friend\'s ICQ status "-15 cm"\n'
'- Did you get a haircut? Or leave your boyfriend?'
)
en[404551] = (
'>Нервная истерика< : Damn, I\'m not up to anything right now, 11 hours of active walking in 12cm heels... Rubbed my feet so bad (\n'
'AprilieЪ: I bought 30 plasters in the train for 30 rubles, they hold really well all day, so I walk in sandals and nothing rubs anywhere .)\n'
'AprilieЪ: And what to do if my calluses won\'t heal?\n'
'>Нервная истерика<: I\'m building Mozilla from source here, later, ok? )\n'
'AprilieЪ: Wanted to write that, judging by our talk about calluses and shoes, we\'re girls after all, saw your last message, lost the desire.'
)
en[404552] = (
'Loky\n'
'"Love is not when they bring you a bouquet of roses and you smell it. Love is when they tell you about 95-octane gasoline all day and you listen."\n'
'Arty\n'
'for sure\n'
'Arty\n'
'by the way do you know why 95?'
)
en[404553] = (
'she: And you know, they say cats look like their owners!\n'
'she: By the way, how\'s my cat doing at your place?\n'
'he: Well yesterday he shit in my boots, hung on the curtain and dropped the curtain rod, broke my favorite mug and now seems to be trying to fuck my laptop. And now you\'re writing me that cats look like their owners, so after all this I look at your cat and think: "Maybe I should postpone our wedding?"'
)
en[404554] = (
nick554 + ': do we know each other?\n'
'mafia.: No (and judging by the nick, fortunately)\n'
+ nick554 + ': likewise'
)
en[404555] = (
'Anders: Imagine, she\'s 40 and she buys a teddy bear in our store, and tenderly hugs it like a child\n'
'Anders: To my question why she needs the bear, she answers: had the same one as a kid, a favorite, but mom cut its body open, took out the stuffing and insulated the apartment windows...\n'
'Doster: fuck...'
)
en[404556] = (
'nadezhda: I was cooking food\n'
'RIO: what?\n'
'nadezhda: was making food.\n'
'RIO: what? )\n'
'nadezhda: was making for you. I\'ll bring you some\n'
'RIO: what? )))\n'
'nadezhda: )))))))\n'
'RIO: baby, will you answer the question "WHAT DID YOU COOK?" )\n'
'nadezhda: salad, cutlets, pasta, compote'
)
en[404557] = (
'boy:\n'
'I have a lot of mushrooms\n'
'want - come over, I\'ll fry them with something!\n'
'\n'
'girl:\n'
'did the socks under the bed actually yield a harvest?'
)
en[404558] = (
'[huragh]: hey asshole stop dumping\n'
'[Барыга]: don\'t give a shit!\n'
'[huragh]: we\'ll take you down with the guild\n'
'[Барыга]: don\'t give a shit!\n'
'[huragh]: are you a bot?\n'
'[Барыга]: don\'t give a shit!\n'
'[GM#03]: Hello.\n'
'[Барыга]: don\'t give a shit!\n'
'[Барыга]: hello.\n'
'[GM#03]: A complaint was filed against you.\n'
'[Барыга]: don\'t give a shit!\n'
'[Барыга]: what kind?\n'
'[GM#03]: You are suspected of using bot programs.\n'
'[Барыга]: don\'t give a shit!\n'
'[Барыга]: that\'s sad.\n'
'[GM#03]: Please turn off the auto-responder.\n'
'[Барыга]: don\'t give a shit!\n'
'[Барыга]: I don\'t know how >_<'
)
en[404559] = (
'xxx: Our institute isn\'t an institute, it\'s Noah\'s ark - every creature gets an extra pair.'
)
en[404560] = (
'XXX: Please explain popularly what distinguishes the concepts honor and dignity.\n'
'YYY: You see, honor is a deeper concept...\n'
'ZZZ: And dignity is a harder one!'
)
en[404561] = (
'xxx: went out to the yard, forgot keys and phone at home, and the door\'s metal\n'
'xxx: sitting, waiting for someone to come out\n'
'xxx: a drunk guy comes up, starts banging the door with hands, feet, tried his head, then took out a key and opened the door\n'
'xxx: must be tired...'
)
en[404562] = (
'<Ostash> Wrote a program for a long time converting info from the old format to the new, launched it - everything worked in less than a second. No, the boss won\'t be happy. I wrote a log, line by line printing the processed data and random delay time. Now - solid and long, there\'s something to show the bosses!'
)
en[404563] = (
'>> we\'re texting with him\n'
'>> true, he doesn\'t answer'
)
en[404564] = (
'zorro: and also, your eyes are shaggy...\n'
'Lina: WHAT!?!?\n'
'zorro: well, I mean, fluffy eyelashes'
)
en[404565] = (
'Exclusive - In general I noticed that when I wake up every morning, I have bruises under my eyes. Not getting enough sleep? Sleep seems normal, keeping the schedule. Go to bed at 23:00, wake up at 7:00. I do get enough sleep, but the bruises are there... How to get rid of them?\n'
'gurzhap - Don\'t fight before bed.'
)
en[404566] = (
'SeeD Seifer: My girlfriend dumped me, and I urgently need something to read where in the end the girl is the culprit of all terrible sins\n'
'Seth S. Svartalf: Read the Bible...'
)
en[404567] = (
'_Master_: the hard drive with porn croaked along with its contents :('
)
en[404568] = (
'xxx: We have a girl here guessing a crossword, went out for a smoke, and we peeked\n'
'xxx: A fruit that karatekas beat, 5 letters - PUMPKIN.'
)

assert set(en.keys()) == set(ids), (set(ids) - set(en.keys()))

# Newline parity check before writing anything
bad = []
for r in batch:
    ru = r["text"]
    e = en[r["id"]]
    if ru.count("\n") != e.count("\n"):
        bad.append((r["id"], ru.count("\n"), e.count("\n")))
if bad:
    raise SystemExit(f"NEWLINE MISMATCH: {bad}")

# <...> token preservation check
bad = []
for r in batch:
    toks = re.findall(r"<[^>\n]*>", r["text"])
    for t in set(toks):
        if t not in en[r["id"]]:
            bad.append((r["id"], t))
if bad:
    raise SystemExit(f"NICK MISMATCH: {bad}")

with open(OUT, "a", encoding="utf-8") as f:
    for r in batch:
        rec = {"id": r["id"], "ru": r["text"], "en": en[r["id"]]}
        f.write(json.dumps(rec, ensure_ascii=False) + "\n")

n = sum(1 for _ in open(OUT, encoding="utf-8"))
print(f"appended 100, out now has {n} lines")
