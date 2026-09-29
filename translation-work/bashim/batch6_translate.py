#!/usr/bin/env python3
"""Translate bashim chunk_13 batch 6 (input lines 601-700). ru is read verbatim
from the input file; only en translations are embedded below."""
import json

EN = {
394746: """tramp67:
There are differences  But would you really give up the subtle erotic pleasure 

Merl:
oh, subtle erotic pleasure is tying a man to a chair, cutting off his access to the TV, the computer and the newspaper, and spending 3 hours telling him how my day went, what Svetka Frolova said, and what discounts there are at Uspensky""",
394747: """xxx: sitting in social studies class, a couple of *blonde* girls are eagerly chattering away, disturbing everyone, the teacher says to one of them - "stop talking already, vika! honestly, you're chattering like a magpie"
A guy from the next row -"gave it to this one, gave it to that one"...""",
394749: """xxx: what's new?
yyy: you can congratulate me, Natasha agreed to go out with me
xxx: you poor bastard...
yyy: why? 0_0
xxx: never cheat on her...
yyy: what's wrong?
xxx: at my last birthday she helped me cut the salads
xxx: and instead of tapping the egg on the table, she smashed them with her fist
xxx: to my question "why" she answered - "old habit"
xxx: never cheat on her
yyy: 0_0""",
394750: """1: Lyokh, did you have a "Kros" bike as a kid?
2: Yeah...
1: I remember envying you when I had a crappy bike...
2: Pash, do you have an Audi A6 now?
1: Exactly.
2: Just so you know, I still have the bike.""",
394751: """parents are on fire! mom, demonstrating typical female logic, is trying to prove to dad and me that a ball is a hollow figure, and a sphere, on the contrary, is a solid.
mom: but what about Christmas tree balls? they're hollow!
dad: damn! and that's really an oversight! well let's call them Christmas tree sphericles!""",
394752: """Damn, I'm already afraid to be in a closed room with her! Sits with her nose in the monitor, indifferent gaze. The cat meows in the kitchen. I tell her:" Go cut the cat a piece of meat or something". Silently gets up, goes to the kitchen. Comes back. In one hand the cat, in the other a knife, in her eyes the same killing indifference, and she asks "which one exactly do I cut?\"""",
394753: """<Nastja> my daughter usually stops crying if I give her the breast
<wRAR> I'd stop too""",
394754: """Kenssi: fuck.... trained the cat.... she's used to the fact that if she bites me or bothers me somehow, I carry her to the kitchen and lock her there... SO NOW THIS BITCH CHEWS ON MY HEEL PURELY SO THAT I'LL CARRY HER TO THE KITCHEN, BECAUSE SHE CAN'T BE BOTHERED TO WALK THERE HERSELF!""",
394755: """дятел: With whose help do I throw a cable across the building tomorrow?
Ac!D: at what time?
дятел:  8:00 AM
Ac!D: with God's...""",
394756: """Оля М. 
Watching Roma assemble the wardrobe

E >|< |/| K
aaah, you can watch these things forever..

E >|< |/| K
and you also run around giving advice, rearranging the tools, getting underfoot

E >|< |/| K
and say, well I saw it should be done like this and not like you're doing it

Оля М.
I already did that, so now I'm sitting on the couch""",
394757: """Он: Do you wanna fuck me?
Она: O_o Where are you from?
Он: German)))
Она: We fucked you at 45)))""",
394758: """Оля 02:11:37
Getting ready for Feb 14?

 dima 02:12:16
yeah, damn. today I went to a surgeon friend at the morgue, we were cutting out hearts.""",
394759: """xxx: How do I transcode a movie from 16:9 to 4:3?
yyy: I'm no expert, but I think you can extract the root...""",
394760: """A guy brought in his system unit for repair and file recovery..
Hooked up the disk, first thing flying at my face.. "Read before doing.txt" 
"If you recover the photos where my girlfriend is naked... don't post them on the net"
I recovered the files with every possible program, tried seven or eight... spent 3 days on it
Recovered all files except 2 Word docs.
Nobody ever motivated me with such a cruel trick=((""",
394761: """XXX: Studying this system...
YYY: And how is it? Productive?
ХХХ: the friendly and intuitive interface is resisting(""",
394762: """xxx: In our men's toilet there's a separate stall with two urinals installed, so some wise guys hung a sign "office for resolving business conflicts", attached little hooks above the urinals and hung a ruler on each...""",
394763: """blaq: A bot once banned me for the word "харакири" O_o
VooDoo_GoD: blaq, harakiri is done with a katana, a katana is such a sabre ("сабля" - get it, it has "бля" in it), ban for the word "saBLYA". Associative moderation, what the hell ^_^""",
394764: """Алёна : I was sick......
Рыжая борода : Oops... Sympathize - currently doing the same thing...
Алёна : who are you rooting for??
Рыжая борода : I'm for the leukocytes)))""",
394765: """winzard: If 1000 spam letters land in my mailbox a day, do I have to register as a post office?""",
394766: """S@NT@: Today I almost beat up the Hamster
S@NT@: Bought it
S@NT@: Such a cutie. And there was a dumb one too)
Ник: how are you?
S@NT@: What do you mean. I'm fine
Ник: moving on... yeah.""",
394767: """yyy: You're a fucking hopeless brake
yyy: Although no
yyy: As a brake you're as reliable as no one else""",
394768: """Yesterday daddy (born in the Year of the Rat) was proving that the Rat is the wisest animal, and his main argument was: "Well, who was the Ninja Turtles' mentor?!\"""",
394769: """Pars (09:37:20 12/02/2008)
Brewed some tea. The tag says: "Kings are not born, they become kings thanks to mass hallucination. B. Shaw" 
A very strange inscription for a bag of dry grass.....""",
394770: """Мэлониэль: That's not even the worst. A trolleybus once hit me right on the sidewalk.

kly06: 
how?

Мэлониэль:
I was standing at the bus stop, not bothering anyone. A trolleybus pulls up, drives onto the sidewalk and hits me!!!

kly06:
O_o And what did you do?

Мэлониэль:
Got in it and rode to the institute...""",
394771: """- Damn, my whole dacha is divided into gardening communities. There's one gardening community called "Red Star". So the local gopniks decided to joke: fix the word star to something else. Intellectuals, damn!!!
- Well it's clear what they pulled there:)
- No, it's not clear! It wouldn't be funny. They scribbled over the word "star" with paint and wrote "dick" next to it!""",
394772: """Я: I have a small problem: the last 2 weeks I've been going to bed at 4-5 am, how the hell do I force myself to go to sleep now? knock myself out till half past eight))))
 
Друг: Yeah, the couch is right by the wall. You get a running start on it, into the wall, and good night)))) Mom will come see what happened, and you're asleep and like, not involved))""".replace("\n \n","\n\n"),
394773: """xxx
- what do you do with the truth?
yyy
- I lie it""",
394774: """Дембель: Hey bro, how's life?
Maverik: it's fucked, you won't believe what woke me up in the middle of the night(
Дембель: Come on, tell me =)
Maverik: fuck, damn, my mom's heels hurt after work, so she takes a rolling pin to bed and taps them, like dispersing the blood. Yesterday I fell asleep on her bed. And, fuck, I wake up from the fact that IN MY SLEEP I grabbed the rolling pin and fucking smashed myself on the head with all my might... Now I'm walking around with a huge bump and can't even answer normally where it's from. They'll laugh their asses off, fuck... =((""",
394775: """Ogneslav
AC/DC must be watched and listened to specifically in the concert version! TWICE THE PLEASURE!

Волосатый
no, that's cool in a pub, but at home alone not so much

Ogneslav
the pub works too))) fuck, I want beer!!! and a chick!!!

Волосатый
don't talk about pubs and chicks, I only have 2k rubles left, I'm afraid of a relapse and I'll drink it all away

Ogneslav
you're hugging the cool side of a dark beer mug with your hand... bubbles slowly rise from the bottom... the fogged-up glass beckons... next to you sits a sexy girl, with your other hand you're hugging her by the waist... you take a sip of beer and lower your hand... the girl smiles invitingly

Волосатый 
Fuck, my hands are even shaking

Ogneslav 
around you sit your friends, your favorite music is playing... you're wondering - order more beer or go to the girl's place - she's inviting...

Волосатый 
*ran off to get dressed and dig out the stash, spraying saliva in all directions*""",
394776: """John: Damn, imagine, yesterday I walk into the kitchen and there's a mouse scurrying under the table.
Alex: But you have two cats 0_0
John: Yeah, grabbed the first one and threw it at the mouse... damn and it caught it ;)
Alex: The cat's a hunter, good boy!!!
John: Nonsense, 50% of the success is mine for the precise guidance.""",
394777: """<RiaS_privat> 402 meters in 9.5 sec
Vicarious: from the net?
<RiaS_privat> lol
<RiaS_privat> nissan skyline)""",
394778: """<[Doom] Shadow> That's how you learn some small fact from world history, and you start looking at your own history differently
<[Doom] Shadow> on July 14, 1789 eight hundred Parisians and two RUSSIANS captured the Bastille""",
394779: """<Remedlet> I had such a nightmare o_O
<Remedlet> I was hydrogen, and I had 4 energy levels O_O
<Remedlet> woke up in a sweat
<Remedlet> still shaking""",
394780: """xxx: my girlfriend sent me a photo where she's kissing another girl, in revenge I sent her a photo where I'm in a skirt!
yyy: you found each other""",
394781: """Вовочка has joined ###
<Вовочка> PREVED PLYA!
Вовочка was kicked by Vendy(now get out of the classroom and come in normally!)""",
394782: """today at work I found a file named "verified members of hired employees.xls"
and then: "verified members of fired employees.xls"
and the girl who's going on maternity leave next week was sitting at that computer.""",
394783: """A friend got drafted into the army. Writes to me in ICQ:
kurt (12:26:15 12/02/2008)
the laptop is shitty single-core, cigarettes ran out, coffee got cold, the chair is uncomfortable. no conditions at all for normal military service!""",
394784: """Микроманиак (about the upcoming concert): Venera's inviting me to Tarakanov..... 
Charlotte Simmons: good thing it's not cockroaches inviting you to Venus)))""",
394785: """ххх: fuck, because of you I totally failed to defend my essay today X(
ууу: What the fuck??? what did I do
ххх: Yesterday I asked you what Ivanov's initials are, what did you tell me??
yyy: dunno - I think
ххх: EXACTLY, FUCK, that's what I wrote!!! and his name is Igor Vladimirovich.""",
394786: """Шкв.
have you ever used the history brush in Photoshop?

Тб.
uhhh.. no, only the chalk of fate...))""",
394787: """ууу
Max, did you study Eugene Onegin?
ххх
yeah. how to shoot a pistol.""",
394788: """My girlfriend is underage... we just hold hands and stuff. The day before yesterday I uploaded "literature for personal growth" to her computer. Now comes an sms: 
AAAAA Dad's been reading "Prolonged Sexual Orgasm" from the "from Artem/Books" folder for two hours already.""",
394789: """Lenko: Vasya, I bought a flash drive at the store, they said it would fit most computers. But my laptop doesn't have such a hole!
Vasa: And what kind of flash drive do you have?
Lenko: Well.. it's round on one side, and oval-ish on the other...
Vasa: O_o did you take the cap off?
Lenko: cap?
...
Lenko: AAA! FOUND IT!! THERE IS SUCH A HOLE!! THANK YOU!!""",
394790: """Tezqatlipoca:
damn, how can you discuss project details with a customer whose last name is Carloson

 LMAO.the.DOOM:
yyyyyyyyyy aaaaaaaaaa daaaamn

 LMAO.the.DOOM:
Calm, only calm

 Tezqatlipoca:
Yeah, it's a trifle""",
394791: """<xxx> "vpadlu" is a good word
<xxx> how many times it saved me
<yyy> example?
<xxx> countless
<yyy> at least one
<xxx> can't be bothered""",
394793: """from the corporate chat:
Саша: And how do you hand out salaries?
Бухгалтерия: I call you out from the chat one by one, you're all in alphabetical order there.
Саша leaves the general chat.
Александр appears. Hi everyone!""",
394794: """Кошко: 
Zhenya, you're our poet, come up with a birthday greeting for me
Madking (11:02 AM) : 
for you?
Кошко: 
No, not for me, for a corporate client
Madking: 
Congrats and I wish you
lots of sex and dough
Lots of vodka, to hell with tea
And hashish blah-blah-blah
Кошко: 
Damn, but seriously, I don't have a single thought in my head today
Madking: 
how will you congratulate? Via ICQ?
Кошко: 
No, by letter
Madking: 
how old?
Кошко: 
40
Madking: 
Congrats my virtual friend
And remember 40 years ago
The doctor put the thermometer in rectally
And now the bastard pokes it under your arm
Кошко: 
You bastard
Madking: 
You forty-year-old bastard
You've got a fucking jubilee
To hell with cakes and candies
Better pour it quick
Me and the colleagues wish 
In this wonderful bright hour
That you wouldn't be a fuckup
Stay just the way you are now""",
394795: """<Лена> I work at a cell phone store. A girl comes in asking for a phone "so the alarm screams like crazy, I need it.." then added "and so it's unbreakable\"""",
394796: """solomon: It's my 3rd day having only 2-3 peeled carrots for dinner. I even see better, I practically walk the street without glasses.
Ocelot: seeing better - that's from hunger, the body automatically seeks out prey""",
394798: """Программист: RPC Call – I've already written forty times that the error occurs when the server can't return the query result to the client.
Тестер: Uh, how's that?
Программист: Say you're a salesman in a store. A man comes up, says: "A kilo of sausage and vodka, please!", you turn to the fridge, weigh the sausage, are about to hand it over, and look – the man's gone! That's when you say: Fuck, what kind of Error while dispatching incoming RPC call is this?!""",
394799: """Форман: I really feel sorry for Pozner, all his shows are alike:
Форман:
Pozner - let's talk about hedgehogs!
interlocutor 1 - my dick is 30 centimeters
2 - mine's 31.
3 - I have 2 of them!
4 - I can fold mine in half!
5 - I can eat cucumbers with milk and nothing will happen to me! (Pause, everyone gleefully looks at #5, the hall applauds)
Pozner - well that's all clear, but still, about hedgehogs...
1 - my dick is 30....""",
394800: """Маришко (17:08:46 11/02/2008)
hi.
please tell me how many hours are in a day?

 Маришко (17:11:22 11/02/2008)
12-12-2007 09:15:18     12-12-2007 13:08:33     3h. 53min. 15sec.     71,912     0
12-12-2007 15:57:16     12-12-2007 15:58:36     1min. 20sec.     71,912     0
12-12-2007 15:58:45     18-12-2007 00:21:52     128h. 23min. 7sec.     71,912     0
12-12-2007 18:40:08     18-12-2007 00:21:52     125h. 41min. 44sec.     71,912     0
12-12-2007 18:40:36     18-12-2007 00:21:52     125h. 41min. 16sec.     71,912     0
12-12-2007 18:41:12     13-12-2007 00:45:59     6h. 4min. 47sec.     71,912     0
12-12-2007 18:41:59     13-12-2007 00:45:59     6h. 4min. 0sec.     71,912     0
12-12-2007 18:43:28     13-12-2007 00:45:59     6h. 2min. 31sec.     71,912     0
Total for the day:     401h. 52min. 0sec.     575,296 bytes     0 rub.

 Вандруша (17:11:50 11/02/2008)
what's this

 Маришко (17:11:54 11/02/2008)
this is the internet connection stats for 12.12.07
I really want to understand how many hours are in a day, maybe I missed something?""",
394802: """apelsinka (15:15:04 11/02/2008)
good one, we have one like that running around at our checkpoint too, only fluffier and with such a good-natured mug, I bring him sausages, so now he recognizes me and plays with me 

 Лёха (15:16:10 11/02/2008)
damn, I wish you'd bring ME sausages, I'd play with you too........""",
394803: """Dreiko to Dzirt_DoUrden
Kot, as a site creator you've fallen in my eyes... But now I know you're a hellish humorist and a person with non-standard logic. And now, if you're suddenly interested in the reason for this appeal, answer me one question :( I understand of course - absinthe, first exams and stuff, but how much of the "green fairy" did you have to blow to make site registration available only to registered users? O_o""",
394804: """<Mil@$hk@>Mish! We need to talk alone!
<@Ham_ster>Heh... alright
Raptor was kicked by @Ham_ster.
CTADO_6APAHOB was kicked by @Ham_ster.
...
Антон was de-oped by @Ham_ster.
Антон was kicked by @Ham_ster.
<@Ham_ster>All done, now you can speak freely)
<Mil@$hk@>Well actually I meant private!""",
394805: """Начальнег: Dmitry, if our Internet were an Indian, his name would be "Big Morning Dick". Fix the proxy already, or your monthly bonus is at serious risk.""",
394806: """Flag: A classmate returned from France - the distinguished ones were sent there on exchange to take a course with famous professors. At the very first class on "some computing software whose exact name I don't remember" the professor pompously asks "who of you is familiar with C/C++?". Immediately a forest of hands goes up and the professor, annoyed, mutters toward the board "ah yes... they're Russians...\"""",
394807: """она: You got a message from the number "Dean's Office"
она: They're wondering whether "their bunny" woke up or not yet
она: So now you have two options - either we go to the dean's office together, and you greet your dean in front of me with the words "Hi, Bunny"
она: or you drive up to my place to pack your things under the windows!!!

он: daaaamn
он: Let's go to the dean's office :-*""",
394808: """<Король Вселенной> strange, the letter "я" started sticking on my computer""",
394809: """WOTW 
Yesterday finished reading Prada
 
 WOTW 
Damn, great book

 Локки 
what Prada?

 WOTW 
The book. The Devil Wears

 Локки 
what do you mean the devil brings you books???

 Локки 
damned satanist""",
394810: """Я пишусь с бывшим начальником:
-Rate this:  Pavsanias — a young Macedonian, bodyguard of Philip II of Macedon, who killed that king. 
In short, in two words - a nobleman raped Pausanias, he went to complain to king Philip. But Philip also raped Pausanias. The guy got offended and stabbed the king.
-Is the moral like - don't fuck with your subordinates or they'll stab you?
-No, I'm saying that there were faggots even cooler than you...""",
394811: """Ann: slow dances are good for resting and thinking... and not only thinking ;)
Orbiter: yeah yeah... do you have sex to music?
Ann: what sex? I'M MARRIED!!!""",
394812: """ХХХ
Scenario for the future inauguration:
in the Andreevsky Hall of the Grand Kremlin Palace Putin, to the applause
of those gathered, solemnly bites Medvedev on the neck and he turns into
the president...

УУУ
lololol...

УУУ
I wonder what Yeltsin bit Putin with... or is it transmitted some other way?""",
394814: """A wonderful idea was born for selecting employees for an IT company: take a complex puzzle, assemble it halfway and offer candidates to assemble it to the end. Depending on the result:
- candidate assembled the puzzle correctly and to the end - the specialist goes to the development department;
- candidate broke the puzzle during assembly - the specialist goes to the testing department;
- candidate disassembled the puzzle down to zero - the specialist goes to the analysis department;
- candidate said that the half-assembled puzzle looks fine as it is - the specialist goes to the implementation department;
- candidate didn't notice the request to assemble the puzzle - aiming for project manager.""",
394815: """Мельников: Morning. Walking down the stairwell to the street. All over the stairs abundantly scattered printed A4s with the inscription repeated 3 times: VITYA! I LOVE YOU!!! Lots of them. The whole staircase, below - the stairwell landing. I go outside. There's a white Toyota standing there. Completely covered up to the roof with these flyers. They're everywhere: on the windshield, on the roof, even the antenna is wrapped in them and tied. And all of it was DOUSED WITH WATER to make it stick... And it was down to minus ten outside at night. Probably a nice girl, though I feel she doesn't have long to live...""",
394816: """Камент к касперу 7:
Went to a friend's to drink some beer, and they're pulling a home LAN cable (+internet) to his place. So I tell him to install KIS just in case. Connected to the internal net, squealing across the whole apartment, I write to the admin in PM - like, respected sir, viruses are jumping across the net, maybe catch them.
The answer just killed me 0_-:
"-You guys with your Kaspersky are annoying, install NOD, IT DOESN'T SEE THEM!\"""",
394817: """dj_kedr: a normal company's anthem should be serious, singing about killed enemies, burning villages and the wrath of northern gods, and containing at least one 5-minute guitar solo""",
394818: """I work at a service center. Another repair request comes in. I read: Beko refrigerator; reported malfunction - rattles loudly during the spin cycle. ...these new technologies...""",
394820: """ххх: fuck!

yyy: what happened?

xxx: yeah, fought with Nadka again, fuck

yyy: well dump her!

xxx: how do I dump her? I love the bitch

yyy: sooo... correct me if I'm wrong. she herself offered you to fuck, came only when it was convenient for you, brought food... and you didn't react to her at all. right?

xxx: well...

yyy:and then she found a job, started going to parties, drinking all kinds of crap like vodka, coming when she likes more, and totally wasted, ignoring you, and stopped putting out?

xxx:yeah... what do you think is the matter?

yyy:you know, friend, the thing, I suppose, is that you're a moron... the main thing is don't worry, everything will work out :-D""",
394821: """Infidel: I will find that asshole builder who placed the fire suppression emergency button next to the office light switch...""",
394822: """Zailon
On Thursday I left on a business trip to Ivdel! Came back Monday, they managed to break everything!:) fuck! With the words "Well why the fuck is it so noisy" they stuck a pen into the CPU cooler!
 Денис
"ROFL"
 Zailon yeah! And I also entrusted the Server to the chief engineer! Dunno fuck what happened, but the bitch is on sick leave, and I'm buying a new server.""",
394823: """gella:
A girlfriend came over, we drank champagne for the holiday... She left...
gella:
then the director came and asked "nobody understands me, but you're drunk now... let's go burn the Christmas tree?\"""",
394824: """Андрей (19:24:48 12/02/2008)
Cool! I got really lucky today! Came to class not knowing there'd be a written test! 5 topics! Quickly read the first one. Didn't have time for the others. And that exact one came up! Wrote everything! 

 YF (19:25:04 12/02/2008)
typical you

 YF (19:25:08 12/02/2008)
if it weren't for luck, you wouldn't exist at all""",
394825: """m0zg (19:57:34 2/02/2008)
guests came to my place, so I'm off to entertain them  :) well we keep it simple, Russian vodka, pickles, meat, potatoes ;)

 m0zg (18:39:11 12/02/2008)
well holy shit did we have fun :O""",
394826: """OPOZING: I might come to the clan meetup with my girlfriend. That won't scare anyone?
wash: possibly only the girlfriend...""",
394827: """Отец works as a taxi driver (О).
Arrived on call to the private sector. Waits 20 minutes, nobody's there.
Calls the dispatcher, like, where is everyone? So the dispatcher called the customer. An auntie (Т) comes out and her first question: "And where is HE?!"
О: Who?
Т: That fool.. I told him to stand here and wait, moron..
Starts calling the man (М) on speakerphone.
Т (yelling): Where are you, asshole?!
М (drunken voice): I'm already on my way..
Т: where to?
М: in a UAZ, with the cops...""",
394828: """>What native fighting games are there for linux?
Gnome vs KDE
(c) LOR anonymous""",
394829: """TZar: Gopniks mastered a new method. At first I didn't even get it ) Going down to the university lobby, 4 dudes come up (right in the lobby!) And say, like, do you know Sasha from your year, how's he doing. They look decent enough. Started spinning something, then say, like "today's a holiday, let's celebrate together" I'm in doubt, and then one says "write down my number, we'll call each other tonight and meet up" Then I got it all ) I dig into my bag, I see his eyes are already joyful and he's looking around for cops.

izgoj: and?

TZar: And I pull out a ballpoint pen )) And say "dictate". )
TZar: I'll never forget his eyes )""",
394830: """1. Why'd you come home from work so late?
2. Was pumping today...
1. )In Lineage?
2. :-D no, at the gym! also, you know, an interesting game! gathered a party of three today, then another highlvl joined us, and we went raiding the barbell! it's this boss, STR and CON level up well on it! did three sets!
1. O_o how was the drop?
2. sucks... one dude dropped a 10k plate on his foot...""",
394831: """111
OK so how did you watch hidden files before that 
222
I didn't
222
I haven't watched hidden files for like half a year
222
I have a girlfriend""",
394832: """мер†вец
And on what grounds does the tax office bury you with letters demanding money?
 Cyklop
Well, I forget to file some crap. And they write a letter that I'm a goat, a letter that I owe them money, a letter that they'll take me to court, a letter from the court, a letter about the court decision, a letter from the bailiff service, a letter that they blocked my account, a letter that the bailiffs took 16 kopecks from my account, a letter that my account is unblocked, but I'm still a goat
 Cyklop 
Really bad for karma""",
394833: """Ananist: I don't celebrate Valentine's day!
Gress: looks like you don't need to...""",
394834: """Ruvo: so, I was told to stop by the pharmacy and buy cough medicine for my father-in-law. The pharmacist asks - is the cough dry or wet. 
Ruvo: I say - it's not me, so I don't know. She's like - fine, how old is your child.
Ruvo: well at first I was confused, then honestly answered: our child is 54 years old, and since he's coded, the medicine must not contain alcohol.""",
394835: """Annette00: oh man... When at 2 fucking AM he gets the urge to make-fix something, when he clatters tools across the whole apartment, when he runs back and forth, stomps and actively figures out where he put the awl, oh HOW fucking much I want to tell him WHERE his awl is...""",
394837: """ <xxxx> so did you quit computer games?
<Sgt. Joker> Yeah, back in the day my dad dripped on my brain like, games are evil, you only waste money on the computer and your time and stuff.
<xxxx> heh, did you really heed the voice of reason?
<Sgt. Joker> nope, I decided to show him that the computer is quite interesting
<Sgt. Joker> Bought him a Nautilus disc (my dad's a fan of theirs) put it in Winamp, and showed him how to play Lines.
<Sgt. Joker> that was a fatal mistake.... ever since then *I* don't play on the computer...""",
394838: """FaultcorE: (walking past a soap store) soap in heart shapes everywhere, everyone has Valentine on their mind...
KRioGeN: And what should everyone have on their mind? The elections? Soap shaped like Medvedev?""",
394839: """ххх (22:22:55 3/02/2008)
today standing with a friend and old ladies walk by one after another in single file

ххх (22:23:05 3/02/2008)
they walk and gossip and gossip

ххх (22:23:14 3/02/2008)
we called them "the caravan of stories\"""",
394840: """xxx:
I even washed the couch with Vanish... otherwise soon you'll be able to sit down and get pregnant""",
394841: """Таня (21:56:14 13/02/2008)
what do you think, will mom notice the good dinner more, or the absence of cats?""",
394842: """Feedthe: I don't take seriously people who were born after Windows 95.""",
394843: """<freeze> no, actually girls like kind, cheerful, responsive guys, with humor and able to come to the rescue at any moment
<freeze> I know from myself =)
<freeze> I'm going by contradiction =(""",
394844: """<Scuko> so why didn't you haul the kegs?
<3ашибись> well we stopped by the store
<Зашибись> Lyonka right away: give us three kegs, the works
<3ашибись> and she to him contemptuously: isn't it a bit small? are you 18?
<Зашибись> and this dumbass to her: haven't measured... and to drink beer does it have to be longer than 18?
<Зашибись> long story short, we didn't haul ourselves to another store for beer""",
394845: """ххх
Yeah I barely see you((( You're either at work or behind me...""",
394846: """Vova And I don't understand why an electromechanic needs philosophy, sociology, religious studies, psychology, pedagogy and law?

lovesun what do you mean why? Horizons! 
lovesun I'd also add painting and cultural studies. 

Vova We had cultural studies in the second year. Instead of painting we had engineering graphics. There were no horizons and I hope there won't be.""",
394847: """ыч: I sense this picture in the future: A new operating system codenamed Windows Brallion will come out in 2023, this is due to the failure of Windows Nescolt. For the same reasons Windows XP sales are extended until the end of 2022.""",
394848: """Mitry: heard they're preparing to shoot a thriller in Hollywood about the October Revolution in Russia. Bruce Willis will play Lenin, Demi Moore - Krupskaya.
Snider: holy shit )) Lenin saves the world ))) personally shoots Kerensky with a six-barreled revolver ))

Snider: tears the angel off the PEtropavlovka with his bare hands )))

Snider: and in the final five minutes fights off the Entente troops with the Alexandrian Pillar, carefully putting it back in place after the brawl )))

Snider: as a separate scene - a kiss with Krupskaya on the balcony of the Kshesinskaya mansion and a speech about American patriotism from an armored car against the backdrop of the shelled Winter Palace ))

Snider: damn...  let me write the script !!!!!   I'll write it for free (since the screenwriters are on strike there anyway) !!!!!

Серджио: and who will play Fanny Kaplan????? )))

Snider: a logical question ))) I think due to political correctness - Halle Berry )) we-e-ell or Whitney Houston )) much more interesting is the casting for Inessa Armand ))""",
394849: """Roman-S:  how did you meet Lena?
Greezly:  first in WoW through private messages o_O I played a dwarf and she a night elf, she helped me do a quest in Stranglethorn, since I was still a little shit then, only level 31, and she was 36. Then she gave me her ICQ and later we met.
Roman-S:  and where did you meet the first time?
Greezly:  don't remember ((""",
394850: """damn, the programmers here are so funny... REAL ones! I mostly had freelancers, they dress more or less decently and take care of themselves. 
And here they're downright classic kosher programmers. With beards, with mustaches.... Tucking their sweaters into their jeans... You can tell right away - professionals.""",
394851: """111: Where's your subordinate? Why don't I see him at his workplace?
222: He asked for time off.
111: What do you mean asked for time off? 
222: He said that working after 6 in the evening on Friday he spins on his dick.""",
394852: """Dzirt_DoUrden: Near the medical university there's a hot dog kiosk at 11 rubles (!), I walk out of the university, observing a sight for sore eyes
Dzirt_DoUrden At huge speed a fat, out-of-breath girl runs and at speed devours the hot dog in giant bites. 
Dzirt_DoUrden: Behind her run 5-6 (!) classmates yelling - Bitch give back the hot dog! WE CHIPPED IN TOGETHER!""",
}

import sys

def main():
    in_path = "/home/hatch/workspace/translation-work/bashim/chunks/chunk_13.jsonl"
    out_path = "/home/hatch/workspace/translation-work/bashim/out/chunk_13.jsonl"
    in_lines = open(in_path, encoding="utf-8").read().split("\n")
    assert in_lines[-1] == ""
    batch = in_lines[600:700]  # input lines 601-700 (0-based 600:700)
    assert len(batch) == 100, len(batch)
    recs = [json.loads(l) for l in batch]
    assert len(EN) == 100, len(EN)
    missing = [r["id"] for r in recs if r["id"] not in EN]
    assert not missing, missing
    out_recs = []
    for r in recs:
        en = EN[r["id"]]
        assert en, r["id"]
        assert en.count("\n") == r["text"].count("\n"), (
            r["id"], en.count("\n"), r["text"].count("\n"))
        assert "\ufffd" not in en, r["id"]
        out_recs.append(json.dumps(
            {"id": r["id"], "ru": r["text"], "en": en}, ensure_ascii=False))
    with open(out_path, "a", encoding="utf-8") as f:
        f.write("\n".join(out_recs) + "\n")
    print("appended", len(out_recs))

if __name__ == "__main__":
    main()
