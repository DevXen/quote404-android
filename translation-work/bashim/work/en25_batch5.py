#!/usr/bin/env python3
"""Translate bashim chunk_25 batch 5 (input lines 401-500, ids 407474-407573)."""
import json

EN = {
407474: """xxx: And I'm in Komsomolsk-on-Amur right now! Went to visit relatives...
yyy: Big time difference?
xxx: Yeah - 7 hours. It's already past midnight here
yyy: So how's the future?
xxx: Don't watch The Ring 2 - it's crap....""",
407475: """xxx: listen
xxx: settle a dispute
yyy: I allow...""",
407476: """Tycho: I had a friend who thought that if you make the font on a page smaller, it takes less traffic to display it.
Arkon: yeah, I had a friend like that too, with a similar diagnosis
Arkon: she even "proved" to me that it loads faster that way
Arkon: first she'd load the page with normal text, time it
Arkon: then she'd make the text scale ooovery small and refresh the page. everything loaded in seconds.""",
407477: """Talking with my friend (F). She asks:
(F): Listen, do mosquitoes bite you?
(Me): Doesn't seem like it, and you?
(F): Damn, yes! When they bite your ass... They suck out all your brains!""",
407478: """From a conversation with a classmate:
me: did you walk the dog?
him: no. we had a fight.
me: and what, he's holding it?
him: no
him: that's why we fought.""",
407479: """Manyunya: yesterday like a true lady I decided to take a bath with sea salt, to relax - scratched my whole ass on it!""",
407480: """Forsaken: *wiping red eyes* Humanity needs to invent a fumitox that repels the nighttime riffraff under the windows...""",
407481: """<mellik> damn, 80k more and I could get a Corolla
<BACb-BACb> take a loan
<mellik> I already have a loan
<BACb-BACb> which one
<mellik> bank "ex-wife". sasha and masha. for 18 years""",
407482: """xxx: Fed up. Heat + my own idiocy. And the soup went sour.
yyy: soup became the cause of a programmer's death - news feed headline!
xxx: programmer became the cause of the soup's death =)
yyy: double suicide in a rented apartment !""",
407483: """xxx: I'll go buy an electric stove, start the collapse of Gazprom ))))""",
407484: """x: Now I get what a pain it is to browse the net via a regular phone
Y: try via mobile, it's easier!""",
407485: """Mango: I wonder, when you download one browser through another, does it get offended?
Totiyango Evgenievno: well Explorer has long made peace with it )""",
407486: """ruokoc> hooked up cable, flipping channels. the name USA DBA catches my eye. I think: "oh! a channel for DBA admins. American ones" decided to watch. turned out to be the Usadba channel.""",
407487: """nico-izo: Dear Gentoo users, it's mockery to send instead of a trailer an archive with frames and a separate audio track! =\\
lol: be thankful they didn't send sheet music.""",
407488: """solitaryplace: what's "starcraft"?
shiva1: star craft
shiva1: probably like burning hydrogen to get heavy elements""",
407489: """xxx: a lady just came by
xxx: asked me to tell her where to find the server
xxx: her computer told her "server not found" - so she went looking
...
xxx: and she almost found it - the server room is two offices away from us
xxx: women's intuition""",
407490: """Finally trained my beloved wife to use the "print" button. Before she used to rewrite by hand a twenty-item table from Excel. Neatly ruling it with a ruler, she'd go to the warehouse, fill it in and then transfer the data back into Excel. To my question: "Why don't you print an empty table?" she'd answer "It's more convenient this way". Well whatever, today she delighted me again.
She writes an SMS:
her: what address should I send the letter to? (I have three).
me: Any
30 minutes later SMS:
her: Did you get the letter?
me: no, where did you write?
her: I sent it to myself. Log into my Yandex mail. You know the login and password. The letter is marked "for husband"
What can I say... I love this woman!""",
407491: """xxx: my mom advised me to take example from my elf girl in L2 =(
xxx: like, she's cool, runs all the time and has a good figure =(""",
407492: """xxx: And in 2012 there'll be a big holiday for Orthodox programmers: 1024 years since the Baptism of Rus :)""",
407493: """her: who's dearer to you? me or StarCraft?!
him: you of course!
him: weeeeell, StarCraft too )
him: I've known it 10 years, and you only 8 ))""",
407494: """*qй saw an abstract artist's painting - "Penguin eating an apple, looking out the window."
<qй> I tell the artist: "you forgot the little devil"
<qй> And he rolled his eyes: "How you all annoy me! What makes you think there should be a devil here?!" """,
407495: """The_God: Listen, what's the best way to cut a watermelon? Pole to pole, or along the equator?
denien: DON'T!!!""",
407496: """SOU: listen - here's an idea: make images of the old system before demolition, burn them to DVD and bury them like the souls of the dead.""",
407497: """xxx: How can you show such movies to a kid?!!
ccc: Well what. There aren't that many corpses. Plus, from fear he hides under the blanket and falls asleep....""",
407498: """Him: want me to toss you a movie?
Her: nah.. no time for movies at work...
Her: I'm reading a book..""",
407499: """<yp107> laziness must be cultivated, work on its quality, improve and irrigate...
<Obscure_me> but lazy""",
407500: """HooliGaN
yesterday I rode a shopping cart

HooliGaN
shopping with my parents, they took groceries from the shelves while their grown-up 26-year-old halfwit son raced around on the cart. They can be proud of their son""",
407501: """Die Kirsche: also realized this... there's nothing nicer at work than when they chew out the one who used to chew you out B)""",
407502: """XXX: my cat at school really loved swimming
YYY: your cat went to school?""",
407503: """xxx: today my son asked what nostalgia is..
xxx: only one example came to mind:
well when you're 70, and you suddenly stumble on an iPhone with the last Harry Potter book you never finished... you'll pick it up, inhale the raw smell of plastic, and think, it was so recently...""",
407504: """xxx: happy professional holiday to you)
yyy: xxx, I'm a sysadmin in name only )
xxx: and the beard?
yyy: and the beard's from having nothing to do""",
407505: """Ping.Win: And would you jump with a parachute if you knew I packed it?

fox15: A parachute packed by a linuxoid programmer naturally "inspires confidence".

static: fox15, what? a linuxoid would've put fabric, a sewing machine, straps and sewing instructions in your backpack""",
407506: """xxx: I was at a sushi bar today )
yyy: Ate with chopsticks?
xxx: no, with instruments :)
yyy: Voltmeter, Ammeter?""",
407507: """topic on a forum:
xxx: want to buy a turtle. what breeds of turtles are there? how much do they cost? where to buy? can it live in a 47-48 cm aquarium, there's no more room on the desk?

yyy: get a ninja turtle, no aquarium needed, unpretentious, eats pizza, you won't regret it""",
407508: """<мама> and don't forget to watch your periods!
<дочь> what is there to watch? there are none.""",
407509: """her: *in depression* You don't understand me!!! You don't even get PMS!!!
him: I understand. You get it - that's enough for me.""",
407510: """TASHA: No, some hacker set it for me, much bigger!
pecheneg: you call a person a hacker if he managed to change his age on ICQ?""",
407511: """homm: They should finally make closing a window by double-clicking the window icon, like it's been in Windows since 3.11
r3r: Until this comment I didn't even know there was such a feature.
develop7: It's in the manual.
yul: until this comment I didn't know there was a manual.""",
407512: """Lena: ever heard of viruses breaking the hardware of a comp?
SoldaFon: yeah... Win32.Kofe.prolil.aa :)""",
407513: """xxx: I have 3 principles in life
yyy: inheritance, encapsulation and polymorphism?""",
407514: """Karisma: Just posted my new mix on the walls of DJ friends on VK. Felt a bit awkward, like deciding to gift Torvalds a disk with the new Windows""",
407515: """xxx: And at our work they fired the loader yesterday over the plan.

yyy: Didn't meet it?""",
407516: """Feo: can't imagine how people lived without comps before? Must've been so boring..
Mr.Angry: Uh-huh. Balls, duels, hunting, fairs, bloody repressions, nomad raids of all kinds, palace coups. Really nothing to do!""",
407517: """xxx: Today we went to the countryside for the weekend to celebrate sysadmin day, to rest from work
xxx: managed in the deep forest to find savages like us, and beat them at Counter-Strike for beer
xxx: just don't ask where we got a diesel power station, a router and 5 laptops in the wild""",
407518: """xxx: It's because they could allocate a billion to restore housing for those whose homes burned. Instead they'll allocate a billion and a half to create Assessment Commissions, Operational Headquarters for Problem Solving and bonuses for officials who proposed "innovative methods and solutions\"""",
407519: """xxx:
News on Yandex:
2 thousand militiamen will ensure the safety of paratroopers' festivities

xxx:
Indeed, what if someone offends them?)""",
407520: """xxx: my comp seems smarter than me (
yyy: why's that?
xxx: was clearing space on the drive for WoW. The comp suggested deleting the unused folder "girls\"""",
407521: """xxx: read the traffic cop book, there are only 3 poses)
yyy: which book are you talking about? :)""",
407522: """Smyek: dreamed I'd hit the bike at night going to the toilet. I woke up and really hit it when I went
xxx: and so?
Smyek: I don't have a bike""",
407523: """Sirion: Judging by the sound, the trolleybus is preparing to make a hyperspace jump.
Mr.Spleen: Judging by the sound, it's preparing to fall apart as fuck.""",
407524: """An old friend stopped by the other day passing through. By the will of fate the guy's from St. Pete, a punk, and works at a construction site.
We have a cultural monument here - the churches on Kizhi island. According to legend, built without using nails.

I explain all this to the guy. He thinks for a couple seconds, then blurts:
"Wait, on self-tapping screws then?\"""",
407525: """<koi-8> and anyway, House reminded me of my advisor. practically in everything. except the limp.
<koi-8> it was like this: Shalom, she'd say. - Do you know where to sign the pass to the 21st department of the Ministry of Education? You're twenty-three already and still don't know? Scram to that hag Romanova. The one with diabetes. Buy the candies at the kiosk. Get Korkunov. I need the pass in half an hour.""",
407526: """xxx: hurry up September 1
yyy: what do you care, you don't study anymore
xxx: well at least working won't feel as bad )""",
407527: """UltimaThule: And also the cat bit me and now they're giving me rabies shots. Just in time for the fest I'll get a shot. :-D

Radiance:
What cat? A wild one?

UltimaThule: That's the thing, it's my own! We took him to the vet yesterday, he mauled two people there: me and some other guy!

UltimaThule: They took him to cut his balls. I feel he somehow found out... Mauled everyone and ran away! Now he lives free, but with balls.""",
407528: """XXX: Would you trade sex for 2 jars of jam?
YYY: What kind of jam?""",
407529: """XXX: Yeah. They made me pay for the internet because his beans could wither in some online game...""",
407530: """Ilya Shmyrov:
after 20-rep squats you don't feel the heat for hours at all))))

Kon Vpalto:
because you're lying unconscious?..""",
407531: """acolight: This is Andrey Andreevich, switched from World of Warcraft to heroin.
acolight: *ovation in the hall*""",
407532: """xxx: Before, when guests came to the house, they were offered a cup of tea.
xxx: Now - the WiFi password )""",
407533: """Client:
Tell me please, can I buy a used automatic transmission for a Peugeot-406, VIN ............?
Dispatcher:
What exactly broke on yours? Maybe only a part can be replaced?
Client:
Specifically - the car at about 130 km/h started screeching like a flock of rabid canaries. When I stopped and opened the hood, smoke was pouring from the gearbox breather. So I think just one part broke - the automatic transmission.""",
407534: """Amvitel: going to work?
MarishkO: I got suspended...
MarishkO: Decided to open the window in the CEO's office, put a huge cactus on his chair, forgot, and the CEO didn't look and plopped down...
Amvitel: You fulfilled the whole department's dream XD""",
407535: """xxx (works at a medical center):
the admins are laughing here.
our users make up their own passwords, so the urologist wrote PROSTATA2010, and the ophthalmologist - Resnichka79""",
407536: """xxx: Oh, those unclosed caps, wrongly squeezed toothpaste tubes, abandoned combs - how many boats of family life they've wrecked on the bathroom tiles.""",
407537: """ggg: love our clients.
ggg: they furiously bring our estimate and spit saliva: "we asked to split everything EQUALLY!"
kkk: and what's wrong?
ggg: well I say "how do you order me to equally split FIVE chandeliers and ELEVEN floor lamps?""",
407538: """Sergey:
The negotiations book is called "Getting More". How best to translate into human?

Reine:
how to get more

Sergey:
Nat (18:40:38 2/08/2010)
The Getting Sea

Sergey:
learn""",
407539: """xxx: you know how this summer differs from the last one?
xxx: back then on the "Cold Beer" sign we first noticed the word "beer", and now - "cold\"""",
407540: """xxx: what do you even do there?
yyy: burning peat bogs, laughing""",
407541: """xxx: let's go for a walk, my mouse battery died.""",
407542: """4_us: the day before yesterday I was hauling the last thing to the apartment
4_us: left the most needed, so to speak, for last
4_us: and here I am carrying it, straining, it was getting dark already
4_us: a group of youths of like 4-5 passes by
4_us: they saw me - fell silent, when they got closer
4_us: then they burst out laughing, I nearly dropped the heater""",
407543: """xxx: After two years of waiting they finally ran unlimited.
xxx: In the tired humming of the hard drive I can hear more and more clearly "pot, don't cook"...""",
407544: """valexey: everyone's concerned with the speed of program development, not the speed of programs :-)
zert: helps utilize more resources
valexey: get to market faster and stake it out!
zert: yeah, go to the center of the market, take a huge dump and stand next to it
zert: like, look what a good boy I am""",
407545: """Alisa: Yeah, but you annoy me in many ways. For example! You seem sociable, but when it's a serious conversation - you pick from it only what you need!
Alisa: You're such an interesting damn guy, when it's about something random you're ready to write a ton of words, but about problems - you totally ignore them!
Meatl: Yeah, I was told I'm interestting""",
407546: """xxx: our village is dying out
xxx: there's so little youth here they don't even sell condoms in the store""",
407547: """Thinking about the bugs in my code I'm making myself tea. I throw a tea bag in the mug, then coffee, then honey and finally sugar. Pour water. The sysadmin sitting next to me silently watching this shit gets up and adds condensed milk to the hellish drink. Looks at me sadly and says: "Epic fail, right?\"""",
407548: """111: Fuck!!! no money, can't find a job.
222: Star in porn.
222: You'll get money, and we'll find out which of our acquaintances watches porn.
111: How will we find out?
222: They'll giggle, whisper and point fingers.""",
407549: """gorkoff: Like my classmates, I got hooked on Lineage. Been measuring for a week, still 17. Can someone explain how they pump up?""",
407550: """A friend teaches her five-year-old son to ride a bike.

friend: I'll let go of the handlebar now, and you'll ride on your own.
son: no, don't let go. I'll fall and crash.
friend: well, you already know how, if you always think about bad things, bad things come true. You need to think about good things, then good things come true.
son(after five minutes of silence): So where is it?
friend: what "where"?
son: Where's the Spider-Man costume?""",
407551: """Olga: how's... morning?
Thrasher: Working
Thrasher: launched AutoCAD before Mozilla""",
407552: """xxx:
Google news:
Shamanov: there are still many willing to serve in the Airborne Troops.

xxx:
At first I didn't notice the colon )))""",
407553: """Lena (10:19:11 3/08/2010)
my husband has a friend working as a taxi driver, so tonight during his shift a huge rottweiler ran out under his wheels, smashed the headlight, dented the fender and the passenger door, and shit on the back door out of fear.
the rottweiler was wearing a telnyashka.""",
407554: """<UncleFedor> fuck, what kind of sclerosis do I have with dumplings!
<UncleFedor> either forgot to turn off, forgot to buy, bought - forgot to cook
<UncleFedor> today the apogee - bought, put to boil, water boiled - realized I forgot the dumplings at the store""",
407555: """Talking with my own sister, describing my chest pains, then it turns out she's feeling the same right now. Her husband sitting next to us sums up:
- The whole batch is defective!""",
407556: """ppp: a girl once dragged me to Sunday school
ppp: when I walked through the gates, I saw poor hungry grannies. through the same gates the church rector drove in on a new Mercedes
ppp: the lecture topic was "we must share and help our neighbors"
ppp: after the question-suggestion "let's sell the priest's Mercedes and feed the grannies" they expelled me from the school...""",
407557: """sss:
hi
haven't seen Oleg since 11:00?

ttt:
Hi!
How do I hint this gently...
Basically, I haven't worked for you for a month and a half!""",
407558: """19N4T0V: parents can't understand programmer tricks
19N4T0V: either "Why do you need a USB flashlight on your glasses?", or "Why do you need a USB fan?", or "Why do you need a toy USB machine gun?", or "Don't roll buns on the floor"...""",
407559: """Achyokak: come over, we'll look at your laptop
Amor: sec, let me eat
Amor: don't think I feed on electricity""",
407560: """Phone conversation, she's home, he's at work:

Her: Sash, listen, what do I do, I can't screw a screw into the wall, it keeps going sideways?
Him: Just hammer it!

Him (calling back 5 minutes later): Uh... I mean don't touch anything, I'll do it all in the evening! Did you already hammer it?!""",
407561: """bbb: are you a twin?
uuu: no, a cancer
bbb: I see...
uuu: actually cancers have many virtues!
bbb: yeah, crayfish are tasty""",
407562: """lll: can you lend me 3k rubles?
ccc: I only have 5 in one bill, will there be change?
lll: I'll go borrow 2k from the neighbor now and there will be
ccc: I think you just became a victim of your own financial pyramid""",
407563: """segr> and I failed the captcha at the draft office...
yarik> where?
serg> couldn't recognize the digits in the eye doctor's book!""",
407564: """Glum: I found a way not to oversleep in the morning, it's simple, we motivate the neighbor who has to go to work early in the morning by blocking his car and hanging a note with the apartment number and phone.""",
407565: """From a chat:
xxx: Sorry, stepped away. Did I miss something?
yyy: A hyphen.""",
407566: """A guy writes from the army.

Drinker: yesterday we buried the immersion heater
oni-chan: you killed the heater?
Drinker: No, alive
oni-chan: hid it? or was it guilty? burned the captain?
Drinker: No, the captain found it during a shakedown and made that room where he found it bury it
Drinker: The guys dug a 2x2x2 meter pit
Drinker: Then stood at attention and sang the anthem when the heater was carried into the pit on a pillow of moss""",
407567: """AiR: I totally screwed up today. We're on the roof mounting the big letters of the ZARYA company. I'm standing behind the last one with a drill and then the boss calls.
- Where are you?
- Here, behind the "Ya"
- What the fuck is "zaya"???""",
407568: """<zubbord> the tax office told me today my declaration is good :)
<AL> soft?""",
407569: """newtochka: I want everyone to live long and happy..
newtochka: and die on the same day :|""",
407570: """M: listen, do condoms without lube still exist?
N: What, found someone? )
M: No. Just thinking what to wrap my phone in to hang out on ICQ in the shower.""",
407571: """steel: such a modest, shy girl in the photo, but in interests "pastel". You meant "sleep", right?
pure_v: *facepalm*""",
407572: """from "Complaints about forum and chat moderators' actions"
a: moderator xxx gave me a 7-day ban for the word pider! others get 5 minutes, lift my ban
b: the ban was given for insulting the site's chief administrator
a: is he special??????""",
407573: """Xxx: The thief's hat is burning :)
Yyy: a cap!""",
}
